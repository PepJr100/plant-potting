# PLANTPOTTING-0004 — Fix sprint for PLANTPOTTING-0003

**Topic:** TFLite UINT8 contract fix + Compose-to-uiautomator bridge + Real-model test gap closure
**Status:** draft
**Plan:** `docs/sprints/drafts/PLANTPOTTING-0004-GEMINI.md`
**Primary Source:** `docs/sprints/feedback/PLANTPOTTING-0003/feedback.md`

---

## 1. Intent

PLANTPOTTING-0003 shipped a functional unit-tested on-device identifier, but two latent production bugs (Bug 1: UINT8/FLOAT32 mismatch; Bug 2: resource-id missing in uiautomator) prevented the headline acceptance bar from being met on a real emulator.

This sprint is a surgical fix cycle (matching the 0002 → 0001 pattern) to:
1. Fix the TFLite preprocessor contract to support the UINT8-quantized model.
2. Bridge Compose `testTag` to `resource-id` for integration script compatibility.
3. Close the test gap by adding an instrumentation test that exercises the real `.tflite` against the real native interpreter.
4. Deliver the deferred §7.5 transcripts and re-run the device-aware GMD chain.

---

## 2. Goals / Non-goals

### 2.1 Goals
- **Bug 1 (P0):** Support `input_dtype` (uint8/float32) in `model_manifest.json` and branch `ImagePreprocessor` accordingly.
- **Bug 2 (P1):** Opt-in to `testTagsAsResourceId` in `MainActivity` to fix integration scripts.
- **Test Gap:** Add a `@HiltAndroidTest` that uses the production `OnDevicePlantIdentifier` binding with a real JPEG fixture.
- **Transcripts:** Capture `transcript-A-cold.txt` and `transcript-B-warm.txt` with a clean diff against the device-aware expected manifest.

### 2.2 Non-goals (deferred to PLANTPOTTING-0005+)
- Confidence calibration / threshold tuning.
- UI polish for `CameraUiState.Failure` (e.g., Snackbar/Banner).
- GPU / NNAPI delegate.
- Re-enabling `PermissionDeniedFlowTest`.
- Any change to the `PlantIdentifier` interface.

---

## 3. Decisions

### 3.1 Manifest-driven vs Introspection for `input_dtype`
- **Decision:** Use manifest-driven `"input_dtype": "uint8"`.
- **Rejected:** Runtime-introspection of the TFLite interpreter `DataType`.
- **Rationale:** The manifest is our central contract for model metadata. Adding `input_dtype` allows the `ImagePreprocessor` to be configured at construction time (matching the current pattern for `input_size` and `normalization`) rather than during the `identify()` call after the interpreter is loaded.

### 3.2 Wrapping `PlantPottingNavHost` vs Top-level Theme
- **Decision:** Wrap `PlantPottingNavHost` in a `Box(modifier = Modifier.semantics { testTagsAsResourceId = true })`.
- **Rejected:** Adding semantics directly to the root surface or activity.
- **Rationale:** Standard practice for Compose semantics opt-in. It provides a clean boundary for the bridge without polluting the theme or activity lifecycle logic.

### 3.3 Real-model test location
- **Decision:** Part of `androidTest` in `app/src/androidTest/java/...`.
- **Rejected:** A new gated source set.
- **Rationale:** A single `@HiltAndroidTest` is sufficient and keeps the CI pipeline simple. It ensures the production binding (without module replacement) is exercised.

---

## 4. Phase 1 — Manifest and Preprocessor Contract (Bug 1)

Fix the UINT8 mismatch by allowing the manifest to specify the input type.

- [ ] **Contract Test (RED):** Update `ModelManifestTest.kt` to expect `input_dtype` field; assert `ImagePreprocessor` throws or fails if fed a UINT8 model while configured for FLOAT32 (if possible to test in isolation).
- [ ] **Manifest Update:** Add `"input_dtype": "uint8"` to `app/src/main/assets/ml/aiy_plants_v1/model_manifest.json`. Document that `normalization` mean/std are ignored for UINT8.
- [ ] **ModelManifest DTO:** Update `com.darkfactory.plantpotting.identify.model.ModelManifest` (and its JSON parser) to include `inputDtype: String`.
- [ ] **ImagePreprocessor Refactor (IMPL):**
    - Update constructor to read `manifest.inputDtype`.
    - Conditional `NormalizeOp`: Only add to `ImageProcessor.Builder()` if `inputDtype == "float32"`.
    - Conditional `TensorImage`: Use `DataType.UINT8` if `inputDtype == "uint8"`, otherwise `DataType.FLOAT32` (line 48 in `ImagePreprocessor.kt`).
    - Update `preprocess` return type if necessary (UINT8 models expect `ByteArray` or `ByteBuffer` of bytes, not `FloatArray`). *Decision:* Keep `PreprocessedImage` returning `FloatArray` for now IF the interpreter facade handles the cast, OR update `PreprocessedImage` to be a `TensorBuffer` wrapper.
