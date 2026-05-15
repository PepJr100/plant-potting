# PLANTPOTTING-0003 - On-device plant identifier behind the existing seam

PLANTPOTTING-0003 replaces the deterministic stub identifier with a real offline image classifier while preserving the `PlantIdentifier` seam from PLANTPOTTING-0001: `PlantIdentifier.identify(jpeg: ByteArray)` stays unchanged, `IdentificationResult.source` uses `IdSource.ON_DEVICE_MODEL` for successful model results, and `StubPlantIdentifier` remains available to tests. The sprint also closes the two known carry-forwards: Bug A in `scripts/integration-flow.ps1` and UX 2, where the result badge must be driven by the identification source instead of the literal stub copy.

---

## 1. Goals and non-goals

### 1.1 Goals

- Land a real offline model path behind `PlantIdentifier`.
- Ship all model artifacts from `app/src/main/assets/`.
- Keep `verifyNoNetworking` green.
- Keep `bash scripts/check-stub-isolation.sh` green.
- Keep `PlantIdentifier` interface shape unchanged.
- Keep `StubPlantIdentifier` referenced from unit and instrumentation tests.
- Add `IdSource.ON_DEVICE_MODEL` as the production source for high-confidence model matches.
- Replace the hard-coded result badge with source-driven copy.
- Fix Bug A so `scripts/integration-flow.ps1` retries UiAutomator dump races.
- Add enough test coverage that a future stub regression or network dependency is caught in CI.

### 1.2 Non-goals

- No cloud identification.
- No network model download at runtime.
- No Firebase, Play Services, Retrofit, OkHttp, Ktor, or ML Kit remote APIs.
- No change to `PlantIdentifier.identify(jpeg: ByteArray)`.
- No deletion of `StubPlantIdentifier`.
- No guarantee that the first on-device model is production-grade for all houseplants.
- No real-device evidence capture; emulator/GMD evidence remains the binding gate.
- No manual taxonomy expansion beyond the existing 16 KB species and 8 archetypes.
- No Room/DataStore persistence for model results.
- No release signing, Play Store metadata, or privacy-form work.

### 1.3 Model decision

Use the Google AIY Vision Plants V1 classifier from TF Hub/Kaggle as the first production model artifact:

- Model family: `google/aiy/vision/classifier/plants_V1/1`.
- Runtime: TensorFlow Lite Interpreter plus TensorFlow Lite Support image preprocessing.
- Artifacts under assets:
  - `app/src/main/assets/ml/aiy_plants_v1/model.tflite`
  - `app/src/main/assets/ml/aiy_plants_v1/labels.csv`
  - `app/src/main/assets/ml/aiy_plants_v1/kb_mapping.json`
  - `app/src/main/assets/ml/aiy_plants_v1/model_manifest.json`
- Prefer the float TFLite variation if available; use the metadata-bearing quantized variation only if the float artifact is not distributed.
- Rough APK impact: expect +8-15 MB for the quantized artifact or +25-45 MB for a float artifact, plus a small label/mapping cost. APK growth is explicitly accepted.
- Accuracy ballpark: broad iNaturalist-style plant classifiers are strong on in-distribution species but weaker on indoor cultivar photos. Expect roughly 40-65 percent top-1 and 60-80 percent top-3 on well-framed plant photos after KB mapping, with lower performance on variegated cultivars such as `Philodendron erubescens 'Pink Princess'`. This sprint measures that ballpark on a small checked-in fixture corpus instead of claiming field accuracy.

### 1.4 Why not ML Kit

- ML Kit custom image labeling still needs a custom TFLite model, so it does not solve the model-sourcing problem.
- Play Services-backed ML Kit artifacts complicate the offline/no-networking guarantee.
- TensorFlow Lite Interpreter keeps the runtime explicit, small, and auditable by `verifyNoNetworking`.

### 1.5 Low-confidence decision

Use a manual confirmation picker, not an archetype auto-fallback.

