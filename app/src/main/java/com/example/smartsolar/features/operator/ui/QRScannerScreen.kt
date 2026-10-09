package com.example.smartsolar.features.operator.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.smartsolar.features.reservations.models.Reservation
import com.example.smartsolar.features.reservations.ui.ReservationUiState
import com.example.smartsolar.features.reservations.ui.ReservationViewModel
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import org.json.JSONObject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    viewModel: ReservationViewModel,
    token: String,
    onQRScanned: (Reservation) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var scanStatus by remember { mutableStateOf("Point camera at QR code") }
    var isProcessing by remember { mutableStateOf(false) }

    val selectedReservation by viewModel.selectedReservation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.clearSelectedReservation()
        viewModel.resetState()
        isProcessing = false
        scanStatus = "Point camera at QR code"
    }

    LaunchedEffect(selectedReservation, isLoading) {
        if (isProcessing && selectedReservation != null && !isLoading) {
            val res = selectedReservation!!
            isProcessing = false
            viewModel.clearSelectedReservation()
            onQRScanned(res)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is ReservationUiState.Error) {
            scanStatus = (uiState as ReservationUiState.Error).message
            isProcessing = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR Code", fontWeight = FontWeight.Bold, color = CharcoalText) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = CharcoalText) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!hasCameraPermission) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Icon(Icons.Default.QrCodeScanner, null, tint = GrayText, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Camera permission is required to scan QR codes.", color = GrayText, fontSize = 16.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Grant Permission")
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val options = BarcodeScannerOptions.Builder()
                                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
                                val scanner = BarcodeScanning.getClient(options)
                                val imageAnalyzer = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build().also { analysis ->
                                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                            val mediaImage = imageProxy.image
                                            if (mediaImage != null && !isProcessing) {
                                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                                scanner.process(image)
                                                    .addOnSuccessListener { barcodes ->
                                                        for (barcode in barcodes) {
                                                            val raw = barcode.rawValue
                                                            if (raw != null && !isProcessing) {
                                                                isProcessing = true
                                                                try {
                                                                    val reservationId = extractReservationId(raw)
                                                                    scanStatus = "Validating reservation..."
                                                                    viewModel.validateQr(token, reservationId)
                                                                } catch (e: Exception) {
                                                                    scanStatus = "Invalid QR code format"
                                                                    isProcessing = false
                                                                }
                                                            }
                                                        }
                                                    }
                                                    .addOnFailureListener { isProcessing = false }
                                                    .addOnCompleteListener { imageProxy.close() }
                                            } else {
                                                imageProxy.close()
                                            }
                                        }
                                    }
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
                                } catch (e: Exception) {
                                    scanStatus = "Camera failed: ${e.message}"
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.Black.copy(alpha = 0.5f)) {
                            Text(
                                if (isLoading) "Verifying..." else scanStatus,
                                color = Color.White,
                                modifier = Modifier.padding(16.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCodeScanner, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("Scan the prosumer's QR code to verify their reservation.", color = GrayText, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/**
 * Parses QR codes from both Mobile (JSON) and Web (SSM|reservationId|token) formats.
 */
private fun extractReservationId(raw: String): String {
    return try {
        val trimmed = raw.trim()
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            val json = JSONObject(trimmed)
            when {
                json.has("reservationId") -> json.getString("reservationId")
                json.has("id") -> json.getString("id")
                json.has("bookingId") -> json.getString("bookingId")
                json.has("qrPayload") -> {
                    val p = json.getString("qrPayload")
                    if (p.startsWith("SSM|")) p.split("|").getOrNull(1) ?: p else p
                }
                else -> trimmed
            }
        } else if (trimmed.startsWith("SSM|")) {
            // Web QR code payload format: "SSM|reservationId|token"
            val parts = trimmed.split("|")
            if (parts.size >= 2 && parts[1].isNotBlank()) parts[1] else trimmed
        } else if (trimmed.contains("|")) {
            // Any pipe-delimited format containing reservationId
            val parts = trimmed.split("|")
            val hex24 = parts.firstOrNull { it.length == 24 && it.all { c -> c.isDigit() || c in 'a'..'f' || c in 'A'..'F' } }
            hex24 ?: (if (parts.size >= 2) parts[1] else parts[0])
        } else if (trimmed.contains("/")) {
            trimmed.substringAfterLast("/")
        } else {
            trimmed
        }
    } catch (e: Exception) {
        raw.trim()
    }
}
