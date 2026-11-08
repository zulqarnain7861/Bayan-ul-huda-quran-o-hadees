package com.example.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthManager
import com.example.data.local.ExtendedHadithDataProvider
import com.example.data.local.entities.AyahEntity
import com.example.data.local.entities.BookmarkEntity
import com.example.data.local.entities.HadithCollectionEntity
import com.example.data.local.entities.HadithEntity
import com.example.data.local.entities.NoteEntity
import com.example.data.local.entities.PrayerScheduleEntity
import com.example.data.local.SourceRegistry
import com.example.data.local.entities.SurahEntity
import com.example.data.local.entities.TranslationEntity
import com.example.data.model.ReflectionDataProvider
import com.example.data.model.ReflectionItem
import com.example.data.prayer.AppThemeMode
import com.example.data.prayer.PrayerAndAlarmConfig
import com.example.data.prayer.PrayerPreferencesManager
import com.example.data.remote.CloudBookmark
import com.example.data.remote.CloudNote
import com.example.data.repository.CloudSyncRepository
import com.example.data.repository.DiagnosticAuditReport
import com.example.data.repository.IslamicRepository
import com.example.ui.audio.AudioPlayerManager
import com.example.ui.audio.AudioState
import com.example.ui.audio.Reciter
import com.example.ui.prayer.AzanAudioController
import com.example.ui.prayer.FajrAlarmPlaybackState
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainNavigationTab {
  HOME,
  QURAN,
  HADITH,
  DUAS,
  SEARCH,
  LIBRARY
}

enum class SearchCategory {
  ALL,
  QURAN,
  HADITH,
  TAFSIR,
  SAHABA_AHL_AL_BAYT
}

enum class HadithEditionFilterMode(val label: String) {
  ALL_EDITIONS("All Editions"),
  DARUSSALAM("Darussalam #"),
  IN_BOOK("In-Book #"),
  SAHIH_INTERNATIONAL_QURAN("Sahih International (Quran Refs)")
}

