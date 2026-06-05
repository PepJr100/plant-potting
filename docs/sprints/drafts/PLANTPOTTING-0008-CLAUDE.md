# PLANTPOTTING-0008 — Training-data availability spike (gates fine-tuning) + shutter-on-return UX fix

PLANTPOTTING-0007 shipped the houseplant model swap: `house_plant_species_mobilenetv2` (MobileNetV2,
47 classes, Apache-2.0, float16 TFLite) is now the production default behind the single
`ACTIVE_MODEL_ROOT` BuildConfig switch, covering **10 of 16** KB species and beating AIY 6-to-1
on-device. That closed the "is there a better off-the-shelf model" question. What it left open is the
**other 6 KB species the model still can't name at all**, plus a known confusion where pothos is
confidently misread as Pilea. The next obvious lever is *fine-tuning the MobileNetV2 we already ship* —
but fine-tuning is only worth a sprint if license-clean training images actually exist in the quantity a
small transfer-learning run needs.

**This sprint does NOT train anything.** It is a **desk-research feasibility spike** whose primary
artifact is a written, per-species **GO / NO-GO** report backed by real license-clean image **counts**
gathered through the sources' own APIs/UI — *assess only, no bulk download, no images committed*. On GO
it hands off a scoped outline for the fine-tuning sprint; on NO-GO it documents the paid-dataset /
self-shot fallback and its cost. Folded in alongside is **one small camera UX bug** from the idea inbox
— the only code change in the sprint.

The spike must answer, per target species, one question: *does enough free, license-clean, correctly-identified, attribution-feasible imagery of THIS species (and, for the boundary case, its lookalikes) exist to build train/val/test splits that are DISJOINT from the 8 existing androidTest fixtures?*

## Target species (the whole assessment surface)

**6 out-of-vocab (OOV) KB species** — no class exists in the production model, so fine-tuning would add/relabel a head class:

- [ ] `monstera-adansonii` (Swiss-cheese vine; distinct from in-vocab `monstera-deliciosa` — fenestration pattern differs, easy to mislabel)
- [ ] `philodendron-hederaceum` (heart-leaf philodendron; the canonical pothos lookalike — see boundary problem)
- [ ] `philodendron-pink-princess` (a **specific cultivar** — pink variegation; generic philodendron imagery does NOT count)
- [ ] `ficus-lyrata` (fiddle-leaf fig)
- [ ] `chlorophytum-comosum` (spider plant)
- [ ] `hoya-carnosa` (wax plant)

**1 boundary problem (both classes already in the model, but mutually confused):**

- [ ] `epipremnum-aureum` (pothos) ↔ Pilea — the model has both classes but confidently misclassifies pothos as Pilea. Needs **extra, varied "hard example" imagery of BOTH** pothos and Pilea (including the visually-confusable shots), not a new class. Assess both as a pair.

## Goals

- [ ] **G1 — Per-species sourcing report.** For every target above, a row-per-source table of license-clean image counts, license types, attribution feasibility, taxonomic/synonym/cultivar pitfalls, and a disjoint-split feasibility check.
- [ ] **G2 — GO/NO-GO recommendation.** A per-species verdict against the agreed thresholds, plus one overall sprint-level GO/NO-GO, with the deciding numbers shown.
- [ ] **G3 — Conditional hand-off.** On GO, a scoped outline for the fine-tuning sprint (sourcing/download plan, disjoint-split strategy, transfer-learning on the existing MobileNetV2, float16/INT8 export, reuse of `ModelSwapEvaluationTest` + `ACTIVE_MODEL_ROOT` + fixture/provenance pattern). On NO-GO, the paid-dataset / self-shot fallback + cost.
- [ ] **G4 — Shutter-on-return UX fix.** The shutter button re-enables when the user navigates back to the camera screen after a capture, with a regression test. The only code change in the sprint.
- [ ] **G5 — Constraints preserved.** `verifyNoNetworking` and `scripts/check-stub-isolation.sh` stay GREEN; no KB edits; no model/bundle/`ACTIVE_MODEL_ROOT` changes; no committed image data; the `PlantIdentifier`/`IdentificationResult` seam stays frozen.

