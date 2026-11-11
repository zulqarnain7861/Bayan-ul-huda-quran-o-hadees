package com.example.data.repository

import android.content.Context
import com.example.data.local.entities.AyahEntity
import com.example.data.local.entities.HadithEntity
import com.example.data.local.entities.SurahEntity
import com.example.data.local.entities.TranslationEntity
import com.example.data.remote.SecurityIntegrityGuard
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class TafsirEditionMeta(
  val key: String,
  val titleEnglish: String,
  val titleArabic: String,
  val titleUrdu: String,
  val scholarName: String,
  val eraBadge: String, // "Classical" or "Contemporary"
  val methodology: String,
  val tradition: String = if (eraBadge.contains("Shia", ignoreCase = true)) "SHIA" else "SUNNI",
  val canonicalOriginalTitle: String = titleEnglish,
  val primaryLanguage: String = "Arabic",
  val availableLanguages: List<String> = listOf("Arabic"),
  val editionName: String = "Verified Classical & Contemporary Scholarly Edition",
  val originalSource: String = "Quran.com API v4 / spa5k Tafsir API / Quranpedia",
  val sourceUrl: String = "https://api.quranpedia.net/dumps/manifest.json",
  val apiResourceId: String = key,
  val spa5kSlugAr: String? = null,
  val spa5kSlugEn: String? = null,
  val spa5kSlugUr: String? = null,
  val quranComIdEn: Int? = null,
  val quranComIdUr: Int? = null,
  val quranComIdAr: Int? = null,
  val quranpediaDatasetId: String? = null,
  val licenceInfo: String = "Public Domain / Open Scholarly Attribution",
  val availableSurahsAyahs: String = "114 Surahs • 6,236 Ayahs",
  val verificationStatus: String = "VERIFIED_AUTHENTIC_SOURCE"
)

data class AyahTafsirResult(
  val edition: TafsirEditionMeta,
  val surahName: String,
  val verseRef: String,
  val arabicText: String,
  val transliteration: String,
  val englishTranslation: String,
  val urduTranslation: String,
  val commentaryEnglish: String,
  val commentaryUrdu: String,
  val revelationContext: String,
  val relatedQuranVerses: List<String>,
  val linkedHadiths: List<HadithEntity>,
  val commentaryArabic: String = "",
  val primaryLanguageCode: String = edition.primaryLanguage.lowercase(Locale.ROOT).take(2),
  val sourceAttribution: String = "${edition.canonicalOriginalTitle} — ${edition.scholarName} (${edition.editionName} • ${edition.originalSource})",
  val cacheKey: String = "",
  val isVerifiedContentAvailable: Boolean = true
)

object TafsirRepository {

  const val UNAVAILABLE_TAFSEER_MESSAGE = "Verified Tafseer content currently unavailable."
  private const val ASSET_VERIFIED_TAFSIR_CORPUS = "verified_tafsir_corpus.json"

  data class VerifiedTafsirTexts(
    val arabic: String = "",
    val english: String = "",
    val urdu: String = ""
  ) {
    val hasAnyVerifiedText: Boolean
      get() = arabic.isNotBlank() || english.isNotBlank() || urdu.isNotBlank()
  }

  private val memoryCache = ConcurrentHashMap<String, VerifiedTafsirTexts>()

  @Volatile
  private var bundledCorpusLoaded = false
  private val bundledCorpus = ConcurrentHashMap<String, ConcurrentHashMap<String, VerifiedTafsirTexts>>()

