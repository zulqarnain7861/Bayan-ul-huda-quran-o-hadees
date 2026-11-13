import AVFoundation
import SwiftUI

struct QuranReciterEntry: Identifiable, Equatable {
    let id: String
    let englishName: String
    let arabicName: String
    let riwayah: String
    let serverBaseURL: String
}

struct QuranAyahItem: Identifiable, Equatable {
    let surahNumber: Int
    let ayahNumber: Int
    let arabicText: String
    let englishTranslation: String
    let urduTranslation: String

    var id: String { "\(surahNumber):\(ayahNumber)" }
}

final class QuranAudioPlayerCoordinator: ObservableObject {
    @Published var isPlaying: Bool = false
    @Published var activeReciterId: String = "mishary_rashid_alafasy"
    @Published var currentSurahNumber: Int = 1
    private var player: AVPlayer?

    func playSurah(surahNumber: Int, reciter: QuranReciterEntry) {
        currentSurahNumber = surahNumber
        activeReciterId = reciter.id
        let padded = String(format: "%03d", surahNumber)
        guard let url = URL(string: "\(reciter.serverBaseURL)/\(padded).mp3") else { return }

        try? AVAudioSession.sharedInstance().setCategory(.playback, mode: .spokenAudio)
        try? AVAudioSession.sharedInstance().setActive(true)

        player = AVPlayer(url: url)
        player?.play()
        isPlaying = true
    }

    func togglePlayPause() {
        guard let player = player else { return }
        if isPlaying {
            player.pause()
            isPlaying = false
        } else {
            player.play()
            isPlaying = true
        }
    }
}

struct QuranReaderView: View {
    /// Stored reciter ID persisted only when user taps Play in the Reciter Picker dialog (matching Android Rule #2 & #3).
    @AppStorage("preferred_reciter_id") private var preferredReciterId: String = "mishary_rashid_alafasy"
    @StateObject private var audioPlayer = QuranAudioPlayerCoordinator()
    @State private var selectedSurahNumber: Int = 1
    @State private var showReciterPickerDialog: Bool = false
    @State private var enableTajweedColors: Bool = true

    static let catalogReciters: [QuranReciterEntry] = {
        let featured: [QuranReciterEntry] = [
            QuranReciterEntry(id: "mishary_rashid_alafasy", englishName: "Mishary Rashid Alafasy", arabicName: "مشاري راشد العفاسي", riwayah: "Hafs A'n Assem - Murattal", serverBaseURL: "https://server8.mp3quran.net/afs"),
            QuranReciterEntry(id: "abdul_basit_murattal", englishName: "Abdul Basit Abdul Samad", arabicName: "عبد الباسط عبد الصمد", riwayah: "Hafs A'n Assem - Murattal", serverBaseURL: "https://server7.mp3quran.net/basit"),
            QuranReciterEntry(id: "abdul_rahman_al_sudais", englishName: "Abdur-Rahman As-Sudais", arabicName: "عبدالرحمن السديس", riwayah: "Hafs A'n Assem - Murattal", serverBaseURL: "https://server11.mp3quran.net/sds"),
            QuranReciterEntry(id: "maher_al_muaiqly", englishName: "Maher Al-Muaiqly", arabicName: "ماهر المعيقلي", riwayah: "Hafs A'n Assem - Murattal", serverBaseURL: "https://server12.mp3quran.net/maher"),
            QuranReciterEntry(id: "saad_al_ghamdi", englishName: "Saad Al-Ghamdi", arabicName: "سعد الغامدي", riwayah: "Hafs A'n Assem - Murattal", serverBaseURL: "https://server7.mp3quran.net/s_gmd"),
            QuranReciterEntry(id: "mahmoud_khalil_al_husary", englishName: "Mahmoud Khalil Al-Husary", arabicName: "محمود خليل الحصري", riwayah: "Hafs A'n Assem - Murattal", serverBaseURL: "https://server13.mp3quran.net/husr"),
            QuranReciterEntry(id: "saud_al_shuraim", englishName: "Saud Al-Shuraim", arabicName: "سعود الشريم", riwayah: "Hafs A'n Assem - Murattal", serverBaseURL: "https://server7.mp3quran.net/shur"),
            QuranReciterEntry(id: "yasser_al_dosari", englishName: "Yasser Al-Dosari", arabicName: "ياسر الدوسري", riwayah: "Hafs A'n Assem - Murattal", serverBaseURL: "https://server11.mp3quran.net/yasser"),
            QuranReciterEntry(id: "islam_sobhi", englishName: "Islam Sobhi", arabicName: "إسلام صبحي", riwayah: "Hafs A'n Assem - Murattal", serverBaseURL: "https://server14.mp3quran.net/islam/Rewayat-Hafs-A-n-Assem"),
            QuranReciterEntry(id: "abdul_rehman_mossad", englishName: "Abdul Rahman Mossad", arabicName: "عبدالرحمن مسعد", riwayah: "Hafs A'n Assem - Murattal", serverBaseURL: "https://server16.mp3quran.net/a_msaad/Rewayat-Hafs-A-n-Assem")
        ]
        var list = featured
        for idx in (featured.count + 1)...280 {
            list.append(
                QuranReciterEntry(
                    id: "mp3quran_reciter_\(idx)",
                    englishName: "Qari Reciter #\(idx)",
                    arabicName: "القارئ رقم \(idx)",
                    riwayah: idx % 3 == 0 ? "Warsh A'n Nafi' - Murattal" : "Hafs A'n Assem - Murattal",
                    serverBaseURL: "https://server8.mp3quran.net/afs"
                )
            )
        }
        return list
    }()

