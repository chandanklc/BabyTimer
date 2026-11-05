package com.example.babytimer.ui.splash

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePicker(onImagePicked: (String?) -> Unit): () -> Unit
