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
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aerodue.app.AeroDueApplication
import com.aerodue.app.mcp.ConnectorKind
import com.aerodue.app.mcp.ConnectorProbe
import com.aerodue.app.mcp.McpConnector
import com.aerodue.app.ui.components.HeroHeader
import com.aerodue.app.ui.components.SectionCard
import com.aerodue.app.ui.components.TintChip
import com.aerodue.app.ui.theme.Amber500
import com.aerodue.app.ui.theme.Money500
import com.aerodue.app.ui.theme.Rose500
import kotlinx.coroutines.launch

@Composable
fun IntegrationsScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as AeroDueApplication
    val registry = app.connectorRegistry
    val scope = rememberCoroutineScope()
    val connectors by registry.state.collectAsState()

    var name by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(ConnectorKind.MCP_TOOLS) }
    var url by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var modelId by remember { mutableStateOf("") }
    var addProbe by remember { mutableStateOf<ConnectorProbe?>(null) }
    var testing by remember { mutableStateOf(false) }

    val probeResults = remember { mutableStateMapOf<String, ConnectorProbe>() }
    val probing = remember { mutableStateMapOf<String, Boolean>() }

    fun draft() = McpConnector(
        id = "",
        name = name.trim().ifBlank { if (kind == ConnectorKind.CLOUD_MODEL) "Cloud model" else "MCP server" },
        kind = kind,
        endpointUrl = url.trim(),
        authToken = token.trim().ifBlank { null },
        modelId = modelId.trim().ifBlank { null },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        HeroHeader(
            eyebrow = "Integrations",
            title = "Connect your AI",
            subtitle = "Plug an MCP server or your own cloud model into AeroDue's assistant. On-device by default.",
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionCard(title = "How this works", icon = Icons.Outlined.Hub, accent = Money500) {
                Text(
                    text = "AeroDue runs an on-device model by default. Add a connector to route the " +
                        "assistant through your own infrastructure or extend it with external tools. " +
                        "Connectors are OFF until you enable them, and run under the provider's own terms — " +
                        "enabling one sends trip data off-device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionCard(title = "Add a connector", icon = Icons.Outlined.Bolt, accent = Amber500) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KindChip("MCP server", kind == ConnectorKind.MCP_TOOLS) {
                        kind = ConnectorKind.MCP_TOOLS; addProbe = null
                    }
                    KindChip("Cloud model", kind == ConnectorKind.CLOUD_MODEL) {
                        kind = ConnectorKind.CLOUD_MODEL; addProbe = null
                    }
                }
                Spacer(Modifier.height(10.dp))
                Field(name, "Name", { name = it })
                Spacer(Modifier.height(8.dp))
                Field(
                    url,
                    if (kind == ConnectorKind.CLOUD_MODEL) "Endpoint (…/v1/chat/completions)" else "MCP endpoint URL",
                    { url = it },
                )
                Spacer(Modifier.height(8.dp))
                Field(token, "API key / token (optional)", { token = it })
                if (kind == ConnectorKind.CLOUD_MODEL) {
                    Spacer(Modifier.height(8.dp))
                    Field(modelId, "Model id (e.g. gpt-4o-mini)", { modelId = it })
                }

                addProbe?.let { p ->
                    Spacer(Modifier.height(10.dp))
                    ProbeLine(p)
                }

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                testing = true
                                addProbe = registry.probe(draft())
                                testing = false
                            }
                        },
                        enabled = url.isNotBlank() && !testing,
                    ) {
                        if (testing) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.size(8.dp))
                        }
                        Text("Test")
                    }
                    Button(
                        onClick = {
                            scope.launch { registry.upsert(draft()) }
                            name = ""; url = ""; token = ""; modelId = ""; addProbe = null
                        },
                        enabled = url.isNotBlank(),
                    ) { Text("Save") }
                }
            }

            if (connectors.isEmpty()) {
                Text(
                    text = "No connectors yet. Add one above to extend the assistant.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            connectors.forEach { c ->
                ConnectorCard(
                    connector = c,
                    probe = probeResults[c.id],
                    probing = probing[c.id] == true,
                    onToggle = { enabled ->
                        scope.launch { registry.setEnabled(c.id, enabled, termsAccepted = true) }
                    },
                    onTest = {
                        scope.launch {
                            probing[c.id] = true
                            probeResults[c.id] = registry.probe(c)
                            probing[c.id] = false
                        }
                    },
                    onRemove = { scope.launch { registry.remove(c.id) } },
                )
            }

            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun KindChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(),
    )
}

@Composable
private fun Field(value: String, label: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
    )
}

@Composable
private fun ProbeLine(p: ConnectorProbe) {
    Column {
        Text(
            text = (if (p.ok) "✓ " else "✕ ") + p.message,
            style = MaterialTheme.typography.bodySmall,
            color = if (p.ok) Money500 else Rose500,
            fontWeight = FontWeight.Bold,
        )
        if (p.tools.isNotEmpty()) {
            p.tools.take(6).forEach {
                Text(
                    text = "• ${it.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ConnectorCard(
    connector: McpConnector,
    probe: ConnectorProbe?,
    probing: Boolean,
    onToggle: (Boolean) -> Unit,
    onTest: () -> Unit,
    onRemove: () -> Unit,
) {
    val kindLabel = if (connector.kind == ConnectorKind.CLOUD_MODEL) "Cloud model" else "MCP server"
    val kindColor = if (connector.kind == ConnectorKind.CLOUD_MODEL) Amber500 else Money500
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (connector.kind == ConnectorKind.CLOUD_MODEL)
                    Icons.Outlined.Cloud else Icons.Outlined.Hub,
                contentDescription = null,
                tint = kindColor,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.size(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(connector.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = connector.endpointUrl,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Switch(checked = connector.enabled, onCheckedChange = onToggle)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TintChip(label = kindLabel, tint = kindColor)
            if (connector.enabled) TintChip(label = "Active", tint = Money500)
            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = onTest, enabled = !probing) {
                if (probing) {
                    CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.size(6.dp))
                }
                Text("Test")
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Outlined.Delete, contentDescription = "Remove", tint = Rose500)
            }
        }
        probe?.let {
            Spacer(Modifier.height(8.dp))
            ProbeLine(it)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Runs under the provider's terms — separate from AeroDue's EULA. Enabling sends data off-device.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
