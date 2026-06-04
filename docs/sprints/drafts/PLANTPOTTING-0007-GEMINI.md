# PLANTPOTTING-0007 — Houseplant model swap (V1 entry)

Replace the houseplant-blind AIY Plants V1/3 TFLite model with a houseplant-weighted classifier. This sprint surveys candidate on-device models, builds a swap-evaluation harness with an expanded real-photo fixture set, compares performance against the AIY baseline, and lands a running prototype of the winning model wired into the app.

## Goals

- **G1 — Expanded Fixture Set:** Expand the real-photo testing fixtures to include more of the 16 KB species (Monstera and Jade, plus Snake plant, Pothos, ZZ plant, Peace lily, etc.).
- **G2 — Model Survey & Harness:** Survey candidate on-device, network-free classifiers with houseplant-weighted vocabularies. Build an evaluation harness to probe the candidates against the expanded fixture set.
- **G3 — Baseline vs Candidate Comparison:** Compare top-1/top-3 accuracy, in-vocab coverage, bundle size, and licensing of candidates against the AIY V1/3 baseline.
- **G4 — Live Prototype:** Land a running prototype of the winning model wired into the app identifying plants live (behind a flag, toggle, or dedicated branch).
- **G5 — Re-baselining:** Update `plant_class_map.json` (`_comment_coverage`) and `model_manifest.json` (`perSpeciesThresholds`) against the new model's vocabulary and confidence distributions.

## Scope boundaries (Non-goals)

- **Network-free constraint is absolute:** No cloud APIs or network-dependent models. `verifyNoNetworking` must remain green.
- **No new ML training:** We are finding, evaluating, and integrating pre-trained models, not training or fine-tuning our own.
- **No unnecessary architectural rewrites:** The `PlantIdentifier` / `IdentificationResult` interface seam (0003 §4.4) is frozen by default. Changes are allowed only with strong justification (e.g. required for TFLite I/O tensor differences).
- **Licensing is reported, not blocking:** Licensing is an evaluation axis to report. A restrictive license does not block the prototype spike.
- **KB locked:** No changes to `species.json` or `archetypes.json` beyond mapping file updates.

## Task list

### Phase 1 — Expanded Fixture Set
- [ ] Source real-photo, CC-licensed/CC0 fixtures for at least 4 additional KB species (e.g., Snake plant `dracaena-trifasciata`, Pothos `epipremnum-aureum`, ZZ plant `zamioculcas-zamiifolia`, Peace lily `spathiphyllum-wallisii`).
- [ ] Uniformly crop, scale, and compress the new fixtures (matching existing 480x480 JPEG q80 baseline) and place them in `app/src/androidTest/assets/identify-fixtures/`.
- [ ] Append full provenance blocks for every new fixture to `app/src/androidTest/assets/identify-fixtures/LICENSE.txt`, mirroring the Monstera/Jade format.

### Phase 2 — Model Survey & Harness Setup
- [ ] Survey at least 2-3 candidate on-device models with houseplant-weighted vocabularies (e.g. MobileNet variants trained on iNaturalist plant subsets, PlantNet-style TFLite exports, or similar).
- [ ] Extend the existing `OnDeviceModelRealInterpreterTest` (or create a dedicated `ModelSwapEvaluationTest`) to iterate over the expanded fixture set.
- [ ] Build test-only wrappers for the candidate `.tflite` models to extract their top-1/top-3 IDs and scores without wiring them into the production app yet.

### Phase 3 — Baseline & Candidate Evaluation
- [ ] Run the expanded fixture set against the AIY V1/3 baseline to establish control numbers (top-1/top-3 scores, route decisions).
- [ ] Run the same expanded fixture set against the candidate models.
- [ ] Document the evaluation results (accuracy, in-vocab coverage of the 16 KB species, model file size, and license terms) and select a winning model.

### Phase 4 — Prototype Integration
- [ ] Import the winning `.tflite` model (and its label map) into `app/src/main/assets/ml/`.
- [ ] Create a new implementation of `PlantIdentifier` wrapping the new model. Adapt to its specific tensor inputs/outputs while preserving the existing `PlantIdentifier` interface seam (modify the interface only if strongly justified).
- [ ] Wire the new `PlantIdentifier` into the DI graph behind a UI toggle, `BuildConfig` flag, or on a dedicated prototype branch, ensuring it can process live camera frames.
- [ ] Map the new model's vocabulary to `plant_class_map.json` and update `_comment_coverage` to reflect the new in-vocab count.
- [ ] Re-baseline the `perSpeciesThresholds` in `model_manifest.json` based on the new model's confidence baseline.

### Phase 5 — Verification & Close-out
- [ ] Run the `verifyNoNetworking` check to guarantee the network-free boundary is unbreached.
- [ ] Confirm all instrumentation tests pass with the new harness and model.
- [ ] Record the final chosen model's licensing, size, and integration details in `docs/kb/ml-mapping-notes.md`.

## Sequencing & dependencies

1. **Phase 1 (Fixtures):** Must precede evaluation. The expanded test set is the ruler we measure candidates against.
2. **Phase 2 & 3 (Survey & Evaluate):** The core spike. Gather data and make a choice.
3. **Phase 4 (Live Prototype):** Blocked on Phase 3's model selection. Wires the actual model into the app to prove it works outside the test harness.
4. **Phase 5 (Verification):** Final security (`verifyNoNetworking`) and regression checks.

## Risks & mitigations

- **R1 — Models are too large for the app bundle.** *Mitigation:* Explicitly record model size in the Phase 3 evaluation. If size is prohibitive, evaluate quantization or document the bundle-size vs accuracy trade-off.
- **R2 — TFLite input/output shape mismatch breaks the seam.** *Mitigation:* Confine tensor preprocessing/postprocessing inside the new `PlantIdentifier` implementation. Only break the interface if the new model fundamentally changes the async or multi-crop nature of the inference.
- **R3 — Strict licensing prevents production use.** *Mitigation:* Licensing is an evaluation axis, not a hard gate. Prototype with the best technical model to prove the concept, but clearly document the license constraint for a future business decision.
- **R4 — Candidate models still perform poorly on our 16 KB species.** *Mitigation:* Expanding the fixture set to 6+ species ensures we don't accidentally overfit our model choice to just Monstera and Jade.

## Acceptance criteria

- [ ] `identify-fixtures/` contains at least 6 canonical plant photos (Monstera, Jade + 4 new) with complete CC-license provenance in `LICENSE.txt`.
- [ ] An evaluation report is recorded comparing the AIY V1/3 baseline against candidate models on top-1/top-3 accuracy, KB species coverage, size, and licensing.
- [ ] A live prototype runs on-device (via flag or branch) identifying plants with the winning model.
- [ ] `PlantIdentifier` / `IdentificationResult` interfaces remain unchanged, or changes are heavily documented and justified.
- [ ] `plant_class_map.json` (`_comment_coverage`) and `model_manifest.json` (`perSpeciesThresholds`) are updated for the new model.
- [ ] `verifyNoNetworking` script remains GREEN.
