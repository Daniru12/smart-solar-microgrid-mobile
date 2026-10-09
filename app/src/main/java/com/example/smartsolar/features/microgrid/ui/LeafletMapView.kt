package com.example.smartsolar.features.microgrid.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.smartsolar.features.microgrid.models.Station
import org.json.JSONArray
import org.json.JSONObject

class LeafletBridge(private val onStationSelected: (String) -> Unit) {
    @JavascriptInterface
    fun selectStation(stationId: String) {
        onStationSelected(stationId)
    }
}

/**
 * Official Google Maps interactive engine powered directly by the Google Maps API Key:
 * AIzaSyDs5GpCSY7nPa2nS5Sm5099_JkKe-oauR0
 * Displays official Google Maps tiles, 10 km radius circle, My Location, and nearest stations.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LeafletMapView(
    stations: List<Station>,
    selectedStationId: String? = null,
    onStationSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialZoom: Int = 12,
    centerLat: Double? = null,
    centerLng: Double? = null,
    userLat: Double? = null,
    userLng: Double? = null,
    show10KmCircle: Boolean = true,
    interactive: Boolean = true
) {
    val htmlContent = remember(stations, centerLat, centerLng, userLat, userLng, show10KmCircle) {
        generateGoogleMapsHtml(
            stations = stations,
            centerLat = centerLat,
            centerLng = centerLng,
            userLat = userLat,
            userLng = userLng,
            show10KmCircle = show10KmCircle,
            initialZoom = initialZoom,
            interactive = interactive
        )
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(AndroidColor.parseColor("#F1F5F9"))
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                settings.builtInZoomControls = false
                settings.displayZoomControls = false
                webViewClient = WebViewClient()
                addJavascriptInterface(LeafletBridge(onStationSelected), "AndroidBridge")
                loadDataWithBaseURL("https://maps.googleapis.com", htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            if (!selectedStationId.isNullOrBlank()) {
                webView.evaluateJavascript("if (window.selectMarkerById) { window.selectMarkerById('$selectedStationId'); }", null)
            }
        },
        modifier = modifier
    )
}

/**
 * Builds self-contained Google Maps JavaScript API HTML using key AIzaSyDs5GpCSY7nPa2nS5Sm5099_JkKe-oauR0
 */
private fun generateGoogleMapsHtml(
    stations: List<Station>,
    centerLat: Double?,
    centerLng: Double?,
    userLat: Double?,
    userLng: Double?,
    show10KmCircle: Boolean,
    initialZoom: Int,
    interactive: Boolean
): String {
    val validStations = stations.filter { it.latitude != 0.0 && it.longitude != 0.0 }

    val defaultLat = userLat ?: centerLat ?: validStations.firstOrNull()?.latitude ?: 6.9271
    val defaultLng = userLng ?: centerLng ?: validStations.firstOrNull()?.longitude ?: 79.8612

    val stationsJsonArray = JSONArray()
    for (s in validStations) {
        val obj = JSONObject()
        obj.put("id", s.id)
        obj.put("name", s.name)
        obj.put("address", s.address)
        obj.put("latitude", s.latitude)
        obj.put("longitude", s.longitude)
        obj.put("capacityKw", s.capacityKw)
        obj.put("availableStorageKwh", s.availableStorageKwh)
        obj.put("status", s.status)
        stationsJsonArray.put(obj)
    }

    val stationsJson = stationsJsonArray.toString()
    val userLatStr = userLat?.toString() ?: "null"
    val userLngStr = userLng?.toString() ?: "null"

    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
    <style>
        html, body, #map {
            width: 100%;
            height: 100%;
            margin: 0;
            padding: 0;
            background-color: #f1f5f9;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            overflow: hidden;
        }
        .popup-content {
            padding: 4px;
            min-width: 170px;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
        }
        .popup-title {
            font-size: 14px;
            font-weight: 800;
            color: #0f172a;
            margin: 0 0 3px 0;
        }
        .popup-address {
            font-size: 11px;
            color: #64748b;
            margin: 0 0 8px 0;
            line-height: 1.3;
        }
        .nearby-badge {
            display: inline-block;
            background: #dcfce7;
            color: #15803d;
            font-size: 10px;
            font-weight: 700;
            padding: 2px 7px;
            border-radius: 6px;
            margin-bottom: 8px;
        }
        .popup-stats {
            display: flex;
            gap: 6px;
            margin-bottom: 10px;
        }
        .stat-badge {
            background: #f1f5f9;
            color: #0f172a;
            font-size: 10px;
            font-weight: 700;
            padding: 3px 8px;
            border-radius: 6px;
            border: 1px solid #e2e8f0;
        }
        .popup-btn {
            display: block;
            width: 100%;
            background: #0f172a;
            color: #bef264;
            font-size: 11px;
            font-weight: 800;
            text-align: center;
            padding: 7px 0;
            border-radius: 8px;
            text-decoration: none;
            cursor: pointer;
            box-sizing: border-box;
            border: none;
        }
    </style>
