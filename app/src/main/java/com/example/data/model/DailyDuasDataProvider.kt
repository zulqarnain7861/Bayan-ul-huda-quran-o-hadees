package com.example.data.model

enum class DuaCategory(val label: String, val arabicLabel: String) {
  MORNING_EVENING("Morning & Evening", "أذكار الصباح والمساء"),
  AFTER_SALAH("After Salah (Namaz)", "أذكار بعد الصلاة"),
  SLEEP_WAKE("Sleep, Wake & Dress", "أذكار النوم والاستيقاظ"),
  HOME_MASJID("Home, Masjid & Daily", "أذكار المنزل والمسجد"),
  QURANIC_RABBANA("Quranic & Prophetic", "الأدعية القرآنية والنبوية"),
  SPECIAL_OCCASIONS("Special Occasions", "أدعية المناسبات الخاصة")
}

enum class SpecialOccasionType(val label: String, val badgeText: String) {
  ALL("All Occasions", "All Special Occasions"),
  JUMUAH_FRIDAY("Jumu'ah (Friday)", "Blessed Friday & Salawat"),
  RAMADAN_IFTAR("Ramadan & Iftar", "Fasting & Breaking Fast"),
  LAYLATUL_QADR("Laylatul Qadr", "Night of Decree"),
  EID_AND_HILAL("Eid & New Moon", "Hilal & Eid Celebration"),
  JANAZA_FUNERAL("Janaza & Graves", "Funeral Prayer & Burial Duas"),
  SAFAR_TRAVEL("Safar (Travel)", "Journey & Riding"),
  RAIN_WEATHER("Rain & Weather", "Beneficial Rain & Wind"),
  ILLNESS_SHIFA("Illness & Shifa", "Healing & Visiting the Sick"),
  DISTRESS_ANXIETY("Distress & Relief", "Anxiety, Sorrow & Debt"),
  ISTIKHARAH("Istikharah", "Seeking Allah's Guidance"),
  NIKAH_MARRIAGE("Marriage & Family", "Nikah & Newlyweds"),
  GATHERING_MAJLIS("Gathering (Majlis)", "Closing a Gathering")
}

data class DailyDuaItem(
  val id: String,
  val title: String,
  val category: DuaCategory,
  val occasionType: SpecialOccasionType = SpecialOccasionType.ALL,
  val occasionBadge: String,
  val arabicText: String,
  val transliteration: String,
  val englishTranslation: String,
  val urduTranslation: String,
  val authenticReference: String,
  val virtueAndTiming: String,
  val recommendedCount: Int = 1,
  val quranSurahAyah: Pair<Int, Int>? = null,
  val searchKeywordsRomanUrduAndAliases: String = "",
  val humanAudioUrl: String? = null
)

data class FridaySunnahChecklistItem(
  val id: String,
  val title: String,
  val arabicTitle: String,
  val description: String,
  val authenticReference: String,
  val actionSurahNumber: Int? = null,
  val actionDuaId: String? = null
)

enum class ReminderFrequency(val label: String) {
  WEEKLY_FRIDAY("Every Friday (Jumu'ah)"),
  DAILY("Every Day"),
  SUNNAH_FASTING_DAYS("Sunday & Wednesday Evenings (For Mon/Thu Fast)"),
  MONTHLY_HIJRI("13th, 14th & 15th Hijri (Ayyam al-Beed)")
}

data class NecessaryReminderSpec(
  val id: String,
  val title: String,
  val arabicSnippet: String,
  val subtitle: String,
  val authenticReference: String,
  val frequency: ReminderFrequency,
  val defaultTime24h: String,
  val timeOptions24h: List<String>,
  val defaultEnabled: Boolean = true,
  val linkedSurahNumber: Int? = null,
  val linkedDuaCategory: DuaCategory? = null
)

data class WeekdaySunnahSpec(
  val calendarDayOfWeek: Int,
  val dayNameEnglish: String,
  val shortDayLabel: String,
  val islamicDayTitle: String,
  val arabicDayName: String,
  val primaryReminderTitle: String,
  val arabicHadithSnippet: String,
  val englishGuidance: String,
  val urduGuidance: String,
  val authenticReference: String,
  val keySunnahBullets: List<String>,
  val isFridayJumuah: Boolean = false,
  val isSunnahFastingDay: Boolean = false,
  val linkedSurahNumber: Int? = null,
  val linkedReminderId: String = "rem_calculated_daily_day_sunnah"
)

object DailyDuasDataProvider {

