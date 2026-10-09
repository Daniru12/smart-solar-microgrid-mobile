package com.example.smartsolar.features.microgrid.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import com.example.smartsolar.features.microgrid.models.Station
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import com.example.smartsolar.ui.theme.LimeAccentDark
import com.google.android.gms.maps.model.LatLng

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
    var liveUserLocation by remember { mutableStateOf<LatLng?>(null) }
    var recenterCounter by remember { mutableStateOf(0) }

    // Request runtime location permissions to acquire true device GPS coordinates
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            queryLiveLocation(context) { loc -> liveUserLocation = loc }
        }
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) {
            queryLiveLocation(context) { loc -> liveUserLocation = loc }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val selectedStation = remember(selectedStationId, stationList) {
        stationList.firstOrNull { it.id == selectedStationId }
    }

    // Identify stations strictly within 10 km of the user's real GPS location
    val stationsWithin10Km = remember(stationList, liveUserLocation) {
        val uLoc = liveUserLocation
        if (uLoc == null) {
            emptyList()
        } else {
            stationList.filter { s ->
                val results = FloatArray(1)
                android.location.Location.distanceBetween(
                    uLoc.latitude, uLoc.longitude,
                    s.latitude, s.longitude,
                    results
                )
                results[0] <= 10000f // 10,000 meters = 10 km
            }
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
        } else {
            // Interactive Google Maps with Live GPS Location & 10 km Circle
            LeafletMapView(
                stations = stationList,
                selectedStationId = selectedStationId,
                onStationSelected = { id ->
                    selectedStationId = id
                },
                userLat = liveUserLocation?.latitude,
                userLng = liveUserLocation?.longitude,
                recenterTrigger = recenterCounter,
                onUserLocationDetected = { lat, lng ->
                    liveUserLocation = LatLng(lat, lng)
                },
                show10KmCircle = true,
                modifier = Modifier.fillMaxSize()
            )
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
                            .background(
                                if (liveUserLocation != null) {
                                    if (stationsWithin10Km.isNotEmpty()) Color(0xFF15803D) else Color(0xFF2563EB)
                                } else {
                                    Color(0xFFEAB308) // Amber while acquiring GPS
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (liveUserLocation != null) {
                                if (stationsWithin10Km.isNotEmpty()) {
                                    "${stationsWithin10Km.size} in 10 km • ${stationList.size} Total"
                                } else {
                                    "0 in 10 km • ${stationList.size} Total"
                                }
                            } else {
                                "Acquiring GPS • ${stationList.size} Stations"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = CharcoalText
                        )
                        Text(
                            text = if (liveUserLocation != null) {
                                "Live GPS Active • 10 km Circle"
                            } else {
                                "Tap 🎯 to center on your location"
                            },
                            fontSize = 10.sp,
                            color = GrayText
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Re-center GPS button
                    IconButton(
                        onClick = {
                            recenterCounter++
                            queryLiveLocation(context) { loc -> liveUserLocation = loc }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "My Location",
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF2563EB)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { viewModel.loadStations() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(18.dp), tint = CharcoalText)
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
                val distanceKm = remember(station, liveUserLocation) {
                    val uLoc = liveUserLocation
                    if (uLoc != null) {
                        val results = FloatArray(1)
                        android.location.Location.distanceBetween(
                            uLoc.latitude, uLoc.longitude,
                            station.latitude, station.longitude,
                            results
                        )
                        results[0] / 1000f
                    } else {
                        null
                    }
                }
                val isWithin10Km = distanceKm != null && distanceKm <= 10.0f

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
                                        text = if (distanceKm != null) "${station.address} • ${String.format("%.1f km away", distanceKm)}" else station.address,
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

/**
 * Queries the device's live GPS/Network location provider for current coordinates.
 */
private fun queryLiveLocation(context: Context, onLocation: (LatLng) -> Unit) {
    try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
        val providers = lm.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val loc = lm.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                bestLocation = loc
            }
        }
        if (bestLocation != null) {
            onLocation(LatLng(bestLocation.latitude, bestLocation.longitude))
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                onLocation(LatLng(loc.latitude, loc.longitude))
                try {
                    lm.removeUpdates(this)
                } catch (e: Exception) {}
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
            override fun onProviderEnabled(p: String) {}
            override fun onProviderDisabled(p: String) {}
        }

        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 1f, listener)
        }
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, listener)
        }
    } catch (e: SecurityException) {
        // Handled if permission not yet accepted
    } catch (e: Exception) {}
}
