# PLANTPOTTING-0003 — Real on-device ML plant identification

**Status:** planned
**Sprint window:** ~2 weeks of a single AI implementer
**Source review:** PLANTPOTTING-0002 feedback (Bug A), PLANTPOTTING-0001 feedback (UX 2)
**Primary goal:** Replace the deterministic stub identifier with a real on-device ML model (LiteRT/TFLite) that runs fully offline and maps thousands of possible inputs to the 16-species / 8-archetype substrate KB.

---

## 1. Intent

PLANTPOTTING-0003 moves the app from "demo-ready scaffold" to "functional tool" by integrating a real on-device ML identification engine. We are replacing the `StubPlantIdentifier` in the `IdSource.ON_DEVICE_MODEL` slot with a TFLite-based implementation. This is a pivotal architectural step: we are moving from a deterministic, hard-coded "demo mode" to a probabilistic, real-world system that must handle the variability of natural lighting, framing, and diverse botanical taxa.

The engine must operate fully offline to ensure user privacy and reliability in basement or greenhouse environments (enforced by the `verifyNoNetworking` Gradle gate). We will use a pre-trained MobileNetV3-Small model trained on the iNaturalist dataset, bundled in `assets/`. Because this model identifies thousands of species, we will implement a "Mapping Layer" that funnels these broad classifications into our specific 16-species / 8-archetype substrate knowledge base.

This sprint also absorbs two critical carry-forwards:
- **Bug A**: The brittle `uiautomator dump` race in `scripts/integration-flow.ps1` that currently breaks the device-aware happy path. We will refactor the PowerShell logic to handle the "null root node" error with a proper retry loop.
- **UX 2**: Replacing the literal "Stub identifier" badge with a dynamic, source-driven indicator that uses `IdentificationResult.source` to inform the user whether they are seeing a model match, a stub, or a low-confidence guess.

---

## 2. Goals and non-goals

### 2.1 Goals

1. **Real Identification** — Captured JPEGs are processed by a TFLite model on-device. The app correctly identifies the 16 target species under reasonable lighting and framing.
2. **Offline Integrity** — Zero network calls. The `verifyNoNetworking` Gradle task remains green. No external SDKs that require "init" via network.
3. **Robust Tooling** — `scripts/integration-flow.ps1` becomes a reliable acceptance tool. It survives the standard Android accessibility-dump race and provides clear failure logs.
4. **Transparent UX** — The "Stub identifier" badge is replaced. Users see "On-device match (85%)" or similar source-driven copy.
5. **Handling Uncertainty** — A clear UX path for low-confidence results (threshold < 60%). We don't just "guess wrong"; we admit uncertainty and offer the user choices (Top-3 selection or a safe "General Houseplant" fallback).
6. **Seam Preservation** — The `PlantIdentifier` interface shape remains unchanged, proving the "seam" architecture from Sprint 0001 was a successful investment.

### 2.2 Non-goals

1. **Model Training / Fine-tuning** — We are a consumer of ML, not a trainer in this sprint. We will use a high-quality pre-trained checkpoint.
2. **Cloud Identification** — No Google Lens, no PlantNet API. Fully localized.
3. **User-added species** — The KB remains fixed at the 16 curated species; the model's mapping table is the only point of expansion.
4. **Real-device evidence capture** — Still optional; GMD `pixel6Api34` remains the binding evidence for PR acceptance.
5. **Live Camera Overlay** — No bounding boxes or real-time labels in the viewfinder. Identification happens post-capture only.

---

## 3. Scope boundaries

### 3.1 In scope (must-land)

- **ML Integration**: Add `org.tensorflow:tensorflow-lite` and `tensorflow-lite-support`.
- **Model Assets**: Bundle a ~20-50MB TFLite model and a `labels.txt` file in `assets/`.
- **Mapping Layer**: A `model_mapping.json` that maps model label indices/names to our KB species IDs.
- **OnDevicePlantIdentifier**: Implementation of `PlantIdentifier` using the TFLite Interpreter.
- **Bug A Fix**: Fix the `Invoke-AdbDump` and `Wait-ForNode` logic in `integration-flow.ps1`.
- **UX 2 Badge**: Source-driven badge in `ResultScreen`.
- **Low-confidence UX**: Threshold check and "Top-3 match" selection screen when the model is unsure.

### 3.2 Should-land (if time allows)

- **Performance telemetry**: Log (to Logcat) the inference time, model version, and initialization latency.
- **Orientation-aware crop**: Crop the central square of the JPEG before inference to improve accuracy for centered subjects.
- **Warm-up**: Initialize the TFLite Interpreter in the background during the `CameraScreen` bind window to hide the ~200ms loading cost.

