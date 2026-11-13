import SwiftUI

enum RTLHelper {
    /// Strips Arabic diacritics (Tashkeel) for normalized search matching across Quran, Hadith, and Noorani Qaida.
    static func normalizeArabic(_ input: String) -> String {
        let tashkeelScalars = CharacterSet(charactersIn: "\u{064B}"..."\u{065F}")
            .union(CharacterSet(charactersIn: "\u{0670}\u{06D6}\u{06D7}\u{06D8}\u{06D9}\u{06DA}\u{06DB}\u{06DC}\u{06DF}\u{06E0}\u{06E1}\u{06E2}\u{06E3}\u{06E4}\u{06E7}\u{06E8}\u{06EA}\u{06EB}\u{06EC}\u{06ED}"))
        let filtered = input.unicodeScalars.filter { !tashkeelScalars.contains($0) }
        return String(String.UnicodeScalarView(filtered))
            .replacingOccurrences(of: "أ", with: "ا")
            .replacingOccurrences(of: "إ", with: "ا")
            .replacingOccurrences(of: "آ", with: "ا")
    }
}

extension View {
    /// Forces right-to-left layout direction for Arabic containers (matching Android `android:textDirection="rtl"`).
    func forceArabicRTL() -> some View {
        self
            .environment(\.layoutDirection, .rightToLeft)
            .multilineTextAlignment(.trailing)
    }
}
