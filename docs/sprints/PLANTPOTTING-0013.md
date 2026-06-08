# PLANTPOTTING-0013 — Direct Pilea care card (conditional) + honest CC0/PD sample-count growth + doc cleanup (v0.7.0)

## Intent

Two independent threads plus a documentation fix, **all behind the frozen `PlantIdentifier` seam** — no
model swap, no training, no networking, no self-shot / first-party imagery.

**Thread A (headline) — permit a *direct* Pilea care card, but only if it earns it.** PLANTPOTTING-0012
mapped `pilea-peperomioides` (class-map count 39) and shipped it **strict-picker**: a `boundary_pairs`
rule in `model_manifest.json` forces *any* top-1 = Pilea result to the `LowConfidencePicker` (Pilea +
pothos both surfaced), CI-bound so Pilea can never map without the gate. The 0012 at-home review found
this too conservative: a **real** Pilea photo was identified correctly at **98%** with **no pothos
competing** (runners-up English Ivy 1%, ZZ plant) — yet it still landed in the picker. The boundary
confusion is **asymmetric**: the dangerous error is *pothos input → Pilea label* @ **0.9661**, **not**
*Pilea input → pothos*. So a real, confident Pilea is conservatively demoted. This sprint refines the
existing gate so a top-1 = Pilea result can yield a **direct** Pilea card **only** when Pilea clears an
elevated, Pilea-specific `per_species_thresholds` bar — *composed with* (never replacing) the existing
top-1=Pilea boundary rule so the asymmetric pothos→Pilea error **still** routes to the picker. The direct
card **ships only if** it clears a pre-committed **+0 confident-wrong** bar under the strictest eval the
thin set allows — **leave-one-out (LOO)** held-out evaluation **and** author/source separation between
threshold-tuning and validation photos, on the **existing ~6 Pilea fixtures** (deliberately **not**
deepened — we do not source Pilea photos to flatter its own gate). If the eval does not clear the bar,
**fall back to strict-picker and document why**. CI must bind the invariant: a pothos→Pilea input can
**never** reach a direct Pilea card.

**Thread B — grow the honest real-world sample count (no model change).** 8 mapped species currently
have only **1 CC0 fixture each** (the 0012 "previously-untested mapped" additions). The honest headline
accuracy number is bounded by this thin clean set. Source **additional CC0/PD** fixtures spread
**broadly** across those 8, keep all fixture license/integrity/schema gates green, and re-run the
`AccuracyEvalTest` scorecard so the headline number reflects the larger set. **Pilea is deliberately NOT
deepened.**

**Doc cleanup.** Scrub / mark-as-historical the stale 0009-era Pilea-deferral prose in
`docs/kb/ml-mapping-notes.md` (≈ lines 270 and 331) that still asserts in the absolute present tense that
Pilea is unmapped / the boundary fix is "still deferred" / `pileaIsNotMapped` passes — all contradicted
by 0012 reality (Pilea IS mapped behind the gate).

**Ship signal:** v0.7.0 / versionCode 7; debug APK delivered to the Dropbox folder **and** the `v0.7.0`
git tag pushed so a public GitHub Release with the APK is published.

---

## Goals

