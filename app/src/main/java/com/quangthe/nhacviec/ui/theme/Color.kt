// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ── Erreur rouge fixe ────────────────────────────────────────────────────────
internal val ErrorRed     = Color(0xFFB3261E)
internal val ErrorRedDark = Color(0xFFF2B8B5)
internal val OnErrorWhite = Color(0xFFFFFFFF)
internal val OnErrorDark  = Color(0xFF601410)

// ── Azure Clair (couleur de l'icône) ─────────────────────────────────────────
internal val AzureClairLightScheme = lightColorScheme(
    primary            = Color(0xFF29B6F6),
    onPrimary          = Color(0xFFFFFFFF),
    primaryContainer   = Color(0xFFCCEEFF),
    onPrimaryContainer = Color(0xFF001E2B),
    secondary          = Color(0xFF0277BD),
    tertiary           = Color(0xFF01579B),
    error = ErrorRed, onError = OnErrorWhite
)
internal val AzureClairDarkScheme = darkColorScheme(
    primary            = Color(0xFF81D4FA),
    onPrimary          = Color(0xFF003A4F),
    primaryContainer   = Color(0xFF004D6B),
    onPrimaryContainer = Color(0xFFCCEEFF),
    secondary          = Color(0xFF4FC3F7),
    tertiary           = Color(0xFF81D4FA),
    background              = Color(0xFF0E1318),
    surface                 = Color(0xFF0E1318),
    surfaceContainerLowest  = Color(0xFF0A0F13),
    surfaceContainerLow     = Color(0xFF161D24),
    surfaceContainer        = Color(0xFF1C242B),
    surfaceContainerHigh    = Color(0xFF273039),
    surfaceContainerHighest = Color(0xFF323C45),
    error = ErrorRedDark, onError = OnErrorDark
)

// ── Cobalt Pro ────────────────────────────────────────────────────────────────
internal val CobaltProLightScheme = lightColorScheme(
    primary            = Color(0xFF0D47A1),
    onPrimary          = Color(0xFFFFFFFF),
    primaryContainer   = Color(0xFFD3E4FF),
    onPrimaryContainer = Color(0xFF001646),
    secondary          = Color(0xFF29B6F6),
    tertiary           = Color(0xFF1565C0),
    error = ErrorRed, onError = OnErrorWhite
)
internal val CobaltProDarkScheme = darkColorScheme(
    primary            = Color(0xFFADC6FF),
    onPrimary          = Color(0xFF002B75),
    primaryContainer   = Color(0xFF0D3A8E),
    onPrimaryContainer = Color(0xFFD3E4FF),
    secondary          = Color(0xFF81D4FA),
    tertiary           = Color(0xFF90CAF9),
    background              = Color(0xFF0D111A),
    surface                 = Color(0xFF0D111A),
    surfaceContainerLowest  = Color(0xFF090C14),
    surfaceContainerLow     = Color(0xFF161B26),
    surfaceContainer        = Color(0xFF1A2030),
    surfaceContainerHigh    = Color(0xFF232C3E),
    surfaceContainerHighest = Color(0xFF2D374C),
    error = ErrorRedDark, onError = OnErrorDark
)

// ── Nuit Électrique (dark uniquement) ────────────────────────────────────────
internal val NuitElectriqueScheme = darkColorScheme(
    primary            = Color(0xFF4FC3F7),
    onPrimary          = Color(0xFF00223A),
    primaryContainer   = Color(0xFF003D5C),
    onPrimaryContainer = Color(0xFFCCE8FF),
    secondary          = Color(0xFF81D4FA),
    tertiary           = Color(0xFF29B6F6),
    background              = Color(0xFF0F1923),
    surface                 = Color(0xFF0F1923),
    surfaceVariant          = Color(0xFF1E3040),
    surfaceContainerLowest  = Color(0xFF0A1219),
    surfaceContainerLow     = Color(0xFF152230),
    surfaceContainer        = Color(0xFF18262F),
    surfaceContainerHigh    = Color(0xFF22323F),
    surfaceContainerHighest = Color(0xFF2C3F4D),
    onBackground       = Color(0xFFE8F4FD),
    onSurface          = Color(0xFFE8F4FD),
    error = ErrorRedDark, onError = OnErrorDark
)

// ── Sarcelle Douce ────────────────────────────────────────────────────────────
internal val SarcelleDouceLight = lightColorScheme(
    primary            = Color(0xFF26C6DA),
    onPrimary          = Color(0xFF003740),
    primaryContainer   = Color(0xFFB2EBF2),
    onPrimaryContainer = Color(0xFF00282D),
    secondary          = Color(0xFF00838F),
    tertiary           = Color(0xFF006064),
    error = ErrorRed, onError = OnErrorWhite
)
internal val SarcelleDouceDark = darkColorScheme(
    primary            = Color(0xFF80DEEA),
    onPrimary          = Color(0xFF003640),
    primaryContainer   = Color(0xFF004F5A),
    onPrimaryContainer = Color(0xFFB2EBF2),
    secondary          = Color(0xFF4DD0E1),
    tertiary           = Color(0xFF80DEEA),
    background              = Color(0xFF0C1414),
    surface                 = Color(0xFF0C1414),
    surfaceContainerLowest  = Color(0xFF080F0F),
    surfaceContainerLow     = Color(0xFF141E1E),
    surfaceContainer        = Color(0xFF182423),
    surfaceContainerHigh    = Color(0xFF21302F),
    surfaceContainerHighest = Color(0xFF2B3C3A),
    error = ErrorRedDark, onError = OnErrorDark
)
