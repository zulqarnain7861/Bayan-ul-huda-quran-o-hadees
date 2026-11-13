import SwiftUI

enum IslamicTheme {
    static let amiriFontName = "Amiri-Regular"
    static let scheherazadeFontName = "ScheherazadeNew-Regular"

    // Shared Android <-> iOS Brand Palette
    static let emeraldDeep = Color(red: 0x05 / 255.0, green: 0x20 / 255.0, blue: 0x18 / 255.0)
    static let emeraldPrimary = Color(red: 0x0A / 255.0, green: 0x3D / 255.0, blue: 0x2E / 255.0)
    static let emeraldSurface = Color(red: 0x10 / 255.0, green: 0x4E / 255.0, blue: 0x3B / 255.0)

    static let goldPrimary = Color(red: 0xD4 / 255.0, green: 0xAF / 255.0, blue: 0x37 / 255.0)
    static let goldLight = Color(red: 0xF3 / 255.0, green: 0xE5 / 255.0, blue: 0xAB / 255.0)
    static let goldBorder = Color(red: 0xC5 / 255.0, green: 0xA0 / 255.0, blue: 0x59 / 255.0)

    static let parchmentOuter = Color(red: 0xFD / 255.0, green: 0xFB / 255.0, blue: 0xF5 / 255.0)
    static let parchmentInner = Color(red: 0xF5 / 255.0, green: 0xEF / 255.0, blue: 0xE2 / 255.0)
    static let inkDark = Color(red: 0x1A / 255.0, green: 0x24 / 255.0, blue: 0x21 / 255.0)

    // Tajweed Highlight Palette (matching Android TajweedEngine)
    static let tajweedGhunnah = Color(red: 0x1B / 255.0, green: 0x8A / 255.0, blue: 0x5A / 255.0)
    static let tajweedQalqalah = Color(red: 0x15 / 255.0, green: 0x65 / 255.0, blue: 0xC0 / 255.0)
    static let tajweedMadd = Color(red: 0xC6 / 255.0, green: 0x28 / 255.0, blue: 0x28 / 255.0)
    static let tajweedIkhfa = Color(red: 0xD9 / 255.0, green: 0x77 / 255.0, blue: 0x06 / 255.0)

    static func arabicFont(size: CGFloat) -> Font {
        .custom(amiriFontName, size: size)
    }

    static func quranFont(size: CGFloat) -> Font {
        .custom(scheherazadeFontName, size: size)
    }
}
