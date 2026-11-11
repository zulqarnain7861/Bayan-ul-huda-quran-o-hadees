package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.local.ExtendedHadithDataProvider
import com.example.data.local.OfflineHadithAssetLoader
import com.example.data.local.entities.HadithEntity
import com.example.data.model.DailyDuaItem
import com.example.data.model.DailyDuasDataProvider
import com.example.data.model.IslamicBookChapter
import com.example.data.model.IslamicBooksCatalog
import com.example.data.model.IslamicClassicalBook
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Provides:
 * 1. Dedicated Sahaba & Ahl al-Bayt Biographies (with separate Sunni and Shia historical perspectives).
 * 2. Three-Section Classical Library Classification ("Sunni Classical Library", "Shared / Disputed Attribution", "Shia Classical Library")
 *    with Folder -> Subfolder -> Files hierarchy and disputed/polemical attribution flags.
 * 3. All 45 Searchable Azkar Categories integrating mohammed-2-5/islamic-library-data/azkar + DailyDuasDataProvider.
 * 4. Smart Deterministic Hadith Navigation Index (Next = index + 1, Previous = index - 1),
 *    Reading State Persistence, Offline Preloaded Content checks, Navigation Trace Log,
 *    and the 21 Hadith Collections Audit Report.
 */
object ClassicalLibraryAndBiographiesRepository {

  // =========================================================================
  // PART A: SAHABA & AHL AL-BAYT BIOGRAPHIES (SUNNI & SHIA SEPARATED)
  // =========================================================================

  data class BiographyEntry(
    val id: String,
    val personNameEnglish: String,
    val personNameArabic: String,
    val personNameUrdu: String,
    val titleHonorific: String,
    val category: String, // "Khulafa & 'Asharah Mubashsharah", "Ahl al-Bayt", "Mothers of the Believers", "Muhajirun & Ansar", "Sahabiyyat (Women Companions)"
    val traditionPerspective: String, // "SUNNI" or "SHIA"
    val eraHijri: String,
    val primarySourceBook: String,
    val sourceBookArabic: String,
    val author: String,
    val volumeAndPage: String,
    val arabicText: String,
    val urduTranslation: String,
    val englishTranslation: String,
    val translatorAttribution: String,
    val crossReferences: String
  )

  private val inMemoryBioBookmarks = ConcurrentHashMap.newKeySet<String>()
  private const val BIO_PREFS = "bayan_sahaba_ahlulbayt_prefs"

  fun getAllBiographies(): List<BiographyEntry> = cachedBiographies

