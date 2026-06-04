# PLANTPOTTING-0007 — Houseplant model swap (V1 entry)

PLANTPOTTING-0006 closed V0.1 by proving the calibration plumbing, and also proved the
wrong thing about the current engine: AIY Plants V1/3 is houseplant-blind for this app's
KB. It recognises only 2 of 16 KB species verbatim, and the jade probe routes
low-confidence at 0.1055 despite `Crassula ovata` being in-vocab. This sprint opens V1
by replacing that model path with an on-device, network-free classifier that is better
weighted toward common houseplants, and by landing a running app prototype using the
winning model behind a flag or branch.

## Goals

- [ ] Select a bundle-friendly, on-device, network-free replacement candidate that improves
      KB species vocabulary coverage and real-photo top-1/top-3 behaviour over the AIY
      Plants V1/3 baseline.
- [ ] Expand the real-photo fixture set beyond `monstera-deliciosa.jpg` and
      `crassula-ovata.jpg` to include at least `dracaena-trifasciata` (snake plant),
      `epipremnum-aureum` (pothos), `zamioculcas-zamiifolia` (ZZ plant), and
      `spathiphyllum-wallisii` (peace lily), with provenance recorded in
      `app/src/androidTest/assets/identify-fixtures/LICENSE.txt`.
- [ ] Build a repeatable swap-evaluation harness that runs AIY plus candidate models over
      the expanded fixture set and reports per-fixture top-1, top-3, mapped KB species,
      in-vocab status, confidence score, route, source, model size, and license notes.
- [ ] Wire the winning model into the app behind the existing
      `PlantIdentifier` / `IdentificationResult` seam by default, changing the seam only
      if the prototype proves a specific, documented need.
- [ ] Preserve the on-device/network-free contract, including a green `verifyNoNetworking`
      gate and no production dependency on PlantNet/iNaturalist/API calls.
- [ ] Re-baseline `per_species_thresholds` in the winning model's
      `model_manifest.json` and `_comment_coverage` in the winning model's
      `plant_class_map.json` from the new vocabulary and fixture results.

## Candidate Models To Evaluate

- [ ] Evaluate the current `ml/aiy_plants_v1` AIY Plants V1/3 UINT8 TFLite bundle as the
      frozen baseline; keep its known numbers in the report: Monstera top-1 0.8984
      high-confidence, jade top mapped 0.1055 low-confidence, 2/16 KB species in-vocab.
- [ ] Evaluate a `plantnet_300k_mobilenet_v3_small_int8` candidate: MobileNetV3Small (or
      EfficientNet-Lite0 if conversion is cleaner) trained or fine-tuned on PlantNet-300K
      labels, exported to TFLite INT8, with candidate facts recorded from
      `https://github.com/plantnet/PlantNet-300K` and the Zenodo/dataset metadata.
- [ ] Evaluate a `plantnet_300k_efficientnet_lite0_int8` candidate if the MobileNetV3Small
      path underperforms or cannot cover the KB vocabulary; this is the quality-vs-size
      comparator, not the default first choice.
- [ ] Evaluate an `inat_plants_tflite` candidate only if a downloadable, redistributable
      TensorFlow Lite plant classifier and labelmap can be located; record whether this is
      a true replacement option or only a reference baseline, using the iNaturalist/Google
      model and labelmap sources found during survey.
- [ ] Evaluate a `plantclef2024_mobile_distill_int8` candidate only if a public checkpoint
      can be converted to TFLite without unsupported ops and compressed to a plausible APK
      size; treat PlantCLEF as a stretch candidate because many public checkpoints are ViT
      scale and may not be bundle-friendly.
- [ ] Evaluate a `kb16_houseplant_mobilenet_v3_small_int8` fallback candidate if public
      ready-to-bundle models do not clear the bar: a 16-class KB-specific MobileNetV3Small
      head trained from licensed/open fixture data, exported to TFLite INT8, and explicitly
      reported as "project-trained" rather than an upstream public model.

