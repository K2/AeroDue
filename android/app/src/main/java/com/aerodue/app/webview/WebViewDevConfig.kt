package com.aerodue.app.webview

import android.content.Context
import android.webkit.WebView
import androidx.webkit.WebViewCompat
import com.aerodue.app.BuildConfig

object WebViewDevConfig {

    const val GOOGLE_WEBVIEW_DEV = "com.google.android.webview.dev"
    const val GOOGLE_WEBVIEW_STABLE = "com.google.android.webview"
    const val VANADIUM_WEBVIEW = "app.vanadium.webview"

    fun initialize(context: Context) {
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }

    fun currentProviderPackage(context: Context): String? =
        WebViewCompat.getCurrentWebViewPackage(context)?.packageName

    fun isGoogleDevProvider(context: Context): Boolean =
        currentProviderPackage(context) == GOOGLE_WEBVIEW_DEV

    fun providerLabel(context: Context): String {
        val pkg = currentProviderPackage(context) ?: return "unknown"
        return when (pkg) {
            GOOGLE_WEBVIEW_DEV -> "Google WebView Dev"
            GOOGLE_WEBVIEW_STABLE -> "Google WebView (stable)"
            VANADIUM_WEBVIEW -> "Vanadium (GrapheneOS)"
            else -> pkg
        }
    }

    /** Warn when system provider is not Dev; app cannot override OS WebView on API 29+. */
    fun devProviderHint(context: Context): String? {
        if (BuildConfig.DEBUG && !isGoogleDevProvider(context)) {
            return "System WebView is ${providerLabel(context)}. " +
                "Run scripts/configure-pixel-dev.sh or set Developer options → " +
                "WebView implementation → Android System WebView Dev."
        }
        return null
    }
}
