package com.example.data.model

import android.content.Context
import com.example.data.local.ExtendedHadithDataProvider
import com.example.data.local.InitialDataLoader
import com.example.data.local.OfflineQuranAssetLoader
import com.example.data.local.entities.AyahEntity
import com.example.data.local.entities.HadithEntity
import java.util.Calendar

data class CuratedDailyAyahSeed(
  val id: String,
  val surahNumber: Int,
  val verseNumber: Int,
  val surahNameEnglish: String,
  val surahNameArabic: String,
  val themeBadgeEnglish: String,
  val themeBadgeUrdu: String,
  val reflectionSummaryEnglish: String,
  val reflectionSummaryUrdu: String,
  val pairedHadithCollectionKey: String,
  val pairedHadithNumber: Int,
  val pairedHadithReference: String,
  val pairedHadithSummaryEnglish: String,
  val pairedHadithSummaryUrdu: String,
  val shiaPairedHadithReference: String = "Al-Kafi Vol. 2, Kitab al-Iman wa al-Kufr, p. 426, Hadith 1 (Sahih)",
  val shiaPairedHadithSummaryEnglish: String = "Imam Muhammad al-Baqir & Imam Ja'far al-Sadiq (AS) taught that a believer never despairs of Allah's vast mercy and turns to Him with sincere repentance (Tawbah) and hope (Raja').",
  val shiaPairedHadithSummaryUrdu: String = "اصولِ کافی کی صحیح حدیث میں امام محمد باقر اور امام جعفر صادق علیہما السلام نے فرمایا کہ مومن اللّٰہ کی وسیع رحمت سے کبھی ناامید نہیں ہوتا اور سچی توبہ و امید کے ساتھ اللّٰہ کی طرف رجوع کرتا ہے۔",
  val fallbackArabic: String,
  val fallbackTransliteration: String,
  val fallbackEnglish: String,
  val fallbackUrdu: String
)

data class StepByStepLearningStage(
  val stepNumber: Int,
  val id: String,
  val stageTitleEnglish: String,
  val stageTitleUrdu: String,
  val stageTitleArabic: String,
  val subtitleEnglish: String,
  val quranReference: String,
  val quranSurahNumber: Int,
  val quranVerseNumber: Int,
  val hadithReference: String,
  val guidanceSummaryEnglish: String,
  val guidanceSummaryUrdu: String,
  val actionLabelEnglish: String,
  val actionTargetType: String // "LEARN_ISLAM", "QURAN_SURAH", "HADITH", "DUAS", "LIBRARY_HISTORY", "QAIDA_PDF"
)

data class IntelligentRecommendationItem(
  val id: String,
  val pillarCategory: String, // "SUNNAH_HADITH", "QURAN_TAFSIR", "DUA_CHARACTER", "HISTORY_SEERAH"
  val reasonBadgeEnglish: String,
  val reasonBadgeUrdu: String,
  val titleEnglish: String,
  val titleUrdu: String,
  val arabicSnippet: String,
  val englishSnippet: String,
  val urduSnippet: String,
  val authenticReference: String,
  val actionLabel: String,
  val actionType: String, // "QURAN_SURAH", "HADITH", "DUAS", "LEARN_ISLAM", "LIBRARY_TAB", "QAIDA_PDF"
  val targetSurahNumber: Int = 1,
  val targetVerseNumber: Int = 1,
  val targetLibraryTab: Int = 0
)

object DailyGuidanceAndRecommendationEngine {

  private const val PREFS_NAME = "bayan_daily_guidance_and_recs_v1"
  private const val KEY_DAILY_AYAH_OFFSET = "daily_ayah_manual_offset"
  private const val KEY_REC_ROTATION_INDEX = "intelligent_rec_rotation_index"
  private const val KEY_COMPLETED_LEARNING_STEPS = "completed_step_by_step_ids"
  private const val KEY_RECENTLY_SHOWN_REC_IDS = "recently_shown_rec_ids_csv"

