# Phase 0 Research: Basic Usage Dashboard

All Technical Context unknowns are resolved below. No open `NEEDS CLARIFICATION`.

## R1. Reading today's per-app foreground usage

- **Decision**: Use `UsageStatsManager.queryAndAggregateUsageStats(startOfToday, now)`
  and read `UsageStats.totalTimeInForeground` per package, summed by the API into a
  `Map<String, UsageStats>`.
- **Rationale**: `totalTimeInForeground` is the clarified metric (foreground /
  active in-app time), available since API 21 — safe for `minSdk 26`.
  `queryAndAggregateUsageStats` returns one aggregated entry per package, avoiding
  the manual de-duplication required when iterating raw `queryUsageStats` buckets.
- **Alternatives considered**:
  - `queryUsageStats(INTERVAL_DAILY, …)` — returns multiple buckets per package
    and buckets may start before local midnight; needs manual aggregation/clamping.
  - `queryEvents` (MOVE_TO_FOREGROUND/BACKGROUND pairing) — most precise but far
    more code and state; unjustified for this slice (Principle: Simplicity).

## R2. Defining "today"

- **Decision**: `startOfToday` = device local midnight via
  `ZonedDateTime.now(ZoneId.systemDefault()).toLocalDate().atStartOfDay(zone)`;
  `now` = current epoch millis. Pass both as `Long` epoch-millis to the data source.
- **Rationale**: Matches spec ("device's local calendar day"). Using `java.time`
  (desugared/available at API 26) keeps the boundary logic testable in pure JVM.
- **Alternatives considered**: `Calendar` with manual field zeroing — error-prone,
  harder to test. Rejected.

## R3. Detecting Usage Access permission

- **Decision**: Check via `AppOpsManager` op `OPSTR_GET_USAGE_STATS` using
  `unsafeCheckOpNoThrow` (API 29+) / `checkOpNoThrow` (API < 29) for the app's uid
  and package; `MODE_ALLOWED` ⇒ granted. Expose as a suspend/synchronous call on
  `UsageAccessRepository` returning `UsageAccessPermissionStatus`.
- **Rationale**: Usage Access is an AppOps-gated special permission, not a runtime
  permission — it cannot be requested via the permission dialog and is not in the
  manifest grant flow. AppOps is the canonical, official check. (Principle I:
  official APIs only.)
- **Alternatives considered**: Attempting a query and inferring from empty results
  — unreliable (empty can also mean "no usage"). Rejected.

## R4. Opening the system Usage Access settings

- **Decision**: Launch `Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)`. The
  Intent is created in the UI layer from a ViewModel-emitted event/callback;
  the repository/domain never touch `Intent`.
- **Rationale**: Official deep link to the exact settings screen. Keeps the
  Android framework dependency in `presentation` only.

## R5. Re-checking permission on return (resume)

- **Decision**: `PermissionViewModel` re-evaluates permission on
  `Lifecycle.Event.ON_RESUME` (observed in the screen via
  `LifecycleEventEffect` / `DisposableEffect` + `LifecycleEventObserver`),
  calling `CheckUsageAccessPermissionUseCase` and updating `PermissionUiState`.
- **Rationale**: The user grants in system settings and returns; resume is the
  reliable signal. No polling, no crash if the user backs out unchanged.

## R6. Resolving app display name & icon (and filtering)

- **Decision**: For each package with usage, resolve label via
  `PackageManager.getApplicationLabel(appInfo)` and icon via
  `getApplicationIcon(packageName)`. If the package is no longer installed
  (`NameNotFoundException`), fall back to the package name as the display name and
  a null icon (UI shows a fallback visual). Zero-foreground entries are dropped.
- **Rationale**: Satisfies FR-007 (icon when available, name, package) and FR-009
  (hide zero usage) while tolerating uninstalled-but-recorded packages.
- **Gotcha**: Icon/label resolution touches `PackageManager` (potentially slow for
  many packages) — perform on a background dispatcher (`Dispatchers.Default/IO`),
  never on main (Principle IV); this is why the Loading state exists (FR-010a).

## R7. Rendering a PackageManager Drawable in Compose

- **Decision**: Convert the resolved `Drawable` to an `ImageBitmap`
  (`drawable.toBitmap().asImageBitmap()`) in the data/mapper boundary or a small UI
  adapter, and render with `Image`. Provide a Material 3 placeholder when null.
- **Rationale**: Avoids adding an image-loading dependency (Coil/Accompanist) for a
  single local Drawable. (Principle: Simplicity.) `androidx.core:core-ktx`
  (already present) provides `Drawable.toBitmap()`.

## R8. Navigation between permission and dashboard

- **Decision**: Use Navigation Compose with two destinations (`permission`,
  `dashboard`). Start destination is chosen from the initial permission check; on
  grant (detected at resume) the app navigates to `dashboard`.
- **Rationale**: Navigation Compose is part of the product's stated stack and the
  app is multi-screen by design; modeling both destinations now keeps the graph
  ready for future screens (classification, goals) at negligible cost.
- **Alternative considered**: A single root `when(permissionState)` swap (no
  Navigation dependency). Simpler for exactly two states, but does not generalize
  to the imminent multi-screen roadmap; chosen against to avoid a near-term rewrite.

## R9. Deferred dependencies (explicit non-decisions)

- **Decision**: Do NOT add Room, DataStore, WorkManager, Kotlinx Serialization, or
  Ktor/Retrofit in this slice.
- **Rationale**: This feature has no persistence, background work, serialization,
  or network need. Constitution "Simplicity" forbids dependencies without a
  present need; Clean Architecture seams already keep the codebase *ready* (NFR-6)
  without installing them. They are introduced by the first feature that needs them.

## R10. Testability strategy (Strict TDD enablement)

- **Decision**: All Android framework access (`AppOpsManager`, `UsageStatsManager`,
  `PackageManager`) sits behind `UsageStatsDataSource` and the repository
  implementations. Domain models, the mapper, use cases, and `DashboardViewModel`
  depend only on Kotlin/interfaces, so their tests are pure JVM with MockK fakes.
- **Tools**: `kotlinx-coroutines-test` (`runTest`, `StandardTestDispatcher`) for
  coroutine control; Turbine to assert `StateFlow` emission sequences
  (Loading → Content/Empty/Error); MockK for repository/use-case test doubles.
- **Rationale**: Pure-JVM tests are fast and deterministic — the foundation for
  Red-Green-Refactor (Principle II) without an emulator.
