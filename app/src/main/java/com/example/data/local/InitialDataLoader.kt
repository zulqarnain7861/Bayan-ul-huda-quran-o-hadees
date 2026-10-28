package com.example.data.local

import com.example.data.local.entities.AyahEntity
import com.example.data.local.entities.HadithEntity
import com.example.data.local.entities.SurahEntity

object InitialDataLoader {

  fun getAll114Surahs(): List<SurahEntity> {
    return listOf(
      SurahEntity(1, "الفَاتِحة", "Al-Fatihah", "The Opening", "سورۃ الفاتحہ", "Meccan", 7, 5, 1),
      SurahEntity(2, "البَقَرَة", "Al-Baqarah", "The Cow", "سورۃ البقرہ", "Medinan", 286, 87, 1),
      SurahEntity(3, "آل عِمرَان", "Ali 'Imran", "Family of Imran", "سورۃ آل عمران", "Medinan", 200, 89, 3),
      SurahEntity(4, "النِّسَاء", "An-Nisa'", "The Women", "سورۃ النساء", "Medinan", 176, 92, 4),
      SurahEntity(5, "المَائدة", "Al-Ma'idah", "The Table Spread", "سورۃ المائدہ", "Medinan", 120, 112, 6),
      SurahEntity(6, "الأنعَام", "Al-An'am", "The Cattle", "سورۃ الانعام", "Meccan", 165, 55, 7),
      SurahEntity(7, "الأعرَاف", "Al-A'raf", "The Heights", "سورۃ الاعراف", "Meccan", 206, 39, 8),
      SurahEntity(8, "الأنفَال", "Al-Anfal", "The Spoils of War", "سورۃ الانفال", "Medinan", 75, 88, 9),
      SurahEntity(9, "التَّوبَة", "At-Tawbah", "The Repentance", "سورۃ التوبہ", "Medinan", 129, 113, 10),
      SurahEntity(10, "يُونُس", "Yunus", "Jonah", "سورۃ یونس", "Meccan", 109, 51, 11),
      SurahEntity(11, "هُود", "Hud", "Hud", "سورۃ ہود", "Meccan", 123, 52, 11),
      SurahEntity(12, "يُوسُف", "Yusuf", "Joseph", "سورۃ یوسف", "Meccan", 111, 53, 12),
      SurahEntity(13, "الرَّعْد", "Ar-Ra'd", "The Thunder", "سورۃ الرعد", "Medinan", 43, 96, 13),
      SurahEntity(14, "إبرَاهِيم", "Ibrahim", "Abraham", "سورۃ ابراہیم", "Meccan", 52, 72, 13),
      SurahEntity(15, "الحِجْر", "Al-Hijr", "The Rocky Tract", "سورۃ الحجر", "Meccan", 99, 54, 14),
      SurahEntity(16, "النَّحْل", "An-Nahl", "The Bee", "سورۃ النحل", "Meccan", 128, 70, 14),
      SurahEntity(17, "الإسرَاء", "Al-Isra'", "The Night Journey", "سورۃ بنی اسرائیل", "Meccan", 111, 50, 15),
      SurahEntity(18, "الكَهْف", "Al-Kahf", "The Cave", "سورۃ الکہف", "Meccan", 110, 69, 15),
      SurahEntity(19, "مَريَم", "Maryam", "Mary", "سورۃ مریم", "Meccan", 98, 44, 16),
      SurahEntity(20, "طه", "Ta-Ha", "Ta-Ha", "سورۃ طہ", "Meccan", 135, 45, 16),
      SurahEntity(21, "الأنبِيَاء", "Al-Anbiya'", "The Prophets", "سورۃ الانبیاء", "Meccan", 112, 73, 17),
      SurahEntity(22, "الحَجّ", "Al-Hajj", "The Pilgrimage", "سورۃ الحج", "Medinan", 78, 103, 17),
      SurahEntity(23, "المُؤمِنُون", "Al-Mu'minun", "The Believers", "سورۃ المؤمنون", "Meccan", 118, 74, 18),
      SurahEntity(24, "النُّور", "An-Nur", "The Light", "سورۃ النور", "Medinan", 64, 102, 18),
      SurahEntity(25, "الفُرقَان", "Al-Furqan", "The Criterion", "سورۃ الفرقان", "Meccan", 77, 42, 18),
      SurahEntity(26, "الشُّعَرَاء", "Ash-Shu'ara'", "The Poets", "سورۃ الشعراء", "Meccan", 227, 47, 19),
      SurahEntity(27, "النَّمْل", "An-Naml", "The Ant", "سورۃ النمل", "Meccan", 93, 48, 19),
      SurahEntity(28, "القَصَص", "Al-Qasas", "The Stories", "سورۃ القصص", "Meccan", 88, 49, 20),
      SurahEntity(29, "العَنكَبُوت", "Al-'Ankabut", "The Spider", "سورۃ العنکبوت", "Meccan", 69, 85, 20),
      SurahEntity(30, "الرُّوم", "Ar-Rum", "The Romans", "سورۃ الروم", "Meccan", 60, 84, 21),
      SurahEntity(31, "لُقمَان", "Luqman", "Luqman", "سورۃ لقمان", "Meccan", 34, 57, 21),
      SurahEntity(32, "السَّجدَة", "As-Sajdah", "The Prostration", "سورۃ السجدہ", "Meccan", 30, 75, 21),
      SurahEntity(33, "الأحزَاب", "Al-Ahzab", "The Combined Forces", "سورۃ الاحزاب", "Medinan", 73, 90, 21),
      SurahEntity(34, "سَبَأ", "Saba'", "Sheba", "سورۃ سبا", "Meccan", 54, 58, 22),
      SurahEntity(35, "فَاطِر", "Fatir", "Originator", "سورۃ فاطر", "Meccan", 45, 43, 22),
      SurahEntity(36, "يس", "Ya-Sin", "Ya-Sin", "سورۃ یٰسین", "Meccan", 83, 41, 22),
      SurahEntity(37, "الصَّافَّات", "As-Saffat", "Those Who Set The Ranks", "سورۃ الصافات", "Meccan", 182, 56, 23),
      SurahEntity(38, "ص", "Sad", "The Letter Sad", "سورۃ ص", "Meccan", 88, 38, 23),
      SurahEntity(39, "الزُّمَر", "Az-Zumar", "The Troops", "سورۃ الزمر", "Meccan", 75, 59, 23),
      SurahEntity(40, "غَافِر", "Ghafir", "The Forgiver", "سورۃ غافر", "Meccan", 85, 60, 24),
      SurahEntity(41, "فُصِّلَت", "Fussilat", "Explained in Detail", "سورۃ فصلت", "Meccan", 54, 61, 24),
      SurahEntity(42, "الشُّورَى", "Ash-Shura", "The Consultation", "سورۃ الشوریٰ", "Meccan", 53, 62, 25),
      SurahEntity(43, "الزُّخْرُف", "Az-Zukhruf", "The Ornaments of Gold", "سورۃ الزخرف", "Meccan", 89, 63, 25),
      SurahEntity(44, "الدُّخَان", "Ad-Dukhan", "The Smoke", "سورۃ الدخان", "Meccan", 59, 64, 25),
      SurahEntity(45, "الجَاثِيَة", "Al-Jathiyah", "The Crouching", "سورۃ الجاثیہ", "Meccan", 37, 65, 25),
      SurahEntity(46, "الأحْقَاف", "Al-Ahqaf", "The Wind-Curved Sandhills", "سورۃ الاحقاف", "Meccan", 35, 66, 26),
      SurahEntity(47, "مُحَمَّد", "Muhammad", "Muhammad", "سورۃ محمد", "Medinan", 38, 95, 26),
      SurahEntity(48, "الفَتْح", "Al-Fath", "The Victory", "سورۃ الفتح", "Medinan", 29, 111, 26),
      SurahEntity(49, "الحُجُرَات", "Al-Hujurat", "The Rooms", "سورۃ الحجرات", "Medinan", 18, 106, 26),
      SurahEntity(50, "ق", "Qaf", "The Letter Qaf", "سورۃ ق", "Meccan", 45, 34, 26),
      SurahEntity(51, "الذَّارِيَات", "Adh-Dhariyat", "The Winnowing Winds", "سورۃ الذاریات", "Meccan", 60, 67, 26),
      SurahEntity(52, "الطُّور", "At-Tur", "The Mount", "سورۃ الطور", "Meccan", 49, 76, 27),
      SurahEntity(53, "النَّجْم", "An-Najm", "The Star", "سورۃ النجم", "Meccan", 62, 23, 27),
      SurahEntity(54, "القَمَر", "Al-Qamar", "The Moon", "سورۃ القمر", "Meccan", 55, 37, 27),
      SurahEntity(55, "الرَّحْمَٰن", "Ar-Rahman", "The Beneficent", "سورۃ الرحمن", "Medinan", 78, 97, 27),
      SurahEntity(56, "الوَاقِعَة", "Al-Waqi'ah", "The Inevitable", "سورۃ الواقعہ", "Meccan", 96, 46, 27),
      SurahEntity(57, "الحَدِيد", "Al-Hadid", "The Iron", "سورۃ الحدید", "Medinan", 29, 94, 27),
      SurahEntity(58, "المُجَادِلَة", "Al-Mujadila", "The Pleading Woman", "سورۃ المجادلہ", "Medinan", 22, 105, 28),
      SurahEntity(59, "الحَشْر", "Al-Hashr", "The Exile", "سورۃ الحشر", "Medinan", 24, 101, 28),
      SurahEntity(60, "المُمْتَحَنَة", "Al-Mumtahanah", "She That Is Examined", "سورۃ الممتحنہ", "Medinan", 13, 91, 28),
      SurahEntity(61, "الصَّفّ", "As-Saff", "The Ranks", "سورۃ الصف", "Medinan", 14, 109, 28),
      SurahEntity(62, "الجُمُعَة", "Al-Jumu'ah", "Friday", "سورۃ الجمعہ", "Medinan", 11, 110, 28),
      SurahEntity(63, "المُنَافِقُون", "Al-Munafiqun", "The Hypocrites", "سورۃ المنافقون", "Medinan", 11, 104, 28),
      SurahEntity(64, "التَّغَابُن", "At-Taghabun", "Mutual Loss and Gain", "سورۃ التغابن", "Medinan", 18, 108, 28),
      SurahEntity(65, "الطَّلَاق", "At-Talaq", "The Divorce", "سورۃ الطلاق", "Medinan", 12, 99, 28),
      SurahEntity(66, "التَّحْرِيم", "At-Tahrim", "The Prohibition", "سورۃ التحریم", "Medinan", 12, 107, 28),
      SurahEntity(67, "المُلْك", "Al-Mulk", "The Sovereignty", "سورۃ الملک", "Meccan", 30, 77, 29),
      SurahEntity(68, "القَلَم", "Al-Qalam", "The Pen", "سورۃ القلم", "Meccan", 52, 2, 29),
      SurahEntity(69, "الحَاقَّة", "Al-Haqqah", "The Inevitable Reality", "سورۃ الحاقہ", "Meccan", 52, 78, 29),
      SurahEntity(70, "المَعَارِج", "Al-Ma'arij", "The Ascending Stairways", "سورۃ المعارج", "Meccan", 44, 79, 29),
      SurahEntity(71, "نُوح", "Nuh", "Noah", "سورۃ نوح", "Meccan", 28, 71, 29),
      SurahEntity(72, "الجِنّ", "Al-Jinn", "The Jinn", "سورۃ الجن", "Meccan", 28, 40, 29),
      SurahEntity(73, "المُزَّمِّل", "Al-Muzzammil", "The Enshrouded One", "سورۃ المزمل", "Meccan", 20, 3, 29),
      SurahEntity(74, "المُدَّثِّر", "Al-Muddaththir", "The Cloaked One", "سورۃ المدثر", "Meccan", 56, 4, 29),
      SurahEntity(75, "القِيَامَة", "Al-Qiyamah", "The Resurrection", "سورۃ القیامہ", "Meccan", 40, 31, 29),
      SurahEntity(76, "الإِنسَان", "Al-Insan", "Man", "سورۃ الدھر / الانسان", "Medinan", 31, 98, 29),
      SurahEntity(77, "المُرْسَلَات", "Al-Mursalat", "The Emissaries", "سورۃ المرسلات", "Meccan", 50, 33, 29),
      SurahEntity(78, "النَّبَأ", "An-Naba'", "The Tidings", "سورۃ النباء", "Meccan", 40, 80, 30),
      SurahEntity(79, "النَّازِعَات", "An-Nazi'at", "Those Who Drag Forth", "سورۃ النازعات", "Meccan", 46, 81, 30),
      SurahEntity(80, "عَبَسَ", "'Abasa", "He Frowned", "سورۃ عبس", "Meccan", 42, 24, 30),
      SurahEntity(81, "التَّكْوِير", "At-Takwir", "The Overthrowing", "سورۃ التکویر", "Meccan", 29, 7, 30),
      SurahEntity(82, "الانفِطَار", "Al-Infitar", "The Cleaving", "سورۃ الانفطار", "Meccan", 19, 82, 30),
      SurahEntity(83, "المُطَفِّفِينَ", "Al-Mutaffifin", "Defrauding", "سورۃ المطففین", "Meccan", 36, 86, 30),
      SurahEntity(84, "الانشِقَاق", "Al-Inshiqaq", "The Splitting Open", "سورۃ الانشقاق", "Meccan", 25, 83, 30),
      SurahEntity(85, "البُرُوج", "Al-Buruj", "The Constellations", "سورۃ البروج", "Meccan", 22, 27, 30),
      SurahEntity(86, "الطَّارِق", "At-Tariq", "The Nightcomer", "سورۃ الطارق", "Meccan", 17, 36, 30),
      SurahEntity(87, "الأَعْلَى", "Al-A'la", "The Most High", "سورۃ الاعلیٰ", "Meccan", 19, 8, 30),
      SurahEntity(88, "الغَاشِيَة", "Al-Ghashiyah", "The Overwhelming Event", "سورۃ الغاشیہ", "Meccan", 26, 68, 30),
      SurahEntity(89, "الفَجْر", "Al-Fajr", "The Dawn", "سورۃ الفجر", "Meccan", 30, 10, 30),
      SurahEntity(90, "البَلَد", "Al-Balad", "The City", "سورۃ البلد", "Meccan", 20, 35, 30),
      SurahEntity(91, "الشَّمْس", "Ash-Shams", "The Sun", "سورۃ الشمس", "Meccan", 15, 26, 30),
      SurahEntity(92, "اللَّيْل", "Al-Layl", "The Night", "سورۃ اللیل", "Meccan", 21, 9, 30),
      SurahEntity(93, "الضُّحَى", "Ad-Duha", "The Morning Brightness", "سورۃ الضحیٰ", "Meccan", 11, 11, 30),
      SurahEntity(94, "الشَّرْح", "Ash-Sharh", "The Expansion of Breast", "سورۃ الانشراح", "Meccan", 8, 12, 30),
      SurahEntity(95, "التِّين", "At-Tin", "The Fig", "سورۃ التین", "Meccan", 8, 28, 30),
      SurahEntity(96, "العَلَق", "Al-'Alaq", "The Clot", "سورۃ العلق", "Meccan", 19, 1, 30),
      SurahEntity(97, "القَدْر", "Al-Qadr", "The Night of Decree", "سورۃ القدر", "Meccan", 5, 25, 30),
      SurahEntity(98, "البَيِّنَة", "Al-Bayyinah", "The Clear Evidence", "سورۃ البینہ", "Medinan", 8, 100, 30),
      SurahEntity(99, "الزَّلْزَلَة", "Az-Zalzalah", "The Earthquake", "سورۃ الزلزلہ", "Medinan", 8, 93, 30),
      SurahEntity(100, "العَادِيَات", "Al-'Adiyat", "The Courser", "سورۃ العادیات", "Meccan", 11, 14, 30),
      SurahEntity(101, "القَارِعَة", "Al-Qari'ah", "The Calamity", "سورۃ القارعہ", "Meccan", 11, 30, 30),
      SurahEntity(102, "التَّكَاثُر", "At-Takathur", "Competition in Increase", "سورۃ التکاثر", "Meccan", 8, 16, 30),
      SurahEntity(103, "العَصْر", "Al-'Asr", "The Declining Day", "سورۃ العصر", "Meccan", 3, 13, 30),
      SurahEntity(104, "الهُمَزَة", "Al-Humazah", "The Scorner", "سورۃ الہمزہ", "Meccan", 9, 32, 30),
      SurahEntity(105, "الفِيل", "Al-Fil", "The Elephant", "سورۃ الفیل", "Meccan", 5, 19, 30),
      SurahEntity(106, "قُرَيْش", "Quraysh", "Quraysh", "سورۃ قریش", "Meccan", 4, 29, 30),
      SurahEntity(107, "المَاعُون", "Al-Ma'un", "Small Kindnesses", "سورۃ الماعون", "Meccan", 7, 17, 30),
      SurahEntity(108, "الكَوْثَر", "Al-Kawthar", "Abundance", "سورۃ الکوثر", "Meccan", 3, 15, 30),
      SurahEntity(109, "الكَافِرُون", "Al-Kafirun", "The Disbelievers", "سورۃ الکافرون", "Meccan", 6, 18, 30),
      SurahEntity(110, "النَّصْر", "An-Nasr", "Divine Support", "سورۃ النصر", "Medinan", 3, 114, 30),
      SurahEntity(111, "المَسَد", "Al-Masad", "The Palm Fiber", "سورۃ المسد", "Meccan", 5, 6, 30),
      SurahEntity(112, "الإِخْلَاص", "Al-Ikhlas", "The Sincerity", "سورۃ الاخلاص", "Meccan", 4, 22, 30),
      SurahEntity(113, "الفَلَق", "Al-Falaq", "The Daybreak", "سورۃ الفلق", "Meccan", 5, 20, 30),
      SurahEntity(114, "النَّاس", "An-Nas", "Mankind", "سورۃ الناس", "Meccan", 6, 21, 30)
    )
  }

