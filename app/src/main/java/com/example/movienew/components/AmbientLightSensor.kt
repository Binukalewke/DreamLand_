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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movienew.storage.LocalStorage
import kotlinx.coroutines.delay


object AmbientLightState {
    var isEnabled by mutableStateOf(false)
}

// Prevent repeated message on rotation
private val shownLightMessages = mutableSetOf<String>()


@Composable
fun GlobalAmbientAlert() {
    val context = LocalContext.current
    val showAmbient = remember { mutableStateOf(LocalStorage.loadShowAmbientLightAlert(context)) }
    var lightLevel by remember { mutableStateOf(-1f) }
    var message by remember { mutableStateOf<String?>(null) }

    // Reload toggle
    LaunchedEffect(Unit) {
        showAmbient.value = LocalStorage.loadShowAmbientLightAlert(context)
    }

    // Auto-hide after 5 seconds
    LaunchedEffect(message) {
        if (message != null) {
            delay(5000)
            message = null
        }
    }

    // Sensor setup
    DisposableEffect(showAmbient.value) {
        if (!showAmbient.value) return@DisposableEffect onDispose {}

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                lightLevel = event?.values?.get(0) ?: -1f

                // New message based on light level
                val newMessage = when (lightLevel) {
                    in 0f..20f -> "🌙 Low light detected. Dark mode is recommended."
                    in 21f..100f -> "🌒 Dim light: Lower brightness for comfort."
                    in 301f..1000f -> "☀ Bright light detected. Increase screen brightness."
                    in 1001f..Float.MAX_VALUE -> "🌞 Very bright light! Avoid screen glare."
                    else -> null
                }

                // ✅ Only show new messages once per app session
                if (newMessage != null && newMessage !in shownLightMessages) {
                    message = newMessage
                    shownLightMessages.add(newMessage)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        onDispose { sensorManager.unregisterListener(listener) }
    }


    // Styled banner
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(
                        color = Color(0xFF212121), // Deep dark gray
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(vertical = 14.dp, horizontal = 16.dp)
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


