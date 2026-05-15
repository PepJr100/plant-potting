# PLANTPOTTING-0004 — Results

**Planned:** PLANTPOTTING-0004 fix sprint for PLANTPOTTING-0003 review bugs.
**Executor:** opus (Claude Code, in-session).
**Window:** 2026-05-15.

---

## §0 Baseline

`./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking`
on clean `main` (HEAD `2d48c64`) before any production edits: **GREEN**.
Captured to `docs/sprints/results/PLANTPOTTING-0004-baseline.txt` (50 lines, BUILD SUCCESSFUL in 2m 53s).
`bash scripts/check-stub-isolation.sh` → `stub isolation OK`.

### §0.4 / §0.5 / §0.6 / §0.7 / §0.8 contract-lock RED → GREEN

The five contract-lock tests landed RED (compile-failed, since they referenced
the not-yet-existing `ModelManifest.inputDtype`, `ModelDtype.UINT8`,
`PreprocessedImage.buffer: ByteBuffer`, `ModelManifestReader.parse(...)`, and
the `Box(Modifier.semantics{testTagsAsResourceId=true})` wrap). All five flipped
to GREEN once §1.1–§1.7 + §2.1 production edits landed:

| File | Status |
| --- | --- |
| `ModelManifestDtypeContractTest.kt` | green after §1.1 + §1.2 |
| `PreprocessedImageBufferShapeContractTest.kt` | green after §1.3 |
| `MainActivityTestTagsAsResourceIdTest.kt` | green after §2.1 |
| `ManifestRejectsInvalidInputDtype*` (in `ModelManifestTest.kt`) | green after §1.2 (rejection logic on `ModelManifestReader.parse`) |
| `OnDeviceModelRealInterpreterTest.kt` (instrumentation) | green on GMD after §1.5/§1.7/§1.9 + §3.1 fixture |

---

## §1 Bug 1 fix — UINT8 input + UINT8 output dequantization

**Diff summary:**

- `model_manifest.json`: added `"input_dtype": "uint8"`, removed the active
  `normalization` stanza (the FP-only contract for an FP16 variant we don't
  ship), preserved a `_comment_normalization_unused_for_uint8` breadcrumb,
  re-stamped `acquisition_date`. Kept sha256, label_count, output_tensor_shape,
  thresholds verbatim (no model swap).
- `ModelManifest.kt`: introduced `enum class ModelDtype { UINT8, FLOAT32 }`,
  added `val inputDtype: ModelDtype` field, refactored `ModelManifestReader`
  with a companion `parse(rawJson: String): ModelManifest` so the JVM-side
  rejection tests (§0.7) feed deliberately-malformed JSON through the same
  path the production reader uses. Missing/invalid `input_dtype` throws at
  parse time.
- `PreprocessedImage.kt`: `normalisedRgb: FloatArray` → `buffer: ByteBuffer`.
  The buffer is the natural shape for both UINT8 and FLOAT32 paths and feeds
  straight into `Interpreter.run`.
- `ImagePreprocessor.kt`: branches `TensorImage(DataType.UINT8)` vs
  `TensorImage(DataType.FLOAT32)` on `manifest.inputDtype`. Always applies
  `ResizeOp`; appends `NormalizeOp(manifest.normalization.mean,
  manifest.normalization.std)` only on the FLOAT32 path.
- `InterpreterFacade.kt`:
  - `TfLiteInterpreterFacade` constructor takes `expectedInputDtype: ModelDtype`.
  - `loadInterpreter()` reads `interpreter.getInputTensor(0).dataType()` and
    asserts it agrees with the manifest dtype; mismatch throws
    `IdentificationFailureException("Model input dtype mismatch: …")`.
  - `runInference` branches: UINT8 path calls `interpreter.run(buffer, output)`
    where `output = Array(1) { ByteArray(labelCount) }`, then dequantizes via
    `interpreter.getOutputTensor(0).quantizationParams()` (`scale`, `zeroPoint`)
    into a `FloatArray` so `ModelScoreMapper`'s contract is unchanged.
  - `FakeInterpreterFacade` records `lastInputBufferCapacity` + carries
    `expectedInputDtype` for `InterpreterFacadeTest` introspection.
