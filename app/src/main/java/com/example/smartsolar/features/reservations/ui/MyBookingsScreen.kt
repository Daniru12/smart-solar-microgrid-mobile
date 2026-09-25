package com.example.smartsolar.features.reservations.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.reservations.models.Reservation
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    viewModel: ReservationViewModel,
    token: String,
    nic: String,
    onReservationClick: (Reservation) -> Unit,
    onCreateReservation: () -> Unit
) {
    val reservations by viewModel.myReservations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val tabs = listOf("All", "Pending", "Approved", "Completed", "Cancelled")
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) { viewModel.loadMyReservations(token, nic) }

    val filtered = when (selectedTab) {
        1 -> reservations.filter { it.status == "Pending" }
        2 -> reservations.filter { it.status == "Approved" }
        3 -> reservations.filter { it.status == "Completed" }
        4 -> reservations.filter { it.status == "Cancelled" }
        else -> reservations
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings", fontWeight = FontWeight.Bold, color = CharcoalText) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                actions = {
                    IconButton(onClick = { viewModel.loadMyReservations(token, nic) }) {
                        Icon(Icons.Default.Refresh, null, tint = CharcoalText)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateReservation,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = CharcoalText
            ) {
                Icon(Icons.Default.Add, "Create Reservation")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventBusy, null, tint = GrayText, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No ${tabs[selectedTab].lowercase()} reservations", color = GrayText, fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { reservation ->
                        ReservationCard(reservation = reservation, onClick = { onReservationClick(reservation) })
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationCard(reservation: Reservation, onClick: () -> Unit) {
    val statusColor = when (reservation.status) {
        "Approved" -> androidx.compose.ui.graphics.Color(0xFF22C55E)
        "Pending" -> androidx.compose.ui.graphics.Color(0xFFF59E0B)
        "Cancelled" -> MaterialTheme.colorScheme.error
        "Completed" -> MaterialTheme.colorScheme.primary
        else -> GrayText
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(reservation.stationName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CharcoalText)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = GrayText, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(reservation.reservationDate.take(10), fontSize = 13.sp, color = GrayText)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.AccessTime, null, tint = GrayText, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${reservation.startTime} - ${reservation.endTime}", fontSize = 13.sp, color = GrayText)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("${reservation.energyAmountKwh} kWh", fontSize = 13.sp, color = GrayText)
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = statusColor.copy(alpha = 0.15f)
            ) {
                Text(
                    reservation.status, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 12.sp, color = statusColor, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
