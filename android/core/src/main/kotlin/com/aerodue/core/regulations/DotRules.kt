package com.aerodue.core.regulations

import com.aerodue.core.domain.ClaimRecommendation
import com.aerodue.core.domain.CompensationSource
import com.aerodue.core.domain.DisruptionEvent
import com.aerodue.core.domain.DisruptionKind
import com.aerodue.core.domain.UserCoverageProfile

fun evaluateDotDelay(
    event: DisruptionEvent,
    profile: UserCoverageProfile,
): List<ClaimRecommendation> {
    if (!profile.dotApplicable) return emptyList()
    if (event.kind !in setOf(DisruptionKind.DELAY, DisruptionKind.CANCELLATION)) return emptyList()

    val delay = event.delayMinutes ?: 0
    val claims = mutableListOf<ClaimRecommendation>()

    if (event.kind == DisruptionKind.CANCELLATION) {
        claims += ClaimRecommendation(
            source = CompensationSource.REGULATION_DOT,
            title = "DOT refund for cancelled flight",
            summary =
                "For cancellations within carrier control, you may be entitled to a " +
                    "refund to original form of payment (DOT enforcement guidance).",
            confidence = 0.85,
            citationIds = listOf("dot-refund-cancel-2024"),
            actionSteps = listOf(
                "Request full refund via airline app or agent",
                "If denied, file DOT aviation consumer complaint",
            ),
        )
    }

    if (delay >= 180) {
        claims += ClaimRecommendation(
            source = CompensationSource.REGULATION_DOT,
            title = "Significant tarmac / delay accommodations",
            summary =
                "Extended delays may trigger meal vouchers, rebooking, or hotel per " +
                    "carrier customer service plan — check operating carrier's CS plan.",
            confidence = 0.7,
            citationIds = listOf("dot-cs-plan-delay"),
            actionSteps = listOf(
                "Ask gate agent for meal/hotel voucher",
                "Document expenses",
            ),
        )
    }

    return claims
}
