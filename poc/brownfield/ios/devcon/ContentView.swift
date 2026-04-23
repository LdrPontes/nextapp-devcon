//
//  ContentView.swift
//  devcon
//
//  Created by Leandro Pontes Berleze on 22/04/26.
//

import SwiftUI

struct ContentView: View {
    @State private var inputText: String = ""
    @State private var savedValue: String = UserDefaults.standard.string(forKey: "username") ?? ""
    @ObservedObject private var migration = MigrationBridge.shared

    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                Text("Native iOS Storage")
                    .font(.title2)
                    .fontWeight(.semibold)

                TextField("Enter your username", text: $inputText)
                    .textFieldStyle(.roundedBorder)
                    .padding(.horizontal)

                Button("Save") {
                    UserDefaults.standard.set(inputText, forKey: "username")
                    savedValue = inputText
                    inputText = ""
                }
                .buttonStyle(.borderedProminent)
                .disabled(inputText.isEmpty)

                if !savedValue.isEmpty {
                    Text("Stored: \(savedValue)")
                        .foregroundStyle(.secondary)
                }

                if let migrated = migration.migratedUsername {
                    Text("MMKV (migrated): \(migrated)")
                        .foregroundStyle(.green)
                }

                NavigationLink {
                    ReactNativeScreen()
                } label: {
                    Text("Open React Native screen")
                }
                .buttonStyle(.bordered)
            }
            .padding()
        }
    }
}

#Preview {
    ContentView()
}
