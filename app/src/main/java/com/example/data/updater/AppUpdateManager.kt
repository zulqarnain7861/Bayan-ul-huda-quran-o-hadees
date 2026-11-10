package com.example.data.updater

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import java.io.File

/**
 * Central state coordinator for the Bayan al-Huda Secure GitHub APK Installer & In-App Updater.
 */
class AppUpdateManager(private val appContext: Context) {

  private val prefs = appContext.getSharedPreferences(
    AppUpdaterConstants.UPDATER_PREFS_NAME,
    Context.MODE_PRIVATE
  )

  private val _uiState = MutableStateFlow(createInitialState())
  val uiState: StateFlow<UpdaterUiState> = _uiState.asStateFlow()

  init {
    if (ApkSecurityVerifier.customInstalledIdentityForTesting == null) {
      runCatching {
        val dir = ApkUpdateDownloadManager.getUpdatesDirectory(appContext)
        dir.listFiles()?.forEach { f ->
          if (f.isFile && f.length() < 50_000L) {
            f.delete()
          }
        }
      }
    }
    refreshEnvironmentAndVerifyPostInstall()
  }

  private fun createInitialState(): UpdaterUiState {
    val installed = ApkSecurityVerifier.getInstalledAppIdentity(appContext)
    val channel = DistributionChannelDetector.detectDistributionChannel(appContext)
    val lastCheckMs = prefs.getLong("last_checked_timestamp_ms", 0L)
    val remindUntilMs = prefs.getLong("remind_me_later_until_ms", 0L)
    val history = UpdateHistoryEntry.parseHistoryList(prefs.getString("update_history_json", null))
    val canInstall = AndroidPackageInstallerCoordinator.canRequestPackageInstalls(appContext)

    return UpdaterUiState(
      installedVersionName = installed.versionName,
      installedVersionCode = installed.versionCode,
      currentBuildConfigVersionCode = installed.versionCode.toInt(),
      distributionChannel = channel,
      status = UpdaterStatus.IDLE,
      lastCheckedTimestampMs = lastCheckMs,
      remindMeLaterUntilMs = remindUntilMs,
      canRequestPackageInstalls = canInstall,
      updateHistory = history
    )
  }

  fun refreshEnvironmentAndVerifyPostInstall() {
    val installed = ApkSecurityVerifier.getInstalledAppIdentity(appContext)
    val channel = DistributionChannelDetector.detectDistributionChannel(appContext)
    val canInstall = AndroidPackageInstallerCoordinator.canRequestPackageInstalls(appContext)
    val (postUpgradeVerified, upgradeMessage) = AndroidPackageInstallerCoordinator.verifyPostInstallVersionUpgrade(appContext)

    _uiState.update { current ->
      current.copy(
        installedVersionName = installed.versionName,
        installedVersionCode = installed.versionCode,
        currentBuildConfigVersionCode = installed.versionCode.toInt(),
        distributionChannel = channel,
        canRequestPackageInstalls = canInstall,
        status = if (postUpgradeVerified) UpdaterStatus.INSTALLED_SUCCESS else current.status,
        errorMessage = if (postUpgradeVerified) null else current.errorMessage
      )
    }

    if (postUpgradeVerified && upgradeMessage != null) {
      recordHistoryEntry(
        targetVersionName = installed.versionName,
        targetVersionCode = installed.versionCode,
        status = "INSTALL_SUCCEEDED",
        details = upgradeMessage
      )
    }
  }

  /**
   * Automatic startup/resume update check (Requirement 3).
   * Checks GitHub Releases if `force == true` or if at least `checkIntervalMs` has elapsed since the last check.
   */
  suspend fun checkForUpdatesOnStartOrResume(
    force: Boolean = false,
    nowMs: Long = System.currentTimeMillis(),
    checkIntervalMs: Long = AppUpdaterConstants.AUTO_CHECK_INTERVAL_MS
  ): UpdaterUiState {
    refreshEnvironmentAndVerifyPostInstall()
    val stateSnapshot = _uiState.value
    if (!force && stateSnapshot.lastCheckedTimestampMs > 0L &&
      (nowMs - stateSnapshot.lastCheckedTimestampMs) in 0 until checkIntervalMs
    ) {
      return _uiState.value
    }
    return checkForUpdatesNow(isAutomaticCheck = !force, nowMs = nowMs)
  }

