import Foundation

struct NooraniQaidaPageEntry: Codable, Identifiable, Equatable {
    let pageNumber: Int
    let bookPageNumber: Int
    let lessonNumber: Int
    let lessonTitleEn: String
    let lessonTitleAr: String
    let lessonSubtitle: String
    let arabicText: String
    let searchKeywords: String

    var id: Int { pageNumber }
}

enum QaidaQuizTier: String, CaseIterable, Identifiable {
    case basic = "Basic"
    case intermediate = "Intermediate"
    case hard = "Hard"

    var id: String { rawValue }

    var subtitle: String {
        switch self {
        case .basic: return "Letters, Dots & Short Vowels (Lessons 1–6)"
        case .intermediate: return "Madd, Leen, Sukoon & Tanween (Lessons 7–12)"
        case .hard: return "Shaddah, Ikhfa, Idgham & Waqf Mastery (Lessons 13–27)"
        }
    }
}

struct QaidaExerciseMcq: Identifiable, Equatable {
    let id: String
    let tier: QaidaQuizTier
    let relatedLesson: Int
    let relatedPageNumber: Int
    let arabicSample: String
    let question: String
    let options: [String]
    let correctIndex: Int
    let explanation: String
}

enum NooraniQaidaCatalog {
    static func loadPagesFromBundle() -> [NooraniQaidaPageEntry] {
        let candidateURLs: [URL?] = [
            Bundle.main.url(forResource: "noorani_qaida_text", withExtension: "json"),
            URL(fileURLWithPath: "BayanAlHuda/Resources/noorani_qaida_text.json"),
            URL(fileURLWithPath: "../BayanAlHuda/Resources/noorani_qaida_text.json")
        ]
        for url in candidateURLs.compactMap({ $0 }) {
            if let data = try? Data(contentsOf: url),
               let decoded = try? JSONDecoder().decode([NooraniQaidaPageEntry].self, from: data),
               !decoded.isEmpty {
                return decoded.sorted { $0.pageNumber < $1.pageNumber }
            }
        }
        return []
    }

