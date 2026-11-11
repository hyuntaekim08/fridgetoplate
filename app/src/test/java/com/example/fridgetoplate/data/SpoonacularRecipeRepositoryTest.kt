package com.example.fridgetoplate.data

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SpoonacularRecipeRepositoryTest {
    private val server = MockWebServer()
    private lateinit var repository: RecipeRepository

    @Before fun setUp() {
        server.start()
        repository = SpoonacularRecipeRepository("test-key", baseUrl = server.url("/"))
    }

    @After fun tearDown() { server.shutdown() }

    @Test fun recommendationEncodesIngredientsAndMapsMissingItems() = runBlocking {
        server.enqueue(MockResponse().setBody("""
            [{"id":7,"title":"Egg rice","usedIngredientCount":2,"missedIngredientCount":1,
              "usedIngredients":[{"id":1,"name":"egg","amount":2,"unit":"","original":"2 eggs"}],
              "missedIngredients":[{"name":"onion","amount":1,"unit":"","original":"1 onion"}]}]
        """.trimIndent()))
        val result = repository.recommend(RecommendationRequest(listOf(" egg ", "rice", "EGG", "")))
        val recipes = (result as RepositoryResult.Success).value
        assertEquals("onion", recipes.single().missingIngredients.single().name)
        assertNull(recipes.single().imageUrl)
        val request = server.takeRequest()
        assertEquals("egg,rice", request.requestUrl!!.queryParameter("ingredients"))
        assertEquals("2", request.requestUrl!!.queryParameter("ranking"))
        assertEquals("test-key", request.getHeader("x-api-key"))
        assertNull(request.requestUrl!!.queryParameter("apiKey"))
    }

    @Test fun invalidInputAndMissingKeyDoNotUseNetwork() = runBlocking {
        assertEquals(RecipeError.MissingApiKey,
            (SpoonacularRecipeRepository("").getRecipe(1) as RepositoryResult.Failure).error)
        assertTrue((repository.recommend(RecommendationRequest(emptyList())) as RepositoryResult.Failure).error is RecipeError.InvalidInput)
        assertTrue((repository.recommend(RecommendationRequest(listOf("egg"), limit = 101)) as RepositoryResult.Failure).error is RecipeError.InvalidInput)
        assertEquals(0, server.requestCount)
    }

    @Test fun mapsHttpErrors() = runBlocking {
        for ((code, error) in listOf(
            401 to RecipeError.Unauthorized,
            402 to RecipeError.QuotaExceeded,
            404 to RecipeError.NotFound,
            429 to RecipeError.RateLimited("60"),
            500 to RecipeError.Http(500),
        )) {
            server.enqueue(MockResponse().setResponseCode(code).setHeader("Retry-After", "60"))
            assertEquals(error, (repository.getRecipe(1) as RepositoryResult.Failure).error)
        }
    }

    @Test fun malformedPayloadIsFailure() = runBlocking {
        server.enqueue(MockResponse().setBody("not json"))
        assertEquals(RecipeError.InvalidResponse, (repository.getRecipe(1) as RepositoryResult.Failure).error)
    }

    @Test fun detailPreservesSectionsAndOptionalFields() = runBlocking {
        server.enqueue(MockResponse().setBody("""
            {"id":7,"title":"Egg rice","servings":2,"readyInMinutes":15,
             "extendedIngredients":[{"id":1,"name":"egg","amount":2,"unit":"","original":"2 eggs"}],
             "analyzedInstructions":[{"name":"Cook","steps":[{"number":1,"step":"Cook rice."}]}]}
        """.trimIndent()))
        val detail = (repository.getRecipe(7) as RepositoryResult.Success).value
        assertEquals(15, detail.readyInMinutes)
        assertEquals("Cook rice.", detail.instructions.single().steps.single().instruction)
        assertNull(detail.sourceUrl)
    }
}
