package com.example.movienew.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.movienew.R
import com.example.movienew.ui.theme.Blue

@Composable
fun HelpAndSupportScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back Button",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { navController.popBackStack() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Help & Support",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Blue
                )
            }
        }

        Text(
            text = stringResource(R.string.faq_heading),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        HelpItem(
            question = stringResource(R.string.faq_1_q),
            answer = stringResource(R.string.faq_1_a)
        )
        HelpItem(
            question = stringResource(R.string.faq_2_q),
            answer = stringResource(R.string.faq_2_a)
        )
        HelpItem(
            question = stringResource(R.string.faq_3_q),
            answer = stringResource(R.string.faq_3_a)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.contact_heading),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        ContactItem(icon = Icons.Default.Email, text = stringResource(R.string.contact_email))
        ContactItem(icon = Icons.Default.Phone, text = stringResource(R.string.contact_phone))
        ContactItem(icon = Icons.Default.Language, text = stringResource(R.string.contact_website))
    }
}

@Composable
fun HelpItem(question: String, answer: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = question,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = answer,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ContactItem(icon: ImageVector, text: String) {
    val context = LocalContext.current
    var showPermissionDeniedToast by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$text")
            }
            context.startActivity(intent)
        } else {
            showPermissionDeniedToast = true
        }
    }

    if (showPermissionDeniedToast) {
        Toast.makeText(context, "Call permission denied", Toast.LENGTH_SHORT).show()
        showPermissionDeniedToast = false
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                when (icon) {
                    Icons.Default.Phone -> {
                        launcher.launch(Manifest.permission.CALL_PHONE)
                    }

                    Icons.Default.Email -> {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$text")
                        }
                        context.startActivity(emailIntent)
                    }

                    Icons.Default.Language -> {
                        val webIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(text)
                        }
                        context.startActivity(webIntent)
                    }
                }
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = when (icon) {
                Icons.Default.Phone -> "Phone: $text"
                Icons.Default.Email -> "Email: $text"
                else -> text
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

