package com.example.wms.utils

import android.content.Context

object UserPreferences {
    private const val PREFS_NAME = "user_prefs"

    fun getUserId(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt("userId", 0)
    }

    fun getToken(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("token", "") ?: ""
    }

    fun getFirstName(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("firstName", "") ?: ""
    }

    // Clear all stored user data
    fun clearUserData(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply() // Clear and commit changes
    }

}