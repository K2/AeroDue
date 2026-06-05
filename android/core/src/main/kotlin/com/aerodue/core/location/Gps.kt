package com.aerodue.core.location

import java.time.Instant

data class GeoFix(
    val latitude: Double,
    val longitude: Double,
    val accuracyM: Float,
    val recordedAt: Instant,
)

fun nearestAirportIcao(fix: GeoFix): String? {
    if (fix.latitude in 40.6..40.7 && fix.longitude in -73.9..-73.7) return "JFK"
    if (fix.latitude in 37.6..37.7 && fix.longitude in -122.4..-122.3) return "SFO"
    return null
}

fun inferOnGroundAtAirport(fix: GeoFix, airportIcao: String): Boolean =
    nearestAirportIcao(fix) == airportIcao
