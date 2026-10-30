package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Shared compact PlatformTextStyle and LineHeightStyle that strips deep Mushaf/Nastaliq
 * ascent/descent blank padding so Arabic and Urdu cards do not render huge vertical gaps.
 */
val CompactPlatformTextStyle = PlatformTextStyle(includeFontPadding = false)

val CompactLineHeightStyle = LineHeightStyle(
  alignment = LineHeightStyle.Alignment.Center,
  trim = LineHeightStyle.Trim.Both
)

/**
 * Authentic bundled full Arabic & Uthmani Quranic font families:
 * - ScheherazadeFontFamily: SIL Scheherazade New full Arabic calligraphy Naskh (Regular & Bold)
 * - AmiriFontFamily: Khaled Hosny's Amiri Quran (Uthmani Mushaf calligraphy) & Amiri Classical Naskh
 * - AmiriClassicalFontFamily: Full Amiri Regular & Bold Naskh typeface
 */
val ScheherazadeFontFamily = FontFamily(
  Font(R.font.scheherazade_new, FontWeight.Normal),
  Font(R.font.scheherazade_new, FontWeight.Medium),
  Font(R.font.scheherazade_new_bold, FontWeight.SemiBold),
  Font(R.font.scheherazade_new_bold, FontWeight.Bold)
)

val AmiriFontFamily = FontFamily(
  Font(R.font.amiri_quran, FontWeight.Normal),
  Font(R.font.amiri_quran, FontWeight.Medium),
  Font(R.font.amiri_quran, FontWeight.SemiBold),
  Font(R.font.amiri_quran, FontWeight.Bold)
)

val AmiriClassicalFontFamily = FontFamily(
  Font(R.font.amiri, FontWeight.Normal),
  Font(R.font.amiri, FontWeight.Medium),
  Font(R.font.amiri_bold, FontWeight.SemiBold),
  Font(R.font.amiri_bold, FontWeight.Bold)
)

val UrduNastaliqFontFamily = FontFamily(
  Font(R.font.noto_nastaliq_urdu, FontWeight.Normal),
  Font(R.font.noto_nastaliq_urdu, FontWeight.Medium),
  Font(R.font.noto_nastaliq_urdu, FontWeight.SemiBold),
  Font(R.font.noto_nastaliq_urdu, FontWeight.Bold)
)

val Typography =
  Typography(
    displayLarge = TextStyle(
      fontFamily = ScheherazadeFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 32.sp,
      lineHeight = 42.sp,
      letterSpacing = 0.sp,
      platformStyle = CompactPlatformTextStyle,
      lineHeightStyle = CompactLineHeightStyle
    ),
    displayMedium = TextStyle(
      fontFamily = ScheherazadeFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 26.sp,
      lineHeight = 36.sp,
      platformStyle = CompactPlatformTextStyle,
      lineHeightStyle = CompactLineHeightStyle
    ),
    displaySmall = TextStyle(
      fontFamily = ScheherazadeFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 22.sp,
      lineHeight = 30.sp,
      platformStyle = CompactPlatformTextStyle,
      lineHeightStyle = CompactLineHeightStyle
    ),
    headlineLarge = TextStyle(
      fontFamily = ScheherazadeFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 24.sp,
      lineHeight = 34.sp,
      platformStyle = CompactPlatformTextStyle,
      lineHeightStyle = CompactLineHeightStyle
    ),
    headlineMedium = TextStyle(
      fontFamily = AmiriFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 21.sp,
      lineHeight = 32.sp,
      platformStyle = CompactPlatformTextStyle,
      lineHeightStyle = CompactLineHeightStyle
    ),
    headlineSmall = TextStyle(
      fontFamily = AmiriFontFamily,
      fontWeight = FontWeight.Normal,
      fontSize = 19.sp,
      lineHeight = 30.sp,
      platformStyle = CompactPlatformTextStyle,
      lineHeightStyle = CompactLineHeightStyle
    ),
    titleLarge = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.SemiBold,
      fontSize = 18.sp,
      lineHeight = 24.sp,
      letterSpacing = 0.15.sp,
      platformStyle = CompactPlatformTextStyle,
      lineHeightStyle = CompactLineHeightStyle
    ),
    titleMedium = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Medium,
      fontSize = 15.sp,
      lineHeight = 21.sp,
      letterSpacing = 0.1.sp,
      platformStyle = CompactPlatformTextStyle,
      lineHeightStyle = CompactLineHeightStyle
    ),
    titleSmall = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.SemiBold,
      fontSize = 14.sp,
      lineHeight = 19.sp,
      letterSpacing = 0.1.sp,
      platformStyle = CompactPlatformTextStyle,
      lineHeightStyle = CompactLineHeightStyle
    ),
    bodyLarge = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
      lineHeight = 22.sp,
      letterSpacing = 0.15.sp,
      platformStyle = CompactPlatformTextStyle,
      lineHeightStyle = CompactLineHeightStyle
    ),
    bodyMedium = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.25.sp,
      platformStyle = CompactPlatformTextStyle,
      lineHeightStyle = CompactLineHeightStyle
    ),
    bodySmall = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp,
      lineHeight = 17.sp,
      letterSpacing = 0.2.sp,
      platformStyle = CompactPlatformTextStyle,
      lineHeightStyle = CompactLineHeightStyle
    ),
    labelLarge = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.SemiBold,
      fontSize = 14.sp,
      lineHeight = 19.sp,
      letterSpacing = 0.1.sp,
      platformStyle = CompactPlatformTextStyle,
      lineHeightStyle = CompactLineHeightStyle
    ),
    labelMedium = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Medium,
      fontSize = 12.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.3.sp,
      platformStyle = CompactPlatformTextStyle,
      lineHeightStyle = CompactLineHeightStyle
    ),
    labelSmall = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Medium,
      fontSize = 11.sp,
      lineHeight = 15.sp,
      letterSpacing = 0.4.sp,
      platformStyle = CompactPlatformTextStyle,
      lineHeightStyle = CompactLineHeightStyle
    )
  )
