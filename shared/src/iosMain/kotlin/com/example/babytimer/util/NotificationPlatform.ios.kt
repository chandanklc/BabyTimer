package com.example.babytimer.util

import androidx.compose.runtime.Composable

@Composable
actual fun rememberNotificationPermissionLauncher(onGranted: () -> Unit): () -> Unit {
    return { onGranted() }
}