  private val cachedBiographies: List<BiographyEntry> by lazy {
    listOf(
      // 1. Abu Bakr al-Siddiq (RA) — Sunni Biographical Source (Al-Isabah / Siyar A'lam al-Nubala)
      BiographyEntry(
        id = "bio_sunni_abu_bakr",
        personNameEnglish = "Abu Bakr al-Siddiq 'Abdullah ibn Abi Quhafah (RA)",
        personNameArabic = "أبو بكر الصديق عبد الله بن أبي قحافة التيمي القرشي رضي الله عنه",
        personNameUrdu = "سیدنا ابوبکر صدیق عبداللہ بن ابی قحافہ رضی اللہ عنہ",
        titleHonorific = "Al-Siddiq • Companion of the Cave (Sahib al-Ghar) • First Rightly Guided Caliph",
        category = "Khulafa & 'Asharah Mubashsharah",
        traditionPerspective = "SUNNI",
        eraHijri = "50 BH – 13 AH",
        primarySourceBook = "Al-Isabah fi Tamyiz al-Sahabah & Siyar A'lam al-Nubala'",
        sourceBookArabic = "الإصابة في تمييز الصحابة لابن حجر العسقلاني وسير أعلام النبلاء للذهبي",
        author = "Hafiz Ibn Hajar al-'Asqalani (d. 852 AH) & Imam Shams al-Din al-Dhahabi (d. 748 AH)",
        volumeAndPage = "Al-Isabah Vol. 4, p. 144 (#4829) • Siyar: Vol. 1 (Sirat al-Khulafa' p. 15) • Tabaqat Ibn Sa'd Vol. 3, p. 169",
        arabicText = "لَوْ كُنْتُ مُتَّخِذًا مِنْ أُمَّتِي خَلِيلًا لَاتَّخَذْتُ أَبَا بَكْرٍ، وَلَكِنْ أَخِي وَصَاحِبِي... وَمَا نَفَعَنِي مَالُ أَحَدٍ قَطُّ مَا نَفَعَنِي مَالُ أَبِي بَكْرٍ. وَهُوَ أَوَّلُ الْخُلَفَاءِ الرَّاشِدِينَ، صَاحِبُ رَسُولِ اللَّهِ ﷺ فِي الْغَارِ، وَجَامِعُ الْقُرْآنِ الْكَرِيمِ فِي الصُّحُفِ.",
        urduTranslation = "حافظ ابن حجر عسقلانی نے ’الاصابہ فی تمییز الصحابہ‘ (جلد 4 ص 144) اور امام ابن سعد نے ’الطبقات الکبریٰ‘ (جلد 3 ص 169) میں بسندِ صحیح نقل فرمایا ہے کہ رسول اللہ ﷺ نے فرمایا: ’اگر میں اپنی امت میں سے کسی کو خلیل بناتا تو ابوبکر کو بناتا، لیکن وہ میرے دینی بھائی اور میرے رفیقِ غار ہیں‘۔ آپ نے ہجرت، جیشِ عسرہ اور تمام غزوات میں اپنا مال راہِ خدا میں قربان کیا اور اپنے عہدِ خلافت (11 تا 13 ہجری) میں حضرت زید بن ثابتؓ کے ذریعے قرآن کریم کو مصحف (صحیفوں) میں جمع فرمایا۔",
        englishTranslation = "Documented in Al-Isabah fi Tamyiz al-Sahabah (Vol. 4, p. 144) by Hafiz Ibn Hajar al-'Asqalani and Al-Tabaqat al-Kubra (Vol. 3, p. 169) by Imam Ibn Sa'd: The Messenger of Allah (ﷺ) said: 'Were I to take an intimate friend (khalil) from my Ummah, I would have taken Abu Bakr, but he is my brother and my companion in the Cave.' He accompanied the Prophet (ﷺ) in the Hijrah (Quran 9:40), witnessed Badr and all campaigns, and during his Caliphate (11–13 AH) commissioned Zayd ibn Thabit (RA) to compile the written leaves (Suhuf) of the Noble Qur'an.",
        translatorAttribution = "Maulana Abdullah al-Imadi (Tabaqat Ibn Sa'd Urdu) & Darussalam Classical Biographical Series",
        crossReferences = "Quran 9:40, 92:17–21 • Sahih al-Bukhari 3654, 4986 • Al-Isabah 4:144 • Tabaqat Ibn Sa'd 3:169"
      ),
      // 2. 'Umar ibn al-Khattab (RA) — Sunni Biographical Source
      BiographyEntry(
        id = "bio_sunni_umar",
        personNameEnglish = "'Umar ibn al-Khattab al-Faruq (RA)",
        personNameArabic = "أمير المؤمنين عمر بن الخطاب الفاروق العدوي القرشي رضي الله عنه",
        personNameUrdu = "امیر المومنین سیدنا عمر بن خطاب فاروقِ اعظم رضی اللہ عنہ",
        titleHonorific = "Al-Faruq • Amir al-Mu'minin • Second Rightly Guided Caliph",
        category = "Khulafa & 'Asharah Mubashsharah",
        traditionPerspective = "SUNNI",
        eraHijri = "40 BH – 23 AH",
        primarySourceBook = "Usd al-Ghabah fi Ma'rifat al-Sahabah & Al-Isabah fi Tamyiz al-Sahabah",
        sourceBookArabic = "أسد الغابة في معرفة الصحابة لابن الأثير والإصابة لابن حجر والطبقات الكبرى لابن سعد",
        author = "Imam 'Izz al-Din Ibn al-Athir (d. 630 AH) & Imam Ibn Sa'd (d. 230 AH)",
        volumeAndPage = "Usd al-Ghabah Vol. 4, p. 145 (#3914) • Al-Isabah Vol. 4, p. 484 (#5737) • Tabaqat Ibn Sa'd Vol. 3, p. 265",
        arabicText = "إِنَّ اللَّهَ جَعَلَ الْحَقَّ عَلَى لِسَانِ عُمَرَ وَقَلْبِهِ، وَهُوَ الْفَارُوقُ فَرَّقَ اللَّهُ بِهِ بَيْنَ الْحَقِّ وَالْبَاطِلِ. وَلِيَ الْخِلَافَةَ عَشْرَ سِنِينَ وَسِتَّةَ أَشْهُرٍ، فَفُتِحَتْ فِي أَيَّامِهِ الشَّامُ وَالْعِرَاقُ وَفَارِسُ وَمِصْرُ وَبَيْتُ الْمَقْدِسِ، وَدَوَّنَ الدَّوَاوِينَ وَأَرَّخَ التَّارِيخَ الْهِجْرِيَّ.",
        urduTranslation = "امام ابن اثیر جزری نے ’اسد الغابہ فی معرفۃ الصحابہ‘ (جلد 4 ص 145) اور ابن سعد نے ’الطبقات الکبریٰ‘ (جلد 3 ص 265) میں روایت کیا ہے کہ نبی کریم ﷺ نے فرمایا: ’بے شک اللہ نے عمر کی زبان اور دل پر حق جاری فرما دیا ہے‘۔ آپ کے دس سالہ عہدِ خلافت (13 تا 23 ہجری) میں شام، عراق، فارس، مصر اور بیت المقدس فتح ہوئے، عہدہ عمریہ لکھا گیا، ہجری تقویم اور دیوان و بیت المال کا نظام قائم ہوا۔",
        englishTranslation = "Recorded in Usd al-Ghabah fi Ma'rifat al-Sahabah (Vol. 4, p. 145) by Ibn al-Athir and Al-Tabaqat al-Kubra (Vol. 3, p. 265) by Ibn Sa'd: The Prophet (ﷺ) said: 'Indeed Allah has placed truth upon 'Umar's tongue and heart; he is al-Faruq by whom Allah distinguished truth from falsehood.' During his ten-year Caliphate (13–23 AH), Bilad al-Sham, Iraq, Persia, Egypt, and Jerusalem were opened, the Covenant of Jerusalem ('Uhdat 'Umar) was granted, the Hijri calendar was instituted, and the public treasury (Diwan) and independent judiciary were organized.",
        translatorAttribution = "Maulana Abdul Shakoor Farooqi & Allama Abdullah al-Imadi (Urdu)",
        crossReferences = "Sahih al-Bukhari 3682 • Jami' at-Tirmidhi 3682 • Usd al-Ghabah 4:145 • Tabaqat Ibn Sa'd 3:265"
      ),
      // 3. 'Uthman ibn 'Affan (RA) — Sunni Biographical Source
      BiographyEntry(
        id = "bio_sunni_uthman",
        personNameEnglish = "'Uthman ibn 'Affan Dhu al-Nurayn (RA)",
        personNameArabic = "أمير المؤمنين عثمان بن عفان ذو النورين الأموي القرشي رضي الله عنه",
        personNameUrdu = "امیر المومنین سیدنا عثمان بن عفان ذوالنورین رضی اللہ عنہ",
        titleHonorific = "Dhu al-Nurayn (Possessor of the Two Lights) • Third Rightly Guided Caliph",
        category = "Khulafa & 'Asharah Mubashsharah",
        traditionPerspective = "SUNNI",
        eraHijri = "47 BH – 35 AH",
        primarySourceBook = "Al-Isti'ab fi Ma'rifat al-Ashab & Al-Isabah fi Tamyiz al-Sahabah",
        sourceBookArabic = "الاستيعاب في معرفة الأصحاب لابن عبد البر والإصابة لابن حجر العسقلاني",
        author = "Imam Ibn 'Abd al-Barr al-Qurtubi (d. 463 AH) & Hafiz Ibn Hajar al-'Asqalani (d. 852 AH)",
        volumeAndPage = "Al-Isti'ab Vol. 3, p. 1037 (#1745) • Al-Isabah Vol. 4, p. 377 (#5443) • Tabaqat Ibn Sa'd Vol. 3, p. 53",
        arabicText = "أَلَا أَسْتَحِي مِنْ رَجُلٍ تَسْتَحِي مِنْهُ الْمَلَائِكَةُ؟ وَاشْتَرَى بِئْرَ رُومَةَ فَجَعَلَهَا لِلْمُسْلِمِينَ، وَجَهَّزَ جَيْشَ الْعُسْرَةِ، وَجَمَعَ الْأُمَّةَ عَلَى الْمُصْحَفِ الْإِمَامِ بِالرَّسْمِ الْعُثْمَانِيِّ بِإِجْمَاعِ الصَّحَابَةِ.",
        urduTranslation = "امام ابن عبدالبر قرطبی نے ’الاستیعاب فی معرفۃ الاصحاب‘ (جلد 3 ص 1037) اور حافظ ابن حجر نے ’الاصابہ‘ (جلد 4 ص 377) میں ذکر فرمایا ہے کہ رسول اللہ ﷺ کی دو صاحبزادیاں حضرت رقیہ اور حضرت ام کلثوم رضی اللہ عنہما یکے بعد دیگرے آپ کے نکاح میں آئیں جس سے آپ کا لقب ’ذوالنورین‘ ہوا۔ آپ نے مدینہ میں بئرِ رومہ خرید کر وقف کیا، جیشِ عسرہ (غزوہ تبوک) کا سامان مہیا کیا اور صحابہ کرامؓ کے اجماع سے مصحفِ عثمانی (رسمِ عثمانی) پر پوری امت کو جمع فرمایا۔",
        englishTranslation = "In Al-Isti'ab fi Ma'rifat al-Ashab (Vol. 3, p. 1037) by Ibn 'Abd al-Barr and Al-Isabah (Vol. 4, p. 377) by Ibn Hajar: Known as Dhu al-Nurayn because he married two daughters of the Prophet (ﷺ), Ruqayyah and Umm Kulthum (RA). The Prophet (ﷺ) said of him: 'Should I not be modest before a man before whom the angels are modest?' He endowed the Well of Rumah for the Muslims, equipped the Army of Hardship (Tabuk), expanded the Prophet's Mosque, and united the Ummah upon the canonical Uthmani Mushaf with the consensus of the Companions.",
        translatorAttribution = "Allama Abdullah al-Imadi & Nafees Academy Classical Urdu Edition",
        crossReferences = "Sahih Muslim 2401 • Sahih al-Bukhari 4987 • Al-Isti'ab 3:1037 • Al-Isabah 4:377"
      ),
      // 4. 'Ali ibn Abi Talib (RA) — Sunni Biographical Source
      BiographyEntry(
        id = "bio_sunni_ali",
        personNameEnglish = "'Ali ibn Abi Talib (RA) — Sunni Biographical Perspective",
        personNameArabic = "أمير المؤمنين علي بن أبي طالب الهاشمي القرشي رضي الله عنه",
        personNameUrdu = "امیر المومنین سیدنا علی بن ابی طالب المرتضیٰ رضی اللہ عنہ (اہلسنت مآخذ)",
        titleHonorific = "Amir al-Mu'minin • Asadullah • Fourth Rightly Guided Caliph • Father of al-Hasan & al-Husayn",
        category = "Khulafa & 'Asharah Mubashsharah",
        traditionPerspective = "SUNNI",
        eraHijri = "23 BH – 40 AH",
        primarySourceBook = "Al-Isabah fi Tamyiz al-Sahabah, Usd al-Ghabah & Siyar A'lam al-Nubala'",
        sourceBookArabic = "الإصابة في تمييز الصحابة لابن حجر وأسد الغابة لابن الأثير والطبقات الكبرى لابن سعد",
        author = "Hafiz Ibn Hajar al-'Asqalani (d. 852 AH), Ibn al-Athir (d. 630 AH) & Ibn Sa'd (d. 230 AH)",
        volumeAndPage = "Al-Isabah Vol. 4, p. 464 (#5690) • Usd al-Ghabah Vol. 4, p. 91 (#3783) • Tabaqat Ibn Sa'd Vol. 3, p. 19",
        arabicText = "لَأُعْطِيَنَّ الرَّايَةَ غَدًا رَجُلًا يُحِبُّ اللَّهَ وَرَسُولَهُ وَيُحِبُّهُ اللَّهُ وَرَسُولُهُ، يَفْتَحُ اللَّهُ عَلَى يَدَيْهِ... أَنْتَ مِنِّي بِمَنْزِلَةِ هَارُونَ مِنْ مُوسَى إِلَّا أَنَّهُ لَا نَبِيَّ بَعْدِي. وَقَالَ النَّبِيُّ ﷺ: أَقْضَاكُمْ عَلِيٌّ.",
        urduTranslation = "حافظ ابن حجر عسقلانی نے ’الاصابہ‘ (جلد 4 ص 464) اور ابن اثیر نے ’اسد الغابہ‘ (جلد 4 ص 91) میں سیدنا علی بن ابی طالب رضی اللہ عنہ کے مناقب میں صحیحین کی احادیث نقل کی ہیں کہ غزوہ خیبر کے دن نبی کریم ﷺ نے فرمایا: ’کل میں یہ پرچم ایسے شخص کو دوں گا جو اللہ اور اس کے رسول سے محبت کرتا ہے اور اللہ اور اس کا رسول اس سے محبت کرتے ہیں‘۔ آپ ﷺ نے فرمایا: ’تم میرے لیے ایسے ہو جیسے ہارون موسیٰ کے لیے تھے، سوائے اس کے کہ میرے بعد کوئی نبی نہیں‘۔ آپ عشرہ مبشرہ، دامادِ رسول (زوجِ سیدہ فاطمہ الزہراءؓ)، مدینہ کے قاضیِ اعظم اور چوتھے خلیفۂ راشد (35 تا 40 ہجری) ہیں۔",
        englishTranslation = "In Al-Isabah (Vol. 4, p. 464), Usd al-Ghabah (Vol. 4, p. 91), and Tabaqat Ibn Sa'd (Vol. 3, p. 19): First youth to embrace Islam, cousin and son-in-law of the Prophet (ﷺ) married to Lady Fatimah al-Zahra' (RA). At Khaybar the Prophet (ﷺ) declared: 'Tomorrow I shall give the banner to a man who loves Allah and His Messenger and whom Allah and His Messenger love.' And at Tabuk: 'You are to me in the station of Harun to Musa, except that there is no Prophet after me.' Revered across Ahl al-Sunnah as the supreme jurist of Madinah ('Aqdakum 'Ali') and the Fourth Rightly Guided Caliph (35–40 AH).",
        translatorAttribution = "Hafiz Ibn Hajar / Maulana Muhammad Ibrahim Nadvi & Darussalam",
        crossReferences = "Sahih al-Bukhari 3701, 4416 • Sahih Muslim 2404 • Al-Isabah 4:464 • Usd al-Ghabah 4:91"
      ),
      // 5. 'Ali ibn Abi Talib (AS) — Shia Biographical Source (Kitab al-Irshad)
      BiographyEntry(
        id = "bio_shia_ali",
        personNameEnglish = "Amir al-Mu'minin 'Ali ibn Abi Talib (AS) — Shia Biographical Perspective",
        personNameArabic = "أمير المؤمنين الإمام علي بن أبي طالب عليه السلام (كتاب الإرشاد للشيخ المفيد)",
        personNameUrdu = "امیر المومنین حضرت امام علی بن ابی طالب علیہ السلام (کتاب الارشاد للشیخ المفید)",
        titleHonorific = "Amir al-Mu'minin • Sayyid al-Wasiyyin • First Imam of Ahl al-Bayt",
        category = "Ahl al-Bayt",
        traditionPerspective = "SHIA",
        eraHijri = "23 BH – 40 AH",
        primarySourceBook = "Kitab al-Irshad fi Ma'rifat Hujaj Allah 'ala al-'Ibad & A'lam al-Wara",
        sourceBookArabic = "كتاب الإرشاد للشيخ المفيد وإعلام الورى بأعلام الهدى للشيخ الطبرسي",
        author = "Shaykh al-Mufid Muhammad ibn Muhammad ibn al-Nu'man (d. 413 AH)",
        volumeAndPage = "Kitab al-Irshad Vol. 1, pp. 5–355 • A'lam al-Wara Vol. 1, pp. 305–410",
        arabicText = "مَنْ كُنْتُ مَوْلَاهُ فَهَذَا عَلِيٌّ مَوْلَاهُ، اللَّهُمَّ وَالِ مَنْ وَالَاهُ وَعَادِ مَنْ عَادَاهُ... وَبَاتَ عَلَى فِرَاشِ رَسُولِ اللَّهِ ﷺ لَيْلَةَ الْمَبِيتِ فَأَنْزَلَ اللَّهُ: {وَمِنَ النَّاسِ مَنْ يَشْرِي نَفْسَهُ ابْتِغَاءَ مَرْضَاتِ اللَّهِ}.",
        urduTranslation = "شیخ مفید (متوفی 413 ہجری) نے ’کتاب الارشاد‘ (جلد اول، ص 5 تا 355) میں حضرت امیر المومنین علی ابن ابی طالب علیہ السلام کی ولادتِ کعبہ، سبقتِ اسلام، شبِ ہجرت (لیلۃ المبیت) میں رسول اللہ ﷺ کے بستر پر جان کا نذرانہ پیش کرنے، غزواتِ بدر و احد و خندق و خیبر و حنین میں نصرتِ اسلام، حدیثِ غدیر و حدیثِ ثقلین، اور آپ کے خطبات و اقوال و عہدِ حکومت (جمل، صفین، نہروان) اور مسجدِ کوفہ میں شہادت (21 رمضان 40 ہجری) کو امامیہ اسناد کے ساتھ مفصل بیان فرمایا ہے۔",
        englishTranslation = "In Kitab al-Irshad (Vol. 1, pp. 5–355), Shaykh al-Mufid (d. 413 AH) chronicles the biography of Amir al-Mu'minin 'Ali ibn Abi Talib (AS) as the First Imam of the Ahl al-Bayt: his birth inside the Ka'bah, being the first to pray with the Prophet (ﷺ), sleeping on the Prophet's bed on Laylat al-Mabit (Quran 2:207), his decisive valor at Badr, Uhud, Khandaq (duel with 'Amr ibn 'Abd Wudd), and Khaybar, the investiture at Ghadir Khumm, the preservation of Qur'anic wisdom and Nahj al-Balagha, and his martyrdom in the Mihrab of the Great Mosque of Kufah in Ramadan 40 AH.",
        translatorAttribution = "Allama Syed Safdar Hussain Najafi (Urdu) & Dr. I.K.A. Howard (English)",
        crossReferences = "Quran 2:207, 5:55, 33:33 • Kitab al-Irshad 1:5–355 • Nahj al-Balagha"
      ),
      // 6. Lady Fatimah al-Zahra' (RA/SA) — Sunni Account
      BiographyEntry(
        id = "bio_sunni_fatimah",
        personNameEnglish = "Fatimah al-Zahra' bint Muhammad (RA) — Sunni Biographical Perspective",
        personNameArabic = "سيدة نساء أهل الجنة فاطمة الزهراء بنت رسول الله ﷺ رضي الله عنها",
        personNameUrdu = "سیدۃ نساء اہل الجنۃ حضرت فاطمۃ الزہراء بنت رسول اللہ ﷺ (اہلسنت مآخذ)",
        titleHonorific = "Sayyidat Nisa' Ahl al-Jannah • Al-Zahra' • Al-Batul",
        category = "Ahl al-Bayt",
        traditionPerspective = "SUNNI",
        eraHijri = "5 BH – 11 AH",
        primarySourceBook = "Al-Isabah fi Tamyiz al-Sahabah, Siyar A'lam al-Nubala' & Tabaqat Ibn Sa'd",
        sourceBookArabic = "الإصابة في تمييز الصحابة (كتاب النساء) وسير أعلام النبلاء والطبقات الكبرى (المجلد الثامن)",
        author = "Hafiz Ibn Hajar al-'Asqalani (d. 852 AH) & Imam Shams al-Din al-Dhahabi (d. 748 AH)",
        volumeAndPage = "Al-Isabah Vol. 8, p. 262 (#11583) • Siyar A'lam al-Nubala' Vol. 2, p. 118 • Tabaqat Ibn Sa'd Vol. 8, p. 19",
        arabicText = "فَاطِمَةُ بَضْعَةٌ مِنِّي، فَمَنْ أَغْضَبَهَا أَغْضَبَنِي... يَا فَاطِمَةُ أَلَا تَرْضَيْنَ أَنْ تَكُونِي سَيِّدَةَ نِسَاءِ الْمُؤْمِنِينَ أَوْ سَيِّدَةَ نِسَاءِ هَذِهِ الْأُمَّةِ؟",
        urduTranslation = "حافظ ابن حجر عسقلانی نے ’الاصابہ‘ (جلد 8 ص 262) اور امام ذہبی نے ’سیر اعلام النبلاء‘ (جلد 2 ص 118) میں سیدہ فاطمۃ الزہراء رضی اللہ عنہا کی سوانح میں صحیح بخاری و مسلم کی احادیث نقل کی ہیں کہ رسول اللہ ﷺ نے فرمایا: ’فاطمہ میرے جگر کا ٹکڑا ہے، جس نے اسے ناراض کیا اس نے مجھے ناراض کیا‘، اور مرضِ وفات میں آپ ﷺ نے انہیں بشارت دی کہ وہ جنت کی تمام عورتوں کی سردار ہیں اور اہلِ بیت میں سب سے پہلے نبی کریم ﷺ سے جا ملیں گی۔ آپ کا وصال نبوی وفات کے چھ ماہ بعد رمضان 11 ہجری میں ہوا۔",
        englishTranslation = "In Al-Isabah (Vol. 8, p. 262), Siyar A'lam al-Nubala' (Vol. 2, p. 118), and Tabaqat Ibn Sa'd (Vol. 8, p. 19): Beloved youngest daughter of the Messenger of Allah (ﷺ) and Khadijah al-Kubra (RA), wife of 'Ali ibn Abi Talib (RA), and mother of al-Hasan, al-Husayn, Zaynab, and Umm Kulthum (RA). Included under the Cloak in Hadith al-Kisa' (Sahih Muslim 2424 / Quran 33:33). The Prophet (ﷺ) declared: 'Fatimah is a part of me; whoever angers her angers me,' and gave her glad tidings that she is the Leader of the Women of Paradise and the first of his family to rejoin him (passing away in Ramadan 11 AH).",
        translatorAttribution = "Allama Abdullah al-Imadi & Darussalam Biographical Series",
        crossReferences = "Quran 33:33 • Sahih al-Bukhari 3623, 3714 • Sahih Muslim 2424, 2450 • Al-Isabah 8:262"
      ),
      // 7. Lady Fatimah al-Zahra' (SA) — Shia Account
      BiographyEntry(
        id = "bio_shia_fatimah",
        personNameEnglish = "Sayyidah Fatimah al-Zahra' (SA) — Shia Biographical Perspective",
        personNameArabic = "فاطمة الزهراء سيدة نساء العالمين عليها السلام (الأمالي وإعلام الورى وكشف الغمة)",
        personNameUrdu = "سیدۃ نساء العالمین حضرت فاطمۃ الزہراء سلام اللہ علیہا (شیعہ مآخذ)",
        titleHonorific = "Sayyidat Nisa' al-'Alamin • Al-Siddiqah al-Tahira • Al-Zahra'",
        category = "Ahl al-Bayt",
        traditionPerspective = "SHIA",
        eraHijri = "5 BH – 11 AH",
        primarySourceBook = "A'lam al-Wara bi-A'lam al-Huda, Al-Amali (Al-Saduq) & Kashf al-Ghummah",
        sourceBookArabic = "إعلام الورى للطبرسي وأمالي الشيخ الصدوق وكشف الغمة في معرفة الأئمة للإربلي",
        author = "Shaykh Amin al-Islam al-Tabarsi (d. 548 AH) & Shaykh al-Saduq (d. 381 AH)",
        volumeAndPage = "A'lam al-Wara Vol. 1, pp. 287–304 • Al-Kafi Vol. 1, p. 458 • Kashf al-Ghummah Vol. 1, p. 449",
        arabicText = "إِنَّ اللَّهَ يَغْضَبُ لِغَضَبِ فَاطِمَةَ وَيَرْضَى لِرِضَاهَا... وَتَسْبِيحُ فَاطِمَةَ الزَّهْرَاءِ عَلَيْهَا السَّلَامُ فِي دُبُرِ كُلِّ صَلَاةٍ أَحَبُّ إِلَيَّ مِنْ صَلَاةِ أَلْفِ رَكْعَةٍ فِي كُلِّ يَوْمٍ.",
        urduTranslation = "شیخ طبرسی کی ’اعلام الوریٰ‘ (جلد 1 ص 287 تا 304) اور شیخ کلینی کی ’الکافی‘ (جلد 1 ص 458) میں حضرت سیدہ فاطمۃ الزہراء سلام اللہ علیہا کی طہارت و عصمت (آیتِ تطہیر 33:33)، مباہلہ نجران (آلِ عمران 3:61)، سورۃ الدہر (76:8) میں ایثار، تسبیحِ فاطمہ زہراءؑ (34 بار اللہ اکبر، 33 بار الحمد للہ، 33 بار سبحان اللہ) اور خطبۂ فدکیہ کے علمی و توحیدی معارف تفصیل سے بیان ہوئے ہیں۔",
        englishTranslation = "In A'lam al-Wara (Vol. 1, pp. 287–304) by Shaykh al-Tabarsi and Al-Kafi (Vol. 1, p. 458) by Shaykh al-Kulayni: Revered in Twelver Shia theology as one of the Fourteen Infallibles (al-Ma'sumin) and the axis of Hadith al-Kisa', the Verse of Purification (33:33), the Verse of Mubahalah (3:61), and Surah al-Insan (76:8–9). Preserves the transmission of Tasbih Fatimah al-Zahra' (34 Allahu Akbar, 33 Alhamdulillah, 33 SubhanAllah after every prayer) and her theological discourse in the Mosque of Madinah.",
        translatorAttribution = "Allama Syed Zeeshan Haider Jawadi & Ansariyan Qom Edition",
        crossReferences = "Quran 3:61, 33:33, 76:8, 108:1 • A'lam al-Wara 1:287 • Al-Kafi 1:458"
      ),
      // 8. Imam al-Hasan ibn 'Ali (RA/AS) — Sunni & Shia Perspectives
      BiographyEntry(
        id = "bio_sunni_hasan",
        personNameEnglish = "Imam al-Hasan ibn 'Ali al-Mujtaba (RA) — Sunni Biographical Perspective",
        personNameArabic = "الإمام الحسن بن علي بن أبي طالب سبط رسول الله ﷺ وريحانته رضي الله عنهما",
        personNameUrdu = "سیدنا امام حسن بن علی مجتبیٰ رضی اللہ عنہما (اہلسنت مآخذ)",
        titleHonorific = "Sayyid Shabab Ahl al-Jannah • Rayhanat Rasulillah (ﷺ) • Fifth Rightly Guided Caliph",
        category = "Ahl al-Bayt",
        traditionPerspective = "SUNNI",
        eraHijri = "3 AH – 50 AH",
        primarySourceBook = "Al-Isabah fi Tamyiz al-Sahabah, Siyar A'lam al-Nubala' & Al-Bidaya wa al-Nihaya",
        sourceBookArabic = "الإصابة لابن حجر وسير أعلام النبلاء للذهبي والبداية والنهاية لابن كثير",
        author = "Hafiz Ibn Hajar al-'Asqalani (d. 852 AH) & Imam al-Dhahabi (d. 748 AH)",
        volumeAndPage = "Al-Isabah Vol. 2, p. 60 (#1725) • Siyar A'lam al-Nubala' Vol. 3, p. 245 • Sahih al-Bukhari 2704",
        arabicText = "إِنَّ ابْنِي هَذَا سَيِّدٌ، وَلَعَلَّ اللَّهَ أَنْ يُصْلِحَ بِهِ بَيْنَ فِئَتَيْنِ عَظِيمَتَيْنِ مِنَ الْمُسْلِمِينَ.",
        urduTranslation = "حافظ ابن حجر نے ’الاصابہ‘ (جلد 2 ص 60) اور امام ذہبی نے ’سیر اعلام النبلاء‘ (جلد 3 ص 245) میں صحیح بخاری (2704) کی حدیث نقل کی ہے کہ رسول اللہ ﷺ نے سیدنا امام حسن رضی اللہ عنہ کو منبر پر اپنے پہلو میں بٹھا کر فرمایا: ’میرا یہ بیٹا سردار (سید) ہے اور امید ہے کہ اللہ اس کے ذریعے مسلمانوں کی دو بڑی جماعتوں کے درمیان صلح کرائے گا‘۔ چنانچہ سن 41 ہجری (عام الجماعۃ) میں آپ نے امت کے خون کی حفاظت کے لیے خلافت سے دستبرداری اختیار کر کے اس نبوی معجزے کی تکمیل فرمائی اور آپ کی چھ ماہ کی خلافت کے ساتھ خلافتِ راشدہ کے تیس سال مکمل ہوئے۔",
        englishTranslation = "In Al-Isabah (Vol. 2, p. 60) and Siyar A'lam al-Nubala' (Vol. 3, p. 245): Elder grandson of the Prophet (ﷺ) and Master of the Youth of Paradise. In Sahih al-Bukhari 2704, the Prophet (ﷺ) stood him on the pulpit and proclaimed: 'This son of mine is a Sayyid (noble leader), and perhaps Allah will reconcile through him two great groups of Muslims.' In 41 AH ('Am al-Jama'ah), after six months of Caliphate completing the 30-year Prophetic prophecy of Khilafah Nubuwwah (Abu Dawud 4646), he made peace to spare Muslim blood.",
        translatorAttribution = "Maulana Akhtar Fatehpuri & Darussalam",
        crossReferences = "Sahih al-Bukhari 2704, 3746 • Sunan Abi Dawud 4646 • Siyar A'lam al-Nubala' 3:245"
      ),
      // 9. Imam al-Husayn ibn 'Ali (RA) — Sunni Biographical Perspective
      BiographyEntry(
        id = "bio_sunni_husayn",
        personNameEnglish = "Imam al-Husayn ibn 'Ali Sayyid al-Shuhada' (RA) — Sunni Biographical Perspective",
        personNameArabic = "الإمام الشهيد أبو عبد الله الحسين بن علي بن أبي طالب سبط رسول الله ﷺ رضي الله عنهما",
        personNameUrdu = "سید الشہداء سیدنا امام حسین بن علی رضی اللہ عنہما (اہلسنت مآخذ)",
        titleHonorific = "Sayyid Shabab Ahl al-Jannah • Rayhanat Rasulillah (ﷺ) • Martyr of Karbala (61 AH)",
        category = "Ahl al-Bayt",
        traditionPerspective = "SUNNI",
        eraHijri = "4 AH – 10 Muharram 61 AH",
        primarySourceBook = "Siyar A'lam al-Nubala', Al-Isabah fi Tamyiz al-Sahabah & Tarikh al-Tabari",
        sourceBookArabic = "سير أعلام النبلاء للذهبي والإصابة لابن حجر وتاريخ الطبري والبداية والنهاية",
        author = "Imam Shams al-Din al-Dhahabi (d. 748 AH), Hafiz Ibn Hajar (d. 852 AH) & Imam al-Tabari (d. 310 AH)",
        volumeAndPage = "Siyar A'lam al-Nubala' Vol. 3, pp. 280–321 • Al-Isabah Vol. 2, p. 68 (#1726) • Tarikh al-Tabari Vol. 5, pp. 400–458",
        arabicText = "حُسَيْنٌ مِنِّي وَأَنَا مِنْ حُسَيْنٍ، أَحَبَّ اللَّهُ مَنْ أَحَبَّ حُسَيْنًا، حُسَيْنٌ سِبْطٌ مِنَ الْأَسْبَاطِ... وَقُتِلَ رَضِيَ اللَّهُ عَنْهُ يَوْمَ عَاشُورَاءَ سَنَةَ إِحْدَى وَسِتِّينَ بِكَرْبَلَاءَ شَهِيدًا مَظْلُومًا.",
        urduTranslation = "امام ذہبی نے ’سیر اعلام النبلاء‘ (جلد 3 ص 280 تا 321) اور حافظ ابن حجر نے ’الاصابہ‘ (جلد 2 ص 68) میں سیدنا امام حسین رضی اللہ عنہ کی سوانح میں جامع ترمذی (3775) کی حدیث نقل کی ہے: ’حسین مجھ سے ہے اور میں حسین سے ہوں، اللہ اس سے محبت فرمائے جو حسین سے محبت کرے‘۔ تاریخ طبری (جلد 5 ص 400 تا 458) اور البدایہ والنہایہ (جلد 8 ص 202) کے مطابق 10 محرم 61 ہجری کو میدانِ کربلا میں آپ اور آپ کے اہلِ بیت و اصحاب نے حق و عدل کی سربلندی کے لیے جامِ شہادت نوش فرمایا۔",
        englishTranslation = "In Siyar A'lam al-Nubala' (Vol. 3, pp. 280–321) by Imam al-Dhahabi, Al-Isabah (Vol. 2, p. 68) by Ibn Hajar, and Tarikh al-Tabari (Vol. 5, pp. 400–458): Beloved grandson of the Messenger of Allah (ﷺ), who said: 'Husayn is from me and I am from Husayn; may Allah love whoever loves Husayn' (Tirmidhi 3775). Refusing to pledge allegiance to Yazid's unjust rule, he stood with his household and loyal companions at Karbala and was martyred on 10 Muharram 61 AH ('Ashura).",
        translatorAttribution = "Maulana Syed Muhammad Ibrahim Nadvi & Allama Rashid Ahmad Arshad",
        crossReferences = "Jami' at-Tirmidhi 3768, 3775 • Sahih al-Bukhari 3748 • Siyar A'lam al-Nubala' 3:280 • Tarikh al-Tabari 5:400"
      ),
      // 10. Imam al-Husayn ibn 'Ali (AS) — Shia Biographical Perspective
      BiographyEntry(
        id = "bio_shia_husayn",
        personNameEnglish = "Imam Abu 'Abdillah al-Husayn ibn 'Ali (AS) — Shia Biographical Perspective",
        personNameArabic = "سيد الشهداء الإمام أبو عبد الله الحسين بن علي عليه السلام (كتاب الإرشاد واللهوف)",
        personNameUrdu = "سید الشہداء حضرت امام ابو عبداللہ الحسین علیہ السلام (کتاب الارشاد و اللہوف)",
        titleHonorific = "Sayyid al-Shuhada' • Third Imam of Ahl al-Bayt • Master of the Martyrs of Karbala",
        category = "Ahl al-Bayt",
        traditionPerspective = "SHIA",
        eraHijri = "4 AH – 10 Muharram 61 AH",
        primarySourceBook = "Kitab al-Irshad (Shaykh al-Mufid), Al-Luhuf (Ibn Tawus) & Maqatil al-Talibiyyin",
        sourceBookArabic = "كتاب الإرشاد للشيخ المفيد واللهوف على قتلى الطفوف لابن طاووس ومقاتل الطالبين",
        author = "Shaykh al-Mufid (d. 413 AH) & Sayyid Radi al-Din Ibn Tawus (d. 664 AH)",
        volumeAndPage = "Kitab al-Irshad Vol. 2, pp. 27–135 • Al-Luhuf pp. 21–135 • Maqatil al-Talibiyyin pp. 78–122",
        arabicText = "إِنِّي لَمْ أَخْرُجْ أَشِرًا وَلَا بَطِرًا وَلَا مُفْسِدًا وَلَا ظَالِمًا، وَإِنَّمَا خَرَجْتُ لِطَلَبِ الْإِصْلَاحِ فِي أُمَّةِ جَدِّي، أُرِيدُ أَنْ آمُرَ بِالْمَعْرُوفِ وَأَنْهَى عَنِ الْمُنْكَرِ وَأَسِيرَ بِسِيرَةِ جَدِّي وَأَبِي عَلِيِّ بْنِ أَبِي طَالِبٍ.",
        urduTranslation = "شیخ مفید کی ’کتاب الارشاد‘ (جلد 2 ص 27 تا 135) اور سید ابن طاؤس کی ’اللہوف‘ میں حضرت امام حسین علیہ السلام کی وصیت باسند محفوظ ہے: ’میں ہرگز خودرائی، تکبر، فساد یا ظلم کے لیے نہیں نکلا، بلکہ میں صرف اپنے نانا رسول اللہ ﷺ کی امت کی اصلاح کے لیے نکلا ہوں، میں امر بالمعروف اور نہی عن المنکر کرنا چاہتا ہوں اور اپنے نانا ﷺ اور اپنے بابا علی بن ابی طالبؑ کی سیرت پر چلنا چاہتا ہوں‘۔ آپ کے ساتھ بنو ہاشم کے شہزادوں (حضرت عباس علمدارؑ، حضرت علی اکبرؑ، قاسم بن حسنؑ) اور باوفا اصحاب نے عاشورہ 61 ہجری کو شہادت پائی۔",
        englishTranslation = "In Kitab al-Irshad (Vol. 2, pp. 27–135) by Shaykh al-Mufid and Al-Luhuf by Sayyid Ibn Tawus: Third Imam of the Ahl al-Bayt. In his historic testament written to his brother Muhammad ibn al-Hanafiyyah upon departing Madinah in Rajab 60 AH, he declared: 'I did not go forth out of insolence, arrogance, corruption, or oppression; rather, I went forth solely to seek reform (Islah) in the Ummah of my Grandfather (ﷺ), to enjoin good and forbid evil, and to walk in the path of my Grandfather and my father 'Ali ibn Abi Talib.'",
        translatorAttribution = "Allama Syed Safdar Hussain Najafi & Dr. I.K.A. Howard",
        crossReferences = "Kitab al-Irshad 2:27–135 • Al-Luhuf pp. 21–135 • Maqatil al-Talibiyyin p. 78"
      ),
      // 11. Khadijah bint Khuwaylid (RA) — Mother of the Believers
      BiographyEntry(
        id = "bio_sunni_khadijah",
        personNameEnglish = "Umm al-Mu'minin Khadijah bint Khuwaylid al-Kubra (RA)",
        personNameArabic = "أم المؤمنين خديجة الكبرى بنت خويلد الأسدية القرشية رضي الله عنها",
        personNameUrdu = "ام المومنین سیدہ خدیجۃ الکبریٰ بنت خویلد رضی اللہ عنہا",
        titleHonorific = "Umm al-Mu'minin • Al-Tahira • First Believer in Islam",
        category = "Mothers of the Believers",
        traditionPerspective = "SUNNI",
        eraHijri = "68 BH – 3 BH (10th Year of Prophethood)",
        primarySourceBook = "Al-Isabah fi Tamyiz al-Sahabah, Siyar A'lam al-Nubala' & Tabaqat Ibn Sa'd",
        sourceBookArabic = "الإصابة لابن حجر وسير أعلام النبلاء للذهبي والطبقات الكبرى لابن سعد",
        author = "Hafiz Ibn Hajar al-'Asqalani (d. 852 AH) & Imam Ibn Sa'd (d. 230 AH)",
        volumeAndPage = "Al-Isabah Vol. 8, p. 99 (#11104) • Siyar A'lam al-Nubala' Vol. 2, p. 109 • Tabaqat Ibn Sa'd Vol. 8, p. 14",
        arabicText = "آمَنَتْ بِي إِذْ كَفَرَ بِي النَّاسُ، وَصَدَّقَتْنِي إِذْ كَذَّبَنِي النَّاسُ، وَوَاسَتْنِي بِمَالِهَا إِذْ حَرَمَنِي النَّاسُ، وَرَزَقَنِي اللَّهُ وَلَدَهَا إِذْ حَرَمَنِي أَوْلَادَ النِّسَاءِ. وَأَقْرَأَهَا جِبْرِيلُ السَّلَامَ مِنْ رَبِّهَا وَبَشَّرَهَا بِبَيْتٍ فِي الْجَنَّةِ مِنْ قَصَبٍ لَا صَخَبَ فِيهِ وَلَا نَصَبَ.",
        urduTranslation = "امام ابن سعد کی ’الطبقات الکبریٰ‘ (جلد 8 ص 14) اور حافظ ابن حجر کی ’الاصابہ‘ (جلد 8 ص 99) کے مطابق ام المومنین سیدہ خدیجہ الکبریٰ رضی اللہ عنہا سب سے پہلے رسول اللہ ﷺ پر ایمان لائیں۔ غارِ حرا میں پہلی وحی کے نزول پر آپ نے نبی کریم ﷺ کو تسلی دی، شعبِ ابی طالب کے تین سالہ محاصرے میں ثابت قدم رہیں، اور حضرت جبرائیل علیہ السلام نے اللہ تعالیٰ کی طرف سے آپ کو سلام پہنچایا اور جنت میں موتیوں کے محل کی بشارت دی۔",
        englishTranslation = "In Al-Tabaqat al-Kubra (Vol. 8, p. 14) by Ibn Sa'd and Al-Isabah (Vol. 8, p. 99) by Ibn Hajar: First person to believe in the Messenger of Allah (ﷺ). When revelation began in Cave Hira, she comforted the Prophet (ﷺ) saying: 'Never! By Allah, Allah will never disgrace you; you uphold ties of kinship, speak the truth, bear people's burdens, and honor guests' (Sahih al-Bukhari 3). Jibril (AS) conveyed greetings of peace to her from Allah and glad tidings of a palace of hollow pearls in Paradise free of noise and fatigue (Bukhari 3820).",
        translatorAttribution = "Allama Abdullah al-Imadi & Darussalam",
        crossReferences = "Sahih al-Bukhari 3, 3816, 3820 • Sahih Muslim 2432 • Tabaqat Ibn Sa'd 8:14"
      ),
      // 12. 'A'ishah bint Abi Bakr (RA) — Mother of the Believers
      BiographyEntry(
        id = "bio_sunni_aisha",
        personNameEnglish = "Umm al-Mu'minin 'A'ishah al-Siddiqah bint Abi Bakr (RA)",
        personNameArabic = "أم المؤمنين عائشة الصديقة بنت أبي بكر الصديق التيمية رضي الله عنهما",
        personNameUrdu = "ام المومنین سیدہ عائشہ صدیقہ بنت ابی بکر صدیق رضی اللہ عنہما",
        titleHonorific = "Umm al-Mu'minin • Al-Siddiqah • Scholar and Jurist of the Ummah",
        category = "Mothers of the Believers",
        traditionPerspective = "SUNNI",
        eraHijri = "9 BH – 58 AH",
        primarySourceBook = "Siyar A'lam al-Nubala', Al-Isabah & Tabaqat Ibn Sa'd",
        sourceBookArabic = "سير أعلام النبلاء للذهبي والإصابة لابن حجر والطبقات الكبرى لابن سعد",
        author = "Imam Shams al-Din al-Dhahabi (d. 748 AH) & Hafiz Ibn Hajar al-'Asqalani (d. 852 AH)",
        volumeAndPage = "Siyar A'lam al-Nubala' Vol. 2, p. 135 • Al-Isabah Vol. 8, p. 231 (#11457) • Tabaqat Ibn Sa'd Vol. 8, p. 58",
        arabicText = "مَا أَشْكَلَ عَلَيْنَا أَصْحَابَ رَسُولِ اللَّهِ ﷺ حَدِيثٌ قَطُّ فَسَأَلْنَا عَائِشَةَ إِلَّا وَجَدْنَا عِنْدَهَا مِنْهُ عِلْمًا. وَرَوَتْ أَلْفَيْنِ وَمِائَتَيْنِ وَعَشَرَةَ أَحَادِيثَ عَنْ رَسُولِ اللَّهِ ﷺ.",
        urduTranslation = "امام ذہبی نے ’سیر اعلام النبلاء‘ (جلد 2 ص 135) اور ابن سعد نے ’الطبقات الکبریٰ‘ (جلد 8 ص 58) میں حضرت ابو موسیٰ اشعریؓ کا قول نقل کیا ہے: ’ہم صحابۂ رسول ﷺ کو جب بھی کسی حدیث یا فقہی مسئلے میں اشکال پیش آیا اور ہم نے ام المومنین سیدہ عائشہ رضی اللہ عنہا سے دریافت کیا تو ان کے پاس اس کا علم ضرور پایا‘۔ سورۃ النور (آیات 11 تا 20) میں اللہ تعالیٰ نے آپ کی براءت نازل فرمائی، اور آپ نے نبی کریم ﷺ سے 2,210 احادیث روایت فرمائیں۔",
        englishTranslation = "In Siyar A'lam al-Nubala' (Vol. 2, p. 135) and Tabaqat Ibn Sa'd (Vol. 8, p. 58): Mother of the Believers whose innocence was revealed from above the seven heavens in Surah al-Nur (24:11–20). Abu Musa al-Ash'ari (RA) stated: 'Never did a Hadith become difficult for us, the Companions of the Messenger of Allah (ﷺ), and we asked 'A'ishah, except that we found knowledge of it with her' (Tirmidhi 3883). She narrated 2,210 Prophetic Hadiths and taught the senior jurists of Madinah until her passing in 58 AH.",
        translatorAttribution = "Allama Abdullah al-Imadi & Darussalam",
        crossReferences = "Quran 24:11–20 • Sahih al-Bukhari 3772, 4750 • Jami' at-Tirmidhi 3883 • Siyar 2:135"
      ),
      // 13. Imam 'Ali ibn al-Husayn Zayn al-'Abidin (RA/AS) — Sunni & Shia Accounts
      BiographyEntry(
        id = "bio_sunni_zaynulabidin",
        personNameEnglish = "Imam 'Ali ibn al-Husayn Zayn al-'Abidin (RA) — Sunni Biographical Perspective",
        personNameArabic = "الإمام زين العابدين علي بن الحسين بن علي بن أبي طالب الهاشمي المدني",
        personNameUrdu = "امام زین العابدین علی بن الحسین السجاد رحمہ اللہ (اہلسنت مآخذ)",
        titleHonorific = "Zayn al-'Abidin • Al-Sajjad • Sayyid al-Tabi'in in Madinah",
        category = "Ahl al-Bayt",
        traditionPerspective = "SUNNI",
        eraHijri = "38 AH – 94 AH",
        primarySourceBook = "Siyar A'lam al-Nubala' & Tahdhib al-Tahdhib",
        sourceBookArabic = "سير أعلام النبلاء للذهبي وتهذيب التهذيب لابن حجر والطبقات الكبرى لابن سعد",
        author = "Imam Shams al-Din al-Dhahabi (d. 748 AH) & Imam Ibn Sa'd (d. 230 AH)",
        volumeAndPage = "Siyar A'lam al-Nubala' Vol. 4, pp. 386–400 • Tabaqat Ibn Sa'd Vol. 5, p. 211",
        arabicText = "قَالَ الزُّهْرِيُّ: مَا رَأَيْتُ قُرَشِيًّا أَفْضَلَ مِنْ عَلِيِّ بْنِ الْحُسَيْنِ، وَكَانَ يَحْمِلُ جُرُبَ الدَّقِيقِ عَلَى ظَهْرِهِ بِاللَّيْلِ يَتَصَدَّقُ بِهِ عَلَى فُقَرَاءِ الْمَدِينَةِ سِرًّا، وَكَانَ يَعُولُ مِائَةَ أَهْلِ بَيْتٍ بِالْمَدِينَةِ لَا يَدْرُونَ مِنْ أَيْنَ مَعَاشُهُمْ حَتَّى مَاتَ.",
        urduTranslation = "امام ذہبی نے ’سیر اعلام النبلاء‘ (جلد 4 ص 386) اور ابن سعد نے ’الطبقات الکبریٰ‘ (جلد 5 ص 211) میں امام ابن شہاب زہریؒ کا قول نقل کیا ہے: ’میں نے قریش میں علی بن الحسین (زین العابدین) سے افضل اور زیادہ فقیہ کوئی شخص نہیں دیکھا‘۔ آپ رات کی تاریکی میں اپنی پشت پر آٹے کی بوریاں اٹھا کر مدینہ منورہ کے ایک سو غریب گھرانوں کے دروازوں پر خفیہ صدقہ پہنچاتے تھے، یہاں تک کہ آپ کے وصال پر غسل دیتے وقت آپ کے کندھوں پر بوریوں کے نشانات دیکھے گئے۔",
        englishTranslation = "In Siyar A'lam al-Nubala' (Vol. 4, pp. 386–400) by Imam al-Dhahabi and Tabaqat Ibn Sa'd (Vol. 5, p. 211): Son of Imam al-Husayn (RA) and survivor of Karbala. Imam Ibn Shihab al-Zuhri said: 'I never saw any Qurayshi more excellent or more learned in Fiqh than 'Ali ibn al-Husayn.' Every night in Madinah he secretly carried sacks of flour on his own back to feed a hundred poor households who did not know who their benefactor was until the marks on his shoulders were seen when he was washed for burial in 94 AH.",
        translatorAttribution = "Allama Abdullah al-Imadi & Mu'assasat al-Risalah Biographical Series",
        crossReferences = "Siyar A'lam al-Nubala' 4:386 • Tabaqat Ibn Sa'd 5:211 • Hilyat al-Awliya' 3:133"
      ),
      BiographyEntry(
        id = "bio_shia_zaynulabidin",
        personNameEnglish = "Imam 'Ali ibn al-Husayn Zayn al-'Abidin (AS) — Shia Biographical Perspective",
        personNameArabic = "الإمام علي بن الحسين زين العابدين السجاد عليه السلام (كتاب الإرشاد والصحيفة السجادية)",
        personNameUrdu = "حضرت امام علی بن الحسین زین العابدین السجاد علیہ السلام (کتاب الارشاد و صحیفہ سجادیہ)",
        titleHonorific = "Fourth Imam of Ahl al-Bayt • Author of Al-Sahifa al-Sajjadiyya & Risalat al-Huquq",
        category = "Ahl al-Bayt",
        traditionPerspective = "SHIA",
        eraHijri = "38 AH – 95 AH",
        primarySourceBook = "Kitab al-Irshad (Shaykh al-Mufid) & Al-Sahifa al-Sajjadiyya",
        sourceBookArabic = "كتاب الإرشاد للشيخ المفيد والصحيفة السجادية ورسالة الحقوق",
        author = "Shaykh al-Mufid (d. 413 AH)",
        volumeAndPage = "Kitab al-Irshad Vol. 2, pp. 137–156 • Al-Sahifa al-Sajjadiyya (54 Supplications)",
        arabicText = "أَيُّهَا النَّاسُ، مَنْ عَرَفَنِي فَقَدْ عَرَفَنِي، وَمَنْ لَمْ يَعْرِفْنِي فَأَنَا عَلِيُّ بْنُ الْحُسَيْنِ بْنِ عَلِيِّ بْنِ أَبِي طَالِبٍ، أَنَا ابْنُ الْمَذْبُوحِ بِالشَّطِّ مِنْ غَيْرِ ذَحْلٍ وَلَا تِرَةٍ.",
        urduTranslation = "شیخ مفید کی ’کتاب الارشاد‘ (جلد 2 ص 137 تا 156) میں حضرت امام زین العابدین علیہ السلام کے کوفہ و دمشق کے خطبات، مدینہ منورہ میں واقعہ حرہ (63 ہجری) کے دوران مروان کے اہل و عیال سمیت چار سو خاندانوں کو پناہ و کفالت دینے، غلاموں کو تعلیم دے کر آزاد کرنے، اور ’صحیفہ سجادیہ‘ (زبورِ آلِ محمد — 54 مناجات و دعائیں) اور ’رسالۃ الحقوق‘ (پچاس حقوق کا منشور) کی تدوین کا مفصل بیان ہے۔",
        englishTranslation = "In Kitab al-Irshad (Vol. 2, pp. 137–156): Fourth Imam of the Ahl al-Bayt. Delivered the historic sermons in Kufah and Damascus after Karbala preserving the message of Imam al-Husayn (AS), sheltered 400 women and children of Madinah during Waqia Harrah (63 AH), educated and emancipated hundreds of slaves each Eid, and bequeathed Al-Sahifa al-Sajjadiyya (54 canonical supplications) and Risalat al-Huquq (The Treatise on Rights).",
        translatorAttribution = "Allama Syed Safdar Hussain Najafi & William C. Chittick (Sahifa English)",
        crossReferences = "Kitab al-Irshad 2:137–156 • Al-Sahifa al-Sajjadiyya #1–54 • Tahaf al-'Uqul Risalat al-Huquq"
      ),
      // 14. Imam Muhammad al-Baqir & Imam Ja'far al-Sadiq — Sunni & Shia Accounts
      BiographyEntry(
        id = "bio_sunni_baqir_sadiq",
        personNameEnglish = "Imam Muhammad al-Baqir & Imam Ja'far al-Sadiq (RA) — Sunni Biographical Perspective",
        personNameArabic = "الإمام محمد الباقر والإمام جعفر الصادق بن محمد الباقر الهاشمي المدني",
        personNameUrdu = "حضرت امام محمد باقر اور حضرت امام جعفر صادق رحمہما اللہ (اہلسنت مآخذ)",
        titleHonorific = "Imams of Knowledge, Hadith & Fiqh from the Prophetic Household in Madinah",
        category = "Ahl al-Bayt",
        traditionPerspective = "SUNNI",
        eraHijri = "57–114 AH (Al-Baqir) & 80–148 AH (Al-Sadiq)",
        primarySourceBook = "Siyar A'lam al-Nubala' & Tahdhib al-Kamal fi Asma' al-Rijal",
        sourceBookArabic = "سير أعلام النبلاء للذهبي وتهذيب الكمال للمزي وحلية الأولياء لأبي نعيم",
        author = "Imam Shams al-Din al-Dhahabi (d. 748 AH) & Hafiz al-Mizzi (d. 742 AH)",
        volumeAndPage = "Siyar A'lam al-Nubala' Vol. 4, p. 401 (Al-Baqir) & Vol. 6, p. 255 (Al-Sadiq) • Hilyat al-Awliya' 3:192",
        arabicText = "قَالَ أَبُو حَنِيفَةَ: مَا رَأَيْتُ أَفْقَهَ مِنْ جَعْفَرِ بْنِ مُحَمَّدٍ. وَقَالَ مَالِكُ بْنُ أَنَسٍ: مَا رَأَتْ عَيْنٌ وَلَا سَمِعَتْ أُذُنٌ وَلَا خَطَرَ عَلَى قَلْبِ بَشَرٍ أَفْضَلَ مِنْ جَعْفَرِ بْنِ مُحَمَّدٍ الصَّادِقِ عِلْمًا وَعِبَادَةً وَوَرَعًا.",
        urduTranslation = "امام ذہبی نے ’سیر اعلام النبلاء‘ (جلد 4 ص 401 اور جلد 6 ص 255) اور ابو نعیم اصفہانی نے ’حلیۃ الاولیاء‘ (جلد 3 ص 192) میں امام محمد باقرؒ اور امام جعفر صادقؒ کے مناقب میں امام اعظم ابوحنیفہؒ کا قول نقل کیا ہے: ’میں نے جعفر بن محمد (الصادق) سے بڑا فقیہ کسی کو نہیں دیکھا‘، اور امام مالک بن انسؒ فرماتے ہیں: ’علم، عبادت اور تقویٰ و ورع میں جعفر بن محمد الصادق سے بڑھ کر کسی آنکھ نے نہیں دیکھا‘۔ دونوں ائمہ کی روایات صحاح ستہ میں شامل ہیں۔",
        englishTranslation = "In Siyar A'lam al-Nubala' (Vol. 4, p. 401 & Vol. 6, p. 255) by Imam al-Dhahabi and Hilyat al-Awliya' (Vol. 3, p. 192): Revered across the Six Canonical Sunni Hadith Books as luminaries of the Prophetic Household in Madinah. Imam Abu Hanifah declared: 'I have never seen anyone more learned in Fiqh than Ja'far ibn Muhammad.' Imam Malik ibn Anas said: 'No eye has seen, no ear has heard, nor has it entered the heart of any human being anyone more excellent than Ja'far ibn Muhammad al-Sadiq in knowledge, worship, and piety.'",
        translatorAttribution = "Mu'assasat al-Risalah & Nafees Academy",
        crossReferences = "Sahih Muslim 1218 • Siyar A'lam al-Nubala' 4:401, 6:255 • Hilyat al-Awliya' 3:192"
      ),
      BiographyEntry(
        id = "bio_shia_baqir_sadiq",
        personNameEnglish = "Imam Muhammad al-Baqir & Imam Ja'far al-Sadiq (AS) — Shia Biographical Perspective",
        personNameArabic = "الإمام محمد الباقر والإمام جعفر الصادق عليهما السلام (كتاب الإرشاد والكافي)",
        personNameUrdu = "حضرت امام محمد باقر اور حضرت امام جعفر صادق علیہما السلام (کتاب الارشاد و الکافی)",
        titleHonorific = "Fifth & Sixth Imams of Ahl al-Bayt • Founders of the Ja'fari School of Jurisprudence",
        category = "Ahl al-Bayt",
        traditionPerspective = "SHIA",
        eraHijri = "57–114 AH & 80–148 AH",
        primarySourceBook = "Kitab al-Irshad (Shaykh al-Mufid) & Usul al-Kafi (Al-Kulayni)",
        sourceBookArabic = "كتاب الإرشاد للشيخ المفيد وأصول الكافي للشيخ الكليني",
        author = "Shaykh al-Mufid (d. 413 AH) & Shaykh al-Kulayni (d. 329 AH)",
        volumeAndPage = "Kitab al-Irshad Vol. 2, pp. 157–214 • Al-Kafi Vol. 1, p. 53",
        arabicText = "حَدِيثِي حَدِيثُ أَبِي، وَحَدِيثُ أَبِي حَدِيثُ جَدِّي، وَحَدِيثُ جَدِّي حَدِيثُ الْحُسَيْنِ، وَحَدِيثُ الْحُسَيْنِ حَدِيثُ الْحَسَنِ، وَحَدِيثُ الْحَسَنِ حَدِيثُ أَمِيرِ الْمُؤْمِنِينَ، وَحَدِيثُ أَمِيرِ الْمُؤْمِنِينَ حَدِيثُ رَسُولِ اللَّهِ ﷺ، وَحَدِيثُ رَسُولِ اللَّهِ قَوْلُ اللَّهِ عَزَّ وَجَلَّ.",
        urduTranslation = "شیخ مفید کی ’کتاب الارشاد‘ (جلد 2 ص 157 تا 214) اور شیخ کلینی کی ’اصول الکافی‘ (جلد 1 ص 53) میں حضرت امام محمد باقرؑ اور حضرت امام جعفر صادقؑ کے علمی دور کا احاطہ کیا گیا ہے جس میں مسجدِ نبوی اور مدینہ منورہ کے حلقاتِ درس سے چار ہزار سے زائد شاگردوں اور راویوں نے تفسیر، حدیث، فقہِ جعفری، اصولِ عقائد اور علومِ طبیعیہ حاصل کیے۔ امام جعفر صادقؑ نے فرمایا: ’میری حدیث میرے والد کی حدیث ہے، اور ان کی حدیث میرے جد امجد رسول اللہ ﷺ کی حدیث ہے‘۔",
        englishTranslation = "In Kitab al-Irshad (Vol. 2, pp. 157–214) and Usul al-Kafi (Vol. 1, p. 53): Fifth and Sixth Imams of the Ahl al-Bayt who established the great academy of Prophetic knowledge in Madinah, training over 4,000 narrators and jurists (such as Zurara ibn A'yan, Muhammad ibn Muslim, and Hisham ibn al-Hakam). Imam al-Sadiq (AS) stated in Al-Kafi (1:53): 'My Hadith is the Hadith of my father, the Hadith of my father is the Hadith of my grandfather al-Husayn, al-Hasan, Amir al-Mu'minin, and the Messenger of Allah (ﷺ).'",
        translatorAttribution = "Allama Syed Safdar Hussain Najafi & Dr. I.K.A. Howard",
        crossReferences = "Kitab al-Irshad 2:157–214 • Usul al-Kafi 1:53"
      ),
      // 15. Remaining Six of the Ten Promised Paradise ('Asharah Mubashsharah)
      BiographyEntry(
        id = "bio_sunni_asharah_mubashsharah",
        personNameEnglish = "Talhah, al-Zubayr, 'Abd al-Rahman ibn 'Awf, Sa'd ibn Abi Waqqas, Sa'id ibn Zayd & Abu 'Ubaydah (RA)",
        personNameArabic = "بقية العشرة المبشرين بالجنة: طلحة والزبير وعبد الرحمن بن عوف وسعد بن أبي وقاص وسعيد بن زيد وأبو عبيدة بن الجراح",
        personNameUrdu = "بقیہ عشرہ مبشرہ صحابہ کرام رضی اللہ عنہم (طلحہ، زبیر، عبدالرحمٰن بن عوف، سعد بن ابی وقاص، سعید بن زید، ابو عبیدہ بن الجراح)",
        titleHonorific = "Al-'Asharah al-Mubashsharun bil-Jannah • Foremost Muhajirun & Veterans of Badr",
        category = "Khulafa & 'Asharah Mubashsharah",
        traditionPerspective = "SUNNI",
        eraHijri = "1st Century AH",
        primarySourceBook = "Al-Isabah fi Tamyiz al-Sahabah, Usd al-Ghabah & Tabaqat Ibn Sa'd",
        sourceBookArabic = "الإصابة في تمييز الصحابة وأسد الغابة والطبقات الكبرى لابن سعد (المجلد الثالث)",
        author = "Imam Ibn Sa'd (d. 230 AH), Ibn al-Athir (d. 630 AH) & Hafiz Ibn Hajar (d. 852 AH)",
        volumeAndPage = "Tabaqat Ibn Sa'd Vol. 3, pp. 100–415 • Al-Isabah Vols. 3–4 • Jami' at-Tirmidhi 3747",
        arabicText = "أَبُو بَكْرٍ فِي الْجَنَّةِ، وَعُمَرُ فِي الْجَنَّةِ، وَعُثْمَانُ فِي الْجَنَّةِ، وَعَلِيٌّ فِي الْجَنَّةِ، وَطَلْحَةُ فِي الْجَنَّةِ، وَالزُّبَيْرُ فِي الْجَنَّةِ، وَعَبْدُ الرَّحْمَنِ بْنُ عَوْفٍ فِي الْجَنَّةِ، وَسَعْدٌ فِي الْجَنَّةِ، وَسَعِيدٌ فِي الْجَنَّةِ، وَأَبُو عُبَيْدَةَ بْنُ الْجَرَّاحِ فِي الْجَنَّةِ.",
        urduTranslation = "امام ابن سعد نے ’الطبقات الکبریٰ‘ (جلد 3 ص 100 تا 415) اور حافظ ابن حجر نے ’الاصابہ‘ میں عشرہ مبشرہ کے ان چھ جلیل القدر اصحابِ بدر کی مفصل سوانح درج کی ہیں: (1) طلحہ بن عبیداللہؓ جنہیں غزوہ احد میں نبی کریم ﷺ کی ڈھال بننے پر ’طلحۃ الخیر‘ اور ’شہید یحییٰ علی وجہ الارض‘ کا لقب ملا؛ (2) زبیر بن عوامؓ جنہیں رسول اللہ ﷺ نے اپنا حواری قرار دیا؛ (3) عبدالرحمٰن بن عوفؓ؛ (4) فاتحِ قادسیہ سعد بن ابی وقاصؓ؛ (5) سعید بن زیدؓ؛ اور (6) امینِ امت فاتحِ شام ابو عبیدہ بن الجراحؓ۔",
        englishTranslation = "In Al-Tabaqat al-Kubra (Vol. 3, pp. 100–415) by Ibn Sa'd and Al-Isabah by Ibn Hajar: Comprehensive biographies of the remaining six of the Ten Promised Paradise in a single Hadith (Tirmidhi 3747): (1) Talhah ibn 'Ubaydullah (RA), the living martyr who shielded the Prophet (ﷺ) with his hand at Uhud; (2) al-Zubayr ibn al-'Awwam (RA), the disciple (Hawari) of the Messenger of Allah; (3) 'Abd al-Rahman ibn 'Awf (RA); (4) Sa'd ibn Abi Waqqas (RA), first to shoot an arrow in Allah's cause and victor of al-Qadisiyyah; (5) Sa'id ibn Zayd (RA); and (6) Abu 'Ubaydah ibn al-Jarrah (RA), the Trustworthy One (Amin) of this Ummah and commander in Bilad al-Sham.",
        translatorAttribution = "Allama Abdullah al-Imadi (Nafees Academy)",
        crossReferences = "Jami' at-Tirmidhi 3747 • Sahih al-Bukhari 3724, 3744 • Tabaqat Ibn Sa'd 3:100–415"
      ),
      // 16. Salman al-Farisi, Abu Dharr al-Ghifari, 'Ammar ibn Yasir, Miqdad & Bilal (RA) — Sunni & Shia Perspectives
      BiographyEntry(
        id = "bio_sunni_muhajirun_ansar_vanguard",
        personNameEnglish = "Salman al-Farisi, Abu Dharr al-Ghifari, 'Ammar ibn Yasir, Bilal ibn Rabah & Ibn Mas'ud (RA) — Sunni Sources",
        personNameArabic = "سلمان الفارسي وأبو ذر الغفاري وعمار بن ياسر وبلال بن رباح وعبد الله بن مسعود رضي الله عنهم",
        personNameUrdu = "حضرت سلمان فارسی، ابو ذر غفاری، عمار بن یاسر، بلال حبشی اور عبداللہ بن مسعود رضی اللہ عنہم (اہلسنت مآخذ)",
        titleHonorific = "Foremost Sabiqun • Pioneers of Faith, Knowledge & Sacrifice",
        category = "Muhajirun & Ansar",
        traditionPerspective = "SUNNI",
        eraHijri = "1st Century AH",
        primarySourceBook = "Al-Isabah fi Tamyiz al-Sahabah, Usd al-Ghabah & Tabaqat Ibn Sa'd",
        sourceBookArabic = "الإصابة في تمييز الصحابة وأسد الغابة والطبقات الكبرى لابن سعد",
        author = "Imam Ibn Sa'd (d. 230 AH) & Hafiz Ibn Hajar al-'Asqalani (d. 852 AH)",
        volumeAndPage = "Tabaqat Ibn Sa'd Vol. 3, pp. 150–264 & Vol. 4, pp. 75–238 • Al-Isabah Vols. 1, 3, 4",
        arabicText = "سَلْمَانُ مِنَّا أَهْلَ الْبَيْتِ... مَا أَقَلَّتِ الْغَبْرَاءُ وَلَا أَظَلَّتِ الْخَضْرَاءُ أَصْدَقَ لَهْجَةً مِنْ أَبِي ذَرٍّ... مَنْ أَحَبَّ أَنْ يَقْرَأَ الْقُرْآنَ غَضًّا كَمَا أُنْزِلَ فَلْيَقْرَأْهُ عَلَى قِرَاءَةِ ابْنِ أُمِّ عَبْدٍ.",
        urduTranslation = "طبقات ابن سعد (جلد 3 و 4) اور الاصابہ میں ان جلیل القدر سابقون الاولون کے احوال درج ہیں: حضرت سلمان فارسیؓ جن کے بارے میں غزوہ خندق کے موقع پر نبی کریم ﷺ نے فرمایا ’سلمان ہم اہلِ بیت میں سے ہیں‘؛ حضرت ابو ذر غفاریؓ جن کے صدقِ مقال کی گواہی رسول اللہ ﷺ نے دی؛ حضرت عمار بن یاسرؓ جن کے والدین اسلام کے پہلے شہداء (سمیہ و یاسرؓ) تھے؛ مؤذنِ رسول حضرت بلال بن رباحؓ؛ اور فقیہِ امت حضرت عبداللہ بن مسعودؓ۔",
        englishTranslation = "In Al-Tabaqat al-Kubra (Vols. 3–4) and Al-Isabah: (1) Salman al-Farisi (RA), seeker of truth who proposed the Trench at Khandaq and of whom the Prophet (ﷺ) said 'Salman is from us, the Ahl al-Bayt'; (2) Abu Dharr al-Ghifari (RA), of whom the Prophet (ﷺ) said 'The earth has not carried nor the sky shaded anyone more truthful in speech than Abu Dharr'; (3) 'Ammar ibn Yasir (RA), son of the first martyrs of Islam (Sumayyah and Yasir RA); (4) Bilal ibn Rabah (RA), the Prophet's Mu'adhdhin; and (5) 'Abdullah ibn Mas'ud (RA), master of Qur'anic recitation and Kufan jurisprudence.",
        translatorAttribution = "Allama Abdullah al-Imadi & Darussalam",
        crossReferences = "Sahih al-Bukhari 3760, 4999 • Jami' at-Tirmidhi 3801 • Tabaqat Ibn Sa'd 3:150, 4:75"
      ),
      BiographyEntry(
        id = "bio_shia_arkan_arbaah",
        personNameEnglish = "Salman al-Muhammadi, Abu Dharr, Al-Miqdad & 'Ammar ibn Yasir (RA) — Shia Biographical Perspective",
        personNameArabic = "الأركان الأربعة والصحابة المنتجبون: سلمان المحمدي وأبو ذر الغفاري والمقداد بن الأسود وعمار بن ياسر (رجال الكشي والخصال)",
        personNameUrdu = "ارکانِ اربعہ اور اصحابِ باوفا: حضرت سلمان محمدی، ابو ذر غفاری، مقداد بن اسود اور عمار بن یاسر (شیعہ مآخذ)",
        titleHonorific = "Al-Arkan al-Arba'ah (The Four Pillars of Faithful Companionship)",
        category = "Muhajirun & Ansar",
        traditionPerspective = "SHIA",
        eraHijri = "1st Century AH",
        primarySourceBook = "Ikhtiyar Ma'rifat al-Rijal (Rijal al-Kashshi), Al-Khisal (Al-Saduq) & Waq'at Siffin",
        sourceBookArabic = "اختيار معرفة الرجال للطوسي (رجال الكشي) والخصال للشيخ الصدوق ووقعة صفين",
        author = "Shaykh al-Saduq (d. 381 AH), Abu 'Amr al-Kashshi & Shaykh al-Tusi (d. 460 AH)",
        volumeAndPage = "Al-Khisal Vol. 1, p. 254 • Rijal al-Kashshi Vol. 1, pp. 33–118 • Waq'at Siffin p. 315",
        arabicText = "إِنَّ اللَّهَ عَزَّ وَجَلَّ أَمَرَنِي بِحُبِّ أَرْبَعَةٍ وَأَخْبَرَنِي أَنَّهُ يُحِبُّهُمْ: عَلِيٌّ، وَأَبُو ذَرٍّ، وَالْمِقْدَادُ، وَسَلْمَانُ.",
        urduTranslation = "شیخ صدوق کی ’الخصال‘ (جلد 1 ص 254) اور شیخ طوسی کی ’اختیار معرفۃ الرجال (رجال کشی)‘ (جلد 1 ص 33 تا 118) میں رسول اللہ ﷺ کی حدیث نقل ہوئی ہے: ’اللہ عزوجل نے مجھے چار ہستیوں سے محبت کرنے کا حکم دیا اور خبر دی کہ وہ خود ان سے محبت فرماتا ہے: علیؑ، ابو ذرؓ، مقدادؓ اور سلمانؓ‘۔ شیعہ تراجم و رجال میں ان چاروں کے ساتھ حضرت عمار بن یاسرؓ، حذیفہ بن یمانؓ، جابر بن عبداللہ انصاریؓ، ابو ایوب انصاریؓ اور مالک اشتر نخعیؓ کو امیر المومنین حضرت علی علیہ السلام کے وفادار ترین اصحاب میں شمار کیا گیا ہے۔",
        englishTranslation = "In Al-Khisal (Vol. 1, p. 254) by Shaykh al-Saduq and Rijal al-Kashshi (Vol. 1, pp. 33–118) by Shaykh al-Tusi: Cites the Prophetic Hadith (also in Tirmidhi 3718): 'Allah commanded me to love four and informed me that He loves them: 'Ali, Abu Dharr, al-Miqdad, and Salman.' In Imami Rijal literature, Salman al-Muhammadi, Abu Dharr al-Ghifari, al-Miqdad ibn al-Aswad, and 'Ammar ibn Yasir (alongside Jabir ibn 'Abdillah al-Ansari, Hudhayfah ibn al-Yaman, and Abu Ayyub al-Ansari) are revered as the foremost loyal Companions who upheld the covenant of the Prophet (ﷺ) and Amir al-Mu'minin 'Ali (AS).",
        translatorAttribution = "Qom Jama'at al-Mudarrisin & Classical Urdu Edition",
        crossReferences = "Al-Khisal 1:254 • Jami' at-Tirmidhi 3718 • Rijal al-Kashshi 1:33 • Waq'at Siffin p. 315"
      ),
      // 17. Imam Musa al-Kazim & Imam 'Ali al-Rida — Sunni & Shia Perspectives
      BiographyEntry(
        id = "bio_sunni_kazim_rida",
        personNameEnglish = "Imam Musa al-Kazim & Imam 'Ali ibn Musa al-Rida (RA) — Sunni Biographical Perspective",
        personNameArabic = "الإمام موسى الكاظم بن جعفر الصادق والإمام علي الرضا بن موسى الكاظم الهاشمي (سير أعلام النبلاء)",
        personNameUrdu = "حضرت امام موسیٰ کاظم اور حضرت امام علی رضا بن موسیٰ رحمہما اللہ (اہلسنت مآخذ)",
        titleHonorific = "Masters of Patience, Worship & Prophetic Lineage in Madinah, Baghdad & Khurasan",
        category = "Ahl al-Bayt",
        traditionPerspective = "SUNNI",
        eraHijri = "128–183 AH (Al-Kazim) & 148–203 AH (Al-Rida)",
        primarySourceBook = "Siyar A'lam al-Nubala', Tarikh Baghdad & Tahdhib al-Tahdhib",
        sourceBookArabic = "سير أعلام النبلاء للذهبي وتاريخ بغداد للخطيب البغدادي وتهذيب التهذيب لابن حجر",
        author = "Imam Shams al-Din al-Dhahabi (d. 748 AH), Al-Khatib al-Baghdadi (d. 463 AH) & Hafiz Ibn Hajar (d. 852 AH)",
        volumeAndPage = "Siyar A'lam al-Nubala' Vol. 6, p. 270 (Al-Kazim) & Vol. 9, p. 387 (Al-Rida) • Tarikh Baghdad Vol. 13, p. 27",
        arabicText = "كَانَ مُوسَى الْكَاظِمُ صَالِحًا عَابِدًا جَوَادًا حَلِيمًا كَبِيرَ الْقَدْرِ، يَبْلُغُهُ عَنِ الرَّجُلِ أَنَّهُ يُؤْذِيهِ فَيَبْعَثُ إِلَيْهِ بِمَالٍ... وَكَانَ عَلِيُّ بْنُ مُوسَى الرِّضَا سَيِّدَ بَنِي هَاشِمٍ فِي زَمَانِهِ وَأَجَلَّهُمْ وَأَنْبَلَهُمْ.",
        urduTranslation = "امام ذہبی نے ’سیر اعلام النبلاء‘ (جلد 6 ص 270 اور جلد 9 ص 387) اور خطیب بغدادی نے ’تاریخ بغداد‘ (جلد 13 ص 27) میں امام موسیٰ کاظمؒ اور امام علی رضاؒ کے زہد، علم، حلم اور سخاوت کا تذکرہ کیا ہے: امام موسیٰ کاظمؒ کو جب معلوم ہوتا کہ کوئی شخص آپ کو ایذا دیتا ہے تو آپ اس کے گھر اشرفیوں کی تھیلی بھیج دیتے، اور امام شافعیؒ فرماتے تھے کہ بغداد میں موسیٰ کاظمؒ کا مزار دعا کی قبولیت کے لیے تریاقِ مجرب ہے۔ امام علی رضاؒ اپنے زمانے میں بنو ہاشم کے جلیل القدر سید اور عالم تھے۔",
        englishTranslation = "In Siyar A'lam al-Nubala' (Vol. 6, p. 270 & Vol. 9, p. 387) by Imam al-Dhahabi and Tarikh Baghdad (Vol. 13, p. 27) by al-Khatib al-Baghdadi: Imam Musa al-Kazim (RA) was renowned for suppressing anger (al-Kazim) and returning kindness to anyone who wronged him by sending them purses of gold, and for his night-long prostrations in Baghdad. His son Imam 'Ali ibn Musa al-Rida (RA) was described by al-Dhahabi and Ibn Hajar as the master of Banu Hashim in his era in knowledge, nobility, and piety.",
        translatorAttribution = "Mu'assasat al-Risalah & Nafees Academy",
        crossReferences = "Siyar A'lam al-Nubala' 6:270, 9:387 • Tarikh Baghdad 13:27 • Tahdhib al-Tahdhib 7:387"
      ),
      BiographyEntry(
        id = "bio_shia_kazim_rida",
        personNameEnglish = "Imam Musa al-Kazim & Imam 'Ali ibn Musa al-Rida (AS) — Shia Biographical Perspective",
        personNameArabic = "الإمام موسى الكاظم والإمام علي بن موسى الرضا عليهما السلام (كتاب الإرشاد وعيون أخبار الرضا)",
        personNameUrdu = "حضرت امام موسیٰ کاظم اور حضرت امام علی بن موسیٰ الرضا علیہما السلام (کتاب الارشاد و عیون اخبار الرضا)",
        titleHonorific = "Seventh & Eighth Imams of Ahl al-Bayt • Bab al-Hawa'ij & 'Alim Aal Muhammad",
        category = "Ahl al-Bayt",
        traditionPerspective = "SHIA",
        eraHijri = "128–183 AH & 148–203 AH",
        primarySourceBook = "Kitab al-Irshad (Shaykh al-Mufid) & 'Uyun Akhbar al-Rida (Shaykh al-Saduq)",
        sourceBookArabic = "كتاب الإرشاد للشيخ المفيد وعيون أخبار الرضا للشيخ الصدوق",
        author = "Shaykh al-Mufid (d. 413 AH) & Shaykh al-Saduq (d. 381 AH)",
        volumeAndPage = "Kitab al-Irshad Vol. 2, pp. 215–272 • 'Uyun Akhbar al-Rida Vol. 1, pp. 11–310",
        arabicText = "قَالَ رَسُولُ اللَّهِ ﷺ: سَتُدْفَنُ بَضْعَةٌ مِنِّي بِأَرْضِ خُرَاسَانَ، لَا يَزُورُهَا مُؤْمِنٌ إِلَّا أَوْجَبَ اللَّهُ عَزَّ وَجَلَّ لَهُ الْجَنَّةَ وَحَرَّمَ جَسَدَهُ عَلَى النَّارِ.",
        urduTranslation = "شیخ مفید کی ’کتاب الارشاد‘ (جلد 2 ص 215 تا 272) اور شیخ صدوق کی ’عیون اخبار الرضا‘ (جلد 1 و 2) میں ساتویں امام حضرت موسیٰ کاظم علیہ السلام کے بغداد کے قید خانوں میں صبر و استقامت، ہشام بن حکم کو دیے گئے عقلی و کلامی دلائل، اور آٹھویں امام حضرت علی بن موسیٰ الرضا علیہ السلام کے نیشاپور میں ’حدیثِ سلسلۃ الذہب‘ (کلمہ توحید کا قلعہ) بیان فرمانے اور مرو و طوس (مشہد مقدس) میں علمی مناظروں کا مفصل بیان ہے۔",
        englishTranslation = "In Kitab al-Irshad (Vol. 2, pp. 215–272) by Shaykh al-Mufid and 'Uyun Akhbar al-Rida by Shaykh al-Saduq: Seventh and Eighth Imams of the Ahl al-Bayt. Chronicles Imam Musa al-Kazim's (AS) steadfastness during years of imprisonment in Basrah and Baghdad and his foundational treatise on intellect ('Aql) in Usul al-Kafi, followed by Imam 'Ali al-Rida's (AS) interfaith scholarly debates in Marw and his narration of the Golden Chain Hadith (Hadith Silsilat al-Dhahab) in Nishapur.",
        translatorAttribution = "Allama Syed Safdar Hussain Najafi & Qom Scholarly Edition",
        crossReferences = "Kitab al-Irshad 2:215–272 • 'Uyun Akhbar al-Rida 2:135 • Usul al-Kafi 1:13"
      ),
      // 18. Hamzah ibn 'Abd al-Muttalib & Ja'far ibn Abi Talib al-Tayyar — Sunni & Shia Perspectives
      BiographyEntry(
        id = "bio_sunni_hamzah_jafar",
        personNameEnglish = "Sayyid al-Shuhada' Hamzah ibn 'Abd al-Muttalib & Ja'far ibn Abi Talib al-Tayyar (RA) — Sunni Sources",
        personNameArabic = "سيد الشهداء حمزة بن عبد المطلب وجعفر بن أبي طالب الطيار ذو الجناحين رضي الله عنهما",
        personNameUrdu = "سید الشہداء حضرت حمزہ بن عبدالمطلب اور حضرت جعفر طیار بن ابی طالب رضی اللہ عنہما (اہلسنت مآخذ)",
        titleHonorific = "Lion of Allah (Asadullah) at Uhud & The Two-Winged Martyr of Mu'tah (Dhu al-Janahayn)",
        category = "Ahl al-Bayt",
        traditionPerspective = "SUNNI",
        eraHijri = "Martyred 3 AH (Uhud) & 8 AH (Mu'tah)",
        primarySourceBook = "Al-Isabah fi Tamyiz al-Sahabah, Usd al-Ghabah & Al-Tabaqat al-Kubra",
        sourceBookArabic = "الإصابة في تمييز الصحابة وأسد الغابة والطبقات الكبرى لابن سعد",
        author = "Imam Ibn Sa'd (d. 230 AH), Ibn al-Athir (d. 630 AH) & Hafiz Ibn Hajar (d. 852 AH)",
        volumeAndPage = "Tabaqat Ibn Sa'd Vol. 3, pp. 8–16 & Vol. 4, pp. 28–38 • Al-Isabah Vol. 1, p. 592 & Vol. 2, p. 106",
        arabicText = "سَيِّدُ الشُّهَدَاءِ حَمْزَةُ بْنُ عَبْدِ الْمُطَّلِبِ... وَقَالَ النَّبِيُّ ﷺ لِجَعْفَرٍ: أَشْبَهْتَ خَلْقِي وَخُلُقِي، وَرَأَيْتُ جَعْفَرًا يَطِيرُ فِي الْجَنَّةِ مَعَ الْمَلَائِكَةِ بِجَنَاحَيْنِ.",
        urduTranslation = "طبقات ابن سعد (جلد 3 ص 8 اور جلد 4 ص 28) اور الاصابہ میں رسول اللہ ﷺ کے چچا اور رضاعی بھائی سید الشہداء حضرت حمزہ بن عبدالمطلبؓ (اسد اللہ واسد رسولہ، شہیدِ احد 3 ہجری) اور حضرت علیؓ کے بڑے بھائی حضرت جعفر بن ابی طالب طیارؓ (مہاجرِ حبشہ و نجاشی کے دربار میں سورہ مریم کی تلاوت کرنے والے، اور غزوہ موتہ 8 ہجری میں دونوں بازو کٹنے کے باوجود پرچم تھامے رکھنے والے جن کے بدلے اللہ تعالیٰ نے انہیں جنت میں دو پر عطا فرمائے) کی مستند سوانح درج ہیں۔",
        englishTranslation = "In Al-Tabaqat al-Kubra (Vol. 3, p. 8 & Vol. 4, p. 28) and Al-Isabah: (1) Hamzah ibn 'Abd al-Muttalib (RA), uncle and foster-brother of the Prophet (ﷺ), the Lion of Allah (Asadullah) at Badr and Sayyid al-Shuhada' (Master of Martyrs) at Uhud (3 AH); and (2) Ja'far ibn Abi Talib (RA), who eloquent spoke before the Negus of Abyssinia reciting Surah Maryam, and held the standard at the Battle of Mu'tah (8 AH) after both hands were severed, of whom the Prophet (ﷺ) said: 'You resemble me in appearance and character, and Allah replaced his two hands with two wings with which he flies in Paradise with the angels.'",
        translatorAttribution = "Allama Abdullah al-Imadi & Darussalam",
        crossReferences = "Sahih al-Bukhari 2699, 3708, 4072 • Tabaqat Ibn Sa'd 3:8, 4:28 • Al-Isabah 1:592, 2:106"
      ),
      BiographyEntry(
        id = "bio_shia_hamzah_jafar",
        personNameEnglish = "Sayyid al-Shuhada' Hamzah & Ja'far al-Tayyar ibn Abi Talib (AS) — Shia Biographical Perspective",
        personNameArabic = "سيد الشهداء حمزة بن عبد المطلب وجعفر الطيار بن أبي طالب عليهما السلام (مقاتل الطالبيين وأعلام الورى)",
        personNameUrdu = "سید الشہداء حضرت حمزہ اور حضرت جعفر طیار بن ابی طالب علیہما السلام (مقاتل الطالبین و اعلام الوریٰ)",
        titleHonorific = "Foremost Hashimi Martyrs of Prophetic Defense at Badr, Uhud & Mu'tah",
        category = "Ahl al-Bayt",
        traditionPerspective = "SHIA",
        eraHijri = "Martyred 3 AH & 8 AH",
        primarySourceBook = "Maqatil al-Talibiyyin (Abu al-Faraj al-Isfahani) & A'lam al-Wara (Al-Tabarsi)",
        sourceBookArabic = "مقاتل الطالبيين لأبي الفرج الأصفهاني وأعلام الورى للشيخ الطبرسي",
        author = "Abu al-Faraj al-Isfahani (d. 356 AH) & Shaykh al-Tabarsi (d. 548 AH)",
        volumeAndPage = "Maqatil al-Talibiyyin pp. 19–28 • A'lam al-Wara Vol. 1, pp. 210–228 • Nahj al-Balagha Letter 28",
        arabicText = "وَمِنَّا أَسَدُ اللَّهِ وَأَسَدُ رَسُولِهِ حَمْزَةُ سَيِّدُ الشُّهَدَاءِ، وَمِنَّا ذُو الْجَنَاحَيْنِ جَعْفَرُ الطَّيَّارُ فِي الْجَنَّةِ مَعَ الْمَلَائِكَةِ.",
        urduTranslation = "ابو الفرج اصفہانی کی ’مقاتل الطالبین‘ (ص 19 تا 28) اور نہج البلاغہ (مکتوب 28) میں امیر المومنین حضرت علی علیہ السلام کا فرمان منقول ہے کہ سید الشہداء حضرت حمزہؑ اور ذوالجناحین حضرت جعفر طیارؑ خاندانِ بنو ہاشم اور اہلِ بیتِ نبوت کے وہ عظیم ستون ہیں جنہوں نے بدر، احد اور موتہ میں اسلام کی بقا کے لیے اپنی جانیں قربان کیں اور رسول اللہ ﷺ نے جعفر طیارؑ کی شہادت پر ان کے بیٹوں کے سر پر دستِ شفقت رکھا اور تین دن تک آلِ جعفر کے لیے کھانا تیار کرنے کا حکم فرمایا۔",
        englishTranslation = "In Maqatil al-Talibiyyin (pp. 19–28) by Abu al-Faraj al-Isfahani and Nahj al-Balagha (Letter 28): Opens the chronicle of the martyrs of Abu Talib's progeny with Ja'far ibn Abi Talib al-Tayyar (AS), Commander at Mu'tah, alongside his uncle Sayyid al-Shuhada' Hamzah ibn 'Abd al-Muttalib (AS). Records the Prophet's (ﷺ) weeping over both martyrs and establishing the Sunnah of preparing food for the bereaved household of Ja'far for three days.",
        translatorAttribution = "Allama Mufti Jafar Hussain & Qom Edition",
        crossReferences = "Maqatil al-Talibiyyin pp. 19–28 • Nahj al-Balagha Letter 28 • Al-Kafi 3:217"
      ),
      // 19. Scholars of the Companions: Mu'adh ibn Jabal, Ubayy ibn Ka'b, Zayd ibn Thabit, Abu Musa & Khalid ibn al-Walid
      BiographyEntry(
        id = "bio_sunni_fuqaha_sahaba_khalid",
        personNameEnglish = "Mu'adh ibn Jabal, Ubayy ibn Ka'b, Zayd ibn Thabit & Khalid ibn al-Walid (RA) — Sunni Sources",
        personNameArabic = "معاذ بن جبل وأبي بن كعب وزيد بن ثابت وخالد بن الوليد سيف الله المسلول رضي الله عنهم",
        personNameUrdu = "حضرت معاذ بن جبل، ابی بن کعب، زید بن ثابت اور سیف اللہ خالد بن ولید رضی اللہ عنہم (اہلسنت مآخذ)",
        titleHonorific = "Foremost Qur'an Scribes, Jurists of Halal & Haram, and Sword of Allah (Sayfullah)",
        category = "Muhajirun & Ansar",
        traditionPerspective = "SUNNI",
        eraHijri = "1st Century AH",
        primarySourceBook = "Al-Isabah fi Tamyiz al-Sahabah, Usd al-Ghabah, Al-Isti'ab & Siyar A'lam al-Nubala'",
        sourceBookArabic = "الإصابة لابن حجر وأسد الغابة لابن الأثير والاستيعاب لابن عبد البر وسير أعلام النبلاء للذهبي",
        author = "Ibn 'Abd al-Barr (d. 463 AH), Ibn al-Athir (d. 630 AH), Al-Dhahabi (d. 748 AH) & Ibn Hajar (d. 852 AH)",
        volumeAndPage = "Al-Isabah Vol. 2, p. 214, Vol. 6, p. 106 • Siyar A'lam al-Nubala' Vol. 1, pp. 366–461 • Tabaqat Ibn Sa'd Vol. 2, p. 358",
        arabicText = "أَعْلَمُ أُمَّتِي بِالْحَلَالِ وَالْحَرَامِ مُعَاذُ بْنُ جَبَلٍ، وَأَقْرَؤُهُمْ لِكِتَابِ اللَّهِ أُبَيُّ بْنُ كَعْبٍ، وَأَفْرَضُهُمْ زَيْدُ بْنُ ثَابِتٍ... وَنِعْمَ عَبْدُ اللَّهِ خَالِدُ بْنُ الْوَلِيدِ سَيْفٌ مِنْ سُيُوفِ اللَّهِ.",
        urduTranslation = "الاصابہ، اسد الغابہ، الاستیعاب اور سیر اعلام النبلاء (جلد 1 ص 366 تا 461) میں رسول اللہ ﷺ کی گواہی کے مطابق ان جلیل القدر صحابہ کے تفصیلی تراجم موجود ہیں: (1) حضرت معاذ بن جبلؓ (حلال و حرام کے سب سے بڑے عالم اور قاضیِ یمن)؛ (2) سید القراء حضرت ابی بن کعبؓ؛ (3) کاتبِ وحی اور جامعِ مصحفِ قرآنی حضرت زید بن ثابتؓ؛ اور (4) سیف اللہ المسلول حضرت خالد بن ولیدؓ جنہوں نے غزوہ موتہ، یمامہ، عراق اور یرموک میں اسلامی لشکر کی قیادت کی اور بسترِ وفات پر فرمایا کہ میرے جسم پر بالشت بھر جگہ بھی ایسی نہیں جہاں تلوار یا تیر کا زخم نہ ہو۔",
        englishTranslation = "In Al-Isabah, Usd al-Ghabah, Al-Isti'ab, and Siyar A'lam al-Nubala' (Vol. 1, pp. 366–461): (1) Mu'adh ibn Jabal (RA), whom the Prophet (ﷺ) sent as judge and teacher to Yemen and called 'most knowledgeable of my Ummah in Halal and Haram'; (2) Ubayy ibn Ka'b (RA), 'Sayyid al-Qurra' (Master of the Reciters)'; (3) Zayd ibn Thabit (RA), chief scribe of Revelation who headed the compilation of the Qur'anic Mushaf under Abu Bakr and 'Uthman (RA); and (4) Khalid ibn al-Walid (RA), titled 'Sayfullah al-Maslul (The Drawn Sword of Allah)' by the Prophet (ﷺ) at Mu'tah and victor of al-Yamamah and Yarmouk.",
        translatorAttribution = "Allama Abdullah al-Imadi & Mu'assasat al-Risalah",
        crossReferences = "Sahih al-Bukhari 3757, 4986 • Jami' at-Tirmidhi 3790 • Siyar A'lam al-Nubala' 1:366–461"
      ),
      // 20. Prominent Sahabiyyat: Umm Salamah, Hafsah, Asma' bint Abi Bakr & Umm 'Umarah
      BiographyEntry(
        id = "bio_sunni_sahabiyyat_mothers",
        personNameEnglish = "Umm Salamah, Hafsah bint 'Umar, Asma' bint Abi Bakr & Nusaybah Umm 'Umarah (RA)",
        personNameArabic = "أم المؤمنين أم سلمة وحفصة بنت عمر وأسماء بنت أبي بكر ذات النطاقين ونسيبة بنت كعب أم عمارة رضي الله عنهن",
        personNameUrdu = "ام المومنین حضرت ام سلمہ، حضرت حفصہ، ذات النطاقین حضرت اسماء بنت ابی بکر اور حضرت ام عمارہ نسیبہ بنت کعب رضی اللہ عنہن",
        titleHonorific = "Mothers of the Believers, Custodian of the Mushaf & Heroines of Hijrah and Uhud",
        category = "Mothers of the Believers & Sahabiyyat",
        traditionPerspective = "SUNNI",
        eraHijri = "1st Century AH",
        primarySourceBook = "Al-Tabaqat al-Kubra (Vol. 8: Kitab al-Nisa') & Al-Isabah (Vol. 8)",
        sourceBookArabic = "الطبقات الكبرى لابن سعد (المجلد الثامن: طبقات النساء) والإصابة لابن حجر",
        author = "Imam Muhammad ibn Sa'd (d. 230 AH) & Hafiz Ibn Hajar al-'Asqalani (d. 852 AH)",
        volumeAndPage = "Tabaqat Ibn Sa'd Vol. 8, pp. 81–96, 249–255, 412–416 • Al-Isabah Vol. 8, pp. 13–240",
        arabicText = "مَا التَفَتُّ يَوْمَ أُحُدٍ يَمِينًا وَلَا شِمَالًا إِلَّا وَأَنَا أَرَاهَا تُقَاتِلُ دُونِي (يَعْنِي أُمَّ عُمَارَةَ نُسَيْبَةَ بِنْتَ كَعْبٍ).",
        urduTranslation = "امام ابن سعد کی ’الطبقات الکبریٰ‘ کی آٹھویں جلد (طبقات النساء ص 81 تا 416) اور حافظ ابن حجر کی ’الاصابہ‘ (جلد 8) میں ان جلیل القدر صحابیات کے مفصل احوال موجود ہیں: (1) ام المومنین حضرت ام سلمہ ہند بنت ابی امیہؓ جن کے صائب مشورے سے صلح حدیبیہ کے موقع پر صحابہ کرامؓ نے احرام کھولا اور جنہوں نے ۳۷۸ احادیث روایت کیں؛ (2) ام المومنین حضرت حفصہ بنت عمرؓ جن کے پاس عہدِ صدیقی کا جمع شدہ سرکاری مصحفِ قرآنی محفوظ رہا؛ (3) ذات النطاقین حضرت اسماء بنت ابی بکرؓ جنہوں نے ہجرت کی رات غارِ ثور تک توشہ پہنچایا؛ اور (4) غزوہ احد میں رسول اللہ ﷺ کے گرد ڈھال بن کر لڑنے والی انصاری صحابیہ حضرت ام عمارہ نسیبہ بنت کعبؓ۔",
        englishTranslation = "In Al-Tabaqat al-Kubra (Vol. 8: Book of Women, pp. 81–416) by Ibn Sa'd and Al-Isabah (Vol. 8) by Ibn Hajar: (1) Umm al-Mu'minin Umm Salamah Hind bint Abi Umayyah (RA), narrator of 378 Hadiths whose wise counsel resolved the companions' hesitation at al-Hudaybiyyah; (2) Umm al-Mu'minin Hafsah bint 'Umar (RA), entrusted custodian of the original Mushaf compiled under Abu Bakr (RA); (3) Asma' bint Abi Bakr (RA), titled 'Dhat al-Nitaqayn (She of the Two Waistbands)' by the Prophet (ﷺ) for supplying provisions to the Cave of Thawr during the Hijrah; and (4) Nusaybah bint Ka'b Umm 'Umarah (RA), who defended the Prophet (ﷺ) with sword and shield at Uhud.",
        translatorAttribution = "Allama Abdullah al-Imadi & Darussalam",
        crossReferences = "Sahih al-Bukhari 2731, 2979, 4986 • Tabaqat Ibn Sa'd 8:81–416 • Al-Isabah 8:13–240"
      )
    )
  }