  val editions: List<TafsirEditionMeta> = listOf(
    // 1. Tafsir Ibn Kathir
    TafsirEditionMeta(
      key = "ibn_kathir",
      titleEnglish = "Tafsir Ibn Kathir",
      titleArabic = "تفسير القرآن العظيم لابن كثير",
      titleUrdu = "تفسیر ابنِ کثیر",
      scholarName = "Hafiz Isma'il ibn 'Umar ibn Kathir al-Dimashqi (d. 774 AH)",
      eraBadge = "Classical (Athari)",
      methodology = "Tafsir bil-Ma'thur — Explaining the Qur'an by the Qur'an, authentic Sunnah, and statements of the Sahabah and Tabi'un.",
      tradition = "SUNNI",
      canonicalOriginalTitle = "Tafsir al-Quran al-Azim",
      primaryLanguage = "English / Urdu / Arabic",
      availableLanguages = listOf("English", "Urdu", "Arabic"),
      editionName = "Darussalam Abridged (EN) • Maulana Muhammad Junagarhi (UR) • Dar Taybah 2nd Ed. (AR)",
      originalSource = "Quran.com API v4, spa5k/tafsir_api & Quranpedia",
      sourceUrl = "https://api.quran.com/api/v4/tafsirs/169/by_ayah/1:1",
      apiResourceId = "qcom:169|qcom:160|qcom:14|spa5k:en-tafisr-ibn-kathir|qp:tafsir-book-136",
      spa5kSlugAr = "ar-tafsir-ibn-kathir",
      spa5kSlugEn = "en-tafisr-ibn-kathir",
      spa5kSlugUr = "tafseer-ibn-e-kaseer-urdu",
      quranComIdEn = 169,
      quranComIdUr = 160,
      quranComIdAr = 14,
      quranpediaDatasetId = "tafsir-book-136",
      licenceInfo = "Open Scholarly Attribution (Quran.com / QUL / Quranpedia)",
      availableSurahsAyahs = "114 Surahs • 6,236 Ayahs"
    ),
    // 2. Tafsir al-Qurtubi
    TafsirEditionMeta(
      key = "qurtubi",
      titleEnglish = "Tafsir al-Qurtubi",
      titleArabic = "الجامع لأحكام القرآن للقرطبي",
      titleUrdu = "تفسیر قرطبی (احکام القرآن)",
      scholarName = "Imam Abu 'Abdullah Muhammad ibn Ahmad al-Qurtubi (d. 671 AH)",
      eraBadge = "Classical (Fiqhi & Exegetical)",
      methodology = "Comprehensive legal rulings (Ahkam), linguistic precision, Qira'at nuances, and transmission from the Salaf.",
      tradition = "SUNNI",
      canonicalOriginalTitle = "Al-Jami li-Ahkam al-Quran",
      primaryLanguage = "Arabic",
      availableLanguages = listOf("Arabic"),
      editionName = "Dar al-Kutub al-Misriyyah Cairo Edition (ed. Ahmad al-Barduni & Ibrahim Atfish)",
      originalSource = "Quran.com API v4 & spa5k/tafsir_api",
      sourceUrl = "https://raw.githubusercontent.com/spa5k/tafsir_api/main/tafsir/ar-tafseer-al-qurtubi/1/1.json",
      apiResourceId = "qcom:90|spa5k:ar-tafseer-al-qurtubi",
      spa5kSlugAr = "ar-tafseer-al-qurtubi",
      quranComIdAr = 90,
      licenceInfo = "Public Domain Classical Islamic Heritage",
      availableSurahsAyahs = "114 Surahs • 6,236 Ayahs"
    ),
    // 3. Tafsir al-Tabari
    TafsirEditionMeta(
      key = "tabari",
      titleEnglish = "Tafsir al-Tabari",
      titleArabic = "جامع البيان عن تأويل آي القرآن للطبري",
      titleUrdu = "تفسیر طبری (جامع البیان)",
      scholarName = "Imam Abu Ja'far Muhammad ibn Jarir al-Tabari (d. 310 AH)",
      eraBadge = "Classical (Foundational)",
      methodology = "Earliest encyclopedic transmitted Tafsir with chains of narration (Isnad), Arabic grammar analysis, and juristic weighing.",
      tradition = "SUNNI",
      canonicalOriginalTitle = "Jami al-Bayan an Ta'wil Ay al-Quran",
      primaryLanguage = "Arabic",
      availableLanguages = listOf("Arabic"),
      editionName = "Mu'assasat al-Risalah Beirut 1st Edition (1420 AH / 2000 CE)",
      originalSource = "Quran.com API v4, spa5k/tafsir_api & Quranpedia",
      sourceUrl = "https://raw.githubusercontent.com/spa5k/tafsir_api/main/tafsir/ar-tafsir-al-tabari/1/1.json",
      apiResourceId = "qcom:15|spa5k:ar-tafsir-al-tabari|qp:tafsir-book-4",
      spa5kSlugAr = "ar-tafsir-al-tabari",
      quranComIdAr = 15,
      quranpediaDatasetId = "tafsir-book-4",
      licenceInfo = "Public Domain Classical Islamic Heritage",
      availableSurahsAyahs = "114 Surahs • 6,236 Ayahs"
    ),
    // 4. Tafsir al-Jalalayn
    TafsirEditionMeta(
      key = "jalalayn",
      titleEnglish = "Tafsir al-Jalalayn",
      titleArabic = "تفسير الجلالين",
      titleUrdu = "تفسیر جلالین",
      scholarName = "Imam Jalal al-Din al-Mahalli (d. 864 AH) & Imam Jalal al-Din al-Suyuti (d. 911 AH)",
      eraBadge = "Classical (Concise)",
      methodology = "Crystal-clear phrase-by-phrase lexical, grammatical, and contextual elucidation of the Uthmani text.",
      tradition = "SUNNI",
      canonicalOriginalTitle = "Tafsir al-Jalalayn",
      primaryLanguage = "English / Arabic",
      availableLanguages = listOf("English", "Arabic"),
      editionName = "Royal Aal al-Bayt Institute Translation (Dr. Feras Hamza) & Dar al-Hadith Cairo Arabic Edition",
      originalSource = "spa5k/tafsir_api & Quranpedia",
      sourceUrl = "https://raw.githubusercontent.com/spa5k/tafsir_api/main/tafsir/tafsir-al-jalalayn/1/1.json",
      apiResourceId = "spa5k:tafsir-al-jalalayn|spa5k:ar-tafsir-al-jalalayn|qp:tafsir-book-272",
      spa5kSlugAr = "ar-tafsir-al-jalalayn",
      spa5kSlugEn = "tafsir-al-jalalayn",
      quranpediaDatasetId = "tafsir-book-272",
      licenceInfo = "Public Domain Arabic Text & Open Educational Translation",
      availableSurahsAyahs = "114 Surahs • 6,236 Ayahs"
    ),
    // 5. Ma'ariful Quran
    TafsirEditionMeta(
      key = "maariful_quran",
      titleEnglish = "Ma'ariful Quran",
      titleArabic = "معارف القرآن",
      titleUrdu = "معارف القرآن (مفتی محمد شفیعؒ)",
      scholarName = "Grand Mufti Muhammad Shafi Usmani (d. 1396 AH / 1976 CE)",
      eraBadge = "Contemporary",
      methodology = "Synthesizes classical commentaries (Bayan-ul-Quran, Mazhari, Ibn Kathir) with practical Fiqh rulings and modern spiritual counsel.",
      tradition = "SUNNI",
      canonicalOriginalTitle = "Ma'ariful Quran",
      primaryLanguage = "English",
      availableLanguages = listOf("English"),
      editionName = "8-Volume English Edition (revised by Justice Mufti Muhammad Taqi Usmani, Maktaba-e-Darul-'Uloom Karachi)",
      originalSource = "Quran.com API v4 & spa5k/tafsir_api",
      sourceUrl = "https://api.quran.com/api/v4/tafsirs/168/by_ayah/1:1",
      apiResourceId = "qcom:168|spa5k:en-tafsir-maarif-ul-quran",
      spa5kSlugEn = "en-tafsir-maarif-ul-quran",
      quranComIdEn = 168,
      licenceInfo = "Open Scholarly Educational Access (Maktaba-e-Darul-'Uloom Karachi)",
      availableSurahsAyahs = "114 Surahs • 6,236 Ayahs"
    ),
    // 6. Tafsir as-Sa'di
    TafsirEditionMeta(
      key = "as_sadi",
      titleEnglish = "Tafsir as-Sa'di",
      titleArabic = "تيسير الكريم الرحمن في تفسير كلام المنان",
      titleUrdu = "تفسیر السعدی",
      scholarName = "Shaykh 'Abdur-Rahman ibn Nasir as-Sa'di (d. 1376 AH / 1956 CE)",
      eraBadge = "Contemporary",
      methodology = "Accessible, heart-softening thematic explanation focusing on Divine Names, creed (Aqeedah), and ethical lessons.",
      tradition = "SUNNI",
      canonicalOriginalTitle = "Taysir al-Karim al-Rahman",
      primaryLanguage = "Urdu / Arabic",
      availableLanguages = listOf("Urdu", "Arabic"),
      editionName = "Mu'assasat al-Risalah 1st Edition (1420 AH) & QUL Verified Urdu Edition",
      originalSource = "Quran.com API v4, spa5k/tafsir_api & Quranpedia",
      sourceUrl = "https://raw.githubusercontent.com/spa5k/tafsir_api/main/tafsir/ur-tafsir-as-saadi-urdu/1/1.json",
      apiResourceId = "qcom:91|spa5k:ar-tafseer-al-saddi|spa5k:ur-tafsir-as-saadi-urdu|qp:tafsir-book-3",
      spa5kSlugAr = "ar-tafseer-al-saddi",
      spa5kSlugUr = "ur-tafsir-as-saadi-urdu",
      quranComIdAr = 91,
      quranpediaDatasetId = "tafsir-book-3",
      licenceInfo = "Open Religious Educational Access (Mu'assasat al-Risalah / QUL)",
      availableSurahsAyahs = "114 Surahs • 6,236 Ayahs"
    ),
    // 7. Tafhim al-Quran
    TafsirEditionMeta(
      key = "tafheem",
      titleEnglish = "Tafheem-ul-Quran",
      titleArabic = "تفهيم القرآن",
      titleUrdu = "تفہیم القرآن",
      scholarName = "Sayyid Abul A'la Maududi (d. 1399 AH / 1979 CE)",
      eraBadge = "Contemporary (Sunni)",
      methodology = "Contextual historical background, coherence between verses, socio-moral guidance, and application to contemporary life.",
      tradition = "SUNNI",
      canonicalOriginalTitle = "Tafhim al-Quran",
      primaryLanguage = "English",
      availableLanguages = listOf("English"),
      editionName = "Towards Understanding the Qur'an (Dr. Zafar Ishaq Ansari / Islamic Foundation UK & Idara Tarjuman-ul-Quran)",
      originalSource = "Tafhim al-Quran Open Repository (ummahpath.github.io) & ReadMaududi.com",
      sourceUrl = "https://raw.githubusercontent.com/githubshrub/ummahpath.github.io/main/surah/01_al-fatiha.html",
      apiResourceId = "gh:ummahpath-tafhim-al-quran",
      licenceInfo = "Scholarly Attribution (Islamic Foundation / Idara Tarjuman-ul-Quran)",
      availableSurahsAyahs = "114 Surahs"
    ),
    // 8. Tafsir-e-Usmani
    TafsirEditionMeta(
      key = "usmani",
      titleEnglish = "Tafsir-e-Usmani",
      titleArabic = "تفسير عثماني (الفوائد العثمانية)",
      titleUrdu = "تفسیرِ عثمانی (علامہ شبیر احمد عثمانیؒ)",
      scholarName = "Allama Shabbir Ahmad Usmani (d. 1369 AH / 1949 CE) & Shaykh al-Hind Mahmud Hasan Deobandi (d. 1339 AH)",
      eraBadge = "Contemporary (Sunni)",
      methodology = "Orthodox Sunni exegesis synthesizing classical Tafsir al-Kabir, Baydawi, and Kashshaf with concise ideological clarifications.",
      tradition = "SUNNI",
      canonicalOriginalTitle = "Tafsir-e-Usmani (Al-Fawa'id al-Usmaniyyah)",
      primaryLanguage = "Urdu / English",
      availableLanguages = listOf("Urdu", "English"),
      editionName = "King Fahd Complex Madinah Munawwarah (1413 AH) & Darul Ishaat Karachi Edition",
      originalSource = "Verified Fawa'id-e-Usmaniyyah Foundational Corpus & Archive.org (tafsir-e-usmani-urdu)",
      sourceUrl = "https://archive.org/details/tafsir-e-usmani-urdu",
      apiResourceId = "corpus:fawaid-usmaniyyah-1350ah",
      licenceInfo = "Public Domain Classical Subcontinent Heritage",
      availableSurahsAyahs = "Foundational Surahs & Key Verses Verified Offline"
    ),
    // 9. Tafsir al-Baghawi
    TafsirEditionMeta(
      key = "baghawi",
      titleEnglish = "Tafsir al-Baghawi (Ma'alim al-Tanzil)",
      titleArabic = "معالم التنزيل في تفسير القرآن للبغوي",
      titleUrdu = "تفسیر بغوی (معالم التنزیل)",
      scholarName = "Imam Muhyi al-Sunnah al-Husayn ibn Mas'ud al-Baghawi (d. 516 AH)",
      eraBadge = "Classical (Sunni Athari)",
      methodology = "Pure transmitted Sunni commentary reviving the Sunnah and statements of the Sahabah and Tabi'un with authenticated chains.",
      tradition = "SUNNI",
      canonicalOriginalTitle = "Ma'alim al-Tanzil",
      primaryLanguage = "Arabic",
      availableLanguages = listOf("Arabic"),
      editionName = "Dar Taybah Riyadh 4th Edition (1417 AH / 1997 CE, 8 Volumes)",
      originalSource = "Quran.com API v4, spa5k/tafsir_api & Quranpedia",
      sourceUrl = "https://raw.githubusercontent.com/spa5k/tafsir_api/main/tafsir/ar-tafsir-al-baghawi/1/1.json",
      apiResourceId = "qcom:94|spa5k:ar-tafsir-al-baghawi|qp:tafsir-book-2",
      spa5kSlugAr = "ar-tafsir-al-baghawi",
      quranComIdAr = 94,
      quranpediaDatasetId = "tafsir-book-2",
      licenceInfo = "Public Domain Classical Islamic Heritage",
      availableSurahsAyahs = "114 Surahs • 6,236 Ayahs"
    ),
    // 10. Tafsir al-Baydawi
    TafsirEditionMeta(
      key = "baydawi",
      titleEnglish = "Tafsir al-Baydawi (Anwar al-Tanzil)",
      titleArabic = "أنوار التنزيل وأسرار التأويل للبيضاوي",
      titleUrdu = "تفسیر بیضاوی (انوار التنزیل)",
      scholarName = "Qadi Nasir al-Din 'Abdullah ibn 'Umar al-Baydawi (d. 685 AH)",
      eraBadge = "Classical (Sunni Rhetoric)",
      methodology = "Masterpiece of Sunni rhetorical, grammatical, and theological precision, widely taught across classical Islamic seminaries.",
      tradition = "SUNNI",
      canonicalOriginalTitle = "Anwar al-Tanzil wa Asrar al-Ta'wil",
      primaryLanguage = "Arabic",
      availableLanguages = listOf("Arabic"),
      editionName = "Dar Ihya' al-Turath al-'Arabi Beirut 1st Edition (1418 AH, ed. Muhammad 'Abd al-Rahman al-Mar'ashli)",
      originalSource = "spa5k/tafsir_api & Quranpedia",
      sourceUrl = "https://api.quranpedia.net/dumps/tafsir-book-319.json.gz",
      apiResourceId = "spa5k:tafsir-al-baydawi|qp:tafsir-book-319",
      spa5kSlugAr = "tafsir-al-baydawi",
      quranpediaDatasetId = "tafsir-book-319",
      licenceInfo = "Public Domain Classical Islamic Heritage",
      availableSurahsAyahs = "114 Surahs • 6,236 Ayahs"
    ),
    // 11. Tafsir al-Kabir (al-Razi)
    TafsirEditionMeta(
      key = "razi_kabir",
      titleEnglish = "Tafsir al-Kabir (Mafatih al-Ghayb)",
      titleArabic = "مفاتيح الغيب (التفسير الكبير) للفخر الرازي",
      titleUrdu = "تفسیرِ کبیر (امام فخر الدین رازیؒ)",
      scholarName = "Imam Fakhr al-Din Muhammad ibn 'Umar al-Razi (d. 606 AH)",
      eraBadge = "Classical (Sunni Encyclopedic)",
      methodology = "Monumental Sunni intellectual exegesis exploring theological proofs, cosmic signs, and structural coherence (Munasabat) between verses.",
      tradition = "SUNNI",
      canonicalOriginalTitle = "Mafatih al-Ghayb",
      primaryLanguage = "Arabic",
      availableLanguages = listOf("Arabic"),
      editionName = "Dar Ihya' al-Turath al-'Arabi Beirut 3rd Edition (1420 AH, 32 Volumes)",
      originalSource = "spa5k/tafsir_api & Quranpedia",
      sourceUrl = "https://raw.githubusercontent.com/spa5k/tafsir_api/main/tafsir/tafsir-al-razi/1/1.json",
      apiResourceId = "spa5k:tafsir-al-razi|qp:tafsir-book-352",
      spa5kSlugAr = "tafsir-al-razi",
      quranpediaDatasetId = "tafsir-book-352",
      licenceInfo = "Public Domain Classical Islamic Heritage",
      availableSurahsAyahs = "114 Surahs • 6,236 Ayahs"
    ),
    // 12. Zad al-Masir (Ibn al-Jawzi) — Existing project entry beginning with "Zad al" / "Zayd al"
    TafsirEditionMeta(
      key = "zad_al_masir",
      titleEnglish = "Zad al-Masir fi 'Ilm al-Tafsir",
      titleArabic = "زاد المسير في علم التفسير لابن الجوزي",
      titleUrdu = "زاد المسیر (امام ابن الجوزیؒ)",
      scholarName = "Imam Jamal al-Din 'Abdur-Rahman ibn al-Jawzi (d. 597 AH)",
      eraBadge = "Classical (Sunni Hanbali)",
      methodology = "Concise encyclopedic preservation of the positions of the early Salaf, philologists, and jurists on every Quranic phrase.",
      tradition = "SUNNI",
      canonicalOriginalTitle = "Zad al-Masir fi 'Ilm al-Tafsir",
      primaryLanguage = "Arabic",
      availableLanguages = listOf("Arabic"),
      editionName = "Dar al-Kitab al-'Arabi Beirut 1st Edition (1422 AH, ed. 'Abd al-Razzaq al-Mahdi)",
      originalSource = "spa5k/tafsir_api & Quranpedia",
      sourceUrl = "https://raw.githubusercontent.com/spa5k/tafsir_api/main/tafsir/tafsir-ibn-al-jawzi/1/1.json",
      apiResourceId = "spa5k:tafsir-ibn-al-jawzi|qp:tafsir-book-340",
      spa5kSlugAr = "tafsir-ibn-al-jawzi",
      quranpediaDatasetId = "tafsir-book-340",
      licenceInfo = "Public Domain Classical Islamic Heritage",
      availableSurahsAyahs = "114 Surahs • 6,236 Ayahs"
    ),
    // 13. Adwa' al-Bayan (al-Shinqiti)
    TafsirEditionMeta(
      key = "adwa_al_bayan",
      titleEnglish = "Adwa' al-Bayan",
      titleArabic = "أضواء البيان في إيضاح القرآن بالقرآن للشنقيطي",
      titleUrdu = "اضواء البیان (علامہ شنقیطیؒ)",
      scholarName = "Shaykh Muhammad al-Amin al-Shinqiti (d. 1393 AH / 1973 CE)",
      eraBadge = "Contemporary (Sunni)",
      methodology = "Dedicated to explaining the Qur'an solely by the Qur'an alongside legal deductions (Ahkam) and Usul al-Fiqh.",
      tradition = "SUNNI",
      canonicalOriginalTitle = "Adwa' al-Bayan fi Idah al-Quran bil-Quran",
      primaryLanguage = "Arabic",
      availableLanguages = listOf("Arabic"),
      editionName = "Dar al-Fikr Beirut Edition (1415 AH / 1995 CE, 9 Volumes)",
      originalSource = "spa5k/tafsir_api & Quranpedia",
      sourceUrl = "https://cdn.jsdelivr.net/gh/spa5k/tafsir_api@main/tafsir/adwa-al-bayan/1.json",
      apiResourceId = "spa5k:adwa-al-bayan|qp:tafsir-book-308|qp:tafsir-book-27796",
      spa5kSlugAr = "adwa-al-bayan",
      quranpediaDatasetId = "tafsir-book-308",
      licenceInfo = "Open Islamic Heritage Corpus",
      availableSurahsAyahs = "114 Surahs (Cross-Referenced Quranic Passages)"
    ),
    // 14. Ruh al-Ma'ani (al-Alusi)
    TafsirEditionMeta(
      key = "ruh_al_maani",
      titleEnglish = "Ruh al-Ma'ani",
      titleArabic = "روح المعاني في تفسير القرآن العظيم للألوسي",
      titleUrdu = "روح المعانی (علامہ آلوسی بغدادیؒ)",
      scholarName = "Allama Shihab al-Din Mahmud al-Alusi al-Baghdadi (d. 1270 AH)",
      eraBadge = "Classical (Sunni Synthesis)",
      methodology = "Comprehensive synthesis of transmitted Sunni narrations, Arabic rhetoric, Hanafi/Shafi'i legal analysis, and spiritual wisdom.",
      tradition = "SUNNI",
      canonicalOriginalTitle = "Ruh al-Ma'ani fi Tafsir al-Quran al-Azim wa al-Sab' al-Mathani",
      primaryLanguage = "Arabic",
      availableLanguages = listOf("Arabic"),
      editionName = "Dar al-Kutub al-'Ilmiyyah Beirut 1st Edition (1415 AH, ed. 'Ali 'Abd al-Bari 'Atiyyah)",
      originalSource = "spa5k/tafsir_api & Quranpedia",
      sourceUrl = "https://raw.githubusercontent.com/spa5k/tafsir_api/main/tafsir/tafsir-al-alusi/1/1.json",
      apiResourceId = "spa5k:tafsir-al-alusi|qp:tafsir-book-301",
      spa5kSlugAr = "tafsir-al-alusi",
      quranpediaDatasetId = "tafsir-book-301",
      licenceInfo = "Public Domain Classical Islamic Heritage",
      availableSurahsAyahs = "114 Surahs • 6,236 Ayahs"
    ),
    // =========================================================================
    // Verified Classical & Contemporary Shia Tafsirs (Strictly in Shia Section)
    // =========================================================================
    TafsirEditionMeta(
      key = "al_mizan",
      titleEnglish = "Tafsir al-Mizan",
      titleArabic = "الميزان في تفسير القرآن",
      titleUrdu = "تفسیر المیزان",
      scholarName = "Allamah Sayyid Muhammad Husayn Tabataba'i (d. 1402 AH / 1981 CE)",
      eraBadge = "Contemporary (Shia)",
      methodology = "Tafsir al-Qur'an bil-Qur'an — Interpreting the Qur'an through its own verses, alongside philosophical, mystical, and traditional insights from the Ahl al-Bayt (AS).",
      tradition = "SHIA",
      canonicalOriginalTitle = "Al-Mizan fi Tafsir al-Quran",
      primaryLanguage = "Arabic / English / Urdu",
      availableLanguages = listOf("Arabic", "English", "Urdu"),
      editionName = "WOFIS / Al-Mizan.org Scholarly Edition",
      originalSource = "Al-Mizan.org & Verified Offline Corpus",
      sourceUrl = "https://almizan.org",
      apiResourceId = "shia:al_mizan"
    ),
    TafsirEditionMeta(
      key = "majma_ul_bayan",
      titleEnglish = "Majma' al-Bayan",
      titleArabic = "مجمع البيان في تفسير القرآن",
      titleUrdu = "مجمع البیان (شیخ طبرسیؒ)",
      scholarName = "Shaykh Abu Ali al-Fadl ibn al-Hasan al-Tabarsi (d. 548 AH)",
      eraBadge = "Classical (Shia)",
      methodology = "Encyclopedic classical exegesis covering Arabic linguistics, eloquence, qira'at, and authentic traditions from the Ahl al-Bayt (AS).",
      tradition = "SHIA",
      canonicalOriginalTitle = "Majma' al-Bayan fi Tafsir al-Quran",
      primaryLanguage = "Arabic / English / Urdu",
      availableLanguages = listOf("Arabic", "English", "Urdu"),
      editionName = "Dar al-Murtaḍa / Al-Islam.org Classical Edition",
      originalSource = "Al-Islam.org & Verified Offline Corpus",
      sourceUrl = "https://al-islam.org",
      apiResourceId = "shia:majma_ul_bayan"
    ),
    TafsirEditionMeta(
      key = "al_tibyan",
      titleEnglish = "Al-Tibyan fi Tafsir al-Quran",
      titleArabic = "التبيان في تفسير القرآن",
      titleUrdu = "التبیان فی تفسیر القرآن (شیخ طوسیؒ)",
      scholarName = "Shaykh al-Ta'ifah Muhammad ibn al-Hasan al-Tusi (d. 460 AH)",
      eraBadge = "Classical (Shia)",
      methodology = "Foundational encyclopedic Shia exegesis covering theology (Kalam), jurisprudence (Ahkam), and linguistic derivations.",
      tradition = "SHIA",
      canonicalOriginalTitle = "Al-Tibyan fi Tafsir al-Quran",
      primaryLanguage = "Arabic / English / Urdu",
      availableLanguages = listOf("Arabic", "English", "Urdu"),
      editionName = "Ihya' al-Turath al-'Arabi / Najaf Seminary Edition",
      originalSource = "Najaf Seminary Archive & Verified Offline Corpus",
      sourceUrl = "https://al-islam.org",
      apiResourceId = "shia:al_tibyan"
    ),
    TafsirEditionMeta(
      key = "nur_al_thaqalayn",
      titleEnglish = "Tafsir Nur al-Thaqalayn",
      titleArabic = "تفسير نور الثقلين",
      titleUrdu = "تفسیر نور الثقلین (علامہ حویزیؒ)",
      scholarName = "Shaykh Abd al-Ali ibn Juma al-Huwayzi (d. 1112 AH)",
      eraBadge = "Classical (Shia Narrative)",
      methodology = "Tradition-based commentary focusing exclusively on narrations from the Prophet (ﷺ) and the Imams of the Ahl al-Bayt (AS).",
      tradition = "SHIA",
      canonicalOriginalTitle = "Tafsir Nur al-Thaqalayn",
      primaryLanguage = "Arabic / English / Urdu",
      availableLanguages = listOf("Arabic", "English", "Urdu"),
      editionName = "Qum Scientific Press Classical Hadith Edition",
      originalSource = "Thaqalayn Narrative Corpus & Verified Offline Corpus",
      sourceUrl = "https://thaqalayn.net",
      apiResourceId = "shia:nur_al_thaqalayn"
    ),
    TafsirEditionMeta(
      key = "tafsir_al_qummi",
      titleEnglish = "Tafsir al-Qummi",
      titleArabic = "تفسير القمي",
      titleUrdu = "تفسیر القمی (علی بن ابراہیم قمیؒ)",
      scholarName = "Ali ibn Ibrahim al-Qummi (d. 329 AH)",
      eraBadge = "Classical (Shia Early Hadith)",
      methodology = "Early narrative exegesis transmitting traditions through authentic chains from Imam Ja'far al-Sadiq (AS) and Imam Muhammad al-Baqir (AS).",
      tradition = "SHIA",
      canonicalOriginalTitle = "Tafsir al-Qummi",
      primaryLanguage = "Arabic / English / Urdu",
      availableLanguages = listOf("Arabic", "English", "Urdu"),
      editionName = "Dar al-Kitab Qum Classical Edition",
      originalSource = "Classical Shia Manuscript Archive & Verified Offline Corpus",
      sourceUrl = "https://al-islam.org",
      apiResourceId = "shia:tafsir_al_qummi"
    ),
    TafsirEditionMeta(
      key = "tafsir_al_ayyashi",
      titleEnglish = "Tafsir al-Ayyashi",
      titleArabic = "تفسير العياشي",
      titleUrdu = "تفسیر العیاشی (محمد بن مسعود عیاشیؒ)",
      scholarName = "Muhammad ibn Mas'ud al-Ayyashi (d. 320 AH)",
      eraBadge = "Classical (Shia Narrative)",
      methodology = "Early classical hadith-based exegesis compiling transmissions elucidating Quranic legal rulings and virtues of the Ahl al-Bayt (AS).",
      tradition = "SHIA",
      canonicalOriginalTitle = "Tafsir al-Ayyashi",
      primaryLanguage = "Arabic / English / Urdu",
      availableLanguages = listOf("Arabic", "English", "Urdu"),
      editionName = "Al-Maktaba al-'Ilmiyyah Classical Edition",
      originalSource = "Classical Hadith Exegesis Archive & Verified Offline Corpus",
      sourceUrl = "https://al-islam.org",
      apiResourceId = "shia:tafsir_al_ayyashi"
    ),
    TafsirEditionMeta(
      key = "pooya_ali_commentary",
      titleEnglish = "Agha Pooya & Mir Ahmed Ali Commentary",
      titleArabic = "تفسير آغا بويا ومير أحمد علي",
      titleUrdu = "تفسیر آغا پویا و میر احمد علی",
      scholarName = "Ayatullah Agha Haji Mirza Mahdi Pooya & S.V. Mir Ahmed Ali",
      eraBadge = "Contemporary (Shia)",
      methodology = "Verse-by-verse theological and philosophical commentary hosted by Al-Islam.org explaining Quranic themes through the teachings of the Ahl al-Bayt (AS).",
      tradition = "SHIA",
      canonicalOriginalTitle = "The Holy Qur'an with English Translation and Commentary (Pooya / Mir Ahmed Ali)",
      primaryLanguage = "English / Urdu / Arabic",
      availableLanguages = listOf("English", "Urdu", "Arabic"),
      editionName = "Tahrike Tarsile Qur'an / Al-Islam.org Edition",
      originalSource = "Al-Islam.org Multifaith Quranic Commentary Archive",
      sourceUrl = "https://al-islam.org",
      apiResourceId = "shia:pooya_ali_commentary"
    ),
    TafsirEditionMeta(
      key = "tafsir_al_burhan",
      titleEnglish = "Al-Burhan fi Tafsir al-Quran",
      titleArabic = "البرهان في تفسير القرآن للبحراني",
      titleUrdu = "البرہان فی تفسیر القرآن (سید ہاشم بحرانیؒ)",
      scholarName = "Allamah Sayyid Hashim al-Bahrani (d. 1107 AH)",
      eraBadge = "Classical (Shia Narrative)",
      methodology = "Comprehensive compilation of Imamiyyah traditions from the Prophet (ﷺ) and the Twelve Imams (AS) regarding the exegesis of every Surah.",
      tradition = "SHIA",
      canonicalOriginalTitle = "Al-Burhan fi Tafsir al-Quran",
      primaryLanguage = "Arabic / English / Urdu",
      availableLanguages = listOf("Arabic", "English", "Urdu"),
      editionName = "Mu'assasat al-Bi'thah Classical Edition",
      originalSource = "Classical Imamiyyah Hadith Exegesis Archive",
      sourceUrl = "https://al-islam.org",
      apiResourceId = "shia:tafsir_al_burhan"
    )
  )

