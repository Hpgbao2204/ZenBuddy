package com.zenbuddy.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zenbuddy.data.local.dao.JournalDao
import com.zenbuddy.data.local.dao.MoodDao
import com.zenbuddy.data.local.dao.QuestDao
import com.zenbuddy.data.local.entity.JournalEntity
import com.zenbuddy.data.local.entity.MoodEntity
import com.zenbuddy.data.local.entity.QuestEntity
import com.zenbuddy.domain.repository.SyncRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val moodDao: MoodDao,
    private val journalDao: JournalDao,
    private val questDao: QuestDao
) : SyncRepository {

    private val uid: String?
        get() = firebaseAuth.currentUser?.uid

    override suspend fun syncToCloud(): Result<Unit> = try {
        val userId = uid ?: return Result.failure(Exception("Not logged in"))
        val userDoc = firestore.collection("users").document(userId)

        // Sync unsynced moods
        val unsyncedMoods = moodDao.getUnsynced(userId)
        unsyncedMoods.forEach { mood ->
            userDoc.collection("moods").document(mood.id).set(mood.toMap()).await()
        }
        if (unsyncedMoods.isNotEmpty()) {
            moodDao.markSynced(unsyncedMoods.map { it.id })
        }

        // Sync unsynced journals
        val unsyncedJournals = journalDao.getUnsynced(userId)
        unsyncedJournals.forEach { journal ->
            userDoc.collection("journals").document(journal.id).set(journal.toMap()).await()
        }
        if (unsyncedJournals.isNotEmpty()) {
            journalDao.markSynced(unsyncedJournals.map { it.id })
        }

        // Sync unsynced quests
        val unsyncedQuests = questDao.getUnsynced(userId)
        unsyncedQuests.forEach { quest ->
            userDoc.collection("quests").document(quest.id).set(quest.toMap()).await()
        }
        if (unsyncedQuests.isNotEmpty()) {
            questDao.markSynced(unsyncedQuests.map { it.id })
        }

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun syncFromCloud(): Result<Unit> = try {
        val userId = uid ?: return Result.failure(Exception("Not logged in"))
        val userDoc = firestore.collection("users").document(userId)

        // Fetch moods from Firestore
        val moodDocs = userDoc.collection("moods").get().await()
        moodDocs.documents.forEach { doc ->
            doc.toMoodEntity(userId)?.let { moodDao.insert(it) }
        }

        // Fetch journals from Firestore
        val journalDocs = userDoc.collection("journals").get().await()
        journalDocs.documents.forEach { doc ->
            doc.toJournalEntity(userId)?.let { journalDao.insert(it) }
        }

        // Fetch quests from Firestore
        val questDocs = userDoc.collection("quests").get().await()
        val questEntities = questDocs.documents.mapNotNull { it.toQuestEntity(userId) }
        if (questEntities.isNotEmpty()) {
            questDao.insertAll(questEntities)
        }

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun MoodEntity.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "score" to score,
        "note" to note,
        "createdAt" to createdAt
    )

    private fun JournalEntity.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "text" to text,
        "audioPath" to audioPath,
        "createdAt" to createdAt
    )

    private fun QuestEntity.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "isCompleted" to isCompleted,
        "generatedForDate" to generatedForDate,
        "createdAt" to createdAt
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toMoodEntity(userId: String): MoodEntity? {
        return try {
            MoodEntity(
                id = getString("id") ?: return null,
                userId = userId,
                score = getLong("score")?.toInt() ?: 3,
                note = getString("note"),
                isSynced = true,
                createdAt = getLong("createdAt") ?: System.currentTimeMillis()
            )
        } catch (_: Exception) { null }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toJournalEntity(userId: String): JournalEntity? {
        return try {
            JournalEntity(
                id = getString("id") ?: return null,
                userId = userId,
                text = getString("text") ?: "",
                audioPath = getString("audioPath"),
                isSynced = true,
                createdAt = getLong("createdAt") ?: System.currentTimeMillis()
            )
        } catch (_: Exception) { null }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toQuestEntity(userId: String): QuestEntity? {
        return try {
            QuestEntity(
                id = getString("id") ?: return null,
                userId = userId,
                title = getString("title") ?: "",
                isCompleted = getBoolean("isCompleted") ?: false,
                generatedForDate = getString("generatedForDate") ?: "",
                isSynced = true,
                createdAt = getLong("createdAt") ?: System.currentTimeMillis()
            )
        } catch (_: Exception) { null }
    }
}
