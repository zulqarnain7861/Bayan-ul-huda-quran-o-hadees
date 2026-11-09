package com.example.data.updater

import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/**
 * Core domain models and constants for the Bayan al-Huda Secure GitHub APK Installer & In-App Updater.
 */
object AppUpdaterConstants {
  const val GITHUB_OWNER = "zulqarnain7861"
  const val GITHUB_REPO = "Bayan-ul-huda-quran-o-hadees"
  const val GITHUB_LATEST_RELEASE_API_URL =
    "https://api.github.com/repos/zulqarnain7861/Bayan-ul-huda-quran-o-hadees/releases/latest"
  const val GITHUB_RELEASES_LIST_API_URL =
    "https://api.github.com/repos/zulqarnain7861/Bayan-ul-huda-quran-o-hadees/releases"
  const val GITHUB_RELEASES_WEB_URL =
    "https://github.com/zulqarnain7861/Bayan-ul-huda-quran-o-hadees/releases"

  const val EXPECTED_APPLICATION_ID = "com.bayanulhuda.com"
  const val INSTALL_STATUS_ACTION = "com.bayanulhuda.com.ACTION_APK_INSTALL_STATUS"
  const val UPDATER_PREFS_NAME = "bayan_github_updater_prefs"

  // Automatic startup/resume check interval (6 hours default) and Remind Me Later snooze (24 hours)
  const val AUTO_CHECK_INTERVAL_MS = 6L * 60L * 60L * 1000L
  const val REMIND_ME_LATER_SNOOZE_MS = 24L * 60L * 60L * 1000L

  val TRUSTED_GITHUB_HOSTS = setOf(
    "api.github.com",
    "github.com",
    "objects.githubusercontent.com",
    "release-assets.githubusercontent.com",
    "codeload.github.com",
    "raw.githubusercontent.com"
  )
}

enum class DistributionChannel(val label: String, val allowsDirectGitHubApkInstall: Boolean) {
  DIRECT_APK_GITHUB(
    label = "Direct APK Distribution (GitHub Releases)",
    allowsDirectGitHubApkInstall = true
  ),
  GOOGLE_PLAY_STORE(
    label = "Google Play Store Installation",
    allowsDirectGitHubApkInstall = false
  )
}

enum class UpdateUrgency(val badgeLabel: String) {
  NONE("Up to Date"),
  OPTIONAL("Optional Update Available"),
  CRITICAL("Critical Update Required")
}

enum class UpdaterStatus(val displayLabel: String) {
  IDLE("Ready to check for updates"),
  CHECKING("Checking GitHub Releases..."),
  UP_TO_DATE("You are running the latest version"),
  UPDATE_AVAILABLE("New update available"),
  DOWNLOADING("Downloading verified APK from GitHub Releases..."),
  DOWNLOAD_CANCELLED("Download cancelled"),
  VERIFYING("Verifying SHA-256 checksum & X.509 signing certificate..."),
  READY_TO_INSTALL("Verified & ready to install"),
  AWAITING_INSTALL_PERMISSION("Permission required to install unknown apps"),
  INSTALLING("Opening Android Package Installer confirmation..."),
  INSTALLED_SUCCESS("Update installed and verified"),
  INSTALL_CANCELLED("Installation cancelled by user"),
  RATE_LIMITED("GitHub API rate limit reached — please try again shortly"),
  NO_PUBLISHED_RELEASE("No published GitHub release yet"),
  ERROR("Update check or verification failed")
}

enum class ApkVerificationFailureReason(val userFriendlyReason: String) {
  FILE_MISSING_OR_EMPTY("Downloaded APK file is missing or empty."),
  DANGEROUS_SCRIPT_OR_EXECUTABLE("Rejected file: detected script, ELF, PE, or non-APK payload."),
  INVALID_ZIP_OR_APK_MAGIC("Rejected file: invalid APK ZIP structure or missing AndroidManifest.xml/classes.dex."),
  UNTRUSTED_OR_UNSIGNED_MANIFEST("Rejected update: update.json metadata signature is missing or invalid."),
  SHA256_MISMATCH("Rejected APK: SHA-256 checksum does not match signed update.json metadata."),
  UNSIGNED_OR_CORRUPTED_APK("Rejected APK: package archive is unsigned or corrupted."),
  PACKAGE_NAME_MISMATCH("Rejected APK: package name does not match installed application ID (com.bayanulhuda.com)."),
  VERSION_DOWNGRADE_REJECTED("Rejected APK: version code is older than or equal to the installed application (downgrade blocked)."),
  SIGNING_CERTIFICATE_MISMATCH("Rejected APK: signing certificate does not match the installed application's certificate.")
}

