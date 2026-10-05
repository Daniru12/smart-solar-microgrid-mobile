# Smart Solar Microgrid Mobile App

A modern Android application for managing smart solar microgrid stations, prosumer reservations, and energy distribution. Built with Kotlin and Jetpack Compose.

## Features

### User Roles
- **Prosumer**: View dashboard, manage reservations, browse stations, manage profile
- **Admin**: Manage users, prosumers, deactivation requests, tab permissions, and bookings
- **Grid Operator**: Monitor dashboard, scan QR codes, view station maps
- **Backoffice**: Combined permissions for admin and operator functions

### Key Features
- **Landing Page**: Comprehensive marketing page with hero section, statistics, features, and call-to-action
- **Authentication**: Secure login and registration system with role-based access control
- **Station Management**: Create, view, edit, and manage solar microgrid stations
- **Reservation System**: Prosumers can book charging stations with QR code scanning
- **QR Code Scanner**: CameraX and ML Kit integration for scanning station QR codes
- **QR Code Generation**: ZXing library for generating QR codes
- **Navigation**: Jetpack Navigation Compose for seamless app navigation
- **Material Design 3**: Modern UI with Material You theming

## Tech Stack

- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose with Material 3
- **Build System**: Gradle with Kotlin DSL
- **Android SDK**: Target SDK 37, Min SDK 24
- **Navigation**: Jetpack Navigation Compose 2.7.7
- **Networking**: Retrofit 2.11.0 with Gson converter
- **Async Programming**: Kotlin Coroutines 1.8.0
- **QR Code**: ZXing 3.5.3
- **Camera**: CameraX 1.3.4
- **ML Kit**: Barcode Scanning 17.3.0
- **Testing**: JUnit, Espresso, Compose UI Testing

## Project Structure

```
app/src/main/java/com/example/smartsolar/
├── MainActivity.kt                 # Main activity entry point
├── AboutSection.kt                 # About section component
├── CallToActionSection.kt          # Call to action component
├── EnergyFlowSection.kt            # Energy flow visualization
├── FeaturesSection.kt              # Features showcase
├── HeroSection.kt                  # Hero section component
├── HowItWorksSection.kt            # How it works section
├── PlatformSection.kt              # Platform information
├── StatisticsSection.kt            # Statistics display
├── UserRolesSection.kt             # User roles explanation
├── WhySolarGridSection.kt          # Why solar grid section
├── api/                            # API layer
│   ├── ApiService.kt
│   ├── data models
│   └── repositories
├── features/                       # Feature modules
│   ├── auth/                       # Authentication (login, register)
│   ├── dashboard/                  # Dashboard views
│   ├── microgrid/                  # Station management
│   ├── operator/                   # Operator features
│   ├── reservations/               # Reservation management
│   └── settings/                   # App settings
└── ui/                             # UI components and themes
```

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK with API level 37
- Gradle 8.0 or higher

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd smart-solar-microgrid-mobile
```

2. Open the project in Android Studio:
   - File → Open → Select the project directory

3. Sync Gradle:
   - Android Studio will automatically prompt to sync Gradle
   - Or click "Sync Project with Gradle Files" in the toolbar

4. Run the app:
   - Connect an Android device or start an emulator
   - Click the Run button or press Shift + F10

### Building from Command Line

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

## Configuration

### API Base URL

Update the API base URL in the `ApiService.kt` file:

```kotlin
private const val BASE_URL = "http://your-api-url/api/"
```

### Build Variants

The project supports the following build types:
- **debug**: Development build with debugging enabled
- **release**: Production build (optimizations currently disabled)

## Available Gradle Tasks

- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build release APK
- `./gradlew installDebug` - Install debug build on connected device
- `./gradlew test` - Run unit tests
- `./gradlew connectedAndroidTest` - Run instrumented tests
- `./gradlew clean` - Clean build artifacts

## Dependencies

### Core Android Libraries
- AndroidX Core KTX
- AndroidX Lifecycle Runtime KTX
- AndroidX Activity Compose

### Jetpack Compose
- Compose BOM 2026.02.01
- Material 3
- Material Icons Extended
- Navigation Compose

### Networking
- Retrofit 2.11.0
- Gson Converter

### Camera & QR
- CameraX 1.3.4
- ML Kit Barcode Scanning 17.3.0
- ZXing Core 3.5.3

### Async
- Kotlin Coroutines 1.8.0

### Testing
- JUnit 4.13.2
- AndroidX JUnit 1.1.5
- Espresso 3.7.0
- Compose UI Testing

## Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

Test results are located in:
- Unit tests: `app/build/test-results/`
- Instrumented tests: `app/build/reports/androidTests/`

## Troubleshooting

### Gradle Sync Issues
- Ensure you have the latest Android SDK
- Update JDK to version 11 or higher
- Clear Gradle cache: `./gradlew clean --no-daemon`

### Camera Permissions
The app requires camera permissions for QR code scanning. Ensure permissions are granted in `AndroidManifest.xml` and runtime permissions are requested.

### API Connection Issues
- Verify the API base URL is correct
- Check network connectivity
- Ensure the API server is running and accessible

Youtube Video Link : https://youtu.be/Bd7gDFGmAKw
Repo Link Web : https://github.com/ishani2924/smart-solar-microgrid-web.git
Repo Link Mobile : https://github.com/Daniru12/smart-solar-microgrid-mobile.git
Repo Link Backend : https://github.com/Daniru12/smart-solar-microgrid-api.git