  /**
   * Queries the official GitHub Releases API and compares the latest stable version against
   * the installed application version.
   */
  suspend fun checkForUpdatesNow(
    isAutomaticCheck: Boolean = false,
    nowMs: Long = System.currentTimeMillis()
  ): UpdaterUiState {
    if (GitHubReleaseClient.customTransportForTesting == null) {
      ApkSecurityVerifier.clearTestingOverrides()
      ApkUpdateDownloadManager.customStreamDownloaderForTesting = null
    }
    val installed = ApkSecurityVerifier.getInstalledAppIdentity(appContext)
    val channel = DistributionChannelDetector.detectDistributionChannel(appContext)
    val canInstall = AndroidPackageInstallerCoordinator.canRequestPackageInstalls(appContext)

    _uiState.update {
      it.copy(
        installedVersionName = installed.versionName,
        installedVersionCode = installed.versionCode,
        currentBuildConfigVersionCode = installed.versionCode.toInt(),
        distributionChannel = channel,
        canRequestPackageInstalls = canInstall,
        status = UpdaterStatus.CHECKING,
        errorMessage = null
      )
    }

    val outcome = GitHubReleaseClient.fetchLatestStableRelease(includePrereleases = false)
    prefs.edit().putLong("last_checked_timestamp_ms", nowMs).apply()

    when (outcome) {
      is GitHubReleaseClient.ReleaseCheckOutcome.Success -> {
        val release = outcome.release
        val effectiveInstalledCode = installed.versionCode
        val urgency = release.computeUrgency(effectiveInstalledCode)
        val hasNewerVersion = InAppUpdateService.isNewerVersionCode(
          remoteVersionCode = release.versionCode,
          currentVersionCode = effectiveInstalledCode
        )
        val isSnoozed = isAutomaticCheck &&
          urgency != UpdateUrgency.CRITICAL &&
          nowMs < _uiState.value.remindMeLaterUntilMs

        if (hasNewerVersion) {
          recordHistoryEntry(
            targetVersionName = release.versionName,
            targetVersionCode = release.versionCode,
            status = "UPDATE_AVAILABLE",
            details = "New release v${release.versionName} (Code ${release.versionCode} > Installed Code $effectiveInstalledCode) detected [${urgency.badgeLabel}]."
          )
          if (!isSnoozed) {
            ApkUpdateDownloadManager.postUpdaterNotification(
              context = appContext,
              title = "BAYAN AL-HUDA UPDATE AVAILABLE",
              message = "Version ${release.versionName} (Code ${release.versionCode}) is available (Installed: ${installed.versionName} / Code $effectiveInstalledCode)."
            )
          }
          _uiState.update {
            it.copy(
              installedVersionCode = effectiveInstalledCode,
              currentBuildConfigVersionCode = effectiveInstalledCode.toInt(),
              status = UpdaterStatus.UPDATE_AVAILABLE,
              urgency = urgency,
              latestRelease = release,
              lastCheckedTimestampMs = nowMs,
              showUpdateNotificationBanner = !isSnoozed,
              showUpdateDialog = !isSnoozed,
              isFreshTestSimulation = false,
              errorMessage = null
            )
          }
        } else {
          recordHistoryEntry(
            targetVersionName = release.versionName,
            targetVersionCode = release.versionCode,
            status = "CHECKED_UP_TO_DATE",
            details = "Installed v${installed.versionName} (Code $effectiveInstalledCode) is up to date."
          )
          _uiState.update {
            it.copy(
              installedVersionCode = effectiveInstalledCode,
              currentBuildConfigVersionCode = effectiveInstalledCode.toInt(),
              status = UpdaterStatus.UP_TO_DATE,
              urgency = UpdateUrgency.NONE,
              latestRelease = release,
              lastCheckedTimestampMs = nowMs,
              showUpdateNotificationBanner = false,
              showUpdateDialog = false,
              isFreshTestSimulation = false,
              errorMessage = null
            )
          }
        }
      }

      is GitHubReleaseClient.ReleaseCheckOutcome.NoPublishedRelease -> {
        recordHistoryEntry(
          targetVersionName = installed.versionName,
          targetVersionCode = installed.versionCode,
          status = "CHECKED_UP_TO_DATE",
          details = outcome.message
        )
        _uiState.update {
          it.copy(
            status = UpdaterStatus.UP_TO_DATE,
            urgency = UpdateUrgency.NONE,
            latestRelease = null,
            lastCheckedTimestampMs = nowMs,
            showUpdateNotificationBanner = false,
            showUpdateDialog = false,
            isFreshTestSimulation = false,
            errorMessage = null
          )
        }
      }

      is GitHubReleaseClient.ReleaseCheckOutcome.RateLimited -> {
        recordHistoryEntry(
          targetVersionName = installed.versionName,
          targetVersionCode = installed.versionCode,
          status = "RATE_LIMITED",
          details = outcome.message
        )
        _uiState.update {
          it.copy(
            status = if (isAutomaticCheck) UpdaterStatus.IDLE else UpdaterStatus.RATE_LIMITED,
            lastCheckedTimestampMs = nowMs,
            errorMessage = if (isAutomaticCheck) null else outcome.message
          )
        }
      }

      is GitHubReleaseClient.ReleaseCheckOutcome.NetworkError -> {
        recordHistoryEntry(
          targetVersionName = installed.versionName,
          targetVersionCode = installed.versionCode,
          status = "NETWORK_ERROR",
          details = outcome.message
        )
        _uiState.update {
          it.copy(
            status = if (isAutomaticCheck) UpdaterStatus.IDLE else UpdaterStatus.ERROR,
            lastCheckedTimestampMs = nowMs,
            errorMessage = if (isAutomaticCheck) null else outcome.message
          )
        }
      }
    }

    return _uiState.value
  }

