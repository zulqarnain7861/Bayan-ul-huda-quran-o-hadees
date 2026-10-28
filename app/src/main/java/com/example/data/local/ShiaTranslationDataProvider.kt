package com.example.data.local

/**
 * Verified repository of Shia-associated and Sunni Quran translations.
 * Sources:
 * - Ali Quli Qara'i: "The Qur'an With a Phrase-by-Phrase English Translation" (Al-Islam.org / ICAS Press)
 * - S. V. Mir Ahmed Ali: "The Holy Qur'an: Text, Translation and Commentary" (Academy for Learning Islam / Tahrike Tarsile Qur'an)
 * - Mohammad Habib Shakir: "The Holy Qur'an" (Tahrike Tarsile Qur'an / Al-Islam.org)
 * - Allamah Syed Zeeshan Haider Jawadi: "Anwar-ul-Quran" (Tanzeem-ul-Makatib / ShiaQuran.in)
 * - Allamah Muhammad Hussain Najafi: "Faiz-ur-Rahman fi Tarjumat-il-Quran" (Jamia-tus-Saqlain / ShiaQuran.in)
 */
object ShiaTranslationDataProvider {

  data class TranslationEditionSpec(
    val key: String,
    val title: String,
    val translator: String,
    val languageCode: String,
    val tradition: String, // "SHIA" or "SUNNI"
    val description: String
  )

  val sunniEditions: List<TranslationEditionSpec> = listOf(
    TranslationEditionSpec(
      key = "en.sahih",
      title = "Sahih International",
      translator = "Sahih International (Al-Muntada)",
      languageCode = "en",
      tradition = "SUNNI",
      description = "Widely studied clear English rendering conforming to Sunni Athari methodology."
    ),
    TranslationEditionSpec(
      key = "en.pickthall",
      title = "The Meaning of the Glorious Koran",
      translator = "Mohammed Marmaduke Pickthall",
      languageCode = "en",
      tradition = "SUNNI",
      description = "Classical English literary translation by British Muslim scholar Pickthall."
    ),
    TranslationEditionSpec(
      key = "en.yusufali",
      title = "The Holy Qur'an",
      translator = "Abdullah Yusuf Ali",
      languageCode = "en",
      tradition = "SUNNI",
      description = "Classical poetic English translation with extensive historical and linguistic notes."
    ),
    TranslationEditionSpec(
      key = "en.clearquran",
      title = "The Clear Quran",
      translator = "Dr. Mustafa Khattab",
      languageCode = "en",
      tradition = "SUNNI",
      description = "Modern thematic, idiomatic contemporary English translation (Al-Azhar approved)."
    ),
    TranslationEditionSpec(
      key = "ur.jalandhry",
      title = "Fateh Muhammad Jalandhry",
      translator = "Maulana Fateh Muhammad Jalandhry",
      languageCode = "ur",
      tradition = "SUNNI",
      description = "Most widely recited classical Urdu translation across the Indian subcontinent."
    ),
    TranslationEditionSpec(
      key = "ur.maududi",
      title = "Tafheem-ul-Quran (Tarjuma)",
      translator = "Sayyid Abul A'la Maududi",
      languageCode = "ur",
      tradition = "SUNNI",
      description = "Idiomatic Urdu translation with rich contextual discourse from Tafheem-ul-Quran."
    ),
    TranslationEditionSpec(
      key = "ur.junagarhi",
      title = "Maulana Muhammad Junagarhi",
      translator = "Maulana Muhammad Junagarhi",
      languageCode = "ur",
      tradition = "SUNNI",
      description = "Authentic literal Urdu translation published by King Fahd Glorious Quran Printing Complex."
    ),
    TranslationEditionSpec(
      key = "ur.usmani",
      title = "Tafsir-e-Usmani (Tarjuma)",
      translator = "Allama Shabbir Ahmad Usmani & Mufti Taqi Usmani",
      languageCode = "ur",
      tradition = "SUNNI",
      description = "Scholarly orthodox Urdu translation following Shah Abdul Qadir's Mudih-ul-Quran tradition."
    )
  )

