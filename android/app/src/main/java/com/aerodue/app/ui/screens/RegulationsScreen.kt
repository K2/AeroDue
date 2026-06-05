package com.aerodue.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aerodue.app.webview.AeroDueWebView
import com.aerodue.app.webview.WebViewDevConfig

private const val DOT_CONSUMER_URL = "https://www.transportation.gov/airconsumer"

@Composable
fun RegulationsScreen() {
    val context = LocalContext.current
    var title by remember { mutableStateOf<String?>(null) }
    val provider = remember { WebViewDevConfig.providerLabel(context) }
    val hint = remember { WebViewDevConfig.devProviderHint(context) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Regulations", style = MaterialTheme.typography.titleMedium)
            Text(
                "WebView: $provider",
                style = MaterialTheme.typography.labelSmall,
            )
            title?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
            hint?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        AeroDueWebView(
            url = DOT_CONSUMER_URL,
            modifier = Modifier.weight(1f),
            onPageTitle = { title = it },
        )
    }
}
