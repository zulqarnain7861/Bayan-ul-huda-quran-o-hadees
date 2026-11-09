package com.example.data.updater

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build

/**
 * Receives installation status callbacks from Android's official `PackageInstaller` and
 * `Intent.ACTION_MY_PACKAGE_REPLACED` events to verify newly installed updates and record history.
 */
class ApkInstallStatusReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    if (intent == null) return
    val action = intent.action ?: return

    if (action == Intent.ACTION_MY_PACKAGE_REPLACED) {
      val (upgraded, message) = AndroidPackageInstallerCoordinator.verifyPostInstallVersionUpgrade(context)
      if (upgraded && message != null) {
        AppUpdateManager.getInstance(context).onPackageReplacedVerified(message)
        ApkUpdateDownloadManager.postUpdaterNotification(
          context = context,
          title = "Bayan al-Huda Updated Successfully",
          message = message
        )
      }
      return
    }

    if (action == AppUpdaterConstants.INSTALL_STATUS_ACTION) {
      val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
      val statusMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE) ?: ""
      val targetVersionName = intent.getStringExtra("target_version_name") ?: ""
      val targetVersionCode = intent.getLongExtra("target_version_code", 0L)

      when (status) {
        PackageInstaller.STATUS_PENDING_USER_ACTION -> {
          val confirmIntent: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
          } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_INTENT)
          }
          if (confirmIntent != null) {
            confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(confirmIntent) }
          }
        }
        PackageInstaller.STATUS_SUCCESS -> {
          val msg = "Update to v$targetVersionName ($targetVersionCode) installed and verified."
          AppUpdateManager.getInstance(context).onInstallerStatusCallback(
            status = UpdaterStatus.INSTALLED_SUCCESS,
            targetVersionName = targetVersionName,
            targetVersionCode = targetVersionCode,
            message = msg
          )
          ApkUpdateDownloadManager.cleanupTemporaryAndOldApks(context)
        }
        PackageInstaller.STATUS_FAILURE_ABORTED -> {
          AppUpdateManager.getInstance(context).onInstallerStatusCallback(
            status = UpdaterStatus.INSTALL_CANCELLED,
            targetVersionName = targetVersionName,
            targetVersionCode = targetVersionCode,
            message = "Installation was cancelled by the user."
          )
        }
        else -> {
          val friendlyError = when (status) {
            PackageInstaller.STATUS_FAILURE_BLOCKED -> "Installation blocked by device policy or security settings."
            PackageInstaller.STATUS_FAILURE_CONFLICT -> "Installation conflict: existing package signing certificate mismatch."
            PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> "APK is incompatible with this Android device."
            PackageInstaller.STATUS_FAILURE_INVALID -> "Downloaded APK archive is invalid or corrupted."
            PackageInstaller.STATUS_FAILURE_STORAGE -> "Insufficient storage space to complete installation."
            else -> "Installation failed ($statusMessage)."
          }
          AppUpdateManager.getInstance(context).onInstallerStatusCallback(
            status = UpdaterStatus.ERROR,
            targetVersionName = targetVersionName,
            targetVersionCode = targetVersionCode,
            message = friendlyError
          )
        }
      }
    }
  }
}
