package com.aerodue.app.mcp

/**
 * MCP connector framework.
 *
 * AeroDue runs on-device by default. These connectors let a user plug their own
 * cloud model or MCP service into the assistant so AeroDue can participate in a
 * larger AI assistant framework. Connectors are OFF by default and run under the
 * provider's own terms — enabling one sends data off-device.
 */
enum class ConnectorKind {
    /** A Model Context Protocol server exposing tools/resources (JSON-RPC over HTTP). */
    MCP_TOOLS,

    /** An OpenAI-compatible chat-completions endpoint (bring-your-own cloud model). */
    CLOUD_MODEL,
}

data class McpConnector(
    val id: String,
    val name: String,
    val kind: ConnectorKind,
    val endpointUrl: String,
    val authToken: String? = null,
    /** Model id for [ConnectorKind.CLOUD_MODEL], e.g. "gpt-4o-mini". */
    val modelId: String? = null,
    val enabled: Boolean = false,
    /** The provider's external terms must be accepted before enabling. */
    val termsAccepted: Boolean = false,
)

data class RemoteTool(
    val name: String,
    val description: String,
)

/** Result of a connection test / capability probe. */
data class ConnectorProbe(
    val ok: Boolean,
    val message: String,
    val tools: List<RemoteTool> = emptyList(),
)
