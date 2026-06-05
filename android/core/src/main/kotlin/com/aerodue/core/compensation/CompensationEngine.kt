package com.aerodue.core.compensation

import com.aerodue.core.domain.ClaimRecommendation
import com.aerodue.core.domain.CompensationSource
import com.aerodue.core.domain.DisruptionEvent
import com.aerodue.core.domain.UserCoverageProfile
import com.aerodue.core.regulations.evaluateDotDelay
import com.aerodue.core.regulations.evaluateEu261Delay

object CompensationEngine {

    fun assess(
        event: DisruptionEvent,
        profile: UserCoverageProfile,
        legDistanceKm: Double = 2000.0,
    ): List<ClaimRecommendation> {
        val results = mutableListOf<ClaimRecommendation>()
        results += evaluateDotDelay(event, profile)
        results += evaluateEu261Delay(event, profile, legDistanceKm)
        results += creditCardClaims(event, profile)
        results += businessPolicyClaims(event, profile)
        results += airlineGoodwillClaims(event, profile)
        return results.sortedByDescending { it.confidence }
    }

    private fun creditCardClaims(
        event: DisruptionEvent,
        profile: UserCoverageProfile,
    ): List<ClaimRecommendation> {
        val delayHours = (event.delayMinutes ?: 0) / 60.0
        return profile.creditCards.mapNotNull { card ->
            val threshold = card.tripDelayHoursThreshold ?: return@mapNotNull null
            if (delayHours < threshold) return@mapNotNull null
            ClaimRecommendation(
                source = CompensationSource.CREDIT_CARD,
                title = "${card.issuer} trip delay benefit",
                summary =
                    "${card.productName} may cover expenses after ${threshold}h delay " +
                        "(cancellation=${card.cancellation}).",
                estimatedAmountUsd = card.maxClaimUsd,
                confidence = 0.65,
                citationIds = listOf("cc-${card.issuer.lowercase()}-guide"),
                actionSteps = listOf(
                    "File claim on issuer benefits portal",
                    "Attach delay certificate and receipts",
                ),
            )
        }
    }

    private fun businessPolicyClaims(
        event: DisruptionEvent,
        profile: UserCoverageProfile,
    ): List<ClaimRecommendation> {
        val policy = profile.businessPolicy ?: return emptyList()
        if (!policy.reimbursesHotelsOnOvernightDelay) return emptyList()
        if ((event.delayMinutes ?: 0) < 360) return emptyList()

        return listOf(
            ClaimRecommendation(
                source = CompensationSource.BUSINESS_POLICY,
                title = "${policy.employer} overnight delay lodging",
                summary = "Corporate policy may reimburse hotel when delay exceeds 6 hours.",
                estimatedAmountUsd = policy.maxLodgingPerNightUsd,
                confidence = 0.6,
                citationIds = listOf("biz-${policy.employer.lowercase()}-travel"),
                actionSteps = listOf(
                    "Book approved hotel via corporate travel tool",
                    "Submit expense report with delay proof",
                ),
            ),
        )
    }

    private fun airlineGoodwillClaims(
        event: DisruptionEvent,
        profile: UserCoverageProfile,
    ): List<ClaimRecommendation> {
        val match = profile.airlineStatus.firstOrNull { status ->
            event.leg.carrierIata.lowercase() in status.program.lowercase()
        } ?: return emptyList()

        return listOf(
            ClaimRecommendation(
                source = CompensationSource.AIRLINE_GOODWILL,
                title = "${match.program} status goodwill",
                summary =
                    "Tier ${match.tier ?: "member"} may qualify for miles, " +
                        "upgrades, or lounge passes after IRROPS.",
                confidence = 0.5,
                citationIds = listOf("airline-status-policy"),
                actionSteps = listOf(
                    "Contact elite line or use app IRROPS chat",
                ),
            ),
        )
    }
}
