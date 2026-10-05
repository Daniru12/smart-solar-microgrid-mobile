package com.example.smartsolar.features.operator.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.reservations.models.Reservation
import com.example.smartsolar.features.reservations.ui.ReservationUiState
import com.example.smartsolar.features.reservations.ui.ReservationViewModel
import com.example.smartsolar.features.reservations.ui.formatDisplayDate
import com.example.smartsolar.features.reservations.ui.formatDisplayTime
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRVerificationResultScreen(
    reservation: Reservation,
    token: String,
    viewModel: ReservationViewModel,
    onComplete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isValid = reservation.status.equals("Approved", ignoreCase = true)
    val isCompleted = reservation.status.equals("Completed", ignoreCase = true)
    val isPending = reservation.status.equals("Pending", ignoreCase = true)

    val formattedDate = formatDisplayDate(reservation.reservationDate)
    val formattedTime = formatDisplayTime(reservation.startTime, reservation.endTime)
    val estimatedCredits = reservation.energyAmountKwh * 44.50
    val co2Saved = reservation.energyAmountKwh * 0.8

    LaunchedEffect(uiState) {
        if (uiState is ReservationUiState.Success) {
            viewModel.resetState()
            onComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dispatch Verification",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            color = CharcoalText
                        )
                        Text(
                            text = "Session Ref #${reservation.id.takeLast(6).uppercase()}",
                            fontSize = 11.sp,
                            color = GrayText
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CharcoalText
                        )
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Verification Status Card
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = when {
                    isValid -> CharcoalText
                    isCompleted -> Color(0xFFDCFCE7)
                    isPending -> Color(0xFFFEF3C7)
                    else -> Color(0xFFFEE2E2)
                },
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isValid -> LimeAccent.copy(alpha = 0.2f)
                                    isCompleted -> Color(0xFF15803D).copy(alpha = 0.15f)
                                    isPending -> Color(0xFFD97706).copy(alpha = 0.15f)
                                    else -> Color(0xFFDC2626).copy(alpha = 0.15f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isValid -> Icons.Default.CheckCircle
                                isCompleted -> Icons.Default.TaskAlt
                                isPending -> Icons.Default.HourglassTop
                                else -> Icons.Default.Cancel
                            },
                            contentDescription = null,
                            tint = when {
                                isValid -> LimeAccent
                                isCompleted -> Color(0xFF15803D)
                                isPending -> Color(0xFFD97706)
                                else -> Color(0xFFDC2626)
                            },
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                isValid -> "Reservation Verified!"
                                isCompleted -> "Transfer Completed"
                                isPending -> "Clearance Pending"
                                else -> "Invalid Reservation"
                            },
                            color = when {
                                isValid -> Color.White
                                isCompleted -> Color(0xFF15803D)
                                isPending -> Color(0xFF92400E)
                                else -> Color(0xFF991B1B)
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = when {
                                isValid -> "Clearance granted for ${reservation.energyAmountKwh} kWh feed-in"
                                isCompleted -> "Session finalized and credits booked to ledger"
                                isPending -> "Awaiting operator approval before transfer"
                                else -> "Status: ${reservation.status}. Cannot process transfer."
                            },
                            color = when {
                                isValid -> Color(0xFFD1D5DB)
                                isCompleted -> Color(0xFF166534)
                                isPending -> Color(0xFF78350F)
                                else -> Color(0xFF7F1D1D)
                            },
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Detailed Dispatch Ledger Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Station & Slot Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CharcoalText),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EvStation,
                                contentDescription = null,
                                tint = LimeAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = reservation.stationName,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
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

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color(0xFFE5E7EB)
                    )

                    // Prosumer Account Identity Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "PROSUMER IDENTIFIER",
                                fontSize = 10.sp,
                                color = GrayText,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = reservation.prosumerNic,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CharcoalText
                            )
                        }

                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Prosumer NIC", reservation.prosumerNic))
                            Toast.makeText(context, "Prosumer NIC copied!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy NIC",
                                tint = GrayText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4-Telemetry Telemetry Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DispatchMetricBox(
                            title = "ENERGY ALLOCATED",
                            value = "${reservation.energyAmountKwh} kWh",
                            icon = Icons.Default.Bolt,
                            accentColor = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                        DispatchMetricBox(
                            title = "EST. CREDIT PAYOUT",
                            value = String.format(Locale.US, "LKR %,.2f", estimatedCredits),
                            icon = Icons.Default.AccountBalanceWallet,
                            accentColor = Color(0xFF15803D),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DispatchMetricBox(
                            title = "SCHEDULED DATE",
                            value = formattedDate,
                            icon = Icons.Default.CalendarToday,
                            accentColor = CharcoalText,
                            modifier = Modifier.weight(1f)
                        )
                        DispatchMetricBox(
                            title = "TIME WINDOW",
                            value = formattedTime,
                            icon = Icons.Default.AccessTime,
                            accentColor = CharcoalText,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Carbon Savings Banner
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
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Estimated Green Offset",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                            Text(
                                text = String.format(Locale.US, "%.1f kg CO₂", co2Saved),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }
            }

            // Pre-Completion Operational Checklist
            if (isValid) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF9FAFB),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "OPERATIONAL CLEARANCE CHECKLIST",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrayText,
                            letterSpacing = 0.5.sp
                        )

                        ChecklistRow("Prosumer physical presence & credentials validated")
                        ChecklistRow("Station node frequency & inverter synchronized")
                        ChecklistRow("Energy transfer quota matches meter dispatch limit")
                    }
                }
            }

            // Error Display if any
            if (uiState is ReservationUiState.Error) {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = (uiState as ReservationUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isValid) {
                    // Mark as Completed Primary Button
                    Button(
                        onClick = {
                            viewModel.completeReservation(token, reservation.id) {}
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF15803D),
                            contentColor = Color.White
                        ),
                        enabled = uiState !is ReservationUiState.Loading,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        if (uiState is ReservationUiState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Finalizing Energy Transfer...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Mark as Completed",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Scan Another QR Button
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CharcoalText)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Scan Another QR Code",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else if (isPending) {
                    // Approve Button
                    Button(
                        onClick = {
                            viewModel.approveReservation(token, reservation.id) {}
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CharcoalText,
                            contentColor = LimeAccent
                        )
                    ) {
                        Icon(Icons.Default.ThumbUp, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Approve Reservation Clearance", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Back to Scanner", fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Back to Scanner", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DispatchMetricBox(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF9FAFB))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 9.sp,
                    color = GrayText,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CharcoalText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ChecklistRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF15803D),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = CharcoalText,
            fontWeight = FontWeight.Medium
        )
    }
}
