package com.example.data.repository

import android.content.Context
import java.io.BufferedInputStream
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipInputStream

/**
 * Offline repository for the 4,203 IslamQA scholarly answers from
 * https://github.com/asim/aslam/blob/main/data/islamqa.zip (islamqa-combined.json).
 *
 * RELIGIOUS SOURCE PRESERVATION RULES:
 * - Preserves original question wording, original answer wording, Arabic quotations,
 *   Quran & Hadith references, categories, and scholarly attribution.
 * - Never rewrites, summarizes, truncates, or AI-translates any fatwa/answer.
 * - Where Urdu translation does not exist in the source dataset, displays:
 *   "اردو ترجمہ فی الحال دستیاب نہیں"
 */
object IslamQaRepository {

  const val ASSET_ZIP_PATH = "islamqa/islamqa.zip"
  const val URDU_UNAVAILABLE_NOTICE = "اردو ترجمہ فی الحال دستیاب نہیں"
  const val EXPECTED_RECORD_COUNT = 4203

  private const val PREFS_NAME = "bayan_islamqa_prefs"
  private const val KEY_BOOKMARKED_IDS = "bookmarked_islamqa_ids"

  data class IslamQaArticle(
    val id: Int,
    val question: String,
    val answer: String,
    val categoryRaw: String,
    val categoryDisplayEnglish: String,
    val categoryDisplayUrdu: String,
    val categoryDisplayArabic: String,
    val extractedReferences: List<String>,
    val hasArabicQuotations: Boolean,
    val urduStatusNotice: String = URDU_UNAVAILABLE_NOTICE,
    val scholarlyAttribution: String = "IslamQA Scholarly Committee (Shaykh Muhammad Salih al-Munajjid — General Supervisor)",
    val sourceRepository: String = "https://github.com/asim/aslam (data/islamqa.zip • islamqa-combined.json)"
  )

  data class IslamQaCategorySummary(
    val categoryRaw: String,
    val displayEnglish: String,
    val displayUrdu: String,
    val displayArabic: String,
    val articleCount: Int
  )

  data class IslamQaAuditReport(
    val totalRecords: Int,
    val categoriesCount: Int,
    val zipSizeBytes: Int,
    val sha256Checksum: String,
    val allQuestionsNonBlank: Boolean,
    val allAnswersNonBlank: Boolean,
    val status: String
  )

  @Volatile
  private var cachedArticles: List<IslamQaArticle>? = null
  @Volatile
  private var cachedCategories: List<IslamQaCategorySummary>? = null
  private val inMemoryBookmarks = ConcurrentHashMap.newKeySet<Int>()

  private val categoryLabelsMap = mapOf(
    "basic tenets of faith" to Triple("Basic Tenets of Faith ('Aqidah)", "عقائد و ایمانیات", "العقيدة وأصول الإيمان"),
    "the quran and its sciences" to Triple("The Qur'an & Its Sciences", "قرآن اور علومِ قرآن", "القرآن وعلومه"),
    "hadith its sciences" to Triple("Hadith & Its Sciences", "حدیث اور علومِ حدیث", "الحديث وعلومه"),
    "principles of fiqh" to Triple("Principles of Fiqh & Worship", "اصولِ فقہ اور عبادات", "أصول الفقه والعبادات"),
    "fiqh of the family" to Triple("Fiqh of the Family (Nikah & Family Law)", "عائلی مسائل و نکاح", "فقه الأسرة والنكاح"),
    "etiquette morals and heart softeners" to Triple("Etiquette, Morals & Heart Softeners (Adab & Raqa'iq)", "آداب، اخلاق اور رقائق", "الآداب والأخلاق والرقائق"),
    "islamic history and biography" to Triple("Islamic History & Biography (Seerah & Tarikh)", "اسلامی تاریخ اور سیرت", "التاريخ الإسلامي والسيرة"),
    "knowledge propagation" to Triple("Seeking Knowledge & Da'wah", "طلبِ علم اور دعوت و تبلیغ", "طلب العلم والدعوة"),
    "pedagogy education and upbringing" to Triple("Education & Islamic Upbringing (Tarbiyah)", "تعلیم و تربیتِ اولاد", "التربية والتعليم"),
    "psychological and social problems" to Triple("Psychological & Social Guidance", "نفسیاتی و سماجی مسائل کا حل", "المشكلات النفسية والاجتماعية"),
    "islamic politics" to Triple("Public Affairs & Governance (Siyasah Shar'iyyah)", "اسلامی سیاست و اجتماعی مسائل", "السياسة الشرعية والشؤون العامة"),
    "general" to Triple("General Scholarly Rulings & Contemporary Issues", "متفرق علمی و فقہی فتاویٰ", "فتاوى عامة وقضايا معاصرة")
  )

