# Data Layer — Room + Supabase

## Room Setup

### AppDatabase

```kotlin
// data/local/db/AppDatabase.kt
@Database(
    entities = [MoodEntity::class, JournalEntity::class, ChatMessageEntity::class, QuestEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moodDao(): MoodDao
    abstract fun journalDao(): JournalDao
    abstract fun chatDao(): ChatDao
    abstract fun questDao(): QuestDao
}
```

### DAO Patterns

```kotlin
// data/local/dao/MoodDao.kt
@Dao
interface MoodDao {
    // Flow — UI observes changes automatically
    @Query("SELECT * FROM moods ORDER BY createdAt DESC")
    fun getAllMoods(): Flow<List<MoodEntity>>

    @Query("SELECT * FROM moods WHERE createdAt >= :from ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentMoods(from: Long, limit: Int = 7): Flow<List<MoodEntity>>

    // suspend for one-shot operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mood: MoodEntity)

    @Update
    suspend fun update(mood: MoodEntity)

    @Query("SELECT * FROM moods WHERE isSynced = 0")
    suspend fun getUnsynced(): List<MoodEntity>

    @Query("UPDATE moods SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)
}
```

### Entities

```kotlin
// data/local/entity/MoodEntity.kt
@Entity(tableName = "moods", indices = [Index("createdAt")])
data class MoodEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val score: Int,                    // 1–10
    val note: String? = null,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// data/local/entity/JournalEntity.kt
@Entity(tableName = "journals")
data class JournalEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val text: String,
    val audioPath: String? = null,     // local file path for voice recording
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// data/local/entity/QuestEntity.kt
@Entity(tableName = "quests")
data class QuestEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false,
    val generatedForDate: String,      // "2026-03-31"
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// data/local/entity/ChatMessageEntity.kt
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val sessionId: String,
    val createdAt: Long = System.currentTimeMillis()
)
```

## Repository Implementation

```kotlin
// data/repository/MoodRepositoryImpl.kt
class MoodRepositoryImpl @Inject constructor(
    private val moodDao: MoodDao,
    private val supabaseClient: SupabaseClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : MoodRepository {

    override fun getMoods(): Flow<Result<List<MoodEntry>>> =
        moodDao.getAllMoods()
            .map { entities -> Result.Success(entities.map { it.toDomain() }) }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)

    override suspend fun logMood(entry: MoodEntry): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching { moodDao.insert(entry.toEntity()) }
                .fold(
                    onSuccess = { Result.Success(Unit) },
                    onFailure = { Result.Error(AppError.DatabaseError(it.message ?: "Insert failed")) }
                )
        }
}
```

## Supabase Setup

```kotlin
// app/di/NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideSupabase(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}
```

**`local.properties`** (never commit to git):
```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key
GEMINI_API_KEY=your-gemini-key
```

**`build.gradle.kts`** (read from local.properties safely):
```kotlin
import java.util.Properties
val localProps = Properties().also {
    val f = rootProject.file("local.properties")
    if (f.exists()) it.load(f.inputStream())
}

android {
    buildFeatures { buildConfig = true }
    defaultConfig {
        buildConfigField("String", "SUPABASE_URL", "\"${localProps["SUPABASE_URL"]}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProps["SUPABASE_ANON_KEY"]}\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"${localProps["GEMINI_API_KEY"]}\"")
    }
}
```

## WorkManager Background Sync

```kotlin
// data/sync/MoodSyncWorker.kt
@HiltWorker
class MoodSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val moodDao: MoodDao,
    private val supabase: SupabaseClient
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val unsynced = moodDao.getUnsynced()
            if (unsynced.isNotEmpty()) {
                supabase.from("moods").upsert(unsynced.map { it.toDto() })
                moodDao.markSynced(unsynced.map { it.id })
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

// Schedule once per constraint: network available
fun scheduleMoodSync(context: Context) {
    val request = PeriodicWorkRequestBuilder<MoodSyncWorker>(1, TimeUnit.HOURS)
        .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
        .build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "mood_sync", ExistingPeriodicWorkPolicy.KEEP, request
    )
}
```

## Schema Migrations

```kotlin
// Always provide migrations — never allowDestructiveMigration in production
Room.databaseBuilder(ctx, AppDatabase::class.java, "zenbuddy.db")
    .addMigrations(MIGRATION_1_2)
    .build()

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE moods ADD COLUMN tags TEXT DEFAULT NULL")
    }
}
```
