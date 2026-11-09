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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.material.icons.filled.Flag
import com.example.data.local.ExtendedHadithDataProvider
import com.example.data.local.entities.BookmarkEntity
import com.example.data.local.entities.NoteEntity
import com.example.data.model.IslamicBooksCatalog
import com.example.data.model.IslamicClassicalBook
import com.example.ui.components.ClassifiedFolderLibraryExplorerView
import com.example.ui.components.IslamQaScholarlyArchiveView
import com.example.ui.components.IslamicResearchHubView
import com.example.ui.components.ProphetStoriesHubView
import com.example.ui.components.SahabaAhlAlBaytBiographiesView
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
  initialSelectedTab: Int = 0,
  selectionNonce: Long = 0L,
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
  // 5 = Video Lectures (EMAM)
  var selectedTab by remember(initialSelectedTab, selectionNonce) { mutableIntStateOf(initialSelectedTab) }
  LaunchedEffect(initialSelectedTab, selectionNonce) {
    selectedTab = initialSelectedTab
  }
  var showAddNoteDialog by remember { mutableStateOf(false) }
  var initialNoteReference by remember { mutableStateOf("") }

  // History of Islam state (in Library)
  var historyTraditionFilter by remember { mutableStateOf("BOTH") } // "BOTH", "SUNNI", "SHIA"
  var historySearchQuery by remember { mutableStateOf("") }
  var selectedHistoryThematicSection by remember { mutableStateOf("All Historical Books") }
  var offlineDownloadRefreshTick by remember { mutableIntStateOf(0) }
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
    val jildNum = book.chapters.getOrNull(safeIdx)?.jildNumber ?: 1
    com.example.data.repository.IslamicDigitalLibraryRepository.saveReadingProgress(
      context = context,
      bookId = book.id,
      chapterIndex = safeIdx,
      volumeNumber = jildNum
    )
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
        val jildNum = book.chapters.getOrNull(idx)?.jildNumber ?: 1
        com.example.data.repository.IslamicDigitalLibraryRepository.saveReadingProgress(
          context = context,
          bookId = book.id,
          chapterIndex = idx,
          volumeNumber = jildNum
        )
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
        val allBooks = com.example.data.repository.ClassicalLibraryAndBiographiesRepository.getAllUniversalLibraryBooks(context)
        val idx = allBooks.indexOfFirst { it.id == book.id }.let { found ->
          if (found >= 0) found else allBooks.indexOfFirst { it.number == book.number }
        }
        val nextIdx = idx + delta
        if (idx >= 0 && nextIdx in allBooks.indices) {
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

  // Complete 26 Historical Books & Thematic Filtering
  val all26HistoryBooks = remember(context) {
    com.example.data.repository.IslamicDigitalLibraryRepository.getCompleteHistoricalBooks26(context)
  }

  val baseHistoryBooks = remember(historyTraditionFilter, selectedHistoryThematicSection, offlineDownloadRefreshTick, context) {
    com.example.data.repository.IslamicDigitalLibraryRepository.filterHistoricalBooks(
      context = context,
      traditionFilter = historyTraditionFilter,
      thematicSection = selectedHistoryThematicSection
    )
  }

  val historyBooksWithSerial = remember(baseHistoryBooks) {
    baseHistoryBooks.mapIndexed { idx, book -> book.catalogBookNumber.takeIf { it > 0 }?.let { it to book } ?: ((idx + 1) to book) }
  }

  val hadithAuditReports = remember(context) {
    com.example.data.repository.IslamicDigitalLibraryRepository.auditRequested15HadithCollections(context)
  }

  val filteredHistoryBooksWithSerial = remember(historyBooksWithSerial, historySearchQuery) {
    val q = historySearchQuery.trim()
    if (q.isBlank()) {
      historyBooksWithSerial
    } else {
      val asNumber = q.removePrefix("#").removePrefix("book").trim().toIntOrNull()
      val matchingInsideBookIds = IslamicBooksCatalog.searchInsideBooks(
        booksToSearch = baseHistoryBooks,
        rawQuery = q
      ).map { it.book.id }.toSet()

      historyBooksWithSerial.filter { (serialNumber, book) ->
        (asNumber != null && (serialNumber == asNumber || book.number == asNumber || book.catalogBookNumber == asNumber)) ||
          book.id in matchingInsideBookIds ||
          book.titleEnglish.contains(q, ignoreCase = true) ||
          book.titleArabic.contains(q) ||
          book.titleUrdu.contains(q) ||
          book.author.contains(q, ignoreCase = true) ||
          book.authorArabic.contains(q) ||
          book.authorUrdu.contains(q) ||
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

  val safeSelectedTab = selectedTab.coerceIn(0, 11)

  val quickModules = remember(bookmarks.size, notes.size) {
    listOf(
      Triple(5, "▶ Video Lectures", "library_quick_chip_video_lectures"),
      Triple(1, "📚 All Books (${IslamicBooksCatalog.allSunniAndShiaBooks.size})", "library_quick_chip_all_books"),
      Triple(0, "🏛 History (${IslamicBooksCatalog.historyBooks.size})", "library_quick_chip_history"),
      Triple(6, "📖 Fikr-e-Maududi", "library_quick_chip_maududi"),
      Triple(8, "🌟 Prophet Stories (25)", "library_quick_chip_prophets"),
      Triple(10, "🕌 Sahaba & Ahl al-Bayt", "library_quick_chip_sahaba"),
      Triple(7, "🔬 Research (4)", "library_quick_chip_research"),
      Triple(11, "⚖️ IslamQA (4,203)", "library_quick_chip_islamqa"),
      Triple(9, "🗂 Folders", "library_quick_chip_folders"),
      Triple(2, "🔖 Bookmarks (${bookmarks.size})", "library_quick_chip_bookmarks"),
      Triple(3, "📝 Notes (${notes.size})", "library_quick_chip_notes"),
      Triple(4, "⬇️ Offline", "library_quick_chip_offline")
    )
  }

  var isFullScreenReadingMode by remember { mutableStateOf(false) }
  var isHeaderCollapsedByScroll by remember { mutableStateOf(false) }
  val isHeaderCompact = isFullScreenReadingMode || isHeaderCollapsedByScroll

  val libraryNestedScrollConnection = remember {
    object : NestedScrollConnection {
      override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (available.y < -18f && !isHeaderCollapsedByScroll) {
          isHeaderCollapsedByScroll = true
        } else if (available.y > 22f && isHeaderCollapsedByScroll && !isFullScreenReadingMode) {
          isHeaderCollapsedByScroll = false
        }
        return Offset.Zero
      }
    }
  }

  val activeSectionTitle = remember(safeSelectedTab, bookmarks.size, notes.size) {
    quickModules.firstOrNull { it.first == safeSelectedTab }?.second ?: "Scholarly Library"
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .nestedScroll(libraryNestedScrollConnection)
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Illuminated Scholarly Library Masthead:
      // Automatically collapses upward when scrolling down to read Research Papers or Books on full screen,
      // and includes a 1-tap Full Screen toggle button.
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("library_all_features_quick_grid"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = if (isHeaderCompact) 4.dp else 8.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(
              modifier = Modifier.weight(1f),
              horizontalAlignment = if (isHeaderCompact) Alignment.Start else Alignment.CenterHorizontally
            ) {
              if (!isHeaderCompact) {
                Text(
                  text = "خَزَانَةُ العِلْمِ • اسلامی تحقیقی و علمی کتب خانہ",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    textAlign = TextAlign.Center
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
              }
              Text(
                text = if (isHeaderCompact) {
                  "✦ $activeSectionTitle (Full-Screen Reading)"
                } else {
                  "Bayan al-Huda Scholarly Library & Research Archive"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 11.sp,
                  color = EmeraldPrimary,
                  textAlign = if (isHeaderCompact) TextAlign.Start else TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isHeaderCompact) EmeraldPrimary else EmeraldPrimary.copy(alpha = 0.12f),
              border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.7f)),
              modifier = Modifier
                .clickable {
                  if (isHeaderCompact) {
                    isFullScreenReadingMode = false
                    isHeaderCollapsedByScroll = false
                  } else {
                    isFullScreenReadingMode = true
                  }
                }
                .testTag("library_fullscreen_reader_toggle")
            ) {
              Text(
                text = if (isHeaderCompact) "⤡ Show Sections" else "⤢ Full Screen",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp,
                  color = if (isHeaderCompact) Color.White else EmeraldPrimary
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          if (!isHeaderCompact) {
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
              verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
              quickModules.forEach { (tabIdx, label, chipTag) ->
                val isSelected = safeSelectedTab == tabIdx
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                  border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) GoldPrimary else EmeraldPrimary.copy(alpha = 0.35f)
                  ),
                  modifier = Modifier
                    .clickable {
                      selectedTab = tabIdx
                      isHeaderCollapsedByScroll = false
                    }
                    .testTag(chipTag)
                ) {
                  Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      fontSize = 10.sp,
                      color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                      textAlign = TextAlign.Center
                    ),
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                  )
                }
              }
            }
          }
        }
      }

      // Symmetrical Top Section Bar (collapses smoothly in full-screen reading mode)
      if (!isHeaderCompact) {
        ScrollableTabRow(
          selectedTabIndex = safeSelectedTab,
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.primary,
          edgePadding = 8.dp,
          indicator = { tabPositions ->
            if (safeSelectedTab in tabPositions.indices) {
              androidx.compose.material3.TabRowDefaults.SecondaryIndicator(
                modifier = with(androidx.compose.material3.TabRowDefaults) {
                  Modifier.tabIndicatorOffset(tabPositions[safeSelectedTab])
                },
                color = EmeraldPrimary
              )
            }
          }
      ) {
        Tab(
          selected = safeSelectedTab == 0,
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
                tint = if (safeSelectedTab == 0) EmeraldPrimary else GoldPrimary
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
          selected = safeSelectedTab == 1,
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
          selected = safeSelectedTab == 2,
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
          selected = safeSelectedTab == 3,
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
          selected = safeSelectedTab == 4,
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
        Tab(
          selected = safeSelectedTab == 5,
          onClick = { selectedTab = 5 },
          modifier = Modifier.testTag("library_tab_video_lectures"),
          text = {
            Text(
              text = "Video Lectures",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              maxLines = 1
            )
          }
        )
        Tab(
          selected = safeSelectedTab == 6,
          onClick = { selectedTab = 6 },
          modifier = Modifier.testTag("library_tab_maududi_library"),
          text = {
            Text(
              text = "Fikr-e-Maududi",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              maxLines = 1
            )
          }
        )
        Tab(
          selected = safeSelectedTab == 7,
          onClick = { selectedTab = 7 },
          modifier = Modifier.testTag("library_tab_islamic_research"),
          text = {
            Text(
              text = "Islamic Research (4)",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              maxLines = 1
            )
          }
        )
        Tab(
          selected = safeSelectedTab == 8,
          onClick = { selectedTab = 8 },
          modifier = Modifier.testTag("library_tab_prophet_stories"),
          text = {
            Text(
              text = "Prophet Stories (25)",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              maxLines = 1
            )
          }
        )
        Tab(
          selected = safeSelectedTab == 9,
          onClick = { selectedTab = 9 },
          modifier = Modifier.testTag("library_tab_classified_folders"),
          text = {
            Text(
              text = "Folders & Subfolders",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              maxLines = 1
            )
          }
        )
        Tab(
          selected = safeSelectedTab == 10,
          onClick = { selectedTab = 10 },
          modifier = Modifier.testTag("library_tab_sahaba_ahl_al_bayt"),
          text = {
            Text(
              text = "Sahaba & Ahl al-Bayt",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              maxLines = 1
            )
          }
        )
        Tab(
          selected = safeSelectedTab == 11,
          onClick = { selectedTab = 11 },
          modifier = Modifier.testTag("library_tab_islamqa_archive"),
          text = {
            Text(
              text = "IslamQA (4,203)",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              maxLines = 1
            )
          }
        )
        }
      }

      when (safeSelectedTab) {
        0 -> {
          // =========================================================================
          // TAB 0: HISTORY OF ISLAM (Top-level in Library)
          // Symmetrical Illuminated Masthead + Full-Page Scrollable History Books
          // =========================================================================
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Top-level Symmetrical History Banner Card (Arabic, Urdu, and English on separate lines)
            item {
              ElevatedCard(
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("library_history_top_banner_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                  containerColor = EmeraldPrimary.copy(alpha = 0.10f)
                )
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Text(
                    text = "التَّارِيخُ الإِسْلَامِيُّ وَالسِّيرَةُ النَّبَوِيَّة",
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontFamily = ScheherazadeFontFamily,
                      fontSize = 20.sp,
                      fontWeight = FontWeight.Bold,
                      color = GoldPrimary,
                      textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                  )
                  Text(
                    text = "تاریخِ اسلام، سیرت النبی ﷺ اور مستند تاریخی کتب",
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontFamily = UrduNastaliqFontFamily,
                      fontSize = 15.sp,
                      lineHeight = 25.sp,
                      fontWeight = FontWeight.Bold,
                      color = EmeraldPrimary,
                      textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                  )
                  Text(
                    text = "History of Islam — ${all26HistoryBooks.size} Classical Historical Books & ${hadithAuditReports.size} Audited Hadith Collections",
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary,
                      textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                  )
                  Text(
                    text = "Complete Arabic, Urdu & English Editions • Scroll up for full-screen reading",
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                  )

                  // Resume Last Read History Book button if available
                  val lastBook = remember(lastReadHistoryBookId, all26HistoryBooks) {
                    all26HistoryBooks.find { it.id == lastReadHistoryBookId }
                      ?: IslamicBooksCatalog.historyBooks.find { it.id == lastReadHistoryBookId }
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

            // Islamic Research Quick Card inside History of Islam Tab
            item {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = GoldPrimary.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.50f)),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { selectedTab = 7 }
                  .testTag("library_history_open_islamic_research_card")
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "Islamic Research • تحقیقی مقالات و مسنون اذکار (4)",
                      style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                      )
                    )
                    Text(
                      text = "❶ رسول اللہ ﷺ کی آخری وصیّتیں (Paper #1) • ❷ قرآن کے 10 احکام و اولیاء کی 12 صفات (Paper #7) • ❸ رافضیت و ناصبیت (Paper #5a) • ❹ فرض نماز کے بعد سُنّت اَذکار (19 Azkar)",
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = UrduNastaliqFontFamily,
                        fontSize = 13.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Open →",
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = EmeraldPrimary
                    )
                  )
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
                      text = "All History (${all26HistoryBooks.size})",
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
                      text = "Sunni History (${all26HistoryBooks.count { it.tradition == "SUNNI" }})",
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
                      text = "Shia History (${all26HistoryBooks.count { it.tradition == "SHIA" }})",
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

            // Thematic Library Sub-Sections Filter Bar (Historical Books, Seerah, Sahaba, Ahl al-Bayt, Khulafa, Battles, Karbala, Sunni Sources, Hadith Collections, Downloaded Books)
            item {
              Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  text = "Library Sub-Sections (Browse by Theme, Personality, or Collection):",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
                FlowRow(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  IslamicBooksCatalog.libraryThematicSubSections.forEach { sectionLabel ->
                    val isSelected = selectedHistoryThematicSection.equals(sectionLabel, ignoreCase = true)
                    FilterChip(
                      selected = isSelected,
                      onClick = {
                        selectedHistoryThematicSection = if (isSelected && sectionLabel != "All Historical Books") {
                          "All Historical Books"
                        } else {
                          sectionLabel
                        }
                      },
                      label = {
                        Text(
                          text = sectionLabel,
                          fontSize = 11.sp,
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                      },
                      colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = Color(0xFF101915)
                      ),
                      modifier = Modifier
                        .height(30.dp)
                        .testTag("history_theme_chip_${sectionLabel.lowercase().replace(" ", "_")}")
                    )
                  }
                }
              }
            }

            // If user selected "Hadith Collections" sub-section, show the 15 Audited Hadith Collections Explorer Card
            if (selectedHistoryThematicSection.equals("Hadith Collections", ignoreCase = true)) {
              item {
                ElevatedCard(
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hadith_15_collections_audit_card"),
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                  )
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Text(
                      text = "Audited Hadith Collections (15 Canonical Sunni Compilations • Arabic + Urdu)",
                      style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                      )
                    )
                    Text(
                      text = "All 15 requested Hadith collections verified with Arabic text, Urdu translation, preserved Book/Chapter/Hadith numbering, and zero duplicate entries:",
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    )
                    hadithAuditReports.forEach { report ->
                      Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EmeraldPrimary.copy(alpha = 0.07f),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                      ) {
                        Column(
                          modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                          verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                          Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                          ) {
                            Text(
                              text = "${report.collectionNumber}. ${report.englishTitle}",
                              style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                              )
                            )
                            Text(
                              text = "${report.arabicTitle} • ${report.urduTitle}",
                              style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = UrduNastaliqFontFamily,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                              )
                            )
                          }
                          Text(
                            text = "Author: ${report.author} • Edition: ${report.sourceEdition}",
                            style = MaterialTheme.typography.bodySmall.copy(
                              fontSize = 11.sp,
                              color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                          )
                          Text(
                            text = "Arabic: ${if (report.arabicAvailable) "Available ✓" else "Missing"} • Urdu: ${if (report.urduAvailable) "Available ✓" else "Missing"} • Canonical: ${report.canonicalTotal} (Offline Core: ${report.localBundledCount}) • Duplicates: ${report.duplicateCount}",
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
          // Full-page vertical scrolling: History of Islam, Video Assets, Islamic Research,
          // search, filter chips, and pagination bar scroll up inside LazyColumn
          // giving 100% full screen height to view book names and cards.
          // =========================================================================
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            item(key = "library_books_header_shortcuts_and_filters") {
              Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                // Top-level History Quick Card inside All Books tab
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = EmeraldPrimary.copy(alpha = 0.12f),
                  border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
                  modifier = Modifier
                    .fillMaxWidth()
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

                // Video Assets to Understand Islam (Engineer Muhammad Ali Mirza Lectures Since 2013) Card
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = GoldPrimary.copy(alpha = 0.14f),
                  border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.45f)),
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedTab = 5 }
                    .testTag("library_books_open_video_assets_card")
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = "Video Assets · Understand Islam (Engr. Muhammad Ali Mirza Since 2013)",
                        style = MaterialTheme.typography.labelLarge.copy(
                          fontWeight = FontWeight.Bold,
                          color = EmeraldPrimary
                        )
                      )
                      Text(
                        text = "Smart Watch-Time Topic Suggestions: Imam Hussain A.S. → Karbala & Reality of Yazeed • Tafseer 1–344 • Salah & Azan",
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontSize = 10.sp,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      )
                    }
                    Text(
                      text = "Watch →",
                      style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                      )
                    )
                  }
                }

                // Islamic Research Quick Card inside All Books Tab
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = EmeraldPrimary.copy(alpha = 0.10f),
                  border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedTab = 7 }
                    .testTag("library_books_open_islamic_research_card")
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = "Islamic Research • رافضیت ، ناصبیت اور یزیدیت کا تحقیقی جائزہ (5a) + فرض نماز کے بعد سُنّت اَذکار",
                        style = MaterialTheme.typography.labelLarge.copy(
                          fontFamily = UrduNastaliqFontFamily,
                          fontWeight = FontWeight.Bold,
                          color = EmeraldPrimary
                        )
                      )
                      Text(
                        text = "Research Paper 5a (12 Sections) & 19 Sahih Post-Prayer Adhkar with International Hadith #",
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontSize = 10.sp,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      )
                    }
                    Text(
                      text = "Read →",
                      style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                      )
                    )
                  }
                }

                // Sunni / Shia / Both Tradition Filter + Entry Time + Retry Bar
                LazyRow(
                  contentPadding = PaddingValues(vertical = 2.dp),
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
                  contentPadding = PaddingValues(vertical = 2.dp),
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
              }
            }

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

            itemsIndexed(paginatedBooks, key = { _, it -> it.id }) { idxInPage, book ->
              val isShiaBook = book.tradition.equals("SHIA", ignoreCase = true)
              val matchedChapterIdx = remember(book, bookSearchQuery) {
                findMatchingChapterIndex(book, bookSearchQuery)
              }
              val orderedSerialNumber = (safePageIndex * itemsPerPage) + idxInPage + 1
              FullWidthIslamicBookCard(
                serialNumber = orderedSerialNumber,
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

        2 -> {
          // Bookmarks Tab (Scrollable & Symmetrical Illuminated Layout)
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            item(key = "bookmarks_decorated_header_card") {
              ElevatedCard(
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("bookmarks_decorated_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                  containerColor = EmeraldPrimary.copy(alpha = 0.10f)
                )
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Text(
                    text = "المَحْفُوظَاتُ وَالعَلَامَاتُ المَرْجِعِيَّة",
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontFamily = ScheherazadeFontFamily,
                      fontSize = 19.sp,
                      fontWeight = FontWeight.Bold,
                      color = GoldPrimary,
                      textAlign = TextAlign.Center
                    )
                  )
                  Text(
                    text = "محفوظ آیات، احادیث اور تاریخی ابواب (${bookmarks.size})",
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontFamily = UrduNastaliqFontFamily,
                      fontSize = 14.sp,
                      lineHeight = 24.sp,
                      fontWeight = FontWeight.Bold,
                      color = EmeraldPrimary,
                      textAlign = TextAlign.Center
                    )
                  )
                  Text(
                    text = "Saved Bookmarks (${bookmarks.size}) • Quran, Hadith & Classical Library",
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary,
                      textAlign = TextAlign.Center
                    )
                  )
                }
              }
            }
            if (bookmarks.isEmpty()) {
              item(key = "bookmarks_empty_state") {
                EmptyStateView(
                  icon = Icons.Default.Bookmark,
                  title = "No Bookmarks Yet",
                  subtitle = "Save inspiring verses, authentic hadiths, and historical chapters while reading to easily revisit them here."
                )
              }
            } else {
              items(bookmarks, key = { it.itemKey }) { bookmark ->
                Card(
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bookmark_item_${bookmark.itemKey}"),
                  shape = RoundedCornerShape(14.dp),
                  border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.45f)),
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
                      Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                      ) {
                        Text(
                          text = bookmark.arabicSnippet,
                          style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = AmiriFontFamily,
                            fontSize = 19.sp,
                            lineHeight = 32.sp,
                            textAlign = TextAlign.Right
                          ),
                          modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                        )
                      }
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
          // Study Notes Tab (Scrollable & Symmetrical Illuminated Layout)
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 12.dp, start = 12.dp, end = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            item(key = "notes_decorated_header_card") {
              ElevatedCard(
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("notes_decorated_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                  containerColor = EmeraldPrimary.copy(alpha = 0.10f)
                )
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Text(
                    text = "المُذَكِّرَاتُ العِلْمِيَّةُ وَالتَّدَبُّر",
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontFamily = ScheherazadeFontFamily,
                      fontSize = 19.sp,
                      fontWeight = FontWeight.Bold,
                      color = GoldPrimary,
                      textAlign = TextAlign.Center
                    )
                  )
                  Text(
                    text = "تفسیر، حدیث اور تحقیقی مطالعہ کے ذاتی نوٹس (${notes.size})",
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontFamily = UrduNastaliqFontFamily,
                      fontSize = 14.sp,
                      lineHeight = 24.sp,
                      fontWeight = FontWeight.Bold,
                      color = EmeraldPrimary,
                      textAlign = TextAlign.Center
                    )
                  )
                  Text(
                    text = "Personal Study Notes & Reflections (${notes.size})",
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary,
                      textAlign = TextAlign.Center
                    )
                  )
                }
              }
            }
            if (notes.isEmpty()) {
              item(key = "notes_empty_state") {
                EmptyStateView(
                  icon = Icons.Default.Note,
                  title = "No Personal Notes",
                  subtitle = "Record personal reflections, tafsir notes, and lessons from verses, hadiths, and historical works."
                )
              }
            } else {
              items(notes, key = { it.id }) { note ->
                val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(note.updatedAt))

                Card(
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_item_${note.id}"),
                  shape = RoundedCornerShape(14.dp),
                  border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.45f)),
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

        5 -> {
          com.example.ui.discover.DiscoverScreen()
        }

        6 -> {
          com.example.ui.discover.MaududiLibraryScreen()
        }

        7 -> {
          IslamicResearchHubView(
            onAddStudyNote = { ref ->
              initialNoteReference = ref
              showAddNoteDialog = true
            }
          )
        }

        8 -> {
          ProphetStoriesHubView(
            onAddStudyNote = { ref ->
              initialNoteReference = ref
              showAddNoteDialog = true
            },
            onOpenClassicalBookById = { bookId ->
              val targetBook = all26HistoryBooks.firstOrNull { it.id == bookId }
                ?: IslamicBooksCatalog.allExpandedLibraryBooks.firstOrNull { it.id == bookId }
              if (targetBook != null) {
                openBookAtChapter(targetBook, 0, "")
              }
            }
          )
        }

        9 -> {
          ClassifiedFolderLibraryExplorerView(
            onOpenClassicalBook = { book ->
              openBookAtChapter(book, 0, "")
            },
            onOpenProphetStories = {
              selectedTab = 8
            },
            onOpenResearchPapers = {
              selectedTab = 7
            }
          )
        }

        10 -> {
          SahabaAhlAlBaytBiographiesView(
            onOpenClassicalBook = { book ->
              openBookAtChapter(book, 0, "")
            }
          )
        }

        11 -> {
          IslamQaScholarlyArchiveView()
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
      // 1. Symmetrical Illuminated Header: Book # Badge + Tradition Pill + Separate Arabic, Urdu, and English Title Lines
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isShiaBook) GoldPrimary.copy(alpha = 0.10f) else EmeraldPrimary.copy(alpha = 0.08f),
        border = BorderStroke(
          1.dp,
          if (isShiaBook) GoldPrimary.copy(alpha = 0.55f) else EmeraldPrimary.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
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
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isShiaBook) GoldPrimary.copy(alpha = 0.22f) else EmeraldPrimary.copy(alpha = 0.16f)
            ) {
              Text(
                text = "${book.tradition} • ${book.category}",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (isShiaBook) GoldPrimary else EmeraldPrimary
                )
              )
            }
          }

          if (book.titleArabic.isNotBlank()) {
            Text(
              text = book.titleArabic,
              style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = ScheherazadeFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary,
                textAlign = TextAlign.Center
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }
          if (book.titleUrdu.isNotBlank()) {
            Text(
              text = book.titleUrdu,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }
          Text(
            text = buildHighlightedAnnotatedText(book.titleEnglish, activeSearchQuery),
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Text(
            text = "Author: ${book.author} (${book.eraHijri})",
            style = MaterialTheme.typography.labelMedium.copy(
              color = MaterialTheme.colorScheme.secondary,
              fontWeight = FontWeight.SemiBold,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
          )
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
        val isDisputedAttribution = book.attributionConfidence.contains("DISPUTED", ignoreCase = true)
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isDisputedAttribution) Color(0xFFFFF3E0) else EmeraldPrimary.copy(alpha = 0.08f),
          border = BorderStroke(
            1.dp,
            if (isDisputedAttribution) Color(0xFFE65100) else EmeraldPrimary.copy(alpha = 0.25f)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("book_entry_time_badge_${book.number}")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            Text(
              text = "Attribution Confidence: ${book.attributionConfidence}",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDisputedAttribution) Color(0xFFE65100) else EmeraldPrimary
              ),
              modifier = Modifier.fillMaxWidth()
            )
            Text(
              text = "Translator: ${book.translatorName} • Source: ${book.sourceTypeLabel}",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
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

      // 6. Footer Row: Jilds/Chapters count + Offline Download + Read Book Button
      var isBookOffline by remember(book.id) {
        mutableStateOf(
          com.example.data.repository.IslamicDigitalLibraryRepository.isBookDownloadedOffline(context, book.id)
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "${book.volumesCount} Volumes (${book.chapters.size} Sections • Full Arabic & Urdu)",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = EmeraldPrimary
            )
          )
          Text(
            text = "Edition: ${book.sourceTypeLabel} (Tap to Read In-App)",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.primary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable {
              onOpenBook(if (matchedChapterIdx >= 0) matchedChapterIdx else 0)
            }
          )
        }

        Spacer(modifier = Modifier.width(6.dp))

        OutlinedButton(
          onClick = {
            if (!isBookOffline) {
              com.example.data.repository.IslamicDigitalLibraryRepository.downloadBookOffline(context, book)
              isBookOffline = true
              Toast.makeText(context, "${book.titleEnglish} saved for offline reading", Toast.LENGTH_SHORT).show()
            } else {
              Toast.makeText(context, "${book.titleEnglish} is ready offline", Toast.LENGTH_SHORT).show()
            }
          },
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.testTag("download_book_btn_${book.number}")
        ) {
          Icon(
            if (isBookOffline) Icons.Default.CheckCircle else Icons.Default.Add,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isBookOffline) "Offline ✓" else "Offline",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = EmeraldPrimary
          )
        }

        Spacer(modifier = Modifier.width(6.dp))

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

@OptIn(ExperimentalLayoutApi::class)
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
  var selectedJildFilter by remember(book.id) { mutableIntStateOf(0) }
  val distinctJildNumbers = remember(book) {
    book.chapters.map { it.jildNumber }.filter { it > 0 }.distinct().sorted().ifEmpty { listOf(1) }
  }
  val allUniversalBooks = remember(book.id) {
    com.example.data.repository.ClassicalLibraryAndBiographiesRepository.getAllUniversalLibraryBooks(context)
  }
  val currentBookUniversalIdx = remember(allUniversalBooks, book.id, book.number) {
    allUniversalBooks.indexOfFirst { it.id == book.id }.let { found ->
      if (found >= 0) found else allUniversalBooks.indexOfFirst { it.number == book.number }
    }
  }
  var readerLanguageMode by remember {
    mutableStateOf(com.example.data.repository.IslamicDigitalLibraryRepository.BookReaderLanguageMode.FULL_TRILINGUAL)
  }
  var showBookMetadataSheet by remember(book.id) {
    mutableStateOf(book.attributionConfidence.contains("DISPUTED", ignoreCase = true))
  }
  var fontScaleDelta by remember { mutableIntStateOf(0) }
  var isBookDownloaded by remember(book.id) {
    mutableStateOf(com.example.data.repository.IslamicDigitalLibraryRepository.isBookDownloadedOffline(context, book.id))
  }
  var bookmarkedChaptersSet by remember(book.id) {
    mutableStateOf(com.example.data.repository.IslamicDigitalLibraryRepository.getBookmarkedChapters(context))
  }
  var reportTargetPassageLabel by remember(book.id) { mutableStateOf<String?>(null) }
  var reportReasonInput by remember(book.id) { mutableStateOf("") }
  val effectiveHighlightQuery = if (inBookSearchQuery.isNotBlank()) inBookSearchQuery else initialHighlightQuery

  val inBookSearchResults = remember(inBookSearchQuery, searchAllBooksScope, book, allUniversalBooks) {
    val q = inBookSearchQuery.trim()
    if (q.isBlank()) emptyList()
    else {
      val targetBooks = if (searchAllBooksScope) allUniversalBooks else listOf(book)
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
      Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
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
          Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(
              onClick = { fontScaleDelta = (fontScaleDelta - 2).coerceAtLeast(-4) },
              contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
              modifier = Modifier.size(width = 32.dp, height = 30.dp).testTag("book_reader_font_decrease")
            ) {
              Text("A-", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(
              onClick = { fontScaleDelta = (fontScaleDelta + 2).coerceAtMost(8) },
              contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
              modifier = Modifier.size(width = 32.dp, height = 30.dp).testTag("book_reader_font_increase")
            ) {
              Text("A+", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(
              onClick = { showBookMetadataSheet = !showBookMetadataSheet },
              modifier = Modifier.size(34.dp).testTag("book_reader_info_toggle_button")
            ) {
              Icon(Icons.Default.Info, contentDescription = "Book Info & Attribution", tint = GoldPrimary)
            }
            IconButton(
              onClick = { onAddBookNote("${book.titleEnglish} - Ch. ${currentChapter.chapterNumber}") },
              modifier = Modifier.size(34.dp)
            ) {
              Icon(Icons.Default.NoteAdd, contentDescription = "Add Note", tint = EmeraldPrimary)
            }
          }
        }

        // Previous Book / Next Book Navigation Bar (Universal across all Library Books)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = { onNavigateBook(-1) },
            enabled = currentBookUniversalIdx > 0 || book.number > 1,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("prev_book_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null, modifier = Modifier.size(16.dp))
            Text("Prev Book", fontSize = 11.sp)
          }

          Text(
            text = "Book ${book.number} • ${book.tradition} • ${distinctJildNumbers.size} Jilds (${book.chapters.size} Pages)",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
          )

          OutlinedButton(
            onClick = { onNavigateBook(1) },
            enabled = currentBookUniversalIdx < 0 || currentBookUniversalIdx < allUniversalBooks.lastIndex,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("next_book_button")
          ) {
            Text("Next Book", fontSize = 11.sp)
            Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null, modifier = Modifier.size(16.dp))
          }
        }

        // Language Display Mode Switcher (Arabic + Urdu Side-by-Side, Stacked, Arabic Only, Urdu Only, Full Tri-Lingual)
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          com.example.data.repository.IslamicDigitalLibraryRepository.BookReaderLanguageMode.entries.forEach { mode ->
            item {
              FilterChip(
                selected = readerLanguageMode == mode,
                onClick = { readerLanguageMode = mode },
                label = {
                  Text(
                    text = mode.label,
                    fontSize = 10.sp,
                    fontWeight = if (readerLanguageMode == mode) FontWeight.Bold else FontWeight.Medium
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EmeraldPrimary,
                  selectedLabelColor = Color.White
                ),
                modifier = Modifier
                  .height(28.dp)
                  .testTag("book_reader_lang_mode_${mode.name.lowercase()}")
              )
            }
          }
        }

        // In-Book Search Bar & Scope Toggle
        Spacer(modifier = Modifier.height(4.dp))
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
                text = if (searchAllBooksScope) "Search all ${IslamicBooksCatalog.allExpandedLibraryBooks.size} books..." else "Search inside this book (English, اردو, العربية)...",
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

        // Jild / Volume Selector Row (All Jilds Full Book Text + Full Jilds 1..N + Individual Pages/Chapters)
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          item {
            FilterChip(
              selected = showAllJildsFullText && selectedJildFilter == 0,
              onClick = {
                selectedJildFilter = 0
                showAllJildsFullText = true
              },
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
          items(distinctJildNumbers.size) { jIdx ->
            val jildNum = distinctJildNumbers[jIdx]
            val pagesInJild = book.chapters.count { it.jildNumber == jildNum }.coerceAtLeast(1)
            FilterChip(
              selected = selectedJildFilter == jildNum,
              onClick = {
                selectedJildFilter = jildNum
                showAllJildsFullText = false
                val firstChIdx = book.chapters.indexOfFirst { it.jildNumber == jildNum }
                if (firstChIdx >= 0) {
                  onSelectChapter(firstChIdx)
                }
              },
              label = {
                Text(
                  text = "Full Jild $jildNum ($pagesInJild Pages)",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary.copy(alpha = 0.85f),
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.height(30.dp).testTag("book_reader_jild_volume_chip_$jildNum")
            )
          }
          items(book.chapters.size) { idx ->
            val ch = book.chapters[idx]
            FilterChip(
              selected = !showAllJildsFullText && selectedJildFilter == 0 && safeChapterIdx == idx,
              onClick = {
                selectedJildFilter = 0
                showAllJildsFullText = false
                onSelectChapter(idx)
              },
              label = {
                Text(
                  text = "Jild ${ch.jildNumber} (Ch. ${ch.chapterNumber} • Pg. ${ch.pageNumber})",
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

    // Expandable Book Metadata, Attribution Confidence & Offline Download Sheet
    if (showBookMetadataSheet) {
      val isDisputed = book.attributionConfidence.contains("DISPUTED", ignoreCase = true)
      Surface(
        color = if (isDisputed) Color(0xFFFFF8E1) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 4.dp)
          .testTag("book_reader_metadata_sheet")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = "${book.titleArabic} • ${book.titleUrdu}",
            style = MaterialTheme.typography.titleSmall.copy(
              fontFamily = ScheherazadeFontFamily,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary,
              textAlign = TextAlign.Right
            ),
            modifier = Modifier.fillMaxWidth()
          )
          Text(
            text = "Author: ${book.author} (${book.eraHijri})",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Original Source & Edition: ${book.sourceTypeLabel} • ${book.numberingSystem}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
          )
          Text(
            text = "Attribution Confidence: ${book.attributionConfidence}",
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isDisputed) Color(0xFFE65100) else EmeraldPrimary
            )
          )
          Text(
            text = "Translator: ${book.translatorName} • Licence: ${book.licenseInfo}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = {
                com.example.data.repository.IslamicDigitalLibraryRepository.downloadBookOffline(context, book)
                isBookDownloaded = true
                Toast.makeText(context, "Full book downloaded for offline reading", Toast.LENGTH_SHORT).show()
              },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.testTag("book_reader_download_full_book_btn")
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isBookDownloaded) "Full Book Offline ✓" else "Download Full Book",
                fontSize = 11.sp
              )
            }
            OutlinedButton(
              onClick = {
                com.example.data.repository.IslamicDigitalLibraryRepository.downloadBookVolumeOffline(
                  context = context,
                  book = book,
                  jildNumber = currentChapter.jildNumber
                )
                Toast.makeText(
                  context,
                  "Volume (Jild ${currentChapter.jildNumber}) downloaded offline",
                  Toast.LENGTH_SHORT
                ).show()
              },
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.testTag("book_reader_download_volume_btn")
            ) {
              Text("Download Jild ${currentChapter.jildNumber}", fontSize = 11.sp)
            }
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

    // Chapter Content Body (Displays All Jilds continuously, all pages of a selected Jild, or a single Chapter)
    val chaptersToRender = remember(book, showAllJildsFullText, selectedJildFilter, currentChapter) {
      when {
        showAllJildsFullText -> book.chapters
        selectedJildFilter > 0 -> book.chapters.filter { it.jildNumber == selectedJildFilter }.ifEmpty { listOf(currentChapter) }
        else -> listOf(currentChapter)
      }
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp),
      contentPadding = PaddingValues(vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(
        items = chaptersToRender,
        key = { renderedChapter -> "${book.id}_ch_${renderedChapter.chapterNumber}_pg_${renderedChapter.pageNumber}" }
      ) { renderedChapter ->
        val chapterBookmarkKey = "${book.id}_ch_${renderedChapter.chapterNumber}"
        val isChapterBookmarked = chapterBookmarkKey in bookmarkedChaptersSet

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
                  text = "${renderedChapter.jildTitle} • Chapter ${renderedChapter.chapterNumber} • Page ${renderedChapter.pageNumber}",
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
              Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                  onClick = {
                    val nowBookmarked = com.example.data.repository.IslamicDigitalLibraryRepository.toggleBookChapterBookmark(
                      context = context,
                      bookId = book.id,
                      chapterNumber = renderedChapter.chapterNumber
                    )
                    bookmarkedChaptersSet = com.example.data.repository.IslamicDigitalLibraryRepository.getBookmarkedChapters(context)
                    Toast.makeText(
                      context,
                      if (nowBookmarked) "Chapter bookmarked" else "Bookmark removed",
                      Toast.LENGTH_SHORT
                    ).show()
                  },
                  modifier = Modifier.testTag("bookmark_chapter_btn_${renderedChapter.chapterNumber}")
                ) {
                  Icon(
                    if (isChapterBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark Chapter",
                    tint = if (isChapterBookmarked) GoldPrimary else EmeraldPrimary
                  )
                }
                IconButton(
                  onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val text = buildString {
                      append("${book.titleArabic} — ${book.titleEnglish}\n")
                      append("Author: ${book.author} (${book.eraHijri})\n")
                      append("${renderedChapter.jildTitle} • Ch. ${renderedChapter.chapterNumber} • Pg. ${renderedChapter.pageNumber}: ${renderedChapter.titleEnglish}\n\n")
                      append("${renderedChapter.arabicExcerpt}\n\n")
                      append("${IslamicBooksCatalog.formatUrduForReader(renderedChapter.urduContent)}\n\n")
                      append("${renderedChapter.englishContent}\n\n")
                      append("Source & Citation: ${book.numberingSystem} • Refs: ${renderedChapter.crossReferences}")
                    }
                    clipboard.setPrimaryClip(ClipData.newPlainText("Islamic Book Chapter", text))
                    Toast.makeText(context, "Chapter copied with full source citation", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.testTag("copy_chapter_btn_${renderedChapter.chapterNumber}")
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = "Copy Chapter")
                }
                IconButton(
                  onClick = {
                    val shareText = buildString {
                      append("${book.titleArabic} (${book.titleEnglish}) — ${book.author}\n")
                      append("${renderedChapter.jildTitle} • Ch. ${renderedChapter.chapterNumber} • Pg. ${renderedChapter.pageNumber}\n\n")
                      append("${renderedChapter.arabicExcerpt}\n\n")
                      append("${IslamicBooksCatalog.formatUrduForReader(renderedChapter.urduContent)}\n\n")
                      append("Refs: ${renderedChapter.crossReferences}")
                    }
                    runCatching {
                      val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                      }
                      context.startActivity(Intent.createChooser(intent, "Share Classical Book Passage"))
                    }
                  },
                  modifier = Modifier.testTag("share_chapter_btn_${renderedChapter.chapterNumber}")
                ) {
                  Icon(Icons.Default.Share, contentDescription = "Share Chapter", tint = EmeraldPrimary)
                }
                IconButton(
                  onClick = {
                    reportTargetPassageLabel = "${book.titleEnglish} • ${renderedChapter.jildTitle} • Ch. ${renderedChapter.chapterNumber} (Pg. ${renderedChapter.pageNumber})"
                  },
                  modifier = Modifier.testTag("report_chapter_btn_${renderedChapter.chapterNumber}")
                ) {
                  Icon(Icons.Default.Flag, contentDescription = "Report Passage Issue", tint = MaterialTheme.colorScheme.error)
                }
              }
            }

            if (renderedChapter.titleArabic.isNotBlank()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = buildHighlightedAnnotatedText(renderedChapter.titleArabic, effectiveHighlightQuery),
                style = MaterialTheme.typography.titleMedium.copy(
                  fontFamily = ScheherazadeFontFamily,
                  fontSize = (20 + fontScaleDelta).sp,
                  color = GoldPrimary,
                  fontWeight = FontWeight.Bold,
                  textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }

            if (renderedChapter.titleUrdu.isNotBlank()) {
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = buildHighlightedAnnotatedText(renderedChapter.titleUrdu, effectiveHighlightQuery),
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = (15 + fontScaleDelta).sp,
                  color = EmeraldPrimary,
                  fontWeight = FontWeight.Bold,
                  textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            val showArabic = readerLanguageMode != com.example.data.repository.IslamicDigitalLibraryRepository.BookReaderLanguageMode.URDU_ONLY
            val showUrdu = readerLanguageMode != com.example.data.repository.IslamicDigitalLibraryRepository.BookReaderLanguageMode.ARABIC_ONLY
            val showEnglish = readerLanguageMode == com.example.data.repository.IslamicDigitalLibraryRepository.BookReaderLanguageMode.FULL_TRILINGUAL
            val isSideBySide = readerLanguageMode == com.example.data.repository.IslamicDigitalLibraryRepository.BookReaderLanguageMode.SIDE_BY_SIDE_AR_UR

            if (isSideBySide) {
              // Side-by-Side Arabic (Right column) + Urdu (Left column)
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("side_by_side_ar_ur_container_${renderedChapter.chapterNumber}"),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
              ) {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = GoldPrimary.copy(alpha = 0.08f),
                  border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.30f)),
                  modifier = Modifier.weight(1f)
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                      text = "اردو ترجمہ",
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
                        IslamicBooksCatalog.formatUrduForReader(renderedChapter.urduContent),
                        effectiveHighlightQuery
                      ),
                      style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = UrduNastaliqFontFamily,
                        fontSize = (15 + fontScaleDelta).sp,
                        lineHeight = (26 + fontScaleDelta).sp,
                        textAlign = TextAlign.Right,
                        color = MaterialTheme.colorScheme.onSurface
                      ),
                      modifier = Modifier.fillMaxWidth()
                    )
                  }
                }

                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.30f)),
                  modifier = Modifier.weight(1f)
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                      text = "النص العربي الأصلي",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        textAlign = TextAlign.Right
                      ),
                      modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = buildHighlightedAnnotatedText(renderedChapter.arabicExcerpt, effectiveHighlightQuery),
                      style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = AmiriFontFamily,
                        fontSize = (20 + fontScaleDelta).sp,
                        lineHeight = (35 + fontScaleDelta).sp,
                        textAlign = TextAlign.Right,
                        color = MaterialTheme.colorScheme.onSurface
                      ),
                      modifier = Modifier.fillMaxWidth()
                    )
                  }
                }
              }
            } else {
              // Stacked / Individual Language Display
              if (showArabic) {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = buildHighlightedAnnotatedText(renderedChapter.arabicExcerpt, effectiveHighlightQuery),
                    style = MaterialTheme.typography.headlineSmall.copy(
                      fontFamily = AmiriFontFamily,
                      fontSize = (22 + fontScaleDelta).sp,
                      lineHeight = (38 + fontScaleDelta).sp,
                      textAlign = TextAlign.Right,
                      color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(14.dp)
                  )
                }
              }

              if (showEnglish && renderedChapter.transliteration.isNotBlank()) {
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
                    fontSize = (13 + fontScaleDelta).sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.secondary
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
              }

              if (showUrdu) {
                Spacer(modifier = Modifier.height(14.dp))
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
                    IslamicBooksCatalog.formatUrduForReader(renderedChapter.urduContent),
                    effectiveHighlightQuery
                  ),
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = (16 + fontScaleDelta).sp,
                    lineHeight = (27 + fontScaleDelta).sp,
                    textAlign = TextAlign.Right,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
              }

              if (showEnglish) {
                Spacer(modifier = Modifier.height(14.dp))
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
                    fontSize = (15 + fontScaleDelta).sp,
                    lineHeight = (23 + fontScaleDelta).sp
                  ),
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }

            // Footnotes Section (if present)
            if (renderedChapter.footnotes.isNotEmpty()) {
              Spacer(modifier = Modifier.height(10.dp))
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = GoldPrimary.copy(alpha = 0.10f),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                  verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                  Text(
                    text = "Footnotes & Critical Apparatus (حواشي وتحقيقات):",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = EmeraldPrimary
                    )
                  )
                  renderedChapter.footnotes.forEach { fn ->
                    Text(
                      text = "• $fn",
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    )
                  }
                }
              }
            }

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
                      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                      clipboard.setPrimaryClip(
                        ClipData.newPlainText("Book Citation", "${book.titleEnglish} — ${book.author} (${book.numberingSystem})")
                      )
                      Toast.makeText(context, "Book citation copied", Toast.LENGTH_SHORT).show()
                    }
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Source Edition: ${book.sourceTypeLabel} • ${book.numberingSystem}",
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

      item {
        val activeJildNum = if (selectedJildFilter > 0) selectedJildFilter else currentChapter.jildNumber
        val activeJildPos = distinctJildNumbers.indexOf(activeJildNum).coerceAtLeast(0)

        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Universal Previous Jild / Next Jild Row
          if (distinctJildNumbers.size > 1) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedButton(
                onClick = {
                  if (activeJildPos > 0) {
                    val prevJild = distinctJildNumbers[activeJildPos - 1]
                    selectedJildFilter = prevJild
                    showAllJildsFullText = false
                    val firstChIdx = book.chapters.indexOfFirst { it.jildNumber == prevJild }
                    if (firstChIdx >= 0) onSelectChapter(firstChIdx)
                  } else if (currentBookUniversalIdx > 0) {
                    onNavigateBook(-1)
                  }
                },
                enabled = activeJildPos > 0 || currentBookUniversalIdx > 0,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("prev_jild_button")
              ) {
                Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Prev Jild", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }

              Text(
                text = "Jild $activeJildNum of ${distinctJildNumbers.last()}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
              )

              OutlinedButton(
                onClick = {
                  if (activeJildPos < distinctJildNumbers.lastIndex) {
                    val nextJild = distinctJildNumbers[activeJildPos + 1]
                    selectedJildFilter = nextJild
                    showAllJildsFullText = false
                    val firstChIdx = book.chapters.indexOfFirst { it.jildNumber == nextJild }
                    if (firstChIdx >= 0) onSelectChapter(firstChIdx)
                  } else if (currentBookUniversalIdx >= 0 && currentBookUniversalIdx < allUniversalBooks.lastIndex) {
                    onNavigateBook(1)
                  }
                },
                enabled = activeJildPos < distinctJildNumbers.lastIndex || (currentBookUniversalIdx >= 0 && currentBookUniversalIdx < allUniversalBooks.lastIndex),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("next_jild_button")
              ) {
                Text("Next Jild", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null, modifier = Modifier.size(16.dp))
              }
            }
          }

          // Universal Previous Chapter / Next Chapter Row (steps across chapters & books)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedButton(
              onClick = {
                selectedJildFilter = 0
                showAllJildsFullText = false
                if (safeChapterIdx > 0) {
                  onSelectChapter(safeChapterIdx - 1)
                } else if (currentBookUniversalIdx > 0) {
                  onNavigateBook(-1)
                }
              },
              enabled = safeChapterIdx > 0 || currentBookUniversalIdx > 0,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("prev_chapter_button")
            ) {
              Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Previous Jild / Ch.", fontSize = 12.sp)
            }

            Text(
              text = "Ch. ${safeChapterIdx + 1} / ${book.chapters.size}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
            )

            OutlinedButton(
              onClick = {
                selectedJildFilter = 0
                showAllJildsFullText = false
                if (safeChapterIdx < book.chapters.lastIndex) {
                  onSelectChapter(safeChapterIdx + 1)
                } else if (currentBookUniversalIdx >= 0 && currentBookUniversalIdx < allUniversalBooks.lastIndex) {
                  onNavigateBook(1)
                }
              },
              enabled = safeChapterIdx < book.chapters.lastIndex || (currentBookUniversalIdx >= 0 && currentBookUniversalIdx < allUniversalBooks.lastIndex),
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

    if (reportTargetPassageLabel != null) {
      AlertDialog(
        onDismissRequest = {
          reportTargetPassageLabel = null
          reportReasonInput = ""
        },
        title = {
          Text("Report Passage / Source Verification", fontWeight = FontWeight.Bold)
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "Reference: $reportTargetPassageLabel",
              style = MaterialTheme.typography.labelMedium.copy(color = EmeraldPrimary, fontWeight = FontWeight.Bold)
            )
            OutlinedTextField(
              value = reportReasonInput,
              onValueChange = { reportReasonInput = it },
              label = { Text("Describe textual, diacritic, or translation issue") },
              minLines = 3,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("book_reader_report_reason_input")
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              Toast.makeText(
                context,
                "Report recorded for $reportTargetPassageLabel",
                Toast.LENGTH_SHORT
              ).show()
              reportTargetPassageLabel = null
              reportReasonInput = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            modifier = Modifier.testTag("book_reader_report_submit_btn")
          ) {
            Text("Submit Report")
          }
        },
        dismissButton = {
          TextButton(
            onClick = {
              reportTargetPassageLabel = null
              reportReasonInput = ""
            }
          ) {
            Text("Cancel")
          }
        }
      )
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