  /**
   * Snoozes automatic update notification popup for 24 hours ("Remind Me Later").
   */
  fun remindMeLater(nowMs: Long = System.currentTimeMillis()) {
    val untilMs = nowMs + AppUpdaterConstants.REMIND_ME_LATER_SNOOZE_MS
    prefs.edit().putLong("remind_me_later_until_ms", untilMs).apply()
    _uiState.update {
      it.copy(
        remindMeLaterUntilMs = untilMs,
        showUpdateNotificationBanner = false,
        showUpdateDialog = false
      )
    }
  }

  fun setShowUpdateDialog(visible: Boolean) {
    _uiState.update { it.copy(showUpdateDialog = visible) }
  }

  fun setShowReleaseDetailsDialog(visible: Boolean) {
    _uiState.update { it.copy(showReleaseDetailsDialog = visible) }
  }

  fun dismissUpdateBanner() {
    _uiState.update { it.copy(showUpdateNotificationBanner = false) }
  }

  /**
   * Stages a fresh update release (`versionCode > BuildConfig.VERSION_CODE`) and immediately
   * triggers the In-App Update Dialog so the user can test the complete update dialog, download,
   * 7-step SHA-256 & X.509 certificate verification, and Android PackageInstaller flow.
   */
  fun stageFreshUpdateReleaseForTesting(
    release: GitHubReleaseInfo,
    nowMs: Long = System.currentTimeMillis()
  ): UpdaterUiState {
    val installed = ApkSecurityVerifier.getInstalledAppIdentity(appContext)
    val effectiveInstalledCode = installed.versionCode
    val urgency = release.computeUrgency(effectiveInstalledCode)
    recordHistoryEntry(
      targetVersionName = release.versionName,
      targetVersionCode = release.versionCode,
      status = "FRESH_UPDATE_TEST_STAGED",
      details = "Staged fresh update v${release.versionName} (Code ${release.versionCode} > Installed Code $effectiveInstalledCode) for verification."
    )
    ApkUpdateDownloadManager.postUpdaterNotification(
      context = appContext,
      title = "BAYAN AL-HUDA UPDATE AVAILABLE (v${release.versionName})",
      message = "Update v${release.versionName} (Code ${release.versionCode}) is ready to download and verify."
    )
    _uiState.update {
      it.copy(
        installedVersionName = installed.versionName,
        installedVersionCode = effectiveInstalledCode,
        currentBuildConfigVersionCode = effectiveInstalledCode.toInt(),
        status = UpdaterStatus.UPDATE_AVAILABLE,
        urgency = urgency,
        latestRelease = release,
        lastCheckedTimestampMs = nowMs,
        showUpdateNotificationBanner = true,
        showUpdateDialog = true,
        isFreshTestSimulation = true,
        verifiedApkFilePath = null,
        verificationResult = null,
        errorMessage = null
      )
    }
    return _uiState.value
  }

