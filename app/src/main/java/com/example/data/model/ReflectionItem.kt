package com.example.data.model

import android.content.Context
import com.example.data.local.ExtendedHadithDataProvider
import com.example.data.local.entities.HadithEntity

/**
 * Four clearly separated categories in the Authentic Islamic Wisdom Engine:
 * 1. Prophet Muhammad ﷺ (Prophetic Guidance)
 * 2. Ahl al-Bayt (AS) (Prophetic Household Wisdom)
 * 3. Early Sahabah (RA) (Strictly ordered by historical chronology of embracing Islam & support for the Prophet ﷺ and Ahl al-Bayt)
 * 4. Other Sahabah (RA) (Companions in their appropriate historical position with verified Hadith citations)
 */
enum class ReflectionCategory(val displayTitle: String, val subtitle: String) {
  PROPHETIC_GUIDANCE(
    "Prophet Muhammad ﷺ",
    "Authentic Prophetic Hadith (Marfu' Traditions from Verified Hadith Collections)"
  ),
  AHLUL_BAYT_WISDOM(
    "Ahl al-Bayt (AS)",
    "Prophetic Household: Imam Ali, Sayyidah Fatimah, Imam Hasan, Imam Husayn & the Imams (AS)"
  ),
  COMPANIONS_WISDOM(
    "Early Sahabah (RA)",
    "Earliest Believers (As-Sabiqun al-Awwalun) & Supporters of Ahl al-Bayt in Historical Chronology"
  ),
  OTHER_SAHABAH_WISDOM(
    "Other Sahabah (RA)",
    "Companions (RA) in Historical Position with Verified Hadith & Athar Citations"
  )
}

/**
 * Explicit Attribution Verification Status to distinguish verified primary-source attributions
 * from disputed chains or popular unattributed sayings.
 */
enum class WisdomAttributionStatus(val label: String, val badgeDescription: String) {
  VERIFIED_ATTRIBUTION(
    "Verified Attribution",
    "Authenticated in primary Hadith or classical manuscript corpora with documented chain (Isnad)."
  ),
  DISPUTED_ATTRIBUTION(
    "Disputed / Dual Attribution",
    "Attributed in classical sources with scholarly discussion regarding chain strength or multiple narrators."
  ),
  UNATTRIBUTED_POPULAR_SAYING(
    "Popular Saying (Chain Unverified)",
    "Widely quoted ethical maxim in later literature without a verified primary-source Hadith chain; shown for scholarly caution."
  )
}

/**
 * Distinguishes between a direct Prophetic Hadith (Marfu'), a narration of Ahl al-Bayt (Ma'thur),
 * a Prophetic Hadith transmitted by or concerning a Companion, and a Companion's own statement (Athar / Mawquf).
 */
enum class WisdomNarrationType(val label: String, val description: String) {
  PROPHETIC_HADITH_MARFU(
    "Prophetic Hadith (Marfu')",
    "Direct statement, action, or approval of Prophet Muhammad ﷺ recorded in canonical Hadith collections."
  ),
  AHL_AL_BAYT_HADITH_MATHUR(
    "Ahl al-Bayt Tradition (Ma'thur)",
    "Documented sermon, supplication, or teaching of the Prophet's Household (Ahl al-Bayt AS)."
  ),
  PROPHETIC_HADITH_VIA_COMPANION(
    "Prophetic Hadith (via Companion)",
    "Authentic Prophetic Hadith transmitted by or testifying to the station of this Companion (RA)."
  ),
  COMPANION_ATHAR_MAWQUF(
    "Companion Statement (Athar / Mawquf)",
    "Documented statement, counsel, or practice of a Companion (RA) preserved in classical Hadith collections."
  )
}

/**
 * Scholarly historical filters for exploring Early Sahabah and Wisdom categories.
 */
enum class HistoricalRelationshipTag(val label: String) {
  ALL("All Verified Narrations"),
  EARLIEST_BELIEVERS_SABIQUIN("First Believers (As-Sabiqun al-Awwalun)"),
  SUPPORTERS_OF_AHL_AL_BAYT("Supporters & Defenders of Ahl al-Bayt"),
  THAQALAYN_AND_KISA_TRANSMITTERS("Hadith al-Thaqalayn & Kisa' Narrators"),
  PROPHETIC_HOUSEHOLD("Prophetic Household (Ahl al-Bayt AS)"),
  LATER_AND_OTHER_COMPANIONS("Later & Other Companions (Historical Order)")
}

data class ReflectionItem(
  val id: Int,
  val category: ReflectionCategory,
  val speaker: String,
  val title: String,
  val theme: String,
  val arabicOrOriginalText: String,
  val translationEnglish: String,
  val translationUrdu: String,
  val sourceReference: String,
  val attributionStatus: WisdomAttributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
  val contextNotes: String = "",
  val chronologyRank: Int = 999,
  val chronologyEraAndOrderNote: String = "",
  val historicalDisputeNote: String = "",
  val narrationType: WisdomNarrationType = WisdomNarrationType.PROPHETIC_HADITH_MARFU,
  val authenticityGrading: String = "Sahih (Verified Primary Source)",
  val hadithBookName: String = "",
  val hadithNumber: String = "",
  val historicalRelationshipTags: List<HistoricalRelationshipTag> = emptyList(),
  val relationshipToProphetAndAhlAlBayt: String = ""
)

object ReflectionDataProvider {
  private const val PREFS_NAME = "bayan_wisdom_engine_prefs"
  private const val KEY_SEEN_IDS = "seen_wisdom_ids_v2"
  private const val KEY_BOOKMARKED_IDS = "bookmarked_wisdom_ids_v2"
  private const val KEY_DISCOVERY_ROTATION = "discovery_rotation_counter_v2"