## Non-goals / scope boundaries

- [ ] **No ML training or fine-tuning in this sprint.** This spike only decides *if* a later fine-tuning sprint is viable. No `.h5`/checkpoint training, no transfer-learning runs, no head surgery.
- [ ] **No model work.** No model conversion, no new model bundle under `app/src/main/assets/ml/`, no change to the `ACTIVE_MODEL_ROOT` default (`ml/house_plant_species_mobilenetv2`), no manifest/label-map/threshold edits.
- [ ] **No KB edits.** `app/src/main/assets/kb/species.json` and `archetypes.json` are LOCKED. (The species list is read-only input to the report.)
- [ ] **No bulk image download; no images committed.** Assess counts via APIs/UI/search only. NO sample images in the repo or the APK. Any tiny manual eyeball check stays **off-repo** (local scratch dir outside the project tree, never staged).
- [ ] **No new fixtures.** The 8 existing `identify-fixtures/` JPGs are untouched; the spike only references them to define the disjoint-split exclusion set.
- [ ] **Seam frozen.** `PlantIdentifier.identify(jpeg): IdentificationResult` and `IdentificationResult` unchanged. The UX fix is confined to the camera screen / `CameraViewModel` state — no identifier-layer changes.
- [ ] **No production networking.** Any API-query tooling lives under `docs/sprints/evidence/PLANTPOTTING-0008/` or `scripts/`, is **never** invoked from a Gradle app task, and never runs on device. `verifyNoNetworking` stays GREEN.
- [ ] **No version bumps** (AGP/Kotlin/Compose/Hilt/TFLite).

## Sources to assess (license-clean only)

Each carries a different license/redistribution profile — the report must separate *count* from *usability*:

- [ ] **iNaturalist** — research-grade observations, per-observation CC license (CC0 / CC-BY / CC-BY-NC). Filter to research-grade + the specific taxon; record the CC-license breakdown because **CC-BY-NC is unusable for a commercial app model** even though it inflates raw counts.
- [ ] **GBIF** — aggregates iNat + herbaria + others; good for cross-checking taxon IDs and synonyms; record media-license field, and de-dupe against iNat to avoid double-counting.
- [ ] **Wikimedia Commons** — CC0 / CC-BY-SA / public-domain; the source the existing fixtures already use (so attribution-block feasibility is proven). Usually lower volume per species but high label quality.
- [ ] **Flickr (CC-licensed)** — search by license + species name; noisier labels (common-name tagging, mislabels), so flag as "needs manual label verification before use".
- [ ] **Any other obvious CC/CC0 source** encountered during the sweep (e.g. Pl@ntNet open data, Plant.id open sets) — record only if license-clean and redistributable; otherwise list as "reference-only, excluded from counts".

## Phase 0 — Scaffolding & inputs (no networking)

- [ ] Create the evidence dir `docs/sprints/evidence/PLANTPOTTING-0008/` (mirror the 0007 structure).
- [ ] Extract the canonical KB species names + any encoded aliases for the 7 targets from `app/src/main/assets/kb/species.json` (read-only) into `docs/sprints/evidence/PLANTPOTTING-0008/species-targets.md` — record KB id, scientific name, accepted synonyms, and known lookalikes per target. This is the controlled vocabulary every source query must use.
- [ ] Record the **disjoint-split exclusion set**: list the 8 existing fixtures in `app/src/androidTest/assets/identify-fixtures/` (and their Wikimedia source URLs from `LICENSE.txt`) so the report can confirm training data can be split train/val/test **without reusing any fixture image**. Note: of the 8 fixtures, `epipremnum-aureum.jpg` overlaps the boundary target — its source URL MUST be excluded from any pothos training count.
- [ ] Write a one-page **methodology note** at `docs/sprints/evidence/PLANTPOTTING-0008/methodology.md`: which query was run per source, what "license-clean" means here (CC0/CC-BY/CC-BY-SA/PD = usable; CC-BY-NC/ND = excluded-from-usable-count but recorded), how counts were de-duped across sources, and the threshold definitions below. Stamp the retrieval date (2026-06-05) — counts are a snapshot.

