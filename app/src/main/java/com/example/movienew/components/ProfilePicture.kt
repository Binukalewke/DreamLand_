package com.example.movienew.components

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.movienew.R
import com.example.movienew.data.NetworkHelper
import com.example.movienew.storage.LocalStorage
import com.google.firebase.auth.FirebaseAuth
import java.io.File

@Composable
fun ProfilePicture(
    imageUri: String?,
    onImageUriChange: (String) -> Unit
) {
    val context = LocalContext.current
    var showImagePickerDialog by remember { mutableStateOf(false) }

    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: "default"
    val profileFile = File(context.filesDir, "${currentUserUid}_profile.jpg")

    var actualUri by remember { mutableStateOf<String?>(null) }

    // Load once on composition
    LaunchedEffect(currentUserUid) {
        if (profileFile.exists()) {
            actualUri = Uri.fromFile(profileFile).toString()
        } else {
            actualUri = null
        }
    }

    val navigateToAppSettings = {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes != null) {
                profileFile.writeBytes(bytes)
                val savedUri = Uri.fromFile(profileFile).toString()
                actualUri = savedUri
                onImageUriChange(savedUri)
                LocalStorage.saveProfileImage(context, savedUri)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            profileFile.outputStream().use { out ->
                it.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            val savedUri = Uri.fromFile(profileFile).toString()
            actualUri = savedUri
            onImageUriChange(savedUri)
            LocalStorage.saveProfileImage(context, savedUri)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            val permanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                context as android.app.Activity,
                android.Manifest.permission.CAMERA
            )
            if (permanentlyDenied) {
                Toast.makeText(context, "Camera permission permanently denied. Please enable it from settings.", Toast.LENGTH_LONG).show()
                navigateToAppSettings()
            } else {
                Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
            android.Manifest.permission.READ_MEDIA_IMAGES
        else android.Manifest.permission.READ_EXTERNAL_STORAGE

        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            val permanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                context as android.app.Activity,
                permission
            )
            if (permanentlyDenied) {
                Toast.makeText(context, "Gallery permission permanently denied. Please enable it from settings.", Toast.LENGTH_LONG).show()
                navigateToAppSettings()
            } else {
                Toast.makeText(context, "Gallery permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val painter = if (!actualUri.isNullOrBlank()) {
            rememberAsyncImagePainter(model = actualUri)
        } else {
            painterResource(id = R.drawable.profile2)
        }

        Image(
            painter = painter,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .clickable {
                    if (NetworkHelper.isOnline(context)) {
                        showImagePickerDialog = true
                    } else {
                        Toast.makeText(context, "You're offline. Cannot change profile picture.", Toast.LENGTH_SHORT).show()
                    }
                }
        )
    }

    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = { Text("Select Image Source") },
            text = { Text("Choose where to get your new profile picture from:") },
            confirmButton = {
                TextButton(onClick = {
                    showImagePickerDialog = false
                    val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                        android.Manifest.permission.READ_MEDIA_IMAGES
                    else android.Manifest.permission.READ_EXTERNAL_STORAGE

                    if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        galleryPermissionLauncher.launch(permission)
                    } else {
                        galleryLauncher.launch("image/*")
                    }
                }) {
                    Text("Gallery")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImagePickerDialog = false
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    } else {
                        cameraLauncher.launch(null)
                    }
                }) {
                    Text("Camera")
                }
            }
        )
    }
}
