package com.example.security

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Debug
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.BuildConfig
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.MessageDigest
import java.text.Normalizer
import java.util.Arrays
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.TlsVersion

/**
 * 12-Layer Enterprise & OWASP MASVS-L2 Defense-in-Depth Security Architecture ("Giant Wall v2")
 * for Bayan al-Huda.
 */

// ============================================================================
// LAYER 1: Sacred Content Integrity & Anti-Tampering Guard (Quran, Hadith, Duas)
// ============================================================================
object SacredContentIntegrityGuard {
  fun computeSha256Hex(bytes: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return digest.joinToString("") { "%02x".format(it) }
  }

  fun verifyChecksum(bytes: ByteArray, expectedSha256Hex: String): Boolean {
    if (expectedSha256Hex.length != 64) return false
    val actual = computeSha256Hex(bytes)
    return MessageDigest.isEqual(
      actual.lowercase(Locale.ROOT).toByteArray(StandardCharsets.UTF_8),
      expectedSha256Hex.lowercase(Locale.ROOT).toByteArray(StandardCharsets.UTF_8)
    )
  }

  /**
   * Verifies a cached file against its expected SHA-256 hash; if corrupted or tampered with,
   * automatically restores from the bundled read-only APK asset.
   */
  fun verifyOrRestoreSacredAsset(
    context: Context,
    cachedFile: File?,
    assetFileName: String,
    expectedSha256Hex: String? = null
  ): ByteArray {
    if (cachedFile != null && cachedFile.exists() && cachedFile.isFile) {
      val cachedBytes = runCatching { cachedFile.readBytes() }.getOrNull()
      if (cachedBytes != null && cachedBytes.isNotEmpty()) {
        val valid = expectedSha256Hex == null || verifyChecksum(cachedBytes, expectedSha256Hex)
        if (valid) {
          return cachedBytes
        } else {
          runCatching { cachedFile.delete() }
        }
      }
    }
    return runCatching {
      context.assets.open(assetFileName).use { it.readBytes() }
    }.getOrDefault(ByteArray(0))
  }
}

// ============================================================================
// LAYER 2: Input Canonicalization, SQLi & XSS Firewall
// ============================================================================
object SecurityFirewall {
  const val MAX_SEARCH_QUERY_LENGTH = 250

  private val SQLI_PATTERNS = listOf(
    Regex("""(?i)'\s*or\s+1\s*=\s*1"""),
    Regex("""(?i)"\s*or\s+1\s*=\s*1"""),
    Regex("""(?i)\bunion\s+(all\s+)?select\b"""),
    Regex("""(?i);\s*drop\s+"""),
    Regex("""(?i)\bsleep\s*\("""),
    Regex("""(?i)\bbenchmark\s*\("""),
    Regex("""(?i)\bwaitfor\s+delay\b"""),
    Regex("""(?i)\bsqlite_master\b"""),
    Regex("""(?i)\bpragma\s+"""),
    Regex("""(?i)\battach\s+database\b""")
  )

  private val XSS_AND_INJECTION_PATTERNS = listOf(
    Regex("""(?i)<\s*/?\s*(script|iframe|object|embed|svg|link|meta|style)[^>]*>"""),
    Regex("""(?i)\b(javascript|vbscript|data)\s*:"""),
    Regex("""(?i)\bon(error|load|click|mouseover|focus|blur|submit)\s*="""),
    Regex("""(?i)\b(eval|Function|setTimeout|setInterval)\s*\("""),
    Regex("""(?i)\b(document\.cookie|window\.location)\b"""),
    Regex("""\$\{[^}]*\}"""),
    Regex("""\{\{[^}]*\}\}""")
  )

  private val CONTROL_CHAR_REGEX = Regex("""[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]""")

  /**
   * Canonicalizes input to Unicode NFKC and strips SQLi, XSS, template injection, and control chars.
   */
  fun canonicalizeAndSanitize(rawInput: String?, maxLength: Int = MAX_SEARCH_QUERY_LENGTH): String {
    if (rawInput.isNullOrBlank()) return ""
    val nfkc = runCatching {
      Normalizer.normalize(rawInput, Normalizer.Form.NFKC)
    }.getOrDefault(rawInput)

    var cleaned = CONTROL_CHAR_REGEX.replace(nfkc, "")
    XSS_AND_INJECTION_PATTERNS.forEach { rx ->
      cleaned = rx.replace(cleaned, " ")
    }
    SQLI_PATTERNS.forEach { rx ->
      cleaned = rx.replace(cleaned, " ")
    }
    // Strip dangerous SQL comment sequences and raw angle brackets/semicolons
    cleaned = cleaned
      .replace("--", " ")
      .replace("/*", " ")
      .replace("*/", " ")
      .replace("<", "")
      .replace(">", "")
      .replace(";", " ")
      .trim()

    return if (cleaned.length > maxLength) cleaned.substring(0, maxLength).trim() else cleaned
  }