/**
 * Signed update.json metadata produced automatically by GitHub Actions CI/CD.
 */
data class SignedUpdateManifest(
  val version: String,
  val versionCode: Long,
  val minimumVersionCode: Long,
  val downloadUrl: String,
  val sha256: String,
  val releaseNotes: String,
  val forceUpdate: Boolean,
  val packageName: String = AppUpdaterConstants.EXPECTED_APPLICATION_ID,
  val signingCertSha256: String = "",
  val publishedAt: String = "",
  val signatureBase64: String = "",
  val publicKeyDerBase64: String = "",
  val signatureAlgorithm: String = "SHA256withRSA"
) {
  fun toCanonicalSigningPayload(): String {
    return buildString {
      append("bayan-al-huda-update-v1")
      append("|package=").append(packageName.trim())
      append("|version=").append(version.trim())
      append("|version_code=").append(versionCode)
      append("|minimum_version_code=").append(minimumVersionCode)
      append("|download_url=").append(downloadUrl.trim())
      append("|sha256=").append(sha256.trim().lowercase(Locale.ROOT))
      append("|force_update=").append(forceUpdate)
      append("|signing_cert_sha256=").append(signingCertSha256.trim().lowercase(Locale.ROOT))
    }
  }

  fun toJsonObject(): JSONObject {
    return JSONObject().apply {
      put("version", version)
      put("version_code", versionCode)
      put("minimum_version_code", minimumVersionCode)
      put("download_url", downloadUrl)
      put("sha256", sha256.lowercase(Locale.ROOT))
      put("release_notes", releaseNotes)
      put("force_update", forceUpdate)
      put("package_name", packageName)
      put("signing_cert_sha256", signingCertSha256.lowercase(Locale.ROOT))
      put("published_at", publishedAt)
      put("signature_base64", signatureBase64)
      put("public_key_der_base64", publicKeyDerBase64)
      put("signature_algorithm", signatureAlgorithm)
    }
  }

  companion object {
    fun fromJsonString(rawJson: String): SignedUpdateManifest? {
      if (rawJson.isBlank()) return null
      return runCatching {
        val obj = JSONObject(rawJson)
        val version = obj.optString("version", "").trim().removePrefix("v")
        val versionCode = obj.optLong("version_code", -1L)
        val minVersionCode = obj.optLong("minimum_version_code", 1L)
        val downloadUrl = obj.optString("download_url", "").trim()
        val sha256 = obj.optString("sha256", "").trim().lowercase(Locale.ROOT)
        val releaseNotes = obj.optString("release_notes", "").trim()
        val forceUpdate = obj.optBoolean("force_update", false)
        val packageName = obj.optString("package_name", AppUpdaterConstants.EXPECTED_APPLICATION_ID).trim()
        val signingCertSha256 = obj.optString("signing_cert_sha256", "").trim().lowercase(Locale.ROOT)
        val publishedAt = obj.optString("published_at", "").trim()
        val signatureBase64 = obj.optString("signature_base64", "").trim()
        val publicKeyDerBase64 = obj.optString("public_key_der_base64", "").trim()
        val signatureAlgorithm = obj.optString("signature_algorithm", "SHA256withRSA").trim()

        if (version.isEmpty() || versionCode <= 0L || downloadUrl.isEmpty() || sha256.length != 64) {
          null
        } else {
          SignedUpdateManifest(
            version = version,
            versionCode = versionCode,
            minimumVersionCode = minVersionCode.coerceAtLeast(1L),
            downloadUrl = downloadUrl,
            sha256 = sha256,
            releaseNotes = releaseNotes,
            forceUpdate = forceUpdate,
            packageName = packageName,
            signingCertSha256 = signingCertSha256,
            publishedAt = publishedAt,
            signatureBase64 = signatureBase64,
            publicKeyDerBase64 = publicKeyDerBase64,
            signatureAlgorithm = signatureAlgorithm
          )
        }
      }.getOrNull()
    }
  }
}

data class GitHubReleaseAsset(
  val name: String,
  val browserDownloadUrl: String,
  val sizeBytes: Long,
  val contentType: String
)

