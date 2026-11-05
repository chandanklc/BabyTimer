package com.example.babytimer.ui.meal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.babytimer.ui.theme.*

@Composable
fun MealPlannerScreen(
    viewModel: MealPlannerViewModel,
    onBack: () -> Unit = {},
    onSelectMealSlot: (String, String, String) -> Unit = { _, _, _ -> },
    onOpenTimePicker: (String, String) -> Unit = { _, _ -> }
) {
    val weeklyPlan by viewModel.weeklyPlan.collectAsState()
    val mealTimes by viewModel.mealTimes.collectAsState()

    Scaffold(
        containerColor = CreamBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DeepPurple
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Meal planner",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepPurple
                    )
                    Text(
                        text = "Plan every bite, one week at a time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Your weekly meal plan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepPurple
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap the meal plan to edit and save the recipes (Tap times to edit reminders)",
                style = MaterialTheme.typography.bodySmall,
                color = MutedText
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "DAY", style = MaterialTheme.typography.labelSmall, color = MutedText, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp))

                MealTimeTag(label = "AM MEAL", time = mealTimes["am"] ?: "08:30 AM") {
                    onOpenTimePicker("am", mealTimes["am"] ?: "08:30 AM")
                }
                MealTimeTag(label = "LUNCH", time = mealTimes["lunch"] ?: "01:00 PM") {
                    onOpenTimePicker("lunch", mealTimes["lunch"] ?: "01:00 PM")
                }
                MealTimeTag(label = "DINNER", time = mealTimes["dinner"] ?: "07:30 PM") {
                    onOpenTimePicker("dinner", mealTimes["dinner"] ?: "07:30 PM")
                }
                MealTimeTag(label = "SNACK", time = mealTimes["snack"] ?: "03:30 PM") {
                    onOpenTimePicker("snack", mealTimes["snack"] ?: "03:30 PM")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(weeklyPlan) { plan ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 68.dp, height = 56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AccentPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = plan.dayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            MealSlotBox(mealName = plan.amMeal, bg = plan.cardColor) {
                                onSelectMealSlot(plan.dayName, "am", plan.amMeal)
                            }
                            MealSlotBox(mealName = plan.lunch, bg = plan.cardColor) {
                                onSelectMealSlot(plan.dayName, "lunch", plan.lunch)
                            }
                            MealSlotBox(mealName = plan.dinner, bg = plan.cardColor) {
                                onSelectMealSlot(plan.dayName, "dinner", plan.dinner)
                            }
                            MealSlotBox(mealName = plan.snack, bg = plan.cardColor) {
                                onSelectMealSlot(plan.dayName, "snack", plan.snack)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MealTimeTag(
    label: String,
    time: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(62.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MutedText, fontWeight = FontWeight.Bold, fontSize = 8.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(1.dp))
        Text(text = time, style = MaterialTheme.typography.labelSmall, color = AccentPurple, fontWeight = FontWeight.Bold, fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun MealSlotBox(
    mealName: String,
    bg: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .size(width = 62.dp, height = 56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = mealName,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = DeepPurple,
            fontSize = 11.sp,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}
