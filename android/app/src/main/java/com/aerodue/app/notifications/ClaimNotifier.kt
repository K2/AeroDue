package com.aerodue.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aerodue.app.R
import com.aerodue.app.filing.FilingNotifier
import kotlin.math.absoluteValue

/** Posts claim filing progress as system notifications. */
class ClaimNotifier(private val context: Context) : FilingNotifier {

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Claim filing",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Updates while AeroDue files and follows up on claims" }
            val mgr = context.getSystemService(NotificationManager::class.java)
            mgr?.createNotificationChannel(channel)
        }
    }

    private fun canPost(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun idFor(key: String): Int = key.hashCode().absoluteValue

    private fun post(key: String, title: String, text: String, ongoing: Boolean) {
        if (!canPost()) return
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_claim)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(ongoing)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(idFor(key), n)
    }

    override fun progress(claimKey: String, claimTitle: String, phase: String) {
        post(claimKey, claimTitle, phase, ongoing = true)
    }

    override fun done(claimKey: String, claimTitle: String, phase: String, reference: String?) {
        val text = if (reference != null) "$phase · $reference" else phase
        post(claimKey, claimTitle, text, ongoing = false)
    }

    companion object {
        private const val CHANNEL_ID = "claim_filing"
    }
}
