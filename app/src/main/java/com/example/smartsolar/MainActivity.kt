package com.example.smartsolar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.smartsolar.ui.components.CustomBottomNavigation
import com.example.smartsolar.ui.components.NavItem
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.smartsolar.features.microgrid.local.StationDatabaseHelper
import com.example.smartsolar.features.microgrid.network.NetworkModule
import com.example.smartsolar.features.microgrid.repository.MicrogridRepository
import com.example.smartsolar.features.microgrid.ui.MicrogridViewModel
import com.example.smartsolar.features.microgrid.ui.StationListScreen
import androidx.compose.ui.unit.sp
import com.example.smartsolar.ui.theme.SmartSolarTheme
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartSolarTheme {
                SmartSolarApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSolarApp() {
    var selectedItemIndex by remember { mutableStateOf(0) }
    
    val context = LocalContext.current
    val microgridViewModel = remember {
        val databaseHelper = StationDatabaseHelper(context)
        val repository = MicrogridRepository(NetworkModule.microgridApiService, databaseHelper)
        MicrogridViewModel(repository)
    }

    val navItems = listOf(
        NavItem.Home,
        NavItem.Search,
        NavItem.Analytics,
        NavItem.Profile
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Solar",
                            color = CharcoalText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Text(
                            text = "Grid",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Open menu */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = CharcoalText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        },
        bottomBar = {
            CustomBottomNavigation(
                items = navItems,
                selectedIndex = selectedItemIndex,
                onItemSelected = { selectedItemIndex = it }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedItemIndex) {
                0 -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item { HeroSection() }
                        item { StatisticsSection() }
                        item { AboutSection() }
                        item { HowItWorksSection() }
                        item { FeaturesSection() }
                        item { PlatformSection() }
                        item { EnergyFlowSection() }
                        item { UserRolesSection() }
                        item { WhySolarGridSection() }
                        item { CallToActionSection() }
                        item { Spacer(modifier = Modifier.height(100.dp)) } // padding for bottom nav
                    }
                }
                1 -> {
                    // Connect the Station List we built earlier
                    StationListScreen(
                        viewModel = microgridViewModel,
                        onStationSelected = { stationId ->
                            /* Handle click, e.g. navigate to Details */
                        }
                    )
                }
                2 -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Analytics Screen", color = CharcoalText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
                3 -> {
                    // Login Screen Placeholder
                    LoginScreenPlaceholder()
                }
            }
        }
    }
}

@Composable
fun LoginScreenPlaceholder() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.WbSunny,
            contentDescription = "Logo",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Welcome Back", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = CharcoalText)
        Text("Sign in to your account to continue", style = MaterialTheme.typography.bodyMedium, color = GrayText)
        Spacer(modifier = Modifier.height(48.dp))
        OutlinedTextField(
            value = email, 
            onValueChange = { email = it }, 
            label = { Text("Email Address", color = GrayText) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = CharcoalText,
                unfocusedTextColor = CharcoalText,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.LightGray
            ),
            shape = MaterialTheme.shapes.medium
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password, 
            onValueChange = { password = it }, 
            label = { Text("Password", color = GrayText) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = CharcoalText,
                unfocusedTextColor = CharcoalText,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.LightGray
            ),
            shape = MaterialTheme.shapes.medium
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { /* TODO: Connect to Auth Backend */ }, 
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = CharcoalText),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
