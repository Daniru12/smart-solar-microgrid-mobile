package com.example.smartsolar.features.microgrid.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartsolar.features.microgrid.models.EnergySlot

@Composable
fun SlotListScreen(
    stationId: String,
    viewModel: MicrogridViewModel,
    onSlotSelected: (String, String, String) -> Unit // Passes (stationId, slotId, date) to Reservation Component
) {
    val state by viewModel.slotsState.collectAsState()
    
    // In a real app, this date would be selected from a DatePicker
    val selectedDate by remember { mutableStateOf("2026-09-25") }

    LaunchedEffect(stationId, selectedDate) {
        viewModel.loadSlots(stationId, selectedDate)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Available Slots", style = MaterialTheme.typography.headlineMedium)
        Text("Date: $selectedDate", color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        when (state) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading slots...")
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
                        Text("No available slots for this date.")
                    }
                } else {
                    LazyColumn {
                        items(slots) { slot ->
                            SlotItem(
                                slot = slot,
                                onSelect = { onSlotSelected(stationId, slot.id, selectedDate) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("${slot.startTime} - ${slot.endTime}", fontWeight = FontWeight.Bold)
                Text("Capacity Available: ${slot.capacityAvailable}")
                Text("Status: ${slot.status}", color = if (slot.status == "Available") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            }
            
            Button(
                onClick = onSelect,
                enabled = slot.status == "Available"
            ) {
                Text("Select")
            }
        }
    }
}
