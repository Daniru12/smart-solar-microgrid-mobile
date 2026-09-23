package com.example.smartsolar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.ui.theme.BorderLight
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import com.example.smartsolar.ui.theme.SurfaceLight

@Composable
fun EnergyFlowSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLight)
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tagline badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(SurfaceLight, RoundedCornerShape(16.dp))
                .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "ENERGY FLOW",
                color = CharcoalText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Energy Flow Visualization",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = CharcoalText,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Track the seamless transfer of renewable energy from generation to storage.",
            color = GrayText,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Flow Steps
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FlowStepItem(Icons.Default.WbSunny, "Solar Panel")
            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = LimeAccent, modifier = Modifier.size(32.dp))
            FlowStepItem(Icons.Default.ElectricBolt, "Energy Flow")
            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = LimeAccent, modifier = Modifier.size(32.dp))
            FlowStepItem(Icons.Default.ElectricBolt, "Microgrid Station")
            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = LimeAccent, modifier = Modifier.size(32.dp))
            FlowStepItem(Icons.Default.BatteryFull, "Battery Storage")
        }
    }
}

@Composable
fun FlowStepItem(icon: androidx.compose.ui.graphics.vector.ImageVector, name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(LimeAccent.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                .border(1.dp, LimeAccent.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = name, tint = CharcoalText, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = name,
            color = CharcoalText,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}
