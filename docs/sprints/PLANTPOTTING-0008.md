# PLANTPOTTING-0008 — Training-data availability spike (gates fine-tuning) + shutter-on-return UX fix

PLANTPOTTING-0007 shipped the houseplant model swap: `house_plant_species_mobilenetv2` (MobileNetV2,
47 classes, Apache-2.0, float16 TFLite) is now the production default behind the single
`ACTIVE_MODEL_ROOT` BuildConfig switch, covering **10 of 16** KB species and beating AIY 6-to-1
on-device. That closed the "is there a better off-the-shelf model" question. What it left open is the
**other 6 KB species the model still can't name at all**, plus a known confusion where pothos is
confidently misread as Pilea. The obvious next lever is *fine-tuning the MobileNetV2 we already ship* —
but a fine-tuning sprint is only worth committing if license-clean training images actually exist in the
quantity a small transfer-learning run needs.

**This sprint does NOT train anything.** It is a **desk-research feasibility spike** whose primary
artifact is a written, per-species **GO / NO-GO** report backed by real license-clean image **counts**
gathered through the sources' own APIs/UI — *assess only, no bulk download, no images committed*. On GO
it hands off a scoped outline for the fine-tuning sprint; on NO-GO (or for any blocking species under a
partial-GO) it documents the paid-dataset / self-shot fallback and its cost. Folded in alongside is
**one small camera UX bug** from the idea inbox — the only code change in the sprint.

The spike answers, per target species, one question: *does enough free, license-clean,
correctly-identified, attribution-feasible imagery of THIS species (and, for the boundary case, its
lookalikes) exist to build train/val/test splits that are **disjoint from the 8 existing androidTest
fixtures**?*

## Target species (the whole assessment surface)

**6 out-of-vocab (OOV) KB species** — no class exists in the production model, so fine-tuning would
add/relabel a head class:

- [ ] `monstera-adansonii` (Swiss-cheese vine; distinct from in-vocab `monstera-deliciosa` — fenestration differs, easy to mislabel)
- [ ] `philodendron-hederaceum` (heart-leaf philodendron; the canonical pothos lookalike — see boundary problem)
- [ ] `philodendron-pink-princess` (a **specific cultivar** — pink variegation; generic philodendron imagery does NOT count — highest-risk target)
- [ ] `ficus-lyrata` (fiddle-leaf fig)
- [ ] `chlorophytum-comosum` (spider plant)
- [ ] `hoya-carnosa` (wax plant)

**1 boundary problem (both classes already in the model, but mutually confused):**

- [ ] `epipremnum-aureum` (pothos) ↔ Pilea — the model has both classes but confidently misclassifies pothos as Pilea. Needs **extra, varied "hard example" imagery of BOTH sides** (including visually-confusable shots), not a new class. Assess as a pair, and assess the **production model's Pilea class specifically** (not generic *Pilea*).

## Goals

- [ ] **G1 — Per-species sourcing report + machine-readable counts.** For every target, a per-source table of license-clean image counts, license types, attribution feasibility, taxonomic/synonym/cultivar pitfalls, count-confidence, and disjoint-split feasibility — backed by a diffable `source-counts.csv` and a `query-log.md` of exact queries/dates.
- [ ] **G2 — GO/NO-GO recommendation.** A per-species verdict against the agreed thresholds, plus one overall sprint-level verdict (supporting **partial-GO**), with the deciding numbers shown.
- [ ] **G3 — Conditional hand-off.** For each GO/CONDITIONAL species, a scoped fine-tuning outline (sourcing/download plan, disjoint-split strategy, transfer-learning on the existing MobileNetV2, float16/INT8 export, reuse of `ModelSwapEvaluationTest` + `ACTIVE_MODEL_ROOT` + fixture/provenance pattern). For each NO-GO species, the paid-dataset / self-shot fallback + rough cost. A partial-GO produces **both**, scoped per species.
- [ ] **G4 — Shutter-on-return UX fix.** The shutter button re-enables when the user navigates back to the camera screen after a capture, with a regression test. The only code change in the sprint.
- [ ] **G5 — Constraints preserved.** `verifyNoNetworking` and `scripts/check-stub-isolation.sh` stay GREEN; no KB edits; no model/bundle/`ACTIVE_MODEL_ROOT` change; no committed image data; the `PlantIdentifier`/`IdentificationResult` seam stays frozen.

