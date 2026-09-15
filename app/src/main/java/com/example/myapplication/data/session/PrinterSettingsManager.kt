package com.example.myapplication.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.printerDataStore by preferencesDataStore(name = "printer_prefs")

/** Persists which paired Bluetooth device to use as the receipt printer. */
class PrinterSettingsManager(private val context: Context) {

    private val addressKey = stringPreferencesKey("printer_mac_address")
    private val nameKey = stringPreferencesKey("printer_name")

    val printerAddress: Flow<String?> = context.printerDataStore.data.map { it[addressKey] }
    val printerName: Flow<String?> = context.printerDataStore.data.map { it[nameKey] }

    suspend fun savePrinter(address: String, name: String) {
        context.printerDataStore.edit { prefs ->
            prefs[addressKey] = address
            prefs[nameKey] = name
        }
    }

    suspend fun clearPrinter() {
        context.printerDataStore.edit { prefs ->
            prefs.remove(addressKey)
            prefs.remove(nameKey)
        }
    }
}
