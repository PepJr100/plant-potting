# PLANTPOTTING-0010 — App-experience sprint (UI/UX refresh + "Add this plant" wireframe + KB expansion)

> Merged plan (Opus synthesis of the CODEX / GEMINI / CLAUDE drafts + their three cross-critiques).
> Drafts and critiques live under `docs/sprints/drafts/PLANTPOTTING-0010-*.md`; refined intent at
> `docs/sprints/drafts/PLANTPOTTING-0010-INTENT.md`. Every actionable item below is a `- [ ]` checkbox.

## Intent

A deliberately mixed **"make it feel like an app"** sprint with three pillars, all behind the **frozen
`PlantIdentifier` / `IdentificationResult` seam** and the **network-free** gate. **Pillar A** — UI/UX
refresh: 2–3 pickable Compose themes (delivered as screenshots + a debug APK), a numeric **+** progress-bar
confidence display, a **"My Plants"** history backed by the app's **first local-persistence layer**,
containment of the species search list, and a license-clean **reference image** per plant. **Pillar B** —
an **"Add this plant"** wireframe that fires when the model is *strongly* confident about a class with **no
KB entry**, logging/totalling the request locally. **Pillar C** — continue the **text-only** KB expansion
over the popular slice of the remaining 21 unmapped model classes. The single unifying architectural move:
**one** local-persistence layer shared by My Plants and the request log — designed once, not twice.

This sprint **deliberately diverges** from the roadmap's planned Next (the pothos↔Pilea boundary fix),
which **stays deferred**; Pilea stays unmapped (CI-enforced).

## Goals

- [ ] 2–3 selectable Compose theme candidates, visible in a debug build and captured as screenshots, so the
      principal picks from real renders (not a hardcoded redesign).
- [ ] Numeric confidence **percentage + progress bar** on the post-identification path, without changing the
      frozen `PlantIdentifier` / `IdentificationResult` contract.
- [ ] A **"My Plants"** collection backed by the app's first local-persistence layer, populated by an
      **explicit user "Save" action**.
- [ ] An **"Add this plant"** wireframe for strong-confidence model classes that have no KB entry, logging a
      local-only request tally.
- [ ] **One** persistence layer shared by saved plants and add-request totals.
- [ ] The species list **contained within** the search-species control in `LowConfidencePickerScreen`.
- [ ] Bundled, **license-clean (CC0/PD)** reference imagery on plant detail / recipe views, within an explicit
      APK-size budget, with a checked-in attribution manifest.
- [x] A bounded **text-only KB expansion** over remaining popular unmapped model classes; Pilea stays unmapped.
- [ ] App version bumped (`versionCode 3→4`, `versionName 0.3.0→0.4.0`); a debug APK + theme screenshots
      delivered for review.
- [ ] `verifyNoNetworking` and `scripts/check-stub-isolation.sh` stay GREEN throughout.

## Non-goals (hold the line)

- [ ] **Do NOT** touch the `PlantIdentifier` / `IdentificationResult` / `IdSource` contract
      (`identify/PlantIdentifier.kt`, frozen per 0003 §4.4). All new cross-screen data rides **side-channels**
      (existing `CandidateProvider`, a new sibling marker, or nav args) — never the seam.
- [ ] **No networking** dependency or runtime fetch; all theme + image assets are **bundled**.
- [ ] **No model training, no fine-tune, no self-shot / first-party plant imagery.** (App-UI screenshots of
      candidate themes are renders, not plant data — fine.)
- [ ] **Do NOT** map Pilea (`Chinese Money Plant (Pilea peperomioides)`) — the CI Pilea-absence guard in
      `HousePlantClassMapValidationTest` stays GREEN. The pothos↔Pilea boundary fix stays deferred.
- [ ] **Do NOT** bump AGP / Kotlin / Compose / Hilt / TFLite versions (roadmap standing non-goal). Adding a
      *new* local-only library (DataStore) is permitted; version *bumps* of the locked stack are not.
- [ ] No cloud sync, no remote telemetry, no export UI for the request log (the principal collects totals
      later, off-device).
