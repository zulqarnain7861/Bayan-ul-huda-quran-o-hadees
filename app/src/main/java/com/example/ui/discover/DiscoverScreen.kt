package com.example.ui.discover

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.LectureFormat
import com.example.data.model.LectureRecord
import com.example.data.model.RecommendedLectureItem
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscoverScreen(
  searchViewModel: SearchViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val state by searchViewModel.uiState.collectAsStateWithLifecycle()

  // If a lecture is selected, render LectureDetailScreen with back navigation
  state.selectedLecture?.let { activeLecture ->
    LectureDetailScreen(
      lecture = activeLecture,
      allLectures = state.allLectures,
      topicDefinitions = state.allTopicDefinitions,
      watchTimeByVideo = state.watchTimeByVideo,
      selectedProfile = state.playbackProfile,
      wifiOnlyEnabled = state.wifiOnlyEnabled,
      isDebugBuild = state.isDebugBuild,
      onBack = { searchViewModel.closeLectureDetail() },
      onSelectProfile = { searchViewModel.selectPlaybackProfile(it) },
      onToggleWifiOnly = { searchViewModel.setWifiOnly(it) },
      onWatchSecondsRecorded = { yid, delta, pos ->
        searchViewModel.recordWatchProgress(yid, delta, pos)
      },
      onSelectLecture = { searchViewModel.openLectureDetail(it) },
      onReportBroken = { searchViewModel.reportLectureBroken(it) },
      modifier = modifier
    )
    return
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp, vertical = 8.dp)
      .testTag("discover_video_lectures_screen"),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Header Banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = EmeraldPrimary.copy(alpha = 0.12f)
        )
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Understand Islam Video Lectures",
              tint = EmeraldPrimary
            )
            Text(
              text = "  Understand Islam · Video Lectures",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              )
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Engineer Muhammad Ali Mirza · Quran Tafseer, Seerah, Ahl al-Bayt & Authentic Sunnah Lectures",
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    }

    // Search Input
    item {
      OutlinedTextField(
        value = state.searchQuery,
        onValueChange = { searchViewModel.onSearchQueryChange(it) },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("discover_fts_search_field"),
        placeholder = {
          Text("Search lectures, topics, or Quran & Hadith references…")
        },
        leadingIcon = {
          Icon(imageVector = Icons.Default.Search, contentDescription = "Search lectures")
        },
        trailingIcon = {
          if (state.searchQuery.isNotEmpty()) {
            IconButton(
              onClick = { searchViewModel.onSearchQueryChange("") },
              modifier = Modifier.testTag("discover_fts_clear_button")
            ) {
              Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )
    }

    val activeSearch = state.searchResults
    if (activeSearch != null) {
      item {
        Text(
          text = "Search Results (${activeSearch.lectures.size} Lectures)",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          modifier = Modifier.testTag("discover_fts_results_header")
        )
      }
      items(activeSearch.lectures, key = { "search_${it.youtubeId}" }) { lecture ->
        LectureListItemCard(
          lecture = lecture,
          watchedSeconds = state.watchTimeByVideo[lecture.youtubeId] ?: 0,
          recommendationReason = null,
          onClick = { searchViewModel.openLectureDetail(lecture) }
        )
      }
    } else {
      // Recommended for You Carousel
      if (state.recommendedByWatchTime.isNotEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("watch_time_recommendation_section"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = GoldPrimary.copy(alpha = 0.12f)
            )
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = "Recommended Lectures",
                  tint = EmeraldPrimary
                )
                Text(
                  text = "  Recommended for You",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
              LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("watch_time_top_carousel")
              ) {
                items(
                  items = state.recommendedByWatchTime.take(8),
                  key = { "rec_${it.lecture.youtubeId}" }
                ) { recItem ->
                  WatchTimeTopCard(
                    item = recItem,
                    onClick = { searchViewModel.openLectureDetail(recItem.lecture) }
                  )
                }
              }
            }
          }
        }
      }

      // Channel Filter Bar
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("discover_fan_channels_top_card"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = EmeraldPrimary.copy(alpha = 0.08f)
          )
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "Browse by Channel",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              )
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.testTag("discover_channel_chips_row")
            ) {
              item {
                FilterChip(
                  selected = state.selectedChannelHandle == null && !state.onlyFanChannels,
                  onClick = {
                    searchViewModel.selectChannelHandle(null)
                    if (state.onlyFanChannels) searchViewModel.toggleOnlyFanChannels()
                  },
                  label = {
                    Text(
                      "All Channels (${state.allLectures.count { it.shouldShowInUi(state.isDebugBuild) }})",
                      fontSize = 11.sp
                    )
                  },
                  modifier = Modifier.testTag("channel_chip_all")
                )
              }
              item {
                FilterChip(
                  selected = state.onlyFanChannels && state.selectedChannelHandle == null,
                  onClick = {
                    searchViewModel.selectChannelHandle(null)
                    searchViewModel.toggleOnlyFanChannels()
                  },
                  label = {
                    Text("Fan Channels (${state.fanChannelTotalCount})", fontSize = 11.sp)
                  },
                  modifier = Modifier.testTag("channel_chip_fan_only")
                )
              }
              items(state.availableChannels, key = { "ch_${it.channelHandle}" }) { chItem ->
                FilterChip(
                  selected = state.selectedChannelHandle.equals(chItem.channelHandle, ignoreCase = true),
                  onClick = {
                    val next = if (state.selectedChannelHandle.equals(chItem.channelHandle, ignoreCase = true)) {
                      null
                    } else {
                      chItem.channelHandle
                    }
                    searchViewModel.selectChannelHandle(next)
                  },
                  label = {
                    Text(
                      text = "${chItem.displayName} (${chItem.count})",
                      fontSize = 11.sp
                    )
                  },
                  modifier = Modifier.testTag("channel_chip_${chItem.channelHandle}")
                )
              }
            }
          }
        }
      }

      // Topic Filter Chips
      item {
        Column {
          Text(
            text = "Browse by Topic",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(6.dp))
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.testTag("discover_topic_chips_row")
          ) {
            item {
              FilterChip(
                selected = state.selectedTopicId == null,
                onClick = { searchViewModel.selectTopic(null) },
                label = { Text("All Topics (${state.seedCounts.total})", fontSize = 11.sp) },
                modifier = Modifier.testTag("topic_chip_all")
              )
            }
            items(state.visibleTopics, key = { it.definition.id }) { topicItem ->
              FilterChip(
                selected = state.selectedTopicId == topicItem.definition.id,
                onClick = {
                  val next = if (state.selectedTopicId == topicItem.definition.id) null else topicItem.definition.id
                  searchViewModel.selectTopic(next)
                },
                label = {
                  Text(
                    text = "${topicItem.definition.titleEn} (${topicItem.matchingLectureCount})",
                    fontSize = 11.sp
                  )
                },
                modifier = Modifier.testTag("topic_chip_${topicItem.definition.id}")
              )
            }
          }
        }
      }

      // Format Filters
      item {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChip(
            selected = state.onlyConfirmedLive,
            onClick = { searchViewModel.toggleOnlyConfirmedLive() },
            label = { Text("Featured (${state.seedCounts.confirmedLive})", fontSize = 11.sp) },
            modifier = Modifier.testTag("filter_chip_confirmed_live")
          )
          FilterChip(
            selected = state.selectedFormatFilter == LectureFormat.FULL_LECTURE,
            onClick = {
              val next = if (state.selectedFormatFilter == LectureFormat.FULL_LECTURE) null else LectureFormat.FULL_LECTURE
              searchViewModel.selectFormatFilter(next)
            },
            label = { Text("Full Lectures (${state.seedCounts.fullLectures})", fontSize = 11.sp) },
            modifier = Modifier.testTag("filter_chip_full_lectures")
          )
          FilterChip(
            selected = state.selectedFormatFilter == LectureFormat.CLIP,
            onClick = {
              val next = if (state.selectedFormatFilter == LectureFormat.CLIP) null else LectureFormat.CLIP
              searchViewModel.selectFormatFilter(next)
            },
            label = { Text("Short Clips (${state.seedCounts.clips})", fontSize = 11.sp) },
            modifier = Modifier.testTag("filter_chip_short_clips")
          )
        }
      }

      // Lecture List
      item {
        Text(
          text = "All Lectures (${state.visibleLectures.size})",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
      }

      items(state.visibleLectures, key = { it.youtubeId }) { lecture ->
        LectureListItemCard(
          lecture = lecture,
          watchedSeconds = state.watchTimeByVideo[lecture.youtubeId] ?: 0,
          recommendationReason = null,
          onClick = { searchViewModel.openLectureDetail(lecture) }
        )
      }
    }
  }
}

