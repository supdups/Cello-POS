package com.example.myapplication.ui.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.checkout.ReceiptLine

@Composable
fun OrderScreen(
    onCheckout: (total: Double, lines: List<ReceiptLine>) -> Unit,
    isAdmin: Boolean = false,
    onManageUsers: () -> Unit = {},
    onManageItems: () -> Unit = {},
    onPrinterSettings: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: OrderViewModel = viewModel(factory = OrderViewModel.Factory(LocalContext.current))
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Menu",
                style = MaterialTheme.typography.headlineMedium
            )
            Row {
                if (isAdmin) {
                    TextButton(onClick = onManageItems) { Text("Manage Items") }
                    TextButton(onClick = onManageUsers) { Text("Manage Users") }
                }
                TextButton(onClick = onPrinterSettings) { Text("Printer") }
                TextButton(onClick = onLogout) { Text("Logout") }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.menu) { product ->
                val quantity = state.cart[product.id] ?: 0

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = product.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "$${"%.2f".format(product.price)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (quantity > 0) {
                            OutlinedButton(onClick = { viewModel.removeFromCart(product.id) }) {
                                Text("-")
                            }
                            Text(
                                text = quantity.toString(),
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                        Button(onClick = { viewModel.addToCart(product.id) }) {
                            Text("+")
                        }
                    }
                }
                HorizontalDivider()
            }
        }

        // Cart summary + checkout, pinned to the bottom
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Items: ${state.itemCount}")
                Text("Total: $${"%.2f".format(state.total)}")
            }

            Button(
                onClick = {
                    val lines = state.cart.map { (productId, qty) ->
                        val product = state.menu.first { it.id == productId }
                        ReceiptLine(name = product.name, quantity = qty, price = product.price)
                    }
                    onCheckout(state.total, lines)
                },
                enabled = state.cart.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text("Checkout")
            }
        }
    }
}