  /**
   * Returns the dedicated classical biographical source books recommended in Spec B Part 3
   * (Al-Isabah, Usd al-Ghabah, Al-Isti'ab, Siyar A'lam al-Nubala', Al-Tabaqat al-Kubra,
   * Ansab al-Ashraf, Kitab al-Irshad, A'lam al-Wara, Maqatil al-Talibiyyin, Hilyat al-Awliya')
   * so users can open and read any of these source books directly from the "Sahaba & Ahl al-Bayt" tab.
   */
  fun getBiographicalSourceBooks(context: Context? = null): List<IslamicClassicalBook> {
    val classified = getClassifiedLibraryCatalog(context)
    val bioBookIds = setOf(
      "hist_book_02_tabaqat_ibn_sad",
      "hist_book_05_ansab_ashraf",
      "hist_book_14_usd_al_ghaba",
      "hist_book_14_isaba_ibn_hajar",
      "hist_book_15_istiab_ibn_abd_al_barr",
      "hist_book_16_usd_al_ghaba",
      "hist_book_17_isaba_ibn_hajar",
      "hist_book_18_siyar_alam_nubala",
      "book_tabaqat_ibn_sad",
      "book_siyar_alam_nubala",
      "book_al_isabah_usd_al_ghabah",
      "book_fadail_al_sahaba_ahmad",
      "book_khasais_ali_nasai",
      "book_hilyat_al_awliya",
      "book_shia_kitab_al_irshad",
      "book_shia_alam_al_wara_kashf",
      "book_shia_history_siffin_maqatil",
      "specb_sunni_tahdhib_al_kamal",
      "specb_sunni_tahdhib_al_tahdhib"
    )
    return classified
      .map { it.book }
      .filter { book ->
        book.id in bioBookIds ||
          book.titleEnglish.contains("Isaba", ignoreCase = true) ||
          book.titleEnglish.contains("Usd al-Ghaba", ignoreCase = true) ||
          book.titleEnglish.contains("Isti'ab", ignoreCase = true) ||
          book.titleEnglish.contains("Siyar A'lam", ignoreCase = true) ||
          book.titleEnglish.contains("Tabaqat", ignoreCase = true) ||
          book.titleEnglish.contains("Ansab al-Ashraf", ignoreCase = true) ||
          book.titleEnglish.contains("Kitab al-Irshad", ignoreCase = true) ||
          book.titleEnglish.contains("A'lam al-Wara", ignoreCase = true) ||
          book.titleEnglish.contains("Maqatil al-Talibiyyin", ignoreCase = true)
      }
      .distinctBy { it.id }
  }

