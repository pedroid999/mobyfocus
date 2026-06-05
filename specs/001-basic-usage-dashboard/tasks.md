---
description: "Task list for Basic Usage Dashboard implementation"
---

# Tasks: Basic Usage Dashboard

**Input**: Design documents from `/specs/001-basic-usage-dashboard/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: MANDATORY for all business logic per Constitution Principle II (Strict
TDD, NON-NEGOTIABLE). Every test task below MUST be written and observed to FAIL
before its corresponding implementation task (Red → Green → Refactor). This
includes ALL ViewModel state transitions (both `PermissionViewModel` and
`DashboardViewModel`).

**Organization**: Tasks are grouped by user story (US1, US2, US3) for independent
implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on an incomplete task)
- **[Story]**: US1 / US2 / US3 — only on user-story phase tasks
- All paths are relative to repo root; package root is
  `app/src/main/java/com/pedroid/mobyfocus/` (abbreviated below as `…/mobyfocus/`),
  unit tests under `app/src/test/java/com/pedroid/mobyfocus/` (`test/…/mobyfocus/`).

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Wire dependencies and Hilt so any story can be built.

- [X] T001 Add dependency entries to `gradle/libs.versions.toml`: Hilt (`hilt-android`, `hilt-compiler`), KSP, `hilt-navigation-compose`, `androidx-navigation-compose`, `androidx-lifecycle-viewmodel-compose`, `androidx-lifecycle-runtime-compose`, `kotlinx-coroutines-android`; test: `mockk`, `kotlinx-coroutines-test`, `turbine`.
- [X] T002 Apply Hilt + KSP plugins and add the new dependencies in `app/build.gradle.kts` (and root `build.gradle.kts` plugin aliases as needed).
- [X] T003 Declare `<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" tools:ignore="ProtectedPermissions" />` (and the `tools` namespace) in `app/src/main/AndroidManifest.xml`.
- [X] T004 Create `MobyFocusApplication` annotated `@HiltAndroidApp` in `…/mobyfocus/MobyFocusApplication.kt` and register it via `android:name` in `AndroidManifest.xml`.
- [X] T005 Annotate `MainActivity` with `@AndroidEntryPoint` and remove the sample `Greeting`/`GreetingPreview` in `…/mobyfocus/MainActivity.kt`.
- [X] T006 [P] Add `MainDispatcherRule` (JUnit rule swapping `Dispatchers.Main` for a test dispatcher) in `test/…/mobyfocus/util/MainDispatcherRule.kt`.

**Checkpoint**: Project builds with Hilt; app launches into an empty entry point.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared domain core, DI skeleton, and navigation host used by ALL stories.

**⚠️ CRITICAL**: No user story work begins until this phase is complete.

- [X] T007 [P] Create `AppUsage` domain model — **icon-agnostic**: `packageName`, `displayName`, `foregroundTimeMillis` only (no Android types; domain stays framework-free per Principle III) in `…/mobyfocus/domain/model/AppUsage.kt`.
- [X] T008 [P] Create `UsageAccessPermissionStatus` (GRANTED / NOT_GRANTED) in `…/mobyfocus/domain/model/UsageAccessPermissionStatus.kt`.
- [X] T009 [P] Create `UsageAccessRepository` interface in `…/mobyfocus/domain/repository/UsageAccessRepository.kt`.
- [X] T010 [P] Create `AppUsageRepository` interface in `…/mobyfocus/domain/repository/AppUsageRepository.kt`.
- [X] T011 Create `DispatcherModule` providing IO/Default `CoroutineDispatcher` (with qualifiers) in `…/mobyfocus/di/DispatcherModule.kt`.
- [X] T012 Create `MobyFocusNavHost` with `permission` and `dashboard` destinations and host it from `MainActivity` inside `MobyFocusTheme` in `…/mobyfocus/presentation/navigation/MobyFocusNavHost.kt`.

**Checkpoint**: Domain contracts, DI, and navigation scaffold exist — stories can start.

---

## Phase 3: User Story 1 - Grant Usage Access through a clear explanation (Priority: P1) 🎯 MVP

**Goal**: First-time user sees a clear explanation, opens system settings, grants
access, and on return the app re-checks and proceeds.

**Independent Test**: With access NOT granted, launch → explanation screen with
rationale + Open-settings button; tapping opens system Usage Access settings;
returning re-checks permission.

### Tests for User Story 1 (MANDATORY - write first, must FAIL) ⚠️

- [X] T013 [P] [US1] Unit test `CheckUsageAccessPermissionUseCaseTest` (granted vs not-granted mapping) in `test/…/mobyfocus/domain/usecase/CheckUsageAccessPermissionUseCaseTest.kt`.
- [X] T014 [P] [US1] Unit test `UsageAccessRepositoryImplTest` (AppOps `MODE_ALLOWED` → GRANTED, else NOT_GRANTED) using MockK in `test/…/mobyfocus/data/repository/UsageAccessRepositoryImplTest.kt`.
- [X] T015 [P] [US1] Unit test `PermissionViewModelTest` — `uiState` transitions `NOT_GRANTED → GRANTED` after `refresh()` when permission becomes granted, and stays `NOT_GRANTED` otherwise (Turbine + `MainDispatcherRule`) in `test/…/mobyfocus/presentation/permission/PermissionViewModelTest.kt`. *(Resolves analysis finding CN1 — Principle II requires ViewModel transition tests.)*

