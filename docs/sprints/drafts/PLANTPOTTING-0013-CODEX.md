# PLANTPOTTING-0013 - Direct Pilea Card, Fixture Depth, And Mapping Docs

PLANTPOTTING-0013 is a combined product and accuracy sprint. It keeps the production
`house_plant_species_mobilenetv2` model, TTA-6 pipeline, and frozen `PlantIdentifier` /
`IdentificationResult` / `IdSource` seam unchanged. The headline work is a conditional relaxation of
the PLANTPOTTING-0012 strict Pilea picker: a real, confident Pilea should be allowed to reach a direct
care card only if an elevated Pilea-specific evidence bar clears held-out evaluation, while the known
pothos->Pilea failure can never reach a direct Pilea card.

## Goals

- [ ] Permit a direct `pilea-peperomioides` care card only when Pilea is top-1, clears an elevated Pilea-specific threshold, clears a measured top1-top2 margin, and still satisfies the existing pothos/Pilea boundary safety invariant.
- [ ] Pre-commit the Pilea direct-card threshold and margin using only threshold-tuning photos, then validate with leave-one-out held-out evaluation and author/source-photo separation on the existing 6 Pilea fixtures.
- [ ] Ship the direct Pilea card only if the strictest available held-out eval shows +0 confident-wrong; otherwise keep the PLANTPOTTING-0012 strict-picker behavior and document why.
- [ ] Add broader license-clean CC0/PD fixture depth across the 8 species that PLANTPOTTING-0012 added as single-photo coverage, without adding new Pilea fixtures.
- [ ] Keep `FixtureLicenseManifestTest`, `FixtureIntegrityTest`, `AccuracyEvalCsvSchemaTest`, `AccuracyEvalTest`, `ModelScoreMapperBoundaryGateTest`, and `HousePlantClassMapValidationTest` green.
- [ ] Scrub or mark-as-historical stale 0009/0010 present-tense Pilea deferral prose in `docs/kb/ml-mapping-notes.md`.
- [ ] Ship v0.7.0 / versionCode 7, copy a debug APK to `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\`, and push the `v0.7.0` git tag so the public GitHub Release workflow publishes.

## Scope Boundaries

- [ ] Do not change `PlantIdentifier`, `IdentificationResult`, or `IdSource`.
- [ ] Do not swap, retrain, fine-tune, re-quantize, or otherwise alter the bundled model.
- [ ] Do not add networking, runtime fetches, generated imagery, self-shot imagery, first-party imagery, or principal-supplied photos.
- [ ] Do not add new Pilea fixtures this sprint; use the existing `pilea-peperomioides__01.jpg` through `pilea-peperomioides__06.jpg` only.
- [ ] Do not re-litigate TTA; leave `tta = 6` unless the non-Pilea fixture set changes in a way that exposes a new, documented issue.
- [ ] Do not loosen `verifyNoNetworking`, `ktlintCheck`, `scripts/check-stub-isolation.sh`, fixture license/integrity gates, or CSV schema gates.
- [ ] Do not change `docs/ROADMAP.md` unless execution uncovers a new roadmap correction; it is already accurate for the known Pilea status.

## Phase 1 - Baseline And Inventory

- [ ] Read `docs/sprints/PLANTPOTTING-0012.md`, `docs/sprints/feedback/PLANTPOTTING-0012/feedback.md`, `docs/sprints/evidence/PLANTPOTTING-0012/boundary-gating-decision.md`, and `docs/sprints/evidence/PLANTPOTTING-0012/post-pilea-gated-summary.md`.
- [ ] Confirm `ModelScoreMapper` currently forces any raw top-1 that resolves to `pilea-peperomioides` through `boundary_pairs` to `LowConfidencePicker`.
- [ ] Confirm `HousePlantClassMapValidationTest` still asserts `mapsExactlyThirtyNineClasses`, `pileaMapsToPileaPeperomioides`, and `pileaMappingRequiresBoundaryGate`.
- [ ] Confirm production manifest values in `app/src/main/assets/ml/house_plant_species_mobilenetv2/model_manifest.json`: global plain 0.55, abstain margin 0.30, `per_species_thresholds = {}`, `tta = 6`, and the Pilea boundary pair.
- [ ] Inventory `fixture-manifest.tsv` counts and record the 8 single-photo species targeted for broadening: `saintpaulia-ionantha`, `chamaedorea-elegans`, `beaucarnea-recurvata`, `alocasia`, `dracaena`, `begonia`, `ctenanthe`, and `schlumbergera-bridgesii`.
- [ ] Record any additional mapped species with only one fixture as secondary candidates, but keep the sprint headline target on the 8 PLANTPOTTING-0012 single-photo additions unless supply is blocked.
- [ ] Create `docs/sprints/evidence/PLANTPOTTING-0013/README.md` with baseline fixture counts, the Pilea no-new-fixtures constraint, and the planned evidence files.

## Phase 2 - Pilea Direct-Card Evaluation Design

- [ ] Create `docs/sprints/evidence/PLANTPOTTING-0013/pilea-direct-card-eval-plan.md` before changing production logic.
- [ ] Define the direct-card candidate rule in the eval plan: top-1 label resolves to `pilea-peperomioides`, score clears a Pilea-specific `per_species_thresholds["pilea-peperomioides"]` value well above 0.55, and top1-top2 margin clears a Pilea-specific margin bar.
- [ ] Pre-commit the candidate threshold grid before validation, including at least one intentionally conservative bar near the observed real-world 0.98 signal and above the known pothos->Pilea 0.9661 failure unless margin separation proves otherwise.
- [ ] Define the hard safety invariant: any pothos fixture whose raw top-1 resolves to Pilea must route low-confidence, regardless of Pilea score.
- [ ] Define the validation split as leave-one-out over the six existing Pilea fixtures, with the held-out fixture's author and source photo excluded from threshold selection for that fold.
- [ ] Define a separate pothos boundary validation set using all existing `epipremnum-aureum` fixtures, with the known 0.9661 pothos->Pilea case treated as a must-fail-direct sentinel.
- [ ] Define pass/fail before tuning: +0 confident-wrong on held-out Pilea folds, +0 pothos->direct-Pilea violations, and no direct card for any validation photo that fails the pre-committed score and margin bar.
- [ ] Define fallback before tuning: if no threshold/margin pair passes, leave `boundary_pairs` strict-picker behavior in production and document direct Pilea as deferred.

## Phase 3 - Offline Threshold And Gate Experiment

- [ ] Run the current production `AccuracyEvalTest` on `pixel6Api34` before any direct-card change and save `baseline-0013-eval.csv` plus `baseline-0013-summary.md` under `docs/sprints/evidence/PLANTPOTTING-0013/`.
- [ ] Extract Pilea and pothos rows from the baseline CSV with raw top-1 label, raw top-1 score, raw top-2 label, raw top-2 score, margin, route, mapped top-3, and latency.
- [ ] Add or update an offline analysis script under `docs/sprints/evidence/PLANTPOTTING-0013/` that simulates candidate Pilea thresholds and margins from the CSV without changing app code.
- [ ] Run leave-one-out threshold selection exactly as pre-committed in `pilea-direct-card-eval-plan.md`.
- [ ] Produce `pilea-direct-card-threshold-sweep.csv` with fold id, tuning photos, held-out photo, threshold, margin bar, held-out route, correctness, confident-wrong, and pothos sentinel outcome.
- [ ] Produce `pilea-direct-card-decision.md` stating either "direct card earned" with the selected threshold/margin or "strict-picker retained" with the failing evidence.
- [ ] If the sweep fails +0 confident-wrong or any pothos sentinel reaches a simulated direct Pilea card, stop Thread A implementation and switch to the strict-picker documentation fallback.

## Phase 4 - Implement Conditional Direct Pilea Logic

- [ ] If the eval decision is strict-picker retained, make no production mapper relaxation and skip to the fallback documentation tasks.
- [ ] If the eval decision earns a direct card, add a Pilea-specific threshold to `per_species_thresholds` in `model_manifest.json` using the pre-committed selected value.
- [ ] If the eval decision earns a direct card, extend the narrowest existing manifest/mapper configuration to express the Pilea direct-card exception without changing the frozen seam.
- [ ] If the eval decision earns a direct card, modify `ModelScoreMapper` so the top-1=Pilea boundary gate first checks the measured Pilea exception, then otherwise falls back to the existing low-confidence pair route.
- [ ] Preserve the existing `boundary_pairs` rule as the load-bearing pothos safety gate; do not remove `pileaMappingRequiresBoundaryGate`.
- [ ] Ensure the Pilea direct exception composes with `high_confidence_abstain_margin = 0.30` and cannot bypass the normal high-confidence mapping checks.
- [ ] Ensure the pothos sentinel path is checked before returning any direct Pilea card, so a pothos input raw-predicted as Pilea remains low-confidence.

## Phase 5 - Unit And Contract Tests For Thread A

- [ ] Update `ModelScoreMapperBoundaryGateTest.confidentTop1PileaRoutesToPickerNotADirectPileaCard` or split it so the known pothos-like Pilea-top-1 sentinel still routes low-confidence under the new config.
- [ ] Add `ModelScoreMapperBoundaryGateTest.confidentTruePileaCanReturnDirectCardOnlyAbovePileaBar` using synthetic scores that clear the selected Pilea threshold and margin.
- [ ] Add `ModelScoreMapperBoundaryGateTest.truePileaBelowPileaBarStillRoutesToPickerWithPileaVisible` to preserve the fallback route.
- [ ] Add `ModelScoreMapperBoundaryGateTest.pothosToPileaSentinelNeverReturnsDirectPileaCard` with the 0.9661-style score row.
- [ ] Add a manifest parsing or contract test that verifies the production Pilea threshold/exception config is present only when the boundary pair is present.
- [ ] Keep `HousePlantClassMapValidationTest.pileaMappingRequiresBoundaryGate` active and update only if its assertion must also bind the new direct-exception config.
- [ ] Run targeted JVM tests for `ModelScoreMapper*`, `ModelManifestTest`, `PerSpeciesThresholdsContractTest`, `HousePlantClassMapValidationTest`, and result picker candidate tests.

## Phase 6 - Post-Change Accuracy Gate For Thread A

- [ ] Run `AccuracyEvalTest` on `pixel6Api34` after the Pilea direct-card candidate implementation.
- [ ] Save `post-pilea-direct-card-eval.csv` and `post-pilea-direct-card-summary.md` under `docs/sprints/evidence/PLANTPOTTING-0013/`.
- [ ] Compare baseline strict-picker vs direct-card candidate on Pilea fixtures, pothos fixtures, all clean photos, all perturbations, direct-card accuracy, low-confidence rate, and confident-wrong rate.
- [ ] Confirm every `epipremnum-aureum` fixture that raw-predicts Pilea routes to `LowConfidencePicker` and never a direct `pilea-peperomioides` card.
- [ ] Confirm every direct `pilea-peperomioides` card in the eval corresponds to an expected Pilea fixture and cleared the selected threshold and margin.
- [ ] Confirm Pilea fixtures that do not clear the selected bar still route to `LowConfidencePicker` with Pilea visible in `mapped_top3_kb_ids`.
- [ ] If post-change eval produces any confident-wrong regression caused by the Pilea exception, revert the exception, keep strict-picker, and document the failed candidate.

## Phase 7 - Fixture Broadening For Thread B

- [ ] Create `docs/sprints/evidence/PLANTPOTTING-0013/fixture-sourcing-log.md` with search terms, source URL, author, license URL, accept/reject decision, and reason codes.
- [ ] Source additional CC0/public-domain real photos broadly across `saintpaulia-ionantha`, `chamaedorea-elegans`, `beaucarnea-recurvata`, `alocasia`, `dracaena`, `begonia`, `ctenanthe`, and `schlumbergera-bridgesii`.
- [ ] Prefer independent authors and visually different contexts for each target species instead of many near-duplicate photos for one easy species.
- [ ] Use the existing reference-photo sourcing approach: Wikimedia Commons, GBIF/iNaturalist license-filtered CC0/PD, Smithsonian Gardens where applicable, Unsplash/Pexels only if their license acceptance already matches repo policy.
- [ ] Reject and log non-CC0/PD new fixtures, CC-BY-SA, CC-BY-NC, ND, unknown license, watermarked images, botanical plates, non-photo images, duplicates, and ambiguous identifications.
- [ ] Do not source or add any new Pilea images, even if license-clean supply is found while searching.
- [ ] Add accepted JPEGs under `app/src/androidTest/assets/identify-fixtures/` using the existing `<species-id>__NN.jpg` convention.
- [ ] Append each accepted fixture to `fixture-manifest.tsv` with the existing schema and exact expected KB species id.
- [ ] Update `LICENSE.txt` in the fixture directory if the accepted sources require human-readable provenance beyond the TSV.
- [ ] Run `FixtureLicenseManifestTest` after the first accepted batch and fix license/schema issues before sourcing more.
- [ ] Run `FixtureIntegrityTest` after each batch to catch corrupt images and unresolved KB species ids.
- [ ] Record a final reason-coded coverage table showing each of the 8 target species, starting count, added count, ending count, and any supply ceiling.

## Phase 8 - Accuracy Scorecard Refresh

- [ ] Run the default `AccuracyEvalTest` on the final fixture set and save `accuracy-eval.csv` plus `accuracy-eval-summary.md` under `docs/sprints/evidence/PLANTPOTTING-0013/`.
- [ ] Update or add the schema guard so `AccuracyEvalCsvSchemaTest` covers the current committed PLANTPOTTING-0013 scorecard without dropping required columns.
- [ ] Summarize the honest accuracy number with the larger fixture set, separating clean-photo metrics from perturbation metrics.
- [ ] Report per-species fixture counts so the headline number is not mistaken for deep per-species calibration.
- [ ] Compare PLANTPOTTING-0012 vs PLANTPOTTING-0013 scorecard metrics and call out whether changes are caused by fixture breadth, Pilea direct-card routing, or both.

## Phase 9 - Documentation Cleanup

- [ ] Edit `docs/kb/ml-mapping-notes.md` around the PLANTPOTTING-0009 Pilea deferral paragraph so it is clearly historical and no longer says in present tense that Pilea is unmapped.
- [ ] Edit the PLANTPOTTING-0010 "Deliberate non-mappings preserved" paragraph so `pileaIsNotMapped` and "boundary fix is still deferred" are marked as superseded by PLANTPOTTING-0012.
- [ ] Keep the PLANTPOTTING-0012 section's statement that Pilea is mapped behind a gate, and add a PLANTPOTTING-0013 note describing direct-card status: earned under threshold or retained strict-picker.
- [ ] Do not edit `docs/ROADMAP.md` unless the execution result differs from the already accurate roadmap state.

## Phase 10 - Version, APK, Tag, And Release Signal

- [ ] Bump `app/build.gradle.kts` to `versionCode = 7` and `versionName = "0.7.0"`.
- [ ] Run `.\gradlew.bat verifyNoNetworking`.
- [ ] Run `pwsh scripts/check-stub-isolation.sh`.
- [ ] Run `.\gradlew.bat ktlintCheck`.
- [ ] Run `.\gradlew.bat lintDebug`.
- [ ] Run `.\gradlew.bat testDebugUnitTest`.
- [ ] Run the established local `pixel6Api34` GMD/androidTest command for `AccuracyEvalTest` and on-device regressions.
- [ ] Confirm `OnDeviceModelRealInterpreterTest` still pins the bundled AIY V1/3 regression anchor.
- [ ] Build the v0.7.0 debug APK.
- [ ] Copy the debug APK to `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\`.
- [ ] Verify with PowerShell that the APK is actually present in the Dropbox folder and record filename, size, and timestamp.
- [ ] Create the `v0.7.0` git tag after verification gates pass.
- [ ] Push the `v0.7.0` tag to origin so the public GitHub Release workflow fires.
- [ ] Confirm the GitHub Release workflow published or record the exact CI failure if the tag push did not produce a release.

## Sequencing

- [ ] Complete Phase 1 before changing code so the executor starts from the real 0012 gate and fixture state.
- [ ] Complete Phases 2 and 3 before any direct-card implementation; the threshold and margin must be pre-committed, not chosen after seeing validation results.
- [ ] Complete Phases 4 through 6 before finalizing Thread A; the direct card is conditional and must fall back to strict-picker if the eval does not clear the bar.
- [ ] Complete fixture sourcing in Phase 7 before the final scorecard refresh in Phase 8.
- [ ] Complete documentation cleanup after the Thread A decision so `ml-mapping-notes.md` records the actual sprint result.
- [ ] Complete versioning, APK delivery, and tag push only after static gates, unit tests, fixture gates, and on-device eval are green or have documented environment-only failures.

## Risks And Mitigations

- [ ] Risk: The 6-photo Pilea set is too thin to support a direct-card exception. Mitigation: use leave-one-out held-out validation, author/source-photo separation, and strict fallback to the 0012 picker.
- [ ] Risk: A high Pilea score alone still admits the known pothos->Pilea failure. Mitigation: compose the elevated Pilea threshold with the existing boundary gate and keep the pothos sentinel as a CI-bound invariant.
- [ ] Risk: Threshold tuning overfits the existing Pilea fixtures. Mitigation: pre-commit the grid and report held-out fold outcomes, not only aggregate fixture wins.
- [ ] Risk: Broad fixture additions shift the headline score downward. Mitigation: treat that as honest measurement, not a regression to hide; report fixture-count changes clearly.
- [ ] Risk: License-clean supply is uneven across the 8 target species. Mitigation: spread sourcing broadly, log supply ceilings, and do not pad one easy species while leaving others untouched.
- [ ] Risk: Updating `AccuracyEvalCsvSchemaTest` to the current scorecard could mask old evidence assumptions. Mitigation: keep all load-bearing columns and note which sprint's CSV is the committed scorecard.
- [ ] Risk: The release is not published because only `versionName` changed. Mitigation: create and push `v0.7.0`; verify the release workflow result.

## Acceptance Criteria

- [ ] `PlantIdentifier`, `IdentificationResult`, and `IdSource` remain untouched.
- [ ] Pilea direct-card behavior is either shipped with documented held-out +0 confident-wrong evidence or explicitly deferred with strict-picker retained.
- [ ] A pothos->Pilea input can never reach a direct Pilea card; this is covered by unit tests and final `AccuracyEvalTest` evidence.
- [ ] `per_species_thresholds` contains `pilea-peperomioides` only if the direct-card gate shipped and the selected value is documented in PLANTPOTTING-0013 evidence.
- [ ] `HousePlantClassMapValidationTest.pileaMappingRequiresBoundaryGate` remains green and continues binding Pilea mapping to the production boundary gate.
- [ ] No new Pilea fixtures are added.
- [ ] Additional CC0/PD fixtures are added broadly across the 8 targeted single-photo species, bounded by license-clean supply and documented in `fixture-sourcing-log.md`.
- [ ] `FixtureLicenseManifestTest`, `FixtureIntegrityTest`, and `AccuracyEvalCsvSchemaTest` are green.
- [ ] The refreshed `AccuracyEvalTest` scorecard exists under `docs/sprints/evidence/PLANTPOTTING-0013/` and reports clean vs perturbation metrics separately.
- [ ] `docs/kb/ml-mapping-notes.md` no longer contains stale present-tense claims that Pilea is unmapped, `pileaIsNotMapped` passes, or the Pilea boundary fix is still deferred.
- [ ] `verifyNoNetworking`, `ktlintCheck`, `scripts/check-stub-isolation.sh`, `lintDebug`, `testDebugUnitTest`, and local `pixel6Api34` on-device eval are green or have documented environment-only failures.
- [ ] The shipped build is v0.7.0 / versionCode 7.
- [ ] The debug APK is verified present in `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\`.
- [ ] The `v0.7.0` tag is pushed and the public GitHub Release workflow has published the APK or has a documented CI failure.
