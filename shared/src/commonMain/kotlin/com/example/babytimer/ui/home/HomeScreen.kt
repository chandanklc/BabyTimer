package com.example.babytimer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.babytimer.ui.splash.SplashViewModel
import com.example.babytimer.ui.splash.dashedBorder
import com.example.babytimer.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: SplashViewModel,
    diaperViewModel: DiaperViewModel,
    onNavigateBackToSplash: () -> Unit = {},
    onNavigateToMealPlanner: () -> Unit = {},
    onRequestNotificationPermission: () -> Unit = {}
) {
    val babyName by viewModel.babyName.collectAsState()
    val photoUri by viewModel.photoUri.collectAsState()
    val diaperState by diaperViewModel.state.collectAsState()

    val cardBg = Color.White

    Scaffold(
        bottomBar = {
            Surface(
                color = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = AccentPurple,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Home",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentPurple,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(AccentPurple)
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add moment",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = "Journal",
                            tint = MutedText,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Journal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedText
                        )
                    }
                }
            }
        },
        containerColor = CreamBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateBackToSplash() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(PinkPillBg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri != null) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Baby Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "Baby Avatar",
                                tint = AccentPurple,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Hello, $babyName",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = DeepPurple
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Growing every day",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, LavenderBubble, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = DeepPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Card 1: Meal Reminder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToMealPlanner() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Meal planner & reminder 🍽️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepPurple
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Plan every bite, one week at a time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MintCardBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🍲", fontSize = 28.sp)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Weekly Menu",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DeepPurple
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Tap to open full schedule",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedText
                                )
                            }
                        }

                        Text(
                            text = "View all ›",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentPurple
                        )
                    }
                }
            }

            // Card 2: Diaper Change Timers
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Diaper change 🚨",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DeepPurple
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "High priority: Set custom duration from seconds to hours",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DiaperTimerColumn(
                            title = "Normal Diaper",
                            secondsRemaining = diaperState.normalSecondsRemaining,
                            selectedDurationSeconds = diaperState.normalSelectedDurationSeconds,
                            isActive = diaperState.normalIsActive,
                            containerColor = PinkPillBg,
                            onDurationSelected = { secs -> diaperViewModel.setNormalDuration(secs) },
                            onStart = {
                                onRequestNotificationPermission()
                                diaperViewModel.startNormalTimer()
                            },
                            onReset = { diaperViewModel.resetNormalTimer() },
                            modifier = Modifier.weight(1f)
                        )

                        DiaperTimerColumn(
                            title = "Reusable Diaper",
                            secondsRemaining = diaperState.reusableSecondsRemaining,
                            selectedDurationSeconds = diaperState.reusableSelectedDurationSeconds,
                            isActive = diaperState.reusableIsActive,
                            containerColor = MintCardBg,
                            onDurationSelected = { secs -> diaperViewModel.setReusableDuration(secs) },
                            onStart = {
                                onRequestNotificationPermission()
                                diaperViewModel.startReusableTimer()
                            },
                            onReset = { diaperViewModel.resetReusableTimer() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiaperTimerColumn(
    title: String,
    secondsRemaining: Long,
    selectedDurationSeconds: Long,
    isActive: Boolean,
    containerColor: Color,
    onDurationSelected: (Long) -> Unit,
    onStart: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val presetDurations = listOf(
        Pair("20 sec (Test)", 20L),
        Pair("30 min", 30 * 60L),
        Pair("1 hour", 3600L),
        Pair("2 hours", 2 * 3600L),
        Pair("3 hours", 3 * 3600L),
        Pair("4 hours", 4 * 3600L)
    )

    val selectedLabel = presetDurations.find { it.second == selectedDurationSeconds }?.first ?: "${selectedDurationSeconds}s"

    val hrs = secondsRemaining / 3600
    val mins = (secondsRemaining % 3600) / 60
    val secs = secondsRemaining % 60
    val timeFormatted = if (hrs > 0) {
        "$hrs:${if (mins < 10) "0" else ""}$mins:${if (secs < 10) "0" else ""}$secs"
    } else if (mins > 0) {
        "${if (mins < 10) "0" else ""}$mins:${if (secs < 10) "0" else ""}$secs"
    } else {
        "00:${if (secs < 10) "0" else ""}$secs"
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = DeepPurple
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(modifier = Modifier.wrapContentSize(Alignment.TopCenter)) {
            OutlinedButton(
                onClick = { if (!isActive) expanded = true },
                enabled = !isActive,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(30.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.7f))
            ) {
                Text(text = selectedLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DeepPurple)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                presetDurations.forEach { (label, secs) ->
                    DropdownMenuItem(
                        text = { Text(label, fontSize = 12.sp) },
                        onClick = {
                            onDurationSelected(secs)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = timeFormatted,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = AccentPurple
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onStart,
                enabled = !isActive,
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Text(text = "Start", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onReset,
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(text = "Reset", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepPurple)
            }
        }
    }
}