  val allDuas: List<DailyDuaItem> = listOf(
    // =========================================================================
    // 1. MORNING & EVENING MASNOON DUAS
    // =========================================================================
    DailyDuaItem(
      id = "dua_ayat_al_kursi",
      title = "Ayat al-Kursi (Verse of the Throne / آیت الکرسی)",
      category = DuaCategory.MORNING_EVENING,
      occasionBadge = "Morning, Evening, After Salah & Before Sleep",
      arabicText = "ٱللَّهُ لَآ إِلَٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ ۚ لَا تَأْخُذُهُۥ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُۥ مَا فِى ٱلسَّمَٰوَٰتِ وَمَا فِى ٱلْأَرْضِ ۗ مَن ذَا ٱلَّذِى يَشْفَعُ عِندَهُۥٓ إِلَّا بِإِذْنِهِۦ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَىْءٍ مِّنْ عِلْمِهِۦٓ إِلَّا بِمَا شَآءَ ۚ وَسِعَ كُرْسِيُّهُ ٱلسَّمَٰوَٰتِ وَٱلْأَرْضَ ۖ وَلَا يَـُٔودُهُۥ حِفْظُهُمَا ۚ وَهُوَ ٱلْعَلِىُّ ٱلْعَظِيمُ",
      transliteration = "Allahu la ilaha illa Huwal-Hayyul-Qayyum, la ta'khudhuhu sinatun wa la nawm, lahu ma fis-samawati wa ma fil-ard, man dhalladhi yashfa'u 'indahu illa bi-idhnih, ya'lamu ma bayna aydihim wa ma khalfahum, wa la yuhituna bishay'im-min 'ilmihi illa bima sha'a, wasi'a kursiyyuhus-samawati wal-ard, wa la ya'uduhu hifzuhuma, wa Huwal-'Aliyyul-'Azim.",
      englishTranslation = "Allah — there is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that can intercede with Him except by His permission? He knows what is before them and what will be after them, and they encompass not a thing of His knowledge except for what He wills. His Kursi extends over the heavens and the earth, and their preservation tires Him not. And He is the Most High, the Most Great.",
      urduTranslation = "اللہ کے سوا کوئی معبود نہیں، وہ زندہ ہے، سب کا تھامنے والا ہے، نہ اسے اونگھ آتی ہے اور نہ نیند، اسی کا ہے جو کچھ آسمانوں میں ہے اور جو کچھ زمین میں ہے، کون ہے جو اس کی اجازت کے بغیر اس کے حضور شفاعت کر سکے؟ وہ جانتا ہے جو ان کے آگے ہے اور جو ان کے پیچھے ہے، اور وہ اس کے علم میں سے کسی چیز کا احاطہ نہیں کر سکتے مگر جتنا وہ چاہے، اس کی کرسی آسمانوں اور زمین کو گھیرے ہوئے ہے، اور ان دونوں کی حفاظت اسے تھکاتی نہیں، اور وہی بلند مرتبہ، عظمت والا ہے۔",
      authenticReference = "Surah Al-Baqarah 2:255 • Sahih al-Bukhari 2311 • An-Nasa'i (Al-Kubra 9848)",
      virtueAndTiming = "Recite after every obligatory prayer, in the morning, in the evening, and before sleeping for continuous divine protection.",
      recommendedCount = 1,
      quranSurahAyah = 2 to 255,
      searchKeywordsRomanUrduAndAliases = "ayatul kursi ayat ul kursi hifazat ki dua namaz ke baad ki dua sone se pehle ki dua",
      humanAudioUrl = "https://everyayah.com/data/Alafasy_128kbps/002255.mp3"
    ),
    DailyDuaItem(
      id = "dua_sayyidul_istighfar",
      title = "Sayyidul Istighfar (Chief Supplication for Forgiveness / سید الاستغفار)",
      category = DuaCategory.MORNING_EVENING,
      occasionBadge = "Morning & Evening Master Istighfar",
      arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي، فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
      transliteration = "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata'tu, a'udhu bika min sharri ma sana'tu, abu'u laka bini'matika 'alayya, wa abu'u laka bidhanbi faghfir li, fa innahu la yaghfirudh-dhunuba illa Anta.",
      englishTranslation = "O Allah, You are my Lord; there is no deity worthy of worship except You. You created me and I am Your servant, and I abide by Your covenant and promise as best I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favor upon me, and I acknowledge my sin, so forgive me, for verily none forgives sins except You.",
      urduTranslation = "اے اللہ! تو ہی میرا رب ہے، تیرے سوا کوئی معبود نہیں، تو نے مجھے پیدا کیا اور میں تیرا بندہ ہوں، اور میں اپنی طاقت کے مطابق تیرے عہد اور وعدے پر قائم ہوں، میں نے جو کچھ کیا اس کے شر سے تیری پناہ مانگتا ہوں، میں اپنے اوپر تیری نعمت کا اقرار کرتا ہوں اور اپنے گناہ کا اعتراف کرتا ہوں، پس مجھے بخش دے کیونکہ تیرے سوا کوئی گناہوں کو نہیں بخش سکتا۔",
      authenticReference = "Sahih al-Bukhari 6306 (Narrated by Shaddad ibn Aws, may Allah be pleased with him) • Hisn al-Muslim #79",
      virtueAndTiming = "Whoever recites this with conviction in the morning and passes away before evening enters Paradise; whoever recites it with conviction in the evening and passes away before morning enters Paradise.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "sayyidul istighfar maghfirat ki dua tauba ki dua bakhshish ki dua gunah maaf karne ki dua استغفار کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/79hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_morning_remembrance",
      title = "Entering the Morning in Allah's Sovereignty (Subah Ki Dua / صبح کی دعا)",
      category = DuaCategory.MORNING_EVENING,
      occasionBadge = "Daily Morning Adhkar",
      arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ",
      transliteration = "Asbahna wa asbahal-mulku lillahi, walhamdu lillahi, la ilaha illallahu wahdahu la sharika lahu, lahul-mulku wa lahul-hamdu wa Huwa 'ala kulli shay'in Qadir. Rabbi as'aluka khayra ma fi hadhal-yawmi wa khayra ma ba'dahu, wa a'udhu bika min sharri ma fi hadhal-yawmi wa sharri ma ba'dahu, Rabbi a'udhu bika minal-kasali wa su'il-kibari, Rabbi a'udhu bika min 'adhabin fin-nari wa 'adhabin fil-qabr.",
      englishTranslation = "We have reached the morning and at this very time all sovereignty belongs to Allah, and all praise is for Allah. None has the right to be worshipped except Allah, alone, without partner; to Him belongs all sovereignty and praise, and He is over all things Omnipotent. My Lord, I ask You for the good of this day and the good of what follows it, and I seek refuge in You from the evil of this day and the evil of what follows it. My Lord, I seek refuge in You from laziness and the misery of old age. My Lord, I seek refuge in You from punishment in the Fire and punishment in the grave.",
      urduTranslation = "ہم نے صبح کی اور اللہ کے ملک نے صبح کی، اور سب تعریف اللہ ہی کے لیے ہے، اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے اس کا کوئی شریک نہیں، اسی کی بادشاہی ہے اور اسی کے لیے تعریف ہے اور وہ ہر چیز پر قادر ہے۔ اے میرے رب! میں تجھ سے اس دن کی بھلائی اور اس کے بعد کی بھلائی مانگتا ہوں، اور اس دن کے شر اور اس کے بعد کے شر سے تیری پناہ مانگتا ہوں۔ اے میرے رب! میں سستی اور بڑھاپے کی برائی سے تیری پناہ مانگتا ہوں، اے میرے رب! میں دوزخ کے عذاب اور قبر کے عذاب سے تیری پناہ مانگتا ہوں۔",
      authenticReference = "Sahih Muslim 2723 (Narrated by Abdullah ibn Mas'ud, may Allah be pleased with him) • Hisn al-Muslim #77",
      virtueAndTiming = "Recite every morning after Fajr to begin the day affirming Tawhid and gratitude.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "subah ki dua subha ki dua morning dua fajr ke baad ki dua صبح کے اذکار",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/77hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_evening_remembrance",
      title = "Entering the Evening in Allah's Sovereignty (Shaam Ki Dua / شام کی دعا)",
      category = DuaCategory.MORNING_EVENING,
      occasionBadge = "Daily Evening Adhkar",
      arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذِهِ اللَّيْلَةِ وَخَيْرَ مَا بَعْدَهَا، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذِهِ اللَّيْلَةِ وَشَرِّ مَا بَعْدَهَا، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ",
      transliteration = "Amsayna wa amsal-mulku lillahi, walhamdu lillahi, la ilaha illallahu wahdahu la sharika lahu, lahul-mulku wa lahul-hamdu wa Huwa 'ala kulli shay'in Qadir. Rabbi as'aluka khayra ma fi hadhihil-laylati wa khayra ma ba'daha, wa a'udhu bika min sharri ma fi hadhihil-laylati wa sharri ma ba'daha, Rabbi a'udhu bika minal-kasali wa su'il-kibari, Rabbi a'udhu bika min 'adhabin fin-nari wa 'adhabin fil-qabr.",
      englishTranslation = "We have reached the evening and at this very time all sovereignty belongs to Allah, and all praise is for Allah. None has the right to be worshipped except Allah, alone, without partner; to Him belongs all sovereignty and praise, and He is over all things Omnipotent. My Lord, I ask You for the good of this night and the good of what follows it, and I seek refuge in You from the evil of this night and the evil of what follows it. My Lord, I seek refuge in You from laziness and the misery of old age. My Lord, I seek refuge in You from punishment in the Fire and punishment in the grave.",
      urduTranslation = "ہم نے شام کی اور اللہ کے ملک نے شام کی، اور سب تعریف اللہ ہی کے لیے ہے، اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے اس کا کوئی شریک نہیں، اسی کی بادشاہی ہے اور اسی کے لیے تعریف ہے اور وہ ہر چیز پر قادر ہے۔ اے میرے رب! میں تجھ سے اس رات کی بھلائی اور اس کے بعد کی بھلائی مانگتا ہوں، اور اس رات کے شر اور اس کے بعد کے شر سے تیری پناہ مانگتا ہوں۔ اے میرے رب! میں سستی اور بڑھاپے کی برائی سے تیری پناہ مانگتا ہوں، اے میرے رب! میں دوزخ کے عذاب اور قبر کے عذاب سے تیری پناہ مانگتا ہوں۔",
      authenticReference = "Sahih Muslim 2723 • Hisn al-Muslim #78",
      virtueAndTiming = "Recite every evening after Asr or Maghrib.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "shaam ki dua sham ki dua evening dua maghrib ki dua شام کے اذکار",
      humanAudioUrl = "https://raw.githubusercontent.com/islamicapi/masnun-dua/main/audio/79.mp3"
    ),
    DailyDuaItem(
      id = "dua_protection_harm",
      title = "Protection from Sudden Harm & Calamity (Har Nuqsan Se Hifazat Ki Dua)",
      category = DuaCategory.MORNING_EVENING,
      occasionBadge = "Morning & Evening (3 Times)",
      arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
      transliteration = "Bismillahilladhi la yadurru ma'as-mihi shay'un fil-ardi wa la fis-sama'i wa Huwas-Sami'ul-'Alim.",
      englishTranslation = "In the Name of Allah, with Whose Name nothing on earth or in the heavens can cause harm, and He is the All-Hearing, the All-Knowing.",
      urduTranslation = "اللہ کے نام کے ساتھ جس کے نام کی برکت سے زمین اور آسمان میں کوئی چیز نقصان نہیں پہنچا سکتی، اور وہ خوب سننے والا، خوب جاننے والا ہے۔",
      authenticReference = "Sunan Abi Dawud 5088 • Jami' at-Tirmidhi 3388 (Sahih) • Hisn al-Muslim #86",
      virtueAndTiming = "Recite 3 times every morning and 3 times every evening; nothing will harm the reciter.",
      recommendedCount = 3,
      searchKeywordsRomanUrduAndAliases = "hifazat ki dua nuqsan se bachne ki dua bismillahillazi la yadurru مصیبت اور نقصان سے حفاظت کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/86hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_hasbiyallah",
      title = "Sufficiency & Reliance Upon Allah (HasbiyAllah / حسبی اللہ)",
      category = DuaCategory.MORNING_EVENING,
      occasionBadge = "Morning & Evening (7 Times)",
      arabicText = "حَسْبِىَ ٱللَّهُ لَآ إِلَٰهَ إِلَّا هُوَ ۖ عَلَيْهِ تَوَكَّلْتُ ۖ وَهُوَ رَبُّ ٱلْعَرْشِ ٱلْعَظِيمِ",
      transliteration = "HasbiyAllahu la ilaha illa Huwa, 'alayhi tawakkaltu wa Huwa Rabbul-'Arshil-'Azim.",
      englishTranslation = "Sufficient for me is Allah; there is no deity except Him. On Him I have relied, and He is the Lord of the Great Throne.",
      urduTranslation = "میرے لیے اللہ ہی کافی ہے، اس کے سوا کوئی معبود نہیں، میں نے اسی پر بھروسہ کیا اور وہ عرشِ عظیم کا رب ہے۔",
      authenticReference = "Surah At-Tawbah 9:129 • Sunan Abi Dawud 5081 • Hisn al-Muslim #83",
      virtueAndTiming = "Recite 7 times in the morning and evening; Allah suffices the servant regarding all worldly and hereafter concerns.",
      recommendedCount = 7,
      quranSurahAyah = 9 to 129,
      searchKeywordsRomanUrduAndAliases = "hasbiyallah pareshani ki dua tawakkul ki dua مشکل اور پریشانی کی دعا",
      humanAudioUrl = "https://everyayah.com/data/Alafasy_128kbps/009129.mp3"
    ),
    DailyDuaItem(
      id = "dua_morning_evening_afiyah_body",
      title = "Supplication for Health in Body, Hearing & Sight (Sehat Aur Afiyat Ki Dua / صحت و عافیت کی دعا)",
      category = DuaCategory.MORNING_EVENING,
      occasionBadge = "Morning & Evening (3 Times)",
      arabicText = "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لَا إِلَهَ إِلَّا أَنْتَ، اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْكُفْرِ وَالْفَقْرِ، وَأَعُوذُ بِكَ مِنْ عَذَابِ الْقَبْرِ، لَا إِلَهَ إِلَّا أَنْتَ",
      transliteration = "Allahumma 'afini fi badani, Allahumma 'afini fi sam'i, Allahumma 'afini fi basari, la ilaha illa Anta. Allahumma inni a'udhu bika minal-kufri wal-faqri, wa a'udhu bika min 'adhabil-qabri, la ilaha illa Anta.",
      englishTranslation = "O Allah, grant health to my body; O Allah, grant health to my hearing; O Allah, grant health to my sight. There is no deity worthy of worship except You. O Allah, I seek refuge in You from disbelief and poverty, and I seek refuge in You from the punishment of the grave. There is no deity worthy of worship except You.",
      urduTranslation = "اے اللہ! مجھے میرے بدن میں عافیت عطا فرما، اے اللہ! مجھے میری سماعت (کانوں) میں عافیت عطا فرما، اے اللہ! مجھے میری بصارت (آنکھوں) میں عافیت عطا فرما، تیرے سوا کوئی معبود نہیں۔ اے اللہ! میں کفر اور محتاجی سے تیری پناہ مانگتا ہوں، اور قبر کے عذاب سے تیری پناہ مانگتا ہوں، تیرے سوا کوئی معبود نہیں۔",
      authenticReference = "Sunan Abi Dawud 5090 • Musnad Ahmad 20430 (Hasan) • Hisn al-Muslim #82",
      virtueAndTiming = "Recite 3 times in the morning and 3 times in the evening for lifelong preservation of health, hearing, and eyesight.",
      recommendedCount = 3,
      searchKeywordsRomanUrduAndAliases = "sehat ki dua afiyat ki dua badani sam'i basari aankhon aur kaanon ki hifazat صحت اور عافیت کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/82hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_morning_evening_raditu_billahi",
      title = "Pleased with Allah as Lord, Islam as Religion & Muhammad ﷺ as Prophet (رضيت بالله ربا)",
      category = DuaCategory.MORNING_EVENING,
      occasionBadge = "Morning & Evening (3 Times)",
      arabicText = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
      transliteration = "Raditu billahi Rabban, wa bil-Islami dinan, wa bi-Muhammadin sallallahu 'alayhi wa sallama Nabiyyan.",
      englishTranslation = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad ﷺ as my Prophet.",
      urduTranslation = "میں اللہ کے رب ہونے پر، اسلام کے دین ہونے پر، اور حضرت محمد ﷺ کے نبی ہونے پر دل سے راضی ہو گیا۔",
      authenticReference = "Sunan Abi Dawud 5072 • Jami' at-Tirmidhi 3389 • Sahih Muslim 1884 • Hisn al-Muslim #87",
      virtueAndTiming = "Whoever recites this 3 times in the morning and 3 times in the evening, it is a promise from Allah to make him pleased on the Day of Resurrection.",
      recommendedCount = 3,
      searchKeywordsRomanUrduAndAliases = "raditu billahi rabban razi hone ki dua iman ki tajdeed صبح و شام راضی ہونے کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/87hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_morning_evening_ya_hayyu_ya_qayyum",
      title = "Seeking Help Through Allah's Mercy (Ya Hayyu Ya Qayyum Birahmatika Astaghith / یا حی یا قیوم)",
      category = DuaCategory.MORNING_EVENING,
      occasionBadge = "Morning, Evening & Times of Distress",
      arabicText = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ، وَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ",
      transliteration = "Ya Hayyu Ya Qayyum birahmatika astaghith, aslih li sha'ni kullahu, wa la takilni ila nafsi tarfata 'ayn.",
      englishTranslation = "O Ever-Living, O Sustainer of all existence, by Your mercy I seek help; rectify all of my affairs and do not entrust me to myself even for the blink of an eye.",
      urduTranslation = "اے ہمیشہ زندہ رہنے والے، اے سب کو قائم رکھنے والے! میں تیری رحمت کے وسیلے سے فریاد کرتا ہوں، میرے تمام معاملات سنوار دے اور مجھے پلک جھپکنے کے برابر بھی میرے نفس کے حوالے نہ فرما۔",
      authenticReference = "Sunan an-Nasa'i (Al-Kubra 10405) • Al-Mustadrak al-Hakim 1/545 (Hasan) • Hisn al-Muslim #88",
      virtueAndTiming = "Taught by the Prophet ﷺ to his daughter Fatimah (RA) to recite every morning and evening.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "ya hayyu ya qayyum birahmatika astaghith pareshani ki dua kaam sanwarne ki dua یا حی یا قیوم برحمتک استغیث",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/88hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_morning_evening_subhanallahi_wa_bihamdihi",
      title = "Tasbih of Immense Weight & Reward (Subhanallahi wa Bihamdihi 'Adada Khalqihi / سبحان اللہ وبحمدہ)",
      category = DuaCategory.MORNING_EVENING,
      occasionBadge = "Morning Adhkar (3 Times After Fajr)",
      arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ: عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ",
      transliteration = "Subhanallahi wa bihamdihi: 'adada khalqihi, wa rida nafsihi, wa zinata 'arshihi, wa midada kalimatih.",
      englishTranslation = "Glory is to Allah and praise is to Him, by the multitude of His creation, by His Pleasure, by the weight of His Throne, and by the extent of His Words.",
      urduTranslation = "اللہ پاک ہے اپنی تعریف کے ساتھ: اس کی مخلوق کی گنتی کے برابر، اس کی ذات کی خوشنودی کے برابر، اس کے عرش کے وزن کے برابر، اور اس کے کلمات کی سیاہی کے برابر۔",
      authenticReference = "Sahih Muslim 2726 (Narrated by Juwayriyah, may Allah be pleased with her) • Hisn al-Muslim #94",
      virtueAndTiming = "The Prophet ﷺ told Juwayriyah (RA) that reciting these four phrases 3 times outweighs sitting in remembrance from Fajr until mid-morning.",
      recommendedCount = 3,
      searchKeywordsRomanUrduAndAliases = "subhanallahi wa bihamdihi adada khalqihi fajr ke baad ki tasbih سبحان اللہ وبحمدہ عدد خلقہ",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/94hm.mp3"
    ),

    // =========================================================================
    // 1B. DUAS AFTER SALAH (NAMAZ) & ADHAN
    // =========================================================================
    DailyDuaItem(
      id = "dua_after_salah_istighfar_salam",
      title = "Immediate Sunnah Dhikr After Salam (Astaghfirullah 3x & Allahumma Antas-Salam / نماز کے فوراً بعد کی دعا)",
      category = DuaCategory.AFTER_SALAH,
      occasionBadge = "Immediately After Every Fard Salah",
      arabicText = "أَسْتَغْفِرُ اللَّهَ (ثَلَاثًا) ۝ اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
      transliteration = "Astaghfirullah (3 times). Allahumma Antas-Salamu wa minkas-salamu, tabarakta ya Dhal-Jalali wal-Ikram.",
      englishTranslation = "I seek the forgiveness of Allah (three times). O Allah, You are As-Salam (Peace and Perfection) and from You is all peace; blessed are You, O Possessor of Majesty and Honor.",
      urduTranslation = "میں اللہ سے بخشش مانگتا ہوں (تین بار)۔ اے اللہ! تو ہی سلامتی والا ہے اور تیری ہی طرف سے سلامتی ہے، تو بڑا بابرکت ہے اے عظمت اور عزت و اکرام والے۔",
      authenticReference = "Sahih Muslim 591 (Narrated by Thawban, may Allah be pleased with him) • Hisn al-Muslim #66",
      virtueAndTiming = "Whenever the Messenger of Allah ﷺ concluded his prayer with Salam, he would immediately seek forgiveness three times and recite this praise.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "namaz ke baad ki dua farz namaz ke baad ki dua allahumma antas salam astaghfirullah نماز کے بعد کے اذکار",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/66hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_after_salah_tahlil_la_mani",
      title = "Tahlil & Tawakkul After Obligatory Prayer (Allahumma La Mani'a Lima A'tayta / اللهم لا مانع لما أعطيت)",
      category = DuaCategory.AFTER_SALAH,
      occasionBadge = "After Every Obligatory Salah",
      arabicText = "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، اللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ، وَلَا مُعْطِيَ لِمَا مَنَعْتَ، وَلَا يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ",
      transliteration = "La ilaha illallahu wahdahu la sharika lahu, lahul-mulku wa lahul-hamdu wa Huwa 'ala kulli shay'in Qadir. Allahumma la mani'a lima a'tayta, wa la mu'tiya lima mana'ta, wa la yanfa'u dhal-jaddi minkal-jadd.",
      englishTranslation = "None has the right to be worshipped except Allah alone, without partner; to Him belongs all sovereignty and praise, and He is over all things Omnipotent. O Allah, none can withhold what You give, and none can give what You withhold, and the wealth or fortune of a fortunate person cannot avail him against You.",
      urduTranslation = "اللہ کے سوا کوئی معبود نہیں وہ اکیلا ہے اس کا کوئی شریک نہیں، اسی کی بادشاہی ہے اور اسی کے لیے تمام تعریف ہے اور وہ ہر چیز پر قادر ہے۔ اے اللہ! جو کچھ تو عطا فرمائے اسے کوئی روکنے والا نہیں، اور جو تو روک لے اسے کوئی دینے والا نہیں، اور کسی مالدار و نصیب والے کی شان تیرے مقابلے میں اسے نفع نہیں دے سکتی۔",
      authenticReference = "Sahih al-Bukhari 844 • Sahih Muslim 593 (Narrated by Al-Mughirah ibn Shu'bah, may Allah be pleased with him) • Hisn al-Muslim #67",
      virtueAndTiming = "Recited by the Prophet ﷺ at the conclusion of every obligatory prayer.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "namaz ke baad ki dua allahumma la mania lima atayta farz نماز کے بعد کی مسنون دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/67hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_after_salah_ainni_dhikrika",
      title = "Seeking Allah's Help in Remembrance, Gratitude & Worship (اللهم أعني على ذكرك وشكرك)",
      category = DuaCategory.AFTER_SALAH,
      occasionBadge = "Taught to Mu'adh ibn Jabal (RA) After Every Salah",
      arabicText = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ، وَشُكْرِكَ، وَحُسْنِ عِبَادَتِكَ",
      transliteration = "Allahumma a'inni 'ala dhikrika, wa shukrika, wa husni 'ibadatika.",
      englishTranslation = "O Allah, help me to remember You, to thank You, and to worship You in the best manner.",
      urduTranslation = "اے اللہ! اپنے ذکر کرنے، اپنا شکر ادا کرنے، اور اپنی بہترین عبادت کرنے پر میری مدد فرما۔",
      authenticReference = "Sunan Abi Dawud 1522 • Sunan an-Nasa'i 1303 (Sahih) • Hisn al-Muslim #59",
      virtueAndTiming = "The Prophet ﷺ took Mu'adh ibn Jabal (RA) by the hand and said: 'O Mu'adh, by Allah I love you; never forget to say at the end of every prayer: Allahumma a'inni 'ala dhikrika wa shukrika wa husni 'ibadatika.'",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "allahumma ainni ala zikrika wa shukrika wa husni ibadatika muadh namaz ke baad ki dua اللھم اعنی علی ذکرک",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/59hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_after_salah_tasbih_fatimi",
      title = "Tasbih After Salah (SubhanAllah 33x, Alhamdulillah 33x, Allahu Akbar 33x + Tahlil / تسبیحاتِ فاطمی)",
      category = DuaCategory.AFTER_SALAH,
      occasionBadge = "After Every Salah & Before Sleeping",
      arabicText = "سُبْحَانَ اللَّهِ (٣٣) ۝ الْحَمْدُ لِلَّهِ (٣٣) ۝ اللَّهُ أَكْبَرُ (٣٣) ۝ لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
      transliteration = "SubhanAllah (33x) • Alhamdulillah (33x) • Allahu Akbar (33x) • La ilaha illallahu wahdahu la sharika lahu, lahul-mulku wa lahul-hamdu wa Huwa 'ala kulli shay'in Qadir.",
      englishTranslation = "Glory is to Allah (33 times). All praise is for Allah (33 times). Allah is the Greatest (33 times). Completing the hundredth: None has the right to be worshipped except Allah alone, without partner; to Him belongs sovereignty and praise, and He is over all things Omnipotent.",
      urduTranslation = "اللہ پاک ہے (۳۳ بار)، تمام تعریف اللہ کے لیے ہے (۳۳ بار)، اللہ سب سے بڑا ہے (۳۳ بار)، اور سو کا عدد پورا کرنے کے لیے: اللہ کے سوا کوئی معبود نہیں وہ اکیلا ہے اس کا کوئی شریک نہیں، اسی کی بادشاہی ہے اور اسی کے لیے تمام تعریف ہے اور وہ ہر چیز پر قادر ہے۔",
      authenticReference = "Sahih Muslim 597 • Sahih al-Bukhari 843 • Hisn al-Muslim #69",
      virtueAndTiming = "Whoever recites this after every prayer will have his sins forgiven even if they are like the foam of the sea (Sahih Muslim 597).",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "tasbih fatimi subhanallah 33 alhamdulillah 33 allahu akbar 34 namaz ke baad tasbih تسبیح فاطمی",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/69hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_after_salah_fajr_ilman_nafian",
      title = "Dua After Fajr Prayer for Beneficial Knowledge, Pure Provision & Accepted Deeds (فجر کے بعد کی دعا)",
      category = DuaCategory.AFTER_SALAH,
      occasionBadge = "After Fajr Salah Salam",
      arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلًا مُتَقَبَّلًا",
      transliteration = "Allahumma inni as'aluka 'ilman nafi'an, wa rizqan tayyiban, wa 'amalan mutaqabbalan.",
      englishTranslation = "O Allah, indeed I ask You for beneficial knowledge, pure and lawful provision, and accepted deeds.",
      urduTranslation = "اے اللہ! میں تجھ سے نفع بخش علم، پاکیزہ و حلال رزق، اور مقبول عمل کا سوال کرتا ہوں۔",
      authenticReference = "Sunan Ibn Majah 925 • Musnad Ahmad 26602 (Narrated by Umm Salamah RA — Sahih) • Hisn al-Muslim #73",
      virtueAndTiming = "The Prophet ﷺ used to recite this supplication every morning after concluding the Fajr prayer.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "fajr ke baad ki dua ilman nafian rizqan tayyiban amalan mutaqabbalan اللھم انی اسئلک علما نافعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/73hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_after_adhan_wasilah",
      title = "Supplication After Hearing the Adhan (Azan Ke Baad Ki Dua / اذان کے بعد کی مسنون دعا)",
      category = DuaCategory.AFTER_SALAH,
      occasionBadge = "After Repeating the Words of the Mu'adhin",
      arabicText = "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ، وَالصَّلَاةِ الْقَائِمَةِ، آتِ مُحَمَّدًا الْوَسِيلَةَ وَالْفَضِيلَةَ، وَابْعَثْهُ مَقَامًا مَحْمُودًا الَّذِي وَعَدْتَهُ",
      transliteration = "Allahumma Rabba hadhihid-da'watit-tammati, was-salatil-qa'imati, ati Muhammadanil-wasilata wal-fadilata, wab'ath-hu maqaman mahmudanil-ladhi wa'adtah.",
      englishTranslation = "O Allah, Lord of this perfect call and established prayer, grant Muhammad the Wasilah (highest station in Paradise) and pre-eminence, and raise him to the praised station (Maqam Mahmud) which You have promised him.",
      urduTranslation = "اے اللہ! اس کامل پکار (اذان) اور قائم ہونے والی نماز کے رب! حضرت محمد ﷺ کو وسیلہ اور فضیلت عطا فرما، اور انہیں اس مقامِ محمود پر فائز فرما جس کا تو نے ان سے وعدہ فرمایا ہے۔",
      authenticReference = "Sahih al-Bukhari 614 (Narrated by Jabir ibn Abdullah, may Allah be pleased with them) • Hisn al-Muslim #25",
      virtueAndTiming = "Repeat after the Mu'adhin, send Salawat upon the Prophet ﷺ (Sahih Muslim 384), then recite this Dua to earn the Prophet's ﷺ intercession on the Day of Judgment.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "azan ki dua adhan ke baad ki dua allahumma rabba hazihid dawatit tammah اذان کے بعد کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/25hm.mp3"
    ),

    // =========================================================================
    // 2. SLEEP, WAKE & DRESS MASNOON DUAS
    // =========================================================================
    DailyDuaItem(
      id = "dua_before_sleeping",
      title = "Dua Before Going to Sleep (Sone Se Pehle Ki Dua / سونے سے پہلے کی دعا)",
      category = DuaCategory.SLEEP_WAKE,
      occasionBadge = "Before Sleeping (Sone Ki Dua)",
      arabicText = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
      transliteration = "Bismika Allahumma amutu wa ahya.",
      englishTranslation = "In Your Name, O Allah, I die and I live.",
      urduTranslation = "اے اللہ! تیرے ہی نام کے ساتھ میں مرتا (سوتا) ہوں اور جیتا (جاگتا) ہوں۔",
      authenticReference = "Sahih al-Bukhari 6324 (Narrated by Hudhayfah, may Allah be pleased with him) • Hisn al-Muslim #105",
      virtueAndTiming = "Perform Wudu before sleeping, lie on the right side, dust the bed, and recite this supplication.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "sone se pehle ki dua sonay se pehlay ki dua sone ki dua sote waqt ki dua neend ki dua sleeping dua before sleep رات کو سونے کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/105hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_waking_up",
      title = "Dua Upon Waking Up from Sleep (So Kar Uthne Ki Dua / سو کر اٹھنے کی دعا)",
      category = DuaCategory.SLEEP_WAKE,
      occasionBadge = "Upon Waking Up",
      arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
      transliteration = "Alhamdu lillahilladhi ahyana ba'da ma amatana wa ilayhin-nushur.",
      englishTranslation = "All praise is for Allah Who gave us life after having taken it from us, and unto Him is the resurrection.",
      urduTranslation = "سب تعریفیں اللہ کے لیے ہیں جس نے ہمیں مارنے (سلانے) کے بعد زندہ (بیدار) کیا اور اسی کی طرف اٹھ کر جانا ہے۔",
      authenticReference = "Sahih al-Bukhari 6312 • Sahih Muslim 2711 • Hisn al-Muslim #1",
      virtueAndTiming = "Recite immediately upon opening your eyes from sleep.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "so kar uthne ki dua neend se uthne ki dua jaagne ki dua jagne ki dua waking up dua نیند سے جاگنے کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/1hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_wearing_garment",
      title = "Dua When Wearing Clothes (Kapde Pehanne Ki Dua / کپڑے پہننے کی دعا)",
      category = DuaCategory.SLEEP_WAKE,
      occasionBadge = "Dressing / Wearing Garment",
      arabicText = "الْحَمْدُ لِلَّهِ الَّذِي كَسَانِي هَذَا الثَّوْبَ وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
      transliteration = "Alhamdu lillahilladhi kasani hadhath-thawba wa razaqanihi min ghayri hawlin minni wa la quwwah.",
      englishTranslation = "All praise is for Allah Who has clothed me with this garment and provided it for me, without any power or might from myself.",
      urduTranslation = "تمام تعریفیں اس اللہ کے لیے ہیں جس نے مجھے یہ لباس پہنایا اور میری کسی طاقت اور قوت کے بغیر مجھے یہ عطا فرمایا۔",
      authenticReference = "Sunan Abi Dawud 4023 • Jami' at-Tirmidhi 3458 (Hasan) • Hisn al-Muslim #5",
      virtueAndTiming = "Whoever recites this when putting on a garment has their previous minor sins forgiven.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "kapde pehanne ki dua kapray pehnay ki dua libas ki dua new dress clothes لباس پہننے کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/5hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_looking_mirror_aaina",
      title = "Dua When Looking in the Mirror (Aaina Dekhne Ki Dua / آئینہ دیکھنے کی دعا)",
      category = DuaCategory.SLEEP_WAKE,
      occasionBadge = "Looking in Mirror",
      arabicText = "اللَّهُمَّ أَنْتَ حَسَّنْتَ خَلْقِي فَحَسِّنْ خُلُقِي",
      transliteration = "Allahumma Anta hassanta khalqi fa-hassin khuluqi.",
      englishTranslation = "O Allah, just as You have made my outward form beautiful, make my character and conduct beautiful as well.",
      urduTranslation = "اے اللہ! جس طرح تو نے میری صورت اچھی بنائی ہے، اسی طرح میرے اخلاق اور سیرت کو بھی اچھا بنا دے۔",
      authenticReference = "Musnad Ahmad 24392 • Sahih Ibn Hibban 959 (Authenticated)",
      virtueAndTiming = "Recite whenever looking into a mirror to thank Allah and ask for noble character.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "aaina dekhne ki dua sheesha dekhne ki dua shisha dekhnay ki dua mirror dua آئینہ دیکھنے کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/islamicapi/masnun-dua/main/audio/831.mp3"
    ),

    // =========================================================================
    // 3. HOME, MASJID, WUDU & DINING MASNOON DUAS
    // =========================================================================
    DailyDuaItem(
      id = "dua_drinking_water_pani",
      title = "Dua for Drinking Water (Pani Peene Ki Dua / پانی پینے کی دعا)",
      category = DuaCategory.HOME_MASJID,
      occasionBadge = "Drinking Water (Before & After)",
      arabicText = "بِسْمِ اللَّهِ ۝ الْحَمْدُ لِلَّهِ الَّذِي سَقَانَا عَذْبًا فُرَاتًا بِرَحْمَتِهِ وَلَمْ يَجْعَلْهُ مِلْحًا أُجَاجًا بِذُنُوبِنَا",
      transliteration = "Bismillahi ... Alhamdu lillahilladhi saqana 'adhban furatan birahmatihi wa lam yaj'alhu milhan ujajan bidhunubina.",
      englishTranslation = "In the Name of Allah (before drinking). All praise is for Allah Who gave us sweet, pure water to drink by His mercy and did not make it bitter and salty due to our sins (after drinking).",
      urduTranslation = "(پانی پینے سے پہلے:) بسم اللہ۔ (پانی پینے کے بعد:) تمام تعریفیں اللہ کے لیے ہیں جس نے ہمیں اپنی رحمت سے میٹھا اور خوشگوار پانی پلایا اور ہمارے گناہوں کے سبب اسے کھارا اور کڑوا نہیں بنایا۔",
      authenticReference = "Sahih al-Bukhari 5631 • Sahih Muslim 2028 • Hilyat al-Muttaqin / Al-Kafi Vol. 6 (Imam Ja'far al-Sadiq AS) • Shu'ab al-Iman al-Bayhaqi 6002",
      virtueAndTiming = "Drink water with the right hand while sitting, in three breaths: say 'Bismillah' before drinking and 'Alhamdulillah' (or this complete praise) after finishing.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "pani peene ki dua paani peenay ki dua pani pina water drinking dua پینے کا پانی پانی پینے کی دعا",
      humanAudioUrl = null
    ),
    DailyDuaItem(
      id = "dua_before_eating_khana",
      title = "Dua Before Eating a Meal (Khana Khane Se Pehle Ki Dua / کھانا کھانے سے پہلے کی دعا)",
      category = DuaCategory.HOME_MASJID,
      occasionBadge = "Before Eating & Drinking",
      arabicText = "بِسْمِ اللَّهِ ۝ (فَإِنْ نَسِيَ فِي أَوَّلِهِ فَلْيَقُلْ: بِسْمِ اللَّهِ فِي أَوَّلِهِ وَآخِرِهِ)",
      transliteration = "Bismillah. (Fa-in nasiya fi awwalihi fal-yaqul: Bismillahi fi awwalihi wa akhirihi.)",
      englishTranslation = "In the Name of Allah. (If one forgets at the beginning, let him say: In the Name of Allah at its beginning and at its end.)",
      urduTranslation = "اللہ کے نام سے۔ (اور اگر شروع میں بھول جائے تو کہے: اللہ کے نام کے ساتھ اس کے اول اور آخر میں۔)",
      authenticReference = "Sunan Abi Dawud 3767 • Jami' at-Tirmidhi 1858 (Sahih) • Al-Mustadrak al-Hakim 7076 • Hisn al-Muslim #178",
      virtueAndTiming = "Recite before starting any meal and eat with the right hand from what is nearest to you.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "khana khane ki dua khana khane se pehle ki dua khana shuru karne ki dua eating food meal کھانا کھانے کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/178hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_after_eating",
      title = "Dua After Finishing a Meal (Khana Khane Ke Baad Ki Dua / کھانا کھانے کے بعد کی دعا)",
      category = DuaCategory.HOME_MASJID,
      occasionBadge = "After Eating & Drinking",
      arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
      transliteration = "Alhamdu lillahilladhi at'amani hadha wa razaqanihi min ghayri hawlin minni wa la quwwah.",
      englishTranslation = "All praise is for Allah Who fed me this food and provided it for me without any might or power on my part.",
      urduTranslation = "تمام تعریفیں اس اللہ کے لیے ہیں جس نے مجھے یہ کھانا کھلایا اور میری کسی طاقت اور قوت کے بغیر مجھے یہ رزق عطا فرمایا۔",
      authenticReference = "Jami' at-Tirmidhi 3458 • Sunan Abi Dawud 4023 (Hasan) • Hisn al-Muslim #180",
      virtueAndTiming = "Whoever recites this after eating a meal has their past minor sins forgiven.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "khana khane ke baad ki dua khana khatam karne ki dua after eating food کھانا کھانے کے بعد کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/180hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_drinking_milk_doodh",
      title = "Dua When Drinking Milk (Doodh Peene Ki Dua / دودھ پینے کی دعا)",
      category = DuaCategory.HOME_MASJID,
      occasionBadge = "Drinking Milk",
      arabicText = "اللَّهُمَّ بَارِكْ لَنَا فِيهِ وَزِدْنَا مِنْهُ",
      transliteration = "Allahumma barik lana fihi wa zidna minhu.",
      englishTranslation = "O Allah, bless it for us and give us more of it.",
      urduTranslation = "اے اللہ! ہمارے لیے اس (دودھ) میں برکت عطا فرما اور ہمیں اس سے مزید عطا فرما۔",
      authenticReference = "Sunan Abi Dawud 3730 • Jami' at-Tirmidhi 3455 • Hisn al-Muslim #179",
      virtueAndTiming = "Taught by the Prophet ﷺ specifically when drinking milk, as milk nourishes both hunger and thirst.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "doodh peene ki dua dudh peenay ki dua milk dua دودھ پینے کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/islamicapi/masnun-dua/main/audio/504.mp3"
    ),
    DailyDuaItem(
      id = "dua_leaving_home",
      title = "Dua When Leaving the Home (Ghar Se Nikalne Ki Dua / گھر سے نکلنے کی دعا)",
      category = DuaCategory.HOME_MASJID,
      occasionBadge = "Stepping Out of Home",
      arabicText = "بِسْمِ اللَّهِ، تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
      transliteration = "Bismillahi, tawakkaltu 'alallahi, wa la hawla wa la quwwata illa billah.",
      englishTranslation = "In the Name of Allah, I place my trust in Allah; and there is no might nor power except with Allah.",
      urduTranslation = "اللہ کے نام کے ساتھ، میں نے اللہ پر بھروسہ کیا، اور گناہ سے بچنے کی ہمت اور نیکی کرنے کی طاقت اللہ ہی کی توفیق سے ہے۔",
      authenticReference = "Sunan Abi Dawud 5095 • Jami' at-Tirmidhi 3426 (Sahih) • Hisn al-Muslim #16",
      virtueAndTiming = "It is said to the one who recites this upon leaving home: 'You are guided, sufficed, and protected,' and Satan steps aside.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "ghar se nikalne ki dua ghar se bahar jane ki dua leaving home house باہر نکلنے کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/16hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_entering_home",
      title = "Dua When Entering the Home (Ghar Mein Dakhil Hone Ki Dua / گھر میں داخل ہونے کی دعا)",
      category = DuaCategory.HOME_MASJID,
      occasionBadge = "Entering Home",
      arabicText = "بِسْمِ اللَّهِ وَلَجْنَا، وَبِسْمِ اللَّهِ خَرَجْنَا، وَعَلَى رَبِّنَا تَوَكَّلْنَا، ثُمَّ لِيُسَلِّمْ عَلَى أَهْلِهِ",
      transliteration = "Bismillahi walajna, wa bismillahi kharajna, wa 'ala Rabbina tawakkalna, thumma liyusallim 'ala ahlihi.",
      englishTranslation = "In the Name of Allah we enter, in the Name of Allah we leave, and upon our Lord we rely — then let him greet his family with Salam.",
      urduTranslation = "اللہ ہی کے نام کے ساتھ ہم داخل ہوئے، اور اللہ ہی کے نام کے ساتھ ہم نکلے، اور اپنے رب ہی پر ہم نے بھروسہ کیا، پھر اپنے گھر والوں کو سلام کرے۔",
      authenticReference = "Sunan Abi Dawud 5096 • Sahih Muslim 2018 (Mentioning Allah's Name upon entering) • Hisn al-Muslim #18",
      virtueAndTiming = "Mention Allah's Name when entering the home and greet your family with 'As-Salamu Alaykum' so blessing descends upon the household.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "ghar mein dakhil hone ki dua ghar aane ki dua entering home گھر آنے کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/18hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_entering_masjid",
      title = "Dua When Entering the Masjid (Masjid Mein Dakhil Hone Ki Dua / مسجد میں داخل ہونے کی دعا)",
      category = DuaCategory.HOME_MASJID,
      occasionBadge = "Entering Mosque (Right Foot First)",
      arabicText = "بِسْمِ اللَّهِ وَالصَّلَاةُ وَالسَّلَامُ عَلَى رَسُولِ اللَّهِ، اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
      transliteration = "Bismillahi was-salatu was-salamu 'ala Rasulillahi, Allahummaftah li abwaba rahmatik.",
      englishTranslation = "In the Name of Allah, and peace and blessings be upon the Messenger of Allah. O Allah, open for me the gates of Your mercy.",
      urduTranslation = "اللہ کے نام کے ساتھ، اور رسول اللہ ﷺ پر درود و سلام ہو۔ اے اللہ! میرے لیے اپنی رحمت کے دروازے کھول دے۔",
      authenticReference = "Sahih Muslim 713 • Sunan Abi Dawud 465 • Hisn al-Muslim #20",
      virtueAndTiming = "Enter the Masjid with the right foot first while reciting this supplication.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "masjid mein dakhil hone ki dua masjid jane ki dua entering mosque مسجد کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/islamicapi/masnun-dua/main/audio/175.mp3"
    ),
    DailyDuaItem(
      id = "dua_leaving_masjid",
      title = "Dua When Leaving the Masjid (Masjid Se Nikalne Ki Dua / مسجد سے نکلنے کی دعا)",
      category = DuaCategory.HOME_MASJID,
      occasionBadge = "Leaving Mosque (Left Foot First)",
      arabicText = "بِسْمِ اللَّهِ وَالصَّلَاةُ وَالسَّلَامُ عَلَى رَسُولِ اللَّهِ، اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ، اللَّهُمَّ اعْصِمْنِي مِنَ الشَّيْطَانِ الرَّجِيمِ",
      transliteration = "Bismillahi was-salatu was-salamu 'ala Rasulillahi, Allahumma inni as'aluka min fadlika, Allahumma'simni minash-shaytanir-rajim.",
      englishTranslation = "In the Name of Allah, and peace and blessings be upon the Messenger of Allah. O Allah, I ask You from Your bounty. O Allah, protect me from Satan, the accursed.",
      urduTranslation = "اللہ کے نام کے ساتھ، اور رسول اللہ ﷺ پر درود و سلام ہو۔ اے اللہ! میں تجھ سے تیرے فضل کا سوال کرتا ہوں۔ اے اللہ! مجھے شیطان مردود سے محفوظ رکھ۔",
      authenticReference = "Sahih Muslim 713 • Sunan Ibn Majah 773 • Hisn al-Muslim #21",
      virtueAndTiming = "Step out of the Masjid with the left foot first and ask Allah for His lawful bounty and protection.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "masjid se nikalne ki dua masjid se bahar aane ki dua leaving mosque مسجد سے نکلنے کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/21hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_after_wudu",
      title = "Dua After Completing Wudu (Wazu Ke Baad Ki Dua / وضو کے بعد کی دعا)",
      category = DuaCategory.HOME_MASJID,
      occasionBadge = "After Wudu (Ablution)",
      arabicText = "أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ، اللَّهُمَّ اجْعَلْنِي مِنَ التَّوَّابِينَ وَاجْعَلْنِي مِنَ الْمُتَطَهِّرِينَ",
      transliteration = "Ash-hadu an la ilaha illallahu wahdahu la sharika lahu, wa ash-hadu anna Muhammadan 'abduhu wa Rasuluhu. Allahummaj'alni minat-tawwabina waj'alni minal-mutatahhirin.",
      englishTranslation = "I bear witness that none has the right to be worshipped except Allah alone, Who has no partner; and I bear witness that Muhammad is His servant and His Messenger. O Allah, make me among those who turn to You in repentance and make me among those who purify themselves.",
      urduTranslation = "میں گواہی دیتا ہوں کہ اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے اس کا کوئی شریک نہیں، اور میں گواہی دیتا ہوں کہ محمد ﷺ اس کے بندے اور رسول ہیں۔ اے اللہ! مجھے کثرت سے توبہ کرنے والوں میں شامل فرما اور مجھے پاک صاف رہنے والوں میں بنا دے۔",
      authenticReference = "Sahih Muslim 234 • Jami' at-Tirmidhi 55 • Hisn al-Muslim #13–14",
      virtueAndTiming = "All eight gates of Paradise are opened for the one who performs Wudu thoroughly and recites this testimony.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "wazu ki dua wudu ki dua wazu ke baad ki dua ablution وضو کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/13hm.mp3|https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/14hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_entering_washroom",
      title = "Dua Before Entering the Restroom (Bait-ul-Khala Mein Dakhil Hone Ki Dua / بیت الخلاء میں داخل ہونے کی دعا)",
      category = DuaCategory.HOME_MASJID,
      occasionBadge = "Entering Restroom (Left Foot First)",
      arabicText = "بِسْمِ اللَّهِ، اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْخُبُثِ وَالْخَبَائِثِ",
      transliteration = "Bismillahi, Allahumma inni a'udhu bika minal-khubuthi wal-khaba'ith.",
      englishTranslation = "In the Name of Allah. O Allah, I seek refuge in You from all impure and evil spirits (male and female devils).",
      urduTranslation = "اللہ کے نام سے۔ اے اللہ! میں خبیث جنوں اور خبیث جنیوں (ہر قسم کی ناپاکی اور شر) سے تیری پناہ مانگتا ہوں۔",
      authenticReference = "Sahih al-Bukhari 142 • Sahih Muslim 375 • Hisn al-Muslim #10",
      virtueAndTiming = "Recite before stepping into the washroom with your left foot.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "bait ul khala ki dua washroom jane ki dua toilet ki dua bathroom mein dakhil hone ki dua بیت الخلاء کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/10hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_leaving_washroom",
      title = "Dua Upon Leaving the Restroom (Bait-ul-Khala Se Nikalne Ki Dua / بیت الخلاء سے نکلنے کی دعا)",
      category = DuaCategory.HOME_MASJID,
      occasionBadge = "Leaving Restroom (Right Foot First)",
      arabicText = "غُفْرَانَكَ",
      transliteration = "Ghufranaka.",
      englishTranslation = "I seek Your forgiveness (O Allah).",
      urduTranslation = "اے اللہ! میں تیری بخشش چاہتا ہوں۔",
      authenticReference = "Sunan Abi Dawud 30 • Jami' at-Tirmidhi 7 • Sunan Ibn Majah 300 (Sahih) • Hisn al-Muslim #11",
      virtueAndTiming = "Step out of the washroom with the right foot and say 'Ghufranaka' (Sunan Abi Dawud 30; also reported in Ibn Majah 301: 'Alhamdu lillahilladhi adh-haba 'annil-adha wa 'afani').",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "bait ul khala se nikalne ki dua washroom se bahar aane ki dua toilet leaving dua ghufranaka بیت الخلاء سے نکلنے کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/11hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_entering_market_bazar",
      title = "Dua When Entering the Market (Bazar Mein Dakhil Hone Ki Dua / بازار کی دعا)",
      category = DuaCategory.HOME_MASJID,
      occasionBadge = "Entering Market / Shopping",
      arabicText = "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، يُحْيِي وَيُمِيتُ وَهُوَ حَيٌّ لَا يَمُوتُ، بِيَدِهِ الْخَيْرُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
      transliteration = "La ilaha illallahu wahdahu la sharika lahu, lahul-mulku wa lahul-hamdu, yuhyi wa yumitu wa Huwa Hayyun la yamutu, biyadihil-khayru wa Huwa 'ala kulli shay'in Qadir.",
      englishTranslation = "None has the right to be worshipped except Allah alone, Who has no partner. His is the dominion and His is the praise. He gives life and causes death, and He is Ever-Living and does not die. In His Hand is all good, and He is over all things Omnipotent.",
      urduTranslation = "اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے اس کا کوئی شریک نہیں، اسی کی بادشاہی ہے اور اسی کے لیے تمام تعریف ہے، وہی زندہ کرتا ہے اور مارتا ہے اور وہ زندہ ہے جسے کبھی موت نہیں آئے گی، اسی کے ہاتھ میں تمام بھلائی ہے اور وہ ہر چیز پر قادر ہے۔",
      authenticReference = "Jami' at-Tirmidhi 3428 • Sunan Ibn Majah 2235 (Hasan) • Hisn al-Muslim #209",
      virtueAndTiming = "Remembering Allah in a place of heedlessness such as the marketplace carries immense reward and protection.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "bazar ki dua bazaar mein jane ki dua market shopping dua بازار میں داخل ہونے کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/209hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_when_angry_ghussa",
      title = "Dua When Feeling Angry (Gusse Ke Waqt Ki Dua / غصے کے وقت کی دعا)",
      category = DuaCategory.HOME_MASJID,
      occasionBadge = "Controlling Anger",
      arabicText = "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ",
      transliteration = "A'udhu billahi minash-shaytanir-rajim.",
      englishTranslation = "I seek refuge in Allah from Satan, the accursed.",
      urduTranslation = "میں اللہ کی پناہ مانگتا ہوں شیطان مردود کے شر سے۔",
      authenticReference = "Sahih al-Bukhari 6115 • Sahih Muslim 2610 • Hisn al-Muslim #193",
      virtueAndTiming = "The Prophet ﷺ said: 'I know a word which, if he were to say it, what he feels (of anger) would go away: A'udhu billahi minash-shaytanir-rajim.'",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "gusse ki dua ghussa kam karne ki dua anger dua غصے کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/193hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_protection_evil_eye_nazar",
      title = "Dua for Protection from Evil Eye (Nazar-e-Bad Se Hifazat Ki Dua / نظرِ بد کی دعا)",
      category = DuaCategory.HOME_MASJID,
      occasionBadge = "Protection of Children & Family from Evil Eye",
      arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّةِ مِنْ كُلِّ شَيْطَانٍ وَهَامَّةٍ، وَمِنْ كُلِّ عَيْنٍ لَامَّةٍ",
      transliteration = "A'udhu bikalimatillahit-tammati min kulli shaytanin wa hammatin, wa min kulli 'aynin lammah.",
      englishTranslation = "I seek refuge in the Perfect Words of Allah from every devil and poisonous pest, and from every evil, harmful eye.",
      urduTranslation = "میں اللہ کے کامل کلمات کی پناہ مانگتا ہوں ہر شیطان سے، ہر زہریلے جانور سے، اور ہر لگنے والی بری نظر سے۔",
      authenticReference = "Sahih al-Bukhari 3371 (Recited by the Prophet ﷺ for Hasan and Husayn, peace be upon them) • Hisn al-Muslim #146",
      virtueAndTiming = "Prophet Ibrahim (AS) used to seek protection with these words for Isma'il and Ishaq (AS), and Prophet Muhammad ﷺ recited them for Imam Hasan and Imam Husayn (AS).",
      recommendedCount = 3,
      searchKeywordsRomanUrduAndAliases = "nazar ki dua nazar e bad ki dua buri nazar se bachne ki dua bachon ki hifazat evil eye نظر بد کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/146hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_sneezing_chheenk",
      title = "Dua Upon Sneezing & Its Response (Chheenk Aane Aur Jawab Ki Dua / چھینک کی دعا)",
      category = DuaCategory.HOME_MASJID,
      occasionBadge = "Sneezing & Responding (Tashmit al-'Atis)",
      arabicText = "﴿الْعَاطِسُ﴾ الْحَمْدُ لِلَّهِ ۝ ﴿السَّامِعُ﴾ يَرْحَمُكَ اللَّهُ ۝ ﴿الرَّدُّ﴾ يَهْدِيكُمُ اللَّهُ وَيُصْلِحُ بَالَكُمْ",
      transliteration = "One who sneezes: Alhamdulillah. • Listener: YarhamukAllah. • Reply: Yahdikumullahu wa yuslihu balakum.",
      englishTranslation = "When one of you sneezes, let him say: 'All praise is for Allah (Alhamdulillah).' And let his brother or companion say to him: 'May Allah have mercy upon you (YarhamukAllah).' And when he says that, let him reply: 'May Allah guide you and rectify your affairs (Yahdikumullahu wa yuslihu balakum).'",
      urduTranslation = "جب چھینک آئے تو کہے: 'الْحَمْدُ لِلَّهِ' (تمام تعریف اللہ کے لیے ہے)۔ سننے والا جواب میں کہے: 'يَرْحَمُكَ اللَّهُ' (اللہ تم پر رحم فرمائے)۔ پھر چھینکنے والا جواب دے: 'يَهْدِيكُمُ اللَّهُ وَيُصْلِحُ بَالَكُمْ' (اللہ تمہیں ہدایت دے اور تمہارا حال درست فرمائے)۔",
      authenticReference = "Sahih al-Bukhari 6224 (Narrated by Abu Hurayrah, may Allah be pleased with him) • Hisn al-Muslim #188",
      virtueAndTiming = "Responding to a Muslim who sneezes and praises Allah (Tashmit al-'Atis) is one of the mutual rights of a Muslim over another (Sahih al-Bukhari 1240).",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "chheenk ki dua cheenk aane ki dua yarhamukallah yahdikumullahu sneezing dua چھینک کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/188hm.mp3"
    ),

    // =========================================================================
    // 4. QURANIC RABBANA & PROPHETIC DUAS
    // =========================================================================
    DailyDuaItem(
      id = "dua_rabbana_fid_dunya",
      title = "Rabbana Atina Fid-Dunya Hasanah (Good in Both Worlds / ربنا آتنا في الدنيا حسنة)",
      category = DuaCategory.QURANIC_RABBANA,
      occasionBadge = "Most Frequent Dua of the Prophet ﷺ",
      arabicText = "رَبَّنَآ ءَاتِنَا فِى ٱلدُّنْيَا حَسَنَةً وَفِى ٱلْـَٔاخِرَةِ حَسَنَةً وَقِنَا عَذَابَ ٱلنَّارِ",
      transliteration = "Rabbana atina fid-dunya hasanatan wa fil-akhirati hasanatan wa qina 'adhaban-nar.",
      englishTranslation = "Our Lord, grant us good in this world and good in the Hereafter, and protect us from the punishment of the Fire.",
      urduTranslation = "اے ہمارے رب! ہمیں دنیا میں بھی بھلائی عطا فرما اور آخرت میں بھی بھلائی عطا فرما، اور ہمیں آگ کے عذاب سے بچا۔",
      authenticReference = "Surah Al-Baqarah 2:201 • Sahih al-Bukhari 6389",
      virtueAndTiming = "Anas (RA) reported that this was the most frequent supplication of the Prophet ﷺ.",
      recommendedCount = 3,
      quranSurahAyah = 2 to 201,
      searchKeywordsRomanUrduAndAliases = "rabbana atina fid dunya hasanah duniya aur akhirat ki bhalai ki dua",
      humanAudioUrl = "https://everyayah.com/data/Alafasy_128kbps/002201.mp3"
    ),
    DailyDuaItem(
      id = "dua_increase_knowledge_ilm",
      title = "Rabbi Zidni 'Ilma (Dua for Increase in Knowledge / علم میں اضافے کی دعا)",
      category = DuaCategory.QURANIC_RABBANA,
      occasionBadge = "Knowledge, Study & Exams (Ilm Ki Dua)",
      arabicText = "رَّبِّ زِدْنِى عِلْمًا",
      transliteration = "Rabbi zidni 'ilma.",
      englishTranslation = "My Lord, increase me in knowledge.",
      urduTranslation = "اے میرے رب! میرے علم میں اضافہ فرما۔",
      authenticReference = "Surah Ta-Ha 20:114",
      virtueAndTiming = "Commanded by Allah directly to His Messenger ﷺ; recite before studying, exams, and seeking sacred or beneficial knowledge.",
      recommendedCount = 3,
      quranSurahAyah = 20 to 114,
      searchKeywordsRomanUrduAndAliases = "ilm ki dua ilm mein izafay ki dua ilm me izafe ki dua parhai ki dua imtihan exam study rabbi zidni ilma علم کی دعا",
      humanAudioUrl = "https://everyayah.com/data/Alafasy_128kbps/020114.mp3"
    ),
    DailyDuaItem(
      id = "dua_for_parents_walidain",
      title = "Rabbirhamhuma Kama Rabbayani Saghira (Dua for Parents / والدین کے لیے دعا)",
      category = DuaCategory.QURANIC_RABBANA,
      occasionBadge = "Mercy & Forgiveness for Parents (Walidain Ki Dua)",
      arabicText = "رَّبِّ ٱرْحَمْهُمَا كَمَا رَبَّيَانِى صَغِيرًا",
      transliteration = "Rabbirhamhuma kama rabbayani saghira.",
      englishTranslation = "My Lord, have mercy upon them [my parents] as they brought me up when I was small.",
      urduTranslation = "اے میرے رب! ان دونوں (میرے والدین) پر رحم فرما جیسا کہ انہوں نے بچپن میں میری پرورش کی۔",
      authenticReference = "Surah Al-Isra 17:24",
      virtueAndTiming = "Taught by Allah in Surah Al-Isra as the essential supplication of gratitude and mercy for one's mother and father.",
      recommendedCount = 3,
      quranSurahAyah = 17 to 24,
      searchKeywordsRomanUrduAndAliases = "walidain ki dua maa baap ki dua parents dua rabbir hamhuma والدین کے لیے دعا ماں باپ کی دعا",
      humanAudioUrl = "https://everyayah.com/data/Alafasy_128kbps/017024.mp3"
    ),
    DailyDuaItem(
      id = "dua_rabbana_la_tuzigh",
      title = "Rabbana La Tuzigh Qulubana (Steadfastness of Heart / ہدایت پر استقامت کی دعا)",
      category = DuaCategory.QURANIC_RABBANA,
      occasionBadge = "Firmness Upon Guidance",
      arabicText = "رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِن لَّدُنكَ رَحْمَةً ۚ إِنَّكَ أَنتَ ٱلْوَهَّابُ",
      transliteration = "Rabbana la tuzigh qulubana ba'da idh hadaytana wa hab lana min ladunka rahmah, innaka Antal-Wahhab.",
      englishTranslation = "Our Lord, let not our hearts deviate after You have guided us and grant us from Yourself mercy. Indeed, You are the Bestower.",
      urduTranslation = "اے ہمارے رب! ہمارے دلوں کو ٹیڑھا نہ کر بعد اس کے کہ تو نے ہمیں ہدایت دی، اور ہمیں اپنے پاس سے رحمت عطا فرما، بے شک تو ہی بڑا عطا کرنے والا ہے۔",
      authenticReference = "Surah Ali 'Imran 3:8",
      virtueAndTiming = "The supplication of those firmly grounded in knowledge asking Allah for steadfastness upon truth.",
      recommendedCount = 3,
      quranSurahAyah = 3 to 8,
      searchKeywordsRomanUrduAndAliases = "hidayat ki dua dil ki islah istiqamat ki dua rabbana la tuzigh",
      humanAudioUrl = "https://everyayah.com/data/Alafasy_128kbps/003008.mp3"
    ),
    DailyDuaItem(
      id = "dua_rabbij_alni_muqimas_salah",
      title = "Rabbij'alni Muqimas-Salah (For Prayer & Parents / نماز اور والدین کی بخشش کی دعا)",
      category = DuaCategory.QURANIC_RABBANA,
      occasionBadge = "Steadfastness in Salah & Forgiveness for Parents",
      arabicText = "رَبِّ ٱجْعَلْنِى مُقِيمَ ٱلصَّلَوٰةِ وَمِن ذُرِّيَّتِى ۚ رَبَّنَا وَتَقَبَّلْ دُعَآءِ ۝ رَبَّنَا ٱغْفِرْ لِى وَلِوَٰلِدَىَّ وَلِلْمُؤْمِنِينَ يَوْمَ يَقُومُ ٱلْحِسَابُ",
      transliteration = "Rabbij'alni muqimas-salati wa min dhurriyyati, Rabbana wa taqabbal du'a. Rabbanaghfir li wa liwalidayya wa lil-mu'minina yawma yaqumul-hisab.",
      englishTranslation = "My Lord, make me an establisher of prayer, and from my descendants. Our Lord, and accept my supplication. Our Lord, forgive me and my parents and the believers the Day the account is established.",
      urduTranslation = "اے میرے رب! مجھے نماز قائم کرنے والا بنا اور میری اولاد میں سے بھی، اے ہمارے رب! اور میری دعا قبول فرما۔ اے ہمارے رب! مجھے، میرے والدین کو اور تمام مومنوں کو اس دن بخش دے جب حساب قائم ہوگا۔",
      authenticReference = "Surah Ibrahim 14:40-41",
      virtueAndTiming = "Prophet Ibrahim's (AS) comprehensive prayer for establishing Salah, righteous offspring, and forgiveness for parents.",
      recommendedCount = 1,
      quranSurahAyah = 14 to 40,
      searchKeywordsRomanUrduAndAliases = "namaz ki pabandi ki dua walidain ki maghfirat ki dua rabbij alni muqimas salah",
      humanAudioUrl = "https://everyayah.com/data/Alafasy_128kbps/014040.mp3|https://everyayah.com/data/Alafasy_128kbps/014041.mp3"
    ),
    DailyDuaItem(
      id = "dua_rabbana_hab_lana",
      title = "Rabbana Hab Lana Min Azwajina (Righteous Family / نیک اولاد اور اہلِ خانہ کی دعا)",
      category = DuaCategory.QURANIC_RABBANA,
      occasionBadge = "Spouses, Children & Leadership in Taqwa",
      arabicText = "رَبَّنَا هَبْ لَنَا مِنْ أَزْوَٰجِنَا وَذُرِّيَّٰتِنَا قُرَّةَ أَعْيُنٍ وَٱجْعَلْنَا لِلْمُتَّقِينَ إِمَامًا",
      transliteration = "Rabbana hab lana min azwajina wa dhurriyyatina qurrata a'yunin waj'alna lil-muttaqina imama.",
      englishTranslation = "Our Lord, grant us from among our spouses and offspring comfort to our eyes and make us an example for the righteous.",
      urduTranslation = "اے ہمارے رب! ہمیں ہماری بیویوں اور ہماری اولاد سے آنکھوں کی ٹھنڈک عطا فرما اور ہمیں پرہیزگاروں کا پیشوا بنا۔",
      authenticReference = "Surah Al-Furqan 25:74",
      virtueAndTiming = "The supplication of 'Ibad ar-Rahman (the Servants of the Most Merciful) for family harmony and piety.",
      recommendedCount = 1,
      quranSurahAyah = 25 to 74,
      searchKeywordsRomanUrduAndAliases = "aulad ki dua nek biwi aur bachon ki dua family dua rabbana hab lana",
      humanAudioUrl = "https://everyayah.com/data/Alafasy_128kbps/025074.mp3"
    ),
    DailyDuaItem(
      id = "dua_rabbishrah_li_sadri",
      title = "Rabbishrah Li Sadri (Ease, Knowledge & Speech / شرح صدر اور آسانی کی دعا)",
      category = DuaCategory.QURANIC_RABBANA,
      occasionBadge = "Study, Confidence & Beneficial Knowledge",
      arabicText = "رَبِّ ٱشْرَحْ لِى صَدْرِى ۝ وَيَسِّرْ لِىٓ أَمْرِى ۝ وَٱحْلُلْ عُقْدَةً مِّن لِّسَانِى ۝ يَفْقَهُوا۟ قَوْلِى",
      transliteration = "Rabbishrah li sadri, wa yassir li amri, wahlul 'uqdatan min lisani, yafqahu qawli.",
      englishTranslation = "My Lord, expand for me my breast [with assurance], and ease for me my task, and untie the knot from my tongue, that they may understand my speech.",
      urduTranslation = "اے میرے رب! میرے لیے میرا سینہ کھول دے، اور میرے لیے میرا کام آسان فرما دے، اور میری زبان کی گرہ کھول دے، تاکہ لوگ میری بات سمجھ سکیں۔",
      authenticReference = "Surah Ta-Ha 20:25-28",
      virtueAndTiming = "Prophet Musa's (AS) supplication before speaking, studying, teaching, or undertaking an important task.",
      recommendedCount = 1,
      quranSurahAyah = 20 to 25,
      searchKeywordsRomanUrduAndAliases = "rabbishrah li sadri imtihan ki dua speech confidence kaam asan hone ki dua",
      humanAudioUrl = "https://everyayah.com/data/Alafasy_128kbps/020025.mp3|https://everyayah.com/data/Alafasy_128kbps/020026.mp3|https://everyayah.com/data/Alafasy_128kbps/020027.mp3|https://everyayah.com/data/Alafasy_128kbps/020028.mp3"
    ),

    // =========================================================================
    // 5. SPECIAL OCCASIONS DUAS (INCLUDING JANAZA & FUNERAL SUPPLICATIONS)
    // =========================================================================
    DailyDuaItem(
      id = "dua_jumuah_salawat_ibrahimiyyah",
      title = "Salawat Ibrahimiyyah (Durood-e-Ibrahimi / درودِ ابراہیمی)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.JUMUAH_FRIDAY,
      occasionBadge = "Jumu'ah (Friday) Special • Salawat",
      arabicText = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ، اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ",
      transliteration = "Allahumma salli 'ala Muhammadin wa 'ala ali Muhammadin, kama sallayta 'ala Ibrahima wa 'ala ali Ibrahima, innaka Hamidun Majid. Allahumma barik 'ala Muhammadin wa 'ala ali Muhammadin, kama barakta 'ala Ibrahima wa 'ala ali Ibrahima, innaka Hamidun Majid.",
      englishTranslation = "O Allah, send peace and blessings upon Muhammad and upon the family of Muhammad, as You sent peace and blessings upon Ibrahim and upon the family of Ibrahim; indeed, You are Praiseworthy, Glorious. O Allah, bless Muhammad and the family of Muhammad, as You blessed Ibrahim and the family of Ibrahim; indeed, You are Praiseworthy, Glorious.",
      urduTranslation = "اے اللہ! محمد ﷺ پر اور آلِ محمد ﷺ پر رحمت نازل فرما جس طرح تو نے ابراہیم علیہ السلام اور آلِ ابراہیم پر رحمت نازل فرمائی، بے شک تو تعریف والا، بزرگی والا ہے۔ اے اللہ! محمد ﷺ پر اور آلِ محمد ﷺ پر برکت نازل فرما جس طرح تو نے ابراہیم علیہ السلام اور آلِ ابراہیم پر برکت نازل فرمائی، بے شک تو تعریف والا، بزرگی والا ہے۔",
      authenticReference = "Sahih al-Bukhari 3370 • Sunan Abi Dawud 1047 (Increasing Salawat on Friday) • Hisn al-Muslim #53",
      virtueAndTiming = "The Prophet ﷺ said: 'Increase your supplications for blessings upon me on Friday and the night of Friday, for your Salawat is presented to me.'",
      recommendedCount = 10,
      searchKeywordsRomanUrduAndAliases = "durood sharif durood ibrahimi salawat jumuah ki dua درود شریف",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/53hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_ramadan_sehri_niyyah",
      title = "Sehri Dua (Roza Rakhne Ki Dua / سحری اور روزے کی دعا)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.RAMADAN_IFTAR,
      occasionBadge = "Ramadan & Fasting • Sehri Dua",
      arabicText = "وَبِصَوْمِ غَدٍ نَّوَيْتُ مِنْ شَهْرِ رَمَضَانَ",
      transliteration = "Wa bisawmi ghadin nawaytu min shahri Ramadan (Wabi soum gadan nowut min shahri Ramadan)",
      englishTranslation = "And I intend to keep the fast of tomorrow in the month of Ramadan.",
      urduTranslation = "اور میں نے ماہِ رمضان کے کل کے روزے کی نیت کی۔",
      authenticReference = "Sunan an-Nasa'i 2331 • Sahih al-Bukhari 1 & 1923 • Traditional Sehri Niyyah Formula",
      virtueAndTiming = "Form the sincere intention before Fajr to fast tomorrow for Allah's sake and partake of the blessed pre-dawn Suhoor (Sehri) meal.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "sehri ki dua sehri ki niyat roza rakhne ki dua suhoor dua wabi soum gadan nowut wa bisawmi ghadin nawaytu سحری کی دعا روزہ رکھنے کی نیت",
      humanAudioUrl = null
    ),
    DailyDuaItem(
      id = "dua_ramadan_iftar",
      title = "Iftari Dua (Roza Kholne Ki Dua / افطار کی مسنون دعا)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.RAMADAN_IFTAR,
      occasionBadge = "Ramadan & Sunnah Fasting • Iftari Dua (Sunan Abi Dawud 2357)",
      arabicText = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ، وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللَّهُ",
      transliteration = "Zahaba al-zama'u wabtallatil-'urooqu wa thabatal-ajru in sha' Allah (Dhahabadh-dhama'u wabtallatil-'uruqu, wa thabatal-ajru in sha'Allah)",
      englishTranslation = "The thirst is gone, the veins are moistened, and the reward is confirmed, if Allah wills.",
      urduTranslation = "پیاس چلی گئی، رگیں تر ہو گئیں، اور ان شاء اللہ اجر ثابت ہو گیا۔",
      authenticReference = "Sunan Abi Dawud 2357 (Graded Hasan — narrated by Abdullah ibn 'Umar RA) • Hisn al-Muslim #176",
      virtueAndTiming = "Break the fast at Maghrib sunset with dates or water saying 'Bismillah', then recite this authentic Prophetic supplication (Sunan Abi Dawud 2357).",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "iftar ki dua roza kholne ki dua roza iftar dua zahaba al-zama'u wabtallatil-'urooqu dhahabadh-dhama'u افطار کی دعا روزہ کھولنے کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/176hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_ramadan_first_ashra_mercy",
      title = "1st Ashra of Ramadan — Days 1–10: Supplication for Mercy (پہلا عشرہ رحمت کی دعا)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.RAMADAN_IFTAR,
      occasionBadge = "Ramadan Days 1–10 • First Ashra (Mercy / رحمت)",
      arabicText = "رَّبِّ ٱغْفِرْ وَٱرْحَمْ وَأَنتَ خَيْرُ ٱلرَّٰحِمِينَ",
      transliteration = "Rabbigh-fir warham wa Anta khayrur-rahimin.",
      englishTranslation = "My Lord, forgive and have mercy, and You are the Best of the merciful.",
      urduTranslation = "اے میرے رب! بخش دے اور رحم فرما، اور تو سب سے بہتر رحم فرمانے والا ہے۔",
      authenticReference = "Surah Al-Mu'minun 23:118",
      virtueAndTiming = "Recite abundantly during the first ten days (First Ashra) of Ramadan to seek Allah's encompassing mercy.",
      recommendedCount = 3,
      quranSurahAyah = 23 to 118,
      searchKeywordsRomanUrduAndAliases = "pehle ashre ki dua first ashra dua ramazan rahmat ki dua پہلا عشرہ رحمت",
      humanAudioUrl = "https://everyayah.com/data/Alafasy_128kbps/023118.mp3"
    ),
    DailyDuaItem(
      id = "dua_ramadan_second_ashra_forgiveness",
      title = "2nd Ashra of Ramadan — Days 11–20: Supplication for Forgiveness (دوسرا عشرہ مغفرت کی دعا)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.RAMADAN_IFTAR,
      occasionBadge = "Ramadan Days 11–20 • Second Ashra (Forgiveness / مغفرت)",
      arabicText = "أَسْتَغْفِرُ اللَّهَ رَبِّي مِنْ كُلِّ ذَنْبٍ وَأَتُوبُ إِلَيْهِ",
      transliteration = "Astaghfirullaha Rabbi min kulli dhanbin wa atubu ilayh.",
      englishTranslation = "I seek forgiveness from Allah, my Lord, from every sin, and I turn to Him in repentance.",
      urduTranslation = "میں اپنے رب اللہ سے ہر گناہ کی بخشش مانگتا ہوں اور اسی کی طرف توبہ کرتا ہوں۔",
      authenticReference = "Sahih al-Bukhari 6307 • Sahih Muslim 2702 (Prophetic Istighfar & Repentance) • Hisn al-Muslim #248",
      virtueAndTiming = "Recite frequently throughout the middle ten days (Second Ashra) of Ramadan for sincere repentance and forgiveness of sins.",
      recommendedCount = 7,
      searchKeywordsRomanUrduAndAliases = "doosre ashre ki dua second ashra dua maghfirat ki dua دوسرا عشرہ مغفرت",
      humanAudioUrl = null
    ),
    DailyDuaItem(
      id = "dua_ramadan_third_ashra_nijat",
      title = "3rd Ashra of Ramadan — Days 21–30: Protection from the Fire (تیسرا عشرہ نجات کی دعا)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.LAYLATUL_QADR,
      occasionBadge = "Ramadan Days 21–30 • Third Ashra (Refuge from Fire / نجات)",
      arabicText = "اللَّهُمَّ أَجِرْنِي مِنَ النَّارِ",
      transliteration = "Allahumma ajirni minan-nar.",
      englishTranslation = "O Allah, protect and save me from the Fire of Hell.",
      urduTranslation = "اے اللہ! مجھے دوزخ کی آگ سے پناہ اور نجات عطا فرما۔",
      authenticReference = "Sunan Abi Dawud 5079 • Sahih Ibn Hibban 2022",
      virtueAndTiming = "Recite 7 times after Fajr and Maghrib and throughout the final ten days and nights of Ramadan.",
      recommendedCount = 7,
      searchKeywordsRomanUrduAndAliases = "teesre ashre ki dua third ashra dua jahannam se nijat ki dua تیسرا عشرہ نجات",
      humanAudioUrl = "https://raw.githubusercontent.com/islamicapi/masnun-dua/main/audio/249.mp3"
    ),
    DailyDuaItem(
      id = "dua_laylatul_qadr",
      title = "Dua for Laylatul Qadr (Shab-e-Qadr Ki Dua / شبِ قدر کی دعا)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.LAYLATUL_QADR,
      occasionBadge = "Laylatul Qadr • Last 10 Nights of Ramadan",
      arabicText = "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي",
      transliteration = "Allahumma innaka 'Afuwwun tuhibbul-'afwa fa'fu 'anni.",
      englishTranslation = "O Allah, indeed You are Pardoning; You love pardon, so pardon me.",
      urduTranslation = "اے اللہ! بے شک تو بہت معاف کرنے والا ہے، معاف کرنے کو پسند فرماتا ہے، پس مجھے معاف فرما دے۔",
      authenticReference = "Jami' at-Tirmidhi 3513 • Sunan Ibn Majah 3850 (Narrated by Aishah, may Allah be pleased with her — Sahih)",
      virtueAndTiming = "Taught directly by the Prophet ﷺ to Aishah (RA) when she asked what to say if she knew which night was Laylatul Qadr.",
      recommendedCount = 7,
      searchKeywordsRomanUrduAndAliases = "shab e qadr ki dua laylatul qadr ki dua lailatul qadr شب قدر کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/islamicapi/masnun-dua/main/audio/310.mp3"
    ),
    DailyDuaItem(
      id = "dua_new_moon_hilal",
      title = "Dua Upon Sighting the New Moon (Naya Chand Dekhne Ki Dua / نیا چاند دیکھنے کی دعا)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.EID_AND_HILAL,
      occasionBadge = "New Hijri Month & Eid Crescent",
      arabicText = "اللَّهُمَّ أَهِلَّهُ عَلَيْنَا بِالْيُمْنِ وَالْإِيمَانِ، وَالسَّلَامَةِ وَالْإِسْلَامِ، رَبِّي وَرَبُّكَ اللَّهُ",
      transliteration = "Allahumma ahillahu 'alayna bil-yumni wal-iman, was-salamati wal-Islam, Rabbi wa RabbukAllah.",
      englishTranslation = "O Allah, bring this crescent upon us with blessing, faith, safety, and Islam. My Lord and your Lord is Allah.",
      urduTranslation = "اے اللہ! اس چاند کو ہم پر برکت، ایمان، سلامتی اور اسلام کے ساتھ طلوع فرما۔ (اے چاند!) میرا اور تیرا رب اللہ ہے۔",
      authenticReference = "Jami' at-Tirmidhi 3451 (Narrated by Talhah ibn Ubaydullah, may Allah be pleased with him — Hasan Sahih) • Hisn al-Muslim #175",
      virtueAndTiming = "Recited whenever sighting the new crescent moon at the start of Ramadan, Shawwal (Eid al-Fitr), Dhul-Hijjah, or any Hijri month.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "chand dekhne ki dua naya chand dekhne ki dua hilal crescent moon چاند دیکھنے کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/islamicapi/masnun-dua/main/audio/312.mp3"
    ),
    DailyDuaItem(
      id = "dua_eid_greeting",
      title = "Dua & Greeting on the Day of Eid (Eid Ki Mubarakbad Ki Dua / عید کی دعا)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.EID_AND_HILAL,
      occasionBadge = "Eid al-Fitr & Eid al-Adha",
      arabicText = "تَقَبَّلَ اللَّهُ مِنَّا وَمِنْكُمْ",
      transliteration = "Taqabbalallahu minna wa minkum.",
      englishTranslation = "May Allah accept [good deeds] from us and from you.",
      urduTranslation = "اللہ تعالیٰ ہماری اور آپ کی (نیک عبادات) قبول فرمائے۔",
      authenticReference = "Al-Bayhaqi (As-Sunan al-Kubra 3/319) • Fath al-Bari (2/446 — Authenticated by Ibn Hajar)",
      virtueAndTiming = "The Companions of the Prophet ﷺ used to greet one another with these words when meeting on the day of Eid.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "eid ki dua taqabbalallahu minna wa minkum عید کی دعا",
      humanAudioUrl = null
    ),

    // =========================================================================
    // JANAZA, FUNERAL PRAYER, BURIAL & VISITING GRAVES SUPPLICATIONS
    // =========================================================================
    DailyDuaItem(
      id = "dua_janaza_adult",
      title = "Salat al-Janaza Dua for Deceased Adult Men & Women (Namaz-e-Janaza Ki Dua / نمازِ جنازہ کی مسنون دعا)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.JANAZA_FUNERAL,
      occasionBadge = "Janaza Prayer (After 3rd Takbeer)",
      arabicText = "اللَّهُمَّ اغْفِرْ لِحَيِّنَا وَمَيِّتِنَا، وَشَاهِدِنَا وَغَائِبِنَا، وَصَغِيرِنَا وَكَبِيرِنَا، وَذَكَرِنَا وَأُنْثَانَا، اللَّهُمَّ مَنْ أَحْيَيْتَهُ مِنَّا فَأَحْيِهِ عَلَى الْإِسْلَامِ، وَمَنْ تَوَفَّيْتَهُ مِنَّا فَتَوَفَّهُ عَلَى الْإِيمَانِ، اللَّهُمَّ لَا تَحْرِمْنَا أَجْرَهُ وَلَا تُضِلَّنَا بَعْدَهُ",
      transliteration = "Allahummaghfir lihayyina wa mayyitina, wa shahidina wa gha'ibina, wa saghirina wa kabirina, wa dhakarina wa unthana. Allahumma man ahyaytahu minna fa-ahyihi 'alal-Islam, wa man tawaffaytahu minna fatawaffahu 'alal-iman. Allahumma la tahrimna ajrahu wa la tudillana ba'dahu.",
      englishTranslation = "O Allah, forgive our living and our deceased, those of us who are present and those who are absent, our young and our old, our males and our females. O Allah, whomever among us You keep alive, keep him alive upon Islam, and whomever among us You cause to die, cause him to die upon Faith (Iman). O Allah, do not deprive us of his reward and do not let us go astray after him.",
      urduTranslation = "اے اللہ! ہمارے زندوں اور ہمارے فوت شدگان کی مغفرت فرما، ہمارے حاضر اور غائب کو، ہمارے چھوٹوں اور بڑوں کو، اور ہمارے مردوں اور عورتوں کو بخش دے۔ اے اللہ! ہم میں سے جسے تو زندہ رکھے اسے اسلام پر زندہ رکھ، اور ہم میں سے جسے تو وفات دے اسے ایمان پر وفات نصیب فرما۔ اے اللہ! ہمیں اس کے اجر سے محروم نہ فرما اور اس کے بعد ہمیں گمراہی میں نہ ڈال۔",
      authenticReference = "Sunan Abi Dawud 3201 • Jami' at-Tirmidhi 1024 • Sunan Ibn Majah 1498 (Sahih) • Hisn al-Muslim #157",
      virtueAndTiming = "Recited in Salat al-Janaza after the 3rd Takbeer (following Sana after the 1st Takbeer and Durood-e-Ibrahimi after the 2nd Takbeer).",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "namaz e janaza ki dua janaze ki dua mayyat ki dua बालिग مرد و عورت کے جنازے کی دعا allahummaghfir lihayyina wa mayyitina نماز جنازہ کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/157hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_janaza_adult_comprehensive",
      title = "Prophetic Janaza Dua for Mercy & Purification of the Deceased (اللهم اغفر له وارحمه)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.JANAZA_FUNERAL,
      occasionBadge = "Salat al-Janaza • Sahih Muslim 963",
      arabicText = "اللَّهُمَّ اغْفِرْ لَهُ وَارْحَمْهُ، وَعَافِهِ وَاعْفُ عَنْهُ، وَأَكْرِمْ نُزُلَهُ، وَوَسِّعْ مُدْخَلَهُ، وَاغْسِلْهُ بِالْمَاءِ وَالثَّلْجِ وَالْبَرَدِ، وَنَقِّهِ مِنَ الْخَطَايَا كَمَا نَقَّيْتَ الثَّوْبَ الْأَبْيَضَ مِنَ الدَّنَسِ، وَأَبْدِلْهُ دَارًا خَيْرًا مِنْ دَارِهِ، وَأَهْلًا خَيْرًا مِنْ أَهْلِهِ، وَأَدْخِلْهُ الْجَنَّةَ، وَأَعِذْهُ مِنْ عَذَابِ الْقَبْرِ وَعَذَابِ النَّارِ",
      transliteration = "Allahummaghfir lahu warhamhu, wa 'afihi wa'fu 'anhu, wa akrim nuzulahu, wa wassi' mudkhalahu, waghsilhu bil-ma'i wath-thalji wal-barad, wa naqqihi minal-khataya kama naqqaytath-thawbal-abyada minad-danas, wa abdilhu daran khayran min darihi, wa ahlan khayran min ahlihi, wa adkhilhul-Jannah, wa a'idh-hu min 'adhabil-qabri wa 'adhabin-nar.",
      englishTranslation = "O Allah, forgive him and have mercy on him, keep him safe and pardon him, honor his resting place and widen his entrance; wash him with water, snow, and hail, and cleanse him of sins as a white garment is cleansed of dirt; grant him a home better than his home and a family better than his family, admit him into Paradise, and protect him from the punishment of the grave and the punishment of the Fire.",
      urduTranslation = "اے اللہ! اس کی مغفرت فرما اور اس پر رحم فرما، اسے عافیت دے اور اس سے درگزر فرما، اس کی مہمانی باعزت بنا اور اس کی قبر کو کشادہ فرما، اسے پانی، برف اور اولوں سے دھو دے اور گناہوں سے ایسے پاک و صاف کر دے جیسے سفید کپڑا میل کچیل سے صاف کیا جاتا ہے، اسے اس کے گھر سے بہتر گھر اور اس کے گھر والوں سے بہتر اہل عطا فرما، اسے جنت میں داخل فرما اور اسے عذابِ قبر اور عذابِ دوزخ سے محفوظ رکھ۔",
      authenticReference = "Sahih Muslim 963 (Narrated by 'Awf ibn Malik RA: 'I wished that I had been that deceased person') • Hisn al-Muslim #156",
      virtueAndTiming = "Recited by the Prophet ﷺ over a deceased Muslim in Salat al-Janaza.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "janaza dua sahih muslim allahummaghfir lahu warhamhu mayyat ki maghfirat ki dua جنازے کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/156hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_janaza_child_boy_girl",
      title = "Janaza Dua for a Deceased Child — Boy & Girl (Nabaligh Ladke Aur Ladki Ke Janaze Ki Dua / نابالغ بچے اور بچی کے جنازے کی دعا)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.JANAZA_FUNERAL,
      occasionBadge = "Child Janaza (Boy & Girl Forms)",
      arabicText = "﴿لِلصَّبِيِّ / لَڑکے کے لیے﴾ اللَّهُمَّ أَعِذْهُ مِنْ عَذَابِ الْقَبْرِ ۝ اللَّهُمَّ اجْعَلْهُ لَنَا فَرَطًا وَسَلَفًا وَأَجْرًا (وَذُخْرًا وَشَافِعًا مُشَفَّعًا)\n﴿لِلصَّبِيَّةِ / لَڑکی کے لیے﴾ اللَّهُمَّ اجْعَلْهَا لَنَا فَرَطًا وَسَلَفًا وَأَجْرًا (وَذُخْرًا وَشَافِعَةً مُشَفَّعَةً)",
      transliteration = "For a Boy: Allahumma a'idh-hu min 'adhabil-qabr. Allahummaj'alhu lana faratan wa salafan wa ajran (wa dhukhran wa shafi'an mushaffa'an).\nFor a Girl: Allahummaj'alha lana faratan wa salafan wa ajran (wa dhukhran wa shafi'atan mushaffa'atan).",
      englishTranslation = "O Allah, protect him from the punishment of the grave. O Allah, make him (or for a girl: make her) for us a precursor, a forerunner, and a source of reward and treasure (and an accepted intercessor).",
      urduTranslation = "(نابالغ لڑکے کے لیے:) اے اللہ! اسے عذابِ قبر سے محفوظ رکھ۔ اے اللہ! اس بچے کو ہمارے لیے آگے جا کر سامانِ آخرت کرنے والا، پیش رو اور اجر و ذخیرہ (اور مقبول شفاعت کرنے والا) بنا۔\n(نابالغ لڑکی کے لیے:) اے اللہ! اس بچی کو ہمارے لیے پیش رو، اجر و ذخیرہ اور مقبول شفاعت کرنے والی بنا۔",
      authenticReference = "Sahih al-Bukhari 1335 (Chapter on Janaza of Children) • Sunan al-Bayhaqi 4/38 • Al-Muwatta 538 • Hisn al-Muslim #160–161",
      virtueAndTiming = "Recited after the 3rd Takbeer in the funeral prayer of a minor child (using the masculine pronoun 'hu' for a boy and feminine pronoun 'ha' for a girl).",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "bache ke janaze ki dua bachi ke janaze ki dua nabaligh ke janaze ki dua allahummaj alhu lana faratan بچے کے جنازے کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/160hm.mp3|https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/161hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_janaza_placing_in_grave",
      title = "Dua When Placing the Deceased in the Grave (Mayyat Ko Qabar Mein Utarne Ki Dua / قبر میں اتارنے کی دعا)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.JANAZA_FUNERAL,
      occasionBadge = "Burial (Tadfeen) • Lowering into Grave",
      arabicText = "بِسْمِ اللَّهِ وَعَلَى سُنَّةِ رَسُولِ اللَّهِ (وَعَلَى مِلَّةِ رَسُولِ اللَّهِ)",
      transliteration = "Bismillahi wa 'ala sunnati Rasulillah (wa 'ala millati Rasulillah).",
      englishTranslation = "In the Name of Allah, and upon the Sunnah (and religion) of the Messenger of Allah.",
      urduTranslation = "اللہ کے نام سے، اور رسول اللہ ﷺ کی سنت (اور ملت/طریقے) پر۔",
      authenticReference = "Sunan Abi Dawud 3213 • Jami' at-Tirmidhi 1046 (Narrated by Abdullah ibn 'Umar RA — Sahih) • Hisn al-Muslim #163",
      virtueAndTiming = "Recited when gently placing the deceased Muslim into the grave facing the Qiblah.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "mayyat ko qabar mein rakhne ki dua dafan karne ki dua bismillahi wa ala millati rasulillah قبر میں رکھنے کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/163hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_janaza_visiting_graves",
      title = "Dua When Visiting the Graveyard (Qabristan Jane Ki Dua / قبرستان میں داخل ہونے کی دعا)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.JANAZA_FUNERAL,
      occasionBadge = "Visiting Graves (Ziyarat al-Qubur)",
      arabicText = "السَّلَامُ عَلَيْكُمْ أَهْلَ الدِّيَارِ مِنَ الْمُؤْمِنِينَ وَالْمُسْلِمِينَ، وَإِنَّا إِنْ شَاءَ اللَّهُ بِكُمْ لَاحِقُونَ، نَسْأَلُ اللَّهَ لَنَا وَلَكُمُ الْعَافِيَةَ",
      transliteration = "As-salamu 'alaykum ahlad-diyari minal-mu'minina wal-muslimina, wa inna in sha' Allahu bikum lahiqun, nas'alullaha lana wa lakumul-'afiyah.",
      englishTranslation = "Peace be upon you, O dwellers of these abodes from among the believers and the Muslims. Indeed, if Allah wills, we shall join you. We ask Allah for well-being and forgiveness for us and for you.",
      urduTranslation = "سلامتی ہو تم پر اے اس بستی (قبرستان) کے رہنے والے مومنو اور مسلمانو! اور بے شک ہم بھی ان شاء اللہ تم سے ملنے والے ہیں، ہم اللہ سے اپنے لیے اور تمہارے لیے عافیت اور بخشش کا سوال کرتے ہیں۔",
      authenticReference = "Sahih Muslim 975 (Narrated by Buraydah ibn al-Husayb, may Allah be pleased with him) • Hisn al-Muslim #165",
      virtueAndTiming = "Taught by the Prophet ﷺ to his Companions whenever they went out to visit the graves.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "qabristan ki dua qabar par jane ki dua as salamu alaykum ahlad diyari قبرستان کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/165hm.mp3"
    ),

    DailyDuaItem(
      id = "dua_safar_travel",
      title = "Dua for Travelling & Boarding a Vehicle (Safar Ki Dua / سفر کی دعا)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.SAFAR_TRAVEL,
      occasionBadge = "Safar • Beginning a Journey",
      arabicText = "سُبْحَٰنَ ٱلَّذِى سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُۥ مُقْرِنِينَ ۝ وَإِنَّآ إِلَىٰ رَبِّنَا لَمُنقَلِبُونَ",
      transliteration = "Subhanalladhi sakh-khara lana hadha wa ma kunna lahu muqrinin, wa inna ila Rabbina lamunqalibun.",
      englishTranslation = "Glory be to Him Who has subjected this to us, and we could never have it by our efforts. And verily, to our Lord we indeed are to return.",
      urduTranslation = "پاک ہے وہ ذات جس نے اس سواری کو ہمارے تابع کر دیا حالانکہ ہم اسے قابو میں لانے کی طاقت نہیں رکھتے تھے، اور بے شک ہم اپنے رب ہی کی طرف لوٹ کر جانے والے ہیں۔",
      authenticReference = "Surah Az-Zukhruf 43:13-14 • Sahih Muslim 1342 • Hisn al-Muslim #207",
      virtueAndTiming = "Say 'Bismillah', praise Allah, say 'Allahu Akbar' (3x), and recite this Quranic & Prophetic travel supplication when embarking on any journey.",
      recommendedCount = 1,
      quranSurahAyah = 43 to 13,
      searchKeywordsRomanUrduAndAliases = "safar ki dua safar shuru karne ki dua sawari ki dua gari mein baithne ki dua travel journey dua سفر کی دعا سواری کی دعا",
      humanAudioUrl = "https://everyayah.com/data/Alafasy_128kbps/043013.mp3|https://everyayah.com/data/Alafasy_128kbps/043014.mp3"
    ),
    DailyDuaItem(
      id = "dua_rain_falling",
      title = "Dua When Rain Falls (Barish Ki Dua / بارش کے وقت کی دعا)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.RAIN_WEATHER,
      occasionBadge = "Rain & Weather",
      arabicText = "اللَّهُمَّ صَيِّبًا نَافِعًا",
      transliteration = "Allahumma sayyiban nafi'an.",
      englishTranslation = "O Allah, make it a beneficial raincloud.",
      urduTranslation = "اے اللہ! اسے نفع بخش اور فائدہ مند بارش بنا دے۔",
      authenticReference = "Sahih al-Bukhari 1032 (Narrated by Aishah, may Allah be pleased with her) • Hisn al-Muslim #172",
      virtueAndTiming = "The time of rainfall is a blessed time when supplications are accepted by Allah.",
      recommendedCount = 3,
      searchKeywordsRomanUrduAndAliases = "barish ki dua baarish hone ki dua rain dua بارش کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/172hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_illness_shifa",
      title = "Dua for Healing & Visiting the Sick (Bimari Aur Shifa Ki Dua / بیماری سے شفا کی دعا)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.ILLNESS_SHIFA,
      occasionBadge = "Illness, Pain & Shifa",
      arabicText = "اللَّهُمَّ رَبَّ النَّاسِ، أَذْهِبِ الْبَأْسَ، اشْفِ أَنْتَ الشَّافِي، لَا شِفَاءَ إِلَّا شِفَاؤُكَ، شِفَاءً لَا يُغَادِرُ سَقَمًا",
      transliteration = "Allahumma Rabban-nasi, adh-hibil-ba'sa, ishfi Antash-Shafi, la shifa'a illa shifa'uka, shifa'an la yughadiru saqama.",
      englishTranslation = "O Allah, Lord of mankind, remove the affliction; cure, for You are the Healer. There is no cure except Your cure — a cure that leaves no illness behind.",
      urduTranslation = "اے اللہ! انسانوں کے رب! تکلیف کو دور فرما دے، شفا عطا فرما کیونکہ تو ہی شفا دینے والا ہے، تیری شفا کے سوا کوئی شفا نہیں، ایسی شفا عطا فرما جو کسی بیماری کو باقی نہ چھوڑے۔",
      authenticReference = "Sahih al-Bukhari 5743 • Sahih Muslim 2191 • Hisn al-Muslim #147",
      virtueAndTiming = "The Prophet ﷺ used to place his right hand on the patient and recite this supplication for complete healing.",
      recommendedCount = 3,
      searchKeywordsRomanUrduAndAliases = "bimari ki dua beemari ki dua shifa ki dua mareez ki ayadat ki dua bukhar dard illness healing بیماری کی دعا شفا کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/islamicapi/masnun-dua/main/audio/534.mp3"
    ),
    DailyDuaItem(
      id = "dua_distress_yunus",
      title = "Dua of Prophet Yunus (AS) in Distress (Ayat-e-Karima / آیتِ کریمہ)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.DISTRESS_ANXIETY,
      occasionBadge = "Relief from Distress & Anxiety",
      arabicText = "لَّآ إِلَٰهَ إِلَّآ أَنتَ سُبْحَٰنَكَ إِنِّى كُنتُ مِنَ ٱلظَّٰلِمِينَ",
      transliteration = "La ilaha illa Anta Subhanaka inni kuntu minaz-zalimin.",
      englishTranslation = "There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.",
      urduTranslation = "تیرے سوا کوئی معبود نہیں، تو پاک ہے، بے شک میں ہی ظالموں (قصور واروں) میں سے تھا۔",
      authenticReference = "Surah Al-Anbiya 21:87 • Jami' at-Tirmidhi 3505 (Sahih) • Hisn al-Muslim #124",
      virtueAndTiming = "The Prophet ﷺ said: 'No Muslim supplicates with this for anything, ever, except that Allah responds to him.'",
      recommendedCount = 7,
      quranSurahAyah = 21 to 87,
      searchKeywordsRomanUrduAndAliases = "ayat e karima dua e yunus pareshani ki dua mushkil waqt ki dua مصیبت اور پریشانی کی دعا",
      humanAudioUrl = "https://everyayah.com/data/Alafasy_128kbps/021087.mp3"
    ),
    DailyDuaItem(
      id = "dua_anxiety_debt",
      title = "Dua for Relief from Worry, Sorrow & Debt (Pareshani Aur Qarz Se Nijat Ki Dua)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.DISTRESS_ANXIETY,
      occasionBadge = "Anxiety, Grief & Financial Relief",
      arabicText = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْبُخْلِ وَالْجُبْنِ، وَضَلَعِ الدَّيْنِ وَغَلَبَةِ الرِّجَالِ",
      transliteration = "Allahumma inni a'udhu bika minal-hammi wal-hazan, wal-'ajzi wal-kasal, wal-bukhli wal-jubn, wa dala'id-dayni wa ghalabatir-rijal.",
      englishTranslation = "O Allah, I seek refuge in You from worry and grief, from incapacity and laziness, from miserliness and cowardice, and from being heavily in debt and being overpowered by men.",
      urduTranslation = "اے اللہ! میں فقر و پریشانی اور غم سے، عاجزی اور سستی سے، بخل اور بزدلی سے، اور قرض کے بوجھ اور لوگوں کے غلبے سے تیری پناہ مانگتا ہوں۔",
      authenticReference = "Sahih al-Bukhari 6369 (Narrated by Anas ibn Malik, may Allah be pleased with him) • Hisn al-Muslim #121",
      virtueAndTiming = "Anas (RA) served the Prophet ﷺ for years and heard him recite this comprehensive protection frequently.",
      recommendedCount = 3,
      searchKeywordsRomanUrduAndAliases = "gham aur pareshani ki dua qarz ki dua karz se nijat ki dua depression anxiety قرض اور پریشانی کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/121hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_relief_from_debt_qarz",
      title = "Dua for Paying Off Debt & Halal Sufficiency (Qarz Utarne Ki Dua / قرض کی ادائیگی کی دعا)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.DISTRESS_ANXIETY,
      occasionBadge = "Debt Relief & Halal Sustenance (Rizq)",
      arabicText = "اللَّهُمَّ اكْفِنِي بِحَلَالِكَ عَنْ حَرَامِكَ، وَأَغْنِنِي بِفَضْلِكَ عَمَّنْ سِوَاكَ",
      transliteration = "Allahummak-fini bihalalika 'an haramika, wa aghnini bifadlika 'amman siwak.",
      englishTranslation = "O Allah, suffice me with what You have made lawful against what You have made unlawful, and make me independent of all others besides You by Your bounty.",
      urduTranslation = "اے اللہ! مجھے اپنے حلال کے ذریعے اپنے حرام سے بے نیاز کر دے، اور اپنے فضل کے ذریعے مجھے اپنے سوا ہر ایک سے بے نیاز کر دے۔",
      authenticReference = "Jami' at-Tirmidhi 3563 (Narrated by Imam Ali ibn Abi Talib AS — Hasan) • Hisn al-Muslim #136",
      virtueAndTiming = "Imam Ali (AS) taught that even if a person had a debt as huge as Mount Sabir, Allah would pay it off for them through this supplication.",
      recommendedCount = 3,
      searchKeywordsRomanUrduAndAliases = "qarz ki dua karz utarne ki dua rizq mein barakat ki dua halal rizq قرض اتارنے کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/136hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_al_faraj_ahlulbayt",
      title = "Dua al-Faraj (Supplication in Times of Trial & Relief / دعاء الفرج)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.DISTRESS_ANXIETY,
      occasionBadge = "Ahlulbayt Heritage • Relief in Hardship",
      arabicText = "اللَّهُمَّ عَظُمَ الْبَلَاءُ، وَبَرِحَ الْخَفَاءُ، وَانْكَشَفَ الْغِطَاءُ، وَضَاقَتِ الْأَرْضُ بِمَا وَسِعَتِ السَّمَاءُ، وَإِلَيْكَ يَا رَبِّ الْمُشْتَكَى، وَعَلَيْكَ الْمُعَوَّلُ فِي الشِّدَّةِ وَالرَّخَاءِ، اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَآلِ مُحَمَّدٍ",
      transliteration = "Allahumma 'azumal-bala'u, wa barihal-khafa'u, wankashafal-ghita'u, wa daqatil-ardu bima wasi'atis-sama'u, wa ilayka ya Rabbil-mushtaka, wa 'alaykal-mu'awwalu fish-shiddati war-rakha'i, Allahumma salli 'ala Muhammadin wa ali Muhammad.",
      englishTranslation = "O Allah, the trial has become great, the hidden has been exposed, the veil has been lifted, and the earth has become constrained despite the vastness of the heaven. To You, O my Lord, is the complaint, and upon You is reliance in hardship and ease. O Allah, bless Muhammad and the family of Muhammad.",
      urduTranslation = "اے اللہ! آزمائش بہت بڑھ گئی، پوشیدہ بات ظاہر ہو گئی، پردہ ہٹ گیا، اور آسمان کی وسعت کے باوجود زمین تنگ ہو گئی۔ اے میرے رب! تیری ہی بارگاہ میں شکایت ہے اور سختی و آسانی میں تجھی پر بھروسہ ہے۔ اے اللہ! محمد و آلِ محمد پر رحمت نازل فرما۔",
      authenticReference = "Mafatih al-Jinan (Shaykh Abbas al-Qummi) • Bihar al-Anwar Vol. 83",
      virtueAndTiming = "Recited across the Shia tradition for relief from severe tribulation and steadfastness.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "dua e faraj dua faraj mushkil kusha ahlulbayt دعائے فرج",
      humanAudioUrl = null
    ),
    DailyDuaItem(
      id = "dua_kumayl_opening",
      title = "Dua Kumayl — Opening Supplication of Divine Mercy (دعاء كميل)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.JUMUAH_FRIDAY,
      occasionBadge = "Thursday Night (Shab-e-Jumu'ah) • Imam Ali (AS)",
      arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ بِرَحْمَتِكَ الَّتِي وَسِعَتْ كُلَّ شَيْءٍ، وَبِقُوَّتِكَ الَّتِي قَهَرْتَ بِهَا كُلَّ شَيْءٍ، وَخَضَعَ لَهَا كُلُّ شَيْءٍ، وَذَلَّ لَهَا كُلُّ شَيْءٍ ... اللَّهُمَّ اغْفِرْ لِيَ الذُّنُوبَ الَّتِي تَهْتِكُ الْعِصَمَ",
      transliteration = "Allahumma inni as'aluka birahmatikal-lati wasi'at kulla shay'in, wa biquwwatikal-lati qaharta biha kulla shay'in, wa khada'a laha kullu shay'in, wa dhalla laha kullu shay'in ... Allahummagh-fir liyadh-dhunubal-lati tahtikul-'isam.",
      englishTranslation = "O Allah, I ask You by Your mercy which embraces all things, and by Your power by which You have overcome all things and before which all things are humble and subdued... O Allah, forgive me those sins which tear apart safeguards.",
      urduTranslation = "اے اللہ! میں تجھ سے تیری اس رحمت کے واسطے سے سوال کرتا ہوں جو ہر چیز پر محیط ہے، اور تیری اس قوت کے واسطے سے جس سے تو نے ہر شے کو مسخر کیا ہے... اے اللہ! میرے وہ گناہ بخش دے جو عصمت و حفاظت کے پردوں کو چاک کر دیتے ہیں۔",
      authenticReference = "Misbah al-Mutahajjid (Shaykh al-Tusi) • Iqbal al-A'mal (Sayyid Ibn Tawus) • Taught by Imam Ali ibn Abi Talib (AS) to Kumayl ibn Ziyad",
      virtueAndTiming = "Recited on Thursday nights (Laylatul Jumu'ah) and the Night of Mid-Sha'ban for forgiveness and spiritual humility.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "dua e kumayl dua kumail imam ali shab e jumuah دعائے کمیل",
      humanAudioUrl = null
    ),
    DailyDuaItem(
      id = "dua_istikharah",
      title = "Dua al-Istikharah (Istikhara Ki Dua / استخارہ کی دعا)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.ISTIKHARAH,
      occasionBadge = "Salatul Istikharah • Important Decisions",
      arabicText = "اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ، وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ، وَأَسْأَلُكَ مِنْ فَضْلِكَ الْعَظِيمِ، فَإِنَّكَ تَقْدِرُ وَلَا أَقْدِرُ، وَتَعْلَمُ وَلَا أَعْلَمُ، وَأَنْتَ عَلَّامُ الْغُيُوبِ، اللَّهُمَّ إِنْ كُنْتَ تَعْلَمُ أَنَّ هَذَا الْأَمْرَ خَيْرٌ لِي فِي دِينِي وَمَعَاشِي وَعَاقِبَةِ أَمْرِي فَاقْدُرْهُ لِي وَيَسِّرْهُ لِي ثُمَّ بَارِكْ لِي فِيهِ، وَإِنْ كُنْتَ تَعْلَمُ أَنَّ هَذَا الْأَمْرَ شَرٌّ لِي فِي دِينِي وَمَعَاشِي وَعَاقِبَةِ أَمْرِي فَاصْرِفْهُ عَنِّي وَاصْرِفْنِي عَنْهُ وَاقْدُرْ لِيَ الْخَيْرَ حَيْثُ كَانَ ثُمَّ أَرْضِنِي بِهِ",
      transliteration = "Allahumma inni astakhiruka bi'ilmika, wa astaqdiruka biqudratika, wa as'aluka min fadlikal-'azim, fa innaka taqdiru wa la aqdiru, wa ta'lamu wa la a'lamu, wa Anta 'Allamul-ghuyub. Allahumma in kunta ta'lamu anna hadhal-amra khayrun li fi dini wa ma'ashi wa 'aqibati amri faqdurhu li wa yassirhu li thumma barik li fihi, wa in kunta ta'lamu anna hadhal-amra sharrun li fi dini wa ma'ashi wa 'aqibati amri fasrifhu 'anni wasrifni 'anhu waqdur liyal-khayra haythu kana thumma ardini bihi.",
      englishTranslation = "O Allah, I seek Your guidance by virtue of Your knowledge, and I seek ability by virtue of Your power, and I ask You of Your great bounty. You have power, and I have none. You know, and I know not. You are the Knower of hidden things. O Allah, if in Your knowledge this matter is good for my religion, my livelihood, and the outcome of my affairs, then ordain it for me, make it easy for me, and bless me in it. And if in Your knowledge this matter is bad for my religion, my livelihood, and the outcome of my affairs, then turn it away from me, turn me away from it, ordain for me the good wherever it may be, and make me pleased with it.",
      urduTranslation = "اے اللہ! میں تیرے علم کے ذریعے تجھ سے خیر طلب کرتا ہوں اور تیری قدرت کے ذریعے تجھ سے طاقت مانگتا ہوں اور تیرے فضلِ عظیم کا سوال کرتا ہوں، کیونکہ تو قدرت رکھتا ہے اور میں قدرت نہیں رکھتا، تو جانتا ہے اور میں نہیں جانتا، اور تو غیبوں کا خوب جاننے والا ہے۔ اے اللہ! اگر تیرے علم میں یہ کام میرے دین، میری معاش اور میرے انجامِ کار کے اعتبار سے بہتر ہے تو اسے میرے مقدر میں کر دے، میرے لیے آسان بنا دے اور اس میں برکت دے۔ اور اگر تیرے علم میں یہ کام میرے دین، میری معاش اور میرے انجامِ کار کے اعتبار سے برا ہے تو اسے مجھ سے پھیر دے اور مجھے اس سے پھیر دے، اور میرے لیے خیر مقدر فرما جہاں کہیں بھی ہو، پھر مجھے اس پر راضی کر دے۔",
      authenticReference = "Sahih al-Bukhari 1166 (Narrated by Jabir ibn Abdullah, may Allah be pleased with them) • Hisn al-Muslim #74",
      virtueAndTiming = "Pray two Rak'ahs of voluntary (Nafl) prayer and then recite this supplication when making any major permissible life decision.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "istikhara ki dua istikharah ki dua استخارہ کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/74hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_nikah_newlyweds",
      title = "Dua for Newlyweds & Marriage Blessing (Nikah Aur Shadi Ki Dua / نکاح کی دعا)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.NIKAH_MARRIAGE,
      occasionBadge = "Marriage (Nikah) & Newlyweds",
      arabicText = "بَارَكَ اللَّهُ لَكَ، وَبَارَكَ عَلَيْكَ، وَجَمَعَ بَيْنَكُمَا فِي خَيْرٍ",
      transliteration = "Barakallahu laka, wa baraka 'alayka, wa jama'a baynakuma fi khayr.",
      englishTranslation = "May Allah bless you, and shower His blessings upon you, and join you together in goodness.",
      urduTranslation = "اللہ تعالیٰ تمہیں برکت دے، اور تم پر برکت نازل فرمائے، اور تم دونوں کو خیر اور بھلائی میں یکجا رکھے۔",
      authenticReference = "Sunan Abi Dawud 2130 • Jami' at-Tirmidhi 1091 (Sahih) • Hisn al-Muslim #190",
      virtueAndTiming = "The Prophetic congratulatory supplication taught for blessing a newly married couple.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "nikah ki dua shadi ki dua dulha dulhan ki dua شادی اور نکاح کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/190hm.mp3"
    ),
    DailyDuaItem(
      id = "dua_kaffaratul_majlis",
      title = "Kaffaratul Majlis (Majlis Ke Ikhtitam Ki Dua / کفارۂ مجلس کی دعا)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionType = SpecialOccasionType.GATHERING_MAJLIS,
      occasionBadge = "Closing a Gathering / Study Circle",
      arabicText = "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا أَنْتَ، أَسْتَغْفِرُكَ وَأَتُوبُ إِلَيْكَ",
      transliteration = "Subhanaka Allahumma wa bihamdika, ash-hadu an la ilaha illa Anta, astaghfiruka wa atubu ilayk.",
      englishTranslation = "Glory is to You, O Allah, and praise; I bear witness that there is no deity worthy of worship except You; I seek Your forgiveness and turn to You in repentance.",
      urduTranslation = "اے اللہ! تو اپنی حمد کے ساتھ پاک ہے، میں گواہی دیتا ہوں کہ تیرے سوا کوئی معبود نہیں، میں تجھ سے بخشش مانگتا ہوں اور تیری طرف توبہ کرتا ہوں۔",
      authenticReference = "Jami' at-Tirmidhi 3433 • Sunan Abi Dawud 4859 (Sahih) • Hisn al-Muslim #196",
      virtueAndTiming = "Recite at the conclusion of any sitting, meeting, or study circle to expiate any unintentional shortcomings in speech.",
      recommendedCount = 1,
      searchKeywordsRomanUrduAndAliases = "majlis ki dua kaffaratul majlis mehfil khatam hone ki dua مجلس کے اختتام کی دعا",
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/196hm.mp3"
    )
  )

