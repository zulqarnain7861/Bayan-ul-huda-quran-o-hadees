package com.example.data.model

data class IslamicBookChapter(
  val chapterNumber: Int,
  val titleEnglish: String,
  val titleArabic: String,
  val arabicExcerpt: String,
  val transliteration: String,
  val englishContent: String,
  val urduContent: String,
  val scholarlyNotes: String,
  val crossReferences: String,
  val jildNumber: Int = 1,
  val jildTitle: String = "Jild 1 (Volume 1)"
)

data class IslamicClassicalBook(
  val id: String,
  val number: Int,
  val titleEnglish: String,
  val titleArabic: String,
  val titleUrdu: String,
  val author: String,
  val eraHijri: String,
  val category: String,
  val numberingSystem: String,
  val summary: String,
  val chapters: List<IslamicBookChapter>,
  val tradition: String = "SUNNI",
  val officialSourceUrl: String = "https://sunnah.com",
  val apiOrRepoUrl: String = "https://github.com/fawazahmed0/hadith-api",
  val sourceTypeLabel: String = "Classical Book & Verified Repository",
  val gradingMethodology: String = "Classical Sunni Muhaddithun & Tahqiq",
  val entryTimestamp: String = "2026-10-23 • 09:30 UTC (Verified Active)"
)

object IslamicBooksCatalog {

  val categories = listOf(
    "All (32)",
    "7 Primary Hadith",
    "Classical Hadith",
    "Tafsir & Exegesis",
    "Seerah & History",
    "Aqeedah & Fiqh",
    "Adhkar & Tazkiyah"
  )

