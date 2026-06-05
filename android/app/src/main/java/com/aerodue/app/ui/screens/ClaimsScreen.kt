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
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.aerodue.app.filing.FilingRecord
import com.aerodue.app.filing.FilingStatus
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
import kotlinx.coroutines.launch
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
    val filing = app.filingService
    val coordinator = app.filingCoordinator
    val scope = rememberCoroutineScope()
    val filingState by filing.state.collectAsState()
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

            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Outlined.Gavel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "AeroDue is not a legal service",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                "We prepare your paperwork, file with your confirmation, and help you follow up if a claim is rejected. You stay in control and submit each step.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }

            items(claims, key = { "${it.source}-${it.title}" }) { claim ->
                val key = "${claim.source}-${claim.title}"
                val record = filingState[key] ?: FilingRecord(claimKey = key)
                val sourceLabel = claim.source.meta().label
                ClaimCard(
                    claim = claim,
                    record = record,
                    onFile = {
                        scope.launch {
                            coordinator.file(key, claim.title, sourceLabel, claim.estimatedAmountUsd)
                        }
                    },
                    onAdvance = { filing.advance(key) },
                    onReject = { filing.reject(key, "Insufficient documentation") },
                    onFollowUp = {
                        scope.launch {
                            coordinator.analyzeAndRefile(
                                key,
                                claim.title,
                                sourceLabel,
                                record.note ?: "Insufficient documentation",
                            )
                        }
                    },
                )
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
private fun ClaimCard(
    claim: ClaimRecommendation,
    record: FilingRecord,
    onFile: () -> Unit,
    onAdvance: () -> Unit,
    onReject: () -> Unit,
    onFollowUp: () -> Unit,
) {
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

            Spacer(Modifier.height(14.dp))
            FilingFooter(
                record = record,
                onFile = onFile,
                onAdvance = onAdvance,
                onReject = onReject,
                onFollowUp = onFollowUp,
            )
        }
    }
}

@Composable
private fun FilingFooter(
    record: FilingRecord,
    onFile: () -> Unit,
    onAdvance: () -> Unit,
    onReject: () -> Unit,
    onFollowUp: () -> Unit,
) {
    val (label, tone) = when (record.status) {
        FilingStatus.NOT_FILED -> "Not filed" to MaterialTheme.colorScheme.onSurfaceVariant
        FilingStatus.FILED -> "Filed" to Indigo500
        FilingStatus.IN_REVIEW -> "In review" to Amber500
        FilingStatus.PAID -> "Paid" to Money500
        FilingStatus.REJECTED -> "Rejected" to Color(0xFFE11D48)
        FilingStatus.APPEALED -> "Appeal filed" to Sky500
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (record.working) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = record.workingMessage ?: "Working…",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                }
                return@Column
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(tone),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = tone,
                    fontWeight = FontWeight.Bold,
                )
                record.reference?.let {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "· $it",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            record.note?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            if (record.rejectionSummary != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Assistant analysis",
                    style = MaterialTheme.typography.labelMedium,
                    color = Sky500,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = record.rejectionSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 2.dp),
                )
                record.rejectionFixes.forEach { fix ->
                    Row(modifier = Modifier.padding(top = 3.dp)) {
                        Text(
                            text = "• ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Sky500,
                        )
                        Text(
                            text = fix,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (record.status) {
                    FilingStatus.NOT_FILED -> {
                        Button(
                            onClick = onFile,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 14.dp,
                                vertical = 6.dp,
                            ),
                        ) { Text("File claim", style = MaterialTheme.typography.labelMedium) }
                    }
                    FilingStatus.FILED, FilingStatus.IN_REVIEW -> {
                        Button(
                            onClick = onAdvance,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 14.dp,
                                vertical = 6.dp,
                            ),
                        ) { Text("Advance", style = MaterialTheme.typography.labelMedium) }
                        OutlinedButton(
                            onClick = onReject,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 14.dp,
                                vertical = 6.dp,
                            ),
                        ) { Text("Simulate rejection", style = MaterialTheme.typography.labelMedium) }
                    }
                    FilingStatus.REJECTED -> {
                        Button(
                            onClick = onFollowUp,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Sky500),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 14.dp,
                                vertical = 6.dp,
                            ),
                        ) { Text("Follow up (appeal)", style = MaterialTheme.typography.labelMedium) }
                    }
                    FilingStatus.APPEALED -> {
                        Button(
                            onClick = onAdvance,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 14.dp,
                                vertical = 6.dp,
                            ),
                        ) { Text("Advance", style = MaterialTheme.typography.labelMedium) }
                    }
                    FilingStatus.PAID -> Unit
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
