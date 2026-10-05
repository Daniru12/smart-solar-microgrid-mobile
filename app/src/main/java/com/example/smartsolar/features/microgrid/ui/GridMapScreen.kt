package com.example.smartsolar.features.microgrid.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.compose.ui.graphics.Color

@Composable
fun GridMapScreen(viewModel: MicrogridViewModel, onStationSelected: (String) -> Unit) {
    val stationsState by viewModel.stationsState.collectAsState()
    val stations = if (stationsState is UiState.Success) {
        (stationsState as UiState.Success).data.filter { it.status == "Active" }
    } else emptyList()

    val myLocation = LatLng(6.9271, 79.8612)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(myLocation, 12f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {

        Marker(
            state = MarkerState(position = myLocation),
            title = "My Location",
            snippet = "You are here",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        )

        Circle(
            center = myLocation,
            radius = 5000.0,
            fillColor = Color(0x334285F4),
            strokeColor = Color(0xFF4285F4),
            strokeWidth = 4f
        )

        stations.forEach { station ->
            Marker(
                state = MarkerState(position = LatLng(station.latitude, station.longitude)),
                title = station.name,
                snippet = "Capacity: ${station.capacityKw} kW - Storage: ${station.availableStorageKwh} kWh",
                onInfoWindowClick = {
                    onStationSelected(station.id)
                }
            )
        }
    }
}
