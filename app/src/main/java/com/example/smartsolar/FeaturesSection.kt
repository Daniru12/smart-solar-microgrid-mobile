package com.example.smartsolar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.ui.theme.GlassBorder
import com.example.smartsolar.ui.theme.GlassPanel
import com.example.smartsolar.ui.theme.Teal400

@Composable
fun FeaturesSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp)
    ) {
        Text(
            text = "Why Choose SolarGrid",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Enterprise-grade tools for modern energy trading.",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Feature Cards
        FeatureCard(
            title = "Real-Time Tracking",
            description = "Monitor energy flow and storage levels across the microgrid with millisecond precision.",
            icon = Icons.Default.Timeline
        )
        Spacer(modifier = Modifier.height(16.dp))
        FeatureCard(
            title = "Secure Transfers",
            description = "End-to-end encryption ensures your grid data and slot reservations are completely protected.",
            icon = Icons.Default.Security
        )
        Spacer(modifier = Modifier.height(16.dp))
        FeatureCard(
            title = "Smart Allocation",
            description = "Automated routing algorithms prevent grid overload and optimize energy distribution.",
            icon = Icons.Default.ElectricBolt
        )
    }
}

@Composable
fun FeatureCard(title: String, description: String, icon: ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(GlassPanel, Color.Transparent)
                )
            )
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Teal400.copy(alpha = 0.2f)),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Teal400,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}