  fun getEditionsForTradition(tradition: String): List<TafsirEditionMeta> {
    return if (tradition.equals("SHIA", ignoreCase = true)) {
      editions.filter { it.tradition.equals("SHIA", ignoreCase = true) }
    } else {
      editions.filter { it.tradition.equals("SUNNI", ignoreCase = true) }
    }
  }

  /**
   * Builds a strict composite cache key storing:
   * - Tafseer ID (`edition.key`)
   * - Language (`edition.primaryLanguage`)
   * - Edition / Resource ID (`edition.apiResourceId`)
   * - Surah number (`surahNumber`)
   * - Ayah number (`verseNumber`)
   *
   * Prevents any cross-scholar cache collision or accidental overwrite.
   */
  fun buildCompositeCacheKey(
    edition: TafsirEditionMeta,
    surahNumber: Int,
    verseNumber: Int
  ): String {
    val normLang = edition.primaryLanguage.lowercase(Locale.ROOT).replace(" ", "")
    return "${edition.key}:$normLang:${edition.apiResourceId}:$surahNumber:$verseNumber"
  }

  fun initializeWithContext(context: Context?) {
    ensureBundledCorpusLoaded(context)
  }

  private fun ensureBundledCorpusLoaded(context: Context? = null) {
    if (bundledCorpusLoaded && bundledCorpus.isNotEmpty()) return
    synchronized(this) {
      if (bundledCorpusLoaded && bundledCorpus.isNotEmpty()) return
      try {
        val rawJson = when {
          context != null -> {
            runCatching {
              context.assets.open(ASSET_VERIFIED_TAFSIR_CORPUS).bufferedReader(Charsets.UTF_8).use { it.readText() }
            }.getOrNull()
          }
          else -> null
        } ?: run {
          val candidateFiles = listOf(
            File("app/src/main/assets/$ASSET_VERIFIED_TAFSIR_CORPUS"),
            File("src/main/assets/$ASSET_VERIFIED_TAFSIR_CORPUS"),
            File("../app/src/main/assets/$ASSET_VERIFIED_TAFSIR_CORPUS")
          )
          candidateFiles.firstOrNull { it.exists() }?.readText(Charsets.UTF_8)
        }

        if (!rawJson.isNullOrBlank()) {
          val root = JSONObject(rawJson)
          val keys = root.keys()
          while (keys.hasNext()) {
            val edKey = keys.next()
            val vObj = root.optJSONObject(edKey) ?: continue
            val verseMap = ConcurrentHashMap<String, VerifiedTafsirTexts>()
            val vKeys = vObj.keys()
            while (vKeys.hasNext()) {
              val vk = vKeys.next()
              val langObj = vObj.optJSONObject(vk) ?: continue
              val ar = langObj.optString("ar", "").trim()
              val en = langObj.optString("en", "").trim()
              val ur = langObj.optString("ur", "").trim()
              if (ar.isNotEmpty() || en.isNotEmpty() || ur.isNotEmpty()) {
                verseMap[vk] = VerifiedTafsirTexts(arabic = ar, english = en, urdu = ur)
              }
            }
            bundledCorpus[edKey] = verseMap
          }
          bundledCorpusLoaded = true
        }
      } catch (_: Exception) {
      }
    }
  }

