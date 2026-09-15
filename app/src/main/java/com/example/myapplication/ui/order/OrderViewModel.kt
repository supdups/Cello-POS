package com.example.myapplication.ui.order

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.Product
import com.example.myapplication.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderUiState(
    val menu: List<Product> = emptyList(),
    val cart: Map<Long, Int> = emptyMap() // productId -> quantity
) {
    val total: Double
        get() = cart.entries.sumOf { (id, qty) ->
            (menu.firstOrNull { it.id == id }?.price ?: 0.0) * qty
        }

    val itemCount: Int
        get() = cart.values.sum()
}

class OrderViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.allProducts.collect { products ->
                _uiState.update { it.copy(menu = products) }
            }
        }
    }

    fun addToCart(productId: Long) {
        _uiState.update { state ->
            val updatedCart = state.cart.toMutableMap()
            updatedCart[productId] = (updatedCart[productId] ?: 0) + 1
            state.copy(cart = updatedCart)
        }
    }

    fun removeFromCart(productId: Long) {
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

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val dao = AppDatabase.getInstance(context).productDao()
            @Suppress("UNCHECKED_CAST")
            return OrderViewModel(ProductRepository(dao)) as T
        }
    }
}
