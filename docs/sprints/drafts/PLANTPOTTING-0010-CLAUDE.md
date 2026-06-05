# PLANTPOTTING-0010 — App-experience sprint (UI/UX refresh + "Add this plant" wireframe + KB expansion)

> Plan draft. Diverges deliberately from the roadmap's planned Next (the pothos↔Pilea boundary
> fix), which **stays deferred**. Refined intent: `docs/sprints/drafts/PLANTPOTTING-0010-INTENT.md`.
> Every concrete piece of work below is a `- [ ]` checkbox.

---

## 1. Goals

A three-pillar "make the app feel like an app" sprint, all behind the frozen identification seam and
the network-free gate:

- **Pillar A — UI/UX refresh:** pickable theme candidates (delivered as screenshots + debug APK),
  numeric+bar confidence display, a **"My Plants"** history backed by the app's **first
  local-persistence layer**, containment of the species search list, and a license-clean **reference
  image** per plant.
- **Pillar B — "Add this plant" wireframe:** when the model is *strongly* confident about a class that
  has **no KB entry**, surface it and an **"Add this plant"** button that **locally logs/totals the
  request** (wireframe — no KB row is added by the button itself).
- **Pillar C — KB expansion:** continue the **text-only** expansion over the popular slice of the
  remaining 21 unmapped model classes (no ML, no self-shot), Pilea still deliberately unmapped.

Single unifying architectural deliverable: **one** local-persistence layer shared by A3 (My Plants)
and B (request log) — designed once, not twice.

## 2. Non-goals (hold the line)

- [ ] **Do NOT** touch the `PlantIdentifier` / `IdentificationResult` contract
      (`app/src/main/java/com/darkfactory/plantpotting/identify/PlantIdentifier.kt`). Frozen per
      0003 §4.4. New data rides on **side-channels** (the existing `CandidateProvider`, a new sibling
      marker, or nav args) — never on the seam.
- [ ] **Do NOT** add any networking dependency or runtime fetch. `verifyNoNetworking` (app
      `build.gradle.kts`) and `scripts/check-stub-isolation.sh` stay GREEN. All theme + image assets
      are **bundled**.
- [ ] **No model training, no fine-tune, no self-shot / first-party plant imagery.** (App-UI
      screenshots of candidate themes are renders, not plant data — those are fine.)
- [ ] **Do NOT** map Pilea (`Chinese Money Plant (Pilea peperomioides)`) — the CI-enforced
      Pilea-absence guard in `HousePlantClassMapValidationTest` stays GREEN. The pothos↔Pilea boundary
      fix stays deferred.
- [ ] **Do NOT** bump AGP / Kotlin / Compose / Hilt / TFLite versions (roadmap standing non-goal).
      Adding a *new* local-only library (DataStore) is permitted; version *bumps* of the locked stack
      are not.
- [ ] No cloud sync, no export UI for the request log (the principal collects totals later, off-device).
- [ ] The "Add this plant" button does **not** write a KB species row in this sprint — it only logs.

## 3. Scope boundaries / key architectural decisions

These are the decisions the rest of the plan depends on. Resolve them first.

### 3.1 Shared persistence layer (blocks A3 + B)
- New package `com.darkfactory.plantpotting.persistence`.
- **Recommended:** **DataStore** (`androidx.datastore:datastore-preferences` *or* typed
  `androidx.datastore:datastore` + a `@Serializable` document) over Room — it's local-file by
  construction (trivially keeps `verifyNoNetworking` GREEN), reuses the existing
  `kotlinx.serialization.json` dependency, and needs no KSP schema/migration ceremony for two small
  collections. Room is the considered alternative but is heavier than two append-mostly lists warrant.
- One store, two logical collections:
  - `identifiedPlants` → feeds **My Plants** (A3): `{speciesId, displayName, source, confidencePct,
    capturedAtEpochMs}`.
  - `addPlantRequests` → feeds the **request log** (B): `{modelClassLabel, count, lastRequestedEpochMs}`
    totalled per model class.
