package com.example.data.remote

import com.example.BuildConfig

/**
 * Multi-Source Hadith Catalog & Endpoint Builder for the 18+ Sunni Hadith Books.
 *
 * Unifies four verified, legally authorized Hadith data sources:
 * 1. Fawaz Ahmed Hadith API (`fawazahmed0/hadith-api@1` via jsDelivr CDN):
 *    - `/editions`: https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions.json & editions.min.json
 *    - `/editions/{editionName}`: https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/{editionName}.json & .min.json
 *    - `/editions/{editionName}/{HadithNo}`: https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/{editionName}/{HadithNo}.json & .min.json
 *    - `/editions/{editionName}/sections/{sectionNo}`: https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/{editionName}/sections/{sectionNo}.json & .min.json
 *    - `/info`: https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/info.json
 *
 * 2. AhmedBaset Hadith-JSON (`AhmedBaset/hadith-json` strictly pinned to tag `v1.2.0`):
 *    - By chapter: https://raw.githubusercontent.com/AhmedBaset/hadith-json/v1.2.0/db/by_chapter/{category}/{bookSlug}/{chapterNo}.json
 *    - By book: https://raw.githubusercontent.com/AhmedBaset/hadith-json/v1.2.0/db/by_book/{category}/{bookSlug}.json
 *
 * 3. HadithAPI.com (`https://hadithapi.com`):
 *    - `/api/books?apiKey={apiKey}`
 *    - `/api/{bookSlug}/chapters?apiKey={apiKey}`
 *    - `/public/api/hadiths?apiKey={apiKey}&book={bookSlug}&chapter={chapterNo}&paginate=100`
 *    - `/public/api/hadiths?apiKey={apiKey}&book={bookSlug}&hadithNumber={hadithNo}`
 *
 * 4. Sunnah.com Official API (`https://api.sunnah.com/v1`, `github.com/sunnah-com/api.git`):
 *    - `/v1/collections`
 *    - `/v1/collections/{collectionName}/books`
 *    - `/v1/collections/{collectionName}/books/{bookNumber}/hadiths`
 *    - `/v1/collections/{collectionName}/hadiths/{hadithNumber}`
 *
 * All editions share the same canonical Arabic text per Hadith while allowing users to switch
 * between multiple named translations and verified scholarly sources.
 */
object MultiSourceHadithCatalog {

    const val FAWAZ_CDN_BASE = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1"
    const val AHMED_BASET_TAG = "v1.2.0"
    const val AHMED_BASET_BASE = "https://raw.githubusercontent.com/AhmedBaset/hadith-json/$AHMED_BASET_TAG/db"
    const val HADITH_API_BASE = "https://hadithapi.com"
    const val SUNNAH_API_BASE = "https://api.sunnah.com/v1"
    const val UMMAH_API_BASE = "https://ummahapi.com"

    enum class HadithSourceProvider(
        val id: String,
        val displayName: String,
        val shortLabel: String
    ) {
        FAWAZ_AHMED(
            id = "fawazahmed0_v1",
            displayName = "FawazAhmed0 Hadith API (@1 CDN)",
            shortLabel = "FawazAhmed0 @1"
        ),
        AHMED_BASET_V1_2_0(
            id = "ahmedbaset_v1_2_0",
            displayName = "AhmedBaset Hadith-JSON (Tag v1.2.0)",
            shortLabel = "AhmedBaset v1.2.0"
        ),
        HADITH_API_COM(
            id = "hadithapi_com",
            displayName = "HadithAPI.com Verified Corpus",
            shortLabel = "HadithAPI.com"
        ),
        SUNNAH_COM_API(
            id = "sunnah_com_v1",
            displayName = "Sunnah.com Official API (v1)",
            shortLabel = "Sunnah.com API"
        ),
        UMMAH_API(
            id = "ummahapi_com",
            displayName = "UmmahAPI.com Verified Hadith Corpus",
            shortLabel = "UmmahAPI.com"
        ),
        BUNDLED_OFFLINE_CORPUS(
            id = "bundled_offline",
            displayName = "Bundled Verified Offline Corpus",
            shortLabel = "Offline Corpus"
        )
    }

    data class AhmedBasetBookCoordinate(
        val collectionKey: String,
        val category: String,
        val bookSlug: String,
        val arabicTitle: String,
        val englishTitle: String,
        val totalChaptersEstimate: Int
    ) {
        fun byBookUrl(): String = "$AHMED_BASET_BASE/by_book/$category/$bookSlug.json"
        fun byChapterUrl(chapterNo: Int): String = "$AHMED_BASET_BASE/by_chapter/$category/$bookSlug/$chapterNo.json"
    }

    data class NamedHadithTranslationEdition(
        val editionKey: String,
        val collectionKey: String,
        val languageCode: String,
        val languageLabel: String,
        val editionTitle: String,
        val translatorOrSourceName: String,
        val provider: HadithSourceProvider,
        val fawazEditionName: String? = null,
        val ahmedBasetCoordinate: AhmedBasetBookCoordinate? = null,
        val hadithApiBookSlug: String? = null,
        val sunnahCollectionSlug: String? = null,
        val ummahApiCollectionSlug: String? = null
    )