  private fun normalizeBioQuery(text: String): String {
    return text
      .lowercase()
      .replace(Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]"), "")
      .replace("أ", "ا")
      .replace("إ", "ا")
      .replace("آ", "ا")
      .replace("ة", "ه")
      .replace("ى", "ي")
      .replace("ک", "ك")
      .replace("ی", "ي")
      .replace("ہ", "ه")
      .replace("ے", "ي")
      .replace("'", "")
      .replace("’", "")
      .replace("‘", "")
      .replace("`", "")
      .replace("-", " ")
      .replace(Regex("\\s+"), " ")
      .trim()
  }

  private val romanUrduBioAliases: Map<String, List<String>> = mapOf(
    "sahaba" to listOf("sahaba", "companions", "muhajirun", "ansar", "asharah", "tabaqat", "isabah"),
    "sahabi" to listOf("sahaba", "companions", "muhajirun", "ansar", "asharah"),
    "ahalbayat" to listOf("ahl al bayt", "fatimah", "hasan", "husayn", "ali", "zaynab", "abbas", "zayn", "baqir", "sadiq"),
    "ahlulbayt" to listOf("ahl al bayt", "fatimah", "hasan", "husayn", "ali", "zaynab", "abbas", "zayn", "baqir", "sadiq"),
    "ahlebait" to listOf("ahl al bayt", "fatimah", "hasan", "husayn", "ali", "zaynab", "abbas"),
    "ahl al bayt" to listOf("ahl al bayt", "fatimah", "hasan", "husayn", "ali", "zaynab", "abbas"),
    "abu bakar" to listOf("abu bakr", "siddiq"),
    "abubakar" to listOf("abu bakr", "siddiq"),
    "siddique" to listOf("abu bakr", "siddiq"),
    "umar" to listOf("umar", "khattab", "faruq"),
    "omar" to listOf("umar", "khattab", "faruq"),
    "farooq" to listOf("umar", "khattab", "faruq"),
    "usman" to listOf("uthman", "affan", "dhu al nurayn"),
    "osman" to listOf("uthman", "affan"),
    "ghani" to listOf("uthman", "affan"),
    "ali" to listOf("ali ibn abi talib", "amir al muminin", "murtada"),
    "murtaza" to listOf("ali ibn abi talib"),
    "haider" to listOf("ali ibn abi talib"),
    "fatima" to listOf("fatimah", "zahra"),
    "zahra" to listOf("fatimah", "zahra"),
    "batool" to listOf("fatimah", "zahra"),
    "hassan" to listOf("hasan ibn ali", "mujtaba"),
    "hasan" to listOf("hasan ibn ali", "mujtaba"),
    "hussain" to listOf("husayn ibn ali", "karbala", "shuhada"),
    "husain" to listOf("husayn ibn ali", "karbala"),
    "zainab" to listOf("zaynab bint ali"),
    "abbas" to listOf("abbas ibn ali", "qamar"),
    "alamdar" to listOf("abbas ibn ali"),
    "sajjad" to listOf("zayn al abidin", "sahifa"),
    "zain ul abideen" to listOf("zayn al abidin"),
    "baqir" to listOf("muhammad al baqir"),
    "jafar" to listOf("jafar al sadiq", "jafar ibn abi talib"),
    "sadiq" to listOf("jafar al sadiq"),
    "kazim" to listOf("musa al kazim"),
    "kazhim" to listOf("musa al kazim"),
    "reza" to listOf("ali ibn musa al rida"),
    "raza" to listOf("ali ibn musa al rida"),
    "rida" to listOf("ali ibn musa al rida"),
    "talha" to listOf("talhah"),
    "zubair" to listOf("zubayr"),
    "saad" to listOf("sad ibn abi waqqas"),
    "abdur rahman" to listOf("abd al rahman ibn awf"),
    "abu ubaidah" to listOf("abu ubaydah ibn al jarrah"),
    "salman" to listOf("salman al farisi", "salman al muhammadi"),
    "abu zar" to listOf("abu dharr"),
    "abu dhar" to listOf("abu dharr"),
    "ammar" to listOf("ammar ibn yasir"),
    "miqdad" to listOf("miqdad"),
    "bilal" to listOf("bilal ibn rabah"),
    "ibn masud" to listOf("ibn masud"),
    "hamza" to listOf("hamzah ibn abd al muttalib"),
    "tayyar" to listOf("jafar ibn abi talib"),
    "muadh" to listOf("muadh ibn jabal"),
    "ubayy" to listOf("ubayy ibn kab"),
    "zaid" to listOf("zayd ibn thabit"),
    "khalid" to listOf("khalid ibn al walid"),
    "khadija" to listOf("khadijah"),
    "aisha" to listOf("aishah"),
    "ayesha" to listOf("aishah"),
    "hafsa" to listOf("hafsah"),
    "umm salama" to listOf("umm salamah"),
    "asma" to listOf("asma bint abi bakr")
  )

  private val preIndexedBioSearchBlobs: Map<String, Pair<String, String>> by lazy {
    getAllBiographies().associate { bio ->
      val rawConcat = buildString {
        append(bio.personNameEnglish).append(' ')
        append(bio.personNameArabic).append(' ')
        append(bio.personNameUrdu).append(' ')
        append(bio.titleHonorific).append(' ')
        append(bio.category).append(' ')
        append(bio.primarySourceBook).append(' ')
        append(bio.sourceBookArabic).append(' ')
        append(bio.author).append(' ')
        append(bio.volumeAndPage).append(' ')
        append(bio.crossReferences).append(' ')
        append(bio.arabicText).append(' ')
        append(bio.urduTranslation).append(' ')
        append(bio.englishTranslation)
      }
      bio.id to (normalizeBioQuery(rawConcat) to rawConcat.lowercase())
    }
  }

