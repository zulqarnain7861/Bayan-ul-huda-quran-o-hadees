package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditedHadithReference
import com.example.data.model.AuditedNamazStep
import com.example.data.model.CountryStatus
import com.example.data.model.LectureFormat
import com.example.data.model.LectureRecord
import com.example.data.model.NamazMuhammadiModuleData
import com.example.data.model.NamazStepType
import com.example.data.model.PlaybackDataProfile
import com.example.data.model.RecordVerificationState
import com.example.data.model.ReferenceStatus
import com.example.data.model.ReferenceVerificationStatus
import com.example.data.model.SourceTier
import com.example.data.model.TranscriptStatus
import com.example.player.YtEmbedHost
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CompactLineHeightStyle
import com.example.ui.theme.CompactPlatformTextStyle
import com.example.ui.theme.UrduNastaliqFontFamily

private val CompactScriptStyle = TextStyle(
  platformStyle = CompactPlatformTextStyle,
  lineHeightStyle = CompactLineHeightStyle
)

enum class NamazDisplayMode(val key: String, val labelEn: String, val labelUr: String) {
  STEP_BY_STEP("step_by_step", "Step-by-Step Guided", "مرحلہ وار سیکھیں"),
  FULL_LIST("full_list", "All Steps List", "مکمل فہرست"),
  PRAYER_STRUCTURES("structures", "Prayer & Rakat Chart", "نمازوں کی رکعات"),
  AUDIT_REPORT("audit", "Reference Audit Report", "تحقیقی آڈٹ رپورٹ")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NamazMuhammadiInteractiveHeaderAndModeBar(
  selectedMode: NamazDisplayMode,
  onSelectMode: (NamazDisplayMode) -> Unit,
  arabicFontSizeSp: Float,
  onChangeArabicFontSize: (Float) -> Unit,
  showUrdu: Boolean,
  onToggleUrdu: () -> Unit,
  showEnglish: Boolean,
  onToggleEnglish: () -> Unit,
  showRomanUrdu: Boolean,
  onToggleRomanUrdu: () -> Unit,
  completedStepIds: Set<Int>,
  totalStepsCount: Int
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFFF4FBF7)),
    border = BorderStroke(1.dp, Color(0xFF1B7A58).copy(alpha = 0.45f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("namaz_interactive_controls_card")
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Interactive Learning & Audit Controls",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0A4D3C)
          )
          Text(
            text = "Completed: ${completedStepIds.size} of $totalStepsCount Steps • Verified Primary Hadith Sources",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF1B7A58)
          )
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Surface(
            onClick = { onChangeArabicFontSize((arabicFontSizeSp - 2f).coerceAtLeast(18f)) },
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF0A4D3C).copy(alpha = 0.1f),
            modifier = Modifier.testTag("namaz_font_decrease_btn")
          ) {
            Text(
              text = "A-",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF0A4D3C),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
          Text(
            text = "${arabicFontSizeSp.toInt()}sp",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF073B2E)
          )
          Surface(
            onClick = { onChangeArabicFontSize((arabicFontSizeSp + 2f).coerceAtMost(34f)) },
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF0A4D3C).copy(alpha = 0.1f),
            modifier = Modifier.testTag("namaz_font_increase_btn")
          ) {
            Text(
              text = "A+",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF0A4D3C),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }
      }

      // Mode selector chips
      LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(NamazDisplayMode.entries) { mode ->
          val selected = selectedMode == mode
          FilterChip(
            selected = selected,
            onClick = { onSelectMode(mode) },
            label = {
              Text(
                text = "${mode.labelEn} (${mode.labelUr})",
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Color(0xFF0A4D3C),
              selectedLabelColor = Color.White
            ),
            modifier = Modifier.testTag("namaz_mode_chip_${mode.key}")
          )
        }
      }

      // Language & Transliteration Toggles
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        FilterChip(
          selected = showUrdu,
          onClick = onToggleUrdu,
          label = { Text("Urdu (اردو)", fontSize = 11.sp) },
          modifier = Modifier.testTag("namaz_toggle_urdu_chip")
        )
        FilterChip(
          selected = showEnglish,
          onClick = onToggleEnglish,
          label = { Text("English", fontSize = 11.sp) },
          modifier = Modifier.testTag("namaz_toggle_english_chip")
        )
        FilterChip(
          selected = showRomanUrdu,
          onClick = onToggleRomanUrdu,
          label = { Text("Roman Urdu (Beginner Transliteration)", fontSize = 11.sp) },
          modifier = Modifier.testTag("namaz_toggle_roman_urdu_chip")
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NamazSequentialStepByStepView(
  steps: List<AuditedNamazStep>,
  currentStepIndex: Int,
  onSelectStepIndex: (Int) -> Unit,
  arabicFontSizeSp: Float,
  showUrdu: Boolean,
  showEnglish: Boolean,
  showRomanUrdu: Boolean,
  completedStepIds: Set<Int>,
  bookmarkedStepIds: Set<Int>,
  onToggleStepCompleted: (Int) -> Unit,
  onToggleStepBookmarked: (Int) -> Unit
) {
  if (steps.isEmpty()) return
  val safeIndex = currentStepIndex.coerceIn(0, steps.lastIndex)
  val currentStep = steps[safeIndex]
  val progress = (safeIndex + 1).toFloat() / steps.size.toFloat()

  val sections = remember(steps) {
    steps.map { it.sectionId to "${it.sectionTitleEn} (${it.sectionTitleUr})" }.distinctBy { it.first }
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("namaz_sequential_step_view"),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Section jump chips
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      items(sections, key = { it.first }) { (secId, secLabel) ->
        val isCurrentSection = currentStep.sectionId == secId
        Surface(
          onClick = {
            val firstIdx = steps.indexOfFirst { it.sectionId == secId }
            if (firstIdx >= 0) onSelectStepIndex(firstIdx)
          },
          shape = RoundedCornerShape(14.dp),
          color = if (isCurrentSection) Color(0xFFD4AF37) else Color(0xFF0A4D3C).copy(alpha = 0.08f),
          modifier = Modifier.testTag("namaz_section_jump_$secId")
        ) {
          Text(
            text = secLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isCurrentSection) Color(0xFF073B2E) else Color(0xFF0A4D3C),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
          )
        }
      }
    }

    // Progress Bar & Prev/Next Navigation Bar
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF073B2E)),
      modifier = Modifier.fillMaxWidth()
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
          OutlinedButton(
            onClick = { if (safeIndex > 0) onSelectStepIndex(safeIndex - 1) },
            enabled = safeIndex > 0,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, if (safeIndex > 0) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.3f)),
            modifier = Modifier.testTag("namaz_prev_step_btn")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Step", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Previous", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          Column(
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Step ${currentStep.id} (${safeIndex + 1} of ${steps.size})",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFFD4AF37),
              textAlign = TextAlign.Center,
              modifier = Modifier.testTag("namaz_step_progress_label")
            )
            Text(
              text = currentStep.sectionTitleUr,
              style = CompactScriptStyle,
              fontFamily = UrduNastaliqFontFamily,
              fontSize = 12.sp,
              lineHeight = 20.sp,
              color = Color.White,
              textAlign = TextAlign.Center
            )
          }

          Button(
            onClick = { if (safeIndex < steps.lastIndex) onSelectStepIndex(safeIndex + 1) },
            enabled = safeIndex < steps.lastIndex,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFFD4AF37),
              contentColor = Color(0xFF073B2E)
            ),
            modifier = Modifier.testTag("namaz_next_step_btn")
          ) {
            Text("Next", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Step", modifier = Modifier.size(16.dp))
          }
        }

        LinearProgressIndicator(
          progress = { progress },
          color = Color(0xFFD4AF37),
          trackColor = Color.White.copy(alpha = 0.2f),
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
        )

        // Quick step number pills
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          itemsIndexed(steps, key = { _, s -> s.id }) { idx, st ->
            val isCurrent = idx == safeIndex
            val isDone = completedStepIds.contains(st.id)
            Surface(
              onClick = { onSelectStepIndex(idx) },
              shape = RoundedCornerShape(8.dp),
              color = when {
                isCurrent -> Color(0xFFD4AF37)
                isDone -> Color(0xFF2E7D32)
                else -> Color.White.copy(alpha = 0.14f)
              },
              modifier = Modifier.testTag("namaz_step_pill_${st.id}")
            ) {
              Text(
                text = if (isDone) "✓ ${st.id}" else "${st.id}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isCurrent) Color(0xFF073B2E) else Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }
        }
      }
    }

    // Active Step Card
    AuditedNamazStepCard(
      step = currentStep,
      arabicFontSizeSp = arabicFontSizeSp,
      showUrdu = showUrdu,
      showEnglish = showEnglish,
      showRomanUrdu = showRomanUrdu,
      isCompleted = completedStepIds.contains(currentStep.id),
      isBookmarked = bookmarkedStepIds.contains(currentStep.id),
      onToggleCompleted = { onToggleStepCompleted(currentStep.id) },
      onToggleBookmarked = { onToggleStepBookmarked(currentStep.id) },
      defaultExpandReferences = true
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuditedNamazStepCard(
  step: AuditedNamazStep,
  arabicFontSizeSp: Float = 22f,
  showUrdu: Boolean = true,
  showEnglish: Boolean = true,
  showRomanUrdu: Boolean = true,
  isCompleted: Boolean = false,
  isBookmarked: Boolean = false,
  onToggleCompleted: () -> Unit = {},
  onToggleBookmarked: () -> Unit = {},
  defaultExpandReferences: Boolean = false,
  testTagPrefix: String = "audited_namaz_step"
) {
  val context = LocalContext.current
  val uriHandler = LocalUriHandler.current
  var selectedOptionIndex by remember(step.id) { mutableIntStateOf(0) }
  var selectedVariantIndex by remember(step.id) { mutableIntStateOf(-1) } // -1 = Primary, 0..N = Variant
  var referencesExpanded by remember(step.id) { mutableStateOf(defaultExpandReferences) }
  var videoProfile by remember { mutableStateOf(PlaybackDataProfile.BALANCED) }
  var videoWifiOnly by remember { mutableStateOf(false) }

  val isResearchedAddition = step.sourceAttribution.contains("Researched Addition", ignoreCase = true)

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
        references = step.references.map { it.formattedDisplay() },
        alternateYoutubeIds = emptyList()
      )
    } else {
      null
    }
  }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(
      width = if (isCompleted || embeddedLectureRecord != null) 1.5.dp else 1.dp,
      color = when {
        isCompleted -> Color(0xFF2E7D32)
        embeddedLectureRecord != null -> Color(0xFFD4AF37)
        else -> MaterialTheme.colorScheme.outlineVariant
      }
    ),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("${testTagPrefix}_${step.id}")
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Embedded Video at the top of Step 10 (Ruku) with Hadith references directly below
      if (embeddedLectureRecord != null) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF073B2E).copy(alpha = 0.06f),
          border = BorderStroke(1.dp, Color(0xFF0A4D3C).copy(alpha = 0.3f)),
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
                  text = "Offline + Online • ${embeddedLectureRecord.youtubeId}",
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
              selectedProfile = videoProfile,
              wifiOnlyEnabled = videoWifiOnly,
              onSelectProfile = { videoProfile = it },
              onToggleWifiOnly = { videoWifiOnly = it },
              onWatchSecondsRecorded = { _, _, _ -> },
              onSelectAlternateLecture = {},
              onReportBroken = {}
            )

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFE8F5E9),
              border = BorderStroke(1.dp, Color(0xFF1B7A58)),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("step_10_below_video_references")
            ) {
              Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text(
                  text = "Step ${step.id} Hadith References (Directly Below Video):",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color(0xFF0A4D3C)
                )
                Text(
                  text = step.references.joinToString("; ") { it.formattedDisplay() },
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF073B2E)
                )
              }
            }
          }
        }
      }

      // Badges Row: Step Number, Type Badge, Ruling Status, Repeat Count, Source Attribution
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        FlowRow(
          modifier = Modifier.weight(1f),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF0A4D3C)
          ) {
            Text(
              text = if (step.id == 0) "Step 0 (Prerequisite)" else "Step ${step.id}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }

          // Step Type Badge (ACTION, RECITATION, DUA, QURAN_RECITATION, EXPLANATION)
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = when (step.stepType) {
              NamazStepType.ACTION -> Color(0xFFE3F2FD)
              NamazStepType.QURAN_RECITATION -> Color(0xFFE8F5E9)
              NamazStepType.DUA -> Color(0xFFFFF8E1)
              NamazStepType.RECITATION -> Color(0xFFF3E5F5)
              NamazStepType.EXPLANATION -> Color(0xFFE0F2F1)
            }
          ) {
            Text(
              text = "${step.stepType.badgeLabelEn} • ${step.stepType.badgeLabelUr}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF073B2E),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }

          if (step.repeatMin > 1) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFD4AF37).copy(alpha = 0.25f)
            ) {
              Text(
                text = "Repeat ≥ ${step.repeatMin}x (کم از کم ${step.repeatMin} مرتبہ)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF5D4037),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          // Source Attribution Badge (Original Booklet vs Researched Addition)
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isResearchedAddition) Color(0xFFE8EAF6) else Color(0xFFE8F5E9)
          ) {
            Text(
              text = step.sourceAttribution,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = if (isResearchedAddition) Color(0xFF283593) else Color(0xFF1B5E20),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }

        // Action icons: Copy, Bookmark, Complete
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
              val copyBody = buildString {
                appendLine("Step ${step.id}: ${step.titleEn} (${step.titleUr})")
                if (step.arabic.isNotBlank()) appendLine(step.arabic)
                if (step.urdu.isNotBlank()) appendLine(step.urdu)
                if (step.english.isNotBlank()) appendLine(step.english)
                appendLine("References: ${step.references.joinToString("; ") { it.formattedDisplay() }}")
              }
              clipboard?.setPrimaryClip(ClipData.newPlainText("Namaz Step ${step.id}", copyBody))
              Toast.makeText(context, "Copied Step ${step.id} with Hadith references", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
              .size(34.dp)
              .testTag("namaz_copy_step_${step.id}")
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy Step ${step.id}",
              tint = Color(0xFF0A4D3C),
              modifier = Modifier.size(18.dp)
            )
          }
          IconButton(
            onClick = onToggleBookmarked,
            modifier = Modifier
              .size(34.dp)
              .testTag("namaz_bookmark_step_${step.id}")
          ) {
            Icon(
              imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
              contentDescription = "Bookmark Step ${step.id}",
              tint = if (isBookmarked) Color(0xFFD4AF37) else Color(0xFF0A4D3C),
              modifier = Modifier.size(20.dp)
            )
          }
          IconButton(
            onClick = onToggleCompleted,
            modifier = Modifier
              .size(34.dp)
              .testTag("namaz_complete_step_${step.id}")
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Mark Step ${step.id} Completed",
              tint = if (isCompleted) Color(0xFF2E7D32) else Color.Gray.copy(alpha = 0.45f),
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      // Step Titles (English & Urdu stacked full-width so neither crushes the other into a 0px tall column)
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = step.titleEn,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFF0A4D3C),
          modifier = Modifier.fillMaxWidth()
        )
        if (step.titleUr.isNotBlank()) {
          Text(
            text = step.titleUr,
            style = CompactScriptStyle,
            fontFamily = UrduNastaliqFontFamily,
            fontSize = 15.sp,
            lineHeight = 25.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF073B2E),
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      // If step has Variants (e.g. Step 3 Thana, Step 4 Ta'awwudh, Step 21 Jalsah, Step 29 Dua before Salam), show selector tabs
      if (step.variants.isNotEmpty()) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          item {
            FilterChip(
              selected = selectedVariantIndex == -1,
              onClick = { selectedVariantIndex = -1 },
              label = { Text("Primary Narration (مسنون دعا ۱)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
              modifier = Modifier.testTag("step_${step.id}_variant_primary")
            )
          }
          itemsIndexed(step.variants) { vIdx, variant ->
            FilterChip(
              selected = selectedVariantIndex == vIdx,
              onClick = { selectedVariantIndex = vIdx },
              label = { Text("${variant.labelEn} (${variant.labelUr})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
              modifier = Modifier.testTag("step_${step.id}_variant_${vIdx + 1}")
            )
          }
        }
      }

      // If step has Multiple Options (e.g. Step 12 Ruku Tasbeehat, Step 19 Sujood Tasbeehat), show selector tabs
      if (step.options.isNotEmpty()) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          itemsIndexed(step.options) { oIdx, opt ->
            FilterChip(
              selected = selectedOptionIndex == oIdx,
              onClick = { selectedOptionIndex = oIdx },
              label = { Text("Option ${oIdx + 1} (${opt.labelUr})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
              modifier = Modifier.testTag("step_${step.id}_option_chip_${oIdx + 1}")
            )
          }
        }
      }

      val activeVariant = if (selectedVariantIndex in step.variants.indices) step.variants[selectedVariantIndex] else null
      val activeOption = if (step.options.isNotEmpty()) step.options[selectedOptionIndex.coerceIn(0, step.options.lastIndex)] else null

      val displayedArabic = activeVariant?.arabic ?: activeOption?.arabic ?: step.arabic
      val displayedUrdu = activeVariant?.urdu ?: activeOption?.urdu ?: step.urdu
      val displayedEnglish = activeVariant?.english ?: activeOption?.english ?: step.english
      val displayedRoman = activeVariant?.romanUrdu?.ifBlank { step.romanUrdu }
        ?: activeOption?.romanUrdu?.ifBlank { step.romanUrdu }
        ?: step.romanUrdu

      // (1) Arabic Recitation with Diacritics
      if (displayedArabic.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF0A4D3C).copy(alpha = 0.06f),
          border = BorderStroke(1.dp, Color(0xFF0A4D3C).copy(alpha = 0.18f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
              text = "1. Arabic Text with Diacritics (عربی متن مع اعراب)",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1B7A58)
            )
            Text(
              text = displayedArabic,
              style = CompactScriptStyle,
              fontFamily = AmiriFontFamily,
              fontSize = arabicFontSizeSp.sp,
              lineHeight = (arabicFontSizeSp * 1.6f).sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF073B2E),
              textAlign = TextAlign.End,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }

      // (2) Roman Urdu Transliteration (for beginners)
      if (showRomanUrdu && displayedRoman.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFFFF8E1).copy(alpha = 0.65f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
              text = "Roman Urdu Transliteration (تلفظ برائے مبتدئین):",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF5D4037)
            )
            Text(
              text = displayedRoman,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF3E2723)
            )
          }
        }
      }

      // (3) Urdu Translation (Right-to-Left)
      if (showUrdu && displayedUrdu.isNotBlank()) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "2. Urdu Translation & Meaning (اردو ترجمہ و مفہوم)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B7A58)
          )
          Text(
            text = displayedUrdu,
            style = CompactScriptStyle,
            fontFamily = UrduNastaliqFontFamily,
            fontSize = 15.sp,
            lineHeight = 25.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      // (4) Simple English Translation
      if (showEnglish && displayedEnglish.isNotBlank()) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "3. English Translation & Meaning",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B7A58)
          )
          Text(
            text = displayedEnglish,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      // (5) Physical Action & Posture Explanation + Recitation Instructions
      if (step.actionInstructionsEn.isNotBlank() || step.recitationInstructionsEn.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "Physical Posture & Recitation Guidance (طریقۂ عمل اور قراءت کی رہنمائی):",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF0A4D3C)
            )
            if (showUrdu && step.actionInstructionsUr.isNotBlank()) {
              Text(
                text = "• جسمانی عمل: ${step.actionInstructionsUr}",
                style = CompactScriptStyle,
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 13.sp,
                lineHeight = 22.sp,
                color = Color(0xFF073B2E),
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
              )
            }
            if (showEnglish && step.actionInstructionsEn.isNotBlank()) {
              Text(
                text = "• Physical Action: ${step.actionInstructionsEn}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            if (showUrdu && step.recitationInstructionsUr.isNotBlank()) {
              Text(
                text = "• تلاوت/ذکر کی ہدایت: ${step.recitationInstructionsUr}",
                style = CompactScriptStyle,
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 13.sp,
                lineHeight = 22.sp,
                color = Color(0xFF1B5E20),
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
              )
            }
            if (showEnglish && step.recitationInstructionsEn.isNotBlank()) {
              Text(
                text = "• Recitation Guidance: ${step.recitationInstructionsEn}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // All Options Overview Box (so all 3 Ruku/Sujood Tasbeehat remain visible & distinct)
      if (step.options.isNotEmpty()) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFE8F5E9).copy(alpha = 0.65f),
          border = BorderStroke(1.dp, Color(0xFF1B7A58).copy(alpha = 0.45f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("step_${step.id}_options_box")
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "All ${step.options.size} Verified Prophetic Options (Each kept separate — Recite any at least ${step.repeatMin} times):",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF0A4D3C)
            )
            step.options.forEachIndexed { idx, opt ->
              Surface(
                onClick = { selectedOptionIndex = idx },
                shape = RoundedCornerShape(8.dp),
                color = if (selectedOptionIndex == idx) Color.White else Color.White.copy(alpha = 0.7f),
                border = BorderStroke(
                  width = if (selectedOptionIndex == idx) 1.5.dp else 1.dp,
                  color = if (selectedOptionIndex == idx) Color(0xFF0A4D3C) else Color(0xFF0A4D3C).copy(alpha = 0.15f)
                ),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                  Text(
                    text = "Option ${idx + 1}:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0A4D3C)
                  )
                  Text(
                    text = opt.arabic,
                    style = CompactScriptStyle,
                    fontFamily = AmiriFontFamily,
                    fontSize = 19.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF073B2E),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                  )
                  if (showUrdu && opt.urdu.isNotBlank()) {
                    Text(
                      text = opt.urdu,
                      style = CompactScriptStyle,
                      fontFamily = UrduNastaliqFontFamily,
                      fontSize = 13.sp,
                      lineHeight = 22.sp,
                      textAlign = TextAlign.End,
                      modifier = Modifier.fillMaxWidth()
                    )
                  }
                  if (showEnglish && opt.english.isNotBlank()) {
                    Text(
                      text = opt.english,
                      style = MaterialTheme.typography.bodySmall
                    )
                  }
                  Text(
                    text = "Refs: ${opt.references.joinToString("; ") { it.formattedDisplay() }}",
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

      // Alternative Supplication Box ( kept visually present for tests & quick scanning)
      if (step.variants.isNotEmpty()) {
        val firstAlt = step.variants.first()
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF0A4D3C).copy(alpha = 0.05f),
          border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.7f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("step_${step.id}_alternative_box")
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "Alternative Authentic Supplication (متبادل مسنون دعا — الگ روایت):",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF0A4D3C)
            )
            Text(
              text = firstAlt.arabic,
              style = CompactScriptStyle,
              fontFamily = AmiriFontFamily,
              fontSize = 20.sp,
              lineHeight = 32.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF073B2E),
              textAlign = TextAlign.End,
              modifier = Modifier.fillMaxWidth()
            )
            if (showUrdu && firstAlt.urdu.isNotBlank()) {
              Text(
                text = firstAlt.urdu,
                style = CompactScriptStyle,
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 14.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
              )
            }
            if (showEnglish && firstAlt.english.isNotBlank()) {
              Text(
                text = firstAlt.english,
                style = MaterialTheme.typography.bodySmall
              )
            }
            Text(
              text = "Alternative References: ${firstAlt.references.joinToString("; ") { it.formattedDisplay() }}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0A4D3C)
            )
          }
        }
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

      // Expandable Verified Hadith References & Scholarly Notes Section
      val activeRefs = activeVariant?.references ?: activeOption?.references ?: step.references
      Surface(
        onClick = { referencesExpanded = !referencesExpanded },
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFFFF8E1),
        border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.7f)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("step_${step.id}_additional_refs")
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
                imageVector = Icons.Default.Verified,
                contentDescription = "Verified References",
                tint = Color(0xFF0A4D3C),
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "Verified Hadith References (${activeRefs.size} Primary Citations • Tap to ${if (referencesExpanded) "Collapse" else "Inspect Details"}):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF5D4037)
              )
            }
            Icon(
              imageVector = if (referencesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
              contentDescription = "Expand References",
              tint = Color(0xFF5D4037)
            )
          }

          // Summary line always visible
          Text(
            text = activeRefs.joinToString("; ") { it.formattedDisplay() },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF073B2E)
          )

          // Additional context notes always visible for clarity
          if (step.additionalNotes.isNotEmpty()) {
            step.additionalNotes.forEach { addNote ->
              Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (showUrdu && addNote.noteUr.isNotBlank()) {
                  Text(
                    text = "• ${addNote.noteUr}",
                    style = CompactScriptStyle,
                    fontFamily = UrduNastaliqFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 22.sp,
                    color = Color(0xFF4E342E),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                  )
                }
                if (showEnglish && addNote.noteEn.isNotBlank()) {
                  Text(
                    text = "• ${addNote.noteEn} — ${addNote.references.joinToString("; ") { it.formattedDisplay() }}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF3E2723)
                  )
                }
              }
            }
          }

          // Expanded per-reference audit breakdown with status badge & clickable source link
          AnimatedVisibility(visible = referencesExpanded) {
            Column(
              modifier = Modifier.padding(top = 4.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              activeRefs.forEach { ref ->
                AuditedReferenceBadgeRow(
                  ref = ref,
                  onOpenUrl = { url ->
                    runCatching { uriHandler.openUri(url) }
                  }
                )
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
private fun AuditedReferenceBadgeRow(
  ref: AuditedHadithReference,
  onOpenUrl: (String) -> Unit
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = Color.White,
    border = BorderStroke(1.dp, Color(0xFF0A4D3C).copy(alpha = 0.2f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(8.dp),
      verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = ref.book,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFF0A4D3C)
        )
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = when (ref.verificationStatus) {
            ReferenceVerificationStatus.VERIFIED -> Color(0xFFE8F5E9)
            ReferenceVerificationStatus.NUMBERING_DIFFERENCE -> Color(0xFFE3F2FD)
            ReferenceVerificationStatus.GRADING_DIFFERENCE -> Color(0xFFFFF3E0)
            ReferenceVerificationStatus.INCORRECT_REFERENCE -> Color(0xFFFFEBEE)
            ReferenceVerificationStatus.VERIFICATION_PENDING -> Color(0xFFF5F5F5)
          }
        ) {
          Text(
            text = "${ref.verificationStatus.badgeLabel} (${ref.verificationStatus.jsonKey})",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = when (ref.verificationStatus) {
              ReferenceVerificationStatus.VERIFIED -> Color(0xFF1B5E20)
              ReferenceVerificationStatus.NUMBERING_DIFFERENCE -> Color(0xFF0D47A1)
              ReferenceVerificationStatus.GRADING_DIFFERENCE -> Color(0xFFE65100)
              ReferenceVerificationStatus.INCORRECT_REFERENCE -> Color(0xFFB71C1C)
              ReferenceVerificationStatus.VERIFICATION_PENDING -> Color(0xFF424242)
            },
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
        if (ref.volumePage.isNotBlank()) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFF3E5F5)
          ) {
            Text(
              text = "Vol/Page: ${ref.volumePage}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF4A148C),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      Text(
        text = "Hadith / Ayah #: ${ref.internationalNumber.ifBlank { ref.hadithOrAyah }} • Grade: ${ref.grade}${if (ref.gradingScholar.isNotBlank()) " (${ref.gradingScholar})" else ""}",
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFF3E2723)
      )

      if (ref.sourceUrl.isNotBlank()) {
        Surface(
          onClick = { onOpenUrl(ref.sourceUrl) },
          shape = RoundedCornerShape(6.dp),
          color = Color(0xFF0A4D3C).copy(alpha = 0.08f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.OpenInNew,
              contentDescription = "Open Primary Source",
              tint = Color(0xFF0A4D3C),
              modifier = Modifier.size(13.dp)
            )
            Text(
              text = "Verify in Primary Source: ${ref.sourceUrl}",
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

@Composable
fun NamazDailyPrayerStructuresSection() {
  val structures = remember { NamazMuhammadiModuleData.dailyPrayerStructures }
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.5.dp, Color(0xFF0A4D3C)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("namaz_daily_prayer_structures_card")
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
          text = "Five Daily Prayers: Fard, Sunnah Mu'akkadah & Witr Structure",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFF0A4D3C),
          modifier = Modifier.fillMaxWidth()
        )
        Text(
          text = "پانچ فرض نمازوں، سنتِ مؤکدہ اور وتر کی رکعات کا نقشہ",
          style = CompactScriptStyle,
          fontFamily = UrduNastaliqFontFamily,
          fontSize = 13.sp,
          lineHeight = 22.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF073B2E),
          textAlign = TextAlign.End,
          modifier = Modifier.fillMaxWidth()
        )
      }

      structures.forEach { spec ->
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFF4FBF7),
          border = BorderStroke(1.dp, Color(0xFF1B7A58).copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              Text(
                text = spec.nameEn,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0A4D3C),
                modifier = Modifier.fillMaxWidth()
              )
              Text(
                text = spec.nameUr,
                style = CompactScriptStyle,
                fontFamily = UrduNastaliqFontFamily,
                fontSize = 14.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF073B2E),
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
              )
            }
            Text(
              text = "• Fard: ${spec.fardRakats}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF073B2E)
            )
            Text(
              text = "• Sunnah Before Fard: ${spec.sunnahMuakkadahBefore}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "• Sunnah After Fard: ${spec.sunnahMuakkadahAfter}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface
            )
            if (spec.witrOrNafl != "None") {
              Text(
                text = "• Witr / Nafl: ${spec.witrOrNafl}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF5D4037)
              )
            }
            Text(
              text = "Step Sequence: ${spec.stepFlowSummary}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1B7A58)
            )
          }
        }
      }
    }
  }
}

