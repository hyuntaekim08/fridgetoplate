package com.example.fridgetoplate.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fridgetoplate.R

@Composable
fun FridgeRoute(modifier: Modifier = Modifier) {
    val ingredients = remember {
        mutableStateListOf(
            FridgeIngredient(1L, "Egg", IngredientCategory.OTHER),
            FridgeIngredient(2L, "Onion", IngredientCategory.VEGETABLES),
            FridgeIngredient(3L, "Carrot", IngredientCategory.VEGETABLES),
            FridgeIngredient(4L, "Chicken", IngredientCategory.MEAT)
        )
    }
    FridgeScreen(
        ingredients = ingredients,
        onAddIngredient = { name, category ->
            val nextId = (ingredients.maxOfOrNull { it.id } ?: 0L) + 1L
            ingredients.add(
                FridgeIngredient(nextId, name = name, category = category)
            )
        },
        onDeleteIngredient = { id ->
            ingredients.removeAll { it.id == id }
        },
        modifier
    )
}

@Composable
fun FridgeScreen(
    ingredients: List<FridgeIngredient>,
    onAddIngredient: (String, IngredientCategory) -> Unit,
    onDeleteIngredient: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by rememberSaveable{ mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf<IngredientCategory?>(null) }
    var showAddDialog by rememberSaveable { mutableStateOf(false)}
    val categories = IngredientCategory.entries

    val filterIngredients = ingredients.filter { ingredient ->
        val matchesSearch = ingredient.name.contains(
            searchQuery.trim(),
            ignoreCase = true
        )

        val matchesCategory =
            selectedCategory == null || ingredient.category == selectedCategory

        matchesSearch && matchesCategory
    }

    Column(modifier = modifier.fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "My Fridge",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "What do we cook today?",
            style = MaterialTheme.typography.bodyLarge
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {searchQuery = it},
            modifier = Modifier.fillMaxWidth()
        )

        // show category filters
        Row(modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        )
        {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text(stringResource(R.string.filter_all)) }
            )
            categories.forEach { category ->
                IngredientCategoryChip(
                    category = category,
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category }
                )
            }
        }

        // show ingredients cards
        if (filterIngredients.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text (
                    text = "No matching ingredients",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = filterIngredients,
                    key = { ingredient -> ingredient.id }
                ) { ingredient ->
                    IngredientCard(
                        ingredient,
                        onDelete = onDeleteIngredient
                    )
                }
            }
        }

        // render add ingredient buttons
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_ingredient))
        }
    }

    if (showAddDialog) {
        AddIngredientDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, category ->
                onAddIngredient(name, category)
                showAddDialog = false
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FridgeScreenPreview() {
    MaterialTheme {
        FridgeRoute()
    }
}