package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.audio.AudioState
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary

@Composable
fun AudioMiniPlayerBar(
  audioState: AudioState,
  onTogglePlayPause: () -> Unit,
  onStop: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (!audioState.isVisible && audioState.surahNumber == null && !audioState.isPlaying && !audioState.isBuffering) {
    return
  }

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
        // Speaker icon
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(EmeraldPrimary),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Audio Reciter",
            tint = GoldPrimary,
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Recitation details
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.Center
        ) {
          val title = if (audioState.surahName.isNotEmpty()) {
            "${audioState.surahName} (${audioState.surahNumber}:${audioState.ayahNumber})"
          } else {
            "Surah ${audioState.surahNumber}:${audioState.ayahNumber}"
          }

          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = audioState.statusMessage.ifBlank { audioState.reciter.name },
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
}