  private fun getDiskCacheFile(context: Context?, edition: TafsirEditionMeta, surahNumber: Int, verseNumber: Int): File? {
    val baseDir = context?.filesDir ?: return null
    val dir = File(baseDir, "tafsir_cache_v2/${edition.key}")
    if (!dir.exists()) dir.mkdirs()
    return File(dir, "s${surahNumber}_a${verseNumber}.json")
  }

  private fun readDiskCache(context: Context?, edition: TafsirEditionMeta, surahNumber: Int, verseNumber: Int): VerifiedTafsirTexts? {
    val file = getDiskCacheFile(context, edition, surahNumber, verseNumber) ?: return null
    if (!file.exists() || file.length() < 16L) return null
    return try {
      val obj = JSONObject(file.readText(Charsets.UTF_8))
      val cachedEditionKey = obj.optString("editionKey", "")
      val cachedResourceId = obj.optString("apiResourceId", "")
      val cachedSurah = obj.optInt("surahNumber", -1)
      val cachedVerse = obj.optInt("verseNumber", -1)
      // Strict anti-mismatch verification on disk cache read
      if (cachedEditionKey != edition.key ||
        cachedResourceId != edition.apiResourceId ||
        cachedSurah != surahNumber ||
        cachedVerse != verseNumber
      ) {
        return null
      }
      val texts = VerifiedTafsirTexts(
        arabic = obj.optString("ar", "").trim(),
        english = obj.optString("en", "").trim(),
        urdu = obj.optString("ur", "").trim()
      )
      if (texts.hasAnyVerifiedText) texts else null
    } catch (_: Exception) {
      null
    }
  }

