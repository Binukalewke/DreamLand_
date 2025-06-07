package com.example.movienew.data

import android.util.Log
import com.example.movienew.model.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL

object GitHubJsonFetcher {
    suspend fun fetchMoviesFromGitHub(): List<Movie> = withContext(Dispatchers.IO) {
        val jsonUrl =
            "https://raw.githubusercontent.com/Binukalewke/Dream_Land_Json/refs/heads/main/movies.json"
        val response = URL(jsonUrl).readText() // still runs on IO thread safely

        Log.d("GitHubFetch", "Movies JSON fetched from GitHub successfully")

        val movieList = mutableListOf<Movie>()
        val jsonArray = JSONArray(response)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val movie = Movie(
                title = obj.getString("title"),
                description = obj.getString("description"),
                rating = obj.getDouble("rating"),
                posterName = obj.getString("posterName"),
                category = obj.optString("category", ""),
                type = obj.optString("type", "")
            )

            movieList.add(movie)
        }

        return@withContext movieList
    }
}
