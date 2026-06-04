package com.aerodue.core.domain

enum class CompensationSource {
    REGULATION_DOT,
    REGULATION_EU261,
    CREDIT_CARD,
    BUSINESS_POLICY,
    AIRLINE_GOODWILL,
    CARRIER_CONTRACT,
}

data class ClaimRecommendation(
    val source: CompensationSource,
    val title: String,
    val summary: String,
    val estimatedAmountUsd: Double? = null,
    val currency: String = "USD",
    val confidence: Double,
    val citationIds: List<String> = emptyList(),
    val actionSteps: List<String> = emptyList(),
)
