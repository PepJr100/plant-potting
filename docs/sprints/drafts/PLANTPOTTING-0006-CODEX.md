# PLANTPOTTING-0006 — Codex Draft

## Goal

Complete the V0.1 in-vocabulary evidence sweep by extending the PLANTPOTTING-0005 real-photo
probe path from `monstera-deliciosa` to the second and final AIY V1/3 overlap,
`crassula-ovata`, while closing the two low-confidence UX findings from the 0005 review.

- [ ] Add real-photo evidence for `crassula-ovata` using the existing fixture/provenance pattern.
- [ ] Run and document the on-device GMD probe result for `crassula-ovata` on `pixel6Api34`.
- [ ] Add an accuracy-bearing assertion for `crassula-ovata` that reflects the probe data.
- [ ] Decide whether `perSpeciesThresholds` needs a `crassula-ovata` entry based only on probe evidence.
- [ ] Remove engineer-facing wording from the low-confidence subtitle.
- [ ] Fix the low-confidence chip rendering that currently shows tiny non-zero scores as `(0%)`.

## Non-Goals

- [ ] Do not swap or retrain the model; AIY Plants V1/3 remains the model under test.
- [ ] Do not add delegate, quantization, GPU, NNAPI, or benchmark variants.
- [ ] Do not add new screens or expand the camera failure live visual work.
- [ ] Do not change `PlantIdentifier` or `IdentificationResult` interfaces.
- [ ] Do not edit locked KB assets: `species.json`, `archetypes.json`, or `plant_class_map.json`.
- [ ] Do not bump AGP, Kotlin, Compose, Hilt, TFLite, or other platform dependencies.
- [ ] Do not re-baseline `expected-artifacts`.
- [ ] Do not broaden README work beyond append-only documentation if a narrow note is required.
- [ ] Do not revisit `perSpeciesThresholds` lower-ranked or margin semantics unless real crassula seeding forces the issue.

## Scope Boundaries

- [ ] Reuse `app/src/androidTest/assets/identify-fixtures/` for the `crassula-ovata` fixture.
- [ ] Reuse `identify-fixtures/LICENSE.txt` as the provenance format instead of creating new fixture metadata.
- [ ] Reuse `OnDeviceModelRealInterpreterTest` for the probe and final assertion.
- [ ] Reuse the existing manifest path from `model_manifest.json` `per_species_thresholds` through `ModelManifest.kt` into `ModelScoreMapper.kt`.
- [ ] Keep any `FakeFixedIdentifier` cleanup conditional on needing another constructor knob or interface for this sprint.
- [ ] Keep any threshold contract updates limited to the actual manifest state after the crassula probe decision.

## Recommended UX Direction

For degenerate low-confidence chips, the narrowest fix is to hide the percentage suffix when
the rounded display value would be `0%`. This avoids implying an impossible exact zero while
preserving the candidate list and without inventing a visibility threshold that could hide useful
fallback choices. Displaying `<1%` is also honest, but it adds visual noise to already weak
recommendations; suppressing chips below a threshold is a larger product decision because it can
remove options from the recovery path.

- [ ] Implement chip score formatting so candidates whose non-zero score floors or rounds to `0%` render without the `(x%)` suffix.
- [ ] Preserve normal percentage suffixes for candidates that display as `1%` or higher.
- [ ] Add `LowConfidencePickerScreenTest` coverage for a tiny non-zero score that previously rendered `(0%)`.

## Work Plan

### Phase 1 — Fixture and Provenance

- [ ] Find a CC-BY-SA, CC-BY, or CC0 Wikimedia Commons photo of `crassula-ovata` suitable for a centered square crop.
- [ ] Add `app/src/androidTest/assets/identify-fixtures/crassula-ovata.jpg` with crop dimensions matching the Monstera fixture convention.
- [ ] Append the crassula source, author, license, source URL, and transformation note to `app/src/androidTest/assets/identify-fixtures/LICENSE.txt`.
- [ ] Verify the fixture is readable from androidTest assets using the same code path as `monstera-deliciosa.jpg`.

### Phase 2 — Probe and Evidence

- [ ] Add a temporary or focused `crassula-ovata` probe path to `OnDeviceModelRealInterpreterTest` mirroring the Monstera probe style.
- [ ] Run the on-device probe on `pixel6Api34`.
- [ ] Record top-1 species, top-3 candidates, score, route, low-confidence state, fixture source, and test device in `docs/sprints/results/PLANTPOTTING-0006.md`.
- [ ] Append the crassula mapping/probe result to `docs/kb/ml-mapping-notes.md` in the same style as the Monstera entry.

### Phase 3 — Accuracy Assertion and Threshold Decision