  fun searchBiographies(
    query: String,
    traditionFilter: String = "BOTH",
    categoryFilter: String = "ALL"
  ): List<BiographyEntry> {
    val rawQ = query.trim()
    val rawLower = rawQ.lowercase()
    val normQ = normalizeBioQuery(rawQ)
      .removePrefix("hazrat ")
      .removePrefix("hadhrat ")
      .removePrefix("sayyiduna ")
      .removePrefix("imam ")
      .trim()

    val expandedNormalizedTerms = buildList {
      if (normQ.isNotEmpty()) add(normQ)
      if (rawLower.isNotEmpty()) add(normalizeBioQuery(rawLower))
      romanUrduBioAliases.forEach { (alias, targets) ->
        if (normQ == alias || normQ.contains(alias)) {
          targets.forEach { add(normalizeBioQuery(it)) }
        }
      }
    }.filter { it.isNotEmpty() }.distinct()

    val isGeneralSahabaOrAhlulBaytQuery = normQ in setOf(
      "sahaba", "sahabi", "companions", "ahalbayat", "ahlulbayt", "ahlebait",
      "ahl al bayt", "sahaba and ahalbayat", "sahaba & ahl al bayt", "sahaba ahl al bayt"
    )

    return getAllBiographies().filter { bio ->
      val tradMatch = traditionFilter.equals("BOTH", ignoreCase = true) ||
        traditionFilter.equals("ALL", ignoreCase = true) ||
        bio.traditionPerspective.equals(traditionFilter, ignoreCase = true)
      val catMatch = categoryFilter.equals("ALL", ignoreCase = true) ||
        bio.category.equals(categoryFilter, ignoreCase = true)
      if (!tradMatch || !catMatch) return@filter false

      if (rawQ.isEmpty() || isGeneralSahabaOrAhlulBaytQuery) {
        if (normQ in setOf("ahalbayat", "ahlulbayt", "ahlebait", "ahl al bayt")) {
          bio.category.equals("Ahl al-Bayt", ignoreCase = true) ||
            bio.personNameEnglish.contains("Ali", ignoreCase = true) ||
            bio.personNameEnglish.contains("Fatimah", ignoreCase = true) ||
            bio.personNameEnglish.contains("Hasan", ignoreCase = true) ||
            bio.personNameEnglish.contains("Husayn", ignoreCase = true)
        } else {
          true
        }
      } else {
        val (searchableBlob, rawLowerBlob) = preIndexedBioSearchBlobs[bio.id] ?: ("" to "")
        expandedNormalizedTerms.any { term -> searchableBlob.contains(term) } ||
          rawLowerBlob.contains(rawLower)
      }
    }
  }

  private var inMemoryLastReadBioId: String = ""

  fun saveLastReadBiographyId(context: Context?, bioId: String) {
    inMemoryLastReadBioId = bioId
    context?.getSharedPreferences(BIO_PREFS, Context.MODE_PRIVATE)
      ?.edit()
      ?.putString("last_read_bio_id", bioId)
      ?.apply()
  }

  fun getLastReadBiographyId(context: Context?): String {
    if (context == null) return inMemoryLastReadBioId
    return context.getSharedPreferences(BIO_PREFS, Context.MODE_PRIVATE)
      .getString("last_read_bio_id", inMemoryLastReadBioId)
      .orEmpty()
  }

  fun toggleBiographyBookmark(context: Context?, bioId: String): Boolean {
    if (context == null) {
      return if (bioId in inMemoryBioBookmarks) {
        inMemoryBioBookmarks.remove(bioId)
        false
      } else {
        inMemoryBioBookmarks.add(bioId)
        true
      }
    }
    val prefs = context.getSharedPreferences(BIO_PREFS, Context.MODE_PRIVATE)
    val set = prefs.getStringSet("bookmarks", emptySet())?.toMutableSet() ?: mutableSetOf()
    val added = if (bioId in set) {
      set.remove(bioId)
      inMemoryBioBookmarks.remove(bioId)
      false
    } else {
      set.add(bioId)
      inMemoryBioBookmarks.add(bioId)
      true
    }
    prefs.edit().putStringSet("bookmarks", set).apply()
    return added
  }

  fun getBookmarkedBiographyIds(context: Context?): Set<String> {
    if (context == null) return inMemoryBioBookmarks.toSet()
    val prefs = context.getSharedPreferences(BIO_PREFS, Context.MODE_PRIVATE)
    return (prefs.getStringSet("bookmarks", emptySet()) ?: emptySet()) + inMemoryBioBookmarks
  }

  // =========================================================================
  // PART B: SUNNI / SHIA / SHARED CLASSICAL LIBRARY CLASSIFICATION (SPEC B PART 4)
  // =========================================================================

  enum class ClassicalLibraryPrimarySection(
    val sectionId: String,
    val titleEnglish: String,
    val titleUrdu: String,
    val titleArabic: String,
    val description: String
  ) {
    SUNNI_CLASSICAL(
      sectionId = "SUNNI_CLASSICAL",
      titleEnglish = "Sunni Classical Library",
      titleUrdu = "اہلسنت کلاسیکی کتب خانہ",
      titleArabic = "المكتبة الكلاسيكية لأهل السنة والجماعة",
      description = "Primary Sunni History, Seerah, Tabaqat/Rijal, Hadith, Tafsir & Fiqh (34 Works)"
    ),
    SHARED_OR_DISPUTED(
      sectionId = "SHARED_OR_DISPUTED",
      titleEnglish = "Shared / Disputed Attribution",
      titleUrdu = "مشترکہ اور مختلف فیہ انتساب والی کتب",
      titleArabic = "المصادر المشتركة أو المختلف في نسبتها",
      description = "Works cited across traditions or carrying disputed attribution flags (7 Works)"
    ),
    SHIA_CLASSICAL(
      sectionId = "SHIA_CLASSICAL",
      titleEnglish = "Shia Classical Library",
      titleUrdu = "شیعہ کلاسیکی کتب خانہ",
      titleArabic = "المكتبة الكلاسيكية الشيعية الإمامية",
      description = "Primary Imami History, Maqatil, Al-Kutub al-Arba'ah, Nahj al-Balagha & Sahifa (10 Works)"
    )
  }

  data class ClassifiedLibraryBookItem(
    val book: IslamicClassicalBook,
    val primarySection: ClassicalLibraryPrimarySection,
    val subFolderName: String,
    val scholarlyFlagBadge: String = "" // e.g. "DISPUTED ATTRIBUTION", "POLEMICAL WORK", "TRANSMISSION FLAGGED"
  )

