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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    var directionsPolylinePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var isNavigating by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState()
    val destination = remember { LatLng(destinationLat, destinationLng) }

    val locationCallback = rememberUpdatedState(newValue = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                val newPosition = LatLng(location.latitude, location.longitude)
                vehiclePosition = newPosition

                if (isNavigating) {
                    // Update route when navigating
                    CoroutineScope(Dispatchers.Main).launch {
                        updateDirections(newPosition, destination) { points ->
                            directionsPolylinePoints = points
                        }
                    }

                    // Update camera to follow vehicle with bearing
                    if (!cameraPositionState.isMoving) {
                        cameraPositionState.position = CameraPosition.Builder()
                            .target(newPosition)
                            .zoom(18f)
                            .tilt(45f)
                            .bearing(calculateBearing(newPosition, destination))
                            .build()
                    }
                }
            }
        }
    })

    // Rest of your location permission code...

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                mapType = MapType.NORMAL
            )
        ) {
            // Draw navigation route
            if (directionsPolylinePoints.isNotEmpty() && isNavigating) {
                Polyline(
                    points = directionsPolylinePoints,
                    color = Color(0xFF2196F3),
                    width = 8f
                )
            }

            // Draw direct line when not navigating
            vehiclePosition?.let { position ->
                if (!isNavigating) {
                    Polyline(
                        points = listOf(position, destination),
                        color = Color(0x4D2196F3),
                        width = 4f
                    )
                }

                // Current location marker
                Marker(
                    state = MarkerState(position = position),
                    title = "Current Location",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                )
            }

            // Destination marker
            Marker(
                state = MarkerState(position = destination),
                title = "Destination",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
        }

        // Direction Button
        FloatingActionButton(
            onClick = { isNavigating = !isNavigating },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (isNavigating) Icons.Default.Close else Icons.Default.Send,
                contentDescription = if (isNavigating) "Stop Navigation" else "Start Navigation"
            )
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
                .apiKey("YOUR_API_KEY")
                .build()

            val result = DirectionsApi.newRequest(context)
                .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .mode(TravelMode.DRIVING)
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