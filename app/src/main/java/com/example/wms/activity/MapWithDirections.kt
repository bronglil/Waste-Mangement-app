import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.wms.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.compose.*
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.TravelMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MapWithDirections(
    destinationLat: Double,
    destinationLng: Double,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var vehiclePosition by remember { mutableStateOf<LatLng?>(null) }
    var vehicleBearing by remember { mutableStateOf(0f) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var directionsPolylinePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    val cameraPositionState = rememberCameraPositionState()

    val carIcon = remember {
        val bitmap = vectorToBitmap(context, R.drawable.ic_launcher_vehicle_foreground, 0.4f)
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    val destinationIcon = remember {
        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()

    // Location callback
    val locationCallback = rememberUpdatedState(newValue = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                val newPosition = LatLng(location.latitude, location.longitude)
                vehiclePosition = newPosition

                // Update bearing
                vehicleBearing = if (vehiclePosition != null) {
                    calculateBearing(vehiclePosition!!, newPosition)
                } else {
                    0f
                }

                // Update directions
                CoroutineScope(Dispatchers.Main).launch {
                    updateDirections(newPosition, LatLng(destinationLat, destinationLng)) { points ->
                        directionsPolylinePoints = points
                    }
                }

                // Update camera position
                cameraPositionState.position = CameraPosition.Builder()
                    .target(newPosition)
                    .zoom(17f)
                    .bearing(vehicleBearing)
                    .build()
            }
        }
    })

    // Request location permission
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasLocationPermission = isGranted
            if (isGranted) {
                if (!isGPSEnabled(context)) {
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                } else {
                    startLocationUpdates(fusedLocationClient, locationRequest, locationCallback.value)
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            hasLocationPermission = true
            if (!isGPSEnabled(context)) {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } else {
                startLocationUpdates(fusedLocationClient, locationRequest, locationCallback.value)
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
            // Destination Marker
            Marker(
                state = MarkerState(position = LatLng(destinationLat, destinationLng)),
                title = "Destination",
                icon = destinationIcon
            )

            // Vehicle Marker
            vehiclePosition?.let { position ->
                Marker(
                    state = MarkerState(position = position),
                    title = "Vehicle",
                    icon = carIcon,
                    rotation = vehicleBearing,
                    flat = true
                )

                // Draw directions polyline
                if (directionsPolylinePoints.isNotEmpty()) {
                    Polyline(
                        points = directionsPolylinePoints,
                        color = Color.Blue,
                        width = 5f,
                        pattern = listOf(Dash(30f), Gap(20f))
                    )
                }
            }
        }
    }
}

// Check if GPS is enabled
private fun isGPSEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

// Start location updates
@SuppressLint("MissingPermission")
private fun startLocationUpdates(
    fusedLocationClient: FusedLocationProviderClient,
    locationRequest: LocationRequest,
    locationCallback: LocationCallback
) {
    try {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// Update directions using the Google Directions API
private suspend fun updateDirections(origin: LatLng, destination: LatLng, onResult: (List<LatLng>) -> Unit) {
    withContext(Dispatchers.IO) {
        try {
            val context = GeoApiContext.Builder()
                .apiKey("APIKEY") // Replace with your actual API key
                .build()

            val result = DirectionsApi.newRequest(context)
                .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .mode(TravelMode.DRIVING)
                .alternatives(false)
                .await()

            result.routes.firstOrNull()?.overviewPolyline?.let { polyline ->
                val decodedPath = PolylineEncoding.decode(polyline.encodedPath)
                val points = decodedPath.map { LatLng(it.lat, it.lng) }
                withContext(Dispatchers.Main) {
                    onResult(points)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// Convert vector drawable to bitmap
private fun vectorToBitmap(context: Context, drawableResId: Int, scaleFactor: Float = 1f): Bitmap {
    val drawable = ContextCompat.getDrawable(context, drawableResId)
    val width = (drawable!!.intrinsicWidth * scaleFactor).toInt()
    val height = (drawable.intrinsicHeight * scaleFactor).toInt()

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, width, height)
    drawable.draw(canvas)
    return bitmap
}

// Calculate bearing between two LatLng points
private fun calculateBearing(start: LatLng, end: LatLng): Float {
    val lat1 = Math.toRadians(start.latitude)
    val lat2 = Math.toRadians(end.latitude)
    val dLng = Math.toRadians(end.longitude - start.longitude)

    val y = Math.sin(dLng) * Math.cos(lat2)
    val x = Math.cos(lat1) * Math.sin(lat2) -
            Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLng)

    return Math.toDegrees(Math.atan2(y, x)).toFloat()
}