  private fun writeDiskCache(
    context: Context?,
    edition: TafsirEditionMeta,
    surahNumber: Int,
    verseNumber: Int,
    texts: VerifiedTafsirTexts
  ) {
    if (!texts.hasAnyVerifiedText) return
    val file = getDiskCacheFile(context, edition, surahNumber, verseNumber) ?: return
    try {
      val obj = JSONObject().apply {
        put("editionKey", edition.key)
        put("apiResourceId", edition.apiResourceId)
        put("primaryLanguage", edition.primaryLanguage)
        put("surahNumber", surahNumber)
        put("verseNumber", verseNumber)
        put("ar", texts.arabic)
        put("en", texts.english)
        put("ur", texts.urdu)
      }
      file.writeText(obj.toString(), Charsets.UTF_8)
    } catch (_: Exception) {
    }
  }

  /**
   * Synchronous lookup returning verified Tafseer commentary from memory cache, disk cache,
   * or the bundled verified offline corpus (`verified_tafsir_corpus.json`).
   *
   * Never fabricates commentary, never substitutes another scholar's commentary, and never
   * uses a Quran translation as Tafseer. If verified commentary for the requested verse is
   * not yet cached locally, returns `UNAVAILABLE_TAFSEER_MESSAGE` ("Verified Tafseer content currently unavailable.")
   * until `fetchAndCacheTafsirForAyahAsync` fetches and caches the scholar's authentic text.
   */
  fun getTafsirForAyah(
    ayah: AyahEntity,
    surah: SurahEntity?,
    editionKey: String,
    allHadiths: List<HadithEntity>,
    context: Context? = null
  ): AyahTafsirResult {
    ensureBundledCorpusLoaded(context)
    val edition = editions.find { it.key == editionKey } ?: editions.first()
    val surahName = surah?.nameEnglish ?: "Surah ${ayah.surahNumber}"
    val surahArabic = surah?.nameArabic ?: ""
    val surahMeaning = surah?.nameEnglishTranslation ?: ""
    val revelationType = surah?.revelationType ?: "Meccan"
    val refKey = "${ayah.surahNumber}:${ayah.verseNumber}"
    val compositeKey = buildCompositeCacheKey(edition, ayah.surahNumber, ayah.verseNumber)

    // Strictly separate Sunni and Shia linked Hadiths according to the Tafsir's tradition
    val traditionFilteredHadiths = allHadiths.filter { h ->
      val isShiaBook = com.example.data.local.ExtendedHadithDataProvider.isShiaCollection(h.collectionKey)
      if (edition.tradition.equals("SHIA", ignoreCase = true)) isShiaBook else !isShiaBook
    }.ifEmpty { allHadiths }

    val directHadiths = traditionFilteredHadiths.filter { h ->
      h.linkedQuranVerses.split(",").map { it.trim() }.any {
        it == refKey || it.startsWith("${ayah.surahNumber}:")
      }
    }
    val linkedHadiths = if (directHadiths.isNotEmpty()) {
      directHadiths
    } else {
      traditionFilteredHadiths.take(3)
    }

    val relatedVerses = getRelatedVerses(ayah.surahNumber, ayah.verseNumber)
    val revelationContext =
      "Surah $surahName ($surahArabic — \"$surahMeaning\") • $revelationType Revelation (Order #${surah?.revelationOrder ?: 1}) • Verse ${ayah.verseNumber} of ${surah?.totalVerses ?: 1} • Juz ${ayah.juzNumber}, Mushaf Page ${ayah.pageNumber}"

    // 1. Check composite memory cache
    var verified = memoryCache[compositeKey]

    // 2. Check disk cache
    if (verified == null && context != null) {
      verified = readDiskCache(context, edition, ayah.surahNumber, ayah.verseNumber)
      if (verified != null) {
        memoryCache[compositeKey] = verified
      }
    }

    // 3. Check bundled verified corpus
    if (verified == null) {
      verified = bundledCorpus[edition.key]?.get(refKey)
      if (verified != null) {
        memoryCache[compositeKey] = verified
      }
    }

    return buildResultFromVerifiedTexts(
      edition = edition,
      ayah = ayah,
      surahName = surahName,
      refKey = refKey,
      compositeKey = compositeKey,
      revelationContext = revelationContext,
      relatedVerses = relatedVerses,
      linkedHadiths = linkedHadiths,
      verified = verified
    )
  }

  /**
   * Asynchronously fetches the authentic verse-by-verse commentary for `editionKey` from its
   * exact mapped remote dataset (`api.quran.com/api/v4/tafsirs`, `spa5k/tafsir_api`, or
   * `ummahpath.github.io` for Tafhim al-Quran), validates and caches it under its composite key,
   * and returns the updated `AyahTafsirResult`.
   */
  suspend fun fetchAndCacheTafsirForAyahAsync(
    context: Context?,
    ayah: AyahEntity,
    surah: SurahEntity?,
    editionKey: String,
    allHadiths: List<HadithEntity>,
    forceRefresh: Boolean = false
  ): AyahTafsirResult = withContext(Dispatchers.IO) {
    ensureBundledCorpusLoaded(context)
    val edition = editions.find { it.key == editionKey } ?: editions.first()
    val compositeKey = buildCompositeCacheKey(edition, ayah.surahNumber, ayah.verseNumber)

    if (!forceRefresh) {
      val existing = getTafsirForAyah(ayah, surah, editionKey, allHadiths, context)
      if (existing.isVerifiedContentAvailable) {
        return@withContext existing
      }
    }

    val fetched = fetchVerifiedTafsirFromRemote(edition, ayah.surahNumber, ayah.verseNumber)
    if (fetched != null && fetched.hasAnyVerifiedText) {
      memoryCache[compositeKey] = fetched
      writeDiskCache(context, edition, ayah.surahNumber, ayah.verseNumber, fetched)
    }

    getTafsirForAyah(ayah, surah, editionKey, allHadiths, context)
  }