- [ ] The "Add this plant" button does **not** write a KB species row — it only logs.
- [ ] Keep the AIY baseline ML files intact (treat as a regression guard, not an edit target).

## Key architectural decisions (resolve first — the rest depends on these)

### D1 — Shared persistence: **DataStore, not Room**
One local store, two logical collections, exposed through two narrow domain accessors (so the "one layer,
two APIs" intent is honoured without two storage mechanisms).
- New package `com.darkfactory.plantpotting.persistence`.
- **DataStore** (typed `androidx.datastore:datastore` + a `@Serializable` document, reusing the existing
  `kotlinx.serialization.json`) over Room: local-file by construction (keeps `verifyNoNetworking` trivially
  GREEN), no KSP/schema/migration ceremony, fewer transitive deps for two append-mostly lists. **Room is
  explicitly rejected** for this data shape (two critiques flagged it as over-engineered).
- Collections: `identifiedPlants` → My Plants `{speciesId, displayName, source, confidencePct, savedAtEpochMs}`;
  `addPlantRequests` → request log `{modelClassLabel, count, lastRequestedEpochMs}` totalled per class.
- **Bound growth:** cap `identifiedPlants` to most-recent-N (e.g. 100) with eviction on write.
- **Inject a `TimeProvider`/`Clock`** (no inline `System.currentTimeMillis()`) so the store is unit-testable
  with a fake clock.

### D2 — Confidence display without touching the seam
`IdentificationResult` carries no probability, but `OnDevicePlantIdentifier` exposes
`CandidateProvider.mostRecentCandidates` and `Candidate.probability` is the raw softmax (0..1).
- On the high-confidence path, `CameraViewModel.onCaptureReady` already reads the identifier as
  `CandidateProvider`; read `mostRecentCandidates.firstOrNull()?.probability` and thread it through as a **new
  optional nav arg** `confidencePct` on `Routes.RESULT` (mirroring how candidates already ride the
  low-confidence route). `ResultViewModel`/`ResultUiState` carry it; `ResultScreen` renders %+bar.
- **Subtlety to test:** in `ModelScoreMapper`'s margin-confidence branch the winner is still `ranked[0]`, so
  the first *mapped* candidate's probability tracks `bestProb` — assert this; if a future mapping makes them
  diverge, prefer the winning species' own score. Absent arg (stub flows) → render no %/bar gracefully.

### D3 — "Add this plant" routing (off the frozen seam)
`ModelScoreMapper.candidateForIndex` returns `null` for unmapped labels, so a *confident-but-unmapped* class
is currently invisible (it collapses into the low-confidence verdict).
- Extend internal `MappedScore` with **`topLabel` + `topProbability` + `topIsMapped`**; populate in
  `ModelScoreMapper.map` from raw `ranked[0]`. **Do not** alter the high/low verdict logic. Expose via a
  side-channel (widen `CandidateProvider` or add a sibling marker, e.g. `UnmappedTopProvider`, implemented by
  `OnDevicePlantIdentifier`). `MappedScore` and the marker are **not** the frozen seam.
- **Routing predicate:** in `CameraViewModel`, when `lowConfidence == true` **but** raw-top probability ≥ the
  high-confidence threshold **and** `topIsMapped == false` → `NavCommand.AddPlant(modelClassLabel,
  confidencePct)` → `AddThisPlantScreen`. Everything else keeps today's `Success` / `LowConfidence` / `Failure`
  routing unchanged. (I.e. carve only the *confident-but-unmapped* slice out of today's low-confidence path;
  weak/ambiguous predictions still go to the picker.)
- `AddThisPlantScreen` shows the recognized model-class name, the confidence, an **"Add this plant"** button
  (wireframe — logs only), and an explicit **"Pick manually"** secondary button into the existing
  `LowConfidencePickerScreen` flow.

### D4 — Reference images: licensing × APK size
- **CC0 / public-domain only** (0008 found CC-BY-NC dominates and is unusable; even CC-BY adds per-image
  attribution burden — prefer CC0/PD). Wikimedia Commons / PD botanical plates are good sources. **No
  self-shot imagery.**
- Bundle as **downscaled WebP** under `res/drawable-nodpi/`, rendered with `painterResource` — **no
  image-loading library / Coil** (avoids a dependency *and* keeps it network-free).
- **Budgets:** ~40 KB per WebP and **≤ 3–5 MiB total** growth over the baseline debug APK (captured in Phase
  0). If clean images aren't available for all species, ship the **popular subset** + a **placeholder vector**
  for the rest — do **not** block the sprint on full coverage.
- Checked-in attribution manifest `docs/licenses/reference-images.md` (image, source URL, CC0/PD license) **+ a
  cross-check test** that every bundled reference image has a manifest entry, **+ a test** that missing image
  metadata does not crash recommendation rendering.

### D5 — Theme candidates → pickable deliverable
- Define **2–3** candidate palettes/`ColorScheme`s as a `ThemeCandidate` enum in `ui/theme/Color.kt`;
  parameterise `PlantPottingTheme(candidate = …)` in `ui/theme/Theme.kt` (keep the current scheme as one
  candidate / the production default). Reconcile `res/values/themes.xml` / `colors.xml` so splash/system
  chrome don't fight the chosen scheme.
- Ship a **debug-only in-app theme switcher** guarded by `BuildConfig.DEBUG` (never in release), persisted via
  the new DataStore, reached via a concrete affordance (e.g. long-press on `CameraScreen` or a Dev sheet) so
  **one** debug APK lets the principal flip candidates on-device. Production default unchanged until the
  principal picks.
- Capture per-candidate screenshots → `docs/sprints/evidence/PLANTPOTTING-0010/themes/`.

## Task list (by phase)

### Phase 0 — Baseline & guards (before any edits)
- [ ] Run `./gradlew :app:testDebugUnitTest`, `./gradlew verifyNoNetworking`, and
      `bash scripts/check-stub-isolation.sh` to catch pre-existing drift.
- [ ] Run a baseline `./gradlew assembleDebug` and **record the current debug-APK size** so the reference-image
      delta has a real anchor.
- [ ] **Start sourcing CC0/PD reference images in parallel now** (highest external-uncertainty task; 0008 found
      only ~1–6.8% of houseplant imagery is license-clean) so integration in Phase 6 isn't blocked at the end.

### Phase 1 — Shared persistence foundation (unblocks My Plants + Add-this-plant)
- [ ] Add `androidx.datastore` to `gradle/libs.versions.toml` + `app/build.gradle.kts` (local-only).
- [ ] **Immediately** re-run `./gradlew verifyNoNetworking` to confirm the new dependency doesn't trip the
      forbidden-substring audit (before stacking feature work on it).
- [ ] Create `persistence/`: `@Serializable` DTOs (`IdentifiedPlant`, `AddPlantRequest`), a `PlantLogStore`
      interface, and a `DataStorePlantLogStore` impl (one DataStore document; reuses `kotlinx.serialization.json`).
- [ ] Implement an injectable `TimeProvider`/`Clock`; use it for all timestamps.
- [ ] Add `PersistenceModule` (Hilt) binding `PlantLogStore` (`@Singleton`, app-context DataStore).
- [ ] Cap `identifiedPlants` length (most-recent-N) with eviction on write.
- [ ] Unit tests: append/read round-trip, request-counter increment+total, N-cap eviction, fake-clock
      timestamps, empty-store cold read.

### Phase 2 — Pillar C: text-only KB expansion (independent, lowest-risk — land early)
- [x] Diff `house_plant_species_mobilenetv2/labels.csv` against `plant_class_map.json`; list the remaining 21
      unmapped classes and pick the popular slice (exclude Pilea; **keep at least one high-confidence unmapped
      class unmapped** so Phase 5's Add-this-plant has a real fixture to exercise).
- [x] Append new species rows to `app/src/main/assets/kb/species.json` (append-only; reuse existing archetypes
      where possible); add archetype rows to `archetypes.json` only if no existing archetype fits.
- [x] Add verbatim-label rows to `plant_class_map.json` (mark coarse/genus rows `alias:true` + `_note`, per the
      0009 convention).
- [x] Update count assertions: `HousePlantClassMapValidationTest` (mapped count — keep Pilea-absence +
      existing-row regression guards), `KbContentSpeciesTest` / `KbLoaderTest` (species & archetype counts).
- [x] Add content tests (toxicity, common name, citation, sum-to-100 recipe) matching existing `KbContentSpeciesTest`
      style; vet content for each new species.
- [x] Record the slice + citations in `docs/kb/ml-mapping-notes.md §PLANTPOTTING-0010`; document the new
      mappings as **editorial / model-vocabulary coverage, not calibrated** behaviour.

### Phase 3 — Pillar A polish on existing screens (independent of persistence)
- [ ] **Confidence (D2):** add optional `confidencePct` nav arg to `Routes.RESULT` + `PlantPottingNavHost` +
      `ResultViewModel`/`ResultUiState`; read the top candidate's probability in `CameraViewModel`'s success
      path and pass it; render % + `LinearProgressIndicator` on `ResultScreen` (new test tag). Surface on
      `RecommendationScreen` too if it flows through cleanly.
- [ ] Confidence tests: `CameraViewModelTest` (threaded on success), `ResultViewModelTest` (arg parse +
      absent-arg graceful default), `ResultScreen` render test (bar + %; no % when absent), and the
      margin-confidence `ranked[0]` assertion from D2.
- [ ] **Search containment (A4):** in `LowConfidencePickerScreen`, contain the species list **within** the
      search control (reveal on focus/non-blank, or move into a dropdown/expandable surface) instead of an
      always-visible `SPECIES_LIST` LazyColumn. **Preserve** candidate chips, the no-candidates card, the
      search-empty state, and the archetype CTA; keep existing `LowConfidencePickerTags` stable where possible.
- [ ] Search-containment tests: update `LowConfidencePickerScreenTest` (+ `LowConfidenceFlowTest` if affected);
      keep chip / search / archetype paths green.

### Phase 4 — Pillar A: "My Plants" folder (depends on Phase 1)
- [ ] Add an **explicit "Save to My Plants" action** on `ResultScreen` (and/or `RecommendationScreen`) that
      appends an `IdentifiedPlant` via `PlantLogStore` — **user-initiated, not auto-append on every scan**
      (avoids double-write ambiguity + history noise); reflect saved state in `ResultUiState`.
- [ ] Add `MyPlantsScreen` + `MyPlantsViewModel` reading `PlantLogStore`: previously-saved plants (name, source
      badge, confidence, saved time), most-recent first; empty state when none.
- [ ] Add `Routes.MY_PLANTS` + nav wiring; add an entry point (e.g. an icon/button on `CameraScreen`). Tapping
      a row navigates to that plant's recommendation/result.
- [ ] My Plants tests: VM ordering + empty state; screen render test (rows + empty); a flow test for
      identify → Save → row appears; **persistence round-trip survives app restart**.

### Phase 5 — Pillar B: "Add this plant" wireframe (depends on Phase 1 + D3)
- [ ] Extend `MappedScore` with `topLabel` / `topProbability` / `topIsMapped`; populate in
      `ModelScoreMapper.map` (raw `ranked[0]`). **Do not** alter verdict logic.
- [ ] Expose raw-top info via a side-channel (widen `CandidateProvider` or add a sibling marker on
      `OnDevicePlantIdentifier`); leave test fakes free not to implement it.
- [ ] Add `NavCommand.AddPlant(modelClassLabel, confidencePct)` + `Routes.ADD_THIS_PLANT`; in `CameraViewModel`
      route confident-but-unmapped (raw-top ≥ high threshold && `!topIsMapped`) to it; all else unchanged.
- [ ] Add `AddThisPlantScreen` (+ VM): model-class name, confidence, **"Add this plant"** button (logs only),
      **"Pick manually"** secondary button → existing `LowConfidencePicker`.
- [ ] On "Add this plant" tap, increment that class's counter in `PlantLogStore.addPlantRequests`; show a
      confirmation state; document the increment semantics (each tap = one request is acceptable).
- [ ] B tests: `ModelScoreMapperTest` (raw-top fields incl. an unmapped-but-high fixture), `CameraViewModelTest`
      (routes AddPlant vs LowConfidencePicker correctly; mapped-high still → `Success`, no regression),
      AddThisPlant VM/screen test (button increments store).

### Phase 6 — Pillar A: reference images (sourcing started in Phase 0)
- [ ] Add CC0/PD WebP images for the popular species subset to `res/drawable-nodpi/`; resize/compress to display
      dimensions (do not commit large originals).
- [ ] Add a `speciesId → drawable` resolver with a **placeholder** fallback for species without a clean image.
- [ ] Render the image on the click-through to a plant / its potting-mix recipe (`ResultScreen` and/or
      `RecommendationScreen`; via `RecommendationViewModel`/`RecommendationUiState`). Do **not** show a plant
      image for archetype-only recommendations.
- [ ] Create `docs/licenses/reference-images.md` (image, source URL, CC0/PD license) + a test asserting every
      bundled reference image has a manifest entry + a test that missing image metadata doesn't crash rendering.
- [ ] Verify the APK-size delta is within the ≤3–5 MiB budget; record before/after sizes in evidence.

### Phase 7 — Pillar A: theme candidates + hand-off (reuses DataStore)
- [ ] Define 2–3 `ThemeCandidate` palettes in `ui/theme/Color.kt`; parameterise `PlantPottingTheme` in
      `ui/theme/Theme.kt`; reconcile `res/values/{themes,colors}.xml` so splash/system chrome match.
- [ ] Add the **debug-only** in-app theme switcher (persisted in DataStore), guarded by `BuildConfig.DEBUG`,
      reached via a concrete affordance (long-press camera / Dev sheet).
- [ ] Theme tests: each candidate composes without crashing (preview/screenshot/render test); **release build
      excludes the debug switcher**.
- [ ] Capture per-candidate screenshots → `docs/sprints/evidence/PLANTPOTTING-0010/themes/`.

### Phase 8 — Version bump, gates, delivery
- [ ] Bump `versionCode` 3→4 and `versionName` 0.3.0→0.4.0 in `app/build.gradle.kts`.
- [ ] Full gate set GREEN: `./gradlew :app:testDebugUnitTest`, `verifyNoNetworking`,
      `scripts/check-stub-isolation.sh`, lint (`abortOnError`), and a `pixel6Api34` GMD run if feasible (known
      transient ~1h GMD timeout → rerun once; JVM coverage carries the bulk — don't block close on flaky GMD).
- [ ] Build the debug APK and copy to `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\`; **verify the
      copy from the user's terminal** (sandbox writes outside the project tree are unreliable — confirm landing).
- [ ] Record final APK size + image-asset contribution; tick acceptance checkboxes as evidence lands under
      `docs/sprints/evidence/PLANTPOTTING-0010/`.

## Sequencing & rationale

```
Phase 0 (baseline + start image sourcing)
Phase 1 (DataStore persistence) ─┬─> Phase 4 (My Plants)
                                 └─> Phase 5 (Add this plant)
Phase 2 (KB expansion) ──── independent, lowest-risk ──> land early (bank the cheap win)
Phase 3 (confidence + search) ── touches existing screens; independent of persistence
Phase 6 (reference images) ──── sourcing started Phase 0; integrate after features proven
Phase 7 (themes) ──────── independent; reuses DataStore for the switcher
Phase 8 (bump + gates + APK) ── last
```

- **Persistence first** — two pillars depend on it; building it twice is the failure mode the intent calls out.
- **KB expansion early** — pure content with strong existing CI guards, lowest risk, merge-conflict-free; do
  not bury it at the end (corrects a draft that scheduled it right before delivery). Keep one high-confidence
  unmapped class unmapped so Add-this-plant has a live fixture.
- **Image *sourcing* starts in Phase 0** because license availability is the highest external uncertainty;
  *integration* lands in Phase 6 after the routing/persistence features are proven.
- **Themes after** DataStore exists so the switcher persists its selection; **version bump + APK last**.

## Risks & mitigations

| # | Risk | Mitigation |
|---|------|-----------|
| 1 | Persistence built twice / over-engineered (Room). | One `PlantLogStore` (DataStore + serialization) in Phase 1 before A/B; Room explicitly rejected (D1). |
| 2 | Confidence display tempts a seam change. | Side-channel `CandidateProvider` + optional nav arg; seam stays frozen; margin-confidence `ranked[0]` test (D2). |
| 3 | Strong-unmapped class invisible / routing collapse. | Extend `MappedScore` raw-top + side-channel; carve only raw-top ≥ threshold && unmapped into AddPlant; regression test mapped-high → `Success`, weak → picker (D3). |
| 4 | DataStore trips `verifyNoNetworking`. | Local-only; re-run the gate immediately after adding the dependency (Phase 1). |
| 5 | License-clean images don't exist for all species (0008: ~1–6.8% clean). | CC0/PD only; start sourcing Phase 0; popular-subset + placeholder; don't block the sprint on full coverage (D4). |
| 6 | APK bloat from images. | WebP, ~40 KB/image + ≤3–5 MiB total budget vs Phase-0 baseline; `painterResource` (no image lib); record delta. |
| 7 | Pilea accidentally mapped during KB expansion. | CI Pilea-absence guard stays; exclude Pilea from the slice up front; reviewer checklist. |
| 8 | Theme work silently hardcodes a winner. | Debug-only switcher + screenshots; production default unchanged until the principal picks; `BuildConfig.DEBUG` exclusion test. |
| 9 | Auto-saving every scan pollutes My Plants / double-writes. | Explicit user "Save" action, not auto-append (Phase 4). |
| 10 | Heterogeneous-sprint sprawl overruns. | Persistence as the single shared dependency; KB content lands first; each pillar independently testable/mergeable. |
| 11 | Dropbox APK copy doesn't land (sandbox overlay). | Verify the copy from the user's terminal before claiming delivery (memory `apk_delivery_dropbox`). |
| 12 | GMD instrumentation transiently times out (~1h). | Known; rerun once; JVM coverage carries the bulk; don't block close on flaky GMD. |

## Acceptance criteria

- [ ] `PlantIdentifier` / `IdentificationResult` / `IdSource` byte-for-byte unchanged; all new cross-screen data
      rides nav args or side-channels.
- [ ] `verifyNoNetworking` GREEN with DataStore added; `scripts/check-stub-isolation.sh` GREEN.
- [ ] No model training, no fine-tune, no self-shot imagery; all theme + reference assets bundled and CC0/PD with
      a checked-in attribution manifest + cross-check test.
- [ ] Pilea remains unmapped; `HousePlantClassMapValidationTest` Pilea-absence + existing-row guards GREEN;
      mapped / species / archetype count asserts updated to the new totals; new mappings documented as
      **editorial coverage, not calibrated**.
- [ ] `ResultScreen` shows a numeric confidence **percentage and progress bar** on the on-device high-confidence
      path; degrades gracefully (no bar/%) when confidence is absent (stub flows).
- [ ] The species list under the search box is **contained within** the search control; existing picker tags +
      flows (chips, no-candidates, empty, archetype) still pass.
- [ ] A license-clean **reference image** (or explicit placeholder) renders for each plant on click-through; APK
      delta within the recorded ≤3–5 MiB budget; missing-image metadata doesn't crash rendering.
- [ ] **My Plants** lists user-saved plants from the shared persistence layer (explicit Save action) and survives
      an app restart (round-trip verified); collection is N-capped.
- [ ] A **confident-but-unmapped** model class surfaces an **"Add this plant"** screen+button; tapping increments
      a locally-stored, totalled request counter; "Pick manually" falls through to `LowConfidencePicker`; a mapped
      high-confidence class still routes to `ResultScreen` (no regression); weak predictions still route to the picker.
- [ ] **2–3 theme candidates** are switchable in a debug build; per-candidate screenshots delivered; no single
      theme silently hardcoded; the debug switcher is excluded from release.
- [ ] `versionCode`/`versionName` bumped (3→4 / 0.3.0→0.4.0); a debug APK built and **confirmed copied** to the
      Dropbox APK folder.
- [ ] Full unit suite + lint GREEN; new features covered by unit/instrumentation tests; evidence captured under
      `docs/sprints/evidence/PLANTPOTTING-0010/`.
- [ ] Sprint notes clearly **separate calibrated model behaviour from editorial KB/class-map coverage**.
