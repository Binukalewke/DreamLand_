package com.example.movienew.storage

import android.content.Context
import android.content.SharedPreferences

object LocalStorage {
    private const val PREF_NAME = "user_credentials"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"
    private const val KEY_USERNAME = "username"

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

    fun isUserLoggedInLocally(context: Context): Boolean {
        val email = LocalStorage.getEmail(context)
        val password = LocalStorage.getPassword(context)
        return !email.isNullOrBlank() && !password.isNullOrBlank()
    }

    fun getUsername(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USERNAME, null)
    }


}
