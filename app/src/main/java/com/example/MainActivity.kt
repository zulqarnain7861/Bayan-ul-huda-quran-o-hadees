package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import com.example.ui.components.AudioMiniPlayerBar
import com.example.ui.components.TopIslamicBar
import com.example.ui.localization.AppLocalization
import com.example.ui.localization.LocalAppStrings
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AddNoteDialog
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DailyDuasScreen
import com.example.ui.screens.DownloadCenterScreen
import com.example.ui.screens.HadithScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IslamWithEvidenceScreen
import com.example.ui.screens.LanguageOnboardingScreen
import com.example.ui.screens.LearnIslamScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.OpeningExperienceScreen
import com.example.ui.screens.QiblaCompassScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.ReportErrorDialog
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SehriIftarTimetableScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodels.BayanViewModel
import com.example.ui.viewmodels.MainNavigationTab
import androidx.compose.material.icons.filled.VolunteerActivism
import kotlinx.coroutines.launch

object BayanNavRoutes {
  const val HOME = "home"
  const val QURAN = "quran"
  const val HADITH = "hadith"
  const val DUAS = "duas"
  const val SEARCH = "search"
  const val LIBRARY = "library"
  const val ABOUT = "about"
  const val DOWNLOADS = "downloads"
  const val EVIDENCE = "evidence"
  const val QIBLA = "qibla"
  const val SEHRI_IFTAR = "sehri_iftar"
  const val LEARN_ISLAM = "learn_islam"
}

