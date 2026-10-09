package com.example.smartsolar.features.reservations.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.reservations.models.Reservation
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRDisplayScreen(
    reservation: Reservation,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val qrBitmap = remember(reservation.id) { generateQRBitmap(reservation) }

    val formattedDate = formatDisplayDate(reservation.reservationDate)
    val formattedTime = formatDisplayTime(reservation.startTime, reservation.endTime)
    val estimatedCredits = reservation.energyAmountKwh * 44.50
    val co2Saved = reservation.energyAmountKwh * 0.8

    val (statusBg, statusFg, statusIcon, statusLabel) = when (reservation.status) {
        "Approved" -> Quad(
            Color(0xFFDCFCE7),
            Color(0xFF15803D),
            Icons.Default.CheckCircle,
            "READY FOR SCAN"
        )
        "Pending" -> Quad(
            Color(0xFFFEF3C7),
            Color(0xFFD97706),
            Icons.Default.HourglassTop,
            "PENDING CLEARANCE"
        )
        "Completed" -> Quad(
            Color(0xFFE0F2FE),
            Color(0xFF0284C7),
            Icons.Default.TaskAlt,
            "COMPLETED"
        )
        "Cancelled" -> Quad(
            Color(0xFFFEE2E2),
            Color(0xFFDC2626),
            Icons.Default.Cancel,
            "CANCELLED"
        )
        else -> Quad(
            Color(0xFFF3F4F6),
            GrayText,
            Icons.Default.Info,
            reservation.status.uppercase()
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Energy Dispatch Pass",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            color = CharcoalText
                        )
                        Text(
                            text = "Ref #${reservation.id.takeLast(6).uppercase()} • Station Node",
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
                actions = {
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Reservation ID", reservation.id))
                        Toast.makeText(context, "Reservation ID copied!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy ID",
                            tint = GrayText
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
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
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = reservation.stationName,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = CharcoalText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Slot #${reservation.slotId.takeLast(4).ifBlank { "01" }.uppercase()} • Node #${reservation.stationId.takeLast(4).uppercase()}",
                                    fontSize = 11.sp,
                                    color = GrayText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = statusBg,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = statusIcon,
                                    contentDescription = null,
                                    tint = statusFg,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = statusLabel,
                                    color = statusFg,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.4.sp
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE5E7EB)),
                            shadowElevation = 2.dp
                        ) {
                            Box(
                                modifier = Modifier.padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (qrBitmap != null) {
                                    Image(
                                        bitmap = qrBitmap.asImageBitmap(),
                                        contentDescription = "Reservation QR Code",
                                        modifier = Modifier.size(220.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.size(220.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = CharcoalText,
                                            strokeWidth = 3.dp,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (reservation.status == "Approved") Color(0xFF22C55E) else GrayText)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (reservation.status == "Approved") "Active QR Pass • Ready for kiosk scan" else "Status: ${reservation.status}",
                            fontSize = 12.sp,
                            color = CharcoalText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF9FAFB),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        modifier = Modifier
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Reservation ID", reservation.id))
                                Toast.makeText(context, "ID copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "REF: #${reservation.id.takeLast(10).uppercase()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrayText,
                                letterSpacing = 0.5.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = GrayText,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                            drawLine(
                                color = Color(0xFFE5E7EB),
                                start = Offset(16.dp.toPx(), 0f),
                                end = Offset(size.width - 16.dp.toPx(), 0f),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PassTelemetryBox(
                                title = "ALLOCATED ENERGY",
                                value = "${reservation.energyAmountKwh} kWh",
                                icon = Icons.Default.Bolt,
                                accentColor = Color(0xFFF59E0B),
                                modifier = Modifier.weight(1f)
                            )
                            PassTelemetryBox(
                                title = "CO2 OFFSET",
                                value = String.format(Locale.US, "%.1f kg", reservation.energyAmountKwh * 0.8),
                                icon = Icons.Default.Eco,
                                accentColor = Color(0xFF15803D),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PassTelemetryBox(
                                title = "DISPATCH DATE",
                                value = formattedDate,
                                icon = Icons.Default.CalendarToday,
                                accentColor = CharcoalText,
                                modifier = Modifier.weight(1f)
                            )
                            PassTelemetryBox(
                                title = "TIME WINDOW",
                                value = formattedTime,
                                icon = Icons.Default.AccessTime,
                                accentColor = CharcoalText,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFDCFCE7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
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
                                    text = "Carbon Emission Saved",
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

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF9FAFB),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Turn up your phone brightness and present this QR code to the station operator or kiosk scanner.",
                        fontSize = 12.sp,
                        color = GrayText,
                        lineHeight = 17.sp
                    )
                }
            }

            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CharcoalText,
                    contentColor = LimeAccent
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Done & Back to Bookings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PassTelemetryBox(
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
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
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

private fun generateQRBitmap(reservation: Reservation): Bitmap? {
    return try {
        val json = JSONObject().apply {
            put("reservationId", reservation.id)
            put("prosumerNic", reservation.prosumerNic)
            put("stationId", reservation.stationId)
            put("stationName", reservation.stationName)
            put("date", reservation.reservationDate.take(10))
            put("startTime", reservation.startTime)
            put("endTime", reservation.endTime)
            put("energyKwh", reservation.energyAmountKwh)
            put("status", reservation.status)
        }.toString()

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(json, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}

private data class Quad<A, B, C, D>(
    val a: A, val b: B, val c: C, val d: D
)
