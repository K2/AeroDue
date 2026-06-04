package com.aerodue.app

import android.app.Application
import com.aerodue.app.service.CompensationService
import com.aerodue.llm.LlmRationaleRunner
import com.aerodue.llm.LlmRationaleRunnerFactory

class AeroDueApplication : Application() {

    lateinit var llmRunner: LlmRationaleRunner
        private set

    lateinit var compensationService: CompensationService
        private set

    override fun onCreate() {
        super.onCreate()
        llmRunner = LlmRationaleRunnerFactory.create(this)
        compensationService = CompensationService(llmRunner)
    }
}