## Non-goals / scope boundaries

- [ ] **No ML training or fine-tuning in this sprint.** This spike only decides *if* a later fine-tuning sprint is viable. No `.h5`/checkpoint training, no transfer-learning runs, no head surgery.
- [ ] **No model work.** No model conversion, no new bundle under `app/src/main/assets/ml/`, no change to the `ACTIVE_MODEL_ROOT` default (`ml/house_plant_species_mobilenetv2`), no manifest/label-map/threshold edits.
- [ ] **No KB edits.** `app/src/main/assets/kb/species.json` and `archetypes.json` are LOCKED (read-only input to the report).
- [ ] **No bulk image download; no images committed.** Assess counts via APIs/UI/search only. NO sample images in the repo or APK. Any tiny manual eyeball check stays **off-repo** (local scratch dir outside the project tree, never staged).
- [ ] **No new fixtures.** The 8 existing `identify-fixtures/` JPGs are untouched; the spike only references them to define the disjoint-split exclusion set.
- [ ] **Seam frozen.** `PlantIdentifier.identify(jpeg): IdentificationResult` and `IdentificationResult` unchanged. The UX fix is confined to the camera screen / `CameraViewModel` state.
- [ ] **No production networking.** Any API-query tooling lives under `docs/sprints/evidence/PLANTPOTTING-0008/` or `scripts/`, is **never** invoked from a Gradle app task, and never runs on device.
- [ ] **No version bumps** (AGP/Kotlin/Compose/Hilt/TFLite).
- [ ] **Do not broaden the idea-inbox UX work** beyond the shutter re-enable bug.

## Sources to assess (license-clean only)

Each carries a different license/redistribution profile — the report must separate *raw count* from
*usable count*. "License-clean / usable" = CC0 / CC-BY / CC-BY-SA / public-domain; **CC-BY-NC and
CC-BY-ND are recorded but excluded from the usable count** (NC is unusable for a commercial-app model).

- [ ] **iNaturalist** — research-grade observations, per-observation CC license. Record the CC breakdown; check **observation-level vs photo-level** license (they can differ) and filter to media-backed, research-grade, the specific taxon.
- [ ] **GBIF** — aggregates iNat + herbaria + others; good for taxon-key/synonym cross-checking. Count only **media-bearing** occurrences with a usable media license; **de-dupe against iNat** to avoid double-counting.
- [ ] **Wikimedia Commons** — CC0 / CC-BY-SA / PD; the source the existing fixtures already use (so provenance-block feasibility is proven). Usually lower volume, higher label quality; map Commons file-license + attribution fields to the fixture provenance pattern.
- [ ] **Flickr (CC-licensed)** — search by license + species/common name; noisier labels (common-name tagging, mislabels) → flag "needs manual label verification"; verify per-photo attribution fields exist.
- [ ] **Any other obvious CC/CC0 source** (e.g. Pl@ntNet open data) — count a row **only** when the source has queryable counts, stable per-image URLs, per-image license metadata, and feasible attribution; otherwise list "reference-only, excluded from counts".

## Phase 0 — Scaffolding & inputs (no networking)

- [ ] Create the evidence dir `docs/sprints/evidence/PLANTPOTTING-0008/` mirroring the 0007 structure, with a `README.md` describing the spike, the no-download rule, the sources used, and the meaning of GO / CONDITIONAL / NO-GO.
- [ ] Add a local `.gitignore` under `docs/sprints/evidence/PLANTPOTTING-0008/` ignoring accidental media/cache: `*.jpg`, `*.jpeg`, `*.png`, `*.webp`, `downloads/`, `samples/`, `raw/`, and API cache files.
- [ ] Extract the canonical KB species names + encoded aliases for the 7 targets from `app/src/main/assets/kb/species.json` (read-only) into `species-targets.md` — KB id, accepted scientific name, synonyms, and known lookalikes per target. This is the controlled vocabulary every source query must use. Reconcile synonym handling with `docs/kb/ml-mapping-notes.md`.
- [ ] Record the **disjoint-split exclusion set** in `species-targets.md`: the 8 fixtures in `app/src/androidTest/assets/identify-fixtures/` and their Wikimedia source URLs (from `LICENSE.txt`), so the report can confirm splits without reusing any fixture image. **`epipremnum-aureum.jpg`'s source URL MUST be excluded from any pothos count** (it overlaps the boundary target).
- [ ] Write `methodology.md`: the query *template* per source, what "license-clean/usable" means (above), how counts are de-duped across sources, the count-confidence rubric (high/medium/low by metadata quality, taxon precision, duplicate risk), and the threshold definitions (below). (Exact executed URLs + retrieval dates land in `query-log.md` during Phase 1, not here.)
- [ ] Create empty `source-counts.csv` (header only) and `query-log.md` so Phase 1 has somewhere to land each query.

