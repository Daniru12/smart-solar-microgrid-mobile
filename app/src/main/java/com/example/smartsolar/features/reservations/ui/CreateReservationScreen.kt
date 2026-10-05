package com.example.smartsolar.features.reservations.ui

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.microgrid.models.EnergySlot
import com.example.smartsolar.features.microgrid.models.Station
import com.example.smartsolar.features.microgrid.ui.MicrogridViewModel
import com.example.smartsolar.features.reservations.models.CreateReservationRequest
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import com.example.smartsolar.ui.theme.LimeAccentDark
import java.util.Calendar
import java.util.Locale

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

    LaunchedEffect(Unit) {
        microgridViewModel.loadStations()
    }

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
            Toast.makeText(context, "Reservation Created Successfully!", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
    }

    val energyNum = energyAmount.toDoubleOrNull() ?: 0.0
    val estimatedCredits = energyNum * 44.50
    val estimatedCo2 = energyNum * 0.8
    val isValid = selectedStation != null && selectedSlot != null &&
            reservationDate.length == 10 && energyNum > 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Create Reservation", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = CharcoalText)
                        Text("Dispatch Clean Energy to Microgrid", fontSize = 11.sp, color = GrayText)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CharcoalText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Surface(
                color = Color(0xFFDCFCE7),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF15803D).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("7-Day Forward Trading Window", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF15803D))
                        Text(
                            "Slots are open for next 7 days. Standard Feed-In rate is fixed at LKR 44.50/kWh.",
                            fontSize = 11.sp,
                            color = CharcoalText.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            if (uiState is ReservationUiState.Error) {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (uiState as ReservationUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(14.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StepHeader(stepNumber = "1", title = "Select Microgrid Hub")

                    ExposedDropdownMenuBox(
                        expanded = stationExpanded,
                        onExpandedChange = { stationExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedStation?.name ?: "Choose a microgrid station...",
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = {
                                Icon(Icons.Default.EvStation, contentDescription = null, tint = CharcoalText)
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = stationExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = CharcoalText
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = stationExpanded,
                            onDismissRequest = { stationExpanded = false }
                        ) {
                            stations.forEach { station ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(station.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("${station.capacityKw} kW Capacity • ${station.address}", fontSize = 11.sp, color = GrayText)
                                        }
                                    },
                                    onClick = {
                                        selectedStation = station
                                        selectedSlot = null
                                        stationExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Bolt, contentDescription = null, tint = CharcoalText)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            val calendar = remember { Calendar.getInstance() }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StepHeader(stepNumber = "2", title = "Choose Transfer Date")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
                            .clickable {
                                val dp = DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        reservationDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                )
                                dp.datePicker.minDate = calendar.timeInMillis
                                calendar.add(Calendar.DAY_OF_YEAR, 7)
                                dp.datePicker.maxDate = calendar.timeInMillis
                                calendar.add(Calendar.DAY_OF_YEAR, -7)
                                dp.show()
                            }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = CharcoalText, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (reservationDate.isNotBlank()) formatDisplayDate(reservationDate) else "Select date (within 7 days)",
                                    fontSize = 14.sp,
                                    fontWeight = if (reservationDate.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                                    color = if (reservationDate.isNotBlank()) CharcoalText else GrayText
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("SELECT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CharcoalText, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StepHeader(stepNumber = "3", title = "Operating Time Slot")

                    ExposedDropdownMenuBox(
                        expanded = slotExpanded,
                        onExpandedChange = { if (selectedStation != null) slotExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedSlot?.let { "${it.startTime} – ${it.endTime}" } ?: "Select an operating slot...",
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = CharcoalText)
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = slotExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = selectedStation != null),
                            shape = RoundedCornerShape(14.dp),
                            enabled = selectedStation != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = CharcoalText,
                                disabledBorderColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                disabledTextColor = GrayText
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = slotExpanded,
                            onDismissRequest = { slotExpanded = false }
                        ) {
                            if (slots.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No active slots available for this station", color = GrayText) },
                                    onClick = {}
                                )
                            } else {
                                slots.forEach { slot ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("${slot.startTime} – ${slot.endTime}", fontWeight = FontWeight.Bold)
                                                Surface(
                                                    color = Color(0xFFDCFCE7),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        "${slot.capacityAvailable} kWh free",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF15803D),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedSlot = slot
                                            slotExpanded = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Schedule, contentDescription = null, tint = CharcoalText)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StepHeader(stepNumber = "4", title = "Energy Quota to Transfer")

                    OutlinedTextField(
                        value = energyAmount,
                        onValueChange = { energyAmount = it },
                        placeholder = { Text("Enter energy (e.g. 25)", color = GrayText, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = CharcoalText)
                        },
                        trailingIcon = {
                            Text("kWh", fontWeight = FontWeight.Bold, color = CharcoalText, modifier = Modifier.padding(end = 12.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = CharcoalText
                        )
                    )

                    AnimatedVisibility(visible = energyNum > 0.0) {
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Estimated Feed-In Revenue", fontSize = 10.sp, color = GrayText, fontWeight = FontWeight.Medium)
                                    Text(
                                        String.format(Locale.US, "LKR %,.2f", estimatedCredits),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Environmental Impact", fontSize = 10.sp, color = GrayText, fontWeight = FontWeight.Medium)
                                    Text(
                                        String.format(Locale.US, "%.1f kg CO₂ Offset", estimatedCo2),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Additional notes for operator (optional)", color = GrayText, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = GrayText) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        minLines = 2,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = CharcoalText
                        )
                    )
                }
            }

            AnimatedVisibility(visible = isValid) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, LimeAccentDark)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dispatch Summary Ready", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CharcoalText)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Station", fontSize = 12.sp, color = GrayText)
                            Text(selectedStation?.name ?: "", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Schedule", fontSize = 12.sp, color = GrayText)
                            Text("$reservationDate • ${selectedSlot?.startTime} - ${selectedSlot?.endTime}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Energy & Value", fontSize = 12.sp, color = GrayText)
                            Text("$energyAmount kWh • LKR ${String.format(Locale.US, "%,.2f", estimatedCredits)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                        }
                    }
                }
            }

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
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CharcoalText,
                    contentColor = LimeAccent,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = GrayText
                ),
                enabled = isValid && uiState !is ReservationUiState.Loading,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (uiState is ReservationUiState.Loading) {
                    CircularProgressIndicator(color = LimeAccent, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                } else {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Energy Dispatch Reservation", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StepHeader(stepNumber: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = CharcoalText,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(stepNumber, color = LimeAccent, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CharcoalText)
    }
}
