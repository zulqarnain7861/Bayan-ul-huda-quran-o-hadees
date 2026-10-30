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
import com.example.ui.audio.DuaAudioController
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
    NAMAZ_AND_WUDU("namaz", "Namaz & Wudu", "وضو اور نمازِ مترجم"),
    DAILY_DUAS_SEQ("duas_seq", "Daily Duas", "مسنون دعائیں (ترتیب وار)"),
    SEHRI_NIYAT_SOURCES("sehri_niyat", "Sehri Niyat & Fiqh", "سحری کی نیت اور فقہی رہنمائی")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnIslamScreen(
    duaAudioController: DuaAudioController,
    initialTab: LearnIslamTab = LearnIslamTab.NOORANI_QAIDA,
    onBack: () -> Unit = {},
    onOpenQuranSurah: (Int, Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("bayan_learn_islam_prefs", Context.MODE_PRIVATE)
    }

    var selectedTab by remember { mutableStateOf(initialTab) }
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
                            text = "Noorani Qaida • 6 Kalimas & Iman • Wudu & Namaz • Daily Duas",
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
                        }
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
                            val exactQuranicRef = if (step.section == "NAMAZ_MUTARJAM" || step.section == "SHORT_SURAHS") {
                                step.quranSurahNumber?.let { Pair(it, step.quranAyahRange?.first ?: 1) }
                            } else {
                                null
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
                            "Offline Authentic Noorani Qaida, Quran & Sunnah Audio (No AI Voice)"
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
    onPlayItem: (QaidaPracticeItem) -> Unit
) {
    val currentLesson = lessons.getOrElse(selectedLessonIndex) { lessons.first() }
    val isLessonCompleted = completedLessons.contains(currentLesson.id)
    val selectedQuizAnswers = remember(currentLesson.id) { mutableStateMapOf<String, Int>() }
    val itemSelfCheckFeedback = remember(currentLesson.id) { mutableStateMapOf<String, Boolean>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("noorani_qaida_lazy_column"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentLesson.titleUrdu,
                            style = CompactArabicUrduTextStyle,
                            fontFamily = NotoNastaliqUrduFont,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentLesson.titleArabic,
                            style = CompactArabicUrduTextStyle,
                            fontFamily = AmiriQuranFont,
                            fontSize = 20.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37),
                            textAlign = TextAlign.End
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

        items(items, key = { it.id }) { item ->
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
                                        "Real voice coming soon, In Sha Allah."
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
    var selectedSectionFilter by remember { mutableStateOf("ALL") }
    val filteredSteps = remember(steps, selectedSectionFilter) {
        if (selectedSectionFilter == "ALL") steps else steps.filter { it.section == selectedSectionFilter }
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
                    "ALL" to "All Steps (${steps.size})",
                    "WUDU" to "Wudu (Ablution)",
                    "SALAH_PREREQUISITES" to "Prerequisites & Rak'at",
                    "NAMAZ_MUTARJAM" to "Complete Salah & Janaza",
                    "SHORT_SURAHS" to "Short Surahs"
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

        items(filteredSteps, key = { it.id }) { step ->
            val isStepPlaying = isPlaying && activePlayingId == step.id
            val isStepComingSoon = isComingSoonNotice && activePlayingId == step.id
            val hasExactQuranAudio = step.quranSurahNumber != null &&
                (step.section == "NAMAZ_MUTARJAM" || step.section == "SHORT_SURAHS")
            val hasVerifiedHumanAudio = (!step.humanAudioUrl.isNullOrBlank() || DuaAudioController.hasOfflineQaidaAudioAsset(null, step.id)) && !isStepComingSoon
            var showScholarlyDiff by remember { mutableStateOf(false) }

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
                                            hasVerifiedHumanAudio -> "✓ Authentic Human Audio (Hisn al-Muslim)"
                                            else -> "Real voice coming soon, In Sha Allah."
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

@Composable
private fun DailyDuasSequenceSectionContent(
    duas: List<DailyDuaSequenceEntry>,
    activePlayingId: String?,
    isPlaying: Boolean,
    isComingSoonNotice: Boolean = false,
    onPlayDua: (DailyDuaSequenceEntry) -> Unit
) {
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

        items(duas, key = { it.duaId }) { entry ->
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
                                        else -> "Real voice coming soon, In Sha Allah."
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
                            IconButton(
                                onClick = { onPlayFormula(formula) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFormulaPlaying) Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = "Play ${formula.labelEnglish}",
                                    tint = Color(0xFF0A4D3C)
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
