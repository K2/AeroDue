package com.aerodue.app.service

/**
 * Foreground/background GPS listener that forwards fixes to the backend bridge.
 * Implementation: FusedLocationProvider + WorkManager geofences (next step).
 */
object TripMonitorService {

    data class TripStatus(
        val headline: String,
        val detail: String,
        val airportGuess: String?,
    )

    fun sampleStatus(): TripStatus = TripStatus(
        headline = "UA 1234 · SFO → JFK",
        detail = "Delayed 7h 30m — assessing compensation paths offline…",
        airportGuess = "SFO",
    )
}
