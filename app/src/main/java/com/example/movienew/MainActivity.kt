package com.example.movienew

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.movienew.data.BookmarkManager
import com.example.movienew.data.DataSource
import com.example.movienew.data.NetworkStatusListener
import com.example.movienew.data.UserSession
import com.example.movienew.model.Movie
import com.example.movienew.ui.theme.MovieNewTheme
import com.example.movienew.screens.BookmarkScreen
import com.example.movienew.screens.ProfileScreen
import com.example.movienew.screens.SignUpScreen
import com.example.movienew.screens.LoginScreen
import com.example.movienew.screens.SearchScreen
import com.example.movienew.screens.EditProfileScreen
import com.example.movienew.screens.ViewScreen
import com.example.movienew.storage.LocalStorage
import com.example.movienew.ui.theme.Blue
import com.example.movienew.ui.theme.beige
import com.example.movienew.ui.theme.staryellow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    //private val firestore = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()


        setContent {
            //  Add a dark mode toggle state
            val context = LocalContext.current
            var isDarkMode by rememberSaveable { mutableStateOf(LocalStorage.loadDarkMode(context)) }

            LaunchedEffect(isDarkMode) {
                LocalStorage.saveDarkMode(context, isDarkMode)
            }


            //  Use your existing MovieNewTheme, just pass the darkTheme state
            MovieNewTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NetworkStatusListener()

                    val navController = rememberNavController()

                    //  Slightly enhance AppNavigation to support toggling
                    AppNavigation(
                        auth = auth,
                        navController = navController,
                        isDarkMode = isDarkMode,
                        toggleDarkMode = { isDarkMode = !isDarkMode }
                    )
                }
            }
        }

    }

    @Composable
    fun AppNavigation(
        auth: FirebaseAuth,
        navController: NavHostController,
        isDarkMode: Boolean,
        toggleDarkMode: () -> Unit
    ) {
        val context = LocalContext.current

        var isInitialized by remember { mutableStateOf(false) }

        // 👇 First initialize user session + bookmarks
        LaunchedEffect(Unit) {
            if (auth.currentUser != null) {
                // 🔥 1. Load bookmarks
                BookmarkManager.loadBookmarksFromFirestore()

                // 🔥 2. Load user details from Firestore
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val firestore = FirebaseFirestore.getInstance()
                    val document = firestore.collection("users").document(userId).get().await()
                    if (document.exists()) {
                        UserSession.username = document.getString("username") ?: "Guest"
                        UserSession.email = document.getString("email") ?: ""
                        // No password stored in Firestore for security, only local
                        UserSession.password = LocalStorage.getPassword(context) ?: ""
                    }
                }
            } else if (LocalStorage.isUserLoggedInLocally(context)) {
                // Local offline login
                UserSession.username = LocalStorage.getUsername(context) ?: ""
                UserSession.email = LocalStorage.getEmail(context) ?: ""
                UserSession.password = LocalStorage.getPassword(context) ?: ""

                Toast.makeText(context, "Offline Mode", Toast.LENGTH_SHORT).show()

            }
            isInitialized = true
        }

        if (!isInitialized) {
            // While loading
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // ✅ After initialization
            val startDestination = if (auth.currentUser != null || LocalStorage.isUserLoggedInLocally(context)) {
                "main"
            } else {
                "signup"
            }

            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable("signup") { SignUpScreen(navController, auth) }
                composable("login") { LoginScreen(navController, auth) }
                composable("main") { MainScreen(navController, isDarkMode, toggleDarkMode) }
                composable("profile") { ProfileScreen(navController, isDarkMode, toggleDarkMode) }
                composable("editProfile") { EditProfileScreen(navController) }
                composable(
                    "movieDetails/{movieTitle}/{moviePoster}/{movieRating}/{movieDescription}",
                    arguments = listOf(
                        navArgument("movieTitle") { type = NavType.StringType },
                        navArgument("moviePoster") { type = NavType.StringType },
                        navArgument("movieRating") { type = NavType.StringType },
                        navArgument("movieDescription") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val movieTitle = backStackEntry.arguments?.getString("movieTitle") ?: ""
                    val moviePoster = backStackEntry.arguments?.getString("moviePoster") ?: ""
                    val movieRating = backStackEntry.arguments?.getString("movieRating")?.toDoubleOrNull() ?: 0.0
                    val movieDescription = backStackEntry.arguments?.getString("movieDescription") ?: ""

                    ViewScreen(
                        movieTitle = movieTitle,
                        moviePoster = moviePoster,
                        movieRating = movieRating,
                        movieDescription = movieDescription,
                        navController = navController
                    )
                }
            }
        }
    }




