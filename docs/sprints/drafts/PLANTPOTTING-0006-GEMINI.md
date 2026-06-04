# PLANTPOTTING-0006 Sprint Plan

## Goals
- Complete the V0.1 multi-species sweep by adding the second and final in-vocab species, `crassula-ovata` (jade plant).
- Provide real-photo evidence across both AIY V1/3 ↔ KB overlaps.
- Resolve two UX findings from the 0005 sprint review regarding jargon and "(0%)" chip rendering.

## Non-goals
- Model swap (AIY V1/3 stays).
- Delegate / quantization variants (INT8 / GPU / NNAPI).
- Net-new screens (`CaptureFailedScreen`, `Settings`, `ModelInfoScreen`).
- ML training / retraining.
- Interface changes to `PlantIdentifier` or `IdentificationResult`.
- KB edits (`species.json`, `archetypes.json`, `plant_class_map.json`).
- AGP / Kotlin / Compose / Hilt / TFLite version bumps.
- README rewrite.
- `expected-artifacts` re-baselining.
- Net-new effort on the `CameraUiState.Failure` live visual.

## Scope Boundaries
- **No new infrastructure**: Reuse the 0005 fixture pattern, `OnDeviceModelRealInterpreterTest`, and `perSpeciesThresholds` mechanism.
- **Adjacent Cleanup**: Permitted *only* if the `crassula-ovata` data forces it (e.g., splitting `FakeFixedIdentifier` if a third knob is added, or revisiting `perSpeciesThresholds` top-1-only override semantics if seeding is required).

## Tasks & Sequencing

### Phase 1: Crassula Ovata Probing & Assertion
- [ ] Source a CC-licensed `crassula-ovata` photo from Wikimedia Commons (CC-BY-SA, CC-BY, or CC0).
- [ ] Centre-crop the photo to match the existing Monstera fixture and add it to `identify-fixtures/`.
- [ ] Append the photo's provenance to `identify-fixtures/LICENSE.txt`.
- [ ] Run the on-device GMD probe on `pixel6Api34` using `OnDeviceModelRealInterpreterTest`.
- [ ] Record the top-1, top-3, score, route, and source for `crassula-ovata` in the results doc and `ml-mapping-notes.md`.
- [ ] Add the accuracy-bearing assertion for `crassula-ovata` (`speciesId == "crassula-ovata" && !lowConfidence`) to the CI. If it clears the 0.55 global cleanly, use the preferred form.
- [ ] Determine if `perSpeciesThresholds` needs seeding. If the probe shows a per-class override is justified, seed the threshold in `model_manifest.json`. Record the decision in `ml-mapping-notes.md` regardless.
- [ ] *Conditional:* Revisit `perSpeciesThresholds` top-1-only override semantics ONLY IF real seeding for crassula forces the decision. Log it otherwise.

### Phase 2: UX Fixes & Cleanup
- [ ] Rewrite `R.string.low_conf_subtitle` to drop the word "model". Use the suggested copy: *"We're best at common houseplants — confirm or pick from the list below."*
- [ ] Fix the degenerate-capture rendering for "(0%)" chips. **Recommendation:** Display `<1%` for non-zero scores that round down to zero. This retains the chip's informational format without incorrectly implying an absolute zero probability, which feels broken to users.
- [ ] Update `LowConfidencePickerScreenTest` to cover the chosen degenerate-capture "(0%)" chip behavior.
- [ ] *Conditional:* Split `FakeFixedIdentifier` into focused fakes (e.g., `FakeLowConfidenceIdentifier`, `FakeUnmappedIdentifier`) ONLY IF Phase 2 introduces a third knob or interface to it.

## Risks & Mitigations
- **Risk:** `crassula-ovata` probe scores below the 0.55 global threshold, requiring a per-species override.
  - **Mitigation:** The `perSpeciesThresholds` mechanism is already built and tested. We will seed it explicitly and document the change without needing new infrastructure.
- **Risk:** Degenerate capture UI logic becomes complex.
  - **Mitigation:** Use a simple formatting threshold (e.g., displaying `<1%`), thoroughly verifying the display via Compose UI tests in `LowConfidencePickerScreenTest`.

## Acceptance Criteria
- `crassula-ovata` fixture is checked in with correct licensing provenance.
- CI passes with a new `OnDeviceModelRealInterpreterTest` assertion for `crassula-ovata`.
- Probe results are documented in `ml-mapping-notes.md` (top-1, top-3, score, route, source).
- Subtitle jargon is removed from the app UI.
- Low confidence chips no longer display "(0%)" for small, non-zero probabilities, backed by `LowConfidencePickerScreenTest`.
- The multi-species sweep for V0.1 is complete.