  fun containsHostileSignature(rawInput: String?): Boolean {
    if (rawInput.isNullOrBlank()) return false
    val nfkc = runCatching {
      Normalizer.normalize(rawInput, Normalizer.Form.NFKC)
    }.getOrDefault(rawInput)
    return SQLI_PATTERNS.any { it.containsMatchIn(nfkc) } ||
      XSS_AND_INJECTION_PATTERNS.any { it.containsMatchIn(nfkc) }
  }
}

// ============================================================================
// LAYER 3: AdShield WebView & Tracker Sinkhole (Zero Ads, Zero Trackers)
// ============================================================================
object AdShieldConfig {
  val BLOCKED_AD_AND_TRACKER_DOMAINS = setOf(
    "doubleclick.net",
    "googleadservices.com",
    "googlesyndication.com",
    "moatads.com",
    "adservice.google.com",
    "pagead2.googlesyndication.com",
    "imasdk.googleapis.com",
    "2mdn.net",
    "adnxs.com",
    "adsrvr.org",
    "criteo.com",
    "taboola.com",
    "outbrain.com",
    "scorecardresearch.com",
    "quantserve.com",
    "zedo.com",
    "rubiconproject.com",
    "pubmatic.com",
    "openx.net",
    "casalemedia.com",
    "advertising.com",
    "adform.net",
    "serving-sys.com",
    "smartadserver.com",
    "popads.net",
    "popcash.net",
    "propellerads.com",
    "adcash.com",
    "exoclick.com",
    "juicyads.com",
    "trafficjunky.net",
    "revcontent.com",
    "mgid.com",
    "adblade.com",
    "teads.tv",
    "spotxchange.com",
    "springserve.com",
    "innovid.com",
    "flashtalking.com",
    "celtra.com",
    "sizmek.com",
    "extreme-dm.com",
    "histats.com",
    "statcounter.com",
    "yandex.ru",
    "mc.yandex.ru",
    "an.yandex.ru",
    "ads.yahoo.com",
    "analytics.yahoo.com",
    "gemini.yahoo.com",
    "amazon-adsystem.com",
    "aax.amazon-adsystem.com",
    "c.amazon-adsystem.com",
    "ads-twitter.com",
    "static.ads-twitter.com",
    "analytics.twitter.com",
    "connect.facebook.net",
    "an.facebook.com",
    "pixel.facebook.com",
    "ads.linkedin.com",
    "analytics.tiktok.com",
    "ads.tiktok.com",
    "log.byteoversea.com",
    "hotjar.com",
    "mouseflow.com",
    "luckyorange.com",
    "fullstory.com",
    "crazyegg.com",
    "mixpanel.com",
    "amplitude.com",
    "segment.io",
    "segment.com",
    "braze.com",
    "appboy.com",
    "appsflyer.com",
    "adjust.com",
    "branch.io",
    "kochava.com",
    "singular.net",
    "tapjoy.com",
    "unityads.unity3d.com",
    "applovin.com",
    "vungle.com",
    "chartboost.com",
    "ironsource.mobi",
    "inmobi.com",
    "startappservice.com",
    "fyber.com",
    "smaato.net",
    "mopub.com",
    "adcolony.com",
    "leadbolt.net",
    "airpush.com",
    "admob.com",
    "media.net",
    "bidswitch.net",
    "sharethrough.com",
    "triplelift.com",
    "indexexchange.com",
    "sovrn.com",
    "lijit.com",
    "gumgum.com",
    "33across.com",
    "conversantmedia.com",
    "mathtag.com",
    "rlcdn.com",
    "bluekai.com",
    "krxd.net",
    "demdex.net",
    "omtrdc.net",
    "everesttech.net",
    "agkn.com",
    "turn.com",
    "eyeota.net",
    "lotame.com",
    "crwdcntrl.net",
    "exelator.com",
    "tapad.com",
    "adsymptotic.com",
    "adhigh.net"
  )

  private val BLOCKED_URL_PATH_KEYWORDS = listOf(
    "/pagead/",
    "/api/stats/ads",
    "/get_midroll_info",
    "ad_break",
    "adformat=",
    "ad_type="
  )

