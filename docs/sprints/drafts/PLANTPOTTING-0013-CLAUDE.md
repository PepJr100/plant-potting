# PLANTPOTTING-0013 — Direct Pilea Card (conditional) + Honest Sample-Count Growth + Doc Cleanup (v0.7.0)

## Intent

Two independent threads plus a doc fix, **all behind the frozen `PlantIdentifier` seam** — no model swap,
no training, no networking, no self-shot / first-party imagery.

**Thread A (headline) — permit a *direct* Pilea care card, but only if it earns it.** PLANTPOTTING-0012
mapped `pilea-peperomioides` (class-map count 39) and shipped it **strict-picker**: a `boundary_pairs`
rule in `model_manifest.json` forces *any* top-1 = Pilea result to the `LowConfidencePicker` (Pilea +
pothos both surfaced), CI-bound so Pilea can never map without the gate. The 0012 at-home review found
this too conservative: a **real** Pilea photo was identified correctly at **98%** with **no pothos
competing** (runners-up English Ivy 1%, ZZ plant) — yet it still landed in the picker. The boundary
confusion is **asymmetric**: the dangerous error is *pothos input → Pilea label* @ **0.9661** (no
second-place mass), **not** *Pilea input → pothos*. So a real, confident Pilea is conservatively
demoted. This sprint refines the existing gate to allow a **direct** Pilea card when Pilea clears an
**elevated `per_species_thresholds` bar with a clear margin over second place**, *composed with* the
existing top-1=Pilea boundary rule so the asymmetric pothos→Pilea error **still** routes to the picker.
The direct card **ships only if** it clears a pre-committed **+0 confident-wrong** bar under the
strictest eval the thin fixture set allows — **leave-one-out (LOO)** held-out evaluation **and**
author/source separation between threshold-tuning and validation photos, on the **existing ~6 Pilea
fixtures** (deliberately **not** deepened — we do not source Pilea photos to flatter its own gate). If
the eval does not clear the bar, **fall back to strict-picker and document why**. CI must bind the
invariant: a pothos→Pilea input can **never** reach a direct Pilea card.

**Thread B — grow the honest real-world sample count (no model change).** 8 mapped species currently
have only **1 CC0 fixture each** (the 0012 "previously-untested mapped" additions: `saintpaulia-ionantha`,
`chamaedorea-elegans`, `beaucarnea-recurvata`, `alocasia`, `dracaena`, `begonia`, `ctenanthe`,
`schlumbergera-bridgesii`). The honest headline accuracy number is bounded by this thin clean set.
Source **additional** CC0/PD fixtures spread **broadly** across those 8, keep all fixture
license/integrity/schema gates green, and re-run the `AccuracyEvalTest` scorecard so the headline number
reflects the larger set. **Pilea is deliberately NOT deepened.**

**Doc cleanup.** Scrub / mark-as-historical the stale 0009-era Pilea-deferral prose in
`docs/kb/ml-mapping-notes.md` (≈ lines 270 and 331) that still asserts in the absolute present tense that
Pilea is unmapped / the boundary fix is "still deferred" / `pileaIsNotMapped` passes — all contradicted by
0012 reality (Pilea IS mapped behind the gate). `docs/ROADMAP.md` is already accurate.

**Ship signal:** v0.7.0 / versionCode 7; debug APK delivered to the Dropbox folder **and** the `v0.7.0`
git tag pushed so a public GitHub Release with the APK is published.

---

## Goals

