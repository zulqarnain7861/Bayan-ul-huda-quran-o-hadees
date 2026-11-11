package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ExtendedHadithDataProvider
import com.example.data.local.entities.AyahEntity
import com.example.data.local.entities.HadithEntity
import com.example.data.local.entities.SurahEntity
import com.example.data.model.IslamicBooksCatalog
import com.example.data.model.LearnIslamCurriculumData
import com.example.data.model.NimazJanazaSubSection
import com.example.ui.components.SahihGradeBadge
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.UrduNastaliqFontFamily
import com.example.ui.viewmodels.SearchCategory

private val SearchMatchRedColor = Color(0xFFD32F2F)
private val SearchMatchRedBackground = Color(0x22D32F2F)
private val WhitespaceSplitRegex = Regex("\\s+")

/**
 * Highlights exact matching phrases, words, and partial terms in RED (`Color(0xFFD32F2F)`)
 * across Arabic, Urdu, and English passages, snippeting oversized passages around the first
 * matched token so LazyColumn scrolling and layout measurement remain fast and smooth.
 */
fun buildRedHighlightedSearchText(
  text: String,
  rawQuery: String,
  maxSnippetChars: Int = 650
): AnnotatedString {
  if (text.isBlank()) return AnnotatedString("")
  val cleanQuery = rawQuery.trim()
  if (cleanQuery.isBlank()) {
    val clipped = if (text.length > maxSnippetChars) {
      text.take(maxSnippetChars).trimEnd() + "…"
    } else {
      text
    }
    return AnnotatedString(clipped)
  }

  val tokens = (
    listOf(cleanQuery) +
      cleanQuery.split(WhitespaceSplitRegex).filter { it.length >= 2 }
    ).distinct()

  val displayString = if (text.length > maxSnippetChars) {
    val firstMatchIdx = tokens
      .map { token -> text.indexOf(token, ignoreCase = true) }
      .filter { it >= 0 }
      .minOrNull() ?: 0
    val windowStart = (firstMatchIdx - (maxSnippetChars / 4)).coerceAtLeast(0)
    val windowEnd = (windowStart + maxSnippetChars).coerceAtMost(text.length)
    buildString(maxSnippetChars + 4) {
      if (windowStart > 0) append("…")
      append(text, windowStart, windowEnd)
      if (windowEnd < text.length) append("…")
    }
  } else {
    text
  }

  return buildAnnotatedString {
    append(displayString)
    for (token in tokens) {
      var startIndex = displayString.indexOf(token, ignoreCase = true)
      var guard = 0
      while (startIndex >= 0 && guard < 24) {
        val endIndex = (startIndex + token.length).coerceAtMost(displayString.length)
        addStyle(
          style = SpanStyle(
            color = SearchMatchRedColor,
            background = SearchMatchRedBackground,
            fontWeight = FontWeight.Bold
          ),
          start = startIndex,
          end = endIndex
        )
        startIndex = displayString.indexOf(token, startIndex + token.length.coerceAtLeast(1), ignoreCase = true)
        guard++
      }
    }
  }
}

private data class LearnIslamSearchHit(
  val id: String,
  val tab: LearnIslamTab,
  val badgeLabel: String,
  val stepNo: Int,
  val sectionTitle: String,
  val stepTitle: String,
  val arabic: String,
  val urdu: String,
  val english: String,
  val reference: String
)

private data class IndexedLearnIslamEntry(
  val hit: LearnIslamSearchHit,
  val lowercaseHaystack: String,
  val isCompleteSalahOverviewStep: Boolean
)

private data class ClassicalBookSearchHit(
  val id: String,
  val bookTitleEnglish: String,
  val bookTitleArabic: String,
  val author: String,
  val tradition: String,
  val category: String,
  val chapterTitle: String,
  val arabicExcerpt: String,
  val englishExcerpt: String,
  val urduExcerpt: String,
  val primarySourceCitation: String
)

private data class IndexedClassicalBookEntry(
  val hit: ClassicalBookSearchHit,
  val traditionUpper: String,
  val lowercaseHaystack: String
)

