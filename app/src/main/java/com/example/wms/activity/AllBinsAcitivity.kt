package com.example.wms.activity

import NavigationMenu
import android.content.Intent
import android.icu.text.SimpleDateFormat
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wms.data.network.RetrofitInstance
import com.example.wms.model.Bin
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Animated Icon Container
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    CircleShape
                                )
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Title and Subtitle
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                "Smart Bins Monitor",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Real-time Waste Management",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        // Status Indicator
                        Row(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.Green, CircleShape)
                            )
                            Text(
                                "Live",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

//            // Search Bar (Optional)
//            SearchBar(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp, vertical = 8.dp),
//                query = "",
//                onQueryChange = { },
//                onSearch = { },
//                active = false,
//                onActiveChange = { },
//                placeholder = { Text("Search bins...") },
//                leadingIcon = {
//                    Icon(Icons.Default.Search, contentDescription = null)
//                },
//                colors = SearchBarDefaults.colors(
//                    containerColor = MaterialTheme.colorScheme.surface,
//                    dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
//                )
//            ) { }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedBinListScreen(navController: NavController) {
    var binList by remember { mutableStateOf<List<Bin>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var activeScreen by remember { mutableStateOf("Bins") }

    LaunchedEffect(Unit) {
        fetchBinList { result, error ->
            binList = result
            errorMessage = error ?: ""
            isLoading = false
        }
    }
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
                modifier = Modifier.fillMaxSize()
            ) {
            TopBar()

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else if (binList != null) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(binList!!) { bin ->
                        BinCard(
                            bin = bin,
                            onClick = {
                                navController.navigate("binDetails/${bin.id}") // Use navController
                            }
                        )
                    }
                }
            } else {
                ErrorDisplay(errorMessage)
            }
        }

        // Enhanced Navigation Menu
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            NavigationMenu(
                activeScreen = activeScreen,
                onNavigate = { screen ->
                    activeScreen = screen
                    when (screen) {
                        "Home" -> navController.navigate("main")
                        "Bins" -> navController.navigate("bins")
                        "Profile" -> navController.navigate("profile")
                    }
                }
            )
        }
    }
}

@Composable
fun BinCard(bin: Bin, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(160.dp)
            .clickable(onClick = onClick)
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Bin #${bin.id}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${bin.status}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = getStatusColor(bin.status),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Last Updated: ${bin.lastUpdated.formatDate()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Capacity",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = when {
                            bin.status >= 80 -> "Critical"
                            bin.status >= 50 -> "Warning"
                            else -> "Normal"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = getStatusColor(bin.status)
                    )
                }

                LinearProgressIndicator(
                    progress = bin.status / 100f,
                    color = getStatusColor(bin.status),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }
    }
}


@Composable
fun ErrorDisplay(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

private fun getStatusColor(status: Int): Color {
    return when {
        status >= 80 -> Color(0xFFE53935)  // Red
        status >= 50 -> Color(0xFFFFA726)  // Orange
        else -> Color(0xFF66BB6A)  // Green
    }
}

private fun String.formatDate(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(this)
        outputFormat.format(date ?: return this)
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
                    callback(null, "Request failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Bin>>, t: Throwable) {
                callback(null, "Error: ${t.message}")
            }
        })
    }

