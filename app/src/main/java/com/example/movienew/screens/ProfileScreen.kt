package com.example.movienew.screens


import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.movienew.R
import com.example.movienew.bottomnavigation.BottomNav
import com.example.movienew.components.AmbientLightState
import com.example.movienew.components.BatteryAlertState
import com.example.movienew.components.ProfileBatteryLevel
import com.example.movienew.components.ProfilePicture
import com.example.movienew.data.UserSession
import com.example.movienew.ui.theme.Blue
import com.example.movienew.ui.theme.errorLight
import com.example.movienew.data.NetworkHelper
import com.example.movienew.storage.LocalStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(navController: NavController, isDarkMode: Boolean, onToggleTheme: () -> Unit) {
    val context = LocalContext.current

    var imageUri by remember { mutableStateOf<String?>(null) }

    var batteryVisible by remember { mutableStateOf(LocalStorage.loadShowBattery(context)) }
    LaunchedEffect(Unit) {
        batteryVisible = LocalStorage.loadShowBattery(context)
        BatteryAlertState.isEnabled.value = batteryVisible
    }



    var ambientVisible by remember { mutableStateOf(LocalStorage.loadShowAmbientLightAlert(context)) }
    LaunchedEffect(Unit) {
        ambientVisible = LocalStorage.loadShowAmbientLightAlert(context)
        AmbientLightState.isEnabled = ambientVisible
    }





    LaunchedEffect(Unit) {
        val loaded = LocalStorage.loadProfileImage(context)
        imageUri = loaded
    }



    var editCredentials by rememberSaveable { mutableStateOf(false) } // state managment for the edit dialog
    var logoutMessage by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(false) }

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
                    editCredentials = true
                })
                {
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
                    ProfileBatteryLevel(
                        batteryVisible = batteryVisible,
                        onToggle = {
                            batteryVisible = it
                            LocalStorage.saveShowBattery(context, it)
                            BatteryAlertState.isEnabled.value = it
                        }
                    )
                } else {
                    ProfileSwitch(
                        icon = Icons.Filled.BatteryFull,
                        title = "Show Battery",
                        isChecked = batteryVisible
                    ) {
                        batteryVisible = true
                        LocalStorage.saveShowBattery(context, true)
                        BatteryAlertState.isEnabled.value = true
                    }
                }



                ProfileSwitch(
                    icon = Icons.Filled.LightMode,
                    title = "Show Ambient Light Alert",
                    isChecked = ambientVisible
                ) {
                    ambientVisible = !ambientVisible
                    LocalStorage.saveShowAmbientLightAlert(context, ambientVisible)
                    AmbientLightState.isEnabled = ambientVisible
                }






            }
        }




        if (editCredentials) {
            EditDialog(
                onDismiss = { editCredentials = false },
                onSaveSuccess = { editCredentials = false }
            )
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
                        com.example.movienew.storage.LocalStorage.clearCredentials(context)
                        com.example.movienew.storage.LocalStorage.setLoggedOut(context, true)

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
fun EditDialog(
    onDismiss: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    var username by rememberSaveable { mutableStateOf(UserSession.username ?: "") }
    var email by rememberSaveable { mutableStateOf(UserSession.email ?: "") }
    var password by rememberSaveable { mutableStateOf(UserSession.password ?: "") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val isOnline = NetworkHelper.isOnline(context)

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = if (isLandscape) Modifier.width(500.dp) else Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { if (isOnline) username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = !isOnline,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { if (isOnline) email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = !isOnline,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { if (isOnline) password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = !isOnline,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                if (!isOnline) {
                                    Toast.makeText(context, "You're offline. Cannot save changes.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                if (username.isBlank() || email.isBlank() || password.isBlank()) {
                                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                if (userId != null) {
                                    val updates = mapOf(
                                        "username" to username,
                                        "email" to email,
                                        "password" to password
                                    )

                                    firestore.collection("users").document(userId)
                                        .update(updates)
                                        .addOnSuccessListener {
                                            UserSession.username = username
                                            UserSession.email = email
                                            UserSession.password = password
                                            LocalStorage.saveCredentials(context, email, password, username)
                                            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                            onSaveSuccess()
                                        }
                                }
                            },
                            enabled = isOnline,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("Save", fontSize = 18.sp)
                        }

                        OutlinedButton(
                            onClick = { onDismiss() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Blue
                            ),
                            border = BorderStroke(2.dp, Blue)
                        ) {
                            Text("Cancel", fontSize = 18.sp)
                        }
                    }
                }
            }
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
