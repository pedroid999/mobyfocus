# Implementation Plan: App Classification

**Branch**: `002-app-classification` | **Date**: 2026-06-09 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-app-classification/spec.md`

## Summary

Let the user assign one of seven fixed attention categories (Productive,
Communication, Learning, Entertainment, Social, Distracting, Neutral) to any
app listed on the usage dashboard. Tapping a dashboard row opens a new detail
screen (icon, name, package, today's usage, current category); tapping a
category persists it immediately to a new Room database. The dashboard combines
today's usage with a reactive classification stream so rows always show the
effective category (saved value, or Neutral default). This is the project's
first persistence slice: it introduces Room behind a domain repository
interface, wired through the existing Hilt graph, with strict TDD throughout.

## Technical Context

**Language/Version**: Kotlin 2.2.10, AGP 9.2.1 (built-in Kotlin toolchain — no
`org.jetbrains.kotlin.android` plugin), Gradle Kotlin DSL, version catalog.

**Primary Dependencies**: Jetpack Compose + Material 3 (BOM 2026.02.01),
Navigation Compose 2.9.5, Hilt 2.59.2 (KSP 2.2.10-2.0.2), Coroutines 1.10.2.
**New this feature**: Room (latest stable 2.8.x line, pinned in
`gradle/libs.versions.toml`; compiler via KSP).

**Storage**: Room (new `MobyFocusDatabase`, version 1, single table
`app_classifications`). On-device only; no network, no export (Principle I /
FR-011).

**Testing**: JUnit4 + MockK + Turbine + kotlinx-coroutines-test for unit tests
(`app/src/test`); Compose UI + Room DAO instrumented tests in
`app/src/androidTest`. Strict TDD (Red-Green-Refactor) per Principle II.

**Target Platform**: Android, `minSdk 26`, single `:app` module, namespace
`com.pedroid.mobyfocus`.

**Project Type**: Native Android mobile app (MVVM + Clean Architecture).

**Performance Goals**: Dashboard render with classifications adds no
perceptible latency vs. current dashboard (classification join is an in-memory
map over ≤ a few hundred rows); category save reflects on the detail screen
and dashboard immediately (reactive Flow, no manual refresh).

**Constraints**: Offline-only, on-device persistence; domain layer has zero
Android imports; all new UI strings in `res/values/strings.xml`; no blocking
calls on the main thread (Room accessed via suspend/Flow on injected IO
dispatcher).

**Scale/Scope**: One new table (≤ ~500 rows realistically — apps used on one
device); one new screen (`presentation.detail`); one dashboard row enhancement;
2 new domain models + 1 enum; 1 repository interface + impl; 3 use cases;
~7 unit test suites + 1 instrumented DAO test + 1 instrumented UI test update.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|-----------|------|--------|
| I. Ethical & Privacy-First Data Collection | No new data collection APIs. Classification is user-entered data, stored on-device in Room, never transmitted (FR-011). No new permissions. | ✅ PASS |
| II. Test-First Development (Strict TDD) | Every unit of logic (enum mapping, entity↔domain mappers, repository impl, use cases, both ViewModels) gets a failing test BEFORE implementation. Tasks phase must order tests before code. | ✅ PASS (planned; enforced in tasks) |
| III. Layered Architecture (MVVM + Clean) | `domain` additions are pure Kotlin (enum, data classes, interface, use cases — zero Android/Room imports). Room types live only in `data.local.*`; mapping in `data.mapper`. Presentation depends on domain only. DI via Hilt modules in `di`. | ✅ PASS |
| IV. Idiomatic Compose UDF | New `DetailViewModel` exposes a single immutable `UiState` via `StateFlow`; `DetailScreen` is stateless (state + callbacks hoisted to a `DetailRoute`). Dashboard keeps its existing UDF shape, with `ClassifiedAppUsage` in `Content`. Material 3, a11y semantics on category controls. | ✅ PASS |
| V. Internationalization-First | 7 category display names + detail screen labels + save-error message all in `strings.xml` (English default). Duration formatting reuses the existing locale-aware dashboard formatter. | ✅ PASS |
| Simplicity (Tech Constraints) | Room is added NOW because this feature requires persistence — exactly the trigger the constitution defines. DataStore/WorkManager/Serialization remain deferred. Room enters via the version catalog. | ✅ PASS |

**Post-Design Re-check (after Phase 1)**: All gates remain ✅ — the data model
keeps Room confined to `data.local`, contracts show domain interfaces free of
Android types, and the detail screen contract is stateless UDF. No Complexity
Tracking entries required.

## Project Structure

### Documentation (this feature)

```text
specs/002-app-classification/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/
│   └── app-classification-contracts.md   # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit-tasks — NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
app/src/main/java/com/pedroid/mobyfocus/
├── domain/
│   ├── model/
│   │   ├── AppUsage.kt                      # existing — unchanged
│   │   ├── AppCategory.kt                   # NEW: enum, 7 values + safe fromStorageKey
│   │   ├── AppClassification.kt             # NEW: packageName + category + updatedAt
│   │   └── ClassifiedAppUsage.kt            # NEW: AppUsage + effective AppCategory
│   ├── repository/
│   │   ├── AppUsageRepository.kt            # existing — unchanged
│   │   └── AppClassificationRepository.kt   # NEW: observeAll/observeByPackageName/save
│   └── usecase/
│       ├── GetTodayAppUsageUseCase.kt       # existing — unchanged
│       ├── GetAppClassificationUseCase.kt        # NEW (observe single, by package)
│       ├── SaveAppClassificationUseCase.kt       # NEW
│       └── GetClassifiedAppUsageUseCase.kt       # NEW: usage ⊕ classifications → Flow
├── data/
│   ├── local/
│   │   ├── MobyFocusDatabase.kt             # NEW: RoomDatabase, v1
│   │   ├── dao/AppClassificationDao.kt      # NEW: observeAll/observeByPackageName/upsert
│   │   └── entity/AppClassificationEntity.kt # NEW: table app_classifications
│   ├── mapper/
│   │   ├── AppUsageMapper.kt                # existing — unchanged
│   │   └── AppClassificationMapper.kt       # NEW: entity ↔ domain
│   └── repository/
│       └── AppClassificationRepositoryImpl.kt # NEW
├── di/
│   ├── RepositoryModule.kt                  # MODIFIED: + bindAppClassificationRepository
│   └── DatabaseModule.kt                    # NEW: Room database + DAO providers
└── presentation/
    ├── dashboard/
    │   ├── DashboardUiState.kt              # MODIFIED: Content holds ClassifiedAppUsage
    │   ├── DashboardViewModel.kt            # MODIFIED: uses GetClassifiedAppUsageUseCase
    │   ├── DashboardScreen.kt               # MODIFIED: row onClick → navigate
    │   └── components/AppUsageRow.kt        # MODIFIED: shows category label
    ├── detail/
    │   ├── DetailUiState.kt                 # NEW
    │   ├── DetailViewModel.kt               # NEW
    │   └── DetailScreen.kt                  # NEW (DetailRoute + stateless DetailScreen)
    └── navigation/
        └── MobyFocusNavHost.kt              # MODIFIED: + detail/{packageName} route

