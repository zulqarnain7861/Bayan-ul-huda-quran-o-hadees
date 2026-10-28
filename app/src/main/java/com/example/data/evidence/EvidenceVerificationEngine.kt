package com.example.data.evidence

import android.content.Context
import com.example.data.download.DownloadManager
import com.example.data.local.AppDatabase
import com.example.data.local.ExtendedHadithDataProvider
import com.example.data.local.InitialDataLoader
import com.example.data.local.entities.AyahEntity
import com.example.data.local.entities.HadithEntity
import com.example.data.local.entities.IncorrectReferenceReportEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Priority 12 Section C — Strictly defined claim verification result statuses.
 * Never uses "FALSE" merely because a search returned no results.
 */
enum class ClaimVerificationStatus(
  val displayLabel: String,
  val badgeSummary: String
) {
  EXACT_SOURCE_FOUND(
    displayLabel = "EXACT SOURCE FOUND",
    badgeSummary = "Verified verbatim match in primary canonical scripture or hadith collection."
  ),
  PARTIAL_MATCH_FOUND(
    displayLabel = "PARTIAL MATCH FOUND",
    badgeSummary = "Related wording or thematic evidence located; exact phrasing differs."
  ),
  ATTRIBUTION_DISPUTED(
    displayLabel = "ATTRIBUTION DISPUTED",
    badgeSummary = "Widely circulated quote whose attribution to the Prophet (ﷺ) is disputed or rejected by hadith critics."
  ),
  SCHOLARLY_GRADING_DISAGREEMENT(
    displayLabel = "SCHOLARLY GRADING DISAGREEMENT",
    badgeSummary = "Primary source exists, and qualified hadith scholars hold differing authentication grades."
  ),
  HISTORICAL_CLAIM_NEEDS_MORE_EVIDENCE(
    displayLabel = "HISTORICAL CLAIM NEEDS MORE EVIDENCE",
    badgeSummary = "Historical report relies on tarikh/chronicle chains requiring further corroboration."
  ),
  NO_VERIFIED_MATCH_IN_INDEXED_SOURCES(
    displayLabel = "NO VERIFIED MATCH IN INDEXED SOURCES",
    badgeSummary = "Question remains unresolved by the currently indexed verified material (not automatically labelled FALSE)."
  )
}

/**
 * Priority 12 Section A — The 7 searchable home actions in Islam with Evidence.
 */
enum class EvidenceQuickAction(
  val id: String,
  val title: String,
  val subtitle: String,
  val sampleQuery: String
) {
  VERIFY_HADITH(
    id = "verify_hadith",
    title = "Verify a Hadith",
    subtitle = "Check collection, book, Darussalam/In-Book number, chain, and grading",
    sampleQuery = "Actions are judged by intentions"
  ),
  CHECK_RELIGIOUS_QUOTE(
    id = "check_quote",
    title = "Check a Religious Quote",
    subtitle = "Distinguish authentic prophetic statements from popular sayings or wisdom",
    sampleQuery = "Seek knowledge even if you have to go as far as China"
  ),
  INVESTIGATE_HISTORICAL_CLAIM(
    id = "investigate_history",
    title = "Investigate a Historical Claim",
    subtitle = "Examine early Islamic history & reports with primary tarikh & hadith scrutiny",
    sampleQuery = "Spider web and dove at the Cave of Thawr during Hijrah"
  ),
  EXPLORE_QURAN_HADITH_EVIDENCE(
    id = "explore_evidence",
    title = "Explore Quran and Hadith Evidence",
    subtitle = "See direct Quranic verses alongside authentic Sunnah and Shia narrations",
    sampleQuery = "Ayat al-Kursi 2:255 and protection before sleep"
  ),
  COMPARE_SCHOLARLY_ASSESSMENTS(
    id = "compare_scholars",
    title = "Compare Scholarly Assessments",
    subtitle = "Compare classical and contemporary muhaddithin and Sunni/Shia methodologies",
    sampleQuery = "Hadith al-Thaqalayn Quran and Ahl al-Bayt"
  ),
  UNDERSTAND_WEAK_OR_FABRICATED(
    id = "weak_or_fabricated",
    title = "Understand a Weak or Fabricated Report",
    subtitle = "Learn why a report is graded Da'if or Mawdu' without sectarian polemics",
    sampleQuery = "He who knows himself knows his Lord"
  ),
  LEARN_HADITH_AUTHENTICATION(
    id = "learn_authentication",
    title = "Learn How Hadith Authentication Works",
    subtitle = "Study isnad continuity, narrator integrity ('adalah), and precision (dabt)",
    sampleQuery = "How do scholars authenticate hadith and isnad?"
  )
}

/**
 * Priority 12 Section D — Hadith Grading Definitions & Scholar Attribution.
 */
enum class HadithGradeCategory(
  val gradeTitle: String,
  val arabicTerm: String,
  val definition: String,
  val conditionsSummary: String,
  val normativeUsageNote: String
) {
  SAHIH(
    gradeTitle = "Sahih",
    arabicTerm = "صَحِيح",
    definition = "Authentic: transmitted through an unbroken chain (isnad) of upright ('adl) and precise (dabit) narrators from beginning to end, free from hidden defects ('illah) and anomalous contradiction (shudhudh).",
    conditionsSummary = "1. Unbroken continuity (Ittisal al-Sanad) • 2. Moral integrity ('Adalah) • 3. Accurate retention (Dabt) • 4. Absence of anomaly (ghayr shadhdh) • 5. Absence of subtle defect (ghayr mu'allal).",
    normativeUsageNote = "Accepted as authoritative primary evidence in creed ('aqidah), law (fiqh), and ethics across classical scholarship."
  ),
  HASAN(
    gradeTitle = "Hasan",
    arabicTerm = "حَسَن",
    definition = "Good / Sound: meets the same requirements as Sahih except that one or more narrators have slightly lesser memory precision (dabt khafif), or it is elevated to Hasan li-ghayrihi through multiple corroborating pathways.",
    conditionsSummary = "Continuous chain, upright narrators, free of anomaly and defect, with slightly lighter precision than top-tier Sahih narrators.",
    normativeUsageNote = "Used alongside Sahih as valid legal and ethical evidence by the vast majority of jurists and hadith scholars."
  ),
  DAIF(
    gradeTitle = "Da'if",
    arabicTerm = "ضَعِيف",
    definition = "Weak: falls short of the requirements of Hasan due to a discontinuity in the chain (e.g., mursal, munqati', mu'dal) or a criticism regarding a narrator's memory or precision.",
    conditionsSummary = "Chain interruption or narrator memory weakness that is not severe enough to indicate fabrication.",
    normativeUsageNote = "Does NOT automatically mean the Prophet (ﷺ) never said it; rather, the specific chain does not establish certainty. Classical scholars debate its cautious use in virtuous deeds (fada'il al-a'mal) when under a general Quranic principle."
  ),
  MAWDU(
    gradeTitle = "Mawdu'",
    arabicTerm = "مَوْضُوع",
    definition = "Fabricated / Forged: a statement falsely attributed to the Prophet (ﷺ), identified either by a known fabricator (kadhdhab) in the chain, confession, or stark contradiction with established Quranic and historical certainties.",
    conditionsSummary = "Presence of an impugned fabricator or clear textual/historical impossibility.",
    normativeUsageNote = "Strictly prohibited to attribute to the Prophet (ﷺ) except to warn and clarify that it is fabricated."
  ),
  GRADING_NOT_VERIFIED(
    gradeTitle = "Grading not verified",
    arabicTerm = "غَيْرُ مُحَقَّق التَّخْرِيج",
    definition = "Unverified Grading: the narration or historical report exists in a secondary compilation or chronicle without an indexed critical grading from a verified muhaddith in the local database.",
    conditionsSummary = "Requires consulting specialized takhrij works before assigning a definitive grade.",
    normativeUsageNote = "Must not be presented as Sahih or Mawdu' without verified scholarly attribution."
  )
}

/**
 * Represents a verifiable citation in Priority 12 with exact source metadata and clickable navigation target.
 */
data class PrimarySourceCitation(
  val citationId: String,
  val sourceType: String, // "QURAN", "HADITH_SUNNI", "HADITH_SHIA", "HISTORICAL_CHRONICLE"
  val traditionBadge: String, // "Universal Quran", "Sunni (Kutub al-Sittah)", "Twelver Shia (Kutub al-Arba'ah)", "Classical Tarikh"
  val collectionTitle: String,
  val editionTitle: String,
  val chapterOrBookLabel: String,
  val narrationOrVerseIdentifier: String, // e.g., "Quran 2:255" or "Sahih al-Bukhari #1 (Book 1, Hadith 1)"
  val originalArabicText: String,
  val englishTranslation: String,
  val urduTranslation: String,
  val transliteration: String = "",
  val narratorOrIsnad: String = "",
  val relationshipType: String = "DIRECT_PRIMARY_QUOTATION", // "DIRECT_PRIMARY_QUOTATION" vs "THEMATIC_CORROBORATION"
  val officialVerificationUrl: String,
  val navigableSurahNumber: Int? = null,
  val navigableVerseNumber: Int? = null,
  val navigableCollectionKey: String? = null,
  val navigableHadithNumber: Int? = null
)

/**
 * Represents an individual scholar's grading or assessment with explicit work attribution.
 */
data class ScholarlyAssessmentEntry(
  val scholarName: String,
  val scholarlyTradition: String,
  val assignedGrade: HadithGradeCategory,
  val citedWorkAndEdition: String,
  val reasoningSummary: String
)

/**
 * Represents a verified Further Study link (Priority 12 Section F — Scholar & Lecture Policy).
 */
data class VerifiedFurtherStudyResource(
  val title: String,
  val speakerOrAuthor: String,
  val originalPublisher: String,
  val resourceType: String, // "Classical Treatise", "Academic Article", "Verified Lecture Link"
  val officialUrl: String,
  val licenceAndLinkingNote: String
)

/**
 * Complete result returned by [EvidenceVerificationEngine.verifyClaimOrQuestion].
 */
data class ClaimVerificationResponse(
  val queryText: String,
  val detectedLanguage: String,
  val status: ClaimVerificationStatus,
  val headlineVerdict: String,
  val directQuotations: List<PrimarySourceCitation>,
  val thematicCorroborations: List<PrimarySourceCitation>,
  val scholarlyAssessments: List<ScholarlyAssessmentEntry>,
  val counterEvidenceOrNuance: List<String>,
  val commentaryAndSynthesisEnglish: String,
  val commentaryAndSynthesisUrdu: String,
  val evidenceVsCommentaryNote: String,
  val furtherStudyLinks: List<VerifiedFurtherStudyResource>,
  val workedOfflineOnly: Boolean,
  val epistemicDisclaimer: String = "This verification tool retrieves indexed primary sources and documented scholarly gradings for educational study. It does not replace consultation with qualified scholars (Ulama) or issue personal fatwas."
)

/**
 * Priority 12 Section E — Respectful, evidence-based dossier on controversial & historical subjects.
 */
data class ControversialTopicDossier(
  val topicId: String,
  val categoryLabel: String,
  val titleEnglish: String,
  val titleArabic: String,
  val titleUrdu: String,
  val primaryEvidenceSummary: String,
  val sunniScholarlyPerspective: String,
  val shiaScholarlyPerspective: String,
  val areasOfAgreement: String,
  val knownDisagreementsAndAdab: String,
  val keyCitations: List<PrimarySourceCitation>,
  val furtherStudy: List<VerifiedFurtherStudyResource>
)

/**
 * Priority 12 Section G — Structured Learning Pathway Module (1..10).
 */
data class LearningModuleQuiz(
  val question: String,
  val options: List<String>,
  val correctOptionIndex: Int,
  val explanation: String
)

data class EvidenceLearningModule(
  val moduleNumber: Int,
  val id: String,
  val title: String,
  val arabicSubtitle: String,
  val shortExplanation: String,
  val workedExampleTitle: String,
  val workedExampleSourceRef: String,
  val workedExampleArabic: String,
  val workedExampleAnalysis: String,
  val quiz: LearningModuleQuiz,
  val furtherReading: List<String>
)

/**
 * Priority 12 — Islam with Evidence: Claim Verification and Learning Centre Engine.
 */
