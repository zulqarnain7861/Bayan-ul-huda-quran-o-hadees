package com.example.net

import android.content.Context
import com.example.data.remote.SecurityIntegrityGuard
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.security.MessageDigest
import java.util.Locale

/**
 * Runtime Delta Index Synchroniser for Section A (`mirza_lectures_v3.json`) per Spec §6.3 & §9.
 *
 * Security & Integrity Guarantees:
 * - HTTPS-only allowlisted endpoints (`SecurityIntegrityGuard.isUrlTrustedAndAllowed`).
 * - Conditional HTTP requests (`If-None-Match` ETag / `If-Modified-Since`).
 * - Hard payload cap: `MAX_INDEX_BYTES = 2 * 1024 * 1024` (2 MiB).
 * - Mandatory SHA-256 verification before any atomic file replacement.
 * - On SHA-256 mismatch, oversize payload, or malformed JSON, discards the candidate bytes
 *   and keeps the bundled `assets/mirza_lectures_v3.json` active (verified by Test T11).
 */
object IndexSync {

  const val MAX_INDEX_BYTES: Int = 2 * 1024 * 1024 // 2 MiB hard cap
  const val CONNECT_TIMEOUT_MS: Int = 8_000
  const val READ_TIMEOUT_MS: Int = 10_000
  const val ACTIVE_SYNCED_INDEX_FILENAME = "mirza_lectures_synced_v3.json"
  private const val PREFS_NAME = "mirza_sync_prefs"
  private const val KEY_ETAG = "mirza_index_etag"
  private const val KEY_LAST_MODIFIED = "mirza_index_last_modified"
  private const val KEY_ACTIVE_SHA256 = "mirza_index_sha256"

  sealed class SyncResult {
    data class Updated(val recordCount: Int, val sha256Hex: String) : SyncResult()
    data object NotModified : SyncResult()
    data class Rejected(val reason: String, val fallbackToBundledAsset: Boolean = true) : SyncResult()
  }

  /**
   * Computes lowercase hex SHA-256 digest of a byte array.
   */
  fun sha256Hex(bytes: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    val sb = StringBuilder(digest.size * 2)
    for (b in digest) {
      sb.append(String.format(Locale.ROOT, "%02x", b))
    }
    return sb.toString()
  }

  /**
   * Verifies and applies a candidate delta/index payload against an expected SHA-256 digest.
   * Never overwrites the active index if the digest mismatches, size exceeds `MAX_INDEX_BYTES`,
   * or JSON schema validation fails (Test T11).
   */
  fun verifyAndApplyCandidateBytes(
    targetDir: File,
    candidateBytes: ByteArray,
    expectedSha256Hex: String
  ): SyncResult {
    if (candidateBytes.isEmpty()) {
      return SyncResult.Rejected("Empty index payload", fallbackToBundledAsset = true)
    }
    if (candidateBytes.size > MAX_INDEX_BYTES) {
      return SyncResult.Rejected(
        "Index payload (${candidateBytes.size} bytes) exceeds MAX_INDEX_BYTES ($MAX_INDEX_BYTES)",
        fallbackToBundledAsset = true
      )
    }

    val normalizedExpected = expectedSha256Hex.trim().lowercase(Locale.ROOT)
    val actualSha256 = sha256Hex(candidateBytes)
    if (normalizedExpected.length != 64 || actualSha256 != normalizedExpected) {
      return SyncResult.Rejected(
        "SHA-256 mismatch: expected=$normalizedExpected actual=$actualSha256",
        fallbackToBundledAsset = true
      )
    }

    val utf8Text = try {
      candidateBytes.toString(Charsets.UTF_8)
    } catch (e: Exception) {
      return SyncResult.Rejected("Invalid UTF-8 encoding", fallbackToBundledAsset = true)
    }

    if (SecurityIntegrityGuard.containsInappropriateOrAlteredPayload(utf8Text)) {
      return SyncResult.Rejected("Rejected script/HTML injection in payload", fallbackToBundledAsset = true)
    }

    if (!utf8Text.contains("\"records\"") || !utf8Text.contains("\"counts\"")) {
      return SyncResult.Rejected("Missing required schema keys ('counts', 'records')", fallbackToBundledAsset = true)
    }

    if (!targetDir.exists()) {
      targetDir.mkdirs()
    }
    val tempFile = File(targetDir, "$ACTIVE_SYNCED_INDEX_FILENAME.tmp")
    val finalFile = File(targetDir, ACTIVE_SYNCED_INDEX_FILENAME)
    return try {
      tempFile.writeBytes(candidateBytes)
      if (finalFile.exists()) {
        finalFile.delete()
      }
      val renamed = tempFile.renameTo(finalFile)
      if (!renamed) {
        tempFile.copyTo(finalFile, overwrite = true)
        tempFile.delete()
      }
      SyncResult.Updated(recordCount = -1, sha256Hex = actualSha256)
    } catch (e: Exception) {
      tempFile.delete()
      SyncResult.Rejected("Atomic write failed: ${e.message}", fallbackToBundledAsset = true)
    }
  }

