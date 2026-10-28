package com.example.data.local

import java.util.Locale

/**
 * Verified catalog of multi-language Ayah-by-Ayah Quran translations from the
 * Quranic Universal Library (QUL - https://qul.tarteel.ai/resources/translation)
 * and fawazahmed0/quran-api (specifically for Afar: aar-sheikhmahmoudab).
 *
 * Strictly preserves existing English, Urdu, and Arabic translations while adding
 * all 72 QUL languages + Afar (73 languages total) with native script labels,
 * RTL metadata, footnote format metadata, and offline + full-pack download endpoints.
 * Reports Dagbani as missing per project specification.
 */
object QulTranslationCatalog {

  data class QulTranslationEdition(
    val editionKey: String,
    val languageCode: String,
    val languageNameEn: String,
    val languageNameNative: String,
    val isRtl: Boolean,
    val translatorName: String,
    val qulResourceId: Int?,
    val qcomTranslationId: Int?,
    val fawazEditionKey: String?,
    val quranEncKey: String?,
    val hasFootnotes: Boolean,
    val source: String, // "QUL" or "fawazahmed0/quran-api"
    val format: String, // "with-footnote-tags" or "simple"
    val ayahCount: Int = 6236
  )

  data class QulLanguageGroup(
    val languageCode: String,
    val languageNameEn: String,
    val languageNameNative: String,
    val isRtl: Boolean,
    val editions: List<QulTranslationEdition>,
    val isMissingInSource: Boolean = false,
    val missingReason: String? = null
  )

  data class SkippedQulResource(
    val qulResourceId: Int?,
    val name: String,
    val language: String,
    val reason: String
  )

  /**
   * RTL language codes requiring right-to-left text layout and alignment:
   * Persian (Farsi), Dari, Pashto, Kurdish, Sindhi, Kashmiri, Uighur, Dhivehi, Hebrew,
   * plus existing Arabic and Urdu.
   */
  val RTL_LANGUAGE_CODES: Set<String> = setOf(
    "ar", "ur", "fa", "prs", "ps", "ku", "sd", "ks", "ug", "dv", "he"
  )

  val skippedResources: List<SkippedQulResource> = listOf(
    SkippedQulResource(
      qulResourceId = NoneOrInt(426),
      name = "Dr. Mustafa Khattab, The Clear Quran",
      language = "English",
      reason = "Copyrighted on QUL ('No files to download' / permission required); already preserved in existing Sunni English catalog"
    ),
    SkippedQulResource(
      qulResourceId = null,
      name = "Dagbani Quran Translation",
      language = "Dagbani",
      reason = "Missing in both QUL (qul.tarteel.ai) and fawazahmed0/quran-api; reported as missing per specification"
    ),
    SkippedQulResource(
      qulResourceId = NoneOrInt(472),
      name = "13 Word-by-Word QUL Translations (English, Bengali, Indonesian, Turkish, German, Russian, Urdu, Persian, Tamil, Divehi, Ingush)",
      language = "Multiple",
      reason = "Excluded per specification: only Ayah-by-Ayah translations are imported"
    )
  )

  private fun NoneOrInt(v: Int): Int = v

  private fun ed(
    editionKey: String,
    langCode: String,
    langEn: String,
    langNative: String,
    isRtl: Boolean,
    translator: String,
    qulId: Int?,
    qcomId: Int?,
    fawazKey: String?,
    qencKey: String?,
    hasFn: Boolean,
    source: String = "QUL",
    ayahCount: Int = 6236
  ) = QulTranslationEdition(
    editionKey = editionKey,
    languageCode = langCode,
    languageNameEn = langEn,
    languageNameNative = langNative,
    isRtl = isRtl,
    translatorName = translator,
    qulResourceId = qulId,
    qcomTranslationId = qcomId,
    fawazEditionKey = fawazKey,
    quranEncKey = qencKey,
    hasFootnotes = hasFn,
    source = source,
    format = if (hasFn) "with-footnote-tags" else "simple",
    ayahCount = ayahCount
  )

