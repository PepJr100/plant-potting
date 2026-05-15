# PLANTPOTTING-0004 - Fix sprint for PLANTPOTTING-0003 device review

**Status:** draft  
**Sprint shape:** narrow fix sprint, same pattern as PLANTPOTTING-0002 -> PLANTPOTTING-0001  
**Primary source:** `docs/sprints/feedback/PLANTPOTTING-0003/feedback.md`  
**Refined intent:** `docs/sprints/drafts/PLANTPOTTING-0004-INTENT.md`

---

## 1. Intent

PLANTPOTTING-0004 exists to make the PLANTPOTTING-0003 on-device identifier actually work on the same Pixel 6 API 34 device path that exposed the review failures. This is not a feature sprint. It fixes two production bugs, closes the test gap that let the model/preprocessor mismatch ship, and captures the cold/warm device-aware integration transcripts that PLANTPOTTING-0003 could not honestly claim.

The must-land outcomes are:

- [ ] Fix the production TFLite input contract mismatch: the shipped AIY Plants V1/3 model at `app/src/main/assets/ml/aiy_plants_v1/model.tflite` is UINT8, while `ImagePreprocessor.kt:48` currently creates `TensorImage(DataType.FLOAT32)` and `InterpreterFacade.kt:65-68` passes a FLOAT32-shaped object to `Interpreter.run`.
- [ ] Fix the Compose-to-uiautomator bridge: `MainActivity.kt:19-23` currently installs `PlantPottingNavHost()` without `testTagsAsResourceId`, so `scripts/integration-flow.ps1:135` cannot find `resource-id="camera.shutter"`.
- [ ] Add a gated real-model instrumentation test that runs a checked-in JPEG through the production `PlantIdentifier` binding and the real `Interpreter.run`, not through `FakeFixedIdentifier`.
- [ ] Capture `docs/sprints/evidence/PLANTPOTTING-0004/transcript-A-cold.txt` and `docs/sprints/evidence/PLANTPOTTING-0004/transcript-B-warm.txt` from `pwsh ./scripts/integration-flow.ps1` with a clean device-aware manifest diff.
- [ ] Re-run `pixel6Api34DebugAndroidTest` after the fixes and record the result in the PLANTPOTTING-0004 results doc.

Hard seam constraints from PLANTPOTTING-0003 still apply:

- [ ] Keep `PlantIdentifier.kt:3-5` byte-for-byte unchanged: `interface PlantIdentifier { suspend fun identify(jpeg: ByteArray): IdentificationResult }`.
- [ ] Keep the `IdentificationResult` schema at `PlantIdentifier.kt:7-12` unchanged.
- [ ] Keep `StubPlantIdentifier.kt` under `app/src/main/java/com/darkfactory/plantpotting/identify/`.
- [ ] Keep `bash scripts/check-stub-isolation.sh` green.
- [ ] Keep `./gradlew verifyNoNetworking` green.

---

## 2. Goals And Non-Goals

### 2.1 Must-Land Goals

- [ ] A real capture no longer surfaces the review error from `diag-step3-after-shutter.xml:1`: `Cannot convert between a TensorFlowLite tensor with type UINT8 and a Java object of type [[[[F`.
- [ ] `model_manifest.json` declares the model input dtype explicitly as `"input_dtype": "uint8"`.
- [ ] UINT8 preprocessing feeds resized raw 0-255 bytes to TFLite with no `NormalizeOp`.
- [ ] The existing FLOAT32 preprocessing path remains test-covered for a future model swap.
- [ ] `MainActivity` exposes Compose `testTag(...)` values as uiautomator `resource-id` values across the whole navigation tree, including `camera.shutter`, `result.sourceBadge`, `result.seePottingMix`, `recommendation.archetypeName`, and `recommendation.recipeRow`.
- [ ] The instrumentation test suite contains one real-model smoke test that proves the production `PlantIdentifier` binding is `OnDevicePlantIdentifier` and `identify(realJpegBytes)` returns without throwing with `source == IdSource.ON_DEVICE_MODEL`.
- [ ] `scripts/integration-flow.ps1` reaches the recommendation screen in default device-aware mode and produces a manifest containing `source-badge=on-device match`.