  /**
   * Downloads the latest release APK into application-private storage and immediately runs
   * the 7-step cryptographic & certificate security verification pipeline.
   *
   * If the app was installed via Google Play Store, refuses direct APK download and opens Play Store instead.
   */
  suspend fun downloadAndVerifyLatestUpdate(resumeIfPartial: Boolean = true): UpdaterUiState {
    val current = _uiState.value
    val release = current.latestRelease ?: return current

    // Enforce Google Play distribution compliance
    if (DistributionChannelDetector.isInstalledFromGooglePlay(appContext)) {
      DistributionChannelDetector.openGooglePlayStoreUpdateListing(appContext)
      recordHistoryEntry(
        targetVersionName = release.versionName,
        targetVersionCode = release.versionCode,
        status = "PLAY_STORE_REDIRECT",
        details = "Google Play installation detected — redirected to Google Play Store."
      )
      _uiState.update {
        it.copy(
          distributionChannel = DistributionChannel.GOOGLE_PLAY_STORE,
          errorMessage = "Installed via Google Play Store. Please update through Google Play."
        )
      }
      return _uiState.value
    }

    _uiState.update {
      it.copy(
        status = UpdaterStatus.DOWNLOADING,
        downloadProgressPercent = 0,
        downloadedBytes = 0L,
        totalBytes = release.apkAsset.sizeBytes,
        errorMessage = null,
        verificationResult = null
      )
    }

    val downloadOutcome = ApkUpdateDownloadManager.downloadReleaseApk(
      context = appContext,
      release = release,
      resumeIfPartialExists = resumeIfPartial
    ) { pct, downloaded, total ->
      _uiState.update { state ->
        state.copy(
          status = UpdaterStatus.DOWNLOADING,
          downloadProgressPercent = pct,
          downloadedBytes = downloaded,
          totalBytes = total
        )
      }
    }

    when (downloadOutcome) {
      is ApkUpdateDownloadManager.DownloadOutcome.Cancelled -> {
        recordHistoryEntry(
          targetVersionName = release.versionName,
          targetVersionCode = release.versionCode,
          status = "DOWNLOAD_CANCELLED",
          details = "Download cancelled at ${ApkUpdateDownloadManager.formatFileSize(downloadOutcome.partialBytes)}."
        )
        _uiState.update {
          it.copy(
            status = UpdaterStatus.DOWNLOAD_CANCELLED,
            errorMessage = "Download paused/cancelled. Tap Retry to resume."
          )
        }
      }

      is ApkUpdateDownloadManager.DownloadOutcome.Failure -> {
        recordHistoryEntry(
          targetVersionName = release.versionName,
          targetVersionCode = release.versionCode,
          status = "DOWNLOAD_FAILED",
          details = downloadOutcome.errorMessage
        )
        _uiState.update {
          it.copy(
            status = UpdaterStatus.ERROR,
            errorMessage = downloadOutcome.errorMessage
          )
        }
      }

      is ApkUpdateDownloadManager.DownloadOutcome.Success -> {
        val apkFile = downloadOutcome.apkFile
        _uiState.update {
          it.copy(
            status = UpdaterStatus.VERIFYING,
            downloadProgressPercent = 100,
            downloadedBytes = downloadOutcome.totalBytes,
            totalBytes = downloadOutcome.totalBytes
          )
        }

        val verification = ApkSecurityVerifier.verifyDownloadedApk(
          context = appContext,
          apkFile = apkFile,
          signedManifest = release.signedManifest,
          expectedVersionCode = release.versionCode
        )

        if (!verification.isValid) {
          // Immediately delete any rejected/corrupted/tampered APK
          runCatching { apkFile.delete() }
          val reasonMsg = verification.errorMessage ?: "APK security verification failed."
          recordHistoryEntry(
            targetVersionName = release.versionName,
            targetVersionCode = release.versionCode,
            status = "VERIFICATION_FAILED",
            details = reasonMsg
          )
          _uiState.update {
            it.copy(
              status = UpdaterStatus.ERROR,
              verifiedApkFilePath = null,
              verificationResult = verification,
              errorMessage = reasonMsg
            )
          }
        } else {
          // Clean up any older APKs while preserving the verified one
          ApkUpdateDownloadManager.cleanupTemporaryAndOldApks(
            context = appContext,
            keepApkFileName = apkFile.name
          )
          val canInstall = AndroidPackageInstallerCoordinator.canRequestPackageInstalls(appContext)
          recordHistoryEntry(
            targetVersionName = release.versionName,
            targetVersionCode = release.versionCode,
            status = "DOWNLOADED_AND_VERIFIED",
            details = "Verified SHA-256 (${verification.actualSha256?.take(12)}...) & X.509 signing certificate."
          )
          ApkUpdateDownloadManager.postUpdaterNotification(
            context = appContext,
            title = "Bayan al-Huda v${release.versionName} Verified & Ready",
            message = "SHA-256 and signing certificate verified. Tap to install through Android Package Installer."
          )
          _uiState.update {
            it.copy(
              status = if (canInstall) UpdaterStatus.READY_TO_INSTALL else UpdaterStatus.AWAITING_INSTALL_PERMISSION,
              verifiedApkFilePath = apkFile.absolutePath,
              verificationResult = verification,
              canRequestPackageInstalls = canInstall,
              errorMessage = null
            )
          }
        }
      }
    }

    return _uiState.value
  }

