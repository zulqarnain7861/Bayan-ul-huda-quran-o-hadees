package com.example.data.model

/**
 * Verified source & license registry for the Elementary Islamic Learning ("Learn Islam") section,
 * Quran translations/audio, Duas, and Hadith references.
 *
 * Every source URL requested in the specification was inspected for API availability,
 * audio/content reuse permissions, and edition structure.
 */
data class VerifiedLearningSourceInfo(
    val id: String,
    val name: String,
    val url: String,
    val category: String,
    val apiAndReuseStatus: String,
    val licenseAndAttributionNote: String,
    val integrationMethodInApp: String
)

data class QaidaPracticeItem(
    val id: String,
    val arabicText: String,
    val transliteration: String,
    val makhrajOrRuleEnglish: String,
    val makhrajOrRuleUrdu: String,
    val pronunciationTip: String,
    val quranSurahAyah: Pair<Int, Int>? = null
)

data class QaidaQuizQuestion(
    val id: String,
    val questionEnglish: String,
    val questionUrdu: String,
    val arabicPrompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanationEnglish: String,
    val explanationUrdu: String
)

data class NooraniQaidaLesson(
    val lessonNumber: Int,
    val id: String,
    val titleArabic: String,
    val titleUrdu: String,
    val titleEnglish: String,
    val subtitle: String,
    val ruleSummaryEnglish: String,
    val ruleSummaryUrdu: String,
    val editionNote: String,
    val practiceItems: List<QaidaPracticeItem>,
    val quizQuestions: List<QaidaQuizQuestion>
)

data class ScholarlyDifferenceEntry(
    val schoolOrTradition: String,
    val positionSummaryEnglish: String,
    val positionSummaryUrdu: String,
    val primarySourceReference: String
)

data class ImanAndKalimaItem(
    val id: String,
    val orderNumber: Int,
    val category: String, // "SIX_KALIMAS", "IMAN_MUJMAL_MUFASSAL", "PILLARS_AND_ARTICLES"
    val titleArabic: String,
    val titleUrdu: String,
    val titleEnglish: String,
    val arabicText: String,
    val transliteration: String,
    val urduTranslation: String,
    val englishTranslation: String,
    val authenticReference: String,
    val scholarlyAndCurriculumNote: String,
    val humanAudioUrl: String? = null
)

data class WuduAndSalahStepItem(
    val id: String,
    val stepOrder: Int,
    val section: String, // "WUDU", "SALAH_PREREQUISITES", "NAMAZ_MUTARJAM", "SHORT_SURAHS"
    val titleArabic: String,
    val titleUrdu: String,
    val titleEnglish: String,
    val rulingCategory: String, // Fard, Wajib, Sunnah Mu'akkadah, Mustahabb
    val arabicRecitation: String,
    val transliteration: String,
    val urduMeaning: String,
    val englishMeaning: String,
    val methodExplanationEnglish: String,
    val methodExplanationUrdu: String,
    val hadithOrFiqhReference: String,
    val quranSurahNumber: Int? = null,
    val quranAyahRange: IntRange? = null,
    val scholarlyDifferences: List<ScholarlyDifferenceEntry> = emptyList(),
    val humanAudioUrl: String? = null
)

data class SehriNiyatGuidanceData(
    val titleEnglish: String,
    val titleUrdu: String,
    val titleArabic: String,
    val heartIntentionPrincipleEnglish: String,
    val heartIntentionPrincipleUrdu: String,
    val propheticHadithReferences: List<String>,
    val customaryFormulas: List<CustomaryNiyatFormula>,
    val scholarlyViews: List<ScholarlyDifferenceEntry>,
    val criticalVerificationNotice: String
)

data class CustomaryNiyatFormula(
    val id: String,
    val labelEnglish: String,
    val labelUrdu: String,
    val arabicText: String,
    val transliteration: String,
    val urduTranslation: String,
    val englishTranslation: String,
    val classificationBadge: String,
    val attributionAndStatusNote: String
)

data class DailyDuaSequenceEntry(
    val sequenceOrder: Int,
    val timeOfDayStage: String,
    val stageUrdu: String,
    val duaId: String,
    val titleEnglish: String,
    val titleUrdu: String,
    val arabicText: String,
    val transliteration: String,
    val AlternateTransliteration: String = "",
    val englishTranslation: String,
    val urduTranslation: String,
    val authenticReference: String,
    val verificationNote: String,
    val quranSurahAyah: Pair<Int, Int>? = null,
    val humanAudioUrl: String? = null
)

object LearnIslamCurriculumData {

    val verifiedSourceRegistry: List<VerifiedLearningSourceInfo> = listOf(
        VerifiedLearningSourceInfo(
            id = "source_noorani_17",
            name = "Classical Noorani Qaida (Maktaba Haqqaniya / Sheikh Noor Muhammad Haqqani Ludhianvi, 1856–1925 CE)",
            url = "https://taqwafoundation.com/resources/noorani-qaida-online/",
            category = "Noorani Qaida Curriculum (17 Lessons)",
            apiAndReuseStatus = "Classical public-domain text & letter progression (authored by Sheikh Noor Muhammad Haqqani, Ludhiana). Website HTML/PDF wrappers on taqwafoundation.com do not expose a public JSON API.",
            licenseAndAttributionNote = "Original 17-lesson Arabic primer is public-domain heritage across South Asian Darasgah/Madrasah curricula. Proprietary website audio recordings are NOT scraped or redistributed.",
            integrationMethodInApp = "All 17 classical lessons (Huroof-e-Mufradat through Tajweed & Quran practice) structured natively with canonical Quran.com / EveryAyah human reciter audio for verified Quranic verses and words (no AI/TTS voice)."
        ),
        VerifiedLearningSourceInfo(
            id = "source_learnnooraniqaida_com",
            name = "LearnNooraniQaida.com & Noorani-Qaida.com (Curriculum Investigation)",
            url = "https://learnnooraniqaida.com/noorani-qaida/ • https://noorani-qaida.com/",
            category = "Noorani Qaida Web Editions Comparison",
            apiAndReuseStatus = "Investigated: Neither learnnooraniqaida.com nor noorani-qaida.com provides an open public REST API or redistributable audio license. Note on edition structure: noorani-qaida.com splits compound exercises and Madd/Waqqf rules into 21 web lessons, whereas the standard printed Haqqani/Madani Darasgah edition has 17 lessons.",
            licenseAndAttributionNote = "Third-party website audio files are copyright-protected and not embedded. Edition differences (17 classic lessons vs. 21 expanded web sub-lessons) are documented on each corresponding lesson.",
            integrationMethodInApp = "Cross-referenced lesson scope so every topic in both the 17-lesson classic edition and 21-lesson expanded breakdown (Makharij, Murakkabat, Muqatta'at, Harakat, Tanween, Khari Harakat, Maddah, Leen, Sukoon/Qalqalah, Shaddah, Idgham/Ikhfa/Izhar/Iqlab, Waqf, and Salah/Quran reading) is covered without copyright infringement."
        ),
        VerifiedLearningSourceInfo(
            id = "source_quran_com_v4",
            name = "Quran.com / Quran Foundation API v4 & QUL (Quranic Universal Library)",
            url = "https://api-docs.quran.com/ • https://api.quran.com/api/v4",
            category = "Quran Text, Multi-Translations, Tafsir & Audio",
            apiAndReuseStatus = "Verified Public REST API (GET /api/v4/resources/translations, /verses/by_chapter, /chapter_recitations) + offline QUL catalog.",
            licenseAndAttributionNote = "Open educational API provided by Quran.Foundation / Tarteel QUL with explicit translator attribution.",
            integrationMethodInApp = "Used for canonical Uthmani Quran text, multi-translator verse translations beneath ayahs, and authentic reciter audio in Quran & Namaz short surahs."
        ),
        VerifiedLearningSourceInfo(
            id = "source_qud_timing",
            name = "QUD-Technologies / quranic-universal-audio",
            url = "https://github.com/QUD-Technologies/quranic-universal-audio",
            category = "Quran Word & Ayah Audio Timing Reference",
            apiAndReuseStatus = "Verified Open-Source GitHub Repository providing verse/word timing metadata and reciter audio structures.",
            licenseAndAttributionNote = "Open-source reference metadata; used alongside EveryAyah and Quran.com v4 word-by-word timing intervals.",
            integrationMethodInApp = "Supports synchronized verse/word highlighting during Quran and Namaz Surah recitation playback."
        ),
        VerifiedLearningSourceInfo(
            id = "source_hisnul_muslim",
            name = "Hisn al-Muslim (Fortress of the Muslim — Sa'id ibn 'Ali ibn Wahf al-Qahtani)",
            url = "https://www.hisnulmuslim.app/",
            category = "Authentic Daily Duas & Adhkar",
            apiAndReuseStatus = "Verified canonical compilation of authentic Prophetic supplications with Hadith citations.",
            licenseAndAttributionNote = "Classical scholarly compilation widely permitted for non-commercial Islamic education with original Hadith references preserved.",
            integrationMethodInApp = "All daily duas in chronological sequence (Waking up, Wudu, Masjid, Eating, Drinking, Iftari, Travel, Sleeping) include full Arabic, transliteration, Urdu, English, and Hadith grading/book numbers."
        ),
        VerifiedLearningSourceInfo(
            id = "source_sunnah_com_islamqa",
            name = "Sunnah.com Canonical Hadith Corpus & IslamQA Fiqh Verification",
            url = "https://sunnah.com/ • https://islamqa.info/en/answers/37805",
            category = "Hadith Verification & Fasting Intention (Sehri Niyat) Fiqh",
            apiAndReuseStatus = "Verified canonical Hadith numbering (Sahih al-Bukhari, Sahih Muslim, Sunan Abi Dawud 2357 & 2454, Sunan an-Nasa'i 2331, Jami' at-Tirmidhi).",
            licenseAndAttributionNote = "Direct scholarly citations with book and hadith number; distinguishes authentic Prophetic Hadith from customary fiqh/madrasah verbal formulas.",
            integrationMethodInApp = "Used to verify all Namaz recitations, the breaking-fast (Iftari) dua (Sunan Abi Dawud 2357), and the Sehri Niyat distinction between heart intention (Hadith) and verbal formulas (customary fiqh aid)."
        )
    )

