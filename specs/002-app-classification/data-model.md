# Data Model: App Classification

**Feature**: `002-app-classification` | **Date**: 2026-06-09

## Domain Layer (pure Kotlin — zero Android/Room imports)

### `AppCategory` (enum) — `domain.model`

The closed set of seven attention categories (FR-001).

| Value | Storage key |
|-------|-------------|
| `PRODUCTIVE` | `"PRODUCTIVE"` |
| `COMMUNICATION` | `"COMMUNICATION"` |
| `LEARNING` | `"LEARNING"` |
| `ENTERTAINMENT` | `"ENTERTAINMENT"` |
| `SOCIAL` | `"SOCIAL"` |
| `DISTRACTING` | `"DISTRACTING"` |
| `NEUTRAL` | `"NEUTRAL"` *(universal default)* |

Rules:
- `storageKey: String` — stable persistence identifier (enum name). UI display
  names are NOT here; they live in `strings.xml` resolved by the presentation
  layer (Principle V).
- `companion fun fromStorageKey(key: String): AppCategory` — returns matching
  value, or **`NEUTRAL` for any unknown/corrupted key** (spec edge case).

### `AppClassification` — `domain.model`

The association between one app and one category (Key Entity 2).

| Field | Type | Rules |
|-------|------|-------|
| `packageName` | `String` | Identity; non-blank; at most one classification per package |
| `category` | `AppCategory` | Never null — absence is modeled by *no record*, presented as Neutral |
| `updatedAtEpochMillis` | `Long` | Stamped by the data layer at save time |

Lifecycle: created on first save; every subsequent save **replaces** the row
(upsert, last-write-wins — spec edge case "last selection wins"). No history.

### `ClassifiedAppUsage` — `domain.model`

Presentation-facing combination (Key Entity 3): what dashboard rows and the
detail screen render.

| Field | Type | Rules |
|-------|------|-------|
| `appUsage` | `AppUsage` | Existing model (packageName, displayName, foregroundTimeMillis) — unchanged |
| `category` | `AppCategory` | Effective category: saved value, or `NEUTRAL` when no record exists (FR-008) |

Derivation: `usage ⊕ classifications` joined by `packageName`; missing key →
`NEUTRAL`. Ordering/filtering of the underlying usage list is untouched
(SC-006).

## Data Layer (`data.local.*`, Room confined here)

### `AppClassificationEntity` — `data.local.entity`

Table: `app_classifications` (database `MobyFocusDatabase`, version 1).

| Column | Kotlin type | Constraints |
|--------|-------------|-------------|
| `packageName` | `String` | `@PrimaryKey` |
| `category` | `String` | Non-null; stores `AppCategory.storageKey` |
| `updatedAtEpochMillis` | `Long` | Non-null |

No foreign keys, no indices beyond the primary key (lookups are by PK; full
scans are ≤ hundreds of rows). No migrations yet (v1); schema exported to
`app/schemas/` for future migration tests.

### Mapping (`data.mapper.AppClassificationMapper`)

- Entity → Domain: `category` string via `AppCategory.fromStorageKey` (unknown
  → `NEUTRAL`, satisfying the corruption edge case).
- Domain → Entity: `category.storageKey`; `updatedAtEpochMillis` passed
  through (stamped by the repository at save time via injected clock).

## State Transitions

```text
                 (no record)
  App appears on dashboard ──────────────► presented as NEUTRAL (FR-008)
        │
        │ user taps category C on detail screen (auto-save, FR-005)
        ▼
  app_classifications[packageName] = (C, now)   ── upsert, replaces previous
        │
        ├─ save OK ──► DAO Flow re-emits ──► detail + dashboard show C (FR-007)
        └─ save FAILS ──► user informed; previous record (or NEUTRAL) stands (FR-010)
```

## Validation Rules (from requirements)

- Exactly seven categories; the set is closed (FR-001). Enforced by the enum.
- One classification per `packageName` (primary key) — re-save overwrites.
- Unknown stored category value → treated as `NEUTRAL` (edge case).
- Classification rows are never deleted by this feature (an app with a saved
  category but no usage today simply isn't displayed — spec edge case).
- All data stays on-device (FR-011): no network access in any layer of this
  feature.
