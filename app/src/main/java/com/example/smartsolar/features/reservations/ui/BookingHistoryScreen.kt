package com.example.smartsolar.features.reservations.ui

import androidx.compose.foundation.clickable
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
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingHistoryScreen(
    viewModel: ReservationViewModel,
    token: String,
    nic: String,
    onReservationClick: (Reservation) -> Unit
) {
    val reservations by viewModel.myReservations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val history = reservations.filter { it.status == "Completed" || it.status == "Cancelled" }
        .sortedByDescending { it.reservationDate }

    LaunchedEffect(Unit) { viewModel.loadMyReservations(token, nic) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking History", fontWeight = FontWeight.Bold, color = CharcoalText) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                actions = {
                    IconButton(onClick = { viewModel.loadMyReservations(token, nic) }) {
                        Icon(Icons.Default.Refresh, null, tint = CharcoalText)
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, null, tint = GrayText, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No booking history yet", color = GrayText, fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history, key = { it.id }) { reservation ->
                    ReservationCard(reservation = reservation, onClick = { onReservationClick(reservation) })
                }
            }
        }
    }
}
