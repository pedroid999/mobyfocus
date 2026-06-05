# Contract: Domain Interfaces

These are the stable seams between layers. Signatures are the contract; bodies are
implemented in the tasks phase under TDD. Domain types are framework-free.

## Repositories (domain/repository)

### UsageAccessRepository

```kotlin
interface UsageAccessRepository {
    /** Live read of the current Usage Access permission state from the OS. */
    fun getPermissionStatus(): UsageAccessPermissionStatus
}
```

- Implemented by `data/repository/UsageAccessRepositoryImpl` using `AppOpsManager`
  (research R3). Never cached.

### AppUsageRepository

```kotlin
interface AppUsageRepository {
    /**
     * Today's per-app foreground usage, already filtered (no zero usage) and
     * sorted by foreground time descending.
     * @throws on unrecoverable read failure → surfaced as Error state.
     */
    suspend fun getTodayUsage(): List<AppUsage>
}
```

- Implemented by `data/repository/AppUsageRepositoryImpl` delegating to
  `UsageStatsDataSource` + `AppUsageMapper`. Runs off the main thread.

## Use Cases (domain/usecase)

### CheckUsageAccessPermissionUseCase

```kotlin
class CheckUsageAccessPermissionUseCase(
    private val repository: UsageAccessRepository,
) {
    operator fun invoke(): UsageAccessPermissionStatus
}
```

- **Behavior contract / tests**: returns `GRANTED` when repository reports granted;
  `NOT_GRANTED` otherwise. (Testing requirement #1.)

### GetTodayAppUsageUseCase

```kotlin
class GetTodayAppUsageUseCase(
    private val repository: AppUsageRepository,
) {
    suspend operator fun invoke(): List<AppUsage>
}
```

- **Behavior contract / tests** (Testing requirement #3):
  - Returns the repository list unchanged when already valid.
  - Result contains no entry with `foregroundTimeMillis == 0`.
  - Result is ordered by `foregroundTimeMillis` descending.
  - Empty input ⇒ empty list (drives `Empty` state, never an error).

## Data Source (data/usage)

### UsageStatsDataSource

```kotlin
interface UsageStatsDataSource {
    /** Raw aggregated foreground millis per package for [startMillis, endMillis]. */
    fun getForegroundUsageMillis(startMillis: Long, endMillis: Long): Map<String, Long>
}
```

- Implemented over `UsageStatsManager.queryAndAggregateUsageStats` (research R1).
  The "today" window is computed by the caller (research R2).

## Mapper (data/mapper)

### AppUsageMapper

```kotlin
class AppUsageMapper(/* PackageManager access */) {
    /** Map raw usage millis-by-package into domain AppUsage list. */
    fun toAppUsageList(usageByPackage: Map<String, Long>): List<AppUsage>
}
```

- **Behavior contract / tests** (Testing requirement #2):
  - Resolves display name; on `NameNotFoundException` falls back to package name.
  - Does NOT resolve the icon — `AppUsage` is icon-agnostic; the presentation
    layer resolves icons by `packageName` (see data-model.md, finding U1).
  - Drops entries whose millis are `0`.
  - Carries precise millis through for downstream sorting.
