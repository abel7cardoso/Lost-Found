
package com.abel.lostandfound.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abel.lostandfound.data.model.LostFoundItem
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel,
    onAddItem: () -> Unit = {},
    onEditItem: (String) -> Unit = {}
) {
    val state by homeViewModel.uiState.collectAsState()

    var itemToDelete by remember { mutableStateOf<LostFoundItem?>(null) }

    // E1: Search
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(state.message) {
        if (state.message != null) {
            delay(3000)
            homeViewModel.clearMessage()
        }
    }

    // E1: Filter by title and location, ignoring letter case
    val filteredItems = state.items.filter { item ->
        item.title.contains(searchQuery, ignoreCase = true) ||
                item.location.contains(searchQuery, ignoreCase = true)
    }

    // E7: Statistics calculated from all items
    val lostCount = state.items.count { it.itemType.equals("lost", ignoreCase = true) }
    val foundCount = state.items.count { it.itemType.equals("found", ignoreCase = true) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Lost & Found",
                    style = MaterialTheme.typography.headlineMedium
                )

                TextButton(onClick = onLogout) {
                    Text("Log Out")
                }
            }

            Button(
                onClick = onAddItem,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Item")
            }

            state.message?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.claims.isNotEmpty()) {
                Text(
                    "Claims received",
                    style = MaterialTheme.typography.titleLarge
                )

                state.claims.forEach { claim ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Someone thinks this item is theirs:",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                claim.itemTitle,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text("Contact: ${claim.claimantEmail}")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                "All items",
                style = MaterialTheme.typography.titleLarge
            )

            // E7: Statistics header
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "$lostCount",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text("LOST")
                    }
                }

                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "$foundCount",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("FOUND")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // E1: Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search by title or location") },
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        TextButton(onClick = { searchQuery = "" }) {
                            Text("Clear")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // E3: Different empty and no-results messages
            when {
                state.isLoading -> {
                    CircularProgressIndicator()
                }

                state.items.isEmpty() -> {
                    Text("No items yet. Add your first item!")
                }

                filteredItems.isEmpty() -> {
                    Text("No items match your search.")
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredItems) { item ->
                            val isOwner =
                                item.ownerId == state.currentUserId

                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            item.title,
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        Text(
                                            item.itemType.uppercase(),
                                            color = if (item.itemType == "found") {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.error
                                            }
                                        )
                                    }

                                    Text("Location: ${item.location}")
                                    Text("Category: ${item.categoryName}")
                                    Text(item.description)

                                    if (isOwner) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    onEditItem(item.id)
                                                }
                                            ) {
                                                Text("Edit")
                                            }

                                            TextButton(
                                                onClick = {
                                                    itemToDelete = item
                                                }
                                            ) {
                                                Text("Delete")
                                            }
                                        }
                                    } else if (item.itemType == "found") {
                                        Button(
                                            onClick = {
                                                homeViewModel.submitClaim(item)
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("I think this is mine")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = {
                itemToDelete = null
            },
            title = {
                Text("Delete item?")
            },
            text = {
                Text("Are you sure you want to delete '${item.title}'?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        homeViewModel.deleteItem(item.id)
                        itemToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        itemToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
