package com.example.smartsolar.features.microgrid.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.microgrid.models.Station
import com.example.smartsolar.ui.theme.BackgroundLight
import com.example.smartsolar.ui.theme.SurfaceLight
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import com.example.smartsolar.ui.theme.SurfaceLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDetailsScreen(
    stationId: String,
    viewModel: MicrogridViewModel,
    onViewSlotsClicked: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.selectedStationState.collectAsState()
    var selectedTab by remember { mutableStateOf("Overview") }
    val tabs = listOf("Today", "Weeks", "Months", "Years")

    LaunchedEffect(stationId) {
        viewModel.loadStationDetails(stationId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Overview", fontWeight = FontWeight.Bold, color = CharcoalText, fontSize = 18.sp, modifier = Modifier.padding(end = 48.dp))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CharcoalText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        containerColor = BackgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            when (state) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LimeAccent)
                    }
                }
                is UiState.Error -> {
                    val error = (state as UiState.Error).message
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }
                }
                is UiState.Success -> {
                    val station = (state as UiState.Success).data
                    
                    // Header Name
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = CharcoalText,
                        modifier = Modifier.padding(horizontal = 24.dp).padding(top = 8.dp, bottom = 4.dp)
                    )

                    // Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        tabs.forEach { tab ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = tab,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == tab) CharcoalText else GrayText,
                                    modifier = Modifier.clickable { selectedTab = tab }
                                )
                                if (selectedTab == tab) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(modifier = Modifier.size(4.dp).clip(RoundedCornerShape(2.dp)).background(CharcoalText))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Row 1
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Capacity",
                            value = "${station.capacityKw}",
                            subtitle = "kW, total",
                            icon = Icons.Default.EvStation,
                            iconColor = MaterialTheme.colorScheme.primary
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Storage",
                            value = "${station.availableStorageKwh}",
                            subtitle = "kWh, current",
                            icon = Icons.Default.BatteryChargingFull,
                            iconColor = LimeAccent
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Status",
                            value = station.status,
                            subtitle = "live status",
                            icon = Icons.Default.Timeline,
                            iconColor = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Chart Section
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Usage Over Time",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(LimeAccent))
                            Text("This week", fontSize = 11.sp, color = GrayText)
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.primary))
                            Text("Last week", fontSize = 11.sp, color = GrayText)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Dummy Line Chart
                    DummyLineChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Map and Address Section
                    Text(
                        text = "Location",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText,
                        modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp)
                    )

                    DummyMapCard(
                        address = station.address,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(140.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Book Button
                    Button(
                        onClick = { onViewSlotsClicked(station.id) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalText, contentColor = LimeAccent),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Book Now (View Slots)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Column {
                Text(title, fontSize = 11.sp, color = CharcoalText, fontWeight = FontWeight.Bold)
                Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
                Text(subtitle, fontSize = 9.sp, color = GrayText)
            }
        }
    }
}

@Composable
fun DummyLineChart(modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = LimeAccent
    val gridColor = Color.LightGray.copy(alpha = 0.3f)
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Draw Grid Lines
        for (i in 0..4) {
            val y = height * (i / 4f)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 2f
            )
        }
        
        // Draw vertical marker
        val markerX = width * 0.55f
        drawLine(
            color = accentColor.copy(alpha = 0.6f),
            start = Offset(markerX, 0f),
            end = Offset(markerX, height),
            strokeWidth = 6f
        )

        // Draw Line 1 (Accent)
        val path1 = Path().apply {
            moveTo(0f, height * 0.8f)
            cubicTo(width * 0.2f, height * 0.5f, width * 0.3f, height * 0.9f, width * 0.5f, height * 0.4f)
            cubicTo(width * 0.7f, height * 0.1f, width * 0.8f, height * 0.6f, width, height * 0.3f)
        }
        drawPath(path1, color = accentColor, style = Stroke(width = 6f))

        // Draw Line 2 (Primary)
        val path2 = Path().apply {
            moveTo(0f, height * 0.9f)
            cubicTo(width * 0.2f, height * 0.7f, width * 0.3f, height * 0.8f, width * 0.5f, height * 0.6f)
            cubicTo(width * 0.7f, height * 0.3f, width * 0.8f, height * 0.7f, width, height * 0.5f)
        }
        drawPath(path2, color = primaryColor, style = Stroke(width = 8f))
        
        // Draw circles at data points on vertical marker
        drawCircle(color = accentColor, radius = 10f, center = Offset(markerX, height * 0.38f))
        drawCircle(color = Color.White, radius = 6f, center = Offset(markerX, height * 0.38f))
        
        drawCircle(color = primaryColor, radius = 10f, center = Offset(markerX, height * 0.58f))
        drawCircle(color = Color.White, radius = 6f, center = Offset(markerX, height * 0.58f))
    }
}

@Composable
fun DummyMapCard(address: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E272E)), // Dark Map base color
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Draw some simulated 3D glowing roads
                val roadColor = LimeAccent.copy(alpha = 0.3f)
                val roadGlow = LimeAccent.copy(alpha = 0.1f)
                
                // Horizontal road glow
                drawLine(
                    color = roadGlow,
                    start = Offset(0f, height * 0.62f),
                    end = Offset(width, height * 0.52f),
                    strokeWidth = 24f
                )
                // Horizontal road core
                drawLine(
                    color = roadColor,
                    start = Offset(0f, height * 0.6f),
                    end = Offset(width, height * 0.5f),
                    strokeWidth = 10f
                )
                
                // Vertical road glow
                drawLine(
                    color = roadGlow,
                    start = Offset(width * 0.37f, 0f),
                    end = Offset(width * 0.43f, height),
                    strokeWidth = 26f
                )
                // Vertical road core
                drawLine(
                    color = roadColor,
                    start = Offset(width * 0.35f, 0f),
                    end = Offset(width * 0.45f, height),
                    strokeWidth = 12f
                )
                
                // Minor road
                drawLine(
                    color = roadColor,
                    start = Offset(width * 0.4f, height * 0.2f),
                    end = Offset(width, height * 0.3f),
                    strokeWidth = 6f
                )
            }

            // Location Pin Icon
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Pin",
                tint = LimeAccent,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = (-10).dp, y = (-15).dp)
                    .size(36.dp)
            )

            // Address overlay at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(SurfaceLight.copy(alpha = 0.9f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = address,
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalText,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        }
    }
}
