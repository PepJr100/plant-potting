# PLANTPOTTING-0004 — Fix sprint for PLANTPOTTING-0003 review bugs

**Status:** planned
**Sprint shape:** narrow fix sprint, same pattern as PLANTPOTTING-0002 → PLANTPOTTING-0001
**Sprint window:** ~3–5 days for a single AI implementer (opus, gpt-5.4, or gemini — picked at `sprint-execute` time)
**Primary source:** `docs/sprints/feedback/PLANTPOTTING-0003/feedback.md`
**Source artefacts:** merged from `drafts/PLANTPOTTING-0004-{CODEX,GEMINI,CLAUDE}.md` plus the three cross-critiques.

---

## 1. Intent

Land the four must-land outcomes that PLANTPOTTING-0003 deferred:

1. **Bug 1 (P0)** — the real AIY Plants V1/3 model dropped into `app/src/main/assets/ml/aiy_plants_v1/model.tflite` (sha256 `9ff2cc02…`, 5 MB) is **fully INT8/UINT8-quantized**: both input *and* output tensors are UINT8. `ImagePreprocessor.kt:48` constructs `TensorImage(DataType.FLOAT32)` and applies `NormalizeOp(mean=127.5, std=127.5)`; `TfLiteInterpreterFacade.runInference` then passes a FLOAT32 buffer to `Interpreter.run`. Every shutter capture throws `"TFLite inference failed: Cannot convert between a TensorFlowLite tensor with type UINT8 and a Java object of type [[[[F …"`. Fix: declare `input_dtype` (`"uint8"` | `"float32"`) in `model_manifest.json`; branch `TensorImage` + `NormalizeOp` in `ImagePreprocessor` accordingly; **dequantize the UINT8 output tensor** in the facade via `interpreter.getOutputTensor(0).quantizationParams()` so `ModelScoreMapper` still sees a `FloatArray`; add a load-time runtime-dtype-vs-manifest assertion so a future FP16 swap that forgets the manifest update fails fast with a diagnostic message instead of producing wrong results silently.

2. **Bug 2 (P1)** — `MainActivity.kt:19-23` calls `setContent { PlantPottingTheme { PlantPottingNavHost() } }` without `Modifier.semantics { testTagsAsResourceId = true }` on the root composition, so every Compose node dumps with `resource-id=""` and `scripts/integration-flow.ps1`'s first `Wait-ForNode -resourceId "camera.shutter"` times out. Fix: wrap the nav host in `Box(modifier = Modifier.semantics { testTagsAsResourceId = true })` opted into `ExperimentalComposeUiApi` at the call site. Remove the redundant per-screen `testTagsAsResourceId` opt-ins in `RecommendationScreen.kt` (lines 22-27, 33, 46-50) after the root bridge passes.

3. **Close the test gap that let Bug 1 ship.** Add one `@HiltAndroidTest` instrumentation test (`OnDeviceModelRealInterpreterTest`) that exercises the **real** `OnDevicePlantIdentifier` (not `FakeFixedIdentifier`) against a checked-in fixture JPEG. The test injects the concrete `OnDevicePlantIdentifier` class — Hilt builds it via its `@Inject` constructor regardless of the `TestIdentifyModule` `@TestInstallIn` swap, because the swap only replaces the `@Binds PlantIdentifier`, not the `@Singleton @Inject` concrete class. (The broader hygiene work — delete the global `TestIdentifyModule` swap and migrate the six existing instrumentation tests to local `@BindValue` fakes — is **explicitly deferred to PLANTPOTTING-0005**; this sprint adds the smallest credible new gate, with a documented fallback if concrete-class injection misbehaves.)

4. **Capture the §7.5 transcripts.** With Bugs 1 + 2 fixed, run `pwsh ./scripts/integration-flow.ps1` cold + warm on a connected `Pixel_6_API_34` emulator. Drop `transcript-A-cold.txt`, `transcript-B-warm.txt`, `transcript-C-buildonly.txt` under `docs/sprints/evidence/PLANTPOTTING-0004/`. Re-run `pixel6Api34DebugAndroidTest`. The headline acceptance bar PLANTPOTTING-0003 deferred is the one this sprint clears.

---

## 2. Goals and non-goals

### 2.1 The single observable success bar

On a connected `Pixel_6_API_34` emulator (AOSP virtual scene):

1. The §2.1 user flow from PLANTPOTTING-0001 completes end-to-end on every shutter capture without surfacing the `Cannot convert between a TensorFlowLite tensor with type UINT8…` failure. The user reaches `ResultScreen` (high-conf path) or `LowConfidencePicker` (low-conf path; the more likely outcome given `_comment_coverage` in the manifest).
2. `adb shell uiautomator dump /sdcard/dump.xml && adb pull /sdcard/dump.xml` produces XML where Compose nodes carry their `testTag` strings as `resource-id` (`camera.shutter`, `result.sourceBadge`, `result.seePottingMix`, `recommendation.archetypeName`, `recommendation.recipeRow`, `lowConf.search`, etc.).
3. `pwsh ./scripts/integration-flow.ps1` (device-aware, no `-BuildOnly`) produces `Integration manifest diff passed.` against `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`. Two transcripts (cold + warm) checked in.
4. `./gradlew --no-daemon pixel6Api34DebugAndroidTest` green, including the new `OnDeviceModelRealInterpreterTest`.

### 2.2 Falsifiability — how we know we hit it

- `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking pixel6Api34DebugAndroidTest` green on a clean clone.
- `bash scripts/check-stub-isolation.sh` green throughout.
- `git diff ac120c9 HEAD -- app/src/main/java/com/darkfactory/plantpotting/identify/PlantIdentifier.kt` returns empty (seam invariants from PLANTPOTTING-0003 §4.4 still hold).
- New tests `ModelManifestDtypeContractTest`, `PreprocessedImageBufferTest`, `ImagePreprocessorTest` (rewritten), `InterpreterFacadeTest` (rewritten), `MainActivityTestTagsAsResourceIdTest`, `OnDeviceModelRealInterpreterTest` all green.
- **Falsifiability proof for the test-gap closure:** if the implementer reverts the §1.7 preprocessor branch on a feature branch (restoring the FLOAT32 path), `OnDeviceModelRealInterpreterTest` fails with the exact Bug 1 error message — confirming the test would have caught Bug 1.

### 2.3 Non-goals (explicit deferrals)

These are deferred to PLANTPOTTING-0005 or later:

- Confidence calibration / per-class threshold tuning. The §4.3 confidence table from PLANTPOTTING-0003 is unchanged this sprint.
- UI polish for `CameraUiState.Failure` (Snackbar / banner / dedicated `CaptureFailedScreen`). The failure-text-at-TopCenter UX is preserved as-is — legible but easy to miss. One-line carry-forward note in the results doc.
- **Full `TestIdentifyModule` global-replacement removal + migration of six existing instrumentation tests to per-test `@BindValue` fakes.** This is the architecturally cleaner fix, but it's a six-test refactor that drifts beyond the fix-sprint shape. Carried to PLANTPOTTING-0005.
- INT8 quant variant *work* beyond what Bug 1 needs (the model is already INT8; this sprint handles it correctly, doesn't swap it).
- GPU / NNAPI delegate.
- Re-enabling `PermissionDeniedFlowTest` (still `@Ignore` from PLANTPOTTING-0001).
- New `IdSource` values, new screens, new KB content edits.
- `LowConfidenceFlowTest` instrumentation (B3 from 0003 — defer; this sprint's real-model test does not replace it).
- Any change to the `PlantIdentifier` interface shape or to `IdentificationResult`'s field list. **PLANTPOTTING-0003 §4.4 invariants still hold.**
- AGP / Kotlin / Compose / Hilt / TFLite version bumps.

---

## 3. Scope boundaries

### 3.1 Must-land

- `input_dtype` field on `model_manifest.json` (set to `"uint8"`) and parsed into `ModelManifest.inputDtype: ModelDtype` via `ModelManifestReader`.
- `ModelManifestReader` rejects (throws) any `input_dtype` value other than `"uint8"` or `"float32"`. A missing field is also a hard error for the production manifest (the JVM unit test allows in-memory fixtures to default to FLOAT32 for back-compat, but the production manifest must declare the field).
- `PreprocessedImage` refactored to `data class PreprocessedImage(val width: Int, val height: Int, val buffer: ByteBuffer)` — `TensorImage.buffer` is the natural shape for both dtypes and avoids a "FloatArray of bytes" pseudo-type.
- `ImagePreprocessor` branches `TensorImage(DataType.UINT8)` vs `TensorImage(DataType.FLOAT32)` from `manifest.inputDtype`. Always applies `ResizeOp`. Applies `NormalizeOp(manifest.normalization.mean, manifest.normalization.std)` **only** on the FLOAT32 path; the UINT8 path encodes normalization in tensor quantization params.
- `TfLiteInterpreterFacade` accepts a `manifest: ModelManifest` (or `expectedInputDtype: ModelDtype`) at construction. In `loadInterpreter()`, after `Interpreter(buffer)` succeeds, reads `interpreter.getInputTensor(0).dataType()` and asserts it agrees with the manifest's declared dtype. Mismatch throws `IdentificationFailureException("Model input dtype mismatch: manifest=<x>, runtime=<y>")`.
- `TfLiteInterpreterFacade.runInference` passes the `PreprocessedImage.buffer` directly to `interpreter.run(buffer, output)`. **For UINT8 models, the output tensor is also UINT8** — declare `val output = Array(1) { ByteArray(labelCount) }`, then dequantize via `interpreter.getOutputTensor(0).quantizationParams()` (scale + zeroPoint) into a `FloatArray` before returning. The FLOAT32 path keeps `Array(1) { FloatArray(labelCount) }`.
- `model_manifest.json` no longer declares an active `normalization` stanza for the UINT8 model. Either remove the block or relocate it to a `_comment_normalization_unused_for_uint8` field that explicitly says it isn't read. Re-stamp `acquisition_date`.
- `MainActivity.onCreate` wraps `PlantPottingNavHost()` in `Box(modifier = Modifier.semantics { testTagsAsResourceId = true })` with `@OptIn(ExperimentalComposeUiApi::class)` scoped to the call site only.
- Per-screen redundant `testTagsAsResourceId` opt-ins removed from `RecommendationScreen.kt:22-27`, `:33`, `:46-50`. (Audit other screens for the same pattern; PLANTPOTTING-0003 may have added them defensively in more than one place.)
- New `@HiltAndroidTest` `OnDeviceModelRealInterpreterTest` injecting concrete `OnDevicePlantIdentifier`, runs `identify(realJpegBytes)` against a checked-in fixture JPEG, asserts no throw and `source = IdSource.ON_DEVICE_MODEL`. **Falsifiability:** documented as catching Bug 1 if the §1.7 branch is reverted.
- Three transcripts captured: `transcript-A-cold.txt` (cold-boot emulator), `transcript-B-warm.txt` (warm re-run after `am force-stop`), `transcript-C-buildonly.txt` (`-BuildOnly` regression). All under `docs/sprints/evidence/PLANTPOTTING-0004/`. Cold + warm produce `Integration manifest diff passed.` against `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`.
- Results doc `docs/sprints/results/PLANTPOTTING-0004.md` + ledger close to `done`.

### 3.2 Nice-to-have (only if time permits — do **NOT** slip §3.1 for these)

- A Compose-UI Robolectric test (`MainActivityTestTagsAsResourceIdTest` extended) that asserts the root `SemanticsNode` exposes `SemanticsProperties.TestTagsAsResourceId = true`. JVM-layer regression catch for Bug 2.
- A second fixture JPEG in `OnDeviceModelRealInterpreterTest` — a blank/grey frame that exercises the low-confidence path through the real interpreter (high-conf path covered by the Monstera fixture). Record observed top-1 / top-3 probabilities in the results doc as evidence.
- A11y verification: in the §2.3 uiautomator dump, assert tags appear only as `resource-id`, not as `text` or `content-desc`, and `content-desc="Capture plant photo"` is still set on the shutter. Mitigation evidence for Risk 7.2.

### 3.3 Out-of-scope (explicit deferrals)

- Anything in §2.3 above.
- Reworking `OnDeviceIdentifyModule`'s split (Binds vs Providers) — the current split already lets us inject the concrete class without touching modules.
- Adding a Snackbar / banner / dedicated screen for `CameraUiState.Failure`.
- Persisting model metadata to disk.
- Multi-plant detection, plant-health diagnosis, AR overlays, live-camera labels (still out-of-scope per 0001/0003).

---

## 4. Decisions (opinionated; alternatives documented for the merge audit)

### 4.1 `input_dtype` source — manifest field vs runtime introspection

**Pick:** **manifest-driven** policy + **runtime cross-check**. Add `"input_dtype": "uint8"` to `model_manifest.json`; parse into `ModelManifest.inputDtype: ModelDtype` (enum). The preprocessor reads it at construction; the interpreter facade asserts the runtime tensor type matches the manifest at load time and throws `IdentificationFailureException` on disagreement.

**Rejected — pure runtime introspection** (`interpreter.getInputTensor(0).dataType()` only, no manifest field): silently masks contract drift. A future FP16 swap that forgets to update the preprocessor "just works" but produces wrong results. The manifest field is the editorial assertion that says "this model is meant to be UINT8" so a contradicting native dtype fails fast. Also, the unit-test path (`FakeInterpreterFacade` + Robolectric) never instantiates a real `Interpreter`, so runtime introspection isn't observable from JVM unit tests — manifest-driven is.

**Rejected — silently default missing `input_dtype` to FLOAT32:** undercuts the manifest contract. A future asset drop without the field would pass tests in paths that should fail. The reader rejects missing-or-invalid `input_dtype` values for the production manifest at parse time. In-memory test fixtures may pass a default for ergonomic reasons, but the JSON deserializer rejects.

### 4.2 `PreprocessedImage` shape — `FloatArray` vs `ByteBuffer` vs sealed type

**Pick:** **`data class PreprocessedImage(val width: Int, val height: Int, val buffer: ByteBuffer)`**. `TensorImage` produces the buffer natively for both UINT8 and FLOAT32; the interpreter consumes it directly via `interpreter.run(buffer, output)`.

**Rejected — keep `FloatArray` and convert in the facade:** produces an awkward "FloatArray of bytes" for the UINT8 path or duplicates normalization logic in the facade. (CODEX's draft left this ambiguous; GEMINI's draft kept it open as a non-decision. Both pathologies recreate Bug 1 under a new name.)

**Rejected — sealed type `PreprocessedImage.Float / .UInt8`:** more surface, no clear pay-off. `ByteBuffer` is already a sufficiently abstract shape.

**Blast radius:** `PreprocessedImage.kt`, `ImagePreprocessor.kt`, `TfLiteInterpreterFacade` (real impl), `FakeInterpreterFacade`, `ImagePreprocessorTest`, `InterpreterFacadeTest`. Bounded; ~80 lines of net diff.

### 4.3 UINT8 output dequantization

**Pick:** **dequantize in the facade**, before `ModelScoreMapper` sees the scores. After `interpreter.run(buffer, output)` where `output = Array(1) { ByteArray(labelCount) }`, read `interpreter.getOutputTensor(0).quantizationParams()` (`scale: Float`, `zeroPoint: Int`) and convert each byte: `(byte.toUByte().toInt() - zeroPoint) * scale`. `ModelScoreMapper` continues to receive a `FloatArray` — its contract is unchanged.

**Why this is must-land and not a nice-to-have:** AIY V1/3 is **fully** quantized. Without output dequantization, the input fix produces a successful `Interpreter.run` call but `ModelScoreMapper` sees raw 0–255 bytes interpreted as probabilities — every score reads as a huge float, the §4.3-from-0003 confidence threshold never holds for the right reason, and the test gap closure ("doesn't throw") still passes. This is exactly the silent-failure shape CODEX's risk note flagged but didn't budget a task for. **Promoted to task §1.9.**

**Branch policy:** branch on `manifest.inputDtype` for symmetry with the input path. Optionally cross-check `interpreter.getOutputTensor(0).dataType()` and treat output dtype as a separate field if AIY mixed-dtype models ever appear (no current model does this; treat as a future-proofing nice-to-have).

### 4.4 Compose root opt-in — `Box(Modifier.semantics)` vs `CompositionLocalProvider`

**Pick:** **`Box(modifier = Modifier.semantics { testTagsAsResourceId = true })`**, scoped narrowly to `MainActivity.onCreate`. `@OptIn(ExperimentalComposeUiApi::class)` annotated on the call site only.

**Rejected — `CompositionLocalProvider`:** there is no canonical `CompositionLocal` for `testTagsAsResourceId`; the property is set via `Modifier.semantics`. Inventing a wrapper is more surface than the bug warrants.

**Rejected — project-wide opt-in via `freeCompilerArgs += "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi"`:** propagates the experimental marker to every `app/` Kotlin file. The bug is one file; keep the opt-in there.

**Cleanup follow-through:** PLANTPOTTING-0003 added per-screen `testTagsAsResourceId` opt-ins to `RecommendationScreen.kt` (and possibly elsewhere) as a stopgap. After the root bridge lands, those become redundant. Task §2.3 removes them. A grep for `testTagsAsResourceId` in `app/src/main/` should return exactly one hit (`MainActivity.kt`) after the fix.

**A11y note:** `testTagsAsResourceId = true` sets the platform `View.id` resource-name string. Talkback / a11y announcements read `contentDescription` and on-screen `Text`, **not** the resource-id. `contentDescription = "Capture plant photo"` on the shutter (set in `CameraScreen.kt:167`-area) remains authoritative for announcements. §3.2 nice-to-have a11y dump check is the empirical confirmation.

### 4.5 Real-model instrumentation test — concrete-class injection vs delete-the-swap

**Pick:** **inject the concrete `OnDevicePlantIdentifier` class** in the new `OnDeviceModelRealInterpreterTest`. Hilt builds the concrete class via its `@Inject` constructor regardless of any `@TestInstallIn(replaces = [OnDeviceIdentifyModule::class])` swap — the swap only replaces the `@Binds PlantIdentifier`, not the `@Singleton` concrete class. The whole production pipeline (`ImagePreprocessor`, `TfLiteInterpreterFacade`, `ModelScoreMapper`, real `.tflite`) is exercised.

**Rejected — delete the global `TestIdentifyModule` swap and migrate six existing instrumentation tests to local `@BindValue` fakes** (CODEX's plan): architecturally cleaner, but a six-test refactor with subtle Hilt-binding-edit pitfalls (singleton scope, `Activity.onCreate` injection order, `@JvmField` visibility). For a "narrow fix sprint matching the 0002 → 0001 shape" this is shape drift. **Deferred to PLANTPOTTING-0005**, paired with confidence calibration + UI polish work that already lives there.

**Rejected — `@UninstallModules(TestIdentifyModule::class)` + per-test re-bind:** PLANTPOTTING-0003 §B2 already documented that `@UninstallModules` rejects `@TestInstallIn` modules. Wouldn't compile.

**Rejected — pure unit test against the real `.tflite` (Robolectric or non-Android JVM):** TFLite native library doesn't load reliably under Robolectric. Instrumentation is the cheap path.

**Risk acceptance:** the concrete-class injection assumes Hilt's `@Inject` constructor registers the concrete class in the binding graph independently of any interface `@Binds` swap. This is documented Hilt behaviour but is not exercised in this codebase today. **Fallback (if assumption proves wrong):** a per-test `@TestInstallIn(replaces = [TestIdentifyModule::class])` module scoped to the one test class. ~15 lines, no architectural shift. Risk §7.8 captures this.

### 4.6 Seam invariants (non-negotiable; carried from 0003 §4.4)

- `interface PlantIdentifier { suspend fun identify(jpeg: ByteArray): IdentificationResult }` — byte-for-byte unchanged.
- `IdentificationResult` field list unchanged: `speciesId, displayName, source, lowConfidence`. (Note: there is no `scientificName` field — Gemini's draft asserted on this in the new test; correct field is `displayName`.)
- `StubPlantIdentifier.kt` stays under `app/src/main/java/com/darkfactory/plantpotting/identify/`; `bash scripts/check-stub-isolation.sh` stays green.
- `./gradlew verifyNoNetworking` stays green.
- `PlantIdentifierContractTest` (from PLANTPOTTING-0003 §0.4) stays green.

### 4.7 Contract-lock pattern — analogue of PLANTPOTTING-0003 §0.4

PLANTPOTTING-0003 §0.4's `PlantIdentifierContractTest` locked the `PlantIdentifier` seam before any edit. The analogue for this sprint locks **three** seams before production code moves:

- `ModelManifestDtypeContractTest` (JVM, Robolectric) — asserts `model_manifest.json` declares `input_dtype = "uint8"` and `ModelManifestReader.read().inputDtype == ModelDtype.UINT8`.
- `PreprocessedImageBufferShapeContractTest` (JVM, reflection) — asserts `PreprocessedImage` exposes `width: Int, height: Int, buffer: ByteBuffer`.
- `MainActivityTestTagsAsResourceIdTest` (Robolectric + Compose) — asserts the root semantics node carries `TestTagsAsResourceId = true`.

All three fail RED on `main` before any §1 / §2 edit lands.

---

## 5. Task list

TDD ordering: every behaviour change has a paired test task that lands RED first. The sprint-execute skill enforces ticking `- [x]` as work lands; do **not** batch.

### Phase 0 — Setup, contract locks, baseline

- [x] **0.1** Re-read `docs/sprints/drafts/PLANTPOTTING-0004-INTENT.md`, `docs/sprints/feedback/PLANTPOTTING-0003/feedback.md`, and the anchor files: `PlantIdentifier.kt`, `OnDevicePlantIdentifier.kt`, `ImagePreprocessor.kt`, `InterpreterFacade.kt`, `OnDeviceIdentifyModule.kt`, `MainActivity.kt`, `model_manifest.json`, `scripts/integration-flow.ps1`, `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`.
- [x] **0.2 (baseline)** Run `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` + `bash scripts/check-stub-isolation.sh` on clean `main` **before** any edits. Record outputs in `docs/sprints/results/PLANTPOTTING-0004.md`. Surface any baseline regression to the user before continuing.
- [x] **0.3** Update `docs/sprints/ledger.yaml`: `PLANTPOTTING-0004` `status: in-progress`, stamp `executor`, refresh `updated`.
- [x] **0.4 (test, RED first)** Add `ModelManifestDtypeContractTest` at `app/src/test/java/com/darkfactory/plantpotting/identify/model/ModelManifestDtypeContractTest.kt`. Two test methods: `manifestJsonDeclaresInputDtypeUint8` (parses asset JSON directly, asserts `input_dtype == "uint8"`); `parsedManifestExposesInputDtypeProperty` (uses `ModelManifestReader`, asserts `manifest.inputDtype == ModelDtype.UINT8`). RED today — field doesn't exist.
- [x] **0.5 (test, RED first)** Add `PreprocessedImageBufferShapeContractTest` at `app/src/test/java/com/darkfactory/plantpotting/identify/model/PreprocessedImageBufferShapeContractTest.kt`. Reflection-based: asserts `PreprocessedImage` declares `buffer: ByteBuffer` (not `normalisedRgb: FloatArray`). RED today.
- [x] **0.6 (test, RED first)** Add `MainActivityTestTagsAsResourceIdTest` at `app/src/test/java/com/darkfactory/plantpotting/MainActivityTestTagsAsResourceIdTest.kt` (Robolectric + `androidx.compose.ui.test.junit4.createComposeRule()`). Hosts the same composition `MainActivity.onCreate` sets. Asserts `composeRule.onRoot().fetchSemanticsNode().config[SemanticsProperties.TestTagsAsResourceId]` returns `true`. RED today.
- [x] **0.7 (test, RED first)** Add `ManifestRejectsInvalidInputDtypeTest` to `app/src/test/java/com/darkfactory/plantpotting/identify/model/ModelManifestTest.kt`. Asserts `ModelManifestReader.read(invalidJson)` throws when `input_dtype` is `"uin8"`, `""`, or `null`. RED today (no validation, no field).
- [x] **0.8 (test, RED first — instrumentation, no code)** Add a stub `OnDeviceModelRealInterpreterTest.kt` at `app/src/androidTest/java/com/darkfactory/plantpotting/identify/OnDeviceModelRealInterpreterTest.kt` with the test body fully defined per Decision §4.5; do **not** add the fixture JPEG yet. RED expectation: this test fails today with the existing Bug 1 error (`Cannot convert between a TensorFlowLite tensor with type UINT8 …`). The failure is the falsifiability evidence; record the captured failure message in the results doc as the "before" snapshot before §3 lands.

### Phase 1 — Bug 1 fix: dtype-aware manifest + preprocessor + facade (input + output)

- [x] **1.1** Add `input_dtype: "uint8"` to `app/src/main/assets/ml/aiy_plants_v1/model_manifest.json`. Document via `_comment_top` (or analogous existing field) that AIY V1/3 is fully quantized (UINT8 input *and* UINT8 output) — normalization is encoded in tensor quantization params, the `normalization` block is preserved as `_comment_normalization_unused_for_uint8` metadata only or removed outright (prefer removal). Re-stamp `acquisition_date` to today's ISO-8601.
- [x] **1.2** Introduce `enum class ModelDtype { UINT8, FLOAT32 }` in `app/src/main/java/com/darkfactory/plantpotting/identify/model/ModelManifest.kt`. Add `val inputDtype: ModelDtype` to `data class ModelManifest`. Update `ModelManifestReader.read()` to parse `input_dtype` from JSON. **No silent default for the production manifest** — missing or invalid values throw at parse time. (In-memory test fixtures constructed via the data-class constructor may pass any value; only the JSON parser rejects.)
- [x] **1.3** Refactor `PreprocessedImage` (currently at `app/src/main/java/com/darkfactory/plantpotting/identify/model/PreprocessedImage.kt`) to `data class PreprocessedImage(val width: Int, val height: Int, val buffer: ByteBuffer)`. Remove `normalisedRgb: FloatArray`.
- [x] **1.4 (test, RED first)** Rewrite `ImagePreprocessorTest.kt` (`app/src/test/java/com/darkfactory/plantpotting/identify/model/ImagePreprocessorTest.kt`):
  - **Drop** `whitePixelNormalisesToOne` and `midGreyPixelNormalisesToZero` (assume FLOAT32 normalization, no longer applies for shipped UINT8 model).
  - **Add** `uint8ManifestProducesUint8BufferAndSkipsNormaliseOp` — constructs in-memory `ModelManifest(inputDtype = UINT8)`, asserts `buffer.capacity() == 224 * 224 * 3` (UINT8: one byte per channel), pure-white JPEG yields bytes ≈ 0xFF, mid-grey ≈ 0x80.
  - **Add** `float32ManifestKeepsNormaliseOp` — in-memory `ModelManifest(inputDtype = FLOAT32)`, asserts `buffer.capacity() == 224 * 224 * 3 * 4` (FLOAT32: four bytes per channel), white-pixel floats ≈ 1.0 after `mean=127.5/std=127.5` normalization.
  - **Keep** `preprocessOutputsManifestInputSize`, `gradientJpegProducesNonConstantTensor`, `emptyJpegBytesThrowIdentificationFailure`.
- [x] **1.5** Refactor `ImagePreprocessor.kt:30-56`:
  - Branch the `TensorImage`: `TensorImage(if (manifest.inputDtype == ModelDtype.UINT8) DataType.UINT8 else DataType.FLOAT32)`.
  - Build the `ImageProcessor` with `ResizeOp` always; append `NormalizeOp(manifest.normalization.mean, manifest.normalization.std)` **only** on the FLOAT32 path.
  - Return `PreprocessedImage(manifest.inputSize, manifest.inputSize, processed.buffer)`.
- [x] **1.6 (test, RED first)** Rewrite `InterpreterFacadeTest.kt` (`app/src/test/java/com/darkfactory/plantpotting/identify/model/InterpreterFacadeTest.kt`):
  - **Add** `runInferenceAcceptsByteBuffer` — asserts the facade accepts a `PreprocessedImage` whose `buffer` is a `ByteBuffer`, the fake returns canned scores regardless of buffer shape.
  - **Add** `fakeInterpreterFacadeRecordsBufferCapacityForBothDtypes` — proves the test fake is dtype-aware enough for `ModelScoreMapperTest` etc. to keep working unchanged.
  - **Defer** the load-time mismatch behaviour test (`loadTimeDtypeMismatchThrowsIdentificationFailure`) to instrumentation (§3 below) — JVM-side, instantiating the real `Interpreter` is unreliable.
- [x] **1.7** Refactor `TfLiteInterpreterFacade` (real impl, in `app/src/main/java/com/darkfactory/plantpotting/identify/model/InterpreterFacade.kt`):
  - Take `manifest: ModelManifest` (or just `expectedInputDtype: ModelDtype`) at construction.
  - In `loadInterpreter()`: after `Interpreter(buffer)` succeeds, read `interpreter.getInputTensor(0).dataType()`, assert matches `expectedInputDtype`. Mismatch → throw `IdentificationFailureException("Model input dtype mismatch: manifest=<x>, runtime=<y>")`.
  - In `runInference`: replace `reshape4d(input.normalisedRgb)` with a single `interpreter.run(input.buffer, output)`.
- [x] **1.8** Update `FakeInterpreterFacade` so it ignores the buffer's contents (returns canned `FloatArray` scores), but records `inputBufferCapacity` and `expectedInputDtype` for `InterpreterFacadeTest` introspection. The fake's contract to `ModelScoreMapper` is unchanged — same `FloatArray` shape regardless of input dtype.
- [x] **1.9 (UINT8 output dequantization — the load-bearing addition)** In `TfLiteInterpreterFacade.runInference`, branch on `manifest.inputDtype` (assume input/output dtypes match for AIY V1/3; cross-check via `getOutputTensor(0).dataType()` if cheap):
  - **UINT8 path:** declare `val output = Array(1) { ByteArray(labelCount) }`. After `run`, read `val qp = interpreter.getOutputTensor(0).quantizationParams()` (or equivalent). Convert each byte: `floats[i] = (output[0][i].toUByte().toInt() - qp.zeroPoint) * qp.scale`. Return the `FloatArray`.
  - **FLOAT32 path:** unchanged — `val output = Array(1) { FloatArray(labelCount) }`, return `output[0]`.
  - **Inline comment** at the dequantization line: `// AIY V1/3 output tensor is UINT8; dequantize via the model's quantizationParams before ModelScoreMapper sees it.` (One short line. WHY-only.)
- [x] **1.10** Update `OnDeviceIdentifyProvidersModule.provideInterpreterFacade` in `app/src/main/java/com/darkfactory/plantpotting/identify/OnDeviceIdentifyModule.kt:62-72` (line range approximate — confirm before edit) to pass `manifest` (or `manifest.inputDtype`) into the `TfLiteInterpreterFacade` constructor.
- [x] **1.11 (verify)** `./gradlew --no-daemon testDebugUnitTest` green: all of §0.4, §0.5, §0.7, §1.4, §1.6 pass; `ModelScoreMapperTest`, `OnDevicePlantIdentifierFixturesTest` still green; `verifyNoNetworking` still green; `check-stub-isolation.sh` still green. Record commit hash + outputs in results doc.

### Phase 2 — Bug 2 fix: testTagsAsResourceId bridge + cleanup

Phase 2 runs **fully in parallel** with Phase 1 — touches disjoint files (`MainActivity.kt`, `RecommendationScreen.kt`).

- [x] **2.1** Edit `app/src/main/java/com/darkfactory/plantpotting/MainActivity.kt:19-23`:
  ```kotlin
  setContent {
      PlantPottingTheme {
          @OptIn(ExperimentalComposeUiApi::class)
          Box(modifier = Modifier.semantics { testTagsAsResourceId = true }) {
              PlantPottingNavHost()
          }
      }
  }
  ```
  Add the imports: `androidx.compose.foundation.layout.Box`, `androidx.compose.ui.ExperimentalComposeUiApi`, `androidx.compose.ui.Modifier`, `androidx.compose.ui.semantics.semantics`, `androidx.compose.ui.semantics.testTagsAsResourceId`. No project-wide `freeCompilerArgs += "-opt-in=..."`.
- [x] **2.2 (verify)** `MainActivityTestTagsAsResourceIdTest` (added at §0.6 RED) is now green.
- [x] **2.3** Audit `app/src/main/java/com/darkfactory/plantpotting/` for redundant per-screen `testTagsAsResourceId` opt-ins. Known sites from prior sprint work: `RecommendationScreen.kt:22-27`, `:33`, `:46-50`. Remove these and their imports of `ExperimentalComposeUiApi` / `semantics` / `testTagsAsResourceId` where the only use is the removed code. Grep for `testTagsAsResourceId` in `app/src/main/` — exactly one hit remains (`MainActivity.kt`).
- [ ] **2.4 (verify, must run against device)** With Phase 2 fix applied, `assembleDebug` + `adb install -r` complete, app launched: `adb shell uiautomator dump /sdcard/dump.xml && adb pull /sdcard/dump.xml ./tmp-dump.xml`. Inspect: at minimum, shutter node has `resource-id="camera.shutter"` non-empty AND `content-desc="Capture plant photo"` still present. Record the one-liner verification in the results doc. (Phase 4 transcripts are the formal evidence; §2.4 is the smoke check while Phase 1 is still in flight.)

### Phase 3 — Close the test gap: real-model instrumentation test

- [x] **3.1** Bundle one fixture JPEG at `app/src/androidTest/assets/identify-fixtures/monstera-deliciosa.jpg`. Creative-Commons-licensed, ~50–150 KB, recognisable Monstera deliciosa photo. Add the licence note to `app/src/androidTest/assets/identify-fixtures/LICENSE.txt`. (Per Risk 7.6: this is a smoke fixture, not an accuracy fixture; the test does not assert on `speciesId`.)
- [x] **3.2** Flesh out the body of `OnDeviceModelRealInterpreterTest.kt` (stub added at §0.8):
  ```kotlin
  @HiltAndroidTest
  class OnDeviceModelRealInterpreterTest {
      @get:Rule val hiltRule = HiltAndroidRule(this)
      @Inject lateinit var identifier: OnDevicePlantIdentifier  // concrete class, not interface

      @Before fun init() { hiltRule.inject() }

      @Test
      fun realJpegFedThroughRealInterpreterDoesNotThrow() = runBlocking {
          val ctx = InstrumentationRegistry.getInstrumentation().context
          val bytes = ctx.assets.open("identify-fixtures/monstera-deliciosa.jpg").use { it.readBytes() }
          val result = identifier.identify(bytes)
          assertThat(result.source).isEqualTo(IdSource.ON_DEVICE_MODEL)
          // No assertion on speciesId — AIY V1/3 maps only 2 of 16 KB species verbatim,
          // so the result may legitimately route to lowConfidence = true. The falsifiable
          // claim is "returns without throwing" — Bug 1's failure mode.
      }
  }
  ```
  `@Inject lateinit var identifier: OnDevicePlantIdentifier` (the concrete class, not `PlantIdentifier` interface) sidesteps `TestIdentifyModule.replaces = [OnDeviceIdentifyModule]` per Decision §4.5.
- [x] **3.3 (verify)** `./gradlew --no-daemon :app:compileDebugAndroidTestKotlin` green (compile-time check before §4 hits the GMD).
- [ ] **3.4 (verify, fallback wiring if needed)** If the concrete-class injection assumption fails (Risk 7.8 fires), add a one-off `OnDeviceModelRealInterpreterModule.kt` in `app/src/androidTest/` annotated `@TestInstallIn(replaces = [TestIdentifyModule::class], components = [SingletonComponent::class])` that re-binds `OnDevicePlantIdentifier` to `PlantIdentifier`. Limit the scope of this module to the new test class (via a custom test-runner pattern if Hilt allows, or accept that this test class is the only consumer of the real binding for this sprint). Document the fallback in the results doc.

### Phase 4 — Device-aware transcripts + GMD final-verify

Phase 4 requires a live `Pixel_6_API_34` emulator. **If unavailable, surface to the user immediately** — this is the headline acceptance bar and must not be silently deferred a second time.

- [x] **4.1** Boot a clean `Pixel_6_API_34` emulator (cold start). Run `pwsh ./scripts/integration-flow.ps1` (no `-BuildOnly`). Tee output to `docs/sprints/evidence/PLANTPOTTING-0004/transcript-A-cold.txt`. Expected final line: `Integration manifest diff passed.`
- [x] **4.2** `adb shell am force-stop com.darkfactory.plantpotting`. Re-run `pwsh ./scripts/integration-flow.ps1` against the now-warm emulator. Tee to `docs/sprints/evidence/PLANTPOTTING-0004/transcript-B-warm.txt`. Same expected outcome.
- [x] **4.3** Run `pwsh ./scripts/integration-flow.ps1 -BuildOnly`. Tee to `docs/sprints/evidence/PLANTPOTTING-0004/transcript-C-buildonly.txt`. Expected: `Integration manifest diff passed.` against `docs/sprints/expected-artifacts/PLANTPOTTING-0001-buildonly.txt`.
- [x] **4.4** Run `./gradlew --no-daemon pixel6Api34DebugAndroidTest`. Expect green, including the new `OnDeviceModelRealInterpreterTest`. Tee to `docs/sprints/evidence/PLANTPOTTING-0004/gmd-output.txt`.
- [x] **4.5 (expected-artifact policy)** Re-inspect `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`. It currently lists `source-badge=on-device match` and `model-asset-present=true` (added by PLANTPOTTING-0003 §7.4). If the captured transcripts surface a different `source-badge` (e.g. `on-device match (low confidence)` because the Monstera fixture routes to the low-conf path), update the expected file with the observed deterministic value and document why in the results doc. **Do not weaken expected-artifact requirements** to make the script pass — adding `(low confidence)` is honest; removing the `source-badge=` line is shape drift.
- [x] **4.6 (falsifiability evidence)** On a feature branch, revert §1.5's `TensorImage` branch (force back to `DataType.FLOAT32`). Re-run `OnDeviceModelRealInterpreterTest`; capture the failure message. Confirm it matches the Bug 1 error verbatim. Record in results doc as proof the test catches Bug 1. Revert the revert before merging.

### Phase 5 — Docs, ledger, sprint close

- [ ] **5.1** Author `docs/sprints/results/PLANTPOTTING-0004.md` (same shape as 0001/0002/0003 results docs):
  - §0 baseline outputs + §0.4/§0.5/§0.6 contract-test RED-then-green status.
  - §1 Bug 1 fix: diff summary (manifest field, preprocessor branch, `PreprocessedImage` → `ByteBuffer`, facade dtype assertion + **output dequantization**), §1.4 / §1.6 test-rename notes.
  - §2 Bug 2 fix: the four-line `MainActivity` diff; the cleanup list (`RecommendationScreen.kt`); the §2.4 dump verification line.
  - §3 real-model instrumentation test: file paths, one paragraph on what the test asserts and why concrete-class injection bypasses `TestIdentifyModule`; the §3.4 fallback (if it fired).
  - §4 transcripts + GMD: links to the four new files under `docs/sprints/evidence/PLANTPOTTING-0004/`; §4.5 expected-artifact policy notes; §4.6 falsifiability evidence (Bug 1 error reproduced on the revert branch).
  - §5 known gaps / handoff for PLANTPOTTING-0005:
    - **Full `TestIdentifyModule` global-removal + six-test `@BindValue` migration** (deferred from this sprint per Decision §4.5).
    - Confidence calibration / per-class threshold tuning.
    - `CameraUiState.Failure` UI polish (Snackbar / banner / dedicated `CaptureFailedScreen`).
    - `LowConfidenceFlowTest` instrumentation (B3 from 0003).
    - Re-enabling `PermissionDeniedFlowTest` (`@Ignore` since 0001).
    - INT8 quant variant work / GPU / NNAPI delegate.
  - §6 final-verify table (commands + outputs + commit hash).
- [ ] **5.2** Update `docs/sprints/ledger.yaml`: `PLANTPOTTING-0004` `status: done`, refresh `updated`.
- [ ] **5.3 (final verify)** Run the full gate chain on the final commit:
  - `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking`
  - `bash scripts/check-stub-isolation.sh`
  - `./gradlew --no-daemon pixel6Api34DebugAndroidTest`
  - `pwsh ./scripts/integration-flow.ps1` (device-aware)
  - `pwsh ./scripts/integration-flow.ps1 -BuildOnly`
  Record outputs in §5.1's results doc.

---

## 6. Sequencing and dependency rules

```
Phase 0 (setup + contract locks + baseline)
   │
   ├──► Phase 1 (Bug 1: manifest + preprocessor + facade input + facade OUTPUT dequant)
   │      │
   │      └──► Phase 3 (real-model instrumentation test — needs the buffer pipeline working
   │             end-to-end against the real .tflite; can be stubbed at §0.8 earlier for the
   │             "before" failure snapshot)
   │
   └──► Phase 2 (Bug 2: testTagsAsResourceId bridge + per-screen cleanup — fully parallel
          with Phase 1, touches disjoint files)

Phase 4 (transcripts + GMD final-verify) — depends on Phase 1 + 2 + 3 green
Phase 5 (docs + ledger) — depends on Phase 4 green
```

### 6.1 Hard gates

- **§0.4, §0.5, §0.6, §0.7, §0.8 RED before any production edit lands.** Standard contract-lock pattern from 0003 §6.1, broadened.
- **§1.4 RED before §1.5** (`ImagePreprocessor` branch).
- **§1.6 RED before §1.7** (`TfLiteInterpreterFacade` refactor + dtype assertion).
- **§0.6 RED before §2.1** (`MainActivity` wrap).
- **`bash scripts/check-stub-isolation.sh` and `./gradlew verifyNoNetworking` green throughout the sprint.** Any task that breaks either is a sprint-blocker bug.
- **§4.6 falsifiability evidence captured before §5.2 ledger close.** Without proof the new test catches Bug-1-shaped regressions, the test-gap-closure goal isn't satisfied.

### 6.2 Soft parallels

- Phase 1 (Bug 1 fix) and Phase 2 (Bug 2 fix) run fully in parallel — disjoint files.
- §1.1 (manifest edit) and §1.3 (`PreprocessedImage` refactor) can interleave once §0.4 / §0.5 are RED.

### 6.3 De-scope order (when the implementer slips)

1. Drop §3.2 nice-to-have second fixture JPEG (blank/grey for the real-model low-conf path).
2. Drop §3.2 nice-to-have observed-probabilities recording.
3. Drop §3.2 nice-to-have a11y dump verification (rely on theory per §4.4 a11y note).
4. Drop §0.6 Robolectric Compose-UI test (rely on §2.4 manual dump + Phase 4 transcripts only) — but keep §0.4 / §0.5 / §0.7 contract locks.
5. **Last-resort:** if §3.4 fallback fires (concrete-class injection doesn't work as theorised), add the per-test `@TestInstallIn(replaces = [TestIdentifyModule::class])` rebind. ~15 lines, no further architectural shift.

**Never drop:** Phase 1 (Bug 1 fix — manifest, preprocessor branch, facade dtype assertion, **output dequantization**); Phase 2 (Bug 2 fix + redundant-opt-in cleanup); Phase 3 (real-model test, even if §3.4 fallback fires); Phase 4 cold + warm transcripts + GMD run; Phase 5 results doc + ledger close; the seam invariants (§4.6); `verifyNoNetworking` + `check-stub-isolation.sh` green.

---

## 7. Risks and mitigations

### 7.1 `ExperimentalComposeUiApi` opt-in surface

**Risk:** the experimental marker propagates if scoped too widely; future Compose minor-version bumps could change the API.
**Mitigation:** scope `@OptIn(ExperimentalComposeUiApi::class)` to the single `Box(...)` call site in `MainActivity.kt`. Do not add it to `freeCompilerArgs` project-wide. Grep for `ExperimentalComposeUiApi` in `app/src/main/` returns exactly one hit after the fix (§2.3 cleanup enforces this). The §0.6 unit test pins the runtime semantic so a breaking API change fails the test loudly.

### 7.2 `testTagsAsResourceId` leaks tag strings into a11y announcements

**Risk:** if `testTagsAsResourceId = true` surfaced tag strings in Talkback output, that would be a UX regression on a11y-enabled devices.
**Mitigation:** the Compose semantics property maps `testTag` → platform `View.id` resource-name. Talkback reads `contentDescription` and on-screen `Text`, not resource-id. PLANTPOTTING-0003 set `contentDescription` on every interactive Compose node (e.g. `CameraScreen.kt:167` shutter `contentDescription = "Capture plant photo"`); those remain authoritative for announcements. §2.4 + §3.2 nice-to-have a11y dump check is the empirical confirmation — assert tags appear only as `resource-id`, not in `text` or `content-desc`, and `content-desc` is preserved.

### 7.3 The real `Interpreter.run` test doesn't boot cleanly on the AOSP `pixel6Api34` GMD image

**Risk:** the GMD image is x86_64 — if TFLite's x86_64 native fails to load, the new `OnDeviceModelRealInterpreterTest` would `IdentificationFailureException` and falsely fail.
**Mitigation:** PLANTPOTTING-0003 §7.6 already verified the dep audit and the existing instrumentation source set builds against `tensorflow-lite`. The GMD has been booting `pixel6Api34DebugAndroidTest` with TFLite on the classpath since 0003 §1.4. The new test exercises the same native library. **If a future TFLite bump breaks this, the test failure itself is the signal — that's its job. Treat as a sprint blocker, not a de-scope.**

### 7.4 The `input_dtype` branch breaks `FakeInterpreterFacade`

**Risk:** the fake returns canned `FloatArray` scores. If the production code expects dequantised UINT8 output but the fake delivers raw `FloatArray`, score-mapper behaviour diverges between unit and instrumentation tests.
**Mitigation:** the fake's contract is "ignore the input, return canned float scores." **Dequantization happens inside `TfLiteInterpreterFacade.runInference`** (§1.9) — by the time `ModelScoreMapper` sees the `FloatArray`, dequantization has already happened. The fake's output is a `FloatArray` as before. Same shape, same mapping behaviour. `ModelScoreMapperTest` is untouched by this sprint. §1.8 explicitly verifies the fake remains contract-compatible.

### 7.5 Future FP16 model swap forgets to update `input_dtype`

**Risk:** someone drops a FP16 `.tflite` into `assets/ml/aiy_plants_v1/` without flipping `input_dtype` back to `"float32"`. The preprocessor would feed UINT8 bytes into a FLOAT32 tensor and TFLite would throw the **mirror image** of today's Bug 1 error.
**Mitigation:** §1.7's load-time dtype assertion in `TfLiteInterpreterFacade.loadInterpreter()`. The interpreter's runtime `getInputTensor(0).dataType()` is the authoritative truth; comparing it to `manifest.inputDtype` at load time means a mismatch throws `IdentificationFailureException` on the **first** `identify(jpeg)` call — same shape of error as Bug 1, but with a much more diagnostic message: `"Model input dtype mismatch: manifest=UINT8, runtime=FLOAT32"`. §3.2's real-model instrumentation test catches it in CI before reaching the user. §0.7's invalid-dtype rejection catches typos before runtime.

### 7.6 The new real-model test's "doesn't throw" assertion is too loose

**Risk:** the test asserts only that `identify(bytes)` returns without throwing — a future regression that produces *wrong* results would still pass.
**Mitigation:** by design. The §3.2 nice-to-have records observed top-1 / top-3 probabilities in the results doc as evidence (per the 0003 §3.8 pattern). Confidence calibration / accuracy gating belongs in PLANTPOTTING-0005. The point of *this* sprint's test is to close the gap that allowed Bug 1 to ship — Bug 1 was a hard failure (exception), not a soft accuracy regression. The "doesn't throw + source == ON_DEVICE_MODEL" assertion is exactly the falsifiable claim that catches Bug-1-shaped regressions. §4.6 proves this empirically by reverting the fix and observing the test fail with the Bug 1 message.

### 7.7 Windows sandbox filesystem overlay (per project auto-memory)

**Risk:** shell-tool writes outside the project tree may not reach disk on this machine.
**Mitigation:** every artefact in this sprint lands under `D:/DarkFactoryProject/Plant potting/`. The fixture JPEG (§3.1), the transcripts (§4.1–§4.3), the results doc (§5.1) — all repo-relative. Implementer verifies file presence from the user's terminal (`ls docs/sprints/evidence/PLANTPOTTING-0004/`) before claiming acceptance.

### 7.8 `@HiltAndroidTest` concrete-class injection assumption is wrong

**Risk:** Decision §4.5 assumes Hilt builds the concrete `OnDevicePlantIdentifier` on demand via its `@Inject` constructor regardless of the `@Binds` swap on the interface. If wrong, the new test injects a fake (defeating its purpose) or fails to compile.
**Mitigation:** Hilt's documented behaviour matches the assumption — `@Inject` constructors register the concrete class in the binding graph independently of any interface `@Binds`. The `@TestInstallIn(replaces = [OnDeviceIdentifyModule::class])` swap removes the interface binding only. §3.3's compile check is the first signal. §4.4's GMD run is the second. If the test injects a fake despite the design (detectable via a `assertThat(identifier::class.java).isEqualTo(OnDevicePlantIdentifier::class.java)` check added to the test), §3.4 fallback fires: a per-test `@TestInstallIn(replaces = [TestIdentifyModule::class])` rebind module. ~15 lines, scoped to this test class only.

### 7.9 Dequantization adds a hot-path cost

**Risk:** dequantising 2102 bytes to floats on every capture is cheap but not free; a future model with a 10k-class output could add measurable latency.
**Mitigation:** 2102 byte-to-float conversions are sub-millisecond on a modern ARM core. The 0003 §7.3 cold-capture budget of 3 s has comfortable headroom. Re-measure as evidence in the results doc only if §4.4 GMD output suggests a regression.

### 7.10 No live emulator on the implementer's machine

**Risk:** Phase 4 transcripts require a live `Pixel_6_API_34`. If not available, the headline acceptance bar slips a second time.
**Mitigation:** the implementer surfaces to the user immediately upon discovering no emulator. Do **not** silently defer §7.5-shape transcripts a second time — the entire raison-d'être of this sprint is to capture them. If the user can boot the emulator, the implementer pauses and resumes. If not, this sprint cannot legitimately close — escalate the planning, do not paper over.

### 7.11 The Monstera fixture routes to low-conf instead of high-conf

**Risk:** AIY V1/3's vocabulary maps only 2 of 16 KB species verbatim (`Monstera deliciosa`, `Crassula ovata`, per `_comment_coverage` in the manifest). A real Monstera deliciosa photo *should* land in the high-conf path, but if the upstream model's threshold for that label is finicky on an emulator-rendered virtual-scene fixture, the result may route low-conf — the device-aware script then sees `source-badge=on-device match (low confidence)`.
**Mitigation:** §4.5 expected-artifact policy — if the captured transcripts surface a low-conf badge for the fixture path, update the expected manifest with the observed deterministic value and document why. The script reaching `ResultScreen` / `LowConfidencePicker` is the success bar, not which one. The test's hard assertion is `source == ON_DEVICE_MODEL`, not high-vs-low confidence.

---

## 8. Acceptance criteria

The sprint is done when **every** statement below is observably true.

### 8.1 Build + lint + gates

- [ ] `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` green on a clean clone.
- [ ] `bash scripts/check-stub-isolation.sh` green.
- [ ] `./gradlew --no-daemon pixel6Api34DebugAndroidTest` green (including `OnDeviceModelRealInterpreterTest`).

### 8.2 Seam invariants (still locked from 0003 §4.4)

- [ ] `PlantIdentifier` interface byte-for-byte unchanged from PLANTPOTTING-0003 close. `git diff ac120c9 HEAD -- app/src/main/java/com/darkfactory/plantpotting/identify/PlantIdentifier.kt` returns empty.
- [ ] `IdentificationResult` field list unchanged (`speciesId, displayName, source, lowConfidence`).
- [ ] `PlantIdentifierContractTest` (from PLANTPOTTING-0003 §0.4) still green.
- [ ] `StubPlantIdentifier.kt` still under `app/src/main/java/com/darkfactory/plantpotting/identify/`. `grep -R "StubPlantIdentifier" app/src/main/` returns only paths under `app/src/main/.../identify/`.

### 8.3 Bug 1 fix (UINT8 input + UINT8 output dequantization)

- [ ] `app/src/main/assets/ml/aiy_plants_v1/model_manifest.json` declares `"input_dtype": "uint8"`.
- [ ] `ModelManifestReader` parses `input_dtype` into `ModelDtype.UINT8`; `ModelManifestDtypeContractTest` green.
- [ ] `ModelManifestReader` rejects `input_dtype` values outside `"uint8"` / `"float32"` and rejects missing values for the production manifest; `ManifestRejectsInvalidInputDtypeTest` green.
- [ ] `model_manifest.json` no longer declares an active `normalization` stanza for the shipped UINT8 model.
- [ ] `ImagePreprocessor.preprocess(jpeg)` for the shipped manifest returns `PreprocessedImage` whose `buffer.capacity() == 224 * 224 * 3` (UINT8, not 224*224*3*4).
- [ ] `PreprocessedImage` declares `buffer: ByteBuffer`; `PreprocessedImageBufferShapeContractTest` green.
- [ ] `TfLiteInterpreterFacade.loadInterpreter()` throws `IdentificationFailureException` if `interpreter.getInputTensor(0).dataType()` disagrees with `manifest.inputDtype`. Verified by the §3.2 instrumentation test (against the real interpreter) — manifest-vs-runtime agreement is observable on the GMD.
- [ ] `TfLiteInterpreterFacade.runInference` dequantises the UINT8 output tensor via `interpreter.getOutputTensor(0).quantizationParams()` before returning a `FloatArray` to `ModelScoreMapper`. Inline comment explains *why*.
- [ ] **On a live emulator:** shutter capture no longer surfaces the `Cannot convert between a TensorFlowLite tensor with type UINT8 …` failure. User reaches `ResultScreen` or `LowConfidencePicker` on every capture.

### 8.4 Bug 2 fix (testTagsAsResourceId bridge + cleanup)

- [ ] `MainActivity.onCreate` wraps `PlantPottingNavHost()` in `Box(modifier = Modifier.semantics { testTagsAsResourceId = true })` opted-into `ExperimentalComposeUiApi`.
- [ ] `MainActivityTestTagsAsResourceIdTest` green (Robolectric).
- [ ] `grep -R "testTagsAsResourceId" app/src/main/` returns exactly one hit: `MainActivity.kt`. Per-screen opt-ins in `RecommendationScreen.kt` removed.
- [ ] **On a live emulator:** `adb shell uiautomator dump` of the camera screen surfaces `resource-id="camera.shutter"` non-empty, and `content-desc="Capture plant photo"` is preserved. Recorded in `transcript-A-cold.txt` (the script's `Wait-ForNode -resourceId "camera.shutter"` step passes without timing out).
- [ ] `uiautomator dump`s of at least three screens (Camera, Result, Recommendation) expose populated `resource-id` attributes — broader than just the shutter.

### 8.5 Test gap closure (real-model instrumentation test)

- [ ] `app/src/androidTest/java/com/darkfactory/plantpotting/identify/OnDeviceModelRealInterpreterTest.kt` exists; injects the concrete `OnDevicePlantIdentifier`; runs `identify(realJpegBytes)` against `app/src/androidTest/assets/identify-fixtures/monstera-deliciosa.jpg`; asserts `source == IdSource.ON_DEVICE_MODEL` without throwing.
- [ ] Test green on `pixel6Api34DebugAndroidTest`.
- [ ] **Falsifiability proof (§4.6):** reverting `ImagePreprocessor`'s `TensorImage` branch to FLOAT32 on a feature branch reproduces the exact Bug 1 error message in `OnDeviceModelRealInterpreterTest`. Documented in `docs/sprints/results/PLANTPOTTING-0004.md` §4.
- [ ] If §3.4 fallback fires, the per-test `@TestInstallIn(replaces = [TestIdentifyModule::class])` module is the only added Hilt-module change in this sprint.

### 8.6 §7.5 transcripts (the headline carry-forward from 0003)

- [ ] `docs/sprints/evidence/PLANTPOTTING-0004/transcript-A-cold.txt` captured from a cold-boot emulator run. Final line: `Integration manifest diff passed.`
- [ ] `docs/sprints/evidence/PLANTPOTTING-0004/transcript-B-warm.txt` captured from a warm re-run after `am force-stop`. Final line: `Integration manifest diff passed.`
- [ ] `docs/sprints/evidence/PLANTPOTTING-0004/transcript-C-buildonly.txt` captured. Build-only diff against `expected-artifacts/PLANTPOTTING-0001-buildonly.txt` passes.
- [ ] `docs/sprints/evidence/PLANTPOTTING-0004/gmd-output.txt` captures `pixel6Api34DebugAndroidTest` green.
- [ ] If `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` is updated (e.g. badge string change), the change is justified in writing in the results doc and does **not** weaken the device-aware requirements (`source-badge=…`, `model-asset-present=true`, screenshot count, archetype, recipe row count all preserved or improved).

### 8.7 Docs + ledger

- [ ] `docs/sprints/results/PLANTPOTTING-0004.md` exists with §0 baseline / §1 Bug 1 / §2 Bug 2 / §3 real-model test / §4 transcripts + falsifiability / §5 carry-forward / §6 final-verify sections.
- [ ] `docs/sprints/ledger.yaml`: `PLANTPOTTING-0004` `status: done`, `updated` refreshed.
- [ ] Every must-land §3.1 task `- [x]` in this file.
- [ ] No edits to prior sprint plan documents (`PLANTPOTTING-0001.md`, `PLANTPOTTING-0002.md`, `PLANTPOTTING-0003.md`).
- [ ] Carry-forward to PLANTPOTTING-0005 explicitly lists: full `TestIdentifyModule` removal + six-test `@BindValue` migration; confidence calibration; `CameraUiState.Failure` UI polish; `LowConfidenceFlowTest`; `PermissionDeniedFlowTest` un-`@Ignore`; INT8 stretch / GPU / NNAPI.

---

## 9. Handoff note for the `sprint-execute` implementer

You are one of `opus`, `gpt-5.4`, or `gemini`, picked by the user.

- **Read this plan first**, then `docs/sprints/drafts/PLANTPOTTING-0004-INTENT.md`, then `docs/sprints/feedback/PLANTPOTTING-0003/feedback.md` (the primary bug source).
- **Tick `- [ ]` boxes as you go**, not in batches. The sprint-execute skill enforces this.
- **TDD is mandatory** on every `(test, RED first)` task — §0.4 / §0.5 / §0.6 / §0.7 / §0.8 / §1.4 / §1.6.
- **The four must-land outcomes from §1 are non-negotiable.** If you slip, use §6.3's de-scope order — never drop Phase 1, 2, 3, 4, 5 entirely.
- **Do not change `PlantIdentifier`'s interface shape or `IdentificationResult`'s field list.** PLANTPOTTING-0003 §0.4's contract test enforces this. The result type's `displayName` field (not `scientificName`) is the species name — Gemini's draft had this wrong.
- **Do not delete `StubPlantIdentifier.kt`** — stays under `identify/` for `check-stub-isolation.sh` and test consumption.
- **Do not edit prior sprint plan documents** (PLANTPOTTING-0001/0002/0003 .md). Record carry-forward closure in `results/PLANTPOTTING-0004.md` only.
- **UINT8 output dequantization is must-land, not nice-to-have.** §1.9 is the load-bearing addition all three drafts under-budgeted. If you fix the input contract but not the output, `Interpreter.run` returns without throwing but `ModelScoreMapper` sees garbage — the §3.2 test still passes ("doesn't throw"), and you ship a different silent bug. Read Decision §4.3 in full.
- **Phase 1 and Phase 2 run in parallel** — disjoint files. Don't let the bigger Phase 1 refactor block the four-line `MainActivity` edit.
- **Phase 4 transcripts require a live emulator.** If you don't have one, surface to the user immediately — the entire point of this sprint is to capture them. **Do not silently defer a second time.**
- **If the §4.5 concrete-class injection assumption proves wrong** (Risk 7.8 fires), §3.4 fallback is the rescue: a per-test `@TestInstallIn(replaces = [TestIdentifyModule::class])` module scoped to the one test. ~15 lines, no architectural shift.
- **Sandbox warning** (per the user's auto-memory): shell-tool writes outside `D:/DarkFactoryProject/Plant potting/` may not reach disk on this machine. Verify file presence from the user's terminal before claiming acceptance.
- **Falsifiability evidence (§4.6) is required before ledger close.** A test that says "doesn't throw" earns its acceptance only if you've proven it would have caught Bug 1. The revert-and-observe experiment takes ten minutes and closes the loop.