@Composable
private fun WatchTimeTopCard(
  item: RecommendedLectureItem,
  onClick: () -> Unit
) {
  val lecture = item.lecture
  Card(
    modifier = Modifier
      .width(280.dp)
      .clickable(onClick = onClick)
      .testTag("watch_time_top_card_${lecture.youtubeId}"),
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
          text = item.reasonLabel,
          style = MaterialTheme.typography.labelSmall.copy(
            color = EmeraldPrimary,
            fontWeight = FontWeight.Bold
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier
            .weight(1f)
            .padding(end = 6.dp)
        )
        SourceTierBadge(tier = lecture.sourceTier)
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = lecture.displayTitle,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "${lecture.channelDisplayName} · ${lecture.recordFormat.label}",
        style = MaterialTheme.typography.labelSmall.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun LectureListItemCard(
  lecture: LectureRecord,
  watchedSeconds: Int,
  recommendationReason: String?,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("lecture_card_${lecture.youtubeId}"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      if (!recommendationReason.isNullOrBlank()) {
        Text(
          text = recommendationReason,
          style = MaterialTheme.typography.labelSmall.copy(
            color = EmeraldPrimary,
            fontWeight = FontWeight.Bold
          )
        )
        Spacer(modifier = Modifier.height(4.dp))
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = "Play ${lecture.displayTitle}",
          tint = EmeraldPrimary,
          modifier = Modifier.padding(end = 10.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = lecture.displayTitle,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${lecture.channelDisplayName} · ${lecture.seriesName ?: "Understand Islam"} · ${lecture.recordedYear ?: 2013}",
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          SourceTierBadge(tier = lecture.sourceTier)
          if (watchedSeconds > 0) {
            Text(
              text = "Watched ${formatSecondsShort(watchedSeconds)}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = EmeraldPrimary,
                fontWeight = FontWeight.Bold
              )
            )
          }
        }
        Text(
          text = lecture.references.firstOrNull() ?: lecture.recordFormat.label,
          style = MaterialTheme.typography.labelSmall
        )
      }
    }
  }
}
