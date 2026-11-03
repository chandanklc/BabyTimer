package com.example.babytimer.ui.meal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.babytimer.ui.theme.*

data class DayMealPlan(
    val dayName: String,
    val amMeal: String,
    val lunch: String,
    val dinner: String,
    val snack: String,
    val cardColor: Color
)

@Composable
fun MealPlannerScreen(
    onBack: () -> Unit = {}
) {
    val weeklyPlan = listOf(
        DayMealPlan("Monday", "Oats", "Veggie", "Rice", "Fruit", Color(0xFFD3F5E4)),
        DayMealPlan("Tuesday", "Idli", "Lentil", "Pasta", "Yogurt", Color(0xFFE8E0FF)),
        DayMealPlan("Wednesday", "Toast", "Khichdi", "Soup", "Banana", Color(0xFFFFE0CC)),
        DayMealPlan("Thursday", "Pancake", "Rice", "Dal", "Berries", Color(0xFFFFD6E0)),
        DayMealPlan("Friday", "Porridge", "Potato", "Fish", "Apple", Color(0xFFD3F5E4)),
        DayMealPlan("Saturday", "Puff", "Noodles", "Stew", "Cookie", Color(0xFFE8E0FF)),
        DayMealPlan("Sunday", "Waffle", "Paneer", "Khichdi", "Mango", Color(0xFFFFE0CC))
    )

    var selectedMeal by remember { mutableStateOf<Pair<String, String>?>(null) }
    var editDialogVisible by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = CreamBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            // Top Bar
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
                        imageVector = Icons.Default.ArrowBack,
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

            // Section Title
            Text(
                text = "Your weekly meal plan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepPurple
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap any meal slot to plan or edit",
                style = MaterialTheme.typography.bodySmall,
                color = MutedText
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Column Labels Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "DAY", style = MaterialTheme.typography.labelSmall, color = MutedText, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
                Text(text = "AM MEAL", style = MaterialTheme.typography.labelSmall, color = MutedText, fontWeight = FontWeight.Bold)
                Text(text = "LUNCH", style = MaterialTheme.typography.labelSmall, color = MutedText, fontWeight = FontWeight.Bold)
                Text(text = "DINNER", style = MaterialTheme.typography.labelSmall, color = MutedText, fontWeight = FontWeight.Bold)
                Text(text = "SNACK", style = MaterialTheme.typography.labelSmall, color = MutedText, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Weekly List
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
                            // Day Box
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
                                    fontSize = 12.sp
                                )
                            }

                            // Meal 1: AM Meal
                            MealSlotBox(mealName = plan.amMeal, bg = plan.cardColor) {
                                selectedMeal = Pair(plan.dayName, "AM Meal: ${plan.amMeal}")
                                editDialogVisible = true
                            }

                            // Meal 2: Lunch
                            MealSlotBox(mealName = plan.lunch, bg = plan.cardColor) {
                                selectedMeal = Pair(plan.dayName, "Lunch: ${plan.lunch}")
                                editDialogVisible = true
                            }

                            // Meal 3: Dinner
                            MealSlotBox(mealName = plan.dinner, bg = plan.cardColor) {
                                selectedMeal = Pair(plan.dayName, "Dinner: ${plan.dinner}")
                                editDialogVisible = true
                            }

                            // Meal 4: Snack
                            MealSlotBox(mealName = plan.snack, bg = plan.cardColor) {
                                selectedMeal = Pair(plan.dayName, "Snack: ${plan.snack}")
                                editDialogVisible = true
                            }
                        }
                    }
                }
            }
        }
    }

    if (editDialogVisible && selectedMeal != null) {
        AlertDialog(
            onDismissRequest = { editDialogVisible = false },
            title = { Text("Edit Meal Plan") },
            text = { Text("Planning for ${selectedMeal?.first}\n${selectedMeal?.second}\n\nTap to customize ingredients or timing.") },
            confirmButton = {
                Button(onClick = { editDialogVisible = false }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editDialogVisible = false }) {
                    Text("Cancel")
                }
            }
        )
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
            fontSize = 11.sp
        )
        Text(
            text = "Tap",
            style = MaterialTheme.typography.labelSmall,
            color = AccentPurple,
            fontSize = 9.sp
        )
    }
}
