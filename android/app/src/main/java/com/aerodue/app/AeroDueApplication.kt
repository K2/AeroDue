package com.aerodue.app

import android.app.Application
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

    override fun onCreate() {
        super.onCreate()
        WebViewDevConfig.initialize(this)
        llmRunner = LlmRationaleRunnerFactory.create(this)
        compensationService = CompensationService(llmRunner)
        appScope.launch(Dispatchers.IO) {
            llmRunner.warmUp()
        }
    }
}
