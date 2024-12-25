package com.example.wms.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wms.ui.NavigationMenu

class UserProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                ProfileScreen()
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
    fun ProfileScreen() {
        var activeScreen by remember { mutableStateOf("Profile") } // Set default active screen

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp), // Remove padding
                verticalArrangement = Arrangement.SpaceBetween // Space between elements
            ) {
                // Main content above the navigation menu
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Allow this column to fill available space
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Spacing between elements
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Profile", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    // User information sections
                    Text("User Name: John Doe", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Email: john.doe@example.com", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { signOut() }) {
                        Text("Sign Out")
                    }
                }

                // Bottom Navigation Menu
                NavigationMenu(activeScreen) { navigateTo ->
                    activeScreen = navigateTo
                    when (navigateTo) {
                        "Home" -> {
                            startActivity(Intent(this@UserProfileActivity, SingleBinDetailsActivity::class.java))
                        }
                        "Profile" -> {
                            // Stay on current screen
                        }
                    }
                }
            }
        }
    }

    private fun signOut() {
        // Implement sign-out logic, e.g., clearing user session or token
        finish() // Close the ProfileActivity
    }
}