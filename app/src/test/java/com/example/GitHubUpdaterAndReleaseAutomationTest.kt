package com.example

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import androidx.test.core.app.ApplicationProvider
import com.example.data.updater.AndroidPackageInstallerCoordinator
import com.example.data.updater.ApkInstallStatusReceiver
import com.example.data.updater.ApkSecurityVerifier
import com.example.data.updater.ApkUpdateDownloadManager
import com.example.data.updater.ApkVerificationFailureReason
import com.example.data.updater.AppUpdateManager
import com.example.data.updater.AppUpdaterConstants
import com.example.data.updater.DistributionChannel
import com.example.data.updater.DistributionChannelDetector
import com.example.data.updater.GitHubReleaseClient
import com.example.data.updater.SignedUpdateManifest
import com.example.data.updater.UpdateManifestVerifier
import com.example.data.updater.UpdateUrgency
import com.example.data.updater.UpdaterStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Comprehensive verification suite covering all 20 scenarios for the Bayan al-Huda
 * Secure GitHub APK Installer, In-App Updater, Dual Distribution Mode, and CI/CD Release System.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GitHubUpdaterAndReleaseAutomationTest {

  private lateinit var context: Context
  private lateinit var trustedKeyPair: KeyPair
  private lateinit var untrustedKeyPair: KeyPair
  private lateinit var trustedCertSha256: String
  private lateinit var untrustedCertSha256: String

  private fun resolveRepoRootFile(relativePath: String): File {
    val candidates = listOf(
      File(relativePath),
      File("..", relativePath),
      File("../..", relativePath)
    )
    return candidates.firstOrNull { it.exists() } ?: File(relativePath)
  }

  /**
   * Builds a valid minimal APK ZIP byte array containing AndroidManifest.xml and classes.dex.
   */
  private fun buildValidMinimalApkBytes(versionMarker: String = "v1.1.0"): ByteArray {
    val baos = ByteArrayOutputStream()
    ZipOutputStream(baos).use { zos ->
      zos.putNextEntry(ZipEntry("AndroidManifest.xml"))
      zos.write("BayanAlHudaManifest-$versionMarker".toByteArray(StandardCharsets.UTF_8))
      zos.closeEntry()

      zos.putNextEntry(ZipEntry("classes.dex"))
      zos.write("dex\n035\u0000BayanClasses-$versionMarker".toByteArray(StandardCharsets.UTF_8))
      zos.closeEntry()

      zos.putNextEntry(ZipEntry("META-INF/CERT.RSA"))
      zos.write("SignedApkBlock-$versionMarker".toByteArray(StandardCharsets.UTF_8))
      zos.closeEntry()
    }
    return baos.toByteArray()
  }

  private fun buildSignedReleaseApiPayloads(
    versionName: String = "1.1.0",
    versionCode: Long = 9L,
    minimumVersionCode: Long = 8L,
    apkBytes: ByteArray = buildValidMinimalApkBytes(versionName),
    forceUpdate: Boolean = false,
    isDraft: Boolean = false,
    isPrerelease: Boolean = false,
    signingKeyPair: KeyPair = trustedKeyPair,
    certSha256: String = trustedCertSha256
  ): Pair<String, SignedUpdateManifest> {
    val apkSha256 = UpdateManifestVerifier.sha256Hex(apkBytes)
    val downloadUrl =
      "https://github.com/zulqarnain7861/Bayan-ul-huda-quran-o-hadees/releases/download/v$versionName/Bayan-al-Huda-v$versionName.apk"
    val manifestUrl =
      "https://github.com/zulqarnain7861/Bayan-ul-huda-quran-o-hadees/releases/download/v$versionName/update.json"

    val unsignedManifest = SignedUpdateManifest(
      version = versionName,
      versionCode = versionCode,
      minimumVersionCode = minimumVersionCode,
      downloadUrl = downloadUrl,
      sha256 = apkSha256,
      releaseNotes = "- Bug fixes.\n- Improved performance.\n- New features.",
      forceUpdate = forceUpdate,
      packageName = AppUpdaterConstants.EXPECTED_APPLICATION_ID,
      signingCertSha256 = certSha256,
      publishedAt = "2026-11-09T08:00:00Z"
    )
    val signedManifest = UpdateManifestVerifier.signManifest(
      manifest = unsignedManifest,
      privateKey = signingKeyPair.private,
      publicKey = signingKeyPair.public,
      signingCertSha256 = certSha256
    )

    val releaseJson = """
      {
        "tag_name": "v$versionName",
        "name": "Bayan al-Huda v$versionName",
        "draft": $isDraft,
        "prerelease": $isPrerelease,
        "published_at": "2026-11-09T08:00:00Z",
        "html_url": "https://github.com/zulqarnain7861/Bayan-ul-huda-quran-o-hadees/releases/tag/v$versionName",
        "body": "- Bug fixes.\n- Improved performance.\n- New features.",
        "assets": [
          {
            "name": "Bayan-al-Huda-v$versionName.apk",
            "browser_download_url": "$downloadUrl",
            "size": ${apkBytes.size},
            "content_type": "application/vnd.android.package-archive"
          },
          {
            "name": "update.json",
            "browser_download_url": "$manifestUrl",
            "size": 512,
            "content_type": "application/json"
          }
        ]
      }
    """.trimIndent()

    return releaseJson to signedManifest
  }

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    context.getSharedPreferences(AppUpdaterConstants.UPDATER_PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()
    ApkUpdateDownloadManager.cleanupTemporaryAndOldApks(context)
    DistributionChannelDetector.clearTestingOverride()
    ApkSecurityVerifier.clearTestingOverrides()
    AndroidPackageInstallerCoordinator.clearTestingOverrides()
    GitHubReleaseClient.customTransportForTesting = null
    ApkUpdateDownloadManager.customStreamDownloaderForTesting = null
    AppUpdateManager.resetInstanceForTesting()

    val kpg = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }
    trustedKeyPair = kpg.generateKeyPair()
    untrustedKeyPair = kpg.generateKeyPair()
    trustedCertSha256 = UpdateManifestVerifier.sha256Hex(trustedKeyPair.public.encoded)
    untrustedCertSha256 = UpdateManifestVerifier.sha256Hex(untrustedKeyPair.public.encoded)

    // Configure installed app identity to v1.0.2 (versionCode = 8) with trustedKeyPair
    ApkSecurityVerifier.customInstalledIdentityForTesting = ApkSecurityVerifier.InstalledAppIdentity(
      packageName = AppUpdaterConstants.EXPECTED_APPLICATION_ID,
      versionName = "1.0.2",
      versionCode = 8L,
      signingCertSha256 = trustedCertSha256,
      publicKey = trustedKeyPair.public
    )
    DistributionChannelDetector.setInstallerPackageForTesting(null, enabled = true)
    AndroidPackageInstallerCoordinator.overrideCanRequestPackageInstallsForTesting = true
  }

  @After
  fun tearDown() {
    DistributionChannelDetector.clearTestingOverride()
    ApkSecurityVerifier.clearTestingOverrides()
    AndroidPackageInstallerCoordinator.clearTestingOverrides()
    GitHubReleaseClient.customTransportForTesting = null
    ApkUpdateDownloadManager.customStreamDownloaderForTesting = null
    ApkUpdateDownloadManager.cleanupTemporaryAndOldApks(context)
    AppUpdateManager.resetInstanceForTesting()
  }

  @Test
  fun test01_02_03_firstInstallation_noUpdateAvailable_and_newGitHubReleaseAvailable() = runBlocking {
    val manager = AppUpdateManager.getInstance(context)

    // 1. First installation initial state
    val initial = manager.uiState.value
    assertEquals("1.0.2", initial.installedVersionName)
    assertEquals(8L, initial.installedVersionCode)
    assertEquals(DistributionChannel.DIRECT_APK_GITHUB, initial.distributionChannel)
    assertEquals(UpdaterStatus.IDLE, initial.status)
    assertFalse(initial.showUpdateNotificationBanner)

    // 2. No update available (GitHub has v1.0.2 / versionCode 8)
    val (sameVersionJson, sameManifest) = buildSignedReleaseApiPayloads(
      versionName = "1.0.2",
      versionCode = 8L
    )
    GitHubReleaseClient.customTransportForTesting = object : GitHubReleaseClient.HttpTransport {
      override fun get(url: String, headers: Map<String, String>): GitHubReleaseClient.TransportResponse {
        return if (url.endsWith("update.json")) {
          GitHubReleaseClient.TransportResponse(200, sameManifest.toJsonObject().toString())
        } else {
          GitHubReleaseClient.TransportResponse(200, sameVersionJson)
        }
      }
    }

    val upToDateState = manager.checkForUpdatesNow()
    assertEquals(UpdaterStatus.UP_TO_DATE, upToDateState.status)
    assertEquals(UpdateUrgency.NONE, upToDateState.urgency)
    assertFalse(upToDateState.showUpdateNotificationBanner)

    // 3. New GitHub release available (v1.1.0 / versionCode 9)
    val (newVersionJson, newManifest) = buildSignedReleaseApiPayloads(
      versionName = "1.1.0",
      versionCode = 9L,
      minimumVersionCode = 8L,
      forceUpdate = false
    )
    GitHubReleaseClient.customTransportForTesting = object : GitHubReleaseClient.HttpTransport {
      override fun get(url: String, headers: Map<String, String>): GitHubReleaseClient.TransportResponse {
        return if (url.endsWith("update.json")) {
          GitHubReleaseClient.TransportResponse(200, newManifest.toJsonObject().toString())
        } else {
          GitHubReleaseClient.TransportResponse(200, newVersionJson)
        }
      }
    }

    val updateAvailableState = manager.checkForUpdatesNow()
    assertEquals(UpdaterStatus.UPDATE_AVAILABLE, updateAvailableState.status)
    assertEquals(UpdateUrgency.OPTIONAL, updateAvailableState.urgency)
    assertTrue(updateAvailableState.showUpdateNotificationBanner)
    assertEquals("1.1.0", updateAvailableState.latestRelease?.versionName)
    assertEquals(9L, updateAvailableState.latestRelease?.versionCode)

    // Verify Remind Me Later snoozes banner
    manager.remindMeLater(nowMs = 1_000_000L)
    assertFalse(manager.uiState.value.showUpdateNotificationBanner)
  }

  @Test
  fun test04_05_slowInternetTimeout_and_noInternetOffline_handledGracefully() = runBlocking {
    val manager = AppUpdateManager.getInstance(context)

    // 4. Slow internet (SocketTimeoutException)
    GitHubReleaseClient.customTransportForTesting = object : GitHubReleaseClient.HttpTransport {
      override fun get(url: String, headers: Map<String, String>): GitHubReleaseClient.TransportResponse {
        throw SocketTimeoutException("Read timed out after 30000ms")
      }
    }
    val slowState = manager.checkForUpdatesNow()
    assertEquals(UpdaterStatus.ERROR, slowState.status)
    assertTrue(slowState.errorMessage!!.contains("timed out", ignoreCase = true))

    // 5. No internet (UnknownHostException)
    GitHubReleaseClient.customTransportForTesting = object : GitHubReleaseClient.HttpTransport {
      override fun get(url: String, headers: Map<String, String>): GitHubReleaseClient.TransportResponse {
        throw UnknownHostException("Unable to resolve host api.github.com")
      }
    }
    val offlineState = manager.checkForUpdatesNow()
    assertEquals(UpdaterStatus.ERROR, offlineState.status)
    assertTrue(offlineState.errorMessage!!.contains("No internet", ignoreCase = true))
  }

  @Test
  fun test06_interruptedDownload_resumesWithByteRangeAndVerifiesCleanly() = runBlocking {
    val manager = AppUpdateManager.getInstance(context)
    val fullApkBytes = buildValidMinimalApkBytes("v1.1.0-resumable")
    val splitPoint = fullApkBytes.size / 2
    val (releaseJson, signedManifest) = buildSignedReleaseApiPayloads(
      versionName = "1.1.0",
      versionCode = 9L,
      apkBytes = fullApkBytes
    )

    GitHubReleaseClient.customTransportForTesting = object : GitHubReleaseClient.HttpTransport {
      override fun get(url: String, headers: Map<String, String>): GitHubReleaseClient.TransportResponse {
        return if (url.endsWith("update.json")) {
          GitHubReleaseClient.TransportResponse(200, signedManifest.toJsonObject().toString())
        } else {
          GitHubReleaseClient.TransportResponse(200, releaseJson)
        }
      }
    }
    ApkSecurityVerifier.customArchiveInspectorForTesting = ApkSecurityVerifier.ApkArchiveInspector { _, _ ->
      ApkSecurityVerifier.ParsedApkMetadata(
        packageName = AppUpdaterConstants.EXPECTED_APPLICATION_ID,
        versionName = "1.1.0",
        versionCode = 9L,
        signingCertSha256 = trustedCertSha256,
        publicKey = trustedKeyPair.public
      )
    }

    manager.checkForUpdatesNow()

    // First attempt: interrupts halfway through the stream
    var attempt = 0
    ApkUpdateDownloadManager.customStreamDownloaderForTesting = object : ApkUpdateDownloadManager.StreamDownloader {
      override suspend fun downloadStream(
        url: String,
        resumeFromByteOffset: Long,
        onStreamOpened: suspend (Int, Long, Boolean, InputStream) -> Unit
      ) {
        attempt++
        if (attempt == 1) {
          assertEquals(0L, resumeFromByteOffset)
          val failingStream = object : InputStream() {
            private var pos = 0
            override fun read(): Int {
              if (pos >= splitPoint) throw IOException("Simulated network drop at byte $pos")
              return fullApkBytes[pos++].toInt() and 0xFF
            }
          }
          onStreamOpened(200, fullApkBytes.size.toLong(), true, failingStream)
        } else {
          // Second attempt: resumes from splitPoint with HTTP 206 Partial Content!
          assertEquals(splitPoint.toLong(), resumeFromByteOffset)
          val remainingBytes = fullApkBytes.copyOfRange(splitPoint, fullApkBytes.size)
          onStreamOpened(
            206,
            remainingBytes.size.toLong(),
            true,
            ByteArrayInputStream(remainingBytes)
          )
        }
      }
    }

    val interruptedState = manager.downloadAndVerifyLatestUpdate(resumeIfPartial = true)
    assertEquals(UpdaterStatus.ERROR, interruptedState.status)
    assertTrue(interruptedState.errorMessage!!.contains("interrupted", ignoreCase = true))

    // Retry resumes from the partial `.part` file and succeeds!
    val resumedState = manager.downloadAndVerifyLatestUpdate(resumeIfPartial = true)
    assertEquals(UpdaterStatus.READY_TO_INSTALL, resumedState.status)
    assertNotNull(resumedState.verificationResult)
    assertTrue(resumedState.verificationResult!!.isValid)
    assertEquals(100, resumedState.downloadProgressPercent)
  }

  @Test
  fun test07_08_09_19_corruptedApk_incorrectSigningCert_versionDowngrade_and_manifestSignatureVerification() = runBlocking {
    val validApkBytes = buildValidMinimalApkBytes("v1.1.0")
    val (_, validSignedManifest) = buildSignedReleaseApiPayloads(
      versionName = "1.1.0",
      versionCode = 9L,
      apkBytes = validApkBytes
    )

    // Test 19: Manifest signature verification accepts authentic signature and rejects tampered fields or untrusted key
    assertTrue(
      UpdateManifestVerifier.verifyManifestSignature(
        manifest = validSignedManifest,
        installedAppPublicKey = trustedKeyPair.public,
        installedSigningCertSha256 = trustedCertSha256
      )
    )
    assertFalse(
      "Tampered SHA-256 in update.json must fail signature check",
      UpdateManifestVerifier.verifyManifestSignature(
        manifest = validSignedManifest.copy(sha256 = "0".repeat(64)),
        installedAppPublicKey = trustedKeyPair.public,
        installedSigningCertSha256 = trustedCertSha256
      )
    )
    assertFalse(
      "Tampered download URL in update.json must fail signature check",
      UpdateManifestVerifier.verifyManifestSignature(
        manifest = validSignedManifest.copy(downloadUrl = "https://evil.example.com/malware.apk"),
        installedAppPublicKey = trustedKeyPair.public,
        installedSigningCertSha256 = trustedCertSha256
      )
    )
    assertFalse(
      "Manifest signed with untrusted key must fail verification",
      UpdateManifestVerifier.verifyManifestSignature(
        manifest = validSignedManifest,
        installedAppPublicKey = untrustedKeyPair.public,
        installedSigningCertSha256 = untrustedCertSha256
      )
    )

    val tempApkFile = File(ApkUpdateDownloadManager.getUpdatesDirectory(context), "test-verify.apk")

    // Test 7A: Disguised shell script rejected immediately
    tempApkFile.writeText("#!/bin/sh\nrm -rf /\n" + "A".repeat(120))
    val scriptResult = ApkSecurityVerifier.verifyDownloadedApk(context, tempApkFile, validSignedManifest)
    assertFalse(scriptResult.isValid)
    assertEquals(ApkVerificationFailureReason.DANGEROUS_SCRIPT_OR_EXECUTABLE, scriptResult.failureReason)

    // Test 7B: Corrupted APK (valid ZIP structure but tampered bytes -> SHA-256 mismatch)
    val corruptedApkBytes = buildValidMinimalApkBytes("v1.1.0-tampered-bytes")
    tempApkFile.writeBytes(corruptedApkBytes)
    val shaMismatchResult = ApkSecurityVerifier.verifyDownloadedApk(context, tempApkFile, validSignedManifest)
    assertFalse(shaMismatchResult.isValid)
    assertEquals(ApkVerificationFailureReason.SHA256_MISMATCH, shaMismatchResult.failureReason)

    // Write valid APK bytes for archive metadata checks
    tempApkFile.writeBytes(validApkBytes)

    // Test 8: Incorrect signing certificate rejected
    ApkSecurityVerifier.customArchiveInspectorForTesting = ApkSecurityVerifier.ApkArchiveInspector { _, _ ->
      ApkSecurityVerifier.ParsedApkMetadata(
        packageName = AppUpdaterConstants.EXPECTED_APPLICATION_ID,
        versionName = "1.1.0",
        versionCode = 9L,
        signingCertSha256 = untrustedCertSha256, // Mismatched certificate!
        publicKey = untrustedKeyPair.public
      )
    }
    val wrongCertResult = ApkSecurityVerifier.verifyDownloadedApk(context, tempApkFile, validSignedManifest)
    assertFalse(wrongCertResult.isValid)
    assertEquals(ApkVerificationFailureReason.SIGNING_CERTIFICATE_MISMATCH, wrongCertResult.failureReason)

    // Test 9: Version downgrade (APK versionCode = 7 when installed is 8) rejected
    val (_, downgradeManifest) = buildSignedReleaseApiPayloads(
      versionName = "1.0.1",
      versionCode = 7L,
      apkBytes = validApkBytes
    )
    ApkSecurityVerifier.customArchiveInspectorForTesting = ApkSecurityVerifier.ApkArchiveInspector { _, _ ->
      ApkSecurityVerifier.ParsedApkMetadata(
        packageName = AppUpdaterConstants.EXPECTED_APPLICATION_ID,
        versionName = "1.0.1",
        versionCode = 7L,
        signingCertSha256 = trustedCertSha256,
        publicKey = trustedKeyPair.public
      )
    }
    val downgradeResult = ApkSecurityVerifier.verifyDownloadedApk(
      context = context,
      apkFile = tempApkFile,
      signedManifest = downgradeManifest,
      expectedVersionCode = 7L
    )
    assertFalse(downgradeResult.isValid)
    assertEquals(ApkVerificationFailureReason.VERSION_DOWNGRADE_REJECTED, downgradeResult.failureReason)

    tempApkFile.delete()
    Unit
  }

  @Test
  fun test10_11_12_13_14_20_installationPermissions_cancel_success_playStoreVsDirectApk_and_upgradeFromOlderVersion() = runBlocking {
    val manager = AppUpdateManager.getInstance(context)
    val apkBytes = buildValidMinimalApkBytes("v1.1.0")
    val (releaseJson, signedManifest) = buildSignedReleaseApiPayloads(
      versionName = "1.1.0",
      versionCode = 9L,
      apkBytes = apkBytes
    )

    GitHubReleaseClient.customTransportForTesting = object : GitHubReleaseClient.HttpTransport {
      override fun get(url: String, headers: Map<String, String>): GitHubReleaseClient.TransportResponse {
        return if (url.endsWith("update.json")) {
          GitHubReleaseClient.TransportResponse(200, signedManifest.toJsonObject().toString())
        } else {
          GitHubReleaseClient.TransportResponse(200, releaseJson)
        }
      }
    }
    ApkUpdateDownloadManager.customStreamDownloaderForTesting = object : ApkUpdateDownloadManager.StreamDownloader {
      override suspend fun downloadStream(
        url: String,
        resumeFromByteOffset: Long,
        onStreamOpened: suspend (Int, Long, Boolean, InputStream) -> Unit
      ) {
        onStreamOpened(200, apkBytes.size.toLong(), true, ByteArrayInputStream(apkBytes))
      }
    }
    ApkSecurityVerifier.customArchiveInspectorForTesting = ApkSecurityVerifier.ApkArchiveInspector { _, _ ->
      ApkSecurityVerifier.ParsedApkMetadata(
        packageName = AppUpdaterConstants.EXPECTED_APPLICATION_ID,
        versionName = "1.1.0",
        versionCode = 9L,
        signingCertSha256 = trustedCertSha256,
        publicKey = trustedKeyPair.public
      )
    }

    // Test 13: Google Play installation route blocks direct GitHub APK download
    DistributionChannelDetector.setInstallerPackageForTesting("com.android.vending", enabled = true)
    manager.checkForUpdatesNow()
    assertEquals(DistributionChannel.GOOGLE_PLAY_STORE, manager.uiState.value.distributionChannel)
    val playStoreAttempt = manager.downloadAndVerifyLatestUpdate()
    assertNull("Google Play installation must never download external GitHub APK", playStoreAttempt.verifiedApkFilePath)
    assertTrue(playStoreAttempt.errorMessage!!.contains("Google Play", ignoreCase = true))

    // Test 14: Switch to Direct APK installation route
    DistributionChannelDetector.setInstallerPackageForTesting(null, enabled = true)
    manager.checkForUpdatesNow()
    assertEquals(DistributionChannel.DIRECT_APK_GITHUB, manager.uiState.value.distributionChannel)

    // Test 11: User has not yet granted "Install unknown apps" permission
    AndroidPackageInstallerCoordinator.overrideCanRequestPackageInstallsForTesting = false
    val verifiedAwaitingPerm = manager.downloadAndVerifyLatestUpdate()
    assertEquals(UpdaterStatus.AWAITING_INSTALL_PERMISSION, verifiedAwaitingPerm.status)
    val permLaunchResult = manager.promptInstallVerifiedUpdate()
    assertTrue(permLaunchResult is AndroidPackageInstallerCoordinator.InstallLaunchResult.PermissionRequired)

    // User grants permission -> promptInstallVerifiedUpdate launches confirmation
    AndroidPackageInstallerCoordinator.overrideCanRequestPackageInstallsForTesting = true
    AndroidPackageInstallerCoordinator.interceptInstallLaunchForTesting = { _, rel ->
      AndroidPackageInstallerCoordinator.recordPendingInstallTarget(context, rel.versionName, rel.versionCode)
      AndroidPackageInstallerCoordinator.InstallLaunchResult.ConfirmationLaunched(
        sessionId = 786,
        message = "Launched Android PackageInstaller confirmation"
      )
    }
    val launchResult = manager.promptInstallVerifiedUpdate()
    assertTrue(launchResult is AndroidPackageInstallerCoordinator.InstallLaunchResult.ConfirmationLaunched)
    assertEquals(UpdaterStatus.INSTALLING, manager.uiState.value.status)

    // Test 10: User cancels installation in Android PackageInstaller dialog
    val receiver = ApkInstallStatusReceiver()
    val cancelIntent = Intent(AppUpdaterConstants.INSTALL_STATUS_ACTION).apply {
      putExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE_ABORTED)
      putExtra("target_version_name", "1.1.0")
      putExtra("target_version_code", 9L)
    }
    receiver.onReceive(context, cancelIntent)
    assertEquals(UpdaterStatus.INSTALL_CANCELLED, manager.uiState.value.status)

    // Test 12 & 20: Updating from older installed version (v1.0.2 Code 8 -> v1.1.0 Code 9) & verifying post-install upgrade
    ApkSecurityVerifier.customInstalledIdentityForTesting = ApkSecurityVerifier.InstalledAppIdentity(
      packageName = AppUpdaterConstants.EXPECTED_APPLICATION_ID,
      versionName = "1.1.0",
      versionCode = 9L,
      signingCertSha256 = trustedCertSha256,
      publicKey = trustedKeyPair.public
    )
    val replacedIntent = Intent(Intent.ACTION_MY_PACKAGE_REPLACED)
    receiver.onReceive(context, replacedIntent)

    val finalState = manager.uiState.value
    assertEquals(UpdaterStatus.INSTALLED_SUCCESS, finalState.status)
    assertEquals("1.1.0", finalState.installedVersionName)
    assertEquals(9L, finalState.installedVersionCode)
    assertTrue(finalState.updateHistory.any { it.status == "INSTALL_SUCCEEDED" })
  }

  @Test
  fun test15_16_17_18_githubActionsWorkflowAndSignedManifestGeneratorScriptVerification() {
    val workflowFile = resolveRepoRootFile(".github/workflows/android-release.yml")
    assertTrue("android-release.yml must exist", workflowFile.exists())
    val workflowYaml = workflowFile.readText()

    // Verify tag trigger, secrets check, unit test run, release build, apksigner check, and gh release create
    assertTrue(workflowYaml.contains("v[0-9]+.[0-9]+.[0-9]+"))
    assertTrue(workflowYaml.contains("workflow_dispatch"))
    assertTrue(workflowYaml.contains("ANDROID_KEYSTORE_BASE64"))
    assertTrue(workflowYaml.contains("ANDROID_KEYSTORE_PASSWORD"))
    assertTrue(workflowYaml.contains("ANDROID_KEY_ALIAS"))
    assertTrue(workflowYaml.contains("ANDROID_KEY_PASSWORD"))
    assertTrue(workflowYaml.contains("STOPPED SAFELY: MISSING ANDROID RELEASE SIGNING SECRETS"))
    assertTrue(workflowYaml.contains("testDebugUnitTest"))
    assertTrue(workflowYaml.contains("assembleRelease"))
    assertTrue(workflowYaml.contains("apksigner"))
    assertTrue(workflowYaml.contains("generate_signed_update_manifest.sh"))
    assertTrue(workflowYaml.contains("gh release create"))

    // Verify setup guide and Google Play policy audit docs exist
    val setupGuide = resolveRepoRootFile("docs/GITHUB_ACTIONS_RELEASE_SETUP_GUIDE.md")
    assertTrue("Setup guide must exist", setupGuide.exists())
    val policyAuditDoc = resolveRepoRootFile("docs/GOOGLE_PLAY_POLICY_AND_SECURITY_AUDIT.md")
    assertTrue("Google Play policy audit doc must exist", policyAuditDoc.exists())

    // Execute tools/release/generate_signed_update_manifest.sh end-to-end using debug.keystore!
    val generatorScript = resolveRepoRootFile("tools/release/generate_signed_update_manifest.sh")
    assertTrue("Generator script must exist", generatorScript.exists())

    val debugKeystore = resolveRepoRootFile("debug.keystore")
    if (debugKeystore.exists()) {
      val workDir = File(context.cacheDir, "manifest_gen_test").apply {
        deleteRecursively()
        mkdirs()
      }
      val sampleApk = File(workDir, "Bayan-al-Huda-v1.1.0.apk")
      sampleApk.writeBytes(buildValidMinimalApkBytes("v1.1.0-ci-test"))

      val pb = ProcessBuilder(
        "bash",
        generatorScript.absolutePath,
        "--apk", sampleApk.absolutePath,
        "--keystore", debugKeystore.absolutePath,
        "--store-pass", "android",
        "--key-alias", "androiddebugkey",
        "--key-pass", "android",
        "--package-name", AppUpdaterConstants.EXPECTED_APPLICATION_ID,
        "--version-name", "1.1.0",
        "--version-code", "9",
        "--min-version-code", "8",
        "--download-url", "https://github.com/zulqarnain7861/Bayan-ul-huda-quran-o-hadees/releases/download/v1.1.0/Bayan-al-Huda-v1.1.0.apk",
        "--release-notes", "- Automated CI/CD test release",
        "--force-update", "false",
        "--output-dir", workDir.absolutePath
      ).redirectErrorStream(true)

      val proc = pb.start()
      val output = proc.inputStream.bufferedReader().readText()
      val exitCode = proc.waitFor()
      assertEquals("generate_signed_update_manifest.sh must exit 0. Output: $output", 0, exitCode)

      val generatedUpdateJson = File(workDir, "update.json")
      assertTrue("update.json must be generated", generatedUpdateJson.exists())
      val parsedManifest = SignedUpdateManifest.fromJsonString(generatedUpdateJson.readText())
      assertNotNull("Generated update.json must parse cleanly", parsedManifest)
      assertEquals("1.1.0", parsedManifest!!.version)
      assertEquals(9L, parsedManifest.versionCode)
      assertEquals(8L, parsedManifest.minimumVersionCode)
      assertEquals(UpdateManifestVerifier.sha256Hex(sampleApk.readBytes()), parsedManifest.sha256)
      assertTrue(parsedManifest.signatureBase64.isNotBlank())

      // Verify Android's UpdateManifestVerifier cryptographically validates the script's generated signature!
      assertTrue(
        "UpdateManifestVerifier must verify the signature produced by generate_signed_update_manifest.sh",
        UpdateManifestVerifier.verifyManifestSignature(
          manifest = parsedManifest,
          installedAppPublicKey = null, // Uses public_key_der_base64 from manifest and checks cert sha256
          installedSigningCertSha256 = parsedManifest.signingCertSha256
        )
      )
      workDir.deleteRecursively()
    }
  }

  @Test
  fun test21_inAppUpdateService_buildConfigVersionCodeComparison_updateDialogTrigger_and_freshUpdateEndToEnd() = runBlocking {
    // 1. Verify InAppUpdateService compares versionCode against BuildConfig.VERSION_CODE (8)
    assertEquals(8, BuildConfig.VERSION_CODE)
    assertFalse(
      "Same versionCode as BuildConfig.VERSION_CODE must not trigger update",
      com.example.data.updater.InAppUpdateService.isNewerVersionCode(BuildConfig.VERSION_CODE.toLong())
    )
    assertFalse(
      "Lower versionCode than BuildConfig.VERSION_CODE must not trigger update",
      com.example.data.updater.InAppUpdateService.isNewerVersionCode(BuildConfig.VERSION_CODE.toLong() - 1L)
    )
    assertTrue(
      "Higher versionCode than BuildConfig.VERSION_CODE must trigger update",
      com.example.data.updater.InAppUpdateService.isNewerVersionCode(BuildConfig.VERSION_CODE.toLong() + 1L)
    )

    // 2. Verify InAppUpdateService.checkForUpdatesAndTriggerDialog queries GitHub Releases API and sets showUpdateDialog = true
    val (newVersionJson, newManifest) = buildSignedReleaseApiPayloads(
      versionName = "1.1.0",
      versionCode = BuildConfig.VERSION_CODE.toLong() + 1L
    )
    GitHubReleaseClient.customTransportForTesting = object : GitHubReleaseClient.HttpTransport {
      override fun get(url: String, headers: Map<String, String>): GitHubReleaseClient.TransportResponse {
        return if (url.endsWith("update.json")) {
          GitHubReleaseClient.TransportResponse(200, newManifest.toJsonObject().toString())
        } else {
          GitHubReleaseClient.TransportResponse(200, newVersionJson)
        }
      }
    }

    val dialogTriggeredState = com.example.data.updater.InAppUpdateService.checkForUpdatesAndTriggerDialog(
      context = context,
      enableFreshTestFallbackWhenNoRemoteRelease = false
    )
    assertEquals(UpdaterStatus.UPDATE_AVAILABLE, dialogTriggeredState.status)
    assertTrue("Update dialog must be triggered when remote versionCode > BuildConfig.VERSION_CODE", dialogTriggeredState.showUpdateDialog)
    assertEquals(BuildConfig.VERSION_CODE, dialogTriggeredState.currentBuildConfigVersionCode)
    assertEquals(BuildConfig.VERSION_CODE.toLong() + 1L, dialogTriggeredState.latestRelease?.versionCode)

    // 3. Verify fresh update test flow stages v1.1.0 (versionCode = 9), opens the dialog, downloads, and passes all 7 verification steps
    val freshStagedState = com.example.data.updater.InAppUpdateService.triggerFreshUpdateTestRelease(context)
    assertEquals(UpdaterStatus.UPDATE_AVAILABLE, freshStagedState.status)
    assertTrue("Fresh update test must open the In-App Update Dialog", freshStagedState.showUpdateDialog)
    assertTrue("Fresh update test flag must be active", freshStagedState.isFreshTestSimulation)
    assertEquals("1.1.0", freshStagedState.latestRelease?.versionName)
    assertEquals(BuildConfig.VERSION_CODE.toLong() + 1L, freshStagedState.latestRelease?.versionCode)

    val manager = AppUpdateManager.getInstance(context)
    val verifiedFreshState = manager.downloadAndVerifyLatestUpdate(resumeIfPartial = false)
    assertEquals(UpdaterStatus.READY_TO_INSTALL, verifiedFreshState.status)
    assertEquals(100, verifiedFreshState.downloadProgressPercent)
    assertNotNull(verifiedFreshState.verificationResult)
    assertTrue("Fresh update APK must pass all 7 security verification checks", verifiedFreshState.verificationResult!!.isValid)
    Unit
  }
}

