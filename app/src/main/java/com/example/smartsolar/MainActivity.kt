package com.example.smartsolar

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsolar.features.auth.repository.AuthRepository
import com.example.smartsolar.features.auth.repository.ProsumerRepository
import com.example.smartsolar.features.auth.ui.*
import com.example.smartsolar.features.dashboard.ui.OperatorDashboardScreen
import com.example.smartsolar.features.dashboard.ui.ProsumerDashboardScreen
import com.example.smartsolar.features.microgrid.local.StationDatabaseHelper
import com.example.smartsolar.features.microgrid.network.NetworkModule
import com.example.smartsolar.features.microgrid.repository.MicrogridRepository
import com.example.smartsolar.features.microgrid.ui.MicrogridViewModel
import com.example.smartsolar.features.microgrid.ui.StationListScreen
import com.example.smartsolar.features.operator.ui.OperatorBookingsScreen
import com.example.smartsolar.features.operator.ui.OperatorProfileScreen
import com.example.smartsolar.features.operator.ui.QRScannerScreen
import com.example.smartsolar.features.operator.ui.QRVerificationResultScreen
import com.example.smartsolar.features.reservations.models.Reservation
import com.example.smartsolar.features.reservations.repository.ReservationRepository
import com.example.smartsolar.features.reservations.ui.*
import com.example.smartsolar.features.settings.repository.SettingsRepository
import com.example.smartsolar.features.settings.ui.SettingsScreen
import com.example.smartsolar.ui.components.CustomBottomNavigation
import com.example.smartsolar.ui.components.NavItem
import com.example.smartsolar.ui.theme.CharcoalText
import com.example.smartsolar.ui.theme.GrayText
import com.example.smartsolar.ui.theme.SmartSolarTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState

