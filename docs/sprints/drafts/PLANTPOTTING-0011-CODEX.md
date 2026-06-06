# PLANTPOTTING-0011 - Accuracy & Trust Sprint

This sprint makes the shipped `house_plant_species_mobilenetv2` identifier more trustworthy without swapping, training, networking, or changing the frozen `PlantIdentifier` / `IdentificationResult` / `IdSource` seam: first measure real-photo and perturbed-photo behavior with license-clean fixtures, then tune confidence abstention so confidently wrong predictions fall back to manual picking, then adopt only no-training preprocessing or test-time-augmentation changes that measurably improve top-1 accuracy at an acceptable latency cost.

## Goals

- Establish an honest before/after evaluation for top-1 accuracy, high-confidence correct rate, low-confidence routing rate, and confident-wrong rate for `house_plant_species_mobilenetv2`.
- Expand the on-device fixture set using only CC0/public-domain real photographs, with checked-in attribution and tests that enforce the license policy.
- Stress the model with deterministic synthetic perturbations of those license-clean photos: blur, crop, rotation, and brightness changes.
- Tune existing `ModelScoreMapper` / `ModelManifest.Thresholds` confidence policy so wrong high-confidence cards become low-confidence manual-pick flows.
- Evaluate center-crop and multi-crop/test-time-augmentation behind existing model internals, keeping only variants that improve measured accuracy enough to justify added latency.
- Fold in the 0010 review fixes: thicker `ResultScreen` confidence bar and replacement/documentation of botanical-plate reference images.

## Non-goals

- No bundled model replacement, retraining, fine-tuning, or training-data pipeline work.
- No changes to `PlantIdentifier`, `IdentificationResult`, or `IdSource`; any new evaluation metadata stays in tests, evidence files, manifest fields, or internal side channels.
- No first-party, self-shot, or principal-supplied plant photos anywhere, including uncommitted evaluation inputs.
- No Pilea KB entry, no Pilea mapping, and no pothos/Pilea-specific special-case fix; `HousePlantClassMapValidationTest` must continue to guard Pilea absence.
- No AGP, Kotlin, Compose, Hilt, or TFLite dependency upgrades.
- No My Plants backup/export/uninstall persistence work.
- No network access at app runtime; `verifyNoNetworking` and `scripts/check-stub-isolation.sh` remain required gates.

## Phased Task List

### Phase 1 - Evaluation Dataset And License Discipline

- [ ] Define the PLANTPOTTING-0011 fixture target list from mapped, popular KB species already represented in `plant_class_map.json`, prioritizing snake plant (`dracaena-trifasciata`), pothos (`epipremnum-aureum`), peace lily (`spathiphyllum-wallisii`), monstera, jade, ZZ plant, moth orchid, calathea/orbifolia, and any other mapped species with clean sources.
- [ ] Add only CC0/public-domain real-photo JPEG fixtures under `app/src/androidTest/assets/identify-fixtures/`, using `<kb-species-id>__NN.jpg` or an equivalent naming convention that supports multiple photos per species without breaking the existing single-photo fixtures.
- [ ] Replace the current freeform fixture `LICENSE.txt` with or extend it into a machine-checkable attribution manifest containing filename, expected KB species id, source URL, author where available, license, license URL, acquisition date, and notes.
- [ ] Add an androidTest or JVM manifest cross-check for identify fixtures, mirroring `ReferenceImageManifestTest`, that fails when a bundled identify fixture lacks an attribution row or declares anything other than CC0/public-domain.
- [ ] Add a fixture integrity test that decodes every fixture, verifies nonzero dimensions, verifies the expected species id resolves in the KB, and marks whether the species is reachable by the active model mapping.
- [ ] Document scarcity honestly in `docs/sprints/evidence/PLANTPOTTING-0011/fixture-manifest-summary.md`, including rejected near-misses where license terms were not clean enough.

### Phase 2 - Perturbation Harness And Baseline Metrics

