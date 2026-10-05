package com.example.smartsolar.features.auth.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.auth.models.ProsumerProfile
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.LimeAccent
import com.example.smartsolar.ui.theme.LimeAccentDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProsumerProfileScreen(
    viewModel: ProsumerViewModel,
    token: String,
    onNavigateToEdit: (ProsumerProfile) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val profileState by viewModel.profileState.collectAsState()
    val profile by viewModel.currentProfile.collectAsState()
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showDeactivateConfirm by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile(token)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Prosumer Profile", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = CharcoalText)
                        Text("Certified Microgrid Energy Exporter", fontSize = 11.sp, color = GrayText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                profileState is ProfileState.Loading -> {
                    Spacer(modifier = Modifier.height(100.dp))
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                profile != null -> {
                    val p = profile!!
                    val initials = "${p.firstName?.firstOrNull() ?: "P"}${p.lastName?.firstOrNull() ?: ""}"
                    val fullName = "${p.firstName ?: ""} ${p.lastName ?: ""}".trim().ifBlank { "Smart Prosumer" }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(3.dp, CharcoalText),
                                    modifier = Modifier.size(96.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text(
                                            initials,
                                            fontSize = 34.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = CharcoalText
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E))
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Verified", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(fullName, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = CharcoalText)

                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF15803D)))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "ACTIVE • PROSUMER TIER 1",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF15803D),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Microgrid Energy Ledger", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CharcoalText)
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("SYNCHRONIZED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CharcoalText, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                LedgerBox("TOTAL EXPORTED", "342.8 kWh", Icons.Default.Bolt, CharcoalText, Modifier.weight(1f))
                                LedgerBox("TOTAL CREDITS", "LKR 28,450", Icons.Default.AccountBalanceWallet, Color(0xFF15803D), Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                LedgerBox("CARBON OFFSET", "274 kg CO₂", Icons.Default.Eco, Color(0xFF16A34A), Modifier.weight(1f))
                                LedgerBox("GRID AUTONOMY", "94% Self-Suff.", Icons.Default.Sensors, LimeAccentDark, Modifier.weight(1f))
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Account & Identity Information", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CharcoalText)

                            EnhancedProfileRow(
                                icon = Icons.Default.Badge,
                                label = "National Identity Card (NIC)",
                                value = p.nic ?: "Not provided",
                                isCopyable = true,
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("NIC", p.nic ?: ""))
                                    Toast.makeText(context, "NIC copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                            EnhancedProfileRow(
                                icon = Icons.Default.Email,
                                label = "Verified Email",
                                value = p.email ?: "Not provided"
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                            EnhancedProfileRow(
                                icon = Icons.Default.Phone,
                                label = "Contact Phone",
                                value = if (p.phoneNumber.isNullOrEmpty()) "Not provided" else p.phoneNumber
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                            EnhancedProfileRow(
                                icon = Icons.Default.Home,
                                label = "Grid Node Physical Address",
                                value = if (p.address.isNullOrEmpty()) "Not provided" else p.address
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Grid Connection & Tariff Details", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CharcoalText)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Feed-in Tariff Agreement", fontSize = 12.sp, color = GrayText)
                                Text("LKR 44.50/kWh (Fixed)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Assigned Regional Grid Node", fontSize = 12.sp, color = GrayText)
                                Text("Node LK-WEST-01", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Smart Meter Hardware ID", fontSize = 12.sp, color = GrayText)
                                Text("#SM-99410-X", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                            }
                        }
                    }

                    Button(
                        onClick = { onNavigateToEdit(p) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalText, contentColor = LimeAccent),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Personal Details", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    }

                    OutlinedButton(
                        onClick = { showDeactivateDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.PersonOff, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Request Account Deactivation", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CharcoalText),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Log Out", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
                profileState is ProfileState.Error -> {
                    Spacer(modifier = Modifier.height(60.dp))
                    Text((profileState as ProfileState.Error).message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadProfile(token) }) { Text("Retry") }
                }
            }

            if (profileState is ProfileState.Success) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    viewModel.resetProfileState()
                }
                Snackbar(modifier = Modifier.padding(16.dp)) {
                    Text((profileState as ProfileState.Success).message)
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out of your Smart Solar Prosumer account?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) { Text("Log Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } }
        )
    }

    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            title = { Text("Request Account Deactivation", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to submit a deactivation request? Your account will be reviewed by a Smart Solar administrator.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeactivateDialog = false
                    viewModel.requestDeactivation(token) {
                        showDeactivateConfirm = true
                    }
                }) { Text("Submit Request", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDeactivateDialog = false }) { Text("Cancel") } }
        )
    }

    if (showDeactivateConfirm) {
        AlertDialog(
            onDismissRequest = { showDeactivateConfirm = false },
            title = { Text("Request Submitted", fontWeight = FontWeight.Bold) },
            text = { Text("Your account deactivation request has been submitted successfully. An administrator will review your request.") },
            confirmButton = { TextButton(onClick = { showDeactivateConfirm = false }) { Text("OK") } }
        )
    }
}

@Composable
private fun LedgerBox(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 9.sp, color = GrayText, fontWeight = FontWeight.SemiBold)
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CharcoalText)
        }
    }
}

@Composable
private fun EnhancedProfileRow(
    icon: ImageVector,
    label: String,
    value: String,
    isCopyable: Boolean = false,
    onCopy: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = CharcoalText, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text(value, fontSize = 14.sp, color = CharcoalText, fontWeight = FontWeight.SemiBold)
            }
        }
        if (isCopyable) {
            IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = GrayText, modifier = Modifier.size(16.dp))
            }
        }
    }
}
