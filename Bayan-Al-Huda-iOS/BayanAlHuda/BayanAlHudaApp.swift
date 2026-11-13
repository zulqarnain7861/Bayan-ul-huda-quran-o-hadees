import SwiftUI

@main
struct BayanAlHudaApp: App {
    @StateObject private var persistenceController = PersistenceController.shared
    @AppStorage("app_color_scheme") private var appColorScheme: String = "system"

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(\.managedObjectContext, persistenceController.container.viewContext)
                .preferredColorScheme(resolvedColorScheme)
        }
    }

    private var resolvedColorScheme: ColorScheme? {
        switch appColorScheme {
        case "light": return .light
        case "dark": return .dark
        default: return nil
        }
    }
}
