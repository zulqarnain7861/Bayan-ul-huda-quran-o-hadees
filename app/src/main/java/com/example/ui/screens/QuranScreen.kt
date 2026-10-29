package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.VolumeUp
import com.example.ui.quran.TajweedPracticeDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.local.ExtendedHadithDataProvider
import com.example.data.local.OfflineQuranAssetLoader
import com.example.data.local.QulOfflineTranslationManager
import com.example.data.local.QulTranslationCatalog
import com.example.data.local.entities.AyahEntity
import com.example.data.local.entities.BookmarkEntity
import com.example.data.local.entities.HadithEntity
import com.example.data.local.entities.SurahEntity
import com.example.data.repository.TafsirRepository
import com.example.ui.components.AyahNumberBadge
import com.example.ui.components.BismillahHeader
import com.example.ui.components.RevelationTypeBadge
import com.example.ui.components.SahihGradeBadge
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ScheherazadeFontFamily
import com.example.ui.theme.UrduNastaliqFontFamily
import kotlinx.coroutines.launch

@Composable
fun QuranScreen(
  surahs: List<SurahEntity>,
  isReaderOpen: Boolean,
  selectedSurah: SurahEntity?,
  currentAyahs: List<AyahEntity>,
  bookmarks: List<BookmarkEntity>,
  showEnglish: Boolean,
  showUrdu: Boolean,
  showTransliteration: Boolean,
  arabicFontSizeSp: Float,
  translationFontSizeSp: Float,
  onOpenSurah: (SurahEntity) -> Unit,
  onCloseReader: () -> Unit,
  onNavigateAdjacentSurah: (Int) -> Unit = {},
  onToggleEnglish: () -> Unit,
  onToggleUrdu: () -> Unit,
  onToggleTransliteration: () -> Unit,
  onSetArabicFontSize: (Float) -> Unit,
  onPlayAyahRecitation: (AyahEntity, String) -> Unit,
  onBookmarkAyah: (AyahEntity, String) -> Unit,
  onAddNote: (String) -> Unit,
  onOpenOfflineDownloads: () -> Unit = {},
  allHadithsForTafsir: List<HadithEntity> = ExtendedHadithDataProvider.getExtendedHadiths(),
  isReadingMode: Boolean = false,
  onToggleReadingMode: () -> Unit = {},
  isContinuousMushafFlow: Boolean = false,
  onToggleContinuousMushafFlow: () -> Unit = {},
  prayerConfig: com.example.data.prayer.PrayerAndAlarmConfig,
  audioState: com.example.ui.audio.AudioState = com.example.ui.audio.AudioState(),
  currentTranslations: List<com.example.data.local.entities.TranslationEntity> = emptyList(),
  onSelectReciter: (com.example.ui.audio.Reciter, String) -> Unit = { _, _ -> },
  onSelectTranslation: (String, String) -> Unit = { _, _ -> },
  highlightedVerseNumber: Int? = null,
  onSaveReadingProgress: (SurahEntity, Int, String) -> Unit = { _, _, _ -> },
  onResumeLastRead: (Int, Int, String) -> Unit = { _, _, _ -> },
  onReportAyah: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val appStrings = com.example.ui.localization.LocalAppStrings.current
  var selectedTradition by remember { mutableStateOf("SUNNI") } // "SUNNI" or "SHIA"
  var showRecitersSheet by remember { mutableStateOf(false) }

  // Independent reciter choices for each tradition (Initialized from persisted config)
  var sunniSelectedReciter by remember(prayerConfig.sunniQuranReciterId) {
    mutableStateOf(com.example.ui.audio.availableReciters.find { it.id == prayerConfig.sunniQuranReciterId } 
      ?: com.example.ui.audio.availableReciters.first { it.tradition == "SUNNI" })
  }
  var shiaSelectedReciter by remember(prayerConfig.shiaQuranReciterId) {
    mutableStateOf(com.example.ui.audio.availableReciters.find { it.id == prayerConfig.shiaQuranReciterId } 
      ?: com.example.ui.audio.availableReciters.first { it.tradition == "SHIA" })
  }

  val activeReciter = if (selectedTradition == "SUNNI") sunniSelectedReciter else shiaSelectedReciter

  LaunchedEffect(activeReciter.id, selectedTradition) {
    if (audioState.reciter.id != activeReciter.id) {
      onSelectReciter(activeReciter, selectedTradition)
    }
  }

  var sunniTranslationKey by remember(prayerConfig.sunniTranslationKey) { mutableStateOf(prayerConfig.sunniTranslationKey) }
  var shiaTranslationKey by remember(prayerConfig.shiaTranslationKey) { mutableStateOf(prayerConfig.shiaTranslationKey) }
  val activeTranslationKey = if (selectedTradition == "SUNNI") sunniTranslationKey else shiaTranslationKey

  Column(modifier = modifier.fillMaxSize()) {
    // 1. Dual Entry Points: Sunni | Shia
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(
        selected = selectedTradition == "SUNNI",
        onClick = { selectedTradition = "SUNNI" },
        label = { Text(appStrings.sunniQuranTab, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = EmeraldPrimary,
          selectedLabelColor = Color.White
        ),
        modifier = Modifier.weight(1f).height(36.dp).testTag("quran_sunni_entry")
      )
      FilterChip(
        selected = selectedTradition == "SHIA",
        onClick = { selectedTradition = "SHIA" },
        label = { Text(appStrings.shiaQuranTab, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = GoldPrimary,
          selectedLabelColor = Color.Black
        ),
        modifier = Modifier.weight(1f).height(36.dp).testTag("quran_shia_entry")
      )
    }

    if (isReaderOpen && selectedSurah != null) {
      SurahReaderView(
        surah = selectedSurah,
        ayahs = currentAyahs,
        bookmarks = bookmarks,
        showEnglish = showEnglish,
        showUrdu = showUrdu,
        showTransliteration = showTransliteration,
        arabicFontSizeSp = arabicFontSizeSp,
        translationFontSizeSp = translationFontSizeSp,
        onBack = onCloseReader,
        onPreviousSurah = { onNavigateAdjacentSurah(-1) },
        onNextSurah = { onNavigateAdjacentSurah(1) },
        onToggleEnglish = onToggleEnglish,
        onToggleUrdu = onToggleUrdu,
        onToggleTransliteration = onToggleTransliteration,
        onSetArabicFontSize = onSetArabicFontSize,
        onPlayAyahRecitation = onPlayAyahRecitation,
        onBookmarkAyah = onBookmarkAyah,
        onAddNote = onAddNote,
        onReportAyah = onReportAyah,
        allHadithsForTafsir = allHadithsForTafsir,
        isReadingMode = isReadingMode,
        onToggleReadingMode = onToggleReadingMode,
        isContinuousMushafFlow = isContinuousMushafFlow,
        onToggleContinuousMushafFlow = onToggleContinuousMushafFlow,
        currentReciter = activeReciter,
        audioState = audioState,
        selectedTradition = selectedTradition,
        activeTranslationKey = activeTranslationKey,
        currentTranslations = currentTranslations,
        onSelectTranslation = { key ->
          if (selectedTradition == "SUNNI") sunniTranslationKey = key
          else shiaTranslationKey = key
          onSelectTranslation(key, selectedTradition)
        },
        onOpenRecitersSheet = { showRecitersSheet = true },
        highlightedVerseNumber = highlightedVerseNumber,
        onSaveReadingProgress = onSaveReadingProgress,
        modifier = Modifier.weight(1f)
      )
    } else {
      SurahCatalogView(
        surahs = surahs,
        onOpenSurah = onOpenSurah,
        onOpenOfflineDownloads = onOpenOfflineDownloads,
        currentReciter = activeReciter,
        selectedTradition = selectedTradition,
        onOpenRecitersSheet = { showRecitersSheet = true },
        prayerConfig = prayerConfig,
        onResumeReading = { surahNum, verseNum, trad ->
          selectedTradition = trad
          onResumeLastRead(surahNum, verseNum, trad)
        },
        modifier = Modifier.weight(1f)
      )
    }
  }

  if (showRecitersSheet) {
    QuranRecitersBottomSheet(
      currentReciter = activeReciter,
      recitersList = com.example.ui.audio.availableReciters,
      onSelectReciter = { reciter ->
        val wasPlayingSurah = (audioState.isPlaying || audioState.isBuffering) &&
          selectedSurah != null &&
          audioState.surahNumber == selectedSurah.number
        val resumeAyahNum = audioState.ayahNumber ?: 1
        if (selectedTradition == "SUNNI") sunniSelectedReciter = reciter
        else shiaSelectedReciter = reciter
        onSelectReciter(reciter, selectedTradition)
        if (wasPlayingSurah && selectedSurah != null) {
          val targetAyah = currentAyahs.firstOrNull {
            it.surahNumber == selectedSurah.number && it.verseNumber == resumeAyahNum
          } ?: currentAyahs.firstOrNull()
          if (targetAyah != null) {
            onPlayAyahRecitation(targetAyah, selectedSurah.nameEnglish)
          }
        }
        showRecitersSheet = false
      },
      onDismiss = { showRecitersSheet = false }
    )
  }
}

@Composable
fun SurahCatalogView(
  surahs: List<SurahEntity>,
  onOpenSurah: (SurahEntity) -> Unit,
  onOpenOfflineDownloads: () -> Unit = {},
  currentReciter: com.example.ui.audio.Reciter = com.example.ui.audio.availableReciters.first(),
  selectedTradition: String = "SUNNI",
  onOpenRecitersSheet: () -> Unit = {},
  prayerConfig: com.example.data.prayer.PrayerAndAlarmConfig = com.example.data.prayer.PrayerAndAlarmConfig(),
  onResumeReading: (Int, Int, String) -> Unit = { _, _, _ -> },
  modifier: Modifier = Modifier
) {
  val appStrings = com.example.ui.localization.LocalAppStrings.current
  var selectedTabIndex by remember { mutableIntStateOf(0) }
  var searchQuery by remember { mutableStateOf("") }

  val filteredSurahs = remember(surahs, searchQuery) {
    if (searchQuery.isBlank()) {
      surahs
    } else {
      surahs.filter {
        it.nameEnglish.contains(searchQuery, ignoreCase = true) ||
          it.nameArabic.contains(searchQuery) ||
          it.nameUrdu.contains(searchQuery) ||
          it.nameEnglishTranslation.contains(searchQuery, ignoreCase = true) ||
          it.number.toString() == searchQuery.trim()
      }
    }
  }

  Column(modifier = modifier.fillMaxSize()) {
    TabRow(
      selectedTabIndex = selectedTabIndex,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.primary
    ) {
      Tab(
        selected = selectedTabIndex == 0,
        onClick = { selectedTabIndex = 0 },
        text = { Text(appStrings.all114SurahsTab, fontWeight = FontWeight.Bold) }
      )
      Tab(
        selected = selectedTabIndex == 1,
        onClick = { selectedTabIndex = 1 },
        text = { Text(appStrings.juzIndexTab, fontWeight = FontWeight.Bold) }
      )
    }

    // Reading Progress Tracker Card (Resume Where You Left Off)
    val lastSurahNum = prayerConfig.lastReadSurahNumber.coerceIn(1, 114)
    val lastVerseNum = prayerConfig.lastReadVerseNumber.coerceAtLeast(1)
    val lastTotalVerses = prayerConfig.lastReadTotalVerses.coerceAtLeast(lastVerseNum)
    val progressPercent = ((lastVerseNum.toFloat() / lastTotalVerses.toFloat()) * 100f).toInt().coerceIn(1, 100)

    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)
        .clickable {
          onResumeReading(lastSurahNum, lastVerseNum, prayerConfig.lastReadTradition)
        }
        .testTag("resume_reading_card"),
      shape = RoundedCornerShape(14.dp),
      color = EmeraldPrimary.copy(alpha = 0.12f),
      border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.55f))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = appStrings.resumeReadingTitle,
            style = MaterialTheme.typography.labelSmall.copy(
              color = EmeraldPrimary,
              fontWeight = FontWeight.Bold
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${lastSurahNum}. ${prayerConfig.lastReadSurahNameEn} (${prayerConfig.lastReadSurahNameAr}) • Verse $lastVerseNum of $lastTotalVerses ($progressPercent%)",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            onResumeReading(lastSurahNum, lastVerseNum, prayerConfig.lastReadTradition)
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier.testTag("resume_reading_button")
        ) {
          Text(appStrings.resumeReadingButton, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedButton(
        onClick = onOpenRecitersSheet,
        border = BorderStroke(1.dp, EmeraldPrimary),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("quran_reciters_button")
      ) {
        Icon(
          imageVector = Icons.Default.VolumeUp,
          contentDescription = null,
          tint = EmeraldPrimary,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "${appStrings.recitersLabel} • ${currentReciter.name.substringBefore(" ")}",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = EmeraldPrimary
        )
      }
      Spacer(modifier = Modifier.weight(1f))
      Button(
        onClick = onOpenOfflineDownloads,
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("quran_download_for_offline_use_button")
      ) {
        Text(appStrings.downloadOfflineLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
    }

    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)
        .testTag("surah_catalog_search_input"),
      placeholder = { Text(appStrings.searchSurahPlaceholder) },
      singleLine = true,
      shape = RoundedCornerShape(12.dp)
    )

    if (selectedTabIndex == 0) {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(filteredSurahs, key = { it.number }) { surah ->
          SurahListItem(
            surah = surah,
            onClick = { onOpenSurah(surah) }
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items((1..30).toList(), key = { it }) { juzNumber ->
          val startingSurah = surahs.find { it.juzStart == juzNumber } ?: surahs.first()
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onOpenSurah(startingSurah) },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "$juzNumber",
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                  )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                  Text(
                    text = "Juz $juzNumber (پارہ $juzNumber)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                  )
                  Text(
                    text = "Starts at ${startingSurah.nameEnglish} (${startingSurah.nameArabic})",
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun SurahListItem(
  surah: SurahEntity,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("surah_item_${surah.number}"),
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "${surah.number}",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(
        modifier = Modifier.weight(1f)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = surah.nameEnglish,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.width(8.dp))
          RevelationTypeBadge(type = surah.revelationType)
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = "${surah.nameEnglishTranslation} • ${surah.nameUrdu}",
          style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Text(
          text = "${surah.totalVerses} Verses • Offline Ready + 14 Tafsirs (Sunni & Shia)",
          style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 11.sp
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Text(
        text = surah.nameArabic,
        style = MaterialTheme.typography.titleLarge.copy(
          fontFamily = ScheherazadeFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 24.sp,
          color = MaterialTheme.colorScheme.primary,
          textDirection = TextDirection.Rtl
        ),
        maxLines = 1,
        softWrap = false,
        textAlign = TextAlign.End
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReaderView(
  surah: SurahEntity,
  ayahs: List<AyahEntity>,
  bookmarks: List<BookmarkEntity>,
  showEnglish: Boolean,
  showUrdu: Boolean,
  showTransliteration: Boolean,
  arabicFontSizeSp: Float,
  translationFontSizeSp: Float,
  onBack: () -> Unit,
  onPreviousSurah: () -> Unit,
  onNextSurah: () -> Unit,
  onToggleEnglish: () -> Unit,
  onToggleUrdu: () -> Unit,
  onToggleTransliteration: () -> Unit,
  onSetArabicFontSize: (Float) -> Unit,
  onPlayAyahRecitation: (AyahEntity, String) -> Unit,
  onBookmarkAyah: (AyahEntity, String) -> Unit,
  onAddNote: (String) -> Unit,
  onReportAyah: (String) -> Unit = {},
  allHadithsForTafsir: List<HadithEntity>,
  isReadingMode: Boolean = false,
  onToggleReadingMode: () -> Unit = {},
  isContinuousMushafFlow: Boolean = false,
  onToggleContinuousMushafFlow: () -> Unit = {},
  currentReciter: com.example.ui.audio.Reciter = com.example.ui.audio.availableReciters.first(),
  audioState: com.example.ui.audio.AudioState = com.example.ui.audio.AudioState(),
  selectedTradition: String = "SUNNI",
  activeTranslationKey: String = "en.sahih",
  currentTranslations: List<com.example.data.local.entities.TranslationEntity> = emptyList(),
  onSelectTranslation: (String) -> Unit = {},
  onOpenRecitersSheet: () -> Unit = {},
  highlightedVerseNumber: Int? = null,
  onSaveReadingProgress: (SurahEntity, Int, String) -> Unit = { _, _, _ -> },
  modifier: Modifier = Modifier
) {
  var localReadingMode by remember(isReadingMode) { mutableStateOf(isReadingMode) }
  var localMushafFlow by remember(isContinuousMushafFlow) { mutableStateOf(isContinuousMushafFlow) }

  val toggleReadingModeInternal = {
    localReadingMode = !localReadingMode
    onToggleReadingMode()
  }
  val toggleMushafFlowInternal = {
    localMushafFlow = !localMushafFlow
    onToggleContinuousMushafFlow()
  }

  BackHandler {
    if (localReadingMode) {
      toggleReadingModeInternal()
    } else {
      onBack()
    }
  }

  val context = LocalContext.current
  val view = LocalView.current
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  // Immersive full-screen system bar control when Reading Mode is active
  DisposableEffect(localReadingMode) {
    val window = (context as? Activity)?.window
    if (window != null) {
      runCatching {
        val insetsController = WindowCompat.getInsetsController(window, view)
        if (localReadingMode) {
          insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
          insetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
          insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
      }
    }
    onDispose {
      val activityWindow = (context as? Activity)?.window
      if (activityWindow != null) {
        runCatching {
          WindowCompat.getInsetsController(activityWindow, view)
            .show(WindowInsetsCompat.Type.systemBars())
        }
      }
    }
  }

  var showSettingsSheet by remember { mutableStateOf(false) }
  var showTranslationSheet by remember { mutableStateOf(false) }
  var selectedAyahForTafsir by remember { mutableStateOf<AyahEntity?>(null) }
  var selectedAyahForTajweed by remember { mutableStateOf<AyahEntity?>(null) }
  var focusedVerseIndex by remember(surah.number) {
    mutableIntStateOf(((highlightedVerseNumber ?: 1) - 1).coerceAtLeast(0))
  }
  var verseJumpText by remember(surah.number) { mutableStateOf("") }

  // Resume reading: scroll to highlightedVerseNumber when Surah opens and ayahs are loaded
  LaunchedEffect(surah.number, highlightedVerseNumber, ayahs.isNotEmpty()) {
    if (highlightedVerseNumber != null && highlightedVerseNumber > 1 && ayahs.isNotEmpty()) {
      val idx = ayahs.indexOfFirst { it.verseNumber == highlightedVerseNumber }
      if (idx >= 0) {
        focusedVerseIndex = idx
        val headerOffset = if (surah.number != 9 && surah.number != 1) 1 else 0
        runCatching {
          listState.scrollToItem((idx + headerOffset).coerceAtLeast(0))
        }
      }
    }
  }

  // Save reading progress automatically when user scrolls through verses
  LaunchedEffect(listState.firstVisibleItemIndex, ayahs.size, surah.number, selectedTradition) {
    if (ayahs.isNotEmpty()) {
      val headerOffset = if (surah.number != 9 && surah.number != 1) 1 else 0
      val rawIndex = listState.firstVisibleItemIndex
      if (rawIndex >= headerOffset) {
        val ayahIdx = (rawIndex - headerOffset).coerceIn(0, ayahs.lastIndex)
        focusedVerseIndex = ayahIdx
        val currentVerse = ayahs[ayahIdx].verseNumber
        onSaveReadingProgress(surah, currentVerse, selectedTradition)
      }
    }
  }

  // Auto-scroll to the active Ayah as full-Surah playback advances from Ayah to Ayah
  LaunchedEffect(audioState.surahNumber, audioState.ayahNumber, audioState.isPlaying) {
    if (audioState.isPlaying && audioState.surahNumber == surah.number && audioState.ayahNumber != null) {
      val idx = ayahs.indexOfFirst { it.verseNumber == audioState.ayahNumber }
      if (idx >= 0) {
        focusedVerseIndex = idx
        onSaveReadingProgress(surah, audioState.ayahNumber, selectedTradition)
        val headerOffset = if (surah.number != 9 && surah.number != 1) 1 else 0
        runCatching {
          listState.animateScrollToItem((idx + headerOffset).coerceAtLeast(0))
        }
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .then(
        if (localReadingMode) {
          Modifier.testTag("reading_mode_immersive_container")
        } else {
          Modifier
        }
      )
  ) {
    // Standard Navigation Elements (Hidden in Reading Mode)
    AnimatedVisibility(
      visible = !localReadingMode,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Reader Toolbar with Prev / Next Surah & Reading Mode Toggle
        TopAppBar(
          title = {
            Column {
              Text(
                text = "${surah.number}. ${surah.nameEnglish} • ${surah.nameArabic}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "${surah.revelationType} • ${surah.totalVerses} Verses • Juz ${surah.juzStart}",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 10.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          },
          navigationIcon = {
            IconButton(onClick = onBack, modifier = Modifier.testTag("surah_reader_back_button")) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
          },
          actions = {
            IconButton(
              onClick = toggleReadingModeInternal,
              modifier = Modifier
                .size(48.dp)
                .testTag("reading_mode_toggle_button")
            ) {
              Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = "Enter Reading Mode",
                tint = EmeraldPrimary
              )
            }
            IconButton(
              onClick = onPreviousSurah,
              enabled = surah.number > 1,
              modifier = Modifier.testTag("prev_surah_button")
            ) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Surah")
            }
            IconButton(
              onClick = onNextSurah,
              enabled = surah.number < 114,
              modifier = Modifier.testTag("next_surah_button")
            ) {
              Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Surah")
            }
            IconButton(
              onClick = { showSettingsSheet = true },
              modifier = Modifier.testTag("reader_settings_button")
            ) {
              Icon(Icons.Default.FormatSize, contentDescription = "Font & Display Settings")
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
          )
        )

        // Quick translation toggle bar + Reading Mode chip + Verse Jump / Prev-Next Ayah controls
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            LazyRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              item {
                val isCurrentSurahPlaying = audioState.isPlaying && audioState.surahNumber == surah.number
                FilterChip(
                  selected = isCurrentSurahPlaying,
                  onClick = {
                    val targetAyah = ayahs.firstOrNull { it.surahNumber == surah.number && it.verseNumber == 1 }
                      ?: ayahs.firstOrNull()
                      ?: AyahEntity(
                        surahNumber = surah.number,
                        verseNumber = 1,
                        textArabic = "",
                        translationEnglish = "",
                        translationUrdu = "",
                        juzNumber = surah.juzStart,
                        pageNumber = 1
                      )
                    onPlayAyahRecitation(targetAyah, surah.nameEnglish)
                  },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.VolumeUp,
                      contentDescription = "Play Surah ${surah.nameEnglish}",
                      modifier = Modifier.size(16.dp)
                    )
                  },
                  label = {
                    Text(
                      text = "Play ${surah.nameEnglish} (${currentReciter.name})",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary,
                    selectedLabelColor = Color.White
                  ),
                  modifier = Modifier.testTag("play_open_surah_button")
                )
              }
              item {
                FilterChip(
                  selected = true,
                  onClick = onOpenRecitersSheet,
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.VolumeUp,
                      contentDescription = "Select Reciter",
                      modifier = Modifier.size(16.dp),
                      tint = if (selectedTradition == "SHIA") GoldPrimary else EmeraldPrimary
                    )
                  },
                  label = {
                    Text(
                      text = "Reciter: ${currentReciter.name}",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (selectedTradition == "SHIA") GoldPrimary.copy(alpha = 0.22f) else EmeraldPrimary.copy(alpha = 0.22f),
                    selectedLabelColor = if (selectedTradition == "SHIA") GoldPrimary else EmeraldPrimary
                  ),
                  modifier = Modifier.testTag("open_reciter_selector_button")
                )
              }
              item {
                val editionLabel = QulTranslationCatalog.getDisplayTitleForEditionKey(
                  editionKey = activeTranslationKey,
                  selectedTradition = selectedTradition
                )
                FilterChip(
                  selected = true,
                  onClick = { showTranslationSheet = true },
                  label = {
                    Text(
                      text = "Translation: $editionLabel",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (selectedTradition == "SHIA") GoldPrimary.copy(alpha = 0.22f) else EmeraldPrimary.copy(alpha = 0.22f),
                    selectedLabelColor = if (selectedTradition == "SHIA") GoldPrimary else EmeraldPrimary
                  ),
                  modifier = Modifier.testTag("open_translation_selector_button")
                )
              }
              item {
                FilterChip(
                  selected = showEnglish,
                  onClick = onToggleEnglish,
                  label = { Text("English", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary,
                    selectedLabelColor = Color.White
                  ),
                  modifier = Modifier.testTag("toggle_english_chip")
                )
              }
              item {
                FilterChip(
                  selected = showUrdu,
                  onClick = onToggleUrdu,
                  label = { Text("Urdu", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary,
                    selectedLabelColor = Color.White
                  ),
                  modifier = Modifier.testTag("toggle_urdu_chip")
                )
              }
              item {
                FilterChip(
                  selected = showTransliteration,
                  onClick = onToggleTransliteration,
                  label = { Text("Transliteration", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary,
                    selectedLabelColor = Color.White
                  ),
                  modifier = Modifier.testTag("toggle_transliteration_chip")
                )
              }
              item {
                FilterChip(
                  selected = localReadingMode,
                  onClick = toggleReadingModeInternal,
                  label = { Text("Reading Mode", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.MenuBook,
                      contentDescription = null,
                      modifier = Modifier.size(14.dp)
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldPrimary,
                    selectedLabelColor = Color.Black
                  ),
                  modifier = Modifier.testTag("reading_mode_chip")
                )
              }
            }

            if (ayahs.isNotEmpty()) {
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                OutlinedButton(
                  onClick = {
                    focusedVerseIndex = (focusedVerseIndex - 1).coerceAtLeast(0)
                    ayahs.getOrNull(focusedVerseIndex)?.let {
                      onSaveReadingProgress(surah, it.verseNumber, selectedTradition)
                    }
                    val headerOffset = if (surah.number != 9 && surah.number != 1) 1 else 0
                    coroutineScope.launch {
                      listState.animateScrollToItem(focusedVerseIndex + headerOffset)
                    }
                  },
                  enabled = focusedVerseIndex > 0,
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier.testTag("prev_ayah_button")
                ) {
                  Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Prev Ayah", fontSize = 11.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  OutlinedTextField(
                    value = verseJumpText,
                    onValueChange = { input ->
                      verseJumpText = input.filter { it.isDigit() }
                      val vNum = verseJumpText.toIntOrNull()
                      if (vNum != null && vNum in 1..ayahs.size) {
                        focusedVerseIndex = vNum - 1
                        onSaveReadingProgress(surah, vNum, selectedTradition)
                        val headerOffset = if (surah.number != 9 && surah.number != 1) 1 else 0
                        coroutineScope.launch {
                          listState.animateScrollToItem(focusedVerseIndex + headerOffset)
                        }
                      }
                    },
                    placeholder = { Text("Ayah # (1-${ayahs.size})", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                      .width(128.dp)
                      .height(46.dp)
                      .testTag("jump_to_ayah_input")
                  )
                }

                OutlinedButton(
                  onClick = {
                    focusedVerseIndex = (focusedVerseIndex + 1).coerceAtMost((ayahs.size - 1).coerceAtLeast(0))
                    ayahs.getOrNull(focusedVerseIndex)?.let {
                      onSaveReadingProgress(surah, it.verseNumber, selectedTradition)
                    }
                    val headerOffset = if (surah.number != 9 && surah.number != 1) 1 else 0
                    coroutineScope.launch {
                      listState.animateScrollToItem(focusedVerseIndex + headerOffset)
                    }
                  },
                  enabled = focusedVerseIndex < ayahs.size - 1,
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier.testTag("next_ayah_button")
                ) {
                  Text("Next Ayah", fontSize = 11.sp)
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                }
              }
            }
          }
        }
      }
    }

    // Minimal Floating Recitation Bar when in Full-Screen Reading Mode
    AnimatedVisibility(
      visible = localReadingMode,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.35f)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("reading_mode_header")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "${surah.nameArabic} • ${surah.nameEnglish}",
              style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = ScheherazadeFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            )
            Text(
              text = "Immersive Reading Mode • Increased Line Spacing (2.45x)",
              style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }

          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            FilterChip(
              selected = localMushafFlow,
              onClick = toggleMushafFlowInternal,
              label = {
                Text(
                  text = if (localMushafFlow) "Mushaf Page" else "Verse View",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier
                .heightIn(min = 48.dp)
                .testTag("reading_mode_mushaf_flow_toggle")
            )

            OutlinedButton(
              onClick = toggleReadingModeInternal,
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
              border = BorderStroke(1.dp, EmeraldPrimary),
              modifier = Modifier
                .heightIn(min = 48.dp)
                .testTag("exit_reading_mode_button")
            ) {
              Icon(
                imageVector = Icons.Default.CloseFullscreen,
                contentDescription = "Exit Reading Mode",
                tint = EmeraldPrimary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Exit Reading Mode",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = EmeraldPrimary
              )
            }
          }
        }
      }
    }

    // Ayah List / Full-Screen Immersive Recitation View
    val effectiveArabicFontSizeSp = if (localReadingMode) {
      (arabicFontSizeSp + 4f).coerceAtMost(42f)
    } else {
      arabicFontSizeSp
    }
    val verticalItemSpacing = if (localReadingMode) 20.dp else 12.dp
    val horizontalListPadding = if (localReadingMode) 16.dp else 12.dp

    // Pre-index bookmarks and translations in O(1) maps outside LazyColumn for fast 60fps scrolling
    val bookmarkedKeysSet = remember(bookmarks) {
      bookmarks.mapTo(HashSet(bookmarks.size.coerceAtLeast(4))) { it.itemKey }
    }
    val activeQulSpec = remember(activeTranslationKey) {
      QulTranslationCatalog.findEditionByKey(activeTranslationKey)
    }
    val enOverrideByVerse = remember(currentTranslations, activeTranslationKey, surah.number) {
      val map = HashMap<Int, String>()
      for (t in currentTranslations) {
        if (t.surahOrBookNumber == surah.number &&
          t.editionKey == activeTranslationKey &&
          t.languageCode == "en"
        ) {
          map[t.verseOrHadithNumber] = t.text
        }
      }
      map
    }
    val urOverrideByVerse = remember(currentTranslations, activeTranslationKey, surah.number) {
      val map = HashMap<Int, String>()
      for (t in currentTranslations) {
        if (t.surahOrBookNumber == surah.number &&
          t.editionKey == activeTranslationKey &&
          t.languageCode == "ur"
        ) {
          map[t.verseOrHadithNumber] = t.text
        }
      }
      map
    }
    val qulTextByVerse = remember(currentTranslations, activeTranslationKey, surah.number) {
      val map = HashMap<Int, String>()
      for (t in currentTranslations) {
        if (t.surahOrBookNumber == surah.number && t.editionKey == activeTranslationKey) {
          map[t.verseOrHadithNumber] = t.text
        }
      }
      map
    }

    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = horizontalListPadding)
        .testTag("surah_ayah_lazy_column"),
      contentPadding = PaddingValues(
        top = if (localReadingMode) 20.dp else 12.dp,
        bottom = if (localReadingMode) 112.dp else 96.dp
      ),
      verticalArrangement = Arrangement.spacedBy(verticalItemSpacing)
    ) {
      if (surah.number != 9 && surah.number != 1) {
        item {
          BismillahHeader()
        }
      }

      if (ayahs.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              CircularProgressIndicator(color = EmeraldPrimary)
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Loading offline verses for Surah ${surah.nameEnglish} (${surah.nameArabic})...",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            }
          }
        }
      } else if (localReadingMode && localMushafFlow) {
        // Continuous Mushaf Page Recitation Flow (all verses flowing with generous 2.45x line spacing)
        item {
          ContinuousMushafRecitationCard(
            surah = surah,
            ayahs = ayahs,
            arabicFontSizeSp = effectiveArabicFontSizeSp,
            audioState = audioState,
            onPlayAyah = { ayah -> onPlayAyahRecitation(ayah, surah.nameEnglish) }
          )
        }
      } else {
        items(ayahs, key = { "${it.surahNumber}_${it.verseNumber}" }) { ayah ->
          val isBookmarked = bookmarkedKeysSet.contains("ayah_${ayah.surahNumber}_${ayah.verseNumber}")
          val enOverride = enOverrideByVerse[ayah.verseNumber]
          val urOverride = urOverrideByVerse[ayah.verseNumber]
          val qulVerseText = if (activeQulSpec != null) {
            qulTextByVerse[ayah.verseNumber] ?: remember(activeTranslationKey, ayah.surahNumber, ayah.verseNumber) {
              QulOfflineTranslationManager.getOfflineVerseText(
                context = context,
                editionKey = activeTranslationKey,
                surahNumber = ayah.surahNumber,
                verseNumber = ayah.verseNumber
              )
            }
          } else null

          AyahCardItem(
            ayah = ayah,
            surahName = surah.nameEnglish,
            isBookmarked = isBookmarked,
            showEnglish = showEnglish,
            showUrdu = showUrdu,
            showTransliteration = showTransliteration,
            arabicFontSizeSp = effectiveArabicFontSizeSp,
            translationFontSizeSp = translationFontSizeSp,
            onPlay = { onPlayAyahRecitation(ayah, surah.nameEnglish) },
            onTajweedPractice = { selectedAyahForTajweed = ayah },
            onOpenTafsir = { selectedAyahForTafsir = ayah },
            onOpenReciterSelector = onOpenRecitersSheet,
            onBookmark = { onBookmarkAyah(ayah, surah.nameEnglish) },
            onCopy = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val enText = enOverride ?: ayah.translationEnglish
              val urText = urOverride ?: ayah.translationUrdu
              val qulPart = if (!qulVerseText.isNullOrBlank() && activeQulSpec != null) {
                "\n\n[${activeQulSpec.languageNameNative} — ${activeQulSpec.translatorName}]\n$qulVerseText"
              } else ""
              val text = "${ayah.textArabic}\n\n${ayah.transliteration}\n\n$enText\n\n$urText$qulPart\n\n[${surah.nameEnglish} ${ayah.surahNumber}:${ayah.verseNumber}]"
              clipboard.setPrimaryClip(ClipData.newPlainText("Ayah", text))
              Toast.makeText(context, "Ayah copied to clipboard", Toast.LENGTH_SHORT).show()
            },
            onAddNote = { onAddNote("${surah.nameEnglish} ${ayah.surahNumber}:${ayah.verseNumber}") },
            onReport = { onReportAyah("Surah ${surah.nameEnglish} (${ayah.surahNumber}:${ayah.verseNumber}) [$activeTranslationKey]") },
            isReadingMode = localReadingMode,
            englishTranslationOverride = enOverride,
            urduTranslationOverride = urOverride,
            activeQulEdition = activeQulSpec,
            qulTranslationText = qulVerseText,
            audioState = audioState,
            currentReciter = currentReciter,
            selectedTradition = selectedTradition
          )
        }

        // Surah Footer with Previous / Next Surah buttons (Hidden in Reading Mode)
        if (!localReadingMode) {
          item {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("surah_footer_navigation"),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              OutlinedButton(
                onClick = onPreviousSurah,
                enabled = surah.number > 1
              ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Surah ${(surah.number - 1).coerceAtLeast(1)}")
              }
              Button(
                onClick = onNextSurah,
                enabled = surah.number < 114,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
              ) {
                Text("Surah ${(surah.number + 1).coerceAtMost(114)}")
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
              }
            }
          }
        }
      }
    }
  }

  // 8-Tafsir & Linked Hadith Modal Bottom Sheet
  selectedAyahForTafsir?.let { activeAyah ->
    TafsirModalSheet(
      ayah = activeAyah,
      surah = surah,
      allHadiths = allHadithsForTafsir,
      hasPrevAyah = activeAyah.verseNumber > 1,
      hasNextAyah = activeAyah.verseNumber < ayahs.size,
      selectedTradition = selectedTradition,
      onPrevAyahTafsir = {
        ayahs.find { it.verseNumber == activeAyah.verseNumber - 1 }?.let {
          selectedAyahForTafsir = it
        }
      },
      onNextAyahTafsir = {
        ayahs.find { it.verseNumber == activeAyah.verseNumber + 1 }?.let {
          selectedAyahForTafsir = it
        }
      },
      onDismiss = { selectedAyahForTafsir = null }
    )
  }

  // Tajweed Practice Dialog (Voice Recording & Pronunciation Check)
  selectedAyahForTajweed?.let { targetAyah ->
    val cleanArabic = OfflineQuranAssetLoader.stripDuplicateBismillahPrefix(
      surahNumber = targetAyah.surahNumber,
      verseNumber = targetAyah.verseNumber,
      rawArabic = targetAyah.textArabic
    )
    TajweedPracticeDialog(
      ayah = targetAyah,
      surah = surah,
      cleanArabicText = cleanArabic,
      currentReciterName = currentReciter.name,
      audioState = audioState,
      onPlayAuthenticReciter = {
        onPlayAyahRecitation(targetAyah, surah.nameEnglish)
      },
      onDismiss = { selectedAyahForTajweed = null }
    )
  }

  // Font & Display Bottom Sheet
  if (showSettingsSheet) {
    ModalBottomSheet(
      onDismissRequest = { showSettingsSheet = false }
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 16.dp)
      ) {
        Text(
          text = "Reader Settings",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Arabic Font Size: ${arabicFontSizeSp.toInt()} sp",
          style = MaterialTheme.typography.titleSmall
        )
        Slider(
          value = arabicFontSizeSp,
          onValueChange = onSetArabicFontSize,
          valueRange = 18f..38f,
          modifier = Modifier.fillMaxWidth()
        )

        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Text(
            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontFamily = AmiriFontFamily,
              fontSize = arabicFontSizeSp.sp,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(16.dp)
          )
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  if (showTranslationSheet) {
    QuranTranslationBottomSheet(
      selectedTradition = selectedTradition,
      activeTranslationKey = activeTranslationKey,
      onSelectTranslation = { key ->
        onSelectTranslation(key)
        showTranslationSheet = false
      },
      onDismiss = { showTranslationSheet = false }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranTranslationBottomSheet(
  selectedTradition: String,
  activeTranslationKey: String,
  onSelectTranslation: (String) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val editions = if (selectedTradition == "SHIA") {
    com.example.data.local.ShiaTranslationDataProvider.shiaEditions
  } else {
    com.example.data.local.ShiaTranslationDataProvider.sunniEditions
  }

  val isCurrentKeyQul = remember(activeTranslationKey) {
    QulTranslationCatalog.findEditionByKey(activeTranslationKey) != null
  }
  var selectedSheetTab by remember(isCurrentKeyQul) {
    mutableIntStateOf(if (isCurrentKeyQul) 1 else 0)
  }
  var languageSearchQuery by remember { mutableStateOf("") }
  var expandedLanguageEn by remember(activeTranslationKey) {
    mutableStateOf(
      QulTranslationCatalog.findEditionByKey(activeTranslationKey)?.languageNameEn ?: "Persian (Farsi)"
    )
  }
  var downloadingEditionKey by remember { mutableStateOf<String?>(null) }
  var downloadedRefreshTick by remember { mutableIntStateOf(0) }

  val filteredLanguageGroups = remember(languageSearchQuery) {
    val q = languageSearchQuery.trim()
    if (q.isEmpty()) {
      QulTranslationCatalog.languageGroups
    } else {
      QulTranslationCatalog.languageGroups.filter { group ->
        group.languageNameEn.contains(q, ignoreCase = true) ||
          group.languageNameNative.contains(q, ignoreCase = true) ||
          group.languageCode.equals(q, ignoreCase = true) ||
          group.editions.any { it.translatorName.contains(q, ignoreCase = true) }
      }
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 12.dp)
        .testTag("quran_translation_modal_sheet")
    ) {
      Text(
        text = "Select Quran Translation • Language & Translator",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = if (selectedTradition == "SHIA") GoldPrimary else EmeraldPrimary
        )
      )
      Text(
        text = "Preserves Sunni (8) & Shia (5) English/Urdu editions + 73 World Languages (QUL & Afar Offline)",
        style = MaterialTheme.typography.bodySmall.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      )
      Spacer(modifier = Modifier.height(10.dp))

      TabRow(
        selectedTabIndex = selectedSheetTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = if (selectedTradition == "SHIA") GoldPrimary else EmeraldPrimary
      ) {
        Tab(
          selected = selectedSheetTab == 0,
          onClick = { selectedSheetTab = 0 },
          text = {
            Text(
              text = if (selectedTradition == "SHIA") "Shia (EN/UR • ${editions.size})" else "Sunni (EN/UR • ${editions.size})",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          },
          modifier = Modifier.testTag("translation_tab_tradition")
        )
        Tab(
          selected = selectedSheetTab == 1,
          onClick = { selectedSheetTab = 1 },
          text = {
            Text(
              text = "73 Languages (QUL Offline)",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          },
          modifier = Modifier.testTag("translation_tab_qul_languages")
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (selectedSheetTab == 0) {
        editions.forEach { edition ->
          val isSelected = edition.key == activeTranslationKey
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .clickable {
                onSelectTranslation(edition.key)
                onDismiss()
              }
              .testTag("translation_option_${edition.key}"),
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) {
              (if (selectedTradition == "SHIA") GoldPrimary else EmeraldPrimary).copy(alpha = 0.16f)
            } else {
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            },
            border = if (isSelected) {
              BorderStroke(1.5.dp, if (selectedTradition == "SHIA") GoldPrimary else EmeraldPrimary)
            } else null
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = edition.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                  ) {
                    Text(
                      text = edition.languageCode.uppercase(),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                      ),
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Translator: ${edition.translator}",
                  style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                  )
                )
                Text(
                  text = edition.description,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
              }
              if (isSelected) {
                Text(
                  text = "Active",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (selectedTradition == "SHIA") GoldPrimary else EmeraldPrimary
                  ),
                  modifier = Modifier.padding(start = 8.dp)
                )
              }
            }
          }
        }
      } else {
        // Tab 1: Language -> Translator Selector for all 73 Languages (72 QUL + Afar, plus Dagbani reported missing)
        val deviceDefaultKey = remember(selectedTradition) {
          QulTranslationCatalog.resolveDefaultEditionForLocale(tradition = selectedTradition)
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "73 Languages • 127 Ayah-by-Ayah Editions",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary
            )
          )
          OutlinedButton(
            onClick = {
              onSelectTranslation(deviceDefaultKey)
              onDismiss()
            },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.testTag("use_device_locale_translation_button")
          ) {
            Text("Use Device Locale", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = languageSearchQuery,
          onValueChange = { languageSearchQuery = it },
          placeholder = {
            Text(
              "Search language (e.g. हिन्दी, کٲشُر, فارسی, Indonesian, Turkish, French) or translator...",
              fontSize = 12.sp
            )
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("translation_language_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Priority Languages Quick-Select Strip (Hindi, Kashmiri, and Major International Languages)
        if (languageSearchQuery.isBlank()) {
          val priorityQuickEditions = remember {
            listOf(
              Triple("हिन्दी (Hindi)", "Maulana Azizul Haque al-Umari", "qul.hi.122"),
              Triple("کٲشُر (Kashmiri)", "Maulana Muhammad Yusuf Shah (Perso-Arabic)", "qul.ks.273"),
              Triple("Bahasa Indonesia", "King Fahd Complex", "qul.id.33"),
              Triple("Bahasa Melayu", "Abdullah Muhammad Basmeih", "qul.ms.39"),
              Triple("Türkçe (Turkish)", "Diyanet İşleri", "qul.tr.77"),
              Triple("فارسی (Persian)", "Hussein Taji Kal Dari", "qul.fa.135"),
              Triple("پښتو (Pashto)", "Zakaria Abulsalam", "qul.ps.118"),
              Triple("Français (French)", "Muhammad Hamidullah", "qul.fr.31"),
              Triple("Español (Spanish)", "Sheikh Isa Garcia", "qul.es.83"),
              Triple("Deutsch (German)", "Frank Bubenheim & Nadeem", "qul.de.27"),
              Triple("Русский (Russian)", "Elmir Kuliev", "qul.ru.45"),
              Triple("中文 (Chinese)", "Ma Jian", "qul.zh.56"),
              Triple("বাংলা (Bengali)", "Dr. Abu Bakr Muhammad Zakaria", "qul.bn.213")
            )
          }

          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("priority_translations_card"),
            shape = RoundedCornerShape(12.dp),
            color = GoldPrimary.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "Priority & International Quran Translations (Offline Verified)",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
              Text(
                text = "Tap to immediately activate verified Hindi (हिन्दी), Kashmiri (کٲشُر), or international Quran translations:",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              )
              Spacer(modifier = Modifier.height(6.dp))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                priorityQuickEditions.forEach { (langLabel, translatorLabel, edKey) ->
                  val isActivePriority = activeTranslationKey == edKey
                  FilterChip(
                    selected = isActivePriority,
                    onClick = {
                      onSelectTranslation(edKey)
                      onDismiss()
                    },
                    label = {
                      Text(
                        text = "$langLabel • $translatorLabel",
                        fontSize = 11.sp,
                        fontWeight = if (isActivePriority) FontWeight.Bold else FontWeight.Medium
                      )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = EmeraldPrimary,
                      selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("priority_translation_chip_$edKey")
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
        }

        filteredLanguageGroups.forEach { group ->
          val isExpanded = expandedLanguageEn == group.languageNameEn || languageSearchQuery.isNotBlank()
          val hasActiveEdition = group.editions.any { it.editionKey == activeTranslationKey }

          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .clickable(enabled = !group.isMissingInSource) {
                expandedLanguageEn = if (expandedLanguageEn == group.languageNameEn) "" else group.languageNameEn
              }
              .testTag("language_group_${group.languageCode}"),
            shape = RoundedCornerShape(12.dp),
            color = when {
              group.isMissingInSource -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.28f)
              hasActiveEdition -> EmeraldPrimary.copy(alpha = 0.14f)
              else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            },
            border = if (hasActiveEdition) BorderStroke(1.5.dp, EmeraldPrimary) else null
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = "${group.languageNameNative} — ${group.languageNameEn}",
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (group.isRtl) {
                      Spacer(modifier = Modifier.width(6.dp))
                      Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = GoldPrimary.copy(alpha = 0.22f)
                      ) {
                        Text(
                          text = "RTL",
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                          ),
                          modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                      }
                    }
                  }
                  if (group.isMissingInSource) {
                    Text(
                      text = group.missingReason ?: "Missing in QUL and fawazahmed0/quran-api",
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                      )
                    )
                  } else {
                    Text(
                      text = "${group.editions.size} Translator(s) • Offline Bundled + Full 6,236 Ayahs",
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    )
                  }
                }

                if (!group.isMissingInSource) {
                  Text(
                    text = if (isExpanded) "▲" else "▼",
                    style = MaterialTheme.typography.labelMedium.copy(
                      color = EmeraldPrimary,
                      fontWeight = FontWeight.Bold
                    )
                  )
                }
              }

              if (isExpanded && !group.isMissingInSource) {
                Spacer(modifier = Modifier.height(8.dp))
                group.editions.forEach { ed ->
                  val isSelected = ed.editionKey == activeTranslationKey
                  val isFullDownloaded = remember(ed.editionKey, downloadedRefreshTick) {
                    QulOfflineTranslationManager.isFull6236PackDownloaded(context, ed.editionKey)
                  }
                  Surface(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 3.dp)
                      .clickable {
                        onSelectTranslation(ed.editionKey)
                        onDismiss()
                      }
                      .testTag("translation_option_${ed.editionKey}"),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) EmeraldPrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                      width = if (isSelected) 1.5.dp else 0.5.dp,
                      color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant
                    )
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = ed.translatorName,
                          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                          text = "Source: ${ed.source} • Format: ${ed.format} • ${ed.ayahCount} Ayahs",
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                        )
                        Text(
                          text = if (isFullDownloaded) {
                            "✓ Complete 6,236-Ayah Pack Cached Offline"
                          } else {
                            "✓ Core Surahs Offline • Auto-caches Full 6,236 Pack"
                          },
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                          )
                        )
                      }

                      Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isFullDownloaded) {
                          OutlinedButton(
                            onClick = {
                              if (downloadingEditionKey == null) {
                                downloadingEditionKey = ed.editionKey
                                coroutineScope.launch {
                                  val ok = QulOfflineTranslationManager.downloadFullEditionForOffline(
                                    context = context,
                                    editionKey = ed.editionKey
                                  )
                                  downloadingEditionKey = null
                                  downloadedRefreshTick++
                                  Toast.makeText(
                                    context,
                                    if (ok) "Cached full 6,236 verses for ${ed.translatorName}"
                                    else "Using bundled offline pack for ${ed.translatorName}",
                                    Toast.LENGTH_SHORT
                                  ).show()
                                }
                              }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("download_qul_pack_${ed.editionKey}")
                          ) {
                            Text(
                              text = if (downloadingEditionKey == ed.editionKey) "Syncing..." else "Full 6236",
                              fontSize = 10.sp
                            )
                          }
                          Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                          text = if (isSelected) "Active" else "Select",
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.primary
                          )
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafsirModalSheet(
  ayah: AyahEntity,
  surah: SurahEntity,
  allHadiths: List<HadithEntity>,
  hasPrevAyah: Boolean,
  hasNextAyah: Boolean,
  selectedTradition: String = "SUNNI",
  onPrevAyahTafsir: () -> Unit,
  onNextAyahTafsir: () -> Unit,
  onDismiss: () -> Unit
) {
  val filteredEditions = remember(selectedTradition) {
    TafsirRepository.getEditionsForTradition(selectedTradition)
  }

  var selectedEditionKey by remember(selectedTradition) { 
    mutableStateOf(filteredEditions.firstOrNull()?.key ?: TafsirRepository.editions.first().key) 
  }
  val tafsirResult = remember(ayah.surahNumber, ayah.verseNumber, selectedEditionKey) {
    TafsirRepository.getTafsirForAyah(
      ayah = ayah,
      surah = surah,
      editionKey = selectedEditionKey,
      allHadiths = allHadiths
    )
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      // Header + Prev/Next Verse Tafsir Controls
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Authentic Tafsir & Linked Hadith",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          )
          Text(
            text = tafsirResult.revelationContext,
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }
        Row {
          IconButton(onClick = onPrevAyahTafsir, enabled = hasPrevAyah) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Verse Tafsir")
          }
          IconButton(onClick = onNextAyahTafsir, enabled = hasNextAyah) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Verse Tafsir")
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 8 Tafsir Edition Selector Chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(filteredEditions, key = { it.key }) { edition ->
          val isSelected = edition.key == selectedEditionKey
          FilterChip(
            selected = isSelected,
            onClick = { selectedEditionKey = edition.key },
            label = {
              Text(
                text = "${edition.titleEnglish} (${edition.eraBadge})",
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldPrimary,
              selectedLabelColor = Color.White
            ),
            modifier = Modifier.testTag("tafsir_chip_${edition.key}")
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Verse Text + Transliteration Box
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          val cleanTafsirArabic = OfflineQuranAssetLoader.stripDuplicateBismillahPrefix(
            surahNumber = ayah.surahNumber,
            verseNumber = ayah.verseNumber,
            rawArabic = tafsirResult.arabicText
          )
          Text(
            text = cleanTafsirArabic,
            style = MaterialTheme.typography.headlineSmall.copy(
              fontFamily = AmiriFontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = 24.sp,
              lineHeight = 46.sp,
              textAlign = TextAlign.Right
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Transliteration: ${tafsirResult.transliteration}",
            style = MaterialTheme.typography.bodySmall.copy(
              fontStyle = FontStyle.Italic,
              color = EmeraldPrimary,
              fontWeight = FontWeight.SemiBold
            )
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = tafsirResult.englishTranslation,
            style = MaterialTheme.typography.bodyMedium
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Selected Scholar & Methodology Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "${tafsirResult.edition.titleEnglish} • ${tafsirResult.edition.titleArabic}",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary
            )
          )
          Text(
            text = tafsirResult.edition.scholarName,
            style = MaterialTheme.typography.labelMedium.copy(
              color = GoldPrimary,
              fontWeight = FontWeight.Bold
            )
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = tafsirResult.edition.methodology,
            style = MaterialTheme.typography.bodySmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )

          HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

          // English Tafsir
          Text(
            text = tafsirResult.commentaryEnglish,
            style = MaterialTheme.typography.bodyMedium.copy(
              lineHeight = 22.sp
            )
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Urdu Tafsir
          Text(
            text = tafsirResult.commentaryUrdu,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = 15.sp,
              lineHeight = 30.sp,
              textAlign = TextAlign.Right,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              platformStyle = PlatformTextStyle(includeFontPadding = true)
            ),
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Cross-Referenced Quranic Verses
      Text(
        text = "Cross-Referenced Quranic Verses (Tafsir al-Qur'an bil-Qur'an)",
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      )
      Spacer(modifier = Modifier.height(6.dp))
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tafsirResult.relatedQuranVerses) { ref ->
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = EmeraldPrimary.copy(alpha = 0.12f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Surah $ref",
                style = MaterialTheme.typography.labelMedium.copy(
                  color = EmeraldPrimary,
                  fontWeight = FontWeight.Bold
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Linked Authentic Hadiths Section
      Text(
        text = "Linked Authentic Hadiths Explaining This Verse (${tafsirResult.linkedHadiths.size})",
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      )
      Spacer(modifier = Modifier.height(8.dp))

      tafsirResult.linkedHadiths.forEach { hadith ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "${hadith.reference} (Intl #${hadith.hadithNumber})",
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
                if (hadith.inBookReference.isNotEmpty()) {
                  Text(
                    text = hadith.inBookReference,
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  )
                }
              }
              SahihGradeBadge(grade = hadith.grade)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = hadith.arabicText,
              style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = AmiriFontFamily,
                textAlign = TextAlign.Right,
                fontSize = 18.sp,
                lineHeight = 30.sp
              ),
              modifier = Modifier.fillMaxWidth()
            )
            if (hadith.transliteration.isNotEmpty()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = hadith.transliteration,
                style = MaterialTheme.typography.bodySmall.copy(
                  fontStyle = FontStyle.Italic,
                  color = EmeraldPrimary
                )
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = hadith.englishText,
              style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = hadith.urduText,
              style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = UrduNastaliqFontFamily,
                textAlign = TextAlign.Right,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
fun AyahCardItem(
  ayah: AyahEntity,
  surahName: String,
  isBookmarked: Boolean,
  showEnglish: Boolean,
  showUrdu: Boolean,
  showTransliteration: Boolean,
  arabicFontSizeSp: Float,
  translationFontSizeSp: Float,
  onPlay: () -> Unit,
  onTajweedPractice: () -> Unit = {},
  onOpenTafsir: () -> Unit,
  onOpenReciterSelector: () -> Unit = {},
  onBookmark: () -> Unit,
  onCopy: () -> Unit,
  onAddNote: () -> Unit,
  onReport: () -> Unit = {},
  isReadingMode: Boolean = false,
  englishTranslationOverride: String? = null,
  urduTranslationOverride: String? = null,
  activeQulEdition: QulTranslationCatalog.QulTranslationEdition? = null,
  qulTranslationText: String? = null,
  audioState: com.example.ui.audio.AudioState = com.example.ui.audio.AudioState(),
  currentReciter: com.example.ui.audio.Reciter = com.example.ui.audio.availableReciters.first(),
  selectedTradition: String = "SUNNI",
  modifier: Modifier = Modifier
) {
  val lineHeightMultiplier = if (isReadingMode) 2.45f else 2.0f
  val cardHorizontalPadding = if (isReadingMode) 16.dp else 10.dp
  val cardVerticalPadding = if (isReadingMode) 20.dp else 8.dp

  val isThisAyahPlaying = (audioState.isPlaying || audioState.isBuffering) &&
    audioState.surahNumber == ayah.surahNumber &&
    audioState.ayahNumber == ayah.verseNumber

  // Clean Uthmani Arabic Text (without duplicate Bismillah on Verse 1)
  val cleanVerseArabic = remember(ayah.surahNumber, ayah.verseNumber, ayah.textArabic) {
    OfflineQuranAssetLoader.stripDuplicateBismillahPrefix(
      surahNumber = ayah.surahNumber,
      verseNumber = ayah.verseNumber,
      rawArabic = ayah.textArabic
    )
  }

  val arabicWords = remember(cleanVerseArabic) {
    cleanVerseArabic.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
  }

  // Compute active word index during playback based on character-weighted timing (resets when reciter or surah changes)
  val activeWordIndex = remember(
    ayah.surahNumber,
    ayah.verseNumber,
    audioState.reciter.id,
    audioState.surahNumber,
    audioState.ayahNumber,
    isThisAyahPlaying,
    audioState.isPlaying,
    audioState.currentPositionMs,
    audioState.durationMs,
    arabicWords
  ) {
    if (!isThisAyahPlaying || arabicWords.isEmpty()) {
      -1
    } else if (!audioState.isPlaying) {
      0
    } else {
      val duration = audioState.durationMs.coerceAtLeast(1)
      val progress = (audioState.currentPositionMs.toFloat() / duration.toFloat()).coerceIn(0f, 0.999f)
      val weights = arabicWords.map { it.length.coerceAtLeast(2).toFloat() }
      val totalWeight = weights.sum().coerceAtLeast(1f)
      val targetWeight = progress * totalWeight
      var cumulative = 0f
      var foundIdx = arabicWords.lastIndex
      for (idx in weights.indices) {
        cumulative += weights[idx]
        if (targetWeight <= cumulative) {
          foundIdx = idx
          break
        }
      }
      foundIdx
    }
  }

  ElevatedCard(
    modifier = modifier
      .fillMaxWidth()
      .then(
        when {
          isThisAyahPlaying -> {
            Modifier.border(
              width = 2.dp,
              color = Color(0xFF00E676),
              shape = RoundedCornerShape(if (isReadingMode) 20.dp else 16.dp)
            )
          }
          isReadingMode -> {
            Modifier.border(
              width = 1.dp,
              color = GoldPrimary.copy(alpha = 0.28f),
              shape = RoundedCornerShape(20.dp)
            )
          }
          else -> Modifier
        }
      )
      .testTag("ayah_card_${ayah.surahNumber}_${ayah.verseNumber}"),
    shape = RoundedCornerShape(if (isReadingMode) 20.dp else 16.dp),
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isReadingMode) 0.5.dp else 1.5.dp)
  ) {
    Column(
      modifier = Modifier.padding(
        horizontal = cardHorizontalPadding,
        vertical = cardVerticalPadding
      )
    ) {
      // Header: In Reading Mode, hide secondary navigation/action buttons for distraction-free recitation
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          AyahNumberBadge(verseNumber = ayah.verseNumber)
          if (!isReadingMode) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              modifier = Modifier
                .clickable(onClick = onOpenTafsir)
                .testTag("ayah_tafsir_button_${ayah.surahNumber}_${ayah.verseNumber}"),
              shape = RoundedCornerShape(8.dp),
              color = EmeraldPrimary.copy(alpha = 0.12f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.AutoStories,
                  contentDescription = "Read Tafsir & Linked Hadith",
                  tint = EmeraldPrimary,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = if (selectedTradition.equals("SHIA", ignoreCase = true)) {
                    "8 Shia Tafsirs & Hadith"
                  } else {
                    "14 Sunni Tafsirs & Hadith"
                  },
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
              }
            }
          }
          if (isThisAyahPlaying && arabicWords.isNotEmpty()) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF00E676).copy(alpha = 0.16f),
              border = BorderStroke(1.dp, Color(0xFF00E676))
            ) {
              Text(
                text = "● Word ${(activeWordIndex + 1).coerceIn(1, arabicWords.size)}/${arabicWords.size}",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF00E676)
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onPlay,
            modifier = Modifier
              .size(if (isReadingMode) 48.dp else 36.dp)
              .testTag("ayah_play_button_${ayah.surahNumber}_${ayah.verseNumber}")
          ) {
            Icon(
              imageVector = Icons.Default.VolumeUp,
              contentDescription = "Play Full Surah from Ayah",
              tint = if (isThisAyahPlaying) Color(0xFF00E676) else EmeraldPrimary
            )
          }
          IconButton(
            onClick = onTajweedPractice,
            modifier = Modifier
              .size(if (isReadingMode) 48.dp else 36.dp)
              .testTag("ayah_tajweed_button_${ayah.surahNumber}_${ayah.verseNumber}")
          ) {
            Icon(Icons.Default.Mic, contentDescription = "Practice Tajweed Recitation", tint = GoldPrimary)
          }
          if (!isReadingMode) {
            IconButton(onClick = onBookmark, modifier = Modifier.size(36.dp)) {
              Icon(
                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = "Bookmark",
                tint = GoldPrimary
              )
            }
            IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
              Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onAddNote, modifier = Modifier.size(36.dp)) {
              Icon(Icons.Default.NoteAdd, contentDescription = "Note", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(
              onClick = onReport,
              modifier = Modifier
                .size(36.dp)
                .testTag("ayah_flag_report_${ayah.surahNumber}_${ayah.verseNumber}")
            ) {
              Icon(
                Icons.Default.Flag,
                contentDescription = "Flag or Report Ayah",
                tint = MaterialTheme.colorScheme.error
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(if (isReadingMode) 20.dp else 14.dp))

      // Arabic Uthmani Text with Word-by-Word Green Highlighting while playing
      val baseTextColor = MaterialTheme.colorScheme.onSurface
      val annotatedArabicText = remember(cleanVerseArabic, arabicWords, isThisAyahPlaying, activeWordIndex, baseTextColor) {
        if (!isThisAyahPlaying || activeWordIndex < 0 || arabicWords.isEmpty()) {
          buildAnnotatedString {
            withStyle(SpanStyle(color = baseTextColor)) {
              append(cleanVerseArabic)
            }
          }
        } else {
          buildAnnotatedString {
            arabicWords.forEachIndexed { idx, word ->
              val isCurrentWord = idx == activeWordIndex
              val isSpokenWord = idx < activeWordIndex
              val wordColor = when {
                isCurrentWord -> Color(0xFF00E676)
                isSpokenWord -> Color(0xFF4CAF50)
                else -> baseTextColor
              }
              val wordBg = if (isCurrentWord) {
                Color(0xFF00E676).copy(alpha = 0.20f)
              } else {
                Color.Transparent
              }
              withStyle(
                SpanStyle(
                  color = wordColor,
                  background = wordBg,
                  fontWeight = if (isCurrentWord) FontWeight.Bold else FontWeight.Normal
                )
              ) {
                append(word)
              }
              if (idx < arabicWords.lastIndex) {
                append(" ")
              }
            }
          }
        }
      }

      Text(
        text = annotatedArabicText,
        style = MaterialTheme.typography.headlineMedium.copy(
          fontFamily = AmiriFontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = arabicFontSizeSp.sp,
          lineHeight = (arabicFontSizeSp * lineHeightMultiplier).sp,
          textAlign = TextAlign.Right,
          color = baseTextColor,
          platformStyle = PlatformTextStyle(includeFontPadding = true),
          lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
          )
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("ayah_arabic_text_${ayah.surahNumber}_${ayah.verseNumber}")
      )

      if (!isReadingMode && showTransliteration && ayah.transliteration.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = ayah.transliteration,
          style = MaterialTheme.typography.bodySmall.copy(
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 13.sp
          )
        )
      }

      if (!isReadingMode && showEnglish) {
        val enText = englishTranslationOverride ?: ayah.translationEnglish
        if (enText.isNotBlank()) {
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = enText,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = translationFontSizeSp.sp,
              lineHeight = (translationFontSizeSp * 1.45f).sp,
              color = MaterialTheme.colorScheme.onSurface
            )
          )
        }
      }

      if (!isReadingMode && showUrdu) {
        val urText = urduTranslationOverride ?: ayah.translationUrdu
        if (urText.isNotBlank()) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = urText,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = (translationFontSizeSp + 1f).sp,
              lineHeight = (translationFontSizeSp * 1.90f).sp,
              textAlign = TextAlign.Right,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              platformStyle = PlatformTextStyle(includeFontPadding = true)
            ),
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      if (!isReadingMode && activeQulEdition != null && !qulTranslationText.isNullOrBlank()) {
        val (mainQulVerse, footnoteQulText) = remember(qulTranslationText) {
          QulOfflineTranslationManager.splitVerseAndFootnotes(qulTranslationText)
        }
        var showFootnotesExpanded by remember(ayah.surahNumber, ayah.verseNumber, activeQulEdition.editionKey) {
          mutableStateOf(false)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = EmeraldPrimary.copy(alpha = 0.08f),
          border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.28f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("ayah_qul_translation_box_${ayah.surahNumber}_${ayah.verseNumber}")
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${activeQulEdition.languageNameNative} (${activeQulEdition.languageNameEn}) • ${activeQulEdition.translatorName}",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
              if (activeQulEdition.isRtl) {
                Text(
                  text = "RTL",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                  )
                )
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            CompositionLocalProvider(
              LocalLayoutDirection provides if (activeQulEdition.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
              val usePersoArabicFont = activeQulEdition.isRtl && activeQulEdition.languageCode != "he" && activeQulEdition.languageCode != "dv"
              Text(
                text = mainQulVerse,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontFamily = if (usePersoArabicFont) UrduNastaliqFontFamily else MaterialTheme.typography.bodyMedium.fontFamily,
                  fontSize = (if (activeQulEdition.isRtl) translationFontSizeSp + 1f else translationFontSizeSp).sp,
                  lineHeight = (translationFontSizeSp * if (activeQulEdition.isRtl) 1.85f else 1.50f).sp,
                  textAlign = if (activeQulEdition.isRtl) TextAlign.Right else TextAlign.Start,
                  textDirection = if (activeQulEdition.isRtl) TextDirection.Rtl else TextDirection.Ltr,
                  color = MaterialTheme.colorScheme.onSurface,
                  platformStyle = PlatformTextStyle(includeFontPadding = true)
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("ayah_qul_translation_text_${ayah.surahNumber}_${ayah.verseNumber}")
              )
            }
            if (!footnoteQulText.isNullOrBlank()) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = if (showFootnotesExpanded) "▼ Hide Footnote" else "▶ View Footnote (${activeQulEdition.format})",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = GoldPrimary,
                  fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                  .clickable { showFootnotesExpanded = !showFootnotesExpanded }
                  .padding(vertical = 2.dp)
              )
              if (showFootnotesExpanded) {
                Text(
                  text = footnoteQulText,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  ),
                  modifier = Modifier.padding(top = 4.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ContinuousMushafRecitationCard(
  surah: SurahEntity,
  ayahs: List<AyahEntity>,
  arabicFontSizeSp: Float,
  audioState: com.example.ui.audio.AudioState = com.example.ui.audio.AudioState(),
  onPlayAyah: (AyahEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  val baseTextColor = MaterialTheme.colorScheme.onSurface
  val isSurahPlaying = (audioState.isPlaying || audioState.isBuffering) && audioState.surahNumber == surah.number
  val activeAyahNum = if (isSurahPlaying) audioState.ayahNumber else null

  val continuousAnnotatedText = remember(
    surah.number,
    ayahs,
    audioState.reciter.id,
    audioState.surahNumber,
    isSurahPlaying,
    activeAyahNum,
    audioState.currentPositionMs,
    audioState.durationMs,
    baseTextColor
  ) {
    buildAnnotatedString {
      ayahs.forEachIndexed { index, ayah ->
        val cleanVerseArabic = OfflineQuranAssetLoader.stripDuplicateBismillahPrefix(
          surahNumber = ayah.surahNumber,
          verseNumber = ayah.verseNumber,
          rawArabic = ayah.textArabic
        )
        val isThisAyahActive = isSurahPlaying && activeAyahNum == ayah.verseNumber
        if (isThisAyahActive) {
          val words = cleanVerseArabic.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
          val duration = audioState.durationMs.coerceAtLeast(1)
          val progress = (audioState.currentPositionMs.toFloat() / duration.toFloat()).coerceIn(0f, 0.999f)
          val weights = words.map { it.length.coerceAtLeast(2).toFloat() }
          val totalWeight = weights.sum().coerceAtLeast(1f)
          val targetWeight = progress * totalWeight
          var cumulative = 0f
          var activeWordIdx = words.lastIndex
          for (wIdx in weights.indices) {
            cumulative += weights[wIdx]
            if (targetWeight <= cumulative) {
              activeWordIdx = wIdx
              break
            }
          }
          words.forEachIndexed { wIdx, w ->
            val wColor = when {
              wIdx == activeWordIdx -> Color(0xFF00E676)
              wIdx < activeWordIdx -> Color(0xFF4CAF50)
              else -> baseTextColor
            }
            withStyle(
              SpanStyle(
                color = wColor,
                background = if (wIdx == activeWordIdx) Color(0xFF00E676).copy(alpha = 0.20f) else Color.Transparent,
                fontWeight = if (wIdx == activeWordIdx) FontWeight.Bold else FontWeight.Normal
              )
            ) {
              append(w)
            }
            if (wIdx < words.lastIndex) append(" ")
          }
        } else {
          withStyle(SpanStyle(color = baseTextColor)) {
            append(cleanVerseArabic)
          }
        }
        withStyle(SpanStyle(color = GoldPrimary)) {
          append(" ﴿")
          append(toArabicIndicDigits(ayah.verseNumber))
          append("﴾ ")
        }
        if (index < ayahs.lastIndex) {
          append(" ")
        }
      }
    }
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .border(
        width = 1.5.dp,
        color = GoldPrimary.copy(alpha = 0.55f),
        shape = RoundedCornerShape(22.dp)
      )
      .testTag("continuous_mushaf_flow_card"),
    shape = RoundedCornerShape(22.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "سُورَةُ ${surah.nameArabic} • Mushaf Recitation View",
          style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = ScheherazadeFontFamily,
            fontWeight = FontWeight.Bold,
            color = EmeraldPrimary
          )
        )
        if (ayahs.isNotEmpty()) {
          OutlinedButton(
            onClick = { onPlayAyah(ayahs.first()) },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier
              .heightIn(min = 48.dp)
              .testTag("mushaf_play_first_ayah_button")
          ) {
            Icon(
              imageVector = Icons.Default.VolumeUp,
              contentDescription = "Recite Surah",
              tint = EmeraldPrimary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Recite", fontSize = 12.sp, color = EmeraldPrimary)
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = continuousAnnotatedText,
        style = MaterialTheme.typography.headlineMedium.copy(
          fontFamily = AmiriFontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = arabicFontSizeSp.sp,
          lineHeight = (arabicFontSizeSp * 2.45f).sp,
          textAlign = TextAlign.Right,
          color = baseTextColor,
          platformStyle = PlatformTextStyle(includeFontPadding = true),
          lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
          )
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("continuous_mushaf_arabic_text")
      )
    }
  }
}

private fun toArabicIndicDigits(number: Int): String {
  val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
  return number.toString().map { ch ->
    if (ch in '0'..'9') arabicDigits[ch - '0'] else ch
  }.joinToString("")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranRecitersBottomSheet(
  currentReciter: com.example.ui.audio.Reciter,
  recitersList: List<com.example.ui.audio.Reciter> = com.example.ui.audio.availableReciters,
  onSelectReciter: (com.example.ui.audio.Reciter) -> Unit,
  onDismiss: () -> Unit
) {
  val sunniReciters = remember(recitersList) {
    recitersList.filter { it.tradition == "SUNNI" }
  }
  val shiaReciters = remember(recitersList) {
    recitersList.filter { it.tradition == "SHIA" }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 12.dp)
        .testTag("quran_reciters_modal_sheet")
    ) {
      Text(
        text = "Verified Quran Reciters (القراء المعتمدون)",
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      )
      Text(
        text = "Separate Sunni & Shia Quran Reciters • Canonical Uthmani Recitation Only",
        style = MaterialTheme.typography.labelMedium.copy(
          color = GoldPrimary,
          fontWeight = FontWeight.Bold
        )
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Section 1: Verified Sunni Quran Reciters
      Text(
        text = "Verified Sunni Quran Reciters (${sunniReciters.size})",
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Bold,
          color = EmeraldPrimary
        )
      )
      Spacer(modifier = Modifier.height(6.dp))

      sunniReciters.forEach { reciter ->
        val isSelected = reciter.id == currentReciter.id
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelectReciter(reciter) }
            .testTag("reciter_option_${reciter.id}"),
          shape = RoundedCornerShape(12.dp),
          color = if (isSelected) EmeraldPrimary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          border = if (isSelected) BorderStroke(1.5.dp, EmeraldPrimary) else null
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${reciter.name} • ${reciter.arabicName}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "${reciter.style} • ${reciter.verificationStatus}",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              )
            }
            if (isSelected) {
              Text(
                text = "Active",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Section 2: Verified Shia Quran Reciters
      Text(
        text = "Verified Shia Quran Reciters (${shiaReciters.size})",
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Bold,
          color = GoldPrimary
        )
      )
      Text(
        text = "Dedicated Quranic Recitation Only (Strictly separate from Duas, Ziyarat & Lectures)",
        style = MaterialTheme.typography.labelSmall.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      )
      Spacer(modifier = Modifier.height(6.dp))

      shiaReciters.forEach { reciter ->
        val isSelected = reciter.id == currentReciter.id
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelectReciter(reciter) }
            .testTag("reciter_option_${reciter.id}"),
          shape = RoundedCornerShape(12.dp),
          color = if (isSelected) GoldPrimary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          border = if (isSelected) BorderStroke(1.5.dp, GoldPrimary) else null
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${reciter.name} • ${reciter.arabicName}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "${reciter.style} • ${reciter.verificationStatus}",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              )
            }
            if (isSelected) {
              Text(
                text = "Active",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = GoldPrimary
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
      HorizontalDivider()
      Spacer(modifier = Modifier.height(10.dp))

      // Section 3: Honest Stream Verification & Availability Notes
      Text(
        text = "Stream Verification & Availability Notes",
        style = MaterialTheme.typography.labelLarge.copy(
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      )
      Spacer(modifier = Modifier.height(6.dp))

      com.example.ui.audio.reciterAvailabilityNotes.forEach { note ->
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${note.name} (${note.arabicName})",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = note.statusBadge,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = GoldPrimary
                )
              )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = note.explanation,
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