  fun getFoundationalAyahs(): List<AyahEntity> {
    val ayahs = mutableListOf<AyahEntity>()

    // Surah 1: Al-Fatihah (Complete 1-7)
    ayahs.add(
      AyahEntity(
        surahNumber = 1,
        verseNumber = 1,
        textArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        translationEnglish = "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
        translationUrdu = "شروع اللہ کا نام لے کر جو بڑا مہربان نہایت رحم والا ہے",
        transliteration = "Bismillāhir-Raḥmānir-Raḥīm",
        juzNumber = 1,
        pageNumber = 1,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/001001.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 1,
        verseNumber = 2,
        textArabic = "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
        translationEnglish = "[All] praise is [due] to Allah, Lord of the worlds -",
        translationUrdu = "سب طرح کی تعریف خدا ہی کو (سزاوار) ہے جو تمام مخلوقات کا پروردگار ہے",
        transliteration = "Al-ḥamdu lillāhi Rabbil-ʻālamīn",
        juzNumber = 1,
        pageNumber = 1,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/001002.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 1,
        verseNumber = 3,
        textArabic = "الرَّحْمَٰنِ الرَّحِيمِ",
        translationEnglish = "The Entirely Merciful, the Especially Merciful,",
        translationUrdu = "بڑا مہربان نہایت رحم والا",
        transliteration = "Ar-Raḥmānir-Raḥīm",
        juzNumber = 1,
        pageNumber = 1,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/001003.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 1,
        verseNumber = 4,
        textArabic = "مَالِكِ يَوْمِ الدِّينِ",
        translationEnglish = "Sovereign of the Day of Recompense.",
        translationUrdu = "انصاف کے دن کا حاکم",
        transliteration = "Māliki Yawmid-Dīn",
        juzNumber = 1,
        pageNumber = 1,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/001004.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 1,
        verseNumber = 5,
        textArabic = "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
        translationEnglish = "It is You we worship and You we ask for help.",
        translationUrdu = "(اے پروردگار) ہم تیری ہی عبادت کرتے ہیں اور تجھ ہی سے مدد مانگتے ہیں",
        transliteration = "Iyyāka naʻbudu wa iyyāka nastaʻīn",
        juzNumber = 1,
        pageNumber = 1,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/001005.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 1,
        verseNumber = 6,
        textArabic = "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ",
        translationEnglish = "Guide us to the straight path -",
        translationUrdu = "ہم کو سیدھے راستے پر چلا",
        transliteration = "Ihdināṣ-ṣirāṭal-mustaqīm",
        juzNumber = 1,
        pageNumber = 1,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/001006.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 1,
        verseNumber = 7,
        textArabic = "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
        translationEnglish = "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.",
        translationUrdu = "ان لوگوں کے راستے جن پر تو اپنا فضل وکرم کرتا رہا نہ ان کے جن پر غضب نازل ہوا اور نہ گمراہوں کے",
        transliteration = "Ṣirāṭallaḏīna anʻamta ʻalayhim ġayril-maġḍūbi ʻalayhim walāḍ-ḍāllīn",
        juzNumber = 1,
        pageNumber = 1,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/001007.mp3"
      )
    )

    // Surah 2: Ayat al-Kursi (2:255)
    ayahs.add(
      AyahEntity(
        surahNumber = 2,
        verseNumber = 255,
        textArabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
        translationEnglish = "Allah - there is no deity except Him, the Ever-Living, the Sustainer of [all] existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that can intercede with Him except by His permission? He knows what is [presently] before them and what will be after them, and they encompass not a thing of His knowledge except for what He wills. His Kursi extends over the heavens and the earth, and their preservation tires Him not. And He is the Most High, the Most Great.",
        translationUrdu = "اللہ کے سوا کوئی عبادت کے لائق نہیں، وہ زندہ ہے سب کا تھامنے والا، نہ اس پر اونگھ طاری ہوتی ہے نہ نیند، جو کچھ آسمانوں میں اور زمین میں ہے اسی کا ہے، کون ہے جو اس کی اجازت کے بغیر اس کے حضور شفاعت کر سکے، وہ سب جانتا ہے جو مخلوق کے سامنے ہے اور جو ان کے پیچھے ہے، اور وہ اس کے علم میں سے کسی چیز کا احاطہ نہیں کر سکتے سوائے اس کے جتنا وہ چاہے، اس کی کرسی آسمانوں اور زمین کو گھیرے ہوئے ہے اور ان کی حفاظت اس پر گراں نہیں گزرتی، اور وہ عالی رتبہ اور بزرگ ہے۔",
        transliteration = "Allāhu lā ilāha illā huwal-Ḥayyul-Qayyūm...",
        juzNumber = 3,
        pageNumber = 42,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/002255.mp3"
      )
    )

    // Surah 36: Ya-Sin (Verses 1 to 5)
    ayahs.add(
      AyahEntity(
        surahNumber = 36,
        verseNumber = 1,
        textArabic = "يس",
        translationEnglish = "Ya-Seen.",
        translationUrdu = "یٰسین",
        transliteration = "Yā-Sīn",
        juzNumber = 22,
        pageNumber = 440,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/036001.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 36,
        verseNumber = 2,
        textArabic = "وَالْقُرْآنِ الْحَكِيمِ",
        translationEnglish = "By the wise Qur'an.",
        translationUrdu = "حکمت والے قرآن کی قسم",
        transliteration = "Wal-Qur'ānil-Ḥakīm",
        juzNumber = 22,
        pageNumber = 440,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/036002.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 36,
        verseNumber = 3,
        textArabic = "إِنَّكَ لَمِنَ الْمُرْسَلِينَ",
        translationEnglish = "Indeed you, [O Muhammad], are from among the messengers,",
        translationUrdu = "کہ بےشک آپ پیغمبروں میں سے ہیں",
        transliteration = "Innaka laminal-mursalīn",
        juzNumber = 22,
        pageNumber = 440,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/036003.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 36,
        verseNumber = 4,
        textArabic = "عَلَىٰ صِرَاطٍ مُّسْتَقِيمٍ",
        translationEnglish = "On a straight path.",
        translationUrdu = "سیدھے راستے پر ہیں",
        transliteration = "ʻAlā ṣirāṭim-mustaqīm",
        juzNumber = 22,
        pageNumber = 440,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/036004.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 36,
        verseNumber = 5,
        textArabic = "تَنزِيلَ الْعَزِيزِ الرَّحِيمِ",
        translationEnglish = "[This is] a revelation of the Exalted in Might, the Merciful,",
        translationUrdu = "(یہ قرآن) خدائے غالب اور مہربان کا نازل کیا ہوا ہے",
        transliteration = "Tanzīlal-ʻAzīzir-Raḥīm",
        juzNumber = 22,
        pageNumber = 440,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/036005.mp3"
      )
    )

    // Surah 67: Al-Mulk (Verses 1 to 4)
    ayahs.add(
      AyahEntity(
        surahNumber = 67,
        verseNumber = 1,
        textArabic = "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
        translationEnglish = "Blessed is He in whose hand is dominion, and He is over all things competent -",
        translationUrdu = "بڑی برکت والا ہے وہ جس کے ہاتھ میں بادشاہی ہے اور وہ ہر چیز پر قادر ہے",
        transliteration = "Tabārakallaḏī biyadihil-mulku wa huwa ʻalā kulli shay'in qadīr",
        juzNumber = 29,
        pageNumber = 562,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/067001.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 67,
        verseNumber = 2,
        textArabic = "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ الْعَزِيزُ الْغَفُورُ",
        translationEnglish = "[He] who created death and life to test you [as to] which of you is best in deed - and He is the Exalted in Might, the Forgiving -",
        translationUrdu = "جس نے موت اور زندگی کو پیدا کیا تاکہ تمہاری آزمائش کرے کہ تم میں کون عمل کے لحاظ سے بہتر ہے اور وہ زبردست بخشنے والا ہے",
        transliteration = "Alladhī khalaqal-mawta wal-ḥayāta liyabluwakum ayyukum aḥsanu ʻamalā...",
        juzNumber = 29,
        pageNumber = 562,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/067002.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 67,
        verseNumber = 3,
        textArabic = "الَّذِي خَلَقَ سَبْعَ سَمَاوَاتٍ طِبَاقًا ۖ مَّا تَرَىٰ فِي خَلْقِ الرَّحْمَٰنِ مِن تَفَاوُتٍ ۖ فَارْجِعِ الْبَصَرَ هَلْ تَرَىٰ مِن فُطُورٍ",
        translationEnglish = "[And] who created seven heavens in layers. You do not see in the creation of the Most Merciful any inconsistency. So return [your] vision [to the sky]; do you see any breaks?",
        translationUrdu = "جس نے سات آسمان اوپر تلے پیدا کیے، تم رحمٰن کی آفرینش میں کوئی خلل نہ دیکھو گے، نگاہ اٹھا کر دیکھو کیا تمہیں کوئی شگاف نظر آتا ہے؟",
        transliteration = "Alladhī khalaqa sabʻa samāwātin ṭibāqan...",
        juzNumber = 29,
        pageNumber = 562,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/067003.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 67,
        verseNumber = 4,
        textArabic = "ثُمَّ ارْجِعِ الْبَصَرَ كَرَّتَيْنِ يَنقَلِبْ إِلَيْكَ الْبَصَرُ خَاسِئًا وَهُوَ حَسِيرٌ",
        translationEnglish = "Then return [your] vision twice again. [Your] vision will return to you humbled while it is fatigued.",
        translationUrdu = "پھر بار بار نظر ڈالو، نگاہ ناکام اور تھک کر تمہاری طرف لوٹ آئے گی",
        transliteration = "Thummarjiʻil-baṣara karratayni yanqalib ilaykal-baṣaru khāsi'aw-wahuwa ḥasīr",
        juzNumber = 29,
        pageNumber = 562,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/067004.mp3"
      )
    )

    // Surah 103: Al-'Asr (Complete 1-3)
    ayahs.add(
      AyahEntity(
        surahNumber = 103,
        verseNumber = 1,
        textArabic = "وَالْعَصْرِ",
        translationEnglish = "By time,",
        translationUrdu = "زمانے کی قسم",
        transliteration = "Wal-ʻaṣr",
        juzNumber = 30,
        pageNumber = 601,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/103001.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 103,
        verseNumber = 2,
        textArabic = "إِنَّ الْإِنسَانَ لَفِي خُسْرٍ",
        translationEnglish = "Indeed, mankind is in loss,",
        translationUrdu = "بےشک انسان خسارے میں ہے",
        transliteration = "Innal-insāna lafī khusr",
        juzNumber = 30,
        pageNumber = 601,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/103002.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 103,
        verseNumber = 3,
        textArabic = "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ",
        translationEnglish = "Except for those who have believed and done righteous deeds and advised each other to truth and advised each other to patience.",
        translationUrdu = "سوائے ان لوگوں کے جو ایمان لائے اور نیک عمل کیے اور ایک دوسرے کو حق کی تلقین اور صبر کی وصیت کرتے رہے",
        transliteration = "Illallaḏīna āmanū wa ʻamiluṣ-ṣāliḥāti wa tawāṣaw bil-ḥaqqi wa tawāṣaw biṣ-ṣabr",
        juzNumber = 30,
        pageNumber = 601,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/103003.mp3"
      )
    )

    // Surah 108: Al-Kawthar (Complete 1-3)
    ayahs.add(
      AyahEntity(
        surahNumber = 108,
        verseNumber = 1,
        textArabic = "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ",
        translationEnglish = "Indeed, We have granted you, [O Muhammad], al-Kawthar.",
        translationUrdu = "(اے محمدﷺ) ہم نے تم کو کوثر عطا فرمائی",
        transliteration = "Innā aʻṭaynākal-kawthar",
        juzNumber = 30,
        pageNumber = 602,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/108001.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 108,
        verseNumber = 2,
        textArabic = "فَصَلِّ لِرَبِّكَ وَانْحَرْ",
        translationEnglish = "So pray to your Lord and sacrifice [to Him alone].",
        translationUrdu = "تو اپنے پروردگار کی نماز پڑھا کرو اور قربانی کیا کرو",
        transliteration = "Faṣalli li-Rabbika wan-ḥar",
        juzNumber = 30,
        pageNumber = 602,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/108002.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 108,
        verseNumber = 3,
        textArabic = "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ",
        translationEnglish = "Indeed, your enemy is the one cut off.",
        translationUrdu = "کچھ شک نہیں کہ تمہارا دشمن ہی بے اولاد رہے گا",
        transliteration = "Inna shāni'aka huwal-abtar",
        juzNumber = 30,
        pageNumber = 602,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/108003.mp3"
      )
    )

    // Surah 112: Al-Ikhlas (Complete 1-4)
    ayahs.add(
      AyahEntity(
        surahNumber = 112,
        verseNumber = 1,
        textArabic = "قُلْ هُوَ اللَّهُ أَحَدٌ",
        translationEnglish = "Say, \"He is Allah, [who is] One,",
        translationUrdu = "کہو کہ وہ اللہ یکتا ہے",
        transliteration = "Qul huwal-Lāhu Aḥad",
        juzNumber = 30,
        pageNumber = 604,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/112001.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 112,
        verseNumber = 2,
        textArabic = "اللَّهُ الصَّمَدُ",
        translationEnglish = "Allah, the Eternal Refuge.",
        translationUrdu = "معبود برحق جو بےنیاز ہے",
        transliteration = "Allāhuṣ-Ṣamad",
        juzNumber = 30,
        pageNumber = 604,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/112002.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 112,
        verseNumber = 3,
        textArabic = "لَمْ يَلِدْ وَلَمْ يُولَدْ",
        translationEnglish = "He neither begets nor is born,",
        translationUrdu = "نہ کسی کا باپ ہے اور نہ کسی کا بیٹا",
        transliteration = "Lam yalid wa lam yūlad",
        juzNumber = 30,
        pageNumber = 604,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/112003.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 112,
        verseNumber = 4,
        textArabic = "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
        translationEnglish = "Nor is there to Him any equivalent.\"",
        translationUrdu = "اور کوئی اس کا ہمسر نہیں",
        transliteration = "Wa lam yakun lahū kufuwan aḥad",
        juzNumber = 30,
        pageNumber = 604,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/112004.mp3"
      )
    )

    // Surah 113: Al-Falaq (Complete 1-5)
    ayahs.add(
      AyahEntity(
        surahNumber = 113,
        verseNumber = 1,
        textArabic = "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ",
        translationEnglish = "Say, \"I seek refuge in the Lord of daybreak",
        translationUrdu = "کہو کہ میں صبح کے پروردگار کی پناہ مانگتا ہوں",
        transliteration = "Qul aʻūḏu birabbil-falaq",
        juzNumber = 30,
        pageNumber = 604,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/113001.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 113,
        verseNumber = 2,
        textArabic = "مِن شَرِّ مَا خَلَقَ",
        translationEnglish = "From the evil of that which He created",
        translationUrdu = "ہر اس چیز کے شر سے جو اس نے پیدا کی",
        transliteration = "Min sharri mā khalaq",
        juzNumber = 30,
        pageNumber = 604,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/113002.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 113,
        verseNumber = 3,
        textArabic = "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ",
        translationEnglish = "And from the evil of darkness when it settles",
        translationUrdu = "اور اندھیری رات کے شر سے جب اس کا اندھیرا چھا جائے",
        transliteration = "Wa min sharri ghāsiqin iḏā waqab",
        juzNumber = 30,
        pageNumber = 604,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/113003.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 113,
        verseNumber = 4,
        textArabic = "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ",
        translationEnglish = "And from the evil of the blowers in knots",
        translationUrdu = "اور گنڈوں پر پھونکنے والیوں کے شر سے",
        transliteration = "Wa min sharrin-naffāthāti fīl-ʻuqad",
        juzNumber = 30,
        pageNumber = 604,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/113004.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 113,
        verseNumber = 5,
        textArabic = "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ",
        translationEnglish = "And from the evil of an envier when he envies.\"",
        translationUrdu = "اور حسد کرنے والے کے شر سے جب وہ حسد کرے",
        transliteration = "Wa min sharri ḥāsidin iḏā ḥasad",
        juzNumber = 30,
        pageNumber = 604,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/113005.mp3"
      )
    )

    // Surah 114: An-Nas (Complete 1-6)
    ayahs.add(
      AyahEntity(
        surahNumber = 114,
        verseNumber = 1,
        textArabic = "قُلْ أَعُوذُ بِرَبِّ النَّاسِ",
        translationEnglish = "Say, \"I seek refuge in the Lord of mankind,",
        translationUrdu = "کہو کہ میں لوگوں کے پروردگار کی پناہ مانگتا ہوں",
        transliteration = "Qul aʻūḏu birabbin-nās",
        juzNumber = 30,
        pageNumber = 604,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/114001.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 114,
        verseNumber = 2,
        textArabic = "مَلِكِ النَّاسِ",
        translationEnglish = "The Sovereign of mankind,",
        translationUrdu = "(یعنی) لوگوں کے بادشاہ کی",
        transliteration = "Malikin-nās",
        juzNumber = 30,
        pageNumber = 604,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/114002.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 114,
        verseNumber = 3,
        textArabic = "إِلَٰهِ النَّاسِ",
        translationEnglish = "The God of mankind,",
        translationUrdu = "لوگوں کے معبود کی",
        transliteration = "Ilāhin-nās",
        juzNumber = 30,
        pageNumber = 604,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/114003.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 114,
        verseNumber = 4,
        textArabic = "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ",
        translationEnglish = "From the evil of the retreating whisperer -",
        translationUrdu = "وسوسہ ڈالنے والے (اور) پیچھے ہٹ جانے والے کے شر سے",
        transliteration = "Min sharril-waswāsil-khannās",
        juzNumber = 30,
        pageNumber = 604,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/114004.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 114,
        verseNumber = 5,
        textArabic = "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ",
        translationEnglish = "Who whispers into the breasts of mankind -",
        translationUrdu = "جو لوگوں کے دلوں میں وسوسے ڈالتا ہے",
        transliteration = "Alladhī yuwaswisu fī ṣudūrin-nās",
        juzNumber = 30,
        pageNumber = 604,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/114005.mp3"
      )
    )
    ayahs.add(
      AyahEntity(
        surahNumber = 114,
        verseNumber = 6,
        textArabic = "مِنَ الْجِنَّةِ وَالنَّاسِ",
        translationEnglish = "From among the jinn and mankind.\"",
        translationUrdu = "وہ جنات میں سے ہو یا انسانوں میں سے",
        transliteration = "Minal-jinnati wan-nās",
        juzNumber = 30,
        pageNumber = 604,
        audioUrl = "https://everyayah.com/data/Husary_128kbps/114006.mp3"
      )
    )

    return ayahs
  }

