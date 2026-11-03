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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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

@Composable
fun RecipeEditorScreen(
    dayName: String,
    slotType: String,
    initialMealName: String,
    viewModel: RecipeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit = {}
) {
    var title by remember { mutableStateOf(initialMealName) }
    var recipeContent by remember { mutableStateOf("") }
    var showRecipePicker by remember { mutableStateOf(false) }

    val savedRecipes by viewModel.savedRecipes.collectAsState()

    val slotTitleFormatted = when (slotType) {
        "am" -> "AM Meal"
        "lunch" -> "Lunch"
        "dinner" -> "Dinner"
        "snack" -> "Snack"
        else -> slotType
    }

    Scaffold(
        containerColor = CreamBg,
        bottomBar = {
            Surface(
                color = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                viewModel.saveRecipe(title, recipeContent)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Save Recipe")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save to Library")
                    }

                    Button(
                        onClick = {
                            viewModel.updateMeal(dayName, slotType, title)
                            onBack()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Assign")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Assign to $dayName")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        text = "Recipe & Meal Planner",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DeepPurple
                    )
                    Text(
                        text = "$dayName • $slotTitleFormatted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }
            }

            // Quick Assign Button from Library
            if (savedRecipes.isNotEmpty()) {
                OutlinedButton(
                    onClick = { showRecipePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("📂 Choose from Saved Recipe Library (${savedRecipes.size})", color = AccentPurple, fontWeight = FontWeight.SemiBold)
                }
            }

            // Recipe Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Meal / Recipe Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // Recipe Instructions / Ingredients
            OutlinedTextField(
                value = recipeContent,
                onValueChange = { recipeContent = it },
                label = { Text("Recipe Ingredients & Instructions") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }

    if (showRecipePicker) {
        AlertDialog(
            onDismissRequest = { showRecipePicker = false },
            title = { Text("Select from Library") },
            text = {
                if (savedRecipes.isEmpty()) {
                    Text("No saved recipes yet.")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(savedRecipes) { recipe ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        title = recipe.title
                                        recipeContent = recipe.content
                                        showRecipePicker = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = MintCardBg)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = recipe.title, fontWeight = FontWeight.Bold, color = DeepPurple)
                                    if (recipe.content.isNotBlank()) {
                                        Text(text = recipe.content, style = MaterialTheme.typography.bodySmall, color = MutedText, maxLines = 2)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRecipePicker = false }) {
                    Text("Close")
                }
            }
        )
    }
}