@Composable
fun NamazReferenceAuditReportSection() {
  val context = LocalContext.current
  val report = remember { NamazMuhammadiModuleData.getModuleAuditReport(context) }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
    border = BorderStroke(1.5.dp, Color(0xFFD4AF37)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("namaz_reference_audit_report_card")
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(
        text = "Namaz-e-Muhammadi ﷺ Reference & Schema Audit Report (${report.auditDate})",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF0A4D3C)
      )
      Text(
        text = report.schemaValidationSummary,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1B5E20)
      )
      Text(
        text = "Total Steps: ${report.totalSteps} (${report.originalBookletStepsCount} Original Booklet + ${report.researchedAdditionStepsCount} Researched Additions) • Total Citations Audited: ${report.totalReferencesAudited} (Verified: ${report.verifiedCount}, Numbering Differences Cross-Linked: ${report.numberingDifferenceCount}, Grading Differences Explained: ${report.gradingDifferenceCount}, Shifted References Corrected: ${report.correctedShiftedReferencesCount})",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF3E2723)
      )

      report.keyFindings.forEach { finding ->
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color.White,
          border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.6f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
          ) {
            Text(
              text = "${finding.stepOrSectionLabel} [${finding.status.badgeLabel}]",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF0A4D3C)
            )
            Text(
              text = "• Booklet Citation: ${finding.originalBookletCitation}",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF5D4037)
            )
            Text(
              text = "• Verified Primary Citation: ${finding.verifiedPrimaryCitation}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1B5E20)
            )
            Text(
              text = finding.explanation,
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF3E2723)
            )
          }
        }
      }
    }
  }
}
