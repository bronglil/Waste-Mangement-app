package com.example.wms.model

data class SignUpRequest(
    val firstName: String,
    val lastName: String,
    val mobile: String,
    val email: String,
    val password: String
)