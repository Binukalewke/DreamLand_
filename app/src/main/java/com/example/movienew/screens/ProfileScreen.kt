package com.example.movienew.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.movienew.R
import com.example.movienew.bottomnavigation.BottomNav
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

    LaunchedEffect(Unit) {
        val loaded = LocalStorage.loadProfileImage(context)
        Log.d("ProfileScreen", "Loaded saved profile image URI: $loaded")
        imageUri = loaded
    }



    var editCredentials by remember { mutableStateOf(false) }
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

                IconButton(onClick = { editCredentials = true }) {
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
    var username by remember { mutableStateOf(UserSession.username ?: "") }
    var email by remember { mutableStateOf(UserSession.email ?: "") }
    var password by remember { mutableStateOf(UserSession.password ?: "") }

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Edit Profile") },
        text = {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (!NetworkHelper.isOnline(context)) {
                    Toast.makeText(context, "Cannot update credentials while offline", Toast.LENGTH_SHORT).show()
                    return@TextButton
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
                            onSaveSuccess()
                        }
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
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
    icon: Int,
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
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() }
        )
    }
}
