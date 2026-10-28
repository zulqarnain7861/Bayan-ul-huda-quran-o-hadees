package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.download.AudioDownloadScopeMode
import com.example.data.download.AyahRangeSelection
import com.example.data.download.DownloadManager
import com.example.data.download.HadithDownloadScopeType
import com.example.data.download.HadithDownloadSelectionRequest
import com.example.data.download.OfflineCatalogRegistry
import com.example.data.download.QuranAudioDownloadRequest
import com.example.data.download.QuranDownloadSelectionRequest
import com.example.data.local.ExtendedHadithDataProvider
import com.example.data.local.InitialDataLoader
import com.example.data.local.entities.ContentLicenceEntity
import com.example.data.local.entities.DownloadManifestStatus
import com.example.data.local.entities.DownloadPackageManifestEntity
import com.example.data.prayer.AppThemeMode
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DownloadCenterScreen(
  downloadManager: DownloadManager,
  currentThemeMode: AppThemeMode = AppThemeMode.LIGHT,
  onSelectThemeMode: (AppThemeMode) -> Unit = {},
  onPlayDownloadedAudio: (Int, Int, String) -> Unit = { _, _, _ -> },
  initialSubTab: Int = 0,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  val packages by downloadManager.observeAllPackages().collectAsStateWithLifecycle(initialValue = emptyList())
  val licences by downloadManager.observeAllLicences().collectAsStateWithLifecycle(initialValue = emptyList())
  val badgeState by downloadManager.syncBadgeState.collectAsStateWithLifecycle()

  var selectedSubTab by remember(initialSubTab) { mutableIntStateOf(initialSubTab) }

  // Confirmation dialogs
  var pendingBatchConfirmationTitle by remember { mutableStateOf<String?>(null) }
  var pendingBatchEstimatedBytes by remember { mutableStateOf(0L) }
  var pendingBatchAction by remember { mutableStateOf<(() -> Unit)?>(null) }
  var pendingDeletePackage by remember { mutableStateOf<DownloadPackageManifestEntity?>(null) }

  // Tab 0: Quran Offline Selection State
  val allSurahs = remember { InitialDataLoader.getAll114Surahs() }
  var quranSelectAll114 by remember { mutableStateOf(true) }
  var selectedQuranSurahs by remember { mutableStateOf(setOf(1, 2, 13, 18, 36, 55, 67, 112)) }
  var useSpecificAyahRange by remember { mutableStateOf(false) }
  var ayahRangeSurahInput by remember { mutableStateOf("2") }
  var ayahRangeStartInput by remember { mutableStateOf("255") }
  var ayahRangeEndInput by remember { mutableStateOf("255") }
  var selectedTranslationKeys by remember { mutableStateOf(setOf("en.sahih", "ur.jalandhry")) }
  var selectedTafsirKeys by remember { mutableStateOf(setOf("ibn_kathir")) }
  var includeQuranMetadata by remember { mutableStateOf(true) }
  var includeQuranSearchIndex by remember { mutableStateOf(true) }

  // Tab 1: Separate Quran Audio Download Manager State
  var selectedReciter by remember { mutableStateOf(OfflineCatalogRegistry.verifiedReciters.first()) }
  var selectedAudioMode by remember { mutableStateOf(AudioDownloadScopeMode.FULL_CHAPTER_AUDIO) }
  var audioSelectAll114 by remember { mutableStateOf(false) }
  var selectedAudioSurahs by remember { mutableStateOf(setOf(1, 13, 36, 112)) }
  var useAudioAyahRange by remember { mutableStateOf(false) }
  var audioRangeSurahInput by remember { mutableStateOf("2") }
  var audioRangeStartInput by remember { mutableStateOf("255") }
  var audioRangeEndInput by remember { mutableStateOf("255") }

  // Tab 2: Sunni & Shia Hadith Offline Download State
  var hadithTraditionTab by remember { mutableStateOf("SUNNI") }
  var selectedHadithCollectionKey by remember { mutableStateOf("bukhari") }
  var selectedHadithScope by remember { mutableStateOf(HadithDownloadScopeType.SELECTED_CHAPTERS) }
  var selectedHadithChapters by remember { mutableStateOf(setOf(1, 2)) }
  var singleHadithNumberInput by remember { mutableStateOf("1") }
  var includeHadithArabic by remember { mutableStateOf(true) }
  var includeHadithEnglish by remember { mutableStateOf(true) }
  var includeHadithUrdu by remember { mutableStateOf(true) }

  LaunchedEffect(Unit) {
    downloadManager.refreshSyncBadgeState()
  }

  Column(modifier = modifier.fillMaxSize().testTag("download_center_screen")) {
    // Top Status & Multi-Theme Bar
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = EmeraldPrimary.copy(alpha = 0.08f)
    ) {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (badgeState.isOfflineMode) Icons.Default.CloudOff else Icons.Default.CloudDownload,
              contentDescription = null,
              tint = if (badgeState.isOfflineMode) GoldPrimary else EmeraldPrimary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Offline Download Manager & Library",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
              Text(
                text = "Verified Packages: ${badgeState.completedPackagesCount} • Storage Used: ${DownloadManager.formatBytes(badgeState.usedOfflineStorageBytes)} / ${DownloadManager.formatBytes(badgeState.configuredStorageLimitBytes)}",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (badgeState.isOfflineMode) Color(0xFFB71C1C) else EmeraldPrimary,
            modifier = Modifier
              .clickable {
                downloadManager.setOfflineModeEnabled(!badgeState.isOfflineMode)
              }
              .testTag("download_center_offline_mode_toggle")
          ) {
            Text(
              text = if (badgeState.isOfflineMode) "OFFLINE MODE ON" else "ONLINE • TAP FOR OFFLINE",
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Multi-Theme Selector Row
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          items(AppThemeMode.entries) { mode ->
            FilterChip(
              selected = currentThemeMode == mode,
              onClick = { onSelectThemeMode(mode) },
              label = { Text(mode.label, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("theme_chip_${mode.name.lowercase()}")
            )
          }
        }
      }
    }

    // 4 Sub-Tabs: Quran Text/Tafsir, Quran Audio Manager, Sunni & Shia Hadith, Manifests & Licences
    ScrollableTabRow(
      selectedTabIndex = selectedSubTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.primary,
      edgePadding = 12.dp
    ) {
      Tab(
        selected = selectedSubTab == 0,
        onClick = { selectedSubTab = 0 },
        modifier = Modifier.testTag("download_tab_quran_text"),
        text = { Text("A. Quran & Tafsir", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
      )
      Tab(
        selected = selectedSubTab == 1,
        onClick = { selectedSubTab = 1 },
        modifier = Modifier.testTag("download_tab_quran_audio"),
        text = { Text("B. Quran Audio Manager", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
      )
      Tab(
        selected = selectedSubTab == 2,
        onClick = { selectedSubTab = 2 },
        modifier = Modifier.testTag("download_tab_hadith"),
        text = { Text("C. Hadith (Sunni & Shia)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
      )
      Tab(
        selected = selectedSubTab == 3,
        onClick = { selectedSubTab = 3 },
        modifier = Modifier.testTag("download_tab_manifests"),
        text = { Text("D–H. Transfers & Licences (${packages.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
      )
    }

    when (selectedSubTab) {
      0 -> {
        val currentRequest = QuranDownloadSelectionRequest(
          includeCompleteArabicText = quranSelectAll114,
          selectAll114Surahs = quranSelectAll114,
          selectedSurahNumbers = if (quranSelectAll114) (1..114).toSet() else selectedQuranSurahs,
          specificAyahRange = if (useSpecificAyahRange) {
            AyahRangeSelection(
              surahNumber = (ayahRangeSurahInput.toIntOrNull() ?: 2).coerceIn(1, 114),
              startAyah = (ayahRangeStartInput.toIntOrNull() ?: 255).coerceAtLeast(1),
              endAyah = (ayahRangeEndInput.toIntOrNull() ?: 255).coerceAtLeast(1)
            )
          } else null,
          selectedTranslationEditions = selectedTranslationKeys,
          selectedTafsirEditions = selectedTafsirKeys,
          includeNavigationMetadata = includeQuranMetadata,
          includeSearchIndex = includeQuranSearchIndex
        )
        val estimatedBytes = downloadManager.estimateQuranSelectionSizeBytes(currentRequest)

        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          item {
            ElevatedCard(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = "Quran Offline Package Builder",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
                Text(
                  text = "Select complete Uthmani Arabic text (all 114 surahs), specific surahs, or a single ayah / ayah range (e.g., Surah Al-Baqarah 2:255). Each translation is stored under its own editionKey so downloading one never overwrites another.",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Select All / Select None / Specific Ayah Range quick buttons
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  FilledTonalButton(
                    onClick = {
                      quranSelectAll114 = true
                      useSpecificAyahRange = false
                      selectedQuranSurahs = (1..114).toSet()
                    },
                    modifier = Modifier.testTag("quran_download_select_all_button")
                  ) {
                    Text("Select All 114 Surahs", fontSize = 11.sp)
                  }
                  OutlinedButton(
                    onClick = {
                      quranSelectAll114 = false
                      useSpecificAyahRange = false
                      selectedQuranSurahs = emptySet()
                    },
                    modifier = Modifier.testTag("quran_download_select_none_button")
                  ) {
                    Text("Select None", fontSize = 11.sp)
                  }
                  OutlinedButton(
                    onClick = {
                      quranSelectAll114 = false
                      useSpecificAyahRange = true
                      ayahRangeSurahInput = "2"
                      ayahRangeStartInput = "255"
                      ayahRangeEndInput = "255"
                    },
                    modifier = Modifier.testTag("quran_download_ayat_al_kursi_preset")
                  ) {
                    Text("2:255 Preset", fontSize = 11.sp)
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Specific Ayah or Range toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Checkbox(
                    checked = useSpecificAyahRange,
                    onCheckedChange = {
                      useSpecificAyahRange = it
                      if (it) quranSelectAll114 = false
                    }
                  )
                  Text(
                    text = "Download Specific Ayah or Ayah Range (e.g., Surah 2, Ayah 255)",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                  )
                }

                if (useSpecificAyahRange) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    OutlinedTextField(
                      value = ayahRangeSurahInput,
                      onValueChange = { ayahRangeSurahInput = it.filter { ch -> ch.isDigit() } },
                      label = { Text("Surah (1-114)") },
                      modifier = Modifier.weight(1f),
                      singleLine = true
                    )
                    OutlinedTextField(
                      value = ayahRangeStartInput,
                      onValueChange = { ayahRangeStartInput = it.filter { ch -> ch.isDigit() } },
                      label = { Text("Start Ayah") },
                      modifier = Modifier.weight(1f),
                      singleLine = true
                    )
                    OutlinedTextField(
                      value = ayahRangeEndInput,
                      onValueChange = { ayahRangeEndInput = it.filter { ch -> ch.isDigit() } },
                      label = { Text("End Ayah") },
                      modifier = Modifier.weight(1f),
                      singleLine = true
                    )
                  }
                } else if (!quranSelectAll114) {
                  Text(
                    text = "Select Individual Surahs (${selectedQuranSurahs.size} selected):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    allSurahs.take(24).forEach { surah ->
                      val isChecked = surah.number in selectedQuranSurahs
                      FilterChip(
                        selected = isChecked,
                        onClick = {
                          selectedQuranSurahs = if (isChecked) {
                            selectedQuranSurahs - surah.number
                          } else {
                            selectedQuranSurahs + surah.number
                          }
                        },
                        label = { Text("${surah.number}. ${surah.nameEnglish}", fontSize = 11.sp) }
                      )
                    }
                  }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Licensed Translations (stored separately by editionKey)
                Text(
                  text = "Licensed Translations (Stored Separately by Edition & Version)",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                OfflineCatalogRegistry.licensedTranslations.forEach { tr ->
                  val checked = tr.editionKey in selectedTranslationKeys
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        selectedTranslationKeys = if (checked) {
                          selectedTranslationKeys - tr.editionKey
                        } else {
                          selectedTranslationKeys + tr.editionKey
                        }
                      }
                      .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Checkbox(
                      checked = checked,
                      onCheckedChange = { isNowChecked ->
                        selectedTranslationKeys = if (isNowChecked) {
                          selectedTranslationKeys + tr.editionKey
                        } else {
                          selectedTranslationKeys - tr.editionKey
                        }
                      }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = "${tr.title} (${tr.languageCode.uppercase()} • ${tr.translator})",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                      )
                      Text(
                        text = "Edition: ${tr.editionKey} • ${tr.version} • ${tr.licenceText} • ${DownloadManager.formatBytes(tr.estimatedSizeBytes)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      )
                    }
                  }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Licensed Tafsirs
                Text(
                  text = "Licensed Tafsir Resources",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                OfflineCatalogRegistry.licensedTafsirs.forEach { tf ->
                  val checked = tf.editionKey in selectedTafsirKeys
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        selectedTafsirKeys = if (checked) {
                          selectedTafsirKeys - tf.editionKey
                        } else {
                          selectedTafsirKeys + tf.editionKey
                        }
                      }
                      .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Checkbox(
                      checked = checked,
                      onCheckedChange = { isNowChecked ->
                        selectedTafsirKeys = if (isNowChecked) {
                          selectedTafsirKeys + tf.editionKey
                        } else {
                          selectedTafsirKeys - tf.editionKey
                        }
                      }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = "${tf.title} (${tf.author})",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                      )
                      Text(
                        text = "${tf.licenceText} • ${DownloadManager.formatBytes(tf.estimatedSizeBytes)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Checkbox(
                    checked = includeQuranMetadata,
                    onCheckedChange = { includeQuranMetadata = it }
                  )
                  Text("Include Chapter, Verse, Page & Juz Navigation Metadata", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Checkbox(
                    checked = includeQuranSearchIndex,
                    onCheckedChange = { includeQuranSearchIndex = it }
                  )
                  Text("Build Local Offline FTS Search Index", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = "Estimated Package Size: ${DownloadManager.formatBytes(estimatedBytes)}",
                      style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                      )
                    )
                    Text(
                      text = "Free Device Storage: ${DownloadManager.formatBytes(badgeState.availableDeviceStorageBytes)}",
                      style = MaterialTheme.typography.labelSmall
                    )
                  }
                  Button(
                    onClick = {
                      pendingBatchConfirmationTitle = "Download Selected Quran Offline Package(s)"
                      pendingBatchEstimatedBytes = estimatedBytes
                      pendingBatchAction = {
                        scope.launch {
                          val ids = downloadManager.enqueueAndExecuteQuranSelection(
                            request = currentRequest,
                            runInBackground = false
                          )
                          Toast.makeText(
                            context,
                            "Verified & saved ${ids.size} Quran offline package(s)",
                            Toast.LENGTH_SHORT
                          ).show()
                          selectedSubTab = 3
                        }
                      }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.testTag("start_quran_offline_download_button")
                  ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download for Offline Use")
                  }
                }
              }
            }
          }
        }
      }

      1 -> {
        // SECTION B: Separate Quran Audio Download Manager
        val audioReq = QuranAudioDownloadRequest(
          reciter = selectedReciter,
          mode = selectedAudioMode,
          selectAll114Surahs = audioSelectAll114,
          selectedSurahNumbers = if (audioSelectAll114) (1..114).toSet() else selectedAudioSurahs,
          specificAyahRange = if (useAudioAyahRange) {
            AyahRangeSelection(
              surahNumber = (audioRangeSurahInput.toIntOrNull() ?: 2).coerceIn(1, 114),
              startAyah = (audioRangeStartInput.toIntOrNull() ?: 255).coerceAtLeast(1),
              endAyah = (audioRangeEndInput.toIntOrNull() ?: 255).coerceAtLeast(1)
            )
          } else null
        )
        val estimatedAudioBytes = downloadManager.estimateAudioSelectionSizeBytes(audioReq)
        val audioPackages = packages.filter { it.category.startsWith("QURAN_AUDIO") }

        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          item {
            ElevatedCard(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = "Separate Quran Audio Download Manager",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
                Text(
                  text = "Download full-chapter MP3 recitations separately from ayah-by-ayah recitations. Deleting audio never deletes Quran text, translations, bookmarks, or notes.",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                  text = "1. Select Verified Reciter:",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                OfflineCatalogRegistry.verifiedReciters.forEach { rec ->
                  val isSel = selectedReciter.id == rec.id
                  Surface(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 4.dp)
                      .clickable { selectedReciter = rec },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) EmeraldPrimary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                  ) {
                    Row(
                      modifier = Modifier.padding(10.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = "${rec.displayName} (${rec.arabicName}) • ${rec.bitrateKbps} kbps",
                          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                          text = "Chapter ID: ${rec.chapterReciterId} | Ayah ID: ${rec.ayahReciterId}",
                          style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                        )
                      }
                      if (isSel) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary)
                      }
                    }
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                  text = "2. Select Audio Type (Chapter vs Ayah-by-Ayah):",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  AudioDownloadScopeMode.entries.forEach { mode ->
                    FilterChip(
                      selected = selectedAudioMode == mode,
                      onClick = { selectedAudioMode = mode },
                      label = { Text(mode.label, fontSize = 12.sp) }
                    )
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = "3. Select Scope (All 114 Surahs, Multiple Surahs, or Ayah Range):",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  FilledTonalButton(
                    onClick = {
                      audioSelectAll114 = true
                      useAudioAyahRange = false
                      selectedAudioSurahs = (1..114).toSet()
                    }
                  ) {
                    Text("All 114 Surahs", fontSize = 11.sp)
                  }
                  OutlinedButton(
                    onClick = {
                      audioSelectAll114 = false
                      useAudioAyahRange = false
                      selectedAudioSurahs = setOf(1, 13, 36, 112)
                    }
                  ) {
                    Text("Selected Surahs", fontSize = 11.sp)
                  }
                  OutlinedButton(
                    onClick = {
                      audioSelectAll114 = false
                      useAudioAyahRange = true
                      selectedAudioMode = AudioDownloadScopeMode.AYAH_BY_AYAH_AUDIO
                    }
                  ) {
                    Text("Ayah Range (2:255)", fontSize = 11.sp)
                  }
                }

                if (useAudioAyahRange) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    OutlinedTextField(
                      value = audioRangeSurahInput,
                      onValueChange = { audioRangeSurahInput = it.filter { ch -> ch.isDigit() } },
                      label = { Text("Surah") },
                      modifier = Modifier.weight(1f),
                      singleLine = true
                    )
                    OutlinedTextField(
                      value = audioRangeStartInput,
                      onValueChange = { audioRangeStartInput = it.filter { ch -> ch.isDigit() } },
                      label = { Text("Start Ayah") },
                      modifier = Modifier.weight(1f),
                      singleLine = true
                    )
                    OutlinedTextField(
                      value = audioRangeEndInput,
                      onValueChange = { audioRangeEndInput = it.filter { ch -> ch.isDigit() } },
                      label = { Text("End Ayah") },
                      modifier = Modifier.weight(1f),
                      singleLine = true
                    )
                  }
                } else if (!audioSelectAll114) {
                  Spacer(modifier = Modifier.height(6.dp))
                  FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    allSurahs.take(18).forEach { s ->
                      val checked = s.number in selectedAudioSurahs
                      val sBytes = downloadManager.estimateSurahChapterAudioBytes(selectedReciter, s.number)
                      FilterChip(
                        selected = checked,
                        onClick = {
                          selectedAudioSurahs = if (checked) {
                            selectedAudioSurahs - s.number
                          } else {
                            selectedAudioSurahs + s.number
                          }
                        },
                        label = {
                          Text("${s.number}. ${s.nameEnglish} (${DownloadManager.formatBytes(sBytes)})", fontSize = 11.sp)
                        }
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = "Total Estimated Audio Size: ${DownloadManager.formatBytes(estimatedAudioBytes)}",
                      style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                      )
                    )
                    Text(
                      text = "Reciter ID: ${if (selectedAudioMode == AudioDownloadScopeMode.FULL_CHAPTER_AUDIO) selectedReciter.chapterReciterId else selectedReciter.ayahReciterId}",
                      style = MaterialTheme.typography.labelSmall
                    )
                  }

                  Button(
                    onClick = {
                      pendingBatchConfirmationTitle = "Download Quran Audio (${selectedReciter.displayName})"
                      pendingBatchEstimatedBytes = estimatedAudioBytes
                      pendingBatchAction = {
                        scope.launch {
                          downloadManager.enqueueAndExecuteQuranAudioDownload(
                            request = audioReq,
                            runInBackground = false
                          )
                          Toast.makeText(
                            context,
                            "Audio batch downloaded & verified for offline playback",
                            Toast.LENGTH_SHORT
                          ).show()
                        }
                      }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.testTag("start_audio_offline_download_button")
                  ) {
                    Icon(Icons.Default.AudioFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download Audio")
                  }
                }
              }
            }
          }

          if (audioPackages.isNotEmpty()) {
            item {
              Text(
                text = "Downloaded Audio Packages (Play Offline or Delete Audio Only)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
            }
            items(audioPackages, key = { it.packageId }) { pkg ->
              ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(pkg.title, fontWeight = FontWeight.Bold)
                      Text(
                        text = "${pkg.scopeDescription} • Status: ${pkg.status} • ${DownloadManager.formatBytes(pkg.downloadedBytes)}",
                        style = MaterialTheme.typography.labelSmall
                      )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                      FilledTonalButton(
                        onClick = {
                          onPlayDownloadedAudio(1, 1, "Al-Fatihah (Offline Audio)")
                        }
                      ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Play Offline", fontSize = 11.sp)
                      }
                      OutlinedButton(
                        onClick = {
                          scope.launch {
                            downloadManager.deleteAudioPackageOnly(pkg.packageId)
                            Toast.makeText(
                              context,
                              "Audio files deleted; Quran text & bookmarks preserved",
                              Toast.LENGTH_SHORT
                            ).show()
                          }
                        }
                      ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Audio Only", modifier = Modifier.size(16.dp))
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      2 -> {
        // SECTION C: Sunni & Shia Hadith Offline Downloads
        val allCollections = remember { ExtendedHadithDataProvider.primaryCollections }
        val filteredCollections = remember(hadithTraditionTab) {
          allCollections.filter { col ->
            val isShia = col.key in setOf("alkafi", "manlayahduruhu", "tahdhib", "istibsar", "nahjulbalagha", "sahifasajjadiyya")
            if (hadithTraditionTab == "SHIA") isShia else !isShia
          }
        }
        val selectedColMeta = filteredCollections.find { it.key == selectedHadithCollectionKey }
          ?: filteredCollections.first()

        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          item {
            ElevatedCard(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = "Sunni & Shia Hadith Offline Library Downloads",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
                Text(
                  text = "Download an individual narration, complete chapter, selected chapters, or an eligible complete collection while preserving collection, edition, Darussalam/In-Book numbers, narrator, and source attribution.",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  FilterChip(
                    selected = hadithTraditionTab == "SUNNI",
                    onClick = {
                      hadithTraditionTab = "SUNNI"
                      selectedHadithCollectionKey = "bukhari"
                    },
                    label = { Text("Sunni Hadith Collections", fontWeight = FontWeight.Bold) }
                  )
                  FilterChip(
                    selected = hadithTraditionTab == "SHIA",
                    onClick = {
                      hadithTraditionTab = "SHIA"
                      selectedHadithCollectionKey = "alkafi"
                    },
                    label = { Text("Twelver Shia Hadith Collections", fontWeight = FontWeight.Bold) }
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = "1. Select Collection (${hadithTraditionTab}):",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  filteredCollections.forEach { col ->
                    FilterChip(
                      selected = selectedColMeta.key == col.key,
                      onClick = { selectedHadithCollectionKey = col.key },
                      label = { Text("${col.nameEnglish} (${col.nameArabic})", fontSize = 11.sp) }
                    )
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                  text = "2. Select Download Scope:",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  HadithDownloadScopeType.entries.forEach { scopeType ->
                    FilterChip(
                      selected = selectedHadithScope == scopeType,
                      onClick = { selectedHadithScope = scopeType },
                      label = { Text(scopeType.label, fontSize = 11.sp) }
                    )
                  }
                }

                if (selectedHadithScope == HadithDownloadScopeType.SINGLE_NARRATION) {
                  OutlinedTextField(
                    value = singleHadithNumberInput,
                    onValueChange = { singleHadithNumberInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Hadith Number (e.g., 1 or 500)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    singleLine = true
                  )
                } else if (selectedHadithScope == HadithDownloadScopeType.COMPLETE_CHAPTER ||
                  selectedHadithScope == HadithDownloadScopeType.SELECTED_CHAPTERS
                ) {
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = "Select Chapters / Books to Download:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                  )
                  FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..8).forEach { chNum ->
                      val isSel = chNum in selectedHadithChapters
                      FilterChip(
                        selected = isSel,
                        onClick = {
                          selectedHadithChapters = if (selectedHadithScope == HadithDownloadScopeType.COMPLETE_CHAPTER) {
                            setOf(chNum)
                          } else {
                            if (isSel && selectedHadithChapters.size > 1) selectedHadithChapters - chNum else selectedHadithChapters + chNum
                          }
                        },
                        label = { Text("Chapter $chNum", fontSize = 11.sp) }
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = "3. Languages & Indexing:",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Checkbox(checked = includeHadithArabic, onCheckedChange = { includeHadithArabic = it })
                  Text("Arabic Text", fontSize = 12.sp)
                  Spacer(modifier = Modifier.width(8.dp))
                  Checkbox(checked = includeHadithEnglish, onCheckedChange = { includeHadithEnglish = it })
                  Text("English", fontSize = 12.sp)
                  Spacer(modifier = Modifier.width(8.dp))
                  Checkbox(checked = includeHadithUrdu, onCheckedChange = { includeHadithUrdu = it })
                  Text("Urdu", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                  onClick = {
                    val req = HadithDownloadSelectionRequest(
                      collectionKey = selectedColMeta.key,
                      collectionTitle = selectedColMeta.nameEnglish,
                      tradition = hadithTraditionTab,
                      scopeType = selectedHadithScope,
                      selectedChapters = selectedHadithChapters,
                      singleHadithNumber = singleHadithNumberInput.toIntOrNull() ?: 1,
                      includeArabicText = includeHadithArabic,
                      includeEnglishTranslation = includeHadithEnglish,
                      includeUrduTranslation = includeHadithUrdu
                    )
                    scope.launch {
                      val pkgId = downloadManager.enqueueAndExecuteHadithDownload(req, runInBackground = false)
                      val pkg = packages.find { it.packageId == pkgId }
                      Toast.makeText(
                        context,
                        if (pkg?.statusEnum == DownloadManifestStatus.LICENCE_RESTRICTED) {
                          "Bulk download restricted by publisher licence — see explanation & external link"
                        } else {
                          "Hadith offline package processed: ${selectedColMeta.nameEnglish}"
                        },
                        Toast.LENGTH_SHORT
                      ).show()
                      selectedSubTab = 3
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                  modifier = Modifier.fillMaxWidth().testTag("start_hadith_offline_download_button")
                ) {
                  Icon(Icons.Default.LibraryBooks, contentDescription = null)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Download Hadith Package (${selectedColMeta.nameEnglish})")
                }
              }
            }
          }
        }
      }

      3 -> {
        // SECTION D, E, G, H: Active & Completed Manifests, Storage Controls & Source Licences
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Storage & Network Controls Card (Section G)
          item {
            ElevatedCard(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Storage, contentDescription = null, tint = EmeraldPrimary)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Storage & Network Controls",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                  )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "Used Offline Storage: ${DownloadManager.formatBytes(badgeState.usedOfflineStorageBytes)} • Limit: ${DownloadManager.formatBytes(badgeState.configuredStorageLimitBytes)} • Available on Device: ${DownloadManager.formatBytes(badgeState.availableDeviceStorageBytes)}",
                  style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("Wi-Fi Only Downloads", style = MaterialTheme.typography.bodySmall)
                  Switch(
                    checked = badgeState.wifiOnlyDownloads,
                    onCheckedChange = { downloadManager.setWifiOnlyDownloads(it) }
                  )
                }
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("Allow Mobile Data When Wi-Fi Unavailable", style = MaterialTheme.typography.bodySmall)
                  Switch(
                    checked = badgeState.allowMobileDataDownloads,
                    onCheckedChange = { downloadManager.setAllowMobileDataDownloads(it) }
                  )
                }
              }
            }
          }

          // Package Manifests Header
          item {
            Text(
              text = "Download Package Manifests (${packages.size})",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            )
          }

          if (packages.isEmpty()) {
            item {
              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
              ) {
                Text(
                  text = "No offline download packages queued yet. Use tabs A, B, or C to select Quran text, translations, recitations, or Hadith packages.",
                  modifier = Modifier.padding(16.dp),
                  style = MaterialTheme.typography.bodySmall
                )
              }
            }
          } else {
            items(packages, key = { it.packageId }) { pkg ->
              PackageManifestCard(
                pkg = pkg,
                onStart = { downloadManager.startPackage(pkg.packageId) },
                onPause = { scope.launch { downloadManager.pausePackage(pkg.packageId) } },
                onResume = { scope.launch { downloadManager.resumePackage(pkg.packageId) } },
                onRetry = { scope.launch { downloadManager.retryPackage(pkg.packageId) } },
                onRetryFailedItems = { scope.launch { downloadManager.retryFailedItems(pkg.packageId) } },
                onCancel = { scope.launch { downloadManager.cancelPackage(pkg.packageId) } },
                onDeleteRequest = { pendingDeletePackage = pkg }
              )
            }
          }

          // Source-Specific Permissions & Quran Foundation Content Sync Records (Section H)
          item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Security, contentDescription = null, tint = GoldPrimary)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Source Licences & Content Sync Schedule (${licences.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
            }
          }

          items(licences, key = { it.resourceId }) { lic ->
            ContentLicenceCard(licence = lic)
          }
        }
      }
    }
  }

  // Batch Size Confirmation Dialog (Section G)
  pendingBatchConfirmationTitle?.let { title ->
    AlertDialog(
      onDismissRequest = { pendingBatchConfirmationTitle = null },
      title = { Text(title, fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text("Estimated Download Size: ${DownloadManager.formatBytes(pendingBatchEstimatedBytes)}")
          Spacer(modifier = Modifier.height(4.dp))
          Text("Available Device Storage: ${DownloadManager.formatBytes(badgeState.availableDeviceStorageBytes)}")
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Each file will undergo 7-step integrity verification (HTTP status, expected size, SHA-256 checksum, format validation, manifest completeness, atomic metadata write, and offline reopen check).",
            style = MaterialTheme.typography.bodySmall
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val action = pendingBatchAction
            pendingBatchConfirmationTitle = null
            action?.invoke()
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          modifier = Modifier.testTag("confirm_batch_download_button")
        ) {
          Text("Confirm & Start Download")
        }
      },
      dismissButton = {
        TextButton(onClick = { pendingBatchConfirmationTitle = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // Warn before deleting downloaded content (Section G & F)
  pendingDeletePackage?.let { pkg ->
    AlertDialog(
      onDismissRequest = { pendingDeletePackage = null },
      title = { Text("Delete Downloaded Package?", fontWeight = FontWeight.Bold) },
      text = {
        Text(
          "Are you sure you want to delete '${pkg.title}' (${DownloadManager.formatBytes(pkg.downloadedBytes)}) from offline storage? Your saved bookmarks and personal study notes will be preserved."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            val targetId = pkg.packageId
            pendingDeletePackage = null
            scope.launch {
              downloadManager.deletePackage(targetId)
              Toast.makeText(
                context,
                "Deleted offline package; bookmarks and personal notes preserved.",
                Toast.LENGTH_SHORT
              ).show()
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
        ) {
          Text("Delete Files")
        }
      },
      dismissButton = {
        TextButton(onClick = { pendingDeletePackage = null }) {
          Text("Keep Offline")
        }
      }
    )
  }
}

@Composable
private fun PackageManifestCard(
  pkg: DownloadPackageManifestEntity,
  onStart: () -> Unit,
  onPause: () -> Unit,
  onResume: () -> Unit,
  onRetry: () -> Unit,
  onRetryFailedItems: () -> Unit,
  onCancel: () -> Unit,
  onDeleteRequest: () -> Unit
) {
  val context = LocalContext.current
  val status = pkg.statusEnum
  val statusColor = when (status) {
    DownloadManifestStatus.COMPLETE -> EmeraldPrimary
    DownloadManifestStatus.DOWNLOADING, DownloadManifestStatus.QUEUED -> GoldPrimary
    DownloadManifestStatus.LICENCE_RESTRICTED -> Color(0xFF6A1B9A)
    DownloadManifestStatus.PARTIAL, DownloadManifestStatus.PAUSED, DownloadManifestStatus.INTERRUPTED -> Color(0xFFE65100)
    DownloadManifestStatus.FAILED, DownloadManifestStatus.VERIFICATION_FAILED -> Color(0xFFB71C1C)
  }

  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("manifest_card_${pkg.packageId}"),
    shape = RoundedCornerShape(14.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = pkg.title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = pkg.scopeDescription,
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = statusColor.copy(alpha = 0.14f)
        ) {
          Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              color = statusColor,
              fontWeight = FontWeight.Bold
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      if (status != DownloadManifestStatus.LICENCE_RESTRICTED) {
        LinearProgressIndicator(
          progress = { (pkg.overallProgressPercent / 100f).coerceIn(0f, 1f) },
          modifier = Modifier.fillMaxWidth().height(7.dp),
          color = statusColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Progress: ${pkg.overallProgressPercent}% • ${DownloadManager.formatBytes(pkg.downloadedBytes)} / ${DownloadManager.formatBytes(pkg.totalBytes)} • Completed: ${pkg.completedItems}/${pkg.totalItems} • Remaining: ${pkg.remainingItems} • Failed: ${pkg.failedItems}",
          style = MaterialTheme.typography.labelSmall
        )
        if (pkg.speedBytesPerSec > 0L || pkg.estimatedRemainingSeconds > 0L) {
          Text(
            text = "Speed: ${DownloadManager.formatBytes(pkg.speedBytesPerSec)}/s • ETA: ${pkg.estimatedRemainingSeconds}s",
            style = MaterialTheme.typography.labelSmall.copy(color = EmeraldPrimary)
          )
        }
        if (pkg.destinationPath.isNotBlank()) {
          Text(
            text = "Destination: ${pkg.destinationPath}",
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.sp
            )
          )
        }
      }

      if (pkg.errorMessage.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = statusColor.copy(alpha = 0.1f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = pkg.errorMessage,
                style = MaterialTheme.typography.bodySmall.copy(color = statusColor, fontWeight = FontWeight.SemiBold)
              )
            }
            if (pkg.externalFallbackUrl.isNotBlank()) {
              Spacer(modifier = Modifier.height(6.dp))
              OutlinedButton(
                onClick = {
                  runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(pkg.externalFallbackUrl)))
                  }
                }
              ) {
                Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Official External Source (${pkg.externalFallbackUrl})", fontSize = 11.sp)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Transfer Action Controls: Start, Pause, Resume, Retry, Retry Failed Items, Cancel, Delete
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        if (status == DownloadManifestStatus.QUEUED) {
          FilledTonalButton(onClick = onStart) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
            Text("Start", fontSize = 11.sp)
          }
        }
        if (status == DownloadManifestStatus.DOWNLOADING) {
          OutlinedButton(onClick = onPause) {
            Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(15.dp))
            Text("Pause", fontSize = 11.sp)
          }
          OutlinedButton(onClick = onCancel) {
            Text("Cancel", fontSize = 11.sp)
          }
        }
        if (status == DownloadManifestStatus.PAUSED || status == DownloadManifestStatus.INTERRUPTED) {
          FilledTonalButton(onClick = onResume) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
            Text("Resume", fontSize = 11.sp)
          }
        }
        if (status == DownloadManifestStatus.FAILED ||
          status == DownloadManifestStatus.VERIFICATION_FAILED ||
          status == DownloadManifestStatus.PARTIAL
        ) {
          FilledTonalButton(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
            Text("Retry All", fontSize = 11.sp)
          }
          if (pkg.failedItems > 0) {
            OutlinedButton(onClick = onRetryFailedItems) {
              Text("Retry Failed (${pkg.failedItems})", fontSize = 11.sp)
            }
          }
        }
        Spacer(modifier = Modifier.weight(1f))
        OutlinedButton(onClick = onDeleteRequest) {
          Icon(Icons.Default.Delete, contentDescription = "Delete Package", modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Delete", fontSize = 11.sp)
        }
      }
    }
  }
}

@Composable
private fun ContentLicenceCard(licence: ContentLicenceEntity) {
  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${licence.provider} (${licence.resourceId})",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Icon(
          imageVector = Icons.Default.Verified,
          contentDescription = null,
          tint = if (licence.bulkDownloadAllowed) EmeraldPrimary else GoldPrimary,
          modifier = Modifier.size(18.dp)
        )
      }
      Text(
        text = "Rights Holder: ${licence.rightsHolder}",
        style = MaterialTheme.typography.labelSmall
      )
      Text(
        text = "Offline Storage: ${if (licence.offlineStorageAllowed) "Allowed" else "Prohibited"} • Bulk Download: ${if (licence.bulkDownloadAllowed) "Allowed" else "Restricted"} • Content Sync Required: ${if (licence.requiresContentSyncProcedure) "Yes" else "No"}",
        style = MaterialTheme.typography.labelSmall.copy(color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
      )
      Text(
        text = "Attribution: ${licence.attributionRequirements}",
        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
      )
      Text(
        text = "Refresh Schedule: Every ${licence.refreshScheduleDays} days • Terms: ${licence.licenceUrl}",
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
      )
    }
  }
}
