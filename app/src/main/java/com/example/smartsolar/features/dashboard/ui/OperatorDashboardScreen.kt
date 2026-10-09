package com.example.smartsolar.features.dashboard.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.smartsolar.features.microgrid.models.EnergySlot
import com.example.smartsolar.features.microgrid.models.Station
import com.example.smartsolar.features.microgrid.ui.MicrogridViewModel
import com.example.smartsolar.features.reservations.models.Reservation
import com.example.smartsolar.features.microgrid.ui.UiState
import com.example.smartsolar.ui.theme.*

import com.example.smartsolar.features.microgrid.models.isStationAssignedToOperator

@Composable
fun OperatorDashboardScreen(
    viewModel: MicrogridViewModel,
    reservationViewModel: com.example.smartsolar.features.reservations.ui.ReservationViewModel? = null,
    token: String = "",
    operatorEmail: String = "",
    operatorName: String = "",
    operatorStationId: String = "",
    onNavigateToStations: () -> Unit,
    onNavigateToBookings: () -> Unit = {},
    onNavigateToScan: () -> Unit = {},
    onViewStation: (String) -> Unit = {},
    onViewSlots: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val stationsState by viewModel.stationsState.collectAsState()
    val slotsState by viewModel.slotsState.collectAsState()
    val allReservations by reservationViewModel?.allReservations?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

    // Filter to only stations assigned to this grid operator (matching web behavior)
    val myStations = remember(stationsState, operatorEmail, operatorName, operatorStationId) {
        if (stationsState is UiState.Success) {
            val list = (stationsState as UiState.Success<List<Station>>).data
            list.filter { isStationAssignedToOperator(it, operatorEmail, operatorName, operatorStationId) }
        } else emptyList()
    }
    val assignedStation = myStations.firstOrNull()

    // Filter reservations belonging to this operator's stations
    val myStationIds = remember(myStations) { myStations.map { it.id }.toSet() }
    val relevantReservations = remember(allReservations, myStationIds) {
        if (myStationIds.isEmpty()) allReservations
        else allReservations.filter { myStationIds.contains(it.stationId) }
    }

    val pendingCount = relevantReservations.count { it.status == "Pending" }
    val approvedCount = relevantReservations.count { it.status == "Approved" }
    val completedCount = relevantReservations.count { it.status == "Completed" }

    LaunchedEffect(Unit) {
        viewModel.loadStations()
        if (token.isNotBlank()) {
            reservationViewModel?.loadAll(token)
        }
    }

    LaunchedEffect(assignedStation?.id) {
        if (assignedStation != null) {
            viewModel.loadSlots(assignedStation.id)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            OperatorHeader()
        }
        item {
            OperatorSummaryCards(pendingCount, approvedCount, completedCount, slotsState)
        }
        item {
            OperatorQuickActions(
                onNavigateToStations = onNavigateToStations,
                onNavigateToBookings = onNavigateToBookings,
                onNavigateToScan = onNavigateToScan
            )
        }
        item {
            QRVerificationCard(onNavigateToScan = onNavigateToScan)
        }
        item {
            StationOverviewCard(
                stationsState = stationsState,
                assignedStation = assignedStation,
                slotsState = slotsState,
                onViewStation = { assignedStation?.let { onViewStation(it.id) } },
                onViewSlots = { assignedStation?.let { onViewSlots(it.id) } }
            )
        }
        item {
            TodaysSlotsList(
                slotsState = slotsState,
                onSeeAll = { assignedStation?.let { onViewSlots(it.id) } }
            )
        }
        item {
            val allStations = if (stationsState is UiState.Success) (stationsState as UiState.Success).data else emptyList()
            val allSlots = if (slotsState is UiState.Success) (slotsState as UiState.Success).data else emptyList()
            val context = LocalContext.current

            PendingReservationsList(
                pendingReservations = relevantReservations.filter { it.status.equals("Pending", ignoreCase = true) },
                stations = allStations,
                slots = allSlots,
                assignedStation = assignedStation,
                onSeeAll = onNavigateToBookings,
                onApprove = { res ->
                    if (token.isNotBlank()) {
                        reservationViewModel?.approveReservation(token, res.id) {
                            Toast.makeText(context, "Reservation #${res.id.takeLast(6).uppercase()} Approved!", Toast.LENGTH_SHORT).show()
                            reservationViewModel.loadAll(token)
                        }
                    } else {
                        Toast.makeText(context, "Authentication token missing", Toast.LENGTH_SHORT).show()
                    }
                },
                onReject = { res ->
                    if (token.isNotBlank()) {
                        reservationViewModel?.cancelReservation(token, res.id) {
                            Toast.makeText(context, "Reservation #${res.id.takeLast(6).uppercase()} Rejected", Toast.LENGTH_SHORT).show()
                            reservationViewModel.loadAll(token)
                        }
                    } else {
                        Toast.makeText(context, "Authentication token missing", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun OperatorHeader() {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "Welcome, Grid Operator",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = CharcoalText,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = "Manage station operations and verify energy-transfer reservations.",
            style = MaterialTheme.typography.bodyMedium,
            color = GrayText,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )
    }
}

@Composable
fun OperatorSummaryCards(pendingCount: Int, approvedCount: Int, completedCount: Int, slotsState: UiState<List<EnergySlot>>) {
    val availableSlots = if (slotsState is UiState.Success) slotsState.data.count { it.status == "Available" } else 0
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(title = "Pending Reservations", value = pendingCount.toString(), icon = Icons.Default.Pending, modifier = Modifier.weight(1f))
            SummaryCard(title = "Approved Today", value = approvedCount.toString(), icon = Icons.Default.ThumbUp, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(title = "Completed Today", value = completedCount.toString(), icon = Icons.Default.TaskAlt, modifier = Modifier.weight(1f))
            SummaryCard(title = "Available Slots", value = availableSlots.toString(), icon = Icons.Default.Schedule, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun OperatorQuickActions(
    onNavigateToStations: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToScan: () -> Unit
) {
    Column {
        Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionButton("Stations", Icons.Default.Storefront, modifier = Modifier.weight(1f)) { onNavigateToStations() }
            QuickActionButton("All Bookings", Icons.AutoMirrored.Filled.FormatListBulleted, modifier = Modifier.weight(1f)) { onNavigateToBookings() }
            QuickActionButton("Scan QR", Icons.Default.QrCodeScanner, modifier = Modifier.weight(1f)) { onNavigateToScan() }
        }
    }
}

@Composable
fun QRVerificationCard(onNavigateToScan: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).background(
                Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent),
                    radius = 400f
                )
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(CharcoalText),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Verify Energy Transfer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = CharcoalText, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Scan a Prosumer's QR code to verify the approved reservation and complete the energy transfer.",
                style = MaterialTheme.typography.bodySmall,
                color = CharcoalText.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onNavigateToScan,
                colors = ButtonDefaults.buttonColors(containerColor = CharcoalText, contentColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Scan QR Code", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun StationOverviewCard(
    stationsState: UiState<List<Station>>,
    assignedStation: Station?,
    slotsState: UiState<List<EnergySlot>>,
    onViewStation: () -> Unit,
    onViewSlots: () -> Unit
) {
    Column {
        Text("Your Assigned Station", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
        Spacer(modifier = Modifier.height(16.dp))

        when {
            stationsState is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            stationsState is UiState.Error -> {
                Text("Failed to load station data: ${(stationsState as UiState.Error).message}", color = MaterialTheme.colorScheme.error)
            }
            assignedStation == null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706))
                            Spacer(Modifier.width(8.dp))
                            Text("No Assigned Station", fontWeight = FontWeight.Bold, color = CharcoalText, fontSize = 16.sp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "You do not have a station assigned to your operator account yet. Please contact the administrator.",
                            fontSize = 13.sp,
                            color = GrayText
                        )
                    }
                }
            }
            else -> {
                val availableSlotsCount = if (slotsState is UiState.Success) {
                    (slotsState as UiState.Success<List<EnergySlot>>).data.count { it.status == "Available" }
                } else 0
                val totalSlotsCount = if (slotsState is UiState.Success) {
                    (slotsState as UiState.Success<List<EnergySlot>>).data.size
                } else 0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(assignedStation.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = CharcoalText)
                                if (assignedStation.address.isNotBlank()) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(assignedStation.address, fontSize = 12.sp, color = GrayText)
                                }
                            }
                            val isLive = assignedStation.status.equals("Active", ignoreCase = true)
                            Surface(
                                color = if (isLive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isLive) Color(0xFF86EFAC) else Color(0xFFFCA5A5))
                            ) {
                                Text(
                                    text = if (isLive) "ACTIVE" else assignedStation.status.uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    color = if (isLive) Color(0xFF15803D) else Color(0xFFB91C1C),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Capacity", fontSize = 12.sp, color = GrayText)
                                Text("${assignedStation.capacityKw} kW", fontWeight = FontWeight.ExtraBold, color = CharcoalText, fontSize = 16.sp)
                            }
                            Column {
                                Text("Battery ESS", fontSize = 12.sp, color = GrayText)
                                Text("${assignedStation.availableStorageKwh} kWh", fontWeight = FontWeight.ExtraBold, color = CharcoalText, fontSize = 16.sp)
                            }
                            Column {
                                Text("Avail. Slots", fontSize = 12.sp, color = GrayText)
                                Text("$availableSlotsCount / $totalSlotsCount", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = onViewStation,
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CharcoalText),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalText)
                            ) {
                                Text("View Station", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onViewSlots,
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = CharcoalText),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("View Slots", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodaysSlotsList(
    slotsState: UiState<List<EnergySlot>>,
    onSeeAll: () -> Unit = {}
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Station Energy Slots", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
            TextButton(onClick = onSeeAll) {
                Text("See All", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                when (slotsState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        }
                    }
                    is UiState.Error -> {
                        Text("Failed to load slots", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                    }
                    is UiState.Success -> {
                        val activeSlots = slotsState.data.filter { !it.status.equals("Deleted", ignoreCase = true) }.take(4)
                        if (activeSlots.isEmpty()) {
                            Text("No slots configured for this station yet.", color = GrayText, modifier = Modifier.padding(16.dp))
                        } else {
                            activeSlots.forEach { slot ->
                                SlotItem(slot)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SlotItem(slot: EnergySlot) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = GrayText, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${slot.startTime} - ${slot.endTime}", fontWeight = FontWeight.SemiBold, color = CharcoalText, fontSize = 14.sp)
                }
                val isAvail = slot.status.equals("Available", ignoreCase = true)
                Surface(
                    color = if (isAvail) LimeAccent.copy(alpha = 0.25f) else Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = slot.status,
                        color = if (isAvail) Color(0xFF15803D) else GrayText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Remaining: ${String.format(java.util.Locale.US, "%.1f", slot.remainingCapacity)} / ${String.format(java.util.Locale.US, "%.1f", slot.effectiveCapacity)} kW",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )
                Text(
                    text = "${String.format(java.util.Locale.US, "%.1f", slot.bookedCapacity)} kW booked",
                    fontSize = 11.sp,
                    color = GrayText
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (slot.percentRemaining / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = LimeAccentDark,
                trackColor = Color(0xFFE5E7EB)
            )
        }
    }
}

@Composable
fun PendingReservationsList(
    pendingReservations: List<Reservation>,
    stations: List<Station> = emptyList(),
    slots: List<EnergySlot> = emptyList(),
    assignedStation: Station? = null,
    onSeeAll: () -> Unit = {},
    onApprove: (Reservation) -> Unit = {},
    onReject: (Reservation) -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Pending Reservations",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = CharcoalText
                )
                if (pendingReservations.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Text(
                            text = "${pendingReservations.size} New",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            TextButton(onClick = onSeeAll) {
                Text("See All", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (pendingReservations.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "No pending dispatch requests",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                        Text(
                            text = "All prosumer energy transfer reservations are processed.",
                            fontSize = 12.sp,
                            color = GrayText
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                pendingReservations.take(4).forEach { reservation ->
                    PendingReservationCard(
                        reservation = reservation,
                        stations = stations,
                        slots = slots,
                        assignedStation = assignedStation,
                        onApprove = { onApprove(reservation) },
                        onReject = { onReject(reservation) }
                    )
                }
            }
        }
    }
}

@Composable
fun PendingReservationCard(
    reservation: Reservation,
    stations: List<Station>,
    slots: List<EnergySlot>,
    assignedStation: Station?,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }

    // 1. Resolve Station Name
    val stationName = when {
        reservation.stationName.isNotBlank() -> reservation.stationName
        else -> stations.find { it.id == reservation.stationId }?.name
            ?: assignedStation?.name
            ?: "Central Solar Microgrid Hub"
    }

    // 2. Resolve Time Window
    val matchedSlot = slots.find { it.id == reservation.slotId }
    val timeDisplay = when {
        reservation.startTime.isNotBlank() && reservation.endTime.isNotBlank() ->
            "${reservation.startTime} - ${reservation.endTime}"
        reservation.startTime.isNotBlank() ->
            reservation.startTime
        matchedSlot != null && matchedSlot.startTime.isNotBlank() ->
            "${matchedSlot.startTime} - ${matchedSlot.endTime}"
        else -> "Standard Dispatch Window"
    }

    // 3. Resolve Date
    val dateDisplay = try {
        val clean = reservation.reservationDate.substringBefore("T")
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val d = parser.parse(clean)
        if (d != null) {
            SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).format(d)
        } else {
            clean
        }
    } catch (e: Exception) {
        reservation.reservationDate.substringBefore("T")
    }

    // 4. Energy & Credits
    val energyAmount = reservation.energyAmountKwh
    val estCredits = energyAmount * 44.50

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Status Pill & Ref ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "AWAITING APPROVAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB45309)
                        )
                    }
                }

                Text(
                    text = "Ref: #${reservation.id.takeLast(6).uppercase()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrayText
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Station Name Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CharcoalText),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EvStation,
                        contentDescription = null,
                        tint = LimeAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = stationName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = CharcoalText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Microgrid Transfer Node",
                        fontSize = 11.sp,
                        color = GrayText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details Box: Date, Time & Energy Metric
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Date & Time Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = CharcoalText, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(dateDisplay, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = CharcoalText, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(timeDisplay, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CharcoalText)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Prosumer & Energy Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = GrayText, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Prosumer NIC", fontSize = 10.sp, color = GrayText, fontWeight = FontWeight.Medium)
                            }
                            Text(
                                text = reservation.prosumerNic,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", energyAmount)} kWh",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF15803D)
                                )
                            }
                            Text(
                                text = String.format(Locale.US, "%.1f kg CO₂", energyAmount * 0.8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrayText
                            )
                        }
                    }

                    if (!reservation.notes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Note: ${reservation.notes}",
                            fontSize = 11.sp,
                            color = GrayText,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row: Reject (outlined) vs Approve (Filled Green)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (!isProcessing) {
                            isProcessing = true
                            onReject()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFDC2626)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Reject", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        if (!isProcessing) {
                            isProcessing = true
                            onApprove()
                        }
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CharcoalText,
                        contentColor = LimeAccent
                    )
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = LimeAccent)
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Approve", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
