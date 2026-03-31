package com.zenbuddy.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.zenbuddy.ui.navigation.ZenNavGraph
import com.zenbuddy.ui.theme.ZenBuddyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZenBuddyTheme {
                val navController = rememberNavController()
                ZenNavGraph(navController)
            }
        }
    }
}
