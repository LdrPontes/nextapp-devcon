# nextapp-devcon

A demonstration project showing how to migrate native mobile app storage to React Native using [MMKV](https://github.com/mrousavy/react-native-mmkv).

## Overview

This project is the base for two hands-on articles about migrating user data from native mobile storage to a cross-platform React Native solution. It is a follow-up to the original article: [How to Migrate User Data from Native to Cross-Platform Apps](https://cheesecakelabs.com/blog/how-to-migrate-user-data-from-native-to-cross-platform-apps/).

**Article 1 — Brownfield:** uses [react-native-brownfield](https://github.com/callstack/react-native-brownfield) to embed React Native incrementally inside the existing native apps.

**Article 2 — Greenfield:** migrates to a brand-new React Native app while preserving data stored in the native apps.

## Project Structure

```
.
├── android/        # Native Android app (SharedPreferences)
├── ios/            # Native iOS app (Keychain / UserDefaults)
├── rn/             # React Native layer (MMKV)
├── poc/ 
│   ├──             # Brownfield POC ios, android, and rn
│   └──             # Greenfield POC ios, android, and rn
└── article/
│   ├── brownfield.md  # Article 1 draft — brownfield approach
│   └── greenfield.md  # Article 2 draft — greenfield approach
```

## Migration Path

| Platform | Native Storage       | Target Storage |
|----------|----------------------|----------------|
| iOS      | Keychain / UserDefaults | MMKV (React Native) |
| Android  | SharedPreferences    | MMKV (React Native) |

## How It Works

Both articles start from the same native baseline (iOS + Android), then diverge:

**Brownfield**
1. Embed React Native into the existing native apps via `react-native-brownfield`.
2. Migrate stored data from native APIs to MMKV without replacing the native shell.

**Greenfield**
1. Create a fresh React Native app from scratch.
2. On first launch, read data from native storage and write it to MMKV.

## References

- [react-native-brownfield](https://github.com/callstack/react-native-brownfield)
- [react-native-mmkv](https://github.com/mrousavy/react-native-mmkv)
- [Original article — Cheesecake Labs](https://cheesecakelabs.com/blog/how-to-migrate-user-data-from-native-to-cross-platform-apps/)
