package com.example.movienew.screens

import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.movienew.R
import com.example.movienew.data.BookmarkManager
import com.example.movienew.data.NetworkHelper
import com.example.movienew.model.Movie
import com.example.movienew.model.Review
import com.example.movienew.storage.LocalStorage
import com.example.movienew.ui.theme.Blue
import com.example.movienew.ui.theme.staryellow
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

@Composable
fun ViewScreen(
    movieTitle: String,
    moviePoster: String,
    movieRating: Double,
    movieDescription: String,
    navController: NavController
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val backgroundColor = MaterialTheme.colorScheme.background

    // Determine if poster is a URL or local drawable
    val isUrl = moviePoster.startsWith("/") || moviePoster.startsWith("http")
    val posterUrl = "https://image.tmdb.org/t/p/w500$moviePoster"

    val movie = remember {
        Movie(
            title = movieTitle,
            posterName = moviePoster,
            rating = movieRating,
            description = movieDescription,
            type = "",
            category = ""
        )
    }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    val posterHeight = if (isPortrait) 500.dp else 1100.dp
    val gradientHeight = if (isPortrait) 500.dp else 1100.dp
    val gradientStartY = if (isPortrait) 700f else 900f

    var isBookmarked by remember {
        mutableStateOf(BookmarkManager.getBookmarks().contains(movie))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = if (isUrl)
                    rememberAsyncImagePainter(posterUrl)
                else
                    painterResource(
                        id = context.resources.getIdentifier(
                            moviePoster,
                            "drawable",
                            context.packageName
                        )
                    ),
                contentDescription = "Movie Poster",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(posterHeight)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gradientHeight)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, backgroundColor),
                            startY = gradientStartY
                        )
                    )
            )

            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    painter = painterResource(R.drawable.back2),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            IconButton(
                onClick = {
                    isBookmarked = !isBookmarked
                    if (isBookmarked) {
                        BookmarkManager.addBookmark(context, movie)
                    } else {
                        BookmarkManager.removeBookmark(movie)
                    }
                },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) Color(0xFFFFD700) else Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = movieTitle,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Blue,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "★", fontSize = 18.sp, color = staryellow)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = String.format("%.1f", movieRating), fontSize = 16.sp, color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Plot Overview",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = Color.LightGray
            )

            Text(
                text = Uri.decode(movieDescription),
                fontSize = 16.sp,
                color = Color(0xFF444444),
                textAlign = TextAlign.Justify,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            ReviewSection(movieTitle)
        }
    }
}

@Composable
fun ReviewSection(movieTitle: String) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val username = LocalStorage.getUsername(context) ?: "Anonymous"

    var showDialog by rememberSaveable { mutableStateOf(false) }  // state managment
    var reviews by remember { mutableStateOf(listOf<Review>()) }

    LaunchedEffect(Unit) {
        firestore.collection("reviews")
            .document(movieTitle)
            .collection("reviews")
            .get()
            .addOnSuccessListener { snapshot ->
                reviews = snapshot.documents.mapNotNull { it.toObject<Review>() }
            }
    }

    Text(
        text = "Behind the Popcorn", // or put Audience reviews
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.secondary
    )

    Spacer(modifier = Modifier.height(12.dp))

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (reviews.isEmpty()) {
                Text(
                    text = "No reviews yet. Be the first to review!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                reviews.forEach { review ->
                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        // Profile icon and username
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile Icon",
                                modifier = Modifier.size(28.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = review.username,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color(0xFF37474F)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Rating stars
                        Row {
                            for (i in 1..5) {
                                Icon(
                                    imageVector = if (i <= review.rating.toInt()) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Star $i",
                                    tint = if (i <= review.rating.toInt()) Color(0xFFFFD700) else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Posted date
                        Text(
                            text = "Posted on ${review.date ?: "Unknown date"}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Review text
                        Text(
                            text = review.text,
                            fontSize = 14.sp,
                            color = Color(0xFF555555),
                            textAlign = TextAlign.Justify
                        )
                    }
                }
            }
        }
    }

    // Write a Review button
    Button(
        onClick = {
            if (NetworkHelper.isOnline(context)) {
                showDialog = true
            } else {
                Toast.makeText(context, "You're offline. Cannot add a review.", Toast.LENGTH_SHORT).show()
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Blue)
    ) {
        Text("Write a Review")
    }


    if (showDialog) {
        AddReview(
            movieTitle = movieTitle,
            username = username,
            onDismiss = { showDialog = false },
            onSubmit = { newText, newRating ->
                val newReview = Review(
                    username = username,
                    text = newText,
                    rating = newRating,
                    date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                )
                firestore.collection("reviews")
                    .document(movieTitle)
                    .collection("reviews")
                    .add(newReview)
                reviews = reviews + newReview
                showDialog = false
            }
        )
    }
}


@Composable
fun AddReview(
    movieTitle: String,
    username: String,
    onDismiss: () -> Unit,
    onSubmit: (String, Float) -> Unit
) {
    var text by rememberSaveable { mutableStateOf("") }
    var rating by rememberSaveable { mutableStateOf(0f) }
    val context = LocalContext.current

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(0.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Write a Review", fontSize = 22.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Your review...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Rating", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 24.dp),
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

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        if (text.isBlank()) {
                            Toast.makeText(context, "Please write a review", Toast.LENGTH_SHORT).show()
                        } else {
                            onSubmit(text, rating)
                            onDismiss()
                        }
                    }) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}





