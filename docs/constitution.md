<!--
SYNC IMPACT REPORT
==================
Version change: TEMPLATE (unversioned) → 1.0.0
Bump rationale: Initial ratification. Template placeholders replaced with the
  five concrete, project-specific principles and governance for MobyFocus.
  First adoption = MAJOR baseline 1.0.0 (no prior version to compare).

Principles (initial set):
  - I. Ethical & Privacy-First Data Collection (NON-NEGOTIABLE)
  - II. Test-First Development — Strict TDD (NON-NEGOTIABLE)
  - III. Layered Architecture (MVVM + Clean Architecture)
  - IV. Idiomatic Compose UI (Unidirectional Data Flow)
  - V. Internationalization-First

Added sections:
  - Technology & Platform Constraints
  - Development Workflow & Quality Gates
  - Governance

Removed sections: none (template placeholders replaced in place).

Templates requiring updates:
  - ✅ .specify/templates/plan-template.md — Constitution Check gate is dynamic
       (reads this file); no hardcoded principle drift. No edit required.
  - ✅ .specify/templates/spec-template.md — constitution adds no new mandatory
       spec sections; existing structure remains aligned. No edit required.
  - ⚠ → ✅ .specify/templates/tasks-template.md — updated: tests changed from
       OPTIONAL to MANDATORY to comply with Principle II (Strict TDD).
  - ✅ .specify/templates/checklist-template.md — generic; no principle drift.

Follow-up TODOs: none. RATIFICATION_DATE set to first adoption date 2026-06-05.
-->

# MobyFocus Constitution

MobyFocus is a native Android application that helps developers and knowledge
workers understand how they spend their attention across mobile apps. Because
the product reads sensitive device-usage data, its integrity depends on a small
set of non-negotiable engineering and ethical principles. This constitution
governs every feature, plan, and task in the project.

## Core Principles

### I. Ethical & Privacy-First Data Collection (NON-NEGOTIABLE)

Usage data MUST be obtained only through official, sanctioned Android APIs
(e.g., `UsageStatsManager`) after the user has granted explicit permission.

- The app MUST NOT use screen scraping, Accessibility Service content
  extraction, unofficial/private APIs, reflection into hidden framework
  internals, or any invasive technique to gather data.
- Every data-collecting capability MUST first detect and respect its required
  permission, and MUST degrade gracefully (clear explanation + path to grant)
  when permission is absent.
- Collected data MUST stay on-device by default; any future export, sync, or
  third-party integration MUST be opt-in and explicitly justified in its spec.

**Rationale**: An attention-analysis app lives or dies by user trust. Invasive
data gathering is both an ethical breach and a Play Store / legal liability.
This principle is the product's reason to exist responsibly — it is absolute.

### II. Test-First Development — Strict TDD (NON-NEGOTIABLE)

Tests are written BEFORE implementation. The Red-Green-Refactor cycle is
strictly enforced.

- For every unit of business logic — use cases, repositories, mappers, and
  ViewModel state transitions — a failing test MUST exist and be observed to
  FAIL before any production code is written.
- Implementation proceeds only to make failing tests pass; refactoring happens
  only under green tests.
- Tests MUST NOT be retroactively written to match already-shipped code. A task
  that produces logic without a preceding test is non-compliant and MUST be
  reworked.
- UI behavior that carries logic (state mapping, navigation decisions) is
  covered by Compose UI / instrumented tests; pixel-only styling is exempt.

**Rationale**: TDD is the only reliable guard against regressions in a codebase
that will grow toward Room persistence, classification, goals, and integrations.
Writing tests first forces testable design — the exact MVVM/Clean separation
Principle III demands.

### III. Layered Architecture (MVVM + Clean Architecture)

Code MUST be organized into clearly separated layers with dependencies pointing
inward toward the domain.

- **domain** (`domain.model`, `domain.repository`, `domain.usecase`): pure
  Kotlin, ZERO Android framework imports. Holds models, repository interfaces,
  and use cases.
- **data** (`data.*`): implements domain repository interfaces; owns data
  sources, mappers, and framework/SDK access (UsageStatsManager, Room,
  DataStore).
- **presentation** (`presentation.*`): ViewModels + Composables, organized by
  feature (e.g., `presentation.permission`, `presentation.dashboard`).
- Business logic MUST NOT live in Composables. The UI layer depends on domain
  abstractions, never the reverse.
- Dependency injection is provided via Hilt (`di` package); layers wire through
  interfaces, not concrete constructors.

