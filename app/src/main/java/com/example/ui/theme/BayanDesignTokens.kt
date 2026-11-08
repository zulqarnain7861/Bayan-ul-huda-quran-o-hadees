package com.example.ui.theme

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Reusable design tokens, layout constraints, RTL/LTR directional helpers, and accessibility
 * touch-target modifiers for Bayan Al-Huda (Workstream A — UI Defect Elimination).
 */
object BayanDesignTokens {
  val SpaceXxs: Dp = 2.dp
  val SpaceXs: Dp = 4.dp
  val SpaceSm: Dp = 8.dp
  val SpaceMd: Dp = 12.dp
  val SpaceLg: Dp = 16.dp
  val SpaceXl: Dp = 24.dp
  val SpaceXxl: Dp = 32.dp

  val MinTouchTargetSize: Dp = 48.dp
  val AdaptiveMaxContentWidth: Dp = 720.dp
  val AdaptiveMaxDialogWidth: Dp = 640.dp

  /**
   * Wraps numeric badges, chapter/verse references (`2:255`), playback rates (`1.0x`), and
   * timestamps (`09:40`) in Unicode First-Strong / Left-to-Right Isolates (`\u2066...\u2069`)
   * so numbers and references never invert in RTL (`ar`, `ur`) layouts.
   */
  fun formatLtrReference(raw: String): String {
    if (raw.isEmpty()) return raw
    return "\u2066$raw\u2069"
  }
}

/**
 * Enforces the Android Accessibility minimum 48dp x 48dp interactive touch target.
 */
fun Modifier.bayanMinTouchTarget(): Modifier =
  this.defaultMinSize(
    minWidth = BayanDesignTokens.MinTouchTargetSize,
    minHeight = BayanDesignTokens.MinTouchTargetSize
  )

/**
 * Prevents content cards and lists from stretching excessively on tablets and foldables while
 * remaining full-width on compact handheld screens.
 */
fun Modifier.bayanAdaptiveContentWidth(
  maxWidth: Dp = BayanDesignTokens.AdaptiveMaxContentWidth
): Modifier = this
  .fillMaxWidth()
  .widthIn(max = maxWidth)

/**
 * Automatically mirrors directional navigation/chevron icons horizontally when `LayoutDirection.Rtl`
 * (`ar`, `ur`) is active.
 */
@Composable
fun Modifier.bayanMirrorInRtl(): Modifier {
  val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
  return if (isRtl) this.scale(scaleX = -1f, scaleY = 1f) else this
}
