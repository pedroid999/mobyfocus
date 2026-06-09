# Tasks: App Classification

**Input**: Design documents from `/specs/002-app-classification/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/app-classification-contracts.md, quickstart.md

**Tests**: MANDATORY. Per the project Constitution (Principle II — Strict TDD, NON-NEGOTIABLE), every unit of business logic (enum mapping, mappers, repository impl, use cases, ViewModel state transitions) gets a failing test BEFORE its implementation. Each test task MUST be written and observed to FAIL (red) before the paired implementation task makes it pass (green).

**Organization**: Tasks are grouped by user story so each story is an independently implementable, independently testable increment.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: User story label (US1, US2, US3) — user story phases only
- Every task includes exact file paths

## Path Conventions

Single `:app` module, namespace `com.pedroid.mobyfocus`:

- Production code: `app/src/main/java/com/pedroid/mobyfocus/`
- JVM unit tests: `app/src/test/java/com/pedroid/mobyfocus/`
- Instrumented tests: `app/src/androidTest/java/com/pedroid/mobyfocus/`
- Resources: `app/src/main/res/`

---

## Phase 1: Setup (Room enters the project)

**Purpose**: Add Room to the build toolchain (first persistence dependency, per research R1/R7) and prove the AGP 9 built-in-Kotlin + KSP toolchain still compiles.

- [X] T001 Add Room to the version catalog in `gradle/libs.versions.toml`: pin latest stable 2.8.x as `room`, declare libraries `androidx-room-runtime` (`androidx.room:room-runtime`) and `androidx-room-compiler` (`androidx.room:room-compiler`). No `room-ktx` (coroutines/Flow support ships in `room-runtime` since 2.6).
- [X] T002 Wire Room into `app/build.gradle.kts`: `implementation(libs.androidx.room.runtime)`, `ksp(libs.androidx.room.compiler)`, and KSP arg `room.schemaLocation` pointing to `app/schemas/` (research R7 — schema export committed to git). Verify the toolchain with a clean `./gradlew :app:assembleDebug` (research R1: `android.disallowKotlinSourceSets=false` already covers Room's KSP processor; this build is the proof).

**Checkpoint**: Project compiles with Room on the classpath — no production code yet.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Domain vocabulary, Room storage stack, repository, DI wiring, and string resources that ALL user stories depend on. TDD pairs throughout.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

### Domain models (pure Kotlin — zero Android/Room imports, Principle III)

- [X] T003 [P] Write failing `AppCategoryTest` in `app/src/test/java/com/pedroid/mobyfocus/domain/model/AppCategoryTest.kt`: exactly 7 values (FR-001); `storageKey` equals the enum name for every value; `fromStorageKey` resolves each known key; unknown/corrupted key returns `NEUTRAL` (spec edge case). Run `./gradlew :app:testDebugUnitTest` — observe RED.
- [X] T004 Implement `AppCategory` enum in `app/src/main/java/com/pedroid/mobyfocus/domain/model/AppCategory.kt`: `PRODUCTIVE, COMMUNICATION, LEARNING, ENTERTAINMENT, SOCIAL, DISTRACTING, NEUTRAL`; `storageKey: String`; `companion fun fromStorageKey(key: String): AppCategory` defaulting to `NEUTRAL` (data-model.md). Run T003 suite — observe GREEN.
- [X] T005 [P] Create `AppClassification` data class in `app/src/main/java/com/pedroid/mobyfocus/domain/model/AppClassification.kt`: `packageName: String`, `category: AppCategory`, `updatedAtEpochMillis: Long` (data-model.md — no logic, no dedicated test).
- [X] T006 [P] Create `ClassifiedAppUsage` data class in `app/src/main/java/com/pedroid/mobyfocus/domain/model/ClassifiedAppUsage.kt`: `appUsage: AppUsage`, `category: AppCategory` (data-model.md).
- [X] T007 [P] Create `AppClassificationRepository` interface in `app/src/main/java/com/pedroid/mobyfocus/domain/repository/AppClassificationRepository.kt` exactly per contract §1: `observeAll(): Flow<List<AppClassification>>`, `observeByPackageName(packageName: String): Flow<AppClassification?>`, `suspend fun save(packageName: String, category: AppCategory)`.

### Room storage stack (`data.local.*` — Room confined here)

- [X] T008 Write failing instrumented `AppClassificationDaoTest` in `app/src/androidTest/java/com/pedroid/mobyfocus/data/local/AppClassificationDaoTest.kt` using `Room.inMemoryDatabaseBuilder` (research R8): upsert inserts a new row; upsert with the same `packageName` REPLACES the row (last-write-wins); `observeAll` re-emits after a change; `observeByPackageName` emits `null` when no row exists. RED = does not compile until T009–T011 exist.
- [X] T009 Create `AppClassificationEntity` in `app/src/main/java/com/pedroid/mobyfocus/data/local/entity/AppClassificationEntity.kt`: `@Entity(tableName = "app_classifications")`, `@PrimaryKey packageName: String`, `category: String` (stores `storageKey`), `updatedAtEpochMillis: Long` (data-model.md).
- [X] T010 Create `AppClassificationDao` in `app/src/main/java/com/pedroid/mobyfocus/data/local/dao/AppClassificationDao.kt` exactly per contract §3: `@Query` `observeAll(): Flow<List<AppClassificationEntity>>`, `@Query` `observeByPackageName(packageName: String): Flow<AppClassificationEntity?>`, `@Upsert suspend fun upsert(entity)` (research R2 — `@Upsert`, NOT `OnConflictStrategy.REPLACE`).
- [X] T011 Create `MobyFocusDatabase` in `app/src/main/java/com/pedroid/mobyfocus/data/local/MobyFocusDatabase.kt`: `@Database(entities = [AppClassificationEntity::class], version = 1, exportSchema = true)`, abstract `appClassificationDao()`. Run `./gradlew :app:connectedDebugAndroidTest` — T008 GREEN. Commit the exported schema JSON under `app/schemas/`.

### Mapper and repository implementation

- [X] T012 [P] Write failing `AppClassificationMapperTest` in `app/src/test/java/com/pedroid/mobyfocus/data/mapper/AppClassificationMapperTest.kt`: entity→domain maps `category` via `fromStorageKey`; entity with unknown category string maps to `NEUTRAL` (corruption edge case); domain→entity writes `storageKey` and passes `updatedAtEpochMillis` through. Observe RED.
- [X] T013 Implement `AppClassificationMapper` in `app/src/main/java/com/pedroid/mobyfocus/data/mapper/AppClassificationMapper.kt` (entity ↔ domain per data-model.md). T012 GREEN.
- [X] T014 [P] Write failing `AppClassificationRepositoryImplTest` in `app/src/test/java/com/pedroid/mobyfocus/data/repository/AppClassificationRepositoryImplTest.kt` (MockK DAO, Turbine for flows): `observeAll`/`observeByPackageName` map entities to domain; `save` builds the entity with `category.storageKey` and stamps `updatedAtEpochMillis` from the injected `() -> Long` clock (research R6 — deterministic time assertion); DAO failure propagates out of `save`. Observe RED.
- [X] T015 Implement `AppClassificationRepositoryImpl` in `app/src/main/java/com/pedroid/mobyfocus/data/repository/AppClassificationRepositoryImpl.kt`: injects `AppClassificationDao` + `@CurrentTimeMillis () -> Long`, delegates to the mapper. T014 GREEN.

### DI wiring and resources

- [X] T016 Create `DatabaseModule` in `app/src/main/java/com/pedroid/mobyfocus/di/DatabaseModule.kt` (SingletonComponent, contract §4): provides `MobyFocusDatabase` via `Room.databaseBuilder` (db name `"mobyfocus.db"`), `AppClassificationDao` from the database, and a `@CurrentTimeMillis`-qualified `() -> Long` bound to `System::currentTimeMillis` (define the qualifier annotation here).
- [X] T017 Extend `RepositoryModule` in `app/src/main/java/com/pedroid/mobyfocus/di/RepositoryModule.kt`: add `@Binds` `AppClassificationRepositoryImpl : AppClassificationRepository`. Verify Hilt graph compiles: `./gradlew :app:assembleDebug`.
- [X] T018 [P] Add all new string resources to `app/src/main/res/values/strings.xml` per contract §7 (English defaults, FR-009/Principle V): `category_productive`, `category_communication`, `category_learning`, `category_entertainment`, `category_social`, `category_distracting`, `category_neutral`, `detail_title`, `detail_usage_today_label`, `detail_category_section_title`, `detail_save_error`, `detail_back_content_description`.

**Checkpoint**: Persistence stack proven (DAO instrumented test green, repository unit-tested, Hilt graph compiles). User story phases can begin.

---

## Phase 3: User Story 1 — Assign a category to an app (Priority: P1) 🎯 MVP

**Goal**: Tapping a dashboard row opens a detail screen (icon, name, package, today's usage, current category); tapping one of the seven categories auto-saves it immediately — no confirmation button (clarified 2026-06-09).

**Independent Test**: Open the detail screen of any listed app, select a category, leave and reopen the detail screen — the saved category is shown as current (persisted, FR-005/FR-006). The dashboard-row reflection half of AC3 becomes fully visible once US2 lands; the save+persistence core is verifiable here on its own.

### Tests for User Story 1 (write first — observe RED) ⚠️

- [X] T019 [P] [US1] Write failing `SaveAppClassificationUseCaseTest` in `app/src/test/java/com/pedroid/mobyfocus/domain/usecase/SaveAppClassificationUseCaseTest.kt`: invoke delegates to `AppClassificationRepository.save(packageName, category)`; repository failure propagates to the caller (contract §2).
- [X] T020 [P] [US1] Write failing `GetAppClassificationUseCaseTest` in `app/src/test/java/com/pedroid/mobyfocus/domain/usecase/GetAppClassificationUseCaseTest.kt` (MockK + Turbine): invoke delegates to `AppClassificationRepository.observeByPackageName(packageName)` and emits its values, including `null` when no classification exists (Strict TDD — the use case is a unit of business logic and gets its own failing test; mocking it in the ViewModel test does NOT cover it).
- [X] T021 [P] [US1] Write failing `DetailViewModelTest` in `app/src/test/java/com/pedroid/mobyfocus/presentation/detail/DetailViewModelTest.kt` (MockK + Turbine + existing `MainDispatcherRule`): reads `packageName` from `SavedStateHandle`; emits `Loading` then `Content` with display name + today's usage from `GetTodayAppUsageUseCase` and effective category from `GetAppClassificationUseCase` (`NEUTRAL` when no classification, FR-008); app absent from today's usage → `displayName = packageName` fallback and `foregroundTimeMillis = 0` (uninstalled-app edge); selecting a category calls save and `Content.category` updates reactively when the classification flow re-emits; consecutive selections in quick succession → the LAST selection is the one saved and displayed (spec edge case "last selection wins").

### Implementation for User Story 1 (make tests GREEN)

- [X] T022 [US1] Implement `SaveAppClassificationUseCase` in `app/src/main/java/com/pedroid/mobyfocus/domain/usecase/SaveAppClassificationUseCase.kt` (`suspend operator fun invoke(packageName: String, category: AppCategory)`). T019 GREEN.
- [X] T023 [P] [US1] Implement `GetAppClassificationUseCase` in `app/src/main/java/com/pedroid/mobyfocus/domain/usecase/GetAppClassificationUseCase.kt` (`operator fun invoke(packageName: String): Flow<AppClassification?>` — delegation to `observeByPackageName`). T020 GREEN.
- [X] T024 [US1] Create `DetailUiState` in `app/src/main/java/com/pedroid/mobyfocus/presentation/detail/DetailUiState.kt` per contract §6: sealed interface with `Loading` and immutable `Content(packageName, displayName, foregroundTimeMillis, category, saveFailed)`.
- [X] T025 [US1] Implement `DetailViewModel` in `app/src/main/java/com/pedroid/mobyfocus/presentation/detail/DetailViewModel.kt`: `@HiltViewModel`, `SavedStateHandle` nav arg, combines usage snapshot + classification flow into `StateFlow<DetailUiState>` (single immutable state, Principle IV), `onCategorySelected` auto-saves via `SaveAppClassificationUseCase`. T021 GREEN (failure-path behavior is extended in US3).
- [X] T026 [US1] Implement `DetailScreen` in `app/src/main/java/com/pedroid/mobyfocus/presentation/detail/DetailScreen.kt`: `DetailRoute` (collects state, wires callbacks) + stateless `DetailScreen(state, onCategorySelected, onSaveErrorShown, onNavigateBack)` per contract §6. Material 3; app icon resolved in the presentation layer via `PackageManager` with generic-icon fallback (intentionally NOT part of `DetailUiState` — same pattern as dashboard rows, contract §6 note); today's usage formatted with the existing locale-aware dashboard formatter; the 7 category options labeled from `strings.xml` via a shared presentation-layer mapping (e.g., `AppCategory.labelRes(): @StringRes Int` — US2's row rendering reuses it) with selection semantics for accessibility (Principle IV/V). Tapping a category fires `onCategorySelected` — NO save button.
- [X] T027 [US1] Add the `detail/{packageName}` route to `app/src/main/java/com/pedroid/mobyfocus/presentation/navigation/MobyFocusNavHost.kt` per contract §5: required `packageName` string argument, back navigation returns to dashboard with no result passing (dashboard updates reactively).
- [X] T028 [US1] Wire row tap → navigation (FR-003): add `onClick: (packageName: String) -> Unit` to `app/src/main/java/com/pedroid/mobyfocus/presentation/dashboard/components/AppUsageRow.kt` and propagate through `app/src/main/java/com/pedroid/mobyfocus/presentation/dashboard/DashboardScreen.kt` to `navController.navigate("detail/$packageName")` in the NavHost.

**Checkpoint**: MVP — quickstart S1 steps 1–3 pass on device (open detail, auto-save category, selection persists when reopening detail). SC-001: ≤ 3 interactions.

---

## Phase 4: User Story 2 — See every app's category at a glance (Priority: P2)

**Goal**: Every dashboard row shows its effective category next to the existing usage info; never-classified apps show Neutral; a save on the detail screen is reflected on the dashboard with no manual refresh (reactive Flow).

**Independent Test**: Open the dashboard with a mix of classified and never-classified apps — every row shows a category, never-classified rows show Neutral (quickstart S2). Change a category on the detail screen and return — the row updates without refresh (completes US1 AC3 / SC-003).

### Tests for User Story 2 (write first — observe RED) ⚠️

- [X] T029 [P] [US2] Write failing `GetClassifiedAppUsageUseCaseTest` in `app/src/test/java/com/pedroid/mobyfocus/domain/usecase/GetClassifiedAppUsageUseCaseTest.kt` (Turbine): joins today's usage with classifications by `packageName`; apps without a classification get `NEUTRAL` (FR-008); usage list order and filtering preserved exactly (SC-006); when the classification flow re-emits a change, a new joined list is emitted (FR-007). (Read-failure fallback is added in US3.)
- [X] T030 [P] [US2] Extend `DashboardViewModelTest` in `app/src/test/java/com/pedroid/mobyfocus/presentation/dashboard/DashboardViewModelTest.kt` with failing cases: `Content` now carries `List<ClassifiedAppUsage>`; a classified app exposes its saved category; a never-classified app exposes `NEUTRAL`; a classification change re-emits `Content` without reloading usage.
- [X] T031 [P] [US2] Extend instrumented `DashboardScreenTest` in `app/src/androidTest/java/com/pedroid/mobyfocus/presentation/dashboard/DashboardScreenTest.kt` with failing assertions BEFORE the row changes (Strict TDD — category-label rendering is state-mapping logic, not pixel-only styling): a row displays its category label resolved from `strings.xml`, with the Neutral default case covered. RED until T033–T035 land.

### Implementation for User Story 2 (make tests GREEN)

- [X] T032 [US2] Implement `GetClassifiedAppUsageUseCase` in `app/src/main/java/com/pedroid/mobyfocus/domain/usecase/GetClassifiedAppUsageUseCase.kt` per research R3: load one-shot usage via existing `AppUsageRepository.getTodayUsage()`, `combine` directly with `AppClassificationRepository.observeAll()` (no intermediate use case — Simplicity), in-memory join by `packageName`, missing → `NEUTRAL`. T029 GREEN.
- [X] T033 [US2] Update `DashboardUiState` in `app/src/main/java/com/pedroid/mobyfocus/presentation/dashboard/DashboardUiState.kt`: `Content` holds `List<ClassifiedAppUsage>` (was `List<AppUsage>`).
- [X] T034 [US2] Update `DashboardViewModel` in `app/src/main/java/com/pedroid/mobyfocus/presentation/dashboard/DashboardViewModel.kt`: collect `GetClassifiedAppUsageUseCase` instead of mapping raw usage; existing permission/empty/error states unchanged. T030 GREEN.
- [X] T035 [US2] Update `AppUsageRow` in `app/src/main/java/com/pedroid/mobyfocus/presentation/dashboard/components/AppUsageRow.kt`: render `ClassifiedAppUsage` with the category label via the shared `AppCategory.labelRes()` mapping from T026 (no hardcoded literals, FR-009) alongside the existing usage info. Run `./gradlew :app:connectedDebugAndroidTest` — T031 GREEN.

**Checkpoint**: Quickstart S1 (full, including step 4) and S2 pass. SC-002, SC-003 met.

---

## Phase 5: User Story 3 — Classifications survive restarts and failures (Priority: P3)

**Goal**: Saved categories survive a full app restart; a failed save informs the user and keeps the previous category; a failed classification read never blocks the dashboard — rows fall back to Neutral (FR-010, research R5).

**Independent Test**: Assign categories, force-stop and reopen the app — every category is shown exactly as saved (quickstart S3). Failure paths are validated by unit tests (quickstart S5 — forcing real SQLite failures on-device is not practical).

### Tests for User Story 3 (write first — observe RED) ⚠️

- [X] T036 [P] [US3] Extend `DetailViewModelTest` in `app/src/test/java/com/pedroid/mobyfocus/presentation/detail/DetailViewModelTest.kt` with failing cases: save throws → `Content.saveFailed == true` AND the displayed category remains the previously persisted one (FR-010); `onSaveErrorShown()` resets `saveFailed` to false (transient flag consumed).
- [X] T037 [P] [US3] Extend `GetClassifiedAppUsageUseCaseTest` in `app/src/test/java/com/pedroid/mobyfocus/domain/usecase/GetClassifiedAppUsageUseCaseTest.kt` with a failing case: classification stream fails → usage list still emits with ALL rows `NEUTRAL` instead of erroring (FR-010, contract §2 behavioral guarantee).

### Implementation for User Story 3 (make tests GREEN)

- [X] T038 [US3] Implement save-failure handling in `app/src/main/java/com/pedroid/mobyfocus/presentation/detail/DetailViewModel.kt`: wrap save in try/catch (ViewModel is the catch boundary, research R5); on failure set transient `saveFailed = true` and keep showing the persisted category; expose `onSaveErrorShown()` to consume the flag. T036 GREEN.
- [X] T039 [US3] Surface the save error in `app/src/main/java/com/pedroid/mobyfocus/presentation/detail/DetailScreen.kt`: show `detail_save_error` (snackbar or equivalent Material 3 transient message) when `saveFailed` is true, invoking `onSaveErrorShown` once displayed.
- [X] T040 [US3] Implement the read-failure fallback in `app/src/main/java/com/pedroid/mobyfocus/domain/usecase/GetClassifiedAppUsageUseCase.kt`: `catch` on the classifications flow → fall back to an empty classification map so every row joins to `NEUTRAL` and usage data still renders. T037 GREEN.
- [X] T041 [US3] Manual durability validation per quickstart S3 on a device/emulator: assign categories to 2–3 apps, force-stop MobyFocus, reopen — every saved category displayed exactly as assigned (SC-004, FR-006).

**Checkpoint**: All three stories independently functional. SC-004, SC-005 met.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [X] T042 Refactor pass over all new code (the Refactor in Red-Green-Refactor): remove duplication across use cases/mappers, tighten naming, confirm immutability of UI states — all suites stay green.
- [X] T043 [P] Run the full quickstart automated validation: `./gradlew :app:testDebugUnitTest`, `./gradlew :app:connectedDebugAndroidTest`, `./gradlew :app:lintDebug` — all green.
- [X] T044 Manual regression per quickstart S4: dashboard sorting by usage descending, zero-usage apps hidden, `<1 min` sub-minute display all unchanged (SC-006); re-run S1/S2 end-to-end.
- [X] T045 Constitution compliance sweep: no new permissions in `app/src/main/AndroidManifest.xml` and no network calls (FR-011 / Principle I); `app/src/main/java/com/pedroid/mobyfocus/domain/` has zero Android/Room imports (Principle III); every new user-facing string lives in `app/src/main/res/values/strings.xml` (FR-009 / Principle V); exported Room schema committed under `app/schemas/`.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies. T001 → T002.
- **Foundational (Phase 2)**: Depends on Setup. BLOCKS all user stories.
- **US1 (Phase 3)**: Depends on Foundational. No dependency on other stories.
- **US2 (Phase 4)**: Depends on Foundational. Integrates with US1's detail screen for the reactive-update scenario (and reuses T026's `labelRes()` mapping), but the Neutral-default display is testable without US1.
- **US3 (Phase 5)**: Depends on Foundational; T036/T038/T039 extend US1 files, T037/T040 extend US2 files — run after both.
- **Polish (Phase 6)**: After all desired stories.

### Key Task-Level Dependencies

- T003 → T004 (red before green); T008 → T009 → T010 → T011; T012 → T013; T014 → T015 (T014 also needs T013's mapper to exist as a collaborator — write the test against the contract first regardless).
- T016/T017 need T011 + T015. T019 → T022; T020 → T023; T021 → T025; T024 before T025; T025 before T026; T026/T027 before T028.
- T029 → T032; T030 → T034; T031 → T035; T033 before T034/T035.
- T036 → T038 → T039; T037 → T040.

### Parallel Opportunities

- **Foundational**: T003, T005, T006, T007 together (different files); T012 + T014 + T018 together once models exist.
- **US1**: T019 + T020 + T021 together (three test files); T022 + T023 in parallel after their tests are red.
- **US2**: T029 + T030 + T031 together (three test files; T031 needs a device); T033 touches a different file than T032.
- **US3**: T036 + T037 together (different test files).
- **Stories**: With Foundational done, US1 and US2's use-case work (T029/T032) can proceed in parallel — they only converge on the dashboard files and the shared `labelRes()` mapping.

## Parallel Example: Foundational

```text
# After T002, launch together:
Task: "Write failing AppCategoryTest (T003)"
Task: "Create AppClassification data class (T005)"
Task: "Create ClassifiedAppUsage data class (T006)"
Task: "Create AppClassificationRepository interface (T007)"