class EvidenceVerificationEngine(
  private val context: Context,
  private val database: AppDatabase = AppDatabase.getDatabase(context),
  private val downloadManager: DownloadManager = DownloadManager.getInstance(context)
) {

  private val ayahDao = database.ayahDao()
  private val hadithDao = database.hadithDao()

  /**
   * Normalizes Arabic text by removing harakat/tashkeel and unifying hamza/alif variants
   * so users can search with or without diacritics.
   */
  fun normalizeArabicForMatch(input: String): String {
    val diacriticsRegex = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u0640]")
    return input.replace(diacriticsRegex, "")
      .replace('أ', 'ا')
      .replace('إ', 'ا')
      .replace('آ', 'ا')
      .replace('ٱ', 'ا')
      .replace('ة', 'ه')
      .replace('ى', 'ي')
      .trim()
  }

  fun detectQueryLanguage(query: String): String {
    val clean = query.trim()
    val hasArabicOrUrduScript = clean.any { it.code in 0x0600..0x06FF }
    val hasUrduSpecificChars = clean.any { it in setOf('ٹ', 'ڈ', 'ڑ', 'ں', 'ے', 'گ', 'چ', 'پ', 'ک', 'ہ', 'ی') }
    return when {
      hasUrduSpecificChars -> "Urdu (اردو)"
      hasArabicOrUrduScript -> "Arabic (العربية)"
      clean.contains("innama", ignoreCase = true) ||
        clean.contains("a'mal", ignoreCase = true) ||
        clean.contains("niyyat", ignoreCase = true) ||
        clean.contains("thaqalayn", ignoreCase = true) ||
        clean.contains("mawla", ignoreCase = true) ||
        clean.contains("isnad", ignoreCase = true) ||
        clean.contains("sahih", ignoreCase = true) -> "Transliteration / English"
      else -> "English"
    }
  }

  /**
   * Main RAG verification pipeline over verified local Room DB + curated scholarly case library.
   * Works seamlessly with the internet disabled using downloaded/local resources (Priority 12 Section I Test 9).
   */
  suspend fun verifyClaimOrQuestion(
    rawQuery: String,
    isOfflineModeOverride: Boolean? = null
  ): ClaimVerificationResponse = withContext(Dispatchers.IO) {
    val query = rawQuery.trim()
    val isOffline = isOfflineModeOverride ?: downloadManager.syncBadgeState.value.isOfflineMode
    val detectedLang = detectQueryLanguage(query)

    if (query.isEmpty()) {
      return@withContext ClaimVerificationResponse(
        queryText = "",
        detectedLanguage = "English",
        status = ClaimVerificationStatus.NO_VERIFIED_MATCH_IN_INDEXED_SOURCES,
        headlineVerdict = "Enter a Hadith, religious quotation, or historical claim to verify.",
        directQuotations = emptyList(),
        thematicCorroborations = emptyList(),
        scholarlyAssessments = emptyList(),
        counterEvidenceOrNuance = emptyList(),
        commentaryAndSynthesisEnglish = "Please enter a query in Arabic, Urdu, English, or transliteration.",
        commentaryAndSynthesisUrdu = "براہِ کرم عربی، اردو، انگریزی یا رومن تلفظ میں کوئی حدیث، قول یا تاریخی دعویٰ درج کریں۔",
        evidenceVsCommentaryNote = "Primary scriptural texts are always displayed separately from scholarly commentary.",
        furtherStudyLinks = emptyList(),
        workedOfflineOnly = isOffline
      )
    }

    // 1. Check curated high-precision scholarly verification dossiers first (covers classic disputed quotes,
    // grading disagreements, historical claims, Sunni & Shia comparisons, and exact famous narrations)
    findCuratedCaseMatch(query, detectedLang, isOffline)?.let { curated ->
      return@withContext curated
    }

    // 2. Search verified local Room DB (Ayahs + Hadiths) and ExtendedHadithDataProvider
    val normalizedArabicQuery = normalizeArabicForMatch(query)
    val localAyahMatches = searchLocalQuranVerses(query, normalizedArabicQuery)
    val localHadithMatches = searchLocalHadiths(query, normalizedArabicQuery)

    if (localAyahMatches.isNotEmpty() || localHadithMatches.isNotEmpty()) {
      val directCitations = mutableListOf<PrimarySourceCitation>()
      val thematicCitations = mutableListOf<PrimarySourceCitation>()
      val assessments = mutableListOf<ScholarlyAssessmentEntry>()

      val isExactMatch = localAyahMatches.any { ayah ->
        ayah.textArabic.contains(query) ||
          normalizeArabicForMatch(ayah.textArabic).contains(normalizedArabicQuery) ||
          ayah.translationEnglish.contains(query, ignoreCase = true) ||
          ayah.translationUrdu.contains(query) ||
          query.contains("${ayah.surahNumber}:${ayah.verseNumber}")
      } || localHadithMatches.any { hadith ->
        hadith.arabicText.contains(query) ||
          normalizeArabicForMatch(hadith.arabicText).contains(normalizedArabicQuery) ||
          hadith.englishText.contains(query, ignoreCase = true) ||
          hadith.urduText.contains(query) ||
          query.equals(hadith.reference, ignoreCase = true)
      }

      for (ayah in localAyahMatches.take(4)) {
        val direct = ayah.textArabic.contains(query) ||
          ayah.translationEnglish.contains(query, ignoreCase = true) ||
          query.contains("${ayah.surahNumber}:${ayah.verseNumber}")
        val citation = PrimarySourceCitation(
          citationId = "quran_${ayah.surahNumber}_${ayah.verseNumber}",
          sourceType = "QURAN",
          traditionBadge = "Universal Quran (Uthmani Script)",
          collectionTitle = "Al-Qur'an al-Kareem",
          editionTitle = "King Fahd Complex / Tanzil Uthmani • Sahih International & Fateh Jalandhry",
          chapterOrBookLabel = "Surah ${ayah.surahNumber}, Juz ${ayah.juzNumber}, Page ${ayah.pageNumber}",
          narrationOrVerseIdentifier = "Quran ${ayah.surahNumber}:${ayah.verseNumber}",
          originalArabicText = ayah.textArabic,
          englishTranslation = ayah.translationEnglish,
          urduTranslation = ayah.translationUrdu,
          transliteration = ayah.transliteration,
          narratorOrIsnad = "Mutawatir Divine Revelation (التواتر القطعي)",
          relationshipType = if (direct) "DIRECT_PRIMARY_QUOTATION" else "THEMATIC_CORROBORATION",
          officialVerificationUrl = "https://quran.com/${ayah.surahNumber}/${ayah.verseNumber}",
          navigableSurahNumber = ayah.surahNumber,
          navigableVerseNumber = ayah.verseNumber
        )
        if (direct) directCitations.add(citation) else thematicCitations.add(citation)
      }

      for (hadith in localHadithMatches.take(5)) {
        val isShia = hadith.collectionKey in setOf("alkafi", "manlayahduruhu", "tahdhib", "istibsar", "nahjulbalagha", "sahifasajjadiyya")
        val traditionBadge = if (isShia) {
          "Twelver Shia (Imamiyyah Canonical Collection)"
        } else {
          "Sunni (Ahl al-Sunnah Canonical Collection)"
        }
        val gradeEnum = mapGradeStringToCategory(hadith.grade)
        val citation = PrimarySourceCitation(
          citationId = "hadith_${hadith.collectionKey}_${hadith.hadithNumber}",
          sourceType = if (isShia) "HADITH_SHIA" else "HADITH_SUNNI",
          traditionBadge = traditionBadge,
          collectionTitle = hadith.collectionName,
          editionTitle = "${hadith.darussalamReference.ifBlank { "Standard Edition" }} • ${hadith.inBookReference}",
          chapterOrBookLabel = "Book ${hadith.bookNumber} (${hadith.bookName}) • Chapter ${hadith.chapterNumber} (${hadith.chapterName})",
          narrationOrVerseIdentifier = "${hadith.collectionName} #${hadith.hadithNumber}",
          originalArabicText = hadith.arabicText,
          englishTranslation = hadith.englishText,
          urduTranslation = hadith.urduText,
          narratorOrIsnad = hadith.narrator,
          relationshipType = if (isExactMatch) "DIRECT_PRIMARY_QUOTATION" else "THEMATIC_CORROBORATION",
          officialVerificationUrl = if (isShia) {
            "https://thaqalayn.net"
          } else {
            "https://sunnah.com/${hadith.collectionKey}:${hadith.hadithNumber}"
          },
          navigableCollectionKey = hadith.collectionKey,
          navigableHadithNumber = hadith.hadithNumber
        )
        if (isExactMatch && directCitations.size < 3) {
          directCitations.add(citation)
        } else {
          thematicCitations.add(citation)
        }

        assessments.add(
          ScholarlyAssessmentEntry(
            scholarName = hadith.gradingScholar.ifBlank { "Classical Muhaddithin" },
            scholarlyTradition = traditionBadge,
            assignedGrade = gradeEnum,
            citedWorkAndEdition = "${hadith.collectionName} (${hadith.darussalamReference.ifBlank { "Book ${hadith.bookNumber}, Hadith ${hadith.hadithNumber}" }})",
            reasoningSummary = "Graded ${hadith.grade} based on narrator chain (${hadith.narrator}) and canonical compilation standards."
          )
        )

        // If the hadith has linked Quran verses, retrieve them explicitly as THEMATIC_CORROBORATION (never mislabelled as a direct quotation of the hadith!)
        if (hadith.linkedQuranVerses.isNotBlank()) {
          val refs = hadith.linkedQuranVerses.split(",").map { it.trim() }.filter { it.contains(":") }
          for (ref in refs.take(2)) {
            val sNum = ref.substringBefore(":").toIntOrNull()
            val vNum = ref.substringAfter(":").toIntOrNull()
            if (sNum != null && vNum != null) {
              val linkedAyah = ayahDao.getAyah(sNum, vNum)
                ?: InitialDataLoader.getFoundationalAyahs().find { it.surahNumber == sNum && it.verseNumber == vNum }
              if (linkedAyah != null && thematicCitations.none { it.narrationOrVerseIdentifier == "Quran $sNum:$vNum" }) {
                thematicCitations.add(
                  PrimarySourceCitation(
                    citationId = "thematic_quran_${sNum}_$vNum",
                    sourceType = "QURAN",
                    traditionBadge = "Universal Quran (Thematic Corroboration)",
                    collectionTitle = "Al-Qur'an al-Kareem",
                    editionTitle = "Uthmani Script • Sahih International",
                    chapterOrBookLabel = "Surah $sNum, Ayah $vNum (Thematically Related to ${hadith.collectionName} #${hadith.hadithNumber})",
                    narrationOrVerseIdentifier = "Quran $sNum:$vNum",
                    originalArabicText = linkedAyah.textArabic,
                    englishTranslation = linkedAyah.translationEnglish,
                    urduTranslation = linkedAyah.translationUrdu,
                    transliteration = linkedAyah.transliteration,
                    narratorOrIsnad = "Thematic Quranic Foundation (Not a verbatim hadith quotation)",
                    relationshipType = "THEMATIC_CORROBORATION",
                    officialVerificationUrl = "https://quran.com/$sNum/$vNum",
                    navigableSurahNumber = sNum,
                    navigableVerseNumber = vNum
                  )
                )
              }
            }
          }
        }
      }

      val status = if (directCitations.isNotEmpty()) {
        ClaimVerificationStatus.EXACT_SOURCE_FOUND
      } else {
        ClaimVerificationStatus.PARTIAL_MATCH_FOUND
      }

      val primaryTitle = (directCitations.firstOrNull() ?: thematicCitations.first()).narrationOrVerseIdentifier
      return@withContext ClaimVerificationResponse(
        queryText = query,
        detectedLanguage = detectedLang,
        status = status,
        headlineVerdict = if (status == ClaimVerificationStatus.EXACT_SOURCE_FOUND) {
          "Verified primary source match found: $primaryTitle."
        } else {
          "Partial or thematic match found in verified sources ($primaryTitle)."
        },
        directQuotations = directCitations,
        thematicCorroborations = thematicCitations,
        scholarlyAssessments = assessments.distinctBy { "${it.scholarName}|${it.citedWorkAndEdition}" },
        counterEvidenceOrNuance = listOf(
          "Always read individual narrations alongside related Quranic verses and chapter headings (tarajim al-abwab) to understand legal scope and context.",
          "Thematic Quran-Hadith links indicate shared legal or ethical principles rather than identical wording."
        ),
        commentaryAndSynthesisEnglish = "The query matches verified primary material in the indexed library ($primaryTitle). Verbatim scriptural quotations are shown above in their original Arabic with licensed English and Urdu translations, kept strictly separate from scholarly grading and commentary.",
        commentaryAndSynthesisUrdu = "یہ تلاش مستند ماخذ ($primaryTitle) سے مطابقت رکھتی ہے۔ اصل عربی متن، انگریزی اور اردو تراجم کو علمی درجہ بندی اور تشریح سے الگ رکھا گیا ہے تاکہ اصل ماخذ کا مفہوم مکمل طور پر محفوظ رہے۔",
        evidenceVsCommentaryNote = "Primary Scriptural Text (Matn / Ayah) is presented verbatim above. The scholarly grading and contextual synthesis below represent human scholarly analysis (Ijtihad & Takhrij).",
        furtherStudyLinks = listOf(
          VerifiedFurtherStudyResource(
            title = "Sunnah.com Canonical Hadith Reference",
            speakerOrAuthor = "Classical Muhaddithin Archive",
            originalPublisher = "Sunnah.com",
            resourceType = "Primary Reference Portal",
            officialUrl = "https://sunnah.com",
            licenceAndLinkingNote = "Verified external reference link permitted."
          ),
          VerifiedFurtherStudyResource(
            title = "Quran.com Verse & Tafsir Study",
            speakerOrAuthor = "Quran Foundation",
            originalPublisher = "Quran.com",
            resourceType = "Primary Quranic Portal",
            officialUrl = "https://quran.com",
            licenceAndLinkingNote = "Official Quran Foundation web study link."
          )
        ),
        workedOfflineOnly = isOffline
      )
    }

    // 3. No verified match found -> Return NO_VERIFIED_MATCH_IN_INDEXED_SOURCES (NEVER fabricate a citation or return "FALSE" merely because search returned no results)
    ClaimVerificationResponse(
      queryText = query,
      detectedLanguage = detectedLang,
      status = ClaimVerificationStatus.NO_VERIFIED_MATCH_IN_INDEXED_SOURCES,
      headlineVerdict = "No verified match found in the currently indexed sources. This question remains unresolved by the local indexed library.",
      directQuotations = emptyList(),
      thematicCorroborations = emptyList(),
      scholarlyAssessments = listOf(
        ScholarlyAssessmentEntry(
          scholarName = "Verification Safeguard",
          scholarlyTradition = "Epistemic Integrity Rule",
          assignedGrade = HadithGradeCategory.GRADING_NOT_VERIFIED,
          citedWorkAndEdition = "Bayan al-Huda Indexed Source Catalogue",
          reasoningSummary = "Absence of a search match in the local index does NOT automatically prove a report is fabricated ('FALSE'). Conversely, popular unverified speaker claims are never treated as authoritative without an authenticated primary source chain (isnad)."
        )
      ),
      counterEvidenceOrNuance = listOf(
        "Do not attribute an unverified quotation to the Prophet (ﷺ) until its collection, chapter, and chain of narrators are confirmed.",
        "If this claim comes from a social media post or unreferenced lecture, ask for the primary book, edition, and narration number."
      ),
      commentaryAndSynthesisEnglish = "The claim or quotation \"$query\" could not be verified in the currently indexed Quran, Sunni Hadith, or Twelver Shia Hadith collections. In accordance with Islamic scholarly ethics, Bayan al-Huda never fabricates replacement citations or chains of narrators, nor does it label a claim 'FALSE' solely because a search returned no results. The question remains unresolved by the currently indexed material.",
      commentaryAndSynthesisUrdu = "آپ کے درج کردہ قول یا سوال (\"$query\") کا فی الحال موجودہ اشاریہ شدہ قرآن و حدیث کے ذخیرے میں کوئی مصدقہ حوالہ نہیں ملا۔ علمی دیانت کے اصولوں کے مطابق محض تلاش میں نہ ملنے کی بنا پر کسی بات کو خودکار طور پر 'جھوٹ' (FALSE) قرار نہیں دیا جاتا اور نہ ہی کوئی فرضی حوالہ گھڑا جاتا ہے۔",
      evidenceVsCommentaryNote = "No primary quotation is displayed because no verified source match was confirmed.",
      furtherStudyLinks = listOf(
        VerifiedFurtherStudyResource(
          title = "Al-Dorar Al-Saniyyah Hadith Encyclopedia (الموسوعة الحديثية)",
          speakerOrAuthor = "Dorar Foundation",
          originalPublisher = "dorar.net",
          resourceType = "Takhrij Lookup Tool",
          officialUrl = "https://dorar.net/hadith",
          licenceAndLinkingNote = "Verified external reference for checking unindexed Arabic narrations."
        )
      ),
      workedOfflineOnly = isOffline
    )
  }

  private suspend fun searchLocalQuranVerses(query: String, normalizedArabic: String): List<AyahEntity> {
    val results = LinkedHashMap<String, AyahEntity>()
    val clean = query.trim()

    // Check explicit surah:ayah pattern e.g. "2:255" or "13:1"
    val refRegex = Regex("(\\d{1,3}):(\\d{1,3})")
    refRegex.find(clean)?.let { match ->
      val sNum = match.groupValues[1].toIntOrNull()
      val vNum = match.groupValues[2].toIntOrNull()
      if (sNum != null && vNum != null) {
        val exact = ayahDao.getAyah(sNum, vNum)
          ?: InitialDataLoader.getFoundationalAyahs().find { it.surahNumber == sNum && it.verseNumber == vNum }
        if (exact != null) {
          results["${exact.surahNumber}:${exact.verseNumber}"] = exact
        }
      }
    }

    val daoHits = runCatching { ayahDao.searchAyahsSnapshot(clean) }.getOrDefault(emptyList())
    for (hit in daoHits.take(6)) {
      results["${hit.surahNumber}:${hit.verseNumber}"] = hit
    }

    if (results.isEmpty()) {
      for (a in InitialDataLoader.getFoundationalAyahs()) {
        if (a.textArabic.contains(clean) ||
          (normalizedArabic.length >= 3 && normalizeArabicForMatch(a.textArabic).contains(normalizedArabic)) ||
          a.translationEnglish.contains(clean, ignoreCase = true) ||
          a.translationUrdu.contains(clean) ||
          a.transliteration.contains(clean, ignoreCase = true)
        ) {
          results["${a.surahNumber}:${a.verseNumber}"] = a
        }
      }
    }
    return results.values.toList()
  }

  private suspend fun searchLocalHadiths(query: String, normalizedArabic: String): List<HadithEntity> {
    val results = LinkedHashMap<String, HadithEntity>()
    val clean = query.trim()

    val daoHits = runCatching { hadithDao.searchHadithsSnapshot(clean) }.getOrDefault(emptyList())
    for (hit in daoHits.take(6)) {
      results["${hit.collectionKey}:${hit.hadithNumber}"] = hit
    }

    val allCatalogHadiths = ExtendedHadithDataProvider.getExtendedHadiths() + InitialDataLoader.getFoundationalHadiths()
    for (h in allCatalogHadiths) {
      if (h.arabicText.contains(clean) ||
        (normalizedArabic.length >= 3 && normalizeArabicForMatch(h.arabicText).contains(normalizedArabic)) ||
        h.englishText.contains(clean, ignoreCase = true) ||
        h.urduText.contains(clean) ||
        h.reference.contains(clean, ignoreCase = true) ||
        h.narrator.contains(clean, ignoreCase = true)
      ) {
        results["${h.collectionKey}:${h.hadithNumber}"] = h
      }
    }
    return results.values.toList()
  }

  private fun mapGradeStringToCategory(rawGrade: String): HadithGradeCategory {
    val g = rawGrade.lowercase()
    return when {
      g.contains("mawdu") || g.contains("fabricated") || g.contains("موضوع") -> HadithGradeCategory.MAWDU
      g.contains("da'if") || g.contains("daif") || g.contains("weak") || g.contains("ضعيف") -> HadithGradeCategory.DAIF
      g.contains("hasan") || g.contains("good") || g.contains("حسن") -> HadithGradeCategory.HASAN
      g.contains("sahih") || g.contains("muwathaq") || g.contains("authentic") || g.contains("صحيح") -> HadithGradeCategory.SAHIH
      else -> HadithGradeCategory.GRADING_NOT_VERIFIED
    }
  }

  /**
   * Curated high-precision scholarly case library covering all Priority 12 scenarios:
   * - Exact Hadith verification (Innama al-a'malu bin-niyyat, Fajr As-salatu khayrun minan-nawm)
   * - Disputed / Weak / Fabricated popular quotes ("Seek knowledge even unto China", "He who knows himself knows his Lord", "Differences of my Ummah are a mercy")
   * - Scholarly grading disagreements (Cave of Thawr spider & dove, Hadith of Mu'adh on ijtihad, Wiping over socks / combining prayers)
   * - Sunni & Twelver Shia primary sources clearly identified (Hadith al-Thaqalayn, Hadith al-Ghadir / Wilayah, Nahj al-Balagha & Al-Kafi vs Sahih Muslim & Tirmidhi)
   * - Historical claims requiring more evidence (Early historical conflicts, story of Tabuk/Karbala chronicler chains)
   */
  private fun findCuratedCaseMatch(
    query: String,
    detectedLang: String,
    isOffline: Boolean
  ): ClaimVerificationResponse? {
    val qLower = query.lowercase()
    val qNormAr = normalizeArabicForMatch(query)

    // CASE 1: Intentions Hadith (Innama al-a'malu bin-niyyat) — EXACT_SOURCE_FOUND
    if (qLower.contains("actions are judged by intentions") ||
      qLower.contains("actions are but by intentions") ||
      qLower.contains("innama al-a'mal") ||
      qLower.contains("innamal a'mal") ||
      qNormAr.contains("انما الاعمال بالنيات") ||
      query.contains("اعمال کا دارومدار نیتوں پر ہے")
    ) {
      return ClaimVerificationResponse(
        queryText = query,
        detectedLanguage = detectedLang,
        status = ClaimVerificationStatus.EXACT_SOURCE_FOUND,
        headlineVerdict = "EXACT SOURCE FOUND: Muttafaqun 'Alayh (Agreed Upon) in Sahih al-Bukhari #1 and Sahih Muslim #1907.",
        directQuotations = listOf(
          PrimarySourceCitation(
            citationId = "hadith_bukhari_1",
            sourceType = "HADITH_SUNNI",
            traditionBadge = "Sunni (Kutub al-Sittah • Sahih al-Bukhari)",
            collectionTitle = "Sahih al-Bukhari",
            editionTitle = "Darussalam Edition #1 • Book 1 (Revelation), Hadith 1",
            chapterOrBookLabel = "Book 1: How the Divine Revelation Started (بدء الوحي), Chapter 1",
            narrationOrVerseIdentifier = "Sahih al-Bukhari 1",
            originalArabicText = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى، فَمَنْ كَانَتْ هِجْرَتُهُ إِلَى دُنْيَا يُصِيبُهَا أَوْ إِلَى امْرَأَةٍ يَنْكِحُهَا فَهِجْرَتُهُ إِلَى مَا هَاجَرَ إِلَيْهِ",
            englishTranslation = "Actions are judged by intentions, and every person shall have what they intended. So whoever emigrated for worldly benefits or for a woman to marry, his emigration is for what he emigrated for.",
            urduTranslation = "اعمال کا دارومدار نیتوں پر ہے اور ہر شخص کے لیے وہی ہے جس کی اس نے نیت کی۔ پس جس کی ہجرت دنیا حاصل کرنے یا کسی عورت سے نکاح کرنے کے لیے ہو تو اس کی ہجرت اسی طرف ہے جس کے لیے اس نے ہجرت کی۔",
            transliteration = "Innama al-a'malu bin-niyyat, wa innama li-kulli imri'in ma nawa...",
            narratorOrIsnad = "Yahya ibn Sa'id al-Ansari -> Muhammad ibn Ibrahim al-Taymi -> 'Alqamah ibn Waqqas al-Laythi -> 'Umar ibn al-Khattab (ra)",
            relationshipType = "DIRECT_PRIMARY_QUOTATION",
            officialVerificationUrl = "https://sunnah.com/bukhari:1",
            navigableCollectionKey = "bukhari",
            navigableHadithNumber = 1
          ),
          PrimarySourceCitation(
            citationId = "hadith_alkafi_niyyah",
            sourceType = "HADITH_SHIA",
            traditionBadge = "Twelver Shia (Kutub al-Arba'ah • Al-Kafi)",
            collectionTitle = "Kitab al-Kafi",
            editionTitle = "Dar al-Kutub al-Islamiyyah • Vol. 2, Book of Faith and Disbelief, Chapter on Intention (باب النية)",
            chapterOrBookLabel = "Usul al-Kafi, Book of Iman and Kufr, Chapter on Niyyah",
            narrationOrVerseIdentifier = "Al-Kafi 2:84 #1",
            originalArabicText = "إِنَّمَا قَدَّرَ اللَّهُ عَزَّ وَجَلَّ عَوْنَ الْعَبْدِ عَلَى قَدْرِ نِيَّتِهِ فَمَنْ تَمَّتْ نِيَّتُهُ تَمَّ عَوْنُ اللَّهِ لَهُ",
            englishTranslation = "Indeed, Allah the Mighty and Majestic proportions His aid to a servant according to the measure of his intention; whoever's intention is complete, Allah's aid to him is complete.",
            urduTranslation = "بے شک اللہ عزوجل بندے کی مدد اس کی نیت کے مطابق فرماتا ہے، پس جس کی نیت کامل ہو اس کے لیے اللہ کی مدد بھی کامل ہوتی ہے۔",
            narratorOrIsnad = "Imam Ja'far al-Sadiq (as)",
            relationshipType = "THEMATIC_CORROBORATION",
            officialVerificationUrl = "https://thaqalayn.net",
            navigableCollectionKey = "alkafi",
            navigableHadithNumber = 1
          )
        ),
        thematicCorroborations = listOf(
          PrimarySourceCitation(
            citationId = "quran_98_5",
            sourceType = "QURAN",
            traditionBadge = "Universal Quran (Thematic Corroboration)",
            collectionTitle = "Al-Qur'an al-Kareem",
            editionTitle = "Uthmani Script • Surah Al-Bayyinah 98:5",
            chapterOrBookLabel = "Surah Al-Bayyinah (98), Ayah 5",
            narrationOrVerseIdentifier = "Quran 98:5",
            originalArabicText = "وَمَا أُمِرُوا إِلَّا لِيَعْبُدُوا اللَّهَ مُخْلِصِينَ لَهُ الدِّينَ حُنَفَاءَ",
            englishTranslation = "And they were not commanded except to worship Allah, [being] sincere to Him in religion, inclining to truth.",
            urduTranslation = "اور انہیں اس کے سوا کوئی حکم نہیں دیا گیا کہ صرف اللہ کی عبادت کریں اس کے لیے دین کو خالص کرتے ہوئے۔",
            transliteration = "Wa ma umiru illa li-ya'budullaha mukhlisina lahud-dina hunafa'",
            narratorOrIsnad = "Thematic Quranic Foundation on Sincerity (Ikhlas)",
            relationshipType = "THEMATIC_CORROBORATION",
            officialVerificationUrl = "https://quran.com/98/5",
            navigableSurahNumber = 98,
            navigableVerseNumber = 5
          )
        ),
        scholarlyAssessments = listOf(
          ScholarlyAssessmentEntry(
            scholarName = "Imam Muhammad ibn Isma'il al-Bukhari (d. 256 AH)",
            scholarlyTradition = "Sunni Hadith Criticism",
            assignedGrade = HadithGradeCategory.SAHIH,
            citedWorkAndEdition = "Sahih al-Bukhari, Hadith #1 (Darussalam Edition)",
            reasoningSummary = "Placed as the opening narration of Sahih al-Bukhari; authenticated with an unbroken Medinan chain from 'Umar ibn al-Khattab (ra) and massive downstream tawatur from Yahya ibn Sa'id al-Ansari."
          ),
          ScholarlyAssessmentEntry(
            scholarName = "Imam Muslim ibn al-Hajjaj (d. 261 AH) & Imam al-Nawawi (d. 676 AH)",
            scholarlyTradition = "Sunni Hadith & Shafi'i Jurisprudence",
            assignedGrade = HadithGradeCategory.SAHIH,
            citedWorkAndEdition = "Sahih Muslim #1907 & Al-Arba'in al-Nawawiyyah #1",
            reasoningSummary = "Imam al-Shafi'i and Imam Ahmad noted that this hadith constitutes one-third of sacred knowledge because human acts are performed by heart, tongue, and limbs, and intention governs all three."
          )
        ),
        counterEvidenceOrNuance = listOf(
          "A good intention (niyyah salihah) is necessary for an act of worship to be accepted, but a good intention cannot transform a prohibited (haram) action into a permissible one.",
          "Notice that Quran 98:5 is presented under Thematic Corroboration — it shares the principle of sincerity (ikhlas) but is a Quranic verse, not a verbatim quotation of the hadith."
        ),
        commentaryAndSynthesisEnglish = "This narration is one of the foundational axes of Islamic ethics and jurisprudence. The verbatim prophetic statement is preserved in Sahih al-Bukhari (#1) and Sahih Muslim (#1907), while Quran 98:5 provides the thematic Quranic foundation on sincere devotion (ikhlas).",
        commentaryAndSynthesisUrdu = "یہ حدیث اسلامی اخلاقیات اور فقہ کے بنیادی ستونوں میں سے ہے۔ اس کا اصل متن صحیح بخاری (حدیث 1) اور صحیح مسلم (حدیث 1907) میں کامل سند کے ساتھ محفوظ ہے، جبکہ سورہ البینہ (98:5) اخلاصِ نیت کی قرآنی بنیاد فراہم کرتی ہے۔",
        evidenceVsCommentaryNote = "Direct Quotation: Sahih al-Bukhari #1 is the verbatim prophetic report. Scholarly Explanation: The remark that this hadith is 'one-third of knowledge' is a scholar's commentary (by Imam al-Shafi'i), not part of the Prophet's words.",
        furtherStudyLinks = listOf(
          VerifiedFurtherStudyResource(
            title = "Jami' al-'Ulum wal-Hikam — Commentary on Hadith #1",
            speakerOrAuthor = "Ibn Rajab al-Hanbali (d. 795 AH)",
            originalPublisher = "Mu'assasat al-Risalah",
            resourceType = "Classical Treatise",
            officialUrl = "https://sunnah.com/nawawi40:1",
            licenceAndLinkingNote = "Public domain classical commentary link."
          )
        ),
        workedOfflineOnly = isOffline
      )
    }

    // CASE 2: "Seek knowledge even unto China" — ATTRIBUTION_DISPUTED / Scholarly Grading Disagreement
    if (qLower.contains("china") ||
      qLower.contains("اطلبوا العلم ولو بالصين") ||
      qNormAr.contains("اطلبوا العلم ولو بالصين") ||
      query.contains("علم حاصل کرو چاہے چین")
    ) {
      return ClaimVerificationResponse(
        queryText = query,
        detectedLanguage = detectedLang,
        status = ClaimVerificationStatus.ATTRIBUTION_DISPUTED,
        headlineVerdict = "ATTRIBUTION DISPUTED: Widely quoted saying on seeking knowledge; classical muhaddithin grade its prophetic chain as weak/fabricated, though a minority graded it hasan li-ghayrihi.",
        directQuotations = listOf(
          PrimarySourceCitation(
            citationId = "shuab_al_iman_china",
            sourceType = "HADITH_SUNNI",
            traditionBadge = "Sunni Secondary Compilation (Shu'ab al-Iman)",
            collectionTitle = "Shu'ab al-Iman (al-Bayhaqi) & Kitab al-Madkhal",
            editionTitle = "Shu'ab al-Iman, Vol. 2, #1543 (Bab fi Talab al-'Ilm)",
            chapterOrBookLabel = "Chapter on Seeking Knowledge",
            narrationOrVerseIdentifier = "Shu'ab al-Iman #1543",
            originalArabicText = "اطْلُبُوا الْعِلْمَ وَلَوْ بِالصِّينِ، فَإِنَّ طَلَبَ الْعِلْمِ فَرِيضَةٌ عَلَى كُلِّ مُسْلِمٍ",
            englishTranslation = "Seek knowledge even if in China, for indeed seeking knowledge is an obligation upon every Muslim.",
            urduTranslation = "علم حاصل کرو خواہ چین ہی میں کیوں نہ ہو، کیونکہ علم حاصل کرنا ہر مسلمان پر فرض ہے۔",
            transliteration = "Utlubu al-'ilma wa law bis-Sin, fa-inna talaba al-'ilmi faridatun 'ala kulli muslim.",
            narratorOrIsnad = "Reported via Abu 'Atikah Tarif ibn Salman from Anas ibn Malik (ra) — Abu 'Atikah is impugned by hadith critics.",
            relationshipType = "DIRECT_PRIMARY_QUOTATION",
            officialVerificationUrl = "https://sunnah.com/ibnmajah:224",
            navigableCollectionKey = "ibnmajah",
            navigableHadithNumber = 224
          )
        ),
        thematicCorroborations = listOf(
          PrimarySourceCitation(
            citationId = "ibnmajah_224_authentic_part",
            sourceType = "HADITH_SUNNI",
            traditionBadge = "Sunni (Kutub al-Sittah • Sunan Ibn Majah)",
            collectionTitle = "Sunan Ibn Majah",
            editionTitle = "Darussalam #224 • Book 1 (The Sunnah), Hadith 224",
            chapterOrBookLabel = "Chapter on Virtues of the Scholars and Encouragement to Seek Knowledge",
            narrationOrVerseIdentifier = "Sunan Ibn Majah 224",
            originalArabicText = "طَلَبُ الْعِلْمِ فَرِيضَةٌ عَلَى كُلِّ مُسْلِمٍ",
            englishTranslation = "Seeking knowledge is an obligation upon every Muslim.",
            urduTranslation = "علم حاصل کرنا ہر مسلمان پر فرض ہے۔",
            transliteration = "Talabu al-'ilmi faridatun 'ala kulli muslim.",
            narratorOrIsnad = "Anas ibn Malik (ra) — authenticated without the 'even in China' addition.",
            relationshipType = "THEMATIC_CORROBORATION",
            officialVerificationUrl = "https://sunnah.com/ibnmajah:224",
            navigableCollectionKey = "ibnmajah",
            navigableHadithNumber = 224
          )
        ),
        scholarlyAssessments = listOf(
          ScholarlyAssessmentEntry(
            scholarName = "Imam al-Bayhaqi (d. 458 AH)",
            scholarlyTradition = "Sunni Hadith Criticism",
            assignedGrade = HadithGradeCategory.DAIF,
            citedWorkAndEdition = "Shu'ab al-Iman #1543 & Al-Madkhal ila al-Sunan",
            reasoningSummary = "Stated: 'Its matn (meaning regarding seeking knowledge) is famous, but its isnad (chain for the China clause) is weak (asaneeduhu da'ifah).'"
          ),
          ScholarlyAssessmentEntry(
            scholarName = "Ibn Hibban (d. 354 AH) & Ibn al-Jawzi (d. 597 AH)",
            scholarlyTradition = "Jarh wa Ta'dil (Narrator Criticism)",
            assignedGrade = HadithGradeCategory.MAWDU,
            citedWorkAndEdition = "Kitab al-Majruhin (Ibn Hibban) & Kitab al-Mawdu'at (Ibn al-Jawzi Vol. 1, p. 215)",
            reasoningSummary = "Graded the 'even in China' addition as baseless/fabricated due to Abu 'Atikah Tarif ibn Salman, whom Imam Ahmad, al-Bukhari, and al-Nasa'i declared matruk/munkar al-hadith."
          ),
          ScholarlyAssessmentEntry(
            scholarName = "Imam al-Suyuti (d. 911 AH) & al-Mizzi",
            scholarlyTradition = "Late Classical Synthesis",
            assignedGrade = HadithGradeCategory.DAIF,
            citedWorkAndEdition = "Al-La'ali al-Masnu'ah fi al-Ahadith al-Mawdu'ah",
            reasoningSummary = "Argued that while individual chains of the 'even in China' clause are weak, multiple pathways exist, whereas the second half ('Seeking knowledge is an obligation upon every Muslim' in Sunan Ibn Majah #224) is authentic."
          )
        ),
        counterEvidenceOrNuance = listOf(
          "Distinguish the two halves of the quote: (1) 'Seeking knowledge is an obligation upon every Muslim' (Sunan Ibn Majah #224) is sound and supported by numerous routes; (2) 'even unto China' has a severely criticized chain.",
          "Islam strongly encourages travelling for beneficial knowledge (rihlah fi talab al-'ilm), as proven in Sahih Muslim #2699 ('Whoever treads a path in search of knowledge, Allah makes easy for him a path to Paradise')."
        ),
        commentaryAndSynthesisEnglish = "The popular phrase 'Seek knowledge even unto China' illustrates why careful takhrij matters: the addition 'even in China' relies on impugned narrator Abu 'Atikah and is rejected by Ibn Hibban and Ibn al-Jawzi, and graded weak by al-Bayhaqi. However, the core statement 'Seeking knowledge is an obligation upon every Muslim' (Sunan Ibn Majah #224) is authentic.",
        commentaryAndSynthesisUrdu = "مشہور مقولہ 'علم حاصل کرو خواہ چین جانا پڑے' اس بات کی عمدہ مثال ہے کہ تحقیقِ حدیث کیوں ضروری ہے: 'خواہ چین ہی میں ہو' کے الفاظ کی سند میں ابو عاتکہ طریف بن سلمان منکر الحدیث راوی ہے جس کی وجہ سے ابن حبان اور ابن الجوزی نے اس اضافے کو موضوع/باطل اور امام بیہقی نے ضعیف قرار دیا، جبکہ دوسرا حصہ 'علم حاصل کرنا ہر مسلمان پر فرض ہے' (سنن ابن ماجہ 224) متعدد طرق سے ثابت ہے۔",
        evidenceVsCommentaryNote = "Primary Text vs Scholar's Ruling: The words of Sunan Ibn Majah #224 are primary narration; the evaluations of Abu 'Atikah's reliability by al-Bukhari, Ibn Hibban, and al-Bayhaqi are scholarly Jarh wa Ta'dil assessments.",
        furtherStudyLinks = listOf(
          VerifiedFurtherStudyResource(
            title = "Sunan Ibn Majah #224 — Obligation of Seeking Knowledge",
            speakerOrAuthor = "Imam Ibn Majah al-Qazwini",
            originalPublisher = "Sunnah.com",
            resourceType = "Primary Hadith Text",
            officialUrl = "https://sunnah.com/ibnmajah:224",
            licenceAndLinkingNote = "Verified canonical link."
          )
        ),
        workedOfflineOnly = isOffline
      )
    }

    // CASE 3: "He who knows himself knows his Lord" (Man 'arafa nafsahu faqad 'arafa Rabbahu) — ATTRIBUTION_DISPUTED / MAWDU as Prophetic Hadith
    if (qLower.contains("knows himself knows his lord") ||
      qLower.contains("man arafa nafsahu") ||
      qNormAr.contains("من عرف نفسه فقد عرف ربه") ||
      query.contains("جس نے اپنے نفس کو پہچانا")
    ) {
      return ClaimVerificationResponse(
        queryText = query,
        detectedLanguage = detectedLang,
        status = ClaimVerificationStatus.ATTRIBUTION_DISPUTED,
        headlineVerdict = "ATTRIBUTION DISPUTED: Not an authentic Prophetic Hadith (لا أصل له مرفوعاً); documented as an early ascetic/philosophical maxim or attributed in Shia sources to Imam 'Ali (as) as wisdom.",
        directQuotations = listOf(
          PrimarySourceCitation(
            citationId = "maxim_man_arafa_nafsahu",
            sourceType = "HISTORICAL_CHRONICLE",
            traditionBadge = "Ascetic & Ethical Maxim (حكمة مأثورة)",
            collectionTitle = "Al-Maqasid al-Hasanah (al-Sakhawi) & Ghurar al-Hikam (al-Amidi)",
            editionTitle = "Al-Maqasid al-Hasanah #1149 / Hilyat al-Awliya (Yahya ibn Mu'adh al-Razi)",
            chapterOrBookLabel = "Section on Popular Sayings on the Tongue",
            narrationOrVerseIdentifier = "Al-Maqasid al-Hasanah #1149",
            originalArabicText = "مَنْ عَرَفَ نَفْسَهُ فَقَدْ عَرَفَ رَبَّهُ",
            englishTranslation = "Whoever knows himself (his own contingent weakness and need) knows his Lord (in His absolute perfection and self-sufficiency).",
            urduTranslation = "جس نے اپنے نفس (اپنی عاجزی اور محتاجی) کو پہچانا اس نے اپنے رب (کی عظمت و بے نیازی) کو پہچان لیا۔",
            transliteration = "Man 'arafa nafsahu faqad 'arafa Rabbahu.",
            narratorOrIsnad = "No Prophetic Isnad (La asla lahu marfu'an); reported as a statement of Yahya ibn Mu'adh al-Razi (d. 258 AH) and in Ghurar al-Hikam attributed to Imam 'Ali (as).",
            relationshipType = "DIRECT_PRIMARY_QUOTATION",
            officialVerificationUrl = "https://quran.com/59/19",
            navigableSurahNumber = 59,
            navigableVerseNumber = 19
          )
        ),
        thematicCorroborations = listOf(
          PrimarySourceCitation(
            citationId = "quran_59_19",
            sourceType = "QURAN",
            traditionBadge = "Universal Quran (Thematic Corroboration)",
            collectionTitle = "Al-Qur'an al-Kareem",
            editionTitle = "Uthmani Script • Surah Al-Hashr 59:19",
            chapterOrBookLabel = "Surah Al-Hashr (59), Ayah 19",
            narrationOrVerseIdentifier = "Quran 59:19",
            originalArabicText = "وَلَا تَكُونُوا كَالَّذِينَ نَسُوا اللَّهَ فَأَنسَاهُمْ أَنفُسَهُمْ ۚ أُولَٰئِكَ هُمُ الْفَاسِقُونَ",
            englishTranslation = "And do not be like those who forgot Allah, so He made them forget themselves. Those are the defiantly disobedient.",
            urduTranslation = "اور ان لوگوں کی طرح نہ ہو جاؤ جنہوں نے اللہ کو بھلا دیا تو اللہ نے انہیں خود ان کی اپنی جانوں سے غافل کر دیا۔",
            transliteration = "Wa la takunu kalladhina nasullaha fa-ansahum anfusahum...",
            narratorOrIsnad = "Divine Revelation contrasting forgetting Allah with forgetting oneself",
            relationshipType = "THEMATIC_CORROBORATION",
            officialVerificationUrl = "https://quran.com/59/19",
            navigableSurahNumber = 59,
            navigableVerseNumber = 19
          )
        ),
        scholarlyAssessments = listOf(
          ScholarlyAssessmentEntry(
            scholarName = "Imam al-Nawawi (d. 676 AH)",
            scholarlyTradition = "Sunni Hadith & Fiqh",
            assignedGrade = HadithGradeCategory.MAWDU,
            citedWorkAndEdition = "Fatawa al-Nawawi (Al-Masa'il al-Manthurah), p. 164",
            reasoningSummary = "Stated clearly: 'It is not established as a Prophetic hadith (laysa bi-thabit).' Attributing it directly to the Prophet (ﷺ) is erroneous."
          ),
          ScholarlyAssessmentEntry(
            scholarName = "Imam Shams al-Din al-Sakhawi (d. 902 AH) & Abu al-Muzaffar ibn al-Sam'ani",
            scholarlyTradition = "Hadith Verification (Takhrij)",
            assignedGrade = HadithGradeCategory.MAWDU,
            citedWorkAndEdition = "Al-Maqasid al-Hasanah fi Bayan Kathir min al-Ahadith al-Mushtahirah #1149",
            reasoningSummary = "Identified it as a statement of the early scholar Yahya ibn Mu'adh al-Razi rather than a marfu' prophetic narration."
          ),
          ScholarlyAssessmentEntry(
            scholarName = "Abd al-Wahid al-Amidi (d. 510 AH) / Twelver Shia Scholarship",
            scholarlyTradition = "Twelver Shia Wisdom Literature",
            assignedGrade = HadithGradeCategory.GRADING_NOT_VERIFIED,
            citedWorkAndEdition = "Ghurar al-Hikam wa Durar al-Kalim",
            reasoningSummary = "Transmitted in mursal form (without continuous chain) as a philosophical aphorism of Imam 'Ali (as); Shia rijal scholars note that Ghurar al-Hikam is a collection of mursal aphorisms requiring corroboration for formal istidlal."
          )
        ),
        counterEvidenceOrNuance = listOf(
          "This example demonstrates Learning Module #6 & #7: A statement can have a true or spiritually meaningful interpretation (recognizing human frailty leads to recognizing Divine majesty, cf. Quran 59:19) while STILL being invalid to attribute to the Prophet Muhammad (ﷺ) as a Hadith.",
          "When quoting it, scholars state 'It is said in wisdom (qila fi al-hikmah)' rather than 'The Prophet (ﷺ) said'."
        ),
        commentaryAndSynthesisEnglish = "While frequently cited in spiritual discourses, 'He who knows himself knows his Lord' has no chain of narration back to the Prophet (ﷺ). Hadith masters including al-Nawawi, Ibn Taymiyyah, and al-Sakhawi confirmed it is not a prophetic hadith, tracing it instead to Yahya ibn Mu'adh al-Razi.",
        commentaryAndSynthesisUrdu = "یہ جملہ ('من عرف نفسه فقد عرف ربه') اکثر تقاریر اور تصوف کی کتابوں میں حدیث کے طور پر پیش کیا جاتا ہے، لیکن امام نووی، ابن تیمیہ اور امام سخاوی نے تصریح کی ہے کہ یہ رسول اللہ ﷺ کی مرفوع حدیث ہرگز نہیں ہے بلکہ یحییٰ بن معاذ رازی وغیرہ کا قولِ حکمت ہے۔",
        evidenceVsCommentaryNote = "Distinction: The spiritual reflection may be discussed as human commentary, but it must never be cited as a Prophetic Hadith.",
        furtherStudyLinks = listOf(
          VerifiedFurtherStudyResource(
            title = "Surah Al-Hashr 59:19 — Remembering Allah and Self-Knowledge",
            speakerOrAuthor = "Quran Foundation",
            originalPublisher = "Quran.com",
            resourceType = "Quranic Reference",
            officialUrl = "https://quran.com/59/19",
            licenceAndLinkingNote = "Verified Quran link."
          )
        ),
        workedOfflineOnly = isOffline
      )
    }

    // CASE 4: Cave of Thawr Spider Web & Dove / Historical & Grading Disagreement
    if (qLower.contains("spider") ||
      qLower.contains("cave of thawr") ||
      qLower.contains("ghar thawr") ||
      qNormAr.contains("عنكبوت") ||
      query.contains("غار ثور") ||
      query.contains("مکڑی")
    ) {
      return ClaimVerificationResponse(
        queryText = query,
        detectedLanguage = detectedLang,
        status = ClaimVerificationStatus.SCHOLARLY_GRADING_DISAGREEMENT,
        headlineVerdict = "SCHOLARLY GRADING DISAGREEMENT: The Sanctuary in the Cave is established in Quran 9:40; the specific detail of the spider web and two doves in Musnad Ahmad #3251 has differing grades among major muhaddithin.",
        directQuotations = listOf(
          PrimarySourceCitation(
            citationId = "quran_9_40_cave",
            sourceType = "QURAN",
            traditionBadge = "Universal Quran (Mutawatir Primary Evidence)",
            collectionTitle = "Al-Qur'an al-Kareem",
            editionTitle = "Uthmani Script • Surah At-Tawbah 9:40",
            chapterOrBookLabel = "Surah At-Tawbah (9), Ayah 40",
            narrationOrVerseIdentifier = "Quran 9:40",
            originalArabicText = "إِذْ هُمَا فِي الْغَارِ إِذْ يَقُولُ لِصَاحِبِهِ لَا تَحْزَنْ إِنَّ اللَّهَ مَعَنَا ۖ فَأَنزَلَ اللَّهُ سَكِينَتَهُ عَلَيْهِ وَأَيَّدَهُ بِجُنُودٍ لَّمْ تَرَوْهَا",
            englishTranslation = "When they were in the cave and he said to his companion, 'Do not grieve; indeed Allah is with us.' And Allah sent down His tranquillity upon him and supported him with soldiers [angels] you did not see.",
            urduTranslation = "جب وہ دونوں غار میں تھے، جب وہ اپنے ساتھی سے کہہ رہے تھے کہ غم نہ کرو بے شک اللہ ہمارے ساتھ ہے، پس اللہ نے ان پر اپنی تسکین نازل فرمائی اور ایسے لشکروں سے مدد فرمائی جنہیں تم نے نہیں دیکھا۔",
            transliteration = "Idh huma fil-ghari idh yaqulu li-sahibihi la tahzan innallaha ma'ana...",
            narratorOrIsnad = "Mutawatir Quranic Text",
            relationshipType = "DIRECT_PRIMARY_QUOTATION",
            officialVerificationUrl = "https://quran.com/9/40",
            navigableSurahNumber = 9,
            navigableVerseNumber = 40
          ),
          PrimarySourceCitation(
            citationId = "musnad_ahmad_3251_spider",
            sourceType = "HADITH_SUNNI",
            traditionBadge = "Sunni Musnad Compilation (Musnad Ahmad)",
            collectionTitle = "Musnad Imam Ahmad ibn Hanbal",
            editionTitle = "Mu'assasat al-Risalah Edition #3251 (Musnad Ibn 'Abbas)",
            chapterOrBookLabel = "Musnad Abdullah ibn 'Abbas (ra)",
            narrationOrVerseIdentifier = "Musnad Ahmad 3251",
            originalArabicText = "فَأَمَرَ اللَّهُ عَنْكَبُوتًا فَنَسَجَتْ بِوَجْهِ الغَارِ، وَأَمَرَ اللَّهُ شَجَرَةً فَنَبَتَتْ بِوَجْهِهِ، وَأَمَرَ حَمَامَتَيْنِ وَحْشِيَّتَيْنِ فَوَقَفَتَا بِفَمِ الغَارِ",
            englishTranslation = "Allah commanded a spider and it wove its web across the entrance of the cave, and commanded a tree and two wild doves at the mouth of the cave...",
            urduTranslation = "پھر اللہ نے مکڑی کو حکم دیا تو اس نے غار کے دہانے پر جالا تن دیا اور دو جنگلی کبوتریوں کو حکم دیا تو وہ غار کے منہ پر بیٹھ گئیں...",
            narratorOrIsnad = "'Uthman al-Jazari -> Miqsam -> Ibn 'Abbas (ra)",
            relationshipType = "DIRECT_PRIMARY_QUOTATION",
            officialVerificationUrl = "https://sunnah.com/ahmad",
            navigableCollectionKey = "ahmad",
            navigableHadithNumber = 3251
          )
        ),
        thematicCorroborations = emptyList(),
        scholarlyAssessments = listOf(
          ScholarlyAssessmentEntry(
            scholarName = "Hafiz Ibn Hajar al-'Asqalani (d. 852 AH) & Hafiz Ibn Kathir (d. 774 AH)",
            scholarlyTradition = "Classical Hadith & Sirah Verification",
            assignedGrade = HadithGradeCategory.HASAN,
            citedWorkAndEdition = "Fath al-Bari (Vol. 7, p. 236) & Al-Bidayah wa al-Nihayah (Vol. 3, p. 181)",
            reasoningSummary = "Graded the report's chain in Musnad Ahmad (#3251) as Hasan (Good/Sound) and noted corroborating mursal reports in Musannaf 'Abd al-Razzaq."
          ),
          ScholarlyAssessmentEntry(
            scholarName = "Shaykh Shu'ayb al-Arna'ut & Shaykh Nasir al-Din al-Albani",
            scholarlyTradition = "Contemporary Hadith Criticism",
            assignedGrade = HadithGradeCategory.DAIF,
            citedWorkAndEdition = "Tahqiq Musnad Ahmad #3251 & Silsilat al-Ahadith al-Da'ifah #1129",
            reasoningSummary = "Graded the specific chain weak (Da'if) due to narrator 'Uthman ibn 'Amr al-Jazari and weakens in the corroborating mursal pathways."
          )
        ),
        counterEvidenceOrNuance = listOf(
          "What is 100% certain (Qat'i) is Quran 9:40: the Prophet (ﷺ) and Abu Bakr (ra) took refuge in the Cave during Hijrah, and Allah protected them with unseen soldiers and sakina.",
          "Whether the physical means included the spider web and doves is a classic example of scholarly grading disagreement (Hasan according to Ibn Hajar & Ibn Kathir; Da'if according to al-Albani & al-Arna'ut)."
        ),
        commentaryAndSynthesisEnglish = "This case demonstrates respectful handling of scholarly grading disagreement: the core event of divine protection in the Cave of Thawr is established by Quran 9:40. Regarding the specific detail of the spider and two doves in Musnad Ahmad #3251, classical masters Ibn Hajar and Ibn Kathir graded its chain Hasan, while contemporary critics Shu'ayb al-Arna'ut and al-Albani graded it Da'if due to 'Uthman al-Jazari.",
        commentaryAndSynthesisUrdu = "یہ مسئلہ محدثین کے درمیان علمی درجہ بندی کے اختلاف کی واضح مثال ہے: غارِ ثور میں الٰہی حفاظت اور سکینت کا نزول سورہ التوبہ (9:40) سے قطعی طور پر ثابت ہے۔ البتہ مسند احمد (3251) میں مکڑی کے جالے اور کبوتریوں کی تفصیل کی سند کو حافظ ابن حجر عسقلانی اور ابن کثیر نے حسن قرار دیا ہے، جبکہ شعیب ارناؤط اور البانی نے عثمان الجزری کے سبب اس سند کو ضعیف شمار کیا ہے۔",
        evidenceVsCommentaryNote = "Quran 9:40 is definitive revelation. Musnad Ahmad #3251 is a historical/hadith narration whose chain is evaluated differently by Ibn Hajar (Hasan) and al-Arna'ut (Da'if).",
        furtherStudyLinks = listOf(
          VerifiedFurtherStudyResource(
            title = "Surah At-Tawbah 9:40 — Tafsir Ibn Kathir",
            speakerOrAuthor = "Hafiz Isma'il ibn Kathir",
            originalPublisher = "Quran.com",
            resourceType = "Classical Tafsir",
            officialUrl = "https://quran.com/9/40/tafsirs/en-tafisr-ibn-kathir",
            licenceAndLinkingNote = "Verified external tafsir reference."
          )
        ),
        workedOfflineOnly = isOffline
      )
    }

    // CASE 5: Hadith al-Thaqalayn / Ahl al-Bayt & Sahaba (Sunni & Shia Primary Sources clearly identified)
    if (qLower.contains("thaqalayn") ||
      qLower.contains("ahl al-bayt") ||
      qLower.contains("ahlulbayt") ||
      qLower.contains("two weighty things") ||
      qNormAr.contains("ثقلين") ||
      qNormAr.contains("اهل بيتي") ||
      query.contains("اہل بیت") ||
      query.contains("ثقلین")
    ) {
      return ClaimVerificationResponse(
        queryText = query,
        detectedLanguage = detectedLang,
        status = ClaimVerificationStatus.EXACT_SOURCE_FOUND,
        headlineVerdict = "EXACT SOURCE FOUND: Hadith al-Thaqalayn is authenticated across both Sunni (Sahih Muslim #2408, Sunan al-Tirmidhi #3786) and Twelver Shia (Al-Kafi Vol. 1, p. 294) canonical collections.",
        directQuotations = listOf(
          PrimarySourceCitation(
            citationId = "muslim_2408_thaqalayn",
            sourceType = "HADITH_SUNNI",
            traditionBadge = "Sunni (Kutub al-Sittah • Sahih Muslim)",
            collectionTitle = "Sahih Muslim",
            editionTitle = "Darussalam #2408a • Book 44 (The Merits of the Companions), Hadith 55",
            chapterOrBookLabel = "Chapter: Of the Virtues of 'Ali ibn Abi Talib (ra)",
            narrationOrVerseIdentifier = "Sahih Muslim 2408",
            originalArabicText = "وَأَنَا تَارِكٌ فِيكُمْ ثَقَلَيْنِ: أَوَّلُهُمَا كِتَابُ اللَّهِ فِيهِ الْهُدَى وَالنُّورُ فَخُذُوا بِكِتَابِ اللَّهِ وَاسْتَمْسِكُوا بِهِ... وَأَهْلُ بَيْتِي أُذَكِّرُكُمُ اللَّهَ فِي أَهْلِ بَيْتِي أُذَكِّرُكُمُ اللَّهَ فِي أَهْلِ بَيْتِي",
            englishTranslation = "And I am leaving among you two weighty things: the first of them is the Book of Allah, in which is guidance and light, so hold fast to the Book of Allah... and my Ahl al-Bayt (Household); I remind you of Allah regarding my Ahl al-Bayt, I remind you of Allah regarding my Ahl al-Bayt.",
            urduTranslation = "اور میں تمہارے درمیان دو بھاری چیزیں چھوڑے جا رہا ہوں: ان میں سے پہلی اللہ کی کتاب ہے جس میں ہدایت اور نور ہے، پس اللہ کی کتاب کو مضبوطی سے تھامو۔۔۔ اور میرے اہلِ بیت، میں تمہیں اپنے اہلِ بیت کے بارے میں اللہ کو یاد دلاتا ہوں۔",
            transliteration = "Wa ana tarikun fikum thaqalayn: awwaluhuma Kitabullah fihi al-huda wa al-nur... wa Ahlu Bayti udhakkirukumullaha fi Ahli Bayti.",
            narratorOrIsnad = "Zayd ibn Arqam (ra) at Ghadir Khumm",
            relationshipType = "DIRECT_PRIMARY_QUOTATION",
            officialVerificationUrl = "https://sunnah.com/muslim:2408a",
            navigableCollectionKey = "muslim",
            navigableHadithNumber = 2408
          ),
          PrimarySourceCitation(
            citationId = "alkafi_thaqalayn_1_294",
            sourceType = "HADITH_SHIA",
            traditionBadge = "Twelver Shia (Kutub al-Arba'ah • Kitab al-Kafi)",
            collectionTitle = "Kitab al-Kafi",
            editionTitle = "Usul al-Kafi, Vol. 1, Book of Al-Hujjah, p. 294",
            chapterOrBookLabel = "Kitab al-Hujjah (Book of Divine Proof)",
            narrationOrVerseIdentifier = "Al-Kafi 1:294",
            originalArabicText = "إِنِّي تَارِكٌ فِيكُمُ الثَّقَلَيْنِ كِتَابَ اللَّهِ وَعِتْرَتِي أَهْلَ بَيْتِي وَإِنَّهُمَا لَنْ يَفْتَرِقَا حَتَّى يَرِدَا عَلَيَّ الْحَوْضَ",
            englishTranslation = "Indeed, I am leaving among you the Two Weighty Things: the Book of Allah and my progeny, my Ahl al-Bayt; and truly they shall never separate until they return to me at the Pond (al-Hawd).",
            urduTranslation = "بے شک میں تم میں دو گراں قدر چیزیں چھوڑ رہا ہوں: اللہ کی کتاب اور میری عترت یعنی میرے اہلِ بیت، اور یہ دونوں ہرگز جدا نہ ہوں گے یہاں تک کہ حوضِ کوثر پر میرے پاس آئیں۔",
            narratorOrIsnad = "Mutawatir across Imamiyyah and Sunni chains (also in Sunan al-Tirmidhi #3786)",
            relationshipType = "DIRECT_PRIMARY_QUOTATION",
            officialVerificationUrl = "https://thaqalayn.net",
            navigableCollectionKey = "alkafi",
            navigableHadithNumber = 2
          )
        ),
        thematicCorroborations = listOf(
          PrimarySourceCitation(
            citationId = "quran_33_33_tathir",
            sourceType = "QURAN",
            traditionBadge = "Universal Quran (Ayah of Purification • آية التطهير)",
            collectionTitle = "Al-Qur'an al-Kareem",
            editionTitle = "Uthmani Script • Surah Al-Ahzab 33:33",
            chapterOrBookLabel = "Surah Al-Ahzab (33), Ayah 33",
            narrationOrVerseIdentifier = "Quran 33:33",
            originalArabicText = "إِنَّمَا يُرِيدُ اللَّهُ لِيُذْهِبَ عَنكُمُ الرِّجْسَ أَهْلَ الْبَيْتِ وَيُطَهِّرَكُمْ تَطْهِيرًا",
            englishTranslation = "Allah intends only to remove from you the impurity [of sin], O People of the [Prophet's] Household, and to purify you with [extensive] purification.",
            urduTranslation = "اے اہلِ بیت! اللہ تو یہی چاہتا ہے کہ تم سے ہر ناپاکی کو دور رکھے اور تمہیں خوب پاک و صاف کر دے۔",
            transliteration = "Innama yuridullahu li-yudhhiba 'ankumu ar-rijsa Ahlal-Bayti wa yutahhirakum tathira.",
            narratorOrIsnad = "Divine Revelation (Verse of Purification)",
            relationshipType = "THEMATIC_CORROBORATION",
            officialVerificationUrl = "https://quran.com/33/33",
            navigableSurahNumber = 33,
            navigableVerseNumber = 33
          )
        ),
        scholarlyAssessments = listOf(
          ScholarlyAssessmentEntry(
            scholarName = "Imam Muslim ibn al-Hajjaj (d. 261 AH) & Imam al-Tirmidhi (d. 279 AH)",
            scholarlyTradition = "Sunni Canonical Hadith",
            assignedGrade = HadithGradeCategory.SAHIH,
            citedWorkAndEdition = "Sahih Muslim #2408a & Jami' al-Tirmidhi #3786 / #3788",
            reasoningSummary = "Authenticated in Sahih Muslim via Zayd ibn Arqam (ra) and in Jami' al-Tirmidhi via Jabir ibn 'Abdillah and Zayd ibn Arqam."
          ),
          ScholarlyAssessmentEntry(
            scholarName = "Shaykh al-Kulayni (d. 329 AH) & Allamah al-Majlisi",
            scholarlyTradition = "Twelver Shia Hadith Scholarship",
            assignedGrade = HadithGradeCategory.SAHIH,
            citedWorkAndEdition = "Usul al-Kafi Vol. 1 & Mir'at al-'Uqul",
            reasoningSummary = "Regarded as Mutawatir (mass-transmitted) across both Sunni and Shia collections."
          )
        ),
        counterEvidenceOrNuance = listOf(
          "Both Sunni and Twelver Shia traditions revere the Qur'an and the Prophet's Ahl al-Bayt based on Quran 33:33, Sahih Muslim #2408, and Jami' al-Tirmidhi #3786.",
          "Scholarly traditions differ on the theological and political scope: Sunni scholarship emphasizes loving, honoring, and learning from the Ahl al-Bayt alongside the noble Sahaba (Companions), whereas Twelver Shia theology views the wording in Tirmidhi #3786 and Al-Kafi as establishing the authoritative spiritual and legal leadership (Imamah) of the Twelve Imams."
        ),
        commentaryAndSynthesisEnglish = "Hadith al-Thaqalayn is an authentic prophetic narration recorded in both Sunni (Sahih Muslim #2408, Jami' al-Tirmidhi #3786) and Twelver Shia (Kitab al-Kafi) primary sources. Presenting both citations side-by-side clarifies shared reverence for the Quran and Ahl al-Bayt while accurately explaining each tradition's interpretive framework.",
        commentaryAndSynthesisUrdu = "حدیثِ ثقلین اہلِ سنت (صحیح مسلم 2408، جامع ترمذی 3786) اور اثنا عشری شیعہ (اصول الکافی) دونوں کے بنیادی مصادر میں صحیح اسانید کے ساتھ موجود ہے۔ دونوں مکاتبِ فکر قرآنِ مجید اور اہلِ بیتِ اطہار کی عظمت پر متفق ہیں، جبکہ اس کے کلامی و فقہی دائرۂ کار کی تعبیر میں ہر مکتب کا اپنا علمی منہج ہے۔",
        evidenceVsCommentaryNote = "Primary Text: Sahih Muslim #2408 and Al-Kafi 1:294 are direct narrations; the theological conclusions on Khilafah vs. Imamah are scholarly interpretations of the primary text.",
        furtherStudyLinks = listOf(
          VerifiedFurtherStudyResource(
            title = "Sahih Muslim 2408a — Virtues of Ahl al-Bayt",
            speakerOrAuthor = "Imam Muslim ibn al-Hajjaj",
            originalPublisher = "Sunnah.com",
            resourceType = "Primary Hadith Source",
            officialUrl = "https://sunnah.com/muslim:2408a",
            licenceAndLinkingNote = "Verified canonical link."
          )
        ),
        workedOfflineOnly = isOffline
      )
    }

    // CASE 6: Historical Claim needing more evidence (Early historical conflict / uncorroborated chronicler report)
    if (qLower.contains("historical claim") ||
      qLower.contains("abu mikhnaf") ||
      qLower.contains("sayf ibn umar") ||
      qLower.contains("tabari") ||
      qLower.contains("siffin") ||
      qLower.contains("jamal") ||
      qLower.contains("waqidi")
    ) {
      return ClaimVerificationResponse(
        queryText = query,
        detectedLanguage = detectedLang,
        status = ClaimVerificationStatus.HISTORICAL_CLAIM_NEEDS_MORE_EVIDENCE,
        headlineVerdict = "HISTORICAL CLAIM NEEDS MORE EVIDENCE: Early historical chronicles (Tarikh al-Tabari, al-Waqidi) preserved all circulating reports with their chains so later researchers could examine each narrator individually.",
        directQuotations = listOf(
          PrimarySourceCitation(
            citationId = "tabari_muqaddimah_methodology",
            sourceType = "HISTORICAL_CHRONICLE",
            traditionBadge = "Classical Tarikh Methodology (تاريخ الطبري)",
            collectionTitle = "Tarikh al-Rusul wa al-Muluk (History of al-Tabari)",
            editionTitle = "Dar al-Ma'arif Edition, Vol. 1, Introduction (p. 6–7)",
            chapterOrBookLabel = "Author's Methodological Preface",
            narrationOrVerseIdentifier = "Tarikh al-Tabari 1:6",
            originalArabicText = "فَمَا يَكُنْ فِي كِتَابِي هَذَا مِنْ خَبَرٍ ذَكَرْنَاهُ عَنْ بَعْضِ الْمَاضِينَ مِمَّا يَسْتَنْكِرُهُ قَارِئُهُ... فَلْيَعْلَمْ أَنَّهُ لَمْ يُؤْتَ فِي ذَلِكَ مِنْ قِبَلِنَا، وَإِنَّمَا أُوتِيَ مِنْ قِبَلِ بَعْضِ نَاقِلِيهِ إِلَيْنَا",
            englishTranslation = "Whatever report is found in this book of mine from past generations that the reader finds objectionable or unverified... let him know that it did not originate from us, but rather came from some of those who transmitted it to us, and we have merely recorded it as it was transmitted to us.",
            urduTranslation = "میری اس کتاب میں ماضی کی خبروں میں سے جو ایسی روایت ہو جسے پڑھنے والا مستنکر یا غیر ثابت پائے تو وہ جان لے کہ یہ ہماری طرف سے نہیں ہے بلکہ بعض ناقلین کی طرف سے ہم تک پہنچی ہے اور ہم نے اس کی سند ذکر کر دی ہے۔",
            narratorOrIsnad = "Imam Muhammad ibn Jarir al-Tabari (d. 310 AH)",
            relationshipType = "DIRECT_PRIMARY_QUOTATION",
            officialVerificationUrl = "https://quran.com/49/6",
            navigableSurahNumber = 49,
            navigableVerseNumber = 6
          )
        ),
        thematicCorroborations = listOf(
          PrimarySourceCitation(
            citationId = "quran_49_6_verify_news",
            sourceType = "QURAN",
            traditionBadge = "Universal Quran (Criterion of Verification • آية التثبت)",
            collectionTitle = "Al-Qur'an al-Kareem",
            editionTitle = "Uthmani Script • Surah Al-Hujurat 49:6",
            chapterOrBookLabel = "Surah Al-Hujurat (49), Ayah 6",
            narrationOrVerseIdentifier = "Quran 49:6",
            originalArabicText = "يَا أَيُّهَا الَّذِينَ آمَنُوا إِن جَاءَكُمْ فَاسِقٌ بِنَبَإٍ فَتَبَيَّنُوا أَن تُصِيبُوا قَوْمًا بِجَهَالَةٍ فَتُصْبِحُوا عَلَىٰ مَا فَعَلْتُمْ نَادِمِينَ",
            englishTranslation = "O you who have believed, if there comes to you a disobedient one with information, investigate [verify], lest you harm a people out of ignorance and become, over what you have done, regretful.",
            urduTranslation = "اے ایمان والو! اگر تمہارے پاس کوئی فاسق کوئی خبر لے کر آئے تو خوب تحقیق کر لیا کرو، کہیں ایسا نہ ہو کہ تم نادانی میں کسی قوم کو نقصان پہنچا بیٹھو پھر اپنے کیے پر پشیمان ہو۔",
            transliteration = "Ya ayyuhalladhina amanu in ja'akum fasiqun bi-naba'in fa-tabayyanu...",
            narratorOrIsnad = "Mutawatir Quranic Injunction on Source Verification (Tabayyun)",
            relationshipType = "THEMATIC_CORROBORATION",
            officialVerificationUrl = "https://quran.com/49/6",
            navigableSurahNumber = 49,
            navigableVerseNumber = 6
          )
        ),
        scholarlyAssessments = listOf(
          ScholarlyAssessmentEntry(
            scholarName = "Imam Muhammad ibn Jarir al-Tabari (d. 310 AH)",
            scholarlyTradition = "Classical Historiography",
            assignedGrade = HadithGradeCategory.GRADING_NOT_VERIFIED,
            citedWorkAndEdition = "Tarikh al-Tabari, Vol. 1, p. 6",
            reasoningSummary = "Explicitly warned readers that inclusion of a historical report in Tarikh al-Tabari does not mean al-Tabari authenticated it; rather, he attached chains (via narrators such as Sayf ibn 'Umar, Abu Mikhnaf, or al-Waqidi) so scholars could scrutinize each chain."
          ),
          ScholarlyAssessmentEntry(
            scholarName = "Hafiz Ibn Hajar al-'Asqalani & Ibn Khaldun",
            scholarlyTradition = "Historical & Biographical Criticism",
            assignedGrade = HadithGradeCategory.DAIF,
            citedWorkAndEdition = "Lisan al-Mizan & Muqaddimah Ibn Khaldun",
            reasoningSummary = "Demonstrated that many dramatic political narratives in early chronicles rely on single-strand akhbari narrators with broken chains, and must be cross-checked against Sahih hadith collections and historical plausibility."
          )
        ),
        counterEvidenceOrNuance = listOf(
          "Merely citing 'It is in Tarikh al-Tabari' or repeating a story across later books does NOT prove historical certainty.",
          "Investigating early Islamic history requires checking: (1) Is the chain continuous to an eyewitness? (2) Are the narrators trustworthy? (3) Does it align with authenticated reports in Sahih al-Bukhari, Sahih Muslim, or verified biographical consensus?"
        ),
        commentaryAndSynthesisEnglish = "Historical reports concerning early political conflicts (such as Jamal, Siffin, or narratives transmitted exclusively through akhbari chroniclers like Sayf ibn 'Umar, Abu Mikhnaf, or al-Waqidi) require rigorous chain analysis. As Imam al-Tabari himself stated in his preface, recording a report in a history book is not an endorsement of its authenticity.",
        commentaryAndSynthesisUrdu = "ابتدائی اسلامی تاریخ اور سیاسی واقعات (جیسے جمل، صفین یا صرف اخباری راویوں مثلاً سیف بن عمر، ابو مخنف یا واقدی کے طریق سے مروی قصے) کے بارے میں کسی نتیجے پر پہنچنے کے لیے سند کی جانچ ناگزیر ہے۔ جیسا کہ خود امام طبری نے اپنی تاریخ کے مقدمے میں واضح فرمایا کہ کسی خبر کا تاریخ کی کتاب میں درج ہونا اس کی صحت کی دلیل نہیں ہے۔",
        evidenceVsCommentaryNote = "Quran 49:6 establishes the duty of verification (tabayyun). Al-Tabari's preface explains why unverified historical reports exist alongside authentic ones in classical chronicles.",
        furtherStudyLinks = listOf(
          VerifiedFurtherStudyResource(
            title = "Surah Al-Hujurat 49:6 — Principle of Tabayyun (Verification)",
            speakerOrAuthor = "Quran Foundation",
            originalPublisher = "Quran.com",
            resourceType = "Quranic Principle",
            officialUrl = "https://quran.com/49/6",
            licenceAndLinkingNote = "Verified Quran link."
          )
        ),
        workedOfflineOnly = isOffline
      )
    }

    return null
  }

  /**
   * Priority 12 Section D — Returns all 5 Hadith Grading Categories with detailed explanations.
   */
  fun getHadithGradingGuide(): List<HadithGradeCategory> = HadithGradeCategory.entries

  /**
   * Priority 12 Section E — Returns respectful, evidence-based dossiers on all 7 controversial/historical subjects.
   */
  fun getControversialAndHistoricalDossiers(): List<ControversialTopicDossier> = listOf(
    ControversialTopicDossier(
      topicId = "ahl_al_bayt_and_sahaba",
      categoryLabel = "Ahl al-Bayt & the Sahaba",
      titleEnglish = "Reverence for the Prophet's Household (Ahl al-Bayt) and the Companions (Sahaba)",
      titleArabic = "مَحَبَّةُ أَهْلِ البَيْتِ وَالصَّحَابَةِ الكِرَام",
      titleUrdu = "اہلِ بیتِ اطہار اور صحابہ کرام کا احترام و مقام",
      primaryEvidenceSummary = "Quran 33:33 (Verse of Purification), Quran 48:29 & 9:100 (Praise of the Muhajirun and Ansar), Sahih Muslim #2408 (Hadith al-Thaqalayn), and Al-Kafi Vol. 1.",
      sunniScholarlyPerspective = "Ahl al-Sunnah hold that loving and honoring both the Prophet's Household (Ahl al-Bayt) and all noble Companions (Sahaba) is an obligation of faith. Abu Bakr, 'Umar, 'Uthman, and 'Ali (ra) are revered as the Rightly Guided Caliphs, while 'Ali, Fatimah, al-Hasan, and al-Husayn hold exalted rank under the Cloak (Hadith al-Kisa, Sahih Muslim #2424).",
      shiaScholarlyPerspective = "Twelver Shia (Imamiyyah) scholarship holds that after the Prophet (ﷺ), divinely designated spiritual and temporal authority (Imamah) belongs to Imam 'Ali and the Twelve Imams of the Ahl al-Bayt, citing Hadith al-Ghadir and Hadith al-Thaqalayn, while honoring faithful Companions who remained steadfast.",
      areasOfAgreement = "Both traditions agree on the sanctity of the Quran, the finality of Prophethood, the exalted purity of Fatimah al-Zahra, Imam 'Ali, Imam al-Hasan, and Imam al-Husayn, and the prohibition of sectarian violence or slandering sacred figures.",
      knownDisagreementsAndAdab = "Theological differences center on whether leadership after the Prophet (ﷺ) was determined by prophetic consultation/consensus (Shura) or explicit designation (Nass). Scholarly adab prohibits abusive labels, takfir, or cursing revered figures.",
      keyCitations = listOf(
        PrimarySourceCitation(
          citationId = "dossier_quran_33_33",
          sourceType = "QURAN",
          traditionBadge = "Universal Quran",
          collectionTitle = "Al-Qur'an al-Kareem",
          editionTitle = "Surah Al-Ahzab 33:33",
          chapterOrBookLabel = "Surah 33, Ayah 33",
          narrationOrVerseIdentifier = "Quran 33:33",
          originalArabicText = "إِنَّمَا يُرِيدُ اللَّهُ لِيُذْهِبَ عَنكُمُ الرِّجْسَ أَهْلَ الْبَيْتِ وَيُطَهِّرَكُمْ تَطْهِيرًا",
          englishTranslation = "Allah intends only to remove from you the impurity, O People of the Household, and to purify you with extensive purification.",
          urduTranslation = "اے اہلِ بیت! اللہ تو یہی چاہتا ہے کہ تم سے ہر ناپاکی کو دور رکھے اور تمہیں خوب پاک کر دے۔",
          officialVerificationUrl = "https://quran.com/33/33",
          navigableSurahNumber = 33,
          navigableVerseNumber = 33
        ),
        PrimarySourceCitation(
          citationId = "dossier_quran_9_100",
          sourceType = "QURAN",
          traditionBadge = "Universal Quran",
          collectionTitle = "Al-Qur'an al-Kareem",
          editionTitle = "Surah At-Tawbah 9:100",
          chapterOrBookLabel = "Surah 9, Ayah 100",
          narrationOrVerseIdentifier = "Quran 9:100",
          originalArabicText = "وَالسَّابِقُونَ الْأَوَّلُونَ مِنَ الْمُهَاجِرِينَ وَالْأَنصَارِ وَالَّذِينَ اتَّبَعُوهُم بِإِحْسَانٍ رَّضِيَ اللَّهُ عَنْهُمْ وَرَضُوا عَنْهُ",
          englishTranslation = "And the first forerunners among the Muhajireen and the Ansar and those who followed them with good conduct — Allah is pleased with them and they are pleased with Him.",
          urduTranslation = "اور مہاجرین اور انصار میں سے سبقت لے جانے والے اولین لوگ اور وہ جنہوں نے نیکی کے ساتھ ان کی پیروی کی، اللہ ان سے راضی ہوا اور وہ اس سے راضی ہوئے۔",
          officialVerificationUrl = "https://quran.com/9/100",
          navigableSurahNumber = 9,
          navigableVerseNumber = 100
        )
      ),
      furtherStudy = listOf(
        VerifiedFurtherStudyResource(
          title = "Amman Message — Mutual Recognition of Islamic Schools of Thought",
          speakerOrAuthor = "International Islamic Fiqh Academy & 200+ Senior Scholars",
          originalPublisher = "AmmanMessage.com",
          resourceType = "International Scholarly Consensus Document",
          officialUrl = "https://ammanmessage.com",
          licenceAndLinkingNote = "Open educational declaration."
        )
      )
    ),
    ControversialTopicDossier(
      topicId = "saints_and_karamat",
      categoryLabel = "Saints & Religious Personalities",
      titleEnglish = "Awliya' (Friends of Allah) and Evaluating Attributed Karamat",
      titleArabic = "أَوْلِيَاءُ اللَّهِ وَضَوَابِطُ الكَرَامَات",
      titleUrdu = "اولیاء اللہ کا مقام اور کرامات و حکایات کی جانچ",
      primaryEvidenceSummary = "Quran 10:62–63 defines the Awliya' of Allah by faith (iman) and God-consciousness (taqwa), and Sahih al-Bukhari #6502 (Hadith al-Wali) outlines drawing near to Allah through obligatory and supererogatory worship.",
      sunniScholarlyPerspective = "Mainstream Sunni creed (e.g., Aqيدة al-Tahawiyyah) affirms the reality of Karamat (extraordinary graces granted by Allah to pious servants) when authentically established, while insisting that true wilayah is measured strictly by adherence to the Quran and Sunnah—never by unverified folklore or exemption from Sacred Law (Shari'ah).",
      shiaScholarlyPerspective = "Twelver Shia theology affirms extraordinary divine graces for the Prophets, Imams, and righteous believers, while classical scholars (such as Shaykh al-Saduq and Shaykh al-Mufid) strongly reject exaggeration (ghuluww) that attributes independent divine lordship to created beings.",
      areasOfAgreement = "Both traditions agree that no saint or religious personality is above the commands of Allah, that exaggeration (ghuluww) is forbidden, and that hagiographical stories lacking authentic chains cannot establish religious creed or law.",
      knownDisagreementsAndAdab = "Scholars differ over specific historical anecdotes in biographical compendia (Tabaqat) and practices around visiting graves (ziyarah vs. tawassul boundaries). Discussions must remain grounded in textual evidence without accusing fellow Muslims of shirk without proof.",
      keyCitations = listOf(
        PrimarySourceCitation(
          citationId = "dossier_quran_10_62",
          sourceType = "QURAN",
          traditionBadge = "Universal Quran",
          collectionTitle = "Al-Qur'an al-Kareem",
          editionTitle = "Surah Yunus 10:62–63",
          chapterOrBookLabel = "Surah 10, Ayah 62",
          narrationOrVerseIdentifier = "Quran 10:62",
          originalArabicText = "أَلَا إِنَّ أَوْلِيَاءَ اللَّهِ لَا خَوْفٌ عَلَيْهِمْ وَلَا هُمْ يَحْزَنُونَ ۝ الَّذِينَ آمَنُوا وَكَانُوا يَتَّقُونَ",
          englishTranslation = "Unquestionably, [for] the allies (awliya') of Allah there will be no fear concerning them, nor will they grieve — those who believed and were fearing Allah.",
          urduTranslation = "خبردار! بے شک اولیاء اللہ پر نہ کوئی خوف ہے اور نہ وہ غمگین ہوں گے، وہ لوگ جو ایمان لائے اور تقویٰ اختیار کرتے رہے۔",
          officialVerificationUrl = "https://quran.com/10/62",
          navigableSurahNumber = 10,
          navigableVerseNumber = 62
        )
      ),
      furtherStudy = emptyList()
    ),
    ControversialTopicDossier(
      topicId = "popular_stories_and_miracles",
      categoryLabel = "Popular Stories & Miracles",
      titleEnglish = "Distinguishing Authenticated Prophetic Miracles from Unverified Popular Stories",
      titleArabic = "التَّمْيِيزُ بَيْنَ المُعْجِزَاتِ الثَّابِتَةِ وَالقِصَصِ الشَّائِعَة",
      titleUrdu = "ثابت شدہ معجزات اور غیر مستند مشہور قصوں میں فرق",
      primaryEvidenceSummary = "Authenticated miracles include the Quran itself, the Splitting of the Moon (Quran 54:1, Sahih al-Bukhari #3636), Isra' and Mi'raj (Quran 17:1), and water flowing from the Prophet's fingers in front of hundreds of Companions (Sahih al-Bukhari #3579). Conversely, many popular pulpit stories (qisas al-qussas) have no chain of narration.",
      sunniScholarlyPerspective = "Muhaddithin authored dedicated works (such as Ibn al-Jawzi's Kitab al-Qussas wa al-Mudhakkirin and al-Sakhawi's Al-Maqasid al-Hasanah) warning preachers not to narrate unverified stories simply to move audiences emotionally, because authentic Quran and Sahih Sunnah are sufficient.",
      shiaScholarlyPerspective = "Imamiyyah critical scholars (such as al-Muhaqqiq al-Hilli, Shahid al-Thani, and Muhaddith al-Nuri's critics) similarly distinguish mutawatir miracles and sahih reports from unverified popular pulpit tales.",
      areasOfAgreement = "Repeating a story in many late books or sermons never turns a chainless story into an authentic report. Every miraculous claim must be traced to its earliest primary source and chain.",
      knownDisagreementsAndAdab = "Some late biographers included weak reports in Sirah compilations for completeness; readers must check the critical footnotes (tahqiq) rather than assuming every story in a biography is Sahih.",
      keyCitations = listOf(
        PrimarySourceCitation(
          citationId = "dossier_quran_54_1",
          sourceType = "QURAN",
          traditionBadge = "Universal Quran",
          collectionTitle = "Al-Qur'an al-Kareem",
          editionTitle = "Surah Al-Qamar 54:1",
          chapterOrBookLabel = "Surah 54, Ayah 1",
          narrationOrVerseIdentifier = "Quran 54:1",
          originalArabicText = "اقْتَرَبَتِ السَّاعَةُ وَانشَقَّ الْقَمَرُ",
          englishTranslation = "The Hour has come near, and the moon has split.",
          urduTranslation = "قیامت قریب آ گئی اور چاند شق ہو گیا۔",
          officialVerificationUrl = "https://quran.com/54/1",
          navigableSurahNumber = 54,
          navigableVerseNumber = 1
        )
      ),
      furtherStudy = emptyList()
    ),
    ControversialTopicDossier(
      topicId = "disputed_religious_practices",
      categoryLabel = "Disputed Fiqh Practices",
      titleEnglish = "Understanding Juristic Differences in Worship and Practice",
      titleArabic = "فِقْهُ الخِلَافِ فِي العِبَادَاتِ وَالمَسَائِلِ الفَرْعِيَّة",
      titleUrdu = "عبادات اور فروعی مسائل میں فقہی اختلاف کے اسباب",
      primaryEvidenceSummary = "Valid juristic differences (ikhtilaf al-tanawwu' wa al-ijtihad) exist on issues such as raising the hands in prayer (raf' al-yadayn), placing the hands in salah, combining prayers when needed (Sahih Muslim #705), and wiping over footgear.",
      sunniScholarlyPerspective = "The four Sunni legal schools (Hanafi, Maliki, Shafi'i, Hanbali) recognize one another as valid orthodox methodologies derived from the Quran and Sunnah. Differences arise from how broad Quranic texts are reconciled with specific hadiths.",
      shiaScholarlyPerspective = "The Ja'fari school derives rulings from the Quran and the Sunnah transmitted through the Prophet (ﷺ) and the Imams of the Ahl al-Bayt (e.g., Wasa'il al-Shi'ah, Tahdhib al-Ahkam), sharing the majority of foundational obligations with Sunni schools while differing on specific ritual details.",
      areasOfAgreement = "All recognized schools agree on the Five Pillars, the timings and rak'at counts of the five daily prayers, fasting Ramadan, Zakah, and Hajj.",
      knownDisagreementsAndAdab = "Classical scholars established the maxim: 'There is no condemnation in matters of valid juristic ijtihad (لا إنكار في مسائل الاجتهاد).' Disagreement must be expressed with evidence and courtesy.",
      keyCitations = emptyOfOrSampleCitation(),
      furtherStudy = emptyList()
    ),
    ControversialTopicDossier(
      topicId = "early_islamic_history",
      categoryLabel = "Early Islamic History",
      titleEnglish = "Methodology for Studying the Preservation of the Quran and Early Caliphate",
      titleArabic = "مَنْهَجُ دِرَاسَةِ التَّارِيخِ الإِسْلَامِيِّ المُبَكِّر",
      titleUrdu = "ابتدائی اسلامی تاریخ اور جمعِ قرآن کے مطالعے کا منہج",
      primaryEvidenceSummary = "Sahih al-Bukhari #4986–4987 documents the compilation of the Mushaf under Abu Bakr al-Siddiq and 'Uthman ibn 'Affan (ra) through Zayd ibn Thabit and a committee of Companions, backed by universal oral and written tawatur.",
      sunniScholarlyPerspective = "Emphasizes the continuity between oral memorization by thousands of Companions and written compilation, verified by eyewitness chains in Sahih al-Bukhari.",
      shiaScholarlyPerspective = "Senior Imamiyyah scholars (including Shaykh al-Saduq, Shaykh al-Mufid, Sharif al-Murtada, Shaykh al-Tusi, and Abu al-Qasim al-Khu'i in Al-Bayan fi Tafsir al-Qur'an) affirm that the Uthmani Mushaf between the two covers is the complete, unaltered Word of Allah, rejecting isolated anomalous reports.",
      areasOfAgreement = "Both Sunni and Twelver Shia mainstream scholarship unanimously affirm the complete textual integrity (tawatur and preservation) of the Holy Quran (Quran 15:9).",
      knownDisagreementsAndAdab = "Historical discussions on the sequence of early political events must never be conflated with the unanimous preservation of the Quranic text.",
      keyCitations = listOf(
        PrimarySourceCitation(
          citationId = "dossier_quran_15_9",
          sourceType = "QURAN",
          traditionBadge = "Universal Quran",
          collectionTitle = "Al-Qur'an al-Kareem",
          editionTitle = "Surah Al-Hijr 15:9",
          chapterOrBookLabel = "Surah 15, Ayah 9",
          narrationOrVerseIdentifier = "Quran 15:9",
          originalArabicText = "إِنَّا نَحْنُ نَزَّلْنَا الذِّكْرَ وَإِنَّا لَهُ لَحَافِظُونَ",
          englishTranslation = "Indeed, it is We who sent down the Reminder [the Qur'an], and indeed, We will be its guardian.",
          urduTranslation = "بے شک ہم ہی نے اس ذکر (قرآن) کو نازل فرمایا ہے اور بے شک ہم ہی اس کی حفاظت کرنے والے ہیں۔",
          officialVerificationUrl = "https://quran.com/15/9",
          navigableSurahNumber = 15,
          navigableVerseNumber = 9
        )
      ),
      furtherStudy = emptyList()
    ),
    ControversialTopicDossier(
      topicId = "historical_conflicts",
      categoryLabel = "Historical Conflicts",
      titleEnglish = "Evaluating Reports on Early Civil Conflicts (Fitnah, Jamal, Siffin, and Karbala)",
      titleArabic = "تَحْقِيقُ المَرْوِيَّاتِ التَّارِيخِيَّةِ فِي الفِتَن",
      titleUrdu = "تاریخی تنازعات اور شہادتِ حسینؓ کے متعلق روایات کی تحقیق",
      primaryEvidenceSummary = "Sahih al-Bukhari #2704 praises al-Hasan ibn 'Ali (ra) for reconciling two great groups of Muslims, and both traditions mourn the tragic martyrdom of Imam al-Husayn (ra/as) at Karbala (61 AH).",
      sunniScholarlyPerspective = "Condemns the injustice perpetrated by Yazid's army at Karbala and honors Imam al-Husayn as the Master of the Youth of Paradise (Jami' al-Tirmidhi #3768), while maintaining restraint regarding the Companions' earlier ijtihadi disputes (Quran 49:9–10).",
      shiaScholarlyPerspective = "Views the stand of Imam al-Husayn (as) at Karbala as the supreme paradigm of standing for truth, justice, and moral reform against tyranny.",
      areasOfAgreement = "Both traditions revere Imam al-Husayn as the beloved grandson of the Prophet (ﷺ) and the Master of the Youth of Paradise, and both require verifying historical reports rather than relying on unauthenticated late additions.",
      knownDisagreementsAndAdab = "Avoid turning historical grief into contemporary sectarian hostility; verify every historical quotation against early primary chains.",
      keyCitations = emptyOfOrSampleCitation(),
      furtherStudy = emptyList()
    ),
    ControversialTopicDossier(
      topicId = "sunni_shia_methodology_comparison",
      categoryLabel = "Sunni & Twelver Shia Methodologies",
      titleEnglish = "Comparing Hadith Authentication in Sunni and Twelver Shia Scholarship",
      titleArabic = "مُقَارَنَةُ مَنَاهِجِ التَّصْحِيحِ عِنْدَ السُّنَّةِ وَالشِّيعَةِ الإِمَامِيَّة",
      titleUrdu = "اہلِ سنت اور اثنا عشری شیعہ کے اصولِ حدیث کا تقابلی مطالعہ",
      primaryEvidenceSummary = "Sunni hadith science evaluates continuity to the Prophet (ﷺ) across the Kutub al-Sittah and Musnads; Twelver Shia dirayah (established systematically by Allamah al-Hilli and Shahid al-Thani) classifies reports into Sahih, Hasan, Muwaththaq, and Da'if based on chains reaching the Prophet (ﷺ) or the Twelve Imams.",
      sunniScholarlyPerspective = "Treats Sahih al-Bukhari and Sahih Muslim as the highest tier of authenticated collections, while evaluating each narration in the four Sunan and Musnads individually.",
      shiaScholarlyPerspective = "Does NOT assume every single report in Kitab al-Kafi or the Four Books (Kutub al-Arba'ah) is automatically Sahih; rather, scholars like Allamah al-Majlisi (in Mir'at al-'Uqul) and Asif al-Muhsini (in Mu'jam al-Ahadith al-Mu'tabarah) grade each narration individually by its chain.",
      areasOfAgreement = "Neither tradition claims that every printed hadith book is above chain scrutiny (except the Holy Quran, which is 100% Mutawatir). Both reject fabricated reports and require narrator integrity ('adalah) and precision (dabt).",
      knownDisagreementsAndAdab = "Differences lie in the biographical dictionaries (Kutub al-Rijal) and the status of narrators. Respectful academic comparison always cites each school's own authentication standards accurately.",
      keyCitations = emptyOfOrSampleCitation(),
      furtherStudy = emptyList()
    )
  )

  private fun emptyOfOrSampleCitation(): List<PrimarySourceCitation> = listOf(
    PrimarySourceCitation(
      citationId = "quran_49_10_brotherhood",
      sourceType = "QURAN",
      traditionBadge = "Universal Quran",
      collectionTitle = "Al-Qur'an al-Kareem",
      editionTitle = "Surah Al-Hujurat 49:10",
      chapterOrBookLabel = "Surah 49, Ayah 10",
      narrationOrVerseIdentifier = "Quran 49:10",
      originalArabicText = "إِنَّمَا الْمُؤْمِنُونَ إِخْوَةٌ فَأَصْلِحُوا بَيْنَ أَخَوَيْكُمْ ۚ وَاتَّقُوا اللَّهَ لَعَلَّكُمْ تُرْحَمُونَ",
      englishTranslation = "The believers are but brothers, so make settlement between your brothers. And fear Allah that you may receive mercy.",
      urduTranslation = "مومن تو آپس میں بھائی ہی ہیں، پس اپنے دو بھائیوں کے درمیان صلح کراؤ اور اللہ سے ڈرو تاکہ تم پر رحم کیا جائے۔",
      officialVerificationUrl = "https://quran.com/49/10",
      navigableSurahNumber = 49,
      navigableVerseNumber = 10
    )
  )

  /**
   * Priority 12 Section G — All 10 structured Learning Modules, each with:
   * - Short explanation
   * - Worked example using a verified source
   * - Interactive Quiz
   * - List of further reading
   */
  fun getAll10LearningModules(): List<EvidenceLearningModule> = listOf(
    EvidenceLearningModule(
      moduleNumber = 1,
      id = "module_1_quran_context",
      title = "1. How to read the Quran with context",
      arabicSubtitle = "قِرَاءَةُ القُرْآنِ فِي سِيَاقِهِ وَأَسْبَابِ النُّزُول",
      shortExplanation = "Every Quranic verse belongs to an internal textual flow (siyaq al-ayah) and often a historical occasion of revelation (sabab al-nuzul). Isolating half a verse from its preceding and succeeding verses can reverse its intended meaning.",
      workedExampleTitle = "Worked Example: Surah Al-Baqarah 2:190–193 & Surah Al-Mumtahanah 60:8",
      workedExampleSourceRef = "Quran 2:190 & Quran 60:8",
      workedExampleArabic = "وَقَاتِلُوا فِي سَبِيلِ اللَّهِ الَّذِينَ يُقَاتِلُونَكُمْ وَلَا تَعْتَدُوا ۚ إِنَّ اللَّهَ لَا يُحِبُّ الْمُعْتَدِينَ",
      workedExampleAnalysis = "When verses regarding defensive combat are read alongside Quran 2:190 ('fight those who fight you, and do not transgress') and Quran 60:8 ('Allah does not forbid you from those who do not fight you... from being righteous and just toward them'), the ethical and legal boundaries become unmistakable.",
      quiz = LearningModuleQuiz(
        question = "Why is it misleading to quote a single phrase of a Quranic verse in isolation?",
        options = listOf(
          "Because textual flow (siyaq) and occasion of revelation (sabab al-nuzul) define its scope and conditions",
          "Because individual verses have no meaning at all",
          "Because only translations matter, not the Arabic context",
          "Because all verses were revealed on the same day"
        ),
        correctOptionIndex = 0,
        explanation = "Classical usul al-tafsir requires reading a verse alongside its surrounding verses (siyaq), cross-referencing related verses across the Quran (tafsir al-Qur'an bil-Qur'an), and checking authenticated asbab al-nuzul."
      ),
      furtherReading = listOf(
        "Al-Itqan fi 'Ulum al-Qur'an — Imam Jalal al-Din al-Suyuti",
        "Muqaddimah fi Usul al-Tafsir — Shaykh al-Islam Ibn Taymiyyah",
        "Al-Bayan fi Tafsir al-Qur'an — Sayyid Abu al-Qasim al-Khu'i"
      )
    ),
    EvidenceLearningModule(
      moduleNumber = 2,
      id = "module_2_locate_hadith",
      title = "2. How to locate a hadith in its original collection",
      arabicSubtitle = "كَيْفِيَّةُ تَخْرِيجِ الحَدِيثِ مِنْ مَصَادِرِهِ الأَصْلِيَّة",
      shortExplanation = "A reliable hadith citation specifies the primary compilation (e.g., Sahih al-Bukhari, Sahih Muslim, Sunan Abu Dawud, Al-Kafi), the Book/Chapter (Kitab/Bab), the edition numbering (such as Darussalam continuous numbering vs. In-Book reference), and the Companions/Imams in the chain.",
      workedExampleTitle = "Worked Example: Locating the Fajr Tathwib Hadith in Sunan Abu Dawud",
      workedExampleSourceRef = "Sunan Abu Dawud #500 (Book 2, Hadith 110)",
      workedExampleArabic = "الصَّلَاةُ خَيْرٌ مِنَ النَّوْمِ",
      workedExampleAnalysis = "Instead of writing 'The Prophet said prayer is better than sleep somewhere in a book', a verifiable citation provides: Sunan Abu Dawud, Darussalam #500, Book 2 (Kitab al-Salah), Narrated by Abu Mahdhurah (ra), graded Sahih.",
      quiz = LearningModuleQuiz(
        question = "Which of the following is a complete, verifiable hadith reference?",
        options = listOf(
          "Sahih al-Bukhari, Darussalam #1 (Book 1: Revelation, Hadith 1), narrated by 'Umar ibn al-Khattab (ra)",
          "A famous hadith I heard in a short video clip",
          "Reported by Bukhari or Muslim somewhere",
          "Page 45 of an unnamed PDF"
        ),
        correctOptionIndex = 0,
        explanation = "Because printings vary by page number, citing the Collection + Edition Number (e.g. Darussalam #1) + Book/Chapter + Narrator ensures any student or scholar can verify the exact report."
      ),
      furtherReading = listOf(
        "Usul al-Takhrij wa Dirasat al-Asanid — Dr. Mahmud al-Tahhan",
        "Al-Mu'jam al-Mufahras li-Alfaz al-Hadith al-Nabawi"
      )
    ),
    EvidenceLearningModule(
      moduleNumber = 3,
      id = "module_3_what_is_isnad",
      title = "3. What an isnad is",
      arabicSubtitle = "مَفْهُومُ الإِسْنَادِ وَسِلْسِلَةِ الرُّوَاة",
      shortExplanation = "The isnad (chain of transmission) is the chronological list of narrators linking the compiler of a book back to the Prophet (ﷺ) or an early authority. The text of the report itself is called the matn.",
      workedExampleTitle = "Worked Example: The Chain of Sahih al-Bukhari #1",
      workedExampleSourceRef = "Sahih al-Bukhari #1",
      workedExampleArabic = "حَدَّثَنَا الحُمَيْدِيُّ عَبْدُ اللَّهِ بْنُ الزُّبَيْرِ قَالَ: حَدَّثَنَا سُفْيَانُ قَالَ: حَدَّثَنَا يَحْيَى بْنُ سَعِيدٍ الأَنْصَارِيُّ...",
      workedExampleAnalysis = "Al-Bukhari records each link: al-Humaydi -> Sufyan ibn 'Uyaynah -> Yahya ibn Sa'id al-Ansari -> Muhammad ibn Ibrahim al-Taymi -> 'Alqamah ibn Waqqas -> 'Umar ibn al-Khattab (ra) -> The Prophet (ﷺ). Scholars verify that each teacher and student met and were trustworthy.",
      quiz = LearningModuleQuiz(
        question = "What is the difference between the 'isnad' and the 'matn' of a hadith?",
        options = listOf(
          "The isnad is the chain of narrators, while the matn is the actual wording/content of the report",
          "The isnad is the English translation, while the matn is the Arabic text",
          "The isnad is a scholar's commentary, while the matn is a Quranic verse",
          "They mean the exact same thing"
        ),
        correctOptionIndex = 0,
        explanation = "'Abdullah ibn al-Mubarak (d. 181 AH) famously stated: 'The isnad is part of the religion; were it not for the isnad, whoever wished could say whatever they wished.'"
      ),
      furtherReading = listOf(
        "Muqaddimah Sahih Muslim — Imam Muslim ibn al-Hajjaj",
        "Nuzhat al-Nazar fi Tawdih Nukhbat al-Fikar — Hafiz Ibn Hajar al-'Asqalani"
      )
    ),
    EvidenceLearningModule(
      moduleNumber = 4,
      id = "module_4_how_grading_works",
      title = "4. How hadith grading works",
      arabicSubtitle = "شُرُوطُ الحَدِيثِ الصَّحِيحِ وَالحَسَنِ وَالضَّعِيف",
      shortExplanation = "Hadith authentication examines five strict criteria: (1) unbroken chain continuity (ittisal), (2) narrator moral integrity ('adalah), (3) narrator memory/writing precision (dabt), (4) absence of contradiction with stronger authorities (ghayr shadhdh), and (5) absence of subtle hidden defects (ghayr mu'allal).",
      workedExampleTitle = "Worked Example: Distinguishing Sahih, Hasan, Da'if, and Mawdu'",
      workedExampleSourceRef = "Nukhbat al-Fikar (Ibn Hajar) & Muqaddimah Ibn al-Salah",
      workedExampleArabic = "الصَّحِيحُ لِذَاتِهِ مَا اتَّصَلَ سَنَدُهُ بِنَقْلِ العَدْلِ الضَّابِطِ عَنْ مِثْلِهِ إِلَى مُنْتَهَاهُ مِنْ غَيْرِ شُذُوذٍ وَلَا عِلَّةٍ",
      workedExampleAnalysis = "If all 5 conditions are met at the highest level, the report is Sahih. If precision (dabt) is slightly lighter, it is Hasan. If a link is missing or memory is weak, it is Da'if. If a narrator is a known liar/forger, it is Mawdu' (Fabricated).",
      quiz = LearningModuleQuiz(
        question = "Which five conditions are required for a narration to be graded Sahih li-dhatihi?",
        options = listOf(
          "Unbroken chain, upright narrators ('adalah), precise retention (dabt), no anomaly (shudhudh), and no hidden defect ('illah)",
          "Popularity on social media and emotional impact",
          "Being quoted in at least three late books without a chain",
          "Having a short Arabic text"
        ),
        correctOptionIndex = 0,
        explanation = "Even if a chain looks continuous on the surface, specialists check for subtle defects ('ilal) and compare the wording against all other students of the same teacher."
      ),
      furtherReading = listOf(
        "Muqaddimah Ibn al-Salah fi 'Ulum al-Hadith",
        "Al-Ba'ith al-Hathith — Ahmad Muhammad Shakir",
        "Dirayat al-Hadith — Shahid al-Thani"
      )
    ),
    EvidenceLearningModule(
      moduleNumber = 5,
      id = "module_5_why_scholars_disagree",
      title = "5. Why scholars sometimes disagree",
      arabicSubtitle = "أَسْبَابُ اخْتِلَافِ العُلَمَاءِ وَالمُحَدِّثِين",
      shortExplanation = "Qualified scholars may assign different grades to the same narration or derive different legal rulings because: (1) they differ on a borderline narrator's precision (mukhtalaf fihi), (2) one scholar found corroborating pathways (shawahid) that another did not accept, or (3) they apply different conditions for mursal reports.",
      workedExampleTitle = "Worked Example:Musnad Ahmad #3251 (Cave of Thawr Spider & Doves)",
      workedExampleSourceRef = "Musnad Ahmad #3251",
      workedExampleArabic = "حَسَّنَهُ الحَافِظُ ابْنُ حَجَرٍ وَابْنُ كَثِيرٍ، وَضَعَّفَهُ الأَلْبَانِيُّ وَالأَرْنَاؤُوطُ",
      workedExampleAnalysis = "Hafiz Ibn Hajar and Ibn Kathir graded the report Hasan because corroborating pathways strengthened the chain, whereas al-Albani and Shu'ayb al-Arna'ut graded it Da'if due to strict criticism of 'Uthman al-Jazari. Both positions follow principled methodology.",
      quiz = LearningModuleQuiz(
        question = "Why might two respected hadith scholars grade the same narration Hasan vs. Da'if?",
        options = listOf(
          "Because a narrator in the chain is borderline (mukhtalaf fihi) or they differ on whether corroborating routes repair a minor weakness",
          "Because scholars grade randomly without rules",
          "Because one of them did not read Arabic",
          "Because hadith books change every year"
        ),
        correctOptionIndex = 0,
        explanation = "Grading a borderline narrator or weighing corroborating chains (shawahid/mutaba'at) is a matter of scholarly ijtihad."
      ),
      furtherReading = listOf(
        "Raf' al-Malam 'an al-A'immah al-A'lam — Ibn Taymiyyah",
        "Adab al-Ikhtilaf fi al-Islam — Dr. Taha Jabir al-'Alwani"
      )
    ),
    EvidenceLearningModule(
      moduleNumber = 6,
      id = "module_6_identify_fabricated_quotes",
      title = "6. How to identify fabricated quotations",
      arabicSubtitle = "عَلَامَاتُ الوَضْعِ وَالأَقْوَالِ المَكْذُوبَة",
      shortExplanation = "Scholars identify fabricated (Mawdu') quotations through two lenses: (1) Isnad: absence of any chain in primary books or presence of a documented fabricator; (2) Matn: stark contradiction with the Quran, established Sunnah, historical fact, or disproportionate hyperbolic rewards/punishments.",
      workedExampleTitle = "Worked Example: 'He who knows himself knows his Lord'",
      workedExampleSourceRef = "Al-Maqasid al-Hasanah #1149 (al-Sakhawi)",
      workedExampleArabic = "مَنْ عَرَفَ نَفْسَهُ فَقَدْ عَرَفَ رَبَّهُ — لَيْسَ بِثَابِتٍ مَرْفُوعًا",
      workedExampleAnalysis = "When checked in primary hadith indices, this quote has zero prophetic chain (la asla lahu marfu'an). Al-Nawawi and al-Sakhawi showed it is actually a statement of Yahya ibn Mu'adh al-Razi, not the Prophet (ﷺ).",
      quiz = LearningModuleQuiz(
        question = "What is the first sign that a viral religious quote online may be fabricated or misattributed?",
        options = listOf(
          "It lacks a primary collection name, chapter, hadith number, and chain of narration, and does not appear in classical takhrij works",
          "It is written in decorative calligraphy",
          "It is translated into English and Urdu",
          "It is short and easy to memorize"
        ),
        correctOptionIndex = 0,
        explanation = "Any authentic prophetic statement transmitted to us is preserved in the indexed canonical collections with its chain (isnad)."
      ),
      furtherReading = listOf(
        "Al-Maqasid al-Hasanah — Imam Shams al-Din al-Sakhawi",
        "Al-Manar al-Munif fi al-Sahih wa al-Da'if — Ibn Qayyim al-Jawziyyah"
      )
    ),
    EvidenceLearningModule(
      moduleNumber = 7,
      id = "module_7_hadith_vs_commentary",
      title = "7. How to distinguish hadith from commentary",
      arabicSubtitle = "التَّفْرِيقُ بَيْنَ النَّصِّ النَّبَوِيِّ وَشَرْحِ العُلَمَاء",
      shortExplanation = "A primary narration (matn) is the exact statement reported from the Prophet (ﷺ). A commentary (sharh) or inserted explanation (idraj) is a Companion's, narrator's, or later scholar's explanation of what the text means. Confusing commentary with the prophetic text leads to attributing human opinions to revelation.",
      workedExampleTitle = "Worked Example: Narrator Idraj & Scholarly Sharh in Sahih al-Bukhari",
      workedExampleSourceRef = "Sahih al-Bukhari #3 (tahannuth = ta'abbud)",
      workedExampleArabic = "كَانَ يَخْلُو بِغَارِ حِرَاءٍ فَيَتَحَنَّثُ فِيهِ — وَهُوَ التَّعَبُّدُ — اللَّيَالِيَ ذَوَاتِ العَدَدِ",
      workedExampleAnalysis = "In Sahih al-Bukhari #3, the phrase 'and that means worship (wa huwa al-ta'abbud)' is an explanatory gloss by Imam al-Zuhri to clarify the rare word 'yatahannathu'. Hadith specialists carefully mark such glosses (mudraj) so the explanation is distinguished from the primary narration.",
      quiz = LearningModuleQuiz(
        question = "Why must a scholar's commentary (sharh) be kept visually and methodologically separate from the primary hadith text?",
        options = listOf(
          "Because the Prophetic text is primary revelation, whereas a scholar's commentary is human explanation subject to review",
          "Because commentary is prohibited in Islam",
          "Because scholars never explain hadiths",
          "Because Arabic texts cannot be explained"
        ),
        correctOptionIndex = 0,
        explanation = "Preserving the boundary between the verbatim text (nass) and human interpretation (fahm/sharh) protects both scriptural integrity and scholarly clarity."
      ),
      furtherReading = listOf(
        "Fath al-Bari bi-Sharh Sahih al-Bukhari — Hafiz Ibn Hajar",
        "Al-Minhaj bi-Sharh Sahih Muslim — Imam al-Nawawi"
      )
    ),
    EvidenceLearningModule(
      moduleNumber = 8,
      id = "module_8_investigate_history",
      title = "8. How to investigate historical reports",
      arabicSubtitle = "ضَوَابِطُ نَقْدِ الرِّوَايَاتِ التَّارِيخِيَّة",
      shortExplanation = "Classical historians like Imam al-Tabari compiled all reports reaching them along with their chains so future scholars could examine each transmitter. A report found in a history book (tarikh) is not automatically authentic unless its chain is continuous and its narrators are reliable.",
      workedExampleTitle = "Worked Example: Imam al-Tabari's Preface & Quran 49:6",
      workedExampleSourceRef = "Tarikh al-Tabari Vol. 1, p. 6 & Quran 49:6",
      workedExampleArabic = "إِن جَاءَكُمْ فَاسِقٌ بِنَبَإٍ فَتَبَيَّنُوا",
      workedExampleAnalysis = "When a dramatic historical claim relies solely on an impugned or disconnected chronicler chain (such as uncorroborated reports from al-Waqidi or disconnected akhbari accounts), scholars classify it as requiring more evidence rather than historical fact.",
      quiz = LearningModuleQuiz(
        question = "Does the appearance of a story in Tarikh al-Tabari automatically prove that the event happened as described?",
        options = listOf(
          "No; al-Tabari explicitly stated that he recorded reports with their chains so readers could inspect the narrators' reliability",
          "Yes, every single sentence in every history book is 100% Sahih",
          "No, history books never contain any true events",
          "Only if the story is repeated on the internet"
        ),
        correctOptionIndex = 0,
        explanation = "Al-Tabari's methodology was 'man asnada laka faqad ahalaka' — by giving you the chain of narrators, he enabled you to verify the report."
      ),
      furtherReading = listOf(
        "Muqaddimah Tarikh al-Tabari — Imam Muhammad ibn Jarir al-Tabari",
        "Muqaddimah Ibn Khaldun — 'Abd al-Rahman ibn Khaldun"
      )
    ),
    EvidenceLearningModule(
      moduleNumber = 9,
      id = "module_9_unsupported_claims",
      title = "9. How to recognise unsupported religious claims",
      arabicSubtitle = "كَشْفُ الدَّعَاوَى الدِّينِيَّةِ غَيْرِ المُسْنَدَة",
      shortExplanation = "Unsupported religious claims often declare specific actions obligatory (wajib) or forbidden (haram), or promise specific numerical rewards on certain dates, without verified evidence from the Quran or authenticated Sunnah. Authority in Islam belongs to verified evidence, not a speaker's popularity.",
      workedExampleTitle = "Worked Example: Quran 16:116 & Quran 17:36 on Speaking Without Knowledge",
      workedExampleSourceRef = "Quran 16:116 & Quran 17:36",
      workedExampleArabic = "وَلَا تَقْفُ مَا لَيْسَ لَكَ بِهِ عِلْمٌ ۚ إِنَّ السَّمْعَ وَالْبَصَرَ وَالْفُؤَادَ كُلُّ أُولَٰئِكَ كَانَ عَنْهُ مَسْئُولًا",
      workedExampleAnalysis = "Quran 17:36 commands: 'And do not pursue that of which you have no knowledge. Indeed, the hearing, the sight and the heart — about all those [one] will be questioned.' When a speaker makes an unverified claim, we politely ask for the primary source and grading.",
      quiz = LearningModuleQuiz(
        question = "How should a student of knowledge evaluate a religious claim made by a popular speaker without citations?",
        options = listOf(
          "Check the claim against verified primary sources (Quran, authenticated Hadith, and documented scholarly consensus) rather than accepting popularity as proof",
          "Accept it immediately because the speaker has millions of followers",
          "Share it immediately before checking the source",
          "Assume the opposite is true without checking"
        ),
        correctOptionIndex = 0,
        explanation = "In Islamic epistemology, truth is known by its evidence (dalil), not merely by the fame of the speaker."
      ),
      furtherReading = listOf(
        "Surah Al-Isra 17:36 & Surah An-Nahl 16:116",
        "Al-I'tisam — Imam Abu Ishaq al-Shatibi"
      )
    ),
    EvidenceLearningModule(
      moduleNumber = 10,
      id = "module_10_disagree_respectfully",
      title = "10. How to disagree respectfully",
      arabicSubtitle = "أَدَبُ الاخْتِلَافِ وَالحِوَارِ العِلْمِيِّ الرَّصِين",
      shortExplanation = "Islamic scholarship has a rich ethical code for disagreement (Adab al-Ikhtilaf): represent the other scholar's or tradition's view accurately without selective quoting, avoid sectarian insults or abusive labels, distinguish definitive consensus (ijma' qat'i) from open ijtihadi questions, and speak with wisdom (Quran 16:125).",
      workedExampleTitle = "Worked Example: Surah An-Nahl 16:125 & Surah Al-Hujurat 49:11",
      workedExampleSourceRef = "Quran 16:125 & Quran 49:11",
      workedExampleArabic = "ادْعُ إِلَىٰ سَبِيلِ رَبِّكَ بِالْحِكْمَةِ وَالْمَوْعِظَةِ الْحَسَنَةِ ۖ وَجَادِلْهُم بِالَّتِي هِيَ أَحْسَنُ",
      workedExampleAnalysis = "Even when scholars hold strong evidence-based disagreements across Sunni madhahib or between Sunni and Twelver Shia traditions, Quran 16:125 commands arguing only in the way that is best (bil-lati hiya ahsan) and never distorting an opponent's words.",
      quiz = LearningModuleQuiz(
        question = "Which practice violates the Islamic ethics of scholarly disagreement (Adab al-Ikhtilaf)?",
        options = listOf(
          "Selectively quoting a source to reverse its meaning, using sectarian insults, or presenting a disputed issue as unanimous consensus",
          "Citing primary sources with exact edition and narration numbers",
          "Explaining both scholars' arguments accurately and respectfully",
          "Acknowledging when a historical report requires further evidence"
        ),
        correctOptionIndex = 0,
        explanation = "Honest scholarship requires representing differing views faithfully, citing primary sources accurately, and maintaining ethical dignity."
      ),
      furtherReading = listOf(
        "Adab al-Ikhtilaf fi al-Islam — Dr. Taha Jabir al-'Alwani",
        "Surah An-Nahl 16:125 & Surah Al-Hujurat 49:11–12"
      )
    )
  )

  /**
   * Priority 12 Section H — Submits a "Report Incorrect Reference" entry into Room DB for administrator review.
   */
  suspend fun reportIncorrectReference(
    claimOrQuery: String,
    referenceIdentifier: String,
    sourceCollection: String,
    issueCategory: String,
    userNotes: String
  ): Long {
    return downloadManager.submitIncorrectReferenceReport(
      claimOrQuery = claimOrQuery,
      referenceIdentifier = referenceIdentifier,
      sourceCollection = sourceCollection,
      issueCategory = issueCategory,
      userNotes = userNotes
    )
  }

  suspend fun getSubmittedReferenceReports(): List<IncorrectReferenceReportEntity> {
    return downloadManager.getAllIncorrectReferenceReports()
  }
}
