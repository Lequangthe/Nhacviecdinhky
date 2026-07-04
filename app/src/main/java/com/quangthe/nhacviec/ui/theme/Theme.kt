// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppTheme(val labelResId: Int) {
    AZURE_CLAIR(com.quangthe.nhacviec.R.string.theme_azure_clair),
    COBALT_PRO(com.quangthe.nhacviec.R.string.theme_cobalt_pro),
    NUIT_ELECTRIQUE(com.quangthe.nhacviec.R.string.theme_nuit_electrique),
    SARCELLE_DOUCE(com.quangthe.nhacviec.R.string.theme_sarcelle_douce),
    DYNAMIQUE(com.quangthe.nhacviec.R.string.theme_dynamique);

    fun widgetTitleColor(isDark: Boolean): Color = when (this) {
        AZURE_CLAIR      -> if (isDark) Color(0xFF81D4FA) else Color(0xFF0277BD)
        COBALT_PRO       -> if (isDark) Color(0xFFADC6FF) else Color(0xFF0D47A1)
        NUIT_ELECTRIQUE  -> Color(0xFF4FC3F7)
        SARCELLE_DOUCE   -> if (isDark) Color(0xFF80DEEA) else Color(0xFF00838F)
        DYNAMIQUE        -> if (isDark) Color(0xFF81D4FA) else Color(0xFF1565C0)
    }
}

@Composable
fun NhacviecTheme(
    appTheme: AppTheme = AppTheme.AZURE_CLAIR,
    content: @Composable () -> Unit
) {
    val forceDark = appTheme == AppTheme.NUIT_ELECTRIQUE
    val darkTheme = forceDark || isSystemInDarkTheme()

    val colorScheme = when (appTheme) {
        AppTheme.DYNAMIQUE -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                val base = if (darkTheme) dynamicDarkColorScheme(context)
                           else dynamicLightColorScheme(context)
                base.copy(
                    error   = if (darkTheme) ErrorRedDark else ErrorRed,
                    onError = if (darkTheme) OnErrorDark  else OnErrorWhite
                )
            } else {
                if (darkTheme) AzureClairDarkScheme else AzureClairLightScheme
            }
        }
        AppTheme.AZURE_CLAIR     -> if (darkTheme) AzureClairDarkScheme  else AzureClairLightScheme
        AppTheme.COBALT_PRO      -> if (darkTheme) CobaltProDarkScheme   else CobaltProLightScheme
        AppTheme.NUIT_ELECTRIQUE -> NuitElectriqueScheme
        AppTheme.SARCELLE_DOUCE  -> if (darkTheme) SarcelleDouceDark     else SarcelleDouceLight
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
