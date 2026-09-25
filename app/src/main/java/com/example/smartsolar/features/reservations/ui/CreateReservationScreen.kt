package com.example.smartsolar.features.reservations.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.app.DatePickerDialog
import java.util.Calendar
import com.example.smartsolar.features.microgrid.models.EnergySlot
import com.example.smartsolar.features.microgrid.models.Station
import com.example.smartsolar.features.microgrid.ui.MicrogridViewModel
import com.example.smartsolar.features.reservations.models.CreateReservationRequest
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReservationScreen(
    reservationViewModel: ReservationViewModel,
    microgridViewModel: MicrogridViewModel,
    token: String,
    nic: String,
    preselectedStationId: String? = null,
    onSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val stations by microgridViewModel.stations.collectAsState()
    val slots by microgridViewModel.slots.collectAsState()
    val uiState by reservationViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var selectedStation by remember { mutableStateOf<Station?>(null) }
    var selectedSlot by remember { mutableStateOf<EnergySlot?>(null) }
    var reservationDate by remember { mutableStateOf("") }
    var energyAmount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var stationExpanded by remember { mutableStateOf(false) }
    var slotExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { microgridViewModel.loadStations() }
    
    LaunchedEffect(stations) {
        if (preselectedStationId != null && selectedStation == null) {
            selectedStation = stations.find { it.id == preselectedStationId }
        }
    }

    LaunchedEffect(selectedStation) {
        selectedStation?.let { microgridViewModel.loadSlots(it.id) }
    }
    LaunchedEffect(uiState) {
        if (uiState is ReservationUiState.Success) {
            reservationViewModel.resetState()
            try {
                onSuccess()
            } catch (e: Throwable) {
                android.widget.Toast.makeText(context, "Nav Crash: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Reservation", fontWeight = FontWeight.Bold, color = CharcoalText) },
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
                    Text((uiState as ReservationUiState.Error).message,
                        color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp), fontSize = 14.sp)
                }
            }

            Text("Step 1: Choose Station", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            ExposedDropdownMenuBox(expanded = stationExpanded, onExpandedChange = { stationExpanded = it }) {
                OutlinedTextField(
                    value = selectedStation?.name ?: "Select a station",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Station") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(stationExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = stationExpanded, onDismissRequest = { stationExpanded = false }) {
                    stations.forEach { station ->
                        DropdownMenuItem(
                            text = { Text(station.name) },
                            onClick = {
                                selectedStation = station
                                selectedSlot = null
                                stationExpanded = false
                            },
                            leadingIcon = { Icon(Icons.Default.ElectricBolt, null) }
                        )
                    }
                }
            }

            val context = LocalContext.current
            val calendar = remember { Calendar.getInstance() }

            Text("Step 2: Choose Date", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            OutlinedTextField(
                value = reservationDate,
                onValueChange = { },
                label = { Text("Date (YYYY-MM-DD)") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                reservationDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = CharcoalText,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )


            Text("Step 3: Choose Slot", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            ExposedDropdownMenuBox(expanded = slotExpanded, onExpandedChange = { slotExpanded = it }) {
                OutlinedTextField(
                    value = selectedSlot?.let { "${it.startTime} – ${it.endTime}" } ?: "Select a slot",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Time Slot") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(slotExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = selectedStation != null
                )
                ExposedDropdownMenu(expanded = slotExpanded, onDismissRequest = { slotExpanded = false }) {
                    if (slots.isEmpty()) {
                        DropdownMenuItem(text = { Text("No slots available", color = GrayText) }, onClick = {})
                    }
                    slots.forEach { slot ->
                        DropdownMenuItem(
                            text = { Text("${slot.startTime} – ${slot.endTime} (${slot.capacityAvailable} kWh available)") },
                            onClick = { selectedSlot = slot; slotExpanded = false },
                            leadingIcon = { Icon(Icons.Default.AccessTime, null) }
                        )
                    }
                }
            }

            Text("Step 4: Energy Amount", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            OutlinedTextField(
                value = energyAmount,
                onValueChange = { energyAmount = it },
                label = { Text("Energy Amount (kWh)") },
                leadingIcon = { Icon(Icons.Default.BatteryChargingFull, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                leadingIcon = { Icon(Icons.Default.Notes, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 2, maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            val isValid = selectedStation != null && selectedSlot != null &&
                    reservationDate.length == 10 && energyAmount.toDoubleOrNull() != null

            Button(
                onClick = {
                    try {
                        reservationViewModel.createReservation(
                            token,
                            CreateReservationRequest(
                                prosumerNic = nic,
                                stationId = selectedStation!!.id,
                                slotId = selectedSlot!!.id,
                                reservationDate = "${reservationDate}T00:00:00Z",
                                energyAmountKwh = energyAmount.toDouble(),
                                notes = notes.takeIf { it.isNotBlank() }
                            )
                        ) {}
                    } catch (e: Throwable) {
                        android.widget.Toast.makeText(context, "Crash: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = CharcoalText),
                enabled = isValid && uiState !is ReservationUiState.Loading
            ) {
                if (uiState is ReservationUiState.Loading) {
                    CircularProgressIndicator(color = CharcoalText, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                } else {
                    Text("Confirm Reservation", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }
}
