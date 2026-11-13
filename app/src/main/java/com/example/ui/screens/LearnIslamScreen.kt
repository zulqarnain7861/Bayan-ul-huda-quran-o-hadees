package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.player.YtEmbedHost
import com.example.ui.audio.DuaAudioController
import com.example.ui.components.*
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CompactLineHeightStyle
import com.example.ui.theme.CompactPlatformTextStyle
import com.example.ui.theme.UrduNastaliqFontFamily

private val AmiriQuranFont = AmiriFontFamily
private val NotoNastaliqUrduFont = UrduNastaliqFontFamily
private val CompactArabicUrduTextStyle = TextStyle(
    platformStyle = CompactPlatformTextStyle,
    lineHeightStyle = CompactLineHeightStyle
)

enum class LearnIslamTab(
    val id: String,
    val titleEnglish: String,
    val titleUrdu: String
) {
    NOORANI_QAIDA("qaida", "Noorani Qaida", "نورانی قاعدہ"),
    IMAN_AND_KALIMAS("iman", "Iman & 6 Kalimas", "ایمان اور ۶ کلمے"),
    NAMAZ_AND_WUDU("namaz", "Namaz & Wudu", "وضو اور مکمل نماز"),
    NIMAZ_JANAZA("nimaz_janaza", "Nimaz Janaza", "نمازِ جنازہ"),
    DAILY_DUAS_SEQ("duas_seq", "Daily Duas", "مسنون دعائیں (ترتیب وار)"),
    SEHRI_NIYAT_SOURCES("sehri_niyat", "Sehri Niyat & Fiqh", "سحری کی نیت اور فقہی رہنمائی"),
    PROPHET_STORIES("prophet_stories", "25 Prophet Stories", "قصص الانبیاء (۲۵ انبیاء)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnIslamScreen(
    duaAudioController: DuaAudioController,
    initialTab: LearnIslamTab = LearnIslamTab.NOORANI_QAIDA,
    onBack: () -> Unit = {},
    onOpenQuranSurah: (Int, Int) -> Unit = { _, _ -> },
    onOpenNooraniQaidaPdfBook: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("bayan_learn_islam_prefs", Context.MODE_PRIVATE)
    }

    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }
    LaunchedEffect(initialTab) {
        selectedTab = initialTab
    }
    var selectedLessonIndex by remember {
        mutableIntStateOf(prefs.getInt("last_qaida_lesson_index", 0).coerceIn(0, LearnIslamCurriculumData.nooraniQaidaLessons.lastIndex))
    }
    var completedLessons by remember {
        mutableStateOf(prefs.getStringSet("completed_qaida_lessons", emptySet())?.toSet() ?: emptySet())
    }
    var bookmarkedItems by remember {
        mutableStateOf(prefs.getStringSet("bookmarked_qaida_items", emptySet())?.toSet() ?: emptySet())
    }
    var showBookmarkedOnly by remember { mutableStateOf(false) }

    val playbackState by duaAudioController.playbackState.collectAsStateWithLifecycle()

    fun toggleLessonComplete(lessonId: String) {
        val next = if (completedLessons.contains(lessonId)) completedLessons - lessonId else completedLessons + lessonId
        completedLessons = next
        prefs.edit().putStringSet("completed_qaida_lessons", next).apply()
    }

    fun toggleBookmarkItem(itemId: String) {
        val next = if (bookmarkedItems.contains(itemId)) bookmarkedItems - itemId else bookmarkedItems + itemId
        bookmarkedItems = next
        prefs.edit().putStringSet("bookmarked_qaida_items", next).apply()
    }

    fun selectLesson(index: Int) {
        val valid = index.coerceIn(0, LearnIslamCurriculumData.nooraniQaidaLessons.lastIndex)
        selectedLessonIndex = valid
        duaAudioController.clearComingSoonNotice()
        prefs.edit().putInt("last_qaida_lesson_index", valid).apply()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Learn Islam • تعلیمِ اسلام (درسگاہ نصاب)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Noorani Qaida • 6 Kalimas & Iman • Wudu & Complete Salah • Nimaz Janaza • Daily Duas",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD8F3DC)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("learn_islam_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A4D3C)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Audio Controls Bar (Authentic Quranic Verse Audio & Verified Human Audio status)
            LearnIslamAudioControlBar(
                isPlaying = playbackState.isPlaying,
                activeTitle = playbackState.activeDuaTitle,
                activeStageLabel = playbackState.activeStageLabel,
                isComingSoonNotice = playbackState.isComingSoonNotice,
                repeatCountTarget = playbackState.repeatCountTarget,
                onCycleRepeat = { duaAudioController.toggleRepeatTarget() },
                onStopAudio = { duaAudioController.stop() }
            )

            // 5 Sub-section Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF073B2E))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(LearnIslamTab.entries) { tab ->
                    val isSelected = selectedTab == tab
                    Surface(
                        onClick = {
                            selectedTab = tab
                            duaAudioController.clearComingSoonNotice()
                        },
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.testTag("learn_islam_tab_${tab.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = tab.titleEnglish,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF073B2E) else Color.White
                            )
                            Text(
                                text = tab.titleUrdu,
                                style = CompactArabicUrduTextStyle,
                                fontFamily = NotoNastaliqUrduFont,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = if (isSelected) Color(0xFF073B2E) else Color(0xFFD8F3DC)
                            )
                        }
                    }
                }
            }

            when (selectedTab) {
                LearnIslamTab.NOORANI_QAIDA -> {
                    NooraniQaidaSectionContent(
                        lessons = LearnIslamCurriculumData.nooraniQaidaLessons,
                        selectedLessonIndex = selectedLessonIndex,
                        completedLessons = completedLessons,
                        bookmarkedItems = bookmarkedItems,
                        showBookmarkedOnly = showBookmarkedOnly,
                        activePlayingId = playbackState.activeDuaId,
                        isPlaying = playbackState.isPlaying,
                        onSelectLesson = { selectLesson(it) },
                        onToggleLessonComplete = { toggleLessonComplete(it) },
                        onToggleBookmark = { toggleBookmarkItem(it) },
                        onToggleShowBookmarkedOnly = { showBookmarkedOnly = !showBookmarkedOnly },
                        onPlayFullLesson = { lesson ->
                            duaAudioController.playQaidaLessonSequence(
                                lessonNumber = lesson.lessonNumber,
                                lessonTitle = lesson.titleEnglish,
                                items = lesson.practiceItems
                            )
                        },
                        onPlayItem = { item ->
                            duaAudioController.playLearningItem(
                                id = item.id,
                                title = "${item.arabicText} (${item.transliteration})",
                                arabicText = item.arabicText,
                                transliteration = item.transliteration,
                                englishText = item.makhrajOrRuleEnglish,
                                urduText = item.makhrajOrRuleUrdu,
                                quranSurahAyah = item.quranSurahAyah
                            )
                        },
                        onOpenNooraniQaidaPdfBook = onOpenNooraniQaidaPdfBook
                    )
                }

                LearnIslamTab.IMAN_AND_KALIMAS -> {
                    ImanAndKalimasSectionContent(
                        items = LearnIslamCurriculumData.imanAndKalimaItems,
                        activePlayingId = playbackState.activeDuaId,
                        isPlaying = playbackState.isPlaying,
                        isComingSoonNotice = playbackState.isComingSoonNotice,
                        onPlayItem = { item ->
                            duaAudioController.playLearningItem(
                                id = item.id,
                                title = item.titleEnglish,
                                arabicText = item.arabicText,
                                transliteration = item.transliteration,
                                englishText = item.englishTranslation,
                                urduText = item.urduTranslation,
                                quranSurahAyah = null,
                                humanAudioUrl = item.humanAudioUrl
                            )
                        }
                    )
                }

                LearnIslamTab.NAMAZ_AND_WUDU -> {
                    NamazAndWuduSectionContent(
                        steps = LearnIslamCurriculumData.wuduAndSalahSteps,
                        activePlayingId = playbackState.activeDuaId,
                        isPlaying = playbackState.isPlaying,
                        isComingSoonNotice = playbackState.isComingSoonNotice,
                        onPlayStep = { step ->
                            val exactQuranicRef = step.quranSurahNumber?.let {
                                Pair(it, step.quranAyahRange?.first ?: 1)
                            }
                            duaAudioController.playLearningItem(
                                id = step.id,
                                title = step.titleEnglish,
                                arabicText = step.arabicRecitation,
                                transliteration = step.transliteration,
                                englishText = step.englishMeaning,
                                urduText = step.urduMeaning,
                                quranSurahAyah = exactQuranicRef,
                                humanAudioUrl = step.humanAudioUrl
                            )
                        },
                        onOpenQuranSurah = onOpenQuranSurah
                    )
                }

                LearnIslamTab.NIMAZ_JANAZA -> {
                    NimazJanazaSectionContent()
                }

                LearnIslamTab.DAILY_DUAS_SEQ -> {
                    DailyDuasSequenceSectionContent(
                        duas = LearnIslamCurriculumData.dailyDuasInSequence,
                        activePlayingId = playbackState.activeDuaId,
                        isPlaying = playbackState.isPlaying,
                        isComingSoonNotice = playbackState.isComingSoonNotice,
                        onPlayDua = { entry ->
                            duaAudioController.playLearningItem(
                                id = entry.duaId,
                                title = entry.titleEnglish,
                                arabicText = entry.arabicText,
                                transliteration = entry.transliteration,
                                englishText = entry.englishTranslation,
                                urduText = entry.urduTranslation,
                                quranSurahAyah = entry.quranSurahAyah,
                                humanAudioUrl = entry.humanAudioUrl
                            )
                        }
                    )
                }

                LearnIslamTab.SEHRI_NIYAT_SOURCES -> {
                    SehriNiyatAndSourcesSectionContent(
                        guidance = LearnIslamCurriculumData.sehriNiyatGuidance,
                        sources = LearnIslamCurriculumData.verifiedSourceRegistry,
                        activePlayingId = playbackState.activeDuaId,
                        isPlaying = playbackState.isPlaying,
                        onPlayFormula = { formula ->
                            duaAudioController.playLearningItem(
                                id = formula.id,
                                title = formula.labelEnglish,
                                arabicText = formula.arabicText,
                                transliteration = formula.transliteration,
                                englishText = formula.englishTranslation,
                                urduText = formula.urduTranslation,
                                quranSurahAyah = null
                            )
                        }
                    )
                }

                LearnIslamTab.PROPHET_STORIES -> {
                    ProphetStoriesHubView()
                }
            }
        }
    }
}

