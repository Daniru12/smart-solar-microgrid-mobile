package com.example.smartsolar.features.microgrid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import com.example.smartsolar.ui.theme.SurfaceLight

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

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        when (state) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LimeAccent)
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
                Text(station.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CharcoalText)
                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceLight)
                        .padding(20.dp)
                ) {
                    DetailRow("Address:", station.address)
                    DetailRow("Capacity:", "${station.capacityKw} kW")
                    DetailRow("Available Storage:", "${station.availableStorageKwh}")
                    DetailRow("Status:", station.status)
                }
                
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onViewSlotsClicked(station.id) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LimeAccent, contentColor = CharcoalText),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View Available Slots", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, color = CharcoalText, fontSize = 14.sp)
        Text(value, color = GrayText, fontSize = 16.sp)
    }
}
