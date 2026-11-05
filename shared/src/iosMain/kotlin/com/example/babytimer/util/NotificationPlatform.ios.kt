package com.example.babytimer.util

import androidx.compose.runtime.Composable
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

@Composable
actual fun rememberNotificationPermissionLauncher(onGranted: () -> Unit): () -> Unit {
    return {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, _ ->
            if (granted) {
                onGranted()
            }
        }
    }
}

actual fun schedulePlatformAlarm(uniqueName: String, durationSeconds: Long, message: String) {
    val center = UNUserNotificationCenter.currentNotificationCenter()
    val content = UNMutableNotificationContent().apply {
        setTitle("BabyTimer Alert 🚨")
        setBody(message)
        setSound(UNNotificationSound.defaultSound)
    }
    val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
        timeInterval = durationSeconds.toDouble(),
        repeats = false
    )
    val request = UNNotificationRequest.requestWithIdentifier(
        identifier = uniqueName,
        content = content,
        trigger = trigger
    )
    center.addNotificationRequest(request, withCompletionHandler = null)
}

actual fun cancelPlatformAlarm(uniqueName: String) {
    val center = UNUserNotificationCenter.currentNotificationCenter()
    center.removePendingNotificationRequestsWithIdentifiers(listOf(uniqueName))
}

actual fun showPlatformRunningNotification(title: String, message: String, notificationId: Int) {
    // iOS handles ongoing running timers via in-app UI state; notifications are delivered upon trigger.
}

actual fun cancelPlatformNotification(notificationId: Int) {
    val center = UNUserNotificationCenter.currentNotificationCenter()
    center.removeDeliveredNotificationsWithIdentifiers(listOf(notificationId.toString()))
}
