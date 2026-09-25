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
import com.example.smartsolar.features.microgrid.models.Station
import com.example.smartsolar.features.microgrid.ui.MicrogridViewModel
import com.example.smartsolar.features.reservations.models.Reservation
import com.example.smartsolar.features.microgrid.ui.UiState
import com.example.smartsolar.ui.theme.*

@Composable
fun ProsumerDashboardScreen(
    viewModel: MicrogridViewModel,
    reservationViewModel: com.example.smartsolar.features.reservations.ui.ReservationViewModel? = null,
    token: String = "",
    prosumerName: String = "",
    nic: String = "",
    onNavigateToStations: () -> Unit,
    onNavigateToBookings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onCreateReservation: () -> Unit = {},
    onViewDetails: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val stationsState by viewModel.stationsState.collectAsState()
    val dashboardSummary by reservationViewModel?.dashboardSummary?.collectAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        viewModel.loadStations()
        if (token.isNotBlank() && nic.isNotBlank()) {
            reservationViewModel?.loadMyReservations(token, nic)
        }
    }

    val myReservations by reservationViewModel?.myReservations?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val pendingCount = myReservations.count { it.status == "Pending" }
    val approvedCount = myReservations.count { it.status == "Approved" }
    val completedCount = myReservations.count { it.status == "Completed" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            ProsumerHeader(prosumerName, nic)
        }
        item {
            ProsumerSummaryCards(stationsState, activeCount = approvedCount, pendingCount = pendingCount, completedCount = completedCount)
        }
        item {
            ProsumerQuickActions(
                onNavigateToStations = onNavigateToStations,
                onNavigateToBookings = onNavigateToBookings,
                onNavigateToHistory = onNavigateToHistory,
                onCreateReservation = onCreateReservation
            )
        }
        item {
            UpcomingReservationCard(myReservations.firstOrNull { it.status == "Approved" })
        }
        item {
            AvailableStationsPreview(stationsState, onNavigateToStations, onViewDetails)
        }
        item {
            RecentActivityList(myReservations.take(3))
        }
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ProsumerHeader(name: String, nic: String) {
    val displayName = name.ifBlank { "Prosumer" }
    val displayNic = nic.ifBlank { "NIC Not Provided" }
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "Welcome Back, $displayName",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = CharcoalText,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = "Manage your solar energy reservations and microgrid activities.",
            style = MaterialTheme.typography.bodyMedium,
            color = GrayText,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("NIC: $displayNic", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CharcoalText)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primary))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Account: Active", style = MaterialTheme.typography.labelMedium, color = GrayText, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProsumerSummaryCards(stationsState: UiState<List<Station>>, activeCount: Int, pendingCount: Int, completedCount: Int) {
    val activeStationsCount = if (stationsState is UiState.Success) {
        stationsState.data.filter { it.status == "Active" }.size
    } else {
        0
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(title = "Active Bookings", value = activeCount.toString(), icon = Icons.Default.EventAvailable, modifier = Modifier.weight(1f))
            SummaryCard(title = "Pending Bookings", value = pendingCount.toString(), icon = Icons.Default.PendingActions, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(title = "Completed Transfers", value = completedCount.toString(), icon = Icons.Default.CheckCircle, modifier = Modifier.weight(1f))
            SummaryCard(title = "Available Stations", value = activeStationsCount.toString(), icon = Icons.Default.EvStation, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
            }
            Text(title, fontSize = 12.sp, color = GrayText, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ProsumerQuickActions(
    onNavigateToStations: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onCreateReservation: () -> Unit
) {
    Column {
        Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionButton("Find Stations", Icons.Default.Search, modifier = Modifier.weight(1f)) { onNavigateToStations() }
            QuickActionButton("New Booking", Icons.Default.Schedule, modifier = Modifier.weight(1f)) { onCreateReservation() }
            QuickActionButton("My Bookings", Icons.Default.BookOnline, modifier = Modifier.weight(1f)) { onNavigateToBookings() }
            QuickActionButton("History", Icons.Default.History, modifier = Modifier.weight(1f)) { onNavigateToHistory() }
        }
    }
}

@Composable
fun QuickActionButton(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = CharcoalText, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalText, maxLines = 1)
    }
}

@Composable
fun UpcomingReservationCard(reservation: Reservation?) {
    Column {
        Text("Upcoming Reservation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (reservation == null) {
            Text("No approved upcoming reservations.", color = GrayText, modifier = Modifier.padding(bottom = 16.dp))
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(
                            Brush.linearGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                            )
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = "Energy", tint = CharcoalText, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(reservation.stationName, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = CharcoalText)
                        Text("${reservation.reservationDate.take(10)} • ${reservation.startTime} - ${reservation.endTime}", fontSize = 13.sp, color = GrayText, modifier = Modifier.padding(vertical = 4.dp))
                        Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                            Text(reservation.status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, color = CharcoalText, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable {  },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = CharcoalText)
                    }
                }
            }
        }
    }
}

@Composable
fun AvailableStationsPreview(stationsState: UiState<List<Station>>, onNavigateToStations: () -> Unit, onViewDetails: (String) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Available Stations", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
            TextButton(onClick = onNavigateToStations) {
                Text("See All", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
        
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
                val activeStations = stationsState.data.filter { it.status == "Active" }.take(3)
                
                if (activeStations.isEmpty()) {
                    Text("No active stations available right now.", color = GrayText, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(activeStations) { station ->
                            StationPreviewItem(
                                name = station.name,
                                capacity = "${station.capacityKw} kW",
                                slots = 4, // Mocking slots since they require a separate API call per station
                                onViewDetails = { onViewDetails(station.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StationPreviewItem(name: String, capacity: String, slots: Int, onViewDetails: () -> Unit) {
    Card(
        modifier = Modifier.width(260.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EvStation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(name, fontWeight = FontWeight.Bold, color = CharcoalText, fontSize = 16.sp, maxLines = 1)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Capacity", fontSize = 11.sp, color = GrayText)
                    Text(capacity, fontWeight = FontWeight.Bold, color = CharcoalText, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Available Slots", fontSize = 11.sp, color = GrayText)
                    Text("$slots", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onViewDetails, 
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CharcoalText, contentColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("View Details", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun RecentActivityList(activities: List<Reservation>) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text("Recent Activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (activities.isEmpty()) {
            Text("No recent activities to show.", color = GrayText)
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    activities.forEachIndexed { index, reservation ->
                        val icon = when (reservation.status) {
                            "Approved" -> Icons.Default.CheckCircleOutline
                            "Completed" -> Icons.Default.Bolt
                            "Cancelled" -> Icons.Default.Cancel
                            else -> Icons.Default.PendingActions
                        }
                        val title = "Reservation ${reservation.status}"
                        ActivityItem(
                            title = title,
                            location = reservation.stationName,
                            date = "${reservation.reservationDate.take(10)} • ${reservation.startTime}",
                            icon = icon,
                            isLast = index == activities.lastIndex
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityItem(title: String, location: String, date: String, icon: ImageVector, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
            Box(
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = CharcoalText, modifier = Modifier.size(16.dp))
            }
            if (!isLast) {
                Box(modifier = Modifier.width(2.dp).height(32.dp).background(MaterialTheme.colorScheme.surfaceVariant))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CharcoalText)
            Text(location, fontSize = 12.sp, color = GrayText)
        }
        Text(date, fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.SemiBold)
    }
}