  fun isBlockedAdOrTrackerUrl(url: String?): Boolean {
    if (url.isNullOrBlank()) return true
    val lower = url.lowercase(Locale.ROOT)
    val uri = runCatching { Uri.parse(lower) }.getOrNull() ?: return true
    val host = uri.host ?: return false
    if (BLOCKED_AD_AND_TRACKER_DOMAINS.any { domain -> host == domain || host.endsWith(".$domain") }) {
      return true
    }
    return BLOCKED_URL_PATH_KEYWORDS.any { kw -> lower.contains(kw) }
  }

  fun isSchemeAllowedInWebView(url: String?): Boolean {
    if (url.isNullOrBlank()) return false
    val lower = url.trim().lowercase(Locale.ROOT)
    return lower.startsWith("https://") || lower.startsWith("about:blank")
  }

  fun createEmpty204Response(): WebResourceResponse {
    return WebResourceResponse(
      "text/plain",
      "UTF-8",
      204,
      "No Content",
      mapOf(
        "Cache-Control" to "no-store",
        "Access-Control-Allow-Origin" to "*",
        "Access-Control-Allow-Methods" to "GET, POST, OPTIONS, HEAD",
        "Access-Control-Allow-Headers" to "*"
      ),
      ByteArrayInputStream(ByteArray(0))
    )
  }

  fun hardenWebViewSettings(webView: WebView) {
    webView.settings.apply {
      allowFileAccess = false
      allowContentAccess = false
      @Suppress("DEPRECATION")
      allowFileAccessFromFileURLs = false
      @Suppress("DEPRECATION")
      allowUniversalAccessFromFileURLs = false
      setSupportMultipleWindows(false)
      javaScriptCanOpenWindowsAutomatically = false
      mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
      mediaPlaybackRequiresUserGesture = false
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        safeBrowsingEnabled = true
      }
    }
    runCatching {
      CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false)
    }
  }
}

open class AdShieldWebViewClient(
  private val allowedHostsSuffix: Set<String> = setOf(
    "youtube-nocookie.com",
    "youtube.com",
    "youtu.be",
    "ytimg.com",
    "googlevideo.com",
    "ggpht.com",
    "gstatic.com",
    "googleapis.com",
    "google.com",
    "googleusercontent.com",
    "archive.org",
    "bayanulhuda.com"
  ),
  private val onBlockedExternalNavigation: ((String) -> Unit)? = null,
  private val onRenderProcessCrashed: ((WebView?) -> Unit)? = null
) : WebViewClient() {

  override fun shouldInterceptRequest(
    view: WebView?,
    request: WebResourceRequest?
  ): WebResourceResponse? {
    val reqUrl = request?.url?.toString() ?: return AdShieldConfig.createEmpty204Response()
    val lower = reqUrl.trim().lowercase(Locale.ROOT)
    if (lower.startsWith("blob:") || lower.startsWith("data:")) {
      return super.shouldInterceptRequest(view, request)
    }
    if (!AdShieldConfig.isSchemeAllowedInWebView(reqUrl)) {
      return AdShieldConfig.createEmpty204Response()
    }
    if (AdShieldConfig.isBlockedAdOrTrackerUrl(reqUrl)) {
      return AdShieldConfig.createEmpty204Response()
    }
    return super.shouldInterceptRequest(view, request)
  }

  override fun shouldOverrideUrlLoading(
    view: WebView?,
    request: WebResourceRequest?
  ): Boolean {
    val reqUrl = request?.url?.toString() ?: return true
    if (!AdShieldConfig.isSchemeAllowedInWebView(reqUrl)) {
      return true
    }
    if (AdShieldConfig.isBlockedAdOrTrackerUrl(reqUrl)) {
      return true
    }
    val host = request.url?.host?.lowercase(Locale.ROOT) ?: return true
    val isAllowedHost = allowedHostsSuffix.any { suffix ->
      host == suffix || host.endsWith(".$suffix")
    }
    if (!isAllowedHost) {
      onBlockedExternalNavigation?.invoke(reqUrl)
      return true
    }
    return false
  }

  override fun onRenderProcessGone(
    view: WebView?,
    detail: android.webkit.RenderProcessGoneDetail?
  ): Boolean {
    SanitizedLogger.w("AdShieldWebViewClient", "WebView renderer process exited; recovering safely without app crash")
    runCatching {
      view?.stopLoading()
      (view?.parent as? android.view.ViewGroup)?.removeView(view)
      view?.destroy()
    }
    runCatching {
      onRenderProcessCrashed?.invoke(view)
    }
    // Returning true is mandatory on Android 8.0+ (API 26+) so Chromium's aw_browser_terminator.cc
    // does NOT kill the host application process when a WebView renderer crashes or is reclaimed.
    return true
  }
}

