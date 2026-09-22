package com.example.smartsolar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.ui.theme.BorderLight
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import com.example.smartsolar.ui.theme.SurfaceLight

@Composable
fun PlatformSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp)
    ) {
        // Block 1: Microgrid Station Section
        PlatformBlock(
            icon = Icons.Default.BatteryChargingFull,
            title = "Manage Solar Energy Infrastructure Efficiently",
            description = "Each microgrid station contains important operational information that helps users identify suitable stations and available energy-transfer opportunities.",
            items = listOf("Station name", "Location", "Energy capacity", "Battery storage")
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Block 2: Energy Slot Section
        PlatformBlock(
            icon = Icons.Default.DateRange,
            title = "Simple Energy Slot Management",
            description = "Microgrid stations can provide multiple energy-transfer slots throughout the day. This provides an organized method for handling energy-transfer activities.",
            items = listOf("Available dates", "Start & end times", "Reservation status")
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Block 3: Connected System Section
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(SurfaceLight, RoundedCornerShape(16.dp))
                    .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Monitor, contentDescription = null, tint = LimeAccent, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "One Platform. Multiple Applications.",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalText,
                lineHeight = 34.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            AppCard("Web Application", "Used by Backoffice staff and Grid Operators to manage users, stations, slots, and reservations.", true)
            Spacer(modifier = Modifier.height(16.dp))
            AppCard("Mobile Application", "Used by Solar Prosumers and Grid Operators for station access, reservations, QR verification.", true)
            Spacer(modifier = Modifier.height(16.dp))
            AppCard("Central Web Service", "Connects the applications and manages the system's main business logic and data.", false)
        }
    }
}

@Composable
fun PlatformBlock(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String, items: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(SurfaceLight, RoundedCornerShape(16.dp))
                .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = LimeAccent, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = CharcoalText,
            lineHeight = 34.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = description,
            color = GrayText,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(LimeAccent))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = item, color = CharcoalText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun AppCard(title: String, description: String, isLight: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isLight) SurfaceLight else CharcoalText)
            .border(1.dp, if (isLight) BorderLight else CharcoalText, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Text(
            text = title,
            color = if (isLight) CharcoalText else SurfaceLight,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = description,
            color = if (isLight) GrayText else Color.LightGray,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}
