// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.notification

import android.content.Context

object NotificationPreference {

    private const val PREF_NAME = "maintask_prefs"
    private const val KEY_HOUR    = "notification_hour"
    private const val KEY_ADVANCE = "notification_advance"

    fun getHour(context: Context): Int =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_HOUR, 11)

    fun getAdvance(context: Context): String =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ADVANCE, "J3") ?: "J3"

    fun save(context: Context, hour: Int, advance: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_HOUR, hour)
            .putString(KEY_ADVANCE, advance)
            .apply()
    }
}
