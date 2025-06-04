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

private var hasShownInitialNetworkToast = false
@Composable
fun NetworkStatusListener() {
    val context = LocalContext.current
    val connectivityManager = remember {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    LaunchedEffect(Unit) {
        if (!hasShownInitialNetworkToast) {
            val isConnected = isNetworkConnected(connectivityManager)
            Toast.makeText(context, if (isConnected) "Online" else "Offline", Toast.LENGTH_SHORT).show()
            hasShownInitialNetworkToast = true
        }
    }

    DisposableEffect(Unit) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Toast.makeText(context, "Online", Toast.LENGTH_SHORT).show()
            }

            override fun onLost(network: Network) {
                Toast.makeText(context, "Offline", Toast.LENGTH_SHORT).show()
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}



// Helper function to check current internet status
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