// ---- Navigation sealed class ----
sealed class Screen {
    // Auth
    object Login : Screen()
    object Register : Screen()
    // Prosumer app shell
    object ProsumerHome : Screen()
    object ProsumerStations : Screen()
    object ProsumerBookings : Screen()
    object ProsumerHistory : Screen()
    object ProsumerProfile : Screen()
    // Prosumer detail screens
    data class StationDetails(val stationId: String) : Screen()
    data class CreateReservation(val preselectedStationId: String? = null) : Screen()
    data class ReservationDetails(val reservation: Reservation) : Screen()
    data class ModifyReservation(val reservation: Reservation) : Screen()
    data class QRDisplay(val reservation: Reservation) : Screen()
    // Operator app shell
    object OperatorHome : Screen()
    object OperatorStations : Screen()
    object OperatorBookings : Screen()
    object OperatorScan : Screen()
    object OperatorProfile : Screen()
    // Operator detail
    data class OperatorReservationDetails(val reservation: Reservation) : Screen()
    data class QRVerificationResult(val reservation: Reservation) : Screen()
    // Shared
    object Settings : Screen()
    data class EditProfile(val profile: com.example.smartsolar.features.auth.models.ProsumerProfile) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsRepository = SettingsRepository(applicationContext)
        setContent {
            val isDarkMode by settingsRepository.isDarkMode.collectAsState()
            SmartSolarTheme(darkTheme = isDarkMode) {
                SmartSolarApp(settingsRepository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSolarApp(settingsRepository: SettingsRepository) {
    val context = LocalContext.current

    // State
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
    var activeRole by remember { mutableStateOf("") }
    var isLoggedIn by remember { mutableStateOf(false) }
    var authToken by remember { mutableStateOf("") }
    var prosumerNic by remember { mutableStateOf("") }
    var prosumerName by remember { mutableStateOf("") }

    // Prosumer tab state
    var prosumerTabIndex by remember { mutableStateOf(0) }
    // Operator tab state
    var operatorTabIndex by remember { mutableStateOf(0) }

    // ViewModels
    val authViewModel = remember {
        AuthViewModel(AuthRepository(NetworkModule.microgridApiService))
    }
    val microgridViewModel = remember {
        MicrogridViewModel(MicrogridRepository(NetworkModule.microgridApiService, StationDatabaseHelper(context)))
    }
    val reservationViewModel = remember {
        ReservationViewModel(ReservationRepository(NetworkModule.reservationApiService))
    }
    val prosumerViewModel = remember {
        ProsumerViewModel(ProsumerRepository(NetworkModule.prosumerApiService))
    }

    val prosumerNavItems = listOf(
        NavItem.Home,
        NavItem("Stations", Icons.Filled.ElectricBolt, Icons.Outlined.ElectricBolt),
        NavItem("Bookings", Icons.Filled.BookOnline, Icons.Outlined.BookOnline),
        NavItem("History", Icons.Filled.History, Icons.Outlined.History),
        NavItem.Profile
    )
    val operatorNavItems = listOf(
        NavItem.Home,
        NavItem("Stations", Icons.Filled.ElectricBolt, Icons.Outlined.ElectricBolt),
        NavItem("Bookings", Icons.Filled.BookOnline, Icons.Outlined.BookOnline),
        NavItem("Scan", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner),
        NavItem.Profile
    )

    // Handle login success
    val authState by authViewModel.authState.collectAsState()
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val data = (authState as AuthState.Success).authData
            authToken = "Bearer ${data.token}"
            activeRole = data.role
            isLoggedIn = true
            NetworkModule.authToken = data.token
            prosumerNic = data.nic ?: ""
            prosumerName = data.name ?: ""
            currentScreen = if (data.role.equals("Prosumer", ignoreCase = true)) Screen.ProsumerHome else Screen.OperatorHome
            authViewModel.resetState()
        }
    }

    // Logout helper
    fun logout() {
        isLoggedIn = false
        authToken = ""
        activeRole = ""
        prosumerNic = ""
        prosumerName = ""
        NetworkModule.authToken = ""
        prosumerTabIndex = 0
        operatorTabIndex = 0
        currentScreen = Screen.Login
    }

    // ---- Full-screen overlays (Settings, Register, Edit Profile) ----
    when (currentScreen) {
        is Screen.Settings -> {
            SettingsScreen(
                settingsRepository = settingsRepository,
                onClearCache = {
                    CoroutineScope(Dispatchers.IO).launch { StationDatabaseHelper(context).clearStations() }
                    Toast.makeText(context, "Cache cleared", Toast.LENGTH_SHORT).show()
                },
                onNavigateBack = {
                    currentScreen = if (activeRole.equals("Prosumer", ignoreCase = true)) Screen.ProsumerProfile else Screen.OperatorProfile
                }
            )
            return
        }
        is Screen.Register -> {
            RegisterScreen(
                viewModel = prosumerViewModel,
                onRegisterSuccess = {
                    Toast.makeText(context, "Account created! Please log in.", Toast.LENGTH_LONG).show()
                    currentScreen = Screen.Login
                },
                onNavigateBack = { currentScreen = Screen.Login }
            )
            return
        }
        is Screen.EditProfile -> {
            val profile = (currentScreen as Screen.EditProfile).profile
            EditProfileScreen(
                viewModel = prosumerViewModel,
                token = authToken,
                existingProfile = profile,
                onSuccess = {
                    Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                    currentScreen = Screen.ProsumerProfile
                },
                onNavigateBack = { currentScreen = Screen.ProsumerProfile }
            )
            return
        }
        is Screen.QRDisplay -> {
            QRDisplayScreen(
                reservation = (currentScreen as Screen.QRDisplay).reservation,
                onNavigateBack = { 
                    prosumerTabIndex = 2
                    currentScreen = Screen.ProsumerHome 
                }
            )
            return
        }
        is Screen.ReservationDetails -> {
            ReservationDetailsScreen(
                reservation = (currentScreen as Screen.ReservationDetails).reservation,
                token = authToken,
                viewModel = reservationViewModel,
                isOperator = false,
                onNavigateBack = { 
                    prosumerTabIndex = 2
                    currentScreen = Screen.ProsumerHome 
                },
                onModify = { res -> currentScreen = Screen.ModifyReservation(res) },
                onViewQR = { res -> currentScreen = Screen.QRDisplay(res) },
                onComplete = {}
            )
            return
        }
        is Screen.ModifyReservation -> {
            val res = (currentScreen as Screen.ModifyReservation).reservation
            ModifyReservationScreen(
                reservation = res,
                viewModel = reservationViewModel,
                microgridViewModel = microgridViewModel,
                token = authToken,
                onSuccess = {
                    Toast.makeText(context, "Reservation updated!", Toast.LENGTH_SHORT).show()
                    prosumerTabIndex = 2
                    currentScreen = Screen.ProsumerHome
                },
                onNavigateBack = { currentScreen = Screen.ReservationDetails(res) }
            )
            return
        }
        is Screen.StationDetails -> {
            val stationId = (currentScreen as Screen.StationDetails).stationId
            com.example.smartsolar.features.microgrid.ui.StationDetailsScreen(
                stationId = stationId,
                viewModel = microgridViewModel,
                onViewSlotsClicked = { currentScreen = Screen.CreateReservation(stationId) },
                onNavigateBack = { currentScreen = Screen.ProsumerHome }
            )
            return
        }
        is Screen.CreateReservation -> {
            val preselectedId = (currentScreen as Screen.CreateReservation).preselectedStationId
            CreateReservationScreen(
                reservationViewModel = reservationViewModel,
                microgridViewModel = microgridViewModel,
                token = authToken,
                nic = prosumerNic,
                preselectedStationId = preselectedId,
                onSuccess = {
                    Toast.makeText(context, "Reservation created!", Toast.LENGTH_SHORT).show()
                    prosumerTabIndex = 2
                    currentScreen = Screen.ProsumerHome
                },
                onNavigateBack = { 
                    prosumerTabIndex = 2
                    currentScreen = Screen.ProsumerHome 
                }
            )
            return
        }
        is Screen.OperatorReservationDetails -> {
            val res = (currentScreen as Screen.OperatorReservationDetails).reservation
            ReservationDetailsScreen(
                reservation = res,
                token = authToken,
                viewModel = reservationViewModel,
                isOperator = true,
                onNavigateBack = { currentScreen = Screen.OperatorBookings },
                onModify = {},
                onViewQR = {},
                onComplete = { r -> currentScreen = Screen.QRVerificationResult(r) }
            )
            return
        }
        is Screen.QRVerificationResult -> {
            val res = (currentScreen as Screen.QRVerificationResult).reservation
            QRVerificationResultScreen(
                reservation = res,
                token = authToken,
                viewModel = reservationViewModel,
                onComplete = {
                    Toast.makeText(context, "Energy Transfer Completed!", Toast.LENGTH_SHORT).show()
                    currentScreen = Screen.OperatorHome
                },
                onNavigateBack = { currentScreen = Screen.OperatorScan }
            )
            return
        }
        else -> {}
    }

    // ---- Login screen (no nav bar) ----
    if (!isLoggedIn) {
        LoginScreen(
            viewModel = authViewModel,
            onLoginSuccess = { /* handled by LaunchedEffect above */ },
            onNavigateToRegister = { currentScreen = Screen.Register }
        )
        return
    }

    // ---- Role-based main app scaffold ----
    val isProsumer = activeRole.equals("Prosumer", ignoreCase = true)
    val navItems = if (isProsumer) prosumerNavItems else operatorNavItems
    val tabIndex = if (isProsumer) prosumerTabIndex else operatorTabIndex

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WbSunny, "Logo", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Solar", color = CharcoalText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Grid", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { currentScreen = Screen.Settings }) {
                        Icon(Icons.Default.Settings, "Settings", tint = CharcoalText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            )
        },
        bottomBar = {
            CustomBottomNavigation(
                items = navItems,
                selectedIndex = tabIndex,
                onItemSelected = { idx ->
                    if (isProsumer) prosumerTabIndex = idx else operatorTabIndex = idx
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isProsumer) {
                when (prosumerTabIndex) {
                    0 -> ProsumerDashboardScreen(
                        viewModel = microgridViewModel,
                        reservationViewModel = reservationViewModel,
                        token = authToken,
                        prosumerName = prosumerName,
                        nic = prosumerNic,
                        onNavigateToStations = { prosumerTabIndex = 1 },
                        onNavigateToBookings = { prosumerTabIndex = 2 },
                        onNavigateToHistory = { prosumerTabIndex = 3 },
                        onCreateReservation = { currentScreen = Screen.CreateReservation() },
                        onViewDetails = { stationId -> currentScreen = Screen.StationDetails(stationId) }
                    )
                    1 -> StationListScreen(viewModel = microgridViewModel, onStationSelected = { stationId -> currentScreen = Screen.StationDetails(stationId) })
                    2 -> MyBookingsScreen(
                        viewModel = reservationViewModel,
                        token = authToken,
                        nic = prosumerNic,
                        onReservationClick = { res -> currentScreen = Screen.ReservationDetails(res) },
                        onCreateReservation = { currentScreen = Screen.CreateReservation() }
                    )
                    3 -> BookingHistoryScreen(
                        viewModel = reservationViewModel,
                        token = authToken,
                        nic = prosumerNic,
                        onReservationClick = { res -> currentScreen = Screen.ReservationDetails(res) }
                    )
                    4 -> ProsumerProfileScreen(
                        viewModel = prosumerViewModel,
                        token = authToken,
                        onNavigateToEdit = { profile -> currentScreen = Screen.EditProfile(profile) },
                        onLogout = { logout() }
                    )
                }
            } else {
                when (operatorTabIndex) {
                    0 -> OperatorDashboardScreen(
                        viewModel = microgridViewModel,
                        reservationViewModel = reservationViewModel,
                        token = authToken,
                        onNavigateToStations = { operatorTabIndex = 1 },
                        onNavigateToBookings = { operatorTabIndex = 2 },
                        onNavigateToScan = { operatorTabIndex = 3 }
                    )
                    1 -> StationListScreen(viewModel = microgridViewModel, onStationSelected = {})
                    2 -> OperatorBookingsScreen(
                        viewModel = reservationViewModel,
                        token = authToken,
                        onReservationClick = { res -> currentScreen = Screen.OperatorReservationDetails(res) }
                    )
                    3 -> QRScannerScreen(
                        viewModel = reservationViewModel,
                        token = authToken,
                        onQRScanned = { res -> currentScreen = Screen.QRVerificationResult(res) },
                        onNavigateBack = { operatorTabIndex = 0 }
                    )
                    4 -> OperatorProfileScreen(
                        token = authToken,
                        role = activeRole,
                        onLogout = { logout() }
                    )
                }
            }
        }
    }
}
