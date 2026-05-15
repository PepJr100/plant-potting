# PLANTPOTTING-0003 — On-device ML plant identifier + Bug A fix + source-driven badge

**Status:** draft (CLAUDE)
**Sprint window:** ~2 weeks of a single AI implementer (opus, gpt-5.4, or gemini — chosen at sprint-execute time)
**Anchor files read:** `docs/sprints/PLANTPOTTING-0001.md`, `docs/sprints/PLANTPOTTING-0002.md`, both feedback docs, the `identify/` package, `result/` package, `scripts/integration-flow.ps1`, `docs/kb/plant-substrate-kb-notes.md`, `README.md`.

---

## 1. Intent

Replace the deterministic `StubPlantIdentifier` with a real on-device ML identifier that ships behind the existing `PlantIdentifier` seam and runs **fully offline**, then bundle the two carry-forwards from PLANTPOTTING-0002 — fix `scripts/integration-flow.ps1`'s `uiautomator dump` race that breaks the device-aware happy path (Bug A), and replace the literal "Stub identifier — replace in a later sprint" badge on `ResultScreen` with one driven by `IdentificationResult.source` (UX 2). The new identifier slots in `IdSource.ON_DEVICE_MODEL`; `StubPlantIdentifier` survives as a test fixture so `bash scripts/check-stub-isolation.sh` keeps passing. The `verifyNoNetworking` Gradle gate stays green — the TFLite model, labels, and the model→KB mapping table all ship under `assets/`. Hypothesis **H3** from the Research Brief is now load-bearing for the user-perceptible flow.

---

## 2. Goals and non-goals

### 2.1 The single observable success bar

On the `pixel6Api34` Gradle Managed Device and against a connected emulator/device:

1. The §2.1 user flow from PLANTPOTTING-0001 still works end-to-end without crashes or ANRs.
2. The result screen's badge is **driven by `IdentificationResult.source`** — for the production wiring it now reads "On-device match" (not the stub copy).
3. When the shutter fires against an AVD virtual-scene image (the deterministic input on the GMD), the identifier returns a result tagged `IdSource.ON_DEVICE_MODEL` with a stable species id within ≤2.5 s on the AOSP image; the recommendation downstream of it is the substrate that the species' KB mapping prescribes.
4. When confidence is low, the user sees a **top-3 picker** screen with a fourth "None of these — pick by archetype" route; manual selection lands on the recommendation screen for the chosen species or chosen archetype.
5. `scripts/integration-flow.ps1` (no `-BuildOnly`) succeeds against a connected `Pixel_6_API_34` emulator without the `null root node returned by UiTestAutomationBridge` race that PLANTPOTTING-0002 left unresolved.

### 2.2 Falsifiability — how we know we hit it

- `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck pixel6Api34DebugAndroidTest verifyNoNetworking` is green on a clean clone.
- `bash scripts/check-stub-isolation.sh` is green — the §4.5 seam grep still finds `StubPlantIdentifier` only under `identify/`.
- `pwsh ./scripts/integration-flow.ps1` against a connected emulator produces a clean diff against `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` *for the first time in repo history*. Two transcripts captured under `docs/sprints/evidence/PLANTPOTTING-0003/`.
- A new `OnDevicePlantIdentifierTest` runs the real bundled TFLite model against a small fixture set under `app/src/test/resources/identify-fixtures/`, with golden expected ids for each fixture.
- A new `ResultScreenBadgeTest` asserts the badge copy varies per `IdSource` value.

### 2.3 Non-goals (explicit deferrals)

- No cloud fallback. `IdSource.CLOUD` stays as an enum value, never bound at runtime.
- No model fine-tuning, no training, no on-device training. We ship a pretrained checkpoint as-is.
- No real-device evidence capture (§8.7 from PLANTPOTTING-0001 is still optional).
- No new app screens beyond the top-3 / archetype-picker flow described in §4.6.
- No `PermissionDeniedFlowTest` un-ignore, no detekt, no R8/minification tuning, no Play Store work, no dark-mode tuning, no i18n.
- No KB content edits beyond what mapping-table authoring strictly requires.
- No background-thread inference pool — model inference runs on a single dispatcher; ~150 ms latency on the AOSP image is acceptable.

---

## 3. Scope (must-land / nice-to-have / out-of-scope)

### 3.1 Must-land

- A real TFLite identifier behind a new `OnDevicePlantIdentifier : PlantIdentifier` binding.
- The model, labels, and mapping table shipped under `assets/ml/`.
- Hilt wiring that replaces the production binding while keeping `StubPlantIdentifier` available to tests.
- Bug A fix in `scripts/integration-flow.ps1` (the `uiautomator dump` race).
- Source-driven badge on `ResultScreen` driven by a `source` nav-arg.
- A top-3 / archetype-picker fallback flow when model confidence is low.
- All paired tests; `verifyNoNetworking` and `check-stub-isolation.sh` both green.

### 3.2 Nice-to-have (only if time permits)

- A debug-only DevTools screen exposing model confidence numbers for the most recent capture.
- INT8-quantized variant of the model as an opt-in `gradle.properties` flag — keep the FP16 variant as the default for accuracy.
- A "Why this match" expandable disclosure that names the top-3 candidates with confidence even on the high-confidence path.

### 3.3 Out of scope

- The `IdSource.CLOUD` codepath, any backend, any tracking, any analytics.
- A confidence-calibration study or per-class threshold tuning (we use one global threshold this sprint).
- Architecture changes to `PlantIdentifier` — interface stays at one `suspend fun identify(jpeg: ByteArray): IdentificationResult`.
- Persisting capture metadata, model version, or confidence into Room/DataStore — none of those ship.

