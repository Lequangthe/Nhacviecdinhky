// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quangthe.nhacviec.R
import com.quangthe.nhacviec.data.Task
import com.quangthe.nhacviec.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun formatDate(millis: Long): String =
    SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(millis))

@Composable
fun TaskFormScreen(
    viewModel: TaskViewModel = viewModel(),
    taskId: Int? = null,
    onBack: () -> Unit = {}
) {
    var loadedTask by remember { mutableStateOf<Task?>(null) }
    var loading    by remember { mutableStateOf(taskId != null) }

    LaunchedEffect(taskId) {
        if (taskId != null) {
            loadedTask = viewModel.getTaskById(taskId)
            loading = false
        }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    TaskFormContent(viewModel = viewModel, task = loadedTask, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TaskFormContent(
    viewModel: TaskViewModel,
    task: Task?,
    onBack: () -> Unit
) {
    val context      = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboard     = LocalSoftwareKeyboardController.current
    val scrollState  = rememberScrollState()

    // Ferme le clavier AVANT de naviguer : évite la bagarre entre le callback de
    // retour de l'IME (prioritaire avec le retour prédictif) et la navigation.
    val dismissAndBack: () -> Unit = { focusManager.clearFocus(); keyboard?.hide(); onBack() }

    val weekDayOptions = remember {
        listOf(
            context.getString(R.string.day_mon) to Calendar.MONDAY,
            context.getString(R.string.day_tue) to Calendar.TUESDAY,
            context.getString(R.string.day_wed) to Calendar.WEDNESDAY,
            context.getString(R.string.day_thu) to Calendar.THURSDAY,
            context.getString(R.string.day_fri) to Calendar.FRIDAY,
            context.getString(R.string.day_sat) to Calendar.SATURDAY,
            context.getString(R.string.day_sun) to Calendar.SUNDAY
        )
    }

    var title          by remember { mutableStateOf(task?.title ?: "") }
    var intervalText   by remember { mutableStateOf(
        if (task == null || task.recurrenceType == "DAYS") task?.intervalDays?.toString() ?: ""
        else ""
    ) }
    var selectedIcon   by remember { mutableStateOf(task?.iconKey ?: "") }
    var note           by remember { mutableStateOf(task?.note ?: "") }
    var recurrenceType by remember { mutableStateOf(task?.recurrenceType ?: "DAYS") }
    var weekDays       by remember { mutableIntStateOf(task?.weekDays ?: 0) }
    var monthDays      by remember { mutableIntStateOf(task?.monthDays ?: 0) }
    var isDisabled     by remember { mutableStateOf(task?.isDisabled ?: false) }
    var oneShotHasDate by remember { mutableStateOf(task?.targetDate?.let { it > 0L } ?: false) }
    var targetDate     by remember { mutableLongStateOf(task?.targetDate ?: 0L) }
    var lastDoneDate   by remember { mutableLongStateOf(
        if (task == null || task.recurrenceType == "ONE_SHOT" || task.lastDoneAt == 0L)
            System.currentTimeMillis() else task.lastDoneAt
    ) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showLastDonePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showHistory    by remember { mutableStateOf(false) }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    // Le formulaire a-t-il été modifié par rapport à son état initial ?
    val initialLastDone = if (task == null || task.recurrenceType == "ONE_SHOT" || task.lastDoneAt == 0L)
        System.currentTimeMillis() else task.lastDoneAt
    val hasChanges = if (task == null) {
        title.isNotBlank() || intervalText.isNotBlank() || selectedIcon.isNotEmpty() ||
            note.isNotBlank() || recurrenceType != "DAYS" || weekDays != 0 || monthDays != 0 ||
            isDisabled || oneShotHasDate || targetDate != 0L || lastDoneDate != initialLastDone
    } else {
        title != task.title || selectedIcon != task.iconKey || note != task.note ||
            recurrenceType != task.recurrenceType || weekDays != task.weekDays ||
            monthDays != task.monthDays || isDisabled != task.isDisabled ||
            oneShotHasDate != (task.targetDate > 0L) || targetDate != task.targetDate ||
            (recurrenceType == "DAYS" && intervalText != task.intervalDays.toString()) ||
            (recurrenceType != "ONE_SHOT" && lastDoneDate != initialLastDone)
    }

    // Retour demandé : confirme si saisies en cours, sinon quitte directement.
    val requestBack: () -> Unit = {
        focusManager.clearFocus()
        if (hasChanges) showDiscardConfirm = true else dismissAndBack()
    }
    // Intercepte aussi le retour système / geste prédictif quand il y a des saisies.
    BackHandler(enabled = hasChanges) { focusManager.clearFocus(); showDiscardConfirm = true }

    val isValid = title.isNotBlank() && selectedIcon.isNotBlank() && when (recurrenceType) {
        "DAYS"     -> intervalText.toIntOrNull()?.let { it > 0 } == true
        "WEEKLY"   -> weekDays != 0
        "MONTHLY"  -> monthDays != 0
        "ONE_SHOT" -> !oneShotHasDate || targetDate > 0L
        else       -> false
    }

    val onSave: () -> Unit = {
        val days = when (recurrenceType) {
            "WEEKLY"   -> 7
            "MONTHLY"  -> 30
            "ONE_SHOT" -> 0
            else       -> intervalText.toInt()
        }
        if (task == null) {
            viewModel.addTask(
                title.trim(), days, selectedIcon, note.trim(),
                recurrenceType, weekDays, monthDays, isDisabled, targetDate,
                lastDoneAtMillis = if (recurrenceType == "ONE_SHOT") System.currentTimeMillis() else lastDoneDate
            )
        } else {
            viewModel.updateTask(task.copy(
                title = title.trim(), intervalDays = days, iconKey = selectedIcon, note = note.trim(),
                recurrenceType = recurrenceType, weekDays = weekDays, monthDays = monthDays,
                isDisabled = isDisabled, targetDate = targetDate,
                lastDoneAt = if (recurrenceType == "ONE_SHOT") task.lastDoneAt else lastDoneDate
            ))
        }
        dismissAndBack()
    }

    val recAnd = stringResource(R.string.rec_and)
    val recurrencePreview: String = when (recurrenceType) {
        "DAYS" -> {
            val n = intervalText.toIntOrNull()
            when {
                n == null || n <= 0 -> ""
                n == 1 -> context.getString(R.string.rec_every_day)
                else   -> context.getString(R.string.rec_every_n_days, n)
            }
        }
        "WEEKLY" -> {
            if (weekDays == 0) ""
            else {
                val names = weekDayOptions
                    .filter { (_, cal) -> (weekDays shr cal) and 1 == 1 }
                    .map { (name, _) -> name.lowercase() }
                val dayStr = when (names.size) {
                    0    -> ""
                    1    -> names[0]
                    else -> names.dropLast(1).joinToString(", ") + " $recAnd ${names.last()}"
                }
                context.getString(R.string.rec_preview_weekly, dayStr)
            }
        }
        "MONTHLY" -> {
            if (monthDays == 0) ""
            else {
                val parts = mutableListOf<String>()
                for (d in 1..31) {
                    if ((monthDays shr d) and 1 == 1)
                        parts.add(if (d == 1) context.getString(R.string.month_first_ordinal) else "$d")
                }
                if (monthDays and 1 == 1) parts.add(context.getString(R.string.month_last_day_short))
                val partStr = when (parts.size) {
                    0    -> ""
                    1    -> parts[0]
                    else -> parts.dropLast(1).joinToString(", ") + " $recAnd ${parts.last()}"
                }
                context.getString(R.string.rec_preview_monthly, partStr)
            }
        }
        "ONE_SHOT" -> when {
            !oneShotHasDate -> context.getString(R.string.rec_preview_someday)
            targetDate > 0L -> context.getString(R.string.rec_preview_oneshot_date, formatDate(targetDate))
            else            -> ""
        }
        else -> ""
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = if (targetDate > 0L) targetDate else System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { targetDate = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.btn_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.btn_cancel)) }
            }
        ) { DatePicker(state = state) }
    }

    if (showLastDonePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = lastDoneDate
        )
        DatePickerDialog(
            onDismissRequest = { showLastDonePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { lastDoneDate = it }
                    showLastDonePicker = false
                }) { Text(stringResource(R.string.btn_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showLastDonePicker = false }) { Text(stringResource(R.string.btn_cancel)) }
            }
        ) { DatePicker(state = state) }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.form_delete_title)) },
            text  = { Text(stringResource(R.string.form_delete_message, task?.title ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = { task?.let { viewModel.deleteTask(it) }; dismissAndBack() },
                    colors  = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.btn_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }

    if (showHistory && task != null) {
        TaskHistoryDialog(task = task, onDismiss = { showHistory = false }, vm = viewModel)
    }

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = { Text(stringResource(R.string.form_discard_title)) },
            text  = { Text(stringResource(R.string.form_discard_message)) },
            confirmButton = {
                TextButton(
                    onClick = { showDiscardConfirm = false; dismissAndBack() },
                    colors  = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.btn_discard)) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirm = false }) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (task == null) R.string.form_new_task else R.string.form_edit_task)) },
                navigationIcon = {
                    IconButton(onClick = requestBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                },
                actions = {
                    IconButton(onClick = onSave, enabled = isValid) {
                        Icon(Icons.Filled.Check,
                            contentDescription = stringResource(R.string.btn_confirm),
                            tint = if (isValid) MaterialTheme.colorScheme.onPrimaryContainer
                                   else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.38f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text(stringResource(R.string.form_title_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note, onValueChange = { note = it },
                label = { Text(stringResource(R.string.form_note_label)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4
            )

            // ── Récurrence ──────────────────────────────────────────────
            Text(stringResource(R.string.form_recurrence_label), style = MaterialTheme.typography.labelMedium)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            ) {
                listOf(
                    "DAYS"     to R.string.form_recurrence_days,
                    "WEEKLY"   to R.string.form_recurrence_weekly,
                    "MONTHLY"  to R.string.form_recurrence_monthly,
                    "ONE_SHOT" to R.string.form_recurrence_oneshot
                ).forEach { (key, labelRes) ->
                    val isActive = recurrenceType == key
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                focusManager.clearFocus()
                                recurrenceType = key
                                if (key == "WEEKLY")  weekDays  = 0
                                if (key == "MONTHLY") monthDays = 0
                                if (key != "ONE_SHOT") { oneShotHasDate = false; targetDate = 0L }
                            }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text  = stringResource(labelRes),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isActive) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            when (recurrenceType) {
                "DAYS" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = intervalText,
                            onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) intervalText = it },
                            label = { Text(stringResource(R.string.form_interval_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true, modifier = Modifier.weight(1f)
                        )
                        Text(stringResource(R.string.form_interval_unit),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                "WEEKLY" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        weekDayOptions.forEach { (label, calDay) ->
                            val isSelected = (weekDays shr calDay) and 1 == 1
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f).aspectRatio(1f).clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { focusManager.clearFocus(); weekDays = weekDays xor (1 shl calDay) }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                "MONTHLY" -> {
                    MonthDayGrid(selected = monthDays, onToggle = { focusManager.clearFocus(); monthDays = monthDays xor it })
                }
                "ONE_SHOT" -> {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { focusManager.clearFocus(); oneShotHasDate = false; targetDate = 0L }
                        ) {
                            RadioButton(
                                selected = !oneShotHasDate,
                                onClick  = { oneShotHasDate = false; targetDate = 0L }
                            )
                            Text(stringResource(R.string.form_oneshot_someday))
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { focusManager.clearFocus(); oneShotHasDate = true }
                        ) {
                            RadioButton(
                                selected = oneShotHasDate,
                                onClick  = { oneShotHasDate = true }
                            )
                            Text(stringResource(R.string.form_oneshot_fixed_date))
                        }
                    }
                    if (oneShotHasDate) {
                        Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                            OutlinedTextField(
                                value = if (targetDate > 0L) formatDate(targetDate) else "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.form_due_date_label)) },
                                trailingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
                        }
                    }
                }
            }

            if (recurrencePreview.isNotEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                ) {
                    Text(
                        text  = recurrencePreview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ── Lần cuối làm ─────────────────────────────────────────────
            if (recurrenceType != "ONE_SHOT") {
                Text(stringResource(R.string.form_last_done_label), style = MaterialTheme.typography.labelMedium)
                Box(modifier = Modifier.fillMaxWidth().clickable { showLastDonePicker = true }) {
                    OutlinedTextField(
                        value = formatDate(lastDoneDate),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { showLastDonePicker = true })
                }
            }

            // ── Icône ────────────────────────────────────────────────────
            Text(stringResource(R.string.form_icon_label), style = MaterialTheme.typography.labelMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 5
            ) {
                taskIconOptions.forEach { option ->
                    val isSelected = option.key == selectedIcon
                    val label = stringResource(option.labelResId)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { focusManager.clearFocus(); selectedIcon = option.key }
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = label,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text  = label,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Actions sur tâche existante ───────────────────────────────
            if (task != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.cd_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = { isDisabled = !isDisabled }) {
                        Icon(
                            imageVector = if (isDisabled) Icons.Filled.PlayCircle
                                          else Icons.Filled.PauseCircle,
                            contentDescription = stringResource(
                                if (isDisabled) R.string.cd_resume else R.string.cd_pause
                            ),
                            tint = if (isDisabled) Color(0xFFF57C00)
                                   else MaterialTheme.colorScheme.outline
                        )
                    }
                    if (task.recurrenceType != "ONE_SHOT") {
                        IconButton(onClick = { showHistory = true }) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = stringResource(R.string.cd_history),
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthDayGrid(selected: Int, onToggle: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (row in 0 until 4) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (col in 0 until 7) {
                    val day = row * 7 + col + 1
                    DayCell(
                        day = day, isSelected = (selected shr day) and 1 == 1,
                        isDimmed = false, modifier = Modifier.weight(1f)
                    ) { onToggle(1 shl day) }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(29, 30, 31).forEach { day ->
                DayCell(
                    day = day, isSelected = (selected shr day) and 1 == 1,
                    isDimmed = true, modifier = Modifier.weight(1f)
                ) { onToggle(1 shl day) }
            }
            val isLastSelected = selected and 1 == 1
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(4f).height(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isLastSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onToggle(1) }
            ) {
                Text(
                    text = stringResource(R.string.month_last_day),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = if (isLastSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DayCell(day: Int, isSelected: Boolean, isDimmed: Boolean,
                    modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f).clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
    ) {
        Text(
            text  = day.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isDimmed   -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                else       -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
