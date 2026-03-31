# Clean Architecture + MVVM — ZenBuddy Rules

## Package Structure

```
com.zenbuddy/
├── app/                        # Application class, DI setup
├── core/
│   ├── result/                 # Result<T> sealed interface
│   ├── error/                  # AppError sealed class
│   └── extensions/             # Kotlin extension functions
├── data/
│   ├── local/
│   │   ├── dao/                # Room DAOs
│   │   ├── entity/             # Room @Entity classes
│   │   └── db/                 # AppDatabase
│   ├── remote/
│   │   ├── dto/                # Supabase / Retrofit DTOs
│   │   └── api/                # Retrofit service interfaces
│   └── repository/             # Repository implementations
├── domain/
│   ├── model/                  # Pure domain models (no Android deps)
│   ├── repository/             # Repository interfaces
│   └── usecase/                # One UseCase per business action
└── ui/
    ├── navigation/             # NavGraph, Routes
    ├── theme/                  # MaterialTheme, Typography, Colors
    └── feature/
        ├── mood/               # MoodScreen + MoodViewModel
        ├── journal/            # JournalScreen + JournalViewModel
        ├── chat/               # ChatScreen + ChatViewModel
        └── quest/              # QuestScreen + QuestViewModel
```

## Unidirectional Data Flow (UDF)

```
User Action
    │
    ▼
Composable emits UiEvent
    │
    ▼
ViewModel.onEvent(event: UiEvent)
    │  launches coroutine
    ▼
UseCase.invoke(params)
    │  returns Flow<Result<DomainModel>>
    ▼
Repository (interface) ──► RoomDAO (suspend/Flow)
                       └──► Supabase (background only)
    │
    ▼
ViewModel maps Result to UiState
    │  StateFlow
    ▼
Composable re-renders via collectAsStateWithLifecycle()
```

## Result & Error Types

```kotlin
// core/result/Result.kt
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val error: AppError) : Result<Nothing>
    data object Loading : Result<Nothing>
}

// core/error/AppError.kt
sealed class AppError(val message: String) {
    class NetworkError(msg: String) : AppError(msg)
    class DatabaseError(msg: String) : AppError(msg)
    class AuthError(msg: String) : AppError(msg)
    class AiError(msg: String) : AppError(msg)
    class Unknown(msg: String) : AppError(msg)
}
```

## Domain Model Rule

Each feature has three separate representations:

| Layer | Class | Example |
|---|---|---|
| Room | `@Entity` | `MoodEntity` |
| Network | `DTO` | `MoodDto` |
| Domain | plain `data class` | `MoodEntry` |

**Mappers live in `data/` layer.** ViewModel and above ONLY know domain models.

```kotlin
// data/local/entity/MoodEntity.kt
@Entity(tableName = "moods")
data class MoodEntity(
    @PrimaryKey val id: String,
    val score: Int,           // 1-10
    val note: String?,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// Extension mapper — stays in data layer
fun MoodEntity.toDomain() = MoodEntry(id, score, note, createdAt)
fun MoodEntry.toEntity() = MoodEntity(id, score, note, createdAt = createdAt)
```

## ViewModel Template Pattern

```kotlin
@HiltViewModel
class MoodViewModel @Inject constructor(
    private val logMood: LogMoodUseCase,
    private val getMoods: GetMoodsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoodUiState())
    val uiState: StateFlow<MoodUiState> = _uiState.asStateFlow()

    // One-off events (navigation, snackbar)
    private val _effects = MutableSharedFlow<MoodEffect>()
    val effects: SharedFlow<MoodEffect> = _effects.asSharedFlow()

    fun onEvent(event: MoodUiEvent) {
        when (event) {
            is MoodUiEvent.LogMood -> handleLogMood(event.score, event.note)
        }
    }

    private fun handleLogMood(score: Int, note: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            logMood(score, note).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _effects.emit(MoodEffect.NavigateToHome)
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(isLoading = false, error = result.error.message)
                    }
                    Result.Loading -> Unit
                }
            }
        }
    }
}
```

## Hilt Module Structure

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "zenbuddy.db").build()

    @Provides fun provideMoodDao(db: AppDatabase): MoodDao = db.moodDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindMoodRepository(impl: MoodRepositoryImpl): MoodRepository
}
```
