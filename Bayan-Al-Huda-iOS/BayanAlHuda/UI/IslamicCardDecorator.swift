import SwiftUI

/// Equivalent to Android's IlluminatedPageFrame + ElevatedCard (rounded corners, subtle Islamic radial gradient,
/// golden ornamental border, corner floral medallions, and soft elevation shadow).
struct IslamicCardDecorator: ViewModifier {
    var cornerRadius: CGFloat = 22
    var shadowRadius: CGFloat = 10
    var showCornerMedallions: Bool = true

    func body(content: Content) -> some View {
        ZStack {
            RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                .fill(
                    RadialGradient(
                        gradient: Gradient(colors: [
                            IslamicTheme.parchmentOuter,
                            IslamicTheme.parchmentInner,
                            Color(red: 0xEB / 255.0, green: 0xE1 / 255.0, blue: 0xCC / 255.0)
                        ]),
                        center: .center,
                        startRadius: 24,
                        endRadius: 560
                    )
                )
                .shadow(color: Color.black.opacity(0.24), radius: shadowRadius, x: 0, y: 5)

            // Outer Gold Frame
            RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                .strokeBorder(IslamicTheme.goldPrimary.opacity(0.65), lineWidth: 1.5)

            // Inner Subtle Arch Frame
            RoundedRectangle(cornerRadius: max(cornerRadius - 6, 8), style: .continuous)
                .strokeBorder(IslamicTheme.emeraldPrimary.opacity(0.18), lineWidth: 1.0)
                .padding(6)

            if showCornerMedallions {
                GeometryReader { proxy in
                    let w = proxy.size.width
                    let h = proxy.size.height
                    Path { path in
                        let offsets: [(CGFloat, CGFloat)] = [
                            (16, 16),
                            (w - 16, 16),
                            (16, h - 16),
                            (w - 16, h - 16)
                        ]
                        for (cx, cy) in offsets {
                            path.addEllipse(in: CGRect(x: cx - 5, y: cy - 5, width: 10, height: 10))
                        }
                    }
                    .stroke(IslamicTheme.goldPrimary.opacity(0.55), lineWidth: 1.2)
                }
                .allowsHitTesting(false)
            }

            content
                .padding(10)
        }
    }
}

extension View {
    func islamicIlluminatedCard(
        cornerRadius: CGFloat = 22,
        shadowRadius: CGFloat = 10,
        showCornerMedallions: Bool = true
    ) -> some View {
        modifier(
            IslamicCardDecorator(
                cornerRadius: cornerRadius,
                shadowRadius: shadowRadius,
                showCornerMedallions: showCornerMedallions
            )
        )
    }
}
