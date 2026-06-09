# Quickstart: App Classification — Validation Guide

**Feature**: `002-app-classification`

How to prove the feature works end-to-end. References:
[spec.md](spec.md) · [data-model.md](data-model.md) ·
[contracts](contracts/app-classification-contracts.md)

## Prerequisites

- JDK + Android SDK as already configured for this repo (AGP 9.2.1, Kotlin
  2.2.10 built-in toolchain; `android.disallowKotlinSourceSets=false` already
  set in `gradle.properties`).
- A device/emulator (API 26+) with Usage Access granted to MobyFocus and some
  app activity today (for manual scenarios and instrumented tests).

## Automated validation

```bash
# 1. JVM unit tests — all business logic (TDD suites for this feature included)
./gradlew :app:testDebugUnitTest

# 2. Instrumented tests — Room DAO round-trip + Compose UI (device/emulator required)
./gradlew :app:connectedDebugAndroidTest

# 3. Static checks
./gradlew :app:lintDebug
```

**Expected**: all three green. Unit suites covering this feature:
`AppCategoryTest`, `AppClassificationMapperTest`,
`AppClassificationRepositoryImplTest`, `SaveAppClassificationUseCaseTest`,
`GetClassifiedAppUsageUseCaseTest`, `DashboardViewModelTest`,
`DetailViewModelTest`; instrumented: `AppClassificationDaoTest`.

## Manual validation scenarios

Install a debug build first: `./gradlew :app:installDebug`

### S1 — Assign a category (User Story 1, P1)

1. Open MobyFocus → dashboard lists today's apps; every row shows a category
   (Neutral on a fresh install).
2. Tap an app row → detail screen shows icon, display name, package name,
   today's usage time, current category.
3. Tap **Productive** → no save button appears (auto-save); selection becomes
   the current category immediately.
4. Navigate back → that app's dashboard row now reads Productive, with no
   manual refresh. ✅ SC-001 (≤3 interactions), SC-003.

### S2 — Neutral default (User Story 2, P2)

1. Find an app never classified → its row shows Neutral.
2. Open its detail screen → current category shows Neutral. ✅ SC-002, FR-008.

### S3 — Persistence across restart (User Story 3, P3)

1. Assign categories to 2–3 apps.
2. Force-stop MobyFocus (App info → Force stop) and reopen.
3. Dashboard shows every saved category exactly as assigned. ✅ SC-004, FR-006.

### S4 — Existing dashboard behavior unchanged

1. Rows remain sorted by usage time descending, zero-usage apps hidden,
   sub-minute usage shown as `<1 min`. ✅ SC-006.

### S5 — Graceful persistence failure (covered by tests)

Save/read failure paths are validated in `DetailViewModelTest` (failed save →
error surfaced, previous category stands) and `GetClassifiedAppUsageUseCaseTest`
(read failure → usage renders with Neutral fallback) rather than manually —
forcing a real SQLite failure on-device is not practical. ✅ SC-005, FR-010.

## Out-of-scope checks (must NOT appear)

- No new runtime permissions requested; no network calls introduced (FR-011,
  Constitution Principle I).
- No focus goals, notifications, summaries, or sync UI.
