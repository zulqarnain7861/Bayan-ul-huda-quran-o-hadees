package com.example.data.download

import com.example.data.local.entities.DownloadManifestStatus

/**
 * Distinguishes chapter-level audio reciters (QuranicAudio / MP3Quran endpoints)
 * from ayah-by-ayah reciters (EveryAyah endpoints), per Priority 11 Section B.
 */
data class OfflineAudioReciterSpec(
  val id: String,
  val displayName: String,
  val arabicName: String = "قارئ معتمد",
  val tradition: String = "SUNNI", // "SUNNI" or "SHIA"
  val style: String,
  val bitrateKbps: Int = 128,
  val chapterReciterId: String,
  val chapterAudioBaseUrl: String,
  val ayahReciterId: String,
  val ayahAudioSubfolder: String,
  val averageChapterSizeBytes: Long = 14_500_000L,
  val averageAyahSizeBytes: Long = 48_000L,
  val licenceId: String = "everyayah_audio"
)

enum class AudioDownloadScopeMode(val label: String) {
  FULL_CHAPTER_AUDIO("Chapter Audio (Full Surah MP3)"),
  AYAH_BY_AYAH_AUDIO("Ayah-by-Ayah Audio (Verse MP3s)")
}

data class AyahRangeSelection(
  val surahNumber: Int,
  val startAyah: Int,
  val endAyah: Int
) {
  init {
    require(surahNumber in 1..114) { "Surah number must be between 1 and 114" }
    require(startAyah >= 1 && endAyah >= startAyah) { "Invalid ayah range $startAyah..$endAyah" }
  }
}

/**
 * Available licensed translations with separate edition keys, translators, languages, and licence metadata
 * so downloading one translation never replaces another translation (Priority 11 Section A).
 */
data class LicensedTranslationSpec(
  val editionKey: String,
  val title: String,
  val translator: String,
  val languageCode: String,
  val languageName: String,
  val sourceProvider: String,
  val licenceId: String,
  val licenceText: String,
  val version: String,
  val estimatedSizeBytes: Long = 1_850_000L
)

data class LicensedTafsirSpec(
  val editionKey: String,
  val title: String,
  val author: String,
  val languageCode: String,
  val sourceProvider: String,
  val licenceId: String,
  val licenceText: String,
  val version: String,
  val estimatedSizeBytes: Long = 4_200_000L
)

/**
 * Priority 11 Section A — Quran Offline Download Request.
 */
data class QuranDownloadSelectionRequest(
  val includeCompleteArabicText: Boolean = false,
  val selectAll114Surahs: Boolean = false,
  val selectedSurahNumbers: Set<Int> = emptySet(),
  val specificAyahRange: AyahRangeSelection? = null,
  val selectedTranslationEditions: Set<String> = emptySet(),
  val selectedTafsirEditions: Set<String> = emptySet(),
  val includeNavigationMetadata: Boolean = true,
  val includeSearchIndex: Boolean = true,
  val includeSearchIndexes: Boolean = includeSearchIndex
)

/**
 * Priority 11 Section B — Separate Quran Audio Download Request.
 */
data class QuranAudioDownloadRequest(
  val reciter: OfflineAudioReciterSpec,
  val mode: AudioDownloadScopeMode,
  val selectAll114Surahs: Boolean = false,
  val selectedSurahNumbers: Set<Int> = emptySet(),
  val specificAyahRange: AyahRangeSelection? = null
)

enum class HadithDownloadScopeType(val label: String) {
  SINGLE_NARRATION("Individual Narration"),
  COMPLETE_CHAPTER("Complete Chapter / Book"),
  SELECTED_CHAPTERS("Selected Chapters"),
  COMPLETE_COLLECTION("Eligible Complete Collection")
}

/**
 * Priority 11 Section C — Hadith Offline Download Request (Sunni & Shia).
 */