### 2.2 Explicit Non-Goals

These are deferred to PLANTPOTTING-0005 or later:

- Confidence calibration, per-class threshold tuning, or changing the PLANTPOTTING-0003 confidence table.
- UI polish for `CameraUiState.Failure`, including Snackbar/banner/dedicated `CaptureFailedScreen`.
- Any model swap. The model is already the INT8/UINT8 AIY Plants V1/3 variant; this sprint handles it correctly.
- GPU or NNAPI delegates.
- Re-enabling `PermissionDeniedFlowTest`; it remains governed by the PLANTPOTTING-0001 deferral.
- New `IdSource` values.
- New screens, new navigation routes, or new KB content.
- `LowConfidenceFlowTest` instrumentation. This remains a PLANTPOTTING-0005 candidate unless all must-land work is green early.
- Changes to `PlantIdentifier`, `IdentificationResult`, or the low-confidence candidate side channel.
- AGP, Kotlin, Compose, Hilt, CameraX, TFLite, or TFLite Support version bumps.

### 2.3 Nice-To-Have Only

- [ ] If the must-land chain is green early, add a single note to `docs/sprints/results/PLANTPOTTING-0004.md` about the current `CameraUiState.Failure` visibility issue from `feedback.md` "UX issues"; do not implement UI polish.
- [ ] If the real-model smoke test is stable and cheap, also record its elapsed runtime in the results doc; do not add production telemetry.

---

## 3. Decisions

### 3.1 Manifest-Driven Input Dtype

Decision: use manifest-driven dtype dispatch, not runtime-only interpreter introspection.

- [ ] Add `input_dtype` to `app/src/main/assets/ml/aiy_plants_v1/model_manifest.json`, with allowed values `"uint8"` and `"float32"`.
- [ ] Parse the field into `ModelManifest` in `ModelManifest.kt:22-34`.
- [ ] Make `ImagePreprocessor` branch from the manifest: UINT8 uses `TensorImage(DataType.UINT8)` and no `NormalizeOp`; FLOAT32 keeps `TensorImage(DataType.FLOAT32)` plus `NormalizeOp`.
- [ ] Add a gated instrumentation assertion that the manifest dtype matches `Interpreter.getInputTensor(0).dataType()` for the shipped model.

Rejected alternative: runtime-introspect the interpreter tensor dtype and have preprocessing follow that value directly. That would couple preprocessing construction to native interpreter load, making the currently clean `ImagePreprocessor` unit-test seam heavier. The safer shape is declarative manifest config plus a real-interpreter contract test so future asset swaps fail CI if the manifest lies.

### 3.2 Root Box For `testTagsAsResourceId`

Decision: wrap the root nav host in `MainActivity` with a `Box(modifier = Modifier.semantics { testTagsAsResourceId = true })`.

- [ ] Put the `@OptIn(ExperimentalComposeUiApi::class)` surface at the `MainActivity` call site only.
- [ ] Use a root `Box`, not per-screen semantics wrappers.
- [ ] Remove the now-redundant local `testTagsAsResourceId` wrapper from `RecommendationScreen.kt:22-27`, `RecommendationScreen.kt:33`, and `RecommendationScreen.kt:46-50` after the root bridge is green.

Rejected alternative: continue adding one-off `semantics { testTagsAsResourceId = true }` wrappers to screens. That already failed for `CameraScreen` and `ResultScreen`. The script consumes navigation-wide resource ids, so the bridge belongs at the root.

Rejected alternative: `CompositionLocalProvider`. `testTagsAsResourceId` is a semantics property applied through a `Modifier`, not a composition local in this codebase.

### 3.3 Real-Model Instrumentation Test Lives In `androidTest`

