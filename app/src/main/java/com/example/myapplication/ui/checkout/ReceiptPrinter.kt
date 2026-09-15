package com.example.myapplication.ui.checkout

import android.util.Log

data class ReceiptLine(
    val name: String,
    val quantity: Int,
    val price: Double
)

/**
 * Abstraction over "print a receipt and open the cash drawer."
 * Swap in BluetoothReceiptPrinter (or a vendor SDK) once a printer is configured —
 * nothing else in the app needs to change.
 */
interface ReceiptPrinter {
    suspend fun printReceipt(lines: List<ReceiptLine>, total: Double, paymentType: String)
}

class StubReceiptPrinter : ReceiptPrinter {
    override suspend fun printReceipt(lines: List<ReceiptLine>, total: Double, paymentType: String) {
        val body = lines.joinToString("\n") { "${it.quantity}x ${it.name} - $${"%.2f".format(it.price * it.quantity)}" }
        Log.d(
            "ReceiptPrinter",
            "No printer configured. Would print:\n$body\nTotal: $${"%.2f".format(total)} ($paymentType)"
        )
    }
}
