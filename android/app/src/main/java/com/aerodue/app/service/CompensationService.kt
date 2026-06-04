package com.aerodue.app.service

import com.aerodue.core.compensation.CompensationEngine
import com.aerodue.core.demo.DemoFixtures
import com.aerodue.core.domain.ClaimRecommendation
import com.aerodue.core.domain.DisruptionEvent
import com.aerodue.core.domain.UserCoverageProfile
import com.aerodue.llm.LlmRationaleRunner

class CompensationService(
    private val llm: LlmRationaleRunner,
) {

    fun assess(
        event: DisruptionEvent,
        profile: UserCoverageProfile,
    ): List<ClaimRecommendation> = CompensationEngine.assess(event, profile)

    fun assessDemo(): List<ClaimRecommendation> = assess(
        DemoFixtures.delayedConnectionEvent,
        DemoFixtures.demoProfile,
    )

    suspend fun passengerSummary(
        event: DisruptionEvent,
        profile: UserCoverageProfile,
        claims: List<ClaimRecommendation>,
    ): String = llm.explain(event, profile, claims)

    suspend fun demoPassengerSummary(): String = passengerSummary(
        DemoFixtures.delayedConnectionEvent,
        DemoFixtures.demoProfile,
        assessDemo(),
    )
}
