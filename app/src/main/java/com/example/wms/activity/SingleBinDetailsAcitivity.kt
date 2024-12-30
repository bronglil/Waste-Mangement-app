package com.example.wms.activity

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wms.data.network.RetrofitInstance
import com.example.wms.viewmodel.BinDetails
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinDetailsScreen(binId: Int, onBack: () -> Unit) {
    var binDetails by remember { mutableStateOf<BinDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(binId) {
        fetchApiData(binId) { result, error ->
            binDetails = result
            errorMessage = error ?: ""
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Bin Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    binDetails?.let { bin ->
                        BinDetailsCard(bin)
                    } ?: errorMessage.takeIf { it.isNotEmpty() }?.let {
                        Text("Error: $it", color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun BinDetailsCard(bin: BinDetails) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text("Bin ID: ${bin.id}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            StatusBar(bin.status)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Last Updated: ${formatDate(bin.lastUpdated)}", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            val sensorDataDisplay = if (bin.sensorData.isEmpty()) {
                "EMPTY"
            } else {
                bin.sensorData
            }
            Text("Sensor Data: $sensorDataDisplay", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            LocationButton(bin.latitude, bin.longitude)
        }
    }
}

@Composable
fun StatusBar(status: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Fill Status: $status%", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .size(250.dp)
                .padding(12.dp)
                .wrapContentSize(Alignment.Center)
        ) {
            CircularProgressIndicator(
                progress = status / 100f,
                modifier = Modifier.size(240.dp),
                color = when {
                    status < 30 -> Color.Green
                    status < 70 -> Color.Yellow
                    else -> Color.Red
                },
                strokeWidth = 55.dp
            )
        }
    }
}

@Composable
fun LocationButton(latitude: Double, longitude: Double) {
    Button(
        onClick = { openMap(latitude, longitude) },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text("View on Map", color = Color.White)
    }
}

private fun openMap(latitude: Double, longitude: Double) {
    val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
    mapIntent.setPackage("com.google.android.apps.maps")
    // Note: You may want to handle the intent safely here
}

private fun formatDate(dateString: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val date = inputFormat.parse(dateString)
    return outputFormat.format(date ?: Date())
}

private fun fetchApiData(binId: Int, callback: (BinDetails?, String?) -> Unit) {
    RetrofitInstance.api.getBinDetails(binId).enqueue(object : Callback<BinDetails> {
        override fun onResponse(call: Call<BinDetails>, response: Response<BinDetails>) {
            if (response.isSuccessful) {
                callback(response.body(), null)
            } else {
                Log.e("API Request", "Request failed: ${response.code()}")
                callback(null, "Request failed: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<BinDetails>, t: Throwable) {
            Log.e("API Request", "Error accessing API: ${t.message}")
            callback(null, "Error: ${t.message}")
        }
    })
}