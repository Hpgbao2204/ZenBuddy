package com.zenbuddy.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zenbuddy.data.local.entity.ScheduleEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedule_entries WHERE date = :date ORDER BY timeHour, timeMinute")
    fun getByDate(date: String): Flow<List<ScheduleEntryEntity>>

    @Query("SELECT * FROM schedule_entries WHERE date >= :fromDate ORDER BY date, timeHour, timeMinute")
    fun getUpcoming(fromDate: String): Flow<List<ScheduleEntryEntity>>

    @Query("SELECT * FROM schedule_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ScheduleEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ScheduleEntryEntity)

    @Update
    suspend fun update(entry: ScheduleEntryEntity)

    @Query("UPDATE schedule_entries SET isCompleted = :completed WHERE id = :id")
    suspend fun markCompleted(id: String, completed: Boolean)

    @Query("DELETE FROM schedule_entries WHERE id = :id")
    suspend fun deleteById(id: String)
}
