// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.quangthe.nhacviec.MainActivity
import com.quangthe.nhacviec.R
import com.quangthe.nhacviec.data.Task
import com.quangthe.nhacviec.data.TaskDatabase
import com.quangthe.nhacviec.data.daysRemaining
import com.quangthe.nhacviec.data.effectiveDueAt
import com.quangthe.nhacviec.data.isOneShotDone
import com.quangthe.nhacviec.ui.theme.AppTheme
import com.quangthe.nhacviec.ui.theme.ThemePreference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NhacviecWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val tasks = TaskDatabase.getInstance(context).taskDao()
            .getAllTasksOnce()
            .filter { !it.isDisabled && !it.isOneShotDone }
            .filter { !(it.recurrenceType == "ONE_SHOT" && it.targetDate == 0L) }
            .sortedBy { it.effectiveDueAt }

        val systemIsDark = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val theme  = ThemePreference.get(context)
        val isDark = systemIsDark || theme == AppTheme.NUIT_ELECTRIQUE

        provideContent { WidgetBody(tasks, isDark, theme, context) }
    }
}

@DrawableRes
private fun iconResForKey(key: String): Int = when (key) {
    "moto"     -> R.drawable.ic_widget_moto
    "car"      -> R.drawable.ic_widget_car
    "bike"     -> R.drawable.ic_widget_bike
    "home"     -> R.drawable.ic_widget_home
    "security" -> R.drawable.ic_widget_security
    "health"   -> R.drawable.ic_widget_health
    "pets"     -> R.drawable.ic_widget_pets
    "garden"   -> R.drawable.ic_widget_garden
    "shopping" -> R.drawable.ic_widget_shopping
    "work"     -> R.drawable.ic_widget_work
    "sport"    -> R.drawable.ic_widget_sport
    "hobby"    -> R.drawable.ic_widget_hobby
    "tools"    -> R.drawable.ic_widget_tools
    "admin"    -> R.drawable.ic_widget_admin
    else       -> R.drawable.ic_widget_task
}

@Composable
private fun WidgetBody(tasks: List<Task>, isDark: Boolean, theme: AppTheme, context: Context) {
    val bgColor      = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)
    val textColor    = if (isDark) Color(0xFFEEEEEE) else Color(0xFF333333)
    val titleColor   = theme.widgetTitleColor(isDark)
    val subColor     = if (isDark) Color(0xFFBBBBBB) else Color(0xFF555555)
    val dividerColor = if (isDark) Color(0xFF3A3A3A) else Color(0xFFDDDDDD)

    val openAppAction = actionStartActivity<MainActivity>()
    val addAction = androidx.glance.appwidget.action.actionStartActivity(
        Intent(context, MainActivity::class.java).apply {
            putExtra("open_add", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    )

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(bgColor))
            .padding(12.dp)
            .clickable(openAppAction)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text  = context.getString(R.string.app_name),
                style = TextStyle(color = ColorProvider(titleColor), fontSize = 18.sp, fontWeight = FontWeight.Bold),
                modifier = GlanceModifier.defaultWeight()
            )
            Image(
                provider = ImageProvider(R.drawable.ic_widget_add),
                contentDescription = context.getString(R.string.fab_new_task),
                modifier = GlanceModifier.size(22.dp).clickable(addAction),
                colorFilter = ColorFilter.tint(ColorProvider(titleColor))
            )
        }
        Spacer(modifier = GlanceModifier.height(6.dp))
        Box(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(ColorProvider(dividerColor))) {}
        Spacer(modifier = GlanceModifier.height(6.dp))
        if (tasks.isEmpty()) {
            Text(
                text  = context.getString(R.string.widget_no_tasks),
                style = TextStyle(color = ColorProvider(subColor), fontSize = 15.sp)
            )
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                items(tasks, itemId = { it.id.toLong() }) { task ->
                    val openTaskAction = actionRunCallback<OpenTaskAction>(
                        actionParametersOf(OpenTaskAction.KEY_TASK_ID to task.id)
                    )
                    WidgetTaskRow(task, textColor, isDark, openTaskAction, context)
                }
            }
        }
    }
}

@Composable
private fun WidgetTaskRow(
    task: Task,
    textColor: Color,
    isDark: Boolean,
    openApp: androidx.glance.action.Action,
    context: Context
) {
    val days = task.daysRemaining
    val dueDateLabel: String = run {
        val cal = Calendar.getInstance().apply { timeInMillis = task.effectiveDueAt }
        val today = Calendar.getInstance()
        val fmt = if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR))
            SimpleDateFormat("d MMM", Locale.getDefault())
        else
            SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        fmt.format(cal.time)
    }
    val label = when {
        days < 0  -> context.getString(R.string.widget_overdue, dueDateLabel)
        days == 0 -> context.getString(R.string.widget_today, dueDateLabel)
        days == 1 -> context.getString(R.string.widget_tomorrow, dueDateLabel)
        else      -> context.getString(R.string.widget_in_days, days, dueDateLabel)
    }
    val labelColor = when {
        days < 0  -> if (isDark) Color(0xFFEF9A9A) else Color(0xFFD32F2F)
        days <= 3 -> if (isDark) Color(0xFFFFCC80) else Color(0xFFF57C00)
        else      -> if (isDark) Color(0xFFA5D6A7) else Color(0xFF388E3C)
    }
    val subColor = if (isDark) Color(0xFFBBBBBB) else Color(0xFF555555)

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable(openApp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(iconResForKey(task.iconKey)),
            contentDescription = null,
            modifier = GlanceModifier.size(16.dp),
            colorFilter = ColorFilter.tint(ColorProvider(textColor))
        )
        Spacer(modifier = GlanceModifier.width(6.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = task.title,
                    style = TextStyle(color = ColorProvider(textColor), fontSize = 15.sp),
                    maxLines = 1,
                    modifier = GlanceModifier.defaultWeight()
                )
                if (task.note.isNotEmpty()) {
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_note),
                        contentDescription = null,
                        modifier = GlanceModifier.size(12.dp),
                        colorFilter = ColorFilter.tint(ColorProvider(subColor))
                    )
                }
            }
            Text(text = label, style = TextStyle(color = ColorProvider(labelColor), fontSize = 13.sp))
        }
    }
}
