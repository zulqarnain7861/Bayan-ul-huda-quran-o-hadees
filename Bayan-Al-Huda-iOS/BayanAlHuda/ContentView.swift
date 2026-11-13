import SwiftUI

enum AppSection: String, CaseIterable, Identifiable {
    case quran = "Al-Quran"
    case hadith = "Al-Hadith"
    case nooraniQaida = "Noorani Qaida"
    case videos = "Bayan Videos"
    case search = "Search"
    case duasAndSettings = "Duas & Settings"

    var id: String { rawValue }

    var systemIcon: String {
        switch self {
        case .quran: return "book.closed.fill"
        case .hadith: return "text.book.closed.fill"
        case .nooraniQaida: return "character.book.closed.fill.ar"
        case .videos: return "play.rectangle.fill"
        case .search: return "magnifyingglass"
        case .duasAndSettings: return "hands.sparkles.fill"
        }
    }

    var arabicSubtitle: String {
        switch self {
        case .quran: return "القرآن الكريم"
        case .hadith: return "الأحاديث النبوية"
        case .nooraniQaida: return "القاعدة النورانية"
        case .videos: return "محاضرات الفيديو"
        case .search: return "البحث الشامل"
        case .duasAndSettings: return "الأدعية والإعدادات"
        }
    }
}

struct ContentView: View {
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @State private var selectedSection: AppSection = .nooraniQaida

    var body: some View {
        Group {
            if horizontalSizeClass == .regular {
                // iPad Adaptive Sidebar Split View
                NavigationView {
                    List(AppSection.allCases) { section in
                        Button {
                            selectedSection = section
                        } label: {
                            HStack(spacing: 12) {
                                Image(systemName: section.systemIcon)
                                    .foregroundColor(selectedSection == section ? IslamicTheme.goldPrimary : IslamicTheme.emeraldPrimary)
                                    .frame(width: 26)
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(section.rawValue)
                                        .font(.system(size: 15, weight: .semibold))
                                    Text(section.arabicSubtitle)
                                        .font(.custom(IslamicTheme.amiriFontName, size: 13))
                                        .foregroundColor(.secondary)
                                }
                                Spacer()
                            }
                            .padding(.vertical, 4)
                        }
                        .accessibilityLabel("\(section.rawValue) (\(section.arabicSubtitle))")
                    }
                    .listStyle(SidebarListStyle())
                    .navigationTitle("Bayan Al-Huda")

                    destinationView(for: selectedSection)
                }
            } else {
                // iPhone Native Tab Bar + Navigation Stack
                TabView(selection: $selectedSection) {
                    ForEach(AppSection.allCases) { section in
                        NavigationView {
                            destinationView(for: section)
                        }
                        .navigationViewStyle(StackNavigationViewStyle())
                        .tabItem {
                            Label(section.rawValue, systemImage: section.systemIcon)
                        }
                        .tag(section)
                    }
                }
                .accentColor(IslamicTheme.emeraldPrimary)
            }
        }
    }

    @ViewBuilder
    private func destinationView(for section: AppSection) -> some View {
        switch section {
        case .quran:
            QuranReaderView()
        case .hadith:
            HadithLibraryView()
        case .nooraniQaida:
            NooraniQaidaReaderView()
        case .videos:
            VideoLibraryView()
        case .search:
            GlobalSearchView()
        case .duasAndSettings:
            DuasAndSettingsView()
        }
    }
}
