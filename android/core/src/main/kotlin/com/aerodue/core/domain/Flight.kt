package com.aerodue.core.domain

import java.time.Instant

enum class FlightStatus {
    SCHEDULED,
    BOARDING,
    DEPARTED,
    ARRIVED,
    DELAYED,
    CANCELLED,
    DIVERTED,
}

data class FlightLeg(
    val carrierIata: String,
    val flightNumber: String,
    val departureAirport: String,
    val arrivalAirport: String,
    val scheduledDeparture: Instant,
    val scheduledArrival: Instant,
    val actualDeparture: Instant? = null,
    val actualArrival: Instant? = null,
    val status: FlightStatus = FlightStatus.SCHEDULED,
    val bookingReference: String? = null,
)

data class Trip(
    val tripId: String,
    val legs: List<FlightLeg>,
    val passengerCount: Int = 1,
)
