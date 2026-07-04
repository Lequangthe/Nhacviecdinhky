// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.ui.theme

import android.content.Context

object ThemePreference {
    const val PREF_NAME = "maintask_prefs"
    private const val KEY_THEME = "app_theme"

    fun get(context: Context): AppTheme {
        val name = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME, AppTheme.AZURE_CLAIR.name)
        return runCatching { AppTheme.valueOf(name ?: "") }.getOrDefault(AppTheme.AZURE_CLAIR)
    }

    fun set(context: Context, theme: AppTheme) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME, theme.name).apply()
    }
}
