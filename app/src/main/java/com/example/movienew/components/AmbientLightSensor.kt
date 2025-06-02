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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.movienew.storage.LocalStorage
import kotlinx.coroutines.delay


object AmbientLightState {
    var isEnabled by mutableStateOf(false)
}

@Composable
fun GlobalAmbientAlert() {
    val context = LocalContext.current
    val showAmbient = remember { mutableStateOf(LocalStorage.loadShowAmbientLightAlert(context)) }
    var lightLevel by remember { mutableStateOf(-1f) }
    var message by remember { mutableStateOf<String?>(null) }

    // Reload toggle on composition
    LaunchedEffect(Unit) {
        showAmbient.value = LocalStorage.loadShowAmbientLightAlert(context)
    }

    // Auto-hide message after 5 seconds
    LaunchedEffect(message) {
        if (message != null) {
            delay(5000)
            message = null
        }
    }

    DisposableEffect(showAmbient.value) {
        if (!showAmbient.value) return@DisposableEffect onDispose {}

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                lightLevel = event?.values?.get(0) ?: -1f

                message = when (lightLevel) {
                    in 0f..20f -> "Low light detected. Consider enabling dark mode."
                    in 21f..100f -> "Dim environment. Lower brightness to reduce eye strain."
                    in 301f..1000f -> "☀Bright room detected. Consider increasing brightness."
                    in 1001f..Float.MAX_VALUE -> "🌞 Very bright light detected. Avoid screen glare."
                    else -> null
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF424242))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message ?: "",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