- `OnDeviceIdentifyProvidersModule.provideInterpreterFacade` now passes
  `manifest.inputDtype` into the `TfLiteInterpreterFacade` constructor.

**§1.4 / §1.6 test rewrites:**

- `ImagePreprocessorTest`: dropped FP32-only normalization assertions
  (`whitePixelNormalisesToOne`, `midGreyPixelNormalisesToZero`) — they no longer
  fit the shipped UINT8 manifest. Added
  `uint8ManifestProducesUint8BufferAndSkipsNormaliseOp` (shipped path: 1 byte
  per channel, white pixel ≈ 0xFF) and `float32ManifestKeepsNormaliseOp`
  (in-memory FP32 manifest: 4 bytes per channel, white pixel ≈ 1.0 after
  `(255-127.5)/127.5` normalize). Kept
  `preprocessOutputsManifestInputSize`, `gradientJpegProducesNonConstantTensor`,
  `emptyJpegBytesThrowIdentificationFailure` modulo the buffer-shape change.
- `InterpreterFacadeTest`: pivoted to the `ByteBuffer` contract; added
  `runInferenceAcceptsByteBuffer` and
  `fakeInterpreterFacadeRecordsBufferCapacityForBothDtypes`. Deferred the
  load-time mismatch behaviour test to instrumentation (the real
  `Interpreter` is unreliable to instantiate on JVM).
- `ModelManifestTest`: replaced `normalisationMeanAndStdAreThreeChannel` with
  `normalisationStanzaAbsentForShippedUint8Model` (asserts the active stanza
  is gone and the breadcrumb is present); refreshed
  `manifestIsValidJson` to require `input_dtype` instead of `normalization`.

**§1.11 verify:** `./gradlew --no-daemon assembleDebug testDebugUnitTest lint
ktlintCheck verifyNoNetworking` GREEN. Captured to
`docs/sprints/results/PLANTPOTTING-0004-phase1-verify.txt` (BUILD SUCCESSFUL
in 9m 29s, 153 unit tests passing).

---

## §2 Bug 2 fix — testTagsAsResourceId bridge

**Diff summary:**

- `MainActivity.onCreate` wraps `PlantPottingNavHost()` in
  `Box(modifier = Modifier.semantics { testTagsAsResourceId = true })`,
  opted into `ExperimentalComposeUiApi` at the call site only.
- `RecommendationScreen.kt` cleanup: removed the redundant per-screen
  `testTagsAsResourceId` opt-in (lines 22-27, 33, 46-50 in PLANTPOTTING-0003)
  and the now-unused imports of `ExperimentalComposeUiApi` /
  `androidx.compose.ui.semantics.semantics` /
  `androidx.compose.ui.semantics.testTagsAsResourceId`.
- Audit: `grep -R "testTagsAsResourceId" app/src/main/` returns exactly **one**
  hit — `MainActivity.kt`. `grep -R "ExperimentalComposeUiApi" app/src/main/`
  also returns exactly one hit, scoped to the `MainActivity` `@OptIn` site.

**§2.4 device-side smoke (taken on the integration-flow.ps1 cold run, see §4):**
the script's `Wait-ForNode -resourceId "camera.shutter"` step now passes;
`uiautomator dump` after launch surfaces `resource-id="camera.shutter"`
non-empty AND `content-desc="Capture plant photo"` preserved (a11y
authority unchanged per §4.4 Decision).

---

## §3 Real-model instrumentation test (closes the gap that let Bug 1 ship)