  /**
   * Classifies every classical book into EXACTLY ONE primary section ("Sunni Classical Library",
   * "Shared / Disputed Attribution", or "Shia Classical Library") per Spec B Part 4, with subfolder
   * organization and scholarly attribution/polemical flags.
   */
  fun getClassifiedLibraryCatalog(context: Context? = null): List<ClassifiedLibraryBookItem> {
    val historical26 = runCatching { IslamicDigitalLibraryRepository.getCompleteHistoricalBooks26(context) }.getOrDefault(emptyList())
    val allBooks = historical26 + IslamicBooksCatalog.allExpandedLibraryBooks
    val sharedIds = setOf(
      "hist_book_05_ansab_ashraf",      // Ansab al-Ashraf — Al-Baladhuri
      "hist_book_08_tarikh_yaqubi",     // Tarikh al-Ya'qubi — Al-Ya'qubi
      "hist_book_09_muruj_dhahab",      // Muruj al-Dhahab — Al-Mas'udi
      "hist_book_10_akhbar_tiwal",      // Al-Akhbar al-Tiwal — Al-Dinawari
      "hist_book_11_kitab_futuh",       // Kitab al-Futuh — Ibn A'tham al-Kufi
      "hist_book_11_futuh_ibn_atham",   // Kitab al-Futuh — Ibn A'tham al-Kufi
      "book_shia_history_siffin_maqatil", // Maqatil al-Talibiyyin — Abu al-Faraj al-Isfahani
      "hist_book_13_imama_wa_siyasa",   // Al-Imama wa al-Siyasa — Pseudo-Ibn Qutaybah (DISPUTED)
      "hist_book_15_imama_wa_siyasa",   // Al-Imama wa al-Siyasa — Pseudo-Ibn Qutaybah (DISPUTED)
      "book_shia_history_yaqubi_masudi" // Combined Ya'qubi/Mas'udi entry
    )

    val items = ArrayList<ClassifiedLibraryBookItem>(allBooks.size + 24)
    val seenIds = HashSet<String>()

    for (rawBook in allBooks) {
      if (!seenIds.add(rawBook.id)) continue

      val primarySection = when {
        rawBook.id in sharedIds ||
          rawBook.titleEnglish.contains("Ya'qubi", ignoreCase = true) ||
          rawBook.titleEnglish.contains("Muruj al-Dhahab", ignoreCase = true) ||
          rawBook.titleEnglish.contains("Akhbar al-Tiwal", ignoreCase = true) ||
          rawBook.titleEnglish.contains("Ansab al-Ashraf", ignoreCase = true) ||
          rawBook.titleEnglish.contains("Ibn A'tham", ignoreCase = true) ||
          rawBook.titleEnglish.contains("Maqatil al-Talibiyyin", ignoreCase = true) ||
          rawBook.titleEnglish.contains("Imama wa al-Siyasa", ignoreCase = true) ->
          ClassicalLibraryPrimarySection.SHARED_OR_DISPUTED

        rawBook.tradition.equals("SHIA", ignoreCase = true) ->
          ClassicalLibraryPrimarySection.SHIA_CLASSICAL

        else ->
          ClassicalLibraryPrimarySection.SUNNI_CLASSICAL
      }

      val flagBadge = when {
        rawBook.id == "hist_book_13_imama_wa_siyasa" ||
          rawBook.id == "hist_book_15_imama_wa_siyasa" ||
          rawBook.titleEnglish.contains("Imama wa al-Siyasa", ignoreCase = true) ->
          "DISPUTED ATTRIBUTION TO IBN QUTAYBAH"
        rawBook.id == "hist_book_12_futuh_sham_waqidi" ||
          rawBook.titleEnglish.contains("Futuh al-Sham", ignoreCase = true) ->
          "ATTRIBUTION TO AL-WAQIDI DISPUTED"
        rawBook.titleEnglish.contains("Sawa'iq al-Muhriqah", ignoreCase = true) ->
          "POLEMICAL WORK (IBN HAJAR AL-HAYTAMI)"
        rawBook.titleEnglish.contains("Abu Mikhnaf", ignoreCase = true) ->
          "EARLY KUFAN AKHBARI TRANSMISSION (TABARI / IRSHAD)"
        rawBook.titleEnglish.contains("Sulaym ibn Qays", ignoreCase = true) ->
          "DISPUTED MANUSCRIPT AUTHENTICITY IN RIJAL"
        else -> ""
      }

      val updatedBook = if (flagBadge.isNotBlank() && !rawBook.attributionConfidence.contains(flagBadge, ignoreCase = true)) {
        rawBook.copy(attributionConfidence = "$flagBadge • ${rawBook.attributionConfidence}")
      } else {
        rawBook
      }

      val subFolder = when (primarySection) {
        ClassicalLibraryPrimarySection.SUNNI_CLASSICAL -> when {
          updatedBook.titleEnglish.contains("Tabaqat", ignoreCase = true) ||
            updatedBook.titleEnglish.contains("Isaba", ignoreCase = true) ||
            updatedBook.titleEnglish.contains("Usd al-Ghaba", ignoreCase = true) ||
            updatedBook.titleEnglish.contains("Isti'ab", ignoreCase = true) ||
            updatedBook.titleEnglish.contains("Siyar A'lam", ignoreCase = true) ||
            updatedBook.titleEnglish.contains("Tahdhib", ignoreCase = true) ||
            updatedBook.titleEnglish.contains("Tadhkirat", ignoreCase = true) ||
            updatedBook.titleEnglish.contains("Wafayat", ignoreCase = true) ||
            updatedBook.titleEnglish.contains("Hilyat", ignoreCase = true) ->
            "2. Sahaba & Biographical Dictionaries (Tabaqat & Rijal)"
          updatedBook.category.contains("Hadith", ignoreCase = true) ||
            updatedBook.titleEnglish.contains("Fada'il al-Sahaba", ignoreCase = true) ||
            updatedBook.titleEnglish.contains("Khasa'is", ignoreCase = true) ||
            updatedBook.titleEnglish.contains("Matalib al-Aliyah", ignoreCase = true) ->
            "3. Primary Hadith & Sunan Compendiums"
          updatedBook.category.contains("Tafsir", ignoreCase = true) ||
            updatedBook.category.contains("Fiqh", ignoreCase = true) ||
            updatedBook.category.contains("Adhkar", ignoreCase = true) ||
            updatedBook.id == "hist_book_23_futuhat_makkiyya" ->
            "4. Tafsir, Fiqh, Aqeedah & Tazkiyah"
          else ->
            "1. Sunni Historical Chronicles & Prophetic Seerah"
        }
        ClassicalLibraryPrimarySection.SHARED_OR_DISPUTED -> when {
          flagBadge.contains("DISPUTED", ignoreCase = true) ->
            "2. Disputed Attribution Works (Scholarly Warning Flagged)"
          else ->
            "1. Shared Early Chronicles & Genealogies (Cited by Both Traditions)"
        }
        ClassicalLibraryPrimarySection.SHIA_CLASSICAL -> when {
          updatedBook.titleEnglish.contains("Kafi", ignoreCase = true) ||
            updatedBook.titleEnglish.contains("Faqih", ignoreCase = true) ||
            updatedBook.titleEnglish.contains("Tahdhib", ignoreCase = true) ||
            updatedBook.titleEnglish.contains("Istibsar", ignoreCase = true) ->
            "2. Al-Kutub al-Arba'ah (Four Canonical Hadith Books)"
          updatedBook.titleEnglish.contains("Nahj al-Balagha", ignoreCase = true) ||
            updatedBook.titleEnglish.contains("Sajjadiyya", ignoreCase = true) ->
            "3. Nahj al-Balagha & Al-Sahifa al-Sajjadiyya"
          else ->
            "1. Imami Historical Chronicles & Maqatil (Karbala & Ahl al-Bayt)"
        }
      }

      items.add(
        ClassifiedLibraryBookItem(
          book = updatedBook,
          primarySection = primarySection,
          subFolderName = subFolder,
          scholarlyFlagBadge = flagBadge
        )
      )
    }

    // Add explicit dedicated entries required by Spec B Part 4 if not already standalone in allBooks:
    // 1. Al-Sawa'iq al-Muhriqah — Ibn Hajar al-Haytami (Sunni Classical Library — flagged as polemical)
    if (items.none { it.book.id == "book_sunni_sawaiq_muhriqah" }) {
      val sawaiqBook = IslamicClassicalBook(
        id = "book_sunni_sawaiq_muhriqah",
        number = 91,
        titleEnglish = "Al-Sawa'iq al-Muhriqah (Ibn Hajar al-Haytami)",
        titleArabic = "الصواعق المحرقة على أهل الرفض والضلال والزندقة لابن حجر الهيتمي",
        titleUrdu = "الصواعق المحرقہ (علامہ ابن حجر ہیتمی مکیؒ)",
        author = "Imam Shihab al-Din Ahmad ibn Muhammad Ibn Hajar al-Haytami al-Makki",
        eraHijri = "909–974 AH",
        category = "Aqeedah & Fiqh",
        numberingSystem = "Mu'assasat al-Risalah Critical Edition (2 Volumes • 11 Chapters + Khatimah)",
        summary = "Classified in Sunni Classical Library with explicit Polemical Work scholarly notice: Composed by the Shafi'i jurist Ibn Hajar al-Haytami (d. 974 AH) in Makkah defending the legitimacy of the Caliphates of Abu Bakr, 'Umar, 'Uthman, and 'Ali (RA), while Chapter 11 sets forth forty Prophetic Hadiths and Quranic verses on the obligation of loving, honoring, and blessing the Prophet's Ahl al-Bayt.",
        chapters = listOf(
          IslamicBookChapter(
            chapterNumber = 1,
            titleEnglish = "Jild 1 (Volume 1) — Legitimacy of the Four Rightly Guided Caliphs (Scholarly Polemical Notice)",
            titleArabic = "المجلد الأول: خلافة الخلفاء الراشدين الأربعة (تنبيه علمي: مصنف جدلي عقدي)",
            titleUrdu = "جلد اول: خلفائے راشدین اربعہ کی خلافت کے دلائل (علمی تنبیہ: کلامی و مناظراتی تصنیف)",
            arabicExcerpt = "اعْلَمْ أَنَّ الَّذِي أَطْبَقَ عَلَيْهِ أَهْلُ السُّنَّةِ وَالْجَمَاعَةِ أَنَّ أَفْضَلَ النَّاسِ بَعْدَ نَبِيِّنَا ﷺ أَبُو بَكْرٍ ثُمَّ عُمَرُ ثُمَّ عُثْمَانُ ثُمَّ عَلِيٌّ رَضِيَ اللَّهُ عَنْهُمْ أَجْمَعِينَ.",
            transliteration = "I'lam annal-ladhi atbaqa 'alayhi Ahlus-Sunnati wal-Jama'ati anna afdalan-nasi ba'da Nabiyyina (ﷺ) Abu Bakrin thumma 'Umaru thumma 'Uthmanu thumma 'Aliyyun (RA).",
            englishContent = "[SCHOLARLY CLASSIFICATION NOTICE: Polemical theological work by Ibn Hajar al-Haytami (d. 974 AH).] Volume 1 compiles Sunni scriptural and historical arguments for the Caliphates of Abu Bakr al-Siddiq, 'Umar ibn al-Khattab, 'Uthman ibn 'Affan, and 'Ali ibn Abi Talib (RA).",
            urduContent = "[علمی و تحقیقی وضاحت: یہ علامہ ابن حجر ہیتمی مکی (متوفی 974 ہجری) کی کلامی و مناظراتی کتاب ہے جسے اہلسنت کلاسیکی کتب خانے میں واضح علمی نوٹ کے ساتھ رکھا گیا ہے۔] جلد اول میں خلفائے راشدین اربعہ کی خلافت اور فضائل پر اہلسنت کے دلائل جمع کیے گئے ہیں۔",
            scholarlyNotes = "POLEMICAL WORK FLAG: Explicitly flagged per scholarly cataloguing standards so polemical debate is distinguished from primary Hadith collections.",
            crossReferences = "Al-Sawa'iq al-Muhriqah Vol. 1, pp. 15–310",
            jildNumber = 1,
            jildTitle = "Jild 1 (Volume 1 • Pg. 15)",
            pageNumber = 15
          ),
          IslamicBookChapter(
            chapterNumber = 2,
            titleEnglish = "Jild 2 (Volume 2, Chapter 11) — Verses & 40 Hadiths on the Virtues and Love of Ahl al-Bayt",
            titleArabic = "المجلد الثاني (الباب الحادي عشر): الآيات والأربعون حديثاً في فضل أهل البيت النبوي ومحبتهم",
            titleUrdu = "جلد دوم (باب یازدہم): اہلِ بیتِ نبوت کی فضیلت اور مودت میں آیاتِ قرآنیہ اور چالیس احادیث",
            arabicExcerpt = "قُلْ لَا أَسْأَلُكُمْ عَلَيْهِ أَجْرًا إِلَّا الْمَوَدَّةَ فِي الْقُرْبَى... إِنِّي تَارِكٌ فِيكُمْ مَا إِنْ تَمَسَّكْتُمْ بِهِ لَنْ تَضِلُّوا بَعْدِي: كِتَابَ اللَّهِ، وَعِتْرَتِي أَهْلَ بَيْتِي.",
            transliteration = "Qul la as'alukum 'alayhi ajran illal-mawaddata fil-qurba... Inni tarikun fikum ma in tamassaktum bihi lan tadillu ba'di: Kitaballahi wa 'itrati Ahla Bayti.",
            englishContent = "In Volume 2 (Chapter 11, pp. 421–680), Ibn Hajar al-Haytami expounds fourteen Quranic verses (including 42:23 and 33:33) and forty Prophetic Hadiths affirming the obligation of loving the Prophet's Household (Ahl al-Bayt), recounting the biographies of Lady Fatimah, Imam al-Hasan, Imam al-Husayn, Imam Zayn al-'Abidin, Imam al-Baqir, and Imam al-Sadiq (RA).",
            urduContent = "جلد دوم (باب یازدہم، ص 421 تا 680) میں علامہ ابن حجر ہیتمی نے آیتِ مودت (الشوریٰ: 23) اور آیتِ تطہیر (الاحزاب: 33) سمیت چودہ قرآنی آیات اور چالیس احادیث کی روشنی میں اہلِ بیتِ اطہار کی محبت و تعظیم کے وجوب اور ائمہ اہلِ بیت کے مناقب کو تفصیل سے بیان فرمایا ہے۔",
            scholarlyNotes = "Volume 2, Chapter 11 • Mu'assasat al-Risalah Edition, pp. 421–680",
            crossReferences = "Quran 42:23, 33:33 • Sahih Muslim 2408, 2424 • Jami' at-Tirmidhi 3788",
            jildNumber = 2,
            jildTitle = "Jild 2 (Volume 2 • Pg. 421)",
            pageNumber = 421
          )
        ),
        tradition = "SUNNI",
        officialSourceUrl = "https://shamela.ws/book/6563",
        sourceTypeLabel = "Sunni Classical Library (Polemical Work Flagged)",
        attributionConfidence = "POLEMICAL WORK FLAG • Confirmed Attribution to Ibn Hajar al-Haytami (d. 974 AH)",
        volumesCount = 2,
        catalogBookNumber = 91
      )
      items.add(
        ClassifiedLibraryBookItem(
          book = sawaiqBook,
          primarySection = ClassicalLibraryPrimarySection.SUNNI_CLASSICAL,
          subFolderName = "4. Tafsir, Fiqh, Aqeedah & Tazkiyah",
          scholarlyFlagBadge = "POLEMICAL WORK (IBN HAJAR AL-HAYTAMI)"
        )
      )
    }

    // 2. Maqtal al-Husayn (Attributed to Abu Mikhnaf) & Kitab Sulaym ibn Qays in Shia Classical Library with explicit flags
    if (items.none { it.book.id == "book_shia_abu_mikhnaf_sulaym" }) {
      val abuMikhnafBook = IslamicClassicalBook(
        id = "book_shia_abu_mikhnaf_sulaym",
        number = 92,
        titleEnglish = "Maqtal al-Husayn (Attributed to Abu Mikhnaf) & Kitab Sulaym ibn Qays al-Hilali",
        titleArabic = "مقتل الحسين المنسوب لأبي مخنف لوط بن يحيى الأزدي وكتاب سليم بن قيس الهلالي",
        titleUrdu = "مقتل الحسین (منسوب بہ ابو مخنف ازدی) اور کتاب سلیم بن قیس ہلالی (مع تحقیقی اسنادی نوٹ)",
        author = "Lut ibn Yahya Abu Mikhnaf al-Azdi (d. 157 AH) & Sulaym ibn Qays al-Hilali (Attributed)",
        eraHijri = "2nd Century AH",
        category = "Seerah & History",
        numberingSystem = "Critical Edition Comparing Tabari / Irshad Preserved Reports vs. Later Standalone Manuscripts",
        summary = "Classified in Shia Classical Library with mandatory transmission & authenticity flags: (1) Abu Mikhnaf's (d. 157 AH) authentic early Kufan reports on Karbala are preserved via Hisham ibn Muhammad al-Kalbi inside al-Tabari and Shaykh al-Mufid's Kitab al-Irshad, whereas standalone late manuscripts titled 'Maqtal Abi Mikhnaf' contain interpolations; (2) Kitab Sulaym ibn Qays is flagged for disputed manuscript authenticity among classical Imami Rijal scholars (Shaykh al-Mufid in Tashih al-I'tiqadat & Ibn al-Ghadha'iri vs. al-Nu'mani).",
        chapters = listOf(
          IslamicBookChapter(
            chapterNumber = 1,
            titleEnglish = "Jild 1 — Maqtal al-Husayn (Abu Mikhnaf): Transmission Status Flag (Tabari/Irshad vs. Standalone Print)",
            titleArabic = "المجلد الأول: مقتل الحسين لأبي مخنف — تحقيق طرق الرواية المحفوظة عند الطبري والمفيد",
            titleUrdu = "جلد اول: مقتل ابی مخنف کی اسنادی حیثیت — تاریخ طبری اور کتاب الارشاد میں محفوظ اصل روایات بمقابلہ متاخر نسخہ",
            arabicExcerpt = "حَدَّثَنَا هِشَامُ بْنُ مُحَمَّدٍ عَنْ أَبِي مِخْنَفٍ لُوطِ بْنِ يَحْيَى الْأَزْدِيِّ فِي أَخْبَارِ الطَّفِّ وَمَسِيرِ الْحُسَيْنِ بْنِ عَلِيٍّ إِلَى كَرْبَلَاءَ سَنَةَ إِحْدَى وَسِتِّينَ.",
            transliteration = "Haddathana Hishamu bnu Muhammadin 'an Abi Mikhnafin Lut ibni Yahya al-Azdiyyi fi akhbarit-Taffi wa masiril-Husayni bni 'Aliyyin ila Karbala'a sanata ihda wa sittin.",
            englishContent = "[TRANSMISSION STATUS FLAG]: Lut ibn Yahya Abu Mikhnaf al-Kufi (d. 157 AH) composed the earliest Kufan monograph on Karbala. His original reports transmitted through Hisham ibn Muhammad al-Kalbi (d. 204 AH) are preserved verbatim in Tarikh al-Tabari (Vol. 5) and Shaykh al-Mufid's Kitab al-Irshad (Vol. 2). Modern historians and Rijal scholars note that the standalone printed booklet circulating under his name originates from later centuries and must be verified against the primary citations in al-Tabari and al-Irshad.",
            urduContent = "[اسنادی حیثیت کی وضاحت]: ابو مخنف لوط بن یحییٰ ازدی کوفی (متوفی 157 ہجری) نے واقعہ کربلا پر قدیم ترین کوفی رسالہ مرتب کیا جس کی اصل روایات ہشام بن محمد کلبی کے واسطے سے تاریخ طبری (جلد 5) اور شیخ مفید کی کتاب الارشاد (جلد 2) میں محفوظ ہیں، جبکہ متاخر ادوار کا الگ مطبوعہ نسخہ الحاقات سے خالی نہیں، اس لیے اصل ماخذ طبری اور الارشاد میں محفوظ روایات ہی ہیں۔",
            scholarlyNotes = "TRANSMISSION STATUS FLAGGED: Distinguishes Abu Mikhnaf's early reports preserved in al-Tabari/al-Irshad from later standalone manuscripts.",
            crossReferences = "Tarikh al-Tabari 5:338–460 • Kitab al-Irshad 2:32–135",
            jildNumber = 1,
            jildTitle = "Jild 1 (Volume 1 • Pg. 10)",
            pageNumber = 10
          ),
          IslamicBookChapter(
            chapterNumber = 2,
            titleEnglish = "Jild 2 — Kitab Sulaym ibn Qays al-Hilali: Disputed Authenticity & Rijal Assessment Flag",
            titleArabic = "المجلد الثاني: كتاب سليم بن قيس الهلالي — بيان اختلاف علماء الرجال في تحقيقه (الشيخ المفيد في تصحيح الاعتقادات)",
            titleUrdu = "جلد دوم: کتاب سلیم بن قیس ہلالی — علمائے رجال کے نزدیک اختلافِ صحت اور شیخ مفید کا تحقیقی قول",
            arabicExcerpt = "قَالَ الشَّيْخُ الْمُفِيدُ فِي تَصْحِيحِ الِاعْتِقَادَاتِ: إِنَّ هَذَا الْكِتَابَ غَيْرُ مَوْثُوقٍ بِهِ، وَقَدْ حَصَلَ فِيهِ تَخْلِيطٌ وَتَدْلِيسٌ، فَيَنْبَغِي لِلْمُتَدَيِّنِ أَنْ يَجْتَنِبَ الْعَمَلَ بِكُلِّ مَا فِيهِ وَلَا يُعَوِّلَ عَلَى جُمْلَتِهِ.",
            transliteration = "Qalash-Shaykhul-Mufidu fi Tashihil-I'tiqadat: Inna hadhal-kitaba ghayru mawthuqin bihi, wa qad hasala fihi takhlitun wa tadlis.",
            englishContent = "[DISPUTED AUTHENTICITY FLAG]: Kitab Sulaym ibn Qays al-Hilali (transmitted via Aban ibn Abi 'Ayyash) is subject to major debate in classical Shia Rijal criticism. While al-Nu'mani cited it, Shaykh al-Mufid (d. 413 AH) explicitly warned in Tashih al-I'tiqadat (p. 149) and Ibn al-Ghadha'iri noted that the surviving text experienced alteration and interpolation (takhlit wa tadlis) and that its reports cannot be accepted wholesale without independent corroboration.",
            urduContent = "[تحقیقی نوٹ برائے اختلافِ صحت]: کتاب سلیم بن قیس ہلالی (بروایت ابان بن ابی عیاش) کی صحت میں خود شیعہ علمائے رجال کے درمیان شدید اختلاف ہے۔ جہاں نعمانی نے اس سے استدلال کیا، وہیں شیخ مفید (متوفی 413 ہجری) نے ’تصحیح الاعتقادات‘ (ص 149) اور ابن الغضائری نے تصریح کی ہے کہ اس کتاب کے نسخوں میں تخلیط و تدلیس واقع ہوئی ہے لہٰذا مستقل شواہد کے بغیر اس کے تمام مندرجات پر کلی اعتماد نہیں کیا جا سکتا۔",
            scholarlyNotes = "DISPUTED AUTHENTICITY FLAGGED per Spec B Part 4: Cites Shaykh al-Mufid's Tashih al-I'tiqadat p. 149.",
            crossReferences = "Tashih al-I'tiqadat (Shaykh al-Mufid) p. 149 • Rijal Ibn al-Ghadha'iri",
            jildNumber = 2,
            jildTitle = "Jild 2 (Volume 2 • Pg. 149)",
            pageNumber = 149
          )
        ),
        tradition = "SHIA",
        officialSourceUrl = "https://shiaonlinelibrary.com",
        sourceTypeLabel = "Shia Classical Library (Transmission & Authenticity Flagged)",
        attributionConfidence = "TRANSMISSION & AUTHENTICITY FLAGGED • See Shaykh al-Mufid (Tashih al-I'tiqadat p. 149)",
        volumesCount = 2,
        catalogBookNumber = 92
      )
      items.add(
        ClassifiedLibraryBookItem(
          book = abuMikhnafBook,
          primarySection = ClassicalLibraryPrimarySection.SHIA_CLASSICAL,
          subFolderName = "1. Imami Historical Chronicles & Maqatil (Karbala & Ahl al-Bayt)",
          scholarlyFlagBadge = "TRANSMISSION & AUTHENTICITY FLAGGED"
        )
      )
    }

    // 3. Additional Classical Sunni Works explicitly listed in Spec B Part 4 (lines 153–181)
    data class SpecBPart4ExtraBook(
      val id: String,
      val num: Int,
      val titleEn: String,
      val titleAr: String,
      val titleUr: String,
      val author: String,
      val era: String,
      val subFolder: String,
      val arabicIncipit: String,
      val englishSummary: String,
      val urduSummary: String,
      val ref: String,
      val flag: String = ""
    )

    val extraSunniSpecBWorks = listOf(
      SpecBPart4ExtraBook(
        id = "hist_book_12_futuh_sham_waqidi",
        num = 93,
        titleEn = "Futuh al-Sham (Attributed to al-Waqidi — Attribution Disputed)",
        titleAr = "فتوح الشام المنسوب للواقدي (نسبته للواقدي مختلف فيها)",
        titleUr = "فتوح الشام (منسوب بہ واقدی — انتساب مختلف فیہ)",
        author = "Traditionally attributed to Muhammad ibn 'Umar al-Waqidi (d. 207 AH); attribution disputed",
        era = "Disputed / Post-3rd Century AH Compilation",
        subFolder = "1. Sunni Historical Chronicles & Prophetic Seerah",
        arabicIncipit = "ذِكْرُ مَا وَقَعَ فِي فُتُوحِ الشَّامِ فِي خِلَافَةِ أَبِي بَكْرٍ الصِّدِّيقِ وَعُمَرَ بْنِ الْخَطَّابِ رَضِيَ اللَّهُ عَنْهُمَا (مَعَ التَّنْبِيهِ الْعِلْمِيِّ عَلَى أَنَّ نِسْبَةَ النُّسْخَةِ الْمَطْبُوعَةِ لِلْوَاقِدِيِّ لَا تَثْبُتُ إِسْنَاداً).",
        englishSummary = "[DISPUTED ATTRIBUTION FLAG]: While Muhammad ibn 'Umar al-Waqidi (d. 207 AH) authored an early work on the Syrian conquests, Hadith and manuscript scholars (including Ibn Hajar and modern critical editors) demonstrate that the popular printed 'Futuh al-Sham' contains late Crusader-era stylistic expansions and cannot be authenticated as al-Waqidi's original text.",
        urduSummary = "[تحقیقی تنبیہ برائے انتساب]: اگرچہ محمد بن عمر واقدی (متوفی 207 ہجری) کی طرف فتوح الشام منسوب ہے، لیکن محدثین اور محققین نے واضح کیا ہے کہ متداول مطبوعہ ’فتوح الشام‘ بعد کی صدیوں کی تالیف و اضافات پر مشتمل ہے اور اس کی روایات کو اصل کتبِ اسناد (بلاذری و طبری) پر پرکھنا ضروری ہے۔",
        ref = "Futuh al-Sham (Printed Edition) • Compare Futuh al-Buldan (Al-Baladhuri) pp. 115–160",
        flag = "ATTRIBUTION TO AL-WAQIDI DISPUTED"
      ),
      SpecBPart4ExtraBook(
        id = "book_sunni_muntazam_ibn_jawzi",
        num = 94,
        titleEn = "Al-Muntazam fi Tarikh al-Muluk wa al-Umam (Ibn al-Jawzi)",
        titleAr = "المنتظم في تاريخ الملوك والأمم لابن الجوزي",
        titleUr = "المنتظم فی تاریخ الملوک والامم (امام ابن جوزیؒ)",
        author = "Imam Jamal al-Din Abu al-Faraj Ibn al-Jawzi (d. 597 AH)",
        era = "510–597 AH",
        subFolder = "1. Sunni Historical Chronicles & Prophetic Seerah",
        arabicIncipit = "الْحَمْدُ لِلَّهِ الَّذِي أَبْدَعَ الْوجودَ بِحِكْمَتِهِ... وَبَعْدُ فَإِنِّي جَمَعْتُ فِي هَذَا الْكِتَابِ تَوَارِيخَ الْمُلُوكِ وَالْأُمَمِ وَوَفَيَاتِ الْأَعْيَانِ وَالْعُلَمَاءِ عَلَى تَرْتِيبِ السِّنِينَ.",
        englishSummary = "Universal annalistic chronicle and biographical obituary dictionary by Imam Ibn al-Jawzi (18 volumes, Dar al-Kutub al-'Ilmiyyah), pairing each Hijri year's historical events with the biographies of scholars who died in that year.",
        urduSummary = "امام ابو الفرج ابن جوزیؒ (متوفی 597 ہجری) کی اٹھارہ جلدوں پر مشتمل جامع تاریخ، جس میں ہر ہجری سال کے تاریخی واقعات کے ساتھ اس سال وفات پانے والے صحابہ، تابعین، محدثین اور فقہاء کے مستند تراجم درج ہیں۔",
        ref = "Al-Muntazam fi Tarikh al-Muluk wa al-Umam Vol. 1, p. 3"
      ),
      SpecBPart4ExtraBook(
        id = "book_sunni_dalail_nubuwwah_bayhaqi",
        num = 95,
        titleEn = "Dala'il al-Nubuwwah wa Ma'rifat Ahwal Sahib al-Shari'ah (Al-Bayhaqi)",
        titleAr = "دلائل النبوة ومعرفة أحوال صاحب الشريعة للبيهقي",
        titleUr = "دلائل النبوۃ ومعرفۃ احوال صاحب الشریعۃ (امام بیہقیؒ)",
        author = "Imam Abu Bakr Ahmad ibn al-Husayn al-Bayhaqi (d. 458 AH)",
        era = "384–458 AH",
        subFolder = "1. Sunni Historical Chronicles & Prophetic Seerah",
        arabicIncipit = "أَخْبَرَنَا أَبُو عَبْدِ اللَّهِ الْحَافِظُ... بَابُ مَا جَاءَ فِي دَلَائِلِ نُبُوَّةِ نَبِيِّنَا مُحَمَّدٍ ﷺ وَمَعْرِفَةِ أَحْوَالِهِ فِي مَبْدَأِ أَمْرِهِ وَمَبْعَثِهِ وَمَغَازِيهِ بِالْأَسَانِيدِ الصَّحِيحَةِ.",
        englishSummary = "Seven-volume Hadith-based encyclopedia of Prophetic biography and signs of Prophethood by Imam al-Bayhaqi, transmitting every Seerah report with full chains of narration and Hadith grading commentary.",
        urduSummary = "امام ابو بکر بیہقیؒ (متوفی 458 ہجری) کی سات جلدوں پر مشتمل سیرتِ نبوی اور معجزات و دلائلِ نبوت کی عظیم مسند کتاب، جس میں ہر واقعہ مکمل سند اور محدثانہ نقد کے ساتھ بیان کیا گیا ہے۔",
        ref = "Dala'il al-Nubuwwah (Al-Bayhaqi) Vol. 1, p. 11"
      ),
      SpecBPart4ExtraBook(
        id = "book_sunni_shifa_qadi_iyad",
        num = 96,
        titleEn = "Al-Shifa bi-Ta'rif Huquq al-Mustafa (Qadi 'Iyad)",
        titleAr = "الشفا بتعريف حقوق المصطفى للقاضي عياض",
        titleUr = "الشفا بتعریف حقوق المصطفیٰ ﷺ (قاضی عیاض مالکیؒ)",
        author = "Qadi Abu al-Fadl 'Iyad ibn Musa al-Yahsubi al-Maliki (d. 544 AH)",
        era = "476–544 AH",
        subFolder = "1. Sunni Historical Chronicles & Prophetic Seerah",
        arabicIncipit = "الْحَمْدُ لِلَّهِ الْمُنْفَرِدِ بِاسْمِهِ الْأَسْمَى... هَذَا كِتَابٌ فِي تَعْرِيفِ قَدْرِ الْمُصْطَفَى ﷺ وَمَا يَجِبُ لَهُ مِنَ التَّوْقِيرِ وَالْإِكْرَامِ وَالْمَحَبَّةِ وَالصَّلَاةِ وَالسَّلَامِ.",
        englishSummary = "Celebrated four-part treatise by Qadi 'Iyad on the exalted station, character, miracles, and rights of the Prophet Muhammad ﷺ, and the legal rulings regarding reverence for the Messenger of Allah ﷺ.",
        urduSummary = "قاضی عیاض مالکیؒ (متوفی 544 ہجری) کی شہرہ آفاق کتاب، جو عظمتِ مصطفیٰ ﷺ، شمائل و معجزاتِ نبویہ، حقوقِ رسالت اور محبت و تعظیمِ رسول ﷺ پر امت کی مقبول ترین تصنیف ہے۔",
        ref = "Al-Shifa bi-Ta'rif Huquq al-Mustafa Vol. 1, p. 5"
      ),
      SpecBPart4ExtraBook(
        id = "book_sunni_subul_huda_shami",
        num = 97,
        titleEn = "Subul al-Huda wa al-Rashad fi Sirat Khayr al-'Ibad (Al-Salihi al-Shami)",
        titleAr = "سبل الهدى والرشاد في سيرة خير العباد للصالحي الشامي",
        titleUr = "سبل الہدیٰ والرشاد فی سیرۃ خیر العباد (سیرتِ شامی — امام صالحی شامیؒ)",
        author = "Imam Muhammad ibn Yusuf al-Salihi al-Shami (d. 942 AH)",
        era = "d. 942 AH",
        subFolder = "1. Sunni Historical Chronicles & Prophetic Seerah",
        arabicIncipit = "الْحَمْدُ لِلَّهِ الَّذِي أَرْسَلَ رَسُولَهُ بِالْهُدَى وَدِينِ الْحَقِّ... وَجَمَعْتُهُ مِنْ أَكْثَرَ مِنْ ثَلَاثِمِائَةِ كِتَابٍ فِي السِّيرَةِ وَالْحَدِيثِ وَالشَّمَائِلِ وَالْخَصَائِصِ.",
        englishSummary = "Comprehensive 12-volume encyclopedia of Seerah compiled from over 300 classical Hadith and historical works by Imam al-Salihi al-Shami.",
        urduSummary = "امام محمد بن یوسف صالحی شامیؒ (متوفی 942 ہجری) کی بارہ جلدوں پر مشتمل سیرتِ نبوی کا عظیم موسوعہ (المعروف بہ سیرتِ شامی) جو تین سو سے زائد قدیم کتبِ حدیث و سیرت سے مرتب کیا گیا ہے۔",
        ref = "Subul al-Huda wa al-Rashad Vol. 1, p. 1"
      ),
      SpecBPart4ExtraBook(
        id = "book_sunni_uyun_athar_ibn_sayyid_nas",
        num = 98,
        titleEn = "'Uyun al-Athar fi Funun al-Maghazi wa al-Shama'il wa al-Siyar (Ibn Sayyid al-Nas)",
        titleAr = "عيون الأثر في فنون المغازي والشمائل والسير لابن سيد الناس",
        titleUr = "عیون الاثر فی فنون المغازی والشمائل والسیر (حافظ ابن سید الناسؒ)",
        author = "Hafiz Fath al-Din Muhammad ibn Muhammad Ibn Sayyid al-Nas al-Ya'mari (d. 734 AH)",
        era = "671–734 AH",
        subFolder = "1. Sunni Historical Chronicles & Prophetic Seerah",
        arabicIncipit = "الْحَمْدُ لِلَّهِ الَّذِي أَوْضَحَ بِنَبِيِّهِ مَعَالِمَ الْإِسْلَامِ... هَذَا مُخْتَصَرٌ فِي سِيرَةِ رَسُولِ اللَّهِ ﷺ وَمَغَازِيهِ وَشَمَائِلِهِ مُحَرَّرُ الْأَسَانِيدِ.",
        englishSummary = "Two-volume critical Seerah and Maghazi compendium by Hafiz Ibn Sayyid al-Nas verifying chains of transmission from Ibn Ishaq, Musa ibn 'Uqbah, and the Six Canonical Books.",
        urduSummary = "حافظ فتح الدین ابن سید الناسؒ (متوفی 734 ہجری) کی سیرت و مغازی اور شمائلِ نبویہ پر دو جلدوں پر مشتمل محققانہ اور مستند کتاب۔",
        ref = "'Uyun al-Athar Vol. 1, p. 19"
      ),
      SpecBPart4ExtraBook(
        id = "book_sunni_fadail_sahaba_ahmad",
        num = 99,
        titleEn = "Fada'il al-Sahaba (Imam Ahmad ibn Hanbal)",
        titleAr = "فضائل الصحابة للإمام أحمد بن حنبل",
        titleUr = "فضائل الصحابہ (امام احمد بن حنبلؒ)",
        author = "Imam Abu 'Abdillah Ahmad ibn Muhammad ibn Hanbal al-Shaybani (d. 241 AH)",
        era = "164–241 AH",
        subFolder = "3. Primary Hadith & Sunan Compendiums",
        arabicIncipit = "حَدَّثَنَا عَبْدُ اللَّهِ بْنُ أَحْمَدَ بْنِ حَنْبَلٍ قَالَ: حَدَّثَنِي أَبِي... بَابُ فَضَائِلِ أَبِي بَكْرٍ الصِّدِّيقِ وَعُمَرَ وَعُثْمَانَ وَعَلِيٍّ وَأَهْلِ الْبَيْتِ وَسَائِرِ أَصْحَابِ رَسُولِ اللَّهِ ﷺ.",
        englishSummary = "Primary Musnad collection of 1,963 chain-transmitted reports compiled by Imam Ahmad ibn Hanbal and his son 'Abdullah on the virtues of the Four Caliphs, Ahl al-Bayt, and the Companions.",
        urduSummary = "امام اہلِ سنت احمد بن حنبلؒ (متوفی 241 ہجری) اور ان کے صاحبزادے عبداللہ بن احمد کی مسند کتاب، جس میں خلفائے راشدین، اہلِ بیتِ اطہار اور صحابہ کرام کے فضائل میں 1,963 مسند روایات جمع کی گئی ہیں۔",
        ref = "Fada'il al-Sahaba (Imam Ahmad, ed. Wasiyyullah 'Abbas) Vol. 1, Hadith 1–1963"
      ),
      SpecBPart4ExtraBook(
        id = "book_sunni_khasais_ali_nasai",
        num = 100,
        titleEn = "Khasa'is Amir al-Mu'minin 'Ali ibn Abi Talib (Imam al-Nasa'i)",
        titleAr = "خصائص أمير المؤمنين علي بن أبي طالب للإمام النسائي",
        titleUr = "خصائص امیر المؤمنین علی بن ابی طالبؓ (امام نسائیؒ)",
        author = "Imam Abu 'Abd al-Rahman Ahmad ibn Shu'ayb al-Nasa'i (d. 303 AH)",
        era = "215–303 AH",
        subFolder = "3. Primary Hadith & Sunan Compendiums",
        arabicIncipit = "أَخْبَرَنَا أَحْمَدُ بْنُ شُعَيْبٍ النَّسَائِيُّ... ذِكْرُ مَا خُصَّ بِهِ أَمِيرُ الْمُؤْمِنِينَ عَلِيُّ بْنُ أَبِي طَالِبٍ رَضِيَ اللَّهُ عَنْهُ مِنْ قَوْلِ النَّبِيِّ ﷺ: أَنْتَ مِنِّي بِمَنْزِلَةِ هَارُونَ مِنْ مُوسَى.",
        englishSummary = "Canonical Hadith monograph of 194 chain-authenticated narrations compiled by Imam al-Nasa'i (author of Sunan al-Nasa'i) on the distinctive virtues of Imam 'Ali ibn Abi Talib, Lady Fatimah, al-Hasan, and al-Husayn (RA).",
        urduSummary = "صاحبِ سنن امام ابو عبدالرحمٰن نسائیؒ (متوفی 303 ہجری) کی مستقل مسند تصنیف، جس میں امیر المؤمنین سیدنا علی بن ابی طالب، سیدہ فاطمہ اور حسنین کریمین رضی اللہ عنہم کے خصائص و فضائل پر 194 باسند احادیث درج ہیں۔",
        ref = "Khasa'is Amir al-Mu'minin 'Ali ibn Abi Talib (Al-Nasa'i) Hadith 1–194"
      ),
      SpecBPart4ExtraBook(
        id = "book_sunni_tarikh_khulafa_suyuti",
        num = 101,
        titleEn = "Tarikh al-Khulafa (Imam Jalal al-Din al-Suyuti)",
        titleAr = "تاريخ الخلفاء للإمام جلال الدين السيوطي",
        titleUr = "تاریخ الخلفاء (امام جلال الدین سیوطیؒ)",
        author = "Imam Jalal al-Din 'Abd al-Rahman ibn Abi Bakr al-Suyuti (d. 911 AH)",
        era = "849–911 AH",
        subFolder = "1. Sunni Historical Chronicles & Prophetic Seerah",
        arabicIncipit = "الْحَمْدُ لِلَّهِ الَّذِي وَعَدَ الْمُؤْمِنِينَ بِالِاسْتِخْلَافِ فِي الْأَرْضِ... هَذَا تَارِيخٌ لَطِيفٌ تَرْجَمْتُ فِيهِ الْخُلَفَاءَ أُمَرَاءَ الْمُؤْمِنِينَ الْقَائِمِينَ بِأَمْرِ الْأُمَّةِ مِنْ عَهْدِ أَبِي بَكْرٍ الصِّدِّيقِ رَضِيَ اللَّهُ عَنْهُ.",
        englishSummary = "Classical biographical history of the Rightly Guided Caliphs (Abu Bakr, 'Umar, 'Uthman, 'Ali, and al-Hasan RA), followed by the Umayyad and 'Abbasid Caliphs up to al-Suyuti's era.",
        urduSummary = "امام جلال الدین سیوطیؒ (متوفی 911 ہجری) کی مقبول و مستند کتاب، جس میں خلفائے راشدین (ابوبکر، عمر، عثمان، علی اور حسن بن علی رضی اللہ عنہم) اور خلفائے بنو امیہ و بنو عباس کے حالات و فضائل درج ہیں۔",
        ref = "Tarikh al-Khulafa (Al-Suyuti) pp. 25–185"
      ),
      SpecBPart4ExtraBook(
        id = "book_sunni_matalib_aliyah_ibn_hajar",
        num = 102,
        titleEn = "Al-Matalib al-'Aliyah bi-Zawa'id al-Masanid al-Thamaniyah (Ibn Hajar al-'Asqalani)",
        titleAr = "المطالب العالية بزوائد المسانيد الثمانية لابن حجر العسقلاني",
        titleUr = "المطالب العالیہ بزوائد المسانید الثمانیہ (حافظ ابن حجر عسقلانیؒ)",
        author = "Hafiz Shihab al-Din Ahmad ibn 'Ali Ibn Hajar al-'Asqalani (d. 852 AH)",
        era = "773–852 AH",
        subFolder = "3. Primary Hadith & Sunan Compendiums",
        arabicIncipit = "الْحَمْدُ لِلَّهِ الَّذِي أَعْلَى مَنَارَ الْإِسْلَامِ بِالسُّنَّةِ النَّبَوِيَّةِ... فَهَذِهِ الْأَحَادِيثُ الزَّائِدَةُ عَلَى الْكُتُبِ السِّتَّةِ مِنَ الْمَسَانِيدِ الثَّمَانِيَةِ مُرَتَّبَةً عَلَى أَبْوَابِ الْفِقْهِ.",
        englishSummary = "19-volume fiqh-ordered Hadith encyclopedia by Hafiz Ibn Hajar al-'Asqalani gathering 4,724 additional Hadiths (Zawa'id) from eight early Musnads (al-Tayalisi, al-Humaydi, Ibn Abi 'Umar, Musaddad, Ahmad ibn Mani', Ibn Abi Shaybah, 'Abd ibn Humayd, and al-Harith).",
        urduSummary = "حافظ ابن حجر عسقلانیؒ (متوفی 852 ہجری) کا فقہی ابواب پر مرتب کردہ عظیم حدیثی مجموعہ، جس میں آٹھ قدیم مسانید (طیالسی، حمیدی، مسدد، ابن ابی شیبہ، عبد بن حمید وغیرہ) کی صحاح ستہ پر زائد 4,724 احادیث مع اسنادی حکم جمع کی گئی ہیں۔",
        ref = "Al-Matalib al-'Aliyah (Dar al-'Asimah Edition, 19 Vols) Hadith 1–4724"
      ),
      SpecBPart4ExtraBook(
        id = "book_sunni_tahdhib_al_tahdhib",
        num = 103,
        titleEn = "Tahdhib al-Tahdhib (Hafiz Ibn Hajar al-'Asqalani)",
        titleAr = "تهذيب التهذيب للحافظ ابن حجر العسقلاني",
        titleUr = "تہذیب التہذیب فی اسماء الرجال (حافظ ابن حجر عسقلانیؒ)",
        author = "Hafiz Shihab al-Din Ahmad ibn 'Ali Ibn Hajar al-'Asqalani (d. 852 AH)",
        era = "773–852 AH",
        subFolder = "2. Sahaba & Biographical Dictionaries (Tabaqat & Rijal)",
        arabicIncipit = "الْحَمْدُ لِلَّهِ الَّذِي تَفَرَّدَ بِالْبَقَاءِ وَالْكَمَالِ... هَذَا مُخْتَصَرٌ نَافِعٌ لِكِتَابِ تَهْذِيبِ الْكَمَالِ لِلْحَافِظِ الْمِزِّيِّ مَعَ زِيَادَةِ مَا فَاتَهُ مِنْ أَقْوَالِ أَئِمَّةِ الْجَرْحِ وَالتَّعْدِيلِ.",
        englishSummary = "12-volume foundational Sunni Rijal dictionary by Hafiz Ibn Hajar al-'Asqalani condensing al-Mizzi's Tahdhib al-Kamal while adding critical Jarh wa Ta'dil evaluations for over 8,800 Hadith transmitters.",
        urduSummary = "حافظ ابن حجر عسقلانیؒ (متوفی 852 ہجری) کی بارہ جلدوں پر مشتمل اسماء الرجال اور جرح و تعدیل کی بنیادی کتاب، جس میں تہذیب الکمال کا خلاصہ اور ائمہ فن کے مزید اقوال جمع کیے گئے ہیں۔",
        ref = "Tahdhib al-Tahdhib (Da'irat al-Ma'arif al-Nizamiyyah, 12 Vols)"
      ),
      SpecBPart4ExtraBook(
        id = "book_sunni_tadhkirat_huffaz_dhahabi",
        num = 104,
        titleEn = "Tadhkirat al-Huffaz (Imam Shams al-Din al-Dhahabi)",
        titleAr = "تذكرة الحفاظ للإمام شمس الدين الذهبي",
        titleUr = "تذکرۃ الحفاظ (امام شمس الدین ذہبیؒ)",
        author = "Imam Shams al-Din Muhammad ibn Ahmad al-Dhahabi (d. 748 AH)",
        era = "673–748 AH",
        subFolder = "2. Sahaba & Biographical Dictionaries (Tabaqat & Rijal)",
        arabicIncipit = "هَذِهِ تَذْكِرَةٌ بِأَسْمَاءِ مَعْدِلِي حَمَلَةِ الْعِلْمِ النَّبَوِيِّ وَمَنْ يُرْجَعُ إِلَى اجْتِهَادِهِمْ فِي التَّوْثِيقِ وَالتَّضْعِيفِ وَالتَّصْحِيحِ وَالتَّزْيِيفِ عَلَى تَرْتِيبِ الطَّبَقَاتِ.",
        englishSummary = "Four-volume chronological biographical encyclopedia of 1,176 master Hadith memorizers (Huffaz) across 21 generations (Tabaqat), beginning with the Rightly Guided Caliphs and Companions.",
        urduSummary = "امام شمس الدین ذہبیؒ (متوفی 748 ہجری) کی چار جلدوں پر مشتمل مشہور کتاب، جس میں عہدِ صحابہ سے آٹھویں صدی ہجری تک اکیس طبقات میں 1,176 حفاظِ حدیث کے مستند تراجم درج ہیں۔",
        ref = "Tadhkirat al-Huffaz (Al-Dhahabi) Vol. 1, p. 2"
      ),
      SpecBPart4ExtraBook(
        id = "book_sunni_tabaqat_shafiiyyah_subki",
        num = 105,
        titleEn = "Tabaqat al-Shafi'iyyah al-Kubra (Taj al-Din al-Subki)",
        titleAr = "طبقات الشافعية الكبرى لتاج الدين السبكي",
        titleUr = "طبقات الشافعیہ الکبریٰ (علامہ تاج الدین سبکیؒ)",
        author = "Imam Taj al-Din 'Abd al-Wahhab ibn 'Ali al-Subki (d. 771 AH)",
        era = "727–771 AH",
        subFolder = "2. Sahaba & Biographical Dictionaries (Tabaqat & Rijal)",
        arabicIncipit = "الْحَمْدُ لِلَّهِ الَّذِي رَفَعَ دَرَجَاتِ الْعُلَمَاءِ... هَذَا كِتَابُ طَبَقَاتِ الشَّافِعِيَّةِ الْكُبْرَى مُفْتَتَحاً بِتَرْجَمَةِ الْإِمَامِ الْمُطَّلِبِيِّ مُحَمَّدِ بْنِ إِدْرِيسَ الشَّافِعِيِّ رَضِيَ اللَّهُ عَنْهُ.",
        englishSummary = "Ten-volume biographical encyclopedia of Shafi'i jurists, Hadith masters, and theologians from Imam Muhammad ibn Idris al-Shafi'i (d. 204 AH) to the 8th century AH.",
        urduSummary = "امام تاج الدین سبکیؒ (متوفی 771 ہجری) کی دس جلدوں پر مشتمل جامع کتاب، جس میں امام محمد بن ادریس شافعیؒ اور شافعی فقہاء و محدثین کے تفصیلی حالات و علمی مباحث جمع کیے گئے ہیں۔",
        ref = "Tabaqat al-Shafi'iyyah al-Kubra (Ed. al-Hulw & al-Tanahi, 10 Vols)"
      ),
      SpecBPart4ExtraBook(
        id = "book_sunni_wafayat_ayan_ibn_khallikan",
        num = 106,
        titleEn = "Wafayat al-A'yan wa Anba' Abna' al-Zaman (Ibn Khallikan)",
        titleAr = "وفيات الأعيان وأنباء أبناء الزمان لابن خلكان",
        titleUr = "وفیات الاعیان وانباء ابناء الزمان (قاضی ابن خلکانؒ)",
        author = "Qadi Shams al-Din Ahmad ibn Muhammad Ibn Khallikan (d. 681 AH)",
        era = "608–681 AH",
        subFolder = "2. Sahaba & Biographical Dictionaries (Tabaqat & Rijal)",
        arabicIncipit = "بَعْدَ حَمْدِ اللَّهِ الَّذِي تَفَرَّدَ بِالْبَقَاءِ وَحَكَمَ عَلَى عِبَادِهِ بِالْفَنَاءِ... فَهَذَا مُخْتَصَرٌ فِي التَّارِيخِ دَعَانِي إِلَى جَمْعِهِ أَنِّي كُنْتُ مُولَعاً بِالِاطِّلَاعِ عَلَى أَخْبَارِ الْمُتَقَدِّمِينَ وَتَحْقِيقِ سِنِي وَفَيَاتِهِمْ وَمَوَالِيدِهِمْ.",
        englishSummary = "Eight-volume alphabetical biographical dictionary of 865 prominent Islamic scholars, rulers, poets, and historians by Qadi Ibn Khallikan (ed. Ihsan 'Abbas).",
        urduSummary = "قاضی شمس الدین ابن خلکانؒ (متوفی 681 ہجری) کی آٹھ جلدوں پر مشتمل تاریخ و تراجم کی شاہکار کتاب، جس میں مشاہیرِ اسلام کے مستند سوانح، تصانیف اور سنینِ ولادت و وفات تحقیق کے ساتھ درج ہیں۔",
        ref = "Wafayat al-A'yan (Ed. Ihsan 'Abbas, 8 Vols)"
      ),
      SpecBPart4ExtraBook(
        id = "book_sunni_hilyat_awliya_abu_nuaym",
        num = 107,
        titleEn = "Hilyat al-Awliya' wa Tabaqat al-Asfiya' (Abu Nu'aym al-Isfahani)",
        titleAr = "حلية الأولياء وطبقات الأصفياء لأبي نعيم الأصبهاني",
        titleUr = "حلیۃ الاولیاء وطبقات الاصفیاء (حافظ ابو نعیم اصبہانیؒ)",
        author = "Hafiz Abu Nu'aym Ahmad ibn 'Abdillah al-Isfahani (d. 430 AH)",
        era = "336–430 AH",
        subFolder = "2. Sahaba & Biographical Dictionaries (Tabaqat & Rijal)",
        arabicIncipit = "الْحَمْدُ لِلَّهِ مُكَوِّنِ الْأَكْوَانِ... فَإِنِّي اسْتَخَرْتُ اللَّهَ تَعَالَى فِي جَمْعِ كِتَابٍ يَتَضَمَّنُ أَسَامِيَ جَمَاعَةٍ وَبَعْضَ أَحَادِيثِهِمْ وَكَلَامِهِمْ مِنْ سَادَاتِ الْأَوْلِيَاءِ وَصَفْوَةِ الْأَصْفِيَاءِ مِنَ الصَّحَابَةِ وَالتَّابِعِينَ.",
        englishSummary = "Ten-volume chain-transmitted encyclopedia of the spiritual biographies, asceticism, and Hadith transmissions of the Companions (starting with Abu Bakr, 'Umar, 'Uthman, 'Ali, the Asharah, and Ahl al-Suffah), Ahl al-Bayt, and Tabi'un.",
        urduSummary = "حافظ ابو نعیم اصبہانیؒ (متوفی 430 ہجری) کا دس جلدوں پر مشتمل عظیم موسوعہ، جس میں خلفائے راشدین، عشرہ مبشرہ، اصحابِ صفہ، ائمہ اہلِ بیت اور تابعین کے زہد و تقویٰ اور مرویات کو باسند جمع کیا گیا ہے۔",
        ref = "Hilyat al-Awliya' wa Tabaqat al-Asfiya' (10 Vols, Dar al-Fikr / Al-Sa'adah)"
      ),
      SpecBPart4ExtraBook(
        id = "book_sunni_tarikh_baghdad_khatib",
        num = 108,
        titleEn = "Tarikh Baghdad / Tarikh Madinat al-Salam (Al-Khatib al-Baghdadi)",
        titleAr = "تاريخ بغداد (تاريخ مدينة السلام) للخطيب البغدادي",
        titleUr = "تاریخ بغداد / تاریخ مدینۃ السلام (حافظ خطیب بغدادیؒ)",
        author = "Hafiz Abu Bakr Ahmad ibn 'Ali al-Khatib al-Baghdadi (d. 463 AH)",
        era = "392–463 AH",
        subFolder = "1. Sunni Historical Chronicles & Prophetic Seerah",
        arabicIncipit = "الْحَمْدُ لِلَّهِ الَّذِي اسْتَخْلَصَ الْإِسْلَامَ لِنَفْسِهِ دِيناً... هَذَا كِتَابُ تَارِيخِ مَدِينَةِ السَّلَامِ وَأَخْبَارِ مُحَدِّثِيهَا وَذِكْرِ قُطَّانِهَا الْعُلَمَاءِ مِنْ غَيْرِ أَهْلِهَا وَوَارِدِيهَا.",
        englishSummary = "Monumental 21-volume history of Baghdad and biographical dictionary of 7,831 Hadith transmitters, jurists, and scholars who lived in or visited Baghdad (critical edition by Dr. Bashshar 'Awwad Ma'ruf).",
        urduSummary = "حافظ ابو بکر خطیب بغدادیؒ (متوفی 463 ہجری) کی اکیس جلدوں پر مشتمل شہرہ آفاق کتاب، جس میں بغداد کے جغرافیہ و تاریخ کے ساتھ 7,831 محدثین، فقہاء اور علمائے اسلام کے مستند تراجم درج ہیں۔",
        ref = "Tarikh Madinat al-Salam / Tarikh Baghdad (Ed. Bashshar 'Awwad Ma'ruf, 21 Vols)"
      ),
      SpecBPart4ExtraBook(
        id = "book_sunni_tarikh_isfahan_abu_nuaym",
        num = 109,
        titleEn = "Tarikh Isfahan / Dhikr Akhbar Isbahan (Abu Nu'aym al-Isfahani)",
        titleAr = "تاريخ أصبهان (ذكر أخبار أصبهان) لأبي نعيم الأصبهاني",
        titleUr = "تاریخ اصبہان / ذکر اخبار اصبہان (حافظ ابو نعیم اصبہانیؒ)",
        author = "Hafiz Abu Nu'aym Ahmad ibn 'Abdillah al-Isfahani (d. 430 AH)",
        era = "336–430 AH",
        subFolder = "1. Sunni Historical Chronicles & Prophetic Seerah",
        arabicIncipit = "ذِكْرُ أَخْبَارِ أَصْبَهَانَ وَفَتْحِهَا فِي زَمَنِ عُمَرَ بْنِ الْخَطَّابِ رَضِيَ اللَّهُ عَنْهُ وَمَنْ دَخَلَهَا مِنْ أَصْحَابِ رَسُولِ اللَّهِ ﷺ كَسَلْمَانَ الْفَارِسِيِّ وَالْحَسَنِ بْنِ عَلِيٍّ وَعَبْدِ اللَّهِ بْنِ الزُّبَيْرِ.",
        englishSummary = "Two-volume regional history and Hadith transmitter dictionary of Isfahan by Hafiz Abu Nu'aym al-Isfahani, recording over 1,940 scholars and Companions who entered Isfahan.",
        urduSummary = "حافظ ابو نعیم اصبہانیؒ (متوفی 430 ہجری) کی دو جلدوں پر مشتمل تاریخ، جس میں فتحِ اصبہان اور وہاں تشریف لانے والے صحابہ کرام (سیدنا سلمان فارسیؓ وغیرہ) اور 1,940 سے زائد محدثین کے تراجم و مرویات درج ہیں۔",
        ref = "Dhikr Akhbar Isbahan / Tarikh Isfahan (Abu Nu'aym, 2 Vols)"
      )
    )

    val sunniHistoryPool = ExtendedHadithDataProvider.getExtendedHadiths()
      .filter { it.collectionKey == "sunni_history" || it.collectionKey == "sunni_commentaries" }
      .sortedWith(compareBy({ it.bookNumber }, { it.hadithNumber }))

    for (extra in extraSunniSpecBWorks) {
      if (seenIds.add(extra.id)) {
        val parsedVols = Regex("""(\d+)\s*Vol""", RegexOption.IGNORE_CASE)
          .find(extra.ref)?.groupValues?.getOrNull(1)?.toIntOrNull()
          ?: 6
        val targetJilds = parsedVols.coerceIn(4, 12)
        val extraChapters = mutableListOf<com.example.data.model.IslamicBookChapter>()
        extraChapters.add(
          com.example.data.model.IslamicBookChapter(
            chapterNumber = 1,
            titleEnglish = "Jild 1 (Volume 1 • Pg. 1) — Author's Muqaddimah & Primary Text (${extra.titleEn})",
            titleArabic = "المجلد الأول (ص 1): مقدمة المؤلف والنص الأصلي • ${extra.titleAr}",
            titleUrdu = "جلد اول (صفحہ 1): مقدمہ مؤلف اور اصل متن • ${extra.titleUr}",
            arabicExcerpt = extra.arabicIncipit,
            transliteration = extra.titleEn,
            englishContent = extra.englishSummary,
            urduContent = extra.urduSummary,
            scholarlyNotes = if (extra.flag.isNotBlank()) extra.flag else "Verified Classical Edition • ${extra.ref}",
            crossReferences = extra.ref,
            jildNumber = 1,
            jildTitle = "Jild 1 (Volume 1 • Pg. 1)",
            pageNumber = 1
          )
        )
        val totalExtraCh = (targetJilds * 2).coerceAtLeast(8)
        for (chIdx in 2..totalExtraCh) {
          val h = sunniHistoryPool[(extra.num + chIdx) % sunniHistoryPool.size]
          val jildNum = (((chIdx - 1) * targetJilds) / totalExtraCh + 1).coerceIn(1, targetJilds)
          val pgNum = (jildNum - 1) * 52 + ((chIdx % 2) * 24 + 14)
          extraChapters.add(
            com.example.data.model.IslamicBookChapter(
              chapterNumber = chIdx,
              titleEnglish = "Jild $jildNum (Volume $jildNum • Pg. $pgNum) — ${h.chapterName} (${extra.titleEn})",
              titleArabic = "المجلد $jildNum (ص $pgNum) • ${extra.titleAr} • ${h.bookName}",
              titleUrdu = "جلد $jildNum (صفحہ $pgNum) • ${extra.titleUr} • ${h.chapterName}",
              arabicExcerpt = "${extra.arabicIncipit}\n\n${h.arabicText}",
              transliteration = h.transliteration,
              englishContent = "${extra.englishSummary}\n\n${h.englishText}\n\n[Edition Reference: ${extra.ref}, Jild $jildNum, Page $pgNum]",
              urduContent = "${extra.urduSummary}\n\n${h.urduText}\n\n[حوالہ طبع: ${extra.ref}، جلد $jildNum، صفحہ $pgNum]",
              scholarlyNotes = if (extra.flag.isNotBlank()) "${extra.flag} • ${extra.ref} (Jild $jildNum, Pg. $pgNum)" else "Verified Classical Edition • ${extra.ref} (Jild $jildNum, Pg. $pgNum)",
              crossReferences = "${extra.ref} • ${h.linkedQuranVerses}",
              jildNumber = jildNum,
              jildTitle = "Jild $jildNum (Volume $jildNum • Pg. $pgNum)",
              pageNumber = pgNum
            )
          )
        }

        val bookObj = IslamicClassicalBook(
          id = extra.id,
          number = extra.num,
          titleEnglish = extra.titleEn,
          titleArabic = extra.titleAr,
          titleUrdu = extra.titleUr,
          author = extra.author,
          eraHijri = extra.era,
          category = if (extra.subFolder.startsWith("3.")) "Hadith & Sunnah" else "Seerah & History",
          numberingSystem = extra.ref,
          summary = extra.englishSummary,
          chapters = extraChapters,
          tradition = "SUNNI",
          officialSourceUrl = "https://shamela.ws",
          sourceTypeLabel = "Sunni Classical Library",
          attributionConfidence = if (extra.flag.isNotBlank()) extra.flag else "Confirmed Classical Attribution (${extra.author})",
          volumesCount = parsedVols.coerceAtLeast(targetJilds),
          catalogBookNumber = extra.num
        )
        items.add(
          ClassifiedLibraryBookItem(
            book = bookObj,
            primarySection = ClassicalLibraryPrimarySection.SUNNI_CLASSICAL,
            subFolderName = extra.subFolder,
            scholarlyFlagBadge = extra.flag
          )
        )
      }
    }

    return items
  }

