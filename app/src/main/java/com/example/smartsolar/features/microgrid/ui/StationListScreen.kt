package com.example.smartsolar.features.microgrid.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartsolar.features.microgrid.models.Station

@Composable
fun StationListScreen(
    viewModel: MicrogridViewModel,
    onStationSelected: (String) -> Unit
) {
    val state by viewModel.stationsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStations()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Microgrid Stations", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        when (state) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading stations...")
                }
            }
            is UiState.Error -> {
                val error = (state as UiState.Error).message
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(error, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadStations() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            is UiState.Success -> {
                val stations = (state as UiState.Success).data
                if (stations.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active microgrid stations available.")
                    }
                } else {
                    LazyColumn {
                        items(stations) { station ->
                            StationItem(station, onClick = { onStationSelected(station.id) })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StationItem(station: Station, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(station.name, fontWeight = FontWeight.Bold)
            Text("Capacity: ${station.capacityKw} kW")
            Text("Available Storage: ${station.availableStorageKwh}")
            Text("Status: ${station.status}")
        }
    }
}