- [ ] Extend `ModelSwapEvaluationTest` or add a focused PLANTPOTTING-0011 evaluation test that runs `house_plant_species_mobilenetv2` over each base fixture plus deterministic perturbations: center crop, off-center crop, mild blur, stronger blur, +/-10 degree rotation, darker, brighter, and aspect-ratio-preserving resize/crop variants.
- [ ] Keep perturbations generated in test code rather than checked in as image assets, so license-clean source photos remain the only real-photo files and the stress set is reproducible.
- [ ] Emit a CSV under the instrumentation target files dir with columns for model id, fixture filename, base species id, perturbation id, expected KB id, raw top-1 label, raw top-1 score, raw top-2 label/score, top1-top2 margin, mapped top-1 KB id, mapped top-3 KB ids, route, correctness, confident-wrong flag, low-confidence flag, latency ms, and failure.
- [ ] Emit a Markdown summary for `docs/sprints/evidence/PLANTPOTTING-0011/` with aggregate top-1 accuracy, high-confidence correct rate, confident-wrong rate, low-confidence routing rate, in-vocab-only metrics, and per-species breakdowns.
- [ ] Capture the untouched baseline numbers before threshold or preprocessing changes and commit them as `baseline-eval.csv` and `baseline-eval-summary.md` evidence.
- [ ] Add a small parser/check test for the generated CSV schema if practical, so future evidence files cannot silently drop the confident-wrong or margin columns.

### Phase 3 - Confidence Abstention Tuning

- [ ] Add unit coverage around `ModelScoreMapper` for the existing direct high-confidence path, existing margin path, low-confidence path, unmapped confident top label path, and per-species threshold override behavior before changing policy.
- [ ] Evaluate candidate threshold policies on the baseline CSV without changing the model: raise `high_confidence_plain`, adjust `high_confidence_margin_min` / `high_confidence_margin_delta`, add an above-plain minimum top1-top2 margin abstention, and seed per-species thresholds only for repeat offenders with multiple independent clean fixtures or perturbation families.
- [ ] Prefer global policy changes over per-species thresholds unless the evidence shows one species is a chronic false-positive source and the override reduces confident-wrong rate without hiding true positives for that species.
- [ ] Implement the selected policy in `ModelScoreMapper`, `ModelManifest.Thresholds`, and `app/src/main/assets/ml/house_plant_species_mobilenetv2/model_manifest.json` without changing public identify interfaces.
- [ ] Update model mapping/calibration notes in `docs/kb/ml-mapping-notes.md` with the chosen thresholds, rejected threshold candidates, and the measured tradeoff between fewer confident-wrong results and more low-confidence routing.
- [ ] Re-run the PLANTPOTTING-0011 eval and save `post-abstention-eval.csv` plus `post-abstention-eval-summary.md` in the evidence directory.

### Phase 4 - Preprocessing And No-Training Accuracy Experiments

- [ ] Add an internal preprocessing strategy abstraction behind `OnDevicePlantIdentifier` / `ImagePreprocessor` without changing `PlantIdentifier`, starting with the current full-frame square resize as the control.
- [ ] Implement center-crop-to-square then resize for FLOAT32 input and evaluate it against the same base and perturbed fixture set.
- [ ] Implement a bounded multi-crop/TTA experiment that averages scores across center crop plus four corner-biased crops, with optional horizontal flip only if it is botanically safe and measured separately.
- [ ] Measure latency for current resize, center-crop, and multi-crop/TTA using the existing median-of-5 style in `ModelSwapEvaluationTest`, reporting median and worst observed latency per image.
- [ ] Adopt center-crop only if it improves in-vocab top-1 or reduces confident-wrong rate without materially increasing low-confidence routing.
- [ ] Adopt multi-crop/TTA only if it gives a clear accuracy or confident-wrong improvement over center-crop and the added on-device latency is acceptable for camera identification; otherwise leave the experiment documented but disabled.
- [ ] Save `preprocessing-experiments.csv` and `preprocessing-experiments-summary.md` in `docs/sprints/evidence/PLANTPOTTING-0011/` with an explicit keep/drop decision for each variant.

### Phase 5 - Review Fold-ins

- [ ] Increase the `LinearProgressIndicator` height in `app/src/main/java/com/darkfactory/plantpotting/result/ResultScreen.kt` so the confidence bar is visually thicker while preserving existing `ResultScreenTags.CONFIDENCE_BAR` testability.
- [ ] Replace botanical-plate/drawing reference images for peace lily, poinsettia, parlor palm, dracaena, and philodendron-pink-princess with CC0/public-domain photographs where clean sources exist.
- [ ] For any reference image that must remain a botanical plate, add an explicit note in `docs/licenses/reference-images.md` explaining the clean-photo search failure and why the plate remains temporarily acceptable.
- [ ] Update the reference-image attribution manifest and keep `ReferenceImageManifestTest` green.

