package com.example.fridgetoplate.data

/** UI-facing contract. Call suspend methods from a ViewModel coroutine. */
interface RecipeRepository {
    suspend fun recommend(request: RecommendationRequest): RepositoryResult<List<RecipeRecommendation>>
    suspend fun getRecipe(id: Int): RepositoryResult<RecipeDetails>
}

enum class RecommendationRanking(val apiValue: Int) {
    MAXIMIZE_USED_INGREDIENTS(1),
    MINIMIZE_MISSING_INGREDIENTS(2),
}

/** Ingredient names must be English; translation is not performed by this repository. */
data class RecommendationRequest(
    val ingredients: List<String>,
    val limit: Int = 10,
    val ranking: RecommendationRanking = RecommendationRanking.MINIMIZE_MISSING_INGREDIENTS,
    val ignorePantry: Boolean = true,
)

data class RecipeIngredient(
    val id: Int?,
    val name: String,
    val amount: Double,
    val unit: String,
    val original: String,
)

data class RecipeRecommendation(
    val id: Int,
    val title: String,
    val imageUrl: String?,
    val usedIngredients: List<RecipeIngredient>,
    val missingIngredients: List<RecipeIngredient>,
    val unusedIngredients: List<RecipeIngredient>,
    val usedIngredientCount: Int,
    val missingIngredientCount: Int,
)

data class RecipeStep(val number: Int, val instruction: String)
data class RecipeInstructionSection(val name: String, val steps: List<RecipeStep>)
data class RecipeDetails(
    val id: Int,
    val title: String,
    val imageUrl: String?,
    val servings: Int?,
    val readyInMinutes: Int?,
    val sourceUrl: String?,
    val ingredients: List<RecipeIngredient>,
    val instructions: List<RecipeInstructionSection>,
    /** Fallback when analyzed instructions are unavailable. May contain HTML. */
    val instructionsHtml: String?,
    val vegetarian: Boolean,
    val vegan: Boolean,
    val glutenFree: Boolean,
    val dairyFree: Boolean,
)

sealed interface RepositoryResult<out T> {
    data class Success<T>(val value: T) : RepositoryResult<T>
    data class Failure(val error: RecipeError) : RepositoryResult<Nothing>
}

sealed interface RecipeError {
    data class InvalidInput(val reason: String) : RecipeError
    data object MissingApiKey : RecipeError
    data object Unauthorized : RecipeError
    data object QuotaExceeded : RecipeError
    data class RateLimited(val retryAfter: String?) : RecipeError
    data object NotFound : RecipeError
    data object Network : RecipeError
    data object InvalidResponse : RecipeError
    data class Http(val statusCode: Int) : RecipeError
}
