package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.SahihBadgeColor
import com.example.ui.theme.ScheherazadeFontFamily

const val WHATSAPP_SUPPORT_URL = "https://wa.me/7889632693?text=Assalamu%20Alaikum"

fun openWhatsAppSupport(context: Context) {
  try {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(WHATSAPP_SUPPORT_URL)).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
  } catch (_: Exception) {
    Toast.makeText(
      context,
      "Assalamu Alaikum! Support WhatsApp: +91 7889632693 (wa.me/7889632693)",
      Toast.LENGTH_LONG
    ).show()
  }
}

@Composable
fun BismillahHeader(
  modifier: Modifier = Modifier,
  showBorder: Boolean = true,
  surahNumber: Int? = null,
  audioState: com.example.ui.audio.AudioState? = null,
  currentReciter: com.example.ui.audio.Reciter? = null,
  isTajweedEnabled: Boolean = false,
  tajweedRulesMap: Map<String, com.example.data.repository.QuranTajweedEngineRepository.TajweedRuleCategory> = emptyMap(),
  selectedTajweedRuleFilter: String? = null,
  onPlayBasmala: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val effectiveReciter = currentReciter ?: audioState?.reciter
  val isSurahReaderMode = surahNumber != null && surahNumber != 9

  val isReciterRecitingBasmalaInSurah = if (isSurahReaderMode && effectiveReciter != null && surahNumber != null) {
    com.example.ui.audio.TajweedAudioSyncEngine.doesReciterReciteBasmalaInSurah(
      context = context,
      reciter = effectiveReciter,
      surahNumber = surahNumber,
      activeAyahNumber = if (audioState?.surahNumber == surahNumber) (audioState.ayahNumber ?: 1) else 1,
      fileDurationMs = (audioState?.durationMs ?: 0).toLong()
    )
  } else {
    true
  }

  val isSurahAudioActive = isSurahReaderMode &&
    audioState != null &&
    (audioState.isPlaying || audioState.isBuffering) &&
    audioState.surahNumber == surahNumber

  val isActivelyPlayingBasmala = isSurahAudioActive &&
    audioState != null &&
    (audioState.ayahNumber == 0 ||
      (surahNumber == 1 && audioState.ayahNumber == 1) ||
      (audioState.ayahNumber == 1 && audioState.isRecitingBasmalaPrefix))

  val wordSyncResult = if (isActivelyPlayingBasmala && audioState != null && effectiveReciter != null && surahNumber != null) {
    com.example.ui.audio.TajweedAudioSyncEngine.computeSyncedWordIndex(
      context = context,
      reciter = effectiveReciter,
      surahNumber = surahNumber,
      ayahNumber = if (audioState.ayahNumber == 0) 0 else 1,
      arabicWords = com.example.ui.audio.TajweedAudioSyncEngine.CANONICAL_BASMALA_WORDS,
      currentPositionMs = audioState.currentPositionMs.toLong(),
      durationMs = audioState.durationMs.toLong().coerceAtLeast(1000L),
      isBuffering = audioState.isBuffering
    )
  } else {
    null
  }

  val activeBasmalaWordIdx = wordSyncResult?.activeBasmalaWordIndex ?: -1

  // Default color is Black (onSurface); during recitation, turn Green when recited or Red when not recited
  val statusGreen = Color(0xFF00C853)
  val statusDarkGreen = Color(0xFF1B5E20)
  val statusRed = Color(0xFFD32F2F)
  val defaultBlackColor = MaterialTheme.colorScheme.onSurface

  val borderColor = when {
    !isSurahAudioActive -> GoldPrimary.copy(alpha = 0.5f)
    isActivelyPlayingBasmala -> statusGreen
    isReciterRecitingBasmalaInSurah -> statusGreen.copy(alpha = 0.8f)
    else -> statusRed.copy(alpha = 0.8f)
  }

  val baseArabicColor = when {
    !isSurahAudioActive -> defaultBlackColor
    isReciterRecitingBasmalaInSurah || isActivelyPlayingBasmala -> statusDarkGreen
    else -> statusRed
  }

  val basmalaAnnotatedText = androidx.compose.runtime.remember(
    isSurahReaderMode,
    isSurahAudioActive,
    isTajweedEnabled,
    tajweedRulesMap,
    selectedTajweedRuleFilter,
    isReciterRecitingBasmalaInSurah,
    isActivelyPlayingBasmala,
    activeBasmalaWordIdx,
    baseArabicColor
  ) {
    val basmalaRecord = com.example.data.repository.QuranTajweedEngineRepository.getCanonicalBasmalaTajweedRecord(context)
    if (isTajweedEnabled && tajweedRulesMap.isNotEmpty()) {
      com.example.data.repository.QuranTajweedEngineRepository.buildSafeTajweedAnnotatedString(
        record = basmalaRecord,
        fallbackArabicText = basmalaRecord.textUthmani,
        rulesMap = tajweedRulesMap,
        baseTextColor = baseArabicColor,
        selectedRuleFilter = selectedTajweedRuleFilter,
        isAudioPlayingOnThisVerse = isActivelyPlayingBasmala,
        activeWordIndex = activeBasmalaWordIdx,
        activeWordHighlightColor = statusGreen,
        recitedWordColor = statusDarkGreen
      )
    } else if (isActivelyPlayingBasmala && activeBasmalaWordIdx >= 0) {
      androidx.compose.ui.text.buildAnnotatedString {
        val words = com.example.ui.audio.TajweedAudioSyncEngine.CANONICAL_BASMALA_WORDS
        words.forEachIndexed { idx, w ->
          val wColor = when {
            idx == activeBasmalaWordIdx -> statusGreen
            idx < activeBasmalaWordIdx -> statusDarkGreen
            else -> baseArabicColor
          }
          pushStyle(
            androidx.compose.ui.text.SpanStyle(
              color = wColor,
              background = if (idx == activeBasmalaWordIdx) statusGreen.copy(alpha = 0.22f) else Color.Transparent,
              fontWeight = if (idx == activeBasmalaWordIdx) FontWeight.Bold else FontWeight.Normal
            )
          )
          append(w)
          pop()
          if (idx < words.lastIndex) append(" ")
        }
      }
    } else {
      androidx.compose.ui.text.AnnotatedString(
        text = basmalaRecord.textUthmani,
        spanStyle = androidx.compose.ui.text.SpanStyle(color = baseArabicColor)
      )
    }
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag(if (surahNumber != null) "bismillah_starting_verse_$surahNumber" else "bismillah_header_card"),
    colors = CardDefaults.cardColors(
      containerColor = when {
        isActivelyPlayingBasmala -> statusGreen.copy(alpha = 0.12f)
        isSurahAudioActive && isReciterRecitingBasmalaInSurah -> statusGreen.copy(alpha = 0.08f)
        isSurahAudioActive && !isReciterRecitingBasmalaInSurah -> statusRed.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surfaceVariant
      }
    ),
    shape = RoundedCornerShape(16.dp),
    border = if (showBorder) {
      CardDefaults.outlinedCardBorder().copy(
        width = if (isSurahAudioActive) 1.8.dp else 1.dp,
        brush = androidx.compose.ui.graphics.SolidColor(borderColor)
      )
    } else null
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp, horizontal = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = EmeraldPrimary.copy(alpha = 0.14f),
        modifier = Modifier
          .clickable { openWhatsAppSupport(context) }
          .testTag("clickable_assalamu_alaikum_greeting")
      ) {
        Text(
          text = "السَّلَامُ عَلَيْكُمْ • Assalamu Alaikum — Support Our App",
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
          maxLines = 1,
          overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = EmeraldPrimary
          )
        )
      }

      if (isSurahReaderMode && effectiveReciter != null) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
          verticalAlignment = Alignment.CenterVertically
        ) {
          val badgeBgColor = when {
            isSurahAudioActive && (isReciterRecitingBasmalaInSurah || isActivelyPlayingBasmala) -> statusGreen.copy(alpha = 0.18f)
            isSurahAudioActive && !isReciterRecitingBasmalaInSurah -> statusRed.copy(alpha = 0.18f)
            else -> MaterialTheme.colorScheme.surface
          }
          val badgeTextColor = when {
            isSurahAudioActive && (isReciterRecitingBasmalaInSurah || isActivelyPlayingBasmala) -> statusDarkGreen
            isSurahAudioActive && !isReciterRecitingBasmalaInSurah -> statusRed
            else -> defaultBlackColor
          }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = badgeBgColor,
            modifier = Modifier
              .weight(1f, fill = false)
              .testTag("bismillah_reciter_status_badge")
          ) {
            Text(
              text = when {
                isSurahAudioActive && (isReciterRecitingBasmalaInSurah || isActivelyPlayingBasmala) ->
                  "● Bismillah Recited (${effectiveReciter.name}) • GREEN"
                isSurahAudioActive && !isReciterRecitingBasmalaInSurah ->
                  "● Bismillah Not Recited (${effectiveReciter.name}) • RED"
                isReciterRecitingBasmalaInSurah ->
                  "Bismillah Recited by ${effectiveReciter.name}"
                else ->
                  "Bismillah Not in Stream (${effectiveReciter.name})"
              },
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              maxLines = 1,
              overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = badgeTextColor
              )
            )
          }

          if (onPlayBasmala != null) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isActivelyPlayingBasmala) statusGreen.copy(alpha = 0.2f) else EmeraldPrimary.copy(alpha = 0.14f),
              modifier = Modifier
                .clickable { onPlayBasmala() }
                .testTag("play_starting_bismillah_button")
            ) {
              Text(
                text = if (isActivelyPlayingBasmala) "♫ Reciting..." else "▶ Recite",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                maxLines = 1,
                softWrap = false,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isActivelyPlayingBasmala) statusDarkGreen else EmeraldPrimary
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = basmalaAnnotatedText,
        style = MaterialTheme.typography.headlineMedium.copy(
          fontFamily = AmiriFontFamily,
          fontSize = 26.sp,
          lineHeight = 44.sp,
          fontWeight = FontWeight.Normal,
          color = baseArabicColor,
          textAlign = TextAlign.Center
        ),
        modifier = Modifier.testTag("bismillah_arabic_text")
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "In the name of Allah, the Entirely Merciful, the Especially Merciful",
        style = MaterialTheme.typography.bodySmall.copy(
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )
      )
      if (isSurahReaderMode) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "شروع اللہ کے نام سے جو بڑا مہربان نہایت رحم والا ہے",
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 11.sp,
            color = when {
              isSurahAudioActive && (isReciterRecitingBasmalaInSurah || isActivelyPlayingBasmala) -> statusDarkGreen
              isSurahAudioActive && !isReciterRecitingBasmalaInSurah -> statusRed
              else -> defaultBlackColor.copy(alpha = 0.8f)
            },
            textAlign = TextAlign.Center
          )
        )
      }
    }
  }
}

@Composable
fun AyahNumberBadge(
  verseNumber: Int,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .size(36.dp)
      .border(1.5.dp, GoldPrimary, RoundedCornerShape(8.dp))
      .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "$verseNumber",
      style = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    )
  }
}

@Composable
fun SahihGradeBadge(
  grade: String,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor) = when (grade.lowercase()) {
    "sahih" -> Pair(SahihBadgeColor.copy(alpha = 0.15f), SahihBadgeColor)
    "hasan" -> Pair(GoldPrimary.copy(alpha = 0.15f), GoldPrimary)
    else -> Pair(Color.Gray.copy(alpha = 0.15f), Color.Gray)
  }

  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(6.dp),
    color = bgColor
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(textColor)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = grade,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.SemiBold,
          color = textColor
        )
      )
    }
  }
}

@Composable
fun RevelationTypeBadge(
  type: String,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(6.dp),
    color = EmeraldContainer
  ) {
    Text(
      text = type,
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = OnEmeraldContainer
      )
    )
  }
}
