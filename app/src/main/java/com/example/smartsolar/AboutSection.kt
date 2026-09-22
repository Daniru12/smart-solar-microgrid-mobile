package com.example.smartsolar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.ui.theme.BorderLight
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import com.example.smartsolar.ui.theme.SurfaceLight

@Composable
fun AboutSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp)
    ) {
        // Tagline badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(SurfaceLight, RoundedCornerShape(16.dp))
                .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CharcoalText)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ABOUT US",
                color = CharcoalText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "What is SolarGrid?",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = CharcoalText
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SolarGrid is a Smart Solar Microgrid Trading System designed to simplify the management of solar microgrid stations and energy-transfer reservations.",
            color = GrayText,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "The system allows administrators to manage microgrid stations, Grid Operators to manage operational activities, and Solar Prosumers to find available stations and reserve energy slots using the mobile application.",
            color = GrayText,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
    }
}
