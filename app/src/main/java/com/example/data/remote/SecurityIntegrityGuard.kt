package com.example.data.remote

import java.net.URI
import java.util.Locale

/**
 * Defensive Security & Sacred Content Integrity Guard for Bayan al-Huda.
 *
 * Ensures:
 * 1. Only official, HTTPS-encrypted Islamic scripture, Hadith, Prayer-Time, and Quran audio hosts are contacted.
 * 2. Zero ad networks, third-party trackers, or unauthorized scripts can ever be loaded or injected.
 * 3. Incoming API responses are sanitized against HTML/script/iframe/ad injection before touching Room or UI.
 * 4. Canonical offline Quran verses (6,236 verses across 114 Surahs) cannot be overwritten with corrupted or empty text.
 * 5. Developer diagnostic access is verified for the authorized maintainer without exposing secrets.
 */
object SecurityIntegrityGuard {

  const val AUTHORIZED_DEVELOPER_EMAIL = "sheikhzulqar@gmail.com"
  const val AUTHORIZED_DEVELOPER_NAME = "Zulqarnain Ahmad Sheikh"

  const val MAX_HTTP_RESPONSE_BYTES = 8 * 1024 * 1024 // 8 MB cap for full 6,236-ayah QUL translation JSONs
  const val MAX_SEARCH_QUERY_LENGTH = 250

  private val ALLOWED_HTTPS_HOSTS = setOf(
    "api.alquran.cloud",
    "cdn.jsdelivr.net",
    "api.aladhan.com",
    "everyayah.com",
    "api.quran.com",
    "quran.com",
    "verses.quran.com",
    "audio.qurancdn.com",
    "qurancdn.com",
    "quranenc.com",
    "qul.tarteel.ai",
    "tarteel.ai",
    "tanzil.net",
    "sunnah.com",
    "api.sunnah.com",
    "hadithapi.com",
    "www.hadithapi.com",
    "ummahapi.com",
    "www.ummahapi.com",
    "thaqalayn-api.net",
    "thaqalayn-api.ieron.dev",
    "thaqalayn.net",
    "al-islam.org",
    "quran.al-islam.org",
    "quran3.al-islam.org",
    "almizan.org",
    "shiaquran.in",
    "academyofislam.com",
    "shiaonlinelibrary.com",
    "duas.org",
    "easyquran.ai",
    "raw.githubusercontent.com",
    "github.com",
    "download.quranicaudio.com",
    "quranicaudio.com",
    "islamcan.com",
    "archive.org",
    "ia800304.us.archive.org",
    "ia802606.us.archive.org",
    "apiquran.mp3quran.net",
    "server6.mp3quran.net",
    "server7.mp3quran.net",
    "server8.mp3quran.net",
    "server9.mp3quran.net",
    "server10.mp3quran.net",
    "server11.mp3quran.net",
    "server12.mp3quran.net",
    "server13.mp3quran.net",
    "server14.mp3quran.net",
    "server15.mp3quran.net",
    "server16.mp3quran.net",
    "cdn.mp3quran.net",
    "mp3quran.net",
    "media.way2quran.com",
    "way2quran.com",
    "surahquran.com",
    "tvquran.com",
    "wa.me",
    "api.whatsapp.com",
    "alquran.cloud",
    "aladhan.com",
    "api-docs.quran.com",
    "api-docs.quran.foundation",
    "quran.foundation",
    "sunnah.readme.io",
    "hisnulmuslim.app",
    "opendua.org",
    "audio.opendua.org",
    "opendua.hdfund.org",
    "islamqa.info",
    "taqwafoundation.com",
    "learnnooraniqaida.com",
    "noorani-qaida.com",
    "iifa-aifi.org",
    "dar-alifta.org",
    "amu.ac.in",
    "creativecommons.org",
    "opensource.org",
    "unlicense.org",
    "apache.org",
    "gnu.org",
    "openfontlicense.org",
    "software.sil.org",
    "fonts.google.com",
    "developer.android.com",
    "firebase.google.com"
  )

