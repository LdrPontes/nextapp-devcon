//
//  MigrationBridge.swift
//  devcon
//

import Foundation
import Combine
import SwiftUI
import devconrnframework

final class MigrationBridge: ObservableObject {
    static let shared = MigrationBridge()

    private static let migratedKey = "mmkv_migrated_username"

    @Published var migratedUsername: String? =
        UserDefaults.standard.string(forKey: MigrationBridge.migratedKey)

    private var observer: NSObjectProtocol?

    private init() {}

    func start() {
        guard observer == nil else { return }
        observer = ReactNativeBrownfield.shared.onMessage { [weak self] raw in
            guard let self else { return }
            guard let data = raw.data(using: .utf8),
                  let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                  let type = json["type"] as? String else { return }

            switch type {
            case "getNativeValue":
                let requestId = json["requestId"] as? String ?? ""
                let key = json["key"] as? String ?? ""
                let value = UserDefaults.standard.string(forKey: key)
                self.sendResponse(requestId: requestId, value: value)
            case "migrationDone":
                let value = json["value"] as? String
                UserDefaults.standard.set(value, forKey: MigrationBridge.migratedKey)
                DispatchQueue.main.async {
                    self.migratedUsername = value
                }
            default:
                break
            }
        }
    }

    private func sendResponse(requestId: String, value: String?) {
        var payload: [String: Any] = [
            "type": "nativeValueResponse",
            "requestId": requestId,
        ]
        if let value { payload["value"] = value } else { payload["value"] = NSNull() }

        guard let data = try? JSONSerialization.data(withJSONObject: payload),
              let json = String(data: data, encoding: .utf8) else { return }

        ReactNativeBrownfield.shared.postMessage(json)
    }
}
