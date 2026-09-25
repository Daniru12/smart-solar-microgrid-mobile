package com.example.smartsolar.features.operator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.smartsolar.features.reservations.ui.ReservationUiState
import com.example.smartsolar.features.reservations.ui.ReservationViewModel
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRVerificationResultScreen(
    reservation: Reservation,
    token: String,
    viewModel: ReservationViewModel,
    onComplete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isValid = reservation.status == "Approved"

    LaunchedEffect(uiState) {
        if (uiState is ReservationUiState.Success) {
            viewModel.resetState()
            onComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verification Result", fontWeight = FontWeight.Bold, color = CharcoalText) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = CharcoalText) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Result icon
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (isValid) Color(0xFF22C55E).copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        if (isValid) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        null,
                        tint = if (isValid) Color(0xFF22C55E) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Text(
                if (isValid) "Reservation Verified!" else "Invalid QR Code",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = if (isValid) Color(0xFF22C55E) else MaterialTheme.colorScheme.error
            )

            if (isValid) {
                Card(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ResultRow(Icons.Default.Person, "Prosumer", reservation.prosumerNic)
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ResultRow(Icons.Default.ElectricBolt, "Station", reservation.stationName)
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ResultRow(Icons.Default.CalendarToday, "Date", reservation.reservationDate.take(10))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ResultRow(Icons.Default.AccessTime, "Time", "${reservation.startTime} – ${reservation.endTime}")
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ResultRow(Icons.Default.BatteryChargingFull, "Energy", "${reservation.energyAmountKwh} kWh")
                    }
                }

                if (uiState is ReservationUiState.Error) {
                    Surface(color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text((uiState as ReservationUiState.Error).message,
                            color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp), fontSize = 14.sp)
                    }
                }

                Button(
                    onClick = {
                        viewModel.approveReservation(token, reservation.id) {}
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E), contentColor = Color.White),
                    enabled = uiState !is ReservationUiState.Loading
                ) {
                    if (uiState is ReservationUiState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    } else {
                        Icon(Icons.Default.FlashOn, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Complete Energy Transfer", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Status: ${reservation.status}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            when (reservation.status) {
                                "Pending" -> "This reservation has not been approved yet."
                                "Cancelled" -> "This reservation was cancelled."
                                "Completed" -> "This reservation was already completed."
                                else -> "Reservation cannot be processed."
                            },
                            color = GrayText, fontSize = 14.sp
                        )
                    }
                }
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Scan Again", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ResultRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = GrayText)
            Text(value, fontSize = 15.sp, color = CharcoalText, fontWeight = FontWeight.SemiBold)
        }
    }
}