- [ ] **Verification:** Run `OnDevicePlantIdentifierFixturesTest` (still using fakes) to ensure no regressions in flow.

---

## 5. Phase 2 — Compose-to-uiautomator Bridge (Bug 2)

Enable `testTagsAsResourceId` so `integration-flow.ps1` can locate nodes.

- [ ] **Baseline Check (RED):** Run `adb shell uiautomator dump` on a running app and verify `resource-id=""` for the shutter button.
- [ ] **Opt-in Semantics (IMPL):** Update `MainActivity.kt` to wrap `PlantPottingNavHost` in a `Box` with `testTagsAsResourceId = true`.
- [ ] **Verification:** Run `adb shell uiautomator dump` again; verify `resource-id="camera.shutter"` (and others) are now present.
- [ ] **Integration Script Run:** Execute `pwsh ./scripts/integration-flow.ps1`. It should no longer time out at the first wait (though it may still fail after capture until Phase 1 is complete).

---

## 6. Phase 3 — Closing the Test Gap (Real-model test)

Add an instrumentation test that exercises the real production code path.

- [ ] **Test Fixture:** Add a small real-world JPEG of a plant (e.g., `crassula_ovata.jpg`) to `app/src/androidTest/assets/fixtures/`.
- [ ] **Instrumentation Test (RED/GREEN):** Create `app/src/androidTest/java/com/darkfactory/plantpotting/identify/RealModelIdentificationTest.kt`.
    - Use `@HiltAndroidTest`.
    - **Do NOT** use `@UninstallModules(OnDeviceIdentifyModule::class)`.
    - Inject `PlantIdentifier`.
    - Call `identifier.identify(fixtureBytes)`.
    - Assert: No exception, `result.source == IdSource.ON_DEVICE_MODEL`, `result.scientificName` is non-null.
- [ ] **Validation:** Run `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.darkfactory.plantpotting.identify.RealModelIdentificationTest`.

---

## 7. Phase 4 — Transcripts and GMD Final-Verify

Capture the final evidence of a working on-device flow.

- [ ] **Transcript Capture:** Run `pwsh ./scripts/integration-flow.ps1` twice (cold launch and warm launch).
    - Save to `docs/sprints/evidence/PLANTPOTTING-0004/transcript-A-cold.txt`.
    - Save to `docs/sprints/evidence/PLANTPOTTING-0004/transcript-B-warm.txt`.
- [ ] **Expected Manifest Update:** Update `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` if the on-device badge string or other fields have changed.
- [ ] **GMD Run:** Run `pixel6Api34DebugAndroidTest` on CI/local GMD.
- [ ] **Final results doc:** Populate `docs/sprints/results/PLANTPOTTING-0004.md` and update `ledger.yaml`.

---

## 8. Risks and Mitigations

| Risk | Mitigation |
| --- | --- |
| `ExperimentalComposeUiApi` opt-in | Scope it strictly to the `Box` wrap in `MainActivity`. |
| `testTagsAsResourceId` leaking to a11y | Monitor TalkBack behavior; normally these tags are only surfaced to uiautomator, not announced unless explicitly added to content description. |
| UINT8 model still throws after fix | Verify `Interpreter.run` input buffer alignment and size; use `TensorImage.buffer` directly if `floatArray` cast is the bottleneck. |
| GMD image drifts | Stick to `pixel6Api34` as the anchor image for all integration evidence. |

---

## 9. Acceptance Criteria

- [ ] `RealModelIdentificationTest` passes against the real `.tflite` asset without Hilt-swapping the identifier.
- [ ] `resource-id` is visible in `uiautomator dump` for at least three distinct screens (Camera, Result, Recommendation).
- [ ] `scripts/integration-flow.ps1` completes a full cycle (shutter → result → recommendation) and returns a clean diff.
- [ ] `transcript-A-cold.txt` and `transcript-B-warm.txt` are present and show a successful flow.
- [ ] `bash scripts/check-stub-isolation.sh` remains green.
- [ ] `verifyNoNetworking` remains green.
