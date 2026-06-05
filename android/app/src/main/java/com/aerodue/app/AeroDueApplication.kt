package com.aerodue.app

import android.app.Application
import com.aerodue.app.assistant.AssistantService
import com.aerodue.app.consent.ConsentStore
import com.aerodue.app.filing.ClaimFilingService
import com.aerodue.app.filing.FilingCoordinator
import com.aerodue.app.mcp.McpConnectorRegistry
import com.aerodue.app.mcp.McpConnectorStore
import com.aerodue.app.mcp.RemoteConnectorClient
import com.aerodue.app.notifications.ClaimNotifier
import com.aerodue.app.plugins.PluginRegistry
import com.aerodue.app.service.CompensationService
import com.aerodue.app.webview.WebViewDevConfig
import com.aerodue.llm.LlmRationaleRunner
import com.aerodue.llm.LlmRationaleRunnerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AeroDueApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var llmRunner: LlmRationaleRunner
        private set

    lateinit var compensationService: CompensationService
        private set

    lateinit var consentStore: ConsentStore
        private set

    lateinit var filingService: ClaimFilingService
        private set

    lateinit var pluginRegistry: PluginRegistry
        private set

    lateinit var assistantService: AssistantService
        private set

    lateinit var filingCoordinator: FilingCoordinator
        private set

    lateinit var connectorRegistry: McpConnectorRegistry
        private set

    override fun onCreate() {
        super.onCreate()
        WebViewDevConfig.initialize(this)
        llmRunner = LlmRationaleRunnerFactory.create(this)
        compensationService = CompensationService(llmRunner)
        consentStore = ConsentStore(this)
        filingService = ClaimFilingService()
        pluginRegistry = PluginRegistry()
        connectorRegistry = McpConnectorRegistry(
            store = McpConnectorStore(this),
            client = RemoteConnectorClient(),
            scope = appScope,
        )
        assistantService = AssistantService(llmRunner, connectorRegistry)
        filingCoordinator = FilingCoordinator(
            service = filingService,
            assistant = assistantService,
            notifier = ClaimNotifier(this),
        )
        appScope.launch(Dispatchers.IO) {
            llmRunner.warmUp()
        }
    }
}