  fun cancelActiveDownload() {
    ApkUpdateDownloadManager.requestCancelCurrentDownload()
  }

  /**
   * Requests user-approved installation of the verified APK via Android's official PackageInstaller.
   */
  fun promptInstallVerifiedUpdate(): AndroidPackageInstallerCoordinator.InstallLaunchResult {
    val state = _uiState.value
    val release = state.latestRelease
      ?: return AndroidPackageInstallerCoordinator.InstallLaunchResult.RejectedBySecurity("No verified release selected.")
    val apkPath = state.verifiedApkFilePath
      ?: return AndroidPackageInstallerCoordinator.InstallLaunchResult.RejectedBySecurity("No verified APK file available to install.")
    val apkFile = File(apkPath)

    val launchResult = AndroidPackageInstallerCoordinator.requestUserApprovedApkInstallation(
      context = appContext,
      apkFile = apkFile,
      release = release
    )

    when (launchResult) {
      is AndroidPackageInstallerCoordinator.InstallLaunchResult.ConfirmationLaunched -> {
        recordHistoryEntry(
          targetVersionName = release.versionName,
          targetVersionCode = release.versionCode,
          status = "INSTALL_PROMPTED",
          details = launchResult.message
        )
        _uiState.update {
          it.copy(
            status = UpdaterStatus.INSTALLING,
            canRequestPackageInstalls = true,
            errorMessage = null
          )
        }
      }
      is AndroidPackageInstallerCoordinator.InstallLaunchResult.PermissionRequired -> {
        recordHistoryEntry(
          targetVersionName = release.versionName,
          targetVersionCode = release.versionCode,
          status = "PERMISSION_REQUIRED",
          details = launchResult.explanation
        )
        _uiState.update {
          it.copy(
            status = UpdaterStatus.AWAITING_INSTALL_PERMISSION,
            canRequestPackageInstalls = false,
            errorMessage = launchResult.explanation
          )
        }
      }
      is AndroidPackageInstallerCoordinator.InstallLaunchResult.RedirectedToGooglePlay -> {
        recordHistoryEntry(
          targetVersionName = release.versionName,
          targetVersionCode = release.versionCode,
          status = "PLAY_STORE_REDIRECT",
          details = launchResult.message
        )
        _uiState.update {
          it.copy(
            distributionChannel = DistributionChannel.GOOGLE_PLAY_STORE,
            errorMessage = launchResult.message
          )
        }
      }
      is AndroidPackageInstallerCoordinator.InstallLaunchResult.RejectedBySecurity -> {
        recordHistoryEntry(
          targetVersionName = release.versionName,
          targetVersionCode = release.versionCode,
          status = "VERIFICATION_FAILED",
          details = launchResult.reason
        )
        _uiState.update {
          it.copy(
            status = UpdaterStatus.ERROR,
            verifiedApkFilePath = null,
            errorMessage = launchResult.reason
          )
        }
      }
    }

    return launchResult
  }

