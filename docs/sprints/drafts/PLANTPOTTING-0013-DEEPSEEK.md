# PLANTPOTTING-0013 — Direct Pilea Care Card + Fixture Deepening + Doc Cleanup (v0.7.0)

## Intent

Two threads plus a doc cleanup, all behind the frozen `PlantIdentifier` seam (no model swap, no training, no networking, no self-shot imagery).

**Thread A (HEADLINE):** Permit a DIRECT Pilea (Chinese Money Plant / Pilea peperomioides) care card when the production `house_plant_species_mobilenetv2` model is confident AND correct, while the asymmetric pothos→Pilea error (@0.9661 from the 0012 baseline) STILL routes to the `LowConfidencePicker`. The 0012 shipped strict-picker (the `ModelScoreMapper` boundary gate unconditionally forces top-1=Pilea → picker). The 0012 at-home review (`docs/sprints/feedback/PLANTPOTTING-0012/feedback.md`) found this too conservative: a real Pilea photo was identified @ 98% top-1 with NO pothos competing (runners-up: English Ivy 1%, ZZ plant) — yet it landed in the picker. The confusion is asymmetric: pothos→Pilea is the dangerous error, NOT Pilea→pothos. So a confident real Pilea is being conservatively demoted.

The gate composition is **boundary gate + elevated Pilea-specific `per_species_thresholds` escape**: if top-1=Pilea AND the score clears an elevated per-species bar (well above the global 0.55) AND a leave-one-out held-out eval clears a pre-committed +0-confident-wrong bar → direct card ships. If the eval does NOT clear → fall back to strict-picker and document why in `pilea-direct-card-blocker.md`. The card is genuinely conditional. CI must bind: a pothos fixture can NEVER reach a direct Pilea card.

**Thread B:** 8 mapped species currently have only 1 CC0 fixture each (the 0012 "previously-untested" set: `saintpaulia-ionantha`, `chamaedorea-elegans`, `beaucarnea-recurvata`, `alocasia`, `dracaena`, `begonia`, `ctenanthe`, `schlumbergera-bridgesii`). Source additional CC0/PD fixtures spread across those 8 species (license-clean supply is the ceiling — Unsplash/Pexels/Wikimedia/GBIF via the existing `scripts/source-reference-images.ps1` approach). Pilea is deliberately NOT deepened. Keep all existing fixture license/integrity gates green. Re-run `AccuracyEvalTest` so the headline honest number reflects the larger fixture set.

**Doc Cleanup:** Scrub the stale 0009-era Pilea-deferral prose in `docs/kb/ml-mapping-notes.md` (around lines ~270 and ~331) that still asserts in absolute present tense that Pilea is unmapped / the boundary fix is "still deferred" / `pileaIsNotMapped` passes. Correct the superseded present-tense claims to mark them as historical. ROADMAP.md is already accurate — no change needed.

**SHIP:** v0.7.0 / versionCode 7; build a debug APK → Dropbox; push the `v0.7.0` git tag → public GitHub Release (workflow at `.github/workflows/release.yml` fires on pushed `v*` tags).

## Non-goals / scope boundaries

- Do NOT touch `PlantIdentifier`, `IdentificationResult`, or `IdSource` (frozen 0003 §4.4 seam).
- Do NOT swap, retrain, fine-tune, or re-quantize the model.
- Do NOT add networking, self-shot, or first-party imagery.
- Do NOT swap the active model root from `house_plant_species_mobilenetv2`.
- Do NOT deepen the Pilea fixture set (the existing ~6 fixtures are the evaluation pool — no Pilea photos sourced just to flatter its own gate).
- Do NOT change the `tta` setting (stays 6 — the 0012 sweep confirmed the gate is deterministic and TTA-independent; grid tiles hurt).
- Do NOT re-litigate any 0007–0011 mapping that already shipped.
- Do NOT loosen `verifyNoNetworking`, `check-stub-isolation.sh`, `ktlintCheck`, fixture license/integrity/CSV-schema guards, or the 0011 abstention/margin policy.
- Do NOT bump AGP / Kotlin / Compose / Hilt / TFLite or any platform dependency.

## Repo anchors (read these before editing)

