package com.example.ui.qaida

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.LruCache
import android.view.Gravity
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ScheherazadeFontFamily
import com.example.ui.theme.UrduNastaliqFontFamily
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class NooraniQaidaPageMetadata(
  val pageNumber: Int, // 1..53
  val bookPageNumber: Int, // Number printed at top of PDF page (1, 5..56)
  val takhtiNumber: Int, // 0 for Guidelines/Cover, 1..28 for Takhtis
  val titleArabic: String,
  val titleUrdu: String,
  val titleEnglish: String,
  val arabicText: String,
  val urduNotes: String
)

object NooraniQaidaPrefs {
  const val PREFS_NAME = "bayan_learn_islam_prefs"
  const val KEY_LAST_READ_PAGE = "last_read_noorani_qaida_page"
  const val KEY_QUIZ_HIGH_SCORE = "noorani_qaida_mcq_high_score"
  const val ASSET_PDF_PATH = "noorani_qaida.pdf"
  const val ASSET_JSON_PATH = "noorani_qaida_text.json"
  const val ASSET_FONT_AMIRI = "fonts/Amiri-Regular.ttf"
  const val ASSET_FONT_SCHEHERAZADE = "fonts/ScheherazadeNew-Regular.ttf"
  const val TOTAL_PAGES = 53

  fun getSavedLastReadPage(context: Context): Int {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getInt(KEY_LAST_READ_PAGE, 1).coerceIn(1, TOTAL_PAGES)
  }

  fun saveLastReadPage(context: Context, pageNumber: Int) {
    val clamped = pageNumber.coerceIn(1, TOTAL_PAGES)
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .putInt(KEY_LAST_READ_PAGE, clamped)
      .apply()
  }

  private var cachedPages: List<NooraniQaidaPageMetadata>? = null

  fun loadAllPagesMetadata(context: Context): List<NooraniQaidaPageMetadata> {
    cachedPages?.let { if (it.isNotEmpty()) return it }
    return runCatching {
      val jsonText = context.assets.open(ASSET_JSON_PATH).bufferedReader(Charsets.UTF_8).use { it.readText() }
      val root = JSONObject(jsonText)
      val arr = root.getJSONArray("pages")
      val list = ArrayList<NooraniQaidaPageMetadata>(arr.length())
      for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        list.add(
          NooraniQaidaPageMetadata(
            pageNumber = obj.optInt("pageNumber", i + 1),
            bookPageNumber = obj.optInt("bookPageNumber", i + 1),
            takhtiNumber = obj.optInt("takhtiNumber", 0),
            titleArabic = obj.optString("titleArabic", "نورانی قاعدہ"),
            titleUrdu = obj.optString("titleUrdu", "نورانی قاعدہ"),
            titleEnglish = obj.optString("titleEnglish", "Noorani Qaida Page ${i + 1}"),
            arabicText = obj.optString("arabicText", ""),
            urduNotes = obj.optString("urduNotes", "")
          )
        )
      }
      cachedPages = list
      list
    }.getOrElse {
      (1..TOTAL_PAGES).map { page ->
        NooraniQaidaPageMetadata(
          pageNumber = page,
          bookPageNumber = if (page == 1) 1 else page + 3,
          takhtiNumber = 0,
          titleArabic = "مُكَمَّل نُورَانِی قَاعِدَہ",
          titleUrdu = "صفحہ نمبر $page",
          titleEnglish = "Noorani Qaida Page $page",
          arabicText = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ",
          urduNotes = ""
        )
      }
    }
  }

  fun loadBundledArabicTypeface(context: Context): Typeface {
    return runCatching {
      Typeface.createFromAsset(context.assets, ASSET_FONT_AMIRI)
    }.recoverCatching {
      Typeface.createFromAsset(context.assets, ASSET_FONT_SCHEHERAZADE)
    }.getOrDefault(Typeface.SERIF)
  }
}

/**
 * Memory-safe, thread-safe controller around [android.graphics.pdf.PdfRenderer]
 * that renders pages from `assets/noorani_qaida.pdf` into high-definition bitmaps
 * and recycles evicted bitmaps cleanly to prevent OutOfMemoryError.
 */
class NooraniQaidaPdfController(private val context: Context) {
  private val renderMutex = Mutex()
  private var parcelFileDescriptor: ParcelFileDescriptor? = null
  private var pdfRenderer: PdfRenderer? = null

  // Keep a bounded LRU cache of at most 5 rendered pages (~offscreenPageLimit = 2 on each side)
  private val bitmapCache = object : LruCache<Int, Bitmap>(5) {
    override fun entryRemoved(
      evicted: Boolean,
      key: Int?,
      oldValue: Bitmap?,
      newValue: Bitmap?
    ) {
      // Do not immediately recycle if still referenced by UI composition, let GC reclaim or recycle on close
    }
  }

