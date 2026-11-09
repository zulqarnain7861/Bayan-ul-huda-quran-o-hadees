package com.example.data.updater

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.PublicKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Locale
import java.util.zip.ZipFile

/**
 * Multi-layer Pre-Installation Security Verifier for downloaded Bayan al-Huda APK updates.
 *
 * Enforces Requirement 5:
 * 1. Rejects missing, empty, or non-APK files (blocks scripts `#!`, ELF, PE `MZ`, DEX, HTML).
 * 2. Verifies valid APK ZIP structure (`PK\u0003\u0004`, `AndroidManifest.xml`, `classes.dex`, zero zip-slip entries).
 * 3. Verifies cryptographic signature on `SignedUpdateManifest` (`update.json`).
 * 4. Verifies streaming SHA-256 checksum of the APK against the verified `SignedUpdateManifest.sha256`.
 * 5. Verifies APK `packageName` matches installed application (`com.bayanulhuda.com`).
 * 6. Verifies APK `versionCode` is strictly greater than installed `versionCode` (blocks downgrades).
 * 7. Verifies APK X.509 signing certificate SHA-256 matches the installed application's signing certificate.
 */
object ApkSecurityVerifier {

  data class ParsedApkMetadata(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val signingCertSha256: String?,
    val publicKey: PublicKey? = null
  )

  fun interface ApkArchiveInspector {
    fun inspectArchive(context: Context, apkFile: File): ParsedApkMetadata?
  }

  data class InstalledAppIdentity(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val signingCertSha256: String,
    val publicKey: PublicKey?
  )

  @Volatile
  var customArchiveInspectorForTesting: ApkArchiveInspector? = null

  @Volatile
  var customInstalledIdentityForTesting: InstalledAppIdentity? = null

  fun clearTestingOverrides() {
    customArchiveInspectorForTesting = null
    customInstalledIdentityForTesting = null
  }

