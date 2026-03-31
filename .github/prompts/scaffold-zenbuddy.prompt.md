---
name: scaffold-zenbuddy
description: "Scaffold the complete ZenBuddy Android project from scratch — generates every file (build.gradle.kts, AndroidManifest, all Kotlin sources, Room DB, Hilt modules, Compose screens, Gemini integration, Supabase sync, WorkManager). Use when: starting from zero, rebuilding the project, generating the full codebase end-to-end."
argument-hint: "Target subfolder name, default: zenbuddy-app"
agent: agent
---

You are a Senior Android Developer. Scaffold the **complete, runnable** ZenBuddy Android project in the subfolder `${input:folderName:zenbuddy-app}/` relative to the workspace root.

## Constraints (non-negotiable)

- Kotlin 2.x, Jetpack Compose Material 3, ABSOLUTELY NO XML layouts
- MVVM + Clean Architecture: `ui/` → `domain/` → `data/`
- Hilt for DI, Room for local DB (single source of truth), Supabase for background sync
- Gradle **Wrapper** only — do NOT require a global Gradle installation
- All API keys read from `local.properties` → `BuildConfig` — never hardcoded
- Every error path returns `Result<T>` sealed interface

---

## Step 1 — Project Root Files

Create these files first:

### `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        google(); mavenCentral(); gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "ZenBuddy"
include(":app")
```

### `build.gradle.kts` (root)

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
```

### `gradle/libs.versions.toml` (Version Catalog)

Generate the full version catalog with these exact versions:

