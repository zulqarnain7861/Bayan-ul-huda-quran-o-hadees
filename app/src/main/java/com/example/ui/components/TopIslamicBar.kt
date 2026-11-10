package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
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
  onOpenVideoLibraryClick: () -> Unit = {},
  onOpenLibraryTabClick: (Int) -> Unit = {},
  onOpenLearnIslamClick: () -> Unit = {},
  onOpenSehriIftarClick: () -> Unit = {},
  onOpenQiblaClick: () -> Unit = {},
  onOpenDownloadsClick: () -> Unit = {},
  onOpenEvidenceClick: () -> Unit = {},
  onCycleThemeMode: () -> Unit = {},
  onSearchClick: () -> Unit,
  onAboutClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  Column(modifier = modifier.fillMaxWidth()) {
    TopAppBar(
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
          Column {
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
        Row(
          modifier = Modifier
            .widthIn(max = 176.dp)
            .padding(end = 2.dp)
            .horizontalScroll(rememberScrollState()),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onOpenVideoLibraryClick,
            modifier = Modifier.size(30.dp).testTag("top_bar_video_library_action")
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Video Lectures Library",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }
          IconButton(
            onClick = { shareAppApkWithDriveLink(context) },
            modifier = Modifier.size(30.dp).testTag("top_bar_share_app_action")
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share Bayan al-Huda APK & Drive Updates Link",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }
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
              tint = MaterialTheme.colorScheme.primary,
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
              tint = if (isOfflineMode) Color(0xFFB71C1C) else MaterialTheme.colorScheme.primary,
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
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface
      )
    )

    // Global Quick Feature Strip: Keeps Video Library, Resume Video, Share App (APK + Link), Learn Islam,
    // 152 Books, Fikr-e-Maududi, Prophet Stories, Sahaba & Ahl al-Bayt, IslamQA, and tools visible on every main tab
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("top_bar_quick_features_strip"),
      color = MaterialTheme.colorScheme.surface
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        QuickFeaturePill(
          label = "▶ Video Library & Resume",
          isHighlighted = true,
          testTag = "top_quick_video_library_btn",
          onClick = onOpenVideoLibraryClick
        )
        QuickFeaturePill(
          label = "📲 Share App (APK + Link)",
          isHighlighted = false,
          testTag = "top_quick_share_app_btn",
          onClick = { shareAppApkWithDriveLink(context) }
        )
        QuickFeaturePill(
          label = "📥 Latest Updates (Drive)",
          isHighlighted = false,
          testTag = "top_quick_drive_updates_btn",
          onClick = { openAppUpdatesDriveFolder(context) }
        )
        QuickFeaturePill(
          label = "🎓 Learn Islam & Qaida",
          isHighlighted = false,
          testTag = "top_quick_learn_islam_btn",
          onClick = onOpenLearnIslamClick
        )
        QuickFeaturePill(
          label = "📚 152 Books",
          isHighlighted = false,
          testTag = "top_quick_books_btn",
          onClick = { onOpenLibraryTabClick(1) }
        )
        QuickFeaturePill(
          label = "🏛 History (26)",
          isHighlighted = false,
          testTag = "top_quick_history_btn",
          onClick = { onOpenLibraryTabClick(0) }
        )
        QuickFeaturePill(
          label = "📖 Fikr-e-Maududi",
          isHighlighted = false,
          testTag = "top_quick_maududi_btn",
          onClick = { onOpenLibraryTabClick(6) }
        )
        QuickFeaturePill(
          label = "🌟 25 Prophets",
          isHighlighted = false,
          testTag = "top_quick_prophets_btn",
          onClick = { onOpenLibraryTabClick(8) }
        )
        QuickFeaturePill(
          label = "🕌 Sahaba & Ahl al-Bayt",
          isHighlighted = false,
          testTag = "top_quick_sahaba_btn",
          onClick = { onOpenLibraryTabClick(10) }
        )
        QuickFeaturePill(
          label = "⚖️ IslamQA (4,203)",
          isHighlighted = false,
          testTag = "top_quick_islamqa_btn",
          onClick = { onOpenLibraryTabClick(11) }
        )
        QuickFeaturePill(
          label = "🔬 Research (4)",
          isHighlighted = false,
          testTag = "top_quick_research_btn",
          onClick = { onOpenLibraryTabClick(7) }
        )
        QuickFeaturePill(
          label = "🌙 Sehri & Iftar",
          isHighlighted = false,
          testTag = "top_quick_sehri_iftar_btn",
          onClick = onOpenSehriIftarClick
        )
      }
    }
  }
}

@Composable
private fun QuickFeaturePill(
  label: String,
  isHighlighted: Boolean,
  testTag: String,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = if (isHighlighted) EmeraldPrimary else EmeraldPrimary.copy(alpha = 0.1f),
    border = BorderStroke(
      width = 1.dp,
      color = if (isHighlighted) GoldPrimary else EmeraldPrimary.copy(alpha = 0.4f)
    ),
    modifier = Modifier
      .clickable(onClick = onClick)
      .testTag(testTag)
  ) {
    Text(
      text = label,
      maxLines = 1,
      softWrap = false,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        color = if (isHighlighted) Color.White else MaterialTheme.colorScheme.onSurface
      ),
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
  }
}
