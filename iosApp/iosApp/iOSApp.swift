import SwiftUI
import ComposeApp
import FirebaseCore
import FirebaseAnalytics
import Foundation

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        IosAnalyticsBridge.shared.setLogger { name, parametersJson in
            let parameters = Self.decodeAnalyticsParameters(parametersJson)
            Analytics.logEvent(name, parameters: parameters)
        }
        KoinHelperKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }

    private static func decodeAnalyticsParameters(_ parametersJson: String) -> [String: Any] {
        guard let data = parametersJson.data(using: .utf8),
              let object = try? JSONSerialization.jsonObject(with: data),
              let parameters = object as? [String: Any] else {
            return [:]
        }
        return parameters
    }
}