## Phase 1 — Per-species data assessment (the core research, ASSESS-ONLY)

> One pass per target species (×7) × per source (×4–5). No bulk download. Optionally a *tiny* off-repo eyeball sample (≤~20 images) per species to sanity-check label quality and the cultivar question — never committed.

- [ ] **iNaturalist counts.** For each target, query research-grade observations for the taxon and record total + the CC-license breakdown (CC0 / CC-BY / CC-BY-SA vs the unusable CC-BY-NC / CC-BY-ND). Use the iNat API/export UI; capture the exact query string in the methodology note. (Any query script lives under `scripts/` or the evidence dir — never a Gradle task.)
- [ ] **GBIF counts.** Cross-check the taxon key, record media-bearing occurrence counts + license field, and estimate the iNat-overlap so GBIF isn't double-counted.
- [ ] **Wikimedia Commons counts.** Count category/search hits for the species (and synonyms), note the dominant license, and confirm the existing provenance-block pattern transfers.
- [ ] **Flickr-CC counts.** Count CC-licensed results by species + common name; flag label-noise risk (e.g. "pothos" tag applied to heart-leaf philodendron).
- [ ] **Per-species pitfall audit.** For each target, document the taxonomic/synonym/cultivar trap and how it inflates or corrupts raw counts:
  - [ ] `monstera-adansonii` vs `monstera-deliciosa` — confirm counts aren't dominated by deliciosa mislabels.
  - [ ] `philodendron-hederaceum` vs `epipremnum-aureum` — the two are routinely cross-tagged; record how much of each count is likely the other.
  - [ ] `philodendron-pink-princess` — **is CC imagery of the specific pink-variegated cultivar available**, vs generic green philodendron? Count the cultivar specifically; record the floor honestly (this is the highest-risk target).
  - [ ] `ficus-lyrata`, `chlorophytum-comosum`, `hoya-carnosa` — confirm species-level label fidelity; note `chlorophytum-comosum` variegation variants.
  - [ ] Sansevieria→Dracaena / Pothos vs Scindapsus / Philodendron lookalike notes captured from the existing `ml-mapping-notes.md` alias discipline so synonym handling is consistent.
- [ ] **Pothos/Pilea boundary assessment (special handling).** Assess `epipremnum-aureum` AND Pilea as a pair: count varied "hard example" imagery for EACH (including the visually-confusable angles — trailing vines, juvenile leaves, similar leaf shapes) toward the ~150–300-each target. This is a *boundary-hardening* data need, not a new class.
- [ ] **Attribution feasibility.** Per source, confirm each candidate image can carry a provenance block like the existing `identify-fixtures/LICENSE.txt` fixtures (Title / Depicts / Source page / Author / Date / License + attribution string). Record whether the source's API exposes author + license + permalink programmatically (it does for iNat/Commons/Flickr) so attribution can be auto-generated at download time in the future sprint.
- [ ] **Disjoint-split feasibility per species.** For each target, confirm the usable count is large enough to form **train/val/test splits disjoint from the 8 fixtures** (and from each other), i.e. usable-count ≥ floor AND no reliance on fixture source URLs. Record a one-line yes/no + the split sketch (e.g. "210 usable → 150/30/30, fixtures excluded").

## Phase 2 — Report & GO/NO-GO synthesis

