# Interface Contracts: App Classification

**Feature**: `002-app-classification` | **Date**: 2026-06-09

These are the seams the feature exposes between layers. Signatures are
binding; bodies belong to the implementation phase. See
[data-model.md](../data-model.md) for type definitions.

## 1. Domain Repository Contract — `domain.repository.AppClassificationRepository`

```kotlin
interface AppClassificationRepository {
    /** All saved classifications; re-emits whenever any row changes. */
    fun observeAll(): Flow<List<AppClassification>>

    /** The classification for [packageName], or null when none saved; reactive. */
    fun observeByPackageName(packageName: String): Flow<AppClassification?>

    /**
     * Insert-or-replace the classification for [packageName] with [category],
     * stamped with the current time by the implementation.
     * @throws Exception on an unrecoverable write failure (ViewModel catch boundary, FR-010).
     */
    suspend fun save(packageName: String, category: AppCategory)
}
```

## 2. Use Case Contracts — `domain.usecase`

```kotlin
class GetAppClassificationUseCase       { operator fun invoke(packageName: String): Flow<AppClassification?> }
class SaveAppClassificationUseCase      { suspend operator fun invoke(packageName: String, category: AppCategory) }
class GetClassifiedAppUsageUseCase      { operator fun invoke(): Flow<List<ClassifiedAppUsage>> }
```

Behavioral guarantees:
- `GetClassifiedAppUsageUseCase` combines today's usage directly with
  `AppClassificationRepository.observeAll()` (research R3) — no intermediate
  observe-all use case exists (removed 2026-06-09: it had no consumer —
  Simplicity).
- `GetClassifiedAppUsageUseCase` preserves the order and filtering of
  `AppUsageRepository.getTodayUsage()` (SC-006), maps missing classifications
  to `AppCategory.NEUTRAL` (FR-008), and re-emits when any classification
  changes (FR-007). If classifications cannot be read, it emits usage with all
  rows `NEUTRAL` rather than failing (FR-010).
- `SaveAppClassificationUseCase` propagates repository failures to the caller
  (the ViewModel decides presentation, FR-010).

## 3. DAO Contract — `data.local.dao.AppClassificationDao`

```kotlin
@Dao
interface AppClassificationDao {
    @Query("SELECT * FROM app_classifications")
    fun observeAll(): Flow<List<AppClassificationEntity>>

    @Query("SELECT * FROM app_classifications WHERE packageName = :packageName")
    fun observeByPackageName(packageName: String): Flow<AppClassificationEntity?>

    @Upsert
    suspend fun upsert(entity: AppClassificationEntity)
}
```

## 4. DI Contract — `di.DatabaseModule` (new) + `di.RepositoryModule` (extended)

```kotlin
// DatabaseModule provides (SingletonComponent):
MobyFocusDatabase                       // Room.databaseBuilder, db name "mobyfocus.db"
AppClassificationDao                    // from the database
@CurrentTimeMillis () -> Long           // production: System::currentTimeMillis

// RepositoryModule adds:
@Binds AppClassificationRepositoryImpl : AppClassificationRepository
```

## 5. Navigation Contract — `presentation.navigation`

| Property | Value |
|----------|-------|
| Route pattern | `detail/{packageName}` |
| Argument | `packageName: String` (required, non-null) |
| Entry point | Dashboard row tap → `navController.navigate("detail/$packageName")` |
| Exit | System/back navigation → dashboard (no result passing; dashboard updates reactively) |

## 6. Screen Contracts — `presentation.detail` (new), `presentation.dashboard` (modified)

```kotlin
// DetailUiState (immutable, exposed via StateFlow<DetailUiState>)
sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Content(
        val packageName: String,
        val displayName: String,        // fallback: packageName (uninstalled-app edge)
        val foregroundTimeMillis: Long, // 0 when app absent from today's usage
        val category: AppCategory,      // effective category (Neutral default)
        val saveFailed: Boolean,        // transient; true after a failed save (FR-010)
    ) : DetailUiState
}

// Stateless screen (UDF): state down, events up
DetailScreen(
    state: DetailUiState,
    onCategorySelected: (AppCategory) -> Unit,  // auto-save on tap (clarified 2026-06-09)
    onSaveErrorShown: () -> Unit,               // consumes saveFailed
    onNavigateBack: () -> Unit,
)

// Note: the app icon (FR-004) is intentionally NOT part of DetailUiState —
// it is resolved in the presentation layer via PackageManager with a generic
// fallback, the same pattern dashboard rows already use.

// Dashboard changes
DashboardUiState.Content(val apps: List<ClassifiedAppUsage>)   // was List<AppUsage>
AppUsageRow(item: ClassifiedAppUsage, onClick: (packageName: String) -> Unit)
// Row renders the category label from strings.xml next to existing usage info.
```

## 7. String Resource Contract — `res/values/strings.xml` (Principle V)

New keys (English defaults; exact wording finalized at implementation):
`category_productive`, `category_communication`, `category_learning`,
`category_entertainment`, `category_social`, `category_distracting`,
`category_neutral`, `detail_title`, `detail_usage_today_label`,
`detail_category_section_title`, `detail_save_error`,
`detail_back_content_description`. No hardcoded user-facing literals in any
new Composable (FR-009).
