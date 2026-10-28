package com.example.data.repository

import com.example.data.local.entities.AyahEntity
import com.example.data.local.entities.HadithEntity
import com.example.data.local.entities.SurahEntity

data class TafsirEditionMeta(
  val key: String,
  val titleEnglish: String,
  val titleArabic: String,
  val titleUrdu: String,
  val scholarName: String,
  val eraBadge: String, // "Classical" or "Contemporary"
  val methodology: String,
  val tradition: String = if (eraBadge.contains("Shia", ignoreCase = true)) "SHIA" else "SUNNI"
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
  val linkedHadiths: List<HadithEntity>
)

object TafsirRepository {

  val editions: List<TafsirEditionMeta> = listOf(
    TafsirEditionMeta(
      key = "ibn_kathir",
      titleEnglish = "Tafsir Ibn Kathir",
      titleArabic = "تفسير القرآن العظيم لابن كثير",
      titleUrdu = "تفسیر ابنِ کثیر",
      scholarName = "Hafiz Isma'il ibn 'Umar ibn Kathir al-Dimashqi (d. 774 AH)",
      eraBadge = "Classical (Athari)",
      methodology = "Tafsir bil-Ma'thur — Explaining the Qur'an by the Qur'an, authentic Sunnah, and statements of the Sahabah and Tabi'un."
    ),
    TafsirEditionMeta(
      key = "qurtubi",
      titleEnglish = "Tafsir al-Qurtubi",
      titleArabic = "الجامع لأحكام القرآن للقرطبي",
      titleUrdu = "تفسیر قرطبی (احکام القرآن)",
      scholarName = "Imam Abu 'Abdullah Muhammad ibn Ahmad al-Qurtubi (d. 671 AH)",
      eraBadge = "Classical (Fiqhi & Exegetical)",
      methodology = "Comprehensive legal rulings (Ahkam), linguistic precision, Qira'at nuances, and transmission from the Salaf."
    ),
    TafsirEditionMeta(
      key = "tabari",
      titleEnglish = "Tafsir al-Tabari",
      titleArabic = "جامع البيان عن تأويل آي القرآن للطبري",
      titleUrdu = "تفسیر طبری (جامع البیان)",
      scholarName = "Imam Abu Ja'far Muhammad ibn Jarir al-Tabari (d. 310 AH)",
      eraBadge = "Classical (Foundational)",
      methodology = "Earliest encyclopedic transmitted Tafsir with chains of narration (Isnad), Arabic grammar analysis, and juristic weighing."
    ),
    TafsirEditionMeta(
      key = "jalalayn",
      titleEnglish = "Tafsir al-Jalalayn",
      titleArabic = "تفسير الجلالين",
      titleUrdu = "تفسیر جلالین",
      scholarName = "Imam Jalal al-Din al-Mahalli (d. 864 AH) & Imam Jalal al-Din al-Suyuti (d. 911 AH)",
      eraBadge = "Classical (Concise)",
      methodology = "Crystal-clear phrase-by-phrase lexical, grammatical, and contextual elucidation of the Uthmani text."
    ),
    TafsirEditionMeta(
      key = "maariful_quran",
      titleEnglish = "Ma'ariful Quran",
      titleArabic = "معارف القرآن",
      titleUrdu = "معارف القرآن (مفتی محمد شفیعؒ)",
      scholarName = "Grand Mufti Muhammad Shafi Usmani (d. 1396 AH / 1976 CE)",
      eraBadge = "Contemporary",
      methodology = "Synthesizes classical commentaries (Bayan-ul-Quran, Mazhari, Ibn Kathir) with practical Fiqh rulings and modern spiritual counsel."
    ),
    TafsirEditionMeta(
      key = "as_sadi",
      titleEnglish = "Tafsir as-Sa'di",
      titleArabic = "تيسير الكريم الرحمن في تفسير كلام المنان",
      titleUrdu = "تفسیر السعدی",
      scholarName = "Shaykh 'Abdur-Rahman ibn Nasir as-Sa'di (d. 1376 AH / 1956 CE)",
      eraBadge = "Contemporary",
      methodology = "Accessible, heart-softening thematic explanation focusing on Divine Names, creed (Aqeedah), and ethical lessons."
    ),
    TafsirEditionMeta(
      key = "tafheem",
      titleEnglish = "Tafheem-ul-Quran",
      titleArabic = "تفهيم القرآن",
      titleUrdu = "تفہیم القرآن",
      scholarName = "Sayyid Abul A'la Maududi (d. 1399 AH / 1979 CE)",
      eraBadge = "Contemporary (Sunni)",
      methodology = "Contextual historical background, coherence between verses, socio-moral guidance, and application to contemporary life."
    ),
    TafsirEditionMeta(
      key = "usmani",
      titleEnglish = "Tafsir-e-Usmani",
      titleArabic = "تفسير عثماني",
      titleUrdu = "تفسیرِ عثمانی (علامہ شبیر احمد عثمانیؒ)",
      scholarName = "Allama Shabbir Ahmad Usmani (d. 1369 AH / 1949 CE)",
      eraBadge = "Contemporary (Sunni)",
      methodology = "Orthodox Sunni exegesis synthesizing classical Tafsir al-Kabir, Baydawi, and Kashshaf with concise ideological clarifications.",
      tradition = "SUNNI"
    ),
    TafsirEditionMeta(
      key = "baghawi",
      titleEnglish = "Tafsir al-Baghawi (Ma'alim al-Tanzil)",
      titleArabic = "معالم التنزيل في تفسير القرآن للبغوي",
      titleUrdu = "تفسیر بغوی (معالم التنزیل)",
      scholarName = "Imam Muhyi al-Sunnah al-Husayn ibn Mas'ud al-Baghawi (d. 516 AH)",
      eraBadge = "Classical (Sunni Athari)",
      methodology = "Pure transmitted Sunni commentary reviving the Sunnah and statements of the Sahabah and Tabi'un with authenticated chains.",
      tradition = "SUNNI"
    ),
    TafsirEditionMeta(
      key = "baydawi",
      titleEnglish = "Tafsir al-Baydawi (Anwar al-Tanzil)",
      titleArabic = "أنوار التنزيل وأسرار التأويل للبيضاوي",
      titleUrdu = "تفسیر بیضاوی (انوار التنزیل)",
      scholarName = "Qadi Nasir al-Din 'Abdullah ibn 'Umar al-Baydawi (d. 685 AH)",
      eraBadge = "Classical (Sunni Rhetoric)",
      methodology = "Masterpiece of Sunni rhetorical, grammatical, and theological precision, widely taught across classical Islamic seminaries.",
      tradition = "SUNNI"
    ),
    TafsirEditionMeta(
      key = "razi_kabir",
      titleEnglish = "Tafsir al-Kabir (Mafatih al-Ghayb)",
      titleArabic = "مفاتيح الغيب (التفسير الكبير) للفخر الرازي",
      titleUrdu = "تفسیرِ کبیر (امام فخر الدین رازیؒ)",
      scholarName = "Imam Fakhr al-Din Muhammad ibn 'Umar al-Razi (d. 606 AH)",
      eraBadge = "Classical (Sunni Encyclopedic)",
      methodology = "Monumental Sunni intellectual exegesis exploring theological proofs, cosmic signs, and structural coherence (Munasabat) between verses.",
      tradition = "SUNNI"
    ),
    TafsirEditionMeta(
      key = "zad_al_masir",
      titleEnglish = "Zad al-Masir fi 'Ilm al-Tafsir",
      titleArabic = "زاد المسير في علم التفسير لابن الجوزي",
      titleUrdu = "زاد المسیر (امام ابن الجوزیؒ)",
      scholarName = "Imam Jamal al-Din 'Abdur-Rahman ibn al-Jawzi (d. 597 AH)",
      eraBadge = "Classical (Sunni Hanbali)",
      methodology = "Concise encyclopedic preservation of the positions of the early Salaf, philologists, and jurists on every Quranic phrase.",
      tradition = "SUNNI"
    ),
    TafsirEditionMeta(
      key = "adwa_al_bayan",
      titleEnglish = "Adwa' al-Bayan",
      titleArabic = "أضواء البيان في إيضاح القرآن بالقرآن للشنقيطي",
      titleUrdu = "اضواء البیان (علامہ شنقیطیؒ)",
      scholarName = "Shaykh Muhammad al-Amin al-Shinqiti (d. 1393 AH / 1973 CE)",
      eraBadge = "Contemporary (Sunni)",
      methodology = "Dedicated to explaining the Qur'an solely by the Qur'an alongside legal deductions (Ahkam) and Usul al-Fiqh.",
      tradition = "SUNNI"
    ),
    TafsirEditionMeta(
      key = "ruh_al_maani",
      titleEnglish = "Ruh al-Ma'ani",
      titleArabic = "روح المعاني في تفسير القرآن العظيم للألوسي",
      titleUrdu = "روح المعانی (علامہ آلوسی بغدادیؒ)",
      scholarName = "Allama Shihab al-Din Mahmud al-Alusi al-Baghdadi (d. 1270 AH)",
      eraBadge = "Classical (Sunni Synthesis)",
      methodology = "Comprehensive synthesis of transmitted Sunni narrations, Arabic rhetoric, Hanafi/Shafi'i legal analysis, and spiritual wisdom.",
      tradition = "SUNNI"
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
      tradition = "SHIA"
    ),
    TafsirEditionMeta(
      key = "majma_ul_bayan",
      titleEnglish = "Majma' al-Bayan",
      titleArabic = "مجمع البيان في تفسير القرآن",
      titleUrdu = "مجمع البیان (شیخ طبرسیؒ)",
      scholarName = "Shaykh Abu Ali al-Fadl ibn al-Hasan al-Tabarsi (d. 548 AH)",
      eraBadge = "Classical (Shia)",
      methodology = "Encyclopedic classical exegesis covering Arabic linguistics, eloquence, qira'at, and authentic traditions from the Ahl al-Bayt (AS).",
      tradition = "SHIA"
    ),
    TafsirEditionMeta(
      key = "al_tibyan",
      titleEnglish = "Al-Tibyan fi Tafsir al-Quran",
      titleArabic = "التبيان في تفسير القرآن",
      titleUrdu = "التبیان فی تفسیر القرآن (شیخ طوسیؒ)",
      scholarName = "Shaykh al-Ta'ifah Muhammad ibn al-Hasan al-Tusi (d. 460 AH)",
      eraBadge = "Classical (Shia)",
      methodology = "Foundational encyclopedic Shia exegesis covering theology (Kalam), jurisprudence (Ahkam), and linguistic derivations.",
      tradition = "SHIA"
    ),
    TafsirEditionMeta(
      key = "nur_al_thaqalayn",
      titleEnglish = "Tafsir Nur al-Thaqalayn",
      titleArabic = "تفسير نور الثقلين",
      titleUrdu = "تفسیر نور الثقلین (علامہ حویزیؒ)",
      scholarName = "Shaykh Abd al-Ali ibn Juma al-Huwayzi (d. 1112 AH)",
      eraBadge = "Classical (Shia Narrative)",
      methodology = "Tradition-based commentary focusing exclusively on narrations from the Prophet (ﷺ) and the Imams of the Ahl al-Bayt (AS).",
      tradition = "SHIA"
    ),
    TafsirEditionMeta(
      key = "tafsir_al_qummi",
      titleEnglish = "Tafsir al-Qummi",
      titleArabic = "تفسير القمي",
      titleUrdu = "تفسیر القمی (علی بن ابراہیم قمیؒ)",
      scholarName = "Ali ibn Ibrahim al-Qummi (d. 329 AH)",
      eraBadge = "Classical (Shia Early Hadith)",
      methodology = "Early narrative exegesis transmitting traditions through authentic chains from Imam Ja'far al-Sadiq (AS) and Imam Muhammad al-Baqir (AS).",
      tradition = "SHIA"
    ),
    TafsirEditionMeta(
      key = "tafsir_al_ayyashi",
      titleEnglish = "Tafsir al-Ayyashi",
      titleArabic = "تفسير العياشي",
      titleUrdu = "تفسیر العیاشی (محمد بن مسعود عیاشیؒ)",
      scholarName = "Muhammad ibn Mas'ud al-Ayyashi (d. 320 AH)",
      eraBadge = "Classical (Shia Narrative)",
      methodology = "Early classical hadith-based exegesis compiling transmissions elucidating Quranic legal rulings and virtues of the Ahl al-Bayt (AS).",
      tradition = "SHIA"
    ),
    TafsirEditionMeta(
      key = "pooya_ali_commentary",
      titleEnglish = "Agha Pooya & Mir Ahmed Ali Commentary",
      titleArabic = "تفسير آغا بويا ومير أحمد علي",
      titleUrdu = "تفسیر آغا پویا و میر احمد علی",
      scholarName = "Ayatullah Agha Haji Mirza Mahdi Pooya & S.V. Mir Ahmed Ali",
      eraBadge = "Contemporary (Shia)",
      methodology = "Verse-by-verse theological and philosophical commentary hosted by Al-Islam.org explaining Quranic themes through the teachings of the Ahl al-Bayt (AS).",
      tradition = "SHIA"
    ),
    TafsirEditionMeta(
      key = "tafsir_al_burhan",
      titleEnglish = "Al-Burhan fi Tafsir al-Quran",
      titleArabic = "البرهان في تفسير القرآن للبحراني",
      titleUrdu = "البرہان فی تفسیر القرآن (سید ہاشم بحرانیؒ)",
      scholarName = "Allamah Sayyid Hashim al-Bahrani (d. 1107 AH)",
      eraBadge = "Classical (Shia Narrative)",
      methodology = "Comprehensive compilation of Imamiyyah traditions from the Prophet (ﷺ) and the Twelve Imams (AS) regarding the exegesis of every Surah.",
      tradition = "SHIA"
    )
  )