  val allEditions: List<QulTranslationEdition> = listOf(
    // 1. Persian (Farsi) - RTL
    ed("qul.fa.135", "fa", "Persian (Farsi)", "فارسی", true, "IslamHouse.com (Persian Translation)", 169, 135, "pes-islamhousecom", "persian_ih", false),
    ed("qul.fa.29", "fa", "Persian (Farsi)", "فارسی", true, "Hussein Taji Kal Dari", 188, 29, "pes-husseintajikald", "persian_tagi", false),
    // 2. Dari - RTL
    ed("qul.prs.785", "prs", "Dari", "دری", true, "Mawlawi Muhammad Anwar Badkhashani", 291, 785, null, "dari_badkhashani", false),
    // 3. Pashto - RTL
    ed("qul.ps.118", "ps", "Pashto", "پښتو", true, "Zakaria Abulsalam", 164, 118, "pus-zakariaabulsala", "pashto_zakaria", false),
    ed("qul.ps.449", "ps", "Pashto", "پښتو", true, "Rowwad Translation Center (Pashto)", 449, null, "pus-rowadtranslatio", "pashto_rwwad", false),
    // 4. Kurdish - RTL
    ed("qul.ku.81", "ku", "Kurdish", "کوردی", true, "Burhan Muhammad-Amin", 144, 81, "kur-burhanmuhammada", null, false),
    ed("qul.ku.143", "ku", "Kurdish", "کوردی", true, "Muhammad Saleh Bamoki", 177, 143, "kur-muhammadsalehba", "kurdish_bamoki", false),
    ed("qul.ku.427", "ku", "Kurdish", "کوردی", true, "Salahuddin Abdulkarim", 427, null, "kur-salahuddinabdul", "kurdish_salahuddin", false),
    // 5. Sindhi - RTL
    ed("qul.sd.238", "sd", "Sindhi", "سنڌي", true, "Taj Mehmood Amroti", 222, 238, "snd-tajmehmoodamrot", null, false),
    // 6. Kashmiri - RTL
    ed("qul.ks.273", "ks", "Kashmiri", "کٲشُر", true, "Bayanul Furqan (Koshur Quran)", 273, null, null, null, false),
    // 7. Uighur - RTL
    ed("qul.ug.76", "ug", "Uighur", "ئۇيغۇرچە", true, "Muhammad Saleh", 304, 76, "uig-muhammadsaleh", "uyghur_saleh", false),
    // 8. Dhivehi - RTL
    ed("qul.dv.86", "dv", "Dhivehi", "ދިވެހި", true, "Office of the President of Maldives", 226, 86, "div-officeofthepres", null, false),
    ed("qul.dv.840", "dv", "Dhivehi", "ދިވެހި", true, "Abu Bakr Ibrahim Ali (Bakurube)", 280, 840, null, null, false),
    // 9. Hindi
    ed("qul.hi.122", "hi", "Hindi", "हिन्दी", false, "Maulana Azizul Haque al-Umari", 166, 122, "hin-maulanaazizulha", "hindi_omari", true),
    // 10. Bengali
    ed("qul.bn.213", "bn", "Bengali", "বাংলা", false, "Dr. Abu Bakr Muhammad Zakaria", 190, 213, "ben-abubakrmuhammad", "bengali_zakaria", true),
    ed("qul.bn.161", "bn", "Bengali", "বাংলা", false, "Taisirul Quran (Tawheed Publication)", 184, 161, null, null, false),
    ed("qul.bn.162", "bn", "Bengali", "বাংলা", false, "Rawai Al-Bayan", 185, 162, null, null, false),
    ed("qul.bn.163", "bn", "Bengali", "বাংলা", false, "Sheikh Mujibur Rahman", 186, 163, null, null, false),
    ed("qul.bn.120b", "bn", "Bengali", "বাংলা", false, "Muhiuddin Khan", 119, 161, "ben-muhiuddinkhan", null, false),
    // 11. Punjabi
    ed("qul.pa.423", "pa", "Punjabi", "پنجابی / ਪੰਜਾਬੀ", false, "Arif Halim (Punjabi)", 423, null, "pan-drmuhamadhabibb", "punjabi_arif", false),
    // 12. Gujarati
    ed("qul.gu.225", "gu", "Gujarati", "ગુજરાતી", false, "Rabila Al-Umry", 211, 225, "guj-rabilaalumry", "gujarati_omari", false),
    // 13. Marathi
    ed("qul.mr.226", "mr", "Marathi", "मराठी", false, "Muhammad Shafi’i Ansari", 213, 226, "mar-muhammadshafiia", "marathi_ansari", false),
    // 14. Nepali
    ed("qul.ne.108", "ne", "Nepali", "नेपाली", false, "Ahl Al-Hadith Central Society of Nepal", 158, 108, "nep-ahlalhadithcent", "nepali_central", false),
    // 15. Assamese
    ed("qul.as.120", "as", "Assamese", "অসমীয়া", false, "Shaykh Rafeequl Islam Habibur-Rahman", 165, 120, "asm-shaykhrafeequli", "assamese_rafeeq", true),
    // 16. Tamil
    ed("qul.ta.50", "ta", "Tamil", "தமிழ்", false, "Jan Trust Foundation", 140, 50, "tam-jantrustfoundat", null, false),
    ed("qul.ta.133", "ta", "Tamil", "தமிழ்", false, "Abdul Hameed Baqavi", 172, 133, "tam-abdulhameedbaqa", "tamil_baqavi", false),
    ed("qul.ta.229", "ta", "Tamil", "தமிழ்", false, "Sheikh Omar Sharif bin Abdul Salam", 214, 229, "tam-omarsharifbinab", "tamil_omar", false),
    // 17. Telugu
    ed("qul.te.227", "te", "Telugu", "తెలుగు", false, "Maulana Abder-Rahim ibn Muhammad", 210, 227, "tel-abderrahimibnmu", "telugu_abdulrahim", false),
    // 18. Kannada
    ed("qul.kn.771", "kn", "Kannada", "ಕನ್ನಡ", false, "Kannada Translation (Abdur-Rahim)", 235, 771, "kan-abdulhamid", "kannada_hamid", true),
    ed("qul.kn.442", "kn", "Kannada", "ಕನ್ನಡ", false, "Bashir Mysuru", 442, null, "kan-bashirmysuru", "kannada_bashir", false),
    // 19. Malayalam
    ed("qul.ml.37", "ml", "Malayalam", "മലയാളം", false, "Cheriyamundam Abdul Hameed and Kunhi Mohammed", 130, 37, "mal-cheriyamundamab", "malayalam_kunhi", false),
    ed("qul.ml.80", "ml", "Malayalam", "മലയാളം", false, "Muhammad Karakunnu and Vanidas Elayavoor", 151, 80, "mal-muhammadkarakun", null, false),
    ed("qul.ml.224", "ml", "Malayalam", "മലയാളം", false, "Abdul-Hamid Haidar & Kanhi Muhammad", 209, 224, null, null, false),
    // 20. Sinhala
    ed("qul.si.228", "si", "Sinhala", "සිංහල", false, "Ruwwad Center (Sinhala)", 212, 228, "sin-rowadtranslatio", "sinhalese_mahir", false),
    // 21. Indonesian
    ed("qul.id.33", "id", "Indonesian", "Bahasa Indonesia", false, "Indonesian Islamic Affairs Ministry (Kemenag)", 224, 33, "ind-indonesianislam", "indonesian_affairs", true),
    ed("qul.id.134", "id", "Indonesian", "Bahasa Indonesia", false, "King Fahad Quran Complex (Indonesian)", 173, 134, "ind-kingfahadquranc", "indonesian_complex", true),
    ed("qul.id.141", "id", "Indonesian", "Bahasa Indonesia", false, "The Sabiq Company", 194, 141, "ind-thesabiqcompany", "indonesian_sabiq", true),
    // 22. Malay
    ed("qul.ms.39", "ms", "Malay", "Bahasa Melayu", false, "Abdullah Muhammad Basmeih", 292, 39, "msa-abdullahmuhamma", null, false),
    // 23. Tagalog
    ed("qul.tl.211", "tl", "Tagalog", "Tagalog", false, "Dar Al-Salam Center (Tagalog)", 199, 211, "tgl-abdullatifeduar", "tagalog_rwwad", false),
    // 24. Vietnamese
    ed("qul.vi.220", "vi", "Vietnamese", "Tiếng Việt", false, "Ruwwad Center (Vietnamese)", 207, 220, "vie-rowadtranslatio", "vietnamese_rwwad", true),
    ed("qul.vi.221", "vi", "Vietnamese", "Tiếng Việt", false, "Hasan Abdul-Karim", 205, 221, "vie-hassanabdulkar", "vietnamese_hassan", false),
    // 25. Thai
    ed("qul.th.51", "th", "Thai", "ภาษาไทย", false, "King Fahad Quran Complex (Thai)", 232, 51, "tha-kingfahadquranc", "thai_complex", false),
    ed("qul.th.230", "th", "Thai", "ภาษาไทย", false, "Society of Institutes and Universities (Thai)", 215, 230, null, null, false),
    // 26. Khmer
    ed("qul.km.128", "km", "Khmer", "ភាសាខ្មែរ", false, "Cambodian Muslim Community Development", 171, 128, "khm-cambodianmuslim", "khmer_cambodia", false),
    ed("qul.km.425", "km", "Khmer", "ភាសាខ្មែរ", false, "Rowwad Translation Center (Khmer)", 425, null, "khm-rowadtranslatio", "khmer_rwwad", false),
    // 27. Chinese
    ed("qul.zh.56", "zh", "Chinese", "中文", false, "Ma Jian (Simplified)", 143, 56, "zho-majian", null, false),
    ed("qul.zh.109", "zh", "Chinese", "中文", false, "Muhammad Makin", 159, 109, "zho-muhammadmakin", "chinese_makin", false),
    // 28. Japanese
    ed("qul.ja.35", "ja", "Japanese", "日本語", false, "Ryoichi Mita", 189, 35, "jpn-ryoichimita", null, false),
    ed("qul.ja.218", "ja", "Japanese", "日本語", false, "Saeed Sato", 202, 218, "jpn-saeedsato", "japanese_saeedsato", false),
    // 29. Korean
    ed("qul.ko.36", "ko", "Korean", "한국어", false, "Korean Translation (King Fahad Complex)", 129, 36, "kor-unknown", null, false),
    ed("qul.ko.219", "ko", "Korean", "한국어", false, "Hamed Choi", 204, 219, "kor-hamidchoi", "korean_hamid", false),
    // 30. Turkish
    ed("qul.tr.77", "tr", "Turkish", "Türkçe", false, "Diyanet İşleri", 148, 77, "tur-diyanetisleri", null, false),
    ed("qul.tr.52", "tr", "Turkish", "Türkçe", false, "Elmalılı Hamdi Yazır", 233, 52, "tur-elmalilihamdiya", null, false),
    ed("qul.tr.112", "tr", "Turkish", "Türkçe", false, "Shaban Britch", 161, 112, "tur-shabanbritch", "turkish_shaban", false),
    ed("qul.tr.124", "tr", "Turkish", "Türkçe", false, "Muslim Shahin", 157, 124, "tur-muslimshahin", "turkish_shahin", false),
    ed("qul.tr.210", "tr", "Turkish", "Türkçe", false, "Dar Al-Salam Center (Turkish)", 196, 210, "tur-daralsalamcente", "turkish_rwwad", false),
    // 31. Azerbaijani
    ed("qul.az.75", "az", "Azerbaijani", "Azərbaycan dili", false, "Alikhan Musayev", 147, 75, "aze-alikhanmusayev", "azeri_musayev", false),
    ed("qul.az.23", "az", "Azerbaijani", "Azərbaycan dili", false, "Vasim Mammadaliyev and Ziya Bunyadov", 118, 23, "aze-vasimmammadaliy", null, false),
    // 32. Uzbek
    ed("qul.uz.55", "uz", "Uzbek", "Oʻzbekcha", false, "Muhammad Sodiq Muhammad Yusuf (Latin)", 142, 55, "uzb-muhammadsodikmu", null, true),
    ed("qul.uz.101", "uz", "Uzbek", "Oʻzbekcha", false, "Alauddin Mansour", 225, 101, "uzb-alauddinmansour", "uzbek_mansour", true),
    ed("qul.uz.127", "uz", "Uzbek", "Ўзбекча", false, "Muhammad Sodik Muhammad Yusuf (Cyrillic)", 168, 127, null, "uzbek_sadiq", true),
    // 33. Kazakh
    ed("qul.kk.222", "kk", "Kazakh", "Қазақша", false, "Khalifa Altay", 208, 222, "kaz-khalifaaltay", "kazakh_altai", false),
    // 34. Tajik
    ed("qul.tg.74", "tg", "Tajik", "Тоҷикӣ", false, "AbdolMohammad Ayati", 146, 74, "tgk-abdolmohammaday", null, false),
    ed("qul.tg.139", "tg", "Tajik", "Тоҷикӣ", false, "Khawaja Mirof & Khawaja Mir", 175, 139, "tgk-khawajamirofkha", "tajik_arifi", true),
    ed("qul.tg.223", "tg", "Tajik", "Тоҷикӣ", false, "Pioneers of Translation Center (Tajik)", 206, 223, null, null, false),
    // 35. Tatar
    ed("qul.tt.53", "tt", "Tatar", "Татарча", false, "Yakub Ibn Nugman", 141, 53, "tat-yakubibnnugman", null, false),
    // 36. Chechen
    ed("qul.ce.106", "ce", "Chechen", "Нохчийн мотт", false, "Magomed Magomedov", 156, 106, "che-magomedmagomedo", null, true, "QUL", 6030),
    // 37. Russian
    ed("qul.ru.45", "ru", "Russian", "Русский", false, "Elmir Kuliev", 136, 45, "rus-elmirkuliev", "russian_kuliev", false),
    ed("qul.ru.78", "ru", "Russian", "Русский", false, "Ministry of Awqaf, Egypt", 149, 78, "rus-ministryofawqaf", "russian_porokhova", false),
    ed("qul.ru.79", "ru", "Russian", "Русский", false, "Abu Adel", 150, 79, "rus-abuadel", "russian_aboadel", false),
    ed("qul.ru.271", "ru", "Russian", "Русский", false, "Magomed-Nuri Osmanovich Osmanov", 271, null, "rus-magomednuriosma", null, false),
    ed("qul.ru.274", "ru", "Russian", "Русский", false, "Gordy Semyonovich Sablukov", 274, null, "rus-gordysemyonovic", null, false),
    // 38. Ukrainian
    ed("qul.uk.217", "uk", "Ukrainian", "Українська", false, "Dr. Mikhailo Yaqubovic", 203, 217, "ukr-mykhayloyakubov", "ukrainian_yakubovych", false),
    // 39. Bulgarian
    ed("qul.bg.237", "bg", "Bulgarian", "Български", false, "Tzvetan Theophanov", 221, 237, "bul-tzvetantheophan", null, false),
    // 40. Macedonian
    ed("qul.mk.289", "mk", "Macedonian", "Македонски", false, "Macedonian Scholars Group", 289, null, "mkd-macedonianschol", "macedonian_group", false),
    // 41. Bosnian
    ed("qul.bs.126", "bs", "Bosnian", "Bosanski", false, "Besim Korkut", 162, 126, "bos-besimkorkut", "bosnian_korkut", false),
    ed("qul.bs.25", "bs", "Bosnian", "Bosanski", false, "Muhamed Mehanović", 119, 25, "bos-muhamedmehanovi", "bosnian_mihanovich", false),
    ed("qul.bs.214", "bs", "Bosnian", "Bosanski", false, "Dar Al-Salam Center (Bosnian)", 200, 214, "bos-rwwad", "bosnian_rwwad", false),
    // 42. Albanian
    ed("qul.sq.88", "sq", "Albanian", "Shqip", false, "Hasan Efendi Nahi", 155, 88, "sqi-hasanefendinahi", "albanian_nahi", true),
    ed("qul.sq.89", "sq", "Albanian", "Shqip", false, "Sherif Ahmeti", 137, 89, "sqi-sherifahmeti", null, false),
    ed("qul.sq.47", "sq", "Albanian", "Shqip", false, "Feti Mehdiu", 137, 47, "sqi-fetimehdiu", null, false),
    // 43. Czech
    ed("qul.cs.26", "cs", "Czech", "Čeština", false, "Preklad I. Hrbek / A. R. Nykl", 120, 26, "ces-prekladihrbek", null, false),
    // 44. Polish
    ed("qul.pl.42", "pl", "Polish", "Polski", false, "Józef Bielawski", 133, 42, "pol-jozefabielawski", null, false),
    // 45. Lithuanian
    ed("qul.lt.279", "lt", "Lithuanian", "Lietuvių", false, "Lithuanian Translation (Rowwad)", 279, null, null, "lithuanian_rwwad", true),
    // 46. Romanian
    ed("qul.ro.44", "ro", "Romanian", "Română", false, "George Grigore", 135, 44, "ron-georgegrigore", "romanian_grigore", false),
    ed("qul.ro.782", "ro", "Romanian", "Română", false, "Islamic and Cultural League (Romanian)", 293, 782, "ron-islamicandcultu", "romanian_project", true),
    // 47. Finnish
    ed("qul.fi.30", "fi", "Finnish", "Suomi", false, "Finnish Translation", 127, 30, "fin-unknown", null, false),
    // 48. Swedish
    ed("qul.sv.48", "sv", "Swedish", "Svenska", false, "Knut Bernström", 138, 48, "swe-knutbernstrom", null, false),
    // 49. Norwegian
    ed("qul.no.41", "no", "Norwegian", "Norsk", false, "Einar Berg", 132, 41, "nor-einarberg", null, false),
    // 50. Dutch
    ed("qul.nl.144", "nl", "Dutch", "Nederlands", false, "Sofian S. Siregar", 178, 144, "nld-sofianssiregar", "dutch_siregar", false),
    ed("qul.nl.235", "nl", "Dutch", "Nederlands", false, "Malak Faris Abdalsalaam", 219, 235, "nld-malakfarisabdal", "dutch_center", false),
    // 51. German
    ed("qul.de.27", "de", "German", "Deutsch", false, "Frank Bubenheim and Nadeem", 191, 27, "deu-frankbubenheima", "german_bubenheim", false),
    ed("qul.de.208", "de", "German", "Deutsch", false, "Abu Reda Muhammad ibn Ahmad", 197, 208, "deu-aburedamuhammad", "german_abureda", false),
    // 52. French
    ed("qul.fr.31", "fr", "French", "Français", false, "Muhammad Hamidullah", 227, 31, "fra-muhammadhamidul", "french_hameedullah", true),
    ed("qul.fr.136", "fr", "French", "Français", false, "Montada Islamic Foundation", 174, 136, "fra-islamicfoundati", "french_montada", true),
    ed("qul.fr.779", "fr", "French", "Français", false, "Rashid Maash", 295, 779, "fra-rashidmaash", "french_rashid", true),
    // 53. Spanish
    ed("qul.es.83", "es", "Spanish", "Español", false, "Sheikh Isa Garcia", 152, 83, "spa-isagarcia", "spanish_garcia", true),
    ed("qul.es.140", "es", "Spanish", "Español", false, "Montada Islamic Foundation (Spanish)", 176, 140, "spa-islamicfoundati", "spanish_montada", true),
    ed("qul.es.199", "es", "Spanish", "Español", false, "Noor International Center", 223, 199, "spa-noorinternation", "spanish_noor", true),
    ed("qul.es.28", "es", "Spanish", "Español", false, "Julio Cortes", 126, 28, "spa-juliocortes", null, false),
    // 54. Portuguese
    ed("qul.pt.43", "pt", "Portuguese", "Português", false, "Samir El-Hayek", 134, 43, "por-samirelhayek", null, false),
    ed("qul.pt.103", "pt", "Portuguese", "Português", false, "Helmi Nasr", 228, 103, "por-helminasr", "portuguese_nasr", false),
    // 55. Italian
    ed("qul.it.153", "it", "Italian", "Italiano", false, "Hamza Roberto Piccardo", 181, 153, "ita-hamzarobertopic", "italian_rwwad", true),
    ed("qul.it.209", "it", "Italian", "Italiano", false, "Othman al-Sharif", 198, 209, "ita-othmanalsharif", null, false),
    // 56. Hebrew - RTL
    ed("qul.he.233", "he", "Hebrew", "עברית", true, "Dar Al-Salam Center (Hebrew)", 297, 233, "heb-daralsalamcente", "hebrew_darussalam", false),
    // 57. Hausa
    ed("qul.ha.32", "ha", "Hausa", "Hausa", false, "Abubakar Mahmoud Gumi", 128, 32, "hau-abubakarmahmoud", "hausa_gummi", false),
    ed("qul.ha.115", "ha", "Hausa", "Hausa", false, "Abubakar Mahmood Jummi", 163, 115, null, null, true),
    // 58. Swahili
    ed("qul.sw.49", "sw", "Swahili", "Kiswahili", false, "Ali Muhsin Al-Barwani", 139, 49, "swa-alimuhsinalbarw", "swahili_barawani", false),
    ed("qul.sw.231", "sw", "Swahili", "Kiswahili", false, "Dr. Abdullah Muhammad Abu Bakr and Sheikh Nasir Khamis", 216, 231, "swa-abdullahmuhamma", "swahili_abubakr", false),
    ed("qul.sw.466", "sw", "Swahili", "Kiswahili", false, "Rowwad Translation Center (Swahili)", 466, null, "swa-rowadtranslatio", "swahili_rwwad", false),
    // 59. Somali
    ed("qul.so.46", "so", "Somali", "Soomaaliga", false, "Mahmud Muhammad Abduh", 231, 46, "som-mahmudmuhammada", "somali_abduh", false),
    ed("qul.so.290", "so", "Somali", "Soomaaliga", false, "Abdullah Hassan Yacoub", 290, null, "som-abdullahhassany", "somali_yacob", true),
    // 60. Amharic
    ed("qul.am.87", "am", "Amharic", "አማርኛ", false, "Sadiq and Sani", 154, 87, "amh-muhammedsadiqan", "amharic_sadiq", false),
    // 61. Oromo
    ed("qul.om.111", "om", "Oromo", "Afaan Oromoo", false, "Ghali Apapur Apaghuna", 160, 111, "orm-ghaliapapurapag", "oromo_ababor", false),
    // 62. Yoruba
    ed("qul.yo.125", "yo", "Yoruba", "Yorùbá", false, "Shaykh Abu Rahimah Mikael Aykyuni", 167, 125, "yor-shaykhaburahima", "yoruba_mikail", true),
    // 63. Asante (Twi)
    ed("qul.tw.285", "tw", "Asante", "Twi (Asante)", false, "Sheikh Haroun Ismaeel", 285, null, "twi-sheikhharounism", "asante_harun", false),
    // 64. Bambara
    ed("qul.bm.795", "bm", "Bambara", "Bamanankan", false, "Suliman Kanti", 288, 795, "bam-sulimankanti", "ankobambara_foudi", false),
    ed("qul.bm.796", "bm", "Bambara", "Bamanankan", false, "Baba Mamady Jani", 294, 796, "bam-babamamadyjani", "bambara_dayyan", false),
    // 65. Fulah
    ed("qul.ff.435", "ff", "Fulah", "Fulfulde / Pulaar", false, "Rowwad Translation Center (Fulah)", 435, null, "ful-rowadtranslatio", "fulani_rwwad", false),
    // 66. Ganda
    ed("qul.lg.232", "lg", "Ganda", "Luganda", false, "African Development Foundation (Luganda)", 217, 232, "lug-africandevelopm", "luganda_foundation", false),
    // 67. Lingala
    ed("qul.ln.431", "ln", "Lingala", "Lingála", false, "Zakariya Muhammed Balingongo", 431, null, "lin-zakariyamuhamme", "lingala_zakaria", false),
    // 68. Maranao
    ed("qul.mrw.38", "mrw", "Maranao", "Mëranaw", false, "Guro Alim Saromantang", 131, 38, null, null, false),
    // 69. Chichewa
    ed("qul.ny.450", "ny", "Chichewa", "Chichewa", false, "Khaled Ibrahim Betala", 450, null, "nya-khaledibrahimbe", "chichewa_betala", false),
    // 70. Kinyarwanda
    ed("qul.rw.774", "rw", "Kinyarwanda", "Ikinyarwanda", false, "The Rwanda Muslims Association Team", 296, 774, "kin-therwandamuslim", "kinyarwanda_assoc", true),
    // 71. Yao
    ed("qul.yao.798", "yao", "Yao", "Chiyao", false, "Abdul Hamid Silika", 286, 798, "yao-abdulhamidsilik", "yao_silika", true),
    // 72. Tamazight (Amazigh)
    ed("qul.ber.236", "ber", "Tamazight (Amazigh)", "Tamaziɣt", false, "Ramdane At Mansour", 220, 236, "ber-ramdaneatmansou", null, false),
    // 73. Afar (via fawazahmed0/quran-api edition aar-sheikhmahmoudab)
    ed(
      editionKey = "fawaz.aar.sheikhmahmoudab",
      langCode = "aa",
      langEn = "Afar",
      langNative = "Qafar af",
      isRtl = false,
      translator = "Sheikh Mahmoud Abdel Qader Hamz (aar-sheikhmahmoudab)",
      qulId = 452,
      qcomId = null,
      fawazKey = "aar-sheikhmahmoudab",
      qencKey = "afar_hamza",
      hasFn = false,
      source = "fawazahmed0/quran-api"
    )
  )

