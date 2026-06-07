# PLANTPOTTING-0011 Feedback

**Review date:** 2026-06-07 · **Verdict:** Clean review — no bugs, no UX issues. All acceptance
criteria pass. Sprint closed `done`.

## Bugs

None.

## UX Issues

None.

## Review walkthrough results

**Automated gates (all GREEN):**
- `./gradlew :app:testDebugUnitTest` — green (includes new `ModelScoreMapperAbstainMarginTest`,
  `ModelManifestTest`, `FixtureLicenseManifestTest`, `FixtureIntegrityTest`, `AccuracyEvalCsvSchemaTest`,
  `HousePlantClassMapValidationTest` Pilea-absence).
- `verifyNoNetworking` — green.
- `bash scripts/check-stub-isolation.sh` — `stub isolation OK`.
- `:app:compileDebugAndroidTestKotlin` — clean.
- `:app:lintDebug` (abortOnError) — green.

**Seam / artifact audit (all confirmed):**
- `PlantIdentifier` / `IdentificationResult` / `IdSource` byte-for-byte unchanged since the plan commit.
- AIY baseline anchor (`OnDeviceModelRealInterpreterTest`) untouched.
- Pilea deliberately unmapped (documented `_comment` in `plant_class_map.json`); guard green.
- `versionCode 5` / `versionName 0.5.0`.
- Production `model_manifest.json`: `preprocess_mode: squash`, `tta: 6`, `high_confidence_abstain_margin: 0.30`.
- Evidence artifacts committed under `docs/sprints/evidence/PLANTPOTTING-0011/`.
- APK `app-debug-PLANTPOTTING-0011-v0.5.0.apk` (43.4 MB) confirmed in the Dropbox APK folder (PowerShell).

**Headline result accepted by principal:**
- Confident-wrong **0.382 → 0.179 (−53%)**; on clean photos **0.268 → 0.098**. Top-1 held ~flat.
- TTA-6 does most of the work (0.382 → 0.226); the 0.30 abstention margin trims the rest (0.226 → 0.179).
- Accepted cost: pick-manually 0.089 → 0.323; latency ~125 ms median (539 ms worst) on a one-shot identify.
- Held-out validated (tune-on-clean / eval-on-perturbation cw 0.186; leave-one-species-out 0.167–0.188).

**On-device sanity (Phase 5, line 279 — principal tested v0.5.0):** PASS. Previously confident-wrong
captures now route to the picker instead of a confident-wrong card; confidence bar reads thicker;
swapped reference photos render.

**Reference images:** confirmed all botanical plates are now real photographs. The 0011 fold-in
(CC0/PD-strict) swapped only pink-princess; the follow-on reference-photos pass (PR #32) swapped the
remaining plates (peace lily, parlor palm, dracaena, poinsettia, etc.) under CC BY / Unsplash / Pexels
with in-app attribution. Documented in `docs/licenses/reference-images.md §PLANTPOTTING-0011`.

**Docs honesty:** `ml-mapping-notes.md §PLANTPOTTING-0011` correctly frames the win as
measured-under-clean-conditions + synthetic-robustness — explicitly NOT "real-world accuracy solved."
Approved.

## Missing Features

None blocking. See forward note below.

## Notes for Next Sprint

- **Expand the eval set, then sweep higher TTA counts (principal request).** The CC0/PD fixture set
  (41 photos / 30 species, 8 mapped species still untested) under-represents messy phone captures —
  acknowledged limitation, not a regression. Once the real fixture set is larger, it would be
  interesting to measure what **TTA ×8 / ×10 / ×20** does to confident-wrong / confidence, sweeping
  up to a **~2 s worst-case latency budget** (this sprint capped TTA at 6 ≈ 125 ms median / 539 ms
  worst). Pair the eval-set expansion with this latency-vs-accuracy curve before raising `tta`.
- The pothos↔Pilea boundary fix + Pilea KB entry remain **deferred** (unchanged from prior sprints).
- Real-world accuracy on messy captures is still the open frontier; the harness is now in place to
  measure any future lever honestly.
