package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.prayer.AppThemeMode
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ScheherazadeFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopIslamicBar(
  title: String = "Bayan al-Huda",
  arabicSubtitle: String = "بَيَانُ الهُدَى",
  themeMode: AppThemeMode = AppThemeMode.LIGHT,
  isOfflineMode: Boolean = false,
  onToggleOfflineMode: () -> Unit = {},
  onOpenQiblaClick: () -> Unit = {},
  onOpenDownloadsClick: () -> Unit = {},
  onOpenEvidenceClick: () -> Unit = {},
  onCycleThemeMode: () -> Unit = {},
  onSearchClick: () -> Unit,
  onAboutClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  TopAppBar(
    modifier = modifier,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .background(EmeraldPrimary, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = null,
            tint = GoldPrimary,
            modifier = Modifier.size(18.dp)
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        androidx.compose.foundation.layout.Column {
          Text(
            text = title,
            maxLines = 1,
            softWrap = false,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              lineHeight = 17.sp,
              color = MaterialTheme.colorScheme.primary
            )
          )
          Text(
            text = arabicSubtitle,
            maxLines = 1,
            softWrap = false,
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = ScheherazadeFontFamily,
              color = GoldPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              lineHeight = 14.sp
            )
          )
        }
        if (isOfflineMode) {
          Spacer(modifier = Modifier.width(6.dp))
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFB71C1C),
            modifier = Modifier
              .clickable { onToggleOfflineMode() }
              .testTag("top_bar_offline_mode_badge")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Offline Mode Active",
                tint = Color.White,
                modifier = Modifier.size(11.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "Offline",
                maxLines = 1,
                softWrap = false,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 9.sp
                )
              )
            }
          }
        }
      }
    },
    actions = {
      IconButton(
        onClick = onOpenQiblaClick,
        modifier = Modifier.size(30.dp).testTag("top_bar_qibla_action")
      ) {
        Icon(
          imageVector = Icons.Default.Explore,
          contentDescription = "Qibla Direction Compass",
          tint = GoldPrimary,
          modifier = Modifier.size(18.dp)
        )
      }
      IconButton(
        onClick = onOpenEvidenceClick,
        modifier = Modifier.size(30.dp).testTag("top_bar_evidence_action")
      ) {
        Icon(
          imageVector = Icons.Default.FactCheck,
          contentDescription = "Islam with Evidence",
          tint = EmeraldPrimary,
          modifier = Modifier.size(18.dp)
        )
      }
      IconButton(
        onClick = onOpenDownloadsClick,
        modifier = Modifier.size(30.dp).testTag("top_bar_downloads_action")
      ) {
        Icon(
          imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.CloudDownload,
          contentDescription = "Offline Downloads",
          tint = if (isOfflineMode) Color(0xFFB71C1C) else EmeraldPrimary,
          modifier = Modifier.size(18.dp)
        )
      }
      IconButton(
        onClick = onCycleThemeMode,
        modifier = Modifier.size(30.dp).testTag("top_bar_theme_action")
      ) {
        val icon = when (themeMode) {
          AppThemeMode.LIGHT -> Icons.Default.LightMode
          AppThemeMode.EYE_COMFORT -> Icons.Default.RemoveRedEye
          AppThemeMode.DARK, AppThemeMode.PURE_BLACK -> Icons.Default.DarkMode
          AppThemeMode.ROYAL_EMERALD, AppThemeMode.MADINAH_PARCHMENT, AppThemeMode.SAPPHIRE_BLUE -> Icons.Default.Palette
          AppThemeMode.AUTO -> Icons.Default.BrightnessAuto
        }
        Icon(
          imageVector = icon,
          contentDescription = "Appearance: ${themeMode.label}",
          tint = GoldPrimary,
          modifier = Modifier.size(18.dp)
        )
      }
      IconButton(
        onClick = onSearchClick,
        modifier = Modifier.size(30.dp).testTag("top_bar_search_action")
      ) {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "Search",
          tint = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.size(18.dp)
        )
      }
      IconButton(
        onClick = onAboutClick,
        modifier = Modifier.size(30.dp).testTag("top_bar_about_action")
      ) {
        Icon(
          imageVector = Icons.Default.Info,
          contentDescription = "About & Diagnostics",
          tint = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.size(18.dp)
        )
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  )
}
