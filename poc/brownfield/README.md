# Brownfield POC — Manual Setup Steps

This POC embeds the React Native app at `react-native/` into the native hosts at `ios/` and `android/` using [`@callstack/react-native-brownfield`](https://github.com/callstack/react-native-brownfield).

- RN app path: `poc/brownfield/react-native`
- Registered JS module name (from `app.json`): `devconrn`
- Package manager: `npm`

> Note: `npx brownfield package:*` auto-runs `expo prebuild` when Expo is detected. This RN app is bare (no `expo` dependency), so nothing needs to be stripped.

## Already done automatically

- Installed `@callstack/react-native-brownfield` in `react-native/`.
- Ran `bundle exec pod install` in `react-native/ios/` (ReactBrownfield pod integrated).
- Created Android library module `react-native/android/devconrn-rn/` with `build.gradle.kts`, empty `AndroidManifest.xml`, `ReactNativeHostManager.kt` facade, and consumer/proguard rules files.
- Registered `:devconrn-rn` in `react-native/android/settings.gradle`.
- Created `react-native/ios/DevconRN/DevconRN.swift` on disk (still needs to be added to the Xcode framework target — see iOS step 1).
- Wired `poc/brownfield/android` host app: added `mavenLocal()` to `settings.gradle.kts`, `dev.ldrpontes:devconrn-rn:0.1.0` dep in `app/build.gradle.kts`, created `DevconApplication` that calls `ReactNativeHostManager.initialize`, created `ReactNativeActivity` hosting `ReactNativeFragment.createReactNativeFragment("devconrn")`, registered both in the manifest, and added an "Open React Native screen" button in `MainActivity`.

## iOS — manual steps

Work inside `poc/brownfield/react-native/ios/devconrn.xcworkspace`.

### 1. Create a framework target

1. In Xcode: **File → New → Target → Framework**. Name it `DevconRN`.
2. If `devconrn`'s folders appear as blue folder references, convert them to yellow groups (right-click → *Convert to Group*).
3. Drag the existing `ios/DevconRN/DevconRN.swift` into the new target (uncheck "copy items if needed", check target membership = `DevconRN`).

### 2. Update the Podfile

Nest the framework target inside the app target so it inherits all RN pods:

```ruby
target 'devconrn' do
  # ... existing react_native_config ...

  target 'DevconRN' do
    inherit! :complete
  end
end
```

Then re-run pods:

```bash
cd poc/brownfield/react-native/ios
bundle exec pod install
```

### 3. Framework target build settings

Select the `DevconRN` target → *Build Settings*:

- `Build Libraries for Distribution` = **YES**
- `User Script Sandboxing` = **NO**
- `Skip Install` = **NO**
- `Enable Module Verifier` = **NO**

### 4. Add the bundle script phase

On the `DevconRN` target → *Build Phases* → **+ → New Run Script Phase**, name it `Bundle React Native code and images`, and paste:

```bash
set -e
WITH_ENVIRONMENT="$REACT_NATIVE_PATH/scripts/xcode/with-environment.sh"
REACT_NATIVE_XCODE="$REACT_NATIVE_PATH/scripts/react-native-xcode.sh"
/bin/sh -c "$WITH_ENVIRONMENT $REACT_NATIVE_XCODE"
```

Add Input Files:

```
$(SRCROOT)/.xcode.env.local
$(SRCROOT)/.xcode.env
```

### 5. Package the XCFramework

From `poc/brownfield/react-native`:

```bash
npx brownfield package:ios --scheme DevconRN --configuration Release
```

Validate the output in `ios/.brownfield/package/` (or `.brownfield/ios/package/`):

- `DevconRN.xcframework`
- `ReactBrownfield.xcframework`
- `hermesvm.xcframework`

### 6. Integrate into `poc/brownfield/ios`

1. Drag the three `.xcframework` artifacts into the host app and set *Embed & Sign*.
2. In app startup (AppDelegate / `@main` SwiftUI App):

   ```swift
   import DevconRN

   ReactNativeBrownfield.shared.bundle = ReactNativeBundle
   ReactNativeBrownfield.shared.startReactNative(onBundleLoaded: {
       print("React Native bundle loaded")
   }, launchOptions: launchOptions)
   ```

3. Render the RN screen with `moduleName: "devconrn"`:
   - UIKit: `ReactNativeViewController(moduleName: "devconrn")`
   - SwiftUI: `ReactNativeView(moduleName: "devconrn")`

4. Validate Debug (with `npx react-native start` from `react-native/`) and Release (no Metro).

## Android — manual steps

The library module `devconrn-rn` is already scaffolded. Remaining steps:

### 1. Package and publish the AAR

From `poc/brownfield/react-native`:

```bash
npx brownfield package:android --variant release --module-name devconrn-rn
npx brownfield publish:android --module-name devconrn-rn
```

This publishes `dev.ldrpontes:devconrn-rn:0.1.0` to the local Maven repo (`~/.m2`).

> The `devconrn-rn/build.gradle.kts` includes a `withXml` POM block and a `removeDependenciesFromModuleFile` task that strip autolinked project deps (published as `devconrn:*:unspecified`) from the POM and Gradle `.module` file. Both are copied from the canonical [Android integration guide](https://oss.callstack.com/react-native-brownfield/docs/getting-started/android.md) (sections 3 and 6) — without them the host app fails to resolve the AAR. The same doc drives the Hermes coordinate (`com.facebook.hermes:hermes-android:0.14.1` for RN 0.83.x).

### 2. Validate integration

The host app (`poc/brownfield/android`) is already wired. After the AAR is published, Gradle-sync and run the app:

- Debug: `npx react-native start` from `react-native/`, then run the Android app and tap "Open React Native screen".
- Release: build without Metro and confirm the bundled JS still loads.

## Reference commands

```bash
# Start Metro (from react-native/)
npx react-native start

# Re-package iOS after RN or native changes
npx brownfield package:ios --scheme DevconRN --configuration Release

# Re-package + re-publish Android
npx brownfield package:android --variant release --module-name devconrn-rn
npx brownfield publish:android --module-name devconrn-rn
```

## Canonical docs

- [iOS integration](https://oss.callstack.com/react-native-brownfield/docs/getting-started/ios.md)
- [Android integration](https://oss.callstack.com/react-native-brownfield/docs/getting-started/android.md)
- [Brownfield CLI](https://oss.callstack.com/react-native-brownfield/docs/cli/brownfield.md)
- [Troubleshooting](https://oss.callstack.com/react-native-brownfield/docs/guides/troubleshooting.md)
