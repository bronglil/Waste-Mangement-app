package com.example.wms.api

data class ApiResponse(
    val success: Boolean,
    val message: String
)

data class LoginResponse(
    val userId: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val contactNumber: String = "",
    val email: String = "",
    val userRole: String = "",
    val token: String = "",
    val message: String? = null,
    val errors: Map<String, String>? = null
)


data class SignUpResponse(
    val  message: String,
    val errors: Map<String, String>? = null,
    val firstName: String,
    val lastName: String,
    val contactNumber: String,
    val email: String,
    val userRole: String,
    val token: String
)

data class UserData(
    val firstName: String,
    val lastName: String,
    val contactNumber: String,
    val email: String,
    val userRole: String,
    val token: String
)