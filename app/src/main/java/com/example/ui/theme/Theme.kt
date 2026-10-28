package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.prayer.AppThemeMode
import java.util.Calendar

private val DarkColorScheme =
  darkColorScheme(
    primary = GoldLight,
    onPrimary = EmeraldPrimaryVariant,
    primaryContainer = EmeraldPrimary,
    onPrimaryContainer = GoldContainer,
    secondary = GoldPrimary,
    onSecondary = Color.Black,
    secondaryContainer = EmeraldPrimaryVariant,
    onSecondaryContainer = GoldLight,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = GoldPrimary,
    onSecondary = Color.Black,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = OnGoldContainer,
    background = CreamBackground,
    onBackground = TextPrimary,
    surface = CreamSurface,
    onSurface = TextPrimary,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFFD6CFC1),
  )

private val EyeComfortColorScheme =
  lightColorScheme(
    primary = EyeComfortPrimary,
    onPrimary = Color(0xFFFDF6E4),
    primaryContainer = Color(0xFFD8CCB2),
    onPrimaryContainer = EyeComfortTextPrimary,
    secondary = EyeComfortSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5D2A8),
    onSecondaryContainer = EyeComfortTextPrimary,
    background = EyeComfortBackground,
    onBackground = EyeComfortTextPrimary,
    surface = EyeComfortSurface,
    onSurface = EyeComfortTextPrimary,
    surfaceVariant = EyeComfortSurfaceVariant,
    onSurfaceVariant = EyeComfortTextSecondary,
    outline = EyeComfortBorder,
  )

private val RoyalEmeraldColorScheme =
  darkColorScheme(
    primary = GoldLight,
    onPrimary = RoyalEmeraldBackground,
    primaryContainer = EmeraldLight,
    onPrimaryContainer = GoldContainer,
    secondary = GoldPrimary,
    onSecondary = Color.Black,
    secondaryContainer = RoyalEmeraldSurfaceVariant,
    onSecondaryContainer = GoldLight,
    background = RoyalEmeraldBackground,
    onBackground = RoyalEmeraldTextPrimary,
    surface = RoyalEmeraldSurface,
    onSurface = RoyalEmeraldTextPrimary,
    surfaceVariant = RoyalEmeraldSurfaceVariant,
    onSurfaceVariant = RoyalEmeraldTextSecondary,
    outline = RoyalEmeraldBorder,
  )

private val MadinahParchmentColorScheme =
  lightColorScheme(
    primary = ParchmentPrimary,
    onPrimary = Color(0xFFFFFBF2),
    primaryContainer = ParchmentSurfaceVariant,
    onPrimaryContainer = ParchmentTextPrimary,
    secondary = ParchmentSecondary,
    onSecondary = Color.White,
    secondaryContainer = EmeraldContainer,
    onSecondaryContainer = OnEmeraldContainer,
    background = ParchmentBackground,
    onBackground = ParchmentTextPrimary,
    surface = ParchmentSurface,
    onSurface = ParchmentTextPrimary,
    surfaceVariant = ParchmentSurfaceVariant,
    onSurfaceVariant = ParchmentTextSecondary,
    outline = ParchmentBorder,
  )

private val PureBlackColorScheme =
  darkColorScheme(
    primary = GoldLight,
    onPrimary = PureBlackBackground,
    primaryContainer = EmeraldPrimary,
    onPrimaryContainer = GoldContainer,
    secondary = GoldPrimary,
    onSecondary = Color.Black,
    secondaryContainer = PureBlackSurfaceVariant,
    onSecondaryContainer = GoldLight,
    background = PureBlackBackground,
    onBackground = PureBlackTextPrimary,
    surface = PureBlackSurface,
    onSurface = PureBlackTextPrimary,
    surfaceVariant = PureBlackSurfaceVariant,
    onSurfaceVariant = PureBlackTextSecondary,
    outline = PureBlackBorder,
  )

private val SapphireBlueColorScheme =
  darkColorScheme(
    primary = GoldLight,
    onPrimary = SapphireBlueBackground,
    primaryContainer = SapphireBlueSurfaceVariant,
    onPrimaryContainer = GoldContainer,
    secondary = SapphireBluePrimary,
    onSecondary = SapphireBlueBackground,
    secondaryContainer = SapphireBlueSurfaceVariant,
    onSecondaryContainer = SapphireBlueTextPrimary,
    background = SapphireBlueBackground,
    onBackground = SapphireBlueTextPrimary,
    surface = SapphireBlueSurface,
    onSurface = SapphireBlueTextPrimary,
    surfaceVariant = SapphireBlueSurfaceVariant,
    onSurfaceVariant = SapphireBlueTextSecondary,
    outline = SapphireBlueBorder,
  )

@Composable
fun MyApplicationTheme(
  themeMode: AppThemeMode = AppThemeMode.LIGHT,
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = when (themeMode) {
    AppThemeMode.LIGHT -> LightColorScheme
    AppThemeMode.DARK -> DarkColorScheme
    AppThemeMode.PURE_BLACK -> PureBlackColorScheme
    AppThemeMode.EYE_COMFORT -> EyeComfortColorScheme
    AppThemeMode.ROYAL_EMERALD -> RoyalEmeraldColorScheme
    AppThemeMode.MADINAH_PARCHMENT -> MadinahParchmentColorScheme
    AppThemeMode.SAPPHIRE_BLUE -> SapphireBlueColorScheme
    AppThemeMode.AUTO -> {
      val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
      val isNightHour = hour < 6 || hour >= 19
      if (darkTheme || isNightHour) DarkColorScheme else LightColorScheme
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
