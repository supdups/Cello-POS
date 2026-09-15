package com.example.myapplication.ui.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ItemsScreen(
    onBack: () -> Unit,
    viewModel: ItemsViewModel = viewModel(factory = ItemsViewModel.Factory(LocalContext.current))
) {
    val products by viewModel.products.collectAsState()
    val form by viewModel.form.collectAsState()
    val error by viewModel.error.collectAsState()
    val editingId by viewModel.editingId.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Manage Items", style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = onBack) { Text("Back") }
        }

        // --- Create / Edit form (same form is reused for both) ---
        OutlinedTextField(
            value = form.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Item name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )
        OutlinedTextField(
            value = form.price,
            onValueChange = viewModel::onPriceChange,
            label = { Text("Price") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
        }

        Row(modifier = Modifier.padding(top = 8.dp)) {
            val editingProduct = products.firstOrNull { it.id == editingId }
            if (editingProduct != null) {
                Button(onClick = { viewModel.saveEdit(editingProduct) }) { Text("Save changes") }
                TextButton(
                    onClick = viewModel::cancelEditing,
                    modifier = Modifier.padding(start = 8.dp)
                ) { Text("Cancel") }
            } else {
                Button(onClick = viewModel::addProduct) { Text("Add Item") }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // --- Read / Update / Delete ---
        Text("Current menu", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
            items(products, key = { it.id }) { product ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(product.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "$${"%.2f".format(product.price)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Row {
                        TextButton(onClick = { viewModel.startEditing(product) }) { Text("Edit") }
                        TextButton(onClick = { viewModel.deleteProduct(product) }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}
