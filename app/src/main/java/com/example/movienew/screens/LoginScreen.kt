package com.example.movienew.screens

import android.content.Context
import android.net.ConnectivityManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.movienew.R
import com.example.movienew.data.BookmarkManager
import com.example.movienew.data.UserSession
import com.example.movienew.storage.LocalStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetworkInfo?.isConnectedOrConnecting == true
}

@Composable
fun LoginScreen(navController: NavController, auth: FirebaseAuth) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isOfflineLogin by remember { mutableStateOf(false) }

    val firestore = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.movie_logo),
                contentDescription = "Login Logo",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isOfflineLogin) {
                Text(
                    text = "You're in offline mode",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Email and Password cannot be empty."
                        return@Button
                    }

                    isLoading = true

                    if (!isOnline(context)) {
                        // Offline login
                        val savedEmail = LocalStorage.getEmail(context)
                        val savedPassword = LocalStorage.getPassword(context)

                        if (email == savedEmail && password == savedPassword) {
                            Toast.makeText(context, "Logged in offline", Toast.LENGTH_SHORT).show()
                            UserSession.email = email
                            UserSession.password = password
                            UserSession.username = LocalStorage.getUsername(context) ?: "Offline User"
                            isOfflineLogin = true
                            isLoading = false

                            LocalStorage.setLoggedOut(context, false)

                            navController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            errorMessage = "Offline login failed. Incorrect credentials."
                            isLoading = false
                        }
                        return@Button
                    }

                    // Firebase login
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid
                                if (userId != null) {
                                    firestore.collection("users").document(userId)
                                        .get()
                                        .addOnSuccessListener { document ->
                                            if (document.exists()) {
                                                val username = document.getString("username") ?: "Unknown"
                                                val emailFromDb = document.getString("email") ?: ""

                                                UserSession.username = username
                                                UserSession.email = emailFromDb
                                                UserSession.password = password
                                                isOfflineLogin = false

                                                // Save locally
                                                LocalStorage.saveCredentials(context, emailFromDb, password, username)
                                                LocalStorage.setLoggedOut(context, false)

                                                CoroutineScope(Dispatchers.IO).launch {
                                                    BookmarkManager.loadBookmarksFromFirestore()
                                                }

                                                navController.navigate("main") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            } else {
                                                errorMessage = "User data not found."
                                            }
                                        }
                                        .addOnFailureListener {
                                            errorMessage = "Failed to fetch user data."
                                        }
                                } else {
                                    errorMessage = "User ID not found."
                                }
                            } else {
                                val savedEmail = LocalStorage.getEmail(context)
                                val savedPassword = LocalStorage.getPassword(context)

                                if (email == savedEmail && password == savedPassword) {
                                    Toast.makeText(context, "Logged in offline", Toast.LENGTH_SHORT).show()
                                    UserSession.email = email
                                    UserSession.password = password
                                    UserSession.username = LocalStorage.getUsername(context) ?: "Offline User"
                                    isOfflineLogin = true

                                    LocalStorage.setLoggedOut(context, false)

                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    errorMessage = task.exception?.message ?: "Login failed."
                                }
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Login")
                }
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("Don't have an account?")
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { navController.navigate("signup") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Up")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
