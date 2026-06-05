package com.aerodue.app.consent

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.consentDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "aerodue_consent")

/** Explicit, granular consent state. Defaults are opt-OUT. */
data class ConsentState(
    val onboardingComplete: Boolean = false,
    /** Foreground/background GPS to auto-detect disruptions for free claims. */
    val gpsTracking: Boolean = false,
    /** Share anonymized door-to-door telemetry that powers premium routing. */
    val telemetrySharing: Boolean = false,
)

class ConsentStore(private val context: Context) {

    private object Keys {
        val onboarding = booleanPreferencesKey("onboarding_complete")
        val gps = booleanPreferencesKey("gps_tracking")
        val telemetry = booleanPreferencesKey("telemetry_sharing")
    }

    val state: Flow<ConsentState> = context.consentDataStore.data.map { p ->
        ConsentState(
            onboardingComplete = p[Keys.onboarding] ?: false,
            gpsTracking = p[Keys.gps] ?: false,
            telemetrySharing = p[Keys.telemetry] ?: false,
        )
    }

    suspend fun completeOnboarding(gpsTracking: Boolean, telemetrySharing: Boolean) {
        context.consentDataStore.edit { p ->
            p[Keys.gps] = gpsTracking
            p[Keys.telemetry] = telemetrySharing
            p[Keys.onboarding] = true
        }
    }

    suspend fun setGpsTracking(enabled: Boolean) {
        context.consentDataStore.edit { it[Keys.gps] = enabled }
    }

    suspend fun setTelemetrySharing(enabled: Boolean) {
        context.consentDataStore.edit { it[Keys.telemetry] = enabled }
    }
}
