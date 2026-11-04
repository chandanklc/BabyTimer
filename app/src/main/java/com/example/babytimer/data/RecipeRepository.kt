package com.example.babytimer.data

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

data class Recipe(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String
)

interface RecipeRepository {
    val savedRecipes: Flow<List<Recipe>>
    suspend fun saveRecipe(title: String, content: String): Recipe
    suspend fun deleteRecipe(id: String)
}

class RecipeRepositoryImpl(private val dataStore: androidx.datastore.core.DataStore<Preferences>) : RecipeRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val rtdb = FirebaseDatabase.getInstance("https://babytimer-a8dcb-default-rtdb.firebaseio.com/")

    private val defaultRecipes = listOf(
        Recipe(
            title = "Oats & Banana Mash",
            content = "Age: 6+ months\n\nIngredients:\n• 3 tbsp rolled oats\n• 1/2 ripe banana\n• 1/2 cup breast milk, formula, or water\n\nInstructions:\n1. Cook oats in water or milk over medium heat for 5 minutes until soft and creamy.\n2. Mash the ripe banana in a bowl using a fork until smooth.\n3. Stir the warm oats into the mashed banana until well blended.\n4. Let cool to room temperature before serving."
        ),
        Recipe(
            title = "Steamed Apple & Pear Puree",
            content = "Age: 6+ months\n\nIngredients:\n• 1 organic sweet apple\n• 1 ripe pear\n• Pinch of Ceylon cinnamon (optional)\n\nInstructions:\n1. Wash, peel, core, and chop the apple and pear into small chunks.\n2. Place in a steamer basket over boiling water, cover, and steam for 8–10 minutes until fork-tender.\n3. Transfer fruits to a blender or food processor.\n4. Blend until completely smooth and velvety. Cool before feeding."
        ),
        Recipe(
            title = "Moong Dal Khichdi",
            content = "Age: 8+ months\n\nIngredients:\n• 2 tbsp yellow moong dal (split yellow lentils)\n• 2 tbsp polished white or brown rice\n• 1/4 tsp turmeric powder\n• 1/2 tsp ghee (clarified butter)\n• 1.5 cups water\n\nInstructions:\n1. Wash rice and moong dal thoroughly together in water.\n2. In a pressure cooker, add rice, dal, turmeric, ghee, and 1.5 cups of water.\n3. Cook for 4-5 whistles on medium-low heat until thoroughly mushy.\n4. Mash with the back of a spoon to a porridge consistency and serve warm."
        ),
        Recipe(
            title = "Mashed Sweet Potato & Carrot",
            content = "Age: 7+ months\n\nIngredients:\n• 1 medium sweet potato\n• 1 fresh carrot\n• 1 tsp unsalted butter or breast milk\n\nInstructions:\n1. Wash, peel, and dice the sweet potato and carrot.\n2. Steam or boil in water for 12-15 minutes until extremely tender.\n3. Transfer to a mixing bowl, add butter or a splash of breast milk/formula.\n4. Mash thoroughly with a potato masher or fork until lump-free."
        ),
        Recipe(
            title = "Soft Idli & Veggie Mash",
            content = "Age: 9+ months\n\nIngredients:\n• 2 soft plain idlis\n• 1/2 cup warm vegetable or bone broth (salt-free)\n\nInstructions:\n1. Take 2 freshly steamed soft idlis in a clean bowl.\n2. Pour warm salt-free vegetable broth or warm milk over them.\n3. Let it soak for 2 minutes to absorb the liquid.\n4. Mash with a fork into a soft, spoonable porridge that is easy for baby to swallow."
        ),
        Recipe(
            title = "Avocado & Yogurt Cream",
            content = "Age: 6+ months\n\nIngredients:\n• 1/2 ripe Hass avocado\n• 3 tbsp plain whole-milk Greek yogurt\n\nInstructions:\n1. Cut the ripe avocado in half, remove the pit, and scoop the flesh into a bowl.\n2. Add the plain whole-milk Greek yogurt.\n3. Mash and whisk together using a fork until completely smooth and creamy.\n4. Packed with healthy fats for baby's brain development!"
        ),
        Recipe(
            title = "Rice & Dal Soup",
            content = "Age: 8+ months\n\nIngredients:\n• 2 tbsp well-cooked soft white rice\n• 1/2 cup watery yellow moong dal soup (dal water)\n• 1 drop of ghee\n\nInstructions:\n1. Prepare thin yellow moong dal soup by boiling dal with turmeric and cumin in plenty of water until watery.\n2. Take 2 tablespoons of soft-cooked mashed rice in a bowl.\n3. Pour the warm dal soup over the rice and mix well.\n4. Serve as an easy-to-sip, highly nourishing liquid meal."
        )
    )

    override val savedRecipes: Flow<List<Recipe>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cloudRecipes = mutableMapOf<String, Recipe>()
                for (child in snapshot.children) {
                    val id = child.key ?: UUID.randomUUID().toString()
                    val title = child.child("title").getValue(String::class.java) ?: ""
                    val content = child.child("content").getValue(String::class.java) ?: ""
                    if (title.isNotBlank()) {
                        cloudRecipes[title] = Recipe(id = id, title = title, content = content)
                    }
                }

                val combined = mutableListOf<Recipe>()
                val seenTitles = mutableSetOf<String>()

                // Add cloud recipes from Firebase Realtime Database
                cloudRecipes.values.forEach { recipe ->
                    combined.add(recipe)
                    seenTitles.add(recipe.title)
                }

                // Add default recipes if not already in Firebase
                defaultRecipes.forEach { defaultRecipe ->
                    if (!seenTitles.contains(defaultRecipe.title)) {
                        combined.add(defaultRecipe)
                        seenTitles.add(defaultRecipe.title)
                    }
                }

                trySend(combined)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(defaultRecipes)
            }
        }

        val ref = rtdb.getReference("recipes")
        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    override suspend fun saveRecipe(title: String, content: String): Recipe {
        val newRecipe = Recipe(title = title, content = content)
        
        try {
            rtdb.getReference("recipes").child(newRecipe.id)
                .setValue(mapOf("title" to newRecipe.title, "content" to newRecipe.content))

            firestore.collection("recipes")
                .document(newRecipe.id)
                .set(mapOf("title" to newRecipe.title, "content" to newRecipe.content))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return newRecipe
    }

    override suspend fun deleteRecipe(id: String) {
        try {
            rtdb.getReference("recipes").child(id).removeValue()
            firestore.collection("recipes").document(id).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
