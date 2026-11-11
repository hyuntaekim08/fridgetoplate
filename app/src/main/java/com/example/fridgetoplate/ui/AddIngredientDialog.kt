package com.example.fridgetoplate.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fridgetoplate.R

@Composable
fun AddIngredientDialog(
    onDismiss: () -> Unit,
    onAdd: (String, IngredientCategory) -> Unit
) {
    var ingredientName by rememberSaveable { mutableStateOf("") }
    var ingredientCategory by rememberSaveable { mutableStateOf(IngredientCategory.OTHER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text(stringResource(R.string.add_ingredient))},
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = ingredientName,
                    onValueChange = { ingredientName = it },
                    label = { Text(stringResource(R.string.ingredient_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IngredientCategory.entries.forEach { category ->
                        IngredientCategoryChip(
                            category = category,
                            selected = ingredientCategory == category,
                            onClick = { ingredientCategory = category }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = ingredientName.isNotBlank(),
                onClick = {
                    val name = ingredientName.trim()
                    if (name.isNotEmpty()) {
                        onAdd(name, ingredientCategory)
                    }
                }
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}