package com.aerodue.app.plugins

/**
 * Optional, decoupled extension point. Plugins ship their own terms; enabling
 * one is an explicit user action and is NOT covered by AeroDue's core EULA.
 * This keeps regulated/experimental features (e.g. prediction-market hedging)
 * isolated from the core product.
 */
interface CompedPlugin {
    val id: String
    val displayName: String
    val description: String

    /** The plugin's own end-user license / terms. AeroDue does not own these. */
    val externalEula: String

    /** Plugins are always opt-in; core never auto-enables them. */
    val enabledByDefault: Boolean
        get() = false
}

data class HedgeContext(
    val flightNumber: String,
    val delayProbability: Double,
    /** Region/market the user is physically nearest to, if known. */
    val nearestMarketRegion: String?,
)

data class HedgeSuggestion(
    val market: String,
    val rationale: String,
    val externalUrl: String,
)

/** Plugins that propose external hedges/bets against disruption. */
interface HedgePlugin : CompedPlugin {
    fun suggestHedges(context: HedgeContext): List<HedgeSuggestion>
}

/**
 * Polymarket hedge — intentionally a LATER OPTION and disabled.
 *
 * Real-money prediction markets implicate US CFTC + state gambling law and
 * Polymarket is geo-blocked to US retail. Shipped only as an interface +
 * inert stub so the architecture exists without exposing core users to it.
 */
class PolymarketHedgePlugin : HedgePlugin {
    override val id = "polymarket-hedge"
    override val displayName = "Polymarket flight/weather hedge"
    override val description =
        "Suggests external prediction-market positions that offset delay cost. " +
            "Third-party, region-restricted, experimental."
    override val externalEula =
        "Provided by an independent third party under its own terms of service. " +
            "Not legal, financial, or gambling advice. Availability is region-restricted; " +
            "you are responsible for eligibility and compliance in your jurisdiction."

    /** Inert until eligibility + compliance gating exists. */
    override fun suggestHedges(context: HedgeContext): List<HedgeSuggestion> = emptyList()
}

data class RegisteredPlugin(
    val plugin: CompedPlugin,
    val enabled: Boolean,
    val eulaAccepted: Boolean,
)

/** In-memory registry. Persistence + real gating come later. */
class PluginRegistry {

    private val entries = linkedMapOf<String, RegisteredPlugin>()

    init {
        // Available but disabled: a later option, behind its own terms.
        register(PolymarketHedgePlugin())
    }

    fun register(plugin: CompedPlugin) {
        entries[plugin.id] = RegisteredPlugin(
            plugin = plugin,
            enabled = plugin.enabledByDefault,
            eulaAccepted = false,
        )
    }

    fun available(): List<RegisteredPlugin> = entries.values.toList()

    fun isEnabled(id: String): Boolean = entries[id]?.enabled == true

    /** Enabling requires explicit acceptance of the plugin's external EULA. */
    fun enable(id: String, eulaAccepted: Boolean) {
        val existing = entries[id] ?: return
        if (!eulaAccepted) return
        entries[id] = existing.copy(enabled = true, eulaAccepted = true)
    }

    fun disable(id: String) {
        val existing = entries[id] ?: return
        entries[id] = existing.copy(enabled = false)
    }
}
