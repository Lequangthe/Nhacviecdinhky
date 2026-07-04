// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskHistoryDao {

    @Query("SELECT * FROM task_history WHERE taskId = :taskId ORDER BY doneAt DESC")
    fun getForTask(taskId: Int): Flow<List<TaskHistory>>

    @Insert
    suspend fun insert(entry: TaskHistory)

    @Insert
    suspend fun insertAll(entries: List<TaskHistory>)

    // Keeps only the :keep most recent entries for a task
    @Query("""
        DELETE FROM task_history
        WHERE taskId = :taskId
          AND id NOT IN (
              SELECT id FROM task_history
              WHERE taskId = :taskId
              ORDER BY doneAt DESC
              LIMIT :keep
          )
    """)
    suspend fun trimForTask(taskId: Int, keep: Int)

    // Deletes the single most recent entry (used by undo)
    @Query("""
        DELETE FROM task_history
        WHERE id = (
            SELECT id FROM task_history
            WHERE taskId = :taskId
            ORDER BY doneAt DESC
            LIMIT 1
        )
    """)
    suspend fun deleteLatestForTask(taskId: Int)

    @Query("SELECT * FROM task_history ORDER BY taskId, doneAt")
    suspend fun getAll(): List<TaskHistory>

    @Query("DELETE FROM task_history WHERE taskId = :taskId")
    suspend fun deleteForTask(taskId: Int)

    @Query("DELETE FROM task_history")
    suspend fun deleteAll()
}
