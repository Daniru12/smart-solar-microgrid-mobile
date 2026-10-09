package com.example.smartsolar.features.microgrid.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.smartsolar.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smartsolar.features.microgrid.models.Station
import com.example.smartsolar.features.microgrid.models.CreateSlotRequest
import com.example.smartsolar.features.microgrid.models.EnergySlot
import com.example.smartsolar.features.microgrid.models.isStationAssignedToOperator
import com.example.smartsolar.ui.theme.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDetailsScreen(
    stationId: String,
    viewModel: MicrogridViewModel,
    isOperator: Boolean = false,
    operatorEmail: String = "",
    operatorName: String = "",
    operatorStationId: String = "",
    onViewSlotsClicked: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.selectedStationState.collectAsState()
    val slotsState by viewModel.slotsState.collectAsState()
    var selectedTab by remember { mutableStateOf("Today") }
    val tabs = listOf("Today", "Weeks", "Months", "Years")
    var showMapDialog by remember { mutableStateOf(false) }

    var slotTab by remember { mutableStateOf("active") }
    var showAddSlotDialog by remember { mutableStateOf(false) }
    var slotDeleteTarget by remember { mutableStateOf<EnergySlot?>(null) }
    var isTogglingStatus by remember { mutableStateOf(false) }

    LaunchedEffect(stationId) {
        viewModel.loadStationDetails(stationId)
        viewModel.loadSlots(stationId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Station Overview",
                        fontWeight = FontWeight.ExtraBold,
                        color = CharcoalText,
                        fontSize = 19.sp
                    )
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
                        viewModel.loadStationDetails(stationId)
                        viewModel.loadSlots(stationId)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = CharcoalText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (state is UiState.Success) {
                val station = (state as UiState.Success).data
                val isAssigned = !isOperator || isStationAssignedToOperator(station, operatorEmail, operatorName, operatorStationId)
                if (isAssigned) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 12.dp,
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 20.dp, vertical = 14.dp)
                        ) {
                            if (isOperator) {
                                Button(
                                    onClick = { showAddSlotDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CharcoalText,
                                        contentColor = LimeAccent
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddCircleOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = "Add New Energy Slot",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { onViewSlotsClicked(station.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CharcoalText,
                                        contentColor = LimeAccent
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ElectricBolt,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = "Book Now (View Available Slots)",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            when (state) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = CharcoalText,
                                strokeWidth = 3.dp
                            )
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = "Loading Station Telemetry...",
                                fontSize = 14.sp,
                                color = GrayText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                is UiState.Error -> {
                    val error = (state as UiState.Error).message
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = "Failed to load station details",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = error,
                                    color = Color(0xFFB91C1C),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(14.dp))
                                Button(
                                    onClick = { viewModel.loadStationDetails(stationId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val station = (state as UiState.Success).data
                    val isAssigned = !isOperator || isStationAssignedToOperator(station, operatorEmail, operatorName, operatorStationId)

                    if (!isAssigned) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Color(0xFFFEE2E2)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFEE2E2)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Block,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        text = "Access Denied",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = CharcoalText
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Grid Operators can only view and manage their own assigned station.",
                                        textAlign = TextAlign.Center,
                                        fontSize = 13.sp,
                                        color = GrayText
                                    )
                                    Spacer(Modifier.height(20.dp))
                                    Button(
                                        onClick = onNavigateBack,
                                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalText),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Return to Dashboard", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        // Function to launch Google Maps / Navigation
                        fun openGoogleMaps() {
                            val lat = if (station.latitude != 0.0) station.latitude else 5.9549
                            val lng = if (station.longitude != 0.0) station.longitude else 80.5550
                            val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(station.name)})")
                            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            try {
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                try {
                                    val fallbackIntent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(fallbackIntent)
                                } catch (e2: Exception) {
                                    val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                }
                            }
                        }

                        // Dialog showing full interactive map when user clicks the map
                        if (showMapDialog) {
                            StationMapDialog(
                                station = station,
                                onDismiss = { showMapDialog = false },
                                onOpenExternalMaps = { openGoogleMaps() }
                            )
                        }

                        // Dialog for creating a new energy slot
                        if (showAddSlotDialog) {
                            CreateSlotDialog(
                                stationId = station.id,
                                viewModel = viewModel,
                                onDismiss = { showAddSlotDialog = false }
                            )
                        }

                        // Dialog for confirming slot deletion
                        slotDeleteTarget?.let { target ->
                            DeleteSlotConfirmDialog(
                                slot = target,
                                onConfirm = {
                                    viewModel.deleteSlot(
                                        stationId = station.id,
                                        slotId = target.id,
                                        onSuccess = {
                                            slotDeleteTarget = null
                                            Toast.makeText(context, "Slot moved to history", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { err ->
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                onDismiss = { slotDeleteTarget = null }
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 1. Station Hero Image Card
                            val isLive = station.status.equals("Active", ignoreCase = true)
                            Card(
                                shape = RoundedCornerShape(22.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Image(
                                        painter = painterResource(id = R.drawable.solar_panel_img),
                                        contentDescription = station.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    // Gradient overlay for readability
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Black.copy(alpha = 0.2f),
                                                        Color.Black.copy(alpha = 0.82f)
                                                    ),
                                                    startY = 0f
                                                )
                                            )
                                    )

                                    // Live Status Badge in top right
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isLive) Color(0xFF15803D).copy(alpha = 0.92f) else Color(0xFFDC2626).copy(alpha = 0.92f),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = if (isLive) "ACTIVE" else "DEACTIVATED",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    // Station Title & Address in bottom overlay
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = station.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = LimeAccent,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = station.address.ifBlank { "Smart Microgrid Regional Node" },
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.9f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            // 2. Operator Control Panel Card (Dedicated, clean alignment)
                            if (isOperator && isAssigned) {
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, if (isLive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth()
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
                                                text = "Station Power Status",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = CharcoalText
                                            )
                                            Text(
                                                text = if (isLive) "Online & generating for transfer dispatch" else "Offline / scheduled maintenance",
                                                fontSize = 12.sp,
                                                color = GrayText
                                            )
                                        }

                                        Spacer(Modifier.width(10.dp))

                                        Button(
                                            onClick = {
                                                if (!isTogglingStatus) {
                                                    isTogglingStatus = true
                                                    viewModel.toggleStationStatus(
                                                        stationId = station.id,
                                                        currentStatus = station.status,
                                                        onSuccess = { newStatus ->
                                                            isTogglingStatus = false
                                                            Toast.makeText(context, "Station status is now $newStatus", Toast.LENGTH_SHORT).show()
                                                        },
                                                        onError = { err ->
                                                            isTogglingStatus = false
                                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                                        }
                                                    )
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isLive) Color(0xFFFEE2E2) else LimeAccent,
                                                contentColor = if (isLive) Color(0xFFDC2626) else CharcoalText
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            if (isTogglingStatus) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp,
                                                    color = if (isLive) Color(0xFFDC2626) else CharcoalText
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = if (isLive) Icons.Default.PowerSettingsNew else Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = if (isLive) "Deactivate" else "Activate",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Operator Chip Info
                            val opDisplay = station.gridOperatorName?.takeIf { it.isNotBlank() } ?: "Daniru (Grid Operator)"
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = CharcoalText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Operator: ",
                                        fontSize = 12.sp,
                                        color = GrayText,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = opDisplay,
                                        fontSize = 12.sp,
                                        color = CharcoalText,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Time Segmented Control
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    tabs.forEach { tab ->
                                        val isSelected = selectedTab == tab
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) CharcoalText else Color.Transparent,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedTab = tab }
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = tab,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                    color = if (isSelected) LimeAccent else GrayText
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 3 Stat Metric Cards
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                EnhancedStatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Capacity",
                                    value = "${station.capacityKw}",
                                    unit = "kW",
                                    subtitle = "Peak Feed-In",
                                    icon = Icons.Default.ElectricBolt,
                                    iconTint = Color(0xFFD97706),
                                    iconBg = Color(0xFFFEF3C7)
                                )
                                EnhancedStatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Storage",
                                    value = "${station.availableStorageKwh}",
                                    unit = "kWh",
                                    subtitle = "Battery ESS",
                                    icon = Icons.Default.BatteryChargingFull,
                                    iconTint = Color(0xFF15803D),
                                    iconBg = Color(0xFFDCFCE7)
                                )
                                EnhancedStatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Clean Offset",
                                    value = "0.80",
                                    unit = "kg/kWh",
                                    subtitle = "CO₂ Reduction",
                                    icon = Icons.Default.Eco,
                                    iconTint = Color(0xFF15803D),
                                    iconBg = Color(0xFFDCFCE7)
                                )
                            }

                            // Usage Telemetry Chart Card
                            Card(
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Power Flow & Usage",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = CharcoalText
                                            )
                                            Text(
                                                text = "$selectedTab Telemetry Analysis",
                                                fontSize = 11.sp,
                                                color = GrayText
                                            )
                                        }

                                        // Peak indicator
                                        Surface(
                                            color = LimeAccent.copy(alpha = 0.25f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, LimeAccent.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = "Peak: 46.2 kW",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 11.sp,
                                                color = CharcoalText,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Chart Canvas
                                    EnhancedUsageChart(
                                        tab = selectedTab,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Legend
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF84CC16))
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("Solar Generation (kW)", fontSize = 11.sp, color = CharcoalText, fontWeight = FontWeight.Bold)

                                        Spacer(Modifier.width(18.dp))

                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(CharcoalText)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("Grid Consumption (kW)", fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }

                            // Technical Specifications Card
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Microgrid Technical Specifications",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = CharcoalText
                                    )
                                    HorizontalDivider(color = Color(0xFFF1F5F9))

                                    SpecRow(label = "Microgrid Station ID", value = station.id.takeLast(8).uppercase(), icon = Icons.Default.Tag)
                                    SpecRow(label = "Solar Array Architecture", value = "High-Yield Monocrystalline PV", icon = Icons.Default.WbSunny)
                                    SpecRow(label = "Inverter Technology", value = "Bi-Directional Smart Inverter", icon = Icons.Default.SettingsInputComponent)
                                    SpecRow(label = "Operational Hours", value = "06:00 - 18:00 (Daily Dispatch)", icon = Icons.Default.AccessTime)
                                    SpecRow(label = "Transmission Reliability", value = "99.8% Grid Uptime", icon = Icons.Default.VerifiedUser)
                                }
                            }

                            // ENERGY SLOTS & CAPACITY REMAINING BREAKDOWN SECTION
                            EnergySlotsSection(
                                slotsState = slotsState,
                                isOperator = isOperator,
                                slotTab = slotTab,
                                onTabSelected = { slotTab = it },
                                onAddNewSlotClicked = { showAddSlotDialog = true },
                                onToggleSlotStatus = { slot ->
                                    val newStatus = if (slot.status.equals("Available", ignoreCase = true)) "Full" else "Available"
                                    viewModel.updateSlotStatus(
                                        stationId = station.id,
                                        slotId = slot.id,
                                        newStatus = newStatus,
                                        onSuccess = {
                                            Toast.makeText(context, "Slot marked as $newStatus", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { err ->
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                onRestoreSlot = { slot ->
                                    viewModel.updateSlotStatus(
                                        stationId = station.id,
                                        slotId = slot.id,
                                        newStatus = "Available",
                                        onSuccess = {
                                            Toast.makeText(context, "Slot restored to Available", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { err ->
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                onDeleteSlot = { slot ->
                                    slotDeleteTarget = slot
                                },
                                onBookSlotClicked = { slot ->
                                    onViewSlotsClicked(station.id)
                                }
                            )

                            // Location & Live Map Section
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Location & Microgrid Site",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CharcoalText
                                    )

                                    TextButton(
                                        onClick = { showMapDialog = true },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Fullscreen,
                                            contentDescription = null,
                                            tint = CharcoalText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Expand Map",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalText
                                        )
                                    }
                                }

                                // Enhanced Interactive Map Card
                                EnhancedInteractiveMapCard(
                                    station = station,
                                    onClick = { showMapDialog = true },
                                    onNavigateClick = { openGoogleMaps() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(110.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnergySlotsSection(
    slotsState: UiState<List<EnergySlot>>,
    isOperator: Boolean,
    slotTab: String,
    onTabSelected: (String) -> Unit,
    onAddNewSlotClicked: () -> Unit,
    onToggleSlotStatus: (EnergySlot) -> Unit,
    onRestoreSlot: (EnergySlot) -> Unit,
    onDeleteSlot: (EnergySlot) -> Unit,
    onBookSlotClicked: (EnergySlot) -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Energy Transfer Slots",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CharcoalText
                    )
                    Text(
                        text = if (isOperator) "Capacity schedule & prosumer bookings" else "Available time windows & booking limits",
                        fontSize = 11.sp,
                        color = GrayText
                    )
                }
                if (isOperator && slotTab == "active") {
                    Button(
                        onClick = onAddNewSlotClicked,
                        colors = ButtonDefaults.buttonColors(containerColor = LimeAccent, contentColor = CharcoalText),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Slot", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tabs Row (Active vs History) - Case-insensitive filter
            val allSlots = if (slotsState is UiState.Success) (slotsState as UiState.Success).data else emptyList()
            val activeSlots = allSlots.filter { !it.status.equals("Deleted", ignoreCase = true) }
            val deletedSlots = allSlots.filter { it.status.equals("Deleted", ignoreCase = true) }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(3.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (slotTab == "active") CharcoalText else Color.Transparent,
                        modifier = Modifier.weight(1f).clickable { onTabSelected("active") }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Active Slots",
                                fontSize = 12.sp,
                                fontWeight = if (slotTab == "active") FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (slotTab == "active") LimeAccent else GrayText
                            )
                            if (activeSlots.isNotEmpty()) {
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    color = if (slotTab == "active") LimeAccent.copy(alpha = 0.25f) else Color(0xFFE2E8F0),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "${activeSlots.size}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (slotTab == "active") LimeAccent else CharcoalText,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (slotTab == "history") CharcoalText else Color.Transparent,
                        modifier = Modifier.weight(1f).clickable { onTabSelected("history") }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "History",
                                fontSize = 12.sp,
                                fontWeight = if (slotTab == "history") FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (slotTab == "history") LimeAccent else GrayText
                            )
                            if (deletedSlots.isNotEmpty()) {
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    color = if (slotTab == "history") LimeAccent.copy(alpha = 0.25f) else Color(0xFFE2E8F0),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "${deletedSlots.size}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (slotTab == "history") LimeAccent else CharcoalText,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Slots list display
            when (slotsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CharcoalText, strokeWidth = 2.dp)
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = "Failed to load energy slots: ${(slotsState as UiState.Error).message}",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
                is UiState.Success -> {
                    val displayedSlots = if (slotTab == "active") activeSlots else deletedSlots
                    if (displayedSlots.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = GrayText, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = if (slotTab == "active") "No active slots configured for this station." else "No deleted slots in history.",
                                    fontSize = 13.sp,
                                    color = GrayText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            displayedSlots.forEach { slot ->
                                EnergySlotCard(
                                    slot = slot,
                                    isOperator = isOperator,
                                    isHistory = slotTab == "history",
                                    onToggleStatus = { onToggleSlotStatus(slot) },
                                    onRestore = { onRestoreSlot(slot) },
                                    onDelete = { onDeleteSlot(slot) },
                                    onBook = { onBookSlotClicked(slot) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper to format ISO dates (e.g. 2026-10-14T18:30:00Z -> Wed, 14 Oct 2026)
private fun formatDisplayDate(dateStr: String): String {
    return try {
        val clean = dateStr.substringBefore("T")
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = parser.parse(clean)
        if (date != null) {
            SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).format(date)
        } else {
            clean
        }
    } catch (e: Exception) {
        dateStr.substringBefore("T")
    }
}

@Composable
fun EnergySlotCard(
    slot: EnergySlot,
    isOperator: Boolean,
    isHistory: Boolean,
    onToggleStatus: () -> Unit,
    onRestore: () -> Unit = {},
    onDelete: () -> Unit,
    onBook: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Formatted Date and Single-Line Status Badge (Separate row so badge never wraps!)
            val isAvail = slot.status.equals("Available", ignoreCase = true)
            val isFull = slot.status.equals("Full", ignoreCase = true)
            val isDel = slot.status.equals("Deleted", ignoreCase = true)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = CharcoalText,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = formatDisplayDate(slot.date),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    }
                }

                // Status Badge with softWrap = false, maxLines = 1, ensuring no awkward text break
                Surface(
                    color = when {
                        isAvail -> Color(0xFFDCFCE7)
                        isFull -> Color(0xFFFEF3C7)
                        isDel -> Color(0xFFFEE2E2)
                        else -> Color(0xFFF3F4F6)
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        when {
                            isAvail -> Color(0xFF86EFAC)
                            isFull -> Color(0xFFFDE68A)
                            isDel -> Color(0xFFFCA5A5)
                            else -> Color(0xFFE5E7EB)
                        }
                    )
                ) {
                    Text(
                        text = slot.status.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false,
                        color = when {
                            isAvail -> Color(0xFF15803D)
                            isFull -> Color(0xFFB45309)
                            isDel -> Color(0xFFDC2626)
                            else -> GrayText
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Time Window and Slot Identifier
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = CharcoalText,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "${slot.startTime} - ${slot.endTime}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CharcoalText
                        )
                    }
                }

                Text(
                    text = "Slot ID: #${slot.id.takeLast(6).uppercase()}",
                    fontSize = 11.sp,
                    color = GrayText,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Energy & Capacity Breakdown
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Capacity", fontSize = 10.sp, color = GrayText, fontWeight = FontWeight.Medium)
                            Text(
                                "${String.format(Locale.US, "%.1f", slot.effectiveCapacity)} kW",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CharcoalText
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Booked", fontSize = 10.sp, color = GrayText, fontWeight = FontWeight.Medium)
                            Text(
                                "${String.format(Locale.US, "%.1f", slot.bookedCapacity)} kW",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (slot.bookedCapacity > 0) Color(0xFFD97706) else CharcoalText
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Remaining", fontSize = 10.sp, color = GrayText, fontWeight = FontWeight.Medium)
                            Text(
                                "${String.format(Locale.US, "%.1f", slot.remainingCapacity)} kW",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (slot.remainingCapacity > 0) Color(0xFF15803D) else Color(0xFFDC2626)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { (slot.percentRemaining / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (slot.percentRemaining > 30f) LimeAccentDark else Color(0xFFEAB308),
                        trackColor = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${slot.percentRemaining.toInt()}% available for booking",
                            fontSize = 10.sp,
                            color = GrayText
                        )
                        Text(
                            text = "${(100f - slot.percentRemaining).toInt()}% reserved",
                            fontSize = 10.sp,
                            color = GrayText
                        )
                    }
                }
            }

            // Action Buttons Row with high contrast and proper alignment
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isOperator) {
                    if (!isHistory) {
                        OutlinedButton(
                            onClick = onToggleStatus,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CharcoalText
                            )
                        ) {
                            Icon(
                                imageVector = if (slot.status.equals("Available", ignoreCase = true)) Icons.Default.CheckCircleOutline else Icons.Default.Replay,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (slot.status.equals("Available", ignoreCase = true)) "Mark Full" else "Mark Available",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFEE2E2))
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Slot",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = onRestore,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalText, contentColor = LimeAccent),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Restore Slot", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (!isHistory && slot.status.equals("Available", ignoreCase = true)) {
                    Button(
                        onClick = onBook,
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalText, contentColor = LimeAccent),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Book This Slot", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateSlotDialog(
    stationId: String,
    viewModel: MicrogridViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val todayStr = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.format(Date())
    }
    var date by remember { mutableStateOf(todayStr) }
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("09:00") }
    var capacityStr by remember { mutableStateOf("50.0") }
    var isSubmitting by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isSubmitting) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Create Energy Slot",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = CharcoalText
                        )
                        Text(
                            text = "Add availability for prosumer booking",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayText
                        )
                    }
                    IconButton(onClick = onDismiss, enabled = !isSubmitting) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = GrayText)
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = GrayText) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        placeholder = { Text("08:00") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = GrayText) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        placeholder = { Text("09:00") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = GrayText) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = capacityStr,
                    onValueChange = { capacityStr = it },
                    label = { Text("Capacity (kW)") },
                    placeholder = { Text("e.g. 50") },
                    leadingIcon = { Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = GrayText) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold, color = CharcoalText)
                    }
                    Button(
                        onClick = {
                            val cap = capacityStr.toDoubleOrNull()
                            if (cap == null || cap <= 0) {
                                Toast.makeText(context, "Please enter a valid capacity in kW", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (date.isBlank() || startTime.isBlank() || endTime.isBlank()) {
                                Toast.makeText(context, "Please fill in all date and time fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSubmitting = true
                            viewModel.createSlot(
                                stationId = stationId,
                                request = CreateSlotRequest(
                                    date = date.trim(),
                                    startTime = startTime.trim(),
                                    endTime = endTime.trim(),
                                    capacity = cap
                                ),
                                onSuccess = {
                                    isSubmitting = false
                                    Toast.makeText(context, "Energy slot created successfully!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                },
                                onError = { errorMsg ->
                                    isSubmitting = false
                                    Toast.makeText(context, "Failed: $errorMsg", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LimeAccent, contentColor = CharcoalText),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = CharcoalText)
                        } else {
                            Text("Create Slot", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteSlotConfirmDialog(
    slot: EnergySlot,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Energy Slot?", fontWeight = FontWeight.Bold, color = CharcoalText) },
        text = {
            Text(
                "Are you sure you want to remove the slot for ${slot.date} (${slot.startTime} - ${slot.endTime})? It will be moved to History.",
                color = GrayText
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = CharcoalText)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Metric stat card with colored icon background, value, unit, and subtitle
 */
@Composable
private fun EnhancedStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color
) {
    Card(
        modifier = modifier.height(130.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = GrayText,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CharcoalText
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = unit,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = GrayText,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Technical specification row
 */
@Composable
private fun SpecRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GrayText,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = GrayText,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            fontSize = 12.sp,
            color = CharcoalText,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Interactive canvas power usage curve
 */
@Composable
private fun EnhancedUsageChart(
    tab: String,
    modifier: Modifier = Modifier
) {
    val genColor = Color(0xFF84CC16)
    val consColor = CharcoalText.copy(alpha = 0.85f)
    val gridColor = Color(0xFFE5E7EB)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Horizontal grid lines
        val lineCount = 4
        for (i in 0..lineCount) {
            val y = height * (i / lineCount.toFloat())
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.5f
            )
        }

        // Generate curve paths based on tab
        val genPath = Path()
        val genFillPath = Path()
        val consPath = Path()

        val points = when (tab) {
            "Today" -> listOf(0.85f, 0.70f, 0.45f, 0.15f, 0.20f, 0.55f, 0.88f)
            "Weeks" -> listOf(0.60f, 0.40f, 0.25f, 0.35f, 0.20f, 0.50f, 0.30f)
            "Months" -> listOf(0.50f, 0.30f, 0.20f, 0.35f, 0.45f)
            else -> listOf(0.70f, 0.50f, 0.35f, 0.20f, 0.30f, 0.60f)
        }

        val consPoints = when (tab) {
            "Today" -> listOf(0.75f, 0.55f, 0.65f, 0.60f, 0.50f, 0.45f, 0.70f)
            "Weeks" -> listOf(0.55f, 0.50f, 0.45f, 0.40f, 0.45f, 0.40f, 0.48f)
            "Months" -> listOf(0.45f, 0.40f, 0.35f, 0.40f, 0.42f)
            else -> listOf(0.50f, 0.45f, 0.40f, 0.38f, 0.42f, 0.48f)
        }

        val stepX = width / (points.size - 1)

        // Draw Generation Path
        genPath.moveTo(0f, height * points[0])
        genFillPath.moveTo(0f, height)
        genFillPath.lineTo(0f, height * points[0])

        for (i in 0 until points.size - 1) {
            val currentX = i * stepX
            val currentY = height * points[i]
            val nextX = (i + 1) * stepX
            val nextY = height * points[i + 1]

            val cx1 = currentX + (stepX / 2f)
            val cy1 = currentY
            val cx2 = currentX + (stepX / 2f)
            val cy2 = nextY

            genPath.cubicTo(cx1, cy1, cx2, cy2, nextX, nextY)
            genFillPath.cubicTo(cx1, cy1, cx2, cy2, nextX, nextY)
        }

        genFillPath.lineTo(width, height)
        genFillPath.close()

        // Fill gradient
        drawPath(
            path = genFillPath,
            brush = Brush.verticalGradient(
                colors = listOf(genColor.copy(alpha = 0.35f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // Draw Generation Line
        drawPath(
            path = genPath,
            color = genColor,
            style = Stroke(width = 5.5f)
        )

        // Draw Consumption Path
        consPath.moveTo(0f, height * consPoints[0])
        val consStepX = width / (consPoints.size - 1)
        for (i in 0 until consPoints.size - 1) {
            val currentX = i * consStepX
            val currentY = height * consPoints[i]
            val nextX = (i + 1) * consStepX
            val nextY = height * consPoints[i + 1]

            val cx1 = currentX + (consStepX / 2f)
            val cy1 = currentY
            val cx2 = currentX + (consStepX / 2f)
            val cy2 = nextY

            consPath.cubicTo(cx1, cy1, cx2, cy2, nextX, nextY)
        }

        drawPath(
            path = consPath,
            color = consColor,
            style = Stroke(width = 3.5f)
        )

        // Draw peak point
        val peakIndex = points.indices.minByOrNull { points[it] } ?: 0
        val peakX = peakIndex * stepX
        val peakY = height * points[peakIndex]

        drawCircle(
            color = genColor.copy(alpha = 0.3f),
            radius = 16f,
            center = Offset(peakX, peakY)
        )
        drawCircle(
            color = genColor,
            radius = 7f,
            center = Offset(peakX, peakY)
        )
        drawCircle(
            color = Color.White,
            radius = 3.5f,
            center = Offset(peakX, peakY)
        )
    }
}

/**
 * Interactive map card shown directly on the screen
 */
@Composable
private fun EnhancedInteractiveMapCard(
    station: Station,
    onClick: () -> Unit,
    onNavigateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lat = if (station.latitude != 0.0) station.latitude else 5.9549
    val lng = if (station.longitude != 0.0) station.longitude else 80.5550
    val stationLatLng = remember(lat, lng) { LatLng(lat, lng) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(stationLatLng, 14.5f)
    }

    // Disable all gestures on the embedded card so outer scroll is 100% fluid
    val embeddedUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            scrollGesturesEnabled = false,
            zoomGesturesEnabled = false,
            tiltGesturesEnabled = false,
            rotationGesturesEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        )
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Live Embedded Leaflet + OpenStreetMap Map
            LeafletMapView(
                stations = listOf(station),
                selectedStationId = station.id,
                onStationSelected = { onClick() },
                modifier = Modifier.fillMaxSize(),
                initialZoom = 15,
                centerLat = lat,
                centerLng = lng,
                interactive = false
            )

            // Click interceptor overlay so the whole map card triggers the dialog
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onClick() }
            )

            // Top-right "Tap to expand map" badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CharcoalText.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = null,
                        tint = LimeAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "Interactive Map",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Bottom bar with address and Navigate button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(LimeAccent.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = CharcoalText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = station.address,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = String.format(Locale.US, "GPS: %.4f° N, %.4f° E", lat, lng),
                                fontSize = 10.sp,
                                color = GrayText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // Directions Button
                    Button(
                        onClick = onNavigateClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CharcoalText,
                            contentColor = LimeAccent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "Directions",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen Interactive Map Dialog with Zoom, Directions, and Coordinates
 */
@Composable
fun StationMapDialog(
    station: Station,
    onDismiss: () -> Unit,
    onOpenExternalMaps: () -> Unit
) {
    val context = LocalContext.current
    val lat = if (station.latitude != 0.0) station.latitude else 5.9549
    val lng = if (station.longitude != 0.0) station.longitude else 80.5550
    val stationLatLng = remember(lat, lng) { LatLng(lat, lng) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(stationLatLng, 15f)
    }

    val fullUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            scrollGesturesEnabled = true,
            zoomGesturesEnabled = true,
            tiltGesturesEnabled = true,
            rotationGesturesEnabled = true,
            myLocationButtonEnabled = true,
            mapToolbarEnabled = true
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Interactive Leaflet + OpenStreetMap Map
                LeafletMapView(
                    stations = listOf(station),
                    selectedStationId = station.id,
                    onStationSelected = {},
                    modifier = Modifier.fillMaxSize(),
                    initialZoom = 15,
                    centerLat = lat,
                    centerLng = lng,
                    interactive = true
                )

                // Top Header Card with Close Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(LimeAccent.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EvStation,
                                    contentDescription = null,
                                    tint = CharcoalText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = station.name,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = CharcoalText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = station.address,
                                    fontSize = 12.sp,
                                    color = GrayText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = CharcoalText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Bottom Action Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(16.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Location specs row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Microgrid Coordinates",
                                    fontSize = 11.sp,
                                    color = GrayText,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = String.format(Locale.US, "%.5f° N, %.5f° E", lat, lng),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CharcoalText
                                )
                            }

                            // Copy coordinates button
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Coordinates", "$lat,$lng"))
                                    Toast.makeText(context, "Coordinates copied: $lat, $lng", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CharcoalText)
                            ) {
                                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Copy GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Primary "Navigate in Google Maps" Button
                        Button(
                            onClick = onOpenExternalMaps,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CharcoalText,
                                contentColor = LimeAccent
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Directions,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Open in Google Maps (Directions)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
