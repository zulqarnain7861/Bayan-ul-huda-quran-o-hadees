package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ExtendedHadithDataProvider
import com.example.data.local.entities.BookmarkEntity
import com.example.data.local.entities.NoteEntity
import com.example.data.model.IslamicBooksCatalog
import com.example.data.model.IslamicClassicalBook
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ScheherazadeFontFamily
import com.example.ui.theme.UrduNastaliqFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun buildHighlightedAnnotatedText(
  text: String,
  rawQuery: String,
  highlightBackground: Color = GoldPrimary.copy(alpha = 0.35f),
  highlightTextColor: Color = EmeraldPrimary
): AnnotatedString {
  val cleanQuery = rawQuery.trim()
  if (cleanQuery.isBlank() || text.isBlank()) {
    return AnnotatedString(text)
  }
  return buildAnnotatedString {
    append(text)
    val tokens = listOf(cleanQuery) + cleanQuery.split(" ").filter { it.length >= 2 }
    for (token in tokens.distinct()) {
      var startIndex = text.indexOf(token, ignoreCase = true)
      var guard = 0
      while (startIndex >= 0 && guard < 12) {
        val endIndex = (startIndex + token.length).coerceAtMost(text.length)
        addStyle(
          style = SpanStyle(
            background = highlightBackground,
            color = highlightTextColor,
            fontWeight = FontWeight.Bold
          ),
          start = startIndex,
          end = endIndex
        )
        startIndex = text.indexOf(token, startIndex + token.length, ignoreCase = true)
        guard++
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(
  bookmarks: List<BookmarkEntity>,
  notes: List<NoteEntity>,
  onRemoveBookmark: (String) -> Unit,
  onSaveNote: (String, String, String) -> Unit,
  onDeleteNote: (Long) -> Unit,
  downloadManager: com.example.data.download.DownloadManager? = null,
  currentThemeMode: com.example.data.prayer.AppThemeMode = com.example.data.prayer.AppThemeMode.LIGHT,
  onSelectThemeMode: (com.example.data.prayer.AppThemeMode) -> Unit = {},
  onPlayDownloadedAudio: (Int, Int, String) -> Unit = { _, _, _ -> },
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val historyPrefs = remember(context) {
    context.getSharedPreferences("bayan_library_history_prefs", Context.MODE_PRIVATE)
  }

  // Top-level Library tabs:
  // 0 = History of Islam (top-level, immediately visible without scrolling or submenu)
  // 1 = All Islamic Books
  // 2 = Bookmarks
  // 3 = Notes
  // 4 = Offline Library
  var selectedTab by remember { mutableIntStateOf(0) }
  var showAddNoteDialog by remember { mutableStateOf(false) }
  var initialNoteReference by remember { mutableStateOf("") }

  // History of Islam state (in Library)
  var historyTraditionFilter by remember { mutableStateOf("BOTH") } // "BOTH", "SUNNI", "SHIA"
  var historySearchQuery by remember { mutableStateOf("") }
  var lastReadHistoryBookId by remember {
    mutableStateOf(historyPrefs.getString("last_history_book_id", "") ?: "")
  }
  var lastReadHistoryChapterIdx by remember {
    mutableIntStateOf(historyPrefs.getInt("last_history_chapter_idx", 0))
  }

  // Classical Islamic Books state
  var selectedTraditionFilter by remember { mutableStateOf("BOTH") } // "SUNNI", "SHIA", "BOTH"
  var selectedCategory by remember { mutableStateOf("All") }
  var bookSearchQuery by remember { mutableStateOf("") }
  var showEntryTimeOnCards by remember { mutableStateOf(true) }
  var sortByEntryTimeDesc by remember { mutableStateOf(false) }
  var currentPageIndex by remember { mutableIntStateOf(0) }
  var itemsPerPage by remember { mutableIntStateOf(10) }
  var retryTriggerCount by remember { mutableIntStateOf(0) }
  var openedBook by remember { mutableStateOf<IslamicClassicalBook?>(null) }
  var selectedChapterIndex by remember { mutableIntStateOf(0) }
  var activeReaderHighlightQuery by remember { mutableStateOf("") }

  fun openBookAtChapter(book: IslamicClassicalBook, chapterIdx: Int, highlightQuery: String = "") {
    val safeIdx = chapterIdx.coerceIn(0, (book.chapters.size - 1).coerceAtLeast(0))
    selectedChapterIndex = safeIdx
    activeReaderHighlightQuery = highlightQuery
    openedBook = book
    if (book.category.equals("History of Islam", ignoreCase = true) ||
      book.category.equals("Seerah & History", ignoreCase = true)
    ) {
      lastReadHistoryBookId = book.id
      lastReadHistoryChapterIdx = safeIdx
      historyPrefs.edit()
        .putString("last_history_book_id", book.id)
        .putInt("last_history_chapter_idx", safeIdx)
        .apply()
    }
  }

  // If a Classical or History Book is open in full reader view
  openedBook?.let { book ->
    BackHandler {
      openedBook = null
    }
    IslamicBookReaderView(
      book = book,
      selectedChapterIndex = selectedChapterIndex.coerceIn(0, (book.chapters.size - 1).coerceAtLeast(0)),
      initialHighlightQuery = activeReaderHighlightQuery,
      onSelectChapter = { idx ->
        selectedChapterIndex = idx
        if (book.category.equals("History of Islam", ignoreCase = true) ||
          book.category.equals("Seerah & History", ignoreCase = true)
        ) {
          lastReadHistoryBookId = book.id
          lastReadHistoryChapterIdx = idx
          historyPrefs.edit()
            .putString("last_history_book_id", book.id)
            .putInt("last_history_chapter_idx", idx)
            .apply()
        }
      },
      onJumpToBookAndChapter = { targetBook, targetChapterIdx ->
        openBookAtChapter(targetBook, targetChapterIdx, activeReaderHighlightQuery)
      },
      onClose = { openedBook = null },
      onNavigateBook = { delta ->
        val allBooks = IslamicBooksCatalog.allSunniAndShiaBooks
        val idx = allBooks.indexOfFirst { it.id == book.id }
        val nextIdx = idx + delta
        if (nextIdx in allBooks.indices) {
          openBookAtChapter(allBooks[nextIdx], 0, "")
        }
      },
      onAddBookNote = { ref ->
        initialNoteReference = ref
        showAddNoteDialog = true
      }
    )

    if (showAddNoteDialog) {
      AddNoteDialog(
        initialReference = initialNoteReference,
        onDismiss = { showAddNoteDialog = false },
        onSave = { ref, title, content ->
          onSaveNote(ref, title, content)
          showAddNoteDialog = false
          Toast.makeText(context, "Study note saved", Toast.LENGTH_SHORT).show()
        }
      )
    }
    return
  }

  fun findMatchingChapterIndex(book: IslamicClassicalBook, rawQuery: String): Int {
    val q = com.example.data.remote.SecurityIntegrityGuard.sanitizeSearchQuery(rawQuery)
    if (q.isEmpty()) return -1
    val normLatin = ExtendedHadithDataProvider.normalizeLatinForSearch(q)
    val normArabic = ExtendedHadithDataProvider.normalizeArabicForSearch(q)
    val latinTokens = normLatin.split(" ").filter { it.isNotBlank() }

    return book.chapters.indexOfFirst { ch ->
      ch.chapterNumber.toString() == q ||
        ch.titleEnglish.contains(q, ignoreCase = true) ||
        ch.titleArabic.contains(q) ||
        ch.arabicExcerpt.contains(q) ||
        ch.transliteration.contains(q, ignoreCase = true) ||
        ch.englishContent.contains(q, ignoreCase = true) ||
        ch.urduContent.contains(q) ||
        ch.scholarlyNotes.contains(q, ignoreCase = true) ||
        ch.crossReferences.contains(q, ignoreCase = true) ||
        (normArabic.isNotBlank() && ExtendedHadithDataProvider.normalizeArabicForSearch(
          "${ch.titleArabic} ${ch.arabicExcerpt} ${ch.urduContent}"
        ).contains(normArabic)) ||
        (latinTokens.isNotEmpty() && latinTokens.all { tok ->
          val hay = ExtendedHadithDataProvider.normalizeLatinForSearch(
            "${ch.chapterNumber} ${ch.titleEnglish} ${ch.transliteration} ${ch.englishContent} ${ch.scholarlyNotes} ${ch.crossReferences}"
          )
          hay.contains(tok)
        })
    }
  }

  // History of Islam Books & Search Results
  val baseHistoryBooks = remember(historyTraditionFilter) {
    IslamicBooksCatalog.getHistoryBooksByTradition(historyTraditionFilter)
  }

  val historyBooksWithSerial = remember(baseHistoryBooks) {
    baseHistoryBooks.mapIndexed { idx, book -> (idx + 1) to book }
  }

  val filteredHistoryBooksWithSerial = remember(historyBooksWithSerial, historySearchQuery) {
    val q = historySearchQuery.trim()
    if (q.isBlank()) {
      historyBooksWithSerial
    } else {
      val asNumber = q.removePrefix("#").trim().toIntOrNull()
      val matchingInsideBookIds = IslamicBooksCatalog.searchInsideBooks(
        booksToSearch = baseHistoryBooks,
        rawQuery = q
      ).map { it.book.id }.toSet()

      historyBooksWithSerial.filter { (serialNumber, book) ->
        (asNumber != null && (serialNumber == asNumber || book.number == asNumber)) ||
          book.id in matchingInsideBookIds ||
          book.titleEnglish.contains(q, ignoreCase = true) ||
          book.titleArabic.contains(q) ||
          book.titleUrdu.contains(q) ||
          book.author.contains(q, ignoreCase = true) ||
          findMatchingChapterIndex(book, q) >= 0
      }
    }
  }

  val historyLineMatches = remember(baseHistoryBooks, historySearchQuery) {
    val q = historySearchQuery.trim()
    if (q.isBlank()) {
      emptyList()
    } else {
      val asNumber = q.removePrefix("#").trim().toIntOrNull()
      val targetBooks = if (asNumber != null) {
        val serialMatch = historyBooksWithSerial.firstOrNull { it.first == asNumber }?.second
        if (serialMatch != null) listOf(serialMatch) else baseHistoryBooks
      } else {
        baseHistoryBooks
      }
      IslamicBooksCatalog.searchInsideBooks(
        booksToSearch = targetBooks,
        rawQuery = q
      )
    }
  }

  // All Islamic Books Filtered & Paginated
  val filteredBooks = remember(selectedTraditionFilter, selectedCategory, bookSearchQuery, sortByEntryTimeDesc, retryTriggerCount) {
    val base = IslamicBooksCatalog.getBooksByTradition(selectedTraditionFilter).filter { book ->
      val matchesCat = selectedCategory == "All" ||
        selectedCategory.startsWith("All ") ||
        book.category.equals(selectedCategory, ignoreCase = true) ||
        (selectedCategory.equals("History of Islam", ignoreCase = true) &&
          (book.category.equals("History of Islam", ignoreCase = true) || book.category.equals("Seerah & History", ignoreCase = true)))
      val q = com.example.data.remote.SecurityIntegrityGuard.sanitizeSearchQuery(bookSearchQuery)
      val matchesBookMeta = q.isEmpty() ||
        book.titleEnglish.contains(q, ignoreCase = true) ||
        book.titleArabic.contains(q) ||
        book.titleUrdu.contains(q) ||
        book.author.contains(q, ignoreCase = true) ||
        book.summary.contains(q, ignoreCase = true) ||
        book.numberingSystem.contains(q, ignoreCase = true) ||
        book.number.toString() == q
      val matchesChapterOrSermon = q.isNotEmpty() && findMatchingChapterIndex(book, q) >= 0
      matchesCat && (matchesBookMeta || matchesChapterOrSermon)
    }
    if (sortByEntryTimeDesc) {
      base.sortedByDescending { it.number }
    } else {
      base.sortedBy { it.number }
    }
  }

  val allBooksLineMatches = remember(selectedTraditionFilter, bookSearchQuery) {
    val q = bookSearchQuery.trim()
    if (q.isBlank()) emptyList()
    else IslamicBooksCatalog.searchInsideBooks(
      booksToSearch = IslamicBooksCatalog.getBooksByTradition(selectedTraditionFilter),
      rawQuery = q
    )
  }

  val totalPages = remember(filteredBooks.size, itemsPerPage) {
    ((filteredBooks.size + itemsPerPage - 1) / itemsPerPage).coerceAtLeast(1)
  }
  val safePageIndex = currentPageIndex.coerceIn(0, (totalPages - 1).coerceAtLeast(0))
  val paginatedBooks = remember(filteredBooks, safePageIndex, itemsPerPage) {
    val start = safePageIndex * itemsPerPage
    if (start >= filteredBooks.size) emptyList()
    else filteredBooks.subList(start, (start + itemsPerPage).coerceAtMost(filteredBooks.size))
  }

  Box(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Non-clipped top section bar: History of Islam is the very first top-level item,
      // fully visible without scrolling or opening any submenu.
      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 8.dp
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          modifier = Modifier.testTag("library_tab_history"),
          text = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                Icons.Default.HistoryEdu,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (selectedTab == 0) EmeraldPrimary else GoldPrimary
              )
              Text(
                text = "History (${IslamicBooksCatalog.historyBooks.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1
              )
            }
          }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          modifier = Modifier.testTag("library_tab_islamic_books"),
          text = {
            Text(
              text = "All Books (${IslamicBooksCatalog.allSunniAndShiaBooks.size})",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              maxLines = 1
            )
          }
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          modifier = Modifier.testTag("library_tab_bookmarks"),
          text = {
            Text(
              text = "Bookmarks (${bookmarks.size})",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              maxLines = 1
            )
          }
        )
        Tab(
          selected = selectedTab == 3,
          onClick = { selectedTab = 3 },
          modifier = Modifier.testTag("library_tab_notes"),
          text = {
            Text(
              text = "Notes (${notes.size})",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              maxLines = 1
            )
          }
        )
        Tab(
          selected = selectedTab == 4,
          onClick = { selectedTab = 4 },
          modifier = Modifier.testTag("library_tab_offline_downloads"),
          text = {
            Text(
              text = "Offline Library",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              maxLines = 1
            )
          }
        )
      }

      when (selectedTab) {
        0 -> {
          // =========================================================================
          // TAB 0: HISTORY OF ISLAM (Top-level in Library)
          // Shows prominent header card, Sunni/Shia/All tradition chips, historical
          // event sub-filters (Qadisiyyah, Waqia Hurrah, Battle of Yarmouk, Badr, etc.),
          // one unified search bar (by book number, keyword, or full/partial sentence
          // in Urdu, English, and Arabic), highlighted line-level search results, and
          // the numbered list of all important History books.
          // =========================================================================
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Top-level History Banner Card
            item {
              ElevatedCard(
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("library_history_top_banner_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(
                  containerColor = EmeraldPrimary.copy(alpha = 0.10f)
                )
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Icon(
                      Icons.Default.HistoryEdu,
                      contentDescription = null,
                      tint = EmeraldPrimary,
                      modifier = Modifier.size(22.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = "History of Islam — تاریخِ اسلام — التاريخ الإسلامي",
                        style = MaterialTheme.typography.titleMedium.copy(
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                      )
                      Text(
                        text = "${IslamicBooksCatalog.historyBooks.size} Verified Sunni & Shia Historical Works • Arabic, English & Urdu",
                        style = MaterialTheme.typography.bodySmall.copy(
                          fontSize = 11.sp,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                      )
                    }
                  }

                  // Resume Last Read History Book button if available
                  val lastBook = remember(lastReadHistoryBookId) {
                    IslamicBooksCatalog.historyBooks.find { it.id == lastReadHistoryBookId }
                  }
                  if (lastBook != null) {
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = GoldPrimary.copy(alpha = 0.18f),
                      modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                          openBookAtChapter(lastBook, lastReadHistoryChapterIdx, "")
                        }
                        .testTag("resume_history_reading_banner")
                    ) {
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = "Continue Reading: ${lastBook.titleEnglish} (Ch. ${lastReadHistoryChapterIdx + 1})",
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                          ),
                          modifier = Modifier.weight(1f)
                        )
                        Text(
                          text = "Open →",
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                          )
                        )
                      }
                    }
                  }
                }
              }
            }

            // Tradition Filter Chips (Wrapped FlowRow so nothing is ever cut off)
            item {
              FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                FilterChip(
                  selected = historyTraditionFilter == "BOTH",
                  onClick = { historyTraditionFilter = "BOTH" },
                  label = {
                    Text(
                      text = "All History (${IslamicBooksCatalog.historyBooks.size})",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                  ),
                  modifier = Modifier.height(32.dp).testTag("history_tradition_all_chip")
                )
                FilterChip(
                  selected = historyTraditionFilter == "SUNNI",
                  onClick = { historyTraditionFilter = "SUNNI" },
                  label = {
                    Text(
                      text = "Sunni History (${IslamicBooksCatalog.getHistoryBooksByTradition("SUNNI").size})",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary,
                    selectedLabelColor = Color.White
                  ),
                  modifier = Modifier.height(32.dp).testTag("history_tradition_sunni_chip")
                )
                FilterChip(
                  selected = historyTraditionFilter == "SHIA",
                  onClick = { historyTraditionFilter = "SHIA" },
                  label = {
                    Text(
                      text = "Shia History (${IslamicBooksCatalog.getHistoryBooksByTradition("SHIA").size})",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldPrimary,
                    selectedLabelColor = Color.Black
                  ),
                  modifier = Modifier.height(32.dp).testTag("history_tradition_shia_chip")
                )
              }
            }

            // Unified History Search Bar:
            // Works by (a) book serial/catalog number, (b) any keyword, (c) full or partial sentence
            // in English, Urdu, or Arabic (ignoring diacritics & normalizing Urdu/Arabic letter variants)
            item {
              OutlinedTextField(
                value = historySearchQuery,
                onValueChange = { historySearchQuery = it },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("library_history_search_input"),
                placeholder = {
                  Text(
                    text = "Search by book #, keyword, or sentence (English, اردو, العربية)...",
                    fontSize = 12.sp
                  )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                  if (historySearchQuery.isNotEmpty()) {
                    IconButton(onClick = { historySearchQuery = "" }) {
                      Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                  }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
              )
            }

            // Historical Events Sub-Filters (Wrapped FlowRow so all sub-filters are visible & never cut off)
            item {
              Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  text = "Major Historical Events & Battles (Tap to filter or search inside books):",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
                FlowRow(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  IslamicBooksCatalog.historySubFilters.forEach { eventFilter ->
                    val isAllEvents = eventFilter.equals("All Events", ignoreCase = true)
                    val isSelected = if (isAllEvents) {
                      historySearchQuery.isBlank()
                    } else {
                      historySearchQuery.equals(eventFilter, ignoreCase = true)
                    }
                    FilterChip(
                      selected = isSelected,
                      onClick = {
                        historySearchQuery = if (isAllEvents || isSelected) "" else eventFilter
                      },
                      label = {
                        Text(
                          text = eventFilter,
                          fontSize = 11.sp,
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                      },
                      colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.White
                      ),
                      modifier = Modifier
                        .height(30.dp)
                        .testTag("history_subfilter_${eventFilter.lowercase().replace(" ", "_")}")
                    )
                  }
                }
              }
            }

            // Inside-Book Matching Lines Section (when user searches by keyword, sentence, or book #)
            if (historySearchQuery.isNotBlank()) {
              item {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = EmeraldPrimary.copy(alpha = 0.10f),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "${historyLineMatches.size} Matching Passages in History Books for \"${historySearchQuery.trim()}\"",
                      style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                      ),
                      modifier = Modifier.weight(1f)
                    )
                    TextButton(
                      onClick = { historySearchQuery = "" },
                      contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                      modifier = Modifier.height(26.dp)
                    ) {
                      Text("Clear", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }

              if (historyLineMatches.isEmpty()) {
                item {
                  Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                  ) {
                    Text(
                      text = "No matching lines found for \"${historySearchQuery}\" in the selected History books. Try another keyword, Arabic/Urdu phrase, or switch to 'All History'.",
                      style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      ),
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                    )
                  }
                }
              } else {
                itemsIndexed(
                  items = historyLineMatches,
                  key = { idx, match -> "hist_match_${match.book.id}_${match.chapterIndex}_$idx" }
                ) { _, match ->
                  val isShiaMatch = match.book.tradition.equals("SHIA", ignoreCase = true)
                  ElevatedCard(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        openBookAtChapter(match.book, match.chapterIndex, historySearchQuery.trim())
                      }
                      .testTag("history_line_match_${match.book.number}_${match.chapterNumber}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(
                      containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                  ) {
                    Column(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                      verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      // Book Name & Tradition Badge (Full width, no narrow column)
                      FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                      ) {
                        Surface(
                          shape = RoundedCornerShape(6.dp),
                          color = if (isShiaMatch) GoldPrimary.copy(alpha = 0.2f) else EmeraldPrimary.copy(alpha = 0.15f)
                        ) {
                          Text(
                            text = "${match.book.tradition} • ${match.matchedLanguage}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                              fontWeight = FontWeight.Bold,
                              color = if (isShiaMatch) GoldPrimary else EmeraldPrimary
                            )
                          )
                        }
                        Text(
                          text = "Ch. ${match.chapterNumber}: ${match.chapterTitle}",
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                          ),
                          modifier = Modifier.align(Alignment.CenterVertically)
                        )
                      }

                      Text(
                        text = "${match.book.titleEnglish} — ${match.book.author}",
                        style = MaterialTheme.typography.titleSmall.copy(
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                      )

                      // Highlighted Matching Line inside Book Text
                      Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                      ) {
                        val isRtlLine = match.matchedLanguage == "AR" || match.matchedLanguage == "UR"
                        Text(
                          text = buildHighlightedAnnotatedText(
                            text = match.matchedLine,
                            rawQuery = historySearchQuery
                          ),
                          style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = when (match.matchedLanguage) {
                              "AR" -> AmiriFontFamily
                              "UR" -> UrduNastaliqFontFamily
                              else -> MaterialTheme.typography.bodyMedium.fontFamily
                            },
                            fontSize = if (match.matchedLanguage == "AR") 17.sp else 14.sp,
                            lineHeight = if (match.matchedLanguage == "AR") 28.sp else 21.sp,
                            textAlign = if (isRtlLine) TextAlign.Right else TextAlign.Start,
                            color = MaterialTheme.colorScheme.onSurface
                          ),
                          modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                        )
                      }

                      Text(
                        text = "Tap to open ${match.book.titleEnglish} at Chapter ${match.chapterNumber} →",
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontWeight = FontWeight.Bold,
                          color = EmeraldPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                      )
                    }
                  }
                }
              }
            }

            // Section Header for Numbered History Books List
            item {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Important History Books (${filteredHistoryBooksWithSerial.size})",
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                )
                Text(
                  text = "Serial # • Title • Author • Language",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
              }
            }

            // Numbered List of Important History Books
            items(
              items = filteredHistoryBooksWithSerial,
              key = { (_, book) -> "history_book_${book.id}" }
            ) { (serialNumber, book) ->
              val isShiaBook = book.tradition.equals("SHIA", ignoreCase = true)
              val matchedChapterIdx = remember(book, historySearchQuery) {
                findMatchingChapterIndex(book, historySearchQuery)
              }
              FullWidthIslamicBookCard(
                serialNumber = serialNumber,
                book = book,
                isShiaBook = isShiaBook,
                matchedChapterIdx = matchedChapterIdx,
                activeSearchQuery = historySearchQuery,
                showEntryTimeOnCards = showEntryTimeOnCards,
                onOpenBook = { targetChapterIdx ->
                  openBookAtChapter(book, targetChapterIdx, historySearchQuery.trim())
                },
                cardTestTag = "history_book_card_$serialNumber"
              )
            }
          }
        }

        1 -> {
          // =========================================================================
          // TAB 1: ALL CLASSICAL & AUTHENTIC SUNNI + SHIA ISLAMIC BOOKS
          // Includes prominent shortcut card to History of Islam at the top level
          // and full-width book cards without narrow-column squeezing.
          // =========================================================================
          Column(modifier = Modifier.fillMaxSize()) {
            // Top-level History Quick Card inside All Books tab as well
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = EmeraldPrimary.copy(alpha = 0.12f),
              border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clickable { selectedTab = 0 }
                .testTag("library_books_open_history_card")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(
                    Icons.Default.HistoryEdu,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                  )
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "History of Islam (${IslamicBooksCatalog.historyBooks.size} Sunni & Shia History Books)",
                      style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                      )
                    )
                    Text(
                      text = "Qadisiyyah • Waqia Hurrah • Yarmouk • Badr • Karbala • Full-Text Search",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    )
                  }
                }
                Text(
                  text = "Open →",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                )
              }
            }

            // Sunni / Shia / Both Tradition Filter + Entry Time + Retry Bar
            LazyRow(
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              item {
                FilterChip(
                  selected = selectedTraditionFilter == "SUNNI",
                  onClick = {
                    selectedTraditionFilter = "SUNNI"
                    selectedCategory = "All"
                    currentPageIndex = 0
                  },
                  label = {
                    Text(
                      text = "Sunni Books (${IslamicBooksCatalog.allSunniBooks.size})",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary,
                    selectedLabelColor = Color.White
                  ),
                  modifier = Modifier.testTag("library_books_sunni_chip")
                )
              }
              item {
                FilterChip(
                  selected = selectedTraditionFilter == "SHIA",
                  onClick = {
                    selectedTraditionFilter = "SHIA"
                    selectedCategory = "All"
                    currentPageIndex = 0
                  },
                  label = {
                    Text(
                      text = "Shia Books (${IslamicBooksCatalog.allShiaBooks.size})",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldPrimary,
                    selectedLabelColor = Color.Black
                  ),
                  modifier = Modifier.testTag("library_books_shia_chip")
                )
              }
              item {
                FilterChip(
                  selected = selectedTraditionFilter == "BOTH",
                  onClick = {
                    selectedTraditionFilter = "BOTH"
                    selectedCategory = "All"
                    currentPageIndex = 0
                  },
                  label = {
                    Text(
                      text = "All Books (${IslamicBooksCatalog.allSunniAndShiaBooks.size})",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                  ),
                  modifier = Modifier.testTag("library_books_both_chip")
                )
              }
              item {
                FilterChip(
                  selected = showEntryTimeOnCards,
                  onClick = {
                    if (!showEntryTimeOnCards) {
                      showEntryTimeOnCards = true
                    } else {
                      sortByEntryTimeDesc = !sortByEntryTimeDesc
                    }
                    currentPageIndex = 0
                  },
                  leadingIcon = {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                  },
                  label = {
                    Text(
                      text = if (sortByEntryTimeDesc) "Entry Time (Newest)" else "Entry Time",
                      fontSize = 11.sp
                    )
                  },
                  modifier = Modifier.testTag("library_entry_time_toggle_chip")
                )
              }
              item {
                OutlinedButton(
                  onClick = {
                    retryTriggerCount++
                    currentPageIndex = 0
                    Toast.makeText(
                      context,
                      "Verified ${filteredBooks.size} Islamic Books reloaded (Arabic, English & Urdu ready)",
                      Toast.LENGTH_SHORT
                    ).show()
                  },
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier
                    .height(32.dp)
                    .testTag("library_retry_books_button")
                ) {
                  Icon(Icons.Default.Refresh, contentDescription = "Retry", modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Retry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }

            OutlinedTextField(
              value = bookSearchQuery,
              onValueChange = {
                bookSearchQuery = it
                currentPageIndex = 0
              },
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp)
                .testTag("islamic_books_search_input"),
              placeholder = {
                Text(
                  text = "Search by book #, keyword, or sentence (English, اردو, العربية)...",
                  fontSize = 12.sp
                )
              },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
              trailingIcon = {
                if (bookSearchQuery.isNotEmpty()) {
                  IconButton(onClick = {
                    bookSearchQuery = ""
                    currentPageIndex = 0
                  }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                  }
                }
              },
              singleLine = true,
              shape = RoundedCornerShape(12.dp)
            )

            LazyRow(
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              items(IslamicBooksCatalog.categories) { cat ->
                FilterChip(
                  selected = selectedCategory == cat || (selectedCategory == "All" && cat.startsWith("All")),
                  onClick = {
                    selectedCategory = if (cat.startsWith("All")) "All" else cat
                    currentPageIndex = 0
                  },
                  label = { Text(cat, fontSize = 11.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary,
                    selectedLabelColor = Color.White
                  )
                )
              }
            }

            // Fast Pagination Bar for Books
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(10.dp))
                .testTag("library_books_pagination_bar")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                OutlinedButton(
                  onClick = { if (safePageIndex > 0) currentPageIndex = safePageIndex - 1 },
                  enabled = safePageIndex > 0,
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier
                    .height(30.dp)
                    .testTag("library_pagination_prev_button")
                ) {
                  Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = "Prev Page", modifier = Modifier.size(15.dp))
                  Text("Prev", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = "Page ${safePageIndex + 1} of $totalPages • ${filteredBooks.size} Books",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("library_pagination_status_label")
                  )
                  TextButton(
                    onClick = {
                      itemsPerPage = when (itemsPerPage) {
                        10 -> 20
                        20 -> IslamicBooksCatalog.allSunniAndShiaBooks.size
                        else -> 10
                      }
                      currentPageIndex = 0
                    },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier = Modifier
                      .height(20.dp)
                      .testTag("library_page_size_toggle_button")
                  ) {
                    Text(
                      text = "$itemsPerPage/page (Tap to change)",
                      fontSize = 10.sp,
                      color = GoldPrimary
                    )
                  }
                }

                OutlinedButton(
                  onClick = { if (safePageIndex < totalPages - 1) currentPageIndex = safePageIndex + 1 },
                  enabled = safePageIndex < totalPages - 1,
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier
                    .height(30.dp)
                    .testTag("library_pagination_next_button")
                ) {
                  Text("Next", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "Next Page", modifier = Modifier.size(15.dp))
                }
              }
            }

            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(12.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // If user typed a search query in All Books, also display highlighted line matches
              if (bookSearchQuery.isNotBlank() && allBooksLineMatches.isNotEmpty()) {
                item {
                  Text(
                    text = "Matching Lines Inside Books (${allBooksLineMatches.size}):",
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = EmeraldPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                  )
                }
                itemsIndexed(
                  items = allBooksLineMatches.take(12),
                  key = { idx, match -> "all_book_line_${match.book.id}_${match.chapterIndex}_$idx" }
                ) { _, match ->
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        openBookAtChapter(match.book, match.chapterIndex, bookSearchQuery.trim())
                      }
                  ) {
                    Column(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                      verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                      Text(
                        text = "#${match.book.number} ${match.book.titleEnglish} • Ch. ${match.chapterNumber}: ${match.chapterTitle}",
                        style = MaterialTheme.typography.labelMedium.copy(
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                      )
                      Text(
                        text = buildHighlightedAnnotatedText(match.matchedLine, bookSearchQuery),
                        style = MaterialTheme.typography.bodySmall.copy(
                          fontSize = 13.sp,
                          color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                      )
                    }
                  }
                }
              }

              items(paginatedBooks, key = { it.id }) { book ->
                val isShiaBook = book.tradition.equals("SHIA", ignoreCase = true)
                val matchedChapterIdx = remember(book, bookSearchQuery) {
                  findMatchingChapterIndex(book, bookSearchQuery)
                }
                FullWidthIslamicBookCard(
                  serialNumber = book.number,
                  book = book,
                  isShiaBook = isShiaBook,
                  matchedChapterIdx = matchedChapterIdx,
                  activeSearchQuery = bookSearchQuery,
                  showEntryTimeOnCards = showEntryTimeOnCards,
                  onOpenBook = { targetChapterIdx ->
                    openBookAtChapter(book, targetChapterIdx, bookSearchQuery.trim())
                  },
                  cardTestTag = "islamic_book_card_${book.number}"
                )
              }
            }
          }
        }

        2 -> {
          // Bookmarks Tab
          if (bookmarks.isEmpty()) {
            EmptyStateView(
              icon = Icons.Default.Bookmark,
              title = "No Bookmarks Yet",
              subtitle = "Save inspiring verses, authentic hadiths, and historical chapters while reading to easily revisit them here."
            )
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(12.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(bookmarks, key = { it.itemKey }) { bookmark ->
                Card(
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bookmark_item_${bookmark.itemKey}"),
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(14.dp)
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (bookmark.itemType == "QURAN") EmeraldPrimary.copy(alpha = 0.15f) else GoldPrimary.copy(alpha = 0.15f)
                      ) {
                        Text(
                          text = bookmark.itemType,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (bookmark.itemType == "QURAN") EmeraldPrimary else GoldPrimary
                          )
                        )
                      }

                      Row {
                        IconButton(
                          onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val text = "${bookmark.title}\n\n${bookmark.arabicSnippet}\n\n${bookmark.subtitle}"
                            clipboard.setPrimaryClip(ClipData.newPlainText("Saved Item", text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                          },
                          modifier = Modifier.size(32.dp)
                        ) {
                          Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(
                          onClick = { onRemoveBookmark(bookmark.itemKey) },
                          modifier = Modifier.size(32.dp)
                        ) {
                          Icon(Icons.Default.BookmarkRemove, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                        }
                      }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                      text = bookmark.title,
                      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                      modifier = Modifier.fillMaxWidth()
                    )

                    if (bookmark.arabicSnippet.isNotEmpty()) {
                      Spacer(modifier = Modifier.height(6.dp))
                      Text(
                        text = bookmark.arabicSnippet,
                        style = MaterialTheme.typography.bodyLarge.copy(
                          fontFamily = AmiriFontFamily,
                          fontSize = 19.sp,
                          lineHeight = 32.sp,
                          textAlign = TextAlign.Right
                        ),
                        modifier = Modifier.fillMaxWidth()
                      )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                      text = bookmark.subtitle,
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
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

        3 -> {
          // Study Notes Tab
          if (notes.isEmpty()) {
            EmptyStateView(
              icon = Icons.Default.Note,
              title = "No Personal Notes",
              subtitle = "Record personal reflections, tafsir notes, and lessons from verses, hadiths, and historical works."
            )
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(top = 12.dp, start = 12.dp, end = 12.dp, bottom = 80.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(notes, key = { it.id }) { note ->
                val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(note.updatedAt))

                Card(
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_item_${note.id}"),
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(14.dp)
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = note.referenceKey,
                        style = MaterialTheme.typography.labelMedium.copy(
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                      )
                      IconButton(
                        onClick = { onDeleteNote(note.id) },
                        modifier = Modifier.size(32.dp)
                      ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                      }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                      text = note.title,
                      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                      modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                      text = note.content,
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                      ),
                      modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                      text = dateStr,
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
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

        4 -> {
          val resolvedManager = downloadManager ?: remember(context) {
            com.example.data.download.DownloadManager.getInstance(context)
          }
          DownloadCenterScreen(
            downloadManager = resolvedManager,
            currentThemeMode = currentThemeMode,
            onSelectThemeMode = onSelectThemeMode,
            onPlayDownloadedAudio = onPlayDownloadedAudio
          )
        }
      }
    }

    // Add Note FAB
    if (selectedTab == 3) {
      FloatingActionButton(
        onClick = {
          initialNoteReference = ""
          showAddNoteDialog = true
        },
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(24.dp)
          .testTag("add_study_note_fab"),
        containerColor = EmeraldPrimary,
        contentColor = Color.White
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Note")
      }
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
      AddNoteDialog(
        initialReference = initialNoteReference,
        onDismiss = { showAddNoteDialog = false },
        onSave = { ref, title, content ->
          onSaveNote(ref, title, content)
          showAddNoteDialog = false
        }
      )
    }
  }
}

/**
 * Full-width Book Card used across both History of Islam and All Islamic Books.
 * Eliminates the side-by-side unweighted right column that previously squeezed English titles
 * into a narrow 1–2 letter wide strip on ~360dp phone screens.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FullWidthIslamicBookCard(
  serialNumber: Int,
  book: IslamicClassicalBook,
  isShiaBook: Boolean,
  matchedChapterIdx: Int,
  activeSearchQuery: String,
  showEntryTimeOnCards: Boolean,
  onOpenBook: (Int) -> Unit,
  cardTestTag: String
) {
  val context = LocalContext.current
  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .clickable {
        onOpenBook(if (matchedChapterIdx >= 0) matchedChapterIdx else 0)
      }
      .testTag(cardTestTag),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      // 1. Serial Number Badge + Full-Width English Title & Author
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (isShiaBook) GoldPrimary else EmeraldPrimary),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "$serialNumber",
            style = MaterialTheme.typography.labelLarge.copy(
              color = if (isShiaBook) Color.Black else Color.White,
              fontWeight = FontWeight.Bold
            )
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = buildHighlightedAnnotatedText(book.titleEnglish, activeSearchQuery),
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Text(
            text = "Author: ${book.author} (${book.eraHijri})",
            style = MaterialTheme.typography.labelMedium.copy(
              color = MaterialTheme.colorScheme.secondary,
              fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      // 2. Full-Width Arabic & Urdu Titles (Right-aligned RTL with 100% card width so neither clips nor squeezes)
      if (book.titleArabic.isNotBlank() || book.titleUrdu.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.End
          ) {
            if (book.titleArabic.isNotBlank()) {
              Text(
                text = book.titleArabic,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontFamily = ScheherazadeFontFamily,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = GoldPrimary,
                  textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }
            if (book.titleUrdu.isNotBlank()) {
              Text(
                text = book.titleUrdu,
                style = MaterialTheme.typography.bodySmall.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }
      }

      // 3. Wrapped Metadata Chips (Tradition, Category, Languages, Edition)
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = if (isShiaBook) GoldPrimary.copy(alpha = 0.16f) else EmeraldPrimary.copy(alpha = 0.12f)
        ) {
          Text(
            text = "${book.tradition} • ${book.category}",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = if (isShiaBook) GoldPrimary else EmeraldPrimary
            )
          )
        }
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = EmeraldPrimary.copy(alpha = 0.10f)
        ) {
          Text(
            text = "Languages: Arabic • English • Urdu",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = EmeraldPrimary
            )
          )
        }
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = GoldPrimary.copy(alpha = 0.14f)
        ) {
          Text(
            text = if (isShiaBook) {
              book.numberingSystem.replace("Darussalam", "Classical Reference", ignoreCase = true)
            } else {
              book.numberingSystem
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = GoldPrimary
            )
          )
        }
      }

      if (showEntryTimeOnCards) {
        Text(
          text = "Entry Time: ${book.entryTimestamp} • Methodology: ${book.gradingMethodology}",
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("book_entry_time_badge_${book.number}")
        )
      }

      // 4. Full-Width Book Summary
      Text(
        text = buildHighlightedAnnotatedText(book.summary, activeSearchQuery),
        style = MaterialTheme.typography.bodySmall.copy(
          fontSize = 13.sp,
          lineHeight = 19.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
      )

      // 5. Highlighted Chapter Match Banner if search matched inside this book
      if (matchedChapterIdx >= 0 && activeSearchQuery.isNotBlank()) {
        val matchedCh = book.chapters[matchedChapterIdx]
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = EmeraldPrimary.copy(alpha = 0.12f),
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              onOpenBook(matchedChapterIdx)
            }
        ) {
          Text(
            text = "Matched in Book: Ch. #${matchedCh.chapterNumber} — ${matchedCh.titleEnglish}",
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary
            )
          )
        }
      }

      // 6. Footer Row: Jilds/Chapters count + Read Book Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "${book.chapters.size} Jilds / Historical Sections (Full Text)",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = EmeraldPrimary
            )
          )
          Text(
            text = "Source: ${book.officialSourceUrl}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.primary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable {
              if (com.example.data.remote.SecurityIntegrityGuard.isExternalIntentSafe(book.officialSourceUrl)) {
                runCatching {
                  context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(book.officialSourceUrl)))
                }
              }
            }
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
          onClick = {
            onOpenBook(if (matchedChapterIdx >= 0) matchedChapterIdx else 0)
          },
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          modifier = Modifier.testTag("open_book_btn_${book.number}")
        ) {
          Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Read Book", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun IslamicBookReaderView(
  book: IslamicClassicalBook,
  selectedChapterIndex: Int,
  initialHighlightQuery: String = "",
  onSelectChapter: (Int) -> Unit,
  onJumpToBookAndChapter: (IslamicClassicalBook, Int) -> Unit = { _, _ -> },
  onClose: () -> Unit,
  onNavigateBook: (Int) -> Unit,
  onAddBookNote: (String) -> Unit
) {
  val context = LocalContext.current
  val safeChapterIdx = selectedChapterIndex.coerceIn(0, (book.chapters.size - 1).coerceAtLeast(0))
  val currentChapter = book.chapters.getOrElse(safeChapterIdx) { book.chapters.first() }
  var inBookSearchQuery by remember(book.id) { mutableStateOf("") }
  var searchAllBooksScope by remember { mutableStateOf(false) }
  var showAllJildsFullText by remember(book.id) { mutableStateOf(selectedChapterIndex == 0 && initialHighlightQuery.isBlank()) }
  val effectiveHighlightQuery = if (inBookSearchQuery.isNotBlank()) inBookSearchQuery else initialHighlightQuery

  val inBookSearchResults = remember(inBookSearchQuery, searchAllBooksScope, book) {
    val q = inBookSearchQuery.trim()
    if (q.isBlank()) emptyList()
    else {
      val targetBooks = if (searchAllBooksScope) IslamicBooksCatalog.allSunniAndShiaBooks else listOf(book)
      IslamicBooksCatalog.searchInsideBooks(
        booksToSearch = targetBooks,
        rawQuery = q
      )
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    // Top Book Reader Header
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 3.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          IconButton(onClick = onClose, modifier = Modifier.testTag("close_book_reader_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Books")
          }
          Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "${book.number}. ${book.titleEnglish}",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "${book.author} • ${book.titleArabic}",
              style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
          IconButton(
            onClick = { onAddBookNote("${book.titleEnglish} - Ch. ${currentChapter.chapterNumber}") }
          ) {
            Icon(Icons.Default.NoteAdd, contentDescription = "Add Note", tint = EmeraldPrimary)
          }
        }

        // Previous Book / Next Book Navigation Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = { onNavigateBook(-1) },
            enabled = book.number > 1,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("prev_book_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null, modifier = Modifier.size(16.dp))
            Text("Prev Book", fontSize = 11.sp)
          }

          Text(
            text = "Book ${book.number}/${IslamicBooksCatalog.allSunniAndShiaBooks.size} • ${book.tradition} • ${book.chapters.size} Jilds",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
          )

          OutlinedButton(
            onClick = { onNavigateBook(1) },
            enabled = book.number < IslamicBooksCatalog.allSunniAndShiaBooks.size,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("next_book_button")
          ) {
            Text("Next Book", fontSize = 11.sp)
            Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null, modifier = Modifier.size(16.dp))
          }
        }

        // In-Book Search Bar & Scope Toggle
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = inBookSearchQuery,
            onValueChange = { inBookSearchQuery = it },
            placeholder = {
              Text(
                text = if (searchAllBooksScope) "Search all ${IslamicBooksCatalog.allSunniAndShiaBooks.size} books..." else "Search inside this book (English, اردو, العربية)...",
                fontSize = 12.sp
              )
            },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
              if (inBookSearchQuery.isNotEmpty()) {
                IconButton(onClick = { inBookSearchQuery = "" }) {
                  Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("book_reader_search_input")
          )
          FilterChip(
            selected = searchAllBooksScope,
            onClick = { searchAllBooksScope = !searchAllBooksScope },
            label = {
              Text(
                text = if (searchAllBooksScope) "All Books" else "This Book",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = GoldPrimary,
              selectedLabelColor = Color(0xFF101915)
            ),
            modifier = Modifier.testTag("book_reader_search_scope_chip")
          )
        }

        // Jild / Volume Selector Row (All Jilds Full Book Text + Individual Jilds 1..N)
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          item {
            FilterChip(
              selected = showAllJildsFullText,
              onClick = { showAllJildsFullText = true },
              label = {
                Text(
                  text = "All Jilds — Full Text (${book.chapters.size})",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.height(30.dp).testTag("book_reader_all_jilds_chip")
            )
          }
          items(book.chapters.size) { idx ->
            val ch = book.chapters[idx]
            FilterChip(
              selected = !showAllJildsFullText && safeChapterIdx == idx,
              onClick = {
                showAllJildsFullText = false
                onSelectChapter(idx)
              },
              label = {
                Text(
                  text = "Jild ${ch.jildNumber} (Ch. ${ch.chapterNumber})",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GoldPrimary,
                selectedLabelColor = Color.Black
              ),
              modifier = Modifier.height(30.dp).testTag("book_reader_jild_chip_${idx + 1}")
            )
          }
        }
      }
    }

    // In-Book Search Results Overlay (if user is searching)
    if (inBookSearchQuery.isNotBlank()) {
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 4.dp)
      ) {
        Column(modifier = Modifier.padding(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${inBookSearchResults.size} matching passages for '$inBookSearchQuery':",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary),
              modifier = Modifier.weight(1f)
            )
            TextButton(
              onClick = { inBookSearchQuery = "" },
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
            ) {
              Text("Close Search", fontSize = 10.sp)
            }
          }
          if (inBookSearchResults.isEmpty()) {
            Text(
              text = "No matching passages found in ${if (searchAllBooksScope) "all classical books" else book.titleEnglish}. Try different keywords or switch to 'All Books'.",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
              modifier = Modifier.padding(vertical = 4.dp)
            )
          } else {
            LazyColumn(
              modifier = Modifier.height(180.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              itemsIndexed(inBookSearchResults) { idx, match ->
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.surface,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      showAllJildsFullText = false
                      onJumpToBookAndChapter(match.book, match.chapterIndex)
                      inBookSearchQuery = ""
                    }
                    .testTag("book_search_match_${match.book.number}_${match.chapterNumber}_$idx")
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(8.dp)
                  ) {
                    Text(
                      text = "${match.book.titleEnglish} • Jild ${match.chapterNumber}: ${match.chapterTitle}",
                      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                      modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                      text = buildHighlightedAnnotatedText(match.matchedLine, inBookSearchQuery),
                      style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                      maxLines = 2,
                      modifier = Modifier.fillMaxWidth()
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Chapter Tabs (guarded with key and safe indicator bounds check)
    if (book.chapters.isNotEmpty()) {
      androidx.compose.runtime.key(book.id, book.chapters.size) {
        ScrollableTabRow(
          selectedTabIndex = safeChapterIdx,
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          contentColor = EmeraldPrimary,
          edgePadding = 12.dp,
          indicator = { tabPositions ->
            if (safeChapterIdx in tabPositions.indices) {
              androidx.compose.material3.TabRowDefaults.SecondaryIndicator(
                modifier = with(androidx.compose.material3.TabRowDefaults) {
                  Modifier.tabIndicatorOffset(tabPositions[safeChapterIdx])
                },
                color = EmeraldPrimary
              )
            }
          }
        ) {
          book.chapters.forEachIndexed { idx, chapter ->
            Tab(
              selected = !showAllJildsFullText && safeChapterIdx == idx,
              onClick = {
                showAllJildsFullText = false
                onSelectChapter(idx)
              },
              text = {
                Text(
                  text = "Ch. ${chapter.chapterNumber}: ${chapter.titleEnglish}",
                  fontWeight = if (!showAllJildsFullText && safeChapterIdx == idx) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 12.sp,
                  maxLines = 1
                )
              }
            )
          }
        }
      }
    }

    // Chapter Content Body (Displays either All Jilds continuously or the selected Jild/Chapter)
    val chaptersToRender = remember(book, showAllJildsFullText, currentChapter) {
      if (showAllJildsFullText) book.chapters else listOf(currentChapter)
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      chaptersToRender.forEach { renderedChapter ->
        ElevatedCard(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "${renderedChapter.jildTitle} • Chapter ${renderedChapter.chapterNumber}",
                  style = MaterialTheme.typography.labelMedium.copy(
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
                Text(
                  text = buildHighlightedAnnotatedText(renderedChapter.titleEnglish, effectiveHighlightQuery),
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
              }
              IconButton(
                onClick = {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  val text = buildString {
                    append("${book.titleEnglish} — ${renderedChapter.titleEnglish}\n\n")
                    append("${renderedChapter.arabicExcerpt}\n\n")
                    append("${renderedChapter.englishContent}\n\n")
                    append("${renderedChapter.urduContent}\n\n")
                    append("Refs: ${renderedChapter.crossReferences}")
                  }
                  clipboard.setPrimaryClip(ClipData.newPlainText("Islamic Book Chapter", text))
                  Toast.makeText(context, "Chapter copied to clipboard", Toast.LENGTH_SHORT).show()
                }
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Chapter")
              }
            }

            if (renderedChapter.titleArabic.isNotBlank()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = buildHighlightedAnnotatedText(renderedChapter.titleArabic, effectiveHighlightQuery),
                style = MaterialTheme.typography.titleMedium.copy(
                  fontFamily = ScheherazadeFontFamily,
                  fontSize = 20.sp,
                  color = GoldPrimary,
                  fontWeight = FontWeight.Bold,
                  textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Arabic Primary Text in Amiri Classical Naskh Font
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = buildHighlightedAnnotatedText(renderedChapter.arabicExcerpt, effectiveHighlightQuery),
                style = MaterialTheme.typography.headlineSmall.copy(
                  fontFamily = AmiriFontFamily,
                  fontSize = 22.sp,
                  lineHeight = 38.sp,
                  textAlign = TextAlign.Right,
                  color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp)
              )
            }

            // Transliteration
            if (renderedChapter.transliteration.isNotBlank()) {
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "Transliteration:",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = GoldPrimary
                ),
                modifier = Modifier.fillMaxWidth()
              )
              Text(
                text = buildHighlightedAnnotatedText(renderedChapter.transliteration, effectiveHighlightQuery),
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontSize = 13.sp,
                  fontStyle = FontStyle.Italic,
                  color = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // English Translation & Exposition
            Text(
              text = "English Translation & Exposition",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              ),
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = buildHighlightedAnnotatedText(
                IslamicBooksCatalog.formatBookEnglishContent(renderedChapter.englishContent),
                effectiveHighlightQuery
              ),
              style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                lineHeight = 23.sp
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Urdu Translation & Exposition
            Text(
              text = "اردو ترجمہ و تشریح",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = GoldPrimary,
                textAlign = TextAlign.Right
              ),
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = buildHighlightedAnnotatedText(
                IslamicBooksCatalog.formatBookUrduContent(renderedChapter.urduContent),
                effectiveHighlightQuery
              ),
              style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 15.sp,
                lineHeight = 25.sp,
                textAlign = TextAlign.Right,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Scholarly Notes & Verified Attribution Links
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = EmeraldPrimary.copy(alpha = 0.08f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp)
              ) {
                Text(
                  text = "Scholarly Commentary & Grading Methodology (${book.tradition}):",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = book.gradingMethodology,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.secondary
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = buildHighlightedAnnotatedText(renderedChapter.scholarlyNotes, effectiveHighlightQuery),
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Cross-References (Quran & Hadith): ${renderedChapter.crossReferences}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Entry Time: ${book.entryTimestamp} • ${book.sourceTypeLabel}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(book.officialSourceUrl)))
                      }
                    }
                ) {
                  Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Official Source: ${book.officialSourceUrl}",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                  )
                }
              }
            }
          }
        }
      }

      // Previous Chapter / Next Chapter Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedButton(
          onClick = {
            showAllJildsFullText = false
            if (safeChapterIdx > 0) {
              onSelectChapter(safeChapterIdx - 1)
            }
          },
          enabled = safeChapterIdx > 0,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("prev_chapter_button")
        ) {
          Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Previous Jild / Ch.", fontSize = 12.sp)
        }

        OutlinedButton(
          onClick = {
            showAllJildsFullText = false
            if (safeChapterIdx < book.chapters.lastIndex) {
              onSelectChapter(safeChapterIdx + 1)
            }
          },
          enabled = safeChapterIdx < book.chapters.lastIndex,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("next_chapter_button")
        ) {
          Text("Next Jild / Ch.", fontSize = 12.sp)
          Spacer(modifier = Modifier.width(4.dp))
          Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null, modifier = Modifier.size(18.dp))
        }
      }
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
fun AddNoteDialog(
  initialReference: String,
  onDismiss: () -> Unit,
  onSave: (String, String, String) -> Unit
) {
  var reference by remember { mutableStateOf(initialReference.ifEmpty { "Quran / Hadith Ref" }) }
  var title by remember { mutableStateOf("") }
  var content by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Study Note", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = reference,
          onValueChange = { reference = it },
          label = { Text("Scripture / Book Reference") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Note Title") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = content,
          onValueChange = { content = it },
          label = { Text("Reflection / Notes") },
          minLines = 4,
          maxLines = 8,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank() && content.isNotBlank()) {
            onSave(reference, title, content)
          }
        }
      ) {
        Text("Save Note")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun EmptyStateView(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(64.dp)
        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(32.dp),
        tint = MaterialTheme.colorScheme.primary
      )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodyMedium.copy(
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    )
  }
}