- High-confidence species match: navigate to result.
- Low-confidence or unmapped match: show an in-app picker over the existing 16 KB species.
- The picker can be prefiltered by model-suggested archetype when the top labels agree on an archetype.
- The app must not silently recommend a potting recipe for a weak match.
- Confidence threshold:
  - Accept top-1 species when `score >= 0.55` and mapped directly to a KB species.
  - Accept a direct species match when `score >= 0.45` and the margin over top-2 is at least `0.18`.
  - Otherwise treat the result as low-confidence.
  - If the top-3 mapped labels aggregate to the same archetype with total score `>= 0.60`, prefilter the manual picker to that archetype.
  - If not, show all 16 species sorted by KB display name.

---

## 2. Scope

### 2.1 Must-land

- On-device TFLite model runtime wired through Hilt.
- `IdSource.ON_DEVICE_MODEL` emitted for accepted model results.
- Model, labels, manifest, and mapping table shipped in `assets/ml/aiy_plants_v1/`.
- Mapping table from model labels to the 16-species KB and 8 archetypes.
- Source-driven result badge.
- Low-confidence manual picker.
- Bug A fix in `scripts/integration-flow.ps1`.
- Unit tests for preprocessing, mapping, thresholding, and source badge copy.
- Instrumentation tests for high-confidence model navigation and low-confidence picker navigation.
- `verifyNoNetworking` and `check-stub-isolation.sh` still green.

### 2.2 Nice-to-have

- Optional NNAPI delegate if it is available without adding network-related dependencies.
- Small checked-in fixture image set under `app/src/test/assets/ml-fixtures/`.
- A local `scripts/evaluate-model-fixtures.ps1` helper that runs fixture evaluation on the JVM or Android test path.
- A developer-facing `docs/ml/model-card-aiy-plants-v1.md`.
- A fallback build flag that can bind the stub for local debugging without changing production code paths.

### 2.3 Out-of-scope

- Fine-tuning a new checkpoint.
- Shipping multiple large model families and ensembling them.
- Real-device screenshot or video evidence.
- Expanding the KB beyond 16 species.
- Adding confidence to `IdentificationResult`.
- Adding `MANUAL` to `IdSource`.
- Cloud fallback.

---

## 3. Concrete task list

Every implementation task has a paired test task. RED-first is required where the behavior can be expressed before implementation.

### Phase 0 - Baseline and sprint setup

- [ ] **0.1 (read)** Re-read `docs/sprints/PLANTPOTTING-0001.md`, `docs/sprints/PLANTPOTTING-0002.md`, both feedback files, `PlantIdentifier.kt`, `StubPlantIdentifier.kt`, `IdentifyModule.kt`, `result/`, `docs/kb/plant-substrate-kb-notes.md`, `app/src/main/assets/`, `scripts/integration-flow.ps1`, `scripts/check-stub-isolation.sh`, and `README.md`.
- [ ] **0.2 (verify)** Run baseline `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` before edits and record the result in sprint notes.
- [ ] **0.3 (verify)** Run `bash scripts/check-stub-isolation.sh` before edits and record the result.
- [ ] **0.4 (test, RED first)** Add a narrow `PlantIdentifierContractTest` asserting the interface still exposes exactly one suspend `identify(ByteArray)` method and that `IdentificationResult` still has `speciesId`, `displayName`, and `source`.
- [ ] **0.5 (impl)** Add a short sprint note in `docs/sprints/results/PLANTPOTTING-0003.md` as the evidence sink; leave status as in-progress until acceptance.

### Phase 1 - Bug A: make the integration script retry UiAutomator races

