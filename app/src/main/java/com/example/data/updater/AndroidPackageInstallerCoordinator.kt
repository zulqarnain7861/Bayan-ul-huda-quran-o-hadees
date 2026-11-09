package com.example.data.updater

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream

/**
 * Official Android PackageInstaller Coordinator for Bayan al-Huda (Requirement 6).
 *
 * Security & Compliance Guarantees:
 * - Refuses to install GitHub APKs when the app was installed from Google Play Store (directs to Play Store instead).
 * - Checks `PackageManager.canRequestPackageInstalls()` before attempting installation.
 * - Provides clear user guidance and opens Android's official `ACTION_MANAGE_UNKNOWN_APP_SOURCES` Settings screen when permission is needed.
 * - Uses Android's official `PackageInstaller.Session` + `FileProvider` `ACTION_VIEW` confirmation dialog so the user is ALWAYS in full control to Approve or Cancel.
 * - Never uses root (`su`), silent installation, Accessibility Services, or Play Protect bypasses.
 */
object AndroidPackageInstallerCoordinator {

  sealed class InstallLaunchResult {
    data class ConfirmationLaunched(val sessionId: Int?, val message: String) : InstallLaunchResult()
    data class PermissionRequired(val settingsIntent: Intent, val explanation: String) : InstallLaunchResult()
    data class RedirectedToGooglePlay(val message: String) : InstallLaunchResult()
    data class RejectedBySecurity(val reason: String) : InstallLaunchResult()
  }

  @Volatile
  var overrideCanRequestPackageInstallsForTesting: Boolean? = null

  @Volatile
  var interceptInstallLaunchForTesting: ((File, GitHubReleaseInfo) -> InstallLaunchResult)? = null

  fun clearTestingOverrides() {
    overrideCanRequestPackageInstallsForTesting = null
    interceptInstallLaunchForTesting = null
  }