  val all32Books: List<IslamicClassicalBook> = listOf(
    // ==================== 1–7: THE 7 PRIMARY HADITH BOOKS ====================
    createBook(
      id = "book_bukhari",
      number = 1,
      titleEnglish = "Al-Jami' al-Musnad as-Sahih (Sahih al-Bukhari)",
      titleArabic = "صحيح البخاري",
      titleUrdu = "صحیح بخاری",
      author = "Imam Muhammad ibn Isma'il al-Bukhari",
      eraHijri = "194–256 AH",
      category = "7 Primary Hadith",
      numberingSystem = "Sahih International (1–7563) & Fath al-Bari / USC-MSA In-Book",
      summary = "The most authentic book after the Book of Allah, comprising 97 books and 7,563 rigorously authenticated Prophetic traditions.",
      chapter1Title = "Kitab Bad' al-Wahy — How Revelation Began",
      chapter1Ar = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
      chapter1Tr = "Innamal-a'malu bin-niyyat wa innama likulli imri'in ma nawa.",
      chapter1En = "Imam al-Bukhari opens his Sahih with the Hadith of 'Umar ibn al-Khattab (RA) on intentions (Intl #1 / Book 1, Hadith 1), establishing that every act of worship and worldly striving is judged by sincere devotion to Allah.",
      chapter1Ur = "امام بخاریؒ نے اپنی صحیح کا آغاز حدیثِ نیت (بین الاقوامی نمبر 1) سے فرمایا ہے کہ تمام اعمال کا دارومدار نیتوں پر ہے۔",
      chapter2Title = "Kitab al-Iman & Kitab al-'Ilm — Faith and Sacred Knowledge",
      chapter2Ar = "بُنِيَ الإِسْلاَمُ عَلَى خَمْسٍ... خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
      chapter2Tr = "Buniyal-Islamu 'ala khams... Khayrukum man ta'allamal-Qur'ana wa 'allamahu.",
      chapter2En = "Establishes that faith (Iman) comprises statement and action, increases with obedience, and culminates in learning and teaching the Noble Qur'an (Intl #8, #52, #69, #5027).",
      chapter2Ur = "کتاب الایمان اور کتاب العلم میں ارکانِ اسلام، حلال و حرام اور قرآن سیکھنے سکھانے کی فضیلت بیان ہوئی ہے۔",
      refs = "Quran 98:5, 2:255, 96:1-5 • Bukhari Intl #1, #3, #8, #52, #2311, #5027"
    ),
    createBook(
      id = "book_muslim",
      number = 2,
      titleEnglish = "Al-Musnad as-Sahih (Sahih Muslim)",
      titleArabic = "صحيح مسلم",
      titleUrdu = "صحیح مسلم",
      author = "Imam Muslim ibn al-Hajjaj al-Naysaburi",
      eraHijri = "204–261 AH",
      category = "7 Primary Hadith",
      numberingSystem = "Sahih International / Fu'ad Abd al-Baqi (1–3033 / 7563)",
      summary = "Renowned for its unmatched arrangement of chains of transmission (Isnad) and verbatim precision across 56 books.",
      chapter1Title = "Muqaddimah & Kitab al-Iman — Rigor of Isnad and Hadith Jibril",
      chapter1Ar = "الإِحْسَانُ أَنْ تَعْبُدَ اللَّهَ كَأَنَّكَ تَرَاهُ فَإِنْ لَمْ تَكُنْ تَرَاهُ فَإِنَّهُ يَرَاكَ",
      chapter1Tr = "Al-Ihsanu an ta'budallaha ka'annaka tarahu fa-in lam takun tarahu fa-innahu yarak.",
      chapter1En = "Imam Muslim's Muqaddimah lays down the science of Hadith verification, followed by the foundational Hadith of Jibril (Intl #8 / Book 1, Hadith 1) defining Islam, Iman, and Ihsan.",
      chapter1Ur = "مقدمہ صحیح مسلم میں اسناد کی تحقیق کے اصول اور کتاب الایمان میں حدیثِ جبرائیل (نمبر 8) کے ذریعے اسلام، ایمان اور احسان کی تشریح ہے۔",
      chapter2Title = "Kitab at-Taharah & Kitab al-Birr — Purity and Brotherhood",
      chapter2Ar = "الطُّهُورُ شَطْرُ الإِيمَانِ... الْمُسْلِمُ أَخُو الْمُسْلِمِ",
      chapter2Tr = "At-tuhuru shatrul-iman... Al-Muslimu akhul-Muslim.",
      chapter2En = "Covers spiritual and physical purification (Intl #223) and the inviolable rights of brotherhood, compassion, and seeking knowledge (Intl #2564, #2699).",
      chapter2Ur = "پاکیزگی کو نصف ایمان قرار دیا گیا ہے اور اخوتِ اسلامی و طلبِ علم کے فضائل بیان ہوئے ہیں۔",
      refs = "Quran 49:10, 5:6, 18:1-10 • Muslim Intl #8, #223, #809, #2564, #2699"
    ),
    createBook(
      id = "book_abudawud",
      number = 3,
      titleEnglish = "Sunan Abu Dawud",
      titleArabic = "سنن أبي داود",
      titleUrdu = "سنن ابی داؤد",
      author = "Imam Abu Dawud Sulayman ibn al-Ash'ath as-Sijistani",
      eraHijri = "202–275 AH",
      category = "7 Primary Hadith",
      numberingSystem = "International Darussalam (1–5274) & In-Book Reference",
      summary = "Selected from 500,000 narrations to serve as the premier encyclopedia of Prophetic legal rulings (Ahkam) and Sunnah.",
      chapter1Title = "Kitab at-Taharah & Kitab as-Salah — Keys of Worship",
      chapter1Ar = "مِفْتَاحُ الصَّلاَةِ الطُّهُورُ وَتَحْرِيمُهَا التَّكْبِيرُ وَتَحْلِيلُهَا التَّسْلِيمُ",
      chapter1Tr = "Miftahus-salatit-tuhur wa tahrimuhat-takbir wa tahliluhat-taslim.",
      chapter1En = "Details the Prophetic methodology of Wudu, prayer, and recitation of the Qur'an (Intl #61, #1464) with explicit scholar gradings.",
      chapter1Ur = "طہارت، نماز کے مسنون طریقے اور ترتیلِ قرآن سے درجات کی بلندی (حدیث 61، 1464) کا بیان ہے۔",
      chapter2Title = "Kitab al-'Ilm & Kitab al-Adab — Heirs of the Prophets",
      chapter2Ar = "وَإِنَّ الْعُلَمَاءَ وَرَثَةُ الأَنْبِيَاءِ... أَنَا زَعِيمٌ بِبَيْتٍ فِي أَعْلَى الْجَنَّةِ لِمَنْ حَسَّنَ خُلُقَهُ",
      chapter2Tr = "Wa innal-'ulama'a warathatul-anbiya'... Ana za'imun bi-baytin fi a'lal-Jannati liman hassana khuluqah.",
      chapter2En = "Highlights the station of scholars as inheritors of the Prophets (Intl #3641) and the Prophetic guarantee of Paradise for noble character (Intl #4800).",
      chapter2Ur = "علماء کو انبیاء کا وارث قرار دیا گیا ہے اور حسنِ اخلاق پر جنت کے اعلیٰ درجے کی بشارت دی گئی ہے۔",
      refs = "Quran 35:28, 68:4, 73:4 • Abu Dawud Intl #61, #1464, #3641, #4800"
    ),
    createBook(
      id = "book_tirmidhi",
      number = 4,
      titleEnglish = "Jami` at-Tirmidhi (Sunan at-Tirmidhi)",
      titleArabic = "جامع الترمذي",
      titleUrdu = "جامع ترمذی",
      author = "Imam Abu `Isa Muhammad ibn `Isa at-Tirmidhi",
      eraHijri = "209–279 AH",
      category = "7 Primary Hadith",
      numberingSystem = "International Darussalam (1–3956) & Ahmad Shakir In-Book",
      summary = "Combines authentic Hadith grading (Sahih, Hasan) with comparative jurisprudence of the Companions and classical Imams.",
      chapter1Title = "Kitab al-Birr & Sifat al-Qiyamah — Mercy and Mindfulness",
      chapter1Ar = "الرَّاحِمُونَ يَرْحَمُهُمُ الرَّحْمَنُ... احْفَظِ اللَّهَ يَحْفَظْكَ",
      chapter1Tr = "Ar-rahimuna yarhamuhumur-Rahman... Ihfazillaha yahfazk.",
      chapter1En = "Presents the golden Prophetic counsels on universal mercy (Intl #1924) and the legacy counsel to Ibn 'Abbas on guarding Allah's commands (Intl #2516).",
      chapter1Ur = "رحم کرنے والوں پر رحمٰن کی رحمت (حدیث 1924) اور حضرت ابنِ عباسؓ کو وصیت (حدیث 2516) کا بیان۔",
      chapter2Title = "Kitab Fada'il al-Qur'an & Kitab ad-Da'awat",
      chapter2Ar = "مَنْ قَرَأَ حَرْفًا مِنْ كِتَابِ اللَّهِ فَلَهُ بِهِ حَسَنَةٌ... الدُّعَاءُ هُوَ الْعِبَادَةُ",
      chapter2Tr = "Man qara'a harfan min Kitabillahi falahu bihi hasanah... Ad-du'a'u huwal-'ibadah.",
      chapter2En = "Establishes ten rewards per letter of the Qur'an recited (Intl #2910) and that supplication is the very essence of worship linked to Surah Ghafir 40:60 (Intl #3371).",
      chapter2Ur = "قرآن کے ہر حرف پر دس نیکیوں کا ثواب (حدیث 2910) اور دعا کے عین عبادت ہونے کی تشریح (حدیث 3371)۔",
      refs = "Quran 40:60, 35:29, 21:107 • Tirmidhi Intl #1924, #2516, #2910, #3371"
    ),
    createBook(
      id = "book_nasai",
      number = 5,
      titleEnglish = "Sunan an-Nasa'i (Al-Mujtaba)",
      titleArabic = "سنن النسائي (المجتبى)",
      titleUrdu = "سنن نسائی",
      author = "Imam Ahmad ibn Shu`ayb an-Nasa'i",
      eraHijri = "215–303 AH",
      category = "7 Primary Hadith",
      numberingSystem = "International Darussalam (1–5758) & Abu Ghuddah In-Book",
      summary = "Praised by classical Hadith masters for having the strictest conditions of narrator reliability among the Four Sunan.",
      chapter1Title = "Kitab as-Sahw & Kitab as-Siyam — Prophetic Du'as and Ramadan",
      chapter1Ar = "قَدْ جَاءَكُمْ رَمَضَانُ شَهْرٌ مُبَارَكٌ افْتَرَضَ اللَّهُ عَزَّ وَجَلَّ عَلَيْكُمْ صِيَامَهُ",
      chapter1Tr = "Qad ja'akum Ramadanu shahrun mubarakun iftaradallahu 'azza wa jalla 'alaykum siyamah.",
      chapter1En = "Contains comprehensive Prophetic supplications in prayer (Intl #1305) and the glad tidings of Ramadan and Laylat al-Qadr (Intl #2106).",
      chapter1Ur = "نماز کی جامع دعائیں (حدیث 1305) اور ماہِ رمضان و شبِ قدر کی فضیلت (حدیث 2106) کا بیان۔",
      chapter2Title = "Kitab al-Jihad & Birr al-Walidayn — Honoring Mothers",
      chapter2Ar = "فَالْزَمْهَا فَإِنَّ الْجَنَّةَ تَحْتَ رِجْلَيْهَا",
      chapter2Tr = "Falzamha fa-innal-Jannata tahta rijlayha.",
      chapter2En = "Records the famous Prophetic directive to stay in the service of one's mother because Paradise lies beneath her feet (Intl #3104).",
      chapter2Ur = "والدہ کی خدمت کو لازم پکڑنے کا حکم کیونکہ جنت ان کے قدموں کے نیچے ہے (حدیث 3104)۔",
      refs = "Quran 2:183-185, 97:1-5, 17:23-24 • Nasa'i Intl #1305, #2106, #3104"
    ),
    createBook(
      id = "book_ibnmajah",
      number = 6,
      titleEnglish = "Sunan Ibn Majah",
      titleArabic = "سنن ابن ماجه",
      titleUrdu = "سنن ابن ماجہ",
      author = "Imam Muhammad ibn Yazid Ibn Majah al-Qazwini",
      eraHijri = "209–273 AH",
      category = "7 Primary Hadith",
      numberingSystem = "International Fu'ad Abd al-Baqi (1–4341) & In-Book",
      summary = "The sixth canonical book of Kutub al-Sittah, distinguished by its eloquent Muqaddimah on following the Sunnah and its unique Zawa'id chapters.",
      chapter1Title = "Al-Muqaddimah & Kitab al-Ahkam — Obligation of Knowledge & No Harm",
      chapter1Ar = "طَلَبُ الْعِلْمِ فَرِيضَةٌ عَلَى كُلِّ مُسْلِمٍ... لاَ ضَرَرَ وَلاَ ضِرَارَ",
      chapter1Tr = "Talabul-'ilmi faridatun 'ala kulli Muslim... La darara wa la dirar.",
      chapter1En = "Establishes the universal duty of seeking sacred knowledge (Intl #224) and the foundational legal maxim prohibiting all forms of harm (Intl #2340).",
      chapter1Ur = "طلبِ علم کا ہر مسلمان پر فرض ہونا (حدیث 224) اور فقہ اسلامی کا بنیادی قاعدہ لا ضرر ولا ضرار (حدیث 2340)۔",
      chapter2Title = "Kitab az-Zuhd — Repentance and Detachment from Worldly Greed",
      chapter2Ar = "كُلُّ ابْنِ آدَمَ خَطَّاءٌ وَخَيْرُ الْخَطَّائِينَ التَّوَّابُونَ",
      chapter2Tr = "Kullu Bni Adama khatta'un wa khayrul-khatta'inat-tawwabun.",
      chapter2En = "Concludes with heart-awakening chapters on sincere repentance (Intl #4251) and preparation for the Hereafter.",
      chapter2Ur = "کتاب الزہد میں توبہ و استغفار کی فضیلت (حدیث 4251) اور فکرِ آخرت کے جامع ابواب۔",
      refs = "Quran 20:114, 2:222, 39:53 • Ibn Majah Intl #224, #2340, #4251"
    ),
    createBook(
      id = "book_malik",
      number = 7,
      titleEnglish = "Al-Muwatta (Muwatta Imam Malik)",
      titleArabic = "موطأ الإمام مالك",
      titleUrdu = "موطا امام مالک",
      author = "Imam Malik ibn Anas (Imam Dar al-Hijrah)",
      eraHijri = "93–179 AH",
      category = "7 Primary Hadith",
      numberingSystem = "Yahya al-Laythi International (1–1858) & Book:Hadith",
      summary = "The earliest compiled masterwork of Prophetic Hadith, Athar of the Companions, and the living practice ('Amal) of Madinah.",
      chapter1Title = "Kitab al-Qur'an & Kitab al-Qadar — Holding Fast to Qur'an and Sunnah",
      chapter1Ar = "تَرَكْتُ فِيكُمْ أَمْرَيْنِ لَنْ تَضِلُّوا مَا تَمَسَّكْتُمْ بِهِمَا كِتَابَ اللَّهِ وَسُنَّةَ نَبِيِّهِ",
      chapter1Tr = "Taraktu fikum amrayni lan tadillu ma tamassaktum bihima: Kitaballahi wa Sunnata Nabiyyih.",
      chapter1En = "Preserves the golden chain (Silsilat adh-Dhahab) on the virtues of Surah Al-Ikhlas (#486) and holding fast to the Book of Allah and the Sunnah (#1594).",
      chapter1Ur = "سورۃ الاخلاص کی فضیلت (نمبر 486) اور کتاب اللہ و سنتِ رسول ﷺ کو مضبوطی سے تھامنے کا فرمان (نمبر 1594)۔",
      chapter2Title = "Kitab Husn al-Khuluq — Perfection of Noble Character",
      chapter2Ar = "إِنَّمَا بُعِثْتُ لأُتَمِّمَ حُسْنَ الأَخْلاَقِ",
      chapter2Tr = "Innama bu'ithtu li-utammima husnal-akhlaq.",
      chapter2En = "Summarizes the moral mission of Prophethood: perfecting noble character, humility, and compassion (#1609).",
      chapter2Ur = "بعثتِ نبوی ﷺ کا عظیم مقصد مکارمِ اخلاق کی تکمیل ہے (نمبر 1609)۔",
      refs = "Quran 112:1-4, 3:103, 68:4 • Muwatta Malik #486, #1594, #1609"
    ),

    // ==================== 8–16: CLASSICAL HADITH COMPENDIUMS & MUSNADS ====================
    createBook(
      id = "book_ahmad",
      number = 8,
      titleEnglish = "Musnad Imam Ahmad ibn Hanbal",
      titleArabic = "مسند الإمام أحمد بن حنبل",
      titleUrdu = "مسند امام احمد بن حنبل",
      author = "Imam Ahmad ibn Hanbal ash-Shaybani",
      eraHijri = "164–241 AH",
      category = "Classical Hadith",
      numberingSystem = "Shu'ayb al-Arna'ut / International (1–27,647)",
      summary = "The largest classical Musnad encyclopedia, organized by the Companions who narrated from the Prophet (ﷺ), starting with the Ten Promised Paradise.",
      chapter1Title = "Musnad al-'Asharah al-Mubashsharin bil-Jannah",
      chapter1Ar = "أَرْحَمُ أُمَّتِي بِأُمَّتِي أَبُو بَكْرٍ وَأَشَدُّهُمْ فِي دِينِ اللَّهِ عُمَرُ",
      chapter1Tr = "Arhamu ummati bi-ummati Abu Bakrin wa ashadduhum fi dinillahi 'Umar.",
      chapter1En = "Opens with the narrations of Abu Bakr as-Siddiq, 'Umar, 'Uthman, and 'Ali (RA), preserving foundational Prophetic sermons and supplications.",
      chapter1Ur = "عشرہ مبشرہ خصوصاً خلفائے راشدین کی مرویات سے آغاز، جس میں عقیدہ و سنت کے بنیادی معارف جمع ہیں۔",
      chapter2Title = "Musnad al-Mukthirin min as-Sahabah",
      chapter2Ar = "إِنَّ اللَّهَ لاَ يَنْظُرُ إِلَى صُوَرِكُمْ وَأَمْوَالِكُمْ وَلَكِنْ يَنْظُرُ إِلَى قُلُوبِكُمْ وَأَعْمَالِكُمْ",
      chapter2Tr = "Innallaha la yanzuru ila suwarikum wa amwalikum walakin yanzuru ila qulubikum wa a'malikum.",
      chapter2En = "Compiles the extensive narrations of Abu Hurairah, Ibn 'Umar, Anas, 'Aisha, and Ibn 'Abbas (RA) on sincerity of heart and righteous action.",
      chapter2Ur = "کثرت سے روایت کرنے والے صحابہ کرام کی مسانید، جن میں اخلاصِ قلب اور عملِ صالح کی اہمیت بیان ہوئی ہے۔",
      refs = "Quran 26:89, 49:13 • Musnad Ahmad #1, #7876, #10945"
    ),
    createBook(
      id = "book_darimi",
      number = 9,
      titleEnglish = "Sunan ad-Darimi (Musnad ad-Darimi)",
      titleArabic = "سنن الدارمي",
      titleUrdu = "سنن دارمی",
      author = "Imam Abu Muhammad 'Abdullah ibn 'Abdur-Rahman ad-Darimi",
      eraHijri = "181–255 AH",
      category = "Classical Hadith",
      numberingSystem = "International Husayn Salim Asad (1–3503)",
      summary = "Celebrated as one of the Nine Books of Hadith, featuring a lengthy introduction on the virtue of the Prophet (ﷺ), adherence to the Sunnah, and the etiquette of scholars.",
      chapter1Title = "Al-Muqaddimah — Reverence for Knowledge and the Sunnah",
      chapter1Ar = "كُونُوا يَنَابِيعَ الْعِلْمِ مَصَابِيحَ الْهُدَى",
      chapter1Tr = "Kunu yanabi'al-'ilmi masabihal-huda.",
      chapter1En = "Contains authentic narrations and statements of the Companions urging students of knowledge to become springs of wisdom and lamps of guidance.",
      chapter1Ur = "علم کے چشمے اور ہدایت کے چراغ بننے کی تلقین اور اتباعِ سنت کے زریں اصول۔",
      chapter2Title = "Kitab Fada'il al-Qur'an — Excellence of the Book of Allah",
      chapter2Ar = "الْقُرْآنُ شَافِعٌ مُشَفَّعٌ",
      chapter2Tr = "Al-Qur'anu shafi'un mushaffa'.",
      chapter2En = "Details the intercession of the Qur'an on the Day of Judgment and the virtues of Surah Al-Baqarah, Al-Mulk, and Ya-Sin.",
      chapter2Ur = "قیامت کے دن قرآن کریم کی شفاعت اور سورۃ البقرہ و سورۃ الملک کے فضائل۔",
      refs = "Quran 35:29, 67:1 • Sunan ad-Darimi #3350, #3407"
    ),
    createBook(
      id = "book_riyad",
      number = 10,
      titleEnglish = "Riyad as-Salihin (The Meadows of the Righteous)",
      titleArabic = "رياض الصالحين",
      titleUrdu = "ریاض الصالحین",
      author = "Imam Abu Zakariyya Yahya ibn Sharaf an-Nawawi",
      eraHijri = "631–676 AH",
      category = "Classical Hadith",
      numberingSystem = "International Continuous (1–1896) & 19 Books / 372 Chapters",
      summary = "Unites Quranic verses with authentic Prophetic Hadiths at the start of every chapter to guide personal character, worship, and spiritual life.",
      chapter1Title = "Book 1: Sincerity (Ikhlas), Repentance (Tawbah) & Patience (Sabr)",
      chapter1Ar = "وَمَا أُمِرُوا إِلَّا لِيَعْبُدُوا اللَّهَ مُخْلِصِينَ لَهُ الدِّينَ... عَجَبًا لأَمْرِ الْمُؤْمِنِ",
      chapter1Tr = "Wa ma umiru illa liya'budullaha mukhlisina lahud-din... 'Ajaban li-amril-mu'min.",
      chapter1En = "Imam an-Nawawi opens each chapter with Quranic verses (98:5, 24:31, 2:153) followed by authentic Hadiths (#1, #14, #27) illustrating how Quran and Sunnah illuminate one another.",
      chapter1Ur = "امام نوویؒ ہر باب کا آغاز قرآنی آیات سے فرماتے ہیں اور پھر ان کے مطابق صحیح احادیث (#1، #14، #27) ذکر فرماتے ہیں۔",
      chapter2Title = "Book 16–18: Remembrance, Du'a & Istighfar",
      chapter2Ar = "كَلِمَتَانِ خَفِيفَتَانِ عَلَى اللِّسَانِ ثَقِيلَتَانِ فِي الْمِيزَانِ حَبِيبَتَانِ إِلَى الرَّحْمَنِ: سُبْحَانَ اللَّهِ وَبِحَمْدِهِ سُبْحَانَ اللَّهِ الْعَظِيمِ",
      chapter2Tr = "Kalimatani khafifatani 'alal-lisani thaqilatani fil-mizani habibatani ilar-Rahman: SubhanAllahi wa bihamdihi, SubhanAllahil-'Azim.",
      chapter2En = "Presents the beloved treasures of remembrance that weigh heavily on the Scale on the Day of Resurrection (#1408).",
      chapter2Ur = "زبان پر ہلکے اور میزانِ عمل میں وزنی دو کلمات: سبحان اللہ وبحمدہ، سبحان اللہ العظیم (#1408)۔",
      refs = "Quran 98:5, 2:152-153, 33:41 • Riyad as-Salihin #1, #14, #27, #1408"
    ),
    createBook(
      id = "book_nawawi40",
      number = 11,
      titleEnglish = "Al-Arba'in an-Nawawiyyah (40 Hadith Nawawi)",
      titleArabic = "الأربعون النووية",
      titleUrdu = "اربعین نووی",
      author = "Imam Yahya ibn Sharaf an-Nawawi & Hafiz Ibn Rajab al-Hanbali",
      eraHijri = "631–676 AH (Expanded to 50 by Ibn Rajab d. 795 AH)",
      category = "Classical Hadith",
      numberingSystem = "Standard Nawawi (1–42) & Jami' al-'Ulum wal-Hikam (1–50)",
      summary = "Forty-two comprehensive Prophetic traditions (Jawami' al-Kalim), each forming a cardinal axis of Islamic theology, law, and ethics.",
      chapter1Title = "Hadiths 1–10: Foundations of Intention, Creed, and Lawful Sustenance",
      chapter1Ar = "إِنَّ اللَّهَ طَيِّبٌ لاَ يَقْبَلُ إِلاَّ طَيِّبًا",
      chapter1Tr = "Innallaha Tayyibun la yaqbalu illa tayyiban.",
      chapter1En = "Covers intention (#1), Islam/Iman/Ihsan (#2), pillars (#3), creation & decree (#4), rejection of innovation (#5), lawful & doubtful (#6), sincere counsel/Nasihah (#7), and pure earnings (#10).",
      chapter1Ur = "حدیث 1 تا 10 میں نیت، عقیدہ، حلال و حرام، نصیحت اور کسبِ حلال کے بنیادی اصول۔",
      chapter2Title = "Hadiths 11–42: Leaving Doubt, Brotherhood, Modesty & Divine Mercy",
      chapter2Ar = "دَعْ مَا يَرِيبُكَ إِلَى مَا لاَ يَرِيبُكَ... يَا ابْنَ آدَمَ إِنَّكَ مَا دَعَوْتَنِي وَرَجَوْتَنِي غَفَرْتُ لَكَ",
      chapter2Tr = "Da' ma yaribuka ila ma la yaribuk... Ya Bna Adama innaka ma da'awtani wa rajawtani ghafartu lak.",
      chapter2En = "Culminates in the vast Hadith Qudsi (#42) promising forgiveness to whoever calls upon Allah with hope and without associating partners with Him.",
      chapter2Ur = "اربعین نووی کی تکمیل حدیثِ قدسی (#42) پر ہوتی ہے جس میں اللہ کی وسیع مغفرت اور توحید کی فضیلت ہے۔",
      refs = "Quran 23:51, 39:53 • Nawawi #1–#42"
    ),
    createBook(
      id = "book_qudsi40",
      number = 12,
      titleEnglish = "Al-Ahadith al-Qudsiyyah (40 Hadith Qudsi)",
      titleArabic = "الأحاديث القدسية الأربعون",
      titleUrdu = "چالیس احادیثِ قدسیہ",
      author = "Compiled from the Six Canonical Books (Mulla 'Ali al-Qari & Imam an-Nawawi)",
      eraHijri = "Classical Consensus",
      category = "Classical Hadith",
      numberingSystem = "Standard Qudsi (1–40) with Sahihayn Cross-Ref",
      summary = "Sacred narrations in which the Prophet (ﷺ) relates the meaning directly from Allah the Exalted, illuminating Divine Mercy, Justice, and Love.",
      chapter1Title = "Qudsi #1 & #17: My Mercy Prevails Over My Wrath & Prohibition of Oppression",
      chapter1Ar = "إِنَّ رَحْمَتِي سَبَقَتْ غَضَبِي... يَا عِبَادِي إِنِّي حَرَّمْتُ الظُّلْمَ عَلَى نَفْسِي وَجَعَلْتُهُ بَيْنَكُمْ مُحَرَّمًا فَلاَ تَظَالَمُوا",
      chapter1Tr = "Inna rahmati sabaqat ghadabi... Ya 'ibadi inni harramtuz-zulma 'ala Nafsi wa ja'altuhu baynakum muharraman fala tazalamu.",
      chapter1En = "Establishes Allah's all-encompassing mercy (Bukhari 3194) and His solemn prohibition of injustice and oppression among His servants (Muslim 2577).",
      chapter1Ur = "میری رحمت میرے غضب پر غالب ہے (بخاری 3194) اور اے میرے بندو! میں نے ظلم کو اپنے اوپر اور تمہارے درمیان حرام قرار دیا ہے (مسلم 2577)۔",
      chapter2Title = "Qudsi #15 & #25: I Am as My Servant Thinks of Me & Drawing Near Through Worship",
      chapter2Ar = "أَنَا عِنْدَ ظَنِّ عَبْدِي بِي وَأَنَا مَعَهُ إِذَا ذَكَرَنِي",
      chapter2Tr = "Ana 'inda zanni 'abdi Bi wa Ana ma'ahu idha dhakarani.",
      chapter2En = "Affirms that Allah is with His servant whenever they remember Him, and if the servant approaches by a handspan, Allah draws near by an arm's length (Bukhari 7405, 6502).",
      chapter2Ur = "میں اپنے بندے کے گمان کے مطابق اس سے معاملہ کرتا ہوں اور جب وہ مجھے یاد کرتا ہے میں اس کے ساتھ ہوتا ہوں۔",
      refs = "Quran 6:12, 6:54, 2:152 • Bukhari 3194, 6502, 7405; Muslim 2577"
    ),
    createBook(
      id = "book_adab_mufrad",
      number = 13,
      titleEnglish = "Al-Adab al-Mufrad (Prophetic Manners & Etiquette)",
      titleArabic = "الأدب المفرد للبخاري",
      titleUrdu = "الادب المفرد (امام بخاریؒ)",
      author = "Imam Muhammad ibn Isma'il al-Bukhari",
      eraHijri = "194–256 AH",
      category = "Classical Hadith",
      numberingSystem = "International Fu'ad Abd al-Baqi (1–1322)",
      summary = "Imam al-Bukhari's dedicated masterpiece of 1,322 narrations on filial piety, kinship ties, neighborly care, gentleness, and social ethics.",
      chapter1Title = "Chapters 1–20: Dutifulness to Parents (Birr al-Walidayn) & Kinship",
      chapter1Ar = "وَوَصَّيْنَا الإِنسَانَ بِوَالِدَيْهِ حُسْنًا... مَنْ أَحَبَّ أَنْ يُبْسَطَ لَهُ فِي رِزْقِهِ فَلْيَصِلْ رَحِمَهُ",
      chapter1Tr = "Wa wassaynal-insana bi-walidayhi husna... Man ahabba an yubsata lahu fi rizqihi fal-yasil rahimah.",
      chapter1En = "Opens with Quran 29:8 and 17:23, detailing the unmatched status of mothers and fathers and the blessing of maintaining family ties (#1–#70).",
      chapter1Ur = "والدین کے ساتھ حسنِ سلوک اور صلہ رحمی سے رزق و عمر میں برکت (حدیث 1 تا 70)۔",
      chapter2Title = "Chapters on Gentleness, Visiting the Sick & Good Character",
      chapter2Ar = "مَنْ يُحْرَمِ الرِّفْقَ يُحْرَمِ الْخَيْرَ",
      chapter2Tr = "Man yuhramir-rifqa yuhramil-khayr.",
      chapter2En = "Whoever is deprived of gentleness is deprived of all good (#463).",
      chapter2Ur = "جو شخص نرمی سے محروم رہا وہ تمام بھلائی سے محروم رہا (#463)۔",
      refs = "Quran 17:23-24, 29:8, 3:159 • Al-Adab al-Mufrad #1, #56, #463"
    ),
    createBook(
      id = "book_shamail",
      number = 14,
      titleEnglish = "Ash-Shama'il al-Muhammadiyyah (Prophetic Character & Appearance)",
      titleArabic = "الشمائل المحمدية للترمذي",
      titleUrdu = "شمائلِ ترمذی",
      author = "Imam Abu `Isa Muhammad at-Tirmidhi",
      eraHijri = "209–279 AH",
      category = "Classical Hadith",
      numberingSystem = "Standard Shama'il (1–415 across 56 Chapters)",
      summary = "Fifty-six intimate chapters describing the noble features, seal of Prophethood, attire, speech, smile, worship, humility, and daily life of the Prophet (ﷺ).",
      chapter1Title = "Chapters 1–10: The Noble Features & Speech of the Messenger of Allah (ﷺ)",
      chapter1Ar = "كَانَ رَسُولُ اللَّهِ ﷺ أَحْسَنَ النَّاسِ وَجْهًا وَأَحْسَنَهُمْ خُلُقًا",
      chapter1Tr = "Kana Rasulullahi (ﷺ) ahsanan-nasi wajhan wa ahsanahum khuluqan.",
      chapter1En = "Describes the radiant countenance of the Prophet (ﷺ) as brighter than the full moon and his speech as clear, measured, and memorable (#1, #7, #221).",
      chapter1Ur = "رسول اللہ ﷺ کا چہرۂ انور چودہویں کے چاند سے زیادہ روشن تھا اور آپ ﷺ کا کلام نہایت واضح اور دل نشین تھا۔",
      chapter2Title = "Chapters 41–56: Humility, Worship, and Character Inside His Home",
      chapter2Ar = "كَانَ خُلُقُهُ الْقُرْآنَ",
      chapter2Tr = "Kana khuluquhul-Qur'an.",
      chapter2En = "His character was the living embodiment of the Noble Qur'an — serving his family, mending his own sandals, and never taking revenge for himself (#330–#415).",
      chapter2Ur = "آپ ﷺ کا اخلاق مجسم قرآن تھا — گھر کے کاموں میں ہاتھ بٹانا، تواضع اور عفو و درگزر۔",
      refs = "Quran 68:4, 33:21 • Shama'il Muhammadiyah #1, #7, #221, #345"
    ),
    createBook(
      id = "book_bulugh",
      number = 15,
      titleEnglish = "Bulugh al-Maram min Adillat al-Ahkam",
      titleArabic = "بلوغ المرام من أدلة الأحكام",
      titleUrdu = "بلوغ المرام (حافظ ابنِ حجر عسقلانیؒ)",
      author = "Shaykh al-Islam Hafiz Ahmad ibn 'Ali Ibn Hajar al-'Asqalani",
      eraHijri = "773–852 AH",
      category = "Classical Hadith",
      numberingSystem = "Standard Bulugh al-Maram (1–1596)",
      summary = "The premier manual of 1,596 legal Hadiths underlying Islamic jurisprudence (Fiqh), concluding with the celebrated Kitab al-Jami' on manners and heart purification.",
      chapter1Title = "Kitab at-Taharah to Kitab al-Hajj — Evidences of Worship",
      chapter1Ar = "هُوَ الطَّهُورُ مَاؤُهُ الْحِلُّ مَيْتَتُهُ",
      chapter1Tr = "Huwat-tahuru ma'uhul-hillu maytatuh.",
      chapter1En = "Opens with the Hadith on the purity of seawater (#1) and systematically presents the Hadith proofs for prayer, Zakat, fasting, and Hajj with precise source grading.",
      chapter1Ur = "سمندر کے پانی کی پاکیزگی (#1) سے آغاز کرتے ہوئے عبادات اور معاملات کے تمام احادیثِ احکام کی تخریج۔",
      chapter2Title = "Kitab al-Jami' — Manners, Kindness, Warning Against Envy & Dhikr",
      chapter2Ar = "الْبِرُّ حُسْنُ الْخُلُقِ",
      chapter2Tr = "Al-birru husnul-khuluq.",
      chapter2En = "Collects core Hadiths on righteousness, avoiding suspicion and envy, and steadfast remembrance of Allah (#1458–#1596).",
      chapter2Ur = "کتاب الجامع میں ادب، صلہ رحمی، زہد و تقویٰ اور اذکار و دعا کی جامع احادیث (#1458 تا #1596)۔",
      refs = "Quran 5:6, 49:12 • Bulugh al-Maram #1, #1458, #1596"
    ),
    createBook(
      id = "book_mishkat",
      number = 16,
      titleEnglish = "Mishkat al-Masabih (Niche of Lamps)",
      titleArabic = "مشكاة المصابيح",
      titleUrdu = "مشکوٰۃ المصابیح",
      author = "Imam Wali al-Din al-Khatib at-Tabrizi (expanding Imam al-Baghawi's Masabih as-Sunnah)",
      eraHijri = "d. 741 AH (al-Baghawi d. 516 AH)",
      category = "Classical Hadith",
      numberingSystem = "International Al-Albani / Zuhayr ash-Shawish (1–6294)",
      summary = "Divides every chapter into Three Sections (Fusul): (1) Bukhari & Muslim, (2) the Four Sunan, and (3) thematic additions from Ahmad, Bayhaqi, and Razin.",
      chapter1Title = "Kitab al-Iman & Kitab al-'Ilm — Three-Tier Hadith Verification",
      chapter1Ar = "الْمُسْلِمُ مَنْ سَلِمَ الْمُسْلِمُونَ مِنْ لِسَانِهِ وَيَدِهِ",
      chapter1Tr = "Al-Muslimu man salimal-Muslimuna min lisanihi wa yadih.",
      chapter1En = "A Muslim is one from whose tongue and hand other Muslims are safe (Mishkat #6 / Bukhari 10).",
      chapter1Ur = "مسلمان وہ ہے جس کی زبان اور ہاتھ سے دوسرے مسلمان محفوظ رہیں (مشکوٰۃ #6)۔",
      chapter2Title = "Kitab Fada'il al-Qur'an & Kitab ad-Da'awat",
      chapter2Ar = "إِنَّ اللَّهَ يَرْفَعُ بِهَذَا الْكِتَابِ أَقْوَامًا وَيَضَعُ بِهِ آخَرِينَ",
      chapter2Tr = "Innallaha yarfa'u bi-hadhal-Kitabi aqwaman wa yada'u bihi akharin.",
      chapter2En = "Indeed Allah elevates peoples by this Book (the Qur'an) and humbles others who abandon it (Mishkat #2115 / Muslim 817).",
      chapter2Ur = "بے شک اللہ اس کتاب (قرآن) کے ذریعے قوموں کو سربلند فرماتا ہے اور اس کے چھوڑنے والوں کو پست کر دیتا ہے (#2115)۔",
      refs = "Quran 35:29, 17:9 • Mishkat al-Masabih #1, #6, #2115"
    ),

    // ==================== 17–22: AUTHENTICATED SAHIH, SUNAN & MUSANNAF COLLECTIONS ====================
    createBook(
      id = "book_ibn_khuzaymah",
      number = 17,
      titleEnglish = "Sahih Ibn Khuzaymah",
      titleArabic = "صحيح ابن خزيمة",
      titleUrdu = "صحیح ابنِ خزیمہ",
      author = "Imam al-A'immah Abu Bakr Muhammad ibn Ishaq Ibn Khuzaymah",
      eraHijri = "223–311 AH",
      category = "Classical Hadith",
      numberingSystem = "Mustafa al-A'zami Edition (1–3079)",
      summary = "One of the earliest independent Sahih collections after Bukhari and Muslim, famous for its detailed chapter headings extracting subtle Fiqh points.",
      chapter1Title = "Kitab at-Taharah & as-Salah — Precision in Prophetic Worship",
      chapter1Ar = "لاَ يَقْبَلُ اللَّهُ صَلاَةً بِغَيْرِ طُهُورٍ",
      chapter1Tr = "La yaqbalullahu salatan bi-ghayri tuhur.",
      chapter1En = "Demonstrates the exact conditions of ritual purity and prayer through continuous chains of trustworthy narrators.",
      chapter1Ur = "متصل اور ثقہ راویوں کی اسناد سے طہارت اور نماز کے دقیق مسائل کا استنباط۔",
      chapter2Title = "Kitab as-Siyam & az-Zakah — Virtues of Ramadan",
      chapter2Ar = "مَنْ صَامَ رَمَضَانَ إِيمَانًا وَاحْتِسَابًا غُفِرَ لَهُ مَا تَقَدَّمَ مِنْ ذَنْبِهِ",
      chapter2Tr = "Man sama Ramadana imanan wahtisaban ghufira lahu ma taqaddama min dhanbih.",
      chapter2En = "Whoever fasts Ramadan out of faith and seeking reward from Allah will have their past sins forgiven (#1887).",
      chapter2Ur = "جس نے ایمان اور احتساب کے ساتھ رمضان کے روزے رکھے اس کے پچھلے گناہ بخش دیے جاتے ہیں (#1887)۔",
      refs = "Quran 2:183-185 • Sahih Ibn Khuzaymah #1, #1887"
    ),
    createBook(
      id = "book_ibn_hibban",
      number = 18,
      titleEnglish = "Sahih Ibn Hibban (Al-Ihsan fi Taqrib Sahih Ibn Hibban)",
      titleArabic = "صحيح ابن حبان",
      titleUrdu = "صحیح ابنِ حبان",
      author = "Imam Abu Hatim Muhammad ibn Hibban al-Busti (arranged by Amir 'Ala' al-Din al-Farisi)",
      eraHijri = "270–354 AH",
      category = "Classical Hadith",
      numberingSystem = "Shu'ayb al-Arna'ut Al-Ihsan Edition (1–7491)",
      summary = "Organized into Five Royal Categories (Commands, Prohibitions, Reports, Permitting Rulings, and Prophetic Actions) covering 7,491 authenticated narrations.",
      chapter1Title = "Book of Raqa'iq & Noble Conduct — Softening the Heart",
      chapter1Ar = "الْمُؤْمِنُ مَنْ أَمِنَهُ النَّاسُ عَلَى دِمَائِهِمْ وَأَمْوَالِهِمْ",
      chapter1Tr = "Al-Mu'minu man aminahun-nasu 'ala dima'ihim wa amwalihim.",
      chapter1En = "The true believer is the one whom people trust with their lives and their property (#180).",
      chapter1Ur = "کامل مومن وہ ہے جس سے لوگ اپنی جانوں اور مالوں کے بارے میں بے خوف اور مامون ہوں (#180)۔",
      chapter2Title = "Book of Supplications & Morning/Evening Remembrance",
      chapter2Ar = "أَفْضَلُ الذِّكْرِ لاَ إِلَهَ إِلاَّ اللَّهُ وَأَفْضَلُ الدُّعَاءِ الْحَمْدُ لِلَّهِ",
      chapter2Tr = "Afdaludh-dhikri La ilaha illallah wa afdalud-du'a'il-hamdu lillah.",
      chapter2En = "The best remembrance is La ilaha illallah and the best supplication is Alhamdulillah (#846).",
      chapter2Ur = "بہترین ذکر لا الٰہ الا اللہ ہے اور بہترین دعا الحمد للہ ہے (#846)۔",
      refs = "Quran 47:19, 1:2 • Sahih Ibn Hibban #180, #846"
    ),
    createBook(
      id = "book_mustadrak",
      number = 19,
      titleEnglish = "Al-Mustadrak 'ala as-Sahihayn",
      titleArabic = "المستدرك على الصحيحين للحاكم",
      titleUrdu = "المستدرک علی الصحیحین (حاکم نیشاپوریؒ)",
      author = "Imam Abu 'Abdullah Muhammad ibn 'Abdullah al-Hakim an-Naysaburi (with Imam adh-Dhahabi's Talkhis)",
      eraHijri = "321–405 AH",
      category = "Classical Hadith",
      numberingSystem = "Mustafa 'Abd al-Qadir 'Ata (1–8803)",
      summary = "Compiles Hadiths meeting the conditions of al-Bukhari and Muslim (or one of them) that were not included in their two books, reviewed by Imam adh-Dhahabi.",
      chapter1Title = "Kitab al-Iman & Kitab at-Tafsir — Companions' Exegesis",
      chapter1Ar = "نِعْمَ التَّرْجُمَانُ لِلْقُرْآنِ ابْنُ عَبَّاسٍ",
      chapter1Tr = "Ni'mat-tarjumanu lil-Qur'ani Bnu 'Abbas.",
      chapter1En = "Contains a vast treasury of authenticated Tafsir narrations from 'Abdullah ibn 'Abbas (RA), whom the Prophet (ﷺ) prayed for to be granted deep understanding of the Book.",
      chapter1Ur = "ترجمان القرآن حضرت عبد اللہ بن عباسؓ کی مرویاتِ تفسیر کا عظیم ذخیرہ جس کی تلخیص امام ذہبیؒ نے فرمائی۔",
      chapter2Title = "Kitab Fada'il as-Sahabah — Virtues of the Noble Companions",
      chapter2Ar = "أَصْحَابِي أَمَنَةٌ لأُمَّتِي",
      chapter2Tr = "Ashabi amanatun li-ummati.",
      chapter2En = "Documents the biographies and virtues of the Muhajirun, Ansar, and Ahl al-Bayt (may Allah be pleased with them all).",
      chapter2Ur = "مہاجرین، انصار اور اہلِ بیتِ اطہار رضوان اللہ علیہم اجمعین کے مناقب و فضائل۔",
      refs = "Quran 9:100, 48:29 • Al-Mustadrak #3062, #4415"
    ),
    createBook(
      id = "book_bayhaqi",
      number = 20,
      titleEnglish = "As-Sunan al-Kubra & Shu'ab al-Iman (Al-Bayhaqi)",
      titleArabic = "السنن الكبرى وشعب الإيمان للبيهقي",
      titleUrdu = "السنن الکبریٰ و شعب الایمان (امام بیہقیؒ)",
      author = "Imam Abu Bakr Ahmad ibn al-Husayn al-Bayhaqi",
      eraHijri = "384–458 AH",
      category = "Classical Hadith",
      numberingSystem = "Sunan al-Kubra (1–21,812) & Shu'ab al-Iman (77 Branches)",
      summary = "Encyclopedic synthesis of Prophetic Sunnah, Companion Athar, and the 77 Branches of Faith mentioned in the Sahih Hadith.",
      chapter1Title = "The First Branch of Faith: Belief in Allah & His Names and Attributes",
      chapter1Ar = "الإِيمَانُ بِضْعٌ وَسَبْعُونَ شُعْبَةً فَأَفْضَلُهَا قَوْلُ لاَ إِلَهَ إِلاَّ اللَّهُ",
      chapter1Tr = "Al-Imanu bid'un wa sab'una shu'batan fa-afdaluha qawlu La ilaha illallah.",
      chapter1En = "Faith consists of over seventy branches: the highest is declaring La ilaha illallah, and the lowest is removing a harmful object from the road; and modesty (Haya') is a branch of faith.",
      chapter1Ur = "ایمان کی ستر سے زائد شاخیں ہیں: سب سے افضل لا الٰہ الا اللہ کہنا ہے اور سب سے ادنیٰ راستے سے تکلیف دہ چیز ہٹانا ہے۔",
      chapter2Title = "Reverence for the Qur'an & Prophetic Ethics",
      chapter2Ar = "أَفْضَلُ عِبَادَةِ أُمَّتِي قِرَاءَةُ الْقُرْآنِ",
      chapter2Tr = "Afdalu 'ibadati ummati qira'atul-Qur'an.",
      chapter2En = "Highlights recitation, contemplation, and implementation of the Qur'an as the noblest devotion of the Ummah.",
      chapter2Ur = "تلاوتِ قرآن، تدبر اور اس پر عمل کو امت کی بہترین عبادت قرار دیا گیا ہے۔",
      refs = "Quran 8:2-4, 35:29 • Shu'ab al-Iman #1, #1865"
    ),
    createBook(
      id = "book_ibn_abi_shaybah",
      number = 21,
      titleEnglish = "Musannaf Ibn Abi Shaybah",
      titleArabic = "مصنف ابن أبي شيبة",
      titleUrdu = "مصنف ابنِ ابی شیبہ",
      author = "Hafiz Abu Bakr 'Abdullah ibn Muhammad Ibn Abi Shaybah (Teacher of Bukhari & Muslim)",
      eraHijri = "159–235 AH",
      category = "Classical Hadith",
      numberingSystem = "Muhammad 'Awwamah Edition (1–38,940)",
      summary = "A monumental pre-Bukhari repository preserving nearly 39,000 Prophetic Hadiths, rulings of the Rightly Guided Caliphs, and verdicts of the Tabi'un.",
      chapter1Title = "Kitab al-Iman, as-Salawat & Fada'il al-Qur'an",
      chapter1Ar = "إِنَّ هَذَا الْقُرْآنَ مَأْدُبَةُ اللَّهِ فَتَعَلَّمُوا مِنْ مَأْدُبَتِهِ مَا اسْتَطَعْتُمْ",
      chapter1Tr = "Inna hadhal-Qur'ana ma'dubatullahi fata'allamu min ma'dubatihi mastata'tum.",
      chapter1En = "Records 'Abdullah ibn Mas'ud's (RA) famous description of the Qur'an as the Banquet of Allah and the Rope of Allah (#30007).",
      chapter1Ur = "حضرت عبد اللہ بن مسعودؓ کا فرمان کہ یہ قرآن اللہ کا دسترخوان اور اللہ کی مضبوط رسی ہے (#30007)۔",
      chapter2Title = "Kitab al-Adab & Kitab az-Zuhd — Wisdom of the Salaf",
      chapter2Ar = "رَأْسُ الْحِكْمَةِ مَخَافَةُ اللَّهِ",
      chapter2Tr = "Ra'sul-hikmati makhafatullah.",
      chapter2En = "The pinnacle of wisdom is reverent fear (Taqwa) of Allah the Exalted.",
      chapter2Ur = "دانائی اور حکمت کا سرچشمہ اللہ تعالیٰ کا خوف اور تقویٰ ہے۔",
      refs = "Quran 3:103, 49:13 • Musannaf Ibn Abi Shaybah #30007, #35412"
    ),
    createBook(
      id = "book_abdur_razzaq",
      number = 22,
      titleEnglish = "Musannaf 'Abd al-Razzaq as-San'ani",
      titleArabic = "مصنف عبد الرزاق الصنعاني",
      titleUrdu = "مصنف عبد الرزاق",
      author = "Imam Abu Bakr 'Abd al-Razzaq ibn Hammam as-San'ani (Teacher of Imam Ahmad)",
      eraHijri = "126–211 AH",
      category = "Classical Hadith",
      numberingSystem = "Habib al-Rahman al-A'zami Edition (1–21,033)",
      summary = "One of the oldest surviving thematic Hadith compilations, ending with the celebrated Al-Jami' of Imam Ma'mar ibn Rashid (d. 153 AH).",
      chapter1Title = "Al-Jami' of Ma'mar ibn Rashid — Early Prophetic & Companion Traditions",
      chapter1Ar = "مَنْ يُرِدِ اللَّهُ بِهِ خَيْرًا يُفَقِّهْهُ فِي الدِّينِ",
      chapter1Tr = "Man yuridillahu bihi khayran yufaqqihhu fid-din.",
      chapter1En = "Whomever Allah intends good for, He grants them deep comprehension (Fiqh) of the religion.",
      chapter1Ur = "اللہ تعالیٰ جس کے ساتھ بھلائی کا ارادہ فرماتا ہے اسے دین کی گہری سمجھ (فقہ) عطا فرماتا ہے۔",
      chapter2Title = "Chapters on Sincerity, Remembrance & Trust in Allah",
      chapter2Ar = "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
      chapter2Tr = "Hasbunallahu wa ni'mal-Wakil.",
      chapter2En = "Explains Ibn 'Abbas's (RA) statement that 'Hasbunallahu wa ni'mal-Wakil' was spoken by Ibrahim (AS) when cast into the fire and by Muhammad (ﷺ) in Surah Ali 'Imran 3:173.",
      chapter2Ur = "حضرت ابنِ عباسؓ کی روایت کہ حسبنا اللہ ونعم الوکیل ابراہیم علیہ السلام اور محمد ﷺ کا کلمۂ توکل ہے۔",
      refs = "Quran 3:173, 9:122 • Musannaf 'Abd al-Razzaq #20455"
    ),

    // ==================== 23–26: TAFSIR & QURANIC SCIENCES ====================
    createBook(
      id = "book_tafsir_ibn_kathir",
      number = 23,
      titleEnglish = "Tafsir al-Qur'an al-'Azim (Tafsir Ibn Kathir)",
      titleArabic = "تفسير القرآن العظيم لابن كثير",
      titleUrdu = "تفسیر ابنِ کثیر",
      author = "Hafiz 'Imad al-Din Isma'il ibn 'Umar ibn Kathir",
      eraHijri = "701–774 AH",
      category = "Tafsir & Exegesis",
      numberingSystem = "Surah 1–114 / Verse-by-Verse (Integrated in Reader)",
      summary = "The gold standard of Tafsir bil-Ma'thur — interpreting every Quranic verse through other Quranic verses, authentic Prophetic Hadiths, and the Sahabah.",
      chapter1Title = "Muqaddimah Usul at-Tafsir — The Four Ranks of Authentic Exegesis",
      chapter1Ar = "إِنْقَالَ قَائِلٌ: فَمَا أَحْسَنُ طُرُقِ التَّفْسِيرِ؟ فَالْجَوَابُ: إِنَّ أَصَحَّ الطُّرُقِ فِي ذَلِكَ أَنْ يُفَسَّرَ الْقُرْآنُ بِالْقُرْآنِ",
      chapter1Tr = "Inna asahhat-turuqi fi dhalika an yufassaral-Qur'anu bil-Qur'an, fa-in a'yaka dhalika fa-bis-Sunnah.",
      chapter1En = "1. Explaining the Qur'an by the Qur'an; 2. Explaining the Qur'an by the Prophetic Sunnah; 3. Statements of the Companions (Sahabah); 4. Statements of the Tabi'un.",
      chapter1Ur = "تفسیر کا سب سے صحیح طریقہ یہ ہے کہ قرآن کی تفسیر قرآن سے، پھر سنتِ نبوی ﷺ سے، پھر اقوالِ صحابہ اور تابعین سے کی جائے۔",
      chapter2Title = "Exegesis of Umm al-Qur'an, Ayat al-Kursi & Al-Mu'awwidhat",
      chapter2Ar = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
      chapter2Tr = "Allahu la ilaha illa Huwal-Hayyul-Qayyum.",
      chapter2En = "Available directly on every single verse in the Quran Reader via the 'Tafsir & Linked Hadith' button.",
      chapter2Ur = "قرآن ریڈر میں ہر آیت کے ساتھ بٹن دبا کر مکمل تفسیر ابنِ کثیر اور متعلقہ احادیث کا مطالعہ کیا جا سکتا ہے۔",
      refs = "Quran 16:44, 2:255 • Integrated across all 114 Surahs"
    ),
    createBook(
      id = "book_tafsir_qurtubi",
      number = 24,
      titleEnglish = "Al-Jami' li-Ahkam al-Qur'an (Tafsir al-Qurtubi)",
      titleArabic = "الجامع لأحكام القرآن للقرطبي",
      titleUrdu = "تفسیر قرطبی (احکام القرآن)",
      author = "Imam Abu 'Abdullah Muhammad ibn Ahmad al-Ansari al-Qurtubi",
      eraHijri = "600–671 AH",
      category = "Tafsir & Exegesis",
      numberingSystem = "Surah 1–114 / Legal & Exegetical Issues (Masa'il)",
      summary = "Masterpiece of Andalusian scholarship extracting legal rulings (Fiqh), ethical duties, and linguistic subtleties from every Surah of the Qur'an.",
      chapter1Title = "Book of Virtues of the Qur'an & Etiquette of Recitation (Tilawah)",
      chapter1Ar = "وَرَتِّلِ الْقُرْآنَ تَرْتِيلًا",
      chapter1Tr = "Wa rattilil-Qur'ana tartila.",
      chapter1En = "Details the inward and outward manners of carrying the Qur'an, acting upon its lawful and unlawful rulings, and reciting with reflection.",
      chapter1Ur = "حاملِ قرآن کے آداب، تلاوت و تدبر کے شرائط اور حلال و حرام پر عمل کرنے کا مفصل بیان۔",
      chapter2Title = "Verse-by-Verse Legal Rulings (Masa'il al-Ahkam)",
      chapter2Ar = "أَفَلَا يَتَدَبَّرُونَ الْقُرْآنَ",
      chapter2Tr = "Afala yatadabbarunal-Qur'an.",
      chapter2En = "Integrated directly into the 8-Tafsir Verse Commentary sheet across all 114 Surahs.",
      chapter2Ur = "قرآن ریڈر کی ہر آیت کے تحت تفسیر قرطبی کے فقہی اور علمی نکات شامل کیے گئے ہیں۔",
      refs = "Quran 73:4, 4:82, 38:29"
    ),
    createBook(
      id = "book_itqan_suyuti",
      number = 25,
      titleEnglish = "Al-Itqan fi 'Ulum al-Qur'an (Sciences of the Qur'an)",
      titleArabic = "الإتقان في علوم القرآن للسيوطي",
      titleUrdu = "الاتقان فی علوم القرآن (امام سیوطیؒ)",
      author = "Imam Jalal al-Din 'Abd al-Rahman as-Suyuti",
      eraHijri = "849–911 AH",
      category = "Tafsir & Exegesis",
      numberingSystem = "80 Classical Disciplines (Anwa') of Quranic Science",
      summary = "The definitive encyclopedia of 80 sciences of the Noble Qur'an: Meccan & Medinan revelation, Asbab al-Nuzul, Qira'at, compilation, and miraculous eloquence (I'jaz).",
      chapter1Title = "Discipline 1–9: Meccan vs. Medinan Surahs & Occasions of Revelation",
      chapter1Ar = "إِنَّا نَحْنُ نَزَّلْنَا الذِّكْرَ وَإِنَّا لَهُ لَحَافِظُونَ",
      chapter1Tr = "Inna Nahnu nazzalnad-Dhikra wa inna lahu la-hafizun.",
      chapter1En = "Explains how the Companions preserved both the exact chronological order of revelation and the Uthmani Mushaf arrangement.",
      chapter1Ur = "مکی و مدنی سورتوں کی پہچان، اسبابِ نزول اور جمعِ قرآن کی مستند تاریخ۔",
      chapter2Title = "Discipline 64–80: Inimitability (I'jaz al-Qur'an) & Exegetical Principles",
      chapter2Ar = "قُل لَّئِنِ اجْتَمَعَتِ الْإِنسُ وَالْجِنُّ عَلَىٰ أَن يَأْتُوا بِمِثْلِ هَٰذَا الْقُرْآنِ لَا يَأْتُونَ بِمِثْلِهِ",
      chapter2Tr = "Qul la'inijtama'atil-insu wal-jinnu 'ala an ya'tu bi-mithli hadhal-Qur'ani la ya'tuna bi-mithlih.",
      chapter2En = "Demonstrates the linguistic, legislative, and unseen-prophecy miracles of the Qur'an (17:88).",
      chapter2Ur = "اعجازِ قرآن، بلاغتِ قرآنی اور مفسر کے لیے ضروری علوم کا جامع بیان۔",
      refs = "Quran 15:9, 17:88, 75:17-19"
    ),
    createBook(
      id = "book_maariful_quran",
      number = 26,
      titleEnglish = "Ma'ariful Quran & Contemporary Exegesis Compendium",
      titleArabic = "معارف القرآن وتيسير الكريم الرحمن",
      titleUrdu = "معارف القرآن و تفہیم و بیان القرآن",
      author = "Mufti Muhammad Shafi Usmani, Shaykh as-Sa'di, Maududi & Dr. Israr Ahmad",
      eraHijri = "14th–15th Century AH",
      category = "Tafsir & Exegesis",
      numberingSystem = "Surah 1–114 (Integrated in 8-Tafsir Reader)",
      summary = "Brings together the finest contemporary orthodox commentaries addressing modern intellectual questions while remaining anchored in classical Sunnah.",
      chapter1Title = "Coherence of the Qur'an (Nazm) & Practical Reformation",
      chapter1Ar = "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ فِيهِ هُدًى لِّلْمُتَّقِينَ",
      chapter1Tr = "Dhalikal-Kitabu la rayba fihi hudan lil-muttaqin.",
      chapter1En = "Shows how every Surah forms a unified thematic necklace guiding personal piety, family harmony, and social justice.",
      chapter1Ur = "ہر سورۃ کے مضامین کا باہمی ربط اور انفرادی و اجتماعی زندگی کی اصلاح کے عملی رہنما اصول۔",
      chapter2Title = "Harmonizing Classical Athar with Contemporary Questions",
      chapter2Ar = "قَدْ جَاءَكُم مِّنَ اللَّهِ نُورٌ وَكِتَابٌ مُّبِينٌ",
      chapter2Tr = "Qad ja'akum minallahi nurun wa Kitabun mubin.",
      chapter2En = "Accessible on every verse inside the Quran Reader.",
      chapter2Ur = "قرآن ریڈر میں ہر آیت کے ساتھ معارف القرآن، السعدی، تفہیم اور بیان القرآن کا مطالعہ دستیاب ہے۔",
      refs = "Quran 2:2, 5:15-16"
    ),

    // ==================== 27–29: SEERAH, AQEEDAH & PROPHETIC GUIDANCE ====================
    createBook(
      id = "book_rahiq",
      number = 27,
      titleEnglish = "Ar-Rahiq al-Makhtum (The Sealed Nectar — Seerah)",
      titleArabic = "الرحيق المختوم في السيرة النبوية",
      titleUrdu = "الرحیق المختوم (سیرتِ النبی ﷺ)",
      author = "Shaykh Safiur-Rahman al-Mubarakpuri ( building on Ibn Hisham & Ibn Kathir)",
      eraHijri = "1362–1427 AH",
      category = "Seerah & History",
      numberingSystem = "Chronological Seerah Eras (Meccan & Medinan Periods)",
      summary = "Award-winning, Hadith-authenticated biography of Prophet Muhammad (ﷺ) from lineage and birth in Makkah to the Farewell Pilgrimage in Madinah.",
      chapter1Title = "The Meccan Era: Lineage, Cave Hira & Thirteen Years of Patience",
      chapter1Ar = "وَإِنَّكَ لَعَلَىٰ خُلُقٍ عَظِيمٍ",
      chapter1Tr = "Wa innaka la-'ala khuluqin 'azim.",
      chapter1En = "Traces the pure Abrahamic lineage of the Prophet (ﷺ), his title Al-Amin (The Trustworthy), the first revelation of Surah Al-'Alaq (96:1-5), and steadfastness in Makkah.",
      chapter1Ur = "نسبِ نبوی ﷺ، غارِ حرا میں پہلی وحی کا نزول اور مکی دور کی تیرہ سالہ دعوت و استقامت۔",
      chapter2Title = "The Medinan Era: Hijrah, Charter of Madinah & Farewell Sermon",
      chapter2Ar = "الْيَوْمَ أَكْمَلْتُ لَكُمْ دِينَكُمْ وَأَتْمَمْتُ عَلَيْكُمْ نِعْمَتِي",
      chapter2Tr = "Al-yawma akmaltu lakum dinakum wa atmamtu 'alaykum ni'mati.",
      chapter2En = "Chronicles the Hijrah, the brotherhood between Muhajirun and Ansar, the Opening of Makkah in mercy, and the universal human rights proclaimed in the Farewell Sermon (5:3).",
      chapter2Ur = "ہجرتِ مدینہ، مواخات، فتحِ مکہ کی بےمثال رحمت اور خطبہ حجۃ الوداع کے عالمگیر انسانی حقوق۔",
      refs = "Quran 68:4, 96:1-5, 5:3, 33:21 • Bukhari 3, Muslim 1218"
    ),
    createBook(
      id = "book_zaad_almaad",
      number = 28,
      titleEnglish = "Zaad al-Ma'ad fi Hadyi Khayr al-'Ibad",
      titleArabic = "زاد المعاد في هدي خير العباد",
      titleUrdu = "زاد المعاد (امام ابنِ قیم الجوزیہؒ)",
      author = "Imam Shams al-Din Muhammad ibn Abi Bakr Ibn Qayyim al-Jawziyyah",
      eraHijri = "691–751 AH",
      category = "Seerah & History",
      numberingSystem = "5 Volumes: Prophetic Guidance in Worship, Manners, Medicine & Law",
      summary = "Unmatched encyclopedia detailing the exact Sunnah and daily guidance (Hady) of the Prophet (ﷺ) in waking, sleeping, prayer, dhikr, family life, and Prophetic medicine (At-Tibb an-Nabawi).",
      chapter1Title = "Prophetic Guidance in Daily Worship, Dhikr & Recitation",
      chapter1Ar = "لَّقَدْ كَانَ لَكُمْ فِي رَسُولِ اللَّهِ أُسْوَةٌ حَسَنَةٌ",
      chapter1Tr = "Laqad kana lakum fi Rasulillahi uswatun hasanah.",
      chapter1En = "Explains how the Prophet (ﷺ) combined outward perfection of worship with complete humility and presence of heart in every moment of the day and night.",
      chapter1Ur = "رسول اللہ ﷺ کے شب و روز، عبادات، تلاوت اور ذکر و دعا کا مکمل مسنون طریقہ۔",
      chapter2Title = "At-Tibb an-Nabawi — Healing of Hearts and Bodies",
      chapter2Ar = "وَنُنَزِّلُ مِنَ الْقُرْآنِ مَا هُوَ شِفَاءٌ وَرَحْمَةٌ لِّلْمُؤْمِنِينَ",
      chapter2Tr = "Wa nunazzilu minal-Qur'ani ma huwa shifa'un wa rahmatun lil-mu'minin.",
      chapter2En = "Demonstrates how the Qur'an is the complete healing for diseases of doubt and desire in the heart alongside physical Prophetic remedies (17:82).",
      chapter2Ur = "قرآن کریم کے ذریعے دلوں کے امراض (شبہات و شہوات) اور جسمانی امراض کی شفا کا بیان۔",
      refs = "Quran 33:21, 17:82, 10:57"
    ),
    createBook(
      id = "book_aqeedah_umdat",
      number = 29,
      titleEnglish = "Al-'Aqeedah at-Tahawiyyah & 'Umdat al-Ahkam",
      titleArabic = "العقيدة الطحاوية وعمدة الأحكام",
      titleUrdu = "عقیدہ طحاویہ و عمدۃ الاحکام",
      author = "Imam Abu Ja'far at-Tahawi (d. 321 AH) & Hafiz 'Abd al-Ghani al-Maqdisi (d. 600 AH)",
      eraHijri = "3rd–6th Century AH",
      category = "Aqeedah & Fiqh",
      numberingSystem = "105 Creed Principles & 430 Agreed-Upon (Muttafaq 'Alayh) Hadiths",
      summary = "Pairs the universally accepted Sunni creed of Imam at-Tahawi with the 430 legal Hadiths agreed upon by both Imam al-Bukhari and Imam Muslim.",
      chapter1Title = "Foundations of Pure Tawhid & Sunni Creed (At-Tahawiyyah)",
      chapter1Ar = "نَقُولُ فِي تَوْحِيدِ اللَّهِ مُعْتَقِدِينَ بِتَوْفِيقِ اللَّهِ: إِنَّ اللَّهَ وَاحِدٌ لاَ شَرِيكَ لَهُ",
      chapter1Tr = "Naqulu fi Tawhidillahi mu'taqidina bi-tawfiqillah: Innallaha Wahidun la sharika lah.",
      chapter1En = "We say regarding the Oneness of Allah, believing by Allah's grace: Indeed Allah is One, without partner; nothing is like unto Him, and nothing is beyond His power.",
      chapter1Ur = "ہم اللہ کی توحید کے بارے میں اعتقاد رکھتے ہوئے کہتے ہیں کہ بے شک اللہ ایک ہے، اس کا کوئی شریک نہیں اور کوئی چیز اس کی مثل نہیں۔",
      chapter2Title = "'Umdat al-Ahkam — Rulings Agreed Upon by Bukhari and Muslim",
      chapter2Ar = "لَيْسَ كَمِثْلِهِ شَيْءٌ وَهُوَ السَّمِيعُ الْبَصِيرُ",
      chapter2Tr = "Laysa kamithlihi shay'un wa Huwas-Sami'ul-Basir.",
      chapter2En = "Gathers the highest tier of authentic Hadiths (Muttafaq 'Alayh) governing purification, prayer, charity, fasting, and Hajj.",
      chapter2Ur = "صحیح بخاری اور صحیح مسلم کی متفق علیہ احادیثِ احکام کا مستند مجموعہ۔",
      refs = "Quran 42:11, 112:1-4 • Bukhari & Muslim Muttafaq 'Alayh"
    ),

    // ==================== 30–32: ADHKAR, DUAS & SPIRITUAL PURIFICATION ====================
    createBook(
      id = "book_hisn_almuslim",
      number = 30,
      titleEnglish = "Hisn al-Muslim (Fortress of the Muslim — Authentic Adhkar)",
      titleArabic = "حصن المسلم من أذكار الكتاب والسنة",
      titleUrdu = "حصن المسلم (قرآن و سنت کی مسنون دعائیں)",
      author = "Shaykh Sa'id ibn 'Ali ibn Wahf al-Qahtani",
      eraHijri = "1371–1440 AH",
      category = "Adhkar & Tazkiyah",
      numberingSystem = "132 Chapters / 267 Authenticated Supplications",
      summary = "The indispensable daily manual of Quranic and Prophetic supplications for morning, evening, prayer, sleep, travel, distress, and gratitude.",
      chapter1Title = "Morning & Evening Adhkar (Adhkar as-Sabah wal-Masa')",
      chapter1Ar = "اللَّهُمَّ أَنْتَ رَبِّي لاَ إِلَهَ إِلاَّ أَنْتَ خَلَقْتَنِي وَأَنَا عَبْدُكَ وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ",
      chapter1Tr = "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduka wa ana 'ala 'ahdika wa wa'dika mastata't...",
      chapter1En = "Sayyid al-Istighfar (The Master Supplication for Forgiveness — Bukhari 6306), Ayat al-Kursi (2:255), and the three Quls recited morning and evening for complete divine protection.",
      chapter1Ur = "سید الاستغفار (صحیح بخاری 6306)، آیت الکرسی اور معوذات — صبح و شام کے مسنون اذکار۔",
      chapter2Title = "Supplications for Relief from Anxiety, Sorrow & Hardship",
      chapter2Ar = "لاَ إِلَهَ إِلاَّ اللَّهُ الْعَظِيمُ الْحَلِيمُ، لاَ إِلَهَ إِلاَّ اللَّهُ رَبُّ الْعَرْشِ الْعَظِيمِ",
      chapter2Tr = "La ilaha illallahul-'Azimul-Halim, la ilaha illallahu Rabbul-'Arshil-'Azim.",
      chapter2En = "The Prophetic supplication at times of distress (Bukhari 6346, Muslim 2730) and the prayer of Yunus (AS): La ilaha illa Anta Subhanaka inni kuntu minaz-zalimin (21:87).",
      chapter2Ur = "کرب اور پریشانی کے وقت کی مسنون دعائیں (بخاری 6346) اور دعائے یونس علیہ السلام (21:87)۔",
      refs = "Quran 2:255, 21:87, 112–114 • Bukhari 6306, 6346"
    ),
    createBook(
      id = "book_adhkar_nawawi",
      number = 31,
      titleEnglish = "Kitab al-Adhkar (The Book of Remembrances)",
      titleArabic = "الأذكار المنتخبة من كلام سيد الأبرار للنووي",
      titleUrdu = "کتاب الاذکار (امام نوویؒ)",
      author = "Imam Abu Zakariyya Yahya ibn Sharaf an-Nawawi",
      eraHijri = "631–676 AH",
      category = "Adhkar & Tazkiyah",
      numberingSystem = "19 Books / 1,245 Narrations with Grading",
      summary = "Imam an-Nawawi's classical treasury of Prophetic remembrances, etiquette of Qur'an recitation (At-Tibyan), guarding the tongue, and sincere supplication.",
      chapter1Title = "Etiquette of Dhikr & Carrying the Noble Qur'an",
      chapter1Ar = "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
      chapter1Tr = "Fadhkuruni adhkurkum washkuru Li wa la takfurun.",
      chapter1En = "Opens with Surah Al-Baqarah 2:152 ('Remember Me; I will remember you'), explaining that remembrance of Allah is the polish of the heart.",
      chapter1Ur = "سورۃ البقرہ 2:152 (فَاذْكُرُونِي أَذْكُرْكُمْ) کی روشنی میں ذکرِ الٰہی کے آداب اور فضائل۔",
      chapter2Title = "Guarding the Tongue (Hifz al-Lisan) & Comprehensive Quranic Du'as",
      chapter2Ar = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
      chapter2Tr = "Rabbana atina fid-dunya hasanatan wa fil-akhirati hasanatan wa qina 'adhaban-Nar.",
      chapter2En = "Highlights the most frequent supplication of the Prophet (ﷺ) from Surah Al-Baqarah 2:201 (Bukhari 4522, Muslim 2690).",
      chapter2Ur = "نبی اکرم ﷺ کی سب سے کثرت سے پڑھی جانے والی قرآنی دعا: رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً (2:201)۔",
      refs = "Quran 2:152, 2:201, 33:41 • Bukhari 4522, Muslim 2690"
    ),
    createBook(
      id = "book_minhaj_wabil",
      number = 32,
      titleEnglish = "Mukhtasar Minhaj al-Qasidin & Al-Wabil as-Sayyib",
      titleArabic = "مختصر منهاج القاصدين والوابل الصيب",
      titleUrdu = "مختصر منہاج القاصدین و الوابل الصیب",
      author = "Imam Ibn Qudamah al-Maqdisi (d. 689 AH) & Imam Ibn Qayyim al-Jawziyyah (d. 751 AH)",
      eraHijri = "7th–8th Century AH",
      category = "Adhkar & Tazkiyah",
      numberingSystem = "4 Quarters (Rub') & 70+ Benefits of Dhikr",
      summary = "Purified synthesis of spiritual refinement (Tazkiyah), curing spiritual diseases of pride and envy, and the 70+ spiritual fruits of remembering Allah.",
      chapter1Title = "Purification of the Soul: Patience, Gratitude, Hope & Reverent Fear",
      chapter1Ar = "قَدْ أَفْلَحَ مَن زَكَّاهَا * وَقَدْ خَابَ مَن دَسَّاهَا",
      chapter1Tr = "Qad aflaha man zakkaha, wa qad khaba man dassaha.",
      chapter1En = "Explains how the believer purifies the heart through sincere obedience, gratitude in ease, and patience in trial (Surah Ash-Shams 91:9-10).",
      chapter1Ur = "سورۃ الشمس (91:9-10) کی روشنی میں تزکیۂ نفس، صبر و شکر، اخلاص اور ریاکاری سے بچاؤ کے عملی طریقے۔",
      chapter2Title = "Al-Wabil as-Sayyib — The Seventy Fruits of Remembering Allah",
      chapter2Ar = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
      chapter2Tr = "Ala bi-dhikrillahi tatma'innul-qulub.",
      chapter2En = "Details over seventy spiritual benefits of Dhikr: it pleases the Most Merciful, drives away Satan, brings tranquility to the heart (13:28), and illuminates the face and soul.",
      chapter2Ur = "ذکرِ الٰہی کے ستر سے زائد فوائد: یہ رحمٰن کی رضا کا سبب ہے اور اسی سے دلوں کو حقیقی اطمینان نصیب ہوتا ہے (الرعد 13:28)۔",
      refs = "Quran 91:9-10, 13:28, 29:45"
    )
  )