Decision: keep the real-model smoke test in `app/src/androidTest`, but remove the global fake identifier replacement first.

Current blocker: `app/src/androidTest/java/com/darkfactory/plantpotting/identify/TestIdentifyModule.kt:15-23` globally replaces `OnDeviceIdentifyModule` with `FakeFixedIdentifier`. Any real-model `@HiltAndroidTest` in the same source set would silently use the fake.

- [ ] Delete the global `TestIdentifyModule.kt` replacement.
- [ ] Convert existing instrumentation tests that need determinism to local `@BindValue` fields: `@BindValue @JvmField val plantIdentifier: PlantIdentifier = FakeFixedIdentifier()`.
- [ ] Add the real-model smoke test in `app/src/androidTest/java/com/darkfactory/plantpotting/identify/RealModelPlantIdentifierSmokeTest.kt` with no `@BindValue` override.

Rejected alternative: create a new gated source set or flavor only for real-model tests. That avoids the global fake, but it adds Gradle variant surface and a second GMD task to a fix sprint. The lower-risk change is to make fake bindings local to the tests that need them, then the normal `pixel6Api34DebugAndroidTest` chain covers both fake-flow instrumentation and the one real-model smoke test.

### 3.4 Internal Preprocessed Image Shape

Decision: change only the internal model pipeline representation, not the public identifier seam.

- [ ] Extend `PreprocessedImage.kt:10-14` to carry `inputDType` plus either UINT8 bytes or FLOAT32 values.
- [ ] Keep `FakeInterpreterFacade` compatible by recording the full `PreprocessedImage` and returning canned scores regardless of dtype.
- [ ] Keep `PlantIdentifier.kt` unchanged.

Rejected alternative: add dtype or tensor details to `PlantIdentifier.identify(...)` or `IdentificationResult`. That would leak model implementation details through the public app seam and violates PLANTPOTTING-0003 invariants.

---

## 4. Task List

TDD ordering is mandatory. Tasks marked `RED first` must be committed or otherwise observed failing before their paired implementation task starts.

### Phase 0 - Baseline And Contract Locks

- [ ] **0.1 Baseline:** run `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` and `bash scripts/check-stub-isolation.sh` before edits; record outputs in `docs/sprints/results/PLANTPOTTING-0004.md`.
- [ ] **0.2 Baseline GMD if available:** run `./gradlew --no-daemon pixel6Api34DebugAndroidTest`; record whether it is green with the current fake-bound instrumentation path.
- [ ] **0.3 Contract lock:** run existing `PlantIdentifierContractTest` from `app/src/test/java/com/darkfactory/plantpotting/identify/PlantIdentifierContractTest.kt:17-91`; any failure blocks the sprint.
- [ ] **0.4 RED first - manifest dtype schema:** add `ModelManifestTest.manifestDeclaresSupportedInputDtype` in `app/src/test/java/com/darkfactory/plantpotting/identify/ModelManifestTest.kt` near lines 43-60; expect RED because `model_manifest.json:1-28` has no `input_dtype`.
- [ ] **0.5 RED first - UINT8 manifest value:** add `ModelManifestTest.bundledAiyV13ManifestDeclaresUint8InputDtype`; expect RED until the manifest declares `"input_dtype": "uint8"`.
- [ ] **0.6 RED first - no UINT8 normalization:** add `ModelManifestTest.uint8ManifestDoesNotDeclareNormalization`; expect RED while `model_manifest.json:9-12` still contains the FP normalization stanza.
- [ ] **0.7 RED first - preprocessor UINT8 branch:** add `ImagePreprocessorTest.uint8ManifestProducesRawByteInputWithoutNormalisation` in `app/src/test/java/com/darkfactory/plantpotting/identify/model/ImagePreprocessorTest.kt`; expect RED because `ImagePreprocessor.kt:48-51` always emits FLOAT32 normalized values.
- [ ] **0.8 RED first - preprocessor FLOAT32 branch retained:** add `ImagePreprocessorTest.float32ManifestKeepsNormalisedFloatPath` using a hand-built `ModelManifest`; expect RED until `ImagePreprocessor` supports both dtype branches.
- [ ] **0.9 RED first - uiautomator resource id bridge:** add `MainActivityResourceIdBridgeTest.cameraShutterTestTagIsExportedAsResourceId` under `app/src/androidTest/java/com/darkfactory/plantpotting/`; use `UiDevice.dumpWindowHierarchy(...)` and assert the XML contains `resource-id="camera.shutter"`; expect RED on current `MainActivity.kt:19-23`.
- [ ] **0.10 RED first - real model production binding:** add `RealModelPlantIdentifierSmokeTest.productionPlantIdentifierBindingIsOnDevicePlantIdentifier` under `app/src/androidTest/java/com/darkfactory/plantpotting/identify/`; expect RED while `TestIdentifyModule.kt:15-23` globally replaces the binding.
- [ ] **0.11 RED first - real model inference:** add `RealModelPlantIdentifierSmokeTest.realModelIdentifyReturnsOnDeviceSourceWithoutThrowing`; use a checked-in fixture JPEG and assert `source == IdSource.ON_DEVICE_MODEL`; expect RED after the fake-binding issue is removed until the UINT8 pipeline fix lands.
- [ ] **0.12 RED first - manifest versus interpreter dtype:** add `RealModelPlantIdentifierSmokeTest.manifestInputDtypeMatchesInterpreterInputTensor`; load the real TFLite interpreter from `assets/ml/aiy_plants_v1/model.tflite` and assert input tensor 0 is UINT8; expect RED until manifest parsing exposes `inputDType`.