  private val refRegex = Regex(
    "(?:al-Bukhaari|Bukhari|Muslim|Abu Dawood|Abu Dawud|al-Tirmidhi|Tirmidhi|al-Nasaa'i|Nasa'i|Ibn Maajah|Ibn Majah|Ahmad|al-baqarah|aal 'imraan|al-nisaa|al-maa'idah|surah)[^.;\\n]{0,45}\\d+",
    RegexOption.IGNORE_CASE
  )

  private fun openZipInputStream(context: Context?): java.io.InputStream {
    if (context != null) {
      runCatching {
        return context.assets.open(ASSET_ZIP_PATH)
      }
    }
    val candidates = listOf(
      File("app/src/main/assets/$ASSET_ZIP_PATH"),
      File("src/main/assets/$ASSET_ZIP_PATH"),
      File("../app/src/main/assets/$ASSET_ZIP_PATH")
    )
    val found = candidates.firstOrNull { it.exists() }
      ?: throw IllegalStateException("Missing IslamQA dataset: $ASSET_ZIP_PATH")
    return found.inputStream()
  }

  private fun readJsonString(reader: java.io.Reader, sb: StringBuilder): String {
    sb.setLength(0)
    while (true) {
      val ch = reader.read()
      if (ch == -1 || ch == '"'.code) break
      if (ch == '\\'.code) {
        val esc = reader.read()
        if (esc == -1) break
        when (esc.toChar()) {
          '"' -> sb.append('"')
          '\\' -> sb.append('\\')
          '/' -> sb.append('/')
          'b' -> sb.append('\b')
          'f' -> sb.append('\u000C')
          'n' -> sb.append('\n')
          'r' -> sb.append('\r')
          't' -> sb.append('\t')
          'u' -> {
            val c1 = reader.read()
            val c2 = reader.read()
            val c3 = reader.read()
            val c4 = reader.read()
            if (c1 != -1 && c2 != -1 && c3 != -1 && c4 != -1) {
              val hex = String(charArrayOf(c1.toChar(), c2.toChar(), c3.toChar(), c4.toChar()))
              val code = hex.toIntOrNull(16)
              if (code != null) sb.append(code.toChar())
            }
          }
          else -> sb.append(esc.toChar())
        }
      } else {
        sb.append(ch.toChar())
      }
    }
    return sb.toString()
  }

  private fun skipJsonValue(reader: java.io.Reader, firstNonWs: Int, sb: StringBuilder) {
    when (firstNonWs.toChar()) {
      '"' -> readJsonString(reader, sb)
      '{', '[' -> {
        var depth = 1
        var inStr = false
        while (depth > 0) {
          val c = reader.read()
          if (c == -1) break
          if (inStr) {
            if (c == '\\'.code) {
              reader.read()
            } else if (c == '"'.code) {
              inStr = false
            }
          } else {
            when (c.toChar()) {
              '"' -> inStr = true
              '{', '[' -> depth++
              '}', ']' -> depth--
            }
          }
        }
      }
      else -> {
        while (true) {
          reader.mark(1)
          val c = reader.read()
          if (c == -1) break
          val ch = c.toChar()
          if (ch == ',' || ch == '}' || ch == ']' || ch.isWhitespace()) {
            reader.reset()
            break
          }
        }
      }
    }
  }

