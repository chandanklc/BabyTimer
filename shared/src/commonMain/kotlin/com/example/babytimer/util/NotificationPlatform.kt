package com.example.babytimer.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberNotificationPermissionLauncher(onGranted: () -> Unit): () -> Unit
