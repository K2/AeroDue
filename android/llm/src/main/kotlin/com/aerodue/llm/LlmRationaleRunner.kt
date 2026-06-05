package com.aerodue.llm

import com.aerodue.core.domain.ClaimRecommendation
import com.aerodue.core.domain.DisruptionEvent
import com.aerodue.core.domain.UserCoverageProfile

/**
 * Generates passenger-facing disruption explanations.
 * Rule assessment stays in [com.aerodue.core.compensation.CompensationEngine].
 */
interface LlmRationaleRunner {
    val isModelLoaded: Boolean

    /** Load LiteRT-LM weights (IO). Safe to call from a background coroutine. */
    suspend fun warmUp(): Boolean

    suspend fun explain(
        event: DisruptionEvent,
        profile: UserCoverageProfile,
        claims: List<ClaimRecommendation>,
    ): String
}
