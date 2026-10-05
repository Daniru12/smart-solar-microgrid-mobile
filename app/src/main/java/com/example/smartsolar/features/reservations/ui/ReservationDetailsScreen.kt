package com.example.smartsolar.features.reservations.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
fun ReservationDetailsScreen(
    reservation: Reservation,
    token: String,
    viewModel: ReservationViewModel,
    microgridViewModel: MicrogridViewModel? = null,
    isOperator: Boolean = false,
    onNavigateBack: () -> Unit,
    onModify: (Reservation) -> Unit,
    onViewQR: (Reservation) -> Unit,
    onComplete: (Reservation) -> Unit,
    onApprove: (Reservation) -> Unit = {}
) {
    val context = LocalContext.current
    var showCancelDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val stationsState by microgridViewModel?.stationsState?.collectAsState() ?: remember { mutableStateOf(null) }

    val stationsList = remember(stationsState) {
        if (stationsState is UiState.Success) {
            (stationsState as UiState.Success<List<Station>>).data
        } else {
            emptyList()
        }
    }

    LaunchedEffect(Unit) {
        microgridViewModel?.loadStations()
    }

    LaunchedEffect(uiState) {
        if (uiState is ReservationUiState.Success) {
            viewModel.resetState()
            onNavigateBack()
        }
    }

    val status = reservation.status
    val (statusBg, statusBorder, statusFg, statusIcon, statusHeadline, statusSubtext) = when (status) {
        "Approved" -> Tuple6(
            Color(0xFFE0F2FE),
            Color(0xFF7DD3FC),
            Color(0xFF0284C7),
            Icons.Default.CheckCircle,
            "DISPATCH APPROVED",
            "Reservation is verified & active. Present QR pass at station."
        )
        "Pending" -> Tuple6(
            Color(0xFFFEF3C7),
            Color(0xFFFDE68A),
            Color(0xFFD97706),
            Icons.Default.HourglassTop,
            "AWAITING CLEARANCE",
            "Submitted to microgrid operator. Modification allowed up to 12h prior."
        )
        "Cancelled" -> Tuple6(
            Color(0xFFFEE2E2),
            Color(0xFFFECACA),
            Color(0xFFDC2626),
            Icons.Default.Cancel,
            "RESERVATION CANCELLED",
            "Slot capacity and energy quota have been released back to grid."
        )
        "Completed" -> Tuple6(
            Color(0xFFDCFCE7),
            Color(0xFF86EFAC),
            Color(0xFF15803D),
            Icons.Default.TaskAlt,
            "TRANSFER COMPLETED",
            "Energy successfully fed to microgrid. Credits added to ledger."
        )
        else -> Tuple6(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.outline,
            GrayText,
            Icons.Default.Info,
            status.uppercase(),
            "Status recorded in microgrid central database."
        )
    }

    val displayStation = resolveStationName(reservation, stationsList)
    val formattedDate = formatDisplayDate(reservation.reservationDate)
    val formattedTime = formatDisplayTime(reservation.startTime, reservation.endTime)
    val estimatedCredits = reservation.energyAmountKwh * 44.50
    val co2Saved = reservation.energyAmountKwh * 0.8

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Reservation Details", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = CharcoalText)
                        Text("Session Ref #${reservation.id.takeLast(6).uppercase()}", fontSize = 11.sp, color = GrayText)
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
                shape = RoundedCornerShape(20.dp),
                color = statusBg,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, statusBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(statusFg.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(statusIcon, contentDescription = null, tint = statusFg, modifier = Modifier.size(30.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = statusHeadline,
                            color = statusFg,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = statusSubtext,
                            color = CharcoalText.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CharcoalText),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.EvStation, contentDescription = null, tint = LimeAccent, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayStation,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = CharcoalText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Station Node ID: #${reservation.stationId.takeLast(6).uppercase()} • Slot #${reservation.slotId.takeLast(4).ifBlank { "01" }.uppercase()}",
                                fontSize = 11.sp,
                                color = GrayText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TelemetryBox(
                            title = "ENERGY ALLOCATED",
                            value = "${reservation.energyAmountKwh} kWh",
                            icon = Icons.Default.Bolt,
                            accentColor = CharcoalText,
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryBox(
                            title = "ESTIMATED CREDITS",
                            value = String.format(Locale.US, "LKR %,.2f", estimatedCredits),
                            icon = Icons.Default.AccountBalanceWallet,
                            accentColor = Color(0xFF15803D),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TelemetryBox(
                            title = "TRANSFER DATE",
                            value = formattedDate,
                            icon = Icons.Default.CalendarToday,
                            accentColor = CharcoalText,
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryBox(
                            title = "TIME WINDOW",
                            value = formattedTime,
                            icon = Icons.Default.AccessTime,
                            accentColor = CharcoalText,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Eco, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Carbon Emission Offset", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            }
                            Text(
                                String.format(Locale.US, "%.1f kg CO₂", co2Saved),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF15803D)
                            )
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
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Technical Audit & Identifiers", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CharcoalText)

                    AuditRow(
                        label = "Full Reservation ID",
                        value = reservation.id,
                        isCopyable = true,
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Reservation ID", reservation.id))
                            Toast.makeText(context, "Reservation ID copied!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    AuditRow(label = "Prosumer NIC", value = reservation.prosumerNic)
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    AuditRow(label = "Grid Rate Tariff", value = "LKR 44.50 per kWh (Fixed Feed-In)")
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    AuditRow(label = "Record Created", value = formatDisplayDate(reservation.createdAt))

                    if (!reservation.notes.isNullOrBlank()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        AuditRow(label = "Operator Dispatch Notes", value = reservation.notes)
                    }
                }
            }

            if (!isOperator) {

                when (reservation.status) {
                    "Approved" -> {
                        Button(
                            onClick = { onViewQR(reservation) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalText, contentColor = LimeAccent),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("View QR Access Pass", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }

                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Cancel Reservation", fontWeight = FontWeight.Bold)
                        }
                    }
                    "Pending" -> {
                        Button(
                            onClick = { onModify(reservation) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalText, contentColor = LimeAccent),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Modify Reservation Time / Slot", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        }

                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Cancel Reservation", fontWeight = FontWeight.Bold)
                        }
                    }
                    "Completed" -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Session Finalized. Credits have been recorded to your Smart Microgrid wallet.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF15803D),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalText, contentColor = LimeAccent)
                        ) {
                            Text("Back to My Bookings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                    else -> {
                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalText, contentColor = LimeAccent)
                        ) {
                            Text("Back to My Bookings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            } else {

                if (reservation.status == "Pending") {
                    Button(
                        onClick = { onApprove(reservation) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalText, contentColor = LimeAccent)
                    ) {
                        Icon(Icons.Default.ThumbUp, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Approve Reservation", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
                if (reservation.status == "Approved") {
                    Button(
                        onClick = { onComplete(reservation) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D), contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.FlashOn, null, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Complete Energy Transfer", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Reservation", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to cancel this energy transfer reservation? Your reserved slot capacity will be made available to other prosumers.") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    viewModel.cancelReservation(token, reservation.id) {}
                }) {
                    Text("Confirm Cancellation", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Reservation", fontWeight = FontWeight.SemiBold, color = CharcoalText)
                }
            }
        )
    }
}

@Composable
private fun TelemetryBox(
    title: String,
    value: String,
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
                Text(title, fontSize = 10.sp, color = GrayText, fontWeight = FontWeight.SemiBold)
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(15.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CharcoalText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AuditRow(
    label: String,
    value: String,
    isCopyable: Boolean = false,
    onCopy: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CharcoalText)
        }
        if (isCopyable) {
            IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = GrayText, modifier = Modifier.size(16.dp))
            }
        }
    }
}

private data class Tuple6<A, B, C, D, E, F>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F
)
