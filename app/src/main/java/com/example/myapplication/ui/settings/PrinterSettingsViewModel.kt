package com.example.myapplication.ui.settings

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.session.PrinterSettingsManager
import com.example.myapplication.ui.checkout.BluetoothReceiptPrinter
import com.example.myapplication.ui.checkout.ReceiptLine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PairedDevice(val name: String, val address: String)

data class PrinterSettingsUiState(
    val savedAddress: String? = null,
    val savedName: String? = null,
    val pairedDevices: List<PairedDevice> = emptyList(),
    val testStatus: String? = null
)

class PrinterSettingsViewModel(
    private val context: Context,
    private val settingsManager: PrinterSettingsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrinterSettingsUiState())
    val uiState: StateFlow<PrinterSettingsUiState> = _uiState

    init {
        viewModelScope.launch {
            settingsManager.printerAddress.collect { address ->
                _uiState.update { it.copy(savedAddress = address) }
            }
        }
        viewModelScope.launch {
            settingsManager.printerName.collect { name ->
                _uiState.update { it.copy(savedName = name) }
            }
        }
    }

    /** Call after BLUETOOTH_CONNECT permission has been granted. */
    @SuppressLint("MissingPermission")
    fun loadPairedDevices() {
        val adapter = context.getSystemService(BluetoothManager::class.java)?.adapter ?: return
        val devices = adapter.bondedDevices.map { PairedDevice(it.name ?: it.address, it.address) }
        _uiState.update { it.copy(pairedDevices = devices) }
    }

    fun selectPrinter(device: PairedDevice) {
        viewModelScope.launch {
            settingsManager.savePrinter(device.address, device.name)
        }
    }

    fun clearPrinter() {
        viewModelScope.launch {
            settingsManager.clearPrinter()
        }
    }

    fun testPrint() {
        val address = _uiState.value.savedAddress ?: run {
            _uiState.update { it.copy(testStatus = "No printer selected yet.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(testStatus = "Printing test receipt...") }
            val printer = BluetoothReceiptPrinter(context, address)
            printer.printReceipt(
                lines = listOf(ReceiptLine("Test Item", 1, 1.00)),
                total = 1.00,
                paymentType = "test"
            )
            _uiState.update { it.copy(testStatus = "Sent. Check the printer (and Logcat if nothing happened).") }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return PrinterSettingsViewModel(context, PrinterSettingsManager(context)) as T
        }
    }
}