  val shiaClassicalBooks: List<IslamicClassicalBook> = listOf(
    createBook(
      id = "book_shia_alkafi",
      number = 33,
      titleEnglish = "Kitab al-Kafi (Usul, Furu' & Rawdat al-Kafi)",
      titleArabic = "الكافي للشيخ الكليني",
      titleUrdu = "اصول، فروع اور روضہ الکافی",
      author = "Shaykh Abu Ja'far Muhammad ibn Ya'qub al-Kulayni",
      eraHijri = "d. 329 AH",
      category = "Shia Four Books (Al-Kutub al-Arba'ah)",
      numberingSystem = "Dar al-Kutub al-Islamiyyah (8 Volumes / 16,199 Narrations)",
      summary = "The foremost of the Four Canonical Books of Twelver Shia Hadith, encompassing Usul al-Kafi (Theology, Intellect & Ethics), Furu' al-Kafi (Jurisprudence), and Rawdat al-Kafi.",
      chapter1Title = "Kitab al-'Aql wal-Jahl & Kitab al-Tawhid — Intellect and Divine Unity",
      chapter1Ar = "لَمَّا خَلَقَ اللَّهُ الْعَقْلَ اسْتَنْطَقَهُ... مَا عُبِدَ اللَّهُ بِشَيْءٍ أَفْضَلَ مِنَ الْعَقْلِ",
      chapter1Tr = "Lamma khalaqallahul-'aqla istantaqahu... Ma 'ubidallahu bi-shay'in afdala minal-'aql.",
      chapter1En = "Opens with the Book of Intellect and Knowledge (Vol. 1, #1) and the Book of Divine Unity, evaluated through Imami rijal scholarship in Mir'at al-'Uqul by 'Allamah al-Majlisi.",
      chapter1Ur = "کتاب العقل و الجہل اور کتاب التوحید میں عقل کی فضیلت اور توحیدِ خالص کے بنیادی اصول علامہ مجلسیؒ کی مرآۃ العقول کی تحقیق کے ساتھ۔",
      chapter2Title = "Kitab al-Iman wal-Kufr & Kitab al-Du'a — Noble Character and Supplication",
      chapter2Ar = "الدُّعَاءُ سِلَاحُ الْمُؤْمِنِ، وَعَمُودُ الدِّينِ، وَنُورُ السَّمَاوَاتِ وَالْأَرْضِ",
      chapter2Tr = "Ad-du'a'u silahul-mu'min, wa 'amudud-din, wa nurus-samawati wal-ard.",
      chapter2En = "Details the moral virtues of the believer, filial piety, and supplication across Volumes 2–8.",
      chapter2Ur = "مومن کے اخلاقِ حسنہ، والدین کے ساتھ نیکی اور دعا و مناجات کے جامع ابواب۔",
      refs = "Quran 39:9, 42:11, 40:60 • Al-Kafi Vol. 1 #1, Vol. 2 #15",
      tradition = "SHIA",
      officialSourceUrl = "https://www.thaqalayn.net/book/1",
      apiOrRepoUrl = "https://github.com/ThaqalaynAPI/thaqalayn-api",
      sourceTypeLabel = "Collection, REST API & Open Repository",
      gradingMethodology = "Twelver Imami Rijal Grading ('Allamah al-Majlisi — Mir'at al-'Uqul & Shaykh Asif al-Muhsini)"
    ),
    createBook(
      id = "book_shia_faqih",
      number = 34,
      titleEnglish = "Man La Yahduruhu al-Faqih (Shaykh al-Saduq)",
      titleArabic = "من لا يحضره الفقيه للشيخ الصدوق",
      titleUrdu = "من لا یحضرہ الفقیہ (شیخ صدوقؒ)",
      author = "Shaykh Abu Ja'far Muhammad ibn 'Ali Ibn Babawayh al-Saduq",
      eraHijri = "306–381 AH",
      category = "Shia Four Books (Al-Kutub al-Arba'ah)",
      numberingSystem = "Mu'assasat al-A'lami / Qom Standard Edition (4 Volumes / 5,998 Narrations)",
      summary = "The second of the Four Canonical Shia Hadith Books, compiled by Shaykh al-Saduq as a comprehensive legal reference of 5,998 traditions on worship, transactions, and ethics.",
      chapter1Title = "Volume 1: Kitab al-Taharah & Kitab al-Salah — Ritual Purity and Prayer",
      chapter1Ar = "مِفْتَاحُ الصَّلَاةِ الطُّهُورُ",
      chapter1Tr = "Miftahus-salatit-tuhur.",
      chapter1En = "Establishes the legal traditions of Wudu, Ghusl, Adhan, and daily prayers according to the school of Ahl al-Bayt (AS).",
      chapter1Ur = "فقہِ جعفریہ کے مطابق طہارت، وضو، غسل، اذان اور نماز کے مستند احکام و روایات۔",
      chapter2Title = "Volume 4: Wasiyyat al-Nabi (ﷺ) & Ethical Maxims",
      chapter2Ar = "مَنْ أَصْبَحَ لَا يَهْتَمُّ بِأُمُورِ الْمُسْلِمِينَ فَلَيْسَ بِمُسْلِمٍ",
      chapter2Tr = "Man asbaha la yahtammu bi-umuril-Muslimina falaysa bi-Muslim.",
      chapter2En = "Preserves the comprehensive ethical counsel of the Prophet (ﷺ) to Imam 'Ali (AS) and social responsibilities toward fellow believers.",
      chapter2Ur = "رسول اکرم ﷺ کی حضرت علی علیہ السلام کو جامع اخلاقی وصیت اور مسلمانوں کے باہمی حقوق۔",
      refs = "Quran 5:6, 2:43 • Man La Yahduruhu al-Faqih Vol. 1 #1, Vol. 4 #5762",
      tradition = "SHIA",
      officialSourceUrl = "https://www.thaqalayn.net/book/10",
      apiOrRepoUrl = "https://github.com/ThaqalaynAPI/thaqalayn-api",
      sourceTypeLabel = "Collection, REST API & Open Repository",
      gradingMethodology = "Shaykh al-Saduq Authentication & Rawdat al-Muttaqin (Allama Muhammad Taqi al-Majlisi)"
    ),
    createBook(
      id = "book_shia_tahdhib_istibsar",
      number = 35,
      titleEnglish = "Tahdhib al-Ahkam & Al-Istibsar (Shaykh al-Ta'ifah al-Tusi)",
      titleArabic = "تهذيب الأحكام والاستبصار للشيخ الطوسي",
      titleUrdu = "تہذیب الاحکام اور الاستبصار (شیخ طوسیؒ)",
      author = "Shaykh al-Ta'ifah Abu Ja'far Muhammad ibn al-Hasan al-Tusi",
      eraHijri = "385–460 AH",
      category = "Shia Four Books (Al-Kutub al-Arba'ah)",
      numberingSystem = "Dar al-Kutub al-Islamiyyah (10 Vols / 13,590 Hadiths & 4 Vols / 5,511 Hadiths)",
      summary = "The third and fourth of the Four Canonical Books of Shia Hadith, systematically analyzing legal traditions and reconciling apparently conflicting reports through Usuli methodology.",
      chapter1Title = "Tahdhib al-Ahkam: Exposition of Legal Traditions",
      chapter1Ar = "لَا صَلَاةَ إِلَّا بِطَهُورٍ",
      chapter1Tr = "La salata illa bi-tuhur.",
      chapter1En = "Organizes 13,590 legal narrations across all chapters of jurisprudence from ritual purity to judicial procedure.",
      chapter1Ur = "تہذیب الاحکام میں طہارت سے لے کر قضا و حدود تک تیرہ ہزار سے زائد فقہی احادیث کی تحقیق۔",
      chapter2Title = "Al-Istibsar: Methodology of Reconciling Hadith Reports",
      chapter2Ar = "الْمَاءُ يَطْهُرُ وَلَا يُطَهَّرُ",
      chapter2Tr = "Al-ma'u yathuru wa la yutahhar.",
      chapter2En = "Demonstrates how Shaykh al-Tusi examines chains of transmission (isnad) and contextual indicators (qara'in) across 5,511 reports.",
      chapter2Ur = "الاستبصار میں مختلف الروایات احادیث کی سند اور دلالت کے اعتبار سے تطبیق اور اصولی تحقیق۔",
      refs = "Quran 5:6, 25:48 • Tahdhib Vol. 1 #1, Al-Istibsar Vol. 1 #1",
      tradition = "SHIA",
      officialSourceUrl = "https://shiaonlinelibrary.com",
      apiOrRepoUrl = "https://www.al-islam.org",
      sourceTypeLabel = "Classical Book Repository & Scholarly Reference",
      gradingMethodology = "Shaykh al-Ta'ifah al-Tusi Usuli & Rijal Verification (Maladh al-Akhyar)"
    ),
    createBook(
      id = "book_shia_nahjul_balagha",
      number = 36,
      titleEnglish = "Nahj al-Balagha (Peak of Eloquence — Sermons, Letters & Sayings)",
      titleArabic = "نهج البلاغة للشريف الرضي",
      titleUrdu = "نہج البلاغہ (خطبات، مکتوبات و کلماتِ قصار)",
      author = "Compiled by al-Sharif al-Radi (d. 406 AH) from Amir al-Mu'minin 'Ali ibn Abi Talib (AS)",
      eraHijri = "Compiled 400 AH (d. 406 AH)",
      category = "Shia Classical Heritage",
      numberingSystem = "Subhi al-Salih Critical Edition (241 Sermons, 79 Letters, 480 Sayings)",
      summary = "The celebrated compendium of sermons, epistles, and aphorisms attributed to Imam 'Ali ibn Abi Talib (AS), renowned for its theology, governance ethics, and Arabic eloquence.",
      chapter1Title = "Part 1: Al-Khutab — Sermons on Divine Unity and Piety (Khutbat al-Muttaqin)",
      chapter1Ar = "الْحَمْدُ لِلَّهِ الَّذِي لَا يُدْرِكُهُ بُعْدُ الْهِمَمِ وَلَا يَنَالُهُ غَوْصُ الْفِطَنِ",
      chapter1Tr = "Al-hamdu lillahil-ladhi la yudrikuhu bu'dul-himam wa la yanaluhu ghawsul-fitan.",
      chapter1En = "Includes Sermon 1 on Creation and Transcendence and Sermon 193 (Hammam) describing the God-conscious believers.",
      chapter1Ur = "خطبہ اولیٰ میں توحید و آفرینشِ کائنات اور خطبہ متقین (193) میں اہلِ تقویٰ کے اوصاف کا بیان۔",
      chapter2Title = "Part 2 & 3: Letter 53 to Malik al-Ashtar & Aphorisms of Wisdom (Hikam)",
      chapter2Ar = "فَإِنَّهُمْ صِنْفَانِ: إِمَّا أَخٌ لَكَ فِي الدِّينِ، وَإِمَّا نَظِيرٌ لَكَ فِي الْخَلْقِ",
      chapter2Tr = "Fa-innahum sinfani: imma akhun laka fid-din, wa imma nazirun laka fil-khalq.",
      chapter2En = "Presents the historic constitutional charter of justice and mercy to Malik al-Ashtar (Letter 53) and 480 concise sayings of wisdom.",
      chapter2Ur = "مالک اشتر کے نام عہد نامۂ حکومت و عدل (مکتوب 53) اور چار سو اسی حکیمانہ کلماتِ قصار۔",
      refs = "Quran 16:90, 49:13 • Nahj al-Balagha Sermon 1, Letter 53, Hikmah 1",
      tradition = "SHIA",
      officialSourceUrl = "https://www.al-islam.org/nahjul-balagha-part-1-sermons",
      apiOrRepoUrl = "https://www.al-islam.org/nahjul-balagha-part-2-letters-and-sayings",
      sourceTypeLabel = "Book & Digital Library",
      gradingMethodology = "Al-Sharif al-Radi Compilation & Classical Commentaries (Ibn Abi al-Hadid / Ibn Maytham al-Bahrani)"
    ),
    createBook(
      id = "book_shia_sahifa_sajjadiyya",
      number = 37,
      titleEnglish = "Al-Sahifa al-Kamilah al-Sajjadiyya (Psalms of Islam)",
      titleArabic = "الصحيفة الكاملة السجادية",
      titleUrdu = "صحیفہ کاملہ سجادیہ (امام زین العابدین علیہ السلام)",
      author = "Imam 'Ali ibn al-Husayn Zayn al-'Abidin (AS)",
      eraHijri = "38–95 AH",
      category = "Shia Classical Heritage",
      numberingSystem = "54 Canonical Supplications & 15 Whispered Prayers (Munajat)",
      summary = "Transmitted through mutawatir Imami chains from Imam Zayn al-'Abidin (AS), uniting devotional supplication with theology, moral self-accounting, and social ethics.",
      chapter1Title = "Supplications 1–20: Praise of Allah & Du'a Makarim al-Akhlaq",
      chapter1Ar = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَآلِهِ، وَبَلِّغْ بِإِيمَانِي أَكْمَلَ الْإِيمَانِ",
      chapter1Tr = "Allahumma salli 'ala Muhammadin wa Alihi, wa balligh bi-imani akmalal-iman.",
      chapter1En = "Features Du'a 1 (Praise of Allah) and Du'a 20 (Makarim al-Akhlaq — Noble Moral Traits), translated into English by Prof. William Chittick and into Urdu by classical scholars.",
      chapter1Ur = "دعائے اول (حمدِ الٰہی) اور دعائے مکارم الاخلاق (دعا نمبر 20) اردو اور انگریزی ترجمے کے ساتھ۔",
      chapter2Title = "Supplications 24–54: Parents, Neighbors, Repentance & Munajat",
      chapter2Ar = "اللَّهُمَّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا",
      chapter2Tr = "Allahummarhamhuma kama rabbayani saghira.",
      chapter2En = "Includes Du'a 24 for Parents, Du'a 26 for Neighbors, Du'a 31 for Repentance, and the Fifteen Whispered Prayers (Al-Munajat al-Khams 'Asharah).",
      chapter2Ur = "والدین کے حق میں دعا (24)، ہمسایوں کے حقوق کی دعا (26)، دعائے توبہ (31) اور مناجاتِ خمس عشرہ۔",
      refs = "Quran 17:24, 33:56, 40:60 • Al-Sahifa al-Sajjadiyya Du'a 1, 20, 24, 31",
      tradition = "SHIA",
      officialSourceUrl = "https://www.al-islam.org/sahifa-al-kamilah-al-sajjadiyya-imam-ali-zayn-al-abidin",
      apiOrRepoUrl = "https://www.al-islam.org",
      sourceTypeLabel = "Book & Digital Library",
      gradingMethodology = "Mutawatir Imami Devotional Transmission (Riyad al-Salikin Commentary)"
    ),
    createBook(
      id = "book_shia_almizan_majma",
      number = 38,
      titleEnglish = "Al-Mizan fi Tafsir al-Qur'an & Majma' al-Bayan",
      titleArabic = "الميزان في تفسير القرآن ومجمع البيان لعلوم القرآن",
      titleUrdu = "تفسیر المیزان (علامہ طباطبائیؒ) و مجمع البیان (شیخ طبرسیؒ)",
      author = "'Allamah Muhammad Husayn Tabataba'i (d. 1402 AH) & Shaykh al-Tabarsi (d. 548 AH)",
      eraHijri = "6th & 14th Century AH",
      category = "Tafsir & Exegesis",
      numberingSystem = "Surah 1–114 (20-Vol Arabic / 40-Vol English Al-Mizan & 10-Vol Majma' al-Bayan)",
      summary = "Pairs the premier classical Shia linguistic and exegetical encyclopedia (Majma' al-Bayan) with the monumental Quran-by-Quran commentary (Tafsir al-Mizan).",
      chapter1Title = "Methodology of Interpreting the Qur'an by the Qur'an (Tafsir al-Qur'an bil-Qur'an)",
      chapter1Ar = "إِنَّ هَٰذَا الْقُرْآنَ يَهْدِي لِلَّتِي هِيَ أَقْوَمُ",
      chapter1Tr = "Inna hadhal-Qur'ana yahdi lillati hiya aqwam.",
      chapter1En = "'Allamah Tabataba'i demonstrates how verses of the Qur'an explain one another, followed by separate 'Discussions on Traditions' (Bahth Riwa'i) evaluating Hadith reports.",
      chapter1Ur = "علامہ طباطبائیؒ کے نزدیک قرآن کی تفسیر خود قرآن کی دیگر آیات سے ہوتی ہے اور اس کے بعد ’بحث روائی‘ کے تحت احادیث کا جائزہ لیا جاتا ہے۔",
      chapter2Title = "Majma' al-Bayan: Qira'at, Lexical Precision, Meaning & Exegesis",
      chapter2Ar = "كِتَابٌ أَنزَلْنَاهُ إِلَيْكَ مُبَارَكٌ لِّيَدَّبَّرُوا آيَاتِهِ",
      chapter2Tr = "Kitabun anzalnahu ilayka mubarakun li-yaddabbaru ayatih.",
      chapter2En = "Shaykh al-Tabarsi structures every passage into Recitation (Qira'ah), Language (Lughah), Grammar (I'rab), Occasion of Revelation (Nuzul), and Meaning (Ma'na).",
      chapter2Ur = "شیخ امین الاسلام طبرسیؒ ہر آیت کے تحت قراءت، لغت، اعراب، شانِ نزول اور معنی و تفسیر کو منظم انداز میں بیان فرماتے ہیں۔",
      refs = "Quran 17:9, 38:29 • Integrated in Shia Quran Reader Tafsir Selector",
      tradition = "SHIA",
      officialSourceUrl = "https://almizan.org/",
      apiOrRepoUrl = "https://quran.al-islam.org/",
      sourceTypeLabel = "Book, Tafsir Portal & Scholarly Reference",
      gradingMethodology = "Imami Exegetical & Rijal Verification ('Allamah Tabataba'i & Shaykh al-Tabarsi)"
    )
  )

