# PLANTPOTTING-0007 — Codex Draft Critique Against Gemini And Claude

## Gemini Draft

### Stronger Than Codex

- Gemini's **G1 — Expanded Fixture Set** makes fixtures the first named goal and puts **Phase 1 — Expanded Fixture Set** before survey/harness work. Codex treats fixtures as Phase 2 and says they can run in parallel after the species list is fixed. Gemini's ordering is simpler for implementers: first build the ruler, then measure models.
- Gemini's **No new ML training** boundary is cleaner than Codex's fallback language. Codex includes **Evaluate a `kb16_houseplant_mobilenet_v3_small_int8` fallback candidate**, which is practical for guaranteeing a prototype, but it blurs the sprint from "model swap" into "training pipeline" unless tightly constrained.
- Gemini's **Create a new implementation of `PlantIdentifier` wrapping the new model** is more direct than Codex's **Refactor `OnDeviceIdentifyProvidersModule`** path. If the winning model has materially different preprocessing or tensor output, a parallel implementation may be a lower-risk prototype route than threading an asset root through the existing provider graph too early.
- Gemini's acceptance criteria are shorter and easier to use as a release checklist. Codex is more complete, but Gemini's criteria avoid burying the sprint's main outcome: fixtures, report, live prototype, seam preserved, manifest/map re-baselined, no networking.

### Weaker Than Codex

- Gemini's **Survey at least 2-3 candidate on-device models** is too vague. Codex names concrete candidates in **Candidate Models To Evaluate**: AIY baseline, PlantNet-300K MobileNetV3Small INT8, PlantNet-300K EfficientNet-Lite0 INT8, iNaturalist/Google TFLite, PlantCLEF mobile/distilled INT8, and KB16 fallback. Gemini gives examples but no candidate matrix shape, no required rows, and no shortlist rule.
- Gemini's **Document the evaluation results** lacks Codex's explicit output contract from **Teach the harness to emit a machine-readable CSV or JSON report** and **Add a summary report**. Without a fixed `model-swap-eval.csv` / summary path and required columns, results can become prose-only and hard to compare.
- Gemini does not carry forward the known AIY control numbers. Codex's **Evaluate the current `ml/aiy_plants_v1` AIY Plants V1/3 UINT8 TFLite bundle** and **Add focused assertions for the baseline AIY rows** pin Monstera 0.8984, jade 0.1055, and 2/16 KB coverage. Gemini's **Run the expanded fixture set against the AIY V1/3 baseline** is weaker because it does not require preserving those regression anchors.
- Gemini's **Prototype Integration** under-specifies app wiring. Codex names **OnDeviceIdentifyProvidersModule**, `ModelManifestReader`, `ModelLabelsReader`, `ModelLabelMapReader`, and `TfLiteInterpreterFacade`. Gemini says "DI graph behind a UI toggle, BuildConfig flag, or branch" but does not prohibit a user-facing picker or scattered conditional wiring.
- Gemini omits Codex's specific verification gates: **scripts/check-stub-isolation.sh**, focused JVM tests for manifest/mapping/threshold behavior, `testDebugUnitTest`, `ktlintCheck`, `scripts/integration-flow.ps1` cold/warm/buildonly, asset/APK size measurement, and a sprint results doc.

### Missing Tasks

- No task equivalent to Codex's **Create `docs/sprints/evidence/PLANTPOTTING-0007/model-candidate-matrix.md`** with source URL, weight availability, labels availability, dtype, output shape, size, license, redistribution notes, and conversion risk.
- No task equivalent to Codex's **For each candidate, compute vocabulary overlap against the 16 KB species** with accepted aliases such as `Sansevieria trifasciata` and `Calathea orbifolia`.
- No task equivalent to Codex's **Pick no more than two candidates for full fixture probing**. Gemini could waste time probing too many weak candidates or select without a recorded shortlist rationale.
- No task equivalent to Codex's **Add fixture-readability checks inside the evaluation harness**.
- No task equivalent to Codex's **Add candidate model asset directories under `app/src/main/assets/ml/<candidate_id>/`** with the complete bundle shape, nor the related rule for candidates that are too large or not redistributable.
- No task equivalent to Codex's **Update `ModelManifestTest`, `ModelAssetsPresenceTest`, `ModelLabelMappingValidationTest`, `PerSpeciesThresholdsContractTest`, and `ModelScoreMapperPerSpeciesThresholdTest`**.
- No explicit results artifact equivalent to Codex's **Write `docs/sprints/results/PLANTPOTTING-0007.md`**.

