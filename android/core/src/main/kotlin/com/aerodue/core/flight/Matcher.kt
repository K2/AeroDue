package com.aerodue.core.flight

import com.aerodue.core.domain.FlightLeg
import com.aerodue.core.domain.FlightStatus
import com.aerodue.core.domain.Trip
import com.aerodue.core.location.GeoFix
import com.aerodue.core.location.nearestAirportIcao
import java.time.Duration

fun matchTripFromLocation(
    fix: GeoFix,
    candidateLegs: List<FlightLeg>,
    windowHours: Long = 18,
): Trip? {
    val airport = nearestAirportIcao(fix) ?: return null
    val now = fix.recordedAt
    val window = Duration.ofHours(windowHours)

    val matches = candidateLegs.filter { leg ->
        val depNear = leg.departureAirport == airport
        val arrNear = leg.arrivalAirport == airport
        val inWindow =
            !now.isBefore(leg.scheduledDeparture.minus(window)) &&
                !now.isAfter(leg.scheduledArrival.plus(window))
        inWindow && (depNear || arrNear)
    }
    if (matches.isEmpty()) return null

    val sorted = matches.sortedBy { leg ->
        kotlin.math.abs(Duration.between(leg.scheduledDeparture, now).seconds)
    }
    val first = sorted.first()
    return Trip(tripId = "auto-${first.flightNumber}", legs = sorted)
}

fun classifyDelayMinutes(leg: FlightLeg): Int? {
    val actual = leg.actualDeparture ?: return null
    if (leg.status !in setOf(
            FlightStatus.DELAYED,
            FlightStatus.DEPARTED,
            FlightStatus.ARRIVED,
        )
    ) {
        return null
    }
    val delta = Duration.between(leg.scheduledDeparture, actual)
    return maxOf(0, delta.toMinutes().toInt())
}
