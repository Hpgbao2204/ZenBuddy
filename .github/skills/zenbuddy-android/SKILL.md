---
name: zenbuddy-android
description: "Build the ZenBuddy mental-health Android app using Kotlin 2.x, Jetpack Compose M3, MVVM + Clean Architecture, Hilt, Room, Supabase, Gemini API. Use when: scaffolding new screens, adding features (mood tracking, voice journaling, AI chat, healing quests), fixing architecture violations, writing Room DAOs, setting up Gemini streaming, syncing with Supabase, debugging in VS Code, demoing on device or emulator."
argument-hint: 'Feature or layer to implement (e.g. "mood tracking screen", "Gemini streaming", "Room DAO", "VS Code setup")'
---

# ZenBuddy Android Skill

Mental-health companion app. Senior Kotlin developer persona. Deliver modular, reviewable code blocks.

## Stack at a Glance

| Layer       | Technology                                                          |
| ----------- | ------------------------------------------------------------------- |
| UI          | Jetpack Compose (Material 3), 100% NO XML                           |
| State       | MVVM + UDF: `UiEvent` → ViewModel → `StateFlow<UiState>`            |
| DI          | Hilt (`@HiltAndroidApp`, `@HiltViewModel`, constructor injection)   |
| Local DB    | Room 2.6+ — single source of truth, all DAOs `suspend` or `Flow`    |
| Remote/Auth | Supabase (background sync only via WorkManager)                     |
| AI          | Gemini API — streaming via `Flow`, cancellable via `viewModelScope` |
| Async       | Coroutines + Flow. NO RxJava.                                       |
| Network     | Retrofit + OkHttp                                                   |
| Nav         | Navigation Compose (single-Activity)                                |

## When to Use This Skill

- Scaffolding a new feature (screen + ViewModel + UseCase + Repository + DAO)
- Implementing Gemini streaming chat
- Setting up Supabase auth or sync
- Writing Room schema migrations
- Fixing UDF / recomposition / architecture violations
- Setting up VS Code for Android dev
- Running / demoing the app without Android Studio

## Procedure

### Step 1 — Identify the Layer

Determine which layer(s) the task touches using [Architecture Reference](./references/architecture.md).

### Step 2 — Choose the Right Template

Use the template matching the component type from [./assets/templates/](./assets/templates/).

### Step 3 — Implement

- **UI screens** → [Compose UI Guide](./references/compose-ui.md)
- **Data (Room/Supabase)** → [Data Layer Guide](./references/data-layer.md)
- **AI / Gemini** → [Gemini AI Guide](./references/gemini-ai.md)
- **New to VS Code Android** → [VS Code Setup & Demo](./references/vscode-setup.md)

### Step 4 — Validate

- All Composables stateless, receive state + lambda callbacks
- No Room Entity or Supabase DTO exposed to UI layer
- API keys only in `local.properties` → `BuildConfig`, never logged
- Every error path wrapped in `Result<T>` sealed interface
- `LazyColumn` / `LazyRow` items have stable `key` parameters

## Core Business Rules (never violate)

1. **Room is the single source of truth.** UI observes Room via `Flow`. Supabase is background-only.
2. **Domain models are always distinct** from Room `@Entity` and Supabase DTOs.
3. **Gemini prompts** must include: `SYSTEM_ROLE` + `SAFETY` + `USER_CONTEXT` (from Room) + `GOAL`.
4. **API keys** live exclusively in `local.properties` → `BuildConfig`. Zero logging of keys.
5. **Mood / journal inputs** normalised to a shared Domain Model before any persistence.

## Features Checklist

- [ ] Mood Tracking — emoji/slider → MoodEntity → Room → sync Supabase
- [ ] Voice Journaling — STT → JournalEntry domain model → Room
- [ ] AI Companion Chat — Gemini streaming, context-injected from Room
- [ ] Healing Quests — Gemini generates 3 micro-tasks, tracked in Room
- [ ] Auth — Supabase email/Google OAuth, persisted session
- [ ] Background Sync — WorkManager syncs Room → Supabase when online
