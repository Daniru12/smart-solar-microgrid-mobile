package com.example.smartsolar.features.reservations.ui

import android.app.DatePickerDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.microgrid.models.EnergySlot
import com.example.smartsolar.features.microgrid.models.Station
import com.example.smartsolar.features.microgrid.ui.MicrogridViewModel
import com.example.smartsolar.features.microgrid.ui.UiState
import com.example.smartsolar.features.reservations.models.Reservation
import com.example.smartsolar.features.reservations.models.UpdateReservationRequest
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import java.util.Calendar
import java.util.Locale

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
    val stationsState by microgridViewModel.stationsState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val stationsList = remember(stationsState) {
        if (stationsState is UiState.Success) {
            (stationsState as UiState.Success<List<Station>>).data
        } else {
            emptyList()
        }
    }

    var reservationDate by remember { mutableStateOf(reservation.reservationDate.take(10)) }
    var selectedSlot by remember { mutableStateOf<EnergySlot?>(null) }
    var energyAmount by remember { mutableStateOf(reservation.energyAmountKwh.toString()) }
    var notes by remember { mutableStateOf(reservation.notes ?: "") }
    var slotExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        microgridViewModel.loadStations()
        microgridViewModel.loadSlots(reservation.stationId)
    }

    LaunchedEffect(uiState) {
        if (uiState is ReservationUiState.Success) {
            viewModel.resetState()
            onSuccess()
        }
    }

    val displayStation = resolveStationName(reservation, stationsList)
    val energyNum = energyAmount.toDoubleOrNull() ?: 0.0
    val estimatedCredits = energyNum * 44.50
    val slotId = selectedSlot?.id ?: reservation.slotId
    val isValid = reservationDate.length == 10 && energyNum > 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Modify Reservation", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = CharcoalText)
                        Text("Update Slot or Dispatch Quota", fontSize = 11.sp, color = GrayText)
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
            // Notice: 12h policy
            Surface(
                color = Color(0xFFFEF3C7),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Modification Policy: Energy transfer details can be updated up to 12 hours prior to scheduled slot.",
                        fontSize = 12.sp,
                        color = CharcoalText.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )
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

            // Station Identity Card (Station cannot be changed after booking, only slot/date/kWh)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Assigned Microgrid Station", fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CharcoalText),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.EvStation, contentDescription = null, tint = LimeAccent, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(displayStation, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = CharcoalText)
                            Text("Node Ref: #${reservation.stationId.takeLast(6).uppercase()}", fontSize = 11.sp, color = GrayText)
                        }
                    }
                }
            }

            // Step 1: Change Date
            val calendar = remember { Calendar.getInstance() }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StepHeader(stepNumber = "1", title = "Update Transfer Date")

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
                                    text = formatDisplayDate(reservationDate),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalText
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("CHANGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CharcoalText, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            }

            // Step 2: Change Time Slot
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StepHeader(stepNumber = "2", title = "Update Operating Slot")

                    ExposedDropdownMenuBox(
                        expanded = slotExpanded,
                        onExpandedChange = { slotExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedSlot?.let { "${it.startTime} – ${it.endTime}" }
                                ?: if (reservation.startTime.isNotBlank()) "${reservation.startTime} – ${reservation.endTime} (Current)" else "Select time slot...",
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
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = CharcoalText
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = slotExpanded,
                            onDismissRequest = { slotExpanded = false }
                        ) {
                            if (slots.isEmpty()) {
                                DropdownMenuItem(text = { Text("Loading available slots...", color = GrayText) }, onClick = {})
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

            // Step 3: Energy Amount
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StepHeader(stepNumber = "3", title = "Update Energy Quota")

                    OutlinedTextField(
                        value = energyAmount,
                        onValueChange = { energyAmount = it },
                        placeholder = { Text("Energy Amount", color = GrayText) },
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
                                Text("New Estimated Revenue", fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium)
                                Text(
                                    String.format(Locale.US, "LKR %,.2f", estimatedCredits),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Notes for operator (optional)", color = GrayText, fontSize = 13.sp) },
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

            // Submit Button
            Button(
                onClick = {
                    viewModel.updateReservation(
                        token,
                        reservation.id,
                        UpdateReservationRequest(
                            reservationDate = "${reservationDate}T00:00:00Z",
                            slotId = slotId,
                            energyAmountKwh = energyAmount.toDouble(),
                            notes = notes.takeIf { it.isNotBlank() }
                        )
                    ) {}
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
                    Text("Save Modified Reservation", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