    data class SunniBookSourceMapping(
        val collectionKey: String,
        val canonicalTitle: String,
        val arabicTitle: String,
        val fawazSlug: String?,
        val ahmedBaset: AhmedBasetBookCoordinate?,
        val hadithApiSlug: String?,
        val sunnahComSlug: String?,
        val hasBundledOfflineArchive: Boolean,
        val ummahApiSlug: String? = null
    )

    /**
     * Complete 17-book coordinate mapping for AhmedBaset/hadith-json pinned strictly to tag v1.2.0.
     */
    val ahmedBasetMappings: Map<String, AhmedBasetBookCoordinate> = mapOf(
        // the_9_books (9 books)
        "bukhari" to AhmedBasetBookCoordinate("bukhari", "the_9_books", "bukhari", "صحيح البخاري", "Sahih al-Bukhari", 97),
        "muslim" to AhmedBasetBookCoordinate("muslim", "the_9_books", "muslim", "صحيح مسلم", "Sahih Muslim", 56),
        "abudawud" to AhmedBasetBookCoordinate("abudawud", "the_9_books", "abudawud", "سنن أبي داود", "Sunan Abu Dawud", 43),
        "tirmidhi" to AhmedBasetBookCoordinate("tirmidhi", "the_9_books", "tirmidhi", "جامع الترمذي", "Jami' at-Tirmidhi", 49),
        "nasai" to AhmedBasetBookCoordinate("nasai", "the_9_books", "nasai", "سنن النسائي", "Sunan an-Nasa'i", 51),
        "ibnmajah" to AhmedBasetBookCoordinate("ibnmajah", "the_9_books", "ibnmajah", "سنن ابن ماجه", "Sunan Ibn Majah", 37),
        "malik" to AhmedBasetBookCoordinate("malik", "the_9_books", "malik", "موطأ مالك", "Muwatta Imam Malik", 61),
        "ahmad" to AhmedBasetBookCoordinate("ahmad", "the_9_books", "ahmed", "مسند الإمام أحمد بن حنبل", "Musnad Ahmad ibn Hanbal", 50),
        "darimi" to AhmedBasetBookCoordinate("darimi", "the_9_books", "darimi", "سنن الدارمي", "Sunan ad-Darimi", 24),
        // forties (3 books)
        "nawawi" to AhmedBasetBookCoordinate("nawawi", "forties", "nawawi40", "الأربعون النووية", "Forty Hadith of an-Nawawi", 1),
        "qudsi" to AhmedBasetBookCoordinate("qudsi", "forties", "qudsi40", "الأحاديث القدسية الأربعون", "Forty Hadith Qudsi", 1),
        "dehlawi" to AhmedBasetBookCoordinate("dehlawi", "forties", "shahwaliullah40", "أربعون شاه ولي الله الدهلوي", "Forty Hadith of Shah Waliullah Dehlawi", 1),
        // other_books (5 books)
        "riyadussalihin" to AhmedBasetBookCoordinate("riyadussalihin", "other_books", "riyad_assalihin", "رياض الصالحين", "Riyad as-Salihin", 19),
        "mishkat" to AhmedBasetBookCoordinate("mishkat", "other_books", "mishkat_almasabih", "مشكاة المصابيح", "Mishkat al-Masabih", 29),
        "adab" to AhmedBasetBookCoordinate("adab", "other_books", "aladab_almufrad", "الأدب المفرد", "Al-Adab al-Mufrad", 57),
        "shamail" to AhmedBasetBookCoordinate("shamail", "other_books", "shamail_muhammadiyah", "الشمائل المحمدية", "Ash-Shama'il al-Muhammadiyah", 56),
        "bulugh" to AhmedBasetBookCoordinate("bulugh", "other_books", "bulugh_almaram", "بلوغ المرام", "Bulugh al-Maram", 16)
    )

    /**
     * FawazAhmed0 Hadith API (@1) collection slugs.
     */
    val fawazSlugs: Map<String, String> = mapOf(
        "bukhari" to "bukhari",
        "muslim" to "muslim",
        "abudawud" to "abudawud",
        "tirmidhi" to "tirmidhi",
        "nasai" to "nasai",
        "ibnmajah" to "ibnmajah",
        "malik" to "malik",
        "nawawi" to "nawawi",
        "qudsi" to "qudsi",
        "dehlawi" to "dehlawi"
    )

    /**
     * HadithAPI.com book slugs (https://hadithapi.com/api/books).
     */
    val hadithApiSlugs: Map<String, String> = mapOf(
        "bukhari" to "sahih-bukhari",
        "muslim" to "sahih-muslim",
        "tirmidhi" to "al-tirmidhi",
        "abudawud" to "abu-dawood",
        "ibnmajah" to "ibn-e-majah",
        "nasai" to "sunan-nasai",
        "mishkat" to "mishkat",
        "ahmad" to "musnad-ahmad"
    )

    /**
     * Sunnah.com API v1 collection names (https://github.com/sunnah-com/api.git).
     */
    val sunnahComSlugs: Map<String, String> = mapOf(
        "bukhari" to "bukhari",
        "muslim" to "muslim",
        "nasai" to "nasai",
        "abudawud" to "abudawud",
        "tirmidhi" to "tirmidhi",
        "ibnmajah" to "ibnmajah",
        "malik" to "malik",
        "ahmad" to "ahmad",
        "darimi" to "darimi",
        "riyadussalihin" to "riyadussalihin",
        "adab" to "adab",
        "shamail" to "shamail",
        "mishkat" to "mishkat",
        "bulugh" to "bulugh",
        "hisn" to "hisn",
        "nawawi" to "nawawi40",
        "qudsi" to "qudsi40",
        "dehlawi" to "shahwaliullah40"
    )