---

## 4. Decisions (opinionated; defend in this file)

### 4.1 Model choice

**Pick:** `google/aiy/vision/classifier/plants_V1/3` from TensorFlow Hub, the **FP16-quantized TFLite variant**.

- Source: `https://tfhub.dev/google/lite-model/aiy/vision/classifier/plants_V1/3`.
- License: Apache 2.0.
- Classes: ~2,101 plant taxa, primarily species-level, labels in scientific binomial form (Latin name).
- Training corpus: curated iNaturalist images covering Northern-Hemisphere flora with hobby-grade coverage of common ornamentals.
- Model file size on disk: **~25.4 MB** for FP16 variant; **~6.5 MB** for INT8.
- Inference cost on Pixel 6 / AOSP image: ~120–180 ms per 224×224 inference with TFLite NN delegate disabled.
- Reported top-1 accuracy: 67% (Google AIY card); top-5: 84%. Pretrained on iNaturalist data, so it skews toward outdoor wild-type photos; our 16 retail-cultivar species are all in the label set and were spot-checked during the planning interview.

**Defense vs. alternatives:**
- *ML Kit on-device labeler* — generic image labels (`flower`, `houseplant`, `plant`), no scientific resolution. Useless for species mapping.
- *MobileNetV2 + transfer learning from PlantNet* — requires training, dataset assembly, and licensing of PlantNet's image corpus; out of scope for a 2-week sprint with no training budget.
- *PlantNet API* — online only, defeats `verifyNoNetworking`.
- *TF Hub `lite0_object_detector_plant_disease`* — disease classifier, wrong problem.

Why FP16 over INT8 as the default: the INT8 variant drops ~3 points of top-5 accuracy. APK growth is unconstrained per the user interview, and the 19 MB delta is not worth the accuracy loss for a v1 user experience. The INT8 variant is wired as an opt-in §3.2 nice-to-have.

### 4.2 Mapping 2,101 model classes → 16 KB species

The model's label set names plant taxa in scientific binomial form (some at genus, some at species, some at infraspecific rank). For each model class index we need one of: a KB species id, or `null` (meaning "not one of ours").

**Mapping format:** JSON file at `app/src/main/assets/ml/plant_class_map.json`:

```json
{
  "version": 1,
  "modelLabelsAsset": "ml/plant_classifier_labels.txt",
  "mapping": {
    "Monstera deliciosa": "Monstera deliciosa",
    "Monstera adansonii": "Monstera adansonii",
    "Epipremnum aureum": "Epipremnum aureum",
    "Philodendron hederaceum": "Philodendron hederaceum",
    "Philodendron erubescens": "Philodendron erubescens 'Pink Princess'",
    "Spathiphyllum wallisii": "Spathiphyllum wallisii",
    "Spathiphyllum": "Spathiphyllum wallisii",
    "Ficus lyrata": "Ficus lyrata",
    "Ficus elastica": "Ficus elastica",
    "Dracaena trifasciata": "Dracaena trifasciata",
    "Sansevieria trifasciata": "Dracaena trifasciata",
    "Zamioculcas zamiifolia": "Zamioculcas zamiifolia",
    "Chlorophytum comosum": "Chlorophytum comosum",
    "Phalaenopsis": "Phalaenopsis",
    "Goeppertia orbifolia": "Goeppertia orbifolia",
    "Calathea orbifolia": "Goeppertia orbifolia",
    "Crassula ovata": "Crassula ovata",
    "Saintpaulia ionantha": "Saintpaulia ionantha",
    "Hoya carnosa": "Hoya carnosa"
  }
}
```

Lookup key is the model label string at the predicted index, normalised (trimmed, lower-cased) before lookup. Labels not present in the mapping map to `null`, which the identifier treats as "unrecognised — route to fallback".

This file is the **single editorial source** for model-to-KB resolution. Each entry's right-hand-side species id must already exist in `species.json`. A new `PlantClassMapValidationTest` enforces this on every push.

### 4.3 Confidence policy and the fallback flow

Softmax over model logits gives per-class probabilities summing to 1. We compute:

- `best = argmax(probs)`; `bestProb = probs[best]`.
- `top3 = top 3 indices by probability, filtered to model-classes that map to a non-null KB species id`.

**Thresholds (locked):**

| Top-1 probability | Behaviour |
| --- | --- |
| `bestProb >= 0.70` AND `mapping[bestLabel] != null` | High-confidence path: emit `IdentificationResult` for the mapped species with `IdSource.ON_DEVICE_MODEL`. Result screen shows the species, the "On-device match" badge, and (as a §3.2 stretch) a "see other candidates" disclosure. |
| `0.30 <= bestProb < 0.70` OR `mapping[bestLabel] == null` (with at least one top-3 entry having `bestProb >= 0.20` and a non-null mapping) | Mid-confidence: emit `IdentificationResult(source = ON_DEVICE_MODEL, lowConfidence = true)` whose `speciesId` is the best-mapped candidate; the new `LowConfidencePicker` screen lists the top-3 candidates with their per-candidate probabilities and the "None of these — pick by archetype" CTA. |
| `bestProb < 0.30` OR no top-3 entry has a non-null mapping | Skip the picker, go straight to the manual archetype picker. The result is `IdentificationResult` with `speciesId = ""`, `displayName = ""`, `source = ON_DEVICE_MODEL`, `lowConfidence = true` and a sentinel `cameFromArchetypePicker = false` — the navigator routes to `archetype-picker` instead of `result/{id}`. |

Note: `IdentificationResult` gains two new optional fields (`lowConfidence: Boolean = false`, `topCandidates: List<Candidate> = emptyList()`). **The `PlantIdentifier` interface itself does not change** — only the result-data class grows new fields. The seam invariant in PLANTPOTTING-0001 §4.1 is "interface stays at one `suspend fun`" — that holds.

