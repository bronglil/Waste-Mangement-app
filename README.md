# Waste Management System - Android Project

## Project Overview
The **Waste Management System** is a comprehensive solution for optimizing waste collection and monitoring. It integrates real-time monitoring using IoT devices and provides a mobile application for drivers to easily access bin status and navigate routes. The system ensures efficient waste collection, with features like route optimization and seamless communication between IoT devices, the backend, and the mobile app.

The Android app is developed using **Kotlin** and **Jetpack Compose**, allowing drivers to receive real-time updates on waste bin statuses and navigate efficiently to the bins that require attention.

## Technical Stack
- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern UI toolkit for native Android UI
- **Coroutines**: For asynchronous programming
- **Flow**: For reactive programming
- **Hilt**: For dependency injection
- **Retrofit**: For API communication
- **Google Maps SDK**: For maps and navigation
- **MVVM Architecture**: For clean separation of concerns

## Download and Setup Instructions

### Download Links

#### Mobile App (Android)
```bash
# Clone the Android repository
git clone git@github.com:Irfan-Ullah-cs/Waste-Mangement-System.git
cd Waste-Mangement-System/android
# Open in Android Studio and build
./gradlew assembleDebug
```

### Setting Up the Repository

To set up the repository for development, follow these steps:

1. **Clone the repository** from GitHub:
    ```bash
    git clone git@github.com:bronglil/Waste-Mangement-app.git
    cd Waste-Mangement-System/android
    ```
2. **Open the Android project in Android Studio**:
    - If you haven't already, [download Android Studio](https://developer.android.com/studio)
    - Once installed, open Android Studio and select "Open an Existing Project"
    - Navigate to the `android` folder and open it

3. **Install dependencies** and build the project:
    ```bash
    ./gradlew assembleDebug
    ```
    The APK will be located in the `app/build/outputs/apk/debug/` directory

4. **Add Values** 
Go to AndroidManifest.xml file and add API key.
Go to RetrofitInstance and add your Ip address.
Go to res/xml/network_security_config.xml file an update Ip address.

### Required Software
- **Android Studio** Latest version
    - [Download Android Studio](https://developer.android.com/studio)
- **Kotlin Plugin** Latest version (included in Android Studio)
- **Android SDK** API Level 21 or higher
    - Download through Android Studio SDK Manager

### System Requirements
- **CPU**: 4+ cores
- **RAM**: 8GB minimum
- **Storage**: 10GB free space
- **OS**: Windows/Linux/macOS

### Additional Tools
- **Git** [Download Git](https://git-scm.com/downloads)
- **Postman (API Testing)** [Download Postman](https://www.postman.com/downloads/)

## Gradle Commands

```bash
# Clean project
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Check dependencies
./gradlew dependencies

# Install debug variant
./gradlew installDebug

# Generate Hilt components
./gradlew kaptDebugKotlin

# Build and run specific variant
./gradlew runDebug
```



## Features Summary
- **Driver Dashboard**: Drivers can access the bin status through the mobile app
- **Navigation**: Features include map functionality to locate bins and navigate routes
- **Efficient Waste Collection**: Drivers are notified of full bins and can plan optimized routes
- **Integrated with Backend**: The app communicates with the backend to fetch the real-time data

## API Endpoints
- GET `/api/bins` - Fetch all bins
- GET `/api/bins/{id}` - Get specific bin details
- PUT `/api/bins/{id}` - Update bin status
- GET `/api/routes/shortest` - Get optimized collection route

## Troubleshooting

### Common Issues and Solutions

#### Gradle Build Fails
```bash
# Clean and rebuild
./gradlew clean build
```

#### Android Studio Sync Issues
- File > Invalidate Caches / Restart
- Sync Project with Gradle Files

#### Device Compatibility
- Ensure minimum SDK version (API 21) is met
- Check device/emulator settings

## Support

For technical support or questions:
- Create an issue on GitHub


## License

This project is licensed under the MIT License. See the LICENSE file for details.