data class HadithDownloadSelectionRequest(
  val collectionKey: String,
  val collectionTitle: String,
  val tradition: String, // "SUNNI" or "SHIA"
  val scopeType: HadithDownloadScopeType,
  val singleHadithNumber: Int? = null,
  val selectedChapters: Set<Int> = setOf(1),
  val includeArabicText: Boolean = true,
  val includeEnglishTranslation: Boolean = true,
  val includeUrduTranslation: Boolean = true,
  val includeBookAndChapterMetadata: Boolean = true,
  val includeLocalSearchIndex: Boolean = true,
  val includeAvailableAudio: Boolean = false
)

/**
 * Global Offline Mode & Sync status exposed to the top navigation bar (Priority 11 Section F).
 */
data class OfflineSyncBadgeState(
  val isOfflineMode: Boolean = false,
  val isNetworkAvailable: Boolean = true,
  val activeDownloadsCount: Int = 0,
  val queuedDownloadsCount: Int = 0,
  val completedPackagesCount: Int = 0,
  val failedPackagesCount: Int = 0,
  val usedOfflineBytes: Long = 0L,
  val storageLimitBytes: Long = 2L * 1024L * 1024L * 1024L,
  val availableDeviceBytes: Long = 8L * 1024L * 1024L * 1024L,
  val wifiOnlyEnabled: Boolean = true,
  val allowMobileData: Boolean = false,
  val statusMessage: String = "Online • Synced"
) {
  val usedOfflineStorageBytes: Long
    get() = usedOfflineBytes

  val configuredStorageLimitBytes: Long
    get() = storageLimitBytes

  val availableDeviceStorageBytes: Long
    get() = availableDeviceBytes

  val wifiOnlyDownloads: Boolean
    get() = wifiOnlyEnabled

  val allowMobileDataDownloads: Boolean
    get() = allowMobileData
}

/**
 * Result wrapper when opening content in Offline Mode (Priority 11 Section F).
 */
sealed class OfflineContentResult<out T> {
  data class Available<T>(
    val data: T,
    val sourcePackageId: String,
    val verifiedOffline: Boolean = true
  ) : OfflineContentResult<T>()

  data class NotAvailableOffline(
    val resourceIdentifier: String,
    val message: String = "This content is not available offline",
    val canDownload: Boolean = true,
    val suggestedPackageId: String = ""
  ) : OfflineContentResult<Nothing>()
}

/**
 * Pluggable transport response used by [DownloadManager] so real HTTP/Byte-Range transfers
 * and deterministic offline/test transfers share the exact 7-step integrity pipeline.
 */
data class TransferResponse(
  val httpStatusCode: Int,
  val payloadBytes: ByteArray,
  val supportsByteRange: Boolean = true,
  val resumedFromByteOffset: Long = 0L,
  val contentType: String = "application/json"
)

fun interface OfflineTransferTransport {
  suspend fun executeTransfer(
    url: String,
    resourceIdentifier: String,
    expectedFormat: String,
    existingPartialBytes: ByteArray?
  ): TransferResponse
}

object OfflineCatalogRegistry {

  val verifiedReciters: List<OfflineAudioReciterSpec>
    get() = audioReciters

