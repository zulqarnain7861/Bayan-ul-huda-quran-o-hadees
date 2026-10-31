package com.example.ui.discover

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.model.DiscoveryTopicDefinition
import com.example.data.model.LectureFormat
import com.example.data.model.LectureRecord
import com.example.data.model.LectureSeedCounts
import com.example.data.model.MaududiLibraryCategory
import com.example.data.model.MaududiWorkRecord
import com.example.data.model.PlaybackDataProfile
import com.example.data.model.RecommendedLectureItem
import com.example.data.model.SourceTier
import com.example.data.repository.DiscoverRepository
import com.example.data.repository.MaududiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChannelFilterItem(
  val channelHandle: String,
  val displayName: String,
  val sourceTier: SourceTier,
  val count: Int
)

data class DiscoverUiState(
  val isLoading: Boolean = true,
  val isDebugBuild: Boolean = BuildConfig.DEBUG,
  val coverageDisclosure: String = "169 seed records · 159 unverified · Tafseer series runs to 344 — tap to load more",
  val seedCounts: LectureSeedCounts = LectureSeedCounts(169, 10, 159, 120, 49, 1, 344),
  val allLectures: List<LectureRecord> = emptyList(),
  val visibleLectures: List<LectureRecord> = emptyList(),
  val recommendedByWatchTime: List<RecommendedLectureItem> = emptyList(),
  val visibleTopics: List<DiscoverRepository.TopicChipItem> = emptyList(),
  val allTopicDefinitions: List<DiscoveryTopicDefinition> = emptyList(),
  val availableChannels: List<ChannelFilterItem> = emptyList(),
  val fanChannelTotalCount: Int = 0,
  val selectedChannelHandle: String? = null,
  val onlyFanChannels: Boolean = false,
  val selectedTopicId: String? = null,
  val selectedFormatFilter: LectureFormat? = null,
  val onlyConfirmedLive: Boolean = false,
  val searchQuery: String = "",
  val searchResults: DiscoverRepository.UnifiedSearchResults? = null,
  val selectedLecture: LectureRecord? = null,
  val selectedMaududiCategory: MaududiLibraryCategory = MaududiLibraryCategory.TAFHEEM_UL_QURAN,
  val maududiWorks: List<MaududiWorkRecord> = emptyList(),
  val selectedMaududiWork: MaududiWorkRecord? = null,
  val playbackProfile: PlaybackDataProfile = PlaybackDataProfile.BALANCED,
  val wifiOnlyEnabled: Boolean = false,
  val watchTimeByVideo: Map<String, Int> = emptyMap(),
  val watchTimeByTopic: Map<String, Int> = emptyMap(),
  val tafheemDisclosureNotice: String = MaududiRepository.TAFHEEM_DISCLOSURE_NOTICE
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = DiscoverRepository(application.applicationContext)

  private val _uiState = MutableStateFlow(DiscoverUiState())
  val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

  init {
    refreshAll()
  }

  fun refreshAll() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      repository.initializeAll()
      val dataset = repository.lectureRepository.loadSeedDataset()
      val config = repository.lectureRepository.loadDiscoveryConfig()
      val maududiWorks = repository.maududiRepository.getAllWorksMemory()
      val watchVideoMap = repository.lectureRepository.getWatchTimeMap()
      val watchTopicMap = repository.lectureRepository.getTopicWatchTimeMap()
      val profile = repository.lectureRepository.getPlaybackDataProfile()
      val wifiOnly = repository.lectureRepository.isWifiOnlyEnabled()

      val isDebug = _uiState.value.isDebugBuild
      val visible = dataset.records.filter { it.shouldShowInUi(isDebug) }
      val visibleTopics = repository.buildVisibleTopics(
        allLectures = dataset.records,
        allTopics = config.topics,
        topicWatchMap = watchTopicMap,
        isDebugBuild = isDebug
      )
      val recommendations = repository.getWatchTimeRecommendations(isDebugBuild = isDebug, limit = 24)
      val channelItems = buildChannelFilterList(visible)
      val fanTotal = visible.count { it.sourceTier == SourceTier.FAN_CHANNEL }

      _uiState.update { state ->
        state.copy(
          isLoading = false,
          coverageDisclosure = dataset.coverageDisclosure,
          seedCounts = dataset.counts,
          allLectures = dataset.records,
          visibleLectures = applyFilters(
            lectures = visible,
            topicId = state.selectedTopicId,
            format = state.selectedFormatFilter,
            onlyConfirmed = state.onlyConfirmedLive,
            onlyFan = state.onlyFanChannels,
            channelHandle = state.selectedChannelHandle
          ),
          recommendedByWatchTime = recommendations,
          visibleTopics = visibleTopics,
          allTopicDefinitions = config.topics,
          availableChannels = channelItems,
          fanChannelTotalCount = fanTotal,
          maududiWorks = maududiWorks,
          playbackProfile = profile,
          wifiOnlyEnabled = wifiOnly,
          watchTimeByVideo = watchVideoMap,
          watchTimeByTopic = watchTopicMap
        )
      }
    }
  }

  fun selectChannelHandle(handle: String?) {
    _uiState.update { state ->
      val visible = state.allLectures.filter { it.shouldShowInUi(state.isDebugBuild) }
      state.copy(
        selectedChannelHandle = handle,
        visibleLectures = applyFilters(
          visible,
          state.selectedTopicId,
          state.selectedFormatFilter,
          state.onlyConfirmedLive,
          state.onlyFanChannels,
          handle
        )
      )
    }
  }

  fun toggleOnlyFanChannels() {
    _uiState.update { state ->
      val nextFan = !state.onlyFanChannels
      val visible = state.allLectures.filter { it.shouldShowInUi(state.isDebugBuild) }
      state.copy(
        onlyFanChannels = nextFan,
        visibleLectures = applyFilters(
          visible,
          state.selectedTopicId,
          state.selectedFormatFilter,
          state.onlyConfirmedLive,
          nextFan,
          state.selectedChannelHandle
        )
      )
    }
  }

  fun selectTopic(topicId: String?) {
    _uiState.update { state ->
      val visible = state.allLectures.filter { it.shouldShowInUi(state.isDebugBuild) }
      state.copy(
        selectedTopicId = topicId,
        visibleLectures = applyFilters(
          visible,
          topicId,
          state.selectedFormatFilter,
          state.onlyConfirmedLive,
          state.onlyFanChannels,
          state.selectedChannelHandle
        )
      )
    }
  }

  fun selectFormatFilter(format: LectureFormat?) {
    _uiState.update { state ->
      val visible = state.allLectures.filter { it.shouldShowInUi(state.isDebugBuild) }
      state.copy(
        selectedFormatFilter = format,
        visibleLectures = applyFilters(
          visible,
          state.selectedTopicId,
          format,
          state.onlyConfirmedLive,
          state.onlyFanChannels,
          state.selectedChannelHandle
        )
      )
    }
  }

  fun toggleOnlyConfirmedLive() {
    _uiState.update { state ->
      val nextConfirmed = !state.onlyConfirmedLive
      val visible = state.allLectures.filter { it.shouldShowInUi(state.isDebugBuild) }
      state.copy(
        onlyConfirmedLive = nextConfirmed,
        visibleLectures = applyFilters(
          visible,
          state.selectedTopicId,
          state.selectedFormatFilter,
          nextConfirmed,
          state.onlyFanChannels,
          state.selectedChannelHandle
        )
      )
    }
  }

  fun onSearchQueryChange(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
    viewModelScope.launch {
      if (query.isBlank()) {
        _uiState.update { it.copy(searchResults = null) }
      } else {
        val res = repository.searchAll(query, _uiState.value.isDebugBuild)
        _uiState.update { it.copy(searchResults = res) }
      }
    }
  }

  fun openLectureDetail(lecture: LectureRecord) {
    _uiState.update { it.copy(selectedLecture = lecture) }
    // Record initial 5-second engagement signal on opening a lecture so related topics surface immediately
    recordWatchProgress(lecture.youtubeId, 5, 5)
  }

  fun closeLectureDetail() {
    _uiState.update { it.copy(selectedLecture = null) }
  }

  fun recordWatchProgress(youtubeId: String, deltaSec: Int, currentPositionSec: Int) {
    viewModelScope.launch {
      repository.lectureRepository.recordWatchTime(youtubeId, deltaSec, currentPositionSec)
      val watchVideoMap = repository.lectureRepository.getWatchTimeMap()
      val watchTopicMap = repository.lectureRepository.getTopicWatchTimeMap()
      val isDebug = _uiState.value.isDebugBuild
      val recs = repository.getWatchTimeRecommendations(isDebugBuild = isDebug, limit = 24)
      val visibleTopics = repository.buildVisibleTopics(
        allLectures = _uiState.value.allLectures,
        allTopics = _uiState.value.allTopicDefinitions,
        topicWatchMap = watchTopicMap,
        isDebugBuild = isDebug
      )
      _uiState.update { state ->
        state.copy(
          recommendedByWatchTime = recs,
          visibleTopics = visibleTopics,
          watchTimeByVideo = watchVideoMap,
          watchTimeByTopic = watchTopicMap
        )
      }
    }
  }

  fun selectPlaybackProfile(profile: PlaybackDataProfile) {
    repository.lectureRepository.setPlaybackDataProfile(profile)
    _uiState.update { it.copy(playbackProfile = profile) }
  }

  fun setWifiOnly(enabled: Boolean) {
    repository.lectureRepository.setWifiOnlyEnabled(enabled)
    _uiState.update { it.copy(wifiOnlyEnabled = enabled) }
  }

  fun reportLectureBroken(youtubeId: String) {
    viewModelScope.launch {
      repository.lectureRepository.markLectureBrokenLocally(youtubeId)
      refreshAll()
    }
  }

  fun selectMaududiCategory(category: MaududiLibraryCategory) {
    _uiState.update { it.copy(selectedMaududiCategory = category) }
  }

  fun openMaududiWorkReader(work: MaududiWorkRecord) {
    _uiState.update { it.copy(selectedMaududiWork = work) }
  }

  fun closeMaududiWorkReader() {
    _uiState.update { it.copy(selectedMaududiWork = null) }
  }

  private fun buildChannelFilterList(lectures: List<LectureRecord>): List<ChannelFilterItem> {
    return lectures
      .groupBy { it.channelHandle ?: "@RAAH-e-HIDAYAT-EMAM" }
      .map { (handle, items) ->
        val sample = items.first()
        ChannelFilterItem(
          channelHandle = handle,
          displayName = sample.channelDisplayName,
          sourceTier = sample.sourceTier,
          count = items.size
        )
      }
      .sortedWith(
        compareBy<ChannelFilterItem> { if (it.sourceTier == SourceTier.FAN_CHANNEL) 0 else 1 }
          .thenByDescending { it.count }
      )
  }

  private fun applyFilters(
    lectures: List<LectureRecord>,
    topicId: String?,
    format: LectureFormat?,
    onlyConfirmed: Boolean,
    onlyFan: Boolean,
    channelHandle: String?
  ): List<LectureRecord> {
    return lectures
      .filter { record ->
        (topicId == null || topicId in record.topics) &&
          (format == null || record.recordFormat == format) &&
          (!onlyConfirmed || record.verificationState.name == "CONFIRMED_LIVE") &&
          (!onlyFan || record.sourceTier == SourceTier.FAN_CHANNEL) &&
          (channelHandle == null || record.channelHandle.equals(channelHandle, ignoreCase = true))
      }
      .sortedBy { if (it.sourceTier == SourceTier.FAN_CHANNEL) 0 else 1 }
  }
}
