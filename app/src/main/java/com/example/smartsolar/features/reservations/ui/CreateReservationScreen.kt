package com.example.smartsolar.features.reservations.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import com.example.smartsolar.features.reservations.models.CreateReservationRequest
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import com.example.smartsolar.ui.theme.LimeAccentDark
import java.text.SimpleDateFormat
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
    val slotsState by microgridViewModel.slotsState.collectAsState()
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
        selectedStation?.let {
            microgridViewModel.loadSlots(it.id)
            reservationDate = ""
            selectedSlot = null
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is ReservationUiState.Success) {
            reservationViewModel.resetState()
            Toast.makeText(context, "Reservation Created Successfully!", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
    }

    val todayDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date())
    }

    // Only real available/active slots with positive capacity scheduled for today or future dates
    val availableSlots = remember(slots, todayDateStr) {
        slots.filter { slot ->
            val slotDate = slot.date.substringBefore("T").take(10)
            val isTodayOrFuture = try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val slotD = sdf.parse(slotDate)
                val todayD = sdf.parse(todayDateStr)
                if (slotD != null && todayD != null) !slotD.before(todayD) else slotDate >= todayDateStr
            } catch (e: Exception) {
                slotDate >= todayDateStr
            }

            (slot.status.equals("Available", ignoreCase = true) || slot.status.equals("Active", ignoreCase = true)) &&
            slot.capacityAvailable > 0.0 &&
            isTodayOrFuture
        }
    }

    // Only dates that actually contain available slots and are today or in the future
    val availableDates = remember(availableSlots, todayDateStr) {
        availableSlots
            .map { it.date.substringBefore("T").take(10) }
            .filter { it.length == 10 && it >= todayDateStr }
            .distinct()
            .sorted()
    }

    // Automatically select first available date when dates load
    LaunchedEffect(availableDates) {
        if (availableDates.isNotEmpty()) {
            if (reservationDate !in availableDates) {
                reservationDate = availableDates.first()
            }
        } else {
            reservationDate = ""
            selectedSlot = null
        }
    }

    // Only slots for the chosen available date
    val slotsForDate = remember(availableSlots, reservationDate) {
        if (reservationDate.isNotBlank()) {
            availableSlots.filter { it.date.substringBefore("T").take(10) == reservationDate }
        } else {
            emptyList()
        }
    }

    // Ensure selected slot belongs to the selected date
    LaunchedEffect(slotsForDate) {
        if (slotsForDate.isNotEmpty()) {
            if (selectedSlot == null || slotsForDate.none { it.id == selectedSlot?.id }) {
                selectedSlot = slotsForDate.first()
            }
        } else {
            selectedSlot = null
        }
    }

    val energyNum = energyAmount.toDoubleOrNull() ?: 0.0
    val maxAvailableCapacity = selectedSlot?.capacityAvailable ?: 0.0
    val isExceedingCapacity = selectedSlot != null && energyNum > maxAvailableCapacity
    val estimatedCredits = energyNum * 44.50
    val estimatedCo2 = energyNum * 0.8
    val isValid = selectedStation != null && selectedSlot != null &&
            reservationDate.isNotBlank() && energyNum > 0.0 && !isExceedingCapacity

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
                        Text("Live Trading & Available Quota Window", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF15803D))
                        Text(
                            "Only verified dates and open slots with real capacity are selectable.",
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

            // Step 1: Select Microgrid Hub
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
                                        reservationDate = ""
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

            // Step 2: Choose Transfer Date (Only Available Dates)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepHeader(stepNumber = "2", title = "Available Transfer Date")
                        if (availableDates.isNotEmpty()) {
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "${availableDates.size} date${if (availableDates.size > 1) "s" else ""} open",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (selectedStation == null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = GrayText, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Select a microgrid station first to view open dates.", fontSize = 12.sp, color = GrayText)
                            }
                        }
                    } else if (slotsState is UiState.Loading) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = CharcoalText)
                            Spacer(Modifier.width(10.dp))
                            Text("Checking available dates and slots...", fontSize = 12.sp, color = GrayText)
                        }
                    } else if (availableDates.isEmpty()) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "No available open slots today or for future dates at this station. All slots are currently full or inactive.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    } else {
                        // Horizontal scrollable real available dates (Today & Future only)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            availableDates.forEach { date ->
                                val isSelected = reservationDate == date
                                val isToday = date == todayDateStr
                                val slotsCount = availableSlots.count { it.date.take(10) == date }
                                val dayOfWeek = formatDayOfWeek(date)
                                val dayNum = formatDayOfMonth(date)
                                val month = formatMonthShort(date)

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            reservationDate = date
                                        },
                                    color = if (isSelected) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) Color(0xFF15803D) else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        if (isToday) {
                                            Surface(
                                                color = Color(0xFF15803D),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.padding(bottom = 3.dp)
                                            ) {
                                                Text(
                                                    text = "TODAY",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = dayOfWeek,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color(0xFF15803D) else GrayText
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = dayNum,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isSelected) CharcoalText else CharcoalText.copy(alpha = 0.85f)
                                        )
                                        Text(
                                            text = month,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color(0xFF15803D) else GrayText
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Surface(
                                            color = if (isSelected) Color(0xFF15803D) else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "$slotsCount slot${if (slotsCount > 1) "s" else ""}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else CharcoalText,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (reservationDate.isNotBlank()) {
                            Text(
                                text = "Selected: ${formatDisplayDate(reservationDate)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CharcoalText
                            )
                        }
                    }
                }
            }

            // Step 3: Operating Time Slot (Real Available Slots)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StepHeader(stepNumber = "3", title = "Operating Time Slot")

                    if (reservationDate.isBlank()) {
                        OutlinedTextField(
                            value = "Select an available date first...",
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            leadingIcon = {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = GrayText)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledTextColor = GrayText
                            )
                        )
                    } else if (slotsForDate.isEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "No active slots available on selected date.",
                                modifier = Modifier.padding(14.dp),
                                fontSize = 12.sp,
                                color = GrayText
                            )
                        }
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = slotExpanded,
                            onExpandedChange = { slotExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedSlot?.let { "${it.startTime} – ${it.endTime}  (${it.capacityAvailable} kWh free)" }
                                    ?: "Select an operating slot...",
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
                                slotsForDate.forEach { slot ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("${slot.startTime} – ${slot.endTime}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Text("Operating Window", fontSize = 10.sp, color = GrayText)
                                                }
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

                        // Quick-select chips for slots
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            slotsForDate.forEach { slot ->
                                val isSlotSelected = selectedSlot?.id == slot.id
                                FilterChip(
                                    selected = isSlotSelected,
                                    onClick = { selectedSlot = slot },
                                    label = {
                                        Text(
                                            "${slot.startTime} - ${slot.endTime} (${slot.capacityAvailable} kWh)",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSlotSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFDCFCE7),
                                        selectedLabelColor = Color(0xFF15803D)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Step 4: Real Energy Quota & Value
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepHeader(stepNumber = "4", title = "Energy Quota to Transfer")
                        if (selectedSlot != null) {
                            Surface(
                                color = CharcoalText,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable {
                                    energyAmount = maxAvailableCapacity.toString()
                                }
                            ) {
                                Text(
                                    "USE MAX",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = LimeAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (selectedSlot != null) {
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Available Capacity in Slot:", fontSize = 11.sp, color = GrayText)
                                Text(
                                    "$maxAvailableCapacity kWh",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = energyAmount,
                        onValueChange = { energyAmount = it },
                        placeholder = {
                            Text(
                                if (selectedSlot != null) "Enter energy (up to $maxAvailableCapacity kWh)" else "Enter energy amount",
                                color = GrayText,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = CharcoalText)
                        },
                        trailingIcon = {
                            Text("kWh", fontWeight = FontWeight.Bold, color = CharcoalText, modifier = Modifier.padding(end = 12.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        isError = isExceedingCapacity,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = CharcoalText,
                            errorBorderColor = MaterialTheme.colorScheme.error
                        )
                    )

                    if (isExceedingCapacity) {
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Amount exceeds available slot capacity ($maxAvailableCapacity kWh).",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = energyNum > 0.0 && !isExceedingCapacity) {
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Transfer Quota", fontSize = 10.sp, color = GrayText, fontWeight = FontWeight.Medium)
                                    Text(
                                        "$energyNum kWh",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF15803D)
                                    )
                                    Text("Clean Energy", fontSize = 9.sp, color = GrayText)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Environmental Impact", fontSize = 10.sp, color = GrayText, fontWeight = FontWeight.Medium)
                                    Text(
                                        String.format(Locale.US, "%.1f kg CO₂", estimatedCo2),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                    if (maxAvailableCapacity > 0) {
                                        Text("${((energyNum / maxAvailableCapacity) * 100).toInt()}% of slot quota", fontSize = 9.sp, color = GrayText)
                                    }
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
                            Text("${formatDisplayDate(reservationDate)} • ${selectedSlot?.startTime} - ${selectedSlot?.endTime}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Energy Quota", fontSize = 12.sp, color = GrayText)
                            Text("$energyAmount kWh", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
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

private fun formatDayOfWeek(dateStr: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val d = sdf.parse(dateStr.take(10)) ?: return ""
        SimpleDateFormat("EEE", Locale.US).format(d).uppercase(Locale.US)
    } catch (e: Exception) {
        ""
    }
}

private fun formatDayOfMonth(dateStr: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val d = sdf.parse(dateStr.take(10)) ?: return ""
        SimpleDateFormat("dd", Locale.US).format(d)
    } catch (e: Exception) {
        ""
    }
}

private fun formatMonthShort(dateStr: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val d = sdf.parse(dateStr.take(10)) ?: return ""
        SimpleDateFormat("MMM", Locale.US).format(d).uppercase(Locale.US)
    } catch (e: Exception) {
        ""
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