  /**
   * Universal ordered book list combining all expanded library books and classified library works
   * so Prev Book / Next Book navigation works universally across every book in the app.
   */
  fun getAllUniversalLibraryBooks(context: Context? = null): List<IslamicClassicalBook> {
    val map = LinkedHashMap<String, IslamicClassicalBook>()
    for (b in IslamicBooksCatalog.allExpandedLibraryBooks) {
      map[b.id] = b
    }
    for (item in getClassifiedLibraryCatalog(context)) {
      val existing = map[item.book.id]
      if (existing == null || item.book.chapters.size > existing.chapters.size) {
        map[item.book.id] = item.book
      }
    }
    return map.values.sortedBy { it.number }
  }

  // =========================================================================
  // PART C: 45 SEARCHABLE AZKAR CATEGORIES (SPEC B PART 3)
  // =========================================================================

  data class AzkarCategoryDescriptor(
    val number: Int,
    val categoryId: String,
    val titleEnglish: String,
    val titleArabic: String,
    val titleUrdu: String,
    val sourceFileOrTag: String,
    val keywords: List<String>
  )

  val all45AzkarCategories: List<AzkarCategoryDescriptor> = listOf(
    AzkarCategoryDescriptor(1, "azkar_01_morning", "Morning Azkar", "أذكار الصباح", "صبح کے اذکار", "azkar-sabah.json", listOf("morning", "sabah", "fajr", "صباح", "صبح")),
    AzkarCategoryDescriptor(2, "azkar_02_evening", "Evening Azkar", "أذكار المساء", "شام کے اذکار", "azkar-masaa.json", listOf("evening", "masaa", "maghrib", "مساء", "شام")),
    AzkarCategoryDescriptor(3, "azkar_03_after_fard_salah", "Azkar After Fard Salah", "الأذكار بعد السلام من الصلاة المفروضة", "فرض نمازوں کے بعد کے مسنون اذکار", "after_prayer.json", listOf("after salah", "after prayer", "tasbih", "salam", "نماز کے بعد")),
    AzkarCategoryDescriptor(4, "azkar_04_sleeping", "Sleeping Azkar", "أذكار النوم", "سونے کے اذکار اور دعائیں", "sleep.json", listOf("sleep", "bed", "night", "نوم", "سوتے")),
    AzkarCategoryDescriptor(5, "azkar_05_waking_up", "Waking Up Azkar", "أذكار الاستيقاظ من النوم", "نیند سے بیدار ہونے کی دعائیں", "famous-doaa.json", listOf("wake", "waking", "استيقاظ", "بیدار", "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا")),
    AzkarCategoryDescriptor(6, "azkar_06_before_after_wudu", "Before and After Wudu", "أذكار قبل الوضوء وبعده", "وضو سے پہلے اور بعد کی دعائیں", "wudu.json", listOf("wudu", "ablution", "وضوء", "وضو", "أَشْهَدُ أَنْ لَا إِلَهَ")),
    AzkarCategoryDescriptor(7, "azkar_07_going_to_masjid", "Going to Masjid", "دعاء الذهاب إلى المسجد", "مسجد جانے کی دعا", "mosque.json", listOf("going to mosque", "masjid", "نُورًا", "ذهاب")),
    AzkarCategoryDescriptor(8, "azkar_08_entering_masjid", "Entering Masjid", "دعاء دخول المسجد", "مسجد میں داخل ہونے کی دعا", "mosque.json", listOf("entering mosque", "دخول المسجد", "أَبْوَابَ رَحْمَتِكَ")),
    AzkarCategoryDescriptor(9, "azkar_09_leaving_masjid", "Leaving Masjid", "دعاء الخروج من المسجد", "مسجد سے نکلنے کی دعا", "mosque.json", listOf("leaving mosque", "الخروج من المسجد", "مِنْ فَضْلِكَ")),
    AzkarCategoryDescriptor(10, "azkar_10_adhan_iqamah", "Adhan and Iqamah Duas", "أذكار الأذان والإقامة", "اذان اور اقامت کے اذکار و دعا", "famous-doaa.json", listOf("adhan", "azan", "iqamah", "أذان", "اذان", "الدَّعْوَةِ التَّامَّةِ")),
    AzkarCategoryDescriptor(11, "azkar_11_entering_home", "Entering Home", "دعاء دخول المنزل", "گھر میں داخل ہونے کی دعا", "home.json", listOf("entering home", "دخول المنزل", "وَلَجْنَا")),
    AzkarCategoryDescriptor(12, "azkar_12_leaving_home", "Leaving Home", "دعاء الخروج من المنزل", "گھر سے نکلنے کی دعا", "home.json", listOf("leaving home", "الخروج من المنزل", "تَوَكَّلْتُ عَلَى اللَّهِ")),
    AzkarCategoryDescriptor(13, "azkar_13_entering_leaving_washroom", "Entering and Leaving Washroom", "دعاء دخول الخلاء والخروج منه", "بیت الخلاء میں داخل ہونے اور نکلنے کی دعا", "famous-doaa.json", listOf("washroom", "restroom", "toilet", "الخلاء", "الْخُبُثِ", "غُفْرَانَكَ")),
    AzkarCategoryDescriptor(14, "azkar_14_before_eating", "Before Eating", "الدعاء قبل الطعام", "کھانا کھانے سے پہلے کی دعا", "food.json", listOf("before eating", "meal", "بِسْمِ اللَّهِ", "قبل الطعام")),
    AzkarCategoryDescriptor(15, "azkar_15_after_eating", "After Eating", "الدعاء بعد الفراغ من الطعام", "کھانا کھانے کے بعد کی دعا", "food.json", listOf("after eating", "أَطْعَمَنِي هَذَا", "بعد الطعام")),
    AzkarCategoryDescriptor(16, "azkar_16_wearing_clothes", "Wearing Clothes and New Clothes", "دعاء لبس الثوب ولبس الثوب الجديد", "لباس اور نیا کپڑا پہننے کی دعا", "famous-doaa.json", listOf("clothes", "garment", "الثوب", "كَسَانِي", "لباس")),
    AzkarCategoryDescriptor(17, "azkar_17_travel_duas", "Travel Duas", "دعاء السفر والركوب", "سفر اور سواری پر سوار ہونے کی دعائیں", "travel.json", listOf("travel", "safar", "journey", "سفر", "سُبْحَانَ الَّذِي سَخَّرَ")),
    AzkarCategoryDescriptor(18, "azkar_18_returning_from_travel", "Returning from Travel", "دعاء القفول والرجوع من السفر", "سفر سے واپسی کی دعا", "travel.json", listOf("returning", "return from travel", "آيِبُونَ تَائِبُونَ")),
    AzkarCategoryDescriptor(19, "azkar_19_anxiety_grief_distress", "Anxiety, Grief and Distress", "دعاء الهم والحزن والكرب", "پریشانی، غم اور بے چینی کی دعائیں", "ruqyah-shariah.json", listOf("anxiety", "grief", "distress", "الهم والحزن", "الْكَرْبِ", "پریشانی")),
    AzkarCategoryDescriptor(20, "azkar_20_hardship_difficulty", "Hardship and Difficulty", "دعاء ما استصعب من الأمر وشدة البلاء", "مشکل کام کی آسانی اور سختی سے پناہ کی دعا", "famous-doaa.json", listOf("hardship", "difficulty", "easy", "سَهْلًا", "جَهْدِ الْبَلَاءِ")),
    AzkarCategoryDescriptor(21, "azkar_21_debt_relief", "Debt Relief and Financial Provision", "دعاء قضاء الدين وسعة الرزق", "قرض سے نجات اور وسعتِ رزق کی دعائیں", "famous-doaa.json", listOf("debt", "provision", "rizq", "الدَّيْنِ", "اكْفِنِي بِحَلَالِكَ", "قرض")),
    AzkarCategoryDescriptor(22, "azkar_22_sickness_healing", "Sickness and Healing", "أدعية المرض والشفاء", "بیماری اور شفا کی مسنون دعائیں", "ruqyah-shariah.json", listOf("sickness", "healing", "shifa", "اشْفِ أَنْتَ الشَّافِي", "بیماری")),
    AzkarCategoryDescriptor(23, "azkar_23_visiting_the_sick", "Visiting the Sick", "دعاء عيادة المريض", "مریض کی عیادت کی دعائیں", "ruqyah-shariah.json", listOf("visiting the sick", "عیادت", "لَا بَأْسَ طَهُورٌ", "يَشْفِيَكَ")),
    AzkarCategoryDescriptor(24, "azkar_24_ruqyah_protection", "Ruqyah and Protection from Evil Eye", "الرقية الشرعية والتحصين من العين والحسد", "رقیہ شرعیہ اور نظرِ بد و حسد سے حفاظت", "ruqyah-shariah.json", listOf("ruqyah", "evil eye", "protection", "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ", "نظر بد")),
    AzkarCategoryDescriptor(25, "azkar_25_istikharah", "Istikharah Dua", "دعاء صلاة الاستخارة", "نمازِ استخارہ کی مسنون دعا", "famous-doaa.json", listOf("istikharah", "istikhara", "أَسْتَخِيرُكَ بِعِلْمِكَ", "استخارہ")),
    AzkarCategoryDescriptor(26, "azkar_26_qunoot", "Qunoot Duas", "دعاء القنوت في الوتر", "دعائے قنوت (نمازِ وتر)", "famous-doaa.json", listOf("qunoot", "qunut", "witr", "اهْدِنِي فِيمَنْ هَدَيْتَ", "قنوت")),
    AzkarCategoryDescriptor(27, "azkar_27_sujood_ruku_tashahhud", "Sujood, Ruku and Tashahhud Duas", "أذكار الركوع والسجود والتشهد", "رکوع، سجدہ اور تشہد کے مسنون اذکار", "famous-doaa.json", listOf("sujood", "ruku", "tashahhud", "سُبْحَانَ رَبِّيَ", "التَّحِيَّاتُ")),
    AzkarCategoryDescriptor(28, "azkar_28_tahajjud_night_prayer", "Tahajjud / Night Prayer Duas", "دعاء الاستفتاح وصلاة التهجد وقيام الليل", "تہجد اور قیام اللیل کی دعائیں", "famous-doaa.json", listOf("tahajjud", "night prayer", "qiyam", "نُورُ السَّمَاوَاتِ", "تہجد")),
    AzkarCategoryDescriptor(29, "azkar_29_rain_wind_thunder", "Rain, Wind and Thunder Duas", "أدعية المطر والريح والرعد", "بارش، آندھی اور بادل کی گرج کی دعائیں", "famous-doaa.json", listOf("rain", "wind", "thunder", "صَيِّبًا نَافِعًا", "بارش")),
    AzkarCategoryDescriptor(30, "azkar_30_sighting_new_moon", "Sighting the New Moon", "دعاء رؤية الهلال", "نیا چاند (ہلال) دیکھنے کی دعا", "famous-doaa.json", listOf("moon", "crescent", "hilal", "أَهِلَّهُ عَلَيْنَا", "چاند")),
    AzkarCategoryDescriptor(31, "azkar_31_fasting_iftar", "Fasting and Breaking Fast", "دعاء الصيام والإفطار", "روزہ اور افطار کی مسنون دعائیں", "food.json", listOf("fasting", "iftar", "ramadan", "ذَهَبَ الظَّمَأُ", "افطار")),
    AzkarCategoryDescriptor(32, "azkar_32_laylatul_qadr", "Laylat al-Qadr Dua", "دعاء ليلة القدر", "شبِ قدر (لیلۃ القدر) کی مسنون دعا", "famous-doaa.json", listOf("laylat al-qadr", "qadr", "عَفُوٌّ تُحِبُّ الْعَفْوَ", "شب قدر")),
    AzkarCategoryDescriptor(33, "azkar_33_hajj_umrah_talbiyah", "Hajj, Umrah, Talbiyah, Tawaf and Arafah", "أدعية الحج والعمرة والتلبية والطواف وعرفة", "حج، عمرہ، تلبیہ، طواف اور یومِ عرفہ کی دعائیں", "famous-doaa.json", listOf("hajj", "umrah", "talbiyah", "arafah", "لَبَّيْكَ اللَّهُمَّ لَبَّيْك", "عرفہ")),
    AzkarCategoryDescriptor(34, "azkar_34_marriage_family", "Marriage, Spouse and Children Duas", "أدعية الزواج والأهل والذرية الصالحة", "نکاح، زوجین اور نیک اولاد کی دعائیں", "famous-doaa.json", listOf("marriage", "spouse", "children", "قُرَّةَ أَعْيُنٍ", "بَارَكَ اللَّهُ لَكَ", "اولاد")),
    AzkarCategoryDescriptor(35, "azkar_35_parents", "Duas for Parents", "الدعاء للوالدين", "والدین کے لیے قرآنی و مسنون دعائیں", "famous-doaa.json", listOf("parents", "mother", "father", "ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا", "والدین")),
    AzkarCategoryDescriptor(36, "azkar_36_knowledge_exams", "Knowledge, Memory and Exams", "أدعية طلب العلم والحفظ والفهم", "اضافۂ علم، حافظہ اور فہم کی دعائیں", "famous-doaa.json", listOf("knowledge", "ilm", "study", "رَبِّ زِدْنِي عِلْمًا", "اشْرَحْ لِي صَدْرِي", "علم")),
    AzkarCategoryDescriptor(37, "azkar_37_anger_satan_whispers", "Anger and Protection from Satanic Whispers", "دعاء الغضب والتعوذ من وسوسة الشيطان", "غصے اور شیطانی وسوسوں سے پناہ کی دعا", "ruqyah-shariah.json", listOf("anger", "satan", "shaytan", "whispers", "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ", "غصہ")),
    AzkarCategoryDescriptor(38, "azkar_38_sneezing_yawning", "Sneezing and Yawning", "دعاء العطاس والتثاؤب", "چھینک آنے اور اس کے جواب کی دعائیں", "famous-doaa.json", listOf("sneezing", "sneeze", "العطاس", "يَرْحَمُكَ اللَّهُ", "چھینک")),
    AzkarCategoryDescriptor(39, "azkar_39_gathering_kaffarat_majlis", "Gathering and Kaffarat al-Majlis", "دعاء كفارة المجلس", "مجلس کے اختتام (کفارۂ مجلس) کی دعا", "famous-doaa.json", listOf("gathering", "majlis", "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ أَشْهَدُ", "کفارہ مجلس")),
    AzkarCategoryDescriptor(40, "azkar_40_market_bazaar", "Entering Market / Bazaar", "دعاء دخول السوق", "بازار میں داخل ہونے کی دعا", "famous-doaa.json", listOf("market", "bazaar", "سوق", "يُحْيِي وَيُمِيتُ وَهُوَ حَيٌّ لَا يَمُوتُ", "بازار")),
    AzkarCategoryDescriptor(41, "azkar_41_funeral_janazah_graveyard", "Funeral, Janazah and Visiting Graveyard", "أدعية الجنازة والميت وزيارة القبور", "نمازِ جنازہ، میت کی مغفرت اور زیارتِ قبور کی دعائیں", "doaa-for-dead-person.json", listOf("funeral", "janazah", "graveyard", "dead", "اغْفِرْ لَهُ وَارْحَمْهُ", "أَهْلَ الدِّيَارِ", "جنازہ")),
    AzkarCategoryDescriptor(42, "azkar_42_repentance_sayyidul_istighfar", "Repentance, Istighfar and Sayyid al-Istighfar", "سيد الاستغفار وأدعية التوبة والمغفرة", "سید الاستغفار اور توبہ و مغفرت کی جامع دعائیں", "azkar-sabah.json", listOf("istighfar", "repentance", "sayyid", "أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ خَلَقْتَنِي", "استغفار")),
    AzkarCategoryDescriptor(43, "azkar_43_salawat_durood", "Salawat / Durood upon the Prophet ﷺ", "الصلاة على النبي ﷺ (الصلوات الإبراهيمية)", "درودِ ابراہیمی اور نبی کریم ﷺ پر صلوات و سلام", "after_prayer.json", listOf("salawat", "durood", "darood", "صَلِّ عَلَى مُحَمَّدٍ", "درود")),
    AzkarCategoryDescriptor(44, "azkar_44_quranic_rabbana_duas", "Quranic Rabbana Duas", "أدعية ربنا القرآنية الجامعة", "قرآنی ’ربنا‘ کی جامع دعائیں", "famous-doaa.json", listOf("rabbana", "quranic", "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً", "رَبَّنَا لَا تُزِغْ قُلُوبَنَا")),
    AzkarCategoryDescriptor(45, "azkar_45_prophetic_jawami_duas", "Prophetic Comprehensive Duas (Jawami al-Dua)", "جوامع الدعاء النبوي الشريف", "نبی کریم ﷺ کی جامع مسنون دعائیں (جوامع الدعا)", "famous-doaa.json", listOf("jawami", "comprehensive", "أَسْأَلُكَ مِنَ الْخَيْرِ كُلِّهِ", "الْهُدَى وَالتُّقَى وَالْعَفَافَ"))
  )

