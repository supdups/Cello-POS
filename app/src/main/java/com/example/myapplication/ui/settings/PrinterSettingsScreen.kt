package com.example.myapplication.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PrinterSettingsScreen(
    onBack: () -> Unit,
    viewModel: PrinterSettingsViewModel = viewModel(
        factory = PrinterSettingsViewModel.Factory(LocalContext.current)
    )
) {
    val state by viewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.loadPairedDevices()
    }

    fun requestDevicesRefresh() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            viewModel.loadPairedDevices()
        }
    }

    // Load the list once when the screen first opens.
    LaunchedEffect(Unit) { requestDevicesRefresh() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Printer Settings", style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = onBack) { Text("Back") }
        }

        Text(
            text = if (state.savedAddress != null) {
                "Current printer: ${state.savedName} (${state.savedAddress})"
            } else {
                "No printer selected — receipts will just log to Logcat until one is."
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp)
        )

        Row(modifier = Modifier.padding(top = 12.dp)) {
            Button(onClick = ::requestDevicesRefresh) { Text("Refresh paired devices") }
            if (state.savedAddress != null) {
                OutlinedButton(
                    onClick = viewModel::testPrint,
                    modifier = Modifier.padding(start = 8.dp)
                ) { Text("Test Print") }
            }
        }

        state.testStatus?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Pair your printer in Android's Bluetooth settings first — only already-paired devices show up here.",
            style = MaterialTheme.typography.bodySmall
        )

        LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
            items(state.pairedDevices, key = { it.address }) { device ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(device.name, style = MaterialTheme.typography.bodyLarge)
                        Text(device.address, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = { viewModel.selectPrinter(device) }) {
                        Text(if (device.address == state.savedAddress) "Selected" else "Use this")
                    }
                }
                HorizontalDivider()
            }
        }
    }
}