  private fun nextNonWhitespace(reader: java.io.Reader): Int {
    while (true) {
      val c = reader.read()
      if (c == -1 || !c.toChar().isWhitespace()) return c
    }
  }

  /**
   * Loads all 4,203 scholarly Q&A records from islamqa.zip (islamqa-combined.json)
   * using a streaming reader so the full 19.5 MB JSON string and intermediate maps
   * are never allocated in heap memory at once.
   */
  fun getAllArticles(context: Context? = null): List<IslamQaArticle> {
    cachedArticles?.let { return it }
    synchronized(this) {
      cachedArticles?.let { return it }
      val articles = ArrayList<IslamQaArticle>(EXPECTED_RECORD_COUNT)
      val sb = StringBuilder(4096)

      openZipInputStream(context).use { rawIn ->
        ZipInputStream(BufferedInputStream(rawIn, 32 * 1024)).use { zis ->
          var entry = zis.nextEntry
          while (entry != null) {
            if (!entry.isDirectory && entry.name.endsWith(".json", ignoreCase = true)) {
              val reader = java.io.BufferedReader(java.io.InputStreamReader(zis, Charsets.UTF_8), 32 * 1024)
              var tok = nextNonWhitespace(reader)
              if (tok == '['.code) {
                var idx = 0
                while (true) {
                  var next = nextNonWhitespace(reader)
                  if (next == ','.code) next = nextNonWhitespace(reader)
                  if (next == -1 || next == ']'.code) break
                  if (next == '{'.code) {
                    var q = ""
                    var a = ""
                    var catRaw = "general"
                    while (true) {
                      var fTok = nextNonWhitespace(reader)
                      if (fTok == ','.code) fTok = nextNonWhitespace(reader)
                      if (fTok == -1 || fTok == '}'.code) break
                      if (fTok == '"'.code) {
                        val key = readJsonString(reader, sb)
                        val colon = nextNonWhitespace(reader)
                        if (colon == ':'.code) {
                          val valStart = nextNonWhitespace(reader)
                          if (valStart == '"'.code) {
                            val strVal = readJsonString(reader, sb)
                            when (key) {
                              "question" -> q = strVal
                              "answer" -> a = strVal
                              "category" -> catRaw = strVal.trim().lowercase().ifEmpty { "general" }
                            }
                          } else {
                            skipJsonValue(reader, valStart, sb)
                          }
                        }
                      }
                    }

                    val labels = categoryLabelsMap[catRaw] ?: Triple(
                      catRaw.replaceFirstChar { it.uppercase() },
                      "علمی و فقہی مسائل",
                      "فتاوى علمية"
                    )
                    var hasArabic = false
                    for (i in 0 until a.length) {
                      val code = a[i].code
                      if (code in 0x0600..0x06FF) {
                        hasArabic = true
                        break
                      }
                    }
                    val refs = if (idx < 300) {
                      refRegex.findAll(a)
                        .map { it.value.trim() }
                        .distinct()
                        .take(4)
                        .toList()
                    } else {
                      emptyList()
                    }
                    idx++
                    articles.add(
                      IslamQaArticle(
                        id = idx,
                        question = q,
                        answer = a,
                        categoryRaw = catRaw,
                        categoryDisplayEnglish = labels.first,
                        categoryDisplayUrdu = labels.second,
                        categoryDisplayArabic = labels.third,
                        extractedReferences = refs,
                        hasArabicQuotations = hasArabic
                      )
                    )
                  } else {
                    skipJsonValue(reader, next, sb)
                  }
                }
              }
              break
            }
            entry = zis.nextEntry
          }
        }
      }

      cachedArticles = articles
      return articles
    }
  }