    /**
     * UmmahAPI.com collection slugs (https://ummahapi.com/api/hadith/collections).
     */
    val ummahApiSlugs: Map<String, String> = mapOf(
        "bukhari" to "bukhari",
        "muslim" to "muslim",
        "abudawud" to "abudawud",
        "tirmidhi" to "tirmidhi",
        "nasai" to "nasai",
        "ibnmajah" to "ibnmajah",
        "malik" to "malik",
        "nawawi" to "nawawi",
        "qudsi" to "qudsi",
        "dehlawi" to "dehlawi",
        "riyadussalihin" to "riyadussalihin",
        "ahmad" to "ahmad",
        "darimi" to "darimi",
        "adab" to "adab",
        "bulugh" to "bulugh",
        "mishkat" to "mishkat",
        "shamail" to "shamail",
        "hisn" to "hisn"
    )

    /**
     * Complete 18 Sunni Hadith books (plus classical musannaf/sahih corpora) source mapping.
     */
    val sunniBookMappings: Map<String, SunniBookSourceMapping> = listOf(
        SunniBookSourceMapping("bukhari", "Sahih al-Bukhari", "صحيح البخاري", "bukhari", ahmedBasetMappings["bukhari"], "sahih-bukhari", "bukhari", false, "bukhari"),
        SunniBookSourceMapping("muslim", "Sahih Muslim", "صحيح مسلم", "muslim", ahmedBasetMappings["muslim"], "sahih-muslim", "muslim", false, "muslim"),
        SunniBookSourceMapping("abudawud", "Sunan Abu Dawud", "سنن أبي داود", "abudawud", ahmedBasetMappings["abudawud"], "abu-dawood", "abudawud", false, "abudawud"),
        SunniBookSourceMapping("tirmidhi", "Jami' at-Tirmidhi", "جامع الترمذي", "tirmidhi", ahmedBasetMappings["tirmidhi"], "al-tirmidhi", "tirmidhi", false, "tirmidhi"),
        SunniBookSourceMapping("nasai", "Sunan an-Nasa'i", "سنن النسائي", "nasai", ahmedBasetMappings["nasai"], "sunan-nasai", "nasai", false, "nasai"),
        SunniBookSourceMapping("ibnmajah", "Sunan Ibn Majah", "سنن ابن ماجه", "ibnmajah", ahmedBasetMappings["ibnmajah"], "ibn-e-majah", "ibnmajah", false, "ibnmajah"),
        SunniBookSourceMapping("malik", "Muwatta Imam Malik", "موطأ الإمام مالك", "malik", ahmedBasetMappings["malik"], null, "malik", false, "malik"),
        SunniBookSourceMapping("ahmad", "Musnad Ahmad ibn Hanbal", "مسند الإمام أحمد بن حنبل", null, ahmedBasetMappings["ahmad"], "musnad-ahmad", "ahmad", true, "ahmad"),
        SunniBookSourceMapping("darimi", "Sunan ad-Darimi", "سنن الدارمي", null, ahmedBasetMappings["darimi"], null, "darimi", true, "darimi"),
        SunniBookSourceMapping("riyadussalihin", "Riyad as-Salihin", "رياض الصالحين", null, ahmedBasetMappings["riyadussalihin"], null, "riyadussalihin", true, "riyadussalihin"),
        SunniBookSourceMapping("adab", "Al-Adab al-Mufrad", "الأدب المفرد", null, ahmedBasetMappings["adab"], null, "adab", true, "adab"),
        SunniBookSourceMapping("bulugh", "Bulugh al-Maram", "بلوغ المرام", null, ahmedBasetMappings["bulugh"], null, "bulugh", true, "bulugh"),
        SunniBookSourceMapping("mishkat", "Mishkat al-Masabih", "مشكاة المصابيح", null, ahmedBasetMappings["mishkat"], "mishkat", "mishkat", true, "mishkat"),
        SunniBookSourceMapping("shamail", "Ash-Shama'il al-Muhammadiyah", "الشمائل المحمدية", null, ahmedBasetMappings["shamail"], null, "shamail", true, "shamail"),
        SunniBookSourceMapping("nawawi", "Al-Arba'in al-Nawawiyyah (40 Hadith Nawawi)", "الأربعون النووية", "nawawi", ahmedBasetMappings["nawawi"], null, "nawawi40", true, "nawawi"),
        SunniBookSourceMapping("qudsi", "Forty Hadith Qudsi", "الأحاديث القدسية الأربعون", "qudsi", ahmedBasetMappings["qudsi"], null, "qudsi40", true, "qudsi"),
        SunniBookSourceMapping("dehlawi", "Forty Hadith of Shah Waliullah Dehlawi", "أربعون شاه ولي الله الدهلوي", "dehlawi", ahmedBasetMappings["dehlawi"], null, "shahwaliullah40", false, "dehlawi"),
        SunniBookSourceMapping("hisn", "Hisn al-Muslim (Fortress of the Muslim)", "حصن المسلم", null, null, null, "hisn", true, "hisn"),
        SunniBookSourceMapping("ibnkhuzaymah", "Sahih Ibn Khuzaymah", "صحيح ابن خزيمة", null, null, null, null, true),
        SunniBookSourceMapping("ibnhibban", "Sahih Ibn Hibban", "صحيح ابن حبان", null, null, null, null, true),
        SunniBookSourceMapping("mustadrak", "Al-Mustadrak ala al-Sahihayn", "المستدرك على الصحيحين", null, null, null, null, true),
        SunniBookSourceMapping("abdurrazzaq", "Musannaf Abd al-Razzaq", "مصنف عبد الرزاق", null, null, null, null, true),
        SunniBookSourceMapping("ibnabishaybah", "Musannaf Ibn Abi Shaybah", "مصنف ابن أبي شيبة", null, null, null, null, true),
        SunniBookSourceMapping("daraqutni", "Sunan al-Daraqutni", "سنن الدارقطني", null, null, null, null, true)
    ).associateBy { it.collectionKey }

