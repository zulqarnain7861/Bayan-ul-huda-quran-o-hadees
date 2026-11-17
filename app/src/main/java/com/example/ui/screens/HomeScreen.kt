package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.example.data.local.entities.AyahEntity
import com.example.data.local.entities.HadithEntity
import com.example.data.local.entities.PrayerScheduleEntity
import com.example.data.local.entities.SurahEntity
import com.example.data.model.DailyDuasDataProvider
import com.example.data.model.HistoricalRelationshipTag
import com.example.data.model.ReflectionCategory
import com.example.data.model.ReflectionDataProvider
import com.example.data.model.WisdomAttributionStatus
import com.example.data.model.WisdomNarrationType
import com.example.data.prayer.PrayerAndAlarmConfig
import com.example.data.prayer.SunnahRemindersUiState
import com.example.ui.components.BismillahHeader
import com.example.ui.components.SahihGradeBadge
import com.example.ui.components.SalahAndFajrAlarmCard
import com.example.ui.prayer.FajrAlarmPlaybackState
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ScheherazadeFontFamily
import com.example.ui.theme.UrduNastaliqFontFamily
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
  dailyAyah: AyahEntity?,
  dailyHadith: HadithEntity?,
  surahs: List<SurahEntity>,
  prayerSchedule: PrayerScheduleEntity? = null,
  prayerConfig: PrayerAndAlarmConfig = PrayerAndAlarmConfig(),
  alarmPlaybackState: FajrAlarmPlaybackState = FajrAlarmPlaybackState(),
  sunnahRemindersState: SunnahRemindersUiState = SunnahRemindersUiState(),
  onUpdatePrayerConfig: (PrayerAndAlarmConfig) -> Unit = {},
  onRefreshPrayerSchedule: () -> Unit = {},
  onTestAzanOrFajrAlarm: (String, Boolean) -> Unit = { _, _ -> },
  onStopActiveAlarm: () -> Unit = {},
  onSnoozeActiveAlarm: (Int) -> Unit = {},
  onSelectPreviewDayOfWeek: (Int) -> Unit = {},
  onSendCalculatedDayNotificationNow: (Int?) -> Unit = {},
  onUpdateFridayBathReminder: (Boolean, String) -> Unit = { _, _ -> },
  onUpdateDailyCalculatedReminder: (Boolean, String) -> Unit = { _, _ -> },
  onToggleFridayChecklistItem: (String) -> Unit = {},
  onIncrementFridaySalawat: (Int) -> Unit = {},
  onOpenSurah: (SurahEntity) -> Unit,
  onResumeLastRead: () -> Unit = {},
  onPlayRecitation: (AyahEntity, String) -> Unit,
  onBookmarkAyah: (AyahEntity, String) -> Unit,
  onBookmarkHadith: (HadithEntity) -> Unit,
  onNavigateToQuran: () -> Unit,
  onNavigateToHadith: () -> Unit,
  onNavigateToLibrary: () -> Unit = {},
  onNavigateToLibraryTab: (Int) -> Unit = { onNavigateToLibrary() },
  onNavigateToDownloads: () -> Unit = {},
  onNavigateToEvidence: () -> Unit = {},
  onNavigateToQibla: () -> Unit = {},
  onNavigateToDailyDuas: () -> Unit = {},
  onNavigateToSehriIftar: () -> Unit = {},
  onNavigateToLearnIslam: () -> Unit = {},
  onNavigateToNooraniQaidaPdf: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val appStrings = com.example.ui.localization.LocalAppStrings.current
  var reflectionCategory by remember { mutableStateOf(ReflectionCategory.PROPHETIC_GUIDANCE) }
  var reflectionAttributionFilter by remember {
    mutableStateOf<WisdomAttributionStatus?>(WisdomAttributionStatus.VERIFIED_ATTRIBUTION)
  }
  var reflectionRelationshipFilter by remember { mutableStateOf(HistoricalRelationshipTag.ALL) }
  var reflectionBookFilter by remember { mutableStateOf<String?>(null) }
  var reflectionBookmarkedOnly by remember { mutableStateOf(false) }
  var bookmarkedWisdomIds by remember {
    mutableStateOf(ReflectionDataProvider.getBookmarkedWisdomIds(context))
  }
  var reflectionSearchQuery by remember { mutableStateOf("") }
  var reflectionIndex by remember { mutableIntStateOf(0) }
  var homeTraditionSectionMode by remember {
    mutableStateOf(com.example.ui.components.HomeTraditionSectionMode.SUNNI_SECTION_ONLY)
  }

  var dailyAyahOffset by remember {
    mutableIntStateOf(com.example.data.model.DailyGuidanceAndRecommendationEngine.getSavedDailyAyahOffset(context))
  }
  var showStepByStepPath by remember { mutableStateOf(true) }
  var completedLearningSteps by remember {
    mutableStateOf(com.example.data.model.DailyGuidanceAndRecommendationEngine.getCompletedLearningSteps(context))
  }
  var recRotationOffset by remember {
    mutableIntStateOf(com.example.data.model.DailyGuidanceAndRecommendationEngine.getSavedRecommendationRotation(context))
  }
  var selectedRecCategory by remember { mutableStateOf("ALL") }

  val activeCuratedSeed = remember(dailyAyahOffset) {
    com.example.data.model.DailyGuidanceAndRecommendationEngine.getActiveDailyAyahSeed(context, dailyAyahOffset)
  }
  val resolvedDailyAyah = remember(activeCuratedSeed, dailyAyah) {
    com.example.data.model.DailyGuidanceAndRecommendationEngine.resolveAuthenticDailyAyahEntity(
      context = context,
      seed = activeCuratedSeed,
      fallbackAyah = dailyAyah
    )
  }
  val resolvedDailyHadith = remember(activeCuratedSeed, dailyHadith) {
    com.example.data.model.DailyGuidanceAndRecommendationEngine.resolveAuthenticDailyHadith(
      seed = activeCuratedSeed,
      fallbackHadith = dailyHadith
    )
  }

  val mostRecentVideoResume = remember(context) {
    com.example.player.VideoPlaybackResumeStore.getMostRecentSavedResumeState(context)
  }

  Column(modifier = modifier.fillMaxSize()) {
    // Top Tradition Entry Switcher (Ahal Sunnat Home Screen vs Shia Home Screen — matching QuranScreen top bar)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 4.dp)
        .testTag("home_top_tradition_switcher_row"),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      val isSunniSelected =
        homeTraditionSectionMode != com.example.ui.components.HomeTraditionSectionMode.SHIA_SECTION_ONLY
      val isShiaSelected =
        homeTraditionSectionMode == com.example.ui.components.HomeTraditionSectionMode.SHIA_SECTION_ONLY

      FilterChip(
        selected = isSunniSelected,
        onClick = {
          homeTraditionSectionMode = com.example.ui.components.HomeTraditionSectionMode.SUNNI_SECTION_ONLY
          reflectionCategory = ReflectionCategory.PROPHETIC_GUIDANCE
          reflectionAttributionFilter = null
          reflectionIndex = 0
        },
        label = {
          Text(
            text = "Ahal Sunnat Home Screen",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = EmeraldPrimary,
          selectedLabelColor = Color.White
        ),
        modifier = Modifier
          .weight(1f)
          .height(36.dp)
          .testTag("home_ahl_sunnat_entry")
      )

      FilterChip(
        selected = isShiaSelected,
        onClick = {
          homeTraditionSectionMode = com.example.ui.components.HomeTraditionSectionMode.SHIA_SECTION_ONLY
          reflectionCategory = ReflectionCategory.AHLUL_BAYT_WISDOM
          reflectionAttributionFilter = null
          reflectionIndex = 0
        },
        label = {
          Text(
            text = "Shia Home Screen",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = GoldPrimary,
          selectedLabelColor = Color.Black
        ),
        modifier = Modifier
          .weight(1f)
          .height(36.dp)
          .testTag("home_shia_entry")
      )
    }

    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(vertical = 12.dp)
    ) {
    // Sacred Bismillah Greeting Header
    item {
      BismillahHeader()
      Spacer(modifier = Modifier.height(12.dp))
    }

    // 1. TOP ON HOME (Right after Bismillah): Start Learning — Learn Quran & Noorani Qaida (Symmetrical Illuminated Card)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToLearnIslam() }
          .testTag("home_learn_islam_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
          containerColor = Color(0xFF0A4D3C)
        ),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldPrimary)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = GoldPrimary.copy(alpha = 0.22f)
          ) {
            Text(
              text = "START LEARNING • DARASGAH & MADRASAH CURRICULUM",
              style = MaterialTheme.typography.labelSmall.copy(
                color = GoldPrimary,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
              ),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "نورانی قاعدہ، تجوید، ایمان و کلمات، نمازِ مترجم، وضو اور مسنون دعائیں",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = com.example.ui.theme.UrduNastaliqFontFamily,
              color = GoldPrimary,
              fontSize = 15.sp,
              lineHeight = 26.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Learn Quran & Noorani Qaida",
            style = MaterialTheme.typography.titleMedium.copy(
              color = Color.White,
              fontWeight = FontWeight.ExtraBold,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "17 Noorani Qaida lessons (Makharij, Harakat, Tanween, Madd & Tajweed with Voice), Six Kalimas, Iman-e-Mufassal/Mujmal, Complete Namaz & Wudu, and Daily Duas.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = Color.White.copy(alpha = 0.9f),
              lineHeight = 18.sp,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(10.dp))
          Button(
            onClick = onNavigateToLearnIslam,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = GoldPrimary,
              contentColor = Color(0xFF073B2E)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("home_open_learn_islam_btn")
          ) {
            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Start Learning: Noorani Qaida & Quran",
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedButton(
            onClick = onNavigateToNooraniQaidaPdf,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldPrimary),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = GoldPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("home_open_noorani_qaida_pdf_btn")
          ) {
            Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Open HD Noorani Qaida Book (53 Pages PDF + MCQ Tests)",
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    // 2. SECOND POSITION ON HOME SCREEN (Immediately after First-Priority Section):
    // Sunni Nimaz Timings & 13 Sunni Azans on Ahal Sunnat Home Screen;
    // Shia (Ja'fari) Nimaz Timings & Official Shia Azans on Shia Home Screen
    item {
      if (homeTraditionSectionMode == com.example.ui.components.HomeTraditionSectionMode.SHIA_SECTION_ONLY) {
        com.example.ui.components.ShiaStandalonePrayerTimingsCard(
          sunniConfig = prayerConfig,
          onOpenSehriIftarScreen = onNavigateToSehriIftar,
          onOpenQiblaCompass = onNavigateToQibla,
          alarmPlaybackState = alarmPlaybackState,
          onUpdateConfig = onUpdatePrayerConfig,
          onStopActiveAlarm = onStopActiveAlarm,
          onSnoozeActiveAlarm = onSnoozeActiveAlarm
        )
      } else {
        SalahAndFajrAlarmCard(
          schedule = prayerSchedule,
          config = prayerConfig,
          alarmPlaybackState = alarmPlaybackState,
          onUpdateConfig = onUpdatePrayerConfig,
          onRefreshSchedule = onRefreshPrayerSchedule,
          onTestAzanOrFajrAlarm = onTestAzanOrFajrAlarm,
          onStopActiveAlarm = onStopActiveAlarm,
          onSnoozeActiveAlarm = onSnoozeActiveAlarm,
          onOpenQiblaCompass = onNavigateToQibla,
          showCombinedSunniShiaComparison = false
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // 1B. Video Lectures Library & Resume Video Lecture Card (Symmetrical Centered Layout)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToLibraryTab(5) }
          .testTag("home_video_library_and_resume_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
          containerColor = EmeraldPrimary.copy(alpha = 0.14f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.4.dp, GoldPrimary)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = EmeraldPrimary
          ) {
            Text(
              text = if (mostRecentVideoResume != null) {
                "▶ RESUME VIDEO LECTURE • SAVED AT ${mostRecentVideoResume.formattedTime}"
              } else {
                "🎬 VIDEO LECTURES LIBRARY • 480+ LECTURES & AUTO-RESUME"
              },
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
              ),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "قرآن و سنت کی روشنی میں علمی و تحقیقی ویڈیو لیکچرز",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = com.example.ui.theme.UrduNastaliqFontFamily,
              fontSize = 15.sp,
              lineHeight = 25.sp,
              fontWeight = FontWeight.Bold,
              color = GoldPrimary,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Text(
            text = "Understand Islam · Video Lectures Library (EMAM)",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (mostRecentVideoResume != null) {
              "Last watched lecture (${mostRecentVideoResume.youtubeId}) saved at ${mostRecentVideoResume.formattedTime}. Tap below to resume playback or browse all 480+ Quran Tafseer, Hadith, Fiqh & Seerah lectures."
            } else {
              "Engineer Muhammad Ali Mirza · 480+ Verified Quran Tafseer, Sahih Hadith, Aqeedah, Fiqh & Seerah Video Lectures with 144p–1080p quality & automatic resume."
            },
            style = MaterialTheme.typography.bodySmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 18.sp,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { onNavigateToLibraryTab(5) },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldPrimary,
                contentColor = Color.White
              ),
              modifier = Modifier
                .weight(1f)
                .testTag("home_resume_video_lecture_btn")
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (mostRecentVideoResume != null) {
                  "Resume Video (${mostRecentVideoResume.formattedTime})"
                } else {
                  "Resume / Watch Video"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
              )
            }
            OutlinedButton(
              onClick = { onNavigateToLibraryTab(5) },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("home_open_video_library_btn")
            ) {
              Text(
                text = "Open Video Library",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
              )
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    // 1C. All Features & Scholarly Modules Quick Hub Card (Symmetrical Centered Grid)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("home_all_features_hub_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "تمام علمی، تحقیقی اور عبادتی شعبے: ایک نظر میں",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = com.example.ui.theme.UrduNastaliqFontFamily,
              fontSize = 14.sp,
              lineHeight = 24.sp,
              fontWeight = FontWeight.Bold,
              color = GoldPrimary,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Text(
            text = "All Features & Scholarly Libraries (1-Tap Access)",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              color = EmeraldPrimary,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          val hubItems = listOf(
            Triple("▶ Video Lectures (480+)", "home_hub_video_lectures_btn") { onNavigateToLibraryTab(5) },
            Triple("📚 152 Classical Books", "home_hub_152_books_btn") { onNavigateToLibraryTab(1) },
            Triple("🏛 Islamic History (26)", "home_hub_history_btn") { onNavigateToLibraryTab(0) },
            Triple("📖 Fikr-e-Maududi", "home_hub_maududi_btn") { onNavigateToLibraryTab(6) },
            Triple("🌟 25 Prophet Stories", "home_hub_prophets_btn") { onNavigateToLibraryTab(8) },
            Triple("🕌 Sahaba & Ahl al-Bayt", "home_hub_sahaba_btn") { onNavigateToLibraryTab(10) },
            Triple("⚖️ IslamQA (4,203)", "home_hub_islamqa_btn") { onNavigateToLibraryTab(11) },
            Triple("🔬 Islamic Research (4)", "home_hub_research_btn") { onNavigateToLibraryTab(7) },
            Triple("🗂 Classified Folders", "home_hub_folders_btn") { onNavigateToLibraryTab(9) },
            Triple("🎓 Noorani Qaida & Namaz", "home_hub_learn_islam_btn") { onNavigateToLearnIslam() },
            Triple("✔ Islam with Evidence", "home_hub_evidence_btn") { onNavigateToEvidence() },
            Triple("🌙 Sehri & Iftar Timetable", "home_hub_sehri_iftar_btn") { onNavigateToSehriIftar() },
            Triple("🧭 Qibla Compass", "home_hub_qibla_btn") { onNavigateToQibla() },
            Triple("🤲 Daily Duas & Sunnah", "home_hub_duas_btn") { onNavigateToDailyDuas() },
            Triple("⬇️ Offline Downloads", "home_hub_downloads_btn") { onNavigateToDownloads() },
            Triple("📲 Share App (APK + Link)", "home_hub_share_app_btn") {
              com.example.ui.components.shareAppApkWithDriveLink(context)
            },
            Triple("📥 Latest Updates (Drive)", "home_hub_drive_updates_btn") {
              com.example.ui.components.openAppUpdatesDriveFolder(context)
            }
          )
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            hubItems.forEach { (label, tag, action) ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f)),
                modifier = Modifier
                  .clickable { action() }
                  .testTag(tag)
              ) {
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                  ),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    // 1D. Share App (APK File + Official Google Drive Updates Folder Link) Card (Symmetrical Centered Layout)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("home_share_app_apk_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = EmeraldPrimary.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.7f))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "بیان الہدیٰ ایپ (APK) اور تازہ ترین اپڈیٹس لنک شیئر کریں",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = com.example.ui.theme.UrduNastaliqFontFamily,
              fontSize = 14.sp,
              lineHeight = 24.sp,
              fontWeight = FontWeight.Bold,
              color = GoldPrimary,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Text(
            text = "Share Bayan al-Huda (APK File + Official Updates Link)",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              color = EmeraldPrimary,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Shares the installed APK file directly along with the official Google Drive folder link for downloading the latest updates.",
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { com.example.ui.components.shareAppApkWithDriveLink(context) },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldPrimary,
                contentColor = Color.White
              ),
              modifier = Modifier
                .weight(1f)
                .testTag("home_share_app_apk_button")
            ) {
              Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Share APK + Link",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
              )
            }
            OutlinedButton(
              onClick = { com.example.ui.components.openAppUpdatesDriveFolder(context) },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("home_open_drive_updates_button")
            ) {
              Text(
                text = "Updates Drive Folder",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
              )
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    // Resume Quran Reading Progress Tracker Card (Shared across all traditions)
    if (prayerConfig.lastReadSurahNumber in 1..114) {
      item {
        val totalVerses = prayerConfig.lastReadTotalVerses.coerceAtLeast(1)
        val currentVerse = prayerConfig.lastReadVerseNumber.coerceIn(1, totalVerses)
        val progressPercent = ((currentVerse.toFloat() / totalVerses.toFloat()) * 100f).toInt().coerceIn(1, 100)

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onResumeLastRead() }
            .testTag("home_resume_reading_card"),
          shape = RoundedCornerShape(16.dp),
          color = EmeraldPrimary.copy(alpha = 0.12f),
          border = androidx.compose.foundation.BorderStroke(1.2.dp, GoldPrimary.copy(alpha = 0.7f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "📖 ${appStrings.resumeReadingTitle} • $progressPercent%",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Surah ${prayerConfig.lastReadSurahNumber}. ${prayerConfig.lastReadSurahNameEn} (${prayerConfig.lastReadSurahNameAr})",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              )
              Text(
                text = "${appStrings.verseLabel} $currentVerse ${appStrings.ofLabel} $totalVerses • ${prayerConfig.lastReadTradition} Quran",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
              onClick = onResumeLastRead,
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldPrimary,
                contentColor = Color.White
              ),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              modifier = Modifier.testTag("home_resume_reading_button")
            ) {
              Text(
                text = appStrings.resumeReadingButton,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(14.dp))
      }
    }

    // =========================================================================
    // AHAL SUNNAT HOME SCREEN CONTENT (Driven solely by the single top selector)
    // (Sunni Sehri/Iftari & Masnoon Duas + Sunni Friday & Daily Sunnah Reminders)
    // =========================================================================
    if (homeTraditionSectionMode != com.example.ui.components.HomeTraditionSectionMode.SHIA_SECTION_ONLY) {
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("home_sunni_homescreen_section"),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          val adjustedSehri = prayerSchedule?.fajrTime?.let { rawFajr ->
            com.example.data.prayer.SehriIftarTimetableEngine.applyMinuteAdjustment(
              rawFajr,
              prayerConfig.sehriAdjustmentMinutes
            )
          }
          val adjustedIftar = prayerSchedule?.maghribTime?.let { rawMaghrib ->
            com.example.data.prayer.SehriIftarTimetableEngine.applyMinuteAdjustment(
              rawMaghrib,
              prayerConfig.iftarAdjustmentMinutes
            )
          }
          val presentDateDuas = DailyDuasDataProvider.getPresentDateShowcaseDuas(
            hijriMonthIndex = sunnahRemindersState.calculatedDayStatus.hijriInfo.monthIndex,
            isFridayOrThursdayAfterMaghrib = sunnahRemindersState.calculatedDayStatus.isFridayToday ||
              sunnahRemindersState.calculatedDayStatus.isThursdayAfterMaghribJumuahActive
          )
          SpecialRamadanDuasShowcaseCard(
            duas = presentDateDuas,
            onOpenSehriIftarTimetable = onNavigateToSehriIftar,
            todaySehriEnds = adjustedSehri,
            todayIftarTime = adjustedIftar,
            cityName = "${prayerConfig.city}, ${prayerConfig.country}",
            prayerConfig = prayerConfig,
            onUpdatePrayerConfig = onUpdatePrayerConfig,
            modifier = Modifier.testTag("home_ramadan_duas_and_timetable_card")
          )

          CalculatedFridayAndDailySunnahHomeCard(
            remindersState = sunnahRemindersState,
            surahs = surahs,
            onSelectPreviewDayOfWeek = onSelectPreviewDayOfWeek,
            onSendCalculatedDayNotificationNow = onSendCalculatedDayNotificationNow,
            onUpdateFridayBathReminder = onUpdateFridayBathReminder,
            onUpdateDailyCalculatedReminder = onUpdateDailyCalculatedReminder,
            onToggleFridayChecklistItem = onToggleFridayChecklistItem,
            onIncrementFridaySalawat = onIncrementFridaySalawat,
            onOpenSurah = onOpenSurah,
            onNavigateToDailyDuas = onNavigateToDailyDuas
          )
        }
        Spacer(modifier = Modifier.height(18.dp))
      }
    }

    // =========================================================================
    // SHIA HOME SCREEN CONTENT (Driven solely by the single top selector)
    // (Shia Duas, Separate Shia Sehri/Iftari Engine, Evidence-Based Practices, Shia Learning & Ahl al-Bayt Daily Hadith)
    // =========================================================================
    if (homeTraditionSectionMode == com.example.ui.components.HomeTraditionSectionMode.SHIA_SECTION_ONLY) {
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("home_shia_homescreen_section"),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          com.example.ui.components.ShiaHomeKnowledgeAndPracticesSectionCard(
            sunniConfig = prayerConfig,
            onOpenSehriIftarScreen = onNavigateToSehriIftar,
            onOpenDailyDuasScreen = onNavigateToDailyDuas,
            onOpenLearnIslamScreen = onNavigateToLearnIslam,
            onOpenNooraniQaidaPdf = onNavigateToNooraniQaidaPdf
          )

          com.example.ui.components.ShiaDailyAhlulBaytHadithCard(
            onNavigateToHadith = onNavigateToHadith
          )

          CalculatedFridayAndDailySunnahHomeCard(
            remindersState = sunnahRemindersState,
            surahs = surahs,
            onSelectPreviewDayOfWeek = onSelectPreviewDayOfWeek,
            onSendCalculatedDayNotificationNow = onSendCalculatedDayNotificationNow,
            onUpdateFridayBathReminder = onUpdateFridayBathReminder,
            onUpdateDailyCalculatedReminder = onUpdateDailyCalculatedReminder,
            onToggleFridayChecklistItem = onToggleFridayChecklistItem,
            onIncrementFridaySalawat = onIncrementFridaySalawat,
            onOpenSurah = onOpenSurah,
            onNavigateToDailyDuas = onNavigateToDailyDuas,
            isShiaTradition = true
          )
        }
        Spacer(modifier = Modifier.height(18.dp))
      }
    }

    // Complete Offline Corpus Summary Card (114 Surahs, 8 Tafsirs, 7 Primary Hadith Books, 32 Islamic Books)
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = EmeraldPrimary.copy(alpha = 0.1f)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "100% Offline Quran (114 Surahs • 6,236 Verses)",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              )
            )
            Text(
              text = "8 Authentic Tafsirs • 7 Primary Hadith Books • 32 Classical Islamic Books",
              style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
          FilledTonalButton(
            onClick = onNavigateToLibrary,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("home_open_32_books_btn")
          ) {
            Icon(Icons.Default.Bookmarks, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("32 Books", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        FilledTonalButton(
          onClick = onNavigateToDownloads,
          modifier = Modifier.weight(1f).testTag("home_open_offline_downloads_btn"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Offline Download Manager", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        OutlinedButton(
          onClick = onNavigateToEvidence,
          modifier = Modifier.weight(1f).testTag("home_open_islam_with_evidence_btn"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Islam with Evidence", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        FilledTonalButton(
          onClick = onNavigateToQibla,
          modifier = Modifier
            .weight(1f)
            .testTag("home_open_qibla_compass_btn"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Explore,
            contentDescription = null,
            tint = GoldPrimary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Qibla Compass",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
        FilledTonalButton(
          onClick = onNavigateToDailyDuas,
          modifier = Modifier
            .weight(1f)
            .testTag("home_open_daily_duas_btn"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AutoStories,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Daily Duas & Reminders",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Daily Quranic Ayah & Step-by-Step Guided Learning Card
    item {
      resolvedDailyAyah.let { ayah ->
        val surah = surahs.find { it.number == ayah.surahNumber }
        val surahName = surah?.nameEnglish ?: activeCuratedSeed.surahNameEnglish
        val surahNameAr = surah?.nameArabic ?: activeCuratedSeed.surahNameArabic

        ElevatedCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_ayah_card"),
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
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(16.dp)
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = "Daily Qur'anic Aayah • آیتِ امروز",
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.ExtraBold,
                      color = MaterialTheme.colorScheme.primary
                    )
                  )
                  Text(
                    text = activeCuratedSeed.themeBadgeEnglish,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = GoldPrimary
                    )
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
              ) {
                Text(
                  text = "$surahName ($surahNameAr) ${ayah.surahNumber}:${ayah.verseNumber}",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // RTL Urdu Theme Subtitle
            Text(
              text = activeCuratedSeed.themeBadgeUrdu,
              style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 14.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.Bold,
                textDirection = TextDirection.Rtl,
                textAlign = TextAlign.Right,
                color = EmeraldPrimary
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Authentic Uthmani Arabic Verse (strictly 0% alteration, full RTL visibility)
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = EmeraldPrimary.copy(alpha = 0.06f),
              border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.55f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = ayah.textArabic,
                style = MaterialTheme.typography.headlineMedium.copy(
                  fontFamily = AmiriFontFamily,
                  fontSize = 23.sp,
                  lineHeight = 42.sp,
                  textDirection = TextDirection.Rtl,
                  textAlign = TextAlign.Right,
                  color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 12.dp)
              )
            }

            if (ayah.transliteration.isNotBlank()) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = ayah.transliteration,
                style = MaterialTheme.typography.bodySmall.copy(
                  fontSize = 12.sp,
                  fontStyle = FontStyle.Italic,
                  color = MaterialTheme.colorScheme.secondary
                )
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reliable English Translation (Sahih International)
            Text(
              text = ayah.translationEnglish,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Reliable Urdu Translation (Fateh Muhammad Jalandhry — full RTL alignment)
            Text(
              text = ayah.translationUrdu,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 15.sp,
                lineHeight = 30.sp,
                textDirection = TextDirection.Rtl,
                textAlign = TextAlign.Right,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Reflection & Paired Hadith Box (Sunni Hadith on Sunni Screen; Al-Kafi / Shia Hadith on Shia Screen)
            val isShiaHomeActive = homeTraditionSectionMode == com.example.ui.components.HomeTraditionSectionMode.SHIA_SECTION_ONLY
            val activePairedHadithRef = if (isShiaHomeActive) {
              activeCuratedSeed.shiaPairedHadithReference
            } else {
              activeCuratedSeed.pairedHadithReference
            }
            val activePairedHadithEn = if (isShiaHomeActive) {
              activeCuratedSeed.shiaPairedHadithSummaryEnglish
            } else {
              activeCuratedSeed.pairedHadithSummaryEnglish
            }
            val activePairedHadithUr = if (isShiaHomeActive) {
              activeCuratedSeed.shiaPairedHadithSummaryUrdu
            } else {
              activeCuratedSeed.pairedHadithSummaryUrdu
            }
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = if (isShiaHomeActive) {
                    "💡 Reflection & Connected Ahl al-Bayt Hadith ($activePairedHadithRef)"
                  } else {
                    "💡 Reflection & Connected Sunnah ($activePairedHadithRef)"
                  },
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldPrimary
                  )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "${activeCuratedSeed.reflectionSummaryEnglish} $activePairedHadithEn",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "${activeCuratedSeed.reflectionSummaryUrdu} $activePairedHadithUr",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 25.sp,
                    textDirection = TextDirection.Rtl,
                    textAlign = TextAlign.Right,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card Action Buttons + Browse Daily Verses
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalButton(
                  onClick = { onPlayRecitation(ayah, surahName) },
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Icon(Icons.Default.VolumeUp, contentDescription = "Play", modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Listen", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                surah?.let { s ->
                  OutlinedButton(
                    onClick = { onOpenSurah(s) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Text("Read Surah & 8 Tafsirs", fontSize = 12.sp)
                  }
                }
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                  onClick = {
                    val nextOffset = dailyAyahOffset - 1
                    dailyAyahOffset = nextOffset
                    com.example.data.model.DailyGuidanceAndRecommendationEngine.saveDailyAyahOffset(context, nextOffset)
                  },
                  modifier = Modifier
                    .size(34.dp)
                    .testTag("daily_ayah_prev_btn")
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                    contentDescription = "Previous Daily Ayah"
                  )
                }
                IconButton(
                  onClick = {
                    val nextOffset = dailyAyahOffset + 1
                    dailyAyahOffset = nextOffset
                    com.example.data.model.DailyGuidanceAndRecommendationEngine.saveDailyAyahOffset(context, nextOffset)
                  },
                  modifier = Modifier
                    .size(34.dp)
                    .testTag("daily_ayah_next_btn")
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                    contentDescription = "Next Daily Ayah"
                  )
                }
                IconButton(
                  onClick = { onBookmarkAyah(ayah, surahName) },
                  modifier = Modifier.size(36.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark Ayah",
                    tint = GoldPrimary
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // Step-by-Step Guided Learning Path (From Daily Verse to All Verses, Hadith, Salah & Seerah)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { showStepByStepPath = !showStepByStepPath }
                .testTag("home_step_by_step_learning_header"),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "🎯 Step-by-Step Guided Learning Path (${completedLearningSteps.size}/5 Completed)",
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldPrimary
                  )
                )
                Text(
                  text = "مرحلہ وار اسلامی رہنمائی: ایمان، نورانی قاعدہ، نماز، احادیث اور تفاسیر",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 23.sp,
                    textDirection = TextDirection.Rtl,
                    textAlign = TextAlign.Right,
                    color = GoldPrimary
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }

            if (showStepByStepPath) {
              Spacer(modifier = Modifier.height(8.dp))
              val activeLearningPath = if (isShiaHomeActive) {
                com.example.data.model.DailyGuidanceAndRecommendationEngine.shiaStepByStepLearningPath
              } else {
                com.example.data.model.DailyGuidanceAndRecommendationEngine.stepByStepLearningPath
              }
              Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("home_step_by_step_learning_section")
              ) {
                activeLearningPath.forEach { step ->
                  val isStepDone = completedLearningSteps.contains(step.id)
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isStepDone) EmeraldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(
                      1.dp,
                      if (isStepDone) EmeraldPrimary else GoldPrimary.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("home_learning_step_${step.stepNumber}")
                  ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = step.stageTitleEnglish,
                          style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldPrimary
                          ),
                          modifier = Modifier.weight(1f)
                        )
                        Surface(
                          shape = RoundedCornerShape(6.dp),
                          color = if (isStepDone) EmeraldPrimary else GoldPrimary.copy(alpha = 0.2f),
                          modifier = Modifier.clickable {
                            completedLearningSteps =
                              com.example.data.model.DailyGuidanceAndRecommendationEngine.toggleLearningStepCompleted(
                                context,
                                step.id
                              )
                          }
                        ) {
                          Text(
                            text = if (isStepDone) "✓ Completed" else "Mark Done",
                            style = MaterialTheme.typography.labelSmall.copy(
                              fontWeight = FontWeight.Bold,
                              color = if (isStepDone) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                          )
                        }
                      }
                      Text(
                        text = step.stageTitleUrdu,
                        style = MaterialTheme.typography.bodySmall.copy(
                          fontFamily = UrduNastaliqFontFamily,
                          fontSize = 13.sp,
                          lineHeight = 23.sp,
                          fontWeight = FontWeight.Bold,
                          textDirection = TextDirection.Rtl,
                          textAlign = TextAlign.Right,
                          color = GoldPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                      )
                      Spacer(modifier = Modifier.height(3.dp))
                      Text(
                        text = step.guidanceSummaryEnglish,
                        style = MaterialTheme.typography.bodySmall.copy(
                          fontSize = 12.sp,
                          lineHeight = 17.sp,
                          color = MaterialTheme.colorScheme.onSurface
                        )
                      )
                      Text(
                        text = step.guidanceSummaryUrdu,
                        style = MaterialTheme.typography.bodySmall.copy(
                          fontFamily = UrduNastaliqFontFamily,
                          fontSize = 13.sp,
                          lineHeight = 23.sp,
                          textDirection = TextDirection.Rtl,
                          textAlign = TextAlign.Right,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                      )
                      Spacer(modifier = Modifier.height(6.dp))
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = "${step.quranReference} • ${step.hadithReference}",
                          style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                          ),
                          modifier = Modifier.weight(1f)
                        )
                        FilledTonalButton(
                          onClick = {
                            when (step.actionTargetType) {
                              "QURAN_SURAH" -> {
                                val targetSurah = surahs.find { it.number == step.quranSurahNumber }
                                if (targetSurah != null) onOpenSurah(targetSurah) else onNavigateToQuran()
                              }
                              "LEARN_ISLAM" -> onNavigateToLearnIslam()
                              "HADITH" -> onNavigateToHadith()
                              "DUAS" -> onNavigateToDailyDuas()
                              "LIBRARY_HISTORY" -> onNavigateToLibraryTab(0)
                              "QAIDA_PDF" -> onNavigateToNooraniQaidaPdf()
                              else -> onNavigateToQuran()
                            }
                          },
                          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                          shape = RoundedCornerShape(8.dp)
                        ) {
                          Text(step.actionLabelEnglish, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
    }

    // Intelligent Prophetic Sunnah / Ahl al-Bayt Hadith & Islamic Discovery Recommendations Card
    item {
      val isShiaRecMode = homeTraditionSectionMode == com.example.ui.components.HomeTraditionSectionMode.SHIA_SECTION_ONLY
      val isFridayOrThuEvening = sunnahRemindersState.calculatedDayStatus.isFridayToday ||
        sunnahRemindersState.calculatedDayStatus.isThursdayAfterMaghribJumuahActive
      val allRecs = remember(
        prayerConfig.lastReadSurahNumber,
        prayerConfig.lastReadVerseNumber,
        isFridayOrThuEvening,
        recRotationOffset,
        isShiaRecMode
      ) {
        com.example.data.model.DailyGuidanceAndRecommendationEngine.buildIntelligentRecommendations(
          context = context,
          lastReadSurahNumber = prayerConfig.lastReadSurahNumber,
          lastReadSurahNameEn = prayerConfig.lastReadSurahNameEn,
          lastReadVerseNumber = prayerConfig.lastReadVerseNumber,
          isFridayOrThursdayEvening = isFridayOrThuEvening,
          rotationOffset = recRotationOffset,
          isShiaTradition = isShiaRecMode
        )
      }
      val visibleRecs = remember(allRecs, selectedRecCategory) {
        if (selectedRecCategory == "ALL") {
          allRecs
        } else {
          allRecs.filter { it.pillarCategory == selectedRecCategory }.ifEmpty { allRecs }
        }
      }

      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("home_intelligent_recommendations_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isShiaRecMode) {
                  "Recommended for You • Ahl al-Bayt (AS) Hadith & Guidance"
                } else {
                  "Recommended for You • Prophetic Sunnah & Guidance"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
              Text(
                text = if (isShiaRecMode) {
                  "کتبِ اربعہ (الکافی، من لا یحضرہ الفقیہ، تہذیب الاحکام)، صحیفہ سجادیہ اور قرآن سے منتخب رہنمائی"
                } else {
                  "سنتِ نبوی ﷺ، صحیح احادیث، قرآن اور اسلامی تاریخ سے منتخب علمی رہنمائی"
                },
                style = MaterialTheme.typography.bodySmall.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 13.sp,
                  lineHeight = 24.sp,
                  fontWeight = FontWeight.Bold,
                  textDirection = TextDirection.Rtl,
                  textAlign = TextAlign.Right,
                  color = GoldPrimary
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }
            OutlinedButton(
              onClick = {
                recRotationOffset =
                  com.example.data.model.DailyGuidanceAndRecommendationEngine.advanceRecommendationRotation(context)
              },
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("home_refresh_recommendations_btn")
            ) {
              Text("Next Guidance", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            val recTabs = listOf(
              "ALL" to "All Guidance",
              "SUNNAH_HADITH" to if (isShiaRecMode) "Ahl al-Bayt Hadith" else "Prophetic Sunnah",
              "QURAN_TAFSIR" to "Qur'an & Tafsir",
              "DUA_CHARACTER" to "Duas & Tajweed",
              "HISTORY_SEERAH" to "Seerah & History"
            )
            recTabs.forEach { (catKey, catLabel) ->
              FilterChip(
                selected = selectedRecCategory == catKey,
                onClick = { selectedRecCategory = catKey },
                label = { Text(catLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EmeraldPrimary,
                  selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("home_rec_filter_$catKey")
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            visibleRecs.forEach { rec ->
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.45f)),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("home_recommendation_item_${rec.id}")
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = rec.reasonBadgeEnglish,
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldPrimary
                      ),
                      modifier = Modifier.weight(1f)
                    )
                  }
                  Text(
                    text = rec.reasonBadgeUrdu,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontFamily = UrduNastaliqFontFamily,
                      fontSize = 12.sp,
                      lineHeight = 21.sp,
                      textDirection = TextDirection.Rtl,
                      textAlign = TextAlign.Right,
                      color = GoldPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = rec.titleEnglish,
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  )
                  Text(
                    text = rec.titleUrdu,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontFamily = UrduNastaliqFontFamily,
                      fontSize = 14.sp,
                      lineHeight = 25.sp,
                      fontWeight = FontWeight.Bold,
                      textDirection = TextDirection.Rtl,
                      textAlign = TextAlign.Right,
                      color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = rec.arabicSnippet,
                    style = MaterialTheme.typography.bodyLarge.copy(
                      fontFamily = AmiriFontFamily,
                      fontSize = 19.sp,
                      lineHeight = 34.sp,
                      textDirection = TextDirection.Rtl,
                      textAlign = TextAlign.Right,
                      color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = rec.englishSnippet,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontSize = 12.sp,
                      lineHeight = 18.sp,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = rec.urduSnippet,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontFamily = UrduNastaliqFontFamily,
                      fontSize = 13.sp,
                      lineHeight = 24.sp,
                      textDirection = TextDirection.Rtl,
                      textAlign = TextAlign.Right,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = rec.authenticReference,
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                      ),
                      modifier = Modifier.weight(1f)
                    )
                    Button(
                      onClick = {
                        when (rec.actionType) {
                          "QURAN_SURAH" -> {
                            val target = surahs.find { it.number == rec.targetSurahNumber }
                            if (target != null) onOpenSurah(target) else onNavigateToQuran()
                          }
                          "HADITH" -> onNavigateToHadith()
                          "DUAS" -> onNavigateToDailyDuas()
                          "LEARN_ISLAM" -> onNavigateToLearnIslam()
                          "QAIDA_PDF" -> onNavigateToNooraniQaidaPdf()
                          "LIBRARY_TAB" -> onNavigateToLibraryTab(rec.targetLibraryTab)
                          else -> onNavigateToQuran()
                        }
                      },
                      colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = Color.White
                      ),
                      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                      shape = RoundedCornerShape(10.dp)
                    ) {
                      Text(rec.actionLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Daily Authentic Hadith Card (Sunni / Combined Home Screen)
    if (homeTraditionSectionMode != com.example.ui.components.HomeTraditionSectionMode.SHIA_SECTION_ONLY) {
      item {
        resolvedDailyHadith.let { hadith ->
          ElevatedCard(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("daily_hadith_card"),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Prophetic Tradition",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
              SahihGradeBadge(grade = hadith.grade)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "${hadith.reference} • ${hadith.inBookReference}",
              style = MaterialTheme.typography.labelMedium.copy(
                color = GoldPrimary,
                fontWeight = FontWeight.Bold
              )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Narrator
            Text(
              text = "Narrated: ${hadith.narrator}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
              )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Arabic Hadith in bundled Amiri Naskh font
            Text(
              text = hadith.arabicText,
              style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = AmiriFontFamily,
                fontSize = 20.sp,
                lineHeight = 36.sp,
                textDirection = TextDirection.Rtl,
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )

            if (hadith.transliteration.isNotBlank()) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = hadith.transliteration,
                style = MaterialTheme.typography.bodySmall.copy(
                  fontSize = 12.sp,
                  fontStyle = FontStyle.Italic,
                  color = MaterialTheme.colorScheme.secondary
                )
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // English Hadith
            Text(
              text = hadith.englishText,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp
              )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Urdu Hadith
            Text(
              text = hadith.urduText,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 15.sp,
                lineHeight = 29.sp,
                textDirection = TextDirection.Rtl,
                textAlign = TextAlign.Right,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedButton(
                onClick = onNavigateToHadith,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Explore 7 Hadith Books", fontSize = 12.sp)
              }

              Row {
                IconButton(
                  onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Hadith", "${hadith.englishText}\n\n[${hadith.reference}]")
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Hadith copied to clipboard", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.size(36.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                IconButton(
                  onClick = { onBookmarkHadith(hadith) },
                  modifier = Modifier.size(36.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = GoldPrimary
                  )
                }
              }
            }
          }
        }
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }

    // Wisdom & Quotations Section — Authentic Islamic Wisdom Engine (Prophetic, Ahl al-Bayt, Early Sahabah & Other Sahabah)
    item {
      val availableHadithBooks = remember(reflectionCategory) {
        ReflectionDataProvider.getAvailableHadithBooks(reflectionCategory)
      }
      val filteredReflections = remember(
        reflectionCategory,
        reflectionAttributionFilter,
        reflectionSearchQuery,
        reflectionBookFilter,
        reflectionRelationshipFilter,
        reflectionBookmarkedOnly,
        bookmarkedWisdomIds
      ) {
        ReflectionDataProvider.filterItems(
          category = reflectionCategory,
          attributionFilter = reflectionAttributionFilter,
          searchQuery = reflectionSearchQuery,
          hadithBookFilter = reflectionBookFilter,
          relationshipFilter = reflectionRelationshipFilter,
          bookmarkedOnly = reflectionBookmarkedOnly,
          bookmarkedIds = bookmarkedWisdomIds
        )
      }
      val safeIndex = if (filteredReflections.isNotEmpty()) {
        reflectionIndex.coerceIn(0, filteredReflections.lastIndex)
      } else {
        0
      }
      val item = filteredReflections.getOrNull(safeIndex)

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("wisdom_and_quotations_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                text = "Wisdom & Quotations (Prophetic, Ahl al-Bayt & Sahabah)",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
              Text(
                text = "Authentic Islamic Wisdom Engine • Verified Hadith collections, historical chronology & separate grading.",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              )
            }
            OutlinedButton(
              onClick = {
                reflectionIndex = ReflectionDataProvider.discoverNextUnseenIndex(
                  context = context,
                  candidates = filteredReflections,
                  currentIndex = safeIndex
                )
              },
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("wisdom_discover_new_btn")
            ) {
              Icon(
                Icons.Default.Explore,
                contentDescription = "Discover Unseen Verified Wisdom",
                modifier = Modifier.size(14.dp),
                tint = EmeraldPrimary
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Discover New", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Four Clearly Separated Category Selector Chips
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            FilterChip(
              selected = reflectionCategory == ReflectionCategory.PROPHETIC_GUIDANCE,
              onClick = {
                reflectionCategory = ReflectionCategory.PROPHETIC_GUIDANCE
                reflectionBookFilter = null
                reflectionIndex = 0
              },
              label = { Text("Prophet Muhammad ﷺ", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("wisdom_category_prophetic_chip")
            )
            FilterChip(
              selected = reflectionCategory == ReflectionCategory.AHLUL_BAYT_WISDOM,
              onClick = {
                reflectionCategory = ReflectionCategory.AHLUL_BAYT_WISDOM
                reflectionBookFilter = null
                reflectionIndex = 0
              },
              label = { Text("Ahl al-Bayt (AS)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GoldPrimary,
                selectedLabelColor = Color.Black
              ),
              modifier = Modifier.testTag("wisdom_category_ahlul_bayt_chip")
            )
            FilterChip(
              selected = reflectionCategory == ReflectionCategory.COMPANIONS_WISDOM,
              onClick = {
                reflectionCategory = ReflectionCategory.COMPANIONS_WISDOM
                reflectionBookFilter = null
                reflectionIndex = 0
              },
              label = { Text("Early Sahabah (RA)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("wisdom_category_sahabah_chip")
            )
            FilterChip(
              selected = reflectionCategory == ReflectionCategory.OTHER_SAHABAH_WISDOM,
              onClick = {
                reflectionCategory = ReflectionCategory.OTHER_SAHABAH_WISDOM
                reflectionBookFilter = null
                reflectionIndex = 0
              },
              label = { Text("Other Sahabah (RA)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary.copy(alpha = 0.85f),
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("wisdom_category_other_sahabah_chip")
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Category Subtitle & Historical Chronology Methodology Note
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
              Text(
                text = reflectionCategory.subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
              if (reflectionCategory == ReflectionCategory.COMPANIONS_WISDOM) {
                Text(
                  text = "Historical Chronology (As-Sabiqun al-Awwalun): 1. Sayyidah Khadijah (RA) • 2. Imam Ali ibn Abi Talib (AS) • 3. Zayd ibn Harithah (RA) • 4. Abu Bakr (RA) • 5. Abu Dharr al-Ghifari (RA) • 6. Ammar ibn Yasir (RA) • 7. Miqdad ibn Amr (RA) • 8. Bilal (RA) • 9. Ibn Mas'ud (RA) • 10. Ja'far al-Tayyar (RA) • 11. Salman al-Farsi (RA) • 12. Umm Salamah (RA) • 13. Jabir (RA) • 14. Zayd ibn Arqam (RA) • 15. Hudhayfah (RA).",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
              } else if (reflectionCategory == ReflectionCategory.OTHER_SAHABAH_WISDOM) {
                Text(
                  text = "Companions who embraced Islam in subsequent Makkan and Madinan phases are presented in their verified historical chronology without placing later converts ahead of the earliest vanguard.",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Historical Relationship & Scholarly Focus Filter Chips
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            HistoricalRelationshipTag.entries.forEach { relTag ->
              FilterChip(
                selected = reflectionRelationshipFilter == relTag,
                onClick = {
                  reflectionRelationshipFilter = relTag
                  reflectionIndex = 0
                },
                label = { Text(relTag.label, fontSize = 10.sp) },
                modifier = Modifier.testTag("wisdom_rel_filter_${relTag.name.lowercase()}")
              )
            }
            FilterChip(
              selected = reflectionBookmarkedOnly,
              onClick = {
                reflectionBookmarkedOnly = !reflectionBookmarkedOnly
                reflectionIndex = 0
              },
              label = { Text("Saved (${bookmarkedWisdomIds.size})", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
              modifier = Modifier.testTag("wisdom_bookmarks_filter_chip")
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Hadith Book Filter Chips (across all 26+ integrated Hadith collections)
          if (availableHadithBooks.isNotEmpty()) {
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              FilterChip(
                selected = reflectionBookFilter == null,
                onClick = {
                  reflectionBookFilter = null
                  reflectionIndex = 0
                },
                label = { Text("All Hadith Books (${availableHadithBooks.size})", fontSize = 10.sp) },
                modifier = Modifier.testTag("wisdom_book_filter_all")
              )
              availableHadithBooks.take(10).forEach { bookName ->
                FilterChip(
                  selected = reflectionBookFilter == bookName,
                  onClick = {
                    reflectionBookFilter = if (reflectionBookFilter == bookName) null else bookName
                    reflectionIndex = 0
                  },
                  label = { Text(bookName, fontSize = 10.sp) }
                )
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
          }

          // Attribution Status Filter Chips (Verified vs Disputed vs Unattributed Popular Saying)
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            FilterChip(
              selected = reflectionAttributionFilter == null,
              onClick = {
                reflectionAttributionFilter = null
                reflectionIndex = 0
              },
              label = { Text("All Attributions", fontSize = 10.sp) },
              modifier = Modifier.testTag("wisdom_attr_filter_all")
            )
            WisdomAttributionStatus.entries.forEach { status ->
              FilterChip(
                selected = reflectionAttributionFilter == status,
                onClick = {
                  reflectionAttributionFilter = status
                  reflectionIndex = 0
                },
                label = { Text(status.label, fontSize = 10.sp) },
                modifier = Modifier.testTag("wisdom_attr_filter_${status.name.lowercase()}")
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = reflectionSearchQuery,
            onValueChange = {
              reflectionSearchQuery = it
              reflectionIndex = 0
            },
            placeholder = {
              Text(
                "Search personality (e.g. Khadijah, Imam Ali, Salman, Abu Dharr, Ammar), book, or topic...",
                fontSize = 12.sp
              )
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("wisdom_search_input")
          )

          Spacer(modifier = Modifier.height(12.dp))

          if (item == null) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surface,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "No quotations in ${reflectionCategory.displayTitle} match the current filter. Try clearing the search or selecting 'All Verified Narrations'.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(14.dp)
              )
            }
          } else {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surface
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                // Separate Authenticity Grading, Attribution Verification, and Narration Type Badges
                FlowRow(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GoldPrimary.copy(alpha = 0.16f)
                  ) {
                    Text(
                      text = item.theme,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                      )
                    )
                  }
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                  ) {
                    Text(
                      text = item.narrationType.label,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                      )
                    )
                  }
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (item.attributionStatus) {
                      WisdomAttributionStatus.VERIFIED_ATTRIBUTION -> EmeraldPrimary.copy(alpha = 0.14f)
                      WisdomAttributionStatus.DISPUTED_ATTRIBUTION -> GoldPrimary.copy(alpha = 0.22f)
                      WisdomAttributionStatus.UNATTRIBUTED_POPULAR_SAYING -> MaterialTheme.colorScheme.errorContainer
                    }
                  ) {
                    Text(
                      text = item.attributionStatus.label,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = when (item.attributionStatus) {
                          WisdomAttributionStatus.VERIFIED_ATTRIBUTION -> EmeraldPrimary
                          WisdomAttributionStatus.DISPUTED_ATTRIBUTION -> GoldPrimary
                          WisdomAttributionStatus.UNATTRIBUTED_POPULAR_SAYING -> MaterialTheme.colorScheme.error
                        }
                      )
                    )
                  }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = "Speaker / Transmitter: ${item.speaker}",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                )

                Text(
                  text = item.title,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )

                if (item.chronologyEraAndOrderNote.isNotBlank()) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = item.chronologyEraAndOrderNote,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.SemiBold,
                      color = EmeraldPrimary
                    )
                  )
                }

                if (item.relationshipToProphetAndAhlAlBayt.isNotBlank()) {
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "Relationship to Prophet ﷺ & Ahl al-Bayt: ${item.relationshipToProphetAndAhlAlBayt}",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = item.arabicOrOriginalText,
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = AmiriFontFamily,
                    fontSize = 19.sp,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.Right
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = item.translationEnglish,
                  style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                  text = item.translationUrdu,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    textAlign = TextAlign.Right,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  ),
                  modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = "Book Reference: ${item.sourceReference}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
                Text(
                  text = "Authenticity Grading: ${item.authenticityGrading}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
                if (item.contextNotes.isNotBlank()) {
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = item.contextNotes,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontStyle = FontStyle.Italic,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  )
                }
                if (item.historicalDisputeNote.isNotBlank()) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text(
                      text = item.historicalDisputeNote,
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      ),
                      modifier = Modifier.padding(8.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                      onClick = { if (safeIndex > 0) reflectionIndex = safeIndex - 1 },
                      enabled = safeIndex > 0,
                      modifier = Modifier.size(30.dp)
                    ) {
                      Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = "Previous Quotation")
                    }
                    Text(
                      text = "${safeIndex + 1} / ${filteredReflections.size}",
                      style = MaterialTheme.typography.labelSmall
                    )
                    IconButton(
                      onClick = {
                        if (safeIndex < filteredReflections.lastIndex) {
                          val nextIdx = safeIndex + 1
                          reflectionIndex = nextIdx
                          ReflectionDataProvider.markWisdomSeen(context, filteredReflections[nextIdx].id)
                        }
                      },
                      enabled = safeIndex < filteredReflections.lastIndex,
                      modifier = Modifier.size(30.dp)
                    ) {
                      Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "Next Quotation")
                    }
                  }

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    val isBookmarked = bookmarkedWisdomIds.contains(item.id)
                    IconButton(
                      onClick = {
                        bookmarkedWisdomIds = ReflectionDataProvider.toggleWisdomBookmark(context, item.id)
                        val msg = if (bookmarkedWisdomIds.contains(item.id)) {
                          "Saved to Wisdom Bookmarks"
                        } else {
                          "Removed from Wisdom Bookmarks"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                      },
                      modifier = Modifier
                        .size(32.dp)
                        .testTag("wisdom_bookmark_btn")
                    ) {
                      Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmarks else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark Wisdom Quotation",
                        tint = if (isBookmarked) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                      )
                    }
                    IconButton(
                      onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val shareBody = "${item.arabicOrOriginalText}\n\n${item.translationEnglish}\n\n${item.translationUrdu}\n\nNarrator: ${item.speaker}\nSource: ${item.sourceReference} | Grading: ${item.authenticityGrading} (${item.attributionStatus.label})"
                        clipboard.setPrimaryClip(ClipData.newPlainText("Wisdom Quotation", shareBody))
                        Toast.makeText(context, "Quotation copied to clipboard", Toast.LENGTH_SHORT).show()
                      },
                      modifier = Modifier.size(32.dp)
                    ) {
                      Icon(Icons.Default.ContentCopy, contentDescription = "Copy Quotation", modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                      onClick = {
                        val shareBody = "${item.arabicOrOriginalText}\n\n${item.translationEnglish}\n\n${item.translationUrdu}\n\nNarrator: ${item.speaker}\nSource: ${item.sourceReference} | Grading: ${item.authenticityGrading} (${item.attributionStatus.label})\nShared via Bayan al-Huda"
                        val sendIntent = Intent().apply {
                          action = Intent.ACTION_SEND
                          putExtra(Intent.EXTRA_TEXT, shareBody)
                          type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Wisdom Quotation"))
                      },
                      modifier = Modifier.size(32.dp)
                    ) {
                      Icon(Icons.Default.Share, contentDescription = "Share Quotation", modifier = Modifier.size(16.dp))
                    }
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }

    // Quick Jump Grid (Essential Surahs from the 114 Offline Surahs)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Quick Jump to Surahs (All 114 Offline)",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        )
        OutlinedButton(
          onClick = onNavigateToQuran,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("All 114 Surahs", fontSize = 11.sp)
        }
      }
      Spacer(modifier = Modifier.height(10.dp))

      val quickSurahs = surahs.filter { it.number in listOf(1, 2, 18, 36, 55, 67, 112, 114) }

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        quickSurahs.forEach { surah ->
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onOpenSurah(surah) },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "${surah.number}",
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                  ),
                  modifier = Modifier.width(32.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = surah.nameEnglish,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "${surah.totalVerses} Verses • ${surah.revelationType} • Offline + 14 Tafsirs",
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }

              Spacer(modifier = Modifier.width(10.dp))

              Text(
                text = surah.nameArabic,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontFamily = ScheherazadeFontFamily,
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold,
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
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalculatedFridayAndDailySunnahHomeCard(
  remindersState: SunnahRemindersUiState,
  surahs: List<SurahEntity>,
  onSelectPreviewDayOfWeek: (Int) -> Unit,
  onSendCalculatedDayNotificationNow: (Int?) -> Unit,
  onUpdateFridayBathReminder: (Boolean, String) -> Unit,
  onUpdateDailyCalculatedReminder: (Boolean, String) -> Unit,
  onToggleFridayChecklistItem: (String) -> Unit,
  onIncrementFridaySalawat: (Int) -> Unit,
  onOpenSurah: (SurahEntity) -> Unit,
  onNavigateToDailyDuas: () -> Unit,
  isShiaTradition: Boolean = false
) {
  val context = LocalContext.current
  val calcStatus = remindersState.calculatedDayStatus
  val selectedDow = remindersState.selectedPreviewDayOfWeek
  val selectedUpcomingItem = calcStatus.upcoming7DaysSchedule.find {
    it.calendarDayOfWeek == selectedDow
  } ?: calcStatus.upcoming7DaysSchedule.first()
  val selectedSpec = selectedUpcomingItem.weekdaySpec
  val isFridayBathDone = remindersState.completedFridayChecklistIds.contains("friday_ghusl_bath")
  val completedFridayCount = DailyDuasDataProvider.fridaySunnahChecklist.count {
    remindersState.completedFridayChecklistIds.contains(it.id)
  }
  val totalFridayCount = DailyDuasDataProvider.fridaySunnahChecklist.size

  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("home_calculated_friday_and_days_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      // 1. Top Header Banner: Calculated Friday & Daily Sunnah / Ahl al-Bayt Practice Reminder
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = EmeraldPrimary
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isShiaTradition) {
                  "Calculated Friday & 7-Day Ahl al-Bayt (AS) Practices"
                } else {
                  "Calculated Friday & Daily Sunnah Reminder"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              )
              Text(
                text = "${calcStatus.gregorianReadable} • ${calcStatus.hijriInfo.formattedReadable}",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = GoldPrimary,
                  fontWeight = FontWeight.SemiBold
                )
              )
            }
            Text(
              text = calcStatus.todayWeekdaySpec.arabicDayName,
              style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = ScheherazadeFontFamily,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Today's Calculated Status Pills
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (calcStatus.isFridayToday || calcStatus.isThursdayAfterMaghribJumuahActive) GoldPrimary else Color.White.copy(alpha = 0.16f)
            ) {
              Text(
                text = calcStatus.nextFridayDateReadable,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (calcStatus.isFridayToday || calcStatus.isThursdayAfterMaghribJumuahActive) Color(0xFF102018) else Color.White,
                  fontWeight = FontWeight.Bold
                )
              )
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color.White.copy(alpha = 0.16f)
            ) {
              Text(
                text = calcStatus.nextSunnahFastSummary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color.White,
                  fontWeight = FontWeight.SemiBold
                )
              )
            }

            calcStatus.hijriInfo.specialOccasionBadge?.let { badge ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = GoldPrimary.copy(alpha = 0.25f)
              ) {
                Text(
                  text = badge,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Auto-Send Calculated Daily & Friday Notifications (${remindersState.dailyCalculatedNotifTime24h})",
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.SemiBold
              ),
              modifier = Modifier.weight(1f)
            )
            Switch(
              checked = remindersState.dailyCalculatedNotifEnabled,
              onCheckedChange = { enabled ->
                onUpdateDailyCalculatedReminder(enabled, remindersState.dailyCalculatedNotifTime24h)
              },
              colors = SwitchDefaults.colors(
                checkedThumbColor = EmeraldPrimary,
                checkedTrackColor = GoldPrimary
              ),
              modifier = Modifier.testTag("home_calculated_day_notif_switch")
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 2. Special Friday (Yawm al-Jumu'ah & Thursday After Maghrib Laylat al-Jumu'ah) Highlight Box on Home Screen
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("home_friday_special_highlight_box"),
        shape = RoundedCornerShape(14.dp),
        color = GoldPrimary.copy(alpha = 0.14f)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = "Friday Ghusl",
                tint = EmeraldPrimary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(
                  text = when {
                    calcStatus.isFridayToday -> "Blessed Friday (Jumu'ah) Special Reminder (TODAY)"
                    calcStatus.isThursdayAfterMaghribJumuahActive -> "🌙 Laylatul Jumu'ah Active (After Thursday Maghrib): Friday Sunnahs"
                    else -> "Special Friday (Jumu'ah) Bath & Sunnah Preparation (Starts Thu Maghrib)"
                  },
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
                Text(
                  text = if (isShiaTradition) {
                    "Al-Kafi Vol. 3, p. 41 (Sahih: Friday Ghusl is Sunnah Mu'akkadah) • Checklist: $completedFridayCount/$totalFridayCount"
                  } else {
                    "${calcStatus.nextFridayBathAlertSummary} • Checklist: $completedFridayCount/$totalFridayCount"
                  },
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
              modifier = Modifier.testTag("home_friday_bath_switch")
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            FilledTonalButton(
              onClick = { onToggleFridayChecklistItem("friday_ghusl_bath") },
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("home_friday_bath_done_btn")
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isFridayBathDone) "Friday Ghusl Done ✓" else "Mark Friday Ghusl",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }

            FilledTonalButton(
              onClick = {
                surahs.find { it.number == 18 }?.let { onOpenSurah(it) }
              },
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("home_friday_open_kahf_btn")
            ) {
              Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Surah Al-Kahf (18)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            FilledTonalButton(
              onClick = { onIncrementFridaySalawat(1) },
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("home_friday_salawat_btn")
            ) {
              Text(
                text = "Salawat +1 (${remindersState.fridaySalawatCount}/100)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }

            OutlinedButton(
              onClick = {
                onSendCalculatedDayNotificationNow(Calendar.FRIDAY)
                Toast.makeText(
                  context,
                  "Sent Friday (Jumu'ah) Ghusl, Surah Al-Kahf & Salawat reminder notification",
                  Toast.LENGTH_SHORT
                ).show()
              },
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("home_send_friday_bath_notif_btn")
            ) {
              Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Notify Friday Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 3. 7-Day Calculated Schedule Selector (Today + Next 6 Days)
      Text(
        text = "Calculated 7-Day Schedule (Tap Friday or Any Day to View & Notify):",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      )
      Spacer(modifier = Modifier.height(6.dp))

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("home_7day_calculated_row")
      ) {
        calcStatus.upcoming7DaysSchedule.forEach { dayItem ->
          val isSelected = selectedDow == dayItem.calendarDayOfWeek
          val chipLabel = buildString {
            append(dayItem.weekdaySpec.shortDayLabel)
            if (dayItem.isToday) append(" (Today)")
            else if (dayItem.isFridayJumuah) append(" (Jumu'ah)")
          }
          FilterChip(
            selected = isSelected,
            onClick = { onSelectPreviewDayOfWeek(dayItem.calendarDayOfWeek) },
            label = {
              Text(
                text = chipLabel,
                fontSize = 11.sp,
                fontWeight = if (dayItem.isToday || dayItem.isFridayJumuah) FontWeight.Bold else FontWeight.Medium
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = if (dayItem.isFridayJumuah) GoldPrimary else EmeraldPrimary,
              selectedLabelColor = if (dayItem.isFridayJumuah) Color.Black else Color.White
            ),
            modifier = Modifier.testTag("home_calculated_day_chip_${dayItem.calendarDayOfWeek}")
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 4. Selected / Today Calculated Day Detailed Sunnah Card
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("home_selected_calculated_day_detail"),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${selectedSpec.dayNameEnglish}: ${selectedSpec.islamicDayTitle}",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
              Text(
                text = "${selectedUpcomingItem.gregorianFullDate} • ${selectedUpcomingItem.hijriReadable}",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = GoldPrimary,
                  fontWeight = FontWeight.Bold
                )
              )
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = EmeraldPrimary.copy(alpha = 0.14f)
            ) {
              Text(
                text = selectedUpcomingItem.specialBadge,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = EmeraldPrimary,
                  fontWeight = FontWeight.Bold
                )
              )
            }
          }

          // Tradition-Specific Weekday Practice Content & Authentic Hadith Source
          val shiaDayPractice = remember(selectedSpec.calendarDayOfWeek, isShiaTradition) {
            if (!isShiaTradition) null else when (selectedSpec.calendarDayOfWeek) {
              Calendar.FRIDAY -> Triple(
                "Friday (Yawm al-Jumu'ah): Ghusl al-Jumu'ah, Salawat, Dua Kumayl & Dua al-Nudba",
                "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَآلِ مُحَمَّدٍ وَعَجِّلْ فَرَجَهُمْ • الْغُسْلُ يَوْمَ الْجُمُعَةِ سُنَّةٌ وَاجِبَةٌ",
                Triple(
                  "In Al-Kafi and Man La Yahduruhu al-Faqih, Imam Ja'far al-Sadiq (AS) taught that Friday Ghusl is an emphasized Sunnah (Sunnah Mu'akkadah) purifying from Friday to Friday, alongside abundant Salawat ('Allahumma salli ala Muhammad wa Aali Muhammad'), Surah Al-Jumu'ah & Al-Kahf, Dua Kumayl on Thursday night, and Dua al-Nudba on Friday morning.",
                  "الکافی اور من لا یحضرہ الفقیہ کی صحیح احادیث میں امام جعفر صادق علیہ السلام نے فرمایا کہ غسلِ جمعہ سنتِ مؤکدہ ہے اور ایک جمعہ سے دوسرے جمعہ تک پاکیزگی و کفارہ ہے، نیز شبِ جمعہ دعائے کمیل، صبحِ جمعہ دعائے ندبہ، سورۃ الجمعہ و سورۃ الکہف اور کثرتِ صلوات بر محمد و آلِ محمد ﷺ کی تاکید فرمائی۔",
                  "Al-Kafi Vol. 3, Kitab al-Taharah, p. 41, Hadith 1 (Sahih) & Vol. 3, p. 425 • Man La Yahduruhu al-Faqih Vol. 1, p. 112 • Misbah al-Mutahajjid, p. 844"
                )
              )
              Calendar.SATURDAY -> Triple(
                "Saturday (Yawm al-Sabt): Day of Prophet Muhammad ﷺ & Silat al-Rahim",
                "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَآلِ مُحَمَّدٍ • يَوْمُ السَّبْتِ لَنَا وَيَوْمُ الأَحَدِ لِشِيعَتِنَا",
                Triple(
                  "Saturday is dedicated in classical Shia devotional manuals (Misbah al-Mutahajjid & Mafatih al-Jinan) to the remembrance and Ziyarah of the Seal of Prophets Muhammad ﷺ, renewing sincerity (Ikhlas), and strengthening family bonds (Silat al-Rahim).",
                  "مصباح المتہجد اور مفاتیح الجنان کے مطابق ہفتہ کا دن خاتم النبیین حضرت محمد مصطفیٰ ﷺ سے منسوب ہے؛ اس دن درود و سلام، تسبیحِ فاطمہ زہراؑ اور صلہ رحمی کی خاص فضیلت وارد ہوئی ہے۔",
                  "Al-Kafi Vol. 2, Bab Silat al-Rahim, p. 150 (Sahih) • Misbah al-Mutahajjid of Shaykh al-Tusi • Bihar al-Anwar Vol. 87"
                )
              )
              Calendar.SUNDAY -> Triple(
                "Sunday (Yawm al-Ahad): Day of Imam Ali (AS) & Sayyidah Fatimah al-Zahra (SA)",
                "السَّلَامُ عَلَيْكَ يَا أَمِيرَ الْمُؤْمِنِينَ • السَّلَامُ عَلَيْكِ يَا فَاطِمَةُ الزَّهْرَاءُ سَيِّدَةَ نِسَاءِ الْعَالَمِينَ",
                Triple(
                  "Sunday is associated in Shia devotional literature with Amir al-Mu'minin Imam Ali ibn Abi Talib (AS) and Sayyidah Fatimah al-Zahra (SA), emphasizing justice, truthfulness, charity to the needy (Surah Al-Insan 76:8), and reciting Tasbih al-Zahra.",
                  "اتوار کا دن امیر المؤمنین حضرت علی بن ابی طالب علیہ السلام اور سیدۃ النساء العالمین حضرت فاطمہ زہرا سلام اللّٰہ علیہا کی یاد، عدل و انصاف، مساکین و یتامیٰ کی مدد (سورۃ الدہر) اور تسبیحِ زہراؑ کے لیے مخصوص ہے۔",
                  "Al-Kafi Vol. 1, p. 458 & Vol. 3, p. 343 (Sahih) • Al-Sahifa al-Sajjadiyya • Mafatih al-Jinan (Weekday Supplications)"
                )
              )
              Calendar.MONDAY -> Triple(
                "Monday (Yawm al-Ithnayn): Day of Imam Hasan & Imam Husayn (AS) & Mustahabb Fasting",
                "الْحَسَنُ وَالْحُسَيْنُ سَيِّدَا شَبَابِ أَهْلِ الْجَنَّةِ",
                Triple(
                  "Monday is honoured with the remembrance of Imam Hasan al-Mujtaba and Imam Husayn Sayyid al-Shuhada (AS), alongside the recommended (Mustahabb) fast of Monday and Thursday documented in Al-Kafi and Man La Yahduruhu al-Faqih.",
                  "پیر کا دن حسنین کریمین (امام حسن مجتبیٰ اور امام حسین سید الشہداء علیہما السلام) سے منسوب ہے، اور الکافی و من لا یحضرہ الفقیہ کی احادیث میں پیر اور جمعرات کے مستحب روزے کی فضیلت بیان ہوئی ہے۔",
                  "Al-Kafi Vol. 4, Kitab al-Sawm, Bab Sawm al-Sunnah, p. 90, Hadith 1 • Man La Yahduruhu al-Faqih Vol. 2, p. 89"
                )
              )
              Calendar.TUESDAY -> Triple(
                "Tuesday (Yawm al-Thulatha'): Day of Imam Sajjad, Imam al-Baqir & Imam al-Sadiq (AS)",
                "اللَّهُمَّ بَلِّغْ بِإِيمَانِي أَكْمَلَ الْإِيمَانِ وَاجْعَلْ يَقِينِي أَفْضَلَ الْيَقِينِ",
                Triple(
                  "Tuesday highlights the scholarly and spiritual legacy of Imam Ali Zayn al-'Abidin, Imam Muhammad al-Baqir, and Imam Ja'far al-Sadiq (AS): seeking religious knowledge (Tafaqquh fi al-Din), reciting Al-Sahifa al-Sajjadiyya, and noble conduct.",
                  "منگل کا دن امام زین العابدینؑ، امام محمد باقرؑ اور امام جعفر صادقؑ کی علمی و روحانی میراث (تفقہ فی الدین، صحیفہ سجادیہ اور مکارمِ اخلاق) کے مطالعہ اور عمل کے لیے خاص اہمیت رکھتا ہے۔",
                  "Al-Kafi Vol. 1, Kitab Fadl al-'Ilm, p. 31, Hadith 1 (Sahih) • Al-Sahifa al-Sajjadiyya, Dua 20"
                )
              )
              Calendar.WEDNESDAY -> Triple(
                "Wednesday (Yawm al-Arbi'a'): Day of Imam al-Kazim, Imam al-Rida, Imam al-Jawad & Imam al-Hadi (AS)",
                "الْكَاظِمِينَ الْغَيْظَ وَالْعَافِينَ عَنِ النَّاسِ وَاللَّهُ يُحِبُّ الْمُحْسِنِينَ",
                Triple(
                  "Wednesday honours Imam Musa al-Kazim, Imam Ali al-Rida, Imam Muhammad al-Jawad, and Imam Ali al-Hadi (AS), focusing on restraining anger (Kazm al-Ghayz), patience, forgiveness, and deep reflection on the Qur'an.",
                  "بدھ کا دن امام موسیٰ کاظمؑ، امام علی رضاؑ، امام محمد تقی الجوادؑ اور امام علی نقی الہادیؑ سے منسوب ہے اور غصہ ضبط کرنے (کاظمین الغیظ)، حلم و بردباری اور تدبرِ قرآن کی تعلیم دیتا ہے۔",
                  "Al-Kafi Vol. 2, Bab Kazm al-Ghayz, p. 109, Hadith 1 (Sahih) • Uyun Akhbar al-Rida by Shaykh al-Saduq"
                )
              )
              else -> Triple(
                "Thursday (Yawm al-Khamis): Mustahabb Fast, Istighfar & Laylat al-Jumu'ah Preparation",
                "اللَّهُمَّ إِنِّي أَسْأَلُكَ بِرَحْمَتِكَ الَّتِي وَسِعَتْ كُلَّ شَيْءٍ",
                Triple(
                  "In Al-Kafi and Tahdhib al-Ahkam, Imam Ja'far al-Sadiq (AS) states that deeds are presented on Thursday; fasting on Thursday is Mustahabb, and once Thursday Maghrib enters (Laylat al-Jumu'ah), reciting Dua Kumayl, abundant Salawat, and Istighfar is highly recommended.",
                  "الکافی اور تہذیب الاحکام کی صحیح احادیث کے مطابق جمعرات کا مستحب روزہ، استغفار اور غروبِ جمعرات (شبِ جمعہ) کے بعد دعائے کمیل، سورۃ الجمعہ اور کثرتِ درود بر محمد و آلِ محمد ﷺ مسنون ہے۔",
                  "Al-Kafi Vol. 4, p. 90 & Vol. 3, p. 414 (Sahih) • Misbah al-Mutahajjid of Shaykh al-Tusi, p. 844 (Dua Kumayl)"
                )
              )
            }
          }

          val activeDayTitle = shiaDayPractice?.first ?: selectedSpec.primaryReminderTitle
          val activeArabicSnippet = shiaDayPractice?.second ?: selectedSpec.arabicHadithSnippet
          val activeEnglishGuidance = shiaDayPractice?.third?.first ?: selectedSpec.englishGuidance
          val activeUrduGuidance = shiaDayPractice?.third?.second ?: selectedSpec.urduGuidance
          val activeAuthenticSource = shiaDayPractice?.third?.third ?: selectedSpec.authenticReference
          val activeBullets = if (isShiaTradition) {
            listOf(
              "17 Rak'ahs Daily Obligatory (Wajib) + 34 Rak'ahs Daily Recommended (Nafilah) Prayers (Al-Kafi Vol. 3, p. 442)",
              "Tasbih of Sayyidah Fatimah al-Zahra (SA): 34x Allahu Akbar, 33x Alhamdulillah, 33x SubhanAllah after every Salah",
              "Abundant Salawat: Allahumma salli 'ala Muhammad wa Aali Muhammad (especially on Thursday night & Friday)",
              "Friday Ghusl (Ghusl al-Jumu'ah) between Fajr and Dhuhr (Al-Kafi Vol. 3, p. 41, Sahih)"
            )
          } else {
            selectedSpec.keySunnahBullets
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = activeDayTitle,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold
            )
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = activeArabicSnippet,
            style = MaterialTheme.typography.titleMedium.copy(
              fontFamily = AmiriFontFamily,
              fontSize = 19.sp,
              lineHeight = 31.sp,
              textAlign = TextAlign.Right,
              color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = activeEnglishGuidance,
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = 12.sp,
              lineHeight = 18.sp
            )
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = activeUrduGuidance,
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = 13.sp,
              lineHeight = 24.sp,
              textAlign = TextAlign.Right,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(6.dp))

          activeBullets.forEach { bullet ->
            Text(
              text = "• $bullet",
              style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurface
              ),
              modifier = Modifier.padding(vertical = 1.dp)
            )
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Authentic Source: $activeAuthenticSource",
            style = MaterialTheme.typography.labelSmall.copy(
              color = EmeraldPrimary,
              fontWeight = FontWeight.Bold
            )
          )
        }
      }

      if (remindersState.lastNotificationBannerMessage.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("home_calculated_notif_status_banner"),
          shape = RoundedCornerShape(10.dp),
          color = EmeraldPrimary.copy(alpha = 0.12f)
        ) {
          Text(
            text = "✓ ${remindersState.lastNotificationBannerMessage}",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              color = EmeraldPrimary,
              fontWeight = FontWeight.Bold
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 5. Action Buttons: Send Today's Notification / Notify Selected Day / Open All Reminders
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = {
            onSendCalculatedDayNotificationNow(selectedSpec.calendarDayOfWeek)
            Toast.makeText(
              context,
              "Sent ${selectedSpec.dayNameEnglish} Sunnah reminder notification",
              Toast.LENGTH_SHORT
            ).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          modifier = Modifier
            .weight(1f)
            .testTag("home_send_calculated_day_notif_btn"),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
        ) {
          Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Send ${selectedSpec.dayNameEnglish} Notification",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }

        OutlinedButton(
          onClick = onNavigateToDailyDuas,
          modifier = Modifier.testTag("home_open_all_reminders_btn"),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
        ) {
          Text("All Duas & Reminders", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