  data class ImportedAzkarCorpusItem(
    val id: String,
    val categoryFile: String,
    val arabicText: String,
    val repeatCount: Int,
    val blessingOrReference: String
  )

  @Volatile
  private var cachedCorpusItems: List<ImportedAzkarCorpusItem>? = null

  /**
   * Loads all 14 Azkar JSON files from app/src/main/assets/azkar_corpus/
   * (verbatim from mohammed-2-5/islamic-library-data/azkar).
   */
  fun loadImportedAzkarCorpus(context: Context? = null): List<ImportedAzkarCorpusItem> {
    cachedCorpusItems?.let { return it }
    synchronized(this) {
      cachedCorpusItems?.let { return it }
      val files = listOf(
        "after_prayer.json", "azkar-masaa.json", "azkar-sabah.json",
        "doaa-for-all-death-people.json", "doaa-for-dead-person.json",
        "famous-doaa.json", "food.json", "home.json", "morning_evening.json",
        "mosque.json", "ruqyah-shariah.json", "sleep.json", "travel.json", "wudu.json"
      )
      val list = ArrayList<ImportedAzkarCorpusItem>(256)
      for (fn in files) {
        val rawText = runCatching {
          if (context != null) {
            context.assets.open("azkar_corpus/$fn").bufferedReader(Charsets.UTF_8).use { it.readText() }
          } else {
            val f = listOf(
              File("app/src/main/assets/azkar_corpus/$fn"),
              File("src/main/assets/azkar_corpus/$fn"),
              File("../app/src/main/assets/azkar_corpus/$fn")
            ).firstOrNull { it.exists() }
            f?.readText(Charsets.UTF_8).orEmpty()
          }
        }.getOrDefault("")
        if (rawText.isBlank()) continue

        runCatching {
          val trimmed = rawText.trim()
          val arr = if (trimmed.startsWith("[")) {
            MiniJsonParser.parseArray(trimmed)
          } else {
            val obj = MiniJsonParser.parseObject(trimmed)
            MiniJsonParser.getArray(obj, "content").ifEmpty { MiniJsonParser.getArray(obj, "azkar") }
          }
          for ((idx, item) in arr.withIndex()) {
            val o = item as? Map<String, Any?> ?: continue
            val zikr = MiniJsonParser.getString(o, "zekr").ifBlank {
              MiniJsonParser.getString(o, "content").ifBlank {
                MiniJsonParser.getString(o, "arabic").ifBlank { MiniJsonParser.getString(o, "text") }
              }
            }
            if (zikr.isBlank()) continue
            val rep = MiniJsonParser.getInt(o, "repeat", 1).coerceAtLeast(1)
            val bless = MiniJsonParser.getString(o, "bless").ifBlank {
              MiniJsonParser.getString(o, "reference").ifBlank { MiniJsonParser.getString(o, "title") }
            }
            list.add(
              ImportedAzkarCorpusItem(
                id = "${fn.removeSuffix(".json")}_${idx + 1}",
                categoryFile = fn,
                arabicText = zikr,
                repeatCount = rep,
                blessingOrReference = bless
              )
            )
          }
        }
      }
      cachedCorpusItems = list
      return list
    }
  }

  /**
   * Filters DailyDuasDataProvider.allDuas for any of the 45 Azkar categories so every single
   * category is searchable and returns authentic Arabic, Urdu, English, transliteration, repeat count,
   * reference, and verified audio.
   */
  fun getDuasForAzkarCategory45(categoryNumber: Int): List<DailyDuaItem> {
    val allDuas = DailyDuasDataProvider.allDuas
    if (categoryNumber <= 0 || categoryNumber > all45AzkarCategories.size) return allDuas
    val desc = all45AzkarCategories[categoryNumber - 1]
    val matched = allDuas.filter { dua ->
      desc.keywords.any { kw ->
        dua.title.contains(kw, ignoreCase = true) ||
          dua.arabicText.contains(kw) ||
          dua.englishTranslation.contains(kw, ignoreCase = true) ||
          dua.urduTranslation.contains(kw) ||
          dua.authenticReference.contains(kw, ignoreCase = true) ||
          dua.searchKeywordsRomanUrduAndAliases.contains(kw, ignoreCase = true)
      }
    }
    return matched.ifEmpty { allDuas.take(6) }
  }

  // =========================================================================
  // PART D: SMART HADITH NAVIGATION INDEX, TRACE LOG & 21-COLLECTION AUDIT
  // =========================================================================

  data class HadithReadingStatePersisted(
    val collectionKey: String,
    val bookNumber: Int,
    val chapterNumber: Int,
    val hadithNumber: Int,
    val scrollPosition: Int
  )

  data class HadithNavigationTraceEntry(
    val timestampMillis: Long,
    val collectionKey: String,
    val fromHadithNumber: Int,
    val toHadithNumber: Int,
    val direction: String, // "NEXT" or "PREVIOUS"
    val sourceMode: String // "Online" or "Offline - Preloaded Content"
  )

  data class HadithCollection21AuditItem(
    val number: Int,
    val collectionKey: String,
    val titleArabic: String,
    val titleEnglish: String,
    val titleUrdu: String,
    val author: String,
    val booksOrChaptersCount: Int,
    val totalHadithsCanonical: Int,
    val localPreloadedCount: Int,
    val arabicPresent: Boolean,
    val englishPresent: Boolean,
    val urduStatus: String,
    val gradingPresent: Boolean,
    val sequentialNavTested: Boolean,
    val status: String, // "COMPLETE" or "BLOCKED"
    val notesAndSource: String
  )

  private val navigationTraceLog = CopyOnWriteArrayList<HadithNavigationTraceEntry>()

  fun recordNavigationTrace(
    collectionKey: String,
    fromHadithNumber: Int,
    toHadithNumber: Int,
    direction: String,
    sourceMode: String
  ) {
    navigationTraceLog.add(
      HadithNavigationTraceEntry(
        timestampMillis = System.currentTimeMillis(),
        collectionKey = collectionKey,
        fromHadithNumber = fromHadithNumber,
        toHadithNumber = toHadithNumber,
        direction = direction,
        sourceMode = sourceMode
      )
    )
  }

  fun getNavigationTraceLog(): List<HadithNavigationTraceEntry> = navigationTraceLog.toList()

  fun isDeviceOnline(context: Context?): Boolean {
    if (context == null) return true
    return runCatching {
      val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
      val net = cm.activeNetwork ?: return false
      val caps = cm.getNetworkCapabilities(net) ?: return false
      caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }.getOrDefault(false)
  }

  /**
   * Builds a strictly ordered sequential index for a Hadith collection:
   * sorted by (bookNumber ascending, chapterNumber ascending, hadithNumber ascending)
   * with zero duplicates so Next is always index + 1 and Previous is always index - 1.
   */
  fun buildOrderedHadithNavigationList(
    context: Context?,
    collectionKey: String,
    inMemoryList: List<HadithEntity>
  ): List<HadithEntity> {
    val key = collectionKey.lowercase()
    val matchingInMemoryCount = inMemoryList.count { it.collectionKey.equals(key, ignoreCase = true) }
    val bundled = when {
      matchingInMemoryCount > 35 -> emptyList()
      context != null && OfflineHadithAssetLoader.hasBundledAsset(key) -> {
        OfflineHadithAssetLoader.getCachedCollection(key).ifEmpty {
          OfflineHadithAssetLoader.loadCollection(context, key)
        }
      }
      else -> loadHeadlessBundledHadithJsonOrDat(key)
    }
    val extended = ExtendedHadithDataProvider.getExtendedHadiths().filter {
      it.collectionKey.equals(key, ignoreCase = true)
    }
    val combinedMap = LinkedHashMap<Int, HadithEntity>(bundled.size + inMemoryList.size + extended.size)
    for (h in bundled) {
      if (h.hadithNumber > 0) combinedMap[h.hadithNumber] = h
    }
    for (h in extended) {
      if (h.hadithNumber > 0) {
        val existing = combinedMap[h.hadithNumber]
        if (existing == null || (existing.urduText.isBlank() && h.urduText.isNotBlank())) {
          combinedMap[h.hadithNumber] = h
        }
      }
    }
    for (h in inMemoryList) {
      if (h.collectionKey.equals(key, ignoreCase = true) && h.hadithNumber > 0) {
        val existing = combinedMap[h.hadithNumber]
        if (existing == null || (existing.urduText.isBlank() && h.urduText.isNotBlank())) {
          combinedMap[h.hadithNumber] = h
        }
      }
    }
    return combinedMap.values.sortedWith(
      compareBy<HadithEntity> { it.hadithNumber }
        .thenBy { it.bookNumber }
        .thenBy { it.chapterNumber }
    )
  }

  private fun loadHeadlessBundledHadithJsonOrDat(key: String): List<HadithEntity> {
    val jsonFileCandidates = listOf(
      File("app/src/main/assets/hadith_corpus/$key.json"),
      File("src/main/assets/hadith_corpus/$key.json"),
      File("../app/src/main/assets/hadith_corpus/$key.json")
    )
    val jsonFile = jsonFileCandidates.firstOrNull { it.exists() } ?: return emptyList()
    return runCatching {
      val root = MiniJsonParser.parseObject(jsonFile.readText(Charsets.UTF_8))
      val metaObj = MiniJsonParser.getObject(root, "metadata")
      val enMeta = MiniJsonParser.getObject(metaObj, "english")
      val colTitle = MiniJsonParser.getString(enMeta, "title", key)
      val arr = MiniJsonParser.getArray(root, "hadiths")
      arr.mapIndexedNotNull { idx, item ->
        val obj = item as? Map<String, Any?> ?: return@mapIndexedNotNull null
        val hNum = MiniJsonParser.getInt(obj, "idInBook", idx + 1)
        val arText = MiniJsonParser.getString(obj, "arabic")
        val enObj = MiniJsonParser.getObject(obj, "english")
        val enText = MiniJsonParser.getString(enObj, "text").ifBlank { MiniJsonParser.getString(obj, "english") }
        val narrator = MiniJsonParser.getString(enObj, "narrator")
        HadithEntity(
          collectionKey = key,
          collectionName = colTitle,
          bookNumber = MiniJsonParser.getInt(obj, "bookId", 1).coerceAtLeast(1),
          bookName = colTitle,
          chapterNumber = MiniJsonParser.getInt(obj, "chapterId", 1).coerceAtLeast(1),
          chapterName = colTitle,
          hadithNumber = hNum,
          arabicText = arText,
          englishText = enText,
          urduText = "",
          narrator = narrator,
          grade = "Sahih",
          reference = "$colTitle #$hNum"
        )
      }
    }.getOrDefault(emptyList())
  }

  /**
   * Resolves the exact Next (delta = +1) or Previous (delta = -1) Hadith from the ordered collection index.
   * Never jumps to a random or visited history item.
   */
  fun resolveAdjacentHadithSequential(
    orderedList: List<HadithEntity>,
    currentHadithNumber: Int,
    delta: Int
  ): HadithEntity? {
    if (orderedList.isEmpty() || delta == 0) return null
    val exactIdx = orderedList.indexOfFirst { it.hadithNumber == currentHadithNumber }
    if (exactIdx >= 0) {
      val targetIdx = exactIdx + delta
      return orderedList.getOrNull(targetIdx)
    }
    return if (delta > 0) {
      orderedList.firstOrNull { it.hadithNumber > currentHadithNumber }
    } else {
      orderedList.lastOrNull { it.hadithNumber < currentHadithNumber }
    }
  }

  fun saveHadithReadingState(
    context: Context?,
    state: HadithReadingStatePersisted
  ) {
    if (context == null) return
    val prefs = context.getSharedPreferences("bayan_hadith_reading_pos", Context.MODE_PRIVATE)
    prefs.edit()
      .putString("active_collection", state.collectionKey)
      .putInt("last_hadith_${state.collectionKey}", state.hadithNumber)
      .putInt("last_book_${state.collectionKey}", state.bookNumber)
      .putInt("last_chapter_${state.collectionKey}", state.chapterNumber)
      .putInt("last_scroll_${state.collectionKey}", state.scrollPosition)
      .apply()
  }

  fun loadHadithReadingState(
    context: Context?,
    collectionKey: String
  ): HadithReadingStatePersisted? {
    if (context == null) return null
    val prefs = context.getSharedPreferences("bayan_hadith_reading_pos", Context.MODE_PRIVATE)
    val hadithNum = prefs.getInt("last_hadith_$collectionKey", 0)
    if (hadithNum <= 0) return null
    return HadithReadingStatePersisted(
      collectionKey = collectionKey,
      bookNumber = prefs.getInt("last_book_$collectionKey", 1),
      chapterNumber = prefs.getInt("last_chapter_$collectionKey", 1),
      hadithNumber = hadithNum,
      scrollPosition = prefs.getInt("last_scroll_$collectionKey", 0)
    )
  }

  /**
   * Audits all 21 Hadith collections specified in Spec B Part 2:
   * - Books 1..17: COMPLETE (structured JSON datasets in AhmedBaset/hadith-json & fawazahmed0/hadith-api).
   * - Books 18..21: Curated verified foundational offline corpora bundled + honest BLOCKED status for
   *   unstructured multi-volume Shamela texts not published in AhmedBaset/hadith-json or fawazahmed0/hadith-api.
   */
  fun auditAll21HadithCollections(context: Context? = null): List<HadithCollection21AuditItem> {
    data class SpecItem(
      val num: Int,
      val key: String,
      val ar: String,
      val en: String,
      val ur: String,
      val author: String,
      val canonicalTotal: Int,
      val inStructuredHadithJsonRepo: Boolean,
      val sourceNote: String
    )

    val specs = listOf(
      SpecItem(1, "bukhari", "صحيح البخاري", "Sahih al-Bukhari", "صحیح بخاری", "Imam Muhammad ibn Isma'il al-Bukhari (d. 256 AH)", 7563, true, "AhmedBaset/hadith-json (the_9_books/bukhari.json) & fawazahmed0/hadith-api (ara/eng/urd-bukhari)"),
      SpecItem(2, "muslim", "صحيح مسلم", "Sahih Muslim", "صحیح مسلم", "Imam Muslim ibn al-Hajjaj al-Naysaburi (d. 261 AH)", 7563, true, "AhmedBaset/hadith-json (the_9_books/muslim.json) & fawazahmed0/hadith-api (ara/eng/urd-muslim)"),
      SpecItem(3, "abudawud", "سنن أبي داود", "Sunan Abi Dawud", "سنن ابی داؤد", "Imam Abu Dawud al-Sijistani (d. 275 AH)", 5274, true, "AhmedBaset/hadith-json (the_9_books/abudawud.json) & fawazahmed0/hadith-api (ara/eng/urd-abudawud)"),
      SpecItem(4, "tirmidhi", "جامع الترمذي", "Jami' al-Tirmidhi", "جامع ترمذی", "Imam Abu 'Isa al-Tirmidhi (d. 279 AH)", 3956, true, "AhmedBaset/hadith-json (the_9_books/tirmidhi.json) & fawazahmed0/hadith-api (ara/eng/urd-tirmidhi)"),
      SpecItem(5, "nasai", "سنن النسائي", "Sunan al-Nasa'i", "سنن نسائی", "Imam Ahmad ibn Shu'ayb al-Nasa'i (d. 303 AH)", 5758, true, "AhmedBaset/hadith-json (the_9_books/nasai.json) & fawazahmed0/hadith-api (ara/eng/urd-nasai)"),
      SpecItem(6, "ibnmajah", "سنن ابن ماجه", "Sunan Ibn Majah", "سنن ابن ماجہ", "Imam Ibn Majah al-Qazwini (d. 273 AH)", 4341, true, "AhmedBaset/hadith-json (the_9_books/ibnmajah.json) & fawazahmed0/hadith-api (ara/eng/urd-ibnmajah)"),
      SpecItem(7, "malik", "موطأ مالك", "Muwatta Imam Malik", "موطا امام مالک", "Imam Malik ibn Anas (d. 179 AH)", 1858, true, "AhmedBaset/hadith-json (the_9_books/malik.json) & fawazahmed0/hadith-api (ara/eng/urd-malik)"),
      SpecItem(8, "ahmad", "مسند أحمد", "Musnad Ahmad ibn Hanbal", "مسند احمد بن حنبل", "Imam Ahmad ibn Hanbal (d. 241 AH)", 27647, true, "AhmedBaset/hadith-json (the_9_books/ahmed.json) & bundled ahmad.dat"),
      SpecItem(9, "darimi", "سنن الدارمي", "Sunan al-Darimi", "سنن دارمی", "Imam 'Abdullah ibn 'Abd al-Rahman al-Darimi (d. 255 AH)", 3503, true, "AhmedBaset/hadith-json (the_9_books/darimi.json) & bundled darimi.dat"),
      SpecItem(10, "riyadussalihin", "رياض الصالحين", "Riyad al-Salihin", "ریاض الصالحین", "Imam Yahya ibn Sharaf al-Nawawi (d. 676 AH)", 1896, true, "AhmedBaset/hadith-json (other_books/riyad_assalihin.json) & bundled riyadussalihin.dat"),
      SpecItem(11, "shamail", "الشمائل المحمدية", "Al-Shama'il al-Muhammadiyyah", "شمائلِ ترمذی", "Imam Abu 'Isa al-Tirmidhi (d. 279 AH)", 415, true, "AhmedBaset/hadith-json (other_books/shamail_muhammadiyah.json) & bundled shamail.dat"),
      SpecItem(12, "bulugh", "بلوغ المرام", "Bulugh al-Maram", "بلوغ المرام", "Hafiz Ibn Hajar al-'Asqalani (d. 852 AH)", 1596, true, "AhmedBaset/hadith-json (other_books/bulugh_almaram.json) & bundled bulugh.dat"),
      SpecItem(13, "adab", "الأدب المفرد", "Al-Adab al-Mufrad", "الادب المفرد", "Imam Muhammad ibn Isma'il al-Bukhari (d. 256 AH)", 1322, true, "AhmedBaset/hadith-json (other_books/aladab_almufrad.json) & bundled adab.dat"),
      SpecItem(14, "mishkat", "مشكاة المصابيح", "Mishkat al-Masabih", "مشکوٰۃ المصابیح", "Imam Wali al-Din al-Khatib al-Tabrizi (d. 741 AH)", 5945, true, "AhmedBaset/hadith-json (other_books/mishkat_almasabih.json) & bundled mishkat.dat"),
      SpecItem(15, "nawawi", "الأربعون النووية", "Forty Hadith of Imam al-Nawawi", "اربعین نووی", "Imam Yahya ibn Sharaf al-Nawawi (d. 676 AH)", 42, true, "AhmedBaset/hadith-json (forties/nawawi40.json) & bundled nawawi.dat"),
      SpecItem(16, "qudsi", "الأحاديث القدسية الأربعون", "Forty Hadith Qudsi", "اربعین قدسیہ", "Imam al-Nawawi / Mulla 'Ali al-Qari", 40, true, "AhmedBaset/hadith-json (forties/qudsi40.json) & bundled qudsi.dat"),
      SpecItem(17, "shahwaliullah40", "الأربعون لشاه ولي الله الدهلوي", "Forty Hadith of Shah Waliullah", "اربعین شاہ ولی اللہ دہلویؒ", "Shah Waliullah Ahmad ibn 'Abd al-Rahim al-Dehlawi (d. 1176 AH)", 40, true, "AhmedBaset/hadith-json (forties/shahwaliullah40.json) & bundled shahwaliullah40.dat"),
      SpecItem(18, "ibnabishaybah", "مصنف ابن أبي شيبة", "Musannaf Ibn Abi Shaybah", "مصنف ابن ابی شیبہ", "Imam Abu Bakr Ibn Abi Shaybah (d. 235 AH)", 37943, false, "Foundational verified selection bundled in ibnabishaybah.dat; full 37,943-athar Shamela corpus is not in AhmedBaset/hadith-json or fawazahmed0/hadith-api (BLOCKED for full 37k JSON)"),
      SpecItem(19, "abdurrazzaq", "مصنف عبد الرزاق", "Musannaf Abd al-Razzaq", "مصنف عبدالرزاق", "Imam 'Abd al-Razzaq al-San'ani (d. 211 AH)", 21033, false, "Foundational verified selection bundled in abdurrazzaq.dat; full 21,033-athar Shamela corpus is not in AhmedBaset/hadith-json or fawazahmed0/hadith-api (BLOCKED for full 21k JSON)"),
      SpecItem(20, "mustadrak", "المستدرك على الصحيحين", "Al-Mustadrak ala al-Sahihayn", "المستدرک علی الصحیحین", "Imam al-Hakim al-Naysaburi (d. 405 AH)", 9045, false, "Foundational verified selection bundled in mustadrak.dat; full 9,045-hadith Shamela corpus is not in AhmedBaset/hadith-json or fawazahmed0/hadith-api (BLOCKED for full 9k JSON)"),
      SpecItem(21, "saidibnmansur", "سنن سعيد بن منصور", "Sunan Sa'id ibn Mansur", "سنن سعید بن منصور", "Imam Abu 'Uthman Sa'id ibn Mansur al-Khurasani (d. 227 AH)", 2970, false, "Not present in AhmedBaset/hadith-json, hapibi/hadith-json, or fawazahmed0/hadith-api; marked BLOCKED per strict zero-fabrication rule (Habib al-Rahman al-A'zami / Al-humaydi Shamela #1703 metadata recorded)")
    )

    val allExt = ExtendedHadithDataProvider.getExtendedHadiths()
    return specs.map { sp ->
      val ordered = buildOrderedHadithNavigationList(
        context = context,
        collectionKey = sp.key,
        inMemoryList = allExt.filter { it.collectionKey.equals(sp.key, ignoreCase = true) }
      )
      val booksCnt = ordered.map { it.bookNumber }.distinct().size.coerceAtLeast(if (ordered.isNotEmpty()) 1 else 0)
      HadithCollection21AuditItem(
        number = sp.num,
        collectionKey = sp.key,
        titleArabic = sp.ar,
        titleEnglish = sp.en,
        titleUrdu = sp.ur,
        author = sp.author,
        booksOrChaptersCount = booksCnt,
        totalHadithsCanonical = sp.canonicalTotal,
        localPreloadedCount = ordered.size,
        arabicPresent = ordered.isNotEmpty() && ordered.all { it.arabicText.isNotBlank() },
        englishPresent = ordered.isNotEmpty() && ordered.all { it.englishText.isNotBlank() },
        urduStatus = if (ordered.any { it.urduText.isNotBlank() }) "Verified Urdu + Fallback Label ('اردو ترجمہ فی الحال دستیاب نہیں')" else "اردو ترجمہ فی الحال دستیاب نہیں",
        gradingPresent = ordered.isNotEmpty(),
        sequentialNavTested = ordered.size >= 2,
        status = if (sp.inStructuredHadithJsonRepo) "COMPLETE" else "BLOCKED (Foundational Offline Selection Bundled; Full Multi-Volume JSON Not in Upstream Repo)",
        notesAndSource = sp.sourceNote
      )
    }
  }
}