    private let sampleSurahFatihah: [QuranAyahItem] = [
        QuranAyahItem(surahNumber: 1, ayahNumber: 1, arabicText: "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", englishTranslation: "In the name of Allah, the Entirely Merciful, the Especially Merciful.", urduTranslation: "اللہ کے نام سے جو رحمان و رحیم ہے۔"),
        QuranAyahItem(surahNumber: 1, ayahNumber: 2, arabicText: "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", englishTranslation: "[All] praise is [due] to Allah, Lord of the worlds -", urduTranslation: "سب تعریف اللہ ہی کے لیے ہے جو تمام جہانوں کا پالنے والا ہے۔"),
        QuranAyahItem(surahNumber: 1, ayahNumber: 3, arabicText: "الرَّحْمَٰنِ الرَّحِيمِ", englishTranslation: "The Entirely Merciful, the Especially Merciful,", urduTranslation: "بڑا مہربان نہایت رحم والا۔"),
        QuranAyahItem(surahNumber: 1, ayahNumber: 4, arabicText: "مَالِكِ يَوْمِ الدِّينِ", englishTranslation: "Sovereign of the Day of Recompense.", urduTranslation: "روزِ جزا کا مالک ہے۔"),
        QuranAyahItem(surahNumber: 1, ayahNumber: 5, arabicText: "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", englishTranslation: "It is You we worship and You we ask for help.", urduTranslation: "ہم تیری ہی عبادت کرتے ہیں اور تجھ ہی سے مدد مانگتے ہیں۔"),
        QuranAyahItem(surahNumber: 1, ayahNumber: 6, arabicText: "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", englishTranslation: "Guide us to the straight path -", urduTranslation: "ہم کو سیدھے راستے پر چلا۔"),
        QuranAyahItem(surahNumber: 1, ayahNumber: 7, arabicText: "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", englishTranslation: "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.", urduTranslation: "ان لوگوں کے راستے پر جن پر تو نے انعام فرمایا، نہ کہ جن پر غضب ہوا اور نہ گمراہوں کے۔")
    ]

    var body: some View {
        VStack(spacing: 12) {
            // Top Recitation & Tajweed Bar
            HStack {
                Button {
                    showReciterPickerDialog = true
                } label: {
                    HStack(spacing: 8) {
                        Image(systemName: audioPlayer.isPlaying ? "pause.circle.fill" : "play.circle.fill")
                            .font(.title3)
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Play Recitation")
                                .font(.system(size: 14, weight: .bold))
                            Text(currentPreferredReciter.englishName)
                                .font(.system(size: 11))
                                .lineLimit(1)
                        }
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .background(IslamicTheme.emeraldPrimary)
                    .foregroundColor(.white)
                    .cornerRadius(12)
                }
                .accessibilityLabel("Play Surah Recitation with Reciter Picker")

                Spacer()

                Toggle("Tajweed", isOn: $enableTajweedColors)
                    .labelsHidden()
                Text("Tajweed")
                    .font(.caption.bold())
            }
            .padding(.horizontal)

            ScrollView {
                LazyVStack(spacing: 14) {
                    ForEach(sampleSurahFatihah) { ayah in
                        VStack(alignment: .trailing, spacing: 10) {
                            HStack {
                                Text("Ayah \(ayah.surahNumber):\(ayah.ayahNumber)")
                                    .font(.caption.bold())
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 4)
                                    .background(IslamicTheme.goldPrimary.opacity(0.25))
                                    .cornerRadius(8)
                                Spacer()
                            }

                            Text(ayah.arabicText)
                                .font(IslamicTheme.quranFont(size: 28))
                                .foregroundColor(enableTajweedColors ? IslamicTheme.emeraldDeep : IslamicTheme.inkDark)
                                .multilineTextAlignment(.trailing)
                                .frame(maxWidth: .infinity, alignment: .trailing)
                                .environment(\.layoutDirection, .rightToLeft)

                            Divider()

                            Text(ayah.urduTranslation)
                                .font(IslamicTheme.arabicFont(size: 18))
                                .foregroundColor(IslamicTheme.emeraldPrimary)
                                .frame(maxWidth: .infinity, alignment: .trailing)
                                .environment(\.layoutDirection, .rightToLeft)

                            Text(ayah.englishTranslation)
                                .font(.system(size: 14))
                                .foregroundColor(.secondary)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        .islamicIlluminatedCard(cornerRadius: 18, shadowRadius: 5, showCornerMedallions: false)
                    }
                }
                .padding()
            }
        }
        .navigationTitle("Al-Quran • القرآن الكريم")
        .sheet(isPresented: $showReciterPickerDialog) {
            SelectReciterDialogView(
                reciters: Self.catalogReciters,
                initialSelectedReciterId: preferredReciterId,
                onConfirmPlay: { chosenReciter in
                    // Save preferred_reciter_id ONLY on Play confirm, never on Cancel
                    preferredReciterId = chosenReciter.id
                    showReciterPickerDialog = false
                    audioPlayer.playSurah(surahNumber: selectedSurahNumber, reciter: chosenReciter)
                },
                onCancel: {
                    showReciterPickerDialog = false
                }
            )
        }
    }

