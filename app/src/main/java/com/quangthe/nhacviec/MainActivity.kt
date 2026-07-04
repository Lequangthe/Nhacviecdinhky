// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.quangthe.nhacviec.notification.MidnightScheduler
import com.quangthe.nhacviec.notification.NotificationHelper
import com.quangthe.nhacviec.ui.AboutScreen
import com.quangthe.nhacviec.ui.MainScreen
import com.quangthe.nhacviec.ui.SettingsScreen
import com.quangthe.nhacviec.ui.TaskActionScreen
import com.quangthe.nhacviec.ui.TaskFormScreen
import com.quangthe.nhacviec.ui.theme.AppTheme
import com.quangthe.nhacviec.ui.theme.NhacviecTheme
import com.quangthe.nhacviec.ui.theme.ThemePreference
import com.quangthe.nhacviec.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {

    // États partagés entre onCreate et onNewIntent (ouverture depuis le widget)
    private val openTaskIdState = mutableStateOf<Int?>(null)
    private val openAddState    = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createChannel(this)
        MidnightScheduler.schedule(this)

        // La permission notifications est demandée au premier ajout de tâche (cf. MainScreen)

        readIntentExtras(intent)

        setContent {
            var appTheme by remember { mutableStateOf(ThemePreference.get(applicationContext)) }
            NhacviecTheme(appTheme = appTheme) {
                val navController = rememberNavController()
                val vm: TaskViewModel = viewModel()

                // Ouverture depuis le widget — déclenchée au niveau racine,
                // donc fonctionne quel que soit l'écran courant
                val openTaskId = openTaskIdState.value
                LaunchedEffect(openTaskId) {
                    if (openTaskId != null) {
                        navController.navigate("taskAction/$openTaskId") { popUpTo("main") }
                        openTaskIdState.value = null
                    }
                }
                val openAdd = openAddState.value
                LaunchedEffect(openAdd) {
                    if (openAdd) {
                        navController.navigate("taskForm") { popUpTo("main") }
                        openAddState.value = false
                    }
                }

                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        MainScreen(
                            viewModel      = vm,
                            onOpenSettings = { navController.navigate("settings") },
                            onOpenAbout    = { navController.navigate("about") },
                            onAddTask      = { navController.navigate("taskForm") },
                            onEditTask     = { id -> navController.navigate("taskForm?taskId=$id") }
                        )
                    }
                    composable(
                        route = "taskForm?taskId={taskId}",
                        arguments = listOf(navArgument("taskId") {
                            type = NavType.IntType; defaultValue = -1
                        })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getInt("taskId") ?: -1
                        TaskFormScreen(
                            viewModel = vm,
                            taskId    = if (id >= 0) id else null,
                            onBack    = { navController.popBackStack() }
                        )
                    }
                    composable(
                        route = "taskAction/{taskId}",
                        arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getInt("taskId") ?: -1
                        TaskActionScreen(
                            viewModel = vm,
                            taskId    = id,
                            onBack    = { navController.popBackStack() },
                            onEdit    = { eid ->
                                navController.navigate("taskForm?taskId=$eid") {
                                    popUpTo("taskAction/{taskId}") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            viewModel     = vm,
                            currentTheme  = appTheme,
                            onThemeChange = { newTheme: AppTheme ->
                                ThemePreference.set(applicationContext, newTheme)
                                appTheme = newTheme
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("about") {
                        AboutScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readIntentExtras(intent)
    }

    private fun readIntentExtras(intent: Intent?) {
        intent ?: return
        intent.getIntExtra("task_id", -1).takeIf { it != -1 }?.let { openTaskIdState.value = it }
        if (intent.getBooleanExtra("open_add", false)) openAddState.value = true
    }
}
