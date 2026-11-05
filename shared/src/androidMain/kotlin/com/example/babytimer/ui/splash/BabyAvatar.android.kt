package com.example.babytimer.ui.splash

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
actual fun BabyAvatar(photoUri: String?, modifier: Modifier) {
    if (photoUri != null && !photoUri.startsWith("content://")) {
        AsyncImage(
            model = photoUri,
            contentDescription = "Baby Photo",
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
