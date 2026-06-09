# Feature Specification: App Classification

**Feature Branch**: `002-app-classification`

**Created**: 2026-06-09

**Status**: Draft

**Input**: User description: "Allow the user to assign a persistent attention category to each analyzed app, so MobyFocus can later generate category-based insights, focus goals and summaries. Users open an app's detail screen from the usage dashboard, see its identity and today's usage, pick one of seven attention categories (Productive, Communication, Learning, Entertainment, Social, Distracting, Neutral), and the choice is saved locally and reflected on the dashboard. Unclassified apps default to Neutral."

## Clarifications

### Session 2026-06-09

- Q: ¿El cambio de categoría se guarda automáticamente al tocar la opción, o el usuario debe confirmar con un botón "Guardar"? → A: Auto-guardado — tocar una categoría la persiste inmediatamente, sin botón de confirmación.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Assign a category to an app (Priority: P1)

From the usage dashboard, the user taps an app they have used today. A detail
screen opens showing the app's icon (when available), display name, package
name, today's usage time, and its current attention category. The user selects
a different category from the fixed set of seven, the choice is saved, and when
they navigate back the dashboard row for that app shows the newly assigned
category.

**Why this priority**: This is the core value of the feature — without the
ability to assign and persist a category, nothing downstream (insights, goals,
summaries) is possible. It is the minimal viable slice.

**Independent Test**: Can be fully tested by opening the detail screen of any
listed app, selecting a category, returning to the dashboard, and confirming
the row reflects the chosen category. Delivers standalone value: the user has
labeled how that app relates to their attention.

**Acceptance Scenarios**:

1. **Given** the dashboard lists an app used today, **When** the user taps that
   app's row, **Then** a detail screen opens showing the app icon (if
   available), display name, package name, today's usage time, and the current
   category.
2. **Given** the detail screen is open for an app, **When** the user taps a
   category from the list of seven (Productive, Communication, Learning,
   Entertainment, Social, Distracting, Neutral), **Then** the selection is
   persisted immediately — with no separate confirmation step — and the detail
   screen shows the new category as current.
3. **Given** the user has just changed an app's category on the detail screen,
   **When** the user navigates back to the dashboard, **Then** that app's row
   displays the newly saved category without any manual refresh.
4. **Given** the detail screen is open, **When** the user navigates back
   without selecting a new category, **Then** the app's previously saved
   category (or the Neutral default) remains unchanged.

---

### User Story 2 - See every app's category at a glance (Priority: P2)

On the usage dashboard, each app row displays the app's current attention
category alongside its existing usage information. Apps the user has never
classified are shown as Neutral.

**Why this priority**: Visible categories turn individual classifications into
an at-a-glance attention picture and give the user feedback that their
classification work is preserved. It depends on classifications existing
(Story 1) but is independently testable with pre-seeded or default data.

**Independent Test**: Can be tested by opening the dashboard with a mix of
classified and never-classified apps and confirming every row shows a category,
with never-classified apps showing Neutral.

**Acceptance Scenarios**:

1. **Given** an app has a saved category, **When** the dashboard is displayed,
   **Then** that app's row shows the saved category.
2. **Given** an app has never been classified, **When** the dashboard is
   displayed, **Then** that app's row shows Neutral.
3. **Given** the dashboard is visible and a classification changes, **When**
   the change is saved, **Then** the dashboard reflects the new category
   automatically.

---

### User Story 3 - Classifications survive restarts and failures (Priority: P3)

A user who classified apps yesterday force-stops or restarts MobyFocus today
and still sees every saved category. If saving or loading classifications fails
for any reason, the app keeps working: the user is informed of a save failure,
and the dashboard falls back to safe defaults instead of crashing.

**Why this priority**: Durability and resilience are what make the
classification trustworthy, but they only matter once Stories 1 and 2 exist.

**Independent Test**: Can be tested by assigning categories, fully restarting
the app, and confirming the categories are still shown; and by simulating a
storage failure and confirming the app degrades gracefully.

**Acceptance Scenarios**:

1. **Given** the user has assigned categories to one or more apps, **When** the
   app is fully closed and reopened, **Then** every previously assigned
   category is displayed exactly as saved.
2. **Given** a save operation fails, **When** the user attempts to change a
   category, **Then** the app does not crash, the user is informed the save
   did not succeed, and the previously saved category remains in effect.
3. **Given** stored classifications cannot be read, **When** the dashboard is
   displayed, **Then** the dashboard still renders usage data and shows
   Neutral for affected apps.

---

### Edge Cases

