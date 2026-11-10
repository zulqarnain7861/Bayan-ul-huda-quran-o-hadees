package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.AuthManager
import com.example.data.local.AppDatabase
import com.example.data.local.BayanDatabase
import com.example.data.local.KnowledgeDatabase
import com.example.data.local.ReligiousContentCompletenessVerifier
import com.example.data.local.entities.BookmarkEntity
import com.example.data.local.entities.NoteEntity
import com.example.data.remote.MultiSourceHadithCatalog
import com.example.data.remote.SecurityIntegrityGuard
import com.example.data.repository.IslamicRepository
import com.example.data.repository.QuranTajweedEngineRepository
import com.example.security.AdShieldConfig
import com.example.security.AdShieldWebViewClient
import com.example.ui.quran.TajweedCorrectionEngine
import com.example.ui.quran.TajweedWordGrade
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SecurityAndQualityTicketVerificationTest {

  private val context: Context = ApplicationProvider.getApplicationContext()

  private fun resolveRepoRootFile(relativePath: String): File {
    val roots = listOf(File("."), File(".."), File("../.."))
    val repoRoot = roots.firstOrNull { File(it, "settings.gradle.kts").exists() }
    if (repoRoot != null) {
      val fromRoot = File(repoRoot, relativePath)
      if (fromRoot.exists()) return fromRoot
    }
    val candidates = listOf(
      File(relativePath),
      File("..", relativePath),
      File("../..", relativePath)
    )
    return candidates.firstOrNull { it.exists() } ?: File(relativePath)
  }

  @Test
  fun verifyOffMainThreadTajweedCachingIntegrityAndNavigationGuards() = runBlocking {
    QuranTajweedEngineRepository.clearSurahTajweedCache()

    // 1. Load Surah 1 and Surah 2 asynchronously off the main thread
    val surah1Bundle = QuranTajweedEngineRepository.loadSurahTajweedBundleAsync(context, 1)
    assertNotNull(surah1Bundle)
    assertEquals(1, surah1Bundle!!.surah)
    assertEquals(7, surah1Bundle.records.size)
    assertTrue("SHA-256 integrity hash must verify", surah1Bundle.verifyIntegrity())

    val cachedEntry = QuranTajweedEngineRepository.getCachedSurahTajweedEntry(1)
    assertNotNull(cachedEntry)
    assertEquals(surah1Bundle.integrityHash, cachedEntry!!.integrityHash)

    // 2. Verify tampered integrity hash is detected
    val tamperedEntry = surah1Bundle.copy(sha256IntegrityHash = "0000000000000000000000000000000000000000000000000000000000000000")
    assertFalse("Tampered cache entry must fail integrity check", tamperedEntry.verifyIntegrity())

    // 3. Verify rapid-navigation cross-surah guard rejects stale record from Surah 1 when rendering Surah 2
    val rulesMap = QuranTajweedEngineRepository.getTajweedRules(context).associateBy { it.id }
    val staleRecordFromSurah1 = surah1Bundle.records.first()
    val targetSurah2Text = "الم"
    val guardedResult = QuranTajweedEngineRepository.buildSafeTajweedAnnotatedString(
      record = staleRecordFromSurah1,
      fallbackArabicText = targetSurah2Text,
      rulesMap = rulesMap,
      expectedSurah = 2,
      expectedAyah = 1
    )
    assertEquals(
      "Must reject stale Surah 1 record when expectedSurah=2 and fall back to Surah 2 text",
      targetSurah2Text,
      guardedResult.text
    )
    assertTrue("Rejected stale record must not attach Surah 1 spans", guardedResult.spanStyles.isEmpty())

    // 4. Verify UTF-16 surrogate pair boundary split is safely dropped without crashing
    val surrogateText = "بِسْمِ \uD83D\uDE00 ٱللَّهِ" // \uD83D\uDE00 is at indices 7..8
    val badSurrogateSpans = listOf(
      QuranTajweedEngineRepository.TajweedSpanAnnotation(start = 7, end = 8, ruleId = "maddSukoon"), // splits surrogate pair at 8!
      QuranTajweedEngineRepository.TajweedSpanAnnotation(start = 8, end = 10, ruleId = "maddSukoon"), // starts on low surrogate at 8!
      QuranTajweedEngineRepository.TajweedSpanAnnotation(start = 0, end = 6, ruleId = "maddSukoon")   // valid span
    )
    val filtered = QuranTajweedEngineRepository.validateAndFilterAnnotations(
      surah = 1,
      ayah = 1,
      textUthmani = surrogateText,
      rawAnnotations = badSurrogateSpans,
      validRuleIds = rulesMap.keys
    )
    assertEquals("Only the non-surrogate-splitting span must survive", 1, filtered.size)
    assertEquals(0, filtered.first().start)
    assertEquals(6, filtered.first().end)
  }

  @Test
  fun verifyFalseTajweedAccuracyResultsAreEliminated() {
    val basmala = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"

    // 1. Silence / background noise (peakVoiceLevel < 0.28f) MUST return 0% accuracy, never 100%
    val silentEval = TajweedCorrectionEngine.evaluateRecordedAudioCadence(
      canonicalVerseText = basmala,
      surah = 1,
      ayah = 1,
      recordingDurationMs = 2500L,
      peakVoiceLevel = 0.05f
    )
    assertEquals(0, silentEval.matchedCount)
    assertEquals(0, silentEval.accuracyPercentage)
    assertTrue(silentEval.isCadenceFallback)

    // 2. Quick tap without reciting (recordingDurationMs < 650ms) MUST return 0% accuracy
    val shortTapEval = TajweedCorrectionEngine.evaluateRecordedAudioCadence(
      canonicalVerseText = basmala,
      surah = 1,
      ayah = 1,
      recordingDurationMs = 250L,
      peakVoiceLevel = 0.80f
    )
    assertEquals(0, shortTapEval.matchedCount)
    assertEquals(0, shortTapEval.accuracyPercentage)

    // 3. Omitting definite article "ال" must be graded as SMALL_MISTAKE (70%), not CORRECT (100%)
    val omittedAlEval = TajweedCorrectionEngine.evaluateRecitation(
      canonicalVerseText = basmala,
      recognizedSpokenArabic = "بسم الله رحمن رحيم",
      surah = 1,
      ayah = 1
    )
    assertEquals(2, omittedAlEval.matchedCount)
    assertEquals(2, omittedAlEval.smallMistakeCount)
    assertEquals(TajweedWordGrade.SMALL_MISTAKE, omittedAlEval.wordStatuses[2].grade)
    assertEquals(TajweedWordGrade.SMALL_MISTAKE, omittedAlEval.wordStatuses[3].grade)
    assertTrue("Omitting ال must not yield 100% accuracy", omittedAlEval.accuracyPercentage < 100)

    // 4. Extra spoken words must penalize accuracy below 100%
    val extraWordsEval = TajweedCorrectionEngine.evaluateRecitation(
      canonicalVerseText = basmala,
      recognizedSpokenArabic = "بسم الله الرحمن الرحيم خطأ زيادة",
      surah = 1,
      ayah = 1
    )
    assertEquals(2, extraWordsEval.extraSpokenWordsCount)
    assertTrue("Extra spoken words must reduce accuracy below 100%", extraWordsEval.accuracyPercentage < 100)
    assertTrue("Extra spoken words must increment wrongOrMissedCount", extraWordsEval.wrongOrMissedCount >= 2)
  }

  @Test
  fun verifyAccountDeletionIsReliableAndVerifiable() = runBlocking {
    val repo = IslamicRepository.getInstance(context)
    val db = BayanDatabase.getDatabase(context)

    // Seed local user bookmarks and notes
    db.bookmarkDao().insertBookmark(
      BookmarkEntity(
        itemType = "QURAN",
        itemKey = "ayah_2_255",
        title = "Al-Baqarah 2:255",
        subtitle = "Ayat al-Kursi",
        arabicSnippet = "ٱللَّهُ لَآ إِلَٰهَ إِلَّا هُوَ"
      )
    )
    db.noteDao().insertNote(
      NoteEntity(
        referenceKey = "2:255",
        title = "Reflection on Ayat al-Kursi",
        content = "Test note for reliable account deletion"
      )
    )
    AuthManager.startGuestSession(context)

    assertTrue(db.bookmarkDao().countBookmarks() >= 1)
    assertTrue(db.noteDao().countNotes() >= 1)
    assertTrue(AuthManager.isGuestSessionActive(context))

    // Execute account & data deletion without any active UI Flow collector
    val delResult = AuthManager.deleteCurrentAccount(
      activityContext = context,
      onDeleteAndVerifyLocalData = { repo.deleteAllUserDataAndVerify() }
    )
    assertTrue("Account deletion must succeed: ${delResult.exceptionOrNull()}", delResult.isSuccess)
    assertEquals("0 bookmarks must remain in SQLite", 0, db.bookmarkDao().countBookmarks())
    assertEquals("0 notes must remain in SQLite", 0, db.noteDao().countNotes())
    assertFalse("Guest session must be cleared", AuthManager.isGuestSessionActive(context))

    val receipt = AuthManager.lastDeletionReceipt.value
    assertNotNull(receipt)
    assertTrue("Deletion receipt must be fully verified", receipt!!.isFullyVerified)
    assertEquals(0, receipt.localRemainingBookmarks)
    assertEquals(0, receipt.localRemainingNotes)
  }

  @Test
  fun verifyFirebaseAppCheckConfigurationInDebugAndProduction() {
    val debugToken = "bayan-test-debug-token-987654"
    val intent = Intent().apply {
      putExtra(DEBUG_TOKEN_EXTRA, debugToken)
    }

    configureAppCheck(context, intent, forcePlayIntegrity = false)
    assertEquals("DebugAppCheckProviderFactory", AppCheckConfigState.lastInstalledProvider)
    assertTrue(AppCheckConfigState.lastDebugTokenConfigured)
    val prefs = context.getSharedPreferences(AppCheckConfigState.lastPersistenceStoreName, Context.MODE_PRIVATE)
    assertEquals(debugToken, prefs.getString(DEBUG_SECRET_KEY, null))

    // Verify production fallback installs PlayIntegrityAppCheckProviderFactory when recaptcha_site_key is placeholder
    configureAppCheck(context, null, forcePlayIntegrity = true)
    assertEquals("PlayIntegrityAppCheckProviderFactory", AppCheckConfigState.lastInstalledProvider)

    // Verify MainActivity calls configureAppCheck(this, intent) immediately after super.onCreate(savedInstanceState)
    val mainActivityFile = resolveRepoRootFile("app/src/main/java/com/example/MainActivity.kt")
    val mainSrc = mainActivityFile.readText()
    val superIdx = mainSrc.indexOf("super.onCreate(savedInstanceState)")
    val appCheckIdx = mainSrc.indexOf("configureAppCheck(this, intent)")
    val sanitizeIdx = mainSrc.indexOf("PathTraversalAndIntentGuard.sanitizeIncomingIntent(intent)")
    assertTrue("configureAppCheck must be present in MainActivity", appCheckIdx > superIdx)
    assertTrue("configureAppCheck must run BEFORE sanitizeIncomingIntent or setContent", appCheckIdx < sanitizeIdx)
  }

  @Test
  fun verifyPrivateApiKeysMovedToServerSideProxy() {
    // Default runtime URLs must route through the server-side proxy and NEVER embed ?apiKey=
    val booksUrl = MultiSourceHadithCatalog.hadithApiBooksUrl()
    val chaptersUrl = MultiSourceHadithCatalog.hadithApiChaptersUrl("bukhari")
    val chapterHadithsUrl = MultiSourceHadithCatalog.hadithApiChapterHadithsUrl("bukhari", 1)
    val singleHadithUrl = MultiSourceHadithCatalog.hadithApiSingleHadithUrl("bukhari", "1")
    val sunnahProxyUrl = MultiSourceHadithCatalog.sunnahProxySingleHadithUrl("bukhari", "1")

    for (url in listOfNotNull(booksUrl, chaptersUrl, chapterHadithsUrl, singleHadithUrl, sunnahProxyUrl)) {
      assertTrue("Proxy URL must start with HTTPS proxy base: $url", url.startsWith("https://hadith-proxy.bayanulhuda.org/v1/"))
      assertFalse("Runtime URL must NEVER contain exposed private apiKey query param: $url", SecurityIntegrityGuard.containsExposedPrivateApiKeyInUrl(url))
      assertTrue("Proxy URL must be trusted by SecurityIntegrityGuard: $url", SecurityIntegrityGuard.isUrlTrustedAndAllowed(url))
    }

    // Verify URL redaction helper strips any accidental query secret
    val redacted = SecurityIntegrityGuard.redactSensitiveUrl("https://hadithapi.com/api/books?apiKey=secret123&paginate=50")
    assertFalse(redacted.contains("secret123"))
    assertTrue(redacted.contains("apiKey=[REDACTED]"))

    // Verify server-side proxy worker exists
    val proxyFile = resolveRepoRootFile("tools/proxy/hadith-api-proxy.js")
    assertTrue("Server-side proxy worker must exist at tools/proxy/hadith-api-proxy.js", proxyFile.exists())
    val proxySource = proxyFile.readText()
    assertTrue(proxySource.contains("env.HADITH_API_KEY"))
    assertTrue(proxySource.contains("env.SUNNAH_API_KEY"))
    assertTrue(proxySource.contains("env.UMMAH_API_KEY"))
  }

  @Test
  fun verifyLocalDatabaseMigrationsPreserveUserDataWithoutDestructiveFallback() {
    assertFalse("AppDatabase must not enable destructive fallback", AppDatabase.IS_DESTRUCTIVE_FALLBACK_ENABLED)
    assertFalse("KnowledgeDatabase must not enable destructive fallback", KnowledgeDatabase.IS_DESTRUCTIVE_FALLBACK_ENABLED)
    assertTrue("AppDatabase must register all migrations from v1..v8", AppDatabase.ALL_MIGRATIONS.size >= 11)

    // Create an in-memory SQLite database simulating legacy v1/v3 user data and run migrations through v8
    val callback = object : SupportSQLiteOpenHelper.Callback(1) {
      override fun onCreate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `bookmarks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `itemType` TEXT NOT NULL, `itemKey` TEXT NOT NULL, `title` TEXT NOT NULL, `subtitle` TEXT NOT NULL, `arabicSnippet` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `notes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `referenceKey` TEXT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `hadiths` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `collectionKey` TEXT NOT NULL, `hadithNumber` INTEGER NOT NULL, `arabicText` TEXT NOT NULL)")
      }
      override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    }
    val config = SupportSQLiteOpenHelper.Configuration.builder(context)
      .name(null) // in-memory
      .callback(callback)
      .build()
    val helper = FrameworkSQLiteOpenHelperFactory().create(config)
    val sqldb = helper.writableDatabase

    sqldb.execSQL("INSERT INTO `bookmarks` (`itemType`, `itemKey`, `title`, `subtitle`, `arabicSnippet`, `timestamp`) VALUES ('QURAN', 'ayah_1_1', 'Al-Fatihah 1:1', 'In the name of Allah', 'بِسْمِ ٱللَّهِ', 1700000000)")
    sqldb.execSQL("INSERT INTO `notes` (`referenceKey`, `title`, `content`, `createdAt`, `updatedAt`) VALUES ('1:1', 'Opening Note', 'Preserved across migrations', 1700000000, 1700000000)")

    // Run sequential migrations 1 -> 2 -> 3 -> 6 -> 8
    AppDatabase.MIGRATION_1_2.migrate(sqldb)
    AppDatabase.MIGRATION_2_3.migrate(sqldb)
    AppDatabase.MIGRATION_3_6.migrate(sqldb)
    AppDatabase.MIGRATION_6_8.migrate(sqldb)

    val bmCursor = sqldb.query("SELECT COUNT(*) FROM `bookmarks` WHERE `itemKey` = 'ayah_1_1'")
    assertTrue(bmCursor.moveToFirst())
    assertEquals("Bookmark row must survive all migrations 1->8", 1, bmCursor.getInt(0))
    bmCursor.close()

    val noteCursor = sqldb.query("SELECT COUNT(*) FROM `notes` WHERE `referenceKey` = '1:1'")
    assertTrue(noteCursor.moveToFirst())
    assertEquals("Note row must survive all migrations 1->8", 1, noteCursor.getInt(0))
    noteCursor.close()

    helper.close()
  }

  @Test
  fun verifyReligiousContentCompletenessAndIntegrity() {
    val report = ReligiousContentCompletenessVerifier.verifyAllBundledReligiousContent(context)
    assertTrue(
      "All religious content must be complete and verified without placeholders, issues=${report.issues}",
      report.isCompleteAndVerified
    )
    assertEquals(114, report.totalSurahsVerified)
    assertEquals(6236, report.totalAyahsVerified)
    assertEquals(0, report.blankAyahFieldsCount)
    assertEquals(0, report.hadithsWithPlaceholderOrBlankCount)
    assertEquals(0, report.blankDuasCount)
    assertEquals(6, report.kalimasCount)
    assertEquals(17, report.nooraniQaidaLessonsCount)
    assertEquals(17, report.tajweedRulesCount)
    assertEquals(57, report.tajweedLessonsCount)
  }

  @Test
  fun verifyStrengthenedWebViewHostRestrictions() {
    val client = AdShieldWebViewClient()

    fun fakeRequest(url: String): WebResourceRequest {
      val parsed = Uri.parse(url)
      return object : WebResourceRequest {
        override fun getUrl(): Uri = parsed
        override fun isForMainFrame(): Boolean = true
        override fun isRedirect(): Boolean = false
        override fun hasGesture(): Boolean = false
        override fun getMethod(): String = "GET"
        override fun getRequestHeaders(): MutableMap<String, String> = mutableMapOf()
      }
    }

    // 1. Legitimate YouTube embed & media CDN requests must be permitted
    assertTrue(client.isUrlPermittedByHostPolicy("https://www.youtube-nocookie.com/embed/DPvekksFmBs"))
    assertTrue(client.isUrlPermittedByHostPolicy("https://m.youtube.com/watch?v=DPvekksFmBs"))
    assertTrue(client.isUrlPermittedByHostPolicy("https://i.ytimg.com/vi/DPvekksFmBs/hqdefault.jpg"))
    assertTrue(client.isUrlPermittedByHostPolicy("https://rr1---sn-4g5ednsd.googlevideo.com/videoplayback?id=123"))
    assertFalse(client.shouldOverrideUrlLoading(null, fakeRequest("https://www.youtube-nocookie.com/embed/DPvekksFmBs")))

    // 2. Unauthorized external domains, broad google.com/archive.org domains, userinfo spoofing, and data:text/html must be BLOCKED in BOTH navigation AND shouldInterceptRequest
    val blockedUrls = listOf(
      "https://evil.example.com/payload.js",
      "https://www.google.com/search?q=phishing",
      "https://drive.google.com/file/d/123",
      "https://archive.org/download/untrusted.js",
      "https://www.youtube.com@evil.example.com/embed/DPvekksFmBs",
      "http://www.youtube-nocookie.com/embed/DPvekksFmBs",
      "javascript:alert(1)",
      "file:///android_asset/quran_full_offline.json",
      "data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg=="
    )

    for (badUrl in blockedUrls) {
      assertFalse("Must reject disallowed URL in host policy: $badUrl", client.isUrlPermittedByHostPolicy(badUrl))
      assertTrue("shouldOverrideUrlLoading must block navigation to: $badUrl", client.shouldOverrideUrlLoading(null, fakeRequest(badUrl)))
      val intercepted = client.shouldInterceptRequest(null, fakeRequest(badUrl))
      assertNotNull("shouldInterceptRequest must block subresource load with 204 response for: $badUrl", intercepted)
      assertEquals(204, intercepted!!.statusCode)
    }
  }

  @Test
  fun verifyCleanBuildSigningAndGitHubBranchProtectionConfig() {
    val envExample = resolveRepoRootFile(".env.example")
    assertTrue(".env.example must exist", envExample.exists())
    assertTrue(envExample.readText().contains("HADITH_API_PROXY_BASE_URL"))

    val signingScript = resolveRepoRootFile("tools/signing/prepare-clean-build-signing.sh")
    assertTrue("Clean-build signing preparation script must exist", signingScript.exists())

    val ciWorkflow = resolveRepoRootFile(".github/workflows/android-ci.yml")
    assertTrue("GitHub Actions CI workflow must exist", ciWorkflow.exists())
    val ciContent = ciWorkflow.readText()
    assertTrue(ciContent.contains("build-and-test"))
    assertTrue(ciContent.contains("firestore-rules-test"))
    assertTrue(ciContent.contains("testDebugUnitTest"))

    val branchProtectionFile = resolveRepoRootFile(".github/branch-protection.json")
    assertTrue("Branch protection JSON must exist", branchProtectionFile.exists())
    val bpJson = JSONObject(branchProtectionFile.readText())
    assertTrue(bpJson.getBoolean("enforce_admins"))
    assertTrue(bpJson.getBoolean("required_linear_history"))
    assertFalse(bpJson.getBoolean("allow_force_pushes"))
    assertFalse(bpJson.getBoolean("allow_deletions"))
    val contexts = bpJson.getJSONObject("required_status_checks").getJSONArray("contexts")
    val contextList = (0 until contexts.length()).map { contexts.getString(it) }
    assertTrue(contextList.contains("build-and-test"))
    assertTrue(contextList.contains("firestore-rules-test"))
  }
}