  private val editionsByKey: Map<String, QulTranslationEdition> by lazy {
    allEditions.associateBy { it.editionKey }
  }

  val languageGroups: List<QulLanguageGroup> by lazy {
    val grouped = LinkedHashMap<String, MutableList<QulTranslationEdition>>()
    for (edition in allEditions) {
      grouped.getOrPut(edition.languageNameEn) { mutableListOf() }.add(edition)
    }
    val list = grouped.map { (langEn, eds) ->
      val first = eds.first()
      QulLanguageGroup(
        languageCode = first.languageCode,
        languageNameEn = langEn,
        languageNameNative = first.languageNameNative,
        isRtl = first.isRtl,
        editions = eds
      )
    }.toMutableList()

    // Explicitly include Dagbani as reported missing per specification
    list.add(
      QulLanguageGroup(
        languageCode = "dag",
        languageNameEn = "Dagbani",
        languageNameNative = "Dagbanli",
        isRtl = false,
        editions = emptyList(),
        isMissingInSource = true,
        missingReason = "Reported missing: Not available in QUL (qul.tarteel.ai) or fawazahmed0/quran-api."
      )
    )
    list
  }

  fun findEditionByKey(editionKey: String): QulTranslationEdition? = editionsByKey[editionKey]

  fun isRtlEdition(editionKey: String): Boolean {
    if (editionKey.startsWith("ur.") || editionKey.startsWith("ar.")) return true
    val qulEd = findEditionByKey(editionKey) ?: return false
    return qulEd.isRtl || RTL_LANGUAGE_CODES.contains(qulEd.languageCode.lowercase(Locale.ROOT))
  }

