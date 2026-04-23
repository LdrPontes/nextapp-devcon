//
//  ReactNativeScreen.swift
//  devcon
//

import SwiftUI
import devconrnframework

struct ReactNativeScreen: View {
    var body: some View {
        ReactNativeView(moduleName: "devconrn")
            .ignoresSafeArea()
            .navigationTitle("React Native")
            .navigationBarTitleDisplayMode(.inline)
    }
}
