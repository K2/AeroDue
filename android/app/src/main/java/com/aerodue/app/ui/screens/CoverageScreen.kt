package com.aerodue.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CoverageScreen() {
    val carrier = remember { mutableStateOf("United") }
    val card = remember { mutableStateOf("Chase Sapphire Reserve") }
    val employer = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Your coverage", style = MaterialTheme.typography.headlineSmall)
        Text(
            "These profiles drive offline eligibility checks alongside regulations.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        SectionTitle("Primary carrier & status")
        OutlinedTextField(
            value = carrier.value,
            onValueChange = { carrier.value = it },
            label = { Text("Airline / loyalty program") },
            modifier = Modifier.fillMaxWidth(),
        )

        SectionTitle("Credit card trip protection")
        OutlinedTextField(
            value = card.value,
            onValueChange = { card.value = it },
            label = { Text("Card product") },
            modifier = Modifier.fillMaxWidth(),
        )

        SectionTitle("Business travel policy")
        OutlinedTextField(
            value = employer.value,
            onValueChange = { employer.value = it },
            label = { Text("Employer (optional)") },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = { /* persist via DataStore — next iteration */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        ) {
            Text("Save coverage profile")
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}
