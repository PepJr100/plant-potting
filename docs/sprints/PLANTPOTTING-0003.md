# PLANTPOTTING-0003 — On-device ML plant identifier + Bug A fix + source-driven badge

**Status:** planned
**Sprint window:** ~2 weeks for a single AI implementer (opus, gpt-5.4, or gemini — chosen at `sprint-execute` time)
**Source review:** PLANTPOTTING-0002 feedback (Bug A); PLANTPOTTING-0001 feedback (UX 2).
**Source artefacts:** merged from `drafts/PLANTPOTTING-0003-{CODEX,GEMINI,CLAUDE}.md` plus the three cross-critiques.

---

## 1. Intent

Replace the deterministic `StubPlantIdentifier` with a real on-device ML identifier behind the existing `PlantIdentifier` seam from PLANTPOTTING-0001 (`IdSource.ON_DEVICE_MODEL`). The implementation runs **fully offline** — the model, labels, and the model→KB mapping all ship under `app/src/main/assets/ml/`, and the `verifyNoNetworking` Gradle gate stays green. The sprint also bundles two carry-forwards: **Bug A** (the `uiautomator dump`/`adb pull` race in `scripts/integration-flow.ps1` that blocked PLANTPOTTING-0002 §8 line 310) and **UX 2** (replace the literal "Stub identifier — replace in a later sprint" badge with copy driven by `IdentificationResult.source`). Seam invariants are non-negotiable: the `PlantIdentifier` interface shape is unchanged, `StubPlantIdentifier` survives as a test fixture, and `bash scripts/check-stub-isolation.sh` keeps passing.

---

## 2. Goals and non-goals

### 2.1 The single observable success bar

On the `pixel6Api34` Gradle Managed Device **and** against a connected emulator:

1. The §2.1 user flow from PLANTPOTTING-0001 still completes end-to-end without crashes or ANRs.
2. The result screen's badge copy is **driven by `IdentificationResult.source`**. The production build no longer shows the literal "Stub identifier — replace in a later sprint" copy on any happy-path capture.
3. When the shutter fires on the AOSP image (deterministic virtual-scene input), the identifier returns a result tagged `IdSource.ON_DEVICE_MODEL` within ≤3 s on a cold first capture, and the downstream recommendation is the substrate that the mapped species' KB entry prescribes.
4. When model confidence is low (or the top label is unmapped), the user is routed to a **`LowConfidencePicker` screen** showing the top-3 mapped candidates **and** a manual search over the 16 KB species, plus an explicit "I don't know — pick by archetype" CTA. The app **never** silently emits a recipe from a weak or unmapped match.
5. `pwsh ./scripts/integration-flow.ps1` (no `-BuildOnly`) succeeds against a connected `Pixel_6_API_34` emulator without the `null root node returned by UiTestAutomationBridge` race. Two transcripts (cold + warm) captured under `docs/sprints/evidence/PLANTPOTTING-0003/`.

### 2.2 Falsifiability — how we know we hit it

- `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking pixel6Api34DebugAndroidTest` is green on a clean clone.
- `bash scripts/check-stub-isolation.sh` is green — production references to `StubPlantIdentifier` are still confined to `app/src/main/java/com/darkfactory/plantpotting/identify/`.
- `git diff main -- app/src/main/java/com/darkfactory/plantpotting/identify/PlantIdentifier.kt` shows **zero** changes to the interface block; only the `IdentificationResult` data class grew an additive field.
- `pwsh ./scripts/integration-flow.ps1` (device-aware) produces a clean diff against the device-aware expected file *for the first time in repo history*. Cold + warm transcripts captured under `docs/sprints/evidence/PLANTPOTTING-0003/`.
- New tests `PlantIdentifierContractTest`, `OnDevicePlantIdentifierFixturesTest`, `ModelLabelMappingValidationTest`, `ModelManifestTest`, `ResultScreenBadgeTest`, and `LowConfidencePickerScreenTest` are all green.

### 2.3 Non-goals (explicit deferrals)

- No cloud identification. `IdSource.CLOUD` stays as an enum value, never bound at runtime.
- No model fine-tuning, no training pipeline, no on-device training.
- No real-device evidence capture (§8.7 from PLANTPOTTING-0001 is still optional; no physical device is assumed).
- No re-enabling of `PermissionDeniedFlowTest` (still `@Ignore`).
- No KB content edits beyond what mapping-table authoring strictly requires.
- No backwards-incompatible change to the `PlantIdentifier` interface. **Adding fields to `IdentificationResult` is permitted only as additive defaults.**
- No accuracy threshold as a hard CI gate. Fixture metrics are recorded as evidence in the results doc; they do not block acceptance unless the model fails to identify *any* mapped species.
- No editing of prior sprint documents (PLANTPOTTING-0001.md, PLANTPOTTING-0002.md). Carry-forward closure is recorded in `docs/sprints/results/PLANTPOTTING-0003.md` only.
- No GPU/NNAPI hardware acceleration in the must-land flow — see §3.2.
- No Room/DataStore persistence of model results, capture metadata, or confidence numbers.
- No new app screens beyond `LowConfidencePicker` and a minimal `ArchetypePicker` (user-initiated, not auto-fallback).

---

## 3. Scope boundaries

### 3.1 Must-land

- A real TFLite identifier behind a new `OnDevicePlantIdentifier : PlantIdentifier` binding.
- The model `.tflite`, labels file, `model_manifest.json`, and `plant_class_map.json` shipped under `app/src/main/assets/ml/aiy_plants_v1/`.
- Hilt wiring: production binding switches to `OnDevicePlantIdentifier`; `StubPlantIdentifier` survives, referenced from tests only, still inside `identify/`.
- An additive field on `IdentificationResult`: `lowConfidence: Boolean = false`. The interface signature does not change.
- A `LowConfidencePicker` screen showing top-3 mapped candidates + manual search over the 16 KB species + "pick by archetype" CTA.
- A minimal `ArchetypePicker` reachable only from the picker's CTA (user-initiated). Tapping an archetype routes through the existing `RecommendationScreen` infrastructure with an archetype-variant `RecommendationUiState`.
- Source-driven `ResultScreen` badge driven by `IdSource` (+ `lowConfidence` modifier).
- Bug A fix in `scripts/integration-flow.ps1` (the `uiautomator dump` race) with paired PowerShell shim tests.
- All paired tests green; `verifyNoNetworking` and `check-stub-isolation.sh` green throughout.

### 3.2 Nice-to-have (only if time permits)

- **Model warm-up during the CameraX bind window.** Pre-load the TFLite Interpreter in a background coroutine when `CameraScreen` first composes, so the first shutter doesn't pay a cold-start cost.
- **Performance telemetry.** Log inference latency + model-load latency to Logcat at `Log.i`. No persistent capture.
- **INT8-quantized model variant** as an opt-in Gradle property; FP16 (or float, depending on what AIY V1 ships) remains the default.
- **GPU/NNAPI delegate** (CPU fallback on driver issues). Only land if `verifyNoNetworking` and ABI fragmentation stay clean.
- **`KbToModelCoverageTest`** (inverse coverage): every KB species has at least one mapping entry pointing *to* it.

