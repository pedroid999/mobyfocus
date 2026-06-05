<!-- SPECKIT START -->
## Active Feature: 001-basic-usage-dashboard

**Plan**: `specs/001-basic-usage-dashboard/plan.md`
**Spec**: `specs/001-basic-usage-dashboard/spec.md`

First vertical slice: grant Android Usage Access, then show today's per-app
foreground usage on a Material 3 dashboard (sorted desc, zero-usage hidden,
sub-minute as `<1 min`).

**Stack**: Kotlin 2.2.10, Jetpack Compose + Material 3, Navigation Compose, Hilt,
Coroutines/`StateFlow`, `UsageStatsManager`. MVVM + Clean Architecture
(`domain` / `data` / `presentation` / `di`), single `:app` module,
namespace `com.pedroid.mobyfocus`, `minSdk 26`.

**Deferred (not this slice)**: Room, DataStore, WorkManager, Kotlinx
Serialization, Ktor/Retrofit — added only when a feature needs them (Constitution
Simplicity).

**Non-negotiables** (`.specify/memory/constitution.md`): (I) official APIs +
explicit permission only — no scraping/Accessibility/private APIs; (II) Strict
TDD — tests first, Red-Green-Refactor; (III) layered, `domain` free of Android;
(IV) idiomatic Compose UDF — stateless composables, immutable `UiState` via
`StateFlow`; (V) i18n-first — all UI text in `strings.xml`, English default.

For full technical context, read the plan and `research.md` / `data-model.md` /
`contracts/` under the feature directory.
<!-- SPECKIT END -->
