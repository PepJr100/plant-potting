# PLANTPOTTING-0004 — Fix sprint for PLANTPOTTING-0003 (CLAUDE draft)

**Sprint window:** ~3–5 days for a single AI implementer (opus, gpt-5.4, or gemini — picked at `sprint-execute` time)
**Source review:** `docs/sprints/feedback/PLANTPOTTING-0003/feedback.md`, `docs/sprints/results/PLANTPOTTING-0003.md` §8 review.
**Shape:** same as PLANTPOTTING-0002 (fix sprint for 0001). Tight, surgical, four must-land outcomes. Confidence calibration + UI polish slide to PLANTPOTTING-0005.

---

## 1. Intent

Land the four must-land outcomes from `docs/sprints/drafts/PLANTPOTTING-0004-INTENT.md`:

1. **Bug 1 (P0)** — the real AIY Plants V1/3 UINT8 model rejects the FLOAT32 tensor that `ImagePreprocessor.kt:48` builds. Fix the preprocessor → facade pipeline so the input contract matches the shipped `.tflite`. Use a manifest field (`input_dtype`) as the policy source-of-truth, with a runtime sanity check at interpreter load time so a future FP16 swap can't silently regress.
2. **Bug 2 (P1)** — `MainActivity.kt:19-23` wraps `PlantPottingNavHost()` directly without `Modifier.semantics { testTagsAsResourceId = true }`, so uiautomator dumps surface every Compose node with `resource-id=""`. Wrap the nav host with a `Box(modifier = Modifier.semantics { testTagsAsResourceId = true })` opted into `ExperimentalComposeUiApi`.
3. **Close the test gap** that let Bug 1 ship. Add one `@HiltAndroidTest` instrumentation test that exercises the **real** `OnDevicePlantIdentifier` (not the `FakeFixedIdentifier`) against a checked-in fixture JPEG. Achieved by injecting the concrete `OnDevicePlantIdentifier` class directly — Hilt can construct it via its `@Inject` constructor regardless of the `TestIdentifyModule` `@TestInstallIn` swap, because the swap only replaces the `PlantIdentifier` `@Binds`, not the `@Singleton` `OnDevicePlantIdentifier` itself.
4. **Capture the §7.5 transcripts.** Re-run `pwsh ./scripts/integration-flow.ps1` cold + warm on the `Pixel_6_API_34` emulator, drop the transcripts under `docs/sprints/evidence/PLANTPOTTING-0004/`, and re-run `pixel6Api34DebugAndroidTest` against the GMD. The headline acceptance bar PLANTPOTTING-0003 deferred is the one this sprint clears.

---

## 2. Goals and non-goals

### 2.1 Goals (must-land)

- `pwsh ./scripts/integration-flow.ps1` (device-aware) produces a clean diff against `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` on a connected `Pixel_6_API_34` emulator. Two transcripts (cold + warm) checked in.
- `pixel6Api34DebugAndroidTest` green on the GMD, including the new real-model instrumentation test.
- Shutter on a real emulator no longer surfaces the TFLite `UINT8` ↔ `FLOAT32` error; the user reaches `ResultScreen` (high-conf path) or `LowConfidencePicker` (low-conf path) on every capture.
- `adb shell uiautomator dump` of the camera screen shows `resource-id="camera.shutter"` non-empty.
- The whole gate chain (`assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking`, `bash scripts/check-stub-isolation.sh`) stays green throughout the sprint.

### 2.2 Non-goals (deferred to PLANTPOTTING-0005 or later)

Verbatim from `PLANTPOTTING-0004-INTENT.md` "Non-goals":

