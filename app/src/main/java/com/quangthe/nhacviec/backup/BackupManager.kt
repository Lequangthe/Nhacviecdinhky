// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.backup

import com.quangthe.nhacviec.data.Task
import com.quangthe.nhacviec.data.TaskHistory
import org.json.JSONArray
import org.json.JSONObject

data class BackupData(
    val tasks: List<Task>,           // task.id = ID xuất bản gốc (0 nếu trước v6)
    val history: List<TaskHistory>   // taskId = ID tác vụ xuất bản gốc
)

object BackupManager {

    private const val VERSION = 6

    fun exportToJson(tasks: List<Task>, history: List<TaskHistory>): String {
        val tasksArray = JSONArray()
        tasks.forEach { task ->
            tasksArray.put(JSONObject().apply {
                put("id",            task.id)
                put("title",         task.title)
                put("intervalDays",  task.intervalDays)
                put("lastDoneAt",    task.lastDoneAt)
                put("iconKey",       task.iconKey)
                put("snoozedUntil",  task.snoozedUntil)
                put("note",          task.note)
                put("recurrenceType",task.recurrenceType)
                put("weekDays",      task.weekDays)
                put("monthDays",     task.monthDays)
                put("isDisabled",    task.isDisabled)
                put("targetDate",    task.targetDate)
            })
        }
        val historyArray = JSONArray()
        history.forEach { entry ->
            historyArray.put(JSONObject().apply {
                put("taskId", entry.taskId)
                put("doneAt", entry.doneAt)
            })
        }
        return JSONObject().apply {
            put("version", VERSION)
            put("tasks", tasksArray)
            put("history", historyArray)
        }.toString(2)
    }

    fun importFromJson(json: String): BackupData {
        val root = JSONObject(json)
        val version = root.optInt("version", 1)
        if (version > VERSION) {
            throw IllegalArgumentException("Định dạng sao lưu không được hỗ trợ (phiên bản $version)")
        }
        val tasksArray = root.getJSONArray("tasks")
        val tasks = (0 until tasksArray.length()).map { i ->
            tasksArray.getJSONObject(i).let { obj ->
                Task(
                    id             = obj.optInt("id", 0),
                    title          = obj.getString("title"),
                    intervalDays   = obj.getInt("intervalDays"),
                    lastDoneAt     = obj.getLong("lastDoneAt"),
                    iconKey        = obj.getString("iconKey"),
                    snoozedUntil   = obj.optLong("snoozedUntil", 0L),
                    note           = obj.optString("note", ""),
                    recurrenceType = obj.optString("recurrenceType", "DAYS"),
                    weekDays       = obj.optInt("weekDays", 0),
                    monthDays      = obj.optInt("monthDays", 0),
                    isDisabled     = obj.optBoolean("isDisabled", false),
                    targetDate     = obj.optLong("targetDate", 0L)
                )
            }
        }
        val historyArray = root.optJSONArray("history")
        val history = if (historyArray != null) {
            (0 until historyArray.length()).map { i ->
                historyArray.getJSONObject(i).let { obj ->
                    TaskHistory(
                        taskId = obj.getInt("taskId"),
                        doneAt = obj.getLong("doneAt")
                    )
                }
            }
        } else emptyList()

        return BackupData(tasks, history)
    }
}