### Underweighted Risks

- Gemini underweights weight redistribution ambiguity. **R3 — Strict licensing prevents production use** says licensing is an axis, but Codex's risk separates public datasets/APIs from redistributable model weights, which is the more likely blocker for PlantNet/iNaturalist candidates.
- Gemini underweights threshold gaming. It has **G5 — Re-baselining**, but no risk equivalent to Codex's **lower thresholds could hide a weak model** and no instruction not to bless weak ~10% predictions.
- Gemini underweights APK size beyond a generic **R1 — Models are too large**. Codex requires compressed and uncompressed size deltas, `noCompress` implications, and rejecting non-bundle-friendly models even if technically accurate.
- Gemini underweights fixture ambiguity. Its fixture task requests CC photos, but Codex specifies canonical visual criteria for snake plant, pothos, ZZ plant, and peace lily, which reduces false evaluation signals.
- Gemini underweights paper-spike risk. It has **G4 — Live Prototype**, but Codex requires a live smoke run and evidence through the production identifier path.

### Wrong Sequencing

- Gemini's **Phase 1 — Expanded Fixture Set** before any survey is only partly right. Fixtures should start early, but Codex's **Phase 1 — Candidate Survey And Decision Matrix** needs to happen before downloads, conversions, or checked-in assets because license, labels, size, and redistribution can invalidate a candidate immediately.
- Gemini combines survey and harness setup in **Phase 2 — Model Survey & Harness Setup**. The survey should produce a shortlist before harness code takes on candidate-specific wrappers; otherwise the test harness may be shaped by unavailable or non-redistributable models.
- Gemini puts **Map the new model's vocabulary** and **Re-baseline the `perSpeciesThresholds`** inside **Phase 4 — Prototype Integration**. Codex's separate **Phase 4 — Winning Model Manifest And Mapping** is better: manifest, mapping, and thresholds should stabilize before app wiring so the harness and production path share the same assets.
- Gemini delays `verifyNoNetworking` to **Phase 5 — Verification & Close-out** only. Codex's sequencing runs it after candidate dependencies and wiring land, which catches accidental network surfaces earlier.

## Claude Draft

### Stronger Than Codex

- Claude grounds the sprint in existing infrastructure more explicitly than Codex: `OnDeviceModelRealInterpreterTest`, `ModelManifestReader`, `ModelScoreMapper`, `ModelLabelMapReader`, `ImagePreprocessor`, and the `app/src/main/assets/ml/<model_id>/` bundle convention. Codex references many of these later, but Claude's opening context makes implementation boundaries clearer.
- Claude's **Build the harness as a dedicated GMD test (`ModelSwapEvaluationTest`) alongside, not replacing, `OnDeviceModelRealInterpreterTest`** is stronger than Codex's **Refactor only as needed around `OnDeviceModelRealInterpreterTest`**. Keeping the old real-interpreter test as a regression anchor and adding a dedicated swap test is cleaner.
- Claude's **Add a single model-root selection point** is more concrete than Codex's **Add a single production model-selection constant or build-config flag**. It names `ACTIVE_MODEL_ROOT`, defaulting to `ml/aiy_plants_v1`, and says "One switch — no scattered conditionals."
- Claude's **Perform a live smoke run** is more measurable than Codex's equivalent: Claude requires identifying at least 3 houseplant fixtures through `OnDevicePlantIdentifier` with evidence. Codex says "at least three" too, but Claude ties it directly to the acceptance gate and `source == ON_DEVICE_MODEL`.
- Claude's **Phase 6 — Constraint, size & integration gates (cheap -> expensive)** is stronger than Codex's Phase 6 because it explicitly orders focused JVM tests, no-network/stub gates, size, instrumented tests, full suite, integration flow, results doc, and ledger flip.
- Claude adds **ModelManifestDtypeContractTest** to the winner test updates, which Codex omits. Given candidate dtype differences are likely, this is a useful addition.

### Weaker Than Codex