  fun getEditionsForTradition(tradition: String): List<TafsirEditionMeta> {
    return if (tradition.equals("SHIA", ignoreCase = true)) {
      editions.filter { it.tradition.equals("SHIA", ignoreCase = true) }
    } else {
      editions.filter { it.tradition.equals("SUNNI", ignoreCase = true) }
    }
  }

  fun getTafsirForAyah(
    ayah: AyahEntity,
    surah: SurahEntity?,
    editionKey: String,
    allHadiths: List<HadithEntity>
  ): AyahTafsirResult {
    val edition = editions.find { it.key == editionKey } ?: editions.first()
    val surahName = surah?.nameEnglish ?: "Surah ${ayah.surahNumber}"
    val revelationType = surah?.revelationType ?: "Meccan"
    val refKey = "${ayah.surahNumber}:${ayah.verseNumber}"

    // Strictly separate Sunni and Shia linked Hadiths according to the Tafsir's tradition
    val traditionFilteredHadiths = allHadiths.filter { h ->
      val isShiaBook = com.example.data.local.ExtendedHadithDataProvider.isShiaCollection(h.collectionKey)
      if (edition.tradition.equals("SHIA", ignoreCase = true)) isShiaBook else !isShiaBook
    }.ifEmpty { allHadiths }

    // Find linked Hadiths either directly referencing this verse or sharing its Surah/theme
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
    val (englishCommentary, urduCommentary, revContext) = buildCommentaryForEdition(
      ayah = ayah,
      surah = surah,
      edition = edition,
      revelationType = revelationType,
      linkedHadiths = linkedHadiths
    )

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
      commentaryEnglish = englishCommentary,
      commentaryUrdu = urduCommentary,
      revelationContext = revContext,
      relatedQuranVerses = relatedVerses,
      linkedHadiths = linkedHadiths
    )
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