### 3.3 Out-of-scope

- `IdSource.CLOUD` codepath, any backend, any analytics.
- Multi-plant detection, plant-health diagnosis, AR overlays, live-camera labels.
- A confidence-calibration study or per-class threshold tuning.
- Changes to `PlantIdentifier` interface shape.
- Persisting capture metadata, model version, or confidence to disk.
- Adding `MANUAL` to `IdSource`.
- Bumping AGP, Kotlin, Compose, Hilt, or any pre-existing dep beyond what TFLite forces.

---

## 4. Decisions (opinionated; defend in this file)

### 4.1 Model choice — AIY Plants V1 family

**Pick:** `google/aiy/vision/classifier/plants_V1` (TF Hub / Kaggle Models).

- Prefer the latest available variant (`/3` if listed; else `/1`). The implementer downloads the artifact and records the exact variant in `model_manifest.json`.
- Runtime: TensorFlow Lite Interpreter + TensorFlow Lite Support (image preprocessing). **Do not** add `tensorflow-lite-task-vision` — it pulls more transitive surface and complicates the unit-test path (codex critique 1.2 of claude). Use a hand-rolled `InterpreterFacade` so unit tests don't instantiate native TFLite.
- Label vocabulary: ~2,101 plant taxa in scientific binomial form. Approximate top-1 on the 16 KB species: 40–65 % (per the codex draft's measured ballpark). Top-3 mapped: 60–80 %. **These numbers are evidence, not acceptance gates.**
- Float (FP16) variant when available; INT8 quantized only as a §3.2 stretch. Approximate sizes: FP16 ~25 MB, INT8 ~6.5 MB.
- License: Apache 2.0. Bundle `LICENSE-aiy-plants-v1.txt` next to the model.

**Rejected alternatives (documented for the merge audit):**
- *MobileNetV3-Small iNat* (gemini): less concrete artifact; gemini's draft did not name a specific TF Hub URL or label map.
- *ML Kit Custom Image Labeling* (codex's rejection holds): still requires a custom TFLite, plus Play Services surface complicates offline gate.
- *PlantNet API*: online only, defeats `verifyNoNetworking`.
- *MobileNetV2 + transfer learning*: requires training + data pipeline; out of scope.

### 4.2 Mapping: model labels → KB species or archetype hint

**Format:** JSON file at `app/src/main/assets/ml/aiy_plants_v1/plant_class_map.json`:

```json
{
  "version": 1,
  "modelLabelsAsset": "ml/aiy_plants_v1/labels.csv",
  "mapping": {
    "Monstera deliciosa":         { "kbSpeciesId": "monstera-deliciosa" },
    "Monstera adansonii":         { "kbSpeciesId": "monstera-adansonii" },
    "Epipremnum aureum":          { "kbSpeciesId": "epipremnum-aureum" },
    "Sansevieria trifasciata":    { "kbSpeciesId": "dracaena-trifasciata", "alias": true },
    "Dracaena trifasciata":       { "kbSpeciesId": "dracaena-trifasciata" },
    "Calathea orbifolia":         { "kbSpeciesId": "goeppertia-orbifolia", "alias": true },
    "Goeppertia orbifolia":       { "kbSpeciesId": "goeppertia-orbifolia" }
  }
}
```

- Lookup key is the predicted-class label string at the predicted index, normalised (trimmed, lower-cased) before lookup.
- Each `kbSpeciesId` must already exist in `app/src/main/assets/kb/species.json`. Enforced by `ModelLabelMappingValidationTest`.
- Unmapped labels (returned at the top-1 / top-2 / top-3 position) route to the `LowConfidencePicker` flow. **No silent archetype fallback at the data layer.** Archetype navigation is user-initiated only.
- Aliases (e.g., `Sansevieria → Dracaena`, `Calathea → Goeppertia`) are flagged with `"alias": true` so the editorial notes can capture the rationale.
- The mapping file is the **single editorial source** for model-to-KB resolution. A companion `docs/kb/ml-mapping-notes.md` records one short paragraph per mapping line justifying the choice (same editorial discipline as `docs/kb/plant-substrate-kb-notes.md`).

### 4.3 Confidence policy and routing

The TFLite model outputs softmax probabilities per class. We compute:

- `bestProb = max(probs)`, `bestLabel = argmax(probs)`.
- `top3 = top 3 (label, prob) pairs filtered to entries with a non-null mapping`.

**Locked thresholds:**

| Condition | Behaviour |
| --- | --- |
| `bestProb >= 0.55` AND `mapping[bestLabel]` resolves to a KB species | **High-confidence path.** Emit `IdentificationResult(speciesId=mapped, displayName=KB.displayName, source=ON_DEVICE_MODEL, lowConfidence=false)`. Navigate to `ResultScreen`. Badge reads "On-device match". |
| `0.45 <= bestProb < 0.55` AND `mapping[bestLabel]` resolves AND `bestProb - secondProb >= 0.18` | **High-confidence with margin.** Same as above. |
| Otherwise, if at least one of `top3` has a non-null mapping | **Low-confidence path.** Emit `IdentificationResult(speciesId="", displayName="", source=ON_DEVICE_MODEL, lowConfidence=true)` and emit the up-to-3 candidates separately as a `NavCommand.LowConfidence(candidates: List<Candidate>)` via `CameraViewModel`'s navigation flow. **The candidates are not stored on `IdentificationResult`** — they ride on the navigation event only, which keeps the seam's result-data class minimal (codex critique 2.2 of claude). |
| No top-3 entry has a non-null mapping | **Unmapped path.** Same low-confidence emission, but `candidates` is empty; the picker shows the manual search over 16 KB species + the "pick by archetype" CTA only. |

**Hard failure (model load fails, JPEG malformed, native crash) is a separate path:** `OnDevicePlantIdentifier` throws `IdentificationFailureException`, `CameraViewModel` catches it and transitions to a new `CameraUiState.Failure("Couldn't read the photo — retake")`. **Hard failures are *not* low confidence** — codex critique 2.2 of claude is right: hiding model breakage as user uncertainty is wrong.

### 4.4 Seam invariants (non-negotiable)

- `interface PlantIdentifier { suspend fun identify(jpeg: ByteArray): IdentificationResult }` — **byte-for-byte unchanged** from PLANTPOTTING-0001.
- `IdentificationResult` grows **exactly one** additive optional field: `val lowConfidence: Boolean = false`. The existing fields (`speciesId`, `displayName`, `source`) and the existing `IdSource` enum are untouched. Top-3 candidates are *not* fields on this class — they ride on the camera navigation event.
- `StubPlantIdentifier` stays in `app/src/main/java/com/darkfactory/plantpotting/identify/StubPlantIdentifier.kt`. Still `@Singleton`, still `@Inject` constructor. The production Hilt binding switches; the stub stays reachable from tests.
- `bash scripts/check-stub-isolation.sh` (the §4.5 grep from PLANTPOTTING-0001) keeps passing. Any new production reference to `StubPlantIdentifier` outside `identify/` is a sprint-blocker bug.

### 4.5 Assets layout

```
app/src/main/assets/
  kb/
    archetypes.json              (existing — unchanged)
    species.json                 (existing — unchanged)
  ml/
    aiy_plants_v1/
      model.tflite               (~25 MB FP16, or smaller INT8 — exact variant recorded in manifest)
      labels.csv                 (line N = class index N; matches model output dim)
      plant_class_map.json       (model label → KB species id, see §4.2)
      model_manifest.json        (source URL, license, sha256, input contract, label count, thresholds, acquisition date)
      LICENSE-aiy-plants-v1.txt  (Apache-2.0 attribution per the upstream model card)
```

### 4.6 Hilt wiring

- Delete the existing `IdentifyModule.kt` production binding to `StubPlantIdentifier`. **Do not delete `StubPlantIdentifier.kt`** — only its production wiring.
- Add `OnDeviceIdentifyModule` that `@Binds`s `OnDevicePlantIdentifier` to `PlantIdentifier` under `SingletonComponent`.
- Instrumentation tests use `@TestInstallIn(replaces = [OnDeviceIdentifyModule::class], component = SingletonComponent::class)` with a `FakeFixedIdentifier` (already used by existing tests) or `StubPlantIdentifier` directly. The §4.5 grep stays clean because the test-only binding module lives under `app/src/androidTest/` or `app/src/test/`.

### 4.7 Navigation routes

Additive only — existing `result/{speciesId}` is extended; no destination is renamed:

- `result/{speciesId}?source={source}&lowConfidence={bool}` — `source` and `lowConfidence` are optional query-style args with defaults preserving legacy callers (`source=STUB_DETERMINISTIC`, `lowConfidence=false`).
- `low-confidence-picker?candidates={encoded}` — `encoded` is a compact comma-separated `speciesId|probPct` list (e.g. `monstera-deliciosa|72,ficus-lyrata|18`). Max 3 entries → string length stays well under nav-arg URL-encode limits (gemini critique 2.4 of claude is mitigated by capping at 3 and using integer percentages).
- `archetype-picker` — flat list of 8 archetypes; tap routes to the existing `recommendation` destination with an archetype-variant `RecommendationUiState`.

### 4.8 Bug A fix shape

`scripts/integration-flow.ps1`'s `Invoke-AdbDump` currently throws on the first `adb pull` miss, which the `null root node returned by UiTestAutomationBridge` race triggers because `uiautomator dump` returned exit 0 but never wrote the file. The fix:

- `Invoke-AdbDump` returns `$true` on real success (file exists after pull) and `$false` on soft failures: (a) exit 0 but file missing after pull, (b) stderr from `uiautomator dump` contains the literal `"null root node returned by UiTestAutomationBridge"`, (c) `adb pull` exit non-zero. **Hard failures** (adb itself missing, transport error) still throw.
- `Wait-ForNode`'s retry loop checks the return value and continues on `$false`. Throws only after `maxAttempts` exhaustion.
- Capture `uiautomator dump`'s stderr via `2>&1` and inspect for the documented race string. Match → retry signal regardless of exit code.
- After each `adb shell am start`, insert `Start-Sleep -Seconds 2` before the first `Wait-ForNode` call. The cold-launch foreground transition on the AOSP image can take longer than the existing 8 × 750 ms budget.
- Bump `Wait-ForNode` default `maxAttempts` from 8 to 12 (≈12 s total budget after the settle sleep).
- Hard failure modes (adb missing, non-retryable error, pull failure after a *successful* dump, unreadable XML) still throw with the existing error messages.

---

## 5. Task list

TDD ordering: paired test tasks land **RED first** where the spec permits. Test tasks for the model itself use small fixture JPEGs committed to the repo.

### Phase 0 — Sprint setup and contract lock

- [x] **0.1** Re-read this plan plus the eight anchor files: `docs/sprints/PLANTPOTTING-0001.md`, `docs/sprints/PLANTPOTTING-0002.md`, both feedback docs, `app/src/main/java/com/darkfactory/plantpotting/identify/PlantIdentifier.kt`, `app/src/main/java/com/darkfactory/plantpotting/identify/StubPlantIdentifier.kt`, `app/src/main/java/com/darkfactory/plantpotting/identify/IdentifyModule.kt`, `scripts/integration-flow.ps1`.
- [x] **0.2 (baseline)** Run `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` + `bash scripts/check-stub-isolation.sh` on a clean `main` *before* any edits. Record outputs in `docs/sprints/results/PLANTPOTTING-0003.md` as the evidence sink. Surface any baseline regression to the user before continuing.
- [x] **0.3** Update `docs/sprints/ledger.yaml`: `PLANTPOTTING-0003` `status: in-progress`, stamp `executor`, refresh `updated`.
- [x] **0.4 (test, RED first)** Add `PlantIdentifierContractTest` (JVM) asserting the interface shape: exactly one `suspend fun identify(jpeg: ByteArray): IdentificationResult`, and `IdentificationResult` still has the four fields (`speciesId`, `displayName`, `source`, and the new `lowConfidence` from §4.4). This locks the seam against accidental drift. RED before §3.1's data-class extension lands.

### Phase 1 — TFLite dependencies + verifyNoNetworking guard

- [x] **1.1** Add to `gradle/libs.versions.toml`: `tensorflow-lite = "2.14.0"`, `tensorflow-lite-support = "0.4.4"`. Add the matching `org.tensorflow:tensorflow-lite` and `org.tensorflow:tensorflow-lite-support` entries to the `[libraries]` section. **Do not add `tensorflow-lite-task-vision`** — see §4.1 rationale.
- [x] **1.2 (test, RED first)** Add `VerifyNoNetworkingRegressionTest` (JVM) that runs `./gradlew :app:dependencies --configuration releaseRuntimeClasspath` (or reads the cached report) and asserts that none of `okhttp`, `retrofit`, `firebase`, `play-services-network`, `volley`, `ktor-client-okhttp` substrings appear in the dependency tree. Fails RED before §1.1 lands; goes green after the TFLite deps are added cleanly. (Do not match on `play-services-tasks` — it's the Tasks API, not networking.)
- [x] **1.3** Add the two TFLite deps to `app/build.gradle.kts` under `implementation(...)`. Configure `androidResources { noCompress += "tflite" }` so the `.tflite` asset can be memory-mapped.
- [x] **1.4 (verify)** `./gradlew verifyNoNetworking` green. `./gradlew :app:dependencies --configuration releaseRuntimeClasspath` shows tensorflow-lite-* and *no* OkHttp/Retrofit/Firebase/Volley/Ktor transitive. Record both in the results doc.

### Phase 2 — Model + labels + mapping + manifest (assets)

- [ ] **2.1 (test, RED first)** Add `ModelAssetsPresenceTest` (Robolectric for `AssetManager`) asserting all five artefacts exist under `assets/ml/aiy_plants_v1/`: `model.tflite`, `labels.csv`, `plant_class_map.json`, `model_manifest.json`, `LICENSE-aiy-plants-v1.txt`. Fails RED until §2.2 lands.
- [ ] **2.2** Download `lite-model_aiy_vision_classifier_plants_V1_<latest>.tflite` and the AIY labels CSV from TF Hub / Kaggle Models (latest available variant). Commit under `app/src/main/assets/ml/aiy_plants_v1/model.tflite` and `labels.csv`. Copy the model card's Apache-2.0 attribution to `LICENSE-aiy-plants-v1.txt`.
- [ ] **2.3** Author `app/src/main/assets/ml/aiy_plants_v1/model_manifest.json` with: `source_url`, `variant` (e.g., `V1/3`), `sha256`, `input_size`, `color_order` (RGB), `normalization` (mean/std), `output_tensor_shape`, `label_count`, `acquisition_date` (ISO-8601), `thresholds` (the §4.3 numbers verbatim).
- [ ] **2.4 (test, RED first)** Add `ModelManifestTest` (JVM) asserting: manifest is valid JSON; declared `sha256` matches the checked-in `model.tflite` byte hash; declared `label_count` matches the line count in `labels.csv`; declared `thresholds` match the §4.3 numbers in the plan (sanity check, not duplicated implementation).
- [ ] **2.5** Author `app/src/main/assets/ml/aiy_plants_v1/plant_class_map.json` per §4.2. Each `kbSpeciesId` value must already exist in `assets/kb/species.json`.
- [ ] **2.6 (test, RED first)** Add `ModelLabelMappingValidationTest` (JVM, Robolectric for AssetManager) asserting: JSON is parseable; every `kbSpeciesId` resolves in `species.json`; `modelLabelsAsset` exists in `assets/`; no duplicate keys; every key normalises consistently with the runtime lookup; `alias: true` rows have a non-alias counterpart elsewhere in the file pointing to the same KB species id.
- [ ] **2.7** Author `docs/kb/ml-mapping-notes.md` — sibling of `plant-substrate-kb-notes.md`. One short paragraph per mapping line justifying the model-label → KB species id decision. No `TODO` / `placeholder` substrings (matches the editorial bar from PLANTPOTTING-0001 KB notes).

### Phase 3 — `OnDevicePlantIdentifier` impl + tests

- [ ] **3.1** Extend `IdentificationResult` (in `PlantIdentifier.kt`) with `val lowConfidence: Boolean = false`. Confirm the `PlantIdentifier` interface block is byte-for-byte unchanged. §0.4 enforces this.
- [ ] **3.2 (test, RED first)** Add `ImagePreprocessorTest` (JVM, no Android UI dep). Covers JPEG decode, center crop, resize to model input dimensions (read from manifest), RGB ordering, and normalization (mean/std from manifest). Use deterministic fixture JPEGs at `app/src/test/resources/identify-fixtures/preprocess-*.jpg` (a 1×1 white pixel, a 100×100 gradient, a 224×224 grey). RED before §3.3 lands.
- [ ] **3.3** Implement `ImagePreprocessor` in `app/src/main/java/com/darkfactory/plantpotting/identify/model/ImagePreprocessor.kt`. No Android UI imports — uses `BitmapFactory` for decode and the TFLite Support `ImageProcessor` (`ResizeOp`, `NormalizeOp`) for the rest. Reads the input contract from `model_manifest.json` at construction time.
- [ ] **3.4 (test, RED first)** Add `InterpreterFacadeTest` (JVM) using a fake `Interpreter` (interface-extracted wrapper) to assert input/output tensor handling and top-k extraction. Locks the facade contract without instantiating native TFLite. RED before §3.5.
- [ ] **3.5** Implement `InterpreterFacade` in `identify/model/InterpreterFacade.kt`: a thin interface wrapper around the real `org.tensorflow.lite.Interpreter`. The real impl loads the `.tflite` from assets via `AssetFileDescriptor` + memory-mapping; the test impl is a Kotlin fake. This keeps Phase 3 unit-testable on JVM without an emulator.
- [ ] **3.6 (test, RED first)** Add `ModelScoreMapperTest` (JVM) covering each row of the §4.3 confidence table: high-conf direct hit; high-conf-with-margin; low-conf with mapped candidates; low-conf unmapped (empty candidates); top-3 filtering to mapped only. Pass via fake-classifier outputs constructed inline.
- [ ] **3.7** Implement `ModelScoreMapper` in `identify/model/ModelScoreMapper.kt`. Converts raw label-score arrays into a `MappedScore` shape: `data class MappedScore(val accepted: IdentificationResult?, val candidates: List<Candidate>, val lowConfidence: Boolean)`. Reads mapping from `plant_class_map.json`. `Candidate` is a new data class with `speciesId`, `displayName`, `probability: Float`.
- [ ] **3.8 (test, RED first)** Add `OnDevicePlantIdentifierFixturesTest` (JVM, Robolectric for AssetManager). Bundle three small Creative-Commons-licensed JPEGs at `app/src/test/resources/identify-fixtures/`: `monstera-deliciosa.jpg` (~150 KB), `ficus-lyrata.jpg`, `blank-grey.jpg`. Assertions: high-conf paths return the mapped `speciesId`; `blank-grey.jpg` returns `lowConfidence = true` with empty candidates. **Do not assert a minimum top-1 accuracy** — record observed probabilities in test output for the results doc, but the only hard assertion is "high-conf path returns the right species id for clear photos AND blank-grey is low-conf".
- [ ] **3.9** Implement `OnDevicePlantIdentifier` in `identify/OnDevicePlantIdentifier.kt`:
  - `@Singleton`, `@Inject` constructor with `(@ApplicationContext context: Context, preprocessor: ImagePreprocessor, facade: InterpreterFacade, mapper: ModelScoreMapper, dispatcher: CoroutineDispatcher)`.
  - Lazy-load the `.tflite` (via `InterpreterFacade`) on first `identify(jpeg)` call. Subsequent calls reuse the loaded interpreter.
  - Body: decode → preprocess → run inference on `dispatcher` (default `Dispatchers.Default`) → `mapper.map(scores)` → return `IdentificationResult` with `source = IdSource.ON_DEVICE_MODEL`.
  - **Throw `IdentificationFailureException`** (new, in this package) on: model load failure, JPEG decode failure, native interpreter exception. **Do not** convert hard failures into `lowConfidence = true` (per §4.3 distinction).
- [ ] **3.10** Define `IdentificationFailureException` in `identify/` as a thin domain exception: `class IdentificationFailureException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)`.
- [ ] **3.11 (test)** Add `OnDevicePlantIdentifierFailureTest`: forces a model-load failure (e.g., point the facade at a missing asset) and asserts `IdentificationFailureException` is thrown, *not* a `lowConfidence` result.

### Phase 4 — Hilt swap; preserve stub for tests

- [ ] **4.1** Add `OnDeviceIdentifyModule` (`identify/OnDeviceIdentifyModule.kt`): `@Module @InstallIn(SingletonComponent::class)`, `@Binds` `OnDevicePlantIdentifier` to `PlantIdentifier`. Annotate with a clear KDoc note that production wiring lives here.
- [ ] **4.2** Modify `IdentifyModule.kt`: remove the production binding to `StubPlantIdentifier`. The file becomes the location where shared `@Provides` for the identify package live (e.g., the `CoroutineDispatcher` qualifier for inference, if needed) — or the file is deleted outright if it now contains nothing. Implementer picks the cleaner shape; records the choice in the results doc.
- [ ] **4.3 (test)** Add `OnDeviceIdentifyModuleBindingTest` (`@HiltAndroidTest`, instrumentation) asserting that an injected `PlantIdentifier` in production wiring is an instance of `OnDevicePlantIdentifier`.
- [ ] **4.4 (test)** Add `StubPlantIdentifierStillReachableTest` (JVM) that constructs `StubPlantIdentifier` directly via its `@Inject` constructor (or with `null` dispatcher if the constructor permits) and calls `identify(byteArrayOf(0))`, asserting `IdSource.STUB_DETERMINISTIC`. This proves the stub class is alive for the §4.5 grep and for test consumers.
- [ ] **4.5** Add a test-only `FakeFixedIdentifierModule` under `app/src/androidTest/` annotated `@TestInstallIn(replaces = [OnDeviceIdentifyModule::class], components = [SingletonComponent::class])` that binds `FakeFixedIdentifier` (existing test fake). Update existing instrumentation tests that previously relied on the stub binding to use this module instead.
- [ ] **4.6 (verify)** `bash scripts/check-stub-isolation.sh` green. `./gradlew testDebugUnitTest` green.

### Phase 5 — Source-driven badge (UX 2)

- [ ] **5.1 (test, RED first)** Add `ResultScreenBadgeTest` (Compose UI test, JVM Robolectric where possible). Render `ResultScreen` with a `ResultUiState` carrying each (source, lowConfidence) tuple; assert the badge text matches the expected string:
  - `STUB_DETERMINISTIC` / `STUB_RANDOM`, any `lowConfidence` → "Stub identifier — replace in a later sprint" (legacy copy, surfaces only in tests).
  - `ON_DEVICE_MODEL`, `lowConfidence = false` → "On-device match".
  - `ON_DEVICE_MODEL`, `lowConfidence = true` → "On-device match (low confidence)".
  - `CLOUD` → "Cloud match unavailable in this offline build".
- [ ] **5.2** Add four new strings to `app/src/main/res/values/strings.xml`: `result_badge_stub`, `result_badge_on_device`, `result_badge_on_device_low`, `result_badge_cloud`. Keep the legacy `result_stub_badge` string for one sprint as a soft-deprecated alias only if any test source still references it; otherwise delete.
- [ ] **5.3** Extend `ResultUiState` (`result/ResultUiState.kt`): `val source: IdSource = IdSource.STUB_DETERMINISTIC` and `val lowConfidence: Boolean = false`. Defaults preserve every existing test that constructed `ResultUiState` without these fields.
- [ ] **5.4** Extend `Routes.kt`: `result/{speciesId}?source={source}&lowConfidence={lowConfidence}`. Defaults for legacy/test callers: `source=STUB_DETERMINISTIC`, `lowConfidence=false`.
- [ ] **5.5** Update `ResultViewModel` to read `source` and `lowConfidence` from `SavedStateHandle` and populate `ResultUiState`.
- [ ] **5.6** Update `ResultScreen` to choose the badge string from `state.source` + `state.lowConfidence`. Replace any hard-coded `R.string.result_stub_badge` reference.
- [ ] **5.7** Update `CameraViewModel`: change the navigation flow from `MutableSharedFlow<String>` to `MutableSharedFlow<NavCommand>` where `NavCommand` is a sealed class with `Success(speciesId, source, lowConfidence)`, `LowConfidence(candidates: List<Candidate>)`, and `Failure(message: String)`. Map identifier outcomes to the right command per §4.3.
- [ ] **5.8** Update `PlantPottingNavHost` to consume the new `NavCommand` shape and route accordingly: `Success` → `result/...`; `LowConfidence` → `low-confidence-picker?candidates=...`; `Failure` → existing camera failure state (no nav).
- [ ] **5.9 (test)** Update `EndToEndFlowTest` so the test fake identifier returns `source = IdSource.ON_DEVICE_MODEL` (since production wiring is now the on-device model), and the badge assertion reads "On-device match", not the stub copy.

### Phase 6 — `LowConfidencePicker` + `ArchetypePicker` screens

- [ ] **6.1 (test, RED first)** Add `LowConfidencePickerViewModelTest` (JVM) covering: render the top-3 mapped candidates received as nav-args; render the full 16-species manual search; search by common name; search by scientific name; selection emits the chosen `speciesId`; "pick by archetype" CTA emits an archetype-picker navigation event.
- [ ] **6.2** Implement `LowConfidencePickerViewModel` in `app/src/main/java/com/darkfactory/plantpotting/result/LowConfidencePickerViewModel.kt`. Backed by `KnowledgeBase`. Reads candidate list from `SavedStateHandle`.
- [ ] **6.3 (test, RED first)** Add `LowConfidencePickerScreenTest` (Compose UI): renders top-3 row (when present) with common name + scientific name + integer-pct probability; renders the manual search list of 16 KB species; renders the "pick by archetype" CTA at the bottom. Tap on a row routes to `result/{speciesId}?source=ON_DEVICE_MODEL&lowConfidence=true`. Tap on CTA routes to `archetype-picker`.
- [ ] **6.4** Implement `LowConfidencePickerScreen` (`result/LowConfidencePickerScreen.kt`). Vertical layout: top-3 chip row (if non-empty), search field, 16-species list, "pick by archetype" CTA. Minimal styling — no animations.
- [ ] **6.5 (test, RED first)** Add `ArchetypePickerScreenTest` + `ArchetypePickerViewModelTest`: list of 8 archetypes (name + 1-line description); tapping an archetype routes to the existing `recommendation` destination with an archetype-variant route.
- [ ] **6.6** Implement `ArchetypePickerViewModel` + `ArchetypePickerScreen` (`result/ArchetypePickerScreen.kt` etc). 8 rows; minimal styling.
- [ ] **6.7 (test, RED first)** Add `RecommendationEngineArchetypeTest`: `recommendByArchetype("aroid-chunky")` returns the archetype's exact recipe (sums to 100), a rationale that does not name a species, `isBlend = false`.
- [ ] **6.8** Implement `recommendByArchetype(archetypeId)` on `KbRecommendationEngine` (the existing engine class). New method; existing `recommend(speciesId)` path is untouched.
- [ ] **6.9** Wire the new routes into `PlantPottingNavHost`. Update `Routes.kt` with new constants.
- [ ] **6.10 (test)** Add an instrumentation test `LowConfidenceFlowTest` (`@HiltAndroidTest`): inject a `FakeFixedIdentifier` that returns `lowConfidence = true` with a known candidate list. Drive the flow: shutter → `LowConfidencePicker` → tap a top-3 row → `ResultScreen` → `RecommendationScreen`. Assert the badge reads "On-device match (low confidence)".

### Phase 7 — Bug A: `scripts/integration-flow.ps1` fix (parallel with Phases 1–6)

- [ ] **7.1** Read PLANTPOTTING-0002 feedback §Bug A in full. Confirm root cause matches §4.8 above.
- [ ] **7.2 (test scaffold, RED first)** Add `scripts/tests/integration-flow-tests.ps1` (or a `Pester`-style harness if Pester is already on `pwsh`'s default module path; otherwise a plain `.ps1` that exit-codes on failure). Two shims under `scripts/tests/adb-shims/`:
  - `adb-null-root-then-ok.ps1` — first invocation prints the documented stderr to stderr with exit 0 and writes no file; second invocation behaves correctly.
  - `adb-always-null-root.ps1` — always prints the stderr and writes no file.
  Tests: `Wait-ForNode` against the first shim succeeds on retry; against the second shim throws after `maxAttempts` exhaustion.
- [ ] **7.3** In `scripts/integration-flow.ps1`:
  - `Invoke-AdbDump` returns `$true` / `$false`. Soft failures per §4.8: dump exit 0 + file missing; stderr matches the null-root string; `adb pull` exit non-zero. Hard failures (adb itself failing on transport, no device, etc.) still throw with the existing messages.
  - Capture `uiautomator dump`'s stderr via `2>&1` and inspect for the documented string. On match → retry signal regardless of `$LASTEXITCODE`.
  - `Wait-ForNode` checks the return value and continues iterating on `$false`. Throws only after `maxAttempts` exhaustion.
  - After each `adb shell am start`, insert `Start-Sleep -Seconds 2` before the first `Wait-ForNode` call.
  - Bump `Wait-ForNode` default `maxAttempts` from 8 to 12.
- [ ] **7.4** Update `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` (the device-aware expected manifest) to include the device-aware lines the original sprint plan §8.5 specified (e.g., `archetype-name=Aroid Chunky`, `recipe-row-count=5`) **plus** the new badge string (`source-badge=On-device match` or the test-fake variant — implementer confirms the exact dump-string when the fix is verified).
- [ ] **7.5 (verify, must capture)** Capture three transcripts under `docs/sprints/evidence/PLANTPOTTING-0003/`:
  - `transcript-A-cold.txt` — clean cold-launch device-aware run after the fix. Expect green diff.
  - `transcript-B-warm.txt` — re-run on a warm emulator after force-stopping the app. Expect green diff. Proves the race no longer fires.
  - `transcript-C-buildonly.txt` — `-BuildOnly` regression. Expect green diff against `PLANTPOTTING-0001-buildonly.txt`.
- [ ] **7.6** Record the retroactive closure of PLANTPOTTING-0002 §8 line 310 in `docs/sprints/results/PLANTPOTTING-0003.md` with a pointer to `transcript-A-cold.txt`. **Do not edit `docs/sprints/PLANTPOTTING-0002.md`** — codex critique 2.6 of claude holds; cross-sprint doc mutation is unnecessary churn.

### Phase 8 — Documentation, ledger, acceptance evidence

- [ ] **8.1** Author `docs/sprints/results/PLANTPOTTING-0003.md` (same shape as the PLANTPOTTING-0001/0002 results docs): build commands + outputs; model choice rationale (linked to §4.1); mapping coverage summary; Bug A transcript A/B/C links; source-driven-badge before/after notes; fixture-evaluation metrics (top-1, top-3 observed on the bundled fixtures); known gaps; one-line handoff note to PLANTPOTTING-0004 (likely topic: confidence calibration + UI polish).
- [ ] **8.2** Confirm `docs/kb/ml-mapping-notes.md` (authored in §2.7) is complete — every mapping line in `plant_class_map.json` has a justification paragraph.
- [ ] **8.3 (nice-to-have)** Author `docs/ml/model-card-aiy-plants-v1.md` — model source URL, asset sha256s, input contract, label count, mapping strategy, known weaknesses, low-confidence policy, APK size impact. Drop from must-land per §3.2 if time slips.
- [ ] **8.4** Update `docs/sprints/ledger.yaml`: `PLANTPOTTING-0003` `status: done`, refresh `updated`.
- [ ] **8.5 (final verify)** Run the full clean-clone CI chain on the final commit:
  - `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking pixel6Api34DebugAndroidTest`
  - `bash scripts/check-stub-isolation.sh`
  - `pwsh scripts/integration-flow.ps1` (device-aware)
  - `pwsh scripts/integration-flow.ps1 -BuildOnly`
  Record commit hash, commands, and outputs in §8.1's results doc.

---

## 6. Sequencing and dependency rules

```
Phase 0 (setup + contract lock)
   │
   ▼
Phase 1 (TFLite deps + verifyNoNetworking guard)
   │
   ▼
Phase 2 (model + labels + mapping + manifest assets)
   │
   ▼
Phase 3 (preprocessing + facade + mapper + identifier)
   │
   ▼
Phase 4 (Hilt swap; stub stays for tests)
   │
   ├──► Phase 5 (source-driven badge, nav-arg plumbing)
   │
   └──► Phase 6 (LowConfidencePicker + ArchetypePicker)

Phase 7 (Bug A script fix)  — fully parallel with Phases 1–6
Phase 8 (docs, ledger, final verify) — depends on 1–7 green
```

### 6.1 Hard gates

- **§0.4 (`PlantIdentifierContractTest`) RED before §3.1.** Locks the seam before any data-class change.
- **§1.2 (`VerifyNoNetworkingRegressionTest`) RED before §1.1.** Proves the dep audit fires before the TFLite deps land.
- **§2.4 (`ModelManifestTest`) RED before §2.3 (manifest file lands).**
- **§2.6 (`ModelLabelMappingValidationTest`) RED before §2.5 (mapping JSON lands).**
- **§3.2, §3.4, §3.6 RED before §3.3, §3.5, §3.7 respectively.** Standard TDD for behaviour.
- **§3.8 RED before §3.9 (`OnDevicePlantIdentifier` impl).**
- **§5.1 RED before §5.6 (badge swap).**
- **§6.1 / §6.3 / §6.5 / §6.7 RED before their corresponding impl tasks.**
- **§7.2 RED before §7.3 (`Wait-ForNode` change).**
- **`bash scripts/check-stub-isolation.sh` and `./gradlew verifyNoNetworking` must stay green throughout the sprint.** Any task that breaks either is a sprint-blocker bug.

### 6.2 Soft parallels

- Phase 7 (Bug A script fix) touches only `scripts/` and `docs/sprints/evidence/` and runs fully in parallel with Phases 1–6.
- Phase 5 (badge) and Phase 6 (pickers) can be developed in either order once §3.1 lands.
- Phase 2 (assets) and Phase 1 (deps) can overlap once §1.2 is green.

### 6.3 De-scope order (when the implementer slips)

1. Drop §3.2 stretch INT8 variant; ship FP16 (or whatever the AIY V1 default is) alone.
2. Drop §8.3 model card (`docs/ml/model-card-aiy-plants-v1.md`). The `ml-mapping-notes.md` is sufficient editorial coverage.
3. Drop the §3.2 GPU/NNAPI delegate work entirely.
4. Drop §6.5–§6.6 `ArchetypePicker` polish — keep a minimal list with no chip styling.
5. Last-resort: defer §6.10's full instrumentation `LowConfidenceFlowTest` to PLANTPOTTING-0004, leaving the picker covered by §6.3's Compose-UI test only. Document the deferral in the results doc.

**Never drop:** Phase 3 identifier itself, Phase 4 Hilt swap, Phase 5 badge, Phase 6 picker (some form of it), Phase 7 Bug A fix, the seam invariants (§4.4), `verifyNoNetworking` green, `check-stub-isolation.sh` green.

---

## 7. Risks and mitigations

### 7.1 The AIY Plants V1 model doesn't cover all 16 KB species

**Risk:** retail cultivars (e.g., `Philodendron erubescens 'Pink Princess'`, `Hoya carnosa` cultivars) may not appear in the model's ~2,101 label set, or appear only at the genus rank.
**Mitigation:** the §4.2 mapping permits many-to-one (any `Hoya` label maps to `Hoya carnosa`). §2.7's editorial notes document each judgment call. Species that have *no* mapping rank at all surface through the `LowConfidencePicker`'s manual search — every KB species is reachable manually even if the model can't see it. A §3.2 stretch `KbToModelCoverageTest` enforces inverse coverage if time permits.

### 7.2 False-positive recipe from a confident-wrong model

**Risk:** a confident wrong prediction would silently route the user to the wrong substrate recipe — the worst UX failure mode.
**Mitigation:** §4.3's confidence threshold (0.55 plain, 0.45 with 0.18 margin) is conservative. Any uncertain result routes to `LowConfidencePicker`, not to a recipe. **No archetype auto-fallback.** Codex critique 2.2 of claude is right: the safety-critical default is "ask the user", not "guess".

### 7.3 Inference latency exceeds the ≤3 s capture-to-result bar

**Risk:** 224×224 inference + JPEG decode + dispatch overhead could push past 2 s on a cold first shot, leaving <1 s for nav + render.
**Mitigation:** `InterpreterFacade` lazy-loads on first call; once loaded, subsequent inferences are ~120–180 ms on the AOSP image. The §3.2 stretch warm-up (model load during the CameraX bind window) hides the cold-start cost. If the GMD measures >2.5 s end-to-end, drop to the INT8 variant.

### 7.4 `verifyNoNetworking` flags a TFLite transitive

**Risk:** TFLite Support pulls in some Play Services Tasks API. The artefact name `play-services-tasks` is *not* networking but contains the `play-services` substring.
**Mitigation:** §1.2's allowlist matches only on substrings that are unambiguously networking (`okhttp`, `retrofit`, `firebase`, `play-services-network`, `volley`, `ktor-client-okhttp`). `play-services-tasks` does not match. §1.4 re-audits the dep tree explicitly.

### 7.5 Mapping drift when the KB grows

**Risk:** a future sprint adds a new species to `species.json` without a matching entry in `plant_class_map.json`. Silent: that species' label would return `null` and route to manual picker.
**Mitigation:** §2.6's `ModelLabelMappingValidationTest` covers outgoing references (mapping values exist in KB). The §3.2 stretch `KbToModelCoverageTest` (if it lands) covers inverse coverage. Either way, the manual picker over all 16 species is the safety net.

### 7.6 TFLite native `.so` arch mismatch on the AOSP `pixel6Api34` GMD

**Risk:** the GMD image is x86_64; `tensorflow-lite` ships natives for `armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64` by default. Should "just work".
**Mitigation:** §1.4 verifies via the dep audit; §0.2 baseline + §8.5 final verify both run `pixel6Api34DebugAndroidTest`. If a future GMD swap surfaces an arch mismatch, add an `ndk.abiFilters` block.

### 7.7 Compose nav-arg URL-encode limits

**Risk:** passing the top-3 candidate list as a nav-arg string can hit URL-encode length issues if names are long (gemini critique 2.4 of claude).
**Mitigation:** the candidate encoding is `speciesId|probPct` (integer percentage, max 3 entries). KB species ids are kebab-case short tokens (e.g., `monstera-deliciosa`); 3 × ~30 chars + delimiters = under 100 chars total. Well within Android's nav-arg URL-encode tolerance.

### 7.8 Hilt + Compose + CameraX + TFLite test-config combo

**Risk:** PLANTPOTTING-0001 §7.7 and PLANTPOTTING-0002 §6.2 both flagged this combo as a half-day sink. Adding TFLite raises the surface again.
**Mitigation:** TFLite is touched only in unit tests via `InterpreterFacade`'s fake impl (Phase 3 fixture tests run JVM-side via Robolectric). The only instrumentation test that touches the real binding is §4.3's `OnDeviceIdentifyModuleBindingTest` — that only verifies the injected type, not inference. The `FakeFixedIdentifier` covers all camera-flow instrumentation tests.

### 7.9 Bug A's documented race has other triggers we haven't identified

**Risk:** the `null root node returned by UiTestAutomationBridge` string is one symptom; window-state-change mid-dump or other races could produce different errors.
**Mitigation:** §4.8 captures `uiautomator dump`'s stderr broadly. The match condition is the documented string today; widen it to "any non-empty stderr from `uiautomator dump`" if a §7.5 transcript surfaces a new race. The two transcripts in §7.5 cover cold and warm starts.

### 7.10 Windows sandbox filesystem overlay (per project auto-memory)

**Risk:** shell-tool writes outside the project tree may not reach disk on this machine.
**Mitigation:** every artefact path stays under `D:/DarkFactoryProject/Plant potting/`. The model `.tflite` downloads land at `app/src/main/assets/ml/aiy_plants_v1/` — under the tree. Implementer verifies file presence from the user's terminal before claiming acceptance (per the existing PLANTPOTTING-0002 §7.x mitigation pattern).

### 7.11 APK size growth

**Risk:** 25 MB model + ~10 MB TFLite libs + existing app ~15 MB ≈ 50 MB debug APK.
**Mitigation:** APK size growth is unconstrained per the user interview (well below Play Store thresholds). The §3.2 INT8 variant is the lever if a future release sprint needs to compress.

### 7.12 Pre-existing "Stub identifier — replace in a later sprint" copy

**Risk:** PLANTPOTTING-0001 §2.1 prohibited placeholder copy on the recommendation screen; the badge has been a tolerated exception. PLANTPOTTING-0003 retires it from production.
**Mitigation:** §5.6 ensures the production debug build never shows the stub copy on a real capture. Tests that drive `StubPlantIdentifier` still see it — which is correct.

---

## 8. Acceptance criteria

The sprint is done when **every** statement below is observably true.

### 8.1 Build + lint + tests

- [ ] `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking pixel6Api34DebugAndroidTest` green on a clean clone.
- [ ] `bash scripts/check-stub-isolation.sh` green. `grep -R "StubPlantIdentifier" app/src/main/` returns only paths under `app/src/main/.../identify/`.

### 8.2 Seam invariants

- [ ] `PlantIdentifier` interface is byte-for-byte identical to PLANTPOTTING-0001 §4.1's definition. Verified by `git diff main -- app/src/main/java/com/darkfactory/plantpotting/identify/PlantIdentifier.kt` showing only changes to `IdentificationResult`'s field list (the additive `lowConfidence` field), not to the interface block.
- [ ] `PlantIdentifierContractTest` (§0.4) green.
- [ ] `OnDevicePlantIdentifier` is the Hilt-bound `PlantIdentifier` in production, verified by `OnDeviceIdentifyModuleBindingTest` (§4.3).
- [ ] `StubPlantIdentifier` still compiles and identifies — `StubPlantIdentifierStillReachableTest` (§4.4) green. It is *not* wired into production but is reachable from the test source set.

### 8.3 Assets + mapping

- [ ] `app/src/main/assets/ml/aiy_plants_v1/{model.tflite, labels.csv, plant_class_map.json, model_manifest.json, LICENSE-aiy-plants-v1.txt}` all exist.
- [ ] `ModelAssetsPresenceTest`, `ModelManifestTest`, `ModelLabelMappingValidationTest` green.
- [ ] `docs/kb/ml-mapping-notes.md` exists with one justification paragraph per mapping line, no `TODO` substrings.

### 8.4 Identifier behaviour

- [ ] `OnDevicePlantIdentifierFixturesTest` green: clear-photo fixtures map to the right species id with `lowConfidence = false`; `blank-grey.jpg` returns `lowConfidence = true`.
- [ ] `OnDevicePlantIdentifierFailureTest` green: hard failure throws `IdentificationFailureException`, *not* a `lowConfidence` result.
- [ ] `ModelScoreMapperTest` green — every row of the §4.3 confidence table is covered.
- [ ] `ImagePreprocessorTest`, `InterpreterFacadeTest` green.

### 8.5 UX + badge

- [ ] `ResultScreenBadgeTest` green — the badge copy varies per `(IdSource, lowConfidence)` per §5.1's table.
- [ ] `EndToEndFlowTest` on the GMD asserts the production badge reads "On-device match" (not the stub copy) on a clean capture.
- [ ] `LowConfidencePickerScreenTest`, `LowConfidencePickerViewModelTest`, `ArchetypePickerScreenTest`, `ArchetypePickerViewModelTest`, `RecommendationEngineArchetypeTest` all green.
- [ ] `LowConfidenceFlowTest` (instrumentation) green — drives the full shutter → picker → result → recommendation flow.

### 8.6 Bug A (carry-forward)

- [ ] `pwsh ./scripts/integration-flow.ps1` (device-aware) succeeds against a connected `Pixel_6_API_34` emulator. Clean diff against the updated device-aware expected manifest.
- [ ] `pwsh ./scripts/integration-flow.ps1 -BuildOnly` still produces a clean diff against `PLANTPOTTING-0001-buildonly.txt`.
- [ ] Three transcripts in `docs/sprints/evidence/PLANTPOTTING-0003/`: cold, warm, build-only.
- [ ] PLANTPOTTING-0002 §8 line 310 retroactively closed in `docs/sprints/results/PLANTPOTTING-0003.md` with a pointer to the cold transcript. **PLANTPOTTING-0002.md itself is not edited.**

### 8.7 Docs + ledger

- [ ] `docs/sprints/results/PLANTPOTTING-0003.md` exists and contains: build commands + outputs, model variant + sha256 from `model_manifest.json`, mapping coverage summary, Bug A transcripts links, fixture-evaluation metrics (top-1, top-3 observed), known gaps, one-line PLANTPOTTING-0004 handoff.
- [ ] `docs/sprints/ledger.yaml`: `PLANTPOTTING-0003` `status: done`, `updated` timestamp refreshed.
- [ ] Every completed feature task has a paired completed test task. Exceptions documented inline in the results doc.

---

## 9. Handoff note for the `sprint-execute` implementer

You are one of `opus`, `gpt-5.4`, or `gemini`, picked by the user.

- **Read this plan first**, then the anchor files listed in §0.1.
- **Tick `- [ ]` boxes as you go**, not in batches. The skill enforces this.
- **TDD is mandatory** on every `(test, RED first)` task. Don't write the impl before the test fails.
- **No new networking deps.** TFLite is fine; OkHttp / Retrofit / Firebase / Volley / Ktor are not.
- **Do not change the `PlantIdentifier` interface shape.** `§0.4`'s test enforces this.
- **Do not delete `StubPlantIdentifier.kt`.** It stays under `identify/` for test consumption and the §4.5 grep.
- **Do not edit prior sprint documents** (PLANTPOTTING-0001.md, PLANTPOTTING-0002.md). Record carry-forward closure in `PLANTPOTTING-0003.md` results doc only.
- **Sandbox warning** (per the user's auto-memory): shell-tool writes outside `D:/DarkFactoryProject/Plant potting/` may not reach disk on this machine. Verify file presence from the user's terminal before claiming acceptance.
- **If you fall behind, follow §6.3's de-scope order.** Phases 1–7 must-land tasks are non-negotiable; the ledger move depends on them.
- **Phase 7 (Bug A) is fully parallel** with Phases 1–6. Don't let the model integration block the script fix or vice versa.
- **The model accuracy ballpark is evidence, not an acceptance gate.** Record observed top-1 and top-3 on the bundled fixtures in the results doc, but the only hard assertions are "high-conf fixtures route to the right species id" and "blank-grey routes to low-confidence".
