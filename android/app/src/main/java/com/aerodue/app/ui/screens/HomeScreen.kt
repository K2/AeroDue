package com.aerodue.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aerodue.app.AeroDueApplication
import com.aerodue.app.service.TripMonitorService
import com.aerodue.core.inference.ModelProfiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen() {
    val app = LocalContext.current.applicationContext as AeroDueApplication
    val status = TripMonitorService.sampleStatus()
    var summary by remember { mutableStateOf<String?>(null) }
    var claimCount by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(app.compensationService) {
        loading = true
        claimCount = app.compensationService.assessDemo().size
        withContext(Dispatchers.IO) {
            app.llmRunner.warmUp()
            summary = app.compensationService.demoPassengerSummary()
        }
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text("Active trip", style = MaterialTheme.typography.headlineSmall)
        Card(
            modifier = Modifier.padding(top = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(status.headline, style = MaterialTheme.typography.titleMedium)
                Text(
                    status.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    "GPS: ${status.airportGuess ?: "acquiring…"} · $claimCount paths found",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        } else {
            Text(
                text = summary.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
        val llmStatus = if (app.llmRunner.isModelLoaded) "LiteRT-LM loaded" else "rules only"
        Text(
            text = "Engine: Kotlin · LLM: ${ModelProfiles.defaultProfileId} ($llmStatus)",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
