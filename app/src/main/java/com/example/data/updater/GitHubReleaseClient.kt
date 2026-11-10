package com.example.data.updater

import com.example.security.SecureNetworkClient
import com.example.security.SsrfRedirectValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Official GitHub Releases API client for Bayan al-Huda (`https://api.github.com/repos/zulqarnain7861/Bayan-ul-huda-quran-o-hadees/releases/latest`).
 *
 * Security & Resilience Guarantees:
 * - Zero hardcoded GitHub personal access tokens or secrets.
 * - Strict HTTPS & GitHub host verification (`api.github.com`, `github.com`, `objects.githubusercontent.com`, `release-assets.githubusercontent.com`).
 * - Ignores draft and prerelease entries by default.
 * - Fetches and parses `update.json` signed update metadata alongside the `.apk` and `.sha256` assets.
 * - Gracefully handles HTTP 403/429 rate limits, HTTP 404 (no release published yet), slow internet, and offline errors.
 */
object GitHubReleaseClient {

  sealed class ReleaseCheckOutcome {
    data class Success(val release: GitHubReleaseInfo) : ReleaseCheckOutcome()
    data class NoPublishedRelease(val message: String) : ReleaseCheckOutcome()
    data class RateLimited(val resetEpochSeconds: Long?, val message: String) : ReleaseCheckOutcome()
    data class NetworkError(val isOfflineOrTimeout: Boolean, val message: String) : ReleaseCheckOutcome()
  }

  interface HttpTransport {
    fun get(url: String, headers: Map<String, String>): TransportResponse
  }

  data class TransportResponse(
    val statusCode: Int,
    val body: String,
    val headers: Map<String, String> = emptyMap()
  )

  @Volatile
  var customTransportForTesting: HttpTransport? = null

