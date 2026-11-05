package com.example.babytimer.ui.splash

import androidx.compose.runtime.Composable

@Composable
actual fun rememberImagePicker(onImagePicked: (String?) -> Unit): () -> Unit {
    return {
        // iOS image picker stub
    }
}
