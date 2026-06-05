package com.aerodue.app.mcp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.mcpDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "aerodue_mcp_connectors")

/** Persists user-configured connectors as a JSON array (org.json, no extra deps). */
class McpConnectorStore(private val context: Context) {

    private val key = stringPreferencesKey("connectors_json")

    val connectors: Flow<List<McpConnector>> = context.mcpDataStore.data.map { p ->
        decode(p[key])
    }

    suspend fun save(connectors: List<McpConnector>) {
        context.mcpDataStore.edit { it[key] = encode(connectors) }
    }

    private fun encode(connectors: List<McpConnector>): String {
        val arr = JSONArray()
        connectors.forEach { c ->
            arr.put(
                JSONObject()
                    .put("id", c.id)
                    .put("name", c.name)
                    .put("kind", c.kind.name)
                    .put("endpointUrl", c.endpointUrl)
                    .put("authToken", c.authToken ?: JSONObject.NULL)
                    .put("modelId", c.modelId ?: JSONObject.NULL)
                    .put("enabled", c.enabled)
                    .put("termsAccepted", c.termsAccepted),
            )
        }
        return arr.toString()
    }

    private fun decode(raw: String?): List<McpConnector> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                McpConnector(
                    id = o.optString("id"),
                    name = o.optString("name"),
                    kind = runCatching { ConnectorKind.valueOf(o.optString("kind")) }
                        .getOrDefault(ConnectorKind.MCP_TOOLS),
                    endpointUrl = o.optString("endpointUrl"),
                    authToken = o.optString("authToken").takeIf { it.isNotBlank() && it != "null" },
                    modelId = o.optString("modelId").takeIf { it.isNotBlank() && it != "null" },
                    enabled = o.optBoolean("enabled"),
                    termsAccepted = o.optBoolean("termsAccepted"),
                )
            }
        }.getOrDefault(emptyList())
    }
}
