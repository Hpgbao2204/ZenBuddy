package com.zenbuddy.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zenbuddy.data.local.dao.MoodDao
import com.zenbuddy.data.remote.dto.toDto
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

@HiltWorker
class MoodSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val moodDao: MoodDao,
    private val supabaseClient: SupabaseClient
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val unsynced = moodDao.getUnsynced()
            if (unsynced.isEmpty()) return Result.success()

            val dtos = unsynced.map { it.toDto() }
            supabaseClient.postgrest.from("moods").upsert(dtos)

            moodDao.markSynced(unsynced.map { it.id })
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