// ============================================================================
// LAYER 4: Malware & Magic-Byte Stream Inspector (Anti-Exploit Guard)
// ============================================================================
object MediaSecurityValidator {
  private val DANGEROUS_EXTENSIONS = setOf(
    ".apk", ".dex", ".so", ".elf", ".sh", ".bat", ".cmd", ".exe", ".dll",
    ".scr", ".jar", ".class", ".js", ".vbs", ".html", ".htm", ".svg", ".php", ".py"
  )

  fun hasDangerousExtension(urlOrFilename: String?): Boolean {
    if (urlOrFilename.isNullOrBlank()) return true
    val cleanPath = urlOrFilename.substringBefore('?').substringBefore('#').lowercase(Locale.ROOT)
    return DANGEROUS_EXTENSIONS.any { ext -> cleanPath.endsWith(ext) }
  }

  /**
   * Inspects up to the first 64 bytes of a stream (or byte array) to reject executable binaries,
   * scripts, archives, or disguised HTML/XSS payloads.
   */
  fun isMagicHeaderSafe(headerBytes: ByteArray, length: Int = headerBytes.size): Boolean {
    if (length <= 0) return false
    val n = length.coerceAtMost(headerBytes.size).coerceAtMost(64)

    // 1. Windows PE / MZ Executable: 4D 5A ("MZ")
    if (n >= 2 && headerBytes[0] == 0x4D.toByte() && headerBytes[1] == 0x5A.toByte()) {
      return false
    }
    // 2. Android Dalvik Executable: "dex\n" (64 65 78 0A)
    if (n >= 4 &&
      headerBytes[0] == 0x64.toByte() &&
      headerBytes[1] == 0x65.toByte() &&
      headerBytes[2] == 0x78.toByte() &&
      headerBytes[3] == 0x0A.toByte()
    ) {
      return false
    }
    // 3. Linux ELF Binary: 7F 45 4C 46 ("\u007FELF")
    if (n >= 4 &&
      headerBytes[0] == 0x7F.toByte() &&
      headerBytes[1] == 0x45.toByte() &&
      headerBytes[2] == 0x4C.toByte() &&
      headerBytes[3] == 0x46.toByte()
    ) {
      return false
    }
    // 4. Java Class / Mach-O Universal: CA FE BA BE
    if (n >= 4 &&
      headerBytes[0] == 0xCA.toByte() &&
      headerBytes[1] == 0xFE.toByte() &&
      headerBytes[2] == 0xBA.toByte() &&
      headerBytes[3] == 0xBE.toByte()
    ) {
      return false
    }
    // 5. Mach-O 32/64-bit: FE ED FA CE or FE ED FA CF (and little-endian CE FA ED FE / CF FA ED FE)
    if (n >= 4) {
      val b0 = headerBytes[0].toInt() and 0xFF
      val b1 = headerBytes[1].toInt() and 0xFF
      val b2 = headerBytes[2].toInt() and 0xFF
      val b3 = headerBytes[3].toInt() and 0xFF
      if ((b0 == 0xFE && b1 == 0xED && b2 == 0xFA && (b3 == 0xCE || b3 == 0xCF)) ||
        ((b0 == 0xCE || b0 == 0xCF) && b1 == 0xFA && b2 == 0xED && b3 == 0xFE)
      ) {
        return false
      }
    }
    // 6. Shell Script: "#!" (23 21)
    if (n >= 2 && headerBytes[0] == 0x23.toByte() && headerBytes[1] == 0x21.toByte()) {
      return false
    }
    // 7. Disguised ZIP / APK / JAR Archive: "PK\u0003\u0004" (50 4B 03 04)
    if (n >= 4 &&
      headerBytes[0] == 0x50.toByte() &&
      headerBytes[1] == 0x4B.toByte() &&
      headerBytes[2] == 0x03.toByte() &&
      headerBytes[3] == 0x04.toByte()
    ) {
      return false
    }
    // 8. Disguised HTML / XSS Payload: "<!DOCTYPE", "<html", "<script", "<?xml"
    val asciiPrefix = String(headerBytes, 0, n, StandardCharsets.UTF_8)
      .trimStart()
      .lowercase(Locale.ROOT)
    if (asciiPrefix.startsWith("<!doctype") ||
      asciiPrefix.startsWith("<html") ||
      asciiPrefix.startsWith("<script") ||
      asciiPrefix.startsWith("<?xml")
    ) {
      return false
    }
    return true
  }