## Non-Goals / Scope Boundaries

- [ ] Do not add cloud identification, API calls, remote model downloads, telemetry,
      Firebase ML, or any production networking path.
- [ ] Do not add a new user-facing model picker, settings screen, model info screen, or
      capture-failure screen.
- [ ] Do not rewrite the potting KB or change `species.json` / `archetypes.json` except for
      fixture/mapping evidence that is strictly required by the new model's vocabulary.
- [ ] Do not change potting-mix recommendation logic; success is better identification
      feeding the existing KB-driven recommendation path.
- [ ] Do not bump AGP, Kotlin, Compose, Hilt, or TFLite versions unless a selected TFLite
      candidate cannot run with the current dependency set and the bump is documented as
      a blocker-removal.
- [ ] Do not re-baseline `docs/sprints/expected-artifacts/PLANTPOTTING-0001*.txt` unless
      the winning prototype intentionally changes integration output and the sprint
      evidence explains why.
- [ ] Do not collapse the low-confidence picker; it remains the fallback for out-of-vocab,
      ambiguous, or weak predictions.
- [ ] Do not remove the AIY model until the replacement is wired, tested, and evidence-backed;
      keeping AIY as a baseline asset during the sprint is acceptable.

## Phase 1 — Candidate Survey And Decision Matrix

- [ ] Create `docs/sprints/evidence/PLANTPOTTING-0007/model-candidate-matrix.md` with rows
      for AIY baseline, PlantNet-300K MobileNetV3Small INT8, PlantNet-300K
      EfficientNet-Lite0 INT8, iNaturalist/Google TFLite plant classifier if available,
      PlantCLEF2024 mobile/distilled INT8 if available, and KB16 MobileNetV3Small INT8
      fallback.
- [ ] For each candidate, record source URL, weight availability, labels availability,
      input size, input dtype, output shape, label count, expected preprocessing,
      estimated compressed and uncompressed model size, license, redistribution notes,
      conversion risk, and whether it is already TFLite or needs conversion.
- [ ] For each candidate, compute vocabulary overlap against the 16 KB species in
      `app/src/main/assets/kb/species.json`, including accepted aliases such as
      `Sansevieria trifasciata` for `dracaena-trifasciata` and `Calathea orbifolia` for
      `goeppertia-orbifolia`.
- [ ] Record licensing as an evaluation axis, not a hard gate: include license name,
      attribution requirement, commercial-use notes, model-weight redistribution status,
      and dataset-vs-weight ambiguity.
- [ ] Pick no more than two candidates for full fixture probing after the matrix is filled;
      the default shortlist is PlantNet-300K MobileNetV3Small INT8 plus either the best
      public ready-made TFLite model or the KB16 fallback.

## Phase 2 — Expanded Fixture Set

- [ ] Audit the existing fixtures in
      `app/src/androidTest/assets/identify-fixtures/`: keep `monstera-deliciosa.jpg` and
      `crassula-ovata.jpg` unchanged so AIY baseline comparisons stay stable.
- [ ] Source a canonical real-photo fixture for `dracaena-trifasciata` showing a snake
      plant's upright banded leaves, avoiding mixed planters and cultivar-only close-ups.
- [ ] Source a canonical real-photo fixture for `epipremnum-aureum` showing pothos leaves
      and vine habit, avoiding philodendron/scindapsus ambiguity.
- [ ] Source a canonical real-photo fixture for `zamioculcas-zamiifolia` showing ZZ plant
      pinnate glossy leaflets and stems, avoiding cropped single-leaf images.
- [ ] Source a canonical real-photo fixture for `spathiphyllum-wallisii` showing peace lily
      foliage and/or spathe, avoiding generic aroid close-ups where the genus is unclear.
- [ ] Add at least two more KB species fixtures after the required four, preferring
      `ficus-lyrata`, `chlorophytum-comosum`, `phalaenopsis`, or
      `goeppertia-orbifolia` to broaden non-aroid coverage.