```toml
[versions]
agp = "8.7.3"
kotlin = "2.1.0"
ksp = "2.1.0-1.0.29"
hilt = "2.51.1"
compose-bom = "2025.02.00"
navigation = "2.8.9"
room = "2.7.0"
lifecycle = "2.8.7"
coroutines = "1.10.1"
supabase = "3.1.4"
generativeai = "0.9.0"
work = "2.10.0"
coil = "2.7.0"
retrofit = "2.11.0"
okhttp = "4.12.0"

[libraries]
# AndroidX Core
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version = "1.15.0" }
androidx-lifecycle-runtime = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-activity = { group = "androidx.activity", name = "activity-compose", version = "1.10.1" }
compose-navigation = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }
hilt-work = { group = "androidx.hilt", name = "hilt-work", version = "1.2.0" }
hilt-work-compiler = { group = "androidx.hilt", name = "hilt-compiler", version = "1.2.0" }
# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
# Coroutines
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
# Supabase
supabase-auth = { group = "io.github.jan-tennert.supabase", name = "auth-kt", version.ref = "supabase" }
supabase-postgrest = { group = "io.github.jan-tennert.supabase", name = "postgrest-kt", version.ref = "supabase" }
supabase-storage = { group = "io.github.jan-tennert.supabase", name = "storage-kt", version.ref = "supabase" }
ktor-client-android = { group = "io.ktor", name = "ktor-client-android", version = "3.1.1" }
# Gemini
generativeai = { group = "com.google.ai.client.generativeai", name = "generativeai", version.ref = "generativeai" }
# WorkManager
work-runtime = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }
# Coil
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
# Retrofit
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
# Test
junit = { group = "junit", name = "junit", version = "4.13.2" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version = "1.2.1" }
compose-ui-test = { group = "androidx.compose.ui", name = "ui-test-junit4" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

### `app/build.gradle.kts`

Read API keys from `local.properties` into `BuildConfig`. Full dependencies block using the version catalog above.

### `gradle/wrapper/gradle-wrapper.properties`

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.11.1-bin.zip
networkTimeout=10000
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

> **Note on GRADLE_USER_HOME**: By default Gradle caches in `~/.gradle`. To isolate like venv, the bootstrap script sets `GRADLE_USER_HOME=.gradle-home` so all Gradle artifacts stay in the project folder.

---

## Step 2 — App Entry Points

### `app/src/main/AndroidManifest.xml`

Single-Activity, permissions: INTERNET, RECORD_AUDIO.

### `app/src/main/java/com/zenbuddy/app/ZenBuddyApp.kt`

```kotlin
@HiltAndroidApp
class ZenBuddyApp : Application()
```

### `app/src/main/java/com/zenbuddy/app/MainActivity.kt`

```kotlin
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
```

---

## Step 3 — Core Layer

Generate these files completely:

1. `core/result/Result.kt` — `sealed interface Result<out T>` with Success, Error, Loading
2. `core/error/AppError.kt` — `sealed class AppError` with NetworkError, DatabaseError, AuthError, AiError, Unknown
3. `core/di/DispatcherModule.kt` — `@IoDispatcher`, `@MainDispatcher` qualifiers + Hilt module

---

## Step 4 — Data Layer (all files)

Generate ALL of:

- `data/local/db/AppDatabase.kt` — Room DB with 4 entities
- `data/local/entity/MoodEntity.kt` + mapper
- `data/local/entity/JournalEntity.kt` + mapper
- `data/local/entity/ChatMessageEntity.kt` + mapper
- `data/local/entity/QuestEntity.kt` + mapper
- `data/local/dao/MoodDao.kt`
- `data/local/dao/JournalDao.kt`
- `data/local/dao/ChatDao.kt`
- `data/local/dao/QuestDao.kt`
- `data/remote/dto/` — DTOs for Supabase (one per entity)
- `data/repository/MoodRepositoryImpl.kt`
- `data/repository/JournalRepositoryImpl.kt`
- `data/repository/ChatRepositoryImpl.kt` — includes Gemini streaming
- `data/repository/QuestRepositoryImpl.kt`
- `data/sync/MoodSyncWorker.kt` — HiltWorker via WorkManager
- `data/di/DatabaseModule.kt`
- `data/di/NetworkModule.kt` — Supabase client from BuildConfig keys
- `data/di/RepositoryModule.kt` — @Binds all repos

---

## Step 5 — Domain Layer (all files)

- `domain/model/MoodEntry.kt`
- `domain/model/JournalEntry.kt`
- `domain/model/ChatMessage.kt`
- `domain/model/Quest.kt`
- `domain/model/ChatContext.kt`
- `domain/repository/MoodRepository.kt` (interface)
- `domain/repository/JournalRepository.kt` (interface)
- `domain/repository/ChatRepository.kt` (interface)
- `domain/repository/QuestRepository.kt` (interface)
- `domain/usecase/mood/LogMoodUseCase.kt`
- `domain/usecase/mood/GetMoodsUseCase.kt`
- `domain/usecase/journal/SaveJournalUseCase.kt`
- `domain/usecase/journal/GetJournalsUseCase.kt`
- `domain/usecase/chat/SendMessageUseCase.kt`
- `domain/usecase/quest/GenerateQuestsUseCase.kt`
- `domain/usecase/quest/CompleteQuestUseCase.kt`

---

## Step 6 — UI Layer (all files)

### Theme

- `ui/theme/Color.kt` — ZenGreen, CalmBlue, DarkSurface palette
- `ui/theme/Type.kt`
- `ui/theme/Theme.kt` — light + dark `ZenBuddyTheme`

### Navigation

- `ui/navigation/Route.kt` — sealed class, paths: home, mood, journal, chat, quests
- `ui/navigation/ZenNavGraph.kt` — NavHost with bottom bar

### Features (each: Route + Screen + ViewModel + UiState/Event/Effect)

1. `ui/feature/home/` — dashboard, shows today's mood + quests summary + chat shortcut
2. `ui/feature/mood/` — emoji slider (1–10), note field, save button
3. `ui/feature/journal/` — text input + optional voice-to-text (SpeechRecognizer), list of past entries
4. `ui/feature/chat/` — streaming AI chat, token-by-token animation, cancel button
5. `ui/feature/quest/` — list of 3 daily quests + checkbox completion + generate button

---

## Step 7 — Resources

- `app/src/main/res/values/strings.xml`
- `local.properties.example` (committed template with placeholder values):

```properties
sdk.dir=/path/to/android/sdk
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key
GEMINI_API_KEY=your-gemini-key
```

---

## Step 8 — VS Code Workspace Config

Create `.vscode/tasks.json` with tasks: Build & Install Debug, Run Tests, Logcat.

---

## Output Rules

- Generate every file completely — no `// TODO` left
- Do NOT truncate any file with "rest of implementation similar to..."
- Each file must compile standalone (correct imports, no missing references)
- After all files: print a summary table with file path + purpose
- Final line: `./gradlew assembleDebug` command to verify the build
