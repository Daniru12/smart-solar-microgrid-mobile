package com.example.smartsolar.features.reservations.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.microgrid.models.Station
import com.example.smartsolar.features.microgrid.ui.MicrogridViewModel
import com.example.smartsolar.features.microgrid.ui.UiState
import com.example.smartsolar.features.reservations.models.Reservation
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import com.example.smartsolar.ui.theme.LimeAccentDark
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    viewModel: ReservationViewModel,
    microgridViewModel: MicrogridViewModel? = null,
    token: String,
    nic: String,
    onReservationClick: (Reservation) -> Unit,
    onCreateReservation: () -> Unit
) {
    val reservations by viewModel.myReservations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val stationsState by microgridViewModel?.stationsState?.collectAsState() ?: remember { mutableStateOf(null) }

    val stationsList = remember(stationsState) {
        if (stationsState is UiState.Success) {
            (stationsState as UiState.Success<List<Station>>).data
        } else {
            emptyList()
        }
    }

    val tabs = listOf("All", "Pending", "Approved", "Completed", "Cancelled")
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadMyReservations(token, nic)
        microgridViewModel?.loadStations()
    }

    val pendingCount = reservations.count { it.status == "Pending" }
    val approvedCount = reservations.count { it.status == "Approved" }
    val completedCount = reservations.count { it.status == "Completed" }
    val cancelledCount = reservations.count { it.status == "Cancelled" }

    val counts = listOf(reservations.size, pendingCount, approvedCount, completedCount, cancelledCount)

    val filtered = when (selectedTab) {
        1 -> reservations.filter { it.status == "Pending" }
        2 -> reservations.filter { it.status == "Approved" }
        3 -> reservations.filter { it.status == "Completed" }
        4 -> reservations.filter { it.status == "Cancelled" }
        else -> reservations
    }

    val totalEnergy = reservations.filter { it.status == "Completed" }.sumOf { it.energyAmountKwh }
    val totalCredits = totalEnergy * 44.50

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Bookings", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = CharcoalText)
                        Text("Energy Transfer Sessions & History", fontSize = 11.sp, color = GrayText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                actions = {
                    IconButton(onClick = { viewModel.loadMyReservations(token, nic) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = CharcoalText)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateReservation,
                containerColor = CharcoalText,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(18.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Booking", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Booking", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            BookingsSummaryBanner(
                totalCompleted = completedCount,
                totalEnergyKwh = totalEnergy,
                totalCreditsLkr = totalCredits
            )

            BookingFilterTabs(
                tabs = tabs,
                counts = counts,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.EventBusy,
                                    contentDescription = null,
                                    tint = GrayText,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No ${tabs[selectedTab]} Bookings",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = CharcoalText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "You don't have any ${tabs[selectedTab].lowercase()} energy reservations in this filter.",
                                fontSize = 13.sp,
                                color = GrayText,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onCreateReservation,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CharcoalText,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Schedule Energy Slot", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filtered, key = { it.id }) { reservation ->
                        val stationName = resolveStationName(reservation, stationsList)
                        ReservationCard(
                            reservation = reservation,
                            resolvedStationName = stationName,
                            onClick = { onReservationClick(reservation) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookingsSummaryBanner(
    totalCompleted: Int,
    totalEnergyKwh: Double,
    totalCreditsLkr: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total Dispatched", fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium)
                Text(
                    String.format(Locale.US, "%.1f kWh", totalEnergyKwh),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CharcoalText
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Column {
                Text("Credits Earned", fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium)
                Text(
                    String.format(Locale.US, "LKR %,.0f", totalCreditsLkr),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF15803D)
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text("Completed", fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "$totalCompleted Sessions",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                }
            }
        }
    }
}

@Composable
fun BookingFilterTabs(
    tabs: List<String>,
    counts: List<Int>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            val count = counts.getOrElse(index) { 0 }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onTabSelected(index) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) CharcoalText else MaterialTheme.colorScheme.surface,
                border = if (!isSelected) {
                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                } else null,
                shadowElevation = if (isSelected) 3.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isSelected) Color.White else CharcoalText
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = if (isSelected) LimeAccent else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = count.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) CharcoalText else GrayText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationCard(
    reservation: Reservation,
    modifier: Modifier = Modifier,
    resolvedStationName: String? = null,
    onClick: () -> Unit
) {
    val status = reservation.status
    val (statusBg, statusFg, statusIcon) = when (status) {
        "Approved" -> Triple(Color(0xFFE0F2FE), Color(0xFF0284C7), Icons.Default.CheckCircle)
        "Pending" -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), Icons.Default.HourglassTop)
        "Cancelled" -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), Icons.Default.Cancel)
        "Completed" -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), Icons.Default.TaskAlt)
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant, GrayText, Icons.Default.Info)
    }

    val displayStation = resolvedStationName ?: resolveStationName(reservation, emptyList())
    val formattedDate = formatDisplayDate(reservation.reservationDate)
    val formattedTime = formatDisplayTime(reservation.startTime, reservation.endTime)
    val estimatedCredits = reservation.energyAmountKwh * 44.50

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EvStation,
                            contentDescription = null,
                            tint = CharcoalText,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = displayStation,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = CharcoalText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Slot #${reservation.slotId.takeLast(4).ifBlank { "01" }.uppercase()} • Node Ref #${reservation.id.takeLast(6).uppercase()}",
                            fontSize = 11.sp,
                            color = GrayText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(statusIcon, contentDescription = null, tint = statusFg, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = status.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusFg,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = CharcoalText, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(formattedDate, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = GrayText, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(formattedTime, fontSize = 12.sp, color = GrayText, fontWeight = FontWeight.Medium)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = CharcoalText, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                "${reservation.energyAmountKwh} kWh",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CharcoalText
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            String.format(Locale.US, "Est: LKR %,.2f", estimatedCredits),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (status) {
                        "Approved" -> "Pass Ready • Tap to view QR Code"
                        "Completed" -> "Energy successfully fed to microgrid"
                        "Pending" -> "Awaiting operator dispatch clearance"
                        else -> "Session cancelled"
                    },
                    fontSize = 11.sp,
                    color = GrayText,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Details",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = CharcoalText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

fun resolveStationName(reservation: Reservation, stations: List<Station>): String {
    if (reservation.stationName.isNotBlank()) {
        return reservation.stationName
    }
    val matched = stations.firstOrNull { it.id == reservation.stationId }
    if (matched != null && matched.name.isNotBlank()) {
        return matched.name
    }

    val fallbackIdx = (reservation.stationId.hashCode().let { if (it < 0) -it else it } % 3) + 1
    return when (fallbackIdx) {
        1 -> "Central Solar Microgrid Hub"
        2 -> "EcoCharge Microgrid Alpha"
        else -> "South Grid Charging Terminal"
    }
}

fun formatDisplayDate(dateStr: String): String {
    return try {
        if (dateStr.length >= 10) {
            val datePart = dateStr.take(10)
            val parts = datePart.split("-")
            if (parts.size == 3) {
                val year = parts[0]
                val month = when (parts[1]) {
                    "01" -> "Jan"; "02" -> "Feb"; "03" -> "Mar"; "04" -> "Apr"
                    "05" -> "May"; "06" -> "Jun"; "07" -> "Jul"; "08" -> "Aug"
                    "09" -> "Sep"; "10" -> "Oct"; "11" -> "Nov"; "12" -> "Dec"
                    else -> parts[1]
                }
                val day = parts[2]
                "$day $month $year"
            } else {
                datePart
            }
        } else {
            dateStr
        }
    } catch (e: Exception) {
        dateStr.take(10)
    }
}

fun formatDisplayTime(start: String, end: String): String {
    val s = start.trim()
    val e = end.trim()
    return if (s.isNotBlank() && e.isNotBlank()) {
        "$s – $e"
    } else if (s.isNotBlank()) {
        "From $s"
    } else {
        "Standard Transfer Window"
    }
}
