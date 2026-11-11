package com.example.fridgetoplate.ui

enum class IngredientCategory {
    VEGETABLES,
    MEAT,
    OTHER
}

data class FridgeIngredient(
    val id: Long,
    val name: String,
    val category: IngredientCategory
)