private val preIndexedLearnIslamEntries: List<IndexedLearnIslamEntry> by lazy {
  buildList {
    LearnIslamCurriculumData.completeSalahSteps.forEach { step ->
      val haystack = buildString {
        append(step.section).append(' ')
        append(step.title).append(' ')
        append(step.arabic).append(' ')
        append(step.urdu).append(' ')
        append(step.english).append(' ')
        append(step.reference).append(' ')
        append(step.videoTitle.orEmpty()).append(' ')
        append("Complete Salah Namaz Wudu")
      }.lowercase()
      add(
        IndexedLearnIslamEntry(
          hit = LearnIslamSearchHit(
            id = "complete_salah_${step.step_no}",
            tab = LearnIslamTab.NAMAZ_AND_WUDU,
            badgeLabel = "Learn Islam • Complete Salah",
            stepNo = step.step_no,
            sectionTitle = "Complete Salah — ${step.section}",
            stepTitle = step.title,
            arabic = step.arabic,
            urdu = step.urdu,
            english = step.english,
            reference = step.reference
          ),
          lowercaseHaystack = haystack,
          isCompleteSalahOverviewStep = step.step_no == 1 || step.step_no == 10
        )
      )
    }

    NimazJanazaSubSection.entries.forEach { sub ->
      LearnIslamCurriculumData.getNimazJanazaSteps(sub).forEach { step ->
        val haystack = buildString {
          append(sub.labelEnglish).append(' ')
          append(sub.labelUrdu).append(' ')
          append(step.section).append(' ')
          append(step.title).append(' ')
          append(step.arabic).append(' ')
          append(step.urdu).append(' ')
          append(step.english).append(' ')
          append(step.reference).append(' ')
          append("Nimaz Janaza نماز جنازہ funeral prayer standing only no ruku no sajdah")
        }.lowercase()
        add(
          IndexedLearnIslamEntry(
            hit = LearnIslamSearchHit(
              id = "nimaz_janaza_${sub.key}_${step.step_no}",
              tab = LearnIslamTab.NIMAZ_JANAZA,
              badgeLabel = "Learn Islam • Nimaz Janaza (${sub.labelEnglish})",
              stepNo = step.step_no,
              sectionTitle = "Nimaz Janaza (${sub.labelEnglish} / ${sub.labelUrdu})",
              stepTitle = step.title,
              arabic = step.arabic,
              urdu = step.urdu,
              english = step.english,
              reference = step.reference
            ),
            lowercaseHaystack = haystack,
            isCompleteSalahOverviewStep = false
          )
        )
      }
    }
  }
}

