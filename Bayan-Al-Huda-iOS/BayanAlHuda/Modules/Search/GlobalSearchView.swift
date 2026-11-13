import SwiftUI

struct GlobalSearchView: View {
    @State private var query: String = ""
    private let qaidaPages = NooraniQaidaCatalog.loadPagesFromBundle()

    var body: some View {
        VStack(spacing: 10) {
            TextField("Search across Quran, Hadith & 53-page Noorani Qaida...", text: $query)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .padding(.horizontal)

            List {
                Section("Noorani Qaida Matches") {
                    ForEach(matchingQaidaPages) { page in
                        VStack(alignment: .leading, spacing: 4) {
                            HStack {
                                Text("Page \(page.pageNumber) (Book Top \(page.bookPageNumber))")
                                    .font(.caption.bold())
                                    .foregroundColor(IslamicTheme.emeraldPrimary)
                                Spacer()
                                Text(page.lessonTitleAr)
                                    .font(IslamicTheme.arabicFont(size: 16))
                            }
                            Text(page.lessonTitleEn)
                                .font(.subheadline.bold())
                            Text(page.arabicText)
                                .font(IslamicTheme.arabicFont(size: 18))
                                .lineLimit(2)
                                .environment(\.layoutDirection, .rightToLeft)
                        }
                    }
                }

                Section("Hadith Matches") {
                    ForEach(matchingHadiths) { hadith in
                        VStack(alignment: .leading, spacing: 4) {
                            Text(hadith.sunnahNumber)
                                .font(.caption.bold())
                                .foregroundColor(IslamicTheme.emeraldPrimary)
                            Text(hadith.englishTranslation)
                                .font(.footnote)
                                .lineLimit(2)
                        }
                    }
                }
            }
        }
        .navigationTitle("Unified Search")
    }

    private var matchingQaidaPages: [NooraniQaidaPageEntry] {
        let q = query.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        guard !q.isEmpty else { return Array(qaidaPages.prefix(5)) }
        return qaidaPages.filter {
            $0.lessonTitleEn.lowercased().contains(q)
                || $0.lessonTitleAr.contains(q)
                || $0.arabicText.contains(q)
                || $0.searchKeywords.lowercased().contains(q)
        }
    }

    private var matchingHadiths: [HadithRecord] {
        let q = query.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        guard !q.isEmpty else { return HadithLibraryView.sampleHadiths }
        return HadithLibraryView.sampleHadiths.filter {
            $0.sunnahNumber.lowercased().contains(q)
                || $0.englishTranslation.lowercased().contains(q)
                || $0.arabicMatn.contains(q)
        }
    }
}
