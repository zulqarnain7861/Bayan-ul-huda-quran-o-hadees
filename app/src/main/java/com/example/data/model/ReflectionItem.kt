package com.example.data.model

enum class ReflectionCategory(val displayTitle: String, val subtitle: String) {
  PROPHETIC_GUIDANCE(
    displayTitle = "Prophet Muhammad ﷺ",
    subtitle = "Verified Prophetic Hadith & Guidance"
  ),
  AHLUL_BAYT_WISDOM(
    displayTitle = "Ahlul Bayt Wisdom",
    subtitle = "Imam Ali, Sayyidah Fatimah, Imam Hasan, Imam Husayn & Ahlul Bayt (AS)"
  ),
  COMPANIONS_WISDOM(
    displayTitle = "Sahabah Wisdom",
    subtitle = "Early Companions (Sahabah RA) with Verified Classical Chains"
  )
}

enum class WisdomAttributionStatus(val label: String, val badgeDescription: String) {
  VERIFIED_ATTRIBUTION(
    label = "Verified Attribution",
    badgeDescription = "Authenticated in primary classical collections with reliable chain"
  ),
  DISPUTED_ATTRIBUTION(
    label = "Disputed Attribution",
    badgeDescription = "Chain or speaker attribution differs across classical scholars"
  ),
  UNATTRIBUTED_POPULAR_SAYING(
    label = "Unattributed Popular Saying",
    badgeDescription = "Widely circulated maxim without a verified primary chain; shown for scholarly caution"
  )
}

data class ReflectionItem(
  val id: String,
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
  internal val internalSourceMetadataUrl: String = ""
)