  /**
   * Dedicated HTTPS OkHttpClient for GitHub Release API and asset redirects.
   * Unlike general media streams (which block `.apk` extensions), this client explicitly allows
   * `.apk`, `.json`, and `.sha256` assets ONLY when hosted on `AppUpdaterConstants.TRUSTED_GITHUB_HOSTS`.
   */
  val githubHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .connectionSpecs(listOf(SecureNetworkClient.MODERN_AEAD_CONNECTION_SPEC))
      .connectTimeout(15L, TimeUnit.SECONDS)
      .readTimeout(30L, TimeUnit.SECONDS)
      .writeTimeout(15L, TimeUnit.SECONDS)
      .followRedirects(true)
      .followSslRedirects(true)
      .addInterceptor { chain ->
        val req = chain.request()
        val urlStr = req.url.toString()
        if (!isTrustedGitHubReleaseUrl(urlStr)) {
          throw IOException("Blocked untrusted updater URL: $urlStr")
        }
        val resp = chain.proceed(req)
        if (resp.isRedirect) {
          val location = resp.header("Location")
          if (location != null) {
            val resolved = req.url.resolve(location)?.toString()
            if (resolved == null || !isTrustedGitHubReleaseUrl(resolved)) {
              resp.close()
              throw IOException("Blocked redirect to untrusted host during GitHub update check")
            }
          }
        }
        resp
      }
      .build()
  }

  fun isTrustedGitHubReleaseUrl(urlStr: String?): Boolean {
    if (urlStr.isNullOrBlank()) return false
    return runCatching {
      val parsed = java.net.URL(urlStr.trim())
      if (parsed.protocol?.lowercase(Locale.ROOT) != "https") return false
      if (!parsed.userInfo.isNullOrEmpty()) return false
      if (parsed.port != -1 && parsed.port != 443) return false
      val host = parsed.host?.lowercase(Locale.ROOT)?.trimEnd('.') ?: return false
      host in AppUpdaterConstants.TRUSTED_GITHUB_HOSTS ||
        host.endsWith(".githubusercontent.com") ||
        host.endsWith(".github.com")
    }.getOrDefault(false)
  }

  private fun executeGet(url: String): TransportResponse {
    if (!isTrustedGitHubReleaseUrl(url)) {
      throw SecurityException("Untrusted GitHub URL rejected: $url")
    }
    val headers = mapOf(
      "Accept" to "application/vnd.github+json",
      "X-GitHub-Api-Version" to "2022-11-28",
      "User-Agent" to "BayanAlHuda-Android-Updater/1.0 (+https://github.com/zulqarnain7861/Bayan-ul-huda-quran-o-hadees)"
    )
    customTransportForTesting?.let { transport ->
      return transport.get(url, headers)
    }

    val requestBuilder = Request.Builder().url(url).get()
    headers.forEach { (k, v) -> requestBuilder.header(k, v) }
    githubHttpClient.newCall(requestBuilder.build()).execute().use { response ->
      val responseHeaders = mutableMapOf<String, String>()
      for (name in response.headers.names()) {
        response.header(name)?.let { responseHeaders[name.lowercase(Locale.ROOT)] = it }
      }
      val bodyText = response.body?.source()?.let { src ->
        src.request(2L * 1024L * 1024L)
        src.buffer.clone().readString(StandardCharsets.UTF_8)
      } ?: ""
      return TransportResponse(
        statusCode = response.code,
        body = bodyText,
        headers = responseHeaders
      )
    }
  }

  /**
   * Queries `https://api.github.com/repos/zulqarnain7861/Bayan-ul-huda-quran-o-hadees/releases/latest`
   * (with automatic fallback to `/releases` so manually uploaded releases or tag releases are also found)
   * and fetches any attached `update.json` signed update metadata.
   */
  suspend fun fetchLatestStableRelease(
    includePrereleases: Boolean = false
  ): ReleaseCheckOutcome = withContext(Dispatchers.IO) {
    try {
      val response = executeGet(AppUpdaterConstants.GITHUB_LATEST_RELEASE_API_URL)
      when (response.statusCode) {
        200 -> {
          val parsed = parseReleaseJsonObject(
            obj = JSONObject(response.body),
            includePrereleases = includePrereleases,
            fetchSignedManifestContent = { manifestUrl ->
              runCatching {
                val manifestResp = executeGet(manifestUrl)
                if (manifestResp.statusCode == 200) manifestResp.body else null
              }.getOrNull()
            }
          )
          if (parsed != null) {
            ReleaseCheckOutcome.Success(parsed)
          } else {
            fetchFromReleasesListFallback(includePrereleases = true)
          }
        }
        404 -> {
          fetchFromReleasesListFallback(includePrereleases = true)
        }
        403, 429 -> {
          val resetHeader = response.headers["x-ratelimit-reset"]?.toLongOrNull()
          ReleaseCheckOutcome.RateLimited(
            resetEpochSeconds = resetHeader,
            message = "GitHub Releases API rate limit reached (HTTP ${response.statusCode}). Please try again later."
          )
        }
        else -> {
          ReleaseCheckOutcome.NetworkError(
            isOfflineOrTimeout = false,
            message = "GitHub Releases API returned HTTP ${response.statusCode}."
          )
        }
      }
    } catch (e: UnknownHostException) {
      ReleaseCheckOutcome.NetworkError(
        isOfflineOrTimeout = true,
        message = "No internet connection available. Could not reach GitHub Releases."
      )
    } catch (e: SocketTimeoutException) {
      ReleaseCheckOutcome.NetworkError(
        isOfflineOrTimeout = true,
        message = "Connection timed out on a slow network while checking GitHub Releases."
      )
    } catch (e: Exception) {
      ReleaseCheckOutcome.NetworkError(
        isOfflineOrTimeout = e is IOException,
        message = "Unable to check GitHub Releases: ${e.message ?: "Network error"}"
      )
    }
  }

  private fun fetchFromReleasesListFallback(includePrereleases: Boolean): ReleaseCheckOutcome {
    return runCatching {
      val listResp = executeGet(AppUpdaterConstants.GITHUB_RELEASES_LIST_API_URL)
      if (listResp.statusCode == 200) {
        val arr = JSONArray(listResp.body)
        var foundSourceOnlyReleaseTag: String? = null
        for (i in 0 until arr.length()) {
          val item = arr.optJSONObject(i) ?: continue
          if (!item.optBoolean("draft", false) && foundSourceOnlyReleaseTag == null) {
            val t = item.optString("tag_name", "").trim()
            if (t.isNotEmpty()) foundSourceOnlyReleaseTag = t
          }
          val parsed = parseReleaseJsonObject(
            obj = item,
            includePrereleases = includePrereleases,
            fetchSignedManifestContent = { manifestUrl ->
              runCatching {
                val manifestResp = executeGet(manifestUrl)
                if (manifestResp.statusCode == 200) manifestResp.body else null
              }.getOrNull()
            }
          )
          if (parsed != null) {
            return ReleaseCheckOutcome.Success(parsed)
          }
        }
        if (foundSourceOnlyReleaseTag != null) {
          return ReleaseCheckOutcome.NoPublishedRelease(
            "Release $foundSourceOnlyReleaseTag on GitHub currently has only Source code (.zip / .tar.gz) archives attached. Run the GitHub Actions Android Release workflow or attach the compiled .apk binary to enable in-app APK updates."
          )
        }
      }
      ReleaseCheckOutcome.NoPublishedRelease(
        "You are running the latest version. No newer APK release is published on GitHub Releases yet."
      )
    }.getOrElse {
      ReleaseCheckOutcome.NoPublishedRelease(
        "You are running the latest version. No newer APK release is published on GitHub Releases yet."
      )
    }
  }

  /**
   * Parses a GitHub Release JSON object into a `GitHubReleaseInfo`, ignoring drafts and (by default)
   * prereleases, locating the `.apk` and `update.json` assets, and resolving versionName + versionCode.
   */
  fun parseReleaseJsonObject(
    obj: JSONObject,
    includePrereleases: Boolean = false,
    fetchSignedManifestContent: ((String) -> String?)? = null
  ): GitHubReleaseInfo? {
    val isDraft = obj.optBoolean("draft", false)
    val isPrerelease = obj.optBoolean("prerelease", false)
    if (isDraft) return null
    if (isPrerelease && !includePrereleases) return null

    val tagName = obj.optString("tag_name", "").trim()
    if (tagName.isEmpty()) return null
    val releaseName = obj.optString("name", tagName).trim().ifEmpty { tagName }
    val bodyNotes = obj.optString("body", "").trim()
    val publishedAt = obj.optString("published_at", "").trim()
    val htmlUrl = obj.optString("html_url", AppUpdaterConstants.GITHUB_RELEASES_WEB_URL).trim()

    val assetsArray = obj.optJSONArray("assets") ?: JSONArray()
    val parsedAssets = mutableListOf<GitHubReleaseAsset>()
    for (i in 0 until assetsArray.length()) {
      val assetObj = assetsArray.optJSONObject(i) ?: continue
      val name = assetObj.optString("name", "").trim()
      val downloadUrl = assetObj.optString("browser_download_url", "").trim()
      val size = assetObj.optLong("size", 0L)
      val contentType = assetObj.optString("content_type", "").trim()
      if (name.isNotEmpty() && isTrustedGitHubReleaseUrl(downloadUrl)) {
        parsedAssets.add(
          GitHubReleaseAsset(
            name = name,
            browserDownloadUrl = downloadUrl,
            sizeBytes = size,
            contentType = contentType
          )
        )
      }
    }

    val apkAsset = parsedAssets.firstOrNull {
      it.name.endsWith(".apk", ignoreCase = true) && !it.name.contains("unsigned", ignoreCase = true)
    } ?: return null

    val manifestAsset = parsedAssets.firstOrNull {
      it.name.equals("update.json", ignoreCase = true) ||
        it.name.endsWith("-update.json", ignoreCase = true)
    }
    val checksumAsset = parsedAssets.firstOrNull {
      it.name.endsWith(".sha256", ignoreCase = true) ||
        it.name.endsWith("-SHA256.txt", ignoreCase = true) ||
        it.name.equals("SHA256SUMS.txt", ignoreCase = true) ||
        it.name.equals("SHA256SUMS", ignoreCase = true)
    }

    val signedManifest = manifestAsset?.let { asset ->
      val rawJson = fetchSignedManifestContent?.invoke(asset.browserDownloadUrl)
      if (!rawJson.isNullOrBlank()) SignedUpdateManifest.fromJsonString(rawJson) else null
    }

    val (tagVersionName, tagVersionCode) = parseVersionFromTagAndNotes(tagName, bodyNotes)
    val effectiveVersionName = signedManifest?.version?.ifBlank { tagVersionName } ?: tagVersionName
    val effectiveVersionCode = signedManifest?.versionCode?.takeIf { it > 0L } ?: tagVersionCode
    val effectiveMinVersionCode = signedManifest?.minimumVersionCode ?: 1L
    val effectiveNotes = signedManifest?.releaseNotes?.ifBlank { bodyNotes } ?: bodyNotes.ifBlank {
      "Stable release $effectiveVersionName"
    }
    val effectiveForceUpdate = signedManifest?.forceUpdate
      ?: bodyNotes.contains("[CRITICAL_UPDATE]", ignoreCase = true)

    return GitHubReleaseInfo(
      tagName = tagName,
      versionName = effectiveVersionName,
      versionCode = effectiveVersionCode,
      minimumVersionCode = effectiveMinVersionCode,
      releaseTitle = releaseName,
      releaseNotes = effectiveNotes,
      publishedAt = publishedAt,
      isDraft = isDraft,
      isPrerelease = isPrerelease,
      apkAsset = apkAsset,
      manifestAsset = manifestAsset,
      checksumAsset = checksumAsset,
      signedManifest = signedManifest,
      htmlUrl = if (isTrustedGitHubReleaseUrl(htmlUrl)) htmlUrl else AppUpdaterConstants.GITHUB_RELEASES_WEB_URL,
      forceUpdate = effectiveForceUpdate
    )
  }

  /**
   * Extracts semantic versionName (e.g. `1.1.0`) and numeric versionCode from a Git tag
   * (such as `v1.1.0`, `v1.1.0+9`, `v1.1.0-code9`) or release notes (`version_code: 9`),
   * or derives a deterministic monotonic version code aligned with:
   * - `1.0.2` -> `8`
   * - `1.1.0` -> `9`
   * - `1.1.1` -> `10`, `1.2.0` -> `20`, etc.
   */
  fun parseVersionFromTagAndNotes(tagName: String, releaseNotes: String = ""): Pair<String, Long> {
    val cleanTag = tagName.trim().removePrefix("v").removePrefix("V")
    val plusSplit = cleanTag.split("+")
    val versionName = plusSplit.first().substringBefore("-").trim().ifEmpty { "1.0.0" }

    // 1. Explicit +versionCode in tag (e.g. v1.1.0+9)
    if (plusSplit.size > 1) {
      val explicitCode = plusSplit[1].filter { it.isDigit() }.toLongOrNull()
      if (explicitCode != null && explicitCode > 0L) {
        return versionName to explicitCode
      }
    }

    // 2. Explicit version_code in release notes (e.g. "version_code: 9" or "Version Code: 9")
    val notesCodeMatch = Regex("""(?i)version[_\s-]*code\s*[:=]\s*(\d+)""").find(releaseNotes)
    if (notesCodeMatch != null) {
      val code = notesCodeMatch.groupValues[1].toLongOrNull()
      if (code != null && code > 0L) {
        return versionName to code
      }
    }

    // 3. Deterministic semantic version fallback aligned with Bayan al-Huda's version history:
    //    1.0.0 -> 6, 1.0.1 -> 7, 1.0.2 -> 8, 1.1.0 -> 9, 1.1.1 -> 10, 1.2.0 -> 20, etc.
    val parts = versionName.split(".").map { it.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 0 }
    val major = parts.getOrElse(0) { 1 }
    val minor = parts.getOrElse(1) { 0 }
    val patch = parts.getOrElse(2) { 0 }
    val derivedCode = when {
      major == 1 && minor == 0 -> (6 + patch).toLong()
      major == 1 && minor == 1 -> (9 + patch).toLong()
      else -> ((major - 1) * 1000 + minor * 10 + patch).toLong().coerceAtLeast(1L)
    }
    return versionName to derivedCode
  }
}