    // =========================================================================
    // 1. FAWAZ AHMED HADITH API (@1) ENDPOINT BUILDERS
    // =========================================================================

    fun fawazEditionsUrl(minified: Boolean = false): String =
        if (minified) "$FAWAZ_CDN_BASE/editions.min.json" else "$FAWAZ_CDN_BASE/editions.json"

    fun fawazInfoUrl(): String = "$FAWAZ_CDN_BASE/info.json"

    fun fawazWholeEditionUrl(editionName: String, minified: Boolean = false): String =
        if (minified) "$FAWAZ_CDN_BASE/editions/$editionName.min.json"
        else "$FAWAZ_CDN_BASE/editions/$editionName.json"

    fun fawazHadithByNumberUrl(editionName: String, hadithNo: Int, minified: Boolean = false): String =
        if (minified) "$FAWAZ_CDN_BASE/editions/$editionName/$hadithNo.min.json"
        else "$FAWAZ_CDN_BASE/editions/$editionName/$hadithNo.json"

    fun fawazSectionUrl(editionName: String, sectionNo: Int, minified: Boolean = false): String =
        if (minified) "$FAWAZ_CDN_BASE/editions/$editionName/sections/$sectionNo.min.json"
        else "$FAWAZ_CDN_BASE/editions/$editionName/sections/$sectionNo.json"

    // =========================================================================
    // 2. AHMED BASET HADITH-JSON (PINNED STRICTLY TO TAG v1.2.0) ENDPOINT BUILDERS
    // =========================================================================

    fun ahmedBasetByChapterUrl(category: String, bookSlug: String, chapterNo: Int): String =
        "$AHMED_BASET_BASE/by_chapter/$category/$bookSlug/$chapterNo.json"

    fun ahmedBasetByBookUrl(category: String, bookSlug: String): String =
        "$AHMED_BASET_BASE/by_book/$category/$bookSlug.json"

    fun ahmedBasetByChapterForCollection(collectionKey: String, chapterNo: Int): String? =
        ahmedBasetMappings[collectionKey]?.byChapterUrl(chapterNo)

    fun ahmedBasetByBookForCollection(collectionKey: String): String? =
        ahmedBasetMappings[collectionKey]?.byBookUrl()

    // =========================================================================
    // 3. HADITHAPI.COM ENDPOINT BUILDERS (WITH BUILDCONFIG SECRET MANAGEMENT)
    // =========================================================================

    fun getConfiguredHadithApiKey(): String? {
        val raw = runCatching { BuildConfig.HADITH_API_KEY }.getOrNull()?.trim().orEmpty()
        return if (raw.isNotEmpty() && raw != "HADITH_API_KEY_DEFAULT_VALUE" && !raw.startsWith("YOUR_")) {
            raw
        } else {
            null
        }
    }

    fun isHadithApiKeyConfigured(): Boolean = getConfiguredHadithApiKey() != null

    fun hadithApiBooksUrl(apiKey: String? = getConfiguredHadithApiKey()): String? {
        val key = apiKey?.takeIf { it.isNotBlank() } ?: return null
        return "$HADITH_API_BASE/api/books?apiKey=$key"
    }

    fun hadithApiChaptersUrl(collectionKey: String, apiKey: String? = getConfiguredHadithApiKey()): String? {
        val key = apiKey?.takeIf { it.isNotBlank() } ?: return null
        val slug = hadithApiSlugs[collectionKey] ?: return null
        return "$HADITH_API_BASE/api/$slug/chapters?apiKey=$key"
    }

    fun hadithApiChapterHadithsUrl(
        collectionKey: String,
        chapterNo: Int,
        paginate: Int = 100,
        apiKey: String? = getConfiguredHadithApiKey()
    ): String? {
        val key = apiKey?.takeIf { it.isNotBlank() } ?: return null
        val slug = hadithApiSlugs[collectionKey] ?: return null
        return "$HADITH_API_BASE/public/api/hadiths?apiKey=$key&book=$slug&chapter=$chapterNo&paginate=$paginate"
    }

    fun hadithApiSingleHadithUrl(
        collectionKey: String,
        hadithNo: String,
        apiKey: String? = getConfiguredHadithApiKey()
    ): String? {
        val key = apiKey?.takeIf { it.isNotBlank() } ?: return null
        val slug = hadithApiSlugs[collectionKey] ?: return null
        return "$HADITH_API_BASE/public/api/hadiths?apiKey=$key&book=$slug&hadithNumber=$hadithNo"
    }

