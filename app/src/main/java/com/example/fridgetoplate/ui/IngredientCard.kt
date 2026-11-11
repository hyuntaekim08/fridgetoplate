package com.example.fridgetoplate.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fridgetoplate.R

@Composable
fun IngredientCard(
    ingredient: FridgeIngredient,
    onDelete: (Long) -> Unit,
    modifier : Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.titleMedium
                )
            TextButton(onClick = { onDelete(ingredient.id) }) { Text(stringResource(R.string.delete_ingredient))}
        }
    }
}