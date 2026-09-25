package com.example.smartsolar.features.reservations.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.reservations.models.Reservation
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailsScreen(
    reservation: Reservation,
    token: String,
    viewModel: ReservationViewModel,
    isOperator: Boolean = false,
    onNavigateBack: () -> Unit,
    onModify: (Reservation) -> Unit,
    onViewQR: (Reservation) -> Unit,
    onComplete: (Reservation) -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is ReservationUiState.Success) {
            viewModel.resetState()
            onNavigateBack()
        }
    }

    val statusColor = when (reservation.status) {
        "Approved" -> Color(0xFF22C55E)
        "Pending" -> Color(0xFFF59E0B)
        "Cancelled" -> MaterialTheme.colorScheme.error
        "Completed" -> MaterialTheme.colorScheme.primary
        else -> GrayText
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reservation Details", fontWeight = FontWeight.Bold, color = CharcoalText) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = CharcoalText) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = statusColor.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        when (reservation.status) {
                            "Approved" -> Icons.Default.CheckCircle
                            "Pending" -> Icons.Default.HourglassEmpty
                            "Cancelled" -> Icons.Default.Cancel
                            "Completed" -> Icons.Default.Done
                            else -> Icons.Default.Info
                        },
                        null, tint = statusColor, modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(reservation.status, color = statusColor, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
            }

            // Details card
            Card(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    DetailRow(Icons.Default.ConfirmationNumber, "Reservation ID", reservation.id.takeLast(8).uppercase())
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    DetailRow(Icons.Default.ElectricBolt, "Station", reservation.stationName)
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    DetailRow(Icons.Default.CalendarToday, "Date", reservation.reservationDate.take(10))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    DetailRow(Icons.Default.AccessTime, "Time", "${reservation.startTime} – ${reservation.endTime}")
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    DetailRow(Icons.Default.BatteryChargingFull, "Energy", "${reservation.energyAmountKwh} kWh")
                    if (!reservation.notes.isNullOrBlank()) {
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        DetailRow(Icons.Default.Notes, "Notes", reservation.notes)
                    }
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    DetailRow(Icons.Default.Schedule, "Created", reservation.createdAt.take(10))
                }
            }

            // Action buttons
            if (!isOperator) {
                // Prosumer actions
                if (reservation.status == "Approved") {
                    Button(
                        onClick = { onViewQR(reservation) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = CharcoalText)
                    ) {
                        Icon(Icons.Default.QrCode2, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("View QR Code", fontWeight = FontWeight.Bold)
                    }
                }
                if (reservation.status == "Pending") {
                    Button(
                        onClick = { onModify(reservation) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = CharcoalText)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Modify Reservation", fontWeight = FontWeight.Bold)
                    }
                }
                if (reservation.status == "Pending" || reservation.status == "Approved") {
                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cancel Reservation", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Operator actions
                if (reservation.status == "Approved") {
                    Button(
                        onClick = { onComplete(reservation) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E), contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.FlashOn, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Complete Energy Transfer", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (uiState is ReservationUiState.Error) {
                Surface(color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text((uiState as ReservationUiState.Error).message,
                        color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp), fontSize = 14.sp)
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Reservation") },
            text = { Text("Are you sure you want to cancel this reservation? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    viewModel.cancelReservation(token, reservation.id) {}
                }) { Text("Cancel Reservation", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showCancelDialog = false }) { Text("Keep") } }
        )
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 15.sp, color = CharcoalText, fontWeight = FontWeight.SemiBold)
        }
    }
}