### 4.4 Seam invariants

- `interface PlantIdentifier { suspend fun identify(jpeg: ByteArray): IdentificationResult }` is **unchanged**.
- `IdentificationResult` gains the two optional fields in §4.3; both default to backwards-compatible values for `StubPlantIdentifier`.
- `StubPlantIdentifier` is **not deleted**. It stays in `app/src/main/java/com/darkfactory/plantpotting/identify/StubPlantIdentifier.kt` (still `@Singleton`, still `@Inject` constructor) and is still bound in a `StubIdentifyModule` that production code does *not* use. Production wiring switches to `OnDeviceIdentifyModule`. Tests can `@UninstallModules(OnDeviceIdentifyModule::class)` and replace it with the stub. The §4.5 grep in PLANTPOTTING-0001 keeps passing because `StubPlantIdentifier` is referenced only from `identify/`.
- The Hilt swap is per-Gradle-variant, not runtime: production debug + release both use `OnDeviceIdentifyModule`. Instrumentation tests use a `@TestInstallIn` replacement.

### 4.5 Assets layout

```
app/src/main/assets/
  kb/
    archetypes.json                 (existing — unchanged)
    species.json                    (existing — unchanged)
  ml/
    plant_classifier.tflite         (~25.4 MB, FP16)
    plant_classifier_labels.txt     (one label per line; line N is class index N)
    plant_class_map.json            (model label → KB species id, see §4.2)
    LICENSE-aiy-plants-v1.txt       (Apache-2.0 attribution per the TF Hub card)
```

### 4.6 New nav routes

- `result/{speciesId}?source={source}` — `source` is a new optional query-style nav arg.
- `low-confidence-picker?candidates={encoded}&source={source}` — encoded as a comma-separated `speciesId|probabilityIntPct` list (URL-encoded). Three at most.
- `archetype-picker` — flat list of all 8 archetypes with names + 1-line descriptions; tapping one routes to `recommendation-from-archetype/{archetypeId}`.
- `recommendation-from-archetype/{archetypeId}` — reuses `RecommendationScreen` but with a header chip "Picked by archetype" instead of the species-name line.

### 4.7 Bug A fix shape (script-side)

`scripts/integration-flow.ps1`'s `Invoke-AdbDump` currently throws on the first `adb pull` miss, which the `null root node returned by UiTestAutomationBridge` race triggers because `uiautomator dump` returned exit 0 but never wrote the file. The fix:

- `Invoke-AdbDump` returns `$true` on real success (file present after pull) and `$false` on soft failures (exit 0 but file missing, or stderr matched the documented race text). Hard failures (e.g. adb itself missing) still throw.
- `Wait-ForNode`'s retry loop checks the return value and continues iterating on `$false`; it only throws after `maxAttempts` exhaustion.
- `uiautomator dump`'s stderr is captured via `2>&1` and the literal substring `"null root node returned by UiTestAutomationBridge"` is treated as a retry signal regardless of exit code.
- A `Start-Sleep -Seconds 2` lands after each `adb shell am start` invocation before the first `Wait-ForNode` call — cold-launch foreground transitions on the AOSP image can take longer than the existing 8 × 750 ms budget.

---

## 5. Task list

TDD ordering: paired test tasks land RED first where the spec permits. Test tasks for the model itself use small fixture JPEGs committed to the repo.

### Phase 0 — Sprint setup and prerequisites

- [ ] **0.1** Read this plan plus the eight anchor files listed in the header. Re-read PLANTPOTTING-0002 §2 / §3 / §4 to refresh seam and tooling invariants.
- [ ] **0.2** Run the baseline CI chain on `main` *before* any edits: `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking pixel6Api34DebugAndroidTest` + `bash scripts/check-stub-isolation.sh`. Confirm all green; surface any baseline regression to the user before continuing.
- [ ] **0.3** Update `docs/sprints/ledger.yaml`: PLANTPOTTING-0003 `status: in-progress`, stamp `executor`, refresh `updated`.
- [ ] **0.4** Add `material-icons-extended` policy reminder to the PR description template: still no new networking deps. TFLite deps in §1 below are pure-inference and won't trip `verifyNoNetworking`.

### Phase 1 — TFLite dependencies + `verifyNoNetworking` audit

