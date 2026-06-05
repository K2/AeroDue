package com.aerodue.app.filing

/** Android-free seam so [FilingCoordinator] can post progress without a Context. */
interface FilingNotifier {
    fun progress(claimKey: String, claimTitle: String, phase: String)
    fun done(claimKey: String, claimTitle: String, phase: String, reference: String?)
}

/** No-op used when notifications are unavailable or permission is denied. */
object NoopFilingNotifier : FilingNotifier {
    override fun progress(claimKey: String, claimTitle: String, phase: String) {}
    override fun done(claimKey: String, claimTitle: String, phase: String, reference: String?) {}
}
