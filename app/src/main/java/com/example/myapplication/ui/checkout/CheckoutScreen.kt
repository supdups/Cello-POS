package com.example.myapplication.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CheckoutScreen(
    total: Double,
    lines: List<ReceiptLine>,
    onOrderComplete: () -> Unit,
    viewModel: CheckoutViewModel = viewModel(
        factory = CheckoutViewModel.Factory(LocalContext.current)
    )
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        when (state) {
            is CheckoutState.Success -> {
                Text("Order complete!", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "Receipt printed. Cash drawer opened.",
                    modifier = Modifier.padding(top = 8.dp)
                )
                Button(
                    onClick = {
                        viewModel.reset()
                        onOrderComplete()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    Text("New order")
                }
            }

            is CheckoutState.Processing -> {
                Text("Processing...", style = MaterialTheme.typography.headlineMedium)
            }

            is CheckoutState.Idle -> {
                Text("Checkout", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "Total: $${"%.2f".format(total)}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                Button(
                    onClick = { viewModel.completeOrder(lines, total, "cash") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pay with Cash")
                }

                OutlinedButton(
                    onClick = { viewModel.completeOrder(lines, total, "card") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text("Pay with Card")
                }
            }
        }
    }
}
