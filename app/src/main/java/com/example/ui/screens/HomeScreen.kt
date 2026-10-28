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
import com.example.data.model.ReflectionCategory
import com.example.data.model.ReflectionDataProvider
import com.example.data.model.WisdomAttributionStatus
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
  onNavigateToDownloads: () -> Unit = {},
  onNavigateToEvidence: () -> Unit = {},
  onNavigateToQibla: () -> Unit = {},
  onNavigateToDailyDuas: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val appStrings = com.example.ui.localization.LocalAppStrings.current
  var reflectionCategory by remember { mutableStateOf(ReflectionCategory.PROPHETIC_GUIDANCE) }
  var reflectionAttributionFilter by remember { mutableStateOf<WisdomAttributionStatus?>(null) }
  var reflectionSearchQuery by remember { mutableStateOf("") }
  var reflectionIndex by remember { mutableIntStateOf(0) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(vertical = 12.dp)
  ) {
    // Sacred Bismillah Greeting Header
    item {
      BismillahHeader()
      Spacer(modifier = Modifier.height(12.dp))
    }

    // Resume Quran Reading Progress Tracker Card
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

    // Five Daily Salah Times, Azan & Special 15-Minute Fajr Wake-Up Alarm
    item {
      SalahAndFajrAlarmCard(
        schedule = prayerSchedule,
        config = prayerConfig,
        alarmPlaybackState = alarmPlaybackState,
        onUpdateConfig = onUpdatePrayerConfig,
        onRefreshSchedule = onRefreshPrayerSchedule,
        onTestAzanOrFajrAlarm = onTestAzanOrFajrAlarm,
        onStopActiveAlarm = onStopActiveAlarm,
        onSnoozeActiveAlarm = onSnoozeActiveAlarm,
        onOpenQiblaCompass = onNavigateToQibla
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Specially Present on Home Screen: Calculated Friday & All Other Days Sunnah & Notification Card
    item {
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
      Spacer(modifier = Modifier.height(16.dp))
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

    // Daily Quranic Ayah Card
    item {
      dailyAyah?.let { ayah ->
        val surah = surahs.find { it.number == ayah.surahNumber }
        val surahName = surah?.nameEnglish ?: "Surah ${ayah.surahNumber}"

        ElevatedCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_ayah_card"),
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
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(28.dp)
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
                Text(
                  text = "Daily Quranic Ayah",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                )
              }

              Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer
              ) {
                Text(
                  text = "$surahName ${ayah.surahNumber}:${ayah.verseNumber}",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Arabic Verse
            Text(
              text = ayah.textArabic,
              style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = AmiriFontFamily,
                fontSize = 23.sp,
                lineHeight = 38.sp,
                textAlign = TextAlign.Right,
                color = MaterialTheme.colorScheme.onSurface
              ),
              modifier = Modifier.fillMaxWidth()
            )

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

            // English Translation (Saheeh International)
            Text(
              text = ayah.translationEnglish,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Urdu Translation (Fateh Muhammad Jalandhry)
            Text(
              text = ayah.translationUrdu,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 15.sp,
                lineHeight = 28.sp,
                textAlign = TextAlign.Right,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Card Action Buttons
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row {
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
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
    }

    // Daily Authentic Hadith Card
    item {
      dailyHadith?.let { hadith ->
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
                lineHeight = 34.sp,
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
                lineHeight = 28.sp,
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

    // Wisdom & Quotations Section (Prophet Muhammad ﷺ, Ahlul Bayt AS, & Early Sahabah RA)
    item {
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
          Text(
            text = "Wisdom & Quotations (Prophetic, Ahlul Bayt & Sahabah)",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          )
          Text(
            text = "Clearly separated categories with explicit speaker, classical source, and attribution verification status.",
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Equal & Separated Category Selector Chips
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            FilterChip(
              selected = reflectionCategory == ReflectionCategory.PROPHETIC_GUIDANCE,
              onClick = {
                reflectionCategory = ReflectionCategory.PROPHETIC_GUIDANCE
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
                reflectionIndex = 0
              },
              label = { Text("Ahlul Bayt Wisdom", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
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
                reflectionIndex = 0
              },
              label = { Text("Sahabah Wisdom (Early Sahabah)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("wisdom_category_sahabah_chip")
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

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
            placeholder = { Text("Search speaker (e.g. Imam Ali, Fatimah, Umar), topic, or source...", fontSize = 12.sp) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("wisdom_search_input")
          )

          Spacer(modifier = Modifier.height(12.dp))

          val filteredReflections = ReflectionDataProvider.filterItems(
            category = reflectionCategory,
            attributionFilter = reflectionAttributionFilter,
            searchQuery = reflectionSearchQuery
          )
          val safeIndex = if (filteredReflections.isNotEmpty()) reflectionIndex.coerceIn(0, filteredReflections.lastIndex) else 0
          val item = filteredReflections.getOrNull(safeIndex)

          if (item == null) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surface,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "No quotations in ${reflectionCategory.displayTitle} match the current filter.",
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
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = item.theme,
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = GoldPrimary
                    )
                  )
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
                  text = "Speaker: ${item.speaker}",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                )

                Text(
                  text = item.title,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )

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
                  text = "Source: ${item.sourceReference}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                      onClick = { if (safeIndex < filteredReflections.lastIndex) reflectionIndex = safeIndex + 1 },
                      enabled = safeIndex < filteredReflections.lastIndex,
                      modifier = Modifier.size(30.dp)
                    ) {
                      Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "Next Quotation")
                    }
                  }

                  Row {
                    IconButton(
                      onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val shareBody = "${item.arabicOrOriginalText}\n\n${item.translationEnglish}\n\n${item.translationUrdu}\n\n— ${item.speaker} | ${item.sourceReference} (${item.attributionStatus.label})"
                        clipboard.setPrimaryClip(ClipData.newPlainText("Wisdom Quotation", shareBody))
                        Toast.makeText(context, "Quotation copied to clipboard", Toast.LENGTH_SHORT).show()
                      },
                      modifier = Modifier.size(32.dp)
                    ) {
                      Icon(Icons.Default.ContentCopy, contentDescription = "Copy Quotation", modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                      onClick = {
                        val shareBody = "${item.arabicOrOriginalText}\n\n${item.translationEnglish}\n\n${item.translationUrdu}\n\n— ${item.speaker} | ${item.sourceReference} (${item.attributionStatus.label})\nShared via Bayan al-Huda"
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
  onNavigateToDailyDuas: () -> Unit
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
      // 1. Top Header Banner: Calculated Friday & Daily Sunnah Reminder
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
                text = "Calculated Friday & Daily Sunnah Reminder",
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
              color = if (calcStatus.isFridayToday) GoldPrimary else Color.White.copy(alpha = 0.16f)
            ) {
              Text(
                text = calcStatus.nextFridayDateReadable,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (calcStatus.isFridayToday) Color(0xFF102018) else Color.White,
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

      // 2. Special Friday (Yawm al-Jumu'ah) Highlight Box on Home Screen
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
                  text = if (calcStatus.isFridayToday) {
                    "Blessed Friday (Jumu'ah) Special Reminder — TODAY"
                  } else {
                    "Special Friday (Jumu'ah) Bath & Sunnah Preparation"
                  },
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
                Text(
                  text = "${calcStatus.nextFridayBathAlertSummary} • Checklist: $completedFridayCount/$totalFridayCount",
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
                text = "${selectedSpec.dayNameEnglish} — ${selectedSpec.islamicDayTitle}",
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

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = selectedSpec.primaryReminderTitle,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold
            )
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = selectedSpec.arabicHadithSnippet,
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
            text = selectedSpec.englishGuidance,
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = 12.sp,
              lineHeight = 18.sp
            )
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = selectedSpec.urduGuidance,
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

          selectedSpec.keySunnahBullets.forEach { bullet ->
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
            text = "Authentic Source: ${selectedSpec.authenticReference}",
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