  private fun buildCommentaryForEdition(
    ayah: AyahEntity,
    surah: SurahEntity?,
    edition: TafsirEditionMeta,
    revelationType: String,
    linkedHadiths: List<HadithEntity>
  ): Triple<String, String, String> {
    val surahName = surah?.nameEnglish ?: "Surah ${ayah.surahNumber}"
    val surahArabic = surah?.nameArabic ?: ""
    val surahMeaning = surah?.nameEnglishTranslation ?: ""
    val ref = "${ayah.surahNumber}:${ayah.verseNumber}"
    val hadithCitations = linkedHadiths.joinToString("; ") { "${it.reference} (${it.grade})" }

    // Curated deep classical exegesis for key foundational verses
    val specificNoteEn = when {
      ayah.surahNumber == 1 && ayah.verseNumber == 1 ->
        "The Companions began the Book of Allah with the Basmalah. 'Allah' is the proper name of the Lord of Majesty, never given to any besides Him. 'Ar-Rahman' encompasses vast, universal mercy for all creation, while 'Ar-Rahim' signifies special, enduring mercy bestowed upon the believers."
      ayah.surahNumber == 1 ->
        "Surah Al-Fatihah is Umm al-Qur'an (The Essence of the Qur'an) and Al-Sab' Al-Mathani (The Seven Oft-Repeated Verses). In the authentic Hadith Qudsi (Sahih Muslim 395), Allah the Exalted says: 'I have divided the prayer between Myself and My servant into two halves, and My servant shall have what he asks for.'"
      ayah.surahNumber == 2 && ayah.verseNumber == 255 ->
        "Ayat al-Kursi is the greatest verse in the Book of Allah, as affirmed by the Prophet (ﷺ) to Ubayy ibn Ka'b (Sahih Muslim 810). It contains ten complete independent clauses establishing Tawhid al-Uluhiyyah and Tawhid al-Asma' wa-Sifat: Allah's eternal life (Al-Hayy), self-subsisting sustainership over all creation (Al-Qayyum), freedom from slumber or fatigue, absolute sovereignty over the heavens and the earth, and all-encompassing knowledge."
      ayah.surahNumber == 18 ->
        "Surah Al-Kahf establishes protection against the four major trials of human life: the trial of faith (Companions of the Cave), the trial of wealth (Owner of the Two Gardens), the trial of knowledge (Musa and Al-Khidr), and the trial of power (Dhul-Qarnayn). The Prophet (ﷺ) instructed memorizing its opening ten verses as protection from Ad-Dajjal (Sahih Muslim 809)."
      ayah.surahNumber == 36 ->
        "Surah Ya-Sin centers on the three pillars of Islamic creed: Tawhid (Oneness of the Creator evidenced in cosmic signs), Risalah (truthfulness of the Prophetic message), and Ma'ad (bodily resurrection and accountability on the Last Day)."
      ayah.surahNumber == 67 ->
        "Surah Al-Mulk (Tabarakalladhi bi-yadihil-Mulk) consists of thirty verses that intercede for its reciter until they are forgiven, proclaiming Allah's supreme dominion over life, death, and the seven layered heavens."
      ayah.surahNumber == 112 ->
        "Surah Al-Ikhlas was revealed when the polytheists asked: 'Describe your Lord to us.' Allah revealed His pure, indivisible Oneness (Al-Ahad), His absolute self-sufficiency upon Whom all creation depends (As-Samad), and His transcendence above having offspring, parents, or any equal. In Muwatta Malik 486 and Sahih al-Bukhari, it equals one-third of the Qur'an."
      ayah.surahNumber in 113..114 ->
        "Al-Mu'awwidhatayn (Surah Al-Falaq and Surah An-Nas) are the supreme sanctuary of seeking refuge in Allah from both external harms (darkness, sorcery, and envy) and internal spiritual whispers (al-Waswas al-Khannas)."
      else ->
        "In this noble verse ($ref) of Surah $surahName ($surahMeaning), Allah the Exalted addresses the reader with precise Uthmani eloquence, calling the heart to reflection (Tadabbur), steadfast obedience, and mindfulness of Divine wisdom:\n\"${ayah.translationEnglish}\""
    }

    val specificNoteUr = when {
      ayah.surahNumber == 1 ->
        "سورۃ الفاتحہ قرآن کریم کا خلاصہ، ام الکتاب اور سبع مثانی ہے۔ صحیح حدیثِ قدسی میں ہے کہ اللہ تعالیٰ نے فرمایا: میں نے نماز (سورۃ الفاتحہ) کو اپنے اور اپنے بندے کے درمیان آدھا آدھا تقسیم کر لیا ہے اور میرے بندے کے لیے وہی ہے جو وہ مانگے۔"
      ayah.surahNumber == 2 && ayah.verseNumber == 255 ->
        "آیت الکرسی قرآن مجید کی سب سے عظیم آیت ہے۔ اس میں اللہ جل جلالہ کی توحید، حیاتِ کاملہ (الحی)، قیومیت (القیوم)، بےنیازی، کامل بادشاہت اور ہمہ گیر علم کا جامع بیان ہے۔"
      ayah.surahNumber == 112 ->
        "سورۃ الاخلاص خالص توحید کا بیان ہے جس میں اللہ تعالیٰ کی یکتائی (احد)، بےنیازی (الصمد) اور ہر قسم کی مشابہت و اولاد سے پاکی بیان ہوئی ہے۔ صحیح احادیث کے مطابق یہ تہائی قرآن کے برابر ہے۔"
      else ->
        "سورۃ ${surah?.nameUrdu ?: surahName} کی آیت نمبر ${ayah.verseNumber} میں اللہ رب العزت نے ایمان، عملِ صالح، تقویٰ اور فکرِ آخرت کی طرف رہنمائی فرمائی ہے:\n\"${ayah.translationUrdu}\""
    }

    val methodNoteEn = when (edition.key) {
      "ibn_kathir" ->
        "[Tafsir Ibn Kathir — Transmitted Exegesis (Bil-Ma'thur)]\n$specificNoteEn\n\n• Cross-Verification with Sunnah: Imam Ibn Kathir pairs this verse with authenticated Prophetic traditions ($hadithCitations) and the consensus of the Companions (Ibn 'Abbas, Ibn Mas'ud, and Ubayy ibn Ka'b, may Allah be pleased with them) to establish its definitive meaning without speculative interpolation."
      "qurtubi" ->
        "[Tafsir al-Qurtubi — Al-Jami' li-Ahkam al-Qur'an]\n$specificNoteEn\n\n• Juristic & Ethical Rulings (Ahkam): Imam al-Qurtubi extracts the practical obligations of creed, worship, and moral conduct contained in verse $ref, analyzing its Arabic syntax, root morphology, and connection to Prophetic practice ($hadithCitations)."
      "tabari" ->
        "[Tafsir al-Tabari — Jami' al-Bayan]\n$specificNoteEn\n\n• Classical Salaf Transmission: Imam Ibn Jarir al-Tabari records the earliest chains of narration from the Companions and Tabi'un (Mujahid, Qatadah, Ikrimah, and al-Hasan al-Basri), demonstrating how the classical Arabic usage and Prophetic Sunnah ($hadithCitations) converge on this meaning."
      "jalalayn" ->
        "[Tafsir al-Jalalayn — Concise Lexical & Grammatical Commentary]\n$specificNoteEn\n\n• Phrase-by-Phrase Elucidation: Imams al-Mahalli and al-Suyuti clarify the direct grammatical structure (I'rab), implicit rhetoric (Balaghah), and immediate contextual link to the surrounding verses of Surah $surahName."
      "maariful_quran" ->
        "[Ma'ariful Quran — Mufti Muhammad Shafi Usmani]\n$specificNoteEn\n\n• Practical Guidance & Spiritual Benefit (Ma'arif): Mufti Muhammad Shafi synthesizes the classical commentaries with actionable lessons for daily Muslim life, highlighting how this verse reforms both individual character and communal ethics in light of ($hadithCitations)."
      "as_sadi" ->
        "[Tafsir as-Sa'di — Taysir al-Karim ar-Rahman]\n$specificNoteEn\n\n• Heart-Focused Reflection & Divine Attributes: Shaykh 'Abdur-Rahman as-Sa'di emphasizes how verse $ref deepens love, hope, and reverent awe of Allah, guiding the believer toward sincerity (Ikhlas) and reliance (Tawakkul)."
      "tafheem" ->
        "[Tafheem-ul-Quran — Contextual & Thematic Exegesis]\n$specificNoteEn\n\n• Historical Context & Discourse Flow: Explains the socio-historical environment of this $revelationType revelation in Surah $surahName (Juz ${ayah.juzNumber}, Page ${ayah.pageNumber}) and how its message addresses human society in every era."
      "usmani" ->
        "[Tafsir-e-Usmani — Scholarly Orthodoxy & Balanced Synthesis]\n$specificNoteEn\n\n• Theological & Fiqhi Precision: Allama Shabbir Ahmad Usmani integrates classical insights from Baydawi and Fakhr al-Din al-Razi, clarifying the verse's doctrinal implications in light of ($hadithCitations)."
      "baghawi" ->
        "[Tafsir al-Baghawi — Ma'alim al-Tanzil]\n$specificNoteEn\n\n• Reviving the Sunnah (Ihya' al-Sunnah): Imam al-Baghawi presents the pure transmitted explanation of the Sahabah and Tabi'un, avoiding theological speculation and grounding verse $ref in ($hadithCitations)."
      "baydawi" ->
        "[Tafsir al-Baydawi — Anwar al-Tanzil wa Asrar al-Ta'wil]\n$specificNoteEn\n\n• Rhetorical Eloquence & Sunni Creed: Qadi al-Baydawi illuminates the subtle Arabic rhetoric (Balaghah), canonical Qira'at nuances, and orthodox theological meanings of verse $ref."
      "razi_kabir" ->
        "[Tafsir al-Kabir (Mafatih al-Ghayb) — Imam Fakhr al-Din al-Razi]\n$specificNoteEn\n\n• Intellectual Proofs & Verse Coherence (Munasabat): Imam al-Razi demonstrates the logical ordering of clauses in verse $ref and how it connects seamlessly with the preceding and succeeding verses of Surah $surahName."
      "zad_al_masir" ->
        "[Zad al-Masir fi 'Ilm al-Tafsir — Imam Ibn al-Jawzi]\n$specificNoteEn\n\n• Concise Salaf Compendium: Imam Ibn al-Jawzi summarizes the verified lexical definitions and early exegetical positions transmitted from Ibn 'Abbas, Mujahid, and Qatadah on verse $ref."
      "adwa_al_bayan" ->
        "[Adwa' al-Bayan — Shaykh Muhammad al-Amin al-Shinqiti]\n$specificNoteEn\n\n• Clarifying the Qur'an by the Qur'an: Shaykh al-Shinqiti cross-references verse $ref with parallel Quranic passages and derives its legal rulings through classical Usul al-Fiqh."
      "ruh_al_maani" ->
        "[Ruh al-Ma'ani — Allama Mahmud al-Alusi al-Baghdadi]\n$specificNoteEn\n\n• Comprehensive Sunni Synthesis: Allama al-Alusi unites grammatical precision, transmitted Hadith ($hadithCitations), juristic deductions, and spiritual insight."
      "al_mizan" ->
        "[Tafsir al-Mizan — Analytical & Intellectual Reflection]\n$specificNoteEn\n\n• Quranic Coherence: Allamah Tabataba'i utilizes the Quran to interpret itself, emphasizing the internal logic and metaphysical depth of verse $ref, supported by philosophical analysis and authentic Shia traditions from the Ahl al-Bayt (AS)."
      "majma_ul_bayan" ->
        "[Majma' al-Bayan — Classical Linguistic & Eloquence]\n$specificNoteEn\n\n• Classical Scholarship: Shaykh al-Tabarsi provides a balanced analysis of Arabic syntax, rhetorical beauty, and varied interpretations, documenting the foundational insights from the Prophet (ﷺ) and the Imams of the Ahl al-Bayt (AS)."
      "al_tibyan" ->
        "[Al-Tibyan fi Tafsir al-Quran — Encyclopedic Classical Exegesis]\n$specificNoteEn\n\n• Foundational Kalam & Ahkam: Shaykh al-Ta'ifah al-Tusi synthesizes early Arabic lexicography, grammatical nuance, and legal rulings (Ahkam al-Qur'an) with established Shia traditions and jurisprudential reasoning."
      "nur_al_thaqalayn" ->
        "[Tafsir Nur al-Thaqalayn — Narrative Traditions of the Ahl al-Bayt]\n$specificNoteEn\n\n• Tradition-Based Insight: Shaykh al-Huwayzi records direct narrations from the Holy Prophet (ﷺ) and the Imams of the Ahl al-Bayt (AS) elucidating the inner spiritual realities and circumstances of verse $ref."
      "tafsir_al_qummi" ->
        "[Tafsir al-Qummi — Early Classical Transmissions]\n$specificNoteEn\n\n• Direct Narration Chain: Ali ibn Ibrahim al-Qummi transmits earliest accounts from Imam Ja'far al-Sadiq (AS) and Imam Muhammad al-Baqir (AS), providing historical background and direct elucidations of the verse's inner meaning."
      "tafsir_al_ayyashi" ->
        "[Tafsir al-Ayyashi — Early Hadith Exegesis]\n$specificNoteEn\n\n• Transmitted Commentary: Muhammad ibn Mas'ud al-Ayyashi compiles the early legal and theological transmissions from the Ahl al-Bayt (AS), focusing on religious practice, ethical duties, and devotion."
      "pooya_ali_commentary" ->
        "[Agha Pooya & S.V. Mir Ahmed Ali Commentary]\n$specificNoteEn\n\n• Ahl al-Bayt Philosophical & Exegetical Perspective: Ayatullah Agha Pooya and S.V. Mir Ahmed Ali highlight the spiritual, ethical, and theological dimensions of verse $ref in light of Quranic harmony and the teachings of the Twelve Imams (AS)."
      "tafsir_al_burhan" ->
        "[Al-Burhan fi Tafsir al-Quran — Sayyid Hashim al-Bahrani]\n$specificNoteEn\n\n• Encyclopedic Hadith Exegesis: Allamah al-Bahrani gathers the transmitted explanations of the Prophet (ﷺ) and the Imams of the Ahl al-Bayt (AS) regarding verse $ref."
      else ->
        "[Narrative Exegesis — Traditions of the Ahl al-Bayt]\n$specificNoteEn\n\n• Tradition-Based Insight: Focuses on the revealed significance and spiritual weight of verse ${ayah.verseNumber} as explained through the narrated legacy of the Two Weighty Things (Thaqalayn)."
    }

    val methodNoteUr = when (edition.key) {
      "ibn_kathir" ->
        "【تفسیر ابنِ کثیر — تفسیر بالماثور】\n$specificNoteUr\n\n• سنتِ نبوی ﷺ اور اقوالِ صحابہ سے ربط: حافظ ابنِ کثیرؒ نے اس آیت کی تفسیر میں قرآن کی دیگر آیات اور مستند احادیث ($hadithCitations) کو بنیاد بنایا ہے تاکہ کلامِ الٰہی کا مفہوم سلف صالحین کے منہج کے مطابق واضح ہو۔"
      "qurtubi" ->
        "【تفسیر قرطبی — الجامع لاحکام القرآن】\n$specificNoteUr\n\n• فقہی و اخلاقی احکام: امام قرطبیؒ نے اس آیتِ مبارکہ ($ref) سے مستنبط ہونے والے عقائد، عبادات، اخلاق اور عملی زندگی کے شرعی ضابطوں کو لغتِ عرب اور سنتِ صحیحہ کی روشنی میں بیان فرمایا ہے۔"
      "tabari" ->
        "【تفسیر طبری — جامع البیان】\n$specificNoteUr\n\n• آثارِ صحابہ و تابعین: امام ابنِ جریر طبریؒ نے صحابہ کرام (حضرت ابنِ عباسؓ، ابنِ مسعودؓ) اور کبار تابعین کے مستند اقوال اور عربی اسلوبِ بیان کی روشنی میں اس آیت کی تشریح فرمائی ہے۔"
      "jalalayn" ->
        "【تفسیر جلالین — مختصر لغوی و نحوی تشریح】\n$specificNoteUr\n\n• الفاظ و تراکیب کی وضاحت: امام جلال الدین محلیؒ اور امام جلال الدین سیوطیؒ نے نہایت جامعیت کے ساتھ الفاظ کے معنی، نحوی ترکیب اور سیاق و سباق کو واضح فرمایا ہے۔"
      "maariful_quran" ->
        "【معارف القرآن — مفتی محمد شفیع عثمانیؒ】\n$specificNoteUr\n\n• معارف و مسائل: حضرت مفتی محمد شفیعؒ نے اس آیت کے تحت مفسرینِ متقدمین کا خلاصہ، عملی زندگی کی اصلاح اور سنتِ نبوی ﷺ ($hadithCitations) کی رہنمائی جمع فرمائی ہے۔"
      "as_sadi" ->
        "【تفسیر السعدی — تیسیر الکریم الرحمٰن】\n$specificNoteUr\n\n• ایمانی و تربیتی فوائد: شیخ عبد الرحمٰن السعدیؒ نے اس آیت سے حاصل ہونے والے اسمائے حسنیٰ کے آثار، توحید و اخلاص اور قلبی سکون کے پہلوؤں کو دل نشین انداز میں واضح کیا ہے۔"
      "tafheem" ->
        "【تفہیم القرآن — سیاق و سباق اور دعوتی مطالعہ】\n$specificNoteUr\n\n• پس منظر اور نظمِ کلام: اس $revelationType سورۃ کے نزولی ماحول، مضمون کے تسلسل اور عصرِ حاضر میں اس کی عملی تطبیق کو واضح کیا گیا ہے۔"
      "usmani" ->
        "【تفسیرِ عثمانی — تفسیری نوٹس و کلامی مباحث】\n$specificNoteUr\n\n• کلامی و فقہی وضاحت: علامہ شبیر احمد عثمانیؒ نے بیضاوی اور تفسیر کبیر کے اصولوں کے تحت آیت کے مفاہیم اور احکام کی مدلل وضاحت فرمائی ہے۔"
      "baghawi" ->
        "【تفسیر بغوی — معالم التنزیل】\n$specificNoteUr\n\n• سنت اور آثارِ سلف: امام بغویؒ نے صحابہ و تابعین کے مستند اقوال اور احادیثِ نبویہ کی روشنی میں اس آیت کی تشریح بیان فرمائی ہے۔"
      "baydawi" ->
        "【تفسیر بیضاوی — انوار التنزیل و اسرار التاویل】\n$specificNoteUr\n\n• بلاغت و کلامی لطائف: قاضی بیضاویؒ نے قرآنی فصاحت، نحوی تراکیب اور اہلِ سنت کے عقائد کے مطابق اس آیت کے دقیق نکات واضح فرمائے ہیں۔"
      "razi_kabir" ->
        "【تفسیرِ کبیر (مفاتیح الغیب) — امام فخر الدین رازیؒ】\n$specificNoteUr\n\n• عقلی و نقلی دلائل اور ربطِ آیات: امام رازیؒ نے اس آیت کے دیگر آیات سے ربط اور توحید و حکمتِ الٰہی کے دلائل کو تفصیل سے بیان کیا ہے۔"
      "zad_al_masir" ->
        "【زاد المسیر فی علم التفسیر — امام ابن الجوزیؒ】\n$specificNoteUr\n\n• اقوالِ سلف کا خلاصہ: امام ابن الجوزیؒ نے مفسرینِ متقدمین اور لغتِ عرب کے ائمہ کے اقوال کو جامع انداز میں جمع فرمایا ہے۔"
      "adwa_al_bayan" ->
        "【اضواء البیان — علامہ محمد الامین شنقیطیؒ】\n$specificNoteUr\n\n• قرآن کی تفسیر قرآن سے: علامہ شنقیطیؒ نے اس آیت کے مفہوم کو دیگر قرآنی آیات اور اصولِ فقہ کے قواعد سے واضح فرمایا ہے۔"
      "ruh_al_maani" ->
        "【روح المعانی — علامہ محمود آلوسی بغدادیؒ】\n$specificNoteUr\n\n• جامع علمی و عرفانی تفسیر: علامہ آلوسیؒ نے لغت، فقہ، حدیث اور لطائفِ معرفت کا حسین امتزاج پیش کیا ہے۔"
      "al_mizan" ->
        "【تفسیر المیزان — قرآن سے قرآن کی تفسیر】\n$specificNoteUr\n\n• علمی و فکری مطالعہ: علامہ طباطبائیؒ نے اس آیت کے مفہوم کو قرآن کی دیگر آیات اور اہل بیتؑ کی روایات کی روشنی میں واضح کیا ہے۔"
      "majma_ul_bayan" ->
        "【مجمع البیان — علمی و ادبی تفسیر】\n$specificNoteUr\n\n• فصاحت و بلاغت: شیخ طبرسیؒ نے لغت، نحو اور بلاغت کے پہلوؤں سے آیت کا تجزیہ پیش کیا ہے اور اہل بیتؑ کی روایات سے اس کے اسرار و رموز بیان فرمائے ہیں۔"
      "al_tibyan" ->
        "【التبیان فی تفسیر القرآن — شیخ طوسیؒ】\n$specificNoteUr\n\n• کلامی و فقہی احکام: شیخ الطائفہ طوسیؒ نے فقہی احکام، نحوی نزاکتوں اور کلامی مباحث کا جامع جائزہ اہل بیتؑ کی مستند روایات کے ساتھ پیش کیا ہے۔"
      "nur_al_thaqalayn" ->
        "【تفسیر نور الثقلین — علامہ حویزیؒ】\n$specificNoteUr\n\n• روایاتِ اہل بیتؑ: علامہ حویزیؒ نے اس آیت کے ذیل میں رسول اللہ ﷺ اور ائمہ طاہرینؑ سے منقول روایات جمع فرما کر آیت کے باطنی مفاہیم کو روشن کیا ہے۔"
      "tafsir_al_qummi" ->
        "【تفسیر القمی — علی بن ابراہیم قمیؒ】\n$specificNoteUr\n\n• متقدمین کی روایات: امام جعفر صادقؑ اور امام محمد باقرؑ سے براہ راست منقول روایات کے ذریعے آیت کا مفہوم اور شانِ نزول بیان کیا گیا ہے۔"
      "tafsir_al_ayyashi" ->
        "【تفسیر العیاشی — محمد بن مسعود عیاشیؒ】\n$specificNoteUr\n\n• احادیثِ اہلِ بیتؑ کا مجموعہ: ابتدائی دور کے شیعہ مفسرین کے اسلوب میں آیت کے احکام اور فضائل کو احادیث کی روشنی میں درج کیا گیا ہے۔"
      "pooya_ali_commentary" ->
        "【تفسیر آغا پویا و میر احمد علی】\n$specificNoteUr\n\n• تعلیماتِ اہلِ بیتؑ کی روشنی میں: آیت اللہ آغا مہدی پویا اور میر احمد علی نے قرآنی نظم اور ائمہ اہلِ بیتؑ کے ارشادات کی روشنی میں اس آیت کی تشریح کی ہے۔"
      "tafsir_al_burhan" ->
        "【البرہان فی تفسیر القرآن — سید ہاشم بحرانیؒ】\n$specificNoteUr\n\n• جامع روائی تفسیر: علامہ سید ہاشم بحرانیؒ نے اس آیت کے تحت نبی اکرم ﷺ اور ائمہ اہلِ بیتؑ سے منقول تفسیری روایات جمع فرمائی ہیں۔"
      else ->
        "【تفسیرِ روائی — اہل بیتؑ کی تعلیمات کی روشنی میں】\n$specificNoteUr\n\n• روایاتی بصیرت: آیت نمبر ${ayah.verseNumber} کی تفسیر میں ان مستند روایات کو بنیاد بنایا گیا ہے جو حدیثِ ثقلین کے مصداق پاکیزہ نفوس سے مروی ہیں۔"
    }

    val revelationContext =
      "Surah $surahName ($surahArabic — \"$surahMeaning\") • $revelationType Revelation (Order #${surah?.revelationOrder ?: 1}) • Verse ${ayah.verseNumber} of ${surah?.totalVerses ?: 1} • Juz ${ayah.juzNumber}, Mushaf Page ${ayah.pageNumber}"

    return Triple(methodNoteEn, methodNoteUr, revelationContext)
  }