  /**
   * Computes streaming SHA-256 hex digest of a file using a 64 KB buffer so large APKs never cause OOM.
   */
  fun computeFileSha256Hex(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(64 * 1024)
    BufferedInputStream(FileInputStream(file)).use { input ->
      while (true) {
        val read = input.read(buffer)
        if (read <= 0) break
        digest.update(buffer, 0, read)
      }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
  }

  /**
   * Retrieves the installed application's package name, versionName, versionCode, signing certificate SHA-256,
   * and X.509 PublicKey.
   */
  fun getInstalledAppIdentity(context: Context): InstalledAppIdentity {
    customInstalledIdentityForTesting?.let { return it }

    return runCatching {
      val pm = context.packageManager
      val pkgName = context.packageName
      val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        PackageManager.GET_SIGNING_CERTIFICATES
      } else {
        @Suppress("DEPRECATION")
        PackageManager.GET_SIGNATURES
      }
      val pkgInfo = pm.getPackageInfo(pkgName, flags)
      val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        pkgInfo.longVersionCode
      } else {
        @Suppress("DEPRECATION")
        pkgInfo.versionCode.toLong()
      }
      val vName = pkgInfo.versionName ?: com.example.BuildConfig.VERSION_NAME
      val (certSha, pubKey) = extractSigningCertSha256AndKey(pkgInfo)
      InstalledAppIdentity(
        packageName = pkgName,
        versionName = vName,
        versionCode = vCode.coerceAtLeast(com.example.BuildConfig.VERSION_CODE.toLong()),
        signingCertSha256 = certSha ?: "",
        publicKey = pubKey
      )
    }.getOrElse {
      InstalledAppIdentity(
        packageName = AppUpdaterConstants.EXPECTED_APPLICATION_ID,
        versionName = com.example.BuildConfig.VERSION_NAME,
        versionCode = com.example.BuildConfig.VERSION_CODE.toLong(),
        signingCertSha256 = "",
        publicKey = null
      )
    }
  }

  /**
   * Extracts the primary signing certificate's SHA-256 hex digest and X.509 PublicKey from a `PackageInfo`.
   */
  fun extractSigningCertSha256AndKey(pkgInfo: PackageInfo): Pair<String?, PublicKey?> {
    val rawCertBytes: ByteArray? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      val signingInfo = pkgInfo.signingInfo
      val signers = if (signingInfo != null) {
        if (signingInfo.hasMultipleSigners()) {
          signingInfo.apkContentsSigners
        } else {
          signingInfo.signingCertificateHistory
        }
      } else {
        null
      }
      signers?.firstOrNull()?.toByteArray()
    } else {
      @Suppress("DEPRECATION")
      pkgInfo.signatures?.firstOrNull()?.toByteArray()
    }

    if (rawCertBytes == null || rawCertBytes.isEmpty()) {
      return null to null
    }
    val certSha256 = UpdateManifestVerifier.sha256Hex(rawCertBytes)
    val publicKey = runCatching {
      val certFactory = CertificateFactory.getInstance("X.509")
      val x509 = certFactory.generateCertificate(ByteArrayInputStream(rawCertBytes)) as? X509Certificate
      x509?.publicKey
    }.getOrNull()
    return certSha256 to publicKey
  }

  /**
   * Inspects the first bytes of the file to reject scripts (`#!`), ELF binaries, Windows PE (`MZ`),
   * raw Dalvik DEX (`dex\n`), or HTML error pages disguised as APKs, and ensures the file starts
   * with the standard ZIP/APK magic header `PK\u0003\u0004` (`50 4B 03 04`) and contains
   * `AndroidManifest.xml` and `classes.dex`.
   */
  fun inspectFileMagicAndApkZipStructure(apkFile: File): ApkVerificationFailureReason? {
    if (!apkFile.exists() || !apkFile.isFile || apkFile.length() < 64L) {
      return ApkVerificationFailureReason.FILE_MISSING_OR_EMPTY
    }

    val header = ByteArray(64)
    val bytesRead = runCatching {
      FileInputStream(apkFile).use { it.read(header) }
    }.getOrDefault(-1)
    if (bytesRead < 4) {
      return ApkVerificationFailureReason.FILE_MISSING_OR_EMPTY
    }

    // Reject shell script "#!"
    if (header[0] == 0x23.toByte() && header[1] == 0x21.toByte()) {
      return ApkVerificationFailureReason.DANGEROUS_SCRIPT_OR_EXECUTABLE
    }
    // Reject Windows PE "MZ"
    if (header[0] == 0x4D.toByte() && header[1] == 0x5A.toByte()) {
      return ApkVerificationFailureReason.DANGEROUS_SCRIPT_OR_EXECUTABLE
    }
    // Reject Linux ELF "\u007FELF"
    if (header[0] == 0x7F.toByte() &&
      header[1] == 0x45.toByte() &&
      header[2] == 0x4C.toByte() &&
      header[3] == 0x46.toByte()
    ) {
      return ApkVerificationFailureReason.DANGEROUS_SCRIPT_OR_EXECUTABLE
    }
    // Reject raw DEX "dex\n"
    if (header[0] == 0x64.toByte() &&
      header[1] == 0x65.toByte() &&
      header[2] == 0x78.toByte() &&
      header[3] == 0x0A.toByte()
    ) {
      return ApkVerificationFailureReason.DANGEROUS_SCRIPT_OR_EXECUTABLE
    }
    // Reject HTML / XML / Script payloads
    val asciiPrefix = String(header, 0, bytesRead, StandardCharsets.UTF_8)
      .trimStart()
      .lowercase(Locale.ROOT)
    if (asciiPrefix.startsWith("<!doctype") ||
      asciiPrefix.startsWith("<html") ||
      asciiPrefix.startsWith("<script") ||
      asciiPrefix.startsWith("<?xml")
    ) {
      return ApkVerificationFailureReason.DANGEROUS_SCRIPT_OR_EXECUTABLE
    }

    // Must start with PK\u0003\u0004 (50 4B 03 04)
    val isZipHeader = header[0] == 0x50.toByte() &&
      header[1] == 0x4B.toByte() &&
      header[2] == 0x03.toByte() &&
      header[3] == 0x04.toByte()
    if (!isZipHeader) {
      return ApkVerificationFailureReason.INVALID_ZIP_OR_APK_MAGIC
    }

    // Verify valid ZIP entries: must contain AndroidManifest.xml and classes.dex, and no zip-slip ("..")
    return try {
      ZipFile(apkFile).use { zip ->
        var hasManifest = false
        var hasClassesDex = false
        val entries = zip.entries()
        while (entries.hasMoreElements()) {
          val entry = entries.nextElement()
          val name = entry.name
          if (name.contains("..") || name.startsWith("/") || name.startsWith("\\")) {
            return ApkVerificationFailureReason.INVALID_ZIP_OR_APK_MAGIC
          }
          if (name == "AndroidManifest.xml") hasManifest = true
          if (name == "classes.dex") hasClassesDex = true
        }
        if (!hasManifest || !hasClassesDex) {
          ApkVerificationFailureReason.INVALID_ZIP_OR_APK_MAGIC
        } else {
          null
        }
      }
    } catch (_: Exception) {
      ApkVerificationFailureReason.INVALID_ZIP_OR_APK_MAGIC
    }
  }

  /**
   * Inspects an APK file on disk using Android `PackageManager.getPackageArchiveInfo` (or the test
   * inspector when running under local JVM unit tests).
   */
  fun inspectApkArchive(context: Context, apkFile: File): ParsedApkMetadata? {
    customArchiveInspectorForTesting?.let { inspector ->
      return inspector.inspectArchive(context, apkFile)
    }

    return runCatching {
      val pm = context.packageManager
      val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        PackageManager.GET_SIGNING_CERTIFICATES
      } else {
        @Suppress("DEPRECATION")
        PackageManager.GET_SIGNATURES
      }
      val archiveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        pm.getPackageArchiveInfo(
          apkFile.absolutePath,
          PackageManager.PackageInfoFlags.of(flags.toLong())
        )
      } else {
        @Suppress("DEPRECATION")
        pm.getPackageArchiveInfo(apkFile.absolutePath, flags)
      } ?: return null

      val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        archiveInfo.longVersionCode
      } else {
        @Suppress("DEPRECATION")
        archiveInfo.versionCode.toLong()
      }
      val (certSha, pubKey) = extractSigningCertSha256AndKey(archiveInfo)
      ParsedApkMetadata(
        packageName = archiveInfo.packageName ?: "",
        versionName = archiveInfo.versionName ?: "",
        versionCode = vCode,
        signingCertSha256 = certSha,
        publicKey = pubKey
      )
    }.getOrNull()
  }

  /**
   * Runs the complete 7-step pre-installation security verification pipeline on `apkFile`.
   */
  fun verifyDownloadedApk(
    context: Context,
    apkFile: File,
    signedManifest: SignedUpdateManifest?,
    expectedVersionCode: Long? = null
  ): ApkVerificationResult {
    val installed = getInstalledAppIdentity(context)

    // Step 1 & 2: File presence, anti-script/anti-executable header check, and APK ZIP structure check
    val structureFailure = inspectFileMagicAndApkZipStructure(apkFile)
    if (structureFailure != null) {
      return ApkVerificationResult(
        isValid = false,
        installedSigningCertSha256 = installed.signingCertSha256,
        failureReason = structureFailure,
        errorMessage = structureFailure.userFriendlyReason
      )
    }

    // Step 3: Verify signed update.json metadata signature
    if (signedManifest == null) {
      return ApkVerificationResult(
        isValid = false,
        installedSigningCertSha256 = installed.signingCertSha256,
        manifestSignatureVerified = false,
        failureReason = ApkVerificationFailureReason.UNTRUSTED_OR_UNSIGNED_MANIFEST,
        errorMessage = ApkVerificationFailureReason.UNTRUSTED_OR_UNSIGNED_MANIFEST.userFriendlyReason
      )
    }

    val manifestSignatureValid = UpdateManifestVerifier.verifyManifestSignature(
      manifest = signedManifest,
      installedAppPublicKey = installed.publicKey,
      installedSigningCertSha256 = installed.signingCertSha256,
      expectedPackageName = installed.packageName
    )
    if (!manifestSignatureValid) {
      return ApkVerificationResult(
        isValid = false,
        expectedSha256 = signedManifest.sha256,
        installedSigningCertSha256 = installed.signingCertSha256,
        manifestSignatureVerified = false,
        failureReason = ApkVerificationFailureReason.UNTRUSTED_OR_UNSIGNED_MANIFEST,
        errorMessage = ApkVerificationFailureReason.UNTRUSTED_OR_UNSIGNED_MANIFEST.userFriendlyReason
      )
    }

    // Step 4: Compute streaming SHA-256 of the downloaded APK and compare in constant time
    val actualSha256 = runCatching { computeFileSha256Hex(apkFile) }.getOrDefault("")
    val expectedSha256 = signedManifest.sha256.trim().lowercase(Locale.ROOT)
    val shaMatches = actualSha256.length == 64 && MessageDigest.isEqual(
      actualSha256.lowercase(Locale.ROOT).toByteArray(StandardCharsets.UTF_8),
      expectedSha256.toByteArray(StandardCharsets.UTF_8)
    )
    if (!shaMatches) {
      return ApkVerificationResult(
        isValid = false,
        actualSha256 = actualSha256,
        expectedSha256 = expectedSha256,
        installedSigningCertSha256 = installed.signingCertSha256,
        manifestSignatureVerified = true,
        failureReason = ApkVerificationFailureReason.SHA256_MISMATCH,
        errorMessage = ApkVerificationFailureReason.SHA256_MISMATCH.userFriendlyReason
      )
    }

    // Step 5: Parse APK archive metadata (package name, versionCode, signing certificate)
    val archiveMeta = inspectApkArchive(context, apkFile)
      ?: return ApkVerificationResult(
        isValid = false,
        actualSha256 = actualSha256,
        expectedSha256 = expectedSha256,
        installedSigningCertSha256 = installed.signingCertSha256,
        manifestSignatureVerified = true,
        failureReason = ApkVerificationFailureReason.UNSIGNED_OR_CORRUPTED_APK,
        errorMessage = ApkVerificationFailureReason.UNSIGNED_OR_CORRUPTED_APK.userFriendlyReason
      )

    if (archiveMeta.packageName != installed.packageName) {
      return ApkVerificationResult(
        isValid = false,
        packageName = archiveMeta.packageName,
        versionName = archiveMeta.versionName,
        versionCode = archiveMeta.versionCode,
        actualSha256 = actualSha256,
        expectedSha256 = expectedSha256,
        installedSigningCertSha256 = installed.signingCertSha256,
        apkSigningCertSha256 = archiveMeta.signingCertSha256,
        manifestSignatureVerified = true,
        failureReason = ApkVerificationFailureReason.PACKAGE_NAME_MISMATCH,
        errorMessage = "${ApkVerificationFailureReason.PACKAGE_NAME_MISMATCH.userFriendlyReason} (Found: ${archiveMeta.packageName})"
      )
    }

    // Step 6: Reject version downgrades or non-increasing version codes
    val targetMinVersionCode = expectedVersionCode ?: signedManifest.versionCode
    if (archiveMeta.versionCode <= installed.versionCode || archiveMeta.versionCode < targetMinVersionCode) {
      return ApkVerificationResult(
        isValid = false,
        packageName = archiveMeta.packageName,
        versionName = archiveMeta.versionName,
        versionCode = archiveMeta.versionCode,
        actualSha256 = actualSha256,
        expectedSha256 = expectedSha256,
        installedSigningCertSha256 = installed.signingCertSha256,
        apkSigningCertSha256 = archiveMeta.signingCertSha256,
        manifestSignatureVerified = true,
        failureReason = ApkVerificationFailureReason.VERSION_DOWNGRADE_REJECTED,
        errorMessage = "${ApkVerificationFailureReason.VERSION_DOWNGRADE_REJECTED.userFriendlyReason} (Installed: ${installed.versionCode}, APK: ${archiveMeta.versionCode})"
      )
    }

    // Step 7: Verify APK X.509 signing certificate matches installed application's signing certificate
    val apkCertSha = archiveMeta.signingCertSha256?.trim()?.lowercase(Locale.ROOT) ?: ""
    if (apkCertSha.isEmpty()) {
      return ApkVerificationResult(
        isValid = false,
        packageName = archiveMeta.packageName,
        versionName = archiveMeta.versionName,
        versionCode = archiveMeta.versionCode,
        actualSha256 = actualSha256,
        expectedSha256 = expectedSha256,
        installedSigningCertSha256 = installed.signingCertSha256,
        apkSigningCertSha256 = null,
        manifestSignatureVerified = true,
        failureReason = ApkVerificationFailureReason.UNSIGNED_OR_CORRUPTED_APK,
        errorMessage = ApkVerificationFailureReason.UNSIGNED_OR_CORRUPTED_APK.userFriendlyReason
      )
    }

    val expectedCertSha = installed.signingCertSha256.ifBlank { signedManifest.signingCertSha256 }.trim().lowercase(Locale.ROOT)
    val certMatches = expectedCertSha.isNotEmpty() && MessageDigest.isEqual(
      apkCertSha.toByteArray(StandardCharsets.UTF_8),
      expectedCertSha.toByteArray(StandardCharsets.UTF_8)
    )
    if (!certMatches) {
      return ApkVerificationResult(
        isValid = false,
        packageName = archiveMeta.packageName,
        versionName = archiveMeta.versionName,
        versionCode = archiveMeta.versionCode,
        actualSha256 = actualSha256,
        expectedSha256 = expectedSha256,
        installedSigningCertSha256 = installed.signingCertSha256,
        apkSigningCertSha256 = apkCertSha,
        manifestSignatureVerified = true,
        failureReason = ApkVerificationFailureReason.SIGNING_CERTIFICATE_MISMATCH,
        errorMessage = ApkVerificationFailureReason.SIGNING_CERTIFICATE_MISMATCH.userFriendlyReason
      )
    }

    return ApkVerificationResult(
      isValid = true,
      packageName = archiveMeta.packageName,
      versionName = archiveMeta.versionName,
      versionCode = archiveMeta.versionCode,
      actualSha256 = actualSha256,
      expectedSha256 = expectedSha256,
      installedSigningCertSha256 = installed.signingCertSha256,
      apkSigningCertSha256 = apkCertSha,
      manifestSignatureVerified = true,
      failureReason = null,
      errorMessage = null
    )
  }
}