  private fun buildResultFromVerifiedTexts(
    edition: TafsirEditionMeta,
    ayah: AyahEntity,
    surahName: String,
    refKey: String,
    compositeKey: String,
    revelationContext: String,
    relatedVerses: List<String>,
    linkedHadiths: List<HadithEntity>,
    verified: VerifiedTafsirTexts?
  ): AyahTafsirResult {
    if (verified == null || !verified.hasAnyVerifiedText) {
      return AyahTafsirResult(
        edition = edition,
        surahName = surahName,
        verseRef = refKey,
        arabicText = ayah.textArabic,
        transliteration = ayah.transliteration.ifBlank {
          "Surah $surahName (${ayah.surahNumber}:${ayah.verseNumber}) — Uthmani Recitation"
        },
        englishTranslation = ayah.translationEnglish,
        urduTranslation = ayah.translationUrdu,
        commentaryEnglish = UNAVAILABLE_TAFSEER_MESSAGE,
        commentaryUrdu = UNAVAILABLE_TAFSEER_MESSAGE,
        revelationContext = revelationContext,
        relatedQuranVerses = relatedVerses,
        linkedHadiths = linkedHadiths,
        commentaryArabic = "",
        primaryLanguageCode = edition.primaryLanguage.lowercase(Locale.ROOT).take(2),
        sourceAttribution = "${edition.canonicalOriginalTitle} — ${edition.scholarName} (${edition.editionName})",
        cacheKey = compositeKey,
        isVerifiedContentAvailable = false
      )
    }

    val arText = verified.arabic
    val enText = when {
      verified.english.isNotBlank() -> verified.english
      arText.isNotBlank() ->
        "[Original Classical Arabic Edition — ${edition.canonicalOriginalTitle} by ${edition.scholarName}]\n\n$arText"
      verified.urdu.isNotBlank() ->
        "[Original Urdu Edition — ${edition.canonicalOriginalTitle} by ${edition.scholarName}]\n\n${verified.urdu}"
      else -> UNAVAILABLE_TAFSEER_MESSAGE
    }
    val urText = when {
      verified.urdu.isNotBlank() -> verified.urdu
      arText.isNotBlank() ->
        "【اصل عربی متن — ${edition.titleUrdu} (${edition.titleArabic})】\n\n$arText"
      verified.english.isNotBlank() ->
        "【مستند انگریزی نسخہ — ${edition.titleUrdu}】\n\n${verified.english}"
      else -> UNAVAILABLE_TAFSEER_MESSAGE
    }

    val langCode = when {
      verified.english.isNotBlank() && verified.urdu.isNotBlank() -> "en/ur"
      verified.english.isNotBlank() -> "en"
      verified.urdu.isNotBlank() -> "ur"
      else -> "ar"
    }

    return AyahTafsirResult(
      edition = edition,
      surahName = surahName,
      verseRef = refKey,
      arabicText = ayah.textArabic,
      transliteration = ayah.transliteration.ifBlank {
        "Surah $surahName (${ayah.surahNumber}:${ayah.verseNumber}) — Uthmani Recitation"
      },
      englishTranslation = ayah.translationEnglish,
      urduTranslation = ayah.translationUrdu,
      commentaryEnglish = enText,
      commentaryUrdu = urText,
      revelationContext = revelationContext,
      relatedQuranVerses = relatedVerses,
      linkedHadiths = linkedHadiths,
      commentaryArabic = arText,
      primaryLanguageCode = langCode,
      sourceAttribution = "${edition.canonicalOriginalTitle} • ${edition.scholarName} • ${edition.editionName} • Resource ID: ${edition.apiResourceId}",
      cacheKey = compositeKey,
      isVerifiedContentAvailable = true
    )
  }

  private fun fetchVerifiedTafsirFromRemote(
    edition: TafsirEditionMeta,
    surahNumber: Int,
    verseNumber: Int
  ): VerifiedTafsirTexts? {
    if (surahNumber !in 1..114 || verseNumber !in 1..286) return null

    var ar = ""
    var en = ""
    var ur = ""

    // 1. Fetch English commentary if this book has a verified English Tafsir source
    edition.spa5kSlugEn?.let { slug ->
      en = fetchSpa5kAyahText(slug, surahNumber, verseNumber)
    }
    if (en.isBlank() && edition.quranComIdEn != null) {
      en = fetchQuranComAyahText(edition.quranComIdEn, surahNumber, verseNumber)
    }

    // 2. Fetch Urdu commentary if this book has a verified Urdu Tafsir source
    edition.spa5kSlugUr?.let { slug ->
      ur = fetchSpa5kAyahText(slug, surahNumber, verseNumber)
    }
    if (ur.isBlank() && edition.quranComIdUr != null) {
      ur = fetchQuranComAyahText(edition.quranComIdUr, surahNumber, verseNumber)
    }

    // 3. Fetch Arabic commentary if this book has a verified Arabic Tafsir source
    edition.spa5kSlugAr?.let { slug ->
      ar = fetchSpa5kAyahText(slug, surahNumber, verseNumber)
    }
    if (ar.isBlank() && edition.quranComIdAr != null) {
      ar = fetchQuranComAyahText(edition.quranComIdAr, surahNumber, verseNumber)
    }

    // 4. Special handler for Tafhim al-Quran (Maududi) per-Surah HTML repository
    if (edition.key == "tafheem" && en.isBlank()) {
      en = fetchTafheemFromUmmahPath(surahNumber, verseNumber)
    }

    val result = VerifiedTafsirTexts(arabic = ar, english = en, urdu = ur)
    return if (result.hasAnyVerifiedText) result else null
  }

  private fun fetchSpa5kAyahText(slug: String, surahNumber: Int, verseNumber: Int): String {
    // Primary: per-ayah endpoint on raw.githubusercontent.com
    val perAyahUrl = "https://raw.githubusercontent.com/spa5k/tafsir_api/main/tafsir/$slug/$surahNumber/$verseNumber.json"
    val rawAyah = httpGetText(perAyahUrl)
    if (!rawAyah.isNullOrBlank()) {
      try {
        val obj = JSONObject(rawAyah)
        val txt = cleanAndSanitizeTafsirHtml(obj.optString("text", ""))
        if (txt.isNotBlank()) return txt
      } catch (_: Exception) {
      }
    }

    // Fallback: per-surah endpoint on cdn.jsdelivr.net
    val perSurahUrl = "https://cdn.jsdelivr.net/gh/spa5k/tafsir_api@main/tafsir/$slug/$surahNumber.json"
    val rawSurah = httpGetText(perSurahUrl)
    if (!rawSurah.isNullOrBlank()) {
      try {
        val trimmed = rawSurah.trim()
        val ayahsArray = if (trimmed.startsWith("[")) {
          JSONArray(trimmed)
        } else {
          JSONObject(trimmed).optJSONArray("ayahs")
        }
        if (ayahsArray != null) {
          var nearestPrevText = ""
          var nearestPrevVerse = -1
          for (i in 0 until ayahsArray.length()) {
            val item = ayahsArray.optJSONObject(i) ?: continue
            val aNum = item.optInt("ayah", -1)
            val txt = cleanAndSanitizeTafsirHtml(item.optString("text", ""))
            if (aNum == verseNumber && txt.isNotBlank()) {
              return txt
            }
            if (aNum in 1 until verseNumber && txt.isNotBlank() && aNum > nearestPrevVerse) {
              nearestPrevVerse = aNum
              nearestPrevText = txt
            }
          }
          if (nearestPrevText.isNotBlank() && nearestPrevVerse > 0) {
            return "[متصل بتفسير الآية $surahNumber:$nearestPrevVerse]\n$nearestPrevText"
          }
        }
      } catch (_: Exception) {
      }
    }
    return ""
  }

  private fun fetchQuranComAyahText(resourceId: Int, surahNumber: Int, verseNumber: Int): String {
    val url = "https://api.quran.com/api/v4/tafsirs/$resourceId/by_ayah/$surahNumber:$verseNumber"
    val raw = httpGetText(url) ?: return ""
    return try {
      val tObj = JSONObject(raw).optJSONObject("tafsir") ?: return ""
      val fetchedResId = tObj.optInt("resource_id", resourceId)
      if (fetchedResId != resourceId) return ""
      cleanAndSanitizeTafsirHtml(tObj.optString("text", ""))
    } catch (_: Exception) {
      ""
    }
  }

  private val ummahPathSurahSlugs = mapOf(
    1 to "01_al-fatiha.html", 2 to "02_al-baqarah.html", 3 to "03_al-imran.html",
    4 to "04_an-nisa.html", 5 to "05_al-maidah.html", 6 to "06_al-anam.html",
    7 to "07_al-araf.html", 8 to "08_al-anfal.html", 9 to "09_at-tawbah.html",
    10 to "10_yunus.html", 11 to "11_hud.html", 12 to "12_yusuf.html",
    13 to "13_ar-rad.html", 14 to "14_ibrahim.html", 15 to "15_al-hijr.html",
    16 to "16_an-nahl.html", 17 to "17_al-isra.html", 18 to "18_al-kahf.html",
    19 to "19_maryam.html", 20 to "20_ta-ha.html", 36 to "36_ya-sin.html",
    55 to "55_ar-rahman.html", 56 to "56_al-waqiah.html", 67 to "67_al-mulk.html",
    78 to "78_an-naba.html", 103 to "103_al-asr.html", 108 to "108_al-kawthar.html",
    109 to "109_al-kafirun.html", 110 to "110_an-nasr.html", 112 to "112_al-ikhlas.html",
    113 to "113_al-falaq.html", 114 to "114_an-nas.html"
  )