- An app appears in today's usage but was uninstalled later today: the detail
  screen shows the package name and usage time, with a generic icon and the
  package name as a fallback display name.
- An app has a saved classification but no usage today: the classification is
  retained in storage; it simply is not visible until the app appears on the
  dashboard again (zero-usage apps stay hidden per existing dashboard rules).
- The user changes the same app's category several times in quick succession:
  the last selection wins and is the one persisted and displayed.
- Storage write fails mid-save: the user is informed, the previous category
  remains in effect, and the app continues to operate.
- Storage read fails on dashboard load: usage data still renders; affected
  apps display Neutral.
- The set of categories is fixed; a stored value that no longer matches a known
  category (e.g., from data corruption) is treated as Neutral.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST define exactly seven attention categories:
  Productive, Communication, Learning, Entertainment, Social, Distracting, and
  Neutral.
- **FR-002**: The system MUST identify each app by its package name for all
  classification purposes.
- **FR-003**: Users MUST be able to open an app usage detail screen by tapping
  an app row on the dashboard.
- **FR-004**: The detail screen MUST display the app icon when available, the
  app display name, the package name, today's usage time, and the app's
  current category.
- **FR-005**: Users MUST be able to change the selected app's category to any
  of the seven defined categories from the detail screen. Tapping a category
  saves it immediately; no separate confirmation action is required.
- **FR-006**: The system MUST persist the selected category locally on the
  device so it survives app restarts.
- **FR-007**: The dashboard MUST display each app's saved category on its row,
  and MUST reflect a newly saved category when the user returns from the
  detail screen without requiring a manual refresh.
- **FR-008**: Apps without a saved category MUST be presented as Neutral
  everywhere a category is shown.
- **FR-009**: All visible UI text introduced by this feature MUST come from
  externalized string resources (no hardcoded user-facing literals), with
  English as the default language.
- **FR-010**: The system MUST handle persistence failures gracefully: a failed
  save MUST inform the user and leave the previous category in effect; a
  failed read MUST NOT prevent the dashboard from rendering and MUST fall back
  to Neutral for affected apps.
- **FR-011**: Classification data MUST remain on-device; this feature MUST NOT
  transmit usage or classification data off the device.

### Key Entities

- **App Category**: One of a fixed, closed set of seven attention labels
  (Productive, Communication, Learning, Entertainment, Social, Distracting,
  Neutral). Neutral is the universal default.
- **App Classification**: The association between one app (identified by
  package name) and one App Category, plus the moment it was last updated.
  At most one classification exists per package name; saving again replaces
  the previous value.
- **Classified App Usage**: The combination of an app's existing usage
  information (identity, display name, icon availability, today's usage time)
  with its effective category (saved category, or Neutral when none exists).
  This is what both the dashboard rows and the detail screen present.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can assign a category to any listed app in 3 or fewer
  interactions starting from the dashboard (tap row → pick category → done).
- **SC-002**: 100% of apps shown on the dashboard display a category, and 100%
  of never-classified apps display Neutral.
- **SC-003**: After returning from the detail screen, the updated category is
  visible on the dashboard immediately, with no manual refresh action.
- **SC-004**: Assigned categories survive a full app restart in 100% of cases
  under normal device conditions.
- **SC-005**: A persistence failure never crashes the app: the user receives
  feedback on a failed save, and the dashboard renders usage data even when
  classifications cannot be read.
- **SC-006**: Existing dashboard behavior (sorting by usage descending, hiding
  zero-usage apps, sub-minute display) is unchanged by this feature.

## Assumptions

- Classification is per-device and per-user; no account, sync, or backup of
  classifications is in scope.
- The seven categories are fixed product vocabulary for this feature; users
  cannot create, rename, or remove categories.
- Each app holds exactly one category at a time (no multi-category tagging).
- Re-assigning a category overwrites the previous one; no history of past
  classifications is kept beyond the last-updated moment.
- The detail screen is reached only from the dashboard in this feature; other
  entry points (search, deep links) are out of scope.
- Today's usage time shown on the detail screen follows the same definition
  and formatting rules as the existing dashboard (foreground time, `<1 min`
  for sub-minute values).
- The existing usage-access permission flow is unchanged; this feature assumes
  usage data is already available when the dashboard is shown.
- Out of scope, per the feature description: focus goals, notifications,
  Spotify integration, OAuth, deep usage integrations, daily summaries, cloud
  sync, accessibility services, and any scraping or unofficial APIs.
- Detailed technical direction provided with the feature request (storage
  technology, package layout, component names) is recorded for the planning
  phase and intentionally excluded from this specification.
