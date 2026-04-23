//
//  devconApp.swift
//  devcon
//
//  Created by Leandro Pontes Berleze on 22/04/26.
//

import SwiftUI
import UIKit
import devconrnframework

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        ReactNativeBrownfield.shared.bundle = ReactNativeBundle
        ReactNativeBrownfield.shared.startReactNative(onBundleLoaded: {
            print("React Native bundle loaded")
        })
        MigrationBridge.shared.start()
        return true
    }
}

@main
struct devconApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
