package com.aerodue.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aerodue.app.AeroDueApplication
import com.aerodue.core.demo.DemoFixtures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ClaimsScreen() {
    val app = LocalContext.current.applicationContext as AeroDueApplication
    val service = app.compensationService
    val claims = remember { service.assessDemo() }
    var summary by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(service) {
        loading = true
        withContext(Dispatchers.IO) {
            summary = service.passengerSummary(
                DemoFixtures.delayedConnectionEvent,
                DemoFixtures.demoProfile,
                claims,
            )
        }
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text("Recommended claims", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Rules: Kotlin · rationale: LiteRT-LM when model is installed",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.padding(bottom = 12.dp))
        } else {
            Text(
                summary.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
        LazyColumn {
            items(claims, key = { "${it.source}-${it.title}" }) { claim ->
                Card(modifier = Modifier.padding(vertical = 6.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(claim.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            claim.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Text(
                            "${claim.source.name.lowercase()} · ${(claim.confidence * 100).toInt()}% confidence",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
