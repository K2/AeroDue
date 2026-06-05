package com.aerodue.core.inference

import com.aerodue.core.domain.ClaimRecommendation
import com.aerodue.core.domain.DisruptionEvent
import com.aerodue.core.domain.UserCoverageProfile

object Rationale {

    fun buildPrompt(
        event: DisruptionEvent,
        profile: UserCoverageProfile,
        claims: List<ClaimRecommendation>,
        excerpts: String = "",
    ): String = """
        Disruption: $event
        Coverage: $profile
        Rule hits: $claims
        Relevant excerpts:
        ${excerpts.ifBlank { "(bundle regulation snippets in assets/regulations/)" }}
    """.trimIndent()

    /** Rule-based summary when LiteRT-LM model is not loaded. */
    fun explainOffline(
        event: DisruptionEvent,
        profile: UserCoverageProfile,
        claims: List<ClaimRecommendation>,
    ): String {
        if (claims.isEmpty()) {
            return "No eligible compensation paths found for this disruption with your current coverage."
        }
        val lines = claims.take(5).joinToString("\n") { "• ${it.title}: ${it.summary}" }
        return "Offline rule assessment complete.\n\n$lines"
    }

    fun explainWithoutLlm(prompt: String): String =
        "Offline LLM not loaded (${ModelProfiles.defaultProfileId}). " +
            "Rule-based assessment is active.\n\nPrompt preview:\n${prompt.take(200)}…"
}
