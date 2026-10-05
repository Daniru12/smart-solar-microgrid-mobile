package com.example.smartsolar.features.backoffice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.smartsolar.features.reservations.models.Reservation
import com.example.smartsolar.features.reservations.ui.ReservationViewModel
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import com.example.smartsolar.ui.theme.LimeAccentDark

private val DangerRed = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackofficeReservationsScreen(
    viewModel: ReservationViewModel,
    token: String,
    modifier: Modifier = Modifier
) {
    val reservations by viewModel.allReservations.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Pending", "Approved", "Completed", "Cancelled")

    LaunchedEffect(token) {
        if (token.isNotBlank()) {
            viewModel.loadAll(token)
        }
    }

    val filtered = reservations.filter { r ->
        if (selectedFilter == "All") true else r.status.equals(selectedFilter, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Filter row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filters) { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LimeAccent,
                        selectedLabelColor = CharcoalText
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "SYSTEM RESERVATIONS (${filtered.size})",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = GrayText
            )
            IconButton(onClick = { viewModel.loadAll(token) }) {
                Icon(Icons.Filled.Refresh, "Refresh", tint = CharcoalText, modifier = Modifier.size(20.dp))
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No reservations match this filter.", color = GrayText)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { r ->
                    ReservationCardItem(
                        reservation = r,
                        onApprove = {
                            viewModel.approveReservation(token, r.id) {
                                viewModel.loadAll(token)
                            }
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
fun ReservationCardItem(
    reservation: Reservation,
    onApprove: () -> Unit
) {
    val isPending = reservation.status.equals("Pending", ignoreCase = true)
    val statusColor = when (reservation.status.lowercase()) {
        "approved" -> Color(0xFF10B981)
        "pending" -> Color(0xFFF59E0B)
        "completed" -> Color(0xFF3B82F6)
        else -> Color(0xFF6B7280)
    }

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
                Text(
                    reservation.stationName.ifBlank { "Microgrid Station" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CharcoalText
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        reservation.status.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Prosumer NIC: ${reservation.prosumerNic}", fontSize = 12.sp, color = GrayText)
                Text(
                    "${reservation.energyAmountKwh} kWh",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Date: ${reservation.reservationDate} | ${reservation.startTime} - ${reservation.endTime}",
                fontSize = 11.sp,
                color = GrayText
            )

            if (isPending) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onApprove,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LimeAccent, contentColor = CharcoalText),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.Check, "Approve", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Approve Reservation", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
