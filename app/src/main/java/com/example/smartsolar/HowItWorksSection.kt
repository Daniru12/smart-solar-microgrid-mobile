package com.example.smartsolar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent

@Composable
fun HowItWorksSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp)
    ) {
        Text(
            text = "How It Works",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = CharcoalText
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        StepItem(
            stepNumber = "1",
            title = "Connect System",
            description = "Link your solar panels and battery storage to the microgrid network."
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        StepItem(
            stepNumber = "2",
            title = "Reserve Slots",
            description = "Schedule exact time slots for energy transfer to avoid grid congestion."
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        StepItem(
            stepNumber = "3",
            title = "Monitor Flow",
            description = "Watch real-time energy flow analytics and grid health metrics."
        )
    }
}

@Composable
fun StepItem(stepNumber: String, title: String, description: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(LimeAccent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = CharcoalText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                color = CharcoalText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = GrayText,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}