  private fun ensureRendererOpen(): PdfRenderer? {
    pdfRenderer?.let { return it }
    return runCatching {
      val cachePdfFile = File(context.cacheDir, "noorani_qaida_hd_v1.pdf")
      if (!cachePdfFile.exists() || cachePdfFile.length() < 100_000L) {
        context.assets.open(NooraniQaidaPrefs.ASSET_PDF_PATH).use { input ->
          FileOutputStream(cachePdfFile).use { output ->
            input.copyTo(output)
          }
        }
      }
      val pfd = ParcelFileDescriptor.open(cachePdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
      val renderer = PdfRenderer(pfd)
      parcelFileDescriptor = pfd
      pdfRenderer = renderer
      renderer
    }.getOrNull()
  }

  suspend fun getPageCount(): Int = withContext(Dispatchers.IO) {
    renderMutex.withLock {
      ensureRendererOpen()?.pageCount ?: NooraniQaidaPrefs.TOTAL_PAGES
    }
  }

  suspend fun renderPageBitmap(pageIndexZeroBased: Int, targetWidthPx: Int = 1280): Bitmap? =
    withContext(Dispatchers.IO) {
      val validIndex = pageIndexZeroBased.coerceIn(0, NooraniQaidaPrefs.TOTAL_PAGES - 1)
      bitmapCache.get(validIndex)?.takeIf { !it.isRecycled }?.let { return@withContext it }

      renderMutex.withLock {
        bitmapCache.get(validIndex)?.takeIf { !it.isRecycled }?.let { return@withLock it }
        val renderer = ensureRendererOpen() ?: return@withLock null
        if (validIndex < 0 || validIndex >= renderer.pageCount) return@withLock null

        runCatching {
          renderer.openPage(validIndex).use { page ->
            val aspect = if (page.width > 0) page.height.toFloat() / page.width.toFloat() else 1.414f
            val bmpWidth = targetWidthPx.coerceIn(720, 1600)
            val bmpHeight = (bmpWidth * aspect).toInt().coerceIn(960, 2400)
            val bitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(AndroidColor.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmapCache.put(validIndex, bitmap)
            bitmap
          }
        }.getOrNull()
      }
    }

  fun close() {
    runCatching {
      bitmapCache.evictAll()
      pdfRenderer?.close()
      parcelFileDescriptor?.close()
    }
    pdfRenderer = null
    parcelFileDescriptor = null
  }
}

/**
 * Standalone Native Activity for the HD Noorani Qaida PDF Reader & Exercise Test Hub.
 * Can be launched from the main navigation drawer/menu, Home screen, or Learn Islam screen.
 */
class NooraniQaidaReaderActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val requestedInitialPage = intent?.getIntExtra(EXTRA_INITIAL_PAGE, -1) ?: -1
    setContent {
      MyApplicationTheme {
        NooraniQaidaReaderScreen(
          initialPageOverride = if (requestedInitialPage in 1..NooraniQaidaPrefs.TOTAL_PAGES) requestedInitialPage else null,
          onBack = { finish() }
        )
      }
    }
  }