@Composable
private fun LearnIslamAudioControlBar(
    isPlaying: Boolean,
    activeTitle: String,
    activeStageLabel: String,
    isComingSoonNotice: Boolean,
    repeatCountTarget: Int,
    onCycleRepeat: () -> Unit,
    onStopAudio: () -> Unit
) {
    Surface(
        color = Color(0xFF125C47),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("learn_islam_audio_control_bar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
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
                    FilterChip(
                        selected = repeatCountTarget > 1,
                        onClick = onCycleRepeat,
                        label = {
                            Text(
                                text = "Repeat: ${repeatCountTarget}x",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Cycle Repeat Practice Count",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White.copy(alpha = 0.14f),
                            labelColor = Color.White,
                            iconColor = Color(0xFFD4AF37),
                            selectedContainerColor = Color(0xFFD4AF37),
                            selectedLabelColor = Color(0xFF073B2E),
                            selectedLeadingIconColor = Color(0xFF073B2E)
                        ),
                        modifier = Modifier.testTag("qaida_repeat_chip")
                    )

                    Text(
                        text = if (isComingSoonNotice) {
                            DuaAudioController.REAL_VOICE_COMING_SOON_MESSAGE
                        } else if (isPlaying && activeStageLabel.isNotBlank()) {
                            activeStageLabel
                        } else {
                            "Offline Authentic Noorani Qaida, Quran & Sunnah Audio"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isComingSoonNotice) Color(0xFFFFD166) else Color(0xFFD8F3DC),
                        modifier = Modifier.testTag("learn_islam_audio_status_text")
                    )
                }

                if (isPlaying) {
                    TextButton(
                        onClick = onStopAudio,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFFD166)),
                        modifier = Modifier.testTag("learn_islam_stop_audio_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Stop Audio",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Stop",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NooraniQaidaSectionContent(
    lessons: List<NooraniQaidaLesson>,
    selectedLessonIndex: Int,
    completedLessons: Set<String>,
    bookmarkedItems: Set<String>,
    showBookmarkedOnly: Boolean,
    activePlayingId: String?,
    isPlaying: Boolean,
    onSelectLesson: (Int) -> Unit,
    onToggleLessonComplete: (String) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onToggleShowBookmarkedOnly: () -> Unit,
    onPlayFullLesson: (NooraniQaidaLesson) -> Unit,
    onPlayItem: (QaidaPracticeItem) -> Unit,
    onOpenNooraniQaidaPdfBook: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentLesson = lessons.getOrElse(selectedLessonIndex) { lessons.first() }
    val isLessonCompleted = completedLessons.contains(currentLesson.id)
    val selectedQuizAnswers = remember(currentLesson.id) { mutableStateMapOf<String, Int>() }
    val itemSelfCheckFeedback = remember(currentLesson.id) { mutableStateMapOf<String, Boolean>() }
    val lastReadPdfPage = remember { com.example.ui.qaida.NooraniQaidaPrefs.getSavedLastReadPage(context) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("noorani_qaida_lazy_column"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Native HD 53-Page Noorani Qaida PDF Reader & MCQ Exercise Test Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A4D3C)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD4AF37)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("learn_islam_open_hd_qaida_pdf_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "مُكَمَّل نُورَانِی قَاعِدَہ (۵۳ صفحات • ۲۸ تختیاں)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = AmiriQuranFont,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD4AF37),
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = "Complete 53-Page HD Noorani Qaida Book + Basic/Intermediate/Hard MCQ Tests",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD4AF37).copy(alpha = 0.22f)
                        ) {
                            Text(
                                text = "Resume Pg $lastReadPdfPage/53",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFD4AF37),
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenNooraniQaidaPdfBook,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD4AF37),
                                contentColor = Color(0xFF073B2E)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("learn_islam_open_hd_qaida_pdf_btn")
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Open HD Qaida Book (53 Pages)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                context.startActivity(
                                    com.example.ui.qaida.NooraniQaidaReaderActivity.createIntent(context, lastReadPdfPage)
                                )
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("learn_islam_launch_qaida_activity_btn")
                        ) {
                            Text("Full Screen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Progress & Lesson Selector Header
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Noorani Qaida Progress: ${completedLessons.size} / ${lessons.size} Lessons",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "All 17 Lessons Available Offline with Authentic Human Audio",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FilterChip(
                            selected = showBookmarkedOnly,
                            onClick = onToggleShowBookmarkedOnly,
                            label = { Text("Saved (${bookmarkedItems.size})", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (showBookmarkedOnly) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Filter Bookmarked Qaida Items",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("qaida_bookmarks_filter_chip")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { completedLessons.size.toFloat() / lessons.size.coerceAtLeast(1).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF0A4D3C)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.testTag("qaida_lesson_selector_row")
                    ) {
                        itemsIndexed(lessons) { idx, lesson ->
                            val isSelected = idx == selectedLessonIndex
                            val isDone = completedLessons.contains(lesson.id)
                            Surface(
                                onClick = { onSelectLesson(idx) },
                                shape = RoundedCornerShape(10.dp),
                                color = when {
                                    isSelected -> Color(0xFF0A4D3C)
                                    isDone -> Color(0xFF1B7A58).copy(alpha = 0.18f)
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFFD4AF37) else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.testTag("qaida_lesson_chip_${lesson.lessonNumber}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isDone) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Completed",
                                            tint = if (isSelected) Color(0xFFD4AF37) else Color(0xFF1B7A58),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = "Lesson ${lesson.lessonNumber}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Lesson Banner & Rule Explanation
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A4D3C))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = currentLesson.titleEnglish,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = currentLesson.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD8F3DC)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = currentLesson.titleArabic,
                            style = CompactArabicUrduTextStyle,
                            fontFamily = AmiriQuranFont,
                            fontSize = 20.sp,
                            lineHeight = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37),
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = currentLesson.titleUrdu,
                            style = CompactArabicUrduTextStyle,
                            fontFamily = NotoNastaliqUrduFont,
                            fontSize = 14.sp,
                            lineHeight = 23.sp,
                            color = Color.White,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    )

                    Text(
                        text = "Tajweed & Pronunciation Rule:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD4AF37)
                    )
                    Text(
                        text = currentLesson.ruleSummaryEnglish,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentLesson.ruleSummaryUrdu,
                        style = CompactArabicUrduTextStyle,
                        fontFamily = NotoNastaliqUrduFont,
                        fontSize = 13.sp,
                        lineHeight = 21.sp,
                        color = Color(0xFFE9F5EC),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentLesson.editionNote,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB7E4C7)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onPlayFullLesson(currentLesson) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD4AF37),
                            contentColor = Color(0xFF073B2E)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("qaida_listen_full_lesson_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Listen Full Lesson Offline",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Listen Full Lesson ${currentLesson.lessonNumber} (Offline Audio)",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onToggleLessonComplete(currentLesson.id) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isLessonCompleted) Color(0xFFD4AF37) else Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("qaida_complete_lesson_${currentLesson.lessonNumber}")
                        ) {
                            Icon(
                                imageVector = if (isLessonCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isLessonCompleted) "Lesson Completed" else "Mark Lesson Complete",
                                fontSize = 12.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (selectedLessonIndex > 0) {
                                FilledTonalButton(
                                    onClick = { onSelectLesson(selectedLessonIndex - 1) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("qaida_prev_lesson_btn")
                                ) {
                                    Text("Prev", fontSize = 12.sp)
                                }
                            }
                            if (selectedLessonIndex < lessons.lastIndex) {
                                Button(
                                    onClick = { onSelectLesson(selectedLessonIndex + 1) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD4AF37),
                                        contentColor = Color(0xFF073B2E)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("qaida_next_lesson_btn")
                                ) {
                                    Text("Next Lesson", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Practice Items List
        val displayedItems = if (showBookmarkedOnly) {
            lessons.flatMap { it.practiceItems }.filter { bookmarkedItems.contains(it.id) }
        } else {
            currentLesson.practiceItems
        }

        items(displayedItems, key = { it.id }) { item ->
            val isItemPlaying = isPlaying && activePlayingId == item.id
            val isBookmarked = bookmarkedItems.contains(item.id)
            val selfCheckResult = itemSelfCheckFeedback[item.id]
            val isCompactInlineArabic = false

            Card(
                onClick = { onPlayItem(item) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isItemPlaying) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isItemPlaying) 2.dp else 1.dp,
                    color = if (isItemPlaying) Color(0xFF1B7A58) else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("qaida_item_${item.id}")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Play button, Bookmark button, Transliteration & Audio status
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            FilledIconButton(
                                onClick = { onPlayItem(item) },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (isItemPlaying) Color(0xFFD4AF37) else Color(0xFF0A4D3C),
                                    contentColor = if (isItemPlaying) Color(0xFF073B2E) else Color.White
                                ),
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("qaida_play_${item.id}")
                            ) {
                                Icon(
                                    imageVector = if (isItemPlaying) Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = "Play pronunciation for ${item.transliteration}",
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { onToggleBookmark(item.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("qaida_bookmark_${item.id}")
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark ${item.transliteration}",
                                    tint = if (isBookmarked) Color(0xFFD4AF37) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.transliteration,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0A4D3C),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (item.quranSurahAyah != null) {
                                        "✓ Offline Authentic Audio • Quran (${item.quranSurahAyah.first}:${item.quranSurahAyah.second})"
                                    } else {
                                        "✓ Offline Authentic Audio"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF1B7A58),
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (isCompactInlineArabic) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0A4D3C).copy(alpha = 0.06f),
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .widthIn(min = 54.dp, max = 108.dp)
                            ) {
                                Text(
                                    text = item.arabicText,
                                    style = CompactArabicUrduTextStyle,
                                    fontFamily = AmiriQuranFont,
                                    fontSize = 28.sp,
                                    lineHeight = 36.sp,
                                    letterSpacing = 0.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF073B2E),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (!isCompactInlineArabic) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0A4D3C).copy(alpha = 0.06f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item.arabicText,
                                style = CompactArabicUrduTextStyle,
                                fontFamily = AmiriQuranFont,
                                fontSize = 26.sp,
                                lineHeight = 38.sp,
                                letterSpacing = 0.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF073B2E),
                                textAlign = TextAlign.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Makhraj & Harakat Rule: ${item.makhrajOrRuleEnglish}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = item.makhrajOrRuleUrdu,
                        style = CompactArabicUrduTextStyle,
                        fontFamily = NotoNastaliqUrduFont,
                        fontSize = 13.sp,
                        lineHeight = 21.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = "Tip: ${item.pronunciationTip}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1B7A58)
                    )

                    // Interactive Makhraj & Harakat Correct / Incorrect Self-Check
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = when (selfCheckResult) {
                                true -> "✓ Correct! Makhraj & Harakat verified"
                                false -> "✗ Incorrect — Review Makhraj & Tip above"
                                null -> "Practice Check:"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (selfCheckResult) {
                                true -> Color(0xFF1B7A58)
                                false -> Color(0xFFC62828)
                                null -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .testTag("qaida_self_check_status_${item.id}")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = selfCheckResult == true,
                                onClick = { itemSelfCheckFeedback[item.id] = true },
                                label = { Text("✓ Correct", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFD8F3DC),
                                    selectedLabelColor = Color(0xFF0A4D3C)
                                ),
                                modifier = Modifier.testTag("qaida_mark_correct_${item.id}")
                            )
                            FilterChip(
                                selected = selfCheckResult == false,
                                onClick = { itemSelfCheckFeedback[item.id] = false },
                                label = { Text("✗ Incorrect", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFCDD2),
                                    selectedLabelColor = Color(0xFFB71C1C)
                                ),
                                modifier = Modifier.testTag("qaida_mark_incorrect_${item.id}")
                            )
                        }
                    }
                }
            }
        }

        // Interactive Lesson Quiz Section with Explicit Correct / Incorrect Feedback
        if (currentLesson.quizQuestions.isNotEmpty() && !showBookmarkedOnly) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("qaida_quiz_section")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Lesson ${currentLesson.lessonNumber} Self-Check Quiz • مشق و امتحان",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0A4D3C)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        currentLesson.quizQuestions.forEachIndexed { qIdx, question ->
                            val selectedOption = selectedQuizAnswers[question.id]
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Q${qIdx + 1}: ${question.questionEnglish}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0A4D3C).copy(alpha = 0.1f),
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .widthIn(max = 120.dp)
                                    ) {
                                        Text(
                                            text = question.arabicPrompt,
                                            style = CompactArabicUrduTextStyle,
                                            fontFamily = AmiriQuranFont,
                                            fontSize = 20.sp,
                                            lineHeight = 28.sp,
                                            letterSpacing = 0.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0A4D3C),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = question.questionUrdu,
                                    style = CompactArabicUrduTextStyle,
                                    fontFamily = NotoNastaliqUrduFont,
                                    fontSize = 12.sp,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                question.options.forEachIndexed { optIdx, optionText ->
                                    val isChosen = selectedOption == optIdx
                                    val isCorrect = optIdx == question.correctIndex
                                    val containerColor = when {
                                        selectedOption == null -> MaterialTheme.colorScheme.surface
                                        isCorrect -> Color(0xFFD8F3DC)
                                        isChosen -> Color(0xFFFFCDD2)
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                    Surface(
                                        onClick = { selectedQuizAnswers[question.id] = optIdx },
                                        shape = RoundedCornerShape(8.dp),
                                        color = containerColor,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isChosen) Color(0xFF0A4D3C) else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                            .testTag("qaida_quiz_option_${question.id}_$optIdx")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = optionText,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (selectedOption != null && (isChosen || isCorrect)) {
                                                Text(
                                                    text = if (isCorrect) "✓ Correct" else "✗ Incorrect",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isCorrect) Color(0xFF0A4D3C) else Color(0xFFB71C1C)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (selectedOption != null) {
                                    val isAnswerCorrect = selectedOption == question.correctIndex
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isAnswerCorrect) Color(0xFFD8F3DC) else Color(0xFFFFEBEE),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isAnswerCorrect) Color(0xFF1B7A58) else Color(0xFFC62828)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("quiz_feedback_${question.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = if (isAnswerCorrect) {
                                                    "✓ Correct! ${question.explanationEnglish}"
                                                } else {
                                                    "✗ Incorrect — ${question.explanationEnglish}"
                                                },
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (isAnswerCorrect) Color(0xFF0A4D3C) else Color(0xFFB71C1C),
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = question.explanationUrdu,
                                                style = CompactArabicUrduTextStyle,
                                                fontFamily = NotoNastaliqUrduFont,
                                                fontSize = 12.sp,
                                                lineHeight = 19.sp,
                                                color = if (isAnswerCorrect) Color(0xFF0A4D3C) else Color(0xFFB71C1C),
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.End
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
}

@Composable
private fun ImanAndKalimasSectionContent(
    items: List<ImanAndKalimaItem>,
    activePlayingId: String?,
    isPlaying: Boolean,
    isComingSoonNotice: Boolean,
    onPlayItem: (ImanAndKalimaItem) -> Unit
) {
    val selfCheckMap = remember { mutableStateMapOf<String, Boolean>() }
    val sortedItems = remember(items) {
        items.sortedBy { item ->
            val hasAudio = !item.humanAudioUrl.isNullOrBlank() ||
                DuaAudioController.hasOfflineQaidaAudioAsset(null, item.id)
            if (hasAudio) 0 else 1
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("iman_kalimas_lazy_column"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A4D3C))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Nimaz-e-Mutarjam Part 1: Six Kalimas, Iman-e-Mujmal & Iman-e-Mufassal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "نصابِ تعلیمِ اسلام: کلمہ طیب، کلمہ شہادت، شش کلمے، ایمانِ مجمل، ایمانِ مفصل اور ارکانِ اسلام",
                        style = CompactArabicUrduTextStyle,
                        fontFamily = NotoNastaliqUrduFont,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFFD4AF37),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        items(sortedItems, key = { it.id }) { item ->
            val isItemPlaying = isPlaying && activePlayingId == item.id
            val isItemComingSoon = isComingSoonNotice && activePlayingId == item.id
            val hasHumanAudio = (!item.humanAudioUrl.isNullOrBlank() || DuaAudioController.hasOfflineQaidaAudioAsset(null, item.id)) && !isItemComingSoon
            val itemSelfCheck = selfCheckMap[item.id]
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isItemPlaying) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isItemPlaying) Color(0xFF1B7A58) else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("iman_item_${item.id}")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.titleEnglish,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0A4D3C)
                            )
                            Text(
                                text = item.titleUrdu,
                                style = CompactArabicUrduTextStyle,
                                fontFamily = NotoNastaliqUrduFont,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when {
                                    hasHumanAudio -> Color(0xFFE8F5E9)
                                    isItemComingSoon -> Color(0xFFFFF8E1)
                                    else -> Color(0xFFF5F5F5)
                                },
                                border = if (isItemComingSoon) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37)) else null
                            ) {
                                Text(
                                    text = if (hasHumanAudio) {
                                        "✓ Authentic Human Audio (Hisn al-Muslim)"
                                    } else {
                                        "No audio available"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (hasHumanAudio || isItemComingSoon) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        hasHumanAudio -> Color(0xFF1B5E20)
                                        isItemComingSoon -> Color(0xFF5D4037)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                        .testTag("iman_coming_soon_${item.id}")
                                )
                            }
                        }
                        if (hasHumanAudio) {
                            FilledIconButton(
                                onClick = { onPlayItem(item) },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (isItemPlaying) Color(0xFFD4AF37) else Color(0xFF0A4D3C),
                                    contentColor = if (isItemPlaying) Color(0xFF073B2E) else Color.White
                                ),
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("iman_play_${item.id}")
                            ) {
                                Icon(
                                    imageVector = if (isItemPlaying) Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = "Play ${item.titleEnglish}",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0A4D3C).copy(alpha = 0.06f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.arabicText,
                            style = CompactArabicUrduTextStyle,
                            fontFamily = AmiriQuranFont,
                            fontSize = 22.sp,
                            lineHeight = 34.sp,
                            letterSpacing = 0.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF073B2E),
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.transliteration,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1B7A58)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.urduTranslation,
                        style = CompactArabicUrduTextStyle,
                        fontFamily = NotoNastaliqUrduFont,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.englishTranslation,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recitation Check:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FilterChip(
                            selected = itemSelfCheck == true,
                            onClick = { selfCheckMap[item.id] = true },
                            label = { Text("✓ Correct", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE8F5E9),
                                selectedLabelColor = Color(0xFF1B5E20)
                            ),
                            modifier = Modifier.testTag("iman_check_correct_${item.id}")
                        )
                        FilterChip(
                            selected = itemSelfCheck == false,
                            onClick = { selfCheckMap[item.id] = false },
                            label = { Text("✗ Incorrect", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFEBEE),
                                selectedLabelColor = Color(0xFFB71C1C)
                            ),
                            modifier = Modifier.testTag("iman_check_incorrect_${item.id}")
                        )
                    }
                    if (itemSelfCheck != null) {
                        Text(
                            text = if (itemSelfCheck) "✓ Correct: Kalima/Iman recited with accurate Arabic letters and harakat." else "✗ Incorrect: Review the Arabic harakat and transliteration above and repeat.",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (itemSelfCheck) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    Text(
                        text = "Reference: ${item.authenticReference}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A4D3C)
                    )
                    Text(
                        text = item.scholarlyAndCurriculumNote,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NamazAndWuduSectionContent(
    steps: List<WuduAndSalahStepItem>,
    activePlayingId: String?,
    isPlaying: Boolean,
    isComingSoonNotice: Boolean = false,
    onPlayStep: (WuduAndSalahStepItem) -> Unit,
    onOpenQuranSurah: (Int, Int) -> Unit
) {
    // Default to "NAMAZ_MUTARJAM" ("Complete Salah") so the pure non-sectarian Prophetic Salah is shown cleanly first,
    // while Sectarian / Madhhab Namaz differences are strictly separated into their own "SECTARIAN_NAMAZ" tab.
    var selectedSectionFilter by remember { mutableStateOf("NAMAZ_MUTARJAM") }
    val filteredSteps = remember(steps, selectedSectionFilter) {
        val base = when (selectedSectionFilter) {
            "NAMAZ_MUTARJAM" -> emptyList() // Pure non-sectarian Prophetic Salah only (from namaz_muhammadi.json)
            "SECTARIAN_NAMAZ" -> steps.filter { it.section == "NAMAZ_MUTARJAM" }
            "ALL" -> steps.filter { it.section != "NAMAZ_MUTARJAM" } // Keep sectarian/madhhab Namaz separate
            else -> steps.filter { it.section == selectedSectionFilter }
        }
        base.sortedBy { step ->
            val hasAudio = step.quranSurahNumber != null ||
                !step.humanAudioUrl.isNullOrBlank() ||
                DuaAudioController.hasOfflineQaidaAudioAsset(null, step.id)
            if (hasAudio) 0 else 1
        }
    }

    val context = LocalContext.current
    val namazPrefs = remember {
        context.getSharedPreferences("bayan_namaz_muhammadi_prefs", Context.MODE_PRIVATE)
    }
    var selectedDisplayMode by remember { mutableStateOf(NamazDisplayMode.FULL_LIST) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var arabicFontSizeSp by remember { mutableFloatStateOf(22f) }
    var showUrdu by remember { mutableStateOf(true) }
    var showEnglish by remember { mutableStateOf(true) }
    var showRomanUrdu by remember { mutableStateOf(true) }
    var completedStepIds by remember {
        mutableStateOf(
            namazPrefs.getStringSet("completed_namaz_steps", emptySet())
                ?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        )
    }
    var bookmarkedStepIds by remember {
        mutableStateOf(
            namazPrefs.getStringSet("bookmarked_namaz_steps", emptySet())
                ?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        )
    }

    fun toggleStepCompleted(stepId: Int) {
        val next = if (completedStepIds.contains(stepId)) completedStepIds - stepId else completedStepIds + stepId
        completedStepIds = next
        namazPrefs.edit().putStringSet("completed_namaz_steps", next.map { it.toString() }.toSet()).apply()
    }

    fun toggleStepBookmarked(stepId: Int) {
        val next = if (bookmarkedStepIds.contains(stepId)) bookmarkedStepIds - stepId else bookmarkedStepIds + stepId
        bookmarkedStepIds = next
        namazPrefs.edit().putStringSet("bookmarked_namaz_steps", next.map { it.toString() }.toSet()).apply()
    }

    var selectedRakatFlowKey by remember { mutableStateOf("ALL_30") }
    var selectedRakatNumberKey by remember { mutableStateOf("rakat_1") }
    val sourceMetadata = remember { LearnIslamCurriculumData.namazMuhammadiSourceMetadata }
    val foundationalSteps = remember { LearnIslamCurriculumData.namazMuhammadiFoundationalSteps }
    val closingAdvice = remember { LearnIslamCurriculumData.namazMuhammadiClosingAdvice }
    val auditedSalahSteps = remember(context) { NamazMuhammadiModuleData.getAllAuditedSteps(context) }
    val rakatFlows = remember { LearnIslamCurriculumData.rakatFlows }
    val activeRakatFlow = remember(selectedRakatFlowKey, rakatFlows) {
        rakatFlows.find { it.prayerKey == selectedRakatFlowKey }
    }
    val displayedAuditedSalahSteps = remember(selectedRakatFlowKey, selectedRakatNumberKey, auditedSalahSteps, activeRakatFlow) {
        if (activeRakatFlow == null) {
            auditedSalahSteps
        } else {
            val stepNumbers = activeRakatFlow.rakats[selectedRakatNumberKey]
                ?: activeRakatFlow.rakats.values.firstOrNull().orEmpty()
            val withPrerequisite = if (selectedRakatNumberKey == "rakat_1") listOf(0) + stepNumbers else stepNumbers
            withPrerequisite.mapNotNull { num -> auditedSalahSteps.find { it.id == num } }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("namaz_wudu_lazy_column"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val filters = listOf(
                    "NAMAZ_MUTARJAM" to "Complete Salah (واحد سنتِ طریقہ)",
                    "WUDU" to "Wudu (Ablution)",
                    "SALAH_PREREQUISITES" to "Prerequisites & Rak'at",
                    "SHORT_SURAHS" to "Short Surahs",
                    "SECTARIAN_NAMAZ" to "Sectarian / Madhhab Namaz (فقہی مسالک)",
                    "ALL" to "All Sunnah & Wudu"
                )
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = selectedSectionFilter == key,
                        onClick = { selectedSectionFilter = key },
                        label = { Text(label, fontSize = 12.sp) },
                        modifier = Modifier.testTag("namaz_filter_chip_$key")
                    )
                }
            }
        }

        if (selectedSectionFilter == "SECTARIAN_NAMAZ") {
            item(key = "sectarian_namaz_separation_banner") {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD4AF37)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sectarian_namaz_banner_card")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Sectarian / Madhhab Namaz Comparison (فقہی مسالک کی نماز — الگ ٹیب)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF5D4037)
                        )
                        Text(
                            text = "یہ سیکشن فقہی مسالک (حنفی، شافعی، مالکی، حنبلی، جعفری) کے فروعی اختلافات کو نبی کریم ﷺ کے خالص «واحد سنتِ طریقہ (Complete Salah)» سے مکمل طور پر الگ رکھتا ہے تاکہ سنتِ نبوی ﷺ اور فقہی مسالک میں خلط ملط نہ ہو۔",
                            style = CompactArabicUrduTextStyle,
                            fontFamily = NotoNastaliqUrduFont,
                            fontSize = 13.sp,
                            lineHeight = 21.sp,
                            color = Color(0xFF4E342E),
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Separated from 'Complete Salah (واحد سنتِ طریقہ)': This tab isolates school-specific fiqh positions (Hanafi, Shafi'i, Maliki, Hanbali, Ja'fari) so the non-sectarian Prophetic Salah remains pure and unmixed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF3E2723)
                        )
                    }
                }
            }
        }

        if (selectedSectionFilter == "NAMAZ_MUTARJAM" || selectedSectionFilter == "ALL") {
            item(key = "namaz_top_practical_video_card") {
                var topVideoProfile by remember { mutableStateOf(PlaybackDataProfile.BALANCED) }
                var topVideoWifiOnly by remember { mutableStateOf(false) }
                val topPracticalNamazLecture = remember {
                    LectureRecord(
                        youtubeId = "Cou1OW7jxx4",
                        title = "Practical Tareqa of Nimaz — Namaz-e-Muhammadi ﷺ",
                        verifiedTitle = "Practical Tareqa of Nimaz (عملی طریقۂ نمازِ محمدی ﷺ) — Engineer Muhammad Ali Mirza",
                        speaker = "Engineer Muhammad Ali Mirza",
                        channelHandle = "@NOOR-e-HIDAYAT-EMAM",
                        channelUrl = "https://www.youtube.com/@NOOR-e-HIDAYAT-EMAM",
                        sourceTier = SourceTier.FAN_CHANNEL,
                        countryStatus = CountryStatus.PLAYABLE_GLOBAL,
                        verificationState = RecordVerificationState.CONFIRMED_LIVE,
                        recordFormat = LectureFormat.CLIP,
                        lectureNumber = 70,
                        seriesName = "Practical Salah & Sunnah Series",
                        recordedYear = 2023,
                        durationSec = 580,
                        topics = listOf("salah_azan", "hadith_sciences"),
                        relatedYoutubeIds = listOf("rsb3X6A8q_o", "dCMRGuPvUlE"),
                        summary = "Practical step-by-step 9m 40s demonstration of Namaz-e-Muhammadi ﷺ (https://youtu.be/Cou1OW7jxx4?si=5qn4EXXiQlwSHbXS) — available through Vine / yMusic SDK & offline.",
                        transcriptStatus = TranscriptStatus.NONE,
                        referenceStatus = ReferenceStatus.VERIFIED,
                        references = listOf(
                            "Sahih al-Bukhari: 631, 735, 828",
                            "Sahih Muslim: 390, 865",
                            "Sunan Abi Dawud: 730, 734"
                        ),
                        alternateYoutubeIds = listOf("rsb3X6A8q_o", "dCMRGuPvUlE")
                    )
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD4AF37)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("namaz_top_practical_video_card")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Practical Tareqa of Nimaz Video",
                                    tint = Color(0xFF0A4D3C),
                                    modifier = Modifier.size(22.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Practical Tareqa of Nimaz (9m 40s · Nimaz ka tareqa)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0A4D3C),
                                        modifier = Modifier.testTag("namaz_top_practical_video_title")
                                    )
                                    Text(
                                        text = "https://youtu.be/Cou1OW7jxx4?si=5qn4EXXiQlwSHbXS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "عملی طریقۂ نمازِ محمدی ﷺ",
                                style = CompactArabicUrduTextStyle,
                                fontFamily = NotoNastaliqUrduFont,
                                fontSize = 14.sp,
                                lineHeight = 23.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF073B2E),
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        YtEmbedHost(
                            lecture = topPracticalNamazLecture,
                            allIndexedLectures = listOf(topPracticalNamazLecture),
                            selectedProfile = topVideoProfile,
                            wifiOnlyEnabled = topVideoWifiOnly,
                            onSelectProfile = { topVideoProfile = it },
                            onToggleWifiOnly = { topVideoWifiOnly = it },
                            onWatchSecondsRecorded = { _, _, _ -> },
                            onSelectAlternateLecture = {},
                            onReportBroken = {}
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE8F5E9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B7A58)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("namaz_top_video_references_box")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Practical Tareqa of Nimaz — Authentic Hadith References (International Numbering):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0A4D3C)
                                )
                                Text(
                                    text = "Sahih al-Bukhari: 631, 735, 736, 740, 756, 780, 789, 799, 809, 822, 823, 828, 831, 6008; Sahih Muslim: 390, 391, 394, 401, 402, 579, 580, 582, 674, 772, 865; Sunan Abi Dawud: 730, 734, 759, 775, 869, 874, 996; Jami at-Tirmidhi: 243, 304",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF073B2E)
                                )
                            }
                        }
                    }
                }
            }

            item(key = "complete_salah_header_card") {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A4D3C)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("complete_salah_heading_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Complete Salah — Namaz-e-Muhammadi ﷺ (واحد سنتِ طریقہ)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "مکمل نمازِ محمدی ﷺ — واحد سنتِ طریقہ (نیت، بنیادی احادیث ۰.۱ تا ۰.۴، ۳۰ مراحلِ نماز اور بعد از سلام مسنون اذکار)",
                            style = CompactArabicUrduTextStyle,
                            fontFamily = NotoNastaliqUrduFont,
                            fontSize = 13.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFFD4AF37),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("namaz_muhammadi_key_hadith_box")
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = sourceMetadata.keyHadithArabic,
                                    style = CompactArabicUrduTextStyle,
                                    fontFamily = AmiriQuranFont,
                                    fontSize = 20.sp,
                                    lineHeight = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD4AF37),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "\"${sourceMetadata.keyHadithEnglish}\" — ${sourceMetadata.keyHadithReferences.joinToString("; ") { it.formatted() }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Document Date: ${sourceMetadata.documentDate} • ${sourceMetadata.referenceStyle}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFD8F3DC)
                                )
                            }
                        }

                        Text(
                            text = "Select Rakat Flow or view all steps in order (Niyyat, Foundational Narrations, Qiyam, Ruku, Qawmah, Sujood, Jalsah, Tashahhud, Salam & Post-Prayer Adhkar):",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD8F3DC)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val flowOptions = listOf("ALL_30" to "All Steps (0–32) + Section 0") +
                                rakatFlows.map { it.prayerKey to it.labelEnglish }
                            items(flowOptions) { (fKey, fLabel) ->
                                val selected = selectedRakatFlowKey == fKey
                                Surface(
                                    onClick = {
                                        selectedRakatFlowKey = fKey
                                        selectedRakatNumberKey = "rakat_1"
                                        currentStepIndex = 0
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (selected) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.14f),
                                    modifier = Modifier.testTag("rakat_flow_chip_$fKey")
                                ) {
                                    Text(
                                        text = fLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color(0xFF073B2E) else Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                        if (activeRakatFlow != null) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(activeRakatFlow.rakats.keys.toList()) { rKey ->
                                    val rSelected = selectedRakatNumberKey == rKey
                                    val displayLabel = rKey.replace("_", " ").replaceFirstChar { it.uppercase() }
                                    Surface(
                                        onClick = {
                                            selectedRakatNumberKey = rKey
                                            currentStepIndex = 0
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (rSelected) Color.White else Color.White.copy(alpha = 0.12f),
                                        modifier = Modifier.testTag("rakat_sub_chip_$rKey")
                                    ) {
                                        Text(
                                            text = "$displayLabel (${activeRakatFlow.rakats[rKey]?.size ?: 0} steps)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (rSelected) Color(0xFF0A4D3C) else Color(0xFFD8F3DC),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Mode Selector, Font Size Controls & Language Toggles
            item(key = "namaz_interactive_mode_controls") {
                NamazMuhammadiInteractiveHeaderAndModeBar(
                    selectedMode = selectedDisplayMode,
                    onSelectMode = { selectedDisplayMode = it },
                    arabicFontSizeSp = arabicFontSizeSp,
                    onChangeArabicFontSize = { arabicFontSizeSp = it },
                    showUrdu = showUrdu,
                    onToggleUrdu = { showUrdu = !showUrdu },
                    showEnglish = showEnglish,
                    onToggleEnglish = { showEnglish = !showEnglish },
                    showRomanUrdu = showRomanUrdu,
                    onToggleRomanUrdu = { showRomanUrdu = !showRomanUrdu },
                    completedStepIds = completedStepIds,
                    totalStepsCount = auditedSalahSteps.size
                )
            }

            if (selectedDisplayMode == NamazDisplayMode.STEP_BY_STEP) {
                item(key = "namaz_step_by_step_guided_container") {
                    NamazSequentialStepByStepView(
                        steps = displayedAuditedSalahSteps,
                        currentStepIndex = currentStepIndex,
                        onSelectStepIndex = { currentStepIndex = it },
                        arabicFontSizeSp = arabicFontSizeSp,
                        showUrdu = showUrdu,
                        showEnglish = showEnglish,
                        showRomanUrdu = showRomanUrdu,
                        completedStepIds = completedStepIds,
                        bookmarkedStepIds = bookmarkedStepIds,
                        onToggleStepCompleted = ::toggleStepCompleted,
                        onToggleStepBookmarked = ::toggleStepBookmarked
                    )
                }
            } else if (selectedDisplayMode == NamazDisplayMode.PRAYER_STRUCTURES) {
                item(key = "namaz_prayer_structures_container") {
                    NamazDailyPrayerStructuresSection()
                }
            } else if (selectedDisplayMode == NamazDisplayMode.AUDIT_REPORT) {
                item(key = "namaz_audit_report_container") {
                    NamazReferenceAuditReportSection()
                }
            } else {
                // Section 0: Foundational narrations on the manner of prayer (0.1 to 0.4)
                if (selectedRakatFlowKey == "ALL_30") {
                    item(key = "section_0_foundational_header") {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B7A58)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("section_0_foundational_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Section 0: ${LearnIslamCurriculumData.namazMuhammadiFoundationalSectionTitleEn}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0A4D3C),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = LearnIslamCurriculumData.namazMuhammadiFoundationalSectionTitleUr,
                                        style = CompactArabicUrduTextStyle,
                                        fontFamily = NotoNastaliqUrduFont,
                                        fontSize = 13.sp,
                                        lineHeight = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF073B2E),
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                foundationalSteps.forEach { fStep ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0A4D3C).copy(alpha = 0.2f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("foundational_step_${fStep.stepLabel}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFF0A4D3C)
                                                ) {
                                                    Text(
                                                        text = "Step ${fStep.stepLabel}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(
                                                    text = fStep.titleEn,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF0A4D3C)
                                                )
                                            }
                                            Text(
                                                text = fStep.descriptionEn,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (fStep.additionalReferences.isNotEmpty()) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFFFFF8E1),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.6f)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(8.dp),
                                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                                    ) {
                                                        Text(
                                                            text = "References (International Numbering):",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color(0xFF5D4037)
                                                        )
                                                        fStep.additionalReferences.forEach { addRef ->
                                                            Text(
                                                                text = "• ${addRef.context} — ${addRef.references.joinToString("; ") { it.formatted() }}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = Color(0xFF3E2723)
                                                            )
                                                        }
                                                    }
                                                }
                                            } else {
                                                Text(
                                                    text = "References (International Numbering): ${fStep.references.joinToString("; ") { it.formatted() }}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0A4D3C)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                itemsIndexed(
                    items = displayedAuditedSalahSteps,
                    key = { idx, item -> "complete_salah_audited_${selectedRakatFlowKey}_${selectedRakatNumberKey}_${idx}_${item.id}" }
                ) { _, auditedStep ->
                    AuditedNamazStepCard(
                        step = auditedStep,
                        arabicFontSizeSp = arabicFontSizeSp,
                        showUrdu = showUrdu,
                        showEnglish = showEnglish,
                        showRomanUrdu = showRomanUrdu,
                        isCompleted = completedStepIds.contains(auditedStep.id),
                        isBookmarked = bookmarkedStepIds.contains(auditedStep.id),
                        onToggleCompleted = { toggleStepCompleted(auditedStep.id) },
                        onToggleBookmarked = { toggleStepBookmarked(auditedStep.id) },
                        testTagPrefix = "complete_salah_step"
                    )
                }

                // Daily 5 Prayers Structure Summary Card
                item(key = "complete_salah_daily_structures_card") {
                    NamazDailyPrayerStructuresSection()
                }
            }

            // Closing Advice Card (Sahih al-Bukhari 791)
            item(key = "complete_salah_closing_advice_card") {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF073B2E)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD4AF37)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("complete_salah_closing_advice")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Closing Advice (Prophetic Warning on Completing Ruku & Sujood)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD4AF37),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = closingAdvice.titleUr,
                                style = CompactArabicUrduTextStyle,
                                fontFamily = NotoNastaliqUrduFont,
                                fontSize = 14.sp,
                                lineHeight = 23.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Text(
                            text = closingAdvice.english,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Reference: ${closingAdvice.references.joinToString("; ") { it.formatted() }}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD4AF37)
                        )
                    }
                }
            }
        }

        items(filteredSteps, key = { it.id }) { step ->
            val isStepPlaying = isPlaying && activePlayingId == step.id
            val isStepComingSoon = isComingSoonNotice && activePlayingId == step.id
            val hasExactQuranAudio = step.quranSurahNumber != null
            val hasVerifiedHumanAudio = (!step.humanAudioUrl.isNullOrBlank() || DuaAudioController.hasOfflineQaidaAudioAsset(null, step.id)) && !isStepComingSoon
            var showScholarlyDiff by remember { mutableStateOf(selectedSectionFilter == "SECTARIAN_NAMAZ") }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isStepPlaying) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isStepPlaying) Color(0xFF1B7A58) else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("namaz_step_${step.id}")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF0A4D3C).copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = step.rulingCategory,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0A4D3C),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when {
                                        hasExactQuranAudio || hasVerifiedHumanAudio -> Color(0xFFE8F5E9)
                                        isStepComingSoon -> Color(0xFFFFF8E1)
                                        else -> Color(0xFFF5F5F5)
                                    }
                                ) {
                                    Text(
                                        text = when {
                                            hasExactQuranAudio -> "✓ Exact Quran Verse (${step.quranSurahNumber}:${step.quranAyahRange?.first ?: 1})"
                                            hasVerifiedHumanAudio -> "✓ Authentic Human Audio (Hisn al-Muslim / Masnun Dua)"
                                            else -> "No audio available"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (hasExactQuranAudio || hasVerifiedHumanAudio || isStepComingSoon) FontWeight.Bold else FontWeight.Medium,
                                        color = when {
                                            hasExactQuranAudio || hasVerifiedHumanAudio -> Color(0xFF1B5E20)
                                            isStepComingSoon -> Color(0xFF5D4037)
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                            .testTag("namaz_audio_status_${step.id}")
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = step.titleEnglish,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0A4D3C)
                            )
                            Text(
                                text = step.titleUrdu,
                                style = CompactArabicUrduTextStyle,
                                fontFamily = NotoNastaliqUrduFont,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (hasExactQuranAudio || hasVerifiedHumanAudio) {
                            FilledIconButton(
                                onClick = { onPlayStep(step) },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (isStepPlaying) Color(0xFFD4AF37) else Color(0xFF0A4D3C),
                                    contentColor = if (isStepPlaying) Color(0xFF073B2E) else Color.White
                                ),
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("namaz_play_${step.id}")
                            ) {
                                Icon(
                                    imageVector = if (isStepPlaying) Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = "Play ${step.titleEnglish}",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0A4D3C).copy(alpha = 0.06f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = step.arabicRecitation,
                            style = CompactArabicUrduTextStyle,
                            fontFamily = AmiriQuranFont,
                            fontSize = 21.sp,
                            lineHeight = 33.sp,
                            letterSpacing = 0.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF073B2E),
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = step.transliteration,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1B7A58)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = step.urduMeaning,
                        style = CompactArabicUrduTextStyle,
                        fontFamily = NotoNastaliqUrduFont,
                        fontSize = 13.sp,
                        lineHeight = 21.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = step.englishMeaning,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Method & Posture:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0A4D3C)
                            )
                            Text(
                                text = step.methodExplanationEnglish,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = step.methodExplanationUrdu,
                                style = CompactArabicUrduTextStyle,
                                fontFamily = NotoNastaliqUrduFont,
                                fontSize = 12.sp,
                                lineHeight = 19.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Evidence & Reference: ${step.hadithOrFiqhReference}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A4D3C)
                    )

                    if (step.scholarlyDifferences.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            onClick = { showScholarlyDiff = !showScholarlyDiff },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF8E1),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Scholarly & Madhhab Positions (${step.scholarlyDifferences.size} Schools)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF5D4037)
                                    )
                                    Icon(
                                        imageVector = if (showScholarlyDiff) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Toggle Scholarly Views",
                                        tint = Color(0xFF5D4037)
                                    )
                                }
                                AnimatedVisibility(visible = showScholarlyDiff) {
                                    Column(
                                        modifier = Modifier.padding(top = 6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        step.scholarlyDifferences.forEach { diff ->
                                            Column {
                                                Text(
                                                    text = "• ${diff.schoolOrTradition}: ${diff.positionSummaryEnglish}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF3E2723)
                                                )
                                                Text(
                                                    text = diff.positionSummaryUrdu,
                                                    style = CompactArabicUrduTextStyle,
                                                    fontFamily = NotoNastaliqUrduFont,
                                                    fontSize = 12.sp,
                                                    lineHeight = 19.sp,
                                                    color = Color(0xFF4E342E),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.End
                                                )
                                                Text(
                                                    text = "Source: ${diff.primarySourceReference}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFF6D4C41),
                                                    fontWeight = FontWeight.SemiBold
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
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NamazJsonStepCard(
    step: NamazStepJsonEntry,
    testTagPrefix: String = "namaz_json_step"
) {
    var selectedProfile by remember { mutableStateOf(PlaybackDataProfile.BALANCED) }
    var wifiOnly by remember { mutableStateOf(false) }

    val embeddedLectureRecord = remember(step.videoYoutubeId, step.videoTitle) {
        val ytId = step.videoYoutubeId
        if (!ytId.isNullOrBlank()) {
            LectureRecord(
                youtubeId = ytId,
                title = step.videoTitle ?: "Nimaz ka tareqa",
                verifiedTitle = "${step.videoTitle ?: "Nimaz ka tareqa"} — Practical Demonstration (Engineer Muhammad Ali Mirza)",
                speaker = "Engineer Muhammad Ali Mirza",
                channelHandle = "@NOOR-e-HIDAYAT-EMAM",
                channelUrl = "https://www.youtube.com/@NOOR-e-HIDAYAT-EMAM",
                sourceTier = SourceTier.FAN_CHANNEL,
                countryStatus = CountryStatus.PLAYABLE_GLOBAL,
                verificationState = RecordVerificationState.CONFIRMED_LIVE,
                recordFormat = LectureFormat.CLIP,
                lectureNumber = 70,
                seriesName = "Practical Salah & Sunnah Series",
                recordedYear = 2023,
                durationSec = 580,
                topics = listOf("salah_azan", "hadith_sciences"),
                relatedYoutubeIds = emptyList(),
                summary = "Practical step-by-step demonstration of Namaz-e-Muhammadi (${step.videoLink.orEmpty()}).",
                transcriptStatus = TranscriptStatus.NONE,
                referenceStatus = ReferenceStatus.VERIFIED,
                references = step.reference.split(";").map { it.trim() }.filter { it.isNotEmpty() },
                alternateYoutubeIds = emptyList()
            )
        } else {
            null
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            width = if (embeddedLectureRecord != null) 1.5.dp else 1.dp,
            color = if (embeddedLectureRecord != null) Color(0xFFD4AF37) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${testTagPrefix}_${step.step_no}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Embedded video in Ruku ("Nimaz ka tareqa") at the very top with its Hadith references directly below it
            if (embeddedLectureRecord != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF073B2E).copy(alpha = 0.06f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0A4D3C).copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("step_10_embedded_video_container")
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
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
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Embedded Video",
                                    tint = Color(0xFF0A4D3C),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = step.videoTitle ?: "Nimaz ka tareqa",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0A4D3C),
                                    modifier = Modifier.testTag("step_10_video_title")
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFD4AF37).copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "Video Library • ${embeddedLectureRecord.youtubeId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF073B2E),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        YtEmbedHost(
                            lecture = embeddedLectureRecord,
                            allIndexedLectures = listOf(embeddedLectureRecord),
                            selectedProfile = selectedProfile,
                            wifiOnlyEnabled = wifiOnly,
                            onSelectProfile = { selectedProfile = it },
                            onToggleWifiOnly = { wifiOnly = it },
                            onWatchSecondsRecorded = { _, _, _ -> },
                            onSelectAlternateLecture = {},
                            onReportBroken = {}
                        )

                        // Directly below the video, show the references for the step
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE8F5E9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B7A58)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("step_10_below_video_references")
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    text = "Step ${step.step_no} Hadith References (International Numbering):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0A4D3C)
                                )
                                Text(
                                    text = if (step.primaryReferences.isNotEmpty()) {
                                        step.primaryReferences.joinToString("; ") { it.formatted() }
                                    } else {
                                        step.reference
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF073B2E)
                                )
                            }
                        }
                    }
                }
            }

            // Step number, section badge (English + Urdu), and title (English + Urdu)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0A4D3C)
                    ) {
                        Text(
                            text = "Step ${step.step_no}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFD4AF37).copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = if (step.sectionUrdu.isNotBlank()) "${step.section} • ${step.sectionUrdu}" else step.section,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF073B2E),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0A4D3C),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (step.titleUrdu.isNotBlank()) {
                        Text(
                            text = step.titleUrdu,
                            style = CompactArabicUrduTextStyle,
                            fontFamily = NotoNastaliqUrduFont,
                            fontSize = 14.sp,
                            lineHeight = 23.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF073B2E),
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // (1) Arabic
            if (step.arabic.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0A4D3C).copy(alpha = 0.06f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(
                            text = "1. Arabic (عربي)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B7A58)
                        )
                        Text(
                            text = step.arabic,
                            style = CompactArabicUrduTextStyle,
                            fontFamily = AmiriQuranFont,
                            fontSize = 22.sp,
                            lineHeight = 35.sp,
                            letterSpacing = 0.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF073B2E),
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // (2) Urdu (Right-to-Left)
            if (step.urdu.isNotBlank()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "2. Urdu (اردو)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B7A58)
                    )
                    Text(
                        text = step.urdu,
                        style = CompactArabicUrduTextStyle,
                        fontFamily = NotoNastaliqUrduFont,
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // (3) English
            if (step.english.isNotBlank()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "3. English",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B7A58)
                    )
                    Text(
                        text = step.english,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Multiple Masnoon Options (e.g., Step 12 Ruku Tasbeehat & Step 19 Sujood Tasbeehat)
            if (step.options.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE8F5E9).copy(alpha = 0.7f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B7A58).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("step_${step.step_no}_options_box")
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Authentic Masnoon Options (${step.options.size} Choices — Recite Any, At Least 3 Times):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0A4D3C)
                        )
                        if (step.generalReferences.isNotEmpty()) {
                            Text(
                                text = "General Reference: ${step.generalReferences.joinToString("; ") { it.formatted() }}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF073B2E)
                            )
                        }
                        step.options.forEachIndexed { idx, opt ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0A4D3C).copy(alpha = 0.15f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = "Option ${idx + 1}:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0A4D3C)
                                    )
                                    Text(
                                        text = opt.arabic,
                                        style = CompactArabicUrduTextStyle,
                                        fontFamily = AmiriQuranFont,
                                        fontSize = 20.sp,
                                        lineHeight = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF073B2E),
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    if (opt.urdu.isNotBlank()) {
                                        Text(
                                            text = opt.urdu,
                                            style = CompactArabicUrduTextStyle,
                                            fontFamily = NotoNastaliqUrduFont,
                                            fontSize = 14.sp,
                                            lineHeight = 22.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    if (opt.english.isNotBlank()) {
                                        Text(
                                            text = opt.english,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "References: ${opt.references.joinToString("; ") { it.formatted() }}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0A4D3C)
                                    )
                                    if (!opt.note.isNullOrBlank()) {
                                        Text(
                                            text = "Note: ${opt.note}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF5D4037)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Alternative Masnoon Supplication (e.g., Step 3 Thana, Step 4 Ta'awwudh, Step 21 Jalsah)
            step.alternative?.let { alt ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0A4D3C).copy(alpha = 0.05f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.7f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("step_${step.step_no}_alternative_box")
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Alternative Authentic Supplication (متبادل مسنون دعا):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0A4D3C)
                        )
                        Text(
                            text = alt.arabic,
                            style = CompactArabicUrduTextStyle,
                            fontFamily = AmiriQuranFont,
                            fontSize = 20.sp,
                            lineHeight = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF073B2E),
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (alt.urdu.isNotBlank()) {
                            Text(
                                text = alt.urdu,
                                style = CompactArabicUrduTextStyle,
                                fontFamily = NotoNastaliqUrduFont,
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (alt.english.isNotBlank()) {
                            Text(
                                text = alt.english,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Alternative References: ${alt.references.joinToString("; ") { it.formatted() }}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0A4D3C)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

            // Hadith References: When additionalReferences are present, they replace the green reference line above
            if (step.additionalReferences.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF8E1),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("step_${step.step_no}_additional_refs")
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "References (International Numbering):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF5D4037)
                        )
                        step.additionalReferences.forEach { addRef ->
                            Text(
                                text = "• ${addRef.context} — ${addRef.references.joinToString("; ") { it.formatted() }}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF3E2723)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = if (step.primaryReferences.isNotEmpty()) {
                        "References (International Numbering): ${step.primaryReferences.joinToString("; ") { it.formatted() }}"
                    } else {
                        "Reference: ${step.reference}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0A4D3C)
                )
            }

            // Step-specific Notes (e.g., Step 18 men & women pray the same way per Bukhari 631; Step 24 finger pointing)
            if (step.notes.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE8F5E9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B7A58).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("step_${step.step_no}_notes")
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Important Prophetic Sunnah Note:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0A4D3C)
                        )
                        step.notes.forEach { noteText ->
                            Text(
                                text = "• $noteText",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF073B2E)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NimazJanazaSectionContent() {
    var selectedSubSection by remember { mutableStateOf(NimazJanazaSubSection.ADULT) }
    val janazaSteps = remember(selectedSubSection) {
        LearnIslamCurriculumData.getNimazJanazaSteps(selectedSubSection)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("nimaz_janaza_lazy_column"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Prominent standing-only rule & heading card
        item(key = "nimaz_janaza_standing_rule_banner") {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A4D3C)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("nimaz_janaza_standing_rule_card")
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
                        Text(
                            text = "Nimaz Janaza (Funeral Prayer)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "نمازِ جنازہ",
                            style = CompactArabicUrduTextStyle,
                            fontFamily = NotoNastaliqUrduFont,
                            fontSize = 16.sp,
                            lineHeight = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFF8E1),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD4AF37)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("nimaz_janaza_no_ruku_no_sajdah_banner")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "IMPORTANT RULE: Janaza prayer is offered standing only. There is NO ruku and NO sajdah.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF073B2E)
                            )
                            Text(
                                text = "اہم شرعی اصول: نمازِ جنازہ صرف کھڑے ہو کر پڑھی جاتی ہے، اس میں رکوع اور سجدہ نہیں ہے۔",
                                style = CompactArabicUrduTextStyle,
                                fontFamily = NotoNastaliqUrduFont,
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF073B2E),
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Three Sub-sections: 1. Adult (بالغ), 2. Baby Boy (بچہ), 3. Baby Girl (بچی)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NimazJanazaSubSection.entries.forEach { sub ->
                            val isSelected = selectedSubSection == sub
                            Surface(
                                onClick = { selectedSubSection = sub },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.14f),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("nimaz_janaza_sub_${sub.key}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = sub.displayTitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) Color(0xFF073B2E) else Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        itemsIndexed(
            items = janazaSteps,
            key = { _, step -> "janaza_${selectedSubSection.key}_step_${step.step_no}" }
        ) { _, step ->
            NamazJsonStepCard(
                step = step,
                testTagPrefix = "nimaz_janaza_${selectedSubSection.key}_step"
            )
        }
    }
}

@Composable
private fun DailyDuasSequenceSectionContent(
    duas: List<DailyDuaSequenceEntry>,
    activePlayingId: String?,
    isPlaying: Boolean,
    isComingSoonNotice: Boolean = false,
    onPlayDua: (DailyDuaSequenceEntry) -> Unit
) {
    val sortedDuas = remember(duas) {
        duas.sortedBy { entry ->
            val hasAudio = entry.quranSurahAyah != null || !entry.humanAudioUrl.isNullOrBlank()
            if (hasAudio) 0 else 1
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("daily_duas_seq_lazy_column"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A4D3C))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Daily Masnoon Duas in Chronological Sequence (Hisn al-Muslim & Sunnah.com)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "صبح جاگنے سے رات سونے تک کی مستند مسنون دعائیں (بمعہ افطار کی مستند دعا — سنن ابی داؤد ۲۳۵۷)",
                        style = CompactArabicUrduTextStyle,
                        fontFamily = NotoNastaliqUrduFont,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFFD4AF37),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        items(sortedDuas, key = { it.duaId }) { entry ->
            val isEntryPlaying = isPlaying && activePlayingId == entry.duaId
            val isEntryComingSoon = isComingSoonNotice && activePlayingId == entry.duaId
            val hasExactQuranAudio = entry.quranSurahAyah != null
            val hasVerifiedHumanAudio = !entry.humanAudioUrl.isNullOrBlank() && !isEntryComingSoon
            val isIftarDua = entry.duaId == "dua_ramadan_iftar"
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isEntryPlaying -> Color(0xFFE8F5E9)
                        isIftarDua -> Color(0xFFFFFBEB)
                        else -> MaterialTheme.colorScheme.surface
                    }
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isIftarDua || isEntryPlaying) 2.dp else 1.dp,
                    color = when {
                        isEntryPlaying -> Color(0xFF1B7A58)
                        isIftarDua -> Color(0xFFD4AF37)
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("seq_dua_card_${entry.duaId}")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.timeOfDayStage,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B7A58)
                            )
                            Text(
                                text = entry.titleEnglish,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0A4D3C)
                            )
                            Text(
                                text = entry.titleUrdu,
                                style = CompactArabicUrduTextStyle,
                                fontFamily = NotoNastaliqUrduFont,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when {
                                    hasExactQuranAudio || hasVerifiedHumanAudio -> Color(0xFFE8F5E9)
                                    isEntryComingSoon -> Color(0xFFFFF8E1)
                                    else -> Color(0xFFF5F5F5)
                                }
                            ) {
                                Text(
                                    text = when {
                                        hasExactQuranAudio -> "✓ Exact Quran Verse (${entry.quranSurahAyah?.first}:${entry.quranSurahAyah?.second})"
                                        hasVerifiedHumanAudio -> "✓ Authentic Human Audio (Hisn al-Muslim)"
                                        else -> "No audio available"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (hasExactQuranAudio || hasVerifiedHumanAudio || isEntryComingSoon) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        hasExactQuranAudio || hasVerifiedHumanAudio -> Color(0xFF1B5E20)
                                        isEntryComingSoon -> Color(0xFF5D4037)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                        .testTag("seq_dua_audio_status_${entry.duaId}")
                                )
                            }
                        }
                        if (hasExactQuranAudio || hasVerifiedHumanAudio) {
                            FilledIconButton(
                                onClick = { onPlayDua(entry) },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (isEntryPlaying) Color(0xFFD4AF37) else Color(0xFF0A4D3C),
                                    contentColor = if (isEntryPlaying) Color(0xFF073B2E) else Color.White
                                ),
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("seq_dua_play_${entry.duaId}")
                            ) {
                                Icon(
                                    imageVector = if (isEntryPlaying) Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = "Play ${entry.titleEnglish}",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0A4D3C).copy(alpha = 0.06f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = entry.arabicText,
                            style = CompactArabicUrduTextStyle,
                            fontFamily = AmiriQuranFont,
                            fontSize = 22.sp,
                            lineHeight = 34.sp,
                            letterSpacing = 0.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF073B2E),
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = entry.transliteration,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B7A58)
                    )
                    if (entry.AlternateTransliteration.isNotBlank()) {
                        Text(
                            text = "Also written: ${entry.AlternateTransliteration}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entry.urduTranslation,
                        style = CompactArabicUrduTextStyle,
                        fontFamily = NotoNastaliqUrduFont,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = entry.englishTranslation,
                        style = MaterialTheme.typography.bodySmall
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    Text(
                        text = "Reference: ${entry.authenticReference}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A4D3C)
                    )
                    Text(
                        text = entry.verificationNote,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SehriNiyatDedicatedSectionCard(
    guidance: SehriNiyatGuidanceData = LearnIslamCurriculumData.sehriNiyatGuidance,
    activePlayingId: String? = null,
    isPlaying: Boolean = false,
    onPlayFormula: (CustomaryNiyatFormula) -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD4AF37)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sehri_niyat_section_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sehri & Fasting Niyat — Authentic Prophetic Hadith (1st Priority)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0A4D3C)
                    )
                    Text(
                        text = guidance.titleUrdu,
                        style = CompactArabicUrduTextStyle,
                        fontFamily = NotoNastaliqUrduFont,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF1B7A58)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 1ST PRIORITY: Authentic Prophetic Hadiths on Sehri & Fasting Intention
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFE8F5E9),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF1B7A58)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "1st Priority — Authentic Prophetic Hadiths (Sunan an-Nasa'i 2331 • Sahih al-Bukhari 1 & 1923):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0A4D3C)
                    )
                    Text(
                        text = "«مَنْ لَمْ يُبَيِّتِ الصِّيَامَ قَبْلَ الْفَجْرِ فَلَا صِيَامَ لَهُ» ۝ «تَسَحَّرُوا فَإِنَّ فِي السَّحُورِ بَرَكَةً» ۝ «إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ»",
                        style = CompactArabicUrduTextStyle,
                        fontFamily = AmiriQuranFont,
                        fontSize = 20.sp,
                        lineHeight = 31.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF073B2E),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                    guidance.propheticHadithReferences.forEach { ref ->
                        Text(
                            text = "• $ref",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF073B2E)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = guidance.heartIntentionPrincipleEnglish,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF073B2E)
                    )
                    Text(
                        text = guidance.heartIntentionPrincipleUrdu,
                        style = CompactArabicUrduTextStyle,
                        fontFamily = NotoNastaliqUrduFont,
                        fontSize = 12.sp,
                        lineHeight = 19.sp,
                        color = Color(0xFF073B2E),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Note on Customary Verbal Formulas (Educational Aids — NOT Prophetic Hadiths):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037)
            )
            Spacer(modifier = Modifier.height(4.dp))

            guidance.customaryFormulas.forEach { formula ->
                val isFormulaPlaying = isPlaying && activePlayingId == formula.id
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFF8E1),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .testTag("sehri_niyat_formula_${formula.id}")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF5D4037)
                            ) {
                                Text(
                                    text = formula.classificationBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF5F5F5)
                            ) {
                                Text(
                                    text = "No audio available",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = formula.labelEnglish,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3E2723)
                        )
                        Text(
                            text = formula.arabicText,
                            style = CompactArabicUrduTextStyle,
                            fontFamily = AmiriQuranFont,
                            fontSize = 20.sp,
                            lineHeight = 31.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF073B2E),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = formula.transliteration,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1B7A58)
                        )
                        Text(
                            text = formula.urduTranslation,
                            style = CompactArabicUrduTextStyle,
                            fontFamily = NotoNastaliqUrduFont,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = formula.englishTranslation,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formula.attributionAndStatusNote,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF5D4037),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SehriNiyatAndSourcesSectionContent(
    guidance: SehriNiyatGuidanceData,
    sources: List<VerifiedLearningSourceInfo>,
    activePlayingId: String?,
    isPlaying: Boolean,
    onPlayFormula: (CustomaryNiyatFormula) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("sehri_niyat_sources_lazy_column"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SehriNiyatDedicatedSectionCard(
                guidance = guidance,
                activePlayingId = activePlayingId,
                isPlaying = isPlaying,
                onPlayFormula = onPlayFormula
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Curriculum & Scholarly Attribution",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A4D3C)
                    )
                    Text(
                        text = "Noorani Qaida lessons follow the classical 17-lesson primer of Sheikh Noor Muhammad Haqqani Ludhianvi (1856–1925 CE). Daily Adhkar are cited from Hisn al-Muslim (Sa'id ibn 'Ali ibn Wahf al-Qahtani) and Kutub al-Sittah. Technical API & licence documentation is consolidated under About → Developer Options → API Sources & Licences.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
