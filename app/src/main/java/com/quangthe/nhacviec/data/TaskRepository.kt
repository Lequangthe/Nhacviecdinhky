// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepository(private val dao: TaskDao) {

    val allTasks: Flow<List<Task>> = dao.getAllTasks()
        .map { list -> list.sortedWith(compareBy({ it.sortPriority() }, { it.effectiveDueAt })) }

    suspend fun markDone(task: Task) {
        dao.update(task.copy(lastDoneAt = System.currentTimeMillis(), snoozedUntil = 0L))
    }

    suspend fun snooze(task: Task, days: Int) {
        dao.update(task.copy(snoozedUntil = System.currentTimeMillis() + days * 86_400_000L))
    }

    suspend fun snoozeUntil(task: Task, dateMillis: Long) {
        dao.update(task.copy(snoozedUntil = dateMillis))
    }

    suspend fun deleteStaleOneShotTasks() {
        dao.deleteStaleOneShotTasks(System.currentTimeMillis() - 7 * 86_400_000L)
    }

    suspend fun getById(id: Int): Task? = dao.getById(id)
    suspend fun update(task: Task)  = dao.update(task)
    suspend fun insert(task: Task): Long = dao.insert(task)
    suspend fun delete(task: Task)  = dao.delete(task)
    suspend fun deleteAll()         = dao.deleteAll()
}

// Priorité de tri :
// 0 = actives (récurrentes + ONE_SHOT date fixe non faite)
// 1 = ONE_SHOT "à faire un jour"
// 2 = en pause
// 3 = ONE_SHOT terminées (limbo 7 jours)
private fun Task.sortPriority(): Int = when {
    recurrenceType == "ONE_SHOT" && lastDoneAt > 0L    -> 3
    isDisabled                                          -> 2
    recurrenceType == "ONE_SHOT" && targetDate == 0L   -> 1
    else                                               -> 0
}
