# Research: App Classification

**Feature**: `002-app-classification` | **Date**: 2026-06-09

All Technical Context unknowns resolved. No NEEDS CLARIFICATION markers remain.

## R1. Room on AGP 9 built-in Kotlin (toolchain compatibility)

- **Decision**: Add Room via the version catalog — `room-runtime` +
  `room-compiler` processed by KSP (KSP already at `2.2.10-2.0.2`, matching
  Kotlin 2.2.10). Pin the latest stable 2.8.x at implementation time and
  verify with a clean `:app:assembleDebug` as the first build-touching task.
- **Rationale**: The project already cleared the two AGP 9 built-in-Kotlin
  blockers while wiring Hilt (verified 2026-06-05): Hilt ≥ 2.59.x and
  `android.disallowKotlinSourceSets=false` in `gradle.properties`. That flag is
  exactly what lets KSP register generated sources, and it applies to Room's
  KSP processor the same way — no additional toolchain work is expected.
  Room 2.6+ ships coroutines/`Flow` support in `room-runtime` directly (no
  `room-ktx` needed).
- **Alternatives considered**: SQLDelight (rejected: Room is the
  constitution-named persistence library); raw SQLite (rejected: hand-rolled
  observability and threading, no compile-time query validation); DataStore
  (rejected: key-value store, wrong shape for a keyed table with reactive
  queries per row).

## R2. Write strategy for classifications

- **Decision**: Use Room's `@Upsert` on the DAO (`suspend fun upsert(entity)`).
- **Rationale**: `packageName` is the primary key and "saving again replaces
  the previous value" is a spec assumption. `@Upsert` (Room ≥ 2.5) expresses
  insert-or-update atomically without `OnConflictStrategy.REPLACE`'s
  delete-then-insert semantics (which would needlessly retrigger `Flow`
  emissions for observers of other rows).
- **Alternatives considered**: `@Insert(onConflict = REPLACE)` (rejected:
  delete+insert under the hood); separate `@Insert`/`@Update` with existence
  check (rejected: two queries and a race window for zero benefit).

## R3. Combining one-shot usage with reactive classifications

- **Decision**: New domain use case `GetClassifiedAppUsageUseCase` returning
  `Flow<List<ClassifiedAppUsage>>`: it loads today's usage snapshot via the
  existing `AppUsageRepository.getTodayUsage()` (suspend, one-shot — unchanged)
  and `combine`s it with `AppClassificationRepository.observeAll()`, joining in
  memory by `packageName` and defaulting missing entries to
  `AppCategory.NEUTRAL`.
- **Rationale**: Keeps FR-007/SC-003 (dashboard reflects a save with no manual
  refresh) purely reactive — when the user saves on the detail screen, the DAO
  `Flow` re-emits and the dashboard recomposes. Usage data itself stays
  one-shot exactly as the existing dashboard behaves today (SC-006: existing
  behavior unchanged). Join cost is an in-memory map over ≤ a few hundred
  entries — negligible.
- **Alternatives considered**: Making usage reactive too (rejected: out of
  scope, changes existing slice's semantics); ViewModel-level manual reload on
  navigation return (rejected: imperative, misses FR-007's "no manual refresh"
  spirit and is harder to test); querying classification per row (rejected:
  N queries instead of one observed table).

## R4. Navigation contract for the detail screen

- **Decision**: Route `detail/{packageName}` with `packageName` as the single
  required string nav argument. `DetailViewModel` reads it from
  `SavedStateHandle`, loads the usage snapshot for that package via
  `GetTodayAppUsageUseCase`, and observes its classification via
  `GetAppClassificationUseCase`.
- **Rationale**: Package name is the canonical app identity (FR-002), is
  process-death safe as a nav argument, and avoids serializing usage objects
  through navigation. The detail screen re-deriving usage keeps a single
  source of truth and naturally covers the "app uninstalled later today" edge
  (fallback display name = package name, generic icon).
- **Alternatives considered**: Passing display name + usage millis as extra nav
  args (rejected: duplicated state that can go stale); shared ViewModel between
  dashboard and detail (rejected: couples screen lifecycles, harder to test).

## R5. Persistence error handling (FR-010)

- **Decision**: Repository methods stay `suspend`/`Flow` and may throw
  (matching the existing `AppUsageRepository` contract style). ViewModels are
  the catch boundary: `DetailViewModel` wraps save in try/catch — on failure it
  sets a transient `saveFailed` flag in `DetailUiState` (consumed via an
  `onSaveErrorShown` event) and keeps showing the previously persisted
  category; the dashboard treats a classification read failure by falling back
  to an empty classification map (rows render with Neutral), never blanking
  usage data.
- **Rationale**: Mirrors the established project pattern (repository throws →
  ViewModel maps to state), keeps domain free of Android error types, and
  satisfies FR-010 verbatim: failed save informs the user + previous value
  stands; failed read still renders the dashboard.
- **Alternatives considered**: `Result<T>` return types in domain (rejected:
  diverges from the existing repository contract style for no added safety at
  this scale); silent failure (rejected: violates FR-010).

## R6. Timestamp source for `updatedAtEpochMillis`

- **Decision**: Inject a `Clock`-style provider (`() -> Long` bound in DI,
  production = `System::currentTimeMillis`) into
  `AppClassificationRepositoryImpl`, which stamps the entity at save time.
- **Rationale**: Timestamping is a persistence concern (the domain model
  carries it; the spec only requires "the moment it was last updated").
  Injection makes the repository unit-testable with deterministic time —
  required for strict TDD assertions on the saved entity.
- **Alternatives considered**: `System.currentTimeMillis()` inline (rejected:
  untestable assertion); `java.time.Clock` (workable on minSdk 26, but a
  `() -> Long` lambda is the smallest thing that satisfies the need —
  Simplicity).

## R7. Room schema export

- **Decision**: Enable schema export (`room.schemaLocation` KSP arg →
  `app/schemas/`, committed to git).
- **Rationale**: This database WILL grow (goals, summaries are on the roadmap).
  Exported schemas are the prerequisite for migration testing later; enabling
  it on day one costs one KSP arg and avoids retrofitting.
- **Alternatives considered**: `exportSchema = false` (rejected: silences the
  build warning by discarding future migration safety).

## R8. Test strategy per layer

- **Decision**:
  - JVM unit tests (MockK + Turbine + coroutines-test, existing
    `MainDispatcherRule`): `AppCategory` storage-key mapping (incl. unknown →
    NEUTRAL), entity↔domain mapper, `SaveAppClassificationUseCase`,
    `GetAppClassificationUseCase` (delegation to `observeByPackageName`),
    `GetClassifiedAppUsageUseCase` (join + Neutral default + re-emission on
    classification change), `AppClassificationRepositoryImpl` (DAO mocked,
    timestamp injected), `DashboardViewModel` (categories present),
    `DetailViewModel` (load, save success, save failure keeps previous +
    flags error).
  - Instrumented: `AppClassificationDaoTest` with `Room.inMemoryDatabaseBuilder`
    (upsert overwrites, observeAll emits on change, observeByPackageName emits
    null when absent).
- **Rationale**: Covers every spec testing requirement plus the DAO round-trip
  the unit layer cannot honestly cover (DAO is generated code + SQLite
  behavior). All business logic remains JVM-testable per Principle III.
- **Alternatives considered**: Robolectric for DAO tests on the JVM (rejected:
  new dependency for one test class — Simplicity; androidTest infrastructure
  already exists from feature 001).