- [ ] Write the **per-species sourcing report** at `docs/sprints/evidence/PLANTPOTTING-0008/data-availability-report.md` — one table per species with columns: **source · license-clean count · license type(s) · attribution feasible (Y/N) · taxonomic/synonym/cultivar pitfall · cultivar-specific (Y/N/n-a) · disjoint-split achievable (Y/N)**. Plus a **boundary** sub-table for pothos vs Pilea hard examples.
- [ ] Add a **roll-up matrix** `docs/sprints/evidence/PLANTPOTTING-0008/go-no-go-matrix.md`: one row per target with summed usable count, the threshold band it lands in, and a per-species GO / NO-GO / CONDITIONAL verdict.
- [ ] Apply the **thresholds** (state the number that decides each):
  - **Comfortable / GO:** ~150–300 license-clean usable images/species.
  - **Floor / CONDITIONAL:** ~50–100/species (viable but thin — note augmentation/few-shot risk).
  - **Below floor / NO-GO:** < ~50 usable/species.
  - **Boundary:** ~150–300 of EACH of pothos and Pilea, including lookalike hard shots.
- [ ] Decide and record the **overall sprint GO/NO-GO**: GO only if a *useful majority* of targets clear the floor AND the pothos/Pilea pair is sourceable; otherwise NO-GO or partial-GO (name which species are in/out).
- [ ] **On GO — fine-tuning sprint outline** at `docs/sprints/evidence/PLANTPOTTING-0008/finetune-sprint-outline.md`: sourcing/download plan (per-source quotas + attribution capture), disjoint-split strategy vs the 8 fixtures, transfer-learning approach on the existing `house_plant_species_mobilenetv2` MobileNetV2 (which classes added/relabeled, frozen-backbone vs full fine-tune), float16/INT8 export path, and explicit reuse of `ModelSwapEvaluationTest` + the `ACTIVE_MODEL_ROOT` switch + the `identify-fixtures/` provenance pattern for new eval fixtures.
- [ ] **On NO-GO — fallback memo** in the same outline file: paid-dataset options (name candidates + rough licensing cost) and the self-shot-photo plan (how many shots/species, who, attribution-trivial since first-party), plus which subset of species the fallback would still cover.
- [ ] Write `docs/sprints/results/PLANTPOTTING-0008.md`: the verdict, the per-species count summary, the chosen hand-off (outline or fallback), the UX-fix status, and the gate results. Flip the ledger only after every gate is green.

## Phase 3 — Shutter-on-return UX fix (the only code change)

**Root cause (confirmed in `CameraScreen.kt:191`):** the shutter is enabled only when
`(state is Idle || state is Failure) && imageCapture != null`. After a capture the `CameraViewModel`
state advances to `CameraUiState.Success(speciesId)` (`CameraViewModel.kt:51`) and the view model
survives in the Compose back-stack-entry `ViewModelStore`. Navigating **back** from the results screen
returns to the camera with state still `Success` → the shutter stays at `alpha 0.5f` / `disabled()` and
never re-enables. The fix is to return the camera to `Idle` when it is re-shown.

- [ ] Reproduce: instrumentation/Compose test that drives `onCaptureReady(...)` to reach `Success`, then simulates return-to-camera, and asserts the shutter is **not enabled** (red/failing first).
- [ ] Implement the re-enable: reset `CameraViewModel` state to `CameraUiState.Idle` when the camera screen is (re)shown — preferred approach: a lifecycle-aware effect in `CameraScreen` (e.g. `ON_RESUME`/`ON_START` observer, or `LaunchedEffect` keyed on screen re-entry) that calls the existing `viewModel.reset()` (`CameraViewModel.kt:61`) when the current state is a terminal `Success`/`Failure`. Do **not** touch `PlantIdentifier`/`IdentificationResult` or the nav-command contract.
- [ ] Guard the in-flight states: ensure the reset does NOT fire while `Capturing`/`Identifying` (only resets terminal `Success` — and decide explicitly whether returning on `Failure` should also reset, preserving the existing failure-banner retry behaviour). Document the chosen rule in a code comment.
- [ ] Make the reproduction test pass; assert on return-to-camera the shutter is **enabled** again (with `imageCapture` bound) and `BIND_PROGRESS` is gone — mirror the existing assertion style in `CameraScreenBindStateTest.kt` / `CameraScreenBoundStateTest.kt`.
- [ ] Add/extend a `CameraViewModelTest` unit case for the state transition (`Success` → `reset()` → `Idle`) so the logic is covered at the JVM layer too (`app/src/test/.../camera/CameraViewModelTest.kt`).
- [ ] Keep existing camera tests green: `CameraScreenBindStateTest`, `CameraScreenBoundStateTest`, `CameraScreenSmokeTest`, `CameraFailureBannerContractTest`, `CameraScreenTest`.

