import PDFKit
import SwiftUI
import UIKit

/// Native iOS PDFKit page renderer wrapped in a UIScrollView supporting:
/// - High-DPI vector PDF page rendering directly from Bundle's `noorani_qaida.pdf`
/// - Smooth pinch-to-zoom (1.0x to 4.0x) and pan
/// - Double-tap gesture to toggle 2.5x zoom vs 1.0x fit
struct NooraniQaidaPDFKitView: UIViewRepresentable {
    let pageNumber: Int // 1-based (1..53)
    @Binding var zoomScale: CGFloat

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    func makeUIView(context: Context) -> UIScrollView {
        let scrollView = UIScrollView()
        scrollView.delegate = context.coordinator
        scrollView.minimumZoomScale = 1.0
        scrollView.maximumZoomScale = 4.0
        scrollView.bouncesZoom = true
        scrollView.showsHorizontalScrollIndicator = false
        scrollView.showsVerticalScrollIndicator = false
        scrollView.backgroundColor = .clear

        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        imageView.backgroundColor = .white
        imageView.layer.cornerRadius = 14
        imageView.layer.masksToBounds = true
        imageView.translatesAutoresizingMaskIntoConstraints = false
        scrollView.addSubview(imageView)

        NSLayoutConstraint.activate([
            imageView.leadingAnchor.constraint(equalTo: scrollView.contentLayoutGuide.leadingAnchor),
            imageView.trailingAnchor.constraint(equalTo: scrollView.contentLayoutGuide.trailingAnchor),
            imageView.topAnchor.constraint(equalTo: scrollView.contentLayoutGuide.topAnchor),
            imageView.bottomAnchor.constraint(equalTo: scrollView.contentLayoutGuide.bottomAnchor),
            imageView.widthAnchor.constraint(equalTo: scrollView.frameLayoutGuide.widthAnchor),
            imageView.heightAnchor.constraint(equalTo: scrollView.frameLayoutGuide.heightAnchor)
        ])

        let doubleTap = UITapGestureRecognizer(
            target: context.coordinator,
            action: #selector(Coordinator.handleDoubleTap(_:))
        )
        doubleTap.numberOfTapsRequired = 2
        scrollView.addGestureRecognizer(doubleTap)

        context.coordinator.scrollView = scrollView
        context.coordinator.imageView = imageView
        context.coordinator.renderPage(pageNumber: pageNumber)
        return scrollView
    }

    func updateUIView(_ scrollView: UIScrollView, context: Context) {
        if context.coordinator.renderedPageNumber != pageNumber {
            scrollView.setZoomScale(1.0, animated: false)
            context.coordinator.renderPage(pageNumber: pageNumber)
        }
        if abs(scrollView.zoomScale - zoomScale) > 0.05 {
            scrollView.setZoomScale(min(max(zoomScale, 1.0), 4.0), animated: true)
        }
    }

    final class Coordinator: NSObject, UIScrollViewDelegate {
        var parent: NooraniQaidaPDFKitView
        weak var scrollView: UIScrollView?
        weak var imageView: UIImageView?
        var renderedPageNumber: Int = -1
        private static let sharedDocument: PDFDocument? = {
            if let url = Bundle.main.url(forResource: "noorani_qaida", withExtension: "pdf") {
                return PDFDocument(url: url)
            }
            let fallbackURL = URL(fileURLWithPath: "BayanAlHuda/Resources/noorani_qaida.pdf")
            return PDFDocument(url: fallbackURL)
        }()
        private static let renderCache = NSCache<NSNumber, UIImage>()

        init(_ parent: NooraniQaidaPDFKitView) {
            self.parent = parent
        }

        func viewForZooming(in scrollView: UIScrollView) -> UIView? {
            imageView
        }

        func scrollViewDidZoom(_ scrollView: UIScrollView) {
            DispatchQueue.main.async {
                self.parent.zoomScale = scrollView.zoomScale
            }
        }

        @objc func handleDoubleTap(_ recognizer: UITapGestureRecognizer) {
            guard let scrollView = scrollView else { return }
            let targetScale: CGFloat = scrollView.zoomScale > 1.2 ? 1.0 : 2.5
            scrollView.setZoomScale(targetScale, animated: true)
            DispatchQueue.main.async {
                self.parent.zoomScale = targetScale
            }
        }

        func renderPage(pageNumber: Int) {
            renderedPageNumber = pageNumber
            let key = NSNumber(value: pageNumber)
            if let cached = Self.renderCache.object(forKey: key) {
                imageView?.image = cached
                return
            }

            DispatchQueue.global(qos: .userInitiated).async { [weak self] in
                guard let document = Self.sharedDocument,
                      pageNumber >= 1,
                      pageNumber <= document.pageCount,
                      let pdfPage = document.page(at: pageNumber - 1) else {
                    return
                }

                let pageBounds = pdfPage.bounds(for: .mediaBox)
                let targetSize = CGSize(width: 1400, height: max(1400 * (pageBounds.height / max(pageBounds.width, 1)), 1800))
                let renderer = UIGraphicsImageRenderer(size: targetSize)
                let image = renderer.image { ctx in
                    UIColor.white.setFill()
                    ctx.fill(CGRect(origin: .zero, size: targetSize))
                    ctx.cgContext.saveGState()
                    ctx.cgContext.translateBy(x: 0, y: targetSize.height)
                    ctx.cgContext.scaleBy(
                        x: targetSize.width / max(pageBounds.width, 1),
                        y: -targetSize.height / max(pageBounds.height, 1)
                    )
                    pdfPage.draw(with: .mediaBox, to: ctx.cgContext)
                    ctx.cgContext.restoreGState()
                }
                Self.renderCache.setObject(image, forKey: key)

                DispatchQueue.main.async {
                    if self?.renderedPageNumber == pageNumber {
                        self?.imageView?.image = image
                    }
                }
            }
        }
    }
}