### Phase 1 - Compose Resource-Id Bridge

- [ ] **1.1 Implement root bridge:** update `app/src/main/java/com/darkfactory/plantpotting/MainActivity.kt:19-23` to wrap `PlantPottingNavHost()` in `Box(modifier = Modifier.semantics { testTagsAsResourceId = true })`.
- [ ] **1.2 Limit opt-in surface:** add `@OptIn(ExperimentalComposeUiApi::class)` at the smallest `MainActivity` call site needed for `testTagsAsResourceId`.
- [ ] **1.3 Clean redundant screen-local bridge:** remove `ExperimentalComposeUiApi`, `semantics`, and `testTagsAsResourceId` imports/usages from `RecommendationScreen.kt:22-27`, `RecommendationScreen.kt:33`, and `RecommendationScreen.kt:46-50` once `MainActivityResourceIdBridgeTest` is green.
- [ ] **1.4 Verify bridge test:** run `./gradlew --no-daemon pixel6Api34DebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.darkfactory.plantpotting.MainActivityResourceIdBridgeTest`; expect the `camera.shutter` resource-id assertion green.
- [ ] **1.5 Accessibility sanity check:** in the same uiautomator dump, assert or manually record that `camera.shutter` appears only as `resource-id`, while the shutter content description remains `Capture plant photo`; this mitigates accidental tag-string leakage into spoken labels.

### Phase 2 - Manifest, Preprocessor, And Facade Input Contract

