package com.example.movienew.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.movienew.R
import com.example.movienew.data.BookmarkManager
import com.example.movienew.model.Movie
import com.example.movienew.ui.theme.Blue
import com.example.movienew.ui.theme.beige
import com.example.movienew.ui.theme.staryellow

@Composable
fun ViewScreen(
    movieTitle: String,
    moviePoster: String,
    movieRating: Double,
    movieDescription: String,
    navController: NavController
) {
    val context = LocalContext.current
    val backgroundColor = MaterialTheme.colorScheme.background

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

    val posterResId = remember(moviePoster) {
        context.resources.getIdentifier(moviePoster, "drawable", context.packageName)
    }

    var isBookmarked by remember {
        mutableStateOf(BookmarkManager.getBookmarks().contains(movie))
    }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    val posterHeight = if (isPortrait) 500.dp else 1100.dp
    val gradientHeight = if (isPortrait) 500.dp else 1100.dp
    val gradientStartY = if (isPortrait) 700f else 900f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(posterResId),
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
                        BookmarkManager.addBookmark(movie)
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

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = movieTitle,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge,
                color = Blue,
                textAlign = TextAlign.Start,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "★",
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.bodyLarge,
                    color = staryellow
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$movieRating",
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        DescriptionSection(movieDescription)
        ReviewSection()
    }
}

@Composable
fun DescriptionSection(movieDescription: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = beige),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Story",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 26.sp),
                color = Color(0xFF1A237E),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = movieDescription,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 26.sp),
                textAlign = TextAlign.Justify,
                color = Color(0xFF333333),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun ReviewSection() {
    var showDialog by remember { mutableStateOf(false) }
    var reviewText by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = beige),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Write a Review",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 26.sp),
                color = Color(0xFF1A237E),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.padding(top = 16.dp),
            ) {
                Text(text = "Add Review")
            }
        }
    }

    if (showDialog) {
        AddReview(
            reviewText = reviewText,
            onReviewChange = { reviewText = it },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun AddReview(
    reviewText: String,
    onReviewChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                IconButton(onClick = { onDismiss() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.back2),
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Review", style = MaterialTheme.typography.headlineMedium)

                Spacer(modifier = Modifier.height(16.dp))

                var newReview by remember { mutableStateOf(reviewText) }

                OutlinedTextField(
                    value = newReview,
                    onValueChange = { newReview = it },
                    label = { Text("Your Review") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onReviewChange(newReview)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            }
        }
    }
}
