package com.example.movienew.data

import android.content.Context
import com.example.movienew.model.Movie
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DataSource {

    /**
     * Loads all movies (movies + anime) from local assets/movies.json.
     * This method is used regardless of whether it's a movie or anime.
     */
    fun loadMovies(context: Context): List<Movie> {
        return try {
            val json = context.assets.open("movies.json").bufferedReader().use { it.readText() }
            val gson = Gson()
            val type = object : TypeToken<List<Movie>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Fallback to an empty list on failure
        }
    }

    /**
     * Loads only new movies (type = "movie", category = "new").
     */
    fun loadNewMovies(context: Context): List<Movie> {
        return loadMovies(context).filter { it.type == "movie" && it.category == "new" }
    }

    /**
     * Loads only popular movies (type = "movie", category = "popular").
     */
    fun loadPopularMovies(context: Context): List<Movie> {
        return loadMovies(context).filter { it.type == "movie" && it.category == "popular" }
    }

    /**
     * Loads only new anime (type = "anime", category = "new").
     */
    fun loadNewAnime(context: Context): List<Movie> {
        return loadMovies(context).filter { it.type == "anime" && it.category == "new" }
    }

    /**
     * Loads only popular anime (type = "anime", category = "popular").
     */
    fun loadPopularAnime(context: Context): List<Movie> {
        return loadMovies(context).filter { it.type == "anime" && it.category == "popular" }
    }

    /**
     * Loads banner items (movies or anime with category = "banner").
     */
    fun loadBannerMovies(context: Context): List<Movie> {
        return loadMovies(context).filter { it.category == "banner" }
    }
}
