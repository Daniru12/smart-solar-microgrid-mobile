package com.example.smartsolar.features.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.auth.models.ProsumerProfile
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProsumerProfileScreen(
    viewModel: ProsumerViewModel,
    token: String,
    onNavigateToEdit: (ProsumerProfile) -> Unit,
    onLogout: () -> Unit
) {
    val profileState by viewModel.profileState.collectAsState()
    val profile by viewModel.currentProfile.collectAsState()
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showDeactivateConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile(token)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold, color = CharcoalText) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                profileState is ProfileState.Loading -> {
                    Spacer(modifier = Modifier.height(80.dp))
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                profile != null -> {
                    val p = profile!!
                    Spacer(modifier = Modifier.height(16.dp))
                    // Avatar
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(100.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                "${p.firstName?.firstOrNull() ?: ""}${p.lastName?.firstOrNull() ?: ""}",
                                fontSize = 36.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("${p.firstName ?: ""} ${p.lastName ?: ""}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = CharcoalText)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(p.status ?: "Unknown", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // Info Card
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            ProfileInfoRow(Icons.Default.Badge, "NIC", p.nic ?: "Not set")
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ProfileInfoRow(Icons.Default.Email, "Email", p.email ?: "Not set")
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ProfileInfoRow(Icons.Default.Phone, "Phone", if (p.phoneNumber.isNullOrEmpty()) "Not set" else p.phoneNumber)
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ProfileInfoRow(Icons.Default.Home, "Address", if (p.address.isNullOrEmpty()) "Not set" else p.address)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onNavigateToEdit(p) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = CharcoalText)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Profile", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showDeactivateDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.PersonOff, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Request Account Deactivation", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GrayText)
                    ) {
                        Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Logout", fontWeight = FontWeight.Bold)
                    }
                }
                profileState is ProfileState.Error -> {
                    Spacer(modifier = Modifier.height(40.dp))
                    Text((profileState as ProfileState.Error).message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadProfile(token) }) { Text("Retry") }
                }
            }

            // Success snackbar
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

    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            title = { Text("Request Account Deactivation") },
            text = { Text("Are you sure you want to submit a deactivation request? Your account will be reviewed by an administrator.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeactivateDialog = false
                    viewModel.requestDeactivation(token) {
                        showDeactivateConfirm = true
                    }
                }) { Text("Submit Request", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeactivateDialog = false }) { Text("Cancel") } }
        )
    }

    if (showDeactivateConfirm) {
        AlertDialog(
            onDismissRequest = { showDeactivateConfirm = false },
            title = { Text("Request Submitted") },
            text = { Text("Your account deactivation request has been submitted successfully. An administrator will review your request.") },
            confirmButton = { TextButton(onClick = { showDeactivateConfirm = false }) { Text("OK") } }
        )
    }
}

@Composable
private fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 15.sp, color = CharcoalText, fontWeight = FontWeight.SemiBold)
        }
    }
}
