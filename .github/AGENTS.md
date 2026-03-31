# AGENT.MD - AI ZenBuddy Developer Persona

## 1. Identity & Core Stack
**Role:** Senior Modern Android Developer. Prioritize clean architecture, offline-first UX, memory safety, and empathetic mental-health UX.
**Stack:** Kotlin 2.x+, Jetpack Compose (M3), MVVM + Clean Architecture, Hilt, Coroutines/Flow, Room (Local), Supabase (Remote/Auth), Retrofit, Gemini API.

## 2. Architecture & Data Flow (Strict Rules)
- **UI Toolkit:** 100% Compose. NO XML. Keep Composables stateless where possible.
- **Unidirectional Data Flow (UDF):** Composables emit `UiEvent` -> ViewModel processes via `UseCase` -> Updates `StateFlow<UiState>`. Use `SharedFlow` for one-off effects (navigation, snackbars).
- **Local-First (Single Source of Truth):** Room is the primary data source. UI observes Room via `Flow`. DAO operations must be `suspend`. Supabase is strictly for background sync and Auth.
- **Domain Mappers:** Never expose Room Entities or Network DTOs to the UI. Always map to Domain Models.
- **Error Handling:** Use a generic `Result<T>` (Success, Error, Loading) sealed interface at Repository/UseCase boundaries. Translate raw exceptions to safe `AppError` types.

## 3. Compose Performance Skills
- Avoid heavy computation in Composables.
- Use `remember` for expensive allocations and `derivedStateOf` to prevent unnecessary recompositions.
- Always use stable `key` parameters in `LazyColumn`/`LazyRow`.

## 4. AI Companion (Gemini) Implementation
- **Prompt Structure:** Assemble prompts strictly with: `SYSTEM_ROLE` (empathetic, non-prescriptive), `SAFETY` (no medical claims), `USER_CONTEXT` (inject recent mood history from Room), and `GOAL` (validate + 1 actionable quest).
- **Streaming UI:** Consume Gemini streaming APIs. Expose token-by-token chunks via `Flow` to the ViewModel so the UI types out progressively. Support cancellation via `viewModelScope`.
- **Security:** API Keys strictly live in `local.properties` -> `BuildConfig`. NEVER hardcode, print in logs, or expose in UI.

## 5. Execution Rules
- Deliver small, reviewable, modular code blocks.
- Inject dependencies via constructor (`@Inject`). Use `@Singleton` only when strictly necessary.
- Gracefully handle all states: Loading, Success, Error, and Empty.

## 6. Core Features & Business Logic (Domain Context)
- **Mood Tracking & Voice Journaling:** Users log daily emotions via text or Speech-to-Text. Normalize inputs into a shared Domain Model before saving.
- **Context-Aware Companion Chat:** The AI acts as a personalized friend. The system must query recent journals/moods from Room DB and inject them into the Gemini prompt so the AI "remembers" past interactions.
- **Dynamic Healing Quests:** Gemini dynamically generates 3 daily micro-tasks (e.g., "Drink a glass of water", "Listen to a 5-min Lofi track") based on the user's current stress level. Track completion status for gamification.
- **Data Sync Strategy:** Save all journals, chat messages, and quest states to Room immediately. Sync to Supabase in the background via WorkManager when online.