  /**
   * Returns true if the application currently has user permission to request APK installations.
   */
  fun canRequestPackageInstalls(context: Context): Boolean {
    overrideCanRequestPackageInstallsForTesting?.let { return it }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      runCatching { context.packageManager.canRequestPackageInstalls() }.getOrDefault(false)
    } else {
      true
    }
  }

  /**
   * Builds the official Android Settings intent (`ACTION_MANAGE_UNKNOWN_APP_SOURCES`) for granting
   * "Install unknown apps" permission to Bayan al-Huda.
   */
  fun buildUnknownAppSourcesSettingsIntent(context: Context): Intent {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Intent(
        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
        Uri.parse("package:${context.packageName}")
      ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    } else {
      Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    }
  }

  fun openUnknownAppSourcesSettings(context: Context): Boolean {
    return try {
      context.startActivity(buildUnknownAppSourcesSettingsIntent(context))
      true
    } catch (_: Exception) {
      try {
        context.startActivity(
          Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
        )
        true
      } catch (_: Exception) {
        false
      }
    }
  }

  /**
   * Initiates user-consented APK installation ONLY after:
   * 1. Verifying distribution channel is Direct APK (not Google Play Store).
   * 2. Re-verifying the APK's SHA-256 checksum, package name, version code, and X.509 signing certificate.
   * 3. Checking `canRequestPackageInstalls(context)`.
   */
  fun requestUserApprovedApkInstallation(
    context: Context,
    apkFile: File,
    release: GitHubReleaseInfo
  ): InstallLaunchResult {
    // 1. Enforce Google Play distribution policy
    if (DistributionChannelDetector.isInstalledFromGooglePlay(context)) {
      DistributionChannelDetector.openGooglePlayStoreUpdateListing(context)
      return InstallLaunchResult.RedirectedToGooglePlay(
        "Bayan al-Huda was installed from Google Play. Directing to Google Play Store for official updates."
      )
    }

    // 2. Re-verify APK integrity & signing certificate immediately prior to installation (anti-TOCTOU)
    val verification = ApkSecurityVerifier.verifyDownloadedApk(
      context = context,
      apkFile = apkFile,
      signedManifest = release.signedManifest,
      expectedVersionCode = release.versionCode
    )
    if (!verification.isValid) {
      runCatching { apkFile.delete() }
      return InstallLaunchResult.RejectedBySecurity(
        verification.errorMessage ?: "APK security verification failed prior to installation."
      )
    }

    // 3. Check Android "Install unknown apps" permission
    if (!canRequestPackageInstalls(context)) {
      val settingsIntent = buildUnknownAppSourcesSettingsIntent(context)
      return InstallLaunchResult.PermissionRequired(
        settingsIntent = settingsIntent,
        explanation = "To install this verified update, please allow 'Install unknown apps' for Bayan al-Huda in Android Settings, then return and tap 'Install Update'."
      )
    }

    interceptInstallLaunchForTesting?.let { interceptor ->
      return interceptor(apkFile, release)
    }

    // Record pending target version so we can verify successful installation on next startup/MY_PACKAGE_REPLACED
    recordPendingInstallTarget(context, release.versionName, release.versionCode)

    // 4. Launch standard Android PackageInstaller confirmation dialog via FileProvider URI
    return try {
      val apkUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        apkFile
      )
      val installIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(apkUri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.packageName)
      }
      context.startActivity(installIntent)
      InstallLaunchResult.ConfirmationLaunched(
        sessionId = null,
        message = "Opened Android Package Installer confirmation screen. Approve to complete update to v${release.versionName}."
      )
    } catch (_: Exception) {
      // Fallback to PackageInstaller.Session API
      launchPackageInstallerSession(context, apkFile, release)
    }
  }

  /**
   * Streams the verified APK into an official `android.content.pm.PackageInstaller.Session` and
   * commits it with `ApkInstallStatusReceiver`, triggering the system confirmation prompt.
   */
  fun launchPackageInstallerSession(
    context: Context,
    apkFile: File,
    release: GitHubReleaseInfo
  ): InstallLaunchResult {
    return try {
      val packageInstaller = context.packageManager.packageInstaller
      val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
        setAppPackageName(context.packageName)
        if (apkFile.length() > 0L) {
          setSize(apkFile.length())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_REQUIRED)
        }
      }
      val sessionId = packageInstaller.createSession(params)
      packageInstaller.openSession(sessionId).use { session ->
        FileInputStream(apkFile).use { input ->
          session.openWrite("Bayan-al-Huda-${release.versionCode}.apk", 0, apkFile.length()).use { output ->
            input.copyTo(output, bufferSize = 64 * 1024)
            session.fsync(output)
          }
        }
        val callbackIntent = Intent(context, ApkInstallStatusReceiver::class.java).apply {
          action = AppUpdaterConstants.INSTALL_STATUS_ACTION
          setPackage(context.packageName)
          putExtra("target_version_name", release.versionName)
          putExtra("target_version_code", release.versionCode)
        }
        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
          PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(
          context,
          sessionId,
          callbackIntent,
          pendingFlags
        )
        session.commit(pendingIntent.intentSender)
      }
      InstallLaunchResult.ConfirmationLaunched(
        sessionId = sessionId,
        message = "Android Package Installer session #${sessionId} ready. Please confirm installation on screen."
      )
    } catch (e: Exception) {
      InstallLaunchResult.RejectedBySecurity(
        "Could not open Android Package Installer: ${e.message ?: "Unknown installer error"}"
      )
    }
  }

  fun recordPendingInstallTarget(context: Context, targetVersionName: String, targetVersionCode: Long) {
    val prefs = context.getSharedPreferences(AppUpdaterConstants.UPDATER_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit()
      .putString("pending_install_version_name", targetVersionName)
      .putLong("pending_install_version_code", targetVersionCode)
      .putLong("pending_install_timestamp_ms", System.currentTimeMillis())
      .apply()
  }

  /**
   * Verifies on startup or `MY_PACKAGE_REPLACED` whether a previously initiated update was
   * successfully installed, cleans up temporary APK files, and returns a confirmation message if verified.
   */
  fun verifyPostInstallVersionUpgrade(context: Context): Pair<Boolean, String?> {
    val prefs = context.getSharedPreferences(AppUpdaterConstants.UPDATER_PREFS_NAME, Context.MODE_PRIVATE)
    val pendingCode = prefs.getLong("pending_install_version_code", -1L)
    val pendingName = prefs.getString("pending_install_version_name", null)
    if (pendingCode <= 0L || pendingName.isNullOrBlank()) {
      return false to null
    }

    val installed = ApkSecurityVerifier.getInstalledAppIdentity(context)
    return if (installed.versionCode >= pendingCode) {
      prefs.edit()
        .remove("pending_install_version_code")
        .remove("pending_install_version_name")
        .remove("pending_install_timestamp_ms")
        .putString("last_verified_installed_version", "${installed.versionName} (${installed.versionCode})")
        .apply()
      ApkUpdateDownloadManager.cleanupTemporaryAndOldApks(context)
      true to "Successfully updated Bayan al-Huda to v${installed.versionName} (Version Code ${installed.versionCode})."
    } else {
      false to null
    }
  }
}
