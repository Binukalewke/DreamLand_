package com.example.movienew.screens

import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.movienew.R
import com.example.movienew.api.TmdbMovie
import com.example.movienew.api.TmdbViewModel
import com.example.movienew.data.DataSource
import com.example.movienew.data.NetworkHelper
import com.example.movienew.data.rememberUpdatedNetworkStatus
import com.example.movienew.model.Movie
import com.example.movienew.ui.theme.Blue
import kotlinx.coroutines.delay


@Composable
fun HomeScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf("Movie") }
    val context = LocalContext.current

    val bannerMovies = DataSource().loadBannerMovies(context)
    var currentBannerIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000L)
            currentBannerIndex = (currentBannerIndex + 1) % bannerMovies.size
        }
    }

    val currentBanner = bannerMovies[currentBannerIndex]
    val resId = remember(currentBanner.posterName) {
        context.resources.getIdentifier(currentBanner.posterName, "drawable", context.packageName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))


        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painter = painterResource(id = R.drawable.movie_logo), contentDescription = "App Logo", modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Dream Land", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = Blue)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = currentBanner.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().background(Color.LightGray)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center) {
                NavigationTab("Movie", selectedTab == "Movie") { selectedTab = "Movie" }
                NavigationTab("Anime", selectedTab == "Anime") { selectedTab = "Anime" }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == "Movie") {
                MovieSection(navController)
            } else {
                AnimeSection(navController)
            }
        }
    }
}

@Composable
fun NavigationTab(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp).clickable { onClick() }
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
        if (isSelected) {
            Box(modifier = Modifier.height(3.dp).width(40.dp).background(MaterialTheme.colorScheme.primary))
        }
    }
}

@Composable
fun MovieSection(navController: NavController) {
    val context = LocalContext.current
    val viewModel: TmdbViewModel = viewModel()
    val isOnline = rememberUpdatedNetworkStatus()

    val allLocalMovies = remember { DataSource().loadMovies(context) }
    val newMovies = allLocalMovies.filter { it.type == "movie" && it.category == "new" }
    val localPopularMovies = allLocalMovies.filter { it.type == "movie" && it.category == "popular" }
    val popularMovies = viewModel.movies

    // New Movies (local)
    Text(
        text = "New Movies",
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(newMovies) { movie ->
            MovieCard(movie, navController)
        }
    }

    Spacer(modifier = Modifier.height(35.dp))

    // Popular Movies (load popular movies form the local Json if Offline , if Online load API data)
    Text(
        text = "Popular Movies",
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        if (isOnline && popularMovies.isNotEmpty()) {
            items(popularMovies) { movie ->
                TmdbMovieCard(movie = movie, navController = navController)
            }
        } else {
            items(localPopularMovies) { movie ->
                MovieCard(movie = movie, navController = navController)
            }
        }
    }
}







@Composable
fun AnimeSection(navController: NavController) {
    val context = LocalContext.current
    val allMovies = remember { DataSource().loadMovies(context) }
    val newAnimeList = allMovies.filter { it.type == "anime" && it.category == "new" }
    val popularAnimeList = allMovies.filter { it.type == "anime" && it.category == "popular" }

    Text(text = "New Anime", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
        items(newAnimeList) { anime -> MovieCard(anime, navController) }
    }

    Spacer(modifier = Modifier.height(32.dp))

    Text(text = "Popular Anime", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
        items(popularAnimeList) { anime -> MovieCard(anime, navController) }
    }
}

@Composable
fun MovieCard(movie: Movie, navController: NavController) {
    val context = LocalContext.current
    val resId = remember(movie.posterName) {
        context.resources.getIdentifier(movie.posterName, "drawable", context.packageName)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.width(160.dp).clickable {
            navController.navigate("movieDetails/${movie.title}/${movie.posterName}/${movie.rating}/${movie.description}")
        },
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Image(painter = painterResource(id = resId), contentDescription = movie.title, contentScale = ContentScale.Crop, modifier = Modifier.height(200.dp).fillMaxWidth())
            Text(text = movie.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(8.dp, 2.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp, 2.dp)) {
                Text(text = "★", style = MaterialTheme.typography.bodyMedium, color = Color.Yellow, fontSize = 20.sp, modifier = Modifier.alignByBaseline())
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = movie.rating.toString(), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), fontSize = 18.sp, modifier = Modifier.alignByBaseline())
            }
        }
    }
}

@Composable
fun TmdbMovieCard(movie: TmdbMovie, navController: NavController) {
    val posterUrl = "https://image.tmdb.org/t/p/w500${movie.poster_path}"

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(160.dp)
            .clickable {
                navController.navigate(
                    "movieDetails/${Uri.encode(movie.title)}/${Uri.encode(movie.poster_path ?: "")}/${movie.vote_average}/${Uri.encode(movie.overview)}"
                )
            },
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(posterUrl),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
            )
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp, 2.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp, 2.dp)
            ) {
                Text(text = "★", style = MaterialTheme.typography.bodyMedium, color = Color.Yellow, fontSize = 20.sp, modifier = Modifier.alignByBaseline())
                Spacer(modifier = Modifier.width(4.dp))
                Text(String.format("%.1f", movie.vote_average), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), fontSize = 18.sp, modifier = Modifier.alignByBaseline())
            }
        }
    }
}
