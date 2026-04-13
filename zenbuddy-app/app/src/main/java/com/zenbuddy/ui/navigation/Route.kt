package com.zenbuddy.ui.navigation

sealed class Route(val path: String) {
    data object Auth : Route("auth")
    data object Onboarding : Route("onboarding")
    data object Home : Route("home")
    data object Mood : Route("mood")
    data object Journal : Route("journal")
    data object Chat : Route("chat")
    data object Quests : Route("quests")
    data object Breathing : Route("breathing")
    data object Insights : Route("insights")
    data object Settings : Route("settings")
    data object Lofi : Route("lofi")
    data object Games : Route("games")

    // Health tracking routes
    data object Dashboard : Route("dashboard")
    data object StepTracker : Route("step_tracker")
    data object FoodScanner : Route("food_scanner")
    data object ExerciseLibrary : Route("exercise_library")
    data object Profile : Route("profile")
    data object HealthChat : Route("health_chat")
    data object Schedule : Route("schedule")
}
