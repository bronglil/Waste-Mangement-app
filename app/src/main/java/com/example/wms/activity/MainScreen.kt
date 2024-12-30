// MainScreen.kt
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.wms.model.Bin
import com.example.wms.viewmodel.BinViewModel
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: BinViewModel
) {
    var activeScreen by remember { mutableStateOf("Main") }
    val bins by viewModel.bins.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Set the default camera position to France (Paris)
    val defaultLocation = LatLng(48.8566, 2.3522) // Paris, France
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 5f) // Set zoom level to 5
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationMenu(activeScreen) { navigateTo ->
                activeScreen = navigateTo
                when (navigateTo) {
                    "Home" -> navController.navigate("main")
                    "Profile" -> navController.navigate("profile")
                    "Bins" -> navController.navigate("Bins")
                }
            }
        },
        floatingActionButton = {
            if (!isLoading && error == null) {
                FloatingActionButton(
                    onClick = { viewModel.fetchBins() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh bins"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else -> {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = false)
                    ) {
                        bins.forEach { bin ->
                            MarkerInfoWindow(
                                state = MarkerState(LatLng(bin.latitude, bin.longitude)),
                                icon = getBinMarkerIcon(bin.status),
                                onClick = {
                                    navController.navigate("binDetails/${bin.id}")
                                    true
                                }
                            ) { marker ->
                                BinMarkerInfoWindow(bin = bin)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BinMarkerInfoWindow(bin: Bin) {
    Card(
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Bin ${bin.id}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Fullness: ${bin.status}%",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Last Updated: ${formatDateTime(bin.lastUpdated)}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    // Navigation will be handled by marker click
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Details")
            }
        }
    }
}

private fun getBinMarkerIcon(status: Int): BitmapDescriptor {
    val size = 100 // Size of the bitmap
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()

    // Draw the outer background circle
    paint.color = Color.LTGRAY
    paint.style = Paint.Style.FILL
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 5f, paint)

    // Draw the progress arc based on the status value
    paint.color = when {
        status < 30 -> Color.GREEN
        status < 70 -> Color.YELLOW
        else -> Color.RED
    }
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 15f
    val rect = android.graphics.RectF(10f, 10f, size - 10f, size - 10f)
    canvas.drawArc(rect, -90f, (status / 100f) * 360, false, paint)

    // Draw the inner circle for a clean look
    paint.color = Color.WHITE
    paint.style = Paint.Style.FILL
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 20f, paint)

    // Draw the text (status percentage) in the center
    paint.color = Color.BLACK
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 28f
    paint.isFakeBoldText = true
    canvas.drawText("$status%", size / 2f, size / 2f + 10f, paint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}


private fun formatDateTime(dateTime: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateTime)
        outputFormat.format(date ?: return dateTime)
    } catch (e: Exception) {
        dateTime
    }
}