- [ ] **1.1 (test, RED first)** Add a PowerShell-focused script test or documented harness under `scripts/tests/` that simulates `uiautomator dump` returning exit 0 while stderr contains `ERROR: null root node returned by UiTestAutomationBridge`.
- [ ] **1.2 (impl)** Refactor `Invoke-AdbDump` in `scripts/integration-flow.ps1` to capture stdout and stderr from `adb shell uiautomator dump /sdcard/ui.xml`.
- [ ] **1.3 (impl)** Make `Invoke-AdbDump` return `$false` for retryable dump failures instead of throwing.
- [ ] **1.4 (impl)** Treat the literal `null root node returned by UiTestAutomationBridge` as retryable even when `$LASTEXITCODE -eq 0`.
- [ ] **1.5 (impl)** Keep throws for hard failures: adb missing, non-retryable adb error, pull failure after a successful dump, unreadable XML file, and missing expected manifest.
- [ ] **1.6 (test, RED first)** Add a test or harness case proving `Wait-ForNode` retries after a retryable dump miss and succeeds on a later dump.
- [ ] **1.7 (impl)** Update `Wait-ForNode` to continue its retry loop when `Invoke-AdbDump` returns `$false`.
- [ ] **1.8 (impl)** Add a 2-second foreground-settle sleep after `am start` and before the first dump attempt.
- [ ] **1.9 (test)** Re-run `pwsh ./scripts/integration-flow.ps1 -BuildOnly`; expect the existing build-only diff to stay green.
- [ ] **1.10 (manual if device attached)** Run `pwsh ./scripts/integration-flow.ps1` against an emulator and record whether the device-aware diff passes. If no device is attached, record that explicitly without blocking the ML work.

### Phase 2 - Model artifacts, manifest, and dependency wiring

- [ ] **2.1 (test, RED first)** Add `ModelAssetsTest` asserting `assets/ml/aiy_plants_v1/model.tflite`, `labels.csv`, `kb_mapping.json`, and `model_manifest.json` are present in the APK asset list.
- [ ] **2.2 (impl)** Add TensorFlow Lite dependencies to `gradle/libs.versions.toml` and `app/build.gradle.kts`: `org.tensorflow:tensorflow-lite` and `org.tensorflow:tensorflow-lite-support`.
- [ ] **2.3 (impl)** Add `aaptOptions` or packaging configuration so `.tflite` is not compressed if the Android Gradle Plugin requires it for memory-mapped loading.
- [ ] **2.4 (impl)** Commit the AIY Plants V1 TFLite artifact under `app/src/main/assets/ml/aiy_plants_v1/model.tflite`.
- [ ] **2.5 (impl)** Commit the AIY Plants V1 label map under `app/src/main/assets/ml/aiy_plants_v1/labels.csv`.
- [ ] **2.6 (impl)** Commit `model_manifest.json` with model name, upstream source, license, input size, color order, normalization, output tensor shape, label count, artifact hash, and acquisition date.
- [ ] **2.7 (test, RED first)** Add `ModelManifestTest` validating the manifest hash matches the checked-in model bytes and that label count matches `labels.csv`.
- [ ] **2.8 (test)** Run `./gradlew :app:dependencies --configuration releaseRuntimeClasspath` and confirm TensorFlow Lite dependencies do not trigger `verifyNoNetworking`.
- [ ] **2.9 (verify)** Run `./gradlew verifyNoNetworking`; expect green.

### Phase 3 - Mapping table from model labels to KB species and archetypes

- [ ] **3.1 (test, RED first)** Add `ModelLabelMappingValidationTest` with invalid JSON fixtures for duplicate labels, unknown KB species ids, unknown archetype ids, empty labels, and invalid confidence boost values.
- [ ] **3.2 (impl)** Define `ModelLabelMapping` data classes in `identify/model/` using Kotlinx Serialization.
- [ ] **3.3 (impl)** Implement `ModelLabelMappingLoader` that reads `kb_mapping.json` from assets and validates it against `KnowledgeBase`.
- [ ] **3.4 (test, RED first)** Add `AiyPlantsKbMappingContentTest` asserting every direct species mapping resolves to one of the 16 KB species and every archetype mapping resolves to one of the 8 KB archetypes.
- [ ] **3.5 (impl)** Author `kb_mapping.json` with:
  - exact species rows for KB species present in AIY labels,
  - alias rows for `Sansevieria trifasciata` and `Calathea orbifolia` if present,
  - genus-level `Phalaenopsis` mapping if labels are genus or common retail equivalents,
  - archetype rows for labels that are informative but not one of the 16 species,
  - no mapping for labels that would create unsafe advice.
