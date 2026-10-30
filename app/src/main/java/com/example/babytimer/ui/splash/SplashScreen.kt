package com.example.babytimer.ui.splash

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onEnterHome: () -> Unit = {}
) {
    val photoUri by viewModel.photoUri.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.updatePhotoUri(uri?.toString())
    }

    // Custom colors matching the reference design
    val creamBg = Color(0xFFFFFDF6)
    val lavenderBubble = Color(0xFFEFE8FA)
    val peachBubble = Color(0xFFFFEAD6)
    val mintCardBg = Color(0xFFD3F5E4)
    val mintCardBorder = Color(0xFF8CE2BA)
    val deepPurple = Color(0xFF2C2543)
    val accentPurple = Color(0xFF6B53C5)
    val mutedText = Color(0xFF7A7585)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(creamBg)
    ) {
        // Top-left decorative bubble
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-60).dp, y = (-60).dp)
                .clip(CircleShape)
                .background(lavenderBubble)
        )

        // Top-right decorative bubble
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = 40.dp)
                .clip(CircleShape)
                .background(peachBubble)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 36.dp)
            ) {
                // Small top badge / icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(accentPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👶", fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Little Moments",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = deepPurple,
                    fontSize = 32.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "A tiny journal for your growing family",
                    style = MaterialTheme.typography.bodyMedium,
                    color = mutedText,
                    textAlign = TextAlign.Center
                )
            }

            // Photo upload placeholder card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(mintCardBg)
                    .dashedBorder(
                        color = mintCardBorder,
                        strokeWidth = 2.dp,
                        strokeLength = 8.dp,
                        gapLength = 6.dp,
                        cornerRadius = 24.dp
                    )
                    .clickable { imagePickerLauncher.launch("image/*") },
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Photo",
                                tint = accentPurple,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Tap to add your baby's first photo",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = accentPurple,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Footer / Action Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Your photos stay beautifully organized here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onEnterHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentPurple)
                ) {
                    Text(
                        text = "Create our story",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Private • Made with love",
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp,
    strokeLength: Dp = 10.dp,
    gapLength: Dp = 10.dp,
    cornerRadius: Dp = 0.dp
): Modifier = this.drawBehind {
    val strokeWidthPx = strokeWidth.toPx()
    val strokeLengthPx = strokeLength.toPx()
    val gapLengthPx = gapLength.toPx()
    val cornerRadiusPx = cornerRadius.toPx()

    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(strokeLengthPx, gapLengthPx), 0f)
    drawRoundRect(
        color = color,
        style = Stroke(
            width = strokeWidthPx,
            pathEffect = pathEffect
        ),
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    )
}
