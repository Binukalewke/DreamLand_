package com.example.movienew.api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class TmdbViewModel : ViewModel() {
    var movies = mutableStateListOf<TmdbMovie>()
        private set

    private val apiKey = "5ec20f3bf21ed6b5530c81c1927bce31"

    init {
        fetchPopularMovies()
    }

    private fun fetchPopularMovies() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getPopularMovies(apiKey)
                movies.clear()
                movies.addAll(response.results)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
