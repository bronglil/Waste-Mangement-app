import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NavigationMenu(activeScreen: String, onNavigate: (String) -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1C1B1F))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationItem(
                icon = Icons.Filled.LocationOn,
                isSelected = activeScreen == "Home",
                onClick = { onNavigate("Home") }
            )
            NavigationItem(
                icon = Icons.Filled.Delete,
                isSelected = activeScreen == "Bins",
                onClick = { onNavigate("Bins") }
            )
            NavigationItem(
                icon = Icons.Filled.Person,
                isSelected = activeScreen == "Profile",
                onClick = { onNavigate("Profile") }
            )
        }
    }
}

@Composable
private fun NavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 40.dp, height = if (isSelected) 48.dp else 40.dp)
            .background(
                color = if (isSelected) Color(0xFF2D2D2D) else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .hoverable(
                interactionSource = remember { MutableInteractionSource() },
                enabled = true
            ),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}