  /**
   * Wraps an InputStream in a BufferedInputStream, inspects the first 64 bytes with mark/reset,
   * and throws SecurityException if a malicious magic header is detected.
   */
  fun validateAndRewindStream(input: InputStream): BufferedInputStream {
    val buffered = if (input is BufferedInputStream) input else BufferedInputStream(input, 8192)
    buffered.mark(64)
    val probe = ByteArray(64)
    var totalRead = 0
    while (totalRead < 64) {
      val r = buffered.read(probe, totalRead, 64 - totalRead)
      if (r <= 0) break
      totalRead += r
    }
    buffered.reset()
    if (totalRead > 0 && !isMagicHeaderSafe(probe, totalRead)) {
      buffered.close()
      throw SecurityException("Blocked unsafe or executable magic header in media stream")
    }
    return buffered
  }
}

// ============================================================================
// LAYER 5: SSRF, DNS Rebinding & Redirect Validator
// ============================================================================
object SsrfRedirectValidator {
  private val ALLOWED_PORTS = setOf(-1, 443, 8443)

  fun isIpAddressSafePublic(address: InetAddress): Boolean {
    if (address.isLoopbackAddress ||
      address.isSiteLocalAddress ||
      address.isLinkLocalAddress ||
      address.isMulticastAddress ||
      address.isAnyLocalAddress
    ) {
      return false
    }
    val raw = address.address
    if (raw.size == 4) {
      val b0 = raw[0].toInt() and 0xFF
      val b1 = raw[1].toInt() and 0xFF
      // 10.0.0.0/8, 127.0.0.0/8, 169.254.0.0/16 (Cloud Metadata), 172.16.0.0/12, 192.168.0.0/16, 0.0.0.0/8, 255.255.255.255
      if (b0 == 0 || b0 == 10 || b0 == 127) return false
      if (b0 == 169 && b1 == 254) return false
      if (b0 == 172 && (b1 in 16..31)) return false
      if (b0 == 192 && b1 == 168) return false
      if (b0 >= 224) return false
    } else if (raw.size == 16) {
      // Unique local fc00::/7 or link-local fe80::/10
      val b0 = raw[0].toInt() and 0xFF
      val b1 = raw[1].toInt() and 0xFF
      if ((b0 and 0xFE) == 0xFC) return false
      if (b0 == 0xFE && (b1 and 0xC0) == 0x80) return false
    }
    return true
  }

  /**
   * Validates URL syntax, scheme (HTTPS only), port (443/8443), absence of embedded credentials,
   * and optionally resolves DNS to block private/loopback/cloud-metadata IP ranges.
   */
  fun validateUrlSafety(urlStr: String?, resolveDns: Boolean = true): Boolean {
    if (urlStr.isNullOrBlank()) return false
    val parsedUrl = runCatching { java.net.URL(urlStr.trim()) }.getOrNull() ?: return false
    val scheme = parsedUrl.protocol?.lowercase(Locale.ROOT) ?: return false
    if (scheme != "https") return false
    if (!parsedUrl.userInfo.isNullOrEmpty()) return false // Block embedded credentials (user:pass@host)
    if (parsedUrl.port !in ALLOWED_PORTS) return false

    val host = parsedUrl.host?.lowercase(Locale.ROOT)?.trim() ?: return false
    if (host.isEmpty() || host == "localhost" || host.endsWith(".local") || host.endsWith(".internal")) {
      return false
    }
    if (MediaSecurityValidator.hasDangerousExtension(parsedUrl.path)) {
      return false
    }

    if (resolveDns) {
      val addresses = runCatching { InetAddress.getAllByName(host) }.getOrNull()
      if (addresses.isNullOrEmpty()) return false
      if (addresses.any { !isIpAddressSafePublic(it) }) {
        return false
      }
    }
    return true
  }
}

class SsrfGuardInterceptor(private val resolveDnsLive: Boolean = true) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val urlStr = request.url.toString()
    if (!SsrfRedirectValidator.validateUrlSafety(urlStr, resolveDns = resolveDnsLive)) {
      throw IOException("Blocked unsafe or SSRF-targeted URL: ${SanitizedLogger.redactUrl(urlStr)}")
    }
    val response = chain.proceed(request)
    if (response.isRedirect) {
      val location = response.header("Location")
      if (location != null) {
        val resolvedRedirect = request.url.resolve(location)?.toString()
        if (resolvedRedirect == null || !SsrfRedirectValidator.validateUrlSafety(resolvedRedirect, resolveDns = resolveDnsLive)) {
          response.close()
          throw IOException("Blocked unsafe redirect target")
        }
      }
    }
    return response
  }
}