- [ ] **2.1 Update manifest:** edit `app/src/main/assets/ml/aiy_plants_v1/model_manifest.json` to add `"input_dtype": "uint8"` near `input_size` / `color_order`.
- [ ] **2.2 Drop dead UINT8 normalization stanza:** remove `model_manifest.json:9-12` normalization for the UINT8 model, or replace it with a comment field that explicitly says normalization is unused for UINT8; prefer removal.
- [ ] **2.3 Update manifest comments:** revise `model_manifest.json:2` so it states the input tensor is UINT8 and expects raw 0-255 bytes after resize.
- [ ] **2.4 Parse dtype:** update `ModelManifest.kt:22-34` with a small enum, for example `enum class InputDType { UINT8, FLOAT32 }`, and add `val inputDType: InputDType`.
- [ ] **2.5 Make normalization optional:** update `ModelManifest.Normalization` and `ModelManifestReader.read()` at `ModelManifest.kt:75-88` so FLOAT32 manifests can carry normalization and UINT8 manifests are valid without it.
- [ ] **2.6 Reject bad dtype values:** add reader validation that throws a useful error for any `input_dtype` outside `uint8` / `float32`; cover it with a JVM test.
- [ ] **2.7 Update `ModelManifestTest.manifestIsValidJson`:** replace the current required `"normalization"` key at `ModelManifestTest.kt:46-59` with required `"input_dtype"` and conditional normalization checks.
- [ ] **2.8 Update `PreprocessedImage`:** change `app/src/main/java/com/darkfactory/plantpotting/identify/model/PreprocessedImage.kt:10-14` so it can represent UINT8 bytes and FLOAT32 values without ambiguous field names like `normalisedRgb` for raw bytes.
- [ ] **2.9 Preserve fake compatibility:** update `InterpreterFacadeTest.image()` at `InterpreterFacadeTest.kt:12-17` and `FakeInterpreterFacade.runInference(...)` at `InterpreterFacade.kt:123-126` so fake-based tests keep recording input for both dtype branches.
- [ ] **2.10 Branch `ImagePreprocessor`:** update `ImagePreprocessor.kt:30-35` and `ImagePreprocessor.kt:48-55` so UINT8 builds `TensorImage(DataType.UINT8)`, applies resize only, and returns raw byte input.
- [ ] **2.11 Keep FLOAT32 branch covered:** keep the current NormalizeOp behavior for hand-built FLOAT32 manifests; `whitePixelNormalisesToOne` and `midGreyPixelNormalisesToZero` should move to the FLOAT32-specific branch rather than asserting against the shipped UINT8 manifest.
- [ ] **2.12 Branch `TfLiteInterpreterFacade.runInference`:** update `InterpreterFacade.kt:61-75` so UINT8 inputs are passed to `Interpreter.run` as a direct `ByteBuffer` or other TFLite-compatible UINT8 object, while FLOAT32 inputs keep a compatible float path.
- [ ] **2.13 Rename misleading docs:** update KDoc in `ImagePreprocessor.kt:13-18` and `PreprocessedImage.kt:3-8` so it no longer claims every tensor is normalized FLOAT32.
- [ ] **2.14 Run focused unit tests:** run `./gradlew --no-daemon testDebugUnitTest --tests '*ModelManifestTest' --tests '*ImagePreprocessorTest' --tests '*InterpreterFacadeTest'`; expect green.

### Phase 3 - Real-Model Instrumentation Without Global Fake Binding

