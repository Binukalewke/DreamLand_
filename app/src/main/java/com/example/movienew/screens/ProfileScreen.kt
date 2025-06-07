package com.example.movienew.screens



import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.movienew.R
import com.example.movienew.bottomnavigation.BottomNav
import com.example.movienew.components.AmbientLightState
import com.example.movienew.components.BatteryAlertState
import com.example.movienew.components.ProfileBatteryLevel
import com.example.movienew.components.ProfilePicture
import com.example.movienew.data.UserSession
import com.example.movienew.data.UserSession.email
import com.example.movienew.ui.theme.Blue
import com.example.movienew.ui.theme.errorLight
import com.example.movienew.storage.LocalStorage
import com.google.firebase.auth.FirebaseAuth


@Composable
fun ProfileScreen(navController: NavController, isDarkMode: Boolean, onToggleTheme: () -> Unit) {
    val context = LocalContext.current
    val email = LocalStorage.getEmail(context) ?: "default"
    var imageUri by remember { mutableStateOf<String?>(null) }

    // State for switches
    var batteryVisible by remember { mutableStateOf(false) }
    var ambientVisible by remember { mutableStateOf(false) }
    var logoutMessage by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(false) }

    // Load states on screen entry
    LaunchedEffect(Unit) {
        batteryVisible = LocalStorage.loadShowBattery(context, email)
        BatteryAlertState.isEnabled.value = batteryVisible

        ambientVisible = LocalStorage.loadShowAmbient(context, email)
        AmbientLightState.isEnabled = ambientVisible

        imageUri = LocalStorage.loadProfileImage(context)
    }

    BottomNav(currentTab = "Profile", navController = navController) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PROFILE",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Blue
                )
                IconButton(onClick = {
                    navController.navigate("edit_profile")
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.edit),
                        contentDescription = "Edit Profile",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                ProfilePicture(
                    imageUri = imageUri,
                    onImageUriChange = { newUri -> imageUri = newUri }
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text(
                    text = UserSession.username ?: "Guest",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                ProfileOption(R.drawable.help, "Help and Support") {
                    navController.navigate("help")
                }

                ProfileOption(R.drawable.logout, "Logout") {
                    logoutMessage = true
                }

                ProfileSwitch(R.drawable.notifications, "Notifications", notificationsEnabled) {
                    notificationsEnabled = !notificationsEnabled
                }

                ProfileSwitch(R.drawable.darkmode, "Dark mode", isDarkMode) {
                    onToggleTheme()
                }

                if (batteryVisible) {

                    ProfileBatteryLevel(batteryVisible) {
                        batteryVisible = it
                        LocalStorage.saveShowBattery(context, email, it)
                        BatteryAlertState.isEnabled.value = it
                    }
                } else {

                    ProfileSwitch(
                        icon = Icons.Filled.BatteryFull,
                        title = "Show Battery Alert",
                        isChecked = batteryVisible
                    ) {
                        batteryVisible = true
                        LocalStorage.saveShowBattery(context, email, true)
                        BatteryAlertState.isEnabled.value = true
                    }
                }


                ProfileSwitch(
                    icon = Icons.Filled.LightMode,
                    title = "Show Ambient Light Alert",
                    isChecked = ambientVisible
                ) {
                    ambientVisible = !ambientVisible
                    LocalStorage.saveShowAmbient(context, email, ambientVisible)
                    AmbientLightState.isEnabled = ambientVisible
                }
            }
        }

        if (logoutMessage) {
            AlertDialog(
                onDismissRequest = { logoutMessage = false },
                title = { Text("Logout") },
                text = { Text("Do you wish to logout?", color = errorLight) },
                confirmButton = {
                    TextButton(onClick = {
                        UserSession.username = null
                        UserSession.email = null
                        UserSession.password = null
                        FirebaseAuth.getInstance().signOut()
                        LocalStorage.clearCredentials(context)
                        LocalStorage.setLoggedOut(context, true)

                        logoutMessage = false
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { logoutMessage = false }) {
                        Text("No")
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileOption(icon: Int, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            title,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ProfileSwitch(
    icon: Any, // Accepts Int (drawable ID) or ImageVector
    title: String,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (icon) {
            is Int -> Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
            is ImageVector -> Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.weight(1f))

        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() }
        )
    }
}
