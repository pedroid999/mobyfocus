# Feature Specification: Basic Usage Dashboard

**Feature Branch**: `001-basic-usage-dashboard`

**Created**: 2026-06-05

**Status**: Draft

**Input**: User description: "Allow the user to grant Android Usage Access permission and then display today's app usage grouped by installed apps. MobyFocus is an attention and app-usage analysis app for developers and knowledge workers."

## Clarifications

### Session 2026-06-05

- Q: Which metric represents each app's "usage time" today? → A: Foreground time (active in-app time, `totalTimeInForeground`), available on all `minSdk 26+` devices.
- Q: Should the dashboard show an explicit loading state while usage is fetched? → A: Yes — model an explicit Loading state; transitions are Loading → Content / Empty / Error.
- Q: How is an app with more than 0 but less than 1 minute of usage displayed? → A: Show the label "<1 min" (never "0 min"); ordering uses the precise duration.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Grant Usage Access through a clear explanation (Priority: P1)

A first-time user opens MobyFocus. Because the app has not yet been granted
permission to read usage data, the user is shown a screen that explains, in
plain language, why the app needs Usage Access and what it will do with it. The
user taps a button that takes them directly to the system screen where access is
granted, then returns to MobyFocus, which now recognizes that access has been
granted.

**Why this priority**: Without granted access there is no data to analyze. This
is the entry gate to all value the product offers, and a trustworthy, honest
explanation is core to the product's promise of respecting user privacy.

**Independent Test**: With access NOT granted, launch the app and confirm the
explanation screen appears, clearly states the reason, and offers a button that
opens the system Usage Access settings. On returning to the app, confirm the app
re-evaluates and reflects the new permission state.

**Acceptance Scenarios**:

1. **Given** Usage Access has not been granted, **When** the user opens the app,
   **Then** an explanation screen is shown describing why access is needed.
2. **Given** the explanation screen is shown, **When** the user taps the action
   button, **Then** the system Usage Access settings screen is opened.
3. **Given** the user granted access in system settings, **When** they return to
   the app, **Then** the app re-checks the permission and proceeds to the usage
   dashboard.
4. **Given** the user returned without granting access, **When** the app
   re-checks, **Then** the explanation screen remains visible.

---

### User Story 2 - View today's app usage on the dashboard (Priority: P1)

A user who has granted Usage Access opens MobyFocus and sees a dashboard listing
the apps they have used today, each showing the app's icon (when available), its
display name, its package name, and how many minutes they have spent in it today.
The list is ordered from most-used to least-used so the heaviest attention sinks
are immediately visible.

**Why this priority**: This is the core insight the product exists to deliver —
a clear, honest picture of where today's attention went.

**Independent Test**: With access granted and usage present for the day, open the
app and confirm the dashboard lists used apps with name, package name, and
minutes, sorted by descending usage, excluding apps with no usage today.

**Acceptance Scenarios**:

1. **Given** access is granted and the user has used several apps today,
   **When** the dashboard loads, **Then** it lists those apps with their display
   name, package name, and total minutes used today.
2. **Given** the dashboard is displayed, **When** the user reviews the list,
   **Then** entries are ordered by usage time, highest first.
3. **Given** an app has zero usage today, **When** the dashboard loads, **Then**
   that app is not shown.
4. **Given** an app's icon is unavailable, **When** its entry is shown, **Then**
   the entry still displays correctly using a fallback visual.

---

### User Story 3 - Resilient experience with no data or errors (Priority: P2)

A user opens the dashboard on a day with no recorded usage, or when the usage
data cannot be read. Instead of a blank or broken screen, the user sees a clear,
friendly message explaining the situation.

**Why this priority**: Trust and polish. Empty and error states are common on
first launch and must never look like a crash, but they do not block the core
value once data exists.

**Independent Test**: Simulate an empty usage result and an error condition;
confirm each shows an appropriate, human-readable state rather than a blank list
or failure.

**Acceptance Scenarios**:

1. **Given** the dashboard is opening and usage is still being read, **When** the
   screen is shown, **Then** a loading state is displayed until data resolves.
2. **Given** access is granted but no usage is recorded for today, **When** the
   dashboard loads, **Then** a friendly empty-state message is shown.
