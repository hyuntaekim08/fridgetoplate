package com.example.fridgetoplate.data

import java.io.IOException
import java.util.Locale
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class SpoonacularRecipeRepository(
    private val apiKey: String,
    private val client: OkHttpClient = OkHttpClient(),
    private val baseUrl: HttpUrl = HttpUrl.Builder()
        .scheme("https").host("api.spoonacular.com").build(),
) : RecipeRepository {
    override suspend fun recommend(request: RecommendationRequest): RepositoryResult<List<RecipeRecommendation>> {
        val ingredients = request.ingredients.map(String::trim).filter(String::isNotEmpty)
            .distinctBy { it.lowercase(Locale.ROOT) }
        if (ingredients.isEmpty() || ingredients.any { ',' in it }) {
            return RepositoryResult.Failure(RecipeError.InvalidInput("Provide separate, non-empty ingredient names without commas."))
        }
        if (request.limit !in 1..100) {
            return RepositoryResult.Failure(RecipeError.InvalidInput("limit must be between 1 and 100."))
        }
        val url = baseUrl.newBuilder().addPathSegments("recipes/findByIngredients")
            .addQueryParameter("ingredients", ingredients.joinToString(","))
            .addQueryParameter("number", request.limit.toString())
            .addQueryParameter("ranking", request.ranking.apiValue.toString())
            .addQueryParameter("ignorePantry", request.ignorePantry.toString()).build()
        return execute(url) { body ->
            JSONArray(body).objects().map { recipe ->
                RecipeRecommendation(
                    id = recipe.getInt("id"),
                    title = recipe.getString("title"),
                    imageUrl = recipe.optionalString("image"),
                    usedIngredients = recipe.optJSONArray("usedIngredients").ingredients(),
                    missingIngredients = recipe.optJSONArray("missedIngredients").ingredients(),
                    unusedIngredients = recipe.optJSONArray("unusedIngredients").ingredients(),
                    usedIngredientCount = recipe.getInt("usedIngredientCount"),
                    missingIngredientCount = recipe.getInt("missedIngredientCount"),
                )
            }
        }
    }

    override suspend fun getRecipe(id: Int): RepositoryResult<RecipeDetails> {
        if (id <= 0) return RepositoryResult.Failure(RecipeError.InvalidInput("Recipe ID must be positive."))
        val url = baseUrl.newBuilder().addPathSegments("recipes/$id/information")
            .addQueryParameter("includeNutrition", "false").build()
        return execute(url) { body ->
            val recipe = JSONObject(body)
            RecipeDetails(
                id = recipe.getInt("id"),
                title = recipe.getString("title"),
                imageUrl = recipe.optionalString("image"),
                servings = recipe.optionalInt("servings"),
                readyInMinutes = recipe.optionalInt("readyInMinutes"),
                sourceUrl = recipe.optionalString("sourceUrl"),
                ingredients = recipe.optJSONArray("extendedIngredients").ingredients(),
                instructions = recipe.optJSONArray("analyzedInstructions").objects().map { section ->
                    RecipeInstructionSection(
                        section.optString("name"),
                        section.optJSONArray("steps").objects().map {
                            RecipeStep(it.getInt("number"), it.getString("step"))
                        },
                    )
                },
                instructionsHtml = recipe.optionalString("instructions"),
                vegetarian = recipe.optBoolean("vegetarian"),
                vegan = recipe.optBoolean("vegan"),
                glutenFree = recipe.optBoolean("glutenFree"),
                dairyFree = recipe.optBoolean("dairyFree"),
            )
        }
    }

    private suspend fun <T> execute(url: HttpUrl, parse: (String) -> T): RepositoryResult<T> {
        if (apiKey.isBlank()) return RepositoryResult.Failure(RecipeError.MissingApiKey)
        val request = Request.Builder().url(url).header("x-api-key", apiKey.trim()).build()
        return try {
            val payload = client.newCall(request).awaitPayload()
            val error = when (payload.code) {
                in 200..299 -> null
                401, 403 -> RecipeError.Unauthorized
                402 -> RecipeError.QuotaExceeded
                404 -> RecipeError.NotFound
                429 -> RecipeError.RateLimited(payload.retryAfter)
                else -> RecipeError.Http(payload.code)
            }
            if (error != null) RepositoryResult.Failure(error)
            else RepositoryResult.Success(parse(payload.body))
        } catch (_: IOException) {
            RepositoryResult.Failure(RecipeError.Network)
        } catch (_: JSONException) {
            RepositoryResult.Failure(RecipeError.InvalidResponse)
        }
    }
}

private data class Payload(val code: Int, val body: String, val retryAfter: String?)

private suspend fun Call.awaitPayload(): Payload = suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation { cancel() }
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            continuation.resumeWith(Result.failure(e))
        }
        override fun onResponse(call: Call, response: Response) {
            try {
                val payload = response.use {
                    Payload(it.code, if (it.isSuccessful) it.body?.string().orEmpty() else "", it.header("Retry-After"))
                }
                continuation.resumeWith(Result.success(payload))
            } catch (e: IOException) {
                continuation.resumeWith(Result.failure(e))
            }
        }
    })
}

private fun JSONArray?.objects(): List<JSONObject> =
    if (this == null) emptyList() else (0 until length()).map { getJSONObject(it) }

private fun JSONObject.optionalString(key: String): String? =
    if (isNull(key)) null else optString(key).takeIf(String::isNotBlank)

private fun JSONObject.optionalInt(key: String): Int? =
    if (isNull(key) || !has(key)) null else getInt(key)

private fun JSONArray?.ingredients(): List<RecipeIngredient> = objects().map {
    RecipeIngredient(
        id = it.optionalInt("id"),
        name = it.getString("name"),
        amount = it.optDouble("amount", 0.0),
        unit = it.optString("unit"),
        original = it.optString("original"),
    )
}