  // =========================================================================
  // FRIDAY (JUMU'AH) BATH REMINDER & SUNNAH CHECKLIST
  // =========================================================================
  val fridaySunnahChecklist: List<FridaySunnahChecklistItem> = listOf(
    FridaySunnahChecklistItem(
      id = "friday_ghusl_bath",
      title = "Perform Friday Bath (Jumu'ah Ghusl) & Complete Purification",
      arabicTitle = "غُسْلُ يَوْمِ الْجُمُعَةِ",
      description = "The Prophet ﷺ said: 'Taking a bath (Ghusl) on Friday is essential for every adult Muslim.' Perform Ghusl before departing for Jumu'ah prayer.",
      authenticReference = "Sahih al-Bukhari 877 • Sahih Muslim 846"
    ),
    FridaySunnahChecklistItem(
      id = "friday_miswak_grooming",
      title = "Use Miswak (Clean Teeth) & Trim Nails",
      arabicTitle = "السِّوَاكُ وَتَقْلِيمُ الأَظْفَارِ",
      description = "Clean the mouth thoroughly with Miswak or brush and maintain personal cleanliness for the blessed Jumu'ah congregation.",
      authenticReference = "Sahih al-Bukhari 880 • Sahih Muslim 846"
    ),
    FridaySunnahChecklistItem(
      id = "friday_clean_attire_attar",
      title = "Wear Clean Best Garments & Apply Perfume (Attar)",
      arabicTitle = "لُبْسُ أَحْسَنِ الثِّيَابِ وَالطِّيبُ",
      description = "Wear clean, dignified garments (white is recommended) and apply pleasant non-alcoholic fragrance before heading to the Masjid.",
      authenticReference = "Sahih al-Bukhari 883 • Sunan Abi Dawud 343"
    ),
    FridaySunnahChecklistItem(
      id = "friday_surah_al_kahf",
      title = "Recite Surah Al-Kahf (Surah 18)",
      arabicTitle = "قِرَاءَةُ سُورَةِ الْكَهْفِ",
      description = "Whoever recites Surah Al-Kahf on Friday will have a light shining for him between this Friday and the next.",
      authenticReference = "Al-Hakim 3392 • Al-Bayhaqi (Sahih)",
      actionSurahNumber = 18
    ),
    FridaySunnahChecklistItem(
      id = "friday_abundant_salawat",
      title = "Send Abundant Salawat (Durood) Upon the Prophet ﷺ",
      arabicTitle = "الإِكْثَارُ مِنَ الصَّلَاةِ عَلَى النَّبِيِّ ﷺ",
      description = "Increase sending blessings upon Prophet Muhammad ﷺ throughout Thursday night and Friday.",
      authenticReference = "Sunan Abi Dawud 1047 • Sunan an-Nasa'i 1374",
      actionDuaId = "dua_jumuah_salawat_ibrahimiyyah"
    ),
    FridaySunnahChecklistItem(
      id = "friday_early_masjid_khutbah",
      title = "Arrive Early at the Masjid & Listen Attentively to the Khutbah",
      arabicTitle = "التَّبْكِيرُ إِلَى الْمَسْجِدِ وَالإِنْصَاتُ لِلْخُطْبَةِ",
      description = "Angels stand at the gates of the Masjid recording those who arrive early in order of arrival until the Imam ascends the Minbar.",
      authenticReference = "Sahih al-Bukhari 881 • Sahih Muslim 850"
    ),
    FridaySunnahChecklistItem(
      id = "friday_saa_at_al_istijabah",
      title = "Make Earnest Dua in the Last Hour Before Maghrib (Saa'at al-Istijabah)",
      arabicTitle = "سَاعَةُ الإِجَابَةِ يَوْمَ الْجُمُعَةِ",
      description = "Seek the special hour of acceptance on Friday (especially after Asr until Maghrib) when no Muslim asks Allah for good except that He grants it.",
      authenticReference = "Sahih al-Bukhari 935 • Sunan Abi Dawud 1048"
    )
  )