| Anchor | Path | What it is |
|---|---|---|
| ModelScoreMapper | `app/src/main/java/com/darkfactory/plantpotting/identify/model/ModelScoreMapper.kt` | Boundary gate + per-species threshold logic lives here |
| ModelManifest | `app/src/main/java/com/darkfactory/plantpotting/identify/model/ModelManifest.kt` | `Thresholds`, `BoundaryPair`, `per_species_thresholds` parser |
| Production manifest | `app/src/main/assets/ml/house_plant_species_mobilenetv2/model_manifest.json` | `boundary_pairs`, `per_species_thresholds: {}`, `thresholds` |
| Class map | `app/src/main/assets/ml/house_plant_species_mobilenetv2/plant_class_map.json` | 39 mapped classes including Pilea |
| Boundary gate tests | `app/src/test/java/com/darkfactory/plantpotting/identify/model/ModelScoreMapperBoundaryGateTest.kt` | Synthetic score-vector tests for the boundary gate |
| Class map validation | `app/src/test/java/com/darkfactory/plantpotting/identify/HousePlantClassMapValidationTest.kt` | Count 39, `pileaMapsToPileaPeperomioides`, `pileaMappingRequiresBoundaryGate` |
| Accuracy eval harness | `app/src/androidTest/java/com/darkfactory/plantpotting/identify/AccuracyEvalTest.kt` | GMD-only on-device eval, emits CSV + summary |
| Fixture dir | `app/src/androidTest/assets/identify-fixtures/` | 60 photos, `fixture-manifest.tsv`, `LICENSE.txt` |
| Fixture manifest | `app/src/androidTest/assets/identify-fixtures/fixture-manifest.tsv` | TSV: filename, expected_species_id, source_url, author, license, license_url |
| License guards | `FixtureLicenseManifestTest`, `FixtureIntegrityTest`, `AccuracyEvalCsvSchemaTest` | License/manifest/schema CI gates |
| ML mapping notes | `docs/kb/ml-mapping-notes.md` | ~430 lines, stale prose at ~lines 270 and ~331 |
| 0012 feedback | `docs/sprints/feedback/PLANTPOTTING-0012/feedback.md` | At-home review finding driving Thread A |
| 0012 evidence | `docs/sprints/evidence/PLANTPOTTING-0012/` | `baseline-pre-pilea-eval.csv`, `post-pilea-gated-eval.csv`, `boundary-gating-decision.md` |
| 0012 plan | `docs/sprints/PLANTPOTTING-0012.md` | Reference for gate design (Candidate B vs C), TTA sweep outcome |
| ROADMAP | `docs/ROADMAP.md` | Already accurate post-0012, no changes needed |
| Version | `app/build.gradle.kts` line 16–17 | Currently `versionCode = 6`, `versionName = "0.6.0"` |
| Release workflow | `.github/workflows/release.yml` | Fires on `v*` tag push, builds + publishes APK to GitHub Release |
| Image sourcing | `scripts/source-reference-images.ps1` | Existing approach for CC0/PD fixture sourcing |
| Build guard | `app/build.gradle.kts` `verifyNoNetworking` task | Networking library blocklist |
| Stub isolation | `scripts/check-stub-isolation.sh` | Checks no network stubs in releaseRuntimeClasspath |

---

## Task list

### PHASE 1 — Thread A: Pilea direct card (elevated per-species threshold + LOOCV eval)

- [ ] **A1. Pre-eval baseline capture.** Run `AccuracyEvalTest` on `pixel6Api34` GMD at the current strict-picker state. Pull `accuracy-eval.csv` + `accuracy-eval-summary.md` → save as `docs/sprints/evidence/PLANTPOTTING-0013/pre-direct-card-baseline.csv` + summary. This is the BEFORE — the gate is still strict-picker. Verify the 6 Pilea fixtures all route `low-conf` with Pilea visible as top candidate, and all pothos fixtures (including `epipremnum-aureum.jpg` which raw-predicts Pilea @ 0.9661) route `low-conf`. Record per-fixture raw top-1 Pilea scores for threshold tuning.

- [ ] **A2. LEAVE-ONE-OUT threshold tuning on the 6 Pilea fixtures.** Apply author/source separation: the 6 Pilea fixtures have distinct authors (dinomariobob, Tiago Lubiana, Olsza Borys, Daniel Atha, dmagdee, Curran Dwyer — see `fixture-manifest.tsv`). For each leave-one-out fold, tune the minimum Pilea per-species threshold on 5 fixtures, then check the held-out 1 fixture: does the held-out Pilea raw top-1 score clear the threshold? Record the minimum score across held-out folds. Set the candidate threshold conservatively above the worst held-out score (not at it) — a threshold that ±clears all 6 folds with margin. Write the tuning log to `docs/sprints/evidence/PLANTPOTTING-0013/pilea-threshold-tuning.csv`.