- [ ] **3.6 (test, RED first)** Add `ModelScoreMapperTest` for top-1 direct species, top-1 alias, top-3 archetype aggregation, unmapped labels, and tie-breaking.
- [ ] **3.7 (impl)** Implement `ModelScoreMapper` that converts raw label scores into either `AcceptedSpeciesMatch` or `LowConfidenceMatch`.
- [ ] **3.8 (impl)** Sort low-confidence manual suggestions by direct species score, then archetype prefilter, then common name.
- [ ] **3.9 (test)** Add a golden test proving all 16 KB species are reachable either by direct model mapping or by the manual picker.

### Phase 4 - On-device identifier implementation

- [ ] **4.1 (test, RED first)** Add `OnDevicePlantIdentifierPreprocessTest` for JPEG decode, center crop or fit crop, resize to model input dimensions, RGB ordering, and normalization from the manifest.
- [ ] **4.2 (impl)** Implement `ImagePreprocessor` in `identify/model/` with no Android UI dependency.
- [ ] **4.3 (test, RED first)** Add `TflitePlantClassifierTest` using a fake `InterpreterFacade` to assert input/output tensor handling and top-k sorting.
- [ ] **4.4 (impl)** Implement a small `InterpreterFacade` wrapper over TensorFlow Lite `Interpreter` so unit tests do not instantiate native TFLite.
- [ ] **4.5 (impl)** Implement `TflitePlantClassifier` that loads the model from assets, runs inference off the main thread, and returns the top 5 raw label scores.
- [ ] **4.6 (test, RED first)** Add `OnDevicePlantIdentifierTest` with fake classifier results covering accepted species, low-confidence exception, and classifier failure.
- [ ] **4.7 (impl)** Implement `OnDevicePlantIdentifier : PlantIdentifier`.
- [ ] **4.8 (impl)** On accepted match, return `IdentificationResult(speciesId, displayName, IdSource.ON_DEVICE_MODEL)`.
- [ ] **4.9 (impl)** On low-confidence match, throw `LowConfidenceIdentificationException` carrying safe manual-picker hints.
- [ ] **4.10 (impl)** On malformed image or model failure, throw a user-safe identification exception that `CameraViewModel` can render.
- [ ] **4.11 (test)** Confirm `StubPlantIdentifierTest` still exists and still references `StubPlantIdentifier` directly.

### Phase 5 - Hilt binding and build-time selector

- [ ] **5.1 (test, RED first)** Add `IdentifyModuleBindingTest` or a Hilt smoke test proving production Hilt resolves `PlantIdentifier` to `OnDevicePlantIdentifier`.
- [ ] **5.2 (impl)** Replace the production binding in `IdentifyModule` with a provider for `OnDevicePlantIdentifier`.
- [ ] **5.3 (impl)** Keep `StubPlantIdentifier` injectable only for tests and explicit debug fixtures; do not delete it.
- [ ] **5.4 (impl)** Add a test-only Hilt module replacement for instrumentation tests that still binds `PlantIdentifier` to `FakeFixedIdentifier`.
- [ ] **5.5 (test)** Run `bash scripts/check-stub-isolation.sh`; expect only files under `identify/` in production source mention `StubPlantIdentifier`.
- [ ] **5.6 (verify)** Run `./gradlew testDebugUnitTest`; expect `StubPlantIdentifierTest` and new model tests green.

### Phase 6 - Camera flow source plumbing

- [ ] **6.1 (test, RED first)** Update `CameraViewModelTest` so a successful identifier result emits both `speciesId` and `IdSource`.
- [ ] **6.2 (impl)** Replace `MutableSharedFlow<String>` navigation events with a small camera navigation event type carrying `speciesId` and `source`.
- [ ] **6.3 (impl)** Keep `CameraUiState.Success` compatible with existing UI expectations or update tests if it needs to carry source.
- [ ] **6.4 (test, RED first)** Add a test where `LowConfidenceIdentificationException` moves the ViewModel into a low-confidence state instead of navigating to result.
- [ ] **6.5 (impl)** Add `CameraUiState.LowConfidence` with manual-picker hints.
- [ ] **6.6 (test)** Update `EndToEndFlowTest` fake identifier to emit `IdSource.STUB_DETERMINISTIC` and confirm existing stub-driven test path still works.
- [ ] **6.7 (impl)** Update `CameraScreen` and `PlantPottingNavHost` to pass `source` through to the result route for successful model and stub paths.

