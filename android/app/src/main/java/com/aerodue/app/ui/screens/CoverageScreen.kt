package com.aerodue.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aerodue.app.ui.components.HeroHeader
import com.aerodue.app.ui.components.SectionCard
import com.aerodue.app.ui.theme.Amber500
import com.aerodue.app.ui.theme.Money600

@Composable
fun CoverageScreen() {
    val carrier = remember { mutableStateOf("United") }
    val card = remember { mutableStateOf("Chase Sapphire Reserve") }
    val employer = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        HeroHeader(
            eyebrow = "Your coverage",
            title = "Stack every protection",
            subtitle = "These profiles drive offline eligibility checks alongside regulations.",
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionCard(
                title = "Primary carrier & status",
                icon = Icons.Outlined.Flight,
                accent = MaterialTheme.colorScheme.primary,
            ) {
                OutlinedTextField(
                    value = carrier.value,
                    onValueChange = { carrier.value = it },
                    label = { Text("Airline / loyalty program") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                )
            }

            SectionCard(
                title = "Credit card trip protection",
                icon = Icons.Outlined.CreditCard,
                accent = Money600,
            ) {
                OutlinedTextField(
                    value = card.value,
                    onValueChange = { card.value = it },
                    label = { Text("Card product") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                )
            }

            SectionCard(
                title = "Business travel policy",
                icon = Icons.Outlined.Business,
                accent = Amber500,
            ) {
                OutlinedTextField(
                    value = employer.value,
                    onValueChange = { employer.value = it },
                    label = { Text("Employer (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                )
            }

            Button(
                onClick = { /* persist via DataStore — next iteration */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(top = 4.dp),
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    "Save coverage profile",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}