data class BayanUiState(
  val currentTab: MainNavigationTab = MainNavigationTab.HOME,
  val isOpeningScreenVisible: Boolean = true,
  val isGuestModeActive: Boolean = false,
  val remainingGuestDays: Int = 7,
  // Appearance & Prayer Config
  val prayerConfig: PrayerAndAlarmConfig = PrayerAndAlarmConfig(),
  // Quran reader state
  val selectedSurah: SurahEntity? = null,
  val isReaderOpen: Boolean = false,
  val isReadingMode: Boolean = false,
  val isContinuousMushafFlow: Boolean = false,
  val highlightedVerseNumber: Int? = null,
  val showEnglish: Boolean = true,
  val showUrdu: Boolean = true,
  val showTransliteration: Boolean = true,
  val arabicFontSizeSp: Float = 24f,
  val translationFontSizeSp: Float = 15f,
  // Hadith state
  val selectedCollectionKey: String = "bukhari",
  val selectedEditionFilterMode: HadithEditionFilterMode = HadithEditionFilterMode.ALL_EDITIONS,
  val selectedHadithDetail: HadithEntity? = null,
  val isFetchingHadithByNumber: Boolean = false,
  val hadithLookupErrorMessage: String? = null,
  val previewedQuranVerse: AyahEntity? = null,
  // Search state
  val searchQuery: String = "",
  val searchCategory: SearchCategory = SearchCategory.ALL,
  val searchTraditionScope: String = "BOTH",
  val isSearchingRemote: Boolean = false,
  // Navigation shortcut targets
  val initialLibraryTab: Int = 0,
  val initialLearnIslamTab: com.example.ui.screens.LearnIslamTab = com.example.ui.screens.LearnIslamTab.NOORANI_QAIDA,
  // Daily insights
  val dailyAyah: AyahEntity? = null,
  val dailyHadith: HadithEntity? = null,
  val dailyReflection: ReflectionItem = ReflectionDataProvider.items.first(),
  // Reporting dialog & Global Error Boundary state
  val showErrorReportDialog: Boolean = false,
  val errorReportContext: String = "",
  val lastRecoverableError: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class BayanViewModel(application: Application) : AndroidViewModel(application) {

  private val vmExceptionHandler = CoroutineExceptionHandler { _, throwable ->
    Log.e("BayanViewModel", "Caught recoverable coroutine exception", throwable)
    _uiState.value = _uiState.value.copy(
      isFetchingHadithByNumber = false,
      isSearchingRemote = false,
      lastRecoverableError = throwable.localizedMessage ?: "An unexpected content loading error occurred."
    )
    _isTimetableLoading.value = false
  }

  private fun launchSafe(
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    block: suspend CoroutineScope.() -> Unit
  ): Job {
    return viewModelScope.launch(dispatcher + vmExceptionHandler) {
      runCatching { block() }.onFailure { throwable ->
        if (throwable !is kotlinx.coroutines.CancellationException) {
          Log.e("BayanViewModel", "Handled coroutine error safely", throwable)
        }
      }
    }
  }

  private val repository = IslamicRepository.getInstance(application)
  private val cloudRepository: CloudSyncRepository? = try {
    CloudSyncRepository.getInstance(application)
  } catch (e: Exception) {
    Log.w("BayanViewModel", "CloudSyncRepository unavailable in this environment", e)
    null
  }
  val audioPlayerManager = AudioPlayerManager(application)
  val duaAudioController = com.example.ui.audio.DuaAudioController(application, audioPlayerManager)
  val downloadManager = com.example.data.download.DownloadManager.getInstance(application)
  val evidenceEngine = com.example.data.evidence.EvidenceVerificationEngine(application)

  private val _sunnahRemindersState = MutableStateFlow(
    com.example.data.prayer.SunnahReminderManager.loadState(application)
  )
  val sunnahRemindersState: StateFlow<com.example.data.prayer.SunnahRemindersUiState> =
    _sunnahRemindersState.asStateFlow()

  val duaAudioState: StateFlow<com.example.ui.audio.DuaAudioPlaybackState> =
    duaAudioController.playbackState

  private val initialPrayerConfig = PrayerPreferencesManager.loadConfig(application)

  private val _uiState = MutableStateFlow(
    BayanUiState(
      isOpeningScreenVisible = !initialPrayerConfig.hasCompletedLanguageOnboarding,
      isGuestModeActive = AuthManager.isGuestSessionValid(application),
      remainingGuestDays = AuthManager.getRemainingGuestDays(application),
      prayerConfig = initialPrayerConfig
    )
  )
  val uiState: StateFlow<BayanUiState> = _uiState.asStateFlow()

  val audioState: StateFlow<AudioState> = audioPlayerManager.audioState
  val alarmPlaybackState: StateFlow<FajrAlarmPlaybackState> = AzanAudioController.playbackState

  // Today's Prayer Schedule
  private val _todayPrayerSchedule = MutableStateFlow<PrayerScheduleEntity?>(null)
  val todayPrayerSchedule: StateFlow<PrayerScheduleEntity?> = _todayPrayerSchedule.asStateFlow()

  // Yearly & Ramadan Sehri/Iftar Timetable State
  private val _sehriIftarTimetable =
    MutableStateFlow<com.example.data.prayer.SehriIftarTimetableResult?>(null)
  val sehriIftarTimetable: StateFlow<com.example.data.prayer.SehriIftarTimetableResult?> =
    _sehriIftarTimetable.asStateFlow()

  private val _isTimetableLoading = MutableStateFlow(false)
  val isTimetableLoading: StateFlow<Boolean> = _isTimetableLoading.asStateFlow()

  private val _timetableScopeMode = MutableStateFlow(
    com.example.data.prayer.TimetableScopeMode.RAMADAN_MONTH
  )
  val timetableScopeMode: StateFlow<com.example.data.prayer.TimetableScopeMode> =
    _timetableScopeMode.asStateFlow()

  private val _selectedTimetableGregorianMonth = MutableStateFlow(3)
  val selectedTimetableGregorianMonth: StateFlow<Int> =
    _selectedTimetableGregorianMonth.asStateFlow()

  // Developer Source & API Audit Report
  private val _auditReport = MutableStateFlow<DiagnosticAuditReport?>(null)
  val auditReport: StateFlow<DiagnosticAuditReport?> = _auditReport.asStateFlow()

  // Verified Source Registry Flow
  val sourceRegistryList: StateFlow<List<SourceRegistry>> = repository.getAllSourceRegistry()
    .catch { e ->
      Log.w("BayanViewModel", "sourceRegistryList flow error", e)
      emit(emptyList())
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Reactive Firebase Auth User
  val currentUser: StateFlow<FirebaseUser?> = AuthManager.authStateFlow()
    .catch { e ->
      Log.w("BayanViewModel", "authStateFlow error", e)
      emit(null)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // All 114 Surahs Flow
  val allSurahs: StateFlow<List<SurahEntity>> = repository.getAllSurahs()
    .catch { e ->
      Log.w("BayanViewModel", "allSurahs flow error, falling back to bundled 114 Surahs", e)
      emit(com.example.data.local.InitialDataLoader.getAll114Surahs())
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Hadith Collections Metadata Flow
  val hadithCollections: StateFlow<List<HadithCollectionEntity>> = repository.getAllHadithCollections()
    .catch { e ->
      Log.w("BayanViewModel", "hadithCollections flow error", e)
      emit(emptyList())
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Ayahs for current Surah
  private val _currentAyahs = MutableStateFlow<List<AyahEntity>>(emptyList())
  val currentAyahs: StateFlow<List<AyahEntity>> = _currentAyahs.asStateFlow()

  // Hadiths for current collection
  private val _collectionHadiths = MutableStateFlow<List<HadithEntity>>(emptyList())
  val collectionHadiths: StateFlow<List<HadithEntity>> = _collectionHadiths.asStateFlow()

  // Dynamic Translations for current Surah/Reference
  private val _currentTranslations = MutableStateFlow<List<TranslationEntity>>(emptyList())
  val currentTranslations: StateFlow<List<TranslationEntity>> = _currentTranslations.asStateFlow()

  // All cached Hadiths for Tafsir verse-to-hadith cross-linking
  val allHadithsForLinks: List<HadithEntity> by lazy {
    ExtendedHadithDataProvider.getExtendedHadiths()
  }

  private var surahAyahsJob: Job? = null
  private var collectionHadithsJob: Job? = null
  private var searchJob: Job? = null

  private val cloudBookmarksFlow = currentUser.flatMapLatest { user ->
    if (user == null || cloudRepository == null) {
      flowOf(emptyList())
    } else {
      cloudRepository.getBookmarksFlow(user.uid).catch { emit(emptyList()) }
    }
  }

  private val cloudNotesFlow = currentUser.flatMapLatest { user ->
    if (user == null || cloudRepository == null) {
      flowOf(emptyList())
    } else {
      cloudRepository.getNotesFlow(user.uid).catch { emit(emptyList()) }
    }
  }

  // Bookmarks merged with Cloud Firestore
  val bookmarks: StateFlow<List<BookmarkEntity>> = combine(
    repository.getAllBookmarks().catch { emit(emptyList()) },
    cloudBookmarksFlow
  ) { localList, cloudList ->
    val byKey = LinkedHashMap<String, BookmarkEntity>()
    cloudList.forEach { cloud ->
      val key = cloud.id.ifEmpty { cloud.referenceKey }
      byKey[key] = BookmarkEntity(
        itemType = if (cloud.itemType == "AYAH") "QURAN" else "HADITH",
        itemKey = key,
        title = cloud.title,
        subtitle = cloud.translationSnippet,
        arabicSnippet = cloud.arabicSnippet,
        timestamp = cloud.createdAt?.time ?: System.currentTimeMillis()
      )
    }
    localList.forEach { local ->
      if (!byKey.containsKey(local.itemKey)) {
        byKey[local.itemKey] = local
      }
    }
    byKey.values.toList()
  }.catch { emit(emptyList()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Notes merged with Cloud Firestore
  val notes: StateFlow<List<NoteEntity>> = combine(
    repository.getAllNotes().catch { emit(emptyList()) },
    cloudNotesFlow
  ) { localList, cloudList ->
    val result = mutableListOf<NoteEntity>()
    val seenTitlesAndRefs = mutableSetOf<String>()
    cloudList.forEachIndexed { index, cloud ->
      val signature = "${cloud.referenceKey}|${cloud.title}"
      seenTitlesAndRefs.add(signature)
      val syntheticId = cloud.id.removePrefix("note_").toLongOrNull() ?: (100000L + index)
      result.add(
        NoteEntity(
          id = syntheticId,
          referenceKey = cloud.referenceKey,
          title = cloud.title,
          content = cloud.content,
          updatedAt = cloud.updatedAt?.time ?: System.currentTimeMillis()
        )
      )
    }
    localList.forEach { local ->
      val signature = "${local.referenceKey}|${local.title}"
      if (!seenTitlesAndRefs.contains(signature)) {
        result.add(local)
      }
    }
    result.toList()
  }.catch { emit(emptyList()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _searchAyahResults = MutableStateFlow<List<AyahEntity>>(emptyList())
  val searchAyahResults: StateFlow<List<AyahEntity>> = _searchAyahResults.asStateFlow()

  private val _searchHadithResults = MutableStateFlow<List<HadithEntity>>(emptyList())
  val searchHadithResults: StateFlow<List<HadithEntity>> = _searchHadithResults.asStateFlow()

  private val _searchTafsirResults = MutableStateFlow<List<com.example.data.repository.AyahTafsirResult>>(emptyList())
  val searchTafsirResults: StateFlow<List<com.example.data.repository.AyahTafsirResult>> = _searchTafsirResults.asStateFlow()

  init {
    viewModelScope.launch(Dispatchers.IO) {
      runCatching { loadCollectionHadiths("bukhari") }
      runCatching { repository.initializeDatabaseIfNeeded() }
      runCatching { loadDailyContent() }
      runCatching { refreshPrayerSchedule(forceRemote = false) }
      runCatching { refreshSehriIftarTimetable(forceRemote = false) }
      runCatching { com.example.data.prayer.SunnahReminderManager.scheduleAllSunnahReminders(getApplication()) }
      runCatching {
        _sunnahRemindersState.value =
          com.example.data.prayer.SunnahReminderManager.ensureTodayCalculatedNotificationSentIfEnabled(getApplication())
      }
      runCatching { cleanupOldWavFiles() }
    }
  }

  private fun cleanupOldWavFiles() {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val app = getApplication<Application>()
        val dir = File(app.filesDir, "adhan")
        if (dir.exists()) {
          dir.listFiles()?.forEach { file ->
            if (file.name.endsWith(".wav")) {
              file.delete()
            }
          }
        }
      } catch (_: Exception) {}
    }
  }

  fun dismissOpeningScreen() {
    _uiState.value = _uiState.value.copy(isOpeningScreenVisible = false)
  }

  fun continueAsGuest() {
    val app = getApplication<Application>()
    val valid = AuthManager.startOrResumeGuestSession(app)
    val remaining = AuthManager.getRemainingGuestDays(app)
    _uiState.value = _uiState.value.copy(
      isGuestModeActive = valid,
      remainingGuestDays = remaining
    )
  }

  fun onGoogleSignInSuccess(user: FirebaseUser) {
    val app = getApplication<Application>()
    AuthManager.endGuestSession(app)
    _uiState.value = _uiState.value.copy(
      isGuestModeActive = false
    )
    val cloudRepo = cloudRepository ?: return
    viewModelScope.launch(Dispatchers.IO) {
      runCatching {
        bookmarks.value.forEach { localBookmark ->
          val cloudBookmark = CloudBookmark(
            itemType = if (localBookmark.itemType == "QURAN") "AYAH" else "HADITH",
            referenceKey = localBookmark.itemKey,
            title = localBookmark.title,
            arabicSnippet = localBookmark.arabicSnippet.take(1500),
            translationSnippet = localBookmark.subtitle.take(1500)
          )
          cloudRepo.saveBookmark(user.uid, localBookmark.itemKey, cloudBookmark)
        }
        notes.value.forEach { localNote ->
          val noteId = "note_${localNote.id}"
          val cloudNote = CloudNote(
            itemType = "AYAH",
            referenceKey = localNote.referenceKey.ifBlank { "General" }.take(100),
            title = localNote.title.take(200),
            content = localNote.content.take(5000)
          )
          cloudRepo.saveNote(user.uid, noteId, cloudNote)
        }
      }
    }
  }

  fun selectTab(tab: MainNavigationTab) {
    _uiState.value = _uiState.value.copy(currentTab = tab)
  }

  // Theme & Prayer Configuration
  fun updatePrayerAndAlarmConfig(newConfig: PrayerAndAlarmConfig) {
    val app = getApplication<Application>()
    PrayerPreferencesManager.saveConfig(app, newConfig)
    _uiState.value = _uiState.value.copy(prayerConfig = newConfig)
    refreshPrayerSchedule(forceRemote = false)
    refreshSehriIftarTimetable(forceRemote = false)
  }

  fun selectTimetableScopeMode(mode: com.example.data.prayer.TimetableScopeMode) {
    _timetableScopeMode.value = mode
    refreshSehriIftarTimetable(forceRemote = false)
  }

  fun selectTimetableGregorianYear(gregorianYear: Int) {
    val validYear = gregorianYear.coerceIn(1990, 2100)
    val ramadanYears =
      com.example.data.prayer.SehriIftarTimetableEngine.getHijriRamadanYearsForGregorianYear(validYear)
    val currentCfg = _uiState.value.prayerConfig
    val targetHijri = if (ramadanYears.contains(currentCfg.selectedTimetableHijriYear)) {
      currentCfg.selectedTimetableHijriYear
    } else {
      ramadanYears.firstOrNull() ?: currentCfg.selectedTimetableHijriYear
    }
    val updated = currentCfg.copy(
      selectedTimetableGregorianYear = validYear,
      selectedTimetableHijriYear = targetHijri
    )
    updatePrayerAndAlarmConfig(updated)
  }

  fun selectTimetableHijriYear(hijriYear: Int) {
    val validHijri = hijriYear.coerceIn(1400, 1500)
    val approxGregorian =
      com.example.data.prayer.SehriIftarTimetableEngine.getPrimaryGregorianYearForHijriRamadan(validHijri)
    val currentCfg = _uiState.value.prayerConfig
    val dualForCurrent =
      com.example.data.prayer.SehriIftarTimetableEngine.getHijriRamadanYearsForGregorianYear(
        currentCfg.selectedTimetableGregorianYear
      )
    val targetGregorian = if (dualForCurrent.contains(validHijri)) {
      currentCfg.selectedTimetableGregorianYear
    } else {
      approxGregorian
    }
    val updated = currentCfg.copy(
      selectedTimetableHijriYear = validHijri,
      selectedTimetableGregorianYear = targetGregorian
    )
    updatePrayerAndAlarmConfig(updated)
  }

  fun selectTimetableGregorianMonth(month: Int) {
    _selectedTimetableGregorianMonth.value = month.coerceIn(1, 12)
    refreshSehriIftarTimetable(forceRemote = false)
  }

  fun refreshSehriIftarTimetable(forceRemote: Boolean = false) {
    launchSafe(Dispatchers.IO) {
      _isTimetableLoading.value = true
      val cfg = _uiState.value.prayerConfig
      val result = runCatching {
        repository.getOrFetchSehriIftarTimetable(
          scopeMode = _timetableScopeMode.value,
          city = cfg.city,
          country = cfg.country,
          latitude = cfg.latitude,
          longitude = cfg.longitude,
          timeZoneId = cfg.timeZoneId,
          methodId = cfg.calculationMethodId,
          school = com.example.data.prayer.JuridicalSchool.fromId(cfg.asrMadhhab),
          gregorianYear = cfg.selectedTimetableGregorianYear,
          gregorianMonth = _selectedTimetableGregorianMonth.value,
          hijriRamadanYear = cfg.selectedTimetableHijriYear,
          sehriAdjustmentMinutes = cfg.sehriAdjustmentMinutes,
          iftarAdjustmentMinutes = cfg.iftarAdjustmentMinutes,
          forceRemoteRefresh = forceRemote
        )
      }.getOrNull()
      if (result != null) {
        _sehriIftarTimetable.value = result
      }
      _isTimetableLoading.value = false
    }
  }

  fun applyTrueMapLocationAndCacheMonth(
    latitude: Double,
    longitude: Double,
    city: String,
    country: String,
    timeZoneId: String
  ) {
    val nearestPreset =
      com.example.data.prayer.SehriIftarTimetableEngine.resolveNearestCityForCoordinates(latitude, longitude)
    val currentCfg = _uiState.value.prayerConfig
    val updatedCfg = currentCfg.copy(
      city = city.ifBlank { nearestPreset.city },
      districtOrRegion = nearestPreset.districtOrRegion,
      country = country.ifBlank { nearestPreset.country },
      latitude = latitude,
      longitude = longitude,
      timeZoneId = timeZoneId.ifBlank { nearestPreset.timeZoneId },
      calculationMethodId = nearestPreset.defaultMethodId,
      asrMadhhab = nearestPreset.defaultAsrMadhhab
    )
    val app = getApplication<Application>()
    PrayerPreferencesManager.saveConfig(app, updatedCfg)
    _uiState.value = _uiState.value.copy(prayerConfig = updatedCfg)
    refreshPrayerSchedule(forceRemote = true)

    // Proactively fetch and cache both the current Ramadan month AND current Gregorian month
    viewModelScope.launch(Dispatchers.IO) {
      _isTimetableLoading.value = true
      val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
      _selectedTimetableGregorianMonth.value = currentMonth
      val school = com.example.data.prayer.JuridicalSchool.fromId(updatedCfg.asrMadhhab)
      // Cache Gregorian Month for true map location
      repository.getOrFetchSehriIftarTimetable(
        scopeMode = com.example.data.prayer.TimetableScopeMode.GREGORIAN_MONTH,
        city = updatedCfg.city,
        country = updatedCfg.country,
        latitude = updatedCfg.latitude,
        longitude = updatedCfg.longitude,
        timeZoneId = updatedCfg.timeZoneId,
        methodId = updatedCfg.calculationMethodId,
        school = school,
        gregorianYear = updatedCfg.selectedTimetableGregorianYear,
        gregorianMonth = currentMonth,
        hijriRamadanYear = updatedCfg.selectedTimetableHijriYear,
        sehriAdjustmentMinutes = updatedCfg.sehriAdjustmentMinutes,
        iftarAdjustmentMinutes = updatedCfg.iftarAdjustmentMinutes,
        forceRemoteRefresh = false
      )
      // And load the active scope mode (Ramadan / Monthly / Yearly)
      val activeResult = repository.getOrFetchSehriIftarTimetable(
        scopeMode = _timetableScopeMode.value,
        city = updatedCfg.city,
        country = updatedCfg.country,
        latitude = updatedCfg.latitude,
        longitude = updatedCfg.longitude,
        timeZoneId = updatedCfg.timeZoneId,
        methodId = updatedCfg.calculationMethodId,
        school = school,
        gregorianYear = updatedCfg.selectedTimetableGregorianYear,
        gregorianMonth = currentMonth,
        hijriRamadanYear = updatedCfg.selectedTimetableHijriYear,
        sehriAdjustmentMinutes = updatedCfg.sehriAdjustmentMinutes,
        iftarAdjustmentMinutes = updatedCfg.iftarAdjustmentMinutes,
        forceRemoteRefresh = false
      )
      _sehriIftarTimetable.value = activeResult
      _isTimetableLoading.value = false
    }
  }

  fun setThemeMode(mode: AppThemeMode) {
    val updated = _uiState.value.prayerConfig.copy(themeMode = mode)
    updatePrayerAndAlarmConfig(updated)
  }

  fun setAppInterfaceLanguage(languageCode: String, syncContentTranslation: Boolean = false) {
    val currentConfig = _uiState.value.prayerConfig
    val langMeta = com.example.ui.localization.AppLocalization.findInterfaceLanguage(languageCode)
    val updatedConfig = if (syncContentTranslation) {
      currentConfig.copy(
        appInterfaceLanguageCode = langMeta.code,
        hasCompletedLanguageOnboarding = true,
        sunniTranslationKey = langMeta.defaultSunniQuranEditionKey,
        shiaTranslationKey = langMeta.defaultShiaQuranEditionKey
      )
    } else {
      currentConfig.copy(
        appInterfaceLanguageCode = langMeta.code,
        hasCompletedLanguageOnboarding = true
      )
    }
    updatePrayerAndAlarmConfig(updatedConfig)
    _uiState.value = _uiState.value.copy(isOpeningScreenVisible = false)
  }

  fun cycleThemeMode() {
    val entries = AppThemeMode.entries
    val currentIdx = entries.indexOf(_uiState.value.prayerConfig.themeMode).coerceAtLeast(0)
    val next = entries[(currentIdx + 1) % entries.size]
    setThemeMode(next)
  }

  fun refreshPrayerSchedule(forceRemote: Boolean = true) {
    launchSafe(Dispatchers.IO) {
      val schedule = runCatching {
        repository.getOrRefreshTodayPrayerSchedule(forceRemoteRefresh = forceRemote)
      }.getOrNull()
      if (schedule != null) {
        _todayPrayerSchedule.value = schedule
      }
    }
  }

  fun testAzanOrFajrAlarm(prayerName: String, isSpecialFajrAlarm: Boolean, muezzinOverrideId: String? = null) {
    AzanAudioController.startAzanOrFajrAlarm(
      context = getApplication(),
      prayerName = prayerName,
      isSpecial15MinFajrAlarm = isSpecialFajrAlarm,
      muezzinOverrideId = muezzinOverrideId
    )
  }

  fun testMuezzinAzan(muezzin: com.example.data.prayer.MuezzinAudioOption) {
    AzanAudioController.testMuezzinAzan(getApplication(), muezzin)
  }

  fun startContinuousVibrationTest() {
    AzanAudioController.startContinuousVibrationTest(getApplication())
  }

  fun stopContinuousVibration() {
    AzanAudioController.stopContinuousVibration(getApplication())
  }

  fun stopActiveAzanOrAlarm() {
    AzanAudioController.stopAlarm(getApplication())
  }

  fun snoozeActiveAzanOrAlarm(minutes: Int) {
    AzanAudioController.snoozeActiveAlarm(getApplication(), minutes)
  }

  fun refreshAuditReport(checkNetworkLive: Boolean = true) {
    launchSafe(Dispatchers.IO) {
      _auditReport.value = repository.generateDiagnosticAuditReport(checkNetworkLive = checkNetworkLive)
    }
  }

  fun signOut() {
    launchSafe(Dispatchers.IO) {
      val app = getApplication<Application>()
      AuthManager.signOut(app)
      _uiState.value = _uiState.value.copy(
        isGuestModeActive = false,
        remainingGuestDays = AuthManager.getRemainingGuestDays(app)
      )
    }
  }

  /**
   * Permanently deletes the signed-in Firebase account, all cloud-synced Firestore bookmarks & notes
   * (`/users/{uid}/bookmarks` and `/users/{uid}/notes`), and (when [deleteLocalBookmarksAndNotes] is true)
   * all local user-authored bookmarks and study notes stored on the device.
   */
  fun deleteAccountAndData(
    activityContext: android.content.Context,
    deleteLocalBookmarksAndNotes: Boolean = true,
    onResult: (Boolean, String) -> Unit
  ) {
    viewModelScope.launch {
      val app = getApplication<Application>()
      if (deleteLocalBookmarksAndNotes) {
        runCatching {
          val currentBookmarks = bookmarks.value.toList()
          for (bm in currentBookmarks) {
            repository.removeBookmark(bm.itemKey)
          }
          val currentNotes = notes.value.toList()
          for (note in currentNotes) {
            repository.deleteNote(note.id)
          }
        }
      }

      val result = AuthManager.deleteCurrentAccount(
        activityContext = activityContext,
        onDeleteCloudData = { uid ->
          cloudRepository?.deleteAllUserData(uid) ?: 0
        }
      )

      result.onSuccess { msg ->
        _uiState.value = _uiState.value.copy(
          isGuestModeActive = false,
          remainingGuestDays = AuthManager.getRemainingGuestDays(app)
        )
        onResult(true, msg)
      }.onFailure { err ->
        val friendly = err.localizedMessage
          ?: "Could not complete automatic account deletion. Please try signing in again or use the email deletion request option."
        onResult(false, friendly)
      }
    }
  }

  private suspend fun loadDailyContent() {
    val ayah = repository.getRandomAyah()
    val hadith = repository.getRandomHadith()
    _uiState.value = _uiState.value.copy(
      dailyAyah = ayah,
      dailyHadith = hadith
    )
  }

  // Quran Actions
  fun openSurahReader(surah: SurahEntity, highlightVerse: Int? = null) {
    val activeAudioSurah = audioPlayerManager.audioState.value.surahNumber
    if (activeAudioSurah != null && activeAudioSurah != surah.number) {
      audioPlayerManager.stop()
    }
    val cfg = _uiState.value.prayerConfig
    val targetVerse = highlightVerse ?: if (cfg.lastReadSurahNumber == surah.number && cfg.lastReadTimestamp > 0L) {
      cfg.lastReadVerseNumber.coerceIn(1, surah.totalVerses.coerceAtLeast(1))
    } else {
      null
    }
    _uiState.value = _uiState.value.copy(
      selectedSurah = surah,
      isReaderOpen = true,
      highlightedVerseNumber = targetVerse,
      currentTab = MainNavigationTab.QURAN
    )
    saveQuranReadingProgress(
      surah = surah,
      verseNumber = targetVerse ?: 1,
      tradition = cfg.lastReadTradition
    )
    surahAyahsJob?.cancel()
    surahAyahsJob = launchSafe(Dispatchers.IO) {
      // 1. Immediately provide offline bundled Quran verses so the reader opens with zero delay or blank state
      runCatching {
        val currentLoadedSurah = _currentAyahs.value.firstOrNull()?.surahNumber
        if (currentLoadedSurah != surah.number) {
          val offlineAyahs = com.example.data.local.OfflineQuranAssetLoader.loadSurahFromAssets(
            getApplication(),
            surah.number
          )
          if (offlineAyahs.isNotEmpty()) {
            _currentAyahs.value = offlineAyahs
            audioPlayerManager.setSurahAyahWeights(
              surahNumber = surah.number,
              weights = offlineAyahs.sortedBy { it.verseNumber }.map { it.textArabic.length.coerceAtLeast(12) }
            )
          }
        }
      }

      // 2. Start collecting Room Ayah & Translation streams immediately (never blocked by network calls)
      launch(vmExceptionHandler) {
        repository.getAyahsBySurah(surah.number)
          .catch { e ->
            Log.w("BayanViewModel", "getAyahsBySurah flow error for surah ${surah.number}", e)
          }
          .collect { ayahs ->
            if (ayahs.isNotEmpty()) {
              _currentAyahs.value = ayahs
              audioPlayerManager.setSurahAyahWeights(
                surahNumber = surah.number,
                weights = ayahs.sortedBy { it.verseNumber }.map { it.textArabic.length.coerceAtLeast(12) }
              )
            }
          }
      }
      launch(vmExceptionHandler) {
        // Load all Quran translations for this Surah e.g. en.qarai, ur.jalandhry, qul.*, etc.
        repository.getTranslationsForSurah(surah.number)
          .catch { e ->
            Log.w("BayanViewModel", "getTranslationsForSurah flow error for surah ${surah.number}", e)
            emit(emptyList())
          }
          .collect { trans ->
            _currentTranslations.value = trans
          }
      }

      // 3. Ensure local Room seed and selected translations in background without blocking the UI
      launch(Dispatchers.IO + vmExceptionHandler) {
        runCatching { repository.ensureSurahAyahsLoaded(surah.number) }
        val currentCfg = _uiState.value.prayerConfig
        runCatching { repository.ensureSurahTranslationsLoaded(surah.number, currentCfg.sunniTranslationKey) }
        if (currentCfg.shiaTranslationKey != currentCfg.sunniTranslationKey) {
          runCatching { repository.ensureQulEditionLoadedForSurah(surah.number, currentCfg.shiaTranslationKey) }
        }
        currentCfg.selectedTranslationKeys.forEach { key ->
          if (key != currentCfg.sunniTranslationKey && key != currentCfg.shiaTranslationKey) {
            runCatching { repository.ensureQulEditionLoadedForSurah(surah.number, key) }
          }
        }
      }
    }
  }

  /**
   * Opens or previews an exact Quran verse from a Sahih International cross-reference badge (e.g. "2:255").
   */
  fun openOrPreviewQuranReference(verseRef: String, openFullSurahReader: Boolean = false) {
    val clean = verseRef.trim().removePrefix("Quran").trim()
    val parts = clean.split(":")
    val sNum = parts.getOrNull(0)?.trim()?.toIntOrNull()?.coerceIn(1, 114) ?: return
    val vNum = parts.getOrNull(1)?.trim()?.toIntOrNull()?.coerceAtLeast(1) ?: 1

    launchSafe(Dispatchers.IO) {
      val ayah = runCatching { repository.getAyah(sNum, vNum) }.getOrNull()
      val surah = runCatching { repository.getSurahByNumber(sNum) }.getOrNull()
        ?: com.example.data.local.InitialDataLoader.getAll114Surahs().find { it.number == sNum }
      if (openFullSurahReader && surah != null) {
        _uiState.value = _uiState.value.copy(previewedQuranVerse = null)
        openSurahReader(surah, highlightVerse = vNum)
      } else {
        _uiState.value = _uiState.value.copy(previewedQuranVerse = ayah)
      }
    }
  }

  fun dismissPreviewedQuranVerse() {
    _uiState.value = _uiState.value.copy(previewedQuranVerse = null)
  }

  fun navigateToAdjacentSurah(offset: Int) {
    val current = _uiState.value.selectedSurah ?: return
    val targetNumber = (current.number + offset).coerceIn(1, 114)
    if (targetNumber == current.number) return
    val targetSurah = allSurahs.value.find { it.number == targetNumber }
    if (targetSurah != null) {
      openSurahReader(targetSurah)
    } else {
      launchSafe(Dispatchers.IO) {
        val dbSurah = runCatching { repository.getSurahByNumber(targetNumber) }.getOrNull()
          ?: com.example.data.local.InitialDataLoader.getAll114Surahs().find { it.number == targetNumber }
        if (dbSurah != null) {
          openSurahReader(dbSurah)
        }
      }
    }
  }

  fun closeSurahReader() {
    _uiState.value = _uiState.value.copy(
      isReaderOpen = false,
      isReadingMode = false,
      highlightedVerseNumber = null
    )
    audioPlayerManager.stop()
  }

  fun toggleReadingMode() {
    _uiState.value = _uiState.value.copy(isReadingMode = !_uiState.value.isReadingMode)
  }

  fun setReadingMode(enabled: Boolean) {
    _uiState.value = _uiState.value.copy(isReadingMode = enabled)
  }

  fun toggleContinuousMushafFlow() {
    _uiState.value = _uiState.value.copy(isContinuousMushafFlow = !_uiState.value.isContinuousMushafFlow)
  }

  fun toggleEnglishTranslation() {
    _uiState.value = _uiState.value.copy(showEnglish = !_uiState.value.showEnglish)
  }

  fun toggleUrduTranslation() {
    _uiState.value = _uiState.value.copy(showUrdu = !_uiState.value.showUrdu)
  }

  fun toggleTransliteration() {
    _uiState.value = _uiState.value.copy(showTransliteration = !_uiState.value.showTransliteration)
  }

  fun setArabicFontSize(sp: Float) {
    _uiState.value = _uiState.value.copy(arabicFontSizeSp = sp.coerceIn(18f, 38f))
  }

  fun setTranslationFontSize(sp: Float) {
    _uiState.value = _uiState.value.copy(translationFontSizeSp = sp.coerceIn(12f, 22f))
  }

  // Audio Playback
  fun playAyahRecitation(ayah: AyahEntity, surahName: String) {
    val openSurah = _uiState.value.selectedSurah
    val targetSurahNumber = openSurah?.number?.takeIf {
      _uiState.value.isReaderOpen && it == ayah.surahNumber
    } ?: ayah.surahNumber
    val targetSurahName = surahName.ifBlank { openSurah?.nameEnglish.orEmpty() }
    val ayahs = _currentAyahs.value.filter { it.surahNumber == targetSurahNumber }
    if (ayahs.isNotEmpty()) {
      audioPlayerManager.setSurahAyahWeights(
        surahNumber = targetSurahNumber,
        weights = ayahs.sortedBy { it.verseNumber }.map { it.textArabic.length.coerceAtLeast(12) }
      )
    }
    audioPlayerManager.playAyah(targetSurahNumber, ayah.verseNumber, targetSurahName)
  }

  fun saveQuranReadingProgress(surah: SurahEntity, verseNumber: Int, tradition: String = "SUNNI") {
    val safeVerse = verseNumber.coerceIn(1, surah.totalVerses.coerceAtLeast(1))
    val currentConfig = _uiState.value.prayerConfig
    if (currentConfig.lastReadSurahNumber == surah.number &&
      currentConfig.lastReadVerseNumber == safeVerse &&
      currentConfig.lastReadTradition == tradition &&
      currentConfig.lastReadTimestamp > 0L
    ) {
      return
    }
    val updatedConfig = currentConfig.copy(
      lastReadSurahNumber = surah.number,
      lastReadVerseNumber = safeVerse,
      lastReadSurahNameEn = surah.nameEnglish,
      lastReadSurahNameAr = surah.nameArabic,
      lastReadTotalVerses = surah.totalVerses,
      lastReadTradition = tradition,
      lastReadTimestamp = System.currentTimeMillis()
    )
    val app = getApplication<Application>()
    PrayerPreferencesManager.saveConfig(app, updatedConfig)
    _uiState.value = _uiState.value.copy(prayerConfig = updatedConfig)
  }

  fun resumeLastReadQuran(surahNumberOverride: Int? = null, verseNumberOverride: Int? = null) {
    val cfg = _uiState.value.prayerConfig
    val targetSurahNum = (surahNumberOverride ?: cfg.lastReadSurahNumber).coerceIn(1, 114)
    val targetVerseNum = (verseNumberOverride ?: cfg.lastReadVerseNumber).coerceAtLeast(1)
    val cachedSurah = allSurahs.value.find { it.number == targetSurahNum }
    if (cachedSurah != null) {
      openSurahReader(cachedSurah, highlightVerse = targetVerseNum)
    } else {
      launchSafe(Dispatchers.IO) {
        val dbSurah = runCatching { repository.getSurahByNumber(targetSurahNum) }.getOrNull()
          ?: com.example.data.local.InitialDataLoader.getAll114Surahs().find { it.number == targetSurahNum }
        if (dbSurah != null) {
          openSurahReader(dbSurah, highlightVerse = targetVerseNum)
        }
      }
    }
  }

  fun setReciter(reciter: Reciter) {
    val currentConfig = _uiState.value.prayerConfig
    val updatedConfig = if (reciter.tradition.equals("SHIA", ignoreCase = true)) {
      currentConfig.copy(shiaQuranReciterId = reciter.id)
    } else {
      currentConfig.copy(sunniQuranReciterId = reciter.id)
    }
    updatePrayerAndAlarmConfig(updatedConfig)
    audioPlayerManager.setReciter(reciter)
  }

  fun setQuranReciter(reciter: Reciter, tradition: String) {
    val currentConfig = _uiState.value.prayerConfig
    val updatedConfig = if (tradition == "SUNNI") {
      currentConfig.copy(sunniQuranReciterId = reciter.id)
    } else {
      currentConfig.copy(shiaQuranReciterId = reciter.id)
    }
    updatePrayerAndAlarmConfig(updatedConfig)
    audioPlayerManager.setReciter(reciter)
  }

  fun setQuranTranslationKey(key: String, tradition: String) {
    val currentConfig = _uiState.value.prayerConfig
    val nextSet = currentConfig.selectedTranslationKeys + key
    val updatedConfig = if (tradition == "SUNNI") {
      currentConfig.copy(sunniTranslationKey = key, selectedTranslationKeys = nextSet)
    } else {
      currentConfig.copy(shiaTranslationKey = key, selectedTranslationKeys = nextSet)
    }
    updatePrayerAndAlarmConfig(updatedConfig)
    val openSurahNum = _uiState.value.selectedSurah?.number
    if (openSurahNum != null) {
      launchSafe(Dispatchers.IO) {
        repository.ensureQulEditionLoadedForSurah(openSurahNum, key)
      }
    }
  }

  fun toggleSelectedTranslationKey(key: String, tradition: String) {
    val currentConfig = _uiState.value.prayerConfig
    val currentSet = currentConfig.selectedTranslationKeys
    val isRemoving = currentSet.contains(key) && currentSet.size > 1
    val nextSet = if (isRemoving) currentSet - key else currentSet + key
    val primaryKey = if (!isRemoving) key else (nextSet.firstOrNull() ?: key)
    val updatedConfig = if (tradition == "SUNNI") {
      currentConfig.copy(
        sunniTranslationKey = primaryKey,
        selectedTranslationKeys = nextSet
      )
    } else {
      currentConfig.copy(
        shiaTranslationKey = primaryKey,
        selectedTranslationKeys = nextSet
      )
    }
    updatePrayerAndAlarmConfig(updatedConfig)
    val openSurahNum = _uiState.value.selectedSurah?.number
    if (!isRemoving && openSurahNum != null) {
      launchSafe(Dispatchers.IO) {
        repository.ensureQulEditionLoadedForSurah(openSurahNum, key)
      }
    }
  }

  // Hadith Actions
  fun selectHadithCollection(collectionKey: String) {
    _uiState.value = _uiState.value.copy(
      selectedCollectionKey = collectionKey,
      selectedHadithDetail = null,
      hadithLookupErrorMessage = null
    )
    loadCollectionHadiths(collectionKey)
  }

  fun clearHadithLookupError() {
    if (_uiState.value.hadithLookupErrorMessage != null) {
      _uiState.value = _uiState.value.copy(hadithLookupErrorMessage = null)
    }
  }

  fun selectHadithEditionFilterMode(mode: HadithEditionFilterMode) {
    _uiState.value = _uiState.value.copy(selectedEditionFilterMode = mode)
  }

  private fun loadCollectionHadiths(collectionKey: String) {
    collectionHadithsJob?.cancel()
    val fallbackCatalog = runCatching {
      com.example.data.local.ExtendedHadithDataProvider.getExtendedHadiths()
        .filter { it.collectionKey == collectionKey }
    }.getOrDefault(emptyList())
    if (fallbackCatalog.isNotEmpty()) {
      _collectionHadiths.value = fallbackCatalog.sortedBy { it.hadithNumber }
    }
    collectionHadithsJob = launchSafe(Dispatchers.IO) {
      val appCtx = getApplication<Application>()
      val hasBundled = com.example.data.local.OfflineHadithAssetLoader.hasBundledAsset(collectionKey)
      val bundledList = runCatching {
        if (hasBundled) {
          com.example.data.local.OfflineHadithAssetLoader.loadCollection(appCtx, collectionKey)
        } else {
          emptyList()
        }
      }.getOrDefault(emptyList())
      if (bundledList.isNotEmpty()) {
        _collectionHadiths.value = bundledList
      }
      var triggeredInitialApiSectionFetch = false
      repository.getHadithsByCollection(collectionKey)
        .catch { e ->
          Log.w("BayanViewModel", "getHadithsByCollection flow error for $collectionKey", e)
          emit(emptyList())
        }
        .collect { dbList ->
          val merged = LinkedHashMap<Int, HadithEntity>()
          for (h in bundledList) {
            merged[h.hadithNumber] = h
          }
          for (h in dbList) {
            merged[h.hadithNumber] = h
          }
          for (h in fallbackCatalog) {
            val existing = merged[h.hadithNumber]
            if (existing == null) {
              merged[h.hadithNumber] = h
            } else if (existing.urduText.isBlank() && h.urduText.isNotBlank()) {
              merged[h.hadithNumber] = existing.copy(
                urduText = h.urduText,
                transliteration = existing.transliteration.ifBlank { h.transliteration },
                linkedQuranVerses = existing.linkedQuranVerses.ifBlank { h.linkedQuranVerses }
              )
            }
          }
          _collectionHadiths.value = merged.values.sortedBy { it.hadithNumber }
          if (!hasBundled && !triggeredInitialApiSectionFetch && dbList.size < 20) {
            triggeredInitialApiSectionFetch = true
            launchSafe(Dispatchers.IO) {
              runCatching { repository.loadHadithBookSectionFromApi(collectionKey, 1) }
            }
          }
        }
    }
  }

  private fun prefetchAdjacentHadithsInBackground(collectionKey: String, hadithNumber: Int) {
    if (com.example.data.local.OfflineHadithAssetLoader.hasBundledAsset(collectionKey)) return
    launchSafe(Dispatchers.IO) {
      val nextNum = hadithNumber + 1
      runCatching { repository.findOrFetchHadithByNumber(collectionKey, nextNum) }
      if (hadithNumber > 1) {
        val prevNum = hadithNumber - 1
        runCatching { repository.findOrFetchHadithByNumber(collectionKey, prevNum) }
      }
    }
  }

  fun lookupHadithByNumber(hadithNumber: Int, collectionKeyOverride: String? = null) {
    val collectionKey = collectionKeyOverride?.takeIf { it.isNotBlank() } ?: _uiState.value.selectedCollectionKey
    val bookMeta = com.example.data.local.ExtendedHadithDataProvider.primaryCollections.find {
      it.key.equals(collectionKey, ignoreCase = true)
    }
    val bookName = bookMeta?.let {
      com.example.data.local.ExtendedHadithDataProvider.cleanBookTitle(it.nameEnglish)
    } ?: collectionKey

    launchSafe(Dispatchers.IO) {
      _uiState.value = _uiState.value.copy(
        selectedCollectionKey = collectionKey,
        isFetchingHadithByNumber = true,
        hadithLookupErrorMessage = null
      )
      val hadith = runCatching {
        repository.findOrFetchHadithByNumber(collectionKey, hadithNumber)
      }.getOrNull()
      val verifiedHadith = hadith?.takeIf {
        it.collectionKey.equals(collectionKey, ignoreCase = true) && it.hadithNumber == hadithNumber
      }
      val range = runCatching {
        com.example.data.local.OfflineHadithAssetLoader.getNumberRangeForCollection(
          getApplication(),
          collectionKey
        )
      }.getOrDefault(1..7563)
      _uiState.value = _uiState.value.copy(
        isFetchingHadithByNumber = false,
        selectedHadithDetail = verifiedHadith ?: _uiState.value.selectedHadithDetail,
        hadithLookupErrorMessage = if (verifiedHadith == null) {
          "Hadith #$hadithNumber not found in $bookName (range is ${range.first}–${range.last})"
        } else {
          null
        }
      )
      if (verifiedHadith != null) {
        prefetchAdjacentHadithsInBackground(collectionKey, hadithNumber)
      }
    }
  }

  fun loadHadithBookSection(bookNumber: Int) {
    val collectionKey = _uiState.value.selectedCollectionKey
    launchSafe(Dispatchers.IO) {
      _uiState.value = _uiState.value.copy(isFetchingHadithByNumber = true)
      runCatching { repository.loadHadithBookSectionFromApi(collectionKey, bookNumber) }
      _uiState.value = _uiState.value.copy(isFetchingHadithByNumber = false)
    }
  }

  fun showHadithDetail(hadith: HadithEntity) {
    _uiState.value = _uiState.value.copy(selectedHadithDetail = hadith)
    prefetchAdjacentHadithsInBackground(hadith.collectionKey, hadith.hadithNumber)
  }

  fun closeHadithDetail() {
    _uiState.value = _uiState.value.copy(selectedHadithDetail = null)
  }

  // Two-Stage Global Hybrid Search Engine:
  // Stage 1: Instant Offline Search (All 6,236 Quran verses, all 36 bundled Hadith collections, local Tafsir & translations)
  // Stage 2: Online Global Search (AlQuran Cloud, Quran.com v4, FawazAhmed0 Sunni API, Thaqalayn v2 Shia API, QuranEnc Tafsir)
  fun onSearchQueryChanged(query: String) {
    _uiState.value = _uiState.value.copy(searchQuery = query)
    searchJob?.cancel()
    if (query.isBlank()) {
      _searchAyahResults.value = emptyList()
      _searchHadithResults.value = emptyList()
      _searchTafsirResults.value = emptyList()
      _uiState.value = _uiState.value.copy(isSearchingRemote = false)
      return
    }

    searchJob = launchSafe(Dispatchers.IO) {
      kotlinx.coroutines.delay(90)
      val traditionScope = _uiState.value.searchTraditionScope
      _uiState.value = _uiState.value.copy(isSearchingRemote = true)

      // STAGE 1: Instant Offline Search — display local results immediately while online search continues
      val ayahs = runCatching { repository.searchAyahsSmart(query) }.getOrDefault(emptyList())
      val translationMatches = runCatching { repository.searchTranslationsSmart(query) }.getOrDefault(emptyList())

      val extraAyahs = mutableListOf<AyahEntity>()
      translationMatches.filter { it.sourceType == "QURAN" }.take(30).forEach { trans ->
        val parts = trans.referenceKey.split(":")
        val sNum = parts.getOrNull(0)?.toIntOrNull()
        val vNum = parts.getOrNull(1)?.toIntOrNull()
        if (sNum != null && vNum != null) {
          runCatching { repository.getAyah(sNum, vNum) }.getOrNull()?.let { extraAyahs.add(it) }
        }
      }
      val localAyahs = (ayahs + extraAyahs).distinctBy { "${it.surahNumber}:${it.verseNumber}" }
      _searchAyahResults.value = localAyahs

      val hadiths = runCatching {
        repository.searchHadithsSmart(
          rawQuery = query,
          activeCollectionKey = _uiState.value.selectedCollectionKey,
          traditionScope = traditionScope
        )
      }.getOrDefault(emptyList())

      val extraHadiths = mutableListOf<HadithEntity>()
      translationMatches.filter { it.sourceType == "HADITH" }.take(30).forEach { trans ->
        val parts = trans.referenceKey.split(":")
        val colKey = parts.getOrNull(0)
        val hNum = parts.getOrNull(1)?.toIntOrNull()
        if (colKey != null && hNum != null) {
          runCatching { repository.getHadith(colKey, hNum) }.getOrNull()?.let { extraHadiths.add(it) }
        }
      }
      val localHadiths = (hadiths + extraHadiths).distinctBy { "${it.collectionKey}:${it.hadithNumber}" }
      _searchHadithResults.value = localHadiths

      val localTafsirs = runCatching {
        repository.searchTafsirSmart(
          query = query,
          traditionScope = traditionScope,
          preMatchedAyahs = localAyahs
        )
      }.getOrDefault(emptyList())
      _searchTafsirResults.value = localTafsirs

      // STAGE 2: Online Global Search — progressively fetch from online APIs & remote databases
      val mergedAyahs = runCatching {
        repository.searchAyahsOnlineStage2(
          rawQuery = query,
          traditionScope = traditionScope,
          localResults = localAyahs
        )
      }.getOrDefault(localAyahs)
      _searchAyahResults.value = mergedAyahs

      val mergedHadiths = runCatching {
        repository.searchHadithsOnlineStage2(
          rawQuery = query,
          traditionScope = traditionScope,
          localResults = localHadiths
        )
      }.getOrDefault(localHadiths)
      _searchHadithResults.value = mergedHadiths

      val mergedTafsirs = runCatching {
        repository.searchTafsirOnlineStage2(
          rawQuery = query,
          traditionScope = traditionScope,
          matchedAyahs = mergedAyahs,
          localTafsirs = localTafsirs
        )
      }.getOrDefault(localTafsirs)
      _searchTafsirResults.value = mergedTafsirs

      _uiState.value = _uiState.value.copy(isSearchingRemote = false)
    }
  }

  fun setSearchCategory(category: SearchCategory) {
    _uiState.value = _uiState.value.copy(searchCategory = category)
  }

  fun setSearchTraditionScope(scope: String) {
    val validScope = when (scope.uppercase()) {
      "SUNNI" -> "SUNNI"
      "SHIA" -> "SHIA"
      else -> "BOTH"
    }
    if (_uiState.value.searchTraditionScope == validScope) return
    _uiState.value = _uiState.value.copy(searchTraditionScope = validScope)
    val currentQuery = _uiState.value.searchQuery
    if (currentQuery.isNotBlank()) {
      onSearchQueryChanged(currentQuery)
    }
  }

  fun setInitialLibraryTab(tabIndex: Int) {
    _uiState.value = _uiState.value.copy(initialLibraryTab = tabIndex.coerceAtLeast(0))
  }

  fun setInitialLearnIslamTab(tab: com.example.ui.screens.LearnIslamTab) {
    _uiState.value = _uiState.value.copy(initialLearnIslamTab = tab)
  }

  // Bookmarks (Local + Cloud Firestore Sync)
  fun toggleBookmarkAyah(ayah: AyahEntity, surahName: String) {
    viewModelScope.launch {
      val key = "ayah_${ayah.surahNumber}_${ayah.verseNumber}"
      val exists = bookmarks.value.any { it.itemKey == key }
      val uid = currentUser.value?.uid
      if (exists) {
        repository.removeBookmark(key)
        if (uid != null && cloudRepository != null) {
          runCatching { cloudRepository.deleteBookmark(uid, key) }
        }
      } else {
        repository.toggleBookmarkAyah(ayah, surahName)
        if (uid != null && cloudRepository != null) {
          val cloudBookmark = CloudBookmark(
            itemType = "AYAH",
            referenceKey = "${ayah.surahNumber}:${ayah.verseNumber}",
            title = "$surahName ${ayah.surahNumber}:${ayah.verseNumber}",
            arabicSnippet = ayah.textArabic.take(1500),
            translationSnippet = ayah.translationEnglish.take(1500)
          )
          runCatching { cloudRepository.saveBookmark(uid, key, cloudBookmark) }
        }
      }
    }
  }

  fun toggleBookmarkHadith(hadith: HadithEntity) {
    viewModelScope.launch {
      val key = "hadith_${hadith.collectionKey}_${hadith.hadithNumber}"
      val exists = bookmarks.value.any { it.itemKey == key }
      val uid = currentUser.value?.uid
      if (exists) {
        repository.removeBookmark(key)
        if (uid != null && cloudRepository != null) {
          runCatching { cloudRepository.deleteBookmark(uid, key) }
        }
      } else {
        repository.toggleBookmarkHadith(hadith)
        if (uid != null && cloudRepository != null) {
          val cloudBookmark = CloudBookmark(
            itemType = "HADITH",
            referenceKey = "${hadith.collectionKey}_${hadith.hadithNumber}",
            title = "${hadith.collectionName} #${hadith.hadithNumber}",
            arabicSnippet = hadith.arabicText.take(1500),
            translationSnippet = hadith.englishText.take(1500)
          )
          runCatching { cloudRepository.saveBookmark(uid, key, cloudBookmark) }
        }
      }
    }
  }

  fun removeBookmarkByKey(key: String) {
    viewModelScope.launch {
      repository.removeBookmark(key)
      val uid = currentUser.value?.uid
      if (uid != null && cloudRepository != null) {
        runCatching { cloudRepository.deleteBookmark(uid, key) }
      }
    }
  }

  // Notes (Local + Cloud Firestore Sync)
  fun saveNote(refKey: String, title: String, content: String) {
    viewModelScope.launch {
      val cleanRef = refKey.ifBlank { "General" }.take(100)
      val cleanTitle = title.trim().take(200)
      val cleanContent = content.trim().take(5000)
      val insertedId = repository.saveNote(cleanRef, cleanTitle, cleanContent)

      val uid = currentUser.value?.uid
      if (uid != null && cloudRepository != null) {
        val noteId = "note_$insertedId"
        val itemType = if (cleanRef.contains("hadith", ignoreCase = true) ||
          cleanRef.contains("bukhari", ignoreCase = true) ||
          cleanRef.contains("muslim", ignoreCase = true) ||
          cleanRef.contains("nawawi", ignoreCase = true) ||
          cleanRef.contains("tirmidhi", ignoreCase = true) ||
          cleanRef.contains("dawud", ignoreCase = true)
        ) {
          "HADITH"
        } else {
          "AYAH"
        }
        val cloudNote = CloudNote(
          itemType = itemType,
          referenceKey = cleanRef,
          title = cleanTitle,
          content = cleanContent
        )
        runCatching { cloudRepository.saveNote(uid, noteId, cloudNote) }
      }
    }
  }

  fun deleteNote(id: Long) {
    viewModelScope.launch {
      repository.deleteNote(id)
      val uid = currentUser.value?.uid
      if (uid != null && cloudRepository != null) {
        runCatching { cloudRepository.deleteNote(uid, "note_$id") }
      }
    }
  }

  // Error reporting & Global Error Boundary recovery
  fun openErrorReportDialog(context: String = "General Content") {
    _uiState.value = _uiState.value.copy(
      showErrorReportDialog = true,
      errorReportContext = context
    )
  }

  fun closeErrorReportDialog() {
    _uiState.value = _uiState.value.copy(showErrorReportDialog = false)
  }

  fun clearRecoverableError() {
    if (_uiState.value.lastRecoverableError != null) {
      _uiState.value = _uiState.value.copy(lastRecoverableError = null)
    }
  }

  fun recoverSafeNavigationAndContentState() {
    _uiState.value = _uiState.value.copy(
      isReaderOpen = false,
      isReadingMode = false,
      selectedHadithDetail = null,
      previewedQuranVerse = null,
      isFetchingHadithByNumber = false,
      hadithLookupErrorMessage = null,
      isSearchingRemote = false,
      lastRecoverableError = null
    )
  }

  // Daily Masnoon Duas & Friday Bath / Necessary Sunnah Reminders Actions
  fun playDuaVocal(dua: com.example.data.model.DailyDuaItem, preferQuranReciter: Boolean = false) {
    duaAudioController.playDuaVocal(dua, preferQuranReciter)
  }

  fun stopDuaAudio() {
    duaAudioController.stop()
  }

  fun setDuaAudioLanguageMode(mode: com.example.ui.audio.DuaAudioLanguageMode) {
    duaAudioController.setLanguageMode(mode)
  }

  fun toggleDuaAudioRepeatMode() {
    duaAudioController.toggleRepeatTarget()
  }

  fun toggleFavoriteDua(duaId: String) {
    _sunnahRemindersState.value =
      com.example.data.prayer.SunnahReminderManager.toggleFavoriteDua(getApplication(), duaId)
  }

  fun incrementDuaTasbih(duaId: String, reset: Boolean = false) {
    _sunnahRemindersState.value =
      com.example.data.prayer.SunnahReminderManager.incrementDuaTasbih(getApplication(), duaId, reset)
  }

  fun updateFridayBathReminder(enabled: Boolean, time24h: String) {
    _sunnahRemindersState.value =
      com.example.data.prayer.SunnahReminderManager.setFridayBathReminder(getApplication(), enabled, time24h)
  }

  fun toggleFridayChecklistItem(itemId: String) {
    _sunnahRemindersState.value =
      com.example.data.prayer.SunnahReminderManager.toggleFridayChecklistItem(getApplication(), itemId)
  }

  fun incrementFridaySalawat(delta: Int) {
    _sunnahRemindersState.value =
      com.example.data.prayer.SunnahReminderManager.incrementFridaySalawat(getApplication(), delta)
  }

  fun updateNecessaryReminder(reminderId: String, enabled: Boolean, time24h: String) {
    _sunnahRemindersState.value =
      com.example.data.prayer.SunnahReminderManager.updateReminderConfig(
        getApplication(),
        reminderId,
        enabled,
        time24h
      )
  }

  fun triggerTestSunnahReminder(spec: com.example.data.model.NecessaryReminderSpec) {
    com.example.data.prayer.SunnahReminderManager.triggerTestReminder(getApplication(), spec)
    _sunnahRemindersState.value =
      com.example.data.prayer.SunnahReminderManager.loadState(getApplication())
  }

  fun selectPreviewDayOfWeek(calendarDayOfWeek: Int) {
    _sunnahRemindersState.value =
      com.example.data.prayer.SunnahReminderManager.selectPreviewDayOfWeek(
        getApplication(),
        calendarDayOfWeek
      )
  }

  fun sendCalculatedDayNotificationNow(dayOfWeekOverride: Int? = null) {
    _sunnahRemindersState.value =
      com.example.data.prayer.SunnahReminderManager.postCalculatedDayReminderNotification(
        context = getApplication(),
        dayOfWeekOverride = dayOfWeekOverride
      )
  }

  fun updateDailyCalculatedReminder(enabled: Boolean, time24h: String) {
    _sunnahRemindersState.value =
      com.example.data.prayer.SunnahReminderManager.setDailyCalculatedReminder(
        getApplication(),
        enabled,
        time24h
      )
  }

  fun clearAudioCache() {
    audioPlayerManager.clearAudioCache()
  }

  override fun onCleared() {
    super.onCleared()
    audioPlayerManager.stop()
    duaAudioController.shutdown()
  }
}