  fun onInstallerStatusCallback(
    status: UpdaterStatus,
    targetVersionName: String,
    targetVersionCode: Long,
    message: String
  ) {
    val historyStatus = when (status) {
      UpdaterStatus.INSTALLED_SUCCESS -> "INSTALL_SUCCEEDED"
      UpdaterStatus.INSTALL_CANCELLED -> "INSTALL_CANCELLED"
      else -> "INSTALL_FAILED"
    }
    recordHistoryEntry(
      targetVersionName = targetVersionName,
      targetVersionCode = targetVersionCode,
      status = historyStatus,
      details = message
    )
    _uiState.update {
      it.copy(
        status = status,
        errorMessage = if (status == UpdaterStatus.ERROR || status == UpdaterStatus.INSTALL_CANCELLED) message else null
      )
    }
  }

  fun onPackageReplacedVerified(message: String) {
    val installed = ApkSecurityVerifier.getInstalledAppIdentity(appContext)
    recordHistoryEntry(
      targetVersionName = installed.versionName,
      targetVersionCode = installed.versionCode,
      status = "INSTALL_SUCCEEDED",
      details = message
    )
    _uiState.update {
      it.copy(
        installedVersionName = installed.versionName,
        installedVersionCode = installed.versionCode,
        status = UpdaterStatus.INSTALLED_SUCCESS,
        urgency = UpdateUrgency.NONE,
        showUpdateNotificationBanner = false,
        errorMessage = null
      )
    }
  }

  private fun recordHistoryEntry(
    targetVersionName: String,
    targetVersionCode: Long,
    status: String,
    details: String
  ) {
    val installed = ApkSecurityVerifier.getInstalledAppIdentity(appContext)
    val entry = UpdateHistoryEntry(
      timestampMs = System.currentTimeMillis(),
      installedVersionName = installed.versionName,
      installedVersionCode = installed.versionCode,
      targetVersionName = targetVersionName,
      targetVersionCode = targetVersionCode,
      status = status,
      details = details
    )
    val updatedList = (listOf(entry) + _uiState.value.updateHistory).take(25)
    val jsonArr = JSONArray()
    updatedList.forEach { jsonArr.put(it.toJson()) }
    prefs.edit().putString("update_history_json", jsonArr.toString()).apply()
    _uiState.update { it.copy(updateHistory = updatedList) }
  }

  companion object {
    @Volatile
    private var INSTANCE: AppUpdateManager? = null

    fun getInstance(context: Context): AppUpdateManager {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: AppUpdateManager(context.applicationContext).also { INSTANCE = it }
      }
    }

    fun resetInstanceForTesting() {
      INSTANCE = null
    }
  }
}
