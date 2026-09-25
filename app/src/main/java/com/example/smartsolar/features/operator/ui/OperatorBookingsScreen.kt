package com.example.smartsolar.features.operator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.smartsolar.features.reservations.ui.ReservationCard
import com.example.smartsolar.features.reservations.ui.ReservationViewModel
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorBookingsScreen(
    viewModel: ReservationViewModel,
    token: String,
    onReservationClick: (Reservation) -> Unit
) {
    val allReservations by viewModel.allReservations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val tabs = listOf("All", "Pending", "Approved", "Completed", "Cancelled")
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) { viewModel.loadAll(token) }

    val filtered = when (selectedTab) {
        1 -> allReservations.filter { it.status == "Pending" }
        2 -> allReservations.filter { it.status == "Approved" }
        3 -> allReservations.filter { it.status == "Completed" }
        4 -> allReservations.filter { it.status == "Cancelled" }
        else -> allReservations
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Bookings", fontWeight = FontWeight.Bold, color = CharcoalText) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                actions = {
                    IconButton(onClick = { viewModel.loadAll(token) }) {
                        Icon(Icons.Default.Refresh, null, tint = CharcoalText)
                    }
                }
            )
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
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventBusy, null, tint = GrayText, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("No ${tabs[selectedTab].lowercase()} reservations", color = GrayText, fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { res ->
                        Column {
                            Text(res.prosumerNic, fontSize = 11.sp, color = GrayText, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
                            ReservationCard(reservation = res, onClick = { onReservationClick(res) })
                        }
                    }
                }
            }
        }
    }
}
