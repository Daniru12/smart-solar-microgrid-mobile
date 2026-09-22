package com.example.smartsolar.features.microgrid.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.microgrid.models.Station
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import com.example.smartsolar.ui.theme.SurfaceLight

@Composable
fun StationListScreen(
    viewModel: MicrogridViewModel,
    onStationSelected: (String) -> Unit
) {
    val state by viewModel.stationsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStations()
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp)) {
        Text("Microgrid Stations", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CharcoalText)
        Spacer(modifier = Modifier.height(24.dp))

        when (state) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LimeAccent)
                }
            }
            is UiState.Error -> {
                val error = (state as UiState.Error).message
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(error, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadStations() },
                            colors = ButtonDefaults.buttonColors(containerColor = LimeAccent, contentColor = CharcoalText)
                        ) {
                            Text("Retry", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            is UiState.Success -> {
                val stations = (state as UiState.Success).data
                if (stations.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active microgrid stations available.", color = GrayText)
                    }
                } else {
                    LazyColumn {
                        items(stations) { station ->
                            StationItem(station, onClick = { onStationSelected(station.id) })
                            Spacer(modifier = Modifier.height(16.dp))
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
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(station.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CharcoalText)
                
                Surface(
                    color = LimeAccent,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = station.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Capacity", color = GrayText, fontSize = 12.sp)
                    Text("${station.capacityKw} kW", color = CharcoalText, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Available Storage", color = GrayText, fontSize = 12.sp)
                    Text("${station.availableStorageKwh} kWh", color = CharcoalText, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