### Phase 7 - Source-driven result badge (UX 2)

- [ ] **7.1 (test, RED first)** Add `ResultScreenBadgeTest` covering `STUB_DETERMINISTIC`, `STUB_RANDOM`, `ON_DEVICE_MODEL`, and `CLOUD`.
- [ ] **7.2 (impl)** Add source route argument support in `Routes.RESULT`, `Routes.result(speciesId, source)`, and `PlantPottingNavHost`.
- [ ] **7.3 (impl)** Decode `IdSource` in `ResultViewModel` from `SavedStateHandle`, defaulting to `STUB_DETERMINISTIC` only in old-route tests.
- [ ] **7.4 (impl)** Extend `ResultUiState` with `source: IdSource`.
- [ ] **7.5 (impl)** Replace `ResultScreenTags.STUB_BADGE` with a source-neutral `ResultScreenTags.SOURCE_BADGE`, keeping a deprecated alias in tests only if needed.
- [ ] **7.6 (impl)** Badge copy:
  - `STUB_DETERMINISTIC`: `Stub identifier - replace in a later sprint`
  - `STUB_RANDOM`: `Debug random identifier`
  - `ON_DEVICE_MODEL`: `On-device match`
  - `CLOUD`: `Cloud match unavailable in this offline build`
- [ ] **7.7 (test)** Update `ResultViewModelTest`, `ResultScreenTest`, and `EndToEndFlowTest` for the new source argument.
- [ ] **7.8 (verify)** Confirm no production screen hard-codes the old stub badge string except source-specific string resources.

### Phase 8 - Low-confidence manual picker

- [ ] **8.1 (test, RED first)** Add `ManualIdentificationViewModelTest` covering all-species list, archetype-prefiltered list, search by common name, search by scientific name, and selection event.
- [ ] **8.2 (impl)** Add `ManualIdentificationViewModel` backed by `KnowledgeBase`.
- [ ] **8.3 (test, RED first)** Add `ManualIdentificationScreenTest` asserting list rows show common name, scientific name, and archetype label without recipe details.
- [ ] **8.4 (impl)** Add `ManualIdentificationScreen` in Compose with a search field, optional archetype filter chip, and rows for the 16 KB species.
- [ ] **8.5 (impl)** When the model is unsure, show concise copy: `On-device model was unsure. Choose the closest plant.`
- [ ] **8.6 (impl)** Selecting a species navigates to the result route with the selected species id and `ON_DEVICE_MODEL` source plus a manual-confirmed route flag if needed for the badge subtitle.
- [ ] **8.7 (test, RED first)** Add instrumentation coverage for low-confidence fake identifier -> manual picker -> select `Monstera deliciosa` -> result -> recommendation.
- [ ] **8.8 (impl)** Add navigation route and arguments for the manual picker.
- [ ] **8.9 (test)** Confirm recommendation flow still works after a manual selection.

### Phase 9 - Fixture evaluation and model card

- [ ] **9.1 (test asset)** Add a small fixture manifest under `app/src/test/assets/ml-fixtures/manifest.json` with at least one local image per easily sourced KB species if licensing permits.
- [ ] **9.2 (impl)** Add `ModelFixtureEvaluationTest` that can be `@Ignore` only if fixture images are not committed; otherwise it reports top-1 and top-3 mapped accuracy.
- [ ] **9.3 (impl)** Add `docs/ml/model-card-aiy-plants-v1.md` with model source, asset hashes, input contract, label count, mapping strategy, known weaknesses, low-confidence policy, and APK size impact.
- [ ] **9.4 (test)** Add `ModelCardContentTest` or a lightweight grep check that the model card names the artifact hash and confidence thresholds.
- [ ] **9.5 (impl)** Record fixture metrics in `docs/sprints/results/PLANTPOTTING-0003.md`; do not block acceptance on a minimum accuracy threshold unless the model cannot identify any mapped species.

### Phase 10 - Offline and regression gates