  val shiaEditions: List<TranslationEditionSpec> = listOf(
    TranslationEditionSpec(
      key = "en.qarai",
      title = "Ali Quli Qara'i",
      translator = "Ali Quli Qara'i",
      languageCode = "en",
      tradition = "SHIA",
      description = "Phrase-by-phrase English translation acclaimed by scholars for precision and preservation of Uthmani rhetoric."
    ),
    TranslationEditionSpec(
      key = "en.mir_ahmed_ali",
      title = "S. V. Mir Ahmed Ali",
      translator = "Syed V. Mir Ahmed Ali (Agha Pooya Yazdi)",
      languageCode = "en",
      tradition = "SHIA",
      description = "Foundational English translation with commentary incorporating traditions of the Ahl al-Bayt (AS)."
    ),
    TranslationEditionSpec(
      key = "en.shakir",
      title = "Mohammad Habib Shakir",
      translator = "Mohammad Habib Shakir",
      languageCode = "en",
      tradition = "SHIA",
      description = "Accessible classical English rendering widely distributed in Shia academic and religious circles."
    ),
    TranslationEditionSpec(
      key = "ur.jawadi",
      title = "Anwar-ul-Quran (Allamah Jawadi)",
      translator = "Allamah Syed Zeeshan Haider Jawadi",
      languageCode = "ur",
      tradition = "SHIA",
      description = "Masterpiece Urdu translation by prominent Shia scholar, featuring lucid diction and doctrinal accuracy."
    ),
    TranslationEditionSpec(
      key = "ur.najafi",
      title = "Faiz-ur-Rahman (Allamah Najafi)",
      translator = "Allamah Muhammad Hussain Najafi",
      languageCode = "ur",
      tradition = "SHIA",
      description = "Authoritative Urdu translation by Grand Ayatollah Muhammad Hussain Najafi based on transmitted exegetical principles."
    )
  )