### Implementation for User Story 1

- [X] T016 [US1] Implement `UsageAccessRepositoryImpl` using `AppOpsManager` (`OPSTR_GET_USAGE_STATS`, `unsafeCheckOpNoThrow`/`checkOpNoThrow`) in `…/mobyfocus/data/repository/UsageAccessRepositoryImpl.kt` (makes T014 pass).
- [X] T017 [US1] Implement `CheckUsageAccessPermissionUseCase` in `…/mobyfocus/domain/usecase/CheckUsageAccessPermissionUseCase.kt` (makes T013 pass).
- [X] T018 [US1] Bind `UsageAccessRepository → UsageAccessRepositoryImpl` in `…/mobyfocus/di/RepositoryModule.kt`.
- [X] T019 [P] [US1] Create `PermissionUiState(status)` in `…/mobyfocus/presentation/permission/PermissionUiState.kt`.
- [X] T020 [US1] Implement `PermissionViewModel` (`@HiltViewModel`, `uiState: StateFlow`, `refresh()`) in `…/mobyfocus/presentation/permission/PermissionViewModel.kt` (makes T015 pass).
- [X] T021 [US1] Implement stateless `PermissionScreen` (rationale + Open-settings button via `onOpenSettings` callback, ON_RESUME re-check using `LifecycleEventEffect`) in `…/mobyfocus/presentation/permission/PermissionScreen.kt`.
- [X] T022 [US1] Wire `ACTION_USAGE_ACCESS_SETTINGS` intent launch from the screen's state holder and `GRANTED → navigate(dashboard)`, `NOT_GRANTED → stay` in `…/mobyfocus/presentation/navigation/MobyFocusNavHost.kt`.
- [X] T023 [P] [US1] Add strings `permission_title`, `permission_rationale`, `permission_open_settings` in `app/src/main/res/values/strings.xml`.

**Checkpoint**: US1 fully functional and independently testable (gate to the app).

---

## Phase 4: User Story 2 - View today's app usage on the dashboard (Priority: P1)

**Goal**: With access granted, show today's used apps (icon/name/package/minutes),
sorted by foreground time descending, zero-usage hidden, sub-minute as `<1 min`.

**Independent Test**: With access granted and usage present, dashboard lists apps
with name/package/minutes sorted desc, no zero entries.

### Tests for User Story 2 (MANDATORY - write first, must FAIL) ⚠️

- [X] T024 [P] [US2] Unit test `AppUsageMapperTest` (label fallback on `NameNotFoundException`, drop zero-millis, precise millis preserved) in `test/…/mobyfocus/data/mapper/AppUsageMapperTest.kt`.
- [X] T025 [P] [US2] Unit test `GetTodayAppUsageUseCaseTest` (no zero entries, sorted desc, empty→empty) in `test/…/mobyfocus/domain/usecase/GetTodayAppUsageUseCaseTest.kt`.
- [X] T026 [P] [US2] Unit test `AppUsageRepositoryImplTest` (data source + mapper composition, filter + sort) in `test/…/mobyfocus/data/repository/AppUsageRepositoryImplTest.kt`.
- [X] T027 [P] [US2] Unit test `DashboardViewModelTest` Loading→Content path with Turbine in `test/…/mobyfocus/presentation/dashboard/DashboardViewModelTest.kt`.

### Implementation for User Story 2

- [X] T028 [US2] Create `UsageStatsDataSource` interface + impl over `UsageStatsManager.queryAndAggregateUsageStats` with today-window helper (`java.time`, local midnight→now) in `…/mobyfocus/data/usage/UsageStatsDataSource.kt`.
- [X] T029 [US2] Implement `AppUsageMapper` (resolve `displayName` label via `PackageManager`, fallback to package name, drop zero-millis — **no icon**, icon is resolved later by the UI) in `…/mobyfocus/data/mapper/AppUsageMapper.kt` (makes T024 pass).
- [X] T030 [US2] Implement `AppUsageRepositoryImpl` (compute window, query source, map, filter, sort desc, off-main dispatcher) in `…/mobyfocus/data/repository/AppUsageRepositoryImpl.kt` (makes T026 pass).
- [X] T031 [US2] Implement `GetTodayAppUsageUseCase` in `…/mobyfocus/domain/usecase/GetTodayAppUsageUseCase.kt` (makes T025 pass).
- [X] T032 [US2] Bind `AppUsageRepository` + provide `UsageStatsDataSource` in `…/mobyfocus/di/RepositoryModule.kt` and `…/mobyfocus/di/DataSourceModule.kt`.
- [X] T033 [P] [US2] Create sealed `DashboardUiState` (Loading/Content/Empty/Error) in `…/mobyfocus/presentation/dashboard/DashboardUiState.kt`.
- [X] T034 [US2] Implement `DashboardViewModel` (`@HiltViewModel`, `uiState` starts Loading, `load()` → Content) in `…/mobyfocus/presentation/dashboard/DashboardViewModel.kt` (makes T027 pass).
- [X] T035 [P] [US2] Implement stateless `AppUsageRow` — receives `app: AppUsage`, `usageLabel: String`, and an optional `icon: ImageBitmap?` param; renders a Material 3 placeholder when `icon == null`; sets icon `contentDescription` in `…/mobyfocus/presentation/dashboard/components/AppUsageRow.kt`.
- [X] T036 [US2] Implement `DashboardScreen` Content list (LazyColumn, sorted) consuming `DashboardUiState`, resolving each app's icon by `packageName` via `PackageManager` → `ImageBitmap` (research R7) and passing it to `AppUsageRow` in `…/mobyfocus/presentation/dashboard/DashboardScreen.kt`.
- [X] T037 [P] [US2] Add strings `dashboard_title`, `usage_minutes` (plural `%d min`), `usage_less_than_minute` (`<1 min`), `app_icon_content_description` in `app/src/main/res/values/strings.xml`.