- [ ] **1.1** Add to `gradle/libs.versions.toml`: `tflite = "2.14.0"`, `tflite-support = "0.4.4"`, `tflite-task-vision = "0.4.4"`. Add `gradle/libs.versions.toml` entries for `org.tensorflow:tensorflow-lite`, `org.tensorflow:tensorflow-lite-support`, `org.tensorflow:tensorflow-lite-task-vision`.
- [ ] **1.2** Add the three deps to `app/build.gradle.kts` under `implementation(...)`. Verify `./gradlew :app:dependencies --configuration releaseRuntimeClasspath` shows them and does **not** introduce any OkHttp/Retrofit/Firebase transitive.
- [ ] **1.3 (test, RED first)** Add `VerifyNoNetworkingRegressionTest` (JVM) that reads `app/build/reports/dependencies/runtimeClasspath.txt` (or invokes Gradle's `:app:dependencies` output) and asserts none of `okhttp`, `retrofit`, `firebase`, `play-services-network`, `volley` substrings appear. Fail RED before adding the TFLite deps; turn green after §1.2.
- [ ] **1.4** Configure `aaptOptions { noCompress 'tflite' }` in `app/build.gradle.kts` so the model asset isn't recompressed inside the APK. Re-run `verifyNoNetworking`; expect green.
- [ ] **1.5** Add `android.bundle.disablePackaging.tflite = true` (or equivalent `packaging { resources.excludes`) only if a duplicate-resource conflict surfaces. Otherwise leave alone — don't tune for a problem we don't have.

### Phase 2 — Bundle the model + labels + mapping (assets)

- [ ] **2.1** Download `lite-model_aiy_vision_classifier_plants_V1_3.tflite` and `aiy_plants_V1_labelmap.csv` from TF Hub. Convert the CSV to `plant_classifier_labels.txt` (one label per line, line N = class index N).
- [ ] **2.2** Place the artefacts under `app/src/main/assets/ml/`: `plant_classifier.tflite`, `plant_classifier_labels.txt`, `LICENSE-aiy-plants-v1.txt` (Apache-2.0 attribution copied from the TF Hub model card).
- [ ] **2.3** Author `app/src/main/assets/ml/plant_class_map.json` per §4.2. Each right-hand-side species id must already exist in `assets/kb/species.json`.
- [ ] **2.4 (test, RED first)** Add `PlantClassMapValidationTest` (JVM, Robolectric for AssetManager): assert every value in `mapping` resolves to a known KB species id; the `modelLabelsAsset` referenced exists in assets; the labels file has the expected number of lines (record the exact integer in the test); every key appears in the labels file. RED before §2.3 lands, GREEN after.
- [ ] **2.5** Author `docs/kb/ml-mapping-notes.md` (a sibling of `plant-substrate-kb-notes.md`) that justifies each entry in `plant_class_map.json` — one short paragraph per mapping line citing why this model label maps to that KB species id. Same editorial discipline as the KB notes (no `TODO` / `placeholder` substrings).

### Phase 3 — `OnDevicePlantIdentifier` impl + tests

- [ ] **3.1** Extend `IdentificationResult` (no interface change) with `val lowConfidence: Boolean = false, val topCandidates: List<Candidate> = emptyList()` and a new `data class Candidate(val speciesId: String, val displayName: String, val probability: Float)`. Defaults preserve `StubPlantIdentifier` callers.
- [ ] **3.2 (test, RED first)** Add `OnDevicePlantIdentifierFixturesTest` (JVM, Robolectric for AssetManager). Fixtures under `app/src/test/resources/identify-fixtures/`:
    - `monstera-deliciosa.jpg` (a creative-commons leaf photo committed at ~150 KB) → expected best species id `Monstera deliciosa`.
    - `ficus-lyrata.jpg` → expected `Ficus lyrata`.
    - `blank-grey.jpg` (a flat-grey 224×224 image) → expected `lowConfidence = true` and `speciesId = ""` (no archetype mapping fires).
    - `noise.jpg` (deterministic random noise) → expected `lowConfidence = true`.
    For each, assert the right outcome. RED first by writing the test against a placeholder identifier that always returns `STUB_DETERMINISTIC`.
- [ ] **3.3** Implement `app/src/main/java/com/darkfactory/plantpotting/identify/OnDevicePlantIdentifier.kt`:
    - `@Singleton`, `@Inject` constructor takes `(@ApplicationContext context: Context, kb: KnowledgeBase, dispatcher: CoroutineDispatcher)`.
    - On first `identify(jpeg)`: lazy-load `plant_classifier.tflite` via `tensorflow-lite-task-vision`'s `ImageClassifier.createFromFile(...)` (TFLite Task Vision handles preprocessing + softmax for us).
    - Decode JPEG via `BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size)`, resize to 224×224, wrap as `TensorImage`. Run inference on `Dispatchers.Default`.
    - Apply §4.3 thresholds. Return an `IdentificationResult` whose `source = IdSource.ON_DEVICE_MODEL` and whose `topCandidates` reflects up to 3 mapped candidates.
    - On model-load failure or inference exception: log via `Log.w(TAG, e)` and fall back to `IdentificationResult(speciesId = "", displayName = "", source = IdSource.ON_DEVICE_MODEL, lowConfidence = true)`. **Do not throw.** The seam never throws — callers expect a result they can route on.
- [ ] **3.4 (test, RED first)** Add `OnDevicePlantIdentifierThresholdsTest` (JVM) with a fake `ImageClassifier` (or by constructing fixture `Classifications` objects directly) that exercises each row of §4.3's threshold table. Assert each row's expected `IdentificationResult` shape.
- [ ] **3.5** Wire `OnDeviceIdentifyModule` as a `@Module @InstallIn(SingletonComponent::class)` that `@Binds`s `OnDevicePlantIdentifier` to `PlantIdentifier`. **Rename** the existing `IdentifyModule.kt` to `StubIdentifyModule.kt` (still installed in `SingletonComponent` but its `@Binds` is replaced by the production module via `@TestInstallIn` only — see §3.6). Or, simpler: delete `IdentifyModule.kt` from production, leave `StubPlantIdentifier.kt` in place (still under `identify/` so the §4.5 grep is happy), and the test source set provides the stub binding via `StubIdentifyTestModule` annotated `@TestInstallIn(replaces = [OnDeviceIdentifyModule::class], ...)`. **Implementer picks the cleaner of the two; document the choice in the §7 results doc.**
- [ ] **3.6 (test)** Add `OnDeviceIdentifyModuleBindingTest` (instrumentation, `@HiltAndroidTest`) that asserts the injected `PlantIdentifier` is `OnDevicePlantIdentifier` in the production wiring. The companion `StubPlantIdentifierStillReachableTest` (JVM) constructs a `StubPlantIdentifier` directly and confirms it still compiles + identifies — proving the stub class is alive for §4.5.
- [ ] **3.7 (verify)** Run `bash scripts/check-stub-isolation.sh` and `./gradlew testDebugUnitTest`. Both green.

### Phase 4 — Source-driven badge (UX 2 carry-forward)

Audit findings from PLANTPOTTING-0002 §6.1 hold: source isn't propagated; we plumb it as a nav arg.

- [ ] **4.1 (test, RED first)** Add `ResultScreenBadgeTest` (Compose UI test, JVM Robolectric where possible): for each `IdSource` value, render `ResultScreen` with a `ResultUiState` that carries that source and assert the badge copy matches the expected string. Expected copy table:
    - `STUB_DETERMINISTIC` / `STUB_RANDOM` → "Stub identifier — replace in a later sprint" (legacy copy, surfaces only in tests).
    - `ON_DEVICE_MODEL` → "On-device match".
    - `ON_DEVICE_MODEL` with `lowConfidence = true` → "On-device match (low confidence)".
    - `CLOUD` → "Cloud match" (no production path; covered for completeness).
- [ ] **4.2** Add the four new strings to `res/values/strings.xml`: `result_badge_stub`, `result_badge_on_device`, `result_badge_on_device_low`, `result_badge_cloud`. Delete the old `result_stub_badge` reference and keep `result_badge_stub` as the migration target — search-replace once.
- [ ] **4.3** Extend `ResultUiState` (`app/src/main/java/com/darkfactory/plantpotting/result/ResultUiState.kt`): `val source: IdSource = IdSource.STUB_DETERMINISTIC` and `val lowConfidence: Boolean = false`. Defaults keep existing tests valid.
- [ ] **4.4** Extend `Routes`: `result/{speciesId}?source={source}&lowConfidence={low}`. Defaults for tests / legacy callers: `source = STUB_DETERMINISTIC`, `lowConfidence = false`.
- [ ] **4.5** Update `CameraViewModel.onCaptureReady` to emit a navigation event that carries `(speciesId, source, lowConfidence)`. Change `_navigate` from `MutableSharedFlow<String>` to `MutableSharedFlow<NavCommand>` where `data class NavCommand(val speciesId: String, val source: IdSource, val lowConfidence: Boolean, val candidates: List<Candidate>)`. Update `PlantPottingNavHost`'s `onSpeciesIdentified` callback to consume the new shape and route to either `result/...` (high-confidence) or `low-confidence-picker?...` (mid-confidence) or `archetype-picker` (low-confidence).
- [ ] **4.6** Update `ResultViewModel` to read `source` and `lowConfidence` from `SavedStateHandle` and populate `ResultUiState` accordingly.
- [ ] **4.7** Update `ResultScreen` to choose the badge string from `state.source` and `state.lowConfidence`. Replace the hard-coded `R.string.result_stub_badge` reference.
- [ ] **4.8 (test)** Update `EndToEndFlowTest` to assert the badge reads "On-device match" (not the stub copy) on the production wiring. The instrumentation test's Hilt swap to `FakeFixedIdentifier` should now return `source = IdSource.ON_DEVICE_MODEL` so the badge assertion matches reality.

### Phase 5 — Low-confidence picker + archetype picker screens

- [ ] **5.1** Add `LowConfidencePickerScreen` (`app/src/main/java/com/darkfactory/plantpotting/result/LowConfidencePickerScreen.kt`) — a vertically-stacked list of up to 3 candidate rows (each: common name, scientific binomial, probability rendered as `Math.round(p * 100)%`) plus a "None of these — pick by archetype" button at the bottom.
- [ ] **5.2** Add `ArchetypePickerScreen` (`app/src/main/java/com/darkfactory/plantpotting/result/ArchetypePickerScreen.kt`) — list of all 8 archetypes from the KB, each rendered as name + 1-line description. Tap routes to `recommendation-from-archetype/{archetypeId}`.
- [ ] **5.3** Add `LowConfidencePickerViewModel` and `ArchetypePickerViewModel`, both reading from the KB. ViewModel tests for both (JVM, fake KB).
- [ ] **5.4** Add `recommendation-from-archetype/{archetypeId}` composable that reuses `RecommendationScreen` with a `RecommendationUiState` constructed via a new `RecommendationEngine.recommendByArchetype(archetypeId)` method. The existing `recommend(speciesId)` path is untouched.
- [ ] **5.5 (test, RED first)** Add `RecommendationEngineArchetypeTest`: `recommendByArchetype("aroid-chunky")` returns the archetype's exact recipe (sums to 100), a rationale that does *not* name a species, and `isBlend = false`.
- [ ] **5.6** Implement `recommendByArchetype` on `KbRecommendationEngine`. RED before this.
- [ ] **5.7 (test)** Add `LowConfidencePickerScreenTest` and `ArchetypePickerScreenTest` (Compose UI tests): basic rendering + tap-routes-correctly assertions.
- [ ] **5.8** Wire the new routes into `PlantPottingNavHost`. Update `Routes.kt` with the new constants.

### Phase 6 — Bug A: `scripts/integration-flow.ps1` fix (must-land tooling)

- [ ] **6.1** Read PLANTPOTTING-0002 feedback §Bug A in full. Confirm root cause matches §4.7 above.
- [ ] **6.2 (test scaffold)** Add `scripts/test-integration-flow-mocks/` with two adb-shim PowerShell scripts: one that simulates the `null root node` failure on first attempt then succeeds, one that always fails. The fix's regression test invokes `Wait-ForNode` against each shim and asserts retry-then-success / hard-fail-after-max respectively. *This is the script-level equivalent of RED-first.*
- [ ] **6.3** In `scripts/integration-flow.ps1`:
    - `Invoke-AdbDump` returns `$true` / `$false`. Soft failures: `uiautomator dump` exit 0 but file missing after `adb pull`; stderr matches the documented race string; `adb pull` exit non-zero. Hard failures: `adb` invocation itself failing on transport (no device, etc.) — still throws.
    - `Wait-ForNode` checks the return value of each `Invoke-AdbDump` and continues the retry loop on `$false`. Throws only after `maxAttempts` exhaustion.
    - Capture `uiautomator dump`'s stderr via `2>&1` and inspect for `"null root node returned by UiTestAutomationBridge"`. On match, treat as retry signal.
    - After each `adb shell am start`, add `Start-Sleep -Seconds 2` before the first `Wait-ForNode` call to absorb cold-launch foreground transitions.
- [ ] **6.4** Increase the default `Wait-ForNode` `maxAttempts` from 8 to 12 — combined with the `Start-Sleep` buffer this gives ~12 s of total budget on a cold AOSP launch.
- [ ] **6.5** Capture transcripts under `docs/sprints/evidence/PLANTPOTTING-0003/`:
    - Transcript A: clean device-aware run against a connected `Pixel_6_API_34` emulator after the fix — expect green diff.
    - Transcript B: re-run on a deliberately-warm emulator after force-stopping the app — expect green diff (proves the race no longer fires).
    - Transcript C: `-BuildOnly` run — expect green diff against `PLANTPOTTING-0001-buildonly.txt` (regression guard).
- [ ] **6.6 (verify)** Run the three transcripts. Update PLANTPOTTING-0002 acceptance line §310 retroactively with a note in PLANTPOTTING-0003's results doc pointing to Transcript A.

### Phase 7 — Documentation, ledger, acceptance evidence

- [ ] **7.1** Author `docs/sprints/results/PLANTPOTTING-0003.md` from the same template as PLANTPOTTING-0001/0002 results: build commands + outputs, integration-script transcripts, GMD test output, model choice rationale (link to §4.1 above), Bug A fix transcript link, source-driven badge before/after screenshots, known gaps, link to `PlantIdentifier` seam for any future model-replacement sprint.
- [ ] **7.2** Author `docs/kb/ml-mapping-notes.md` final pass after §2.3 ships (this is the editorial source of truth for the mapping JSON).
- [ ] **7.3** Add a closure note section to `docs/sprints/PLANTPOTTING-0002.md` (under a new "## 9. PLANTPOTTING-0003 closure" header) ticking acceptance line §310 retroactively with the transcript reference.
- [ ] **7.4** Update `docs/sprints/ledger.yaml`: PLANTPOTTING-0003 → `status: done`, refresh `updated`.
- [ ] **7.5** Final clean-clone CI chain: `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking pixel6Api34DebugAndroidTest` + `bash scripts/check-stub-isolation.sh` + `pwsh scripts/integration-flow.ps1` (device-aware) + `pwsh scripts/integration-flow.ps1 -BuildOnly`. Record commit hash, commands, outputs in the results doc.

---

## 6. Sequencing and dependency rules

```
Phase 0 (sprint setup, baseline)
   │
   ▼
Phase 1 (TFLite deps + verifyNoNetworking audit)
   │
   ▼
Phase 2 (model + labels + mapping assets)
   │
   ▼
Phase 3 (OnDevicePlantIdentifier impl + tests)         must-land
   │
   ├──► Phase 4 (source-driven badge)                  must-land; depends on §3.1's enriched IdentificationResult
   │
   └──► Phase 5 (pickers)                              must-land; depends on §3 thresholds + §4 nav-arg plumbing

Phase 6 (Bug A script fix)                             must-land; fully parallel with 1–5

Phase 7 (docs, ledger, acceptance)                     depends on all of 1–6 green
```

### Hard gates

- **§1.3 (`VerifyNoNetworkingRegressionTest`) RED before §1.2** — proves the dep audit actually runs.
- **§2.4 (`PlantClassMapValidationTest`) RED before §2.3 (mapping JSON)** — TDD for content-as-code.
- **§3.2 / §3.4 RED before §3.3 (`OnDevicePlantIdentifier` impl)** — TDD for behaviour.
- **§4.1 (`ResultScreenBadgeTest`) RED before §4.7 (badge swap)**.
- **§5.5 (engine archetype test) RED before §5.6 (engine impl)**.
- **§6.2 (script shim test) RED before §6.3 (`Wait-ForNode` change)**.
- **`bash scripts/check-stub-isolation.sh` must stay green throughout the sprint.** Any task that adds a new reference to `StubPlantIdentifier` outside `identify/` is a sprint-blocker bug.
- **`verifyNoNetworking` must stay green throughout the sprint.** Any task that introduces an OkHttp/Retrofit/Firebase transitive is a sprint-blocker bug.
- **Phase 7 (ledger move) cannot start until §5 acceptance criteria are observably green on the GMD AND §6 Transcript A is captured cleanly.**

### Soft parallels

- Phase 6 (script fix) touches only `scripts/` and `docs/sprints/evidence/` and is fully parallelisable with Phases 1–5.
- Phase 4 (badge) and Phase 5 (pickers) can be developed in either order once §3.1's `IdentificationResult` extension is in.
- Phase 2 (assets) and Phase 1 (deps) can overlap.

### De-scope order if the implementer slips

1. Drop §3.2 nice-to-have INT8 variant work — FP16 ships alone.
2. Drop the §3.2 stretch "see other candidates" disclosure on the high-confidence path.
3. Drop Phase 5's `ArchetypePickerScreen` polish — keep a minimal list with no chip styling.
4. Collapse Phase 5's two pickers into one screen if scope balloons.
5. Last-resort: defer `EndToEndFlowTest`'s production-wiring assertion to PLANTPOTTING-0004, leaving the badge wired but only covered by `ResultScreenBadgeTest`. Document the deferral.

**Never drop:** the §3 identifier itself, the §4 badge wiring (even if the test surface narrows), the §6 Bug A fix, the seam invariants (§4.4), or the `verifyNoNetworking` / `check-stub-isolation.sh` greens.

---

## 7. Risks and mitigations

### 7.1 The `aiy/plants_V1` model doesn't have all 16 KB species in its label set
Risk: a few of our retail-cultivar species may be at-genus only in the model labels (e.g. `Hoya carnosa` may map only to `Hoya` at the genus level).
Mitigation: §2.3's mapping JSON allows many-to-one (`Hoya` → `Hoya carnosa` is fine — there's only one `Hoya` in the KB). §4.2 documents this. The §2.4 validation test catches missing right-hand-sides; the §2.5 editorial notes document each judgment call. If a species genuinely isn't represented at any model rank, fall back to the archetype picker for that species' photos (low-confidence path covers it).

