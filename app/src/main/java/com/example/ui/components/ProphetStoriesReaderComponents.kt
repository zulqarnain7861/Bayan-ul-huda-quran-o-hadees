package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ProphetStoriesRepository
import com.example.data.repository.ProphetStoriesRepository.ProphetDisplayLanguageMode
import com.example.data.repository.ProphetStoriesRepository.ProphetIndexEntry
import com.example.data.repository.ProphetStoriesRepository.ProphetStoryChapter
import com.example.data.repository.ProphetStoriesRepository.ProphetStoryCompletePackage
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.UrduNastaliqFontFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProphetStoriesHubView(
  onAddStudyNote: (String) -> Unit = {},
  onOpenClassicalBookById: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val allProphets = remember(context) { ProphetStoriesRepository.getProphetIndexList(context) }
  val allPackages = remember(context) { ProphetStoriesRepository.getAll25ProphetPackages(context) }
  val externalAudits = remember(context) { ProphetStoriesRepository.getExternalRepositoriesAudit(context) }
  val classicalWorks = remember { ProphetStoriesRepository.CLASSICAL_PROPHET_WORKS }
  val checksumRecords = remember(context) { ProphetStoriesRepository.verifyAll51FilesIntegrity(context) }

  var selectedProphetId by remember { mutableStateOf<String?>(null) }
  var selectedSubView by remember { mutableStateOf("PROPHETS_25") } // "PROPHETS_25", "CLASSICAL_WORKS", "AUDIT_SHA256"
  var searchQuery by remember { mutableStateOf("") }
  var languageMode by remember {
    mutableStateOf(ProphetStoriesRepository.getSavedLanguageMode(context))
  }
  var bookmarkedKeys by remember {
    mutableStateOf(ProphetStoriesRepository.getBookmarkedChapterKeys(context))
  }
  var showBookmarkedOnly by remember { mutableStateOf(false) }
  var fontBoostSp by remember { mutableIntStateOf(0) }

  val activeProphetPackage = remember(selectedProphetId, context) {
    selectedProphetId?.let { ProphetStoriesRepository.getProphetStoryPackage(it, context) }
  }

  if (activeProphetPackage != null) {
    BackHandler { selectedProphetId = null }
    ProphetStoryDetailReaderView(
      pkg = activeProphetPackage,
      languageMode = languageMode,
      onSelectLanguageMode = { nextMode ->
        languageMode = nextMode
        ProphetStoriesRepository.saveLanguageMode(context, nextMode)
      },
      bookmarkedKeys = bookmarkedKeys,
      onToggleBookmark = { trackType, chId ->
        bookmarkedKeys = ProphetStoriesRepository.toggleChapterBookmark(
          context = context,
          prophetId = activeProphetPackage.indexEntry.id,
          trackType = trackType,
          chapterId = chId
        )
      },
      fontBoostSp = fontBoostSp,
      onAdjustFontBoost = { delta ->
        fontBoostSp = (fontBoostSp + delta).coerceIn(-4, 10)
      },
      onAddStudyNote = onAddStudyNote,
      onOpenClassicalBookById = onOpenClassicalBookById,
      onSelectAdjacentProphet = { nextOrder ->
        val nextEntry = allProphets.firstOrNull { it.order == nextOrder }
        if (nextEntry != null) {
          selectedProphetId = nextEntry.id
          ProphetStoriesRepository.saveReadingProgress(context, nextEntry.id, 1)
        }
      },
      onClose = { selectedProphetId = null },
      modifier = modifier
    )
    return
  }

  val searchHits = remember(searchQuery, context) {
    ProphetStoriesRepository.searchProphetStories(searchQuery, context)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("prophet_stories_hub_list"),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 1. Header Banner with Non-Negotiable Source Preservation Badge
    item {
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("prophet_stories_header_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
          containerColor = EmeraldPrimary.copy(alpha = 0.10f)
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(22.dp)
              )
              Column {
                Text(
                  text = "قِصَصُ الْأَنْبِيَاءِ • Stories of the 25 Prophets (Peace Be Upon Them)",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
                Text(
                  text = "25 Prophets • 177 Adult Chapters • 84 Kids Subchapters • 1,250 Quiz Questions • 100% Verbatim SHA-256 Verified",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                  )
                )
              }
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = GoldPrimary.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = "Immutable Religious Text Guarantee (Zero AI Alteration)",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
              Text(
                text = "Source: github.com/mohammed-2-5/islamic-library-data/tree/master/prophet_stories • Every Arabic & English chapter and quiz question is copied byte-for-byte with verified SHA-256 checksums. Where no published Urdu translation exists in the upstream JSON, '$ProphetStoriesRepository.URDU_UNAVAILABLE_NOTICE' is displayed per strict religious preservation policy.",
                style = MaterialTheme.typography.bodySmall.copy(
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
              )
            }
          }
        }
      }
    }

    // 2. Language Display Mode Switcher (PART 7: Arabic + Urdu, Arabic + English, Arabic Only, Urdu Only)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "Display Language Mode (طرزِ مطالعہ):",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("prophet_stories_language_switcher_row")
        ) {
          items(ProphetDisplayLanguageMode.entries) { mode ->
            val selected = languageMode == mode
            FilterChip(
              selected = selected,
              onClick = {
                languageMode = mode
                ProphetStoriesRepository.saveLanguageMode(context, mode)
              },
              label = {
                Text(
                  text = "${mode.labelEn} (${mode.labelUr})",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("prophet_lang_mode_${mode.id}")
            )
          }
        }
      }
    }

    // 3. Sub-section Switcher: 25 Prophets | Classical Qisas Works (8) | Source & SHA-256 Audit (51 Files + 9 Repos)
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        val tabs = listOf(
          "PROPHETS_25" to "All 25 Prophets (1–25)",
          "CLASSICAL_WORKS" to "Classical Qisas Works (${classicalWorks.size})",
          "AUDIT_SHA256" to "SHA-256 & Repo Audit (51 Files)"
        )
        items(tabs) { (key, label) ->
          FilterChip(
            selected = selectedSubView == key,
            onClick = { selectedSubView = key },
            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = GoldPrimary,
              selectedLabelColor = Color.Black
            ),
            modifier = Modifier.testTag("prophet_hub_subtab_$key")
          )
        }
        item {
          FilterChip(
            selected = showBookmarkedOnly,
            onClick = { showBookmarkedOnly = !showBookmarkedOnly },
            label = {
              Text(
                text = "Bookmarked (${bookmarkedKeys.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            },
            modifier = Modifier.testTag("prophet_hub_bookmarks_filter")
          )
        }
      }
    }

    // 4. Search Input across all 25 Prophets, 261 Chapters, and Quran References
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("prophet_stories_search_input"),
        placeholder = {
          Text(
            text = "Search 25 Prophets, Arabic/English chapters, or Quran refs (آدم، إبراهيم، موسى، يوسف، محمد، البقرة)...",
            fontSize = 12.sp
          )
        },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Default.Close, contentDescription = "Clear")
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )
    }

    if (searchQuery.isNotBlank()) {
      item {
        Text(
          text = "Search Results Across 25 Prophets (${searchHits.size} matching chapters):",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = EmeraldPrimary
          )
        )
      }
      itemsIndexed(
        items = searchHits.take(25),
        key = { idx, (entry, ch) -> "search_hit_${entry.id}_${ch.trackType}_${ch.id}_$idx" }
      ) { _, (entry, ch) ->
        ElevatedCard(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              selectedProphetId = entry.id
              ProphetStoriesRepository.saveReadingProgress(context, entry.id, ch.id)
            },
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "Prophet #${entry.order}: ${entry.nameEn} • ${entry.nameAr} (${ch.trackType} Ch. ${ch.id})",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              )
            )
            Text(
              text = ch.titleAr,
              style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = AmiriFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )
            Text(
              text = ch.titleEn,
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
              text = ch.contentAr,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = AmiriFontFamily,
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    } else if (selectedSubView == "CLASSICAL_WORKS") {
      item {
        Text(
          text = "Classical Qisas al-Anbiya & Seerah Sources (Kept Strictly Separate from Simplified Summaries — PART 4):",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = EmeraldPrimary
          )
        )
      }
      items(classicalWorks, key = { it.workId }) { work ->
        ElevatedCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("classical_prophet_work_${work.workId}"),
          shape = RoundedCornerShape(14.dp)
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = work.titleAr,
              style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = AmiriFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )
            Text(
              text = "${work.titleEn} • ${work.titleUr}",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Author: ${work.authorEn} (${work.authorAr})",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
              text = "Arabic Edition: ${work.editionPublisher}",
              style = MaterialTheme.typography.bodySmall
            )
            Text(
              text = "Published Urdu Translation & Edition: ${work.urduTranslatorAndEdition}",
              style = MaterialTheme.typography.bodySmall.copy(
                color = EmeraldPrimary,
                fontWeight = FontWeight.SemiBold
              )
            )
            Text(
              text = work.separationNote,
              style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
            if (work.linkedLibraryBookId != null) {
              OutlinedButton(
                onClick = { onOpenClassicalBookById(work.linkedLibraryBookId) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("open_classical_work_btn_${work.workId}")
              ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Classical Book in Historical Library", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    } else if (selectedSubView == "AUDIT_SHA256") {
      val verifiedCount = checksumRecords.count { it.isVerifiedMatch }
      item {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = EmeraldPrimary.copy(alpha = 0.12f),
          border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "Live SHA-256 Cryptographic Verification: $verifiedCount / ${checksumRecords.size} Files Byte-for-Byte Verified",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              )
            )
            Text(
              text = "Repository: ${ProphetStoriesRepository.PRIMARY_FOLDER_URL} • Retrieved: ${ProphetStoriesRepository.RETRIEVAL_TIMESTAMP_ISO}",
              style = MaterialTheme.typography.labelSmall
            )
          }
        }
      }
      item {
        Text(
          text = "External Repositories & Datasets Audited (PART 4 & PART 9):",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
        )
      }
      items(externalAudits, key = { it.number }) { audit ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "${audit.number}. ${audit.title} [${audit.availabilityStatus}]",
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
            )
            Text(
              text = "URL: ${audit.repositoryOrSourceUrl}",
              style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary)
            )
            if (audit.sha256Checksum != null) {
              Text(
                text = "SHA-256: ${audit.sha256Checksum} (${audit.sizeBytes} bytes)",
                style = MaterialTheme.typography.labelSmall
              )
            }
            Text(
              text = "Languages: ${audit.languageCoverage}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
              text = audit.verificationSummary,
              style = MaterialTheme.typography.bodySmall
            )
          }
        }
      }
    } else {
      // Default: All 25 Prophets in strict canonical order (1..25)
      val filteredPackages = if (showBookmarkedOnly) {
        allPackages.filter { pkg ->
          bookmarkedKeys.any { it.startsWith("${pkg.indexEntry.id}_") }
        }
      } else {
        allPackages
      }

      items(filteredPackages, key = { it.indexEntry.id }) { pkg ->
        ProphetCatalogCard(
          pkg = pkg,
          languageMode = languageMode,
          onClick = {
            selectedProphetId = pkg.indexEntry.id
            ProphetStoriesRepository.saveReadingProgress(context, pkg.indexEntry.id, 1)
          }
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProphetCatalogCard(
  pkg: ProphetStoryCompletePackage,
  languageMode: ProphetDisplayLanguageMode,
  onClick: () -> Unit
) {
  val entry: ProphetIndexEntry = pkg.indexEntry
  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("prophet_card_${entry.order}_${entry.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(EmeraldPrimary),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "${entry.order}",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Prophet ${entry.order} • ${entry.nameEn}",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            )
            Text(
              text = entry.nameUr,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 14.sp,
                color = EmeraldPrimary,
                fontWeight = FontWeight.Bold
              )
            )
          }
        }
        Text(
          text = entry.nameAr,
          style = MaterialTheme.typography.titleLarge.copy(
            fontFamily = AmiriFontFamily,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = EmeraldPrimary
          )
        )
      }

      if (languageMode != ProphetDisplayLanguageMode.URDU_ONLY) {
        Text(
          text = entry.summaryAr,
          style = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = AmiriFontFamily,
            fontSize = 18.sp,
            lineHeight = 30.sp,
            textAlign = TextAlign.Right,
            color = MaterialTheme.colorScheme.onSurface
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }

      if (languageMode == ProphetDisplayLanguageMode.ARABIC_AND_ENGLISH) {
        Text(
          text = entry.summaryEn,
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
      }

      if (languageMode == ProphetDisplayLanguageMode.ARABIC_AND_URDU ||
        languageMode == ProphetDisplayLanguageMode.URDU_ONLY
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = GoldPrimary.copy(alpha = 0.12f),
          border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = entry.urduTranslationStatus,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = UrduNastaliqFontFamily,
              fontSize = 14.sp,
              textAlign = TextAlign.Right,
              color = EmeraldPrimary,
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        BadgeChip("${pkg.adultChapters.size} Adult Chapters")
        BadgeChip("${pkg.kidsSubchapters.size} Kids Subchapters")
        BadgeChip("${pkg.quizQuestions.size} Quiz Questions")
        BadgeChip("${pkg.provenance.quranReferencesAll.size} Quran Refs")
        BadgeChip("SHA-256: ${pkg.provenance.storyFileChecksum.actualSha256.take(8)}… ✓")
      }
    }
  }
}

