package com.example.smartsolar.features.microgrid.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StationDetailsScreen(
    stationId: String,
    viewModel: MicrogridViewModel,
    onViewSlotsClicked: (String) -> Unit
) {
    val state by viewModel.selectedStationState.collectAsState()

    LaunchedEffect(stationId) {
        viewModel.loadStationDetails(stationId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when (state) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading details...")
                }
            }
            is UiState.Error -> {
                val error = (state as UiState.Error).message
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
            is UiState.Success -> {
                val station = (state as UiState.Success).data
                Text(station.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                DetailRow("Address:", station.address)
                DetailRow("Capacity:", "${station.capacityKw} kW")
                DetailRow("Available Storage:", "${station.availableStorageKwh}")
                DetailRow("Status:", station.status)
                
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onViewSlotsClicked(station.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Available Slots")
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