  val sunniHistoryBooks: List<IslamicClassicalBook> = listOf(
    createBook(
      id = "book_history_tabari",
      number = 39,
      titleEnglish = "Tarikh al-Tabari (History of the Prophets and Kings)",
      titleArabic = "تاريخ الرسل والملوك (تاريخ الطبري)",
      titleUrdu = "تاریخ الرسل والملوک (تاریخ طبری)",
      author = "Imam Abu Ja'far Muhammad ibn Jarir al-Tabari",
      eraHijri = "224–310 AH",
      category = "Seerah & History",
      numberingSystem = "Dar al-Ma'arif Critical Edition (11 Volumes • Chronological AH)",
      summary = "The foundational universal chronicle of Islamic history by Imam al-Tabari, documenting the Prophetic Maghazi (Badr, Uhud, Khandaq, Khaybar), the Rightly Guided Caliphate, the Battle of Yarmouk (15 AH), the Battle of al-Qadisiyyah (14–15 AH), the Covenant of Jerusalem (16 AH), Karbala (61 AH), and Waqia Harrah (63 AH) with full chains of transmission.",
      chapter1Title = "Battle of al-Qadisiyyah (14–15 AH) & Rib'i ibn 'Amir's Embassy to Rustam",
      chapter1Ar = "إِنَّ اللَّهَ ابْتَعَثَنَا لِنُخْرِجَ مَنْ شَاءَ مِنْ عِبَادَةِ الْعِبَادِ إِلَى عِبَادَةِ اللَّهِ، وَمِنْ ضِيقِ الدُّنْيَا إِلَى سَعَتِهَا، وَمِنْ جَوْرِ الْأَدْيَانِ إِلَى عَدْلِ الْإِسْلَامِ",
      chapter1Tr = "Innallaha ibta'athana li-nukhrija man sha'a min 'ibadatil-'ibadi ila 'ibadatillahi, wa min diqid-dunya ila sa'atiha, wa min jawril-adyani ila 'adlil-Islam.",
      chapter1En = "Imam al-Tabari (Tarikh Vol. 3, p. 520) records the four-day Battle of al-Qadisiyyah under Sa'd ibn Abi Waqqas (RA) during the Caliphate of 'Umar ibn al-Khattab (RA), and Rib'i ibn 'Amir's immortal proclamation before Rustam: 'Allah has sent us to bring forth whomever He wills from the servitude of human beings to the worship of Allah alone, from the narrowness of this world to its vastness, and from the tyranny of religions to the justice of Islam.'",
      chapter1Ur = "امام ابن جریر طبری نے ’تاریخ طبری‘ (جلد 3 ص 520) میں معرکہ قادسیہ (14–15 ہجری) کی مکمل تفصیل حضرت سعد بن ابی وقاص رضی اللہ عنہ کی سپہ سالاری اور حضرت ربعی بن عامر رضی اللہ عنہ کے رستم کے دربار میں تاریخی خطبے کے ساتھ قلمبند فرمائی ہے۔",
      chapter2Title = "Battle of Yarmouk (15 AH) & Waqia Harrah in Madinah (63 AH)",
      chapter2Ar = "إِنَّ هَذَا يَوْمٌ مِنْ أَيَّامِ اللَّهِ، لَا يَنْبَغِي فِيهِ الْفَخْرُ وَلَا الْبَغْيُ، أَخْلِصُوا جِهَادَكُمْ وَأَرِيدُوا اللَّهَ بِعَمَلِكُمْ",
      chapter2Tr = "Inna hadha yawmun min ayyamillahi, la yanbaghi fihil-fakhru wa lal-baghy, akhlisu jihadakum wa aridullaha bi-'amalikum.",
      chapter2En = "Chronicles the decisive six-day Battle of Yarmouk in Bilad al-Sham (Rajab 15 AH / 636 CE) under Abu 'Ubaydah ibn al-Jarrah and Khalid ibn al-Walid (RA) (Vol. 3, p. 395), as well as the tragedy of Karbala (61 AH, Vol. 5, p. 400) and the Battle of al-Harrah / Waqia Hurrah in Madinah (63 AH, Vol. 5, p. 482).",
      chapter2Ur = "تاریخ طبری (جلد 3 ص 395) میں معرکہ یرموک (15 ہجری)، فتح بیت المقدس (16 ہجری)، سانحۂ کربلا (61 ہجری، جلد 5 ص 400) اور واقعہ حرہ مدینہ منورہ (63 ہجری، جلد 5 ص 482) کی مستند روایات درج ہیں۔",
      refs = "Tarikh al-Tabari 2:449, 3:395, 3:520, 3:609, 5:400, 5:482 • Quran 3:123, 30:2-4",
      tradition = "SUNNI",
      officialSourceUrl = "https://shamela.ws/book/9783",
      apiOrRepoUrl = "https://sunnah.com",
      sourceTypeLabel = "Primary Classical Historical Chronicle",
      gradingMethodology = "Isnad-Based Historical Transmission (Imam Ibn Jarir al-Tabari)"
    ),
    createBook(
      id = "book_history_ibn_kathir",
      number = 40,
      titleEnglish = "Al-Bidayah wa'l-Nihayah (The Beginning and the End)",
      titleArabic = "البداية والنهاية للحافظ ابن كثير",
      titleUrdu = "البدایہ والنہایہ (تاریخ ابن کثیر)",
      author = "Hafiz 'Imad al-Din Isma'il ibn 'Umar Ibn Kathir al-Dimashqi",
      eraHijri = "701–774 AH",
      category = "Seerah & History",
      numberingSystem = "Dar Hajr / Al-Turki Critical Edition (21 Volumes)",
      summary = "Hafiz Ibn Kathir's monumental Hadith-verified encyclopedia of Islamic history from Creation and the Prophets through the Prophetic Seerah (Badr, Uhud, Khandaq, Khaybar), the Rightly Guided Caliphs, Battle of Yarmouk, Battle of al-Qadisiyyah, Karbala, and Waqia Harrah.",
      chapter1Title = "Waqia Harrah — Battle of al-Harrah in Madinah (63 AH) & Prophetic Warning",
      chapter1Ar = "مَنْ أَخَافَ أَهْلَ الْمَدِينَةِ ظُلْمًا أَخَافَهُ اللَّهُ، وَعَلَيْهِ لَعْنَةُ اللَّهِ وَالْمَلَائِكَةِ وَالنَّاسِ أَجْمَعِينَ",
      chapter1Tr = "Man akhafa ahlal-Madinati zulman akhafahullahu, wa 'alayhi la'natullahi wal-mala'ikati wan-nasi ajma'in.",
      chapter1En = "In Al-Bidayah wa'l-Nihayah (Vol. 8, pp. 217–224), Hafiz Ibn Kathir documents Waqia Harrah (Battle of al-Harrah, Dhu al-Hijjah 63 AH / 683 CE) when Muslim ibn 'Uqbah assaulted Madinah, martyring 'Abdullah ibn Hanzalah (RA) and scores of Companions and Ansar, and cites Sahih Muslim 1362: 'Whoever unjustly terrifies the people of Madinah, Allah will terrify him.'",
      chapter1Ur = "حافظ ابن کثیر نے ’البدایہ والنہایہ‘ (جلد 8 ص 217 تا 224) میں واقعہ حرہ (63 ہجری) کا تفصیلی اور تحقیقی ذکر فرمایا ہے جس میں حضرت عبداللہ بن حنظلہ رضی اللہ عنہ اور صحابہ و انصار کی شہادت ہوئی، اور صحیح مسلم (1362) کی حدیث سے اہلِ مدینہ پر ظلم کرنے والوں کے خلاف وعید نقل کی ہے۔",
      chapter2Title = "Battle of Yarmouk (15 AH), Battle of al-Qadisiyyah (15 AH) & Martyrdom of al-Husayn (61 AH)",
      chapter2Ar = "الْحَسَنُ وَالْحُسَيْنُ سَيِّدَا شَبَابِ أَهْلِ الْجَنَّةِ",
      chapter2Tr = "Al-Hasanu wal-Husaynu sayyida shababi ahlil-Jannah.",
      chapter2En = "Presents Hadith-authenticated accounts of the Battle of Yarmouk (Vol. 7, p. 8), the Battle of al-Qadisiyyah (Vol. 7, p. 38), and the martyrdom of Imam al-Husayn ibn 'Ali (RA) at Karbala (Vol. 8, p. 202), stating: 'Every Muslim ought to grieve over his martyrdom, for he is among the foremost masters of the Muslims.'",
      chapter2Ur = "البدایہ والنہایہ کی جلد 7 میں معرکہ یرموک اور معرکہ قادسیہ کی تفصیلات اور جلد 8 ص 202 میں نواسۂ رسول سیدنا امام حسین رضی اللہ عنہ کی شہادتِ کربلا پر ائمہ اہلسنت کا متفقہ موقف درج ہے۔",
      refs = "Al-Bidaya wa'l-Nihaya 3:224, 4:14, 7:8, 7:38, 8:202, 8:217 • Sahih Muslim 1362",
      tradition = "SUNNI",
      officialSourceUrl = "https://shamela.ws/book/4445",
      apiOrRepoUrl = "https://sunnah.com",
      sourceTypeLabel = "Hadith-Verified Historical Encyclopedia",
      gradingMethodology = "Hafiz Ibn Kathir Muhaddithun Historical Criticism"
    ),
    createBook(
      id = "book_history_ibn_hisham",
      number = 41,
      titleEnglish = "Al-Sirah al-Nabawiyyah (Sirah Ibn Hisham & Ibn Ishaq)",
      titleArabic = "السيرة النبوية لابن هشام عن ابن إسحاق",
      titleUrdu = "سیرت ابن ہشام و سیرت ابن اسحاق",
      author = "Imam Abu Muhammad 'Abd al-Malik Ibn Hisham (from Muhammad ibn Ishaq)",
      eraHijri = "d. 218 AH (Ibn Ishaq d. 151 AH)",
      category = "Seerah & History",
      numberingSystem = "Mustafa al-Saqqa Edition (4 Volumes • Prophetic Biography & Maghazi)",
      summary = "The earliest extant comprehensive Prophetic biography and record of the Maghazi: the Hijrah, the Constitution of Madinah (Sahifat al-Madinah, 1 AH), the Great Battle of Badr (2 AH), Battle of Uhud (3 AH), Battle of the Trench / Khandaq (5 AH), Treaty of Hudaybiyyah (6 AH), Khaybar (7 AH), and Conquest of Makkah (8 AH).",
      chapter1Title = "The Hijrah & The Constitution of Madinah (Sahifat al-Madinah, 1 AH)",
      chapter1Ar = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ، هَذَا كِتَابٌ مِنْ مُحَمَّدٍ النَّبِيِّ بَيْنَ الْمُؤْمِنِينَ وَالْمُسْلِمِينَ مِنْ قُرَيْشٍ وَيَثْرِبَ... أَنَّهُمْ أُمَّةٌ وَاحِدَةٌ مِنْ دُونِ النَّاسِ",
      chapter1Tr = "Bismillahir-Rahmanir-Rahim. Hadha kitabun min Muhammadin an-Nabiyyi baynal-mu'minina wal-muslimina min Qurayshin wa Yathrib... annahum ummatun wahidatun min dunin-nas.",
      chapter1En = "Preserves the complete Arabic text of the Constitution of Madinah (Sirah Ibn Hisham Vol. 1, p. 501), establishing the Muhajirun, Ansar, and allied communities of Madinah as a single community (Ummah Wahidah) governed by justice and mutual defense.",
      chapter1Ur = "سیرت ابن ہشام (جلد 1 ص 501) میں میثاقِ مدینہ کا مکمل عربی متن محفوظ ہے جس کے ذریعے نبی کریم ﷺ نے مہاجرین و انصار پر مشتمل ایک امتِ واحدہ اور منصفانہ ریاست کی بنیاد رکھی۔",
      chapter2Title = "Prophetic Maghazi: Badr (2 AH), Uhud (3 AH), Khandaq (5 AH) & Khaybar (7 AH)",
      chapter2Ar = "اللَّهُمَّ أَنْجِزْ لِي مَا وَعَدْتَنِي... اللَّهُمَّ لَا عَيْشَ إِلَّا عَيْشُ الْآخِرَهْ، فَاغْفِرْ لِلْأَنْصَارِ وَالْمُهَاجِرَهْ",
      chapter2Tr = "Allahumma anjiz li ma wa'adtani... Allahumma la 'aysha illa 'ayshul-akhirah, faghfir lil-Ansari wal-Muhajirah.",
      chapter2En = "Details the 313 companions at Badr (Yawm al-Furqan, 17 Ramadan 2 AH), the archers on Jabal al-Rumat at Uhud (3 AH), digging the Trench at Khandaq (5 AH), the Pledge of Ridwan (6 AH), Khaybar (7 AH), and the general amnesty at the Conquest of Makkah (8 AH).",
      chapter2Ur = "غزوہ بدر الکبریٰ (17 رمضان 2 ہجری)، غزوہ احد (3 ہجری)، غزوہ خندق (5 ہجری)، صلح حدیبیہ (6 ہجری)، غزوہ خیبر (7 ہجری) اور فتحِ مکہ (8 ہجری) کی مستند سیرت۔",
      refs = "Sirah Ibn Hisham 1:501, 2:609 • Sahih al-Bukhari 3039, 3953, 4104, 4210",
      tradition = "SUNNI",
      officialSourceUrl = "https://shamela.ws/book/23833",
      apiOrRepoUrl = "https://sunnah.com",
      sourceTypeLabel = "Earliest Prophetic Seerah & Maghazi",
      gradingMethodology = "Classical Seerah & Maghazi Verification (Ibn Ishaq & Ibn Hisham)"
    ),
    createBook(
      id = "book_history_kamil_futuh",
      number = 42,
      titleEnglish = "Al-Kamil fi al-Tarikh & Futuh al-Buldan (Ibn al-Athir & Al-Baladhuri)",
      titleArabic = "الكامل في التاريخ لابن الأثير وفتوح البلدان للبلاذري",
      titleUrdu = "الکامل فی التاریخ (ابن اثیر) و فتوح البلدان (بلاذری)",
      author = "Imam 'Izz al-Din Ibn al-Athir (d. 630 AH) & Imam Ahmad ibn Yahya al-Baladhuri (d. 279 AH)",
      eraHijri = "3rd & 7th Century AH",
      category = "Seerah & History",
      numberingSystem = "Dar Sader Edition (12 Volumes Al-Kamil & Futuh al-Buldan)",
      summary = "Pairs Al-Baladhuri's primary administrative and military history (Futuh al-Buldan) with Ibn al-Athir's analytical chronicle (Al-Kamil fi al-Tarikh), detailing the Battle of Yarmouk, Battle of al-Qadisiyyah, Covenant of 'Umar in Jerusalem, Siffin, Karbala, and Waqia Harrah.",
      chapter1Title = "Battle of Yarmouk (15 AH), Battle of al-Qadisiyyah (15 AH) & Covenant of Jerusalem (16 AH)",
      chapter1Ar = "هَذَا مَا أَعْطَى عَبْدُ اللَّهِ عُمَرُ أَمِيرُ الْمُؤْمِنِينَ أَهْلَ إِيلِيَاءَ مِنَ الْأَمَانِ: أَعْطَاهُمْ أَمَانًا لِأَنْفُسِهِمْ وَأَمْوَالِهِمْ وَلِكَنَائِسِهِمْ",
      chapter1Tr = "Hadha ma a'ta 'Abdullahi 'Umaru Amirul-Mu'minina ahla Iliya'a minal-aman: a'tahum amanan li-anfusihim wa amwalihim wa li-kana'isihim.",
      chapter1En = "Documents the topographical and military details of the Battle of Yarmouk (Futuh al-Buldan p. 141), the four days of al-Qadisiyyah (Al-Kamil Vol. 2, p. 452), and Caliph 'Umar ibn al-Khattab's peaceful entry into Jerusalem and assurance of safety ('Uhdat 'Umar, 16 AH).",
      chapter1Ur = "فتوح البلدان (ص 141) اور الکامل فی التاریخ (جلد 2 ص 452) میں معرکہ یرموک، معرکہ قادسیہ اور عہدہ عمریہ (فتح بیت المقدس، 16 ہجری) کی مکمل تاریخی اسناد۔",
      chapter2Title = "Siyar A'lam al-Nubala' & Classical Sunni Assessment of Karbala and Waqia Harrah",
      chapter2Ar = "وَهُوَ سَيِّدٌ كَبِيرٌ، وَابْنُ بِنْتِ رَسُولِ اللَّهِ ﷺ",
      chapter2Tr = "Wa huwa sayyidun kabirun, wabnu binti Rasulillahi (ﷺ).",
      chapter2En = "Synthesizes the historical assessments of Ibn al-Athir and Imam al-Dhahabi (Siyar A'lam al-Nubala' Vol. 3, pp. 280–322) regarding the martyrdom of Imam al-Husayn (RA) at Karbala (61 AH) and the Battle of al-Harrah / Waqia Hurrah in Madinah (63 AH).",
      chapter2Ur = "علامہ ابن اثیر اور امام ذہبی (سیر اعلام النبلاء جلد 3 ص 280 تا 322) کی تحقیق کے مطابق سانحۂ کربلا (61 ہجری) اور واقعہ حرہ (63 ہجری) کا تاریخی تجزیہ۔",
      refs = "Al-Kamil fi al-Tarikh 2:452 • Futuh al-Buldan p. 141 • Siyar A'lam al-Nubala' 3:280, 3:322",
      tradition = "SUNNI",
      officialSourceUrl = "https://shamela.ws/book/21712",
      apiOrRepoUrl = "https://sunnah.com",
      sourceTypeLabel = "Classical Sunni Historical Chronicles",
      gradingMethodology = "Comparative Historical Synthesis (Ibn al-Athir, Al-Baladhuri & Imam al-Dhahabi)"
    ),
    createBook(
      id = "book_history_dhahabi_siyar",
      number = 46,
      titleEnglish = "Siyar A'lam al-Nubala' & Tarikh al-Islam (Imam al-Dhahabi)",
      titleArabic = "سير أعلام النبلاء وتاريخ الإسلام للإمام شمس الدين الذهبي",
      titleUrdu = "سیر اعلام النبلاء اور تاریخ الاسلام (امام شمس الدین ذہبیؒ)",
      author = "Imam Shams al-Din Abu 'Abdillah Muhammad ibn Ahmad al-Dhahabi",
      eraHijri = "673–748 AH",
      category = "Seerah & History",
      numberingSystem = "Mu'assasat al-Risalah Critical Edition (25 Vols Siyar & 50 Vols Tarikh al-Islam • All Jilds Integrated)",
      summary = "The monumental biographical and historical encyclopedia by Hafiz al-Dhahabi covering the Prophetic Seerah, the Rightly Guided Caliphs, the Companions at Badr, Uhud, Khandaq, Khaybar, Yarmouk (15 AH), al-Qadisiyyah (15 AH), the martyrdom of Imam al-Husayn (RA) at Karbala (61 AH), and Waqia Harrah in Madinah (63 AH).",
      chapter1Title = "Jild 1: The Prophetic Biography, Companions of Badr & Heroes of Yarmouk and al-Qadisiyyah",
      chapter1Ar = "إِنَّ هَذَا يَوْمٌ مِنْ أَيَّامِ اللَّهِ، لَا يَنْبَغِي فِيهِ الْفَخْرُ وَلَا الْبَغْيُ، أَخْلِصُوا جِهَادَكُمْ وَأَرِيدُوا اللَّهَ بِعَمَلِكُمْ",
      chapter1Tr = "Inna hadha yawmun min ayyamillahi, la yanbaghi fihil-fakhru wa lal-baghy, akhlisu jihadakum wa aridullaha bi-'amalikum.",
      chapter1En = "In Siyar A'lam al-Nubala' (Vol. 1) and Tarikh al-Islam, Imam al-Dhahabi documents the biographies and verified historical transmissions of Abu 'Ubaydah ibn al-Jarrah, Sa'd ibn Abi Waqqas, and Khalid ibn al-Walid (RA) at the Battle of Yarmouk (15 AH) and the Battle of al-Qadisiyyah (14–15 AH).",
      chapter1Ur = "امام شمس الدین ذہبی کی ’سیر اعلام النبلاء‘ (جلد اول) اور ’تاریخ الاسلام‘ میں اصحابِ بدر، فاتحِ شام حضرت ابو عبیدہ بن الجراحؓ، حضرت خالد بن ولیدؓ اور فاتحِ قادسیہ حضرت سعد بن ابی وقاصؓ کے مستند حالات و فتوحات درج ہیں۔",
      chapter2Title = "Jild 2: Biography of Imam al-Husayn (RA), Karbala (61 AH) & Waqia Harrah in Madinah (63 AH)",
      chapter2Ar = "الْحَسَنُ وَالْحُسَيْنُ سَيِّدَا شَبَابِ أَهْلِ الْجَنَّةِ... وَقَدْ أُصِيبَ أَهْلُ الْمَدِينَةِ يَوْمَ الْحَرَّةِ مُصِيبَةً عَظِيمَةً",
      chapter2Tr = "Al-Hasanu wal-Husaynu sayyida shababi ahlil-Jannah... Wa qad usiba ahlul-Madinati yawmal-Harrati musibatan 'azimah.",
      chapter2En = "In Siyar A'lam al-Nubala' (Vol. 3, pp. 280–325), Imam al-Dhahabi presents the full biographical chronicle of Imam al-Husayn ibn 'Ali (RA), his martyrdom at Karbala (10 Muharram 61 AH), and the tragedy of Waqia Harrah (63 AH) in Madinah.",
      chapter2Ur = "سیر اعلام النبلاء (جلد سوم، ص 280 تا 325) میں سیدنا امام حسین بن علی رضی اللہ عنہما کی مکمل سوانح، سانحۂ کربلا (61 ہجری) اور مدینہ منورہ میں واقعہ حرہ (63 ہجری) کی تفصیلی تاریخ موجود ہے۔",
      refs = "Siyar A'lam al-Nubala' 1:348, 3:280, 3:322 • Tarikh al-Islam Vols. 2–5",
      tradition = "SUNNI",
      officialSourceUrl = "https://shamela.ws/book/10906",
      apiOrRepoUrl = "https://sunnah.com",
      sourceTypeLabel = "25-Volume Biographical & Historical Encyclopedia",
      gradingMethodology = "Hafiz al-Dhahabi Rijal & Historical Criticism"
    )
  )

