# MobyFocus

A native Android app that helps developers and knowledge workers understand how
they spend their attention across mobile apps. MobyFocus reads on-device usage
data through official Android APIs only (`UsageStatsManager`), shows a daily
usage dashboard, and lets you classify each app into one of seven attention
categories (Productive, Communication, Learning, Entertainment, Social,
Distracting, Neutral).

**Privacy first**: all data stays on the device. No network calls, no
analytics, no accessibility-service tricks, no scraping. Ever.

---

## Quick start

```bash
git clone https://github.com/pedroid999/mobyfocus.git
cd mobyfocus
./gradlew :app:assembleDebug          # builds the debug APK
./gradlew :app:installDebug           # installs on a connected device/emulator
```

That's it — the Gradle wrapper downloads everything else.

### Requirements

| Tool | Version |
|------|---------|
| JDK | 17 or newer (Gradle 9 requirement) |
| Android SDK | compileSdk 36 (installed automatically by Android Studio) |
| Android Studio | Latest stable (Ladybug or newer recommended for AGP 9) |
| Device / emulator | API 26+ (Android 8.0) |

No API keys, no secrets, no backend — the project builds out of the box.

### Run it on your device

1. Enable **Developer options** and **USB debugging** on your phone.
2. Plug it in and check it shows up: `adb devices`
3. Install: `./gradlew :app:installDebug`
   - With multiple devices connected (e.g. phone + emulator), target one:
     `ANDROID_SERIAL=<serial-from-adb-devices> ./gradlew :app:installDebug`
4. Open MobyFocus. The app asks for **Usage Access** and sends you to system
   settings — grant it and come back. The dashboard appears automatically.

> **Shortcut**: grant Usage Access from the terminal instead of Settings:
> ```bash
> adb shell appops set com.pedroid.mobyfocus android:get_usage_stats allow
> ```

If the dashboard is empty, the device simply has no recorded app usage today —
use a couple of apps for a minute and reopen.

---

## Tech stack

- **Kotlin** 2.2.10 on **AGP** 9.2.1 (built-in Kotlin toolchain — there is no
  `org.jetbrains.kotlin.android` plugin; see [Troubleshooting](#troubleshooting))
- **Jetpack Compose** + **Material 3** (BOM-managed), **Navigation Compose**
- **Hilt** for DI (KSP compiler)
- **Coroutines / Flow / StateFlow** for async and reactive state
- **Room** for persistence (`MobyFocusDatabase`, schema exported to `app/schemas/`)
- **JUnit4 + MockK + Turbine** for JVM tests; **Compose UI Test + Espresso**
  for instrumented tests
- Single `:app` module; dependency versions live in `gradle/libs.versions.toml`
  (the version catalog is the single source of dependency truth)

## Architecture

MVVM + Clean Architecture with dependencies pointing inward:

```
app/src/main/java/com/pedroid/mobyfocus/
├── domain/          # Pure Kotlin — ZERO Android imports
│   ├── model/       #   AppUsage, AppCategory, AppClassification, ...
│   ├── repository/  #   Interfaces only
│   └── usecase/     #   Business logic, one class per use case
├── data/            # Implements domain interfaces; owns framework access
│   ├── local/       #   Room: database, DAOs, entities
│   ├── mapper/      #   entity/raw ↔ domain mapping
│   ├── repository/  #   Repository implementations
│   └── usage/       #   UsageStatsManager data source
├── di/              # Hilt modules (database, repositories, dispatchers)
├── presentation/    # ViewModels + Composables, organized by feature
│   ├── permission/  #   Usage-access onboarding
│   ├── dashboard/   #   Today's usage list with categories
│   ├── detail/      #   Per-app detail + category picker (auto-save)
│   ├── common/      #   Shared UI helpers (icons, labels, formatters)
│   └── navigation/  #   NavHost and routes
└── ui/theme/        # Material 3 theme
```

Ground rules (enforced in review — see the constitution below):

- `domain` never imports Android or Room types.
- Business logic never lives inside Composables.
- ViewModels expose a single immutable `UiState` via `StateFlow`; screens are
  stateless (state down, events up).
- All user-facing text comes from `res/values/strings.xml` (i18n-first).

## Testing

**Strict TDD is non-negotiable in this project**: every unit of business logic
(use cases, repositories, mappers, ViewModel state transitions) gets a failing
test *before* its implementation. PRs with untested logic get reworked.

```bash
./gradlew :app:testDebugUnitTest           # JVM unit tests (fast, no device)
./gradlew :app:connectedDebugAndroidTest   # Room DAO + Compose UI tests (device/emulator)
./gradlew :app:lintDebug                   # static checks
```

All three must be green before merging. Instrumented tests need a device or
emulator on API 26+; the Room DAO tests run against an in-memory database.

## Development workflow

The project is **spec-driven** (GitHub Spec Kit). Substantial changes flow
through `specify → clarify → plan → tasks → implement`, and each feature keeps
its full artifact trail under `specs/<NNN-feature-name>/` (spec, plan,
research, data model, contracts, quickstart, tasks).

The non-negotiable engineering principles live in
`.specify/memory/constitution.md`:

1. **Ethical & privacy-first data collection** — official APIs + explicit
   permission only; data stays on-device.
2. **Test-first development** — strict Red-Green-Refactor.
3. **Layered architecture** — MVVM + Clean, domain free of Android.
4. **Idiomatic Compose** — UDF, immutable state, stateless composables.
5. **Internationalization-first** — no hardcoded user-facing strings.

Branches follow `NNN-feature-name` for spec-kit features (e.g.
`002-app-classification`) or `type/description` for standalone changes
(e.g. `docs/developer-readme`). Commits use
[Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`,
`build:`, `docs:`, ...). PRs target `main`.

## Troubleshooting

**Compose UI tests crash with `NoSuchMethodException: InputManager.getInstance`**
→ You downgraded Espresso. Espresso < 3.6 reflects on a framework method
removed in newer Android and breaks *all* Compose tests on API 35+ images.
Keep `espressoCore = 3.7.0` (or newer) in the version catalog.

**KSP/Hilt/Room build errors after touching the toolchain**
→ AGP 9 uses the built-in Kotlin toolchain. Two things make KSP processors
work here: Hilt ≥ 2.59 and `android.disallowKotlinSourceSets=false` in
`gradle.properties`. Don't remove either.

**`Installed on 2 devices` or wrong install target**
→ Multiple devices connected. Prefix Gradle commands with
`ANDROID_SERIAL=<serial>`.

**Dashboard never appears, stuck on the permission screen**
→ Usage Access wasn't granted. Settings → Special app access → Usage access →
MobyFocus → Allow (or use the `appops` shortcut above).

## License

No license has been declared yet — all rights reserved by default. Open an
issue if you need clarification before reusing the code.