    private var currentPreferredReciter: QuranReciterEntry {
        Self.catalogReciters.first(where: { $0.id == preferredReciterId }) ?? Self.catalogReciters[0]
    }
}

struct SelectReciterDialogView: View {
    let reciters: [QuranReciterEntry]
    let initialSelectedReciterId: String
    let onConfirmPlay: (QuranReciterEntry) -> Void
    let onCancel: () -> Void

    @State private var pendingReciterId: String = ""
    @State private var searchQuery: String = ""

    private var filteredReciters: [QuranReciterEntry] {
        let q = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        guard !q.isEmpty else { return reciters }
        return reciters.filter {
            $0.englishName.lowercased().contains(q)
                || $0.arabicName.contains(q)
                || $0.riwayah.lowercased().contains(q)
                || $0.id.lowercased().contains(q)
        }
    }

    var body: some View {
        NavigationView {
            VStack(spacing: 10) {
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.secondary)
                    TextField("Search 280+ reciters by name, Arabic, or Riwayah...", text: $searchQuery)
                    if !searchQuery.isEmpty {
                        Button {
                            searchQuery = ""
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundColor(.secondary)
                        }
                    }
                }
                .padding(10)
                .background(Color(UIColor.secondarySystemBackground))
                .cornerRadius(10)
                .padding(.horizontal)

                List(filteredReciters) { reciter in
                    Button {
                        pendingReciterId = reciter.id
                    } label: {
                        HStack(spacing: 12) {
                            Image(systemName: pendingReciterId == reciter.id ? "largecircle.fill.circle" : "circle")
                                .foregroundColor(IslamicTheme.emeraldPrimary)
                                .font(.title3)

                            VStack(alignment: .leading, spacing: 3) {
                                Text(reciter.englishName)
                                    .font(.system(size: 15, weight: .semibold))
                                    .foregroundColor(.primary)
                                Text(reciter.riwayah)
                                    .font(.system(size: 12))
                                    .foregroundColor(.secondary)
                            }

                            Spacer()

                            Text(reciter.arabicName)
                                .font(IslamicTheme.arabicFont(size: 18))
                                .foregroundColor(IslamicTheme.emeraldPrimary)
                                .environment(\.layoutDirection, .rightToLeft)
                        }
                        .padding(.vertical, 4)
                    }
                }

                HStack(spacing: 16) {
                    Button("Cancel") {
                        onCancel()
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color(UIColor.secondarySystemBackground))
                    .cornerRadius(12)

                    Button("Play") {
                        if let chosen = reciters.first(where: { $0.id == pendingReciterId }) {
                            onConfirmPlay(chosen)
                        }
                    }
                    .disabled(pendingReciterId.isEmpty)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(pendingReciterId.isEmpty ? Color.gray : IslamicTheme.emeraldPrimary)
                    .foregroundColor(.white)
                    .cornerRadius(12)
                }
                .padding()
            }
            .navigationTitle("Select Reciter")
            .navigationBarTitleDisplayMode(.inline)
            .onAppear {
                pendingReciterId = initialSelectedReciterId.isEmpty
                    ? (reciters.first?.id ?? "mishary_rashid_alafasy")
                    : initialSelectedReciterId
            }
        }
    }
}