data class BottomNavDestination(
  val route: String,
  val tab: MainNavigationTab,
  val label: String,
  val icon: ImageVector,
  val testTag: String
)

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // STEP 1: Configure App Check with launch intent FIRST before intent sanitization, UI, ViewModels, or Firebase calls
    configureAppCheck(this, intent)
    installGlobalExceptionBoundary()
    com.example.security.TapjackingGuard.enableOverlayProtection(this)
    com.example.security.PathTraversalAndIntentGuard.sanitizeIncomingIntent(intent)
    com.example.security.RuntimeIntegrityGuard.performStartupCheck(this)
    enableEdgeToEdge()
    setContent {
      val bayanViewModel: BayanViewModel = viewModel()
      val uiState by bayanViewModel.uiState.collectAsStateWithLifecycle()
      MyApplicationTheme(themeMode = uiState.prayerConfig.themeMode) {
        BayanApp(viewModel = bayanViewModel)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    runCatching {
      com.example.data.updater.AppUpdateManager.getInstance(this).refreshEnvironmentAndVerifyPostInstall()
    }
  }

  private fun installGlobalExceptionBoundary() {
    val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
      android.util.Log.e(
        "GlobalErrorBoundary",
        "Uncaught exception on thread ${thread.name}: ${throwable.message}",
        throwable
      )
      // If a background worker thread throws a non-fatal IO/network/SQLite/JSON exception, absorb it safely
      val isMainThread = thread == android.os.Looper.getMainLooper()?.thread
      if (!isMainThread && throwable !is Error) {
        android.util.Log.w(
          "GlobalErrorBoundary",
          "Absorbed non-fatal background thread exception on ${thread.name} to prevent app crash."
        )
        return@setDefaultUncaughtExceptionHandler
      }
      previousHandler?.uncaughtException(thread, throwable)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BayanApp(
  viewModel: BayanViewModel = viewModel(),
  navController: NavHostController = rememberNavController()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val alarmPlaybackState by viewModel.alarmPlaybackState.collectAsStateWithLifecycle()
  val syncBadgeState by viewModel.downloadManager.syncBadgeState.collectAsStateWithLifecycle()
  val dbSurahs by viewModel.allSurahs.collectAsStateWithLifecycle()
  val surahs = remember(dbSurahs) {
    dbSurahs.ifEmpty { com.example.data.local.InitialDataLoader.getAll114Surahs() }
  }

  val appLanguageCode = uiState.prayerConfig.appInterfaceLanguageCode
  val appStrings = remember(appLanguageCode) {
    AppLocalization.getStrings(appLanguageCode)
  }
  val layoutDirection = remember(appLanguageCode) {
    AppLocalization.getLayoutDirection(appLanguageCode)
  }

  val appContext = androidx.compose.ui.platform.LocalContext.current
  val updaterManager = remember(appContext) { com.example.data.updater.AppUpdateManager.getInstance(appContext) }
  val updaterState by updaterManager.uiState.collectAsStateWithLifecycle()
  val updaterCoroutineScope = androidx.compose.runtime.rememberCoroutineScope()

  androidx.compose.runtime.LaunchedEffect(Unit) {
    updaterManager.checkForUpdatesOnStartOrResume(force = false)
  }

  var pendingNoteRef by remember { mutableStateOf<String?>(null) }

  val bottomDestinations = remember(appStrings) {
    listOf(
      BottomNavDestination(
        route = BayanNavRoutes.HOME,
        tab = MainNavigationTab.HOME,
        label = appStrings.navHome,
        icon = Icons.Default.Home,
        testTag = "nav_tab_home"
      ),
      BottomNavDestination(
        route = BayanNavRoutes.QURAN,
        tab = MainNavigationTab.QURAN,
        label = appStrings.navQuran,
        icon = Icons.Default.MenuBook,
        testTag = "nav_tab_quran"
      ),
      BottomNavDestination(
        route = BayanNavRoutes.HADITH,
        tab = MainNavigationTab.HADITH,
        label = appStrings.navHadith,
        icon = Icons.Default.AutoStories,
        testTag = "nav_tab_hadith"
      ),
      BottomNavDestination(
        route = BayanNavRoutes.DUAS,
        tab = MainNavigationTab.DUAS,
        label = appStrings.navDuas,
        icon = Icons.Default.VolunteerActivism,
        testTag = "nav_tab_duas"
      ),
      BottomNavDestination(
        route = BayanNavRoutes.SEARCH,
        tab = MainNavigationTab.SEARCH,
        label = appStrings.navSearch,
        icon = Icons.Default.Search,
        testTag = "nav_tab_search"
      ),
      BottomNavDestination(
        route = BayanNavRoutes.LIBRARY,
        tab = MainNavigationTab.LIBRARY,
        label = appStrings.navLibrary,
        icon = Icons.Default.Bookmarks,
        testTag = "nav_tab_library"
      )
    )
  }

  fun navigateToTab(route: String, tab: MainNavigationTab) {
    viewModel.selectTab(tab)
    runCatching {
      val currentRoute = navController.currentBackStackEntry?.destination?.route
      if (currentRoute == route) return@runCatching
      val startDestId = runCatching { navController.graph.findStartDestination().id }.getOrNull()
      navController.navigate(route) {
        if (startDestId != null) {
          popUpTo(startDestId) {
            saveState = true
          }
        }
        launchSingleTop = true
        restoreState = true
      }
    }.onFailure { e ->
      android.util.Log.w("BayanNavigation", "Safe navigation fallback for tab $route", e)
      runCatching {
        navController.navigate(route) { launchSingleTop = true }
      }
    }
  }

  fun safeNavigateToRoute(route: String) {
    runCatching {
      val currentRoute = navController.currentBackStackEntry?.destination?.route
      if (currentRoute != route) {
        navController.navigate(route) {
          launchSingleTop = true
        }
      }
    }.onFailure { e ->
      android.util.Log.w("BayanNavigation", "Safe route navigation error for $route", e)
    }
  }

  fun safePopBackStack() {
    runCatching {
      if (!navController.popBackStack()) {
        navigateToTab(BayanNavRoutes.HOME, MainNavigationTab.HOME)
      }
    }.onFailure { e ->
      android.util.Log.w("BayanNavigation", "Safe popBackStack fallback to HOME", e)
    }
  }

  CompositionLocalProvider(
    LocalAppStrings provides appStrings,
    LocalLayoutDirection provides layoutDirection
  ) {
    // Step 1: First-Launch Multilingual Onboarding Screen ("Choose Your Language")
    if (uiState.isOpeningScreenVisible) {
      LanguageOnboardingScreen(
        initialLanguageCode = uiState.prayerConfig.appInterfaceLanguageCode,
        onContinueWithLanguage = { selectedCode, syncContent ->
          viewModel.setAppInterfaceLanguage(
            languageCode = selectedCode,
            syncContentTranslation = syncContent
          )
        }
      )
      return@CompositionLocalProvider
    }

    // Step 2: Authentication or 7-Day Guest Access Mode
    if (currentUser == null && !uiState.isGuestModeActive) {
      AuthScreen(
        remainingGuestDays = uiState.remainingGuestDays,
        onContinueAsGuest = { viewModel.continueAsGuest() },
        onSignInSuccess = { user -> viewModel.onGoogleSignInSuccess(user) }
      )
      return@CompositionLocalProvider
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: BayanNavRoutes.HOME
    val isSubpageWithBack = currentRoute in setOf(
      BayanNavRoutes.ABOUT,
      BayanNavRoutes.DOWNLOADS,
      BayanNavRoutes.EVIDENCE,
      BayanNavRoutes.QIBLA,
      BayanNavRoutes.SEHRI_IFTAR,
      BayanNavRoutes.LEARN_ISLAM
    )

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        if (isSubpageWithBack && currentRoute != BayanNavRoutes.LEARN_ISLAM) {
          val headerTitle = when (currentRoute) {
            BayanNavRoutes.DOWNLOADS -> appStrings.offlineDownloadManager
            BayanNavRoutes.EVIDENCE -> appStrings.islamWithEvidence
            BayanNavRoutes.QIBLA -> appStrings.qiblaCompass
            BayanNavRoutes.SEHRI_IFTAR -> "Ramadan & Yearly Sehri / Iftar Timetable"
            else -> appStrings.settingsTitle
          }
          TopAppBar(
            title = {
              Text(
                text = headerTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            },
            navigationIcon = {
              IconButton(onClick = { safePopBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.surface
            )
          )
        } else if (!(currentRoute == BayanNavRoutes.QURAN && uiState.isReaderOpen) && currentRoute != BayanNavRoutes.LEARN_ISLAM) {
          TopIslamicBar(
            themeMode = uiState.prayerConfig.themeMode,
            isOfflineMode = syncBadgeState.isOfflineMode,
            onToggleOfflineMode = {
              viewModel.downloadManager.setOfflineModeEnabled(!syncBadgeState.isOfflineMode)
            },
            onOpenVideoLibraryClick = {
              viewModel.setInitialLibraryTab(5)
              navigateToTab(BayanNavRoutes.LIBRARY, MainNavigationTab.LIBRARY)
            },
            onOpenLibraryTabClick = { tabIdx ->
              viewModel.setInitialLibraryTab(tabIdx)
              navigateToTab(BayanNavRoutes.LIBRARY, MainNavigationTab.LIBRARY)
            },
            onOpenLearnIslamClick = {
              safeNavigateToRoute(BayanNavRoutes.LEARN_ISLAM)
            },
            onOpenSehriIftarClick = {
              viewModel.refreshSehriIftarTimetable(forceRemote = false)
              safeNavigateToRoute(BayanNavRoutes.SEHRI_IFTAR)
            },
            onOpenQiblaClick = {
              safeNavigateToRoute(BayanNavRoutes.QIBLA)
            },
            onOpenDownloadsClick = {
              safeNavigateToRoute(BayanNavRoutes.DOWNLOADS)
            },
            onOpenEvidenceClick = {
              safeNavigateToRoute(BayanNavRoutes.EVIDENCE)
            },
            onCycleThemeMode = { viewModel.cycleThemeMode() },
            onSearchClick = { navigateToTab(BayanNavRoutes.SEARCH, MainNavigationTab.SEARCH) },
            onAboutClick = {
              viewModel.refreshAuditReport(checkNetworkLive = true)
              safeNavigateToRoute(BayanNavRoutes.ABOUT)
            }
          )
        }
      },
      bottomBar = {
        Column {
          // In-App GitHub Update Available Banner (when a newer release is detected on startup/resume)
          com.example.ui.components.InAppUpdateAvailableBanner(
            updaterState = updaterState,
            onDownloadUpdate = {
              updaterCoroutineScope.launch {
                updaterManager.downloadAndVerifyLatestUpdate(resumeIfPartial = true)
              }
            },
            onInstallVerifiedUpdate = {
              updaterManager.promptInstallVerifiedUpdate()
            },
            onRemindMeLater = {
              updaterManager.remindMeLater()
            },
            onShowReleaseDetails = {
              updaterManager.setShowReleaseDetailsDialog(true)
            },
            onNavigateToUpdaterSettings = {
              safeNavigateToRoute(BayanNavRoutes.ABOUT)
            }
          )
          // Global Recoverable Error Boundary Banner (safe state recovery without crashing)
          uiState.lastRecoverableError?.let { errorMsg ->
            androidx.compose.material3.Surface(
              modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .testTag("global_error_boundary_banner"),
              shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
              color = androidx.compose.ui.graphics.Color(0xFF3E1F1F),
              tonalElevation = 4.dp
            ) {
              androidx.compose.foundation.layout.Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
              ) {
                Text(
                  text = errorMsg,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = androidx.compose.ui.graphics.Color(0xFFFFD6D6)
                  ),
                  modifier = Modifier.weight(1f),
                  maxLines = 2
                )
                androidx.compose.material3.TextButton(
                  onClick = {
                    viewModel.recoverSafeNavigationAndContentState()
                    navigateToTab(BayanNavRoutes.HOME, MainNavigationTab.HOME)
                  }
                ) {
                  Text("Reset View", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                androidx.compose.material3.TextButton(
                  onClick = { viewModel.clearRecoverableError() }
                ) {
                  Text("Dismiss", color = androidx.compose.ui.graphics.Color.White, fontSize = 11.sp)
                }
              }
            }
          }
          // Global Azan / Fajr Alarm / Continuous Vibration Popup Control when user is on tabs other than Home
          if ((alarmPlaybackState.isActive || alarmPlaybackState.isContinuousVibrationActive) && currentRoute != BayanNavRoutes.HOME) {
            androidx.compose.material3.Surface(
              modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .testTag("global_azan_active_popup"),
              shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
              color = androidx.compose.ui.graphics.Color(0xFF101915),
              tonalElevation = 6.dp
            ) {
              androidx.compose.foundation.layout.Row(
                modifier = Modifier
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = alarmPlaybackState.prayerOrAlarmTitle.ifBlank { "200% Boosted Azan & Continuous Vibration" },
                    style = MaterialTheme.typography.labelLarge.copy(
                      fontWeight = FontWeight.Bold,
                      color = GoldPrimary
                    )
                  )
                  Text(
                    text = alarmPlaybackState.currentStageLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = androidx.compose.ui.graphics.Color(0xFFD8CCB2)
                    )
                  )
                }
                if (alarmPlaybackState.isActive) {
                  androidx.compose.material3.TextButton(
                    onClick = { viewModel.snoozeActiveAzanOrAlarm(5) }
                  ) {
                    Text("Snooze 5m", color = GoldPrimary, fontSize = 11.sp)
                  }
                }
                androidx.compose.material3.Button(
                  onClick = { viewModel.stopActiveAzanOrAlarm() },
                  colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary
                  ),
                  modifier = Modifier.testTag("global_azan_stop_button")
                ) {
                  Text("Stop", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }

          // Persistent Audio Reciter Mini Player (scoped so 60ms progress ticks only recompose the mini-player)
          val miniPlayerAudioState by viewModel.audioState.collectAsStateWithLifecycle()
          AudioMiniPlayerBar(
            audioState = miniPlayerAudioState,
            onTogglePlayPause = { viewModel.audioPlayerManager.togglePlayPause() },
            onStop = { viewModel.audioPlayerManager.stop() },
            onSelectReciter = { reciter -> viewModel.setQuranReciter(reciter, reciter.tradition) },
            onChangePlaybackSpeed = { speed -> viewModel.audioPlayerManager.setPlaybackSpeed(speed) },
            onToggleAudioRefinement = { enabled -> viewModel.audioPlayerManager.setAudioAssistedRefinement(enabled) }
          )

          // Main Bottom Navigation Bar controlled by NavHostController
          if (!(currentRoute == BayanNavRoutes.QURAN && uiState.isReaderOpen) && !isSubpageWithBack) {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surface,
              tonalElevation = 8.dp
            ) {
              bottomDestinations.forEach { dest ->
                val isSelected = currentRoute == dest.route
                NavigationBarItem(
                  selected = isSelected,
                  onClick = { navigateToTab(dest.route, dest.tab) },
                  icon = { Icon(dest.icon, contentDescription = dest.label) },
                  label = { Text(dest.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = EmeraldPrimary,
                    indicatorColor = GoldPrimary.copy(alpha = 0.25f)
                  ),
                  modifier = Modifier.testTag(dest.testTag)
                )
              }
            }
          }
        }
      }
    ) { paddingValues ->
      NavHost(
        navController = navController,
        startDestination = BayanNavRoutes.HOME,
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
      ) {
        composable(BayanNavRoutes.HOME) {
          val todayPrayerSchedule by viewModel.todayPrayerSchedule.collectAsStateWithLifecycle()
          val sunnahRemindersState by viewModel.sunnahRemindersState.collectAsStateWithLifecycle()
          HomeScreen(
            dailyAyah = uiState.dailyAyah,
            dailyHadith = uiState.dailyHadith,
            surahs = surahs,
            prayerSchedule = todayPrayerSchedule,
            prayerConfig = uiState.prayerConfig,
            alarmPlaybackState = alarmPlaybackState,
            sunnahRemindersState = sunnahRemindersState,
            onUpdatePrayerConfig = { viewModel.updatePrayerAndAlarmConfig(it) },
            onRefreshPrayerSchedule = { viewModel.refreshPrayerSchedule(forceRemote = true) },
            onTestAzanOrFajrAlarm = { prayer, isFajr -> viewModel.testAzanOrFajrAlarm(prayer, isFajr) },
            onStopActiveAlarm = { viewModel.stopActiveAzanOrAlarm() },
            onSnoozeActiveAlarm = { mins -> viewModel.snoozeActiveAzanOrAlarm(mins) },
            onSelectPreviewDayOfWeek = { dow -> viewModel.selectPreviewDayOfWeek(dow) },
            onSendCalculatedDayNotificationNow = { dowOverride ->
              viewModel.sendCalculatedDayNotificationNow(dowOverride)
            },
            onUpdateFridayBathReminder = { enabled, time ->
              viewModel.updateFridayBathReminder(enabled, time)
            },
            onUpdateDailyCalculatedReminder = { enabled, time ->
              viewModel.updateDailyCalculatedReminder(enabled, time)
            },
            onToggleFridayChecklistItem = { itemId ->
              viewModel.toggleFridayChecklistItem(itemId)
            },
            onIncrementFridaySalawat = { delta ->
              viewModel.incrementFridaySalawat(delta)
            },
            onOpenSurah = { surah ->
              viewModel.openSurahReader(surah)
              navigateToTab(BayanNavRoutes.QURAN, MainNavigationTab.QURAN)
            },
            onResumeLastRead = {
              viewModel.resumeLastReadQuran()
              navigateToTab(BayanNavRoutes.QURAN, MainNavigationTab.QURAN)
            },
            onPlayRecitation = { ayah, sName -> viewModel.playAyahRecitation(ayah, sName) },
            onBookmarkAyah = { ayah, sName -> viewModel.toggleBookmarkAyah(ayah, sName) },
            onBookmarkHadith = { hadith -> viewModel.toggleBookmarkHadith(hadith) },
            onNavigateToQuran = { navigateToTab(BayanNavRoutes.QURAN, MainNavigationTab.QURAN) },
            onNavigateToHadith = { navigateToTab(BayanNavRoutes.HADITH, MainNavigationTab.HADITH) },
            onNavigateToLibrary = { navigateToTab(BayanNavRoutes.LIBRARY, MainNavigationTab.LIBRARY) },
            onNavigateToLibraryTab = { tabIdx ->
              viewModel.setInitialLibraryTab(tabIdx)
              navigateToTab(BayanNavRoutes.LIBRARY, MainNavigationTab.LIBRARY)
            },
            onNavigateToDownloads = {
              safeNavigateToRoute(BayanNavRoutes.DOWNLOADS)
            },
            onNavigateToEvidence = {
              safeNavigateToRoute(BayanNavRoutes.EVIDENCE)
            },
            onNavigateToQibla = {
              safeNavigateToRoute(BayanNavRoutes.QIBLA)
            },
            onNavigateToDailyDuas = {
              navigateToTab(BayanNavRoutes.DUAS, MainNavigationTab.DUAS)
            },
            onNavigateToSehriIftar = {
              viewModel.refreshSehriIftarTimetable(forceRemote = false)
              safeNavigateToRoute(BayanNavRoutes.SEHRI_IFTAR)
            },
            onNavigateToLearnIslam = {
              safeNavigateToRoute(BayanNavRoutes.LEARN_ISLAM)
            }
          )
        }

        composable(BayanNavRoutes.QURAN) {
          val currentAyahs by viewModel.currentAyahs.collectAsStateWithLifecycle()
          val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
          val audioState by viewModel.audioState.collectAsStateWithLifecycle()
          val currentTranslations by viewModel.currentTranslations.collectAsStateWithLifecycle()
          QuranScreen(
            surahs = surahs,
            isReaderOpen = uiState.isReaderOpen,
            selectedSurah = uiState.selectedSurah,
            currentAyahs = currentAyahs,
            bookmarks = bookmarks,
            showEnglish = uiState.showEnglish,
            showUrdu = uiState.showUrdu,
            showTransliteration = uiState.showTransliteration,
            arabicFontSizeSp = uiState.arabicFontSizeSp,
            translationFontSizeSp = uiState.translationFontSizeSp,
            onOpenSurah = { viewModel.openSurahReader(it) },
            onCloseReader = { viewModel.closeSurahReader() },
            onNavigateAdjacentSurah = { offset -> viewModel.navigateToAdjacentSurah(offset) },
            onToggleEnglish = { viewModel.toggleEnglishTranslation() },
            onToggleUrdu = { viewModel.toggleUrduTranslation() },
            onToggleTransliteration = { viewModel.toggleTransliteration() },
            onSetArabicFontSize = { viewModel.setArabicFontSize(it) },
            onPlayAyahRecitation = { ayah, sName -> viewModel.playAyahRecitation(ayah, sName) },
            onBookmarkAyah = { ayah, sName -> viewModel.toggleBookmarkAyah(ayah, sName) },
            onAddNote = { ref -> pendingNoteRef = ref },
            onOpenOfflineDownloads = {
              safeNavigateToRoute(BayanNavRoutes.DOWNLOADS)
            },
            allHadithsForTafsir = viewModel.allHadithsForLinks,
            isReadingMode = uiState.isReadingMode,
            onToggleReadingMode = { viewModel.toggleReadingMode() },
            isContinuousMushafFlow = uiState.isContinuousMushafFlow,
            onToggleContinuousMushafFlow = { viewModel.toggleContinuousMushafFlow() },
            prayerConfig = uiState.prayerConfig,
            audioState = audioState,
            currentTranslations = currentTranslations,
            onSelectReciter = { reciter, tradition -> viewModel.setQuranReciter(reciter, tradition) },
            onSelectTranslation = { key, tradition -> viewModel.setQuranTranslationKey(key, tradition) },
            onToggleTranslationKey = { key, tradition -> viewModel.toggleSelectedTranslationKey(key, tradition) },
            highlightedVerseNumber = uiState.highlightedVerseNumber,
            onSaveReadingProgress = { surah, verseNum, tradition ->
              viewModel.saveQuranReadingProgress(surah, verseNum, tradition)
            },
            onResumeLastRead = { surahNum, verseNum, _ ->
              viewModel.resumeLastReadQuran(surahNum, verseNum)
            },
            onReportAyah = { ref ->
              viewModel.openErrorReportDialog(ref)
            }
          )
        }

        composable(BayanNavRoutes.HADITH) {
          val hadiths by viewModel.collectionHadiths.collectAsStateWithLifecycle()
          val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
          HadithScreen(
            selectedCollectionKey = uiState.selectedCollectionKey,
            hadiths = hadiths,
            bookmarks = bookmarks,
            selectedEditionFilterMode = uiState.selectedEditionFilterMode,
            isHadithLookupLoading = uiState.isFetchingHadithByNumber,
            hadithLookupErrorMessage = uiState.hadithLookupErrorMessage,
            selectedHadithDetail = uiState.selectedHadithDetail,
            previewedQuranVerse = uiState.previewedQuranVerse,
            onSelectCollection = { viewModel.selectHadithCollection(it) },
            onSelectEditionFilterMode = { viewModel.selectHadithEditionFilterMode(it) },
            onLookupHadithByNumber = { colKey, num -> viewModel.lookupHadithByNumber(num, colKey) },
            onClearHadithLookupError = { viewModel.clearHadithLookupError() },
            onLoadBookSection = { bookNum -> viewModel.loadHadithBookSection(bookNum) },
            onShowHadithDetail = { viewModel.showHadithDetail(it) },
            onCloseHadithDetail = { viewModel.closeHadithDetail() },
            onClickQuranVerseRef = { verseRef, openFullReader ->
              viewModel.openOrPreviewQuranReference(verseRef, openFullReader)
              if (openFullReader) {
                navigateToTab(BayanNavRoutes.QURAN, MainNavigationTab.QURAN)
              }
            },
            onDismissQuranVersePreview = { viewModel.dismissPreviewedQuranVerse() },
            onBookmarkHadith = { viewModel.toggleBookmarkHadith(it) },
            onAddNote = { ref -> pendingNoteRef = ref },
            onReportHadith = { hadith ->
              viewModel.openErrorReportDialog("${hadith.collectionName} #${hadith.hadithNumber} (${hadith.reference})")
            },
            onOpenOfflineDownloads = {
              safeNavigateToRoute(BayanNavRoutes.DOWNLOADS)
            },
            onOpenEvidenceVerification = {
              safeNavigateToRoute(BayanNavRoutes.EVIDENCE)
            }
          )
        }

        composable(BayanNavRoutes.DUAS) {
          val sunnahRemindersState by viewModel.sunnahRemindersState.collectAsStateWithLifecycle()
          val duaAudioState by viewModel.duaAudioState.collectAsStateWithLifecycle()
          DailyDuasScreen(
            remindersState = sunnahRemindersState,
            duaAudioState = duaAudioState,
            showEnglish = uiState.showEnglish,
            showUrdu = uiState.showUrdu,
            showTransliteration = uiState.showTransliteration,
            onToggleEnglish = { viewModel.toggleEnglishTranslation() },
            onToggleUrdu = { viewModel.toggleUrduTranslation() },
            onToggleTransliteration = { viewModel.toggleTransliteration() },
            onPlayDuaVocal = { dua, preferReciter -> viewModel.playDuaVocal(dua, preferReciter) },
            onStopDuaAudio = { viewModel.stopDuaAudio() },
            onSelectAudioLanguageMode = { viewModel.setDuaAudioLanguageMode(it) },
            onToggleAudioRepeatMode = { viewModel.toggleDuaAudioRepeatMode() },
            onToggleFavoriteDua = { viewModel.toggleFavoriteDua(it) },
            onIncrementDuaTasbih = { duaId, reset -> viewModel.incrementDuaTasbih(duaId, reset) },
            onUpdateFridayBathReminder = { enabled, time -> viewModel.updateFridayBathReminder(enabled, time) },
            onToggleFridayChecklistItem = { viewModel.toggleFridayChecklistItem(it) },
            onIncrementFridaySalawat = { viewModel.incrementFridaySalawat(it) },
            onUpdateNecessaryReminder = { id, enabled, time -> viewModel.updateNecessaryReminder(id, enabled, time) },
            onTriggerTestReminder = { viewModel.triggerTestSunnahReminder(it) },
            onOpenSurahByNumber = { surahNum ->
              val targetSurah = surahs.find { it.number == surahNum }
              if (targetSurah != null) {
                viewModel.openSurahReader(targetSurah)
                navigateToTab(BayanNavRoutes.QURAN, MainNavigationTab.QURAN)
              }
            },
            onNavigateToLearnIslam = {
              safeNavigateToRoute(BayanNavRoutes.LEARN_ISLAM)
            }
          )
        }

        composable(BayanNavRoutes.SEARCH) {
          val searchAyahs by viewModel.searchAyahResults.collectAsStateWithLifecycle()
          val searchHadiths by viewModel.searchHadithResults.collectAsStateWithLifecycle()
          val searchTafsirs by viewModel.searchTafsirResults.collectAsStateWithLifecycle()
          SearchScreen(
            searchQuery = uiState.searchQuery,
            searchCategory = uiState.searchCategory,
            ayahResults = searchAyahs,
            hadithResults = searchHadiths,
            tafsirResults = searchTafsirs,
            surahs = surahs,
            isSearchingRemote = uiState.isSearchingRemote,
            selectedTraditionScope = uiState.searchTraditionScope,
            onTraditionScopeChanged = { viewModel.setSearchTraditionScope(it) },
            onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
            onSearchCategoryChanged = { viewModel.setSearchCategory(it) },
            onOpenSurah = { surah ->
              viewModel.openSurahReader(surah)
              navigateToTab(BayanNavRoutes.QURAN, MainNavigationTab.QURAN)
            },
            onOpenSurahWithVerse = { surah, verseNum ->
              viewModel.openSurahReader(surah, highlightVerse = verseNum)
              navigateToTab(BayanNavRoutes.QURAN, MainNavigationTab.QURAN)
            },
            onOpenHadith = { hadith ->
              viewModel.selectHadithCollection(hadith.collectionKey)
              viewModel.showHadithDetail(hadith)
              navigateToTab(BayanNavRoutes.HADITH, MainNavigationTab.HADITH)
            },
            onOpenVideoLibrary = {
              viewModel.setInitialLibraryTab(5)
              navigateToTab(BayanNavRoutes.LIBRARY, MainNavigationTab.LIBRARY)
            },
            onOpenLearnIslam = { tab ->
              viewModel.setInitialLearnIslamTab(tab)
              safeNavigateToRoute(BayanNavRoutes.LEARN_ISLAM)
            },
            onOpenLibraryBooks = {
              viewModel.setInitialLibraryTab(1)
              navigateToTab(BayanNavRoutes.LIBRARY, MainNavigationTab.LIBRARY)
            }
          )
        }

        composable(BayanNavRoutes.LIBRARY) {
          val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
          val notes by viewModel.notes.collectAsStateWithLifecycle()
          LibraryScreen(
            bookmarks = bookmarks,
            notes = notes,
            onRemoveBookmark = { viewModel.removeBookmarkByKey(it) },
            onSaveNote = { ref, title, content -> viewModel.saveNote(ref, title, content) },
            onDeleteNote = { viewModel.deleteNote(it) },
            downloadManager = viewModel.downloadManager,
            currentThemeMode = uiState.prayerConfig.themeMode,
            onSelectThemeMode = { viewModel.setThemeMode(it) },
            onPlayDownloadedAudio = { s, v, name -> viewModel.audioPlayerManager.playAyah(s, v, name) },
            initialSelectedTab = uiState.initialLibraryTab,
            selectionNonce = uiState.libraryTabSelectionNonce
          )
        }

        composable(BayanNavRoutes.DOWNLOADS) {
          DownloadCenterScreen(
            downloadManager = viewModel.downloadManager,
            currentThemeMode = uiState.prayerConfig.themeMode,
            onSelectThemeMode = { viewModel.setThemeMode(it) },
            onPlayDownloadedAudio = { s, v, name -> viewModel.audioPlayerManager.playAyah(s, v, name) }
          )
        }

        composable(BayanNavRoutes.EVIDENCE) {
          IslamWithEvidenceScreen(
            evidenceEngine = viewModel.evidenceEngine,
            onOpenQuranReference = { surahNum, verseNum ->
              viewModel.openOrPreviewQuranReference("$surahNum:$verseNum", openFullSurahReader = true)
              navigateToTab(BayanNavRoutes.QURAN, MainNavigationTab.QURAN)
            },
            onOpenHadithReference = { colKey, hadithNum ->
              viewModel.selectHadithCollection(colKey)
              viewModel.lookupHadithByNumber(hadithNum, colKey)
              navigateToTab(BayanNavRoutes.HADITH, MainNavigationTab.HADITH)
            }
          )
        }

        composable(BayanNavRoutes.QIBLA) {
          QiblaCompassScreen(
            prayerConfig = uiState.prayerConfig,
            onUpdatePrayerConfig = { viewModel.updatePrayerAndAlarmConfig(it) }
          )
        }

        composable(BayanNavRoutes.SEHRI_IFTAR) {
          val sehriIftarTimetable by viewModel.sehriIftarTimetable.collectAsStateWithLifecycle()
          val isTimetableLoading by viewModel.isTimetableLoading.collectAsStateWithLifecycle()
          val timetableScopeMode by viewModel.timetableScopeMode.collectAsStateWithLifecycle()
          val selectedTimetableGregorianMonth by viewModel.selectedTimetableGregorianMonth.collectAsStateWithLifecycle()
          SehriIftarTimetableScreen(
            prayerConfig = uiState.prayerConfig,
            timetableResult = sehriIftarTimetable,
            isLoading = isTimetableLoading,
            selectedScopeMode = timetableScopeMode,
            selectedGregorianYear = uiState.prayerConfig.selectedTimetableGregorianYear,
            selectedHijriYear = uiState.prayerConfig.selectedTimetableHijriYear,
            selectedGregorianMonth = selectedTimetableGregorianMonth,
            onSelectScopeMode = { viewModel.selectTimetableScopeMode(it) },
            onSelectGregorianYear = { viewModel.selectTimetableGregorianYear(it) },
            onSelectHijriYear = { viewModel.selectTimetableHijriYear(it) },
            onSelectGregorianMonth = { viewModel.selectTimetableGregorianMonth(it) },
            onUpdatePrayerConfig = { viewModel.updatePrayerAndAlarmConfig(it) },
            onRefreshTimetable = { forceRemote -> viewModel.refreshSehriIftarTimetable(forceRemote) },
            onApplyTrueMapLocationAndCacheMonth = { lat, lon, city, country, tz ->
              viewModel.applyTrueMapLocationAndCacheMonth(lat, lon, city, country, tz)
            }
          )
        }

        composable(BayanNavRoutes.LEARN_ISLAM) {
          LearnIslamScreen(
            duaAudioController = viewModel.duaAudioController,
            initialTab = uiState.initialLearnIslamTab,
            onBack = { safePopBackStack() },
            onOpenQuranSurah = { surahNum, verseNum ->
              viewModel.openOrPreviewQuranReference("$surahNum:$verseNum", openFullSurahReader = true)
              navigateToTab(BayanNavRoutes.QURAN, MainNavigationTab.QURAN)
            }
          )
        }

        composable(BayanNavRoutes.ABOUT) {
          val activityContext = androidx.compose.ui.platform.LocalContext.current
          val audioState by viewModel.audioState.collectAsStateWithLifecycle()
          val auditReport by viewModel.auditReport.collectAsStateWithLifecycle()
          val sourceRegistryList by viewModel.sourceRegistryList.collectAsStateWithLifecycle()
          AboutScreen(
            currentReciter = audioState.reciter,
            themeMode = uiState.prayerConfig.themeMode,
            auditReport = auditReport,
            sourceRegistryList = sourceRegistryList,
            onSelectThemeMode = { viewModel.setThemeMode(it) },
            onRefreshAuditReport = { viewModel.refreshAuditReport(checkNetworkLive = true) },
            onSelectReciter = { viewModel.setReciter(it) },
            onOpenErrorReport = { viewModel.openErrorReportDialog("General App Experience") },
            userEmail = currentUser?.email,
            userName = currentUser?.displayName,
            isGuestMode = uiState.isGuestModeActive,
            remainingGuestDays = uiState.remainingGuestDays,
            onSignOut = {
              safePopBackStack()
              viewModel.signOut()
            },
            onSignInSuccess = { user -> viewModel.onGoogleSignInSuccess(user) },
            onDeleteAccount = { deleteLocalToo, onResult ->
              viewModel.deleteAccountAndData(
                activityContext = activityContext,
                deleteLocalBookmarksAndNotes = deleteLocalToo,
                onResult = { success, msg ->
                  onResult(success, msg)
                  if (success) {
                    safePopBackStack()
                  }
                }
              )
            },
            onClearAudioCache = { viewModel.clearAudioCache() },
            currentAppLanguageCode = uiState.prayerConfig.appInterfaceLanguageCode,
            onSelectAppLanguage = { langCode, syncContent ->
              viewModel.setAppInterfaceLanguage(langCode, syncContent)
            }
          )
        }
      }
    }
  }

  // Report Error Dialog
  if (uiState.showErrorReportDialog) {
    ReportErrorDialog(
      initialContext = uiState.errorReportContext,
      onDismiss = { viewModel.closeErrorReportDialog() }
    )
  }

  // Add Note Dialog (triggered from Ayah or Hadith item)
  pendingNoteRef?.let { ref ->
    AddNoteDialog(
      initialReference = ref,
      onDismiss = { pendingNoteRef = null },
      onSave = { noteRef, title, content ->
        viewModel.saveNote(noteRef, title, content)
        pendingNoteRef = null
      }
    )
  }
}