- [ ] Centre-crop and resize each new fixture to match the existing 480x480 JPEG fixture
      convention unless the candidate model requires a different input size that the
      preprocessor handles internally.
- [ ] Append complete provenance blocks to
      `app/src/androidTest/assets/identify-fixtures/LICENSE.txt` for every new fixture:
      title, depicts, source page, original file, author, date taken if available,
      retrieved date, modifications, license, attribution string, and test-only note.
- [ ] Add fixture-readability checks inside the evaluation harness so a missing, corrupt,
      or badly named image fails loudly before inference.

## Phase 3 — Multi-Model Evaluation Harness

- [ ] Refactor only as needed around `OnDeviceModelRealInterpreterTest` so the test can run
      the AIY baseline and one or more candidate asset directories without duplicating
      interpreter/preprocessor/mapping code.
- [ ] Add a test-only `ModelUnderTest` descriptor that names the model id, manifest asset,
      labels asset, mapping asset, model asset, and expected fixture list.
- [ ] Add candidate model asset directories under `app/src/main/assets/ml/<candidate_id>/`
      for any candidate that will be wired into the prototype, each with `model.tflite`,
      `labels.csv`, `model_manifest.json`, `plant_class_map.json`, and a license file.
- [ ] If a candidate is too large or licensing prevents checking weights into the repo,
      keep the acquisition/conversion script and evidence in
      `docs/sprints/evidence/PLANTPOTTING-0007/`, mark the candidate "not bundle-ready",
      and do not select it for the running prototype.
- [ ] Teach the harness to emit a machine-readable CSV or JSON report at
      `docs/sprints/evidence/PLANTPOTTING-0007/model-swap-eval.csv` containing model id,
      fixture species id, expected species id, raw top-1 label, raw top-1 score, raw top-3
      labels/scores, mapped top-1 KB id if any, mapped top-3 KB ids if any, in-vocab flag,
      high/low-confidence route, and failure reason if inference fails.
- [ ] Add focused assertions for the baseline AIY rows so Monstera and jade keep their
      known behaviour; this prevents the harness from hiding a regression in existing
      model plumbing.
- [ ] Add candidate assertions that are evidence-driven after the probe run: preferred form
      is correct top-1 high-confidence for required fixtures, acceptable fallback is
      correct species present in top-3 with documented threshold/mapping reason.
- [ ] Add a summary report at
      `docs/sprints/evidence/PLANTPOTTING-0007/model-swap-eval-summary.md` comparing AIY
      vs candidates on top-1 count, top-3 count, in-vocab KB coverage, average correct-class
      score for fixtures, APK asset size impact, and license status.

## Phase 4 — Winning Model Manifest And Mapping

- [ ] Create or update the winning model's
      `app/src/main/assets/ml/<winning_model>/model_manifest.json` with source URL,
      variant, sha256, placeholder=false, input size, input dtype, color order,
      output tensor shape, label count, acquisition date, license, license file,
      labels asset, mapping asset, thresholds, `per_species_thresholds`, and
      `_comment_coverage`.
- [ ] Create or update the winning model's
      `app/src/main/assets/ml/<winning_model>/plant_class_map.json` so every KB species
      that appears in the candidate vocabulary maps to the correct `kbSpeciesId`, including
      alias rows where the candidate uses older names.
- [ ] Recalculate `_comment_coverage` from the winning model's actual labels and record
      both mapping-entry overlap and KB-species overlap, e.g. "N of 18 mapping entries,
      M of 16 KB species".
- [ ] Re-baseline `per_species_thresholds` using the expanded fixture scores: seed an
      override only where the correct mapped class is near the global threshold and the
      override is defensible; do not lower thresholds to bless weak ~10% predictions.
- [ ] Update `docs/kb/ml-mapping-notes.md` with a PLANTPOTTING-0007 section describing the
      winning vocabulary, alias decisions, fixture outcomes, coverage change from AIY, and
      per-species threshold decisions.
