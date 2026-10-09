package com.example.smartsolar.features.dashboard.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.example.smartsolar.features.reservations.ui.ReservationViewModel
import com.example.smartsolar.ui.theme.*

@Composable
fun ProsumerDashboardScreen(
    viewModel: MicrogridViewModel,
    reservationViewModel: ReservationViewModel? = null,
    token: String = "",
    prosumerName: String = "",
    nic: String = "",
    onNavigateToStations: () -> Unit,
    onNavigateToBookings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onCreateReservation: () -> Unit = {},
    onViewDetails: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val stationsState by viewModel.stationsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStations()
        if (token.isNotBlank() && nic.isNotBlank()) {
            reservationViewModel?.loadMyReservations(token, nic)
        }
    }

    val myReservations by reservationViewModel?.myReservations?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val pendingCount = myReservations.count { it.status == "Pending" }
    val approvedCount = myReservations.count { it.status == "Approved" }
    val completedCount = myReservations.count { it.status == "Completed" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            MicrogridStatusTicker()
        }
        item {
            ProsumerHeader(prosumerName, nic)
        }
        item {
            LivePowerFlowCard()
        }
        item {
            ProsumerSummaryCards(
                stationsState = stationsState,
                activeCount = approvedCount,
                pendingCount = pendingCount,
                completedCount = completedCount
            )
        }
        item {
            ProsumerQuickActions(
                onNavigateToStations = onNavigateToStations,
                onNavigateToBookings = onNavigateToBookings,
                onNavigateToHistory = onNavigateToHistory,
                onCreateReservation = onCreateReservation
            )
        }
        item {
            EnergyYieldMiniChart()
        }
        item {
            UpcomingReservationCard(
                reservation = myReservations.firstOrNull { it.status == "Approved" },
                onNavigateToBookings = onNavigateToBookings,
                onCreateReservation = onCreateReservation
            )
        }
        item {
            AvailableStationsPreview(stationsState, onNavigateToStations, onViewDetails)
        }
        item {
            RecentActivityList(myReservations)
        }
        item {
            Spacer(modifier = Modifier.height(130.dp))
        }
    }
}

@Composable
fun MicrogridStatusTicker() {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TickerPill(
            icon = Icons.Default.Sensors,
            text = "GRID ONLINE • 50.02 Hz",
            badgeColor = LimeAccent,
            textColor = CharcoalText
        )
        TickerPill(
            icon = Icons.Default.Hub,
            text = "NODE: LK-WEST-01",
            badgeColor = MaterialTheme.colorScheme.surfaceVariant,
            textColor = CharcoalText
        )
        TickerPill(
            icon = Icons.Default.Eco,
            text = "CLEAN OFFSET: 0.8 kg/kWh",
            badgeColor = MaterialTheme.colorScheme.surfaceVariant,
            textColor = CharcoalText
        )
        TickerPill(
            icon = Icons.Default.WbSunny,
            text = "IRRADIANCE: 890 W/m² (Peak)",
            badgeColor = MaterialTheme.colorScheme.surfaceVariant,
            textColor = CharcoalText
        )
    }
}