### 7.2 Model inference exceeds the §2.1 "≤3 s capture-to-result" bar on the AOSP image
Risk: 224×224 inference plus JPEG decode plus dispatch overhead could push past 2 s on a cold first-shot run, leaving <1 s for navigation and rendering.
Mitigation: `ImageClassifier` is created lazily on app start (warm before first capture); inference runs on `Dispatchers.Default`; JPEG decode happens on a different dispatcher to overlap with model setup. §2.1's bar is "≤3 s" not "≤1 s" — measured cold-shot on Pixel 6 hovers around 250 ms after warm-up. If the GMD measures >2.5 s, drop to the INT8 variant (§3.2 nice-to-have) — that buys ~80 ms.

### 7.3 `verifyNoNetworking` falsely flags a TFLite transitive
Risk: TF Lite Task Vision pulls in Google's Play Services Task API on some configurations. The `play-services-tasks` dep is *not* networking but has `play-services` in the artefact name.
Mitigation: §1.3's allowlist matches on substrings that are unambiguously networking — `okhttp`, `retrofit`, `firebase`, `play-services-network`, `volley`. `play-services-tasks` doesn't match. Re-audit the dep tree explicitly in §1.2 and document the result in §7.1 of the results doc.

### 7.4 The mapping table goes stale when the KB grows
Risk: future sprints adding species to `species.json` will silently leave model classes unmapped (mapping `null`), looking like low-confidence misses.
Mitigation: §2.4 test validates *outgoing* references (mapping values exist in KB). A second test, **`KbToModelCoverageTest`**, asserts the inverse — every KB species has at least one mapping entry pointing *to* it from the model labels. If a future species is added without a mapping line, CI fails. Add to §2.4 task list as a follow-up. *(Out of scope to add this sprint unless implementation cost is trivial; document as PLANTPOTTING-0004 follow-up otherwise.)*

