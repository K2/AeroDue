package com.aerodue.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aerodue.app.AeroDueApplication
import com.aerodue.app.service.TripMonitorService
import com.aerodue.app.ui.components.HeroHeader
import com.aerodue.app.ui.components.SectionCard
import com.aerodue.app.ui.components.TintChip
import com.aerodue.app.ui.theme.Amber500
import com.aerodue.app.ui.theme.Money500
import com.aerodue.core.domain.ClaimRecommendation
import com.aerodue.core.inference.ModelProfiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
fun HomeScreen(onOpenPremium: () -> Unit = {}) {
    val app = LocalContext.current.applicationContext as AeroDueApplication
    val status = TripMonitorService.sampleStatus()
    var summary by remember { mutableStateOf<String?>(null) }
    var claims by remember { mutableStateOf<List<ClaimRecommendation>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(app.compensationService) {
        loading = true
        claims = app.compensationService.assessDemo()
        withContext(Dispatchers.IO) {
            app.llmRunner.warmUp()
            summary = app.compensationService.demoPassengerSummary()
        }
        loading = false
    }

    val totalRecovery = claims.sumOf { it.estimatedAmountUsd ?: 0.0 }
    val llmLoaded = app.llmRunner.isModelLoaded

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        HeroHeader(
            eyebrow = "Active trip",
            title = status.headline,
        ) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "  ${status.detail}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.92f),
                )
            }
            Spacer(Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Estimated recovery",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.75f),
                        )
                        Text(
                            text = if (loading) "Calculating…" else "$${totalRecovery.roundToInt()}",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.16f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlightTakeoff,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "  ${claims.size} paths",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionCard(
                title = "What happened",
                icon = Icons.Outlined.LocationOn,
                accent = Amber500,
            ) {
                Text(
                    text = status.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(10.dp))
                Row {
                    TintChip(
                        label = "GPS: ${status.airportGuess ?: "acquiring…"}",
                        tint = MaterialTheme.colorScheme.primary,
                        leadingIcon = Icons.Outlined.LocationOn,
                    )
                    Spacer(Modifier.width(8.dp))
                    TintChip(
                        label = "${claims.size} compensation paths",
                        tint = Money500,
                    )
                }
            }

            SectionCard(
                title = "Your assistant",
                icon = Icons.Default.AutoAwesome,
                accent = MaterialTheme.colorScheme.secondary,
            ) {
                if (loading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                        Text(
                            text = "  Warming up on-device model…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Text(
                        text = summary.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            PremiumCta(onOpenPremium)

            EngineStatusRow(
                modelId = ModelProfiles.defaultProfileId,
                llmLoaded = llmLoaded,
            )
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun PremiumCta(onOpenPremium: () -> Unit) {
    Surface(
        onClick = onOpenPremium,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Optimize my trip",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Premium · agent plans flight, hotel, ground & airport",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                )
            }
            Text(
                text = "→",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun EngineStatusRow(modelId: String, llmLoaded: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TintChip(label = "Engine: Kotlin", tint = MaterialTheme.colorScheme.primary)
        TintChip(
            label = if (llmLoaded) "LiteRT-LM loaded" else "rules only",
            tint = if (llmLoaded) Money500 else Amber500,
        )
        TintChip(label = modelId, tint = MaterialTheme.colorScheme.secondary)
    }
}
