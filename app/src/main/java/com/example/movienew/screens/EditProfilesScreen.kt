package com.example.movienew.screens

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.movienew.data.UserSession
import com.example.movienew.data.NetworkHelper
import com.example.movienew.storage.LocalStorage
import com.example.movienew.ui.theme.Blue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val isOnline = NetworkHelper.isOnline(context)
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    var username by rememberSaveable { mutableStateOf(UserSession.username ?: "") }
    var email by rememberSaveable { mutableStateOf(UserSession.email ?: "") }
    var password by rememberSaveable { mutableStateOf(UserSession.password ?: "") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "Edit Profile",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Blue,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { if (isOnline) username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = !isOnline,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { if (isOnline) email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = !isOnline,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { if (isOnline) password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = !isOnline,
            singleLine = true,
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
                            navController.popBackStack()
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

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue),
            border = BorderStroke(2.dp, Blue)
        ) {
            Text("Cancel", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

