package com.example.smartsolar.features.microgrid.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.microgrid.models.Station
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import com.example.smartsolar.ui.theme.LimeAccentDark
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun GridMapScreen(
    viewModel: MicrogridViewModel,
    stations: List<Station>? = null,
    onStationSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val stationsState by viewModel.stationsState.collectAsState()

    val stationList = remember(stations, stationsState) {
        if (stations != null) {
            stations.filter { it.status.equals("Active", ignoreCase = true) }
        } else if (stationsState is UiState.Success) {
            (stationsState as UiState.Success<List<Station>>).data.filter { it.status.equals("Active", ignoreCase = true) }
        } else {
            emptyList()
        }
    }

    var selectedStationId by remember { mutableStateOf<String?>(null) }
    var useGoogleNativeMap by remember { mutableStateOf(false) }

    val selectedStation = remember(selectedStationId, stationList) {
        stationList.firstOrNull { it.id == selectedStationId }
    }

    // Determine user location from device sensors or default to nearest station / Colombo
    val userLatLng = remember(stationList) {
        var detected: LatLng? = null
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
            val loc = lm?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                ?: lm?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
            if (loc != null) {
                detected = LatLng(loc.latitude, loc.longitude)
            }
        } catch (e: Exception) {}

        detected ?: if (stationList.isNotEmpty()) {
            LatLng(stationList.first().latitude, stationList.first().longitude)
        } else {
            LatLng(6.9271, 79.8612) // Colombo default
        }
    }

    // Identify stations strictly within 10 km of the user location
    val stationsWithin10Km = remember(stationList, userLatLng) {
        stationList.filter { s ->
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                userLatLng.latitude, userLatLng.longitude,
                s.latitude, s.longitude,
                results
            )
            results[0] <= 10000f // 10,000 meters = 10 km
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (stationList.isEmpty() && stationsState is UiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = LimeAccentDark)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loading Microgrid Map...", fontWeight = FontWeight.SemiBold, color = CharcoalText)
                }
            }
        } else if (!useGoogleNativeMap) {
            // Interactive Leaflet Map with My Location & 10 km Circle
            LeafletMapView(
                stations = stationList,
                selectedStationId = selectedStationId,
                onStationSelected = { id ->
                    selectedStationId = id
                },
                userLat = userLatLng.latitude,
                userLng = userLatLng.longitude,
                show10KmCircle = true,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Google Maps Compose with My Location & 10 km Circle
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(userLatLng, 12f)
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // My Location Marker
                Marker(
                    state = MarkerState(position = userLatLng),
                    title = "My Location",
                    snippet = "10 km radius active",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )

                // 10 km Radius Circle
                Circle(
                    center = userLatLng,
                    radius = 10000.0,
                    fillColor = Color(0x223B82F6),
                    strokeColor = Color(0xFF2563EB),
                    strokeWidth = 3f
                )

                stationList.forEach { station ->
                    if (station.latitude != 0.0 && station.longitude != 0.0) {
                        val results = FloatArray(1)
                        android.location.Location.distanceBetween(
                            userLatLng.latitude, userLatLng.longitude,
                            station.latitude, station.longitude,
                            results
                        )
                        val isInside10Km = results[0] <= 10000f
                        val distKm = String.format("%.1f km", results[0] / 1000f)

                        Marker(
                            state = MarkerState(position = LatLng(station.latitude, station.longitude)),
                            title = station.name,
                            snippet = if (isInside10Km) "⚡ Inside 10 km ($distKm) - ${station.capacityKw} kW" else "$distKm - ${station.capacityKw} kW",
                            icon = BitmapDescriptorFactory.defaultMarker(
                                if (isInside10Km) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_YELLOW
                            ),
                            onClick = {
                                selectedStationId = station.id
                                false
                            }
                        )
                    }
                }
            }
        }

        // Top Status Header / Map Controls
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            shadowElevation = 4.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (stationsWithin10Km.isNotEmpty()) Color(0xFF15803D) else Color(0xFF2563EB))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (stationsWithin10Km.isNotEmpty()) {
                                "${stationsWithin10Km.size} in 10 km • ${stationList.size} Total"
                            } else {
                                "${stationList.size} Active Solar Grids"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = CharcoalText
                        )
                        Text(
                            text = "10 km Coverage Circle Active",
                            fontSize = 10.sp,
                            color = GrayText
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Map engine toggle
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { useGoogleNativeMap = !useGoogleNativeMap },
                        color = MaterialTheme.colorScheme.background,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (useGoogleNativeMap) Icons.Default.Layers else Icons.Default.Map,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = CharcoalText
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (useGoogleNativeMap) "Native" else "Live Grid",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { viewModel.loadStations() },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp), tint = CharcoalText)
                    }
                }
            }
        }

        // Bottom Selected Station Floating Preview Card
        AnimatedVisibility(
            visible = selectedStation != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            selectedStation?.let { station ->
                val distanceKm = remember(station, userLatLng) {
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(
                        userLatLng.latitude, userLatLng.longitude,
                        station.latitude, station.longitude,
                        results
                    )
                    results[0] / 1000f
                }
                val isWithin10Km = distanceKm <= 10.0f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isWithin10Km) Color(0xFFDCFCE7) else LimeAccent.copy(alpha = 0.25f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EvStation,
                                        contentDescription = null,
                                        tint = if (isWithin10Km) Color(0xFF15803D) else CharcoalText,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = station.name,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = CharcoalText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (isWithin10Km) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = Color(0xFFDCFCE7),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "< 10 km",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF15803D),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${station.address} • ${String.format("%.1f km away", distanceKm)}",
                                        fontSize = 11.sp,
                                        color = GrayText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(
                                onClick = { selectedStationId = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = GrayText, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Text("Capacity", fontSize = 10.sp, color = GrayText, fontWeight = FontWeight.Medium)
                                    Text("${station.capacityKw} kW", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Text("Storage", fontSize = 10.sp, color = GrayText, fontWeight = FontWeight.Medium)
                                    Text("${station.availableStorageKwh} kWh", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    launchExternalMaps(
                                        context = context,
                                        latitude = station.latitude,
                                        longitude = station.longitude,
                                        label = station.name
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = CharcoalText
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Directions", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { onStationSelected(station.id) },
                                modifier = Modifier.weight(1.3f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CharcoalText,
                                    contentColor = LimeAccent
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Text("View Station", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
