package com.aerodue.app.mcp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Single source of truth for MCP / cloud-model connectors. Backed by
 * [McpConnectorStore] for persistence and [RemoteConnectorClient] for I/O.
 */
class McpConnectorRegistry(
    private val store: McpConnectorStore,
    private val client: RemoteConnectorClient,
    scope: CoroutineScope,
) {
    private val _state = MutableStateFlow<List<McpConnector>>(emptyList())
    val state: StateFlow<List<McpConnector>> = _state.asStateFlow()

    init {
        scope.launch {
            store.connectors.collect { _state.value = it }
        }
    }

    suspend fun upsert(connector: McpConnector) {
        val id = connector.id.ifBlank { UUID.randomUUID().toString() }
        val withId = connector.copy(id = id)
        val next = _state.value.filterNot { it.id == id } + withId
        store.save(next)
    }

    suspend fun remove(id: String) {
        store.save(_state.value.filterNot { it.id == id })
    }

    /** Enabling a connector requires accepting its provider terms. */
    suspend fun setEnabled(id: String, enabled: Boolean, termsAccepted: Boolean) {
        val next = _state.value.map {
            if (it.id != id) it
            else it.copy(enabled = enabled && termsAccepted, termsAccepted = it.termsAccepted || termsAccepted)
        }
        store.save(next)
    }

    suspend fun probe(connector: McpConnector): ConnectorProbe = client.probe(connector)

    /** First enabled cloud-model connector, if any (used to route assistant text). */
    fun activeModelConnector(): McpConnector? =
        _state.value.firstOrNull { it.enabled && it.kind == ConnectorKind.CLOUD_MODEL }

    fun enabledToolConnectors(): List<McpConnector> =
        _state.value.filter { it.enabled && it.kind == ConnectorKind.MCP_TOOLS }

    /** Route a generation to the active cloud model, or null to fall back on-device. */
    suspend fun generate(prompt: String, system: String?): String? {
        val connector = activeModelConnector() ?: return null
        return client.generate(connector, prompt, system)
    }
}