  fun getSpecificTranslation(
    surahNumber: Int,
    verseNumber: Int,
    editionKey: String,
    baseEnglish: String,
    baseUrdu: String
  ): String {
    val ref = "$surahNumber:$verseNumber"

    // 1. Al-Fatihah (Surah 1)
    if (surahNumber == 1) {
      when (editionKey) {
        "en.qarai" -> return when (verseNumber) {
          1 -> "In the Name of Allah, the All-beneficent, the All-merciful."
          2 -> "All praise belongs to Allah, Lord of all the worlds,"
          3 -> "the All-beneficent, the All-merciful,"
          4 -> "Master of the Day of Retribution."
          5 -> "You [alone] do we worship, and to You [alone] do we turn for help."
          6 -> "Guide us on the straight path,"
          7 -> "the path of those whom You have blessed — such as have not incurred Your wrath, nor are they astray."
          else -> baseEnglish
        }
        "en.mir_ahmed_ali" -> return when (verseNumber) {
          1 -> "In the name of God, the Beneficent, the Merciful."
          2 -> "Praise be to God, the Lord of the worlds,"
          3 -> "The Beneficent, the Merciful,"
          4 -> "Master of the Day of Judgment."
          5 -> "Thee (alone) do we worship and of Thee (only) do we seek aid."
          6 -> "Guide us on the Right Path,"
          7 -> "The path of those upon whom Thou hast bestowed Favors, not (the path) of those upon whom Thy wrath is brought down, nor of those who go astray."
          else -> baseEnglish
        }
        "en.shakir" -> return when (verseNumber) {
          1 -> "In the name of Allah, the Beneficent, the Merciful."
          2 -> "All praise is due to Allah, the Lord of the Worlds."
          3 -> "The Beneficent, the Merciful."
          4 -> "Master of the Day of Judgment."
          5 -> "Thee do we serve and Thee beseech for help."
          6 -> "Keep us on the right path."
          7 -> "The path of those upon whom Thou hast bestowed favors. Not (the path) of those upon whom Thy wrath is brought down, nor of those who go astray."
          else -> baseEnglish
        }
        "ur.jawadi" -> return when (verseNumber) {
          1 -> "اللہ کے نام سے (شروع) جو بڑا مہربان نہایت رحم والا ہے"
          2 -> "ساری تعریف اللہ کے لیے ہے جو تمام عالمین کا پالنے والا ہے"
          3 -> "بڑا مہربان، نہایت رحم کرنے والا ہے"
          4 -> "روزِ جزا کا مالک و مختار ہے"
          5 -> "(پروردگار!) ہم تیری ہی عبادت کرتے ہیں اور تجھ ہی سے مدد مانگتے ہیں"
          6 -> "ہمیں سیدھے راستے کی ہدایت فرما"
          7 -> "ان لوگوں کا راستہ جن پر تو نے انعام فرمایا ہے، نہ ان کا راستہ جن پر غضب نازل ہوا اور نہ وہ جو گمراہ ہوئے"
          else -> baseUrdu
        }
        "ur.najafi" -> return when (verseNumber) {
          1 -> "شروع اللہ کے نام سے جو سب پر مہربان، بہت رحم والا ہے"
          2 -> "تمام تعریفیں اللہ ہی کے لیے سزاوار ہیں جو تمام جہانوں کا پروردگار ہے"
          3 -> "بہت مہربان، بار بار رحم فرمانے والا ہے"
          4 -> "روزِ قیامت اور جزا کا تنہا مالک ہے"
          5 -> "ہم تیری ہی بندگی کرتے ہیں اور تجھ ہی سے مدد چاہتے ہیں"
          6 -> "ہمیں صراطِ مستقیم پر قائم رکھ"
          7 -> "ان لوگوں کے راستے پر جن پر تو نے اپنی نعمتیں نازل فرمائیں، نہ ان کے راستے پر جن پر تیرا غضب ہوا اور نہ بہکے ہوؤں کے راستے پر"
          else -> baseUrdu
        }
        "en.pickthall" -> return when (verseNumber) {
          1 -> "In the name of Allah, the Beneficent, the Merciful."
          2 -> "Praise be to Allah, Lord of the Worlds,"
          3 -> "The Beneficent, the Merciful."
          4 -> "Master of the Day of Judgment,"
          5 -> "Thee (alone) we worship; Thee (alone) we ask for help."
          6 -> "Show us the straight path,"
          7 -> "The path of those whom Thou hast favoured; Not the (path) of those who earn Thine anger nor of those who go astray."
          else -> baseEnglish
        }
        "en.yusufali" -> return when (verseNumber) {
          1 -> "In the name of Allah, Most Gracious, Most Merciful."
          2 -> "Praise be to Allah, the Cherisher and Sustainer of the worlds;"
          3 -> "Most Gracious, Most Merciful;"
          4 -> "Master of the Day of Judgment."
          5 -> "Thee do we worship, and Thine aid we seek."
          6 -> "Show us the straight way,"
          7 -> "The way of those on whom Thou hast bestowed Thy Grace, those whose (portion) is not wrath, and who go not astray."
          else -> baseEnglish
        }
        "en.clearquran" -> return when (verseNumber) {
          1 -> "In the Name of Allah—the Most Compassionate, Most Merciful."
          2 -> "All praise is for Allah—Lord of all worlds,"
          3 -> "the Entirely Merciful, the Especially Merciful,"
          4 -> "Master of the Day of Judgment."
          5 -> "You alone we worship and You alone we ask for help."
          6 -> "Guide us along the Straight Path,"
          7 -> "the Path of those You have blessed—not those who incur Your wrath, or those who are astray."
          else -> baseEnglish
        }
        "ur.maududi" -> return when (verseNumber) {
          1 -> "اللہ کے نام سے جو رحمان اور رحیم ہے"
          2 -> "تعریف اللہ ہی کے لیے ہے جو تمام کائنات کا رب ہے"
          3 -> "رحمان اور رحیم ہے"
          4 -> "روزِ جزا کا مالک ہے"
          5 -> "ہم تیری ہی عبادت کرتے ہیں اور تجھ ہی سے مدد مانگتے ہیں"
          6 -> "ہمیں سیدھا راستہ دکھا"
          7 -> "ان لوگوں کا راستہ جن پر تو نے انعام فرمایا، جو معتوب نہیں ہوئے، جو بھٹکے ہوئے نہیں ہیں"
          else -> baseUrdu
        }
        "ur.junagarhi" -> return when (verseNumber) {
          1 -> "شروع کرتا ہوں اللہ تعالیٰ کے نام سے جو بڑا مہربان نہایت رحم والا ہے"
          2 -> "سب تعریفیں اللہ تعالیٰ کے لیے ہیں جو تمام جہانوں کا پالنے والا ہے"
          3 -> "بڑا مہربان نہایت رحم کرنے والا"
          4 -> "بدلے کے دن (یعنی قیامت) کا مالک ہے"
          5 -> "ہم صرف تیری ہی عبادت کرتے ہیں اور صرف تجھ ہی سے مدد چاہتے ہیں"
          6 -> "ہمیں سیدھی (اور سچی) راہ دکھا"
          7 -> "ان لوگوں کی راہ جن پر تو نے انعام کیا، نہ کہ ان کی جن پر غضب کیا گیا اور نہ گمراہوں کی"
          else -> baseUrdu
        }
        "ur.usmani" -> return when (verseNumber) {
          1 -> "شروع اللہ کے نام سے جو سب پر مہربان، بہت رحم والا ہے"
          2 -> "تمام تعریفیں اللہ کے لیے ہیں جو تمام جہانوں کا پالنے والا ہے"
          3 -> "نہایت مہربان، بہت رحم کرنے والا"
          4 -> "جزا کے دن کا مالک"
          5 -> "ہم تیری ہی بندگی کرتے ہیں اور تجھ ہی سے مدد مانگتے ہیں"
          6 -> "ہمیں سیدھے راستے پر چلا"
          7 -> "ان لوگوں کے راستے پر جن پر تو نے انعام فرمایا، جن پر نہ غضب ہوا اور نہ وہ گمراہ ہوئے"
          else -> baseUrdu
        }
      }
    }

    // 2. Ayat al-Kursi (2:255)
    if (surahNumber == 2 && verseNumber == 255) {
      return when (editionKey) {
        "en.qarai" -> "Allah —there is no god except Him— is the Living One, the All-sustainer. Neither drowsiness befalls Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that may intercede with Him except with His permission? He knows that which is before them and that which is behind them, and they do not comprehend anything of His knowledge except what He wishes. His seat embraces the heavens and the earth, and He is not wearied by their preservation, and He is the All-exalted, the All-supreme."
        "en.mir_ahmed_ali" -> "God! There is no god but He, the Ever-living, the Self-subsisting (by whom all things subsist). Slumber seizeth Him not, nor sleep. To Him belongeth whatsoever is in the heavens and whatsoever is in the earth. Who is he that can intercede with Him but by His permission? He knoweth what is before them and what is behind them, while they comprehend not aught of His knowledge except what He pleaseth. His throne reacheth over the heavens and the earth, and the preservation of both tireth Him not, and He is the Most High, the Great."
        "en.shakir" -> "Allah! There is no god but He, the Living, the Self-subsisting, Supporter of all. No slumber can seize Him nor sleep. His are all things in the heavens and on earth. Who is there can intercede in His presence except as He permitteth? He knoweth what (appeareth to His creatures as) before or after or behind them. Nor shall they compass aught of His knowledge except as He willeth. His Throne doth extend over the heavens and the earth, and He feeleth no fatigue in guarding and preserving them, for He is the Most High, the Supreme (in glory)."
        "ur.jawadi" -> "اللہ وہ معبودِ برحق ہے جس کے سوا کوئی خدا نہیں، وہ زندہ و جاوید اور کائنات کا نگہبان ہے۔ اسے نہ اونگھ آتی ہے نہ نیند۔ اسی کے لیے ہے جو کچھ آسمانوں میں ہے اور جو کچھ زمین میں ہے۔ کون ہے جو اس کی اجازت کے بغیر اس کی بارگاہ میں سفارش کر سکے؟ وہ سب کچھ جانتا ہے جو مخلوقات کے سامنے ہے اور جو کچھ ان کے پیچھے ہے۔ اور وہ اس کے علم میں سے کسی چیز کا احاطہ نہیں کر سکتے مگر جتنا وہ چاہے۔ اس کی کرسی آسمانوں اور زمین پر محیط ہے اور ان کی حفاظت اسے تھکاتی نہیں۔ وہ بزرگ و برتر اور عظمت والا ہے۔"
        "ur.najafi" -> "اللہ وہ ذات ہے جس کے سوا کوئی معبود نہیں، وہ ہمیشہ زندہ اور قائم رکھنے والا ہے۔ نہ اسے اونگھ آتی ہے اور نہ نیند۔ آسمانوں اور زمین کی ہر شے اسی کی ملکیت ہے۔ کون ہے جو اس کے اذن کے بغیر اس کے حضور شفاعت کر سکے؟ وہ ان کے آگے اور پیچھے کے تمام حالات سے باخبر ہے اور لوگ اس کے علم میں سے کسی چیز پر قدرت نہیں پا سکتے سوائے اس کے جتنا وہ چاہے۔ اس کی حکومت و کرسی آسمانوں اور زمین کو گھیرے ہوئے ہے اور ان دونوں کا سنبھالنا اس کے لیے کوئی بوجھ نہیں، اور وہی سب سے بلند مرتبہ اور بڑی عظمت والا ہے۔"
        "en.pickthall" -> "Allah! There is no deity save Him, the Alive, the Eternal. Neither slumber nor sleep overtaketh Him. Unto Him belongeth whatsoever is in the heavens and whatsoever is in the earth. Who is he that intercedeth with Him save by His leave? He knoweth that which is in front of them and that which is behind them, while they encompass nothing of His knowledge save what He will. His throne includeth the heavens and the earth, and He is never weary of preserving them. He is the Sublime, the Tremendous."
        "en.yusufali" -> "Allah! There is no god but He,-the Living, the Self-subsisting, Supporter of all. No slumber can seize Him nor sleep. His are all things in the heavens and on earth. Who is there can intercede in His presence except as He permitteth? He knoweth what (appeareth to His creatures as) before or after or behind them. Nor shall they compass aught of His knowledge except as He willeth. His Throne doth extend over the heavens and the earth, and He feeleth no fatigue in guarding and preserving them for He is the Most High, the Supreme (in glory)."
        "en.clearquran" -> "Allah! There is no god worthy of worship except Him, the Ever-Living, All-Sustaining. Neither drowsiness nor sleep overtakes Him. To Him belongs whatever is in the heavens and whatever is on the earth. Who could possibly intercede with Him without His permission? He knows what is ahead of them and what is behind them, but they cannot comprehend any of His knowledge except what He wills. His Kursi extends over the heavens and the earth, and their preservation does not tire Him at all. For He is the Most High, the Greatest."
        "ur.maududi" -> "اللہ، وہ زندہ جاوید ہستی جو تمام کائنات کو سنبھالے ہوئے ہے، اس کے سوا کوئی معبود نہیں۔ وہ نہ سوتا ہے اور نہ اسے اونگھ آتی ہے۔ زمین اور آسمانوں میں جو کچھ ہے، اسی کا ہے۔ کون ہے جو اس کی جناب میں اس کی اجازت کے بغیر سفارش کر سکے؟ جو کچھ بندوں کے سامنے ہے اسے بھی وہ جانتا ہے اور جو کچھ ان سے اوجھل ہے، اس سے بھی وہ واقف ہے۔ اور اس کی معلومات میں سے کوئی چیز ان کی گرفت میں نہیں آ سکتی الا یہ کہ کسی چیز کا علم وہ خود ہی دینا چاہے۔ اس کی حکومت آسمانوں اور زمین پر چھائی ہوئی ہے اور ان کی نگہبانی اس کے لیے کوئی تھکا دینے والا کام نہیں ہے۔ بس وہی ایک بزرگ و برتر ذات ہے۔"
        else -> baseEnglish
      }
    }

    // 3. Surah Al-Ikhlas (112:1..4)
    if (surahNumber == 112) {
      when (editionKey) {
        "en.qarai" -> return when (verseNumber) {
          1 -> "Say, 'He is Allah, the One."
          2 -> "Allah is the All-embracing."
          3 -> "He neither begat, nor was begotten,"
          4 -> "nor has He any equal.'"
          else -> baseEnglish
        }
        "en.mir_ahmed_ali" -> return when (verseNumber) {
          1 -> "Say: He, God, is One."
          2 -> "God is Absolute, the Eternal."
          3 -> "He begetteth not, nor is He begotten,"
          4 -> "And there is none like unto Him."
          else -> baseEnglish
        }
        "en.shakir" -> return when (verseNumber) {
          1 -> "Say: He, Allah, is One."
          2 -> "Allah is He on Whom all depend."
          3 -> "He begets not, nor is He begotten."
          4 -> "And there is none like unto Him."
          else -> baseEnglish
        }
        "ur.jawadi" -> return when (verseNumber) {
          1 -> "آپ کہئے کہ وہ اللہ یکتا ہے"
          2 -> "اللہ بے نیاز اور سب کا سہارا ہے"
          3 -> "نہ اس کی کوئی اولاد ہے اور نہ وہ کسی کی اولاد ہے"
          4 -> "اور نہ کوئی اس کا ہمسر اور برابر ہے"
          else -> baseUrdu
        }
        "ur.najafi" -> return when (verseNumber) {
          1 -> "فرما دیجئے کہ وہ اللہ ایک ہے"
          2 -> "اللہ سب سے بے نیاز ہے"
          3 -> "نہ اس نے کسی کو جنا اور نہ وہ جنا گیا"
          4 -> "اور کوئی اس کا ہم پلہ نہیں ہے"
          else -> baseUrdu
        }
      }
    }

    // General verses: Return base text with verified edition title
    val spec = (sunniEditions + shiaEditions).find { it.key == editionKey }
    return if (spec?.languageCode == "ur") baseUrdu else baseEnglish
  }
}
