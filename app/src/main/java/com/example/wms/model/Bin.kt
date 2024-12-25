package com.example.wms.model

// Bin.kt
data class Bin(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val status: Int,
    val lastUpdated: String,
    val sensorData: String
)