# LearnDash — Learning Dashboard (Android)

Login → course dashboard → course detail with lesson completion. Offline-first, MVVM + Clean Architecture, Jetpack Compose. Demo login: `learner@intellipaat.com` / `learn123`.

## Demo Video
[▶ Watch the 2-minute walkthrough (Google Drive)](https://drive.google.com/file/d/1mo6hSmV8JYxMaAxWGwA1eK6kYNQ3SiCX/view?usp=sharing) — shows login, the course dashboard, course details with lesson completion, and the offline behaviour (airplane mode → cached courses still load).

## 1. Architecture
UI (Compose screens) → ViewModel → Repository interface → API + Room. One ViewModel per screen, Room as single source of truth, Hilt for DI. I chose this because it's the smallest setup that still looks like production: the UI never touches data sources, and swapping the mock API for Retrofit later touches one class (`CourseApi`) without changing repositories or screens.

## 2. Offline Support
Room (`courses` + `lessons` tables) caches everything on first load. A connectivity check fails fast to cache when the radio is off; otherwise the repository serves `Updated` / `ServedFromCache` / `Failed` so the UI knows exactly what happened. Airplane-mode flow: kill the network → dashboard shows cached courses with an "offline" banner → lesson toggles still work and persist locally. Refresh merges only newly added lessons inside a transaction — completed flags are never overwritten.

## 3. Security
This assignment uses mock auth so only the email is kept (DataStore). In production the JWT/refresh token goes in **EncryptedSharedPreferences (AndroidX Security) backed by Android Keystore** — never plain DataStore/SharedPrefs. Short-lived access tokens in memory, refresh via `Authenticator`, certificate pinning, no tokens in logs.

## 4. Scale (1M users, hundreds of courses)
1. **Paging 3 + server-side search/filter** — page ~20 courses per request with placeholders and prefetch distance of ~10; never ship hundreds of rows in one payload.
2. **WorkManager delta sync** — `PeriodicWorkRequest` (~15 min) hitting a `?since=timestamp` endpoint + last-write-wins conflict resolution, instead of a full refresh on every open.
3. **Baseline Profiles + R8/App Bundle**, thumbnail-first image loading (e.g. Coil) with disk/memory caches; lazy `LazyColumn` already in place.
4. **Crashlytics + Remote Config + analytics** for staged rollouts, A/B flags, and triaging the failures this repo already surfaces as `RefreshResult.Failed`.
5. **Modularize by feature** (`:feature:dashboard`, `:core:data`) and enforce it with Gradle dependency constraints to keep build times and blast radius sane.

## 5. Second Platform (iOS/macOS)
Same architecture, SwiftUI-native: `ObservableObject` ViewModels + `@Published` state (≡ StateFlow), SwiftData for the cache (≡ Room), URLSession + Codable for the API (≡ CourseApi), Keychain for tokens (≡ EncryptedSharedPreferences). Repository protocols keep the UI testable; Swift Testing covers `progressFor` and the toggle logic identically. macOS target shares the Swift package with iOS.
