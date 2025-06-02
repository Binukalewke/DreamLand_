package com.example.movienew.components

import android.content.*
import android.os.BatteryManager
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.movienew.storage.LocalStorage
import kotlinx.coroutines.delay


object BatteryAlertState {
    var isEnabled = mutableStateOf(false)
}

@Composable
fun GlobalBatteryAlert() {
    val context = LocalContext.current
    var showLowBattery by remember { mutableStateOf(false) }

    LaunchedEffect(showLowBattery) {
        if (showLowBattery) {
            delay(5000)
            showLowBattery = false
        }
    }
    DisposableEffect(BatteryAlertState.isEnabled.value) {
        if (!BatteryAlertState.isEnabled.value) return@DisposableEffect onDispose {}

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                val percent = if (level >= 0 && scale > 0) (level * 100) / scale else -1

                Log.d("BatteryBroadcast", "Battery %: $percent")
                showLowBattery = percent in 1..15
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    AnimatedVisibility(
        visible = showLowBattery,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp) // 👈 Moves entire banner down
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = Color.Red,
                shape = MaterialTheme.shapes.medium
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        "Low Battery!",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }


}

@Composable
fun ProfileBatteryLevel(
    batteryVisible: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var batteryLevel by remember { mutableStateOf(0) }

    // Read battery level from broadcast
    DisposableEffect(Unit) {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (level >= 0 && scale > 0) {
                    batteryLevel = (level * 100) / scale
                }
            }
        }

        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    val batteryColor = when (batteryLevel) {
        in 0..15 -> Color.Red
        in 16..40 -> Color(0xFFFFA000)
        in 41..75 -> Color(0xFF4CAF50)
        else -> Color(0xFF1E88E5)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.BatteryFull,
                contentDescription = "Battery Icon",
                tint = batteryColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Battery Level", style = MaterialTheme.typography.bodyMedium)

                LinearProgressIndicator(
                    progress = batteryLevel / 100f,
                    color = batteryColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .padding(top = 4.dp)
                )

                Text(
                    text = "$batteryLevel%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = batteryColor,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = batteryVisible,
                onCheckedChange = { onToggle(it) }
            )
        }
    }
}




