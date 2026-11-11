package com.example.fridgetoplate.ui

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.fridgetoplate.R

@Composable
fun IngredientCategoryChip(
    category: IngredientCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val labelRes = when(category) {
        IngredientCategory.MEAT -> R.string.category_meat
        IngredientCategory.VEGETABLES -> R.string.category_vegetables
        IngredientCategory.OTHER -> R.string.category_other
    }

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(stringResource(labelRes)) },
        modifier = modifier
    )
}