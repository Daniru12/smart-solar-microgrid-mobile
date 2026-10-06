package com.example.smartsolar.features.reservations.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.microgrid.models.Station
import com.example.smartsolar.features.microgrid.ui.MicrogridViewModel
import com.example.smartsolar.features.microgrid.ui.UiState
import com.example.smartsolar.features.reservations.models.Reservation
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingHistoryScreen(
    viewModel: ReservationViewModel,
    microgridViewModel: MicrogridViewModel? = null,
    token: String,
    nic: String,
    onReservationClick: (Reservation) -> Unit
) {
    val reservations by viewModel.myReservations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val stationsState by microgridViewModel?.stationsState?.collectAsState() ?: remember { mutableStateOf(null) }

    val stationsList = remember(stationsState) {
        if (stationsState is UiState.Success) {
            (stationsState as UiState.Success<List<Station>>).data
        } else {
            emptyList()
        }
    }

    val history = reservations.filter { it.status == "Completed" || it.status == "Cancelled" }
        .sortedByDescending { it.reservationDate }

    LaunchedEffect(Unit) {
        viewModel.loadMyReservations(token, nic)
        microgridViewModel?.loadStations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Booking History", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = CharcoalText)
                        Text("Past Dispatches & Energy Transferred", fontSize = 11.sp, color = GrayText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                actions = {
                    IconButton(onClick = { viewModel.loadMyReservations(token, nic) }) {
                        Icon(Icons.Default.Refresh, null, tint = CharcoalText)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.History, null, tint = GrayText, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No Transfer History", fontWeight = FontWeight.Bold, color = CharcoalText, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Completed and finalized energy transfers will be logged here.", color = GrayText, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(history, key = { it.id }) { reservation ->
                    val stationName = resolveStationName(reservation, stationsList)
                    ReservationCard(
                        reservation = reservation,
                        resolvedStationName = stationName,
                        onClick = { onReservationClick(reservation) }
                    )
                }
            }
        }
    }
}
