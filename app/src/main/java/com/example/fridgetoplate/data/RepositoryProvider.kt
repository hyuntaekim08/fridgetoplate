package com.example.fridgetoplate.data

import com.example.fridgetoplate.BuildConfig

/** Shared instance; UI can substitute any RecipeRepository implementation for previews or tests. */
object RepositoryProvider {
    val recipes: RecipeRepository by lazy {
        SpoonacularRecipeRepository(apiKey = BuildConfig.SPOONACULAR_API_KEY)
    }
}