- [ ] **10.1 (test)** Run `./gradlew verifyNoNetworking`; expect green.
- [ ] **10.2 (test)** Run `bash scripts/check-stub-isolation.sh`; expect green.
- [ ] **10.3 (test)** Run `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck`; expect green.
- [ ] **10.4 (test)** Run `./gradlew pixel6Api34DebugAndroidTest`; expect green.
- [ ] **10.5 (test)** Run `pwsh ./scripts/integration-flow.ps1 -BuildOnly`; expect green.
- [ ] **10.6 (test if emulator attached)** Run `pwsh ./scripts/integration-flow.ps1`; expect Bug A fixed and the device-aware diff green.
- [ ] **10.7 (impl)** Update `README.md` only if new developer commands are required for model fixture evaluation.
- [ ] **10.8 (impl)** Finalize `docs/sprints/results/PLANTPOTTING-0003.md` with command outputs, known model limitations, and any emulator availability notes.

---

## 4. Sequencing and dependency rules

```text
Phase 0 (baseline)
   |
   +--> Phase 1 (Bug A script fix) ------------------------------+
   |                                                             |
   +--> Phase 2 (model artifacts + deps)                         |
            |                                                    |
            v                                                    |
      Phase 3 (label mapping)                                    |
            |                                                    |
            v                                                    |
      Phase 4 (OnDevicePlantIdentifier)                          |
            |                                                    |
            v                                                    |
      Phase 5 (Hilt binding)                                     |
            |                                                    |
            v                                                    |
      Phase 6 (camera source plumbing)                           |
            |                                                    |
            +--> Phase 7 (source-driven badge)                   |
            |                                                    |
            +--> Phase 8 (low-confidence manual picker)          |
                       |                                         |
                       v                                         |
              Phase 9 (fixture eval + model card)                |
                       |                                         |
                       v                                         |
              Phase 10 (offline/regression gates) <--------------+
```

### 4.1 Hard gates

- Phase 2 cannot mark done until model, labels, manifest, and mapping table are in `assets/`.
- Phase 3 tests must fail before mapping validation implementation lands.
- Phase 4 must keep `PlantIdentifier.identify(jpeg: ByteArray)` unchanged.
- Phase 5 cannot remove `StubPlantIdentifier` or its tests.
- Phase 7 must land before acceptance because UX 2 is a carry-forward.
- Phase 10 cannot mark done unless `verifyNoNetworking` and `check-stub-isolation.sh` both pass.

### 4.2 Soft parallels

- Phase 1 can land in parallel with model work.
- Phase 7 source badge tests can be drafted while Phase 6 source plumbing is underway.
- Phase 9 model card can be drafted as soon as Phase 2 artifacts are known.
- Fixture image work is useful but should not block the seam replacement.

### 4.3 De-scope order

1. Drop optional NNAPI delegate.
2. Drop fixture images and leave fixture evaluation as a documented manual helper.
3. Drop model-card grep check while still writing the model card.
4. Drop device-aware `integration-flow.ps1` run only if no emulator is attached; do not drop the Bug A code fix.
5. Do not drop source badge, low-confidence picker, `verifyNoNetworking`, or stub isolation.

---

## 5. Risks and mitigations

### 5.1 Model labels do not cover all 16 KB species

Risk: AIY Plants V1 is broad and may not contain every houseplant, cultivar, or genus in the starter KB.

Mitigation: `kb_mapping.json` explicitly distinguishes direct species mappings from archetype hints. Missing species are still reachable through the manual picker. Acceptance requires all 16 species to remain selectable, not all 16 to be direct model labels.

### 5.2 False-positive plant match produces bad potting advice

Risk: A confident-looking classifier score can still be wrong on household photos, especially with cropped leaves or variegated cultivars.

Mitigation: Use conservative thresholds and route low-confidence cases to manual confirmation. Do not auto-fallback to a recipe from an unmapped or weak model label.

### 5.3 TensorFlow Lite native dependency breaks CI or APK packaging

Risk: TFLite native libraries can introduce ABI or packaging friction.

Mitigation: Keep runtime wrapper small, memory-map the model if possible, add asset and manifest tests, and run `assembleDebug` plus GMD before acceptance.

### 5.4 `verifyNoNetworking` flags transitive dependencies

Risk: A support library may pull in a forbidden networking coordinate.

