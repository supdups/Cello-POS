package com.example.myapplication.ui.checkout

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.Order
import com.example.myapplication.data.repository.OrderRepository
import com.example.myapplication.data.session.PrinterSettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class CheckoutState {
    data object Idle : CheckoutState()
    data object Processing : CheckoutState()
    data object Success : CheckoutState()
}

class CheckoutViewModel(
    private val context: Context,
    private val repository: OrderRepository,
    private val printerSettings: PrinterSettingsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val uiState: StateFlow<CheckoutState> = _uiState

    fun completeOrder(lines: List<ReceiptLine>, total: Double, paymentType: String) {
        viewModelScope.launch {
            _uiState.value = CheckoutState.Processing

            // Cash orders save immediately (offline-friendly).
            // Card/QRIS orders would confirm with a payment gateway here before saving —
            // left as a TODO since it depends on which gateway you integrate.
            val order = Order(total = total, paymentType = paymentType)
            repository.saveOrderLocally(order)

            val printer = resolvePrinter()
            printer.printReceipt(lines, total, paymentType)

            _uiState.value = CheckoutState.Success
        }
    }

    private suspend fun resolvePrinter(): ReceiptPrinter {
        val address = printerSettings.printerAddress.first()
        return if (address != null) {
            BluetoothReceiptPrinter(context, address)
        } else {
            StubReceiptPrinter()
        }
    }

    fun reset() {
        _uiState.value = CheckoutState.Idle
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val dao = AppDatabase.getInstance(context).orderDao()
            @Suppress("UNCHECKED_CAST")
            return CheckoutViewModel(
                context = context.applicationContext,
                repository = OrderRepository(dao),
                printerSettings = PrinterSettingsManager(context)
            ) as T
        }
    }
}
