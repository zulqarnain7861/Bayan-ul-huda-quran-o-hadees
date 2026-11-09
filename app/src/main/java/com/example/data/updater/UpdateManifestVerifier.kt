package com.example.data.updater

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Locale

/**
 * Cryptographic signer and verifier for `update.json` metadata (`SignedUpdateManifest`).
 *
 * Security Model:
 * - Prevents relying solely on an unsigned checksum hosted alongside an APK.
 * - The canonical update metadata payload (package name, version, version_code, minimum_version_code,
 *   download_url, sha256, force_update, signing_cert_sha256) is signed using the developer's private
 *   release signing key (`SHA256withRSA` or `SHA256withECDSA`).
 * - At runtime, the app verifies:
 *   1. `manifest.packageName` == installed application ID (`com.bayanulhuda.com`)
 *   2. `manifest.signingCertSha256` matches the installed application's X.509 signing certificate SHA-256
 *      (or the pinned trusted release public key).
 *   3. `manifest.signatureBase64` is a valid cryptographic signature over `manifest.toCanonicalSigningPayload()`
 *      using the installed application's own X.509 `PublicKey` (or the verified public key matching the
 *      installed signing certificate).
 */
object UpdateManifestVerifier {

  /**
   * Computes lowercase 64-character hex SHA-256 digest of a byte array.
   */
  fun sha256Hex(bytes: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return digest.joinToString("") { "%02x".format(it) }
  }

  /**
   * Signs a `SignedUpdateManifest` using the provided `PrivateKey` and returns a copy populated
   * with `signatureBase64` and `publicKeyDerBase64`.
   */
  fun signManifest(
    manifest: SignedUpdateManifest,
    privateKey: PrivateKey,
    publicKey: PublicKey,
    signingCertSha256: String,
    algorithm: String = "SHA256withRSA"
  ): SignedUpdateManifest {
    val normalizedCertSha = signingCertSha256.trim().lowercase(Locale.ROOT)
    val unsignedWithCert = manifest.copy(
      signingCertSha256 = normalizedCertSha,
      publicKeyDerBase64 = Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP),
      signatureAlgorithm = algorithm
    )
    val payloadBytes = unsignedWithCert.toCanonicalSigningPayload().toByteArray(StandardCharsets.UTF_8)
    val signer = Signature.getInstance(algorithm)
    signer.initSign(privateKey)
    signer.update(payloadBytes)
    val sigBytes = signer.sign()
    return unsignedWithCert.copy(
      signatureBase64 = Base64.encodeToString(sigBytes, Base64.NO_WRAP)
    )
  }

  /**
   * Verifies that `manifest` is cryptographically signed by the trusted key corresponding to
   * the installed application's signing certificate.
   */
  fun verifyManifestSignature(
    manifest: SignedUpdateManifest,
    installedAppPublicKey: PublicKey?,
    installedSigningCertSha256: String?,
    expectedPackageName: String = AppUpdaterConstants.EXPECTED_APPLICATION_ID
  ): Boolean {
    if (manifest.packageName.trim() != expectedPackageName.trim()) {
      return false
    }
    if (manifest.version.isBlank() || manifest.versionCode <= 0L) {
      return false
    }
    if (manifest.sha256.trim().length != 64) {
      return false
    }
    if (!manifest.downloadUrl.startsWith("https://")) {
      return false
    }
    if (manifest.signatureBase64.isBlank()) {
      return false
    }

    // Verify certificate SHA-256 binding when installedSigningCertSha256 is present
    if (!installedSigningCertSha256.isNullOrBlank()) {
      if (manifest.signingCertSha256.isBlank()) {
        return false
      }
      val certMatches = MessageDigest.isEqual(
        manifest.signingCertSha256.trim().lowercase(Locale.ROOT).toByteArray(StandardCharsets.UTF_8),
        installedSigningCertSha256.trim().lowercase(Locale.ROOT).toByteArray(StandardCharsets.UTF_8)
      )
      if (!certMatches) {
        return false
      }
    }

    val candidatePublicKey: PublicKey = installedAppPublicKey ?: run {
      if (manifest.publicKeyDerBase64.isBlank()) return false
      decodePublicKeyDerBase64(manifest.publicKeyDerBase64, manifest.signatureAlgorithm) ?: return false
    }

    // If both installedAppPublicKey and manifest.publicKeyDerBase64 are present, ensure they match
    if (installedAppPublicKey != null && manifest.publicKeyDerBase64.isNotBlank()) {
      val manifestKeyBytes = runCatching { Base64.decode(manifest.publicKeyDerBase64, Base64.DEFAULT) }.getOrNull()
      if (manifestKeyBytes == null || !MessageDigest.isEqual(installedAppPublicKey.encoded, manifestKeyBytes)) {
        return false
      }
    }

    return runCatching {
      val sigBytes = Base64.decode(manifest.signatureBase64.trim(), Base64.DEFAULT)
      if (sigBytes.isEmpty()) return@runCatching false
      val payloadBytes = manifest.toCanonicalSigningPayload().toByteArray(StandardCharsets.UTF_8)
      val algorithm = manifest.signatureAlgorithm.ifBlank { "SHA256withRSA" }
      val verifier = Signature.getInstance(algorithm)
      verifier.initVerify(candidatePublicKey)
      verifier.update(payloadBytes)
      verifier.verify(sigBytes)
    }.getOrDefault(false)
  }

  fun decodePublicKeyDerBase64(publicKeyDerBase64: String, signatureAlgorithm: String = "SHA256withRSA"): PublicKey? {
    return runCatching {
      val keyBytes = Base64.decode(publicKeyDerBase64.trim(), Base64.DEFAULT)
      val keyAlg = when {
        signatureAlgorithm.contains("ECDSA", ignoreCase = true) || signatureAlgorithm.contains("EC", ignoreCase = true) -> "EC"
        else -> "RSA"
      }
      KeyFactory.getInstance(keyAlg).generatePublic(X509EncodedKeySpec(keyBytes))
    }.getOrNull()
  }
}
