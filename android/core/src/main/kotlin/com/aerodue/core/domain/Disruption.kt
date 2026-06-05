package com.aerodue.core.domain

import java.time.Instant

enum class DisruptionKind {
    DELAY,
    CANCELLATION,
    MISSED_CONNECTION,
    DENIED_BOARDING,
    BAGGAGE_DELAY,
    SCHEDULE_CHANGE,
}

data class DisruptionEvent(
    val kind: DisruptionKind,
    val leg: FlightLeg,
    val detectedAt: Instant,
    val delayMinutes: Int? = null,
    val extraordinaryCircumstanceClaimed: Boolean = false,
    val notes: String? = null,
)
