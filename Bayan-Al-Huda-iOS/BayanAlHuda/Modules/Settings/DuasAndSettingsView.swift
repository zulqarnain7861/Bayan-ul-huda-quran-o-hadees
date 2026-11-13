import SwiftUI

struct DuasAndSettingsView: View {
    @AppStorage("app_color_scheme") private var appColorScheme: String = "system"
    @AppStorage("preferred_reciter_id") private var preferredReciterId: String = "mishary_rashid_alafasy"
    @AppStorage("last_read_noorani_qaida_page") private var lastQaidaPage: Int = 1

    private let duas: [(title: String, arabic: String, translation: String)] = [
        (
            "Dua for Increasing Knowledge",
            "رَّبِّ زِدْنِي عِلْمًا",
            "My Lord, increase me in knowledge. (Surah Taha 20:114)"
        ),
        (
            "Morning & Evening Protection",
            "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            "In the Name of Allah with Whose Name there is protection against every kind of harm in the earth or in the heaven, and He is the All-Hearing and All-Knowing."
        )
    ]

    var body: some View {
        Form {
            Section("Masnoon Duas & Azkar") {
                ForEach(duas, id: \.title) { dua in
                    VStack(alignment: .trailing, spacing: 8) {
                        Text(dua.title)
                            .font(.subheadline.bold())
                            .frame(maxWidth: .infinity, alignment: .leading)
                        Text(dua.arabic)
                            .font(IslamicTheme.arabicFont(size: 22))
                            .foregroundColor(IslamicTheme.emeraldPrimary)
                            .environment(\.layoutDirection, .rightToLeft)
                        Text(dua.translation)
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    .padding(.vertical, 6)
                }
            }

            Section("Appearance & Sync State") {
                Picker("Theme Mode", selection: $appColorScheme) {
                    Text("System").tag("system")
                    Text("Light").tag("light")
                    Text("Dark").tag("dark")
                }
                HStack {
                    Text("Saved Reciter ID")
                    Spacer()
                    Text(preferredReciterId)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                HStack {
                    Text("Last Noorani Qaida Page")
                    Spacer()
                    Text("Page \(lastQaidaPage) of 53")
                        .font(.caption.bold())
                        .foregroundColor(IslamicTheme.emeraldPrimary)
                }
            }

            Section("Platform Parity Info") {
                HStack {
                    Text("Version")
                    Spacer()
                    Text("1.0.2 (Build 8)")
                        .foregroundColor(.secondary)
                }
                HStack {
                    Text("Primary Upstream Repo")
                    Spacer()
                    Text("Bayan-ul-huda-quran-o-hadees")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
        }
        .navigationTitle("Duas & Settings")
    }
}