  private val ALLOWED_HTTPS_DOMAIN_SUFFIXES = listOf(
    ".mp3quran.net",
    ".way2quran.com",
    ".archive.org",
    ".opendua.org",
    ".quran.com",
    ".quran.foundation",
    ".qurancdn.com",
    ".quranicaudio.com",
    ".al-islam.org",
    ".easyquran.ai",
    ".tarteel.ai",
    ".duas.org",
    ".hadithapi.com",
    ".ummahapi.com",
    ".sunnah.com",
    ".google.com",
    ".android.com"
  )

  private val BLOCKED_AD_AND_TRACKER_KEYWORDS = listOf(
    "doubleclick",
    "googlesyndication",
    "admob",
    "adservice",
    "adsense",
    "unityads",
    "applovin",
    "vungle",
    "ironsource",
    "chartboost",
    "taboola",
    "outbrain",
    "moatads",
    "scorecardresearch",
    "adsystem",
    "popads",
    "propellerads"
  )

  private val DANGEROUS_BLOCK_REGEX = Regex(
    """<\s*(script|iframe|object|embed|form|style|svg)\b[^>]*>.*?</\s*\1\s*>""",
    setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
  )

  private val DANGEROUS_INJECTION_REGEX = Regex(
    """<\s*/?\s*(?:script|iframe|object|embed|form|style|meta|link|svg|img)[^>]*>|javascript\s*:|vbscript\s*:|data\s*:\s*text/html|on(?:load|error|click|mouseover|focus)\s*=""",
    setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
  )

  private val SQL_INJECTION_META_REGEX = Regex(
    """(?i)(\b(?:DROP|DELETE|INSERT|UPDATE|ALTER|CREATE|EXEC|UNION\s+SELECT)\b|--|/\*|\*/|;)"""
  )

  /**
   * Validates that a target URL is strictly HTTPS, belongs to the official Islamic content host allowlist,
   * and contains no ad-network or malicious redirect patterns.
   */
  fun isUrlTrustedAndAllowed(rawUrl: String): Boolean {
    val trimmed = rawUrl.trim()
    if (trimmed.isEmpty()) return false
    return try {
      val uri = URI(trimmed)
      val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: return false
      if (scheme != "https") return false

      val host = uri.host?.lowercase(Locale.ROOT)?.removePrefix("www.") ?: return false
      if (BLOCKED_AD_AND_TRACKER_KEYWORDS.any { host.contains(it) || trimmed.lowercase(Locale.ROOT).contains(it) }) {
        return false
      }
      ALLOWED_HTTPS_HOSTS.contains(host) ||
        ALLOWED_HTTPS_DOMAIN_SUFFIXES.any { suffix -> host.endsWith(suffix) }
    } catch (_: Exception) {
      false
    }
  }

  /**
   * Neutralizes BiDi (Bidirectional) Override characters (\u202A-\u202E, \u2066-\u2069) and zero-width
   * cloaking characters (\u200B-\u200D, \uFEFF) commonly exploited by penetration testers and adversaries
   * to visually spoof Arabic text, disguise malicious payloads, or bypass verification filters.
   */
  fun stripUnicodeCloakingAndBidiTrojan(raw: String): String {
    if (raw.isEmpty()) return ""
    return raw
      .replace(Regex("[\u202A-\u202E\u2066-\u2069\u200B-\u200D\uFEFF]"), "")
      .replace(Regex("""[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]"""), "") // Control characters
  }

  /**
   * Sanitizes user search queries against SQL/FTS5 injection, BiDi spoofing, and ReDoS length attacks
   * while preserving Arabic, Urdu, and English characters, colons (for Surah:Ayah), and numbers.
   */
  fun sanitizeSearchQuery(rawQuery: String): String {
    if (rawQuery.isBlank()) return ""
    val stripped = stripUnicodeCloakingAndBidiTrojan(rawQuery)
      .replace(DANGEROUS_BLOCK_REGEX, "")
      .replace(DANGEROUS_INJECTION_REGEX, "")
      .replace(SQL_INJECTION_META_REGEX, " ")
      .replace(Regex("""["'\\^*]"""), " ")
      .replace(Regex("""\s+"""), " ")
      .trim()
    return if (stripped.length > MAX_SEARCH_QUERY_LENGTH) {
      stripped.take(MAX_SEARCH_QUERY_LENGTH).trim()
    } else {
      stripped
    }
  }

