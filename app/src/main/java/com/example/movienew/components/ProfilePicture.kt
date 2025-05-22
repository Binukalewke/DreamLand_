package com.example.movienew.components

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.movienew.R
import com.example.movienew.storage.LocalStorage
import java.io.File

@Composable
fun ProfilePicture(
    imageUri: String?,
    onImageUriChange: (String) -> Unit
) {
    val context = LocalContext.current
    var showImagePickerDialog by remember { mutableStateOf(false) }

    // Gallery Picker
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes != null) {
                val file = File(context.filesDir, "profile_image.jpg")
                file.writeBytes(bytes)

                val savedUri = Uri.fromFile(file).toString()

                onImageUriChange(savedUri)
                LocalStorage.saveProfileImage(context, savedUri)
            }
        }
    }

    // Camera Capture
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            val file = File(context.filesDir, "profile_image.jpg")
            val outputStream = file.outputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()

            val savedUri = Uri.fromFile(file).toString()

            onImageUriChange(savedUri)
            LocalStorage.saveProfileImage(context, savedUri)
        }
    }

    // UI
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val painter = if (!imageUri.isNullOrBlank()) {
            rememberAsyncImagePainter(model = imageUri)
        } else {
            painterResource(id = R.drawable.profile2)
        }

        Image(
            painter = painter,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .clickable { showImagePickerDialog = true }
        )
    }

    // Picker Dialog
    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = { Text("Select Image Source") },
            text = { Text("Choose where to get your new profile picture from:") },
            confirmButton = {
                TextButton(onClick = {
                    showImagePickerDialog = false
                    galleryLauncher.launch("image/*")
                }) {
                    Text("Gallery")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImagePickerDialog = false
                    cameraLauncher.launch(null)
                }) {
                    Text("Camera")
                }
            }
        )
    }
}