`app/src/androidTest/java/com/darkfactory/plantpotting/identify/OnDeviceModelRealInterpreterTest.kt`
injects the **concrete** `OnDevicePlantIdentifier` (not the `PlantIdentifier`
interface) — Hilt builds the concrete class via its `@Inject` constructor
regardless of `TestIdentifyModule`'s `@TestInstallIn(replaces =
[OnDeviceIdentifyModule::class])` swap (Decision §4.5). The whole production
pipeline is exercised: `ImagePreprocessor` → `TfLiteInterpreterFacade` against
the real `model.tflite` → `ModelScoreMapper`.

The fixture at
`app/src/androidTest/assets/identify-fixtures/monstera-deliciosa.jpg`
(~52 KB, 480×480) is a procedurally-generated synthetic JPEG with a leafy-
green radial pattern (deterministic seed `0x4D4F4E54`). It is **not** a
photograph of an actual Monstera deliciosa — see
`app/src/androidTest/assets/identify-fixtures/LICENSE.txt`. Per Risk §7.6
this is a smoke fixture: the test asserts only `result.source ==
IdSource.ON_DEVICE_MODEL` and that `identify(bytes)` returns without throwing.
The synthetic image will route to the low-confidence path (which is itself
the expected outcome on the AOSP virtual scene per §4.5).

`./gradlew --no-daemon :app:compileDebugAndroidTestKotlin` GREEN
(BUILD SUCCESSFUL in 2m 10s) — §3.3 compile gate met.

§3.4 fallback (concrete-class injection failure): **not exercised** — the
GMD run (§4.4 below) confirmed Hilt builds the concrete class from its
`@Inject` constructor as theorised. No per-test rebind module added.

---

## §4 Transcripts + GMD final-verify

All four artefacts under `docs/sprints/evidence/PLANTPOTTING-0004/`:

| Artefact | Result |
| --- | --- |
| `transcript-A-cold.txt` | `Integration manifest diff passed.` (cold-boot `Pixel_6_API_34`) |
| `transcript-B-warm.txt` | `Integration manifest diff passed.` (after `am force-stop`) |
| `transcript-C-buildonly.txt` | `Integration manifest diff passed.` (`-BuildOnly`) |
| `gmd-output.txt` | `pixel6Api34DebugAndroidTest` GREEN — 11/11 tests (1 `@Ignore`d skipped, 10 passing including the new `OnDeviceModelRealInterpreterTest`). BUILD SUCCESSFUL in 4m 33s. |

### §4.5 expected-artifact policy update

Per the §4.5 policy and Risk §7.11, the AIY V1/3 model maps only 2 of 16 KB
species verbatim. Synthetic emulator captures (the `Pixel_6_API_34` AOSP
virtual scene) route to `LowConfidencePicker`, not the high-conf `ResultScreen`
direct path. Two adjustments landed:

1. **`scripts/integration-flow.ps1`:** added a low-confidence override branch
   between the shutter tap and the result-screen wait. After the shutter, the
   script polls for either `result.seePottingMix` (high-conf path) OR
   `lowConf.species.monstera-deliciosa` (low-conf path). On the low-conf path
   it taps the deterministic `lowConf.species.monstera-deliciosa` row to
   navigate onward to ResultScreen with `lowConfidence = true`. The rest of
   the flow (`See potting mix` → recommendation) is unchanged.
   Also fixed an orthogonal latent bug in the script's badge extraction:
   the Material3 `AssistChip` doesn't surface its label in its own `text`
   attribute; the script now walks descendants for the label.
2. **`docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`:**
   `source-badge=on-device match` → `source-badge=on-device match (low confidence)`
   to match the captured deterministic value. Per the §4.5 policy this is
   "honest evidence", not weakening — `source-badge=` line preserved,
   archetype-name + recipe-row-count + model-asset-present unchanged.

### §4.6 falsifiability evidence

Captured locally (no separate branch — the revert was applied in-place,
captured, and reverted again immediately, leaving `main`-tracking state
clean). Output saved to
`docs/sprints/evidence/PLANTPOTTING-0004/falsifiability-revert-output.txt`.

**Revert:** `ImagePreprocessor.kt`'s `tensorDataType` initializer was forced
to `DataType.FLOAT32` regardless of `manifest.inputDtype`.

**Single-test run:**
`./gradlew --no-daemon pixel6Api34DebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.darkfactory.plantpotting.identify.OnDeviceModelRealInterpreterTest`

**Result (GMD):**
```
com.darkfactory.plantpotting.identify.OnDeviceModelRealInterpreterTest > realJpegFedThroughRealInterpreterDoesNotThrow[pixel6Api34] FAILED
    com.darkfactory.plantpotting.identify.IdentificationFailureException: TFLite inference failed: Cannot copy to a TensorFlowLite tensor (module/hub_input/images_uint8) with 150528 bytes from a Java Buffer with 602112 bytes.
        at com.darkfactory.plantpotting.identify.model.TfLiteInterpreterFacade.runInference(InterpreterFacade.kt:95)
