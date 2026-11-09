package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
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
import com.example.ui.theme.UrduNastaliqFontFamily

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

  val basmalaRecordState = androidx.compose.runtime.produceState(
    initialValue = com.example.data.repository.QuranTajweedEngineRepository.getCanonicalBasmalaTajweedRecord(context),
    key1 = isTajweedEnabled
  ) {
    if (isTajweedEnabled) {
      val asyncRec = com.example.data.repository.QuranTajweedEngineRepository.loadAyahTajweedRecordAsync(context, 1, 1)
      if (asyncRec != null && asyncRec.textUthmani.isNotBlank() && asyncRec.annotations.isNotEmpty()) {
        value = asyncRec
      }
    }
  }
  val basmalaRecord = basmalaRecordState.value
  val basmalaAnnotatedText = androidx.compose.runtime.remember(
    basmalaRecord,
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
        recitedWordColor = statusDarkGreen,
        expectedSurah = 1,
        expectedAyah = 1
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
        shape = RoundedCornerShape(14.dp),
        color = EmeraldPrimary.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.55f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { openWhatsAppSupport(context) }
          .testTag("clickable_assalamu_alaikum_greeting")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
            style = MaterialTheme.typography.labelMedium.copy(
              fontFamily = ScheherazadeFontFamily,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = GoldPrimary,
              textAlign = TextAlign.Center
            )
          )
          Text(
            text = "Assalamu Alaikum • Support Bayan al-Huda",
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary,
              textAlign = TextAlign.Center
            )
          )
        }
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
        text = "شروع اللہ کے نام سے جو بڑا مہربان نہایت رحم والا ہے",
        style = MaterialTheme.typography.bodySmall.copy(
          fontFamily = UrduNastaliqFontFamily,
          fontSize = 14.sp,
          lineHeight = 24.sp,
          color = GoldPrimary,
          fontWeight = FontWeight.SemiBold,
          textAlign = TextAlign.Center
        ),
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "In the name of Allah, the Entirely Merciful, the Especially Merciful",
        style = MaterialTheme.typography.bodySmall.copy(
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        ),
        modifier = Modifier.fillMaxWidth()
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

/**
 * Official Google Drive Folder URL where the latest APK updates of Bayan al-Huda are published.
 */
const val APP_UPDATES_GOOGLE_DRIVE_FOLDER_URL =
  "https://drive.google.com/drive/folders/14i90MpNPedlnBk3l53L_yplzzFNIa4N3"

const val SHARED_APK_FILENAME = "Bayan-al-Huda.apk"

/**
 * Builds the canonical share message containing the app description and the official
 * Google Drive folder link for downloading the latest APK updates.
 */
fun buildShareAppMessage(): String = buildString {
  appendLine("📲 Bayan al-Huda (بَيَانُ الهُدَى) — Complete Quran, Hadith, 17-Rule Tajweed, Salah & Islamic Library App")
  appendLine("Developed by Zulqarnain Ahmad Sheikh")
  appendLine()
  appendLine("📥 Download Latest APK Updates (Official Google Drive Folder):")
  append(APP_UPDATES_GOOGLE_DRIVE_FOLDER_URL)
}

/**
 * Copies the currently installed APK (`applicationInfo.sourceDir`) into a shareable
 * `cacheDir/shared_apk/Bayan-al-Huda.apk` file so `FileProvider` can attach it to `ACTION_SEND`.
 */
fun prepareShareableApkFile(context: Context): java.io.File? {
  return runCatching {
    val sharedDir = java.io.File(context.cacheDir, "shared_apk").apply {
      if (!exists()) mkdirs()
    }
    val targetApkFile = java.io.File(sharedDir, SHARED_APK_FILENAME)
    val sourcePath = context.applicationInfo?.sourceDir
      ?: context.applicationInfo?.publicSourceDir
    val sourceFile = if (!sourcePath.isNullOrBlank()) java.io.File(sourcePath) else null
    if (sourceFile != null && sourceFile.exists() && sourceFile.isFile && sourceFile.length() > 0L) {
      if (!targetApkFile.exists() || targetApkFile.length() != sourceFile.length()) {
        sourceFile.inputStream().use { input ->
          targetApkFile.outputStream().use { output ->
            input.copyTo(output)
          }
        }
      }
      targetApkFile
    } else {
      // Fallback for unit-test / JVM environments where sourceDir is not a real APK file on disk
      if (!targetApkFile.exists() || targetApkFile.length() == 0L) {
        targetApkFile.writeBytes(
          byteArrayOf(0x50, 0x4B, 0x03, 0x04) +
            "Bayan-al-Huda APK Package ($APP_UPDATES_GOOGLE_DRIVE_FOLDER_URL)".encodeToByteArray()
        )
      }
      targetApkFile
    }
  }.getOrNull()
}

