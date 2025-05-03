package com.example.movienew.storage

import android.content.Context
import android.content.SharedPreferences

object LocalStorage {
    private const val PREF_NAME = "user_credentials"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"
    private const val KEY_USERNAME = "username"
    private const val KEY_DARK_MODE = "is_dark_mode"

    fun saveCredentials(context: Context, email: String, password: String, username: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, password)
            putString(KEY_USERNAME, username)
            apply()
        }
    }

    fun getEmail(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_EMAIL, null)
    }

    fun getPassword(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PASSWORD, null)
    }

    fun getUsername(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USERNAME, null)
    }

    fun isUserLoggedInLocally(context: Context): Boolean {
        val email = getEmail(context)
        val password = getPassword(context)
        return !email.isNullOrBlank() && !password.isNullOrBlank()
    }

    // Save dark mode preference
    fun saveDarkMode(context: Context, isDarkMode: Boolean) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_MODE, isDarkMode).apply()
    }

    // Load dark mode preference
    fun loadDarkMode(context: Context): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false) // false = Light mode by default
    }
}