## Phase 1 — Per-species data assessment (the core research, ASSESS-ONLY)

> One pass per target species (×7) across all sources (×4–5). No bulk download. Optionally a *tiny*
> off-repo eyeball sample (≤~20 images/species) to sanity-check label quality + the cultivar question —
> never committed; note each sample in `query-log.md`. **Throttle API calls** / prefer UI counts to
> respect rate limits (R11). One checkbox per species fills its full source row-set into
> `source-counts.csv` + `query-log.md` — keep it per-species (not 32 species×source tasks) for
> trackability without bloat.

- [ ] **`monstera-adansonii`** — count usable images across iNat (CC breakdown, obs-vs-photo license), GBIF (media-bearing, de-duped), Wikimedia Commons, Flickr-CC; pitfall: vs `monstera-deliciosa` (confirm counts aren't dominated by deliciosa mislabels). Write rows to `source-counts.csv` + queries to `query-log.md`.
- [ ] **`philodendron-hederaceum`** — same source sweep; pitfall: routinely **cross-tagged with `epipremnum-aureum`** — record how much of each count is likely the other.
- [ ] **`philodendron-pink-princess`** — same sweep, but **count the pink-variegated cultivar specifically** (`Philodendron erubescens 'Pink Princess'`); generic `Philodendron erubescens` is **excluded** from the recommendation. Record a separate `cultivar-proven` count. (Highest-risk target.)
- [ ] **`ficus-lyrata`** — same sweep; note indoor/compact vs tree-form imagery.
- [ ] **`chlorophytum-comosum`** — same sweep; note variegated cultivar variants.
- [ ] **`hoya-carnosa`** — same sweep; note variegated/compacta forms that may not represent the base class cleanly.
- [ ] **Pothos/Pilea boundary (special handling).** Assess `epipremnum-aureum` AND the production Pilea class as a pair: count varied **hard-example** imagery for EACH side separately (trailing vines, juvenile leaves, confusable leaf shapes) toward the ~150–300-each target. Exclude true `Pothos`-genus records and generic non-class *Pilea*.
- [ ] **Per-row license + provenance inventory.** For every `source-counts.csv` row, record license type(s), whether per-image (vs aggregate) license is verifiable, and whether author / source page / original-file-or-observation URL / date / modifications / license / attribution string are API-exposed — i.e. whether a fixture-style provenance block (`identify-fixtures/LICENSE.txt`) can be auto-generated at download time in the future sprint.
- [ ] **Count-confidence + dedup.** Per species, classify count confidence high/medium/low; estimate cross-source duplicate risk (GBIF mirroring iNat media) and de-dupe by observation/file id; downgrade confidence where per-image license can't be verified.
- [ ] **Disjoint-split feasibility per species.** Confirm usable-count ≥ floor and that train/val/test splits are achievable **disjoint from the 8 fixtures** (by source observation/file id). Record a one-line yes/no + split sketch (e.g. "210 usable → 150/30/30, fixtures excluded").

## Phase 2 — Report & GO/NO-GO synthesis

- [ ] Write the **per-species sourcing report** at `data-availability-report.md` — one table per species with columns: **source · usable (license-clean) count · raw count · license type(s) · attribution feasible (Y/N) · taxonomic/synonym/cultivar pitfall · cultivar-specific (Y/N/n-a) · count-confidence · disjoint-split achievable (Y/N)** — plus a **boundary** sub-table for pothos vs Pilea hard examples. The CSV (`source-counts.csv`) is the machine-readable mirror.
- [ ] Add a roll-up **`go-no-go-matrix.md`**: one row per target with summed usable count, threshold band, and per-species GO / CONDITIONAL / NO-GO verdict (deciding number shown).
- [ ] Apply the **thresholds** (state the deciding number for each):
  - **Comfortable / GO:** ~150–300 license-clean usable images/species.
  - **Floor / CONDITIONAL:** ~50–100/species (viable but thin — note augmentation/few-shot risk).
  - **Below floor / NO-GO:** < ~50 usable/species.
  - **Boundary:** ~150–300 of EACH of pothos and Pilea, including lookalike hard shots.
- [ ] Decide and record the **overall sprint verdict**, supporting **partial-GO**: GO for the species that clear the floor with a sourceable boundary pair; name explicitly which species are in vs out. (A realistic likely outcome: most OOV species GO, `philodendron-pink-princess` NO-GO on cultivar scarcity → fallback.)
- [ ] **Conditional hand-off** in `finetune-sprint-outline.md`, scoped **per species**:
  - [ ] For each GO/CONDITIONAL species: sourcing/download plan (per-source quotas + attribution capture), disjoint-split strategy vs the 8 fixtures, transfer-learning approach on the existing `house_plant_species_mobilenetv2` (classes added/relabeled, frozen-backbone vs full fine-tune), float16/INT8 export path, and explicit reuse of `ModelSwapEvaluationTest` + the `ACTIVE_MODEL_ROOT` switch + the `identify-fixtures/` provenance pattern for new held-out eval fixtures.
  - [ ] For each NO-GO/blocking species (e.g. pink-princess): fallback memo — paid-dataset options (name candidates + rough licensing cost) and the self-shot plan (shots/species, who, attribution-trivial since first-party), and which subset the fallback covers.
- [ ] Write `docs/sprints/results/PLANTPOTTING-0008.md`: the verdict, per-species count summary, the chosen hand-off, the UX-fix status, and all gate results, plus an explicit statement that **no training data or model assets were committed**. Flip the ledger only after every gate is green.

## Phase 3 — Shutter-on-return UX fix (the only code change)

**Root cause (confirmed in `CameraScreen.kt:191`):** the shutter is enabled only when
`(state is Idle || state is Failure) && imageCapture != null`. After a capture the `CameraViewModel`
state advances to `CameraUiState.Success(speciesId)` (`CameraViewModel.kt:51`) and the view model
survives in the Compose back-stack-entry `ViewModelStore`. Navigating **back** from the results screen
returns to the camera with state still `Success` → the shutter stays at `alpha 0.5f` / `disabled()` and
never re-enables. The fix is to return the camera to `Idle` when it is re-shown.

- [ ] **Reproduce (red first):** an instrumentation/Compose test that drives `onCaptureReady(...)` to reach `Success`, simulates return-to-camera, and asserts the shutter is **not enabled** (failing).
- [ ] **Implement the re-enable:** reset `CameraViewModel` state to `CameraUiState.Idle` when the camera screen is (re)shown — preferred: a lifecycle-aware effect in `CameraScreen` (`ON_RESUME`/`ON_START` observer or `LaunchedEffect` keyed on re-entry) calling the existing `viewModel.reset()` (`CameraViewModel.kt:61`) when the current state is terminal `Success`. Do **not** touch `PlantIdentifier`/`IdentificationResult` or the nav-command contract.
- [ ] **Guard in-flight + failure:** ensure the reset does NOT fire while `Capturing`/`Identifying`; decide explicitly whether returning on `Failure` resets (preserving the failure-banner retry behaviour) and document the chosen rule in a code comment.
- [ ] **Make it pass (green):** on return-to-camera assert the shutter is **enabled** with `imageCapture` bound and `BIND_PROGRESS` gone — mirror the assertion style in `CameraScreenBindStateTest.kt` / `CameraScreenBoundStateTest.kt`.
- [ ] **JVM-layer coverage:** add/extend a `CameraViewModelTest` case for the transition (`Success` → `reset()` → `Idle`) in `app/src/test/.../camera/CameraViewModelTest.kt`.
- [ ] **Keep existing camera tests green** (by name): `CameraScreenBindStateTest.shutterIsDisabledAndBindProgressVisibleWhileImageCaptureIsNull`, `CameraScreenBoundStateTest.shutterIsEnabledAndBindProgressGoneWhenImageCaptureIsBound`, `CameraScreenSmokeTest`, `CameraFailureBannerContractTest`, and any `CameraScreenTest` / `CameraViewModelTest` failure-retry cases (`failureRetryReturnsToIdle`, `newCaptureClearsFailureState`).

## Phase 4 — Gates (cheap → expensive)

> While developing the fix, run the focused camera tests first (`./gradlew testDebugUnitTest --tests "*CameraViewModelTest*"`), then the full cheap→expensive closeout below.

- [ ] `ktlintCheck` GREEN (the only code change is the camera screen/VM + its tests).
- [ ] `testDebugUnitTest` GREEN — including the new `CameraViewModelTest` case.
- [ ] `verifyNoNetworking` GREEN — confirm no new network surface; any API-query script is outside the app source set and not wired to a Gradle app task.
- [ ] `scripts/check-stub-isolation.sh` GREEN — the UX fix introduces no fake/test identifier into app wiring.
- [ ] Instrumented camera tests on `pixel6Api34` GMD: the new shutter-on-return test + the existing bind/bound/smoke tests, all green.
- [ ] `scripts/integration-flow.ps1` cold / warm / buildonly — confirm expected artifacts unchanged (no model/KB/manifest change ⇒ no re-baseline expected).
- [ ] Confirm `git status` shows **no** new image/binary files anywhere under `app/` or `docs/` (the spike commits text reports only), and **no** research API script is referenced by any Gradle app task, app source set, Android asset dir, or instrumentation test.

## Sequencing & dependencies

```
Phase 0 (scaffold + species-targets + exclusion set + methodology + empty CSV/log)
        │
        ▼
Phase 1 (per-species ASSESS-ONLY counts across sources → CSV + query-log) ──┐  (research; no code)
        │                                                                   │
        ▼                                                                   │
Phase 2 (report → thresholds → GO/PARTIAL/NO-GO → conditional hand-off)     │
                                                                            │
Phase 3 (shutter-on-return UX fix + tests) ◄────────────────────────────────┘  (CODE; independent —
        │                                                                        runs in parallel w/ 1–2)
        ▼
Phase 4 (gates: focused camera → ktlint → JVM → net-free/stub → instrumented → integration → results → ledger)
```

- **Phase 0 gates the research** — every source query must use the controlled vocabulary + exclusion set, or counts are meaningless.
- **Phases 1–2 (research) and Phase 3 (code) are independent** and proceed in parallel; they share no files. The UX fix does not depend on the data verdict, and code gates must not wait on the research report.
- **Phase 2's hand-off is per-species** — GO species get an outline, NO-GO species a fallback; a partial-GO produces both in `finetune-sprint-outline.md`.
- **Phase 4 runs last for closeout** (focused camera tests run earlier during development).

## Risks & mitigations

- [ ] **R1 — CC-BY-NC inflates iNat counts.** Most iNat houseplant photos are CC-BY-NC, unusable for a commercial-app model. *Mitigation:* report the license breakdown; count ONLY usable licenses toward the threshold; show the NC count separately so the gap is explicit.
- [ ] **R2 — Cultivar unavailability for `philodendron-pink-princess`.** Generic philodendron imagery doesn't teach the pink-variegated cultivar. *Mitigation:* count the cultivar specifically; if below floor, flag pink-princess a standalone NO-GO even under an otherwise-GO sprint and route it to the self-shot fallback.
- [ ] **R3 — Cross-tagged lookalikes corrupt labels** (`philodendron-hederaceum` ↔ pothos; `monstera-adansonii` ↔ deliciosa; true `Pothos` genus vs `Epipremnum`; generic *Pilea* vs the production class). *Mitigation:* per-species pitfall audit + a tiny off-repo eyeball sample to estimate mislabel rate; discount raw counts accordingly.
- [ ] **R4 — Cross-source double-counting** (iNat photos resurfacing via GBIF). *Mitigation:* treat GBIF as cross-check, de-dupe by occurrence/observation id, state the de-dup method in `methodology.md`.
- [ ] **R5 — Accidental image commit / APK bloat.** *Mitigation:* eyeball samples in an off-repo scratch dir; evidence-folder `.gitignore`; Phase 4 `git status` asserts zero new binaries; nothing added to `app/src/main/assets` or `androidTest/assets`.
- [ ] **R6 — A query script smuggles a networking path into the app.** *Mitigation:* research tooling under `scripts/`/evidence dir, never invoked from a Gradle app task, never on device; `verifyNoNetworking` stays GREEN; Phase 4 asserts no app-source reference to it.
- [ ] **R7 — Spike inflates into model work.** *Mitigation:* non-goals forbid any training/conversion/bundle/`ACTIVE_MODEL_ROOT` change; the only code diff allowed is the camera screen/VM + tests.
- [ ] **R8 — UX reset fires mid-flight or clobbers the failure banner.** *Mitigation:* reset only terminal `Success` (and an explicitly-decided `Failure` rule); guard against `Capturing`/`Identifying`; cover both the JVM `CameraViewModelTest` and the instrumented return-to-camera test.
- [ ] **R9 — Counts are a moving target.** iNat/Commons grow daily. *Mitigation:* stamp the retrieval date (2026-06-05) in `query-log.md`; treat counts as a floor-as-of-date.
- [ ] **R10 — Windows-portability of query tooling.** *Mitigation:* prefer portable checks (REST/JSON via the 0007 venv pattern under the evidence dir, or plain `curl`); document the exact invocation so it reruns on Windows.
- [ ] **R11 — API rate limits / throttling.** Programmatic source queries can hit rate caps or get the IP throttled. *Mitigation:* prefer source UI counts where exact; for API calls, low-volume + throttled requests kept strictly off-device and off-build-pipeline; record any cap encountered in `query-log.md`.
- [ ] **R12 — Partial-GO mishandled as all-or-nothing.** A realistic verdict is mixed (most species GO, pink-princess NO-GO). *Mitigation:* the matrix + hand-off are per-species; the outline carries GO species and the fallback carries blockers in the same file.

## Acceptance criteria

- [ ] `docs/sprints/evidence/PLANTPOTTING-0008/data-availability-report.md` exists with a per-species table (source · usable count · raw count · license type(s) · attribution feasible · pitfall · cultivar-specific · count-confidence · disjoint-split achievable) for **all 6 OOV species + the pothos/Pilea boundary pair**.
- [ ] `source-counts.csv` (machine-readable, row-per-species/source/license-bucket) and `query-log.md` (exact queries/URLs + retrieval dates) exist and back the report.
- [ ] `go-no-go-matrix.md` gives a per-species GO/CONDITIONAL/NO-GO verdict against the ~150–300 comfortable / ~50–100 floor thresholds (boundary: ~150–300 of EACH), plus one overall sprint verdict that supports **partial-GO**, each with the deciding number shown.
- [ ] The `philodendron-pink-princess` cultivar question is answered explicitly (CC imagery of the pink-variegated cultivar specifically — `cultivar-proven` count + verdict), not folded into generic philodendron.
- [ ] Each species' report confirms train/val/test splits are achievable **disjoint from the 8 existing `identify-fixtures/` images** (with `epipremnum-aureum.jpg`'s source URL excluded from pothos counts).
- [ ] Attribution feasibility is recorded per source row (provenance-field inventory: author/source-page/permalink/date/license/attribution string), with a note on whether these are API-exposed for future auto-attribution.
- [ ] `finetune-sprint-outline.md` exists with — per GO/CONDITIONAL species — a sourcing/download plan, disjoint-split strategy, transfer-learning approach on the existing MobileNetV2, float16/INT8 export, and explicit reuse of `ModelSwapEvaluationTest` + `ACTIVE_MODEL_ROOT` + the fixture/provenance pattern; and — per NO-GO species — the paid-dataset/self-shot fallback + rough cost.
- [ ] The shutter-on-return bug is fixed: after a capture, navigating back to the camera screen re-enables the shutter, proven by a failing-then-passing instrumented test plus a `CameraViewModelTest` unit case; existing named camera tests stay green.
- [ ] No KB edits, no model/bundle/`ACTIVE_MODEL_ROOT` change, no new fixtures, no committed image data (verified by `git status`), and the `PlantIdentifier`/`IdentificationResult` seam is unchanged.
- [ ] `ktlintCheck`, `testDebugUnitTest`, `verifyNoNetworking`, `scripts/check-stub-isolation.sh`, the instrumented camera tests on `pixel6Api34`, and `scripts/integration-flow.ps1` cold/warm/buildonly are GREEN — or carry a documented, accepted blocker in `docs/sprints/results/PLANTPOTTING-0008.md`.
- [ ] `docs/sprints/results/PLANTPOTTING-0008.md` records the verdict, per-species count summary, the chosen hand-off, the UX-fix status, and all gate results; the ledger entry is flipped only after every gate is green.