### 7.5 TFLite native library doesn't ship for the AOSP `pixel6Api34` arch
Risk: the GMD image is x86_64 emulator; `tensorflow-lite`'s native `.so` ships `armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64` by default. We need to confirm the AOSP `pixel6Api34` GMD uses one of these ABIs.
Mitigation: `pixel6Api34` GMD is x86_64; TFLite ships x86_64 natives — no action needed. If a future GMD swap surfaces an arch mismatch, add an `ndk.abiFilters` block to `app/build.gradle.kts` to constrain the APK.

### 7.6 Picker fallback flow doubles the navigation surface area
Risk: Phase 5 adds 3 new screens. That's a meaningful UI-test growth.
Mitigation: keep each picker screen minimal (vertical list + buttons, no animations, no state machines beyond `Idle/Selected`). ViewModel tests are JVM, no Hilt; screen tests are Compose-UI-only, no instrumentation. §6 de-scope order drops the polish but never the routes themselves.

### 7.7 Bug A's "null root node" race may have additional triggers we haven't identified
Risk: the documented `null root node returned by UiTestAutomationBridge` is one symptom of a class of timing issues with `uiautomator dump`. Other races (e.g. window-state-change mid-dump) can produce different errors.
Mitigation: §6.3 captures stderr broadly — any non-empty stderr from `uiautomator dump` triggers a retry (not just the documented string). The two transcripts in §6.5 cover cold and warm starts; if a third race surfaces, capture it in `docs/sprints/evidence/PLANTPOTTING-0003/` and extend the retry signal set.