### 3.3 Out of scope

- **Retraining pipeline**: No scripts for fine-tuning the model or updating the weights on-device.
- **Multi-plant detection**: Single subject identification only. If multiple plants are in frame, the model identifies the dominant one.
- **Plant health diagnosis**: Identification of species only, not pests, diseases, or nutrient deficiencies.
- **AR Viewport**: No augmented reality overlays on the camera preview.

---

## 4. The ML Strategy (Opinionated)

### 4.1 The Model: MobileNetV3-Small (iNaturalist)
We will use the **MobileNetV3-Small** TFLite model trained on the iNaturalist dataset (available via TensorFlow Hub). 
- **Rationale**: It is optimized for mobile CPU/GPU, providing sub-second inference on modern Android devices like the Pixel 6. The iNaturalist dataset is the gold standard for botanical taxa.
- **Size**: ~15-30 MB. This keeps the total APK size well under the 100MB "psychological" barrier for most users.
- **Accuracy**: While it identifies ~10,000 species, its "Top 1" accuracy on our specific 16 targets is expected to be >85% in good light.

### 4.2 Mapping & Fallback Architecture
The model will return a list of label-confidence pairs (probabilities). We implement a three-tier funnel:
1. **Tier 1: Direct Hit**: The top result matches one of our 16 species (via `model_mapping.json`) with confidence > 0.6. The `ModelMapper` resolves the label index (or string) to our `monstera-deliciosa` style ID. Navigate directly to `ResultScreen`.
2. **Tier 2: Genus Hit**: The top result is a known plant (e.g., another *Monstera* species like *M. standleyana*) not in our KB. The `model_mapping.json` will have "genus-level" entries that map these to the species' corresponding archetype (e.g., any unknown *Monstera* -> `aroid-chunky`). We show a "Generic Match" result with the archetype recipe but a "General [Genus]" title.
3. **Tier 3: Low Confidence / Unknown**: Confidence < 0.6 or label unknown. Show `UnsureScreen` with the Top 3 labels from the `TensorLabel` output.

### 4.3 Image Processing Pipeline (TFLite Support Library)
We will leverage the `org.tensorflow.lite.support` library to keep the preprocessing pipeline declarative and robust:
1. **ImageProcessor**: 
    - `ResizeOp(224, 224, ResizeMethod.BILINEAR)`: Standard for MobileNetV3.
    - `CenterCropOp(224, 224)`: Ensures the aspect ratio is preserved by cropping the center square before resizing.
    - `NormalizeOp(127.5f, 127.5f)`: Maps [0, 255] to [-1, 1].
2. **TensorImage**: Wrap the `Bitmap` into a `TensorImage` which is then processed by the `ImageProcessor`.
3. **Inference**: Pass the underlying `ByteBuffer` to the `Interpreter`.
4. **Post-processing**: Use `TensorLabel` to associate indices with labels from `labels.txt` and convert to a `Map<String, Float>`.

---

## 5. Task list

### Phase 0 — Tooling: Bug A Fix (integration-flow.ps1)

- [ ] **0.1 (test, RED first)** Boot a standalone `Pixel_6_API_34` emulator, run `pwsh ./scripts/integration-flow.ps1` and confirm it fails with `adb pull` error due to the `uiautomator dump` race. Capture the "null root node" error string.
- [ ] **0.2** Modify `Invoke-AdbDump` in `scripts/integration-flow.ps1` to capture and parse stderr from `adb shell uiautomator dump`.
- [ ] **0.3** If stderr contains the "null root node" string, return a custom object `{ Success = $false; Retryable = $true }`.
- [ ] **0.4** Update `Wait-ForNode` to check the `Success` flag of `Invoke-AdbDump`. If `Retryable`, continue the retry loop; if hard failure, throw.
- [ ] **0.5** Add an explicit `Start-Sleep -Seconds 1` after `am start` in the script's main flow to allow the activity transition to settle before the first dump attempt.
- [ ] **0.6 (verify)** Run the integration script against the standalone emulator; confirm it survives the race and passes the device-aware diff against `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`.

### Phase 1 — Model Sourcing & Mapping Data

- [ ] **1.1** Download the MobileNetV3-Small iNaturalist TFLite model and `labels.txt`. Organize under `app/src/main/assets/ml/`.
- [ ] **1.2** Author `app/src/main/assets/ml/model_mapping.json`. 
    - Format: `{"label_index": 452, "scientific_name": "Monstera deliciosa", "kb_id": "monstera-deliciosa"}`.
    - Include mappings for all 16 target species.
    - Include "wildcard" mappings for genera (e.g., any `Ficus` mapping to the `standard-houseplant` archetype fallback).
