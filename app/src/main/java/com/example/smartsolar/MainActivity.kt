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
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Solar",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Text(
                            text = "Grid",
                            color = MaterialTheme.colorScheme.tertiary,
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
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
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
                        item { FeaturesSection() }
                        item { HowItWorksSection() }
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
                        Text("Analytics Screen", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
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
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = email, 
            onValueChange = { email = it }, 
            label = { Text("Email", color = Color.White) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password, 
            onValueChange = { password = it }, 
            label = { Text("Password", color = Color.White) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { /* TODO: Connect to Auth Backend */ }, modifier = Modifier.fillMaxWidth(0.8f)) {
            Text("Sign In")
        }
    }
}
