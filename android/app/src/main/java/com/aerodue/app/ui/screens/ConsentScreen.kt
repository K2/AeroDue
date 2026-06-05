package com.aerodue.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aerodue.app.AeroDueApplication
import com.aerodue.app.ui.components.HeroHeader
import com.aerodue.app.ui.components.SectionCard
import com.aerodue.app.ui.theme.Money500
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun ConsentScreen(onContinue: () -> Unit) {
    val app = LocalContext.current.applicationContext as AeroDueApplication
    val scope = rememberCoroutineScope()

    var gps by remember { mutableStateOf(true) }
    var telemetry by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val current = app.consentStore.state.first()
        if (current.onboardingComplete) {
            onContinue()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        HeroHeader(
            eyebrow = "Before we start",
            title = "Your data, your call",
            subtitle = "AeroDue is free because you let it watch your trip to find what you're owed. You choose what to share.",
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ConsentToggle(
                icon = Icons.Outlined.LocationOn,
                accent = MaterialTheme.colorScheme.primary,
                title = "Location for flight detection",
                body = "Use GPS to detect delays and cancellations automatically so we can prepare claims without you lifting a finger. Used only for your trips.",
                checked = gps,
                onCheckedChange = { gps = it },
            )

            ConsentToggle(
                icon = Icons.Outlined.Insights,
                accent = Money500,
                title = "Share anonymized travel data",
                body = "Contribute de-identified door-to-door routing (no name, no account) to improve premium optimizations. Optional — the free claim service works without it.",
                checked = telemetry,
                onCheckedChange = { telemetry = it },
            )

            SectionCard(title = "What we never do", icon = Icons.Outlined.Lock) {
                BulletLine("Sell data tied to your identity")
                BulletLine("Track you when location is off")
                BulletLine("Charge a cut of your rebates")
            }

            Text(
                text = "You can change these anytime in Profile. See how data is used in our privacy notice.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
            )

            Button(
                onClick = {
                    scope.launch {
                        app.consentStore.completeOnboarding(
                            gpsTracking = gps,
                            telemetrySharing = telemetry,
                        )
                        onContinue()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(top = 4.dp),
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    "Agree & continue",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                )
            }

            TextButton(
                onClick = {
                    scope.launch {
                        app.consentStore.completeOnboarding(
                            gpsTracking = false,
                            telemetrySharing = false,
                        )
                        onContinue()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Continue without sharing")
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ConsentToggle(
    icon: ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SectionCard(title = title, icon = icon, accent = accent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.size(12.dp))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun BulletLine(text: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(
            text = "•  ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