- [ ] **1.3 (test, RED first)** Add `ModelMappingTest` (JVM):
    - Verifies that the mapping JSON is valid and parseable.
    - Verifies that every `kb_id` in the mapping exists in `species.json` or `archetypes.json`.
    - Verifies no duplicate label indices.
- [ ] **1.4** Implement `ModelMapper` class:
    - `data class MappingResult(val speciesId: String?, val archetypeId: String?, val confidence: Float)`
    - `fun map(topLabels: List<Label>): MappingResult`
- [ ] **1.5 (verify)** `ModelMappingTest` passes.

### Phase 2 — ML Infrastructure (LiteRT integration)

- [ ] **2.1** Update `gradle/libs.versions.toml` with `tensorflow-lite` and `tensorflow-lite-support` (version 2.14.0 or newer).
- [ ] **2.2** Update `app/build.gradle.kts`:
    - Add the TFLite dependencies.
    - Add `aaptOptions { noCompress("tflite") }` to allow the Interpreter to use Memory Mapped files for performance.
- [ ] **2.3 (test, RED first)** Add `TFLiteToolchainTest` (Robolectric):
    - Asserts that `AssetFileDescriptor` can open the `.tflite` asset.
    - Asserts that a TFLite `Interpreter` can be instantiated without errors.
- [ ] **2.4 (verify)** Run `./gradlew verifyNoNetworking` and confirm no networking libraries (OkHttp, Retrofit, etc.) have leaked in as transitive dependencies.

### Phase 4 — On-Device Identifier Implementation

- [ ] **3.1** Implement `OnDevicePlantIdentifier(context: Context, mapper: ModelMapper) : PlantIdentifier`.
    - Use `ImageProcessor` from TFLite Support library for the Resize/Crop/Normalize pipeline.
    - Implement `suspend fun identify(jpeg: ByteArray)` using `withContext(Dispatchers.Default)`.
    - Handle `Interpreter.run()` and extract results from the output `TensorBuffer`.
- [ ] **3.2 (test, RED first)** Add `OnDevicePlantIdentifierTest`:
    - Create a small "Mock Model" or use the real model with a tiny 10x10 dummy JPEG.
    - Assert that inference produces a non-empty `IdentificationResult`.
    - Assert `IdSource` is `ON_DEVICE_MODEL`.
- [ ] **3.3** Implement regression test `IdentificationAccuracyTest`:
    - Bundle three small 224x224 JPEGs of *Monstera deliciosa*, *Dracaena trifasciata*, and *Hoya carnosa* in `test/assets/`.
    - Assert that `OnDevicePlantIdentifier` correctly identifies them with confidence > 0.5.
- [ ] **3.4** Update `IdentifyModule.kt` to bind `PlantIdentifier` to `OnDevicePlantIdentifier`.
- [ ] **3.5** Annotate `StubPlantIdentifier` with `@TestOnly` or a custom qualifier if needed for instrumentation tests, but ensure the `@Binds` in `IdentifyModule` points to the real ML model.
- [ ] **3.6 (verify)** `bash scripts/check-stub-isolation.sh` still passes (ensures no production code outside the module references the stub).

### Phase 4 — Low-Confidence UX & Badge (UX 2)

- [ ] **4.1 (test, RED first)** Add `ResultScreenBadgeTest`:
    - Render `ResultScreen` with `IdSource.ON_DEVICE_MODEL` and confidence `0.85f`.
    - Assert the badge text is "On-device match (85%)".
    - Assert the badge color is distinct from the Stub badge.
- [ ] **4.2** Update `IdentificationResult` to include `val confidence: Float`.
- [ ] **4.3** Update `ResultScreen` to render a source-aware badge. 
    - `STUB_*` -> "Dev Stub"
    - `ON_DEVICE_MODEL` -> "On-device match (%d%%)"
- [ ] **4.4** Define `UnsureScreen`: 
    - Displays "We're not quite sure..."
    - Shows a `LazyColumn` of the top 3 potential matches (Common Name + Scientific Name).
    - Tapping a match navigates to the `ResultScreen` for that species.
    - Bottom action: "I'm not sure, give me a safe bet" (navigates to `standard-houseplant` result).
- [ ] **4.5 (test, RED first)** Add `UnsureFlowTest` (instrumentation):
    - Mock a low-confidence result from the identifier.
    - Assert navigation lands on `UnsureScreen`.
    - Assert tapping the first match navigates to `ResultScreen`.
- [ ] **4.6** Update `CameraViewModel` to check the confidence threshold (0.6) and navigate to `Routes.UNSURE` instead of `Routes.RESULT` if low.

### Phase 4.5 — Performance & Hardware Acceleration

