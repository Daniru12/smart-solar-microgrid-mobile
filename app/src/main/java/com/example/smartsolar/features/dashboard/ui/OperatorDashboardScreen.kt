package com.example.smartsolar.features.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.microgrid.models.EnergySlot
import com.example.smartsolar.features.microgrid.models.Station
import com.example.smartsolar.features.microgrid.ui.MicrogridViewModel
import com.example.smartsolar.features.reservations.models.Reservation
import com.example.smartsolar.features.microgrid.ui.UiState
import com.example.smartsolar.ui.theme.*

@Composable
fun OperatorDashboardScreen(
    viewModel: MicrogridViewModel,
    reservationViewModel: com.example.smartsolar.features.reservations.ui.ReservationViewModel? = null,
    token: String = "",
    onNavigateToStations: () -> Unit,
    onNavigateToBookings: () -> Unit = {},
    onNavigateToScan: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val stationsState by viewModel.stationsState.collectAsState()
    val slotsState by viewModel.slotsState.collectAsState()
    val allReservations by reservationViewModel?.allReservations?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

    val pendingCount = allReservations.count { it.status == "Pending" }
    val approvedCount = allReservations.count { it.status == "Approved" }
    val completedCount = allReservations.count { it.status == "Completed" }

    LaunchedEffect(Unit) {
        viewModel.loadStations()
        if (token.isNotBlank()) {
            reservationViewModel?.loadAll(token)
        }
    }

    // When stations load successfully, load slots for the first active station
    LaunchedEffect(stationsState) {
        if (stationsState is UiState.Success) {
            val activeStation = (stationsState as UiState.Success<List<Station>>).data.firstOrNull { it.status == "Active" }
            if (activeStation != null) {
                viewModel.loadSlots(activeStation.id)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            OperatorHeader()
        }
        item {
            OperatorSummaryCards(pendingCount, approvedCount, completedCount, slotsState)
        }
        item {
            OperatorQuickActions(
                onNavigateToStations = onNavigateToStations,
                onNavigateToBookings = onNavigateToBookings,
                onNavigateToScan = onNavigateToScan
            )
        }
        item {
            QRVerificationCard(onNavigateToScan = onNavigateToScan)
        }
        item {
            StationOverviewCard(stationsState)
        }
        item {
            TodaysSlotsList(slotsState)
        }
        item {
            PendingReservationsList(allReservations.filter { it.status == "Pending" })
        }
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun OperatorHeader() {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "Welcome, Grid Operator",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = CharcoalText,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = "Manage station operations and verify energy-transfer reservations.",
            style = MaterialTheme.typography.bodyMedium,
            color = GrayText,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )
    }
}

@Composable
fun OperatorSummaryCards(pendingCount: Int, approvedCount: Int, completedCount: Int, slotsState: UiState<List<EnergySlot>>) {
    val availableSlots = if (slotsState is UiState.Success) slotsState.data.count { it.status == "Available" } else 0
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(title = "Pending Reservations", value = pendingCount.toString(), icon = Icons.Default.Pending, modifier = Modifier.weight(1f))
            SummaryCard(title = "Approved Today", value = approvedCount.toString(), icon = Icons.Default.ThumbUp, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(title = "Completed Today", value = completedCount.toString(), icon = Icons.Default.TaskAlt, modifier = Modifier.weight(1f))
            SummaryCard(title = "Available Slots", value = availableSlots.toString(), icon = Icons.Default.Schedule, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun OperatorQuickActions(
    onNavigateToStations: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToScan: () -> Unit
) {
    Column {
        Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionButton("Stations", Icons.Default.Storefront, modifier = Modifier.weight(1f)) { onNavigateToStations() }
            QuickActionButton("All Bookings", Icons.AutoMirrored.Filled.FormatListBulleted, modifier = Modifier.weight(1f)) { onNavigateToBookings() }
            QuickActionButton("Scan QR", Icons.Default.QrCodeScanner, modifier = Modifier.weight(1f)) { onNavigateToScan() }
        }
    }
}

@Composable
fun QRVerificationCard(onNavigateToScan: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).background(
                Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent),
                    radius = 400f
                )
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(CharcoalText),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Verify Energy Transfer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = CharcoalText, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Scan a Prosumer's QR code to verify the approved reservation and complete the energy transfer.",
                style = MaterialTheme.typography.bodySmall,
                color = CharcoalText.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onNavigateToScan,
                colors = ButtonDefaults.buttonColors(containerColor = CharcoalText, contentColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Scan QR Code", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun StationOverviewCard(stationsState: UiState<List<Station>>) {
    Column {
        Text("Station Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
        Spacer(modifier = Modifier.height(16.dp))
        
        when (stationsState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is UiState.Error -> {
                Text("Failed to load stations", color = MaterialTheme.colorScheme.error)
            }
            is UiState.Success -> {
                val activeStation = stationsState.data.firstOrNull { it.status == "Active" }
                
                if (activeStation == null) {
                    Text("No active stations available right now.", color = GrayText)
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(activeStation.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CharcoalText)
                                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                    Text("Active", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Capacity", fontSize = 12.sp, color = GrayText)
                                    Text("${activeStation.capacityKw} kW", fontWeight = FontWeight.ExtraBold, color = CharcoalText, fontSize = 16.sp)
                                }
                                Column {
                                    Text("Avail. Storage", fontSize = 12.sp, color = GrayText)
                                    Text("4", fontWeight = FontWeight.ExtraBold, color = CharcoalText, fontSize = 16.sp)
                                }
                                Column {
                                    Text("Avail. Slots", fontSize = 12.sp, color = GrayText)
                                    Text("6", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = {},
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CharcoalText),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalText)
                                ) {
                                    Text("View Station", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {},
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = CharcoalText),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("View Slots", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodaysSlotsList(slotsState: UiState<List<EnergySlot>>) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Today's Energy Slots", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
            TextButton(onClick = {}) {
                Text("See All", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                when (slotsState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        }
                    }
                    is UiState.Error -> {
                        Text("Failed to load slots", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                    }
                    is UiState.Success -> {
                        val slots = slotsState.data.take(4) // Show upcoming 4 slots
                        if (slots.isEmpty()) {
                            Text("No slots available today.", color = GrayText, modifier = Modifier.padding(16.dp))
                        } else {
                            slots.forEach { slot ->
                                SlotItem("${slot.startTime} - ${slot.endTime}", slot.status == "Available")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SlotItem(time: String, isAvailable: Boolean) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = GrayText, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(time, fontWeight = FontWeight.SemiBold, color = CharcoalText, fontSize = 15.sp)
        }
        if (isAvailable) {
            Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)) {
                Text("Available", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
        } else {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
                Text("Full", color = GrayText, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
fun PendingReservationsList(pendingReservations: List<Reservation>) {
    Column {
        Text("Pending Reservations", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (pendingReservations.isEmpty()) {
            Text("No pending reservations.", color = GrayText)
        } else {
            pendingReservations.take(3).forEach { reservation ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Prosumer: ${reservation.prosumerNic}", fontWeight = FontWeight.Bold, color = CharcoalText, fontSize = 15.sp)
                            Text("Station: ${reservation.stationName}", fontSize = 12.sp, color = GrayText, modifier = Modifier.padding(vertical = 2.dp))
                            Text("Time: ${reservation.startTime} - ${reservation.endTime}", fontSize = 12.sp, color = GrayText)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Status: Pending", fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}
