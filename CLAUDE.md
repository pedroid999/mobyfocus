<!-- SPECKIT START -->
## Active Feature: 002-app-classification

**Plan**: `specs/002-app-classification/plan.md`
**Spec**: `specs/002-app-classification/spec.md`

Second vertical slice: assign one of seven fixed attention categories
(Productive, Communication, Learning, Entertainment, Social, Distracting,
Neutral) to any app on the usage dashboard. Tapping a row opens a detail
screen (icon, name, package, today's usage, current category); tapping a
category auto-saves it to a new Room database. Dashboard rows show the
effective category reactively (Neutral default when unclassified).

**Stack**: Kotlin 2.2.10, Jetpack Compose + Material 3, Navigation Compose, Hilt
2.59.2 (KSP), Coroutines/`StateFlow`, `UsageStatsManager`, **Room (new this
slice)** — first persistence: `MobyFocusDatabase` v1, table
`app_classifications`, schema export to `app/schemas/`. MVVM + Clean
Architecture (`domain` / `data` / `presentation` / `di`), single `:app` module,
namespace `com.pedroid.mobyfocus`, `minSdk 26`. AGP 9.2.1 built-in Kotlin:
`android.disallowKotlinSourceSets=false` already set in `gradle.properties`.

**Deferred (not this slice)**: DataStore, WorkManager, Kotlinx Serialization,
Ktor/Retrofit — added only when a feature needs them (Constitution Simplicity).

**Non-negotiables** (`.specify/memory/constitution.md`): (I) official APIs +
explicit permission only — no scraping/Accessibility/private APIs; (II) Strict
TDD — tests first, Red-Green-Refactor; (III) layered, `domain` free of Android;
(IV) idiomatic Compose UDF — stateless composables, immutable `UiState` via
`StateFlow`; (V) i18n-first — all UI text in `strings.xml`, English default.

For full technical context, read the plan and `research.md` / `data-model.md` /
`contracts/` under the feature directory.
<!-- SPECKIT END -->
