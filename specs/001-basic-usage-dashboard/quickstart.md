# Quickstart: Basic Usage Dashboard

A validation/run guide proving the slice works end-to-end. Implementation details
live in `tasks.md`; contracts in `contracts/`, model in `data-model.md`.

## Prerequisites

- Android Studio (current) + JDK 17 to run Gradle; `minSdk 26` device or emulator.
- A device/emulator with some real app usage today (open a couple of apps first),
  or test the empty state on a fresh emulator.
- New dependencies wired in `gradle/libs.versions.toml` + `app/build.gradle.kts`:
  Hilt (+ KSP), Navigation Compose, lifecycle-viewmodel/runtime-compose,
  coroutines; test: MockK, `kotlinx-coroutines-test`, Turbine.
- `AndroidManifest.xml` declares the special permission:
  `<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"
  tools:ignore="ProtectedPermissions" />`.

## Run the app

```bash
./gradlew :app:installDebug
# then launch MobyFocus from the launcher
```

## Validate user stories

### US1 — Permission onboarding (P1)

1. Fresh install (access not granted) → app opens on the **explanation screen**.
2. Screen states *why* Usage Access is needed and shows an **Open settings** button.
3. Tap it → the system **Usage Access settings** screen opens (for MobyFocus).
4. Grant access, press back to return → app **re-checks on resume** and shows the
   dashboard. (Return without granting → explanation screen stays.)

**Expected**: matches spec Acceptance Scenarios US1 #1–#4.

### US2 — Today's usage dashboard (P1)

1. With access granted and prior app usage today, open MobyFocus.
2. Dashboard lists used apps: **icon (or placeholder), display name, package name,
   minutes today**.
3. List is **sorted by usage descending**; apps with **zero** usage are absent.
4. An app used <1 minute shows **`<1 min`**, never `0 min`.

**Expected**: matches Acceptance Scenarios US2 #1–#4.

### US3 — Loading / empty / error (P2)

1. On open, a **Loading** state shows until usage resolves.
2. Fresh emulator with no usage → friendly **empty-state** message (not a blank list).
3. Force a read failure (e.g., test double) → **error message** + retry, no crash.

**Expected**: matches Acceptance Scenarios US3 #1–#3 and FR-010a.

## Run the tests (TDD — written first, must pass)

```bash
./gradlew :app:testDebugUnitTest        # JVM unit tests (domain, mapper, repo, VM)
./gradlew :app:connectedDebugAndroidTest # optional Compose UI checks
```

**Must pass (per Constitution Principle II):**
- `CheckUsageAccessPermissionUseCaseTest` — granted/not-granted mapping.
- `AppUsageMapperTest` — label fallback, icon optionality, zero-usage drop.
- `GetTodayAppUsageUseCaseTest` — filtered, sorted desc, empty→empty.
- `DashboardViewModelTest` — `Loading → Content/Empty/Error` emission order (Turbine).

## Constitution gate (manual check before merge)

- [ ] No data obtained except via `UsageStatsManager` after explicit grant (P-I).
- [ ] Every test existed and failed before its implementation (P-II).
- [ ] No Android imports in `domain/` (P-III).
- [ ] Composables stateless; single immutable `UiState` via `StateFlow` (P-IV).
- [ ] Zero hard-coded user-facing strings; English in `values/strings.xml` (P-V).