**Checkpoint**: US1 + US2 work — full happy path (grant → see usage).

---

## Phase 5: User Story 3 - Resilient loading / empty / error states (Priority: P2)

**Goal**: Loading, empty, and error states render clear messages — never a blank
screen or crash.

**Independent Test**: Loading shows until resolved; no-usage day → empty message;
forced read failure → error message + retry, no crash.

### Tests for User Story 3 (MANDATORY - write first, must FAIL) ⚠️

- [X] T038 [P] [US3] Extend `DashboardViewModelTest` with Loading→Empty (empty usage) and Loading→Error (use case throws) transitions in `test/…/mobyfocus/presentation/dashboard/DashboardViewModelTest.kt`.

### Implementation for User Story 3

- [X] T039 [US3] Emit `Empty` on empty result and `Error` on thrown failure in `…/mobyfocus/presentation/dashboard/DashboardViewModel.kt` (makes T038 pass).
- [X] T040 [US3] Render Loading + Empty + Error (with retry via `onRetry → load()`) branches in `…/mobyfocus/presentation/dashboard/DashboardScreen.kt`.
- [X] T041 [P] [US3] Add strings `dashboard_empty`, `dashboard_error`, `dashboard_retry` in `app/src/main/res/values/strings.xml`.

**Checkpoint**: All user stories independently functional and resilient.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [X] T042 [P] Instrumented Compose UI test `DashboardScreenTest` (Loading/Content/Empty/Error render) in `app/src/androidTest/java/com/pedroid/mobyfocus/presentation/dashboard/DashboardScreenTest.kt`.
- [X] T043 [P] Verify accessibility (icon `contentDescription`, screen-reader order) and dark mode + Material 3 dynamic theming on both screens.
- [X] T044 Remove leftover sample tests `ExampleUnitTest.kt` and `ExampleInstrumentedTest.kt`.
- [X] T045 Run `quickstart.md` end-to-end validation (install, grant, verify US1–US3).
- [X] T046 [P] Run `./gradlew :app:lintDebug` and resolve any new warnings introduced.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: no dependencies — start immediately.
- **Foundational (Phase 2)**: depends on Setup — BLOCKS all user stories.
- **US1 (Phase 3)**: depends on Foundational. MVP gate.
- **US2 (Phase 4)**: depends on Foundational. Independent of US1 (can build in
  parallel by another dev), but the live app reaches it via US1's permission gate.
- **US3 (Phase 5)**: depends on US2's `DashboardViewModel`/`DashboardScreen`.
- **Polish (Phase 6)**: depends on the stories it touches being complete.

### Story Independence

- **US1** and **US2** share only Foundational artifacts (models, repo interfaces,
  DI, nav host) — implementable in parallel.
- **US3** extends US2's dashboard, so it follows US2.

### Within Each Story

- Test tasks (⚠️) MUST be written and FAIL before their implementation task.
- Domain/use case before data impl wiring; ViewModel before screen; strings any time.

---

## Parallel Opportunities

```text
# Phase 2 foundational models/interfaces (different files):
T007, T008, T009, T010  → in parallel, then T011, T012.

# US1 tests together (write first, must fail):
T013, T014, T015  → in parallel.

# US2 tests together (write first, must fail):
T024, T025, T026, T027  → in parallel.

# String-resource additions are [P] within their story (separate keys):
T023 (US1), T037 (US2), T041 (US3).
```

---

## Implementation Strategy

### MVP First

1. Phase 1 Setup → Phase 2 Foundational.
2. Phase 3 (US1) → **STOP & VALIDATE**: permission onboarding works end-to-end.
3. Phase 4 (US2) → grant→dashboard happy path is the demoable MVP.

### Incremental Delivery

- Setup + Foundational → US1 (gate) → US2 (core value) → US3 (resilience) → Polish.
- Each story is a testable increment; tests precede implementation throughout
  (Constitution Principle II).
