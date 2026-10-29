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
  TAFSIR
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
  val isSearchingRemote: Boolean = false,
  // Daily insights
  val dailyAyah: AyahEntity? = null,
  val dailyHadith: HadithEntity? = null,
  val dailyReflection: ReflectionItem = ReflectionDataProvider.items.first(),
  // Reporting dialog
  val showErrorReportDialog: Boolean = false,
  val errorReportContext: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
class BayanViewModel(application: Application) : AndroidViewModel(application) {

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
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Reactive Firebase Auth User
  val currentUser: StateFlow<FirebaseUser?> = AuthManager.authStateFlow()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // All 114 Surahs Flow
  val allSurahs: StateFlow<List<SurahEntity>> = repository.getAllSurahs()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Hadith Collections Metadata Flow
  val hadithCollections: StateFlow<List<HadithCollectionEntity>> = repository.getAllHadithCollections()
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
    repository.getAllBookmarks(),
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
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Notes merged with Cloud Firestore
  val notes: StateFlow<List<NoteEntity>> = combine(
    repository.getAllNotes(),
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
    result
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
    viewModelScope.launch(Dispatchers.IO) {
      _isTimetableLoading.value = true
      val cfg = _uiState.value.prayerConfig
      val result = repository.getOrFetchSehriIftarTimetable(
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
      _sehriIftarTimetable.value = result
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
    viewModelScope.launch {
      val schedule = repository.getOrRefreshTodayPrayerSchedule(forceRemoteRefresh = forceRemote)
      _todayPrayerSchedule.value = schedule
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
    viewModelScope.launch {
      _auditReport.value = repository.generateDiagnosticAuditReport(checkNetworkLive = checkNetworkLive)
    }
  }

  fun signOut() {
    viewModelScope.launch {
      val app = getApplication<Application>()
      AuthManager.signOut(app)
      _uiState.value = _uiState.value.copy(
        isGuestModeActive = false,
        remainingGuestDays = AuthManager.getRemainingGuestDays(app)
      )
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
    surahAyahsJob = viewModelScope.launch {
      repository.ensureSurahAyahsLoaded(surah.number)
      val currentCfg = _uiState.value.prayerConfig
      repository.ensureSurahTranslationsLoaded(surah.number, currentCfg.sunniTranslationKey)
      if (currentCfg.shiaTranslationKey != currentCfg.sunniTranslationKey) {
        repository.ensureQulEditionLoadedForSurah(surah.number, currentCfg.shiaTranslationKey)
      }
      launch {
        repository.getAyahsBySurah(surah.number).collect { ayahs ->
          _currentAyahs.value = ayahs
          if (ayahs.isNotEmpty()) {
            audioPlayerManager.setSurahAyahWeights(
              surahNumber = surah.number,
              weights = ayahs.sortedBy { it.verseNumber }.map { it.textArabic.length.coerceAtLeast(12) }
            )
          }
        }
      }
      launch {
        // Load all Quran translations for this Surah e.g. en.qarai, ur.jalandhry, qul.*, etc.
        repository.getTranslationsForSurah(surah.number).collect { trans ->
          _currentTranslations.value = trans
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
    val sNum = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: return
    val vNum = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 1

    viewModelScope.launch {
      val ayah = repository.getAyah(sNum, vNum)
      val surah = repository.getSurahByNumber(sNum)
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
      viewModelScope.launch {
        repository.getSurahByNumber(targetNumber)?.let { openSurahReader(it) }
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
      viewModelScope.launch {
        val dbSurah = repository.getSurahByNumber(targetSurahNum)
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
    val updatedConfig = if (tradition == "SUNNI") {
      currentConfig.copy(sunniTranslationKey = key)
    } else {
      currentConfig.copy(shiaTranslationKey = key)
    }
    updatePrayerAndAlarmConfig(updatedConfig)
    val openSurahNum = _uiState.value.selectedSurah?.number
    if (openSurahNum != null) {
      viewModelScope.launch {
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
    val fallbackCatalog = com.example.data.local.ExtendedHadithDataProvider.getExtendedHadiths()
      .filter { it.collectionKey == collectionKey }
    if (fallbackCatalog.isNotEmpty()) {
      _collectionHadiths.value = fallbackCatalog.sortedBy { it.hadithNumber }
    }
    collectionHadithsJob = viewModelScope.launch(Dispatchers.IO) {
      val appCtx = getApplication<Application>()
      val bundledList = if (com.example.data.local.OfflineHadithAssetLoader.hasBundledAsset(collectionKey)) {
        com.example.data.local.OfflineHadithAssetLoader.loadCollection(appCtx, collectionKey)
      } else {
        emptyList()
      }
      if (bundledList.isNotEmpty()) {
        _collectionHadiths.value = bundledList
      }
      repository.getHadithsByCollection(collectionKey).collect { dbList ->
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

    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(
        selectedCollectionKey = collectionKey,
        isFetchingHadithByNumber = true,
        hadithLookupErrorMessage = null
      )
      val hadith = repository.findOrFetchHadithByNumber(collectionKey, hadithNumber)
      val verifiedHadith = hadith?.takeIf {
        it.collectionKey.equals(collectionKey, ignoreCase = true) && it.hadithNumber == hadithNumber
      }
      val range = com.example.data.local.OfflineHadithAssetLoader.getNumberRangeForCollection(
        getApplication(),
        collectionKey
      )
      _uiState.value = _uiState.value.copy(
        isFetchingHadithByNumber = false,
        selectedHadithDetail = verifiedHadith,
        hadithLookupErrorMessage = if (verifiedHadith == null) {
          "Hadith #$hadithNumber not found in $bookName (range is ${range.first}–${range.last})"
        } else {
          null
        }
      )
    }
  }

  fun loadHadithBookSection(bookNumber: Int) {
    val collectionKey = _uiState.value.selectedCollectionKey
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isFetchingHadithByNumber = true)
      repository.loadHadithBookSectionFromApi(collectionKey, bookNumber)
      _uiState.value = _uiState.value.copy(isFetchingHadithByNumber = false)
    }
  }

  fun showHadithDetail(hadith: HadithEntity) {
    _uiState.value = _uiState.value.copy(selectedHadithDetail = hadith)
  }

  fun closeHadithDetail() {
    _uiState.value = _uiState.value.copy(selectedHadithDetail = null)
  }

  // Unified Smart Search (Offline 6,236 Quran Verses + Local & Live Sunni/Shia Hadith & Sermon Lookup)
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

    searchJob = viewModelScope.launch(Dispatchers.IO) {
      kotlinx.coroutines.delay(120)
      _uiState.value = _uiState.value.copy(isSearchingRemote = true)

      val ayahs = repository.searchAyahsSmart(query)
      val translationMatches = repository.searchTranslationsSmart(query)

      // If translations matched but not ayahs, fetch the corresponding ayahs
      val extraAyahs = mutableListOf<AyahEntity>()
      translationMatches.filter { it.sourceType == "QURAN" }.take(30).forEach { trans ->
        val parts = trans.referenceKey.split(":")
        val sNum = parts.getOrNull(0)?.toIntOrNull()
        val vNum = parts.getOrNull(1)?.toIntOrNull()
        if (sNum != null && vNum != null) {
          repository.getAyah(sNum, vNum)?.let { extraAyahs.add(it) }
        }
      }

      _searchAyahResults.value = (ayahs + extraAyahs).distinctBy { "${it.surahNumber}:${it.verseNumber}" }

      val hadiths = repository.searchHadithsSmart(
        rawQuery = query,
        activeCollectionKey = _uiState.value.selectedCollectionKey
      )

      // Add Hadiths matched by translations
      val extraHadiths = mutableListOf<HadithEntity>()
      translationMatches.filter { it.sourceType == "HADITH" }.take(30).forEach { trans ->
        val parts = trans.referenceKey.split(":")
        val colKey = parts.getOrNull(0)
        val hNum = parts.getOrNull(1)?.toIntOrNull()
        if (colKey != null && hNum != null) {
          repository.getHadith(colKey, hNum)?.let { extraHadiths.add(it) }
        }
      }

      _searchHadithResults.value = (hadiths + extraHadiths).distinctBy { "${it.collectionKey}:${it.hadithNumber}" }

      // Real full-text search across Tafsir commentaries (Classical & Contemporary Sunni & Shia)
      val tafsirMatches = repository.searchTafsirSmart(query, traditionScope = "BOTH")
      _searchTafsirResults.value = tafsirMatches

      _uiState.value = _uiState.value.copy(isSearchingRemote = false)
    }
  }

  fun setSearchCategory(category: SearchCategory) {
    _uiState.value = _uiState.value.copy(searchCategory = category)
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
      repository.saveNote(cleanRef, cleanTitle, cleanContent)

      val uid = currentUser.value?.uid
      if (uid != null && cloudRepository != null) {
        val noteId = "note_${System.currentTimeMillis()}"
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

  // Error reporting
  fun openErrorReportDialog(context: String = "General Content") {
    _uiState.value = _uiState.value.copy(
      showErrorReportDialog = true,
      errorReportContext = context
    )
  }

  fun closeErrorReportDialog() {
    _uiState.value = _uiState.value.copy(showErrorReportDialog = false)
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
    duaAudioController.stop()
  }
}
