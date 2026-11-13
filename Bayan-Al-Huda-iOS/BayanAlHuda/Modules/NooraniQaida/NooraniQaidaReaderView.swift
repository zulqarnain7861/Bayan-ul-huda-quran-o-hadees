import SwiftUI

struct NooraniQaidaReaderView: View {
    @AppStorage("last_read_noorani_qaida_page") private var currentPageNumber: Int = 1
    @State private var pages: [NooraniQaidaPageEntry] = NooraniQaidaCatalog.loadPagesFromBundle()
    @State private var currentZoomScale: CGFloat = 1.0
    @State private var showLessonIndexSheet: Bool = false
    @State private var showExerciseQuizSheet: Bool = false
    @State private var showArabicTranscriptSheet: Bool = false
    @State private var searchQuery: String = ""

    private var totalPages: Int {
        max(pages.count, 53)
    }

    private var currentEntry: NooraniQaidaPageEntry? {
        pages.first(where: { $0.pageNumber == currentPageNumber })
    }

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [IslamicTheme.emeraldDeep, IslamicTheme.emeraldPrimary],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 10) {
                // Decorative Lesson Header Card
                lessonHeaderBanner

                // Horizontal Swipeable Pager (equivalent to Android ViewPager2 / HorizontalPager)
                TabView(selection: $currentPageNumber) {
                    ForEach(1...totalPages, id: \.self) { pageNum in
                        NooraniQaidaPDFKitView(
                            pageNumber: pageNum,
                            zoomScale: Binding(
                                get: { pageNum == currentPageNumber ? currentZoomScale : 1.0 },
                                set: { newVal in
                                    if pageNum == currentPageNumber {
                                        currentZoomScale = newVal
                                    }
                                }
                            )
                        )
                        .islamicIlluminatedCard(cornerRadius: 22, shadowRadius: 10)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 4)
                        .tag(pageNum)
                        .accessibilityLabel(accessibilityDescription(for: pageNum))
                    }
                }
                .tabViewStyle(PageTabViewStyle(indexDisplayMode: .never))
                .environment(\.layoutDirection, .rightToLeft) // RTL Mushaf/Qaida page swipe direction

                // Bottom Synchronized Navigation & Page Number Dock
                bottomControlBar
            }
            .padding(.bottom, 6)
        }
        .navigationTitle("Noorani Qaida • القاعدة النورانية")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItemGroup(placement: .navigationBarTrailing) {
                Button {
                    showArabicTranscriptSheet = true
                } label: {
                    Image(systemName: "text.magnifyingglass")
                }
                .accessibilityLabel("Search & Selectable Arabic Text")

                Button {
                    showLessonIndexSheet = true
                } label: {
                    Image(systemName: "square.grid.2x2.fill")
                }
                .accessibilityLabel("Open Noorani Qaida Lesson & Page Index")

                Button {
                    showExerciseQuizSheet = true
                } label: {
                    Image(systemName: "checkmark.seal.fill")
                }
                .accessibilityLabel("Open Basic, Intermediate, and Hard Exercise Quizzes")
            }
        }
        .sheet(isPresented: $showLessonIndexSheet) {
            lessonIndexSheetView
        }
        .sheet(isPresented: $showExerciseQuizSheet) {
            NooraniQaidaExerciseSheet(
                onJumpToPage: { targetPage in
                    currentPageNumber = min(max(targetPage, 1), totalPages)
                    showExerciseQuizSheet = false
                }
            )
        }
        .sheet(isPresented: $showArabicTranscriptSheet) {
            selectableArabicTextSheet
        }
    }

    private var lessonHeaderBanner: some View {
        HStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 3) {
                Text(currentEntry?.lessonTitleEn ?? "Lesson • Noorani Qaida")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(.white)
                    .lineLimit(1)
                Text(currentEntry?.lessonSubtitle ?? "Pinch or double-tap any page to zoom in HD")
                    .font(.system(size: 12))
                    .foregroundColor(IslamicTheme.goldLight)
                    .lineLimit(1)
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 2) {
                Text(currentEntry?.lessonTitleAr ?? "القاعدة النورانية")
                    .font(IslamicTheme.arabicFont(size: 19))
                    .foregroundColor(IslamicTheme.goldPrimary)
                    .environment(\.layoutDirection, .rightToLeft)
                if let bookPage = currentEntry?.bookPageNumber {
                    Text("Top Book No: \(bookPage)")
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundColor(IslamicTheme.goldLight)
                }
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 10)
        .background(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .fill(IslamicTheme.emeraldSurface)
                .overlay(
                    RoundedRectangle(cornerRadius: 16, style: .continuous)
                        .strokeBorder(IslamicTheme.goldPrimary.opacity(0.45), lineWidth: 1)
                )
        )
        .padding(.horizontal, 12)
    }

    private var bottomControlBar: some View {
        HStack(spacing: 10) {
            Button {
                if currentPageNumber > 1 {
                    withAnimation {
                        currentPageNumber -= 1
                        currentZoomScale = 1.0
                    }
                }
            } label: {
                HStack(spacing: 4) {
                    Image(systemName: "chevron.left")
                    Text("Prev")
                        .font(.system(size: 13, weight: .semibold))
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(IslamicTheme.emeraldSurface)
                .foregroundColor(.white)
                .cornerRadius(10)
            }
            .disabled(currentPageNumber <= 1)
            .accessibilityLabel("Previous Page")

            Spacer()

            Button {
                showLessonIndexSheet = true
            } label: {
                VStack(spacing: 2) {
                    Text("Page \(currentPageNumber) of \(totalPages)")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(IslamicTheme.goldLight)
                    if let bookNo = currentEntry?.bookPageNumber {
                        Text("Book Top Page \(bookNo) • Tap for Index")
                            .font(.system(size: 10))
                            .foregroundColor(.white.opacity(0.8))
                    }
                }
            }
            .accessibilityLabel("Page \(currentPageNumber) of \(totalPages). Tap to open lesson index.")

            Spacer()

            Button {
                currentZoomScale = currentZoomScale > 1.2 ? 1.0 : 2.25
            } label: {
                Text(currentZoomScale > 1.2 ? "1x" : "2.2x")
                    .font(.system(size: 12, weight: .bold))
                    .padding(.horizontal, 9)
                    .padding(.vertical, 8)
                    .background(IslamicTheme.goldPrimary.opacity(0.22))
                    .foregroundColor(IslamicTheme.goldLight)
                    .cornerRadius(8)
            }
            .accessibilityLabel("Toggle Page Zoom")

            Button {
                if currentPageNumber < totalPages {
                    withAnimation {
                        currentPageNumber += 1
                        currentZoomScale = 1.0
                    }
                }
            } label: {
                HStack(spacing: 4) {
                    Text("Next")
                        .font(.system(size: 13, weight: .semibold))
                    Image(systemName: "chevron.right")
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(IslamicTheme.goldPrimary)
                .foregroundColor(IslamicTheme.emeraldDeep)
                .cornerRadius(10)
            }
            .disabled(currentPageNumber >= totalPages)
            .accessibilityLabel("Next Page")
        }
        .padding(.horizontal, 14)
    }

    private var lessonIndexSheetView: some View {
        NavigationView {
            VStack(spacing: 10) {
                TextField("Search lesson, page number, or Arabic letters...", text: $searchQuery)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    .padding(.horizontal)

                List(filteredPages) { entry in
                    Button {
                        currentPageNumber = entry.pageNumber
                        currentZoomScale = 1.0
                        showLessonIndexSheet = false
                    } label: {
                        HStack(spacing: 12) {
                            VStack(spacing: 2) {
                                Text("P.\(entry.pageNumber)")
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundColor(.white)
                                Text("Top \(entry.bookPageNumber)")
                                    .font(.system(size: 10, weight: .medium))
                                    .foregroundColor(IslamicTheme.goldLight)
                            }
                            .frame(width: 56, height: 46)
                            .background(
                                entry.pageNumber == currentPageNumber
                                    ? IslamicTheme.goldPrimary
                                    : IslamicTheme.emeraldPrimary
                            )
                            .cornerRadius(10)

                            VStack(alignment: .leading, spacing: 3) {
                                Text(entry.lessonTitleEn)
                                    .font(.system(size: 14, weight: .semibold))
                                    .foregroundColor(.primary)
                                Text(entry.lessonSubtitle)
                                    .font(.system(size: 12))
                                    .foregroundColor(.secondary)
                            }

                            Spacer()

                            Text(entry.lessonTitleAr)
                                .font(IslamicTheme.arabicFont(size: 18))
                                .foregroundColor(IslamicTheme.emeraldPrimary)
                                .environment(\.layoutDirection, .rightToLeft)
                        }
                        .padding(.vertical, 4)
                    }
                }
            }
            .navigationTitle("Noorani Qaida Index (53 Pages)")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { showLessonIndexSheet = false }
                }
            }
        }
    }

    private var selectableArabicTextSheet: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .trailing, spacing: 14) {
                    if let entry = currentEntry {
                        HStack {
                            Text("PDF Page \(entry.pageNumber) • Printed Top Page \(entry.bookPageNumber)")
                                .font(.caption.bold())
                                .foregroundColor(.secondary)
                            Spacer()
                            Text(entry.lessonTitleAr)
                                .font(IslamicTheme.arabicFont(size: 22))
                                .foregroundColor(IslamicTheme.emeraldPrimary)
                        }

                        Text(entry.arabicText)
                            .font(IslamicTheme.arabicFont(size: 28))
                            .multilineTextAlignment(.trailing)
                            .environment(\.layoutDirection, .rightToLeft)
                            .textSelection(.enabled)
                            .padding(16)
                            .frame(maxWidth: .infinity, alignment: .trailing)
                            .islamicIlluminatedCard(cornerRadius: 18, shadowRadius: 6)
                    }
                }
                .padding()
            }
            .navigationTitle("Selectable Arabic Text (RTL)")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { showArabicTranscriptSheet = false }
                }
            }
        }
    }

    private var filteredPages: [NooraniQaidaPageEntry] {
        let q = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        guard !q.isEmpty else { return pages }
        return pages.filter { entry in
            "\(entry.pageNumber)".contains(q)
                || "\(entry.bookPageNumber)".contains(q)
                || entry.lessonTitleEn.lowercased().contains(q)
                || entry.lessonTitleAr.contains(q)
                || entry.arabicText.contains(q)
                || entry.searchKeywords.lowercased().contains(q)
        }
    }

    private func accessibilityDescription(for pageNum: Int) -> String {
        if let entry = pages.first(where: { $0.pageNumber == pageNum }) {
            return "Noorani Qaida Page \(entry.pageNumber) of \(totalPages), Book Top Number \(entry.bookPageNumber). \(entry.lessonTitleEn). \(entry.lessonTitleAr). \(entry.arabicText)"
        }
        return "Noorani Qaida Page \(pageNum) of \(totalPages)"
    }
}

