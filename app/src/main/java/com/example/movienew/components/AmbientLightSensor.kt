package com.example.movienew.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

object AmbientLightState {
    var isEnabled by mutableStateOf(false)
    var lastZoneShown: Int? = null
}

@Composable
fun GlobalAmbientAlert() {
    val context = LocalContext.current
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var currentZone by rememberSaveable { mutableStateOf<Int?>(null) }

    // Auto-hide after 5 seconds
    LaunchedEffect(message) {
        if (message != null) {
            delay(5000)
            message = null
        }
    }

    // Sensor logic
    DisposableEffect(AmbientLightState.isEnabled) {
        if (!AmbientLightState.isEnabled) return@DisposableEffect onDispose {}

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                val lux = event?.values?.get(0) ?: return

                val zone = when (lux) {
                    in 0f..20f -> 1
                    in 21f..100f -> 2
                    in 301f..1000f -> 3
                    in 1001f..Float.MAX_VALUE -> 4
                    else -> null
                }

                if (zone != null && zone != currentZone) {
                    currentZone = zone
                    AmbientLightState.lastZoneShown = zone
                    message = when (zone) {
                        1 -> "\uD83C\uDF19 Low light detected. Dark mode is recommended."
                        2 -> "\uD83C\uDF12 Dim light: Lower brightness for comfort."
                        3 -> "\u2600 Bright light detected. Increase screen brightness."
                        4 -> "\uD83C\uDF1E Very bright light! Avoid screen glare."
                        else -> null
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // Alert message UI
    AnimatedVisibility(visible = message != null, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF212121), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = message ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
