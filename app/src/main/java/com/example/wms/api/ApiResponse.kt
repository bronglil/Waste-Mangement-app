package com.example.wms.api

data class ApiResponse(
    val success: Boolean, // This must match the API response field
    val message: String
)