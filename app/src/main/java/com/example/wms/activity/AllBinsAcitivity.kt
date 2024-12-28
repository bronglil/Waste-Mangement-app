package com.example.wms.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wms.data.network.RetrofitInstance
import com.example.wms.model.Bin
import com.example.wms.ui.NavigationMenu
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AllBinsActivity : ComponentActivity() {
    private val TAG = "AllBinsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                BinListScreen()
            }
        }
    }

    @Composable
    fun MyApp(content: @Composable () -> Unit) {
        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }

    @Composable
    fun BinListScreen() {
        var binList by remember { mutableStateOf<List<Bin>?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf("") }
        var activeScreen by remember { mutableStateOf("Home") }

        // Fetch data on screen load
        LaunchedEffect(Unit) {
            fetchBinList { result, error ->
                binList = result
                errorMessage = error ?: ""
                isLoading = false
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (binList != null) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(binList!!) { bin ->
                            BinRow(bin)
                        }
                    }
                } else {
                    Text(
                        text = "Error: $errorMessage",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            // Bottom Navigation Menu
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(0.dp)
            ) {
                NavigationMenu(activeScreen) { navigateTo ->
                    activeScreen = navigateTo
                    when (navigateTo) {
                        "Home" -> {
                            // Stay on current screen
                        }
                        "Profile" -> {
                            startActivity(Intent(this@AllBinsActivity, UserProfileActivity::class.java))
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun BinRow(bin: Bin) {
        val statusColor = when {
            bin.status >= 80 -> Color.Red
            bin.status >= 50 -> Color.Yellow
            else -> Color.Green
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable {
                    val intent = Intent(this@AllBinsActivity, SingleBinDetailsActivity::class.java)
                    intent.putExtra("binId", bin.id.toInt()) // Pass bin ID to the details activity
                    startActivity(intent)
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Bin ID: ${bin.id}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${bin.status}%",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Last Updated: ${bin.lastUpdated.formatDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6F)
                )
            }
        }
    }

    // Extension function for formatting the date and time
    fun String.formatDate(): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", java.util.Locale.getDefault())
            val date = inputFormat.parse(this)
            if (date != null) outputFormat.format(date) else this
        } catch (e: Exception) {
            this
        }
    }

    private fun fetchBinList(callback: (List<Bin>?, String?) -> Unit) {
        RetrofitInstance.api.getAllBins().enqueue(object : Callback<List<Bin>> {
            override fun onResponse(call: Call<List<Bin>>, response: Response<List<Bin>>) {
                if (response.isSuccessful) {
                    callback(response.body(), null)
                } else {
                    Log.e(TAG, "Request failed: ${response.code()}")
                    callback(null, "Request failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Bin>>, t: Throwable) {
                Log.e(TAG, "Error accessing API: ${t.message}")
                callback(null, "Error: ${t.message}")
            }
        })
    }
}
