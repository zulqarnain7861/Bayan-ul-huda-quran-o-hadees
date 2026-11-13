import SwiftUI

struct HadithRecord: Identifiable, Equatable {
    let id: String
    let collectionSlug: String
    let collectionTitle: String
    let sunnahNumber: String
    let chapterTitle: String
    let arabicMatn: String
    let englishTranslation: String
    let grade: String
}

struct HadithLibraryView: View {
    @State private var searchQuery: String = ""
    @State private var selectedCollection: String = "all"

    static let sampleHadiths: [HadithRecord] = [
        HadithRecord(
            id: "bukhari:1",
            collectionSlug: "bukhari",
            collectionTitle: "Sahih al-Bukhari",
            sunnahNumber: "Sahih al-Bukhari 1",
            chapterTitle: "Book 1: Revelation (كتاب بدء الوحي)",
            arabicMatn: "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
            englishTranslation: "Narrated 'Umar bin Al-Khattab: I heard Allah's Messenger (ﷺ) saying, 'The reward of deeds depends upon the intentions and every person will get the reward according to what he has intended.'",
            grade: "Sahih (Authentic)"
        ),
        HadithRecord(
            id: "bukhari:5027",
            collectionSlug: "bukhari",
            collectionTitle: "Sahih al-Bukhari",
            sunnahNumber: "Sahih al-Bukhari 5027",
            chapterTitle: "Book 66: Virtues of the Qur'an (كتاب فضائل القرآن)",
            arabicMatn: "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
            englishTranslation: "Narrated 'Uthman: The Prophet (ﷺ) said, 'The best among you (Muslims) are those who learn the Qur'an and teach it.'",
            grade: "Sahih (Authentic)"
        ),
        HadithRecord(
            id: "muslim:2699",
            collectionSlug: "muslim",
            collectionTitle: "Sahih Muslim",
            sunnahNumber: "Sahih Muslim 2699a",
            chapterTitle: "Book 48: Remembrance & Supplication",
            arabicMatn: "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ",
            englishTranslation: "Abu Hurairah reported Allah's Messenger (ﷺ) as saying: 'He who treads a path in search of knowledge, Allah would make that path easy for him leading to Paradise.'",
            grade: "Sahih (Authentic)"
        )
    ]

    private var filteredHadiths: [HadithRecord] {
        let q = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        return Self.sampleHadiths.filter { item in
            let matchesCollection = selectedCollection == "all" || item.collectionSlug == selectedCollection
            let matchesQuery = q.isEmpty
                || item.sunnahNumber.lowercased().contains(q)
                || item.englishTranslation.lowercased().contains(q)
                || item.arabicMatn.contains(q)
                || item.chapterTitle.lowercased().contains(q)
            return matchesCollection && matchesQuery
        }
    }

    var body: some View {
        VStack(spacing: 10) {
            TextField("Search by Sunnah.com number (e.g. 5027), Arabic, or English...", text: $searchQuery)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .padding(.horizontal)

            ScrollView {
                LazyVStack(spacing: 14) {
                    ForEach(filteredHadiths) { hadith in
                        VStack(alignment: .leading, spacing: 10) {
                            HStack {
                                Text(hadith.sunnahNumber)
                                    .font(.caption.bold())
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 4)
                                    .background(IslamicTheme.emeraldPrimary)
                                    .foregroundColor(.white)
                                    .cornerRadius(8)
                                Spacer()
                                Text(hadith.grade)
                                    .font(.caption.bold())
                                    .foregroundColor(IslamicTheme.emeraldPrimary)
                            }

                            Text(hadith.chapterTitle)
                                .font(.caption)
                                .foregroundColor(.secondary)

                            Text(hadith.arabicMatn)
                                .font(IslamicTheme.arabicFont(size: 24))
                                .frame(maxWidth: .infinity, alignment: .trailing)
                                .environment(\.layoutDirection, .rightToLeft)
                                .padding(.vertical, 4)

                            Text(hadith.englishTranslation)
                                .font(.system(size: 14))
                                .foregroundColor(.primary)
                        }
                        .islamicIlluminatedCard(cornerRadius: 18, shadowRadius: 5, showCornerMedallions: false)
                    }
                }
                .padding()
            }
        }
        .navigationTitle("Al-Hadith • السنة النبوية")
    }
}