**Rationale**: A vertical-slice app that intends to add classification, focus
goals, notifications, and deep integrations needs seams from day one. Clean
layering keeps the domain free of Android so logic is unit-testable per
Principle II and features compose without entanglement.

### IV. Idiomatic Compose UI (Unidirectional Data Flow)

The UI MUST follow Jetpack Compose best practices and a unidirectional data
flow (UDF).

- ViewModels expose a single immutable `UiState` via `StateFlow`; the UI
  observes state and emits events upward. State flows down, events flow up.
- Composables MUST be stateless wherever practical (state hoisting); a stateless
  composable receives state + callbacks and renders without owning mutable
  business state.
- Material 3 is the component baseline. Screens MUST support dynamic theming and
  dark mode, and interactive elements MUST provide accessibility semantics
  (e.g., `contentDescription`).
- Async work uses Coroutines and `Flow`/`StateFlow`; no blocking calls on the
  main thread.

**Rationale**: UDF with immutable state is what makes ViewModel transitions
deterministic and therefore testable (Principle II). Stateless composables are
reusable and previewable; logic-in-UI is technical debt from the first commit.

### V. Internationalization-First

All user-facing text MUST be externalized for translation from the first line
of UI code.

- Visible UI strings MUST come from Android string resources
  (`res/values/strings.xml`); hardcoded user-facing literals in Composables are
  prohibited.
- The default language is English (`values/`), with the resource structure ready
  for additional locales without code changes.
- Formatting that varies by locale (numbers, durations, dates) MUST use
  locale-aware APIs rather than manual string concatenation.

**Rationale**: Retrofitting i18n is expensive and error-prone. Building it in
from the start costs almost nothing and keeps the product globally shippable —
a stated product requirement, not an afterthought.

## Technology & Platform Constraints

- **Language/Build**: Kotlin (2.2.x) with Gradle Kotlin DSL and the version
  catalog (`gradle/libs.versions.toml`) as the single source of dependency
  truth.
- **UI**: Jetpack Compose + Material 3 + Navigation Compose.
- **Async**: Coroutines, `Flow`/`StateFlow`.
- **DI**: Hilt. **Persistence**: Room + DataStore. **Background**: WorkManager.
- **Serialization**: Kotlinx Serialization. **Networking (future)**: Ktor or
  Retrofit — not introduced until a feature requires it.
- **Platform**: `minSdk 26`, native single-module `:app` (modularization may be
  introduced later only when justified).
- New libraries MUST be added through the version catalog and justified against
  Principle "Simplicity": no dependency without a concrete, present need.

## Development Workflow & Quality Gates

- **Spec-Driven**: Substantial work flows through Spec Kit — `specify` → `plan`
  → `tasks` → `implement`. Each `plan` MUST pass the Constitution Check gate
  before research begins and again after design.
- **TDD Gate**: A change is not "done" until its tests existed first, fail
  without the implementation, and pass with it. The compile-and-test gate MUST
  be green before merge.
- **Architecture Gate**: Reviews MUST reject Android imports in `domain`,
  business logic inside Composables, and mutable shared UI state outside an
  immutable `UiState`.
- **Privacy Gate**: Any new data access MUST be reviewed against Principle I
  (official APIs + explicit permission only) before implementation.
- **Simplicity**: Complexity MUST be justified. Prefer the simplest design that
  satisfies the spec; record any deviation in the plan's Complexity Tracking.

## Governance

This constitution supersedes all other development practices. When guidance
conflicts, the constitution wins.

- **Amendments**: Changes to this document MUST be proposed with rationale,
  reviewed, and accompanied by a version bump and propagation to dependent
  templates (`plan`, `spec`, `tasks`, `checklist`).
- **Versioning Policy** (semantic):
  - **MAJOR**: Removal or backward-incompatible redefinition of a principle or
    governance rule.
  - **MINOR**: A new principle/section is added, or existing guidance is
    materially expanded.
  - **PATCH**: Clarifications, wording, or non-semantic refinements.
- **Compliance Review**: Every PR and `/speckit-plan` Constitution Check MUST
  verify compliance. Violations either block the change or are explicitly
  justified in Complexity Tracking; unjustified complexity is rejected.
- **Runtime Guidance**: Agent and contributor guidance lives in `CLAUDE.md` and
  the active plan; those documents MUST stay consistent with this constitution.

**Version**: 1.0.0 | **Ratified**: 2026-06-05 | **Last Amended**: 2026-06-05
