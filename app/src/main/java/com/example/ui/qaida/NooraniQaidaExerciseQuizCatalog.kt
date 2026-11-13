package com.example.ui.qaida

data class NooraniQaidaMcqQuestion(
  val id: String,
  val pageNumber: Int,
  val bookPageNumber: Int,
  val takhtiNumber: Int,
  val difficulty: String, // "BASIC", "INTERMEDIATE", "HARD"
  val questionEnglish: String,
  val questionUrdu: String,
  val arabicPrompt: String,
  val options: List<String>,
  val correctOptionIndex: Int,
  val explanationEnglish: String,
  val explanationUrdu: String
)

object NooraniQaidaExerciseQuizCatalog {
  val DIFFICULTY_LEVELS = listOf("ALL", "BASIC", "INTERMEDIATE", "HARD")

  val allQuestions: List<NooraniQaidaMcqQuestion> = listOf(
    // Page 1 (Book Page 1) - Cover & Hadith
    NooraniQaidaMcqQuestion(
      id = "mcq_p1_b1",
      pageNumber = 1,
      bookPageNumber = 1,
      takhtiNumber = 0,
      difficulty = "BASIC",
      questionEnglish = "Which noble Hadith is inscribed at the top of Page 1 of Mukammal Noorani Qaida?",
      questionUrdu = "نورانی قاعدہ کے صفحہ ۱ کے سرِ ورق پر کون سی حدیثِ مبارکہ درج ہے؟",
      arabicPrompt = "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْاٰنَ وَعَلَّمَهٗ",
      options = listOf(
        "The best among you are those who learn the Quran and teach it",
        "Actions are judged by intentions",
        "Cleanliness is half of faith",
        "Prayer is the pillar of religion"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Page 1 opens with the Hadith: 'Khayrukum man ta'allamal-Qur'ana wa 'allamah' (The best among you are those who learn the Quran and teach it).",
      explanationUrdu = "صفحہ ۱ پر حدیثِ مبارکہ درج ہے: تم میں سب سے بہتر وہ لوگ ہیں جو قرآن کا علم حاصل کریں اور (دوسروں کو) اس کی تعلیم دیں۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p1_i1",
      pageNumber = 1,
      bookPageNumber = 1,
      takhtiNumber = 0,
      difficulty = "INTERMEDIATE",
      questionEnglish = "Who originally compiled the classical Noorani Qaida mentioned on Page 1?",
      questionUrdu = "صفحہ ۱ کے مطابق نورانی قاعدہ کو اولاً کس بزرگ عالم نے مرتب فرمایا تھا؟",
      arabicPrompt = "مُكَمَّل نُورَانِی قَاعِدَہ",
      options = listOf(
        "Maulvi Noor Muhammad Sahib Ludhianvi",
        "Imam Abu Hanifa",
        "Allama Ibn Jazari",
        "Qari Abdul Basit"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "The cover page states it was originally compiled by Maulvi Noor Muhammad Sahib Ludhianvi and republished by Majlis Da'wat-ul-Haq Hardoi.",
      explanationUrdu = "سرورق پر درج ہے کہ جس کو اولاً مولوی نور محمد صاحب لدھیانویؒ نے مرتب کیا تھا۔"
    ),

    // Page 2 (Book Page 5) - Teacher Guidelines Takhti 1
    NooraniQaidaMcqQuestion(
      id = "mcq_p2_b1",
      pageNumber = 2,
      bookPageNumber = 5,
      takhtiNumber = 1,
      difficulty = "BASIC",
      questionEnglish = "According to Page 5 (Teacher Guidelines for Takhti 1), how many single Arabic letters (Mufradaat) are there in total?",
      questionUrdu = "صفحہ ۵ (ہدایت تختی نمبر ۱ مفردات) کے مطابق کل کتنے مفرد حروف ہیں؟",
      arabicPrompt = "اَلِفْ ، بَا ، تَا ، ثَا ... يَا",
      options = listOf("29 letters (کل ۲۹ حروف)", "26 letters", "32 letters", "24 letters"),
      correctOptionIndex = 0,
      explanationEnglish = "The guidelines explicitly state: 'There are 29 letters in total (کل ۲۹ حروف ہیں), which are the foundation of all Quranic learning.'",
      explanationUrdu = "ہدایت تختی نمبر ۱ میں لکھا ہے: کل ۲۹ حروف ہیں، جو تمام تعلیم کی بنیاد اور جڑ ہیں۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p2_i1",
      pageNumber = 2,
      bookPageNumber = 5,
      takhtiNumber = 1,
      difficulty = "INTERMEDIATE",
      questionEnglish = "Which three letters share the exact same boat-like base shape and are distinguished only by 1, 2, or 3 dots?",
      questionUrdu = "صفحہ ۵ کے مطابق کن تین حروف کی ایک ہی شکل ہے اور صرف نقطوں سے ان کی پہچان ہوتی ہے؟",
      arabicPrompt = "ب ، ت ، ث",
      options = listOf("ب ، ت ، ث (Ba, Ta, Tha)", "ج ، ح ، خ (Jeem, Ha, Kha)", "د ، ذ ، ر (Dal, Dhal, Ra)", "س ، ش ، ص (Seen, Sheen, Sad)"),
      correctOptionIndex = 0,
      explanationEnglish = "Ba (1 dot below), Ta (2 dots above), and Tha (3 dots above) share the same base shape on the first line.",
      explanationUrdu = "ب، ت، ث تینوں کی ایک ہی شکل ہے، نیچے ایک نقطہ ہو تو ب، اوپر دو نقطے ہوں تو ت، اور تین نقطے اوپر ہوں تو ث ہے۔"
    ),

    // Page 3 (Book Page 6) - Makharij Guidelines
    NooraniQaidaMcqQuestion(
      id = "mcq_p3_h1",
      pageNumber = 3,
      bookPageNumber = 6,
      takhtiNumber = 1,
      difficulty = "HARD",
      questionEnglish = "According to Page 6 (Makharij), which group of three letters is pronounced with a whistling sound (Seeti / Safir) by placing the tongue tip near the front teeth?",
      questionUrdu = "صفحہ ۶ کے مطابق کن تین حروف میں سیٹی کی آواز (صفیر) ہوتی ہے؟",
      arabicPrompt = "س ص ز",
      options = listOf("س ، ص ، ز (Seen, Sad, Za)", "ث ، ذ ، ظ (Tha, Dhal, Zua)", "ت ، د ، ط (Ta, Dal, Tua)", "ع ، ح ، ه (Ayn, Ha, Haa)"),
      correctOptionIndex = 0,
      explanationEnglish = "Seen (س), Sad (ص), and Za (ز) share the whistling (Seeti) quality; Za is loud, Sad is heavy (Pur), and Seen is light (Bareek).",
      explanationUrdu = "س، ص، ز تینوں حروف میں سیٹی کی آواز ہوتی ہے، ص کی آواز پُر اور س کی آواز باریک ہوتی ہے۔"
    ),

    // Page 4 (Book Page 7) - Heavy Letters (Musta'liyah)
    NooraniQaidaMcqQuestion(
      id = "mcq_p4_h1",
      pageNumber = 4,
      bookPageNumber = 7,
      takhtiNumber = 2,
      difficulty = "HARD",
      questionEnglish = "Which 7 letters are called Huruf-e-Musta'liyah (always pronounced heavy/full-mouth) on Page 7?",
      questionUrdu = "صفحہ ۷ کے مطابق وہ ۷ حروف کون سے ہیں جو ہمیشہ پُر (موٹے) پڑھے جاتے ہیں اور حروفِ مستعلیہ کہلاتے ہیں؟",
      arabicPrompt = "خ ص ض غ ط ق ظ",
      options = listOf(
        "خ ص ض غ ط ق ظ (Kha, Sad, Dad, Ghayn, Tua, Qaf, Zua)",
        "ب ت ث ج ح د ذ",
        "ق ط ب ج د (Qutbu Jaddin)",
        "ي ر م ل و ن (Yarmaloon)"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "The 7 Huruf-e-Musta'liyah (خ ص ض غ ط ق ظ - Khussa Daghtin Qiz) are always read with a full/elevated tongue.",
      explanationUrdu = "یہ ۷ حروف پُر (موٹے) پڑھے جاتے ہیں اور ان ہی سات حروف کو حروفِ مستعلیہ کہتے ہیں: خ ص ض غ ط ق ظ۔"
    ),

    // Page 5 (Book Page 8) - Harakat Terms
    NooraniQaidaMcqQuestion(
      id = "mcq_p5_i1",
      pageNumber = 5,
      bookPageNumber = 8,
      takhtiNumber = 3,
      difficulty = "INTERMEDIATE",
      questionEnglish = "According to Page 8, what is an Alif called when it carries a Zabar, Zer, or Pesh (vowel sign)?",
      questionUrdu = "صفحہ ۸ کے مطابق جس الف پر زبر، زیر یا پیش (حرکت) ہو اسے کیا کہتے ہیں؟",
      arabicPrompt = "اَ اِ اُ",
      options = listOf("Hamzah (ہمزہ)", "Alif Maddah (الف مدہ)", "Waaw Leen (واؤ لین)", "Sakin (ساکن)"),
      correctOptionIndex = 0,
      explanationEnglish = "Alif is always empty of vowels; any Alif bearing a Zabar, Zer, or Pesh is actually a Hamzah (جس الف پر زبر، زیر، پیش ہو اسے ہمزہ جانو).",
      explanationUrdu = "الف پر کوئی حرکت نہیں ہوتی، جس الف پر زبر، زیر، پیش ہو اسے ہمزہ جانو۔"
    ),

    // Page 6 (Book Page 9) - Maddah Guidelines
    NooraniQaidaMcqQuestion(
      id = "mcq_p6_b1",
      pageNumber = 6,
      bookPageNumber = 9,
      takhtiNumber = 4,
      difficulty = "BASIC",
      questionEnglish = "How long should Huruf-e-Maddah (ا ، و ، ی) and Standing Vowels (Khara Zabar, Khari Zer, Ulta Pesh) be stretched?",
      questionUrdu = "صفحہ ۹ کے مطابق حروفِ مدہ اور کھڑی حرکات کو کتنی مقدار کھینچ کر پڑھنا چاہئے؟",
      arabicPrompt = "بَا بُوْ بِيْ",
      options = listOf(
        "Equal to 1 Alif (ایک الف کے برابر)",
        "Equal to 5 Alifs",
        "Not stretched at all",
        "With a nasal sound (Ghunnah)"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "All three letters of Maddah and the three standing vowel signs are stretched equal to one Alif without nasalization.",
      explanationUrdu = "ان چھ کو ایک الف کے برابر کھینچنا چاہئے، اس مقدار میں کمی بیشی نہ ہو۔"
    ),

    // Page 7 & 8 (Book Page 10 & 11) - Takhti 1 Mufradaat & Imtihan
    NooraniQaidaMcqQuestion(
      id = "mcq_p7_b1",
      pageNumber = 7,
      bookPageNumber = 10,
      takhtiNumber = 1,
      difficulty = "BASIC",
      questionEnglish = "In Takhti 1 (Mufradaat, Page 10), which supplication is recited before starting the alphabet?",
      questionUrdu = "تختی نمبر ۱ (صفحہ ۱۰) کے شروع میں کون سی مسنون دعا لکھی ہے؟",
      arabicPrompt = "رَبِّ يَسِّرْ وَلَا تُعَسِّرْ وَتَمِّمْ بِالْخَيْرِ ... رَبِّ زِدْنِيْ عِلْمًا",
      options = listOf(
        "Rabbi yassir wa la tu'assir wa tammim bil-khayr ... Rabbi zidni 'ilma",
        "Subhanallahi wa bihamdihi",
        "Hasbunallahu wa ni'mal wakeel",
        "Allahu Akbar Kabira"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Takhti 1 begins with Bismillah followed by 'Rabbi yassir wa la tu'assir wa tammim bil-khayr wa bika nasta'eenu ya Fattah, Rabbi zidni 'ilma'.",
      explanationUrdu = "تختی نمبر ۱ کے آغاز میں رَبِّ يَسِّرْ وَلَا تُعَسِّرْ وَتَمِّمْ بِالْخَيْرِ وَبِكَ نَسْتَعِيْنُ يَا فَتَّاحُ رَبِّ زِدْنِيْ عِلْمًا درج ہے۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p8_i1",
      pageNumber = 8,
      bookPageNumber = 11,
      takhtiNumber = 1,
      difficulty = "INTERMEDIATE",
      questionEnglish = "On Page 11 (Takhti 1 Imtihan), what is tested at the bottom section of the page?",
      questionUrdu = "صفحہ ۱۱ (تختی نمبر ۱ امتحان) کے نچلے حصے میں کس چیز کی پہچان کرائی گئی ہے؟",
      arabicPrompt = "مرکبات میں ملے ہوئے حروف کی شکلیں",
      options = listOf(
        "Shortened shapes of letters when joined in compound words (مرکبات میں ملے ہوئے حروف کی شکلیں)",
        "Rules of Waqf (stopping)",
        "Rules of Idgham",
        "Six Kalimas"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "The bottom table of Page 11 shows each letter's full isolated form on top and its truncated joined forms below.",
      explanationUrdu = "صفحہ ۱۱ کے نیچے ’مرکبات میں ملے ہوئے حروف کی شکلیں‘ (ب، ت، ج، د، ع، ف، ک، ل، م، ن، ہ، ء، ی) دکھائی گئی ہیں۔"
    ),

    // Page 9–12 (Book Page 12–15) - Takhti 2 Murakkabaat
    NooraniQaidaMcqQuestion(
      id = "mcq_p9_b1",
      pageNumber = 9,
      bookPageNumber = 12,
      takhtiNumber = 2,
      difficulty = "BASIC",
      questionEnglish = "In Takhti 2 (Murakkabaat, Page 12), which two letters are combined in the compound form 'لا'?",
      questionUrdu = "تختی نمبر ۲ (مرکبات، صفحہ ۱۲) میں ’لا‘ کن دو حروف سے مل کر بنا ہے؟",
      arabicPrompt = "لا — ل ا",
      options = listOf("Lam + Alif (ل + ا)", "Ba + Alif (ب + ا)", "Kaf + Alif (ك + ا)", "Mim + Alif (م + ا)"),
      correctOptionIndex = 0,
      explanationEnglish = "The first row of Takhti 2 introduces Lam (ل) and Alif (ا) joining to form Lam-Alif (لا).",
      explanationUrdu = "تختی نمبر ۲ کی پہلی سطر میں ل اور ا کے ملنے سے ’لا‘ کی شکل سکھائی گئی ہے۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p10_i1",
      pageNumber = 10,
      bookPageNumber = 13,
      takhtiNumber = 2,
      difficulty = "INTERMEDIATE",
      questionEnglish = "On Page 13 (Takhti 2), identify the three letters joined in the word 'يَجِب' (last word of the 6th row):",
      questionUrdu = "صفحہ ۱۳ (تختی نمبر ۲) میں لفظ ’يَجِب‘ کن تین حروف سے مل کر بنا ہے؟",
      arabicPrompt = "يَجِب",
      options = listOf("Ya + Jeem + Ba (ي + ج + ب)", "Nun + Ha + Ba (ن + ح + ب)", "Ta + Kha + Ba (ت + خ + ب)", "Ba + Jeem + Ta (ب + ج + ت)"),
      correctOptionIndex = 0,
      explanationEnglish = "Two dots below at the start is Ya (ي), one dot below the middle arch is Jeem (ج), and one dot below the final boat is Ba (ب).",
      explanationUrdu = "شروع میں دو نقطے نیچے ’ی‘، درمیان میں ایک نقطہ نیچے ’ج‘ اور آخر میں ’ب‘ ہے (ی + ج + ب)۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p11_h1",
      pageNumber = 11,
      bookPageNumber = 14,
      takhtiNumber = 2,
      difficulty = "HARD",
      questionEnglish = "On Page 14 (Takhti 2), what are the three carrier letters shown under Hamzah at the very bottom of the page?",
      questionUrdu = "صفحہ ۱۴ کے آخر میں ہمزہ (ء) کن تین حروف کی شکل میں دکھایا گیا ہے؟",
      arabicPrompt = "ء أ ؤ ئ",
      options = listOf(
        "Hamzah on Alif (أ), Hamzah on Waaw (ؤ), and Hamzah on Ya (ئ)",
        "Hamzah on Ba, Dal, and Ra",
        "Hamzah on Seen, Sad, and Tua",
        "Hamzah on Mim, Nun, and Lam"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "As explained in Teacher Guideline #4 (Page 8), the bottom row of Page 14 shows original Hamzah (ء), Hamzah in the shape of Alif (أ), Waaw (ؤ), and Ya (ئ).",
      explanationUrdu = "صفحہ ۱۴ کی آخری سطر میں ہمزہ اصلی شکل میں (ء)، ہمزہ الف کی شکل میں (أ)، ہمزہ واو کی شکل میں (ؤ) اور ہمزہ یا کی شکل میں (ئ) ہے۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p12_i1",
      pageNumber = 12,
      bookPageNumber = 15,
      takhtiNumber = 2,
      difficulty = "INTERMEDIATE",
      questionEnglish = "Page 15 contains the Examination (اِمْتِحَانٌ) for Takhti 2. Which Quranic phrase is the very first test item?",
      questionUrdu = "صفحہ ۱۵ پر تختی نمبر ۲ کے ’امتحان‘ کا سب سے پہلا لفظ کون سا ہے؟",
      arabicPrompt = "اياك نستعين",
      options = listOf("اياك نستعين (Iyyaka na'budu / nasta'een)", "فجعلهم كعصف", "الذكرى", "مستقيم"),
      correctOptionIndex = 0,
      explanationEnglish = "The Imtihan box on Page 15 begins with 'اياك نستعين و ما يدريك'.",
      explanationUrdu = "صفحہ ۱۵ کے امتحان کی پہلی سطر ’اياك نستعين و ما يدريك‘ سے شروع ہوتی ہے۔"
    ),

    // Page 13–15 (Book Page 16–18) - Takhti 3 Harakaat
    NooraniQaidaMcqQuestion(
      id = "mcq_p13_b1",
      pageNumber = 13,
      bookPageNumber = 16,
      takhtiNumber = 3,
      difficulty = "BASIC",
      questionEnglish = "In Takhti 3 (Harakaat - Zabar, Page 16), how should Zabar, Zer, and Pesh be pronounced?",
      questionUrdu = "تختی نمبر ۳ (حرکات، صفحہ ۱۶) کے اصول کے مطابق زبر، زیر، پیش کو کیسے پڑھنا چاہئے؟",
      arabicPrompt = "دَرَسَ • وَدَعَ • عَبَدَ",
      options = listOf(
        "Read quickly without stretching or jerking (جلدی پڑھیں، تھوڑا بھی نہ کھینچیں)",
        "Stretch for 2 seconds",
        "Pause with a nasal sound",
        "Echo the letter (Qalqalah)"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Page 16 states: 'Read Zabar, Zer, Pesh quickly; do not stretch them even slightly (تھوڑا بھی نہ کھینچیں).'",
      explanationUrdu = "صفحہ ۱۶ پر درج ہے: زبر زیر پیش کو حرکات کہتے ہیں، زبر زیر پیش کو جلدی پڑھیں، تھوڑا بھی نہ کھینچیں۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p14_i1",
      pageNumber = 14,
      bookPageNumber = 17,
      takhtiNumber = 3,
      difficulty = "INTERMEDIATE",
      questionEnglish = "On Page 17 (Zer practice), what is the correct Hijje (spelling) shown at the footnote for the first Mashq word 'اِبِلِ'?",
      questionUrdu = "صفحہ ۱۷ (زیر کی مشق) میں پہلے لفظ ’اِبِلِ‘ کے صحیح ہجے حاشیے میں کیا لکھے ہیں؟",
      arabicPrompt = "اِبِلِ",
      options = listOf(
        "Hamzah Zer I, Ba Zer Bi -> Ibi, Lam Zer Li -> Ibili (ہمزہ زیر اِ، ب زیر بِ، اِبِ، ل زیر لِ، اِبِلِ)",
        "Alif Zer I, Ba Zabar Ba, Lam Pesh Lu",
        "Hamzah Zabar A, Ba Zer Bi, Lam Sakin",
        "Alif Maddah, Ba, Lam"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "The footnote on Page 17 spells it: 'ہمزہ زیر اِ - ب زیر بِ ، اِبِ - ل زیر لِ - اِبِلِ' noting that it is Hamzah, not Alif.",
      explanationUrdu = "صفحہ ۱۷ کے حاشیے میں لکھا ہے: ہمزہ زیر اِ، ب زیر بِ (اِبِ)، ل زیر لِ (اِبِلِ)۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p15_i1",
      pageNumber = 15,
      bookPageNumber = 18,
      takhtiNumber = 3,
      difficulty = "INTERMEDIATE",
      questionEnglish = "On Page 18 (Pesh practice), how do the lips move when pronouncing Pesh (ـُ)?",
      questionUrdu = "صفحہ ۱۸ کے مطابق پیش (ـُ) پڑھتے وقت ہونٹوں کی کیا حالت ہوتی ہے؟",
      arabicPrompt = "رُسُلُ • سُدُسُ • فُقِدَ",
      options = listOf(
        "The sound goes forward and the edges of the lips meet with a slight opening in the middle",
        "The lips stay wide flat",
        "The tongue touches the throat",
        "The sound goes into the nose"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Page 18 explains: 'The sound of Pesh goes forward, the edges of the lips meet, and the middle remains slightly open.'",
      explanationUrdu = "پیش کی آواز آگے کو جاتی ہے اور ہونٹوں کے کنارے ملتے ہیں اور بیچ کا حصہ کچھ کھلا رہتا ہے۔"
    ),

    // Page 16–21 (Book Page 19–24) - Takhti 4 Huruf-e-Maddah & Standing Vowels
    NooraniQaidaMcqQuestion(
      id = "mcq_p16_b1",
      pageNumber = 16,
      bookPageNumber = 19,
      takhtiNumber = 4,
      difficulty = "BASIC",
      questionEnglish = "According to Takhti 4 (Page 19), when does Alif become 'Alif Maddah'?",
      questionUrdu = "تختی نمبر ۴ (صفحہ ۱۹) کے مطابق الف کب ’الف مدہ‘ بنتا ہے؟",
      arabicPrompt = "بَا تَا ثَا — زَادَ خَافَ تَابَ",
      options = listOf(
        "When Alif is preceded by a Zabar (جس الف سے پہلے زبر ہو)",
        "When Alif is preceded by a Zer",
        "When Alif is preceded by a Pesh",
        "When Alif has a Jazam on it"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Alif Maddah occurs when an empty Alif is preceded by a Zabar on the previous letter (e.g., بَا ، زَادَ).",
      explanationUrdu = "جس الف سے پہلے زبر ہو وہ الف مدہ ہوتا ہے، اسے ایک الف کے برابر کھینچ کر پڑھیں۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p17_b1",
      pageNumber = 17,
      bookPageNumber = 20,
      takhtiNumber = 4,
      difficulty = "BASIC",
      questionEnglish = "According to Page 20, when does Ya become 'Ya Maddah' (ی مدہ)?",
      questionUrdu = "صفحہ ۲۰ کے مطابق ’ی‘ کب یائے مدہ ہوتی ہے؟",
      arabicPrompt = "بِيْ تِيْ — دِيْنِيْ فِيْهِ",
      options = listOf(
        "When Ya has Jazam (Sukoon) and is preceded by a Zer (جس ی پر جزم ہو اور اس سے پہلے زیر ہو)",
        "When Ya is preceded by a Zabar",
        "When Ya has a Tashdeed",
        "When Ya is at the start of a word"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Ya Sakin preceded by Zer is Ya Maddah (e.g., دِيْنِيْ ، فِيْهِ) and is stretched 1 Alif with a Ma'roof sound.",
      explanationUrdu = "جس ی پر جزم ہو اور اس سے پہلے زیر ہو تو وہ یائے مدہ ہے، اسے تھوڑا کھینچیں اور معروف پڑھیں۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p18_b1",
      pageNumber = 18,
      bookPageNumber = 21,
      takhtiNumber = 4,
      difficulty = "BASIC",
      questionEnglish = "According to Page 21, when does Waaw become 'Waaw Maddah' (و مدہ)?",
      questionUrdu = "صفحہ ۲۱ کے مطابق ’و‘ کب واؤ مدہ ہوتا ہے؟",
      arabicPrompt = "بُوْ تُوْ — نُوْحٌ طُوْرُ تُوْبُوْا",
      options = listOf(
        "When Waaw has Jazam and is preceded by a Pesh (جس و پر جزم ہو اور اس سے پہلے پیش ہو)",
        "When Waaw has Jazam and is preceded by a Zabar",
        "When Waaw carries a Zabar",
        "When Waaw is followed by Alif"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Waaw Sakin preceded by Pesh is Waaw Maddah (e.g., نُوْحٌ ، طُوْرُ ، تُوْبُوْا).",
      explanationUrdu = "جس و پر جزم ہو اور اس سے پہلے پیش ہو اسے واؤ مدہ کہتے ہیں۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p19_i1",
      pageNumber = 19,
      bookPageNumber = 22,
      takhtiNumber = 4,
      difficulty = "INTERMEDIATE",
      questionEnglish = "According to Page 22, Khara Zabar (کھڑا زبر ـٰ) is equal in pronunciation to which letter of Maddah?",
      questionUrdu = "صفحہ ۲۲ کے مطابق کھڑا زبر (ـٰ) کس حرفِ مدہ کے برابر ہوتا ہے؟",
      arabicPrompt = "بٰ = بَا • اٰدَمَ • اٰمَنَ",
      options = listOf(
        "Alif Maddah (بٰ is equal to بَا)",
        "Ya Maddah",
        "Waaw Maddah",
        "Noon Sakin"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Page 22 states: 'Khara Zabar is equal to Alif Maddah, just as بٰ is equal to بَا (e.g., اٰدَمَ ، هٰذَا).'",
      explanationUrdu = "کھڑا زبر برابر ہوتا ہے الف مدہ کے، جیسے بٰ برابر ہے بَا کے۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p20_i1",
      pageNumber = 20,
      bookPageNumber = 23,
      takhtiNumber = 4,
      difficulty = "INTERMEDIATE",
      questionEnglish = "According to Page 23, Khari Zer (کھڑی زیر ـٖ) is equal in pronunciation to which letter?",
      questionUrdu = "صفحہ ۲۳ کے مطابق کھڑی زیر کس کے برابر ہے؟",
      arabicPrompt = "بٖ = بِيْ • اٖلٰفِ • بِهٖ",
      options = listOf(
        "Ya Maddah (بٖ is equal to بِيْ)",
        "Alif Maddah",
        "Waaw Leen",
        "Tanween"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Page 23 states: 'Khari Zer is equal to Ya Maddah, just as بٖ is equal to بِيْ (e.g., بِهٖ ، عِبَادِهٖ).'",
      explanationUrdu = "کھڑی زیر برابر ہے ی مدہ کے، جیسے بٖ برابر ہے بِيْ کے۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p21_i1",
      pageNumber = 21,
      bookPageNumber = 24,
      takhtiNumber = 4,
      difficulty = "INTERMEDIATE",
      questionEnglish = "According to Page 24, Ulta Pesh (الٹا پیش) is equal in pronunciation to which letter?",
      questionUrdu = "صفحہ ۲۴ کے مطابق الٹا پیش کس کے برابر ہے؟",
      arabicPrompt = "لَهٗ • دَاوٗدَ • رَسُوْلُهٗ",
      options = listOf(
        "Waaw Maddah (بٗ is equal to بُوْ)",
        "Alif Maddah",
        "Ya Maddah",
        "Qalqalah"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Page 24 states: 'Ulta Pesh is equal to Waaw Maddah, just as بٗ is equal to بُوْ (e.g., لَهٗ ، رَسُوْلُهٗ).'",
      explanationUrdu = "الٹا پیش برابر ہے و مدہ کے، جیسے بٗ برابر ہے بُوْ کے۔"
    ),

    // Page 22–24 (Book Page 25–27) - Advanced Teacher Guidelines (Takhti 5 to 24)
    NooraniQaidaMcqQuestion(
      id = "mcq_p22_h1",
      pageNumber = 22,
      bookPageNumber = 25,
      takhtiNumber = 14,
      difficulty = "HARD",
      questionEnglish = "According to Page 25 (Guidelines for Takhti 14), what are the 3 conditions for Ra Sakinah (رْ) preceded by Zer to be pronounced light (Bareek)?",
      questionUrdu = "صفحہ ۲۵ (ہدایت تختی نمبر ۱۴) کے مطابق رائے ساکنہ سے پہلے زیر ہو تو اس کے باریک ہونے کی تین شرطیں کیا ہیں؟",
      arabicPrompt = "فِرْعَوْنَ • مِرْيَةٍ — قِرْطَاسٍ • مِرْصَادًا",
      options = listOf(
        "1) The Zer is original (Asli, not Arzi), 2) Both are in the same word, 3) Not followed by a Musta'liyah letter in the same word",
        "1) Ra is at the end of a verse, 2) Preceded by Alif, 3) Followed by Mim",
        "1) Preceded by Pesh, 2) Followed by Waaw, 3) Has Tanween",
        "Ra Sakinah is always heavy without exception"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Page 25 lists the 3 conditions: 1) Original Zer before Ra Sakinah, 2) In one word, 3) No Musta'liyah letter (خ ص ض غ ط ق ظ) after it in the same word.",
      explanationUrdu = "تین شرطیں ہیں: ۱) رائے ساکنہ سے پہلے زیر اصلی ہو عارضی نہ ہو، ۲) دونوں ایک کلمہ میں ہوں، ۳) رائے ساکنہ کے بعد اسی کلمہ میں حروفِ مستعلیہ میں سے کوئی نہ ہو۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p23_h1",
      pageNumber = 23,
      bookPageNumber = 26,
      takhtiNumber = 19,
      difficulty = "HARD",
      questionEnglish = "According to Page 26 (Guidelines for Takhti 19 - Madd), which is the ONLY example of 'Madd-e-Lazim Kalmi Mukhaffaf' in the entire Holy Quran?",
      questionUrdu = "صفحہ ۲۶ کے مطابق پورے قرآن مجید میں ’مدِ لازم کلمی مخفف‘ کی واحد مثال کون سی ہے؟",
      arabicPrompt = "آلْـٰٔنَ",
      options = listOf("آلْـٰٔنَ (Aal'aana)", "دَآبَّةٍ (Daabbatin)", "الم (Alif-Lam-Mim)", "جَآءَ (Jaa'a)"),
      correctOptionIndex = 0,
      explanationEnglish = "Page 26 explicitly notes: 'Madd Lazim Kalmi Mukhaffaf: where original Sukoon follows a Madd letter in a word, like آلْـٰٔنَ — this is the only example in the entire Quran.'",
      explanationUrdu = "صفحہ ۲۶ پر درج ہے: مد لازم کلمی مخفف جہاں حرفِ مد کے بعد والے حرف پر سکونِ اصلی ہو جیسے آلْـٰٔنَ، پورے قرآن میں یہی ایک مثال ہے۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p24_h1",
      pageNumber = 24,
      bookPageNumber = 27,
      takhtiNumber = 24,
      difficulty = "HARD",
      questionEnglish = "According to Page 27 (bottom lines), when Noon Sakin is followed by Ya or Waaw within the SAME single word, what rule applies and what are its 4 Quranic words?",
      questionUrdu = "صفحہ ۲۷ کے مطابق نون ساکن کے بعد ی یا و ایک ہی کلمے میں آئیں تو کون سا قاعدہ ہوتا ہے اور قرآن میں اس کے چار الفاظ کون سے ہیں؟",
      arabicPrompt = "دُنْيَا ، قِنْوَانٌ ، صِنْوَانٌ ، بُنْيَانٌ",
      options = listOf(
        "Izhar-e-Mutlaq (اظہارِ مطلق — read clearly without Idgham in: دُنْيَا ، قِنْوَانٌ ، صِنْوَانٌ ، بُنْيَانٌ)",
        "Idgham Bil-Ghunnah",
        "Iqlab",
        "Ikhfa-e-Shafawi"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "When Noon Sakin and Ya/Waaw appear in the same word, Idgham does NOT occur; it is read clearly and called Izhar-e-Mutlaq in the 4 words: Dunya, Qinwan, Sinwan, Bunyan.",
      explanationUrdu = "نون ساکن کے بعد ی یا و ایک ہی کلمہ میں ہوں تو ادغام و غنہ نہ ہوگا بلکہ اظہار کے ساتھ پڑھیں گے جیسے دُنْيَا، قِنْوَانٌ، صِنْوَانٌ، بُنْيَانٌ، اس کو اظہارِ مطلق کہتے ہیں۔"
    ),

    // Page 25 & 26 (Book Page 28 & 29) - Takhti 5 Huruf-e-Leen
    NooraniQaidaMcqQuestion(
      id = "mcq_p25_b1",
      pageNumber = 25,
      bookPageNumber = 28,
      takhtiNumber = 5,
      difficulty = "BASIC",
      questionEnglish = "According to Takhti 5 (Page 28), which two letters are Huruf-e-Leen and how are they pronounced?",
      questionUrdu = "تختی نمبر ۵ (صفحہ ۲۸) کے مطابق حروفِ لین کتنے ہیں اور کیسے ادا کئے جاتے ہیں؟",
      arabicPrompt = "اَوْ بَوْ — خَوْفٍ • قَوْمٍ • صَوْمٌ",
      options = listOf(
        "2 letters: Waaw Sakin & Ya Sakin preceded by Zabar, read softly and quickly without stretching",
        "3 letters: Alif, Waaw, Ya stretched for 4 counts",
        "5 letters of Qalqalah",
        "6 throat letters"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Huruf-e-Leen are two: Waaw Sakin (وْ) and Ya Sakin (يْ) preceded by a Zabar, pronounced softly and smoothly without stretching.",
      explanationUrdu = "حروفِ لین دو ہیں و اور ی جن پر جزم ہو اور ان سے پہلے زبر ہو، ان دونوں کو نرم آواز کے ساتھ جلد ادا کریں۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p26_i1",
      pageNumber = 26,
      bookPageNumber = 29,
      takhtiNumber = 5,
      difficulty = "INTERMEDIATE",
      questionEnglish = "Which of the following Mashq words from Page 29 contains 'Ya-e-Leen' (يْ preceded by Zabar)?",
      questionUrdu = "صفحہ ۲۹ کی مشق میں سے کس لفظ میں ’یائے لین‘ موجود ہے؟",
      arabicPrompt = "صَيْفٌ • قُرَيْشٍ • عَلَيْهِمْ",
      options = listOf("صَيْفٌ (Sayfun)", "دِيْنِيْ (Deenee)", "فِيْهِ (Feehi)", "نُوْحٌ (Noohun)"),
      correctOptionIndex = 0,
      explanationEnglish = "In صَيْفٌ, the letter Sad has a Zabar followed by Ya Sakin (صَيْ), making it Ya-e-Leen.",
      explanationUrdu = "لفظ صَيْفٌ میں ص پر زبر اور اس کے بعد ی ساکن ہے، جو یائے لین ہے۔"
    ),

    // Page 27 (Book Page 30) - Takhti 6 Combined Mashq
    NooraniQaidaMcqQuestion(
      id = "mcq_p27_i1",
      pageNumber = 27,
      bookPageNumber = 30,
      takhtiNumber = 6,
      difficulty = "INTERMEDIATE",
      questionEnglish = "What is the purpose of Takhti 6 (Page 30) in the first row 'بَ بَا • بِ بٖ بِيْ • بُ بٗ بُوْ بَوْ'?",
      questionUrdu = "تختی نمبر ۶ (صفحہ ۳۰) کی پہلی سطر ’بَ بَا • بِ بٖ بِيْ • بُ بٗ بُوْ بَوْ‘ میں کس چیز کا موازنہ کرایا گیا ہے؟",
      arabicPrompt = "بَ بَا ❖ بِ بٖ بِيْ ❖ بُ بٗ بُوْ بَوْ",
      options = listOf(
        "Comparing short vowels (Harakaat), standing vowels / Maddah letters, and Leen letters side by side",
        "Practicing stopping signs only",
        "Practicing Idgham only",
        "Learning the alphabet order"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Takhti 6 drills the contrast between un-stretched Harakaat (بَ), stretched Maddah/Standing vowels (بَا ، بٖ ، بِيْ), and soft Leen (بَوْ ، بَيْ).",
      explanationUrdu = "تختی نمبر ۶ حرکات، حروفِ مدہ (و کھڑی حرکات) اور حروفِ لین کے فرق کی مشترکہ مشق ہے۔"
    ),

    // Page 28 & 29 (Book Page 31 & 32) - Takhti 7 & 8 Tanween & Noon Sakin
    NooraniQaidaMcqQuestion(
      id = "mcq_p28_b1",
      pageNumber = 28,
      bookPageNumber = 31,
      takhtiNumber = 7,
      difficulty = "BASIC",
      questionEnglish = "According to Takhti 7 (Page 31), what are Do Zabar (ـً), Do Zer (ـٍ), and Do Pesh (ـٌ) called?",
      questionUrdu = "تختی نمبر ۷ (صفحہ ۳۱) کے مطابق دو زبر (ـً)، دو زیر (ـٍ) اور دو پیش (ـٌ) کو کیا کہتے ہیں؟",
      arabicPrompt = "ـً  ـٍ  ـٌ  — بًا بٍِ بٌ",
      options = listOf("Tanween (تنوین)", "Tashdeed (تشدید)", "Jazam (جزم)", "Maddah (مدہ)"),
      correctOptionIndex = 0,
      explanationEnglish = "Do Zabar, Do Zer, and Do Pesh are called Tanween, which produces an inherent Noon Sakin sound at the end.",
      explanationUrdu = "دو زبر، دو زیر، دو پیش کو تنوین کہتے ہیں۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p29_b1",
      pageNumber = 29,
      bookPageNumber = 32,
      takhtiNumber = 8,
      difficulty = "BASIC",
      questionEnglish = "According to Takhti 8 (Page 32), why are 'تًا' and 'تَنْ' shown side by side in the practice table?",
      questionUrdu = "تختی نمبر ۸ (صفحہ ۳۲) میں ’تًا | تَنْ‘ کو آمنے سامنے کیوں دکھایا گیا ہے؟",
      arabicPrompt = "تًا | تَنْ • تٍ | تِنْ • تٌ | تُنْ",
      options = listOf(
        "Because Tanween and Noon Sakin have the exact same sound (تنوین اور نون ساکن کی آواز یکساں ہوتی ہے)",
        "Because they have opposite meanings",
        "Because one is stretched 4 counts",
        "Because one is silent"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Page 32 states: 'A Noon with Jazam is called Noon Sakin. The sound of Tanween and Noon Sakin is identical, just as بًا equals بَنْ.'",
      explanationUrdu = "صفحہ ۳۲ پر لکھا ہے: جس نون پر جزم (سکون) ہو اس کو نون ساکن کہتے ہیں، تنوین اور نون ساکن کی آواز یکساں ہوتی ہے جیسے بًا برابر ہے بَنْ کے۔"
    ),

    // Page 30 (Book Page 33) - Takhti 9 Izhar
    NooraniQaidaMcqQuestion(
      id = "mcq_p30_i1",
      pageNumber = 30,
      bookPageNumber = 33,
      takhtiNumber = 9,
      difficulty = "INTERMEDIATE",
      questionEnglish = "According to Takhti 9 (Page 33 - Izhar), what are the 6 Throat Letters (Huruf-e-Halqi)?",
      questionUrdu = "تختی نمبر ۹ (صفحہ ۳۳، اظہار) کے مطابق ۶ حروفِ حلقی کون سے ہیں؟",
      arabicPrompt = "ء ه ع ح غ خ — طَيْرًا اَبَابِيْلَ • مَنْ اٰمَنَ",
      options = listOf(
        "ء ، ه ، ع ، ح ، غ ، خ (Hamzah, Ha, Ayn, Haa, Ghayn, Kha)",
        "ي ، ر ، م ، ل ، و ، ن",
        "ق ، ط ، ب ، ج ، د",
        "ف ، ق ، ك ، ل ، م ، ن"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "When any of the 6 Huruf-e-Halqi (ء ه ع ح غ خ) comes after Tanween or Noon Sakin, the Noon sound is pronounced clearly without Ghunnah (Izhar).",
      explanationUrdu = "حروفِ حلقی ۶ ہیں: ء ہ ع ح غ خ۔ تنوین یا نون ساکن کے بعد حروفِ حلقی آویں تو نون کو ظاہر کر کے بغیر غنہ کے جلد پڑھیں گے، اس کو اظہار کہتے ہیں۔"
    ),

    // Page 31 (Book Page 34) - Takhti 10 Ikhfa
    NooraniQaidaMcqQuestion(
      id = "mcq_p31_i1",
      pageNumber = 31,
      bookPageNumber = 34,
      takhtiNumber = 10,
      difficulty = "INTERMEDIATE",
      questionEnglish = "According to Takhti 10 (Page 34 - Ikhfa), how many letters of Ikhfa are there and how is Ikhfa performed?",
      questionUrdu = "تختی نمبر ۱۰ (صفحہ ۳۴، اخفاء) کے مطابق حروفِ اخفاء کتنے ہیں اور اخفاء کیسے کیا جاتا ہے؟",
      arabicPrompt = "ت ث ج د ذ ز س ش ص ض ط ظ ف ق ك — اَنْتَ مُنْذِرٌ",
      options = listOf(
        "15 letters; hide the Noon sound in the nose with Ghunnah for the duration of 1 Alif",
        "6 letters; read Noon clearly without Ghunnah",
        "1 letter (Ba); change Noon into Mim",
        "4 letters; bounce the sound"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "There are 15 Huruf-e-Ikhfa (ت ث ج د ذ ز س ش ص ض ط ظ ف ق ك). After Noon Sakin/Tanween, the sound is concealed in the nasal passage for 1 Alif.",
      explanationUrdu = "حروفِ اخفاء ۱۵ ہیں (ت ث ج د ذ ز س ش ص ض ط ظ ف ق ك)۔ تنوین یا نون ساکن کے بعد حروفِ اخفاء آویں تو نون کی آواز کو ناک میں چھپا کر ایک الف کے برابر پڑھنا چاہئے۔"
    ),

    // Page 32 & 33 (Book Page 35 & 36) - Takhti 11 & 12 Jazam / Sukoon & Qalqalah
    NooraniQaidaMcqQuestion(
      id = "mcq_p32_b1",
      pageNumber = 32,
      bookPageNumber = 35,
      takhtiNumber = 11,
      difficulty = "BASIC",
      questionEnglish = "According to Takhti 11 (Page 35), what are the 5 letters of Qalqalah (combined in the phrase قُطْبُ جَدٍّ)?",
      questionUrdu = "تختی نمبر ۱۱ (صفحہ ۳۵) کے مطابق حروفِ قلقلہ ۵ کون سے ہیں جن کا مجموعہ ’قُطْبُ جَدٍّ‘ ہے؟",
      arabicPrompt = "ق ط ب ج د — اَبْ اِبْ اُبْ • قَطْ قِطْ قُطْ",
      options = listOf(
        "Qaf, Tua, Ba, Jeem, Dal (ق ، ط ، ب ، ج ، د)",
        "Kha, Sad, Dad, Ghayn, Zua",
        "Hamzah, Ha, Ayn, Haa, Kha",
        "Mim, Noon, Waaw, Ya, Lam"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "The 5 Qalqalah letters are ق ط ب ج د (Qutbu Jaddin). When they have Jazam (Sukoon), their articulation point bounces with an echoing sound.",
      explanationUrdu = "حروفِ قلقلہ ۵ ہیں: ق ط ب ج د (جن کا مجموعہ قُطْبُ جَدٍّ ہے)، جب ان پر جزم ہو تو ان کو پڑھتے وقت ان کے مخرج ٹکرا کر الگ ہو جاتے ہیں۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p33_i1",
      pageNumber = 33,
      bookPageNumber = 36,
      takhtiNumber = 12,
      difficulty = "INTERMEDIATE",
      questionEnglish = "According to Takhti 12 (Page 36 - Mashq Sukoon), which letter has a slight glottal jerk (Jhatka) when it is Sakin?",
      questionUrdu = "تختی نمبر ۱۲ (صفحہ ۳۶، مشقِ سکون) کے مطابق کس ساکن حرف کے پڑھنے میں جھٹکا ہوتا ہے؟",
      arabicPrompt = "يَأْمُرُوْنَ • يُؤْتُوْنَ • جِئْنَا • اِقْرَأْ",
      options = listOf("Hamzah Sakinah (ہمزہ ساکنہ)", "Sin Sakinah (سین ساکنہ)", "Mim Sakinah (میم ساکنہ)", "Fa Sakinah (فا ساکنہ)"),
      correctOptionIndex = 0,
      explanationEnglish = "Page 36 states: 'A letter with Jazam is called Sakin; there is a slight glottal stop/jerk when pronouncing Hamzah Sakinah (ہمزہ ساکنہ کے پڑھنے میں جھٹکا ہوتا ہے).'",
      explanationUrdu = "صفحہ ۳۶ پر لکھا ہے: جس حرف پر جزم ہو اس کو ساکن کہتے ہیں، ہمزہ ساکنہ کے پڑھنے میں جھٹکا ہوتا ہے جیسے يَأْمُرُوْنَ، يُؤْتُوْنَ۔"
    ),

    // Page 34 (Book Page 37) - Takhti 13 Tashdeed
    NooraniQaidaMcqQuestion(
      id = "mcq_p34_b1",
      pageNumber = 34,
      bookPageNumber = 37,
      takhtiNumber = 13,
      difficulty = "BASIC",
      questionEnglish = "According to Takhti 13 (Page 37 - Tashdeed), what must you do when Noon or Mim has a Tashdeed (نّ ، مّ)?",
      questionUrdu = "تختی نمبر ۱۳ (صفحہ ۳۷، تشدید) کے مطابق ن یا م پر تشدید ہو تو کیا حکم ہے؟",
      arabicPrompt = "اِنَّ • ثُمَّ • عَمَّ",
      options = listOf(
        "Perform Ghunnah (hold the sound in the nose for the duration of 1 Alif)",
        "Perform Qalqalah",
        "Read quickly without holding",
        "Drop the letter"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Page 37 states: 'When Noon or Mim has Tashdeed, there will be Ghunnah; holding the sound in the nose for 1 Alif is called Ghunnah (e.g., اِنَّ ، ثُمَّ).'",
      explanationUrdu = "ن اور م پر تشدید ہو تو غنہ ہوگا، ناک میں تھوڑی دیر آواز کو روکنا غنہ کہلاتا ہے، غنہ کی مقدار ایک الف کے برابر ہے۔"
    ),

    // Page 35 (Book Page 38) - Takhti 14 Ra-e-Sakinah wa Mushaddadah
    NooraniQaidaMcqQuestion(
      id = "mcq_p35_h1",
      pageNumber = 35,
      bookPageNumber = 38,
      takhtiNumber = 14,
      difficulty = "HARD",
      questionEnglish = "On Page 38 (Takhti 14), why is Ra Sakinah read HEAVY (پُر) in 'مِرْصَادًا' and 'قِرْطَاسٍ' even though it is preceded by a Zer?",
      questionUrdu = "صفحہ ۳۸ (تختی نمبر ۱۴) میں ’مِرْصَادًا‘ اور ’قِرْطَاسٍ‘ میں رائے ساکنہ سے پہلے زیر ہونے کے باوجود ر کو پُر (موٹا) کیوں پڑھا جاتا ہے؟",
      arabicPrompt = "فِرْقَةٌ • مِرْصَادًا • قِرْطَاسٍ",
      options = listOf(
        "Because Ra Sakinah is immediately followed in the same word by a Musta'liyah letter (ق ، ص ، ط)",
        "Because the Zer is a Pesh",
        "Because of Idgham",
        "Because of Waqf"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "In فِرْقَةٌ ، مِرْصَادًا ، قِرْطَاسٍ, Ra Sakinah is followed in the same word by a Musta'liyah letter (ق ، ص ، ط), so it is read heavy (Pur).",
      explanationUrdu = "کیونکہ رائے ساکنہ کے بعد اسی کلمہ میں حروفِ مستعلیہ (ق، ص، ط) آ گئے ہیں، اس لئے ان الفاظ میں ر کو پُر کر کے پڑھنا چاہئے۔"
    ),

    // Page 36 (Book Page 39) - Takhti 15 & 16 Huruf Qamari/Shamsi & Lafz Allah
    NooraniQaidaMcqQuestion(
      id = "mcq_p36_i1",
      pageNumber = 36,
      bookPageNumber = 39,
      takhtiNumber = 15,
      difficulty = "INTERMEDIATE",
      questionEnglish = "According to Takhti 15 & 16 (Page 39), when is the Lam of Lafz 'Allah' (اللّٰه) read heavy (Pur) vs. light (Bareek)?",
      questionUrdu = "صفحہ ۳۹ (تختی نمبر ۱۶، لفظ اللّٰہ کا قاعدہ) کے مطابق لفظ اللّٰہ کے لام کو کب پُر اور کب باریک پڑھیں گے؟",
      arabicPrompt = "قَالَ اللّٰهُ • حُدُوْدُ اللّٰهِ — بِسْمِ اللّٰهِ • دِيْنِ اللّٰهِ",
      options = listOf(
        "Heavy (Pur) when preceded by Zabar or Pesh; Light (Bareek) when preceded by Zer",
        "Always Heavy in all cases",
        "Always Light in all cases",
        "Heavy after Zer and Light after Zabar"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Page 39 states: 'If Zabar or Pesh precedes the Lam of Allah (e.g., قَالَ اللّٰهُ ، حُدُوْدُ اللّٰهِ), read it heavy; if Zer precedes it (e.g., دِيْنِ اللّٰهِ), read it light.'",
      explanationUrdu = "لفظ اللّٰہ کے لام سے پہلے زبر یا پیش ہو تو لفظ اللّٰہ کے لام کو پُر پڑھیں گے اور اگر زیر ہو تو باریک پڑھیں گے۔"
    ),

    // Page 37 (Book Page 40) - Takhti 17 Rules of Waqf
    NooraniQaidaMcqQuestion(
      id = "mcq_p37_i1",
      pageNumber = 37,
      bookPageNumber = 40,
      takhtiNumber = 17,
      difficulty = "INTERMEDIATE",
      questionEnglish = "According to Takhti 17 (Page 40 - Rules of Waqf), how do you stop (Waqf) on a word ending with Do Zabar (e.g., اَحَدًا) vs. Round Ta (e.g., جَارِيَةٌ)?",
      questionUrdu = "تختی نمبر ۱۷ (صفحہ ۴۰، وقف کرنے کے قاعدے) کے مطابق دو زبر (جیسے اَحَدًا) اور گول تا (جیسے جَارِيَةٌ) پر وقف کیسے کیا جاتا ہے؟",
      arabicPrompt = "اَحَدًا ⬅ اَحَدَا • جَارِيَةٌ ⬅ جَارِيَهْ",
      options = listOf(
        "Do Zabar becomes Alif (اَحَدَا), and Round Ta (ة) becomes Ha Sakinah (جَارِيَهْ)",
        "Both are dropped completely",
        "Both are read with Do Pesh",
        "Do Zabar becomes Noon Sakin"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Page 40 rules: 1) On Zabar, Zer, Pesh, Do Zer, Do Pesh -> make the last letter Sakin. 2) On Do Zabar -> read with 1 Zabar + Alif (Madd-e-Iwad). 3) On Round Ta (ة) -> change to Ha Sakinah (هْ).",
      explanationUrdu = "دو زبر پر وقف کرنا ہو تو ایک زبر کے ساتھ الف پڑھا جاتا ہے جیسے اَحَدًا سے اَحَدَا، اور گول تا (ة) پر وقف کرنا ہو تو وہ ہائے ساکنہ (ہْ) بن جاتی ہے جیسے جَارِيَةٌ سے جَارِيَهْ۔"
    ),

    // Page 38 & 39 (Book Page 41 & 42) - Takhti 18–21 Tashdeed, Madd & Muqatta'at
    NooraniQaidaMcqQuestion(
      id = "mcq_p38_i1",
      pageNumber = 38,
      bookPageNumber = 41,
      takhtiNumber = 19,
      difficulty = "INTERMEDIATE",
      questionEnglish = "According to Takhti 19 (Page 41 - Madd), what is the difference between Madd-e-Muttasil (جَآءَ) and Madd-e-Munfasil (فِيْٓ اَمْرِنَا)?",
      questionUrdu = "تختی نمبر ۱۹ (صفحہ ۴۱، مد کا بیان) کے مطابق مدِ متصل اور مدِ منفصل میں کیا فرق ہے؟",
      arabicPrompt = "جَآءَ (مد متصل) • فِيْٓ اَمْرِنَا (مد منفصل) • آلْـٰٔنَ (مد لازم)",
      options = listOf(
        "In Madd-e-Muttasil, Hamzah is in the SAME word after the Madd letter; in Madd-e-Munfasil, Hamzah is at the start of the NEXT word",
        "Madd-e-Muttasil is followed by Tashdeed; Madd-e-Munfasil is followed by Jazam",
        "There is no difference",
        "Madd-e-Muttasil is not stretched"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Page 41 defines: 1) Hamzah in the same word after Huruf-e-Maddah = Madd-e-Muttasil (Wajib). 2) Hamzah in the second word = Madd-e-Munfasil (Ja'iz). 3) Jazam or Tashdeed after Huruf-e-Maddah = Madd-e-Lazim.",
      explanationUrdu = "۱) حروفِ مدہ کے بعد ہمزہ اسی کلمہ میں ہو تو مدِ متصل (واجب) ہوگا۔ ۲) اگر ہمزہ دوسرے کلمہ میں ہو تو مدِ منفصل (جائز) ہوگا۔ ۳) بعد والے حرف پر جزم یا تشدید ہو تو مدِ لازم ہوگا۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p39_h1",
      pageNumber = 39,
      bookPageNumber = 42,
      takhtiNumber = 21,
      difficulty = "HARD",
      questionEnglish = "In Takhti 21 (Page 42 - Huruf-e-Muqatta'at), how are the disjointed Quranic letters like 'الم' or 'كهيعص' recited?",
      questionUrdu = "تختی نمبر ۲۱ (صفحہ ۴۲، حروفِ مقطعات) میں ’الم‘ یا ’كهيعص‘ کو کیسے پڑھا جاتا ہے؟",
      arabicPrompt = "الم • المص • الر • كهيعص • طه • طسم • يس • حـم • ق • ن",
      options = listOf(
        "By pronouncing each individual letter's full Arabic name with its proper Madd and Ghunnah rules (e.g., Alif-Laaam-Meeem)",
        "As regular joined words without stretching",
        "Silently without voice",
        "By translating them first"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Huruf-e-Muqatta'at at the start of Surahs are recited by spelling out each letter's name (Alif, Laam, Meem) while applying Madd-e-Lazim Harfi and Ghunnah/Ikhfa.",
      explanationUrdu = "حروفِ مقطعات میں ہر حرف کا پورا نام مدِ لازم اور غنہ و اخفاء کے قواعد کے ساتھ ادا کیا جاتا ہے (جیسے الف، لام، میم)۔"
    ),

    // Page 40 (Book Page 43) - Takhti 22 Mim Sakin Rules
    NooraniQaidaMcqQuestion(
      id = "mcq_p40_i1",
      pageNumber = 40,
      bookPageNumber = 43,
      takhtiNumber = 22,
      difficulty = "INTERMEDIATE",
      questionEnglish = "According to Takhti 22 (Page 43 - Rules of Mim Sakin), what are the three rules of Mim Sakin (مْ)?",
      questionUrdu = "تختی نمبر ۲۲ (صفحہ ۴۳، میم ساکن کے قاعدے) کے مطابق میم ساکن کے تین قاعدے کون سے ہیں؟",
      arabicPrompt = "اِلَيْكُمْ مُّرْسَلُوْنَ (ادغام شفوی) • تَرْمِيْهِمْ بِحِجَارَةٍ (اخفاء شفوی) • لَكُمْ دِيْنُكُمْ (اظہار شفوی)",
      options = listOf(
        "1) Idgham Shafawi (followed by م), 2) Ikhfa Shafawi (followed by ب), 3) Izhar Shafawi (followed by any other letter)",
        "1) Qalqalah, 2) Iqlab, 3) Madd",
        "1) Shamsi, 2) Qamari, 3) Waqf",
        "1) Fathah, 2) Kasrah, 3) Dammah"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Page 43 teaches: Mim Sakin + Mim = Idgham Shafawi (with Ghunnah); Mim Sakin + Ba = Ikhfa Shafawi (with Ghunnah); Mim Sakin + any other letter (especially Waaw/Fa) = Izhar Shafawi.",
      explanationUrdu = "۱) میم ساکن کے بعد م آئے تو ادغامِ شفوی، ۲) ب آئے تو اخفاءِ شفوی، ۳) ب اور م کے علاوہ کوئی اور حرف آئے تو اظہارِ شفوی ہوگا۔"
    ),

    // Page 41 (Book Page 44) - Takhti 23 Iqlab
    NooraniQaidaMcqQuestion(
      id = "mcq_p41_b1",
      pageNumber = 41,
      bookPageNumber = 44,
      takhtiNumber = 23,
      difficulty = "BASIC",
      questionEnglish = "According to Takhti 23 (Page 44 - Iqlab), when does Iqlab occur and how is it indicated in the script?",
      questionUrdu = "تختی نمبر ۲۳ (صفحہ ۴۴، اقلاب) کے مطابق اقلاب کب ہوتا ہے اور اس میں نون کو کس حرف سے بدلتے ہیں؟",
      arabicPrompt = "مَنْۢ بَخِلَ • مِنْۢ بَعْدِ • سَمِيْعٌۢ بَصِيْرٌ",
      options = listOf(
        "When Ba (ب) comes after Noon Sakin or Tanween; Noon is changed into a small Mim (م) with Ghunnah",
        "When Mim comes after Noon Sakin",
        "When Ra comes after Noon Sakin",
        "When Qaf comes after Noon Sakin"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "When Ba (ب) follows Tanween or Noon Sakin, the Noon sound converts into Mim (م) with Ghunnah and Ikhfa — called Iqlab (e.g., مَنْۢ بَخِلَ).",
      explanationUrdu = "تنوین یا نون ساکن کے بعد ب آئے تو نون کو م سے بدل کر غنہ اور اخفاء کے ساتھ پڑھیں گے، اس کو اقلاب کہتے ہیں۔"
    ),

    // Page 42 & 43 (Book Page 45 & 46) - Takhti 24 Idgham Yarmaloon
    NooraniQaidaMcqQuestion(
      id = "mcq_p42_i1",
      pageNumber = 42,
      bookPageNumber = 45,
      takhtiNumber = 24,
      difficulty = "INTERMEDIATE",
      questionEnglish = "According to Takhti 24 (Pages 45–46 - Idgham Yarmaloon), which 2 letters of 'يَرْمَلُوْنَ' have Idgham WITHOUT Ghunnah (Bila-Ghunnah), and which 4 have Idgham WITH Ghunnah?",
      questionUrdu = "تختی نمبر ۲۴ (صفحہ ۴۵ تا ۴۶، ادغام یرملون) کے مطابق کن ۲ حروف میں ادغام بلا غنہ اور کن ۴ حروف میں ادغام مع الغنہ ہوتا ہے؟",
      arabicPrompt = "مِنْ رَّبِّكَ • مِنْ لَّدُنْهُ (بلا غنہ) — مَنْ يَّقُوْلُ • مِنْ وَّالٍ • مِنْ مَّاءٍ • مِنْ نِّعْمَةٍ (مع الغنہ)",
      options = listOf(
        "Without Ghunnah: ر and ل (Ra, Lam); With Ghunnah: ي ، و ، م ، ن (Yawman)",
        "Without Ghunnah: ي and و; With Ghunnah: ر ، ل ، م ، ن",
        "All 6 letters are without Ghunnah",
        "All 6 letters are with Ghunnah"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Page 45 covers Idgham Bila-Ghunnah in Ra (ر) and Lam (ل); Page 46 covers Idgham Ma'al-Ghunnah in Ya, Waaw, Mim, and Noon (ي و م ن).",
      explanationUrdu = "حروفِ یرملون ۶ ہیں: ل اور ر کی صورت میں بغیر غنہ کے ملا کر پڑھیں گے (ادغام بلا غنہ)، اور ی و م ن (یومن) میں غنہ کے ساتھ ملا کر پڑھیں گے (ادغام مع الغنہ)۔"
    ),

    // Page 44 & 45 (Book Page 47 & 48) - Takhti 25 Rasm-ul-Khat
    NooraniQaidaMcqQuestion(
      id = "mcq_p44_h1",
      pageNumber = 44,
      bookPageNumber = 47,
      takhtiNumber = 25,
      difficulty = "HARD",
      questionEnglish = "According to Takhti 25 (Pages 47–48 - Rasm-ul-Khat), how is the word 'اَنَا' (I) read when continuing vs. stopping?",
      questionUrdu = "تختی نمبر ۲۵ (صفحہ ۴۷ تا ۴۸، رسم الخط) کے مطابق لفظ ’اَنَا‘ میں نون کے بعد والا الف وصل (ملا کر پڑھنے) اور وقف میں کیسے پڑھا جاتا ہے؟",
      arabicPrompt = "اَنَا ⬅ اَنَ (وصل میں) • صَلٰوةَ ⬅ صَلَاتَ • زَكٰوةَ ⬅ زَكَاتَ",
      options = listOf(
        "When continuing (Wasl), the Alif after Noon is NOT stretched (read as اَنَ); if breath breaks (Waqf), the Alif is read",
        "The Alif in اَنَا is always stretched for 4 Alifs",
        "The Noon is silent",
        "It is read as اِنَّ"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Pages 47–48 state: 'In the word اَنَا, the Alif after Noon is never read when continuing (read like اَنَ); if breath breaks on اَنَا, the Alif is pronounced.'",
      explanationUrdu = "لفظ اَنَا میں نون کے بعد کا الف ملا کر پڑھنے میں کہیں نہیں پڑھا جائے گا، اَنَ کی طرح پڑھیں گے؛ البتہ اَنَا پر اگر سانس ٹوٹے تو الف پڑھا جائے گا۔"
    ),

    // Page 46 (Book Page 49) - Takhti 26 Alamat-e-Waqf (Stopping Signs)
    NooraniQaidaMcqQuestion(
      id = "mcq_p46_i1",
      pageNumber = 46,
      bookPageNumber = 49,
      takhtiNumber = 26,
      difficulty = "INTERMEDIATE",
      questionEnglish = "According to Takhti 26 (Page 49 - Alamat-e-Waqf), what do the stopping signs 'مـ' (Mim), 'لا' (La), and 'سكته' (Saktah) mean?",
      questionUrdu = "تختی نمبر ۲۶ (صفحہ ۴۹، علاماتِ وقف) میں علامت ’مـ‘، ’لا‘ اور ’سکتہ‘ کا کیا حکم ہے؟",
      arabicPrompt = "○ • مـ • ط • ج • ز • ص • قف • صل • لا • سكته • وقفه",
      options = listOf(
        "'مـ' is Waqf-e-Lazim (must stop); 'لا' within a verse means do NOT stop; 'Saktah' means pause the voice briefly WITHOUT breaking breath",
        "'مـ' means never stop; 'لا' means must stop; 'Saktah' means take a deep breath",
        "All signs mean the exact same thing",
        "They indicate Sajdah Tilawat"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Page 49 table: 'مـ' = Waqf-e-Lazim (compulsory stop); 'لا' in the middle of a phrase = do not stop; 'Saktah' = pause voice briefly without breaking breath (وقفه has a slightly longer pause than Saktah without breaking breath).",
      explanationUrdu = "’مـ‘ وقفِ لازم کی علامت ہے اس پر ٹھہرنا ضروری ہے؛ عبارت کے درمیان ’لا‘ ہو وہاں ٹھہرنا جائز نہیں؛ ’سکتہ‘ میں تھوڑی دیر آواز بند کرے مگر سانس نہ ٹوٹنے پائے۔"
    ),

    // Page 47 & 48 (Book Page 50 & 51) - Takhti 27 Ijra-e-Qawa'id
    NooraniQaidaMcqQuestion(
      id = "mcq_p47_h1",
      pageNumber = 47,
      bookPageNumber = 50,
      takhtiNumber = 27,
      difficulty = "HARD",
      questionEnglish = "On Page 51 (Takhti 27 - Ijra-e-Qawa'id), which Quranic phrases demonstrate the rule of 'Saktah' (brief voice pause without breaking breath)?",
      questionUrdu = "صفحہ ۵۱ (تختی نمبر ۲۷، اجرائے قواعد) میں کن قرآنی الفاظ پر ’سکتہ‘ کی مشق کرائی گئی ہے؟",
      arabicPrompt = "عِوَجًا ۜ قَيِّمًا • كَلَّا بَلْ ۜ رَانَ • وَقِيْلَ مَنْ ۜ رَاقٍ",
      options = listOf(
        "عِوَجًا ۜ قَيِّمًا ، كَلَّا بَلْ ۜ رَانَ ، and وَقِيْلَ مَنْ ۜ رَاقٍ",
        "اَلْحَمْدُ لِلّٰهِ رَبِّ الْعٰلَمِيْنَ",
        "قُلْ هُوَ اللّٰهُ اَحَدٌ",
        "اِنَّآ اَعْطَيْنٰكَ الْكَوْثَرَ"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Page 51 explicitly marks 'سكتة' over عِوَجًا ۜ قَيِّمًا, كَلَّا بَلْ ۜ رَانَ, and وَقِيْلَ مَنْ ۜ رَاقٍ (and 'وقفة' over لِيَتَمَتَّعُوْا  سکتہ/وقفہ فَسَوْفَ يَعْلَمُوْنَ).",
      explanationUrdu = "صفحہ ۵۱ پر عِوَجًا قَيِّمًا، كَلَّا بَلْ رَانَ اور وَقِيْلَ مَنْ رَاقٍ پر لفظ ’سکتہ‘ لکھ کر اس کے عملی اجرا کی مشق کرائی گئی ہے۔"
    ),

    // Page 49–51 (Book Page 52–54) - Takhti 28 Kalimat-e-Islam & Iman
    NooraniQaidaMcqQuestion(
      id = "mcq_p49_b1",
      pageNumber = 49,
      bookPageNumber = 52,
      takhtiNumber = 28,
      difficulty = "BASIC",
      questionEnglish = "Which three Kalimas with Urdu translation are taught on Page 52 (Takhti 28 - Kalimat-e-Islam)?",
      questionUrdu = "تختی نمبر ۲۸ (صفحہ ۵۲، کلماتِ اسلام) پر کون سے تین کلمے مع ترجمہ درج ہیں؟",
      arabicPrompt = "كلمه طيبه • كلمه شهادت • كلمه تمجيد",
      options = listOf(
        "1) Kalima Tayyibah, 2) Kalima Shahadat, and 3) Kalima Tamjeed",
        "Only Kalima Radd-e-Kufr",
        "Surah Al-Fatihah and Surah Al-Baqarah",
        "Duas for Wudu"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Page 52 presents Kalima Tayyibah (لَا اِلٰهَ اِلَّا اللّٰهُ مُحَمَّدٌ رَّسُوْلُ اللّٰهِ), Kalima Shahadat (اَشْهَدُ اَنْ لَّآ اِلٰهَ...), and Kalima Tamjeed (سُبْحَانَ اللّٰهِ وَالْحَمْدُ لِلّٰهِ...) with full Urdu translations.",
      explanationUrdu = "صفحہ ۵۲ پر کلمہ طیبہ، کلمہ شہادت اور کلمہ تمجید مع اردو ترجمہ درج ہیں۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p50_i1",
      pageNumber = 50,
      bookPageNumber = 53,
      takhtiNumber = 28,
      difficulty = "INTERMEDIATE",
      questionEnglish = "On Page 53, after Kalima Tawheed and Kalima Istighfar, which famous prophetic supplication for forgiveness is taught as 'Kalima Sayyid-ul-Istighfar'?",
      questionUrdu = "صفحہ ۵۳ پر کلمہ توحید اور کلمہ استغفار کے بعد کون سی عظیم دعا درج ہے؟",
      arabicPrompt = "اَللّٰهُمَّ اَنْتَ رَبِّيْ لَآ اِلٰهَ اِلَّآ اَنْتَ خَلَقْتَنِيْ وَاَنَا عَبْدُكَ...",
      options = listOf(
        "Sayyid-ul-Istighfar (اَللّٰهُمَّ اَنْتَ رَبِّيْ لَآ اِلٰهَ اِلَّآ اَنْتَ خَلَقْتَنِيْ...)",
        "Dua-e-Qunoot",
        "Ayat-ul-Kursi",
        "Dua after Azan"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Pages 53–54 present Kalima Tawheed, Kalima Istighfar, and Kalima Sayyid-ul-Istighfar (اَللّٰهُمَّ اَنْتَ رَبِّيْ...), followed by Iman-e-Mujmal and Iman-e-Mufassal on Page 54.",
      explanationUrdu = "صفحہ ۵۳ پر کلمہ توحید، کلمہ استغفار اور کلمہ سید الاستغفار (اَللّٰهُمَّ اَنْتَ رَبِّيْ...) درج ہیں اور صفحہ ۵۴ پر ایمانِ مجمل اور ایمانِ مفصل ہیں۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p51_b1",
      pageNumber = 51,
      bookPageNumber = 54,
      takhtiNumber = 28,
      difficulty = "BASIC",
      questionEnglish = "On Page 54, which declaration of faith lists the 7 articles of faith (Allah, His Angels, His Books, His Messengers, the Last Day, Destiny of good & evil from Allah, and Resurrection after death)?",
      questionUrdu = "صفحہ ۵۴ پر وہ کون سا ایمان کا کلمہ ہے جس میں اللہ، فرشتوں، کتابوں، رسولوں، یومِ آخرت، تقدیر اور موت کے بعد دوبارہ زندہ ہونے کا اقرار ہے؟",
      arabicPrompt = "اٰمَنْتُ بِاللّٰهِ وَمَلٰٓئِكَتِهٖ وَكُتُبِهٖ وَرُسُلِهٖ وَالْيَوْمِ الْاٰخِرِ وَالْقَدْرِ خَيْرِهٖ وَشَرِّهٖ مِنَ اللّٰهِ تَعَالٰى وَالْبَعْثِ بَعْدَ الْمَوْتِ",
      options = listOf(
        "Iman-e-Mufassal (ایمانِ مفصل)",
        "Iman-e-Mujmal (ایمانِ مجمل)",
        "Kalima Tayyibah",
        "Tasbih-e-Fatimi"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Iman-e-Mufassal (detailed faith) declares belief in the 7 pillars of Iman, while Iman-e-Mujmal declares summary faith in Allah as He is with His Names and Attributes and accepting all His commands.",
      explanationUrdu = "ایمانِ مفصل میں ایمان کے ساتوں عقائد (اللہ، فرشتوں، کتابوں، رسولوں، یومِ آخرت، تقدیر اور بعث بعد الموت) کا تفصیلی اقرار ہے۔"
    ),

    // Page 52 & 53 (Book Page 55 & 56) - Azan & Iqamat Rules and Words
    NooraniQaidaMcqQuestion(
      id = "mcq_p52_h1",
      pageNumber = 52,
      bookPageNumber = 55,
      takhtiNumber = 28,
      difficulty = "HARD",
      questionEnglish = "According to Page 55 ('Common Mistakes in Azan & Iqamat'), which of the following is warned against as an error during Azan and Iqamat?",
      questionUrdu = "صفحہ ۵۵ (اذان و اقامت کی بعض غلطیاں) کے مطابق اذان و اقامت میں کن غلطیوں سے بچنے کی تاکید کی گئی ہے؟",
      arabicPrompt = "حَيَّ عَلَى الصَّلٰوةِ • قَدْ قَامَتِ الصَّلٰوةُ • اَللّٰهُ اَكْبَرُ",
      options = listOf(
        "Over-stretching 'Ilaha' or 'Rasool', dropping the 'Ayn' of 'Ala', or showing the vowel on the Ta of 'Salat' when stopping in 'Qad Qamatis-Salah'",
        "Turning the face right and left on Hayya 'alas-Salah and Hayya 'alal-Falah",
        "Reciting the last Kalima of Iqamat in one breath",
        "Saying As-Salatu Khayrun minan-Nawm in Fajr Azan"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Page 55 warns against stretching letters beyond Madd Tabi'i, dropping ع in عَلَى الصَّلٰوةِ, and pronouncing the vowel on ت when stopping on قَدْ قَامَتِ الصَّلٰوةُ.",
      explanationUrdu = "صفحہ ۵۵ میں تنبیہ کی گئی ہے کہ اِلٰهَ اور رَسُوْلُ میں حرفِ مدہ کو حد سے زیادہ بڑھانا، عَلَى الصَّلٰوةِ میں ع کو حذف کر دینا، اور قَدْ قَامَتِ الصَّلٰوةُ پر وقف میں ت کی حرکت ظاہر کرنا غلط ہے۔"
    ),
    NooraniQaidaMcqQuestion(
      id = "mcq_p53_b1",
      pageNumber = 53,
      bookPageNumber = 56,
      takhtiNumber = 28,
      difficulty = "BASIC",
      questionEnglish = "According to Page 56 (Words of Azan & Iqamat), what is added twice after 'Hayya 'alal-Falah' in the Fajr Azan, and what is added twice in Iqamat?",
      questionUrdu = "صفحہ ۵۶ (اذان و اقامت کے کلمات) کے مطابق فجر کی اذان اور اقامت میں حَيَّ عَلَى الْفَلَاحْ کے بعد دو دو مرتبہ کن کلمات کا اضافہ کیا جاتا ہے؟",
      arabicPrompt = "اَلصَّلٰوةُ خَيْرٌ مِّنَ النَّوْمِ (فجر کی اذان میں) • قَدْ قَامَتِ الصَّلٰوةُ (اقامت میں)",
      options = listOf(
        "Fajr Azan: 'As-Salatu khayrun minan-nawm' (twice); Iqamat: 'Qad qamatis-Salah' (twice)",
        "Fajr Azan: 'Qad qamatis-Salah'; Iqamat: 'Subhanallah'",
        "Nothing is added in either",
        "Both add 'Allahu Akbar' 10 times"
      ),
      correctOptionIndex = 0,
      explanationEnglish = "Page 56 concludes the book: In Fajr Azan, say 'اَلصَّلٰوةُ خَيْرٌ مِّنَ النَّوْمِ' twice after Hayya 'alal-Falah; in Iqamat, say 'قَدْ قَامَتِ الصَّلٰوةُ' twice.",
      explanationUrdu = "صفحہ ۵۶ پر درج ہے: فجر کی اذان میں حَيَّ عَلَى الْفَلَاحْ کے بعد اَلصَّلٰوةُ خَيْرٌ مِّنَ النَّوْمِ دو مرتبہ کہنا چاہئے اور اقامت میں قَدْ قَامَتِ الصَّلٰوةُ دو مرتبہ کہنا چاہئے۔"
    )
  )

  fun getQuestionsForDifficulty(difficulty: String): List<NooraniQaidaMcqQuestion> {
    val norm = difficulty.trim().uppercase()
    if (norm.isEmpty() || norm == "ALL") return allQuestions
    return allQuestions.filter { it.difficulty.equals(norm, ignoreCase = true) }
  }

  fun getQuestionsForPage(pageNumber: Int): List<NooraniQaidaMcqQuestion> {
    val exact = allQuestions.filter { it.pageNumber == pageNumber }
    if (exact.isNotEmpty()) return exact
    // Fallback to same Takhti if another page of the same Takhti is open
    return allQuestions.filter { kotlin.math.abs(it.pageNumber - pageNumber) <= 1 }
  }
}