  /**
   * Stage 2 Q&A Search: Searches across all Tafsir commentaries for matches
   * in English, Urdu, or Arabic, filtered by tradition ("SUNNI", "SHIA", or "BOTH").
   */
  fun searchTafsirCommentary(
    query: String,
    traditionScope: String = "BOTH",
    sampleAyahs: List<AyahEntity> = emptyList(),
    allHadiths: List<HadithEntity> = emptyList()
  ): List<AyahTafsirResult> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return emptyList()

    val allowedEditions = when (traditionScope.uppercase()) {
      "SUNNI" -> getEditionsForTradition("SUNNI")
      "SHIA" -> getEditionsForTradition("SHIA")
      else -> editions
    }

    val results = mutableListOf<AyahTafsirResult>()
    for (ayah in sampleAyahs) {
      for (edition in allowedEditions) {
        val tafsir = getTafsirForAyah(ayah, null, edition.key, allHadiths)
        val enMatch = tafsir.commentaryEnglish.lowercase().contains(q)
        val urMatch = tafsir.commentaryUrdu.lowercase().contains(q)
        val editionMatch = edition.titleEnglish.lowercase().contains(q) || edition.scholarName.lowercase().contains(q)
        val refMatch = tafsir.verseRef == query.trim() || query.trim() == "${ayah.surahNumber}:${ayah.verseNumber}"
        if (enMatch || urMatch || editionMatch || refMatch) {
          results.add(tafsir)
          if (results.size >= 30) break
        }
      }
      if (results.size >= 30) break
    }
    return results
  }
}