- [ ] **A3. Implement Pilea direct-card escape in ModelScoreMapper.** The composition is: boundary gate + per_species_thresholds escape. When a `boundaryPair` fires (top-1 resolves to `pilea-peperomioides`), check `perSpeciesThresholds["pilea-peperomioides"]`. If present AND `bestProb >= speciesThreshold`, DO NOT fire the boundary gate — fall through to the normal `highConfDirect`/`highConfMargin` verdict logic so Pilea can earn a direct card. If the threshold is NOT met (or absent), fire the boundary gate → picker (existing behavior). This is a single `if` change inside the existing `boundaryPair != null` block in `ModelScoreMapper.map()`. **The per_species_thresholds mechanism already exists** (added in 0005 §5.3, `perSpeciesThresholds` constructor param, `plainThreshold` lookup at line 69–74 of `ModelScoreMapper.kt`). No new data structures or seams needed.

- [ ] **A4. Set the Pilea `per_species_thresholds` in `model_manifest.json`.** Add `"pilea-peperomioides": <TUNED_VALUE>` to the `per_species_thresholds` object (currently `{}`). Add a `_comment_per_species_thresholds_0013` documenting the LOOCV provenance, the author-separation discipline, the held-out results, and the explicit link to the boundary gate composition. The existing `_comment_per_species_thresholds` (0006 discipline — "Empty by design until …") stays; the new comment supplements it.