```

**Why this is the same defect as Bug 1:** PLANTPOTTING-0003 review captured
Bug 1 as `"TFLite inference failed: Cannot convert between a TensorFlowLite
tensor with type UINT8 and a Java object of type [[[[F …"`. The revert
reproduces a near-identical TFLite native error wrapped in the same
`IdentificationFailureException("TFLite inference failed: …")` envelope.
The exact wording differs because the §1.7 facade now passes a
`ByteBuffer` to `interpreter.run` (the size-mismatch path:
602112 = 224·224·3·4 vs 150528 = 224·224·3) instead of the previous
`Array<Array<Array<FloatArray>>>` (the type-mismatch path: `[[[[F`).
Both surface the same root contract violation — feeding FLOAT32 bytes
into a UINT8 tensor — and both are caught by the same exception
envelope. The new test catches Bug-1-shaped regressions.

The revert was reverted before this results doc was committed; no
production code drifted as a result.

---

## §5 Carry-forward to PLANTPOTTING-0005

Explicitly deferred (per plan §3.3 / §6.3 / §8.7):

- **Full `TestIdentifyModule` global-removal + six-test `@BindValue` migration**
  (Decision §4.5 — architecturally cleaner but a six-test refactor that drifts
  beyond the fix-sprint shape).
- Confidence calibration / per-class threshold tuning.
- `CameraUiState.Failure` UI polish (Snackbar / banner / dedicated
  `CaptureFailedScreen`).
- `LowConfidenceFlowTest` instrumentation (B3 from PLANTPOTTING-0003).
- Re-enabling `PermissionDeniedFlowTest` (`@Ignore` since PLANTPOTTING-0001).
- INT8 stretch / GPU / NNAPI delegate.

Open observation worth carrying forward: the `LowConfidencePicker` is now the
primary user-facing post-shutter screen on devices where the AIY V1/3
vocabulary doesn't directly cover the captured species. PLANTPOTTING-0005 UI
polish should treat it as the headline result surface, not a fallback.

---

## §6 Final-verify

| Command | Result |
| --- | --- |
| `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` | GREEN — `phase1-verify.txt` |
| `bash scripts/check-stub-isolation.sh` | GREEN — `stub isolation OK` |
| `./gradlew --no-daemon :app:compileDebugAndroidTestKotlin` | GREEN |
| `./gradlew --no-daemon pixel6Api34DebugAndroidTest` | GREEN — `gmd-output.txt`, 10 tests passing |
| `pwsh ./scripts/integration-flow.ps1` (cold) | GREEN — `transcript-A-cold.txt` |
| `pwsh ./scripts/integration-flow.ps1` (warm) | GREEN — `transcript-B-warm.txt` |
| `pwsh ./scripts/integration-flow.ps1 -BuildOnly` | GREEN — `transcript-C-buildonly.txt` |

Final commit hash: implementation at `c26d1ac` (PLANTPOTTING-0004 fix Bug 1
+ Bug 2). Ledger-close commit follows; both are on `main`.
