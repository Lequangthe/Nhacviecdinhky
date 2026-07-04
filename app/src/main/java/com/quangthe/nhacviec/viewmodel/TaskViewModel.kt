// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quangthe.nhacviec.backup.BackupManager
import com.quangthe.nhacviec.data.Task
import com.quangthe.nhacviec.data.TaskDatabase
import com.quangthe.nhacviec.data.TaskHistory
import com.quangthe.nhacviec.data.TaskRepository
import com.quangthe.nhacviec.notification.NotificationHelper
import com.quangthe.nhacviec.notification.NotificationScheduler
import com.quangthe.nhacviec.widget.NhacviecWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UndoItem(
    val task: Task,
    val previousLastDoneAt: Long,
    val addedAt: Long = System.currentTimeMillis()
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val db          = TaskDatabase.getInstance(application)
    private val repository  = TaskRepository(db.taskDao())
    private val historyDao  = db.taskHistoryDao()

    val tasks: StateFlow<List<Task>?> = repository.allTasks
        .stateIn(
            scope          = viewModelScope,
            started        = SharingStarted.WhileSubscribed(5_000),
            initialValue   = null
        )

    init {
        viewModelScope.launch { repository.deleteStaleOneShotTasks() }
    }

    // ── Filtre catégorie (affichage seul, en mémoire) ────────────────────────
    // null = « Toutes ». Non persisté : repart à « Toutes » à froid, survit
    // rotation/navigation. N'affecte ni notifications, ni widget, ni échéances.
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    fun setCategoryFilter(iconKey: String?) { _selectedCategory.value = iconKey }

    fun historyForTask(taskId: Int): Flow<List<TaskHistory>> = historyDao.getForTask(taskId)

    suspend fun getTaskById(id: Int): Task? = repository.getById(id)

    // ── Pile d'annulation partagée (liste + page d'actions widget) ───────────
    private val _undoItems = MutableStateFlow<List<UndoItem>>(emptyList())
    val undoItems: StateFlow<List<UndoItem>> = _undoItems.asStateFlow()

    fun markDoneWithUndo(task: Task) {
        val prev = task.lastDoneAt
        markDone(task)
        _undoItems.value = listOf(UndoItem(task, prev)) + _undoItems.value
    }

    fun performUndo(item: UndoItem) {
        undoMarkDone(item.task, item.previousLastDoneAt)
        _undoItems.value = _undoItems.value - item
    }

    fun dismissUndo(item: UndoItem) {
        _undoItems.value = _undoItems.value - item
    }

    fun markDone(task: Task) {
        viewModelScope.launch {
            repository.markDone(task)
            if (task.recurrenceType != "ONE_SHOT") {
                historyDao.insert(TaskHistory(taskId = task.id, doneAt = System.currentTimeMillis()))
                historyDao.trimForTask(task.id, 6)
                val updated = task.copy(lastDoneAt = System.currentTimeMillis(), snoozedUntil = 0L)
                NotificationScheduler.scheduleForTask(getApplication(), updated)
            } else {
                NotificationScheduler.cancelForTask(getApplication(), task.id)
            }
            doUpdateWidget()
        }
    }

    fun addTask(
        title: String,
        intervalDays: Int,
        iconKey: String,
        note: String = "",
        recurrenceType: String = "DAYS",
        weekDays: Int = 0,
        monthDays: Int = 0,
        isDisabled: Boolean = false,
        targetDate: Long = 0L
    ) {
        viewModelScope.launch {
            val newTask = Task(
                title          = title,
                intervalDays   = intervalDays,
                lastDoneAt     = if (recurrenceType == "ONE_SHOT") 0L else System.currentTimeMillis(),
                iconKey        = iconKey,
                note           = note,
                recurrenceType = recurrenceType,
                weekDays       = weekDays,
                monthDays      = monthDays,
                isDisabled     = isDisabled,
                targetDate     = targetDate
            )
            val id = repository.insert(newTask)
            NotificationScheduler.scheduleForTask(getApplication(), newTask.copy(id = id.toInt()))
            doUpdateWidget()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            val taskToSave = if (task.recurrenceType != "ONE_SHOT" && task.lastDoneAt == 0L)
                task.copy(lastDoneAt = System.currentTimeMillis())
            else task
            repository.update(taskToSave)
            NotificationScheduler.scheduleForTask(getApplication(), taskToSave)
            doUpdateWidget()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
            historyDao.deleteForTask(task.id)
            NotificationScheduler.cancelForTask(getApplication(), task.id)
            doUpdateWidget()
        }
    }

    fun snooze(task: Task, days: Int) {
        viewModelScope.launch {
            repository.snooze(task, days)
            val snoozed = task.copy(snoozedUntil = System.currentTimeMillis() + days * 86_400_000L)
            NotificationScheduler.scheduleForTask(getApplication(), snoozed)
            doUpdateWidget()
        }
    }

    fun snoozeUntil(task: Task, dateMillis: Long) {
        viewModelScope.launch {
            repository.snoozeUntil(task, dateMillis)
            val snoozed = task.copy(snoozedUntil = dateMillis)
            NotificationScheduler.scheduleForTask(getApplication(), snoozed)
            doUpdateWidget()
        }
    }

    fun importTasks(json: String) {
        viewModelScope.launch {
            val backup = BackupManager.importFromJson(json)
            repository.deleteAll()
            historyDao.deleteAll()
            val idMap = mutableMapOf<Int, Int>() // originalId → newId
            backup.tasks.forEach { exportedTask ->
                val originalId = exportedTask.id
                val newId = repository.insert(exportedTask.copy(id = 0)).toInt()
                if (originalId > 0) idMap[originalId] = newId
                NotificationScheduler.scheduleForTask(getApplication(), exportedTask.copy(id = newId))
            }
            backup.history.forEach { entry ->
                val newTaskId = idMap[entry.taskId] ?: return@forEach
                historyDao.insert(entry.copy(taskId = newTaskId))
            }
            doUpdateWidget()
        }
    }

    fun rescheduleAllNotifications() {
        viewModelScope.launch {
            repository.allTasks.first().forEach { task ->
                NotificationScheduler.scheduleForTask(getApplication(), task)
            }
        }
    }

    fun undoMarkDone(task: Task, previousLastDoneAt: Long) {
        viewModelScope.launch {
            val restored = task.copy(lastDoneAt = previousLastDoneAt, snoozedUntil = 0L)
            repository.update(restored)
            if (task.recurrenceType != "ONE_SHOT") {
                historyDao.deleteLatestForTask(task.id)
            }
            NotificationScheduler.scheduleForTask(getApplication(), restored)
            doUpdateWidget()
        }
    }

    fun testNotification(task: Task) {
        NotificationHelper.showNotification(
            getApplication(), task.id, task.title, "Ceci est une notification de test"
        )
    }

    // Export helper: get all history for backup
    suspend fun getAllHistoryOnce(): List<TaskHistory> = historyDao.getAll()

    private suspend fun doUpdateWidget() {
        try {
            NhacviecWidget().updateAll(getApplication<Application>())
        } catch (e: Exception) {
            android.util.Log.e("MainTask", "Widget update failed", e)
        }
    }
}