  /**
   * Curated authentic Qur'anic verses encouraging reflection (Tadabbur), good character (Akhlaq),
   * hope & Allah's mercy (Rahmah), patience (Sabr), gratitude (Shukr), and comprehensive understanding of Islam.
   * Note: Whenever offline database/assets are available, the canonical Uthmani text from
   * `OfflineQuranAssetLoader` / `ayahDao` is used with 0% alteration.
   */
  val curatedDailyAyahSeeds: List<CuratedDailyAyahSeed> = listOf(
    CuratedDailyAyahSeed(
      id = "daily_ayah_39_53_mercy",
      surahNumber = 39,
      verseNumber = 53,
      surahNameEnglish = "Az-Zumar",
      surahNameArabic = "الزمر",
      themeBadgeEnglish = "Hope & Allah's Infinite Mercy (Rahmah)",
      themeBadgeUrdu = "امید اور اللّٰہ کی بے پایاں رحمت",
      reflectionSummaryEnglish = "Never despair of returning to Allah. Every sincere step of repentance is met with complete forgiveness and mercy.",
      reflectionSummaryUrdu = "اللّٰہ کی رحمت سے کبھی مایوس نہ ہوں؛ سچی توبہ اور رجوع سے اللّٰہ تمام گناہ معاف فرما دیتا ہے۔",
      pairedHadithCollectionKey = "muslim",
      pairedHadithNumber = 2752,
      pairedHadithReference = "Sahih Muslim 2752 • Book of Repentance",
      pairedHadithSummaryEnglish = "The Prophet ﷺ taught that Allah divided mercy into one hundred parts, keeping ninety-nine with Himself for the Day of Resurrection.",
      pairedHadithSummaryUrdu = "نبی کریم ﷺ نے فرمایا کہ اللّٰہ تعالیٰ کی سو رحمتیں ہیں جن میں سے ننانوے رحمتیں اس نے قیامت کے دن اپنے بندوں کے لیے رکھی ہیں۔",
      shiaPairedHadithReference = "Al-Kafi Vol. 2, Kitab al-Iman wa al-Kufr, Bab al-Tawbah, p. 426, Hadith 1 (Sahih)",
      shiaPairedHadithSummaryEnglish = "Imam Muhammad al-Baqir (AS) narrated in a Sahih tradition: 'Allah is more joyful at the repentance of His believing servant than a traveler in a dark night who finds his lost provisions.'",
      shiaPairedHadithSummaryUrdu = "الکافی جلد ۲ صفحہ ۴۲۶ کی صحیح حدیث میں امام محمد باقر علیہ السلام سے مروی ہے کہ اللّٰہ تعالیٰ اپنے مومن بندے کی توبہ سے اس مسافر سے بھی زیادہ خوش ہوتا ہے جسے تاریک رات میں اپنا کھویا ہوا زادِ راہ مل جائے۔",
      fallbackArabic = "قُلْ يَٰعِبَادِيَ ٱلَّذِينَ أَسْرَفُوا۟ عَلَىٰٓ أَنفُسِهِمْ لَا تَقْنَطُوا۟ مِن رَّحْمَةِ ٱللَّهِ ۚ إِنَّ ٱللَّهَ يَغْفِرُ ٱلذُّنُوبَ جَمِيعًا ۚ إِنَّهُۥ هُوَ ٱلْغَفُورُ ٱلرَّحِيمُ",
      fallbackTransliteration = "Qul ya 'ibadiyal-ladhina asrafu 'ala anfusihim la taqnatu min rahmatillah, innallaha yaghfirudh-dhunuba jami'a, innahu huwal-Ghafurur-Rahim.",
      fallbackEnglish = "Say, \"O My servants who have transgressed against themselves [by sinning], do not despair of the mercy of Allah. Indeed, Allah forgives all sins. Indeed, it is He who is the Forgiving, the Merciful.\"",
      fallbackUrdu = "کہہ دو کہ اے میرے بندو جنہوں نے اپنی جانوں پر زیادتی کی ہے اللّٰہ کی رحمت سے ناامید نہ ہونا، بے شک اللّٰہ سب گناہ بخش دیتا ہے، یقیناً وہی بخشنے والا مہربان ہے۔"
    ),
    CuratedDailyAyahSeed(
      id = "daily_ayah_16_90_justice_character",
      surahNumber = 16,
      verseNumber = 90,
      surahNameEnglish = "An-Nahl",
      surahNameArabic = "النحل",
      themeBadgeEnglish = "Justice, Excellence & Good Character (Adl & Ihsan)",
      themeBadgeUrdu = "عدل، احسان اور اعلیٰ اخلاق",
      reflectionSummaryEnglish = "A foundational verse summarizing Islam's moral code: fairness in every dealing, kindness beyond obligation, caring for relatives, and shunning indecency.",
      reflectionSummaryUrdu = "یہ آیتِ کریمہ اسلام کے پورے اخلاقی و سماجی نظام کا خلاصہ ہے: عدل، احسان، صلہ رحمی اور ہر برائی و ظلم سے اجتناب۔",
      pairedHadithCollectionKey = "bukhari",
      pairedHadithNumber = 6029,
      pairedHadithReference = "Sahih al-Bukhari 6029 • Book of Good Manners (Al-Adab)",
      pairedHadithSummaryEnglish = "The Prophet ﷺ said: \"The best among you are those who have the best manners and character.\"",
      pairedHadithSummaryUrdu = "رسول اللّٰہ ﷺ نے فرمایا: تم میں سب سےبہتر وہ ہے جس کے اخلاق سب سے اچھے ہیں۔",
      shiaPairedHadithReference = "Al-Kafi Vol. 2, Bab Husn al-Khuluq, p. 99, Hadith 1 (Sahih from Imam Ja'far al-Sadiq AS)",
      shiaPairedHadithSummaryEnglish = "Imam Ja'far al-Sadiq (AS) narrated from the Prophet ﷺ: 'The most complete of believers in faith are those with the best character, and nothing heavier is placed in the scale than noble manners.'",
      shiaPairedHadithSummaryUrdu = "اصولِ کافی جلد ۲ صفحہ ۹۹ کی صحیح حدیث میں امام جعفر صادق علیہ السلام سے مروی ہے کہ مومنین میں کامل ترین ایمان والا وہ ہے جس کے اخلاق سب سے بہترین ہوں۔",
      fallbackArabic = "إِنَّ ٱللَّهِ يَأْمُرُ بِٱلْعَدْلِ وَٱلْإِحْسَٰنِ وَإِيتَآئِ ذِى ٱلْقُرْبَىٰ وَيَنْهَىٰ عَنِ ٱلْفَحْشَآءِ وَٱلْمُنكَرِ وَٱلْبَغْىِ ۚ يَعِظُكُمْ لَعَلَّكُمْ تَذَكَّرُونَ",
      fallbackTransliteration = "Innallaha ya'muru bil-'adli wal-ihsani wa ita'i dhil-qurba wa yanha 'anil-fahsha'i wal-munkari wal-baghy, ya'izhukum la'allakum tadhakkarun.",
      fallbackEnglish = "Indeed, Allah orders justice and good conduct and giving to relatives and forbids immorality and bad conduct and oppression. He admonishes you that perhaps you will be reminded.",
      fallbackUrdu = "بے شک اللّٰہ عدل اور احسان اور قرابت داروں کو دینے کا حکم فرماتا ہے اور بے حیائی اور برے کاموں اور سرکشی سے منع فرماتا ہے، وہ تمہیں نصیحت کرتا ہے تاکہ تم نصیحت حاصل کرو۔"
    ),
    CuratedDailyAyahSeed(
      id = "daily_ayah_94_5_6_ease_after_hardship",
      surahNumber = 94,
      verseNumber = 5,
      surahNameEnglish = "Ash-Sharh",
      surahNameArabic = "الشرح",
      themeBadgeEnglish = "Relief, Patience & Optimism (Sabr & Yusr)",
      themeBadgeUrdu = "صبر، سکونِ قلب اور مشکل کے بعد آسانی",
      reflectionSummaryEnglish = "Allah promises twice in Surah Ash-Sharh that with every hardship comes ease. Trust His wisdom and keep moving forward in worship and service.",
      reflectionSummaryUrdu = "ہر تنگی کے ساتھ کشادگی اور آسانی جڑی ہوئی ہے؛ اللّٰہ پر بھروسہ رکھیں اور عمل و عبادت میں استقامت اختیار کریں۔",
      pairedHadithCollectionKey = "muslim",
      pairedHadithNumber = 2999,
      pairedHadithReference = "Sahih Muslim 2999 • Book of Zuhd & Softening of Hearts",
      pairedHadithSummaryEnglish = "The Prophet ﷺ said: \"Wondrous is the affair of the believer! Every matter is good for him: if prosperity comes he is grateful, and if adversity strikes he is patient.\"",
      pairedHadithSummaryUrdu = "نبی کریم ﷺ نے فرمایا: مومن کا معاملہ بھی عجیب ہے، اس کے ہر حال میں خیر ہے؛ خوشحالی ملے تو شکر کرتا ہے اور تکلیف پہنچے تو صبر کرتا ہے۔",
      shiaPairedHadithReference = "Al-Kafi Vol. 2, Bab al-Sabr, p. 89, Hadith 5 (Sahih from Imam Ali & Imam al-Sadiq AS)",
      shiaPairedHadithSummaryEnglish = "Imam Ali ibn Abi Talib & Imam Ja'far al-Sadiq (AS) taught: 'Patience (Sabr) to faith (Iman) is like the head to the body; and one hardship can never overcome two eases promised by Allah.'",
      shiaPairedHadithSummaryUrdu = "اصولِ کافی جلد ۲ صفحہ ۸۹ میں امیر المؤمنین علیؑ اور امام جعفر صادقؑ سے مروی ہے کہ ایمان میں صبر کی منزلت جسم میں سر کی طرح ہے، اور ایک تنگی کبھی دو آسانیوں پر غالب نہیں آ سکتی۔",
      fallbackArabic = "فَإِنَّ مَعَ ٱلْعُسْرِ يُسْرًا",
      fallbackTransliteration = "Fa-inna ma'al-'usri yusra.",
      fallbackEnglish = "For indeed, with hardship [will be] ease.",
      fallbackUrdu = "پس بے شک مشکل کے ساتھ آسانی ہے۔"
    ),
    CuratedDailyAyahSeed(
      id = "daily_ayah_13_28_peace_of_heart",
      surahNumber = 13,
      verseNumber = 28,
      surahNameEnglish = "Ar-Ra'd",
      surahNameArabic = "الرعد",
      themeBadgeEnglish = "Inner Peace & Remembrance of Allah (Dhikr)",
      themeBadgeUrdu = "ذکرِ الٰہی اور دلوں کا حقیقی اطمینان",
      reflectionSummaryEnglish = "True tranquility of heart is found in remembering Allah through the Qur'an, daily Salah, and morning/evening Prophetic supplications.",
      reflectionSummaryUrdu = "دلوں کا حقیقی چین اور سکون اللّٰہ کے ذکر، تلاوتِ قرآن اور مسنون اذکار ہی میں ہے۔",
      pairedHadithCollectionKey = "bukhari",
      pairedHadithNumber = 6407,
      pairedHadithReference = "Sahih al-Bukhari 6407 • Book of Invocations (Ad-Da'awat)",
      pairedHadithSummaryEnglish = "The Prophet ﷺ said: \"The example of the one who remembers his Lord and the one who does not is like that of the living and the dead.\"",
      pairedHadithSummaryUrdu = "نبی کریم ﷺ نے فرمایا: اپنے رب کا ذکر کرنے والے اور ذکر نہ کرنے والے کی مثال زندہ اور مردہ کی سی ہے۔",
      shiaPairedHadithReference = "Al-Kafi Vol. 2, Kitab al-Du'a, Bab Dhikr Allah, p. 499, Hadith 1 (Sahih from Imam Ja'far al-Sadiq AS)",
      shiaPairedHadithSummaryEnglish = "Imam Ja'far al-Sadiq (AS) said: 'There is a limit set for everything except the remembrance of Allah (Dhikr), for Allah has commanded remembering Him abundantly in every state.'",
      shiaPairedHadithSummaryUrdu = "الکافی جلد ۲ کتاب الدعاء صفحہ ۴۹۹ کی صحیح حدیث میں امام جعفر صادق علیہ السلام نے فرمایا: اللّٰہ نے ہر شے کی ایک حد مقرر کی ہے سوائے ذکرِ الٰہی کے، جس میں کثرت کا حکم دیا گیا ہے۔",
      fallbackArabic = "ٱلَّذِينَ ءَامَنُوا۟ وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ ٱللَّهِ ۗ أَلَا بِذِكْرِ ٱللَّهِ تَطْمَئِنُّ ٱلْقُلُوبُ",
      fallbackTransliteration = "Alladhina amanu wa tatma'innu qulubuhum bidhikrillah, ala bidhikrillahi tatma'innul-qulub.",
      fallbackEnglish = "Those who have believed and whose hearts are assured by the remembrance of Allah. Unquestionably, by the remembrance of Allah hearts are assured.",
      fallbackUrdu = "وہ لوگ جو ایمان لائے اور ان کے دل اللّٰہ کی یاد سے اطمینان پاتے ہیں، خبردار! اللّٰہ ہی کی یاد سے دل اطمینان پاتے ہیں۔"
    ),
    CuratedDailyAyahSeed(
      id = "daily_ayah_41_34_repel_evil_with_good",
      surahNumber = 41,
      verseNumber = 34,
      surahNameEnglish = "Fussilat",
      surahNameArabic = "فصلت",
      themeBadgeEnglish = "Forgiveness & Noble Conduct (Hilm & Akhlaq)",
      themeBadgeUrdu = "عفو و درگزر اور برائی کا جواب بھلائی سے",
      reflectionSummaryEnglish = "Responding to harshness with dignity and kindness transforms hostility into warm friendship and elevates a believer's rank.",
      reflectionSummaryUrdu = "برائی کا جواب بہترین اخلاق اور نرمی سے دینے سے دشمنی بھی گہری دوستی میں بدل جاتی ہے۔",
      pairedHadithCollectionKey = "tirmidhi",
      pairedHadithNumber = 1987,
      pairedHadithReference = "Jami` at-Tirmidhi 1987 • Book of Righteousness (Hasan Sahih)",
      pairedHadithSummaryEnglish = "The Prophet ﷺ said: \"Fear Allah wherever you are, follow a bad deed with a good deed to erase it, and treat people with noble character.\"",
      pairedHadithSummaryUrdu = "رسول اللّٰہ ﷺ نے فرمایا: جہاں کہیں ہو اللّٰہ سے ڈرو، برائی کے بعد نیکی کرو جو اسے مٹا دے، اور لوگوں سے اچھے اخلاق کے ساتھ پیش آؤ۔",
      shiaPairedHadithReference = "Al-Kafi Vol. 2, Bab al-'Afw wa al-Hilm, p. 107, Hadith 3 (Sahih from Imam Ali Zayn al-'Abidin & Imam al-Sadiq AS)",
      shiaPairedHadithSummaryEnglish = "Imam Ja'far al-Sadiq (AS) narrated from the Prophet ﷺ: 'Shall I inform you of the noblest morals of this world and the Hereafter? Forgiving the one who wronged you, reconnecting with the one who cut you off, and doing good to the one who mistreated you.'",
      shiaPairedHadithSummaryUrdu = "الکافی جلد ۲ صفحہ ۱۰۷ میں امام جعفر صادقؑ نے رسول اللّٰہ ﷺ کا ارشاد نقل فرمایا: دنیا و آخرت کے بہترین اخلاق یہ ہیں کہ جو تم پر ظلم کرے اسے معاف کرو، جو قطع رحمی کرے اس سے جڑو اور جو برائی کرے اس سے احسان کرو۔",
      fallbackArabic = "وَلَا تَسْتَوِى ٱلْحَسَنَةُ وَلَا ٱلسَّيِّئَةُ ۚ ٱدْفَعْ بِٱلَّتِى هِىَ أَحْسَنُ فَإِذَا ٱلَّذِى بَيْنَكَ وَبَيْنَهُۥ عَدَٰوَةٌ كَأَنَّهُۥ وَلِىٌّ حَمِيمٌ",
      fallbackTransliteration = "Wa la tastawil-hasanatu wa las-sayyi'ah, idfa' billati hiya ahsanu fa-idhal-ladhi baynaka wa baynahu 'adawatun ka'annahu waliyyun hamim.",
      fallbackEnglish = "And not equal are the good deed and the bad. Repel [evil] by that [deed] which is better; and thereupon the one whom between you and him is enmity [will become] as though he was a devoted friend.",
      fallbackUrdu = "اور نیکی اور بدی برابر نہیں ہو سکتیں، تم (برائی کو) اس طریقے سے دور کرو جو سب سے اچھا ہو، پھر وہی شخص جس کے اور تمہارے درمیان دشمنی تھی ایسا ہو جائے گا گویا وہ گہرا دوست ہے۔"
    ),
    CuratedDailyAyahSeed(
      id = "daily_ayah_38_29_reflection_on_quran",
      surahNumber = 38,
      verseNumber = 29,
      surahNameEnglish = "Sad",
      surahNameArabic = "ص",
      themeBadgeEnglish = "Deep Reflection & Understanding (Tadabbur)",
      themeBadgeUrdu = "تدبرِ قرآن اور فہمِ دین",
      reflectionSummaryEnglish = "The Qur'an was revealed so hearts reflect deeply upon its verses and live by its wisdom step by step.",
      reflectionSummaryUrdu = "قرآنِ مجید اس لیے نازل کیا گیا ہے تاکہ لوگ اس کی آیات میں غور و فکر کریں اور عقل و بصیرت والے نصیحت حاصل کریں۔",
      pairedHadithCollectionKey = "bukhari",
      pairedHadithNumber = 5027,
      pairedHadithReference = "Sahih al-Bukhari 5027 • Virtues of the Qur'an",
      pairedHadithSummaryEnglish = "The Prophet ﷺ said: \"The best among you are those who learn the Qur'an and teach it.\"",
      pairedHadithSummaryUrdu = "رسول اللّٰہ ﷺ نے فرمایا: تم میں سب سے بہتر وہ شخص ہے جو قرآن سیکھے اور اوروں کو سکھائے۔",
      shiaPairedHadithReference = "Al-Kafi Vol. 2, Kitab Fadl al-Qur'an, p. 599, Hadith 2 (Sahih from Imam Ja'far al-Sadiq AS)",
      shiaPairedHadithSummaryEnglish = "Imam Ja'far al-Sadiq (AS) said: 'The Qur'an is the covenant of Allah to His creation; therefore a Muslim ought to look into His covenant and read and reflect upon its verses daily.'",
      shiaPairedHadithSummaryUrdu = "الکافی جلد ۲ کتاب فضل القرآن صفحہ ۵۹۹ کی صحیح حدیث میں امام جعفر صادق علیہ السلام نے فرمایا: قرآن مخلوق کے نام اللّٰہ کا عہد نامہ ہے، پس ہر مسلمان کو چاہیے کہ روزانہ اپنے رب کے عہد نامے میں غور و تدبر کرے۔",
      fallbackArabic = "كِتَٰبٌ أَنزَلْنَٰهُ إِلَيْكَ مُبَٰرَكٌ لِّيَدَّبَّرُوٓا۟ ءَايَٰتِهِۦ وَلِيَتَذَكَّرَ أُو۟لُوا۟ ٱلْأَلْبَٰبِ",
      fallbackTransliteration = "Kitabun anzalnahu ilayka mubarakul-liyaddabbaru ayatihi wa liyatadhakkara ulul-albab.",
      fallbackEnglish = "[This is] a blessed Book which We have revealed to you, [O Muhammad], that they might reflect upon its verses and that those of understanding would be reminded.",
      fallbackUrdu = "یہ ایک بابرکت کتاب ہے جو ہم نے آپ کی طرف نازل فرمائی ہے تاکہ لوگ اس کی آیات میں غور و فکر کریں اور تاکہ عقل والے نصیحت حاصل کریں۔"
    ),
    CuratedDailyAyahSeed(
      id = "daily_ayah_2_286_mercy_and_capacity",
      surahNumber = 2,
      verseNumber = 286,
      surahNameEnglish = "Al-Baqarah",
      surahNameArabic = "البقرة",
      themeBadgeEnglish = "Compassion, Ease & Supplication (Rahmah & Dua)",
      themeBadgeUrdu = "اللّٰہ کی نرمی، وسعت اور مسنون دعا",
      reflectionSummaryEnglish = "Allah never burdens any soul beyond its capacity. The closing verse of Surah Al-Baqarah teaches us how to ask Allah for pardon, strength, and mercy.",
      reflectionSummaryUrdu = "اللّٰہ کسی جان پر اس کی طاقت سے بڑھ کر بوجھ نہیں ڈالتا؛ سورۃ البقرہ کی یہ آخری آیت مومن کو عفو، مغفرت اور رحمت کی جامع دعا سکھاتی ہے۔",
      pairedHadithCollectionKey = "muslim",
      pairedHadithNumber = 126,
      pairedHadithReference = "Sahih Muslim 126 • Book of Faith (Al-Iman)",
      pairedHadithSummaryEnglish = "When the believers prayed 'Our Lord, do not impose blame upon us if we forget or make a mistake,' Allah replied: 'I have done so (granted it).'",
      pairedHadithSummaryUrdu = "صحیح مسلم کے مطابق جب صحابہ نے سورۃ البقرہ کی آخری آیت کی ہر دعا پڑھی تو اللّٰہ تعالیٰ نے فرمایا: قَدْ فَعَلْتُ (میں نے قبول فرما لیا)۔",
      shiaPairedHadithReference = "Al-Kafi Vol. 2, Bab al-Taklif, p. 86 & Man La Yahduruhu al-Faqih Vol. 1, p. 308 (Sahih)",
      shiaPairedHadithSummaryEnglish = "Imam Ja'far al-Sadiq (AS) explained regarding 'Allah does not burden a soul beyond its capacity' that Allah's religion is built upon ease, mercy, and lifting of unintentional error and compulsion.",
      shiaPairedHadithSummaryUrdu = "الکافی جلد ۲ اور من لا یحضرہ الفقیہ میں امام جعفر صادق علیہ السلام سے مروی ہے کہ اللّٰہ نے کسی نفس پر اس کی طاقت سے زیادہ بوجھ نہیں رکھا اور امت سے خطا، نسیان اور مجبوری کو معاف فرما دیا ہے۔",
      fallbackArabic = "لَا يُكَلِّفُ ٱللَّهُ نَفْسًا إِلَّا وُسْعَهَا",
      fallbackTransliteration = "La yukallifullahu nafsan illa wus'aha.",
      fallbackEnglish = "Allah does not charge a soul except [with that within] its capacity.",
      fallbackUrdu = "اللّٰہ کسی جان کو اس کی وسعت سے زیادہ تکلیف نہیں دیتا۔"
    )
  )