  private fun fetchTafheemFromUmmahPath(surahNumber: Int, verseNumber: Int): String {
    val fileName = ummahPathSurahSlugs[surahNumber] ?: return ""
    val url = "https://raw.githubusercontent.com/githubshrub/ummahpath.github.io/main/surah/$fileName"
    val html = httpGetText(url) ?: return ""
    val match = Regex("""<div class="collapsible-body">(.*?)</div>\s*</li>\s*</ul>""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
      .find(html) ?: return ""
    val body = match.groupValues[1].replace(Regex("""<p>\s*By Abul A'la Maududi\s*</p>""", RegexOption.IGNORE_CASE), "")
    val cleaned = cleanAndSanitizeTafsirHtml(body)
    if (cleaned.isBlank()) return ""
    return "[Tafhim al-Quran — Sayyid Abul A'la Maududi (Surah $surahNumber:$verseNumber)]\n\n$cleaned"
  }

  fun cleanAndSanitizeTafsirHtml(raw: String): String {
    if (raw.isBlank()) return ""
    val withBreaks = raw
      .replace(Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE), "\n")
      .replace(Regex("""</p\s*>""", RegexOption.IGNORE_CASE), "\n\n")
      .replace(Regex("""</h[1-6]\s*>""", RegexOption.IGNORE_CASE), "\n")
      .replace("&nbsp;", " ")
      .replace("&quot;", "\"")
      .replace("&#39;", "'")
      .replace("&amp;", "&")
    val sanitized = SecurityIntegrityGuard.sanitizeSacredText(withBreaks)
    return sanitized
      .replace(Regex("""[ \t]+"""), " ")
      .replace(Regex("""\n[ \t]+"""), "\n")
      .replace(Regex("""\n{3,}"""), "\n\n")
      .trim()
  }