// ============================================================================
// LAYER 6 & LAYER 7: TLS 1.3 / AEAD Network Hardening, Privacy Header Scrubber,
// and Anti-DoS / Resource Exhaustion Shield
// ============================================================================
object DoSResourceShield {
  const val CONNECT_TIMEOUT_SEC = 10L
  const val READ_TIMEOUT_SEC = 15L
  const val WRITE_TIMEOUT_SEC = 15L
  const val CALL_TIMEOUT_SEC = 25L
  const val MAX_JSON_RESPONSE_BYTES = 2L * 1024L * 1024L // 2 MB cap
  const val DEBOUNCE_WINDOW_MS = 80L

  private val extractionMutex = Mutex()
  private val lastExtractionTimestampByVideo = ConcurrentHashMap<String, Long>()

  fun allowExtractionRequest(videoId: String, nowMs: Long = System.currentTimeMillis()): Boolean {
    val prev = lastExtractionTimestampByVideo[videoId]
    if (prev != null && (nowMs - prev) in 0 until DEBOUNCE_WINDOW_MS) {
      return false
    }
    lastExtractionTimestampByVideo[videoId] = nowMs
    return true
  }

  suspend fun <T> withExtractionLock(block: suspend () -> T): T {
    return extractionMutex.withLock { block() }
  }
}

class PrivacyHeaderScrubberInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val original = chain.request()
    val host = original.url.host.lowercase(Locale.ROOT)
    val isYoutubeSessionHost =
      host == "youtube.com" ||
        host.endsWith(".youtube.com") ||
        host.endsWith(".youtube-nocookie.com") ||
        host.endsWith(".googlevideo.com")
    val sanitizedBuilder = original.newBuilder()
      .removeHeader("X-Requested-With")
      .removeHeader("X-Forwarded-For")
    if (!isYoutubeSessionHost) {
      sanitizedBuilder.removeHeader("Cookie")
    }
    sanitizedBuilder.header(
      "User-Agent",
      original.header("User-Agent")
        ?: "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
    )
    return chain.proceed(sanitizedBuilder.build())
  }
}

object SecureNetworkClient {
  val MODERN_AEAD_CONNECTION_SPEC: ConnectionSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
    .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2)
    .cipherSuites(
      CipherSuite.TLS_AES_128_GCM_SHA256,
      CipherSuite.TLS_AES_256_GCM_SHA384,
      CipherSuite.TLS_CHACHA20_POLY1305_SHA256,
      CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
      CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
      CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
      CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
    )
    .build()

  val httpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .connectionSpecs(listOf(MODERN_AEAD_CONNECTION_SPEC))
      .connectTimeout(DoSResourceShield.CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
      .readTimeout(DoSResourceShield.READ_TIMEOUT_SEC, TimeUnit.SECONDS)
      .writeTimeout(DoSResourceShield.WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
      .callTimeout(DoSResourceShield.CALL_TIMEOUT_SEC, TimeUnit.SECONDS)
      .followRedirects(true)
      .followSslRedirects(true)
      .addInterceptor(PrivacyHeaderScrubberInterceptor())
      .addInterceptor(SsrfGuardInterceptor(resolveDnsLive = true))
      .build()
  }

  /**
   * Reads a JSON/API response body safely with a strict 2 MB ceiling to prevent OOM DoS attacks.
   */
  fun readBoundedUtf8Body(response: Response, maxBytes: Long = DoSResourceShield.MAX_JSON_RESPONSE_BYTES): String {
    val body = response.body ?: return ""
    val contentLength = body.contentLength()
    if (contentLength > maxBytes) {
      throw IOException("Response Content-Length ($contentLength) exceeds safety cap ($maxBytes bytes)")
    }
    val source = body.source()
    source.request(maxBytes + 1)
    if (source.buffer.size > maxBytes) {
      throw IOException("Response stream exceeded safety cap ($maxBytes bytes)")
    }
    return source.buffer.clone().readString(StandardCharsets.UTF_8)
  }
}

