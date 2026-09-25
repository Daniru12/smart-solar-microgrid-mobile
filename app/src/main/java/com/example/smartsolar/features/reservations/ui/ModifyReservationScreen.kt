package com.example.smartsolar.features.reservations.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.microgrid.models.EnergySlot
import com.example.smartsolar.features.microgrid.ui.MicrogridViewModel
import com.example.smartsolar.features.reservations.models.Reservation
import com.example.smartsolar.features.reservations.models.UpdateReservationRequest
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyReservationScreen(
    reservation: Reservation,
    viewModel: ReservationViewModel,
    microgridViewModel: MicrogridViewModel,
    token: String,
    onSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val slots by microgridViewModel.slots.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var reservationDate by remember { mutableStateOf(reservation.reservationDate.take(10)) }
    var selectedSlot by remember { mutableStateOf<EnergySlot?>(null) }
    var energyAmount by remember { mutableStateOf(reservation.energyAmountKwh.toString()) }
    var notes by remember { mutableStateOf(reservation.notes ?: "") }
    var slotExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { microgridViewModel.loadSlots(reservation.stationId) }
    LaunchedEffect(uiState) {
        if (uiState is ReservationUiState.Success) { viewModel.resetState(); onSuccess() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modify Reservation", fontWeight = FontWeight.Bold, color = CharcoalText) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = CharcoalText) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState is ReservationUiState.Error) {
                Surface(color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text((uiState as ReservationUiState.Error).message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp), fontSize = 14.sp)
                }
            }

            Text("Station: ${reservation.stationName}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = reservationDate, onValueChange = { reservationDate = it },
                label = { Text("Date (YYYY-MM-DD)") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), singleLine = true
            )

            ExposedDropdownMenuBox(expanded = slotExpanded, onExpandedChange = { slotExpanded = it }) {
                OutlinedTextField(
                    value = selectedSlot?.let { "${it.startTime} – ${it.endTime}" }
                        ?: "Current: ${reservation.startTime} – ${reservation.endTime}",
                    onValueChange = {}, readOnly = true,
                    label = { Text("Time Slot") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(slotExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = slotExpanded, onDismissRequest = { slotExpanded = false }) {
                    if (slots.isEmpty()) {
                        DropdownMenuItem(text = { Text("Loading slots...", color = GrayText) }, onClick = {})
                    }
                    slots.forEach { slot ->
                        DropdownMenuItem(
                            text = { Text("${slot.startTime} – ${slot.endTime} (${slot.capacityAvailable} kWh avail)") },
                            onClick = { selectedSlot = slot; slotExpanded = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = energyAmount, onValueChange = { energyAmount = it },
                label = { Text("Energy Amount (kWh)") },
                leadingIcon = { Icon(Icons.Default.BatteryChargingFull, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), singleLine = true
            )

            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), minLines = 2, maxLines = 3
            )

            val slotId = selectedSlot?.id ?: reservation.slotId
            val isValid = reservationDate.length == 10 && energyAmount.toDoubleOrNull() != null

            Button(
                onClick = {
                    viewModel.updateReservation(token, reservation.id,
                        UpdateReservationRequest(
                            reservationDate = "${reservationDate}T00:00:00Z",
                            slotId = slotId,
                            energyAmountKwh = energyAmount.toDouble(),
                            notes = notes.takeIf { it.isNotBlank() }
                        )
                    ) {}
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = CharcoalText),
                enabled = isValid && uiState !is ReservationUiState.Loading
            ) {
                if (uiState is ReservationUiState.Loading) {
                    CircularProgressIndicator(color = CharcoalText, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                } else {
                    Text("Update Reservation", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }
}
