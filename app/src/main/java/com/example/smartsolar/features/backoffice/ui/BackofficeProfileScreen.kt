package com.example.smartsolar.features.backoffice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.microgrid.network.NetworkModule
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import com.example.smartsolar.ui.theme.LimeAccentDark

private val DangerRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackofficeProfileScreen(
    token: String,
    role: String,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Officer Hero Badge Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(LimeAccent.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.AdminPanelSettings,
                        "Officer Icon",
                        tint = CharcoalText,
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    "Backoffice Officer",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = CharcoalText
                )

                Spacer(Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(LimeAccent)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        "ROLE: BACKOFFICE • CLEARANCE LEVEL 1",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CharcoalText
                    )
                }
            }
        }

        // Authority Breakdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "OFFICER AUTHORIZATION SCOPE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = GrayText
                )

                AuthorityItem(
                    icon = Icons.Filled.People,
                    title = "User Account Management",
                    desc = "Activate, deactivate, and govern system credentials"
                )
                AuthorityItem(
                    icon = Icons.Filled.AssignmentInd,
                    title = "Prosumer Registry Oversight",
                    desc = "Review, approve, and reject account deactivation requests"
                )
                AuthorityItem(
                    icon = Icons.Filled.EvStation,
                    title = "Microgrid Infrastructure",
                    desc = "Monitor generation capacity and distributed battery storage"
                )
                AuthorityItem(
                    icon = Icons.Filled.ConfirmationNumber,
                    title = "Reservation Audit & Overrides",
                    desc = "Approve pending requests and resolve schedule disputes"
                )
            }
        }

        // Host Endpoint Information
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Dns, "Server", tint = CharcoalText, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("API Host Endpoint", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CharcoalText)
                    Text(NetworkModule.BASE_URL, fontSize = 11.sp, color = GrayText)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Logout Button
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, "Logout", modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Sign Out Officer Account", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out Confirmation", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out from the Backoffice Officer Command Center?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AuthorityItem(icon: ImageVector, title: String, desc: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LimeAccent.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, title, tint = CharcoalText, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalText)
            Text(desc, fontSize = 11.sp, color = GrayText)
        }
    }
}