    /**
     * All 17 Traditional Lessons of Noorani Qaida (Sheikh Noor Muhammad Haqqani Ludhianvi curriculum),
     * annotated with cross-references to the 21-lesson web division where applicable.
     */
    val nooraniQaidaLessons: List<NooraniQaidaLesson> = listOf(
        NooraniQaidaLesson(
            lessonNumber = 1,
            id = "qaida_lesson_01",
            titleArabic = "الدَّرْسُ الْأَوَّلُ: الْحُرُوفُ الْمُفْرَدَاتُ",
            titleUrdu = "سبق نمبر ۱: حروفِ مفردات (تہجی اور مخارج)",
            titleEnglish = "Lesson 1: Single Arabic Letters (Huroof-e-Mufradat & Makharij)",
            subtitle = "29 letters of the Arabic alphabet with articulation points (Makharij)",
            ruleSummaryEnglish = "Pronounce each of the 29 Arabic letters clearly from its exact point of articulation (Makhraj). Seven letters (خ ص ض ط ظ غ ق — 'Khussa Daughtin Qidh') are always pronounced with a full/heavy mouth (Tafkheem).",
            ruleSummaryUrdu = "عربی کے ۲۹ حروفِ مفردات کو ان کے درست مخارج سے تجوید کے ساتھ ادا کریں۔ سات حروفِ مستعلیہ (خ، ص، ض، ط، ظ، غ، ق — خُصَّ ضَغْطٍ قِظْ) ہمیشہ پُر (موٹے) پڑھے جاتے ہیں۔",
            editionNote = "Corresponds to Lesson 1 in both the classical 17-lesson Haqqani Qaida and 21-lesson web editions.",
            practiceItems = listOf(
                QaidaPracticeItem("l1_01", "ا", "Alif", "Empty space of throat & mouth (Jawf)", "جوفِ دہن (منہ اور حلق کا خلا)", "Straight vertical letter without bending sound."),
                QaidaPracticeItem("l1_02", "ب", "Baa", "Wet part of both lips meeting firmly", "دونوں ہونٹوںکی تری والا حصہ", "Clear labial closure."),
                QaidaPracticeItem("l1_03", "ت", "Taa", "Tip of tongue touching roots of upper two front teeth", "زبان کی نوک اور اوپر کے دو اگلے دانتوں کی جڑ", "Soft and light (Tarqeeq)."),
                QaidaPracticeItem("l1_04", "ث", "Thaa", "Tip of tongue touching edges of upper two front teeth", "زبان کی نوک اور اوپر کے دو اگلے دانتوں کا کنارہ", "Soft interdental sound, never like Seen."),
                QaidaPracticeItem("l1_05", "ج", "Jeem", "Middle of tongue touching middle of hard palate", "زبان کا درمیان اور تالو کا درمیانی حصہ", "Strong closure in the middle palate."),
                QaidaPracticeItem("l1_06", "ح", "Haa", "Middle of the throat (Wasat al-Halq)", "وسطِ حلق (حلق کا درمیانی حصہ)", "Sharp, clean throat friction."),
                QaidaPracticeItem("l1_07", "خ", "Khaa", "Upper throat nearest the mouth (Adna al-Halq) — Heavy", "ادنیٰ حلق (منہ کے قریب والا حصہ) — پُر حرف", "Heavy/full mouth letter (Musta'liyah)."),
                QaidaPracticeItem("l1_08", "د", "Daal", "Tip of tongue touching roots of upper front teeth", "زبان کی نوک اور اوپر کے اگلے دانتوں کی جڑ", "Has Qalqalah when sakin."),
                QaidaPracticeItem("l1_09", "ذ", "Dhaal", "Tip of tongue touching edges of upper front teeth", "زبان کی نوک اور اوپر کے اگلے دانتوں کا کنارہ", "Soft interdental, distinct from Zaa."),
                QaidaPracticeItem("l1_10", "ر", "Raa", "Tip and slightly back of tongue touching upper gums", "زبان کی نوک کی پشت اور اوپر کے مسوڑھے", "Avoid excessive trilling (Takreer)."),
                QaidaPracticeItem("l1_11", "ز", "Zaa", "Tip of tongue near lower front teeth with whistle (Safeer)", "زبان کی نوک اور نیچے کے اگلے دانت (سیٹی والا حرف)", "Sharp buzzing sound (Safeer)."),
                QaidaPracticeItem("l1_12", "س", "Seen", "Tip of tongue near lower front teeth (light whistle)", "زبان کی نوک اور نیچے کے اگلے دانت (باریک سیٹی)", "Light (Tarqeeq) with Safeer."),
                QaidaPracticeItem("l1_13", "ش", "Sheen", "Middle of tongue with spreading breath (Tafashshi)", "وسطِ زبان سے آواز کا منہ میں پھیلنا (تفشی)", "Soft spreading sound."),
                QaidaPracticeItem("l1_14", "ص", "Saad", "Tip of tongue with elevated back tongue — Heavy whistle", "زبان کی نوک اور نیچے کے دانت — ہمیشہ پُر (موٹا)", "Heavy Musta'liyah counterpart of Seen."),
                QaidaPracticeItem("l1_15", "ض", "Daad", "Side(s) of tongue touching upper molars — Heavy", "زبان کی کروٹ اور اوپر کی ڈاڑھوں کی جڑ — پُر حرف", "Unique Arabic letter (Lughat ad-Daad)."),
                QaidaPracticeItem("l1_16", "ط", "Taa (Heavy)", "Tip of tongue against upper front tooth roots — Heavy", "زبان کی نوک اور اوپر کے دانتوں کی جڑ — ہمیشہ پُر", "Strongest heavy letter (Itbaq + Isti'la)."),
                QaidaPracticeItem("l1_17", "ظ", "Zaa (Heavy)", "Tip of tongue at edge of upper front teeth — Heavy", "زبان کی نوک اور اوپر کے دانتوں کا کنارہ — پُر", "Heavy interdental counterpart of Dhaal."),
                QaidaPracticeItem("l1_18", "ع", "'Ayn", "Middle of the throat (Wasat al-Halq)", "وسطِ حلق (حلق کا درمیانی حصہ)", "Deep throat constriction distinct from Hamzah."),
                QaidaPracticeItem("l1_19", "غ", "Ghayn", "Upper throat (Adna al-Halq) — Heavy", "ادنیٰ حلق — ہمیشہ پُر پڑھا جاتا ہے", "Smooth voiced throat friction."),
                QaidaPracticeItem("l1_20", "ف", "Faa", "Inner edge of lower lip touching tips of upper front teeth", "نچلے ہونٹ کا اندرونی حصہ اور اوپر کے دانتوں کے سرے", "Gentle airflow."),
                QaidaPracticeItem("l1_21", "ق", "Qaaf", "Extreme back of tongue touching soft palate — Heavy", "زبان کی جڑ اور نرم تالو — پُر حرف", "Deep uvular closure."),
                QaidaPracticeItem("l1_22", "ك", "Kaaf", "Back of tongue slightly forward of Qaaf — Light", "زبان کی جڑ سے ذرا آگے اور تالو — باریک حرف", "Light velar closure with slight breath (Hams)."),
                QaidaPracticeItem("l1_23", "ل", "Laam", "Edges of tongue tip touching gums of upper premolars/incisors", "زبان کا کنارہ اور اوپر کے دانتوں کے مسوڑھے", "Clear lateral sound."),
                QaidaPracticeItem("l1_24", "م", "Meem", "Dry outer part of both lips closing together", "دونوں ہونٹوں کی خشکی والا حصہ", "Accompanied by nasal resonance (Ghunnah) when doubled."),
                QaidaPracticeItem("l1_25", "ن", "Noon", "Tip of tongue touching upper gums just below Laam", "زبان کی نوک اور اوپر کے مسوڑھے", "Carries natural nasal resonance in the Khayshoom."),
                QaidaPracticeItem("l1_26", "و", "Waaw", "Rounding of both lips without closing completely", "دونوں ہونٹوں کو گول کرنے سے", "Keep lips rounded smoothly."),
                QaidaPracticeItem("l1_27", "ه", "Haa (Deep)", "Bottom/chest end of throat (Aqsa al-Halq)", "اقصیٰ حلق (سینے کی طرف والا حصہ)", "Deep breathy sound from lowest throat."),
                QaidaPracticeItem("l1_28", "ء", "Hamzah", "Bottom of throat (Aqsa al-Halq — glottal stop)", "اقصیٰ حلق (سینے کی طرف والا حصہ)", "Crisp glottal stop."),
                QaidaPracticeItem("l1_29", "ي", "Yaa", "Middle of tongue raised toward middle palate", "زبان کا درمیان اور تالو کا درمیانی حصہ", "Smooth palatal sound.")
            ),
            quizQuestions = listOf(
                QaidaQuizQuestion(
                    id = "q_l1_1",
                    questionEnglish = "Which group of 7 letters is ALWAYS pronounced heavy/full-mouth (Musta'liyah)?",
                    questionUrdu = "وہ کونسے ۷ حروفِ مستعلیہ ہیں جو ہمیشہ پُر (موٹے) پڑھے جاتے ہیں؟",
                    arabicPrompt = "خ ص ض ط ظ غ ق",
                    options = listOf("خ ص ض ط ظ غ ق (Khussa Daughtin Qidh)", "ب ت ث ج ح د ذ", "س ش ل م ن و ي"),
                    correctIndex = 0,
                    explanationEnglish = "The seven letters collected in 'Khussa Daughtin Qidh' (خ ص ض ط ظ غ ق) always have Isti'la (elevation of the back tongue).",
                    explanationUrdu = "حروفِ مستعلیہ سات ہیں جن کا مجموعہ 'خُصَّ ضَغْطٍ قِظْ' ہے، یہ ہر حال میں پُر پڑھے جاتے ہیں۔"
                ),
                QaidaQuizQuestion(
                    id = "q_l1_2",
                    questionEnglish = "From which part of the throat are 'Ayn (ع) and Haa (ح) articulated?",
                    questionUrdu = "عین (ع) اور حا (ح) حلق کے کس حصے سے ادا ہوتے ہیں؟",
                    arabicPrompt = "ع • ح",
                    options = listOf("Middle of the throat (Wasat al-Halq / وسطِ حلق)", "Lips (Shafatayn)", "Tip of the tongue"),
                    correctIndex = 0,
                    explanationEnglish = "'Ayn (ع) and Haa (ح) are articulated from the middle of the throat (Wasat al-Halq).",
                    explanationUrdu = "ع اور ح وسطِ حلق (حلق کے درمیانی حصے) سے ادا ہوتے ہیں۔"
                )
            )
        ),
        NooraniQaidaLesson(
            lessonNumber = 2,
            id = "qaida_lesson_02",
            titleArabic = "الدَّرْسُ الثَّانِي: الْحُرُوفُ الْمُرَكَّبَاتُ",
            titleUrdu = "سبق نمبر ۲: حروفِ مرکبات (ملے ہوئے حروف کی شکلیں)",
            titleEnglish = "Lesson 2: Joined / Compound Letters (Huroof-e-Murakkabat)",
            subtitle = "Recognizing Arabic letters in initial, medial, and final joined shapes",
            ruleSummaryEnglish = "When two or more letters join together to form a word, their shapes change (usually only the head of the letter remains in initial/medial position). Identify each letter by its body shape and the number/placement of its dots (Nuqta), and pronounce each letter's full name individually.",
            ruleSummaryUrdu = "جب دو یا دو سے زیادہ حروف آپس میں مل کر لکھے جاتے ہیں تو ان کی شکل بدل جاتی ہے (اکثر حروف کا صرف سرہ باقی رہتا ہے)۔ نقطوں کی جگہ اور تعداد سے ہر حرف کو پہچان کر الگ الگ نام لے کر پڑھیں۔",
            editionNote = "Lesson 2 in classical 17-lesson Haqqani Qaida.",
            practiceItems = listOf(
                QaidaPracticeItem("l2_01", "لا", "Laam Alif", "Laam joined with Alif", "لام الف", "Read as Laam followed by Alif."),
                QaidaPracticeItem("l2_02", "با", "Baa Alif", "Initial Baa (1 dot below) + Alif", "با الف", "Notice single dot under the first tooth."),
                QaidaPracticeItem("l2_03", "تا", "Taa Alif", "Initial Taa (2 dots above) + Alif", "تا الف", "Two dots above mark Taa."),
                QaidaPracticeItem("l2_04", "ثا", "Thaa Alif", "Initial Thaa (3 dots above) + Alif", "ثا الف", "Three dots above mark interdental Thaa."),
                QaidaPracticeItem("l2_05", "نا", "Noon Alif", "Initial Noon (1 dot above) + Alif", "نون الف", "One dot above the tooth marks Noon."),
                QaidaPracticeItem("l2_06", "يا", "Yaa Alif", "Initial Yaa (2 dots below) + Alif", "یا الف", "Two dots underneath mark Yaa."),
                QaidaPracticeItem("l2_07", "بج", "Baa Jeem", "Initial Baa joined to final Jeem", "با جیم", "Read each letter's full name clearly."),
                QaidaPracticeItem("l2_08", "بتث", "Baa Taa Thaa", "Three-letter compound: Baa + Taa + Thaa", "با تا ثا", "Distinguish 1 dot below, 2 above, 3 above."),
                QaidaPracticeItem("l2_09", "يهب", "Yaa Haa Baa", "Yaa + medial Haa + final Baa", "یا ہا با", "Recognize medial looped Haa."),
                QaidaPracticeItem("l2_10", "سلك", "Seen Laam Kaaf", "Seen + medial Laam + final Kaaf", "سین لام کاف", "Notice three teeth of Seen before Laam.")
            ),
            quizQuestions = listOf(
                QaidaQuizQuestion(
                    id = "q_l2_1",
                    questionEnglish = "How do you identify the letters in the compound 'بتث'?",
                    questionUrdu = "مرکب 'بتث' میں حروف کی ترتیب کیا ہے؟",
                    arabicPrompt = "بتث",
                    options = listOf("Baa (1 dot below), Taa (2 dots above), Thaa (3 dots above)", "Noon, Yaa, Baa", "Jeem, Haa, Khaa"),
                    correctIndex = 0,
                    explanationEnglish = "In joined form, Baa has 1 dot below, Taa has 2 dots above, and Thaa has 3 dots above.",
                    explanationUrdu = "ملے ہوئے حروف میں با کے نیچے ایک نقطہ، تا کے اوپر دو نقطے اور ثا کے اوپر تین نقطے ہوتے ہیں۔"
                )
            )
        ),
        NooraniQaidaLesson(
            lessonNumber = 3,
            id = "qaida_lesson_03",
            titleArabic = "الدَّرْسُ الثَّالِثُ: الْحُرُوفُ الْمُقَطَّعَاتُ",
            titleUrdu = "سبق نمبر ۳: حروفِ مقطعات (قرآنی سورتوں کے ابتدائی حروف)",
            titleEnglish = "Lesson 3: Disjointed Quranic Letters (Huroof-e-Muqatta'at)",
            subtitle = "Letter combinations at the opening of 29 Quranic Surahs",
            ruleSummaryEnglish = "Huroof-e-Muqatta'at appear at the beginning of 29 Surahs. Each letter is read by its name: Alif has no elongation; letters of 'Hayyun Tahirun' (ح ي ط ه ر) are stretched 2 counts (Madd Tabee'i); letters of 'Sanakus Lahu' / 'Kam Asal Naqas' (ك م ع س ل ن ق ص) are stretched 6 counts (Madd Lazim).",
            ruleSummaryUrdu = "حروفِ مقطعات ۲۹ سورتوں کے شروع میں آتے ہیں۔ الف پر مد نہیں ہوتی، 'حَيٌّ طَهُرَ' کے حروف ایک الف (۲ حرکات) کے برابر اور 'سَنَقُصُّ لَكُمْ' کے حروف تین الف (۶ حرکات — مدِ لازم) کے برابر کھینچ کر پڑھے جاتے ہیں۔",
            editionNote = "Lesson 3 in classical 17-lesson Haqqani Qaida; authentic Quran verse audio linked.",
            practiceItems = listOf(
                QaidaPracticeItem("l3_01", "الٓمٓ", "Alif-Laam-Meem", "Surah Al-Baqarah 2:1 — Laam & Meem have 6-count Madd Lazim with Ghunnah on Meem-to-Meem", "الف لام میم (سورۃ البقرہ ۲:۱)", "Stretch Laam 6 counts, merge Meem with Ghunnah, stretch Meem 6 counts.", Pair(2, 1)),
                QaidaPracticeItem("l3_02", "الٓمٓصٓ", "Alif-Laam-Meem-Saad", "Surah Al-A'raf 7:1 — 6-count Madd Lazim on Laam, Meem, Saad with Qalqalah on Daa of Saad", "الف لام میم صاد (سورۃ الاعراف ۷:۱)", "Stretch Laam, Meem, and Saad 6 counts each.", Pair(7, 1)),
                QaidaPracticeItem("l3_03", "الٓرٰ", "Alif-Laam-Raa", "Surah Yunus 10:1 — Laam (6 counts), Raa (2 counts)", "الف لام را (سورۃ یونس ۱۰:۱)", "Laam 6 counts, Raa 2 counts (Khari Zabar).", Pair(10, 1)),
                QaidaPracticeItem("l3_04", "كٓهٰيٰعٓصٓ", "Kaaf-Haa-Yaa-'Ayn-Saad", "Surah Maryam 19:1 — Kaaf (6), Haa (2), Yaa (2), 'Ayn (4 or 6 with Ikhfa), Saad (6)", "کاف ہا یا عین صاد (سورۃ مریم ۱۹:۱)", "Observe Ikhfa between Noon of 'Ayn and Saad.", Pair(19, 1)),
                QaidaPracticeItem("l3_05", "طٰهٰ", "Taa-Haa", "Surah Ta-Ha 20:1 — Both letters have 2-count standing Fatha", "طٰہٰ (سورۃ طٰہٰ ۲۰:۱)", "Stretch both Taa and Haa for 2 counts.", Pair(20, 1)),
                QaidaPracticeItem("l3_06", "طٰسٓمٓ", "Taa-Seen-Meem", "Surah Ash-Shu'ara 26:1 — Taa (2 counts), Seen & Meem (6 counts)", "طٰسٓمٓ (سورۃ الشعراء ۲۶:۱)", "Merge Noon of Seen into Meem with Ghunnah.", Pair(26, 1)),
                QaidaPracticeItem("l3_07", "يٰسٓ", "Yaa-Seen", "Surah Ya-Sin 36:1 — Yaa (2 counts), Seen (6 counts)", "یٰسٓ (سورۃ یٰس ۳۶:۱)", "Yaa 2 counts, Seen 6 counts.", Pair(36, 1)),
                QaidaPracticeItem("l3_08", "حٰمٓ", "Haa-Meem", "Surah Ghafir 40:1 — Haa (2 counts), Meem (6 counts)", "حٰمٓ (سورۃ غافر ۴۰:۱)", "Clean throat Haa for 2 counts, Meem for 6 counts.", Pair(40, 1)),
                QaidaPracticeItem("l3_09", "قٓ", "Qaaf", "Surah Qaf 50:1 — 6-count Madd Lazim + Qalqalah on Faa", "قٓ (سورۃ ق ۵۰:۱)", "Heavy Qaaf stretched 6 counts.", Pair(50, 1)),
                QaidaPracticeItem("l3_10", "نٓ", "Noon", "Surah Al-Qalam 68:1 — 6-count Madd Lazim", "نٓ (سورۃ القلم ۶۸:۱)", "Stretch Noon 6 counts smoothly.", Pair(68, 1))
            ),
            quizQuestions = listOf(
                QaidaQuizQuestion(
                    id = "q_l3_1",
                    questionEnglish = "In 'الٓمٓ' (Alif-Laam-Meem), how many counts (harakat) are Laam and Meem elongated?",
                    questionUrdu = "'الٓمٓ' میں لام اور میم کو کتنی حرکات (الف) کے برابر کھینچا جاتا ہے؟",
                    arabicPrompt = "الٓمٓ",
                    options = listOf("6 counts (3 Alifs — Madd Lazim)", "1 count without elongation", "Only Alif is stretched"),
                    correctIndex = 0,
                    explanationEnglish = "Laam and Meem have a 3-letter spelling with a central Madd letter followed by a permanent Sukoon, requiring 6 counts (Madd Lazim Harfi).",
                    explanationUrdu = "لام اور میم پر مدِ لازم حرفی ہوتی ہے، اس لیے انہیں تین الف (۶ حرکات) کے برابر کھینچ کر پڑھا جاتا ہے۔"
                )
            )
        ),
        NooraniQaidaLesson(
            lessonNumber = 4,
            id = "qaida_lesson_04",
            titleArabic = "الدَّرْسُ الرَّابِعُ: الْحَرَكَاتُ (فَتْحَةٌ • كَسْرَةٌ • ضَمَّةٌ)",
            titleUrdu = "سبق نمبر ۴: حرکات (زبر، زیر، پیش)",
            titleEnglish = "Lesson 4: Short Vowels / Movements (Harakat — Fatha, Kasra, Damma)",
            subtitle = "Crisp 1-count vowel sounds without stretching or jerking",
            ruleSummaryEnglish = "Fatha (Zabar ـَ), Kasra (Zer ـِ), and Damma (Pesh ـُ) are called Harakat. Pronounce each vowel briskly for exactly 1 count without elongating it (do not turn 'Ba' into 'Baa') and without a glottal jerk (Jhatka). Alif with any Harakah or Sukoon is called Hamzah.",
            ruleSummaryUrdu = "زبر (ـَ)، زیر (ـِ) اور پیش (ـُ) کو حرکات کہتے ہیں۔ حرکات کو بغیر کھینچے اور بغیر جھٹکا دیے معروف آواز کے ساتھ جلدی پڑھیں۔ جس الف پر حرکت یا جزم ہو اسے ہمزہ کہتے ہیں۔",
            editionNote = "Lesson 4 in classical 17-lesson Haqqani Qaida.",
            practiceItems = listOf(
                QaidaPracticeItem("l4_01", "ءَ ءِ ءُ", "A - I - U", "Hamzah Fatha A, Hamzah Kasra I, Hamzah Damma U", "ہمزہ زبر اَ، ہمزہ زیر اِ، ہمزہ پیش اُ", "Crisp 1-count glottal vowel without stretching."),
                QaidaPracticeItem("l4_02", "بَ بِ بُ", "Ba - Bi - Bu", "Baa Fatha Ba, Baa Kasra Bi, Baa Damma Bu", "با زبر بَ، با زیر بِ، با پیش بُ", "Round the lips fully on Damma (Bu)."),
                QaidaPracticeItem("l4_03", "تَ تِ تُ", "Ta - Ti - Tu", "Taa Fatha Ta, Taa Kasra Ti, Taa Damma Tu", "تا زبر تَ، تا زیر تِ، تا پیش تُ", "Keep Taa light and short."),
                QaidaPracticeItem("l4_04", "ثَ ثِ ثُ", "Tha - Thi - Thu", "Thaa Fatha Tha, Thaa Kasra Thi, Thaa Damma Thu", "ثا زبر ثَ، ثا زیر ثِ، ثا پیش ثُ", "Soft tip of tongue on upper teeth."),
                QaidaPracticeItem("l4_05", "حَ حِ حُ", "Ha - Hi - Hu", "Haa Fatha Ha, Haa Kasra Hi, Haa Damma Hu", "حا زبر حَ، حا زیر حِ، حا پیش حُ", "From the middle of the throat."),
                QaidaPracticeItem("l4_06", "خَ خِ خُ", "Kha - Khi - Khu", "Khaa Fatha Kha, Khaa Kasra Khi, Khaa Damma Khu (Heavy)", "خا زبر خَ، خا زیر خِ، خا پیش خُ (پُر)", "Heavy throat sound without stretching."),
                QaidaPracticeItem("l4_07", "صَ صِ صُ", "Sa - Si - Su", "Saad Fatha Sa, Saad Kasra Si, Saad Damma Su (Heavy)", "صاد زبر صَ، صاد زیر صِ، صاد پیش صُ", "Maintain full mouth (Isti'la) on all three vowels."),
                QaidaPracticeItem("l4_08", "طَ طِ طُ", "Ta - Ti - Tu (Heavy)", "Taa Fatha, Kasra, Damma (Heavy)", "طا زبر طَ، طا زیر طِ، طا پیش طُ", "Never elongate into Taa-Alif."),
                QaidaPracticeItem("l4_09", "عَ عِ عُ", "'A - 'I - 'U", "'Ayn Fatha 'A, 'Ayn Kasra 'I, 'Ayn Damma 'U", "عین زبر عَ، عین زیر عِ، عین پیش عُ", "Distinct middle-throat 'Ayn."),
                QaidaPracticeItem("l4_10", "قَ قِ قُ", "Qa - Qi - Qu", "Qaaf Fatha Qa, Qaaf Kasra Qi, Qaaf Damma Qu", "قاف زبر قَ، قاف زیر قِ، قاف پیش قُ", "Deep root-of-tongue Qaaf.")
            ),
            quizQuestions = listOf(
                QaidaQuizQuestion(
                    id = "q_l4_1",
                    questionEnglish = "How should a letter with Fatha (Zabar), Kasra (Zer), or Damma (Pesh) be read?",
                    questionUrdu = "زبر، زیر اور پیش والے حرف کو کیسے پڑھنا چاہیے؟",
                    arabicPrompt = "بَ بِ بُ",
                    options = listOf("Briskly for 1 count without stretching or jerking", "Stretched for 2 counts like Madd", "With nasal Ghunnah"),
                    correctIndex = 0,
                    explanationEnglish = "Short vowels (Harakat) are read for a single count without elongation or a glottal jerk.",
                    explanationUrdu = "حرکات کو بغیر کھینچے اور بغیر جھٹکا دیے معروف طریقے سے جلدی ادا کیا جاتا ہے۔"
                )
            )
        ),
        NooraniQaidaLesson(
            lessonNumber = 5,
            id = "qaida_lesson_05",
            titleArabic = "الدَّرْسُ الْخَامِسُ: التَّنْوِينُ (فَتْحَتَانِ • كَسْرَتَانِ • ضَمَّتَانِ)",
            titleUrdu = "سبق نمبر ۵: تنوین (دو زبر، دو زیر، دو پیش)",
            titleEnglish = "Lesson 5: Nunation / Double Vowels (Tanween)",
            subtitle = "Double Fatha (-an), Double Kasra (-in), Double Damma (-un)",
            ruleSummaryEnglish = "Double Fatha (ـً), Double Kasra (ـٍ), and Double Damma (ـٌ) are called Tanween. Tanween produces the sound of an unvoweled Noon (Noon Sakinah) at the end of a word. Notice the written Alif after Double Fatha on most letters (except round Taa ة and Hamzah).",
            ruleSummaryUrdu = "دو زبر (ـً)، دو زیر (ـٍ) اور دو پیش (ـٌ) کو تنوین کہتے ہیں۔ تنوین کی آواز نونِ ساکن کی طرح ہوتی ہے۔ دو زبر کے بعد اکثر حروف کے ساتھ الف لکھا جاتا ہے (مگر وصل کی صورت میں الف نہیں پڑھا جاتا)۔",
            editionNote = "Lesson 5 in classical 17-lesson Haqqani Qaida.",
            practiceItems = listOf(
                QaidaPracticeItem("l5_01", "مًا مٍ مٌ", "Man - Min - Mun", "Meem two Zabar Man, Meem two Zer Min, Meem two Pesh Mun", "میم دو زبر مًا، میم دو زیر مٍ، میم دو پیش مٌ", "Clear Noon sound at the end."),
                QaidaPracticeItem("l5_02", "بًا بٍ بٌ", "Ban - Bin - Bun", "Baa two Zabar Ban, Baa two Zer Bin, Baa two Pesh Bun", "با دو زبر بًا، با دو زیر بٍ، با پیش بٌ", "Crisp vowel before the Noon Sakinah sound."),
                QaidaPracticeItem("l5_03", "تًا تٍ تٌ", "Tan - Tin - Tun", "Taa two Zabar Tan, Taa two Zer Tin, Taa two Pesh Tun", "تا دو زبر تًا، تا دو زیر تٍ، تا دو پیش تٌ", "Do not stretch the vowel before the 'n'."),
                QaidaPracticeItem("l5_04", "حًا حٍ حٌ", "Han - Hin - Hun", "Haa two Zabar Han, Haa two Zer Hin, Haa two Pesh Hun", "حا دو زبر حًا، حا دو زیر حٍ، حا دو پیش حٌ", "Articulate Haa from middle throat."),
                QaidaPracticeItem("l5_05", "صًا صٍ صٌ", "San - Sin - Sun (Heavy)", "Saad Tanween with heavy Isti'la", "صاد دو زبر صًا، صاد دو زیر صٍ، صاد دو پیش صٌ", "Heavy Saad followed by light Noon sound."),
                QaidaPracticeItem("l5_06", "قًا قٍ قٌ", "Qan - Qin - Qun", "Qaaf Tanween with heavy Qaaf", "قاف دو زبر قًا، قاف دو زیر قٍ، قاف دو پیش قٌ", "Heavy Qaaf, clear ending."),
                QaidaPracticeItem("l5_07", "ةً ةٍ ةٌ", "Tan - Tin - Tun (Round Taa)", "Taa Marbootah with Tanween (no Alif added after two Zabar)", "گول تا (ة) دو زبر ةً، دو زیر ةٍ، دو پیش ةٌ", "Turns into Haa Sakinah when stopping (Waqf).")
            ),
            quizQuestions = listOf(
                QaidaQuizQuestion(
                    id = "q_l5_1",
                    questionEnglish = "What hidden consonant sound is heard at the end of every Tanween (ـً ـٍ ـٌ)?",
                    questionUrdu = "تنوین (دو زبر، دو زیر، دو پیش) کے آخر میں کس حرف کی آواز پیدا ہوتی ہے؟",
                    arabicPrompt = "بًا • بٍ • بٌ",
                    options = listOf("Noon Sakinah (نْ)", "Meem Sakinah (مْ)", "Waaw Sakinah (وْ)"),
                    correctIndex = 0,
                    explanationEnglish = "Tanween is an unwritten Noon Sakinah pronounced at the end of nouns.",
                    explanationUrdu = "تنوین دراصل نونِ ساکن کی آواز ہے جو اسم کے آخر میں ادا ہوتی ہے مگر لکھی نہیں جاتی۔"
                )
            )
        ),
        NooraniQaidaLesson(
            lessonNumber = 6,
            id = "qaida_lesson_06",
            titleArabic = "الدَّرْسُ السَّادِسُ: تَدْرِيبٌ عَلَى الْحَرَكَاتِ وَالتَّنْوِينِ",
            titleUrdu = "سبق نمبر ۶: تختی مشق حرکات و تنوین (تین حرفی قرآنی الفاظ)",
            titleEnglish = "Lesson 6: Exercise on Harakat and Tanween (Quranic Words)",
            subtitle = "Joining voweled letters and Tanween into complete Quranic words",
            ruleSummaryEnglish = "Practice reading complete 3-letter and 4-letter Quranic words formed with Harakat and Tanween. Keep every single-vowel letter brisk (1 count) so a Fatha never accidentally sounds like an Alif.",
            ruleSummaryUrdu = "حرکات اور تنوین والے مکمل قرآنی الفاظ کی مشق کریں۔ ہر حرف کو اس کی حرکت کے مطابق بغیر کھینچے ملا کر پڑھیں۔",
            editionNote = "Lesson 6 in classical 17-lesson Haqqani Qaida (split into Lessons 6–7 in 21-lesson web editions).",
            practiceItems = listOf(
                QaidaPracticeItem("l6_01", "أَبَدًا", "Abadan", "Surah An-Naba 78:23 — Hamzah Fatha A + Baa Fatha Ba + Daal two Zabar Dan = Abadan", "ہمزہ زبر اَ، با زبر بَ (اَبَ)، دال دو زبر دًا = اَبَدًا", "Do not elongate A or Ba.", Pair(78, 23)),
                QaidaPracticeItem("l6_02", "أَحَدٌ", "Ahadun", "Surah Al-Ikhlas 112:1 — A-ha-dun", "ہمزہ زبر اَ، حا زبر حَ (اَحَ)، دال دو پیش دٌ = اَحَدٌ", "Middle-throat Haa, turns to Qalqalah on Daal when stopping.", Pair(112, 1)),
                QaidaPracticeItem("l6_03", "أَخَذَ", "Akhadha", "Surah An-Nazi'at 79:25 — A-kha-dha (Light Hamzah, Heavy Khaa, Soft Dhaal)", "اَخَذَ", "Distinguish heavy Khaa between two light letters.", Pair(79, 25)),
                QaidaPracticeItem("l6_04", "أَذِنَ", "Adhina", "Surah An-Naba 78:38 — A-dhi-na", "اَذِنَ", "Crisp Kasra on Dhaal.", Pair(78, 38)),
                QaidaPracticeItem("l6_05", "أَمَرَ", "Amara", "Surah 'Abasa 80:23 — A-ma-ra (Raa is heavy because it has Fatha)", "اَمَرَ", "Raa with Fatha is read heavy (Pura).", Pair(80, 23)),
                QaidaPracticeItem("l6_06", "بَخِلَ", "Bakhila", "Surah Al-Layl 92:8 — Ba-khi-la", "بَخِلَ", "Crisp Kasra on Khaa.", Pair(92, 8)),
                QaidaPracticeItem("l6_07", "بَرَرَةٍ", "Bararatin", "Surah 'Abasa 80:16 — Ba-ra-ra-tin", "بَرَرَةٍ", "Four syllables read evenly.", Pair(80, 16)),
                QaidaPracticeItem("l6_08", "حَسَنَةً", "Hasanatan", "Surah Al-Baqarah 2:201 — Ha-sa-na-tan", "حَسَنَةً", "All letters light (Mustafilah).", Pair(2, 201)),
                QaidaPracticeItem("l6_09", "سَفَرَةٍ", "Safaratin", "Surah 'Abasa 80:15 — Sa-fa-ra-tin", "سَفَرَةٍ", "Keep Seen and Faa light, Raa heavy.", Pair(80, 15)),
                QaidaPracticeItem("l6_10", "كُفُوًا", "Kufuwan", "Surah Al-Ikhlas 112:4 — Ku-fu-wan", "كُفُوًا", "Round lips smoothly on Ku-fu.", Pair(112, 4))
            ),
            quizQuestions = listOf(
                QaidaQuizQuestion(
                    id = "q_l6_1",
                    questionEnglish = "In the word 'أَخَذَ' (Akhadha), which letter is pronounced heavy?",
                    questionUrdu = "لفظ 'أَخَذَ' میں کون سا حرف پُر (موٹا) پڑھا جائے گا؟",
                    arabicPrompt = "أَخَذَ",
                    options = listOf("Khaa (خَ)", "Hamzah (أَ)", "Dhaal (ذَ)"),
                    correctIndex = 0,
                    explanationEnglish = "Khaa (خ) is one of the 7 letters of Isti'la, while Hamzah and Dhaal are light letters.",
                    explanationUrdu = "خ حروفِ مستعلیہ میں سے ہے اس لیے پُر پڑھا جائے گا جبکہ ہمزہ اور ذال باریک ہیں۔"
                )
            )
        ),
        NooraniQaidaLesson(
            lessonNumber = 7,
            id = "qaida_lesson_07",
            titleArabic = "الدَّرْسُ السَّابِعُ: الْحَرَكَاتُ الْمَمْدُودَةُ (الْأَلِفُ الصَّغِيرَةُ وَالْيَاءُ وَالْوَاوُ)",
            titleUrdu = "سبق نمبر ۷: کھڑی حرکات (کھڑا زبر، کھڑی زیر، الٹا پیش)",
            titleEnglish = "Lesson 7: Standing Vowels (Khari Zabar, Khari Zer, Ulta Pesh)",
            subtitle = "Vertical Fatha, Vertical Kasra, and Inverted Damma (stretched 2 counts)",
            ruleSummaryEnglish = "Standing Fatha (Khara Zabar ـٰ), Standing Kasra (Khari Zer ـٖ), and Inverted Damma (Ulta Pesh ـٗ) are equal to the three letters of Madd (Alif, Yaa, Waaw) and are elongated for 1 Alif (2 counts / harakat).",
            ruleSummaryUrdu = "کھڑا زبر (ـٰ)، کھڑی زیر (ـٖ) اور الٹا پیش (ـٗ) حروفِ مدہ کے قائم مقام ہیں اور انہیں ایک الف (۲ حرکات) کے برابر کھینچ کر پڑھا جاتا ہے۔",
            editionNote = "Lesson 7 in classical 17-lesson Haqqani Qaida.",
            practiceItems = listOf(
                QaidaPracticeItem("l7_01", "بٰ بٖ بٗ", "Baa - Bee - Boo", "Baa Standing Fatha Baa, Standing Kasra Bee, Inverted Damma Boo", "با کھڑا زبر بٰ، با کھڑی زیر بٖ، با الٹا پیش بٗ", "Stretch each for 2 counts."),
                QaidaPracticeItem("l7_02", "يٰ يٖ يٗ", "Yaa - Yee - Yoo", "Yaa with standing vowels (2 counts)", "یا کھڑا زبر یٰ، یا کھڑی زیر یٖ، یا الٹا پیش یٗ", "Equal to Yaa-Alif, Yaa-Yaa, Yaa-Waaw."),
                QaidaPracticeItem("l7_03", "رٰ رٖ رٗ", "Raa - Ree - Roo", "Raa with standing vowels", "را کھڑا زبر رٰ، را کھڑی زیر رٖ، را الٹا پیش رٗ", "Raa is heavy on Zabar/Pesh and light on Zer."),
                QaidaPracticeItem("l7_04", "مٰلِكِ", "Maaliki", "Surah Al-Fatiha 1:4 — Standing Fatha on Meem (2 counts)", "مٰلِكِ (سورۃ الفاتحہ ۱:۴)", "Stretch Meem 2 counts, keep Laam and Kaaf 1 count.", Pair(1, 4)),
                QaidaPracticeItem("l7_05", "إِلٰهِ", "Ilaahi", "Surah An-Nas 114:3 — Standing Fatha on Laam", "اِلٰهِ (سورۃ الناس ۱۱۴:۳)", "Stretch Laam 2 counts.", Pair(114, 3)),
                QaidaPracticeItem("l7_06", "بِهِٖ", "Bihee", "Standing Kasra on Haa (Madd Silah Sughra — 2 counts)", "بِہٖ", "Stretch Haa 2 counts when connecting."),
                QaidaPracticeItem("l7_07", "لَهُٗ", "Lahoo", "Inverted Damma on Haa (Madd Silah Sughra — 2 counts)", "لَہٗ", "Stretch Haa Damma for 2 counts.")
            ),
            quizQuestions = listOf(
                QaidaQuizQuestion(
                    id = "q_l7_1",
                    questionEnglish = "How long is a Standing Fatha (Khara Zabar ـٰ) elongated?",
                    questionUrdu = "کھڑا زبر، کھڑی زیر اور الٹا پیش کو کتنا کھینچ کر پڑھا جاتا ہے؟",
                    arabicPrompt = "مٰلِكِ",
                    options = listOf("1 Alif (2 counts), just like Huroof-e-Maddah", "No elongation (1 count)", "6 counts like Madd Lazim"),
                    correctIndex = 0,
                    explanationEnglish = "Standing vowels represent miniature Madd letters and are stretched for 2 counts (1 Alif).",
                    explanationUrdu = "کھڑی حرکات حروفِ مدہ کے برابر ہوتی ہیں، اس لیے انہیں ایک الف (۲ حرکات) کے برابر کھینچا جاتا ہے۔"
                )
            )
        ),
        NooraniQaidaLesson(
            lessonNumber = 8,
            id = "qaida_lesson_08",
            titleArabic = "الدَّرْسُ الثَّامِنُ: حُرُوفُ الْمَدِّ وَاللِّينِ",
            titleUrdu = "سبق نمبر ۸: حروفِ مدہ اور حروفِ لین",
            titleEnglish = "Lesson 8: Letters of Elongation & Softness (Huroof-e-Maddah & Leen)",
            subtitle = "Alif Maddah, Waaw Maddah, Yaa Maddah (2 counts) & Waaw/Yaa Leen (soft)",
            ruleSummaryEnglish = "Huroof-e-Maddah are 3: Alif preceded by Fatha (ـَا), Waaw Sakinah preceded by Damma (ـُوْ), and Yaa Sakinah preceded by Kasra (ـِيْ) — stretch each for 2 counts. Huroof-e-Leen are 2: Waaw Sakinah preceded by Fatha (ـَوْ) and Yaa Sakinah preceded by Fatha (ـَيْ) — pronounce softly without jerking.",
            ruleSummaryUrdu = "حروفِ مدہ تین ہیں: الف سے پہلے زبر ہو، واؤ ساکن سے پہلے پیش ہو، یا ساکن سے پہلے زیر ہو (انہیں ایک الف یعنی ۲ حرکات کھینچیں)۔ حروفِ لین دو ہیں: واؤ ساکن اور یا ساکن سے پہلے زبر ہو (انہیں نرمی سے بغیر جھٹکے کے پڑھیں)۔",
            editionNote = "Lesson 8 in classical 17-lesson Haqqani Qaida (split into Maddah and Leen in 21-lesson web editions).",
            practiceItems = listOf(
                QaidaPracticeItem("l8_01", "بَا بُوْ بِيْ", "Baa - Boo - Bee", "Three Huroof-e-Maddah on Baa (2 counts each)", "با الف زبر بَا، با واؤ پیش بُوْ، با یا زیر بِيْ", "Smooth 2-count elongation."),
                QaidaPracticeItem("l8_02", "تَا تُوْ تِيْ", "Taa - Too - Tee", "Three Huroof-e-Maddah on Taa", "تا الف زبر تَا، تا واؤ پیش تُوْ، تا یا زیر تِيْ", "Clear vowel purity."),
                QaidaPracticeItem("l8_03", "نُوْحِيْهَا", "Nooheehaa", "Surah Hud 11:49 — Contains all three Madd letters in a single Quranic word!", "نُوْحِيْهَا (سورۃ ہود ۱۱:۴۹ — تینوں حروفِ مدہ کا مجموعہ)", "Noo (Waaw Maddah), Hee (Yaa Maddah), Haa (Alif Maddah).", Pair(11, 49)),
                QaidaPracticeItem("l8_04", "بَوْ بَيْ", "Baw - Bay", "Two Huroof-e-Leen on Baa (Waaw Leen & Yaa Leen)", "با واؤ زبر بَوْ، با یا زبر بَيْ (حروفِ لین)", "Pronounce softly and smoothly."),
                QaidaPracticeItem("l8_05", "تَوْ تَيْ", "Taw - Tay", "Waaw Leen & Yaa Leen on Taa", "تا واؤ زبر تَوْ، تا یا زبر تَيْ", "Soft diphthong sound."),
                QaidaPracticeItem("l8_06", "قُرَيْشٍ", "Qurayshin", "Surah Quraysh 106:1 — Yaa Leen on Raa", "قُرَيْشٍ (سورۃ قریش ۱۰۶:۱)", "Soft Yaa Leen; stretches 2–6 counts if stopping (Madd Leen).", Pair(106, 1)),
                QaidaPracticeItem("l8_07", "خَوْفٍ", "Khawfin", "Surah Quraysh 106:4 — Waaw Leen on Khaa", "خَوْفٍ (سورۃ قریش ۱۰۶:۴)", "Heavy Khaa + soft Waaw Leen.", Pair(106, 4)),
                QaidaPracticeItem("l8_08", "الصَّيْفِ", "As-Sayfi", "Surah Quraysh 106:2 — Yaa Leen on Saad", "الصَّيْفِ (سورۃ قریش ۱۰۶:۲)", "Heavy Saad + soft Yaa Leen.", Pair(106, 2))
            ),
            quizQuestions = listOf(
                QaidaQuizQuestion(
                    id = "q_l8_1",
                    questionEnglish = "Which famous Quranic word gathers all three Huroof-e-Maddah (Waaw, Yaa, and Alif Maddah) together?",
                    questionUrdu = "قرآن مجید کے کس مشہور لفظ میں تینوں حروفِ مدہ (واؤ، یا، الف) ایک ساتھ جمع ہیں؟",
                    arabicPrompt = "نُوْحِيْهَا",
                    options = listOf("نُوْحِيْهَا (Nooheehaa — Surah Hud 11:49)", "قُرَيْشٍ (Qurayshin)", "أَحَدٌ (Ahadun)"),
                    correctIndex = 0,
                    explanationEnglish = "In نُوْحِيْهَا (Nooheehaa), نُوْ has Waaw Maddah, حِيْ has Yaa Maddah, and هَا has Alif Maddah.",
                    explanationUrdu = "لفظ 'نُوْحِيْهَا' میں نُوْ (واؤ مدہ)، حِيْ (یا مدہ) اور هَا (الف مدہ) تینوں جمع ہیں۔"
                )
            )
        ),
        NooraniQaidaLesson(
            lessonNumber = 9,
            id = "qaida_lesson_09",
            titleArabic = "الدَّرْسُ التَّاسِعُ: تَدْرِيبٌ عَلَى الْمَدِّ وَاللِّينِ وَالْحَرَكَاتِ",
            titleUrdu = "سبق نمبر ۹: مشق حرکات، کھڑی حرکات، مدہ اور لین",
            titleEnglish = "Lesson 9: Exercise on Harakat, Standing Vowels, Maddah & Leen",
            subtitle = "Distinguishing 1-count vowels, 2-count Madd, and longer Madd Muttasil/Munfasil",
            ruleSummaryEnglish = "Apply all vowel rules together in Quranic words. When a Madd letter is followed by a Hamzah in the same word (Madd Muttasil) or the next word (Madd Munfasil), elongate for 4–5 counts.",
            ruleSummaryUrdu = "حرکات، کھڑی حرکات، حروفِ مدہ اور حروفِ لین کی مشترکہ مشق۔ اگر حرفِ مدہ کے بعد ہمزہ اسی کلمے میں ہو (مدِ متصل) یا اگلے کلمے کے شروع میں ہو (مدِ منفصل) تو ۴ سے ۵ حرکات تک کھینچ کر پڑھیں۔",
            editionNote = "Lesson 9 in classical 17-lesson Haqqani Qaida.",
            practiceItems = listOf(
                QaidaPracticeItem("l9_01", "آمَنَ", "Aamana", "Hamzah Standing Fatha Aa (Madd Badal 2 counts) + Ma-na", "آ مَنَ", "Stretch initial Hamzah 2 counts."),
                QaidaPracticeItem("l9_02", "إِيْلَافِهِمْ", "Eelaafihim", "Surah Quraysh 106:2 — Yaa Maddah + Standing Fatha on Laam", "اِيْلٰفِهِمْ (سورۃ قریش ۱۰۶:۲)", "Stretch both Ee and Laa 2 counts.", Pair(106, 2)),
                QaidaPracticeItem("l9_03", "جَآءَ", "Jaa'a", "Surah An-Nasr 110:1 — Madd Wajib Muttasil (4–5 counts before Hamzah)", "جَآءَ (سورۃ النصر ۱۱۰:۱ — مدِ واجب متصل)", "Stretch Jaa 4–5 counts before crisp final Hamzah.", Pair(110, 1)),
                QaidaPracticeItem("l9_04", "شَآءَ", "Shaa'a", "Surah 'Abasa 80:12 — Madd Wajib Muttasil (4–5 counts)", "شَآءَ", "Light Sheen stretched 4–5 counts.", Pair(80, 12)),
                QaidaPracticeItem("l9_05", "مَالِئُونَ", "Maali'oona", "Surah Al-Waqi'ah 56:53 — Alif Maddah + Waaw Maddah", "مَالِئُوْنَ", "Balance 2-count elongations.", Pair(56, 53)),
                QaidaPracticeItem("l9_06", "مَوْءُودَةُ", "Maw'oodatu", "Surah At-Takwir 81:8 — Waaw Leen + Inverted Damma/Waaw Maddah", "مَوْءٗدَةُ", "Soft Waaw Leen followed by Hamzah with 2-count Damma.", Pair(81, 8))
            ),
            quizQuestions = listOf(
                QaidaQuizQuestion(
                    id = "q_l9_1",
                    questionEnglish = "In 'جَآءَ' (Jaa'a), why is the Alif Maddah stretched for 4–5 counts?",
                    questionUrdu = "لفظ 'جَآءَ' میں الف مدہ کو ۴ سے ۵ حرکات کیوں کھینچا جاتا ہے؟",
                    arabicPrompt = "جَآءَ",
                    options = listOf("Because Hamzah follows the Madd letter in the same word (Madd Wajib Muttasil)", "Because it is Huroof-e-Leen", "Because of Tanween"),
                    correctIndex = 0,
                    explanationEnglish = "When a Madd letter is immediately followed by a Hamzah within the same word, it is Madd Wajib Muttasil (4–5 counts).",
                    explanationUrdu = "حرفِ مدہ کے بعد ہمزہ اسی کلمے میں آئے تو اسے مدِ واجب متصل کہتے ہیں اور ۴ تا ۵ حرکات کھینچا جاتا ہے۔"
                )
            )
        ),
        NooraniQaidaLesson(
            lessonNumber = 10,
            id = "qaida_lesson_10",
            titleArabic = "الدَّرْسُ الْعَاشِرُ: السُّكُونُ (الْجَزْمُ) وَحُرُوفُ الْقَلْقَلَةِ",
            titleUrdu = "سبق نمبر ۱۰: سکون یعنی جزم اور حروفِ قلقلہ",
            titleEnglish = "Lesson 10: Sukoon / Jazm (Resting Sign) & Qalqalah Letters",
            subtitle = "Joining a voweled letter to a Sakin letter & echoing 'Qutbu Jaddin' (ق ط ب ج د)",
            ruleSummaryEnglish = "A letter with Sukoon/Jazm (ـْ) is called Sakin and cannot be read alone; it joins the voweled letter before it. When any of the 5 Qalqalah letters — ق ط ب ج د ('Qutbu Jaddin') — has Sukoon, pronounce it with a natural echoing bounce (Qalqalah). Never bounce non-Qalqalah letters.",
            ruleSummaryUrdu = "جس حرف پر جزم (ـْ) ہو اسے ساکن کہتے ہیں، وہ اپنے سے پہلے متحرک حرف سے مل کر پڑھا جاتا ہے۔ حروفِ قلقلہ پانچ ہیں: 'قُطْبُ جَدٍّ' (ق، ط، ب، ج، د) — جب یہ ساکن ہوں تو ان کے مخرج میں جنبش (لَوٹتی ہوئی آواز / قلقلہ) ہوتی ہے۔",
            editionNote = "Lesson 10 in classical 17-lesson Haqqani Qaida.",
            practiceItems = listOf(
                QaidaPracticeItem("l10_01", "أَبْ إِبْ أُبْ", "Ab - Ib - Ub", "Baa Sakinah — Qalqalah letter (bounce on lips)", "ہمزہ با زبر اَبْ، ہمزہ با زیر اِبْ، ہمزہ با پیش اُبْ (قلقلہ)", "Clear labial bounce on Baa Sakinah."),
                QaidaPracticeItem("l10_02", "أَجْ إِجْ أُجْ", "Aj - Ij - Uj", "Jeem Sakinah — Qalqalah letter", "اَجْ اِجْ اُجْ (قلقلہ)", "Echoing release from middle palate."),
                QaidaPracticeItem("l10_03", "أَدْ إِدْ أُدْ", "Ad - Id - Ud", "Daal Sakinah — Qalqalah letter", "اَدْ اِدْ اُدْ (قلقلہ)", "Crisp bounce on Daal Sakinah."),
                QaidaPracticeItem("l10_04", "أَطْ إِطْ أُطْ", "At - It - Ut (Heavy Qalqalah)", "Taa Sakinah — Heavy Qalqalah letter", "اَطْ اِطْ اُطْ (پُر قلقلہ)", "Keep Qalqalah heavy on Taa."),
                QaidaPracticeItem("l10_05", "أَقْ إِقْ أُقْ", "Aq - Iq - Uq", "Qaaf Sakinah — Heavy Qalqalah letter", "اَقْ اِقْ اُقْ (پُر قلقلہ)", "Deep uvular Qalqalah."),
                QaidaPracticeItem("l10_06", "أَتْ إِتْ أُتْ", "At - It - Ut (Light, Hams)", "Taa Sakinah — NO Qalqalah, gentle breath (Hams)", "اَتْ اِتْ اُتْ (ہمس — قلقلہ نہ کریں)", "Release slight breath without bouncing."),
                QaidaPracticeItem("l10_07", "أَهْ إِهْ أُهْ", "Ah - Ih - Uh", "Haa Sakinah — Deep throat breath without Qalqalah", "اَهْ اِهْ اُهْ", "Smooth throat airflow.")
            ),
            quizQuestions = listOf(
                QaidaQuizQuestion(
                    id = "q_l10_1",
                    questionEnglish = "Which 5 letters make up Huroof-e-Qalqalah ('Qutbu Jaddin')?",
                    questionUrdu = "حروفِ قلقلہ کونسے پانچ حروف ہیں جن کا مجموعہ 'قُطْبُ جَدٍّ' ہے؟",
                    arabicPrompt = "ق ط ب ج د",
                    options = listOf("ق ط ب ج د (Qaaf, Taa, Baa, Jeem, Daal)", "س ش ص ض ز", "م ن و ي ا"),
                    correctIndex = 0,
                    explanationEnglish = "The 5 letters of Qalqalah are Qaaf, Taa, Baa, Jeem, and Daal (ق ط ب ج د).",
                    explanationUrdu = "حروفِ قلقلہ پانچ ہیں: ق، ط، ب، ج، د (قُطْبُ جَدٍّ)۔"
                )
            )
        ),
        NooraniQaidaLesson(
            lessonNumber = 11,
            id = "qaida_lesson_11",
            titleArabic = "الدَّرْسُ الْحَادِي عَشَرَ: تَدْرِيبٌ عَلَى السُّكُونِ",
            titleUrdu = "سبق نمبر ۱۱: تختی مشق سکون (جزم)",
            titleEnglish = "Lesson 11: Exercise on Sukoon (Jazm) in Quranic Words",
            subtitle = "Applying Qalqalah, Izhar, Ikhfa, and Raa Sakinah rules in Quranic words",
            ruleSummaryEnglish = "Practice reading Quranic words containing Sukoon. Pay attention to Qalqalah on ق ط ب ج د, clear pronunciation (Izhar) when Noon Sakinah is followed by throat letters (ء ه ع ح غ خ), and heavy Raa Sakinah after Fatha or Damma.",
            ruleSummaryUrdu = "جزم والے قرآنی الفاظ کی مشق۔ قلقلہ، نون ساکن کا اظہار (حروفِ حلقی سے پہلے) اور را ساکن کے پُر یا باریک ہونے کے قواعد کا خیال رکھیں۔",
            editionNote = "Lesson 11 in classical 17-lesson Haqqani Qaida.",
            practiceItems = listOf(
                QaidaPracticeItem("l11_01", "أَبْقَىٰ", "Abqaa", "Surah Ta-Ha 20:73 — Qalqalah on Baa Sakinah", "اَبْقٰی", "Bounce Baa Sakinah, stretch heavy Qaaf 2 counts.", Pair(20, 73)),
                QaidaPracticeItem("l11_02", "أَطْعَمَهُمْ", "At'amahum", "Surah Quraysh 106:4 — Heavy Qalqalah on Taa Sakinah", "اَطْعَمَهُمْ (سورۃ قریش ۱۰۶:۴)", "Heavy Qalqalah on Taa before 'Ayn.", Pair(106, 4)),
                QaidaPracticeItem("l11_03", "أَنْعَمْتَ", "An'amta", "Surah Al-Fatiha 1:7 — Izhar on Noon Sakinah before 'Ayn & Meem Sakinah before Taa", "اَنْعَمْتَ (سورۃ الفاتحہ ۱:۷)", "Read Noon and Meem clearly without Ghunnah delay.", Pair(1, 7)),
                QaidaPracticeItem("l11_04", "الْحَمْدُ", "Al-Hamdu", "Surah Al-Fatiha 1:2 — Laam Qamariyyah + Meem Sakinah Izhar", "اَلْحَمْدُ (سورۃ الفاتحہ ۱:۲)", "Do not bounce Laam or Meem.", Pair(1, 2)),
                QaidaPracticeItem("l11_05", "خَلَقْنَا", "Khalaqnaa", "Surah At-Tin 95:4 — Heavy Qalqalah on Qaaf Sakinah", "خَلَقْنَا (سورۃ التین ۹۵:۴)", "Distinct bounce on Qaaf before Noon.", Pair(95, 4)),
                QaidaPracticeItem("l11_06", "كَالْعِهْنِ", "Kal-'ihni", "Surah Al-Qari'ah 101:5 — Throat Haa Sakinah", "كَالْعِهْنِ (سورۃ القارعہ ۱۰۱:۵)", "Soft breath on Haa Sakinah.", Pair(101, 5)),
                QaidaPracticeItem("l11_07", "فَصَلِّ لِرَبِّكَ وَانْحَرْ", "Fasalli li-Rabbika wanhar", "Surah Al-Kawthar 108:2 — Izhar of Noon before Haa + heavy Raa on stop", "فَصَلِّ لِرَبِّكَ وَانْحَرْ (سورۃ الکوثر ۱۰۸:۲)", "Clear Noon before Haa, heavy Raa Sakinah.", Pair(108, 2)),
                QaidaPracticeItem("l11_08", "أَلَمْ نَشْرَحْ", "Alam Nashrah", "Surah Ash-Sharh 94:1 — Tafashshi on Sheen Sakinah, Hams on Haa", "اَلَمْ نَشْرَحْ (سورۃ الانشراح ۹۴:۱)", "Spreading breath on Sheen, clean throat Haa.", Pair(94, 1))
            ),
            quizQuestions = listOf(
                QaidaQuizQuestion(
                    id = "q_l11_1",
                    questionEnglish = "In 'أَنْعَمْتَ' (An'amta — Surah Al-Fatiha 1:7), why is Noon Sakinah pronounced clearly without hiding (Izhar)?",
                    questionUrdu = "'أَنْعَمْتَ' میں نونِ ساکن پر غنہ کے بغیر اظہار کیوں کیا جاتا ہے؟",
                    arabicPrompt = "أَنْعَمْتَ",
                    options = listOf("Because it is followed by 'Ayn (ع), one of the 6 throat letters (Huroof-e-Halqi)", "Because it is Qalqalah", "Because of Madd"),
                    correctIndex = 0,
                    explanationEnglish = "When Noon Sakinah or Tanween is followed by any of the 6 throat letters (ء ه ع ح غ خ), the rule is Izhar Halqi (clear pronunciation).",
                    explanationUrdu = "نون ساکن یا تنوین کے بعد حروفِ حلقی (ء ہ ع ح غ خ) میں سے کوئی حرف آئے تو اظہارِ حلقی ہوتا ہے۔"
                )
            )
        ),
        NooraniQaidaLesson(
            lessonNumber = 12,
            id = "qaida_lesson_12",
            titleArabic = "الدَّرْسُ الثَّانِي عَشَرَ: الشَّدَّةُ (التَّشْدِيدُ)",
            titleUrdu = "سبق نمبر ۱۲: تشدید (شدّہ ـّ)",
            titleEnglish = "Lesson 12: Shaddah / Tashdeed (Doubling Sign)",
            subtitle = "Reading a doubled letter firmly & holding Ghunnah on Noon/Meem Mushaddad",
            ruleSummaryEnglish = "A letter with Shaddah (ـّ) is called Mushaddad and represents two identical letters: the first is Sakin and the second is voweled. Join the preceding letter firmly into the Mushaddad letter. IMPORTANT: Whenever Noon (نّ) or Meem (مّ) has Shaddah, hold the nasal sound (Ghunnah Wajib) for 2 counts.",
            ruleSummaryUrdu = "جس حرف پر تشدید (ـّ) ہو اسے مشدد کہتے ہیں۔ مشدد حرف دو حروف کے برابر ہوتا ہے (پہلا ساکن، دوسرا متحرک)۔ اسے پچھلے حرف سے مضبوطی سے ملا کر پڑھیں۔ نون مشدد (نّ) اور میم مشدد (مّ) پر ہمیشہ ایک الف کے برابر غنہ واجب ہے۔",
            editionNote = "Lesson 12 in classical 17-lesson Haqqani Qaida.",
            practiceItems = listOf(
                QaidaPracticeItem("l12_01", "أَبَّ أَبِّ أَبُّ", "Abba - Abbi - Abbu", "Hamzah joined firmly to Baa Mushaddad", "ہمزہ با زبر اَبْ، با زبر بَ = اَبَّ", "Hold Baa firmly without Qalqalah break between the two Baas."),
                QaidaPracticeItem("l12_02", "أَتَّ أَتِّ أَتُّ", "Atta - Atti - Attu", "Taa Mushaddad", "اَتَّ اَتِّ اَتُّ", "Firm dental closure."),
                QaidaPracticeItem("l12_03", "أَنَّ أَنِّ أَنُّ", "Anna - Anni - Annu", "Noon Mushaddad — Mandatory 2-count Ghunnah!", "اَنَّ اَنِّ اَنُّ (غنہ واجب)", "Hold the nasal resonance in the nose for 2 counts."),
                QaidaPracticeItem("l12_04", "أَمَّ أَمِّ أَمُّ", "Amma - Ammi - Ammu", "Meem Mushaddad — Mandatory 2-count Ghunnah!", "اَمَّ اَمِّ اَمُّ (غنہ واجب)", "Hold lips closed with nasal Ghunnah for 2 counts.")
            ),
            quizQuestions = listOf(
                QaidaQuizQuestion(
                    id = "q_l12_1",
                    questionEnglish = "Which two letters ALWAYS require a 2-count nasal Ghunnah when they carry a Shaddah (ـّ)?",
                    questionUrdu = "وہ کونسے دو حروف ہیں جن پر تشدید ہو تو ہمیشہ غنہ کرنا واجب ہوتا ہے؟",
                    arabicPrompt = "إِنَّ • ثُمَّ",
                    options = listOf("Noon Mushaddad (نّ) and Meem Mushaddad (مّ)", "Baa and Taa", "Qaaf and Daal"),
                    correctIndex = 0,
                    explanationEnglish = "Noon Mushaddad (نّ) and Meem Mushaddad (مّ) always carry Ghunnah Wajib for 2 counts.",
                    explanationUrdu = "نون مشدد اور میم مشدد پر ہر حال میں ایک الف (۲ حرکات) کے برابر غنہ واجب ہے۔"
                )
            )
        ),
        NooraniQaidaLesson(
            lessonNumber = 13,
            id = "qaida_lesson_13",
            titleArabic = "الدَّرْسُ الثَّالِثَ عَشَرَ: تَدْرِيبٌ عَلَى الشَّدَّةِ",
            titleUrdu = "سبق نمبر ۱۳: تختی مشق تشدید",
            titleEnglish = "Lesson 13: Exercise on Shaddah in Quranic Words",
            subtitle = "Practicing Mushaddad words, Ghunnah, and Lafz-ul-Jalalah (Allah)",
            ruleSummaryEnglish = "Practice Quranic words containing Shaddah. Rule of Lafz-ul-Jalalah (اللّٰه): The Laam in 'Allah' is read heavy (full mouth) after Fatha or Damma (e.g., هُوَ اللّٰهُ), and light after Kasra (e.g., بِسْمِ اللّٰهِ).",
            ruleSummaryUrdu = "تشدید والے قرآنی الفاظ کی مشق۔ لفظِ اللہ (اللّٰه) کا قاعدہ: لفظِ اللہ کے لام سے پہلے زبر یا پیش ہو تو پُر (موٹا) اور زیر ہو تو باریک پڑھا جاتا ہے۔",
            editionNote = "Lesson 13 in classical 17-lesson Haqqani Qaida.",
            practiceItems = listOf(
                QaidaPracticeItem("l13_01", "إِنَّ", "Inna", "Surah Al-Kawthar 108:3 — Noon Mushaddad with 2-count Ghunnah", "اِنَّ (غنہ کے ساتھ)", "Hold Noon nasal sound for 2 counts.", Pair(108, 3)),
                QaidaPracticeItem("l13_02", "ثُمَّ", "Thumma", "Surah At-Takathur 102:4 — Meem Mushaddad with 2-count Ghunnah", "ثُمَّ (غنہ کے ساتھ)", "Hold Meem nasal sound for 2 counts.", Pair(102, 4)),
                QaidaPracticeItem("l13_03", "رَبِّ", "Rabbi", "Surah Al-Fatiha 1:2 — Baa Mushaddad", "رَبِّ", "Heavy Raa + firm Baa Mushaddad.", Pair(1, 2)),
                QaidaPracticeItem("l13_04", "قُوَّةٍ", "Quwwatin", "Surah At-Tariq 86:10 — Waaw Mushaddad pressed firmly", "قُوَّةٍ", "Round lips firmly on doubled Waaw.", Pair(86, 10)),
                QaidaPracticeItem("l13_05", "سُيِّرَتْ", "Suyyirat", "Surah At-Takwir 81:3 — Yaa Mushaddad", "سُيِّرَتْ (سورۃ التکویر ۸۱:۳)", "Press middle tongue firmly on Yaa.", Pair(81, 3)),
                QaidaPracticeItem("l13_06", "تَبَّتْ", "Tabbat", "Surah Al-Masad 111:1 — Baa Mushaddad + Taa Sakinah", "تَبَّتْ (سورۃ المسد ۱۱۱:۱)", "Firm Baa, gentle breath on final Taa.", Pair(111, 1)),
                QaidaPracticeItem("l13_07", "الْجَنَّةِ", "Al-Jannati", "Surah An-Nas 114:6 — Noon Mushaddad with Ghunnah", "الْجَنَّةِ (سورۃ الناس ۱۱۴:۶)", "2-count Ghunnah on Noon.", Pair(114, 6))
            ),
            quizQuestions = listOf(
                QaidaQuizQuestion(
                    id = "q_l13_1",
                    questionEnglish = "When is the Laam of Lafz-ul-Jalalah (اللّٰه — Allah) pronounced light (Tarqeeq)?",
                    questionUrdu = "لفظِ اللہ (اللّٰه) کا لام کب باریک پڑھا جاتا ہے؟",
                    arabicPrompt = "بِسْمِ اللّٰهِ",
                    options = listOf("When preceded by a Kasra (Zer), as in بِسْمِ اللّٰهِ", "When preceded by a Fatha (Zabar)", "When preceded by a Damma (Pesh)"),
                    correctIndex = 0,
                    explanationEnglish = "The Laam of Allah is read light when preceded by Kasra (e.g., Bismillahi) and heavy when preceded by Fatha or Damma (e.g., Wallahu, Rasulullah).",
                    explanationUrdu = "لفظِ اللہ کے لام سے پہلے زیر ہو (جیسے بِسْمِ اللّٰهِ) تو لام باریک پڑھا جاتا ہے، اور زبر یا پیش ہو تو پُر۔"
                )
            )
        ),
        NooraniQaidaLesson(
            lessonNumber = 14,
            id = "qaida_lesson_14",
            titleArabic = "الدَّرْسُ الرَّابِعَ عَشَرَ: الشَّدَّةُ مَعَ السُّكُونِ",
            titleUrdu = "سبق نمبر ۱۴: تشدید مع سکون (شمس و قمر کے حروف)",
            titleEnglish = "Lesson 14: Shaddah with Sukoon (Solar Letters & Joined Clusters)",
            subtitle = "Joining across silent letters (Huroof-e-Shamsiyyah) into a Mushaddad letter followed by Sukoon",
            ruleSummaryEnglish = "When a voweled letter joins a Mushaddad letter (as in Solar Letters / Huroof-e-Shamsiyyah where the 'Al-' Laam is silent) and is followed by a Sakin letter, drop the unvoweled intermediate letters in pronunciation and connect directly.",
            ruleSummaryUrdu = "جب کسی مشدد حرف سے پہلے حروفِ شمسیہ کا لام آئے تو لام نہیں پڑھا جاتا بلکہ پچھلے متحرک حرف کو براہِ راست مشدد حرف سے ملایا جاتا ہے اور آگے ساکن حرف کو بھی ملا کر پڑھا جاتا ہے۔",
            editionNote = "Lesson 14 in classical 17-lesson Haqqani Qaida.",
            practiceItems = listOf(
                QaidaPracticeItem("l14_01", "وَالشَّمْسِ", "Wash-shamsi", "Surah Ash-Shams 91:1 — Waaw joins Sheen Mushaddad, then Meem Sakinah", "وَالشَّمْسِ (سورۃ الشمس ۹۱:۱)", "Alif and Laam are silent; connect Waaw directly to Sheen.", Pair(91, 1)),
                QaidaPracticeItem("l14_02", "وَاللَّيْلِ", "Wal-layli", "Surah Al-Layl 92:1 — Laam Mushaddad + Yaa Leen", "وَاللَّيْلِ (سورۃ اللیل ۹۲:۱)", "Connect Waaw to Laam Mushaddad, then Yaa Sakinah.", Pair(92, 1)),
                QaidaPracticeItem("l14_03", "وَالنَّجْمِ", "Wan-najmi", "Surah An-Najm 53:1 — Noon Mushaddad (Ghunnah) + Jeem Sakinah (Qalqalah)", "وَالنَّجْمِ (سورۃ النجم ۵۳:۱)", "Hold Ghunnah on Noon, then bounce Jeem Sakinah.", Pair(53, 1)),
                QaidaPracticeItem("l14_04", "وَالصُّبْحِ", "Was-subhi", "Surah At-Takwir 81:18 — Heavy Saad Mushaddad + Baa Qalqalah", "وَالصُّبْحِ (سورۃ التکویر ۸۱:۱۸)", "Heavy Saad followed by crisp Qalqalah on Baa.", Pair(81, 18)),
                QaidaPracticeItem("l14_05", "فَانْصَبْ", "Fansab", "Surah Ash-Sharh 94:7 — Ikhfa on Noon before Saad + Qalqalah on Baa", "فَانْصَبْ (سورۃ الانشراح ۹۴:۷)", "Heavy Ikhfa before Saad.", Pair(94, 7))
            ),
            quizQuestions = listOf(
                QaidaQuizQuestion(
                    id = "q_l14_1",
                    questionEnglish = "In 'وَالشَّمْسِ' (Wash-shamsi), why is the Laam of 'ال' not pronounced?",
                    questionUrdu = "'وَالشَّمْسِ' میں الف اور لام کیوں نہیں پڑھے جاتے؟",
                    arabicPrompt = "وَالشَّمْسِ",
                    options = listOf("Because Sheen (ش) is a Solar Letter (Harf Shamsi), so the Laam merges into Sheen", "Because of Qalqalah", "Because Sheen is a throat letter"),
                    correctIndex = 0,
                    explanationEnglish = "Before the 14 Solar Letters (Huroof-e-Shamsiyyah), the definite article Laam assimilates into the following letter, which takes a Shaddah.",
                    explanationUrdu = "ش حروفِ شمسیہ میں سے ہے، اس لیے ال کا لام ش میں مدغم ہو جاتا ہے اور و براہِ راست ش مشدد سے ملتا ہے۔"
                )
            )
        ),
        NooraniQaidaLesson(
            lessonNumber = 15,
            id = "qaida_lesson_15",
            titleArabic = "الدَّرْسُ الْخَامِسَ عَشَرَ: شَدَّتَانِ وَالشَّدَّةُ بَعْدَ الْمَدِّ",
            titleUrdu = "سبق نمبر ۱۵: تشدید مع تشدید اور مدِ لازم کلمی مثقل",
            titleEnglish = "Lesson 15: Two Consecutive Shaddahs & Shaddah After Madd",
            subtitle = "Pronouncing back-to-back Mushaddad letters & 6-count Madd Lazim Kalimi",
            ruleSummaryEnglish = "When two Mushaddad letters appear in sequence, give each its full doubling and any required Ghunnah. When a Madd letter is followed immediately by a Mushaddad letter in the same word (Madd Lazim Kalimi Muthaqqal, as in الضَّآلِّينَ), stretch the Madd for 6 counts (3 Alifs) before joining the Shaddah.",
            ruleSummaryUrdu = "جب دو مشدد حروف یکے بعد دیگرے آئیں تو دونوں کی تشدید اور غنہ پورا ادا کریں۔ اور جب حرفِ مدہ کے بعد مشدد حرف ہو (جیسے الضَّآلِّينَ) تو یہ مدِ لازم کلمی مثقل ہے، اسے ۳ الف (۶ حرکات) کھینچ کر مشدد حرف سے ملائیں۔",
            editionNote = "Lesson 15 in classical 17-lesson Haqqani Qaida (split across Lessons 15–17 in 21-lesson web editions).",
            practiceItems = listOf(
                QaidaPracticeItem("l15_01", "مُزَّمِّلُ", "Muzzammilu", "Surah Al-Muzzammil 73:1 — Zaa Mushaddad followed by Meem Mushaddad (Ghunnah)", "مُزَّمِّلُ (سورۃ المزمل ۷۳:۱)", "Firm Zaa, then 2-count Ghunnah on Meem Mushaddad.", Pair(73, 1)),
                QaidaPracticeItem("l15_02", "مُدَّثِّرُ", "Muddaththiru", "Surah Al-Muddaththir 74:1 — Daal Mushaddad + Thaa Mushaddad", "مُدَّثِّرُ (سورۃ المدثر ۷۴:۱)", "Firm Daal followed by soft interdental Thaa Mushaddad.", Pair(74, 1)),
                QaidaPracticeItem("l15_03", "عِلِّيِّينَ", "'Illiyyeena", "Surah Al-Mutaffifin 83:18 — Laam Mushaddad + Yaa Mushaddad", "عِلِّيِّيْنَ (سورۃ المطففین ۸۳:۱۸)", "Press both Laam and Yaa firmly.", Pair(83, 18)),
                QaidaPracticeItem("l15_04", "وَلَا الضَّآلِّينَ", "Walad-daalleen", "Surah Al-Fatiha 1:7 — Heavy Daad + 6-count Madd Lazim + Laam Mushaddad", "وَلَا الضَّآلِّيْنَ (سورۃ الفاتحہ ۱:۷ — مدِ لازم ۶ حرکات)", "Stretch Daad-Alif 6 full counts, then press Laam Mushaddad.", Pair(1, 7)),
                QaidaPracticeItem("l15_05", "الْحَآقَّةُ", "Al-Haaqqatu", "Surah Al-Haqqah 69:1 — 6-count Madd Lazim before Qaaf Mushaddad", "اَلْحَآقَّةُ (سورۃ الحاقہ ۶۹:۱)", "Stretch Haa-Alif 6 counts, then firm heavy Qaaf.", Pair(69, 1)),
                QaidaPracticeItem("l15_06", "الصَّآخَّةُ", "As-Saakhkhatu", "Surah 'Abasa 80:33 — 6-count Madd Lazim before Khaa Mushaddad", "اَلصَّآخَّةُ (سورۃ عبس ۸۰:۳۳)", "Heavy Saad stretched 6 counts into heavy Khaa.", Pair(80, 33))
            ),
            quizQuestions = listOf(
                QaidaQuizQuestion(
                    id = "q_l15_1",
                    questionEnglish = "In 'وَلَا الضَّآلِّينَ' (Surah Al-Fatiha 1:7), how many counts is the Alif after Daad elongated?",
                    questionUrdu = "سورۃ الفاتحہ کے لفظ 'الضَّآلِّينَ' میں ض کے بعد الف کو کتنی حرکات کھینچا جاتا ہے؟",
                    arabicPrompt = "وَلَا الضَّآلِّينَ",
                    options = listOf("6 counts (Madd Lazim Kalimi Muthaqqal)", "2 counts", "1 count"),
                    correctIndex = 0,
                    explanationEnglish = "Because the Alif Maddah is followed by a Mushaddad letter (Laam) in the same word, it is Madd Lazim Kalimi Muthaqqal and must be elongated 6 counts.",
                    explanationUrdu = "حرفِ مدہ کے بعد اسی کلمے میں مشدد حرف آنے کی وجہ سے یہ مدِ لازم کلمی مثقل ہے جسے ۶ حرکات کھینچنا لازم ہے۔"
                )
            )
        ),
        NooraniQaidaLesson(
            lessonNumber = 16,
            id = "qaida_lesson_16",
            titleArabic = "الدَّرْسُ السَّادِسَ عَشَرَ: أَحْكَامُ التَّجْوِيدِ وَعَلَامَاتُ الْوَقْفِ",
            titleUrdu = "سبق نمبر ۱۶: قواعدِ تجوید (ادغام، اقلاب، اخفاء، اظہار) اور علاماتِ وقف",
            titleEnglish = "Lesson 16: Core Tajweed Rules (Noon Sakinah/Tanween, Meem Sakinah) & Waqf Signs",
            subtitle = "Izhar, Idgham (Yarmaloon), Iqlab, Ikhfa & Quranic Stopping Signs (Rumooz-e-Awqaf)",
            ruleSummaryEnglish = "Four rules of Noon Sakinah & Tanween: (1) Izhar before 6 throat letters (ء ه ع ح غ خ); (2) Idgham (merging) before 6 letters of 'Yarmaloon' (ي ر م ل و ن — with Ghunnah on ي م و ن and without Ghunnah on ل ر); (3) Iqlab (turning into Meem with Ghunnah) before ب, marked by a small م; (4) Ikhfa (nasal hiding for 2 counts) before the remaining 15 letters. Also learn the Quranic Waqf (stopping) signs: مـ (Mandatory stop), ط (Absolute stop), ج (Permissible), لا (Do not stop mid-phrase).",
            ruleSummaryUrdu = "نون ساکن اور تنوین کے ۴ قواعد: (۱) اظہارِ حلقی (ء ہ ع ح غ خ سے پہلے بغیر غنہ صاف پڑھنا)؛ (۲) ادغام ('یَرْمَلُوْنَ' کے حروف سے پہلے ملانا: ی م و ن میں غنہ کے ساتھ، ل اور ر میں بغیر غنہ)؛ (۳) اقلاب (ب سے پہلے نون کو چھوٹی م سے بدل کر غنہ کرنا)؛ (۴) اخفاء (باقی ۱۵ حروف سے پہلے ناک میں آواز چھپا کر غنہ کرنا)۔ علاماتِ وقف: 〇 (آیت کا اختتام)، مـ (وقفِ لازم)، ط (وقفِ مطلق)، ج (وقفِ جائز)۔",
            editionNote = "Lesson 16 in classical 17-lesson Haqqani Qaida (corresponds to Lessons 16–20 in 21-lesson web editions).",
            practiceItems = listOf(
                QaidaPracticeItem("l16_01", "مِنْ بَعْدِ", "Mim-ba'di (Iqlab)", "Surah Al-Bayyinah 98:4 — Noon Sakinah before Baa turns into Meem with 2-count Ghunnah", "مِنْ بَعْدِ (اقلاب — نون کو میم سے بدل کر غنہ کریں)", "Notice the miniature Meem over Noon.", Pair(98, 4)),
                QaidaPracticeItem("l16_02", "سَمِيعٌ بَصِيرٌ", "Samee'um-Baseer (Iqlab)", "Surah Al-Hajj 22:75 — Tanween before Baa turns into Meem with Ghunnah", "سَمِيْعٌۢ بَصِيْرٌ (اقلاب)", "Gentle labial contact with 2-count Ghunnah.", Pair(22, 75)),
                QaidaPracticeItem("l16_03", "مَنْ يَقُولُ", "May-yaqoolu (Idgham bi-Ghunnah)", "Surah Al-Baqarah 2:8 — Noon merges into Yaa with 2-count Ghunnah", "مَنْ يَّقُوْلُ (ادغام مع الغنہ)", "Merge Noon into Yaa with nasal resonance.", Pair(2, 8)),
                QaidaPracticeItem("l16_04", "مِنْ رَبِّهِمْ", "Mir-Rabbihim (Idgham bila Ghunnah)", "Surah Al-Baqarah 2:5 — Noon merges completely into Raa WITHOUT Ghunnah", "مِنْ رَّبِّهِمْ (ادغام بلا غنہ)", "No nasal sound on Raa; merge cleanly.", Pair(2, 5)),
                QaidaPracticeItem("l16_05", "مِنْ لَدُنْهُ", "Mil-ladunhu (Idgham bila Ghunnah)", "Surah Al-Kahf 18:2 — Noon merges completely into Laam WITHOUT Ghunnah", "مِنْ لَّدُنْهُ (ادغام بلا غنہ)", "Merge directly into Laam.", Pair(18, 2)),
                QaidaPracticeItem("l16_06", "مِنْ شَرِّ", "Min-sharri (Ikhfa Haqiqi)", "Surah Al-Falaq 113:2 — Hide Noon in the nose for 2 counts before Sheen", "مِنْ شَرِّ (سورۃ الفلق ۱۱۳:۲ — اخفاء حقیقی)", "2-count light nasal Ikhfa.", Pair(113, 2)),
                QaidaPracticeItem("l16_07", "أَنْتُمْ", "Antum (Ikhfa)", "Surah Al-Kafirun 109:3 — Noon Sakinah before Taa has 2-count Ikhfa", "اَنْتُمْ (اخفاء)", "Do not press tongue tip onto Taa during the Ghunnah.", Pair(109, 3)),
                QaidaPracticeItem("l16_08", "تَرْمِيهِمْ بِحِجَارَةٍ", "Tarmeehim-bi-hijarah (Ikhfa Shafawi)", "Surah Al-Fil 105:4 — Meem Sakinah before Baa has labial Ghunnah", "تَرْمِيْهِمْ بِحِجَارَةٍ (سورۃ الفیل ۱۰۵:۴ — اخفاء شفوی)", "Hold Meem before Baa for 2 counts.", Pair(105, 4))
            ),
            quizQuestions = listOf(
                QaidaQuizQuestion(
                    id = "q_l16_1",
                    questionEnglish = "What is the Tajweed rule when Noon Sakinah or Tanween is followed by the letter Baa (ب), as in 'مِنْ بَعْدِ'?",
                    questionUrdu = "نون ساکن یا تنوین کے بعد حرف 'ب' آئے (جیسے مِنْ بَعْدِ) تو کون سا قاعدہ لاگو ہوتا ہے؟",
                    arabicPrompt = "مِنْ بَعْدِ",
                    options = listOf("Iqlab (converting Noon into Meem with Ghunnah)", "Izhar Halqi", "Idgham without Ghunnah"),
                    correctIndex = 0,
                    explanationEnglish = "Before Baa (ب), Noon Sakinah or Tanween undergoes Iqlab — it is converted into a hidden Meem with 2 counts of Ghunnah.",
                    explanationUrdu = "نون ساکن یا تنوین کے بعد 'ب' آئے تو اقلاب ہوتا ہے یعنی نون کو میم سے بدل کر غنہ کے ساتھ پڑھا جاتا ہے۔"
                ),
                QaidaQuizQuestion(
                    id = "q_l16_2",
                    questionEnglish = "In which two letters of 'Yarmaloon' (يرملون) does Idgham take place WITHOUT Ghunnah?",
                    questionUrdu = "'یَرْمَلُوْنَ' کے کن دو حروف میں بغیر غنہ کے ادغام (ادغام بلا غنہ) ہوتا ہے؟",
                    arabicPrompt = "مِنْ رَبِّهِمْ • مِنْ لَدُنْهُ",
                    options = listOf("Laam (ل) and Raa (ر)", "Yaa (ي) and Waaw (و)", "Meem (م) and Noon (ن)"),
                    correctIndex = 0,
                    explanationEnglish = "Idgham bila Ghunnah (complete merging without nasalization) occurs before Laam (ل) and Raa (ر).",
                    explanationUrdu = "لام (ل) اور را (ر) میں ادغام بلا غنہ (بغیر غنہ کے کامل ادغام) ہوتا ہے۔"
                )
            )
        ),
        NooraniQaidaLesson(
            lessonNumber = 17,
            id = "qaida_lesson_17",
            titleArabic = "الدَّرْسُ السَّابِعَ عَشَرَ: تَطْبِيقُ التِّلَاوَةِ الْقُرْآنِيَّةِ وَخَتْمُ الْقَاعِدَةِ",
            titleUrdu = "سبق نمبر ۱۷: مشقِ تلاوتِ قرآن مجید و تکمیلِ نورانی قاعدہ",
            titleEnglish = "Lesson 17: Complete Quran Reading Practice & Waqf Application",
            subtitle = "Applying all 16 lessons of Noorani Qaida directly on canonical Quranic verses",
            ruleSummaryEnglish = "Congratulations on reaching the final lesson of Noorani Qaida! Practice reading complete Quranic verses with full Tajweed: observe Harakat, Madd (2, 4, and 6 counts), Qalqalah, Ghunnah on Noon/Meem Mushaddad, Izhar/Idgham/Iqlab/Ikhfa, Lafz-ul-Jalalah, and proper Waqf (turning final short vowels into Sukoon and final round Taa ة into Haa Sakinah هْ when stopping at an ayah end).",
            ruleSummaryUrdu = "نورانی قاعدہ کے آخری سبق میں سابقہ تمام ۱۶ اسباق کے قواعد (حرکات، مدود، قلقلہ، غنہ، اظہار، ادغام، اقلاب، اخفاء اور قواعدِ وقف) کا عملی اطلاق قرآن مجید کی آیات پر کریں۔ وقف کرتے وقت آخری حرف کو ساکن کریں، دو زبر کو الف مدہ سے بدلیں، اور گول تا (ة) کو ہا ساکنہ (هْ) پڑھیں۔",
            editionNote = "Lesson 17 in classical 17-lesson Haqqani Qaida (Lesson 21 in expanded web editions). Audio plays authentic Quran reciter.",
            practiceItems = listOf(
                QaidaPracticeItem("l17_01", "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيمِ", "Bismillaahir-Rahmaanir-Raheem", "Surah Al-Fatiha 1:1 — Light Laam in Bismillah, Heavy Raa, Standing Fatha on Meem, Madd 'Arid lis-Sukoon on stop", "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ (۱:۱)", "Tap Play to hear authentic Quranic recitation.", Pair(1, 1)),
                QaidaPracticeItem("l17_02", "الْحَمْدُ لِلّٰهِ رَبِّ الْعَالَمِينَ", "Al-hamdu lillaahi Rabbil-'aalameen", "Surah Al-Fatiha 1:2 — Laam Qamariyyah, Light Laam in Lillah, Heavy Raa, Baa Mushaddad", "اَلْحَمْدُ لِلّٰهِ رَبِّ الْعٰلَمِيْنَ (۱:۲)", "Stop on Noon Sakinah with 2–4–6 count Madd 'Arid.", Pair(1, 2)),
                QaidaPracticeItem("l17_03", "قُلْ هُوَ اللّٰهُ أَحَدٌ", "Qul Huwallaahu Ahad", "Surah Al-Ikhlas 112:1 — Heavy Qaaf, Heavy Laam in Allah (after Fatha), Qalqalah Kubra on Daal when stopping", "قُلْ هُوَ اللّٰهُ اَحَدٌ (۱۱۲:۱)", "When stopping on أَحَدٌ, drop Tanween and bounce Daal (Qalqalah).", Pair(112, 1)),
                QaidaPracticeItem("l17_04", "اللّٰهُ الصَّمَدُ", "Allaahus-Samad", "Surah Al-Ikhlas 112:2 — Heavy Allah, Solar Saad Mushaddad, Qalqalah on Daal", "اَللّٰهُ الصَّمَدُ (۱۱۲:۲)", "Connect Haa of Allah directly to heavy Saad Mushaddad.", Pair(112, 2)),
                QaidaPracticeItem("l17_05", "لَمْ يَلِدْ وَلَمْ يُولَدْ", "Lam yalid wa lam yoolad", "Surah Al-Ikhlas 112:3 — Meem Sakinah Izhar, Qalqalah on both Daals", "لَمْ يَلِدْ وَلَمْ يُوْلَدْ (۱۱۲:۳)", "Crisp Qalqalah on yalid and yoolad.", Pair(112, 3)),
                QaidaPracticeItem("l17_06", "وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ", "Wa lam yakul-lahoo kufuwan ahad", "Surah Al-Ikhlas 112:4 — Idgham bila Ghunnah (yakul-lahoo), Izhar of Tanween before Hamzah, Qalqalah on stop", "وَلَمْ يَكُنْ لَّهٗ كُفُوًا اَحَدٌ (۱۱۲:۴)", "Complete Tajweed synthesis in a single verse!", Pair(112, 4)),
                QaidaPracticeItem("l17_07", "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", "Innaa a'taynaakal-Kawthar", "Surah Al-Kawthar 108:1 — Noon Mushaddad Ghunnah, Madd Munfasil, Yaa Leen, Waaw Leen, Heavy Raa on stop", "اِنَّآ اَعْطَيْنٰكَ الْكَوْثَرَ (۱۰۸:۱)", "Hold Ghunnah on Innaa and stretch Madd 4 counts.", Pair(108, 1))
            ),
            quizQuestions = listOf(
                QaidaQuizQuestion(
                    id = "q_l17_1",
                    questionEnglish = "When stopping (Waqf) at the end of 'قُلْ هُوَ اللّٰهُ أَحَدٌ' (Surah Al-Ikhlas 112:1), how is the final letter 'دٌ' pronounced?",
                    questionUrdu = "'قُلْ هُوَ اللّٰهُ أَحَدٌ' پر وقف کرتے وقت آخری حرف 'دٌ' کو کیسے پڑھا جائے گا؟",
                    arabicPrompt = "قُلْ هُوَ اللّٰهُ أَحَدٌ 〇",
                    options = listOf("The Tanween drops and Daal becomes Sakin with Qalqalah (Ahadْ)", "The two Pesh are still pronounced", "It turns into Haa Sakinah"),
                    correctIndex = 0,
                    explanationEnglish = "On Waqf, Tanween (except two Zabar) is dropped and the final letter becomes Sakin; since Daal is a Qalqalah letter, it is read with Qalqalah Kubra.",
                    explanationUrdu = "وقف کی حالت میں دو پیش ساقط ہو جاتے ہیں اور دال ساکن ہو کر قلقلہ کبریٰ کے ساتھ 'اَحَدْ' پڑھا جاتا ہے۔"
                )
            )
        )
    )

