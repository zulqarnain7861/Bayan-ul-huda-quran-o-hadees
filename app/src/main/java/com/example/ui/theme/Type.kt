package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

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
      fontSize = 34.sp,
      lineHeight = 48.sp,
      letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
      fontFamily = ScheherazadeFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 28.sp,
      lineHeight = 42.sp
    ),
    headlineLarge = TextStyle(
      fontFamily = ScheherazadeFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 26.sp,
      lineHeight = 38.sp
    ),
    headlineMedium = TextStyle(
      fontFamily = AmiriFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 22.sp,
      lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
      fontFamily = AmiriFontFamily,
      fontWeight = FontWeight.Normal,
      fontSize = 20.sp,
      lineHeight = 34.sp
    ),
    titleLarge = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.SemiBold,
      fontSize = 18.sp,
      lineHeight = 26.sp,
      letterSpacing = 0.15.sp
    ),
    titleMedium = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Medium,
      fontSize = 15.sp,
      lineHeight = 22.sp,
      letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
      fontFamily = AmiriFontFamily,
      fontWeight = FontWeight.Normal,
      fontSize = 17.sp,
      lineHeight = 28.sp,
      letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.SemiBold,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Medium,
      fontSize = 11.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.5.sp
    )
  )