- [ ] **Thread A — gate refinement:** refine the pothos↔Pilea `boundary_pairs` early-return in `ModelScoreMapper` so a top-1 = Pilea result can yield a **direct** Pilea care card **only** when `bestProb ≥ per_species_thresholds["pilea-peperomioides"]` (an elevated, Pilea-specific bar well above the global 0.55), composed so the asymmetric pothos→Pilea error (top-1 Pilea @ 0.9661, no second-place mass) **still** routes to the `LowConfidencePicker`.
- [ ] **Thread A — conditional ship:** the direct card ships **only if** a pre-committed bar (**+0 confident-wrong on held-out** vs. the strict-picker baseline, on clean fixtures + all perturbations + the pothos/Pilea subset) is cleared under **leave-one-out** evaluation **and** author/source separation, on the **existing 6 Pilea fixtures**; otherwise **fall back to strict-picker** and write a blocker note explaining why. The fallback is a valid sprint outcome.
- [ ] **Thread A — CI invariant (two-layer):** (1) a unit test pinning the pothos→Pilea 0.9661 row to the picker **even with** the direct-card threshold configured, and (2) a **config-level** `ModelManifestTest` bind that fails CI if `per_species_thresholds["pilea-peperomioides"]` is present and **≤ 0.9661** (the documented pothos ceiling). So a pothos→Pilea input can never reach a direct Pilea card.
- [ ] **Thread B:** source **additional CC0/PD** fixtures spread broadly across the 8 single-photo mapped species (license-clean supply is the ceiling), keeping `FixtureLicenseManifestTest` / `FixtureIntegrityTest` / `AccuracyEvalCsvSchemaTest` green; close with a reason-coded coverage table.
- [ ] **Thread B:** re-run the `AccuracyEvalTest` scorecard on the expanded fixture set and record the refreshed honest headline number (clean vs perturbation metrics reported separately).
- [ ] **Doc cleanup:** correct the superseded present-tense Pilea-deferral claims in `docs/kb/ml-mapping-notes.md` (mark historical, keep the per-sprint-section convention — don't delete the history).
- [ ] **Ship:** v0.7.0 / versionCode 7, all gates green, debug APK **verified present** in the Dropbox folder, **and** the `v0.7.0` tag pushed → public GitHub Release published (workflow result confirmed).

## Non-goals / scope boundaries

- [ ] Do **not** touch the frozen seam: `PlantIdentifier`, `IdentificationResult`, `IdSource` stay byte-for-byte unchanged. All new logic lives in `ModelScoreMapper`, `ModelManifest` parsing, manifest config, internal candidate construction, and tests.
- [ ] Do **not** swap, retrain, fine-tune, re-quantize, or otherwise alter `house_plant_species_mobilenetv2`, the `ACTIVE_MODEL_ROOT`, or the AIY V1/3 regression anchor.
- [ ] Do **not** deepen the Pilea fixture set, swap the AIY-anchor `monstera-deliciosa.jpg`, or add any self-shot / first-party / generated imagery anywhere.
- [ ] **New fixtures this sprint are CC0 / PD / PD-mark real photographs only** — reject CC-BY / SA / NC / ND / unknown / watermarked / botanical-plate / illustration / AI / duplicate / cultivar-mismatched candidates (matches the 0011/0012 CC0/PD eval-fixture discipline and the stated intent). Log every reject with a reason code.
- [ ] Do **not** re-litigate **TTA** — `tta` stays **6**. The boundary gate is deterministic and TTA-independent; the 0012 sweep already showed ×8/×10/×20 / grid tiling buy nothing within the ~2 s budget and 3×3 busts the cap. Only revisit if the fixture set changes *character* (it doesn't — Thread B adds the same kind of clean whole-plant photos).
- [ ] Do **not** loosen the global plain/margin gates or the 0011 `high_confidence_abstain_margin` (0.30) to flatter Pilea (0006 anti-overfit discipline). The elevated Pilea bar is an **additional** gate, never a relaxation. The margin-over-second signal is **not** the load-bearing separator (see A0) — at most a belt-and-suspenders sanity guard.
- [ ] Do **not** loosen `verifyNoNetworking`, `ktlintCheck`, `check-stub-isolation.sh`, or the fixture license/integrity/CSV-schema guards.
- [ ] Do **not** bump AGP / Kotlin / Compose / Hilt / TFLite or any platform dependency.
- [ ] Do **not** add KB content (the `pilea-peperomioides` species entry already exists from 0012); no new species, archetypes, or reference images.

## Repo anchors (confirm each on disk before editing — line numbers are drafter estimates)

- [ ] Gate logic: `app/src/main/java/com/darkfactory/plantpotting/identify/model/ModelScoreMapper.kt` — the boundary-pair check (≈ lines 89–103) currently fires **unconditionally** for top-1 = Pilea and `return`s `lowConfidence(...)` *before* the high-conf/abstain path; the per-species `plainThreshold` lookup is ≈ lines 69–74 (`perSpeciesThresholds`, added 0005 §5.3). The early-return is the surface Thread A modifies.
- [ ] Manifest model + parser: `.../identify/model/ModelManifest.kt` — `BoundaryPair(top1KbSpeciesId, surfaceKbSpeciesIds)`, `perSpeciesThresholds: Map<String,Float>`, `Thresholds`, `ModelManifestReader.parse(...)`.
- [ ] Config: `app/src/main/assets/ml/house_plant_species_mobilenetv2/model_manifest.json` — `per_species_thresholds: {}` (with the existing `_comment_per_species_thresholds` 0006-discipline note), `thresholds` (plain 0.55 / margin / `high_confidence_abstain_margin` 0.30 / top_k 3), `boundary_pairs` (top1 `pilea-peperomioides` → low_confidence, surfacing Pilea + `epipremnum-aureum`), `tta: 6`.
- [ ] Class map: `.../house_plant_species_mobilenetv2/plant_class_map.json` — 39 mapped classes incl. Pilea.
- [ ] DI wiring: `.../identify/OnDeviceIdentifyModule.kt` — `provideBoundaryPairs` / `providePerSpeciesThresholds` / `provideThresholds` already thread manifest config into `ModelScoreMapper`. No new providers expected.
- [ ] Unit tests (JVM): `ModelScoreMapperBoundaryGateTest.kt`, `ModelScoreMapperPerSpeciesThresholdTest.kt`, `ModelScoreMapperAbstainMarginTest.kt`, `PerSpeciesThresholdsContractTest.kt`, `CandidateBundleContractTest.kt`, `ModelManifestTest.kt`.
- [ ] Map-validation gate: `.../identify/HousePlantClassMapValidationTest.kt` — `mapsExactlyThirtyNineClasses`, `pileaMapsToPileaPeperomioides`, `pileaMappingRequiresBoundaryGate`. These stay green; count stays **39**.
- [ ] Eval harness (instrumented, `pixel6Api34`): `app/src/androidTest/java/.../identify/AccuracyEvalTest.kt` — emits `accuracy-eval.csv` + summary; pull via `adb pull /sdcard/Android/data/com.darkfactory.plantpotting/files/...` (CSV is **wiped on reinstall** — use the manual `adb install` + `am instrument` + `adb pull` workflow, PowerShell for `/sdcard` paths).
- [ ] Fixtures: `app/src/androidTest/assets/identify-fixtures/` — flat `<kb-species-id>__NN.jpg`, with `LICENSE.txt` + `fixture-manifest.tsv` (filename, expected_species_id, source_url, author, license, license_url, acquisition_date, notes). The `author` column is the basis for author/source separation. 6 Pilea fixtures present: `pilea-peperomioides__01..06.jpg`; their authors per the manifest are **dinomariobob, Tiago Lubiana, Olsza Borys, Daniel Atha, dmagdee, Curran Dwyer** (confirm against the TSV before relying on the partition).
- [ ] Version: `app/build.gradle.kts` (`versionCode = 6`, `versionName = "0.6.0"` → 7 / "0.7.0"). Doc-cleanup target: `docs/kb/ml-mapping-notes.md` (≈430 lines). Release workflow: `.github/workflows/release.yml` (fires on pushed `v*` tag). Evidence dir: `docs/sprints/evidence/PLANTPOTTING-0013/`.

---

## Phase 1 — Cold start: read 0012, confirm state, baseline

- [x] Read `docs/sprints/PLANTPOTTING-0012.md`, `docs/sprints/feedback/PLANTPOTTING-0012/feedback.md`, and the 0012 evidence (`boundary-gating-decision.md`, `post-pilea-gated-summary.md`) so the executor starts from the real gate + fixture state.
- [x] Confirm `ModelScoreMapper` currently forces any top-1 resolving to `pilea-peperomioides` through `boundary_pairs` to `LowConfidencePicker`, and that production `per_species_thresholds = {}`, `tta = 6`, plain 0.55, abstain margin 0.30. *(Confirmed: `ModelScoreMapper.kt:94-103` early-return; manifest `per_species_thresholds: {}`, `tta: 6`, plain 0.55, abstain 0.30.)*
- [x] Confirm `HousePlantClassMapValidationTest` still asserts `mapsExactlyThirtyNineClasses`, `pileaMapsToPileaPeperomioides`, `pileaMappingRequiresBoundaryGate`. *(Confirmed present.)*
- [x] Inventory `fixture-manifest.tsv`; record the 8 single-photo target species (each currently exactly one `__01.jpg`) and the 6 Pilea fixtures with their authors. *(In evidence README: targets = saintpaulia-ionantha, chamaedorea-elegans, beaucarnea-recurvata, alocasia, dracaena, begonia, ctenanthe, schlumbergera-bridgesii; 6 Pilea authors all distinct.)*
- [x] Create `docs/sprints/evidence/PLANTPOTTING-0013/README.md` with baseline fixture counts, the Pilea no-new-fixtures constraint, and the planned evidence files.
- [x] Run the **current strict-picker** production app through `AccuracyEvalTest` on `pixel6Api34`; save `baseline-strict-picker-eval.csv` + `baseline-strict-picker-summary.md`. This is the **+0-confident-wrong reference** Thread A must not regress against. Confirm all 6 Pilea fixtures route low-conf with Pilea visible/first, and all pothos fixtures (incl. the base `epipremnum-aureum` @ 0.9661) route low-conf. *(Production config byte-identical to 0012-shipped + no model/preprocessing/fixture change → committed 0012 `post-pilea-gated-eval.csv` IS this baseline; carried forward as `baseline-strict-picker-eval.csv` rather than a redundant identical device cycle — see README rationale. All 6 Pilea route low-conf Pilea-first, all pothos route low-conf (table in 0012 post-pilea-gated-summary §3/§4). Fresh device run done on the **expanded** set for the final scorecard.)*

---

## Thread A — Direct Pilea care card (conditional)

### Phase A0 — State the crux honestly (read before designing)

- [x] Internalise the central problem: a **real Pilea** (~0.98–1.00 top-1, no second-place mass) and a **pothos misread as Pilea** (0.9661 top-1, no second-place mass) are **near-identical in score shape**. The top1−top2 **margin cannot separate them** (both are huge). The **only** separating lever the score vector offers is the **absolute Pilea top-1 value**: the worst pothos→Pilea sits at 0.9661; the real Pilea fixtures sit higher. A direct card is only safe behind a threshold `T_pilea` set **strictly above 0.9661** with a safety margin. **Record "margin-over-second as the separator" as a rejected mechanism** so no one spends a phase tuning a dead lever. *(Recorded in `pilea-direct-card-eval-plan.md` §A0 + decision doc.)*
- [x] Internalise the **n=1 reality**: per the 0012 per-fixture breakdown, **only the base `epipremnum-aureum.jpg` actually mispredicts as Pilea** (the other pothos fixtures predict pothos / alocasia / calathea / monstera). So `T_pilea` is being fit above what may be a **single** dangerous data point — *more* overfit-prone, not less. This makes the fail-safe **load-bearing** and the bar **conservative**: confirm the breakdown from the Phase-1 baseline CSV before relying on it, and treat any newly-surfaced pothos→Pilea fixture as raising the ceiling. *(Confirmed from baseline CSV: clean = base pothos only @0.9661 squash / 0.6566 tta6. A 2nd, **synthetic-perturbation** event surfaced — `epipremnum-aureum__06` rotate_-90 @0.9063 tta6 — raising the shipped ceiling to 0.9063; still well below T_pilea=0.98.)*

### Phase A1 — Measure the separation under the strictest eval the fixtures allow

- [x] Create `pilea-direct-card-eval-plan.md` in the evidence dir **before** changing any production logic — a pre-registered protocol: the candidate rule, the **pre-committed threshold grid** (register the full candidate set *before* seeing any fold, so LOO selection can't leak), the LOO + author-separation method, the pass/fail bar (+0 confident-wrong on held-out), and the fall-back rule.
- [x] From the Phase-1 baseline CSV extract, per fixture, the **raw Pilea top-1 score** for every `epipremnum-aureum__*` (pothos) and every `pilea-peperomioides__*` (Pilea) fixture (squash = shipped control; tta6 as cross-check). Build two distributions: **pothos→Pilea confidence** (ceiling = the dangerous bound, expected n=1) and **real-Pilea confidence** (floor = what a direct card must clear). *(Done via `simulate_pilea_threshold.py`. Correction to the plan's parenthetical: **tta6 is the shipped pipeline** (manifest tta=6); squash/cc are non-shipped 0011 diagnostics. Distributions in decision doc.)*
- [x] **Separation-gap early-abort (crisp go/no-go):** compute `gap = min(real-Pilea top-1) − max(pothos→Pilea top-1)`. If `gap ≤ 0` (a pothos can reach as high as the weakest real Pilea), **no safe `T_pilea` exists → fall back to strict-picker now** (skip to the A4 blocker path). If `gap > 0`, proceed. *(Shipped tta6 gap = **+0.0877** → proceed. Diagnostic squash/cc gap = −0.0176 from synthetic perturbations in non-shipped modes; principal-confirmed the shipped-pipeline reading governs.)*
- [x] Build an **offline threshold-simulation script** under the evidence dir that simulates candidate `T_pilea` values from the CSV **without changing app code** — this decouples threshold iteration from the slow, wipe-on-reinstall GMD device cycle. Run the pre-committed grid through it. *(`simulate_pilea_threshold.py`.)*
- [x] Run the **leave-one-out** protocol over the 6 Pilea fixtures with **author/source separation**: for each held-out Pilea fixture, derive `T_pilea` from the other 5 (and the pothos ceiling) using only the pre-committed grid, ensuring the held-out fixture's **author** is not among the tuning photos' authors (drop or note any fold that can't be author-separated; document the partition). A candidate "passes LOO" only if **every** held-out fold keeps **+0 confident-wrong** (no pothos crosses `T_pilea`); restoring the held-out real-Pilea direct card is the *benefit*, not the safety bar. *(All 6 authors distinct → every fold author-separated. **All 6 folds PASS +0 confident-wrong** on the shipped pipeline.)*
- [x] Emit `pilea-direct-card-threshold-sweep.csv` (fold id, tuning photos/authors, held-out photo/author, candidate `T_pilea`, held-out route, correctness, confident-wrong, pothos-sentinel outcome) — an auditable, diffable artifact.
- [x] Write the verdict into `pilea-direct-card-eval-plan.md` (or a sibling `pilea-direct-card-decision.md`): the two distributions, the separation gap, the LOO fold table, the author partition, the **chosen `T_pilea`**, and a one-line **SHIP / FALL-BACK** decision against the +0-confident-wrong bar. If FALL-BACK, this doc *is* the blocker note. *(`pilea-direct-card-decision.md`: **SHIP, T_pilea = 0.98**, principal-confirmed.)*

### Phase A2 — Design the gate refinement (decision, in-plan)

- [x] **Chosen mechanism:** reuse `per_species_thresholds["pilea-peperomioides"] = T_pilea` as the elevated bar (the existing per-species plain-threshold mechanism), and refine the `boundary_pairs` early-return in `ModelScoreMapper` so top-1 = Pilea routes to the picker **unless** `bestProb ≥ T_pilea`, in which case control **falls through** to the existing high-confidence path (which emits the direct card). This composes the elevated bar *with* the boundary rule and adds **no new manifest key** (inspect the parser first; default = reuse `per_species_thresholds`). *(Confirmed parser already reads `per_species_thresholds`; no new key. Implemented in A3.)*
- [x] **Rejected — lower `T_pilea` ≤ 0.9661:** readmits the pothos→Pilea confident-wrong card. Hard floor, CI-bound (Phase A3). *(CI bind added: HousePlantClassMapValidationTest.)*
- [x] **Rejected — margin-over-second as the load-bearing separator:** both real-Pilea and pothos-misread have huge margins; a Pilea-specific margin is at most a belt-and-suspenders sanity guard, never the separator. Record explicitly. *(Recorded in eval-plan §A0 + decision doc.)*
- [x] State the binding bar in the design note: enabling the direct card must produce **+0 new confident-wrong** on clean fixtures, all perturbations, and the pothos/Pilea subset vs. the Phase-1 strict-picker baseline — under LOO + author separation. If unmet, ship strict-picker. *(Bar stated in eval-plan; met on the shipped tta6 pipeline.)*

### Phase A3 — Implement the gate refinement (tests first, dormant-safe)

- [x] Add `ModelScoreMapperBoundaryGateTest` cases:
  - (a) a pothos misread as Pilea @ **0.9661** (no second-place mass) routes to the **picker** *even when* `per_species_thresholds["pilea-peperomioides"] = T_pilea` is configured (proves `T_pilea > 0.9661` is enforced and the known failure can never reach a direct card). *(`pothosMisreadAsPileaAt09661RoutesToPickerEvenWithDirectThresholdConfigured`)*
  - (b) a real-Pilea-dominant row **above `T_pilea`** (e.g. 0.99) yields a **direct** `pilea-peperomioides` card (`lowConfidence == false`). *(`realPileaAboveTPileaYieldsADirectPileaCard`)*
  - (c) a Pilea-dominant row **between 0.9661 and `T_pilea`** still routes to the **picker** with Pilea + pothos visible (conservative middle band). *(`pileaInMiddleBandStillRoutesToPickerWithBothVisible`)*
  - (d) with `per_species_thresholds = {}` and a boundary pair present, behaviour is **identical to today's strict-picker** (the dormant-safe default — any boundary pair without a configured elevated threshold stays strict-picker). *(`emptyThresholdPathIsByteForByteTheStrictPicker`)*
  - (e) a non-Pilea species with a per-species threshold still overrides `highConfidencePlain` as before (regression); abstain behaviour off the boundary is byte-for-byte unchanged. *(`pileaDirectThresholdDoesNotPerturbNonBoundarySpeciesOrAbstain` — per-species *plain* override semantics covered by ModelScoreMapperPerSpeciesThresholdTest.)*
- [x] Implement the refinement in `ModelScoreMapper.map(...)`: in the `boundaryPair != null` branch, compute `directCardAllowed = perSpeciesThresholds[pair.top1KbSpeciesId]?.let { bestProb >= it } == true`; `return lowConfidence(...)` only when `!directCardAllowed`; otherwise fall through to the unchanged high-conf/abstain path. Keep the early-return as the **default** so the empty-threshold path is exactly today's behaviour.
- [x] Extend `ModelManifestTest` (and/or `PerSpeciesThresholdsContractTest`) with the **config-level CI bind**: if `per_species_thresholds` contains `pilea-peperomioides`, its value must be **> 0.9661** (the documented pothos ceiling) — the tripwire that makes "pothos→Pilea can never reach a direct card" enforceable below the on-device eval, where DEEPSEEK correctly noted the eval-level property itself can't be CI-bound. *(Added to `HousePlantClassMapValidationTest.pileaDirectCardThresholdMustExceedPothosCeiling` — the test that already reads the house-plant `model_manifest.json`; `ModelManifestTest` is AIY-scoped.)*
- [x] Keep `HousePlantClassMapValidationTest.pileaMappingRequiresBoundaryGate` active; update only if its assertion must also bind the new direct-exception config. *(Kept active, unchanged.)*
- [x] Run `.\gradlew.bat testDebugUnitTest` for `ModelScoreMapper*`, `ModelManifest*`, `PerSpeciesThresholds*`, `CandidateBundle*` green against synthetic rows **before** touching the device. *(BUILD SUCCESSFUL, all green.)*

### Phase A4 — Conditional ship decision (gate the manifest edit on the eval)

- [x] **If Phase A1 = SHIP:** set `per_species_thresholds["pilea-peperomioides"] = T_pilea` in `model_manifest.json` with a `_comment` citing the eval doc (LOO + author-separation evidence and the > 0.9661 invariant); the existing 0006-discipline `_comment_per_species_thresholds` stays and the new comment supplements it. Re-run `AccuracyEvalTest` on `pixel6Api34`; save `post-direct-card-eval.csv` + summary + a `direct-card-gating-decision.md` (mirroring the 0012 `boundary-gating-decision.md` structure). Confirm on device: **every** `epipremnum-aureum__*` fixture routes to picker or correct pothos (**never** a direct Pilea card), real-Pilea fixtures above `T_pilea` surface a direct Pilea card, Pilea fixtures **below** the bar still route to the picker with Pilea visible & first in `mapped_top3_kb_ids`, and confident-wrong is **+0** vs. baseline on all three surfaces (clean / all perturbations / pothos-Pilea subset). If any pothos fixture crosses or confident-wrong rises, **revert to strict-picker** (do not relax the bar to pass). *(SHIP confirmed by principal. Manifest set to `{"pilea-peperomioides": 0.98}` with citing `_comment`. On-device `post-direct-card-eval` runs in the Ship phase on the expanded set; `direct-card-gating-decision.md` written there.)*
- [ ] ~~**If Phase A1 = FALL-BACK:**~~ N/A — A1 = SHIP (shipped tta6 pipeline, +0.0877 gap, LOO +0 cw). Recorded for completeness: had it fallen back, `per_species_thresholds` would stay empty and the decision doc would be the blocker note.
- [x] Either way, confirm `HousePlantClassMapValidationTest` stays green — the mapping↔gate bind is untouched. *(Green; `pileaMappingRequiresBoundaryGate` + new `pileaDirectCardThresholdMustExceedPothosCeiling` both pass.)*

---

## Thread B — Grow the honest real-world sample count (CC0/PD only; parallel to Thread A)

- [ ] Create `fixture-sourcing-log.md` in the evidence dir capturing every search term, source URL, author, license + license URL, accept/reject decision, **rejected near-misses with reason codes**, and a running per-species count (start the log *before* sourcing).
- [ ] Confirm the 8 single-photo target species from `fixture-manifest.tsv` each still have exactly one `__01.jpg`; record the baseline.
- [ ] Source **additional CC0 / PD / PD-mark** real photographs spread **broadly** across those 8 species (Wikimedia Commons, GBIF/iNaturalist license-filtered CC0/PD, Smithsonian Gardens, Unsplash/Pexels CC0 — via the existing reference-photo sourcing approach / `scripts/source-reference-images.ps1` pattern). Prioritise **independent authors** and visually distinct houseplant contexts; aim for **breadth** (a 2nd/3rd photo across as many of the 8 as supply allows) over depth on one easy species. **Do NOT add any Pilea fixtures.**
- [ ] Accept only CC0/PD real photographs that are attributable; **reject + log** CC-BY/SA/NC/ND/all-rights-reserved/unknown, botanical plates/drawings, watermarked, duplicate, cultivar-mismatched, AI/illustration, and non-houseplant-context candidates.
- [ ] Add accepted JPEGs as `<species-id>__NN.jpg` (next sequential number); append one `fixture-manifest.tsv` row per file using the existing schema, with `notes` tagged `PLANTPOTTING-0013`. Update `LICENSE.txt` / `docs/licenses/` provenance as the schema requires.
- [ ] Run `FixtureLicenseManifestTest` after the **first** accepted batch; clear all license/schema-vocabulary failures before sourcing more. Then run `FixtureIntegrityTest` (decode + KB resolution + in-vocab reachability) and `AccuracyEvalCsvSchemaTest`; clear before proceeding.
- [ ] Re-run `AccuracyEvalTest` on `pixel6Api34` over the expanded fixture set (using whichever gate config Thread A lands — direct-card or strict-picker); save `expanded-scorecard.csv` + summary. Record the **refreshed honest headline number** (per-base-image-averaged clean top-1 + confident-wrong, clean vs perturbation separated) vs. the 0011/0012 number, and **report per-species fixture counts** so the headline isn't mistaken for deep per-species calibration.
- [ ] **Schema-drift guard:** if the refreshed/committed scorecard CSV changes shape, **update or extend `AccuracyEvalCsvSchemaTest`** so it still covers the committed scorecard **without dropping required columns** — keep all load-bearing columns and note which sprint's CSV is the committed scorecard. Do **not** weaken the schema to make it pass.
- [ ] Close with a **reason-coded coverage table** in the sourcing log: each of the 8 species with **starting count, added count, ending count**, and any **supply ceiling** reason (e.g. license-clean supply exhausted). "Sourced as many as possible" must be auditable.

---

## Doc cleanup (parallel; finalise the 0013 note after the A4 verdict)

- [x] In `docs/kb/ml-mapping-notes.md`, correct the **0009 section** (≈ line 270, "Pilea deferral (deliberate, CI-enforced)… `pileaIsNotMapped` enforces the deferral"): mark it **historical** (e.g. retitle "Pilea deferral (0009–0011, LIFTED in 0012)", change present-tense claims to past tense, cross-reference the 0012 section). Do **not** delete the paragraph — the history of *why* it was deferred is valuable.
- [x] In `docs/kb/ml-mapping-notes.md`, correct the **0010 section** (≈ line 331, "Pilea … stays **unmapped** … the pothos↔Pilea boundary fix is still deferred"): replace the absolute-present-tense "still deferred / still unmapped" claim with a historical marker pointing at the 0012 section. Leave the other deliberate-non-mapping notes (Rattlesnake Plant, Iron Cross begonia, seasonal bulbs) unchanged — still accurate.
- [x] In the existing **0012 section**, add a short PLANTPOTTING-0013 note recording the direct-card decision (SHIP with `T_pilea = …`, or FALL-BACK with the measured gap) and the Thread B fixture-count refresh. *(Added a new 0013 section: SHIP with `T_pilea = 0.98`, the safety rationale, and a Thread B pointer to the coverage table + expanded scorecard.)*

---

## Ship — version, gates, APK, release tag (last)

- [ ] Bump `app/build.gradle.kts` to `versionCode = 7`, `versionName = "0.7.0"`.
- [ ] Run `.\gradlew.bat verifyNoNetworking`.
- [ ] Run `pwsh scripts/check-stub-isolation.sh`.
- [ ] Run `.\gradlew.bat ktlintCheck` **before pushing** (CI-only gate; on Windows autocrlf churns ~76 files — stage only real changes; run `ktlintFormat` if needed).
- [ ] Run `.\gradlew.bat lintDebug` (abortOnError) and fix findings.
- [ ] Run `.\gradlew.bat testDebugUnitTest` (incl. `ModelScoreMapper*`, `HousePlantClassMapValidationTest`, `ModelManifestTest`, `PerSpeciesThresholds*`, `FixtureLicenseManifestTest`, `FixtureIntegrityTest`, `AccuracyEvalCsvSchemaTest`).
- [ ] Run the established local `pixel6Api34` GMD/androidTest command for `AccuracyEvalTest` + on-device regressions; confirm `OnDeviceModelRealInterpreterTest` still pins the AIY V1/3 regression anchor.
- [ ] **Update `docs/ROADMAP.md`** with the 0013 result (direct-Pilea-card decision SHIP/FALL-BACK + `T_pilea`, Thread B fixture-count delta + refreshed headline number, remaining limitations) — **before** the final gate run and tag so the released state is documented.
- [ ] Build a debug APK, copy it to `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\` as `plantpotting-v0.7.0-debug.apk`, and **verify via PowerShell from the user's terminal** that the file actually landed — record **filename, size, and timestamp** (the Windows sandbox overlay can silently swallow writes outside the project tree).
- [ ] **Push the `v0.7.0` git tag** (after gates pass) so `release.yml` publishes the public GitHub Release with the APK — a `versionName` bump alone never publishes; the workflow fires only on a pushed `v*` tag.
- [ ] **Confirm the GitHub Release workflow actually published** the APK (check the run + release asset), or record the exact CI failure.

---

## Sequencing / dependencies

- [ ] **Phase 1 (cold start + baseline) first** — the strict-picker baseline eval is the +0-confident-wrong reference for all of Thread A and seeds the threshold extraction.
- [ ] **Threads A and B are independent** (different files, different gates) and proceed in **parallel**; both feed the final scorecard. The doc cleanup is independent too (finalise its 0013 note after A4).
- [ ] **Thread A is strictly A0 → A1 → A2 → A3 → A4:** state the crux, then *measure* the separation (gap-sign abort + pre-committed grid + offline sweep + LOO/author-separation) before any production change, then design against the *measured* gap, then land the dormant-safe gate refinement + tests, then gate the manifest `per_species_thresholds` edit on the SHIP/FALL-BACK verdict. **No tree may set `T_pilea` ≤ 0.9661** at any point (config CI bind enforces it).
- [ ] **Thread B fixtures before the final scorecard:** add + verify the new fixtures, then run the expanded `AccuracyEvalTest` once on the winning gate config.
- [ ] **Ship last:** version bump, ROADMAP, static gates, unit tests, on-device eval, APK delivery, then tag push + release confirmation — only after all routing/fixture/eval gates are green (or have documented environment-only failures).

## Risks & mitigations

- [ ] **The two failure modes are near-indistinguishable by score shape** (real-Pilea ~0.98 vs pothos→Pilea 0.9661, both no second-place mass). → The only separator is the absolute top-1 value; require `T_pilea` strictly above the pothos ceiling, CI-bound > 0.9661, with a gap-sign abort and strict-picker fall-back if the gap is too thin.
- [ ] **n=1 pothos point.** Only the base `epipremnum-aureum` fixture mispredicts as Pilea, so `T_pilea` may be fit above a single point. → Treat the fail-safe as load-bearing and the bar as conservative; the eval runs **all** pothos fixtures × all perturbations, so a wider error is caught; any new pothos→Pilea fixture raises the ceiling.
- [ ] **Overfit on 6 Pilea photos.** → LOO + author/source separation + pre-committed grid; `T_pilea` must survive every held-out fold with +0 confident-wrong; do **not** deepen Pilea to flatter the gate; ship strict-picker if it can't clear.
- [ ] **A future/real pothos photo could hit Pilea above the fixture ceiling.** → Document the residual risk in the eval doc; the > 0.9661 bind + conservative middle band (0.9661 < score < `T_pilea` → picker) are the buffer; the fail-safe remains the picker.
- [ ] **TTA interaction.** `T_pilea` is checked against the TTA-averaged `bestProb`, which could move a pothos→Pilea score. → The eval runs all three modes (squash / center_crop / tta6); if any pushes a pothos score over the bar, the eval catches it. `tta` itself is unchanged.
- [ ] **Non-atomic edit opens a confident-wrong window.** → The gate-refinement code defaults to strict-picker when `per_species_thresholds` lacks Pilea, so the manifest edit (A4) is the *only* activation, gated on the eval + the > 0.9661 CI bind.
- [ ] **Broad fixture additions shift the headline score down.** → That is honest measurement, not a regression to hide; report fixture-count changes and clean-vs-perturbation metrics clearly.
- [ ] **License-clean supply is uneven across the 8 species.** → Source for breadth, log every reject with a reason code, report which species couldn't be deepened rather than padding one easy species or admitting CC-BY.
- [ ] **Schema-test drift.** Growing the fixture set / refreshing the committed scorecard could force an `AccuracyEvalCsvSchemaTest` update. → Keep all load-bearing columns; note which sprint's CSV is the committed scorecard; don't weaken the schema while refreshing evidence.
- [ ] **Windows sandbox overlay** may silently swallow the Dropbox APK copy. → Verify the APK landed via PowerShell from the user's terminal before claiming delivery.
- [ ] **Forgetting the release tag** (v0.6.0's public release was skipped for exactly this reason). → The tag push + release-published confirmation are explicit acceptance criteria, not side effects of the version bump.

## Acceptance criteria

- [ ] `PlantIdentifier`, `IdentificationResult`, `IdSource` remain untouched; the model and AIY anchor are untouched; `tta` stays 6.
- [ ] `ModelScoreMapper` permits a **direct** Pilea card only when top-1 = Pilea **and** `bestProb ≥ per_species_thresholds["pilea-peperomioides"]`; otherwise top-1 = Pilea routes to the `LowConfidencePicker` with Pilea + pothos visible. The boundary rule composes with — never replaces — the 0011 abstain margin; the margin-over-second is not used as the separator.
- [ ] A unit test proves the pothos→Pilea @ 0.9661 (no second-place mass) case routes to the picker **even with** the direct-card threshold configured; a **config test enforces `per_species_thresholds["pilea-peperomioides"] > 0.9661` whenever present** — so a pothos→Pilea input can never reach a direct Pilea card (CI-bound). The eval-level "0 pothos→direct-Pilea" property is documented as a manual review gate (it cannot be CI-bound on-device).
- [ ] The direct card is shipped **only if** the LOO + author-separated eval shows **+0 confident-wrong** vs. the strict-picker baseline on clean fixtures, all perturbations, **and** the pothos/Pilea subset; otherwise strict-picker is retained and the blocker note documents the measured gap and why. The dormant gate-refinement code + tests land either way, with the empty-threshold path proven identical to today's strict-picker.
- [ ] On the on-device eval, **0** pothos fixtures surface a direct Pilea card; pothos-dominant correct cards are preserved; real-Pilea fixtures above `T_pilea` (if shipped) surface a direct Pilea card, else route to the picker with Pilea visible & first.
- [ ] No new Pilea fixtures are added. The 8 single-photo mapped species are deepened with additional **CC0/PD** fixtures wherever license-clean supply allows; every added fixture is a real photo documented in `fixture-manifest.tsv` / `docs/licenses/`; a reason-coded coverage table records start/added/end counts + supply ceilings. `FixtureLicenseManifestTest`, `FixtureIntegrityTest`, `AccuracyEvalCsvSchemaTest` are green.
- [ ] The expanded-fixture `AccuracyEvalTest` scorecard is re-run; the refreshed honest headline number is recorded in the evidence dir + ROADMAP, with clean vs perturbation metrics and per-species counts separated.
- [ ] `HousePlantClassMapValidationTest` stays green (count **39**, `pileaMapsToPileaPeperomioides`, `pileaMappingRequiresBoundaryGate`).
- [ ] `docs/kb/ml-mapping-notes.md` no longer asserts in the present tense that Pilea is unmapped / the boundary fix is "still deferred" / `pileaIsNotMapped` passes; the superseded 0009/0010 paragraphs are marked historical and a 0013 note records the outcome.
- [ ] `verifyNoNetworking`, `check-stub-isolation.sh`, `ktlintCheck`, `lintDebug`, JVM unit tests, fixture/license/integrity/CSV-schema tests, and the local `pixel6Api34` on-device eval are green (or have documented environment-only failures).
- [ ] The shipped build is v0.7.0 / versionCode 7; the debug APK is **verified present** (filename + size + timestamp) in the Dropbox folder; **and** the `v0.7.0` tag is pushed and the public GitHub Release with the APK is **confirmed published**.

## Evidence dir manifest (`docs/sprints/evidence/PLANTPOTTING-0013/`)

- [ ] `README.md` — baseline fixture counts, constraints, planned artifacts.
- [ ] `baseline-strict-picker-eval.csv` + summary — the +0-confident-wrong reference.
- [ ] `pilea-direct-card-eval-plan.md` — pre-registered protocol + the two distributions, separation gap, LOO fold table, author partition, chosen `T_pilea` (or blocker rationale), SHIP/FALL-BACK decision.
- [ ] `pilea-direct-card-threshold-sweep.csv` — the offline-simulator fold/threshold table.
- [ ] `post-direct-card-eval.csv` + summary + `direct-card-gating-decision.md` — only if Thread A ships the direct card (else the blocker note above stands in).
- [ ] `fixture-sourcing-log.md` — Thread B search/accept/reject log + the 8-species reason-coded coverage table.
- [ ] `expanded-scorecard.csv` + summary — the refreshed honest headline number over the expanded fixture set.