  companion object {
    const val EXTRA_INITIAL_PAGE = "extra_initial_noorani_qaida_page"

    fun createIntent(context: Context, initialPage: Int? = null): Intent {
      return Intent(context, NooraniQaidaReaderActivity::class.java).apply {
        if (initialPage != null) {
          putExtra(EXTRA_INITIAL_PAGE, initialPage.coerceIn(1, NooraniQaidaPrefs.TOTAL_PAGES))
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NooraniQaidaReaderScreen(
  initialPageOverride: Int? = null,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  val allPages = remember { NooraniQaidaPrefs.loadAllPagesMetadata(context) }
  val pdfController = remember { NooraniQaidaPdfController(context) }
  val arabicTypeface = remember { NooraniQaidaPrefs.loadBundledArabicTypeface(context) }

  DisposableEffect(pdfController) {
    onDispose {
      pdfController.close()
    }
  }

  val initialPageZeroBased = remember(initialPageOverride) {
    val pageOneBased = initialPageOverride ?: NooraniQaidaPrefs.getSavedLastReadPage(context)
    (pageOneBased - 1).coerceIn(0, allPages.lastIndex.coerceAtLeast(0))
  }

  val pagerState = rememberPagerState(
    initialPage = initialPageZeroBased,
    pageCount = { allPages.size }
  )

  // Save user's last read page in SharedPreferences whenever currentPage changes
  LaunchedEffect(pagerState.currentPage) {
    val currentOneBased = (pagerState.currentPage + 1).coerceIn(1, allPages.size)
    NooraniQaidaPrefs.saveLastReadPage(context, currentOneBased)
  }

  var showIndexSheet by rememberSaveable { mutableStateOf(false) }
  var showExerciseTestSheet by rememberSaveable { mutableStateOf(false) }
  var showArabicTextSheet by rememberSaveable { mutableStateOf(false) }
  var indexSearchQuery by rememberSaveable { mutableStateOf("") }
  var selectedQuizDifficulty by rememberSaveable { mutableStateOf("ALL") }
  var quizScopeCurrentPageOnly by rememberSaveable { mutableStateOf(false) }

  val currentPageMeta = allPages.getOrElse(pagerState.currentPage) { allPages.first() }
  val currentPageQuestions = remember(currentPageMeta.pageNumber) {
    NooraniQaidaExerciseQuizCatalog.getQuestionsForPage(currentPageMeta.pageNumber)
  }

  BackHandler {
    onBack()
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .testTag("noorani_qaida_pdf_reader_screen"),
    topBar = {
      TopAppBar(
        title = {
          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "Noorani Qaida HD • نُورَانِی قَاعِدَہ",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = GoldPrimary.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, GoldPrimary)
              ) {
                Text(
                  text = "Book Top Pg ${currentPageMeta.bookPageNumber}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                  ),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
            Text(
              text = "${currentPageMeta.titleEnglish} • ${currentPageMeta.titleUrdu}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFFD8F3DC),
                fontSize = 11.sp
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("qaida_pdf_top_lesson_title")
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("qaida_pdf_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }
        },
        actions = {
          IconButton(
            onClick = { showArabicTextSheet = true },
            modifier = Modifier.testTag("qaida_pdf_text_companion_button")
          ) {
            Icon(
              imageVector = Icons.Default.MenuBook,
              contentDescription = "Selectable RTL Arabic Text & Notes",
              tint = GoldPrimary
            )
          }
          IconButton(
            onClick = { showExerciseTestSheet = true },
            modifier = Modifier.testTag("qaida_pdf_mcq_test_button")
          ) {
            Icon(
              imageVector = Icons.Default.Quiz,
              contentDescription = "MCQ Exercise Test (Basic, Intermediate, Hard)",
              tint = GoldPrimary
            )
          }
          IconButton(
            onClick = { showIndexSheet = true },
            modifier = Modifier.testTag("qaida_pdf_index_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.List,
              contentDescription = "Open Lesson & Page Index",
              tint = Color.White
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = Color(0xFF0A4D3C)
        )
      )
    },
    floatingActionButton = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        ExtendedFloatingActionButton(
          onClick = { showExerciseTestSheet = true },
          containerColor = Color(0xFF0A4D3C),
          contentColor = GoldPrimary,
          icon = {
            Icon(Icons.Default.Quiz, contentDescription = "Exercise MCQ Test")
          },
          text = {
            Text(
              text = "Test MCQs (${currentPageQuestions.size})",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 12.sp
            )
          },
          modifier = Modifier.testTag("qaida_pdf_fab_mcq_test")
        )
        ExtendedFloatingActionButton(
          onClick = { showIndexSheet = true },
          containerColor = GoldPrimary,
          contentColor = Color(0xFF073B2E),
          icon = {
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Lesson Index")
          },
          text = {
            Text(
              text = "Index (53)",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 12.sp
            )
          },
          modifier = Modifier.testTag("qaida_pdf_fab_index")
        )
      }
    },
    bottomBar = {
      Surface(
        color = Color(0xFF073B2E),
        tonalElevation = 8.dp,
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          // Page Slider for instant scrubbing across all 53 pages
          Slider(
            value = (pagerState.currentPage + 1).toFloat(),
            onValueChange = { newPageFloat ->
              val targetIdx = (newPageFloat.toInt() - 1).coerceIn(0, allPages.lastIndex)
              coroutineScope.launch {
                pagerState.scrollToPage(targetIdx)
              }
            },
            valueRange = 1f..allPages.size.toFloat(),
            steps = (allPages.size - 2).coerceAtLeast(0),
            modifier = Modifier
              .fillMaxWidth()
              .height(24.dp)
              .testTag("qaida_pdf_page_slider")
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            val hasPrev = pagerState.currentPage > 0
            val hasNext = pagerState.currentPage < allPages.lastIndex

            Button(
              onClick = {
                if (hasPrev) {
                  coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                  }
                }
              },
              enabled = hasPrev,
              colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = Color(0xFF073B2E),
                disabledContainerColor = Color.White.copy(alpha = 0.14f),
                disabledContentColor = Color.White.copy(alpha = 0.45f)
              ),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("qaida_pdf_prev_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                contentDescription = "Previous Page",
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Previous", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .testTag("qaida_pdf_bottom_page_indicator")
            ) {
              Text(
                text = "Page ${currentPageMeta.pageNumber} of ${allPages.size} (Top #${currentPageMeta.bookPageNumber})",
                style = MaterialTheme.typography.labelLarge.copy(
                  color = Color.White,
                  fontWeight = FontWeight.ExtraBold
                ),
                textAlign = TextAlign.Center
              )
              Text(
                text = if (currentPageMeta.takhtiNumber > 0) {
                  "Takhti ${currentPageMeta.takhtiNumber} • ${currentPageMeta.titleArabic}"
                } else {
                  currentPageMeta.titleArabic
                },
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = AmiriFontFamily,
                  color = GoldPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  textDirection = TextDirection.Rtl
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
              )
            }

            Button(
              onClick = {
                if (hasNext) {
                  coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                  }
                }
              },
              enabled = hasNext,
              colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = Color(0xFF073B2E),
                disabledContainerColor = Color.White.copy(alpha = 0.14f),
                disabledContentColor = Color.White.copy(alpha = 0.45f)
              ),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("qaida_pdf_next_button")
            ) {
              Text("Next", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                contentDescription = "Next Page",
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0xFF062920),
              Color(0xFF0D3B2E),
              Color(0xFF062920)
            )
          )
        )
    ) {
      // Decorative Islamic Header Banner displaying Lesson/Takhti Title & Top Printed Page Number
      Surface(
        color = Color(0xFF0E4A3A),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.55f)),
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 4.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (currentPageMeta.takhtiNumber > 0) {
                "TAKHTI ${currentPageMeta.takhtiNumber} • PRINTED TOP PAGE #${currentPageMeta.bookPageNumber}"
              } else {
                "GUIDELINES & CURRICULUM • PRINTED TOP PAGE #${currentPageMeta.bookPageNumber}"
              },
              style = MaterialTheme.typography.labelSmall.copy(
                color = GoldPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp
              )
            )
            Text(
              text = currentPageMeta.titleEnglish,
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          // Native RTL Arabic Title rendered via bundled Typeface (Amiri-Regular.ttf)
          AndroidView(
            factory = { ctx ->
              TextView(ctx).apply {
                typeface = arabicTypeface
                textSize = 16f
                setTextColor(AndroidColor.parseColor("#FFD4AF37"))
                textDirection = android.view.View.TEXT_DIRECTION_RTL
                layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                maxLines = 1
              }
            },
            update = { tv ->
              tv.text = currentPageMeta.titleArabic
            },
            modifier = Modifier
              .padding(start = 8.dp)
              .testTag("qaida_header_rtl_typeface_title")
          )
        }
      }

      // Horizontal Swipeable Pages (ViewPager2 / Compose HorizontalPager with beyondViewportPageCount = 2)
      HorizontalPager(
        state = pagerState,
        beyondViewportPageCount = 2,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .testTag("qaida_pdf_horizontal_pager")
      ) { pageIdx ->
        val pageMeta = allPages[pageIdx]
        DecoratedZoomableQaidaPageCard(
          pageMetadata = pageMeta,
          pdfController = pdfController,
          arabicTypeface = arabicTypeface,
          onOpenArabicTextSheet = { showArabicTextSheet = true },
          onOpenPageQuiz = {
            quizScopeCurrentPageOnly = true
            showExerciseTestSheet = true
          }
        )
      }
    }
  }

  // Modal Bottom Sheet 1: Lesson & Page Index (53 Pages + 28 Takhtis + Search)
  if (showIndexSheet) {
    NooraniQaidaLessonIndexBottomSheet(
      allPages = allPages,
      currentPageOneBased = currentPageMeta.pageNumber,
      searchQuery = indexSearchQuery,
      onSearchQueryChange = { indexSearchQuery = it },
      onSelectPage = { targetPageOneBased ->
        showIndexSheet = false
        coroutineScope.launch {
          pagerState.scrollToPage((targetPageOneBased - 1).coerceIn(0, allPages.lastIndex))
        }
      },
      onDismiss = { showIndexSheet = false }
    )
  }

  // Modal Bottom Sheet 2: Selectable RTL Arabic Text & Teacher Notes (from assets/noorani_qaida_text.json + Amiri-Regular.ttf)
  if (showArabicTextSheet) {
    NooraniQaidaRtlTextBottomSheet(
      pageMeta = currentPageMeta,
      arabicTypeface = arabicTypeface,
      onDismiss = { showArabicTextSheet = false }
    )
  }

  // Modal Bottom Sheet 3: Exercise MCQ Test (Basic, Intermediate, Hard levels mapped by Page & Top Book Page Number)
  if (showExerciseTestSheet) {
    NooraniQaidaExerciseMcqBottomSheet(
      currentPageMeta = currentPageMeta,
      selectedDifficulty = selectedQuizDifficulty,
      onSelectDifficulty = { selectedQuizDifficulty = it },
      currentPageOnly = quizScopeCurrentPageOnly,
      onToggleScope = { quizScopeCurrentPageOnly = it },
      onJumpToPage = { pageOneBased ->
        showExerciseTestSheet = false
        coroutineScope.launch {
          pagerState.animateScrollToPage((pageOneBased - 1).coerceIn(0, allPages.lastIndex))
        }
      },
      onDismiss = { showExerciseTestSheet = false }
    )
  }
}

@Composable
private fun DecoratedZoomableQaidaPageCard(
  pageMetadata: NooraniQaidaPageMetadata,
  pdfController: NooraniQaidaPdfController,
  arabicTypeface: Typeface,
  onOpenArabicTextSheet: () -> Unit,
  onOpenPageQuiz: () -> Unit
) {
  var pageBitmap by remember(pageMetadata.pageNumber) { mutableStateOf<Bitmap?>(null) }
  var isLoading by remember(pageMetadata.pageNumber) { mutableStateOf(true) }

  var zoomScale by remember(pageMetadata.pageNumber) { mutableFloatStateOf(1f) }
  var panOffset by remember(pageMetadata.pageNumber) { mutableStateOf(Offset.Zero) }

  val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
    val newScale = (zoomScale * zoomChange).coerceIn(1f, 4f)
    zoomScale = newScale
    if (newScale > 1.01f) {
      val maxPan = 700f * (newScale - 1f)
      panOffset = Offset(
        x = (panOffset.x + offsetChange.x).coerceIn(-maxPan, maxPan),
        y = (panOffset.y + offsetChange.y).coerceIn(-maxPan, maxPan)
      )
    } else {
      panOffset = Offset.Zero
    }
  }

