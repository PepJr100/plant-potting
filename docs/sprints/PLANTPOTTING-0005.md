# PLANTPOTTING-0005 — Post-shutter polish + un-defer carry-forward from 0003/0004

**Status:** planned
**Sprint shape:** polish + un-defer. Broader than the 0002 / 0004 fix sprints; narrower than 0001 / 0003 feature sprints. ~5–7 days for a single AI implementer (opus / gpt-5.4 / gemini — picked at `sprint-execute` time).
**Primary sources:** carry-forward bullets in `docs/sprints/results/PLANTPOTTING-0004.md` §5 and "Notes for next sprint planner" in `docs/sprints/feedback/PLANTPOTTING-0004/feedback.md`.
**Predecessor:** PLANTPOTTING-0004 (UINT8 dtype + testTagsAsResourceId bridge, both fixed; review clean).

---

## 1. Intent

PLANTPOTTING-0003 shipped the on-device ML identifier; PLANTPOTTING-0004 fixed the two bugs that landed with it. The app now identifies plants on-device end-to-end. But because the AIY V1/3 model covers only 2 of 16 KB species verbatim, the **headline post-shutter screen on most captures is `LowConfidencePicker`, not `ResultScreen`** — and that screen is skeletal next to the polished surfaces around it. PLANTPOTTING-0005 closes six debts in parallel:

1. **Polish `LowConfidencePicker`** as the primary post-shutter destination (not a fallback). Add a sub-headline that names the coverage limit honestly, empty-state composables for "no mapped candidates" and "search returned no results", visual de-emphasis of the archetype CTA, and a small-screen viewport assertion so search + at least one species row stay reachable.
2. **Polish `CameraUiState.Failure`.** Replace the easy-to-miss top-center `Text` with a persistent bottom-anchored Material3 `Banner`-style `Card` carrying a `Try again` retry affordance that resets state to `Idle`. No new screen.
3. **Delete the global `TestIdentifyModule`** and migrate the seven instrumentation tests that depend on it to per-test `@BindValue` fakes (the architecturally cleaner shape that PLANTPOTTING-0004 §4.5 deferred). Add a production-shape regression guard so a future global swap cannot silently downgrade `OnDeviceModelRealInterpreterTest`.
4. **Add `LowConfidenceFlowTest`** (B3 from 0003, never landed) and **re-enable `PermissionDeniedFlowTest.openSettingsIntentFiresOnPermanentDenial`** (`@Ignore`'d since PLANTPOTTING-0001) by driving denial state via `FakeCameraPermissionGuard`, not the system permission dialog.
5. **Begin confidence calibration.** Land the `per_species_thresholds` mechanism in `ModelManifest` (consumed by `ModelScoreMapper`); swap the procedural Monstera fixture for a real CC-licensed photograph; run a probe to capture observed top-1/top-3/score and route; add one accuracy-bearing assertion to `OnDeviceModelRealInterpreterTest` informed by the probe (preferred form pre-committed; honest fallback pre-committed). **Seed override values only if the probe shows a specific in-vocab species fails the global threshold by a margin a per-class override would close** — otherwise the map ships empty.
6. **Add `docs/ROADMAP.md`** (sprint-review skill expects it) and document calibration rationale in `docs/kb/ml-mapping-notes.md`.

---

## 2. Goals and non-goals

### 2.1 Working decisions (durable project policy — lift these into reviewer notes)

- **Treat `LowConfidencePicker` as the default post-shutter experience** for unmapped, weak, or uncertain predictions. It is not a fallback.
- **Preserve honest confidence language.** Never imply the model identified a species when the user made the final selection. The `on-device match (low confidence)` source badge stays exact for user-picked low-conf results.
- **Keep `ResultScreen` source-badge behaviour unchanged.** UI polish does not rename source semantics.
- **Keep the real-model androidTest production-shaped.** `OnDeviceModelRealInterpreterTest` must inject the concrete `OnDevicePlantIdentifier`. After `TestIdentifyModule` removal, an explicit assertion guards against re-introduction of a global fake.
- **Calibrate modestly.** Land the mechanism + one falsifiable real-photo assertion. Seed per-species overrides only with probe evidence justifying them. Do not claim broad model accuracy.
- **Photo licence is auditable.** Source, author (when available), licence, URL, and retrieval date live next to the fixture.
- **No `@Ignore`s land on net.** The single `@Ignore`'d test is un-ignored; new `@Ignore`s are not introduced.
- **No re-baselining `expected-artifacts/PLANTPOTTING-0001.txt`.** UI polish must preserve the manifest fields the integration-flow script captures.

### 2.2 The observable success bars

On `pixel6Api34` GMD + JVM/CI:

1. **`LowConfidencePicker` looks finished** — sub-headline; "no mapped candidates" info card; "no search results" empty state; outlined-style archetype CTA; trailing chevron on candidate chips; small-screen reachability of search + ≥1 species row.
2. **`CameraUiState.Failure` is unmissable** — bottom-anchored `Banner`-style `Card` with `Try again`. Shutter stays enabled and reachable. A new capture clears any prior `Failure` reason (ViewModel test asserts this).
3. **`TestIdentifyModule` is deleted** — `grep -R "TestIdentifyModule\|@TestInstallIn(.*OnDeviceIdentifyModule" app/src/androidTest/` → zero hits. The seven dependent tests use local `@BindValue`. A new assertion confirms `OnDeviceModelRealInterpreterTest` resolves `PlantIdentifier` to `OnDevicePlantIdentifier`, not `FakeFixedIdentifier`.
4. **Tests added / un-ignored:** `LowConfidenceFlowTest` (instrumentation); `PermissionDeniedFlowTest.openSettingsIntentFiresOnPermanentDenial` un-`@Ignore`d; new Robolectric Compose tests for both `LowConfidencePicker` empty states and the `CameraUiState.Failure` banner.
5. **Real Monstera fixture in place** — CC-licensed photograph; LICENSE.txt updated with attribution + licence name + source URL + retrieval date; `OnDeviceModelRealInterpreterTest` asserts one accuracy-bearing claim.
6. **Per-class threshold mechanism wired** — `ModelManifest.perSpeciesThresholds: Map<String, Float>`; `ModelScoreMapper` consults override first then global default; JVM unit test covers both branches. Seeded values only if the probe demands them.
7. **`docs/ROADMAP.md` exists**; `docs/kb/ml-mapping-notes.md` carries the calibration provenance entry; `README.md` links the roadmap.

### 2.3 Falsifiability — how we know we hit it

- `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` GREEN.
- `./gradlew --no-daemon pixel6Api34DebugAndroidTest` GREEN: existing 10 + `LowConfidenceFlowTest` + `PermissionDeniedFlowTest.openSettingsIntentFiresOnPermanentDenial` un-ignored. `@Ignore` count in `app/src/androidTest/` drops from 1 to 0.
- `bash scripts/check-stub-isolation.sh` GREEN.
- `pwsh ./scripts/integration-flow.ps1` (cold + warm) and `-BuildOnly` all emit `Integration manifest diff passed.` against the existing `docs/sprints/expected-artifacts/PLANTPOTTING-0001*.txt` — no re-baselining.
- `grep -R "TestIdentifyModule" app/src/androidTest/` → zero hits.
- `grep -R "@Ignore" app/src/androidTest/` → zero hits.
- `grep -R "testTagsAsResourceId" app/src/main/` → exactly one hit at `MainActivity.kt` (no regression from 0004).
- §0 contract-lock tests RED on baseline, GREEN at close (recorded in results doc).
- Probe-run output (top-1 species + score, top-3, route) is recorded in `docs/sprints/results/PLANTPOTTING-0005.md` and `docs/kb/ml-mapping-notes.md`.

### 2.4 Non-goals (explicit deferrals to PLANTPOTTING-0006+)

- Model swap (AIY V1/3 stays).
- INT8 / GPU / NNAPI delegate.
- Net-new screens (no `CaptureFailedScreen`, no `Settings`, no `ModelInfoScreen`). `LowConfidencePicker` and `CameraScreen` are polished in-place.
- ML training / retraining.
- Multi-species fixture sweep — one real Monstera photo is the start.
- `PlantIdentifier` / `IdentificationResult` interface changes — seam invariants from PLANTPOTTING-0003 §4.4 hold.
- KB edits (`species.json`, `archetypes.json`) — locked.
- AGP / Kotlin / Compose / Hilt / TFLite version bumps — locked.
- Full README rewrite — append a "Current state (post-0005)" section and link `ROADMAP.md`; no other prose churn.
- Re-baselining `docs/sprints/expected-artifacts/PLANTPOTTING-0001*.txt`.

---

## 3. Task list

TDD ordering: behaviour-changing tasks have paired test tasks that land RED first. Tick each `- [ ]` to `- [x]` as work lands — do **not** batch.

### Phase 0 — Setup, contract locks, baseline, inventory

- [x] **0.1** Re-read: `docs/sprints/results/PLANTPOTTING-0004.md`, `docs/sprints/feedback/PLANTPOTTING-0004/feedback.md`, and the anchor files — `LowConfidencePickerScreen.kt`, `LowConfidencePickerViewModel.kt`, `CameraUiState.kt`, `CameraScreen.kt` (Failure region around line 140), `TestIdentifyModule.kt`, `FakeFixedIdentifier.kt`, `PermissionDeniedFlowTest.kt`, `FakeCameraPermissionGuard.kt`, `ModelScoreMapper.kt`, `ModelManifest.kt`, `ModelManifestReader.kt`, `OnDeviceModelRealInterpreterTest.kt`, `app/src/androidTest/assets/identify-fixtures/LICENSE.txt`, `OnDeviceIdentifyProvidersModule.kt`.
- [x] **0.2 (baseline)** Run `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` + `bash scripts/check-stub-isolation.sh` + `./gradlew --no-daemon pixel6Api34DebugAndroidTest` on clean `main` **before** any edits. Capture full output to `docs/sprints/results/PLANTPOTTING-0005-baseline.txt` and `…-baseline-gmd.txt`. Surface any pre-existing regression to the user before continuing.
- [x] **0.3** Update `docs/sprints/ledger.yaml`: `PLANTPOTTING-0005` `status: in-progress`, stamp `executor`, refresh `updated`.
- [x] **0.4 (inventory)** Grep `app/src/androidTest/` for every `@HiltAndroidTest` that launches `MainActivity` or injects `PlantIdentifier`/`CameraViewModel`. Expected list: `EndToEndFlowTest`, `CameraPreviewLayoutTest`, `CameraScreenSmokeTest`, `CameraScreenBindStateTest`, `CameraScreenBoundStateTest`, `PermissionResumeRecoveryTest`, `PermissionDeniedFlowTest` — **seven tests, not six.** Record the final list in the results doc; if the count differs, update §3 migration tasks before any code moves.
- [x] **0.5** Open `PermissionScreenTags.kt` and confirm the test tag for the "Open Settings" button used in the permanent-denial path (not `GRANT_BUTTON`). Record the tag name in the results doc; cross-check against the `@Ignore`d test body.
- [x] **0.6 (contract-lock test, RED first)** Add `PerSpeciesThresholdsContractTest` at `app/src/test/java/com/darkfactory/plantpotting/identify/model/PerSpeciesThresholdsContractTest.kt`. Reflection-based: asserts `ModelManifest::class.java.declaredFields.any { it.name == "perSpeciesThresholds" && Map::class.java.isAssignableFrom(it.type) }`. RED today. Flips GREEN after §5.2.
- [x] **0.7 (contract-lock test, RED first)** Add `LowConfidencePickerSubtitleContractTest` at `app/src/test/java/com/darkfactory/plantpotting/result/LowConfidencePickerSubtitleContractTest.kt` (Robolectric + `createComposeRule()`). Wires the screen against a no-arg `LowConfidencePickerViewModel`; asserts `composeRule.onNodeWithTag(LowConfidencePickerTags.SUBTITLE).assertExists()`. RED today.
- [x] **0.8 (contract-lock test, RED first)** Add `CameraFailureBannerContractTest` (Robolectric Compose): wires `CameraScreen` against `CameraUiState.Failure("test")`; asserts `composeRule.onNodeWithTag(CameraScreenTags.FAILURE_BANNER).assertExists()`. RED today. Flips GREEN after §2.3.
- [x] **0.9** **No JVM-reflection contract-test for `TestIdentifyModule` absence.** The JVM `test` source set does not see androidTest classpath; the reflection test in Claude's draft is unreliable. Replace with the §7.5 `grep` audit (file absence) and the §3.10 production-shape assertion (`OnDeviceModelRealInterpreterTest` resolves to `OnDevicePlantIdentifier`).

### Phase 1 — LowConfidencePicker polish

- [x] **1.1** Add `LowConfidencePickerTags.SUBTITLE = "lowConf.subtitle"`. Add `R.string.low_conf_subtitle` (copy: "This model recognises a limited plant vocabulary — please confirm or pick below."). Render `Text` with `MaterialTheme.typography.bodyMedium` under the existing headline, tagged `SUBTITLE`. (Flips §0.7 GREEN.) **Copy may be revised by the executor if it lands awkwardly with the live screen** — record any deviation in the results doc.
- [x] **1.2 (test, RED first)** Add `LowConfidencePickerEmptyCandidatesTest` to `app/src/test/java/com/darkfactory/plantpotting/result/LowConfidencePickerScreenTest.kt`. Methods: `emptyCandidatesShowsInfoCard` (constructs the screen with `topCandidates = emptyList()`, asserts `onNodeWithTag(LowConfidencePickerTags.NO_CANDIDATES_EMPTY).assertExists()`); `nonEmptyCandidatesHidesInfoCard`. RED today.
- [x] **1.3** Add `LowConfidencePickerTags.NO_CANDIDATES_EMPTY = "lowConf.noCandidatesEmpty"`. Implement: when `viewModel.topCandidates.isEmpty()`, render an outlined `Card` with copy from `R.string.low_conf_no_candidates` ("No close matches — pick from the full list below.") in place of the chip row.
- [x] **1.4 (test, RED first)** Add `searchWithNoMatchesShowsEmptyState`: drive `onQueryChange("zzzz")`, assert `onNodeWithTag(LowConfidencePickerTags.SEARCH_EMPTY).assertExists()` and the species list is gone. RED today.
- [x] **1.5** Add `LowConfidencePickerTags.SEARCH_EMPTY = "lowConf.searchEmpty"`. When `query.isNotBlank() && filtered.isEmpty()`, replace the `LazyColumn` with `Text` "No species match \"$query\"" (`R.string.low_conf_search_empty` with one positional arg).
- [x] **1.6** Replace the bottom `Button` for the archetype path with `OutlinedButton`. Update the copy from the current "I don't know — pick by archetype" to "None of these look like my plant" (Gemini's product-language proposal — better confirmation-flow framing). Existing tag `PICK_BY_ARCHETYPE` preserved.
- [x] **1.7** Add a trailing icon to candidate `AssistChip`s to make tappability obvious. **Do NOT pull `material-icons-extended` for one icon** — use an inline vector or an icon already on the classpath.
- [x] **1.8 (test)** Add `lowConfidencePickerSmallScreenReachability` — at a 360 × 640 dp viewport (`createComposeRule()` test config), `LowConfidencePickerTags.SEARCH` and at least one row from `LowConfidencePickerTags.SPECIES_LIST` (or any `speciesTag(...)`) are reachable without layout overlap. Falsifies the "search drops below the fold" regression that polish could introduce.
- [x] **1.9** Preserve existing tags: `LowConfidencePickerTags.candidateTag(...)`, `speciesTag(...)`, `SEARCH`, `SPECIES_LIST`, `PICK_BY_ARCHETYPE`. New tags are additive only. **No `integration-flow.ps1` or `expected-artifacts` change required.**
- [x] **1.10** Run `./gradlew --no-daemon testDebugUnitTest` to confirm Phase 1 tests are GREEN. Capture output to `docs/sprints/results/PLANTPOTTING-0005-phase1-verify.txt`.

### Phase 2 — CameraUiState.Failure polish

- [ ] **2.1 (test, RED first)** Add `CameraFailureBannerTest` to `app/src/test/java/com/darkfactory/plantpotting/camera/CameraScreenTest.kt` (or create). Methods: `failureStateRendersBanner` (asserts `onNodeWithTag(CameraScreenTags.FAILURE_BANNER).assertExists()` when state = `CameraUiState.Failure("test reason")`); `failureBannerRetryClickResetsState` (clicks `FAILURE_RETRY`, asserts state flips to `Idle`). RED today.
- [ ] **2.2 (test, RED first — Codex addition)** Add `newCaptureClearsFailureState` to `CameraViewModelTest`: with state seeded `Failure("prior")`, invoke `onCaptureReady(jpegBytes)`, assert state is no longer `Failure` (the new banner is persistent unlike the old top-center `Text`; a stale `Failure` reason would lie). RED today.
- [ ] **2.3** Add `CameraScreenTags.FAILURE_BANNER = "camera.failureBanner"` and `CameraScreenTags.FAILURE_RETRY = "camera.failureRetry"`. Keep `CameraScreenTags.ERROR` as an alias on the body text inside the banner for back-compat with any test that already greps for it.
- [ ] **2.4** Replace the bare top-center `Text` (current `CameraScreen.kt:140-149`) with a Material3-style `Banner` built as an `OutlinedCard` (Material3 Compose has no first-class `Banner` composable — build with `OutlinedCard`), anchored at `Alignment.BottomCenter` with `.padding(bottom = 144.dp)` so the shutter stays visible above. Contents: leading `Icons.Outlined.ErrorOutline` (use existing classpath icon, no `material-icons-extended` add), body `Text(state.reason, tag = CameraScreenTags.ERROR)`, trailing `TextButton(onClick = viewModel::reset, tag = CameraScreenTags.FAILURE_RETRY) { Text(stringResource(R.string.camera_failure_retry)) }`. **Tap-target check:** verify the shutter (FAB) is not occluded; the banner sits *above* the shutter, not on top of it.
- [ ] **2.5** Verify the existing `shutterEnabled` predicate still allows `Failure` state to re-enable the shutter — the new banner doesn't change this, but assert via the §2.1 test.
- [ ] **2.6** Add `R.string.camera_failure_retry` ("Try again") and `R.string.camera_failure_banner_content_description` ("Capture failed" or similar — for the leading icon a11y).
- [ ] **2.7** Run `./gradlew --no-daemon testDebugUnitTest`. Capture to `docs/sprints/results/PLANTPOTTING-0005-phase2-verify.txt`.

### Phase 3 — Test-infra refactor: `TestIdentifyModule` removal + `@BindValue` migration

**Order matters — migrate every test before deleting the module, then delete. Compile-check after each test, not in batches** (Hilt's generated-graph error blames the generated file, not the source line).

- [ ] **3.1** Extend `FakeFixedIdentifier`'s constructor to accept `lowConfidence: Boolean = false` and `speciesId: String = "monstera-deliciosa"` (and any other knobs the seven tests will need). Do not break existing call sites — all new params have defaults.
- [ ] **3.2** Migrate `EndToEndFlowTest`: add `@BindValue @JvmField var fakeIdentifier: PlantIdentifier = FakeFixedIdentifier()`. Run `./gradlew --no-daemon :app:compileDebugAndroidTestKotlin` and confirm GREEN before moving on.
- [ ] **3.3** Migrate `CameraPreviewLayoutTest` (same pattern). Compile-check.
- [ ] **3.4** Migrate `CameraScreenSmokeTest`. Compile-check.
- [ ] **3.5** Migrate `CameraScreenBindStateTest`. Compile-check.
- [ ] **3.6** Migrate `CameraScreenBoundStateTest` (Codex inventory found this; Claude's draft missed it). Compile-check.
- [ ] **3.7** Migrate `PermissionResumeRecoveryTest`. Compile-check.
- [ ] **3.8** Migrate `PermissionDeniedFlowTest` (existing methods only — §4 handles the un-ignore). Compile-check.
- [ ] **3.9** Delete `app/src/androidTest/java/com/darkfactory/plantpotting/identify/TestIdentifyModule.kt`. Confirm `./gradlew --no-daemon :app:compileDebugAndroidTestKotlin` GREEN.
- [ ] **3.10 (production-shape regression guard — Codex addition)** Add an explicit androidTest assertion that `OnDeviceModelRealInterpreterTest` resolves `PlantIdentifier` to `OnDevicePlantIdentifier`, not `FakeFixedIdentifier`. Implementation: inject `PlantIdentifier` (or `@ApplicationContext` + manual resolution) and assert `identifier::class.qualifiedName == "com.darkfactory.plantpotting.identify.OnDevicePlantIdentifier"`. Without this, a future global swap could silently downgrade the real-model test back to a fake — the exact regression PLANTPOTTING-0004 §4.5 deferred.
- [ ] **3.11** Update comments in migrated androidTests that referenced "the global fake" to name the local `@BindValue` binding instead. Remove the obsolete §1.7 / §4.5 explanation in `OnDeviceModelRealInterpreterTest`.
- [ ] **3.12** Run `./gradlew --no-daemon pixel6Api34DebugAndroidTest`. All seven migrated tests + `OnDeviceModelRealInterpreterTest` pass. Capture to `docs/sprints/results/PLANTPOTTING-0005-phase3-gmd.txt`.

### Phase 4 — `LowConfidenceFlowTest` + `PermissionDeniedFlowTest` un-ignore

- [ ] **4.1** Add `app/src/androidTest/java/com/darkfactory/plantpotting/LowConfidenceFlowTest.kt`. `@HiltAndroidTest`; `@BindValue` a `FakeFixedIdentifier(lowConfidence = true, speciesId = "monstera-deliciosa")` and seed it (via constructor or a `mostRecentCandidates` setter — see §4.2) so the chip row has at least one entry. Rule chain: `HiltAndroidRule`, `GrantPermissionRule.grant(Manifest.permission.CAMERA)`, `createAndroidComposeRule<MainActivity>()`, `IntentsRule` if needed.
- [ ] **4.2** Drive the capture path **via `ViewModelProbe.findCameraViewModel()?.onCaptureReady(jpegBytes)`** (Codex seam — concrete and doesn't depend on a real camera frame). If `ViewModelProbe` doesn't exist in the current code, name the closest existing seam in the results doc and use it. Test method asserts:
  - After capture, `LowConfidencePicker` is displayed (`onNodeWithTag(LowConfidencePickerTags.HEADLINE).assertIsDisplayed()` and `…SUBTITLE.assertExists()`).
  - The seeded `monstera-deliciosa` candidate row is visible (`LowConfidencePickerTags.candidateTag("monstera-deliciosa")` or `speciesTag(...)`).
  - Clicking the candidate routes to `ResultScreen` with `lowConfidence = true`.
  - `ResultScreenTags.SOURCE_BADGE` reads the established low-confidence copy (cross-reference `EndToEndFlowTest` for the exact assertion pattern).
  - Tapping `result.seePottingMix` reaches `RecommendationScreen`; the archetype name and recipe list render.
- [ ] **4.3 (Codex addition — second path)** Add a second test method that exercises the **search → species-row tap** path: drive `onQueryChange("monstera")`, tap a search result row via `speciesTag(...)`, verify navigation to `ResultScreen`. Covers the path that real users hit when the model emits zero mapped candidates.
- [ ] **4.4** Extend `FakeCameraPermissionGuard` with a `permanentlyDenied: Boolean = false` constructor flag (or equivalent state-injection point). Wire it into whatever method production code consults for "permanently denied" semantics; verify the production path that flips the screen to "Open Settings" copy + tag is reachable via this flag.
- [ ] **4.5** Replace the body of `openSettingsIntentFiresOnPermanentDenial`: use a per-test `FakeGuardStateRule(granted = false, permanentlyDenied = true)`; assert the screen surfaces the permanent-denied state without driving the system dialog; click the "Open Settings" button (tag confirmed at §0.5); assert `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` intent fires via `Intents.intended(allOf(hasAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)))`.
- [ ] **4.6** Remove `@Ignore` from `openSettingsIntentFiresOnPermanentDenial`.
- [ ] **4.7** Run `./gradlew --no-daemon pixel6Api34DebugAndroidTest`. Confirm `@Ignore` count drops to zero. Capture to `docs/sprints/results/PLANTPOTTING-0005-phase4-gmd.txt`.

### Phase 5 — Confidence calibration scaffold (probe → assert)

- [ ] **5.1 (test, RED first)** Add `ModelScoreMapperPerSpeciesThresholdTest` at `app/src/test/java/com/darkfactory/plantpotting/identify/model/ModelScoreMapperPerSpeciesThresholdTest.kt`. Methods: `perSpeciesOverrideUsedWhenPresent` (manifest seeded with `perSpeciesThresholds = mapOf("monstera-deliciosa" to 0.35f)`, top-1 score = 0.40f → high-confidence); `globalThresholdUsedWhenNoOverride` (override map empty, score = 0.40f → low-confidence vs `high_confidence_plain = 0.55`). RED today.
- [ ] **5.2** Add `val perSpeciesThresholds: Map<String, Float> = emptyMap()` to `ModelManifest`. Parse `per_species_thresholds` (snake-case) in `ModelManifestReader`; optional — defaults to empty if absent. Add a `_comment_per_species_thresholds` breadcrumb in `model_manifest.json` explaining override semantics even though no values are seeded yet. (Flips §0.6 GREEN.)
- [ ] **5.3** Update `ModelScoreMapper`: in the method that decides `lowConfidence` for a given top-1, consult `manifest.perSpeciesThresholds[speciesId] ?: manifest.thresholds.high_confidence_plain`. Override only the `_plain` value — `high_confidence_margin_min` / `_margin_delta` overrides are deferred (premature surface; see §4 below).
- [ ] **5.4** Source a CC-licensed Monstera deliciosa photograph from Wikimedia Commons (CC-BY or CC0). Resize to 480×480 JPEG-q80; budget ≤200 KB. Replace `app/src/androidTest/assets/identify-fixtures/monstera-deliciosa.jpg`. Update `LICENSE.txt` with: source URL, author (if available), licence name, retrieval date (Codex addition). **Verify the licence terms permit redistribution under this repo's licence** before committing. Note in the commit body that the previous procedural fixture (seed `0x4D4F4E54`) is replaced.
- [ ] **5.5 (probe, NOT a checked-in test)** With the real fixture in place, run `OnDeviceModelRealInterpreterTest` with a temporary `println` (or a one-off `@Test fun probe()` you remove before commit) that captures: top-1 species id + score; top-3 species/score pairs; `result.lowConfidence`; `result.source`. Run on GMD. **Record the captured output verbatim in `docs/sprints/results/PLANTPOTTING-0005.md`.** This is research, not a test gate.
- [ ] **5.6 (test, after probe)** Add the accuracy-bearing assertion to `OnDeviceModelRealInterpreterTest`. Pre-committed forms:
  - **Preferred:** `assertThat(result.speciesId).isEqualTo("monstera-deliciosa"); assertThat(result.lowConfidence).isFalse()`.
  - **Fallback (only if §5.5 shows the real photo still routes low-conf):** `assertThat(result.source).isEqualTo(IdSource.ON_DEVICE_MODEL); assertThat(observedTop3SpeciesIds).contains("monstera-deliciosa")` — pulling top-3 via `(identifier as? CandidateProvider)?.mostRecentCandidates`.
  
  Pick **only after** §5.5. **Do not invent a third weaker form.** Record the decision (preferred vs fallback) with the probe numbers as evidence in the results doc. **Anti-overfit prohibition (Codex):** do not tune thresholds in `model_manifest.json` to force a false green — if the photo doesn't support a direct species-id assertion, take the fallback and document the limitation.
- [ ] **5.7 (conditional — Gemini's bolder calibration)** If §5.5 shows that an in-vocab species (Monstera deliciosa or Crassula ovata per `_comment_coverage`) fails the global threshold by a margin a per-class override would close — **and only then** — seed exactly that species in `perSpeciesThresholds` with the value the probe justifies. If the probe shows the photo clears the global threshold cleanly, the map ships empty.
- [ ] **5.8** Run `./gradlew --no-daemon pixel6Api34DebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.darkfactory.plantpotting.identify.OnDeviceModelRealInterpreterTest`. Capture to `docs/sprints/results/PLANTPOTTING-0005-phase5-real-model.txt`.

### Phase 6 — Documentation: ROADMAP.md + ml-mapping-notes.md + README link

- [ ] **6.1** Write `docs/ROADMAP.md` — one page, four sections:
  1. **Current state (post-0005)** — three-sentence summary: on-device ML works; LowConfidencePicker + Failure polished; calibration scaffold in place; test infra clean.
  2. **Layer status** — table: rows KB / identifier / UI / tests / infra; columns Status (✓ shipped / △ partial / ✗ gap) and Notes.
  3. **Known gaps** — bullets seeded by §2.4 non-goals.
  4. **Candidate next sprint** — one paragraph proposing PLANTPOTTING-0006 (likely: confidence calibration v2 with multi-species fixture sweep + per-species threshold seeding). Do not pre-commit scope.
- [ ] **6.2 (Gemini addition)** Add a calibration-provenance entry to `docs/kb/ml-mapping-notes.md` — names the per-class threshold mechanism, summarises the probe outcome (one paragraph), and explains the §5.6 / §5.7 decisions. This is *why* the values landed where they did — survives sprint result rotation.
- [ ] **6.3** Update `README.md`'s "Current state" section to reflect post-0005 state. Replace the existing "Known gaps (carried to PLANTPOTTING-0005, see …)" bullet list with one line: "See [`docs/ROADMAP.md`](docs/ROADMAP.md) for layer status and the gap inventory." Add `PLANTPOTTING-0005 | Post-shutter polish + un-defer | done` to the status table only after the ledger flips at §7.7.

### Phase 7 — Final-verify, evidence, ledger close

- [ ] **7.1 (final-verify, JVM gates)** `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking`. Capture to `docs/sprints/results/PLANTPOTTING-0005-final-verify.txt`. **Required GREEN.**
- [ ] **7.2 (final-verify, instrumentation)** `./gradlew --no-daemon pixel6Api34DebugAndroidTest`. Capture to `docs/sprints/evidence/PLANTPOTTING-0005/gmd-output.txt`. **Required GREEN; @Ignore count == 0; expected pass count ≥ 12** (existing 10 + `LowConfidenceFlowTest` + `PermissionDeniedFlowTest.openSettings…` un-ignored; one or two extra from the §4.3 second path).
- [ ] **7.3 (final-verify, integration-flow)** `pwsh ./scripts/integration-flow.ps1` (cold), `pwsh ./scripts/integration-flow.ps1` (warm), `pwsh ./scripts/integration-flow.ps1 -BuildOnly`. Capture to `evidence/PLANTPOTTING-0005/transcript-A-cold.txt`, `transcript-B-warm.txt`, `transcript-C-buildonly.txt`. **All three required GREEN** (`Integration manifest diff passed.`).
- [ ] **7.4 (final-verify, stub isolation)** `bash scripts/check-stub-isolation.sh`. **Required GREEN.**
- [ ] **7.5 (audit)** Confirm: `grep -R "TestIdentifyModule" app/src/androidTest/` → 0 hits; `grep -R "@Ignore" app/src/androidTest/` → 0 hits; `grep -R "testTagsAsResourceId" app/src/main/` → exactly 1 hit at `MainActivity.kt`; **`grep -RE "println|@Ignore\\(\"probe" app/src/androidTest/` → 0 hits** (probe-code removal audit).
- [ ] **7.6 (Codex addition — manual emulator walkthrough)** On a booted emulator, walk: grant permission → camera → shutter → land on `LowConfidencePicker` (verify sub-headline + chevron chips + outlined archetype CTA + working search/empty-states) → pick a species → `ResultScreen` (source badge: `on-device match (low confidence)`) → `RecommendationScreen` (archetype name + recipe table summing to 100%) → retake. Then manually trigger a capture failure (e.g. block `ImageCapture` momentarily or use a forced-failure debug toggle if one exists; otherwise simulate by injecting `Failure` state via a debug-only path) and verify the polished bottom-anchored banner is legible and the `Try again` button restores `Idle`. Capture screenshots of the polished `LowConfidencePicker` and `Failure` banner to `evidence/PLANTPOTTING-0005/`.
- [ ] **7.7** Write `docs/sprints/results/PLANTPOTTING-0005.md`. Cover: baseline, per-phase diff summary, contract-lock RED→GREEN evidence, the seven-test migration list (per §0.4 reconciliation), §5.5 probe numbers, §5.6 accuracy-assertion decision, §5.7 seeding decision, final-verify gates, files added/modified/deleted, deferrals carried to PLANTPOTTING-0006.
- [ ] **7.8** Update `docs/sprints/ledger.yaml`: `PLANTPOTTING-0005` `status: done`, refresh `updated`.
- [ ] **7.9** Tick every `- [ ]` in this plan to `- [x]` as work lands (the executor should be doing this incrementally; final pass at close-out catches strays).
- [ ] **7.10** Record final commit hash(es) in the results doc.

---

## 4. Decisions (opinionated; alternatives documented)

### 4.1 `CameraUiState.Failure` UI — Banner-style Card vs Snackbar vs new screen

**Pick:** **Material3-style `Banner` built as `OutlinedCard`**, anchored bottom-center, padded above the shutter. Persistent (Snackbars time out); unmissable (top-center text isn't); no new navigation route.

**Rejected:** `Snackbar` (designed for transient confirmations — a capture failure is a state the user should read and act on without time pressure). New `CaptureFailedScreen` route (failure is recoverable in place; a new screen adds a back-stack hop). Bigger red `Text` (band-aid; no retry affordance). **Note:** Material3 Compose has no first-class `Banner` composable — build with `OutlinedCard` (Claude's catch; Codex and Gemini both glossed over this).

### 4.2 Test-infra refactor — `@BindValue` per test

**Pick:** **per-test `@BindValue`.** Canonical Hilt pattern for "this test wants its own fake". Global `@TestInstallIn` is for "every test in the module wants this fake by default" — the pattern we're moving away from. Compile-check after **each** test migration, not in batches — Hilt's generated-graph error blames the generated file, not the source line.

**Rejected:** Keep global swap + add per-test `@TestInstallIn(replaces = [TestIdentifyModule::class])` (adds another module per test that needs the real identifier; doesn't address the footgun). Postpone again (the carry-forward exists for a reason).

### 4.3 Per-class threshold mechanism shape — `Map<String, Float>` vs `Map<String, ThresholdRow>` vs separate file

**Pick:** **`Map<String, Float>`** keyed by `speciesId`, overriding `high_confidence_plain` only. Minimum surface. Margin overrides (`_margin_min`, `_margin_delta`) are deferred — the data to justify them doesn't exist yet.

**Rejected:** `Map<String, ThresholdRow>` mirroring the global block (premature surface). Separate `per_species_calibration.json` (a second file with its own validator + parser + contract test for no gain). Kotlin-side `object` constant (makes threshold a code change instead of a data change — manifest is the policy source-of-truth per 0004 §4.1).

### 4.4 Probe-then-assert for the accuracy assertion

**Pick:** **Probe first; record numbers; pick the pre-committed preferred-or-fallback form.** The probe converts a known-unknown into a known-known before the assertion lands. Pre-committing both forms stops an executor under deadline pressure from inventing a third weaker form.

**Rejected:** Commit the optimistic assertion + fix forward (wastes CI; pollutes git history). `assumeTrue`-style soft assertion (tests that pass when they shouldn't). Gemini's hardcoded `> 0.6` (the value is arbitrary without measurement).

### 4.5 `PermissionDeniedFlowTest` un-ignore — fake guard, not system dialog

**Pick:** Drive permanent-denial state via `FakeCameraPermissionGuard`. The original `@Ignore` reason cites that the system dialog is outside the Compose tree — true, but the test asserts *app behaviour* under a known permission state, not *system dialog behaviour*. The fake guard isolates the assertion to the surface that's worth testing.

**Rejected:** `uiautomator` taps on the system dialog (brittle across API levels, locales, OEM skins). Leave `@Ignore`'d (ignored since 0001 — either fix it or delete it; fixing recovers coverage of the open-settings intent firing).

### 4.6 Calibration seeding — empty by default, seed only with probe evidence

**Pick:** Ship `perSpeciesThresholds = emptyMap()` by default. Seed **exactly one** in-vocab species in the manifest **only if** §5.5 shows it fails the global threshold by a margin an override would close. Otherwise the map ships empty and PLANTPOTTING-0006 owns the multi-species calibration sweep.

This threads the needle between Claude's "no seeding" rule (which weakens the "begin calibration" goal) and Gemini's "seed all in-vocab species" (which is the threshold-sprawl risk in miniature). It also preserves the falsifiability bar: an in-manifest override is only present if the results doc shows the probe evidence for it.

### 4.7 Seam invariants (carried forward; non-negotiable)

- `interface PlantIdentifier { suspend fun identify(jpeg: ByteArray): IdentificationResult }` — byte-for-byte unchanged.
- `IdentificationResult` field list: `speciesId, displayName, source, lowConfidence`. Unchanged.
- `StubPlantIdentifier.kt` stays under `app/src/main/java/com/darkfactory/plantpotting/identify/`; `check-stub-isolation.sh` stays GREEN.
- `verifyNoNetworking` stays GREEN.
- `expected-artifacts/PLANTPOTTING-0001*.txt` unchanged (no re-baselining).
- All existing test tags preserved (additive only).

---

## 5. Sequencing

```
0  (setup, contract locks RED, baseline, inventory)
  ↓
1  (LowConfidencePicker polish) ──┐
2  (Failure banner polish) ───────┤  Phases 1 & 2 are parallel-safe
                                  │  (disjoint files; both UI-only)
3  (TestIdentifyModule removal) ──┘  must precede Phase 4
  ↓
4  (LowConfidenceFlowTest + PermissionDeniedFlowTest un-ignore)
  ↓
5  (calibration scaffold; §5.5 probe gates §5.6 + §5.7)
  ↓
6  (ROADMAP + ml-mapping-notes + README link)
  ↓
7  (final-verify + manual walkthrough + results + ledger close)
```

Key dependencies:
- **Phase 0 must complete before any other phase.** Baseline + contract-lock RED is the falsifiability foundation.
- **Phase 3 must precede Phase 4.** `LowConfidenceFlowTest` is written with `@BindValue` from day one — easier than writing against the old global swap then migrating.
- **§5.4 → §5.5 → §5.6 → §5.7** is strictly serial. The probe is the input to the assertion design, which is the input to the seeding decision.
- **Phase 6 (docs)** can begin once §5 facts are known — runs in parallel with §7.1–§7.5 if convenient.
- **Phase 7 must be last.** One final-verify pass; intermediate per-phase verifies (§1.10, §2.7, §3.12, §4.7, §5.8) catch regressions early without burning CI on redundant chains.

**Soft ramp note (Claude's preference):** Phases 1 and 2 (UI polish — lower-risk, smaller surface) can land first to warm up the executor before the higher-risk Phase 3 `@BindValue` migration. Codex prefers Phase 3 first to unblock §4.1's `@BindValue`. Either order works since the dependency is only Phase 3 → Phase 4. Default: Phase 1 + 2 first, then 3.

---

## 6. Risks and mitigations

- **6.1 `@BindValue` + `@JvmField` Kotlin trap.** Visibility/placement mistakes produce a Hilt-generated-graph compile error that blames the generated file. **Mitigation:** §3.2–§3.8 compile-check after **each** test, not after the batch. **Fallback:** per-class `@TestInstallIn(replaces = [OnDeviceIdentifyModule::class])` scoped to one test (no global swap; no other test affected). Documented in results doc.

- **6.2 Real Monstera photo still routes low-confidence.** Likelihood low for an asset-loaded photo of an in-vocab species (`_comment_coverage` confirms Monstera is mapped) but not zero — the fixture is JPEG-decoded, not real camera. **Mitigation:** §5.5 probe captures the truth; §5.6 fallback assertion preserves an honest accuracy bar.

- **6.3 `FakeCameraPermissionGuard` doesn't expose enough state for permanent-denial.** **Mitigation:** §4.4 extends minimally — one boolean flag. **Fallback:** if extension is more invasive than expected, delete the `@Ignore`d test rather than re-ignoring — losing instrumentation coverage of an intent fire is acceptable; carrying an `@Ignore`'d test for another sprint is not.

- **6.4 `LowConfidencePicker` empty-state copy is too prominent.** **Mitigation:** copy intentionally short and informational, not alarming. **Fallback:** demote the info card to a one-line `Text` in PLANTPOTTING-0006; the test tag preserves the regression catch.

- **6.5 Banner / FAB tap-target collision.** **Mitigation:** §2.4 `padding(bottom = 144.dp)` keeps the banner above the shutter; §2.1 verifies retry click works; §2.5 verifies shutter remains enabled in `Failure`. **Fallback:** swap `OutlinedCard` for `Snackbar` (the rejected option in §4.1) — test tags survive the swap.

- **6.6 Probe-run code accidentally ships.** **Mitigation:** §5.5 calls out probe removal; §7.5 audit greps `println` and `@Ignore("probe` in androidTest. **Fallback:** the results doc captures the probe data — code can be removed in a follow-up if it sneaks past §7.5.

- **6.7 Threshold sprawl (Gemini's framing).** **Mitigation:** §4.6 limits seeding to at most one in-vocab species this sprint, gated on §5.5 evidence. The override map is optional in the manifest.

- **6.8 `per_species_thresholds` map accidentally seeded.** **Mitigation:** §5.2 explicitly leaves the map empty unless §5.7 fires; §7.7 results doc records final seeded contents. Reviewer can catch a stray entry against §5.7's narrow gate.

- **6.9 `integration-flow.ps1` resource-id drift from UI polish.** **Mitigation:** new tags are additive only; existing tags (`LowConfidencePickerTags.candidateTag`, `speciesTag`, `SEARCH`, `SPECIES_LIST`, `PICK_BY_ARCHETYPE`, `CameraScreenTags.ERROR`) preserved. **Fallback:** if a tag must change, update `expected-artifacts/PLANTPOTTING-0001*.txt` in the same change and call it out in the results doc; this would invalidate the §2.4 non-goal and require explicit review.

- **6.10 Intents-test leakage on GMD.** `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` could launch the system Settings activity if `Intents.intercept` isn't initialised before the click. **Mitigation:** `Intents.init()` in `@Before`; `Intents.release()` in `@After`; assertion via `Intents.intended(...)`. **Fallback:** wrap the test body in `IntentsTestRule`.

- **6.11 Real photo licence incompatibility.** **Mitigation:** §5.4 requires Wikimedia Commons CC-BY-SA / CC-BY / CC0; verify licence terms before commit. **Fallback:** any other CC-licensed source meeting the same redistribution terms.

- **6.12 LowConfidenceFlowTest covers the empty-candidate path but misses the manual-candidate path.** **Mitigation:** §4.3 adds the second test method (search → species-row tap) so both real-world paths are covered.

- **6.13 ROADMAP.md goes stale fast.** **Mitigation:** keep it short and current-state oriented; link to ledger + results docs rather than duplicate history. PLANTPOTTING-0006 plan should refresh it.

---

## 7. Acceptance criteria

The sprint is `done` when every one of these holds:

- [ ] §3 task list — all `- [ ]` boxes ticked.
- [ ] `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` GREEN (`docs/sprints/results/PLANTPOTTING-0005-final-verify.txt`).
- [ ] `./gradlew --no-daemon pixel6Api34DebugAndroidTest` GREEN with zero `@Ignore`d tests in `app/src/androidTest/` (`evidence/PLANTPOTTING-0005/gmd-output.txt`).
- [ ] `bash scripts/check-stub-isolation.sh` GREEN.
- [ ] `pwsh ./scripts/integration-flow.ps1` (cold + warm) and `-BuildOnly` all emit `Integration manifest diff passed.` (`evidence/PLANTPOTTING-0005/transcript-A/B/C-*.txt`).
- [ ] §7.5 audit greps: `TestIdentifyModule` → 0 hits in androidTest; `@Ignore` → 0 hits in androidTest; `testTagsAsResourceId` → exactly 1 hit at `MainActivity.kt`; `println` / `@Ignore("probe` → 0 hits in androidTest.
- [ ] §0.6, §0.7, §0.8 contract-lock tests RED on baseline, GREEN at close (evidence in results doc).
- [ ] §3.10 production-shape regression guard added and GREEN.
- [ ] `OnDeviceModelRealInterpreterTest` asserts an accuracy-bearing claim (preferred or fallback form per §5.6) against a real CC-licensed Monstera photo.
- [ ] `app/src/androidTest/assets/identify-fixtures/LICENSE.txt` carries source URL + author (if available) + licence name + retrieval date for the real fixture.
- [ ] `docs/ROADMAP.md` exists (one page, four sections).
- [ ] `docs/kb/ml-mapping-notes.md` carries the calibration provenance entry.
- [ ] `README.md` links `docs/ROADMAP.md` and reflects post-0005 state.
- [ ] `docs/sprints/results/PLANTPOTTING-0005.md` covers baseline, per-phase diffs, contract-lock evidence, probe numbers, accuracy-assertion decision, seeding decision (§5.7), final-verify gates, files-added/modified/deleted, deferrals to 0006.
- [ ] `docs/sprints/ledger.yaml`: `PLANTPOTTING-0005` `status: done`, `executor` stamped.
- [ ] §7.6 manual emulator walkthrough completed; screenshots captured.
- [ ] **No new** `@Ignore`s, dependencies, model swaps, KB edits, interface changes, or `expected-artifacts` re-baselining.

---

## 8. Sprint window estimate

| Phase | Estimated effort (single AI implementer) |
| --- | --- |
| Phase 0 (setup, contract locks, baseline, inventory) | 0.5 day |
| Phase 1 (LowConfidencePicker polish) | 1.0 day |
| Phase 2 (Failure banner polish) | 0.5 day |
| Phase 3 (TestIdentifyModule removal + 7-test migration + regression guard) | 1.0 day |
| Phase 4 (LowConfidenceFlowTest + PermissionDeniedFlowTest un-ignore) | 0.5 day |
| Phase 5 (calibration scaffold + real photo + probe + assertion + seeding decision) | 1.5 days |
| Phase 6 (ROADMAP + ml-mapping-notes + README) | 0.25 day |
| Phase 7 (final-verify + manual walkthrough + results + ledger) | 0.75 day |
| **Total** | **~6 days** |

Within the 5–7 day envelope. §6.1 (`@BindValue` finicky), §6.2 (probe outcome), and §6.3 (`FakeCameraPermissionGuard` state extent) are the three most likely slip sources; all have documented fallbacks that keep the sprint in scope.