## Phase 4 — Gates (cheap → expensive)

- [ ] `ktlintCheck` GREEN (the only code change is the camera screen/VM + its test).
- [ ] `testDebugUnitTest` GREEN — including the new `CameraViewModelTest` case.
- [ ] `verifyNoNetworking` GREEN — confirm no new network surface; any API-query script is outside the app source set and not wired to a Gradle app task.
- [ ] `scripts/check-stub-isolation.sh` GREEN — the UX fix introduces no fake/test identifier into app wiring.
- [ ] Instrumented camera tests on `pixel6Api34` GMD: the new shutter-on-return test + the existing bind/bound/smoke tests, all green.
- [ ] `scripts/integration-flow.ps1` cold / warm / buildonly — confirm expected artifacts unchanged (no model/KB/manifest change ⇒ no re-baseline expected).
- [ ] Confirm `git status` shows **no** new image files anywhere under `app/` or `docs/` (the spike committed text reports only).

## Sequencing & dependencies

```
Phase 0 (scaffold + species targets + exclusion set + methodology)
        │
        ▼
Phase 1 (per-species ASSESS-ONLY counts across sources)  ──┐  (research; no code)
        │                                                  │
        ▼                                                  │
Phase 2 (report → thresholds → GO/NO-GO → conditional hand-off)
                                                           │
Phase 3 (shutter-on-return UX fix + tests) ◄──────────────┘  (CODE; fully independent —
        │                                                      can run in parallel with 1–2)
        ▼
Phase 4 (gates: ktlint → JVM → net-free/stub → instrumented → integration → results → ledger)
```

- **Phase 0 gates the research** — every source query must use the controlled vocabulary + exclusion set, or counts are meaningless.
- **Phases 1–2 (research) and Phase 3 (code) are independent** and may proceed in parallel; they share no files. The UX fix does not depend on the data verdict.
- **Phase 2's hand-off is conditional** on the verdict — write the outline OR the fallback memo, not both fully (the unused one gets a one-line "not applicable, see verdict").
- **Phase 4 runs last** — gates exercise the single code change; the research artifacts are text-only and don't affect build gates beyond the "no committed images" check.

## Risks & mitigations

- [ ] **R1 — CC-BY-NC inflates iNat counts.** Most iNat houseplant photos are CC-BY-NC, which is unusable for a commercial-app model. *Mitigation:* report the license breakdown and count ONLY usable licenses toward the threshold; show the NC count separately so the gap is explicit.
- [ ] **R2 — Cultivar unavailability for `philodendron-pink-princess`.** Generic philodendron imagery doesn't teach the pink-variegated cultivar. *Mitigation:* count the cultivar specifically; if it falls below floor, flag pink-princess as a standalone NO-GO even if the sprint is otherwise GO, and route it to the self-shot fallback.
- [ ] **R3 — Cross-tagged lookalikes corrupt labels** (`philodendron-hederaceum` ↔ pothos; `monstera-adansonii` ↔ deliciosa). *Mitigation:* per-species pitfall audit + a tiny off-repo eyeball sample to estimate mislabel rate; discount raw counts accordingly.
- [ ] **R4 — Cross-source double-counting** (iNat photos resurfacing via GBIF). *Mitigation:* treat GBIF as cross-check, de-dupe by occurrence/observation id, and state the de-dup method in the methodology note.
- [ ] **R5 — Accidental image commit / APK bloat.** *Mitigation:* keep all eyeball samples in an off-repo scratch dir; Phase 4 `git status` check asserts zero new binaries; nothing added to `app/src/main/assets` or `androidTest/assets`.
- [ ] **R6 — A query script smuggles in a networking path into the app.** *Mitigation:* research tooling lives under `scripts/`/evidence dir, is never invoked from a Gradle app task, never runs on device; `verifyNoNetworking` stays GREEN.
- [ ] **R7 — Spike inflates into model work.** *Mitigation:* non-goals forbid any training/conversion/bundle/`ACTIVE_MODEL_ROOT` change; the only code diff allowed is the camera screen/VM + tests.
- [ ] **R8 — UX reset fires mid-flight or clobbers the failure banner.** *Mitigation:* reset only terminal `Success` (and an explicitly-decided `Failure` rule); guard against `Capturing`/`Identifying`; cover both the JVM `CameraViewModelTest` and the instrumented return-to-camera test.
- [ ] **R9 — Counts are a moving target.** iNat/Commons grow daily. *Mitigation:* stamp the retrieval date (2026-06-05) in every report; treat counts as a floor-as-of-date, not a fixed figure.
- [ ] **R10 — Windows-portability of query tooling.** *Mitigation:* prefer portable checks (REST/JSON via the existing PLANTPOTTING-0007 venv pattern under the evidence dir, or plain `curl`); document the exact invocation so it reruns on Windows.