- [ ] **3.1 Delete global fake identifier replacement:** remove `app/src/androidTest/java/com/darkfactory/plantpotting/identify/TestIdentifyModule.kt:15-23`.
- [ ] **3.2 Add local fake binding to `EndToEndFlowTest`:** add `@BindValue @JvmField val plantIdentifier: PlantIdentifier = FakeFixedIdentifier()` to `EndToEndFlowTest.kt:19-35`.
- [ ] **3.3 Add local fake binding to camera instrumentation tests:** add the same local binding to `CameraScreenSmokeTest.kt`, `CameraScreenBindStateTest.kt`, and `CameraPreviewLayoutTest.kt`.
- [ ] **3.4 Add local fake binding to permission instrumentation tests that launch `MainActivity`:** add the same local binding to `PermissionResumeRecoveryTest.kt` and `PermissionDeniedFlowTest.kt` if they construct the full activity graph.
- [ ] **3.5 Keep `FakeFixedIdentifier` test-only:** leave `FakeFixedIdentifier.kt:12-24` in `app/src/androidTest/.../identify/`; do not move it into production.
- [ ] **3.6 Add fixture JPEG:** add a small valid checked-in JPEG at `app/src/androidTest/assets/fixtures/real-model-smoke.jpg`; use instrumentation test context assets to read it.
- [ ] **3.7 Implement production binding assertion:** in `RealModelPlantIdentifierSmokeTest`, inject `PlantIdentifier` and assert `identifier` is `OnDevicePlantIdentifier`, not `FakeFixedIdentifier`.
- [ ] **3.8 Implement real inference assertion:** in `RealModelPlantIdentifierSmokeTest`, call `identifier.identify(realJpegBytes)` and assert it returns without `IdentificationFailureException` and with `result.source == IdSource.ON_DEVICE_MODEL`.
- [ ] **3.9 Implement interpreter dtype assertion:** in `RealModelPlantIdentifierSmokeTest`, load `model.tflite` through `AssetManager.openFd`, create an `Interpreter`, and assert `getInputTensor(0).dataType()` maps to manifest `inputDType`.
- [ ] **3.10 Run focused real-model test:** run `./gradlew --no-daemon pixel6Api34DebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.darkfactory.plantpotting.identify.RealModelPlantIdentifierSmokeTest`; expect green on the AOSP Pixel 6 API 34 image.
- [ ] **3.11 Run existing fake-flow instrumentation tests:** run `./gradlew --no-daemon pixel6Api34DebugAndroidTest` and confirm `EndToEndFlowTest.kt:37-70` still reaches the recommendation screen with the local fake binding.

### Phase 4 - Integration Flow Evidence

- [ ] **4.1 Create evidence directory:** create `docs/sprints/evidence/PLANTPOTTING-0004/`.
- [ ] **4.2 Run cold device-aware transcript:** from a clean emulator boot / cold app launch, run `pwsh ./scripts/integration-flow.ps1` and save complete terminal output to `docs/sprints/evidence/PLANTPOTTING-0004/transcript-A-cold.txt`.
- [ ] **4.3 Verify cold manifest:** confirm `artifacts/PLANTPOTTING-0001/manifest.txt` includes `manifest-mode=device-aware`, `device-screenshot-count-at-least-1=true`, `source-badge=on-device match`, `archetype-name=aroid chunky`, and `recipe-row-count=5`.
- [ ] **4.4 Run warm device-aware transcript:** force-stop the app but keep the emulator warm, re-run `pwsh ./scripts/integration-flow.ps1`, and save output to `docs/sprints/evidence/PLANTPOTTING-0004/transcript-B-warm.txt`.
- [ ] **4.5 Verify warm manifest:** confirm the warm run has the same clean expected-manifest diff as cold.
- [ ] **4.6 Run build-only regression:** run `pwsh ./scripts/integration-flow.ps1 -BuildOnly` and save output to `docs/sprints/evidence/PLANTPOTTING-0004/transcript-C-buildonly.txt`.
- [ ] **4.7 Update expected artifact only if observed output changed:** if the fixed real model routes the device-aware run to a different stable archetype/row count than `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt:30-31`, update that file with the observed deterministic values and explain the reason in the results doc.
- [ ] **4.8 Do not weaken expected artifact requirements:** keep `source-badge=on-device match` from `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt:32`; do not remove device-aware lines to make the script pass.

### Phase 5 - Final Verification And Documentation

- [ ] **5.1 Full local verification:** run `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking pixel6Api34DebugAndroidTest`.
- [ ] **5.2 Stub isolation verification:** run `bash scripts/check-stub-isolation.sh`.
- [ ] **5.3 Record command outputs:** fill `docs/sprints/results/PLANTPOTTING-0004.md` with commit hash, commands, outputs, and transcript links.
- [ ] **5.4 Record decisions:** in the results doc, explicitly record manifest-driven dtype, root `Box` bridge, and `androidTest` with local `@BindValue` fake bindings.
- [ ] **5.5 Record deferred work:** add one short section in the results doc listing PLANTPOTTING-0005 deferrals: confidence calibration, failure UI polish, `LowConfidenceFlowTest`, `PermissionDeniedFlowTest`, GPU/NNAPI.
- [ ] **5.6 Ledger update:** update `docs/sprints/ledger.yaml` for PLANTPOTTING-0004 only after all acceptance criteria below are true.

