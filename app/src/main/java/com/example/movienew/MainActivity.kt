package com.example.movienew

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.movienew.components.GlobalAmbientAlert
import com.example.movienew.components.GlobalBatteryAlert
import com.example.movienew.data.BookmarkManager
import com.example.movienew.data.NetworkStatusListener
import com.example.movienew.data.UserSession
import com.example.movienew.screens.*
import com.example.movienew.storage.LocalStorage
import com.example.movienew.ui.theme.MovieNewTheme
import com.example.movienew.ui.theme.beige
import com.example.movienew.ui.theme.Blue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.movienew.screens.HomeScreen

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            val context = LocalContext.current
            var isDarkMode by rememberSaveable { mutableStateOf(LocalStorage.loadDarkMode(context)) }

            LaunchedEffect(isDarkMode) {
                LocalStorage.saveDarkMode(context, isDarkMode)
            }

            MovieNewTheme(darkTheme = isDarkMode) {
                // Wrap everything in a Box so GlobalBatteryAlert overlays the whole app
                Box(modifier = Modifier.fillMaxSize()) {

                    // Main app content
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        NetworkStatusListener()

                        AppNavigation(
                            auth = auth,
                            navController = navController,
                            isDarkMode = isDarkMode,
                            toggleDarkMode = { isDarkMode = !isDarkMode }
                        )
                    }

                    // This will float on top if battery is low
                    GlobalBatteryAlert()
                    GlobalAmbientAlert()

                }
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

        LaunchedEffect(Unit) {
            if (auth.currentUser != null) {
                BookmarkManager.loadBookmarksFromFirestore()
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val doc = FirebaseFirestore.getInstance().collection("users").document(userId).get().await()
                    if (doc.exists()) {
                        UserSession.username = doc.getString("username") ?: "Guest"
                        UserSession.email = doc.getString("email") ?: ""
                        UserSession.password = LocalStorage.getPassword(context) ?: ""
                    }
                }
            } else if (LocalStorage.isUserLoggedInLocally(context)) {
                UserSession.username = LocalStorage.getUsername(context) ?: ""
                UserSession.email = LocalStorage.getEmail(context) ?: ""
                UserSession.password = LocalStorage.getPassword(context) ?: ""
                Toast.makeText(context, "Offline Mode", Toast.LENGTH_SHORT).show()
            }
            isInitialized = true
        }

        if (!isInitialized) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        NavHost(navController = navController, startDestination = getStartDestination(auth, context)) {
            composable("signup") { SignUpScreen(navController, auth) }
            composable("login") { LoginScreen(navController, auth) }
            composable("main?tab={tab}", arguments = listOf(navArgument("tab") {
                defaultValue = "Home"
                type = NavType.StringType
            })) {
                val selectedTab = it.arguments?.getString("tab") ?: "Home"
                MainScreen(navController, selectedTab)
            }
            composable("profile") { ProfileScreen(navController, isDarkMode, toggleDarkMode) }
            composable("edit_Profile") { EditProfileScreen(navController) }
            composable("help") { HelpAndSupportScreen(navController) }
            composable(
                "movieDetails/{movieTitle}/{moviePoster}/{movieRating}/{movieDescription}",
                arguments = listOf(
                    navArgument("movieTitle") { type = NavType.StringType },
                    navArgument("moviePoster") { type = NavType.StringType },
                    navArgument("movieRating") { type = NavType.StringType },
                    navArgument("movieDescription") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                ViewScreen(
                    movieTitle = backStackEntry.arguments?.getString("movieTitle") ?: "",
                    moviePoster = backStackEntry.arguments?.getString("moviePoster") ?: "",
                    movieRating = backStackEntry.arguments?.getString("movieRating")?.toDoubleOrNull() ?: 0.0,
                    movieDescription = backStackEntry.arguments?.getString("movieDescription") ?: "",
                    navController = navController
                )
            }
        }
    }

    private fun getStartDestination(auth: FirebaseAuth, context: android.content.Context): String {
        return when {
            auth.currentUser != null -> "main?tab=Home"
            LocalStorage.isUserLoggedInLocally(context) && !LocalStorage.isLoggedOut(context) -> "main?tab=Home"
            LocalStorage.isLoggedOut(context) -> "login"
            else -> "signup"
        }
    }

    @Composable
    fun MainScreen(navController: NavController, initialTab: String) {
        var selectedTab by remember { mutableStateOf(initialTab) }

        Scaffold(
            bottomBar = {
                BottomNavigationBar(selectedTab) { selected ->
                    selectedTab = selected
                    if (selected == "Profile") {
                        navController.navigate("profile")
                    } else {
                        navController.navigate("main?tab=$selected") {
                            popUpTo("main?tab=$initialTab") { inclusive = true }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                Crossfade(targetState = selectedTab, animationSpec = tween(300)) { screen ->
                    when (screen) {
                        "Home" -> HomeScreen(navController)
                        "Bookmark" -> BookmarkScreen(navController)
                        "Search" -> SearchScreen(navController)
                    }
                }
            }
        }
    }

    @Composable
    fun BottomNavigationBar(currentTab: String, onTabSelected: (String) -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(beige)
                .padding(12.dp)
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(Icons.Default.Home, "Home", currentTab == "Home") { onTabSelected("Home") }
            BottomNavItem(Icons.Default.Bookmark, "Bookmark", currentTab == "Bookmark") { onTabSelected("Bookmark") }
            BottomNavItem(Icons.Default.Person, "Profile", currentTab == "Profile") { onTabSelected("Profile") }
            BottomNavItem(Icons.Default.Search, "Search", currentTab == "Search") { onTabSelected("Search") }
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
            modifier = Modifier.clickable { onClick() }.padding(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Blue else Color.Gray
            )
            Text(text = label, color = if (isSelected) Blue else Color.Gray)
        }
    }