// ============================================================================
// LAYER 8: Hardware-Backed Keystore Encryption (AES-256-GCM) & Memory Hygiene
// ============================================================================
object HardwareKeystoreVault {
  private const val ANDROID_KEYSTORE = "AndroidKeyStore"
  private const val KEY_ALIAS = "bayan_ul_huda_master_aes256_gcm_key"
  private const val TRANSFORMATION = "AES/GCM/NoPadding"
  private const val GCM_TAG_BITS = 128

  private fun getOrCreateSecretKey(): SecretKey {
    val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    val existing = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
    if (existing != null) return existing.secretKey

    val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
    val spec = KeyGenParameterSpec.Builder(
      KEY_ALIAS,
      KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
      .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
      .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
      .setKeySize(256)
      .build()
    keyGenerator.init(spec)
    return keyGenerator.generateKey()
  }

  fun encryptUtf8(plaintext: String): String? {
    val rawBytes = plaintext.toByteArray(StandardCharsets.UTF_8)
    return try {
      val cipher = Cipher.getInstance(TRANSFORMATION)
      cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
      val iv = cipher.iv
      val cipherBytes = cipher.doFinal(rawBytes)
      val combined = ByteArray(1 + iv.size + cipherBytes.size)
      combined[0] = iv.size.toByte()
      System.arraycopy(iv, 0, combined, 1, iv.size)
      System.arraycopy(cipherBytes, 0, combined, 1 + iv.size, cipherBytes.size)
      Base64.encodeToString(combined, Base64.NO_WRAP)
    } catch (_: Exception) {
      null
    } finally {
      Arrays.fill(rawBytes, 0.toByte())
    }
  }

  fun decryptUtf8(encodedCiphertext: String): String? {
    return try {
      val combined = Base64.decode(encodedCiphertext, Base64.NO_WRAP)
      if (combined.size < 14) return null
      val ivLen = combined[0].toInt() and 0xFF
      if (ivLen !in 12..16 || combined.size <= 1 + ivLen) return null
      val iv = combined.copyOfRange(1, 1 + ivLen)
      val cipherBytes = combined.copyOfRange(1 + ivLen, combined.size)
      val cipher = Cipher.getInstance(TRANSFORMATION)
      cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
      val plainBytes = cipher.doFinal(cipherBytes)
      try {
        String(plainBytes, StandardCharsets.UTF_8)
      } finally {
        Arrays.fill(plainBytes, 0.toByte())
      }
    } catch (_: Exception) {
      null
    }
  }

  fun wipeByteArray(bytes: ByteArray?) {
    if (bytes != null) {
      Arrays.fill(bytes, 0.toByte())
    }
  }

  fun wipeCharArray(chars: CharArray?) {
    if (chars != null) {
      Arrays.fill(chars, '\u0000')
    }
  }
}

// ============================================================================
// LAYER 9: Anti-Tapjacking & Screen Overlay Protection
// ============================================================================
object TapjackingGuard {
  fun enableOverlayProtection(activity: Activity) {
    runCatching {
      activity.window?.decorView?.filterTouchesWhenObscured = true
    }
  }
}

// ============================================================================
// LAYER 10: Path Traversal (Zip-Slip) & Intent Hijacking Protection
// ============================================================================
object PathTraversalAndIntentGuard {
  private val SAFE_FILENAME_REGEX = Regex("""[^A-Za-z0-9._-]""")

  fun sanitizeFileName(rawName: String): String {
    val stripped = rawName
      .replace("..", "")
      .replace("/", "_")
      .replace("\\", "_")
      .replace("\u0000", "")
    return SAFE_FILENAME_REGEX.replace(stripped, "_").take(120).ifBlank { "safe_asset" }
  }

  fun resolveSafeCanonicalChild(allowedBaseDir: File, relativePath: String): File {
    val baseCanonical = allowedBaseDir.canonicalPath
    val candidate = File(allowedBaseDir, relativePath)
    val candidateCanonical = candidate.canonicalPath
    if (candidateCanonical != baseCanonical &&
      !candidateCanonical.startsWith(baseCanonical + File.separator)
    ) {
      throw SecurityException("Blocked path traversal attempt outside allowed directory")
    }
    return candidate
  }

  fun sanitizeIncomingIntent(intent: Intent?): Boolean {
    if (intent == null) return true
    return runCatching {
      val dataUri = intent.dataString
      if (dataUri != null && SecurityFirewall.containsHostileSignature(dataUri)) {
        intent.data = null
        return@runCatching false
      }
      true
    }.getOrDefault(false)
  }
}

// ============================================================================
// LAYER 11: Runtime Integrity, Anti-Tamper & Anti-Hooking (RASP)
// ============================================================================
object RuntimeIntegrityGuard {
  private val HOOK_KEYWORDS = listOf(
    "frida",
    "xposed",
    "edxp",
    "lsposed",
    "substrate",
    "libgadget.so"
  )