  val shiaHistoryBooks: List<IslamicClassicalBook> = listOf(
    createBook(
      id = "book_shia_history_irshad",
      number = 43,
      titleEnglish = "Kitab al-Irshad fi Ma'rifat Hujaj Allah 'ala al-'Ibad (Shaykh al-Mufid)",
      titleArabic = "كتاب الإرشاد في معرفة حجج الله على العباد للشيخ المفيد",
      titleUrdu = "کتاب الارشاد فی معرفۃ حجج اللہ علی العباد (شیخ مفیدؒ)",
      author = "Shaykh al-Mufid Muhammad ibn Muhammad ibn al-Nu'man al-'Ukbari al-Baghdadi",
      eraHijri = "336–413 AH",
      category = "Seerah & History",
      numberingSystem = "Mu'assasat Al al-Bayt Critical Edition (All Jilds • Biographies & Historical Chronicles)",
      summary = "The premier classical Twelver Shia historical and biographical work by Shaykh al-Mufid, chronicling Laylat al-Mabit & Hijrah, Badr, Uhud, Khandaq, Khaybar, Saqifah, Qadisiyyah & Yarmouk counsels, Jamal (36 AH), Siffin (37 AH), Nahrawan (38 AH), the Peace Treaty of Imam al-Hasan (41 AH), the Epic of Karbala (61 AH), and Waqia Harrah (63 AH).",
      chapter1Title = "Jild 1: Laylat al-Mabit (1 AH) & Prophetic Battles: Badr, Uhud, Khandaq & Khaybar",
      chapter1Ar = "وَمِنَ النَّاسِ مَنْ يَشْرِي نَفْسَهُ ابْتِغَاءَ مَرْضَاتِ اللَّهِ... لَا فَتَى إِلَّا عَلِيٌّ، وَلَا سَيْفَ إِلَّا ذُو الْفَقَارِ",
      chapter1Tr = "Wa minan-nasi man yashri nafsahu ibtigha'a mardatillah... La fata illa 'Ali, wa la sayfa illa Dhul-Faqar.",
      chapter1En = "Chronicles Amir al-Mu'minin 'Ali ibn Abi Talib's (AS) sacrifice on the Prophet's bed on the Night of the Hijrah (Laylat al-Mabit, Kitab al-Irshad Vol. 1, p. 51) and his decisive valor at Badr (2 AH), Uhud (3 AH), Khandaq (5 AH), and Khaybar (7 AH) (Vol. 1, pp. 67–135).",
      chapter1Ur = "شیخ مفید کی ’کتاب الارشاد‘ (جلد 1 ص 51 تا 135) میں شبِ ہجرت (لیلۃ المبیت) اور غزواتِ نبوی — بدر، احد، خندق اور خیبر — کے مستند تاریخی واقعات درج ہیں۔",
      chapter2Title = "Jild 2: Epic of Karbala ('Ashura, 61 AH) & Waqia Harrah in Madinah (63 AH)",
      chapter2Ar = "إِنِّي لَمْ أَخْرُجْ أَشِرًا وَلَا بَطِرًا وَلَا مُفْسِدًا وَلَا ظَالِمًا، وَإِنَّمَا خَرَجْتُ لِطَلَبِ الْإِصْلَاحِ فِي أُمَّةِ جَدِّي",
      chapter2Tr = "Inni lam akhruj ashiran wa la batiran wa la mufsidan wa la zaliman, wa innama kharajtu li-talabil-islahi fi ummati jaddi.",
      chapter2En = "Preserves the primary historical narrative of Imam al-Husayn's (AS) uprising for reform and martyrdom at Karbala (Vol. 2, pp. 32–135) and Imam Zayn al-'Abidin's (AS) sheltering of four hundred families of Madinah during Waqia Harrah / Battle of al-Harrah (63 AH, Vol. 2, p. 149).",
      chapter2Ur = "کتاب الارشاد (جلد 2 ص 32 تا 149) میں سید الشہداء حضرت امام حسین علیہ السلام کے قیامِ اصلاح و واقعہ کربلا (61 ہجری) اور واقعہ حرہ (63 ہجری) میں حضرت امام زین العابدین علیہ السلام کی جانب سے اہلِ مدینہ کے چار سو خاندانوں کی کفالت و حفاظت کا بیان ہے۔",
      refs = "Kitab al-Irshad 1:51, 1:67, 1:250, 2:8, 2:32, 2:149 • Quran 2:207, 33:23",
      tradition = "SHIA",
      officialSourceUrl = "https://www.al-islam.org/kitab-al-irshad-shaykh-al-mufid",
      apiOrRepoUrl = "https://shiaonlinelibrary.com",
      sourceTypeLabel = "Primary Imami Historical & Biographical Chronicle",
      gradingMethodology = "Shaykh al-Mufid (d. 413 AH) Classical Imami Historical Verification"
    ),
    createBook(
      id = "book_shia_history_yaqubi_masudi",
      number = 44,
      titleEnglish = "Tarikh al-Ya'qubi & Muruj al-Dhahab (Al-Ya'qubi & Al-Mas'udi)",
      titleArabic = "تاريخ اليعقوبي ومروج الذهب ومعادن الجوهر للمسعودي",
      titleUrdu = "تاریخ یعقوبی اور مروج الذہب (مسعودی)",
      author = "Ahmad ibn Abi Ya'qub al-Ya'qubi (d. 284 AH) & Abu al-Hasan 'Ali ibn al-Husayn al-Mas'udi (d. 346 AH)",
      eraHijri = "3rd–4th Century AH",
      category = "Seerah & History",
      numberingSystem = "Dar Sader Edition (All Jilds • Tarikh al-Ya'qubi & Muruj al-Dhahab)",
      summary = "Two of the earliest surviving world and Islamic histories from the 3rd and 4th centuries AH, recording the Prophetic era, Saqifah Bani Sa'idah (11 AH), the Battle of al-Qadisiyyah and Battle of Yarmouk (14–15 AH), Jamal, Siffin, Nahrawan, Karbala (61 AH), and Waqia Harrah (63 AH).",
      chapter1Title = "Jild 1: Battle of al-Qadisiyyah & Battle of Yarmouk (14–15 AH) & Imam 'Ali's Strategic Counsel",
      chapter1Ar = "إِنَّ هَذَا الْأَمْرَ لَمْ يَكُنْ نَصْرُهُ وَلَا خِذْلَانُهُ بِكَثْرَةٍ وَلَا بِقِلَّةٍ، وَهُوَ دِينُ اللَّهِ الَّذِي أَظْهَرَهُ",
      chapter1Tr = "Inna hadhal-amra lam yakun nasruhu wa la khidhlanuhu bi-kathratin wa la bi-qillah, wa huwa dinulladhi azharah.",
      chapter1En = "In Tarikh al-Ya'qubi (Vol. 2, pp. 139–146) and Nahj al-Balagha (Sermon 146), Imam 'Ali (AS) counsels 'Umar ibn al-Khattab to remain as the central axis in Madinah during the Battle of al-Qadisiyyah, while Hashim ibn 'Utbah al-Mirqal, Malik al-Ashtar, Hujr ibn 'Adi, and 'Adi ibn Hatim lead the vanguards at Yarmouk and al-Qadisiyyah.",
      chapter1Ur = "تاریخ یعقوبی (جلد 2 ص 139 تا 146) اور نہج البلاغہ (خطبہ 146) میں معرکہ قادسیہ اور معرکہ یرموک کے موقع پر حضرت علی علیہ السلام کے تزویراتی مشورے اور ہاشم بن عتبہ المرقال، مالک اشتر نخعی اور حجر بن عدی کندی کی شجاعت کا بیان ہے۔",
      chapter2Title = "Jild 2: Saqifah (11 AH), Treaty of Imam al-Hasan (41 AH) & Waqia Harrah in Madinah (63 AH)",
      chapter2Ar = "فَضَمَّ عَلِيُّ بْنُ الْحُسَيْنِ أَرْبَعَمِائَةِ امْرَأَةٍ وَعِيَالَهُنَّ إِلَى دَارِهِ يُنْفِقُ عَلَيْهِنَّ",
      chapter2Tr = "Fa-damma 'Aliyyu bnu al-Husayni arba'a mi'ati imra'atin wa 'iyalahunna ila darihi yunfiqu 'alayhinna.",
      chapter2En = "Records the events of Saqifah Bani Sa'idah (Tarikh al-Ya'qubi Vol. 2, p. 123), the peace treaty of Imam al-Hasan (Vol. 2, p. 215), and the eyewitness accounts in Tarikh al-Ya'qubi (Vol. 2, p. 250) and Al-Mas'udi's Muruj al-Dhahab (Vol. 3, p. 69) of Waqia Harrah (Battle of al-Harrah, 63 AH) in Madinah.",
      chapter2Ur = "تاریخ یعقوبی (جلد 2 ص 123، 215، 250) اور مسعودی کی ’مروج الذہب‘ (جلد 3 ص 69) میں سقیفہ بنی ساعدہ، صلح امام حسنؑ اور واقعہ حرہ (63 ہجری) کی تاریخی تفصیلات۔",
      refs = "Tarikh al-Ya'qubi 2:38, 2:123, 2:143, 2:215, 2:250 • Muruj al-Dhahab 3:69",
      tradition = "SHIA",
      officialSourceUrl = "https://shiaonlinelibrary.com",
      apiOrRepoUrl = "https://www.al-islam.org",
      sourceTypeLabel = "3rd & 4th Century AH Classical Chronicles",
      gradingMethodology = "Early Historiographical Transmission (Al-Ya'qubi d. 284 AH & Al-Mas'udi d. 346 AH)"
    ),
    createBook(
      id = "book_shia_history_siffin_maqatil",
      number = 45,
      titleEnglish = "Waq'at Siffin & Maqatil al-Talibiyyin (Nasr ibn Muzahim & Al-Isfahani)",
      titleArabic = "وقعة صفين لنصر بن مزاحم ومقاتل الطالبيين لأبي الفرج الأصفهاني",
      titleUrdu = "وقعۃ صفین (نصر بن مزاحم) و مقاتل الطالبین (ابو الفرج اصفہانی)",
      author = "Nasr ibn Muzahim al-Minqari (d. 212 AH) & Abu al-Faraj al-Isfahani (d. 356 AH)",
      eraHijri = "2nd–4th Century AH",
      category = "Seerah & History",
      numberingSystem = "Critical Editions (All Jilds • Waq'at Siffin • Kitab al-Jamal • Maqatil al-Talibiyyin)",
      summary = "Preserves the earliest dedicated historical monographs on the Caliphate of Amir al-Mu'minin 'Ali (AS): the Battle of Jamal (36 AH), the Battle of Siffin and martyrdom of 'Ammar ibn Yasir (37 AH), the Battle of Nahrawan (38 AH), and the historical martyrologies of Banu Hashim.",
      chapter1Title = "Jild 1: Battle of Siffin (37 AH), Liberation of the Euphrates & Martyrdom of 'Ammar ibn Yasir",
      chapter1Ar = "وَيْحَ عَمَّارٍ تَقْتُلُهُ الْفِئَةُ الْبَاغِيَةُ، يَدْعُوهُمْ إِلَى الْجَنَّةِ وَيَدْعُونَهُ إِلَى النَّارِ",
      chapter1Tr = "Wayha 'Ammarin taqtuluhul-fi'atul-baghiyah, yad'uhum ilal-Jannati wa yad'unahu ilan-Nar.",
      chapter1En = "Nasr ibn Muzahim (d. 212 AH) documents in Waq'at Siffin (p. 152) how Imam 'Ali (AS) forbade denying the water of the Euphrates to the Syrian army after Malik al-Ashtar liberated the riverbanks, and records the martyrdom of 'Ammar ibn Yasir (RA/AS) fulfilling the Prophetic Hadith.",
      chapter1Ur = "نصر بن مزاحم منقری (متوفی 212 ہجری) کی ’وقعۃ صفین‘ (ص 152) میں معرکہ صفین (37 ہجری)، دریائے فرات کے پانی کی آزادی اور حضرت عمار بن یاسرؓ کی شہادت کی مکمل تاریخی تفصیل درج ہے۔",
      chapter2Title = "Jild 2: Battle of Jamal (36 AH), Battle of Nahrawan (38 AH) & Ethical Rules of Warfare",
      chapter2Ar = "لَا تَتْبَعُوا مُدْبِرًا، وَلَا تُذَفِّفُوا عَلَى جَرِيحٍ، وَمَنْ أَغْلَقَ بَابَهُ فَهُوَ آمِنٌ",
      chapter2Tr = "La tatba'u mudbiran, wa la tudhaffifu 'ala jarihin, wa man aghlaqa babahu fa-huwa amin.",
      chapter2En = "Records Imam 'Ali's (AS) ethical rules of engagement at Basra (36 AH) and his debate and confrontation with the armed Khawarij insurgents at Nahrawan (38 AH).",
      chapter2Ur = "جنگِ جمل (36 ہجری) میں امیر المومنین حضرت علی علیہ السلام کے اخلاقی قوانینِ جنگ اور معرکہ نہروان (38 ہجری) کا تاریخی بیان۔",
      refs = "Waq'at Siffin p. 152 • Kitab al-Jamal p. 388 • Nahj al-Balagha Sermon 40, Letter 47",
      tradition = "SHIA",
      officialSourceUrl = "https://shiaonlinelibrary.com",
      apiOrRepoUrl = "https://www.al-islam.org",
      sourceTypeLabel = "Earliest Historical Monographs (212 AH & 356 AH)",
      gradingMethodology = "Primary Early Kufan & Baghdadi Historical Chains (Nasr ibn Muzahim & Al-Mufid)"
    ),
    createBook(
      id = "book_shia_history_luhuf_tabarsi",
      number = 47,
      titleEnglish = "Al-Luhuf 'ala Qatla al-Tufuf & A'lam al-Wara (Ibn Tawus & Al-Tabarsi)",
      titleArabic = "اللهوف على قتلى الطفوف لابن طاووس وإعلام الورى بأعلام الهدى للطبرسي",
      titleUrdu = "اللہوف علی قتلی الطفوف (سید ابن طاؤسؒ) اور اعلام الوریٰ باعلام الہدیٰ (شیخ طبرسیؒ)",
      author = "Sayyid Radi al-Din 'Ali ibn Musa Ibn Tawus (d. 664 AH) & Shaykh Amin al-Islam al-Tabarsi (d. 548 AH)",
      eraHijri = "6th–7th Century AH",
      category = "Seerah & History",
      numberingSystem = "Mu'assasat Al al-Bayt Critical Edition (All Jilds • Seerah, Imams & Maqtal al-Husayn)",
      summary = "Combines Shaykh al-Tabarsi's authoritative historical encyclopedia of the Prophet (ﷺ) and the Twelve Imams (A'lam al-Wara bi-A'lam al-Huda) with Sayyid Ibn Tawus's classic three-part historical account of the journey to Karbala, the Day of 'Ashura (61 AH), and the aftermath in Kufah, Damascus, and Madinah including Waqia Harrah (63 AH).",
      chapter1Title = "Jild 1: A'lam al-Wara — Prophetic Maghazi, Badr, Uhud, Khandaq, Khaybar & Ghadir",
      chapter1Ar = "لَا فَتَى إِلَّا عَلِيٌّ، وَلَا سَيْفَ إِلَّا ذُو الْفَقَارِ... فَمَنْ كُنْتُ مَوْلَاهُ فَهَذَا عَلِيٌّ مَوْلَاهُ",
      chapter1Tr = "La fata illa 'Ali, wa la sayfa illa Dhul-Faqar... Faman kuntu mawlahu fahadha 'Aliyyun mawlahu.",
      chapter1En = "In A'lam al-Wara bi-A'lam al-Huda (Vol. 1, pp. 176–265), Shaykh al-Tabarsi documents the Prophetic biography, the Hijrah, the battles of Badr, Uhud, Khandaq, Khaybar, and Hunayn, and the proclamation at Ghadir Khumm (10 AH).",
      chapter1Ur = "شیخ امین الاسلام طبرسی کی ’اعلام الوریٰ باعلام الہدیٰ‘ (جلد اول، ص 176 تا 265) میں سیرتِ نبوی ﷺ، غزوہ بدر، احد، خندق، خیبر، حنین اور غدیرِ خم کے مستند تاریخی واقعات درج ہیں۔",
      chapter2Title = "Jild 2: Al-Luhuf — Complete Three-Maslak Chronicle of Karbala (61 AH) & Madinah (63 AH)",
      chapter2Ar = "إِنِّي لَمْ أَخْرُجْ أَشِرًا وَلَا بَطِرًا وَلَا مُفْسِدًا وَلَا ظَالِمًا، وَإِنَّمَا خَرَجْتُ لِطَلَبِ الْإِصْلَاحِ فِي أُمَّةِ جَدِّي",
      chapter2Tr = "Inni lam akhruj ashiran wa la batiran wa la mufsidan wa la zaliman, wa innama kharajtu li-talabil-islahi fi ummati jaddi.",
      chapter2En = "Sayyid Ibn Tawus organizes Al-Luhuf into Three Maslaks (Parts): (1) Events before 'Ashura from Madinah to Makkah and arrival at Karbala; (2) The Day of 'Ashura (10 Muharram 61 AH) and martyrdom of Banu Hashim and the companions; (3) Post-'Ashura sermons of Lady Zaynab (SA) and Imam Zayn al-'Abidin (AS) in Kufah and Sham, return to Madinah, and protection of Madinah's families during Waqia Harrah (63 AH).",
      chapter2Ur = "سید ابن طاؤس کی ’اللہوف علی قتلی الطفوف‘ تین مسالک (حصوں) پر مشتمل ہے: (1) مدینہ سے مکہ اور کربلا تک کا سفر؛ (2) یومِ عاشورہ (10 محرم 61 ہجری) کے مفصل واقعات و شہادتیں؛ (3) بعد از عاشورہ کوفہ و شام کے خطبات، مدینہ واپسی اور واقعہ حرہ (63 ہجری) کے حالات۔",
      refs = "Al-Luhuf pp. 21–135 • A'lam al-Wara 1:176, 2:149 • Bihar al-Anwar 44:329",
      tradition = "SHIA",
      officialSourceUrl = "https://www.al-islam.org/al-luhuf-sighs-sorrow-sayyid-ibn-tawus",
      apiOrRepoUrl = "https://shiaonlinelibrary.com",
      sourceTypeLabel = "Classical Imami Seerah & Maqtal Chronicles",
      gradingMethodology = "Shaykh al-Tabarsi (d. 548 AH) & Sayyid Ibn Tawus (d. 664 AH) Verification"
    )
  )

