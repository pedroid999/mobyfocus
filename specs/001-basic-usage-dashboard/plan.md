# Implementation Plan: Basic Usage Dashboard

**Branch**: `001-basic-usage-dashboard` | **Date**: 2026-06-05 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-basic-usage-dashboard/spec.md`

## Summary

Deliver the first vertical slice of MobyFocus: detect whether Usage Access is
granted; if not, show an explanatory screen that deep-links to the system Usage
Access settings and re-checks on resume; once granted, read today's per-app
foreground usage via `UsageStatsManager`, map it to a domain model, and render a
Material 3 dashboard list sorted by descending usage (zero-usage apps hidden,
sub-minute usage shown as `<1 min`). The slice is built with MVVM + Clean
Architecture (domain/data/presentation/di), strict TDD, an immutable
`StateFlow`-backed `UiState` (Loading → Content / Empty / Error), and fully
externalized English strings.

## Technical Context

**Language/Version**: Kotlin 2.2.10 (JVM target 11), Gradle Kotlin DSL with
version catalog (`gradle/libs.versions.toml`).

**Primary Dependencies**:
- UI: Jetpack Compose (BOM 2026.02.01), Material 3, Navigation Compose.
- DI: Hilt (`hilt-android`, KSP compiler, `hilt-navigation-compose`).
- Async: Kotlin Coroutines + `Flow`/`StateFlow`; lifecycle-compose for
  `collectAsStateWithLifecycle`.
- Platform: `UsageStatsManager` (official) + `PackageManager` (labels/icons).
- Deferred (NOT added this slice, per Constitution simplicity): Room, DataStore,
  WorkManager, Kotlinx Serialization, Ktor/Retrofit.

**Storage**: N/A for this slice — usage is read on demand from the OS; nothing is
persisted. (Room/DataStore introduced when a feature needs them.)

**Testing**: JUnit4 + MockK + `kotlinx-coroutines-test` + Turbine for unit tests
(domain, data mapper, repository, ViewModel). Existing Compose UI test harness
(`androidx.compose.ui.test.junit4`) available for instrumented UI checks.

**Target Platform**: Android, `minSdk 26`, `targetSdk 36`, single `:app` module,
namespace `com.pedroid.mobyfocus`.

**Project Type**: Native Android mobile application (single module).

**Performance Goals**: Dashboard usable on cold open — usage read + label/icon
resolution complete within ~1s for a typical device (dozens of used apps);
loading state covers any longer read. No main-thread I/O (Principle IV).

**Constraints**: Official APIs + explicit permission only (Principle I); no
network; all user-facing text in string resources (Principle V); business logic
out of Composables; immutable `UiState` via `StateFlow` (Principle IV).

**Scale/Scope**: Two screens (permission, dashboard); ~2 use cases, 2
repositories, 1 data source, 2 domain models; tens of usage entries per day.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|-----------|------|--------|
| I. Ethical & Privacy-First | Data via `UsageStatsManager` only, after explicit `PACKAGE_USAGE_STATS` grant; no scraping/Accessibility/private APIs; data stays on device. | ✅ PASS |
| II. Test-First (Strict TDD) | Permission logic, `UsageStats→AppUsage` mapper, `GetTodayAppUsageUseCase`, and `DashboardViewModel` transitions all driven by tests written first (Red→Green→Refactor). | ✅ PASS |
| III. Layered Architecture | `domain` (pure Kotlin, no Android), `data` (impl + sources + mappers), `presentation` (VM + Compose), `di` (Hilt). Deps point inward. | ✅ PASS |
| IV. Idiomatic Compose UI (UDF) | Single immutable `UiState` per screen via `StateFlow`; stateless composables with hoisted state + callbacks; Material 3, dynamic theme, a11y semantics; coroutines off main thread. | ✅ PASS |
| V. Internationalization-First | 100% of visible text in `res/values/strings.xml`; English default; no hard-coded UI literals. | ✅ PASS |

**Initial gate**: PASS — no violations. **Post-design re-check**: PASS (see
Complexity Tracking — empty).

## Project Structure

### Documentation (this feature)

```text
specs/001-basic-usage-dashboard/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (domain + UI-state contracts)
│   ├── domain-interfaces.md
│   └── ui-state.md
├── checklists/
│   └── requirements.md  # Spec quality checklist (from /speckit-specify)
└── tasks.md             # Phase 2 output (/speckit-tasks — NOT created here)
```

### Source Code (repository root)

```text
app/src/main/java/com/pedroid/mobyfocus/
├── MobyFocusApplication.kt          # @HiltAndroidApp
├── MainActivity.kt                  # @AndroidEntryPoint; hosts NavHost + theme
├── presentation/
│   ├── navigation/
│   │   └── MobyFocusNavHost.kt      # permission ⇄ dashboard destinations
│   ├── permission/
│   │   ├── PermissionViewModel.kt   # PermissionUiState via StateFlow
│   │   ├── PermissionUiState.kt
│   │   └── PermissionScreen.kt      # stateless content + state holder
│   └── dashboard/
│       ├── DashboardViewModel.kt    # DashboardUiState: Loading/Content/Empty/Error
│       ├── DashboardUiState.kt
│       ├── DashboardScreen.kt       # stateless content + state holder
│       └── components/
│           └── AppUsageRow.kt       # stateless row (icon, name, package, time)
├── domain/
│   ├── model/
│   │   ├── AppUsage.kt
│   │   └── UsageAccessPermissionStatus.kt
│   ├── repository/
│   │   ├── UsageAccessRepository.kt
│   │   └── AppUsageRepository.kt
│   └── usecase/
│       ├── CheckUsageAccessPermissionUseCase.kt
│       └── GetTodayAppUsageUseCase.kt
├── data/
│   ├── usage/
│   │   └── UsageStatsDataSource.kt  # wraps UsageStatsManager + PackageManager
│   ├── repository/
│   │   ├── UsageAccessRepositoryImpl.kt
│   │   └── AppUsageRepositoryImpl.kt
│   └── mapper/
│       └── AppUsageMapper.kt        # UsageStats (+ pkg metadata) → AppUsage
├── di/
│   ├── RepositoryModule.kt
│   └── DataSourceModule.kt
└── ui/theme/                        # existing (Color/Theme/Type)

app/src/main/res/values/strings.xml  # all user-facing strings (extended)

app/src/test/java/com/pedroid/mobyfocus/        # JVM unit tests (TDD)
├── domain/usecase/
│   ├── CheckUsageAccessPermissionUseCaseTest.kt
│   └── GetTodayAppUsageUseCaseTest.kt
├── data/mapper/
│   └── AppUsageMapperTest.kt
├── data/repository/
│   └── AppUsageRepositoryImplTest.kt
└── presentation/dashboard/
    └── DashboardViewModelTest.kt

app/src/androidTest/java/com/pedroid/mobyfocus/ # instrumented (optional UI)
└── presentation/dashboard/DashboardScreenTest.kt
```

**Structure Decision**: Single Android module (`:app`) with package-by-layer then
feature inside `presentation`, matching the user-provided structure and
Constitution Principle III. Domain stays framework-free so its tests are pure
JVM. `MainActivity` becomes a thin Hilt entry point hosting a small
Navigation-Compose graph with two destinations. No new Gradle module is
introduced.

## Complexity Tracking

> No Constitution violations. No deviations to justify.

*(Intentionally empty — all gates pass; deferred libraries reduce, not add,
complexity for this slice.)*