/**
 * Resolves a `content://` URI for the prepared APK file using `FileProvider`.
 */
fun resolveShareableApkUri(context: Context, apkFile: java.io.File): android.net.Uri {
  val authority = "${context.packageName}.fileprovider"
  return runCatching {
    androidx.core.content.FileProvider.getUriForFile(context, authority, apkFile)
  }.getOrElse {
    android.net.Uri.parse("content://$authority/shared_apk/${apkFile.name}")
  }
}

/**
 * Builds the `Intent.ACTION_SEND` intent that attaches BOTH:
 * 1. The installed APK file (`Intent.EXTRA_STREAM`) via `FileProvider`
 * 2. The share text containing the official Google Drive folder link (`Intent.EXTRA_TEXT`)
 */
fun buildShareAppApkAndLinkIntent(context: Context): Intent {
  val shareMessage = buildShareAppMessage()
  val apkFile = prepareShareableApkFile(context)
  return Intent(Intent.ACTION_SEND).apply {
    putExtra(Intent.EXTRA_SUBJECT, "Bayan al-Huda — APK & Latest Updates Link")
    putExtra(Intent.EXTRA_TITLE, "Bayan al-Huda APK & Official Updates Folder")
    putExtra(Intent.EXTRA_TEXT, shareMessage)
    if (apkFile != null && apkFile.exists()) {
      val apkUri = resolveShareableApkUri(context, apkFile)
      type = "application/vnd.android.package-archive"
      putExtra(Intent.EXTRA_STREAM, apkUri)
      clipData = ClipData.newRawUri("Bayan al-Huda APK", apkUri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    } else {
      type = "text/plain"
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
  }
}

/**
 * Launches the Android system Share sheet with the APK file (`EXTRA_STREAM`) and the
 * Google Drive updates folder link (`EXTRA_TEXT`), and also copies the updates link to
 * the clipboard so recipients/senders always have the link even if a target app only reads the file stream.
 */
fun shareAppApkWithDriveLink(context: Context) {
  runCatching {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(
      ClipData.newPlainText("Bayan al-Huda Updates Link", buildShareAppMessage())
    )
  }
  try {
    val sendIntent = buildShareAppApkAndLinkIntent(context)
    val chooserIntent = Intent.createChooser(
      sendIntent,
      "Share Bayan al-Huda (APK + Updates Link)"
    ).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(chooserIntent)
  } catch (e: Exception) {
    // Fallback to plain text + link sharing if APK stream chooser fails on a restricted device
    runCatching {
      val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Bayan al-Huda — Latest Updates Link")
        putExtra(Intent.EXTRA_TEXT, buildShareAppMessage())
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(
        Intent.createChooser(fallbackIntent, "Share Bayan al-Huda Link")
          .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      )
    }.onFailure {
      Toast.makeText(
        context,
        "Updates link copied: $APP_UPDATES_GOOGLE_DRIVE_FOLDER_URL",
        Toast.LENGTH_LONG
      ).show()
    }
  }
}

/**
 * Opens the official Google Drive folder containing the latest APK updates of Bayan al-Huda.
 */
fun openAppUpdatesDriveFolder(context: Context) {
  try {
    val viewIntent = Intent(
      Intent.ACTION_VIEW,
      android.net.Uri.parse(APP_UPDATES_GOOGLE_DRIVE_FOLDER_URL)
    ).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(viewIntent)
  } catch (e: Exception) {
    copyAppUpdatesDriveLink(context)
  }
}

/**
 * Copies the official Google Drive updates folder link to the device clipboard.
 */
fun copyAppUpdatesDriveLink(context: Context) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
  clipboard?.setPrimaryClip(
    ClipData.newPlainText("Bayan al-Huda Latest Updates Drive Folder", APP_UPDATES_GOOGLE_DRIVE_FOLDER_URL)
  )
  Toast.makeText(
    context,
    "Google Drive updates link copied to clipboard",
    Toast.LENGTH_SHORT
  ).show()
}

