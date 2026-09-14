package com.example.myapplication.ui.checkout

import android.util.Log

/**
 * Abstraction over "print a receipt and open the cash drawer."
 * Swap StubReceiptPrinter for a real implementation once you pick a printer SDK —
 * nothing else in the app needs to change.
 */
interface ReceiptPrinter {
    fun printReceipt(total: Double, paymentType: String)
}

class StubReceiptPrinter : ReceiptPrinter {
    override fun printReceipt(total: Double, paymentType: String) {
        // TODO: replace with real ESC/POS printer SDK calls (ties into the receipt printer + cash drawer).
        Log.d("ReceiptPrinter", "Printing receipt: $$total paid by $paymentType. Opening cash drawer.")
    }
}