// Main Screen
@Composable
fun MainScreen(navController: NavController,isDarkMode: Boolean,onToggleTheme: () -> Unit) {
    var selectedBottomTab by remember { mutableStateOf("Home") }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(selectedBottomTab) {
                selectedBottomTab = it
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Crossfade(
                targetState = selectedBottomTab,
                animationSpec = tween(300)
            ) { screen ->
                when (screen) {
                    "Home" -> HomeScreen(navController)
                    "Bookmark" -> BookmarkScreen(navController)
                    "Profile" -> ProfileScreen(navController,isDarkMode = isDarkMode,onToggleTheme)
                    "Search" -> SearchScreen(navController)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(selectedTab: String, onTabSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = beige)
            .padding(12.dp)
            .height(60.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(Icons.Default.Home, "Home", selectedTab == "Home") {
            onTabSelected("Home")
        }
        BottomNavItem(Icons.Default.Bookmark, "Bookmark", selectedTab == "Bookmark") {
            onTabSelected("Bookmark")
        }
        BottomNavItem(Icons.Default.Person, "Profile", selectedTab == "Profile") {
            onTabSelected("Profile")
        }
        BottomNavItem(Icons.Default.Search, "Search", selectedTab == "Search") {
            onTabSelected("Search")
        }
    }
}

@Composable
fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Blue else Color.Gray
        )
        Text(
            text = label,
            color =     if (isSelected) Blue else Color.Gray
        )
    }
}






    @Composable
    fun MovieCard(movie: Movie, navController: NavController) {
        val context = LocalContext.current

        // Dynamically resolve the drawable ID from the posterName string
        val resId = remember(movie.posterName) {
            context.resources.getIdentifier(movie.posterName, "drawable", context.packageName)
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .width(160.dp)
                .clickable {
                    navController.navigate(
                        "movieDetails/${movie.title}/${movie.posterName}/${movie.rating}/${movie.description}"
                    )
                },
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = resId),
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
                    Text(
                        text = "★",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Yellow,
                        fontSize = 20.sp,
                        modifier = Modifier.alignByBaseline()
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = movie.rating.toString(),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        fontSize = 18.sp,
                        modifier = Modifier.alignByBaseline()
                    )
                }
            }
        }
    }




    // Home Screen
    @Composable
    fun HomeScreen(navController: NavController) {
        var selectedTab by remember { mutableStateOf("Movie") }
        val context = LocalContext.current

        // Load banner movies
        val bannerMovies = DataSource().loadBannerMovies(context)

        // Track the current banner index
        var currentBannerIndex by remember { mutableStateOf(0) }

        // Handle auto-switching of banner
        LaunchedEffect(Unit) {
            while (true) {
                kotlinx.coroutines.delay(3000L) // 3 seconds
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.movie_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Dream Land",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = Blue
                )
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
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
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
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge,color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .height(3.dp)
                    .width(40.dp)
                    .background(Color.Red)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}






    @Composable
    fun MovieSection(navController: NavController) {
        val context = LocalContext.current
        val allMovies = remember { DataSource().loadMovies(context) }

        val newMovies = allMovies.filter { it.type == "movie" && it.category == "new" }
        val popularMovies = allMovies.filter { it.type == "movie" && it.category == "popular" }

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
            items(popularMovies) { movie ->
                MovieCard(movie, navController)
            }
        }
    }


    @Composable
    fun AnimeSection(navController: NavController) {
        val context = LocalContext.current
        val allMovies = remember { DataSource().loadMovies(context) }

        val newAnimeList = allMovies.filter { it.type == "anime" && it.category == "new" }
        val popularAnimeList = allMovies.filter { it.type == "anime" && it.category == "popular" }

        Text(
            text = "New Anime",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(newAnimeList) { anime ->
                MovieCard(anime, navController)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Popular Anime",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(popularAnimeList) { anime ->
                MovieCard(anime, navController)
            }
        }
    }}


