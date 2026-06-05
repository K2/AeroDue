package com.aerodue.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aerodue.app.AeroDueApplication
import com.aerodue.app.consent.ConsentState
import com.aerodue.app.ui.components.HeroHeader
import com.aerodue.app.ui.components.SectionCard
import com.aerodue.app.ui.components.TintChip
import com.aerodue.app.ui.theme.Amber500
import com.aerodue.app.ui.theme.Money500
import com.aerodue.app.webview.WebViewDevConfig
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(onOpenIntegrations: () -> Unit = {}) {
    val context = LocalContext.current
    val app = context.applicationContext as AeroDueApplication
    val scope = rememberCoroutineScope()
    val consent by app.consentStore.state.collectAsState(initial = ConsentState())
    val plugins = remember { app.pluginRegistry.available() }
    val webViewProvider = remember { WebViewDevConfig.providerLabel(context) }
    val homeAirport = remember { mutableStateOf("SFO") }
    val dotRules = remember { mutableStateOf(true) }
    val euRules = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        HeroHeader(
            eyebrow = "Profile",
            title = "Traveler settings",
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionCard(
                title = "Home base",
                icon = Icons.Outlined.MyLocation,
                accent = MaterialTheme.colorScheme.primary,
            ) {
                OutlinedTextField(
                    value = homeAirport.value,
                    onValueChange = { homeAirport.value = it },
                    label = { Text("Home airport") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                )
            }

            SectionCard(
                title = "Rules & monitoring",
                icon = Icons.Outlined.Tune,
                accent = Money500,
            ) {
                ToggleRow("U.S. DOT rules", Icons.Outlined.Gavel, dotRules.value) { dotRules.value = it }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ToggleRow("EU261 rules", Icons.Outlined.Public, euRules.value) { euRules.value = it }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ToggleRow("Location for flight detection", Icons.Outlined.LocationOn, consent.gpsTracking) { enabled ->
                    scope.launch { app.consentStore.setGpsTracking(enabled) }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ToggleRow("Share anonymized travel data", Icons.Outlined.Insights, consent.telemetrySharing) { enabled ->
                    scope.launch { app.consentStore.setTelemetrySharing(enabled) }
                }
            }

            SectionCard(
                title = "Extensions",
                icon = Icons.Outlined.Extension,
                accent = Amber500,
            ) {
                Text(
                    text = "Optional add-ons run under their own separate terms — not AeroDue's. Off by default.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenIntegrations)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Hub,
                        contentDescription = null,
                        tint = Money500,
                        modifier = Modifier.size(18.dp),
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp),
                    ) {
                        Text("AI integrations (MCP)", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Connect an MCP server or your own cloud model",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text("→", style = MaterialTheme.typography.titleMedium)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                plugins.forEachIndexed { index, entry ->
                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.plugin.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = entry.plugin.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TintChip(
                            label = if (entry.enabled) "Enabled" else "Later · separate terms",
                            tint = if (entry.enabled) Money500 else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            SectionCard(title = "System") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TintChip(label = "Engine: Kotlin :core", tint = MaterialTheme.colorScheme.primary)
                    TintChip(
                        label = ":llm ${com.aerodue.core.inference.ModelProfiles.defaultProfileId}",
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "System WebView: $webViewProvider",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.size(4.dp))
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    icon: ImageVector,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        )
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
