package com.aerodue.core.regulations

import com.aerodue.core.domain.ClaimRecommendation
import com.aerodue.core.domain.CompensationSource
import com.aerodue.core.domain.DisruptionEvent
import com.aerodue.core.domain.DisruptionKind
import com.aerodue.core.domain.UserCoverageProfile

private val EU261_EUR = mapOf(
    "short" to 250,
    "medium" to 400,
    "long" to 600,
)

private fun distanceBandKm(legDistanceKm: Double): String = when {
    legDistanceKm < 1500 -> "short"
    legDistanceKm < 3500 -> "medium"
    else -> "long"
}

fun evaluateEu261Delay(
    event: DisruptionEvent,
    profile: UserCoverageProfile,
    legDistanceKm: Double = 2000.0,
): List<ClaimRecommendation> {
    if (!profile.eu261Applicable) return emptyList()
    if (event.extraordinaryCircumstanceClaimed) return emptyList()
    if (event.kind !in setOf(DisruptionKind.DELAY, DisruptionKind.CANCELLATION)) return emptyList()

    val delay = event.delayMinutes ?: 0
    if (delay < 180 && event.kind != DisruptionKind.CANCELLATION) return emptyList()

    val band = distanceBandKm(legDistanceKm)
    val eur = EU261_EUR.getValue(band)

    return listOf(
        ClaimRecommendation(
            source = CompensationSource.REGULATION_EU261,
            title = "EU261 fixed compensation",
            summary =
                "Arrival delay 3+ hours may entitle €$eur ($band haul) unless " +
                    "extraordinary circumstances apply.",
            estimatedAmountUsd = eur * 1.08,
            currency = "EUR",
            confidence = 0.75,
            citationIds = listOf("eu261-art7"),
            actionSteps = listOf(
                "Submit EU261 claim form to operating carrier",
                "Keep boarding pass and delay confirmation",
            ),
        ),
    )
}