  // =========================================================================
  // NECESSARY SUNNAH & DAILY REMINDERS
  // =========================================================================
  val defaultNecessaryReminders: List<NecessaryReminderSpec> = listOf(
    NecessaryReminderSpec(
      id = "rem_friday_bath_ghusl",
      title = "Friday Bath (Jumu'ah Ghusl) & Preparation Reminder",
      arabicSnippet = "غُسْلُ يَوْمِ الجُمُعَةِ وَاجِبٌ عَلَى كُلِّ مُحْتَلِمٍ",
      subtitle = "Reminds you every Friday morning to perform Jumu'ah Ghusl (bath), use Miswak, wear clean clothes, and prepare for Jumu'ah prayer.",
      authenticReference = "Sahih al-Bukhari 877 • Sahih Muslim 846",
      frequency = ReminderFrequency.WEEKLY_FRIDAY,
      defaultTime24h = "09:30",
      timeOptions24h = listOf("08:00", "09:00", "09:30", "10:30", "11:15"),
      defaultEnabled = true,
      linkedDuaCategory = DuaCategory.SPECIAL_OCCASIONS
    ),
    NecessaryReminderSpec(
      id = "rem_friday_surah_kahf",
      title = "Friday Surah Al-Kahf Recitation Reminder",
      arabicSnippet = "مَنْ قَرَأَ سُورَةَ الْكَهْفِ فِي يَوْمِ الْجُمُعَةِ أَضَاءَ لَهُ مِنَ النُّورِ",
      subtitle = "Weekly Friday reminder to recite Surah Al-Kahf (Surah 18) for illumination and protection between the two Fridays.",
      authenticReference = "Al-Mustadrak al-Hakim 3392 • Sunan al-Bayhaqi",
      frequency = ReminderFrequency.WEEKLY_FRIDAY,
      defaultTime24h = "10:30",
      timeOptions24h = listOf("08:30", "10:30", "11:30", "14:00", "16:00"),
      defaultEnabled = true,
      linkedSurahNumber = 18
    ),
    NecessaryReminderSpec(
      id = "rem_friday_istijabah_dua",
      title = "Friday Hour of Acceptance (Asr to Maghrib Dua Reminder)",
      arabicSnippet = "فِيهِ سَاعَةٌ لَا يُوَافِقُهَا عَبْدٌ مُسْلِمٌ يَسْأَلُ اللَّهَ شَيْئًا إِلَّا أَعْطَاهُ إِيَّاهُ",
      subtitle = "Reminds you on Friday afternoon before Maghrib to devote time to earnest Dua during Saa'at al-Istijabah.",
      authenticReference = "Sahih al-Bukhari 935 • Sunan Abi Dawud 1048",
      frequency = ReminderFrequency.WEEKLY_FRIDAY,
      defaultTime24h = "16:30",
      timeOptions24h = listOf("15:45", "16:15", "16:30", "17:00", "17:30"),
      defaultEnabled = true,
      linkedDuaCategory = DuaCategory.QURANIC_RABBANA
    ),
    NecessaryReminderSpec(
      id = "rem_daily_morning_adhkar",
      title = "Daily Morning Adhkar (Subah ke Masnoon Azkar)",
      arabicSnippet = "وَسَبِّحْ بِحَمْدِ رَبِّكَ قَبْلَ طُلُوعِ الشَّمْسِ",
      subtitle = "Daily morning reminder after Fajr for Ayat al-Kursi, Sayyidul Istighfar, 3 Quls, and morning protection Duas.",
      authenticReference = "Surah Qaf 50:39 • Sahih Muslim 2723",
      frequency = ReminderFrequency.DAILY,
      defaultTime24h = "06:30",
      timeOptions24h = listOf("05:45", "06:15", "06:30", "07:00", "07:30"),
      defaultEnabled = true,
      linkedDuaCategory = DuaCategory.MORNING_EVENING
    ),
    NecessaryReminderSpec(
      id = "rem_daily_evening_adhkar",
      title = "Daily Evening Adhkar (Shaam ke Masnoon Azkar)",
      arabicSnippet = "وَقَبْلَ الْغُرُوبِ",
      subtitle = "Daily afternoon/evening reminder after Asr for evening protection supplications and Istighfar.",
      authenticReference = "Surah Qaf 50:39 • Sunan Abi Dawud 5088",
      frequency = ReminderFrequency.DAILY,
      defaultTime24h = "17:30",
      timeOptions24h = listOf("16:45", "17:15", "17:30", "18:00", "18:30"),
      defaultEnabled = true,
      linkedDuaCategory = DuaCategory.MORNING_EVENING
    ),
    NecessaryReminderSpec(
      id = "rem_nightly_sleep_sunnah",
      title = "Before-Sleep Sunnah Reminder (Wudu, Ayat al-Kursi & Surah Al-Mulk)",
      arabicSnippet = "تَبَٰرَكَ ٱلَّذِى بِيَدِهِ ٱلْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَىْءٍ قَدِيرٌ",
      subtitle = "Nightly reminder to sleep in a state of Wudu, recite Surah Al-Mulk (Surah 67), Ayat al-Kursi (2:255), and the last two verses of Surah Al-Baqarah.",
      authenticReference = "Sahih al-Bukhari 247 & 5017 • Jami' at-Tirmidhi 2891",
      frequency = ReminderFrequency.DAILY,
      defaultTime24h = "21:45",
      timeOptions24h = listOf("20:30", "21:00", "21:45", "22:15", "22:45"),
      defaultEnabled = true,
      linkedSurahNumber = 67
    ),
    NecessaryReminderSpec(
      id = "rem_mon_thu_fasting",
      title = "Monday & Thursday Sunnah Fasting Reminder",
      arabicSnippet = "تُعْرَضُ الأَعْمَالُ يَوْمَ الاِثْنَيْنِ وَالخَمِيسِ",
      subtitle = "Evening reminder on Sundays and Wednesdays to prepare for voluntary Sunnah fasting on Monday and Thursday.",
      authenticReference = "Jami' at-Tirmidhi 747 • Sahih Muslim 1162",
      frequency = ReminderFrequency.SUNNAH_FASTING_DAYS,
      defaultTime24h = "20:30",
      timeOptions24h = listOf("19:30", "20:00", "20:30", "21:00"),
      defaultEnabled = true,
      linkedDuaCategory = DuaCategory.SPECIAL_OCCASIONS
    ),
    NecessaryReminderSpec(
      id = "rem_ayyam_al_beed",
      title = "Ayyam al-Beed (13th, 14th & 15th Hijri White Days) Reminder",
      arabicSnippet = "صِيَامُ ثَلَاثَةِ أَيَّامٍ مِنْ كُلِّ شَهْرٍ",
      subtitle = "Monthly reminder for the three blessed lunar days of every Islamic month recommended by the Prophet ﷺ.",
      authenticReference = "Sahih al-Bukhari 1978 • Sahih Muslim 721",
      frequency = ReminderFrequency.MONTHLY_HIJRI,
      defaultTime24h = "20:00",
      timeOptions24h = listOf("19:00", "20:00", "21:00"),
      defaultEnabled = true,
      linkedDuaCategory = DuaCategory.SPECIAL_OCCASIONS
    ),
    NecessaryReminderSpec(
      id = "rem_calculated_daily_day_sunnah",
      title = "Calculated Today & Weekday Sunnah Notification (Friday & All 7 Days)",
      arabicSnippet = "خَيْرُ يَوْمٍ طَلَعَتْ عَلَيْهِ الشَّمْسُ يَوْمُ الْجُمُعَةِ",
      subtitle = "Automatically calculates whether today is Friday (Jumu'ah), Monday/Thursday Fasting Day, Ayyam al-Beed, or another weekday and sends the day's tailored Sunnah notification.",
      authenticReference = "Sahih Muslim 854 • Sahih al-Bukhari 877 • Jami' at-Tirmidhi 747",
      frequency = ReminderFrequency.DAILY,
      defaultTime24h = "08:00",
      timeOptions24h = listOf("07:00", "08:00", "08:30", "09:00", "10:00"),
      defaultEnabled = true,
      linkedDuaCategory = DuaCategory.MORNING_EVENING
    )
  )