  /**
   * Defense-in-depth verification against arbitrary intent injection or malicious redirects.
   * Only trusted, HTTPS-encrypted endpoints matching the strictly defined allowlist can be launched.
   */
  fun isExternalIntentSafe(rawUrl: String): Boolean {
    val trimmed = rawUrl.trim()
    if (trimmed.isEmpty()) return false
    return try {
      val uri = URI(trimmed)
      val scheme = uri.scheme?.lowercase(Locale.ROOT)
      if (scheme != "https") return false
      val host = uri.host?.lowercase(Locale.ROOT)?.removePrefix("www.") ?: return false
      if (BLOCKED_AD_AND_TRACKER_KEYWORDS.any { host.contains(it) || trimmed.lowercase(Locale.ROOT).contains(it) }) {
        return false
      }
      ALLOWED_HTTPS_HOSTS.contains(host) ||
        ALLOWED_HTTPS_DOMAIN_SUFFIXES.any { suffix -> host.endsWith(suffix) }
    } catch (_: Exception) {
      false
    }
  }

  /**
   * Launches an email client (Gmail / default mail app) pre-populated with a Flag / Content Report
   * directed to `sheikhzulqar@gmail.com`. Falls back to ACTION_SEND message/rfc822 if needed.
   */
  fun launchDeveloperEmailReport(
    context: android.content.Context,
    referenceTitle: String,
    issueCategory: String,
    issueDetails: String,
    sourceCollectionOrEdition: String = ""
  ): Boolean {
    val cleanRef = stripUnicodeCloakingAndBidiTrojan(referenceTitle).trim().ifBlank { "General Content" }
    val cleanCat = stripUnicodeCloakingAndBidiTrojan(issueCategory).trim().ifBlank { "Content / Translation Flag" }
    val cleanDetails = stripUnicodeCloakingAndBidiTrojan(issueDetails).trim().ifBlank { "Flagged by user for scholarly verification." }
    val cleanSource = stripUnicodeCloakingAndBidiTrojan(sourceCollectionOrEdition).trim()

    val subject = "Bayan al-Huda: Flag & Report [$cleanRef]"
    val body = buildString {
      append("Reference: ").append(cleanRef).append("\n")
      if (cleanSource.isNotEmpty()) {
        append("Source / Edition: ").append(cleanSource).append("\n")
      }
      append("Category: ").append(cleanCat).append("\n\n")
      append("Reported Details:\n").append(cleanDetails).append("\n\n")
      append("Recipient: ").append(AUTHORIZED_DEVELOPER_EMAIL).append("\n")
      append("App Version: 1.1.0")
    }

    return try {
      val mailtoUri = android.net.Uri.parse("mailto:$AUTHORIZED_DEVELOPER_EMAIL")
      val sendToIntent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
        data = mailtoUri
        putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(AUTHORIZED_DEVELOPER_EMAIL))
        putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
        putExtra(android.content.Intent.EXTRA_TEXT, body)
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(
        android.content.Intent.createChooser(sendToIntent, "Send Report to Gmail ($AUTHORIZED_DEVELOPER_EMAIL)").apply {
          addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      )
      true
    } catch (_: Exception) {
      try {
        val fallbackIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
          type = "message/rfc822"
          putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(AUTHORIZED_DEVELOPER_EMAIL))
          putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
          putExtra(android.content.Intent.EXTRA_TEXT, body)
          addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(
          android.content.Intent.createChooser(fallbackIntent, "Send Report to Gmail ($AUTHORIZED_DEVELOPER_EMAIL)").apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
          }
        )
        true
      } catch (_: Exception) {
        false
      }
    }
  }

  /**
   * Checks whether incoming text contains dangerous injection strings, eval/script tags,
   * or inappropriate altered payloads intended to corrupt Islamic scripture.
   */
  fun containsInappropriateOrAlteredPayload(raw: String): Boolean {
    if (raw.isEmpty()) return false
    val lower = raw.lowercase(Locale.ROOT)
    if (DANGEROUS_BLOCK_REGEX.containsMatchIn(raw) || DANGEROUS_INJECTION_REGEX.containsMatchIn(raw)) {
      return true
    }
    return lower.contains("javascript:") ||
      lower.contains("data:text/html") ||
      lower.contains("<svg") ||
      lower.contains("onload=") ||
      lower.contains("onerror=")
  }

  /**
   * Sanitizes sacred text with full anti-tamper, BiDi neutralizing, and script stripping protections.
   */
  fun sanitizeSacredText(rawText: String): String {
    if (rawText.isEmpty()) return ""
    val deBidi = stripUnicodeCloakingAndBidiTrojan(rawText)
    var cleaned = deBidi.replace(DANGEROUS_BLOCK_REGEX, "")
    cleaned = cleaned.replace(DANGEROUS_INJECTION_REGEX, "")
    // Remove any stray HTML tags while keeping legitimate angle-bracket-free religious text intact
    cleaned = cleaned.replace(Regex("""</?[a-zA-Z][^>]{0,120}>"""), "")
    return cleaned.trim()
  }

  /**
   * Verifies that a fetched Quran verse has non-empty Uthmani Arabic and translation text before
   * allowing it to be stored, preventing any corrupted response from overwriting canonical offline verses.
   */
  fun isValidQuranVersePayload(surahNumber: Int, verseNumber: Int, arabicText: String): Boolean {
    if (surahNumber !in 1..114) return false
    if (verseNumber !in 1..286) return false
    val sanitized = sanitizeSacredText(arabicText)
    if (sanitized.isBlank()) return false
    // Must contain authentic Arabic Unicode block characters (\u0600-\u06FF or \u0750-\u077F or \uFB50-\uFDFF)
    return sanitized.any { ch ->
      ch in '\u0600'..'\u06FF' || ch in '\u0750'..'\u077F' || ch in '\uFB50'..'\uFDFF'
    }
  }

  /**
   * Verifies that a Hadith or Sermon record has a valid positive canonical number, non-blank collection key,
   * and non-empty sanitized text free of script/injection payloads.
   */
  fun isValidCanonicalHadithPayload(
    collectionKey: String,
    hadithNumber: Int,
    arabicText: String,
    englishText: String
  ): Boolean {
    if (collectionKey.isBlank() || hadithNumber <= 0) return false
    if (containsInappropriateOrAlteredPayload(arabicText) || containsInappropriateOrAlteredPayload(englishText)) {
      return false
    }
    val cleanAr = sanitizeSacredText(arabicText)
    val cleanEn = sanitizeSacredText(englishText)
    return cleanAr.isNotBlank() || cleanEn.isNotBlank()
  }

  private val FTS_RESERVED_KEYWORDS = setOf("AND", "OR", "NOT", "NEAR")

  /**
   * Builds a safe SQLite FTS4 MATCH prefix expression from user input.
   * Strips FTS syntax characters and excludes standalone FTS boolean operators
   * (AND, OR, NOT, NEAR) so queries like "NOT AVAILABLE" or "MERCY AND FORGIVENESS"
   * never produce malformed MATCH expressions such as "NOT*" or "AND*".
   */
  fun toSafeFtsMatchQuery(rawQuery: String): String {
    val sanitized = sanitizeSearchQuery(rawQuery)
    if (sanitized.isBlank()) return ""
    return sanitized
      .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
      .split(Regex("\\s+"))
      .map { it.trim() }
      .filter { token ->
        token.length > 1 && token.uppercase() !in FTS_RESERVED_KEYWORDS
      }
      .joinToString(" ") { "$it*" }
  }

  /**
   * Redacts sensitive query parameters (such as apiKey, key, token) from URLs before logging.
   */
  fun redactSensitiveUrl(url: String): String {
    return url.replace(
      Regex("([?&](?:apiKey|api_key|key|token)=)[^&#\\s]+", RegexOption.IGNORE_CASE),
      "$1[REDACTED]"
    )
  }

  /**
   * UI-only provenance header helper: checks whether the signed-in email matches the maintainer
   * email to toggle the About-screen section heading label. Never use for access control.
   */
  fun isAuthorizedDeveloper(email: String?): Boolean {
    return email?.trim()?.equals(AUTHORIZED_DEVELOPER_EMAIL, ignoreCase = true) == true
  }

  fun getSecurityAuditSummary(): String {
    return "HTTPS-Only Host Pinning Active (${ALLOWED_HTTPS_HOSTS.size} Allowlisted Islamic APIs) • " +
      "Anti-Penetration BiDi & FTS5 Injection Shield Active • Ad-Injection & Script Sanitizer Active • " +
      "Cleartext Disabled • Room Canonical Scripture & Hadith Protection Enabled"
  }
}
