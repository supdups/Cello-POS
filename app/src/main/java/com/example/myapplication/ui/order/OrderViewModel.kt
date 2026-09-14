package com.example.myapplication.ui.order

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class OrderUiState(
    val menu: List<Product> = sampleMenu,
    val cart: Map<String, Int> = emptyMap() // productId -> quantity
) {
    val total: Double
        get() = cart.entries.sumOf { (id, qty) ->
            (menu.firstOrNull { it.id == id }?.price ?: 0.0) * qty
        }

    val itemCount: Int
        get() = cart.values.sum()
}

class OrderViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState

    fun addToCart(productId: String) {
        _uiState.update { state ->
            val updatedCart = state.cart.toMutableMap()
            updatedCart[productId] = (updatedCart[productId] ?: 0) + 1
            state.copy(cart = updatedCart)
        }
    }

    fun removeFromCart(productId: String) {
        _uiState.update { state ->
            val updatedCart = state.cart.toMutableMap()
            val current = updatedCart[productId] ?: 0
            if (current <= 1) {
                updatedCart.remove(productId)
            } else {
                updatedCart[productId] = current - 1
            }
            state.copy(cart = updatedCart)
        }
    }

    fun clearCart() {
        _uiState.update { it.copy(cart = emptyMap()) }
    }
}
