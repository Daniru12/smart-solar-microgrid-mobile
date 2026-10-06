package com.example.smartsolar.features.backoffice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.backoffice.models.BackofficeProsumer
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import com.example.smartsolar.ui.theme.LimeAccentDark

private val DangerRed = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackofficeProsumersScreen(
    viewModel: BackofficeViewModel,
    token: String,
    modifier: Modifier = Modifier
) {
    val prosumers by viewModel.prosumers.collectAsState()
    val deactivations by viewModel.deactivationRequests.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(token) {
        if (token.isNotBlank()) {
            viewModel.loadProsumers(token)
            viewModel.loadDeactivationRequests(token)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Tab switcher
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = CharcoalText
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Text(
                        "Prosumers (${prosumers.size})",
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Deactivations",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                        if (deactivations.isNotEmpty()) {
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(DangerRed)
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    deactivations.size.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            )
        }

        Spacer(Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by NIC or Name...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, "Search", tint = GrayText) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = CharcoalText
            )
        )

        Spacer(Modifier.height(12.dp))

        if (selectedTabIndex == 0) {
            // All prosumers tab
            val filtered = prosumers.filter {
                it.nic.contains(searchQuery, ignoreCase = true) || it.name.contains(searchQuery, ignoreCase = true)
            }

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No prosumers registered.", color = GrayText)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered) { p ->
                        ProsumerCardItem(prosumer = p)
                    }
                }
            }
        } else {
            // Deactivation requests tab
            val filteredDeactivations = deactivations.filter {
                it.nic.contains(searchQuery, ignoreCase = true) || it.name.contains(searchQuery, ignoreCase = true)
            }

            if (filteredDeactivations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.CheckCircle, "None", tint = Color(0xFF10B981), modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No deactivation requests pending!", fontWeight = FontWeight.Bold, color = CharcoalText)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDeactivations) { p ->
                        DeactivationRequestCard(
                            prosumer = p,
                            onApprove = { viewModel.approveDeactivation(token, p.id) },
                            onReject = { viewModel.rejectDeactivation(token, p.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProsumerCardItem(prosumer: BackofficeProsumer) {
    val isActive = prosumer.status.equals("Active", ignoreCase = true)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(prosumer.name.ifBlank { "Prosumer" }, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CharcoalText)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isActive) Color(0xFFD1FAE5) else Color(0xFFFEE2E2))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        prosumer.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color(0xFF065F46) else Color(0xFF991B1B)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("NIC: ${prosumer.nic}", fontSize = 12.sp, color = CharcoalText, fontWeight = FontWeight.SemiBold)
            if (prosumer.address.isNotBlank()) {
                Text(prosumer.address, fontSize = 11.sp, color = GrayText)
            }
        }
    }
}

@Composable
fun DeactivationRequestCard(
    prosumer: BackofficeProsumer,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(prosumer.name.ifBlank { "Prosumer" }, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = CharcoalText)
                    Text("NIC: ${prosumer.nic}", fontSize = 12.sp, color = CharcoalText, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFEF2F2))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("DEACTIVATION REQUESTED", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = DangerRed)
                }
            }

            if (!prosumer.deactivationReason.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF9FAFB))
                        .padding(10.dp)
                ) {
                    Text(
                        "Reason: \"${prosumer.deactivationReason}\"",
                        fontSize = 12.sp,
                        color = GrayText
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GrayText)
                ) {
                    Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Approve & Deactivate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
