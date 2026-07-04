// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class NhacviecWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NhacviecWidget()
}