- [ ] If the probe clears the global `0.55` threshold cleanly, add an assertion that `speciesId == "crassula-ovata"` and `lowConfidence == false`.
- [ ] If the probe maps correctly but does not clear the global threshold, seed `crassula-ovata` in `per_species_thresholds` with the narrowest justified override.
- [ ] If a threshold is seeded, update `ModelScoreMapperPerSpeciesThresholdTest` and `PerSpeciesThresholdsContractTest` for the new manifest contract.
- [ ] If the probe result is not a clean top-1 crassula match, document the honest fallback assertion and explain why it still represents the real model behavior.
- [ ] Record the explicit seed/no-seed decision in both the sprint results doc and `ml-mapping-notes.md`.

### Phase 4 — Low-Confidence UX Fixes

- [ ] Rewrite `R.string.low_conf_subtitle` to remove "model" and use user-facing copy such as "We're best at common houseplants - confirm or pick from the list below."
- [ ] Update `LowConfidencePickerSubtitleContractTest` to pin the new subtitle copy.
- [ ] Implement the chosen chip-score rendering behavior in `LowConfidencePickerScreen.kt` or the nearest existing formatting helper.
- [ ] Add or update `LowConfidencePickerScreenTest` so a tiny non-zero score no longer renders as `(0%)`.
- [ ] Confirm existing normal-score chip rendering remains covered.

### Phase 5 — Verification

- [ ] Run the focused JVM tests for `LowConfidencePickerScreenTest` and `LowConfidencePickerSubtitleContractTest`.
- [ ] Run the focused mapper/manifest tests if `per_species_thresholds` changes.
- [ ] Run the focused androidTest containing `OnDeviceModelRealInterpreterTest` on `pixel6Api34`.
- [ ] Run `scripts/check-stub-isolation.sh`.
- [ ] Run `verifyNoNetworking`.
- [ ] Run the relevant `scripts/integration-flow.ps1` mode required by the sprint branch before merge.

## Sequencing and Dependencies

- [ ] Complete fixture provenance before the GMD probe so the evidence is tied to a licensed source.
- [ ] Run the crassula probe before changing thresholds so the manifest remains evidence-driven.
- [ ] Decide the threshold contract before updating mapper tests so tests assert the final intended manifest state.
- [ ] Land subtitle and chip rendering changes after reading existing UI tests so test updates match local patterns.
- [ ] Run focused tests before full integration gates so failures are cheaper to diagnose.

## Risks and Mitigations

- [ ] Risk: the selected crassula photo is not representative enough and produces misleading evidence; mitigation: prefer a clear, centered, healthy jade plant image and document the exact crop/source.
- [ ] Risk: crassula scores below the global threshold and needs the first real per-species override; mitigation: seed only if top-1 is correct and the override is justified by the recorded score.
- [ ] Risk: crassula exposes ambiguity in top-1-only override semantics; mitigation: revisit mapper semantics only if a seeded threshold cannot be represented honestly by current behavior.
- [ ] Risk: hiding `(0%)` could obscure ranking confidence; mitigation: hide only the degenerate suffix while preserving candidate order and normal percentages at `1%` or higher.
- [ ] Risk: subtitle copy changes break pinned contract tests; mitigation: update `LowConfidencePickerSubtitleContractTest` in the same change as `strings.xml`.
- [ ] Risk: fake identifier setup becomes harder to maintain if more low-confidence scenarios are added; mitigation: split `FakeFixedIdentifier` only if this sprint otherwise adds another knob or interface.

## Acceptance Criteria

- [ ] `crassula-ovata.jpg` exists under `app/src/androidTest/assets/identify-fixtures/` and has appended license/provenance in `LICENSE.txt`.
- [ ] `OnDeviceModelRealInterpreterTest` includes a `crassula-ovata` assertion based on real on-device probe data.
- [ ] `docs/sprints/results/PLANTPOTTING-0006.md` records crassula top-1, top-3, score, route, low-confidence state, source, and device.
- [ ] `docs/kb/ml-mapping-notes.md` includes the crassula probe result and threshold seed/no-seed decision.
- [ ] `model_manifest.json` keeps `per_species_thresholds` empty if crassula clears the global threshold cleanly.
- [ ] If `model_manifest.json` gains a crassula override, mapper and manifest contract tests prove the override is parsed and applied.
- [ ] `low_conf_subtitle` no longer contains "model" and the subtitle contract test pins the revised user-facing copy.
- [ ] Low-confidence chips no longer render a non-zero candidate as `(0%)`, and `LowConfidencePickerScreenTest` covers the degenerate case.
- [ ] Focused UI, mapper/manifest where applicable, and on-device probe tests pass.
- [ ] Stub isolation, no-networking, and the selected integration flow gate remain green.