    /**
     * Nimaz-e-Mutarjam Part 1: Kalima Tayyibah, Kalima Shahadah, all Six Kalimas,
     * Iman-e-Mujmal, Iman-e-Mufassal, 5 Pillars of Islam & 6 Articles of Faith.
     */
    val imanAndKalimaItems: List<ImanAndKalimaItem> = listOf(
        ImanAndKalimaItem(
            id = "kalima_1_tayyibah",
            orderNumber = 1,
            category = "SIX_KALIMAS",
            titleArabic = "الْكَلِمَةُ الطَّيِّبَةُ (١)",
            titleUrdu = "پہلا کلمہ طیب (پاک کلام)",
            titleEnglish = "1st Kalima: Kalima Tayyibah (The Word of Purity)",
            arabicText = "لَا إِلٰهَ إِلَّا اللّٰهُ مُحَمَّدٌ رَسُولُ اللّٰهِ",
            transliteration = "Laa ilaaha illallaahu Muhammadur-Rasoolullaah",
            urduTranslation = "اللہ کے سوا کوئی معبود نہیں، حضرت محمد ﷺ اللہ کے رسول ہیں۔",
            englishTranslation = "There is no deity worthy of worship except Allah, and Muhammad ﷺ is the Messenger of Allah.",
            authenticReference = "Quran 37:35 & 47:19 (لَا إِلٰهَ إِلَّا اللّٰهُ); Quran 48:29 (مُحَمَّدٌ رَسُولُ اللّٰهِ); Sahih al-Bukhari 8; Sahih Muslim 16.",
            scholarlyAndCurriculumNote = "The two clauses are verbatim Quranic statements (37:35, 48:29) and form the core creed of Islam taught first in traditional Darasgah/Madrasah curricula.",
            humanAudioUrl = "https://raw.githubusercontent.com/islamicapi/masnun-dua/main/audio/89.mp3"
        ),
        ImanAndKalimaItem(
            id = "kalima_2_shahadah",
            orderNumber = 2,
            category = "SIX_KALIMAS",
            titleArabic = "كَلِمَةُ الشَّهَادَةِ (٢)",
            titleUrdu = "دوسرا کلمہ شہادت (گواہی کا کلام)",
            titleEnglish = "2nd Kalima: Kalima Shahadah (The Testimony of Faith)",
            arabicText = "أَشْهَدُ أَنْ لَا إِلٰهَ إِلَّا اللّٰهُ وَحْدَهُ لَا شَرِيكَ لَهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
            transliteration = "Ash-hadu al-laa ilaaha illallaahu wahdahoo laa shareeka lahoo, wa ash-hadu anna Muhammadan 'abduhoo wa rasooluh",
            urduTranslation = "میں گواہی دیتا ہوں کہ اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے اس کا کوئی شریک نہیں، اور میں گواہی دیتا ہوں کہ حضرت محمد ﷺ اس کے بندے اور رسول ہیں۔",
            englishTranslation = "I bear witness that there is no deity worthy of worship except Allah alone, without partner, and I bear witness that Muhammad ﷺ is His servant and Messenger.",
            authenticReference = "Sahih al-Bukhari 8; Sahih Muslim 234 (also recited after Wudu in Sahih Muslim 234 and in Tashahhud of every Salah).",
            scholarlyAndCurriculumNote = "Directly established in Mutawatir Hadith as the formal testimony of Islam, Tashahhud, Adhan response, and post-Wudu supplication.",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n13.mp3"
        ),
        ImanAndKalimaItem(
            id = "kalima_3_tamjeed",
            orderNumber = 3,
            category = "SIX_KALIMAS",
            titleArabic = "كَلِمَةُ التَّمْجِيدِ (٣)",
            titleUrdu = "تیسرا کلمہ تمجید (بزرگی بیان کرنے کا کلام)",
            titleEnglish = "3rd Kalima: Kalima Tamjeed (The Word of Glorification)",
            arabicText = "سُبْحَانَ اللّٰهِ وَالْحَمْدُ لِلّٰهِ وَلَا إِلٰهَ إِلَّا اللّٰهُ وَاللّٰهُ أَكْبَرُ وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللّٰهِ الْعَلِيِّ الْعَظِيمِ",
            transliteration = "Subhaanallaahi wal-hamdu lillaahi wa laa ilaaha illallaahu wallaahu akbar, wa laa hawla wa laa quwwata illaa billaahil-'Aliyyil-'Azeem",
            urduTranslation = "اللہ پاک ہے، اور سب تعریف اللہ ہی کے لیے ہے، اور اللہ کے سوا کوئی معبود نہیں، اور اللہ سب سے بڑا ہے، اور گناہوں سے بچنے کی طاقت اور نیکی کرنے کی قوت اللہ ہی کی طرف سے ہے جو بہت بلند، عظمت والا ہے۔",
            englishTranslation = "Glory be to Allah, all praise is due to Allah, there is no deity except Allah, and Allah is the Greatest; there is no power and no strength except with Allah, the Most High, the Supreme.",
            authenticReference = "Sahih Muslim 2137 (Al-Baqiyat as-Salihat); Sahih al-Bukhari 6409; Sunan Abi Dawud 832.",
            scholarlyAndCurriculumNote = "Combines the four most beloved phrases to Allah (Sahih Muslim 2137) with the treasure of Paradise ('La hawla wa la quwwata illa billah').",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n253.mp3"
        ),
        ImanAndKalimaItem(
            id = "kalima_4_tawheed",
            orderNumber = 4,
            category = "SIX_KALIMAS",
            titleArabic = "كَلِمَةُ التَّوْحِيدِ (٤)",
            titleUrdu = "چوتھا کلمہ توحید (یکتائی کا کلام)",
            titleEnglish = "4th Kalima: Kalima Tawheed (The Word of Oneness)",
            arabicText = "لَا إِلٰهَ إِلَّا اللّٰهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ يُحْيِي وَيُمِيتُ وَهُوَ حَيٌّ لَا يَمُوتُ أَبَدًا أَبَدًا، ذُو الْجَلَالِ وَالْإِكْرَامِ، بِيَدِهِ الْخَيْرُ، وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "Laa ilaaha illallaahu wahdahoo laa shareeka lahoo, lahul-mulku wa lahul-hamdu yuhyee wa yumeetu wa Huwa Hayyul-laa yamootu abadan abadaa, Dhul-Jalaali wal-Ikraam, biyadihil-khayru wa Huwa 'alaa kulli shay'in Qadeer",
            urduTranslation = "اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے اس کا کوئی شریک نہیں، اسی کی بادشاہی ہے اور اسی کے لیے تمام تعریف ہے، وہی زندہ کرتا ہے اور مارتا ہے اور وہ ہمیشہ زندہ ہے اسے کبھی موت نہیں آئے گی، بڑائی اور بزرگی والا ہے، اسی کے ہاتھ میں ہر بھلائی ہے اور وہ ہر چیز پر قادر ہے۔",
            englishTranslation = "There is no deity except Allah alone, without partner. To Him belongs sovereignty and all praise; He gives life and causes death, and He is Ever-Living and never dies. Possessor of Majesty and Honor; in His hand is all good, and He has power over all things.",
            authenticReference = "Sahih al-Bukhari 3293 & Sahih Muslim 2691 (core Tahlil); Jami' at-Tirmidhi 3428; Quran 3:26 & 55:27.",
            scholarlyAndCurriculumNote = "Synthesizes authentic Prophetic Adhkar of Tawheed with Quranic attributes from Surah Aal-e-Imran (3:26) and Surah Ar-Rahman (55:27).",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n209.mp3"
        ),
        ImanAndKalimaItem(
            id = "kalima_5_istighfar",
            orderNumber = 5,
            category = "SIX_KALIMAS",
            titleArabic = "كَلِمَةُ الِاسْتِغْفَارِ (٥)",
            titleUrdu = "پانچواں کلمہ استغفار (توبہ و بخشش مانگنے کا کلام)",
            titleEnglish = "5th Kalima: Kalima Istighfar (Seeking Forgiveness)",
            arabicText = "أَسْتَغْفِرُ اللّٰهَ رَبِّي مِنْ كُلِّ ذَنْبٍ أَذْنَبْتُهُ عَمَدًا أَوْ خَطَأً سِرًّا أَوْ عَلَانِيَةً وَأَتُوبُ إِلَيْهِ مِنَ الذَّنْبِ الَّذِي أَعْلَمُ وَمِنَ الذَّنْبِ الَّذِي لَا أَعْلَمُ، إِنَّكَ أَنْتَ عَلَّامُ الْغُيُوبِ وَسَتَّارُ الْعُيُوبِ وَغَفَّارُ الذُّنُوبِ وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللّٰهِ الْعَلِيِّ الْعَظِيمِ",
            transliteration = "Astaghfirullaaha Rabbee min kulli dhanbin adhnabtuhoo 'amadan aw khata'an sirran aw 'alaaniyatan wa atoobu ilayhi minadh-dhanbilladhee a'lamu wa minadh-dhanbilladhee laa a'lamu, innaka Anta 'Allaamul-ghuyoobi wa Sattaarul-'uyoobi wa Ghaffaarudh-dhunoobi wa laa hawla wa laa quwwata illaa billaahil-'Aliyyil-'Azeem",
            urduTranslation = "میں اپنے رب اللہ سے ہر اس گناہ کی معافی مانگتا ہوں جو میں نے جان بوجھ کر کیا یا بھول چوک سے، چھپ کر کیا یا کھلم کھلا، اور میں اس کی بارگاہ میں توبہ کرتا ہوں اس گناہ سے بھی جسے میں جانتا ہوں اور اس گناہ سے بھی جسے میں نہیں جانتا۔ بے شک تو غیبوں کا خوب جاننے والا، عیبوں کو چھپانے والا اور گناہوں کو بخشنے والا ہے، اور گناہ سے بچنے کی طاقت اور نیکی کرنے کی قوت اللہ ہی کی طرف سے ہے جو بہت بلند عظمت والا ہے۔",
            englishTranslation = "I seek forgiveness from Allah, my Lord, for every sin I committed knowingly or unknowingly, secretly or openly, and I turn in repentance to Him for the sin I know and the sin I do not know. Truly You are the Knower of the unseen, the Concealer of faults, and the Forgiver of sins; and there is no power and no strength except with Allah, the Most High, the Supreme.",
            authenticReference = "Based on Quran 5:109 (عَلَّامُ الْغُيُوبِ), Musnad Ahmad 4/403 & Al-Adab Al-Mufrad 716 (seeking refuge from known and unknown sins); compiled in traditional Darasgah primers.",
            scholarlyAndCurriculumNote = "Scholarly Transparency Note: While the individual concepts and phrases derive from the Quran and Sunnah, the exact combined paragraph of the 5th and 6th Kalimas is a classical South Asian educational compilation (Nimaz-e-Mutarjam curriculum) rather than a single verbatim Prophetic Hadith. For the highest-graded verbatim Prophetic Istighfar, see Sayyidul-Istighfar (Sahih al-Bukhari 6306).",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n203.mp3"
        ),
        ImanAndKalimaItem(
            id = "kalima_6_radde_kufr",
            orderNumber = 6,
            category = "SIX_KALIMAS",
            titleArabic = "كَلِمَةُ رَدِّ الْكُفْرِ (٦)",
            titleUrdu = "چھٹا کلمہ ردِّ کفر (کفر و شرک سے بیزاری کا کلام)",
            titleEnglish = "6th Kalima: Kalima Radd-e-Kufr (Refutation of Disbelief & Shirk)",
            arabicText = "اَللّٰهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ أَنْ أُشْرِكَ بِكَ شَيْئًا وَأَنَا أَعْلَمُ بِهِ وَأَسْتَغْفِرُكَ لِمَا لَا أَعْلَمُ بِهِ، تُبْتُ عَنْهُ وَتَبَرَّأْتُ مِنَ الْكُفْرِ وَالشِّرْكِ وَالْكَذِبِ وَالْغِيبَةِ وَالْبِدْعَةِ وَالنَّمِيمَةِ وَالْفَوَاحِشِ وَالْبُهْتَانِ وَالْمَعَاصِي كُلِّهَا، وَأَسْلَمْتُ وَأَقُولُ لَا إِلٰهَ إِلَّا اللّٰهُ مُحَمَّدٌ رَسُولُ اللّٰهِ",
            transliteration = "Allaahumma innee a'oodhu bika min an ushrika bika shay'an wa ana a'lamu bihee wa astaghfiruka limaa laa a'lamu bihee, tubtu 'anhu wa tabarra'tu minal-kufri wash-shirki wal-kadhibi wal-gheebati wal-bid'ati wan-nameemati wal-fawaahishi wal-buhtaani wal-ma'aasee kullihaa, wa aslamtu wa aqoolu Laa ilaaha illallaahu Muhammadur-Rasoolullaah",
            urduTranslation = "اے اللہ! میں تیری پناہ مانگتا ہوں اس بات سے کہ میں جان بوجھ کر تیرے ساتھ کسی چیز کو شریک ٹھہراؤں، اور تجھ سے بخشش مانگتا ہوں اس شرک سے جسے میں نہیں جانتا۔ میں نے اس سے توبہ کی اور میں بیزار ہوا کفر، شرک، جھوٹ، غیبت، بدعت، چغلی، بے حیائی، بہتان اور تمام گناہوں سے، اور میں اسلام لایا اور کہتا ہوں کہ اللہ کے سوا کوئی معبود نہیں، حضرت محمد ﷺ اللہ کے رسول ہیں۔",
            englishTranslation = "O Allah! I seek refuge in You from associating anything with You knowingly, and I seek Your forgiveness for that which I do not know. I repent from it and declare myself free from disbelief, polytheism, falsehood, backbiting, innovation, tale-bearing, indecencies, slander, and all sins; I submit in Islam and say: There is no deity except Allah, Muhammad ﷺ is the Messenger of Allah.",
            authenticReference = "Opening clause is a verbatim Hasan Hadith in Al-Adab Al-Mufrad 716 & Musnad Ahmad 19606 (اَللّٰهُمَّ إِنِّي أَعُوذُ بِكَ أَنْ أُشْرِكَ بِكَ وَأَنَا أَعْلَمُ، وَأَسْتَغْفِرُكَ لِمَا لَا أَعْلَمُ); closing clause is the comprehensive repentance formula in traditional Nimaz-e-Mutarjam.",
            scholarlyAndCurriculumNote = "Distinguishes the verbatim Prophetic supplication against hidden Shirk (Al-Adab Al-Mufrad 716) from the expanded educational pledge of repentance appended by classical Madrasah scholars.",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n203.mp3"
        ),
        ImanAndKalimaItem(
            id = "iman_mujmal",
            orderNumber = 7,
            category = "IMAN_MUJMAL_MUFASSAL",
            titleArabic = "الْإِيمَانُ الْمُجْمَلُ",
            titleUrdu = "ایمانِ مجمل (اجمالی اقرارِ ایمان)",
            titleEnglish = "Iman-e-Mujmal (Concise Declaration of Faith)",
            arabicText = "آمَنْتُ بِاللّٰهِ كَمَا هُوَ بِأَسْمَائِهِ وَصِفَاتِهِ وَقَبِلْتُ جَمِيعَ أَحْكَامِهِ، إِقْرَارٌ بِاللِّسَانِ وَتَصْدِيقٌ بِالْقَلْبِ",
            transliteration = "Aamantu billaahi kamaa Huwa bi-asmaa'ihee wa sifaatihee wa qabiltu jamee'a ahkaamihee, iqraarum-bil-lisaani wa tasdeequm-bil-qalb",
            urduTranslation = "میں ایمان لایا اللہ پر جیسا کہ وہ اپنے ناموں اور اپنی صفات کے ساتھ ہے، اور میں نے اس کے تمام احکام قبول کیے، زبان سے اقرار کرتے ہوئے اور دل سے تصدیق کرتے ہوئے۔",
            englishTranslation = "I believe in Allah as He is with all His Names and Attributes, and I have accepted all His commandments—affirming with the tongue and verifying with the heart.",
            authenticReference = "Creedal summary based on Quran 7:180 (وَلِلّٰهِ الْأَسْمَاءُ الْحُسْنَىٰ), Quran 2:285, and classical Aqeedah texts (Al-Fiqh Al-Akbar / Aqeedah Tahawiyyah).",
            scholarlyAndCurriculumNote = "Expresses comprehensive faith in Allah's Names, Attributes, and Commands before detailing individual articles in Iman-e-Mufassal.",
            humanAudioUrl = "https://raw.githubusercontent.com/islamicapi/masnun-dua/main/audio/370.mp3"
        ),
        ImanAndKalimaItem(
            id = "iman_mufassal",
            orderNumber = 8,
            category = "IMAN_MUJMAL_MUFASSAL",
            titleArabic = "الْإِيمَانُ الْمُفَصَّلُ",
            titleUrdu = "ایمانِ مفصل (ایمان کے سات بنیادی عقائد)",
            titleEnglish = "Iman-e-Mufassal (Detailed Declaration of the Articles of Faith)",
            arabicText = "آمَنْتُ بِاللّٰهِ وَمَلَائِكَتِهِ وَكُتُبِهِ وَرُسُلِهِ وَالْيَوْمِ الْآخِرِ وَالْقَدَرِ خَيْرِهِ وَشَرِّهِ مِنَ اللّٰهِ تَعَالَىٰ وَالْبَعْثِ بَعْدَ الْمَوْتِ",
            transliteration = "Aamantu billaahi wa malaa'ikatihee wa kutubihee wa rusulihee wal-yawmil-aakhiri wal-qadari khayrihee wa sharrihee minallaahi Ta'aalaa wal-ba'thi ba'dal-mawt",
            urduTranslation = "میں ایمان لایا اللہ پر، اس کے فرشتوں پر، اس کی کتابوں پر، اس کے رسولوں پر، آخرت کے دن پر، اور اس بات پر کہ اچھی اور بری تقدیر اللہ تعالیٰ کی طرف سے ہے، اور موت کے بعد دوبارہ اٹھائے جانے پر۔",
            englishTranslation = "I believe in Allah, in His Angels, in His Scriptures, in His Messengers, in the Last Day, and that Divine Decree (both good and adversity) is from Allah the Exalted, and in resurrection after death.",
            authenticReference = "Sahih Muslim 8 (Hadith Jibreel: أَنْ تُؤْمِنَ بِاللّٰهِ وَمَلَائِكَتِهِ وَكُتُبِهِ وَرُسُلِهِ وَالْيَوْمِ الْآخِرِ وَتُؤْمِنَ بِالْقَدَرِ خَيْرِهِ وَشَرِّهِ) & Quran 2:285, 4:136.",
            scholarlyAndCurriculumNote = "Directly derived from the famous Hadith of Jibreel (Sahih Muslim 8) and Surah An-Nisa 4:136.",
            humanAudioUrl = "https://raw.githubusercontent.com/islamicapi/masnun-dua/main/audio/92.mp3"
        ),
        ImanAndKalimaItem(
            id = "five_pillars_of_islam",
            orderNumber = 9,
            category = "PILLARS_AND_ARTICLES",
            titleArabic = "أَرْكَانُ الْإِسْلَامِ الْخَمْسَةُ",
            titleUrdu = "ارکانِ اسلام (اسلام کی پانچ بنیادیں)",
            titleEnglish = "The Five Pillars of Islam (Arkan al-Islam)",
            arabicText = "بُنِيَ الْإِسْلَامُ عَلَىٰ خَمْسٍ: شَهَادَةِ أَنْ لَا إِلٰهَ إِلَّا اللّٰهُ وَأَنَّ مُحَمَّدًا رَسُولُ اللّٰهِ، وَإِقَامِ الصَّلَاةِ، وَإِيتَاءِ الزَّكَاةِ، وَالْحَجِّ، وَصَوْمِ رَمَضَانَ",
            transliteration = "Buniyal-Islaamu 'alaa khamsin: Shahaadati al-laa ilaaha illallaahu wa anna Muhammadar-Rasoolullaah, wa iqaamis-Salaati, wa eetaa'iz-Zakaati, wal-Hajji, wa sawmi Ramadaan",
            urduTranslation = "اسلام کی بنیاد پانچ چیزوں پر رکھی گئی ہے: (۱) گواہی دینا کہ اللہ کے سوا کوئی معبود نہیں اور محمد ﷺ اللہ کے رسول ہیں، (۲) نماز قائم کرنا، (۳) زکوٰۃ ادا کرنا، (۴) بیت اللہ کا حج کرنا، اور (۵) رمضان کے روزے رکھنا۔",
            englishTranslation = "Islam is built upon five pillars: (1) Testifying that there is no deity worthy of worship except Allah and that Muhammad ﷺ is the Messenger of Allah, (2) Establishing the five daily Prayers (Salah), (3) Paying obligatory Charity (Zakat), (4) Pilgrimage to the House (Hajj for those able), and (5) Fasting the month of Ramadan.",
            authenticReference = "Sahih al-Bukhari 8; Sahih Muslim 16 (Narrated by Abdullah ibn Umar رضي الله عنهما).",
            scholarlyAndCurriculumNote = "Note on Sunni & Shia Jurisprudence: The four Sunni schools hold these 5 Arkan as the foundational pillars; Ja'fari (Twelver Shia) jurisprudence organizes fundamentals into 5 Usool ad-Deen (Tawheed, 'Adl, Nubuwwah, Imamah, Ma'ad) and 10 Furoo' ad-Deen (Salah, Sawm, Zakat, Khums, Hajj, Jihad, Amr bil-Ma'ruf, Nahi 'anil-Munkar, Tawalla, Tabarra).",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n27.mp3"
        )
    )