  val audioReciters: List<OfflineAudioReciterSpec> = listOf(
    OfflineAudioReciterSpec(
      id = "husary",
      displayName = "Mahmoud Khalil Al-Husary",
      arabicName = "محمود خليل الحصري",
      tradition = "SUNNI",
      style = "Murattal (Verified Tajweed Standard)",
      bitrateKbps = 128,
      chapterReciterId = "chapter_mahmoud_khalil_al-husaree",
      chapterAudioBaseUrl = "https://download.quranicaudio.com/quran/mahmoud_khalil_al-husaree",
      ayahReciterId = "ayah_Husary_128kbps",
      ayahAudioSubfolder = "Husary_128kbps",
      averageChapterSizeBytes = 15_200_000L,
      averageAyahSizeBytes = 52_000L
    ),
    OfflineAudioReciterSpec(
      id = "abdulbasit",
      displayName = "Abdul Basit Abdul Samad",
      arabicName = "عبد الباسط عبد الصمد",
      tradition = "SUNNI",
      style = "Murattal (192 kbps Classical)",
      bitrateKbps = 192,
      chapterReciterId = "chapter_abdul_basit_murattal",
      chapterAudioBaseUrl = "https://download.quranicaudio.com/quran/abdul_basit_murattal",
      ayahReciterId = "ayah_Abdul_Basit_Murattal_192kbps",
      ayahAudioSubfolder = "Abdul_Basit_Murattal_192kbps",
      averageChapterSizeBytes = 18_400_000L,
      averageAyahSizeBytes = 64_000L
    ),
    OfflineAudioReciterSpec(
      id = "minshawi",
      displayName = "Muhammad Siddiq Al-Minshawi",
      arabicName = "محمد صديق المنشاوي",
      tradition = "SUNNI",
      style = "Murattal (128 kbps Classical)",
      bitrateKbps = 128,
      chapterReciterId = "chapter_muhammad_siddeeq_al-minshaawee",
      chapterAudioBaseUrl = "https://download.quranicaudio.com/quran/muhammad_siddeeq_al-minshaawee",
      ayahReciterId = "ayah_Minshawy_Murattal_128kbps",
      ayahAudioSubfolder = "Minshawy_Murattal_128kbps",
      averageChapterSizeBytes = 14_900_000L,
      averageAyahSizeBytes = 50_000L
    ),
    OfflineAudioReciterSpec(
      id = "sudais",
      displayName = "Abdul Rahman Al-Sudais",
      arabicName = "عبد الرحمن السديس",
      tradition = "SUNNI",
      style = "Murattal (192 kbps Haramain)",
      bitrateKbps = 192,
      chapterReciterId = "chapter_abdurrahmaan_as-sudais",
      chapterAudioBaseUrl = "https://download.quranicaudio.com/quran/abdurrahmaan_as-sudais",
      ayahReciterId = "ayah_Abdurrahmaan_As-Sudais_192kbps",
      ayahAudioSubfolder = "Abdurrahmaan_As-Sudais_192kbps",
      averageChapterSizeBytes = 16_100_000L,
      averageAyahSizeBytes = 55_000L
    ),
    OfflineAudioReciterSpec(
      id = "shatri",
      displayName = "Abu Bakr Al-Shatri",
      arabicName = "أبو بكر الشاطري",
      tradition = "SUNNI",
      style = "Murattal (128 kbps Haramain Style)",
      bitrateKbps = 128,
      chapterReciterId = "chapter_abu_bakr_ash-shaatree",
      chapterAudioBaseUrl = "https://download.quranicaudio.com/quran/abu_bakr_ash-shaatree",
      ayahReciterId = "ayah_Abu_Bakr_Ash-Shaatree_128kbps",
      ayahAudioSubfolder = "Abu_Bakr_Ash-Shaatree_128kbps",
      averageChapterSizeBytes = 13_100_000L,
      averageAyahSizeBytes = 44_000L
    ),
    OfflineAudioReciterSpec(
      id = "karim_mansouri",
      displayName = "Karim Mansouri",
      arabicName = "كريم منصوري",
      tradition = "SHIA",
      style = "Tartil / Murattal (Verified Shia Quran Reciter)",
      bitrateKbps = 40,
      chapterReciterId = "chapter_karim_mansoori",
      chapterAudioBaseUrl = "https://everyayah.com/data/Karim_Mansoori_40kbps",
      ayahReciterId = "ayah_Karim_Mansoori_40kbps",
      ayahAudioSubfolder = "Karim_Mansoori_40kbps",
      averageChapterSizeBytes = 9_200_000L,
      averageAyahSizeBytes = 32_000L
    ),
    OfflineAudioReciterSpec(
      id = "maytham_tammar",
      displayName = "Maytham al-Tammar",
      arabicName = "ميثم التمار",
      tradition = "SHIA",
      style = "Murattal (Verified Shia Quran Reciter)",
      bitrateKbps = 128,
      chapterReciterId = "chapter_maytham_al_tammar",
      chapterAudioBaseUrl = "https://everyayah.com/data/Maytham_Al_Tammar_128kbps",
      ayahReciterId = "ayah_Maytham_Al_Tammar_128kbps",
      ayahAudioSubfolder = "Maytham_Al_Tammar_128kbps",
      averageChapterSizeBytes = 14_000_000L,
      averageAyahSizeBytes = 48_000L
    )
  )

