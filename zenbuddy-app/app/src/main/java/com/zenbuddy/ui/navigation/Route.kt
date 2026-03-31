package com.zenbuddy.ui.navigation

sealed class Route(val path: String) {
    data object Home : Route("home")
    data object Mood : Route("mood")
    data object Journal : Route("journal")
    data object Chat : Route("chat")
    data object Quests : Route("quests")
}
