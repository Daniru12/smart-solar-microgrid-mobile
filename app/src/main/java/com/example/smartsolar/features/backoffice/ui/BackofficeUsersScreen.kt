package com.example.smartsolar.features.backoffice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.smartsolar.features.backoffice.models.BackofficeUser
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent

private val BrandAccent = LimeAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackofficeUsersScreen(
    viewModel: BackofficeViewModel,
    token: String,
    modifier: Modifier = Modifier
) {
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("All") }

    val roles = listOf("All", "Backoffice", "GridOperator", "Prosumer")

    LaunchedEffect(Unit) {
        if (token.isNotBlank()) {
            viewModel.loadUsers(token)
        }
    }

    val filteredUsers = users.filter { user ->
        val matchesSearch = user.email.contains(searchQuery, ignoreCase = true)
        val matchesRole = if (selectedRoleFilter == "All") true else user.role.equals(selectedRoleFilter, ignoreCase = true)
        matchesSearch && matchesRole
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search users by email...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, "Search", tint = GrayText) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Filled.Close, "Clear", tint = GrayText)
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = CharcoalText
            )
        )

        Spacer(Modifier.height(12.dp))

        // Role filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(roles) { role ->
                val isSelected = selectedRoleFilter == role
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedRoleFilter = role },
                    label = { Text(role, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LimeAccent,
                        selectedLabelColor = CharcoalText
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Total counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "REGISTERED ACCOUNTS (${filteredUsers.size})",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = GrayText
            )
            IconButton(onClick = { viewModel.loadUsers(token) }) {
                Icon(Icons.Filled.Refresh, "Refresh", tint = CharcoalText, modifier = Modifier.size(20.dp))
            }
        }

        if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No users found matching your filters.", color = GrayText, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredUsers) { user ->
                    UserCardItem(
                        user = user,
                        onToggleStatus = {
                            viewModel.toggleUserStatus(token, user)
                        }
                    )
                }
                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun UserCardItem(
    user: BackofficeUser,
    onToggleStatus: () -> Unit
) {
    val isActive = user.status.equals("Active", ignoreCase = true)
    val roleColor = when (user.role.lowercase()) {
        "backoffice" -> CharcoalText
        "gridoperator" -> Color(0xFF2563EB)
        else -> Color(0xFF059669)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(roleColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            user.role.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = roleColor
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Color(0xFF10B981) else Color(0xFFEF4444))
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (isActive) "Active" else "Inactive",
                        fontSize = 11.sp,
                        color = if (isActive) Color(0xFF059669) else Color(0xFFDC2626),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    user.email,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )
                Text(
                    "ID: ${user.id.take(12)}...",
                    fontSize = 10.sp,
                    color = GrayText
                )
            }

            // Quick toggle button
            OutlinedButton(
                onClick = onToggleStatus,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isActive) Color(0xFFDC2626) else Color(0xFF059669)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    if (isActive) "Deactivate" else "Activate",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