</head>
<body>
    <div id="map"></div>
    <script>
        var stations = $stationsJson;
        var userLat = $userLatStr;
        var userLng = $userLngStr;
        var defaultLat = $defaultLat;
        var defaultLng = $defaultLng;
        var defaultZoom = $initialZoom;
        var show10Km = $show10KmCircle;

        var map;
        var markersMap = {};
        var currentInfoWindow = null;

        function initMap() {
            var centerPos = { lat: defaultLat, lng: defaultLng };

            map = new google.maps.Map(document.getElementById("map"), {
                center: centerPos,
                zoom: defaultZoom,
                disableDefaultUI: true,
                zoomControl: $interactive,
                gestureHandling: "$interactive" === "true" ? "greedy" : "none",
                styles: [
                    { featureType: "poi", elementType: "labels", stylers: [{ visibility: "off" }] }
                ]
            });

            // 10 km Radius Circle and My Location Marker
            if (userLat !== null && userLng !== null) {
                var userPos = { lat: userLat, lng: userLng };

                if (show10Km) {
                    new google.maps.Circle({
                        strokeColor: "#2563EB",
                        strokeOpacity: 0.85,
                        strokeWeight: 2,
                        fillColor: "#3B82F6",
                        fillOpacity: 0.12,
                        map: map,
                        center: userPos,
                        radius: 10000 // 10,000 meters = 10 km
                    });
                }

                var userPinSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="36" height="48" viewBox="0 0 36 48">' +
                    '<path d="M18 0C8.06 0 0 8.06 0 18c0 13.5 18 30 18 30s18-16.5 18-30C36 8.06 27.94 0 18 0z" fill="#2563EB"/>' +
                    '<circle cx="18" cy="18" r="7.5" fill="#FFFFFF"/>' +
                    '<circle cx="18" cy="18" r="4" fill="#2563EB"/>' +
                '</svg>';

                var userMarker = new google.maps.Marker({
                    position: userPos,
                    map: map,
                    title: "My Location",
                    icon: {
                        url: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(userPinSvg),
                        scaledSize: new google.maps.Size(34, 45),
                        anchor: new google.maps.Point(17, 45)
                    },
                    zIndex: 999
                });

                var userInfowindow = new google.maps.InfoWindow({
                    content: '<div style="color:#0f172a;font-weight:bold;font-size:12px;padding:4px;">📍 My Location<br/><span style="color:#64748b;font-weight:normal;font-size:10px;">10 km station range active</span></div>'
                });

                userMarker.addListener("click", function() {
                    if (currentInfoWindow) currentInfoWindow.close();
                    currentInfoWindow = userInfowindow;
                    userInfowindow.open(map, userMarker);
                });
            }

            var bounds = new google.maps.LatLngBounds();
            if (userLat !== null && userLng !== null) {
                bounds.extend(new google.maps.LatLng(userLat, userLng));
            }

            stations.forEach(function(station) {
                var pos = { lat: station.latitude, lng: station.longitude };
                bounds.extend(pos);

                var distKm = null;
                var isNearby = false;
                if (userLat !== null && userLng !== null) {
                    var d = computeDistanceKm(userLat, userLng, station.latitude, station.longitude);
                    distKm = d.toFixed(1);
                    isNearby = (d <= 10.0);
                }

                var badgeColor = isNearby ? '#10B981' : '#84CC16';
                var stationSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="40" height="52" viewBox="0 0 40 52">' +
                    '<path d="M20 0C8.95 0 0 8.95 0 20c0 15 20 32 20 32s20-17 20-32C40 8.95 31.05 0 20 0z" fill="#0F172A"/>' +
                    '<circle cx="20" cy="19" r="13" fill="' + badgeColor + '"/>' +
                    '<path d="M21.5 8.5L13 21h7L18.5 30l9.5-13.5h-7.5l2.5-8z" fill="#0F172A"/>' +
                '</svg>';

                var marker = new google.maps.Marker({
                    position: pos,
                    map: map,
                    title: station.name,
                    icon: {
                        url: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(stationSvg),
                        scaledSize: new google.maps.Size(38, 49),
                        anchor: new google.maps.Point(19, 49)
                    }
                });

                markersMap[station.id] = marker;

                var nearbyHtml = isNearby 
                    ? '<div class="nearby-badge">⚡ ' + distKm + ' km away • Inside 10 km zone</div>' 
                    : (distKm !== null ? '<div style="font-size:10px; color:#64748b; margin-bottom:6px;">📍 ' + distKm + ' km away</div>' : '');

                var popupHtml = '<div class="popup-content">' +
                    '<div class="popup-title">' + station.name + '</div>' +
                    '<div class="popup-address">' + station.address + '</div>' +
                    nearbyHtml +
                    '<div class="popup-stats">' +
                        '<span class="stat-badge">' + station.capacityKw + ' kW</span>' +
                        '<span class="stat-badge">' + station.availableStorageKwh + ' kWh</span>' +
                    '</div>' +
                    '<button class="popup-btn" onclick="triggerSelect(\'' + station.id + '\')">Select Station</button>' +
                '</div>';

                var infowindow = new google.maps.InfoWindow({
                    content: popupHtml
                });

                marker.addListener("click", function() {
                    if (currentInfoWindow) currentInfoWindow.close();
                    currentInfoWindow = infowindow;
                    infowindow.open(map, marker);
                    triggerSelect(station.id);
                });
            });

            if (stations.length > 1 && !userLat) {
                map.fitBounds(bounds);
            }
        }

        function computeDistanceKm(lat1, lon1, lat2, lon2) {
            var R = 6371;
            var dLat = (lat2 - lat1) * Math.PI / 180;
            var dLon = (lon2 - lon1) * Math.PI / 180;
            var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                    Math.sin(dLon/2) * Math.sin(dLon/2);
            var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            return R * c;
        }

        function triggerSelect(stationId) {
            if (window.AndroidBridge && window.AndroidBridge.selectStation) {
                window.AndroidBridge.selectStation(stationId);
            }
        }

        window.selectMarkerById = function(stationId) {
            var marker = markersMap[stationId];
            if (marker && map) {
                map.panTo(marker.getPosition());
                google.maps.event.trigger(marker, 'click');
            }
        };

        window.centerOnUserLocation = function() {
            if (map && userLat !== null && userLng !== null) {
                map.panTo({ lat: userLat, lng: userLng });
                map.setZoom(12);
            }
        };
    </script>
    <script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyDs5GpCSY7nPa2nS5Sm5099_JkKe-oauR0&callback=initMap" async defer></script>
</body>
</html>
    """.trimIndent()
}

/**
 * Launches external Google Maps navigation for the given coordinates.
 */
fun launchExternalMaps(context: Context, latitude: Double, longitude: Double, label: String = "Microgrid Station") {
    try {
        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}
