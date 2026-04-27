# **NextApp Devcon Call for Papers**
## **How to Migrate User Data from a Native to a React Native App**

Migrating a native mobile app to React Native can streamline development and reduce long-term maintenance overhead. The transition involves several key steps to ensure a smooth rollout with minimal disruption to users.

Apps that store preferences, cached content, or authentication tokens locally must preserve that data across the transition. If they don't, the update feels indistinguishable from a fresh install to everyone who already relies on the app — carts vanish, sessions expire, preferences reset — and many users will simply abandon it.

Imagine an e-commerce app with thousands of active users. Each user has a unique profile that may include items in their cart, a wishlist, saved preferences, and payment information. Lose any of that in the rewrite and you've handed those users a reason to walk away.

That's exactly the problem Cheesecake Labs helped Tapcart solve. Tapcart powers thousands of native iOS and Android storefronts, each with their own base of shoppers, carts, and saved sessions. Moving that surface area to React Native meant the migration couldn't be visible to end users — every cart, login, and preference had to survive the cutover on every single one of those apps. The patterns described in this article are the same ones we used to make that transition smooth.

### **Types of storages in Native apps**

Before you can move user data over, you need to know where it lives. Native apps don't have a single storage location — they spread data across several subsystems depending on the kind of data and the platform's conventions. The same logical piece of information, like a user's preferences, often ends up behind completely different APIs on Android and iOS, which is part of what makes migration tricky.

Most apps mix four broad storage categories. Preferences hold small key-value pairs like user settings and feature flags. The file system stores larger or unstructured content such as cached images and user documents. Databases handle structured, queryable data like order history or offline content. And secure storage is reserved for sensitive material — auth tokens, credentials, payment details — backed by hardware-level security.

The table below shows how each category maps to its underlying API on Android and iOS:


| Type | Android (Native) | iOS (Native) |
|------|------------------|---------------|
| Preferences | SharedPreferences | UserDefaults |
| Files | Internal Storage, External Storage | File System |
| Databases | SQLite, Room | Core Data, SQLite |
| Secure data | EncryptedSharedPreferences | Keychain |


### **Greenfield migration**

Once you've mapped out where your data lives, the next challenge is actually moving it. In a greenfield setup, you spin up a brand new React Native project and rebuild the app from scratch, rather than gradually replacing native screens.

That's a cleaner starting point, but it introduces a subtle problem: from the OS's perspective, a brand new project is a brand new app. The requirements below exist to convince the OS otherwise, so the new build takes over the old app's installation slot and inherits its sandboxed storage.

> **Greenfield vs. brownfield, visually**
>
> ```
>   Greenfield:        Brownfield:
>
>   ┌─────────┐        ┌──────────────────┐
>   │ Native  │        │      Native      │
>   │  app    │   →    │  ┌────────────┐  │
>   │ (gone)  │        │  │ React Native│ │
>   └─────────┘        │  └────────────┘  │
>   ┌─────────┐        └──────────────────┘
>   │   RN    │           one app, two
>   │  app    │            runtimes
>   └─────────┘
> ```
> Greenfield replaces the native app entirely. Brownfield embeds a React Native bundle inside it, and both runtimes coexist.

#### **Requirements**
##### **iOS**
- **Bundle identifier**: Keep the exact same Bundle ID (e.g. com.yourcompany.app) as the original native app. iOS uses this as the primary identity of the app; change it and the system treats your build as a brand new app with its own empty sandbox.
- **App Group / Keychain Group**: If the native app used shared storage — for example, credentials in a Keychain access group, or files shared with an extension via an App Group — reuse the same identifiers. Keychain access groups are prefixed with your Apple Developer Team ID, so all builds must belong to the same team for the prefix to match.
- **Provisioning profile and signing team**: Sign the new build with the same Apple Developer Team that shipped the original. A different team produces a different Team ID prefix, which silently breaks Keychain access even when the group name looks identical.

##### **Android**
- **Package name**: Keep the exact same applicationId as the native app. Android uses this as the app's unique identity; if it changes, Play Store and the OS will install your build alongside the old one rather than upgrading it.
- **Signing key (keystore)**: Sign the new APK/AAB with the same upload/signing key the original app used. Android refuses to install an update signed with a different key — it will reject the install entirely or, on sideload, prompt the user to uninstall the existing app first.
- **Play App Signing**: If the original app was enrolled in Play App Signing, the new build must be uploaded with the same upload key registered for that app in the Play Console. The signing key Google holds doesn't change; what matters is that your upload key still matches.

