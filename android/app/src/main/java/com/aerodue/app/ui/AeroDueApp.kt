package com.aerodue.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aerodue.app.ui.screens.AuthScreen
import com.aerodue.app.ui.screens.ClaimsScreen
import com.aerodue.app.ui.screens.CoverageScreen
import com.aerodue.app.ui.screens.HomeScreen
import com.aerodue.app.ui.screens.ProfileScreen

sealed class Route(val path: String) {
    data object Auth : Route("auth")
    data object Home : Route("home")
    data object Coverage : Route("coverage")
    data object Claims : Route("claims")
    data object Profile : Route("profile")
}

@Composable
fun AeroDueApp() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination
    val showBottomBar = current?.route != Route.Auth.path

    val bottomItems = listOf(
        Triple(Route.Home, "Trip", Icons.Default.Home),
        Triple(Route.Coverage, "Coverage", Icons.Default.CardGiftcard),
        Triple(Route.Claims, "Claims", Icons.Default.Flight),
        Triple(Route.Profile, "Profile", Icons.Default.AccountCircle),
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
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
                            label = { Text(label) },
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
                        navController.navigate(Route.Home.path) {
                            popUpTo(Route.Auth.path) { inclusive = true }
                        }
                    },
                )
            }
            composable(Route.Home.path) { HomeScreen() }
            composable(Route.Coverage.path) { CoverageScreen() }
            composable(Route.Claims.path) { ClaimsScreen() }
            composable(Route.Profile.path) { ProfileScreen() }
        }
    }
}