### Phase 6 - Verification And Evidence Packaging

- [ ] Run local JVM tests with `.\gradlew.bat testDebugUnitTest`.
- [ ] Run compile-clean instrumentation sources with the repo's established local Gradle target, even though `pixel6Api34` execution is CI-only.
- [ ] Run `.\gradlew.bat verifyNoNetworking`.
- [ ] Run `pwsh scripts/check-stub-isolation.sh`.
- [ ] On CI/GMD, run the PLANTPOTTING-0011 androidTest evaluation on `pixel6Api34` and pull the generated CSV/Markdown evidence into `docs/sprints/evidence/PLANTPOTTING-0011/`.
- [ ] Confirm `HousePlantClassMapValidationTest` still proves Pilea is absent and no Pilea KB mapping was added.
- [ ] Confirm the final evidence summary states baseline vs final top-1 accuracy, confident-wrong rate, low-confidence routing rate, and latency impact.

## Sequencing And Dependencies

Phase 1 must finish before any metric is trusted, because the fixture manifest and decode checks define the legal and technical evaluation set. Phase 2 then creates the baseline and should be run before any threshold or preprocessing edits. Phase 3 uses the baseline CSV to choose abstention changes and must rerun the same harness afterward. Phase 4 should branch from the post-abstention state so preprocessing is judged against the policy users would actually see. Phase 5 can proceed in parallel after Phase 1 license sourcing patterns are established, but image replacements should share the same CC0/public-domain review discipline. Phase 6 is the final gate, with local JVM/static checks first and `pixel6Api34` GMD evidence as the authoritative on-device run.

## Risks & Mitigations

- Tiny clean-photo set may overfit thresholds. Mitigation: separate base-photo and perturbation metrics, report per-species counts, prefer global thresholds, require multiple independent fixtures before per-species overrides, and document rejected threshold candidates.
- License-clean imagery may be too scarce for some species. Mitigation: keep the fixture count honest, use deterministic perturbations to stress robustness, and document search failures rather than weakening the CC0/public-domain rule.
- Perturbations can overweight one source photo. Mitigation: aggregate both "all variants" and "per base image averaged" metrics so one heavily perturbed photo cannot dominate the sprint headline.
- Abstention may make the app feel less capable. Mitigation: set acceptance around reducing confident-wrong cards, explicitly report the low-confidence routing increase, and preserve top-3 candidates for manual pick.
- Multi-crop/TTA may be too slow on device. Mitigation: measure median and worst-case latency in the same harness and keep TTA disabled unless the accuracy gain is clear enough to justify the cost.
- Center-crop may remove diagnostic plant context. Mitigation: evaluate current full-frame resize, center-crop, and crop ensembles against the same fixtures before adopting any change.
- Public API freeze may tempt side-channel leaks. Mitigation: keep additional score/margin data internal to mapper/eval code and do not edit `PlantIdentifier`, `IdentificationResult`, or `IdSource`.

## Acceptance Criteria

- The sprint ships no edits to `PlantIdentifier`, `IdentificationResult`, or `IdSource`.
- All added identify and reference images are CC0/public-domain or explicitly documented public-domain equivalents, with manifest rows and passing manifest tests.
- `docs/sprints/evidence/PLANTPOTTING-0011/` contains baseline, post-abstention, and preprocessing experiment CSV/Markdown evidence from the on-device harness.
- The final policy reduces confident-wrong rate versus baseline on the PLANTPOTTING-0011 evaluation set, with the low-confidence routing increase reported as an explicit tradeoff.
- Any preprocessing/TTA change that remains enabled has measured top-1 or confident-wrong improvement and documented latency impact; any rejected variant is documented as rejected.
- `ResultScreen` shows a thicker confidence bar without removing existing test tags or result behavior.
- Botanical-plate reference-image replacements are complete where clean photos exist; remaining plates are justified in `docs/licenses/reference-images.md`.
- `.\gradlew.bat testDebugUnitTest`, `.\gradlew.bat verifyNoNetworking`, `pwsh scripts/check-stub-isolation.sh`, and the repo's instrumentation compile/GMD path are green or have documented environment-only failures.
- Pilea remains unmapped and deferred, and the Pilea-absence guard remains green.
