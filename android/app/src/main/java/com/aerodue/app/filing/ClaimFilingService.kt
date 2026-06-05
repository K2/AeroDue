package com.aerodue.app.filing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

enum class FilingStatus { NOT_FILED, FILED, IN_REVIEW, PAID, REJECTED, APPEALED }

data class FilingRecord(
    val claimKey: String,
    val status: FilingStatus = FilingStatus.NOT_FILED,
    val reference: String? = null,
    val note: String? = null,
    /** True while an async phase (filing / analyzing / refiling) is in flight. */
    val working: Boolean = false,
    val workingMessage: String? = null,
    /** LLM-drafted cover note attached at filing. */
    val coverNote: String? = null,
    /** LLM rejection analysis. */
    val rejectionSummary: String? = null,
    val rejectionFixes: List<String> = emptyList(),
    val history: List<String> = emptyList(),
)

/**
 * Tracks claim filing lifecycle, including rejection follow-up (appeal).
 *
 * AeroDue is NOT a legal service: this automates document preparation and
 * status tracking, and helps the user follow up on rejections. The user
 * submits and confirms each step.
 */
class ClaimFilingService {

    private val records = MutableStateFlow<Map<String, FilingRecord>>(emptyMap())
    val state: StateFlow<Map<String, FilingRecord>> = records.asStateFlow()

    fun record(key: String): FilingRecord =
        records.value[key] ?: FilingRecord(claimKey = key)

    private fun mutate(key: String, transform: (FilingRecord) -> FilingRecord) {
        val current = record(key)
        records.value = records.value + (key to transform(current))
    }

    private fun reference(prefix: String): String =
        "$prefix-${Random.nextInt(100000, 999999)}"

    fun setWorking(key: String, message: String) = mutate(key) {
        it.copy(working = true, workingMessage = message)
    }

    fun setCoverNote(key: String, note: String) = mutate(key) {
        it.copy(coverNote = note)
    }

    fun setRejectionAnalysis(key: String, summary: String, fixes: List<String>) = mutate(key) {
        it.copy(rejectionSummary = summary, rejectionFixes = fixes)
    }

    /** Prepare paperwork and submit the claim. */
    fun file(key: String) = mutate(key) { r ->
        val ref = reference("ADU")
        r.copy(
            status = FilingStatus.FILED,
            reference = ref,
            note = "Paperwork prepared and submitted by you.",
            working = false,
            workingMessage = null,
            history = r.history + "Filed as $ref",
        )
    }

    /** Demo progression: FILED -> IN_REVIEW -> PAID. */
    fun advance(key: String) = mutate(key) { r ->
        when (r.status) {
            FilingStatus.FILED -> r.copy(
                status = FilingStatus.IN_REVIEW,
                history = r.history + "Moved to in review",
            )
            FilingStatus.IN_REVIEW -> r.copy(
                status = FilingStatus.PAID,
                note = "Funds issued to your original form of payment.",
                history = r.history + "Marked paid",
            )
            FilingStatus.APPEALED -> r.copy(
                status = FilingStatus.PAID,
                note = "Appeal accepted; funds issued.",
                history = r.history + "Appeal accepted, marked paid",
            )
            else -> r
        }
    }

    /** Simulate an airline/insurer/regulator rejection. */
    fun reject(key: String, reason: String = "Insufficient documentation") = mutate(key) { r ->
        r.copy(
            status = FilingStatus.REJECTED,
            note = reason,
            working = false,
            workingMessage = null,
            history = r.history + "Rejected: $reason",
        )
    }

    /** Follow up on a rejection by preparing and resubmitting an appeal. */
    fun followUpOnRejection(key: String) = mutate(key) { r ->
        if (r.status != FilingStatus.REJECTED) return@mutate r
        val ref = reference("APL")
        r.copy(
            status = FilingStatus.APPEALED,
            reference = ref,
            note = "Appeal prepared with added evidence and resubmitted.",
            working = false,
            workingMessage = null,
            history = r.history + "Appeal filed as $ref",
        )
    }
}
