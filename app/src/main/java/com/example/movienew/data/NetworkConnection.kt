package com.example.movienew.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private var hasShownInitialToast = false
private var currentStatus: Boolean? = null // Track current status to avoid duplicate toasts

@Composable
fun NetworkStatusListener() {
    val context = LocalContext.current

    val connectivityManager = remember {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    // Show initial toast only once on first app load
    LaunchedEffect(Unit) {
        if (!hasShownInitialToast) {
            val isConnected = isNetworkConnected(connectivityManager)
            currentStatus = isConnected
            Toast.makeText(context, if (isConnected) "Online" else "Offline", Toast.LENGTH_SHORT).show()
            hasShownInitialToast = true
        }
    }

    DisposableEffect(Unit) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (currentStatus != true) {
                    currentStatus = true
                    Toast.makeText(context, "Online", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onLost(network: Network) {
                if (currentStatus != false) {
                    currentStatus = false
                    Toast.makeText(context, "Offline", Toast.LENGTH_SHORT).show()
                }
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}

// Check current connection
private fun isNetworkConnected(connectivityManager: ConnectivityManager): Boolean {
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@Composable
fun rememberUpdatedNetworkStatus(): Boolean {
    val context = LocalContext.current
    val isConnected = remember { mutableStateOf(NetworkHelper.isOnline(context)) }

    DisposableEffect(Unit) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isConnected.value = true
            }

            override fun onLost(network: Network) {
                isConnected.value = false
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    return isConnected.value
}







