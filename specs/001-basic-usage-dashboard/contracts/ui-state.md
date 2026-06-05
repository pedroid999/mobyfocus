# Contract: UI State & Screen Contracts

Presentation contracts. ViewModels expose a single immutable state via
`StateFlow`; Composables are stateless and receive state + callbacks (Principle IV).

## DashboardViewModel

```kotlin
sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Content(val apps: List<AppUsage>) : DashboardUiState   // non-empty, sorted
    data object Empty : DashboardUiState
    data class Error(val messageKey: ErrorReason = ErrorReason.GENERIC) : DashboardUiState
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getTodayAppUsage: GetTodayAppUsageUseCase,
) : ViewModel() {
    val uiState: StateFlow<DashboardUiState>   // starts Loading
    fun load()   // (re)reads usage; Loading → Content/Empty/Error
}
```

**State-transition contract (Testing requirement #4 — tests written first):**
- Initial value is `Loading`.
- On success with non-empty usage → `Content(apps)` where `apps` is sorted desc
  and contains no zero-usage entry.
- On success with empty usage → `Empty`.
- On thrown failure from the use case → `Error`.
- Emission order on load is exactly `[Loading, <terminal>]` (assert via Turbine).
- Work runs on an injected dispatcher (test overrides with `StandardTestDispatcher`).

## PermissionViewModel

```kotlin
data class PermissionUiState(
    val status: UsageAccessPermissionStatus = UsageAccessPermissionStatus.NOT_GRANTED,
)

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val checkPermission: CheckUsageAccessPermissionUseCase,
) : ViewModel() {
    val uiState: StateFlow<PermissionUiState>
    fun refresh()   // called on ON_RESUME
}
```

**Contract:**
- `refresh()` re-reads permission; updates `status`.
- The screen observes `status`; `GRANTED` triggers navigation to dashboard,
  `NOT_GRANTED` keeps the explanation screen visible (FR-005).

## Composable contracts (stateless)

```kotlin
@Composable fun PermissionScreen(
    state: PermissionUiState,
    onOpenSettings: () -> Unit,          // launches ACTION_USAGE_ACCESS_SETTINGS
)

@Composable fun DashboardScreen(
    state: DashboardUiState,
    onRetry: () -> Unit,                 // re-invokes load() from Error
)

@Composable fun AppUsageRow(
    app: AppUsage,
    usageLabel: String,                  // pre-formatted from string resources
    icon: ImageBitmap?,                  // resolved by the screen from packageName; null → placeholder
)
```

**Rules:**
- No business logic inside Composables (Principle IV).
- Every visible string (`title`, explanation body, button text, empty message,
  error message, `"<1 min"`, `"%d min"`) comes from `strings.xml` (Principle V).
- `AppUsageRow` provides `contentDescription` for the icon and is screen-reader
  navigable; supports Material 3 dynamic theming and dark mode.

## String resource keys (to add to res/values/strings.xml)

| Key (proposed) | Purpose |
|----------------|---------|
| `permission_title` | Explanation screen title |
| `permission_rationale` | Why Usage Access is needed |
| `permission_open_settings` | Settings button label |
| `dashboard_title` | Dashboard screen title |
| `dashboard_empty` | Empty-state message |
| `dashboard_error` | Error-state message |
| `dashboard_retry` | Retry button label |
| `usage_minutes` | `"%d min"` (plural-aware via `plurals` if desired) |
| `usage_less_than_minute` | `"<1 min"` |
| `app_icon_content_description` | Accessibility description for app icons |

Final keys/wording are finalized during implementation; the contract is that
**none** of these are hard-coded in Composables.
