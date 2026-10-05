package com.example.smartsolar.features.microgrid.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.microgrid.models.Station
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import com.example.smartsolar.ui.theme.LimeAccentDark
import com.example.smartsolar.ui.theme.SurfaceLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationListScreen(
    viewModel: MicrogridViewModel,
    onStationSelected: (String) -> Unit
) {
    val state by viewModel.stationsState.collectAsState()
    var isMapView by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) {
        viewModel.loadStations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Microgrid Stations", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = CharcoalText)
                        Text("Active Fleet & Energy Transfer Hubs", fontSize = 11.sp, color = GrayText)
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (!isMapView) CharcoalText else Color.Transparent,
                                modifier = Modifier.clickable { isMapView = false }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.List,
                                        contentDescription = "List View",
                                        tint = if (!isMapView) LimeAccent else GrayText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "List",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isMapView) Color.White else GrayText
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isMapView) CharcoalText else Color.Transparent,
                                modifier = Modifier.clickable { isMapView = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Map,
                                        contentDescription = "Map View",
                                        tint = if (isMapView) LimeAccent else GrayText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Map",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMapView) Color.White else GrayText
                                    )
                                }
                            }
                        }
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
        ) {
            if (isMapView) {
                Box(modifier = Modifier.fillMaxSize()) {
                    GridMapScreen(viewModel = viewModel, onStationSelected = onStationSelected)
                }
            } else {
                when (state) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is UiState.Error -> {
                        val error = (state as UiState.Error).message
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                                Text(error, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.loadStations() },
                                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalText, contentColor = LimeAccent),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Retry Connection", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    is UiState.Success -> {
                        val stations = (state as UiState.Success).data

                        val filteredStations = stations.filter { s ->
                            val matchesSearch = s.name.contains(searchQuery, ignoreCase = true) ||
                                    s.address.contains(searchQuery, ignoreCase = true)
                            val matchesFilter = when (selectedFilter) {
                                "Active" -> s.status.equals("Active", ignoreCase = true)
                                "High Power" -> s.capacityKw >= 40.0
                                else -> true
                            }
                            matchesSearch && matchesFilter
                        }

                        val totalCapacity = stations.sumOf { it.capacityKw }
                        val totalStorage = stations.sumOf { it.availableStorageKwh }
                        val activeCount = stations.count { it.status.equals("Active", ignoreCase = true) }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Fleet Telemetry Summary Banner
                            item {
                                FleetSummaryCard(
                                    activeCount = activeCount,
                                    totalStations = stations.size,
                                    totalCapacityKw = totalCapacity,
                                    totalStorageKwh = totalStorage
                                )
                            }

                            // Search and Filter Bar
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        placeholder = { Text("Search by station name or location...", fontSize = 13.sp, color = GrayText) },
                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GrayText) },
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(onClick = { searchQuery = "" }) {
                                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = GrayText)
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                                            focusedBorderColor = CharcoalText
                                        )
                                    )

                                    // Filter chips
                                    val filterChips = listOf("All", "Active", "High Power")
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        filterChips.forEach { chip ->
                                            val isSelected = selectedFilter == chip
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = if (isSelected) CharcoalText else MaterialTheme.colorScheme.surface,
                                                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant) else null,
                                                modifier = Modifier.clickable { selectedFilter = chip }
                                            ) {
                                                Text(
                                                    text = chip,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else CharcoalText,
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (filteredStations.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.EvStation, contentDescription = null, tint = GrayText, modifier = Modifier.size(48.dp))
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text("No stations match your filter.", color = GrayText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            } else {
                                items(filteredStations, key = { it.id }) { station ->
                                    EnhancedStationCard(
                                        station = station,
                                        onViewDetails = { onStationSelected(station.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FleetSummaryCard(
    activeCount: Int,
    totalStations: Int,
    totalCapacityKw: Int,
    totalStorageKwh: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Grid Fleet Overview", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CharcoalText)
                }
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "$activeCount/$totalStations Online",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF15803D),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Fleet Capacity", fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium)
                    Text("$totalCapacityKw kW", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
                }
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                Column {
                    Text("Reserve Storage", fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium)
                    Text("$totalStorageKwh kWh", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
                }
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                Column(horizontalAlignment = Alignment.End) {
                    Text("Grid Protocol", fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium)
                    Text("IEEE 2030.5", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LimeAccentDark)
                }
            }
        }
    }
}

@Composable
fun EnhancedStationCard(
    station: Station,
    onViewDetails: () -> Unit
) {
    val isOnline = station.status.equals("Active", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp))
            .clickable { onViewDetails() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Row: Icon + Name + Online Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CharcoalText),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.EvStation,
                            contentDescription = null,
                            tint = LimeAccent,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = station.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = CharcoalText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = GrayText, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = station.address.ifBlank { "Microgrid Regional Sector" },
                                fontSize = 12.sp,
                                color = GrayText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isOnline) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) Color(0xFF15803D) else GrayText)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isOnline) "ONLINE" else "OFFLINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isOnline) Color(0xFF15803D) else GrayText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Specs Grid (Capacity, Storage, Slots)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Node Capacity", fontSize = 10.sp, color = GrayText, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = CharcoalText, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${station.capacityKw} kW", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                        }
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                    Column {
                        Text("Reserve Storage", fontSize = 10.sp, color = GrayText, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${station.availableStorageKwh} kWh", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                        }
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Transfer Ports", fontSize = 10.sp, color = GrayText, fontWeight = FontWeight.Medium)
                        Text("4 Slots Ready", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onViewDetails,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalText, contentColor = LimeAccent)
                ) {
                    Text("View Station & Book Slot", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
