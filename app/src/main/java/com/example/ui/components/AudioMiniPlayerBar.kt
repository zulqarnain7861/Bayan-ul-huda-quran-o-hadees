package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.audio.AudioState
import com.example.ui.audio.Reciter
import com.example.ui.audio.TajweedAudioSyncEngine
import com.example.ui.audio.TimingCalibrationStatus
import com.example.ui.audio.availableReciters
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AudioMiniPlayerBar(
  audioState: AudioState,
  onTogglePlayPause: () -> Unit,
  onStop: () -> Unit,
  onSelectReciter: ((Reciter) -> Unit)? = null,
  onChangePlaybackSpeed: ((Float) -> Unit)? = null,
  onToggleAudioRefinement: ((Boolean) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  if (!audioState.isVisible && audioState.surahNumber == null && !audioState.isPlaying && !audioState.isBuffering) {
    return
  }

  val context = LocalContext.current
  var showReciterPickerSheet by remember { mutableStateOf(false) }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 6.dp)
      .testTag("audio_mini_player_popup"),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.primaryContainer,
    tonalElevation = 6.dp,
    shadowElevation = 8.dp
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Progress line
      if (audioState.durationMs > 0) {
        val progress = (audioState.currentPositionMs.toFloat() / audioState.durationMs.toFloat()).coerceIn(0f, 1f)
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(3.dp),
          color = GoldPrimary,
          trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
        )
      } else if (audioState.isBuffering) {
        LinearProgressIndicator(
          modifier = Modifier
            .fillMaxWidth()
            .height(3.dp),
          color = GoldPrimary,
          trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
        )
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Speaker icon (tappable to open Reciter Picker sheet)
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(EmeraldPrimary)
            .clickable { showReciterPickerSheet = true },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Choose Audio Reciter",
            tint = GoldPrimary,
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Recitation details & Tappable Reciter Name (Part 5 & Part 6)
        Column(
          modifier = Modifier
            .weight(1f)
            .clickable { showReciterPickerSheet = true }
            .testTag("mini_player_reciter_picker_button"),
          verticalArrangement = Arrangement.Center
        ) {
          val ayahDisplay = if (audioState.ayahNumber == 0) "Bismillah" else "${audioState.ayahNumber ?: 1}"
          val title = if (audioState.surahName.isNotEmpty()) {
            "${audioState.surahName} (${audioState.surahNumber ?: 1}:$ayahDisplay)"
          } else {
            "Surah ${audioState.surahNumber ?: 1}:$ayahDisplay"
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = title,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              ),
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f, fill = false)
            )
            // Connection & Sync Status Chip (Online / Offline / Resyncing per Section 3)
            val connectionStatusLabel = when {
              audioState.isBuffering || audioState.timingStatus == TimingCalibrationStatus.RESYNCING -> "Resyncing"
              audioState.audioSource == "offline_local" -> "Offline"
              else -> "Online"
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = EmeraldPrimary.copy(alpha = 0.28f),
              modifier = Modifier.testTag("audio_connection_status_chip")
            ) {
              Text(
                text = connectionStatusLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = GoldPrimary
                ),
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
              )
            }
            // Unobtrusive Timing Status Pill (Dataset / Calibrated / Fallback / Resyncing)
            val statusLabel = if (audioState.isBuffering) {
              TimingCalibrationStatus.RESYNCING.label
            } else {
              audioState.timingStatus.label
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = EmeraldPrimary.copy(alpha = 0.22f),
              modifier = Modifier.testTag("mini_player_timing_status_badge")
            ) {
              Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = GoldPrimary
                ),
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
              )
            }
          }

          Text(
            text = "${audioState.reciter.name} (${audioState.reciter.arabicName}) ▾",
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.92f)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        // Playback Speed Badge (1.0x default, tappable to open speed/reciter sheet)
        val normalizedSpeed = TajweedAudioSyncEngine.normalizePlaybackSpeed(audioState.playbackSpeed)
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = EmeraldPrimary.copy(alpha = 0.28f),
          modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickable { showReciterPickerSheet = true }
            .testTag("mini_player_speed_badge")
        ) {
          Text(
            text = "${normalizedSpeed}x",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = GoldPrimary
            ),
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
          )
        }

        // Play/Pause button
        if (audioState.isBuffering) {
          CircularProgressIndicator(
            modifier = Modifier
              .size(26.dp)
              .padding(4.dp),
            strokeWidth = 2.5.dp,
            color = GoldPrimary
          )
        } else {
          IconButton(
            onClick = onTogglePlayPause,
            modifier = Modifier.testTag("audio_toggle_play_pause")
          ) {
            Icon(
              imageVector = if (audioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (audioState.isPlaying) "Pause" else "Play",
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(28.dp)
            )
          }
        }

        // Close / Dismiss popup button
        IconButton(
          onClick = onStop,
          modifier = Modifier.testTag("audio_stop_button")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close Audio Player Popup",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }

  if (showReciterPickerSheet) {
    ModalBottomSheet(
      onDismissRequest = { showReciterPickerSheet = false }
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 12.dp)
          .testTag("mini_player_reciter_bottom_sheet")
      ) {
        Text(
          text = "Select Reciter & Playback Speed (القراء وسرعة التلاوة)",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = EmeraldPrimary
          )
        )
        Text(
          text = "Switching reciters mid-surah keeps your current verse and saved speed (1.0x default).",
          style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Playback Speed Selector (0.5x .. 2.0x, default 1.0x)
        Text(
          text = "Playback Speed (Global Setting • Default 1.0x)",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        )
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          val activeSpeed = TajweedAudioSyncEngine.normalizePlaybackSpeed(audioState.playbackSpeed)
          TajweedAudioSyncEngine.SUPPORTED_PLAYBACK_SPEEDS.forEach { spd ->
            val isSelectedSpeed = spd == activeSpeed
            FilterChip(
              selected = isSelectedSpeed,
              onClick = {
                val saved = TajweedAudioSyncEngine.saveGlobalPlaybackSpeed(context, spd)
                onChangePlaybackSpeed?.invoke(saved)
              },
              label = {
                Text(
                  text = if (spd == 1.0f) "1.0x (Normal)" else "${spd}x",
                  fontSize = 11.sp,
                  fontWeight = if (isSelectedSpeed) FontWeight.Bold else FontWeight.Medium
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("playback_speed_chip_$spd")
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Audio-Assisted Timing Refinement Toggle (Part 6)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Audio-Assisted Timing Refinement",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Status: ${audioState.timingStatus.label} (${audioState.calibrationSource.label})",
              style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
          Switch(
            checked = audioState.isAudioAssistedRefinementEnabled,
            onCheckedChange = { enabled ->
              TajweedAudioSyncEngine.setAudioAssistedRefinementEnabled(context, enabled)
              onToggleAudioRefinement?.invoke(enabled)
            },
            modifier = Modifier.testTag("audio_assisted_refinement_switch")
          )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(10.dp))

        var miniMergedReciters by remember { mutableStateOf(availableReciters) }
        var miniSearchQuery by remember { mutableStateOf("") }

        androidx.compose.runtime.LaunchedEffect(Unit) {
          val liveMerged = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            com.example.ui.audio.Mp3QuranTimingClient.fetchAndMergeLiveRecitersCatalog(availableReciters)
          }
          if (liveMerged.size > miniMergedReciters.size) {
            miniMergedReciters = liveMerged
          }
        }

        val displayedReciters = remember(miniMergedReciters, miniSearchQuery) {
          val q = miniSearchQuery.trim().lowercase()
          if (q.isBlank()) {
            miniMergedReciters
          } else {
            miniMergedReciters.filter { r ->
              r.name.lowercase().contains(q) ||
                r.arabicName.contains(q) ||
                r.style.lowercase().contains(q)
            }
          }
        }

        androidx.compose.material3.OutlinedTextField(
          value = miniSearchQuery,
          onValueChange = { miniSearchQuery = it },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("mini_player_reciter_search_input"),
          singleLine = true,
          placeholder = { Text("Search ${miniMergedReciters.size}+ reciters...") },
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // All Available Reciters with Checkmark & Offline/Basmala/Timing Badges (Part 5)
        displayedReciters.forEach { reciter ->
          val isSelected = reciter.id == audioState.reciter.id
          val timingReadId = com.example.ui.audio.Mp3QuranTimingClient.resolveReadIdForReciter(reciter.id)
          val hasCachedAudio = remember(reciter.id, audioState.surahNumber) {
            val dir = File(context.filesDir, "audio_cache_v2/${reciter.id}")
            dir.exists() && (dir.listFiles()?.isNotEmpty() == true)
          }
          val recitesBasmala = TajweedAudioSyncEngine.doesReciterReciteBasmalaInSurah(
            context = context,
            reciter = reciter,
            surahNumber = audioState.surahNumber ?: 1
          )
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .clickable {
                onSelectReciter?.invoke(reciter)
                showReciterPickerSheet = false
              }
              .testTag("mini_player_reciter_item_${reciter.id}"),
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) EmeraldPrimary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = if (isSelected) BorderStroke(1.5.dp, EmeraldPrimary) else null
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
                  text = "${reciter.name} • ${reciter.arabicName}",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = "${reciter.tradition} • ${reciter.style}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                )
                Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = if (hasCachedAudio) "✓ Offline Cached" else "☁ Stream + Auto-Cache",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 10.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = EmeraldPrimary
                    )
                  )
                  Text(
                    text = if (recitesBasmala) "● Basmala Recited (Green)" else "● Direct Ayah 1 (Red)",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (recitesBasmala) Color(0xFF1B5E20) else Color(0xFFD32F2F)
                    )
                  )
                  if (timingReadId != null) {
                    Text(
                      text = "⏱ Timing #$timingReadId",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                      )
                    )
                  }
                }
              }
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Selected Reciter",
                  tint = EmeraldPrimary,
                  modifier = Modifier.size(22.dp)
                )
              }
            }
          }
        }
        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}
