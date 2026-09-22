package com.example.smartsolar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun UserRolesSection() {
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
                text = "USER ROLES",
                color = CharcoalText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Built for every member of the energy ecosystem",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = CharcoalText,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        UserRoleCard(
            icon = Icons.Default.Person,
            title = "Backoffice",
            subtitle = "SYSTEM ADMINISTRATION",
            desc = "Responsible for system administration and management.",
            functions = listOf("Manage system users", "Create and update microgrid stations", "Monitor reservations")
        )

        Spacer(modifier = Modifier.height(24.dp))

        UserRoleCard(
            icon = Icons.Default.Build,
            title = "Grid Operator",
            subtitle = "DAILY OPERATIONS",
            desc = "Responsible for daily operational activities at microgrid stations.",
            functions = listOf("View microgrid station information", "Scan Prosumer QR codes", "Complete energy transfers")
        )

        Spacer(modifier = Modifier.height(24.dp))

        UserRoleCard(
            icon = Icons.Default.Home,
            title = "Solar Prosumer",
            subtitle = "END USER",
            desc = "A property owner who uses solar panels and interacts with microgrid stations.",
            functions = listOf("View available energy slots", "Create reservations", "Receive reservation QR confirmation")
        )
    }
}

@Composable
fun UserRoleCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, desc: String, functions: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceLight)
            .border(1.dp, BorderLight, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(SurfaceLight, RoundedCornerShape(16.dp))
                    .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = CharcoalText, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = CharcoalText)
                Text(text = subtitle, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = LimeAccent, letterSpacing = 1.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = desc, color = GrayText, fontSize = 14.sp, lineHeight = 20.sp)
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceLight)
                .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text("KEY FUNCTIONS", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = CharcoalText, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                functions.forEach { function ->
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier.size(16.dp).clip(RoundedCornerShape(8.dp)).background(LimeAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(LimeAccent))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = function, color = GrayText, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
