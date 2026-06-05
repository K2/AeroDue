package com.aerodue.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aerodue.app.AeroDueApplication
import com.aerodue.app.ui.components.HeroHeader
import com.aerodue.app.ui.components.MoneyBadge
import com.aerodue.app.ui.theme.Amber500
import com.aerodue.app.ui.theme.Indigo500
import com.aerodue.app.ui.theme.Money500
import com.aerodue.app.ui.theme.Money600
import com.aerodue.app.ui.theme.Sky500
import com.aerodue.core.demo.DemoFixtures
import com.aerodue.core.domain.ClaimRecommendation
import com.aerodue.core.domain.CompensationSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private data class SourceMeta(val label: String, val color: Color, val icon: ImageVector)

private fun CompensationSource.meta(): SourceMeta = when (this) {
    CompensationSource.REGULATION_DOT -> SourceMeta("US DOT", Indigo500, Icons.Outlined.Gavel)
    CompensationSource.REGULATION_EU261 -> SourceMeta("EU261", Sky500, Icons.Outlined.AccountBalance)
    CompensationSource.CREDIT_CARD -> SourceMeta("Credit card", Money600, Icons.Outlined.CreditCard)
    CompensationSource.BUSINESS_POLICY -> SourceMeta("Business policy", Amber500, Icons.Outlined.Business)
    CompensationSource.AIRLINE_GOODWILL -> SourceMeta("Goodwill", Color(0xFF8B5CF6), Icons.Outlined.Redeem)
    CompensationSource.CARRIER_CONTRACT -> SourceMeta("Carrier contract", Color(0xFF0EA5E9), Icons.Outlined.Flight)
}

@Composable
fun ClaimsScreen() {
    val app = LocalContext.current.applicationContext as AeroDueApplication
    val service = app.compensationService
    val claims = remember { service.assessDemo() }
    var summary by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(service) {
        loading = true
        withContext(Dispatchers.IO) {
            summary = service.passengerSummary(
                DemoFixtures.delayedConnectionEvent,
                DemoFixtures.demoProfile,
                claims,
            )
        }
        loading = false
    }

    val total = claims.sumOf { it.estimatedAmountUsd ?: 0.0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        HeroHeader(
            eyebrow = "Compensation receipt",
            title = "You may be owed",
        ) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "$${total.roundToInt()}",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
            Text(
                text = "across ${claims.size} stacked sources · estimated, on-device",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        if (loading) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                )
                                Text(
                                    "  Drafting your summary…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            }
                        } else {
                            Text(
                                text = summary.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }
            }

            items(claims, key = { "${it.source}-${it.title}" }) { claim ->
                ClaimCard(claim)
            }

            item {
                Text(
                    text = "Estimates only — final amounts depend on the airline, your card benefits guide, and applicable regulations. Not legal advice.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun ClaimCard(claim: ClaimRecommendation) {
    val meta = claim.source.meta()
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(meta.color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = meta.icon,
                        contentDescription = null,
                        tint = meta.color,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = claim.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = meta.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = meta.color,
                    )
                }
                claim.estimatedAmountUsd?.let {
                    MoneyBadge(amountLabel = "$${it.roundToInt()}")
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = claim.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Confidence",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(10.dp))
                LinearProgressIndicator(
                    progress = { claim.confidence.toFloat() },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = confidenceColor(claim.confidence),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "${(claim.confidence * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = confidenceColor(claim.confidence),
                    fontWeight = FontWeight.Bold,
                )
            }

            if (claim.actionSteps.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                claim.actionSteps.take(3).forEachIndexed { i, step ->
                    Row(modifier = Modifier.padding(top = 4.dp)) {
                        Text(
                            text = "${i + 1}. ",
                            style = MaterialTheme.typography.bodySmall,
                            color = meta.color,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun confidenceColor(confidence: Double): Color = when {
    confidence >= 0.75 -> Money500
    confidence >= 0.5 -> Amber500
    else -> Color(0xFFE11D48)
}