  fun getDisplayTitleForEditionKey(editionKey: String, selectedTradition: String): String {
    val legacy = (ShiaTranslationDataProvider.sunniEditions + ShiaTranslationDataProvider.shiaEditions)
      .find { it.key == editionKey }
    if (legacy != null) return "${legacy.title} (${legacy.languageCode.uppercase()})"

    val qul = findEditionByKey(editionKey)
    if (qul != null) return "${qul.languageNameNative} (${qul.languageNameEn}) • ${qul.translatorName}"

    return if (selectedTradition == "SHIA") "Ali Quli Qara'i (EN)" else "Sahih International (EN)"
  }

  /**
   * Resolves the best default translation edition for the user's device locale if supported,
   * falling back to existing English defaults.
   */
  fun resolveDefaultEditionForLocale(
    locale: Locale = Locale.getDefault(),
    tradition: String = "SUNNI"
  ): String {
    val lang = locale.language.lowercase(Locale.ROOT)
    if (lang == "en") {
      return if (tradition == "SHIA") "en.qarai" else "en.sahih"
    }
    if (lang == "ur") {
      return if (tradition == "SHIA") "ur.jawadi" else "ur.jalandhry"
    }
    val matched = allEditions.firstOrNull {
      it.languageCode.equals(lang, ignoreCase = true) ||
        (lang == "in" && it.languageCode == "id") ||
        (lang == "iw" && it.languageCode == "he") ||
        (lang == "ji" && it.languageCode == "yi") ||
        (lang == "ak" && it.languageCode == "tw")
    }
    return matched?.editionKey ?: if (tradition == "SHIA") "en.qarai" else "en.sahih"
  }
}