#### **Planning the migration**

With the requirements in place, the next step is taking inventory. Before writing any code, list every piece of data the native app persists: where it's stored, whether it's encrypted, what format it's in, and how critical it is. That list becomes both your migration checklist and your test plan — and it's also where you discover the awkward cases (the legacy key nobody remembers writing, the cache that's safe to drop, the schema that quietly changed two releases ago).

With the inventory in hand, each item gets a strategy. In most cases, the new app can read existing data directly using compatible libraries; only the trickier stores need a native bridge.

For example:

- **Preferences**: The new app can access user preferences stored in `UserDefaults` (iOS) directly using the [React Native Settings](https://reactnative.dev/docs/settings) API. For Android `SharedPreferences`, there's no first-party equivalent, so a community library or a small native module is the usual path.
- **Secure data**: The new app can access secure data stored in the iOS Keychain or Android `EncryptedSharedPreferences` using [`expo-secure-store`](https://docs.expo.dev/versions/latest/sdk/securestore/), which wraps both platforms behind a single API.
- **Native bridging**: In cases where data is encrypted with custom logic, stored in proprietary formats, or you don't find a compatible library, you can create a small native bridge that exposes read/write methods to the cross-platform layer.

In practice, most apps end up combining these approaches rather than picking one. Plain preferences and SQLite databases get read directly from the React Native side, secure values go through a cross-platform Keychain wrapper, and anything stored in a custom or proprietary format gets a one-off native bridge. The result is a migration layer that touches each storage type with the lightest tool that works — and stays out of the way once the first launch is done.


#### **Implementation Example**

The pseudocode below sketches the shape most migrations end up taking on the React Native side. It's deliberately storage-agnostic — `oldStorage` and `newStorage` are stand-ins for whatever you're reading from (a native bridge over `UserDefaults`, a Keychain wrapper, a SQLite handle) and writing into (MMKV, AsyncStorage, a fresh SQLite database).

```typescript
const migrateUserData = async () => {
  // Step 1: Check if migration is needed
  if (!needsMigration()) return;

  // Step 2: Initialize storages
  const oldStorage = getOldStorage();
  const newStorage = getNewStorage();

  // Step 3: Get old data
  const oldData = await oldStorage.getData();

  // Step 4: Transform or map old data to new format
  const mappedData = mapData(oldData);

  // Step 5: Save new data
  await newStorage.setData(mappedData);

  // Step 6: Clean up old storage
  await oldStorage.clear();

  // Step 7: Mark migration as complete
  markMigrationComplete();
```

A few things worth calling out about the flow:
- **It runs once, on first launch**. The `needsMigration()` guard at the top is what makes that true — usually a flag stored in the new storage layer, set at the end of step 7. Skip this and you'll re-run the migration on every cold start, which at best is wasted work and at worst overwrites legitimate user changes with stale data.
- **The mapping step earns its line**. Even when the old and new schemas look identical, you almost always need to massage something — renamed keys, changed types (a string ID that's now a number), nested structures that got flattened, or values that need to be re-encrypted under a new key. Doing this in a dedicated `mapData` step keeps the read and write sides clean.
- **Cleanup is optional but recommended**. Clearing the old storage prevents two sources of truth from drifting apart if the user ever downgrades or if your migration code gets re-triggered by a bug. For sensitive data (Keychain entries, tokens), clearing the source is also a good security hygiene step.
- **What's not shown: error handling**. In production you'll want every step wrapped so that a partial failure doesn't leave the user with half-migrated data and a "migration complete" flag set. The usual pattern is: only mark the migration complete after the new storage write succeeds, and only clear the old storage after that.

### **Brownfield migration**

In a brownfield setup, you don't replace the native app — you embed React Native inside it. The native shell stays alive, the existing screens keep working, and a React Native bundle gets loaded alongside them. Users see a gradual transition, not a big-bang rewrite.

That changes the migration problem. The native and JS sides now run in the same process, but they don't share storage: the native layer still owns whatever it always did (preferences, Keychain entries, SQLite files), while React Native writes into a separate cross-platform store of its choice — MMKV, AsyncStorage, expo-sqlite, or anything else. Any data you want to move has to cross the native ↔ JS boundary explicitly.

#### **Tools that help the migration**

The brownfield model gives you one primitive: a `postMessage` / `onMessage` channel between the native host and the JS bundle. Both [`@callstack/react-native-brownfield`](https://github.com/callstack/react-native-brownfield) and the [Expo Brownfield SDK](https://docs.expo.dev/versions/latest/sdk/brownfield/) expose the same API, so the patterns below apply to either one. JSON goes in, JSON comes out — anything beyond that, you build yourself.

Two message types cover a one-shot migration:

```typescript
type BridgeMessage =
  | { type: 'getNativeValue', requestId: string, key: string }                // RN → Native: read this key
  | { type: 'nativeValueResponse', requestId: string, value: string | null }  // Native → RN: here it is
```

The `requestId` correlates each reply with the request that produced it — the channel is a shared bus, so without it you can't tell which response is yours.

The flow looks like this:

```
   React Native                       Native
   ────────────                      ────────
        │                                │
        │  getNativeValue                │
        │  { requestId: "abc",           │
        │    key: "authToken" }          │
        │ ─────────────────────────────► │
        │                                │  read "authToken"
        │                                │  from native storage
        │                                │
        │  nativeValueResponse           │
        │  { requestId: "abc",           │
        │    value: "..." }              │
        │ ◄───────────────────────────── │
        │                                │
   match requestId,                      │
   resolve the Promise                   │
```

#### **Implementation Example**

On the React Native side, the flow is the same as greenfield — guard, read, write — except the read goes through the bridge. `newStorage` is a stand-in for whatever cross-platform store you picked (MMKV, AsyncStorage, SQLite, …):

```typescript
// MigrationService.ts
const KEYS_TO_MIGRATE = ['username', 'theme', 'authToken'];
const MIGRATION_DONE_KEY = 'migration_done';

async function runMigration(): Promise<void> {
  if (newStorage.contains(MIGRATION_DONE_KEY)) return;

  for (const key of KEYS_TO_MIGRATE) {
    const value = await requestNativeValue(key); // async, goes over the bridge
    if (value != null) newStorage.set(key, value);
  }

  newStorage.set(MIGRATION_DONE_KEY, 'true');
}
```

`requestNativeValue` is a small helper that wraps `postMessage` in a Promise: it sends the request, stores the resolver in a map keyed by `requestId`, and resolves it when the matching reply arrives — with a timeout that resolves to null after a few seconds, so a silent native side doesn't deadlock first launch.

On the native side, a single listener is registered at app startup. The same logic applies on both platforms, against `UserDefaults`/Keychain on iOS or `SharedPreferences`/`EncryptedSharedPreferences` on Android. `nativeStorage` is a stand-in for whichever native API holds the data:

```swift
// iOS — registered once in AppDelegate
ReactNativeBrownfield.shared.onMessage { raw in
    let message = decode(raw)
    guard message.type == "getNativeValue" else { return }

    let value = nativeStorage.read(message.key)
    ReactNativeBrownfield.shared.postMessage([
        "type": "nativeValueResponse",
        "requestId": message.requestId,
        "value": value as Any,
    ])
}
```

That's the whole protocol: React Native asks, native answers.

#### **A few things worth noting**

- **The migration logic lives entirely in JS.** The native side is a pure responder, which means migration changes ship in an OTA bundle update instead of a native release.
- **The completion flag lives in the new storage.** It's only written after every key has landed, so a crash mid-migration just retries on next launch.
- **Don't clear native storage right away.** Native screens that haven't been ported yet still read from it. Wipe the source only once those screens are gone.
- **Optional: notify native when a key migrates.** Adding a `migrationDone` message lets the native side mirror the new value into its own observable, so unported native screens update reactively instead of polling.

### **Testing and Rollout**

Before releasing the cross-platform update:

- **Test on real devices with existing user data.** Verify that preferences, tokens, and database records are preserved.
- **Simulate interruptions.** Force-close the app mid-migration to ensure that a partial migration doesn't corrupt data.
- **Measure performance.** Large migrations can delay startup; perform them asynchronously and show a progress indicator if needed.
- **Track metrics after release.** Use analytics to monitor migration success rates and crash reports. If failure rates rise, disable the migration or roll back the update.

Following these patterns, the Tapcart migration shipped without users noticing — every cart, every login, every preference exactly where it had been. That's the bar: a rewrite the user never knows happened.
