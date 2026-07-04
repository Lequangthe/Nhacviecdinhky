// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.quangthe.nhacviec.R
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnoozeDialog(
    onSnooze: (days: Int) -> Unit,
    onSnoozeUntil: (dateMillis: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var customMode by remember { mutableStateOf(false) }
    var customDays by remember { mutableStateOf("") }
    var pickDate   by remember { mutableStateOf(false) }

    if (pickDate) {
        // Minuit UTC d'aujourd'hui : DatePicker raisonne en UTC pour isSelectableDate.
        val todayUtc = remember {
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        val state = rememberDatePickerState(
            initialSelectedDateMillis = todayUtc + 86_400_000L,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayUtc
            }
        )
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = { state.selectedDateMillis?.let(onSnoozeUntil) },
                    enabled = state.selectedDateMillis != null
                ) { Text(stringResource(R.string.btn_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
            }
        ) { DatePicker(state = state) }
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.snooze_title)) },
        text = {
            if (customMode) {
                OutlinedTextField(
                    value = customDays,
                    onValueChange = {
                        if (it.length <= 3 && it.all(Char::isDigit)) customDays = it
                    },
                    label = { Text(stringResource(R.string.snooze_days_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        1  to R.string.snooze_1_day,
                        3  to R.string.snooze_3_days,
                        7  to R.string.snooze_7_days,
                        14 to R.string.snooze_14_days
                    ).forEach { (days, labelRes) ->
                        OutlinedButton(
                            onClick = { onSnooze(days) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(labelRes)) }
                    }
                    OutlinedButton(
                        onClick = { customMode = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.snooze_other)) }
                    OutlinedButton(
                        onClick = { pickDate = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.snooze_pick_date)) }
                }
            }
        },
        confirmButton = {
            if (customMode) {
                TextButton(
                    onClick = {
                        val d = customDays.toIntOrNull()
                        if (d != null && d > 0) onSnooze(d)
                    },
                    enabled = customDays.toIntOrNull()?.let { it > 0 } == true
                ) { Text(stringResource(R.string.btn_confirm)) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
        }
    )
}
