package com.example.myapplication.ui.checkout

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

/**
 * Talks to a generic ESC/POS Bluetooth thermal printer over classic Bluetooth (SPP).
 * This covers most inexpensive 58mm/80mm printers sold for POS use. If your printer
 * ships its own SDK (Epson ePOS, Sunmi built-in printer, etc.), swap this out for that —
 * the ReceiptPrinter interface is the only thing the rest of the app depends on.
 */
class BluetoothReceiptPrinter(
    private val context: Context,
    private val deviceAddress: String
) : ReceiptPrinter {

    companion object {
        private const val TAG = "BluetoothPrinter"
        // Standard Serial Port Profile UUID — works for the vast majority of SPP printers.
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission") // Permission is checked explicitly below before any Bluetooth call.
    override suspend fun printReceipt(lines: List<ReceiptLine>, total: Double, paymentType: String) {
        withContext(Dispatchers.IO) {
            if (!hasBluetoothConnectPermission()) {
                Log.e(TAG, "Missing BLUETOOTH_CONNECT permission — cannot print.")
                return@withContext
            }

            val adapter = context.getSystemService(BluetoothManager::class.java)?.adapter
            if (adapter == null || !adapter.isEnabled) {
                Log.e(TAG, "Bluetooth is off or unavailable — cannot print.")
                return@withContext
            }

            val device = adapter.bondedDevices.firstOrNull { it.address == deviceAddress }
            if (device == null) {
                Log.e(TAG, "Configured printer ($deviceAddress) is not paired — cannot print.")
                return@withContext
            }

            try {
                val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                socket.use {
                    it.connect()
                    writeReceipt(it.outputStream, lines, total, paymentType)
                }
            } catch (e: IOException) {
                // Common causes: printer off, out of range, or a printer that needs a
                // different UUID/insecure socket — see fallback note below.
                Log.e(TAG, "Failed to print: ${e.message}", e)
            }
        }
    }

    private fun hasBluetoothConnectPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun writeReceipt(
        out: OutputStream,
        lines: List<ReceiptLine>,
        total: Double,
        paymentType: String
    ) {
        val esc = 0x1B
        val gs = 0x1D

        fun text(s: String) = out.write((s + "\n").toByteArray(Charsets.UTF_8))

        // Initialize printer
        out.write(byteArrayOf(esc.toByte(), 0x40))

        text("Cello POS")
        text("--------------------------------")
        lines.forEach { line ->
            val lineTotal = "%.2f".format(line.price * line.quantity)
            text("${line.quantity}x ${line.name}".padEnd(24) + "$$lineTotal")
        }
        text("--------------------------------")
        text("TOTAL: $${"%.2f".format(total)}")
        text("Paid via: $paymentType")
        text(" ")
        text(" ")

        // Feed and cut (GS V 0 = full cut). Comment this out if your printer doesn't have a cutter.
        out.write(byteArrayOf(gs.toByte(), 0x56, 0x00))

        // Open the cash drawer (ESC p 0 25 250 — the common "kick pin 2" command).
        out.write(byteArrayOf(esc.toByte(), 0x70, 0x00, 0x19.toByte(), 0xFA.toByte()))

        out.flush()
    }
}
