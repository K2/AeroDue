package com.aerodue.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(onSignedIn: () -> Unit) {
    val email = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "AeroDue",
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = "Know what you're owed when travel goes wrong — fully offline on your device.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp),
        )
        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Button(
            onClick = onSignedIn,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = email.value.contains("@"),
        ) {
            Text("Continue")
        }
        Text(
            text = "Account data stays on-device. No cloud required for assessments.",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 24.dp),
            textAlign = TextAlign.Center,
        )
    }
}