    // =========================================================================
    // 4. SUNNAH.COM API v1 ENDPOINT BUILDERS
    // =========================================================================

    fun getConfiguredSunnahApiKey(): String? {
        val raw = runCatching { BuildConfig.SUNNAH_API_KEY }.getOrNull()?.trim().orEmpty()
        return if (raw.isNotEmpty() && raw != "SUNNAH_API_KEY_DEFAULT_VALUE" && !raw.startsWith("YOUR_")) {
            raw
        } else {
            null
        }
    }

    fun isSunnahApiKeyConfigured(): Boolean = getConfiguredSunnahApiKey() != null

    fun sunnahCollectionsUrl(): String = "$SUNNAH_API_BASE/collections"

    fun sunnahCollectionBooksUrl(collectionKey: String): String? {
        val slug = sunnahComSlugs[collectionKey] ?: return null
        return "$SUNNAH_API_BASE/collections/$slug/books"
    }

    fun sunnahBookHadithsUrl(collectionKey: String, bookNumber: Int, limit: Int = 100, page: Int = 1): String? {
        val slug = sunnahComSlugs[collectionKey] ?: return null
        return "$SUNNAH_API_BASE/collections/$slug/books/$bookNumber/hadiths?limit=$limit&page=$page"
    }

    fun sunnahSingleHadithUrl(collectionKey: String, hadithNumber: String): String? {
        val slug = sunnahComSlugs[collectionKey] ?: return null
        return "$SUNNAH_API_BASE/collections/$slug/hadiths/$hadithNumber"
    }

    // =========================================================================
    // 4B. UMMAHAPI.COM ENDPOINT BUILDERS (https://ummahapi.com)
    // =========================================================================

    fun getConfiguredUmmahApiKey(): String? {
        val raw = runCatching { BuildConfig.UMMAH_API_KEY }.getOrNull()?.trim().orEmpty()
        return if (raw.isNotEmpty() && raw != "UMMAH_API_KEY_DEFAULT_VALUE" && !raw.startsWith("YOUR_")) {
            raw
        } else {
            null
        }
    }

    fun isUmmahApiKeyConfigured(): Boolean = getConfiguredUmmahApiKey() != null

    fun ummahCollectionsUrl(): String = "$UMMAH_API_BASE/api/hadith/collections"

    fun ummahCollectionHadithsUrl(collectionKey: String, page: Int = 1, limit: Int = 100): String? {
        val slug = ummahApiSlugs[collectionKey] ?: return null
        return "$UMMAH_API_BASE/api/hadith/$slug?page=$page&limit=$limit"
    }

    fun ummahSingleHadithUrl(collectionKey: String, hadithNumber: Int): String? {
        val slug = ummahApiSlugs[collectionKey] ?: return null
        return "$UMMAH_API_BASE/api/hadith/$slug/$hadithNumber"
    }

    fun ummahSearchHadithsUrl(query: String, collectionKey: String? = null): String {
        val encoded = java.net.URLEncoder.encode(query.trim(), "UTF-8")
        val slug = collectionKey?.let { ummahApiSlugs[it] }
        return if (!slug.isNullOrBlank()) {
            "$UMMAH_API_BASE/api/hadith/search?q=$encoded&collection=$slug"
        } else {
            "$UMMAH_API_BASE/api/hadith/search?q=$encoded"
        }
    }

    fun ummahRandomHadithUrl(): String = "$UMMAH_API_BASE/api/hadith/random"

    // =========================================================================
    // 5. NAMED MULTI-TRANSLATION EDITIONS PER COLLECTION
    // =========================================================================

    private val fawazTranslatorNames: Map<String, Pair<String, String>> = mapOf(
        "bukhari" to ("Dr. M. Muhsin Khan (Sahih Bukhari English)" to "مولانا داؤد راز (صحیح بخاری اردو)"),
        "muslim" to ("Abdul Hamid Siddiqui (Sahih Muslim English)" to "علامہ وحید الزماں (صحیح مسلم اردو)"),
        "abudawud" to ("Ahmad Hasan / Nasiruddin al-Khattab (Sunan Abu Dawud English)" to "مولانا وحید الزماں (سنن ابی داؤد اردو)"),
        "tirmidhi" to ("Abu Khaliyl (Jami' at-Tirmidhi English)" to "مولانا بدیع الزماں (جامع ترمذی اردو)"),
        "nasai" to ("Nasiruddin al-Khattab (Sunan an-Nasa'i English)" to "علامہ وحید الزماں (سنن نسائی اردو)"),
        "ibnmajah" to ("Nasiruddin al-Khattab (Sunan Ibn Majah English)" to "مولانا وحید الزماں (سنن ابن ماجہ اردو)"),
        "malik" to ("Aisha Abdurrahman Bewley (Muwatta Malik English)" to "علامہ وحید الزماں (موطأ مالک اردو)"),
        "nawawi" to ("Ezzeddin Ibrahim & Denys Johnson-Davies (40 Hadith Nawawi)" to "الأربعون النووية (اردو ترجمہ)"),
        "qudsi" to ("Ezzeddin Ibrahim & Denys Johnson-Davies (40 Hadith Qudsi)" to "الأحاديث القدسية (اردو ترجمہ)"),
        "dehlawi" to ("Shah Waliullah Dehlawi (40 Hadith English)" to "شاہ ولی اللہ دہلوی (اردو ترجمہ)")
    )

