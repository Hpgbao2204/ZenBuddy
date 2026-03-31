package com.zenbuddy.ui.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val PREFS_NAME = "zenbuddy_prefs"
private const val KEY_REMINDERS = "reminders_enabled"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val syncState by viewModel.state.collectAsStateWithLifecycle()
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var remindersEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_REMINDERS, false)) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val firebaseUser = remember { FirebaseAuth.getInstance().currentUser }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Data?") },
            text = { Text("This will permanently delete all your moods, journals, chats, and quests. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    context.deleteDatabase("zenbuddy.db")
                    showClearDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("How ZenBuddy Works 🌸") },
            text = {
                Text(
                    "• Log your mood daily with a simple slider\n" +
                    "• Write journals — AI will reflect on your thoughts\n" +
                    "• Chat with your AI companion for support\n" +
                    "• Complete gentle healing quests each day\n" +
                    "• Practice 4-7-8 breathing when stressed\n" +
                    "• Track mood trends in Insights\n\n" +
                    "Data syncs to Firebase when you're logged in."
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Got it 💜")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout?") },
            text = { Text("You can log back in anytime. Your local data will remain on this device.") },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseAuth.getInstance().signOut()
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings ⚙️") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // App info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                ) {
                    Text("🌸", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ZenBuddy",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Your mental wellness companion",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "v1.0.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // General
            Text(
                text = "General",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = "Reminders",
                subtitle = "Daily mood check-in reminders",
                checked = remindersEnabled,
                onCheckedChange = { enabled ->
                    remindersEnabled = enabled
                    prefs.edit().putBoolean(KEY_REMINDERS, enabled).apply()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Crisis Resources
            Text(
                text = "Crisis Resources",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            SettingsItem(
                icon = Icons.Default.Phone,
                title = "988 Suicide & Crisis Lifeline (US)",
                subtitle = "Tap to call 988",
                tintColor = MaterialTheme.colorScheme.error,
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:988"))
                    context.startActivity(intent)
                }
            )
            SettingsItem(
                icon = Icons.Default.Favorite,
                title = "Vietnam Crisis Hotline",
                subtitle = "Tap to call 1800-599-0019",
                tintColor = MaterialTheme.colorScheme.error,
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:18005990019"))
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Account
            Text(
                text = "Account",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            if (firebaseUser != null) {
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = firebaseUser.displayName ?: "User",
                    subtitle = firebaseUser.email ?: "Logged in"
                )
                SettingsItem(
                    icon = Icons.Default.CloudUpload,
                    title = if (syncState.isSyncing) "Syncing..." else "Sync to Cloud",
                    subtitle = syncState.syncMessage ?: "Upload data to Firebase",
                    onClick = {
                        if (!syncState.isSyncing) {
                            viewModel.syncToCloud()
                        }
                    }
                )
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Logout",
                    subtitle = "Sign out of your account",
                    tintColor = MaterialTheme.colorScheme.error,
                    onClick = { showLogoutDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About
            Text(
                text = "About",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            SettingsItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                title = "How it works",
                subtitle = "Learn about ZenBuddy's features",
                onClick = { showAboutDialog = true }
            )
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Privacy",
                subtitle = "Your data stays on your device"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Danger zone
            Text(
                text = "Data",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            SettingsItem(
                icon = Icons.Default.Delete,
                title = "Clear all data",
                subtitle = "Delete all moods, journals, chats, and quests",
                tintColor = MaterialTheme.colorScheme.error,
                onClick = { showClearDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy note
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = "🔒 ZenBuddy syncs your data to Firebase when logged in. " +
                            "AI conversations are processed through Gemini API. " +
                            "Your mental health data is kept secure.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tintColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = tintColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onCheckedChange(!checked) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