  /**
   * Reads an InputStream up to `MAX_INDEX_BYTES`. Throws IllegalArgumentException if exceeded.
   */
  fun readBoundedBytes(input: InputStream, maxBytes: Int = MAX_INDEX_BYTES): ByteArray {
    val buffer = ByteArray(8192)
    val out = ByteArrayOutputStream()
    var total = 0
    while (true) {
      val read = input.read(buffer)
      if (read < 0) break
      total += read
      if (total > maxBytes) {
        throw IllegalArgumentException("Stream exceeded bound of $maxBytes bytes")
      }
      out.write(buffer, 0, read)
    }
    return out.toByteArray()
  }

  /**
   * Performs an HTTPS-only ETag / Last-Modified conditional sync against an allowlisted URL.
   */
  fun syncFromHttpsEndpoint(
    context: Context,
    endpointUrl: String,
    expectedSha256Hex: String
  ): SyncResult {
    val trimmed = endpointUrl.trim()
    val uri = try {
      URI(trimmed)
    } catch (_: Exception) {
      return SyncResult.Rejected("Malformed URL", fallbackToBundledAsset = true)
    }
    if (uri.scheme?.lowercase(Locale.ROOT) != "https" || !SecurityIntegrityGuard.isUrlTrustedAndAllowed(trimmed)) {
      return SyncResult.Rejected("Untrusted or non-HTTPS endpoint blocked", fallbackToBundledAsset = true)
    }

    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val cachedEtag = prefs.getString(KEY_ETAG, null)
    val cachedLastMod = prefs.getString(KEY_LAST_MODIFIED, null)

    var conn: HttpURLConnection? = null
    return try {
      conn = (URL(trimmed).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = CONNECT_TIMEOUT_MS
        readTimeout = READ_TIMEOUT_MS
        instanceFollowRedirects = false
        setRequestProperty("Accept", "application/json")
        if (!cachedEtag.isNullOrBlank()) {
          setRequestProperty("If-None-Match", cachedEtag)
        }
        if (!cachedLastMod.isNullOrBlank()) {
          setRequestProperty("If-Modified-Since", cachedLastMod)
        }
      }

      val status = conn.responseCode
      if (status == HttpURLConnection.HTTP_NOT_MODIFIED) {
        return SyncResult.NotModified
      }
      if (status != HttpURLConnection.HTTP_OK) {
        return SyncResult.Rejected("HTTP $status", fallbackToBundledAsset = true)
      }

      val contentLength = conn.contentLengthLong
      if (contentLength > MAX_INDEX_BYTES) {
        return SyncResult.Rejected("Content-Length $contentLength exceeds $MAX_INDEX_BYTES", fallbackToBundledAsset = true)
      }

      val bytes = conn.inputStream.use { readBoundedBytes(it, MAX_INDEX_BYTES) }
      val result = verifyAndApplyCandidateBytes(context.filesDir, bytes, expectedSha256Hex)
      if (result is SyncResult.Updated) {
        prefs.edit()
          .putString(KEY_ETAG, conn.getHeaderField("ETag"))
          .putString(KEY_LAST_MODIFIED, conn.getHeaderField("Last-Modified"))
          .putString(KEY_ACTIVE_SHA256, result.sha256Hex)
          .apply()
      }
      result
    } catch (e: Exception) {
      SyncResult.Rejected("Network/Verification error: ${e.message}", fallbackToBundledAsset = true)
    } finally {
      conn?.disconnect()
    }
  }
}