  // =========================================================================
  // CALCULATED 7-DAY WEEKDAY SUNNAH & REMINDER SPECIFICATIONS (FRI..THU)
  // =========================================================================
  val weekdaySunnahSpecs: List<WeekdaySunnahSpec> = listOf(
    WeekdaySunnahSpec(
      calendarDayOfWeek = java.util.Calendar.FRIDAY,
      dayNameEnglish = "Friday",
      shortDayLabel = "Fri",
      islamicDayTitle = "Yawm al-Jumu'ah (Master of Days)",
      arabicDayName = "يَوْمُ الْجُمُعَةِ",
      primaryReminderTitle = "Blessed Friday (Jumu'ah): Perform Ghusl (Bath), Recite Surah Al-Kahf & Abundant Salawat",
      arabicHadithSnippet = "«خَيْرُ يَوْمٍ طَلَعَتْ عَلَيْهِ الشَّمْسُ يَوْمُ الْجُمُعَةِ» • «غُسْلُ يَوْمِ الجُمُعَةِ وَاجِبٌ عَلَى كُلِّ مُحْتَلِمٍ»",
      englishGuidance = "Today is Blessed Friday (Yawm al-Jumu'ah). Perform the Friday Bath (Ghusl), use Miswak, wear clean garments & Attar, arrive early for Khutbah, recite Surah Al-Kahf (Surah 18), send abundant Salawat upon the Prophet ﷺ, and make earnest Dua in Saa'at al-Istijabah before Maghrib.",
      urduGuidance = "آج جمعۃ المبارک ہے: غسلِ جمعہ کریں، مسواک اور خوشبو استعمال کریں، سورۃ الکہف کی تلاوت کریں، نبی کریم ﷺ پر کثرت سے درود شریف بھیجیں، اور عصر تا مغرب قبولیت کی گھڑی میں دعا کا اہتمام کریں۔",
      authenticReference = "Sahih al-Bukhari 877 & 935 • Sahih Muslim 846 & 854 • Sunan Abi Dawud 1047",
      keySunnahBullets = listOf(
        "Perform Friday Bath (Jumu'ah Ghusl) & use Miswak (Bukhari 877)",
        "Recite Surah Al-Kahf (Surah 18) for light between the two Fridays (Al-Hakim 3392)",
        "Send abundant Salawat (Durood) upon Prophet Muhammad ﷺ (Abu Dawud 1047)",
        "Make earnest Dua in the last hour after Asr before Maghrib (Bukhari 935)"
      ),
      isFridayJumuah = true,
      isSunnahFastingDay = false,
      linkedSurahNumber = 18,
      linkedReminderId = "rem_friday_bath_ghusl"
    ),
    WeekdaySunnahSpec(
      calendarDayOfWeek = java.util.Calendar.SATURDAY,
      dayNameEnglish = "Saturday",
      shortDayLabel = "Sat",
      islamicDayTitle = "Yawm as-Sabt (Salatud-Duha & Masjid Visitation)",
      arabicDayName = "يَوْمُ السَّبْتِ",
      primaryReminderTitle = "Saturday Sunnah: Pray Salatud-Duha, Morning/Evening Adhkar & Uphold Family Ties",
      arabicHadithSnippet = "«كَانَ النَّبِيُّ ﷺ يَأْتِي مَسْجِدَ قُبَاءٍ كُلَّ سَبْتٍ رَاكِبًا وَمَاشِيًا فَيُصَلِّي فِيهِ رَكْعَتَيْنِ»",
      englishGuidance = "On Saturdays, the Prophet ﷺ regularly went to Masjid Quba and prayed two Rak'ahs. Observe Salatud-Duha (the Forenoon Prayer of the Oft-Repentant), complete Morning & Evening Masnoon Adhkar, and strengthen kinship ties (Silat ar-Rahim).",
      urduGuidance = "ہفتے کے دن سنتِ نبوی ﷺ کے مطابق چاشت (صلاۃ الضحیٰ) کی دو رکعتیں ادا کریں، صبح و شام کے مسنون اذکار پڑھیں اور صلہ رحمی (رشتہ داروں سے حسنِ سلوک) کا اہتمام کریں۔",
      authenticReference = "Sahih al-Bukhari 1193 • Sahih Muslim 1399 & 748",
      keySunnahBullets = listOf(
        "Pray 2 or 4 Rak'ahs of Salatud-Duha (Charity for every joint — Muslim 720)",
        "Recite Morning & Evening Masnoon Adhkar (Ayat al-Kursi & Sayyidul Istighfar)",
        "Check on parents, family, and neighbors (Silat ar-Rahim — Bukhari 5985)"
      ),
      isFridayJumuah = false,
      isSunnahFastingDay = false,
      linkedSurahNumber = 67
    ),
    WeekdaySunnahSpec(
      calendarDayOfWeek = java.util.Calendar.SUNDAY,
      dayNameEnglish = "Sunday",
      shortDayLabel = "Sun",
      islamicDayTitle = "Yawm al-Ahad (Tawhid Reflection & Prepare for Monday Fast)",
      arabicDayName = "يَوْمُ الأَحَدِ",
      primaryReminderTitle = "Sunday Reminder: Daily Quranic Wird, Evening Adhkar & Prepare for Tomorrow's Monday Sunnah Fast",
      arabicHadithSnippet = "«تُعْرَضُ الأَعْمَالُ يَوْمَ الاِثْنَيْنِ وَالخَمِيسِ، فَأُحِبُّ أَنْ يُعْرَضَ عَمَلِي وَأَنَا صَائِمٌ»",
      englishGuidance = "Complete your daily Quranic recitation, Morning & Evening Adhkar, and before-sleep Surah Al-Mulk (Surah 67). Tonight (Sunday evening), form the intention for tomorrow's voluntary Monday Sunnah fast if able.",
      urduGuidance = "آج قرآنِ کریم کا روزانہ ورد اور صبح و شام کے مسنون اذکار مکمل کریں، رات کو سورۃ الملک پڑھیں، اور کل پیر (دوشنبہ) کے نفلی روزے کی نیت اور سحری کی تیاری کریں۔",
      authenticReference = "Jami' at-Tirmidhi 747 • Sahih al-Bukhari 5017 • Jami' at-Tirmidhi 2891",
      keySunnahBullets = listOf(
        "Prepare tonight for Monday's Sunnah Fast (Tirmidhi 747)",
        "Recite Quranic Rabbana Duas & Sayyidul Istighfar (Bukhari 6306)",
        "Recite Surah Al-Mulk (Surah 67) & Ayat al-Kursi before sleeping"
      ),
      isFridayJumuah = false,
      isSunnahFastingDay = false,
      linkedSurahNumber = 67,
      linkedReminderId = "rem_mon_thu_fasting"
    ),
    WeekdaySunnahSpec(
      calendarDayOfWeek = java.util.Calendar.MONDAY,
      dayNameEnglish = "Monday",
      shortDayLabel = "Mon",
      islamicDayTitle = "Yawm al-Ithnayn (Sunnah Fast & Presentation of Deeds)",
      arabicDayName = "يَوْمُ الإِثْنَيْنِ",
      primaryReminderTitle = "Monday Sunnah: Voluntary Fasting, Presentation of Deeds & Prophetic Remembrance",
      arabicHadithSnippet = "«ذَاكَ يَوْمٌ وُلِدْتُ فِيهِ، وَيَوْمٌ بُعِثْتُ أَوْ أُنْزِلَ عَلَيَّ فِيهِ»",
      englishGuidance = "Monday is a blessed day on which the Prophet ﷺ was born, received revelation, and on which deeds are presented to Allah. Observe the Monday Sunnah fast if able, guard the tongue, and increase Istighfar and Salawat.",
      urduGuidance = "پیر (دوشنبہ) مبارک دن ہے جس میں نبی کریم ﷺ کی ولادت ہوئی، وحی کا آغاز ہوا اور اللہ کے حضور اعمال پیش ہوتے ہیں۔ سنت روزہ، استغفار اور درود شریف کا اہتمام کریں۔",
      authenticReference = "Sahih Muslim 1162 • Jami' at-Tirmidhi 747",
      keySunnahBullets = listOf(
        "Observe Monday voluntary Sunnah fast & break fast with Dua at Maghrib (Muslim 1162)",
        "Increase Istighfar & righteous deeds as actions are presented to Allah today (Tirmidhi 747)",
        "Maintain Morning & Evening Masnoon Adhkar after Fajr and Asr"
      ),
      isFridayJumuah = false,
      isSunnahFastingDay = true,
      linkedSurahNumber = 2,
      linkedReminderId = "rem_mon_thu_fasting"
    ),
    WeekdaySunnahSpec(
      calendarDayOfWeek = java.util.Calendar.TUESDAY,
      dayNameEnglish = "Tuesday",
      shortDayLabel = "Tue",
      islamicDayTitle = "Yawm ath-Thulatha' (Steadfast Salah & Protection Adhkar)",
      arabicDayName = "يَوْمُ الثُّلَاثَاءِ",
      primaryReminderTitle = "Tuesday Sunnah: 12 Sunnah Rawatib Rak'ahs, Protection Duas & Nightly Surah Al-Mulk",
      arabicHadithSnippet = "«مَنْ صَلَّى اثْنَتَيْ عَشْرَةَ رَكْعَةً فِي يَوْمٍ وَلَيْلَةٍ بُنِيَ لَهُ بِهِنَّ بَيْتٌ فِي الْجَنَّةِ»",
      englishGuidance = "Guard the five daily obligatory prayers and the 12 daily Sunnah Rawatib Rak'ahs (for which a house is built in Paradise). Recite the 3x Morning/Evening protection Dua ('Bismillahilladhi la yadurru...') and Surah Al-Mulk at night.",
      urduGuidance = "فرض نمازوں کے ساتھ بارہ رکعت سنتِ مؤکدہ (رواتب) کی پابندی کریں جس پر جنت میں گھر کی بشارت ہے، اور صبح و شام حفاظت کی مسنون دعائیں پڑھیں۔",
      authenticReference = "Sahih Muslim 728 • Sunan Abi Dawud 5088 • Jami' at-Tirmidhi 2891",
      keySunnahBullets = listOf(
        "Pray the 12 Sunnah Rawatib Rak'ahs (2 before Fajr, 4+2 Dhuhr, 2 Maghrib, 2 Isha — Muslim 728)",
        "Recite 'Bismillahilladhi la yadurru...' 3x morning & evening (Abu Dawud 5088)",
        "Sleep in Wudu after reciting Ayat al-Kursi and Surah Al-Mulk (Surah 67)"
      ),
      isFridayJumuah = false,
      isSunnahFastingDay = false,
      linkedSurahNumber = 67
    ),
    WeekdaySunnahSpec(
      calendarDayOfWeek = java.util.Calendar.WEDNESDAY,
      dayNameEnglish = "Wednesday",
      shortDayLabel = "Wed",
      islamicDayTitle = "Yawm al-Arbi'a' (Dua Between Dhuhr & Asr + Prepare for Thursday Fast)",
      arabicDayName = "يَوْمُ الأَرْبِعَاءِ",
      primaryReminderTitle = "Wednesday Sunnah: Earnest Dua Between Dhuhr & Asr + Prepare for Thursday Sunnah Fast",
      arabicHadithSnippet = "«فَاسْتُجِيبَ لَهُ يَوْمَ الأَرْبِعَاءِ بَيْنَ الصَّلَاتَيْنِ الظُّهْرِ وَالعَصْرِ»",
      englishGuidance = "Jabir ibn Abdullah (RA) reported that the Prophet ﷺ supplicated between Dhuhr and Asr on Wednesday and his supplication was answered. Devote time to Dua in the afternoon, and prepare tonight for tomorrow's Thursday Sunnah fast.",
      urduGuidance = "بدھ کے دن ظہر اور عصر کے درمیان دعا قبول ہونے کی سنتِ نبوی ﷺ ثابت ہے، اس وقت دعا کا اہتمام کریں اور کل جمعرات کے مسنون روزے کی تیاری کریں۔",
      authenticReference = "Musnad Ahmad 14563 • Al-Bukhari (Al-Adab al-Mufrad 704) • Jami' at-Tirmidhi 747",
      keySunnahBullets = listOf(
        "Make earnest Dua between Dhuhr and Asr prayer today (Adab al-Mufrad 704)",
        "Prepare tonight (Wednesday evening) for Thursday's Sunnah Fast (Tirmidhi 747)",
        "Complete Evening Masnoon Adhkar & Istighfar (100x daily — Muslim 2702)"
      ),
      isFridayJumuah = false,
      isSunnahFastingDay = false,
      linkedSurahNumber = 18,
      linkedReminderId = "rem_mon_thu_fasting"
    ),
    WeekdaySunnahSpec(
      calendarDayOfWeek = java.util.Calendar.THURSDAY,
      dayNameEnglish = "Thursday",
      shortDayLabel = "Thu",
      islamicDayTitle = "Yawm al-Khamis (Sunnah Fast, Deeds Presented & Jumu'ah Eve at Maghrib)",
      arabicDayName = "يَوْمُ الْخَمِيسِ",
      primaryReminderTitle = "Thursday Sunnah: Fast Today & Welcome Blessed Laylatul Jumu'ah (Friday Eve) at Maghrib",
      arabicHadithSnippet = "«أَكْثِرُوا الصَّلَاةَ عَلَيَّ يَوْمَ الْجُمُعَةِ وَلَيْلَةَ الْجُمُعَةِ»",
      englishGuidance = "Deeds are presented to Allah on Thursday, making it a beloved Sunnah fasting day. At Maghrib tonight, the blessed Night of Friday (Laylatul Jumu'ah) begins — start sending abundant Salawat upon the Prophet ﷺ and prepare for tomorrow's Friday Bath (Ghusl) and Surah Al-Kahf.",
      urduGuidance = "جمعرات کے دن اللہ کے حضور اعمال پیش ہوتے ہیں اور روزہ رکھنا سنت ہے۔ آج مغرب سے شبِ جمعہ کا آغاز ہوتا ہے: کثرت سے درود شریف پڑھیں اور کل کے غسلِ جمعہ و سورۃ الکہف کی تیاری کریں۔",
      authenticReference = "Jami' at-Tirmidhi 747 • Sunan Abi Dawud 1047 • Al-Bayhaqi 5994",
      keySunnahBullets = listOf(
        "Observe Thursday voluntary Sunnah fast & break fast with Masnoon Dua (Tirmidhi 747)",
        "Begin abundant Salawat on the Prophet ﷺ from Maghrib tonight (Laylatul Jumu'ah)",
        "Confirm tomorrow's Friday Bath (Jumu'ah Ghusl) morning reminder time"
      ),
      isFridayJumuah = false,
      isSunnahFastingDay = true,
      linkedSurahNumber = 18,
      linkedReminderId = "rem_friday_bath_ghusl"
    )
  )

