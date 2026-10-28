package com.example.ui.quran

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.local.entities.AyahEntity
import com.example.data.local.entities.SurahEntity
import com.example.ui.audio.AudioState
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TajweedPracticeDialog(
  ayah: AyahEntity,
  surah: SurahEntity,
  cleanArabicText: String,
  currentReciterName: String,
  audioState: AudioState = AudioState(),
  onPlayAuthenticReciter: () -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val recorder = remember { TajweedVoiceRecorder(context) }
  val recordingState by recorder.state.collectAsState()

  // Default to Left-to-Right word sequence (Word 1 -> Word 2 -> Word 3 -> Word 4 from left to right)
  // as requested by user, with 1-tap toggle to switch to RTL if desired.
  var leftToRightWordOrder by remember { mutableStateOf(true) }

  val canonicalWords = remember(cleanArabicText) {
    cleanArabicText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
  }

  // Live pacing index while user is actively recording so words light up in green from left to right as they recite
  var activeRecordingWordIndex by remember { mutableIntStateOf(-1) }
  LaunchedEffect(recordingState.isListening, canonicalWords.size) {
    if (recordingState.isListening && canonicalWords.isNotEmpty()) {
      activeRecordingWordIndex = 0
      for (i in canonicalWords.indices) {
        activeRecordingWordIndex = i
        val charWeightMs = (canonicalWords[i].length * 160L).coerceIn(550L, 1400L)
        delay(charWeightMs)
      }
    } else {
      activeRecordingWordIndex = -1
    }
  }

  val isReciterPlayingThisAyah = audioState.isPlaying &&
    audioState.surahNumber == ayah.surahNumber &&
    audioState.ayahNumber == ayah.verseNumber

  val activeReciterWordIndex = remember(
    isReciterPlayingThisAyah,
    audioState.currentPositionMs,
    audioState.durationMs,
    canonicalWords
  ) {
    if (!isReciterPlayingThisAyah || canonicalWords.isEmpty()) {
      -1
    } else {
      val duration = audioState.durationMs.coerceAtLeast(1)
      val progress = (audioState.currentPositionMs.toFloat() / duration.toFloat()).coerceIn(0f, 0.999f)
      val weights = canonicalWords.map { it.length.coerceAtLeast(2).toFloat() }
      val totalWeight = weights.sum().coerceAtLeast(1f)
      val targetWeight = progress * totalWeight
      var cumulative = 0f
      var foundIdx = canonicalWords.lastIndex
      for (idx in weights.indices) {
        cumulative += weights[idx]
        if (targetWeight <= cumulative) {
          foundIdx = idx
          break
        }
      }
      foundIdx
    }
  }

  val liveHighlightedWordIndex = when {
    recordingState.isListening -> activeRecordingWordIndex
    isReciterPlayingThisAyah -> activeReciterWordIndex
    else -> -1
  }

  DisposableEffect(Unit) {
    onDispose {
      recorder.stopListening()
    }
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      recorder.startListening(
        surah = ayah.surahNumber,
        ayah = ayah.verseNumber,
        canonicalVerseText = cleanArabicText
      )
    } else {
      Toast.makeText(
        context,
        "Microphone permission is required to analyze your recitation.",
        Toast.LENGTH_LONG
      ).show()
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .padding(vertical = 16.dp)
        .testTag("tajweed_practice_dialog"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(18.dp)
      ) {
        // Top Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Surface(
              shape = CircleShape,
              color = EmeraldPrimary.copy(alpha = 0.18f),
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Tajweed Recitation Practice",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "${surah.nameEnglish} • Ayah ${ayah.verseNumber} (${ayah.surahNumber}:${ayah.verseNumber})",
                style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontWeight = FontWeight.SemiBold)
              )
            }
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Word-by-Word Green Highlighted Connected Ayah Banner + Order Toggle
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            // Order Header Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (leftToRightWordOrder) {
                  "Word Sequence: Left → Right (1 → ${canonicalWords.size})"
                } else {
                  "Word Sequence: Arabic RTL (1 ← ${canonicalWords.size})"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color(0xFF00E676),
                  fontWeight = FontWeight.Bold
                )
              )
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = EmeraldPrimary.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f)),
                modifier = Modifier
                  .clickable { leftToRightWordOrder = !leftToRightWordOrder }
                  .testTag("tajweed_word_order_toggle")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Toggle Word Order",
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (leftToRightWordOrder) "L→R Order" else "R→L Order",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E676)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Connected Arabic Verse with real-time word-by-word green highlighting
            val eval = recordingState.evaluationResult
            val annotatedVerse = remember(canonicalWords, liveHighlightedWordIndex, eval) {
              buildAnnotatedString {
                canonicalWords.forEachIndexed { index, word ->
                  val wordGrade = eval?.wordStatuses?.getOrNull(index)?.grade
                  val isCurrentActive = index == liveHighlightedWordIndex
                  val isAlreadySpoken = liveHighlightedWordIndex > index
                  val wordColor = when {
                    isCurrentActive -> Color(0xFF00E676)
                    isAlreadySpoken -> Color(0xFF4CAF50)
                    wordGrade == TajweedWordGrade.CORRECT -> Color(0xFF00E676)
                    wordGrade == TajweedWordGrade.SMALL_MISTAKE -> Color(0xFFFFD54F)
                    wordGrade == TajweedWordGrade.WRONG_OR_MISSED -> Color(0xFFFF5252)
                    else -> Color.Unspecified
                  }
                  withStyle(
                    SpanStyle(
                      color = wordColor,
                      fontWeight = if (isCurrentActive) FontWeight.Bold else FontWeight.Normal,
                      background = if (isCurrentActive) Color(0xFF00E676).copy(alpha = 0.18f) else Color.Transparent
                    )
                  ) {
                    append(word)
                  }
                  if (index < canonicalWords.lastIndex) {
                    append(" ")
                  }
                }
              }
            }

            Text(
              text = annotatedVerse,
              style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = AmiriFontFamily,
                fontSize = 24.sp,
                lineHeight = 44.sp,
                textAlign = TextAlign.Center
              ),
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val displayWordStatuses = if (eval != null) {
              eval.wordStatuses
            } else {
              canonicalWords.mapIndexed { index, word ->
                TajweedWordStatus(index = index, canonicalWord = word, grade = TajweedWordGrade.PENDING)
              }
            }

            // Render word cards in strictly proper left-to-right (1, 2, 3...) or RTL order based on toggle
            val flowDirection = if (leftToRightWordOrder) LayoutDirection.Ltr else LayoutDirection.Rtl
            CompositionLocalProvider(LocalLayoutDirection provides flowDirection) {
              FlowRow(
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("tajweed_word_display_flow"),
                horizontalArrangement = Arrangement.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                displayWordStatuses.forEach { wordStatus ->
                  val isLiveActiveWord = wordStatus.index == liveHighlightedWordIndex
                  val isLiveCompletedWord = liveHighlightedWordIndex > wordStatus.index

                  val bgColor = when {
                    isLiveActiveWord -> Color(0xFF1B5E20)
                    isLiveCompletedWord && wordStatus.grade == TajweedWordGrade.PENDING -> Color(0xFFE8F5E9)
                    wordStatus.grade == TajweedWordGrade.CORRECT -> Color(0xFFE8F5E9)
                    wordStatus.grade == TajweedWordGrade.SMALL_MISTAKE -> Color(0xFFFFF9C4)
                    wordStatus.grade == TajweedWordGrade.WRONG_OR_MISSED -> Color(0xFFFFEBEE)
                    else -> MaterialTheme.colorScheme.surface
                  }
                  val borderColor = when {
                    isLiveActiveWord -> Color(0xFF00E676)
                    isLiveCompletedWord && wordStatus.grade == TajweedWordGrade.PENDING -> Color(0xFF4CAF50)
                    wordStatus.grade == TajweedWordGrade.CORRECT -> Color(0xFF2E7D32)
                    wordStatus.grade == TajweedWordGrade.SMALL_MISTAKE -> Color(0xFFFBC02D)
                    wordStatus.grade == TajweedWordGrade.WRONG_OR_MISSED -> Color(0xFFD32F2F)
                    else -> MaterialTheme.colorScheme.outlineVariant
                  }
                  val textColor = when {
                    isLiveActiveWord -> Color(0xFF00E676)
                    isLiveCompletedWord && wordStatus.grade == TajweedWordGrade.PENDING -> Color(0xFF1B5E20)
                    wordStatus.grade == TajweedWordGrade.CORRECT -> Color(0xFF1B5E20)
                    wordStatus.grade == TajweedWordGrade.SMALL_MISTAKE -> Color(0xFFE65100)
                    wordStatus.grade == TajweedWordGrade.WRONG_OR_MISSED -> Color(0xFFD32F2F)
                    else -> MaterialTheme.colorScheme.onSurface
                  }
                  val label = when {
                    isLiveActiveWord -> "Reciting #${wordStatus.index + 1}"
                    isLiveCompletedWord && wordStatus.grade == TajweedWordGrade.PENDING -> "Done #${wordStatus.index + 1}"
                    wordStatus.grade == TajweedWordGrade.CORRECT -> "Correct"
                    wordStatus.grade == TajweedWordGrade.SMALL_MISTAKE -> "Small Mistake"
                    wordStatus.grade == TajweedWordGrade.WRONG_OR_MISSED -> "Wrong / Missed"
                    else -> "Word #${wordStatus.index + 1}"
                  }

                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = bgColor,
                    border = BorderStroke(if (isLiveActiveWord) 2.dp else 1.5.dp, borderColor),
                    modifier = Modifier
                      .padding(horizontal = 3.dp, vertical = 2.dp)
                      .testTag("tajweed_word_${wordStatus.index}_${wordStatus.grade.name.lowercase()}")
                  ) {
                    Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                      Text(
                        text = "#${wordStatus.index + 1}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLiveActiveWord) Color.White else GoldPrimary
                      )
                      Text(
                        text = wordStatus.canonicalWord,
                        fontFamily = AmiriFontFamily,
                        fontSize = 22.sp,
                        fontWeight = if (wordStatus.grade == TajweedWordGrade.CORRECT && !isLiveActiveWord) FontWeight.Normal else FontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.Center
                      )
                      Text(
                        text = label,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                      )
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Canonical Uthmani Arabic • Words ordered #1 → #${canonicalWords.size} from left to right for clear step-by-step tracking",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Evaluation Summary & Accuracy Badge (High-contrast colors for both Dark & Light themes)
        recordingState.evaluationResult?.let { eval ->
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = when {
              eval.wrongOrMissedCount == 0 && eval.smallMistakeCount == 0 -> Color(0xFF1B5E20).copy(alpha = 0.28f)
              eval.wrongOrMissedCount == 0 -> Color(0xFFFFF9C4).copy(alpha = 0.25f)
              else -> Color(0xFFFFEBEE).copy(alpha = 0.25f)
            },
            border = BorderStroke(
              1.dp,
              when {
                eval.accuracyPercentage >= 80 -> Color(0xFF00E676)
                eval.accuracyPercentage >= 50 -> Color(0xFFFFB300)
                else -> Color(0xFFFF5252)
              }
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("tajweed_evaluation_summary_card")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
            ) {
              Text(
                text = "Recitation Accuracy: ${eval.accuracyPercentage}% (${eval.matchedCount}/${eval.totalCount} Correct)",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = when {
                    eval.accuracyPercentage >= 80 -> Color(0xFF00E676)
                    eval.accuracyPercentage >= 50 -> Color(0xFFFFB300)
                    else -> Color(0xFFFF5252)
                  }
                )
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = eval.feedbackSummary,
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface)
              )

              Spacer(modifier = Modifier.height(8.dp))

              // 3-Color Legend: Green (Correct), Yellow (Small Mistake), Red (Wrong of text)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = Color(0xFFE8F5E9),
                  border = BorderStroke(1.dp, Color(0xFF2E7D32))
                ) {
                  Text(
                    text = "● ${eval.matchedCount} Correct",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }

                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = Color(0xFFFFF9C4),
                  border = BorderStroke(1.dp, Color(0xFFFBC02D))
                ) {
                  Text(
                    text = "● ${eval.smallMistakeCount} Small Mistake",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }

                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = Color(0xFFFFEBEE),
                  border = BorderStroke(1.dp, Color(0xFFD32F2F))
                ) {
                  Text(
                    text = "● ${eval.wrongOrMissedCount} Wrong",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }
          Spacer(modifier = Modifier.height(12.dp))
        }

        // Active Listening or Error Status Banner
        if (recordingState.isListening) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1B5E20).copy(alpha = 0.25f),
            border = BorderStroke(1.dp, Color(0xFF00E676)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF00E676), strokeWidth = 2.dp)
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Listening... Recite word #1 → #${canonicalWords.size} aloud in Arabic now.",
                style = MaterialTheme.typography.bodySmall.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFF00E676)
                )
              )
            }
          }
          Spacer(modifier = Modifier.height(12.dp))
        } else if (recordingState.errorMessage != null) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = recordingState.errorMessage ?: "",
              modifier = Modifier.padding(10.dp),
              style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.error)
            )
          }
          Spacer(modifier = Modifier.height(12.dp))
        }

        // Recording & Playback Action Buttons (Unclipped minHeight 50.dp)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (recordingState.isListening) {
            Button(
              onClick = { recorder.stopListening() },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 50.dp)
                .testTag("tajweed_stop_recording_button")
            ) {
              Icon(Icons.Default.MicOff, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Stop & Check", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
            }
          } else {
            Button(
              onClick = {
                val hasPermission = ContextCompat.checkSelfPermission(
                  context,
                  Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                  recorder.startListening(
                    surah = ayah.surahNumber,
                    ayah = ayah.verseNumber,
                    canonicalVerseText = cleanArabicText
                  )
                } else {
                  permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 50.dp)
                .testTag("tajweed_record_button")
            ) {
              Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (recordingState.evaluationResult != null) "Re-Record" else "Record Recitation",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          // Listen to Authentic Human Reciter Button (Strictly No AI voice)
          OutlinedButton(
            onClick = {
              onPlayAuthenticReciter()
            },
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 50.dp)
              .testTag("tajweed_listen_reciter_button")
          ) {
            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Listen: ${currentReciterName.substringBefore(" ")}",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Strict Authenticity & Privacy Notice
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "• Zero AI-Generated Voice: Recitation is played strictly by verified human reciters ($currentReciterName).\n" +
              "• Words light up in GREEN word-by-word while reciting or listening; missed words are highlighted in RED.\n" +
              "• Zero alteration to original Quranic Arabic text.",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 9.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }
    }
  }
}
