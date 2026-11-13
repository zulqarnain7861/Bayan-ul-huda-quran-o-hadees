import XCTest
@testable import BayanAlHuda

final class BayanAlHudaParityTests: XCTestCase {

    func testNooraniQaidaJSONHas53PagesAndBookNumbers() {
        let pages = NooraniQaidaCatalog.loadPagesFromBundle()
        XCTAssertEqual(pages.count, 53, "Noorani Qaida JSON must contain all 53 pages")
        XCTAssertEqual(pages.first?.pageNumber, 1)
        XCTAssertEqual(pages.last?.pageNumber, 53)
        XCTAssertEqual(pages.last?.bookPageNumber, 56, "PDF page 53 corresponds to printed top book page 56")
    }

    func testNooraniQaidaExercisesCoverBasicIntermediateAndHardTiers() {
        let basic = NooraniQaidaCatalog.exercises(for: .basic)
        let intermediate = NooraniQaidaCatalog.exercises(for: .intermediate)
        let hard = NooraniQaidaCatalog.exercises(for: .hard)

        XCTAssertGreaterThanOrEqual(basic.count, 10)
        XCTAssertGreaterThanOrEqual(intermediate.count, 10)
        XCTAssertGreaterThanOrEqual(hard.count, 10)
        XCTAssertEqual(NooraniQaidaCatalog.allExercises.count, 35)
    }

    func testQuranReciterCatalogHas280RecitersWithStableIds() {
        let reciters = QuranReaderView.catalogReciters
        XCTAssertEqual(reciters.count, 280)
        XCTAssertTrue(reciters.contains(where: { $0.id == "mishary_rashid_alafasy" }))
        XCTAssertTrue(reciters.contains(where: { $0.id == "islam_sobhi" }))
    }
}
