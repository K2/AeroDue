package com.aerodue.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aerodue.app.ui.screens.AuthScreen
import com.aerodue.app.ui.screens.ClaimsScreen
import com.aerodue.app.ui.screens.ConsentScreen
import com.aerodue.app.ui.screens.CoverageScreen
import com.aerodue.app.ui.screens.HomeScreen
import com.aerodue.app.ui.screens.IntegrationsScreen
import com.aerodue.app.ui.screens.PremiumScreen
import com.aerodue.app.ui.screens.ProfileScreen
import com.aerodue.app.ui.screens.RegulationsScreen

sealed class Route(val path: String) {
    data object Auth : Route("auth")
    data object Consent : Route("consent")
    data object Home : Route("home")
    data object Coverage : Route("coverage")
    data object Claims : Route("claims")
    data object Profile : Route("profile")
    data object Regulations : Route("regulations")
    data object Premium : Route("premium")
    data object Integrations : Route("integrations")
}

@Composable
fun AeroDueApp() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination
    val showBottomBar = current?.route != Route.Auth.path &&
        current?.route != Route.Consent.path

    val bottomItems = listOf(
        Triple(Route.Home, "Trip", Icons.Outlined.Home),
        Triple(Route.Coverage, "Coverage", Icons.Outlined.CreditCard),
        Triple(Route.Claims, "Claims", Icons.AutoMirrored.Outlined.ReceiptLong),
        Triple(Route.Regulations, "Rules", Icons.Outlined.Gavel),
        Triple(Route.Profile, "Profile", Icons.Outlined.AccountCircle),
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                ) {
                    bottomItems.forEach { (route, label, icon) ->
                        val selected = current?.hierarchy?.any { it.route == route.path } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(route.path) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Route.Auth.path,
            modifier = Modifier.padding(padding),
        ) {
            composable(Route.Auth.path) {
                AuthScreen(
                    onSignedIn = {
                        navController.navigate(Route.Consent.path) {
                            popUpTo(Route.Auth.path) { inclusive = true }
                        }
                    },
                )
            }
            composable(Route.Consent.path) {
                ConsentScreen(
                    onContinue = {
                        navController.navigate(Route.Home.path) {
                            popUpTo(Route.Consent.path) { inclusive = true }
                        }
                    },
                )
            }
            composable(Route.Home.path) {
                HomeScreen(onOpenPremium = { navController.navigate(Route.Premium.path) })
            }
            composable(Route.Premium.path) {
                PremiumScreen(onBack = { navController.popBackStack() })
            }
            composable(Route.Coverage.path) { CoverageScreen() }
            composable(Route.Claims.path) { ClaimsScreen() }
            composable(Route.Regulations.path) { RegulationsScreen() }
            composable(Route.Profile.path) {
                ProfileScreen(onOpenIntegrations = { navController.navigate(Route.Integrations.path) })
            }
            composable(Route.Integrations.path) {
                IntegrationsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
