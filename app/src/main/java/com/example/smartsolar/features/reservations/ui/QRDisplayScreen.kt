package com.example.smartsolar.features.reservations.ui

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.reservations.models.Reservation
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRDisplayScreen(
    reservation: Reservation,
    onNavigateBack: () -> Unit
) {
    val qrBitmap = remember(reservation.id) { generateQRBitmap(reservation) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reservation QR", fontWeight = FontWeight.Bold, color = CharcoalText) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(Icons.Default.QrCode2, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            Text("Scan this QR at the station", style = MaterialTheme.typography.bodyLarge, color = GrayText)

            // QR Code
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(260.dp).padding(16.dp)
                    )
                } else {
                    Box(modifier = Modifier.size(260.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Reservation summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(reservation.stationName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CharcoalText)
                    Text("${reservation.reservationDate.take(10)}  •  ${reservation.startTime} – ${reservation.endTime}", fontSize = 13.sp, color = GrayText)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ID: ...${reservation.id.takeLast(8).uppercase()}", fontSize = 12.sp, color = GrayText)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = androidx.compose.ui.graphics.Color(0xFF22C55E).copy(alpha = 0.15f)
                        ) {
                            Text(
                                reservation.status,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = androidx.compose.ui.graphics.Color(0xFF22C55E),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Text(
                "Show this QR code to the Grid Operator at the station.",
                style = MaterialTheme.typography.bodySmall,
                color = GrayText,
                modifier = Modifier.padding(horizontal = 16.dp)
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
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}