  /**
   * 5-Stage Step-by-Step Guided Learning Path that takes a user from initial inspiration
   * in the Daily Aayah to comprehensive guidance across Qur'an, Tajweed, Salah, Sahih Hadith, and History.
   */
  val stepByStepLearningPath: List<StepByStepLearningStage> = listOf(
    StepByStepLearningStage(
      stepNumber = 1,
      id = "step_1_mercy_and_fatihah",
      stageTitleEnglish = "Step 1: Heart & Hope — Surah Al-Fatihah & Core Faith",
      stageTitleUrdu = "مرحلہ ۱: ایمان، توحید اور سورۃ الفاتحہ کا فہم",
      stageTitleArabic = "١. الإيمان وفاتحة الكتاب",
      subtitleEnglish = "Understand the opening conversation with Allah (Surah 1:1–7) & the 6 Kalimas of faith.",
      quranReference = "Qur'an 1:1–7 & 39:53",
      quranSurahNumber = 1,
      quranVerseNumber = 1,
      hadithReference = "Sahih Muslim 395 (Allah answers every verse of Al-Fatihah)",
      guidanceSummaryEnglish = "Start by reading Surah Al-Fatihah with translation and Tafsir: in Hadith Qudsi (Sahih Muslim 395), Allah says 'I have divided the prayer between Myself and My servant into two halves, and My servant shall have what he asks for.'",
      guidanceSummaryUrdu = "حدیثِ قدسی (صحیح مسلم ۳۹۵) میں اللّٰہ تعالیٰ فرماتا ہے کہ میں نے سورۃ الفاتحہ اپنے اور اپنے بندے کے درمیان آدھی آدھی تقسیم کر لی ہے اور میرے بندے کے لیے وہی ہے جو وہ مانگے۔",
      actionLabelEnglish = "Open Surah Al-Fatihah + Tafsir",
      actionTargetType = "QURAN_SURAH"
    ),
    StepByStepLearningStage(
      stepNumber = 2,
      id = "step_2_noorani_qaida_tajweed",
      stageTitleEnglish = "Step 2: Accurate Recitation — Noorani Qaida & Tajweed",
      stageTitleUrdu = "مرحلہ ۲: نورانی قاعدہ اور مخارج و تجوید کی مشق",
      stageTitleArabic = "٢. القاعدة النورانية والتجويد",
      subtitleEnglish = "Master Arabic letters, Harakat, Madd, and Tajweed across 17 interactive lessons & 53 HD pages.",
      quranReference = "Qur'an 73:4 (Warattilil-Qur'ana tartila)",
      quranSurahNumber = 73,
      quranVerseNumber = 4,
      hadithReference = "Sahih al-Bukhari 5027 & Sunan Abi Dawud 1468",
      guidanceSummaryEnglish = "Practice letter pronunciation (Makharij) with authentic human voice in the 17 Noorani Qaida lessons and test your understanding with interactive MCQs in the 53-page HD book.",
      guidanceSummaryUrdu = "سترہ اسباق اور ۵۳ صفحات پر مشتمل نورانی قاعدہ اور ایم سی کیو ٹیسٹ کے ذریعے حروف کے مخارج اور تجوید کے قواعد مضبوط کریں۔",
      actionLabelEnglish = "Practice Noorani Qaida & Tajweed",
      actionTargetType = "LEARN_ISLAM"
    ),
    StepByStepLearningStage(
      stepNumber = 3,
      id = "step_3_wudu_and_salah",
      stageTitleEnglish = "Step 3: Daily Connection — Prophetic Wudu & Complete Salah",
      stageTitleUrdu = "مرحلہ ۳: مسنون وضو اور مکمل نمازِ مترجم",
      stageTitleArabic = "٣. الوضوء والصلاة النبوية",
      subtitleEnglish = "Learn every phrase of Wudu and Salah word-by-word with Arabic, Urdu & English meaning.",
      quranReference = "Qur'an 29:45 & 20:14",
      quranSurahNumber = 29,
      quranVerseNumber = 45,
      hadithReference = "Sahih al-Bukhari 631 ('Pray as you have seen me praying')",
      guidanceSummaryEnglish = "When you know the meaning of Sana, Ruku, Sajdah, Tashahhud, and Durood-e-Ibrahimi, your 5 daily prayers become a living conversation with Allah.",
      guidanceSummaryUrdu = "جب آپ ثناء، رکوع، سجدہ، تشہد اور درودِ ابراہیمی کے معانی سمجھ کر پڑھتے ہیں تو نماز خشوع و خضوع کا ذریعہ بن جاتی ہے۔",
      actionLabelEnglish = "Learn Wudu & Complete Salah",
      actionTargetType = "LEARN_ISLAM"
    ),
    StepByStepLearningStage(
      stepNumber = 4,
      id = "step_4_prophetic_sunnah_and_hadith",
      stageTitleEnglish = "Step 4: Prophetic Character — Authentic Hadith & Daily Duas",
      stageTitleUrdu = "مرحلہ ۴: احادیثِ نبویہ، اخلاقِ حسنہ اور مسنون دعائیں",
      stageTitleArabic = "٤. السنة النبوية والأخلاق والأذكار",
      subtitleEnglish = "Explore Sahih al-Bukhari, Sahih Muslim, 40 Nawawi, Riyad as-Salihin & 45 Azkar categories.",
      quranReference = "Qur'an 33:21 & 68:4",
      quranSurahNumber = 33,
      quranVerseNumber = 21,
      hadithReference = "Sahih Muslim 746 ('His character was the Qur'an')",
      guidanceSummaryEnglish = "Aishah (RA) described the Prophet ﷺ by saying 'His character was the Qur'an.' Read verified Prophetic traditions on honesty, mercy, family kindness, and daily remembrance.",
      guidanceSummaryUrdu = "سیدہ عائشہؓ نے فرمایا کہ نبی کریم ﷺ کا اخلاق سراپا قرآن تھا۔ مستند کتبِ حدیث اور مسنون دعاؤں سے اپنی روزمرہ زندگی سنواریں۔",
      actionLabelEnglish = "Explore Verified Hadith Books",
      actionTargetType = "HADITH"
    ),
    StepByStepLearningStage(
      stepNumber = 5,
      id = "step_5_tafsir_seerah_and_history",
      stageTitleEnglish = "Step 5: Comprehensive Wisdom — Tafsir, Seerah & Classical History",
      stageTitleUrdu = "مرحلہ ۵: تفاسیر، سیرت النبی ﷺ، قصص الانبیاء اور تاریخِ اسلام",
      stageTitleArabic = "٥. التفسير والسيرة والتاريخ الإسلامي",
      subtitleEnglish = "Study 8 classical Tafsirs, 25 Prophet Stories, Sahaba & Ahl al-Bayt biographies, and 152 Classical Books.",
      quranReference = "Qur'an 12:111 & 38:29",
      quranSurahNumber = 12,
      quranVerseNumber = 111,
      hadithReference = "Jami` at-Tirmidhi 2682 (Path to Knowledge leads to Paradise)",
      guidanceSummaryEnglish = "Deepen your understanding by comparing classical Tafsirs (Ibn Kathir, Al-Tabari, Maariful Quran, Tafheem-ul-Quran) and reading primary historical chronicles.",
      guidanceSummaryUrdu = "مستند تفاسیر، پچیس انبیاء کرام کے واقعات، صحابہ و اہلِ بیت کے تراجم اور کلاسیکی کتبِ تاریخ کا مطالعہ کریں۔",
      actionLabelEnglish = "Open Classical & History Library",
      actionTargetType = "LIBRARY_HISTORY"
    )
  )

