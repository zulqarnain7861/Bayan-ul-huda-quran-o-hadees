package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyDuaItem
import com.example.data.model.DailyDuasDataProvider
import com.example.data.model.DuaCategory
import com.example.data.model.FridaySunnahChecklistItem
import com.example.data.model.NecessaryReminderSpec
import com.example.data.model.SpecialOccasionType
import com.example.data.prayer.PrayerTimeCalculator
import com.example.data.prayer.ReminderItemState
import com.example.data.prayer.SunnahRemindersUiState
import com.example.ui.audio.DuaAudioLanguageMode
import com.example.ui.audio.DuaAudioPlaybackState
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ScheherazadeFontFamily

enum class DailyDuasSectionTab(val label: String, val testTag: String) {
  MASNOON_DUAS("Masnoon Duas", "duas_tab_masnoon"),
  SPECIAL_OCCASIONS("Special Occasions", "duas_tab_special_occasions"),
  FRIDAY_AND_REMINDERS("Friday Bath & Reminders", "duas_tab_friday_reminders")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyDuasScreen(
  remindersState: SunnahRemindersUiState,
  duaAudioState: DuaAudioPlaybackState,
  showEnglish: Boolean = true,
  showUrdu: Boolean = true,
  showTransliteration: Boolean = true,
  onToggleEnglish: () -> Unit = {},
  onToggleUrdu: () -> Unit = {},
  onToggleTransliteration: () -> Unit = {},
  onPlayDuaVocal: (DailyDuaItem, Boolean) -> Unit = { _, _ -> },
  onStopDuaAudio: () -> Unit = {},
  onSelectAudioLanguageMode: (DuaAudioLanguageMode) -> Unit = {},
  onToggleAudioRepeatMode: () -> Unit = {},
  onToggleFavoriteDua: (String) -> Unit = {},
  onIncrementDuaTasbih: (String, Boolean) -> Unit = { _, _ -> },
  onUpdateFridayBathReminder: (Boolean, String) -> Unit = { _, _ -> },
  onToggleFridayChecklistItem: (String) -> Unit = {},
  onIncrementFridaySalawat: (Int) -> Unit = {},
  onUpdateNecessaryReminder: (String, Boolean, String) -> Unit = { _, _, _ -> },
  onTriggerTestReminder: (NecessaryReminderSpec) -> Unit = {},
  onOpenSurahByNumber: (Int) -> Unit = {},
  onNavigateToLearnIslam: () -> Unit = {},
  initialTab: DailyDuasSectionTab = DailyDuasSectionTab.MASNOON_DUAS,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var activeTab by remember { mutableStateOf(initialTab) }
  var searchQuery by remember { mutableStateOf("") }
  var selectedMasnoonCategory by remember { mutableStateOf<DuaCategory?>(null) }
  var showFavoritesOnly by remember { mutableStateOf(false) }
  var selectedOccasionType by remember { mutableStateOf(SpecialOccasionType.ALL) }

  val filteredMasnoonDuas = remember(
    searchQuery,
    selectedMasnoonCategory,
    showFavoritesOnly,
    remindersState.favoriteDuaIds
  ) {
    val query = searchQuery.trim()
    val categoryCandidates = DailyDuasDataProvider.allDuas.filter { dua ->
      when {
        showFavoritesOnly -> remindersState.favoriteDuaIds.contains(dua.id)
        query.isNotEmpty() && selectedMasnoonCategory == null -> true // Search across all Duas (including Safar, Iftar, etc.) when searching from Masnoon tab
        selectedMasnoonCategory == null -> dua.category != DuaCategory.SPECIAL_OCCASIONS
        else -> dua.category == selectedMasnoonCategory
      }
    }
    if (query.isEmpty()) {
      categoryCandidates
    } else {
      DailyDuasDataProvider.searchDuasFuzzy(query, categoryCandidates)
    }
  }

  val filteredOccasionDuas = remember(searchQuery, selectedOccasionType) {
    val query = searchQuery.trim()
    val occasionCandidates = DailyDuasDataProvider.allDuas.filter { dua ->
      val isOccasion = dua.category == DuaCategory.SPECIAL_OCCASIONS ||
        (selectedOccasionType != SpecialOccasionType.ALL && dua.occasionType == selectedOccasionType)
      val typeMatch = selectedOccasionType == SpecialOccasionType.ALL ||
        dua.occasionType == selectedOccasionType
      isOccasion && typeMatch
    }
    if (query.isEmpty()) {
      occasionCandidates
    } else {
      DailyDuasDataProvider.searchDuasFuzzy(query, occasionCandidates)
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("daily_duas_screen_root"),
    contentPadding = PaddingValues(vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // 1. Header Banner with Arabic Calligraphy & Section Tabs
    item {
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
          containerColor = EmeraldPrimary
        )
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Daily Masnoon Duas & Sunnah Reminders",
                style = MaterialTheme.typography.titleMedium.copy(
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Authentic Quranic & Prophetic Supplications • Special Occasions • Friday Ghusl & Daily Reminders",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color.White.copy(alpha = 0.85f)
                )
              )
            }
            Text(
              text = "الأَدْعِيَةُ المَسْنُونَة",
              style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = ScheherazadeFontFamily,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 3 Primary Section Sub-Tabs
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            DailyDuasSectionTab.entries.forEach { tab ->
              val selected = activeTab == tab
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { activeTab = tab }
                  .testTag(tab.testTag),
                shape = RoundedCornerShape(12.dp),
                color = if (selected) GoldPrimary else Color.White.copy(alpha = 0.14f)
              ) {
                Box(
                  modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = if (selected) Color(0xFF102018) else Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 11.sp
                    ),
                    textAlign = TextAlign.Center
                  )
                }
              }
            }
          }
        }
      }
    }

    // 2. Active Dua Audio Playback Status Bar (Exact Quranic Verse or Real Voice Coming Soon Notice)
    if (duaAudioState.activeDuaId != null) {
      item {
        val isComingSoon = duaAudioState.isComingSoonNotice
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("dua_audio_active_banner"),
          shape = RoundedCornerShape(16.dp),
          color = if (isComingSoon) Color(0xFF4E342E) else Color(0xFF10231B),
          tonalElevation = 4.dp
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = duaAudioState.activeDuaTitle,
                  style = MaterialTheme.typography.labelLarge.copy(
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                  )
                )
                Text(
                  text = duaAudioState.activeStageLabel,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isComingSoon) Color(0xFFFFE082) else Color.White.copy(alpha = 0.9f),
                    fontWeight = if (isComingSoon) FontWeight.Bold else FontWeight.Normal
                  )
                )
              }
              FilledTonalButton(
                onClick = onStopDuaAudio,
                colors = ButtonDefaults.filledTonalButtonColors(
                  containerColor = if (isComingSoon) Color(0xFF6D4C41) else Color(0xFFB71C1C),
                  contentColor = Color.White
                ),
                modifier = Modifier.testTag("dua_audio_stop_button")
              ) {
                Icon(
                  if (isComingSoon) Icons.Default.Clear else Icons.Default.Stop,
                  contentDescription = "Stop Dua Audio",
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isComingSoon) "Dismiss" else "Stop", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
            if (!isComingSoon) {
              Spacer(modifier = Modifier.height(8.dp))
              LinearProgressIndicator(
                progress = { duaAudioState.progressFraction },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(5.dp)
                  .clip(RoundedCornerShape(4.dp)),
                color = GoldPrimary,
                trackColor = Color.White.copy(alpha = 0.2f)
              )
            }
          }
        }
      }
    }

    // =========================================================================
    // TAB 1 & TAB 2: MASNOON DUAS & SPECIAL OCCASIONS DUAS
    // =========================================================================
    if (activeTab == DailyDuasSectionTab.MASNOON_DUAS || activeTab == DailyDuasSectionTab.SPECIAL_OCCASIONS) {
      item {
        ElevatedCard(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            OutlinedTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("duas_search_input"),
              placeholder = {
                Text(
                  text = if (activeTab == DailyDuasSectionTab.MASNOON_DUAS) {
                    "Search Masnoon Duas by title, Arabic, English, Urdu, or Hadith reference..."
                  } else {
                    "Search Special Occasion Duas (Ramadan, Iftar, Jumu'ah, Safar, Istikharah, Shifa...)"
                  },
                  fontSize = 12.sp
                )
              },
              leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search Duas")
              },
              trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                  IconButton(onClick = { searchQuery = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                  }
                }
              },
              singleLine = true,
              shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Translation & Authentic Verse Audio Controls (No AI/Synthesized Voice)
            Text(
              text = "Translation Display & Authentic Human Reciter Audio (Exact Quranic Verse Only — No AI Voice):",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              FilterChip(
                selected = showEnglish,
                onClick = onToggleEnglish,
                label = { Text("English Translation", fontSize = 11.sp) },
                modifier = Modifier.testTag("duas_toggle_english_chip")
              )
              FilterChip(
                selected = showUrdu,
                onClick = onToggleUrdu,
                label = { Text("اردو ترجمہ (Urdu)", fontSize = 11.sp) },
                modifier = Modifier.testTag("duas_toggle_urdu_chip")
              )
              FilterChip(
                selected = showTransliteration,
                onClick = onToggleTransliteration,
                label = { Text("Transliteration", fontSize = 11.sp) },
                modifier = Modifier.testTag("duas_toggle_translit_chip")
              )
              FilterChip(
                selected = duaAudioState.repeatCountTarget == 3,
                onClick = onToggleAudioRepeatMode,
                leadingIcon = {
                  Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(14.dp))
                },
                label = { Text("Repeat: ${duaAudioState.repeatCountTarget}x", fontSize = 11.sp) },
                modifier = Modifier.testTag("duas_audio_repeat_toggle")
              )
            }

            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              DuaAudioLanguageMode.entries.forEach { mode ->
                FilterChip(
                  selected = duaAudioState.languageMode == mode,
                  onClick = { onSelectAudioLanguageMode(mode) },
                  label = { Text(mode.label, fontSize = 11.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary.copy(alpha = 0.15f),
                    selectedLabelColor = EmeraldPrimary
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // Category / Occasion Filter Chips
            if (activeTab == DailyDuasSectionTab.MASNOON_DUAS) {
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                FilterChip(
                  selected = selectedMasnoonCategory == null && !showFavoritesOnly,
                  onClick = {
                    selectedMasnoonCategory = null
                    showFavoritesOnly = false
                  },
                  label = { Text("All Masnoon (${DailyDuasDataProvider.allDuas.count { it.category != DuaCategory.SPECIAL_OCCASIONS }})", fontSize = 11.sp) },
                  modifier = Modifier.testTag("duas_cat_all_masnoon")
                )
                listOf(
                  DuaCategory.MORNING_EVENING,
                  DuaCategory.SLEEP_WAKE,
                  DuaCategory.HOME_MASJID,
                  DuaCategory.QURANIC_RABBANA
                ).forEach { cat ->
                  FilterChip(
                    selected = selectedMasnoonCategory == cat && !showFavoritesOnly,
                    onClick = {
                      selectedMasnoonCategory = cat
                      showFavoritesOnly = false
                    },
                    label = { Text(cat.label, fontSize = 11.sp) },
                    modifier = Modifier.testTag("duas_cat_${cat.name.lowercase()}")
                  )
                }
                FilterChip(
                  selected = showFavoritesOnly,
                  onClick = { showFavoritesOnly = !showFavoritesOnly },
                  label = { Text("Saved Duas (${remindersState.favoriteDuaIds.size})", fontSize = 11.sp) },
                  modifier = Modifier.testTag("duas_cat_favorites")
                )
              }
            } else {
              // Special Occasions Filter Chips
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                SpecialOccasionType.entries.forEach { occasion ->
                  FilterChip(
                    selected = selectedOccasionType == occasion,
                    onClick = { selectedOccasionType = occasion },
                    label = { Text(occasion.label, fontSize = 11.sp) },
                    modifier = Modifier.testTag("duas_occasion_${occasion.name.lowercase()}")
                  )
                }
              }
            }
          }
        }
      }

      val targetList = if (activeTab == DailyDuasSectionTab.MASNOON_DUAS) {
        filteredMasnoonDuas
      } else {
        filteredOccasionDuas
      }

      if (targetList.isEmpty()) {
        item {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "No matching Duas found",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Try clearing the search query or selecting another category.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      } else {
        items(targetList, key = { it.id }) { dua ->
          val isFav = remindersState.favoriteDuaIds.contains(dua.id)
          val tasbihCount = remindersState.duaTasbihCounts[dua.id] ?: 0
          val isPlayingThisDua = duaAudioState.isPlaying && duaAudioState.activeDuaId == dua.id
          val isComingSoonThisDua = duaAudioState.isComingSoonNotice && duaAudioState.activeDuaId == dua.id

          MasnoonDuaCard(
            dua = dua,
            isFavorite = isFav,
            tasbihCount = tasbihCount,
            isPlayingThisDua = isPlayingThisDua,
            isComingSoonThisDua = isComingSoonThisDua,
            activeWordIndex = if (isPlayingThisDua) duaAudioState.activeWordIndex else -1,
            activeLetterIndexInWord = if (isPlayingThisDua) duaAudioState.activeLetterIndexInWord else -1,
            showEnglish = showEnglish,
            showUrdu = showUrdu,
            showTransliteration = showTransliteration,
            onPlayVocal = { onPlayDuaVocal(dua, true) },
            onPlayQuranReciter = { onPlayDuaVocal(dua, true) },
            onStopAudio = onStopDuaAudio,
            onToggleFavorite = { onToggleFavoriteDua(dua.id) },
            onIncrementTasbih = { reset -> onIncrementDuaTasbih(dua.id, reset) },
            onCopyDua = {
              copyDuaToClipboard(context, dua)
            },
            onShareDua = {
              shareDuaText(context, dua)
            }
          )
        }
      }
    }

    // =========================================================================
    // TAB 3: FRIDAY BATH (JUMU'AH GHUSL) REMINDER & NECESSARY REMINDERS
    // =========================================================================
    if (activeTab == DailyDuasSectionTab.FRIDAY_AND_REMINDERS) {
      // 1. Dedicated Friday Bath (Jumu'ah Ghusl) Reminder Hero Card
      item {
        val fridayBathSpec = DailyDuasDataProvider.defaultNecessaryReminders.first {
          it.id == "rem_friday_bath_ghusl"
        }
        val isFridayBathDone = remindersState.completedFridayChecklistIds.contains("friday_ghusl_bath")

        ElevatedCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("friday_bath_reminder_card"),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(42.dp)
                    .background(EmeraldPrimary.copy(alpha = 0.14f), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "Friday Ghusl Reminder",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(22.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "Friday Bath (Jumu'ah Ghusl) Reminder",
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = EmeraldPrimary
                    )
                  )
                  Text(
                    text = "غُسْلُ يَوْمِ الْجُمُعَةِ • Weekly Friday Morning Sunnah Alert",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = GoldPrimary,
                      fontWeight = FontWeight.SemiBold
                    )
                  )
                }
              }

              Switch(
                checked = remindersState.fridayBathReminderEnabled,
                onCheckedChange = { enabled ->
                  onUpdateFridayBathReminder(enabled, remindersState.fridayBathReminderTime24h)
                },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = EmeraldPrimary
                ),
                modifier = Modifier.testTag("friday_bath_reminder_switch")
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Authentic Hadith Box
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              color = EmeraldPrimary.copy(alpha = 0.08f)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = "«غُسْلُ يَوْمِ الجُمُعَةِ وَاجِبٌ عَلَى كُلِّ مُحْتَلِمٍ، وَأَنْ يَسْتَنَّ، وَأَنْ يَمَسَّ طِيبًا إِنْ وَجَدَ»",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = AmiriFontFamily,
                    fontSize = 20.sp,
                    lineHeight = 32.sp,
                    color = MaterialTheme.colorScheme.onSurface
                  ),
                  textAlign = TextAlign.End,
                  modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "\"Taking a bath (Ghusl) on Friday is essential for every adult Muslim, and that he should use a Miswak (to clean his teeth) and apply perfume if available.\"",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic
                  )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Authentic Reference: Sahih al-Bukhari 877 & 880 • Sahih Muslim 846 (Narrated by Abu Sa'id al-Khudri, may Allah be pleased with him)",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "Select Friday Morning Ghusl Reminder Time (Silent / Non-Musical Alert):",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              fridayBathSpec.timeOptions24h.forEach { timeOpt ->
                val selected = remindersState.fridayBathReminderTime24h == timeOpt
                FilterChip(
                  selected = selected,
                  onClick = {
                    onUpdateFridayBathReminder(true, timeOpt)
                  },
                  leadingIcon = {
                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp))
                  },
                  label = {
                    Text(PrayerTimeCalculator.format24hTo12h(timeOpt), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  },
                  modifier = Modifier.testTag("friday_bath_time_${timeOpt.replace(":", "")}")
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = { onToggleFridayChecklistItem("friday_ghusl_bath") },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isFridayBathDone) GoldPrimary else EmeraldPrimary
                ),
                modifier = Modifier
                  .weight(1f)
                  .testTag("friday_bath_mark_done_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (isFridayBathDone) "Ghusl Completed ✓" else "Mark Friday Ghusl Done",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              OutlinedButton(
                onClick = {
                  onTriggerTestReminder(fridayBathSpec)
                  Toast.makeText(
                    context,
                    "Friday Bath (Jumu'ah Ghusl) reminder notification sent (Silent / No Music)",
                    Toast.LENGTH_SHORT
                  ).show()
                },
                modifier = Modifier.testTag("friday_bath_test_notif_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.NotificationsActive,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Test Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // 2. Interactive Jumu'ah (Friday) Sunnah Checklist & Abundant Salawat Counter
      item {
        val totalItems = DailyDuasDataProvider.fridaySunnahChecklist.size
        val completedCount = DailyDuasDataProvider.fridaySunnahChecklist.count {
          remindersState.completedFridayChecklistIds.contains(it.id)
        }
        val progress = (completedCount.toFloat() / totalItems.toFloat()).coerceIn(0f, 1f)
        val salawatDua = DailyDuasDataProvider.allDuas.first { it.id == "dua_jumuah_salawat_ibrahimiyyah" }

        ElevatedCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("friday_sunnah_checklist_card"),
          shape = RoundedCornerShape(20.dp)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Jumu'ah Mubarak Sunnah Checklist",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = "$completedCount of $totalItems Blessed Friday Sunnahs Completed",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = GoldPrimary.copy(alpha = 0.2f)
              ) {
                Text(
                  text = "${(progress * 100).toInt()}%",
                  style = MaterialTheme.typography.labelLarge.copy(
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Bold
                  ),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
              progress = { progress },
              modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = EmeraldPrimary,
              trackColor = EmeraldPrimary.copy(alpha = 0.14f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            DailyDuasDataProvider.fridaySunnahChecklist.forEach { item ->
              val isDone = remindersState.completedFridayChecklistIds.contains(item.id)
              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { onToggleFridayChecklistItem(item.id) }
                  .testTag("friday_check_item_${item.id}"),
                shape = RoundedCornerShape(12.dp),
                color = if (isDone) {
                  EmeraldPrimary.copy(alpha = 0.10f)
                } else {
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                }
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                      contentDescription = item.title,
                      tint = if (isDone) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = item.title,
                          style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                          ),
                          modifier = Modifier.weight(1f)
                        )
                        Text(
                          text = item.arabicTitle,
                          style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = ScheherazadeFontFamily,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                          )
                        )
                      }
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      )
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = item.authenticReference,
                        style = MaterialTheme.typography.labelSmall.copy(
                          color = EmeraldPrimary,
                          fontWeight = FontWeight.SemiBold
                        )
                      )
                    }
                  }

                  if (item.actionSurahNumber != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(
                      onClick = { onOpenSurahByNumber(item.actionSurahNumber) },
                      modifier = Modifier.testTag("friday_open_surah_kahf_btn"),
                      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                      Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(15.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("Open Surah Al-Kahf (Surah 18) in Quran Reader", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Friday Abundant Salawat (Durood) Counter Box
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              color = GoldPrimary.copy(alpha = 0.14f)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "Friday Salawat (Durood) Counter",
                      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                      text = "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ • Target: 100x on Jumu'ah",
                      style = MaterialTheme.typography.labelSmall
                    )
                  }
                  Text(
                    text = "${remindersState.fridaySalawatCount} / 100",
                    style = MaterialTheme.typography.titleMedium.copy(
                      color = EmeraldPrimary,
                      fontWeight = FontWeight.ExtraBold
                    ),
                    modifier = Modifier.testTag("friday_salawat_count_label")
                  )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Button(
                    onClick = { onIncrementFridaySalawat(1) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier
                      .weight(1f)
                      .testTag("friday_salawat_plus_one_btn")
                  ) {
                    Text("+1 Salawat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }
                  FilledTonalButton(
                    onClick = { onIncrementFridaySalawat(10) },
                    modifier = Modifier.testTag("friday_salawat_plus_ten_btn")
                  ) {
                    Text("+10", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }
                  OutlinedButton(
                    onClick = { onPlayDuaVocal(salawatDua, false) },
                    modifier = Modifier.testTag("friday_salawat_play_audio_btn")
                  ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Listen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }
                  IconButton(
                    onClick = { onIncrementFridaySalawat(0) }
                  ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Salawat Counter")
                  }
                }
              }
            }
          }
        }
      }

      // 3. Necessary Daily & Weekly Sunnah Reminders Header & List
      item {
        Text(
          text = "Necessary Daily & Weekly Sunnah Reminders (Purely Non-Musical)",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          ),
          modifier = Modifier.padding(top = 6.dp)
        )
        Text(
          text = "Configure scheduled reminders for Friday Ghusl, Surah Al-Kahf, Saa'at al-Istijabah, Daily Morning/Evening Adhkar, Before-Sleep Sunnahs (Surah Al-Mulk & Ayat al-Kursi), and Sunnah Fasting.",
          style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
      }

      items(DailyDuasDataProvider.defaultNecessaryReminders, key = { it.id }) { spec ->
        val itemState = remindersState.reminderStates[spec.id] ?: ReminderItemState(
          id = spec.id,
          enabled = spec.defaultEnabled,
          time24h = spec.defaultTime24h
        )
        NecessaryReminderCard(
          spec = spec,
          state = itemState,
          onUpdateReminder = { enabled, time24 ->
            onUpdateNecessaryReminder(spec.id, enabled, time24)
          },
          onTriggerTest = {
            onTriggerTestReminder(spec)
            Toast.makeText(
              context,
              "Sent silent/non-musical reminder: ${spec.title}",
              Toast.LENGTH_SHORT
            ).show()
          },
          onOpenLinkedSurah = { surahNum ->
            onOpenSurahByNumber(surahNum)
          },
          onOpenLinkedDuaCategory = { cat ->
            if (cat == DuaCategory.SPECIAL_OCCASIONS) {
              activeTab = DailyDuasSectionTab.SPECIAL_OCCASIONS
            } else {
              selectedMasnoonCategory = cat
              showFavoritesOnly = false
              activeTab = DailyDuasSectionTab.MASNOON_DUAS
            }
          }
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MasnoonDuaCard(
  dua: DailyDuaItem,
  isFavorite: Boolean,
  tasbihCount: Int,
  isPlayingThisDua: Boolean,
  isComingSoonThisDua: Boolean = false,
  activeWordIndex: Int = -1,
  activeLetterIndexInWord: Int = -1,
  showEnglish: Boolean,
  showUrdu: Boolean,
  showTransliteration: Boolean,
  onPlayVocal: () -> Unit,
  onPlayQuranReciter: () -> Unit,
  onStopAudio: () -> Unit,
  onToggleFavorite: () -> Unit,
  onIncrementTasbih: (Boolean) -> Unit,
  onCopyDua: () -> Unit,
  onShareDua: () -> Unit
) {
  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("dua_card_${dua.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Top Row: Category / Occasion Badge + Favorite & Share
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = EmeraldPrimary.copy(alpha = 0.12f)
        ) {
          Text(
            text = dua.occasionBadge,
            style = MaterialTheme.typography.labelSmall.copy(
              color = EmeraldPrimary,
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.testTag("dua_fav_btn_${dua.id}")
          ) {
            Icon(
              imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
              contentDescription = "Save Dua",
              tint = if (isFavorite) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          IconButton(onClick = onCopyDua) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy Dua",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
          }
          IconButton(onClick = onShareDua) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share Dua",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Text(
        text = dua.title,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Sacred Arabic Text Box with Synchronized Green Word-by-Word & Letter-by-Letter Highlighting
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isPlayingThisDua) Color(0xFF00C853).copy(alpha = 0.08f) else EmeraldPrimary.copy(alpha = 0.06f)
      ) {
        val baseTextColor = MaterialTheme.colorScheme.onSurface
        val spokenGreen = Color(0xFF1B5E20)
        val activeLetterGreen = Color(0xFF00C853)
        val activeWordBg = Color(0xFF00E676).copy(alpha = 0.22f)

        val annotatedArabic = remember(dua.arabicText, isPlayingThisDua, activeWordIndex, activeLetterIndexInWord, baseTextColor) {
          val words = dua.arabicText.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
          buildAnnotatedString {
            words.forEachIndexed { idx, word ->
              if (idx > 0) append(" ")
              when {
                !isPlayingThisDua || activeWordIndex < 0 -> {
                  withStyle(SpanStyle(color = baseTextColor)) {
                    append(word)
                  }
                }
                idx < activeWordIndex -> {
                  // Already recited words highlighted in rich green
                  withStyle(SpanStyle(color = spokenGreen, fontWeight = FontWeight.Bold)) {
                    append(word)
                  }
                }
                idx == activeWordIndex -> {
                  // Active word: letter-by-letter green highlighting
                  val splitPos = (activeLetterIndexInWord + 1).coerceIn(1, word.length)
                  val activePart = word.substring(0, splitPos)
                  val remainingPart = word.substring(splitPos)
                  withStyle(
                    SpanStyle(
                      color = activeLetterGreen,
                      background = activeWordBg,
                      fontWeight = FontWeight.ExtraBold
                    )
                  ) {
                    append(activePart)
                  }
                  if (remainingPart.isNotEmpty()) {
                    withStyle(
                      SpanStyle(
                        color = spokenGreen.copy(alpha = 0.85f),
                        background = activeWordBg
                      )
                    ) {
                      append(remainingPart)
                    }
                  }
                }
                else -> {
                  withStyle(SpanStyle(color = baseTextColor)) {
                    append(word)
                  }
                }
              }
            }
          }
        }

        Text(
          text = annotatedArabic,
          style = MaterialTheme.typography.headlineSmall.copy(
            fontFamily = AmiriFontFamily,
            fontSize = 24.sp,
            lineHeight = 42.sp,
            letterSpacing = 0.sp
          ),
          textAlign = TextAlign.End,
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
            .testTag("dua_arabic_text_${dua.id}")
        )
      }

      if (showTransliteration) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = dua.transliteration,
          style = MaterialTheme.typography.bodySmall.copy(
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
      }

      if (showEnglish) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = dua.englishTranslation,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 21.sp
          )
        )
      }

      if (showUrdu) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = dua.urduTranslation,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = com.example.ui.theme.UrduNastaliqFontFamily,
            fontSize = 18.sp,
            lineHeight = 30.sp,
            color = MaterialTheme.colorScheme.onSurface
          ),
          textAlign = TextAlign.End,
          modifier = Modifier.fillMaxWidth()
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Authentic Reference & Virtue Note
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          Text(
            text = "Reference: ${dua.authenticReference}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = dua.virtueAndTiming,
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }
      }

      if (isComingSoonThisDua || dua.quranSurahAyah == null) {
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isComingSoonThisDua) Color(0xFFFFF8E1) else Color(0xFFF5F5F5),
          border = if (isComingSoonThisDua) androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary) else null,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "Real voice coming soon, In Sha Allah.",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = if (isComingSoonThisDua) FontWeight.Bold else FontWeight.Medium,
              color = if (isComingSoonThisDua) Color(0xFF5D4037) else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier
              .padding(horizontal = 10.dp, vertical = 6.dp)
              .testTag("dua_coming_soon_label_${dua.id}")
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Audio Playback & Tasbih Recitation Counter Controls
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (isPlayingThisDua) {
          Button(
            onClick = onStopAudio,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
            modifier = Modifier.testTag("dua_stop_btn_${dua.id}")
          ) {
            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Stop Audio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        } else if (dua.quranSurahAyah != null) {
          Button(
            onClick = onPlayVocal,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            modifier = Modifier.testTag("dua_play_vocal_btn_${dua.id}")
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              "Play Exact Verse (${dua.quranSurahAyah.first}:${dua.quranSurahAyah.second})",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        } else {
          OutlinedButton(
            onClick = onPlayVocal,
            modifier = Modifier.testTag("dua_play_vocal_btn_${dua.id}")
          ) {
            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Real voice coming soon, In Sha Allah.", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        if (dua.quranSurahAyah != null) {
          FilledTonalButton(
            onClick = onPlayQuranReciter,
            modifier = Modifier.testTag("dua_play_reciter_btn_${dua.id}")
          ) {
            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Quran Reciter (${dua.quranSurahAyah.first}:${dua.quranSurahAyah.second})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        OutlinedButton(
          onClick = { onIncrementTasbih(false) },
          modifier = Modifier.testTag("dua_tasbih_btn_${dua.id}")
        ) {
          Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Recited: $tasbihCount / ${dua.recommendedCount}x",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }

        if (tasbihCount > 0) {
          IconButton(
            onClick = { onIncrementTasbih(true) },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset Counter", modifier = Modifier.size(16.dp))
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NecessaryReminderCard(
  spec: NecessaryReminderSpec,
  state: ReminderItemState,
  onUpdateReminder: (Boolean, String) -> Unit,
  onTriggerTest: () -> Unit,
  onOpenLinkedSurah: (Int) -> Unit,
  onOpenLinkedDuaCategory: (DuaCategory) -> Unit
) {
  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("necessary_reminder_card_${spec.id}"),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = GoldPrimary.copy(alpha = 0.2f)
          ) {
            Text(
              text = spec.frequency.label,
              style = MaterialTheme.typography.labelSmall.copy(
                color = EmeraldPrimary,
                fontWeight = FontWeight.Bold
              ),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = spec.title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
        }

        Switch(
          checked = state.enabled,
          onCheckedChange = { enabled -> onUpdateReminder(enabled, state.time24h) },
          colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = EmeraldPrimary
          ),
          modifier = Modifier.testTag("reminder_switch_${spec.id}")
        )
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = spec.arabicSnippet,
        style = MaterialTheme.typography.titleMedium.copy(
          fontFamily = AmiriFontFamily,
          fontSize = 19.sp,
          color = EmeraldPrimary
        ),
        textAlign = TextAlign.End,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = spec.subtitle,
        style = MaterialTheme.typography.bodySmall.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "Source: ${spec.authenticReference}",
        style = MaterialTheme.typography.labelSmall.copy(
          color = EmeraldPrimary,
          fontWeight = FontWeight.SemiBold
        )
      )

      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Scheduled Time (${PrayerTimeCalculator.format24hTo12h(state.time24h)}):",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
      )
      Spacer(modifier = Modifier.height(4.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        spec.timeOptions24h.forEach { t24 ->
          FilterChip(
            selected = state.time24h == t24,
            onClick = { onUpdateReminder(true, t24) },
            label = { Text(PrayerTimeCalculator.format24hTo12h(t24), fontSize = 11.sp) }
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onTriggerTest,
          modifier = Modifier.testTag("reminder_test_btn_${spec.id}")
        ) {
          Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Test Reminder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        if (spec.linkedSurahNumber != null) {
          FilledTonalButton(
            onClick = { onOpenLinkedSurah(spec.linkedSurahNumber) },
            modifier = Modifier.testTag("reminder_open_surah_${spec.id}")
          ) {
            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Open Surah ${spec.linkedSurahNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        } else if (spec.linkedDuaCategory != null) {
          FilledTonalButton(
            onClick = { onOpenLinkedDuaCategory(spec.linkedDuaCategory) },
            modifier = Modifier.testTag("reminder_open_duas_${spec.id}")
          ) {
            Text("View Duas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

private fun copyDuaToClipboard(context: Context, dua: DailyDuaItem) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
  val formatted = buildString {
    appendLine(dua.title)
    appendLine(dua.arabicText)
    appendLine()
    appendLine("Transliteration: ${dua.transliteration}")
    appendLine("Translation: ${dua.englishTranslation}")
    appendLine("اردو: ${dua.urduTranslation}")
    appendLine("Reference: ${dua.authenticReference}")
  }
  clipboard.setPrimaryClip(ClipData.newPlainText(dua.title, formatted))
  Toast.makeText(context, "Copied: ${dua.title}", Toast.LENGTH_SHORT).show()
}

private fun shareDuaText(context: Context, dua: DailyDuaItem) {
  val formatted = buildString {
    appendLine("${dua.title} (${dua.occasionBadge})")
    appendLine()
    appendLine(dua.arabicText)
    appendLine()
    appendLine(dua.transliteration)
    appendLine()
    appendLine(dua.englishTranslation)
    appendLine()
    appendLine(dua.urduTranslation)
    appendLine()
    appendLine("Source: ${dua.authenticReference}")
  }
  val intent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_SUBJECT, dua.title)
    putExtra(Intent.EXTRA_TEXT, formatted)
  }
  runCatching {
    context.startActivity(Intent.createChooser(intent, "Share Masnoon Dua"))
  }
}
