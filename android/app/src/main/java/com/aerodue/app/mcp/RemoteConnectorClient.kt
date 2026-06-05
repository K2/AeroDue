package com.aerodue.app.mcp

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Minimal-dependency client for the two connector kinds:
 *  - [ConnectorKind.MCP_TOOLS]: JSON-RPC 2.0 over HTTP (MCP `initialize` + `tools/list` + `tools/call`).
 *  - [ConnectorKind.CLOUD_MODEL]: OpenAI-compatible `chat/completions`.
 *
 * Uses HttpURLConnection + org.json so it adds no third-party dependencies.
 */
class RemoteConnectorClient {

    private data class HttpResult(
        val code: Int,
        val body: String,
        val sessionId: String?,
    )

    suspend fun probe(connector: McpConnector): ConnectorProbe = withContext(Dispatchers.IO) {
        try {
            when (connector.kind) {
                ConnectorKind.CLOUD_MODEL -> probeCloudModel(connector)
                ConnectorKind.MCP_TOOLS -> probeMcp(connector)
            }
        } catch (e: Exception) {
            Log.w(TAG, "probe failed", e)
            ConnectorProbe(ok = false, message = e.message ?: "Connection failed")
        }
    }

    /** Generate text from a cloud-model connector, or null on failure. */
    suspend fun generate(connector: McpConnector, prompt: String, system: String?): String? =
        withContext(Dispatchers.IO) {
            if (connector.kind != ConnectorKind.CLOUD_MODEL) return@withContext null
            try {
                val body = JSONObject()
                    .put("model", connector.modelId ?: "gpt-4o-mini")
                    .put("temperature", 0.3)
                    .put(
                        "messages",
                        JSONArray().apply {
                            if (!system.isNullOrBlank()) {
                                put(JSONObject().put("role", "system").put("content", system))
                            }
                            put(JSONObject().put("role", "user").put("content", prompt))
                        },
                    )
                val res = post(connector.endpointUrl, connector.authToken, body.toString(), accept = "application/json")
                if (res.code !in 200..299) return@withContext null
                JSONObject(res.body)
                    .optJSONArray("choices")
                    ?.optJSONObject(0)
                    ?.optJSONObject("message")
                    ?.optString("content")
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                Log.w(TAG, "cloud generate failed", e)
                null
            }
        }

    /** Call an MCP tool, returning a text rendering of the result, or null. */
    suspend fun callTool(connector: McpConnector, tool: String, argumentsJson: String): String? =
        withContext(Dispatchers.IO) {
            if (connector.kind != ConnectorKind.MCP_TOOLS) return@withContext null
            try {
                val init = rpc(connector, 1, "initialize", initializeParams(), sessionId = null)
                val params = JSONObject()
                    .put("name", tool)
                    .put("arguments", JSONObject(argumentsJson))
                val res = rpc(connector, 2, "tools/call", params, init.sessionId)
                parseRpc(res.body)?.optJSONArray("content")?.let { content ->
                    (0 until content.length()).joinToString("\n") {
                        content.optJSONObject(it)?.optString("text").orEmpty()
                    }.trim().takeIf { it.isNotBlank() }
                }
            } catch (e: Exception) {
                Log.w(TAG, "tool call failed", e)
                null
            }
        }

    private fun probeCloudModel(connector: McpConnector): ConnectorProbe {
        val body = JSONObject()
            .put("model", connector.modelId ?: "gpt-4o-mini")
            .put("max_tokens", 1)
            .put(
                "messages",
                JSONArray().put(JSONObject().put("role", "user").put("content", "ping")),
            )
        val res = post(connector.endpointUrl, connector.authToken, body.toString(), accept = "application/json")
        return if (res.code in 200..299) {
            ConnectorProbe(true, "Reachable · model ${connector.modelId ?: "default"}")
        } else {
            ConnectorProbe(false, "HTTP ${res.code}: ${res.body.take(120)}")
        }
    }

    private fun probeMcp(connector: McpConnector): ConnectorProbe {
        val init = rpc(connector, 1, "initialize", initializeParams(), sessionId = null)
        if (init.code !in 200..299) {
            return ConnectorProbe(false, "initialize HTTP ${init.code}: ${init.body.take(120)}")
        }
        val list = rpc(connector, 2, "tools/list", JSONObject(), init.sessionId)
        val tools = parseRpc(list.body)?.optJSONArray("tools")?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                arr.optJSONObject(i)?.let {
                    RemoteTool(
                        name = it.optString("name"),
                        description = it.optString("description"),
                    )
                }
            }
        }.orEmpty()
        return ConnectorProbe(true, "Connected · ${tools.size} tool(s)", tools)
    }

    private fun initializeParams(): JSONObject = JSONObject()
        .put("protocolVersion", "2024-11-05")
        .put("capabilities", JSONObject())
        .put(
            "clientInfo",
            JSONObject().put("name", "AeroDue").put("version", "0.1.0"),
        )

    private fun rpc(
        connector: McpConnector,
        id: Int,
        method: String,
        params: JSONObject,
        sessionId: String?,
    ): HttpResult {
        val payload = JSONObject()
            .put("jsonrpc", "2.0")
            .put("id", id)
            .put("method", method)
            .put("params", params)
        return post(
            url = connector.endpointUrl,
            token = connector.authToken,
            body = payload.toString(),
            accept = "application/json, text/event-stream",
            sessionId = sessionId,
        )
    }

    /** Extracts the JSON-RPC `result` object, tolerating SSE-framed responses. */
    private fun parseRpc(raw: String): JSONObject? {
        val json = extractJson(raw) ?: return null
        return json.optJSONObject("result")
    }

    /** MCP Streamable HTTP may return `data: {...}` SSE frames. */
    private fun extractJson(raw: String): JSONObject? {
        val trimmed = raw.trim()
        if (trimmed.startsWith("{")) return runCatching { JSONObject(trimmed) }.getOrNull()
        val dataLine = trimmed.lineSequence().firstOrNull { it.startsWith("data:") }
            ?.removePrefix("data:")?.trim()
        return dataLine?.let { runCatching { JSONObject(it) }.getOrNull() }
    }

    private fun post(
        url: String,
        token: String?,
        body: String,
        accept: String,
        sessionId: String? = null,
    ): HttpResult {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", accept)
            if (!token.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $token")
            if (!sessionId.isNullOrBlank()) setRequestProperty("Mcp-Session-Id", sessionId)
        }
        return try {
            conn.outputStream.use { it.write(body.toByteArray()) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
            HttpResult(code, text, conn.getHeaderField("Mcp-Session-Id"))
        } finally {
            conn.disconnect()
        }
    }

    companion object {
        private const val TAG = "AeroDueMcp"
        private const val TIMEOUT_MS = 15_000
    }
}
