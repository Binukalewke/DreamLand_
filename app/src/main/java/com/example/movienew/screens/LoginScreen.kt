
package com.example.movienew.screens

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    var showPassword by rememberSaveable { mutableStateOf(false) }

    val firestore = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(painter = painterResource(id = R.drawable.movie_logo), contentDescription = null, modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showPassword) "Hide password" else "Show password"
                    )
                }
            }
        )

        Button(onClick = {
            if (email.isBlank() || password.isBlank()) {
                errorMessage = "Email and Password cannot be empty."
                return@Button
            }

            if (!isOnline(context)) {
                errorMessage = "Network error. Please connect to the internet."
                return@Button
            }

            isLoading = true
            val emailLower = email.lowercase().trim()

            auth.signInWithEmailAndPassword(emailLower, password)
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val username = document.getString("username") ?: "Unknown"
                                    val emailFromDb = document.getString("email") ?: emailLower

                                    UserSession.username = username
                                    UserSession.email = emailFromDb
                                    UserSession.password = password

                                    LocalStorage.saveCredentials(context, emailFromDb, password, username)
                                    LocalStorage.setLoggedOut(context, false)

                                    CoroutineScope(Dispatchers.IO).launch {
                                        BookmarkManager.loadBookmarksFromFirestore()
                                    }

                                    navController.navigate("main") { popUpTo("login") { inclusive = true } }
                                } else {
                                    errorMessage = "User data not found."
                                }
                            }.addOnFailureListener {
                                errorMessage = "Failed to fetch user data."
                            }
                        } else {
                            errorMessage = "User ID not found."
                        }
                    } else {
                        val msg = task.exception?.message?.lowercase() ?: ""
                        Log.e("FirebaseLogin", "Login failed: $msg")

                        errorMessage = when {
                            "no user record" in msg || "user doesn't exist" in msg || "no user corresponding" in msg ->
                                "User not found. Please sign up."
                            "password is invalid" in msg || "invalid credential" in msg || "auth credential is incorrect" in msg ->
                                "Incorrect password."
                            "badly formatted" in msg -> "Please enter a valid email address."
                            else -> "Login failed. Please check your credentials."
                        }
                    }
                }
        }, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Login")
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)

            LaunchedEffect(errorMessage) {
                kotlinx.coroutines.delay(3000)
                errorMessage = null
            }
        }


        Spacer(modifier = Modifier.weight(1f))
        Text("Don't have an account?")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { navController.navigate("signup") }, modifier = Modifier.fillMaxWidth()) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