- Confidence calibration / per-class threshold tuning.
- UI polish for `CameraUiState.Failure` (Snackbar / banner / dedicated `CaptureFailedScreen`). One-line carry-forward note in the results doc is enough.
- INT8 quant variant work beyond what Bug 1 needs (the shipped model is already INT8; this sprint handles it correctly, doesn't swap it).
- GPU / NNAPI delegate.
- Re-enabling `PermissionDeniedFlowTest` (still `@Ignore` from PLANTPOTTING-0001).
- New `IdSource` values, new screens, new KB content edits.
- `LowConfidenceFlowTest` instrumentation (B3 from 0003 — defer unless it falls out trivially of the Bug 2 fix; it almost certainly won't).
- Any change to the `PlantIdentifier` interface shape or to `IdentificationResult`'s field list. The PLANTPOTTING-0003 §4.4 invariants still hold.
- AGP / Kotlin / Compose / Hilt / TFLite version bumps.

---

## 3. Scope boundaries

### 3.1 Must-land

- `input_dtype` field on `model_manifest.json` and `ModelManifest`, parsed by `ModelManifestReader`.
- `ImagePreprocessor` branches `TensorImage(DataType.UINT8)` vs `TensorImage(DataType.FLOAT32)` based on the manifest. Skips the `NormalizeOp` on the UINT8 path; keeps it on the FLOAT32 path.
- `PreprocessedImage` carries a `ByteBuffer` instead of a `FloatArray`. The interpreter consumes the buffer directly via `interpreter.run(buffer, output)`.
- `TfLiteInterpreterFacade` reads `interpreter.getInputTensor(0).dataType()` at load time and throws `IdentificationFailureException` if it doesn't match the manifest's declared `input_dtype` (catches future FP16-swap mistakes).
- `TfLiteInterpreterFacade` dequantises the UINT8 output tensor to a `FloatArray` via the interpreter's `quantizationParams()` so `ModelScoreMapper` sees the same shape regardless of model dtype. (AIY V1/3 is fully quantised — UINT8 input *and* UINT8 output.)
- `MainActivity.kt` wraps the nav host with `Box(modifier = Modifier.semantics { testTagsAsResourceId = true })` under `@OptIn(ExperimentalComposeUiApi::class)`.
- New `@HiltAndroidTest` `OnDeviceModelRealInterpreterTest` that injects the concrete `OnDevicePlantIdentifier`, feeds it a checked-in fixture JPEG, and asserts the call returns without throwing and the result is `source = ON_DEVICE_MODEL`.
- Two transcripts captured: `docs/sprints/evidence/PLANTPOTTING-0004/transcript-A-cold.txt`, `transcript-B-warm.txt`. Clean diff against the device-aware expected manifest.
- Results doc `docs/sprints/results/PLANTPOTTING-0004.md` + ledger close to `done`.

### 3.2 Nice-to-have (only if time permits — do NOT slip the §3.1 list for these)

- A small Compose-UI unit test that asserts the root `SemanticsNode` exposes the `TestTagsAsResourceId` property (catches a future regression on the Bug 2 fix at the JVM layer, no emulator required).
- A second fixture JPEG in `OnDeviceModelRealInterpreterTest` — a blank/grey frame that exercises the low-confidence path against the real interpreter (high-conf path is already covered by the Monstera fixture, and the low-conf path is currently only exercised against the fake).
- Record observed top-1 / top-3 probabilities from the new real-model test in the results doc (evidence, not gate).

### 3.3 Out-of-scope (explicit deferrals — same as 0003 §3.3 plus the 0004 carry-forwards)

- Anything in §2.2 above.
- Reworking `OnDeviceIdentifyModule`'s split (Binds vs Providers) — the current split already lets us inject the concrete class without touching modules.
- Adding a Snackbar / banner / dedicated screen for `CameraUiState.Failure`. The failure-text-at-TopCenter UX is preserved as-is — it's legible, just easy to miss. Note carried forward to 0005.
- Persisting model metadata to disk.

---

## 4. Decisions (opinionated; alternatives documented for the merge audit)

### 4.1 `input_dtype` source — manifest field vs runtime introspection

**Pick:** **manifest-driven**. Add `"input_dtype": "uint8"` to `model_manifest.json`; parse into `ModelManifest.inputDtype: ModelDtype` (sealed type or enum). The preprocessor reads it at construction; the interpreter facade asserts the runtime tensor type matches the manifest at load time.

**Rejected alternative — pure runtime introspection** (`interpreter.getInputTensor(0).dataType()` only, no manifest field):
- Pro: zero asset change; "just works" against any model.
- Con: silently masks contract drift. A future FP16 swap that forgets to update the preprocessor would still "work" but produce wrong results. The manifest field is the editorial assertion that says "this model is meant to be UINT8" so a contradicting native dtype fails fast.
- Con: the unit-test path (`FakeInterpreterFacade` + Robolectric) never instantiates a real `Interpreter`, so runtime introspection isn't observable from JVM unit tests. The manifest field is.
- The intent doc independently picked manifest-driven; this draft agrees.

**Belt-and-braces:** keep both. Manifest is the policy source-of-truth; `TfLiteInterpreterFacade.loadInterpreter()` asserts the runtime dtype agrees. A mismatch throws `IdentificationFailureException("Model input dtype mismatch …")`. Cheap, defensive.

### 4.2 Compose root opt-in — `Box(Modifier.semantics)` vs `CompositionLocalProvider`

**Pick:** **`Box(modifier = Modifier.semantics { testTagsAsResourceId = true })`**, scoped narrowly to `MainActivity.onCreate`. `@OptIn(ExperimentalComposeUiApi::class)` annotated on the call site only.

**Rejected alternative — `CompositionLocalProvider`:** there is no canonical `CompositionLocal` for `testTagsAsResourceId`. The property is set via `Modifier.semantics`, not via a CompositionLocal. Suggesting one would mean inventing a wrapper, which is more surface than the bug warrants.

**Rejected alternative — project-wide `@OptIn` via `freeCompilerArgs += "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi"`:** propagates the experimental marker to every `app/` Kotlin file. The bug is one file; keep the opt-in there.

**Note on a11y:** `testTagsAsResourceId = true` sets the platform `View.id` resource-name string. The Talkback / a11y announcement path reads `contentDescription` and on-screen text, **not** the resource-id. The shutter retains `contentDescription = "Capture plant photo"` (set in `CameraScreen.kt:167`-area). Empirically: a11y announcements are unchanged. The Phase 2 test below asserts the semantics property is set; a separate manual a11y smoke-test isn't worth the time.

### 4.3 Where the real-model instrumentation test lives — androidTest vs gated source set

**Pick:** **`app/src/androidTest/java/com/darkfactory/plantpotting/identify/OnDeviceModelRealInterpreterTest.kt`**, plain `@HiltAndroidTest` in the existing source set. Injects the **concrete `OnDevicePlantIdentifier` class** (not the `PlantIdentifier` interface).

**Why this works without fighting the `TestIdentifyModule` swap:**
- `TestIdentifyModule` (`@TestInstallIn(replaces = [OnDeviceIdentifyModule::class])`) only replaces the `@Binds` from `OnDevicePlantIdentifier` → `PlantIdentifier`. It does NOT replace the `OnDeviceIdentifyProvidersModule` providers (manifest, labels, mapper, interpreter facade, dispatcher).
- `OnDevicePlantIdentifier` is `@Singleton @Inject constructor(...)`. Hilt can still build it on demand, the swap notwithstanding, because the swap only affects the `@Binds` graph node for the `PlantIdentifier` interface.
- The new test injects the concrete class directly: `@Inject lateinit var identifier: OnDevicePlantIdentifier`. The whole production pipeline (`ImagePreprocessor`, `TfLiteInterpreterFacade`, `ModelScoreMapper`, real `.tflite` from assets) is exercised.

**Rejected alternative — new gated source set** (e.g. `app/src/androidTestProd/`): adds a toolchain ceremony (sourceSets, dependencies, a new Gradle task wiring) that pays no dividend. The injection trick above gets us the same coverage with zero new build config.

**Rejected alternative — `@UninstallModules(TestIdentifyModule::class)` + a per-test re-bind:** PLANTPOTTING-0003 §B2 already documented that `@UninstallModules` rejects `@TestInstallIn` modules. Even if it worked, it's noisier than "inject the concrete".

**Rejected alternative — pure unit test against the real `.tflite`** (Robolectric or non-Android JVM): the TFLite native library doesn't load reliably under Robolectric. Instrumentation is the cheap path.

### 4.4 PreprocessedImage shape — `FloatArray` vs `ByteBuffer`

**Pick:** refactor `PreprocessedImage` to carry `val buffer: ByteBuffer` (and keep `width: Int, height: Int`). The TFLite Support `TensorImage` produces this buffer natively for both UINT8 and FLOAT32; the interpreter consumes it directly via `interpreter.run(buffer, output)`.

**Rejected alternative — keep `FloatArray` and convert in the facade:** would produce an awkward "FloatArray of bytes" representation for the UINT8 path, or duplicate normalization logic in the facade. Reject.

**Rejected alternative — sealed type `PreprocessedImage.Float / .UInt8`:** more surface, no clear pay-off. The ByteBuffer is already a sufficiently abstract input shape.

**Blast radius:** `PreprocessedImage` data class, `ImagePreprocessor.preprocess`, `TfLiteInterpreterFacade.runInference`, `FakeInterpreterFacade.runInference`, `ImagePreprocessorTest`, `InterpreterFacadeTest`. Bounded; ~80 lines of net diff.

### 4.5 §0.4 contract test analogue for this sprint

PLANTPOTTING-0003 §0.4's `PlantIdentifierContractTest` locked the `PlantIdentifier` seam **before** any edit. The analogue this sprint locks the **preprocessor → facade seam** before the dtype branch lands:

- `ModelManifestDtypeContractTest` (JVM, Robolectric) — asserts `model_manifest.json` declares `input_dtype = "uint8"` and that `ModelManifestReader.read().inputDtype == ModelDtype.UINT8`. Fails RED until §1 lands the manifest field + parser change.
- `PreprocessorFacadeSeamContractTest` (JVM) — asserts via reflection that `PreprocessedImage` exposes a `ByteBuffer`-typed property and that `InterpreterFacade.runInference(PreprocessedImage): FloatArray` is still the signature. Fails RED until §1's refactor lands.

The two tests are cheap, JVM-only, and lock the contract before any production code moves — same discipline as 0003's §0.4.

### 4.6 Seam invariants (still non-negotiable)

- `interface PlantIdentifier { suspend fun identify(jpeg: ByteArray): IdentificationResult }` — byte-for-byte unchanged.
- `IdentificationResult` fields unchanged (`speciesId, displayName, source, lowConfidence`).
- `StubPlantIdentifier.kt` stays under `app/src/main/java/com/darkfactory/plantpotting/identify/`; `bash scripts/check-stub-isolation.sh` green.
- `./gradlew verifyNoNetworking` green.

---

## 5. Task list

TDD ordering: every behaviour change has a paired test task that lands RED first.

### Phase 0 — Setup, contract lock, baseline

- [ ] **0.1** Re-read `docs/sprints/drafts/PLANTPOTTING-0004-INTENT.md`, `docs/sprints/feedback/PLANTPOTTING-0003/feedback.md`, and the anchor files in §Anchor files of the intent doc.
- [ ] **0.2 (baseline)** Run `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` + `bash scripts/check-stub-isolation.sh` on clean `main` **before** any edits. Record outputs in `docs/sprints/results/PLANTPOTTING-0004.md` as the evidence sink. Surface any baseline regression to the user before continuing.
- [ ] **0.3** Update `docs/sprints/ledger.yaml`: `PLANTPOTTING-0004` `status: in-progress`, stamp `executor`, refresh `updated`.
- [ ] **0.4 (test, RED first)** Add `ModelManifestDtypeContractTest` at `app/src/test/java/com/darkfactory/plantpotting/identify/model/ModelManifestDtypeContractTest.kt`. Two test methods: `manifestJsonDeclaresInputDtypeUint8` (parses the asset JSON directly, asserts `input_dtype == "uint8"`); `parsedManifestExposesInputDtypeProperty` (uses `ModelManifestReader` and asserts `manifest.inputDtype == ModelDtype.UINT8`). Fails RED today because neither the field nor the property exists.
- [ ] **0.5 (test, RED first)** Add `PreprocessorFacadeSeamContractTest` at `app/src/test/java/com/darkfactory/plantpotting/identify/model/PreprocessorFacadeSeamContractTest.kt`. Reflection-based: asserts `PreprocessedImage::class.java.declaredFields` contains a `java.nio.ByteBuffer` field named `buffer`; asserts `InterpreterFacade::class.java` declares `runInference(PreprocessedImage): FloatArray`. Fails RED today (current shape is `FloatArray`-backed).

### Phase 1 — Bug 1 fix: dtype-aware preprocessor + manifest + facade

- [ ] **1.1 (test, RED first)** Extend `ModelManifestTest` (`app/src/test/java/com/darkfactory/plantpotting/identify/ModelManifestTest.kt`) with `manifestDeclaresInputDtypeUint8MatchingShippedModel` — asserts the JSON's `input_dtype` field is `"uint8"`. (Lives alongside the existing eight tests; doesn't replace any.)
- [ ] **1.2** Add `input_dtype: "uint8"` to `app/src/main/assets/ml/aiy_plants_v1/model_manifest.json`. Document via the existing `_comment_top` field that the AIY V1/3 model is fully quantised (UINT8 input and UINT8 output) — normalisation is encoded in the tensor's quantisation params, so the `normalization` block in the manifest is preserved as editorial metadata but **unused** on the UINT8 path. Re-stamp `acquisition_date` to today (2026-05-15-ish) per the existing manifest pattern.
- [ ] **1.3** Introduce `enum class ModelDtype { UINT8, FLOAT32 }` (or sealed class) in `app/src/main/java/com/darkfactory/plantpotting/identify/model/ModelManifest.kt`. Add `val inputDtype: ModelDtype` to `data class ModelManifest`. Update `ModelManifestReader.read()` to parse `input_dtype` from JSON, defaulting to `FLOAT32` if absent (back-compat for tests).
- [ ] **1.4 (test, RED first)** Add `app/src/test/java/com/darkfactory/plantpotting/identify/model/PreprocessedImageBufferTest.kt`. Asserts: `PreprocessedImage` is constructible from a `(width: Int, height: Int, buffer: ByteBuffer)` triple; `buffer.capacity()` matches `width * height * 3` for UINT8 and `width * height * 3 * 4` for FLOAT32. RED today.
- [ ] **1.5** Refactor `PreprocessedImage` in `app/src/main/java/com/darkfactory/plantpotting/identify/model/PreprocessedImage.kt` (or wherever it currently lives — search and confirm before edit) to `data class PreprocessedImage(val width: Int, val height: Int, val buffer: ByteBuffer)`. Remove `normalisedRgb: FloatArray`.
- [ ] **1.6 (test, RED first)** Rewrite `ImagePreprocessorTest.kt` (`app/src/test/java/com/darkfactory/plantpotting/identify/model/ImagePreprocessorTest.kt`). Drop `whitePixelNormalisesToOne` and `midGreyPixelNormalisesToZero` — those assertions assume FLOAT32 normalisation, which no longer applies for the shipped UINT8 model. Replace with:
  - `uint8ManifestProducesUint8BufferAndSkipsNormaliseOp` (constructs an in-memory `ModelManifest` with `inputDtype = UINT8`, asserts `buffer.capacity() == 224*224*3` and that pure-white JPEG yields buffer bytes ≈ 0xFF, mid-grey yields ≈ 0x80; no normalisation applied).
  - `float32ManifestKeepsNormaliseOp` (constructs an in-memory manifest with `inputDtype = FLOAT32`, asserts `buffer.capacity() == 224*224*3*4` and that white-pixel float values are ≈ 1.0 after the mean=127.5/std=127.5 normalisation).
  - Keep `preprocessOutputsManifestInputSize`, `gradientJpegProducesNonConstantTensor`, `emptyJpegBytesThrowIdentificationFailure` — they still apply.
  RED before §1.7.
- [ ] **1.7** Refactor `ImagePreprocessor.kt:30-56`:
  - Branch the `TensorImage`: `TensorImage(if (manifest.inputDtype == ModelDtype.UINT8) DataType.UINT8 else DataType.FLOAT32)`.
  - Build the `ImageProcessor` with `ResizeOp` always, and `NormalizeOp(manifest.normalization.mean, manifest.normalization.std)` **only** on the FLOAT32 path. (For UINT8, normalisation is encoded in quantization params, not the preprocessing pipeline.)
  - Return `PreprocessedImage(manifest.inputSize, manifest.inputSize, processed.buffer)` — the `TensorImage.buffer` is the right shape for both dtypes.
- [ ] **1.8 (test, RED first)** Rewrite `InterpreterFacadeTest.kt` (or its current name — `app/src/test/java/com/darkfactory/plantpotting/identify/model/InterpreterFacadeTest.kt` if present; otherwise find it) to drive the new buffer-based contract. The `FakeInterpreterFacade` no longer cares about the buffer's contents (it returns canned scores), so most tests just need the constructor + score-return path updated. Add:
  - `runInferenceAcceptsByteBuffer` — asserts the facade accepts a `PreprocessedImage` whose `buffer` is a `ByteBuffer`.
  - `loadTimeDtypeMismatchThrowsIdentificationFailure` — this one is a behaviour test for the real `TfLiteInterpreterFacade` only; can be JVM-mocked by passing a stub `Interpreter`-equivalent (or skipped from JVM and exercised from the new instrumentation test in Phase 3).
- [ ] **1.9** Refactor `TfLiteInterpreterFacade` in `app/src/main/java/com/darkfactory/plantpotting/identify/model/InterpreterFacade.kt`:
  - Take `manifest: ModelManifest` (or at least `expectedInputDtype: ModelDtype`) as a constructor parameter so the load-time assertion has something to check against.
  - In `loadInterpreter()`, after `Interpreter(buffer)` succeeds: read `interpreter.getInputTensor(0).dataType()` and assert it agrees with `expectedInputDtype`. Throw `IdentificationFailureException("Model input dtype mismatch: manifest=<x>, runtime=<y>")` on disagreement. Same check for output if it's worth the few lines.
  - Replace the `reshape4d(input.normalisedRgb)` block in `runInference` with a single `interpreter.run(input.buffer, output)` call (the buffer is already in the right shape).
  - For the UINT8 output case: declare `val output = Array(1) { ByteArray(labelCount) }` instead of `FloatArray`. After `run`, dequantise: read `interpreter.getOutputTensor(0).quantizationParams()` (scale + zeroPoint); convert byte-by-byte to a `FloatArray` (`(byte.toUByte().toInt() - zeroPoint) * scale`). On the FLOAT32 path keep the existing `Array(1) { FloatArray(labelCount) }` shape.
  - Branch on `manifest.inputDtype` (or read the output tensor's dataType directly — pick one, document the choice inline).
- [ ] **1.10** Update `OnDeviceIdentifyProvidersModule.provideInterpreterFacade` in `app/src/main/java/com/darkfactory/plantpotting/identify/OnDeviceIdentifyModule.kt:62-72` to pass `manifest.inputDtype` (or the whole manifest) into the `TfLiteInterpreterFacade` constructor.
- [ ] **1.11 (verify)** `./gradlew testDebugUnitTest` green: all of §0.4, §0.5, §1.1, §1.4, §1.6, §1.8 tests pass; no regressions in `ModelScoreMapperTest`, `OnDevicePlantIdentifierFixturesTest`, etc. `./gradlew verifyNoNetworking` still green. `bash scripts/check-stub-isolation.sh` still green.

### Phase 2 — Bug 2 fix: testTagsAsResourceId bridge

- [ ] **2.1 (test, RED first)** Add `app/src/test/java/com/darkfactory/plantpotting/MainActivityTestTagsAsResourceIdTest.kt` (Robolectric + Compose UI test). Hosts the same composition that `MainActivity.onCreate` sets — `PlantPottingTheme { /* the new Box wrapper */ PlantPottingNavHost() }` — and asserts the root semantics node carries the `TestTagsAsResourceId` property set to `true`. Use `composeRule.onRoot().fetchSemanticsNode().config[SemanticsProperties.TestTagsAsResourceId]`. Fails RED today because the `Box` wrapper isn't there.
- [ ] **2.2** Edit `app/src/main/java/com/darkfactory/plantpotting/MainActivity.kt:19-23`:
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
  Add the four imports: `androidx.compose.foundation.layout.Box`, `androidx.compose.ui.ExperimentalComposeUiApi`, `androidx.compose.ui.Modifier`, `androidx.compose.ui.semantics.semantics`, `androidx.compose.ui.semantics.testTagsAsResourceId`. No project-wide opt-in.
- [ ] **2.3 (verify, must run against device)** With the fix applied and `assembleDebug` + `adb install -r` complete, run on a live emulator: `adb shell uiautomator dump /sdcard/dump.xml && adb pull /sdcard/dump.xml ./tmp-dump.xml`. Inspect `./tmp-dump.xml` — assert at least the shutter node has `resource-id="camera.shutter"` non-empty. Record the one-liner verification in the results doc. (This is the manual smoke check; the full transcript capture is Phase 4.)

### Phase 3 — Close the test gap: real-model instrumentation test

- [ ] **3.1** Bundle one fixture JPEG at `app/src/androidTest/assets/identify-fixtures/monstera-deliciosa.jpg` (Creative-Commons-licensed, ~50–150 KB). It must be a recognisable real-world Monstera deliciosa photo so a future maintainer can read the asset path and understand the intent. Copy the licence note to `app/src/androidTest/assets/identify-fixtures/LICENSE.txt`.
- [ ] **3.2 (test)** Add `app/src/androidTest/java/com/darkfactory/plantpotting/identify/OnDeviceModelRealInterpreterTest.kt`:
  ```kotlin
  @HiltAndroidTest
  class OnDeviceModelRealInterpreterTest {
      @get:Rule val hiltRule = HiltAndroidRule(this)
      @Inject lateinit var identifier: OnDevicePlantIdentifier

      @Before fun init() { hiltRule.inject() }

      @Test
      fun realJpegFedThroughRealInterpreterDoesNotThrow() = runBlocking {
          val ctx = InstrumentationRegistry.getInstrumentation().context
          val bytes = ctx.assets.open("identify-fixtures/monstera-deliciosa.jpg").use { it.readBytes() }
          val result = identifier.identify(bytes)
          assertThat(result.source).isEqualTo(IdSource.ON_DEVICE_MODEL)
          // No assertion on speciesId — AIY V1/3 maps only 2 of 16 KB species (see manifest
          // _comment_coverage), so the result may legitimately route to low-conf. The hard
          // assertion is "returns without throwing" — which is what Bug 1 explicitly broke.
      }
  }
  ```
  Use `@Inject lateinit var identifier: OnDevicePlantIdentifier` — the concrete class, not the `PlantIdentifier` interface. This sidesteps the `TestIdentifyModule.replaces = [OnDeviceIdentifyModule]` swap (per Decision §4.3).
- [ ] **3.3 (verify)** `./gradlew --no-daemon :app:compileDebugAndroidTestKotlin` green (compile-time check before §4 runs the GMD chain).

### Phase 4 — Device-aware transcripts + final verify

- [ ] **4.1** With Bugs 1 + 2 fixed, run `pwsh ./scripts/integration-flow.ps1` against a connected `Pixel_6_API_34` emulator from a **cold** boot. Tee output to `docs/sprints/evidence/PLANTPOTTING-0004/transcript-A-cold.txt`. Expected: `Integration manifest diff passed.` Clean diff against `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`.
- [ ] **4.2** Re-run `pwsh ./scripts/integration-flow.ps1` against the same (now warm) emulator after `adb shell am force-stop com.darkfactory.plantpotting`. Tee to `docs/sprints/evidence/PLANTPOTTING-0004/transcript-B-warm.txt`. Same expected outcome.
- [ ] **4.3** Run `./gradlew --no-daemon pixel6Api34DebugAndroidTest`. Expect green, including the new `OnDeviceModelRealInterpreterTest`. Tee to `docs/sprints/evidence/PLANTPOTTING-0004/gmd-output.txt`.
- [ ] **4.4** Re-inspect `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`. The current file already lists `source-badge=on-device match` and `model-asset-present=true`; if the actual transcripts contain a different `source-badge` value (e.g. `on-device match (low confidence)` on the manually-fed fixture path), update the expected file or — more likely — keep it as-is and document any one-off discrepancy in the results doc. **Do not edit the expected file unless the transcripts contradict it.**

### Phase 5 — Docs, ledger, acceptance close

- [ ] **5.1** Author `docs/sprints/results/PLANTPOTTING-0004.md` (same shape as the 0001/0002/0003 results docs):
  - §0 baseline + §0.4 contract test status.
  - §1 Bug 1 fix: diff summary (manifest field, preprocessor branch, facade dtype assertion + dequantisation), §1.6/§1.8 test renaming notes.
  - §2 Bug 2 fix: the four-line MainActivity diff, the dump verification line from §2.3.
  - §3 real-model instrumentation test: file paths, one paragraph on what the test asserts and why the concrete-class injection bypasses `TestIdentifyModule`.
  - §4 transcripts: links to the three new files under `docs/sprints/evidence/PLANTPOTTING-0004/`, paragraph on the cold/warm diff results.
  - §5 known gaps / handoff for PLANTPOTTING-0005: confidence calibration + `CameraUiState.Failure` UI polish + `LowConfidenceFlowTest` instrumentation + `PermissionDeniedFlowTest` un-`@Ignore`.
  - §6 final-verify table (commands + outputs + commit hash).
- [ ] **5.2** Update `docs/sprints/ledger.yaml`: `PLANTPOTTING-0004` `status: done`, refresh `updated`.
- [ ] **5.3 (final verify)** Run the full gate chain on the final commit:
  - `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking`
  - `bash scripts/check-stub-isolation.sh`
  - `./gradlew --no-daemon pixel6Api34DebugAndroidTest`
  - `pwsh ./scripts/integration-flow.ps1`
  - `pwsh ./scripts/integration-flow.ps1 -BuildOnly`
  Record outputs in §5.1's results doc.

---

## 6. Sequencing and dependency rules

```
Phase 0 (setup + contract lock + baseline)
   │
   ├──► Phase 1 (Bug 1 fix: dtype-aware preprocessor + manifest + facade)
   │      │
   │      └──► Phase 3 (real-model instrumentation test — depends on the
   │             buffer-based pipeline working end-to-end with the real .tflite)
   │
   └──► Phase 2 (Bug 2 fix: testTagsAsResourceId — fully parallel with Phase 1
          and Phase 3 because it only touches MainActivity.kt)

Phase 4 (transcripts + final verify) — depends on Phase 1 + 2 + 3 green
Phase 5 (docs + ledger) — depends on Phase 4 green
```

### 6.1 Hard gates

- **§0.4, §0.5 RED before §1.2 / §1.5.** Standard contract-lock pattern from 0003 §6.1.
- **§1.1 RED before §1.2 (manifest edit).**
- **§1.4 RED before §1.5 (PreprocessedImage refactor).**
- **§1.6 RED before §1.7 (ImagePreprocessor branch).**
- **§1.8 RED before §1.9 (facade refactor + dtype assertion).**
- **§2.1 RED before §2.2 (MainActivity wrap).**
- **`bash scripts/check-stub-isolation.sh` and `./gradlew verifyNoNetworking` green throughout.**

### 6.2 De-scope order (when the implementer slips)

1. Drop §3.2 nice-to-have second fixture JPEG (blank/grey for the real-model low-conf path).
2. Drop §3.2 nice-to-have observed-probabilities recording.
3. Drop §3.2 nice-to-have Compose-UI Robolectric semantic test (rely on §2.3 manual `uiautomator dump` + Phase 4 transcripts only).
4. **Never drop:** Phase 1 (Bug 1 fix), Phase 2 (Bug 2 fix), Phase 3 (real-model test), Phase 4 transcripts, Phase 5 results doc + ledger close.

---

## 7. Risks and mitigations

### 7.1 `ExperimentalComposeUiApi` opt-in surface

**Risk:** the experimental marker propagates if scoped too widely; future Compose minor-version bumps could change the API.
**Mitigation:** scope the `@OptIn(ExperimentalComposeUiApi::class)` to the single `Box(...)` call site in `MainActivity.kt`. Do NOT add it to `freeCompilerArgs` project-wide. A grep for `ExperimentalComposeUiApi` in `app/src/main/` should return exactly one hit after the fix. The §2.1 unit test pins the runtime semantic so a breaking API change fails the test loudly.

### 7.2 `testTagsAsResourceId` leaks tag strings into a11y announcements

**Risk:** if `testTagsAsResourceId = true` were to surface in Talkback output, that would be a UX regression on a11y-enabled devices.
**Mitigation:** the Compose semantics property maps testTag → platform `View.id` resource-name. Talkback reads `contentDescription` and on-screen `Text`, not the resource-id. PLANTPOTTING-0003 set `contentDescription` on every interactive Compose node (e.g. `CameraScreen.kt:167` shutter `contentDescription = "Capture plant photo"`); those remain authoritative for announcements. No code change required. §2.3's `uiautomator dump` verification doubles as evidence — the dump should still show `content-desc` populated alongside the new `resource-id`.

### 7.3 The real `Interpreter.run` test doesn't boot cleanly on the AOSP `pixel6Api34` GMD image

**Risk:** the GMD image is x86_64 — if TFLite's x86_64 native fails to load (e.g., `.so` ABI mismatch under a future TFLite version bump), the new `OnDeviceModelRealInterpreterTest` would `IdentificationFailureException` and falsely fail.
**Mitigation:** PLANTPOTTING-0003 §7.6 already verified the dep audit and that the existing instrumentation source set builds against `tensorflow-lite` (the GMD has been booting `pixel6Api34DebugAndroidTest` with TFLite on the classpath since 0003 §1.4). The new test exercises the same native library that was already there. If a future TFLite bump breaks this, the test failure itself is the signal — that's its job. No upfront mitigation worth the time.

### 7.4 The `input_dtype` branch breaks the `FakeInterpreterFacade`

**Risk:** `FakeInterpreterFacade` (the JVM unit-test impl) takes `PreprocessedImage` and returns canned `FloatArray` scores. If the production code expects dequantised UINT8 output but the fake delivers raw FloatArray, the score-mapper path could diverge between unit and instrumentation tests.
**Mitigation:** the fake's contract is "ignore the input, return canned float scores". The dequantisation happens **inside `TfLiteInterpreterFacade.runInference`** — by the time `ModelScoreMapper` sees the FloatArray, dequantisation has already happened. The fake's output is a FloatArray as before. Same shape, same mapping behaviour. `ModelScoreMapperTest` is untouched by this sprint.

### 7.5 A future FP16 model swap forgets to update `input_dtype`

**Risk:** someone drops a FP16 `.tflite` into `assets/ml/aiy_plants_v1/` without flipping `input_dtype` back to `"float32"`. The preprocessor would feed UINT8 bytes into a FLOAT32 tensor and TFLite would throw the **mirror image** of today's Bug 1 error.
**Mitigation:** §1.9's load-time dtype assertion in `TfLiteInterpreterFacade.loadInterpreter()`. The interpreter's runtime `getInputTensor(0).dataType()` is the authoritative truth; comparing it to `manifest.inputDtype` at load time means a mismatch throws `IdentificationFailureException` on the **first** `identify(jpeg)` call — same shape of error as Bug 1, but with a much more diagnostic message: `"Model input dtype mismatch: manifest=UINT8, runtime=FLOAT32"`. The §3.2 real-model instrumentation test would catch it in CI before reaching the user.

### 7.6 The new real-model test's "doesn't throw" assertion is too loose

**Risk:** the test asserts only that `identify(bytes)` returns without throwing — a future regression that produces *wrong* results would still pass.
**Mitigation:** that's by design. The §3.2 nice-to-have records observed top-1/top-3 probabilities as evidence in the results doc (per the 0003 §3.8 pattern). Confidence calibration / accuracy gating belongs in PLANTPOTTING-0005. The point of this sprint's test is to close the gap that allowed Bug 1 to ship — Bug 1 was a hard failure (exception), not a soft accuracy regression. The "doesn't throw" assertion is exactly the falsifiable claim that catches Bug-1-shaped regressions.

### 7.7 Windows sandbox filesystem overlay (per project auto-memory)

**Risk:** shell-tool writes outside the project tree may not reach disk on this machine.
**Mitigation:** every artefact in this sprint lands under `D:/DarkFactoryProject/Plant potting/`. The fixture JPEG (§3.1), the transcripts (§4.1–§4.3), the results doc (§5.1) — all repo-relative. Implementer verifies file presence from the user's terminal (`ls docs/sprints/evidence/PLANTPOTTING-0004/`) before claiming acceptance.

### 7.8 `@HiltAndroidTest` injection of the concrete `OnDevicePlantIdentifier` doesn't actually bypass `TestIdentifyModule`

**Risk:** Decision §4.3 assumes Hilt builds the concrete class on demand via its `@Inject` constructor, regardless of the `@Binds` swap on the interface. If this assumption is wrong, the new test would inject a fake (defeating its purpose) or fail to compile.
**Mitigation:** Hilt's behaviour here is well-documented — `@Inject` constructors register the concrete class in the binding graph independently of any interface `@Binds`. The `@TestInstallIn(replaces = [OnDeviceIdentifyModule::class])` swap removes the interface binding only. The §3.3 compile check verifies the test compiles; the §4.3 GMD run verifies it injects the real class (any preprocessor exception would surface as a test failure with a clear stack trace pointing into `TfLiteInterpreterFacade`). If the assumption proves wrong despite the docs, fallback is **Decision §4.3 footnote**: write a per-test module that re-binds `PlantIdentifier → OnDevicePlantIdentifier` via `@TestInstallIn(replaces = [TestIdentifyModule::class])` scoped to this test class. Adds ~15 lines, no further architectural shift.

### 7.9 Dequantising the UINT8 output adds a hot-path cost

**Risk:** dequantising 2102 bytes to floats on every capture is cheap, but if a future model has a 10k-class output it could add measurable latency.
**Mitigation:** 2102 byte-to-float conversions are sub-millisecond on a modern ARM core. The §7.3 risk from PLANTPOTTING-0003 capped the cold-capture budget at 3 s; dequantisation is well within budget. Re-measure as evidence in the results doc only if §4.3 GMD output suggests a regression.

---

## 8. Acceptance criteria — falsifiable and observable

The sprint is done when **every** statement below is observably true.

### 8.1 Build + lint + gates

- [ ] `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` green on a clean clone.
- [ ] `bash scripts/check-stub-isolation.sh` green.
- [ ] `./gradlew --no-daemon pixel6Api34DebugAndroidTest` green (including the new `OnDeviceModelRealInterpreterTest`).

### 8.2 Seam invariants (still locked from 0003)

- [ ] `PlantIdentifier` interface byte-for-byte unchanged from PLANTPOTTING-0003 close (`git diff ac120c9 HEAD -- app/src/main/java/com/darkfactory/plantpotting/identify/PlantIdentifier.kt` returns empty).
- [ ] `IdentificationResult` field list unchanged (`speciesId, displayName, source, lowConfidence`).
- [ ] `PlantIdentifierContractTest` (from PLANTPOTTING-0003 §0.4) still green.
- [ ] `StubPlantIdentifier.kt` still under `app/src/main/java/com/darkfactory/plantpotting/identify/`.

### 8.3 Bug 1 fix (UINT8 preprocessing)

- [ ] `app/src/main/assets/ml/aiy_plants_v1/model_manifest.json` declares `"input_dtype": "uint8"`.
- [ ] `ModelManifestReader` parses `input_dtype` into `ModelDtype.UINT8`; `ModelManifestDtypeContractTest` green.
- [ ] `ImagePreprocessor.preprocess(jpeg)` for the shipped manifest returns a `PreprocessedImage` whose `buffer.capacity() == 224*224*3` (UINT8, not 224*224*3*4).
- [ ] `TfLiteInterpreterFacade.loadInterpreter()` throws `IdentificationFailureException` if `interpreter.getInputTensor(0).dataType()` disagrees with `manifest.inputDtype`. Verified by a (negative-path) JVM unit test or the §3.2 instrumentation test against a deliberately-mismatched in-memory manifest.
- [ ] **On a live emulator:** shutter capture no longer surfaces the `Cannot convert between a TensorFlowLite tensor with type UINT8 and a Java object of type [[[[F …` failure. The user reaches `ResultScreen` (or `LowConfidencePicker` for low-confidence results — see manifest `_comment_coverage`).

### 8.4 Bug 2 fix (testTagsAsResourceId bridge)

- [ ] `MainActivity.onCreate` wraps `PlantPottingNavHost()` in a `Box(modifier = Modifier.semantics { testTagsAsResourceId = true })` opted-into `ExperimentalComposeUiApi`.
- [ ] `MainActivityTestTagsAsResourceIdTest` (§2.1) green.
- [ ] **On a live emulator:** `adb shell uiautomator dump` of the camera screen surfaces `resource-id="camera.shutter"` non-empty. Recorded in `transcript-A-cold.txt` (the script's `Wait-ForNode -resourceId "camera.shutter"` step passes without timing out).

### 8.5 Test gap closure (real-model instrumentation test)

- [ ] `app/src/androidTest/java/com/darkfactory/plantpotting/identify/OnDeviceModelRealInterpreterTest.kt` exists; injects the concrete `OnDevicePlantIdentifier`; runs `identify(realJpegBytes)` against `app/src/androidTest/assets/identify-fixtures/monstera-deliciosa.jpg`.
- [ ] Test green on `pixel6Api34DebugAndroidTest`. Asserts the call returns without throwing and `result.source == IdSource.ON_DEVICE_MODEL`.
- [ ] **Falsifiability proof:** if the implementer reverts the §1.7 preprocessor branch on a feature branch, the new test fails with the exact Bug 1 error message — confirming the test would have caught Bug 1.

### 8.6 §7.5 transcripts (the headline carry-forward from 0003)

- [ ] `docs/sprints/evidence/PLANTPOTTING-0004/transcript-A-cold.txt` captured from a cold-boot emulator run. Final line: `Integration manifest diff passed.`
- [ ] `docs/sprints/evidence/PLANTPOTTING-0004/transcript-B-warm.txt` captured from a warm-emulator re-run (after `am force-stop`). Final line: `Integration manifest diff passed.`
- [ ] Both transcripts diff cleanly against `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`.
- [ ] `docs/sprints/evidence/PLANTPOTTING-0004/gmd-output.txt` (or equivalent) captures `pixel6Api34DebugAndroidTest` green.

### 8.7 Docs + ledger

- [ ] `docs/sprints/results/PLANTPOTTING-0004.md` exists with §0 baseline / §1 Bug 1 / §2 Bug 2 / §3 real-model test / §4 transcripts / §5 carry-forward / §6 final-verify sections.
- [ ] `docs/sprints/ledger.yaml`: `PLANTPOTTING-0004` `status: done`, `updated` refreshed.
- [ ] Every must-land §3.1 task `- [x]` in this file.
- [ ] No edits to prior sprint plan documents (`PLANTPOTTING-0001.md`, `PLANTPOTTING-0002.md`, `PLANTPOTTING-0003.md`).

---

## 9. Handoff note for the `sprint-execute` implementer

You are one of `opus`, `gpt-5.4`, or `gemini`, picked by the user.

- **Read this plan first**, then `PLANTPOTTING-0004-INTENT.md`, then `docs/sprints/feedback/PLANTPOTTING-0003/feedback.md` (the primary bug source).
- **Tick `- [ ]` boxes as you go**, not in batches. The skill enforces this.
- **TDD is mandatory** on every `(test, RED first)` task — §0.4, §0.5, §1.1, §1.4, §1.6, §1.8, §2.1.
- **The four must-land outcomes from §1 are non-negotiable.** If you slip, use the §6.2 de-scope order — never drop Phases 1, 2, 3, 4, 5.
- **Do not change `PlantIdentifier`'s interface shape or `IdentificationResult`'s field list.** PLANTPOTTING-0003 §0.4's contract test enforces this.
- **Do not delete `StubPlantIdentifier.kt`** — it stays under `identify/` for `check-stub-isolation.sh` and for test consumption.
- **Do not edit prior sprint plan documents.**
- **Phase 2 (MainActivity wrap) is fully parallel** with Phase 1 (preprocessor fix). Don't let the bigger refactor block the four-line file edit.
- **Phase 4 transcripts require a live emulator.** If one isn't available, surface to the user — this is the headline acceptance bar and must not be silently deferred a second time.
- **Sandbox warning** (per the user's auto-memory): shell-tool writes outside `D:/DarkFactoryProject/Plant potting/` may not reach disk on this machine. Verify file presence from the user's terminal before claiming acceptance.
- **If the §4.3 concrete-class injection assumption proves wrong** (Risk 7.8 fallback), the §4.3 footnote gives you the rescue path: a per-test `@TestInstallIn(replaces = [TestIdentifyModule::class])` module. ~15 lines, no architectural shift.
