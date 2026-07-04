// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Interests
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.quangthe.nhacviec.R

data class TaskIconOption(val key: String, val icon: ImageVector, val labelResId: Int)

// ⚠️ Les `key` sont des identifiants PERMANENTS et append-only (stockés en base + backups).
// On peut en ajouter, on peut retirer l'affichage d'une icône, mais on ne RÉUTILISE JAMAIS
// une clé existante pour un autre sens. Une clé inconnue retombe sur l'icône « Autre »
// (voir taskIcon() ci-dessous et iconResForKey() côté widget) — donc rien ne casse.
// La clé décrit le CONCEPT (ex. "health"), pas l'icône, pour pouvoir changer l'icône sans
// toucher aux données.
// L'ordre de cette liste = l'ordre d'affichage dans le sélecteur (aucun impact données).
val taskIconOptions = listOf(
    TaskIconOption("moto",     Icons.Filled.TwoWheeler,                  R.string.icon_moto),
    TaskIconOption("car",      Icons.Filled.DirectionsCar,               R.string.icon_car),
    TaskIconOption("bike",     Icons.AutoMirrored.Filled.DirectionsBike, R.string.icon_bike),
    TaskIconOption("home",     Icons.Filled.Home,                        R.string.icon_home),
    TaskIconOption("security", Icons.Filled.Security,                    R.string.icon_security),
    TaskIconOption("health",   Icons.Filled.MedicalServices,             R.string.icon_health),
    TaskIconOption("shopping", Icons.Filled.ShoppingCart,                R.string.icon_shopping),
    TaskIconOption("pets",     Icons.Filled.Pets,                        R.string.icon_pets),
    TaskIconOption("garden",   Icons.Filled.LocalFlorist,                R.string.icon_garden),
    TaskIconOption("tools",    Icons.Filled.Build,                       R.string.icon_tools),
    TaskIconOption("hobby",    Icons.Filled.Interests,                   R.string.icon_hobby),
    TaskIconOption("sport",    Icons.Filled.FitnessCenter,               R.string.icon_sport),
    TaskIconOption("admin",    Icons.Filled.Description,                 R.string.icon_admin),
    TaskIconOption("work",     Icons.Filled.Work,                        R.string.icon_work),
    TaskIconOption("task",     Icons.Filled.Grain,                       R.string.icon_other),
)

fun taskIcon(iconKey: String): ImageVector =
    taskIconOptions.find { it.key == iconKey }?.icon ?: Icons.Filled.Grain
