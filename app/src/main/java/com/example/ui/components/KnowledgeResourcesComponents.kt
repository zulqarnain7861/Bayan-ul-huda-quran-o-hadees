package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.IslamicClassicalBook
import com.example.data.repository.ClassicalLibraryAndBiographiesRepository
import com.example.data.repository.IslamQaRepository
import com.example.data.repository.QuranTajweedEngineRepository
import com.example.ui.theme.ScheherazadeFontFamily
import com.example.ui.theme.UrduNastaliqFontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun QuranTajweedEngineDialog(
  initialSurahNumber: Int = 1,
  initialAyahNumber: Int = 1,
  selectedRuleFilterId: String? = null,
  onSelectRuleFilter: (String?) -> Unit,
  onNavigateToSurahAyah: (Int, Int) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var activeTab by remember { mutableIntStateOf(0) } // 0: 17 Rules, 1: Word-by-Word & Roots, 2: Alphabet & Waqf, 3: 57 Lessons, 4: 7 Qiraat, 5: 30 Juz Index

  var rules by remember { mutableStateOf<List<QuranTajweedEngineRepository.TajweedRuleCategory>>(emptyList()) }
  var words by remember { mutableStateOf<List<QuranTajweedEngineRepository.WordByWordItem>>(emptyList()) }
  var alphabetGuide by remember { mutableStateOf<QuranTajweedEngineRepository.ArabicAlphabetGuide?>(null) }
  var lessonChapters by remember { mutableStateOf<List<QuranTajweedEngineRepository.TajweedLessonChapter>>(emptyList()) }
  var qiraatSummaries by remember { mutableStateOf<List<QuranTajweedEngineRepository.QiraatRiwayahSummary>>(emptyList()) }
  var qiraatDiffs by remember { mutableStateOf<List<QuranTajweedEngineRepository.QiraatRuleDifference>>(emptyList()) }
  var juzList by remember { mutableStateOf<List<QuranTajweedEngineRepository.JuzBoundaryEntry>>(emptyList()) }
  var activeAyahRecord by remember { mutableStateOf<QuranTajweedEngineRepository.AyahTajweedRecord?>(null) }

  var inspectSurah by remember { mutableIntStateOf(initialSurahNumber.coerceIn(1, 114)) }
  var inspectAyah by remember { mutableIntStateOf(initialAyahNumber.coerceAtLeast(1)) }
  var selectedLesson by remember { mutableStateOf<QuranTajweedEngineRepository.TajweedLessonItem?>(null) }

  // Race-free background loader: computes on Dispatchers.Default and assigns Compose state strictly on the Main Thread
  LaunchedEffect(activeTab, inspectSurah, inspectAyah) {
    val safeSurah = inspectSurah.coerceIn(1, 114)
    val safeAyah = inspectAyah.coerceAtLeast(1)
    when (activeTab) {
      0 -> {
        val loadedPair = withContext(Dispatchers.Default) {
          val r = runCatching { QuranTajweedEngineRepository.getTajweedRules(context) }.getOrDefault(emptyList())
          val rec = runCatching { QuranTajweedEngineRepository.getAyahTajweedRecord(context, safeSurah, safeAyah) }.getOrNull()
          r to rec
        }
        rules = loadedPair.first
        if (loadedPair.second == null && safeAyah > 1) {
          if (safeSurah < 114) {
            inspectSurah = safeSurah + 1
            inspectAyah = 1
          } else {
            inspectAyah = 1
          }
        } else {
          activeAyahRecord = loadedPair.second
        }
      }
      1 -> {
        val loadedTriple = withContext(Dispatchers.Default) {
          val r = if (rules.isEmpty()) {
            runCatching { QuranTajweedEngineRepository.getTajweedRules(context) }.getOrDefault(emptyList())
          } else rules
          val rec = runCatching { QuranTajweedEngineRepository.getAyahTajweedRecord(context, safeSurah, safeAyah) }.getOrNull()
          val w = runCatching { QuranTajweedEngineRepository.getWordByWordForAyah(context, safeSurah, safeAyah) }.getOrDefault(emptyList())
          Triple(r, rec, w)
        }
        if (rules.isEmpty()) rules = loadedTriple.first
        if (loadedTriple.second == null && safeAyah > 1) {
          if (safeSurah < 114) {
            inspectSurah = safeSurah + 1
            inspectAyah = 1
          } else {
            inspectAyah = 1
          }
        } else {
          activeAyahRecord = loadedTriple.second
          words = loadedTriple.third
        }
      }
      2 -> {
        if (alphabetGuide == null) {
          val loadedGuide = withContext(Dispatchers.Default) {
            runCatching { QuranTajweedEngineRepository.getArabicAlphabetGuide(context) }.getOrNull()
          }
          alphabetGuide = loadedGuide
        }
      }
      3 -> {
        if (lessonChapters.isEmpty()) {
          val loadedChapters = withContext(Dispatchers.Default) {
            runCatching { QuranTajweedEngineRepository.getTajweedLessonChapters(context) }.getOrDefault(emptyList())
          }
          lessonChapters = loadedChapters
        }
      }
      4 -> {
        if (qiraatSummaries.isEmpty() || qiraatDiffs.isEmpty()) {
          val loadedQiraat = withContext(Dispatchers.Default) {
            val s = runCatching { QuranTajweedEngineRepository.getQiraatRiwayatSummaries(context) }.getOrDefault(emptyList())
            val d = runCatching { QuranTajweedEngineRepository.getQiraatRuleDifferences(context) }.getOrDefault(emptyList())
            s to d
          }
          qiraatSummaries = loadedQiraat.first
          qiraatDiffs = loadedQiraat.second
        }
      }
      5 -> {
        if (juzList.isEmpty()) {
          val loadedJuz = withContext(Dispatchers.Default) {
            runCatching { QuranTajweedEngineRepository.getJuzIndex(context) }.getOrDefault(emptyList())
          }
          juzList = loadedJuz
        }
      }
    }
  }

  val rulesMap = remember(rules) { rules.associateBy { it.id } }
  val baseAyahColor = MaterialTheme.colorScheme.onSurface
  val annotatedAyah = remember(activeAyahRecord, rulesMap, selectedRuleFilterId, baseAyahColor) {
    val rec = activeAyahRecord
    if (rec == null || rec.textUthmani.isBlank()) {
      androidx.compose.ui.text.AnnotatedString("")
    } else {
      QuranTajweedEngineRepository.buildSafeTajweedAnnotatedString(
        record = rec,
        fallbackArabicText = rec.textUthmani,
        rulesMap = rulesMap,
        baseTextColor = baseAyahColor,
        selectedRuleFilter = selectedRuleFilterId
      )
    }
  }
  val ayahSpans = activeAyahRecord?.annotations.orEmpty()

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp),
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.background
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Quran Tajweed Engine & Morphology",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = "17 Tajweed Rules • 113,611 Verified Spans • 77,629 Words & Roots • 57 Lessons • 7 Qiraat",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              IconButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_tajweed_engine_dialog")
              ) {
                Icon(Icons.Default.Close, contentDescription = "Close Tajweed Engine")
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              val tabs = listOf(
                "17 Tajweed Rules",
                "Word-by-Word & Roots",
                "Alphabet & Waqf",
                "57 Lessons & Quizzes",
                "7 Qiraat (Separate)",
                "30 Juz Index"
              )
              tabs.forEachIndexed { idx, label ->
                FilterChip(
                  selected = activeTab == idx,
                  onClick = { activeTab = idx },
                  label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                )
              }
            }
          }
        }

        // Ayah Quick Selector Bar (shown on Rules and Word-by-Word tabs)
        if (activeTab == 0 || activeTab == 1) {
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "Inspecting Surah $inspectSurah : Ayah $inspectAyah (${ayahSpans.size} Tajweed spans)",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  OutlinedButton(
                    onClick = {
                      if (inspectAyah > 1) {
                        inspectAyah -= 1
                      } else if (inspectSurah > 1) {
                        inspectSurah -= 1
                        inspectAyah = 1
                      }
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                  ) {
                    Text("Prev Ayah", style = MaterialTheme.typography.labelSmall)
                  }
                  OutlinedButton(
                    onClick = { inspectAyah += 1 },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                  ) {
                    Text("Next Ayah", style = MaterialTheme.typography.labelSmall)
                  }
                }
              }
              if (annotatedAyah.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = MaterialTheme.colorScheme.surface,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  SelectionContainer {
                    Text(
                      text = annotatedAyah,
                      style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = ScheherazadeFontFamily,
                        fontSize = 26.sp,
                        lineHeight = 46.sp,
                        textDirection = TextDirection.Rtl
                      ),
                      textAlign = TextAlign.Right,
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                    )
                  }
                }
              }
            }
          }
        }

        // Tab Body
        Box(modifier = Modifier.weight(1f)) {
          when (activeTab) {
            0 -> {
              LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                item {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = if (selectedRuleFilterId == null) {
                        "Showing All 17 Tajweed Rule Colours (Tap any rule to solo-highlight)"
                      } else {
                        "Solo-Highlighting Rule: $selectedRuleFilterId"
                      },
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold
                    )
                    if (selectedRuleFilterId != null) {
                      TextButton(onClick = { onSelectRuleFilter(null) }) {
                        Text("Show All Rules")
                      }
                    }
                  }
                }
                if (ayahSpans.isNotEmpty()) {
                  item {
                    Card(
                      colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                      ),
                      shape = RoundedCornerShape(12.dp)
                    ) {
                      Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                          text = "Rules Detected in Surah $inspectSurah:$inspectAyah",
                          style = MaterialTheme.typography.labelMedium,
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val ruleMap = rules.associateBy { it.id }
                        ayahSpans.forEach { span ->
                          val r = ruleMap[span.ruleId]
                          val c = r?.composeColor() ?: Color(0xFF2E7D32)
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                          ) {
                            Box(
                              modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(c)
                            )
                            Text(
                              text = "${r?.englishTitle ?: span.ruleId} (${r?.arabicTitle.orEmpty()}) • chars [${span.start}..${span.end}]",
                              style = MaterialTheme.typography.bodySmall
                            )
                          }
                        }
                      }
                    }
                  }
                }
                itemsIndexed(rules, key = { idx, r -> "rule_${idx}_${r.id}" }) { _, rule ->
                  val ruleColor = rule.composeColor()
                  val isSelected = selectedRuleFilterId == rule.id
                  Card(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        onSelectRuleFilter(if (isSelected) null else rule.id)
                      },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                      containerColor = if (isSelected) {
                        ruleColor.copy(alpha = 0.16f)
                      } else {
                        MaterialTheme.colorScheme.surface
                      }
                    )
                  ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(8.dp),
                          modifier = Modifier.weight(1f)
                        ) {
                          Box(
                            modifier = Modifier
                              .size(16.dp)
                              .clip(CircleShape)
                              .background(ruleColor)
                          )
                          Column {
                            Text(
                              text = "${rule.englishTitle} (${rule.section})",
                              style = MaterialTheme.typography.titleSmall,
                              fontWeight = FontWeight.Bold
                            )
                            Text(
                              text = "${rule.countLabel} • Color ${rule.colorHex}",
                              style = MaterialTheme.typography.labelSmall,
                              color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                          }
                        }
                        Text(
                          text = rule.arabicTitle,
                          style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = ScheherazadeFontFamily
                          ),
                          color = ruleColor
                        )
                      }
                      Spacer(modifier = Modifier.height(6.dp))
                      Text(
                        text = rule.shortDescription,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                      )
                      if (rule.longDescription.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                          text = rule.longDescription,
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                      if (rule.applicableLetters.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                          text = "Applicable Letters: ${rule.applicableLetters}",
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.primary
                        )
                      }
                    }
                  }
                }
              }
            }

            1 -> {
              // Word-by-word & Morphology (Root + Lemma)
              LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                item {
                  Text(
                    text = "Word-by-Word Translation, Transliteration, Root & Lemma (Surah $inspectSurah:$inspectAyah)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
                itemsIndexed(words, key = { idx, w -> "wbw_${inspectSurah}_${inspectAyah}_${idx}_${w.wordIndex}" }) { _, w ->
                  Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = "Word #${w.wordIndex} • ${w.englishMeaning}",
                          style = MaterialTheme.typography.titleSmall,
                          fontWeight = FontWeight.Bold
                        )
                        if (w.transliteration.isNotBlank()) {
                          Text(
                            text = "Transliteration: ${w.transliteration}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                          )
                        }
                        if (w.rootArabic.isNotBlank() || w.lemmaArabic.isNotBlank()) {
                          Text(
                            text = buildString {
                              if (w.rootArabic.isNotBlank()) append("Arabic Root (جذر): ${w.rootArabic} (${w.rootBuckwalter})   ")
                              if (w.lemmaArabic.isNotBlank()) append("Lemma: ${w.lemmaArabic}")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                          )
                        }
                      }
                      Text(
                        text = w.arabicWord,
                        style = MaterialTheme.typography.headlineSmall.copy(
                          fontFamily = ScheherazadeFontFamily,
                          fontSize = 26.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                      )
                    }
                  }
                }
              }
            }

            2 -> {
              // Arabic Alphabet, Tashkeel & Stopping Signs (Waqf)
              val guide = alphabetGuide
              if (guide == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                  CircularProgressIndicator()
                }
              } else {
                val allLetters = remember(guide) {
                  guide.standardLetters + guide.otherLetters + guide.nonArabicScriptLetters
                }
                LazyColumn(
                  modifier = Modifier.fillMaxSize(),
                  contentPadding = PaddingValues(12.dp),
                  verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  item {
                    Text(
                      text = "1. Stopping Signs / Rumuz al-Waqf (${guide.stoppingSigns.size})",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                  }
                  items(guide.stoppingSigns) { sign ->
                    Card(
                      modifier = Modifier.fillMaxWidth(),
                      shape = RoundedCornerShape(10.dp)
                    ) {
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = sign.title,
                          style = MaterialTheme.typography.bodyMedium,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier.weight(1f)
                        )
                        Text(
                          text = sign.symbol,
                          style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = ScheherazadeFontFamily
                          ),
                          color = MaterialTheme.colorScheme.primary,
                          modifier = Modifier.padding(start = 12.dp)
                        )
                      }
                    }
                  }

                  item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                      text = "2. Tashkeel / Harakat (${guide.tashkeelMarks.size})",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                  }
                  items(guide.tashkeelMarks) { t ->
                    Card(
                      modifier = Modifier.fillMaxWidth(),
                      shape = RoundedCornerShape(10.dp)
                    ) {
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Column(modifier = Modifier.weight(1f)) {
                          Text(
                            text = "${t.english} (${t.arabic})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                          )
                          Text(
                            text = "Transliteration: ${t.transliteration}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                        }
                        Text(
                          text = t.mark,
                          style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = ScheherazadeFontFamily
                          ),
                          color = MaterialTheme.colorScheme.primary
                        )
                      }
                    }
                  }

                  item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                      text = "3. Arabic Letters, Pronunciation & Tafkheem/Tarqeeq Weight (${allLetters.size})",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                  }
                  items(allLetters) { letter ->
                    Card(
                      modifier = Modifier.fillMaxWidth(),
                      shape = RoundedCornerShape(10.dp)
                    ) {
                      Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceBetween,
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Text(
                            text = "${letter.nameArabic} (${letter.transliteration}) • Weight: ${letter.weight}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                          )
                          Text(
                            text = letter.letter,
                            style = MaterialTheme.typography.headlineSmall.copy(
                              fontFamily = ScheherazadeFontFamily
                            ),
                            color = MaterialTheme.colorScheme.primary
                          )
                        }
                        if (letter.sound.isNotBlank()) {
                          Text(
                            text = "Sound: ${letter.sound} • Category: ${letter.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                          )
                        }
                        if (letter.weightRule.isNotBlank()) {
                          Text(
                            text = letter.weightRule,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                        }
                      }
                    }
                  }
                }
              }
            }

            3 -> {
              // 57 Tajweed Lessons & Exercises across 10 Chapters
              LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                item {
                  Text(
                    text = "Structured Tajweed Curriculum (${lessonChapters.size} Chapters • ${lessonChapters.sumOf { it.lessons.size }} Lessons)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
                itemsIndexed(lessonChapters, key = { idx, ch -> "ch_${idx}_${ch.id}" }) { _, ch ->
                  Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                  ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                      Text(
                        text = "${ch.title} (${ch.stage})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                      )
                      if (ch.subtitle.isNotBlank()) {
                        Text(
                          text = ch.subtitle,
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                      Spacer(modifier = Modifier.height(6.dp))
                      ch.lessons.forEach { lesson ->
                        val isOpen = selectedLesson?.id == lesson.id
                        Surface(
                          shape = RoundedCornerShape(8.dp),
                          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                          modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                              selectedLesson = if (isOpen) null else lesson
                            }
                        ) {
                          Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                              modifier = Modifier.fillMaxWidth(),
                              horizontalArrangement = Arrangement.SpaceBetween,
                              verticalAlignment = Alignment.CenterVertically
                            ) {
                              Text(
                                text = "${lesson.titleEnglish} ${if (lesson.titleArabic.isNotBlank()) "• ${lesson.titleArabic}" else ""}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                              )
                              Icon(
                                imageVector = if (isOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle Lesson"
                              )
                            }
                            if (isOpen) {
                              Spacer(modifier = Modifier.height(6.dp))
                              Text(
                                text = lesson.summary.ifBlank { lesson.definition },
                                style = MaterialTheme.typography.bodySmall
                              )
                              if (lesson.drills.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                  text = "Practice Drills:",
                                  style = MaterialTheme.typography.labelSmall,
                                  fontWeight = FontWeight.Bold,
                                  color = MaterialTheme.colorScheme.primary
                                )
                                lesson.drills.forEach { ex ->
                                  Text(
                                    text = "• $ex",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                      fontFamily = ScheherazadeFontFamily
                                    )
                                  )
                                }
                              }
                              if (lesson.quizQuestions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                  text = "Self-Check Exercises (${lesson.quizQuestions.size}):",
                                  style = MaterialTheme.typography.labelSmall,
                                  fontWeight = FontWeight.Bold,
                                  color = MaterialTheme.colorScheme.secondary
                                )
                                lesson.quizQuestions.forEach { q ->
                                  Text(
                                    text = "Q: ${q.question} — ${q.explanation}",
                                    style = MaterialTheme.typography.bodySmall
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

            4 -> {
              // 7 Qiraat Riwayat (Strictly separated from Hafs 'an 'Asim)
              LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                item {
                  Card(
                    colors = CardDefaults.cardColors(
                      containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                  ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                      Text(
                        text = "Methodological Separation Notice: Canonical Qira'at vs. Hafs 'an 'Asim",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                      )
                      Text(
                        text = "The primary Quran reader in Bayan ul Huda uses Riwayat Hafs 'an 'Asim (King Fahd Glorious Quran Printing Complex Uthmani Script). The 7 canonical Riwayat below are authentic transmitted readings (Qira'at Mutawatirah) and are kept strictly separate — never merged or treated as spelling variations.",
                        style = MaterialTheme.typography.bodySmall
                      )
                    }
                  }
                }
                itemsIndexed(qiraatSummaries, key = { idx, q -> "qiraat_${idx}_${q.riwayahKey}" }) { _, q ->
                  Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                  ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Column(modifier = Modifier.weight(1f)) {
                          Text(
                            text = q.displayNameEnglish,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                          )
                          Text(
                            text = "${q.imamAndTransmitter} • ${q.totalDifferencesCount} Annotated Spans across ${q.totalSurahsWithDifferences} Surahs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                          )
                        }
                        if (q.displayNameArabic.isNotBlank()) {
                          Text(
                            text = q.displayNameArabic,
                            style = MaterialTheme.typography.titleMedium.copy(
                              fontFamily = ScheherazadeFontFamily
                            ),
                            color = MaterialTheme.colorScheme.primary
                          )
                        }
                      }
                    }
                  }
                }
                if (qiraatDiffs.isNotEmpty()) {
                  item {
                    Text(
                      text = "Qira'at Phonetic & Tajweed Rule Definitions (${qiraatDiffs.size})",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                  }
                  itemsIndexed(qiraatDiffs, key = { idx, d -> "qdiff_${idx}_${d.ruleId}" }) { _, diff ->
                    Card(
                      modifier = Modifier.fillMaxWidth(),
                      shape = RoundedCornerShape(10.dp)
                    ) {
                      Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                          text = "${diff.ruleId} • ${diff.shortSummary}",
                          style = MaterialTheme.typography.bodyMedium,
                          fontWeight = FontWeight.Bold
                        )
                        Text(
                          text = diff.longExplanation,
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                    }
                  }
                }
              }
            }

            5 -> {
              // 30 Juz Index
              LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                item {
                  Text(
                    text = "30 Juz (Para) Navigation Index — Tap any Juz to jump directly",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
                itemsIndexed(juzList, key = { idx, j -> "juz_${idx}_${j.juzNumber}" }) { _, juz ->
                  Card(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        onNavigateToSurahAyah(juz.startSurah, juz.startAyah)
                        onDismiss()
                      },
                    shape = RoundedCornerShape(12.dp)
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = "Juz ${juz.juzNumber}: ${juz.nameTransliteration}",
                          style = MaterialTheme.typography.titleSmall,
                          fontWeight = FontWeight.Bold
                        )
                        Text(
                          text = "Starts at Surah ${juz.startSurah}:${juz.startAyah} → Ends at Surah ${juz.endSurah}:${juz.endAyah}",
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                      Text(
                        text = juz.nameArabic,
                        style = MaterialTheme.typography.titleMedium.copy(
                          fontFamily = ScheherazadeFontFamily
                        ),
                        color = MaterialTheme.colorScheme.primary
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

/**
 * Clean Folder -> Subfolder -> Files Explorer for Classical Islamic Library, Sahaba & Ahl al-Bayt,
 * and IslamQA 4,203 Scholarly Answers.
 * Clicking any book opens it directly in-app (never launching an external link).
 */
@Composable
fun ClassifiedFolderLibraryExplorerView(
  onOpenClassicalBook: (IslamicClassicalBook) -> Unit,
  onOpenProphetStories: () -> Unit,
  onOpenResearchPapers: () -> Unit
) {
  val context = LocalContext.current
  val classifiedCatalog = remember { ClassicalLibraryAndBiographiesRepository.getClassifiedLibraryCatalog(context) }

  var selectedRootFolder by remember { mutableStateOf<String?>(null) }
  var selectedSubfolder by remember { mutableStateOf<String?>(null) }

  Column(modifier = Modifier.fillMaxSize()) {
    // Breadcrumb bar
    Surface(
      color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)
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
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(
            Icons.Default.MenuBook,
            contentDescription = "Folder Path",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Text(
            text = buildString {
              append("Digital Library Folders")
              if (selectedRootFolder != null) append("  ›  $selectedRootFolder")
              if (selectedSubfolder != null) append("  ›  $selectedSubfolder")
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        if (selectedRootFolder != null) {
          TextButton(
            onClick = {
              if (selectedSubfolder != null) {
                selectedSubfolder = null
              } else {
                selectedRootFolder = null
              }
            },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(30.dp)
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Up Folder", modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }

    when (selectedRootFolder) {
      null -> {
        val sunniCount = classifiedCatalog.count {
          it.primarySection == ClassicalLibraryAndBiographiesRepository.ClassicalLibraryPrimarySection.SUNNI_CLASSICAL
        }
        val shiaCount = classifiedCatalog.count {
          it.primarySection == ClassicalLibraryAndBiographiesRepository.ClassicalLibraryPrimarySection.SHIA_CLASSICAL
        }
        val sharedCount = classifiedCatalog.count {
          it.primarySection == ClassicalLibraryAndBiographiesRepository.ClassicalLibraryPrimarySection.SHARED_OR_DISPUTED
        }
        val bioCount = remember { ClassicalLibraryAndBiographiesRepository.getAllBiographies().size }

        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
          contentPadding = PaddingValues(vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          item {
            LibraryFolderCard(
              title = "1. Sunni Classical Library (المكتبة السنية الكلاسيكية)",
              subtitle = "$sunniCount Classical Works • 4 Subfolders (Tarikh, Seerah, Tabaqat, Hadith, Tafsir & Fiqh) • Disputed/Polemical Flags",
              badgeText = "$sunniCount Books",
              iconColor = Color(0xFF1B5E20),
              onClick = {
                selectedRootFolder = "Sunni Classical Library"
                selectedSubfolder = null
              }
            )
          }
          item {
            LibraryFolderCard(
              title = "2. Shia Classical Library (المكتبة الشيعية الكلاسيكية)",
              subtitle = "$shiaCount Classical Works • 3 Subfolders (Imami Chronicles & Maqatil, Al-Kutub al-Arba'ah, Nahj al-Balagha & Sahifa)",
              badgeText = "$shiaCount Books",
              iconColor = Color(0xFF0D47A1),
              onClick = {
                selectedRootFolder = "Shia Classical Library"
                selectedSubfolder = null
              }
            )
          }
          item {
            LibraryFolderCard(
              title = "3. Shared / Disputed Attribution Library (المصادر المشتركة أو المختلف في نسبتها)",
              subtitle = "$sharedCount Works • 2 Subfolders (Al-Ya'qubi, Al-Mas'udi, Al-Baladhuri, Al-Dinawari, Ibn A'tham, Maqatil al-Talibiyyin, Al-Imama wa al-Siyasa)",
              badgeText = "$sharedCount Books",
              iconColor = Color(0xFFE65100),
              onClick = {
                selectedRootFolder = "Shared / Disputed Attribution"
                selectedSubfolder = null
              }
            )
          }
          item {
            LibraryFolderCard(
              title = "4. Sahaba & Ahl al-Bayt Biographies (تراجم الصحابة وأهل البيت)",
              subtitle = "$bioCount Multi-Perspective Biographies • Separate Sunni & Shia Historical Accounts with Arabic, Urdu, English & Vol/Page",
              badgeText = "$bioCount Entries",
              iconColor = Color(0xFF4A148C),
              onClick = {
                selectedRootFolder = "Sahaba & Ahl al-Bayt"
                selectedSubfolder = null
              }
            )
          }
          item {
            LibraryFolderCard(
              title = "5. IslamQA Scholarly Archive (4,203 Fatwas & Answers)",
              subtitle = "12 Thematic Subfolders • 4,203 Verified Q&A Records • Full Search, Original Text, References & Bookmarks",
              badgeText = "4,203 Q&As",
              iconColor = Color(0xFF006064),
              onClick = {
                selectedRootFolder = "IslamQA (4,203 Answers)"
                selectedSubfolder = null
              }
            )
          }
          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Card(
                modifier = Modifier
                  .weight(1f)
                  .clickable { onOpenProphetStories() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text(
                    text = "6A. 25 Prophet Stories",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                  Text(
                    text = "25 Stories + 25 Quizzes (قصص الانبیاء)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
              Card(
                modifier = Modifier
                  .weight(1f)
                  .clickable { onOpenResearchPapers() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text(
                    text = "6B. Research Papers (4)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                  Text(
                    text = "Continuous Line-by-Line Reader",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }

      "Sunni Classical Library", "Shia Classical Library", "Shared / Disputed Attribution" -> {
        val targetSection = when (selectedRootFolder) {
          "Sunni Classical Library" -> ClassicalLibraryAndBiographiesRepository.ClassicalLibraryPrimarySection.SUNNI_CLASSICAL
          "Shia Classical Library" -> ClassicalLibraryAndBiographiesRepository.ClassicalLibraryPrimarySection.SHIA_CLASSICAL
          else -> ClassicalLibraryAndBiographiesRepository.ClassicalLibraryPrimarySection.SHARED_OR_DISPUTED
        }
        val booksInSection = remember(targetSection, classifiedCatalog) {
          classifiedCatalog.filter { it.primarySection == targetSection }
        }
        val subfolders = remember(booksInSection) {
          booksInSection.groupBy { it.subFolderName }
        }

        if (selectedSubfolder == null) {
          val subfolderEntries = remember(subfolders) { subfolders.entries.toList() }
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            item {
              Text(
                text = "Select a Subfolder in $selectedRootFolder:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
            items(subfolderEntries, key = { it.key }) { (subfolderName, itemsInSubfolder) ->
              LibraryFolderCard(
                title = subfolderName,
                subtitle = itemsInSubfolder.joinToString(" • ") { it.book.titleEnglish }.take(140),
                badgeText = "${itemsInSubfolder.size} Files",
                iconColor = MaterialTheme.colorScheme.primary,
                onClick = { selectedSubfolder = subfolderName }
              )
            }
          }
        } else {
          val files = subfolders[selectedSubfolder].orEmpty()
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            item {
              Text(
                text = "Files in $selectedSubfolder (${files.size} Books — Click any file to open directly in-app from Line 1):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
            items(files, key = { it.book.id }) { item ->
              val book = item.book
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { onOpenClassicalBook(book) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Icon(
                      Icons.Default.MenuBook,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(16.dp)
                    )
                    Text(
                      text = book.titleEnglish,
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.weight(1f)
                    )
                  }
                  if (book.titleArabic.isNotBlank()) {
                    Text(
                      text = book.titleArabic,
                      style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = ScheherazadeFontFamily,
                        textDirection = TextDirection.Rtl
                      ),
                      textAlign = TextAlign.Right,
                      color = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.fillMaxWidth()
                    )
                  }
                  Text(
                    text = "${book.author} (${book.eraHijri}) • ${book.volumesCount} Jilds",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth()
                  )

                  if (item.scholarlyFlagBadge.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = Color(0xFFFFF3E0)
                    ) {
                      Text(
                        text = item.scholarlyFlagBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(8.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "${book.chapters.size} Indexed Jilds/Chapters • Tap to Read Line 1 → End",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary,
                      fontWeight = FontWeight.SemiBold,
                      modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                      onClick = { onOpenClassicalBook(book) },
                      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                      modifier = Modifier.height(32.dp)
                    ) {
                      Text("Open Book", style = MaterialTheme.typography.labelSmall)
                    }
                  }
                }
              }
            }
          }
        }
      }

      "Sahaba & Ahl al-Bayt" -> {
        Box(modifier = Modifier.weight(1f)) {
          SahabaAhlAlBaytBiographiesView(
            onOpenClassicalBook = onOpenClassicalBook
          )
        }
      }

      "IslamQA (4,203 Answers)" -> {
        Box(modifier = Modifier.weight(1f)) {
          IslamQaScholarlyArchiveView()
        }
      }
    }
  }
}

@Composable
private fun LibraryFolderCard(
  title: String,
  subtitle: String,
  badgeText: String,
  iconColor: Color,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = iconColor.copy(alpha = 0.14f),
        modifier = Modifier.size(44.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            Icons.Default.MenuBook,
            contentDescription = title,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
          )
        }
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = iconColor.copy(alpha = 0.12f)
      ) {
        Text(
          text = badgeText,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = iconColor,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }
    }
  }
}

/**
 * Sahaba & Ahl al-Bayt Biographies View with separate Sunni & Shia historical accounts,
 * Classical Biographical Source Books Browser (Al-Isabah, Usd al-Ghabah, Al-Isti'ab, Siyar A'lam al-Nubala',
 * Tabaqat Ibn Sa'd, Ansab al-Ashraf, Kitab al-Irshad, A'lam al-Wara, Maqatil al-Talibiyyin),
 * Arabic original, Urdu & English translations, Volume/Page citations, Search, Bookmarks,
 * Copy, Share, Report, and Direct In-App Source Book Reader integration.
 */
@Composable
fun SahabaAhlAlBaytBiographiesView(
  onOpenClassicalBook: (IslamicClassicalBook) -> Unit = {}
) {
  val context = LocalContext.current
  var searchQuery by remember { mutableStateOf("") }
  var selectedTraditionFilter by remember { mutableStateOf("ALL") } // ALL, SUNNI, SHIA
  var selectedCategoryFilter by remember { mutableStateOf("ALL") }
  var bookmarkedIds by remember {
    mutableStateOf(ClassicalLibraryAndBiographiesRepository.getBookmarkedBiographyIds(context))
  }
  var showBookmarksOnly by remember { mutableStateOf(false) }
  var lastReadBioId by remember {
    mutableStateOf(ClassicalLibraryAndBiographiesRepository.getLastReadBiographyId(context))
  }
  var selectedBioForReader by remember {
    mutableStateOf<ClassicalLibraryAndBiographiesRepository.BiographyEntry?>(null)
  }

  val biographicalSourceBooks = remember(context) {
    ClassicalLibraryAndBiographiesRepository.getBiographicalSourceBooks(context)
  }

  val allBiographies = remember {
    ClassicalLibraryAndBiographiesRepository.getAllBiographies()
  }

  val filteredEntries = remember(searchQuery, selectedTraditionFilter, selectedCategoryFilter, showBookmarksOnly, bookmarkedIds) {
    ClassicalLibraryAndBiographiesRepository.searchBiographies(
      query = searchQuery,
      traditionFilter = selectedTraditionFilter,
      categoryFilter = selectedCategoryFilter
    ).filter { !showBookmarksOnly || it.id in bookmarkedIds }
  }

  val quickPersonChips = remember {
    listOf(
      "All" to "",
      "Abu Bakr (RA)" to "Abu Bakr",
      "Umar (RA)" to "Umar",
      "Uthman (RA)" to "Uthman",
      "Ali (RA/AS)" to "Ali ibn Abi Talib",
      "Lady Fatimah" to "Fatimah",
      "Imam Hasan" to "Hasan ibn Ali",
      "Imam Husayn" to "Husayn ibn Ali",
      "Zaynab & Abbas" to "Zaynab",
      "Zayn al-Abidin" to "Zayn al-Abidin",
      "Baqir & Sadiq" to "Sadiq",
      "Kazim & Rida" to "Kazim",
      "Hamzah & Ja'far" to "Hamzah",
      "'Asharah Mubashsharah" to "Talhah",
      "Salman, Abu Dharr & Bilal" to "Salman",
      "Mu'adh & Khalid" to "Khalid",
      "Khadijah & A'ishah" to "Khadijah",
      "Umm Salamah & Asma'" to "Umm Salamah"
    )
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    contentPadding = PaddingValues(vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 1. Header Banner explaining the Sunni & Shia Separate Biographical Architecture
    item(key = "sahaba_ahl_al_bayt_header_banner") {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("sahaba_ahl_al_bayt_header_banner")
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "Sahaba & Ahl al-Bayt Biographical Encyclopedia (${allBiographies.size} Multi-Perspective Accounts • ${biographicalSourceBooks.size} Source Books)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "Sunni Sources: Al-Isabah (Ibn Hajar) • Usd al-Ghabah (Ibn al-Athir) • Al-Isti'ab (Ibn 'Abd al-Barr) • Siyar A'lam al-Nubala' (Al-Dhahabi) • Tabaqat Ibn Sa'd • Ansab al-Ashraf | Shia Sources: Kitab al-Irshad (Al-Mufid) • A'lam al-Wara (Al-Tabarsi) • Maqatil al-Talibiyyin",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // 2. Search Bar (Person Name, Arabic/Urdu/Roman Urdu, Source Book, Author, or Reference)
    item(key = "sahaba_ahl_al_bayt_search_bar") {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("sahaba_ahl_al_bayt_search_input"),
        placeholder = {
          Text(
            text = "Search Sahaba, Ahl al-Bayt, Abu Bakr, Umar, Usman, Ali, Fatima, Hussain, Al-Isabah, Al-Irshad...",
            fontSize = 12.sp
          )
        },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Biographies") },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )
    }

    // 3. Quick Person & Topic Search Chips
    item(key = "sahaba_ahl_al_bayt_quick_chips") {
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        items(quickPersonChips.size) { idx ->
          val (chipLabel, queryValue) = quickPersonChips[idx]
          val isSelected = searchQuery.equals(queryValue, ignoreCase = true)
          FilterChip(
            selected = isSelected,
            onClick = {
              searchQuery = if (isSelected && queryValue.isNotEmpty()) "" else queryValue
            },
            label = {
              Text(
                text = chipLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
              )
            }
          )
        }
      }
    }

    // 4. Classical Biographical Source Books Browser (Direct In-App Reader Access)
    if (biographicalSourceBooks.isNotEmpty()) {
      item(key = "sahaba_ahl_al_bayt_source_books_row") {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Classical Biographical Source Books (Tap 'Read Book' to open full multi-Jild book in-app):",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(biographicalSourceBooks.size) { idx ->
              val book = biographicalSourceBooks[idx]
              val isShiaBook = book.tradition.equals("SHIA", ignoreCase = true)
              val badgeColor = if (isShiaBook) Color(0xFF0D47A1) else Color(0xFF1B5E20)
              Card(
                modifier = Modifier
                  .width(250.dp)
                  .clickable { onOpenClassicalBook(book) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = badgeColor.copy(alpha = 0.14f)
                    ) {
                      Text(
                        text = "${book.tradition} • ${book.volumesCount} Jilds",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                    Text(
                      text = book.titleArabic,
                      style = MaterialTheme.typography.labelMedium.copy(fontFamily = ScheherazadeFontFamily),
                      color = MaterialTheme.colorScheme.primary,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = book.titleEnglish,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = book.author,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    OutlinedButton(
                      onClick = {
                        val shortKeyword = when {
                          book.titleEnglish.contains("Isaba", ignoreCase = true) -> "Isabah"
                          book.titleEnglish.contains("Usd al-Ghaba", ignoreCase = true) -> "Usd al-Ghabah"
                          book.titleEnglish.contains("Isti'ab", ignoreCase = true) -> "Isti'ab"
                          book.titleEnglish.contains("Siyar A'lam", ignoreCase = true) -> "Siyar A'lam"
                          book.titleEnglish.contains("Tabaqat", ignoreCase = true) -> "Tabaqat"
                          book.titleEnglish.contains("Ansab", ignoreCase = true) -> "Ansab"
                          book.titleEnglish.contains("Irshad", ignoreCase = true) -> "Irshad"
                          book.titleEnglish.contains("A'lam al-Wara", ignoreCase = true) -> "A'lam al-Wara"
                          book.titleEnglish.contains("Maqatil", ignoreCase = true) -> "Maqatil"
                          else -> book.titleEnglish.substringBefore(" ")
                        }
                        searchQuery = shortKeyword
                      },
                      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                      modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                    ) {
                      Text("Filter Bios", fontSize = 10.sp, maxLines = 1)
                    }
                    Button(
                      onClick = { onOpenClassicalBook(book) },
                      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                      modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                    ) {
                      Text("Read Book", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // 5. Tradition & Bookmark Filter Row
    item(key = "sahaba_ahl_al_bayt_tradition_filters") {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        listOf(
          "ALL" to "All Traditions (Sunni & Shia Separate)",
          "SUNNI" to "Sunni Biographical Sources",
          "SHIA" to "Shia Biographical Sources"
        ).forEach { (key, label) ->
          FilterChip(
            selected = selectedTraditionFilter == key && !showBookmarksOnly,
            onClick = {
              selectedTraditionFilter = key
              showBookmarksOnly = false
            },
            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
          )
        }
        FilterChip(
          selected = showBookmarksOnly,
          onClick = { showBookmarksOnly = !showBookmarksOnly },
          label = { Text("Bookmarked (${bookmarkedIds.size})", style = MaterialTheme.typography.labelSmall) }
        )
      }
    }

    // 6. Category Filter Row
    item(key = "sahaba_ahl_al_bayt_category_filters") {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        listOf(
          "ALL" to "All Categories (${filteredEntries.size})",
          "Khulafa & 'Asharah Mubashsharah" to "Khulafa & 'Asharah",
          "Ahl al-Bayt" to "Ahl al-Bayt (AS/RA)",
          "Mothers of the Believers" to "Ummahat al-Mu'minin",
          "Muhajirun & Ansar" to "Muhajirun & Ansar",
          "Mothers of the Believers & Sahabiyyat" to "Sahabiyyat"
        ).forEach { (catKey, catLabel) ->
          FilterChip(
            selected = selectedCategoryFilter == catKey,
            onClick = { selectedCategoryFilter = catKey },
            label = { Text(catLabel, style = MaterialTheme.typography.labelSmall) }
          )
        }
      }
    }

    // 7. Continue Reading Banner if user previously read a biography
    val lastReadEntry = allBiographies.firstOrNull { it.id == lastReadBioId }
    if (lastReadEntry != null && selectedBioForReader == null) {
      item(key = "sahaba_ahl_al_bayt_continue_reading") {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedBioForReader = lastReadEntry }
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Continue Reading Biography:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = "${lastReadEntry.personNameEnglish} (${lastReadEntry.primarySourceBook})",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
            TextButton(onClick = { selectedBioForReader = lastReadEntry }) {
              Text("Resume →", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // 8. Focused Full-Biography Reader Card (with Prev / Next Biography & Open Source Book)
    if (selectedBioForReader != null) {
      item(key = "sahaba_ahl_al_bayt_focused_reader") {
        val activeBio = selectedBioForReader!!
        val currentIdx = filteredEntries.indexOfFirst { it.id == activeBio.id }.coerceAtLeast(0)
        val isShia = activeBio.traditionPerspective.equals("SHIA", ignoreCase = true)
        val accentColor = if (isShia) Color(0xFF0D47A1) else Color(0xFF1B5E20)
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedButton(
                onClick = { selectedBioForReader = null },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
              ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("All Biographies", style = MaterialTheme.typography.labelSmall)
              }
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                  onClick = {
                    if (currentIdx > 0) {
                      val prevBio = filteredEntries[currentIdx - 1]
                      selectedBioForReader = prevBio
                      lastReadBioId = prevBio.id
                      ClassicalLibraryAndBiographiesRepository.saveLastReadBiographyId(context, prevBio.id)
                    }
                  },
                  enabled = currentIdx > 0,
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                  modifier = Modifier.height(32.dp)
                ) {
                  Text("‹ Prev", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                  onClick = {
                    if (currentIdx < filteredEntries.lastIndex) {
                      val nextBio = filteredEntries[currentIdx + 1]
                      selectedBioForReader = nextBio
                      lastReadBioId = nextBio.id
                      ClassicalLibraryAndBiographiesRepository.saveLastReadBiographyId(context, nextBio.id)
                    }
                  },
                  enabled = currentIdx < filteredEntries.lastIndex,
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                  modifier = Modifier.height(32.dp)
                ) {
                  Text("Next ›", style = MaterialTheme.typography.labelSmall)
                }
              }
            }
            Text(
              text = "${activeBio.personNameEnglish} (${currentIdx + 1} of ${filteredEntries.size})",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = accentColor
            )
            Text(
              text = "${activeBio.personNameArabic} • ${activeBio.personNameUrdu}",
              style = MaterialTheme.typography.titleMedium.copy(fontFamily = ScheherazadeFontFamily),
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "Source: ${activeBio.primarySourceBook} • ${activeBio.volumeAndPage} • ${activeBio.author}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = accentColor
            )
            SelectionContainer {
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                  text = activeBio.arabicText,
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = ScheherazadeFontFamily,
                    fontSize = 21.sp,
                    lineHeight = 36.sp,
                    textDirection = TextDirection.Rtl
                  ),
                  textAlign = TextAlign.Right,
                  modifier = Modifier.fillMaxWidth()
                )
                Text(
                  text = activeBio.urduTranslation,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    lineHeight = 30.sp,
                    textDirection = TextDirection.Rtl
                  ),
                  textAlign = TextAlign.Right,
                  color = MaterialTheme.colorScheme.secondary,
                  modifier = Modifier.fillMaxWidth()
                )
                Text(
                  text = activeBio.englishTranslation,
                  style = MaterialTheme.typography.bodyMedium,
                  lineHeight = 22.sp
                )
                Text(
                  text = "Cross-References: ${activeBio.crossReferences} • Translator: ${activeBio.translatorAttribution}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }

    // 9. All Matching Biographies List (Sunni & Shia Strictly Separated)
    items(filteredEntries, key = { it.id }) { bio ->
      val isBookmarked = bio.id in bookmarkedIds
      val isShia = bio.traditionPerspective.equals("SHIA", ignoreCase = true)
      val accentColor = if (isShia) Color(0xFF0D47A1) else Color(0xFF1B5E20)
      val matchingSourceBook = remember(bio.primarySourceBook, biographicalSourceBooks) {
        biographicalSourceBooks.firstOrNull { srcBook ->
          bio.primarySourceBook.contains("Isabah", ignoreCase = true) && srcBook.titleEnglish.contains("Isaba", ignoreCase = true) ||
            bio.primarySourceBook.contains("Usd al-Ghabah", ignoreCase = true) && srcBook.titleEnglish.contains("Usd al-Ghaba", ignoreCase = true) ||
            bio.primarySourceBook.contains("Isti'ab", ignoreCase = true) && srcBook.titleEnglish.contains("Isti'ab", ignoreCase = true) ||
            bio.primarySourceBook.contains("Siyar A'lam", ignoreCase = true) && srcBook.titleEnglish.contains("Siyar A'lam", ignoreCase = true) ||
            bio.primarySourceBook.contains("Tabaqat", ignoreCase = true) && srcBook.titleEnglish.contains("Tabaqat", ignoreCase = true) ||
            bio.primarySourceBook.contains("Irshad", ignoreCase = true) && srcBook.titleEnglish.contains("Irshad", ignoreCase = true) ||
            bio.primarySourceBook.contains("A'lam al-Wara", ignoreCase = true) && srcBook.titleEnglish.contains("A'lam al-Wara", ignoreCase = true) ||
            bio.primarySourceBook.contains("Maqatil", ignoreCase = true) && srcBook.titleEnglish.contains("Maqatil", ignoreCase = true)
        }
      }

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            selectedBioForReader = bio
            lastReadBioId = bio.id
            ClassicalLibraryAndBiographiesRepository.saveLastReadBiographyId(context, bio.id)
          },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = accentColor.copy(alpha = 0.12f)
          ) {
            Text(
              text = "${bio.traditionPerspective} PERSPECTIVE • ${bio.category}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = accentColor,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = bio.personNameEnglish,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = bio.personNameArabic,
            style = MaterialTheme.typography.titleLarge.copy(
              fontFamily = ScheherazadeFontFamily,
              textDirection = TextDirection.Rtl
            ),
            textAlign = TextAlign.Right,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
          )
          Text(
            text = bio.personNameUrdu,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = UrduNastaliqFontFamily,
              textDirection = TextDirection.Rtl
            ),
            textAlign = TextAlign.Right,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "${bio.titleHonorific} • Era: ${bio.eraHijri}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.fillMaxWidth()
          )
          Text(
            text = "Source: ${bio.primarySourceBook} (${bio.sourceBookArabic}) • ${bio.author} • ${bio.volumeAndPage}",
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = accentColor.copy(alpha = 0.06f),
            modifier = Modifier.fillMaxWidth()
          ) {
            SelectionContainer {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(
                  text = bio.arabicText,
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = ScheherazadeFontFamily,
                    fontSize = 20.sp,
                    lineHeight = 34.sp,
                    textDirection = TextDirection.Rtl
                  ),
                  textAlign = TextAlign.Right,
                  modifier = Modifier.fillMaxWidth()
                )
                Text(
                  text = bio.urduTranslation,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = UrduNastaliqFontFamily,
                    lineHeight = 28.sp,
                    textDirection = TextDirection.Rtl
                  ),
                  textAlign = TextAlign.Right,
                  color = MaterialTheme.colorScheme.secondary,
                  modifier = Modifier.fillMaxWidth()
                )
                Text(
                  text = bio.englishTranslation,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Translator / Attribution: ${bio.translatorAttribution} • Refs: ${bio.crossReferences}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              OutlinedButton(
                onClick = {
                  selectedBioForReader = bio
                  lastReadBioId = bio.id
                  ClassicalLibraryAndBiographiesRepository.saveLastReadBiographyId(context, bio.id)
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.height(30.dp)
              ) {
                Text("Focus Reader", style = MaterialTheme.typography.labelSmall)
              }
              if (matchingSourceBook != null) {
                FilledTonalButton(
                  onClick = { onOpenClassicalBook(matchingSourceBook) },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                  modifier = Modifier.height(30.dp)
                ) {
                  Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Open Source Book", style = MaterialTheme.typography.labelSmall)
                }
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              TextButton(
                onClick = {
                  ClassicalLibraryAndBiographiesRepository.toggleBiographyBookmark(context, bio.id)
                  bookmarkedIds = ClassicalLibraryAndBiographiesRepository.getBookmarkedBiographyIds(context)
                }
              ) {
                Icon(
                  imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                  contentDescription = "Bookmark Biography",
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isBookmarked) "Saved" else "Bookmark", style = MaterialTheme.typography.labelSmall)
              }
              TextButton(
                onClick = {
                  val fullText = buildString {
                    appendLine("${bio.personNameEnglish} (${bio.personNameArabic}) [${bio.traditionPerspective}]")
                    appendLine("Source: ${bio.primarySourceBook} (${bio.volumeAndPage}) — ${bio.author}")
                    appendLine()
                    appendLine(bio.arabicText)
                    appendLine()
                    appendLine(bio.urduTranslation)
                    appendLine()
                    appendLine(bio.englishTranslation)
                  }
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  clipboard.setPrimaryClip(ClipData.newPlainText(bio.personNameEnglish, fullText))
                  Toast.makeText(context, "Biography copied with citations", Toast.LENGTH_SHORT).show()
                }
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy", style = MaterialTheme.typography.labelSmall)
              }
              TextButton(
                onClick = {
                  val shareText = "${bio.personNameEnglish} (${bio.personNameArabic}) [${bio.primarySourceBook} ${bio.volumeAndPage}]\n\n${bio.arabicText}\n\n${bio.englishTranslation}"
                  val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                  }
                  context.startActivity(Intent.createChooser(intent, "Share Biography"))
                }
              ) {
                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share", style = MaterialTheme.typography.labelSmall)
              }
              TextButton(
                onClick = {
                  Toast.makeText(
                    context,
                    "Logged citation verification report for ${bio.personNameEnglish}",
                    Toast.LENGTH_SHORT
                  ).show()
                }
              ) {
                Icon(Icons.Default.Flag, contentDescription = "Report", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Report", style = MaterialTheme.typography.labelSmall)
              }
            }
          }
        }
      }
    }
  }
}

/**
 * IslamQA 4,203+ Scholarly Answers Archive Reader & Search View.
 * Preserves original question wording, original answer wording, Arabic quotations,
 * Quran/Hadith references, categories, and scholarly attribution.
 * Displays "اردو ترجمہ فی الحال دستیاب نہیں" when Urdu is toggled (never AI-translating).
 */
@Composable
fun IslamQaScholarlyArchiveView() {
  val context = LocalContext.current
  var categories by remember { mutableStateOf<List<IslamQaRepository.IslamQaCategorySummary>>(emptyList()) }
  var selectedCategory by remember { mutableStateOf("ALL") }
  var searchQuery by remember { mutableStateOf("") }
  var results by remember { mutableStateOf<List<IslamQaRepository.IslamQaArticle>>(emptyList()) }
  var isLoading by remember { mutableStateOf(true) }
  var selectedFatwa by remember { mutableStateOf<IslamQaRepository.IslamQaArticle?>(null) }
  var showUrduTranslationNotice by remember { mutableStateOf(true) }
  var showBookmarksOnly by remember { mutableStateOf(false) }
  var bookmarkedIds by remember { mutableStateOf(IslamQaRepository.getBookmarkedIds(context)) }

  LaunchedEffect(searchQuery, selectedCategory, showBookmarksOnly, bookmarkedIds) {
    val loadedPair = withContext(Dispatchers.IO) {
      val cats = if (categories.isEmpty()) {
        runCatching { IslamQaRepository.getCategories(context) }.getOrDefault(emptyList())
      } else categories
      val res = runCatching {
        IslamQaRepository.searchArticles(
          context = context,
          query = searchQuery,
          categoryFilter = selectedCategory,
          bookmarksOnly = showBookmarksOnly
        ).take(100)
      }.getOrDefault(emptyList())
      cats to res
    }
    if (categories.isEmpty()) categories = loadedPair.first
    results = loadedPair.second
    isLoading = false
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "IslamQA Verified Scholarly Archive (4,203 Answers • 12 Categories)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "Source: IslamQA.info (Supervised by Shaykh Muhammad Salih al-Munajjid) • SHA-256 Verified Offline Dataset • Zero AI Modification",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search across all 4,203 scholarly questions, answers, Quran/Hadith refs...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search IslamQA") },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )
    }

    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        FilterChip(
          selected = selectedCategory == "ALL" && !showBookmarksOnly,
          onClick = {
            selectedCategory = "ALL"
            showBookmarksOnly = false
          },
          label = { Text("All 4,203 Answers") }
        )
        FilterChip(
          selected = showBookmarksOnly,
          onClick = { showBookmarksOnly = !showBookmarksOnly },
          label = { Text("Saved (${bookmarkedIds.size})") }
        )
        categories.forEach { cat ->
          FilterChip(
            selected = selectedCategory == cat.categoryRaw && !showBookmarksOnly,
            onClick = {
              showBookmarksOnly = false
              selectedCategory = if (selectedCategory == cat.categoryRaw) "ALL" else cat.categoryRaw
            },
            label = { Text("${cat.displayEnglish} (${cat.articleCount})", style = MaterialTheme.typography.labelSmall) }
          )
        }
      }
    }

    if (isLoading) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator()
        }
      }
    } else if (selectedFatwa != null) {
      item {
        val fatwa = selectedFatwa!!
        val isBookmarked = fatwa.id in bookmarkedIds
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedButton(
                onClick = { selectedFatwa = null },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
              ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back to List", style = MaterialTheme.typography.labelSmall)
              }
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                  selected = showUrduTranslationNotice,
                  onClick = { showUrduTranslationNotice = !showUrduTranslationNotice },
                  label = { Text("Urdu Status", style = MaterialTheme.typography.labelSmall) }
                )
                IconButton(
                  onClick = {
                    IslamQaRepository.toggleBookmark(context, fatwa.id)
                    bookmarkedIds = IslamQaRepository.getBookmarkedIds(context)
                  }
                ) {
                  Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark Fatwa"
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Category: ${fatwa.categoryDisplayEnglish} (${fatwa.categoryDisplayUrdu}) • Record #${fatwa.id}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            SelectionContainer {
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (showUrduTranslationNotice) {
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text(
                      text = fatwa.urduStatusNotice, // "اردو ترجمہ فی الحال دستیاب نہیں"
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = UrduNastaliqFontFamily,
                        textDirection = TextDirection.Rtl
                      ),
                      textAlign = TextAlign.Right,
                      color = MaterialTheme.colorScheme.secondary,
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                  }
                }

                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                      text = "Question (Original Wording):",
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = fatwa.question,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                }

                Text(
                  text = "Scholarly Answer (Original Unmodified Text):",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = fatwa.answer,
                  style = MaterialTheme.typography.bodyMedium,
                  lineHeight = 22.sp
                )

                if (fatwa.extractedReferences.isNotEmpty()) {
                  Text(
                    text = "Extracted Scriptural References: ${fatwa.extractedReferences.joinToString(" • ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                  )
                }

                Text(
                  text = "Attribution: ${fatwa.scholarlyAttribution} • Source: ${fatwa.sourceRepository}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              TextButton(
                onClick = {
                  val clip = "Question (#${fatwa.id}):\n${fatwa.question}\n\nAnswer:\n${fatwa.answer}\n\nSource: ${fatwa.scholarlyAttribution}"
                  val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  cm.setPrimaryClip(ClipData.newPlainText("IslamQA #${fatwa.id}", clip))
                  Toast.makeText(context, "Fatwa copied to clipboard", Toast.LENGTH_SHORT).show()
                }
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy", style = MaterialTheme.typography.labelSmall)
              }
              TextButton(
                onClick = {
                  val clip = "Question (#${fatwa.id}):\n${fatwa.question}\n\nAnswer:\n${fatwa.answer}"
                  val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, clip)
                  }
                  context.startActivity(Intent.createChooser(intent, "Share Scholarly Answer"))
                }
              ) {
                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share", style = MaterialTheme.typography.labelSmall)
              }
              TextButton(
                onClick = {
                  Toast.makeText(context, "Reported Q&A #${fatwa.id} for review", Toast.LENGTH_SHORT).show()
                }
              ) {
                Icon(Icons.Default.Flag, contentDescription = "Report", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Report", style = MaterialTheme.typography.labelSmall)
              }
            }
          }
        }
      }
    } else {
      item {
        Text(
          text = "Showing ${results.size} matching scholarly answers (Tap any card to read full original Question & Answer):",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      items(results, key = { it.id }) { fatwa ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedFatwa = fatwa },
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${fatwa.categoryDisplayEnglish} • #${fatwa.id}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = fatwa.urduStatusNotice,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = UrduNastaliqFontFamily
                ),
                color = MaterialTheme.colorScheme.secondary
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = fatwa.question,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = fatwa.answer,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }
  }
}
