# Phase 1 Data Model: Basic Usage Dashboard

Domain models are pure Kotlin (no Android imports) per Constitution Principle III.

## Entity: AppUsage

Represents a single app's foreground attention summary for today.

| Field | Type | Rules |
|-------|------|-------|
| `packageName` | `String` | Identity. Non-blank. Unique within a dashboard list. |
| `displayName` | `String` | Human-readable label. Falls back to `packageName` when the app is uninstalled / label unavailable. Non-blank. |
| `foregroundTimeMillis` | `Long` | Total foreground (active in-app) time today, in milliseconds. MUST be `> 0` for an entry to exist (zero-usage filtered out). |

**Derived (presentation) value — not stored on the domain model:**
- `usageLabel`: `foregroundTimeMillis >= 60_000` → whole minutes (e.g., `"12 min"`);
  `0 < foregroundTimeMillis < 60_000` → `"<1 min"`. Computed in the UI layer from
  string resources; never hard-coded.

**Validation rules (from requirements):**
- FR-009: entries with `foregroundTimeMillis == 0` are excluded before the list is
  produced (filtered in `GetTodayAppUsageUseCase` / repository).
- FR-008: list ordering is by `foregroundTimeMillis` descending, using the precise
  millis value (not the rounded label) so equal labels still sort deterministically.

**Icon representation (decided — resolves analysis finding U1)**: `AppUsage` is
icon-agnostic. It carries only `packageName`, `displayName`, and
`foregroundTimeMillis`, keeping `domain` free of any Android type (Principle III).
The **presentation layer** resolves each app's icon by `packageName` via
`PackageManager` and renders it as an `ImageBitmap` (research R7), falling back to a
Material 3 placeholder when the icon is unavailable (`NameNotFoundException`).
`displayName` (a plain `String`) is resolved in the data layer by the mapper.

## Entity: UsageAccessPermissionStatus

Whether the app currently holds permission to read device usage data.

- **Type**: enum / sealed value with exactly two states:
  - `GRANTED` — AppOps `OPSTR_GET_USAGE_STATS` is `MODE_ALLOWED`.
  - `NOT_GRANTED` — any other mode.
- **Usage**: drives the start destination and the permission screen vs. dashboard
  decision; re-evaluated on `ON_RESUME` (research R5).
- **No persistence**: always read live from the OS; never cached across launches.

## State: DashboardUiState (presentation)

Immutable, exposed via `StateFlow` (Principle IV). Models the load lifecycle:

| State | Meaning | Carried data |
|-------|---------|--------------|
| `Loading` | Usage is being read/resolved | — |
| `Content` | At least one used app today | `List<AppUsage>` (pre-sorted, non-empty) |
| `Empty` | Access granted, no usage today | — |
| `Error` | Usage could not be retrieved | optional message key |

**Transitions**: `Loading → Content` \| `Loading → Empty` \| `Loading → Error`
(clarified). A manual refresh (if added later) re-enters `Loading`.

## State: PermissionUiState (presentation)

Immutable, exposed via `StateFlow`.

| Field | Type | Notes |
|-------|------|-------|
| `status` | `UsageAccessPermissionStatus` | Drives whether the explanation screen is shown. |

`NOT_GRANTED` → show explanation screen (FR-002/003/004). `GRANTED` → proceed to
dashboard (navigation). Re-checked on resume (FR-005).

## Relationships

- A dashboard `Content` state holds an ordered `List<AppUsage>`; each `AppUsage`
  is identified by `packageName`.
- `UsageAccessPermissionStatus` gates whether any `AppUsage` data is read at all —
  no permission ⇒ no query is performed (Principle I).
