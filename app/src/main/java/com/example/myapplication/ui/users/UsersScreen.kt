package com.example.myapplication.ui.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun UsersScreen(
    onBack: () -> Unit,
    viewModel: UsersViewModel = viewModel(factory = UsersViewModel.Factory(LocalContext.current))
) {
    val users by viewModel.users.collectAsState()
    val form by viewModel.form.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Manage Users", style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = onBack) { Text("Back") }
        }

        // --- Create ---
        OutlinedTextField(
            value = form.username,
            onValueChange = viewModel::onUsernameChange,
            label = { Text("New username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )
        OutlinedTextField(
            value = form.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Row(modifier = Modifier.padding(top = 8.dp)) {
            FilterChip(
                selected = form.role == "cashier",
                onClick = { viewModel.onRoleChange("cashier") },
                label = { Text("Cashier") }
            )
            FilterChip(
                selected = form.role == "admin",
                onClick = { viewModel.onRoleChange("admin") },
                label = { Text("Admin") },
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
        }
        Button(
            onClick = viewModel::addUser,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Add User")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // --- Read / Update / Delete ---
        Text("Existing users", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
            items(users) { user ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(user.username, style = MaterialTheme.typography.bodyLarge)
                        Text(user.role, style = MaterialTheme.typography.bodySmall)
                    }
                    Row {
                        OutlinedButton(onClick = { viewModel.toggleRole(user) }) {
                            Text(if (user.role == "admin") "Make cashier" else "Make admin")
                        }
                        TextButton(onClick = { viewModel.deleteUser(user) }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}
