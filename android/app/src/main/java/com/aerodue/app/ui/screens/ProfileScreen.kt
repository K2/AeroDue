package com.aerodue.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen() {
    val homeAirport = remember { mutableStateOf("SFO") }
    val dotRules = remember { mutableStateOf(true) }
    val euRules = remember { mutableStateOf(false) }
    val backgroundGps = remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = homeAirport.value,
            onValueChange = { homeAirport.value = it },
            label = { Text("Home airport") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        )

        ToggleRow("U.S. DOT rules", dotRules.value) { dotRules.value = it }
        ToggleRow("EU261 rules", euRules.value) { euRules.value = it }
        ToggleRow("Background GPS for flight match", backgroundGps.value) { backgroundGps.value = it }

        Text(
            text = "Engine: Kotlin :core · LLM: :llm LiteRT-LM (${com.aerodue.core.inference.ModelProfiles.defaultProfileId})",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 24.dp),
        )
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onChecked)
        }
    }
}
