# PLANTPOTTING-0004 — refined intent (shared prompt seed)

You are drafting sprint **PLANTPOTTING-0004**.

## Refined intent

Land a fix sprint for PLANTPOTTING-0003 (same shape as PLANTPOTTING-0002 → 0001). Four must-land outcomes:

1. **Bug 1 (P0) — UINT8 model fed FLOAT32 tensor.** The real AIY Plants V1/3 model dropped into `app/src/main/assets/ml/aiy_plants_v1/model.tflite` (sha256 `9ff2cc02…`, 5 MB, INT8/UINT8 quantized) throws on every shutter capture: `"TFLite inference failed: Cannot convert between a TensorFlowLite tensor with type UINT8 and a Java object of type [[[[F (which is compatible with the TensorFlowLite type FLOAT32)."`. Root cause: `ImagePreprocessor.kt:48` builds `TensorImage(DataType.FLOAT32)` and applies a `NormalizeOp(mean=127.5, std=127.5)`; the UINT8 model wants raw 0–255 bytes fed into a `TensorImage(DataType.UINT8)` with normalization encoded in quantization parameters (scale/zero_point). Fix: add `input_dtype` (`"uint8"` | `"float32"`) to `model_manifest.json`; `ImagePreprocessor` branches `TensorImage(...)` and includes/skips the `NormalizeOp` accordingly; drop the dead normalization stanza from the manifest for the UINT8 variant (or document that it's unused).

2. **Bug 2 (P1) — Compose `testTag` not bridged to uiautomator `resource-id`.** Every Compose node dumps with `resource-id=""`, so `scripts/integration-flow.ps1`'s `Wait-ForNode -resourceId "camera.shutter"` times out at the first wait. Root cause: `MainActivity.kt:19-23` calls `setContent { PlantPottingTheme { PlantPottingNavHost() } }` without `Modifier.semantics { testTagsAsResourceId = true }` on the root composition. Fix: wrap the nav host with a `Box(modifier = Modifier.semantics { testTagsAsResourceId = true })`, `@OptIn(ExperimentalComposeUiApi::class)` on the call site.

3. **Close the test gap that allowed Bug 1 to ship.** Add at least one `@HiltAndroidTest` instrumentation test that does **not** replace `OnDeviceIdentifyModule` — calls `identifier.identify(realJpegBytes)` against a checked-in fixture JPEG, asserts the call returns without throwing, asserts `source = ON_DEVICE_MODEL`. The whole point: feed a real `ByteArray` through the real `Interpreter.run` against the shipped `.tflite` exactly once in the gated test chain, so a future preprocessor / model contract drift fails CI rather than the user's emulator.

4. **Capture the §7.5 transcripts and re-run the GMD chain.** Once Bug 1 + Bug 2 are fixed, capture cold + warm device-aware integration-flow transcripts (`docs/sprints/evidence/PLANTPOTTING-0004/transcript-A-cold.txt`, `transcript-B-warm.txt`) and re-run `pixel6Api34DebugAndroidTest`. Update `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` if the device-aware expected manifest needs to change (e.g., the badge string is now `on-device match` not `stub identifier — replace in a later sprint`).

## Non-goals (deferred to PLANTPOTTING-0005 or later)

- Confidence calibration / per-class threshold tuning.
- UI polish for `CameraUiState.Failure` (Snackbar, banner, dedicated `CaptureFailedScreen`). Worth a one-line note in the results doc, but not in this sprint.
- INT8 quant variant work beyond what Bug 1 needs. (The model is already INT8; the fix is to handle it correctly, not to swap it.)
- GPU / NNAPI delegate.
- Re-enabling `PermissionDeniedFlowTest` (still `@Ignore` from PLANTPOTTING-0001).
- New `IdSource` values; new screens; new KB content edits.
- `LowConfidenceFlowTest` instrumentation (B3 from 0003 — defer unless trivially in reach after Bug 1 + Bug 2 land).
- Any change to the `PlantIdentifier` interface shape (PLANTPOTTING-0003 §4.4 invariants still hold).
- Bumping AGP / Kotlin / Compose / Hilt / TFLite versions.

## Hard constraints (non-negotiable)

- `PlantIdentifier` interface byte-for-byte unchanged; `IdentificationResult` schema unchanged.
- `StubPlantIdentifier.kt` stays under `app/src/main/java/com/darkfactory/plantpotting/identify/`; `bash scripts/check-stub-isolation.sh` stays green throughout.
- `./gradlew verifyNoNetworking` stays green throughout.
- `bash scripts/check-stub-isolation.sh` stays green throughout.
- No regressions in `EndToEndFlowTest`, `ResultScreenBadgeTest`, `LowConfidencePicker*Test`, `OnDevicePlantIdentifier*Test`, `ModelLabelMappingValidationTest`, `ModelManifestTest`.
- TDD discipline: every behaviour change has a paired test task that lands RED first.
- The new real-model instrumentation test must not require swapping `OnDeviceIdentifyModule`. It must exercise the production binding.
- Acceptance is only met when **both** transcripts (cold + warm) land with a clean diff against the device-aware expected file — the headline acceptance bar that PLANTPOTTING-0003 deferred.

## Anchor files (read before drafting)

- `docs/sprints/PLANTPOTTING-0001.md`, `PLANTPOTTING-0002.md`, `PLANTPOTTING-0003.md` — prior sprint plans.
- `docs/sprints/feedback/PLANTPOTTING-0003/feedback.md` — bug reports + fix directions (THIS IS THE PRIMARY SOURCE).
- `docs/sprints/results/PLANTPOTTING-0003.md` — what shipped and what didn't; see §6 / §8.
- `app/src/main/java/com/darkfactory/plantpotting/identify/PlantIdentifier.kt`
- `app/src/main/java/com/darkfactory/plantpotting/identify/StubPlantIdentifier.kt`
- `app/src/main/java/com/darkfactory/plantpotting/identify/OnDevicePlantIdentifier.kt`
- `app/src/main/java/com/darkfactory/plantpotting/identify/model/ImagePreprocessor.kt`
- `app/src/main/java/com/darkfactory/plantpotting/identify/model/InterpreterFacade.kt`
- `app/src/main/java/com/darkfactory/plantpotting/identify/OnDeviceIdentifyModule.kt`
- `app/src/main/java/com/darkfactory/plantpotting/MainActivity.kt`
- `app/src/main/assets/ml/aiy_plants_v1/model_manifest.json`
- `scripts/integration-flow.ps1`, `scripts/integration-flow-helpers.ps1`
- `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` (device-aware manifest)
- `docs/sprints/feedback/PLANTPOTTING-0003/diag-step2-camera-dump.xml` (Bug 2 evidence)
- `docs/sprints/feedback/PLANTPOTTING-0003/diag-step3-after-shutter.xml` (Bug 1 evidence)