  /**
   * 5-Stage Step-by-Step Guided Learning Path for the Shia (Ja'fari) Home Screen,
   * grounded in the Holy Qur'an, Al-Kafi, Man La Yahduruhu al-Faqih, Tahdhib al-Ahkam, Al-Sahifa al-Sajjadiyya & Tafsir al-Mizan.
   */
  val shiaStepByStepLearningPath: List<StepByStepLearningStage> = listOf(
    StepByStepLearningStage(
      stepNumber = 1,
      id = "step_1_mercy_and_fatihah",
      stageTitleEnglish = "Step 1: Heart & Hope: Surah Al-Fatihah & Usul al-Din",
      stageTitleUrdu = "مرحلہ ۱: اصولِ دین، توحید و عدل اور سورۃ الفاتحہ کا فہم",
      stageTitleArabic = "١. أصول الدين وفاتحة الكتاب",
      subtitleEnglish = "Understand Surah Al-Fatihah (with Bismillah as an integral verse) and the 5 Usul al-Din.",
      quranReference = "Qur'an 1:1–7 & 39:53",
      quranSurahNumber = 1,
      quranVerseNumber = 1,
      hadithReference = "Al-Kafi Vol. 3, p. 312 & Man La Yahduruhu al-Faqih Vol. 1, p. 304 (Sahih)",
      guidanceSummaryEnglish = "In Al-Kafi and Man La Yahduruhu al-Faqih, Imam Muhammad al-Baqir and Imam Ja'far al-Sadiq (AS) explain that Bismillahir-Rahmanir-Rahim is the noblest verse of the Qur'an and Allah responds to every verse of Surah Al-Fatihah recited by His servant.",
      guidanceSummaryUrdu = "الکافی اور من لا یحضرہ الفقیہ کی صحیح احادیث میں امام محمد باقر اور امام جعفر صادق علیہما السلام نے فرمایا کہ بسم اللّٰہ الرحمٰن الرحیم سورۃ الفاتحہ کی پہلی آیت ہے اور اللّٰہ تعالیٰ سورۃ الفاتحہ کی ہر آیت پر اپنے بندے کو جواب عطا فرماتا ہے۔",
      actionLabelEnglish = "Open Surah Al-Fatihah + Shia Tafsir",
      actionTargetType = "QURAN_SURAH"
    ),
    StepByStepLearningStage(
      stepNumber = 2,
      id = "step_2_noorani_qaida_tajweed",
      stageTitleEnglish = "Step 2: Accurate Recitation: Noorani Qaida & Tajweed",
      stageTitleUrdu = "مرحلہ ۲: نورانی قاعدہ اور مخارج و تجوید کی مشق",
      stageTitleArabic = "٢. القاعدة النورانية وترتيل القرآن",
      subtitleEnglish = "Master Arabic letters, Harakat, Madd, and Tartil across 17 audio lessons & 53 HD pages.",
      quranReference = "Qur'an 73:4 (Warattilil-Qur'ana tartila)",
      quranSurahNumber = 73,
      quranVerseNumber = 4,
      hadithReference = "Al-Kafi Vol. 2, Kitab Fadl al-Qur'an, p. 614, Hadith 1 (Sahih from Imam al-Sadiq AS)",
      guidanceSummaryEnglish = "In Al-Kafi Vol. 2, p. 614, Imam Ja'far al-Sadiq (AS) explained 'Warattilil-Qur'ana tartila': 'Make its letters clear and distinct, and do not scatter it hastily like sand.'",
      guidanceSummaryUrdu = "الکافی جلد ۲ صفحہ ۶۱۴ کی صحیح حدیث میں امام جعفر صادق علیہ السلام نے آیت وَرَتِّلِ الْقُرْآنَ تَرْتِيلاً کی تفسیر میں فرمایا کہ قرآن کے حروف کو واضح اور ٹھہر ٹھہر کر پڑھو۔",
      actionLabelEnglish = "Practice Noorani Qaida & Tajweed",
      actionTargetType = "LEARN_ISLAM"
    ),
    StepByStepLearningStage(
      stepNumber = 3,
      id = "step_3_wudu_and_salah",
      stageTitleEnglish = "Step 3: Daily Connection: Ja'fari Wudu & Complete Salah",
      stageTitleUrdu = "مرحلہ ۳: فقہِ جعفری کے مطابق وضو اور مکمل نماز",
      stageTitleArabic = "٣. الوضوء البياني والصلاة الجعفرية",
      subtitleEnglish = "Learn descriptive Wudu (washing face/arms, wiping head/feet), Qunut, Prostration on Earth & Tasbih of Fatimah (SA).",
      quranReference = "Qur'an 5:6 & 17:78",
      quranSurahNumber = 17,
      quranVerseNumber = 78,
      hadithReference = "Al-Kafi Vol. 3, pp. 24–31 & 330; Tahdhib al-Ahkam Vol. 2, p. 60",
      guidanceSummaryEnglish = "Study the authentic descriptive narrations of Imam al-Baqir and Imam al-Sadiq (AS) on Wudu, the 17 obligatory and 34 recommended daily Rak'ahs, Qunut in the second Rak'ah, and Tasbih of Sayyidah Fatimah al-Zahra (SA).",
      guidanceSummaryUrdu = "امام محمد باقر اور امام جعفر صادق علیہما السلام کی بیانیہ احادیث کی روشنی میں وضو، سترہ فرائض و چونتیس نوافل، قنوت، زمین پر سجدہ اور تسبیحِ حضرت فاطمہ زہراؑ سیکھیں۔",
      actionLabelEnglish = "Learn Ja'fari Wudu & Salah",
      actionTargetType = "LEARN_ISLAM"
    ),
    StepByStepLearningStage(
      stepNumber = 4,
      id = "step_4_prophetic_sunnah_and_hadith",
      stageTitleEnglish = "Step 4: Ahl al-Bayt Wisdom: Al-Kutub al-Arba'ah & Sahifa Sajjadiyya",
      stageTitleUrdu = "مرحلہ ۴: کتبِ اربعہ کی احادیث اور صحیفہ سجادیہ کی دعائیں",
      stageTitleArabic = "٤. أحاديث أهل البيت والصحيفة السجادية",
      subtitleEnglish = "Explore Al-Kafi, Man La Yahduruhu al-Faqih, Tahdhib al-Ahkam, Al-Istibsar & Al-Sahifa al-Sajjadiyya.",
      quranReference = "Qur'an 33:33 & 42:23",
      quranSurahNumber = 33,
      quranVerseNumber = 33,
      hadithReference = "Al-Kafi Vol. 1, Kitab Fadl al-'Ilm, p. 32 & Al-Sahifa al-Sajjadiyya",
      guidanceSummaryEnglish = "Study verified Prophetic and Imami traditions in the Four Canonical Books (Al-Kutub al-Arba'ah) alongside the supplications of Imam Ali Zayn al-'Abidin (AS) in Al-Sahifa al-Sajjadiyya.",
      guidanceSummaryUrdu = "کتبِ اربعہ (الکافی، من لا یحضرہ الفقیہ، تہذیب الاحکام، الاستبصار) کی مستند احادیث اور امام زین العابدینؑ کے صحیفہ سجادیہ کا مطالعہ کریں۔",
      actionLabelEnglish = "Explore Al-Kafi & Shia Hadith",
      actionTargetType = "HADITH"
    ),
    StepByStepLearningStage(
      stepNumber = 5,
      id = "step_5_tafsir_seerah_and_history",
      stageTitleEnglish = "Step 5: Comprehensive Scholarship: Tafsir al-Mizan, Nahj al-Balagha & History",
      stageTitleUrdu = "مرحلہ ۵: تفسیر المیزان، مجمع البیان، نہج البلاغہ اور تاریخِ اسلام",
      stageTitleArabic = "٥. تفسير الميزان ونهج البلاغة والتاريخ",
      subtitleEnglish = "Study Tafsir al-Mizan (Tabataba'i), Majma' al-Bayan (Tabarsi), Nahj al-Balagha & classical biographies.",
      quranReference = "Qur'an 12:111 & 38:29",
      quranSurahNumber = 12,
      quranVerseNumber = 111,
      hadithReference = "Al-Kafi Vol. 1, p. 34, Hadith 1 (Sahih: 'Whoever travels a path seeking knowledge...')",
      guidanceSummaryEnglish = "Deepen your research through classical and contemporary Shia Tafsirs (Tafsir al-Mizan, Majma' al-Bayan, Al-Tibyān, Safi), Nahj al-Balagha, and primary historical chronicles.",
      guidanceSummaryUrdu = "تفسیر المیزان، مجمع البیان، التبیان، نہج البلاغہ اور انبیاء و اہلِ بیتؑ کے مستند تاریخی مآخذ کا مطالعہ کریں۔",
      actionLabelEnglish = "Open Classical & History Library",
      actionTargetType = "LIBRARY_HISTORY"
    )
  )

  fun getSavedDailyAyahOffset(context: Context): Int {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getInt(KEY_DAILY_AYAH_OFFSET, 0)
  }