  fun getWeekdaySpec(calendarDayOfWeek: Int): WeekdaySunnahSpec {
    return weekdaySunnahSpecs.find { it.calendarDayOfWeek == calendarDayOfWeek }
      ?: weekdaySunnahSpecs.first()
  }

  private val duaStopWords = setOf(
    "ki", "ka", "ke", "se", "mein", "me", "par", "ko", "aur", "waqt", "dua", "duaa",
    "for", "of", "the", "when", "upon", "before", "after", "to", "in", "a", "an",
    "دعا", "کی", "کا", "کے", "سے", "میں", "پر", "وقت"
  )

  fun normalizePhoneticToken(token: String): String {
    val lower = token.lowercase().trim()
      .replace("['’`\\-_.,()\\[\\]•:;/]".toRegex(), "")
    if (lower.isEmpty()) return ""
    // Preserve non-ASCII (Urdu/Arabic) tokens directly
    if (lower.any { it.code > 127 }) return lower
    return lower
      .replace("aa", "a")
      .replace("ee", "i")
      .replace("oo", "u")
      .replace("ay", "e")
      .replace("ai", "e")
      .replace("ph", "f")
      .replace("kh", "k")
      .replace("gh", "g")
      .replace("sh", "s")
      .replace("ch", "c")
      .replace("zh", "z")
      .replace("q", "k")
      .replace("z", "s")
      .replace("w", "v")
  }