---

## 5. Phase Sequencing And Dependencies

```
Phase 0: baseline + RED contract tests
  |
  +--> Phase 1: root testTag -> resource-id bridge
  |
  +--> Phase 2: manifest/preprocessor/facade dtype fix
          |
          v
      Phase 3: real-model Hilt instrumentation smoke
          |
          v
      Phase 4: cold/warm/build-only integration transcripts
          |
          v
      Phase 5: final verification + docs + ledger
```

Dependency rules:

- [ ] Phase 1 can land before Phase 2; it fixes the script's first wait at `scripts/integration-flow.ps1:135`.
- [ ] Phase 3 depends on Phase 2 for the real `Interpreter.run` call to pass.
- [ ] Phase 3 also depends on replacing the global `TestIdentifyModule` with local fake bindings; otherwise the real-model smoke test is meaningless.
- [ ] Phase 4 depends on both Phase 1 and Phase 2; without Phase 1 the script cannot find `camera.shutter`, and without Phase 2 the script cannot reach `result.seePottingMix`.
- [ ] Phase 5 cannot mark the sprint done without both cold and warm device-aware transcripts.

Hard gates:

- [ ] `ModelManifestTest.manifestDeclaresSupportedInputDtype` must fail before `model_manifest.json` is edited.
- [ ] `ImagePreprocessorTest.uint8ManifestProducesRawByteInputWithoutNormalisation` must fail before `ImagePreprocessor.kt` is edited.
- [ ] `MainActivityResourceIdBridgeTest.cameraShutterTestTagIsExportedAsResourceId` must fail before `MainActivity.kt` is edited.
- [ ] `RealModelPlantIdentifierSmokeTest.productionPlantIdentifierBindingIsOnDevicePlantIdentifier` must fail before `TestIdentifyModule.kt` is removed.
- [ ] `RealModelPlantIdentifierSmokeTest.realModelIdentifyReturnsOnDeviceSourceWithoutThrowing` must fail against the current FLOAT32 path before the dtype implementation is considered complete.

---

## 6. Risks And Mitigations

- [ ] **Risk - ExperimentalComposeUiApi opt-in spreads.** Mitigation: keep `@OptIn(ExperimentalComposeUiApi::class)` in `MainActivity` only, and remove the redundant `RecommendationScreen` opt-in after the root bridge passes.
- [ ] **Risk - `testTagsAsResourceId` leaks internal tag strings into accessibility announcements.** Mitigation: inspect the uiautomator dump after Phase 1; tags should appear as `resource-id`, not as `text` or `content-desc`. Preserve the shutter content description `Capture plant photo`.
- [ ] **Risk - real-model test still uses `FakeFixedIdentifier`.** Mitigation: remove global `TestIdentifyModule.kt` and add a test assertion that injected `PlantIdentifier` is `OnDevicePlantIdentifier`.
- [ ] **Risk - AOSP Pixel 6 API 34 GMD cannot load TFLite native libs or mmap the asset.** Mitigation: the new smoke test is the binding signal. If it fails before inference with a native load error, record the exact error and treat it as a sprint blocker, not a de-scope.
- [ ] **Risk - UINT8 branch breaks `FakeInterpreterFacade`.** Mitigation: update `InterpreterFacadeTest` so the fake records both FLOAT32 and UINT8 `PreprocessedImage` values and remains independent of native TFLite.
- [ ] **Risk - future FP16/FLOAT32 model swap forgets to update `input_dtype`.** Mitigation: `RealModelPlantIdentifierSmokeTest.manifestInputDtypeMatchesInterpreterInputTensor` compares manifest dtype to the real interpreter tensor dtype.
- [ ] **Risk - output tensor dtype also differs.** Mitigation: while adding the interpreter dtype assertion, log or assert output tensor 0 dtype if easy. If output is not FLOAT32, extend `TfLiteInterpreterFacade.runInference` in the same sprint; do not leave a second native dtype mismatch.
- [ ] **Risk - removing `TestIdentifyModule` destabilizes existing instrumentation tests.** Mitigation: convert existing tests to local `@BindValue` fakes one file at a time, running focused GMD tests after each small batch.
- [ ] **Risk - checked-in JPEG accidentally becomes too large or copyrighted.** Mitigation: use a tiny generated plant-like or neutral JPEG fixture committed under `app/src/androidTest/assets/fixtures/`; it is only a smoke input, not an accuracy fixture.
- [ ] **Risk - device-aware script output changes because the real model routes low-confidence.** Mitigation: the script only requires reaching the result/recommendation flow with badge and recipe evidence. If deterministic emulator imagery yields a stable different archetype, update `PLANTPOTTING-0001.txt` with the observed values and explain why.