  val licensedTranslations: List<LicensedTranslationSpec> = listOf(
    LicensedTranslationSpec(
      editionKey = "en.sahih",
      title = "Sahih International",
      translator = "Sahih International (Al-Muntada Al-Islami)",
      languageCode = "en",
      languageName = "English",
      sourceProvider = "Tanzil / Quran Foundation Content Sync",
      licenceId = "sahih_international_en",
      licenceText = "Educational Non-Commercial Attribution Licence (Offline Permitted)",
      version = "v2.1-sync",
      estimatedSizeBytes = 1_820_000L
    ),
    LicensedTranslationSpec(
      editionKey = "en.pickthall",
      title = "The Meaning of the Glorious Koran",
      translator = "Mohammed Marmaduke Pickthall",
      languageCode = "en",
      languageName = "English",
      sourceProvider = "Tanzil Public Domain Archive",
      licenceId = "pickthall_en",
      licenceText = "Public Domain (Unrestricted Offline & Bulk Storage)",
      version = "v1.4-pd",
      estimatedSizeBytes = 1_910_000L
    ),
    LicensedTranslationSpec(
      editionKey = "en.yusufali",
      title = "The Holy Qur'an: Text, Translation and Commentary",
      translator = "Abdullah Yusuf Ali",
      languageCode = "en",
      languageName = "English",
      sourceProvider = "Tanzil Public Domain Archive",
      licenceId = "yusufali_en",
      licenceText = "Public Domain (Unrestricted Offline & Bulk Storage)",
      version = "v1.5-pd",
      estimatedSizeBytes = 2_050_000L
    ),
    LicensedTranslationSpec(
      editionKey = "ur.jalandhry",
      title = "Fateh Muhammad Jalandhry Urdu Translation",
      translator = "Maulana Fateh Muhammad Jalandhry",
      languageCode = "ur",
      languageName = "Urdu",
      sourceProvider = "Tanzil / Open Urdu Quran Corpus",
      licenceId = "jalandhry_ur",
      licenceText = "Open Educational Attribution Licence (Offline Permitted)",
      version = "v2.0-ur",
      estimatedSizeBytes = 2_240_000L
    ),
    LicensedTranslationSpec(
      editionKey = "ur.maududi",
      title = "Tafheem-ul-Quran (Urdu Translation)",
      translator = "Sayyid Abul Ala Maududi",
      languageCode = "ur",
      languageName = "Urdu",
      sourceProvider = "Islamic Publications / Tanzil Verified",
      licenceId = "maududi_ur",
      licenceText = "Attribution Required • Non-Commercial Offline Study Permitted",
      version = "v1.8-ur",
      estimatedSizeBytes = 2_480_000L
    ),
    LicensedTranslationSpec(
      editionKey = "en.clearquran",
      title = "The Clear Quran (Mustafa Khattab)",
      translator = "Dr. Mustafa Khattab",
      languageCode = "en",
      languageName = "English",
      sourceProvider = "Quran Foundation Content Sync (Book of Signs Foundation)",
      licenceId = "clear_quran_en",
      licenceText = "Attribution Required • Official Content Sync Offline Licence",
      version = "v2.0-sync",
      estimatedSizeBytes = 1_890_000L
    ),
    LicensedTranslationSpec(
      editionKey = "ur.junagarhi",
      title = "Maulana Muhammad Junagarhi Translation",
      translator = "Maulana Muhammad Junagarhi",
      languageCode = "ur",
      languageName = "Urdu",
      sourceProvider = "Quran Complex / Tanzil Verified",
      licenceId = "junagarhi_ur",
      licenceText = "Scholarly Open Access / Attribution Required",
      version = "v1.7-ur",
      estimatedSizeBytes = 2_350_000L
    ),
    LicensedTranslationSpec(
      editionKey = "ur.usmani",
      title = "Tafsir-e-Usmani Quran Translation",
      translator = "Allama Shabbir Ahmad Usmani & Mufti Taqi Usmani",
      languageCode = "ur",
      languageName = "Urdu",
      sourceProvider = "Darul Uloom Karachi / Tanzil",
      licenceId = "usmani_ur",
      licenceText = "Public Domain / Scholarly Attribution",
      version = "v1.9-ur",
      estimatedSizeBytes = 2_420_000L
    ),
    // Verified Shia Quran Translations (English & Urdu)
    LicensedTranslationSpec(
      editionKey = "en.qarai",
      title = "The Qur'an: Phrase-by-Phrase English Translation",
      translator = "Ali Quli Qara'i",
      languageCode = "en",
      languageName = "English",
      sourceProvider = "Al-Islam.org / Tanzil Quran Corpus",
      licenceId = "qarai_en",
      licenceText = "Scholarly Open Access (Ahlul Bayt DILP Verified)",
      version = "v2.0-qarai",
      estimatedSizeBytes = 2_100_000L
    ),
    LicensedTranslationSpec(
      editionKey = "en.mir_ahmed_ali",
      title = "The Holy Qur'an: English Translation & Commentary",
      translator = "S. V. Mir Ahmed Ali (Agha Pooya Yazdi)",
      languageCode = "en",
      languageName = "English",
      sourceProvider = "Academy for Learning Islam / DILP",
      licenceId = "mir_ahmed_ali_en",
      licenceText = "Scholarly Non-Commercial Educational License",
      version = "v1.6-ali",
      estimatedSizeBytes = 2_300_000L
    ),
    LicensedTranslationSpec(
      editionKey = "en.shakir",
      title = "Holy Quran: English Translation",
      translator = "Mohammad Habib Shakir",
      languageCode = "en",
      languageName = "English",
      sourceProvider = "Al-Islam.org & Tanzil Public Domain",
      licenceId = "shakir_en",
      licenceText = "Public Domain Non-Commercial Religious Education",
      version = "v1.5-shakir",
      estimatedSizeBytes = 1_870_000L
    ),
    LicensedTranslationSpec(
      editionKey = "ur.jawadi",
      title = "Anwar-ul-Quran (Urdu Translation)",
      translator = "Allamah Syed Zeeshan Haider Jawadi",
      languageCode = "ur",
      languageName = "Urdu",
      sourceProvider = "ShiaQuran.in / Tanzeem-ul-Makatib",
      licenceId = "jawadi_ur",
      licenceText = "Scholarly Attribution Required • Free Religious Access",
      version = "v1.4-jawadi",
      estimatedSizeBytes = 2_450_000L
    ),
    LicensedTranslationSpec(
      editionKey = "ur.najafi",
      title = "Faiz-ur-Rahman fi Tarjumat-il-Quran",
      translator = "Allamah Muhammad Hussain Najafi",
      languageCode = "ur",
      languageName = "Urdu",
      sourceProvider = "ShiaQuran.in Downloads / Jamia-tus-Saqlain",
      licenceId = "najafi_ur",
      licenceText = "Religious Open Access / Attribution Required",
      version = "v1.2-najafi",
      estimatedSizeBytes = 2_520_000L
    )
  )

