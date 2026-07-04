// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.ui

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Fond progressif (fade) sur les bords gauche/droite d'une rangée scrollable horizontale.
 *
 * Bord-conscient : le fondu gauche n'apparaît que s'il reste du contenu à gauche
 * (`canScrollBackward`), le fondu droit que s'il en reste à droite (`canScrollForward`).
 * En butée, le bord concerné redevient net.
 *
 * Utilise un calque hors-écran (`CompositingStrategy.Offscreen`) pour que `BlendMode.DstIn`
 * gomme le contenu vers la transparence au lieu de mélanger avec l'arrière-plan.
 */
fun Modifier.horizontalFadingEdges(
    state: LazyListState,
    edgeWidth: Dp = 24.dp,
): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        val w = edgeWidth.toPx().coerceAtMost(size.width / 2f)

        if (state.canScrollBackward) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startX = 0f,
                    endX = w,
                ),
                topLeft = Offset.Zero,
                size = Size(w, size.height),
                blendMode = BlendMode.DstIn,
            )
        }
        if (state.canScrollForward) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startX = size.width - w,
                    endX = size.width,
                ),
                topLeft = Offset(size.width - w, 0f),
                size = Size(w, size.height),
                blendMode = BlendMode.DstIn,
            )
        }
    }