- [ ] Update `ModelManifestTest`, `ModelAssetsPresenceTest`,
      `ModelLabelMappingValidationTest`, `PerSpeciesThresholdsContractTest`, and
      `ModelScoreMapperPerSpeciesThresholdTest` for the winning model's manifest,
      mapping, coverage, and threshold decisions.

## Phase 5 — App Wiring Prototype

- [ ] Add a single production model-selection constant or build-config flag that chooses
      the active asset root, defaulting to AIY unless the sprint branch explicitly opts into
      the winning candidate.
- [ ] Refactor `OnDeviceIdentifyProvidersModule` so `ModelManifestReader`,
      `ModelLabelsReader`, `ModelLabelMapReader`, and `TfLiteInterpreterFacade` read from
      the selected model asset root instead of hardcoding `ml/aiy_plants_v1`.
- [ ] Keep `PlantIdentifier.identify(jpeg: ByteArray): IdentificationResult` unchanged
      unless the candidate requires extra result data that cannot be represented; if it is
      changed, document the exact reason and update every caller/test in the same phase.
- [ ] Ensure `OnDevicePlantIdentifier` still returns `IdSource.ON_DEVICE_MODEL` for the
      replacement model and still routes weak/out-of-vocab predictions to the existing
      low-confidence flow.
- [ ] Add or update an instrumentation test that runs the app-wired winning model through
      `OnDevicePlantIdentifier.identify(...)` on at least Monstera, jade, snake plant,
      pothos, ZZ, and peace lily fixtures.
- [ ] Perform a live smoke run on the emulator or device using the branch/flagged winning
      model and capture evidence that the app identifies at least three houseplant
      fixtures directly from the production identifier path.

## Phase 6 — Network-Free, Size, And Integration Gates

- [ ] Run `verifyNoNetworking` after candidate dependencies and model wiring are in place;
      fix any new network surface before proceeding.
- [ ] Run `scripts/check-stub-isolation.sh` and keep the production model swap from leaking
      fake/test identifiers into app wiring.
- [ ] Measure APK or asset-size impact before and after the winning model lands; record the
      raw model size, compressed APK delta, and any `noCompress` implication in
      `model-swap-eval-summary.md`.
- [ ] Run focused JVM tests for manifest parsing, label mapping, score mapping,
      per-species thresholds, and no-network regression.
- [ ] Run the instrumented real-interpreter evaluation tests on `pixel6Api34`, including
      AIY baseline rows and winning-model rows.
- [ ] Run full `testDebugUnitTest` and `ktlintCheck`.
- [ ] Run `scripts/integration-flow.ps1` cold, warm, and buildonly; record whether expected
      artifacts remain unchanged or why a re-baseline is intentionally required.
- [ ] Write `docs/sprints/results/PLANTPOTTING-0007.md` summarising selected model,
      rejected candidates, fixture results, top-1/top-3 comparison, coverage delta,
      threshold changes, license report, size report, live-prototype status, and gate
      results.

## Sequencing

- [ ] Start with Phase 1 because no model should be downloaded, converted, or checked in
      before its license, label availability, expected size, and KB vocabulary overlap are
      known.
- [ ] Run Phase 2 in parallel with candidate conversion once the required species list is
      fixed; fixtures are reusable regardless of which candidate wins.
- [ ] Complete Phase 3 before app wiring; the prototype should be backed by comparative
      data, not by whichever model is easiest to load.
- [ ] Complete Phase 4 immediately after choosing the winner; mapping and thresholds are
      part of the model swap, not documentation cleanup.
- [ ] Complete Phase 5 only after the winning manifest and map are stable enough that the
      app path and test harness use the same assets.
- [ ] Run Phase 6 in cheap-to-expensive order: focused JVM tests, network/stub gates,
      instrumented model tests, full unit/ktlint, integration-flow transcripts, results doc.

## Risks And Mitigations

- [ ] Risk: public PlantNet or iNaturalist production weights may not be available for
      redistribution even when datasets or APIs are public. Mitigation: record license and
      weight availability separately, and keep the KB16 MobileNetV3Small INT8 fallback in
      scope for a running prototype.