---

## 7. Acceptance Criteria

The sprint is done only when every item below is observably true:

- [ ] `PlantIdentifier.kt:3-5` is unchanged.
- [ ] `IdentificationResult` at `PlantIdentifier.kt:7-12` is unchanged.
- [ ] `model_manifest.json` contains `"input_dtype": "uint8"`.
- [ ] `model_manifest.json` no longer declares the FLOAT32 normalization stanza for the shipped UINT8 model.
- [ ] `ModelManifestTest`, `ImagePreprocessorTest`, and `InterpreterFacadeTest` are green.
- [ ] `RealModelPlantIdentifierSmokeTest.productionPlantIdentifierBindingIsOnDevicePlantIdentifier` is green.
- [ ] `RealModelPlantIdentifierSmokeTest.realModelIdentifyReturnsOnDeviceSourceWithoutThrowing` is green on `pixel6Api34DebugAndroidTest`.
- [ ] `RealModelPlantIdentifierSmokeTest.manifestInputDtypeMatchesInterpreterInputTensor` is green.
- [ ] A uiautomator dump after launch contains `resource-id="camera.shutter"`.
- [ ] `scripts/integration-flow.ps1` no longer times out at `Wait-ForNode -resourceId "camera.shutter"`.
- [ ] A real shutter-driven capture no longer renders the UINT8/FLOAT32 error from `diag-step3-after-shutter.xml`.
- [ ] `docs/sprints/evidence/PLANTPOTTING-0004/transcript-A-cold.txt` exists and shows a clean device-aware manifest diff.
- [ ] `docs/sprints/evidence/PLANTPOTTING-0004/transcript-B-warm.txt` exists and shows a clean device-aware manifest diff.
- [ ] `docs/sprints/evidence/PLANTPOTTING-0004/transcript-C-buildonly.txt` exists and shows the build-only regression still passes.
- [ ] `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking pixel6Api34DebugAndroidTest` is green.
- [ ] `bash scripts/check-stub-isolation.sh` is green.
- [ ] `docs/sprints/results/PLANTPOTTING-0004.md` records the final commit, all verification commands, transcript links, and any expected-artifact update.
- [ ] `docs/sprints/ledger.yaml` marks PLANTPOTTING-0004 done only after the previous criteria are true.

---

## 8. Implementer Notes

- [ ] Read `docs/sprints/drafts/PLANTPOTTING-0004-INTENT.md` and `docs/sprints/feedback/PLANTPOTTING-0003/feedback.md` before editing code.
- [ ] Keep fixes surgical. This sprint should touch model input plumbing, the root Compose semantics bridge, instrumentation test binding shape, and evidence docs.
- [ ] Do not turn the smoke JPEG into an accuracy test. The assertion is "real interpreter accepts real bytes and returns an on-device result", not "the model correctly names the fixture".
- [ ] Do not weaken `scripts/integration-flow.ps1` expected lines to get a green diff.
- [ ] Tick sprint checkboxes as work lands, not in a final batch.