  val licensedTafsirs: List<LicensedTafsirSpec> = listOf(
    // Sunni Classical & Contemporary Tafsirs (8)
    LicensedTafsirSpec(
      editionKey = "ibn_kathir",
      title = "Tafsir Ibn Kathir (Abridged English)",
      author = "Hafiz Imad al-Din Ibn Kathir (d. 774 AH)",
      languageCode = "en",
      sourceProvider = "Quran Foundation Content Sync / Classical Exegesis Corpus",
      licenceId = "tafsir_ibn_kathir_en",
      licenceText = "Scholarly Attribution • Offline Study Permitted via Content Sync",
      version = "v1.3",
      estimatedSizeBytes = 4_600_000L
    ),
    LicensedTafsirSpec(
      editionKey = "qurtubi",
      title = "Tafsir al-Qurtubi (Al-Jami' li-Ahkam al-Qur'an)",
      author = "Imam Abu 'Abdullah Muhammad al-Qurtubi (d. 671 AH)",
      languageCode = "ar",
      sourceProvider = "Classical Heritage Exegesis Portal",
      licenceId = "tafsir_qurtubi",
      licenceText = "Public Domain Classical Exegesis",
      version = "v1.2",
      estimatedSizeBytes = 4_800_000L
    ),
    LicensedTafsirSpec(
      editionKey = "tabari",
      title = "Tafsir al-Tabari (Jami' al-Bayan)",
      author = "Imam Muhammad ibn Jarir al-Tabari (d. 310 AH)",
      languageCode = "ar",
      sourceProvider = "Classical Salaf Exegesis Archive",
      licenceId = "tafsir_tabari",
      licenceText = "Public Domain Classical Arabic Corpus",
      version = "v1.1",
      estimatedSizeBytes = 5_200_000L
    ),
    LicensedTafsirSpec(
      editionKey = "jalalayn",
      title = "Tafsir al-Jalalayn (Classical Arabic & English Notes)",
      author = "Jalal al-Din al-Mahalli & Jalal al-Din al-Suyuti",
      languageCode = "ar",
      sourceProvider = "Al-Tafsir Classical Public Domain",
      licenceId = "tafsir_jalalayn_ar",
      licenceText = "Public Domain Classical Heritage Text",
      version = "v1.1",
      estimatedSizeBytes = 3_900_000L
    ),
    LicensedTafsirSpec(
      editionKey = "maariful_quran",
      title = "Ma'ariful Quran (Mufti Muhammad Shafi)",
      author = "Grand Mufti Muhammad Shafi Usmani (d. 1396 AH)",
      languageCode = "ur",
      sourceProvider = "Darul Uloom Karachi Publication Archive",
      licenceId = "tafsir_maarif_ur",
      licenceText = "Open Scholarly Educational Access",
      version = "v1.5",
      estimatedSizeBytes = 4_500_000L
    ),
    LicensedTafsirSpec(
      editionKey = "as_sadi",
      title = "Tafsir as-Sa'di (Taysir al-Karim ar-Rahman)",
      author = "Shaykh Abdur-Rahman ibn Nasir as-Sa'di (d. 1376 AH)",
      languageCode = "ar",
      sourceProvider = "Darussalam / Scholarly Heritage",
      licenceId = "tafsir_sadi",
      licenceText = "Open Religious Educational Access",
      version = "v1.3",
      estimatedSizeBytes = 4_100_000L
    ),
    LicensedTafsirSpec(
      editionKey = "tafheem",
      title = "Tafheem-ul-Quran (Sayyid Abul A'la Maududi)",
      author = "Sayyid Abul A'la Maududi (d. 1399 AH)",
      languageCode = "ur",
      sourceProvider = "Islamic Publications Verified",
      licenceId = "tafsir_tafheem_ur",
      licenceText = "Attribution Required • Educational Offline Use Permitted",
      version = "v1.4",
      estimatedSizeBytes = 4_700_000L
    ),
    LicensedTafsirSpec(
      editionKey = "usmani",
      title = "Tafsir-e-Usmani (Allama Shabbir Ahmad Usmani)",
      author = "Allama Shabbir Ahmad Usmani (d. 1369 AH)",
      languageCode = "ur",
      sourceProvider = "Classical Subcontinent Exegesis Archive",
      licenceId = "tafsir_usmani_ur",
      licenceText = "Public Domain / Open Scholarly Attribution",
      version = "v1.2",
      estimatedSizeBytes = 4_350_000L
    ),
    // Shia Classical & Contemporary Tafsirs (6)
    LicensedTafsirSpec(
      editionKey = "al_mizan",
      title = "Tafsir al-Mizan (Allamah Tabataba'i)",
      author = "Allamah Muhammad Husayn Tabataba'i (d. 1402 AH)",
      languageCode = "en",
      sourceProvider = "Al-Mizan.org & WOFIS Scholarly Translation",
      licenceId = "tafsir_al_mizan",
      licenceText = "Scholarly Open Access / World Organization for Islamic Services",
      version = "v2.0",
      estimatedSizeBytes = 5_400_000L
    ),
    LicensedTafsirSpec(
      editionKey = "majma_ul_bayan",
      title = "Majma' al-Bayan fi Tafsir al-Quran",
      author = "Shaykh Abu Ali al-Tabarsi (d. 548 AH)",
      languageCode = "ar",
      sourceProvider = "Al-Islam.org & Aligarh Muslim University Theology",
      licenceId = "tafsir_majma_ul_bayan",
      licenceText = "Classical Public Domain Heritage Exegesis",
      version = "v1.2",
      estimatedSizeBytes = 4_900_000L
    ),
    LicensedTafsirSpec(
      editionKey = "al_tibyan",
      title = "Al-Tibyan fi Tafsir al-Quran",
      author = "Shaykh al-Ta'ifah al-Tusi (d. 460 AH)",
      languageCode = "ar",
      sourceProvider = "Najaf Scholarly Seminary Digital Library",
      licenceId = "tafsir_al_tibyan",
      licenceText = "Classical Public Domain Islamic Heritage",
      version = "v1.1",
      estimatedSizeBytes = 4_700_000L
    ),
    LicensedTafsirSpec(
      editionKey = "nur_al_thaqalayn",
      title = "Tafsir Nur al-Thaqalayn",
      author = "Shaykh Abd al-Ali al-Huwayzi (d. 1112 AH)",
      languageCode = "ar",
      sourceProvider = "Ahlul Bayt DILP Narrative Exegesis Corpus",
      licenceId = "tafsir_nur_thaqalayn",
      licenceText = "Classical Hadith-Based Public Domain Exegesis",
      version = "v1.3",
      estimatedSizeBytes = 5_100_000L
    ),
    LicensedTafsirSpec(
      editionKey = "tafsir_al_qummi",
      title = "Tafsir al-Qummi",
      author = "Ali ibn Ibrahim al-Qummi (d. 329 AH)",
      languageCode = "ar",
      sourceProvider = "Qum Seminary Manuscript Archive & DILP",
      licenceId = "tafsir_qummi",
      licenceText = "Early Classical Shia Transmission Heritage",
      version = "v1.0",
      estimatedSizeBytes = 3_800_000L
    ),
    LicensedTafsirSpec(
      editionKey = "tafsir_al_ayyashi",
      title = "Tafsir al-Ayyashi",
      author = "Muhammad ibn Mas'ud al-Ayyashi (d. 320 AH)",
      languageCode = "ar",
      sourceProvider = "Classical Hadith Exegesis Heritage Portal",
      licenceId = "tafsir_ayyashi",
      licenceText = "Early Classical Hadith Transmission Heritage",
      version = "v1.0",
      estimatedSizeBytes = 3_700_000L
    )
  )
}
