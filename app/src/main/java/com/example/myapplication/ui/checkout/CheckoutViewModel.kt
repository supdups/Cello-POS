package com.example.myapplication.ui.checkout

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.Order
import com.example.myapplication.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CheckoutState {
    data object Idle : CheckoutState()
    data object Processing : CheckoutState()
    data object Success : CheckoutState()
}

class CheckoutViewModel(
    private val repository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val uiState: StateFlow<CheckoutState> = _uiState

    fun completeOrder(total: Double, paymentType: String) {
        viewModelScope.launch {
            _uiState.value = CheckoutState.Processing
            val order = Order(total = total, paymentType = paymentType)
            repository.saveOrderLocally(order)
            _uiState.value = CheckoutState.Success
        }
    }

    /**
     * Manual factory since Hilt isn't wired up yet.
     * We'll replace this with @HiltViewModel once dependency injection is added.
     */
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val dao = AppDatabase.getInstance(context).orderDao()
            @Suppress("UNCHECKED_CAST")
            return CheckoutViewModel(OrderRepository(dao)) as T
        }
    }
}
