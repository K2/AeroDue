package com.aerodue.llm

import com.aerodue.core.domain.ClaimRecommendation
import com.aerodue.core.domain.DisruptionEvent
import com.aerodue.core.domain.UserCoverageProfile
import com.aerodue.core.inference.Rationale

/** Deterministic copy when no LiteRT-LM bundle is installed. */
class RuleOnlyRationaleRunner : LlmRationaleRunner {
    override val isModelLoaded: Boolean = false

    override suspend fun warmUp(): Boolean = true

    override suspend fun explain(
        event: DisruptionEvent,
        profile: UserCoverageProfile,
        claims: List<ClaimRecommendation>,
    ): String = Rationale.explainOffline(event, profile, claims)

    /** No model installed — let the caller use deterministic copy. */
    override suspend fun generate(prompt: String, system: String?): String = ""
}
