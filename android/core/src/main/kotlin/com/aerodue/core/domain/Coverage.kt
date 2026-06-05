package com.aerodue.core.domain

data class CreditCardCoverage(
    val issuer: String,
    val productName: String,
    val tripDelayHoursThreshold: Int? = 6,
    val cancellation: Boolean = true,
    val baggageDelay: Boolean = false,
    val maxClaimUsd: Double? = null,
)

data class BusinessTravelPolicy(
    val employer: String,
    val policyId: String? = null,
    val requiresManagerApproval: Boolean = false,
    val reimbursesHotelsOnOvernightDelay: Boolean = true,
    val maxLodgingPerNightUsd: Double? = null,
)

data class AirlineStatus(
    val program: String,
    val tier: String? = null,
    val alliance: String? = null,
)

data class UserCoverageProfile(
    val userId: String,
    val homeAirport: String? = null,
    val creditCards: List<CreditCardCoverage> = emptyList(),
    val businessPolicy: BusinessTravelPolicy? = null,
    val airlineStatus: List<AirlineStatus> = emptyList(),
    val eu261Applicable: Boolean = false,
    val dotApplicable: Boolean = true,
)
