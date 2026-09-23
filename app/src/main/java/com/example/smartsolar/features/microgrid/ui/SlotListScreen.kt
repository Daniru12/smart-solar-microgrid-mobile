package com.example.smartsolar.features.microgrid.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.microgrid.models.EnergySlot
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import com.example.smartsolar.ui.theme.SurfaceLight
import com.example.smartsolar.ui.theme.DangerRed

@Composable
fun SlotListScreen(
    stationId: String,
    viewModel: MicrogridViewModel,
    onSlotSelected: (String, String, String) -> Unit
) {
    val state by viewModel.slotsState.collectAsState()
    
    val selectedDate by remember { mutableStateOf("2026-09-25") }

    LaunchedEffect(stationId, selectedDate) {
        viewModel.loadSlots(stationId, selectedDate)
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp)) {
        Text("Available Slots", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CharcoalText)
        Text("Date: $selectedDate", color = GrayText, fontWeight = FontWeight.Medium)
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
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
            is UiState.Success -> {
                val slots = (state as UiState.Success).data
                if (slots.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No available slots for this date.", color = GrayText)
                    }
                } else {
                    LazyColumn {
                        items(slots) { slot ->
                            SlotItem(
                                slot = slot,
                                onSelect = { onSlotSelected(stationId, slot.id, selectedDate) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SlotItem(slot: EnergySlot, onSelect: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("${slot.startTime} - ${slot.endTime}", fontWeight = FontWeight.Bold, color = CharcoalText, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Capacity Available: ${slot.capacityAvailable}", color = GrayText, fontSize = 12.sp)
                Text(
                    text = "Status: ${slot.status}", 
                    color = if (slot.status == "Available") LimeAccent else DangerRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            
            Button(
                onClick = onSelect,
                enabled = slot.status == "Available",
                colors = ButtonDefaults.buttonColors(containerColor = CharcoalText, contentColor = SurfaceLight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Select", fontWeight = FontWeight.Bold)
            }
        }
    }
}
