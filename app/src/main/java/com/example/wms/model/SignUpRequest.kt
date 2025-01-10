package com.example.wms.model

data class SignUpRequest(
    val firstName: String,
    val lastName: String,
    val contactNumber: String,
    val email: String,
    val password: String,
    val userRole: String = "driver",
    val userStatus: String = "pending"
)

