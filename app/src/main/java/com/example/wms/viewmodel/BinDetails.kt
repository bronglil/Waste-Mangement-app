package com.example.wms.viewmodel

data class BinDetails(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val status: Int,
    val lastUpdated: String,
    val sensorData: String
)