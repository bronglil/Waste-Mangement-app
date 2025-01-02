import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.compose.*
import com.google.maps.model.TravelMode
import kotlinx.coroutines.*
import android.os.Looper
import androidx.compose.material.icons.filled.Place

@Composable
fun MapWithDirections(
    destinationLat: Double,
    destinationLng: Double,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var vehiclePosition by remember { mutableStateOf<LatLng?>(null) }
    var directionsPolylinePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var isNavigating by remember { mutableStateOf(false) }
    var showGpsDialog by remember { mutableStateOf(false) }

    val destination = remember { LatLng(destinationLat, destinationLng) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(destination, 15f)
    }

    // GPS Dialog
    if (showGpsDialog) {
        AlertDialog(
            onDismissRequest = { showGpsDialog = false },
            title = { Text("Enable GPS") },
            text = { Text("GPS is required for navigation. Please enable it in settings.") },
            confirmButton = {
                TextButton(onClick = {
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    showGpsDialog = false
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGpsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Location callback
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val newPosition = LatLng(location.latitude, location.longitude)
                    vehiclePosition = newPosition

                    if (vehiclePosition == null) {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(newPosition, 15f)
                    }

                    if (isNavigating) {
                        scope.launch {
                            updateDirections(newPosition, destination) { points ->
                                directionsPolylinePoints = points
                            }
                        }

                        if (!cameraPositionState.isMoving) {
                            cameraPositionState.position = CameraPosition.Builder()
                                .target(newPosition)
                                .zoom(18f)
                                .tilt(45f)
                                .build()
                        }
                    }
                }
            }
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.all { it }
        if (hasLocationPermission) {
            checkGpsAndStartUpdates(context, locationCallback) { showGpsDialog = true }
        }
    }

    // Check permissions on launch
    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                hasLocationPermission = true
                checkGpsAndStartUpdates(context, locationCallback) { showGpsDialog = true }
            }
            else -> {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                mapType = MapType.NORMAL
            )
        ) {
            // Navigation route
            if (directionsPolylinePoints.isNotEmpty() && isNavigating) {
                Polyline(
                    points = directionsPolylinePoints,
                    color = Color(0xFF2196F3),
                    width = 8f
                )
            }

            // Direct line and markers
            vehiclePosition?.let { position ->
                if (!isNavigating) {
                    Polyline(
                        points = listOf(position, destination),
                        color = Color(0x4D2196F3),
                        width = 4f
                    )
                }

                Marker(
                    state = MarkerState(position = position),
                    title = "Current Location",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                )
            }

            Marker(
                state = MarkerState(position = destination),
                title = "Destination",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
        }

        // Navigation buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { isNavigating = !isNavigating }
            ) {
                Icon(
                    imageVector = if (isNavigating) Icons.Default.Close else Icons.Default.Send,
                    contentDescription = if (isNavigating) "Stop Navigation" else "Start Navigation"
                )
            }

            FloatingActionButton(
                onClick = {
                    vehiclePosition?.let {
                        val uri = "google.navigation:q=${destinationLat},${destinationLng}&mode=d"
                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri))
                        intent.setPackage("com.google.android.apps.maps")
                        context.startActivity(intent)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Open in Google Maps"
                )
            }
        }
    }
}

private fun checkGpsAndStartUpdates(
    context: Context,
    locationCallback: LocationCallback,
    onGpsDisabled: () -> Unit
) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        startLocationUpdates(context, locationCallback)
    } else {
        onGpsDisabled()
    }
}

@SuppressLint("MissingPermission")
private fun startLocationUpdates(context: Context, locationCallback: LocationCallback) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationRequest = LocationRequest.Builder(5000L)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .build()

    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.getMainLooper()
    )
}

private suspend fun updateDirections(origin: LatLng, destination: LatLng, onResult: (List<LatLng>) -> Unit) {
    withContext(Dispatchers.IO) {
        try {
            val context = GeoApiContext.Builder()
                .apiKey("APIKEY")  // Replace with your API key
                .build()

            val result = DirectionsApi.newRequest(context)
                .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .mode(TravelMode.DRIVING)
                .await()

            result.routes.firstOrNull()?.legs?.flatMap { leg ->
                leg.steps.flatMap { step ->
                    com.google.maps.internal.PolylineEncoding.decode(step.polyline.encodedPath)
                        .map { LatLng(it.lat, it.lng) }
                }
            }?.let { points ->
                withContext(Dispatchers.Main) {
                    onResult(points)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}