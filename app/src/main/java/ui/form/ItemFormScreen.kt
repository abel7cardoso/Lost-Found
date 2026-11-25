
package com.abel.lostandfound.ui.form

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ItemFormScreen(
    viewModel: ItemFormViewModel,
    onDone: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onDone()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Item Details", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = state.title,
            onValueChange = viewModel::onTitleChange,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        state.errors["title"]?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        OutlinedTextField(
            value = state.description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        state.errors["description"]?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        OutlinedTextField(
            value = state.location,
            onValueChange = viewModel::onLocationChange,
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth()
        )
        state.errors["location"]?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Text("Category")

        state.categories.forEach { category ->
            OutlinedButton(
                onClick = { viewModel.onCategoryChange(category) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (state.categoryId == category.id) {
                        "✓ ${category.name}"
                    } else {
                        category.name
                    }
                )
            }
        }

        state.errors["categoryId"]?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Text("Item type")

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            RadioButton(
                selected = state.itemType == "lost",
                onClick = { viewModel.onItemTypeChange("lost") }
            )
            Text("Lost")
        }

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            RadioButton(
                selected = state.itemType == "found",
                onClick = { viewModel.onItemTypeChange("found") }
            )
            Text("Found")
        }

        state.errors["itemType"]?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onDone,
                enabled = !state.isSaving,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = viewModel::save,
                enabled = !state.isSaving && !state.isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (state.isSaving) "Saving..." else "Save")
            }
        }
    }
}
