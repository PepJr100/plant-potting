# Sprint PLANTPOTTING-0011-GEMINI: Accuracy & Trust Calibration

The primary intent of this sprint is to address the production model's high confident-wrong rate on messy, real-world photos, an issue that severely erodes user trust in the app. Since the model itself is frozen and we cannot fine-tune it or swap it, we will combat this via network-free, behind-the-seam maneuvers. This includes building out a robust, purely CC0/PD real-world evaluation dataset (plus synthetic perturbations) to rigorously measure the gap, adjusting the model score gating in `ModelScoreMapper` to confidently abstain rather than confidently err, and optionally experimenting with test-time augmentation (multi-crop) to squeeze raw accuracy improvements from the existing model. We will also address small UI feedback like thickening the confidence bar and swapping botanical plate reference images for real CC0 photos.

## Goals

- Establish a rigorous, measured baseline for top-1 accuracy and confident-wrong rate using an expanded set of CC0/PD real photos and synthetically perturbed variants.
- Drastically reduce the confident-wrong rate by tuning global and/or per-species gating thresholds, shifting these failures into the low-confidence "pick manually" flow.
- Investigate and potentially adopt multi-crop / test-time-augmentation (TTA) or center-crop strategies if they measurably improve top-1 accuracy on the evaluation set without training.
- Address outstanding UI feedback by thickening the `ResultScreen` confidence bar and replacing botanical plates with actual CC0 photos.

## Non-Goals

- No model swap, model training, or fine-tuning.
- No self-shot or first-party imagery will be used for any purpose, including evaluation. All new photos must be license-clean CC0/PD.
- No changes to the frozen `PlantIdentifier`, `IdentificationResult`, or `IdSource` interfaces.
- No mapping of Pilea or resolving the pothos↔Pilea boundary.
- No version bumps for AGP, Kotlin, Compose, Hilt, or TFLite.
- No implementation of My Plants surviving uninstall/reinstall (deferred).

## Task List

### Phase 1: Measure (The Baseline)
- [ ] Research and download an expanded set of CC0/PD real-world houseplant photos for mapped species (e.g., from Wikimedia Commons).
- [ ] Verify licenses, build an attribution manifest for the new eval photos, and implement a cross-check test mirroring the reference-image manifest test.
- [ ] Implement synthetic image perturbation functions (e.g., blur, crop, rotate, brightness adjustments) to approximate messy captures in the eval harness.
- [ ] Extend `OnDeviceModelRealInterpreterTest` and `ModelSwapEvaluationTest` to run over the expanded, perturbed fixture set and calculate honest top-1 accuracy and confident-wrong rates.
- [ ] Record the initial baseline metrics in `docs/sprints/evidence/PLANTPOTTING-0011/baseline_metrics.md`.

### Phase 2: Abstain (Threshold Tuning)
- [ ] Modify `ModelScoreMapper` to support an additional margin-based abstention check above the plain high-confidence gate (e.g., abstain if the margin between top-1 and top-2 is too small).
- [ ] Seed `per_species_thresholds` in `ModelManifest.Thresholds` for specific chronic offenders (like snake plant) if global tuning is insufficient.
- [ ] Experiment with raising `high_confidence_plain` and tuning `high_confidence_margin_min` / `high_confidence_margin_delta` to minimize the confident-wrong rate.
- [ ] Record the tuned thresholds and the new metric numbers in `docs/sprints/evidence/PLANTPOTTING-0011/tuned_metrics.md`.

### Phase 3: Try to Improve (Raw Accuracy Levers)
- [ ] Experiment with center-crop vs. full-frame resize in `ImagePreprocessor`.
- [ ] Implement multi-crop / test-time-augmentation (TTA) averaging logits over several crops, without violating the `PlantIdentifier` interface.
- [ ] Evaluate the accuracy vs. latency trade-offs of TTA using the updated eval harness.
- [ ] Adopt TTA or alternate cropping strategies only if they empirically improve top-1 accuracy; document the decision in the evidence directory.

### Phase 4: Fold-in Refinements
- [ ] Modify the `LinearProgressIndicator` in `ResultScreen` to increase its height (thicker confidence bar).
- [ ] Replace botanical-plate reference images (peace lily, poinsettia, parlor palm, dracaena, philodendron-pink-princess) with CC0/PD photographs in `app/src/main/res/drawable-nodpi/`.
- [ ] Update `docs/licenses/reference-images.md` and the `ReferenceImageManifestTest` to reflect the new photographic reference images. (Document any species where a plate remains the only clean option).

## Sequencing and Dependencies

1. **Phase 1 (Measure)** must block all other phases, as the evaluation harness and the new perturbed dataset are required to objectively measure any changes in Phase 2 or 3.
2. **Phase 2 (Abstain)** and **Phase 3 (Try to Improve)** can proceed in parallel or sequentially, but both must continuously validate against the metrics established in Phase 1.
3. **Phase 4 (Fold-in Refinements)** is independent and can be picked up at any time during the sprint.

## Risks & Mitigations

- **Risk:** Over-fitting thresholds to a tiny clean fixture set, causing poor generalization in the wild.
  - **Mitigation:** The synthetic perturbations (blur, crop, rotation) introduced in Phase 1 effectively multiply the evaluation set size and simulate out-of-distribution variance, making the evaluation harness harder to overfit. We will also avoid overly granular per-species thresholds unless absolutely necessary.
- **Risk:** Finding sufficient CC0/PD images for all classes is historically difficult (~1-6.8% clean).
  - **Mitigation:** Accept that the base set will be small. Heavily rely on the programmatic synthetic perturbation harness to generate enough test cases from the clean base set.
- **Risk:** Multi-crop/TTA adds unacceptable latency on low-end devices.
  - **Mitigation:** The `OnDeviceModelRealInterpreterTest` should measure inference time. If latency spikes beyond acceptable UX bounds, TTA will be discarded regardless of accuracy gains.

## Acceptance Criteria

- [ ] The evaluation harness tests successfully run against an expanded set of verified CC0/PD photos and their synthetically perturbed variants.
- [ ] A documented baseline is captured showing top-1 accuracy and confident-wrong rate.
- [ ] The confident-wrong rate on the evaluation set is significantly reduced through tuned confidence and margin gating, with the "pick manually" flow handling the abstained cases.
- [ ] Experiments on center-crop and test-time-augmentation are completed, measured, and the winning strategy is merged (or deliberately rejected).
- [ ] `verifyNoNetworking` and `scripts/check-stub-isolation.sh` pass.
- [ ] The `ResultScreen` confidence bar is visually thicker.
- [ ] Botanical plate reference images are replaced with CC0/PD photos (or documented if impossible), and `ReferenceImageManifestTest` is green.
- [ ] `HousePlantClassMapValidationTest` remains green without mapping Pilea.