- [ ] **Thread A:** refine the existing pothos↔Pilea `boundary_pairs` gate in `ModelScoreMapper` so a top-1 = Pilea result can yield a **direct** Pilea care card **only** when Pilea clears an elevated, Pilea-specific `per_species_thresholds` bar (well above the global 0.55) **and** a clear top1−top2 margin — composed so the asymmetric pothos→Pilea error (top-1 Pilea @ 0.9661, no second-place mass) **still** routes to the `LowConfidencePicker`.
- [ ] **Thread A — conditional ship:** the direct card ships **only if** a pre-committed bar (+0 confident-wrong on held-out vs. the strict-picker baseline) is cleared under **leave-one-out** evaluation **and** author/source separation, on the **existing 6 Pilea fixtures**; otherwise **fall back to strict-picker** and write a blocker note explaining why.
- [ ] **Thread A — CI invariant:** add/extend tests so a tree where a pothos→Pilea input (the 0.9661 no-second-mass case) reaches a direct Pilea card turns CI **red** — the direct-card threshold can never be set low enough to admit the known failure.
- [ ] **Thread B:** source **additional** CC0/PD fixtures spread broadly across the 8 single-photo mapped species (license-clean supply is the ceiling), keeping `FixtureLicenseManifestTest` / `FixtureIntegrityTest` / `AccuracyEvalCsvSchemaTest` green.
- [ ] **Thread B:** re-run the `AccuracyEvalTest` scorecard on the expanded fixture set and record the refreshed honest headline number.
- [ ] **Doc cleanup:** correct the superseded present-tense Pilea-deferral claims in `docs/kb/ml-mapping-notes.md` (keep the per-sprint-section convention; mark historical, don't delete the history).
- [ ] **Ship:** v0.7.0 / versionCode 7, all gates green, debug APK **verified present** in the Dropbox folder, **and** the `v0.7.0` tag pushed to publish the GitHub Release.

## Non-goals / scope boundaries

- [ ] Do **not** touch the frozen seam: `PlantIdentifier`, `IdentificationResult`, `IdSource` stay byte-for-byte unchanged. All new logic lives in `ModelScoreMapper`, `ModelManifest` parsing, manifest config, internal candidate construction, and tests.
- [ ] Do **not** swap, retrain, fine-tune, re-quantize, or otherwise alter `house_plant_species_mobilenetv2` or the AIY V1/3 regression anchor.
- [ ] Do **not** deepen the Pilea fixture set, swap the AIY-anchor `monstera-deliciosa.jpg`, or add any self-shot / first-party / generated imagery anywhere. Fixtures stay CC0/PD/PD-mark/CC-BY real photographs only (no plates/drawings, no SA/NC/ND/unknown).
- [ ] Do **not** re-litigate **TTA** — `tta` stays **6**. The boundary gate is deterministic and TTA-independent; the 0012 grid-tiling sweep already showed ×8/×10/×20/grid buy nothing within the ~2 s budget and 3×3 busts the cap. Only revisit if the fixture set changes *character* (it doesn't — Thread B adds the same kind of clean photos).
- [ ] Do **not** loosen the global plain/margin gates (0.55 / 0.45 / 0.18) or the 0011 `high_confidence_abstain_margin` (0.30) to flatter Pilea (the 0006 anti-overfit discipline). The elevated Pilea bar is an *additional* gate, never a relaxation.
- [ ] Do **not** loosen `verifyNoNetworking`, `ktlintCheck`, `check-stub-isolation.sh`, or the fixture license/integrity/CSV-schema guards.
- [ ] Do **not** bump AGP / Kotlin / Compose / Hilt / TFLite or any platform dependency.
- [ ] Do **not** add KB content (the `pilea-peperomioides` species entry already exists from 0012); no new species, archetypes, or reference images.

## Repo anchors (confirm each before editing)

- [ ] Gate logic: `app/src/main/java/com/darkfactory/plantpotting/identify/model/ModelScoreMapper.kt` — the boundary-pair check (lines ~89–103) currently fires **unconditionally** for top-1 = Pilea and `return`s `lowConfidence(...)` *before* the high-conf/abstain path. This early-return is the surface Thread A modifies.
- [ ] Manifest model + parser: `.../identify/model/ModelManifest.kt` — `BoundaryPair(top1KbSpeciesId, surfaceKbSpeciesIds)`, `perSpeciesThresholds: Map<String,Float>`, `Thresholds`, and `ModelManifestReader.parse(...)` (parses `boundary_pairs`, `per_species_thresholds`, `thresholds`).
- [ ] Config: `app/src/main/assets/ml/house_plant_species_mobilenetv2/model_manifest.json` — `per_species_thresholds: {}`, `thresholds` (plain 0.55 / margin_min 0.45 / delta 0.18 / `high_confidence_abstain_margin` 0.30 / top_k 3), `boundary_pairs` (top1 `pilea-peperomioides` → low_confidence, surfacing Pilea + `epipremnum-aureum`), `tta: 6`.
- [ ] DI wiring: `.../identify/OnDeviceIdentifyModule.kt` — `provideBoundaryPairs`, `providePerSpeciesThresholds`, `provideThresholds` already thread the manifest config into `ModelScoreMapper`. No new providers expected.
- [ ] Unit tests (JVM): `ModelScoreMapperBoundaryGateTest.kt`, `ModelScoreMapperPerSpeciesThresholdTest.kt`, `ModelScoreMapperAbstainMarginTest.kt`, `PerSpeciesThresholdsContractTest.kt`, `CandidateBundleContractTest.kt`, `ModelManifestTest.kt`.
- [ ] Map-validation gate (Robolectric): `.../identify/HousePlantClassMapValidationTest.kt` — `mapsExactlyThirtyNineClasses`, `pileaMapsToPileaPeperomioides`, `pileaMappingRequiresBoundaryGate` (bind-mapping-to-gate). These stay green; count stays **39**.
- [ ] Eval harness (instrumented, `pixel6Api34`): `app/src/androidTest/java/.../identify/AccuracyEvalTest.kt` — `scoreProductionModelOverFixturesAndPerturbationsEmitCsv` exercises the real `boundaryPairs` + `perSpeciesThresholds` and emits `accuracy-eval.csv` + summary; pull via `adb pull /sdcard/Android/data/com.darkfactory.plantpotting/files/...`.
- [ ] Fixtures: `app/src/androidTest/assets/identify-fixtures/` — flat `<kb-species-id>__NN.jpg`, with `LICENSE.txt` + `fixture-manifest.tsv` (columns: filename, expected_species_id, source_url, author, license, license_url, acquisition_date, notes). The `author` column is the basis for author/source separation. 6 Pilea fixtures already present: `pilea-peperomioides__01..06.jpg`.
- [ ] Version: `app/build.gradle.kts` (`versionCode = 6`, `versionName = "0.6.0"` → 7 / "0.7.0"). Doc-cleanup target: `docs/kb/ml-mapping-notes.md`. Evidence dir: `docs/sprints/evidence/PLANTPOTTING-0013/`.

---

## Thread A — Direct Pilea care card (conditional)

### Phase A0 — The central problem, stated honestly (read before designing)

- [ ] Internalise the crux: a **real Pilea** (~0.98–1.00 top-1, no second-place mass) and a **pothos misread as Pilea** (0.9661 top-1, no second-place mass) are **near-identical in score shape**. The top1−top2 *margin* cannot separate them (both are huge). The **only** separating lever the score vector offers is the **absolute Pilea top-1 value**: the worst pothos→Pilea sits at 0.9661; the 6 Pilea fixtures sit higher. So a direct card is only safe behind a threshold `T_pilea` set **strictly above the measured pothos→Pilea ceiling** with a safety margin.
- [ ] Accept up front that fitting `T_pilea` into the gap above 0.9661 on **6 photos** is overfit-prone. The LOO + author-separation protocol (Phase A1) is the guard, and the fail-safe (strict-picker) is **load-bearing** — this thread is genuinely conditional, not a foregone ship.

### Phase A1 — Measure the separation under the strictest eval the fixtures allow

- [ ] Create `docs/sprints/evidence/PLANTPOTTING-0013/` and a `pilea-direct-card-eval.md` stub for the protocol + numbers.
- [ ] Run the **current strict-picker** production app (gate on, `per_species_thresholds` empty) through `AccuracyEvalTest` on `pixel6Api34` over the current fixture set; save `baseline-strict-picker-eval.csv` + summary. This is the **+0 confident-wrong reference** Thread A must not regress against.
- [ ] From the eval CSV (squash, the shipped control; tta6 as cross-check), extract per fixture the **raw Pilea top-1 score** for: every `epipremnum-aureum__*` (pothos) fixture and every `pilea-peperomioides__*` (Pilea) fixture. Build two distributions: **pothos→Pilea confidence** (ceiling = the dangerous bound) and **real-Pilea confidence** (floor = what a direct card must clear).
- [ ] Record the **separation gap** = (min real-Pilea top-1) − (max pothos→Pilea top-1). If the gap is ≤ 0 (pothos can reach as high as the weakest real Pilea), **no safe `T_pilea` exists → fall back to strict-picker** (skip to Phase A4 blocker path). If the gap is positive, proceed.
- [ ] Define and run a **leave-one-out (LOO)** protocol over the 6 Pilea fixtures: for each held-out Pilea fixture, derive a candidate `T_pilea` from the *other 5* (e.g. tune so all 5 clear it with margin) and the pothos ceiling, then check the held-out Pilea still clears `T_pilea` **and** no pothos fixture clears it. A direct card "passes LOO" only if **every** held-out fold keeps **+0 confident-wrong** (no pothos crosses) — restoring the held-out real-Pilea direct card is the *benefit*, not the safety bar.
- [ ] Add **author/source separation** on top of LOO: using the `author` column in `fixture-manifest.tsv`, ensure the photos used to *derive* `T_pilea` and the photo used to *validate* it are not by the same author (drop or note any fold that cannot be author-separated). Document the author partition.
- [ ] Write the verdict into `pilea-direct-card-eval.md`: the two distributions, the separation gap, the LOO fold table, the author-separation partition, the **chosen `T_pilea`** (and Pilea-specific margin if used), and a one-line **SHIP / FALL-BACK** decision against the +0-confident-wrong bar. If FALL-BACK, this doc *is* the blocker note.

### Phase A2 — Design the gate refinement (decision + tradeoffs, in-plan)

- [ ] **Chosen mechanism:** reuse `per_species_thresholds["pilea-peperomioides"] = T_pilea` as the elevated bar (the existing per-species plain-threshold mechanism, honestly), and refine the `boundary_pairs` early-return in `ModelScoreMapper` so it routes top-1 = Pilea to the picker **unless** `bestProb ≥ T_pilea` (and the Pilea-specific margin holds), in which case control **falls through** to the existing high-confidence path — which then emits the direct card because `plainThreshold` for Pilea is also `T_pilea`. This composes the elevated bar *with* the boundary rule rather than replacing it, and adds **no new threshold field**.
- [ ] **Rejected — lower `T_pilea` to admit more Pileas:** anything ≤ 0.9661 readmits the pothos→Pilea confident-wrong card. Recorded as the hard floor.
- [ ] **Rejected — margin-over-second as the separator:** both real-Pilea and pothos-misread have huge margins (no second-place mass), so a margin rule alone cannot tell them apart. A Pilea-specific margin is at most a belt-and-suspenders sanity guard, never the load-bearing separator. Record explicitly.
- [ ] **Rejected — a separate `boundary_pairs` "direct_above" sub-field / new manifest block:** prefer reusing `per_species_thresholds` (already parsed, already DI-wired, already CI-covered) over inventing config; inspect the parser before committing to any new key. (Decide concretely while implementing Phase A3; default = no new manifest key.)
- [ ] State the binding bar in the design note: enabling the direct card must produce **+0 new confident-wrong** on clean fixtures, all perturbations, and the pothos/Pilea subset vs. the Phase A1 strict-picker baseline — under LOO + author separation. If unmet, ship strict-picker.

### Phase A3 — Implement the gate refinement (tests first)

- [ ] Add a `ModelScoreMapperBoundaryGateTest` case: a pothos misread as Pilea @ **0.9661** (no second-place mass) routes to the **picker** — *even when* `per_species_thresholds["pilea-peperomioides"] = T_pilea` is configured (proves `T_pilea > 0.9661` is enforced and the known failure can never reach a direct card).
- [ ] Add a case: a **real-Pilea-dominant row above `T_pilea`** (e.g. 0.99 with a clear margin) yields a **direct** `pilea-peperomioides` card (`lowConfidence == false`).
- [ ] Add a case: a Pilea-dominant row **between 0.9661 and `T_pilea`** (e.g. 0.97) still routes to the **picker** with Pilea + pothos visible (the conservative middle band).
- [ ] Keep/adjust the existing cases: `truePileaDominantRoutesToPickerWithPileaAndPothosVisible` (0.88, below `T_pilea`) still routes to picker; `pothosDominantStillReturnsADirectPothosCard`, `nonBoundarySpeciesAndAbstainBehaviourAreUnchangedByTheGate`, and `boundaryBundleSurfacesPothosEvenWhenOutsideTopK` stay green (the gate is still scoped to top-1 = Pilea; abstain behaviour byte-for-byte unchanged off the boundary).
- [ ] Implement the refinement in `ModelScoreMapper.map(...)`: in the `boundaryPair != null` branch, compute `directCardAllowed = perSpeciesThresholds[pair.top1KbSpeciesId]?.let { bestProb >= it } == true` (and the Pilea-specific margin, if adopted); `return lowConfidence(boundaryCandidates...)` only when `!directCardAllowed`; otherwise fall through to the unchanged high-conf/abstain path. Keep the early-return as the default so any boundary pair **without** a configured elevated threshold remains strict-picker (no behaviour change for a future pair).
- [ ] Extend `ModelManifestTest` (and/or `PerSpeciesThresholdsContractTest`) so that **if** `per_species_thresholds` contains `pilea-peperomioides`, its value is **> 0.9661** (the documented pothos→Pilea ceiling) — a config-level CI bind that the direct-card threshold can never be set low enough to admit the known failure.
- [ ] Run `.\gradlew.bat testDebugUnitTest` for `ModelScoreMapper*`, `ModelManifest*`, `PerSpeciesThresholds*`, `CandidateBundle*` green against synthetic rows before touching the device.

### Phase A4 — Conditional ship decision (gate the manifest edit on the eval)

- [ ] **If Phase A1 = SHIP:** set `per_species_thresholds["pilea-peperomioides"] = T_pilea` in `model_manifest.json` with a `_comment` citing `pilea-direct-card-eval.md` (the LOO + author-separation evidence and the > 0.9661 invariant). Re-run `AccuracyEvalTest` on `pixel6Api34`; save `post-direct-card-eval.csv` + summary. Confirm on-device: **every** `epipremnum-aureum__*` fixture routes to picker or correct pothos (never a direct Pilea card), real-Pilea fixtures above `T_pilea` surface a direct Pilea card, and confident-wrong is **+0** vs. the strict-picker baseline on all three surfaces. If any pothos fixture crosses or confident-wrong rises, **revert to strict-picker** (do not relax the bar to pass).
- [ ] **If Phase A1 = FALL-BACK:** leave `per_species_thresholds` empty (strict-picker unchanged); keep `pilea-direct-card-eval.md` as the blocker note recording the measured gap and why a safe `T_pilea` doesn't exist on this fixture set. The gate-refinement code + tests still land (dormant for Pilea until a future, deeper fixture set), guarded so the empty-threshold path is exactly today's strict-picker behaviour.
- [ ] Either way, confirm `HousePlantClassMapValidationTest` (`mapsExactlyThirtyNineClasses`, `pileaMapsToPileaPeperomioides`, `pileaMappingRequiresBoundaryGate`) stays green — the mapping↔gate bind is untouched.

---

## Thread B — Grow the honest real-world sample count (no model change)

- [ ] Create `docs/sprints/evidence/PLANTPOTTING-0013/fixture-sourcing-log.md` capturing every search term, source URL, author, license + license URL, accept/reject decision, **rejected near-misses with reason codes**, and a running per-species count.
- [ ] Confirm the 8 single-photo target species from `fixture-manifest.tsv` (`saintpaulia-ionantha`, `chamaedorea-elegans`, `beaucarnea-recurvata`, `alocasia`, `dracaena`, `begonia`, `ctenanthe`, `schlumbergera-bridgesii`) each still have exactly one `__01.jpg`; record the baseline.
- [ ] Source **additional** CC0/PD real photographs spread **broadly** across those 8 species (Unsplash / Pexels / Wikimedia / GBIF·iNaturalist CC0, via the existing reference-photos sourcing approach), prioritising **independent authors** and houseplant context. Aim for breadth (a 2nd/3rd photo across as many of the 8 as supply allows) rather than depth on one.
- [ ] Accept only CC0 / PD / PD-mark / CC-BY real photographs that permit commercial app-test use and are attributable; reject + log SA/NC/ND/all-rights-reserved/unknown, botanical plates/drawings, watermarked, duplicate, cultivar-mismatched, and non-houseplant-context candidates. **Do not** add any Pilea fixtures.
- [ ] Add accepted JPEGs as `<species-id>__NN.jpg` following the flat `__NN` convention; append one `fixture-manifest.tsv` row per file using the existing schema; add `docs/licenses/` attribution rows for any CC-BY fixture (keep CC0/PD rows explicit too).
- [ ] Run `FixtureLicenseManifestTest` after the first batch; clear all schema/license-vocabulary failures before adding more.
- [ ] Run `FixtureIntegrityTest` (separate step — decode + KB-resolution + in-vocab reachability) and clear before proceeding.
- [ ] Run `AccuracyEvalCsvSchemaTest` to confirm the manifest still parses against the eval schema.
- [ ] Re-run `AccuracyEvalTest` on `pixel6Api34` over the expanded fixture set; save `expanded-scorecard.csv` + summary under the evidence dir and record the **refreshed honest headline number** (per-base-image-averaged clean top-1 + confident-wrong) vs. the 0011/0012 number.
- [ ] Close the phase with a **reason-coded outcome table** in the sourcing log: each of the 8 species marked deepened (with new count) or not (with reason — e.g. license-clean supply exhausted). "Sourced as many as possible" must be auditable.

---

## Doc cleanup

- [ ] In `docs/kb/ml-mapping-notes.md`, correct the **0009 section** (≈ line 270, "Pilea deferral (deliberate, CI-enforced)… intentionally left unmapped… `pileaIsNotMapped` enforces the deferral"): mark it **historical** (e.g. "*(superseded by 0012 — Pilea is now mapped behind the pothos↔Pilea boundary gate; `pileaIsNotMapped` was replaced by `pileaMapsToPileaPeperomioides` + `pileaMappingRequiresBoundaryGate`.)*") rather than deleting the history. Keep the per-sprint-section convention.
- [ ] In `docs/kb/ml-mapping-notes.md`, correct the **0010 section** (≈ line 331, "Pilea … stays **unmapped** (CI-enforced by `pileaIsNotMapped`; the pothos↔Pilea boundary fix is still deferred)"): replace the absolute-present-tense "still deferred / still unmapped" claim with a historical marker pointing at the 0012 section. Do not alter the surrounding correct 0010 prose.
- [ ] In the existing **0012 section** of the same file, add a short PLANTPOTTING-0013 note recording the direct-card decision (SHIP with `T_pilea = …`, or FALL-BACK with the measured gap) and the Thread B fixture-count refresh. Leave `docs/ROADMAP.md` unchanged (already accurate) — but bump it in the ship phase if the `roadmap` skill convention calls for a per-sprint line.

---

## Ship — version, gates, APK, release tag

- [ ] Bump `app/build.gradle.kts` to `versionCode = 7`, `versionName = "0.7.0"`.
- [ ] Run `.\gradlew.bat verifyNoNetworking`.
- [ ] Run `pwsh scripts/check-stub-isolation.sh`.
- [ ] Run `.\gradlew.bat ktlintCheck` **before pushing** (CI-only gate; on Windows autocrlf churns ~76 files — stage only real changes; run `ktlintFormat` if needed).
- [ ] Run `.\gradlew.bat lintDebug` (abortOnError) and fix findings.
- [ ] Run `.\gradlew.bat testDebugUnitTest` (incl. `ModelScoreMapper*`, `HousePlantClassMapValidationTest`, `ModelManifestTest`, `PerSpeciesThresholds*`, `FixtureLicenseManifestTest`, `FixtureIntegrityTest`, `AccuracyEvalCsvSchemaTest`).
- [ ] Run the established local `pixel6Api34` GMD/androidTest command for `AccuracyEvalTest` + on-device regression tests; confirm `OnDeviceModelRealInterpreterTest` still pins the AIY V1/3 regression anchor.
- [ ] Build a debug APK, copy it to `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\` as `plantpotting-v0.7.0-debug.apk`, and **verify via PowerShell that the file actually landed** (sandbox-overlay writes outside the project tree can silently not reach disk).
- [ ] Update `docs/ROADMAP.md` with the 0013 result: direct-Pilea-card decision (SHIP/FALL-BACK + `T_pilea`), Thread B fixture-count delta + refreshed headline number, and remaining limitations.
- [ ] **Push the `v0.7.0` git tag** so `release.yml` publishes the public GitHub Release with the APK (a `versionName` bump alone never publishes — the workflow fires only on a pushed `v*` tag).

---

## Sequencing / dependencies

- [ ] **Threads A and B are independent** and can proceed in parallel; both feed the final scorecard.
- [ ] **Thread B fixtures before the final scorecard:** add + verify the new fixtures, then run the expanded `AccuracyEvalTest` once so the refreshed headline number reflects the full set.
- [ ] **A1 → A2 → A3 → A4:** measure the separation first; design against the *measured* gap; land the gate refinement + tests (dormant-safe); then gate the manifest `per_species_thresholds` edit on the SHIP/FALL-BACK verdict. **No tree may set `T_pilea` ≤ 0.9661** at any point (the config CI bind enforces it).
- [ ] **A1's strict-picker baseline eval** can share the same GMD run discipline as Thread B's scorecard, but the **post-direct-card** eval (A4) must run *after* the manifest edit.
- [ ] **Doc cleanup** can land any time; do the 0013 summary note after the A4 verdict is known.
- [ ] **Ship last:** version bump, gates, APK, ROADMAP, and tag only after all routing/fixture/eval gates are green.

## Risks & mitigations

- [ ] **The two failure modes are nearly indistinguishable by score shape** (real-Pilea ~0.98 vs pothos→Pilea 0.9661, both no second-place mass). → The only separator is the absolute top-1 value; require `T_pilea` strictly above the measured pothos ceiling, CI-bound > 0.9661, and accept fall-back if the gap is too thin.
- [ ] **Overfit on 6 Pilea photos.** → LOO + author/source separation; `T_pilea` must survive every held-out fold with +0 confident-wrong; do **not** deepen Pilea to flatter the gate; if it can't clear, ship strict-picker.
- [ ] **A future/real pothos photo could hit Pilea above the fixture ceiling** and surface a wrong Pilea card. → Document this residual risk explicitly in `pilea-direct-card-eval.md`; the `> 0.9661` margin + the conservative middle band (0.9661 < score < `T_pilea` → picker) are the buffer; the fail-safe remains the picker.
- [ ] **Non-atomic edit opens a confidently-wrong window.** → The gate-refinement code defaults to strict-picker when `per_species_thresholds` lacks Pilea, so the manifest edit (A4) is the *only* thing that activates the direct card, and it's gated on the eval + the > 0.9661 CI bind.
- [ ] **Thread B license-clean supply is the ceiling.** → Source for breadth, log every reject with a reason code, and report which of the 8 could not be deepened rather than padding with weak/ambiguous sources.
- [ ] **Incomplete CC-BY attribution / manifest drift.** → Manifest tests stay strict; add license rows before images land; run `FixtureLicenseManifestTest` / `FixtureIntegrityTest` after the first batch.
- [ ] **Windows sandbox filesystem overlay** may silently swallow the Dropbox APK copy. → Verify the APK landed via PowerShell from the user's terminal before claiming delivery.
- [ ] **Forgetting the release tag** (v0.6.0's public release was skipped for exactly this reason). → The tag push is an explicit acceptance criterion, not a side effect of the version bump.

## Acceptance criteria

- [ ] No production change alters `PlantIdentifier`, `IdentificationResult`, or `IdSource`; the model and AIY anchor are untouched; `tta` stays 6.
- [ ] `ModelScoreMapper` permits a **direct** Pilea card only when top-1 = Pilea **and** `bestProb ≥ per_species_thresholds["pilea-peperomioides"]` (and the Pilea-specific margin, if adopted); otherwise top-1 = Pilea routes to the `LowConfidencePicker` with Pilea + pothos visible. The boundary rule still composes with — never replaces — the 0011 abstain margin.
- [ ] A test proves the pothos→Pilea @ 0.9661 (no second-place mass) case routes to the picker **even with** the direct-card threshold configured; a config test enforces `per_species_thresholds["pilea-peperomioides"] > 0.9661` whenever present — so a pothos→Pilea input can never reach a direct Pilea card (CI-bound).
- [ ] The direct card is shipped **only** if the LOO + author-separated eval shows **+0 confident-wrong** vs. the strict-picker baseline on clean fixtures, all perturbations, **and** the pothos/Pilea subset; otherwise strict-picker is retained and `pilea-direct-card-eval.md` documents why.
- [ ] On the on-device eval, **0** pothos fixtures surface a direct Pilea card; pothos-dominant correct cards are preserved; real-Pilea fixtures above `T_pilea` (if shipped) surface a direct Pilea card, else route to the picker with Pilea visible & first.
- [ ] The 8 single-photo mapped species are deepened with additional CC0/PD fixtures wherever license-clean supply allows; every added fixture is a real photo documented in `fixture-manifest.tsv` / `docs/licenses/`; Pilea is **not** deepened. `FixtureLicenseManifestTest`, `FixtureIntegrityTest`, `AccuracyEvalCsvSchemaTest` are green.
- [ ] The expanded-fixture `AccuracyEvalTest` scorecard is re-run and the refreshed honest headline number is recorded in the evidence dir + ROADMAP.
- [ ] `HousePlantClassMapValidationTest` stays green (count **39**, `pileaMapsToPileaPeperomioides`, `pileaMappingRequiresBoundaryGate`).
- [ ] `docs/kb/ml-mapping-notes.md` no longer asserts in the present tense that Pilea is unmapped / the boundary fix is "still deferred" / `pileaIsNotMapped` passes; the superseded 0009/0010 paragraphs are marked historical and a 0013 note records the outcome.
- [ ] `verifyNoNetworking`, `check-stub-isolation.sh`, `ktlintCheck`, `lintDebug`, JVM unit tests, fixture/license/integrity/CSV-schema tests, and the local `pixel6Api34` on-device eval are green (or have documented environment-only failures).
- [ ] The shipped build is v0.7.0 / versionCode 7; the debug APK is **verified present** in the Dropbox folder; **and** the `v0.7.0` tag is pushed so the public GitHub Release with the APK is published.

## Evidence dir manifest (`docs/sprints/evidence/PLANTPOTTING-0013/`)

- [ ] `pilea-direct-card-eval.md` — the two confidence distributions, separation gap, LOO fold table, author-separation partition, chosen `T_pilea` (or the blocker rationale), and the SHIP/FALL-BACK decision.
- [ ] `baseline-strict-picker-eval.csv` + summary — the +0-confident-wrong reference.
- [ ] `post-direct-card-eval.csv` + summary — only if Thread A ships the direct card.
- [ ] `fixture-sourcing-log.md` — Thread B search/accept/reject log + the 8-species reason-coded outcome table.
- [ ] `expanded-scorecard.csv` + summary — the refreshed honest headline number over the expanded fixture set.