    static let allExercises: [QaidaExerciseMcq] = [
        // BASIC TIER (12 Questions)
        QaidaExerciseMcq(
            id: "basic_1",
            tier: .basic,
            relatedLesson: 1,
            relatedPageNumber: 4,
            arabicSample: "ا ب ت ث ج ح خ",
            question: "How many single letters (Huroof-e-Mufradat) are in the Arabic alphabet taught in Lesson 1?",
            options: ["26 letters", "28 or 29 letters (including Hamzah)", "32 letters", "24 letters"],
            correctIndex: 1,
            explanation: "Lesson 1 of Noorani Qaida presents 29 Huroof-e-Mufradat (single Arabic letters) starting with Alif and including Hamzah."
        ),
        QaidaExerciseMcq(
            id: "basic_2",
            tier: .basic,
            relatedLesson: 1,
            relatedPageNumber: 4,
            arabicSample: "ب ت ث",
            question: "Which of these boat-shaped letters has exactly ONE dot BELOW it?",
            options: ["Taa (ت)", "Thaa (ث)", "Baa (ب)", "Noon (ن)"],
            correctIndex: 2,
            explanation: "Baa (ب) has one dot below; Taa (ت) has two dots above; Thaa (ث) has three dots above."
        ),
        QaidaExerciseMcq(
            id: "basic_3",
            tier: .basic,
            relatedLesson: 1,
            relatedPageNumber: 4,
            arabicSample: "ج ح خ",
            question: "Among Jeem (ج), Haa (ح), and Khaa (خ), which letter has NO dot at all?",
            options: ["Jeem (ج)", "Haa (ح)", "Khaa (خ)", "Ghayn (غ)"],
            correctIndex: 1,
            explanation: "Haa (ح) is completely free of dots, Jeem (ج) has one dot in its belly, and Khaa (خ) has one dot on top."
        ),
        QaidaExerciseMcq(
            id: "basic_4",
            tier: .basic,
            relatedLesson: 1,
            relatedPageNumber: 5,
            arabicSample: "ث ذ ظ",
            question: "Which group of three letters are pronounced softly by touching the tip of the tongue to the edges of the upper front teeth?",
            options: ["س ز ص", "ث ذ ظ", "ج ش ي", "ق ك غ"],
            correctIndex: 1,
            explanation: "Thaa (ث), Dhaal (ذ), and Zaa (ظ) are the three interdental (Lathawiyyah) letters pronounced softly without whistling."
        ),
        QaidaExerciseMcq(
            id: "basic_5",
            tier: .basic,
            relatedLesson: 1,
            relatedPageNumber: 5,
            arabicSample: "خ ص ض غ ط ق ظ",
            question: "How are the seven elevated letters (خ ص ض غ ط ق ظ — হুরুফে মুস্তা'লিয়াহ) always pronounced?",
            options: ["With a full, heavy mouth (Tafkheem)", "With a nasal sound (Ghunnah)", "Silently", "With an echoing bounce"],
            correctIndex: 0,
            explanation: "The seven letters gathered in 'Khussa Daghdin Qiz' (خ ص ض غ ط ق ظ) are Huroof-e-Musta'liyah and are always read with fullness of mouth."
        ),
        QaidaExerciseMcq(
            id: "basic_6",
            tier: .basic,
            relatedLesson: 2,
            relatedPageNumber: 6,
            arabicSample: "لا با تا نب نت",
            question: "In Lesson 2 (Compound Letters / Huroof-e-Murakkabat), how should you read joined letters before vowels appear?",
            options: ["Recite each individual letter's full name clearly", "Skip the first letter", "Add Fathah to every letter", "Stretch every letter for 4 counts"],
            correctIndex: 0,
            explanation: "In Lesson 2, compound forms are identified by pronouncing the name of each constituent letter separately (e.g., Laam-Alif, Baa-Alif)."
        ),
        QaidaExerciseMcq(
            id: "basic_7",
            tier: .basic,
            relatedLesson: 4,
            relatedPageNumber: 12,
            arabicSample: "بَ بِ بُ",
            question: "What are the three short vowel marks (Harakat: Fathah/Zabar, Kasrah/Zer, Dammah/Pesh) and how long are they held?",
            options: ["1 count (one Harakah) without stretching", "2 counts like Madd", "4 counts", "Held in the nose"],
            correctIndex: 0,
            explanation: "Harakat (Fathah, Kasrah, Dammah) must be pronounced briskly for exactly 1 count without jerking (Jhatka) or stretching."
        ),
        QaidaExerciseMcq(
            id: "basic_8",
            tier: .basic,
            relatedLesson: 4,
            relatedPageNumber: 12,
            arabicSample: "ءَ ءِ ءُ",
            question: "When an Alif carries a Fathah, Kasrah, Dammah, or Sukoon, what is it actually called in Tajweed?",
            options: ["Alif Maddah", "Hamzah (ء)", "Haa", "Waw"],
            correctIndex: 1,
            explanation: "True Alif is always empty and preceded by Fathah; any Alif carrying a vowel mark or Sukoon is a Hamzah."
        ),
        QaidaExerciseMcq(
            id: "basic_9",
            tier: .basic,
            relatedLesson: 5,
            relatedPageNumber: 14,
            arabicSample: "بًا بٍِ بٌ",
            question: "What sound does Tanween (Double Fathah ً, Double Kasrah ٍ, Double Dammah ٌ) produce at the end of a letter?",
            options: ["An unspoken Noon Saakinah ('n') sound", "A long 'aa' sound", "A heavy Qalqalah bounce", "A silent pause"],
            correctIndex: 0,
            explanation: "Tanween is a Noon Saakinah sound written as double vowel marks (an, in, un)."
        ),
        QaidaExerciseMcq(
            id: "basic_10",
            tier: .basic,
            relatedLesson: 6,
            relatedPageNumber: 16,
            arabicSample: "كَتَبَ • عَمِلَ • رُسُلُ",
            question: "In Lesson 6 exercises, when reading three-letter words with Harakat like كَتَبَ, what fault must be avoided?",
            options: ["Stretching a short vowel into a long Alif/Yaa/Waw", "Reading from right to left", "Pronouncing each letter from its Makhraj", "Looking at the dots"],
            correctIndex: 0,
            explanation: "Over-stretching a short Harakah creates an unintended Madd letter, which can change the meaning of Quranic words."
        ),
        QaidaExerciseMcq(
            id: "basic_11",
            tier: .basic,
            relatedLesson: 7,
            relatedPageNumber: 19,
            arabicSample: "بٰ بٖ بٗ",
            question: "How long are Standing Fathah (Khari Zabar ٰ), Standing Kasrah (Khari Zer ٖ), and Inverted Dammah (Ulta Pesh ٗ) stretched?",
            options: ["2 counts (equal to Huroof-e-Maddah)", "1 count", "5 counts", "Not stretched"],
            correctIndex: 0,
            explanation: "Standing Harakat are equivalent to the three Madd letters (Alif, Yaa, Waw Maddah) and are stretched for 2 counts (1 Alif)."
        ),
        QaidaExerciseMcq(
            id: "basic_12",
            tier: .basic,
            relatedLesson: 1,
            relatedPageNumber: 4,
            arabicSample: "ق ك",
            question: "How does Qaaf (ق) differ in pronunciation from Kaaf (ك)?",
            options: ["Qaaf is deeper and heavy (Tafkheem); Kaaf is slightly forward and light (Tarqeeq)", "Both are identical", "Kaaf is heavy and Qaaf is light", "Qaaf is nasal"],
            correctIndex: 0,
            explanation: "Qaaf (ق) originates from the deepest part of the tongue touching the soft palate with fullness, whereas Kaaf (ك) is slightly forward and light."
        ),

        // INTERMEDIATE TIER (12 Questions)
        QaidaExerciseMcq(
            id: "inter_1",
            tier: .intermediate,
            relatedLesson: 8,
            relatedPageNumber: 21,
            arabicSample: "بَا بُو بِي",
            question: "What are the three Huroof-e-Maddah (Letters of Lengthening) taught in Lesson 8?",
            options: [
                "Alif preceded by Fathah, Waw Saakin preceded by Dammah, Yaa Saakin preceded by Kasrah",
                "Waw and Yaa preceded by Fathah",
                "Only Alif and Hamzah",
                "Letters with Shaddah"
            ],
            correctIndex: 0,
            explanation: "Alif after Fathah (بَا), Waw Saakin after Dammah (بُو), and Yaa Saakin after Kasrah (بِي) are Huroof-e-Maddah stretched for 2 counts."
        ),
        QaidaExerciseMcq(
            id: "inter_2",
            tier: .intermediate,
            relatedLesson: 8,
            relatedPageNumber: 22,
            arabicSample: "بَوْ بَيْ",
            question: "When Waw Saakin or Yaa Saakin is preceded by a Fathah (Zabar), as in بَوْ and بَيْ, what are they called?",
            options: ["Huroof-e-Leen (Soft Letters)", "Huroof-e-Maddah", "Huroof-e-Qalqalah", "Huroof-e-Halqi"],
            correctIndex: 0,
            explanation: "Waw Saakin and Yaa Saakin preceded by Fathah are Huroof-e-Leen and are pronounced softly and smoothly without jerking."
        ),
        QaidaExerciseMcq(
            id: "inter_3",
            tier: .intermediate,
            relatedLesson: 10,
            relatedPageNumber: 27,
            arabicSample: "قُطْبُ جَدٍّ (ق ط ب ج د)",
            question: "Which five letters form the group of Qalqalah (echoing bounce) when carrying a Sukoon/Jazm?",
            options: ["ق ط ب ج د (Qutbu Jaddin)", "ي ر م ل و ن (Yarmaloon)", "ء ه ع ح غ خ", "ص ض ط ظ"],
            correctIndex: 0,
            explanation: "When any of the five letters ق ط ب ج د has a Sukoon, it is pronounced with a clear echoing bounce (Qalqalah)."
        ),
        QaidaExerciseMcq(
            id: "inter_4",
            tier: .intermediate,
            relatedLesson: 10,
            relatedPageNumber: 27,
            arabicSample: "أَبْ • أَجْ • أَدْ",
            question: "Can a letter with Sukoon (Jazm ْ) be pronounced by itself at the start of a syllable?",
            options: ["No, it must join with the voweled letter before it", "Yes, with a Fathah", "Yes, silently", "Only if it is Alif"],
            correctIndex: 0,
            explanation: "A Saakin letter cannot be initiated alone; it always merges with the preceding voweled (Mutaharrik) letter."
        ),
        QaidaExerciseMcq(
            id: "inter_5",
            tier: .intermediate,
            relatedLesson: 10,
            relatedPageNumber: 28,
            arabicSample: "ء هـ ع ح غ خ",
            question: "When Noon Saakin (نْ) or Tanween is followed by any of the 6 throat letters (ء هـ ع ح غ خ), which rule applies?",
            options: ["Izhar-e-Halqi (Clear pronunciation without additional Ghunnah)", "Ikhfa (Nasal hiding)", "Iqlab (Changing to Meem)", "Idgham (Merging)"],
            correctIndex: 0,
            explanation: "Before the six Huroof-e-Halqi (throat letters), Noon Saakin or Tanween is pronounced clearly without nasal stretching (Izhar)."
        ),
        QaidaExerciseMcq(
            id: "inter_6",
            tier: .intermediate,
            relatedLesson: 11,
            relatedPageNumber: 31,
            arabicSample: "مَنْ آمَنَ • مِنْ تَحْتِهَا",
            question: "In Lesson 11 (Noon Saakin & Tanween exercises), what is the rule when Noon Saakin is followed by one of the 15 Ikhfa letters (like ت ث ج د...)?",
            options: ["Ikhfa: Hide the Noon sound in the nose with 2 counts of Ghunnah", "Read clearly with no Ghunnah", "Change Noon into Waw", "Drop the Noon completely"],
            correctIndex: 0,
            explanation: "Ikhfa-e-Haqiqi hides the Noon Saakin/Tanween sound between Izhar and Idgham with a 2-count nasal Ghunnah."
        ),
        QaidaExerciseMcq(
            id: "inter_7",
            tier: .intermediate,
            relatedLesson: 10,
            relatedPageNumber: 29,
            arabicSample: "رْ",
            question: "When is Raa Saakin (رْ) pronounced heavy (Tafkheem)?",
            options: ["When the letter before it has a Fathah or Dammah", "When the letter before it has an original Kasrah", "Never", "Only at the beginning of a word"],
            correctIndex: 0,
            explanation: "Raa Saakin is read with a full mouth (Tafkheem) if preceded by Fathah or Dammah, and light (Tarqeeq) if preceded by an original Kasrah."
        ),
        QaidaExerciseMcq(
            id: "inter_8",
            tier: .intermediate,
            relatedLesson: 12,
            relatedPageNumber: 35,
            arabicSample: "أَبَّ أَتَّ أَثَّ",
            question: "A letter carrying a Shaddah / Tashdeed (ّ) actually represents how many letters combined?",
            options: ["Two identical letters: the first Saakin and the second voweled", "Three letters", "One long vowel", "A silent letter"],
            correctIndex: 0,
            explanation: "Tashdeed (ّ) merges a Saakin letter followed by an identical Mutaharrik letter into one stressed pronunciation with firmness."
        ),
        QaidaExerciseMcq(
            id: "inter_9",
            tier: .intermediate,
            relatedLesson: 12,
            relatedPageNumber: 36,
            arabicSample: "إِنَّ • ثُمَّ",
            question: "What is the mandatory rule whenever Noon or Meem has a Shaddah (نّ / مّ — Noon & Meem Mushaddad)?",
            options: ["Hold a clear nasal Ghunnah for 2 counts (Wajib Ghunnah)", "Read quickly without nasalization", "Perform Qalqalah", "Stretch for 6 counts"],
            correctIndex: 0,
            explanation: "Noon Mushaddad (نّ) and Meem Mushaddad (مّ) always require Wajib Ghunnah held in the nasal passage for 2 counts."
        ),
        QaidaExerciseMcq(
            id: "inter_10",
            tier: .intermediate,
            relatedLesson: 13,
            relatedPageNumber: 39,
            arabicSample: "اللّٰهُ • بِسْمِ اللّٰهِ",
            question: "How is the Laam in the Majestic Name 'Allah' (اللّٰه) pronounced when preceded by a Kasrah (as in بِسْمِ اللّٰهِ)?",
            options: ["Light / thin (Tarqeeq)", "Heavy / full mouth (Tafkheem)", "With nasal Ghunnah", "Silently"],
            correctIndex: 0,
            explanation: "The Laam of Lafz-ul-Jalalah (اللّٰه) is read light (Tarqeeq) after a Kasrah, and heavy (Tafkheem) after a Fathah or Dammah."
        ),
        QaidaExerciseMcq(
            id: "inter_11",
            tier: .intermediate,
            relatedLesson: 9,
            relatedPageNumber: 24,
            arabicSample: "خَوْفٍ • قُرَيْشٍ",
            question: "When stopping (Waqf) on a word where a Leen letter is followed by the final consonant (like خَوْفْ or قُرَيْشْ), which Madd occurs?",
            options: ["Madd-e-Leen Aarid (2, 4, or 6 counts)", "No Madd at all", "Iqlab", "Izhar"],
            correctIndex: 0,
            explanation: "Stopping after a Leen letter creates a temporary Sukoon on the last letter, allowing Madd-e-Leen for 2, 4, or 6 counts."
        ),
        QaidaExerciseMcq(
            id: "inter_12",
            tier: .intermediate,
            relatedLesson: 14,
            relatedPageNumber: 41,
            arabicSample: "رَبِّ اشْرَحْ",
            question: "What happens to Hamzat-ul-Wasl (connecting Alif) when reading continuously from the preceding word?",
            options: ["It is dropped in pronunciation, connecting directly to the next Saakin letter", "It is pronounced with a loud Hamzah", "It is stretched for 4 counts", "It requires a mandatory pause"],
            correctIndex: 0,
            explanation: "Hamzat-ul-Wasl is only pronounced when starting speech on that word; in continuous recitation it drops out."
        ),

        // HARD TIER (11 Questions)
        QaidaExerciseMcq(
            id: "hard_1",
            tier: .hard,
            relatedLesson: 15,
            relatedPageNumber: 43,
            arabicSample: "مِنْ رَّبِّهِمْ • مِنْ لَّدُنْهُ",
            question: "When Noon Saakin or Tanween is followed by Laam (ل) or Raa (ر), what type of Idgham takes place?",
            options: ["Idgham Bila Ghunnah (Complete merging WITHOUT nasalization)", "Idgham Ma'al Ghunnah (With nasalization)", "Ikhfa Shafawi", "Izhar"],
            correctIndex: 0,
            explanation: "Among the 6 Yarmaloon letters (يرملون), Laam (ل) and Raa (ر) merge completely without Ghunnah (Idgham Bila Ghunnah)."
        ),
        QaidaExerciseMcq(
            id: "hard_2",
            tier: .hard,
            relatedLesson: 15,
            relatedPageNumber: 43,
            arabicSample: "مَنْ يَّقُولُ • مِنْ وَّلِيٍّ",
            question: "When Noon Saakin or Tanween is followed by Yaa, Noon, Meem, or Waw (ي ن م و — Yanmoo) in the NEXT word, which rule applies?",
            options: ["Idgham Ma'al Ghunnah (Merging WITH 2 counts of Ghunnah)", "Idgham Bila Ghunnah", "Iqlab", "Izhar-e-Mutlaq"],
            correctIndex: 0,
            explanation: "When Noon Saakin or Tanween meets any of ي ن م و across two words, it merges with a 2-count nasal Ghunnah."
        ),
        QaidaExerciseMcq(
            id: "hard_3",
            tier: .hard,
            relatedLesson: 15,
            relatedPageNumber: 44,
            arabicSample: "دُنْيَا • بُنْيَانٌ • صِنْوَانٌ • قِنْوَانٌ",
            question: "Why is Idgham NOT performed in the four Quranic words دُنْيَا, بُنْيَانٌ, صِنْوَانٌ, and قِنْوَانٌ even though Noon Saakin is followed by Yaa or Waw?",
            options: ["Because Noon Saakin and Yaa/Waw occur within the SAME word (Izhar-e-Mutlaq)", "Because they are Makki words", "Because of Waqf", "Because Yaa is silent"],
            correctIndex: 0,
            explanation: "Idgham requires Noon Saakin to be at the end of the first word and Yanmoo at the start of the second; inside a single word it is read clearly (Izhar-e-Mutlaq)."
        ),
        QaidaExerciseMcq(
            id: "hard_4",
            tier: .hard,
            relatedLesson: 16,
            relatedPageNumber: 46,
            arabicSample: "مِنْ بَعْدِ (مۢ بَعْدِ)",
            question: "When Noon Saakin or Tanween is followed by the letter Baa (ب), notice the tiny Meem (م) symbol above. What is this rule?",
            options: ["Iqlab: Convert the Noon Saakin/Tanween into a hidden Meem with 2 counts of Ghunnah", "Izhar-e-Halqi", "Idgham Bila Ghunnah", "Qalqalah"],
            correctIndex: 0,
            explanation: "Before Baa (ب), Noon Saakin or Tanween changes into a Meem sound with Ghunnah (Iqlab)."
        ),
        QaidaExerciseMcq(
            id: "hard_5",
            tier: .hard,
            relatedLesson: 17,
            relatedPageNumber: 47,
            arabicSample: "تَرْمِيهِمْ بِحِجَارَةٍ • لَهُمْ مَّا",
            question: "What are the three rules of Meem Saakin (مْ) taught in Noorani Qaida?",
            options: [
                "Ikhfa Shafawi (before ب), Idgham Shafawi (before م), and Izhar Shafawi (before all other 26 letters)",
                "Only Izhar and Iqlab",
                "Qalqalah and Tafkheem",
                "Madd Lazim and Madd Muttasil"
            ],
            correctIndex: 0,
            explanation: "Meem Saakin has three labial (Shafawi) rules: Ikhfa Shafawi before Baa, Idgham Shafawi before Meem, and Izhar Shafawi before all remaining letters."
        ),
        QaidaExerciseMcq(
            id: "hard_6",
            tier: .hard,
            relatedLesson: 18,
            relatedPageNumber: 49,
            arabicSample: "جَآءَ • سُوٓءَ • الضَّآلِّينَ",
            question: "In Madd-e-Lazim (such as الضَّآلِّينَ where a Madd letter is followed by a permanent Shaddah or Sukoon), how many counts must it be stretched?",
            options: ["6 counts (3 Alifs) compulsorily", "2 counts", "1 count", "Optional"],
            correctIndex: 0,
            explanation: "Madd-e-Lazim occurs when a permanent Sukoon or Shaddah follows a Madd letter in the same word, requiring a mandatory 6-count stretch."
        ),
        QaidaExerciseMcq(
            id: "hard_7",
            tier: .hard,
            relatedLesson: 19,
            relatedPageNumber: 50,
            arabicSample: "الٓمٓ • يٰسٓ • طسمٓ",
            question: "In Huroof-e-Muqatta'at at the opening of Surahs, how are the letters of 'Naqasa Asalukum' (ن ق ص ع س ل ك م) recited?",
            options: ["Stretched for 6 counts (Madd Lazim Harfi)", "Stretched for 2 counts (Haya Tahura)", "Not stretched at all (Alif)", "Skipped"],
            correctIndex: 0,
            explanation: "Huroof-e-Muqatta'at divide into Alif (no Madd), 'Hayy-un Tahur' (حي طهر — 2 counts), and 'Naqasa Asalukum' (نقص عسلكم — 6 counts)."
        ),
        QaidaExerciseMcq(
            id: "hard_8",
            tier: .hard,
            relatedLesson: 20,
            relatedPageNumber: 52,
            arabicSample: "رَحْمَةٌ ➔ رَحْمَهْ",
            question: "When stopping (Waqf) on a word ending with the round Taa Marbootah (ة / ـة), how is that final letter pronounced?",
            options: ["As a quiet Haa Saakinah (هْ) regardless of its vowel", "As a strong Taa (تْ)", "With Tanween", "With Qalqalah"],
            correctIndex: 0,
            explanation: "During Waqf, round Taa Marbootah (ة) always transforms into a soft Haa Saakinah (هْ)."
        ),
        QaidaExerciseMcq(
            id: "hard_9",
            tier: .hard,
            relatedLesson: 20,
            relatedPageNumber: 52,
            arabicSample: "عَلِيمًا ➔ عَلِيمَا",
            question: "When stopping (Waqf) on a word ending with Double Fathah (Fathatain ً, except on round ة), what is the rule?",
            options: ["Madd-e-Iwad: Replace the Tanween with an Alif Maddah stretched for 2 counts", "Make the last letter Saakin with no vowel", "Keep the 'an' sound", "Add a Haa sound"],
            correctIndex: 0,
            explanation: "Stopping on Fathatain (other than round Taa) substitutes the Tanween with a 2-count Alif Maddah (Madd-e-Iwad)."
        ),
        QaidaExerciseMcq(
            id: "hard_10",
            tier: .hard,
            relatedLesson: 20,
            relatedPageNumber: 53,
            arabicSample: "مـ • لا • ج • ط • قف",
            question: "Among the Quranic Waqf (stop) signs at the end of Noorani Qaida, what do the small superscript Meem (مـ) and Laa (لا) indicate?",
            options: ["مـ is Waqf-e-Lazim (Compulsory Stop); لا means Do Not Stop in the middle of an Ayah", "Both mean optional stop", "مـ means continue; لا means compulsory stop", "They are Tajweed nasal marks"],
            correctIndex: 0,
            explanation: "Small Meem (مـ) marks Waqf-e-Lazim where continuing alters the meaning, whereas Laa (لا) warns against stopping mid-phrase."
        ),
        QaidaExerciseMcq(
            id: "hard_11",
            tier: .hard,
            relatedLesson: 20,
            relatedPageNumber: 53,
            arabicSample: "سَكْتَة (س) • نُون قُطْنِي (نِ)",
            question: "What is 'Saktah' (سكتة) in Quranic recitation, and what is 'Noon Qutni' (tiny نِ)?",
            options: [
                "Saktah is a brief 2-count pause without breaking breath; Noon Qutni is a tiny Noon with Kasrah inserted when Tanween meets Hamzat-ul-Wasl",
                "Saktah is taking a deep breath; Noon Qutni is a silent Noon",
                "Both mean repeating the verse",
                "Saktah is prostration (Sajdah)"
            ],
            correctIndex: 0,
            explanation: "Saktah pauses the voice briefly without taking a breath, and Noon Qutni (نِ) bridges a Tanween to a following Saakin letter across Hamzat-ul-Wasl."
        )
    ]

    static func exercises(for tier: QaidaQuizTier) -> [QaidaExerciseMcq] {
        allExercises.filter { $0.tier == tier }
    }
}