- Bound growth (cap `identifiedPlants` to a sane N, e.g. most-recent 100) so the store can't grow
  unbounded.
- **Timestamps:** inject a `Clock`/time provider (do not call `System.currentTimeMillis()` inline) so
  the store is unit-testable with a fake clock.

### 3.2 Confidence display without touching the seam (A2)
- `IdentificationResult` carries **no** numeric probability — but `OnDevicePlantIdentifier` already
  exposes `CandidateProvider.mostRecentCandidates`, and `Candidate.probability` is the raw softmax
  (0..1). On the **high-confidence** path, `CameraViewModel.onCaptureReady` already reads the
  identifier as `CandidateProvider`; read `mostRecentCandidates.firstOrNull()?.probability` there and
  thread it through as a **new optional nav arg** `confidencePct` on `Routes.RESULT` (exactly mirroring
  how candidates already ride the low-confidence route). `ResultScreen` renders it.
- **Subtlety to handle:** in the margin-confidence branch of `ModelScoreMapper`, the winning class is
  still `ranked[0]`, so the first *mapped* candidate's probability tracks `bestProb` — assert this in a
  test; if a future mapping makes them diverge, prefer the winning species' own score. Default arg
  absent (stub flows) → render no percentage/bar gracefully.

### 3.3 "Add this plant" routing seam (B)
- **Problem:** `ModelScoreMapper.candidateForIndex` returns `null` for unmapped labels, so a
  *high-confidence but unmapped* class is currently invisible — it collapses into the low-confidence
  verdict and routes to `LowConfidencePicker` with the raw label dropped.
- **Fix (off the frozen seam):** extend the internal `MappedScore` with the **raw top label + raw top
  probability + a `topIsMapped` flag**, and expose it via a **new side-channel** (either widen
  `CandidateProvider` or add a sibling marker e.g. `UnmappedTopProvider`). `MappedScore` and the marker
  interface are **not** the frozen seam — only `PlantIdentifier`/`IdentificationResult` are.