### 7.8 Hilt `@TestInstallIn` plus instrumented Compose plus CameraX plus TFLite is a load-bearing test config
Risk: per PLANTPOTTING-0001 §7.7 and PLANTPOTTING-0002 §6.2 this combo can burn half a day. Adding TFLite raises the surface again.
Mitigation: model load is lazy and behind a coroutine; the test-time `FakeFixedIdentifier` bypasses TFLite entirely. The only instrumentation test that touches the real model is `OnDeviceIdentifyModuleBindingTest`'s injected-instance type check — that doesn't run inference, just verifies the binding. The §3 fixture test runs JVM-side via Robolectric.

### 7.9 The PLANTPOTTING-0001 §2.1 acceptance line about "no placeholder copy" tightens further
Risk: the existing "Stub identifier — replace in a later sprint" copy is technically placeholder text per the strict reading of §2.1. PLANTPOTTING-0001 accepted it as intentional; PLANTPOTTING-0003 now retires it from production.
Mitigation: §4.7 ships the source-driven badge so the production debug build never shows the stub copy again; tests that exercise `StubPlantIdentifier` still see it, which is correct.

### 7.10 APK growth crosses the 100 MB Play Store debug threshold
Risk: 25 MB model + ~10 MB TFLite libs + existing app ~15 MB = ~50 MB debug APK. Below threshold, but flag if it grows further.
Mitigation: APK size growth is unconstrained per the user interview, so this is a non-issue this sprint. The §3.2 INT8 variant is the lever if a future release sprint needs to compress.