object ReflectionDataProvider {
  val items: List<ReflectionItem> = listOf(
    // ==================== 1. PROPHET MUHAMMAD (SAW) ====================
    ReflectionItem(
      id = "ref_1",
      category = ReflectionCategory.PROPHETIC_GUIDANCE,
      speaker = "Prophet Muhammad (ﷺ)",
      theme = "Mercy & Compassion",
      title = "Mercy to All of Creation",
      arabicOrOriginalText = "الرَّاحِمُونَ يَرْحَمُهُمُ الرَّحْمَنُ، ارْحَمُوا مَنْ فِي الأَرْضِ يَرْحَمْكُمْ مَنْ فِي السَّمَاءِ",
      translationEnglish = "Those who show mercy will be shown mercy by the Most Merciful. Show mercy to those on earth, and the One in the heavens will show mercy to you.",
      translationUrdu = "رحم کرنے والوں پر رحمٰن رحم فرماتا ہے۔ تم زمین والوں پر رحم کرو، آسمان والا تم پر رحم کرے گا۔",
      sourceReference = "Jami' at-Tirmidhi 1924 • Sunan Abu Dawud 4941 (Sahih)",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "A foundational principle of Prophetic ethics governing mercy toward all creation.",
      internalSourceMetadataUrl = "https://sunnah.com/tirmidhi:1924"
    ),
    ReflectionItem(
      id = "ref_2",
      category = ReflectionCategory.PROPHETIC_GUIDANCE,
      speaker = "Prophet Muhammad (ﷺ)",
      theme = "Contentment & Gratitude",
      title = "True Wealth of the Soul",
      arabicOrOriginalText = "لَيْسَ الْغِنَى عَنْ كَثْرَةِ الْعَرَضِ، وَلَكِنَّ الْغِنَى غِنَى النَّفْسِ",
      translationEnglish = "Richness does not lie in an abundance of worldly goods; true richness is the richness of the soul.",
      translationUrdu = "مالداری ساز و سامان کی کثرت سے نہیں، بلکہ حقیقی مالداری دل کی بے نیازی ہے۔",
      sourceReference = "Sahih al-Bukhari 6446 • Sahih Muslim 1051",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Direct Prophetic guidance on prioritizing inner tranquility and spiritual contentment.",
      internalSourceMetadataUrl = "https://sunnah.com/bukhari:6446"
    ),
    ReflectionItem(
      id = "ref_3",
      category = ReflectionCategory.PROPHETIC_GUIDANCE,
      speaker = "Prophet Muhammad (ﷺ)",
      theme = "Kindness & Gentleness",
      title = "The Beauty of Gentleness (Al-Rifq)",
      arabicOrOriginalText = "إِنَّ الرِّفْقَ لاَ يَكُونُ فِي شَيْءٍ إِلاَّ زَانَهُ، وَلاَ يُنْزَعُ مِنْ شَيْءٍ إِلاَّ شَانَهُ",
      translationEnglish = "Indeed, gentleness is not in anything except that it beautifies it, and it is not removed from anything except that it disgraces it.",
      translationUrdu = "نرمی جس چیز میں بھی ہو اسے خوبصورت بنا دیتی ہے، اور جس چیز سے نکال دی جائے اسے عیب دار کر دیتی ہے۔",
      sourceReference = "Sahih Muslim 2594",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Narrated by Umm al-Mu'minin Aisha (RA) regarding speech, character, and conduct.",
      internalSourceMetadataUrl = "https://sunnah.com/muslim:2594"
    ),
    ReflectionItem(
      id = "ref_4",
      category = ReflectionCategory.PROPHETIC_GUIDANCE,
      speaker = "Prophet Muhammad (ﷺ)",
      theme = "Purification of the Heart",
      title = "The Soundness of the Heart (Al-Qalb)",
      arabicOrOriginalText = "أَلَا وَإِنَّ فِي الْجَسَدِ مُضْغَةً إِذَا صَلَحَتْ صَلَحَ الْجَسَدُ كُلُّهُ، وَإِذَا فَسَدَتْ فَسَدَ الْجَسَدُ كُلُّهُ، أَلَا وَهِيَ الْقَلْبُ",
      translationEnglish = "Truly in the body there is a morsel of flesh which, if it be whole, all the body is whole, and which, if it be diseased, all of it is diseased. Truly, it is the heart.",
      translationUrdu = "خبردار! جسم میں گوشت کا ایک ٹکڑا ہے، جب وہ سنور جائے تو پورا جسم سنور جاتا ہے اور جب وہ بگڑ جائے تو پورا جسم بگڑ جاتا ہے، سن لو وہ دل ہے۔",
      sourceReference = "Sahih al-Bukhari 52 • Sahih Muslim 1599",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Part of the comprehensive Hadith of An-Nu'man ibn Bashir (RA) on lawful and doubtful matters.",
      internalSourceMetadataUrl = "https://sunnah.com/bukhari:52"
    ),

    // ==================== 2. AHLUL BAYT WISDOM ====================
    ReflectionItem(
      id = "ref_ab_1",
      category = ReflectionCategory.AHLUL_BAYT_WISDOM,
      speaker = "Imam Ali ibn Abi Talib (AS / RA)",
      theme = "Justice & Human Dignity",
      title = "Brothers in Faith or Equals in Creation",
      arabicOrOriginalText = "أَشْعِرْ قَلْبَكَ الرَّحْمَةَ لِلرَّعِيَّةِ، وَالْمَحَبَّةَ لَهُمْ، وَاللُّطْفَ بِهِمْ... فَإِنَّهُمْ صِنْفَانِ: إِمَّا أَخٌ لَكَ فِي الدِّينِ، وَإِمَّا نَظِيرٌ لَكَ فِي الْخَلْقِ",
      translationEnglish = "Infuse your heart with mercy for the people, love for them, and kindness toward them... For they are of two kinds: either your brother in religion or your equal in creation.",
      translationUrdu = "اپنے دل میں لوگوں کے لیے رحمت، محبت اور نرمی پیدا کرو... کیونکہ لوگ دو ہی قسم کے ہیں: یا تو وہ دین میں تمہارے بھائی ہیں یا خلقت (انسانیت) میں تمہاری مثل ہیں۔",
      sourceReference = "Nahj al-Balagha, Letter 53 (Covenant to Malik al-Ashtar — Subhi as-Salih Ed.)",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Classical epistle of Imam Ali (AS / RA) on ethical governance, mercy, and universal justice.",
      internalSourceMetadataUrl = "https://thaqalayn.net"
    ),
    ReflectionItem(
      id = "ref_ab_2",
      category = ReflectionCategory.AHLUL_BAYT_WISDOM,
      speaker = "Sayyidah Fatimah al-Zahra, salamun alayha",
      theme = "Sincerity & Devotion",
      title = "The Wisdom of Divine Commandments",
      arabicOrOriginalText = "فَجَعَلَ اللَّهُ الْإِيمَانَ تَطْهِيرًا لَكُمْ مِنَ الشِّرْكِ، وَالصَّلَاةَ تَنْزِيهًا لَكُمْ عَنِ الْكِبْرِ، وَالزَّكَاةَ تَزْكِيَةً لِلنَّفْسِ وَنَمَاءً فِي الرِّزْقِ",
      translationEnglish = "Allah made faith (Iman) a purification for you from polytheism, prayer (Salah) a cleansing for you from arrogance, and almsgiving (Zakat) a purification for the soul and a cause of growth in provision.",
      translationUrdu = "اللہ تعالیٰ نے ایمان کو تمہارے لیے شرک سے پاکیزگی، نماز کو تکبر سے طہارت، اور زکوٰۃ کو نفس کے تزکیے اور رزق میں برکت کا ذریعہ بنایا۔",
      sourceReference = "Ihtijaj al-Tabarsi Vol. 1 • Bihar al-Anwar Vol. 29 (Khutbah of Sayyidah Fatimah al-Zahra s.a.)",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "From the renowned discourse of Sayyidah Fatimah al-Zahra (salamun alayha) in Al-Masjid an-Nabawi.",
      internalSourceMetadataUrl = "https://thaqalayn.net"
    ),
    ReflectionItem(
      id = "ref_ab_3",
      category = ReflectionCategory.AHLUL_BAYT_WISDOM,
      speaker = "Imam al-Hasan ibn Ali (AS / RA)",
      theme = "Noble Character & Forbearance",
      title = "The Essence of Noble Character (Makarim al-Akhlaq)",
      arabicOrOriginalText = "إِنَّ أَحْسَنَ الْحَسَنِ الْخُلُقُ الْحَسَنُ، وَالصَّبْرُ عِنْدَ النَّازِلَةِ، وَالْعَفْوُ عِنْدَ الْمَقْدِرَةِ",
      translationEnglish = "Indeed, the most beautiful of all virtues is noble character, patience when calamity descends, and pardoning when one has the power to retaliate.",
      translationUrdu = "بے شک سب سے بہترین خوبی حسنِ اخلاق، مصیبت کے وقت صبر، اور قدرت ہونے کے باوجود معاف کر دینا ہے۔",
      sourceReference = "Tuhaf al-'Uqul (Ibn Shu'bah al-Harrani) • Al-Khisal (Shaykh al-Saduq)",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Verified ethical counsel of Imam al-Hasan al-Mujtaba (AS / RA) on forbearance and generosity.",
      internalSourceMetadataUrl = "https://thaqalayn.net"
    ),
    ReflectionItem(
      id = "ref_ab_4",
      category = ReflectionCategory.AHLUL_BAYT_WISDOM,
      speaker = "Imam al-Husayn ibn Ali (AS / RA)",
      theme = "Generosity & Moral Integrity",
      title = "People's Needs Are Among Allah's Blessings Upon You",
      arabicOrOriginalText = "إِنَّ حَوَائِجَ النَّاسِ إِلَيْكُمْ مِنْ نِعَمِ اللَّهِ عَلَيْكُمْ فَلَا تَمَلُّوا النِّعَمَ",
      translationEnglish = "Truly, the needs that people bring to you are among the blessings of Allah upon you; therefore, do not grow weary of those blessings.",
      translationUrdu = "بے شک لوگوں کی حاجتیں جو تمہاری طرف آتی ہیں وہ تم پر اللہ کی نعمتوں میں سے ہیں، لہٰذا ان نعمتوں سے اکتا نہ جانا۔",
      sourceReference = "Tuhaf al-'Uqul p. 245 • Bihar al-Anwar Vol. 75",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Counsel of Sayyid al-Shuhada Imam al-Husayn (AS / RA) on serving humanity with gratitude.",
      internalSourceMetadataUrl = "https://thaqalayn.net"
    ),
    ReflectionItem(
      id = "ref_ab_5",
      category = ReflectionCategory.AHLUL_BAYT_WISDOM,
      speaker = "Imam Ali Zayn al-Abidin & Imam Ja'far al-Sadiq (AS)",
      theme = "Sincerity of Intention & Moral Excellence",
      title = "Carrying Intentions and Deeds to Their Highest Perfection",
      arabicOrOriginalText = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَآلِهِ، وَبَلِّغْ بِإِيمَانِي أَكْمَلَ الْإِيمَانِ، وَاجْعَلْ يَقِينِي أَفْضَلَ الْيَقِينِ، وَانْتَهِ بِنِيَّتِي إِلَى أَحْسَنِ النِّيَّاتِ",
      translationEnglish = "O Allah, bless Muhammad and his Household, cause my faith to reach the most perfect faith, make my certainty the most excellent certainty, and carry my intention to the best of intentions.",
      translationUrdu = "اے اللہ! محمد اور آلِ محمد پر رحمت نازل فرما، اور میرے ایمان کو کامل ترین ایمان تک پہنچا دے اور میری نیت کو بہترین نیتوں تک پہنچا دے۔",
      sourceReference = "Al-Sahifa al-Sajjadiyya, Supplication 20 (Du'a Makarim al-Akhlaq)",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Mutawatir devotional supplication of Imam Ali ibn al-Husayn Zayn al-Abidin (AS).",
      internalSourceMetadataUrl = "https://thaqalayn.net"
    ),
    ReflectionItem(
      id = "ref_ab_disputed_1",
      category = ReflectionCategory.AHLUL_BAYT_WISDOM,
      speaker = "Attributed in Popular Literature (Disputed / Unverified as Marfu' Hadith)",
      theme = "Self-Knowledge & Humility",
      title = "He Who Knows Himself Knows His Lord (Scholarly Attribution Notice)",
      arabicOrOriginalText = "مَنْ عَرَفَ نَفْسَهُ فَقَدْ عَرَفَ رَبَّهُ",
      translationEnglish = "Whoever knows his own self (in its dependence and servitude) comes to know his Lord (in His absolute Lordship).",
      translationUrdu = "جس نے اپنے نفس (اپنی عاجزی اور بندگی) کو پہچان لیا اس نے اپنے رب کو پہچان لیا۔",
      sourceReference = "Ghurar al-Hikam (Attributed as a maxim to Imam Ali / Yahya ibn Mu'adh al-Razi; graded non-Prophetic by al-Nawawi & Ibn Taymiyyah)",
      attributionStatus = WisdomAttributionStatus.DISPUTED_ATTRIBUTION,
      contextNotes = "Presented with explicit attribution transparency: not an authenticated Prophetic Hadith, though recorded in later wisdom compilations.",
      internalSourceMetadataUrl = "https://sunnah.com"
    ),

    // ==================== 3. SAHABAH WISDOM (EARLY SAHABAH) ====================
    ReflectionItem(
      id = "ref_sahaba_1",
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Abu Bakr as-Siddiq (RA)",
      theme = "Truthfulness & Sincerity",
      title = "Truthfulness Is a Sacred Trust",
      arabicOrOriginalText = "الصِّدْقُ أَمَانَةٌ، وَالْكَذِبُ خِيَانَةٌ، وَالضَّعِيفُ فِيكُمْ قَوِيٌّ عِنْدِي حَتَّى أُرِيحَ عَلَيْهِ حَقَّهُ إِنْ شَاءَ اللَّهُ",
      translationEnglish = "Truthfulness is a sacred trust, and falsehood is betrayal. The weak among you is strong in my sight until I restore to him his rightful due, if Allah wills.",
      translationUrdu = "سچائی امانت ہے اور جھوٹ خیانت ہے۔ تم میں سے کمزور شخص میرے نزدیک طاقتور ہے یہاں تک کہ ان شاء اللہ میں اس کا حق اسے دلا دوں۔",
      sourceReference = "Sirah Ibn Hisham • Al-Bidayah wa'l-Nihayah (Inaugural Address of Abu Bakr as-Siddiq RA)",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Early Sahabi counsel of Abu Bakr as-Siddiq (RA) establishing justice and accountability.",
      internalSourceMetadataUrl = "https://sunnah.com"
    ),
    ReflectionItem(
      id = "ref_5",
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Umar ibn al-Khattab (RA)",
      theme = "Self-Accountability (Muhasabah)",
      title = "Take Account of Yourselves Before You Are Taken to Account",
      arabicOrOriginalText = "حَاسِبُوا أَنْفُسَكُمْ قَبْلَ أَنْ تُحَاسَبُوا، وَزِنُوا أَعْمَالَكُمْ قَبْلَ أَنْ تُوزَنَ عَلَيْكُمْ",
      translationEnglish = "Take account of yourselves before you are taken to account, and weigh your deeds before they are weighed for you.",
      translationUrdu = "اپنا محاسبہ کرو اس سے پہلے کہ تمہارا حساب لیا جائے، اور اپنے اعمال کو تولو اس سے پہلے کہ وہ تمہارے لیے تولے جائیں۔",
      sourceReference = "Jami' at-Tirmidhi 2459 • Musannaf Ibn Abi Shaybah 34459",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Classical counsel of Amir al-Mu'minin Umar ibn al-Khattab (RA) on daily spiritual reflection.",
      internalSourceMetadataUrl = "https://sunnah.com/tirmidhi:2459"
    ),
    ReflectionItem(
      id = "ref_sahaba_3",
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Uthman ibn Affan (RA)",
      theme = "Purity of Heart & Love of the Qur'an",
      title = "If Our Hearts Were Pure, They Would Never Tire of the Word of Allah",
      arabicOrOriginalText = "لَوْ أَنَّ قُلُوبَنَا طَهُرَتْ مَا شَبِعَتْ مِنْ كَلَامِ رَبِّنَا، وَإِنِّي لَأَكْرَهُ أَنْ يَأْتِيَ عَلَيَّ يَوْمٌ لَا أَنْظُرُ فِيهِ فِي الْمُصْحَفِ",
      translationEnglish = "If our hearts were truly pure, they would never have their fill of the Word of our Lord; and indeed I dislike that a single day should pass over me without looking into the Mushaf (Qur'an).",
      translationUrdu = "اگر ہمارے دل پاک ہو جائیں تو وہ ہمارے رب کے کلام (قرآن) سے کبھی سیر نہ ہوں، اور میں ناپسند کرتا ہوں کہ مجھ پر کوئی ایسا دن گزرے جس میں میں مصحفِ قرآن میں نظر نہ کروں۔",
      sourceReference = "Kitab al-Zuhd li-Ahmad ibn Hanbal • Shu'ab al-Iman lil-Bayhaqi",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Verified statement of Uthman ibn Affan Dhu al-Nurayn (RA) on daily attachment to the Qur'an.",
      internalSourceMetadataUrl = "https://sunnah.com"
    ),
    ReflectionItem(
      id = "ref_sahaba_4",
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Abdullah ibn Mas'ud (RA)",
      theme = "Reverential Awe & True Knowledge",
      title = "True Knowledge Is Reverential Awe of Allah",
      arabicOrOriginalText = "لَيْسَ الْعِلْمُ بِكَثْرَةِ الرِّوَايَةِ، وَلَكِنَّ الْعِلْمَ الْخَشْيَةُ",
      translationEnglish = "Sacred knowledge does not consist merely in an abundance of narration; rather, true knowledge is reverential awe (Khashyah) of Allah.",
      translationUrdu = "علم محض کثرتِ روایت کا نام نہیں، بلکہ حقیقی علم اللہ تعالیٰ کی خشیت کا نام ہے۔",
      sourceReference = "Musannaf Ibn Abi Shaybah 26085 • Jami' Bayan al-'Ilm wa Fadlihi (Ibn Abd al-Barr)",
      attributionStatus = WisdomAttributionStatus.VERIFIED_ATTRIBUTION,
      contextNotes = "Early Sahabi wisdom from Abdullah ibn Mas'ud (RA) rooted in Surah Fatir 35:28.",
      internalSourceMetadataUrl = "https://sunnah.com"
    ),
    ReflectionItem(
      id = "ref_popular_unattributed_1",
      category = ReflectionCategory.COMPANIONS_WISDOM,
      speaker = "Unattributed Popular Saying (Scholarly Caution)",
      theme = "Verification Before Attribution",
      title = "Work for Your World as If You Live Forever (Unverified as Prophetic Hadith)",
      arabicOrOriginalText = "اعْمَلْ لِدُنْيَاكَ كَأَنَّكَ تَعِيشُ أَبَدًا، وَاعْمَلْ لِآخِرَتِكَ كَأَنَّكَ تَمُوتُ غَدًا",
      translationEnglish = "Work for your worldly life as though you will live forever, and work for your Hereafter as though you will die tomorrow.",
      translationUrdu = "اپنی دنیا کے لیے ایسے عمل کرو گویا ہمیشہ زندہ رہو گے، اور اپنی آخرت کے لیے ایسے عمل کرو گویا کل مر جاؤ گے۔",
      sourceReference = "Recorded by al-Bayhaqi with a weak/interrupted chain to Abdullah ibn Amr (RA); Shaykh al-Albani (Da'if al-Jami' 988) notes it is not an authentic Marfu' Prophetic Hadith",
      attributionStatus = WisdomAttributionStatus.UNATTRIBUTED_POPULAR_SAYING,
      contextNotes = "Included with explicit scholarly caution so commonly circulated maxims are never mistaken for authenticated Prophetic Hadith.",
      internalSourceMetadataUrl = "https://sunnah.com"
    )
  )

  fun filterItems(
    category: ReflectionCategory? = null,
    attributionFilter: WisdomAttributionStatus? = null,
    searchQuery: String = ""
  ): List<ReflectionItem> {
    val q = searchQuery.trim()
    return items.filter { item ->
      val catMatch = category == null || item.category == category
      val attrMatch = attributionFilter == null || item.attributionStatus == attributionFilter
      val queryMatch = q.isEmpty() ||
        item.speaker.contains(q, ignoreCase = true) ||
        item.title.contains(q, ignoreCase = true) ||
        item.theme.contains(q, ignoreCase = true) ||
        item.arabicOrOriginalText.contains(q) ||
        item.translationEnglish.contains(q, ignoreCase = true) ||
        item.translationUrdu.contains(q) ||
        item.sourceReference.contains(q, ignoreCase = true)
      catMatch && attrMatch && queryMatch
    }
  }
}