  val historySubFilters: List<String> = listOf(
    "All Events",
    "Qadisiyyah",
    "Waqia Hurrah",
    "Battle of Yarmouk",
    "Badr",
    "Uhud",
    "Khandaq",
    "Khaybar",
    "Karbala",
    "Siffin",
    "Nahrawan",
    "Saqifah"
  )

  val allSunniBooks: List<IslamicClassicalBook>
    get() = all32Books + sunniHistoryBooks

  val allShiaBooks: List<IslamicClassicalBook>
    get() = shiaClassicalBooks + shiaHistoryBooks

  val allSunniAndShiaBooks: List<IslamicClassicalBook>
    get() = all32Books + shiaClassicalBooks + sunniHistoryBooks + shiaHistoryBooks

  val historyBooks: List<IslamicClassicalBook>
    get() = sunniHistoryBooks + shiaHistoryBooks + all32Books.filter {
      it.category.equals("Seerah & History", ignoreCase = true)
    }

  fun getBooksByTradition(traditionFilter: String): List<IslamicClassicalBook> {
    return when (traditionFilter.uppercase()) {
      "SUNNI" -> allSunniBooks
      "SHIA" -> allShiaBooks
      else -> allSunniAndShiaBooks
    }
  }

  fun getHistoryBooksByTradition(traditionFilter: String): List<IslamicClassicalBook> {
    return when (traditionFilter.uppercase()) {
      "SUNNI" -> historyBooks.filter { it.tradition.equals("SUNNI", ignoreCase = true) }
      "SHIA" -> historyBooks.filter { it.tradition.equals("SHIA", ignoreCase = true) }
      else -> historyBooks
    }
  }

