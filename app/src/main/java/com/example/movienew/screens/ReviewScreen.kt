package com.example.movienew.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.movienew.data.NetworkHelper
import com.example.movienew.model.Review
import com.example.movienew.storage.LocalStorage
import com.example.movienew.ui.theme.Blue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddReviewScreen(navController: NavController, movieTitle: String) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val username = LocalStorage.getUsername(context) ?: "Anonymous"

    var text by rememberSaveable { mutableStateOf("") }
    var rating by rememberSaveable { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "Write a Review",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Blue,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Your review...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Your Rating", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            for (i in 1..5) {
                IconButton(onClick = { rating = i.toFloat() }) {
                    Icon(
                        imageVector = if (i <= rating.toInt()) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        tint = if (i <= rating.toInt()) Color(0xFFFFD700) else Color.Gray,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (text.isBlank()) {
                    Toast.makeText(context, "Please write a review", Toast.LENGTH_SHORT).show()
                } else {
                    val newReview = Review(
                        username = username,
                        text = text,
                        rating = rating,
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    )

                    firestore.collection("reviews")
                        .document(movieTitle)
                        .collection("reviews")
                        .add(newReview)

                    Toast.makeText(context, "Review added!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue)
        ) {
            Text("Submit", fontSize = 18.sp)
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
