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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.DirectionsTransit
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aerodue.app.AeroDueApplication
import com.aerodue.app.assistant.TripPlan
import com.aerodue.app.assistant.TripRecommendation
import com.aerodue.app.ui.components.HeroHeader
import com.aerodue.app.ui.components.SectionCard
import com.aerodue.app.ui.components.TintChip
import com.aerodue.app.ui.theme.Amber500
import com.aerodue.app.ui.theme.Money500
import kotlinx.coroutines.launch

private const val ROUTE_LABEL = "UA 1234 · SFO → JFK"
private const val ARRIVAL_LOCAL = "11:30 PM"

@Composable
fun PremiumScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as AeroDueApplication
    val assistant = app.assistantService
    val scope = rememberCoroutineScope()

    var plan by remember { mutableStateOf<TripPlan?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        HeroHeader(
            eyebrow = "Premium preview",
            title = "Door-to-door optimizer",
            subtitle = "Your on-device agent plans the lowest-stress recovery: flight, hotel, ground, and airport.",
        ) {
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = Color.White.copy(alpha = 0.14f),
            ) {
                Text(
                    text = ROUTE_LABEL,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Button(
                onClick = {
                    scope.launch {
                        loading = true
                        plan = assistant.optimalTripPlan(ROUTE_LABEL, ARRIVAL_LOCAL)
                        loading = false
                    }
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.small,
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.size(10.dp))
                    Text("Planning your trip…", fontWeight = FontWeight.Bold)
                } else {
                    Text(
                        if (plan == null) "Find my optimal plan" else "Re-plan",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }

            plan?.let { p ->
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
                        Spacer(Modifier.size(12.dp))
                        Column {
                            Text(
                                text = "Agent plan · ${assistant.backendLabel}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = p.narrative,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }

                RecCard(Icons.Outlined.Flight, MaterialTheme.colorScheme.primary, p.flight)
                RecCard(Icons.Outlined.Hotel, Amber500, p.hotel)
                RecCard(Icons.Outlined.DirectionsTransit, Money500, p.ground)
                RecCard(Icons.Outlined.Luggage, MaterialTheme.colorScheme.secondary, p.airport)
            }

            if (plan == null && !loading) {
                Text(
                    text = "Tap above to generate a recommended plan. Premium wireframe — bookings are illustrative.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Back to trip")
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun RecCard(icon: ImageVector, accent: Color, rec: TripRecommendation) {
    SectionCard(title = rec.title, icon = icon, accent = accent) {
        Text(
            text = rec.detail,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        rec.tag?.let {
            Spacer(Modifier.height(10.dp))
            TintChip(label = it, tint = accent)
        }
    }
}