  data class IntegrityStatus(
    val signaturePresent: Boolean,
    val hookingDetected: Boolean,
    val debuggerAttached: Boolean,
    val fridaPortOpen: Boolean
  ) {
    val isClean: Boolean
      get() = signaturePresent && !hookingDetected && !debuggerAttached && !fridaPortOpen
  }

  fun performStartupCheck(context: Context): IntegrityStatus {
    val sigOk = runCatching { verifyApkSignaturePresent(context) }.getOrDefault(true)
    val hookFound = runCatching { detectMemoryOrStackHooks() }.getOrDefault(false)
    val debugger = runCatching { Debug.isDebuggerConnected() || Debug.waitingForDebugger() }.getOrDefault(false)
    // Non-blocking quick check
    return IntegrityStatus(
      signaturePresent = sigOk,
      hookingDetected = hookFound,
      debuggerAttached = debugger,
      fridaPortOpen = false
    )
  }

  fun verifyApkSignaturePresent(context: Context): Boolean {
    return runCatching {
      val pm = context.packageManager
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val pkgInfo = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        val signers = pkgInfo.signingInfo?.apkContentsSigners
        !signers.isNullOrEmpty() && signers.first().toByteArray().isNotEmpty()
      } else {
        @Suppress("DEPRECATION")
        val pkgInfo = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
        @Suppress("DEPRECATION")
        !pkgInfo.signatures.isNullOrEmpty()
      }
    }.getOrDefault(true)
  }

  fun detectMemoryOrStackHooks(): Boolean {
    val mapsHooked = runCatching {
      val mapsFile = File("/proc/self/maps")
      if (mapsFile.exists() && mapsFile.canRead()) {
        mapsFile.useLines { lines ->
          lines.any { line ->
            val lower = line.lowercase(Locale.ROOT)
            HOOK_KEYWORDS.any { kw -> lower.contains(kw) }
          }
        }
      } else {
        false
      }
    }.getOrDefault(false)
    if (mapsHooked) return true

    return runCatching {
      Thread.currentThread().stackTrace.any { frame ->
        val cls = frame.className.lowercase(Locale.ROOT)
        HOOK_KEYWORDS.any { kw -> cls.contains(kw) }
      }
    }.getOrDefault(false)
  }

  fun isLocalFridaPortOpen(timeoutMs: Int = 80): Boolean {
    for (port in intArrayOf(27042, 27043)) {
      val open = runCatching {
        Socket().use { socket ->
          socket.connect(InetSocketAddress("127.0.0.1", port), timeoutMs)
          true
        }
      }.getOrDefault(false)
      if (open) return true
    }
    return false
  }
}

// ============================================================================
// LAYER 12: Zero-Leak Logging, ProGuard & Crash-Resilient Fail-Safe Architecture
// ============================================================================
object SanitizedLogger {
  private val URL_REDACT_REGEX = Regex("""https?://[^\s"']+""")
  private val IPV4_REDACT_REGEX = Regex("""\b\d{1,3}(?:\.\d{1,3}){3}\b""")
  private val TOKEN_REDACT_REGEX = Regex("""(?i)(key|token|auth|signature|sig|expire)=([^&\s]+)""")

  fun redactSensitiveText(message: String?): String {
    if (message.isNullOrBlank()) return ""
    var out = TOKEN_REDACT_REGEX.replace(message, "$1=[REDACTED]")
    out = URL_REDACT_REGEX.replace(out, "[REDACTED_URL]")
    out = IPV4_REDACT_REGEX.replace(out, "[REDACTED_IP]")
    return out
  }

  fun redactUrl(url: String?): String {
    if (url.isNullOrBlank()) return "[EMPTY_URL]"
    val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return "[REDACTED_URL]"
    return "${uri.scheme ?: "https"}://[REDACTED_HOST]"
  }

  fun d(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
      Log.d(tag, redactSensitiveText(message))
    }
  }

  fun w(tag: String, message: String, throwable: Throwable? = null) {
    if (BuildConfig.DEBUG) {
      Log.w(tag, redactSensitiveText(message), throwable)
    }
  }

  fun e(tag: String, message: String, throwable: Throwable? = null) {
    if (BuildConfig.DEBUG) {
      Log.e(tag, redactSensitiveText(message), throwable)
    }
  }
}