app/src/main/res/values/strings.xml         # MODIFIED: category names, detail labels, save error

app/src/test/java/com/pedroid/mobyfocus/
├── domain/model/AppCategoryTest.kt                      # NEW
├── domain/usecase/SaveAppClassificationUseCaseTest.kt   # NEW
├── domain/usecase/GetAppClassificationUseCaseTest.kt    # NEW
├── domain/usecase/GetClassifiedAppUsageUseCaseTest.kt   # NEW
├── data/mapper/AppClassificationMapperTest.kt           # NEW
├── data/repository/AppClassificationRepositoryImplTest.kt # NEW
├── presentation/dashboard/DashboardViewModelTest.kt     # MODIFIED
└── presentation/detail/DetailViewModelTest.kt           # NEW

app/src/androidTest/java/com/pedroid/mobyfocus/
├── data/local/AppClassificationDaoTest.kt   # NEW: in-memory Room round-trip
└── presentation/dashboard/DashboardScreenTest.kt # MODIFIED if assertions touch rows
```

**Structure Decision**: Extend the existing single-module Clean Architecture
layout in place. The only structurally new packages are `data.local` (first
Room artifacts: database, `dao`, `entity`) and `presentation.detail` (new
screen), both already anticipated by the constitution's layering rules. DI
gains one focused `DatabaseModule`; everything else extends existing files.

## Complexity Tracking

No constitution violations — table intentionally left empty.