  fun saveDailyAyahOffset(context: Context, offset: Int) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .putInt(KEY_DAILY_AYAH_OFFSET, offset)
      .apply()
  }

  fun getCompletedLearningSteps(context: Context): Set<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getStringSet(KEY_COMPLETED_LEARNING_STEPS, emptySet())?.toSet() ?: emptySet()
  }

  fun toggleLearningStepCompleted(context: Context, stepId: String): Set<String> {
    val current = getCompletedLearningSteps(context).toMutableSet()
    if (current.contains(stepId)) {
      current.remove(stepId)
    } else {
      current.add(stepId)
    }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .putStringSet(KEY_COMPLETED_LEARNING_STEPS, current)
      .apply()
    return current
  }

  /**
   * Resolves the active curated Daily Aayah seed for today's calendar day + user's manual step offset.
   */
  fun getActiveDailyAyahSeed(context: Context, manualOffset: Int = getSavedDailyAyahOffset(context)): CuratedDailyAyahSeed {
    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    val total = curatedDailyAyahSeeds.size
    val idx = ((dayOfYear + manualOffset) % total + total) % total
    return curatedDailyAyahSeeds[idx]
  }

  /**
   * Resolves the canonical `AyahEntity` from offline assets (preserving 100% authentic Uthmani text)
   * for the active `CuratedDailyAyahSeed`, falling back to `fallbackAyah` only if assets are unavailable.
   */
  fun resolveAuthenticDailyAyahEntity(
    context: Context,
    seed: CuratedDailyAyahSeed,
    fallbackAyah: AyahEntity? = null
  ): AyahEntity {
    val surahAyahs = runCatching {
      OfflineQuranAssetLoader.loadSurahFromAssets(context, seed.surahNumber)
    }.getOrDefault(emptyList())
    val exactFromAsset = surahAyahs.firstOrNull { it.verseNumber == seed.verseNumber }
    if (exactFromAsset != null) {
      return exactFromAsset
    }
    val foundational = InitialDataLoader.getFoundationalAyahs().firstOrNull {
      it.surahNumber == seed.surahNumber && it.verseNumber == seed.verseNumber
    }
    if (foundational != null) {
      return foundational
    }
    if (fallbackAyah != null && fallbackAyah.surahNumber == seed.surahNumber && fallbackAyah.verseNumber == seed.verseNumber) {
      return fallbackAyah
    }
    return AyahEntity(
      surahNumber = seed.surahNumber,
      verseNumber = seed.verseNumber,
      textArabic = seed.fallbackArabic,
      translationEnglish = seed.fallbackEnglish,
      translationUrdu = seed.fallbackUrdu,
      transliteration = seed.fallbackTransliteration,
      juzNumber = 1,
      pageNumber = 1
    )
  }

  /**
   * Resolves a verified, authentic Hadith for today's reflection or paired with the Daily Aayah.
   */
  fun resolveAuthenticDailyHadith(
    seed: CuratedDailyAyahSeed,
    fallbackHadith: HadithEntity? = null
  ): HadithEntity {
    val allExtended = ExtendedHadithDataProvider.getExtendedHadiths() + InitialDataLoader.getFoundationalHadiths()
    val exactPaired = allExtended.firstOrNull {
      it.collectionKey.equals(seed.pairedHadithCollectionKey, ignoreCase = true) &&
        it.hadithNumber == seed.pairedHadithNumber
    }
    if (exactPaired != null) return exactPaired

    val sameCollection = allExtended.firstOrNull {
      it.collectionKey.equals(seed.pairedHadithCollectionKey, ignoreCase = true) &&
        it.arabicText.isNotBlank() &&
        it.englishText.isNotBlank() &&
        it.urduText.isNotBlank()
    }
    if (sameCollection != null) return sameCollection

    return fallbackHadith ?: InitialDataLoader.getFoundationalHadiths().first()
  }

  /**
   * Generates context-aware, non-repetitive recommendations tailored to the user's reading progress,
   * time of day, day of week, Noorani Qaida progress, and rotation index.
   */
  fun buildIntelligentRecommendations(
    context: Context,
    lastReadSurahNumber: Int,
    lastReadSurahNameEn: String,
    lastReadVerseNumber: Int,
    isFridayOrThursdayEvening: Boolean,
    rotationOffset: Int = getSavedRecommendationRotation(context),
    isShiaTradition: Boolean = false
  ): List<IntelligentRecommendationItem> {
    val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val qaidaPage = runCatching {
      context.getSharedPreferences("bayan_learn_islam_prefs", Context.MODE_PRIVATE)
        .getInt("last_read_noorani_qaida_page", 1)
        .coerceIn(1, 53)
    }.getOrDefault(1)

    if (isShiaTradition) {
      val shiaHadithPool = listOf(
        IntelligentRecommendationItem(
          id = "rec_shia_kafi_intellect_and_knowledge_1",
          pillarCategory = "SUNNAH_HADITH",
          reasonBadgeEnglish = "Al-Kafi Foundational Hadith • Intellect & Sincerity ('Aql & Ikhlas)",
          reasonBadgeUrdu = "اصولِ کافی کی بنیادی حدیث • عقل، علم اور اخلاص",
          titleEnglish = "Intellect ('Aql) Is the Pillar by Which the All-Merciful Is Worshipped",
          titleUrdu = "عقل وہ جوہر ہے جس کے ذریعے رحمٰن کی عبادت کی جاتی ہے",
          arabicSnippet = "لَمَّا خَلَقَ اللَّهُ الْعَقْلَ اسْتَنْطَقَهُ... بِكَ أُثِيبُ وَبِكَ أُعَاقِبُ",
          englishSnippet = "In the opening Hadith of Al-Kafi, Imam Muhammad al-Baqir (AS) narrates that Allah created the Intellect ('Aql) as the foundation of sincere obedience, understanding, and moral accountability.",
          urduSnippet = "الکافی کی سب سے پہلی حدیث میں امام محمد باقر علیہ السلام فرماتے ہیں کہ اللّٰہ تعالیٰ نے عقل کو اطاعت، معرفت اور اخلاص کی بنیاد بنایا ہے۔",
          authenticReference = "Al-Kafi Vol. 1, Kitab al-'Aql wa al-Jahl, p. 10, Hadith 1",
          actionLabel = "Read in Al-Kafi Library",
          actionType = "HADITH"
        ),
        IntelligentRecommendationItem(
          id = "rec_shia_kafi_prayer_on_time_and_parents",
          pillarCategory = "SUNNAH_HADITH",
          reasonBadgeEnglish = "Al-Kutub al-Arba'ah • Prayer in Waqt al-Fadilah & Kindness to Parents",
          reasonBadgeUrdu = "کتبِ اربعہ • اول وقت میں نماز اور والدین سے حسنِ سلوک",
          titleEnglish = "The Most Beloved Deeds: Prayer in Its Prime Time & Birr al-Walidayn",
          titleUrdu = "اللّٰہ کے نزدیک محبوب ترین اعمال: اول وقت میں نماز اور والدین کی خدمت",
          arabicSnippet = "أَحَبُّ الْأَعْمَالِ إِلَى اللَّهِ عَزَّ وَجَلَّ الصَّلَاةُ لِوَقْتِهَا وَبِرُّ الْوَالِدَيْنِ",
          englishSnippet = "Imam Ja'far al-Sadiq (AS) narrated in a Sahih tradition in Al-Kafi that the noblest of deeds to Allah are offering obligatory prayers at the beginning of their prime time (Waqt al-Fadilah) and kindness to parents.",
          urduSnippet = "الکافی جلد ۳ کی صحیح حدیث میں امام جعفر صادق علیہ السلام نے فرمایا کہ اللّٰہ عزوجل کے نزدیک سب سے محبوب عمل نماز کو اس کے اول وقتِ فضیلت میں ادا کرنا اور والدین کے ساتھ نیکی کرنا ہے۔",
          authenticReference = "Al-Kafi Vol. 3, Kitab al-Salah, p. 264, Hadith 1 & Vol. 2, p. 157 (Sahih)",
          actionLabel = "Explore Al-Kafi Hadiths",
          actionType = "HADITH"
        ),
        IntelligentRecommendationItem(
          id = "rec_shia_tasbih_fatimah_zahra",
          pillarCategory = "SUNNAH_HADITH",
          reasonBadgeEnglish = "Daily Ja'fariTa'qib • Better Than 1,000 Voluntary Rak'ats",
          reasonBadgeUrdu = "تعقیباتِ نماز • ہزار نفل رکعات سے افضل ذکر",
          titleEnglish = "Tasbih of Sayyidah Fatimah al-Zahra (SA) After Every Prayer",
          titleUrdu = "ہر نماز کے بعد تسبیحِ حضرت فاطمہ زہرا سلام اللّٰہ علیہا",
          arabicSnippet = "اللَّهُ أَكْبَرُ (٣٤) • الْحَمْدُ لِلَّهِ (٣٣) • سُبْحَانَ اللَّهِ (٣٣)",
          englishSnippet = "Imam Muhammad al-Baqir and Imam Ja'far al-Sadiq (AS) stated: 'The Tasbih of Fatimah al-Zahra (SA) after every obligatory prayer is more beloved to me than a thousand voluntary prayers every day.'",
          urduSnippet = "امام محمد باقر اور امام جعفر صادق علیہما السلام نے فرمایا: ہر فرض نماز کے بعد تسبیحِ فاطمہ زہراؑ (۳۴ اللّٰہ اکبر، ۳۳ الحمد للّٰہ، ۳۳ سبحان اللّٰہ) روزانہ ہزار رکعت نفل نماز سے افضل ہے۔",
          authenticReference = "Al-Kafi Vol. 3, Kitab al-Salah, Bab al-Ta'qib, p. 343, Hadith 13 (Sahih) • Tahdhib al-Ahkam Vol. 2, p. 105",
          actionLabel = "Open Shia Hadith & Duas",
          actionType = "HADITH"
        ),
        IntelligentRecommendationItem(
          id = "rec_shia_kafi_noble_character_and_kinship",
          pillarCategory = "SUNNAH_HADITH",
          reasonBadgeEnglish = "Ahl al-Bayt Ethics • Good Character & Silat al-Rahim",
          reasonBadgeUrdu = "اخلاقِ اہلِ بیتؑ • حسنِ خلق اور صلہ رحمی",
          titleEnglish = "Good Character Melts Sins as the Sun Melts Ice",
          titleUrdu = "حسنِ اخلاق گناہوں کو ایسے پگھلا دیتا ہے جیسے سورج برف کو",
          arabicSnippet = "إِنَّ حُسْنَ الْخُلُقِ يُذِيبُ الْخَطِيئَةَ كَمَا تُذِيبُ الشَّمْسُ الْجَلِيدَ",
          englishSnippet = "Imam Ja'far al-Sadiq (AS) taught in Al-Kafi that gentleness, truthfulness, fulfilling trusts (Amanah), and maintaining family ties (Silat al-Rahim) are the true hallmarks of a follower of Ahl al-Bayt.",
          urduSnippet = "الکافی جلد ۲ صفحہ ۱۰۰ کی صحیح حدیث میں امام جعفر صادق علیہ السلام نے فرمایا کہ خوش اخلاقی، امانت داری، صدقِ مقال اور صلہ رحمی شیعیانِ اہلِ بیت کی حقیقی پہچان ہے۔",
          authenticReference = "Al-Kafi Vol. 2, Bab Husn al-Khuluq, p. 100, Hadith 4 (Sahih) & Bab Silat al-Rahim, p. 150",
          actionLabel = "Read in Al-Kafi",
          actionType = "HADITH"
        ),
        IntelligentRecommendationItem(
          id = "rec_shia_salat_al_layl_honour",
          pillarCategory = "SUNNAH_HADITH",
          reasonBadgeEnglish = "Nightly Devotion • Salat al-Layl (Namaz-e-Shab)",
          reasonBadgeUrdu = "شرفِ مومن • نمازِ شب (تہجد) کی فضیلت",
          titleEnglish = "The Honour of a Believer Lies in Salat al-Layl (11 Rak'ats)",
          titleUrdu = "مومن کا شرف نمازِ شب کے قیام میں ہے",
          arabicSnippet = "شَرَفُ الْمُؤْمِنِ صَلَاتُهُ بِاللَّيْلِ، وَعِزُّهُ كَفُّ الأَذَى عَنِ النَّاسِ",
          englishSnippet = "Imam Ja'far al-Sadiq (AS) narrated in Al-Kafi that Salat al-Layl (8 Rak'ats Nawafil + 2 Shaf' + 1 Witr) illuminates the face, expands sustenance, and brings nearness to Allah.",
          urduSnippet = "الکافی جلد ۳ صفحہ ۴۴۴ اور من لا یحضرہ الفقیہ میں امام جعفر صادق علیہ السلام سے مروی ہے کہ نمازِ شب چہرے کو منور کرتی ہے، رزق میں برکت دیتی ہے اور مومن کا شرف ہے۔",
          authenticReference = "Al-Kafi Vol. 3, p. 444, Hadith 12 (Sahih) • Man La Yahduruhu al-Faqih Vol. 1, p. 471",
          actionLabel = "Explore Shia Hadith",
          actionType = "HADITH"
        )
      )

      val shiaQuranPool = buildList {
        if (isFridayOrThursdayEvening) {
          add(
            IntelligentRecommendationItem(
              id = "rec_shia_quran_kahf_and_jumuah_friday",
              pillarCategory = "QURAN_TAFSIR",
              reasonBadgeEnglish = "Laylat al-Jumu'ah & Friday Practice • Al-Kafi & Thawab al-A'mal",
              reasonBadgeUrdu = "شبِ جمعہ اور روزِ جمعہ کی مستند تلاوت",
              titleEnglish = "Recite Surah Al-Jumu'ah (62), Surah Al-Kahf (18) & Surah Al-Isra (17)",
              titleUrdu = "جمعہ کے دن سورۃ الجمعہ، سورۃ الکہف اور بنی اسرائیل کی تلاوت",
              arabicSnippet = "يُسَبِّحُ لِلَّهِ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ الْمَلِكِ الْقُدُّوسِ الْعَزِيزِ الْحَكِيمِ",
              englishSnippet = "In Al-Kafi and Man La Yahduruhu al-Faqih, Imam Ja'far al-Sadiq (AS) emphasized reciting Surah Al-Jumu'ah and Surah Al-A'la in Friday prayers and reciting Surah Al-Kahf and Surah Al-Isra on Thursday night / Friday.",
              urduSnippet = "الکافی اور من لا یحضرہ الفقیہ کی صحیح احادیث کے مطابق شبِ جمعہ اور روزِ جمعہ سورۃ الجمعہ، سورۃ الکہف اور سورۃ بنی اسرائیل کی تلاوت عظیم اجر و نور کا باعث ہے۔",
              authenticReference = "Al-Kafi Vol. 2, p. 616 & Vol. 3, p. 425 (Sahih) • Man La Yahduruhu al-Faqih Vol. 1, p. 426",
              actionLabel = "Open Surah Al-Kahf (18)",
              actionType = "QURAN_SURAH",
              targetSurahNumber = 18,
              targetVerseNumber = 1
            )
          )
        }
        if (lastReadSurahNumber in 1..114) {
          add(
            IntelligentRecommendationItem(
              id = "rec_shia_quran_continue_${lastReadSurahNumber}_${lastReadVerseNumber}",
              pillarCategory = "QURAN_TAFSIR",
              reasonBadgeEnglish = "Based on Your Reading • Surah $lastReadSurahNumber ($lastReadSurahNameEn)",
              reasonBadgeUrdu = "آپ کے مطالعۂ قرآن کے مطابق اگلا قدم",
              titleEnglish = "Continue Surah $lastReadSurahNameEn (Verse $lastReadVerseNumber) with Shia Tafsirs (Al-Mizan & Majma' al-Bayan)",
              titleUrdu = "سورۃ $lastReadSurahNameEn (آیت $lastReadVerseNumber) تفسیر المیزان اور مجمع البیان کے ساتھ جاری رکھیں",
              arabicSnippet = "كِتَٰبٌ أَنزَلْنَٰهُ إِلَيْكَ مُبَٰرَكٌ لِّيَدَّبَّرُوٓا۟ ءَايَٰتِهِۦ",
              englishSnippet = "Resume from Verse $lastReadVerseNumber with Shia translations (Ali Quli Qarai / Shakir / Syed Zeeshan Haider Jawadi) and classical Shia commentaries (Tafsir al-Mizan, Majma' al-Bayan, Al-Tibyān, Safi).",
              urduSnippet = "اپنی تلاوت وہیں سے جاری رکھیں اور علامہ طباطبائی کی تفسیر المیزان، شیخ طبرسی کی مجمع البیان اور شیخ طوسی کی التبیان کا مطالعہ کریں۔",
              authenticReference = "Qur'an $lastReadSurahNumber:$lastReadVerseNumber • Tafsir al-Mizan & Majma' al-Bayan",
              actionLabel = "Resume Surah $lastReadSurahNameEn",
              actionType = "QURAN_SURAH",
              targetSurahNumber = lastReadSurahNumber,
              targetVerseNumber = lastReadVerseNumber
            )
          )
        }
        add(
          IntelligentRecommendationItem(
            id = "rec_shia_quran_mulk_and_yasin",
            pillarCategory = "QURAN_TAFSIR",
            reasonBadgeEnglish = "Nightly & Morning Qur'anic Practice • Al-Kafi Fadl al-Qur'an",
            reasonBadgeUrdu = "سورۃ یٰس اور سورۃ الملک کی تلاوت • کتاب فضل القرآن الکافی",
            titleEnglish = "Surah Yasin (Heart of the Qur'an) & Surah Al-Mulk (Al-Manjiyah)",
            titleUrdu = "سورۃ یٰس (قلبِ قرآن) اور سورۃ الملک (عذابِ قبر سے محافظ)",
            arabicSnippet = "تَبَٰرَكَ ٱلَّذِى بِيَدِهِ ٱلْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَىْءٍ قَدِيرٌ",
            englishSnippet = "In Al-Kafi Vol. 2, p. 633, Imam Muhammad al-Baqir (AS) states: 'Surah Al-Mulk is the protector (Al-Mani'ah); it protects its reciter from the chastisement of the grave.'",
            urduSnippet = "الکافی جلد ۲ صفحہ ۶۳۳ میں امام محمد باقر علیہ السلام سے مروی ہے کہ سورۃ الملک مانعہ ہے جو اپنے پڑھنے والے کو قبر کی وحشت سے محفوظ رکھتی ہے۔",
            authenticReference = "Qur'an 67:1–30 • Al-Kafi Vol. 2, Kitab Fadl al-Qur'an, p. 633, Hadith 26",
            actionLabel = "Read Surah Al-Mulk (67)",
            actionType = "QURAN_SURAH",
            targetSurahNumber = 67,
            targetVerseNumber = 1
          )
        )
      }

      val shiaDuaAndLearningPool = listOf(
        if (hourOfDay in 4..11) {
          IntelligentRecommendationItem(
            id = "rec_shia_dua_sabah_and_ahd",
            pillarCategory = "DUA_CHARACTER",
            reasonBadgeEnglish = "Morning Shia Devotion • Dua al-Sabah & Sahifa Sajjadiyya",
            reasonBadgeUrdu = "صبح کی مستند دعائیں • دعائے صباح اور صحیفہ سجادیہ",
            titleEnglish = "Morning Supplications: Dua al-Sabah (Imam Ali AS) & Sahifa Sajjadiyya Dua 6",
            titleUrdu = "دعائے صباح (امیر المؤمنین علیؑ) اور صحیفہ سجادیہ کی صبح و شام کی دعا",
            arabicSnippet = "اللَّهُمَّ يَا مَنْ دَلَعَ لِسَانَ الصَّبَاحِ بِنُطْقِ تَبَلُّجِهِ",
            englishSnippet = "Begin your morning with Imam Ali Zayn al-'Abidin's (AS) Morning & Evening Supplication (Al-Sahifa al-Sajjadiyya, Dua 6) and Dua al-Sabah from Bihar al-Anwar & Mafatih al-Jinan.",
            urduSnippet = "امام زین العابدین علیہ السلام کی صحیفہ سجادیہ (دعا نمبر ۶) اور امیر المؤمنین علی علیہ السلام کی دعائے صباح سے اپنے دن کا آغاز کریں۔",
            authenticReference = "Al-Sahifa al-Sajjadiyya, Dua 6 • Bihar al-Anwar Vol. 84 / Mafatih al-Jinan",
            actionLabel = "Open Shia Duas",
            actionType = "DUAS"
          )
        } else {
          IntelligentRecommendationItem(
            id = "rec_shia_dua_kumayl_and_makarim",
            pillarCategory = "DUA_CHARACTER",
            reasonBadgeEnglish = "Supplications of Ahl al-Bayt • Dua Kumayl & Makarim al-Akhlaq",
            reasonBadgeUrdu = "ادعیۂ اہلِ بیتؑ • دعائے کمیل اور دعائے مکارم الاخلاق",
            titleEnglish = "Dua Kumayl (Misbah al-Mutahajjid) & Dua Makarim al-Akhlaq (Sahifa Sajjadiyya 20)",
            titleUrdu = "دعائے کمیل (مصباح المتہجد) اور دعائے مکارم الاخلاق (صحیفہ سجادیہ دعا ۲۰)",
            arabicSnippet = "اللَّهُمَّ بَلِّغْ بِإِيمَانِي أَكْمَلَ الْإِيمَانِ، وَاجْعَلْ يَقِينِي أَفْضَلَ الْيَقِينِ",
            englishSnippet = "Reflect on Imam Ali's (AS) Dua Kumayl recorded by Shaykh al-Tusi in Misbah al-Mutahajjid and Imam Sajjad's (AS) masterpiece on noble character (Dua 20 of Al-Sahifa al-Sajjadiyya).",
            urduSnippet = "شیخ طوسی کی مصباح المتہجد سے دعائے کمیل اور صحیفہ سجادیہ کی بیسویں دعا (مکارم الاخلاق) کا ترجمہ و حوالہ جات کے ساتھ مطالعہ کریں۔",
            authenticReference = "Al-Sahifa al-Sajjadiyya, Dua 20 • Misbah al-Mutahajjid of Shaykh al-Tusi, p. 844",
            actionLabel = "Open Shia Dua Library",
            actionType = "DUAS"
          )
        },
        IntelligentRecommendationItem(
          id = "rec_shia_qaida_hd_page_$qaidaPage",
          pillarCategory = "DUA_CHARACTER",
          reasonBadgeEnglish = "Shared Qur'anic Foundation • HD Noorani Qaida Page $qaidaPage/53",
          reasonBadgeUrdu = "مشترکہ قرآنی بنیاد • نورانی قاعدہ اور تجوید صفحہ $qaidaPage از ۵۳",
          titleEnglish = "Continue Noorani Qaida HD (Page $qaidaPage) + Interactive Tajweed Quiz",
          titleUrdu = "ایچ ڈی نورانی قاعدہ (صفحہ $qaidaPage) اور تجوید ایم سی کیو ٹیسٹ",
          arabicSnippet = "وَرَتِّلِ ٱلْقُرْءَانَ تَرْتِيلًا",
          englishSnippet = "Qur'anic Arabic articulation (Makharij) and Tajweed are shared by all Muslims. Practice Page $qaidaPage and test your mastery.",
          urduSnippet = "قرآنی عربی، مخارج اور تجوید تمام مسلمانوں کا متفقہ ورثہ ہے؛ صفحہ $qaidaPage کی مشق اور سوالات حل کریں۔",
          authenticReference = "Qur'an 73:4 • Al-Kafi Vol. 2, p. 614 (Tartil al-Qur'an)",
          actionLabel = "Open Qaida Page $qaidaPage",
          actionType = "QAIDA_PDF"
        )
      )

      val shiaHistoryAndLibraryPool = listOf(
        IntelligentRecommendationItem(
          id = "rec_shia_history_nahj_al_balagha_and_irshad",
          pillarCategory = "HISTORY_SEERAH",
          reasonBadgeEnglish = "Ahl al-Bayt Heritage • Nahj al-Balagha & Kitab al-Irshad",
          reasonBadgeUrdu = "تاریخی و علمی ورثہ • نہج البلاغہ اور کتاب الارشاد (شیخ مفید)",
          titleEnglish = "Nahj al-Balagha (Sermons & Letters of Imam Ali AS) & Lives of the Twelve Imams",
          titleUrdu = "نہج البلاغہ (خطبات و مکتوباتِ امیر المؤمنینؑ) اور سوانحِ ائمہ اہلِ بیتؑ",
          arabicSnippet = "وَٱلسَّٰبِقُونَ ٱلْأَوَّلُونَ مِنَ ٱلْمُهَٰجِرِينَ وَٱلْأَنصَارِ",
          englishSnippet = "Study the sermons, letters (including the famous Covenant to Malik al-Ashtar, Letter 53), and wisdom of Imam Ali ibn Abi Talib (AS) alongside biographies of Ahl al-Bayt.",
          urduSnippet = "امیر المؤمنین حضرت علی بن ابی طالب علیہ السلام کے خطبات، عہد نامۂ مالک اشتر (مکتوب ۵۳) اور ائمہ اہلِ بیتؑ کے مستند سوانحی ابواب کا مطالعہ کریں۔",
          authenticReference = "Nahj al-Balagha (compiled by Sharif al-Radi) • Kitab al-Irshad by Shaykh al-Mufid",
          actionLabel = "Explore Biographies & Books",
          actionType = "LIBRARY_TAB",
          targetLibraryTab = 10
        ),
        IntelligentRecommendationItem(
          id = "rec_shia_history_prophet_stories_25",
          pillarCategory = "HISTORY_SEERAH",
          reasonBadgeEnglish = "Qur'anic History • 25 Prophets of Allah ('Alayhimus-Salam)",
          reasonBadgeUrdu = "قصص الانبیاء • قرآن اور احادیثِ اہلِ بیتؑ کی روشنی میں",
          titleEnglish = "Explore the Lives & Lessons of 25 Prophets in the Qur'an",
          titleUrdu = "قرآنِ حکیم کی روشنی میں ۲۵ انبیاء کرام علیہم السلام کے واقعات",
          arabicSnippet = "لَقَدْ كَانَ فِى قَصَصِهِمْ عِبْرَةٌ لِّأُو۟لِى ٱلْأَلْبَٰبِ",
          englishSnippet = "Read verified accounts of Adam, Nuh, Ibrahim, Yusuf, Musa, 'Isa, and Prophet Muhammad ﷺ with exact Surah citations.",
          urduSnippet = "حضرت آدمؑ، نوحؑ، ابراہیمؑ، یوسفؑ، موسیٰؑ، عیسیٰؑ اور خاتم النبیین حضرت محمد مصطفیٰ ﷺ کی سیرت کے مستند ابواب۔",
          authenticReference = "Qur'an 12:111 • Al-Kafi Vol. 1 & Vol. 8 • Hayat al-Qulub",
          actionLabel = "Open 25 Prophet Stories",
          actionType = "LIBRARY_TAB",
          targetLibraryTab = 8
        )
      )

      val safeRot = rotationOffset.coerceAtLeast(0)
      val chosenShiaHadith = shiaHadithPool[safeRot % shiaHadithPool.size]
      val chosenShiaHadithSecond = shiaHadithPool[(safeRot + 2) % shiaHadithPool.size]
      val chosenShiaQuran = shiaQuranPool[safeRot % shiaQuranPool.size]
      val chosenShiaDua = shiaDuaAndLearningPool[safeRot % shiaDuaAndLearningPool.size]
      val chosenShiaHistory = shiaHistoryAndLibraryPool[safeRot % shiaHistoryAndLibraryPool.size]

      return listOf(chosenShiaHadith, chosenShiaQuran, chosenShiaDua, chosenShiaHistory, chosenShiaHadithSecond)
        .distinctBy { it.id }
    }

    // Pool of verified Prophetic Sunnah & Hadith recommendations (non-repetitive rotation)
    val sunnahPool = listOf(
      IntelligentRecommendationItem(
        id = "rec_sunnah_intentions_bukhari_1",
        pillarCategory = "SUNNAH_HADITH",
        reasonBadgeEnglish = "Foundational Prophetic Tradition • Sincerity (Ikhlas)",
        reasonBadgeUrdu = "بنیادی نبوی اصول • اخلاصِ نیت",
        titleEnglish = "Actions Are Judged by Intentions (Innamal-A'malu bin-Niyyat)",
        titleUrdu = "اعمال کا دارومدار نیتوں پر ہے",
        arabicSnippet = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
        englishSnippet = "Renewing a sincere intention turns even daily work, study, and kindness to family into rewarded worship.",
        urduSnippet = "سچی نیت کے ساتھ کیا گیا ہر جائز کام، تعلیم اور اہلِ خانہ کی خدمت بھی عبادت بن جاتی ہے۔",
        authenticReference = "Sahih al-Bukhari 1 • Sahih Muslim 1907",
        actionLabel = "Read in Sahih al-Bukhari",
        actionType = "HADITH"
      ),
      IntelligentRecommendationItem(
        id = "rec_sunnah_mercy_tirmidhi_1924",
        pillarCategory = "SUNNAH_HADITH",
        reasonBadgeEnglish = "Prophetic Character • Mercy to All Creation",
        reasonBadgeUrdu = "سنتِ نبوی • مخلوق پر رحم و شفقت",
        titleEnglish = "The Hadith of Mercy (Hadith al-Musalsal bil-Awwaliyyah)",
        titleUrdu = "رحم کرنے والوں پر رحمٰن رحم فرماتا ہے",
        arabicSnippet = "الرَّاحِمُونَ يَرْحَمُهُمُ الرَّحْمَنُ، ارْحَمُوا مَنْ فِي الأَرْضِ يَرْحَمْكُمْ مَنْ فِي السَّمَاءِ",
        englishSnippet = "The Merciful (Allah) shows mercy to those who are merciful. Show mercy to those on earth, and the One in heaven will show mercy to you.",
        urduSnippet = "رحم کرنے والوں پر رحمٰن رحم فرماتا ہے؛ تم زمین والوں پر رحم کرو، آسمان والا تم پر رحم فرمائے گا۔",
        authenticReference = "Jami` at-Tirmidhi 1924 • Sunan Abi Dawud 4941 (Sahih)",
        actionLabel = "Explore Prophetic Hadiths",
        actionType = "HADITH"
      ),
      IntelligentRecommendationItem(
        id = "rec_sunnah_beloved_words_muslim_2695",
        pillarCategory = "SUNNAH_HADITH",
        reasonBadgeEnglish = "Light on the Tongue, Heavy on the Scale",
        reasonBadgeUrdu = "زبان پر ہلکے، میزانِ عمل میں بھاری کلمات",
        titleEnglish = "Two Phrases Beloved to the Most Merciful",
        titleUrdu = "رحمٰن کے محبوب دو کلمات",
        arabicSnippet = "كَلِمَتَانِ خَفِيفَتَانِ عَلَى اللِّسَانِ، ثَقِيلَتَانِ فِي الْمِيزَانِ، حَبِيبَتَانِ إِلَى الرَّحْمَنِ: سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
        englishSnippet = "SubhanAllahi wa bihamdihi, SubhanAllahil-'Azim — effortless to recite throughout your day yet heavy on the Scale of Deeds.",
        urduSnippet = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ — زبان پر انتہائی ہلکے مگر قیامت کے دن ترازو میں بہت بھاری۔",
        authenticReference = "Sahih al-Bukhari 6406 • Sahih Muslim 2694",
        actionLabel = "Open Hadith & Sunnah",
        actionType = "HADITH"
      ),
      IntelligentRecommendationItem(
        id = "rec_sunnah_smiling_charity_tirmidhi_1956",
        pillarCategory = "SUNNAH_HADITH",
        reasonBadgeEnglish = "Daily Sunnah Ethics • Kindness & Brotherhood",
        reasonBadgeUrdu = "اخلاقِ نبوی • مسکراہٹ اور خیر خواہی صدقہ ہے",
        titleEnglish = "Your Smile to Your Brother Is Charity (Sadaqah)",
        titleUrdu = "اپنے بھائی سے مسکرا کر ملنا بھی صدقہ ہے",
        arabicSnippet = "تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ",
        englishSnippet = "Prophetic guidance teaches that every kind word, removing harm from the road, and guiding someone who is lost is recorded as charity.",
        urduSnippet = "کسی سے خندہ پیشانی سے پیش آنا، اچھی بات سکھانا اور راستے سے تکلیف دہ چیز ہٹانا بھی صدقہ ہے۔",
        authenticReference = "Jami` at-Tirmidhi 1956 (Sahih)",
        actionLabel = "Read in Hadith Library",
        actionType = "HADITH"
      ),
      IntelligentRecommendationItem(
        id = "rec_sunnah_parents_bukhari_5971",
        pillarCategory = "SUNNAH_HADITH",
        reasonBadgeEnglish = "Beloved Deeds to Allah •Salah & Honoring Parents",
        reasonBadgeUrdu = "اللّٰہ کے نزدیک محبوب ترین اعمال",
        titleEnglish = "Prayer on Time & Dutiful Kindness to Parents",
        titleUrdu = "وقت پر نماز اور والدین کے ساتھ حسنِ سلوک",
        arabicSnippet = "سَأَلْتُ النَّبِيَّ ﷺ أَيُّ الْعَمَلِ أَحَبُّ إِلَى اللَّهِ؟ قَالَ: الصَّلَاةُ عَلَى وَقْتِهَا، قُلْتُ ثُمَّ أَيٌّ؟ قَالَ: بِرُّ الْوَالِدَيْنِ",
        englishSnippet = "Abdullah ibn Mas'ud (RA) asked the Prophet ﷺ which deed is most beloved to Allah: First is offering Salah on its proper time, then kindness to parents.",
        urduSnippet = "حضرت عبداللّٰہ بن مسعودؓ نے پوچھا کہ اللّٰہ کو کون سا عمل سب سے زیادہ محبوب ہے؟ آپ ﷺ نے فرمایا: وقت پر نماز پڑھنا اور والدین سے نیک سلوک کرنا۔",
        authenticReference = "Sahih al-Bukhari 527, 5971 • Sahih Muslim 85",
        actionLabel = "Explore Sahih Hadith",
        actionType = "HADITH"
      )
    )

    // Pool of Qur'an & Tafsir recommendations (context-aware of user's last read Surah & Friday status)
    val quranPool = buildList {
      if (isFridayOrThursdayEvening) {
        add(
          IntelligentRecommendationItem(
            id = "rec_quran_kahf_friday",
            pillarCategory = "QURAN_TAFSIR",
            reasonBadgeEnglish = "Blessed Jumu'ah Sunnah • Light Between Two Fridays",
            reasonBadgeUrdu = "جمعۃ المبارک کی سنت • دو جمعوں کے درمیان نور",
            titleEnglish = "Recite & Reflect on Surah Al-Kahf (Surah 18)",
            titleUrdu = "سورۃ الکہف کی تلاوت اور ۸ مستند تفاسیر",
            arabicSnippet = "ٱلْحَمْدُ لِلَّهِ ٱلَّذِىٓ أَنزَلَ عَلَىٰ عَبْدِهِ ٱلْكِتَٰبَ وَلَمْ يَجْعَل لَّهُۥ عِوَجَا ۜ",
            englishSnippet = "Whoever recites Surah Al-Kahf on Friday will have a light shining for him between this Friday and the next.",
            urduSnippet = "جو شخص جمعہ کے دن سورۃ الکہف کی تلاوت کرے اس کے لیے دو جمعوں کے درمیان نور روشن کر دیا جاتا ہے۔",
            authenticReference = "Qur'an 18:1–110 • Al-Mustadrak 3392 / Sahih al-Jami' 6470",
            actionLabel = "Open Surah Al-Kahf (18)",
            actionType = "QURAN_SURAH",
            targetSurahNumber = 18,
            targetVerseNumber = 1
          )
        )
      }
      if (lastReadSurahNumber in 1..114) {
        val nextSurahNum = if (lastReadSurahNumber < 114) lastReadSurahNumber + 1 else 1
        add(
          IntelligentRecommendationItem(
            id = "rec_quran_continue_${lastReadSurahNumber}_${lastReadVerseNumber}",
            pillarCategory = "QURAN_TAFSIR",
            reasonBadgeEnglish = "Based on Your Reading • Surah $lastReadSurahNumber ($lastReadSurahNameEn)",
            reasonBadgeUrdu = "آپ کے مطالعۂ قرآن کے مطابق اگلا قدم",
            titleEnglish = "Continue Surah $lastReadSurahNameEn (Verse $lastReadVerseNumber) with 8 Classical Tafsirs",
            titleUrdu = "سورۃ $lastReadSurahNameEn (آیت $lastReadVerseNumber) ترجمہ اور تفسیر کے ساتھ جاری رکھیں",
            arabicSnippet = "كِتَٰبٌ أَنزَلْنَٰهُ إِلَيْكَ مُبَٰرَكٌ لِّيَدَّبَّرُوٓا۟ ءَايَٰتِهِۦ",
            englishSnippet = "Resume from Verse $lastReadVerseNumber or explore Surah $nextSurahNum with word-by-word Tajweed highlighting and Ibn Kathir / Maariful Quran commentary.",
            urduSnippet = "اپنی تلاوت وہیں سے جاری رکھیں اور ابنِ کثیر، قرطبی، معارف القرآن اور تفہیم القرآن کا تقابلی مطالعہ کریں۔",
            authenticReference = "Qur'an $lastReadSurahNumber:$lastReadVerseNumber • 8 Offline Tafsirs",
            actionLabel = "Resume Surah $lastReadSurahNameEn",
            actionType = "QURAN_SURAH",
            targetSurahNumber = lastReadSurahNumber,
            targetVerseNumber = lastReadVerseNumber
          )
        )
      }
      add(
        IntelligentRecommendationItem(
          id = "rec_quran_mulk_night_protection",
          pillarCategory = "QURAN_TAFSIR",
          reasonBadgeEnglish = "Prophetic Nightly Practice • 30 Verses of Intercession",
          reasonBadgeUrdu = "رات کی مسنون تلاوت • تیس آیات والی شفاعت کرنے والی سورت",
          titleEnglish = "Surah Al-Mulk (Surah 67) — The Protector from the Grave",
          titleUrdu = "سورۃ الملک (۶۷) — عذابِ قبر سے نجات دلانے والی سورت",
          arabicSnippet = "تَبَٰرَكَ ٱلَّذِى بِيَدِهِ ٱلْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَىْءٍ قَدِيرٌ",
          englishSnippet = "There is a Surah in the Qur'an of thirty verses which intercedes for a man until he is forgiven: Tabarakalladhi biyadihil-Mulk.",
          urduSnippet = "قرآن مجید میں تیس آیات والی ایک سورت ہے جو اپنے پڑھنے والے کی شفاعت کرتی ہے یہاں تک کہ اسے بخش دیا جاتا ہے: تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ۔",
          authenticReference = "Qur'an 67:1–30 • Sunan Abi Dawud 1400 / Jami` at-Tirmidhi 2891",
          actionLabel = "Read Surah Al-Mulk (67)",
          actionType = "QURAN_SURAH",
          targetSurahNumber = 67,
          targetVerseNumber = 1
        )
      )
      add(
        IntelligentRecommendationItem(
          id = "rec_quran_luqman_wisdom",
          pillarCategory = "QURAN_TAFSIR",
          reasonBadgeEnglish = "Character & Family Wisdom • Surah Luqman (31:12–19)",
          reasonBadgeUrdu = "حکمتِ لقمان • اولاد کی تربیت اور اخلاقِ حسنہ",
          titleEnglish = "Luqman's Golden Advice: Tawheed, Gratitude, Salah & Humility",
          titleUrdu = "حضرت لقمان کی نصیحتیں: توحید، شکر، نماز، صبر اور عاجزی",
          arabicSnippet = "يَٰبُنَىَّ أَقِمِ ٱلصَّلَوٰةَ وَأْمُرْ بِٱلْمَعْرُوفِ وَٱنْهَ عَنِ ٱلْمُنكَرِ وَٱصْبِرْ عَلَىٰ مَآ أَصَابَكَ",
          englishSnippet = "Explore Surah Luqman's timeless curriculum for personal character: avoiding shirk, honoring parents, establishing prayer, and walking humbly.",
          urduSnippet = "سورۃ لقمان میں عقیدہ، والدین کے حقوق، اقامتِ صلوٰۃ، امر بالمعروف اور گفتگو و چال ڈھال میں تواضع کی جامع تعلیم ہے۔",
          authenticReference = "Qur'an 31:12–19 • Tafsir Ibn Kathir & Maariful Quran",
          actionLabel = "Read Surah Luqman (31)",
          actionType = "QURAN_SURAH",
          targetSurahNumber = 31,
          targetVerseNumber = 12
        )
      )
    }

    // Pool of Time-Aware Duas, Noorani Qaida & Character Practice
    val duaAndLearningPool = listOf(
      if (hourOfDay in 4..11) {
        IntelligentRecommendationItem(
          id = "rec_dua_morning_adhkar",
          pillarCategory = "DUA_CHARACTER",
          reasonBadgeEnglish = "Morning Sunnah Adhkar • Protection & Barakah",
          reasonBadgeUrdu = "صبح کے مسنون اذکار • حفاظت اور برکت",
          titleEnglish = "Morning Remembrance & Sayyidul Istighfar",
          titleUrdu = "صبح کے اذکار اور سید الاستغفار",
          arabicSnippet = "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُورُ",
          englishSnippet = "Begin your morning with authentic Prophetic supplications from Hisnul Muslim & Sunan Abi Dawud with human audio and translation.",
          urduSnippet = "اپنے دن کا آغاز حصن المسلم اور سنن ابی داؤد سے ثابت شدہ صبح کے مسنون اذکار اور سید الاستغفار سے کریں۔",
          authenticReference = "Jami` at-Tirmidhi 3391 • Sahih al-Bukhari 6306",
          actionLabel = "Open Morning Duas",
          actionType = "DUAS"
        )
      } else if (hourOfDay in 15..20) {
        IntelligentRecommendationItem(
          id = "rec_dua_evening_adhkar",
          pillarCategory = "DUA_CHARACTER",
          reasonBadgeEnglish = "Evening Sunnah Adhkar • Peace & Protection",
          reasonBadgeUrdu = "شام کے مسنون اذکار • سکون اور حفاظت",
          titleEnglish = "Evening Supplications & Ayat al-Kursi Protection",
          titleUrdu = "شام کے مسنون اذکار اور آیت الکرسی کی فضیلت",
          arabicSnippet = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
          englishSnippet = "Recite the Prophetic evening Adhkar for serenity, gratitude, and protection through the night.",
          urduSnippet = "شام کے وقت مسنون اذکار اور معوذات کی تلاوت سے اللّٰہ کی امان اور قلبی سکون حاصل کریں۔",
          authenticReference = "Sahih Muslim 2723 • Hisnul Muslim",
          actionLabel = "Open Evening Duas",
          actionType = "DUAS"
        )
      } else {
        IntelligentRecommendationItem(
          id = "rec_dua_comprehensive_good_2_201",
          pillarCategory = "DUA_CHARACTER",
          reasonBadgeEnglish = "The Prophet's ﷺ Most Frequent Supplication",
          reasonBadgeUrdu = "نبی کریم ﷺ کی سب سے زیادہ پڑھی جانے والی جامع دعا",
          titleEnglish = "Rabbana Atina fid-Dunya Hasanah (Good in This World & Hereafter)",
          titleUrdu = "ربنا آتنا فی الدنیا حسنۃ — دنیا و آخرت کی ہر بھلائی کی دعا",
          arabicSnippet = "رَبَّنَآ ءَاتِنَا فِى ٱلدُّنْيَا حَسَنَةً وَفِى ٱلْءَاخِرَةِ حَسَنَةً وَقِنَا عَذَابَ ٱلنَّارِ",
          englishSnippet = "Anas ibn Malik (RA) reported that the most frequent invocation of the Prophet ﷺ was this comprehensive Qur'anic dua.",
          urduSnippet = "حضرت انس بن مالکؓ فرماتے ہیں کہ رسول اللّٰہ ﷺ سب سے کثرت کے ساتھ یہی جامع قرآنی دعا مانگا کرتے تھے۔",
          authenticReference = "Qur'an 2:201 • Sahih al-Bukhari 6389 • Sahih Muslim 2690",
          actionLabel = "Open Daily Duas & Azkar",
          actionType = "DUAS"
        )
      },
      IntelligentRecommendationItem(
        id = "rec_qaida_hd_page_$qaidaPage",
        pillarCategory = "DUA_CHARACTER",
        reasonBadgeEnglish = "Step-by-Step Tajweed • HD Noorani Qaida Page $qaidaPage/53",
        reasonBadgeUrdu = "نورانی قاعدہ اور تجوید • صفحہ $qaidaPage از ۵۳",
        titleEnglish = "Continue Noorani Qaida HD (Page $qaidaPage) + Interactive MCQ Quiz",
        titleUrdu = "ایچ ڈی نورانی قاعدہ (صفحہ $qaidaPage) اور ایم سی کیو ٹیسٹ حل کریں",
        arabicSnippet = "وَرَتِّلِ ٱلْقُرْءَانَ تَرْتِيلًا",
        englishSnippet = "Practice Arabic pronunciation rules with RTL teacher notes and test your mastery with Basic, Intermediate & Hard MCQs.",
        urduSnippet = "نورانی قاعدہ کے قوانین، اردو ہدایات اور ہر صفحے کے تحقیقی سوالات (MCQs) سے اپنی تجوید مضبوط بنائیں۔",
        authenticReference = "Qur'an 73:4 • 53-Page HD Qaida & 17 Audio Lessons",
        actionLabel = "Open Qaida Page $qaidaPage",
        actionType = "QAIDA_PDF"
      )
    )

    // Pool of Islamic History, Seerah & Classical Books Discovery
    val historyAndLibraryPool = listOf(
      IntelligentRecommendationItem(
        id = "rec_history_prophet_stories_25",
        pillarCategory = "HISTORY_SEERAH",
        reasonBadgeEnglish = "Qur'anic History • 25 Prophets of Allah ('Alayhimus-Salam)",
        reasonBadgeUrdu = "قصص الانبیاء • ۲۵ انبیاء کرام کی مستند تاریخ",
        titleEnglish = "Explore the Lives & Lessons of 25 Prophets from Qur'an & Sahih Hadith",
        titleUrdu = "قرآن و صحیح احادیث کی روشنی میں ۲۵ انبیاء کرام کے واقعات",
        arabicSnippet = "لَقَدْ كَانَ فِى قَصَصِهِمْ عِبْرَةٌ لِّأُو۟لِى ٱلْأَلْبَٰبِ",
        englishSnippet = "Read verified accounts of Adam, Nuh, Ibrahim, Yusuf, Musa, 'Isa, and Prophet Muhammad ﷺ with exact Surah and Hadith citations.",
        urduSnippet = "حضرت آدمؑ، نوحؑ، ابراہیمؑ، یوسفؑ، موسیٰؑ، عیسیٰؑ اور سیدنا محمد رسول اللّٰہ ﷺ کی سیرت کے مستند ابواب۔",
        authenticReference = "Qur'an 12:111 • Ibn Kathir's Al-Bidayah wa'l-Nihayah",
        actionLabel = "Open 25 Prophet Stories",
        actionType = "LIBRARY_TAB",
        targetLibraryTab = 8
      ),
      IntelligentRecommendationItem(
        id = "rec_history_sahaba_ahlulbayt",
        pillarCategory = "HISTORY_SEERAH",
        reasonBadgeEnglish = "Biographical Heritage • Sahaba & Ahl al-Bayt",
        reasonBadgeUrdu = "تراجم و سیرت • صحابہ کرامؓ اور اہلِ بیت اطہارؑ",
        titleEnglish = "Lives of the Noble Companions & Ahl al-Bayt from Primary Sources",
        titleUrdu = "صحابہ کرام اور اہلِ بیت اطہار کے مستند سوانحی ابواب",
        arabicSnippet = "وَٱلسَّٰبِقُونَ ٱلْأَوَّلُونَ مِنَ ٱلْمُهَٰجِرِينَ وَٱلْأَنصَارِ وَٱلَّذِينَ ٱتَّبَعُوهُم بِإِحْسَٰنٍ",
        englishSnippet = "Study documented biographies from Al-Isabah, Siyar A'lam al-Nubala, Tabaqat Ibn Sa'd, and Nahj al-Balagha with full Jild & Page navigation.",
        urduSnippet = "الاصابہ، سیر اعلام النبلاء، طبقات ابن سعد اور نہج البلاغہ کے مستند حوالہ جات کے ساتھ۔",
        authenticReference = "Qur'an 9:100 & 33:33 • Classical Rijal & Tabaqat",
        actionLabel = "Explore Biographies",
        actionType = "LIBRARY_TAB",
        targetLibraryTab = 10
      ),
      IntelligentRecommendationItem(
        id = "rec_history_26_chronicles",
        pillarCategory = "HISTORY_SEERAH",
        reasonBadgeEnglish = "Islamic History • 26 Multi-Volume Classical Chronicles",
        reasonBadgeUrdu = "تاریخِ اسلام • ۲۶ مستند کلاسیکی کتبِ تاریخ (جلد و صفحہ وار)",
        titleEnglish = "Tarikh al-Tabari, Al-Kamil (Ibn al-Athir) & Al-Bidayah wa'l-Nihayah",
        titleUrdu = "تاریخ طبری، الکامل فی التاریخ اور البدایہ والنہایہ کا مطالعہ",
        arabicSnippet = "قَدْ خَلَتْ مِن قَبْلِكُمْ سُنَنٌ فَسِيرُوا۟ فِى ٱلْأَرْضِ",
        englishSnippet = "Navigate Jild by Jild and Page by Page through Prophetic Seerah, the Rightly Guided Caliphate, and Islamic civilization.",
        urduSnippet = "عہدِ نبوی ﷺ، خلافتِ راشدہ اور اسلامی تاریخ کی ۲۶ امہات الکتب کا جلد اور صفحہ وار مطالعہ کریں۔",
        authenticReference = "26 Historical Chronicles • Multi-Jild Reader",
        actionLabel = "Open History Library",
        actionType = "LIBRARY_TAB",
        targetLibraryTab = 0
      )
    )

    val safeRot = rotationOffset.coerceAtLeast(0)
    val chosenSunnah = sunnahPool[safeRot % sunnahPool.size]
    val chosenSunnahSecond = sunnahPool[(safeRot + 2) % sunnahPool.size]
    val chosenQuran = quranPool[safeRot % quranPool.size]
    val chosenDua = duaAndLearningPool[safeRot % duaAndLearningPool.size]
    val chosenHistory = historyAndLibraryPool[safeRot % historyAndLibraryPool.size]

    return listOf(chosenSunnah, chosenQuran, chosenDua, chosenHistory, chosenSunnahSecond)
      .distinctBy { it.id }
  }

  fun getSavedRecommendationRotation(context: Context): Int {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getInt(KEY_REC_ROTATION_INDEX, 0)
  }

  fun advanceRecommendationRotation(context: Context): Int {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val next = (prefs.getInt(KEY_REC_ROTATION_INDEX, 0) + 1) % 1000
    prefs.edit().putInt(KEY_REC_ROTATION_INDEX, next).apply()
    return next
  }
}