@Composable
private fun BadgeChip(text: String) {
  Surface(
    shape = RoundedCornerShape(6.dp),
    color = EmeraldPrimary.copy(alpha = 0.10f),
    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.25f))
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = EmeraldPrimary
      ),
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProphetStoryDetailReaderView(
  pkg: ProphetStoryCompletePackage,
  languageMode: ProphetDisplayLanguageMode,
  onSelectLanguageMode: (ProphetDisplayLanguageMode) -> Unit,
  bookmarkedKeys: Set<String>,
  onToggleBookmark: (String, Int) -> Unit,
  fontBoostSp: Int,
  onAdjustFontBoost: (Int) -> Unit,
  onAddStudyNote: (String) -> Unit,
  onOpenClassicalBookById: (String) -> Unit,
  onSelectAdjacentProphet: (Int) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var activeReaderTab by remember(pkg.indexEntry.id) { mutableStateOf("ADULT") } // "ADULT", "KIDS", "QUIZ", "PROVENANCE"
  val selectedQuizAnswers = remember(pkg.indexEntry.id) { mutableStateMapOf<String, Int>() }

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("prophet_story_detail_reader_${pkg.indexEntry.id}")
  ) {
    // Top Reader Bar
    Surface(
      color = Color(0xFF0A4D3C),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            IconButton(
              onClick = onClose,
              modifier = Modifier
                .size(38.dp)
                .testTag("prophet_reader_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to 25 Prophets",
                tint = Color.White
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Prophet #${pkg.indexEntry.order}: ${pkg.indexEntry.nameAr} • ${pkg.indexEntry.nameEn}",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "${pkg.indexEntry.nameUr} • ${pkg.adultChapters.size} Adult Ch. + ${pkg.kidsSubchapters.size} Kids Ch. + ${pkg.quizQuestions.size} Quiz Qs",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color(0xFFD8F3DC),
                  fontSize = 11.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedButton(
              onClick = { onAdjustFontBoost(-2) },
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier.height(30.dp)
            ) {
              Text("A-", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
              onClick = { onAdjustFontBoost(2) },
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier.height(30.dp)
            ) {
              Text("A+", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Track Tabs: Adult Chapters | Kids Subchapters | 50-Question Quiz | Source Provenance & SHA-256
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          val readerTabs = listOf(
            "ADULT" to "Adult Chapters (${pkg.adultChapters.size})",
            "KIDS" to "Kids Subchapters (${pkg.kidsSubchapters.size})",
            "QUIZ" to "Prophet Quiz (${pkg.quizQuestions.size} Qs)",
            "PROVENANCE" to "Source & SHA-256 Audit"
          )
          items(readerTabs) { (tabKey, tabLabel) ->
            val selected = activeReaderTab == tabKey
            Surface(
              onClick = { activeReaderTab = tabKey },
              shape = RoundedCornerShape(16.dp),
              color = if (selected) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.14f),
              modifier = Modifier.testTag("prophet_reader_tab_$tabKey")
            ) {
              Text(
                text = tabLabel,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (selected) Color(0xFF073B2E) else Color.White
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }
      }
    }

    // Language Mode Bar inside Reader
    Surface(
      color = MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier.fillMaxWidth()
    ) {
      LazyRow(
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        items(ProphetDisplayLanguageMode.entries) { mode ->
          FilterChip(
            selected = languageMode == mode,
            onClick = { onSelectLanguageMode(mode) },
            label = {
              Text("${mode.labelEn} (${mode.labelUr})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldPrimary,
              selectedLabelColor = Color.White
            ),
            modifier = Modifier.testTag("prophet_reader_lang_${mode.id}")
          )
        }
      }
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .weight(1f),
      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      when (activeReaderTab) {
        "ADULT", "KIDS" -> {
          val chapters = if (activeReaderTab == "ADULT") pkg.adultChapters else pkg.kidsSubchapters
          item {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = EmeraldPrimary.copy(alpha = 0.08f),
              border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.28f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = if (activeReaderTab == "ADULT") {
                    "Complete Adult Track (${chapters.size} Unmodified Chapters) — Continuous Vertical Scroll"
                  } else {
                    "Simplified Kids Track (${chapters.size} Unmodified Subchapters) — Kept Separate from Adult Track"
                  },
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
                Text(
                  text = "Source File: ${pkg.provenance.storyFilePath} • SHA-256: ${pkg.provenance.storyFileChecksum.actualSha256.take(16)}… (${pkg.provenance.verificationStatus})",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
              }
            }
          }

          items(
            items = chapters,
            key = { ch -> "${pkg.indexEntry.id}_${ch.trackType}_${ch.id}" }
          ) { chapter ->
            val isBookmarked = bookmarkedKeys.contains("${pkg.indexEntry.id}_${chapter.trackType}_${chapter.id}")
            ProphetChapterCard(
              prophetEntry = pkg.indexEntry,
              chapter = chapter,
              languageMode = languageMode,
              isBookmarked = isBookmarked,
              fontBoostSp = fontBoostSp,
              onToggleBookmark = { onToggleBookmark(chapter.trackType, chapter.id) },
              onCopyChapter = {
                val clipText = buildString {
                  appendLine("${pkg.indexEntry.nameAr} (${pkg.indexEntry.nameEn}) — Chapter ${chapter.id}")
                  appendLine(chapter.titleAr)
                  appendLine(chapter.titleEn)
                  appendLine()
                  appendLine(chapter.contentAr)
                  appendLine()
                  appendLine(chapter.contentEn)
                  if (chapter.quranReferences.isNotEmpty()) {
                    appendLine("Quran References: ${chapter.quranReferences.joinToString(", ")}")
                  }
                }
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText(chapter.titleEn, clipText))
                Toast.makeText(context, "Chapter copied verbatim", Toast.LENGTH_SHORT).show()
              },
              onSaveNote = {
                onAddStudyNote(
                  "${pkg.indexEntry.nameEn} (${pkg.indexEntry.nameAr}) • Ch. ${chapter.id}: ${chapter.titleEn} — Quran Refs: ${chapter.quranReferences.joinToString(", ")}"
                )
                Toast.makeText(context, "Saved to Study Notes", Toast.LENGTH_SHORT).show()
              }
            )
          }
        }

        "QUIZ" -> {
          val answeredCount = selectedQuizAnswers.size
          val correctCount = pkg.quizQuestions.count { q -> selectedQuizAnswers[q.id] == q.correctIndex }
          item {
            ElevatedCard(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp)
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(Icons.Default.Quiz, contentDescription = null, tint = EmeraldPrimary)
                  Text(
                    text = "Verbatim 50-Question Quiz: ${pkg.indexEntry.nameAr} (${pkg.indexEntry.nameEn})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                  )
                }
                Text(
                  text = "Score: $correctCount / $answeredCount answered (${pkg.quizQuestions.size} Total Unmodified Questions from ${pkg.provenance.quizFilePath})",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = "Quiz SHA-256: ${pkg.provenance.quizFileChecksum.actualSha256}",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                )
              }
            }
          }

          itemsIndexed(
            items = pkg.quizQuestions,
            key = { _, q -> q.id }
          ) { idx, q ->
            val chosenIdx = selectedQuizAnswers[q.id]
            ElevatedCard(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("prophet_quiz_question_${q.id}"),
              shape = RoundedCornerShape(12.dp)
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(
                  text = "سؤال ${idx + 1} (${q.id}): ${q.questionAr}",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = AmiriFontFamily,
                    fontSize = (19 + fontBoostSp).sp,
                    lineHeight = (30 + fontBoostSp).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    color = EmeraldPrimary
                  ),
                  modifier = Modifier.fillMaxWidth()
                )

                q.optionsAr.forEachIndexed { optIdx, optText ->
                  val isChosen = chosenIdx == optIdx
                  val isCorrectOption = optIdx == q.correctIndex
                  val bgColor = when {
                    chosenIdx == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    isCorrectOption -> Color(0xFF2E7D32).copy(alpha = 0.18f)
                    isChosen -> MaterialTheme.colorScheme.error.copy(alpha = 0.18f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                  }
                  Surface(
                    onClick = { selectedQuizAnswers[q.id] = optIdx },
                    shape = RoundedCornerShape(8.dp),
                    color = bgColor,
                    border = BorderStroke(
                      width = 1.dp,
                      color = if (chosenIdx != null && isCorrectOption) Color(0xFF2E7D32) else Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text(
                      text = "${optIdx + 1}. $optText",
                      style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = AmiriFontFamily,
                        fontSize = (17 + fontBoostSp).sp,
                        textAlign = TextAlign.Right,
                        fontWeight = if (isChosen || (chosenIdx != null && isCorrectOption)) FontWeight.Bold else FontWeight.Normal
                      ),
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                  }
                }

                if (chosenIdx != null) {
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldPrimary.copy(alpha = 0.14f),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text(
                      text = "التوضيح من المصدر: ${q.explanationAr}",
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = AmiriFontFamily,
                        fontSize = (17 + fontBoostSp).sp,
                        textAlign = TextAlign.Right,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldPrimary
                      ),
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                    )
                  }
                }
              }
            }
          }
        }

        "PROVENANCE" -> {
          val prov = pkg.provenance
          item {
            ElevatedCard(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("prophet_provenance_card_${prov.prophetId}"),
              shape = RoundedCornerShape(14.dp)
            ) {
              Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(Icons.Default.FactCheck, contentDescription = null, tint = EmeraldPrimary)
                  Text(
                    text = "Source-Provenance & SHA-256 Record (PART 3)",
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = EmeraldPrimary
                    )
                  )
                }
                HorizontalDivider()
                ProvenanceRow("Canonical Prophet #", "Prophet ${prov.canonicalNumber} — ${pkg.indexEntry.nameEn} (${pkg.indexEntry.nameAr})")
                ProvenanceRow("Original Repository", prov.originalRepository)
                ProvenanceRow("Original Story File Path", "${prov.storyFilePath} (${prov.storyFileChecksum.actualSizeBytes} bytes)")
                ProvenanceRow("Original Story SHA-256", prov.storyFileChecksum.actualSha256)
                ProvenanceRow("Original Story Git Blob SHA", prov.storyFileChecksum.gitSha)
                ProvenanceRow("Original Quiz File Path", "${prov.quizFilePath} (${prov.quizFileChecksum.actualSizeBytes} bytes)")
                ProvenanceRow("Original Quiz SHA-256", prov.quizFileChecksum.actualSha256)
                ProvenanceRow("Original Author / Corpus", prov.originalAuthor)
                ProvenanceRow("Original Publication", prov.originalBookOrPublication)
                ProvenanceRow("Edition & Publisher", prov.editionAndPublisher)
                ProvenanceRow("Original Language", prov.originalLanguage)
                ProvenanceRow("Translation Information", prov.translationInformation)
                ProvenanceRow("Urdu Translation Policy", prov.urduTranslationPolicyNote)
                ProvenanceRow("Retrieval Date", prov.retrievalDateIso)
                ProvenanceRow("Verification Status", prov.verificationStatus)
                ProvenanceRow("Quran References (${prov.quranReferencesAll.size})", prov.quranReferencesAll.joinToString(" • "))
                ProvenanceRow(
                  "Hadith Citations in Source (${prov.hadithReferencesAll.size})",
                  prov.hadithReferencesAll.ifEmpty { listOf("None explicitly named beyond Quranic Tafsir in this file") }.joinToString(" • ")
                )

                HorizontalDivider()
                Text(
                  text = "Source Content Classifications Present (PART 3 A–F):",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                )
                prov.contentClassifications.forEach { cls ->
                  Text(
                    text = "• ${cls.titleEn} (${cls.titleAr}): ${cls.descriptionEn}",
                    style = MaterialTheme.typography.bodySmall
                  )
                }

                HorizontalDivider()
                Text(
                  text = "Separate Classical Works (Kept Strictly Unmerged — PART 4):",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                )
                val matchingClassical = ProphetStoriesRepository.CLASSICAL_PROPHET_WORKS.filter {
                  it.workId in prov.separateClassicalWorksIds
                }
                matchingClassical.forEach { cw ->
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldPrimary.copy(alpha = 0.07f),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                      Text(
                        text = "${cw.titleEn} (${cw.titleAr}) — ${cw.authorEn}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                      )
                      Text(
                        text = "Published Urdu Translation: ${cw.urduTranslatorAndEdition}",
                        style = MaterialTheme.typography.labelSmall
                      )
                      if (cw.linkedLibraryBookId != null) {
                        OutlinedButton(
                          onClick = { onOpenClassicalBookById(cw.linkedLibraryBookId) },
                          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                          modifier = Modifier.height(30.dp)
                        ) {
                          Text("Open in Historical Library", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      // Prev / Next Prophet Navigation Footer
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          OutlinedButton(
            onClick = { onSelectAdjacentProphet(pkg.indexEntry.order - 1) },
            enabled = pkg.indexEntry.order > 1,
            modifier = Modifier.testTag("prev_prophet_button")
          ) {
            Text("← Previous Prophet", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
          OutlinedButton(
            onClick = { onSelectAdjacentProphet(pkg.indexEntry.order + 1) },
            enabled = pkg.indexEntry.order < 25,
            modifier = Modifier.testTag("next_prophet_button")
          ) {
            Text("Next Prophet →", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun ProvenanceRow(label: String, value: String) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Bold,
        color = EmeraldPrimary
      )
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall.copy(
        color = MaterialTheme.colorScheme.onSurface
      )
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProphetChapterCard(
  prophetEntry: ProphetIndexEntry,
  chapter: ProphetStoryChapter,
  languageMode: ProphetDisplayLanguageMode,
  isBookmarked: Boolean,
  fontBoostSp: Int,
  onToggleBookmark: () -> Unit,
  onCopyChapter: () -> Unit,
  onSaveNote: () -> Unit
) {
  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("prophet_chapter_card_${prophetEntry.id}_${chapter.trackType}_${chapter.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Chapter Header Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = EmeraldPrimary
        ) {
          Text(
            text = "${if (chapter.trackType == "ADULT") "Chapter" else "Kids Subchapter"} #${chapter.id}",
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onToggleBookmark, modifier = Modifier.size(34.dp)) {
            Icon(
              imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
              contentDescription = "Bookmark Chapter",
              tint = GoldPrimary
            )
          }
          IconButton(onClick = onCopyChapter, modifier = Modifier.size(34.dp)) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy Verbatim Text",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          IconButton(onClick = onSaveNote, modifier = Modifier.size(34.dp)) {
            Icon(
              imageVector = Icons.Default.NoteAdd,
              contentDescription = "Save to Study Notes",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // Chapter Title in Arabic & English (Verbatim)
      Text(
        text = chapter.titleAr,
        style = MaterialTheme.typography.headlineSmall.copy(
          fontFamily = AmiriFontFamily,
          fontSize = (22 + fontBoostSp).sp,
          lineHeight = (36 + fontBoostSp).sp,
          fontWeight = FontWeight.Bold,
          color = EmeraldPrimary,
          textAlign = TextAlign.Right
        ),
        modifier = Modifier.fillMaxWidth()
      )

      Text(
        text = chapter.titleEn,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.fillMaxWidth()
      )

      // PART 3 Content Classification Badges (A–F)
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        chapter.classifications.forEach { cls ->
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = EmeraldPrimary.copy(alpha = 0.10f),
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.28f))
          ) {
            Text(
              text = cls.titleEn,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              ),
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
            )
          }
        }
      }

      HorizontalDivider()

      // Verbatim Original Arabic Text (Always shown except when Urdu Only is selected, and even in Urdu Only kept accessible because Urdu is unavailable in upstream JSON)
      Text(
        text = chapter.contentAr,
        style = MaterialTheme.typography.bodyLarge.copy(
          fontFamily = AmiriFontFamily,
          fontSize = (20 + fontBoostSp).sp,
          lineHeight = (36 + fontBoostSp * 2).sp,
          textAlign = TextAlign.Right,
          color = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("prophet_chapter_ar_${prophetEntry.id}_${chapter.trackType}_${chapter.id}")
      )

      // Verbatim Original English Text
      if (languageMode == ProphetDisplayLanguageMode.ARABIC_AND_ENGLISH) {
        Text(
          text = chapter.contentEn,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = (15 + fontBoostSp).sp,
            lineHeight = (23 + fontBoostSp).sp,
            color = MaterialTheme.colorScheme.onSurface
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("prophet_chapter_en_${prophetEntry.id}_${chapter.trackType}_${chapter.id}")
        )
      }

      // PART 5 Mandatory Urdu Translation Notice when Arabic + Urdu or Urdu Only is selected
      if (languageMode == ProphetDisplayLanguageMode.ARABIC_AND_URDU ||
        languageMode == ProphetDisplayLanguageMode.URDU_ONLY
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = GoldPrimary.copy(alpha = 0.12f),
          border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("prophet_chapter_ur_notice_${prophetEntry.id}_${chapter.trackType}_${chapter.id}")
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = chapter.displayUrduOrNotice,
              style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = (17 + fontBoostSp).sp,
                lineHeight = (28 + fontBoostSp).sp,
                textAlign = TextAlign.Right,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              ),
              modifier = Modifier.fillMaxWidth()
            )
            Text(
              text = "English (Original Source): ${chapter.contentEn}",
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = (13 + fontBoostSp).sp,
                lineHeight = (20 + fontBoostSp).sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
        }
      }

      // Verbatim Quran References
      if (chapter.quranReferences.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Quran References (المراجع القرآنية):",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary
            )
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            chapter.quranReferences.forEach { ref ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = EmeraldPrimary.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f))
              ) {
                Text(
                  text = "📖 $ref",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = AmiriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                    fontSize = 14.sp
                  ),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }
            }
          }
        }
      }

      // Extracted Hadith Citations (if present in the chapter text)
      if (chapter.hadithReferencesExtracted.isNotEmpty()) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
              text = "Explicit Hadith & Sunnah Citations in Chapter:",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              )
            )
            chapter.hadithReferencesExtracted.forEach { hRef ->
              Text(
                text = "• $hRef",
                style = MaterialTheme.typography.bodySmall.copy(
                  fontFamily = AmiriFontFamily,
                  fontSize = 14.sp
                )
              )
            }
          }
        }
      }

      // Verbatim Lessons & Moral Sections (lessonsAr & lessonsEn)
      if (chapter.lessonsAr.isNotEmpty() || chapter.lessonsEn.isNotEmpty()) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = EmeraldPrimary.copy(alpha = 0.07f),
          border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.25f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "الدروس والعبر المستفادة • Lessons & Morals (Preserved Verbatim)",
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
            }
            chapter.lessonsAr.forEach { lessonAr ->
              Text(
                text = "• $lessonAr",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontFamily = AmiriFontFamily,
                  fontSize = (17 + fontBoostSp).sp,
                  textAlign = TextAlign.Right,
                  fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }
            if (languageMode == ProphetDisplayLanguageMode.ARABIC_AND_ENGLISH ||
              languageMode == ProphetDisplayLanguageMode.ARABIC_AND_URDU
            ) {
              chapter.lessonsEn.forEach { lessonEn ->
                Text(
                  text = "• $lessonEn",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = (13 + fontBoostSp).sp,
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
