package com.example.data.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import java.util.Locale

/**
 * Detects whether Bayan al-Huda was installed from the Google Play Store or via Direct APK distribution,
 * enforcing Google Play Developer Program policies.
 *
 * - Google Play installations route updates exclusively through the official Google Play Store mechanism
 *   and never download or install external GitHub APKs.
 * - Direct APK installations (GitHub Releases / Google Drive / sideloaded) use the verified GitHub
 *   Releases updater and Android's official PackageInstaller confirmation flow.
 */
object DistributionChannelDetector {

  private val GOOGLE_PLAY_INSTALLER_PACKAGES = setOf(
    "com.android.vending",
    "com.google.android.feedback"
  )

  @Volatile
  var overrideInstallerPackageForTesting: String? = null

  @Volatile
  var forceTestOverride: Boolean = false

  fun setInstallerPackageForTesting(installerPackage: String?, enabled: Boolean = true) {
    overrideInstallerPackageForTesting = installerPackage
    forceTestOverride = enabled
  }

  fun clearTestingOverride() {
    overrideInstallerPackageForTesting = null
    forceTestOverride = false
  }

  fun getInstallerPackageName(context: Context): String? {
    if (forceTestOverride) {
      return overrideInstallerPackageForTesting
    }
    return runCatching {
      val pm = context.packageManager
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val sourceInfo = pm.getInstallSourceInfo(context.packageName)
        sourceInfo.installingPackageName ?: sourceInfo.initiatingPackageName
      } else {
        @Suppress("DEPRECATION")
        pm.getInstallerPackageName(context.packageName)
      }
    }.getOrNull()
  }

  fun detectDistributionChannel(context: Context): DistributionChannel {
    val installer = getInstallerPackageName(context)?.trim()?.lowercase(Locale.ROOT)
    return if (installer != null && installer in GOOGLE_PLAY_INSTALLER_PACKAGES) {
      DistributionChannel.GOOGLE_PLAY_STORE
    } else {
      DistributionChannel.DIRECT_APK_GITHUB
    }
  }

  fun isInstalledFromGooglePlay(context: Context): Boolean {
    return detectDistributionChannel(context) == DistributionChannel.GOOGLE_PLAY_STORE
  }

  /**
   * Opens the official Google Play Store listing for Bayan al-Huda when the user is on the
   * Google Play distribution channel.
   */
  fun openGooglePlayStoreUpdateListing(context: Context, packageName: String = context.packageName): Boolean {
    val marketIntent = Intent(
      Intent.ACTION_VIEW,
      Uri.parse("market://details?id=$packageName")
    ).apply {
      setPackage("com.android.vending")
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return try {
      context.startActivity(marketIntent)
      true
    } catch (_: Exception) {
      try {
        val webIntent = Intent(
          Intent.ACTION_VIEW,
          Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        ).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(webIntent)
        true
      } catch (_: Exception) {
        false
      }
    }
  }
}
