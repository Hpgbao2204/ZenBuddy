package com.zenbuddy.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.zenbuddy.ui.navigation.Route
import com.zenbuddy.ui.navigation.ZenNavGraph
import com.zenbuddy.ui.theme.ZenBuddyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val PREFS_NAME = "zenbuddy_prefs"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val onboardingDone = prefs.getBoolean(KEY_ONBOARDING_DONE, false)

        setContent {
            ZenBuddyTheme {
                val navController = rememberNavController()
                ZenNavGraph(
                    navController = navController,
                    startDestination = if (onboardingDone) Route.Home.path else Route.Onboarding.path,
                    onOnboardingComplete = {
                        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
                    }
                )
            }
        }
    }
}