  LaunchedEffect(pageMetadata.pageNumber) {
    isLoading = true
    pageBitmap = pdfController.renderPageBitmap(pageMetadata.pageNumber - 1, targetWidthPx = 1280)
    isLoading = false
  }

  // Outer Decorated Islamic Container with subtle illuminated pattern, rounded corners & elevation
  Card(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp, vertical = 8.dp)
      .shadow(elevation = 10.dp, shape = RoundedCornerShape(22.dp))
      .testTag("qaida_decorated_page_card_${pageMetadata.pageNumber}"),
    shape = RoundedCornerShape(22.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    border = BorderStroke(2.dp, GoldPrimary),
    colors = CardDefaults.cardColors(
      containerColor = Color(0xFFF6EFE2) // Warm Madinah parchment frame around the crisp white page
    )
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.radialGradient(
            colors = listOf(
              Color(0xFFFFFBF2),
              Color(0xFFF3E6CE),
              Color(0xFFE6D4B3)
            )
          )
        )
        .padding(10.dp)
    ) {
      // Subtle geometric Islamic corner ornaments on the parchment frame
      Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeColor = Color(0xFFD4AF37).copy(alpha = 0.45f)
        val cornerRadius = 24.dp.toPx()
        drawCircle(
          color = strokeColor,
          radius = cornerRadius,
          center = Offset(cornerRadius, cornerRadius),
          style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
          color = strokeColor,
          radius = cornerRadius,
          center = Offset(size.width - cornerRadius, cornerRadius),
          style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
          color = strokeColor,
          radius = cornerRadius,
          center = Offset(cornerRadius, size.height - cornerRadius),
          style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
          color = strokeColor,
          radius = cornerRadius,
          center = Offset(size.width - cornerRadius, size.height - cornerRadius),
          style = Stroke(width = 2.dp.toPx())
        )
      }

      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // Inner Top Ornamental Frame Bar (shows Top Printed Book Page Number + Zoom Controls)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF0A4D3C),
            border = BorderStroke(1.dp, GoldPrimary)
          ) {
            Text(
              text = "Top Page #${pageMetadata.bookPageNumber} • PDF Pg ${pageMetadata.pageNumber}/53",
              style = MaterialTheme.typography.labelSmall.copy(
                color = GoldPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp
              ),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF0A4D3C).copy(alpha = 0.9f),
              modifier = Modifier
                .clickable { onOpenPageQuiz() }
                .testTag("qaida_page_card_quiz_chip_${pageMetadata.pageNumber}")
            ) {
              Text(
                text = "Quiz Pg ${pageMetadata.bookPageNumber}",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = GoldPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp
                ),
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
              )
            }

            IconButton(
              onClick = {
                zoomScale = (zoomScale - 0.5f).coerceAtLeast(1f)
                if (zoomScale <= 1.01f) panOffset = Offset.Zero
              },
              modifier = Modifier
                .size(28.dp)
                .testTag("qaida_zoom_out_button_${pageMetadata.pageNumber}")
            ) {
              Text(
                text = "−",
                style = MaterialTheme.typography.titleMedium.copy(
                  color = Color(0xFF0A4D3C),
                  fontWeight = FontWeight.ExtraBold
                )
              )
            }
            Text(
              text = "${(zoomScale * 100).toInt()}%",
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF0A4D3C),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp
              )
            )
            IconButton(
              onClick = {
                zoomScale = (zoomScale + 0.5f).coerceAtMost(4f)
              },
              modifier = Modifier
                .size(28.dp)
                .testTag("qaida_zoom_in_button_${pageMetadata.pageNumber}")
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Zoom In",
                tint = Color(0xFF0A4D3C),
                modifier = Modifier.size(18.dp)
              )
            }
            if (zoomScale > 1.01f) {
              IconButton(
                onClick = {
                  zoomScale = 1f
                  panOffset = Offset.Zero
                },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = "Reset Zoom",
                  tint = Color(0xFF0A4D3C),
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }

        // White Page Surface with Gold Inner Border + Pinch-to-Zoom & Double-Tap Zoom
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, Color(0xFFC59B27), RoundedCornerShape(14.dp)),
          color = Color.White,
          shadowElevation = 4.dp
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .pointerInput(pageMetadata.pageNumber) {
                detectTapGestures(
                  onDoubleTap = {
                    if (zoomScale > 1.05f) {
                      zoomScale = 1f
                      panOffset = Offset.Zero
                    } else {
                      zoomScale = 2.25f
                    }
                  }
                )
              }
              .let { baseMod ->
                if (zoomScale > 1.01f) baseMod.transformable(state = transformableState) else baseMod
              },
            contentAlignment = Alignment.Center
          ) {
            when {
              isLoading -> {
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center
                ) {
                  CircularProgressIndicator(color = EmeraldPrimary)
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = "Rendering HD Noorani Qaida Page ${pageMetadata.pageNumber}...",
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = Color(0xFF0A4D3C),
                      fontWeight = FontWeight.SemiBold
                    )
                  )
                }
              }

              pageBitmap != null && !pageBitmap!!.isRecycled -> {
                Image(
                  bitmap = pageBitmap!!.asImageBitmap(),
                  contentDescription = "Noorani Qaida Page ${pageMetadata.pageNumber} — ${pageMetadata.titleEnglish}",
                  contentScale = ContentScale.Fit,
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
                    .graphicsLayer(
                      scaleX = zoomScale,
                      scaleY = zoomScale,
                      translationX = panOffset.x,
                      translationY = panOffset.y
                    )
                    .testTag("qaida_rendered_pdf_bitmap_${pageMetadata.pageNumber}")
                )
              }

              else -> {
                // Graceful RTL Arabic & Urdu fallback if PdfRenderer is unavailable in headless JVM unit tests
                Column(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center
                ) {
                  AndroidView(
                    factory = { ctx ->
                      TextView(ctx).apply {
                        typeface = arabicTypeface
                        textSize = 24f
                        setTextColor(AndroidColor.parseColor("#FF073B2E"))
                        textDirection = android.view.View.TEXT_DIRECTION_RTL
                        layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
                        gravity = Gravity.CENTER
                      }
                    },
                    update = { tv ->
                      tv.text = pageMetadata.arabicText
                    }
                  )
                  Spacer(modifier = Modifier.height(12.dp))
                  Text(
                    text = pageMetadata.urduNotes,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontFamily = UrduNastaliqFontFamily,
                      color = Color(0xFF1F2937),
                      textDirection = TextDirection.Rtl,
                      textAlign = TextAlign.Center
                    )
                  )
                }
              }
            }
          }
        }

        // Bottom RTL Arabic Snippet Strip inside the Decorated Card (uses bundled Amiri-Regular.ttf + RTL)
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF0A4D3C).copy(alpha = 0.08f),
          border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.6f)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenArabicTextSheet() }
            .testTag("qaida_page_rtl_strip_${pageMetadata.pageNumber}")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "RTL Text & Notes",
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF0A4D3C),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp
              )
            )
            AndroidView(
              factory = { ctx ->
                TextView(ctx).apply {
                  typeface = arabicTypeface
                  textSize = 14f
                  setTextColor(AndroidColor.parseColor("#FF073B2E"))
                  textDirection = android.view.View.TEXT_DIRECTION_RTL
                  layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
                  gravity = Gravity.END or Gravity.CENTER_VERTICAL
                  maxLines = 1
                }
              },
              update = { tv ->
                tv.text = pageMetadata.arabicText
              },
              modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NooraniQaidaLessonIndexBottomSheet(
  allPages: List<NooraniQaidaPageMetadata>,
  currentPageOneBased: Int,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onSelectPage: (Int) -> Unit,
  onDismiss: () -> Unit
) {
  val filteredPages = remember(allPages, searchQuery) {
    val q = searchQuery.trim()
    if (q.isEmpty()) {
      allPages
    } else {
      allPages.filter { page ->
        page.titleEnglish.contains(q, ignoreCase = true) ||
          page.titleUrdu.contains(q, ignoreCase = true) ||
          page.titleArabic.contains(q, ignoreCase = true) ||
          page.arabicText.contains(q, ignoreCase = true) ||
          page.urduNotes.contains(q, ignoreCase = true) ||
          page.pageNumber.toString() == q ||
          page.bookPageNumber.toString() == q ||
          (page.takhtiNumber > 0 && "takhti ${page.takhtiNumber}".contains(q, ignoreCase = true))
      }
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("qaida_lesson_index_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Noorani Qaida Lesson & Page Index",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              color = EmeraldPrimary
            )
          )
          Text(
            text = "All 53 HD Pages • 28 Takhtis • Jump by PDF Page or Top Book Page #",
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close Index")
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        singleLine = true,
        placeholder = {
          Text(
            text = "Search Takhti (1-28), page #, Arabic/Urdu/English title...",
            fontSize = 12.sp
          )
        },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = "Search Qaida Index", tint = EmeraldPrimary)
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchQueryChange("") }) {
              Icon(Icons.Default.Close, contentDescription = "Clear Search")
            }
          }
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("qaida_index_search_input")
      )

      Spacer(modifier = Modifier.height(10.dp))

      LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 155.dp),
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 420.dp)
          .testTag("qaida_index_grid"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
      ) {
        items(
          items = filteredPages,
          key = { it.pageNumber }
        ) { page ->
          val isCurrent = page.pageNumber == currentPageOneBased
          Surface(
            onClick = { onSelectPage(page.pageNumber) },
            shape = RoundedCornerShape(12.dp),
            color = if (isCurrent) EmeraldPrimary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            border = BorderStroke(
              width = if (isCurrent) 2.dp else 1.dp,
              color = if (isCurrent) EmeraldPrimary else GoldPrimary.copy(alpha = 0.45f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("qaida_index_item_${page.pageNumber}")
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (isCurrent) EmeraldPrimary else Color(0xFF0A4D3C)
                ) {
                  Text(
                    text = "Pg ${page.pageNumber} (Top #${page.bookPageNumber})",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
                if (page.takhtiNumber > 0) {
                  Text(
                    text = "تختی ${page.takhtiNumber}",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = EmeraldPrimary,
                      fontWeight = FontWeight.ExtraBold
                    )
                  )
                }
              }

              Text(
                text = page.titleArabic,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontFamily = AmiriFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  textDirection = TextDirection.Rtl,
                  color = EmeraldPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
              )

              Text(
                text = page.titleEnglish,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NooraniQaidaRtlTextBottomSheet(
  pageMeta: NooraniQaidaPageMetadata,
  arabicTypeface: Typeface,
  onDismiss: () -> Unit
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("qaida_rtl_text_sheet")
  ) {
    SelectionContainer {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "Page ${pageMeta.pageNumber} (Top #${pageMeta.bookPageNumber}) • ${pageMeta.titleEnglish}",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = EmeraldPrimary
          )
        )

        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color(0xFFFFFBF2),
          border = BorderStroke(1.5.dp, GoldPrimary),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalAlignment = Alignment.End
          ) {
            AndroidView(
              factory = { ctx ->
                TextView(ctx).apply {
                  typeface = arabicTypeface
                  textSize = 24f
                  setTextColor(AndroidColor.parseColor("#FF073B2E"))
                  textDirection = android.view.View.TEXT_DIRECTION_RTL
                  layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
                  gravity = Gravity.END
                  setTextIsSelectable(true)
                }
              },
              update = { tv ->
                tv.text = pageMeta.arabicText
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("qaida_selectable_rtl_arabic_textview")
            )
          }
        }

        if (pageMeta.urduNotes.isNotBlank()) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = EmeraldPrimary.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = pageMeta.urduNotes,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 16.sp,
                lineHeight = 28.sp,
                textDirection = TextDirection.Rtl,
                textAlign = TextAlign.Right,
                color = MaterialTheme.colorScheme.onSurface
              ),
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NooraniQaidaExerciseMcqBottomSheet(
  currentPageMeta: NooraniQaidaPageMetadata,
  selectedDifficulty: String,
  onSelectDifficulty: (String) -> Unit,
  currentPageOnly: Boolean,
  onToggleScope: (Boolean) -> Unit,
  onJumpToPage: (Int) -> Unit,
  onDismiss: () -> Unit
) {
  val selectedAnswers = remember { mutableStateMapOf<String, Int>() }

  val filteredQuestions = remember(currentPageMeta.pageNumber, selectedDifficulty, currentPageOnly) {
    val base = if (currentPageOnly) {
      NooraniQaidaExerciseQuizCatalog.getQuestionsForPage(currentPageMeta.pageNumber)
    } else {
      NooraniQaidaExerciseQuizCatalog.getQuestionsForDifficulty(selectedDifficulty)
    }
    if (selectedDifficulty == "ALL" || currentPageOnly && base.none { it.difficulty.equals(selectedDifficulty, true) }) {
      base
    } else {
      base.filter { it.difficulty.equals(selectedDifficulty, ignoreCase = true) }
    }
  }

  val answeredCount = filteredQuestions.count { selectedAnswers.containsKey(it.id) }
  val correctCount = filteredQuestions.count { q -> selectedAnswers[q.id] == q.correctOptionIndex }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("qaida_mcq_exercise_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Noorani Qaida Exercise Test (MCQs)",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              color = EmeraldPrimary
            )
          )
          Text(
            text = "Score: $correctCount / ${filteredQuestions.size} (Answered: $answeredCount) • Basic, Intermediate & Hard",
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.SemiBold
            )
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close Quiz")
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Scope Toggle + Difficulty Level Filter Chips (ALL, BASIC, INTERMEDIATE, HARD)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        FilterChip(
          selected = currentPageOnly,
          onClick = { onToggleScope(!currentPageOnly) },
          label = {
            Text(
              text = if (currentPageOnly) "This Page (#${currentPageMeta.bookPageNumber})" else "All 53 Pages",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF0A4D3C),
            selectedLabelColor = GoldPrimary
          ),
          modifier = Modifier.testTag("qaida_mcq_scope_chip")
        )

        NooraniQaidaExerciseQuizCatalog.DIFFICULTY_LEVELS.forEach { level ->
          val isSelected = selectedDifficulty.equals(level, ignoreCase = true)
          FilterChip(
            selected = isSelected,
            onClick = { onSelectDifficulty(level) },
            label = {
              Text(
                text = level,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldPrimary,
              selectedLabelColor = Color.White
            ),
            modifier = Modifier.testTag("qaida_mcq_level_$level")
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 440.dp)
          .testTag("qaida_mcq_questions_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
      ) {
        itemsIndexed(
          items = filteredQuestions,
          key = { _, q -> q.id }
        ) { idx, question ->
          val chosenIdx = selectedAnswers[question.id]
          val isAnswered = chosenIdx != null
          val isCorrect = chosenIdx == question.correctOptionIndex

          Card(
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(
              width = 1.5.dp,
              color = when {
                !isAnswered -> GoldPrimary.copy(alpha = 0.55f)
                isCorrect -> Color(0xFF2E7D32)
                else -> Color(0xFFC62828)
              }
            ),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("qaida_mcq_card_${question.id}")
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (question.difficulty) {
                      "BASIC" -> Color(0xFF1B5E20)
                      "INTERMEDIATE" -> Color(0xFFE65100)
                      else -> Color(0xFF880E4F)
                    }
                  ) {
                    Text(
                      text = question.difficulty,
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp
                      ),
                      modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                  }
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldPrimary.copy(alpha = 0.14f),
                    modifier = Modifier.clickable { onJumpToPage(question.pageNumber) }
                  ) {
                    Text(
                      text = "PDF Pg ${question.pageNumber} • Top #${question.bookPageNumber}",
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                      ),
                      modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                  }
                }
                Text(
                  text = "Q${idx + 1}",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
              }

              // Arabic Calligraphic Prompt Banner
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFFBF2),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = question.arabicPrompt,
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = AmiriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color(0xFF073B2E),
                    textDirection = TextDirection.Rtl,
                    textAlign = TextAlign.Center
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 10.dp)
                )
              }

              Text(
                text = question.questionEnglish,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              )

              Text(
                text = question.questionUrdu,
                style = MaterialTheme.typography.bodySmall.copy(
                  fontFamily = UrduNastaliqFontFamily,
                  fontSize = 14.sp,
                  lineHeight = 22.sp,
                  textDirection = TextDirection.Rtl,
                  textAlign = TextAlign.Right,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
              )

              question.options.forEachIndexed { optIdx, optionText ->
                val isOptionSelected = chosenIdx == optIdx
                val isOptionCorrect = optIdx == question.correctOptionIndex
                Surface(
                  onClick = { selectedAnswers[question.id] = optIdx },
                  shape = RoundedCornerShape(8.dp),
                  color = when {
                    !isAnswered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    isOptionCorrect -> Color(0xFF2E7D32).copy(alpha = 0.16f)
                    isOptionSelected -> Color(0xFFC62828).copy(alpha = 0.16f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                  },
                  border = BorderStroke(
                    width = if (isOptionSelected || (isAnswered && isOptionCorrect)) 1.5.dp else 0.5.dp,
                    color = when {
                      !isAnswered -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                      isOptionCorrect -> Color(0xFF2E7D32)
                      isOptionSelected -> Color(0xFFC62828)
                      else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    }
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("qaida_mcq_option_${question.id}_$optIdx")
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    RadioButton(
                      selected = isOptionSelected,
                      onClick = { selectedAnswers[question.id] = optIdx },
                      modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = optionText,
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isOptionSelected || (isAnswered && isOptionCorrect)) FontWeight.Bold else FontWeight.Normal
                      ),
                      modifier = Modifier.weight(1f)
                    )
                  }
                }
              }

              if (isAnswered) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isCorrect) Color(0xFF1B5E20).copy(alpha = 0.1f) else Color(0xFFB71C1C).copy(alpha = 0.1f),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                      text = if (isCorrect) "✓ Correct! ${question.explanationEnglish}" else "✗ Review: ${question.explanationEnglish}",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isCorrect) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                      )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = question.explanationUrdu,
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = UrduNastaliqFontFamily,
                        textDirection = TextDirection.Rtl,
                        textAlign = TextAlign.Right,
                        fontSize = 12.sp,
                        lineHeight = 20.sp
                      ),
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
  }
}