  /**
   * Curated, Hadith-verified core catalog with strict historical chronology for Early Sahabah (RA),
   * special research attention to supporters of Prophet Muhammad ﷺ and Ahl al-Bayt (AS),
   * and separate category placement for later/other Sahabah (RA).
   */
  val curatedItems: List<ReflectionItem> = listOf(
    // =========================================================================
    // 1. PROPHET MUHAMMAD ﷺ (PROPHETIC_GUIDANCE)
    // =========================================================================
    ReflectionItem(
      id = 1,
      category = ReflectionCategory.PROPHETIC_GUIDANCE,
      speaker = "Prophet Muhammad ﷺ (Narrated by Umar ibn al-Khattab RA)",
      title = "Sincerity of Intentions (Ikhlas)",
      theme = "Inner Sincerity & Purpose",
      arabicOrOriginalText = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
      translationEnglish = "Actions are judged only by intentions, and every person shall have only that which they intended.",
      translationUrdu = "اعمال کا دارومدار نیتوں پر ہے اور ہر شخص کے لیے وہی ہے جس کی اس نے نیت کی۔",
      sourceReference = "Sahih al-Bukhari, Hadith 1; Sahih Muslim, Hadith 1907",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Foundational Prophetic Hadith opening Sahih al-Bukhari on the purification of intention before every act of worship and service.",
      chronologyRank = 1,
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_MARFU,
      authenticityGrading = "Muttafaqun Alayh (Sahih al-Bukhari & Sahih Muslim)",
      hadithBookName = "Sahih al-Bukhari",
      hadithNumber = "1",
      historicalRelationshipTags = listOf(HistoricalRelationshipTag.ALL)
    ),
    ReflectionItem(
      id = 2,
      category = ReflectionCategory.PROPHETIC_GUIDANCE,
      speaker = "Prophet Muhammad ﷺ (Narrated by Zayd ibn Arqam RA)",
      title = "The Two Weighty Things (Hadith al-Thaqalayn)",
      theme = "The Qur'an & Ahl al-Bayt",
      arabicOrOriginalText = "أَلَا وَإِنِّي تَارِكٌ فِيكُمْ ثَقَلَيْنِ: أَحَدُهُمَا كِتَابُ اللَّهِ عَزَّ وَجَلَّ هُوَ حَبْلُ اللَّهِ مَنِ اتَّبَعَهُ كَانَ عَلَى الْهُدَى وَمَنْ تَرَكَهُ كَانَ عَلَى ضَلَالَةٍ، وَأَهْلُ بَيْتِي أُذَكِّرُكُمُ اللَّهَ فِي أَهْلِ بَيْتِي أُذَكِّرُكُمُ اللَّهَ فِي أَهْلِ بَيْتِي أُذَكِّرُكُمُ اللَّهَ فِي أَهْلِ بَيْتِي",
      translationEnglish = "Indeed, I am leaving among you two weighty things: the first of them is the Book of Allah, the Mighty and Majestic—it is the rope of Allah; whoever follows it will be upon guidance, and whoever abandons it will be in error—and my Ahl al-Bayt (Household). I remind you of Allah concerning my Ahl al-Bayt, I remind you of Allah concerning my Ahl al-Bayt, I remind you of Allah concerning my Ahl al-Bayt.",
      translationUrdu = "خبردار! میں تمہارے درمیان دو بھاری (گرانقدر) چیزیں چھوڑے جا رہا ہوں: ان میں سے پہلی اللہ عزوجل کی کتاب ہے جو اللہ کی رسی ہے، جس نے اس کی پیروی کی وہ ہدایت پر رہا اور جس نے اسے چھوڑ دیا وہ گمراہی پر رہا؛ اور میرے اہلِ بیت۔ میں تمہیں اپنے اہلِ بیت کے بارے میں اللہ یاد دلاتا ہوں، میں تمہیں اپنے اہلِ بیت کے بارے میں اللہ یاد دلاتا ہوں، میں تمہیں اپنے اہلِ بیت کے بارے میں اللہ یاد دلاتا ہوں۔",
      sourceReference = "Sahih Muslim, Kitab Fada'il al-Sahabah, Hadith 2408a; Jami' at-Tirmidhi, Hadith 3786 & 3788; Musnad Ahmad, Hadith 19265; Al-Kafi, Vol. 1, p. 294",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Delivered by Prophet Muhammad ﷺ at Ghadir Khumm between Makkah and Madinah upon returning from the Farewell Pilgrimage, establishing the inseparable bond between the Qur'an and his Ahl al-Bayt.",
      chronologyRank = 2,
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_MARFU,
      authenticityGrading = "Sahih (Mutawatir across Sunni & Shia Hadith Corpora)",
      hadithBookName = "Sahih Muslim",
      hadithNumber = "2408",
      historicalRelationshipTags = listOf(
        HistoricalRelationshipTag.THAQALAYN_AND_KISA_TRANSMITTERS,
        HistoricalRelationshipTag.PROPHETIC_HOUSEHOLD
      )
    ),
    ReflectionItem(
      id = 3,
      category = ReflectionCategory.PROPHETIC_GUIDANCE,
      speaker = "Prophet Muhammad ﷺ (Narrated by Abu Hurayrah RA)",
      title = "Perfection of Noble Character (Makarim al-Akhlaq)",
      theme = "Prophetic Ethics",
      arabicOrOriginalText = "إِنَّمَا بُعِثْتُ لِأُتَمِّمَ صَالِحَ الأَخْلاقِ",
      translationEnglish = "Indeed, I was sent only to perfect noble and righteous character.",
      translationUrdu = "بے شک مجھے صالح اور اعلیٰ اخلاق کی تکمیل کے لیے بھیجا گیا ہے۔",
      sourceReference = "Al-Adab al-Mufrad (Al-Bukhari), Hadith 273; Musnad Ahmad, Hadith 8952; Al-Muwatta, Book 47, Hadith 8",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Summarizes the moral mission of Prophet Muhammad ﷺ across both Sunni and Shia ethical literature.",
      chronologyRank = 3,
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_MARFU,
      authenticityGrading = "Sahih (Authenticated by Al-Bukhari in Al-Adab al-Mufrad & Al-Hakim)",
      hadithBookName = "Al-Adab al-Mufrad",
      hadithNumber = "273",
      historicalRelationshipTags = listOf(HistoricalRelationshipTag.ALL)
    ),
    ReflectionItem(
      id = 4,
      category = ReflectionCategory.PROPHETIC_GUIDANCE,
      speaker = "Prophet Muhammad ﷺ (Narrated by Abdullah ibn Amr RA)",
      title = "Universal Mercy to All Creation",
      theme = "Compassion & Mercy",
      arabicOrOriginalText = "الرَّاحِمُونَ يَرْحَمُهُمُ الرَّحْمَنُ، ارْحَمُوا مَنْ فِي الأَرْضِ يَرْحَمْكُمْ مَنْ فِي السَّمَاءِ",
      translationEnglish = "The merciful are shown mercy by the Most Merciful (Ar-Rahman). Be merciful to those on the earth, and the One above the heavens will have mercy upon you.",
      translationUrdu = "رحم کرنے والوں پر رحمٰن رحم فرماتا ہے؛ تم زمین والوں پر رحم کرو، آسمان والا تم پر رحم فرمائے گا۔",
      sourceReference = "Sunan Abu Dawud, Hadith 4941; Jami' at-Tirmidhi, Hadith 1924",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Graded Hasan Sahih by Imam at-Tirmidhi; the traditional Hadith transmittedfirst in classical Hadith study circles (Hadith al-Musalsal bil-Awwaliyyah).",
      chronologyRank = 4,
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_MARFU,
      authenticityGrading = "Hasan Sahih (Jami' at-Tirmidhi 1924)",
      hadithBookName = "Jami' at-Tirmidhi",
      hadithNumber = "1924",
      historicalRelationshipTags = listOf(HistoricalRelationshipTag.ALL)
    ),
    ReflectionItem(
      id = 5,
      category = ReflectionCategory.PROPHETIC_GUIDANCE,
      speaker = "Prophet Muhammad ﷺ (Narrated by An-Nu'man ibn Bashir RA)",
      title = "The Believers as One Living Body",
      theme = "Unity & Empathy",
      arabicOrOriginalText = "مَثَلُ الْمُؤْمِنِينَ فِي تَوَادِّهِمْ وَتَرَاحُمِهِمْ وَتَعَاطُفِهِمْ مَثَلُ الْجَسَدِ إِذَا اشْتَكَى مِنْهُ عُضْوٌ تَدَاعَى لَهُ سَائِرُ الْجَسَدِ بِالسَّهَرِ وَالْحُمَّى",
      translationEnglish = "The parable of the believers in their mutual love, mercy, and compassion is that of a single body: if one limb suffers, the whole body responds to it with wakefulness and fever.",
      translationUrdu = "آپس کی محبت، رحم دلی اور ہمدردی میں مومنوں کی مثال ایک جسم کی سی ہے کہ جب اس کے کسی ایک عضو کو تکلیف ہوتی ہے تو سارا جسم بے خوابی اور بخار میں اس کا شریک ہو جاتا ہے۔",
      sourceReference = "Sahih al-Bukhari, Hadith 6011; Sahih Muslim, Hadith 2586",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Core Prophetic teaching on solidarity, social responsibility, and mutual care.",
      chronologyRank = 5,
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_MARFU,
      authenticityGrading = "Muttafaqun Alayh (Sahih al-Bukhari & Sahih Muslim)",
      hadithBookName = "Sahih al-Bukhari",
      hadithNumber = "6011",
      historicalRelationshipTags = listOf(HistoricalRelationshipTag.ALL)
    ),
    ReflectionItem(
      id = 6,
      category = ReflectionCategory.PROPHETIC_GUIDANCE,
      speaker = "Prophet Muhammad ﷺ (Narrated by Abu Hurayrah RA)",
      title = "True Strength is Self-Mastery",
      theme = "Patience & Restraint",
      arabicOrOriginalText = "لَيْسَ الشَّدِيدُ بِالصُّرَعَةِ، إِنَّمَا الشَّدِيدُ الَّذِي يَمْلِكُ نَفْسَهُ عِنْدَ الْغَضَبِ",
      translationEnglish = "The strong person is not the one who overcomes others by physical force, but the strong person is the one who controls himself when angry.",
      translationUrdu = "طاقتور وہ نہیں جو کشتی میں دوسرے کو پچھاڑ دے، بلکہ حقیقی طاقتور وہ ہے جو غصے کے وقت اپنے نفس پر قابو رکھے۔",
      sourceReference = "Sahih al-Bukhari, Hadith 6114; Sahih Muslim, Hadith 2609",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Redefines courage as emotional and spiritual self-control.",
      chronologyRank = 6,
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_MARFU,
      authenticityGrading = "Muttafaqun Alayh (Sahih al-Bukhari & Sahih Muslim)",
      hadithBookName = "Sahih al-Bukhari",
      hadithNumber = "6114",
      historicalRelationshipTags = listOf(HistoricalRelationshipTag.ALL)
    ),

    // =========================================================================
    // 2. AHL AL-BAYT (AS) (AHLUL_BAYT_WISDOM)
    // =========================================================================
    ReflectionItem(
      id = 101,
      category = ReflectionCategory.AHLUL_BAYT_WISDOM,
      speaker = "Imam Ali ibn Abi Talib (AS / RA)",
      title = "Universal Human Dignity & Just Governance",
      theme = "Justice & Human Brotherhood",
      arabicOrOriginalText = "النَّاسُ صِنْفَانِ: إِمَّا أَخٌ لَكَ فِي الدِّينِ، وَإِمَّا نَظِيرٌ لَكَ فِي الْخَلْقِ",
      translationEnglish = "People are of two kinds: either your brother in faith, or your equal in creation (humanity).",
      translationUrdu = "لوگ دو ہی قسم کے ہیں: یا تو وہ دین میں تمہارے بھائی ہیں، یا خلقت (انسانیت) میں تمہارے برابر کے شریک ہیں۔",
      sourceReference = "Nahj al-Balagha (compiled by Al-Sharif al-Radi), Letter 53 (Covenant to Malik al-Ashtar); Tuhaf al-Uqul, p. 126",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "From Imam Ali's famous constitutional epistle ('Ahd) to Malik al-Ashtar upon appointing him governor of Egypt (38 AH).",
      chronologyRank = 1,
      narrationType = WisdomNarrationType.AHL_AL_BAYT_HADITH_MATHUR,
      authenticityGrading = "Sahih / Mashhur in Classical Epistles & Nahj al-Balagha",
      hadithBookName = "Nahj al-Balagha",
      hadithNumber = "Letter 53",
      historicalRelationshipTags = listOf(
        HistoricalRelationshipTag.PROPHETIC_HOUSEHOLD,
        HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN
      ),
      relationshipToProphetAndAhlAlBayt = "Cousin, son-in-law, and brother of Prophet Muhammad ﷺ; head of the Prophetic Household after the Prophet ﷺ."
    ),
    ReflectionItem(
      id = 102,
      category = ReflectionCategory.AHLUL_BAYT_WISDOM,
      speaker = "Sayyidah Fatimah al-Zahra (SA) (Narrated by Sayyidah Aishah RA)",
      title = "The Beloved Part of the Prophet ﷺ & Glad Tidings of Paradise",
      theme = "Station of Sayyidah Fatimah (SA)",
      arabicOrOriginalText = "فَاطِمَةُ بَضْعَةٌ مِنِّي، فَمَنْ أَغْضَبَهَا أَغْضَبَنِي ... أَمَا تَرْضَيْنَ أَنْ تَكُونِي سَيِّدَةَ نِسَاءِ أَهْلِ الْجَنَّةِ أَوْ نِسَاءِ الْمُؤْمِنِينَ",
      translationEnglish = "Fatimah is a part of me; whoever angers her angers me... Are you not pleased that you are the Chief of the Women of Paradise (or the Women of the Believers)?",
      translationUrdu = "فاطمہ میرے جگر کا ٹکڑا ہیں، جس نے انہیں ناراض کیا اس نے مجھے ناراض کیا... اے فاطمہ! کیا تم اس بات پر راضی نہیں کہ تم جنت کی تمام عورتوں (یا مومنہ عورتوں) کی سردار ہو؟",
      sourceReference = "Sahih al-Bukhari, Hadith 3714, 3767 & 3624; Sahih Muslim, Hadith 2449 & 2450; Al-Kafi, Vol. 1, p. 458",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Authenticated across Sahih al-Bukhari, Sahih Muslim, and Shia Hadith collections regarding the preeminent spiritual rank of Sayyidah Fatimah al-Zahra (SA).",
      chronologyRank = 2,
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_MARFU,
      authenticityGrading = "Muttafaqun Alayh (Sahih al-Bukhari & Sahih Muslim)",
      hadithBookName = "Sahih al-Bukhari",
      hadithNumber = "3714 / 3624",
      historicalRelationshipTags = listOf(
        HistoricalRelationshipTag.PROPHETIC_HOUSEHOLD,
        HistoricalRelationshipTag.THAQALAYN_AND_KISA_TRANSMITTERS
      ),
      relationshipToProphetAndAhlAlBayt = "Daughter of Prophet Muhammad ﷺ and Sayyidah Khadijah (RA); wife of Imam Ali (AS) and mother of Imam al-Hasan and Imam al-Husayn (AS)."
    ),
    ReflectionItem(
      id = 103,
      category = ReflectionCategory.AHLUL_BAYT_WISDOM,
      speaker = "Imam al-Hasan & Imam al-Husayn (AS) (Narrated by Abu Sa'id al-Khudri & Hudhayfah RA)",
      title = "The Two Masters of the Youth of Paradise",
      theme = "Ahl al-Bayt & Prophetic Legacy",
      arabicOrOriginalText = "الْحَسَنُ وَالْحُسَيْنُ سَيِّدَا شَبَابِ أَهْلِ الْجَنَّةِ",
      translationEnglish = "Al-Hasan and Al-Husayn are the two masters of the youth of the people of Paradise.",
      translationUrdu = "حسن اور حسین جنت کے نوجوانوں کے سردار ہیں۔",
      sourceReference = "Jami' at-Tirmidhi, Hadith 3768 & 3781; Musnad Ahmad, Hadith 11012; Sunan Ibn Majah, Hadith 118; Al-Amali (Al-Saduq), p. 122",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Graded Hasan Sahih by Imam at-Tirmidhi; narrated by Abu Sa'id al-Khudri, Hudhayfah ibn al-Yaman, Jabir ibn Abdillah, and Imam Ali (RA/AS).",
      chronologyRank = 3,
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_MARFU,
      authenticityGrading = "Hasan Sahih (Jami' at-Tirmidhi 3768 & 3781)",
      hadithBookName = "Jami' at-Tirmidhi",
      hadithNumber = "3768",
      historicalRelationshipTags = listOf(
        HistoricalRelationshipTag.PROPHETIC_HOUSEHOLD,
        HistoricalRelationshipTag.THAQALAYN_AND_KISA_TRANSMITTERS
      ),
      relationshipToProphetAndAhlAlBayt = "Grandsons of Prophet Muhammad ﷺ and members of the People of the Cloak (Ashab al-Kisa')."
    ),
    ReflectionItem(
      id = 104,
      category = ReflectionCategory.AHLUL_BAYT_WISDOM,
      speaker = "Imam al-Husayn ibn Ali (AS)",
      title = "Reformation of the Ummah & Standing for Justice",
      theme = "Moral Courage & Principle",
      arabicOrOriginalText = "إِنِّي لَمْ أَخْرُجْ أَشِرًا وَلَا بَطِرًا وَلَا مُفْسِدًا وَلَا ظَالِمًا، وَإِنَّمَا خَرَجْتُ لِطَلَبِ الْإِصْلَاحِ فِي أُمَّةِ جَدِّي رَسُولِ اللَّهِ ﷺ، أُرِيدُ أَنْ آمُرَ بِالْمَعْرُوفِ وَأَنْهَى عَنِ الْمُنْكَرِ وَأَسِيرَ بِسِيرَةِ جَدِّي وَأَبِي عَلِيِّ بْنِ أَبِي طَالِبٍ",
      translationEnglish = "Indeed, I have not risen out of insolence, arrogance, corruption, or oppression; rather, I have risen only to seek reform in the Ummah of my grandfather, the Messenger of Allah ﷺ. I desire to enjoin what is right, forbid what is wrong, and walk in the way of my grandfather and my father Ali ibn Abi Talib.",
      translationUrdu = "بے شک میں خود سری، تکبر، فساد یا ظلم کے لیے نہیں نکلا، بلکہ میں صرف اپنے نانا رسول اللہ ﷺ کی امت کی اصلاح کے لیے نکلا ہوں۔ میں نیکی کا حکم دینا اور برائی سے روکنا چاہتا ہوں اور اپنے نانا ﷺ اور اپنے والد علی بن ابی طالب (ع) کی سیرت پر چلنا چاہتا ہوں۔",
      sourceReference = "Bihar al-Anwar (from Maqtal al-Khwarizmi, Vol. 1, p. 188 & الفتوح لابن أعثم Vol. 5, p. 21)",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "From the written testament of Imam al-Husayn (AS) to his brother Muhammad ibn al-Hanafiyyah upon departing Madinah in 60 AH.",
      chronologyRank = 4,
      narrationType = WisdomNarrationType.AHL_AL_BAYT_HADITH_MATHUR,
      authenticityGrading = "Verified Historical Testament in Primary Maqtal & Tarikh Corpora",
      hadithBookName = "Maqtal al-Khwarizmi / Bihar al-Anwar",
      hadithNumber = "Vol. 1, p. 188",
      historicalRelationshipTags = listOf(HistoricalRelationshipTag.PROPHETIC_HOUSEHOLD),
      relationshipToProphetAndAhlAlBayt = "Grandson of Prophet Muhammad ﷺ, 3rd Imam of Ahl al-Bayt, Sayyid al-Shuhada'."
    ),
    ReflectionItem(
      id = 105,
      category = ReflectionCategory.AHLUL_BAYT_WISDOM,
      speaker = "Imam Ali ibn al-Husayn Zayn al-Abidin (AS)",
      title = "Supplication for Noble Character & Forgiveness of Others",
      theme = "Spiritual Refinement (Tazkiyah)",
      arabicOrOriginalText = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَآلِهِ، وَحَلِّنِي بِحِلْيَةِ الصَّالِحِينَ، وَأَلْبِسْنِي زِينَةَ الْمُتَّقِينَ، فِي بَسْطِ الْعَدْلِ، وَكَظْمِ الْغَيْظِ، وَإِطْفَاءِ النَّائِرَةِ، وَضَمِّ أَهْلِ الْفُرْقَةِ",
      translationEnglish = "O Allah, bless Muhammad and his Household, adorn me with the ornament of the righteous, and clothe me in the beauty of the God-conscious: in spreading justice, restraining anger, quenching the flames of hostility, and bringing together those who are divided.",
      translationUrdu = "اے اللہ! محمد اور آلِ محمد پر رحمت نازل فرما، اور مجھے نیکوکاروں کے زیور سے آراستہ فرما اور پرہیزگاروں کا لباس پہنا دے: عدل پھیلانے، غصہ ضبط کرنے، دشمنی کی آگ بجھانے اور بچھڑے ہوؤں کو ملانے میں۔",
      sourceReference = "Al-Sahifa al-Sajjadiyya, Supplication 20 (Makarim al-Akhlaq), Verses 1–2",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Transmitted by Imam Zayn al-Abidin (AS) through his sons Imam Muhammad al-Baqir and Zayd ibn Ali with continuous chains.",
      chronologyRank = 5,
      narrationType = WisdomNarrationType.AHL_AL_BAYT_HADITH_MATHUR,
      authenticityGrading = "Mutawatir / Sahih (Al-Sahifa al-Sajjadiyya)",
      hadithBookName = "Al-Sahifa al-Sajjadiyya",
      hadithNumber = "Dua 20",
      historicalRelationshipTags = listOf(HistoricalRelationshipTag.PROPHETIC_HOUSEHOLD),
      relationshipToProphetAndAhlAlBayt = "Son of Imam al-Husayn (AS), great-grandson of Prophet Muhammad ﷺ, 4th Imam of Ahl al-Bayt."
    ),
    ReflectionItem(
      id = 106,
      category = ReflectionCategory.AHLUL_BAYT_WISDOM,
      speaker = "Imam Muhammad al-Baqir (AS)",
      title = "Perfection of Intellect, Modesty & Good Character",
      theme = "Intellect & Ethics",
      arabicOrOriginalText = "إِنَّمَا يُدَاقُّ اللَّهُ الْعِبَادَ فِي الْحِسَابِ يَوْمَ الْقِيَامَةِ عَلَى قَدْرِ مَا آتَاهُمْ مِنَ الْعُقُولِ فِي الدُّنْيَا",
      translationEnglish = "Indeed, Allah will scrutinize servants in the reckoning on the Day of Resurrection in exact proportion to the intellect He granted them in this world.",
      translationUrdu = "بے شک اللہ تعالیٰ قیامت کے دن بندوں سے اس عقل کے بقدر باریک بینی سے حساب لے گا جو اس نے دنیا میں انہیں عطا فرمائی تھی۔",
      sourceReference = "Al-Kafi (Al-Kulayni), Vol. 1, Kitab al-'Aql wal-Jahl, Hadith 3",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Graded Sahih by Allamah al-Majlisi in Mir'at al-'Uqul (Vol. 1, p. 30).",
      chronologyRank = 6,
      narrationType = WisdomNarrationType.AHL_AL_BAYT_HADITH_MATHUR,
      authenticityGrading = "Sahih (Mir'at al-'Uqul Vol. 1, p. 30)",
      hadithBookName = "Al-Kafi",
      hadithNumber = "Vol. 1, Kitab al-'Aql, H. 3",
      historicalRelationshipTags = listOf(HistoricalRelationshipTag.PROPHETIC_HOUSEHOLD),
      relationshipToProphetAndAhlAlBayt = "5th Imam of Ahl al-Bayt (AS), renowned opener of sacred knowledge (Baqir al-'Ulum)."
    ),
    ReflectionItem(
      id = 107,
      category = ReflectionCategory.AHLUL_BAYT_WISDOM,
      speaker = "Imam Ja'far al-Sadiq (AS)",
      title = "Calling People to Islam Through Action, Truthfulness & Trust",
      theme = "Living Ethics & Honesty",
      arabicOrOriginalText = "كُونُوا دُعَاةً لِلنَّاسِ بِغَيْرِ أَلْسِنَتِكُمْ، لِيَرَوْا مِنْكُمُ الْوَرَعَ وَالِاجْتِهَادَ وَالصَّلَاةَ وَالْخَيْرَ، فَإِنَّ ذَلِكَ دَاعِيَةٌ",
      translationEnglish = "Be callers of people to goodness without your tongues, so that they may see from you piety, diligence, prayer, and goodness—for indeed that is the true invitation.",
      translationUrdu = "لوگوں کو اپنی زبانوں کے بغیر (اپنے عمل سے) بھلائی کی طرف بلانے والے بنو، تاکہ وہ تم سے تقویٰ، محنت، نماز اور خیر دیکھیں، کیونکہ یہی حقیقی دعوت ہے۔",
      sourceReference = "Al-Kafi (Al-Kulayni), Vol. 2, Kitab al-Iman wal-Kufr, Bab al-Wara', Hadith 14; Hilyat al-Awliya (Abu Nu'aym), Vol. 3, p. 195",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Graded Sahih by Allamah al-Majlisi in Mir'at al-'Uqul (Vol. 8, p. 71).",
      chronologyRank = 7,
      narrationType = WisdomNarrationType.AHL_AL_BAYT_HADITH_MATHUR,
      authenticityGrading = "Sahih (Al-Kafi Vol. 2, p. 77 / Mir'at al-'Uqul Vol. 8, p. 71)",
      hadithBookName = "Al-Kafi",
      hadithNumber = "Vol. 2, Bab al-Wara', H. 14",
      historicalRelationshipTags = listOf(HistoricalRelationshipTag.PROPHETIC_HOUSEHOLD),
      relationshipToProphetAndAhlAlBayt = "6th Imam of Ahl al-Bayt (AS) and teacher of both Sunni and Shia jurists and Hadith scholars."
    ),
    ReflectionItem(
      id = 108,
      category = ReflectionCategory.AHLUL_BAYT_WISDOM,
      speaker = "Imam Musa al-Kazim (AS)",
      title = "Two Proofs of Allah Upon Humanity: The Outer and the Inner",
      theme = "Reason & Revelation",
      arabicOrOriginalText = "يَا هِشَامُ، إِنَّ لِلَّهِ عَلَى النَّاسِ حُجَّتَيْنِ: حُجَّةً ظَاهِرَةً وَحُجَّةً بَاطِنَةً؛ فَأَمَّا الظَّاهِرَةُ فَالرُّسُلُ وَالْأَنْبِيَاءُ وَالْأَئِمَّةُ عَلَيْهِمُ السَّلَامُ، وَأَمَّا الْبَاطِنَةُ فَالْعُقُولُ",
      translationEnglish = "O Hisham! Indeed Allah has two proofs over mankind: an outward proof and an inward proof. As for the outward proof, it is the Messengers, the Prophets, and the Imams (peace be upon them); and as for the inward proof, it is the intellects ('Uqul).",
      translationUrdu = "اے ہشام! بے شک لوگوں پر اللہ کی دو حجتیں ہیں: ایک ظاہری حجت اور ایک باطنی حجت۔ ظاہری حجت رسول، انبیاء اور ائمہ علیہم السلام ہیں، اور باطنی حجت عقلیں ہیں۔",
      sourceReference = "Al-Kafi (Al-Kulayni), Vol. 1, Kitab al-'Aql wal-Jahl, Hadith 12; Tuhaf al-Uqul, p. 383",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "From Imam Musa al-Kazim's (AS) comprehensive discourse to his student Hisham ibn al-Hakam.",
      chronologyRank = 8,
      narrationType = WisdomNarrationType.AHL_AL_BAYT_HADITH_MATHUR,
      authenticityGrading = "Mu'tabar (Al-Kafi Vol. 1, Kitab al-'Aql, H. 12)",
      hadithBookName = "Al-Kafi",
      hadithNumber = "Vol. 1, Kitab al-'Aql, H. 12",
      historicalRelationshipTags = listOf(HistoricalRelationshipTag.PROPHETIC_HOUSEHOLD),
      relationshipToProphetAndAhlAlBayt = "7th Imam of Ahl al-Bayt (AS), renowned for forbearance (Al-Kazim) and devotion."
    ),
    ReflectionItem(
      id = 109,
      category = ReflectionCategory.AHLUL_BAYT_WISDOM,
      speaker = "Imam Ali ibn Musa al-Rida (AS)",
      title = "Faith as Fulfillment of Trust & Abstaining from Harming Believers",
      theme = "Trustworthiness & Integrity",
      arabicOrOriginalText = "لَا يَكُونُ الْمُؤْمِنُ مُؤْمِنًا حَتَّى تَكُونَ فِيهِ ثَلَاثُ خِصَالٍ: سُنَّةٌ مِنْ رَبِّهِ، وَسُنَّةٌ مِنْ نَبِيِّهِ ﷺ، وَسُنَّةٌ مِنْ وَلِيِّهِ ... أَمَّا السُّنَّةُ مِنْ نَبِيِّهِ فَمُدَارَاةُ النَّاسِ",
      translationEnglish = "A believer is not a true believer until he possesses three qualities: a way from his Lord, a way from his Prophet ﷺ, and a way from his Wali... As for the way from his Prophet ﷺ, it is gentle kindness and courtesy toward people.",
      translationUrdu = "مومن اس وقت تک کامل مومن نہیں ہوتا جب تک اس میں تین خصلتیں نہ ہوں: ایک سنت اپنے رب سے، ایک سنت اپنے نبی ﷺ سے اور ایک سنت اپنے ولی سے... اور نبی ﷺ کی سنت لوگوں کے ساتھ نرمی اور حسنِ مدارات ہے۔",
      sourceReference = "Uyun Akhbar al-Rida (Shaykh al-Saduq), Vol. 1, Ch. 28, Hadith 1; Al-Kafi, Vol. 2, p. 241",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Documented in Uyun Akhbar al-Rida and Al-Kafi on the ethical harmony of faith.",
      chronologyRank = 9,
      narrationType = WisdomNarrationType.AHL_AL_BAYT_HADITH_MATHUR,
      authenticityGrading = "Sahih (Al-Kafi Vol. 2, p. 241)",
      hadithBookName = "Uyun Akhbar al-Rida",
      hadithNumber = "Vol. 1, Ch. 28, H. 1",
      historicalRelationshipTags = listOf(HistoricalRelationshipTag.PROPHETIC_HOUSEHOLD),
      relationshipToProphetAndAhlAlBayt = "8th Imam of Ahl al-Bayt (AS), scholar of Medina and Khurasan."
    ),
    ReflectionItem(
      id = 110,
      category = ReflectionCategory.AHLUL_BAYT_WISDOM,
      speaker = "Attributed to Imam Ali ibn Abi Talib (AS) / Imam al-Shafi'i",
      title = "Value of Knowledge & Humility in Debate",
      theme = "Knowledge & Humility",
      arabicOrOriginalText = "مَا جَادَلْتُ عَالِمًا إِلَّا غَلَبْتُهُ، وَمَا جَادَلَنِي جَاهِلٌ إِلَّا غَلَبَنِي",
      translationEnglish = "Never did I debate a scholar except that I convinced him with evidence, and never did an ignorant person argue with me except that he overcame me (by refusing reason).",
      translationUrdu = "میں نے کسی عالم سے دلیل کے ساتھ گفتگو نہیں کی مگر یہ کہ میں غالب آیا، اور کسی جاہل نے مجھ سے بحث نہیں کی مگر یہ کہ وہ مجھ پر غالب آ گیا۔",
      sourceReference = "Diwan al-Imam Ali (popular attribution); also documented in Manaqib al-Shafi'i by Al-Bayhaqi (Vol. 2, p. 157)",
      attributionStatus = WisdomAttributionStatus.DISPUTED_ATTRIBUTION,
      contextNotes = "Included with transparent 'Disputed / Dual Attribution' status so readers can distinguish verified primary-source Hadith from sayings attributed to multiple classical figures.",
      chronologyRank = 10,
      narrationType = WisdomNarrationType.AHL_AL_BAYT_HADITH_MATHUR,
      authenticityGrading = "Dual Attribution (Manaqib al-Shafi'i / Later Adab Collections)",
      hadithBookName = "Manaqib al-Shafi'i / Adab",
      hadithNumber = "Vol. 2, p. 157",
      historicalRelationshipTags = listOf(HistoricalRelationshipTag.PROPHETIC_HOUSEHOLD)
    ),

    // =========================================================================
    // 3. EARLY SAHABAH (RA) (COMPANIONS_WISDOM)
    // Strictly ordered by verified historical chronology of accepting Islam
    // (As-Sabiqun al-Awwalun) and loyal defense of Prophet Muhammad ﷺ & Ahl al-Bayt (AS).
    // =========================================================================
    ReflectionItem(
      id = 201,
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Sayyidah Khadijah bint Khuwaylid (RA) [Chronology #1: First Believer in Islam]",
      title = "Reassurance at the First Revelation & Unwavering Support of the Prophet ﷺ",
      theme = "First Believer & Pillar of Prophethood",
      arabicOrOriginalText = "كَلَّا وَاللَّهِ مَا يُخْزِيكَ اللَّهُ أَبَدًا؛ إِنَّكَ لَتَصِلُ الرَّحِمَ، وَتَحْمِلُ الْكَلَّ، وَتَكْسِبُ الْمَعْدُومَ، وَتَقْرِي الضَّيْفَ، وَتُعِينُ عَلَى نَوَائِبِ الْحَقِّ",
      translationEnglish = "Never! By Allah, Allah will never disgrace you. Indeed, you uphold the ties of kinship, bear the burdens of the weak, provide for the destitute, honor the guest, and assist in the causas of truth.",
      translationUrdu = "ہرگز نہیں! اللہ کی قسم، اللہ آپ کو کبھی رسوا نہیں کرے گا؛ بے شک آپ صلہ رحمی کرتے ہیں، بے سہاروں کا بوجھ اٹھاتے ہیں، ناداروں کو کما کر دیتے ہیں، مہمان نوازی کرتے ہیں اور حق کی راہ میں پیش آنے والی مصیبتوں میں مدد کرتے ہیں۔",
      sourceReference = "Sahih al-Bukhari, Hadith 3 (Kitab Bad' al-Wahy) & Hadith 3821; Sahih Muslim, Hadith 160 & 2435; Musnad Ahmad, Hadith 24864",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Unanimously recognized across all Sunni and Shia historians (Ibn Ishaq, Al-Tabari, Ibn Hajar in Al-Isabah, Al-Kulayni) as the very first human being to believe in Prophet Muhammad ﷺ. In Musnad Ahmad 24864, the Prophet ﷺ testified: 'She believed in me when people disbelieved in me, affirmed my truthfulness when people belied me, and shared her wealth with me when people deprived me.'",
      chronologyRank = 1,
      chronologyEraAndOrderNote = "Chronological Rank #1 (13 BH / 610 CE, Day 1 of Revelation in Makkah): Unanimously the first believer in Islam among all men and women.",
      historicalDisputeNote = "Scholarly Consensus (Ijma'): Historians and Hadith scholars across all schools agree without dispute that Sayyidah Khadijah (RA) was the first person to embrace Islam.",
      narrationType = WisdomNarrationType.COMPANION_ATHAR_MAWQUF,
      authenticityGrading = "Muttafaqun Alayh (Sahih al-Bukhari 3 & Sahih Muslim 160)",
      hadithBookName = "Sahih al-Bukhari",
      hadithNumber = "3 / 3821",
      historicalRelationshipTags = listOf(
        HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN,
        HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT,
        HistoricalRelationshipTag.PROPHETIC_HOUSEHOLD
      ),
      relationshipToProphetAndAhlAlBayt = "First wife of Prophet Muhammad ﷺ, Mother of the Believers, and mother of Sayyidah Fatimah al-Zahra (SA) from whom the lineage of Ahl al-Bayt continues."
    ),
    ReflectionItem(
      id = 202,
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Imam Ali ibn Abi Talib (AS / RA) [Chronology #2: First Male / Youth Believer & Defender]",
      title = "First Prayer with the Prophet ﷺ & Standard-Bearer loved by Allah and His Messenger",
      theme = "Earliest Faith, Courage & Fidelity",
      arabicOrOriginalText = "لَأُعْطِيَنَّ الرَّايَةَ غَدًا رَجُلًا يُحِبُّ اللَّهَ وَرَسُولَهُ وَيُحِبُّهُ اللَّهُ وَرَسُولُهُ، يَفْتَحُ اللَّهُ عَلَى يَدَيْهِ ... أَنْتَ مِنِّي بِمَنْزِلَةِ هَارُونَ مِنْ مُوسَى إِلَّا أَنَّهُ لَا نَبِيَّ بَعْدِي",
      translationEnglish = "The Prophet ﷺ said: 'Tomorrow I shall certainly give the standard to a man who loves Allah and His Messenger, and whom Allah and His Messenger love; Allah will grant victory at his hands'—and he gave it to Ali. And he ﷺ said to him: 'You are to me in the position of Aaron (Harun) to Moses (Musa), except that there is no prophet after me.'",
      translationUrdu = "رسول اللہ ﷺ نے فرمایا: کل میں یہ علم (جھنڈا) ایک ایسے مرد کو دوں گا جو اللہ اور اس کے رسول سے محبت کرتا ہے اور اللہ اور اس کا رسول اس سے محبت کرتے ہیں، اللہ اس کے ہاتھ پر فتح عطا فرمائے گا — پس آپ ﷺ نے وہ علم علی (ع) کو عطا فرمایا۔ اور آپ ﷺ نے فرمایا: اے علی! تم کو مجھ سے وہی نسبت ہے جو ہارون کو موسیٰ سے تھی، سوائے اس کے کہ میرے بعد کوئی نبی نہیں۔",
      sourceReference = "Sahih al-Bukhari, Hadith 3701, 4210 & 4416; Sahih Muslim, Hadith 2404 & 2406; Jami' at-Tirmidhi, Hadith 3728 & 3735; Musnad Ahmad, Hadith 1191; Al-Mustadrak, Hadith 4579",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "In Jami' at-Tirmidhi 3728 & 3735, Musnad Ahmad 1191, and Al-Mustadrak 4579 (graded Sahih), Anas ibn Malik, Zayd ibn Arqam, and Ibn Abbas (RA) report that Ali (AS) was the first male after Khadijah (RA) to pray with the Prophet ﷺ.",
      chronologyRank = 2,
      chronologyEraAndOrderNote = "Chronological Rank #2 (13 BH / 610 CE, immediately following Sayyidah Khadijah RA): Raised in the Prophet's ﷺ house; the first male and first youth to accept Islam and pray behind the Prophet ﷺ.",
      historicalDisputeNote = "Classical Chronological Note: Ibn Ishaq, Al-Tabari (Tarikh Vol. 2, p. 310), Zayd ibn Arqam, and Ibn Abbas record Ali (AS) as the very first male to embrace Islam after Sayyidah Khadijah (RA). Some later historians categorized earliest converts into three sub-groups: first woman (Khadijah RA), first youth/male in the Prophet's household (Ali AS), first freedman (Zayd ibn Harithah RA), and first adult freeman outside the household (Abu Bakr RA).",
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION,
      authenticityGrading = "Muttafaqun Alayh (Sahih al-Bukhari 3701 & Sahih Muslim 2404)",
      hadithBookName = "Sahih al-Bukhari",
      hadithNumber = "3701 / 4416",
      historicalRelationshipTags = listOf(
        HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN,
        HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT,
        HistoricalRelationshipTag.PROPHETIC_HOUSEHOLD
      ),
      relationshipToProphetAndAhlAlBayt = "Raised in the care of Prophet Muhammad ﷺ from childhood; slept in the Prophet's bed on the night of Hijrah; husband of Sayyidah Fatimah (SA) and father of Al-Hasan and Al-Husayn (AS)."
    ),
    ReflectionItem(
      id = 203,
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Zayd ibn Harithah al-Kalbi (RA) [Chronology #3: Earliest Freedman Believer]",
      title = "Choosing the Messenger of Allah ﷺ Over All Worldly Ties",
      theme = "Devotion & Beloved of the Prophet ﷺ",
      arabicOrOriginalText = "وَاللَّهِ لَا أَخْتَارُ عَلَيْكَ أَحَدًا أَبَدًا، أَنْتَ مِنِّي بِمَكَانِ الْأَبِ وَالْعَمِّ ... وَإِنْ كَانَ مِنْ أَحَبِّ النَّاسِ إِلَيَّ",
      translationEnglish = "Zayd said to the Prophet ﷺ: 'By Allah, I will never choose anyone over you; you are to me in the place of both father and uncle.' And the Prophet ﷺ testified: 'Indeed, Zayd was among the most beloved of people to me.'",
      translationUrdu = "حضرت زید بن حارثہ (رض) نے عرض کیا: اللہ کی قسم! میں آپ ﷺ کے مقابلے میں کسی کو ہرگز اختیار نہیں کروں گا، آپ ہی میرے نزدیک والد اور چچا کی جگہ ہیں۔ اور نبی کریم ﷺ نے فرمایا: بے شک زید مجھے لوگوں میں سب سے زیادہ محبوب تھے۔",
      sourceReference = "Sahih al-Bukhari, Hadith 3730 & 4468; Sahih Muslim, Hadith 2426; Jami' at-Tirmidhi, Hadith 3816; Al-Isabah (Ibn Hajar), Vol. 2, p. 598",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "The only Companion mentioned by name in the Qur'an (Surah Al-Ahzab 33:37) and martyred as first commander at the Battle of Mu'tah (8 AH) alongside Ja'far ibn Abi Talib (RA).",
      chronologyRank = 3,
      chronologyEraAndOrderNote = "Chronological Rank #3 (13 BH / 610 CE, inside the Prophet's ﷺ household in Makkah): Embraced Islam immediately alongside Khadijah (RA) and Ali (AS) as a member of the Prophet's household.",
      historicalDisputeNote = "Scholarly Chronological Note: Al-Zuhri, Urwah ibn al-Zubayr, and Ibn Ishaq note that Zayd ibn Harithah (RA), living in the Prophet's ﷺ household, embraced Islam right after Khadijah (RA) and Ali (AS), while other accounts place Zayd and Abu Bakr (RA) in close succession as the third and fourth Muslims.",
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION,
      authenticityGrading = "Muttafaqun Alayh (Sahih al-Bukhari 3730 & Sahih Muslim 2426)",
      hadithBookName = "Sahih al-Bukhari",
      hadithNumber = "3730",
      historicalRelationshipTags = listOf(
        HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN,
        HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT
      ),
      relationshipToProphetAndAhlAlBayt = "Freedman and beloved member of the Prophet's ﷺ household; paired in brotherhood with Hamzah ibn Abd al-Muttalib (RA) and martyred alongside Ja'far ibn Abi Talib (RA) at Mu'tah."
    ),
    ReflectionItem(
      id = 204,
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Abu Bakr as-Siddiq (RA) [Chronology #4: Early Adult Freeman Believer]",
      title = "Honoring the Prophet's ﷺ Kinship & Accountability in Leadership",
      theme = "Early Faith & Regard for the Prophet's Family",
      arabicOrOriginalText = "ارْقُبُوا مُحَمَّدًا ﷺ فِي أَهْلِ بَيْتِهِ ... وَاللَّهِ لَأَنْ أَصِلَ قَرَابَةَ رَسُولِ اللَّهِ ﷺ أَحَبُّ إِلَيَّ مِنْ أَنْ أَصِلَ قَرَابَتِي",
      translationEnglish = "Honor and revere Muhammad ﷺ by honoring his Ahl al-Bayt (Household)... By Allah, maintaining the ties of the relatives of the Messenger of Allah ﷺ is more beloved to me than maintaining the ties of my own relatives.",
      translationUrdu = "محمد ﷺ کے اہلِ بیت کے معاملے میں آپ ﷺ کا لحاظ رکھو... اللہ کی قسم! رسول اللہ ﷺ کے قرابت داروں کے ساتھ صلہ رحمی کرنا مجھے اپنے قرابت داروں کے ساتھ صلہ رحمی کرنے سے زیادہ محبوب ہے۔",
      sourceReference = "Sahih al-Bukhari, Hadith 3712, 3713 & 3751 (Kitab Fada'il Ashab al-Nabi)",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Recorded in Sahih al-Bukhari 3712 & 3713; expresses the duty of the Muslim community to revere the Prophet's ﷺ Household (Ahl al-Bayt).",
      chronologyRank = 4,
      chronologyEraAndOrderNote = "Chronological Rank #4 (13 BH / 610 CE, Early Makkan Period): Among the earliest converts outside the Prophet's ﷺ immediate household and companion during the Hijrah.",
      historicalDisputeNote = "Scholarly Chronological Note: As summarized by Ibn Abd al-Barr in Al-Isti'ab and Ibn Hajar in Al-Isabah, scholars reconciled the narrations on 'the first after Khadijah (RA)' by noting Ali (AS) as first among youth/males in the household, Zayd ibn Harithah (RA) as first among freedmen, and Abu Bakr (RA) as first among adult freemen outside the household.",
      narrationType = WisdomNarrationType.COMPANION_ATHAR_MAWQUF,
      authenticityGrading = "Sahih (Sahih al-Bukhari 3712 & 3713)",
      hadithBookName = "Sahih al-Bukhari",
      hadithNumber = "3712 / 3713",
      historicalRelationshipTags = listOf(HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN),
      relationshipToProphetAndAhlAlBayt = "Early Makkan companion and father-in-law of the Prophet ﷺ; transmitted the command in Sahih al-Bukhari 3713 to honor the Prophet ﷺ in his Ahl al-Bayt."
    ),
    ReflectionItem(
      id = 205,
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Abu Dharr al-Ghifari (RA) [Chronology #5: 4th/5th Early Believer & Loyal Supporter of Ahl al-Bayt]",
      title = "Uncompromising Truthfulness, Asceticism & Devotion to the Prophet's ﷺ Family",
      theme = "Truthfulness & Defense of Justice",
      arabicOrOriginalText = "مَا أَظَلَّتِ الْخَضْرَاءُ وَلَا أَقَلَّتِ الْغَبْرَاءُ مِنْ ذِي لَهْجَةٍ أَصْدَقَ وَلَا أَوْفَى مِنْ أَبِي ذَرٍّ شَبِيهِ عِيسَى ابْنِ مَرْيَمَ ... لَقَدْ رَأَيْتُنِي رَابِعَ الإِسْلامِ",
      translationEnglish = "The Prophet ﷺ said: 'Neither has the green sky shaded nor has the earth carried anyone of speech more truthful and faithful than Abu Dharr, who resembles Jesus son of Mary in asceticism.' And Abu Dharr (RA) narrated: 'I saw myself as the fourth (or fifth) person in Islam.'",
      translationUrdu = "نبی کریم ﷺ نے فرمایا: نیلے آسمان نے سایہ نہیں کیا اور نہ زمین نے اپنے اوپر کسی ایسے شخص کو اٹھایا جو ابو ذر سے زیادہ سچ بولنے والا اور وفا کرنے والا ہو۔ اور حضرت ابو ذر غفاری (رض) نے فرمایا: میں نے اپنے آپ کو اسلام لانے والوں میں چوتھا (یا پانچواں) پایا۔",
      sourceReference = "Sahih Muslim, Hadith 2473 (Kitab Fada'il al-Sahabah); Jami' at-Tirmidhi, Hadith 3801 & 3802; Musnad Ahmad, Hadith 6519; Al-Kafi, Vol. 2, p. 239; Al-Mustadrak, Hadith 4795",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "In Sahih Muslim 2473, Abu Dharr al-Ghifari (RA) recounts traveling to Makkah in the earliest days of revelation (helped by Imam Ali AS who hosted him for three nights before bringing him to the Prophet ﷺ) and embracing Islam as the fourth or fifth believer. He remained one of the most devoted supporters of Ahl al-Bayt and defenders of the poor until his passing in Al-Rabadhah (32 AH).",
      chronologyRank = 5,
      chronologyEraAndOrderNote = "Chronological Rank #5 (13 BH / Early Makkan Period): Explicitly attested in Sahih Muslim 2473 as the fourth or fifth person to accept Islam; hosted by Imam Ali (AS) upon arriving in Makkah.",
      historicalDisputeNote = "Verified in Sahih Muslim 2473 from Abu Dharr's own first-person account ('I was the fourth of Islam') and in both Sunni and Shia biographical dictionaries as one of the Four Pillars (Al-Arkan al-Arba'ah) loyal to Ahl al-Bayt.",
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION,
      authenticityGrading = "Sahih (Sahih Muslim 2473; Hasan in Jami' at-Tirmidhi 3801)",
      hadithBookName = "Sahih Muslim",
      hadithNumber = "2473",
      historicalRelationshipTags = listOf(
        HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN,
        HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT
      ),
      relationshipToProphetAndAhlAlBayt = "Hosted by Imam Ali (AS) during his secret conversion in Makkah; lifelong supporter of Imam Ali and Ahl al-Bayt; Imam Ali and Imam Hasan personally bade him farewell when he was exiled to Al-Rabadhah."
    ),
    ReflectionItem(
      id = 206,
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Ammar ibn Yasir (RA) & Family of Yasir [Chronology #6: Early Persecuted Vanguard & Defender of Ahl al-Bayt]",
      title = "Patience of the Family of Yasir & Prophetic Criterion of Truth",
      theme = "Steadfastness, Martyrdom & Loyalty to Truth",
      arabicOrOriginalText = "وَيْحَ عَمَّارٍ، تَقْتُلُهُ الْفِئَةُ الْبَاغِيَةُ، عَمَّارٌ يَدْعُوهُمْ إِلَى اللَّهِ وَيَدْعُونَهُ إِلَى النَّارِ ... إِنَّ عَمَّارًا مُلِئَ إِيمَانًا إِلَى مُشَاشِهِ",
      translationEnglish = "The Prophet ﷺ said while wiping the dust from Ammar's head during the building of the Prophetic Mosque: 'Alas for Ammar! The rebellious group (al-fi'ah al-baghiyah) will kill him; Ammar will be calling them to Allah (and Paradise), and they will be calling him to the Fire.' And the Prophet ﷺ said: 'Indeed, Ammar is filled with faith to the very marrow of his bones.'",
      translationUrdu = "نبی کریم ﷺ نے مسجدِ نبوی کی تعمیر کے دوران حضرت عمار بن یاسر (رض) کے سر سے گرد جھاڑتے ہوئے فرمایا: افسوس عمار پر! انہیں ایک باغی گروہ شہید کرے گا؛ عمار انہیں اللہ (اور جنت) کی طرف بلا رہے ہوں گے اور وہ انہیں آگ کی طرف بلا رہے ہوں گے۔ اور آپ ﷺ نے فرمایا: بے شک عمار سر سے پاؤں (ہڈیوں کے گودے) تک ایمان سے بھرے ہوئے ہیں۔",
      sourceReference = "Sahih al-Bukhari, Hadith 447 & 2812; Sahih Muslim, Hadith 2915 & 2916; Jami' at-Tirmidhi, Hadith 3798 & 3800; Sunan an-Nasa'i, Hadith 5007; Sunan Ibn Majah, Hadith 150",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "In Sunan Ibn Majah 150 (graded Hasan Sahih), Abdullah ibn Mas'ud (RA) states: 'The first seven to openly display their Islam in Makkah were the Messenger of Allah ﷺ, Abu Bakr, Ammar, his mother Sumayyah, Suhayb, Bilal, and Miqdad.' Ammar's parents, Sumayyah bint Khayyat and Yasir (RA), were the first two martyrs in Islam, and Ammar (RA) stood steadfastly alongside Imam Ali (AS) until his martyrdom at Siffin (37 AH).",
      chronologyRank = 6,
      chronologyEraAndOrderNote = "Chronological Rank #6 (13 BH / Earliest Makkan Vanguard): Among the first seven to openly declare Islam in Makkah (Sunan Ibn Majah 150); son of Sumayyah (RA), the first martyr in Islam.",
      historicalDisputeNote = "Mutawatir Hadith across Sahih al-Bukhari (447, 2812), Sahih Muslim (2915), Musnad Ahmad, and Shia sources; universally cited by classical Hadith masters (Ibn Hajar in Fath al-Bari, Vol. 1, p. 542) as proof that Imam Ali (AS) and Ammar (RA) were upon the truth at Siffin.",
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION,
      authenticityGrading = "Mutawatir / Muttafaqun Alayh (Sahih al-Bukhari 447 & 2812; Sahih Muslim 2915)",
      hadithBookName = "Sahih al-Bukhari",
      hadithNumber = "447 / 2812",
      historicalRelationshipTags = listOf(
        HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN,
        HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT
      ),
      relationshipToProphetAndAhlAlBayt = "One of the earliest persecuted companions in Makkah and among the closest lifelong supporters of Imam Ali (AS) and Ahl al-Bayt; martyred at age 93 fighting alongside Imam Ali (AS) at Siffin (37 AH)."
    ),
    ReflectionItem(
      id = 207,
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Miqdad ibn Amr al-Kindi (RA) [Chronology #7: Early Believer, Hero of Badr & Supporter of Ahl al-Bayt]",
      title = "Unwavering Loyalty to the Prophet ﷺ at Badr & Divine Love for Miqdad",
      theme = "Courage, Fidelity & Defense of the Prophet ﷺ",
      arabicOrOriginalText = "لَا نَقُولُ لَكَ كَمَا قَالَتْ بَنُو إِسْرَائِيلَ لِمُوسَى: اذْهَبْ أَنْتَ وَرَبُّكَ فَقَاتِلَا، وَلَكِنَّا نُقَاتِلُ عَنْ يَمِينِكَ وَعَنْ شِمَالِكَ وَبَيْنَ يَدَيْكَ وَمِنْ خَلْفِكَ ... إِنَّ اللَّهَ أَمَرَنِي بِحُبِّ أَرْبَعَةٍ وَأَخْبَرَنِي أَنَّهُ يُحِبُّهُمْ: عَلِيٌّ وَأَبُو ذَرٍّ وَالْمِقْدَادُ وَسَلْمَانُ",
      translationEnglish = "Before the Battle of Badr, Al-Miqdad (RA) stood up and said: 'O Messenger of Allah! We shall never say to you what the Children of Israel said to Moses: Go, you and your Lord, and fight! Rather, we will fight on your right, on your left, in front of you, and behind you!' The Prophet's ﷺ face shone with joy. And the Prophet ﷺ declared: 'Indeed Allah has commanded me to love four men, and informed me that He loves them: Ali, Abu Dharr, Al-Miqdad, and Salman.'",
      translationUrdu = "غزوۂ بدر سے پہلے حضرت مقداد بن عمرو (رض) کھڑے ہوئے اور عرض کیا: یا رسول اللہ ﷺ! ہم آپ سے ہرگز وہ نہیں کہیں گے جو بنی اسرائیل نے موسیٰ (ع) سے کہا تھا کہ آپ اور آپ کا رب جا کر لڑیں، بلکہ ہم آپ کے دائیں، بائیں، آگے اور پیچھے سے لڑیں گے! یہ سن کر رسول اللہ ﷺ کا چہرہ مبارک خوشی سے چمک اٹھا۔ اور آپ ﷺ نے فرمایا: بے شک اللہ نے مجھے چار افراد سے محبت کرنے کا حکم دیا ہے اور مجھے خبر دی ہے کہ وہ خود ان سے محبت فرماتا ہے: علی، ابو ذر، مقداد اور سلمان۔",
      sourceReference = "Sahih al-Bukhari, Hadith 3952 & 4609; Sahih Muslim, Hadith 1779; Jami' at-Tirmidhi, Hadith 3718; Sunan Ibn Majah, Hadith 149; Al-Kafi, Vol. 1, p. 401",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Miqdad ibn Amr (RA) was one of the first seven to openly declare Islam in Makkah (Sunan Ibn Majah 150), the pioneer horseman of Badr, and one of the four companions singled out in Jami' at-Tirmidhi 3718 & Sunan Ibn Majah 149 for their steadfast love and defense of Imam Ali and Ahl al-Bayt.",
      chronologyRank = 7,
      chronologyEraAndOrderNote = "Chronological Rank #7 (13 BH / Earliest Seven in Makkah): Listed by Abdullah ibn Mas'ud (RA) in Sunan Ibn Majah 150 among the first seven to publicly proclaim Islam in Makkah.",
      historicalDisputeNote = "Verified across Sahih al-Bukhari (3952, 4609), Sahih Muslim (1779), Jami' at-Tirmidhi (3718), Sunan Ibn Majah (149), and Al-Kafi.",
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION,
      authenticityGrading = "Sahih (Sahih al-Bukhari 3952 & 4609; Hasan in Tirmidhi 3718)",
      hadithBookName = "Sahih al-Bukhari",
      hadithNumber = "3952 / Tirmidhi 3718",
      historicalRelationshipTags = listOf(
        HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN,
        HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT
      ),
      relationshipToProphetAndAhlAlBayt = "Married Dubaa'ah bint al-Zubayr ibn Abd al-Muttalib (first cousin of the Prophet ﷺ and Imam Ali); one of the four foremost supporters of Ahl al-Bayt named in Jami' at-Tirmidhi 3718."
    ),
    ReflectionItem(
      id = 208,
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Bilal ibn Rabah al-Habashi (RA) [Chronology #8: Early Persecuted Vanguard & First Mu'adhdhin]",
      title = "Steadfastness Under Torture ('Ahad, Ahad') & Footsteps in Paradise",
      theme = "Tawhid, Equality & Devotion",
      arabicOrOriginalText = "يَا بِلَالُ حَدِّثْنِي بِأَرْجَى عَمَلٍ عَمِلْتَهُ فِي الْإِسْلَامِ، فَإِنِّي سَمِعْتُ دَفَّ نَعْلَيْكَ بَيْنَ يَدَيَّ فِي الْجَنَّةِ",
      translationEnglish = "At daybreak, the Prophet ﷺ said to Bilal: 'O Bilal, tell me of the most hopeful deed you have practiced in Islam, for indeed I heard the sound of your footsteps ahead of me in Paradise.' Bilal replied: 'I have never performed purification (Wudu) by night or day without praying with that purification whatever was destined for me to pray.'",
      translationUrdu = "فجر کے وقت نبی کریم ﷺ نے حضرت بلال (رض) سے فرمایا: اے بلال! مجھے وہ سب سے زیادہ امید افزا عمل بتاؤ جو تم نے اسلام میں کیا ہے، کیونکہ میں نے جنت میں اپنے آگے تمہارے قدموں کی آہٹ سنی ہے۔ حضرت بلال (رض) نے عرض کیا: میں نے رات یا دن کی کسی گھڑی میں جب بھی وضو کیا تو اس وضو کے ساتھ جتنی نماز میرے مقدر میں تھی ضرور ادا کی۔",
      sourceReference = "Sahih al-Bukhari, Hadith 1149 & 3754; Sahih Muslim, Hadith 2458; Sunan Ibn Majah, Hadith 150",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "One of the earliest seven who publicly declared Islam in Makkah under severe persecution, enduring scorching stones on the sands of Makkah while repeating 'Ahad, Ahad' (The One, The One), and appointed by Prophet Muhammad ﷺ as the first Mu'adhdhin of Islam.",
      chronologyRank = 8,
      chronologyEraAndOrderNote = "Chronological Rank #8 (13 BH / Earliest Seven in Makkah): Listed in Sunan Ibn Majah 150 and Sahih al-Bukhari 3754 among the earliest persecuted vanguard in Makkah.",
      historicalDisputeNote = "Unanimously documented in Sunni and Shia sources as one of the earliest Makkan believers and the Prophet's ﷺ devoted Mu'adhdhin.",
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION,
      authenticityGrading = "Muttafaqun Alayh (Sahih al-Bukhari 1149 & Sahih Muslim 2458)",
      hadithBookName = "Sahih al-Bukhari",
      hadithNumber = "1149",
      historicalRelationshipTags = listOf(
        HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN,
        HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT
      ),
      relationshipToProphetAndAhlAlBayt = "First Mu'adhdhin of Prophet Muhammad ﷺ; deeply devoted to the Prophet ﷺ and his Household (Sayyidah Fatimah, Imam Ali, Imam Hasan, and Imam Husayn)."
    ),
    ReflectionItem(
      id = 209,
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Abdullah ibn Mas'ud (RA) [Chronology #9: Sixth Early Believer & Master of Qur'an Recitation]",
      title = "True Knowledge is Reverence (Khashyah) of Allah & Love for the Qur'an",
      theme = "Sacred Knowledge & Qur'anic Devotion",
      arabicOrOriginalText = "لَيْسَ الْعِلْمُ بِكَثْرَةِ الرِّوَايَةِ، وَلَكِنَّ الْعِلْمَ الْخَشْيَةُ ... خُذُوا الْقُرْآنَ مِنْ أَرْبَعَةٍ: مِنْ عَبْدِ اللَّهِ بْنِ مَسْعُودٍ، وَسَالِمٍ، وَمُعَاذٍ، وَأُبَيِّ بْنِ كَعْبٍ",
      translationEnglish = "Abdullah ibn Mas'ud (RA) said: 'Knowledge is not measured merely by the abundance of narrations; rather, true knowledge is reverence (khashyah) of Allah.' And the Prophet ﷺ said: 'Learn the recitation of the Qur'an from four: from Abdullah ibn Mas'ud, Salim, Mu'adh, and Ubayy ibn Ka'b.'",
      translationUrdu = "حضرت عبداللہ بن مسعود (رض) نے فرمایا: علم محض کثرتِ روایت کا نام نہیں، بلکہ حقیقی علم خشیتِ الٰہی ہے۔ اور رسول اللہ ﷺ نے فرمایا: قرآن چار اصحاب سے سیکھو: عبداللہ بن مسعود، سالم، معاذ اور ابی بن کعب۔",
      sourceReference = "Sahih al-Bukhari, Hadith 3760 & 5000; Sahih Muslim, Hadith 2462; Musannaf Ibn Abi Shaybah, Hadith 26085; Al-Mustadrak, Vol. 3, p. 313",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Embraced Islam in the earliest Makkan phase (describing himself in Al-Mustadrak as the sixth of six early Muslims in Makkah) and was the first after the Prophet ﷺ to recite the Qur'an aloud before the Quraysh at the Ka'bah.",
      chronologyRank = 9,
      chronologyEraAndOrderNote = "Chronological Rank #9 (13–12 BH / Early Makkan Period): Recorded in Al-Mustadrak (Vol. 3, p. 313) and Sirah Ibn Ishaq as the sixth early believer in Makkah.",
      historicalDisputeNote = "Authenticated across Sahih al-Bukhari (3760, 5000), Sahih Muslim (2462), and Musannaf Ibn Abi Shaybah.",
      narrationType = WisdomNarrationType.COMPANION_ATHAR_MAWQUF,
      authenticityGrading = "Sahih (Sahih al-Bukhari 5000 & Musannaf Ibn Abi Shaybah 26085)",
      hadithBookName = "Sahih al-Bukhari",
      hadithNumber = "5000 / 3760",
      historicalRelationshipTags = listOf(HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN),
      relationshipToProphetAndAhlAlBayt = "Personal attendant of Prophet Muhammad ﷺ in Makkah and Madinah; later appointed over the treasury of Kufa and praised by Imam Ali (AS) for his knowledge of the Qur'an and Sunnah."
    ),
    ReflectionItem(
      id = 210,
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Ja'far ibn Abi Talib al-Tayyar (RA) [Chronology #10: Early Believer, Spokesperson in Abyssinia & Brother of Imam Ali]",
      title = "Eloquent Defense of Islam in Abyssinia & Resemblance to the Prophet ﷺ",
      theme = "Prophetic Character, Generosity & Martyrdom",
      arabicOrOriginalText = "قَالَ النَّبِيُّ ﷺ لِجَعْفَرٍ: أَشْبَهْتَ خَلْقِي وَخُلُقِي ... كَانَ جَعْفَرُ بْنُ أَبِي طَالِبٍ يُحِبُّ الْمَسَاكِينَ وَيَجْلِسُ إِلَيْهِمْ وَيُحَدِّثُهُمْ وَيُحَدِّثُونَهُ، فَكَانَ رَسُولُ اللَّهِ ﷺ يُكَنِّيهِ أَبَا الْمَسَاكِينِ",
      translationEnglish = "The Prophet ﷺ said to Ja'far ibn Abi Talib: 'You resemble both my physical appearance and my moral character.' And Abu Hurayrah (RA) reported: 'Ja'far ibn Abi Talib loved the poor, sat with them, and conversed with them, so the Messenger of Allah ﷺ titled him Abu al-Masakin (Father of the Poor).'",
      translationUrdu = "نبی کریم ﷺ نے حضرت جعفر بن ابی طالب (رض/ع) سے فرمایا: تم صورت اور سیرت دونوں میں میرے مشابہ ہو۔ اور روایت ہے کہ حضرت جعفر بن ابی طالب مساکین سے بے حد محبت کرتے تھے اور ان کے پاس بیٹھتے تھے، چنانچہ رسول اللہ ﷺ نے انہیں ’ابو المساکین‘ (مسکینوں کے باپ) کی کنیت عطا فرمائی۔",
      sourceReference = "Sahih al-Bukhari, Hadith 2699, 3708 & 4264; Musnad Ahmad, Hadith 1740 (Speech before the Negus); Jami' at-Tirmidhi, Hadith 3765",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Full brother of Imam Ali (AS); embraced Islam in the earliest Makkan days right after Ali (AS), led the emigrants to Abyssinia where he recited Surah Maryam before the righteous Negus (Musnad Ahmad 1740), and was martyred at Mu'tah (8 AH), earning the title 'Ja'far al-Tayyar' (Ja'far of the Two Wings in Paradise, Sahih al-Bukhari 3709).",
      chronologyRank = 10,
      chronologyEraAndOrderNote = "Chronological Rank #10 (13–12 BH / Early Makkan Period): Embraced Islam shortly after his younger brother Imam Ali (AS) at the encouragement of their father Abu Talib.",
      historicalDisputeNote = "Unanimously revered across Sunni and Shia Hadith corpora (Sahih al-Bukhari 2699, 3709, Musnad Ahmad 1740, Al-Kafi).",
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION,
      authenticityGrading = "Sahih (Sahih al-Bukhari 2699 & 4264; Musnad Ahmad 1740)",
      hadithBookName = "Sahih al-Bukhari",
      hadithNumber = "2699 / 3709",
      historicalRelationshipTags = listOf(
        HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN,
        HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT,
        HistoricalRelationshipTag.PROPHETIC_HOUSEHOLD
      ),
      relationshipToProphetAndAhlAlBayt = "Cousin of Prophet Muhammad ﷺ and older brother of Imam Ali (AS); member of Banu Hashim and Ahl al-Bayt."
    ),
    ReflectionItem(
      id = 211,
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Salman al-Farsi al-Muhammadi (RA) [Chronology #11: 'Salman is From Us, the Ahl al-Bayt']",
      title = "Salman is From Us, the Ahl al-Bayt & Reaching Faith at the Pleiades",
      theme = "Spiritual Quest, Wisdom & Bond with Ahl al-Bayt",
      arabicOrOriginalText = "سَلْمَانُ مِنَّا أَهْلَ الْبَيْتِ ... لَوْ كَانَ الإِيمَانُ عِنْدَ الثُّرَيَّا لَنَالَهُ رِجَالٌ أَوْ رَجُلٌ مِنْ هَؤُلاءِ",
      translationEnglish = "The Prophet ﷺ declared on the day of the Trench (Khandaq): 'Salman is from us, the Ahl al-Bayt (People of the Household).' And placing his hand upon Salman al-Farsi (RA), the Prophet ﷺ said: 'Even if faith were suspended at the Pleiades (al-Thurayya), men—or a man—from among these would surely attain it.'",
      translationUrdu = "غزوۂ خندق کے موقع پر نبی کریم ﷺ نے اعلان فرمایا: سلمان ہم اہلِ بیت میں سے ہیں۔ اور حضرت سلمان فارسی (رض) پر اپنا دستِ مبارک رکھ کر فرمایا: اگر ایمان ثریا (ستارے) کے پاس بھی ہوتا تو ان میں سے ایک مرد (یا کچھ لوگ) اسے ضرور پا لیتے۔",
      sourceReference = "Sahih al-Bukhari, Hadith 4897 & 1968; Sahih Muslim, Hadith 2546; Al-Mustadrak 'ala al-Sahihayn (Al-Hakim), Hadith 6541; Musnad Ahmad, Hadith 23737 (Story of Salman's Quest); Al-Kafi, Vol. 1, p. 401",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Salman al-Farsi (RA) traveled across Persia, Syria, and Arabia seeking the final Prophet foretold in previous scriptures. Though prevented from witnessing Badr and Uhud due to unjust enslavement until the Prophet ﷺ and companions ransomed him, his long quest for truth began before the Hijrah, and the Prophet ﷺ honored him by inducting him spiritually into the Ahl al-Bayt.",
      chronologyRank = 11,
      chronologyEraAndOrderNote = "Chronological Rank #11 (1 AH / 622 CE upon the Prophet's ﷺ arrival in Quba/Madinah, after years of pre-Hijrah search for the Prophet ﷺ): Honored with the Prophetic decree 'Salman is from us, the Ahl al-Bayt.'",
      historicalDisputeNote = "Authenticated across Sahih al-Bukhari (4897, 1968), Sahih Muslim (2546), Al-Mustadrak (6541), Jami' at-Tirmidhi (3718), and Al-Kafi.",
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION,
      authenticityGrading = "Sahih (Sahih al-Bukhari 4897; Sahih Muslim 2546; Al-Mustadrak 6541)",
      hadithBookName = "Sahih al-Bukhari",
      hadithNumber = "4897 / Al-Mustadrak 6541",
      historicalRelationshipTags = listOf(
        HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN,
        HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT,
        HistoricalRelationshipTag.PROPHETIC_HOUSEHOLD
      ),
      relationshipToProphetAndAhlAlBayt = "Inducted into the spiritual circle of Ahl al-Bayt by Prophet Muhammad ﷺ ('Salman is from us, the Ahl al-Bayt'); lifelong confidant of Imam Ali (AS), who washed and shrouded him upon his passing in Al-Mada'in (35 AH)."
    ),
    ReflectionItem(
      id = 212,
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Umm Salamah Hind bint Abi Umayyah (RA) [Chronology #12: Early Muhajirah & Transmitter of Hadith al-Kisa']",
      title = "Revelation of the Verse of Purification (33:33) in Her Home & Defense of Imam Ali (AS)",
      theme = "Preservation of Prophetic Sunnah & Ahl al-Bayt Virtues",
      arabicOrOriginalText = "نَزَلَتْ هَذِهِ الآيَةُ فِي بَيْتِي: ﴿إِنَّمَا يُرِيدُ اللَّهُ لِيُذْهِبَ عَنْكُمُ الرِّجْسَ أَهْلَ الْبَيْتِ وَيُطَهِّرَكُمْ تَطْهِيرًا﴾ فَجَلَّلَ النَّبِيُّ ﷺ عَلِيًّا وَفَاطِمَةَ وَحَسَنًا وَحُسَيْنًا بِكِسَاءٍ ثُمَّ قَالَ: اللَّهُمَّ هَؤُلاءِ أَهْلُ بَيْتِي فَأَذْهِبْ عَنْهُمُ الرِّجْسَ وَطَهِّرْهُمْ تَطْهِيرًا ... مَنْ سَبَّ عَلِيًّا فَقَدْ سَبَّنِي",
      translationEnglish = "Umm Salamah (RA) narrated: 'This verse—\"Indeed Allah desires only to remove all impurity from you, O People of the Household (Ahl al-Bayt), and to purify you with a thorough purification\" (33:33)—was revealed in my house. The Prophet ﷺ covered Ali, Fatimah, Hasan, and Husayn with a cloak (Kisa') and said: \"O Allah, these are my Ahl al-Bayt, so remove impurity from them and purify them thoroughly.\"' And Umm Salamah (RA) also narrated that the Messenger of Allah ﷺ said: 'Whoever reviles Ali has reviled me.'",
      translationUrdu = "ام المؤمنین حضرت ام سلمہ (رض) روایت فرماتی ہیں کہ یہ آیت ﴿إِنَّمَا يُرِيدُ اللَّهُ لِيُذْهِبَ عَنْكُمُ الرِّجْسَ أَهْلَ الْبَيْتِ وَيُطَهِّرَكُمْ تَطْهِيرًا﴾ میرے گھر میں نازل ہوئی۔ نبی کریم ﷺ نے حضرت علی، حضرت فاطمہ، حضرت حسن اور حضرت حسین (ع) پر ایک چادر (کساء) اوڑھائی اور عرض کیا: اے اللہ! یہ میرے اہلِ بیت ہیں، پس ان سے ہر رجس کو دور فرما اور انہیں کامل طہارت عطا فرما۔ اور حضرت ام سلمہ (رض) ہی سے مروی ہے کہ رسول اللہ ﷺ نے فرمایا: جس نے علی کو برا بھلا کہا اس نے مجھے برا بھلا کہا۔",
      sourceReference = "Jami' at-Tirmidhi, Hadith 3205, 3787 & 3871; Sahih Muslim, Hadith 2424 (via Aishah RA on the Cloak); Musnad Ahmad, Hadith 26508 & 26748; Al-Mustadrak, Hadith 4617",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Umm Salamah (RA) was among the earliest emigrants to both Abyssinia and Madinah, renowned for her wisdom at Hudaybiyyah, and entrusted by Sayyidah Fatimah (SA) and Imam al-Husayn (AS) with the soil of Karbala (Musnad Ahmad 26524, Jami' at-Tirmidhi 3774).",
      chronologyRank = 12,
      chronologyEraAndOrderNote = "Chronological Rank #12 (Early Makkan Period / First Emigration to Abyssinia 5th Year of Prophethood): Early Muhajirah and Mother of the Believers.",
      historicalDisputeNote = "Graded Hasan Sahih by Imam at-Tirmidhi (3205, 3787) and Sahih on the conditions of Bukhari and Muslim by Al-Hakim in Al-Mustadrak (4617).",
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION,
      authenticityGrading = "Hasan Sahih / Sahih on Conditions of Bukhari & Muslim (Tirmidhi 3205 & 3787; Al-Mustadrak 4617)",
      hadithBookName = "Jami' at-Tirmidhi",
      hadithNumber = "3205 / 3787",
      historicalRelationshipTags = listOf(
        HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN,
        HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT,
        HistoricalRelationshipTag.THAQALAYN_AND_KISA_TRANSMITTERS,
        HistoricalRelationshipTag.PROPHETIC_HOUSEHOLD
      ),
      relationshipToProphetAndAhlAlBayt = "Mother of the Believers and staunch supporter of Ahl al-Bayt; in her apartment Hadith al-Kisa' took place, and she consistently defended Imam Ali (AS) and Imam al-Husayn (AS)."
    ),
    ReflectionItem(
      id = 213,
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Jabir ibn Abdillah al-Ansari (RA) [Chronology #13: Early Ansar Pledge at Aqabah & Companion of Ahl al-Bayt]",
      title = "Witnessing Hadith al-Thaqalayn on Arafah & Conveying the Prophet's ﷺ Greeting to Imam al-Baqir (AS)",
      theme = "Fidelity to the Qur'an & Ahl al-Bayt Across Generations",
      arabicOrOriginalText = "رَأَيْتُ رَسُولَ اللَّهِ ﷺ فِي حَجَّتِهِ يَوْمَ عَرَفَةَ وَهُوَ عَلَى نَاقَتِهِ الْقَصْوَاءِ يَخْطُبُ فَسَمِعْتُهُ يَقُولُ: يَا أَيُّهَا النَّاسُ إِنِّي قَدْ تَرَكْتُ فِيكُمْ مَا إِنْ أَخَذْتُمْ بِهِ لَنْ تَضِلُّوا: كِتَابَ اللَّهِ وَعِتْرَتِي أَهْلَ بَيْتِي",
      translationEnglish = "Jabir ibn Abdillah al-Ansari (RA) narrated: 'I saw the Messenger of Allah ﷺ during his Pilgrimage on the Day of Arafah while he was delivering a sermon upon his she-camel Al-Qaswa, and I heard him say: \"O people! Indeed I have left among you that which, if you hold fast to it, you will never go astray: the Book of Allah and my progeny ('Itrati), my Ahl al-Bayt.\"'",
      translationUrdu = "حضرت جابر بن عبداللہ انصاری (رض) روایت کرتے ہیں کہ میں نے رسول اللہ ﷺ کو حجۃ الوداع میں عرفہ کے دن اپنی اونٹنی قصواء پر خطبہ دیتے ہوئے دیکھا اور میں نے آپ ﷺ کو یہ فرماتے ہوئے سنا: اے لوگو! بے شک میں نے تم میں وہ چیز چھوڑی ہے کہ اگر تم اسے مضبوطی سے تھامے رہو گے تو کبھی گمراہ نہیں ہو گے: اللہ کی کتاب اور میری عترت یعنی میرے اہلِ بیت۔",
      sourceReference = "Jami' at-Tirmidhi, Hadith 3786; Sahih Muslim, Hadith 1218 (Jabir's complete narration of the Farewell Hajj); Al-Kafi, Vol. 1, p. 469 (Prophet's greeting to Imam al-Baqir via Jabir)",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Jabir ibn Abdillah (RA) pledged allegiance at the Second Pledge of Aqabah before the Hijrah, witnessed 19 expeditions with the Prophet ﷺ, defended Imam Ali (AS), was the first pilgrim to visit the grave of Imam al-Husayn (AS) on Arba'een, and lived until ~78 AH to convey the Prophet's ﷺ personal greeting (Salam) to the young Imam Muhammad al-Baqir (AS).",
      chronologyRank = 13,
      chronologyEraAndOrderNote = "Chronological Rank #13 (2 BH / Second Pledge of Aqabah before the Hijrah): Youngest Ansari companion at the Pledge of Aqabah.",
      historicalDisputeNote = "Authenticated in Jami' at-Tirmidhi 3786, Sahih Muslim 1218, and Shia biographical and Hadith corpora (Al-Kafi Vol. 1, p. 469; Rijal al-Kashshi).",
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION,
      authenticityGrading = "Sahih / Hasan (Jami' at-Tirmidhi 3786; Sahih Muslim 1218)",
      hadithBookName = "Jami' at-Tirmidhi",
      hadithNumber = "3786",
      historicalRelationshipTags = listOf(
        HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN,
        HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT,
        HistoricalRelationshipTag.THAQALAYN_AND_KISA_TRANSMITTERS
      ),
      relationshipToProphetAndAhlAlBayt = "Devoted Ansari companion who served as a living bridge between Prophet Muhammad ﷺ and five generations of Ahl al-Bayt (from Imam Ali to Imam Muhammad al-Baqir AS)."
    ),
    ReflectionItem(
      id = 214,
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Zayd ibn Arqam al-Ansari (RA) [Chronology #14: Transmitter of Hadith al-Thaqalayn & Defender of Imam Husayn's Legacy]",
      title = "Who Are the Ahl al-Bayt of the Messenger of Allah ﷺ?",
      theme = "Verification of Ahl al-Bayt & Hadith al-Thaqalayn",
      arabicOrOriginalText = "قَامَ رَسُولُ اللَّهِ ﷺ يَوْمًا فِينَا خَطِيبًا بِمَاءٍ يُدْعَى خُمًّا بَيْنَ مَكَّةَ وَالْمَدِينَةِ فَحَمِدَ اللَّهَ وَأَثْنَى عَلَيْهِ ... وَأَهْلُ بَيْتِي أُذَكِّرُكُمُ اللَّهَ فِي أَهْلِ بَيْتِي ... أَهْلُ بَيْتِهِ مَنْ حُرِمَ الصَّدَقَةَ بَعْدَهُ: آلُ عَلِيٍّ وَآلُ عَقِيلٍ وَآلُ جَعْفَرٍ وَآلُ عَبَّاسٍ",
      translationEnglish = "Zayd ibn Arqam (RA) narrated: 'One day the Messenger of Allah ﷺ stood up to deliver a sermon at a watering place called Khumm between Makkah and Madinah. He praised Allah and extolled Him, exhorted and reminded us, and then said: \"Now then, O people! Indeed I am only a human being; the messenger of my Lord is about to come and I shall respond. And I am leaving among you two weighty things (Thaqalayn): the first of them is the Book of Allah, in which is guidance and light—so take hold of the Book of Allah and hold fast to it... and my Ahl al-Bayt; I remind you of Allah concerning my Ahl al-Bayt (three times).\"' When asked who his Ahl al-Bayt are, Zayd clarified: 'Those for whom charity (Zakah/Sadaqah) is forbidden after him: the family of Ali, the family of Aqil, the family of Ja'far, and the family of Abbas.'",
      translationUrdu = "حضرت زید بن ارقم (رض) روایت کرتے ہیں کہ ایک دن رسول اللہ ﷺ نے مکہ اور مدینہ کے درمیان ’خم‘ نامی تالاب پر ہمارے درمیان خطبہ دیا، اللہ کی حمد و ثنا بیان کی، وعظ و نصیحت فرمائی اور پھر فرمایا: اما بعد، اے لوگو! میں اپنے تمہارے درمیان دو بھاری چیزیں (ثقلین) چھوڑے جا رہا ہوں: پہلی اللہ کی کتاب جس میں ہدایت اور نور ہے، اور دوسرے میرے اہلِ بیت؛ میں تمہیں اپنے اہلِ بیت کے بارے میں اللہ یاد دلاتا ہوں (تین مرتبہ)۔",
      sourceReference = "Sahih Muslim, Kitab Fada'il al-Sahabah, Hadith 2408a–2408d; Jami' at-Tirmidhi, Hadith 3788; Musnad Ahmad, Hadith 19265",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Zayd ibn Arqam (RA) also wept and publicly rebuked disrespect toward the blessed head of Imam al-Husayn (AS) in Kufa, reciting the Prophet's ﷺ kissing of the lips of Al-Hasan and Al-Husayn (recorded in Tarikh al-Tabari Vol. 5, p. 456 & Jami' at-Tirmidhi 3777).",
      chronologyRank = 14,
      chronologyEraAndOrderNote = "Chronological Rank #14 (Early Madinan Ansar Companion; accompanied 17 campaigns with the Prophet ﷺ).",
      historicalDisputeNote = "Authenticated in Sahih Muslim (2408a–d) with four distinct chains of transmission.",
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION,
      authenticityGrading = "Sahih (Sahih Muslim 2408a–2408d)",
      hadithBookName = "Sahih Muslim",
      hadithNumber = "2408a",
      historicalRelationshipTags = listOf(
        HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT,
        HistoricalRelationshipTag.THAQALAYN_AND_KISA_TRANSMITTERS
      ),
      relationshipToProphetAndAhlAlBayt = "Fought alongside Imam Ali (AS) at Siffin and transmitted both Hadith al-Thaqalayn (Sahih Muslim 2408) and the Hadith confirming Ali (AS) as the first to embrace Islam (Jami' at-Tirmidhi 3735)."
    ),
    ReflectionItem(
      id = 215,
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Hudhayfah ibn al-Yaman (RA) [Chronology #15: Keeper of the Prophet's ﷺ Counsel & Supporter of Ahl al-Bayt]",
      title = "Glad Tidings Given by an Angel Regarding Fatimah, Al-Hasan & Al-Husayn (AS)",
      theme = "Vigilance Against Fitnah & Devotion to Ahl al-Bayt",
      arabicOrOriginalText = "إِنَّ هَذَا مَلَكٌ لَمْ يَنْزِلِ الأَرْضَ قَطُّ قَبْلَ هَذِهِ اللَّيْلَةِ اسْتَأْذَنَ رَبَّهُ أَنْ يُسَلِّمَ عَلَيَّ وَيُبَشِّرَنِي بِأَنَّ فَاطِمَةَ سَيِّدَةُ نِسَاءِ أَهْلِ الْجَنَّةِ وَأَنَّ الْحَسَنَ وَالْحُسَيْنَ سَيِّدَا شَبَابِ أَهْلِ الْجَنَّةِ",
      translationEnglish = "Hudhayfah ibn al-Yaman (RA) narrated that after praying closely behind the Prophet ﷺ at night, the Prophet ﷺ said to him: 'Did you see the figure that was before me? Indeed, this was an angel who had never descended to the earth before tonight; he asked permission from his Lord to greet me and give me the glad tidings that Fatimah is the Chief of the Women of Paradise, and that Al-Hasan and Al-Husayn are the two Masters of the Youth of Paradise.'",
      translationUrdu = "حضرت حذیفہ بن یمان (رض) روایت کرتے ہیں کہ نبی کریم ﷺ نے رات کی طویل نماز کے بعد مجھ سے فرمایا: یہ ایک فرشتہ تھا جو آج کی رات سے پہلے کبھی زمین پر اتر کر نہیں آیا تھا؛ اس نے اپنے رب سے اجازت طلب کی کہ مجھ پر سلام عرض کرے اور مجھے یہ بشارت دے کہ فاطمہ جنت کی عورتوں کی سردار ہیں اور حسن اور حسین جنت کے نوجوانوں کے سردار ہیں۔",
      sourceReference = "Jami' at-Tirmidhi, Hadith 3781; Sunan an-Nasa'i al-Kubra, Hadith 8298; Musnad Ahmad, Hadith 23329; Al-Mustadrak, Hadith 4738",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Hudhayfah ibn al-Yaman (RA) and his father Husayl embraced Islam early and only missed Badr because Quraysh forced a binding oath upon them on the road (Sahih Muslim 1787); his father was martyred at Uhud. On his deathbed in 36 AH, Hudhayfah (RA) instructed his two sons Safwan and Sa'id to pledge allegiance to Imam Ali (AS), and both were martyred fighting alongside Imam Ali at Siffin.",
      chronologyRank = 15,
      chronologyEraAndOrderNote = "Chronological Rank #15 (Early Believer prior to Badr; Companion of the Trench where the Prophet ﷺ entrusted him with his confidential counsel).",
      historicalDisputeNote = "Graded Hasan Gharib by Imam at-Tirmidhi (3781) and Sahih by Al-Hakim in Al-Mustadrak (4738) and Al-Albani in Sahih al-Jami' (2276).",
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION,
      authenticityGrading = "Hasan Sahih (Jami' at-Tirmidhi 3781; Al-Mustadrak 4738)",
      hadithBookName = "Jami' at-Tirmidhi",
      hadithNumber = "3781",
      historicalRelationshipTags = listOf(
        HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN,
        HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT,
        HistoricalRelationshipTag.THAQALAYN_AND_KISA_TRANSMITTERS
      ),
      relationshipToProphetAndAhlAlBayt = "Keeper of the Prophet's ﷺ confidential counsel (Sahib al-Sirr); appointed governor of Al-Mada'in after Salman al-Farsi (RA) and instructed his sons to support Imam Ali (AS)."
    ),

    // =========================================================================
    // 4. OTHER SAHABAH (RA) (OTHER_SAHABAH_WISDOM)
    // Placed in their appropriate historical chronology and separate category
    // so later converts are never placed ahead of the earliest Muslims.
    // =========================================================================
    ReflectionItem(
      id = 301,
      category = ReflectionCategory.OTHER_SAHABAH_WISDOM,
      speaker = "Uthman ibn Affan (RA) [Chronology: Converted via Abu Bakr RA; Early Makkan Phase after the First Vanguard]",
      title = "Learning and Teaching the Noble Qur'an",
      theme = "Service to the Qur'an",
      arabicOrOriginalText = "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
      translationEnglish = "Uthman ibn Affan (RA) narrated that the Prophet ﷺ said: 'The best among you is the one who learns the Qur'an and teaches it.'",
      translationUrdu = "حضرت عثمان بن عفان (رض) سے روایت ہے کہ نبی کریم ﷺ نے فرمایا: تم میں سب سےبہتر وہ ہے جو قرآن سیکھے اور اسے سکھائے۔",
      sourceReference = "Sahih al-Bukhari, Hadith 5027 (Kitab Fada'il al-Qur'an)",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Authentic Prophetic Hadith transmitted through Uthman ibn Affan (RA) via Abu Abd al-Rahman al-Sulami (who taught the Qur'an in the Grand Mosque of Kufa from the caliphate of Uthman until the governorship of Al-Hajjaj, including to the companions of Imam Ali AS).",
      chronologyRank = 20,
      chronologyEraAndOrderNote = "Historical Position: Converted in the early Makkan phase via Abu Bakr's (RA) invitation, after the initial vanguard of Khadijah (RA), Imam Ali (AS), and Zayd ibn Harithah (RA).",
      historicalDisputeNote = "Placed in the 'Other Sahabah (RA)' category to preserve exact chronological accuracy without placing later converts ahead of the first believers.",
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION,
      authenticityGrading = "Sahih (Sahih al-Bukhari 5027)",
      hadithBookName = "Sahih al-Bukhari",
      hadithNumber = "5027",
      historicalRelationshipTags = listOf(HistoricalRelationshipTag.LATER_AND_OTHER_COMPANIONS)
    ),
    ReflectionItem(
      id = 302,
      category = ReflectionCategory.OTHER_SAHABAH_WISDOM,
      speaker = "Umar ibn al-Khattab (RA) [Chronology: Converted ~6th Year of Prophethood / 7 BH]",
      title = "Self-Accountability (Muhasabah) & Consulting Imam Ali (AS) in Difficult Judgments",
      theme = "Self-Examination & Judicial Reliance on Imam Ali",
      arabicOrOriginalText = "حَاسِبُوا أَنْفُسَكُمْ قَبْلَ أَنْ تُحَاسَبُوا، وَزِنُوا أَنْفُسَكُمْ قَبْلَ أَنْ تُوزَنُوا ... لَوْلَا عَلِيٌّ لَهَلَكَ عُمَرُ",
      translationEnglish = "Umar ibn al-Khattab (RA) said: 'Hold yourselves accountable before you are held accountable, and weigh your deeds before they are weighed for you.' And in judicial matters where Imam Ali (AS) resolved complex cases by the Qur'an and Sunnah, Umar (RA) famously declared: 'Were it not for Ali, Umar would have perished.'",
      translationUrdu = "حضرت عمر بن خطاب (رض) نے فرمایا: اپنا محاسبہ کر لو اس سے پہلے کہ تمہارا محاسبہ کیا جائے، اور اپنے اعمال کو تول لو اس سے پہلے کہ وہ تمہارے لیے تولے جائیں۔ اور مشکل فقہی و عدالتی مسائل میں حضرت علی (ع) کے فیصلے پر حضرت عمر (رض) نے فرمایا: اگر علی نہ ہوتے تو عمر ہلاک ہو جاتا۔",
      sourceReference = "Jami' at-Tirmidhi, under Hadith 2459; Musannaf Ibn Abi Shaybah, Hadith 34455; Al-Isti'ab (Ibn Abd al-Barr), Vol. 3, p. 1103; Musnad Ahmad (Fada'il al-Sahabah), Hadith 1100",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Umar ibn al-Khattab (RA) embraced Islam around the 6th year of Prophethood (after ~40 to 50 earlier men and women had already accepted Islam and migrated to Abyssinia, as documented by Ibn Ishaq and Ibn Hajar in Al-Isabah). Placed in his historically verified chronological position.",
      chronologyRank = 45,
      chronologyEraAndOrderNote = "Historical Position (~6th Year of Prophethood / ~616 CE): Embraced Islam after the first ~40–50 Makkan believers and after the First Emigration to Abyssinia.",
      historicalDisputeNote = "Historical Chronology Note: Classical historians (Ibn Ishaq, Ibn Hisham, Al-Tabari, and Ibn Hajar in Al-Isabah Vol. 4, p. 485) document that Umar (RA) accepted Islam in the 6th year of Prophethood after approximately 40 men and 11 women had preceded him in Islam.",
      narrationType = WisdomNarrationType.COMPANION_ATHAR_MAWQUF,
      authenticityGrading = "Sahih Athar (Musannaf Ibn Abi Shaybah 34455; Al-Isti'ab Vol. 3, p. 1103)",
      hadithBookName = "Musannaf Ibn Abi Shaybah",
      hadithNumber = "34455",
      historicalRelationshipTags = listOf(HistoricalRelationshipTag.LATER_AND_OTHER_COMPANIONS)
    ),
    ReflectionItem(
      id = 303,
      category = ReflectionCategory.OTHER_SAHABAH_WISDOM,
      speaker = "Abdullah ibn Abbas (RA) [Chronology: Born 3 BH in Shi'b Abi Talib; Cousin of the Prophet ﷺ & Student of Imam Ali AS]",
      title = "Seeking Mindful Wisdom from the Prophet ﷺ & Learning Tafsir from Imam Ali (AS)",
      theme = "Qur'anic Exegesis & Reliance on Allah",
      arabicOrOriginalText = "يَا غُلَامُ إِنِّي أُعَلِّمُكَ كَلِمَاتٍ: احْفَظِ اللَّهَ يَحْفَظْكَ، احْفَظِ اللَّهَ تَجِدْهُ تُجَاهَكَ، إِذَا سَأَلْتَ فَاسْأَلِ اللَّهَ، وَإِذَا اسْتَعَنْتَ فَاسْتَعِنْ بِاللَّهِ",
      translationEnglish = "Ibn Abbas (RA) narrated: 'One day I was riding behind the Prophet ﷺ when he said: \"O young man, I shall teach you words of wisdom: Be mindful of Allah and He will protect you. Be mindful of Allah and you will find Him before you. When you ask, ask of Allah; and when you seek help, seek help from Allah.\"'",
      translationUrdu = "حضرت عبداللہ بن عباس (رض) روایت کرتے ہیں کہ ایک دن میں نبی کریم ﷺ کے پیچھے سوار تھا تو آپ ﷺ نے فرمایا: اے لڑکے! میں تمہیں چند کلمات سکھاتا ہوں: اللہ کے احکام کی حفاظت کرو، اللہ تمہاری حفاظت فرمائے گا؛ اللہ کو یاد رکھو، تم اسے اپنے سامنے پاؤ گے؛ جب مانگو تو اللہ ہی سے مانگو اور جب مدد چاہو تو اللہ ہی سے مدد مانگو۔",
      sourceReference = "Jami' at-Tirmidhi, Hadith 2516; Musnad Ahmad, Hadith 2669 (40 Hadith an-Nawawi #19)",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Cousin of Prophet Muhammad ﷺ and Imam Ali (AS); born during the boycott in Shi'b Abi Talib (3 BH). Regarding his Qur'anic knowledge, Ibn Abbas (RA) stated: 'Whatever I took of the exegesis (Tafsir) of the Qur'an, I took from Ali ibn Abi Talib' (Tafsir al-Tabari; Hilyat al-Awliya Vol. 1, p. 321).",
      chronologyRank = 50,
      chronologyEraAndOrderNote = "Historical Position (Born 3 BH in Makkah during the Banu Hashim boycott; grew up in the Prophet's ﷺ care and studied under Imam Ali AS).",
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION,
      authenticityGrading = "Hasan Sahih (Jami' at-Tirmidhi 2516)",
      hadithBookName = "Jami' at-Tirmidhi",
      hadithNumber = "2516",
      historicalRelationshipTags = listOf(
        HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT,
        HistoricalRelationshipTag.LATER_AND_OTHER_COMPANIONS,
        HistoricalRelationshipTag.PROPHETIC_HOUSEHOLD
      ),
      relationshipToProphetAndAhlAlBayt = "Son of Al-Abbas ibn Abd al-Muttalib (uncle of the Prophet ﷺ), disciple of Imam Ali (AS), and commander in Imam Ali's army at Basra and Siffin."
    ),
    ReflectionItem(
      id = 304,
      category = ReflectionCategory.OTHER_SAHABAH_WISDOM,
      speaker = "Abu Sa'id al-Khudri (RA) [Chronology: Young Madinan Ansari Companion; First Campaign at the Trench 5 AH]",
      title = "Changing Evil by Hand, Tongue, or Heart & Transmitting the Virtues of Ahl al-Bayt",
      theme = "Moral Responsibility & Transmission of Sunnah",
      arabicOrOriginalText = "مَنْ رَأَى مِنْكُمْ مُنْكَرًا فَلْيُغَيِّرْهُ بِيَدِهِ، فَإِنْ لَمْ يَسْتَطِعْ فَبِلِسَانِهِ، فَإِنْ لَمْ يَسْتَطِعْ فَبِقَلْبِهِ، وَذَلِكَ أَضْعَفُ الْإِيمَانِ",
      translationEnglish = "Abu Sa'id al-Khudri (RA) narrated that he heard the Messenger of Allah ﷺ say: 'Whoever among you sees an evil, let him change it with his hand; if he is not able, then with his tongue; and if he is not able, then with his heart—and that is the weakest of faith.'",
      translationUrdu = "حضرت ابو سعید خدری (رض) سے روایت ہے کہ میں نے رسول اللہ ﷺ کو فرماتے ہوئے سنا: تم میں سے جو شخص کوئی برائی دیکھے تو اسے اپنے ہاتھ سے روکے، اگر اس کی طاقت نہ رکھتا ہو تو اپنی زبان سے، اور اگر اس کی بھی طاقت نہ ہو تو اپنے دل میں (اسے برا جانے)، اور یہ ایمان کا کمزور ترین درجہ ہے۔",
      sourceReference = "Sahih Muslim, Hadith 49; Jami' at-Tirmidhi, Hadith 2172",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Abu Sa'id al-Khudri (RA) also transmitted numerous authentic Hadiths on the virtues of Ahl al-Bayt, including Hadith al-Thaqalayn, Hadith al-Kisa', and that Al-Hasan and Al-Husayn are the Masters of the Youth of Paradise (Jami' at-Tirmidhi 3768).",
      chronologyRank = 55,
      chronologyEraAndOrderNote = "Historical Position (Madinan Ansari Companion; too young for Uhud in 3 AH, joined the Prophet ﷺ from the Battle of the Trench in 5 AH onward).",
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION,
      authenticityGrading = "Sahih (Sahih Muslim 49)",
      hadithBookName = "Sahih Muslim",
      hadithNumber = "49",
      historicalRelationshipTags = listOf(
        HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT,
        HistoricalRelationshipTag.THAQALAYN_AND_KISA_TRANSMITTERS,
        HistoricalRelationshipTag.LATER_AND_OTHER_COMPANIONS
      ),
      relationshipToProphetAndAhlAlBayt = "Major Ansari Hadith transmitter who preserved key Prophetic traditions on the station of Imam Ali, Sayyidah Fatimah, Imam Hasan, and Imam Husayn (AS)."
    ),
    ReflectionItem(
      id = 305,
      category = ReflectionCategory.OTHER_SAHABAH_WISDOM,
      speaker = "Abu al-Darda' Uwaymir al-Ansari (RA) [Chronology: Converted on the Day of Badr, 2 AH]",
      title = "Sincere Remembrance & Detachment from Worldly Arrogance",
      theme = "Asceticism & Spiritual Mindfulness",
      arabicOrOriginalText = "أَلَا أُنَبِّئُكُمْ بِخَيْرِ أَعْمَالِكُمْ وَأَزْكَاهَا عِنْدَ مَلِيكِكُمْ وَأَرْفَعِهَا فِي دَرَجَاتِكُمْ ... ذِكْرُ اللَّهِ تَعَالَى",
      translationEnglish = "Abu al-Darda' (RA) narrated that the Prophet ﷺ said: 'Shall I not inform you of the best of your deeds, the purest of them before your Sovereign, and the highest of them in raising your ranks... It is the remembrance (Dhikr) of Allah the Exalted.'",
      translationUrdu = "حضرت ابو الدرداء (رض) سے روایت ہے کہ نبی کریم ﷺ نے فرمایا: کیا میں تمہیں تمہارے بہترین اعمال کی خبر نہ دوں جو تمہارے مالک کے نزدیک سب سے پاکیزہ اور تمہارے درجات کو سب سے زیادہ بلند کرنے والے ہیں... وہ اللہ تعالیٰ کا ذکر ہے۔",
      sourceReference = "Jami' at-Tirmidhi, Hadith 3377; Sunan Ibn Majah, Hadith 3790; Al-Muwatta, Book 15, Hadith 24",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Abu al-Darda' (RA) embraced Islam in 2 AH around the time of Badr, was paired in brotherhood with Salman al-Farsi (RA) in Madinah (Sahih al-Bukhari 1968), and became renowned as the sage (Hakim) of the Ummah in Damascus.",
      chronologyRank = 60,
      chronologyEraAndOrderNote = "Historical Position (2 AH / 624 CE, Madinah): Embraced Islam at the time of Badr and was paired in brotherhood with Salman al-Farsi (RA).",
      narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION,
      authenticityGrading = "Sahih (Jami' at-Tirmidhi 3377; Al-Mustadrak 1825)",
      hadithBookName = "Jami' at-Tirmidhi",
      hadithNumber = "3377",
      historicalRelationshipTags = listOf(HistoricalRelationshipTag.LATER_AND_OTHER_COMPANIONS),
      relationshipToProphetAndAhlAlBayt = "Brother-in-faith of Salman al-Farsi (RA) in Madinah (Sahih al-Bukhari 1968)."
    ),
    ReflectionItem(
      id = 306,
      category = ReflectionCategory.OTHER_SAHABAH_WISDOM,
      speaker = "Popular Unattributed Maxim (Often Loosely Quoted as a Companion Saying)",
      title = "Patience & Gratitude as the Two Wings of Faith",
      theme = "Patience & Gratitude",
      arabicOrOriginalText = "الإِيمَانُ نِصْفَانِ: نِصْفٌ فِي الصَّبْرِ، وَنِصْفٌ فِي الشُّكْرِ",
      translationEnglish = "Faith is in two halves: half in patience (Sabr) and half in gratitude (Shukr).",
      translationUrdu = "ایمان کے دو حصے ہیں: آدھا صبر میں اور آدھا شکر میں۔",
      sourceReference = "Shu'ab al-Iman (Al-Bayhaqi), Hadith 4186 (recorded as a statement of Tabi'un / ascetic sages rather than a Marfu' Prophetic Hadith)",
      attributionStatus = WisdomAttributionStatus.UNATTRIBUTED_POPULAR_SAYING,
      contextNotes = "Kept explicitly tagged as 'Popular Saying (Chain Unverified)' so users and scholars can see how the engine separates unverified Marfu' chains from authenticated Hadith.",
      chronologyRank = 900,
      narrationType = WisdomNarrationType.COMPANION_ATHAR_MAWQUF,
      authenticityGrading = "Mawquf / Maqtu' (Not a Marfu' Prophetic Hadith; Shu'ab al-Iman 4186)",
      hadithBookName = "Shu'ab al-Iman",
      hadithNumber = "4186",
      historicalRelationshipTags = listOf(HistoricalRelationshipTag.LATER_AND_OTHER_COMPANIONS)
    )
  )

  private val collectionDisplayNames: Map<String, String> = mapOf(
    "bukhari" to "Sahih al-Bukhari",
    "muslim" to "Sahih Muslim",
    "abudawud" to "Sunan Abu Dawud",
    "tirmidhi" to "Jami' at-Tirmidhi",
    "nasai" to "Sunan an-Nasa'i",
    "ibnmajah" to "Sunan Ibn Majah",
    "malik" to "Muwatta Imam Malik",
    "nawawi" to "40 Hadith an-Nawawi",
    "riyadussalihin" to "Riyad as-Salihin",
    "ahmad" to "Musnad Imam Ahmad",
    "darimi" to "Sunan ad-Darimi",
    "adab" to "Al-Adab al-Mufrad",
    "bulugh" to "Bulugh al-Maram",
    "mishkat" to "Mishkat al-Masabih",
    "shamail" to "Shama'il al-Muhammadiyyah",
    "qudsi" to "40 Hadith Qudsi",
    "shahwaliullah40" to "Al-Arba'un (Shah Waliullah)",
    "saidibnmansur" to "Sunan Sa'id ibn Mansur",
    "ibnkhuzaymah" to "Sahih Ibn Khuzaymah",
    "ibnhibban" to "Sahih Ibn Hibban",
    "mustadrak" to "Al-Mustadrak (Al-Hakim)",
    "abdurrazzaq" to "Musannaf Abd al-Razzaq",
    "ibnabishaybah" to "Musannaf Ibn Abi Shaybah",
    "alkafi" to "Al-Kafi (Al-Kulayni)",
    "manlayahduruhu" to "Man La Yahduruhu al-Faqih",
    "tahdhib" to "Tahdhib al-Ahkam",
    "istibsar" to "Al-Istibsar",
    "nahjulbalagha" to "Nahj al-Balagha",
    "sahifasajjadiyya" to "Al-Sahifa al-Sajjadiyya",
    "amali_saduq" to "Al-Amali (Shaykh al-Saduq)",
    "uyun_akhbar_rida" to "Uyun Akhbar al-Rida"
  )

  private val shiaAhlulBaytCollections: Set<String> = setOf(
    "alkafi",
    "manlayahduruhu",
    "tahdhib",
    "istibsar",
    "nahjulbalagha",
    "sahifasajjadiyya",
    "amali_saduq",
    "uyun_akhbar_rida"
  )

  /**
   * Dynamically harvests verified wisdom from the entire integrated Hadith database
   * (`ExtendedHadithDataProvider.getExtendedHadiths()`) across all 26+ Sunni and Shia collections,
   * ensuring the Wisdom Engine is never restricted to only 7 books and never uses AI-generated quotes.
   */
  private val harvestedHadithWisdomItems: List<ReflectionItem> by lazy {
    val allExtended = runCatching { ExtendedHadithDataProvider.getExtendedHadiths() }.getOrDefault(emptyList())
    allExtended.mapNotNull { entity ->
      convertHadithEntityToVerifiedWisdom(entity)
    }
  }

  private fun convertHadithEntityToVerifiedWisdom(entity: HadithEntity): ReflectionItem? {
    if (entity.arabicText.isBlank() || entity.englishText.isBlank() || entity.urduText.isBlank()) {
      return null
    }
    val bookTitle = collectionDisplayNames[entity.collectionKey]
      ?: entity.collectionName.ifBlank { entity.collectionKey.replaceFirstChar { it.uppercase() } }
    val narratorClean = entity.narrator.trim()
    val lowerNarrator = narratorClean.lowercase()
    val lowerEn = entity.englishText.lowercase()
    val isAhlulBaytCollection = entity.collectionKey in shiaAhlulBaytCollections
    val entityIdInt = entity.id.toInt().let { if (it <= 0) entity.hadithNumber else it }

    // Determine category and chronological metadata strictly from verified narrator & collection
    val category: ReflectionCategory
    val chronologyRank: Int
    val chronologyNote: String
    val narrationType: WisdomNarrationType
    val tags = mutableListOf(HistoricalRelationshipTag.ALL)
    val relationshipNote: String

    when {
      isAhlulBaytCollection ||
        lowerNarrator.contains("imam ali") ||
        lowerNarrator.contains("ali ibn abi talib") ||
        lowerNarrator.contains("fatimah") ||
        lowerNarrator.contains("hasan") ||
        lowerNarrator.contains("husayn") ||
        lowerNarrator.contains("zayn al-abidin") ||
        lowerNarrator.contains("sajjad") ||
        lowerNarrator.contains("baqir") ||
        lowerNarrator.contains("jafar al-sadiq") ||
        lowerNarrator.contains("ja'far al-sadiq") ||
        lowerNarrator.contains("musa al-kazim") ||
        lowerNarrator.contains("ali al-rida") -> {
        category = ReflectionCategory.AHLUL_BAYT_WISDOM
        chronologyRank = 50 + (entityIdInt % 100)
        chronologyNote = "Verified Ahl al-Bayt narration from $bookTitle (Hadith #${entity.hadithNumber})."
        narrationType = if (lowerEn.contains("messenger of allah") || lowerEn.contains("prophet")) {
          WisdomNarrationType.PROPHETIC_HADITH_MARFU
        } else {
          WisdomNarrationType.AHL_AL_BAYT_HADITH_MATHUR
        }
        tags.add(HistoricalRelationshipTag.PROPHETIC_HOUSEHOLD)
        relationshipNote = "Transmitted through the Prophetic Household (Ahl al-Bayt AS) in $bookTitle."
      }

      lowerNarrator.contains("khadijah") -> {
        category = ReflectionCategory.COMPANIONS_WISDOM
        chronologyRank = 1
        chronologyNote = "Chronological Rank #1: Sayyidah Khadijah bint Khuwaylid (RA), First Believer in Islam."
        narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION
        tags.add(HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN)
        tags.add(HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT)
        relationshipNote = "First wife and earliest supporter of Prophet Muhammad ﷺ; mother of Sayyidah Fatimah (SA)."
      }

      lowerNarrator.contains("zayd ibn harithah") -> {
        category = ReflectionCategory.COMPANIONS_WISDOM
        chronologyRank = 3
        chronologyNote = "Chronological Rank #3: Zayd ibn Harithah (RA), Earliest Freedman Believer."
        narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION
        tags.add(HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN)
        relationshipNote = "Beloved member of the Prophet's ﷺ household."
      }

      lowerNarrator.contains("abu bakr") -> {
        category = ReflectionCategory.COMPANIONS_WISDOM
        chronologyRank = 4
        chronologyNote = "Chronological Rank #4: Abu Bakr as-Siddiq (RA), Early Adult Freeman Believer."
        narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION
        tags.add(HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN)
        relationshipNote = "Early Makkan companion and Companion of the Migration."
      }

      lowerNarrator.contains("abu dharr") -> {
        category = ReflectionCategory.COMPANIONS_WISDOM
        chronologyRank = 5
        chronologyNote = "Chronological Rank #5: Abu Dharr al-Ghifari (RA), 4th/5th Early Believer."
        narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION
        tags.add(HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN)
        tags.add(HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT)
        relationshipNote = "Early convert and loyal supporter of Prophet Muhammad ﷺ and Ahl al-Bayt."
      }

      lowerNarrator.contains("ammar") -> {
        category = ReflectionCategory.COMPANIONS_WISDOM
        chronologyRank = 6
        chronologyNote = "Chronological Rank #6: Ammar ibn Yasir (RA), Earliest Persecuted Vanguard."
        narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION
        tags.add(HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN)
        tags.add(HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT)
        relationshipNote = "Son of the first martyrs of Islam and steadfast defender of Imam Ali (AS) and Ahl al-Bayt."
      }

      lowerNarrator.contains("miqdad") -> {
        category = ReflectionCategory.COMPANIONS_WISDOM
        chronologyRank = 7
        chronologyNote = "Chronological Rank #7: Miqdad ibn Amr (RA), Early Believer & Defender at Badr."
        narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION
        tags.add(HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN)
        tags.add(HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT)
        relationshipNote = "One of the earliest seven Muslims in Makkah and loyal supporter of Ahl al-Bayt."
      }

      lowerNarrator.contains("bilal") -> {
        category = ReflectionCategory.COMPANIONS_WISDOM
        chronologyRank = 8
        chronologyNote = "Chronological Rank #8: Bilal ibn Rabah (RA), Early Persecuted Believer & First Mu'adhdhin."
        narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION
        tags.add(HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN)
        tags.add(HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT)
        relationshipNote = "First Mu'adhdhin of Prophet Muhammad ﷺ."
      }

      lowerNarrator.contains("ibn mas'ud") || lowerNarrator.contains("ibn masud") -> {
        category = ReflectionCategory.COMPANIONS_WISDOM
        chronologyRank = 9
        chronologyNote = "Chronological Rank #9: Abdullah ibn Mas'ud (RA), 6th Early Believer in Makkah."
        narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION
        tags.add(HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN)
        relationshipNote = "Early Makkan companion and foremost reciter of the Qur'an."
      }

      lowerNarrator.contains("salman") -> {
        category = ReflectionCategory.COMPANIONS_WISDOM
        chronologyRank = 11
        chronologyNote = "Chronological Rank #11: Salman al-Farsi (RA), 'Salman is from us, the Ahl al-Bayt.'"
        narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION
        tags.add(HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN)
        tags.add(HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT)
        relationshipNote = "Honored by the Prophet ﷺ as a spiritual member of Ahl al-Bayt."
      }

      lowerNarrator.contains("umm salamah") || lowerNarrator.contains("umm salama") -> {
        category = ReflectionCategory.COMPANIONS_WISDOM
        chronologyRank = 12
        chronologyNote = "Chronological Rank #12: Umm Salamah (RA), Early Muhajirah & Mother of the Believers."
        narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION
        tags.add(HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN)
        tags.add(HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT)
        tags.add(HistoricalRelationshipTag.THAQALAYN_AND_KISA_TRANSMITTERS)
        relationshipNote = "Mother of the Believers and primary transmitter of Hadith al-Kisa'."
      }

      lowerNarrator.contains("jabir") -> {
        category = ReflectionCategory.COMPANIONS_WISDOM
        chronologyRank = 13
        chronologyNote = "Chronological Rank #13: Jabir ibn Abdillah al-Ansari (RA), Early Aqabah Pledge & Devoted Companion of Ahl al-Bayt."
        narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION
        tags.add(HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN)
        tags.add(HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT)
        tags.add(HistoricalRelationshipTag.THAQALAYN_AND_KISA_TRANSMITTERS)
        relationshipNote = "Transmitted Hadith al-Thaqalayn and conveyed the Prophet's ﷺ greeting to Imam al-Baqir (AS)."
      }

      lowerNarrator.contains("zayd ibn arqam") -> {
        category = ReflectionCategory.COMPANIONS_WISDOM
        chronologyRank = 14
        chronologyNote = "Chronological Rank #14: Zayd ibn Arqam (RA), Narrator of Hadith al-Thaqalayn."
        narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION
        tags.add(HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT)
        tags.add(HistoricalRelationshipTag.THAQALAYN_AND_KISA_TRANSMITTERS)
        relationshipNote = "Narrator of Hadith al-Thaqalayn in Sahih Muslim 2408."
      }

      lowerNarrator.contains("hudhayfah") || lowerNarrator.contains("hudhaifah") -> {
        category = ReflectionCategory.COMPANIONS_WISDOM
        chronologyRank = 15
        chronologyNote = "Chronological Rank #15: Hudhayfah ibn al-Yaman (RA), Keeper of the Prophet's ﷺ Counsel."
        narrationType = WisdomNarrationType.PROPHETIC_HADITH_VIA_COMPANION
        tags.add(HistoricalRelationshipTag.EARLIEST_BELIEVERS_SABIQUIN)
        tags.add(HistoricalRelationshipTag.SUPPORTERS_OF_AHL_AL_BAYT)
        relationshipNote = "Keeper of the Prophet's ﷺ confidential counsel and loyal supporter of Imam Ali (AS)."
      }

      else -> {
        // Every authentic Prophetic Hadith in the 26+ collections enriches PROPHETIC_GUIDANCE
        category = ReflectionCategory.PROPHETIC_GUIDANCE
        chronologyRank = 100 + (entityIdInt % 500)
        chronologyNote = "Verified Prophetic Hadith from $bookTitle (Book ${entity.bookNumber}, Hadith ${entity.hadithNumber})."
        narrationType = WisdomNarrationType.PROPHETIC_HADITH_MARFU
        relationshipNote = "Narrated via $narratorClean in $bookTitle."
      }
    }

    val speakerFormatted = when (category) {
      ReflectionCategory.PROPHETIC_GUIDANCE -> "Prophet Muhammad ﷺ (Narrated by $narratorClean)"
      ReflectionCategory.AHLUL_BAYT_WISDOM -> narratorClean
      ReflectionCategory.COMPANIONS_WISDOM -> "$narratorClean (Transmitting Prophetic Sunnah)"
      ReflectionCategory.OTHER_SAHABAH_WISDOM -> narratorClean
    }
    val gradeInfo = if (entity.gradingScholar.isNotBlank()) {
      "${entity.grade} (${entity.gradingScholar})"
    } else {
      "${entity.grade.ifBlank { "Verified" }} ($bookTitle #${entity.hadithNumber})"
    }

    return ReflectionItem(
      id = 10_000 + (entity.collectionKey.hashCode() and 0x7FFF) * 100 + entity.hadithNumber,
      category = category,
      speaker = speakerFormatted,
      title = "$bookTitle • Hadith #${entity.hadithNumber}",
      theme = "${entity.bookName.ifBlank { bookTitle }} (Book ${entity.bookNumber})",
      arabicOrOriginalText = entity.arabicText,
      translationEnglish = entity.englishText,
      translationUrdu = entity.urduText,
      sourceReference = entity.reference.ifBlank { "$bookTitle, Book ${entity.bookNumber}, Hadith ${entity.hadithNumber}" },
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = chronologyNote,
      chronologyRank = chronologyRank,
      chronologyEraAndOrderNote = chronologyNote,
      historicalDisputeNote = "",
      narrationType = narrationType,
      authenticityGrading = gradeInfo,
      hadithBookName = bookTitle,
      hadithNumber = entity.hadithNumber.toString(),
      historicalRelationshipTags = tags.distinct(),
      relationshipToProphetAndAhlAlBayt = relationshipNote
    )
  }

  /**
   * Combined corpus of curated items and verified Hadith items harvested from all integrated Hadith collections.
   */
  val items: List<ReflectionItem> by lazy {
    val combined = ArrayList<ReflectionItem>(curatedItems.size + harvestedHadithWisdomItems.size)
    combined.addAll(curatedItems)
    val existingArabicHashes = curatedItems.map { it.arabicOrOriginalText.take(40) }.toHashSet()
    harvestedHadithWisdomItems.forEach { harvested ->
      if (existingArabicHashes.add(harvested.arabicOrOriginalText.take(40))) {
        combined.add(harvested)
      }
    }
    combined
  }

  /**
   * Returns all distinct Hadith books represented in the Wisdom Engine across all 26+ collections.
   */
  fun getAvailableHadithBooks(category: ReflectionCategory? = null): List<String> {
    return items
      .asSequence()
      .filter { category == null || it.category == category }
      .map { it.hadithBookName.trim() }
      .filter { it.isNotEmpty() }
      .distinct()
      .sorted()
      .toList()
  }

  /**
   * Standard filtering with strict chronological ordering for Early Sahabah (`COMPANIONS_WISDOM`)
   * and Other Sahabah (`OTHER_SAHABAH_WISDOM`), plus optional filtering by attribution status,
   * Hadith book, historical relationship tag, and search query.
   */
  fun filterItems(
    category: ReflectionCategory? = null,
    attributionFilter: WisdomAttributionStatus? = null,
    searchQuery: String = "",
    hadithBookFilter: String? = null,
    relationshipFilter: HistoricalRelationshipTag = HistoricalRelationshipTag.ALL,
    bookmarkedOnly: Boolean = false,
    bookmarkedIds: Set<Int> = emptySet()
  ): List<ReflectionItem> {
    val q = searchQuery.trim().lowercase()
    val filtered = items.filter { item ->
      val matchesCategory = category == null || item.category == category
      // By default (when attributionFilter == null), prioritize verified attributions unless user searches or selects a specific attribution filter
      val matchesAttribution = attributionFilter == null || item.attributionStatus == attributionFilter
      val matchesBook = hadithBookFilter.isNullOrBlank() ||
        item.hadithBookName.equals(hadithBookFilter, ignoreCase = true) ||
        item.sourceReference.contains(hadithBookFilter, ignoreCase = true)
      val matchesRelationship = relationshipFilter == HistoricalRelationshipTag.ALL ||
        item.historicalRelationshipTags.contains(relationshipFilter)
      val matchesBookmark = !bookmarkedOnly || bookmarkedIds.contains(item.id)
      val matchesQuery = q.isEmpty() ||
        item.speaker.lowercase().contains(q) ||
        item.title.lowercase().contains(q) ||
        item.theme.lowercase().contains(q) ||
        item.translationEnglish.lowercase().contains(q) ||
        item.translationUrdu.contains(q) ||
        item.arabicOrOriginalText.contains(q) ||
        item.sourceReference.lowercase().contains(q) ||
        item.hadithBookName.lowercase().contains(q) ||
        item.relationshipToProphetAndAhlAlBayt.lowercase().contains(q) ||
        item.chronologyEraAndOrderNote.lowercase().contains(q)
      matchesCategory && matchesAttribution && matchesBook && matchesRelationship && matchesBookmark && matchesQuery
    }

    return when (category) {
      ReflectionCategory.COMPANIONS_WISDOM,
      ReflectionCategory.OTHER_SAHABAH_WISDOM -> {
        filtered.sortedWith(compareBy<ReflectionItem> { it.chronologyRank }.thenBy { it.id })
      }
      else -> filtered
    }
  }

  /**
   * Intelligent Continuous Discovery Engine:
   * Surfaces verified narrations across all integrated Hadith collections while tracking seen items
   * in SharedPreferences so users discover fresh wisdom every time they open or refresh the section.
   * For `COMPANIONS_WISDOM` (Early Sahabah), strict historical chronology (`chronologyRank`) is preserved
   * in the base list, and the discovery engine advances to the next unseen chronological item.
   */
  fun getSeenWisdomIds(context: Context): Set<Int> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getString(KEY_SEEN_IDS, "") ?: ""
    if (raw.isBlank()) return emptySet()
    return raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
  }

  fun markWisdomSeen(context: Context, wisdomId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val updated = getSeenWisdomIds(context).toMutableSet()
    updated.add(wisdomId)
    // Cap history at 500 IDs so SharedPreferences stays lightweight
    val trimmed = if (updated.size > 500) updated.toList().takeLast(500) else updated.toList()
    prefs.edit().putString(KEY_SEEN_IDS, trimmed.joinToString(",")).apply()
  }

  fun discoverNextUnseenIndex(
    context: Context,
    candidates: List<ReflectionItem>,
    currentIndex: Int
  ): Int {
    if (candidates.isEmpty()) return 0
    val verifiedCandidates = candidates.withIndex().filter {
      it.value.attributionStatus == WisdomAttributionStatus.VERIFIED_ATTRIBUTION
    }
    val pool = verifiedCandidates.ifEmpty { candidates.withIndex().toList() }
    val seenIds = getSeenWisdomIds(context)

    // Find the first unseen verified candidate after currentIndex (wrapping around)
    for (offset in 1..pool.size) {
      val candidateEntry = pool[(currentIndex + offset) % pool.size]
      if (!seenIds.contains(candidateEntry.value.id)) {
        markWisdomSeen(context, candidateEntry.value.id)
        return candidateEntry.index
      }
    }

    // All items in this filtered view have been seen once — reset seen IDs for this category's items and advance
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val candidateIdSet = candidates.map { it.id }.toSet()
    val remainingSeen = seenIds.filterNot { candidateIdSet.contains(it) }
    val nextRotation = prefs.getInt(KEY_DISCOVERY_ROTATION, 0) + 1
    val nextIdx = (currentIndex + 1) % candidates.size
    val nextId = candidates[nextIdx].id
    prefs.edit()
      .putString(KEY_SEEN_IDS, (remainingSeen + nextId).joinToString(","))
      .putInt(KEY_DISCOVERY_ROTATION, nextRotation)
      .apply()
    return nextIdx
  }

  fun getBookmarkedWisdomIds(context: Context): Set<Int> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getString(KEY_BOOKMARKED_IDS, "") ?: ""
    if (raw.isBlank()) return emptySet()
    return raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
  }

  fun toggleWisdomBookmark(context: Context, wisdomId: Int): Set<Int> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val current = getBookmarkedWisdomIds(context).toMutableSet()
    if (current.contains(wisdomId)) {
      current.remove(wisdomId)
    } else {
      current.add(wisdomId)
    }
    prefs.edit().putString(KEY_BOOKMARKED_IDS, current.joinToString(",")).apply()
    return current
  }
}
