package com.example.movienew.viewmodel

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movienew.data.GitHubJsonFetcher
import com.example.movienew.model.Movie
import kotlinx.coroutines.launch

class MovieDataViewModel : ViewModel() {
    var remoteMovies by mutableStateOf<List<Movie>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            try {
                remoteMovies = GitHubJsonFetcher.fetchMoviesFromGitHub()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // code to see in the logcat is the movies are loaded in the search screen from the github Json -- search for "MovieFetch" in the logcat
    init {
        viewModelScope.launch {
            try {
                remoteMovies = GitHubJsonFetcher.fetchMoviesFromGitHub()
                Log.d("MovieFetch", "Loaded ${remoteMovies.size} movies from GitHub")
            } catch (e: Exception) {
                Log.e("MovieFetch", "Failed to load data", e)
            }
        }
    }



}
