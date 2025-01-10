package com.example.wms.activity

import NavigationMenu
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.navigation.NavHostController
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.wms.api.UserData
import com.example.wms.utils.UserPreferences
import com.example.wms.viewmodel.UserProfileViewModel
import kotlinx.coroutines.launch


@Composable
fun UserProfileScreen(viewModel: UserProfileViewModel, onEditClick: (UserData) -> Unit, onBackClick: () -> Unit, navController: NavHostController) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val userId = UserPreferences.getUserId(context)
        viewModel.fetchUserProfile(userId)
    }

    val userProfile by viewModel.userProfile.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()


    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                DrawerContent(
                    userProfile = userProfile,
                    onEditClick = {
                        userProfile?.let { onEditClick(it) }
                        scope.launch { drawerState.close() }
                    },
                    onLogoutClick = {
                        scope.launch {
                            drawerState.close()
                            UserPreferences.clearUserData(context)
                            val intent = Intent(context, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            context.startActivity(intent)
                        }
                    }
                )
            }
        }
    ) {
        ProfileContent(
            userProfile = userProfile,
            errorMessage = errorMessage,
            onBackClick = onBackClick,
            onMenuClick = { scope.launch { drawerState.open() } },
            navController = navController
        )
    }

}

@Composable
private fun DrawerContent(
    userProfile: UserData?,
    onEditClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Drawer Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 24.dp, horizontal = 16.dp),
               contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User Info
                userProfile?.let { user ->
                    Text(
                        text = "${user.firstName} ${user.lastName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Drawer Items
        DrawerNavigationItem(
            icon = Icons.Filled.Edit,
            label = "Edit Profile",
            onClick = onEditClick
        )

        DrawerNavigationItem(
            icon = Icons.Filled.ExitToApp,
            label = "Logout",
            onClick = onLogoutClick,
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun DrawerNavigationItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = tint
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    userProfile: UserData?,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    navController: NavHostController
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(2.dp)
    ) {
        var activeScreen by remember { mutableStateOf("Bins") }
        when {
            errorMessage != null -> {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            userProfile == null -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(bottom = 64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Bar with Back and Menu buttons
                    TopAppBar(
                        title = { Text("Profile") },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = onMenuClick) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp)) // Space below top bar

                    ProfileLogo()

                    Spacer(modifier = Modifier.height(24.dp)) // Space before details

                    ProfileDetails(userProfile)
                }
            }
        }

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
private fun ProfileLogo() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = "Profile Logo",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun ProfileDetails(userProfile: UserData) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // First Row of Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailCard(
                modifier = Modifier.weight(1f),
                title = "First Name",
                value = userProfile.firstName
            )
            DetailCard(
                modifier = Modifier.weight(1f),
                title = "Last Name",
                value = userProfile.lastName
            )
        }

        // Second Row of Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailCard(
                modifier = Modifier.weight(1f),
                title = "Email",
                value = userProfile.email
            )
            DetailCard(
                modifier = Modifier.weight(1f),
                title = "Contact",
                value = userProfile.contactNumber
            )
        }

        // Single Card for Role
        userProfile.role?.let {
            DetailCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Role",
                value = it,
                elevation = 8.dp
            )
        }
    }
}

@Composable
private fun DetailCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    elevation: Dp = 4.dp
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}