## Acceptance criteria

- [ ] `docs/sprints/evidence/PLANTPOTTING-0008/data-availability-report.md` exists with a per-species table (source · license-clean count · license type(s) · attribution feasible · taxonomic/synonym/cultivar pitfall · cultivar-specific · disjoint-split achievable) for **all 6 OOV species + the pothos/Pilea boundary pair**.
- [ ] `docs/sprints/evidence/PLANTPOTTING-0008/go-no-go-matrix.md` gives a per-species GO/NO-GO/CONDITIONAL verdict against the ~150–300 comfortable / ~50–100 floor thresholds (boundary: ~150–300 of EACH), plus one overall sprint verdict, each with the deciding number shown.
- [ ] The `philodendron-pink-princess` cultivar question is answered explicitly (CC imagery of the pink-variegated cultivar specifically — count + verdict), not folded into generic philodendron.
- [ ] Each species' report confirms train/val/test splits are achievable **disjoint from the 8 existing `identify-fixtures/` images** (with `epipremnum-aureum.jpg`'s source URL excluded from pothos counts).
- [ ] Attribution feasibility is recorded per source (each candidate image can carry a provenance block like the existing fixtures), with a note on whether author/license/permalink are API-exposed for future auto-attribution.
- [ ] On GO: `finetune-sprint-outline.md` exists with sourcing/download plan, disjoint-split strategy, transfer-learning approach on the existing MobileNetV2, float16/INT8 export, and explicit reuse of `ModelSwapEvaluationTest` + `ACTIVE_MODEL_ROOT` + the fixture/provenance pattern. On NO-GO: the paid-dataset/self-shot fallback + rough cost is documented instead.
- [ ] The shutter-on-return bug is fixed: after a capture, navigating back to the camera screen re-enables the shutter, proven by a failing-then-passing instrumented test plus a `CameraViewModelTest` unit case; existing camera tests stay green.
- [ ] No KB edits, no model/bundle/`ACTIVE_MODEL_ROOT` change, no new fixtures, no committed image data (verified by `git status`), and the `PlantIdentifier`/`IdentificationResult` seam is unchanged.
- [ ] `ktlintCheck`, `testDebugUnitTest`, `verifyNoNetworking`, `scripts/check-stub-isolation.sh`, the instrumented camera tests on `pixel6Api34`, and `scripts/integration-flow.ps1` cold/warm/buildonly are GREEN — or carry a documented, accepted blocker in `docs/sprints/results/PLANTPOTTING-0008.md`.
- [ ] `docs/sprints/results/PLANTPOTTING-0008.md` records the verdict, per-species count summary, the chosen hand-off, the UX-fix status, and all gate results; the ledger entry is flipped only after every gate is green.