struct NooraniQaidaExerciseSheet: View {
    let onJumpToPage: (Int) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var selectedTier: QaidaQuizTier = .basic
    @State private var selectedAnswers: [String: Int] = [:]

    private var questions: [QaidaExerciseMcq] {
        NooraniQaidaCatalog.exercises(for: selectedTier)
    }

    private var answeredCount: Int {
        questions.filter { selectedAnswers[$0.id] != nil }.count
    }

    private var correctCount: Int {
        questions.filter { selectedAnswers[$0.id] == $0.correctIndex }.count
    }

    var body: some View {
        NavigationView {
            VStack(spacing: 12) {
                Picker("Difficulty Tier", selection: $selectedTier) {
                    ForEach(QaidaQuizTier.allCases) { tier in
                        Text(tier.rawValue).tag(tier)
                    }
                }
                .pickerStyle(SegmentedPickerStyle())
                .padding(.horizontal)

                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(selectedTier.subtitle)
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Text("Score: \(correctCount) / \(questions.count) (\(answeredCount) answered)")
                            .font(.subheadline.bold())
                            .foregroundColor(IslamicTheme.emeraldPrimary)
                    }
                    Spacer()
                    Button("Reset Tier") {
                        for q in questions {
                            selectedAnswers.removeValue(forKey: q.id)
                        }
                    }
                    .font(.caption.bold())
                }
                .padding(.horizontal)

                ScrollView {
                    LazyVStack(spacing: 14) {
                        ForEach(Array(questions.enumerated()), id: \.element.id) { index, mcq in
                            VStack(alignment: .leading, spacing: 10) {
                                HStack {
                                    Text("Q\(index + 1) • Lesson \(mcq.relatedLesson)")
                                        .font(.caption.bold())
                                        .padding(.horizontal, 8)
                                        .padding(.vertical, 4)
                                        .background(IslamicTheme.emeraldPrimary.opacity(0.12))
                                        .cornerRadius(6)
                                    Spacer()
                                    Button("Review Page \(mcq.relatedPageNumber)") {
                                        onJumpToPage(mcq.relatedPageNumber)
                                    }
                                    .font(.caption.bold())
                                }

                                Text(mcq.arabicSample)
                                    .font(IslamicTheme.arabicFont(size: 26))
                                    .frame(maxWidth: .infinity, alignment: .trailing)
                                    .environment(\.layoutDirection, .rightToLeft)
                                    .padding(10)
                                    .background(IslamicTheme.parchmentInner)
                                    .cornerRadius(10)

                                Text(mcq.question)
                                    .font(.system(size: 15, weight: .semibold))

                                ForEach(mcq.options.indices, id: \.self) { optIdx in
                                    let chosen = selectedAnswers[mcq.id]
                                    let isSelected = chosen == optIdx
                                    let isCorrect = optIdx == mcq.correctIndex

                                    Button {
                                        selectedAnswers[mcq.id] = optIdx
                                    } label: {
                                        HStack {
                                            Text(mcq.options[optIdx])
                                                .font(.system(size: 14))
                                                .multilineTextAlignment(.leading)
                                            Spacer()
                                            if chosen != nil {
                                                if isCorrect {
                                                    Image(systemName: "checkmark.circle.fill")
                                                        .foregroundColor(.green)
                                                } else if isSelected {
                                                    Image(systemName: "xmark.circle.fill")
                                                        .foregroundColor(.red)
                                                }
                                            }
                                        }
                                        .padding(10)
                                        .background(
                                            RoundedRectangle(cornerRadius: 10)
                                                .strokeBorder(
                                                    chosen != nil && isCorrect
                                                        ? Color.green
                                                        : (isSelected ? Color.red : Color.secondary.opacity(0.3)),
                                                    lineWidth: 1.3
                                                )
                                        )
                                    }
                                    .buttonStyle(PlainButtonStyle())
                                }

                                if selectedAnswers[mcq.id] != nil {
                                    Text(mcq.explanation)
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                        .padding(8)
                                        .background(Color.secondary.opacity(0.08))
                                        .cornerRadius(8)
                                }
                            }
                            .padding(14)
                            .background(
                                RoundedRectangle(cornerRadius: 16)
                                    .fill(Color(UIColor.secondarySystemBackground))
                            )
                        }
                    }
                    .padding()
                }
            }
            .navigationTitle("Qaida Exercises & Tests")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { dismiss() }
                }
            }
        }
    }
}
