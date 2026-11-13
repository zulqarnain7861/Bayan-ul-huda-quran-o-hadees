import SwiftUI

struct BayanVideoLecture: Identifiable {
    let id: String
    let title: String
    let scholar: String
    let duration: String
    let youtubeVideoId: String

    var primaryEmbedURL: URL? {
        URL(string: "https://www.youtube-nocookie.com/embed/\(youtubeVideoId)?playsinline=1&rel=0")
    }

    var fallbackWatchURL: URL? {
        URL(string: "https://www.youtube.com/watch?v=\(youtubeVideoId)")
    }
}

struct VideoLibraryView: View {
    @Environment(\.openURL) private var openURL

    private let lectures: [BayanVideoLecture] = [
        BayanVideoLecture(id: "vid_1", title: "Complete Noorani Qaida Makharij & Tajweed Guide", scholar: "Bayan Al-Huda Academy", duration: "42:18", youtubeVideoId: "dQw4w9WgXcQ"),
        BayanVideoLecture(id: "vid_2", title: "Tafsir Surah Al-Fatihah & Essentials of Salah", scholar: "Shaykh Bayan Series", duration: "36:05", youtubeVideoId: "M7lc1UVf-VE"),
        BayanVideoLecture(id: "vid_3", title: "Understanding Sahih al-Bukhari Book of Revelation", scholar: "Hadith Circle", duration: "51:40", youtubeVideoId: "ysz5S6PUM-U")
    ]

    var body: some View {
        List(lectures) { lecture in
            VStack(alignment: .leading, spacing: 8) {
                Text(lecture.title)
                    .font(.system(size: 16, weight: .bold))
                HStack {
                    Text(lecture.scholar)
                        .font(.caption)
                        .foregroundColor(.secondary)
                    Spacer()
                    Text(lecture.duration)
                        .font(.caption.bold())
                        .foregroundColor(IslamicTheme.emeraldPrimary)
                }
                HStack(spacing: 12) {
                    if let embed = lecture.primaryEmbedURL {
                        Button("Tier 1: Embedded Player") {
                            openURL(embed)
                        }
                        .font(.caption.bold())
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(IslamicTheme.emeraldPrimary)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                    }
                    if let watch = lecture.fallbackWatchURL {
                        Button("Tier 2: YouTube Fallback") {
                            openURL(watch)
                        }
                        .font(.caption.bold())
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(IslamicTheme.goldPrimary.opacity(0.25))
                        .foregroundColor(IslamicTheme.emeraldDeep)
                        .cornerRadius(8)
                    }
                }
            }
            .padding(.vertical, 6)
        }
        .navigationTitle("Bayan Videos")
    }
}