data class GitHubReleaseInfo(
  val tagName: String,
  val versionName: String,
  val versionCode: Long,
  val minimumVersionCode: Long,
  val releaseTitle: String,
  val releaseNotes: String,
  val publishedAt: String,
  val isDraft: Boolean,
  val isPrerelease: Boolean,
  val apkAsset: GitHubReleaseAsset,
  val manifestAsset: GitHubReleaseAsset? = null,
  val checksumAsset: GitHubReleaseAsset? = null,
  val signedManifest: SignedUpdateManifest? = null,
  val htmlUrl: String = AppUpdaterConstants.GITHUB_RELEASES_WEB_URL,
  val forceUpdate: Boolean = false
) {
  fun computeUrgency(installedVersionCode: Long): UpdateUrgency {
    if (versionCode <= installedVersionCode) return UpdateUrgency.NONE
    return if (forceUpdate || installedVersionCode < minimumVersionCode) {
      UpdateUrgency.CRITICAL
    } else {
      UpdateUrgency.OPTIONAL
    }
  }
}

data class ApkVerificationResult(
  val isValid: Boolean,
  val packageName: String? = null,
  val versionName: String? = null,
  val versionCode: Long? = null,
  val actualSha256: String? = null,
  val expectedSha256: String? = null,
  val installedSigningCertSha256: String? = null,
  val apkSigningCertSha256: String? = null,
  val manifestSignatureVerified: Boolean = false,
  val failureReason: ApkVerificationFailureReason? = null,
  val errorMessage: String? = null
)

data class UpdateHistoryEntry(
  val timestampMs: Long,
  val installedVersionName: String,
  val installedVersionCode: Long,
  val targetVersionName: String,
  val targetVersionCode: Long,
  val status: String,
  val details: String
) {
  fun toJson(): JSONObject = JSONObject().apply {
    put("timestampMs", timestampMs)
    put("installedVersionName", installedVersionName)
    put("installedVersionCode", installedVersionCode)
    put("targetVersionName", targetVersionName)
    put("targetVersionCode", targetVersionCode)
    put("status", status)
    put("details", details)
  }

  companion object {
    fun fromJson(obj: JSONObject): UpdateHistoryEntry {
      return UpdateHistoryEntry(
        timestampMs = obj.optLong("timestampMs", 0L),
        installedVersionName = obj.optString("installedVersionName", ""),
        installedVersionCode = obj.optLong("installedVersionCode", 0L),
        targetVersionName = obj.optString("targetVersionName", ""),
        targetVersionCode = obj.optLong("targetVersionCode", 0L),
        status = obj.optString("status", ""),
        details = obj.optString("details", "")
      )
    }

    fun parseHistoryList(rawJsonArray: String?): List<UpdateHistoryEntry> {
      if (rawJsonArray.isNullOrBlank()) return emptyList()
      return runCatching {
        val arr = JSONArray(rawJsonArray)
        (0 until arr.length()).mapNotNull { idx ->
          arr.optJSONObject(idx)?.let { fromJson(it) }
        }
      }.getOrDefault(emptyList())
    }
  }
}

data class UpdaterUiState(
  val installedVersionName: String = com.example.BuildConfig.VERSION_NAME,
  val installedVersionCode: Long = com.example.BuildConfig.VERSION_CODE.toLong(),
  val currentBuildConfigVersionCode: Int = com.example.BuildConfig.VERSION_CODE,
  val distributionChannel: DistributionChannel = DistributionChannel.DIRECT_APK_GITHUB,
  val status: UpdaterStatus = UpdaterStatus.IDLE,
  val urgency: UpdateUrgency = UpdateUrgency.NONE,
  val latestRelease: GitHubReleaseInfo? = null,
  val downloadProgressPercent: Int = 0,
  val downloadedBytes: Long = 0L,
  val totalBytes: Long = 0L,
  val verifiedApkFilePath: String? = null,
  val lastCheckedTimestampMs: Long = 0L,
  val showUpdateNotificationBanner: Boolean = false,
  val showUpdateDialog: Boolean = false,
  val showReleaseDetailsDialog: Boolean = false,
  val isFreshTestSimulation: Boolean = false,
  val remindMeLaterUntilMs: Long = 0L,
  val verificationResult: ApkVerificationResult? = null,
  val errorMessage: String? = null,
  val canRequestPackageInstalls: Boolean = true,
  val updateHistory: List<UpdateHistoryEntry> = emptyList()
)
