package com.example.gazdik_vamz.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.gazdik_vamz.ui.screens.*

/** Navigačný graf aplikácie. */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // startDestination určuje, ktorá obrazovka sa zobrazí ako prvá po spustení aplikácie
    NavHost(navController = navController, startDestination = Screen.Home.route) {

        // Hlavná obrazovka
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }

        // Detail/úprava rutiny
        composable(
            route = Screen.RoutineDetail.route,
            arguments = listOf(navArgument("routineId") {
                type = NavType.LongType; defaultValue = -1L
            })
        ) { back ->
            val routineId = back.arguments?.getLong("routineId") ?: -1L
            RoutineDetailScreen(navController, routineId)
        }

        // Aktívny tréning
        composable(
            route = Screen.ActiveWorkout.route,
            arguments = listOf(
                navArgument("routineId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("sessionId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { back ->
            val routineId  = back.arguments?.getLong("routineId")  ?: -1L
            val sessionId  = back.arguments?.getLong("sessionId")  ?: -1L
            ActiveWorkoutScreen(navController, routineId, sessionId)
        }

        // História tréningov
        composable(Screen.History.route) {
            HistoryScreen(navController)
        }

        // Detail dokončeného tréningu
        composable(
            route = Screen.WorkoutDetail.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { back ->
            val sessionId = back.arguments?.getLong("sessionId") ?: 0L
            WorkoutDetailScreen(navController, sessionId)
        }

        // Štatistiky
        composable(Screen.Stats.route) {
            StatsScreen(navController)
        }
    }
}
