package com.example.movienew.storage

import android.content.Context
import android.content.SharedPreferences


object LocalStorage {
    private const val PREF_NAME = "user_credentials"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"
    private const val KEY_USERNAME = "username"
    private const val KEY_DARK_MODE = "is_dark_mode"
    private const val KEY_LOGGED_OUT = "logged_out"
    private const val KEY_PROFILE_IMAGE = "profile_image"
    private const val KEY_SHOW_BATTERY = "show_battery"
    private const val KEY_SHOW_AMBIENT_LIGHT_ALERT = "show_ambient_light_alert"





    fun saveCredentials(context: Context, email: String, password: String, username: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, password)
            putString(KEY_USERNAME, username)
            apply()
        }
    }

    fun clearCredentials(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            remove(KEY_EMAIL)
            remove(KEY_PASSWORD)
            remove(KEY_USERNAME)
            remove(KEY_LOGGED_OUT)
            apply()
        }
    }



    fun setLoggedOut(context: Context, loggedOut: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_LOGGED_OUT, loggedOut).apply()
    }

    fun isLoggedOut(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_LOGGED_OUT, false)
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


    fun saveProfileImage(context: Context, uri: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PROFILE_IMAGE, uri).apply()
    }

    fun loadProfileImage(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val uri = prefs.getString(KEY_PROFILE_IMAGE, null)
        return uri
    }

    fun saveShowBattery(context: Context, value: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SHOW_BATTERY, value).apply()
    }
    fun loadShowBattery(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SHOW_BATTERY, true)
    }


    fun saveShowAmbientLightAlert(context: Context, value: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SHOW_AMBIENT_LIGHT_ALERT, value).apply()
    }

    fun loadShowAmbientLightAlert(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SHOW_AMBIENT_LIGHT_ALERT, false)
    }







}
