package com.example.smartsolar.features.backoffice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.microgrid.ui.MicrogridViewModel
import com.example.smartsolar.features.reservations.models.Reservation
import com.example.smartsolar.features.reservations.ui.ReservationViewModel
import com.example.smartsolar.ui.theme.*

private val BrandAccent = LimeAccent
private val AmberWarning = Color(0xFFF59E0B)
private val AmberLight = Color(0xFFFEF3C7)

@Composable
fun BackofficeDashboardScreen(
    backofficeViewModel: BackofficeViewModel,
    microgridViewModel: MicrogridViewModel,
    reservationViewModel: ReservationViewModel,
    token: String,
    onNavigateToUsers: () -> Unit,
    onNavigateToProsumers: () -> Unit,
    onNavigateToBookings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val users by backofficeViewModel.users.collectAsState()
    val prosumers by backofficeViewModel.prosumers.collectAsState()
    val deactivationRequests by backofficeViewModel.deactivationRequests.collectAsState()
    val stationsState by microgridViewModel.stationsState.collectAsState()
    val allReservations by reservationViewModel.allReservations.collectAsState()

    val pendingReservations = allReservations.filter { it.status.equals("Pending", ignoreCase = true) }
    val stationsCount = when (val state = stationsState) {
        is com.example.smartsolar.features.microgrid.ui.UiState.Success -> state.data.size
        else -> 0
    }

    LaunchedEffect(token) {
        if (token.isNotBlank()) {
            backofficeViewModel.loadAll(token)
            reservationViewModel.loadAll(token)
            microgridViewModel.loadStations()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Officer Header Banner
        item {
            BackofficeHeader(usersCount = users.size, pendingCount = pendingReservations.size)
        }

        // 2. Deactivation Alert Banner (if any pending)
        if (deactivationRequests.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToProsumers() },
                    colors = CardDefaults.cardColors(containerColor = AmberLight),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AmberWarning, AmberWarning)))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AmberWarning),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Warning, "Warning", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Deactivation Requests Pending",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                "${deactivationRequests.size} prosumer account(s) awaiting review",
                                fontSize = 12.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                        Icon(Icons.Filled.ChevronRight, "Go", tint = Color(0xFF92400E))
                    }
                }
            }
        }

        // 3. Key Operational KPI Cards
        item {
            Text(
                "SYSTEM OVERVIEW",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                letterSpacing = 1.2.sp,
                color = GrayText
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OfficerStatCard(
                    title = "System Users",
                    value = users.size.toString(),
                    subtitle = "Accounts Total",
                    icon = Icons.Filled.People,
                    accentColor = CharcoalText,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToUsers
                )
                OfficerStatCard(
                    title = "Prosumers",
                    value = prosumers.size.toString(),
                    subtitle = "Grid Producers",
                    icon = Icons.Filled.AssignmentInd,
                    accentColor = Color(0xFF059669),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToProsumers
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OfficerStatCard(
                    title = "Microgrid Nodes",
                    value = stationsCount.toString(),
                    subtitle = "Distributed Hubs",
                    icon = Icons.Filled.EvStation,
                    accentColor = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
                OfficerStatCard(
                    title = "Pending Bookings",
                    value = pendingReservations.size.toString(),
                    subtitle = "Requires Action",
                    icon = Icons.Filled.HourglassTop,
                    accentColor = if (pendingReservations.isNotEmpty()) AmberWarning else Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToBookings
                )
            }
        }

        // 4. Quick Governance Actions
        item {
            Text(
                "OFFICER ACTION CENTER",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                letterSpacing = 1.2.sp,
                color = GrayText
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OfficerActionTile(
                    title = "User Access",
                    desc = "Activate & role assignment",
                    icon = Icons.Filled.ManageAccounts,
                    color = CharcoalText,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToUsers
                )
                OfficerActionTile(
                    title = "Deactivations",
                    desc = "${deactivationRequests.size} review requests",
                    icon = Icons.Filled.PersonOff,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToProsumers
                )
            }
        }

        // 5. Urgent Pending Approvals
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "PENDING RESERVATIONS (${pendingReservations.size})",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.2.sp,
                    color = GrayText
                )
                if (pendingReservations.isNotEmpty()) {
                    Text(
                        "View All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText,
                        modifier = Modifier.clickable { onNavigateToBookings() }
                    )
                }
            }
        }

        if (pendingReservations.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.CheckCircle, "Clean", tint = Color(0xFF10B981), modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("All caught up!", fontWeight = FontWeight.Bold, color = CharcoalText)
                        Text("No pending energy reservation approvals right now.", fontSize = 12.sp, color = GrayText)
                    }
                }
            }
        } else {
            items(pendingReservations.take(4)) { res ->
                PendingReservationItem(
                    reservation = res,
                    onApprove = {
                        reservationViewModel.approveReservation(token, res.id) {
                            reservationViewModel.loadAll(token)
                        }
                    }
                )
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun BackofficeHeader(usersCount: Int, pendingCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(BrandAccent.copy(alpha = 0.25f), Color.Transparent)))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CharcoalText)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "BACKOFFICE OFFICER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = CharcoalText
                            )
                        }
                        Text(
                            "Command Center",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CharcoalText
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandAccent)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "CLEARANCE L1",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CharcoalText
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "You have authority over user accounts, deactivation verification, and system reservations.",
                    fontSize = 12.sp,
                    color = GrayText,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
fun OfficerStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, title, tint = accentColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
            Text(subtitle, fontSize = 10.sp, color = GrayText)
        }
    }
}

@Composable
fun OfficerActionTile(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, title, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalText)
                Text(desc, fontSize = 10.sp, color = GrayText)
            }
        }
    }
}

@Composable
fun PendingReservationItem(
    reservation: Reservation,
    onApprove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    reservation.stationName.ifBlank { "Microgrid Station" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CharcoalText
                )
                Text("NIC: ${reservation.prosumerNic}", fontSize = 12.sp, color = GrayText)
                Text(
                    "${reservation.energyAmountKwh} kWh • ${reservation.reservationDate} (${reservation.startTime})",
                    fontSize = 11.sp,
                    color = GrayText,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Button(
                onClick = onApprove,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandAccent, contentColor = CharcoalText),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Filled.Check, "Approve", modifier = Modifier.size(16.dp), tint = CharcoalText)
                Spacer(Modifier.width(4.dp))
                Text("Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
            }
        }
    }
}
