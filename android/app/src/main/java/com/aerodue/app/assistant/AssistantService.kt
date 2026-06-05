package com.aerodue.app.assistant

import com.aerodue.app.mcp.McpConnectorRegistry
import com.aerodue.llm.LlmRationaleRunner

data class TripRecommendation(
    val title: String,
    val detail: String,
    val tag: String? = null,
)

data class TripPlan(
    val narrative: String,
    val flight: TripRecommendation,
    val hotel: TripRecommendation,
    val ground: TripRecommendation,
    val airport: TripRecommendation,
)

data class RejectionAnalysis(
    val summary: String,
    val fixes: List<String>,
)

/**
 * App-side facade over the on-device model. Every method returns a useful
 * deterministic result even when no LiteRT-LM model is installed; the model,
 * when present, enriches the narrative copy.
 */
class AssistantService(
    private val llm: LlmRationaleRunner,
    private val connectors: McpConnectorRegistry? = null,
) {

    val modelLoaded: Boolean get() = llm.isModelLoaded

    /** Where assistant text is currently coming from. */
    val backendLabel: String
        get() = when {
            connectors?.activeModelConnector() != null ->
                "cloud · ${connectors.activeModelConnector()?.name}"
            llm.isModelLoaded -> "on-device LLM"
            else -> "rules"
        }

    /**
     * Generation fallback chain: enabled cloud-model connector (off-device, opt-in)
     * → on-device LiteRT-LM → "" so callers use deterministic copy.
     */
    private suspend fun generateText(prompt: String, system: String?): String {
        connectors?.generate(prompt, system)?.takeIf { it.isNotBlank() }?.let { return it }
        return llm.generate(prompt, system)
    }

    suspend fun filingNote(source: String, title: String, amountUsd: Double?): String {
        val amount = amountUsd?.let { "$${it.toInt()}" } ?: "the eligible amount"
        val prompt =
            "Write one concise sentence (max 25 words) a passenger can attach when " +
                "submitting a $source claim titled \"$title\" for $amount. Factual, polite, no invented details."
        val generated = generateText(prompt, FILING_SYSTEM)
        return generated.ifBlank {
            "Requesting $amount under $title per the applicable policy; supporting itinerary and delay record attached."
        }
    }

    suspend fun analyzeRejection(
        title: String,
        source: String,
        reason: String,
    ): RejectionAnalysis {
        val prompt =
            "A $source claim \"$title\" was rejected for: \"$reason\". In one short sentence, " +
                "explain the likely gap and how an appeal should address it. No invented facts."
        val summary = generateText(prompt, REJECTION_SYSTEM).ifBlank {
            "Rejection cited \"$reason\". An appeal should supply the missing evidence and restate eligibility under the cited rule."
        }
        val fixes = buildList {
            add("Attach itinerary, boarding pass, and the carrier's delay/cancellation notice")
            when {
                reason.contains("document", true) ->
                    add("Add receipts and a timestamped screenshot of the disruption")
                reason.contains("eligib", true) ->
                    add("Cite the specific regulation/benefit clause and the qualifying delay threshold")
                else ->
                    add("Reference the exact policy clause and the recorded delay duration")
            }
            add("Resubmit within the appeal window with a one-line cover note")
        }
        return RejectionAnalysis(summary = summary, fixes = fixes)
    }

    suspend fun optimalTripPlan(routeLabel: String, arrivalLocal: String): TripPlan {
        val flight = TripRecommendation(
            title = "Rebook the morning nonstop",
            detail = "Swap the delayed red-eye for the 7:10a nonstop; protects your status and avoids the connection at risk.",
            tag = "Saves ~3h",
        )
        val hotel = TripRecommendation(
            title = "Refundable airport hotel hold",
            detail = "Hold a free-cancel room near the airport; release it automatically if the rebooking sticks.",
            tag = "$0 risk",
        )
        val ground = TripRecommendation(
            title = "Train over rideshare at this hour",
            detail = "At $arrivalLocal arrival, rail beats surge pricing and traffic to downtown by ~20 min and ~$45.",
            tag = "Saves ~$45",
        )
        val airport = TripRecommendation(
            title = "Carry-on only + lounge reroute",
            detail = "Skip checked bags to keep the tight connection; your card's lounge access covers the wait.",
            tag = "Lower risk",
        )

        val prompt =
            "Summarize an optimal recovery plan in 2 sentences for a traveler on $routeLabel " +
                "arriving about $arrivalLocal: rebook a morning nonstop, hold a refundable airport hotel, " +
                "take the train downtown, and go carry-on only. Encouraging, concrete, no invented prices."
        val narrative = generateText(prompt, TRIP_SYSTEM).ifBlank {
            "Here's the lowest-stress recovery: take the early nonstop, hold a refundable airport room as backup, " +
                "and ride the train downtown. Going carry-on only keeps your connection safe."
        }

        return TripPlan(
            narrative = narrative,
            flight = flight,
            hotel = hotel,
            ground = ground,
            airport = airport,
        )
    }

    companion object {
        private const val FILING_SYSTEM =
            "You draft short, factual claim cover notes. Never invent amounts, dates, or policy text."
        private const val REJECTION_SYSTEM =
            "You analyze claim rejections and propose appeal fixes. Be specific and factual; never fabricate."
        private const val TRIP_SYSTEM =
            "You are a calm travel concierge. Give concrete, encouraging guidance. Never invent prices or times."
    }
}