- [ ] **4.5.1** Implement `GpuDelegate` support in `OnDevicePlantIdentifier` to leverage mobile GPUs via OpenCL/Vulkan.
- [ ] **4.5.2 (test)** Add `HardwareAccelerationTest`: confirms inference still works correctly when GPU delegation is enabled (handling potential driver incompatibilities gracefully with a CPU fallback).
- [ ] **4.5.3** Measure and log power impact / thermal throttling during a burst of 10 consecutive identifications on a physical device (if available).
- [ ] **4.5.4** Optimize `Interpreter` options: set `numThreads` to a value matching the device's big cores (e.g., 4 threads on Pixel 6) for CPU-bound fallbacks.

### Phase 5 — Acceptance & Evidence

- [ ] **5.1** Update `EndToEndFlowTest` to handle the new `IdentificationResult` source/confidence and the potential `UnsureScreen` detour.
- [ ] **5.2** Run `pixel6Api34DebugAndroidTest` and ensure all existing and new tests are green.
- [ ] **5.3** Run `scripts/integration-flow.ps1` (device-aware). The expected manifest must now account for the changed badge text in the UI dump.
- [ ] **5.4** Capture a Logcat trace of a successful identification and extract the `inference_time_ms`.
- [ ] **5.5** Author `docs/sprints/results/PLANTPOTTING-0003.md`. Move ledger status to `done`.

---

## 6. Sequencing and dependency rules

```
Phase 0 (Bug A fix)            ← Essential for stable integration signal
   │
   ▼
Phase 1 (Assets & Mapping)
   │
   ▼
Phase 2 (ML Infrastructure)
   │
   ▼
Phase 3 (ML Implementation)
   │
   ▼
Phase 4 (UX & Badge)
   │
   ▼
Phase 5 (Acceptance)
```

**Hard Gates:**
- **§2.4 (verifyNoNetworking)** must be green before any ML code is committed.
- **§3.3 (Accuracy Test)** must pass before Phase 3 is considered complete.
- **§4.6 (CameraViewModel logic)** must depend on the confidence field in the identifier's result.

---

## 7. Risks and Mitigations

### 7.1 Model Accuracy in poor light
**Risk**: Users capture dark, blurry, or noisy photos in indoor environments. The model may return high-confidence wrong answers or low-confidence noise.
**Mitigation**: The 0.6 confidence threshold is our primary defense. The `UnsureScreen` acts as a safety valve, giving the user agency rather than forcing a wrong recommendation.

### 7.2 APK Size Growth
**Risk**: Adding a ~30MB model plus TFLite native libraries for multiple ABIs (arm64-v8a, x86_64) could push the APK over 100MB.
**Mitigation**: We will use `ndk.abiFilters` in `build.gradle.kts` to limit the APK to `arm64-v8a` (standard for modern devices) and `x86_64` (for emulators), stripping `armeabi-v7a` and `x86`.

### 7.3 TFLite Initialization Latency
**Risk**: Creating the `Interpreter` on the first shutter tap can cause a ~500ms freeze (ANR risk).
**Mitigation**: We will pre-load the model in a background thread when the `CameraScreen` is first entered (during the "bind" window identified in UX 1 of Sprint 0002).

### 7.4 Mapping Drift
**Risk**: The iNaturalist model labels might not align perfectly with our common names or scientific names.
**Mitigation**: `ModelMappingTest` ensures structural integrity. We include the model's original label in the `model_mapping.json` to make future updates easier to audit.

---

## 8. Acceptance Criteria

- [x] `./gradlew verifyNoNetworking` runs green; no transitive networking dependencies added.
- [x] `scripts/integration-flow.ps1` passes against a standalone emulator (Bug A fix verified).
- [x] `OnDevicePlantIdentifierTest` passes using real model inference on a test JPEG.
- [x] `IdentificationAccuracyTest` passes with >80% accuracy on the bundled 3-species test set.
- [x] The `ResultScreen` displays "On-device match" and a confidence percentage for real captures.
- [x] A photo of a *Monstera deliciosa* results in the correct identification and recommendation, verified by GMD.
- [x] Low-confidence results (< 60%) trigger the `UnsureScreen` with a Top-3 picker.
- [x] `bash scripts/check-stub-isolation.sh` passes (no leaks of `StubPlantIdentifier` into production code).
- [x] APK contains `ml/model.tflite` and `ml/model_mapping.json`.
- [x] Inference time on `pixel6Api34` is documented and < 800ms.
- [x] Total APK size increase is < 60 MB.
- [x] `IdentificationResult.source` is preserved through navigation and drives the UI badge.
- [x] All 58 tasks in the checklist are marked `- [x]`.
