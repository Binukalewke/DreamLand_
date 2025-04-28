package com.example.movienew.data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.movienew.model.Movie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object BookmarkManager {
    private val bookmarks = mutableStateListOf<Movie>()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun getBookmarks(): List<Movie> = bookmarks

    fun addBookmark(movie: Movie) {
        if (!bookmarks.any { it.title == movie.title }) {
            bookmarks.add(movie)
            saveToFirestore(movie)
        }
    }

    fun removeBookmark(movie: Movie) {
        bookmarks.removeAll { it.title == movie.title }
        removeFromFirestore(movie)
    }

    private fun saveToFirestore(movie: Movie) {
        val userId = auth.currentUser?.uid ?: return

        val movieMap = mapOf(
            "title" to movie.title,
            "posterName" to movie.posterName,
            "rating" to movie.rating,
            "description" to movie.description,
            "type" to movie.type,
            "category" to movie.category
        )

        firestore.collection("users")
            .document(userId)
            .collection("bookmarks")
            .document(movie.title) // Use title as the unique key
            .set(movieMap)
            .addOnFailureListener { e -> Log.e("BookmarkManager", "Error saving bookmark", e) }
    }

    private fun removeFromFirestore(movie: Movie) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .collection("bookmarks")
            .document(movie.title)
            .delete()
            .addOnFailureListener { e -> Log.e("BookmarkManager", "Error removing bookmark", e) }
    }

    suspend fun loadBookmarksFromFirestore() {
        val userId = auth.currentUser?.uid ?: return
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("bookmarks")
            .get()
            .await()

        bookmarks.clear()

        for (doc in snapshot.documents) {
            val title = doc.getString("title") ?: continue
            val posterName = doc.getString("posterName") ?: continue
            val rating = doc.getDouble("rating") ?: continue
            val description = doc.getString("description") ?: continue
            val type = doc.getString("type") ?: continue
            val category = doc.getString("category") ?: continue

            val movie = Movie(title, posterName, rating, description, type, category)
            bookmarks.add(movie)
        }
    }
}