@Composable
fun TickerPill(icon: ImageVector, text: String, badgeColor: Color, textColor: Color) {
    Surface(
        color = badgeColor,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
fun ProsumerHeader(name: String, nic: String) {
    val displayName = name.ifBlank { "Prosumer" }
    val displayNic = nic.ifBlank { "NIC Not Provided" }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Welcome Back,",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = GrayText
        )
        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = CharcoalText,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = "Enterprise Solar Microgrid Dispatcher & Energy Trading Hub",
            style = MaterialTheme.typography.bodySmall,
            color = GrayText,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = CharcoalText,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "NIC: $displayNic",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Account: Active • Tier 1 Prosumer",
                                style = MaterialTheme.typography.labelMedium,
                                color = GrayText,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "VERIFIED",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CharcoalText
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Clean Energy Yield", fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("284.5 kWh", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "+14.8%",
                                    fontSize = 10.sp,
                                    color = Color(0xFF15803D),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Carbon Offset", fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Eco, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("274 kg CO₂", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LivePowerFlowCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Live Microgrid Power Flow",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = CharcoalText
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "TELEMETRY",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PowerNodeBox(
                    title = "Solar PV",
                    value = "4.8 kW",
                    subtext = "Active Generation",
                    icon = Icons.Default.WbSunny,
                    accentColor = Color(0xFFEAB308),
                    modifier = Modifier.weight(1f)
                )
                PowerNodeBox(
                    title = "Battery",
                    value = "84%",
                    subtext = "10.2 kWh Stored",
                    icon = Icons.Default.BatteryChargingFull,
                    accentColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PowerNodeBox(
                    title = "Home Load",
                    value = "1.6 kW",
                    subtext = "Self Consumed",
                    icon = Icons.Default.Home,
                    accentColor = Color(0xFF6366F1),
                    modifier = Modifier.weight(1f)
                )
                PowerNodeBox(
                    title = "Grid Feed",
                    value = "+3.2 kW",
                    subtext = "Exporting to Grid",
                    icon = Icons.Default.Bolt,
                    accentColor = LimeAccentDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Self-Sufficiency Rate", fontSize = 12.sp, color = GrayText, fontWeight = FontWeight.Medium)
                Text("92% (High Autonomy)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { 0.92f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = LimeAccentDark,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun PowerNodeBox(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.SemiBold)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
            Text(subtext, fontSize = 10.sp, color = GrayText, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

@Composable
fun ProsumerSummaryCards(
    stationsState: UiState<List<Station>>,
    activeCount: Int,
    pendingCount: Int,
    completedCount: Int
) {
    val activeStationsCount = if (stationsState is UiState.Success) {
        stationsState.data.filter { it.status == "Active" }.size
    } else {
        3
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                title = "Active Bookings",
                value = activeCount.toString(),
                icon = Icons.Default.EventAvailable,
                statusText = "Ready to dispatch",
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Pending Bookings",
                value = pendingCount.toString(),
                icon = Icons.Default.PendingActions,
                statusText = "Awaiting clearance",
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                title = "Completed Transfers",
                value = completedCount.toString(),
                icon = Icons.Default.CheckCircle,
                statusText = "Ledger verified",
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Available Stations",
                value = activeStationsCount.toString(),
                icon = Icons.Default.EvStation,
                statusText = "Grid hubs active",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    statusText: String? = null
) {
    Card(
        modifier = modifier.height(115.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = title,
                        tint = CharcoalText,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CharcoalText
                )
            }
            Column {
                Text(title, fontSize = 12.sp, color = CharcoalText, fontWeight = FontWeight.Bold, maxLines = 1)
                if (statusText != null) {
                    Text(statusText, fontSize = 10.sp, color = GrayText, fontWeight = FontWeight.Medium, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun ProsumerQuickActions(
    onNavigateToStations: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onCreateReservation: () -> Unit
) {
    Column {
        Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            QuickActionButton("Find Stations", Icons.Default.Search, subtitle = "Hubs", modifier = Modifier.weight(1f)) { onNavigateToStations() }
            QuickActionButton("New Booking", Icons.Default.Schedule, subtitle = "Reserve", modifier = Modifier.weight(1f)) { onCreateReservation() }
            QuickActionButton("My Bookings", Icons.Default.BookOnline, subtitle = "Tickets", modifier = Modifier.weight(1f)) { onNavigateToBookings() }
            QuickActionButton("History", Icons.Default.History, subtitle = "Audit Log", modifier = Modifier.weight(1f)) { onNavigateToHistory() }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .shadow(elevation = 5.dp, shape = RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = CharcoalText, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(7.dp))
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalText, maxLines = 1, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Text(subtitle, fontSize = 9.sp, color = GrayText, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

@Composable
fun EnergyYieldMiniChart() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Weekly Energy Yield", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
                    Text("Solar Generation vs Grid Export (kWh)", fontSize = 11.sp, color = GrayText)
                }
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Peak: Wed (34.2 kWh)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val heights = listOf(0.65f, 0.78f, 1.0f, 0.88f, 0.72f, 0.82f, 0.91f)
            val kwhValues = listOf("24.2", "28.6", "34.2", "30.1", "26.4", "29.0", "31.5")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEachIndexed { index, day ->
                    val isPeak = index == 2
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = kwhValues[index],
                            fontSize = 9.sp,
                            fontWeight = if (isPeak) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isPeak) CharcoalText else GrayText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .fillMaxHeight(heights[index] * 0.75f)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    if (isPeak) LimeAccentDark else LimeAccent.copy(alpha = 0.55f)
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = day,
                            fontSize = 10.sp,
                            fontWeight = if (isPeak) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (isPeak) CharcoalText else GrayText
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingReservationCard(
    reservation: Reservation?,
    onNavigateToBookings: () -> Unit = {},
    onCreateReservation: () -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Upcoming Reservation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
            if (reservation != null) {
                TextButton(onClick = onNavigateToBookings) {
                    Text("View Pass", fontSize = 13.sp, color = CharcoalText, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (reservation == null) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = CharcoalText, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("Peak Solar Window Active", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CharcoalText)
                            Text("Grid Demand: High • Feed-In Rate: +15%", fontSize = 12.sp, color = GrayText)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No active booking right now. Microgrid Hub Alpha has 4 charging slots open. Reserve a slot to transfer surplus solar energy and earn credits.",
                        fontSize = 12.sp,
                        color = GrayText,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onCreateReservation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CharcoalText,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Book Energy Transfer Slot", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        } else {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, LimeAccentDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = "Energy", tint = CharcoalText, modifier = Modifier.size(34.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(reservation.stationName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CharcoalText)
                        Text(
                            "${reservation.reservationDate.take(10)} • ${reservation.startTime} - ${reservation.endTime}",
                            fontSize = 12.sp,
                            color = GrayText,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    reservation.status.uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontSize = 10.sp,
                                    color = CharcoalText,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${reservation.energyAmountKwh} kWh", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayText)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onNavigateToBookings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = CharcoalText)
                    }
                }
            }
        }
    }
}

@Composable
fun AvailableStationsPreview(
    stationsState: UiState<List<Station>>,
    onNavigateToStations: () -> Unit,
    onViewDetails: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Microgrid Nodes Fleet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
            TextButton(onClick = onNavigateToStations) {
                Text("See All", fontSize = 14.sp, color = CharcoalText, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        when (stationsState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is UiState.Error -> {
                Text("Failed to load stations", color = MaterialTheme.colorScheme.error)
            }
            is UiState.Success -> {
                val activeStations = stationsState.data.filter { it.status == "Active" }

                if (activeStations.isEmpty()) {
                    Text("No active microgrid stations available.", color = GrayText, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(activeStations) { station ->
                            StationPreviewItem(
                                name = station.name,
                                capacity = "${station.capacityKw} kW",
                                slots = 4,
                                onViewDetails = { onViewDetails(station.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StationPreviewItem(name: String, capacity: String, slots: Int, onViewDetails: () -> Unit) {
    Card(
        modifier = Modifier.width(260.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.EvStation, contentDescription = null, tint = CharcoalText, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(name, fontWeight = FontWeight.Bold, color = CharcoalText, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "ONLINE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Node Capacity", fontSize = 10.sp, color = GrayText)
                    Text(capacity, fontWeight = FontWeight.Bold, color = CharcoalText, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Available Ports", fontSize = 10.sp, color = GrayText)
                    Text("$slots Ready", fontWeight = FontWeight.Bold, color = CharcoalText, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onViewDetails,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CharcoalText,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("View Node Details", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun RecentActivityList(activities: List<Reservation>) {
    Column {
        Text("Transfer Audit Ledger", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                if (activities.isEmpty()) {

                    AuditLogItem(
                        title = "Grid Connection Initialized",
                        location = "Microgrid Central Node LK-01",
                        date = "Today • Synchronized",
                        status = "Active",
                        icon = Icons.Default.CheckCircleOutline,
                        isLast = false
                    )
                    AuditLogItem(
                        title = "Solar Telemetry Calibrated",
                        location = "Smart Inverter & Battery Bank",
                        date = "Yesterday • 4.8 kW Peak",
                        status = "Verified",
                        icon = Icons.Default.Bolt,
                        isLast = false
                    )
                    AuditLogItem(
                        title = "Prosumer Tier 1 Approved",
                        location = "Smart Microgrid Authority",
                        date = "Sep 2026 • Verified",
                        status = "Approved",
                        icon = Icons.Default.Security,
                        isLast = true
                    )
                } else {
                    val displayList = activities.take(4)
                    displayList.forEachIndexed { index, reservation ->
                        val icon = when (reservation.status) {
                            "Approved" -> Icons.Default.CheckCircleOutline
                            "Completed" -> Icons.Default.Bolt
                            "Cancelled" -> Icons.Default.Cancel
                            else -> Icons.Default.PendingActions
                        }
                        val title = "Energy Transfer (${reservation.status})"
                        AuditLogItem(
                            title = title,
                            location = "${reservation.stationName} • ${reservation.energyAmountKwh} kWh",
                            date = "${reservation.reservationDate.take(10)} • ${reservation.startTime}",
                            status = reservation.status,
                            icon = icon,
                            isLast = index == displayList.lastIndex
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogItem(
    title: String,
    location: String,
    date: String,
    status: String,
    icon: ImageVector,
    isLast: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = CharcoalText, modifier = Modifier.size(16.dp))
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CharcoalText)
                Surface(
                    color = when (status) {
                        "Approved", "Active", "Verified" -> Color(0xFFDCFCE7)
                        "Completed" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        "Pending" -> Color(0xFFFEF3C7)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (status) {
                            "Approved", "Active", "Verified" -> Color(0xFF15803D)
                            "Pending" -> Color(0xFFB45309)
                            else -> CharcoalText
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(location, fontSize = 12.sp, color = GrayText)
            Text(date, fontSize = 11.sp, color = GrayText.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
        }
    }
}