  fun getCategories(context: Context? = null): List<IslamQaCategorySummary> {
    cachedCategories?.let { return it }
    val all = getAllArticles(context)
    val grouped = all.groupingBy { it.categoryRaw }.eachCount()
    val summaries = grouped.entries
      .sortedByDescending { it.value }
      .map { (catRaw, count) ->
        val labels = categoryLabelsMap[catRaw] ?: Triple(
          catRaw.replaceFirstChar { it.uppercase() },
          "علمی و فقہی مسائل",
          "فتاوى علمية"
        )
        IslamQaCategorySummary(
          categoryRaw = catRaw,
          displayEnglish = labels.first,
          displayUrdu = labels.second,
          displayArabic = labels.third,
          articleCount = count
        )
      }
    cachedCategories = summaries
    return summaries
  }

  fun searchArticles(
    context: Context?,
    query: String,
    categoryFilter: String = "ALL",
    bookmarksOnly: Boolean = false
  ): List<IslamQaArticle> {
    val all = getAllArticles(context)
    val bookmarkedSet = if (bookmarksOnly) getBookmarkedIds(context) else emptySet()
    val cleanQuery = query.trim()
    val asId = cleanQuery.removePrefix("#").trim().toIntOrNull()

    return all.filter { art ->
      val catMatch = categoryFilter.isBlank() ||
        categoryFilter.equals("ALL", ignoreCase = true) ||
        art.categoryRaw.equals(categoryFilter, ignoreCase = true)
      val bmMatch = !bookmarksOnly || (art.id in bookmarkedSet)
      if (!catMatch || !bmMatch) return@filter false

      if (cleanQuery.isEmpty()) {
        true
      } else {
        (asId != null && art.id == asId) ||
          art.question.contains(cleanQuery, ignoreCase = true) ||
          art.answer.contains(cleanQuery, ignoreCase = true) ||
          art.categoryDisplayEnglish.contains(cleanQuery, ignoreCase = true) ||
          art.categoryDisplayUrdu.contains(cleanQuery, ignoreCase = true)
      }
    }
  }

  fun toggleBookmark(context: Context?, articleId: Int): Boolean {
    if (context == null) {
      return if (articleId in inMemoryBookmarks) {
        inMemoryBookmarks.remove(articleId)
        false
      } else {
        inMemoryBookmarks.add(articleId)
        true
      }
    }
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val current = prefs.getStringSet(KEY_BOOKMARKED_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
    val key = articleId.toString()
    val added = if (key in current) {
      current.remove(key)
      inMemoryBookmarks.remove(articleId)
      false
    } else {
      current.add(key)
      inMemoryBookmarks.add(articleId)
      true
    }
    prefs.edit().putStringSet(KEY_BOOKMARKED_IDS, current).apply()
    return added
  }

  fun getBookmarkedIds(context: Context?): Set<Int> {
    if (context == null) return inMemoryBookmarks.toSet()
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val fromPrefs = (prefs.getStringSet(KEY_BOOKMARKED_IDS, emptySet()) ?: emptySet())
      .mapNotNull { it.toIntOrNull() }
      .toSet()
    return fromPrefs + inMemoryBookmarks
  }

  fun auditIslamQaDataset(context: Context? = null): IslamQaAuditReport {
    val digest = MessageDigest.getInstance("SHA-256")
    var totalZipBytes = 0
    openZipInputStream(context).use { input ->
      val buf = ByteArray(16 * 1024)
      var r = input.read(buf)
      while (r != -1) {
        digest.update(buf, 0, r)
        totalZipBytes += r
        r = input.read(buf)
      }
    }
    val sha = digest.digest().joinToString("") { "%02x".format(it) }
    val all = getAllArticles(context)
    val cats = getCategories(context)
    return IslamQaAuditReport(
      totalRecords = all.size,
      categoriesCount = cats.size,
      zipSizeBytes = totalZipBytes,
      sha256Checksum = sha,
      allQuestionsNonBlank = all.isNotEmpty() && all.all { it.question.isNotBlank() },
      allAnswersNonBlank = all.isNotEmpty() && all.all { it.answer.isNotBlank() },
      status = if (all.size >= EXPECTED_RECORD_COUNT) "COMPLETE" else "PARTIAL"
    )
  }
}
