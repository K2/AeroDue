package com.aerodue.llm

import android.content.Context
import com.aerodue.core.domain.ClaimRecommendation
import com.aerodue.core.domain.DisruptionEvent
import com.aerodue.core.domain.UserCoverageProfile
import com.aerodue.core.inference.Rationale
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.LogSeverity
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * LiteRT-LM on-device generation for claim rationale (GPU → CPU fallback on Pixel-class devices).
 */
class LiteRtLlmRationaleRunner(
    private val context: Context,
    private val modelFile: File,
) : LlmRationaleRunner {

    private val mutex = Mutex()
    private var engine: Engine? = null
    private val ruleFallback = RuleOnlyRationaleRunner()

    override val isModelLoaded: Boolean
        get() = engine != null

    override suspend fun warmUp(): Boolean = mutex.withLock {
        withContext(Dispatchers.IO) {
            if (!modelFile.isFile) return@withContext false
            if (engine != null) return@withContext true
            engine?.close()
            engine = null
            Engine.setNativeMinLogSeverity(LogSeverity.WARN)
            engine = openEngine(preferGpu = true) ?: openEngine(preferGpu = false)
            engine != null
        }
    }

    override suspend fun explain(
        event: DisruptionEvent,
        profile: UserCoverageProfile,
        claims: List<ClaimRecommendation>,
    ): String {
        if (!warmUp()) {
            return ruleFallback.explain(event, profile, claims)
        }
        val prompt = Rationale.buildPrompt(event, profile, claims)
        return mutex.withLock {
            withContext(Dispatchers.IO) {
                try {
                    val eng = engine ?: return@withContext ruleFallback.explain(event, profile, claims)
                    eng.createConversation(
                        ConversationConfig(
                            systemInstruction = Contents.of(SYSTEM_INSTRUCTION),
                            samplerConfig = SamplerConfig(
                                topK = 40,
                                topP = 0.9,
                                temperature = 0.2,
                            ),
                        ),
                    ).use { conversation ->
                        val response = conversation.sendMessage(prompt)
                        response.text?.takeIf { it.isNotBlank() }
                            ?: ruleFallback.explain(event, profile, claims)
                    }
                } catch (_: Exception) {
                    ruleFallback.explain(event, profile, claims)
                }
            }
        }
    }

    fun close() {
        engine?.close()
        engine = null
    }

    private fun openEngine(preferGpu: Boolean): Engine? {
        val backend = if (preferGpu) Backend.GPU() else Backend.CPU()
        return try {
            val config = EngineConfig(
                modelPath = modelFile.absolutePath,
                backend = backend,
                cacheDir = context.cacheDir.absolutePath,
            )
            Engine(config).also { it.initialize() }
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val SYSTEM_INSTRUCTION =
            "You are AeroDue, an offline travel compensation assistant. " +
                "Use only the facts in the user message. Never invent dollar amounts. " +
                "If uncertain, list documentation the passenger should collect. " +
                "Keep responses under 200 words with a short checklist."
    }
}