  data class BookLineMatch(
    val book: IslamicClassicalBook,
    val serialNumber: Int,
    val chapterIndex: Int,
    val chapterNumber: Int,
    val chapterTitle: String,
    val matchedLine: String,
    val matchedLanguage: String,
    val highlightTerm: String
  )

  fun searchInsideBooks(
    booksToSearch: List<IslamicClassicalBook>,
    rawQuery: String
  ): List<BookLineMatch> {
    val sanitized = com.example.data.remote.SecurityIntegrityGuard.sanitizeSearchQuery(rawQuery).trim()
    if (sanitized.isEmpty()) return emptyList()

    val cleanNumQuery = sanitized.removePrefix("#").removePrefix("book").trim().toIntOrNull()
    val normLatinQuery = com.example.data.local.ExtendedHadithDataProvider.normalizeLatinForSearch(sanitized)
    val normArabicQuery = com.example.data.local.ExtendedHadithDataProvider.normalizeArabicForSearch(sanitized)
    val latinTokens = normLatinQuery.split(" ").filter { it.isNotBlank() }
    val arabicTokens = normArabicQuery.split(Regex("\\s+")).filter { it.isNotBlank() }

    val matches = mutableListOf<BookLineMatch>()

    booksToSearch.forEachIndexed { index, book ->
      val serialNum = index + 1
      // (a) Direct Book Number match (by serial number in current list or global book.number)
      if (cleanNumQuery != null && (serialNum == cleanNumQuery || book.number == cleanNumQuery)) {
        val firstCh = book.chapters.firstOrNull()
        if (firstCh != null) {
          matches.add(
            BookLineMatch(
              book = book,
              serialNumber = serialNum,
              chapterIndex = 0,
              chapterNumber = firstCh.chapterNumber,
              chapterTitle = firstCh.titleEnglish,
              matchedLine = "Book #$serialNum (${book.titleEnglish}) — ${book.summary}",
              matchedLanguage = "EN",
              highlightTerm = cleanNumQuery.toString()
            )
          )
        }
      }

      // (b) & (c) Keyword or Full/Partial Sentence search across all chapters (English, Urdu, Arabic)
      book.chapters.forEachIndexed { chIdx, ch ->
        val candidateBlocks = listOf(
          Triple("EN", ch.titleEnglish, ch.titleEnglish),
          Triple("EN", ch.englishContent, ch.englishContent),
          Triple("UR", ch.urduContent, ch.urduContent),
          Triple("AR", ch.arabicExcerpt, ch.arabicExcerpt),
          Triple("AR", ch.titleArabic, ch.titleArabic),
          Triple("EN", ch.transliteration, ch.transliteration),
          Triple("EN", ch.scholarlyNotes, ch.scholarlyNotes),
          Triple("EN", ch.crossReferences, ch.crossReferences)
        )

        var matchedForChapter: BookLineMatch? = null

        for ((lang, _, fullText) in candidateBlocks) {
          if (fullText.isBlank()) continue
          // Split text into sentences/clauses so we highlight the exact matching line
          val sentences = fullText.split(Regex("(?<=[.۔!؟\\n])\\s+|\\n+")).filter { it.isNotBlank() }
          for (sentence in sentences.ifEmpty { listOf(fullText) }) {
            val rawMatch = sentence.contains(sanitized, ignoreCase = true)
            val normArabicMatch = normArabicQuery.isNotBlank() &&
              com.example.data.local.ExtendedHadithDataProvider.normalizeArabicForSearch(sentence).let { normSent ->
                normSent.contains(normArabicQuery) ||
                  (arabicTokens.size > 1 && arabicTokens.all { tok -> normSent.contains(tok) })
              }
            val normLatinMatch = normLatinQuery.isNotBlank() &&
              com.example.data.local.ExtendedHadithDataProvider.normalizeLatinForSearch(sentence).let { normSent ->
                normSent.contains(normLatinQuery) ||
                  (latinTokens.isNotEmpty() && latinTokens.all { tok -> normSent.contains(tok) })
              }

            if (rawMatch || normArabicMatch || normLatinMatch) {
              val highlight = when {
                rawMatch -> sanitized
                normLatinMatch && latinTokens.isNotEmpty() -> latinTokens.maxByOrNull { it.length } ?: sanitized
                normArabicMatch && arabicTokens.isNotEmpty() -> arabicTokens.maxByOrNull { it.length } ?: sanitized
                else -> sanitized
              }
              matchedForChapter = BookLineMatch(
                book = book,
                serialNumber = serialNum,
                chapterIndex = chIdx,
                chapterNumber = ch.chapterNumber,
                chapterTitle = ch.titleEnglish,
                matchedLine = sentence.trim(),
                matchedLanguage = lang,
                highlightTerm = highlight
              )
              break
            }
          }
          if (matchedForChapter != null) break
        }

        // Also check if multi-sentence or chapter-wide match occurs across fields
        if (matchedForChapter == null) {
          val combinedLatin = com.example.data.local.ExtendedHadithDataProvider.normalizeLatinForSearch(
            "${book.titleEnglish} ${book.author} ${ch.titleEnglish} ${ch.englishContent} ${ch.transliteration} ${ch.scholarlyNotes} ${ch.crossReferences}"
          )
          val combinedArabic = com.example.data.local.ExtendedHadithDataProvider.normalizeArabicForSearch(
            "${book.titleArabic} ${book.titleUrdu} ${ch.titleArabic} ${ch.arabicExcerpt} ${ch.urduContent}"
          )
          val multiLatinHit = latinTokens.isNotEmpty() && latinTokens.all { combinedLatin.contains(it) }
          val multiArabicHit = arabicTokens.isNotEmpty() && arabicTokens.all { combinedArabic.contains(it) }
          if (multiLatinHit || multiArabicHit) {
            val snippet = if (multiArabicHit && ch.urduContent.isNotBlank()) ch.urduContent else ch.englishContent
            val tok = if (multiArabicHit) arabicTokens.firstOrNull().orEmpty() else latinTokens.firstOrNull().orEmpty()
            matchedForChapter = BookLineMatch(
              book = book,
              serialNumber = serialNum,
              chapterIndex = chIdx,
              chapterNumber = ch.chapterNumber,
              chapterTitle = ch.titleEnglish,
              matchedLine = snippet.take(220),
              matchedLanguage = if (multiArabicHit) "UR" else "EN",
              highlightTerm = tok.ifBlank { sanitized }
            )
          }
        }

        if (matchedForChapter != null) {
          matches.add(matchedForChapter)
        }
      }
    }

    return matches
  }