  private fun levenshteinDistance(a: String, b: String): Int {
    if (a == b) return 0
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length
    val dp = IntArray(b.length + 1) { it }
    for (i in 1..a.length) {
      var prev = dp[0]
      dp[0] = i
      for (j in 1..b.length) {
        val temp = dp[j]
        val cost = if (a[i - 1] == b[j - 1]) 0 else 1
        dp[j] = minOf(dp[j] + 1, dp[j - 1] + 1, prev + cost)
        prev = temp
      }
    }
    return dp[b.length]
  }

  /**
   * Multilingual & Roman Urdu Fuzzy Search for Duas.
   * Supports natural queries like "Safar ki dua", "Sone se pehle ki dua",
   * "Pani peene ki dua", "Khana khane ki dua", "Wazu ki dua", "Bimari ki dua", etc.
   */
  fun searchDuasFuzzy(query: String, candidates: List<DailyDuaItem> = allDuas): List<DailyDuaItem> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return candidates

    val rawTokens = trimmed.lowercase()
      .split(Regex("[\\s,.;:!?/\\-]+"))
      .map { it.trim() }
      .filter { it.isNotEmpty() }

    val meaningfulTokens = rawTokens.filter { it !in duaStopWords }
    val activeTokens = if (meaningfulTokens.isNotEmpty()) meaningfulTokens else rawTokens