- **Routing decision (document explicitly):** in `CameraViewModel`, when the identifier returns
  `lowConfidence == true` **but** raw-top probability ≥ the high-confidence threshold **and**
  `topIsMapped == false` → emit a new `NavCommand.AddPlant(modelClassLabel, confidencePct)` →
  `AddThisPlantScreen`. Otherwise the existing `NavCommand.LowConfidence` → `LowConfidencePicker`
  behaviour is unchanged. (I.e. "Add this plant" carves the *confident-but-unmapped* slice **out of**
  today's low-confidence path; weak/ambiguous predictions still go to the picker.)

### 3.4 Reference images: licensing × APK size (A5)
- Source **CC0 / public-domain** images only (avoid CC-BY-NC entirely — 0008 found CC-BY-NC dominates
  and is unusable; even CC-BY adds per-image attribution burden — prefer CC0/PD). Wikimedia Commons /
  PD botanical plates are good sources. **No self-shot imagery.**
- Bundle as **downscaled WebP** under `app/src/main/res/drawable-nodpi/` (render with
  `painterResource` — **no image-loading library / Coil**, avoiding a dependency *and* keeping it
  network-free).
- **APK-size guard:** set a per-image budget (e.g. ≤ ~40 KB WebP) and a total budget; if license-clean
  images aren't available for all ~32 species, ship images for the **popular subset** and a
  **placeholder vector** (reuse/extend an existing drawable) for the rest — do **not** block the sprint
  on full coverage.
- Add a checked-in attribution/license manifest (`docs/licenses/reference-images.md`) listing every
  bundled image, its source URL, and its CC0/PD license — plus a test/asset cross-check that every
  bundled reference image has a manifest entry.

### 3.5 Theme candidates → pickable deliverable (A1)
- Define **2–3** candidate palettes/`ColorScheme`s (+ any typography) as a `ThemeCandidate` enum in
  `ui/theme/Color.kt`; parameterise `PlantPottingTheme(candidate = …)` in `ui/theme/Theme.kt` (keep the
  existing scheme as one candidate / the default).
- Deliver **pickable candidates, not a silent winner:** ship a **debug-only in-app theme switcher**
  (persisted via the new DataStore — nice reuse) so **one** debug APK lets the principal flip between
  all candidates on-device and pick. Production default stays the current theme until the principal
  chooses.
- Capture **screenshots** of each candidate (Compose `@Preview` screenshot test, a GMD-driven capture,
  or a manual run) and place them under `evidence/PLANTPOTTING-0010/themes/` for the hand-off.

## 4. Task list

### Phase 0 — Shared persistence foundation (unblocks A3 + B)
- [ ] Add `androidx.datastore` to `gradle/libs.versions.toml` + `app/build.gradle.kts` (local-only; no
      networking transitive).
- [ ] Confirm `./gradlew verifyNoNetworking` stays GREEN with the new dependency on
      `releaseRuntimeClasspath` (extend the forbidden-substring audit reasoning if needed).
- [ ] Create `persistence/` package: `@Serializable` DTOs (`IdentifiedPlant`, `AddPlantRequest`), a
      `PlantLogStore` interface, and a `DataStorePlantLogStore` implementation (reads/writes one
      DataStore document; reuses `kotlinx.serialization.json`).
- [ ] Implement an injectable time provider (e.g. `TimeProvider`/`Clock`) and use it for all timestamps.
- [ ] Add `PersistenceModule` (Hilt) binding `PlantLogStore` (`@Singleton`, app-context DataStore).
- [ ] Bound `identifiedPlants` length (most-recent-N cap) on write.
- [ ] Unit tests: append/read round-trip, request-counter increment+total, N-cap eviction, fake-clock
      timestamps, empty-store cold read.

### Phase 1 — Pillar C: text-only KB expansion (independent, low-risk; can start early/parallel)
- [ ] Pick the popular slice of the remaining 21 unmapped model classes (exclude Pilea); record the
      rationale + license-clean citations in `docs/kb/ml-mapping-notes.md`.
- [ ] Append new species rows to `app/src/main/assets/kb/species.json` (append-only; do not mutate
      existing rows; reuse existing archetypes where possible).
- [ ] Add the corresponding verbatim-label rows to
      `app/src/main/assets/ml/house_plant_species_mobilenetv2/plant_class_map.json` (mark coarse/genus
      rows with `alias:true` + `_note`, matching 0009 convention).
- [ ] Update count assertions in `HousePlantClassMapValidationTest` (mapped count), `KbContentSpeciesTest`
      (species count), and any archetype-count asserts — keep the **Pilea-absence** and existing-row
      regression guards intact.
- [ ] Run `KbValidationTest` / KB content tests; confirm sum-to-100 recipe + toxicity content vetted for
      each new species.

### Phase 2 — Pillar A polish on existing screens
- [ ] **A2 Confidence display:** add optional `confidencePct` nav arg to `Routes.RESULT` +
      `PlantPottingNavHost` + `ResultViewModel`/`ResultUiState`; read the top candidate's probability in
      `CameraViewModel` success path and pass it; render a numeric **percentage + `LinearProgressIndicator`**
      on `ResultScreen` (new test tag). Also surface confidence on `RecommendationScreen` if it flows
      through cleanly.
- [ ] A2 tests: `CameraViewModelTest` (confidence threaded on success), `ResultViewModelTest` (arg parse
      + absent-arg graceful default), `ResultScreenBadgeTest`/new screen test (bar + % render; no % when
      arg absent).
- [ ] **A4 Search containment:** in `LowConfidencePickerScreen`, make the species list **contained within
      the search control** (e.g. reveal the list only when the search field is focused/non-blank, or move
      it into a dropdown/expandable surface) instead of an always-visible `SPECIES_LIST` LazyColumn.
      Preserve all existing `LowConfidencePickerTags` and the empty-state behaviour.
- [ ] A4 tests: update `LowConfidencePickerScreenTest` (+ `LowConfidenceFlowTest` if affected) for the
      contained-list interaction; keep chip/search/archetype paths green.
- [ ] **A5 Reference images:** source CC0/PD WebP images for the popular species subset; add to
      `res/drawable-nodpi/`; add a `speciesId → drawable` resolver (+ placeholder fallback); render the
      image on the click-through to a plant / its potting-mix recipe (`ResultScreen` and/or
      `RecommendationScreen`).
- [ ] A5 licensing: create `docs/licenses/reference-images.md` (image, source URL, CC0/PD license) and a
      test asserting every bundled reference image has a manifest entry.
- [ ] A5 size guard: verify the APK-size delta is within budget; record before/after sizes in evidence.

### Phase 3 — Pillar A: "My Plants" folder (depends on Phase 0)
- [ ] On a **high-confidence identification**, append an `IdentifiedPlant` to `PlantLogStore` (wire from
      `CameraViewModel` success path or `ResultViewModel` init — pick one, document why; avoid double-writes).
- [ ] Add `MyPlantsScreen` + `MyPlantsViewModel` (reads `PlantLogStore`): list of previously identified
      plants (name, source badge, confidence, captured time), most-recent first; empty-state when none.
- [ ] Add a `Routes.MY_PLANTS` + nav entry; add an entry point to it (e.g. a button/icon on
      `CameraScreen`). Tapping a row navigates to that plant's result/recommendation.
- [ ] My Plants tests: VM list ordering + empty state; screen render test (rows + empty state); a flow
      test for camera → identify → row appears.

### Phase 4 — Pillar B: "Add this plant" wireframe (depends on Phase 0 + 3.3)
- [ ] Extend `MappedScore` with `topLabel` / `topProbability` / `topIsMapped`; populate in
      `ModelScoreMapper.map` (raw `ranked[0]` label + score + mapping presence). **Do not** alter the
      high/low verdict logic.
- [ ] Expose the raw-top info via a side-channel (widen `CandidateProvider` or add a sibling marker
      implemented by `OnDevicePlantIdentifier`); leave the test fakes free to not implement it.
- [ ] Add `NavCommand.AddPlant(modelClassLabel, confidencePct)`; in `CameraViewModel`, route
      confident-but-unmapped (raw-top ≥ high threshold && `!topIsMapped`) to it; everything else keeps
      today's `LowConfidence`/`Success`/`Failure` routing.
- [ ] Add `AddThisPlantScreen` (+ VM): shows the recognized model-class name, the confidence, and an
      **"Add this plant"** button (wireframe — no KB write); a secondary action to fall through to the
      existing low-confidence picker / archetype path.
- [ ] On button tap, increment that model class's counter in `PlantLogStore` (`addPlantRequests`); show a
      confirmation state; keep it idempotent-friendly (each tap = one request increment is acceptable —
      document the chosen semantics).
- [ ] Add `Routes.ADD_THIS_PLANT` + nav wiring.
- [ ] B tests: `ModelScoreMapperTest` (raw-top fields incl. an unmapped-but-high fixture),
      `CameraViewModelTest` (routes to AddPlant vs LowConfidencePicker correctly), AddThisPlant VM/screen
      test (button increments store), and a guard that a mapped high-confidence class still routes to
      `Success` (no regression).

### Phase 5 — Pillar A1: theme candidates + hand-off
- [ ] Define 2–3 `ThemeCandidate` palettes in `ui/theme/Color.kt`; parameterise `PlantPottingTheme`
      in `ui/theme/Theme.kt`; reconcile `res/values/themes.xml` / `colors.xml` so the splash/system
      chrome don't fight the chosen scheme.
- [ ] Add a **debug-only** in-app theme switcher (persisted in the new DataStore), guarded by
      `BuildConfig.DEBUG` so it never ships in release.
- [ ] Capture per-candidate screenshots → `evidence/PLANTPOTTING-0010/themes/`.
- [ ] Theme tests: each candidate composes without crashing (preview/screenshot or render test); release
      build excludes the debug switcher.

### Phase 6 — Version bump, gates, delivery
- [ ] Bump `versionCode` 3 → 4 and `versionName` 0.3.0 → 0.4.0 in `app/build.gradle.kts`.
- [ ] Run the full gate set GREEN: `./gradlew :app:testDebugUnitTest`, `verifyNoNetworking`,
      `scripts/check-stub-isolation.sh`, lint (`abortOnError`), and a `pixel6Api34` GMD run if feasible
      (note the known transient 1h GMD timeout → rerun).
- [ ] Build the **debug APK** and copy to `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\`;
      verify the copy from the user's terminal (sandbox writes outside the project tree are unreliable —
      confirm landing).
- [ ] Tick the acceptance checkboxes (§6) as evidence lands; capture transcripts/screens under
      `evidence/PLANTPOTTING-0010/`.

## 5. Sequencing & phases (heterogeneous-sprint rationale)

```
Phase 0 (persistence) ─┬─> Phase 3 (My Plants)
                       └─> Phase 4 (Add this plant)
Phase 1 (KB expand) ───── independent ──> can land first / in parallel (lowest risk, content-only)
Phase 2 (confidence / search / images) ── touches existing screens; independent of persistence
Phase 5 (themes) ──────── independent; may reuse DataStore for the switcher
Phase 6 (bump + gates + APK) ──────────── last
```

- **Persistence first** because two pillars depend on it; building it twice is the failure mode the
  intent calls out.
- **KB expansion early** — it's pure content/config with strong existing CI guards, the lowest-risk
  pillar, and merge-conflict-free against the UI work.
- **Theme switcher after** the DataStore exists so it can persist the selection through it.
- **Version bump + APK last** so the deliverable reflects everything.

## 6. Acceptance criteria

- [ ] `PlantIdentifier.kt` (`PlantIdentifier` + `IdentificationResult` + `IdSource`) is byte-for-byte
      unchanged; all new cross-screen data rides nav args or side-channels.
- [ ] `verifyNoNetworking` GREEN with DataStore added; `scripts/check-stub-isolation.sh` GREEN.
- [ ] No model training, no fine-tune, no self-shot imagery; all theme + reference assets bundled and
      CC0/PD with a checked-in attribution manifest.
- [ ] Pilea remains unmapped; `HousePlantClassMapValidationTest` Pilea-absence + existing-row guards
      GREEN; mapped/species/archetype count asserts updated to the new totals.
- [ ] `ResultScreen` shows a numeric confidence **percentage and a progress bar** on the on-device
      high-confidence path; degrades gracefully (no bar/%) when confidence is absent (stub flows).
- [ ] The species list under the search box is **contained within** the search control, not an
      always-visible separate list; existing picker test tags + flows still pass.
- [ ] A license-clean **reference image** (or an explicit placeholder) renders for each plant on
      click-through; APK-size delta within the recorded budget.
- [ ] **My Plants** lists previously identified plants from the shared persistence layer and survives an
      app restart (persistence round-trip verified).
- [ ] A **confident-but-unmapped** model class surfaces an **"Add this plant"** screen+button; tapping it
      **increments a locally-stored, totalled request counter**; a mapped high-confidence class still
      routes to `ResultScreen` (no regression); weak/ambiguous predictions still route to
      `LowConfidencePicker`.
- [ ] **2–3 theme candidates** are switchable in a debug build; per-candidate screenshots delivered; no
      single theme silently hardcoded as the winner; the debug switcher is excluded from release.
- [ ] `versionCode`/`versionName` bumped (3→4 / 0.3.0→0.4.0); a debug APK is built and confirmed copied to
      the Dropbox APK folder.
- [ ] Full unit suite + lint GREEN; new features covered by unit/instrumentation tests; evidence captured
      under `evidence/PLANTPOTTING-0010/`.

## 7. Risks & mitigations

| # | Risk | Mitigation |
|---|------|-----------|
| 1 | **Persistence built twice / over-engineered (Room).** | One `PlantLogStore` (DataStore + serialization) designed in Phase 0 *before* A3/B; Room explicitly rejected for two small collections. |
| 2 | **Confidence display tempts a seam change** to add probability to `IdentificationResult`. | Use the existing `CandidateProvider` side-channel + a new optional nav arg; seam stays frozen (§3.2). |
| 3 | **License-clean reference images don't exist for all ~32 species** (0008: only 1–6.8% of houseplant imagery is license-clean). | CC0/PD-only; popular-subset + placeholder strategy; do not block the sprint on full coverage; attribution manifest + cross-check test. |
| 4 | **APK bloat** from images. | WebP + per-image/total byte budget; `painterResource` (no image lib); record before/after sizes. |
| 5 | **`verifyNoNetworking` false-trip** on a new dependency. | DataStore is local-only; verify the gate explicitly in Phase 0 before building further on it. |
| 6 | **"Add this plant" routing collides with the low-confidence path** / hides genuine low-confidence cases. | Carve only the *raw-top ≥ high-threshold && unmapped* slice into AddPlant; everything else unchanged; regression test that mapped-high still → `Success` and weak → picker. |
| 7 | **Pilea accidentally mapped** during KB expansion. | CI Pilea-absence guard stays; reviewer checklist item; exclude Pilea from the candidate slice up front. |
| 8 | **Theme work silently hardcodes one winner** instead of delivering pickable candidates. | Debug switcher + screenshots; production default unchanged until the principal picks; acceptance criterion enforces it. |
| 9 | **Sandbox writes outside the project tree (Dropbox APK copy) don't land.** | Verify the copy from the user's terminal before claiming delivery (per memory `apk_delivery_dropbox`). |
| 10 | **GMD instrumentation run transiently times out (~1h).** | Known; rerun once; JVM coverage carries the bulk; don't block close on a flaky GMD. |
| 11 | **Heterogeneous sprint sprawl** (UI + new feature + content) overruns. | Phase ordering with persistence as the single shared dependency; KB content can land first; each pillar independently testable/mergeable. |

## 8. Files likely touched (orientation, not exhaustive)

- **New:** `persistence/{PlantLogStore,DataStorePlantLogStore,PersistenceModule, DTOs, TimeProvider}.kt`;
  `result/AddThisPlantScreen.kt` + VM; `<feature>/MyPlantsScreen.kt` + VM; `ui/theme/` candidate
  additions + debug switcher; `res/drawable-nodpi/*.webp`; `docs/licenses/reference-images.md`;
  `evidence/PLANTPOTTING-0010/**`.
- **Edited (UI):** `result/ResultScreen.kt` + `ResultUiState.kt` + `ResultViewModel.kt`;
  `result/RecommendationScreen.kt` (image); `result/LowConfidencePickerScreen.kt` (search containment);
  `camera/CameraScreen.kt` (My Plants entry); `camera/CameraViewModel.kt` + `camera/NavCommand.kt`
  (confidence + AddPlant routing); `ui/navigation/{PlantPottingNavHost,Routes}.kt`;
  `ui/theme/{Color,Theme}.kt`; `res/values/{themes,colors,strings}.xml`.
- **Edited (identify, off-seam):** `identify/model/{MappedScore,ModelScoreMapper}.kt`;
  `identify/model/CandidateProvider.kt` (or new sibling marker); `identify/OnDevicePlantIdentifier.kt`.
- **Edited (KB / config):** `assets/kb/species.json`;
  `assets/ml/house_plant_species_mobilenetv2/plant_class_map.json`; `docs/kb/ml-mapping-notes.md`;
  `app/build.gradle.kts` (DataStore dep, version bump); `gradle/libs.versions.toml`.
- **Edited (tests):** the corresponding `*Test.kt` under `app/src/test` and `app/src/androidTest` named
  in each phase.
```
