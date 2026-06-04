package com.aerodue.core.demo

import com.aerodue.core.domain.AirlineStatus
import com.aerodue.core.domain.BusinessTravelPolicy
import com.aerodue.core.domain.CreditCardCoverage
import com.aerodue.core.domain.DisruptionEvent
import com.aerodue.core.domain.DisruptionKind
import com.aerodue.core.domain.FlightLeg
import com.aerodue.core.domain.FlightStatus
import com.aerodue.core.domain.UserCoverageProfile
import java.time.Instant

/** Mirrors backend/samples/delayed_connection.json */
object DemoFixtures {

    val delayedConnectionEvent = DisruptionEvent(
        kind = DisruptionKind.DELAY,
        leg = FlightLeg(
            carrierIata = "UA",
            flightNumber = "UA1234",
            departureAirport = "SFO",
            arrivalAirport = "JFK",
            scheduledDeparture = Instant.parse("2026-06-01T18:00:00Z"),
            scheduledArrival = Instant.parse("2026-06-02T02:30:00Z"),
            actualDeparture = Instant.parse("2026-06-02T01:30:00Z"),
            status = FlightStatus.DELAYED,
        ),
        detectedAt = Instant.parse("2026-06-02T01:35:00Z"),
        delayMinutes = 450,
        extraordinaryCircumstanceClaimed = false,
    )

    val demoProfile = UserCoverageProfile(
        userId = "user-demo",
        homeAirport = "SFO",
        dotApplicable = true,
        eu261Applicable = false,
        creditCards = listOf(
            CreditCardCoverage(
                issuer = "Chase",
                productName = "Sapphire Reserve",
                tripDelayHoursThreshold = 6,
                cancellation = true,
                maxClaimUsd = 500.0,
            ),
        ),
        businessPolicy = BusinessTravelPolicy(
            employer = "Acme Corp",
            reimbursesHotelsOnOvernightDelay = true,
            maxLodgingPerNightUsd = 250.0,
        ),
        airlineStatus = listOf(
            AirlineStatus(program = "United MileagePlus", tier = "Gold"),
        ),
    )
}