    /**
     * Nimaz-e-Mutarjam Part 2: Wudu, Salah Prerequisites, Complete Step-by-Step Salah Recitations,
     * Short Surahs, and Comparative Scholarly Differences.
     */
    val wuduAndSalahSteps: List<WuduAndSalahStepItem> = listOf(
        WuduAndSalahStepItem(
            id = "wudu_step_1_niyyah_tasmiyah",
            stepOrder = 1,
            section = "WUDU",
            titleArabic = "١. النِّيَّةُ وَالتَّسْمِيَةُ لِلْوُضُوءِ",
            titleUrdu = "۱۔ وضو کی نیت، تسمیہ اور دونوں ہاتھ دھونا",
            titleEnglish = "1. Intention (Niyyah), Bismillah & Washing Hands Three Times",
            rulingCategory = "Sunnah Mu'akkadah (Fard Niyyah in Shafi'i/Maliki/Hanbali/Ja'fari)",
            arabicRecitation = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيمِ",
            transliteration = "Bismillaahir-Rahmaanir-Raheem",
            urduMeaning = "اللہ کے نام سے شروع جو نہایت مہربان، بڑا رحم والا ہے۔",
            englishMeaning = "In the name of Allah, the Most Gracious, the Most Merciful.",
            methodExplanationEnglish = "Make the intention in the heart to perform Wudu for purity and prayer, recite Bismillah, and wash both hands up to the wrists 3 times, passing wet fingers between the fingers (Khilal).",
            methodExplanationUrdu = "دل میں پاکی اور نماز کے لیے وضو کی نیت کریں، بسم اللہ پڑھیں اور دونوں ہاتھ گٹوں تک تین بار دھوئیں اور انگلیوں کا خلال کریں۔",
            hadithOrFiqhReference = "Sahih al-Bukhari 159; Sunan Abi Dawud 101.",
            scholarlyDifferences = listOf(
                ScholarlyDifferenceEntry("Hanafi", "Niyyah and Tasmiyah are Sunnah Mu'akkadah in Wudu; the 4 Fara'id of Wudu are specified in Surah Al-Ma'idah 5:6 (washing face, washing arms with elbows, wiping 1/4 of head, washing feet with ankles).", "حنفی فقہ میں وضو کی نیت اور تسمیہ سنتِ مؤکدہ ہیں جبکہ وضو کے فرائض سورۃ المائدہ ۵:۶ کی نص سے چار ہیں۔", "Al-Hidayah; Radd al-Muhtar"),
                ScholarlyDifferenceEntry("Shafi'i, Maliki & Hanbali", "Intention (Niyyah in the heart) at the start of washing the face is Fard (obligatory pillar) of Wudu.", "شافعی، مالکی اور حنبلی فقہ میں وضو کے شروع میں دل سے نیت کرنا فرض ہے۔", "Al-Majmu' (An-Nawawi); Al-Mughni (Ibn Qudamah)")
            ),
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n12.mp3"
        ),
        WuduAndSalahStepItem(
            id = "wudu_step_2_mouth_nose_face",
            stepOrder = 2,
            section = "WUDU",
            titleArabic = "٢. الْمَضْمَضَةُ وَالِاسْتِنْشَاقُ وَغَسْلُ الْوَجْهِ",
            titleUrdu = "۲۔ کلی کرنا، ناک میں پانی ڈالنا اور چہرہ دھونا (فرض اول)",
            titleEnglish = "2. Rinsing Mouth, Nose & Washing the Whole Face (1st Fard of Wudu)",
            rulingCategory = "Fard (Washing Face) + Sunnah (Mouth & Nose; Fard in Hanbali)",
            arabicRecitation = "فَاغْسِلُوا وُجُوهَكُمْ وَأَيْدِيَكُمْ إِلَى الْمَرَافِقِ وَامْسَحُوا بِرُءُوسِكُمْ وَأَرْجُلَكُمْ إِلَى الْكَعْبَيْنِ",
            transliteration = "Faghsiloo wujoohakum wa aydiyakum ilal-maraafiqi wamsahoo bi-ru'oosikum wa arjulakum ilal-ka'bayn",
            urduMeaning = "پس اپنے چہروں کو اور اپنے ہاتھوں کو کہنیوں سمیت دھو لو اور اپنے سروں کا مسح کرو اور اپنے پاؤں ٹخنوں سمیت دھو لو۔ (سورۃ المائدہ ۵:۶)",
            englishMeaning = "Wash your faces and your hands/forearms up to the elbows, wipe over your heads, and wash your feet up to the ankles. (Quran 5:6)",
            methodExplanationEnglish = "Rinse the mouth 3 times (using Miswak if available), draw water into the nostrils 3 times and blow it out with the left hand, then wash the entire face 3 times from the hairline of the forehead down to beneath the chin, and from earlobe to earlobe.",
            methodExplanationUrdu = "تین بار کلی کریں، تین بار ناک میں نرم ہڈی تک پانی ڈال کر بائیں ہاتھ سے صاف کریں، پھر پیشانی کے بالوں سے ٹھوڑی کے نیچے تک اور ایک کان کی لو سے دوسرے کان کی لو تک پورا چہرہ تین بار دھوئیں۔",
            hadithOrFiqhReference = "Quran 5:6; Sahih al-Bukhari 159; Sahih Muslim 226.",
            quranSurahNumber = 5,
            quranAyahRange = 6..6,
            humanAudioUrl = "https://everyayah.com/data/Alafasy_128kbps/005006.mp3"
        ),
        WuduAndSalahStepItem(
            id = "wudu_step_3_arms_head_feet_dua",
            stepOrder = 3,
            section = "WUDU",
            titleArabic = "٣. غَسْلُ الْيَدَيْنِ وَمَسْحُ الرَّأْسِ وَغَسْلُ الرِّجْلَيْنِ وَدُعَاءُ الْوُضُوءِ",
            titleUrdu = "۳۔ کہنیوں سمیت ہاتھ دھونا، سر کا مسح، پاؤں دھونا اور وضو کے بعد کی دعا",
            titleEnglish = "3. Forearms (2nd Fard), Wiping Head (3rd Fard), Feet (4th Fard) & Post-Wudu Dua",
            rulingCategory = "Fard (4 Pillars of Wudu) + Masnoon Post-Wudu Dua",
            arabicRecitation = "أَشْهَدُ أَنْ لَا إِلٰهَ إِلَّا اللّٰهُ وَحْدَهُ لَا شَرِيكَ لَهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ، اَللّٰهُمَّ اجْعَلْنِي مِنَ التَّوَّابِينَ وَاجْعَلْنِي مِنَ الْمُتَطَهِّرِينَ",
            transliteration = "Ash-hadu al-laa ilaaha illallaahu wahdahoo laa shareeka lahoo, wa ash-hadu anna Muhammadan 'abduhoo wa rasooluh. Allaahummaj-'alnee minat-tawwaabeena waj-'alnee minal-mutatahhireen",
            urduMeaning = "میں گواہی دیتا ہوں کہ اللہ کے سوا کوئی معبود نہیں وہ اکیلا ہے اس کا کوئی شریک نہیں اور محمد ﷺ اس کے بندے اور رسول ہیں۔ اے اللہ! مجھے کثرت سے توبہ کرنے والوں اور خوب پاک رہنے والوں میں شامل فرما۔",
            englishMeaning = "I bear witness that there is no deity except Allah alone without partner, and that Muhammad ﷺ is His servant and Messenger. O Allah, make me among those who constantly repent and make me among those who purify themselves.",
            methodExplanationEnglish = "Wash the right forearm then the left forearm including the elbows 3 times each (2nd Fard). Wipe the head with wet hands once (3rd Fard) and wipe the ears. Wash the right foot then the left foot including the ankles 3 times each, cleaning between the toes (4th Fard). Then recite the authentic Post-Wudu Dua.",
            methodExplanationUrdu = "پہلے دایاں اور پھر بایاں بازو کہنیوں سمیت تین تین بار دھوئیں (دوسرا فرض)۔ ایک بار سر کا مسح اور کانوں کا مسح کریں (تیسرا فرض)۔ پھر دایاں اور بایاں پاؤں ٹخنوں سمیت تین تین بار دھوئیں (چوتھا فرض)۔ آخر میں وضو کے بعد کی مسنون دعا پڑھیں۔",
            hadithOrFiqhReference = "Sahih Muslim 234 (Eight gates of Paradise open for one who recites the Shahadah after Wudu); Jami' at-Tirmidhi 55.",
            scholarlyDifferences = listOf(
                ScholarlyDifferenceEntry("Sunni Consensus (Hanafi, Maliki, Shafi'i, Hanbali)", "Both feet are washed completely including the ankles (or wiped over leather socks/Khuffayn when conditions are met).", "اہلِ سنت کے چاروں مذاہب میں پاؤں کو ٹخنوں سمیت دھونا فرض ہے (یا شرائط کے ساتھ موزوں پر مسح)۔", "Sahih al-Bukhari 163; Sahih Muslim 241"),
                ScholarlyDifferenceEntry("Ja'fari (Twelver Shia)", "Face and arms (from elbows down to fingertips) are washed twice at most, followed by wiping the front of the head and wiping the tops of both feet to the ankle bones with the remaining moisture of Wudu.", "فقہ جعفریہ میں چہرہ اور بازو (کہنی سے انگلیوں تک) دھوئے جاتے ہیں اور سر کے اگلے حصے اور دونوں پاؤں کی پشت پر وضو کی تری سے مسح کیا جاتا ہے۔", "Al-Kafi; Tahrir al-Wasilah")
            ),
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n13.mp3"
        ),
        WuduAndSalahStepItem(
            id = "salah_prereq_and_rakat",
            stepOrder = 4,
            section = "SALAH_PREREQUISITES",
            titleArabic = "شَرَائِطُ الصَّلَاةِ وَأَرْكَانُهَا وَعَدَدُ الرَّكَعَاتِ",
            titleUrdu = "نماز کی شرائط، فرائض اور پانچوں نمازوں کی رکعات",
            titleEnglish = "Prerequisites of Salah (Shara'it), Pillars (Arkan) & Daily Rak'at Table",
            rulingCategory = "Fard (7 Outer Shara'it + 6 Inner Arkan)",
            arabicRecitation = "إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَوْقُوتًا",
            transliteration = "Innas-Salaata kaanat 'alal-mu'mineena kitaabam-mawqootaa",
            urduMeaning = "بے شک نماز مومنوں پر مقررہ اوقات میں فرض ہے۔ (سورۃ النساء ۴:۱۰۳)",
            englishMeaning = "Indeed, Prayer has been decreed upon the believers at specified times. (Quran 4:103)",
            methodExplanationEnglish = "Outer Prerequisites (Shara'it-e-Salah): (1) Purity of body (Wudu/Ghusl), (2) Purity of clothes, (3) Purity of place, (4) Covering the 'Awrah (Satr-e-'Awrah), (5) Entry of prayer time, (6) Facing the Qiblah, (7) Intention (Niyyah in the heart).\nInner Pillars (Fara'id-e-Salah): (1) Takbeer-e-Tahreema, (2) Qiyam (standing), (3) Qira'ah (recitation), (4) Ruku (bowing), (5) Sujood (two prostrations per Rak'ah), (6) Qa'dah Akhirah (final sitting for Tashahhud).\nDaily Rak'at Summary:\n• Fajr: 2 Sunnah Mu'akkadah + 2 Fard (Total 4)\n• Dhuhr: 4 Sunnah Mu'akkadah + 4 Fard + 2 Sunnah Mu'akkadah + 2 Nafl (Total 12)\n• Asr: 4 Sunnah Ghayr Mu'akkadah + 4 Fard (Total 8)\n• Maghrib: 3 Fard + 2 Sunnah Mu'akkadah + 2 Nafl (Total 7)\n• Isha: 4 Sunnah Ghayr Mu'akkadah + 4 Fard + 2 Sunnah Mu'akkadah + 2 Nafl + 3 Witr + 2 Nafl (Total 17)",
            methodExplanationUrdu = "شرائطِ نماز (باہر کے فرائض): (۱) بدن کا پاک ہونا، (۲) کپڑوں کا پاک ہونا، (۳) جگہ کا پاک ہونا، (۴) سترِ عورت، (۵) نماز کا وقت ہونا، (۶) قبلہ رخ ہونا، (۷) دل میں نیت کرنا۔\nارکانِ نماز (اندر کے فرائض): (۱) تکبیرِ تحریمہ، (۲) قیام، (۳) قراءت، (۴) رکوع، (۵) دونوں سجدے، (۶) قعدہ اخیرہ۔\nپانچوں نمازوں کی رکعات:\n• فجر: ۲ سنت مؤکدہ + ۲ فرض = ۴\n• ظہر: ۴ سنت مؤکدہ + ۴ فرض + ۲ سنت مؤکدہ + ۲ نفل = ۱۲\n• عصر: ۴ سنت غیر مؤکدہ + ۴ فرض = ۸\n• مغرب: ۳ فرض + ۲ سنت مؤکدہ + ۲ نفل = ۷\n• عشاء: ۴ سنت غیر مؤکدہ + ۴ فرض + ۲ سنت مؤکدہ + ۲ نفل + ۳ وتر + ۲ نفل = ۱۷",
            hadithOrFiqhReference = "Quran 4:103; Sahih al-Bukhari 757 (Hadith of the man who prayed badly); Sahih Muslim 728 (12 daily Sunnah Rawatib).",
            quranSurahNumber = 4,
            quranAyahRange = 103..103,
            humanAudioUrl = "https://everyayah.com/data/Alafasy_128kbps/004103.mp3"
        ),
        WuduAndSalahStepItem(
            id = "namaz_step_1_takbeer_sana",
            stepOrder = 5,
            section = "NAMAZ_MUTARJAM",
            titleArabic = "١. تَكْبِيرَةُ الْإِحْرَامِ وَالثَّنَاءُ",
            titleUrdu = "۱۔ تکبیرِ تحریمہ اور ثناء",
            titleEnglish = "Step 1: Takbeer-e-Tahreema (Allahu Akbar) & Sana (Opening Supplication)",
            rulingCategory = "Takbeer: Fard • Sana: Sunnah",
            arabicRecitation = "اَللّٰهُ أَكْبَرُ ۝ سُبْحَانَكَ اللّٰهُمَّ وَبِحَمْدِكَ وَتَبَارَكَ اسْمُكَ وَتَعَالَىٰ جَدُّكَ وَلَا إِلٰهَ غَيْرُكَ",
            transliteration = "Allaahu Akbar. Subhaanaka Allaahumma wa bihamdika wa tabaarakasmuka wa ta'aalaa jadduka wa laa ilaaha ghayruk",
            urduMeaning = "اللہ سب سے بڑا ہے۔ اے اللہ! میں تیری پاکی بیان کرتا ہوں تیری تعریف کے ساتھ، اور تیرا نام بابرکت ہے، تیری شان بہت بلند ہے، اور تیرے سوا کوئی معبود نہیں۔",
            englishMeaning = "Allah is the Greatest. Glory be to You, O Allah, and praise be to You; blessed is Your Name, exalted is Your Majesty, and there is no deity other than You.",
            methodExplanationEnglish = "Stand facing the Qiblah with intention in the heart, raise both hands and say 'Allahu Akbar' (Takbeer-e-Tahreema), place the right hand over the left, and silently recite Sana.",
            methodExplanationUrdu = "قبلہ رخ کھڑے ہو کر دل میں نماز کی نیت کریں، دونوں ہاتھ اٹھا کر 'اللہ اکبر' کہیں، ہاتھ باندھ لیں اور آہستہ آواز سے ثناء پڑھیں۔",
            hadithOrFiqhReference = "Sunan Abi Dawud 775, Jami' at-Tirmidhi 243, Sunan an-Nasa'i 899; Sahih Muslim 399.",
            scholarlyDifferences = listOf(
                ScholarlyDifferenceEntry("Hanafi & Hanbali", "Place the right hand over the left hand below the navel (Hanafi) or below/on the chest, and recite 'Subhanaka Allahumma wa bihamdika...'.", "حنفی اور حنبلی فقہ میں تکبیرِ تحریمہ کے بعد دائیں ہاتھ کو بائیں پر رکھ کر 'سبحانک اللھم وبحمدک...' پڑھنا مسنون ہے۔", "Sunan Abi Dawud 756, 775"),
                ScholarlyDifferenceEntry("Shafi'i", "Place hands above the navel / below the chest and recite 'Wajjahtu wajhiya lilladhi fataras-samawati wal-ard...' (Sahih Muslim 771) or 'Subhanaka Allahumma...'.", "شافعی فقہ میں سینے کے نیچے ہاتھ باندھے جاتے ہیں اور دعائے استفتاح 'وجھت وجھی...' یا 'سبحانک اللھم' پڑھی جاتی ہے۔", "Sahih Muslim 771"),
                ScholarlyDifferenceEntry("Maliki & Ja'fari", "In the well-known Maliki view (Sadl in Fard) and in Ja'fari jurisprudence (Irsal), the arms are left naturally at the sides during Qiyam.", "مالکی مشہور قول اور فقہ جعفریہ میں قیام کی حالت میں ہاتھ کھلے (ارسال/سدل) رکھے جاتے ہیں۔", "Al-Mudawwanah; Wasa'il ash-Shi'ah")
            ),
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n28.mp3"
        ),
        WuduAndSalahStepItem(
            id = "namaz_step_2_taawwudh_tasmiyah_fatiha",
            stepOrder = 6,
            section = "NAMAZ_MUTARJAM",
            titleArabic = "٢. التَّعَوُّذُ وَالتَّسْمِيَةُ وَسُورَةُ الْفَاتِحَةِ",
            titleUrdu = "۲۔ تعوذ، تسمیہ اور سورۃ الفاتحہ",
            titleEnglish = "Step 2: Ta'awwudh, Tasmiyah & Surah Al-Fatiha (The Opening)",
            rulingCategory = "Fard / Wajib in Every Rak'ah",
            arabicRecitation = "أَعُوذُ بِاللّٰهِ مِنَ الشَّيْطَانِ الرَّجِيمِ ۝ بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيمِ ۝ الْحَمْدُ لِلّٰهِ رَبِّ الْعَالَمِينَ ۝ الرَّحْمٰنِ الرَّحِيمِ ۝ مٰلِكِ يَوْمِ الدِّينِ ۝ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ ۝ اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ ۝ صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ ۝ (آمِينَ)",
            transliteration = "A'oodhu billaahi minash-shaytaanir-rajeem. Bismillaahir-Rahmaanir-Raheem. Al-hamdu lillaahi Rabbil-'aalameen. Ar-Rahmaanir-Raheem. Maaliki yawmid-deen. Iyyaaka na'budu wa iyyaaka nasta'een. Ihdinas-siraatal-mustaqeem. Siraatalladheena an'amta 'alayhim ghayril-maghdoobi 'alayhim walad-daalleen. (Aameen)",
            urduMeaning = "میں اللہ کی پناہ مانگتا ہوں شیطان مردود سے۔ اللہ کے نام سے شروع جو نہایت مہربان، بڑا رحم والا ہے۔ سب تعریف اللہ ہی کے لیے ہے جو تمام جہانوں کا پالنے والا ہے۔ بڑا مہربان، نہایت رحم والا۔ روزِ جزا کا مالک۔ (اے اللہ!) ہم تیری ہی عبادت کرتے ہیں اور تجھ ہی سے مدد مانگتے ہیں۔ ہمیں سیدھا راستہ دکھا۔ ان لوگوں کا راستہ جن پر تو نے انعام فرمایا، نہ کہ ان کا جن پر غضب کیا گیا اور نہ گمراہوں کا۔ (الٰہی قبول فرما)",
            englishMeaning = "I seek refuge in Allah from the accursed Satan. In the name of Allah, the Most Gracious, the Most Merciful. All praise is due to Allah, Lord of the worlds. The Most Gracious, the Most Merciful. Master of the Day of Judgment. You alone we worship, and You alone we ask for help. Guide us to the Straight Path—the path of those upon whom You have bestowed favor, not of those who have evoked anger, nor of those who are astray. (Aameen)",
            methodExplanationEnglish = "Recite Ta'awwudh and Tasmiyah silently in the first Rak'ah, then recite Surah Al-Fatiha with full Tajweed and say 'Aameen' at its conclusion.",
            methodExplanationUrdu = "پہلی رکعت میں ثناء کے بعد آہستہ آواز سے اعوذ باللہ اور بسم اللہ پڑھیں، پھر سورۃ الفاتحہ تجوید کے ساتھ پڑھیں اور آخر میں آمین کہیں۔",
            hadithOrFiqhReference = "Quran 1:1–7; Sahih al-Bukhari 756 ('There is no prayer for one who does not recite the Opening of the Book').",
            quranSurahNumber = 1,
            quranAyahRange = 1..7,
            scholarlyDifferences = listOf(
                ScholarlyDifferenceEntry("Hanafi & Maliki", "'Bismillah' is recited silently before Al-Fatiha, and 'Aameen' is said silently.", "حنفی اور مالکی فقہ میں بسم اللہ اور آمین آہستہ آواز (سراً) سے پڑھی جاتی ہیں۔", "Sunan at-Tirmidhi 248"),
                ScholarlyDifferenceEntry("Shafi'i & Hanbali", "'Aameen' is said aloud in audible prayers; Shafi'i school counts 'Bismillah' as the first verse recited aloud in audible prayers.", "شافعی اور حنبلی فقہ میں جہری نمازوں میں آمین بلند آواز سے کہی جاتی ہے، اور شافعیہ کے نزدیک بسم اللہ بھی بلند آواز سے پڑھی جاتی ہے۔", "Sahih al-Bukhari 780; Sahih Muslim 400"),
                ScholarlyDifferenceEntry("Ja'fari (Twelver Shia)", "'Bismillah' is an obligatory part of every Surah and is recited aloud; after Al-Fatiha, 'Al-hamdu lillahi Rabbil-'alameen' is said instead of 'Aameen'.", "فقہ جعفریہ میں بسم اللہ ہر سورت کا جزو ہے اور سورۃ الفاتحہ کے بعد 'الحمد للہ رب العالمین' کہا جاتا ہے۔", "Al-Kafi")
            ),
            humanAudioUrl = "https://everyayah.com/data/Alafasy_128kbps/001001.mp3"
        ),
        WuduAndSalahStepItem(
            id = "namaz_step_3_short_surahs",
            stepOrder = 7,
            section = "SHORT_SURAHS",
            titleArabic = "٣. ضَمُّ السُّورَةِ (سُورَةُ الْعَصْرِ • الْكَوْثَرِ • الْإِخْلَاصِ • الْفَلَقِ • النَّاسِ)",
            titleUrdu = "۳۔ سورۃ الفاتحہ کے بعد مختصر سورتیں (العصر، الکوثر، الاخلاص، الفلق، الناس)",
            titleEnglish = "Step 3: Reciting a Short Surah After Al-Fatiha (Dammu-s-Surah)",
            rulingCategory = "Wajib in First Two Rak'ahs of Fard & All Rak'ahs of Sunnah/Nafl/Witr",
            arabicRecitation = "﴿سُورَةُ الْإِخْلَاصِ﴾ قُلْ هُوَ اللّٰهُ أَحَدٌ ۝ اللّٰهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ ۝\n﴿سُورَةُ الْكَوْثَرِ﴾ إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ ۝ فَصَلِّ لِرَبِّكَ وَانْحَرْ ۝ إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ ۝\n﴿سُورَةُ الْعَصْرِ﴾ وَالْعَصْرِ ۝ إِنَّ الْإِنْسَانَ لَفِي خُسْرٍ ۝ إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ ۝",
            transliteration = "Surah Al-Ikhlas (112): Qul Huwallaahu Ahad. Allaahus-Samad. Lam yalid wa lam yoolad. Wa lam yakul-lahoo kufuwan ahad.\nSurah Al-Kawthar (108): Innaa a'taynaakal-Kawthar. Fasalli li-Rabbika wanhar. Inna shaani'aka huwal-abtar.\nSurah Al-'Asr (103): Wal-'Asr. Innal-insaana lafee khusr. Illalladheena aamanoo wa 'amilus-saalihaati wa tawaasaw bil-haqqi wa tawaasaw bis-sabr.",
            urduMeaning = "سورۃ الاخلاص: کہہ دیجیے وہ اللہ ایک ہے۔ اللہ بے نیاز ہے۔ نہ اس کی کوئی اولاد ہے اور نہ وہ کسی سے پیدا ہوا۔ اور کوئی اس کا ہمسر نہیں۔\nسورۃ الکوثر: بے شک ہم نے آپ کو کوثر عطا فرمایا۔ پس اپنے رب کے لیے نماز پڑھیے اور قربانی کیجیے۔ بے شک آپ کا دشمن ہی بے نام و نشان ہے۔\nسورۃ العصر: زمانے کی قسم! بے شک انسان خسارے میں ہے، سوائے ان لوگوں کے جو ایمان لائے اور نیک اعمال کیے اور ایک دوسرے کو حق کی نصیحت کی اور صبر کی تلقین کی۔",
            englishMeaning = "Surah Al-Ikhlas (112): Say, 'He is Allah, the One. Allah, the Eternal Refuge. He neither begets nor is born, nor is there to Him any equivalent.'\nSurah Al-Kawthar (108): Indeed, We have granted you Al-Kawthar. So pray to your Lord and sacrifice. Indeed, your enemy is the one cut off.\nSurah Al-'Asr (103): By time! Indeed, mankind is in loss, except those who believe and do righteous deeds and advise each other to truth and advise each other to patience.",
            methodExplanationEnglish = "After Surah Al-Fatiha in the first two Rak'ahs, recite at least 3 short verses or 1 complete short Surah (such as Al-Kawthar, Al-Ikhlas, Al-'Asr, Al-Falaq, or An-Nas).",
            methodExplanationUrdu = "فرض نماز کی پہلی دو رکعتوں میں اور سنت، نفل اور وتر کی ہر رکعت میں سورۃ الفاتحہ کے بعد کم از کم تین چھوٹی آیات یا ایک چھوٹی سورت ملائیں۔",
            hadithOrFiqhReference = "Quran 73:20 (فَاقْرَءُوا مَا تَيَسَّرَ مِنَ الْقُرْآنِ); Sahih al-Bukhari 757; Sahih Muslim 451.",
            quranSurahNumber = 112,
            quranAyahRange = 1..4,
            humanAudioUrl = "https://everyayah.com/data/Alafasy_128kbps/112001.mp3"
        ),
        WuduAndSalahStepItem(
            id = "namaz_step_4_ruku_qaumah",
            stepOrder = 8,
            section = "NAMAZ_MUTARJAM",
            titleArabic = "٤. الرُّكُوعُ وَالْقَوْمَةُ",
            titleUrdu = "۴۔ رکوع اور قومہ (رکوع سے کھڑے ہونا)",
            titleEnglish = "Step 4: Ruku (Bowing) & Qaumah (Standing Upright After Ruku)",
            rulingCategory = "Ruku: Fard • Tasbeeh & Qaumah: Wajib / Sunnah",
            arabicRecitation = "﴿فِي الرُّكُوعِ﴾ سُبْحَانَ رَبِّيَ الْعَظِيمِ (٣ مَرَّاتٍ)\n﴿فِي الْقَوْمَةِ﴾ سَمِعَ اللّٰهُ لِمَنْ حَمِدَهُ ۝ رَبَّنَا لَكَ الْحَمْدُ (حَمْدًا كَثِيرًا طَيِّبًا مُبَارَكًا فِيهِ)",
            transliteration = "In Ruku: Subhaana Rabbiyal-'Azeem (3 times).\nRising to Qaumah: Sami'allaahu liman hamidah. Rabbanaa lakal-hamd (hamdan katheeran tayyiban mubaarakan feeh).",
            urduMeaning = "رکوع کی تسبیح: پاک ہے میرا رب بڑی عظمت والا (کم از کم ۳ بار)۔\nقومہ (کھڑے ہوتے وقت): اللہ نے اس کی سن لی جس نے اس کی تعریف کی۔ اے ہمارے رب! تیرے ہی لیے تمام تعریف ہے (بہت زیادہ، پاکیزہ اور بابرکت تعریف)۔",
            englishMeaning = "In Ruku: Glory be to my Lord, the Almighty (at least 3 times).\nRising to Qaumah: Allah hears the one who praises Him. Our Lord, to You belongs all praise (abundant, pure, and blessed praise).",
            methodExplanationEnglish = "Say 'Allahu Akbar' and bow into Ruku, grasping the knees with fingers spread and keeping the back level. Recite 'Subhana Rabbiyal-'Azim' 3, 5, or 7 times. Then rise upright saying 'Sami'Allahu liman hamidah' and while standing calm in Qaumah say 'Rabbana lakal-hamd'.",
            methodExplanationUrdu = "'اللہ اکبر' کہتے ہوئے رکوع میں جائیں، ہاتھوں سے گھٹنوں کو پکڑیں اور کمر سیدھی رکھیں۔ کم از کم تین بار 'سُبْحَانَ رَبِّيَ الْعَظِيمِ' پڑھیں۔ پھر 'سَمِعَ اللّٰهُ لِمَنْ حَمِدَهُ' کہتے ہوئے سیدھے کھڑے ہو جائیں اور قومہ میں 'رَبَّنَا لَكَ الْحَمْدُ' کہیں۔",
            hadithOrFiqhReference = "Sahih Muslim 772; Sunan Abi Dawud 869; Sahih al-Bukhari 799.",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n33.mp3"
        ),
        WuduAndSalahStepItem(
            id = "namaz_step_5_sajdah_jalsah",
            stepOrder = 9,
            section = "NAMAZ_MUTARJAM",
            titleArabic = "٥. السُّجُودُ وَالْجَلْسَةُ بَيْنَ السَّجْدَتَيْنِ",
            titleUrdu = "۵۔ دونوں سجدے اور جلسہ (دو سجدوں کے درمیان بیٹھنا)",
            titleEnglish = "Step 5: Two Prostrations (Sajdah) & Sitting Between Prostrations (Jalsah)",
            rulingCategory = "Two Sajdahs: Fard • Jalsah: Wajib / Fard",
            arabicRecitation = "﴿فِي السُّجُودِ﴾ سُبْحَانَ رَبِّيَ الْأَعْلَىٰ (٣ مَرَّاتٍ)\n﴿فِي الْجَلْسَةِ﴾ اَللّٰهُمَّ اغْفِرْ لِي وَارْحَمْنِي وَعَافِنِي وَاهْدِنِي وَارْزُقْنِي",
            transliteration = "In Sajdah: Subhaana Rabbiyal-A'laa (3 times).\nIn Jalsah: Allaahummaghfir lee warhamnee wa 'aafinee wahdinee warzuqnee",
            urduMeaning = "سجدے کی تسبیح: پاک ہے میرا رب جو سب سے بلند ہے (کم از کم ۳ بار)۔\nجلسہ (دو سجدوں کے درمیان کی دعا): اے اللہ! مجھے بخش دے، مجھ پر رحم فرما، مجھے عافیت دے، مجھے ہدایت دے اور مجھے رزق عطا فرما۔",
            englishMeaning = "In Sajdah: Glory be to my Lord, the Most High (at least 3 times).\nIn Jalsah: O Allah, forgive me, have mercy on me, grant me well-being, guide me, and provide for me.",
            methodExplanationEnglish = "Say 'Allahu Akbar' and prostrate on 7 bones: the forehead with the nose, both palms, both knees, and the toes of both feet facing the Qiblah. Recite 'Subhana Rabbiyal-A'la' 3 times. Rise to sit calmly in Jalsah and say 'Rabbighfir li / Allahummaghfir li...', then perform the second Sajdah.",
            methodExplanationUrdu = "'اللہ اکبر' کہتے ہوئے سات اعضاء (پیشانی مع ناک، دونوں ہتھیلیوں، دونوں گھٹنوں اور دونوں پاؤں کی انگلیوں) پر سجدہ کریں اور تین بار 'سُبْحَانَ رَبِّيَ الْأَعْلَىٰ' پڑھیں۔ پھر 'اللہ اکبر' کہہ کر اطمینان سے بیٹھیں (جلسہ) اور مغفرت کی دعا پڑھیں، پھر دوسرا سجدہ کریں۔",
            hadithOrFiqhReference = "Sahih al-Bukhari 812 (Prostration on 7 bones); Sahih Muslim 772; Sunan Abi Dawud 850; Sunan Ibn Majah 898.",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n41.mp3"
        ),
        WuduAndSalahStepItem(
            id = "namaz_step_6_tashahhud",
            stepOrder = 10,
            section = "NAMAZ_MUTARJAM",
            titleArabic = "٦. التَّشَهُّدُ (التَّحِيَّاتُ)",
            titleUrdu = "۶۔ تشہد (التحیات)",
            titleEnglish = "Step 6: Tashahhud (Attahiyyat in Sitting Posture)",
            rulingCategory = "Wajib / Fard in Qa'dah",
            arabicRecitation = "التَّحِيَّاتُ لِلّٰهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ، السَّلَامُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللّٰهِ وَبَرَكَاتُهُ، السَّلَامُ عَلَيْنَا وَعَلَىٰ عِبَادِ اللّٰهِ الصَّالِحِينَ، أَشْهَدُ أَنْ لَا إِلٰهَ إِلَّا اللّٰهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
            transliteration = "At-tahiyyaatu lillaahi was-salawaatu wat-tayyibaatu, as-salaamu 'alayka ayyuhan-Nabiyyu wa rahmatullaahi wa barakaatuh, as-salaamu 'alaynaa wa 'alaa 'ibaadillaahis-saaliheen, ash-hadu al-laa ilaaha illallaahu wa ash-hadu anna Muhammadan 'abduhoo wa rasooluh",
            urduMeaning = "تمام قولی، بدنی اور مالی عبادتیں اللہ ہی کے لیے ہیں۔ اے نبی ﷺ! آپ پر سلام ہو اور اللہ کی رحمت اور اس کی برکتیں ہوں۔ سلام ہو ہم پر اور اللہ کے نیک بندوں پر۔ میں گواہی دیتا ہوں کہ اللہ کے سوا کوئی معبود نہیں اور میں گواہی دیتا ہوں کہ حضرت محمد ﷺ اس کے بندے اور رسول ہیں۔",
            englishMeaning = "All greetings, prayers, and pure words/deeds belong to Allah. Peace be upon you, O Prophet ﷺ, and the mercy of Allah and His blessings. Peace be upon us and upon the righteous servants of Allah. I bear witness that there is no deity except Allah, and I bear witness that Muhammad ﷺ is His servant and Messenger.",
            methodExplanationEnglish = "Sit after the second Rak'ah (and in the final Rak'ah) with hands on the thighs and recite Tashahhud, raising the right index finger at the testimony of Tawheed ('Ash-hadu al-laa ilaaha illallah').",
            methodExplanationUrdu = "دوسری رکعت کے دونوں سجدوں کے بعد اور آخری رکعت میں قعدہ میں بیٹھ کر التحیات پڑھیں، اور 'أَشْهَدُ أَنْ لَا إِلٰهَ إِلَّا اللّٰهُ' پر دائیں ہاتھ کی شہادت کی انگلی سے اشارہ کریں۔",
            hadithOrFiqhReference = "Sahih al-Bukhari 831 & Sahih Muslim 402 (Tashahhud of Abdullah ibn Mas'ud رضي الله عنه).",
            scholarlyDifferences = listOf(
                ScholarlyDifferenceEntry("Hanafi & Hanbali", "Prefer the Tashahhud narrated by Abdullah ibn Mas'ud رضي الله عنه (Sahih al-Bukhari 831).", "حنفی اور حنبلی فقہ میں حضرت عبداللہ بن مسعود رضی اللہ عنہ سے مروی تشہد مختار ہے۔", "Sahih al-Bukhari 831"),
                ScholarlyDifferenceEntry("Shafi'i", "Prefer the Tashahhud narrated by Abdullah ibn 'Abbas رضي الله عنهما ('At-tahiyyatul-mubarakatas-salawatut-tayyibatu lillah...' — Sahih Muslim 403).", "شافعی فقہ میں حضرت عبداللہ بن عباس رضی اللہ عنہما سے مروی تشہد مختار ہے؛ دونوں ہی صحیح احادیث سے ثابت ہیں۔", "Sahih Muslim 403"),
                ScholarlyDifferenceEntry("Maliki", "Prefer the Tashahhud of 'Umar ibn al-Khattab رضي الله عنه recorded in Muwatta Imam Malik.", "مالکی فقہ میں موطا امام مالک میں مروی حضرت عمر رضی اللہ عنہ کا تشہد مختار ہے۔", "Muwatta Malik")
            ),
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n52.mp3"
        ),
        WuduAndSalahStepItem(
            id = "namaz_step_7_durood_ibrahim",
            stepOrder = 11,
            section = "NAMAZ_MUTARJAM",
            titleArabic = "٧. الصَّلَاةُ الْإِبْرَاهِيمِيَّةُ (دُرُودِ إِبْرَاهِيمِي)",
            titleUrdu = "۷۔ درودِ ابراہیمی (قعدہ اخیرہ میں)",
            titleEnglish = "Step 7: Durood-e-Ibrahimi (As-Salatul-Ibrahimiyyah)",
            rulingCategory = "Sunnah Mu'akkadah (Fard in Shafi'i/Hanbali final sitting)",
            arabicRecitation = "اَللّٰهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ كَمَا صَلَّيْتَ عَلَىٰ إِبْرَاهِيمَ وَعَلَىٰ آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ ۝ اَللّٰهُمَّ بَارِكْ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ كَمَا بَارَكْتَ عَلَىٰ إِبْرَاهِيمَ وَعَلَىٰ آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ",
            transliteration = "Allaahumma salli 'alaa Muhammadin wa 'alaa aali Muhammadin kamaa sallayta 'alaa Ibraaheema wa 'alaa aali Ibraaheema innaka Hameedum-Majeed. Allaahumma baarik 'alaa Muhammadin wa 'alaa aali Muhammadin kamaa baarakta 'alaa Ibraaheema wa 'alaa aali Ibraaheema innaka Hameedum-Majeed",
            urduMeaning = "اے اللہ! رحمت بھیج حضرت محمد ﷺ پر اور حضرت محمد ﷺ کی آل پر، جس طرح تو نے رحمت بھیجی حضرت ابراہیم علیہ السلام پر اور حضرت ابراہیم علیہ السلام کی آل پر، بے شک تو بے حد تعریف والا، بڑی بزرگی والا ہے۔ اے اللہ! برکت نازل فرما حضرت محمد ﷺ پر اور حضرت محمد ﷺ کی آل پر، جس طرح تو نے برکت نازل فرمائی حضرت ابراہیم علیہ السلام پر اور حضرت ابراہیم علیہ السلام کی آل پر، بے شک تو بے حد تعریف والا، بڑی بزرگی والا ہے۔",
            englishMeaning = "O Allah, send grace and honor upon Muhammad and upon the family of Muhammad, just as You sent grace and honor upon Ibrahim and upon the family of Ibrahim; indeed, You are Praiseworthy, Glorious. O Allah, send blessings upon Muhammad and upon the family of Muhammad, just as You sent blessings upon Ibrahim and upon the family of Ibrahim; indeed, You are Praiseworthy, Glorious.",
            methodExplanationEnglish = "In the final sitting (Qa'dah Akhirah), immediately after Tashahhud, recite Durood-e-Ibrahimi.",
            methodExplanationUrdu = "آخری قعدہ میں تشہد (التحیات) کے فوراً بعد درودِ ابراہیمی پڑھیں۔",
            hadithOrFiqhReference = "Sahih al-Bukhari 3370; Sahih Muslim 406 (Narrated by Ka'b ibn 'Ujrah رضي الله عنه).",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n53.mp3"
        ),
        WuduAndSalahStepItem(
            id = "namaz_step_8_dua_masoorah_salam",
            stepOrder = 12,
            section = "NAMAZ_MUTARJAM",
            titleArabic = "٨. الدُّعَاءُ بَعْدَ الصَّلَاةِ الْإِبْرَاهِيمِيَّةِ وَالسَّلَامُ",
            titleUrdu = "۸۔ دعائے ماثورہ (درود کے بعد کی دعائیں) اور سلام",
            titleEnglish = "Step 8: Duas After Durood (Dua-e-Masoorah) & Concluding Salam",
            rulingCategory = "Dua: Sunnah • Salam: Wajib / Fard",
            arabicRecitation = "﴿دُعَاءُ الصِّدِّيقِ﴾ اَللّٰهُمَّ إِنِّي ظَلَمْتُ نَفْسِي ظُلْمًا كَثِيرًا وَلَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ فَاغْفِرْ لِي مَغْفِرَةً مِنْ عِنْدِكَ وَارْحَمْنِي إِنَّكَ أَنْتَ الْغَفُورُ الرَّحِيمُ\n﴿دُعَاءٌ قُرْآنِيٌّ﴾ رَبِّ اجْعَلْنِي مُقِيمَ الصَّلَاةِ وَمِنْ ذُرِّيَّتِي رَبَّنَا وَتَقَبَّلْ دُعَاءِ ۝ رَبَّنَا اغْفِرْ لِي وَلِوَالِدَيَّ وَلِلْمُؤْمِنِينَ يَوْمَ يَقُومُ الْحِسَابُ\n﴿السَّلَامُ﴾ السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللّٰهِ",
            transliteration = "1. Allaahumma innee zalamtu nafsee zulman katheeran wa laa yaghfirudh-dhunooba illaa Anta faghfir lee maghfiratam-min 'indika warhamnee innaka Antal-Ghafoorur-Raheem.\n2. Rabbij-'alnee muqeemas-Salaati wa min dhurriyyatee Rabbanaa wa taqabbal du'aa. Rabbanaghfir lee wa li-waalidayya wa lil-mu'mineena yawma yaqoomul-hisaab.\nSalam: As-salaamu 'alaykum wa rahmatullaah (turning right, then left).",
            urduMeaning = "۱۔ (حضرت ابوبکر صدیقؓ کی دعا): اے اللہ! بے شک میں نے اپنی جان پر بہت زیادہ ظلم کیا اور تیرے سوا کوئی گناہوں کو نہیں بخش سکتا، پس تو اپنی خاص بخشش سے مجھے بخش دے اور مجھ پر رحم فرما، بے شک تو ہی بخشنے والا، نہایت مہربان ہے۔\n۲۔ (قرآنی دعا): اے میرے رب! مجھے اور میری اولاد کو نماز قائم کرنے والا بنا، اے ہمارے رب! اور میری دعا قبول فرما۔ اے ہمارے رب! مجھے، میرے والدین کو اور تمام مومنوں کو اس دن بخش دے جس دن حساب قائم ہوگا۔\nسلام: تم پر سلامتی ہو اور اللہ کی رحمت (پہلے دائیں طرف اور پھر بائیں طرف چہرہ پھیر کر)۔",
            englishMeaning = "1. O Allah, I have wronged myself greatly, and none forgives sins except You; so grant me forgiveness from Yourself and have mercy on me. Truly You are the Forgiving, the Merciful.\n2. My Lord, make me an establisher of prayer, and from my descendants. Our Lord, and accept my supplication. Our Lord, forgive me and my parents and the believers the Day the account is established.\nSalam: Peace and mercy of Allah be upon you (turning head to the right, then to the left).",
            methodExplanationEnglish = "After Durood-e-Ibrahimi, recite an authentic Quranic or Prophetic Dua, then conclude the prayer by turning the head to the right saying 'As-salamu 'alaykum wa rahmatullah' and to the left saying 'As-salamu 'alaykum wa rahmatullah'.",
            methodExplanationUrdu = "درود شریف کے بعد دعائے ماثورہ پڑھیں، پھر پہلے دائیں کندھے کی طرف اور پھر بائیں کندھے کی طرف رخ کر کے 'السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللّٰهِ' کہہ کر نماز مکمل کریں۔",
            hadithOrFiqhReference = "Sahih al-Bukhari 834 & Sahih Muslim 2705 (Dua taught to Abu Bakr as-Siddiq رضي الله عنه); Quran 14:40–41; Sahih Muslim 582 (Salam).",
            quranSurahNumber = 14,
            quranAyahRange = 40..41,
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n57.mp3"
        ),
        WuduAndSalahStepItem(
            id = "namaz_step_9_dua_qunoot",
            stepOrder = 13,
            section = "NAMAZ_MUTARJAM",
            titleArabic = "٩. دُعَاءُ الْقُنُوتِ (فِي صَلَاةِ الْوِتْرِ)",
            titleUrdu = "۹۔ دعائے قنوت (نمازِ وتر کی تیسری رکعت میں)",
            titleEnglish = "Step 9: Dua-e-Qunoot (Supplication in Witr Prayer)",
            rulingCategory = "Wajib / Sunnah Mu'akkadah in Witr",
            arabicRecitation = "﴿١﴾ اَللّٰهُمَّ إِنَّا نَسْتَعِينُكَ وَنَسْتَغْفِرُكَ وَنُؤْمِنُ بِكَ وَنَتَوَكَّلُ عَلَيْكَ وَنُثْنِي عَلَيْكَ الْخَيْرَ، وَنَشْكُرُكَ وَلَا نَكْفُرُكَ، وَنَخْلَعُ وَنَتْرُكُ مَنْ يَفْجُرُكَ، اَللّٰهُمَّ إِيَّاكَ نَعْبُدُ وَلَكَ نُصَلِّي وَنَسْجُدُ، وَإِلَيْكَ نَسْعَىٰ وَنَحْفِدُ، وَنَرْجُو رَحْمَتَكَ وَنَخْشَىٰ عَذَابَكَ، إِنَّ عَذَابَكَ بِالْكُفَّارِ مُلْحِقٌ\n﴿٢﴾ اَللّٰهُمَّ اهْدِنِي فِيمَنْ هَدَيْتَ، وَعَافِنِي فِيمَنْ عَافَيْتَ، وَتَوَلَّنِي فِيمَنْ تَوَلَّيْتَ، وَبَارِكْ لِي فِيمَا أَعْطَيْتَ، وَقِنِي شَرَّ مَا قَضَيْتَ، فَإِنَّكَ تَقْضِي وَلَا يُقْضَىٰ عَلَيْكَ، وَإِنَّهُ لَا يَذِلُّ مَنْ وَالَيْتَ، وَلَا يَعِزُّ مَنْ عَادَيْتَ، تَبَارَكْتَ رَبَّنَا وَتَعَالَيْتَ",
            transliteration = "1. Allaahumma innaa nasta'eenuka wa nastaghfiruka wa nu'minu bika wa natawakkalu 'alayka wa nuthnee 'alaykal-khayr, wa nashkuruka wa laa nakfuruka, wa nakhla'u wa natruku may-yafjuruk. Allaahumma iyyaaka na'budu wa laka nusallee wa nasjudu, wa ilayka nas'aa wa nahfidu, wa narjoo rahmataka wa nakhshaa 'adhaabaka, inna 'adhaabaka bil-kuffaari mulhiq.\n2. Allaahummahdinee feeman hadayta, wa 'aafinee feeman 'aafayta, wa tawallanee feeman tawallayta, wa baarik lee feemaa a'tayta, wa qinee sharra maa qadayta, fa-innaka taqdee wa laa yuqdaa 'alayka, wa innahoo laa yadhillu man waalayta, wa laa ya'izzu man 'aadayta, tabaarakta Rabbanaa wa ta'aalayt.",
            urduMeaning = "۱۔ اے اللہ! ہم تجھ سے مدد مانگتے ہیں اور تجھ سے بخشش طلب کرتے ہیں، تجھ پر ایمان رکھتے ہیں، تجھ پر بھروسہ کرتے ہیں اور تیری بہترین تعریف کرتے ہیں، تیرا شکر ادا کرتے ہیں اور تیری ناشکری نہیں کرتے، اور جو تیری نافرمانی کرے اس سے الگ ہوتے اور اسے چھوڑتے ہیں۔ اے اللہ! ہم تیری ہی عبادت کرتے ہیں، تیرے ہی لیے نماز پڑھتے اور سجدہ کرتے ہیں، تیری ہی طرف دوڑتے اور لپکتے ہیں، تیری رحمت کی امید رکھتے ہیں اور تیرے عذاب سے ڈرتے ہیں، بے شک تیرا عذاب کافروں کو پہنچنے والا ہے۔\n۲۔ اے اللہ! مجھے ہدایت دے ان لوگوں میں جنہیں تو نے ہدایت دی، اور مجھے عافیت دے ان میں جنہیں تو نے عافیت دی... اے ہمارے رب! تو بابرکت اور بلند و بالا ہے۔",
            englishMeaning = "1. O Allah! We seek Your help, ask Your forgiveness, believe in You, rely upon You, and praise You with all good. We thank You and are not ungrateful to You, and we disassociate from and leave whoever disobeys You. O Allah! You alone we worship, to You we pray and prostrate, toward You we strive and hasten; we hope for Your mercy and fear Your punishment; truly Your punishment will overtake the disbelievers.\n2. O Allah! Guide me among those You have guided, grant me well-being among those You have granted well-being, take me into Your charge among those You have taken into Your charge, bless me in what You have given, and protect me from the evil of what You have decreed...",
            methodExplanationEnglish = "Both authentic Qunoot formulas are provided: (1) Qunoot narrated from 'Umar ibn al-Khattab & Abdullah ibn Mas'ud رضي الله عنهما (standard in Hanafi Witr before Ruku in the 3rd Rak'ah), and (2) Qunoot taught by the Prophet ﷺ to Al-Hasan ibn 'Ali رضي الله عنهما (Sunan Abi Dawud 1425, Jami' at-Tirmidhi 464).",
            methodExplanationUrdu = "دعائے قنوت کے دونوں مستند مسنون صیغے درج ہیں: (۱) حضرت عمر اور حضرت عبداللہ بن مسعود رضی اللہ عنہما سے مروی دعائے قنوت (جو حنفی فقہ میں وتر کی تیسری رکعت میں رکوع سے پہلے پڑھی جاتی ہے)، اور (۲) حضرت حسن بن علی رضی اللہ عنہما کو سکھائی گئی دعائے قنوت (سنن ابی داؤد ۱۴۲۵، جامع ترمذی ۴۶۴)۔",
            hadithOrFiqhReference = "Musannaf Ibn Abi Shaybah 6965 & Sunan al-Bayhaqi 2/210 ('Allahumma inna nasta'inuka...'); Sunan Abi Dawud 1425 & Jami' at-Tirmidhi 464 ('Allahummahdini fiman hadayta...').",
            scholarlyDifferences = listOf(
                ScholarlyDifferenceEntry("Hanafi", "Witr is 3 Rak'ahs with one Salam; Qunoot is recited silently in the 3rd Rak'ah after Surah recitation and BEFORE Ruku, raising the hands for Takbeer-e-Qunoot.", "حنفی فقہ میں وتر تین رکعت ایک سلام کے ساتھ واجب ہے اور تیسری رکعت میں قراءت کے بعد رکوع سے پہلے تکبیر کہہ کر دعائے قنوت پڑھی جاتی ہے۔", "Al-Hidayah; Sunan an-Nasa'i 1699"),
                ScholarlyDifferenceEntry("Shafi'i & Hanbali", "Qunoot is recited AFTER rising from Ruku in the final Rak'ah of Witr (and in Fajr prayer according to the Shafi'i school).", "شافعی اور حنبلی فقہ میں وتر کی آخری رکعت میں رکوع سے اٹھنے کے بعد قنوت پڑھی جاتی ہے (اور شافعیہ کے نزدیک فجر میں بھی)۔", "Sunan Abi Dawud 1425; Al-Majmu'")
            ),
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n58.mp3"
        ),
        WuduAndSalahStepItem(
            id = "namaz_step_10_salatul_janaza",
            stepOrder = 14,
            section = "NAMAZ_MUTARJAM",
            titleArabic = "١٠. صَلَاةُ الْجَنَازَةِ وَأَدْعِيَتُهَا (لِلْبَالِغِ وَالصَّبِيِّ وَالصَّبِيَّةِ)",
            titleUrdu = "۱۰۔ نمازِ جنازہ کا طریقہ اور مسنون دعائیں (بالغ مرد و عورت، نابالغ لڑکے اور لڑکی کی دعا)",
            titleEnglish = "Step 10: Salat al-Janaza (Funeral Prayer Method & Duas for Adult, Boy & Girl)",
            rulingCategory = "Fard Kifayah (4 Takbeers Standing — No Ruku or Sajdah)",
            arabicRecitation = "﴿دُعَاءُ الْبَالِغِ وَالْبَالِغَةِ﴾ اَللّٰهُمَّ اغْفِرْ لِحَيِّنَا وَمَيِّتِنَا وَشَاهِدِنَا وَغَائِبِنَا وَصَغِيرِنَا وَكَبِيرِنَا وَذَكَرِنَا وَأُنْثَانَا، اَللّٰهُمَّ مَنْ أَحْيَيْتَهُ مِنَّا فَأَحْيِهِ عَلَى الْإِسْلَامِ وَمَنْ تَوَفَّيْتَهُ مِنَّا فَتَوَفَّهُ عَلَى الْإِيمَانِ\n﴿لِلصَّبِيِّ / نابالغ لڑکے کے لیے﴾ اَللّٰهُمَّ اجْعَلْهُ لَنَا فَرَطًا وَسَلَفًا وَأَجْرًا (وَذُخْرًا وَشَافِعًا مُشَفَّعًا)\n﴿لِلصَّبِيَّةِ / نابالغ لڑکی کے لیے﴾ اَللّٰهُمَّ اجْعَلْهَا لَنَا فَرَطًا وَسَلَفًا وَأَجْرًا (وَذُخْرًا وَشَافِعَةً مُشَفَّعَةً)",
            transliteration = "Adult: Allaahummaghfir li-hayyinaa wa mayyitinaa wa shaahidinaa wa ghaa'ibinaa wa sagheerinaa wa kabeerinaa wa dhakarinaa wa unthaanaa. Allaahumma man ahyaytahoo minnaa fa-ahyihi 'alal-Islaam, wa man tawaffaytahoo minnaa fatawaffahoo 'alal-eemaan.\nMinor Boy: Allaahummaj-'alhu lanaa faratan wa salafan wa ajran (wa dhukhran wa shaafi'an mushaffa'an).\nMinor Girl: Allaahummaj-'alhaa lanaa faratan wa salafan wa ajran (wa dhukhran wa shaafi'atan mushaffa'atan).",
            urduMeaning = "(بالغ مرد و عورت کے لیے): اے اللہ! ہمارے زندوں اور ہمارے مردوں کو بخش دے، ہمارے حاضر اور غائب کو، ہمارے چھوٹوں اور بڑوں کو، اور ہمارے مردوں اور عورتوں کو بخش دے۔ اے اللہ! ہم میں سے جسے تو زندہ رکھے اسے اسلام پر زندہ رکھ اور جسے وفات دے اسے ایمان پر وفات دے۔\n(نابالغ لڑکے کے لیے): اے اللہ! اس بچے کو ہمارے لیے پیش رو، سامانِ آخرت اور اجر و مقبول شفاعت کرنے والا بنا۔\n(نابالغ لڑکی کے لیے): اے اللہ! اس بچی کو ہمارے لیے پیش رو، سامانِ آخرت اور اجر و مقبول شفاعت کرنے والی بنا۔",
            englishMeaning = "Adult: O Allah, forgive our living and our deceased, our present and our absent, our young and our old, our males and our females. O Allah, whomever among us You keep alive, keep him alive upon Islam, and whomever among us You cause to die, cause him to die upon Faith.\nMinor Boy: O Allah, make him for us a forerunner, a precursor, and a treasure and accepted intercessor.\nMinor Girl: O Allah, make her for us a forerunner, a precursor, and a treasure and accepted intercessor.",
            methodExplanationEnglish = "Salat al-Janaza has 4 Takbeers performed standing without Ruku or Sajdah:\n• 1st Takbeer: Raise hands, say Allahu Akbar, fold hands, and recite Sana (or Surah Al-Fatiha in Shafi'i/Hanbali fiqh).\n• 2nd Takbeer: Say Allahu Akbar and recite Durood-e-Ibrahimi.\n• 3rd Takbeer: Say Allahu Akbar and recite the Janaza Dua for the deceased (adult, boy, or girl).\n• 4th Takbeer: Say Allahu Akbar and conclude with Salam.",
            methodExplanationUrdu = "نمازِ جنازہ فرضِ کفایہ ہے اور اس میں چار تکبیریں کھڑے ہو کر کہی جاتی ہیں (رکوع اور سجدہ نہیں ہوتا):\n• پہلی تکبیر: ہاتھ اٹھا کر اللہ اکبر کہیں، ہاتھ باندھ کر ثناء (یا شافعی/حنبلی مسلک میں سورۃ الفاتحہ) پڑھیں۔\n• دوسری تکبیر: اللہ اکبر کہہ کر درودِ ابراہیمی پڑھیں۔\n• تیسری تکبیر: اللہ اکبر کہہ کر میت کے لیے مسنون دعا (بالغ یا نابالغ لڑکے/لڑکی کی دعا) پڑھیں۔\n• چوتھی تکبیر: اللہ اکبر کہہ کر سلام پھیر دیں۔",
            hadithOrFiqhReference = "Sunan Abi Dawud 3201; Jami' at-Tirmidhi 1024; Sahih Muslim 963; Sahih al-Bukhari 1335 (Hisn al-Muslim #156–161).",
            scholarlyDifferences = listOf(
                ScholarlyDifferenceEntry("Hanafi & Maliki", "After the 1st Takbeer, Sana ('Subhanaka Allahumma wa bihamdika...') is recited without Surah Al-Fatiha (Maliki goes straight to Dua after Takbeers).", "حنفی فقہ میں پہلی تکبیر کے بعد ثناء پڑھی جاتی ہے۔", "Al-Hidayah; Radd al-Muhtar"),
                ScholarlyDifferenceEntry("Shafi'i & Hanbali", "After the 1st Takbeer, reciting Surah Al-Fatiha is a pillar (Rukn) of Salat al-Janaza.", "شافعی اور حنبلی فقہ میں پہلی تکبیر کے بعد سورۃ الفاتحہ پڑھنا فرض ہے۔", "Sahih al-Bukhari 1335")
            ),
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n157.mp3"
        )
    )