- [ ] Risk: a high-accuracy PlantCLEF/ViT checkpoint may be too large or use unsupported
      TFLite ops. Mitigation: treat it as a stretch candidate, require conversion proof
      before fixture probing, and prefer MobileNetV3Small/EfficientNet-Lite for bundle fit.
- [ ] Risk: a KB16 fallback can overfit the small fixture set and look better than it is.
      Mitigation: separate training/source images from androidTest fixtures, require
      top-3 and confidence reporting, and label the result clearly as project-trained.
- [ ] Risk: candidate labels use taxonomic synonyms or common names that break direct
      mapping. Mitigation: audit labels against `species.json` aliases and document every
      alias in `plant_class_map.json` plus `docs/kb/ml-mapping-notes.md`.
- [ ] Risk: lower thresholds could hide a weak model behind optimistic routing.
      Mitigation: seed `per_species_thresholds` only from fixture evidence near the global
      threshold; keep weak predictions low-confidence and report them honestly.
- [ ] Risk: model asset size can make the APK impractical. Mitigation: measure compressed
      and uncompressed deltas before selecting the winner, prefer INT8, and reject models
      that are not bundle-friendly even if licensing is acceptable.
- [ ] Risk: app wiring accidentally hardcodes a candidate or removes the AIY baseline before
      comparison is complete. Mitigation: add a single asset-root selection point and keep
      AIY baseline tests alive through close-out.
- [ ] Risk: adding model acquisition/conversion scripts creates a hidden networking path.
      Mitigation: keep acquisition scripts outside production runtime, do not invoke them
      from Gradle app tasks, and preserve `verifyNoNetworking`.

## Acceptance Criteria

- [ ] `docs/sprints/evidence/PLANTPOTTING-0007/model-candidate-matrix.md` exists and
      reports availability, license, size, conversion risk, and 16-species vocabulary
      overlap for AIY baseline plus at least two serious replacement candidates.
- [ ] The androidTest fixture set includes Monstera and jade plus at least snake plant,
      pothos, ZZ plant, peace lily, and two additional KB species, all with complete
      provenance in `LICENSE.txt`.
- [ ] The swap-evaluation harness runs AIY and the shortlisted candidate model(s) over the
      expanded fixture set and writes a top-1/top-3/in-vocab/route/size/license comparison
      into PLANTPOTTING-0007 evidence.
- [ ] A winning replacement model is selected from evidence and has a complete asset bundle
      under `app/src/main/assets/ml/<winning_model>/` unless the sprint explicitly rejects
      all public candidates and lands the KB16 fallback bundle instead.
- [ ] The winning model is wired into the app behind a flag, branch, or single model-root
      selection point, and live identification through `OnDevicePlantIdentifier` works on
      real houseplant fixture photos without network access.
- [ ] `PlantIdentifier` / `IdentificationResult` remain unchanged, or the sprint results
      document contains a strong justification for the seam change and all callers/tests
      are updated.
- [ ] The winning model's `plant_class_map.json` and `model_manifest.json` have updated
      `_comment_coverage` and `per_species_thresholds` based on its actual vocabulary and
      fixture probes.
- [ ] `docs/kb/ml-mapping-notes.md` contains a PLANTPOTTING-0007 model-swap provenance
      section with coverage delta, fixture outcomes, threshold decisions, and license
      notes.
- [ ] `verifyNoNetworking`, `scripts/check-stub-isolation.sh`, focused model/manifest/score
      tests, GMD real-interpreter tests, full `testDebugUnitTest`, `ktlintCheck`, and
      `scripts/integration-flow.ps1` cold/warm/buildonly are green or have documented,
      accepted blockers in `docs/sprints/results/PLANTPOTTING-0007.md`.
- [ ] The sprint does not end as a paper spike: the results doc links to a running app
      prototype state where the selected model identifies plants live behind the agreed
      flag/branch.