    private val fawazExtraLanguages: Map<String, List<Triple<String, String, String>>> = mapOf(
        "bukhari" to listOf(
            Triple("ben", "Bengali (বাংলা)", "FawazAhmed0 Bengali Sahih Bukhari (ben-bukhari)"),
            Triple("tur", "Turkish (Türkçe)", "FawazAhmed0 Turkish Sahih Bukhari (tur-bukhari)"),
            Triple("ind", "Indonesian (Bahasa Indonesia)", "FawazAhmed0 Indonesian Sahih Bukhari (ind-bukhari)"),
            Triple("fra", "French (Français)", "FawazAhmed0 French Sahih Bukhari (fra-bukhari)"),
            Triple("rus", "Russian (Русский)", "FawazAhmed0 Russian Sahih Bukhari (rus-bukhari)"),
            Triple("tam", "Tamil (தமிழ்)", "FawazAhmed0 Tamil Sahih Bukhari (tam-bukhari)")
        ),
        "muslim" to listOf(
            Triple("ben", "Bengali (বাংলা)", "FawazAhmed0 Bengali Sahih Muslim (ben-muslim)"),
            Triple("tur", "Turkish (Türkçe)", "FawazAhmed0 Turkish Sahih Muslim (tur-muslim)"),
            Triple("ind", "Indonesian (Bahasa Indonesia)", "FawazAhmed0 Indonesian Sahih Muslim (ind-muslim)"),
            Triple("fra", "French (Français)", "FawazAhmed0 French Sahih Muslim (fra-muslim)"),
            Triple("rus", "Russian (Русский)", "FawazAhmed0 Russian Sahih Muslim (rus-muslim)"),
            Triple("tam", "Tamil (தமிழ்)", "FawazAhmed0 Tamil Sahih Muslim (tam-muslim)")
        ),
        "abudawud" to listOf(
            Triple("ben", "Bengali (বাংলা)", "FawazAhmed0 Bengali Sunan Abu Dawud (ben-abudawud)"),
            Triple("tur", "Turkish (Türkçe)", "FawazAhmed0 Turkish Sunan Abu Dawud (tur-abudawud)"),
            Triple("ind", "Indonesian (Bahasa Indonesia)", "FawazAhmed0 Indonesian Sunan Abu Dawud (ind-abudawud)"),
            Triple("fra", "French (Français)", "FawazAhmed0 French Sunan Abu Dawud (fra-abudawud)"),
            Triple("rus", "Russian (Русский)", "FawazAhmed0 Russian Sunan Abu Dawud (rus-abudawud)")
        ),
        "tirmidhi" to listOf(
            Triple("ben", "Bengali (বাংলা)", "FawazAhmed0 Bengali Jami' at-Tirmidhi (ben-tirmidhi)"),
            Triple("tur", "Turkish (Türkçe)", "FawazAhmed0 Turkish Jami' at-Tirmidhi (tur-tirmidhi)"),
            Triple("ind", "Indonesian (Bahasa Indonesia)", "FawazAhmed0 Indonesian Jami' at-Tirmidhi (ind-tirmidhi)")
        ),
        "nasai" to listOf(
            Triple("ben", "Bengali (বাংলা)", "FawazAhmed0 Bengali Sunan an-Nasa'i (ben-nasai)"),
            Triple("tur", "Turkish (Türkçe)", "FawazAhmed0 Turkish Sunan an-Nasa'i (tur-nasai)"),
            Triple("ind", "Indonesian (Bahasa Indonesia)", "FawazAhmed0 Indonesian Sunan an-Nasa'i (ind-nasai)"),
            Triple("fra", "French (Français)", "FawazAhmed0 French Sunan an-Nasa'i (fra-nasai)")
        ),
        "ibnmajah" to listOf(
            Triple("ben", "Bengali (বাংলা)", "FawazAhmed0 Bengali Sunan Ibn Majah (ben-ibnmajah)"),
            Triple("tur", "Turkish (Türkçe)", "FawazAhmed0 Turkish Sunan Ibn Majah (tur-ibnmajah)"),
            Triple("ind", "Indonesian (Bahasa Indonesia)", "FawazAhmed0 Indonesian Sunan Ibn Majah (ind-ibnmajah)"),
            Triple("fra", "French (Français)", "FawazAhmed0 French Sunan Ibn Majah (fra-ibnmajah)")
        ),
        "malik" to listOf(
            Triple("ben", "Bengali (বাংলা)", "FawazAhmed0 Bengali Muwatta Malik (ben-malik)"),
            Triple("tur", "Turkish (Türkçe)", "FawazAhmed0 Turkish Muwatta Malik (tur-malik)"),
            Triple("ind", "Indonesian (Bahasa Indonesia)", "FawazAhmed0 Indonesian Muwatta Malik (ind-malik)"),
            Triple("fra", "French (Français)", "FawazAhmed0 French Muwatta Malik (fra-malik)")
        ),
        "nawawi" to listOf(
            Triple("ben", "Bengali (বাংলা)", "FawazAhmed0 Bengali 40 Hadith Nawawi (ben-nawawi)"),
            Triple("tur", "Turkish (Türkçe)", "FawazAhmed0 Turkish 40 Hadith Nawawi (tur-nawawi)"),
            Triple("ind", "Indonesian (Bahasa Indonesia)", "FawazAhmed0 Indonesian 40 Hadith Nawawi (ind-nawawi)"),
            Triple("fra", "French (Français)", "FawazAhmed0 French 40 Hadith Nawawi (fra-nawawi)"),
            Triple("rus", "Russian (Русский)", "FawazAhmed0 Russian 40 Hadith Nawawi (rus-nawawi)"),
            Triple("tam", "Tamil (தமிழ்)", "FawazAhmed0 Tamil 40 Hadith Nawawi (tam-nawawi)")
        ),
        "qudsi" to listOf(
            Triple("ben", "Bengali (বাংলা)", "FawazAhmed0 Bengali 40 Hadith Qudsi (ben-qudsi)"),
            Triple("tur", "Turkish (Türkçe)", "FawazAhmed0 Turkish 40 Hadith Qudsi (tur-qudsi)"),
            Triple("ind", "Indonesian (Bahasa Indonesia)", "FawazAhmed0 Indonesian 40 Hadith Qudsi (ind-qudsi)"),
            Triple("fra", "French (Français)", "FawazAhmed0 French 40 Hadith Qudsi (fra-qudsi)")
        )
    )

