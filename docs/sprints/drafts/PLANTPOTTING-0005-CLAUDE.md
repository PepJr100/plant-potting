# PLANTPOTTING-0005 — Post-shutter polish + un-defer carry-forward from 0003/0004

**Status:** planned
**Sprint shape:** polish + un-defer (broader than the 0002 / 0004 fix sprints; narrower than 0001 / 0003 feature sprints). Roughly the size of two stacked fix sprints in scope, with one new test-infra refactor and one new calibration scaffold.
**Sprint window:** ~5–7 days for a single AI implementer (opus / gpt-5.4 / gemini — picked at `sprint-execute` time).
**Primary source:** the carry-forward bullets in `docs/sprints/results/PLANTPOTTING-0004.md` §5 and the "Notes for next sprint planner" in `docs/sprints/feedback/PLANTPOTTING-0004/feedback.md`.
**Predecessor:** PLANTPOTTING-0004 (UINT8 dtype + testTagsAsResourceId bridge, both fixed; review clean).

---

## 1. Intent

PLANTPOTTING-0003 shipped the on-device ML identifier; PLANTPOTTING-0004 fixed the two bugs that landed with it (UINT8 dtype, testTagsAsResourceId). The app now identifies plants on-device end-to-end. But because the AIY V1/3 model covers only 2 of the 16 KB species verbatim (per `model_manifest.json`'s `_comment_coverage`), the **headline post-shutter screen on most captures is `LowConfidencePicker`, not `ResultScreen`** — and that screen is skeletal next to the polished surfaces around it. This sprint:

1. **Polishes `LowConfidencePicker` as the primary post-shutter result surface.** It is currently three text widgets stacked vertically with no empty state, no "didn't find your plant" affordance, no model-rationale context for the candidates, and no source-of-uncertainty cue. Treat it as a first-class screen on par with `ResultScreen` / `RecommendationScreen`.

2. **Polishes `CameraUiState.Failure`.** Today a single white `Text` line at `Alignment.TopCenter` (`CameraScreen.kt:140-149`). Functional but easy to miss. Replace with a Material3 banner (or Snackbar) anchored at the bottom, dismissable, with a "Try again" affordance that resets state to `Idle`. No new screen.

3. **Lands the deferred test-infra refactor.** Delete the global `TestIdentifyModule` `@TestInstallIn(replaces = [OnDeviceIdentifyModule::class])` swap; migrate the six instrumentation tests that depend on it to per-test `@BindValue` fakes. This is the architecturally cleaner shape PLANTPOTTING-0004 §4.5 deferred verbatim.

4. **Adds the missing instrumentation tests.** `LowConfidenceFlowTest` (B3 from 0003, never landed); re-enable `PermissionDeniedFlowTest.openSettingsIntentFiresOnPermanentDenial` which has been `@Ignore`'d since PLANTPOTTING-0001.

5. **Begins confidence calibration.** Not a full retune — a scaffold:
   - Replace the procedurally-generated synthetic Monstera fixture (`app/src/androidTest/assets/identify-fixtures/monstera-deliciosa.jpg`, deterministic seed `0x4D4F4E54`) with a real CC-licensed Monstera deliciosa photograph.
   - Add one accuracy-bearing assertion to `OnDeviceModelRealInterpreterTest` against the real photo (e.g. top-1 score for `monstera-deliciosa` ≥ `high_confidence_plain` threshold, OR top-3 contains `monstera-deliciosa`). The exact assertion is picked at §5.5 task time after a one-off probe run captures the observed top-k.
   - Define (in code + manifest) the **per-class threshold override mechanism** so future calibration can tune specific species without re-baselining the global threshold. Implementation: an optional `per_species_thresholds: { species_id: float }` map in `model_manifest.json`, consumed by `ModelScoreMapper` as a fallback that overrides `high_confidence_plain` per species. **No actual per-species overrides are written this sprint** — the mechanism lands, the calibration data does not (that's PLANTPOTTING-0006+).

6. **Adds `docs/ROADMAP.md`.** The `sprint-review` skill expects it. Currently absent — flagged in feedback §"Notes for next sprint planner". One page: current state, layer status (KB, identifier, UI, tests, infra), known gaps, candidate next sprint.

---

## 2. Goals and non-goals

### 2.1 The observable success bars

On a connected `Pixel_6_API_34` emulator (AOSP virtual scene) plus JVM/CI:

1. **`LowConfidencePicker` looks finished.** A user landing on it sees: a clear headline (still "We couldn't identify your plant confidently."), a one-line subtitle that names the model coverage limit in human language ("This model recognises a limited plant vocabulary — please confirm or pick below"), an evidence area (the existing up-to-3 candidate chips with probabilities, **plus** a "no candidates mapped" empty state when `topCandidates` is empty — today the screen silently omits the chip row), the existing search field, the full-list `LazyColumn` with a sticky-header alphabetisation and a "no results" empty state for unmatched searches, and an outlined-style `Pick by archetype` button at the bottom that visually de-emphasises (not promotes) the fallback path. Snapshot: a new Robolectric Compose test asserts the empty-state branches render under both candidate-present and candidate-absent inputs.

2. **`CameraUiState.Failure` is unmissable.** The user sees a bottom-anchored Material3 banner (not a fragile top-center `Text`), with the failure reason and a `Try again` button that calls `viewModel.reset()`. A JVM Compose test asserts the banner is displayed when state is `Failure`, hidden otherwise. The shutter remains the existing "tap to retry" path; the new affordance is an additional, more discoverable retry vector.

3. **`TestIdentifyModule` is deleted.** `grep -R "TestIdentifyModule\|@TestInstallIn(.*OnDeviceIdentifyModule" app/src/androidTest/` returns zero hits. Each of the six instrumentation tests that used to rely on the global swap now installs its own `@BindValue` fake or a per-class `@TestInstallIn` module scoped to that test. `pixel6Api34DebugAndroidTest` green on a cold GMD run.

4. **Tests added or un-ignored:**
   - `LowConfidenceFlowTest` (new instrumentation): captures the full `Camera → LowConfidencePicker → ResultScreen (lowConfidence=true) → RecommendationScreen` flow with a fake identifier that emits `lowConfidence = true`.
   - `PermissionDeniedFlowTest.openSettingsIntentFiresOnPermanentDenial`: `@Ignore` removed; test passes on GMD. Approach: drive the permanent-denial state via `FakeCameraPermissionGuard` (which already exists per `app/src/androidTest/java/com/darkfactory/plantpotting/permission/FakeCameraPermissionGuard.kt`), bypassing the system permission dialog that the original `@Ignore` reason cites.
   - Two new Robolectric Compose tests for `LowConfidencePicker` empty states.
   - One new Robolectric Compose test for the `CameraUiState.Failure` banner.

5. **Real Monstera fixture in place.** `app/src/androidTest/assets/identify-fixtures/monstera-deliciosa.jpg` is a real CC-licensed Monstera deliciosa photograph; `LICENSE.txt` updated with attribution and licence URL. `OnDeviceModelRealInterpreterTest` asserts `result.source == IdSource.ON_DEVICE_MODEL` **and** one accuracy-bearing claim (chosen after the probe — likely `result.lowConfidence == false` since the real photo should clear the threshold; falls back to `top3.contains(monstera-deliciosa)` if probe shows it routes low-conf).

6. **Per-class threshold mechanism wired.** `ModelManifest.perSpeciesThresholds: Map<String, Float>` (defaults to empty). `ModelScoreMapper.isHighConfidence(speciesId, score)` consults the override first, falls back to `thresholds.high_confidence_plain`. JVM unit test covers both branches. **No production values seeded** — the map is empty in the shipped manifest.

7. **`docs/ROADMAP.md` exists.** One-page snapshot, linked from `README.md`'s post-sprint state section.

### 2.2 Falsifiability — how we know we hit it

- `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` green.
- `./gradlew --no-daemon pixel6Api34DebugAndroidTest` green: 11 → ~13 tests passing (existing 10 + `LowConfidenceFlowTest` + `PermissionDeniedFlowTest.openSettingsIntentFiresOnPermanentDenial` un-ignored; the `@Ignore`d count drops from 1 to 0).
- `bash scripts/check-stub-isolation.sh` green throughout.
- `pwsh ./scripts/integration-flow.ps1` (cold + warm) green: `Integration manifest diff passed.` against the existing `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` (no changes to expected artefact — the low-conf path the script already exercises is unchanged at the manifest level; UI polish doesn't alter the captured manifest fields).
- `grep -R "TestIdentifyModule" app/src/androidTest/` → zero hits (file deleted).
- `grep -R "@Ignore" app/src/androidTest/` → zero hits (the only currently `@Ignore`d test is un-ignored this sprint; no new `@Ignore`s allowed).
- A probe run against the real Monstera photo (recorded in the results doc) demonstrates the model can clear or near-clear the `high_confidence_plain = 0.55` threshold on real photographic input — concrete evidence that the calibration scaffolding is grounded in reality and the synthetic fixture's low-conf routing was a fixture artefact, not a model problem.

### 2.3 Non-goals (explicit deferrals to PLANTPOTTING-0006+)

- **Model swap.** AIY V1/3 stays. No model file replacement, no INT8→FP16 swap.
- **INT8 / GPU / NNAPI delegate.** TFLite CPU stays.
- **Net-new screens.** No `CaptureFailedScreen`, no `ModelInfoScreen`, no `AboutScreen`. `LowConfidencePicker` and `CameraScreen` are polished in-place.
- **ML training / retraining.** Out of scope.
- **Seeding per-species threshold values.** The map is wired but empty. Producing actual calibrated thresholds requires a labelled dataset run we don't have.
- **Multi-photo Monstera fixture / multi-species fixture sweep.** One real photo is the start; a fixture sweep is its own sprint.
- **`PlantIdentifier` / `IdentificationResult` interface changes.** Seam invariants from PLANTPOTTING-0003 §4.4 still hold.
- **KB edits** (`species.json`, `archetypes.json`). Locked.
- **AGP / Kotlin / Compose / Hilt / TFLite version bumps.** Locked.
- **README rewrite.** Append a "Current state (post 0005)" section and link `ROADMAP.md`; no other prose churn.

---

## 3. Scope boundaries

### 3.1 Must-land

**LowConfidencePicker polish (`app/src/main/java/com/darkfactory/plantpotting/result/LowConfidencePickerScreen.kt`):**

- [ ] Sub-headline body row added under the existing headline, sourcing copy from `strings.xml` (new `R.string.low_conf_subtitle`), wired through a new test tag `LowConfidencePickerTags.SUBTITLE`.
- [ ] Empty-state Composable for "no mapped candidates" — when `viewModel.topCandidates.isEmpty()`, the screen shows an outlined info card with copy "No close matches — pick from the full list below." carrying tag `LowConfidencePickerTags.NO_CANDIDATES_EMPTY`. Today the chip row is silently omitted; an empty state is more honest about what happened.
- [ ] Empty-state Composable for "search returned no results" — when `query.isNotBlank() && filtered.isEmpty()`, the `LazyColumn` is replaced with a `Text` "No species match \"$query\"" with tag `LowConfidencePickerTags.SEARCH_EMPTY`.
- [ ] Replace `Button` for "I don't know — pick by archetype" with `OutlinedButton` (visual de-emphasis); copy unchanged; tag unchanged.
- [ ] Candidate `AssistChip` rows show a `trailingIcon` (Material3 `Icons.Outlined.ChevronRight`) to make tappability obvious. No layout change.

**CameraUiState.Failure polish (`app/src/main/java/com/darkfactory/plantpotting/camera/CameraScreen.kt`):**

- [ ] Replace the bare top-center `Text` (current `CameraScreen.kt:140-149`) with a Material3 `Banner`-style composable (an outlined `Card` with leading `Icons.Outlined.ErrorOutline`, body text from `state.reason`, trailing `TextButton("Try again")` calling `viewModel.reset()`), anchored at `Alignment.BottomCenter` above the shutter.
- [ ] New test tag `CameraScreenTags.FAILURE_BANNER` on the banner root; `CameraScreenTags.FAILURE_RETRY` on the retry button. Keep `CameraScreenTags.ERROR` as an alias on the body Text inside the banner for back-compat with any test that already grepped for it.
- [ ] Banner only visible when `state is CameraUiState.Failure`; hidden otherwise (the existing predicate is correct — refactor, don't expand).

**Test-infra refactor (`app/src/androidTest/...`):**

- [ ] Delete `app/src/androidTest/java/com/darkfactory/plantpotting/identify/TestIdentifyModule.kt`.
- [ ] Migrate `EndToEndFlowTest`, `CameraPreviewLayoutTest`, `CameraScreenSmokeTest`, `CameraScreenBindStateTest`, `PermissionResumeRecoveryTest`, `PermissionDeniedFlowTest` to use a per-test `@BindValue lateinit var fakeIdentifier: PlantIdentifier` initialised in `@Before` via `FakeFixedIdentifier(...)`. (These are the six tests `results/PLANTPOTTING-0004.md` §5 identifies as the migration set; verify the list at §0.4 by grepping androidTest for tests that compile against the global swap.)
- [ ] If a test only needs the default Monstera-deliciosa stub behaviour and never references the identifier directly, a `@BindValue` of `FakeFixedIdentifier()` with default-arg constructor suffices — no test-method changes.
- [ ] `OnDeviceModelRealInterpreterTest` does NOT migrate to `@BindValue` — it still injects the concrete `OnDevicePlantIdentifier` class (per 0004 §4.5). Confirm the test still compiles and passes after the global swap is deleted.
- [ ] `HiltTestRunner.kt` unchanged — it's the runner, not the binding.

**LowConfidenceFlowTest (new):**

- [ ] Add `app/src/androidTest/java/com/darkfactory/plantpotting/LowConfidenceFlowTest.kt`. `@HiltAndroidTest`, drives `MainActivity`. Uses `@BindValue` to install a `FakeFixedIdentifier` constructed with `lowConfidence = true` (extend `FakeFixedIdentifier` to expose a constructor flag if it doesn't already). Asserts:
  - After granting `CAMERA` and clicking the shutter, the `LowConfidencePicker` screen surfaces (`onNodeWithTag(LowConfidencePickerTags.HEADLINE).assertIsDisplayed()`).
  - Clicking a species row (`LowConfidencePickerTags.speciesTag("monstera-deliciosa")`) navigates to `ResultScreen` with `lowConfidence = true`.
  - The result-screen source badge reads `on-device match (low confidence)` (or whatever the current test-style assertion uses for source labelling — check `EndToEndFlowTest` for the established pattern).
  - Tapping `See potting mix` reaches `RecommendationScreen`.

**PermissionDeniedFlowTest un-ignore:**

- [ ] Remove `@Ignore` from `openSettingsIntentFiresOnPermanentDenial`.
- [ ] Replace the original "click GRANT_BUTTON → system dialog" path (which the `@Ignore` reason calls out as unscriptable) with: drive the permanent-denial state via `FakeCameraPermissionGuard` (already exists per `app/src/androidTest/.../permission/FakeCameraPermissionGuard.kt`); assert the screen reflects the permanently-denied surface; click the "Open Settings" button (find its test tag in `PermissionScreenTags` — confirm at §0.5 task time); assert the `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` intent fires via `Intents.intended(...)`.
- [ ] If the fake doesn't currently expose a "permanently denied" mode, extend it. Keep the extension minimal — one boolean flag or one method.

**Confidence calibration scaffold:**

- [ ] **(test, RED first)** Add `ModelScoreMapperPerSpeciesThresholdTest` (JVM) — asserts `ModelScoreMapper.isHighConfidence("monstera-deliciosa", 0.40f)` returns `true` when the manifest declares `per_species_thresholds = { "monstera-deliciosa": 0.35 }`, and returns `false` when the override is absent (falls back to `high_confidence_plain = 0.55`). RED today — field doesn't exist.
- [ ] Add `per_species_thresholds: Map<String, Float>` (optional, defaults to empty map) to `ModelManifest`. Parse it in `ModelManifestReader`. **Do not seed values in `model_manifest.json`** — the field is optional and absent from the shipped manifest this sprint.
- [ ] Plumb the override through `ModelScoreMapper`: in whatever method decides `lowConfidence` for a given top-1, consult `manifest.perSpeciesThresholds[speciesId]` before `manifest.thresholds.high_confidence_plain`.
- [ ] Swap the procedurally-generated `monstera-deliciosa.jpg` fixture for a real CC-licensed photograph. Source: an explicitly CC-BY or CC0 Monstera deliciosa photo (Wikimedia Commons or similar — confirm licence terms before committing). Update `app/src/androidTest/assets/identify-fixtures/LICENSE.txt` with attribution, licence name, and source URL. File size budget: <=200 KB after JPEG-quality 80 resize to 480×480.
- [ ] **Probe run** (one-off, NOT a checked-in test): with the real photo in place, run `OnDeviceModelRealInterpreterTest` with a temporary `println(result)` (or a one-off logging branch behind a `BuildConfig.DEBUG` check — remove before commit) and capture the observed top-1 species, top-1 score, top-3, and `result.lowConfidence`. Record the captured numbers in `docs/sprints/results/PLANTPOTTING-0005.md`. **This is research, not a test gate.**
- [ ] **(test, after probe)** Add one accuracy-bearing assertion to `OnDeviceModelRealInterpreterTest` informed by the probe. Preferred: `assertThat(result.speciesId).isEqualTo("monstera-deliciosa")` AND `assertThat(result.lowConfidence).isFalse()`. Fallback if the probe shows the photo still routes low-conf: assert `result.source == IdSource.ON_DEVICE_MODEL` AND (`result.speciesId == "monstera-deliciosa"` OR `most-recent-candidates` contains `monstera-deliciosa`). The fallback is honest evidence the model can map this species; the preferred assertion is honest evidence it can map it confidently. Pick after the probe; do not pre-commit to either.

**ROADMAP.md:**

- [ ] Add `docs/ROADMAP.md`. One page, four sections:
  1. **Current state (as of post-0005)** — three-sentence summary (on-device ML works; KB validated; LowConfidencePicker / Failure polished).
  2. **Layer status** — a table with rows for KB / identifier / UI / tests / infra, columns Status (✓ shipped / △ partial / ✗ gap) and Notes.
  3. **Known gaps** — bullet list, with the deferrals from §2.3 above seeding it.
  4. **Candidate next sprint** — one-paragraph proposal for PLANTPOTTING-0006 (likely: confidence calibration with a multi-species fixture sweep + per-species threshold seeding).
- [ ] Link `ROADMAP.md` from `README.md`'s "Current state (post-0005)" section, replacing the existing "Known gaps (carried to PLANTPOTTING-0005)" bullet list with "See [`docs/ROADMAP.md`](docs/ROADMAP.md) for layer status and the gap inventory."

**Final-verify and ledger close:**

- [ ] All §0 contract-lock tests green.
- [ ] Full pre-PR check chain green: `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking pixel6Api34DebugAndroidTest` + `bash scripts/check-stub-isolation.sh` + `pwsh ./scripts/integration-flow.ps1` (cold + warm) + `-BuildOnly`.
- [ ] Results doc `docs/sprints/results/PLANTPOTTING-0005.md` written, evidence dir `docs/sprints/evidence/PLANTPOTTING-0005/` populated (transcripts + GMD output + probe-run record).
- [ ] Ledger `docs/sprints/ledger.yaml`: `PLANTPOTTING-0005` flipped to `status: done`, `executor` stamped, `updated` refreshed.

### 3.2 Nice-to-have (do **NOT** slip §3.1 for these)

- [ ] An `OutlinedButton` "Try a different photo" affordance on `LowConfidencePicker` that navigates back to `CameraScreen` (alongside the existing chips + search + archetype paths). Discoverable retry without forcing the user to interpret the search box as "I should give up".
- [ ] A Robolectric snapshot test for the new `CameraUiState.Failure` banner (composition only, no device).
- [ ] Extend `ROADMAP.md` with a fifth section "Architecture sketch" reproducing the diagram from `README.md`.
- [ ] Per-class threshold documentation in `model_manifest.json` via a `_comment_per_species_thresholds` breadcrumb explaining the override semantics, even though no values are seeded.
- [ ] A second real-photo fixture (different species — Crassula ovata is the other in-vocabulary species) added under `identify-fixtures/`, used in a second pass of `OnDeviceModelRealInterpreterTest` parameterised over two fixtures.

### 3.3 Out-of-scope (explicit deferrals)

- Everything in §2.3.
- New `IdSource` values or new source badges.
- Live-camera labels, AR overlays, multi-plant detection.
- Persisting capture history / favourites.
- A `Settings` screen.
- Reworking `OnDeviceIdentifyModule`'s `@Module` split — the existing split is not blocking the `@BindValue` migration.
- Re-baselining `expected-artifacts/PLANTPOTTING-0001.txt`. The UI polish does not alter the captured manifest fields (`source-badge`, `archetype-name`, `recipe-row-count`, `model-asset-present`).

---

## 4. Decisions (opinionated; alternatives documented)

### 4.1 `LowConfidencePicker` polish — banner vs sub-headline vs full rebuild

**Pick:** **sub-headline + empty states + visual de-emphasis of the archetype fallback.** Minimum surface area to make the screen feel like a destination instead of a fallback. The screen's three regions (top candidates, search, archetype fallback) are the right information architecture today — they just lack empty states and copy that explains the situation.

**Rejected — full rebuild with a `Material3 ModalBottomSheet`:** the screen is fine as a full-screen destination; a bottom sheet would re-introduce the "fallback" framing this sprint is explicitly arguing against.

**Rejected — collapse top-candidates into the search list with rank badges:** loses the visual hierarchy that distinguishes "the model's best guesses" from "the full vocabulary". The hierarchy is honest about the data.

### 4.2 `CameraUiState.Failure` UI — Banner vs Snackbar vs CaptureFailedScreen

**Pick:** **Material3 `Banner`-style `Card` anchored bottom-center.** Persistent (Snackbars time out), unmissable (top-center text isn't), no new navigation (a dedicated `CaptureFailedScreen` would force a back-stack hop for what is recoverable in-place).

**Rejected — `Snackbar` via `SnackbarHostState`:** Snackbars are designed for transient confirmations; a capture failure is a state the user should be able to read and act on without time pressure. Also adds `SnackbarHost` plumbing to `CameraScreen` that doesn't otherwise exist.

**Rejected — new `CaptureFailedScreen` route:** the failure is recoverable in place via `viewModel.reset()`; a new screen adds a back-stack hop and a navhost route for no behavioural gain.

**Rejected — keep the bare `Text` but make it red and bigger:** band-aid; doesn't add the retry affordance, doesn't fix discoverability for users on light backgrounds.

### 4.3 Test-infra refactor — `@BindValue` per test vs `@TestInstallIn` per test vs keep global

**Pick:** **`@BindValue` per test.** Per Hilt docs, `@BindValue` is the canonical pattern for "this single test wants its own fake". The global `@TestInstallIn` swap is the right pattern for "every test in the module wants this fake by default" — which is precisely what we're moving *away* from. Per-test `@BindValue` makes each test's fake declaration local and grep-discoverable.

**Rejected — keep the global swap and add per-test `@TestInstallIn(replaces = [TestIdentifyModule::class])`:** adds another module per test that needs a different fake; doesn't reduce surface; doesn't address the original critique (a future test author adding a test that needs the *real* identifier gets the fake silently).

**Rejected — keep the global swap as-is:** the carry-forward bullet exists for a reason. Postponing again is sunk cost.

**Migration ergonomics:** each `@BindValue` site is ~3 lines (`@BindValue @JvmField var fakeIdentifier: PlantIdentifier = FakeFixedIdentifier()`). `@JvmField` is required because Hilt's processor looks for fields, not properties — easy to get wrong; verify each migration with a `compileDebugAndroidTestKotlin` before moving to the next test.

### 4.4 Real Monstera fixture — Wikimedia Commons vs Flickr CC vs in-app capture

**Pick:** **Wikimedia Commons CC-BY-SA or CC-BY photograph** of *Monstera deliciosa*. Wikimedia's licence metadata is structured and auditable; Flickr's CC licences exist but are harder to verify hadn't been retroactively changed; in-app capture by the developer adds copyright provenance the project doesn't want to track. The licence file at `app/src/androidTest/assets/identify-fixtures/LICENSE.txt` already exists from 0004 — extend it.

**Rejected — public-domain photo with no attribution:** even CC0 / public-domain photos benefit from a source URL for audit; cost is zero.

**Rejected — buy a stock photo:** unnecessary; CC-licensed photographs of common houseplants are abundant.

**Resize policy:** 480×480 JPEG quality 80 to match the existing fixture's dimensions and keep the test-asset budget bounded. The model resizes to 224×224 internally — the fixture only needs to be plausibly a Monstera photo, not a 4K source.

### 4.5 Per-class threshold mechanism shape — Map<String, Float> vs Map<String, ThresholdRow> vs ML config file

**Pick:** **`Map<String, Float>`**, `species_id → high_confidence_threshold` override. Minimum surface; matches the single threshold we override (`high_confidence_plain`). Higher-arity overrides (margin-min, margin-delta) can be added if calibration shows the simple one doesn't cover the failure modes — but the data to justify that doesn't exist yet.

**Rejected — `Map<String, ThresholdRow>` where `ThresholdRow` mirrors the global `thresholds` block:** premature surface. We don't know yet whether per-species margin overrides will matter.

**Rejected — separate `per_species_calibration.json` asset file:** a second file with its own validator, parser, contract test. The override map fits inside `model_manifest.json` cleanly. If the map grows past ~50 entries, revisit.

**Rejected — Kotlin-side `object` constant**: makes the threshold a code change instead of a data change; the manifest is the policy source-of-truth per 0004 §4.1.

### 4.6 `PermissionDeniedFlowTest` un-ignore — fake guard vs uiautomator-driven dialog

**Pick:** **drive the denial state via `FakeCameraPermissionGuard`**, not via the real system dialog. The original `@Ignore` reason calls out that the system dialog "is outside the Compose tree" and can't be scripted — true, but the test is asserting *app behaviour* under a known permission state, not *system dialog behaviour*. The fake guard isolates the assertion to the surface that's worth testing.

**Rejected — `uiautomator` taps on the system dialog:** brittle across API levels, locales, and OEM skins (even AOSP changes the dialog wording per release).

**Rejected — leave `@Ignore` in place:** the test has been ignored since 0001; either fix it or delete it. Deleting loses coverage of the "Open Settings" intent firing on permanent denial. Fixing via the fake recovers that coverage at the cost of one boolean on the fake.

### 4.7 Probe run for the calibration assertion — pre-commit vs post-commit

**Pick:** **pre-commit, captured in the results doc, used to inform the assertion.** Committing an assertion that happens to fail because the model didn't behave as expected wastes a sprint cycle on a known-unknown. The probe converts a known-unknown into a known-known before §5.5 commits an assertion.

**Rejected — commit the optimistic assertion, fix forward if it fails:** wastes CI time; pollutes git history with a follow-up commit that has no engineering content.

**Rejected — make the assertion `assumeTrue`-style soft:** soft assertions are tests that pass when they shouldn't. The §5.5 fallback path (top-3 contains, not top-1 equals) is already the honest weak assertion.

### 4.8 Seam invariants (carried; non-negotiable)

- `interface PlantIdentifier { suspend fun identify(jpeg: ByteArray): IdentificationResult }` — byte-for-byte unchanged.
- `IdentificationResult` field list unchanged: `speciesId, displayName, source, lowConfidence`.
- `StubPlantIdentifier.kt` stays under `app/src/main/java/com/darkfactory/plantpotting/identify/`; `bash scripts/check-stub-isolation.sh` stays green.
- `./gradlew verifyNoNetworking` stays green.
- `expected-artifacts/PLANTPOTTING-0001.txt` unchanged.
- All existing test tags (`camera.shutter`, `result.sourceBadge`, `result.seePottingMix`, `recommendation.*`, `lowConf.*`) preserved. New tags are additive.

### 4.9 Contract-lock pattern — analogue of 0003 §0.4 / 0004 §4.7

Three contract-lock tests RED before any production edit lands:

- `TestIdentifyModuleAbsenceContractTest` (JVM, reflection-driven) — asserts the class `com.darkfactory.plantpotting.identify.TestIdentifyModule` does **not** exist in the androidTest source set. RED today (the class is present). Will go GREEN once §2.4 deletes the file. *Note:* this is unusual because the contract is "this file should not exist" — implement as `assertThat(Class.forName(...))` throwing `ClassNotFoundException`, wrapped in `assertThrows`.
- `PerSpeciesThresholdsContractTest` (JVM) — asserts `ModelManifest` exposes a property `perSpeciesThresholds: Map<String, Float>` via reflection. RED today.
- `LowConfidencePickerSubtitleContractTest` (Robolectric Compose) — asserts the screen exposes a node with tag `LowConfidencePickerTags.SUBTITLE` when wired against a default `LowConfidencePickerViewModel`. RED today.

All three fail RED on `main` before any §1–§6 edit lands. They flip GREEN as the corresponding scope items land.

---

## 5. Task list

TDD ordering: behaviour-changing tasks have paired test tasks that land RED first. The `sprint-execute` skill enforces ticking `- [x]` boxes as work lands; do **not** batch.

### Phase 0 — Setup, contract locks, baseline

- [ ] **0.1** Re-read `docs/sprints/results/PLANTPOTTING-0004.md`, `docs/sprints/feedback/PLANTPOTTING-0004/feedback.md`, and the anchor files: `LowConfidencePickerScreen.kt`, `LowConfidencePickerViewModel.kt`, `CameraUiState.kt`, `CameraScreen.kt` (Failure region), `TestIdentifyModule.kt`, `FakeFixedIdentifier.kt`, `PermissionDeniedFlowTest.kt`, `FakeCameraPermissionGuard.kt`, `ModelScoreMapper.kt`, `ModelManifest.kt`, `OnDeviceModelRealInterpreterTest.kt`, `app/src/androidTest/assets/identify-fixtures/LICENSE.txt`.
- [ ] **0.2 (baseline)** Run `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` + `bash scripts/check-stub-isolation.sh` on clean `main` **before** any edits. Capture full output to `docs/sprints/results/PLANTPOTTING-0005-baseline.txt`. Surface any baseline regression to the user before continuing.
- [ ] **0.3** Update `docs/sprints/ledger.yaml`: `PLANTPOTTING-0005` `status: in-progress`, stamp `executor`, refresh `updated`.
- [ ] **0.4** Grep `app/src/androidTest/` for tests that compile against the global `TestIdentifyModule` swap (any `@HiltAndroidTest` that injects `PlantIdentifier` or `CameraViewModel` without its own `@BindValue`). Record the exact list in the results doc and reconcile against the "six tests" in `results/PLANTPOTTING-0004.md` §5. If the count differs, update §3.1's migration list before any code moves.
- [ ] **0.5** Open `PermissionScreenTags.kt` and confirm the test tag for the "Open Settings" button used in the permanent-denial path. Record the tag name in the results doc. (Cross-check against `PermissionDeniedFlowTest`'s `@Ignore`d test body — the original test clicks `GRANT_BUTTON`, which is the wrong starting point per the §4.6 decision.)
- [ ] **0.6 (contract-lock test, RED first)** Add `TestIdentifyModuleAbsenceContractTest` at `app/src/test/java/com/darkfactory/plantpotting/identify/TestIdentifyModuleAbsenceContractTest.kt`. Asserts `Class.forName("com.darkfactory.plantpotting.identify.TestIdentifyModule")` throws `ClassNotFoundException`. RED today (class exists in androidTest classpath — actually verify the JVM `test` source set sees it, may need to use the androidTest reflection probe instead; pick the source set at task time). Flips GREEN after §2.4.
- [ ] **0.7 (contract-lock test, RED first)** Add `PerSpeciesThresholdsContractTest` at `app/src/test/java/com/darkfactory/plantpotting/identify/model/PerSpeciesThresholdsContractTest.kt`. Reflection-based: asserts `ModelManifest::class.java.declaredFields.any { it.name == "perSpeciesThresholds" && it.type == Map::class.java }`. RED today. Flips GREEN after §5.2.
- [ ] **0.8 (contract-lock test, RED first)** Add `LowConfidencePickerSubtitleContractTest` at `app/src/test/java/com/darkfactory/plantpotting/result/LowConfidencePickerSubtitleContractTest.kt` (Robolectric + `createComposeRule()`). Wires the screen against a no-arg `LowConfidencePickerViewModel` (use a fake `SavedStateHandle` and the canonical test KB). Asserts `composeRule.onNodeWithTag(LowConfidencePickerTags.SUBTITLE).assertExists()`. RED today (tag does not exist yet). Flips GREEN after §1.1.

### Phase 1 — LowConfidencePicker polish

- [ ] **1.1** Add `LowConfidencePickerTags.SUBTITLE = "lowConf.subtitle"`. Add `R.string.low_conf_subtitle` ("This model recognises a limited plant vocabulary — please confirm or pick below."). Render a `Text` with `MaterialTheme.typography.bodyMedium` under the existing headline, tagged `SUBTITLE`. (Flips §0.8 GREEN.)
- [ ] **1.2 (test, RED first)** Add `LowConfidencePickerEmptyCandidatesTest` to `app/src/test/java/com/darkfactory/plantpotting/result/LowConfidencePickerScreenTest.kt` (extend the existing test file). Two methods: `emptyCandidatesShowsInfoCard` (constructs the screen with `topCandidates = emptyList()`, asserts `onNodeWithTag(LowConfidencePickerTags.NO_CANDIDATES_EMPTY).assertExists()`); `nonEmptyCandidatesHidesInfoCard`. RED today.
- [ ] **1.3** Add `LowConfidencePickerTags.NO_CANDIDATES_EMPTY = "lowConf.noCandidatesEmpty"`. Implement the empty state: when `viewModel.topCandidates.isEmpty()`, render an outlined `Card` with the empty-state copy in place of the chip row. (Copy: "No close matches — pick from the full list below.", via `R.string.low_conf_no_candidates`.)
- [ ] **1.4 (test, RED first)** Add `searchWithNoMatchesShowsEmptyState` test method. Construct the screen, drive `onQueryChange("zzzz")` via the VM, assert `onNodeWithTag(LowConfidencePickerTags.SEARCH_EMPTY).assertExists()` and the species list is gone. RED today.
- [ ] **1.5** Add `LowConfidencePickerTags.SEARCH_EMPTY = "lowConf.searchEmpty"`. Implement: when `query.isNotBlank() && filtered.isEmpty()`, replace the `LazyColumn` with a `Text` "No species match \"$query\"" (via `R.string.low_conf_search_empty`, with one positional arg).
- [ ] **1.6** Replace the bottom `Button` for "I don't know — pick by archetype" with `OutlinedButton`. Tag and copy unchanged.
- [ ] **1.7** Add `Icons.Outlined.ChevronRight` `trailingIcon` to candidate `AssistChip`s. (`androidx.compose.material.icons.outlined.ChevronRight` is in the existing `material-icons-extended` dep if it's on the classpath; if not, add a lightweight inline vector — do NOT pull `material-icons-extended` for one icon.)
- [ ] **1.8** Run `./gradlew --no-daemon testDebugUnitTest` to confirm Phase 1 tests are GREEN. Capture output to `docs/sprints/results/PLANTPOTTING-0005-phase1-verify.txt`.

### Phase 2 — CameraUiState.Failure polish

- [ ] **2.1 (test, RED first)** Add `CameraFailureBannerTest` to `app/src/test/java/com/darkfactory/plantpotting/camera/CameraScreenTest.kt` (or create if it doesn't exist — verify at task time). Two methods: `failureStateRendersBanner` (asserts `onNodeWithTag(CameraScreenTags.FAILURE_BANNER).assertExists()` when `viewModel.state.value = CameraUiState.Failure("test reason")`); `failureBannerRetryClickResetsState` (clicks `FAILURE_RETRY`, asserts state flips to `Idle`). RED today.
- [ ] **2.2** Add `CameraScreenTags.FAILURE_BANNER = "camera.failureBanner"` and `CameraScreenTags.FAILURE_RETRY = "camera.failureRetry"`. Keep `CameraScreenTags.ERROR` for back-compat (alias on the body text inside the banner).
- [ ] **2.3** Replace the top-center `Text` block at `CameraScreen.kt:140-149` with a Material3 `Card` (`OutlinedCardElevation` or equivalent), anchored at `Alignment.BottomCenter` with `.padding(bottom = 144.dp)` so the shutter remains visible above it. Card contains: leading `Icons.Outlined.ErrorOutline`, body `Text(state.reason, tag = CameraScreenTags.ERROR)`, trailing `TextButton(onClick = viewModel::reset, tag = CameraScreenTags.FAILURE_RETRY) { Text("Try again") }`.
- [ ] **2.4** Update the existing shutter `shutterEnabled` predicate (line 153-155) — confirm it still allows the `Failure` state to re-enable the shutter; the new banner doesn't change this, but verify with the test from §2.1.
- [ ] **2.5** Add `R.string.camera_failure_retry` ("Try again") and `R.string.camera_failure_banner_content_description` (for the leading icon a11y, e.g. "Error icon").
- [ ] **2.6** Run `./gradlew --no-daemon testDebugUnitTest` to confirm Phase 2 tests are GREEN.

### Phase 3 — Test-infra refactor (`TestIdentifyModule` removal + `@BindValue` migration)

**Order matters — migrate tests before deleting the module, then delete.**

- [ ] **3.1** Confirm `FakeFixedIdentifier`'s constructor signature supports the call sites the six tests will need. If a test needs `lowConfidence = true` and the constructor doesn't currently expose that, add a defaulted constructor flag (`class FakeFixedIdentifier(val lowConfidence: Boolean = false, val speciesId: String = "monstera-deliciosa")`). Do not break existing tests.
- [ ] **3.2** Migrate `EndToEndFlowTest`: add `@BindValue @JvmField var fakeIdentifier: PlantIdentifier = FakeFixedIdentifier()` to the test class. Confirm `./gradlew --no-daemon :app:compileDebugAndroidTestKotlin` GREEN.
- [ ] **3.3** Migrate `CameraPreviewLayoutTest` the same way. Compile-check.
- [ ] **3.4** Migrate `CameraScreenSmokeTest`. Compile-check.
- [ ] **3.5** Migrate `CameraScreenBindStateTest`. Compile-check.
- [ ] **3.6** Migrate `PermissionResumeRecoveryTest`. Compile-check.
- [ ] **3.7** Migrate `PermissionDeniedFlowTest` (the existing test methods; §4 below handles the un-ignore and behavioural change for `openSettingsIntentFiresOnPermanentDenial`). Compile-check.
- [ ] **3.8** Delete `app/src/androidTest/java/com/darkfactory/plantpotting/identify/TestIdentifyModule.kt`. Confirm `./gradlew --no-daemon :app:compileDebugAndroidTestKotlin` GREEN. (Flips §0.6 GREEN.)
- [ ] **3.9** Run `./gradlew --no-daemon pixel6Api34DebugAndroidTest` to confirm all six migrated tests pass + the existing `OnDeviceModelRealInterpreterTest` (concrete-class injection) still passes. Capture output to `docs/sprints/results/PLANTPOTTING-0005-phase3-gmd.txt`.

### Phase 4 — LowConfidenceFlowTest + PermissionDeniedFlowTest un-ignore

- [ ] **4.1 (test, lands GREEN — exercising existing behaviour)** Add `app/src/androidTest/java/com/darkfactory/plantpotting/LowConfidenceFlowTest.kt`. `@HiltAndroidTest`. `@BindValue` a `FakeFixedIdentifier(lowConfidence = true)`. `@get:Rule` chain: `HiltAndroidRule`, `GrantPermissionRule.grant(Manifest.permission.CAMERA)`, `createAndroidComposeRule<MainActivity>()`. Test method: shutter → assertHeadlineDisplayed → click species → assertOnResultScreen(lowConfidence = true) → click `result.seePottingMix` → assertOnRecommendationScreen.
- [ ] **4.2** If `FakeFixedIdentifier` doesn't expose `mostRecentCandidates` (it'd need to implement `CandidateProvider` to drive a non-empty chip row), keep the test minimal: `lowConfidence = true` but no candidates, exercising the empty-state path from §1.3. (The full-candidate path is implicitly covered by the new §1.2 JVM test.)
- [ ] **4.3** Extend `FakeCameraPermissionGuard` (if needed) with a `permanentlyDenied: Boolean` flag exposed in its constructor; default `false`. Wire it into whatever method the production code consults for "permanently denied" semantics (look up the guard's API at task time).
- [ ] **4.4** Replace the `@Ignore`d `openSettingsIntentFiresOnPermanentDenial` body in `PermissionDeniedFlowTest`: use a per-test `FakeGuardStateRule(granted = false, permanentlyDenied = true)` (extend the rule if needed); click the "Open Settings" button (tag confirmed at §0.5); assert the `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` intent fired via `Intents.intended(allOf(hasAction(...)))`.
- [ ] **4.5** Remove `@Ignore` from `openSettingsIntentFiresOnPermanentDenial`.
- [ ] **4.6** Run `./gradlew --no-daemon pixel6Api34DebugAndroidTest`. Confirm `@Ignore` count drops to zero in the GMD output. Capture to `docs/sprints/results/PLANTPOTTING-0005-phase4-gmd.txt`.

### Phase 5 — Confidence calibration scaffold

- [ ] **5.1 (test, RED first)** Add `ModelScoreMapperPerSpeciesThresholdTest` at `app/src/test/java/com/darkfactory/plantpotting/identify/model/ModelScoreMapperPerSpeciesThresholdTest.kt`. Two methods: `perSpeciesOverrideUsedWhenPresent` (manifest seeded with `perSpeciesThresholds = mapOf("monstera-deliciosa" to 0.35f)`, top-1 score = 0.40f → high-confidence); `globalThresholdUsedWhenNoOverride` (override map empty, score = 0.40f → low-confidence). RED today.
- [ ] **5.2** Add `val perSpeciesThresholds: Map<String, Float> = emptyMap()` to `ModelManifest`. Parse `per_species_thresholds` (snake-case) in `ModelManifestReader`; defaults to empty if the field is absent (this field is optional even for the production manifest — unlike `input_dtype` from 0004 §4.1, which was a hard requirement). (Flips §0.7 GREEN.)
- [ ] **5.3** Update `ModelScoreMapper` to consult `manifest.perSpeciesThresholds[speciesId] ?: manifest.thresholds.high_confidence_plain` wherever the high-confidence threshold is read. (Confirm at task time exactly which methods/branches read the threshold; the manifest's `_comment_thresholds` calls out `high_confidence_plain`, `high_confidence_margin_min`, `high_confidence_margin_delta`. Override only the `_plain` value — margin overrides are deferred.)
- [ ] **5.4** Source a CC-licensed Monstera deliciosa photograph from Wikimedia Commons (or equivalent). Resize to 480×480 JPEG-quality-80. Replace `app/src/androidTest/assets/identify-fixtures/monstera-deliciosa.jpg`. Update `LICENSE.txt` with the new attribution + source URL + licence name. **Verify the licence terms permit redistribution in this repo's licence.** Note in the commit message that the previous procedural fixture is replaced (record the previous fixture's seed `0x4D4F4E54` in the commit body for audit).
- [ ] **5.5 (probe, NOT a checked-in test)** Add a temporary `println` (or a `@Test` method tagged `@Ignore("probe — remove after observation")`) inside `OnDeviceModelRealInterpreterTest` that captures: top-1 species ID, top-1 score, top-3 species/score pairs, `result.lowConfidence`. Run the test against the real fixture on GMD. **Record the captured output verbatim in `docs/sprints/results/PLANTPOTTING-0005.md`.** Remove the probe before committing the final test.
- [ ] **5.6 (test, after probe)** Add the accuracy-bearing assertion to `OnDeviceModelRealInterpreterTest`. Preferred form: `assertThat(result.speciesId).isEqualTo("monstera-deliciosa"); assertThat(result.lowConfidence).isFalse()`. Fallback (only if §5.5 shows the photo still routes low-conf): `assertThat(result.source).isEqualTo(IdSource.ON_DEVICE_MODEL); assertThat(observedTop3SpeciesIds).contains("monstera-deliciosa")` — pulling top-3 via `(identifier as? CandidateProvider)?.mostRecentCandidates`. Record the decision (preferred vs fallback) in the results doc with the §5.5 probe numbers as evidence.
- [ ] **5.7** Run `./gradlew --no-daemon pixel6Api34DebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.darkfactory.plantpotting.identify.OnDeviceModelRealInterpreterTest` to confirm §5.6 lands GREEN. Capture output to `docs/sprints/results/PLANTPOTTING-0005-phase5-real-model.txt`.

### Phase 6 — ROADMAP.md and README link

- [ ] **6.1** Write `docs/ROADMAP.md` per §3.1 (one page, four sections). Use the same Markdown conventions as `README.md`. Link forward to the candidate next sprint (PLANTPOTTING-0006) — name it as "Confidence calibration v2: multi-species fixture sweep + per-species threshold seeding" or similar; do not commit to a specific scope, just signal the candidate.
- [ ] **6.2** Update `README.md`'s "Current state" section to reflect post-0005 state (LowConfidencePicker polished, Failure UI polished, calibration scaffold in place, test infra refactored). Replace the existing "Known gaps (carried to PLANTPOTTING-0005, see …)" bullet list with a one-line "See [`docs/ROADMAP.md`](docs/ROADMAP.md) for layer status and the gap inventory."
- [ ] **6.3** Bump the README's status table to include `PLANTPOTTING-0005 | Post-shutter polish + un-defer carry-forward | done` (only after the ledger flips at §7.3 — sequence-check at PR time).

### Phase 7 — Final-verify, evidence, ledger close

- [ ] **7.1 (final-verify, JVM gates)** Run `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking`. Capture full output to `docs/sprints/results/PLANTPOTTING-0005-final-verify.txt`. **Required green.**
- [ ] **7.2 (final-verify, instrumentation)** Run `./gradlew --no-daemon pixel6Api34DebugAndroidTest`. Capture full output to `docs/sprints/evidence/PLANTPOTTING-0005/gmd-output.txt`. **Required green; required `@Ignore` count == 0.**
- [ ] **7.3 (final-verify, integration-flow)** Run `pwsh ./scripts/integration-flow.ps1` (cold + warm) and `pwsh ./scripts/integration-flow.ps1 -BuildOnly`. Capture transcripts to `docs/sprints/evidence/PLANTPOTTING-0005/transcript-A-cold.txt`, `transcript-B-warm.txt`, `transcript-C-buildonly.txt`. **Required green: `Integration manifest diff passed.`** for all three.
- [ ] **7.4 (final-verify, stub isolation)** Run `bash scripts/check-stub-isolation.sh`. **Required green.** Note in the results doc.
- [ ] **7.5 (audit)** Confirm `grep -R "TestIdentifyModule" app/src/androidTest/` returns zero hits. Confirm `grep -R "@Ignore" app/src/androidTest/` returns zero hits. Confirm `grep -R "testTagsAsResourceId" app/src/main/` still returns exactly one hit at `MainActivity.kt` (no regression from 0004's cleanup).
- [ ] **7.6** Write `docs/sprints/results/PLANTPOTTING-0005.md`. Cover: baseline, every phase's diff summary, contract-lock RED→GREEN evidence, probe numbers from §5.5, accuracy-assertion choice from §5.6, final-verify gates, list of files added/modified/deleted, any deferrals carried to PLANTPOTTING-0006.
- [ ] **7.7** Update `docs/sprints/ledger.yaml`: `PLANTPOTTING-0005` `status: done`, refresh `updated` timestamp.
- [ ] **7.8** Tick all `- [ ]` boxes in this plan to `- [x]` as work lands (the `sprint-execute` skill should be doing this incrementally; final pass at close-out verifies nothing was missed).
- [ ] **7.9** Final commit hash recorded in the results doc.

---

## 6. Sequencing

The seven phases are mostly independent and can land in the order above, but a few cross-phase dependencies matter:

- **Phase 0 must complete before any other phase.** Baseline + contract-lock RED is the falsifiability foundation.
- **Phase 3 (test-infra refactor) must precede Phase 4 (new instrumentation test).** `LowConfidenceFlowTest` is written with `@BindValue` from day one — easier than writing it against the old global swap and then migrating it.
- **Phase 5.4 (real fixture) must precede Phase 5.5 (probe) which must precede Phase 5.6 (accuracy assertion).** The probe is the input to the assertion's design.
- **Phase 6 (ROADMAP) can land in parallel with any other phase** — it's pure documentation, no code dependency. Recommended order: after Phase 5 so the roadmap can name the calibration scaffold as landed.
- **Phase 7 (final-verify) must be last.** Re-running the gate chain after every prior phase is wasteful; one final-verify at the end is enough as long as every phase ran its own scoped verify.

A reasonable executor sequence:

```
0  (setup, contract locks RED)
  ↓
1  (LowConfidencePicker polish) ──┐
2  (CameraUiState.Failure polish) ┤  parallel-safe (no shared files)
                                  │
3  (TestIdentifyModule removal) ──┘  must precede Phase 4
  ↓
4  (LowConfidenceFlowTest + un-ignore)
  ↓
5  (calibration scaffold; probe gates §5.6)
  ↓
6  (ROADMAP + README link)
  ↓
7  (final-verify + results + ledger)
```

Phases 1 and 2 touch disjoint files; Phase 3 touches androidTest only. A motivated executor can interleave 1, 2, 3 once their phase-0 contract-locks are RED.

---

## 7. Risks

### 7.1 `@BindValue` migration breaks one of the six tests

**Likelihood:** medium. `@BindValue` + `@JvmField` is finicky in Kotlin — getting the visibility or the placement wrong produces a Hilt-generated-graph compile error that points at the generated file, not the source. **Mitigation:** §3.2–§3.7 compile-check after each migration, not after the batch. **Fallback:** if a specific test can't be migrated cleanly, leave it on a per-class `@TestInstallIn(replaces = [OnDeviceIdentifyModule::class])` module scoped to that one test (no global swap, no other test affected). Document the fallback in the results doc.

### 7.2 The real Monstera photo still routes low-confidence on the AOSP virtual scene

**Likelihood:** low for the photo on a JPEG-decode path (the fixture isn't going through the camera; it's loaded from assets). Medium if the AIY V1/3 model's training set didn't include Monstera deliciosa under that exact name/index — `_comment_coverage` says it did (one of the 2 mapped species), so the photo should clear the threshold. **Mitigation:** the §5.5 probe is exactly for this. **Fallback:** §5.6 fallback assertion (`top3 contains`, not `top1 equals`) preserves an honest accuracy bar even if the photo routes low-conf for reasons we don't fully control.

### 7.3 `FakeCameraPermissionGuard` doesn't expose enough state to drive "permanently denied"

**Likelihood:** medium — the existing fake was written for "granted vs not granted" per 0001. **Mitigation:** §4.3 extends the fake minimally. **Fallback:** if extension is more invasive than expected, delete the `@Ignore`d test rather than `@Ignore` it again. The "Open Settings" intent firing is a one-line production behaviour that the JVM-side `PermissionViewModelTest` (if it exists; check at task time) can cover; losing instrumentation coverage of an intent is acceptable; carrying an `@Ignore`d test for another sprint is not.

### 7.4 `LowConfidencePicker` empty-state copy lands in awkward states

**Likelihood:** medium for the "no candidates" path on real captures — today the screen silently omits the chip row, so users don't see anything missing; adding an info card surfaces a state that today is invisible. Some users may find it noisy. **Mitigation:** copy is intentionally short and informational, not alarming. **Fallback:** if review surfaces the card as too prominent, demote it to a one-line `Text` in PLANTPOTTING-0006; the test tag preserves the regression catch.

### 7.5 Material3 banner Card competes with the shutter for tap-target priority

**Likelihood:** low. The banner is anchored above the shutter (`padding(bottom = 144.dp)`) and the shutter remains a `FloatingActionButton` with its own elevation. **Mitigation:** §2.4 verifies the shutter remains enabled in the `Failure` state; §2.1 test asserts retry click path works. **Fallback:** if device-side review shows tap-target overlap, swap `Card` for `Snackbar` (the rejected option in §4.2) — the test tags survive the swap.

### 7.6 Probe-run code accidentally ships

**Likelihood:** medium — the §5.5 probe is explicitly temporary code. **Mitigation:** §5.5 calls out "remove the probe before committing the final test" as a step. The results doc captures the probe output, so the data survives the code removal. **Fallback:** the §0.6 contract-lock test asserts file absence, not behaviour; add a final-verify `grep` (or extend §7.5's audit) for `println(` and `@Ignore("probe` in the androidTest source set.

### 7.7 `per_species_thresholds` map gets seeded by accident

**Likelihood:** low — the field is optional and defaults to empty. **Mitigation:** §5.2 explicitly does not seed values; §3.1's "no production values seeded" bullet calls it out. **Fallback:** if a value sneaks in, the results doc's "files modified" section will catch it at §7.6 review.

### 7.8 `LowConfidencePickerViewModel` constructor change breaks existing tests

**Likelihood:** low — this sprint doesn't change the VM's constructor surface. **Mitigation:** keep the VM untouched. **Fallback:** if a polish task drifts into VM behaviour (e.g. "make the subtitle copy reactive to candidate count"), revert that drift; the screen polish is the scope.

### 7.9 Real photo fixture licence is incompatible with this repo's licence

**Likelihood:** low if sourced from Wikimedia Commons CC-BY or CC0. Medium if the developer picks a random Flickr photo. **Mitigation:** §5.4 explicitly says "Wikimedia Commons CC-BY-SA or CC-BY" and "verify the licence terms permit redistribution". **Fallback:** if the picked photo's licence is incompatible, find another; the fixture is replaceable.

### 7.10 GMD run for the un-ignored `PermissionDeniedFlowTest` fires the Settings intent and disrupts the GMD environment

**Likelihood:** low — `Intents.intercept` (used in the existing test setup via `Intents.init()` / `Intents.release()`) blocks the intent from actually launching the target Activity; the test only asserts the intent was *issued*. **Mitigation:** confirm `Intents.init()` is called in `@Before` (it is, per the existing test body) and the assertion uses `Intents.intended(...)` not `Intents.releasable()`. **Fallback:** if the intent leaks and Settings actually launches on GMD, wrap the test body in an `IntentsTestRule` (older API but more aggressive interception) or guard with a `try/finally` around `Intents.release()`.

---

## 8. Acceptance criteria

This sprint is `done` when **every** one of these holds:

- [ ] §3.1 must-land bullets are all ticked (and the nice-to-have / out-of-scope sections honestly reflect what landed).
- [ ] `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` GREEN.
- [ ] `./gradlew --no-daemon pixel6Api34DebugAndroidTest` GREEN with **zero** `@Ignore`d tests in the androidTest source set.
- [ ] `bash scripts/check-stub-isolation.sh` GREEN.
- [ ] `pwsh ./scripts/integration-flow.ps1` (cold + warm) and `-BuildOnly` all emit `Integration manifest diff passed.`.
- [ ] `grep -R "TestIdentifyModule" app/src/androidTest/` returns zero hits; `grep -R "@Ignore" app/src/androidTest/` returns zero hits; `grep -R "testTagsAsResourceId" app/src/main/` returns exactly one hit at `MainActivity.kt`.
- [ ] §0.6 / §0.7 / §0.8 contract-lock tests RED on baseline, GREEN at sprint close (evidence in the results doc).
- [ ] `OnDeviceModelRealInterpreterTest` asserts an accuracy-bearing claim against a real CC-licensed Monstera photo (preferred or fallback form per §5.6).
- [ ] `app/src/androidTest/assets/identify-fixtures/LICENSE.txt` reflects the real photo's attribution + licence + source URL.
- [ ] `docs/ROADMAP.md` exists, one page, four sections; `README.md` links it.
- [ ] `docs/sprints/results/PLANTPOTTING-0005.md` covers baseline, per-phase diff summary, contract-lock evidence, probe numbers, accuracy-assertion decision, final-verify gates, files-added/modified/deleted, deferrals to 0006.
- [ ] `docs/sprints/ledger.yaml`: `PLANTPOTTING-0005` `status: done`, `executor` stamped.
- [ ] No new `@Ignore`s introduced. No new dependencies added. No model swap. No KB edits. No interface-shape changes to `PlantIdentifier` / `IdentificationResult`. No `expected-artifacts` re-baselining.

---

## 9. Sprint window estimate

| Phase | Estimated effort (single AI implementer) |
| --- | --- |
| Phase 0 (setup, contract locks, baseline) | 0.5 day |
| Phase 1 (LowConfidencePicker polish) | 1.0 day |
| Phase 2 (CameraUiState.Failure polish) | 0.5 day |
| Phase 3 (test-infra refactor) | 1.0 day |
| Phase 4 (LowConfidenceFlowTest + un-ignore) | 0.5 day |
| Phase 5 (calibration scaffold + real photo + probe + assertion) | 1.5 days |
| Phase 6 (ROADMAP + README) | 0.25 day |
| Phase 7 (final-verify + results + ledger) | 0.5 day |
| **Total** | **~5.75 days** |

Within the 5–7 day envelope at the top. Risk §7.1 (`@BindValue` finicky) and §7.2 (probe outcome) are the two most likely sources of slippage; both have documented fallbacks that keep the sprint in scope.
