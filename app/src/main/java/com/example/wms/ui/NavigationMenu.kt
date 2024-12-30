package com.example.wms.ui


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
@Composable
fun NavigationMenu(activeScreen: String, onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth(), // No padding or margins
        horizontalArrangement = Arrangement.SpaceAround, // Space buttons evenly
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onNavigate("Home") },
            modifier = Modifier.weight(1f), // Equal weight for each button
            shape = RectangleShape, // Change shape to rectangle
            colors = ButtonDefaults.buttonColors(
                containerColor = if (activeScreen == "Home") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(Icons.Filled.Home, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Home", color = Color.White)
        }
        Button(
            onClick = { onNavigate("Bins") },
            modifier = Modifier.weight(1f), // Equal weight for each button
            shape = RectangleShape, // Change shape to rectangle
            colors = ButtonDefaults.buttonColors(
                containerColor = if (activeScreen == "Bins") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Bins", color = Color.White)
        }
        Button(
            onClick = { onNavigate("Profile") },
            modifier = Modifier.weight(1f), // Equal weight for each button
            shape = RectangleShape, // Change shape to rectangle
            colors = ButtonDefaults.buttonColors(
                containerColor = if (activeScreen == "Profile") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(Icons.Filled.Person, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Profile", color = Color.White)
        }
    }
}