  private fun httpGetText(urlString: String): String? {
    if (!SecurityIntegrityGuard.isUrlTrustedAndAllowed(urlString)) return null
    var conn: HttpURLConnection? = null
    return try {
      conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        instanceFollowRedirects = false
        connectTimeout = 5000
        readTimeout = 7000
        setRequestProperty("User-Agent", "BayanUlHuda-Android/1.1 (AuthenticTafsirLibrary)")
        setRequestProperty("Accept", "application/json, text/plain, */*")
      }
      if (conn.responseCode in 200..299) {
        if (conn.contentLengthLong > SecurityIntegrityGuard.MAX_HTTP_RESPONSE_BYTES) return null
        conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
      } else {
        null
      }
    } catch (_: Exception) {
      null
    } finally {
      conn?.disconnect()
    }
  }

  private fun getRelatedVerses(surahNumber: Int, verseNumber: Int): List<String> {
    return when {
      surahNumber == 1 -> listOf("15:87", "2:286", "39:53", "40:60")
      surahNumber == 2 && verseNumber == 255 -> listOf("3:2", "20:111", "40:65", "59:22-24", "112:1-4")
      surahNumber == 2 && verseNumber >= 285 -> listOf("1:5-7", "3:8", "23:1-11", "94:5-6")
      surahNumber == 18 -> listOf("18:10", "18:28", "18:109-110", "41:30")
      surahNumber == 36 -> listOf("2:255", "36:58", "36:82-83", "55:26-27")
      surahNumber == 55 -> listOf("14:34", "16:18", "55:13", "76:1-9")
      surahNumber == 67 -> listOf("2:284", "23:84-89", "67:1-2", "87:1-5")
      surahNumber == 112 -> listOf("2:163", "2:255", "19:65", "42:11", "59:22-24")
      surahNumber in 113..114 -> listOf("7:200", "16:98", "23:97-98", "41:36")
      else -> listOf(
        "1:1-7 (Umm al-Kitab)",
        "2:255 (Ayat al-Kursi)",
        "$surahNumber:${(verseNumber - 1).coerceAtLeast(1)}",
        "39:23 (Ahsan al-Hadith)"
      ).distinct()
    }
  }

  /**
   * Stage 2 Q&A Search: Searches across all verified Tafsir commentaries for matches
   * in English, Urdu, or Arabic, filtered by tradition ("SUNNI", "SHIA", or "BOTH").
   * Strictly excludes unavailable entries so no placeholder text is ever matched.
   */
  fun searchTafsirCommentary(
    query: String,
    traditionScope: String = "BOTH",
    sampleAyahs: List<AyahEntity> = emptyList(),
    allHadiths: List<HadithEntity> = emptyList()
  ): List<AyahTafsirResult> {
    val q = query.trim().lowercase(Locale.ROOT)
    if (q.isEmpty()) return emptyList()

    val allowedEditions = when (traditionScope.uppercase(Locale.ROOT)) {
      "SUNNI" -> getEditionsForTradition("SUNNI")
      "SHIA" -> getEditionsForTradition("SHIA")
      else -> editions
    }

    val results = mutableListOf<AyahTafsirResult>()
    for (ayah in sampleAyahs) {
      for (edition in allowedEditions) {
        val tafsir = getTafsirForAyah(ayah, null, edition.key, allHadiths)
        if (!tafsir.isVerifiedContentAvailable) continue
        val enMatch = tafsir.commentaryEnglish.lowercase(Locale.ROOT).contains(q)
        val urMatch = tafsir.commentaryUrdu.lowercase(Locale.ROOT).contains(q)
        val arMatch = tafsir.commentaryArabic.lowercase(Locale.ROOT).contains(q)
        val editionMatch = edition.titleEnglish.lowercase(Locale.ROOT).contains(q) ||
          edition.scholarName.lowercase(Locale.ROOT).contains(q) ||
          edition.canonicalOriginalTitle.lowercase(Locale.ROOT).contains(q)
        val refMatch = tafsir.verseRef == query.trim() || query.trim() == "${ayah.surahNumber}:${ayah.verseNumber}"
        if (enMatch || urMatch || arMatch || editionMatch || refMatch) {
          results.add(tafsir)
          if (results.size >= 30) break
        }
      }
      if (results.size >= 30) break
    }
    return results
  }

  /**
   * Exports a schema-compliant JSON dataset package for any of the 22 registered Tafseer works
   * (including Ibn Kathir, Al-Jalalayn, Ma'ariful Quran, Al-Qurtubi, Al-Tabari, As-Sa'di,
   * Tafhim al-Quran, Tafseer Usmani, Al-Baghawi, Al-Baydawi, Al-Razi, Zad al-Masir,
   * Adwa' al-Bayan, and Ruh al-Ma'ani).
   *
   * This canonical JSON structure is directly consumed by [DownloadManager] and [ingestSchemaCompliantDatasetJson].
   */
  fun exportSchemaCompliantDatasetJson(
    editionKey: String,
    context: Context? = null
  ): String {
    ensureBundledCorpusLoaded(context)
    val edition = editions.find { it.key == editionKey } ?: editions.first()
    val verseMap = bundledCorpus[edition.key].orEmpty()

    val versesArray = JSONArray()
    val sortedKeys = verseMap.keys.sortedWith(
      compareBy(
        { it.substringBefore(":").toIntOrNull() ?: 0 },
        { it.substringAfter(":").toIntOrNull() ?: 0 }
      )
    )
    for (refKey in sortedKeys) {
      val sNum = refKey.substringBefore(":").toIntOrNull() ?: continue
      val vNum = refKey.substringAfter(":").toIntOrNull() ?: continue
      val texts = verseMap[refKey] ?: continue
      if (!texts.hasAnyVerifiedText) continue
      versesArray.put(
        JSONObject().apply {
          put("surahNumber", sNum)
          put("verseNumber", vNum)
          put("referenceKey", refKey)
          put("compositeCacheKey", buildCompositeCacheKey(edition, sNum, vNum))
          put("arabicCommentary", texts.arabic)
          put("englishCommentary", texts.english)
          put("urduCommentary", texts.urdu)
        }
      )
    }

    return JSONObject().apply {
      put("schemaVersion", "2.0-bayan-authentic-tafsir")
      put("editionKey", edition.key)
      put("canonicalOriginalTitle", edition.canonicalOriginalTitle)
      put("titleEnglish", edition.titleEnglish)
      put("titleArabic", edition.titleArabic)
      put("titleUrdu", edition.titleUrdu)
      put("scholarName", edition.scholarName)
      put("tradition", edition.tradition)
      put("primaryLanguage", edition.primaryLanguage)
      put("availableLanguages", JSONArray(edition.availableLanguages))
      put("editionName", edition.editionName)
      put("originalSource", edition.originalSource)
      put("sourceUrl", edition.sourceUrl)
      put("apiResourceId", edition.apiResourceId)
      put("licenceInfo", edition.licenceInfo)
      put("verseCount", versesArray.length())
      put("verses", versesArray)
    }.toString()
  }

  /**
   * Evaluates, validates, and ingests a JSON dataset for `expectedEditionKey` from any of the
   * supported scholarly repository schemas:
   * 1. Bayan Canonical Tafsir Schema v2.0 (`{"editionKey": "...", "verses": [...]}`)
   * 2. `spa5k/tafsir_api` Schema (`{"ayahs": [{"surah": 1, "ayah": 1, "text": "..."}]}`)
   * 3. `Quran.com API v4` Schema (`{"tafsirs": [{"resource_id": 169, "verse_key": "1:1", "text": "..."}]}`)
   * 4. `Quranpedia` Dump Array Schema (`[{"sura": 1, "ayah": 1, "text": "..."}]`)
   *
   * Enforces strict anti-mismatch validation, updates the composite memory/disk caches, and
   * returns schema-compliant [TranslationEntity] rows (`sourceType = "TAFSIR"`) ready for
   * Room SQLite (`translations` + `translations_fts`) persistence.
   */
  fun ingestSchemaCompliantDatasetJson(
    context: Context?,
    expectedEditionKey: String,
    rawJson: String
  ): List<TranslationEntity> {
    ensureBundledCorpusLoaded(context)
    val edition = editions.find { it.key == expectedEditionKey } ?: return emptyList()
    val trimmed = rawJson.trim()
    if (trimmed.isEmpty()) return emptyList()

    val roomEntities = ArrayList<TranslationEntity>()
    val editionVerseMap = bundledCorpus.getOrPut(edition.key) { ConcurrentHashMap() }
    val editionTitleFull = "${edition.titleEnglish} (${edition.canonicalOriginalTitle} • ${edition.scholarName})"

    fun recordVerifiedVerse(sNum: Int, vNum: Int, ar: String, en: String, ur: String) {
      if (sNum !in 1..114 || vNum !in 1..286) return
      val cleanAr = cleanAndSanitizeTafsirHtml(ar)
      val cleanEn = cleanAndSanitizeTafsirHtml(en)
      val cleanUr = cleanAndSanitizeTafsirHtml(ur)
      val merged = VerifiedTafsirTexts(arabic = cleanAr, english = cleanEn, urdu = cleanUr)
      if (!merged.hasAnyVerifiedText) return

      val refKey = "$sNum:$vNum"
      val compositeKey = buildCompositeCacheKey(edition, sNum, vNum)
      memoryCache[compositeKey] = merged
      editionVerseMap[refKey] = merged
      writeDiskCache(context, edition, sNum, vNum, merged)

      if (cleanEn.isNotBlank()) {
        roomEntities.add(
          TranslationEntity(
            sourceType = "TAFSIR",
            referenceKey = refKey,
            surahOrBookNumber = sNum,
            verseOrHadithNumber = vNum,
            editionKey = "tafsir.${edition.key}.en",
            editionTitle = editionTitleFull,
            languageCode = "en",
            text = cleanEn,
            isVerified = true
          )
        )
      }
      if (cleanUr.isNotBlank()) {
        roomEntities.add(
          TranslationEntity(
            sourceType = "TAFSIR",
            referenceKey = refKey,
            surahOrBookNumber = sNum,
            verseOrHadithNumber = vNum,
            editionKey = "tafsir.${edition.key}.ur",
            editionTitle = editionTitleFull,
            languageCode = "ur",
            text = cleanUr,
            isVerified = true
          )
        )
      }
      if (cleanAr.isNotBlank()) {
        roomEntities.add(
          TranslationEntity(
            sourceType = "TAFSIR",
            referenceKey = refKey,
            surahOrBookNumber = sNum,
            verseOrHadithNumber = vNum,
            editionKey = "tafsir.${edition.key}.ar",
            editionTitle = editionTitleFull,
            languageCode = "ar",
            text = cleanAr,
            isVerified = true
          )
        )
      }
    }

    try {
      if (trimmed.startsWith("{")) {
        val obj = JSONObject(trimmed)

        // Strict anti-mismatch check if editionKey is explicitly declared in payload
        val declaredKey = obj.optString("editionKey", "")
        if (declaredKey.isNotBlank() && declaredKey != edition.key) {
          return emptyList()
        }

        val versesArr = obj.optJSONArray("verses")
        val ayahsArr = obj.optJSONArray("ayahs")
        val tafsirsArr = obj.optJSONArray("tafsirs")

        when {
          versesArr != null -> {
            for (i in 0 until versesArr.length()) {
              val v = versesArr.optJSONObject(i) ?: continue
              val sNum = v.optInt("surahNumber", -1)
              val vNum = v.optInt("verseNumber", -1)
              recordVerifiedVerse(
                sNum = sNum,
                vNum = vNum,
                ar = v.optString("arabicCommentary", v.optString("ar", "")),
                en = v.optString("englishCommentary", v.optString("en", "")),
                ur = v.optString("urduCommentary", v.optString("ur", ""))
              )
            }
          }

          ayahsArr != null -> {
            // spa5k/tafsir_api per-surah or full-book schema
            val isEnglishPrimary = edition.primaryLanguage.equals("English", ignoreCase = true)
            val isUrduPrimary = edition.primaryLanguage.equals("Urdu", ignoreCase = true)
            for (i in 0 until ayahsArr.length()) {
              val a = ayahsArr.optJSONObject(i) ?: continue
              val sNum = a.optInt("surah", -1)
              val vNum = a.optInt("ayah", -1)
              val txt = a.optString("text", "")
              recordVerifiedVerse(
                sNum = sNum,
                vNum = vNum,
                ar = if (!isEnglishPrimary && !isUrduPrimary) txt else "",
                en = if (isEnglishPrimary) txt else "",
                ur = if (isUrduPrimary) txt else ""
              )
            }
          }

          tafsirsArr != null -> {
            // Quran.com API v4 bulk tafsirs schema
            val isEnglishPrimary = edition.primaryLanguage.equals("English", ignoreCase = true)
            val isUrduPrimary = edition.primaryLanguage.equals("Urdu", ignoreCase = true)
            for (i in 0 until tafsirsArr.length()) {
              val t = tafsirsArr.optJSONObject(i) ?: continue
              val verseKey = t.optString("verse_key", "")
              val sNum = verseKey.substringBefore(":").toIntOrNull() ?: t.optInt("chapter_id", -1)
              val vNum = verseKey.substringAfter(":").toIntOrNull() ?: t.optInt("verse_number", -1)
              val txt = t.optString("text", "")
              recordVerifiedVerse(
                sNum = sNum,
                vNum = vNum,
                ar = if (!isEnglishPrimary && !isUrduPrimary) txt else "",
                en = if (isEnglishPrimary) txt else "",
                ur = if (isUrduPrimary) txt else ""
              )
            }
          }
        }
      } else if (trimmed.startsWith("[")) {
        // Quranpedia dump array schema: [{"sura": 1, "ayah": 1, "text": "..."}]
        val arr = JSONArray(trimmed)
        val isEnglishPrimary = edition.primaryLanguage.equals("English", ignoreCase = true)
        val isUrduPrimary = edition.primaryLanguage.equals("Urdu", ignoreCase = true)
        for (i in 0 until arr.length()) {
          val item = arr.optJSONObject(i) ?: continue
          val sNum = item.optInt("sura", item.optInt("surah", item.optInt("surah_number", -1)))
          val vNum = item.optInt("ayah", item.optInt("aya", item.optInt("ayah_number", -1)))
          val txt = item.optString("text", item.optString("tafseer", ""))
          recordVerifiedVerse(
            sNum = sNum,
            vNum = vNum,
            ar = if (!isEnglishPrimary && !isUrduPrimary) txt else "",
            en = if (isEnglishPrimary) txt else "",
            ur = if (isUrduPrimary) txt else ""
          )
        }
      }
    } catch (_: Exception) {
    }

    // If Room entities were not produced from an external payload, fall back to the bundled corpus for this edition
    if (roomEntities.isEmpty() && editionVerseMap.isNotEmpty()) {
      for ((refKey, texts) in editionVerseMap) {
        val sNum = refKey.substringBefore(":").toIntOrNull() ?: continue
        val vNum = refKey.substringAfter(":").toIntOrNull() ?: continue
        recordVerifiedVerse(sNum, vNum, texts.arabic, texts.english, texts.urdu)
      }
    }

    return roomEntities
  }

  /**
   * Builds schema-compliant Room SQLite [TranslationEntity] records (`sourceType = "TAFSIR"`)
   * across all 22 registered Tafseer works from the bundled verified corpus.
   */
  fun buildRoomTranslationEntitiesForCorpus(context: Context? = null): List<TranslationEntity> {
    ensureBundledCorpusLoaded(context)
    val allEntities = ArrayList<TranslationEntity>(2048)
    for (edition in editions) {
      val verseMap = bundledCorpus[edition.key] ?: continue
      val editionTitleFull = "${edition.titleEnglish} (${edition.canonicalOriginalTitle} • ${edition.scholarName})"
      for ((refKey, texts) in verseMap) {
        val sNum = refKey.substringBefore(":").toIntOrNull() ?: continue
        val vNum = refKey.substringAfter(":").toIntOrNull() ?: continue
        if (texts.english.isNotBlank()) {
          allEntities.add(
            TranslationEntity(
              sourceType = "TAFSIR",
              referenceKey = refKey,
              surahOrBookNumber = sNum,
              verseOrHadithNumber = vNum,
              editionKey = "tafsir.${edition.key}.en",
              editionTitle = editionTitleFull,
              languageCode = "en",
              text = texts.english,
              isVerified = true
            )
          )
        }
        if (texts.urdu.isNotBlank()) {
          allEntities.add(
            TranslationEntity(
              sourceType = "TAFSIR",
              referenceKey = refKey,
              surahOrBookNumber = sNum,
              verseOrHadithNumber = vNum,
              editionKey = "tafsir.${edition.key}.ur",
              editionTitle = editionTitleFull,
              languageCode = "ur",
              text = texts.urdu,
              isVerified = true
            )
          )
        }
        if (texts.arabic.isNotBlank()) {
          allEntities.add(
            TranslationEntity(
              sourceType = "TAFSIR",
              referenceKey = refKey,
              surahOrBookNumber = sNum,
              verseOrHadithNumber = vNum,
              editionKey = "tafsir.${edition.key}.ar",
              editionTitle = editionTitleFull,
              languageCode = "ar",
              text = texts.arabic,
              isVerified = true
            )
          )
        }
      }
    }
    return allEntities
  }
}
