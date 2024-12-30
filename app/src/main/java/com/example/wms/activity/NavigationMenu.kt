package com.example.wms.activity

import EditProfileScreen
import MainScreen
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.wms.utils.UserPreferences
import com.example.wms.viewmodel.BinViewModel
import com.example.wms.viewmodel.UserProfileViewModel

@Composable
fun SetupNavGraph(navController: NavHostController, viewModel: UserProfileViewModel, binViewModel: BinViewModel) {
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(navController, binViewModel)
        }
        composable("Bins") {
            EnhancedBinListScreen(navController)
        }
        composable("profile") {
            UserProfileScreen(
                viewModel = viewModel,
                onEditClick = {
                    navController.navigate("editProfile")
                },
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }
        composable(
            "binDetails/{binId}",
            arguments = listOf(navArgument("binId") { type = NavType.LongType })
        ) { backStackEntry ->
            val binId = backStackEntry.arguments?.getLong("binId") ?: return@composable
            BinDetailsScreen(binId = binId.toInt(), onBack = { navController.popBackStack() })
        }
        composable("editProfile") {
            val context = LocalContext.current // Get the context
            val userId = UserPreferences.getUserId(context) // Retrieve userId from preferences
            val userProfile = viewModel.userProfile.collectAsState().value

            userProfile?.let { user ->
                val completeUserProfile = user.copy(
                    firstName = user.firstName ?: "",
                    lastName = user.lastName ?: "",
                    email = user.email ?: "",
                    contactNumber = user.contactNumber ?: "",
                    role = user.role ?: "USER",
                    token = user.token ?: ""
                )

                EditProfileScreen(
                    userProfile = completeUserProfile,
                    userId = userId,
                    context = context,
                    onSaveClick = { updatedUser ->
                        viewModel.updateUserProfile(userId, updatedUser, context)
                        navController.popBackStack()
                    },
                    onCancelClick = { navController.popBackStack() }
                )
            }
        }
    }
}