  fun getFoundationalHadiths(): List<HadithEntity> {
    return ExtendedHadithDataProvider.getExtendedHadiths()
  }

  fun getFoundationalTranslations(): List<com.example.data.local.entities.TranslationEntity> {
    return listOf(
      // Ali Quli Qara'i - Al-Fatihah
      com.example.data.local.entities.TranslationEntity(
        sourceType = "QURAN",
        referenceKey = "1:1",
        surahOrBookNumber = 1,
        verseOrHadithNumber = 1,
        editionKey = "en.qarai",
        editionTitle = "Ali Quli Qara'i",
        languageCode = "en",
        text = "In the Name of Allah, the All-beneficent, the All-merciful."
      ),
      com.example.data.local.entities.TranslationEntity(
        sourceType = "QURAN",
        referenceKey = "1:2",
        surahOrBookNumber = 1,
        verseOrHadithNumber = 2,
        editionKey = "en.qarai",
        editionTitle = "Ali Quli Qara'i",
        languageCode = "en",
        text = "All praise belongs to Allah, Lord of all the worlds,"
      ),
      com.example.data.local.entities.TranslationEntity(
        sourceType = "QURAN",
        referenceKey = "1:3",
        surahOrBookNumber = 1,
        verseOrHadithNumber = 3,
        editionKey = "en.qarai",
        editionTitle = "Ali Quli Qara'i",
        languageCode = "en",
        text = "the All-beneficent, the All-merciful,"
      ),
      com.example.data.local.entities.TranslationEntity(
        sourceType = "QURAN",
        referenceKey = "1:4",
        surahOrBookNumber = 1,
        verseOrHadithNumber = 4,
        editionKey = "en.qarai",
        editionTitle = "Ali Quli Qara'i",
        languageCode = "en",
        text = "Master of the Day of Retribution."
      ),
      com.example.data.local.entities.TranslationEntity(
        sourceType = "QURAN",
        referenceKey = "1:5",
        surahOrBookNumber = 1,
        verseOrHadithNumber = 5,
        editionKey = "en.qarai",
        editionTitle = "Ali Quli Qara'i",
        languageCode = "en",
        text = "You [alone] do we worship, and to You [alone] do we turn for help."
      ),
      com.example.data.local.entities.TranslationEntity(
        sourceType = "QURAN",
        referenceKey = "1:6",
        surahOrBookNumber = 1,
        verseOrHadithNumber = 6,
        editionKey = "en.qarai",
        editionTitle = "Ali Quli Qara'i",
        languageCode = "en",
        text = "Guide us on the straight path,"
      ),
      com.example.data.local.entities.TranslationEntity(
        sourceType = "QURAN",
        referenceKey = "1:7",
        surahOrBookNumber = 1,
        verseOrHadithNumber = 7,
        editionKey = "en.qarai",
        editionTitle = "Ali Quli Qara'i",
        languageCode = "en",
        text = "the path of those whom You have blessed — such as have not incurred Your wrath, nor are they astray."
      ),
      // Ayat al-Kursi (2:255) - Ali Quli Qara'i
      com.example.data.local.entities.TranslationEntity(
        sourceType = "QURAN",
        referenceKey = "2:255",
        surahOrBookNumber = 2,
        verseOrHadithNumber = 255,
        editionKey = "en.qarai",
        editionTitle = "Ali Quli Qara'i",
        languageCode = "en",
        text = "Allah —there is no god except Him— is the Living One, the All-sustainer. Neither drowsiness befalls Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that may intercede with Him except with His permission? He knows that which is before them and that which is behind them, and they do not comprehend anything of His knowledge except what He wishes. His seat embraces the heavens and the earth, and He is not wearied by their preservation, and He is the All-exalted, the All-supreme."
      )
    )
  }
}

