package com.aerodue.llm

import android.content.Context

object LlmRationaleRunnerFactory {

    fun create(context: Context): LlmRationaleRunner {
        ModelInstaller.ensureDefaultModelFromAssets(context)
        val model = LlmModelPaths.defaultModelFile(context)
        return if (model.isFile && model.length() > 0L) {
            LiteRtLlmRationaleRunner(context.applicationContext, model)
        } else {
            RuleOnlyRationaleRunner()
        }
    }
}
