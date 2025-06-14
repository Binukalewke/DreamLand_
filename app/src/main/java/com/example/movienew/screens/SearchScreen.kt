package com.example.movienew.screens

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.movienew.api.TmdbViewModel
import com.example.movienew.model.Movie
import com.example.movienew.ui.theme.lightblack
import com.example.movienew.viewmodel.MovieDataViewModel
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(navController: NavController) {
    val localViewModel: MovieDataViewModel = viewModel()
    val apiViewModel: TmdbViewModel = viewModel()
    val context = LocalContext.current

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Combine GitHub JSON + TMDb API movies
    val localMovies = localViewModel.remoteMovies
    val apiMovies = apiViewModel.movies.map {
        Movie(
            title = it.title,
            posterName = it.poster_path ?: "",
            rating = it.vote_average,
            description = it.overview,
            type = "movie",
            category = "popular"
        )
    }

    val allMovies = localMovies + apiMovies
    val filteredMovies = allMovies.filter {
        searchQuery.isEmpty() || it.title.startsWith(searchQuery, ignoreCase = true)
    }

    LaunchedEffect(searchQuery) {
        isLoading = true
        delay(500)
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search for a Movie or Anime") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).alpha(0.7f),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Search, contentDescription = "Search Icon")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(filteredMovies) { movie ->
                SearchMovieCard(movie = movie, navController = navController, context = context)
            }
        }
    }
}


@Composable
fun SearchMovieCard(movie: Movie, navController: NavController, context: Context) {
    val isUrl = movie.posterName.startsWith("/") || movie.posterName.startsWith("http")
    val poster = if (isUrl) {
        rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${movie.posterName}")
    } else {
        val resId = remember(movie.posterName) {
            context.resources.getIdentifier(movie.posterName, "drawable", context.packageName)
        }
        painterResource(id = resId)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = lightblack, shape = MaterialTheme.shapes.medium)
            .clickable {
                navController.navigate(
                    "movieDetails/${Uri.encode(movie.title)}/${Uri.encode(movie.posterName)}/${movie.rating}/${Uri.encode(movie.description)}"
                )
            }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = poster,
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .padding(end = 16.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "★",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Yellow,
                    modifier = Modifier.alignByBaseline()
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = movie.rating.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
}