- [ ] **A5. Full eval: post-direct-card gated run.** Run `AccuracyEvalTest` on `pixel6Api34` GMD with the elevated Pilea threshold + boundary gate composition. Pull the CSV + summary → save as `docs/sprints/evidence/PLANTPOTTING-0013/post-direct-card-gated-eval.csv` + summary. The binding bar is: **+0 confident-wrong** vs. A1 baseline on ALL surfaces (clean, all perturbations, pothos/Pilea subset), for ALL modes (squash, center_crop, tta6). Specifically check: 0 pothos fixture → direct Pilea card. All true-Pilea fixtures that clear the threshold → direct card (or route low-conf with Pilea visible if they don't clear). Write the comparison summary: `docs/sprints/evidence/PLANTPOTTING-0013/direct-card-gating-decision.md` (mirroring the 0012 `boundary-gating-decision.md` structure).

- [ ] **A6. Conditional SHIP / FALLBACK decision.** If A5 eval clears +0 confident-wrong → proceed to A7. If ANY pothos fixture reaches a direct Pilea card (or confident-wrong rises on any surface) → FALL BACK to strict-picker: revert the `per_species_thresholds` entry to `{}`, revert the ModelScoreMapper escape, and write `docs/sprints/evidence/PLANTPOTTING-0013/pilea-direct-card-blocker.md` documenting exactly which fixture(s) broke the bar and why the direct card is unsafe. The Pilea mapping and boundary gate remain unchanged — only the direct-card escape is reverted. **The fallback IS a valid sprint outcome.**

- [ ] **A7. Update ModelScoreMapperBoundaryGateTest.** Add test cases for the per-species-threshold escape:
  - (a) `confidentTop1PileaClearsPerSpeciesThresholdReturnsDirectCard`: scores [0.98, 0.01, …], per_species_thresholds contains `pilea-peperomioides: 0.90` → result is high-conf, speciesId = pilea-peperomioides.
  - (b) `confidentTop1PileaBelowPerSpeciesThresholdStillRoutesToPicker`: scores [0.85, 0.06, …], threshold 0.90 → result is low-conf (boundary gate still fires).
  - (c) `pothosInputStillRoutesToPickerEvenWithPerSpeciesThreshold`: the live 0.9661 case — scores [0.9661, 0.01, 0.01, 0.005, 0.005], threshold 0.90 (or even lower) — if this fixture raw-predicts Pilea at a score above the threshold, the test must document the expected behavior. If below threshold → picker. If above → this IS the confident-wrong window the eval must catch; the unit test can encode the pre-commit constraint.
  - (d) `perSpeciesThresholdAbsentBoundaryGateUnchanged`: with `perSpeciesThresholds = {}` and a boundary pair present, behavior is identical to the existing 0012 tests (strict-picker).
  - (e) `nonBoundaryPerSpeciesThresholdStillWorks`: a non-Pilea species with a per-species threshold still overrides `highConfidencePlain` as before (regression).

- [ ] **A8. CI binding: verify pothos→Pilea never reaches direct card.** Add a test (or extend `pileaMappingRequiresBoundaryGate` / a new `HousePlantClassMapValidationTest` method) that asserts: IF Pilea is mapped AND a per_species_thresholds entry for Pilea exists, THEN the gated eval (A5) must have 0 pothos→direct-Pilea-card rows. Since the eval is on-device only, this is a documentation gate: the `direct-card-gating-decision.md` must state the pass condition explicitly, and the sprint review gates on it. (If this is unenforceable in CI, document it as a manual review gate in the acceptance criteria.)

- [ ] **A9. Evidence dir deliverables.** Ensure `docs/sprints/evidence/PLANTPOTTING-0013/` contains:
  - `pre-direct-card-baseline.csv` + summary (A1)
  - `pilea-threshold-tuning.csv` (A2)
  - `post-direct-card-gated-eval.csv` + summary (A5)
  - `direct-card-gating-decision.md` (A5)
  - `pilea-direct-card-blocker.md` ONLY if the fallback fires (A6)
  - If shipped: a short `pilea-direct-card-shipped.md` documenting the tuned threshold, LOOCV results, and the per-fixture raw scores.

### PHASE 2 — Thread B: Deepen single-photo species fixtures

- [ ] **B1. Inventory the 8 target species.** Confirm the exact list from `fixture-manifest.tsv`: `saintpaulia-ionantha`, `chamaedorea-elegans`, `beaucarnea-recurvata`, `alocasia`, `dracaena`, `begonia`, `ctenanthe`, `schlumbergera-bridgesii` — each has exactly 1 CC0 fixture (`__01.jpg`). These are the "previously-untested" species that got their first fixture in 0012 Phase 1. Verify no other mapped in-vocab species with fixtures has only 1 photo (some species may have 1 grandfathered CC-BY-SA photo but that's a different class; focus on CC0 only for this pass).

- [ ] **B2. Source additional CC0/PD fixtures.** Use the existing sourcing pipeline (`scripts/source-reference-images.ps1` pattern, iNaturalist CC0 via `stage_inat.py`, Unsplash, Pexels, Wikimedia PD/CC0, GBIF). Spread effort across all 8 species — aim for at least 1 additional fixture per species, bounded by the license-clean ceiling. Do NOT source Pilea photos. Each sourced photo must be:
  - A real photograph (not illustration/AI)
  - CC0 or Public Domain (PD/PD-mark)
  - From a verifiable source URL with identifiable author
  - Added as `<kb-species-id>__<NN>.jpg` in `identify-fixtures/` (next sequential number)
  - Recorded as a new row in `fixture-manifest.tsv` with author, source_url, license, license_url, acquisition_date, and notes tagged `PLANTPOTTING-0013`.

- [ ] **B3. Verify all fixture license/integrity gates stay green.** Run `FixtureLicenseManifestTest` (license cross-check — rejects non-CC0/PD new fixtures), `FixtureIntegrityTest` (decode + KB resolution + in-vocab reachability), `AccuracyEvalCsvSchemaTest`. Any failure → remove/replace the offending fixture before proceeding.

- [ ] **B4. Re-run AccuracyEvalTest with deepened fixture set.** Run on `pixel6Api34` GMD with the production (gated) pipeline. Pull CSV + summary → save as `docs/sprints/evidence/PLANTPOTTING-0013/post-fixture-deepening-eval.csv` + summary. The headline honest number from this run reflects the larger fixture set. Compare top-1 / top-3 / confident-wrong / abstain vs. the A1 baseline on the same rows (the 8 deepened species + overall).

- [ ] **B5. Update fixture-sourcing log.** Append to `docs/sprints/evidence/PLANTPOTTING-0013/fixture-sourcing-log.md` (or a standalone file) documenting each sourced photo, why it was chosen, the license verification, and any species that could not be deepened (with reason codes: no-license-clean-source, poor-quality-only, etc.).

### PHASE 3 — Doc cleanup

- [ ] **C1. Scrub stale 0009-era Pilea-deferral prose in `docs/kb/ml-mapping-notes.md`.** Two specific passages:
  - **Lines ~270–278** (the "Pilea deferral (deliberate, CI-enforced)" paragraph): Currently asserts in present tense that Pilea is "intentionally left unmapped", `pileaIsNotMapped` "enforces the deferral". Replace with: mark the paragraph as historical (retitle to "Pilea deferral (0009–0011, LIFTED in 0012)"), change present-tense claims to past tense, and cross-reference the 0012 section below that documents the lift. Do NOT delete the paragraph — the historical record of WHY it was deferred is valuable.
  - **Lines ~331–335** (the "Deliberate non-mappings preserved" paragraph): Currently says Pilea "stays **unmapped** (CI-enforced by `pileaIsNotMapped`; the pothos↔Pilea boundary fix is still deferred)". Replace with: mark as historical, note the deferral was lifted in 0012, reference the 0012 section. Keep the other deliberate non-mapping notes (`Rattlesnake Plant`, `Iron Cross begonia`, seasonal bulbs) unchanged — those are still accurate.

- [ ] **C2. Verify ROADMAP.md needs no changes.** The post-0012 ROADMAP section at the top is accurate. Read it to confirm. If Thread A ships a direct Pilea card, add a brief clause to the ROADMAP current-state blurb noting the direct card is permitted under the elevated threshold. If Thread A falls back, note that Pilea remains strict-picker (no functional change from 0012, so ROADMAP may not need updating beyond acknowledging 0013 happened).

### PHASE 4 — Ship

- [ ] **S1. Bump version to v0.7.0.** In `app/build.gradle.kts`: `versionCode = 7`, `versionName = "0.7.0"`.

- [ ] **S2. Gate check — all static gates green.** Run `./gradlew verifyNoNetworking`, `./gradlew ktlintCheck`, `./scripts/check-stub-isolation.sh`, `./gradlew lintDebug`, JVM unit tests (`./gradlew testDebugUnitTest`). All must be green before building the APK.

- [ ] **S3. Build debug APK.** `./gradlew assembleDebug`. Verify the APK exists at `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **S4. Deliver APK to Dropbox.** Copy to `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\plantpotting-v0.7.0-debug.apk`. Verify the file is present at that path via PowerShell `Test-Path`.

- [ ] **S5. Push the `v0.7.0` git tag.** `git tag v0.7.0` and `git push origin v0.7.0`. The `.github/workflows/release.yml` workflow fires on this tag push — it builds the APK on CI, runs `verifyNoNetworking` + unit tests, and publishes the APK as a GitHub Release asset. **The tag push IS the publish trigger; a versionName bump alone never publishes.**

- [ ] **S6. Run the sprint review gates.** All acceptance criteria below must pass. Write the review feedback to `docs/sprints/feedback/PLANTPOTTING-0013/feedback.md`.

---

## Sequencing

```
Phase 1 (A1–A6) — must be sequential: baseline → tune → implement → eval → decide → ship/fallback
    │
    ├── Phase 2 (B1–B5) — independent of Phase 1, can run in parallel
    │
    └── Phase 3 (C1–C2) — independent of both, can run anytime
         │
         └── Phase 4 (S1–S6) — gates on Phases 1–3 completion
```

**Critical path:** A1 → A2 → A3 → A4 → A5 → A6. If A6 falls back, A7–A9 still execute (tests + evidence for the fallback). B1–B5 and C1–C2 are parallelizable.

**Key dependency:** A4 (`per_species_thresholds` in manifest) must land AFTER A3 (code change). If A6 falls back, A4 must be reverted. The B4 re-run (`AccuracyEvalTest` with deepened fixtures) should use whichever gate configuration wins (direct-card or fallback strict-picker).

---

## Risks & mitigations

- **Pothos→Pilea score clears the per-species threshold.** The 0.9661 pothos→Pilea error from `epipremnum-aureum.jpg` is a single fixture data point. Other pothos fixtures may produce different Pilea scores. If ANY pothos fixture clears the tuned threshold, the eval catches it → fallback fires. **Mitigation:** the LOOCV threshold is tuned to be above the worst held-out Pilea score; the eval is the binding gate — if it fails, strict-picker stays. No threshold flattering.

- **Per-species threshold overfits 6 Pilea fixtures.** A threshold tuned on 5 held-in folds may not generalize. **Mitigation:** LOOCV is the strictest evaluation the thin fixture set allows; author/source separation prevents leakage within a fold; the +0-confident-wrong bar must hold on ALL surfaces (including all 7 pothos fixtures × 11 perturbations each = 77 pothos rows). The "don't deepen Pilea" constraint is load-bearing — no Pilea photos are sourced just to flatter the gate.

- **Pothos→Pilea score distribution unknown.** The 0.9661 is from ONE pothos fixture. Other pothos fixtures raw-predict different top-1 labels (see post-pilea-gated-summary: epipremnum-aureum__01 → pothos, __02 → pothos, __03 → alocasia, __04 → calathea, __05 → monstera, __06 → alocasia). Only the original `epipremnum-aureum.jpg` mispredicts as Pilea. So the pothos→Pilea error may be a single-fixture phenomenon. **Mitigation:** the eval runs ALL pothos fixtures × ALL perturbations — if the error is wider than one fixture, the eval catches it.

- **License-clean supply for the 8 species is exhausted.** iNaturalist CC0 may not have additional usable photos for some of the 8 species. **Mitigation:** the plan is "bounded by the license-clean ceiling" — if a species can't be deepened, document it in the sourcing log with a reason code. The headline number improves from whatever we CAN source.

- **Boundary gate escape introduces a confident-wrong window for pothos.** If the per-species threshold is set too low, a pothos photo that raw-predicts Pilea above the threshold would bypass the boundary gate and surface a wrong direct Pilea card. **Mitigation:** the eval (A5) is the binding gate — +0 confident-wrong on pothos/Pilea subset means this window does NOT exist. If it exists, the fallback (A6) closes it by reverting to strict-picker.

- **TTA interaction.** The boundary gate is TTA-independent (top-1=Pilea → gate fires, regardless of TTA averaging). The per-species threshold is checked against the TTA-averaged `bestProb`. TTA6 averaging could push a pothos→Pilea score up or down. **Mitigation:** eval runs ALL three modes (squash, center_crop, tta6) — if TTA6 pushes a pothos→Pilea score above the threshold, the eval catches it.

---

## Acceptance criteria

- [ ] `PlantIdentifier`, `IdentificationResult`, `IdSource` untouched in the final diff.
- [ ] Thread A: IF direct card ships — `per_species_thresholds` in `model_manifest.json` contains a non-empty `pilea-peperomioides` entry, the LOOCV tuning log is in evidence, the +0-confident-wrong bar is met on all surfaces/all modes, 0 pothos→direct-Pilea-card violations in the A5 eval.
- [ ] Thread A: IF direct card falls back — `per_species_thresholds` stays `{}`, `ModelScoreMapper` boundary gate escape is reverted or gated behind an absent threshold, `pilea-direct-card-blocker.md` documents the blocker, strict-picker behavior is byte-for-byte unchanged from 0012.
- [ ] `ModelScoreMapperBoundaryGateTest` covers the per-species threshold escape (A7) with at least 5 test cases, or documents the fallback.
- [ ] `HousePlantClassMapValidationTest` still passes: count 39, `pileaMapsToPileaPeperomioides`, `pileaMappingRequiresBoundaryGate`.
- [ ] Thread B: Every added fixture is CC0/PD, documented in `fixture-manifest.tsv`, source URL + author verifiable. `FixtureLicenseManifestTest`, `FixtureIntegrityTest`, `AccuracyEvalCsvSchemaTest` green. At least 1 additional fixture per species where license-clean supply permits; species with no additional source logged with reason code.
- [ ] `AccuracyEvalTest` re-run with the deepened fixture set; post-deepening CSV + summary in evidence.
- [ ] Doc cleanup: `docs/kb/ml-mapping-notes.md` no longer asserts in present tense that Pilea is unmapped / the boundary fix is deferred. The 0009-era passages are marked historical with cross-references to the 0012 section.
- [ ] ROADMAP.md verified accurate (updated with 0013 outcome if substantive, or acknowledged as no-change-needed).
- [ ] All static gates green: `verifyNoNetworking`, `ktlintCheck`, `check-stub-isolation.sh`, `lintDebug`, JVM unit tests.
- [ ] v0.7.0 / versionCode 7; debug APK verified present at `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\plantpotting-v0.7.0-debug.apk`.
- [ ] `v0.7.0` git tag pushed; GitHub Release workflow triggered.
- [ ] Evidence dir `docs/sprints/evidence/PLANTPOTTING-0013/` contains all required artifacts (baseline, tuning log, gated eval + summary, decision doc, fixture-sourcing log, and either `pilea-direct-card-shipped.md` or `pilea-direct-card-blocker.md`).
- [ ] Review feedback written to `docs/sprints/feedback/PLANTPOTTING-0013/feedback.md`.
