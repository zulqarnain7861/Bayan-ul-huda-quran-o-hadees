package com.example.ui.discover

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiscoveryTopicDefinition
import com.example.data.model.LectureRecord
import com.example.data.model.PlaybackDataProfile
import com.example.data.model.RecordVerificationState
import com.example.data.model.SourceTier
import com.example.data.model.TranscriptStatus
import com.example.data.repository.LectureRepository
import com.example.player.YtEmbedHost
import com.example.player.YtEmbedUrlBuilder
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary

/**
 * Lecture Player & Provenance Detail Screen (`ui/discover/LectureDetailScreen.kt`) per Spec §6 & §10.
 *
 * Features:
 * - Hardened `YtEmbedHost` (`youtube-nocookie.com`) with data policy selector & error fallback card.
 * - Source Tier badge (`OFFICIAL` green, `FAN_CHANNEL` amber, `THIRD_PARTY_ARCHIVE` grey,
 *   `UNKNOWN_PENDING` red in debug) with explicit `contentDescription`.
 * - Honest transcript disclosure (`"No verified transcript attached"` when `NONE`).
 * - **"Related Lectures on Top (Based on Watch Time)"**: immediately surfaces related lectures
 *   (e.g., watching *Imam Hussain A.S.* puts *Karbala*, *Reality of Yazeed*, *Ahl al-Bayt* on top).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LectureDetailScreen(
  lecture: LectureRecord,
  allLectures: List<LectureRecord>,
  topicDefinitions: List<DiscoveryTopicDefinition>,
  watchTimeByVideo: Map<String, Int>,
  selectedProfile: PlaybackDataProfile,
  wifiOnlyEnabled: Boolean,
  isDebugBuild: Boolean,
  simulatedErrorCode: Int? = null,
  onBack: () -> Unit,
  onSelectProfile: (PlaybackDataProfile) -> Unit,
  onToggleWifiOnly: (Boolean) -> Unit,
  onWatchSecondsRecorded: (youtubeId: String, deltaSec: Int, currentSec: Int) -> Unit,
  onSelectLecture: (LectureRecord) -> Unit,
  onReportBroken: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBack() }
  val context = LocalContext.current

  // Compute related lectures boosted by the current video's topics + user watch history
  val relatedRecommendations = remember(lecture.youtubeId, allLectures, watchTimeByVideo, isDebugBuild) {
    val boostedWatchMap = watchTimeByVideo.toMutableMap()
    // Ensure the currently playing lecture contributes at least 60s of topic affinity for immediate related ranking
    boostedWatchMap[lecture.youtubeId] = (boostedWatchMap[lecture.youtubeId] ?: 0) + 60
    LectureRepository.computeWatchTimeRecommendations(
      allLectures = allLectures.filter { it.youtubeId != lecture.youtubeId },
      watchTimeByYoutubeId = boostedWatchMap,
      topicDefinitions = topicDefinitions,
      isDebugBuild = isDebugBuild,
      limit = 10
    )
  }

  val totalUserWatchedSec = watchTimeByVideo[lecture.youtubeId] ?: 0

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp, vertical = 8.dp)
      .testTag("lecture_detail_screen"),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("lecture_detail_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to Lecture Library"
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Understand Islam · Video Lecture",
            style = MaterialTheme.typography.labelMedium.copy(
              color = EmeraldPrimary,
              fontWeight = FontWeight.Bold
            )
          )
          Text(
            text = lecture.speaker,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
        }
        SourceTierBadge(tier = lecture.sourceTier)
      }
    }

    item {
      YtEmbedHost(
        lecture = lecture,
        allIndexedLectures = allLectures,
        selectedProfile = selectedProfile,
        wifiOnlyEnabled = wifiOnlyEnabled,
        simulatedErrorCode = simulatedErrorCode,
        onSelectProfile = onSelectProfile,
        onToggleWifiOnly = onToggleWifiOnly,
        onWatchSecondsRecorded = onWatchSecondsRecorded,
        onSelectAlternateLecture = onSelectLecture,
        onReportBroken = onReportBroken
      )
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = lecture.displayTitle,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.testTag("lecture_detail_title")
          )
          Spacer(modifier = Modifier.height(6.dp))

          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            VerificationStateChip(state = lecture.verificationState)
            AssistChip(
              onClick = {},
              label = {
                Text(
                  text = "${lecture.recordFormat.label} · ${lecture.recordedYear ?: 2013}",
                  fontSize = 11.sp
                )
              }
            )
            if (lecture.lectureNumber != null) {
              AssistChip(
                onClick = {},
                label = {
                  Text(
                    text = "Lecture #${lecture.lectureNumber}",
                    fontSize = 11.sp
                  )
                }
              )
            }
            if (totalUserWatchedSec > 0) {
              AssistChip(
                onClick = {},
                label = {
                  Text(
                    text = "Watched: ${formatSecondsShort(totalUserWatchedSec)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              )
            }
          }

          if (!lecture.summary.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = lecture.summary,
              style = MaterialTheme.typography.bodyMedium
            )
          }

          Spacer(modifier = Modifier.height(10.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(8.dp))

          // Primary Hadith / Quran References
          Text(
            text = "Primary Scriptural References (${lecture.referenceStatus.label})",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(4.dp))
          if (lecture.references.isEmpty()) {
            Text(
              text = "References pending verification.",
              style = MaterialTheme.typography.bodySmall
            )
          } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              lecture.references.forEach { ref ->
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = EmeraldPrimary.copy(alpha = 0.12f),
                  modifier = Modifier.padding(vertical = 2.dp)
                ) {
                  Text(
                    text = ref,
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = EmeraldPrimary,
                      fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          // Honest Transcript Status Disclosure (§6.5)
          Text(
            text = if (lecture.transcriptStatus == TranscriptStatus.NONE) {
              "Transcript: No verified transcript attached"
            } else {
              "Transcript: ${lecture.transcriptStatus.label}"
            },
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("lecture_detail_transcript_disclosure")
          )

          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Channel: ${lecture.channelDisplayName}",
              style = MaterialTheme.typography.labelSmall
            )
            OutlinedButton(
              onClick = { YtEmbedUrlBuilder.openOnYouTubeSafely(context, lecture.youtubeId) },
              modifier = Modifier.testTag("lecture_detail_open_yt_button")
            ) {
              Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Open lecture on YouTube"
              )
              Text("  YouTube", fontSize = 11.sp)
            }
          }
        }
      }
    }

    // Related Lectures
    item {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = "Related Lectures",
          tint = GoldPrimary
        )
        Text(
          text = "  Related Lectures",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
      }
    }

    items(relatedRecommendations, key = { it.lecture.youtubeId }) { recItem ->
      RelatedLectureCard(
        recommendedItem = recItem,
        onClick = { onSelectLecture(recItem.lecture) }
      )
    }
  }
}

@Composable
fun SourceTierBadge(
  tier: SourceTier,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor) = when (tier) {
    SourceTier.OFFICIAL -> Color(0xFF1B5E20) to Color.White // Green
    SourceTier.FAN_CHANNEL -> Color(0xFFE65100) to Color.White // Amber
    SourceTier.THIRD_PARTY_ARCHIVE -> Color(0xFF546E7A) to Color.White // Grey
    SourceTier.UNKNOWN_PENDING -> Color(0xFFB71C1C) to Color.White // Red
  }
  Surface(
    shape = RoundedCornerShape(6.dp),
    color = bgColor,
    modifier = modifier
      .semantics { contentDescription = tier.contentDescription }
      .testTag("source_tier_badge_${tier.name}")
  ) {
    Text(
      text = tier.badgeLabel,
      color = textColor,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
  }
}

@Composable
fun VerificationStateChip(state: RecordVerificationState) {
  val isLive = state == RecordVerificationState.CONFIRMED_LIVE
  Surface(
    shape = RoundedCornerShape(6.dp),
    color = if (isLive) EmeraldPrimary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      if (isLive) {
        Icon(
          imageVector = Icons.Default.Verified,
          contentDescription = "Confirmed Live Lecture",
          tint = EmeraldPrimary,
          modifier = Modifier.height(14.dp)
        )
      }
      Text(
        text = if (isLive) " CONFIRMED LIVE" else state.label,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (isLive) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun RelatedLectureCard(
  recommendedItem: com.example.data.model.RecommendedLectureItem,
  onClick: () -> Unit
) {
  val rec = recommendedItem.lecture
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("related_lecture_card_${rec.youtubeId}"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Icon(
        imageVector = Icons.Default.PlayArrow,
        contentDescription = "Play ${rec.displayTitle}",
        tint = EmeraldPrimary,
        modifier = Modifier.padding(end = 10.dp)
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = recommendedItem.reasonLabel,
          style = MaterialTheme.typography.labelSmall.copy(
            color = EmeraldPrimary,
            fontWeight = FontWeight.Bold
          )
        )
        Text(
          text = rec.displayTitle,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
          text = "${rec.speaker} · ${rec.recordedYear ?: 2013} · ${rec.references.firstOrNull() ?: rec.seriesName ?: ""}",
          style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
      }
      SourceTierBadge(tier = rec.sourceTier)
    }
  }
}

fun formatSecondsShort(seconds: Int): String {
  if (seconds < 60) return "${seconds}s"
  val mins = seconds / 60
  val remSec = seconds % 60
  return if (remSec == 0) "${mins}m" else "${mins}m ${remSec}s"
}