3. **Given** the usage data cannot be retrieved, **When** the dashboard attempts
   to load, **Then** a clear error message is shown instead of a broken screen.

### Edge Cases

- **Day boundary**: "Today" is defined as the device's local calendar day from
  midnight to the current moment; usage is measured within that window.
- **Sub-minute usage**: Apps used for more than zero but less than one full
  minute are still considered "used today" and are not hidden as zero usage;
  they are displayed with the label "<1 min" (never "0 min").
- **Permission revoked while running**: If access is revoked after being granted,
  the next permission re-check returns the user to the explanation screen.
- **Many apps used**: The dashboard remains readable and scrollable when dozens
  of apps have usage today.
- **Returning from settings without action**: Re-checking permission must not
  crash or loop if the user backs out of settings without changing anything.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST detect whether Usage Access has been granted.
- **FR-002**: When Usage Access is not granted, the system MUST present a
  permission explanation screen.
- **FR-003**: The explanation screen MUST clearly state why the app needs Usage
  Access and how the data is used.
- **FR-004**: The explanation screen MUST provide an action that opens the system
  Usage Access settings screen.
- **FR-005**: When the user returns to the app, the system MUST re-evaluate the
  current permission status.
- **FR-006**: When Usage Access is granted, the system MUST read today's app
  usage through the official Android usage-data facility only.
- **FR-007**: The system MUST present each used app with: its icon when
  available, its display name, its package name, and its total foreground usage
  time today (active in-app time) expressed in minutes.
- **FR-008**: The system MUST order the dashboard list by total usage time,
  highest first.
- **FR-009**: The system MUST exclude apps with zero usage time today from the
  dashboard.
- **FR-010**: The system MUST present a clear empty state when no usage exists
  for today.
- **FR-010a**: While usage data is being retrieved, the system MUST present a
  loading state to the user; the dashboard transitions from loading to content,
  empty, or error.
- **FR-011**: The system MUST present a clear error state when usage data cannot
  be retrieved, without crashing.
- **FR-012**: All user-visible text MUST be sourced from localizable string
  resources; no user-facing text may be hard-coded in the UI. The default
  language is English.
- **FR-013**: The system MUST NOT obtain usage data through screen scraping,
  Accessibility Service content extraction, unofficial/private APIs, or any
  invasive technique. (Constitution Principle I — non-negotiable.)

### Key Entities *(include if feature involves data)*

- **App Usage**: A single app's attention summary for today — identified by its
  package name, with a human-readable display name, an optional icon, and a total
  foreground usage duration (active in-app time) for the current day.
- **Usage Access Permission Status**: Whether the app currently holds permission
  to read device usage data (granted or not granted), used to decide whether to
  show the explanation screen or the dashboard.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A first-time user who has not granted access always sees the
  explanation screen rather than an empty or broken dashboard (100% of launches
  without permission).
- **SC-002**: From the explanation screen, a user can reach the system settings
  to grant access in a single tap.
- **SC-003**: After granting access and returning to the app, the user reaches
  the usage dashboard without restarting the app.
- **SC-004**: When usage exists, the dashboard displays today's used apps sorted
  from highest to lowest usage, with no zero-usage entries present.
- **SC-005**: Every visible label, message, and button text can be translated
  without changing application logic (zero hard-coded user-facing strings).
- **SC-006**: Empty-data and error conditions each present a readable message;
  neither results in a blank screen or a crash.

## Assumptions

- "Today" is the device's local calendar day (midnight to now); no custom
  reporting window is offered in this feature.
- All apps reported by the official usage-data facility are eligible for display,
  including system and pre-installed apps, provided they have non-zero usage
  today. No allow/deny filtering is part of this feature.
- Usage time of one minute or more is displayed rounded to whole minutes;
  usage greater than zero but under one minute is displayed as "<1 min". The
  underlying ordering uses the precise duration so two apps that display the same
  value still sort correctly.
- The feature reads usage data on demand when the dashboard is shown; persistent
  storage, background collection, and historical (multi-day) views are out of
  scope for this slice.
- This feature does not implement any third-party integration (e.g., Spotify
  OAuth); the experience is self-contained on the device.
