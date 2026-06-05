package com.aerodue.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Gavel
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aerodue.app.ui.components.HeroHeader
import com.aerodue.app.ui.components.SectionCard
import com.aerodue.app.ui.components.TintChip
import com.aerodue.app.ui.theme.Money500
import com.aerodue.app.webview.WebViewDevConfig

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val webViewProvider = remember { WebViewDevConfig.providerLabel(context) }
    val homeAirport = remember { mutableStateOf("SFO") }
    val dotRules = remember { mutableStateOf(true) }
    val euRules = remember { mutableStateOf(false) }
    val backgroundGps = remember { mutableStateOf(true) }

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
                ToggleRow("Background GPS flight match", Icons.Outlined.LocationOn, backgroundGps.value) {
                    backgroundGps.value = it
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
