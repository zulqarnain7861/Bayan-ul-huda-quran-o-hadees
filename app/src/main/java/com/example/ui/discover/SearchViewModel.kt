package com.example.ui.discover

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.model.DiscoveryTopicDefinition
import com.example.data.model.IslamicVideoCategory
import com.example.data.model.LectureDateFilter
import com.example.data.model.LectureDurationFilter
import com.example.data.model.LectureFormat
import com.example.data.model.LectureRecord
import com.example.data.model.LectureSeedCounts
import com.example.data.model.LectureSortOption
import com.example.data.model.MaududiLibraryCategory
import com.example.data.model.MaududiWorkRecord
import com.example.data.model.PlaybackDataProfile
import com.example.data.model.RecommendationShelfTab
import com.example.data.model.RecommendedLectureItem
import com.example.data.model.SourceTier
import com.example.data.repository.DiscoverRepository
import com.example.data.repository.LectureRepository
import com.example.data.repository.MaududiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
  val isSearchingYouTube: Boolean = false,
  val isSyncingOnline: Boolean = false,
  val syncStatusMessage: String? = null,
  val isDebugBuild: Boolean = BuildConfig.DEBUG,
  val coverageDisclosure: String = "480 seed records · 321 confirmed live · Tafseer series runs to 344 — tap to load more",
  val seedCounts: LectureSeedCounts = LectureSeedCounts(480, 321, 159, 264, 216, 1, 344),
  val allLectures: List<LectureRecord> = emptyList(),
  val visibleLectures: List<LectureRecord> = emptyList(),
  val recommendedByWatchTime: List<RecommendedLectureItem> = emptyList(),
  val continueWatchingLectures: List<RecommendedLectureItem> = emptyList(),
  val recentlyAddedLectures: List<RecommendedLectureItem> = emptyList(),
  val newLectures: List<RecommendedLectureItem> = emptyList(),
  val relatedTopicsLectures: List<RecommendedLectureItem> = emptyList(),
  val similarLectures: List<RecommendedLectureItem> = emptyList(),
  val unwatchedLectures: List<RecommendedLectureItem> = emptyList(),
  val selectedShelfTab: RecommendationShelfTab = RecommendationShelfTab.RECOMMENDED,
  val visibleTopics: List<DiscoverRepository.TopicChipItem> = emptyList(),
  val allTopicDefinitions: List<DiscoveryTopicDefinition> = emptyList(),
  val availableChannels: List<ChannelFilterItem> = emptyList(),
  val fanChannelTotalCount: Int = 0,
  val selectedChannelHandle: String? = null,
  val onlyFanChannels: Boolean = false,
  val selectedTopicId: String? = null,
  val selectedCategory: IslamicVideoCategory = IslamicVideoCategory.ALL,
  val selectedDateFilter: LectureDateFilter = LectureDateFilter.ALL,
  val selectedDurationFilter: LectureDurationFilter = LectureDurationFilter.ALL,
  val selectedSortOption: LectureSortOption = LectureSortOption.RELEVANCE,
  val selectedFormatFilter: LectureFormat? = null,
  val onlyConfirmedLive: Boolean = false,
  val currentPageLimit: Int = 30,
  val bookmarkedYoutubeIds: Set<String> = emptySet(),
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
  private var liveSearchJob: Job? = null

  private val _uiState = MutableStateFlow(DiscoverUiState())
  val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

  init {
    refreshAll()
  }

  fun refreshAll() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      runCatching {
        val dataset = repository.lectureRepository.loadSeedDataset()
        val config = repository.lectureRepository.loadDiscoveryConfig()
        val watchVideoMap = repository.lectureRepository.getWatchTimeMap()
        val watchTopicMap = repository.lectureRepository.getTopicWatchTimeMap()
        val bookmarkedIds = repository.lectureRepository.getBookmarkedYoutubeIds()
        val profile = repository.lectureRepository.getPlaybackDataProfile()
        val wifiOnly = repository.lectureRepository.isWifiOnlyEnabled()

        val isDebug = _uiState.value.isDebugBuild
        val seedRecords = dataset.records
        val visible = seedRecords.filter { it.shouldShowInUi(isDebug) }
        val visibleTopics = repository.buildVisibleTopics(
          allLectures = seedRecords,
          allTopics = config.topics,
          topicWatchMap = watchTopicMap,
          isDebugBuild = isDebug
        )
        val recommendations = repository.getWatchTimeRecommendations(isDebugBuild = isDebug, limit = 24)
        val shelves = buildRecommendationShelves(
          visible = visible,
          watchVideoMap = watchVideoMap,
          watchTopicMap = watchTopicMap,
          bookmarkedIds = bookmarkedIds,
          topicDefs = config.topics
        )
        val channelItems = buildChannelFilterList(visible)
        val fanTotal = visible.count { it.sourceTier == SourceTier.FAN_CHANNEL }

        // Populate UI state immediately from mirza_lectures_v3.json so Video Library is never empty
        val filteredVisible = applyFilters(
          lectures = visible,
          topicId = _uiState.value.selectedTopicId,
          category = _uiState.value.selectedCategory,
          dateFilter = _uiState.value.selectedDateFilter,
          durationFilter = _uiState.value.selectedDurationFilter,
          sortOption = _uiState.value.selectedSortOption,
          format = _uiState.value.selectedFormatFilter,
          onlyConfirmed = _uiState.value.onlyConfirmedLive,
          onlyFan = _uiState.value.onlyFanChannels,
          channelHandle = _uiState.value.selectedChannelHandle
        )
        com.example.player.VideoStreamExtractor.prewarmLecturesInBackground(
          (recommendations.map { it.lecture } + filteredVisible).take(8)
        )
        _uiState.update { state ->
          state.copy(
            isLoading = false,
            coverageDisclosure = dataset.coverageDisclosure,
            seedCounts = dataset.counts,
            allLectures = seedRecords,
            visibleLectures = filteredVisible,
            recommendedByWatchTime = recommendations,
            continueWatchingLectures = shelves.continueWatching,
            recentlyAddedLectures = shelves.recentlyAdded,
            newLectures = shelves.newLectures,
            relatedTopicsLectures = shelves.relatedTopics,
            similarLectures = shelves.similarLectures,
            unwatchedLectures = shelves.unwatched,
            visibleTopics = visibleTopics,
            allTopicDefinitions = config.topics,
            availableChannels = channelItems,
            fanChannelTotalCount = fanTotal,
            bookmarkedYoutubeIds = bookmarkedIds,
            playbackProfile = profile,
            wifiOnlyEnabled = wifiOnly,
            watchTimeByVideo = watchVideoMap,
            watchTimeByTopic = watchTopicMap
          )
        }

        // Seed Room FTS4 index & Maududi library safely
        runCatching {
          repository.initializeAll()
          val maududiWorks = repository.maududiRepository.getAllWorksMemory()
          _uiState.update { it.copy(maududiWorks = maududiWorks) }
        }
      }.onFailure {
        _uiState.update { it.copy(isLoading = false) }
      }

      // Fetch newly added or missed Engineer Muhammad Ali Mirza lectures online in background
      runCatching {
        val isDebug = _uiState.value.isDebugBuild
        val config = repository.lectureRepository.loadDiscoveryConfig()
        val watchTopicMap = repository.lectureRepository.getTopicWatchTimeMap()
        val watchVideoMap = repository.lectureRepository.getWatchTimeMap()
        val bookmarkedIds = repository.lectureRepository.getBookmarkedYoutubeIds()
        val onlineNew = repository.lectureRepository.syncNewlyAddedOnlineLectures(manualRefresh = false)
        if (onlineNew.isNotEmpty()) {
          val updatedDataset = repository.lectureRepository.loadSeedDataset()
          val updatedVisible = updatedDataset.records.filter { it.shouldShowInUi(isDebug) }
          val updatedTopics = repository.buildVisibleTopics(
            allLectures = updatedDataset.records,
            allTopics = config.topics,
            topicWatchMap = watchTopicMap,
            isDebugBuild = isDebug
          )
          val shelves = buildRecommendationShelves(
            visible = updatedVisible,
            watchVideoMap = watchVideoMap,
            watchTopicMap = watchTopicMap,
            bookmarkedIds = bookmarkedIds,
            topicDefs = config.topics
          )
          val updatedChannels = buildChannelFilterList(updatedVisible)
          _uiState.update { state ->
            state.copy(
              coverageDisclosure = updatedDataset.coverageDisclosure,
              seedCounts = updatedDataset.counts,
              allLectures = updatedDataset.records,
              visibleLectures = applyFilters(
                lectures = updatedVisible,
                topicId = state.selectedTopicId,
                category = state.selectedCategory,
                dateFilter = state.selectedDateFilter,
                durationFilter = state.selectedDurationFilter,
                sortOption = state.selectedSortOption,
                format = state.selectedFormatFilter,
                onlyConfirmed = state.onlyConfirmedLive,
                onlyFan = state.onlyFanChannels,
                channelHandle = state.selectedChannelHandle
              ),
              continueWatchingLectures = shelves.continueWatching,
              recentlyAddedLectures = shelves.recentlyAdded,
              newLectures = shelves.newLectures,
              relatedTopicsLectures = shelves.relatedTopics,
              similarLectures = shelves.similarLectures,
              unwatchedLectures = shelves.unwatched,
              visibleTopics = updatedTopics,
              availableChannels = updatedChannels,
              fanChannelTotalCount = updatedVisible.count { it.sourceTier == SourceTier.FAN_CHANNEL }
            )
          }
        }
      }
    }
  }

  fun triggerManualLibrarySync() {
    viewModelScope.launch {
      _uiState.update { it.copy(isSyncingOnline = true, syncStatusMessage = "Syncing new & missed EMAM lectures…") }
      runCatching {
        val onlineNew = repository.lectureRepository.syncNewlyAddedOnlineLectures(manualRefresh = true)
        val updatedDataset = repository.lectureRepository.loadSeedDataset()
        val isDebug = _uiState.value.isDebugBuild
        val config = repository.lectureRepository.loadDiscoveryConfig()
        val watchTopicMap = repository.lectureRepository.getTopicWatchTimeMap()
        val watchVideoMap = repository.lectureRepository.getWatchTimeMap()
        val bookmarkedIds = repository.lectureRepository.getBookmarkedYoutubeIds()
        val updatedVisible = updatedDataset.records.filter { it.shouldShowInUi(isDebug) }
        val updatedTopics = repository.buildVisibleTopics(
          allLectures = updatedDataset.records,
          allTopics = config.topics,
          topicWatchMap = watchTopicMap,
          isDebugBuild = isDebug
        )
        val recommendations = repository.getWatchTimeRecommendations(isDebugBuild = isDebug, limit = 24)
        val shelves = buildRecommendationShelves(
          visible = updatedVisible,
          watchVideoMap = watchVideoMap,
          watchTopicMap = watchTopicMap,
          bookmarkedIds = bookmarkedIds,
          topicDefs = config.topics
        )
        val updatedChannels = buildChannelFilterList(updatedVisible)
        val statusMsg = if (onlineNew.isNotEmpty()) {
          "Synced ${onlineNew.size} verified EMAM lectures (${updatedVisible.size} total)"
        } else {
          "Library up to date (${updatedVisible.size} verified EMAM lectures)"
        }
        _uiState.update { state ->
          state.copy(
            isSyncingOnline = false,
            syncStatusMessage = statusMsg,
            coverageDisclosure = updatedDataset.coverageDisclosure,
            seedCounts = updatedDataset.counts,
            allLectures = updatedDataset.records,
            visibleLectures = applyFilters(
              lectures = updatedVisible,
              topicId = state.selectedTopicId,
              category = state.selectedCategory,
              dateFilter = state.selectedDateFilter,
              durationFilter = state.selectedDurationFilter,
              sortOption = state.selectedSortOption,
              format = state.selectedFormatFilter,
              onlyConfirmed = state.onlyConfirmedLive,
              onlyFan = state.onlyFanChannels,
              channelHandle = state.selectedChannelHandle
            ),
            recommendedByWatchTime = recommendations,
            continueWatchingLectures = shelves.continueWatching,
            recentlyAddedLectures = shelves.recentlyAdded,
            newLectures = shelves.newLectures,
            relatedTopicsLectures = shelves.relatedTopics,
            similarLectures = shelves.similarLectures,
            unwatchedLectures = shelves.unwatched,
            visibleTopics = updatedTopics,
            availableChannels = updatedChannels,
            fanChannelTotalCount = updatedVisible.count { it.sourceTier == SourceTier.FAN_CHANNEL }
          )
        }
      }.onFailure {
        _uiState.update {
          it.copy(
            isSyncingOnline = false,
            syncStatusMessage = "Offline catalog active (${it.allLectures.size} lectures)"
          )
        }
      }
    }
  }

  fun clearHistoryAndResetRecommendations() {
    viewModelScope.launch {
      repository.lectureRepository.clearWatchAndSearchHistory()
      val isDebug = _uiState.value.isDebugBuild
      val recs = repository.getWatchTimeRecommendations(isDebugBuild = isDebug, limit = 24)
      val visible = _uiState.value.allLectures.filter { it.shouldShowInUi(isDebug) }
      val visibleTopics = repository.buildVisibleTopics(
        allLectures = _uiState.value.allLectures,
        allTopics = _uiState.value.allTopicDefinitions,
        topicWatchMap = emptyMap(),
        isDebugBuild = isDebug
      )
      val shelves = buildRecommendationShelves(
        visible = visible,
        watchVideoMap = emptyMap(),
        watchTopicMap = emptyMap(),
        bookmarkedIds = _uiState.value.bookmarkedYoutubeIds,
        topicDefs = _uiState.value.allTopicDefinitions
      )
      _uiState.update { state ->
        state.copy(
          recommendedByWatchTime = recs,
          continueWatchingLectures = shelves.continueWatching,
          recentlyAddedLectures = shelves.recentlyAdded,
          newLectures = shelves.newLectures,
          relatedTopicsLectures = shelves.relatedTopics,
          similarLectures = shelves.similarLectures,
          unwatchedLectures = shelves.unwatched,
          visibleTopics = visibleTopics,
          watchTimeByVideo = emptyMap(),
          watchTimeByTopic = emptyMap(),
          syncStatusMessage = "Watch history & recommendations reset"
        )
      }
    }
  }

  fun toggleBookmarkLecture(lecture: LectureRecord) {
    viewModelScope.launch {
      repository.lectureRepository.toggleLectureBookmark(lecture)
      val bookmarkedIds = repository.lectureRepository.getBookmarkedYoutubeIds()
      val isDebug = _uiState.value.isDebugBuild
      val recs = repository.getWatchTimeRecommendations(isDebugBuild = isDebug, limit = 24)
      val visible = _uiState.value.allLectures.filter { it.shouldShowInUi(isDebug) }
      val shelves = buildRecommendationShelves(
        visible = visible,
        watchVideoMap = _uiState.value.watchTimeByVideo,
        watchTopicMap = _uiState.value.watchTimeByTopic,
        bookmarkedIds = bookmarkedIds,
        topicDefs = _uiState.value.allTopicDefinitions
      )
      _uiState.update { state ->
        state.copy(
          bookmarkedYoutubeIds = bookmarkedIds,
          recommendedByWatchTime = recs,
          continueWatchingLectures = shelves.continueWatching,
          recentlyAddedLectures = shelves.recentlyAdded,
          newLectures = shelves.newLectures,
          relatedTopicsLectures = shelves.relatedTopics,
          similarLectures = shelves.similarLectures,
          unwatchedLectures = shelves.unwatched
        )
      }
    }
  }

  fun selectShelfTab(tab: RecommendationShelfTab) {
    _uiState.update { it.copy(selectedShelfTab = tab) }
  }

  fun selectCategory(category: IslamicVideoCategory) {
    _uiState.update { state ->
      val visible = state.allLectures.filter { it.shouldShowInUi(state.isDebugBuild) }
      state.copy(
        selectedCategory = category,
        currentPageLimit = 30,
        visibleLectures = applyFilters(
          visible,
          state.selectedTopicId,
          category,
          state.selectedDateFilter,
          state.selectedDurationFilter,
          state.selectedSortOption,
          state.selectedFormatFilter,
          state.onlyConfirmedLive,
          state.onlyFanChannels,
          state.selectedChannelHandle
        )
      )
    }
  }

  fun selectDateFilter(filter: LectureDateFilter) {
    _uiState.update { state ->
      val visible = state.allLectures.filter { it.shouldShowInUi(state.isDebugBuild) }
      state.copy(
        selectedDateFilter = filter,
        currentPageLimit = 30,
        visibleLectures = applyFilters(
          visible,
          state.selectedTopicId,
          state.selectedCategory,
          filter,
          state.selectedDurationFilter,
          state.selectedSortOption,
          state.selectedFormatFilter,
          state.onlyConfirmedLive,
          state.onlyFanChannels,
          state.selectedChannelHandle
        )
      )
    }
  }

  fun selectDurationFilter(filter: LectureDurationFilter) {
    _uiState.update { state ->
      val visible = state.allLectures.filter { it.shouldShowInUi(state.isDebugBuild) }
      state.copy(
        selectedDurationFilter = filter,
        currentPageLimit = 30,
        visibleLectures = applyFilters(
          visible,
          state.selectedTopicId,
          state.selectedCategory,
          state.selectedDateFilter,
          filter,
          state.selectedSortOption,
          state.selectedFormatFilter,
          state.onlyConfirmedLive,
          state.onlyFanChannels,
          state.selectedChannelHandle
        )
      )
    }
  }

  fun selectSortOption(sort: LectureSortOption) {
    _uiState.update { state ->
      val visible = state.allLectures.filter { it.shouldShowInUi(state.isDebugBuild) }
      state.copy(
        selectedSortOption = sort,
        visibleLectures = applyFilters(
          visible,
          state.selectedTopicId,
          state.selectedCategory,
          state.selectedDateFilter,
          state.selectedDurationFilter,
          sort,
          state.selectedFormatFilter,
          state.onlyConfirmedLive,
          state.onlyFanChannels,
          state.selectedChannelHandle
        )
      )
    }
  }

  fun loadMoreLectures() {
    _uiState.update { state ->
      state.copy(currentPageLimit = state.currentPageLimit + 30)
    }
  }

  fun selectChannelHandle(handle: String?) {
    _uiState.update { state ->
      val visible = state.allLectures.filter { it.shouldShowInUi(state.isDebugBuild) }
      state.copy(
        selectedChannelHandle = handle,
        currentPageLimit = 30,
        visibleLectures = applyFilters(
          visible,
          state.selectedTopicId,
          state.selectedCategory,
          state.selectedDateFilter,
          state.selectedDurationFilter,
          state.selectedSortOption,
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
        currentPageLimit = 30,
        visibleLectures = applyFilters(
          visible,
          state.selectedTopicId,
          state.selectedCategory,
          state.selectedDateFilter,
          state.selectedDurationFilter,
          state.selectedSortOption,
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
        currentPageLimit = 30,
        visibleLectures = applyFilters(
          visible,
          topicId,
          state.selectedCategory,
          state.selectedDateFilter,
          state.selectedDurationFilter,
          state.selectedSortOption,
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
        currentPageLimit = 30,
        visibleLectures = applyFilters(
          visible,
          state.selectedTopicId,
          state.selectedCategory,
          state.selectedDateFilter,
          state.selectedDurationFilter,
          state.selectedSortOption,
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
        currentPageLimit = 30,
        visibleLectures = applyFilters(
          visible,
          state.selectedTopicId,
          state.selectedCategory,
          state.selectedDateFilter,
          state.selectedDurationFilter,
          state.selectedSortOption,
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
    liveSearchJob?.cancel()
    if (query.isBlank()) {
      _uiState.update { it.copy(searchResults = null, isSearchingYouTube = false) }
      return
    }
    viewModelScope.launch {
      val instantRes = repository.searchAll(
        rawQuery = query,
        isDebugBuild = _uiState.value.isDebugBuild,
        includeLiveYouTube = false
      )
      if (_uiState.value.searchQuery == query) {
        _uiState.update { it.copy(searchResults = instantRes) }
      }
    }
    if (query.trim().length >= 3) {
      liveSearchJob = viewModelScope.launch {
        delay(320L)
        _uiState.update { it.copy(isSearchingYouTube = true) }
        val liveRes = repository.searchAll(
          rawQuery = query,
          isDebugBuild = _uiState.value.isDebugBuild,
          includeLiveYouTube = true
        )
        if (_uiState.value.searchQuery == query) {
          val updatedDataset = repository.lectureRepository.loadSeedDataset()
          _uiState.update {
            it.copy(
              searchResults = liveRes,
              allLectures = updatedDataset.records,
              seedCounts = updatedDataset.counts,
              coverageDisclosure = updatedDataset.coverageDisclosure,
              isSearchingYouTube = false
            )
          }
        }
      }
    }
  }

  fun openLectureDetail(lecture: LectureRecord) {
    _uiState.update {
      it.copy(
        selectedLecture = lecture,
        selectedShelfTab = RecommendationShelfTab.RECOMMENDED
      )
    }
    // Record initial engagement signal on opening a lecture so that video and its related videos surface immediately in Recommendations
    recordWatchProgress(lecture.youtubeId, 15, 15)
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
      val visible = _uiState.value.allLectures.filter { it.shouldShowInUi(isDebug) }
      val visibleTopics = repository.buildVisibleTopics(
        allLectures = _uiState.value.allLectures,
        allTopics = _uiState.value.allTopicDefinitions,
        topicWatchMap = watchTopicMap,
        isDebugBuild = isDebug
      )
      val shelves = buildRecommendationShelves(
        visible = visible,
        watchVideoMap = watchVideoMap,
        watchTopicMap = watchTopicMap,
        bookmarkedIds = _uiState.value.bookmarkedYoutubeIds,
        topicDefs = _uiState.value.allTopicDefinitions
      )
      _uiState.update { state ->
        state.copy(
          visibleLectures = applyFilters(
            lectures = visible,
            topicId = state.selectedTopicId,
            category = state.selectedCategory,
            dateFilter = state.selectedDateFilter,
            durationFilter = state.selectedDurationFilter,
            sortOption = state.selectedSortOption,
            format = state.selectedFormatFilter,
            onlyConfirmed = state.onlyConfirmedLive,
            onlyFan = state.onlyFanChannels,
            channelHandle = state.selectedChannelHandle,
            watchVideoMap = watchVideoMap,
            watchTopicMap = watchTopicMap
          ),
          recommendedByWatchTime = recs,
          continueWatchingLectures = shelves.continueWatching,
          recentlyAddedLectures = shelves.recentlyAdded,
          newLectures = shelves.newLectures,
          relatedTopicsLectures = shelves.relatedTopics,
          similarLectures = shelves.similarLectures,
          unwatchedLectures = shelves.unwatched,
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

  private data class ShelfBundle(
    val continueWatching: List<RecommendedLectureItem>,
    val recentlyAdded: List<RecommendedLectureItem>,
    val newLectures: List<RecommendedLectureItem>,
    val relatedTopics: List<RecommendedLectureItem>,
    val similarLectures: List<RecommendedLectureItem>,
    val unwatched: List<RecommendedLectureItem>
  )

  private fun buildRecommendationShelves(
    visible: List<LectureRecord>,
    watchVideoMap: Map<String, Int>,
    watchTopicMap: Map<String, Int>,
    bookmarkedIds: Set<String>,
    topicDefs: List<DiscoveryTopicDefinition>
  ): ShelfBundle {
    val topicNames = topicDefs.associate { it.id to it.titleEn }

    val watchedList = visible
      .filter { rec -> (watchVideoMap[rec.youtubeId] ?: 0) > 0 }
      .sortedByDescending { watchVideoMap[it.youtubeId] ?: 0 }
      .map { rec ->
        val watched = watchVideoMap[rec.youtubeId] ?: 0
        val dur = (rec.durationSec ?: 1800).coerceAtLeast(60)
        val pct = ((watched.toDouble() / dur.toDouble()) * 100).toInt().coerceIn(1, 100)
        RecommendedLectureItem(
          lecture = rec,
          score = 10_000.0 + watched.toDouble(),
          reasonLabel = "Watched ($pct% · ${watched}s)",
          matchedTopicIds = rec.topics,
          userWatchedSecondsOnTopic = watched
        )
      }

    val watchedIds = watchedList.map { it.lecture.youtubeId }.toSet()
    val watchedTopics = watchedList.flatMap { it.lecture.topics }.toSet()
    val relatedToWatchedList = if (watchedList.isNotEmpty()) {
      visible
        .filter { cand ->
          cand.youtubeId !in watchedIds &&
            (cand.topics.any { it in watchedTopics } ||
              watchedList.any { w ->
                cand.youtubeId in w.lecture.relatedYoutubeIds ||
                  w.lecture.youtubeId in cand.relatedYoutubeIds ||
                  (!cand.seriesName.isNullOrBlank() && cand.seriesName == w.lecture.seriesName)
              })
        }
        .sortedByDescending { cand ->
          cand.topics.sumOf { watchTopicMap[it] ?: 0 } +
            (if (watchedList.any { cand.youtubeId in it.lecture.relatedYoutubeIds }) 500 else 0)
        }
        .take(12)
        .map { rec ->
          val primaryTopic = rec.topics.firstOrNull { it in watchedTopics } ?: rec.topics.firstOrNull()
          val topicLabel = primaryTopic?.let { topicNames[it] ?: it } ?: "Related Lecture"
          RecommendedLectureItem(
            lecture = rec,
            score = 5000.0,
            reasonLabel = "Related to Watched · $topicLabel",
            matchedTopicIds = rec.topics,
            userWatchedSecondsOnTopic = primaryTopic?.let { watchTopicMap[it] ?: 0 } ?: 0
          )
        }
    } else {
      emptyList()
    }

    val continueWatching = (watchedList + relatedToWatchedList).take(16)

    val recentlyAdded = visible
      .sortedByDescending { it.recordedYear ?: 2013 }
      .take(12)
      .map { rec ->
        RecommendedLectureItem(
          lecture = rec,
          score = (rec.recordedYear ?: 2024).toDouble(),
          reasonLabel = "Recently Indexed (${rec.recordedYear ?: 2024})",
          matchedTopicIds = rec.topics,
          userWatchedSecondsOnTopic = 0
        )
      }

    val newLectures = visible
      .filter { (it.recordedYear ?: 2013) >= 2022 }
      .sortedByDescending { it.recordedYear ?: 2022 }
      .ifEmpty { visible.sortedByDescending { it.recordedYear ?: 2013 } }
      .take(12)
      .map { rec ->
        RecommendedLectureItem(
          lecture = rec,
          score = (rec.recordedYear ?: 2024).toDouble(),
          reasonLabel = "New Lecture · ${rec.recordedYear ?: 2024}",
          matchedTopicIds = rec.topics,
          userWatchedSecondsOnTopic = 0
        )
      }

    val topTopicId = watchTopicMap.maxByOrNull { it.value }?.key
      ?: visible.firstOrNull { it.youtubeId in bookmarkedIds }?.topics?.firstOrNull()
      ?: "tafseer_quran"
    val topTopicLabel = topicNames[topTopicId] ?: "Quran & Sunnah"

    val relatedTopics = visible
      .filter { topTopicId in it.topics }
      .sortedByDescending { (watchVideoMap[it.youtubeId] ?: 0) }
      .take(12)
      .map { rec ->
        RecommendedLectureItem(
          lecture = rec,
          score = 100.0,
          reasonLabel = "Related Topic · $topTopicLabel",
          matchedTopicIds = listOf(topTopicId),
          userWatchedSecondsOnTopic = watchTopicMap[topTopicId] ?: 0
        )
      }

    val anchorLecture = visible
      .filter { (watchVideoMap[it.youtubeId] ?: 0) > 0 }
      .maxByOrNull { watchVideoMap[it.youtubeId] ?: 0 }
      ?: visible.firstOrNull { it.youtubeId in bookmarkedIds }
      ?: visible.firstOrNull()

    val similarLectures = if (anchorLecture != null) {
      visible
        .filter { cand ->
          cand.youtubeId == anchorLecture.youtubeId ||
            cand.youtubeId in anchorLecture.relatedYoutubeIds ||
            anchorLecture.youtubeId in cand.relatedYoutubeIds ||
            (cand.seriesName != null && cand.seriesName == anchorLecture.seriesName) ||
            cand.topics.any { it in anchorLecture.topics }
        }
        .sortedByDescending { cand ->
          when {
            cand.youtubeId == anchorLecture.youtubeId -> 10_000
            cand.youtubeId in anchorLecture.relatedYoutubeIds || anchorLecture.youtubeId in cand.relatedYoutubeIds -> 5_000
            cand.seriesName != null && cand.seriesName == anchorLecture.seriesName -> 2_500
            else -> cand.topics.count { it in anchorLecture.topics } * 500
          }
        }
        .take(12)
        .map { rec ->
          RecommendedLectureItem(
            lecture = rec,
            score = 90.0,
            reasonLabel = if (rec.youtubeId == anchorLecture.youtubeId) {
              "Watched · ${anchorLecture.displayTitle.take(26)}"
            } else {
              "Related to ${anchorLecture.seriesName ?: anchorLecture.displayTitle.take(24)}"
            },
            matchedTopicIds = rec.topics,
            userWatchedSecondsOnTopic = watchVideoMap[rec.youtubeId] ?: 0
          )
        }
    } else {
      emptyList()
    }

    val unwatched = visible
      .filter { (watchVideoMap[it.youtubeId] ?: 0) == 0 }
      .take(12)
      .map { rec ->
        RecommendedLectureItem(
          lecture = rec,
          score = 50.0,
          reasonLabel = "Unwatched · ${rec.recordFormat.label}",
          matchedTopicIds = rec.topics,
          userWatchedSecondsOnTopic = 0
        )
      }

    return ShelfBundle(
      continueWatching = continueWatching.distinctBy { it.lecture.youtubeId },
      recentlyAdded = recentlyAdded.distinctBy { it.lecture.youtubeId },
      newLectures = newLectures.distinctBy { it.lecture.youtubeId },
      relatedTopics = relatedTopics.distinctBy { it.lecture.youtubeId },
      similarLectures = similarLectures.distinctBy { it.lecture.youtubeId },
      unwatched = unwatched.distinctBy { it.lecture.youtubeId }
    )
  }

  private fun buildChannelFilterList(lectures: List<LectureRecord>): List<ChannelFilterItem> {
    return lectures
      .distinctBy { it.youtubeId }
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
    category: IslamicVideoCategory,
    dateFilter: LectureDateFilter,
    durationFilter: LectureDurationFilter,
    sortOption: LectureSortOption,
    format: LectureFormat?,
    onlyConfirmed: Boolean,
    onlyFan: Boolean,
    channelHandle: String?,
    watchVideoMap: Map<String, Int> = _uiState.value.watchTimeByVideo,
    watchTopicMap: Map<String, Int> = _uiState.value.watchTimeByTopic
  ): List<LectureRecord> {
    val filtered = lectures
      .distinctBy { it.youtubeId }
      .filter { record ->
        (topicId == null || topicId in record.topics) &&
          category.matchesLecture(record) &&
          dateFilter.matchesYear(record.recordedYear) &&
          durationFilter.matchesDuration(record.durationSec) &&
          (format == null || record.recordFormat == format) &&
          (!onlyConfirmed || record.verificationState.name == "CONFIRMED_LIVE") &&
          (!onlyFan || record.sourceTier == SourceTier.FAN_CHANNEL) &&
          (channelHandle == null || record.channelHandle.equals(channelHandle, ignoreCase = true))
      }

    return when (sortOption) {
      LectureSortOption.RELEVANCE -> {
        val watchedIds = watchVideoMap.filterValues { it > 0 }.keys
        val watchedRecords = lectures.filter { it.youtubeId in watchedIds }
        filtered.sortedWith(
          compareByDescending<LectureRecord> { rec ->
            val directWatchedSec = watchVideoMap[rec.youtubeId] ?: 0
            val isExplicitRelated = watchedRecords.any { w ->
              rec.youtubeId in w.relatedYoutubeIds ||
                w.youtubeId in rec.relatedYoutubeIds ||
                (!rec.seriesName.isNullOrBlank() && rec.seriesName == w.seriesName)
            }
            val topicAffinitySec = rec.topics.sumOf { watchTopicMap[it] ?: 0 }
            val watchBoost = when {
              directWatchedSec > 0 -> 100_000 + directWatchedSec * 10
              isExplicitRelated && topicAffinitySec > 0 -> 40_000 + topicAffinitySec * 5
              topicAffinitySec > 0 -> 10_000 + topicAffinitySec * 2
              else -> 0
            }
            watchBoost + LectureRepository.computeEmamRelevanceScore(
              title = rec.displayTitle,
              descriptionSnippet = rec.summary ?: "",
              channelName = rec.channelDisplayName,
              channelHandle = rec.channelHandle ?: "",
              tags = rec.topics
            )
          }
            .thenBy { if (it.sourceTier == SourceTier.FAN_CHANNEL) 0 else 1 }
            .thenBy { if (it.recordFormat == LectureFormat.FULL_LECTURE) 0 else 1 }
        )
      }
      LectureSortOption.NEWEST_FIRST -> {
        filtered.sortedWith(
          compareByDescending<LectureRecord> { it.recordedYear ?: 2013 }
            .thenByDescending { it.lectureNumber ?: 0 }
        )
      }
      LectureSortOption.LONGEST_FIRST -> {
        filtered.sortedByDescending { it.durationSec ?: 0 }
      }
    }
  }
}