    /**
     * Separate Fasting-Intention (Sehri Niyat) Section (Requirement 3):
     * Explicitly distinguishes the Prophetic rule that intention is in the heart
     * from customary verbal formulas taught in elementary primers, with full scholarly views
     * and Sunnah.com / IslamQA #37805 citations. Never labels an unverified verbal formula as a Hadith.
     */
    val sehriNiyatGuidance: SehriNiyatGuidanceData = SehriNiyatGuidanceData(
        titleEnglish = "Sehri Niyat (Fasting Intention) — Heart Intention & Scholarly Guidance",
        titleUrdu = "سحری اور روزے کی نیت — شرعی حکم، مسنون تحقیق اور فقہی آراء",
        titleArabic = "نِيَّةُ الصِّيَامِ وَأَحْكَامُهَا الشَّرْعِيَّةُ",
        heartIntentionPrincipleEnglish = "In Islamic jurisprudence across all schools of thought, the intention (Niyyah) for fasting is an act of the HEART—namely, the firm resolve in one's heart to fast the next day of Ramadan in obedience to Allah. Waking up for Suhoor (Sehri) or resolving in the heart at night to fast tomorrow already fulfills the obligatory intention in full.",
        heartIntentionPrincipleUrdu = "تمام فقہی مکاتبِ فکر کے نزدیک روزے کی نیت دراصل 'دل کے پختہ ارادے' کا نام ہے، یعنی دل میں یہ عزم ہونا کہ میں کل اللہ تعالیٰ کی رضا کے لیے رمضان کا فرض روزہ رکھ رہا ہوں۔ سحری کے لیے اٹھنا اور روزے کی نیت سے کھانا پینا بذاتِ خود مکمل شرعی نیت ہے۔",
        propheticHadithReferences = listOf(
            "Sahih al-Bukhari 1 & Sahih Muslim 1907 — «إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى» ('Actions are judged by intentions, and every person will have what they intended').",
            "Sunan an-Nasa'i 2331 & Sunan Abi Dawud 2454 (Sahih) — «مَنْ لَمْ يُبَيِّتِ الصِّيَامَ قَبْلَ الْفَجْرِ فَلَا صِيَامَ لَهُ» ('Whoever does not form the intention to fast before Fajr, there is no fast for him' — narrated by Hafsah رضي الله عنها).",
            "Sahih al-Bukhari 1923 & Sahih Muslim 1095 — «تَسَحَّرُوا فَإِنَّ فِي السَّحُورِ بَرَكَةً» ('Take Suhoor, for indeed there is blessing in Suhoor').",
            "IslamQA Fatwa #37805 & Classical Fiqh Manuals — Clarifies that the place of intention (mahall an-niyyah) is the heart, and no specific verbal wording for starting the fast was narrated from the Prophet ﷺ or his Companions."
        ),
        customaryFormulas = listOf(
            CustomaryNiyatFormula(
                id = "niyat_formula_1_bisawmi_ghadin",
                labelEnglish = "Commonly Taught Formula 1 (South Asian Madrasah / Hanafi Primers)",
                labelUrdu = "مروج الفاظِ نیت (۱) — نصابی و فقہی کتب میں رائج جملہ",
                arabicText = "وَبِصَوْمِ غَدٍ نَوَيْتُ مِنْ شَهْرِ رَمَضَانَ",
                transliteration = "Wa bisawmi ghadin nawaytu min shahri Ramadaan",
                urduTranslation = "اور میں نے ماہِ رمضان کے کل کے روزے کی نیت کی۔",
                englishTranslation = "And I intend to keep the fast of tomorrow in the month of Ramadan.",
                classificationBadge = "CUSTOMARY FIQH FORMULA • NOT A HADITH",
                attributionAndStatusNote = "Source Verification: This Arabic phrase is a customary verbal intention formula taught in South Asian elementary books to help learners focus their intention. It is NOT a Prophetic Hadith and must not be attributed to the Prophet ﷺ."
            ),
            CustomaryNiyatFormula(
                id = "niyat_formula_2_nawaytu_an_asuma",
                labelEnglish = "Commonly Taught Formula 2 (Classical Fiqh Manuals — Al-Fatawa Al-Hindiyyah / Al-Majmu')",
                labelUrdu = "مروج الفاظِ نیت (۲) — فقہی کتب میں مذکور صیغہ",
                arabicText = "نَوَيْتُ أَنْ أَصُومَ غَدًا لِلّٰهِ تَعَالَىٰ مِنْ فَرْضِ رَمَضَانَ",
                transliteration = "Nawaytu an asooma ghadan lillaahi Ta'aalaa min fardi Ramadaan",
                urduTranslation = "میں نے نیت کی کہ کل اللہ تعالیٰ کے لیے رمضان کا فرض روزہ رکھوں گا۔",
                englishTranslation = "I intend to fast tomorrow for Allah the Exalted from the obligatory fasts of Ramadan.",
                classificationBadge = "CLASSICAL FIQH EXAMPLE • NOT A HADITH",
                attributionAndStatusNote = "Source Verification: Mentioned by classical jurists (e.g., Al-Fatawa Al-Hindiyyah 1/195, Al-Majmu' 6/289) as an illustrative example of matching the tongue with the heart's resolve. It is NOT a Prophetic narration."
            )
        ),
        scholarlyViews = listOf(
            ScholarlyDifferenceEntry(
                schoolOrTradition = "Hanafi & Shafi'i Jurists (Later Manuals)",
                positionSummaryEnglish = "The obligation is fulfilled by the intention of the heart alone. Speaking the intention with the tongue (in Arabic, Urdu, English, or any language) alongside the heart's resolve is considered permissible or helpful (mustahabb/aid to concentration) by later jurists, provided one does not believe the verbal formula is an obligatory Sunnah or Hadith. In the Hanafi school, the intention for a Ramadan fast may also be formed up until mid-day (Dahwah Kubra) if nothing breaking the fast occurred after Fajr.",
                positionSummaryUrdu = "اصل فرض دل کا ارادہ ہے۔ متاخرین احناف اور شوافع کے نزدیک دل کے ارادے کے ساتھ زبان سے نیت کے الفاظ (عربی یا اپنی زبان میں) کہہ لینا دل کی یکسوئی کے لیے جائز/مددگار ہے، بشرطیکہ اسے حدیثِ نبوی یا لازم نہ سمجھا جائے۔ حنفی فقہ میں رمضان کے روزے کی نیت ضحوہ کبریٰ (نصف النہار شرعی) تک بھی درست ہو جاتی ہے بشرطیکہ فجر کے بعد کوئی منافیِ روزہ عمل نہ پایا گیا ہو۔",
                primarySourceReference = "Radd al-Muhtar (Ibn 'Abidin) 2/377; Al-Fatawa Al-Hindiyyah 1/195; Al-Majmu' (An-Nawawi) 6/289"
            ),
            ScholarlyDifferenceEntry(
                schoolOrTradition = "Maliki, Hanbali & Athari Scholars (e.g., Ibn Taymiyyah / IslamQA #37805)",
                positionSummaryEnglish = "Intention belongs strictly to the heart; anyone who knows in their heart that tomorrow is Ramadan and resolves to fast has already made the complete intention. Verbalizing a set formula out loud is discouraged because neither the Prophet ﷺ nor the Sahabah transmitted any verbal formula for starting the fast.",
                positionSummaryUrdu = "نیت کا محل صرف اور صرف دل ہے؛ جس شخص کو معلوم ہو کہ کل رمضان ہے اور اس کا روزہ رکھنے کا ارادہ ہو تو اس کی نیت مکمل ہو گئی۔ زبان سے مخصوص الفاظ دہرانا مسنون نہیں کیونکہ نبی کریم ﷺ اور صحابہ کرامؓ سے روزہ شروع کرنے کے کوئی مخصوص الفاظ منقول نہیں۔",
                primarySourceReference = "Majmu' al-Fatawa (Ibn Taymiyyah) 25/215; Al-Mughni (Ibn Qudamah); IslamQA #37805"
            ),
            ScholarlyDifferenceEntry(
                schoolOrTradition = "Ja'fari (Twelver Shia) Jurisprudence",
                positionSummaryEnglish = "Intention is the inner resolve of the heart to abstain from fast-invalidators from Fajr to Maghrib for nearness to Allah (Qurbatan ilallah); no spoken formula is required, and a single continuous intention at the start of the month of Ramadan suffices alongside nightly renewal.",
                positionSummaryUrdu = "فقہ جعفریہ میں بھی نیت دل کے ارادے اور قربتِ الٰہی کے قصد کا نام ہے، زبان سے الفاظ ادا کرنا شرط نہیں۔",
                primarySourceReference = "Tahrir al-Wasilah; Minhaj as-Salihin"
            )
        ),
        criticalVerificationNotice = "Verification Summary: Intention (Niyyah) is in the heart (Sahih al-Bukhari 1; Sunan an-Nasa'i 2331). Customary verbal formulas ('Wa bisawmi ghadin nawaytu...') are educational/fiqh aids and are NEVER labeled as Prophetic Hadiths."
    )

