package com.example.wms.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.wms.viewmodel.UserProfileViewModel
import com.example.wms.ui.theme.WMSTheme
import com.example.wms.viewmodel.BinViewModel

class MainActivity : ComponentActivity() {
    private val userProfileViewModel: UserProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WMSTheme {
                MainNavHost(userProfileViewModel)
            }
        }
    }

    @Composable
    fun MainNavHost(viewModel: UserProfileViewModel) {
        val navController = rememberNavController()
        SetupNavGraph(navController, viewModel, binViewModel = BinViewModel())
    }
}