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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.babytimer.data.Recipe
import com.example.babytimer.ui.theme.*

@Composable
fun MealPlannerScreen(
    viewModel: MealPlannerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit = {},
    onSelectMealSlot: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val weeklyPlan by viewModel.weeklyPlan.collectAsState()
    val savedRecipes by viewModel.savedRecipes.collectAsState()

    var activeDay by remember { mutableStateOf("") }
    var activeSlot by remember { mutableStateOf("") }
    var activeMealName by remember { mutableStateOf("") }

    var showActionDialog by remember { mutableStateOf(false) }
    var showLibraryPicker by remember { mutableStateOf(false) }

    var viewingRecipe by remember { mutableStateOf<Recipe?>(null) }
    var editingRecipe by remember { mutableStateOf<Recipe?>(null) }

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
                                activeDay = plan.dayName
                                activeSlot = "am"
                                activeMealName = plan.amMeal
                                showActionDialog = true
                            }

                            // Meal 2: Lunch
                            MealSlotBox(mealName = plan.lunch, bg = plan.cardColor) {
                                activeDay = plan.dayName
                                activeSlot = "lunch"
                                activeMealName = plan.lunch
                                showActionDialog = true
                            }

                            // Meal 3: Dinner
                            MealSlotBox(mealName = plan.dinner, bg = plan.cardColor) {
                                activeDay = plan.dayName
                                activeSlot = "dinner"
                                activeMealName = plan.dinner
                                showActionDialog = true
                            }

                            // Meal 4: Snack
                            MealSlotBox(mealName = plan.snack, bg = plan.cardColor) {
                                activeDay = plan.dayName
                                activeSlot = "snack"
                                activeMealName = plan.snack
                                showActionDialog = true
                            }
                        }
                    }
                }
            }
        }
    }

    // Action Dialog: Ask user if they want to use Saved Recipe or Create New Recipe
    if (showActionDialog) {
        AlertDialog(
            onDismissRequest = { showActionDialog = false },
            title = { Text("Plan Meal for $activeDay") },
            text = { Text("How would you like to update this meal slot?") },
            confirmButton = {
                Button(onClick = {
                    showActionDialog = false
                    showLibraryPicker = true
                }) {
                    Text("📂 Choose from Library")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showActionDialog = false
                    onSelectMealSlot(activeDay, activeSlot, activeMealName)
                }) {
                    Text("✍️ Create New Recipe")
                }
            }
        )
    }

    // Saved Recipe Library Picker Dialog with View & Edit options
    if (showLibraryPicker) {
        AlertDialog(
            onDismissRequest = { showLibraryPicker = false },
            title = { Text("Saved Recipe Library") },
            text = {
                if (savedRecipes.isEmpty()) {
                    Text("No saved recipes found.")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(savedRecipes) { recipe ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MintCardBg)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = recipe.title, fontWeight = FontWeight.Bold, color = DeepPurple)
                                    if (recipe.content.isNotBlank()) {
                                        Text(text = recipe.content, style = MaterialTheme.typography.bodySmall, color = MutedText, maxLines = 2)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Assign Button
                                        Button(
                                            onClick = {
                                                viewModel.updateMeal(activeDay, activeSlot, recipe.title)
                                                showLibraryPicker = false
                                            },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(0.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                                        ) {
                                            Text("Assign", fontSize = 11.sp)
                                        }

                                        // View Button
                                        OutlinedButton(
                                            onClick = {
                                                showLibraryPicker = false
                                                viewingRecipe = recipe
                                            },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("👁️ View", fontSize = 11.sp, color = DeepPurple)
                                        }

                                        // Edit Button
                                        OutlinedButton(
                                            onClick = {
                                                showLibraryPicker = false
                                                editingRecipe = recipe
                                            },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("✏️ Edit", fontSize = 11.sp, color = DeepPurple)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLibraryPicker = false }) {
                    Text("Close")
                }
            }
        )
    }

    // View Recipe Dialog with option to Edit
    if (viewingRecipe != null) {
        val recipe = viewingRecipe!!
        AlertDialog(
            onDismissRequest = { viewingRecipe = null },
            title = { Text(recipe.title, fontWeight = FontWeight.Bold, color = DeepPurple) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    Text(
                        text = recipe.content.ifBlank { "No ingredients or instructions provided yet." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeepPurple
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val r = viewingRecipe
                    viewingRecipe = null
                    editingRecipe = r
                }) {
                    Text("✏️ Edit Recipe")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewingRecipe = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Edit Recipe Dialog
    if (editingRecipe != null) {
        val recipe = editingRecipe!!
        var editTitle by remember { mutableStateOf(recipe.title) }
        var editContent by remember { mutableStateOf(recipe.content) }

        AlertDialog(
            onDismissRequest = { editingRecipe = null },
            title = { Text("Edit Recipe") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Recipe Title") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        label = { Text("Ingredients & Instructions") },
                        modifier = Modifier.height(200.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveRecipe(editTitle, editContent)
                    editingRecipe = null
                }) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingRecipe = null }) {
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
            fontSize = 11.sp,
            maxLines = 1
        )
        Text(
            text = "Tap to edit",
            style = MaterialTheme.typography.labelSmall,
            color = AccentPurple,
            fontSize = 9.sp
        )
    }
}