    /**
     * Chronological Daily Duas Sequence (Requirement 1):
     * Makes daily duas easy to find in natural daily order from waking up to sleeping,
     * including the verified Iftari / Breaking Fast Dua (Sunan Abi Dawud 2357).
     */
    val dailyDuasInSequence: List<DailyDuaSequenceEntry> = listOf(
        DailyDuaSequenceEntry(
            sequenceOrder = 1,
            timeOfDayStage = "1. Waking Up (Morning)",
            stageUrdu = "۱۔ صبح سو کر اٹھنے کی دعا",
            duaId = "dua_sleep_wake_up",
            titleEnglish = "Upon Waking Up from Sleep",
            titleUrdu = "نیند سے بیدار ہونے کی مسنون دعا",
            arabicText = "الْحَمْدُ لِلّٰهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            transliteration = "Al-hamdu lillaahilladhee ahyaanaa ba'da maa amaatanaa wa ilayhin-nushoor",
            englishTranslation = "All praise is for Allah who gave us life after having taken it from us, and unto Him is the resurrection.",
            urduTranslation = "تمام تعریفیں اللہ ہی کے لیے ہیں جس نے ہمیں موت (نیند) کے بعد زندگی عطا فرمائی اور اسی کی طرف اٹھ کر جانا ہے۔",
            authenticReference = "Sahih al-Bukhari 6312; Sahih Muslim 2711 (Hisn al-Muslim #1)",
            verificationNote = "Muttafaqun 'Alayh (Sahih al-Bukhari & Sahih Muslim).",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n1.mp3"
        ),
        DailyDuaSequenceEntry(
            sequenceOrder = 2,
            timeOfDayStage = "2. Entering the Restroom",
            stageUrdu = "۲۔ بیت الخلاء میں داخل ہونے کی دعا",
            duaId = "dua_home_toilet_enter",
            titleEnglish = "Before Entering the Washroom / Restroom",
            titleUrdu = "بیت الخلاء میں داخل ہونے سے پہلے کی دعا",
            arabicText = "بِسْمِ اللّٰهِ، اَللّٰهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْخُبُثِ وَالْخَبَائِثِ",
            transliteration = "Bismillaahi, Allaahumma innee a'oodhu bika minal-khubuthi wal-khabaa'ith",
            englishTranslation = "In the name of Allah. O Allah, I seek refuge in You from male and female evil spirits.",
            urduTranslation = "اللہ کے نام سے۔ اے اللہ! میں خبیث جنوں اور خبیث جننیوں کے شر سے تیری پناہ مانگتا ہوں۔",
            authenticReference = "Sahih al-Bukhari 142; Sahih Muslim 375 (Hisn al-Muslim #10)",
            verificationNote = "Step in with the left foot after reciting outside.",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n10.mp3"
        ),
        DailyDuaSequenceEntry(
            sequenceOrder = 3,
            timeOfDayStage = "3. Leaving the Restroom",
            stageUrdu = "۳۔ بیت الخلاء سے باہر نکلنے کی دعا",
            duaId = "dua_home_toilet_exit",
            titleEnglish = "Upon Leaving the Washroom / Restroom",
            titleUrdu = "بیت الخلاء سے باہر نکلنے کی دعا",
            arabicText = "غُفْرَانَكَ",
            transliteration = "Ghufraanaka",
            englishTranslation = "I seek Your forgiveness (O Allah).",
            urduTranslation = "(اے اللہ!) میں تیری بخشش طلب کرتا ہوں۔",
            authenticReference = "Sunan Abi Dawud 30; Jami' at-Tirmidhi 7 (Sahih — Hisn al-Muslim #11)",
            verificationNote = "Step out with the right foot.",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n11.mp3"
        ),
        DailyDuaSequenceEntry(
            sequenceOrder = 4,
            timeOfDayStage = "4. After Wudu (Ablution)",
            stageUrdu = "۴۔ وضو کے بعد کی دعا",
            duaId = "dua_wudu_after",
            titleEnglish = "Upon Completing Wudu",
            titleUrdu = "وضو مکمل کرنے کے بعد کی دعا",
            arabicText = "أَشْهَدُ أَنْ لَا إِلٰهَ إِلَّا اللّٰهُ وَحْدَهُ لَا شَرِيكَ لَهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ، اَللّٰهُمَّ اجْعَلْنِي مِنَ التَّوَّابِينَ وَاجْعَلْنِي مِنَ الْمُتَطَهِّرِينَ",
            transliteration = "Ash-hadu al-laa ilaaha illallaahu wahdahoo laa shareeka lahoo wa ash-hadu anna Muhammadan 'abduhoo wa rasooluh. Allaahummaj-'alnee minat-tawwaabeena waj-'alnee minal-mutatahhireen",
            englishTranslation = "I bear witness that none has the right to be worshipped except Allah alone without partner, and that Muhammad is His servant and Messenger. O Allah, make me among those who repent and those who purify themselves.",
            urduTranslation = "میں گواہی دیتا ہوں کہ اللہ کے سوا کوئی معبود نہیں وہ اکیلا ہے اور محمد ﷺ اس کے بندے اور رسول ہیں۔ اے اللہ! مجھے توبہ کرنے والوں اور پاک رہنے والوں میں شامل فرما۔",
            authenticReference = "Sahih Muslim 234; Jami' at-Tirmidhi 55 (Hisn al-Muslim #13)",
            verificationNote = "Sahih Muslim 234.",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n13.mp3"
        ),
        DailyDuaSequenceEntry(
            sequenceOrder = 5,
            timeOfDayStage = "5. Wearing Clothes",
            stageUrdu = "۵۔ لباس پہننے کی دعا",
            duaId = "dua_home_dressing",
            titleEnglish = "When Putting On Clothes",
            titleUrdu = "کپڑے پہننے کی مسنون دعا",
            arabicText = "الْحَمْدُ لِلّٰهِ الَّذِي كَسَانِي هٰذَا الثَّوْبَ وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
            transliteration = "Al-hamdu lillaahilladhee kasaanee haadhath-thawba wa razaqaneehi min ghayri hawlim-minnee wa laa quwwah",
            englishTranslation = "All praise is for Allah who clothed me with this garment and provided it for me without any power or might from myself.",
            urduTranslation = "تمام تعریفیں اس اللہ کے لیے ہیں جس نے مجھے یہ لباس پہنایا اور میری کسی طاقت اور قوت کے بغیر مجھے یہ عطا فرمایا۔",
            authenticReference = "Sunan Abi Dawud 4023; Jami' at-Tirmidhi 3458 (Hasan — Hisn al-Muslim #6)",
            verificationNote = "Graded Hasan.",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n6.mp3"
        ),
        DailyDuaSequenceEntry(
            sequenceOrder = 6,
            timeOfDayStage = "6. Leaving Home",
            stageUrdu = "۶۔ گھر سے نکلتے وقت کی دعا",
            duaId = "dua_home_leaving",
            titleEnglish = "When Leaving the House",
            titleUrdu = "گھر سے باہر نکلتے وقت کی دعا",
            arabicText = "بِسْمِ اللّٰهِ تَوَكَّلْتُ عَلَى اللّٰهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللّٰهِ",
            transliteration = "Bismillaahi tawakkaltu 'alallaahi, wa laa hawla wa laa quwwata illaa billaah",
            englishTranslation = "In the name of Allah, I place my trust in Allah; there is no power and no strength except with Allah.",
            urduTranslation = "اللہ کے نام کے ساتھ، میں نے اللہ پر بھروسہ کیا، اور گناہ سے بچنے کی طاقت اور نیکی کرنے کی قوت اللہ ہی کی مدد سے ہے۔",
            authenticReference = "Sunan Abi Dawud 5095; Jami' at-Tirmidhi 3426 (Sahih — Hisn al-Muslim #16)",
            verificationNote = "The angels say: 'You are guided, sufficed, and protected.'",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n16.mp3"
        ),
        DailyDuaSequenceEntry(
            sequenceOrder = 7,
            timeOfDayStage = "7. Entering Home",
            stageUrdu = "۷۔ گھر میں داخل ہوتے وقت کی دعا",
            duaId = "dua_home_entering",
            titleEnglish = "When Entering the House",
            titleUrdu = "گھر میں داخل ہونے کی دعا",
            arabicText = "بِسْمِ اللّٰهِ وَلَجْنَا، وَبِسْمِ اللّٰهِ خَرَجْنَا، وَعَلَىٰ رَبِّنَا تَوَكَّلْنَا",
            transliteration = "Bismillaahi walajnaa, wa bismillaahi kharajnaa, wa 'alaa Rabbinaa tawakkalnaa",
            englishTranslation = "In the name of Allah we enter, in the name of Allah we leave, and upon our Lord we rely (then greet the family with Salam).",
            urduTranslation = "اللہ کے نام سے ہم داخل ہوئے اور اللہ کے نام سے ہم نکلے اور اپنے رب ہی پر ہم نے بھروسہ کیا (پھر گھر والوں کو سلام کریں)۔",
            authenticReference = "Sunan Abi Dawud 5096; Sahih Muslim 2018 (remembering Allah upon entering & eating — Hisn al-Muslim #18)",
            verificationNote = "Sahih Muslim 2018 confirms mentioning Allah's name upon entering home.",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n18.mp3"
        ),
        DailyDuaSequenceEntry(
            sequenceOrder = 8,
            timeOfDayStage = "8. Entering & Leaving the Masjid",
            stageUrdu = "۸۔ مسجد میں داخل ہونے اور نکلنے کی دعا",
            duaId = "dua_masjid_entering",
            titleEnglish = "Entering & Leaving the Mosque",
            titleUrdu = "مسجد میں داخل ہوتے اور نکلتے وقت کی دعا",
            arabicText = "﴿عِنْدَ الدُّخُولِ﴾ بِسْمِ اللّٰهِ وَالصَّلَاةُ وَالسَّلَامُ عَلَىٰ رَسُولِ اللّٰهِ، اَللّٰهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ\n﴿عِنْدَ الْخُرُوجِ﴾ اَللّٰهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
            transliteration = "Entering: Bismillaahi was-salaatu was-salaamu 'alaa Rasoolillaah, Allaahummaftah lee abwaaba rahmatik.\nLeaving: Allaahumma innee as'aluka min fadlik.",
            englishTranslation = "Entering (right foot): In the name of Allah, blessings and peace be upon the Messenger of Allah. O Allah, open for me the gates of Your mercy.\nLeaving (left foot): O Allah, I ask You from Your bounty.",
            urduTranslation = "داخل ہوتے وقت (دایاں قدم): اے اللہ! میرے لیے اپنی رحمت کے دروازے کھول دے۔\nنکلتے وقت (بایاں قدم): اے اللہ! میں تجھ سے تیرے فضل کا سوال کرتا ہوں۔",
            authenticReference = "Sahih Muslim 713; Sunan Abi Dawud 465 (Hisn al-Muslim #20–21)",
            verificationNote = "Sahih Muslim 713.",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n20.mp3"
        ),
        DailyDuaSequenceEntry(
            sequenceOrder = 9,
            timeOfDayStage = "9. Seeking Beneficial Knowledge",
            stageUrdu = "۹۔ علم میں اضافے اور نفع بخش علم کی دعا",
            duaId = "dua_study_rabbi_zidni",
            titleEnglish = "Before Studying / Seeking Knowledge",
            titleUrdu = "علم میں اضافے کی قرآنی و مسنون دعا",
            arabicText = "رَّبِّ زِدْنِي عِلْمًا",
            transliteration = "Rabbi zidnee 'ilmaa.",
            englishTranslation = "My Lord, increase me in knowledge (Quran 20:114).",
            urduTranslation = "اے میرے رب! میرے علم میں اضافہ فرما (سورۃ طٰہٰ ۲۰:۱۱۴)۔",
            authenticReference = "Quran 20:114 (Surah Ta-Ha 20:114)",
            verificationNote = "Exact Quranic verse (Surah Ta-Ha 20:114) — plays authentic human reciter verse audio.",
            quranSurahAyah = 20 to 114,
            humanAudioUrl = "https://everyayah.com/data/Alafasy_128kbps/020114.mp3"
        ),
        DailyDuaSequenceEntry(
            sequenceOrder = 10,
            timeOfDayStage = "10. Before Eating & Drinking",
            stageUrdu = "۱۰۔ کھانا کھانے اور پانی پینے سے پہلے کی دعا",
            duaId = "dua_food_before",
            titleEnglish = "Before Eating or Drinking ( & If Forgotten at Start)",
            titleUrdu = "کھانا شروع کرنے کی دعا (اور شروع میں بھول جانے کی دعا)",
            arabicText = "بِسْمِ اللّٰهِ وَعَلَىٰ بَرَكَةِ اللّٰهِ ۝ (وَإِذَا نَسِيَ فِي أَوَّلِهِ فَلْيَقُلْ: بِسْمِ اللّٰهِ أَوَّلَهُ وَآخِرَهُ)",
            transliteration = "Bismillaahi wa 'alaa barakatillaah. (If forgotten at start: Bismillaahi awwalahoo wa aakhirahoo)",
            englishTranslation = "In the name of Allah and upon the blessings of Allah. (If one forgets at the beginning: In the name of Allah at its beginning and at its end).",
            urduTranslation = "اللہ کے نام سے اور اللہ کی برکت پر۔ (اور اگر شروع میں بھول جائے تو کہے: اللہ کے نام سے اس کے اول اور آخر میں)۔",
            authenticReference = "Sahih al-Bukhari 5376 ('Mention Allah's Name and eat with your right hand'); Sunan Abi Dawud 3767; Jami' at-Tirmidhi 1858 (Hisn al-Muslim #178)",
            verificationNote = "Sahih Hadith.",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n178.mp3"
        ),
        DailyDuaSequenceEntry(
            sequenceOrder = 11,
            timeOfDayStage = "11. After Eating & Drinking",
            stageUrdu = "۱۱۔ کھانا کھانے اور پانی/دودھ پینے کے بعد کی دعا",
            duaId = "dua_food_after",
            titleEnglish = "After Finishing a Meal or Drink",
            titleUrdu = "کھانا کھانے کے بعد کی مسنون دعا",
            arabicText = "الْحَمْدُ لِلّٰهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ ۝ الْحَمْدُ لِلّٰهِ الَّذِي أَطْعَمَنِي هٰذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
            transliteration = "Al-hamdu lillaahilladhee at'amanaa wa saqaanaa wa ja'alanaa Muslimeen. Al-hamdu lillaahilladhee at'amanee haadhaa wa razaqaneehi min ghayri hawlim-minnee wa laa quwwah",
            englishTranslation = "All praise is due to Allah who fed us, gave us drink, and made us Muslims. All praise is due to Allah who fed me this and provided it for me without any power or strength from me.",
            urduTranslation = "تمام تعریفیں اس اللہ کے لیے ہیں جس نے ہمیں کھلایا اور پلایا اور ہمیں مسلمان بنایا۔ تمام تعریفیں اللہ کے لیے ہیں جس نے مجھے یہ کھانا کھلایا اور میری طاقت و قوت کے بغیر مجھے یہ عطا فرمایا۔",
            authenticReference = "Sunan Abi Dawud 3850 & 4023; Jami' at-Tirmidhi 3458 (Hasan — Hisn al-Muslim #180)",
            verificationNote = "Whoever recites the second formula after eating has their past minor sins forgiven (Jami' at-Tirmidhi 3458).",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n180.mp3"
        ),
        DailyDuaSequenceEntry(
            sequenceOrder = 12,
            timeOfDayStage = "12. Breaking the Fast (Iftari Dua)",
            stageUrdu = "۱۲۔ افطار کے وقت (روزہ کھولنے) کی مسنون دعا",
            duaId = "dua_ramadan_iftar",
            titleEnglish = "Upon Breaking the Fast (Iftar)",
            titleUrdu = "افطاری کے وقت کی مستند مسنون دعا",
            arabicText = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللّٰهُ",
            transliteration = "Zahaba al-zama'u wabtallatil-'urooqu wa thabatal-ajru in sha' Allah",
            AlternateTransliteration = "Dhahabadh-dhama'u wabtallatil-'urooqu wa thabatal-ajru in shaa' Allaah",
            englishTranslation = "The thirst is gone, the veins are moistened, and the reward is confirmed, if Allah wills.",
            urduTranslation = "پیاس چلی گئی، رگیں تر ہو گئیں اور ان شاء اللہ اجر پکا ہو گیا۔",
            authenticReference = "Sunan Abi Dawud 2357 (Graded Hasan by Al-Daraqutni, Al-Hakim 1/422, Ibn Hajar & Al-Albani — Hisn al-Muslim #176)",
            verificationNote = "Most authentic Prophetic narration upon breaking the fast, narrated by Abdullah ibn 'Umar رضي الله عنهما in Sunan Abi Dawud 2357. (Note: The popular phrase 'Allahumma inni laka sumtu wa bika aamantu wa 'ala rizqika aftartu' in Sunan Abi Dawud 2358 is a Mursal narration of Mu'adh ibn Zuhrah).",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n176.mp3"
        ),
        DailyDuaSequenceEntry(
            sequenceOrder = 13,
            timeOfDayStage = "13. Embarking on a Journey (Travel)",
            stageUrdu = "۱۳۔ سفر پر روانہ ہونے اور سواری پر بیٹھنے کی دعا",
            duaId = "dua_travel_riding",
            titleEnglish = "Supplication for Travel (Dua as-Safar)",
            titleUrdu = "سواری پر بیٹھنے اور سفر شروع کرنے کی دعا",
            arabicText = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَىٰ رَبِّنَا لَمُنْقَلِبُونَ",
            transliteration = "Subhaanalladhee sakhkhara lanaa haadhaa wa maa kunnaa lahoo muqrineena wa innaa ilaa Rabbinaa lamunqaliboon",
            englishTranslation = "Glory be to Him who has subjected this to us, and we could never have subdued it by our efforts; and indeed, to our Lord we shall surely return.",
            urduTranslation = "پاک ہے وہ ذات جس نے اس سواری کو ہمارے تابع کر دیا حالانکہ ہم اسے قابو میں لانے والے نہ تھے، اور بے شک ہم اپنے رب ہی کی طرف لوٹ کر جانے والے ہیں۔",
            authenticReference = "Quran 43:13–14; Sahih Muslim 1342 (Hisn al-Muslim #207)",
            verificationNote = "Verbatim Quranic verse (Az-Zukhruf 43:13) & Sahih Muslim 1342 — plays authentic human reciter verse audio.",
            quranSurahAyah = 43 to 13,
            humanAudioUrl = "https://everyayah.com/data/Alafasy_128kbps/043013.mp3"
        ),
        DailyDuaSequenceEntry(
            sequenceOrder = 14,
            timeOfDayStage = "14. Before Going to Sleep (Night)",
            stageUrdu = "۱۴۔ رات کو سونے سے پہلے کی مسنون دعا",
            duaId = "dua_sleep_before",
            titleEnglish = "Before Going to Sleep",
            titleUrdu = "رات کو سوتے وقت کی مسنون دعا",
            arabicText = "بِاسْمِكَ اللّٰهُمَّ أَمُوتُ وَأَحْيَا",
            transliteration = "Bismika Allaahumma amootu wa ahyaa",
            englishTranslation = "In Your Name, O Allah, I die (sleep) and I live (wake up).",
            urduTranslation = "اے اللہ! میں تیرے ہی نام کے ساتھ مرتا (سوتا) ہوں اور جیتا (جاگتا) ہوں۔",
            authenticReference = "Sahih al-Bukhari 6324; Sahih Muslim 2711 (Hisn al-Muslim #105)",
            verificationNote = "Recited alongside Ayat al-Kursi (2:255) and the Three Quls (112, 113, 114).",
            humanAudioUrl = "https://archive.org/download/HisnulMuslimAudio_201510/n105.mp3"
        )
    )
}
