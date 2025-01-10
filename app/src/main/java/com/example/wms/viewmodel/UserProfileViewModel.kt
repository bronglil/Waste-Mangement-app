package com.example.wms.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wms.api.UserData
import com.example.wms.data.network.RetrofitInstance
import com.example.wms.utils.ToastUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {

    private val _userProfile = MutableStateFlow<UserData?>(null)
    val userProfile: StateFlow<UserData?> = _userProfile

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchUserProfile(userId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getDriverDetails(userId)
                val userData = UserData(
                    firstName = response.firstName,
                    lastName = response.lastName,
                    email = response.email,
                    contactNumber = response.contactNumber,
                    userRole = response.role,
                    token = response.token,
                    role = response.role
                )
                _userProfile.value = userData
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch user data: ${e.message}"
                e.printStackTrace() // Log the error
            }
        }
    }

    fun updateUserProfile(userId: Int, updatedUser: UserData, context: Context) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.updateUserProfile(userId, updatedUser)
                if (response.isSuccessful) {
                    response.body()?.let { loginResponse ->
                        _userProfile.value = updatedUser.copy(
                            firstName = loginResponse.firstName ?: updatedUser.firstName,
                            lastName = loginResponse.lastName ?: updatedUser.lastName,
                            email = loginResponse.email ?: updatedUser.email,
                            contactNumber = loginResponse.contactNumber ?: updatedUser.contactNumber,
                            role = loginResponse.role ?: updatedUser.role,
                            token = loginResponse.token ?: updatedUser.token
                        )
                        ToastUtils.showToastAtTop(context, "Profile updated successfully!")
                    } ?: ToastUtils.showToastAtTop(context, "Failed to parse response.")
                } else {
                    ToastUtils.showToastAtTop(context, "Error: ${response.errorBody()?.string() ?: response.message()}")
                }
            } catch (e: Exception) {
                ToastUtils.showToastAtTop(context, "Exception: ${e.message}")
            }
        }
    }

}
