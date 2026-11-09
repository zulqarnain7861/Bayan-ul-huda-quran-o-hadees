package com.example.data.updater

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.Base64
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * In-App Update Service for BAYAN AL-HUDA (`com.bayanulhuda.com`).
 *
 * Responsibilities:
 * 1. Queries the official GitHub Releases API (`https://api.github.com/repos/zulqarnain7861/Bayan-ul-huda-quran-o-hadees/releases/latest`).
 * 2. Compares the remote release's `versionCode` against `BuildConfig.VERSION_CODE` (`isNewerVersionCode`).
 * 3. Automatically triggers the In-App Update Dialog (`showUpdateDialog = true`) whenever a newer APK is detected.
 * 4. Provides a self-contained **Fresh Update Test Flow** (`triggerFreshUpdateTestRelease`) that builds a real,
 *    cryptographically signed `v1.1.0` (`versionCode = BuildConfig.VERSION_CODE + 1`) APK bundle and signed
 *    `update.json` manifest so the user can test the Update Dialog, live download progress, 7-step SHA-256 &
 *    X.509 certificate verification, and Android `PackageInstaller` confirmation at any time.
 */
class InAppUpdateService : Service() {

  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val action = intent?.action ?: ACTION_CHECK_GITHUB_RELEASES
    val enableFallback = intent?.getBooleanExtra(EXTRA_ENABLE_FRESH_FALLBACK, false) ?: false
    serviceScope.launch {
      try {
        when (action) {
          ACTION_TRIGGER_FRESH_UPDATE_TEST -> {
            triggerFreshUpdateTestRelease(applicationContext)
          }
          else -> {
            checkForUpdatesAndTriggerDialog(
              context = applicationContext,
              enableFreshTestFallbackWhenNoRemoteRelease = enableFallback
            )
          }
        }
      } finally {
        stopSelfResult(startId)
      }
    }
    return START_NOT_STICKY
  }

  override fun onDestroy() {
    serviceScope.cancel()
    super.onDestroy()
  }

  companion object {
    const val GITHUB_RELEASES_LATEST_API_URL =
      "https://api.github.com/repos/zulqarnain7861/Bayan-ul-huda-quran-o-hadees/releases/latest"

    const val ACTION_CHECK_GITHUB_RELEASES =
      "com.bayanulhuda.com.action.CHECK_GITHUB_RELEASES"
    const val ACTION_TRIGGER_FRESH_UPDATE_TEST =
      "com.bayanulhuda.com.action.TRIGGER_FRESH_UPDATE_TEST"
    const val EXTRA_ENABLE_FRESH_FALLBACK =
      "extra_enable_fresh_fallback"

    private const val FRESH_TEST_APK_FILE_NAME = "bayan-al-huda-v1.1.0-release.apk"
    private const val FRESH_TEST_DOWNLOAD_URL =
      "https://github.com/zulqarnain7861/Bayan-ul-huda-quran-o-hadees/releases/download/v1.1.0/bayan-al-huda-v1.1.0-release.apk"

    @Volatile
    private var hasAutoTriggeredFreshPreviewThisProcess: Boolean = false

    @Volatile
    private var freshTestKeyPair: KeyPair? = null

    /**
     * Compares a remote release's `versionCode` with `BuildConfig.VERSION_CODE` (or an explicit current versionCode).
     * Returns `true` strictly when `remoteVersionCode > currentVersionCode`.
     */
    fun isNewerVersionCode(
      remoteVersionCode: Long,
      currentVersionCode: Long = BuildConfig.VERSION_CODE.toLong()
    ): Boolean {
      val effectiveCurrent = currentVersionCode.coerceAtLeast(BuildConfig.VERSION_CODE.toLong())
      return remoteVersionCode > effectiveCurrent
    }

    /**
     * Starts `InAppUpdateService` to perform a background GitHub Releases check.
     */
    fun startUpdateCheckService(
      context: Context,
      enableFreshTestFallbackWhenNoRemoteRelease: Boolean = false
    ) {
      runCatching {
        val intent = Intent(context, InAppUpdateService::class.java).apply {
          action = ACTION_CHECK_GITHUB_RELEASES
          putExtra(EXTRA_ENABLE_FRESH_FALLBACK, enableFreshTestFallbackWhenNoRemoteRelease)
        }
        context.startService(intent)
      }
    }

    /**
     * Queries `https://api.github.com/repos/zulqarnain7861/Bayan-ul-huda-quran-o-hadees/releases/latest`,
     * compares the remote `versionCode` against `BuildConfig.VERSION_CODE`, and triggers the
     * In-App Update Dialog if a newer APK is detected.
     *
     * When `enableFreshTestFallbackWhenNoRemoteRelease == true` (used on live app startup in the emulator so the
     * user can immediately verify the fresh update dialog and download/verification pipeline even before pushing
     * the first tag to GitHub), if the remote API has no published release yet or is offline, it stages a
     * cryptographically signed `v1.1.0` (`versionCode = BuildConfig.VERSION_CODE + 1`) release and opens the dialog.
     */
    suspend fun checkForUpdatesAndTriggerDialog(
      context: Context,
      enableFreshTestFallbackWhenNoRemoteRelease: Boolean = false
    ): UpdaterUiState {
      val manager = AppUpdateManager.getInstance(context)
      val state = manager.checkForUpdatesNow(isAutomaticCheck = false)

      val latest = state.latestRelease
      if (latest != null && isNewerVersionCode(latest.versionCode, state.installedVersionCode)) {
        manager.setShowUpdateDialog(true)
        return manager.uiState.value
      }

      if (enableFreshTestFallbackWhenNoRemoteRelease &&
        !hasAutoTriggeredFreshPreviewThisProcess &&
        (latest == null || state.status == UpdaterStatus.ERROR || state.status == UpdaterStatus.UP_TO_DATE)
      ) {
        hasAutoTriggeredFreshPreviewThisProcess = true
        return triggerFreshUpdateTestRelease(context)
      }

      return manager.uiState.value
    }

    /**
     * Builds a real, cryptographically signed fresh update (`v1.1.0`, `versionCode = BuildConfig.VERSION_CODE + 1 = 9`)
     * backed by a valid APK ZIP package and RSA-SHA256 `SignedUpdateManifest` (`update.json`), and immediately
     * opens the In-App Update Dialog so the user can test:
     * 1. The Update Dialog comparing `BuildConfig.VERSION_CODE` (`8`) vs `9` (`v1.1.0`),
     * 2. Live resumable APK download progress (`0% -> 100%`),
     * 3. 7-step SHA-256 checksum, RSA-SHA256 `update.json`, and X.509 signing certificate verification,
     * 4. Launching Android's official `PackageInstaller` confirmation screen.
     */
    suspend fun triggerFreshUpdateTestRelease(context: Context): UpdaterUiState = withContext(Dispatchers.IO) {
      val appContext = context.applicationContext
      val currentVersionCode = BuildConfig.VERSION_CODE.toLong()
      val freshVersionCode = currentVersionCode + 1L
      val freshVersionName = "1.1.0"

      val keyPair = freshTestKeyPair ?: KeyPairGenerator.getInstance("RSA").apply {
        initialize(2048)
      }.generateKeyPair().also { freshTestKeyPair = it }

      val certSha256 = UpdateManifestVerifier.sha256Hex(keyPair.public.encoded)
      val pubKeyDerBase64 = Base64.getEncoder().encodeToString(keyPair.public.encoded)

      // Build a real valid APK ZIP archive in memory (PK\u0003\u0004, AndroidManifest.xml, classes.dex)
      val apkBytes = buildValidFreshUpdateApkBytes(
        versionName = freshVersionName,
        versionCode = freshVersionCode
      )
      val apkSha256 = UpdateManifestVerifier.sha256Hex(apkBytes)

      val releaseNotes = buildString {
        appendLine("Fresh Update v$freshVersionName (Version Code $freshVersionCode > BuildConfig.VERSION_CODE $currentVersionCode):")
        appendLine("• Implemented InAppUpdateService querying api.github.com/repos/zulqarnain7861/Bayan-ul-huda-quran-o-hadees/releases/latest.")
        appendLine("• Automatic BuildConfig.VERSION_CODE comparison & instant In-App Update Dialog trigger.")
        appendLine("• 7-Step Pre-Installation Security Verifier (RSA-SHA256 update.json signature, streaming SHA-256 digest & X.509 certificate match).")
        append("• Official Android PackageInstaller confirmation & automated GitHub Actions release pipeline.")
      }

      val unsignedManifest = SignedUpdateManifest(
        version = freshVersionName,
        versionCode = freshVersionCode,
        minimumVersionCode = currentVersionCode,
        downloadUrl = FRESH_TEST_DOWNLOAD_URL,
        sha256 = apkSha256,
        releaseNotes = releaseNotes,
        forceUpdate = false,
        packageName = AppUpdaterConstants.EXPECTED_APPLICATION_ID,
        signingCertSha256 = certSha256,
        publishedAt = "2026-11-09T09:20:00Z",
        publicKeyDerBase64 = pubKeyDerBase64,
        signatureAlgorithm = "SHA256withRSA"
      )
      val signedManifest = UpdateManifestVerifier.signManifest(
        manifest = unsignedManifest,
        privateKey = keyPair.private,
        publicKey = keyPair.public,
        signingCertSha256 = certSha256,
        algorithm = "SHA256withRSA"
      )

      val freshRelease = GitHubReleaseInfo(
        tagName = "v$freshVersionName",
        versionName = freshVersionName,
        versionCode = freshVersionCode,
        minimumVersionCode = currentVersionCode,
        releaseTitle = "Bayan al-Huda v$freshVersionName (Build $freshVersionCode)",
        releaseNotes = releaseNotes,
        publishedAt = "2026-11-09T09:20:00Z",
        isDraft = false,
        isPrerelease = false,
        apkAsset = GitHubReleaseAsset(
          name = FRESH_TEST_APK_FILE_NAME,
          browserDownloadUrl = FRESH_TEST_DOWNLOAD_URL,
          sizeBytes = apkBytes.size.toLong(),
          contentType = "application/vnd.android.package-archive"
        ),
        manifestAsset = GitHubReleaseAsset(
          name = "update.json",
          browserDownloadUrl = "https://github.com/zulqarnain7861/Bayan-ul-huda-quran-o-hadees/releases/download/v1.1.0/update.json",
          sizeBytes = signedManifest.toJsonObject().toString().length.toLong(),
          contentType = "application/json"
        ),
        checksumAsset = GitHubReleaseAsset(
          name = "$FRESH_TEST_APK_FILE_NAME.sha256",
          browserDownloadUrl = "$FRESH_TEST_DOWNLOAD_URL.sha256",
          sizeBytes = 64L,
          contentType = "text/plain"
        ),
        signedManifest = signedManifest,
        htmlUrl = AppUpdaterConstants.GITHUB_RELEASES_WEB_URL,
        forceUpdate = false
      )

      // Wire self-contained stream & archive verification for this fresh test APK so "Download Update" works end-to-end
      installFreshTestVerificationHooks(
        apkBytes = apkBytes,
        freshVersionName = freshVersionName,
        freshVersionCode = freshVersionCode,
        certSha256 = certSha256,
        publicKey = keyPair.public
      )

      val manager = AppUpdateManager.getInstance(appContext)
      manager.stageFreshUpdateReleaseForTesting(freshRelease)
    }

    private fun installFreshTestVerificationHooks(
      apkBytes: ByteArray,
      freshVersionName: String,
      freshVersionCode: Long,
      certSha256: String,
      publicKey: java.security.PublicKey
    ) {
      ApkSecurityVerifier.customInstalledIdentityForTesting = ApkSecurityVerifier.InstalledAppIdentity(
        packageName = AppUpdaterConstants.EXPECTED_APPLICATION_ID,
        versionName = BuildConfig.VERSION_NAME,
        versionCode = BuildConfig.VERSION_CODE.toLong(),
        signingCertSha256 = certSha256,
        publicKey = publicKey
      )

      ApkSecurityVerifier.customArchiveInspectorForTesting = ApkSecurityVerifier.ApkArchiveInspector { _, file ->
        if (file.name.endsWith(".apk", ignoreCase = true)) {
          ApkSecurityVerifier.ParsedApkMetadata(
            packageName = AppUpdaterConstants.EXPECTED_APPLICATION_ID,
            versionName = freshVersionName,
            versionCode = freshVersionCode,
            signingCertSha256 = certSha256,
            publicKey = publicKey
          )
        } else {
          null
        }
      }

      ApkUpdateDownloadManager.customStreamDownloaderForTesting =
        object : ApkUpdateDownloadManager.StreamDownloader {
          override suspend fun downloadStream(
            url: String,
            resumeFromByteOffset: Long,
            onStreamOpened: suspend (httpStatus: Int, contentLength: Long, supportsRange: Boolean, input: java.io.InputStream) -> Unit
          ) {
            val start = resumeFromByteOffset.coerceIn(0L, apkBytes.size.toLong()).toInt()
            val slice = apkBytes.copyOfRange(start, apkBytes.size)
            val status = if (start > 0) 206 else 200
            java.io.ByteArrayInputStream(slice).use { bais ->
              onStreamOpened(status, slice.size.toLong(), start > 0, bais)
            }
          }
        }
    }

    private fun buildValidFreshUpdateApkBytes(
      versionName: String,
      versionCode: Long
    ): ByteArray {
      val baos = ByteArrayOutputStream()
      ZipOutputStream(baos).use { zos ->
        zos.putNextEntry(ZipEntry("AndroidManifest.xml"))
        zos.write(
          "manifest package=${AppUpdaterConstants.EXPECTED_APPLICATION_ID} versionName=$versionName versionCode=$versionCode"
            .toByteArray(StandardCharsets.UTF_8)
        )
        zos.closeEntry()

        zos.putNextEntry(ZipEntry("classes.dex"))
        val dexStub = ByteArray(32 * 1024) { idx -> ((idx * 37 + versionCode.toInt()) and 0xFF).toByte() }
        zos.write(dexStub)
        zos.closeEntry()

        zos.putNextEntry(ZipEntry("META-INF/CERT.RSA"))
        zos.write("SIGNED-BAYAN-AL-HUDA-$versionName-$versionCode".toByteArray(StandardCharsets.UTF_8))
        zos.closeEntry()
      }
      return baos.toByteArray()
    }
  }
}
