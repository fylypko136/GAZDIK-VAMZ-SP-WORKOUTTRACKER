package com.example.gazdik_vamz.ui.navigation

/** Definuje všetky navigačné destinácie aplikácie. */
sealed class Screen(val route: String) {

    /** Hlavná obrazovka — zoznam rutín a celkové štatistiky. */
    object Home : Screen("home")

    /** Obrazovka detailu/úpravy rutiny. routineId = -1 vytvorenie novej rutiny. */
    object RoutineDetail : Screen("routine_detail/{routineId}") {
        fun createRoute(routineId: Long = -1L) = "routine_detail/$routineId"
    }

    /**
     * Obrazovka aktívneho tréningu.
     * routineId = -1 → voľný tréning bez rutiny.
     * sessionId = -1 → nová session; inak obnova prerušenej session.
     */
    object ActiveWorkout : Screen("active_workout/{routineId}/{sessionId}") {
        fun createRoute(routineId: Long = -1L, sessionId: Long = -1L) =
            "active_workout/$routineId/$sessionId"
    }

    /** Obrazovka histórie tréningov. */
    object History : Screen("history")

    /** Obrazovka detailu dokončeného tréningu.*/
    object WorkoutDetail : Screen("workout_detail/{sessionId}") {
        fun createRoute(sessionId: Long) = "workout_detail/$sessionId"
    }

    /** Obrazovka štatistík — grafy, progress váhy. */
    object Stats : Screen("stats")
}