Mitigation: Use only TensorFlow Lite Interpreter and Support. Avoid ML Kit and Play Services. Run `verifyNoNetworking` immediately after adding dependencies.

### 5.5 Source plumbing expands navigation churn

Risk: Passing `IdSource` through result routes touches camera, nav, ViewModel, and tests.

Mitigation: Use a single encoded route argument and update tests in one phase. Do not add a repository just to carry one enum.

### 5.6 Low-confidence exception feels like control flow

Risk: Throwing for low confidence can look like an error path.

Mitigation: Use a named domain exception, catch it explicitly in `CameraViewModel`, and treat it as a first-class UI state in tests.

### 5.7 Bug A still flakes on cold emulator launch

Risk: Even with retries, `uiautomator dump` can miss focus on slow starts.

Mitigation: Add the 2-second settle sleep, treat the null-root stderr as retryable, and keep enough retry attempts to cover cold launch. Preserve hard failure after timeout.

### 5.8 APK size grows materially

Risk: The model adds tens of MB.

Mitigation: APK size growth is unconstrained for this sprint. Record the delta in the model card and results doc.

### 5.9 Stub isolation grep catches source badge tests incorrectly

Risk: Production files outside `identify/` might mention `StubPlantIdentifier`.

Mitigation: Source badge code switches on `IdSource`, not stub class names. Tests may reference the stub; production UI must not.

---

## 6. Acceptance criteria

- [ ] `PlantIdentifier.kt` interface shape is unchanged: one `suspend fun identify(jpeg: ByteArray): IdentificationResult`.
- [ ] `IdSource.ON_DEVICE_MODEL` is emitted by the production on-device identifier for accepted model matches.
- [ ] `StubPlantIdentifier` still exists and is directly referenced from tests.
- [ ] `IdentifyModule` production binding resolves `PlantIdentifier` to the on-device implementation.
- [ ] Model artifacts live under `app/src/main/assets/ml/aiy_plants_v1/`.
- [ ] `model_manifest.json` records source, hash, input contract, label count, and confidence thresholds.
- [ ] `kb_mapping.json` validates against the existing 16-species / 8-archetype KB.
- [ ] Low-confidence model results show the manual picker instead of auto-selecting a recipe.
- [ ] The result badge is driven by `IdentificationResult.source` propagated through navigation.
- [ ] `STUB_DETERMINISTIC` still displays `Stub identifier - replace in a later sprint`.
- [ ] `ON_DEVICE_MODEL` displays `On-device match`.
- [ ] Bug A is fixed: retryable `uiautomator dump` null-root failures no longer abort `Wait-ForNode` on the first attempt.
- [ ] `pwsh ./scripts/integration-flow.ps1 -BuildOnly` still passes.
- [ ] `pwsh ./scripts/integration-flow.ps1` passes on a connected emulator if one is available; otherwise the no-device limitation is recorded.
- [ ] `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` runs green.
- [ ] `./gradlew pixel6Api34DebugAndroidTest` runs green.
- [ ] `bash scripts/check-stub-isolation.sh` runs green.
- [ ] No production runtime networking dependency is introduced.
- [ ] `EndToEndFlowTest` still passes with the fake fixed identifier.
- [ ] A new instrumentation test covers low-confidence model -> manual picker -> recommendation.
- [ ] `docs/ml/model-card-aiy-plants-v1.md` exists with model limitations and APK size impact.
- [ ] `docs/sprints/results/PLANTPOTTING-0003.md` records commands, outputs, model artifact hashes, and known accuracy limitations.
- [ ] Every completed implementation task above has a paired completed test task or a recorded reason why the test is manual-only.

---

## 7. Sprint-execute handoff

- Read the required files before editing.
- Do not relitigate the model direction unless the AIY Plants V1 artifact is unavailable or legally unusable.
- Keep the TFLite runtime offline and explicit.
- Keep the `PlantIdentifier` interface unchanged.
- Do not delete the stub.
- Prefer conservative UX over confident wrong recommendations.
- Tick tasks as they complete.
- Record every command run in `docs/sprints/results/PLANTPOTTING-0003.md`.