# After T004–T011, launch together:
Task: "Write failing AppClassificationMapperTest (T012)"
Task: "Write failing AppClassificationRepositoryImplTest (T014)"
Task: "Add string resources (T018)"
```

## Parallel Example: User Story 1

```text
# Launch all three US1 test suites together (RED first):
Task: "Write failing SaveAppClassificationUseCaseTest (T019)"
Task: "Write failing GetAppClassificationUseCaseTest (T020)"
Task: "Write failing DetailViewModelTest (T021)"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1 (Setup) → Phase 2 (Foundational — the persistence stack, fully TDD).
2. Phase 3 (US1): detail screen + auto-save. **STOP and VALIDATE** with quickstart S1 steps 1–3.
3. Demo: the user can label any app's attention category and it persists.

### Incremental Delivery

1. Setup + Foundational → storage proven (DAO instrumented test green).
2. US1 → MVP: assign + persist a category from the detail screen.
3. US2 → dashboard shows every category at a glance; completes the reactive round-trip (SC-003).
4. US3 → durability + graceful failure; the feature becomes trustworthy.
5. Polish → refactor, full validation, constitution sweep.

### Notes

- Strict TDD is NON-NEGOTIABLE (Constitution Principle II): never write implementation before its failing test exists and has been observed red.
- `ObserveAppClassificationsUseCase` was removed from the original contract draft: it had no consumer (`GetClassifiedAppUsageUseCase` combines with `repository.observeAll()` directly per research R3) — Simplicity.
- Instrumented tasks (T008/T011, T031/T035, T041) need a device/emulator (API 26+) with Usage Access granted.
- Commit after each task or red-green pair; conventional commits.
- Total: 45 tasks — Setup 2, Foundational 16, US1 10, US2 7, US3 6, Polish 4.
