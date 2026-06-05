package com.aerodue.app.filing

import com.aerodue.app.assistant.AssistantService
import kotlinx.coroutines.delay

/**
 * Orchestrates multi-phase, LLM-assisted filing flows and surfaces progress
 * to the UI (via [ClaimFilingService] state) and to system notifications
 * (via [FilingNotifier]).
 */
class FilingCoordinator(
    private val service: ClaimFilingService,
    private val assistant: AssistantService,
    private val notifier: FilingNotifier,
) {

    /** NOT_FILED -> "Filing paperwork…" -> FILED, with an LLM cover note. */
    suspend fun file(key: String, title: String, source: String, amountUsd: Double?) {
        service.setWorking(key, PHASE_FILING)
        notifier.progress(key, title, PHASE_FILING)

        val note = assistant.filingNote(source, title, amountUsd)
        service.setCoverNote(key, note)
        delay(PHASE_DELAY_MS)

        service.file(key)
        notifier.done(key, title, "Filed", service.record(key).reference)
    }

    /** REJECTED -> "Analyzing rejection…" -> "Refiling…" -> APPEALED. */
    suspend fun analyzeAndRefile(
        key: String,
        title: String,
        source: String,
        reason: String,
    ) {
        service.setWorking(key, PHASE_ANALYZING)
        notifier.progress(key, title, PHASE_ANALYZING)

        val analysis = assistant.analyzeRejection(title, source, reason)
        service.setRejectionAnalysis(key, analysis.summary, analysis.fixes)
        delay(PHASE_DELAY_MS)

        service.setWorking(key, PHASE_REFILING)
        notifier.progress(key, title, PHASE_REFILING)
        delay(PHASE_DELAY_MS)

        service.followUpOnRejection(key)
        notifier.done(key, title, "Appeal filed", service.record(key).reference)
    }

    companion object {
        const val PHASE_FILING = "Filing paperwork…"
        const val PHASE_ANALYZING = "Analyzing rejection…"
        const val PHASE_REFILING = "Refiling appeal…"
        private const val PHASE_DELAY_MS = 1100L
    }
}