    /**
     * Returns all verified named translation editions for a given collection key.
     * Every edition shares the same canonical Arabic text in HadithEntity while maintaining
     * distinct named translation entries so users can compare or select their preferred source.
     */
    fun getNamedEditionsForCollection(collectionKey: String): List<NamedHadithTranslationEdition> {
        val result = mutableListOf<NamedHadithTranslationEdition>()
        val mapping = sunniBookMappings[collectionKey]

        // 1. FawazAhmed0 Hadith API @1 editions (English, Urdu, and international editions)
        val fawazSlug = mapping?.fawazSlug ?: fawazSlugs[collectionKey]
        if (fawazSlug != null) {
            val (engName, urdName) = fawazTranslatorNames[collectionKey]
                ?: ("FawazAhmed0 English Edition (eng-$fawazSlug)" to "FawazAhmed0 Urdu Edition (urd-$fawazSlug)")
            result.add(
                NamedHadithTranslationEdition(
                    editionKey = "eng-$fawazSlug",
                    collectionKey = collectionKey,
                    languageCode = "en",
                    languageLabel = "English",
                    editionTitle = "$engName [fawazahmed0 @1]",
                    translatorOrSourceName = engName,
                    provider = HadithSourceProvider.FAWAZ_AHMED,
                    fawazEditionName = "eng-$fawazSlug"
                )
            )
            if (collectionKey != "nawawi" && collectionKey != "qudsi" && collectionKey != "dehlawi") {
                result.add(
                    NamedHadithTranslationEdition(
                        editionKey = "urd-$fawazSlug",
                        collectionKey = collectionKey,
                        languageCode = "ur",
                        languageLabel = "Urdu (اردو)",
                        editionTitle = "$urdName [fawazahmed0 @1]",
                        translatorOrSourceName = urdName,
                        provider = HadithSourceProvider.FAWAZ_AHMED,
                        fawazEditionName = "urd-$fawazSlug"
                    )
                )
            }
            fawazExtraLanguages[collectionKey]?.forEach { (langPrefix, langLabel, title) ->
                result.add(
                    NamedHadithTranslationEdition(
                        editionKey = "$langPrefix-$fawazSlug",
                        collectionKey = collectionKey,
                        languageCode = langPrefix,
                        languageLabel = langLabel,
                        editionTitle = title,
                        translatorOrSourceName = title,
                        provider = HadithSourceProvider.FAWAZ_AHMED,
                        fawazEditionName = "$langPrefix-$fawazSlug"
                    )
                )
            }
        }

        // 2. AhmedBaset Hadith-JSON v1.2.0 (Pinned Tag v1.2.0)
        val ahmedBaset = mapping?.ahmedBaset ?: ahmedBasetMappings[collectionKey]
        if (ahmedBaset != null) {
            result.add(
                NamedHadithTranslationEdition(
                    editionKey = "eng-ahmedbaset-${ahmedBaset.bookSlug}-v1.2.0",
                    collectionKey = collectionKey,
                    languageCode = "en",
                    languageLabel = "English",
                    editionTitle = "${ahmedBaset.englishTitle} — AhmedBaset Hadith-JSON (v1.2.0)",
                    translatorOrSourceName = "AhmedBaset Hadith-JSON v1.2.0 (${ahmedBaset.category}/${ahmedBaset.bookSlug})",
                    provider = HadithSourceProvider.AHMED_BASET_V1_2_0,
                    ahmedBasetCoordinate = ahmedBaset
                )
            )
        }

        // 3. HadithAPI.com Verified English & Urdu Editions
        val hadithApiSlug = mapping?.hadithApiSlug ?: hadithApiSlugs[collectionKey]
        if (hadithApiSlug != null) {
            result.add(
                NamedHadithTranslationEdition(
                    editionKey = "eng-hadithapi-$hadithApiSlug",
                    collectionKey = collectionKey,
                    languageCode = "en",
                    languageLabel = "English",
                    editionTitle = "${mapping?.canonicalTitle ?: collectionKey} — HadithAPI.com English",
                    translatorOrSourceName = "HadithAPI.com Verified English ($hadithApiSlug)",
                    provider = HadithSourceProvider.HADITH_API_COM,
                    hadithApiBookSlug = hadithApiSlug
                )
            )
            result.add(
                NamedHadithTranslationEdition(
                    editionKey = "urd-hadithapi-$hadithApiSlug",
                    collectionKey = collectionKey,
                    languageCode = "ur",
                    languageLabel = "Urdu (اردو)",
                    editionTitle = "${mapping?.canonicalTitle ?: collectionKey} — HadithAPI.com Urdu (اردو)",
                    translatorOrSourceName = "HadithAPI.com Verified Urdu ($hadithApiSlug)",
                    provider = HadithSourceProvider.HADITH_API_COM,
                    hadithApiBookSlug = hadithApiSlug
                )
            )
        }

        // 4. Sunnah.com Official API v1 Edition
        val sunnahSlug = mapping?.sunnahComSlug ?: sunnahComSlugs[collectionKey]
        if (sunnahSlug != null) {
            result.add(
                NamedHadithTranslationEdition(
                    editionKey = "eng-sunnahcom-$sunnahSlug",
                    collectionKey = collectionKey,
                    languageCode = "en",
                    languageLabel = "English",
                    editionTitle = "${mapping?.canonicalTitle ?: collectionKey} — Sunnah.com Official Edition",
                    translatorOrSourceName = "Sunnah.com Official API ($sunnahSlug)",
                    provider = HadithSourceProvider.SUNNAH_COM_API,
                    sunnahCollectionSlug = sunnahSlug
                )
            )
        }

        // 5. UmmahAPI.com Official Hadith Edition (https://ummahapi.com)
        val ummahSlug = mapping?.ummahApiSlug ?: ummahApiSlugs[collectionKey]
        if (ummahSlug != null) {
            result.add(
                NamedHadithTranslationEdition(
                    editionKey = "eng-ummahapi-$ummahSlug",
                    collectionKey = collectionKey,
                    languageCode = "en",
                    languageLabel = "English",
                    editionTitle = "${mapping?.canonicalTitle ?: collectionKey} — UmmahAPI.com Verified Edition",
                    translatorOrSourceName = "UmmahAPI.com Verified Collection ($ummahSlug)",
                    provider = HadithSourceProvider.UMMAH_API,
                    ummahApiCollectionSlug = ummahSlug
                )
            )
        }

        // 6. Bundled Offline Archive Edition (if present in assets/hadith_corpus)
        if (mapping?.hasBundledOfflineArchive == true || result.isEmpty()) {
            result.add(
                NamedHadithTranslationEdition(
                    editionKey = "hadith_${collectionKey}_en",
                    collectionKey = collectionKey,
                    languageCode = "en",
                    languageLabel = "English",
                    editionTitle = "${mapping?.canonicalTitle ?: collectionKey} — Verified Offline English",
                    translatorOrSourceName = "Bayan ul Huda Verified Offline Archive",
                    provider = HadithSourceProvider.BUNDLED_OFFLINE_CORPUS
                )
            )
            result.add(
                NamedHadithTranslationEdition(
                    editionKey = "hadith_${collectionKey}_ur",
                    collectionKey = collectionKey,
                    languageCode = "ur",
                    languageLabel = "Urdu (اردو)",
                    editionTitle = "${mapping?.canonicalTitle ?: collectionKey} — Verified Offline Urdu (اردو)",
                    translatorOrSourceName = "Bayan ul Huda Verified Offline Archive",
                    provider = HadithSourceProvider.BUNDLED_OFFLINE_CORPUS
                )
            )
        }

        return result
    }