- Claude's **No ML training/retraining as the primary path** is stronger as a boundary, but weaker for delivery certainty than Codex's fallback treatment. Codex's KB16 fallback is in the candidate list from the start and can guarantee a running prototype if public weights fail. Claude also includes the fallback, but labels it last-resort more forcefully, which may make implementers hesitate until too late.
- Claude's **Candidate models to evaluate** uses slightly inconsistent candidate ids: `plantnet_300k_mobilenetv3s_int8`, `plantnet_300k_efflite0_int8`, and `kb16_houseplant_mvn3s_int8`. Codex's ids are longer but clearer and less abbreviated: `plantnet_300k_mobilenet_v3_small_int8`, `plantnet_300k_efficientnet_lite0_int8`, and `kb16_houseplant_mobilenet_v3_small_int8`.
- Claude's fixture task requires `file(1)` to confirm dimensions/encoding. On this Windows-heavy repo, Codex's more general "centre-crop and resize" plus harness readability check is less platform-assumptive.
- Claude says the AIY bundle stays "the default flag target" through close-out in one non-goal, while also requiring a running prototype with the winning model flipped on a prototype build/branch. Codex's wording is slightly less internally tense: defaulting to AIY unless the sprint branch explicitly opts into the winner.
- Claude's ledger-flip instruction in **Phase 6** may be premature because the draft does not name the ledger file or define the exact ledger state transition. Codex stops at the results doc and gate results, which is safer without current ledger context.

### Missing Tasks

- Claude covers most of Codex's tasks. The main missing item is Codex's explicit non-goal **Do not add a new user-facing model picker, settings screen, model info screen, or capture-failure screen** as separate named exclusions. Claude has **No net-new screens**, which is equivalent but less exhaustive in task-level wording.
- Claude does not include Codex's explicit **Do not re-baseline `docs/sprints/expected-artifacts/PLANTPOTTING-0001*.txt`** path pattern in acceptance criteria, though it does mention no expected-artifacts re-baselining in non-goals.
- Claude does not phrase licensing as Codex's **Record licensing as an evaluation axis, not a hard gate** in both Phase 1 and Non-goals with commercial-use notes, model-weight redistribution status, and dataset-vs-weight ambiguity. Claude has the substance, but Codex is slightly more repetitive in the right places.
- Claude does not explicitly say **Run full `testDebugUnitTest` and `ktlintCheck`** until Phase 6; Codex also lists them in acceptance criteria. Claude's acceptance criteria includes them, so this is minor.

### Underweighted Risks

- Claude underweights the operational cost of KB16 fallback training. It identifies **R3 — KB16 fallback overfits**, but it does not call out that creating a licensed/open training set, keeping it disjoint from androidTest fixtures, exporting INT8, and documenting provenance can exceed the sprint budget.
- Claude underweights model acquisition fragility for PlantNet-300K. It names GitHub and Zenodo, but the risk section focuses on redistribution, not the possibility that published weights/checkpoints may not match a mobile architecture, may require nontrivial training code, or may not convert cleanly despite being nominally available.
- Claude underweights Windows/tooling drift in fixture processing and conversion. The `file(1)` instruction and likely conversion scripts may be brittle in the current PowerShell/Windows environment.
- Claude underweights the risk that fixture count is still too small for confidence calibration. It says not to bless ~10% predictions, but six to eight fixtures are still weak evidence for per-species thresholds; Codex's phrasing is also imperfect here, but Claude's acceptance criterion that thresholds are "re-baselined" could sound stronger than the data supports.

### Wrong Sequencing

- Claude's sequencing is mostly right and generally stronger than Codex's. The one concern is that **Phase 2 runs in parallel with candidate conversion** may start conversion before the matrix has fully proven redistribution and label availability. Claude also says Phase 1 gates model-side work, so the diagram needs the stricter text to win over the parallel-work suggestion.
- Claude's **Phase 4 — Winning-model manifest & mapping** before **Phase 5 — Running-prototype wiring** is correct, but the task **Author `model_manifest.json`** includes `sha256`, thresholds, labels, mapping, and license before the live prototype has proven the model works through the production path. A draft implementation may need provisional manifest values before final threshold decisions are locked.
- Claude's **Phase 6** puts asset/APK size after `verifyNoNetworking` and `check-stub-isolation.sh`. Size should be measured before final winner selection as Codex says in **Measure APK or asset-size impact before and after the winning model lands**, because size can disqualify an otherwise winning model.

## If I Were Merging

- I would keep Codex's concrete candidate matrix, named evaluation outputs, explicit model asset bundle shape, and exhaustive verification/result artifacts.
- I would keep Gemini's simple fixture-first emphasis and its concise acceptance checklist, but not its vague candidate survey or missing gate detail.
- I would keep Claude's existing-infra grounding, dedicated `ModelSwapEvaluationTest`, `ACTIVE_MODEL_ROOT` single-switch wiring, `ModelManifestDtypeContractTest`, cheap-to-expensive gate ordering, and measurable live smoke-run acceptance.