private val preIndexedClassicalBookEntries: List<IndexedClassicalBookEntry> by lazy {
  buildList {
    for (book in IslamicBooksCatalog.allSunniAndShiaBooks) {
      val traditionUpper = book.tradition.uppercase()
      for (chapter in book.chapters) {
        val haystack = buildString {
          append(book.titleEnglish).append(' ')
          append(book.titleArabic).append(' ')
          append(book.author).append(' ')
          append(book.category).append(' ')
          append(chapter.titleEnglish).append(' ')
          append(chapter.arabicExcerpt).append(' ')
          append(chapter.englishContent).append(' ')
          append(chapter.urduContent).append(' ')
          append(chapter.crossReferences)
        }.lowercase()
        add(
          IndexedClassicalBookEntry(
            hit = ClassicalBookSearchHit(
              id = "${book.id}_${chapter.chapterNumber}",
              bookTitleEnglish = book.titleEnglish,
              bookTitleArabic = book.titleArabic,
              author = book.author,
              tradition = book.tradition,
              category = book.category,
              chapterTitle = "Ch. ${chapter.chapterNumber}: ${chapter.titleEnglish}",
              arabicExcerpt = chapter.arabicExcerpt,
              englishExcerpt = chapter.englishContent,
              urduExcerpt = chapter.urduContent,
              primarySourceCitation = chapter.crossReferences
            ),
            traditionUpper = traditionUpper,
            lowercaseHaystack = haystack
          )
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
  searchQuery: String,
  searchCategory: SearchCategory,
  ayahResults: List<AyahEntity>,
  hadithResults: List<HadithEntity>,
  surahs: List<SurahEntity>,
  tafsirResults: List<com.example.data.repository.AyahTafsirResult> = emptyList(),
  isSearchingRemote: Boolean = false,
  selectedTraditionScope: String = "BOTH",
  onTraditionScopeChanged: ((String) -> Unit)? = null,
  onSearchQueryChanged: (String) -> Unit,
  onSearchCategoryChanged: (SearchCategory) -> Unit,
  onOpenSurah: (SurahEntity) -> Unit,
  onOpenSurahWithVerse: ((SurahEntity, Int?) -> Unit)? = null,
  onOpenHadith: (HadithEntity) -> Unit = {},
  onOpenVideoLibrary: () -> Unit = {},
  onOpenLearnIslam: (LearnIslamTab) -> Unit = {},
  onOpenLibraryBooks: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var localTraditionScope by remember(selectedTraditionScope) {
    mutableStateOf(selectedTraditionScope)
  }
  val hadithTraditionScope = if (onTraditionScopeChanged != null) {
    selectedTraditionScope
  } else {
    localTraditionScope
  }

  fun updateTraditionScope(nextScope: String) {
    localTraditionScope = nextScope
    onTraditionScopeChanged?.invoke(nextScope)
  }

  val scopedHadithResults = remember(hadithResults, hadithTraditionScope) {
    if (hadithTraditionScope == "BOTH") {
      hadithResults
    } else {
      hadithResults.filter { hadith ->
        val isShia = ExtendedHadithDataProvider.isShiaCollection(hadith.collectionKey)
        if (hadithTraditionScope == "SHIA") isShia else !isShia
      }
    }
  }

  val scopedTafsirResults = remember(tafsirResults, hadithTraditionScope) {
    when (hadithTraditionScope) {
      "SUNNI" -> tafsirResults.filter { !it.edition.tradition.equals("SHIA", ignoreCase = true) }
      "SHIA" -> tafsirResults.filter { it.edition.tradition.equals("SHIA", ignoreCase = true) }
      else -> tafsirResults
    }
  }

  // Fast pre-indexed search across Learn Islam ("Complete Salah" & "Nimaz Janaza")
  val learnIslamHits = remember(searchQuery) {
    val q = searchQuery.trim().lowercase()
    if (q.length < 2) {
      emptyList()
    } else {
      val tokens = q.split(WhitespaceSplitRegex).filter { it.length >= 2 }
      val matchesCompleteSalahLabel = "complete salah".contains(q) || "namaz".contains(q) || "nimaz".contains(q)
      val hits = ArrayList<LearnIslamSearchHit>(15)
      for (entry in preIndexedLearnIslamEntries) {
        val haystack = entry.lowercaseHaystack
        val matched = haystack.contains(q) ||
          (tokens.isNotEmpty() && tokens.all { haystack.contains(it) }) ||
          (matchesCompleteSalahLabel && entry.isCompleteSalahOverviewStep)
        if (matched) {
          hits.add(entry.hit)
          if (hits.size >= 15) break
        }
      }
      hits
    }
  }

  // Fast pre-indexed search across Classical Sunni & Shia Books Catalog
  val classicalBookHits = remember(searchQuery, hadithTraditionScope) {
    val q = searchQuery.trim().lowercase()
    if (q.length < 2) {
      emptyList()
    } else {
      val tokens = q.split(WhitespaceSplitRegex).filter { it.length >= 2 }
      val hits = ArrayList<ClassicalBookSearchHit>(20)
      for (entry in preIndexedClassicalBookEntries) {
        val matchesTradition = when (hadithTraditionScope) {
          "SUNNI" -> entry.traditionUpper == "SUNNI"
          "SHIA" -> entry.traditionUpper == "SHIA"
          else -> true
        }
        if (!matchesTradition) continue
        val haystack = entry.lowercaseHaystack
        if (haystack.contains(q) || (tokens.isNotEmpty() && tokens.all { haystack.contains(it) })) {
          hits.add(entry.hit)
          if (hits.size >= 20) break
        }
      }
      hits
    }
  }

  // Search Sahaba & Ahl al-Bayt Biographies (Sunni & Shia Separate Accounts)
  val biographyHits = remember(searchQuery, hadithTraditionScope, searchCategory) {
    val q = searchQuery.trim()
    if (q.isEmpty() && searchCategory != SearchCategory.SAHABA_AHL_AL_BAYT) {
      emptyList()
    } else {
      com.example.data.repository.ClassicalLibraryAndBiographiesRepository.searchBiographies(
        query = q,
        traditionFilter = hadithTraditionScope,
        categoryFilter = "ALL"
      )
    }
  }

  val suggestions = listOf(
    "2:255",
    "36:1",
    "Sahaba",
    "Ahl al-Bayt",
    "Abu Bakr",
    "Umar",
    "Uthman",
    "Ali ibn Abi Talib",
    "Lady Fatimah",
    "Imam Husayn",
    "Complete Salah",
    "Nimaz Janaza",
    "Al-Kafi 1",
    "Nahj al-Balagha 1",
    "Sermon 193",
    "Bukhari 5027",
    "Muslim 223",
    "Mercy",
    "Knowledge"
  )

  val showAyahs = searchCategory == SearchCategory.ALL || searchCategory == SearchCategory.QURAN
  val showHadiths = searchCategory == SearchCategory.ALL || searchCategory == SearchCategory.HADITH
  val showTafsirs = searchCategory == SearchCategory.ALL || searchCategory == SearchCategory.TAFSIR
  val showBiographies = searchCategory == SearchCategory.ALL || searchCategory == SearchCategory.SAHABA_AHL_AL_BAYT
  val showExtraSources = searchCategory == SearchCategory.ALL || searchCategory == SearchCategory.SAHABA_AHL_AL_BAYT

  val totalCount = (if (showAyahs) ayahResults.size else 0) +
    (if (showHadiths) scopedHadithResults.size else 0) +
    (if (showTafsirs) scopedTafsirResults.size else 0) +
    (if (showBiographies) biographyHits.size else 0) +
    (if (searchCategory == SearchCategory.ALL) learnIslamHits.size + classicalBookHits.size else 0)

  Column(modifier = modifier.fillMaxSize()) {
    // Top Row: Search Bar + YouTube-Style Rotating Circular Loading Indicator Beside Search Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        modifier = Modifier
          .weight(1f)
          .testTag("unified_search_input"),
        placeholder = {
          Text(
            text = "Search Quran, Hadith, Sahaba & Ahl al-Bayt, Tafsir, Books...",
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSearchingRemote) {
              CircularProgressIndicator(
                modifier = Modifier
                  .size(18.dp)
                  .padding(end = 4.dp)
                  .testTag("search_online_stage2_spinner"),
                strokeWidth = 2.dp,
                color = EmeraldPrimary
              )
            }
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { onSearchQueryChanged("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear search")
              }
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp)
      )

      // Small YouTube-style rotating circular loading indicator beside the search bar while Stage 2 online search runs
      if (isSearchingRemote) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = EmeraldPrimary.copy(alpha = 0.12f),
          border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
          modifier = Modifier.testTag("youtube_style_online_search_indicator")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              strokeWidth = 2.2.dp,
              color = EmeraldPrimary
            )
            Text(
              text = "Online",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                fontSize = 10.sp
              )
            )
          }
        }
      }
    }

    // Single Full-Page Scrollable LazyColumn so Video Library, Category Chips, and Tradition Scope scroll up off-screen
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      item(key = "search_scrollable_header_controls") {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          // Clearly visible Video Library shortcut right below the main search bar
          Button(
            onClick = onOpenVideoLibrary,
            colors = ButtonDefaults.buttonColors(
              containerColor = EmeraldPrimary,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("search_open_video_library_btn")
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Open Video Library",
              tint = GoldPrimary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Video Library (Engineer Muhammad Ali Mirza Lectures)",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          // Category filter chips (Scrollable LazyRow so chips never wrap vertically)
          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            item {
              FilterChip(
                selected = searchCategory == SearchCategory.ALL,
                onClick = { onSearchCategoryChanged(SearchCategory.ALL) },
                label = { Text("All Sources (Offline + Online)", fontSize = 11.sp, maxLines = 1, softWrap = false) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EmeraldPrimary,
                  selectedLabelColor = Color.White
                )
              )
            }
            item {
              FilterChip(
                selected = searchCategory == SearchCategory.SAHABA_AHL_AL_BAYT,
                onClick = { onSearchCategoryChanged(SearchCategory.SAHABA_AHL_AL_BAYT) },
                label = {
                  Text(
                    text = "Sahaba & Ahl al-Bayt (Sunni & Shia Sources)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EmeraldPrimary,
                  selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("search_category_sahaba_ahl_al_bayt")
              )
            }
            item {
              FilterChip(
                selected = searchCategory == SearchCategory.QURAN,
                onClick = { onSearchCategoryChanged(SearchCategory.QURAN) },
                label = { Text("Quran (6,236 Verses + Translations)", fontSize = 11.sp, maxLines = 1, softWrap = false) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EmeraldPrimary,
                  selectedLabelColor = Color.White
                )
              )
            }
            item {
              FilterChip(
                selected = searchCategory == SearchCategory.HADITH,
                onClick = { onSearchCategoryChanged(SearchCategory.HADITH) },
                label = {
                  Text(
                    text = when (hadithTraditionScope) {
                      "SHIA" -> "Hadith (All Shia Collections)"
                      "SUNNI" -> "Hadith (All Sunni Collections)"
                      else -> "Hadith (36 Sunni & Shia Books)"
                    },
                    fontSize = 11.sp,
                    maxLines = 1,
                    softWrap = false
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EmeraldPrimary,
                  selectedLabelColor = Color.White
                )
              )
            }
            item {
              FilterChip(
                selected = searchCategory == SearchCategory.TAFSIR,
                onClick = { onSearchCategoryChanged(SearchCategory.TAFSIR) },
                label = {
                  Text(
                    text = when (hadithTraditionScope) {
                      "SHIA" -> "Tafsir (8 Shia Exegeses)"
                      "SUNNI" -> "Tafsir (8+ Sunni Exegeses)"
                      else -> "Tafsir (16+ Sunni & Shia Exegeses)"
                    },
                    fontSize = 11.sp,
                    maxLines = 1,
                    softWrap = false
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EmeraldPrimary,
                  selectedLabelColor = Color.White
                )
              )
            }
          }

          // Tradition Scope Filter (Strict Sunni / Shia separation across Hadith, Tafsir, Biographies, and Classical Books)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            FilterChip(
              selected = hadithTraditionScope == "BOTH",
              onClick = { updateTraditionScope("BOTH") },
              label = { Text("Both Traditions", fontSize = 11.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
              ),
              modifier = Modifier.weight(1f).testTag("search_hadith_scope_both")
            )
            FilterChip(
              selected = hadithTraditionScope == "SUNNI",
              onClick = { updateTraditionScope("SUNNI") },
              label = { Text("Sunni Only", fontSize = 11.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.weight(1f).testTag("search_hadith_scope_sunni")
            )
            FilterChip(
              selected = hadithTraditionScope == "SHIA",
              onClick = { updateTraditionScope("SHIA") },
              label = { Text("Shia Only", fontSize = 11.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GoldPrimary,
                selectedLabelColor = Color.Black
              ),
              modifier = Modifier.weight(1f).testTag("search_hadith_scope_shia")
            )
          }
        }
      }

      if (searchQuery.isBlank() && searchCategory != SearchCategory.SAHABA_AHL_AL_BAYT) {
        item(key = "search_empty_prompt_card") {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 20.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              modifier = Modifier.size(56.dp),
              tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "Two-Stage Global Hybrid Search Engine",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Stage 1 searches all 6,236 Quran verses, 36 bundled Sunni & Shia Hadith books, Sahaba & Ahl al-Bayt Biographies, 16+ Tafsirs, Classical Books, Complete Salah, and Nimaz Janaza offline instantly. Stage 2 simultaneously queries global online APIs & remote databases.",
              style = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
              text = "Try Quick Reference & Topic Searches:",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(suggestions) { topic ->
                Surface(
                  modifier = Modifier
                    .clickable { onSearchQueryChanged(topic) }
                    .testTag("search_suggestion_${topic.replace(" ", "_")}"),
                  shape = RoundedCornerShape(20.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                  Text(
                    text = topic,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Medium,
                      color = MaterialTheme.colorScheme.primary
                    )
                  )
                }
              }
            }
          }
        }
      } else {
        item(key = "search_results_count_banner") {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (searchQuery.isBlank() && searchCategory == SearchCategory.SAHABA_AHL_AL_BAYT) {
                "Showing all ${biographyHits.size} Sahaba & Ahl al-Bayt Multi-Perspective Biographies (Sunni & Shia Sources Separate)"
              } else if (isSearchingRemote) {
                "Showing $totalCount instant offline matches • Fetching global online sources for \"$searchQuery\"..."
              } else {
                "Found $totalCount verified results across local & online sources for \"$searchQuery\""
              },
              style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
              ),
              modifier = Modifier.weight(1f)
            )
          }
        }

        // Sahaba & Ahl al-Bayt Biographies Section (Sunni & Shia Accounts Strictly Separated)
        if (showBiographies && biographyHits.isNotEmpty()) {
          item(key = "search_biographies_header") {
            Text(
              text = "Sahaba & Ahl al-Bayt Biographies — Sunni & Shia Classical Sources (${biographyHits.size})",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              ),
              modifier = Modifier.padding(top = 4.dp)
            )
          }

          items(biographyHits, key = { "search_bio_${it.id}" }) { bio ->
            val isShiaBio = bio.traditionPerspective.equals("SHIA", ignoreCase = true)
            val accentColor = if (isShiaBio) Color(0xFF0D47A1) else Color(0xFF1B5E20)
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenLibraryBooks() },
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.Top
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = accentColor.copy(alpha = 0.14f)
                    ) {
                      Text(
                        text = "${bio.traditionPerspective} BIOGRAPHICAL SOURCE • ${bio.category}",
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontWeight = FontWeight.Bold,
                          color = accentColor
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                      )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = buildRedHighlightedSearchText(bio.personNameEnglish, searchQuery),
                      style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                      )
                    )
                    Text(
                      text = "${bio.titleHonorific} • Era: ${bio.eraHijri}",
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.secondary
                      )
                    )
                    Text(
                      text = "Source: ${bio.primarySourceBook} (${bio.volumeAndPage}) • ${bio.author}",
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold
                      )
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(horizontalAlignment = Alignment.Start) {
                      Text(
                        text = bio.personNameArabic,
                        style = MaterialTheme.typography.titleMedium.copy(
                          fontFamily = AmiriFontFamily,
                          lineHeight = 28.sp,
                          textAlign = TextAlign.Right,
                          color = MaterialTheme.colorScheme.primary
                        )
                      )
                      Text(
                        text = bio.personNameUrdu,
                        style = MaterialTheme.typography.bodySmall.copy(
                          fontFamily = UrduNastaliqFontFamily,
                          fontSize = 13.sp,
                          lineHeight = 26.sp,
                          platformStyle = PlatformTextStyle(includeFontPadding = true),
                          textAlign = TextAlign.Right,
                          color = MaterialTheme.colorScheme.secondary
                        )
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(6.dp))
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                  Text(
                    text = buildRedHighlightedSearchText(bio.arabicText, searchQuery),
                    style = MaterialTheme.typography.bodyLarge.copy(
                      fontFamily = AmiriFontFamily,
                      fontSize = 19.sp,
                      lineHeight = 32.sp,
                      textAlign = TextAlign.Right
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                  Text(
                    text = buildRedHighlightedSearchText(bio.urduTranslation, searchQuery),
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontFamily = UrduNastaliqFontFamily,
                      fontSize = 14.sp,
                      lineHeight = 28.sp,
                      platformStyle = PlatformTextStyle(includeFontPadding = true),
                      textAlign = TextAlign.Right,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = buildRedHighlightedSearchText(bio.englishTranslation, searchQuery),
                  style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 20.sp),
                  maxLines = 5,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Refs: ${bio.crossReferences} • Translator: ${bio.translatorAttribution}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = GoldPrimary,
                    fontWeight = FontWeight.SemiBold
                  )
                )
              }
            }
          }
        }

        // Learn Islam Hits ("Complete Salah" & "Nimaz Janaza")
        if (searchCategory == SearchCategory.ALL && learnIslamHits.isNotEmpty()) {
          item {
            Text(
              text = "Learn Islam — Complete Salah & Nimaz Janaza (${learnIslamHits.size})",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              ),
              modifier = Modifier.padding(top = 4.dp)
            )
          }

          items(learnIslamHits, key = { "search_learn_${it.id}" }) { hit ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenLearnIslam(hit.tab) },
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                  ) {
                    Icon(
                      imageVector = Icons.Default.School,
                      contentDescription = null,
                      tint = EmeraldPrimary,
                      modifier = Modifier.size(16.dp)
                    )
                    Text(
                      text = "${hit.sectionTitle} • Step ${hit.stepNo}: ${hit.stepTitle}",
                      style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                      )
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GoldPrimary.copy(alpha = 0.2f)
                  ) {
                    Text(
                      text = hit.badgeLabel,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                      ),
                      maxLines = 1
                    )
                  }
                }

                Spacer(modifier = Modifier.height(6.dp))

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                  Text(
                    text = buildRedHighlightedSearchText(hit.arabic, searchQuery),
                    style = MaterialTheme.typography.bodyLarge.copy(
                      fontFamily = AmiriFontFamily,
                      fontSize = 20.sp,
                      lineHeight = 34.sp,
                      textAlign = TextAlign.Right
                    ),
                    modifier = Modifier.fillMaxWidth()
                  )
                }

                Spacer(modifier = Modifier.height(4.dp))

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                  Text(
                    text = buildRedHighlightedSearchText(hit.urdu, searchQuery),
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontFamily = UrduNastaliqFontFamily,
                      fontSize = 14.sp,
                      lineHeight = 28.sp,
                      platformStyle = PlatformTextStyle(includeFontPadding = true),
                      textAlign = TextAlign.Right,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                  )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                  text = buildRedHighlightedSearchText(hit.english, searchQuery),
                  style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 20.sp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                  text = "Reference: ${hit.reference}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = GoldPrimary
                  )
                )
              }
            }
          }
        }

        if (showAyahs && ayahResults.isNotEmpty()) {
          item {
            Text(
              text = "Quran Verses — Arabic, English & Urdu (${ayahResults.size})",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              ),
              modifier = Modifier.padding(top = 8.dp)
            )
          }

          items(ayahResults, key = { "search_ayah_${it.surahNumber}_${it.verseNumber}" }) { ayah ->
            val surah = surahs.find { it.number == ayah.surahNumber }
            val surahName = surah?.nameEnglish ?: "Surah ${ayah.surahNumber}"

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  if (surah != null) {
                    if (onOpenSurahWithVerse != null) {
                      onOpenSurahWithVerse(surah, ayah.verseNumber)
                    } else {
                      onOpenSurah(surah)
                    }
                  }
                },
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "$surahName (${surah?.nameArabic.orEmpty()}) • Surah ${ayah.surahNumber}:Ayah ${ayah.verseNumber} (Juz ${ayah.juzNumber})",
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = GoldPrimary
                    ),
                    modifier = Modifier.weight(1f)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldPrimary.copy(alpha = 0.12f)
                  ) {
                    Text(
                      text = "Open Ayah ${ayah.surahNumber}:${ayah.verseNumber}",
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                      ),
                      maxLines = 1
                    )
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                  Text(
                    text = buildRedHighlightedSearchText(ayah.textArabic, searchQuery),
                    style = MaterialTheme.typography.bodyLarge.copy(
                      fontFamily = AmiriFontFamily,
                      fontSize = 20.sp,
                      lineHeight = 34.sp,
                      textAlign = TextAlign.Right
                    ),
                    modifier = Modifier.fillMaxWidth()
                  )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = buildRedHighlightedSearchText(ayah.translationEnglish, searchQuery),
                  style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 20.sp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (ayah.translationUrdu.isNotBlank()) {
                  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                      text = buildRedHighlightedSearchText(ayah.translationUrdu, searchQuery),
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = UrduNastaliqFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 28.sp,
                        platformStyle = PlatformTextStyle(includeFontPadding = true),
                        textAlign = TextAlign.Right,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      ),
                      modifier = Modifier.fillMaxWidth()
                    )
                  }
                }
              }
            }
          }
        }

        if (showHadiths && scopedHadithResults.isNotEmpty()) {
          item {
            Text(
              text = when (hadithTraditionScope) {
                "SHIA" -> "Shia Hadith Collections & Narrations (${scopedHadithResults.size})"
                "SUNNI" -> "Sunni Hadith Collections & Narrations (${scopedHadithResults.size})"
                else -> "Hadith Narrations — Sunni & Shia Canonical Editions (${scopedHadithResults.size})"
              },
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              ),
              modifier = Modifier.padding(top = 12.dp)
            )
          }

          items(scopedHadithResults, key = { "search_hadith_${it.collectionKey}_${it.hadithNumber}" }) { hadith ->
            val isShia = ExtendedHadithDataProvider.isShiaCollection(hadith.collectionKey)
            val primaryLabel = if (isShia) {
              ExtendedHadithDataProvider.formatPrimaryEditionBadge(hadith)
            } else {
              hadith.darussalamReference.ifBlank { "Darussalam: #${hadith.hadithNumber}" }
            }
            val inBookLabel = hadith.inBookReference.ifBlank { "Book ${hadith.bookNumber} • Hadith #${hadith.hadithNumber}" }
            val collectionMeta = ExtendedHadithDataProvider.primaryCollections.find { it.key == hadith.collectionKey }
            val traditionLabel = if (isShia || collectionMeta?.tradition == "SHIA") {
              "SHIA • ${collectionMeta?.subTradition ?: "Ithna Ashari / Ahl al-Bayt"}"
            } else {
              "SUNNI • ${collectionMeta?.subTradition ?: "Ahl al-Sunnah"}"
            }

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenHadith(hadith) },
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "${hadith.collectionName} • Book ${hadith.bookNumber} (${hadith.chapterName}) • Hadith #${hadith.hadithNumber}",
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  SahihGradeBadge(grade = hadith.grade.ifBlank { "Verified Source" })
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Separate Clickable Edition Numbering + Tradition Badges
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isShia) GoldPrimary.copy(alpha = 0.22f) else EmeraldPrimary.copy(alpha = 0.18f)
                  ) {
                    Text(
                      text = traditionLabel,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isShia) GoldPrimary else EmeraldPrimary
                      )
                    )
                  }

                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldPrimary.copy(alpha = 0.14f),
                    modifier = Modifier.clickable {
                      onSearchQueryChanged("${hadith.collectionKey} ${hadith.hadithNumber}")
                    }
                  ) {
                    Text(
                      text = primaryLabel,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                      )
                    )
                  }

                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GoldPrimary.copy(alpha = 0.16f)
                  ) {
                    Text(
                      text = inBookLabel,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                      )
                    )
                  }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                  text = "Reference: ${hadith.reference} • Narrator: ${hadith.narrator} • Scholar: ${hadith.gradingScholar}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.secondary
                  )
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (hadith.arabicText.isNotBlank()) {
                  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                      text = buildRedHighlightedSearchText(hadith.arabicText, searchQuery),
                      style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = AmiriFontFamily,
                        fontSize = 19.sp,
                        lineHeight = 32.sp,
                        textAlign = TextAlign.Right
                      ),
                      modifier = Modifier.fillMaxWidth(),
                      maxLines = 5,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                  Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                  text = buildRedHighlightedSearchText(hadith.englishText, searchQuery),
                  style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 20.sp),
                  maxLines = 6,
                  overflow = TextOverflow.Ellipsis
                )

                if (hadith.urduText.isNotBlank()) {
                  Spacer(modifier = Modifier.height(4.dp))
                  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                      text = buildRedHighlightedSearchText(hadith.urduText, searchQuery),
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = UrduNastaliqFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 28.sp,
                        platformStyle = PlatformTextStyle(includeFontPadding = true),
                        textAlign = TextAlign.Right,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      ),
                      modifier = Modifier.fillMaxWidth(),
                      maxLines = 5,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
              }
            }
          }
        }

        if (showTafsirs && scopedTafsirResults.isNotEmpty()) {
          item {
            Text(
              text = when (hadithTraditionScope) {
                "SHIA" -> "Shia Tafsir Collections & Commentary Matches (${scopedTafsirResults.size})"
                "SUNNI" -> "Sunni Tafsir Collections & Commentary Matches (${scopedTafsirResults.size})"
                else -> "Sunni & Shia Tafsir Commentary Matches (${scopedTafsirResults.size})"
              },
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              ),
              modifier = Modifier.padding(top = 10.dp)
            )
          }

          items(scopedTafsirResults, key = { "search_tafsir_${it.edition.key}_${it.verseRef}" }) { tafsir ->
            val parts = tafsir.verseRef.split(":")
            val sNum = parts.getOrNull(0)?.toIntOrNull() ?: 1
            val vNum = parts.getOrNull(1)?.toIntOrNull()
            val surah = surahs.find { it.number == sNum }
            val isShiaTafsir = tafsir.edition.tradition.equals("SHIA", ignoreCase = true)

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  if (surah != null) {
                    if (onOpenSurahWithVerse != null) {
                      onOpenSurahWithVerse(surah, vNum)
                    } else {
                      onOpenSurah(surah)
                    }
                  }
                },
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "${tafsir.edition.titleEnglish} (${tafsir.edition.titleArabic}) • Surah ${tafsir.verseRef}",
                      style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isShiaTafsir) GoldPrimary else EmeraldPrimary
                      )
                    )
                    Text(
                      text = "Author: ${tafsir.edition.scholarName} • ${tafsir.edition.eraBadge}",
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isShiaTafsir) GoldPrimary.copy(alpha = 0.2f) else EmeraldPrimary.copy(alpha = 0.12f)
                  ) {
                    Text(
                      text = if (isShiaTafsir) "SHIA TAFSIR • Open ${tafsir.verseRef}" else "SUNNI TAFSIR • Open ${tafsir.verseRef}",
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isShiaTafsir) GoldPrimary else EmeraldPrimary
                      ),
                      maxLines = 1
                    )
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (tafsir.commentaryArabic.isNotBlank()) {
                  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                      text = buildRedHighlightedSearchText(tafsir.commentaryArabic, searchQuery),
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = AmiriFontFamily,
                        fontSize = 18.sp,
                        lineHeight = 32.sp,
                        textAlign = TextAlign.Right
                      ),
                      modifier = Modifier.fillMaxWidth(),
                      maxLines = 4,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                }

                if (tafsir.commentaryEnglish.isNotBlank()) {
                  Text(
                    text = buildRedHighlightedSearchText(tafsir.commentaryEnglish, searchQuery),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 20.sp),
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                  )
                }

                if (tafsir.commentaryUrdu.isNotBlank()) {
                  Spacer(modifier = Modifier.height(4.dp))
                  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                      text = buildRedHighlightedSearchText(tafsir.commentaryUrdu, searchQuery),
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = UrduNastaliqFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 28.sp,
                        platformStyle = PlatformTextStyle(includeFontPadding = true),
                        textAlign = TextAlign.Right,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      ),
                      modifier = Modifier.fillMaxWidth(),
                      maxLines = 4,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
              }
            }
          }
        }

        // Classical Islamic Books (Sunni & Shia Books Catalog)
        if (showExtraSources && classicalBookHits.isNotEmpty()) {
          item {
            Text(
              text = "Classical Islamic Books & History (${classicalBookHits.size})",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              ),
              modifier = Modifier.padding(top = 10.dp)
            )
          }

          items(classicalBookHits, key = { "search_book_${it.id}" }) { bookHit ->
            val isShiaBook = bookHit.tradition.equals("SHIA", ignoreCase = true)
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenLibraryBooks() },
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "${bookHit.bookTitleEnglish} (${bookHit.bookTitleArabic})",
                      style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isShiaBook) GoldPrimary else EmeraldPrimary
                      )
                    )
                    Text(
                      text = "Author: ${bookHit.author} • ${bookHit.chapterTitle}",
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isShiaBook) GoldPrimary.copy(alpha = 0.2f) else EmeraldPrimary.copy(alpha = 0.14f)
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = if (isShiaBook) GoldPrimary else EmeraldPrimary,
                        modifier = Modifier.size(12.dp)
                      )
                      Text(
                        text = "${bookHit.tradition} • ${bookHit.category}",
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontWeight = FontWeight.Bold,
                          color = if (isShiaBook) GoldPrimary else EmeraldPrimary
                        ),
                        maxLines = 1
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (bookHit.arabicExcerpt.isNotBlank()) {
                  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                      text = buildRedHighlightedSearchText(bookHit.arabicExcerpt, searchQuery),
                      style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = AmiriFontFamily,
                        fontSize = 18.sp,
                        lineHeight = 32.sp,
                        textAlign = TextAlign.Right
                      ),
                      modifier = Modifier.fillMaxWidth(),
                      maxLines = 4,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                  text = buildRedHighlightedSearchText(bookHit.englishExcerpt, searchQuery),
                  style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 20.sp),
                  maxLines = 4,
                  overflow = TextOverflow.Ellipsis
                )

                if (bookHit.urduExcerpt.isNotBlank()) {
                  Spacer(modifier = Modifier.height(4.dp))
                  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                      text = buildRedHighlightedSearchText(bookHit.urduExcerpt, searchQuery),
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = UrduNastaliqFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 28.sp,
                        platformStyle = PlatformTextStyle(includeFontPadding = true),
                        textAlign = TextAlign.Right,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      ),
                      modifier = Modifier.fillMaxWidth(),
                      maxLines = 3,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Source: ${bookHit.primarySourceCitation}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = GoldPrimary,
                    fontWeight = FontWeight.SemiBold
                  )
                )
              }
            }
          }
        }

        if (totalCount == 0 && !isSearchingRemote) {
          item {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "No matching records found in offline or online sources for \"$searchQuery\"",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Search across 6,236 Quran verses, 16+ Sunni & Shia Tafsir exegeses, 36 Sunni & Shia Hadith books, Classical Books, Complete Salah, and Nimaz Janaza by reference (e.g. 2:255, Bukhari 5027, Al-Kafi 1) or keywords in English, Urdu, or Arabic.",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
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