    /**
     * Resolves the primary offline download URLs for a given collection & scope so
     * DownloadManager can accurately fetch all 18 Sunni Hadith books from either
     * FawazAhmed0 (@1) or AhmedBaset Hadith-JSON (v1.2.0) or HadithAPI.com or UmmahAPI.com.
     */
    fun resolveDownloadSourceUrl(
        collectionKey: String,
        chapterOrSectionNo: Int?,
        hadithNo: Int?,
        preferFullBook: Boolean
    ): String {
        val fawazSlug = fawazSlugs[collectionKey]
        val ahmedBaset = ahmedBasetMappings[collectionKey]

        // Single narration
        if (hadithNo != null && fawazSlug != null) {
            return fawazHadithByNumberUrl("ara-$fawazSlug", hadithNo)
        }
        if (hadithNo != null) {
            ummahSingleHadithUrl(collectionKey, hadithNo)?.let { return it }
        }

        // Chapter / section download
        if (chapterOrSectionNo != null && !preferFullBook) {
            if (fawazSlug != null) {
                return fawazSectionUrl("ara-$fawazSlug", chapterOrSectionNo)
            }
            if (ahmedBaset != null) {
                return ahmedBaset.byChapterUrl(chapterOrSectionNo)
            }
            hadithApiChapterHadithsUrl(collectionKey, chapterOrSectionNo)?.let { return it }
            ummahCollectionHadithsUrl(collectionKey, page = chapterOrSectionNo)?.let { return it }
        }

        // Full book download
        if (fawazSlug != null) {
            return fawazWholeEditionUrl("ara-$fawazSlug")
        }
        if (ahmedBaset != null) {
            return ahmedBaset.byBookUrl()
        }
        hadithApiChapterHadithsUrl(collectionKey, chapterOrSectionNo ?: 1)?.let { return it }
        ummahCollectionHadithsUrl(collectionKey, page = chapterOrSectionNo ?: 1)?.let { return it }
        return fawazInfoUrl()
    }
}
