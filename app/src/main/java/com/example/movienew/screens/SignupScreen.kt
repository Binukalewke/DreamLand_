package com.example.movienew.screens

import android.widget.Toast
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
import com.example.movienew.storage.LocalStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignUpScreen(navController: NavController, auth: FirebaseAuth) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirmPassword by rememberSaveable { mutableStateOf(false) }
    var navigateToLogin by remember { mutableStateOf(false) }



    val firestore = FirebaseFirestore.getInstance()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(id = R.drawable.movie_logo),
            contentDescription = null,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                    Icon(
                        imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showConfirmPassword) "Hide password" else "Show password"
                    )
                }
            }
        )

        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))

            LaunchedEffect(errorMessage) {
                kotlinx.coroutines.delay(3000)
                errorMessage = null
            }
        }


        Button(
            onClick = {
                if (username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    errorMessage = "All fields are required."
                } else if (password != confirmPassword) {
                    errorMessage = "Passwords do not match."
                } else if (password.length < 6) {
                    errorMessage = "Password must be at least 6 characters."
                } else {
                    isLoading = true
                    val emailLower = email.trim().lowercase()
                    val usernameLower = username.trim().lowercase()

                    // Check if username exists
                    firestore.collection("users")
                        .whereEqualTo("username", usernameLower)
                        .get()
                        .addOnSuccessListener { usernameSnapshot ->
                            if (!usernameSnapshot.isEmpty) {
                                isLoading = false
                                errorMessage = "Username already taken."
                            } else {
                                // Check if email already used in users collection
                                firestore.collection("users")
                                    .whereEqualTo("email", emailLower)
                                    .get()
                                    .addOnSuccessListener { emailSnapshot ->
                                        if (!emailSnapshot.isEmpty) {
                                            isLoading = false
                                            errorMessage = "This email already has an account."
                                        } else {
                                            // Step 3: Create Firebase Auth account
                                            auth.createUserWithEmailAndPassword(emailLower, password)
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        val userId = task.result?.user?.uid ?: return@addOnCompleteListener
                                                        val user = hashMapOf(
                                                            "userId" to userId,
                                                            "username" to usernameLower,
                                                            "email" to emailLower
                                                        )
                                                        firestore.collection("users").document(userId).set(user)
                                                            .addOnSuccessListener {
                                                                LocalStorage.saveCredentials(
                                                                    context,
                                                                    emailLower,
                                                                    password,
                                                                    usernameLower
                                                                )
                                                                Toast.makeText(context, "Sign up successful!", Toast.LENGTH_SHORT).show()
                                                                navigateToLogin = true
                                                            }
                                                            .addOnFailureListener { e ->
                                                                isLoading = false
                                                                errorMessage = "Failed to save user data: ${e.message}"
                                                            }
                                                    } else {
                                                        val firebaseError = task.exception?.message
                                                        isLoading = false
                                                        errorMessage = if (firebaseError?.contains("badly formatted", ignoreCase = true) == true) {
                                                            "Please enter a valid email address."
                                                        } else {
                                                            firebaseError ?: "Sign up failed."
                                                        }
                                                    }
                                                }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = "Failed to check email: ${e.message}"
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            errorMessage = "Failed to check username: ${e.message}"
                        }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading)
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            else
                Text("Sign Up")
        }

        Spacer(modifier = Modifier.weight(1f))
        Text("Already have an account?")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { navController.navigate("login") }, modifier = Modifier.fillMaxWidth()) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (navigateToLogin) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            navController.navigate("login") {
                popUpTo("signup") { inclusive = true }
            }
        }
    }

}