  fun formatBookEnglishContent(raw: String?): String {
    val t = raw?.trim().orEmpty()
    return if (t.isEmpty() || t.equals("Not available in this source", ignoreCase = true)) {
      "English Translation Coming Soon"
    } else {
      t
    }
  }

  fun formatBookUrduContent(raw: String?): String {
    val t = raw?.trim().orEmpty()
    return if (t.isEmpty() || t.equals("Not available in this source", ignoreCase = true)) {
      "Urdu Translation Coming Soon"
    } else {
      t
    }
  }

  private fun resolveLinkedCollectionKeysForBook(bookId: String): List<String> = when (bookId) {
    "book_bukhari" -> listOf("bukhari")
    "book_muslim" -> listOf("muslim")
    "book_abudawud" -> listOf("abudawud")
    "book_tirmidhi" -> listOf("tirmidhi")
    "book_nasai" -> listOf("nasai", "nasai_kubra")
    "book_ibnmajah" -> listOf("ibnmajah")
    "book_muwatta" -> listOf("malik")
    "book_musnad_ahmad" -> listOf("ahmad")
    "book_darimi" -> listOf("darimi")
    "book_riyad" -> listOf("riyadussalihin")
    "book_nawawi40" -> listOf("nawawi", "qudsi")
    "book_adab_mufrad" -> listOf("adab")
    "book_bulugh" -> listOf("bulugh")
    "book_mishkat" -> listOf("mishkat")
    "book_shamail" -> listOf("shamail", "sunni_history")
    "book_fath_albari", "book_sharh_nawawi" -> listOf("sunni_commentaries")
    "book_shia_alkafi" -> listOf("alkafi")
    "book_shia_faqih" -> listOf("faqih", "khisal", "uyun_akhbar_rida", "amali_saduq", "saduq_corpus")
    "book_shia_tahdhib_istibsar" -> listOf("tahdhib", "istibsar", "wasail")
    "book_shia_nahjul_balagha" -> listOf("nahjulbalagha")
    "book_shia_sahifa_sajjadiyya" -> listOf("sajjadiyya")
    "book_shia_almizan_majma" -> listOf("tafsir_qummi_ayyashi", "bihar")
    "book_history_tabari",
    "book_history_ibn_kathir",
    "book_history_ibn_hisham",
    "book_history_kamil_futuh",
    "book_history_dhahabi_siyar",
    "book_rahiq",
    "book_zaad_almaad" -> listOf("sunni_history")
    "book_shia_history_irshad",
    "book_shia_history_yaqubi_masudi",
    "book_shia_history_siffin_maqatil",
    "book_shia_history_luhuf_tabarsi" -> listOf("shia_history")
    else -> emptyList()
  }

  private fun createBook(
    id: String,
    number: Int,
    titleEnglish: String,
    titleArabic: String,
    titleUrdu: String,
    author: String,
    eraHijri: String,
    category: String,
    numberingSystem: String,
    summary: String,
    chapter1Title: String,
    chapter1Ar: String,
    chapter1Tr: String,
    chapter1En: String,
    chapter1Ur: String,
    chapter2Title: String,
    chapter2Ar: String,
    chapter2Tr: String,
    chapter2En: String,
    chapter2Ur: String,
    refs: String,
    tradition: String = "SUNNI",
    officialSourceUrl: String = "https://sunnah.com",
    apiOrRepoUrl: String = "https://github.com/fawazahmed0/hadith-api",
    sourceTypeLabel: String = "Classical Book & Verified Repository",
    gradingMethodology: String = "Classical Sunni Muhaddithun & Tahqiq"
  ): IslamicClassicalBook {
    val minuteOffset = (number * 5) % 50 + 10
    val tradLabel = if (tradition == "SHIA") "Shia Classical Library" else "Sunni Classical Library"

    val baseChapters = mutableListOf(
      IslamicBookChapter(
        chapterNumber = 1,
        titleEnglish = if (chapter1Title.startsWith("Jild", ignoreCase = true)) chapter1Title else "Jild 1 (Volume 1) — $chapter1Title",
        titleArabic = "المجلد الأول (الباب الأول) • $titleArabic",
        arabicExcerpt = chapter1Ar,
        transliteration = chapter1Tr,
        englishContent = formatBookEnglishContent(chapter1En),
        urduContent = formatBookUrduContent(chapter1Ur),
        scholarlyNotes = "Jild 1 (Volume 1) • Author: $author ($eraHijri) • Edition: $numberingSystem • Authentication: $gradingMethodology",
        crossReferences = refs,
        jildNumber = 1,
        jildTitle = "Jild 1 (Volume 1)"
      ),
      IslamicBookChapter(
        chapterNumber = 2,
        titleEnglish = if (chapter2Title.startsWith("Jild", ignoreCase = true)) chapter2Title else "Jild 2 (Volume 2) — $chapter2Title",
        titleArabic = "المجلد الثاني (الباب الثاني) • $titleArabic",
        arabicExcerpt = chapter2Ar,
        transliteration = chapter2Tr,
        englishContent = formatBookEnglishContent(chapter2En),
        urduContent = formatBookUrduContent(chapter2Ur),
        scholarlyNotes = "Jild 2 (Volume 2) • Tradition: $tradLabel • Source Type: $sourceTypeLabel • Official Reference: $officialSourceUrl",
        crossReferences = refs,
        jildNumber = 2,
        jildTitle = "Jild 2 (Volume 2)"
      )
    )

    val linkedKeys = resolveLinkedCollectionKeysForBook(id)
    if (linkedKeys.isNotEmpty()) {
      val matchingHadiths = com.example.data.local.ExtendedHadithDataProvider.getExtendedHadiths()
        .filter { it.collectionKey in linkedKeys }
        .sortedWith(compareBy({ it.bookNumber }, { it.hadithNumber }))

      matchingHadiths.forEachIndexed { idx, h ->
        val canonicalChapterNum = if (id == "book_shia_nahjul_balagha") {
          h.hadithNumber
        } else {
          idx + 3
        }
        val computedJild = if (h.collectionKey.endsWith("_history")) {
          (idx + 3)
        } else {
          h.bookNumber.coerceAtLeast(1)
        }
        val titlePrefix = if (id == "book_shia_nahjul_balagha" || h.collectionKey.endsWith("_history")) {
          "Jild $computedJild (Volume $computedJild) — ${h.bookName.replaceFirst("Book ", "Part ")} — ${h.chapterName}"
        } else {
          "Jild $computedJild — ${h.reference} — ${h.chapterName}"
        }
        baseChapters.add(
          IslamicBookChapter(
            chapterNumber = canonicalChapterNum,
            titleEnglish = titlePrefix,
            titleArabic = "المجلد $computedJild • ${h.bookName} • #${h.hadithNumber}",
            arabicExcerpt = h.arabicText,
            transliteration = h.transliteration,
            englishContent = formatBookEnglishContent(h.englishText),
            urduContent = formatBookUrduContent(h.urduText),
            scholarlyNotes = "Jild $computedJild • Source & Narrator: ${h.narrator} • ${com.example.data.local.ExtendedHadithDataProvider.formatPrimaryEditionBadge(h)} • ${h.inBookReference} • ${com.example.data.local.ExtendedHadithDataProvider.formatGradingMethodologyLabel(h)}",
            crossReferences = h.linkedQuranVerses.ifBlank { refs },
            jildNumber = computedJild,
            jildTitle = "Jild $computedJild (Volume $computedJild)"
          )
        )
      }
    }

    return IslamicClassicalBook(
      id = id,
      number = number,
      titleEnglish = titleEnglish,
      titleArabic = titleArabic,
      titleUrdu = titleUrdu,
      author = author,
      eraHijri = eraHijri,
      category = category,
      numberingSystem = numberingSystem,
      summary = summary,
      tradition = tradition,
      officialSourceUrl = officialSourceUrl,
      apiOrRepoUrl = apiOrRepoUrl,
      sourceTypeLabel = sourceTypeLabel,
      gradingMethodology = gradingMethodology,
      entryTimestamp = "2026-10-23 • 09:$minuteOffset UTC ($tradLabel)",
      chapters = baseChapters
    )
  }
}