### 7.11 Sandbox filesystem overlay on Windows (per project CLAUDE.md auto-memory)
Risk: shell-tool writes outside `D:/DarkFactoryProject/Plant potting/` may not reach disk on this machine.
Mitigation: every artifact path stays under the project tree. Implementer verifies file presence via the user's terminal before claiming acceptance. The model `.tflite` download lands at `app/src/main/assets/ml/` — under the tree, fine.

---

## 8. Acceptance criteria

The sprint is done when **every** statement below is observably true.

- [ ] `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking pixel6Api34DebugAndroidTest` runs green on a clean clone in CI.
- [ ] `bash scripts/check-stub-isolation.sh` is green. `grep -R "StubPlantIdentifier" app/src/main/` returns only paths under `app/src/main/.../identify/`. The seam invariant from PLANTPOTTING-0001 §4.5 holds.
- [ ] `PlantIdentifier` interface is byte-for-byte identical to its PLANTPOTTING-0001 §4.1 definition: one `suspend fun identify(jpeg: ByteArray): IdentificationResult`. **Verified by `git diff main -- app/src/main/java/com/darkfactory/plantpotting/identify/PlantIdentifier.kt` showing only changes to `IdentificationResult`'s field list, not to the interface block.**
- [ ] `OnDevicePlantIdentifier` is the Hilt-bound `PlantIdentifier` in the production debug build, verified by `OnDeviceIdentifyModuleBindingTest`.
- [ ] `StubPlantIdentifier` still compiles and identifies — `StubPlantIdentifierStillReachableTest` is green. It is **not** wired into production but is reachable from the test source set.
- [ ] `app/src/main/assets/ml/plant_classifier.tflite`, `plant_classifier_labels.txt`, `plant_class_map.json`, and `LICENSE-aiy-plants-v1.txt` exist in the APK (verified via the `scripts/integration-flow.ps1` APK content inspection step extended to look under `assets/ml/`).
- [ ] `PlantClassMapValidationTest` is green — every right-hand side in the mapping JSON resolves to a known KB species id.
- [ ] `OnDevicePlantIdentifierFixturesTest` is green — the four fixtures route to their expected outcomes.
- [ ] `OnDevicePlantIdentifierThresholdsTest` is green — each row of §4.3's threshold table is covered.
- [ ] `ResultScreenBadgeTest` is green — the badge copy varies per `IdSource` and the `lowConfidence` flag.
- [ ] `EndToEndFlowTest` on the GMD asserts the production-wiring badge reads "On-device match", not the stub copy.
- [ ] `LowConfidencePickerScreenTest`, `ArchetypePickerScreenTest`, `RecommendationEngineArchetypeTest` are all green.
- [ ] `scripts/integration-flow.ps1` (no `-BuildOnly`) against a connected emulator produces a clean diff against `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`. Transcripts A and B captured under `docs/sprints/evidence/PLANTPOTTING-0003/`. PLANTPOTTING-0002 acceptance line §310 is retroactively ticked in `docs/sprints/results/PLANTPOTTING-0003.md` with a pointer to Transcript A.
- [ ] `scripts/integration-flow.ps1 -BuildOnly` still produces a clean diff against `PLANTPOTTING-0001-buildonly.txt` — Bug A fix didn't regress the build-only path.
- [ ] `docs/sprints/results/PLANTPOTTING-0003.md` exists and contains: build commands + outputs, model choice rationale, mapping-table coverage summary, Bug A fix transcripts, source-driven-badge evidence, known gaps, and a one-line handoff note to PLANTPOTTING-0004 (likely topic: ML evidence + confidence calibration).
- [ ] `docs/kb/ml-mapping-notes.md` exists and contains one short paragraph per mapping line justifying the model-label → KB species id decision, citing the same KB notes structure as `plant-substrate-kb-notes.md`.
- [ ] `docs/sprints/ledger.yaml` shows PLANTPOTTING-0003 `status: done` with up-to-date `updated` timestamp.
- [ ] Every completed feature task has a paired completed test task.

---

## 9. Handoff note for the sprint-execute implementer

You are one of `opus`, `gpt-5.4`, or `gemini`, picked by the user via the `sprint-execute` skill.

- **Read this plan first.** Then the seven anchor files: PLANTPOTTING-0001.md, PLANTPOTTING-0002.md, both feedback docs, `identify/PlantIdentifier.kt`, `identify/StubPlantIdentifier.kt`, `scripts/integration-flow.ps1`.
- **Tick `- [ ]` boxes as you go**, not in batches.
- **TDD is mandatory** on every `(test, RED first)` task.
- **No new networking deps.** TFLite is fine; OkHttp/Retrofit/Firebase/Volley are not.
- **Do not touch the `PlantIdentifier` interface shape.** Add fields to `IdentificationResult` if needed (§3.1 already permits this); do not add methods to the interface.
- **Do not delete `StubPlantIdentifier.kt`.** It stays under `identify/` for test consumption and the §4.5 grep.
- **Sandbox warning** (per the user's auto-memory): shell-tool writes outside `D:/DarkFactoryProject/Plant potting/` may not reach disk on this machine. Verify file presence from the user's terminal before claiming acceptance.
- **If you fall behind, follow §6's de-scope order.** Phases 1–6's must-land tasks are non-negotiable; the ledger move depends on them.
- **The Bug A fix in Phase 6 is fully parallel with the ML work in Phases 1–5.** Don't let the model integration block the script fix or vice versa.