    val scored = candidates.mapNotNull { dua ->
      val searchableFields = listOf(
        dua.title,
        dua.searchKeywordsRomanUrduAndAliases,
        dua.occasionBadge,
        dua.urduTranslation,
        dua.englishTranslation,
        dua.transliteration,
        dua.arabicText,
        dua.authenticReference
      )
      val fullTextLower = searchableFields.joinToString(" ").lowercase()
      var score = 0

      // 1. Direct phrase match in title or Roman Urdu aliases
      if (dua.title.lowercase().contains(trimmed.lowercase())) {
        score += 120
      }
      if (dua.searchKeywordsRomanUrduAndAliases.lowercase().contains(trimmed.lowercase())) {
        score += 140
      }
      if (fullTextLower.contains(trimmed.lowercase())) {
        score += 80
      }

      // 2. Token-by-token exact, phonetic, and fuzzy matching
      val candidateWords = (dua.title + " " + dua.searchKeywordsRomanUrduAndAliases + " " + dua.occasionBadge + " " + dua.englishTranslation + " " + dua.urduTranslation)
        .lowercase()
        .split(Regex("[\\s,.;:!?()/\\-•]+"))
        .filter { it.isNotEmpty() }
      val candidatePhonetic = candidateWords.map { it to normalizePhoneticToken(it) }

      var matchedTokenCount = 0
      for (qToken in activeTokens) {
        val qPhonetic = normalizePhoneticToken(qToken)
        var tokenBestScore = 0
        for ((candWord, candPhon) in candidatePhonetic) {
          if (candWord == qToken) {
            tokenBestScore = maxOf(tokenBestScore, 45)
          } else if (candWord.contains(qToken) || qToken.contains(candWord) && candWord.length >= 3) {
            tokenBestScore = maxOf(tokenBestScore, 35)
          } else if (qPhonetic.length >= 3 && candPhon == qPhonetic) {
            tokenBestScore = maxOf(tokenBestScore, 40)
          } else if (qPhonetic.length >= 3 && (candPhon.contains(qPhonetic) || qPhonetic.contains(candPhon)) && candPhon.length >= 3) {
            tokenBestScore = maxOf(tokenBestScore, 30)
          } else if (qPhonetic.length >= 4 && candPhon.length >= 4) {
            val dist = levenshteinDistance(qPhonetic, candPhon)
            val maxAllowed = if (qPhonetic.length >= 6) 2 else 1
            if (dist <= maxAllowed) {
              tokenBestScore = maxOf(tokenBestScore, 25 - dist * 5)
            }
          }
        }
        if (tokenBestScore > 0) {
          matchedTokenCount++
          score += tokenBestScore
        }
      }

      if (activeTokens.size > 1 && matchedTokenCount == activeTokens.size) {
        score += 60
      }

      if (score >= 25) dua to score else null
    }

    return scored.sortedByDescending { it.second }.map { it.first }
  }

  /**
   * Dedicated list of Special Duas for the Month of Ramadan displayed prominently on the Home Screen
   * and inside the Yearly & Ramadan Sehri/Iftar Timetable Screen.
   */
  val ramadanSpecialDuas: List<DailyDuaItem> by lazy {
    val targetIds = listOf(
      "dua_ramadan_iftar",
      "dua_ramadan_first_ashra_mercy",
      "dua_ramadan_third_ashra_nijat",
      "dua_laylatul_qadr",
      "dua_new_moon_hilal",
      "dua_ramadan_sehri_niyyah",
      "dua_ramadan_second_ashra_forgiveness"
    )
    targetIds.mapNotNull { id -> allDuas.find { it.id == id } }
      .sortedBy { dua ->
        val hasAudio = dua.quranSurahAyah != null || !dua.humanAudioUrl.isNullOrBlank()
        if (hasAudio) 0 else 1
      }
  }
}
