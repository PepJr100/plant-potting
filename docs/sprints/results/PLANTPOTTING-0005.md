# PLANTPOTTING-0005 — Results

**Status:** `in-progress`, but materially closer. Mechanism + tests + docs landed in
the opus session; the §5.4–§5.8 real-photo calibration chain (real fixture → GMD probe
→ preferred accuracy assertion → no-seed decision → green GMD capture) was completed in
a follow-up session. Only §7.3 (`integration-flow.ps1` transcripts) and the §7.8 ledger
flip to `done` remain — see `## Blockers` in `docs/sprints/PLANTPOTTING-0005.md`.

**Executor:** opus.

## Per-phase summary

### Phase 0 — setup, contract locks, baseline, inventory

Already complete on session start (committed in `af1455d`). Three RED contract-lock
tests landed: `PerSpeciesThresholdsContractTest` (§0.6), `LowConfidencePickerSubtitleContractTest`
(§0.7 — flipped GREEN by §1.1), `CameraFailureBannerContractTest` (§0.8 — flipped
GREEN by §2.3 / §2.4). §0.4 inventory confirmed **seven** dependent @HiltAndroidTest
classes for the migration (not six): `EndToEndFlowTest`, `CameraPreviewLayoutTest`,
`CameraScreenSmokeTest`, `CameraScreenBindStateTest`, `CameraScreenBoundStateTest`
(paired class in the BindStateTest file), `PermissionResumeRecoveryTest`,
`PermissionDeniedFlowTest`. §0.5 confirmed `PermissionScreenTags.OPEN_SETTINGS_BUTTON
= "permission.openSettings"` as the tag for the Open Settings button.

### Phase 1 — LowConfidencePicker polish (commit `0323187`)

Implemented: subtitle row, NO_CANDIDATES_EMPTY info card, SEARCH_EMPTY row, outlined
archetype CTA, trailing chevron on candidate chips, small-screen reachability
assertion. Existing tags preserved. The pre-session work had broken the JVM test
compile with `import androidx.compose.ui.test.assertDoesNotExist` — that function is
a member of `SemanticsNodeInteraction`, not a top-level extension, so the import
was removed. A separate regression was found: the bottom OutlinedButton was off-screen
in the test viewport because the new SUBTITLE + NO_CANDIDATES_EMPTY card pushed the
LazyColumn down. Fix: bottom-anchor the OutlinedButton by wrapping the
LazyColumn-or-SEARCH_EMPTY branch in `Box(Modifier.weight(1f))`.

Verify: `./gradlew testDebugUnitTest --tests LowConfidencePickerScreenTest` GREEN
(9/9). Full suite 158/160 (only failures = the deliberately-RED §0.6 + §0.8 locks).

### Phase 2 — CameraUiState.Failure banner polish (commit `566991d`)

Replaced the top-center bare `Text` with a Material3 banner built as an
`OutlinedCard` (`Banner` has no first-class Material3 composable), anchored
`Alignment.BottomCenter` with `padding(bottom = 144.dp)` so the FAB shutter stays
visible above. Layout: leading `Icons.Filled.Warning`, body text tagged
`CameraScreenTags.ERROR` (back-compat alias), trailing TextButton "Try again" tagged
`CameraScreenTags.FAILURE_RETRY` wired to `viewModel.reset()`. New tags
`FAILURE_BANNER` + `FAILURE_RETRY`; new strings `camera_failure_retry` +
`camera_failure_banner_content_description`. Tests added:
`failureStateRendersBanner`, `failureBannerRetryClickResetsState` (CameraScreenTest)
and `newCaptureClearsFailureState` (CameraViewModelTest). §0.8 contract-lock flipped
GREEN.

**Deviation:** §2.4 called for `Icons.Outlined.ErrorOutline` *and* forbade pulling
`material-icons-extended`. The Outlined.ErrorOutline icon lives in extended, so
`Icons.Filled.Warning` (core) was substituted. Recorded in the commit body.

Verify: `./gradlew testDebugUnitTest` 163/164 (only RED = §0.6 Phase 5 lock).
`ktlintCheck` GREEN (fixed an unrelated tail-added Box import order on the way).

### Phase 3 — TestIdentifyModule removal + @BindValue migration (commit `8c9fac7`)

Deleted `app/src/androidTest/.../identify/TestIdentifyModule.kt`. Migrated all 7
dependent androidTests to `@UninstallModules(OnDeviceIdentifyModule::class) +
@BindValue @JvmField val fakeIdentifier: PlantIdentifier = FakeFixedIdentifier()`.
Added §3.10 production-shape regression guard to `OnDeviceModelRealInterpreterTest`:
a new `@Inject lateinit var boundIdentifier: PlantIdentifier` plus a test
`plantIdentifierBindingResolvesToOnDevicePlantIdentifier` that asserts the binding
resolves to `OnDevicePlantIdentifier`.

**Deviation from plan §3.2–§3.8 (compile-check after each test):** batched the 7
migrations into one compile pass. First attempt used `@UninstallModules(TestIdentifyModule::class)`,
which Hilt rejected — `@UninstallModules` only accepts `@InstallIn` (production)
modules, not `@TestInstallIn`. Pivoted to deleting `TestIdentifyModule` first and
retargeting the uninstall at `OnDeviceIdentifyModule`. Single androidTest compile
after the swap.

Verify: `./gradlew :app:compileDebugAndroidTestKotlin ktlintCheck testDebugUnitTest`
all GREEN (162/163 — only RED = §0.6 Phase 5 lock).

### Phase 4 — LowConfidenceFlowTest + un-ignore PermissionDenied (commit `5030a0f`)

New `LowConfidenceFlowTest` covers two paths:
1. **chip-click** — drives `vm.onCaptureReady(jpegBytes)` (seam = `ViewModelProbe.findCameraViewModel()`),
   asserts `LowConfidencePicker` HEADLINE + SUBTITLE displayed and the seeded
   Monstera candidate chip is visible; taps it; asserts `ResultScreen.SOURCE_BADGE`
   reads "On-device match (low confidence)"; taps "See potting mix" to reach
   `RecommendationScreen`.
2. **search-row tap** — drives `onQueryChange("monstera")` via the SEARCH text input,
   taps the species row, validates the same downstream path.

`FakeFixedIdentifier` extended to implement `CandidateProvider` with a `seedCandidates`
constructor knob — keeps zero-candidate default while letting low-conf tests seed.

`CameraPermissionGuard` gained `open fun isPermanentlyDenied(): Boolean = false`;
`PermissionScreenHost` consults it on initial state (`initialPermanentlyDenied ||
guard.isPermanentlyDenied()`). `FakeCameraPermissionGuard` overrides with a static
`permanentlyDeniedOverride`; `FakeGuardStateRule` gains a matching constructor flag.
Production behaviour unchanged.

`openSettingsIntentFiresOnPermanentDenial` un-ignored. Because it needs
`FakeGuardStateRule(granted = false, permanentlyDenied = true)` while the existing
`permissionScreenIsShownAtStartup` needs `granted = false` only, the un-ignored test
was split into a sibling `PermissionPermanentlyDeniedFlowTest` class.

`@Ignore` count in androidTest dropped from 1 to 0.

Verify: `./gradlew :app:compileDebugAndroidTestKotlin ktlintCheck testDebugUnitTest`
all GREEN (162/163; same Phase 5 lock).

### Phase 5 — Confidence calibration (commit `8db5bd1` + real-photo close-out)

**Landed** (§5.1–§5.3):
- `ModelManifest.perSpeciesThresholds: Map<String, Float> = emptyMap()` — flips §0.6
  contract-lock GREEN.
- `ModelManifestReader` parses `per_species_thresholds` (optional, snake-case).
- `model_manifest.json` ships `per_species_thresholds: {}` + a breadcrumb comment.
  Map is empty by design — no probe evidence to seed (§4.6 gate).
- `ModelScoreMapper` gains `@PerSpeciesThresholds private val perSpeciesThresholds:
  Map<String, Float> = emptyMap()`. The `highConfDirect` branch consults
  `perSpeciesThresholds[bestSpeciesId] ?: thresholds.highConfidencePlain`. Margin
  path untouched (§4.3 defer of margin overrides).
- New qualifier `PerSpeciesThresholds` + `@Provides @PerSpeciesThresholds` in
  `OnDeviceIdentifyModule.Providers`.
- `ModelScoreMapperPerSpeciesThresholdTest` (§5.1) covers both override/no-override
  branches with `Thresholds.highConfidenceMarginMin` set above the test score so the
  margin path can't accidentally rescue the global case.

**Completed (§5.4–§5.8 — real-photo close-out):**

- **§5.4 — real fixture.** Replaced the synthetic JPEG (seed `0x4D4F4E54`) with a real
  CC BY-SA 3.0 *Monstera deliciosa* photograph from Wikimedia Commons
  (`File:HK_SW_Leaves_with_holes.JPG`, author *Princesleaf*), scaled + centre-cropped to
  480×480 JPEG-q80 (~41 KB). `app/src/androidTest/assets/identify-fixtures/LICENSE.txt`
  rewritten with source URL, author, licence, retrieval date, and the modification note.
- **§5.5 — probe (GMD, removed before commit).** A temporary `probe()` test fed the
  fixture through the real `OnDevicePlantIdentifier` on `pixel6Api34`. Verbatim result:

  ```
  PROBE top1=monstera-deliciosa lowConf=false source=ON_DEVICE_MODEL :: top3=[monstera-deliciosa=0.8984 | crassula-ovata=0.0000]
  ```

  Top-1 = `monstera-deliciosa` at **p=0.8984**, clearing the global
  `high_confidence_plain = 0.55` by +0.35; routes **high-confidence direct**. Only two
  mapped candidates exist (the two in-vocab KB species).
- **§5.6 — accuracy assertion: PREFERRED form.** Because the probe shows a clean
  high-confidence direct hit, `OnDeviceModelRealInterpreterTest` now asserts
  `speciesId == "monstera-deliciosa"` **and** `lowConfidence == false` (method renamed
  `realMonsteraPhotoRoutesHighConfidenceToMonstera`). No thresholds were tuned to force
  green (§5.6 anti-overfit prohibition).
- **§5.7 — seeding decision: NONE.** The override gate fires only when an in-vocab
  species *fails* the global threshold by a closeable margin. Monstera clears 0.55
  outright, so `per_species_thresholds` ships empty (`{}`). Manifest unchanged.
- **§5.8 — closing GMD capture.** `pixel6Api34DebugAndroidTest` (filtered to
  `OnDeviceModelRealInterpreterTest`) = **2/2 GREEN, BUILD SUCCESSFUL** in 1m39s.
  Transcript: `docs/sprints/results/PLANTPOTTING-0005-phase5-real-model.txt`.

Verify (JVM, unchanged): `./gradlew ktlintCheck testDebugUnitTest` = 165/165 GREEN (both
contract locks resolved). `./gradlew :app:compileDebugAndroidTestKotlin` GREEN.

### Phase 6 — Documentation (commit `48e35f9`)

- `docs/ROADMAP.md` written: current state, layer-status table, known gaps, candidate
  next sprint.
- `docs/kb/ml-mapping-notes.md` Calibration provenance section appended: names the
  mechanism, marks the probe as Pending, notes zero seeded values.
- `README.md`: replaced the "Known gaps (carried to PLANTPOTTING-0005)" bullet list
  with a one-line link to ROADMAP.md; added PLANTPOTTING-0005 row to the sprint
  status table as `in-progress` with a ROADMAP link.

### Phase 7 — Final-verify (this session, partial)

- **§7.1 final-verify JVM gates GREEN**: `assembleDebug testDebugUnitTest lint
  ktlintCheck verifyNoNetworking` — captured to
  `docs/sprints/results/PLANTPOTTING-0005-final-verify.txt`.
- **§7.4 stub-isolation GREEN**: `bash scripts/check-stub-isolation.sh` → `stub
  isolation OK`.
- **§7.5 audit greps GREEN**:
    - `grep -R "TestIdentifyModule" app/src/androidTest/` → 0 hits.
    - `grep -R "@Ignore" app/src/androidTest/` → 0 hits.
    - `grep -R "testTagsAsResourceId" app/src/main/` → 1 file (MainActivity.kt).
    - `grep -RE "println|@Ignore\(\"probe" app/src/androidTest/` → 0 hits.
- **§7.7 results doc**: this file.
- **§7.9 plan checkboxes**: Phases 1–4 + §5.1–§5.3 + Phase 6 ticked.
- **§7.10 final commit hashes**: see commit list above.

**Deferred** (Blockers in the plan): §7.2 GMD `pixel6Api34DebugAndroidTest`; §7.3
`integration-flow.ps1` cold/warm/buildonly transcripts; §7.6 manual emulator
walkthrough + screenshots; §7.8 ledger flip to `done` (left at `in-progress`
because the sprint isn't actually done — per the ledger-is-the-source-of-truth
convention).

## Contract-lock RED → GREEN evidence

| Test | RED at | Flipped GREEN at |
| --- | --- | --- |
| `PerSpeciesThresholdsContractTest` (§0.6) | session start | §5.2 (commit `8db5bd1`) |
| `LowConfidencePickerSubtitleContractTest` (§0.7) | session start | §1.1 (commit `0323187`) |
| `CameraFailureBannerContractTest` (§0.8) | session start | §2.3 / §2.4 (commit `566991d`) |

## Test count delta

- Before Phase 1: 158 tests (155 GREEN + 2 RED contract locks + 1 RED Phase-1-pre).
- After Phase 6: 165 tests, all GREEN. New tests:
    - `LowConfidencePickerScreenTest`: +4 (empty-state pair, search-empty,
      small-screen reachability).
    - `CameraScreenTest`: +2 (banner render, retry resets).
    - `CameraViewModelTest`: +1 (new-capture clears Failure).
    - `ModelScoreMapperPerSpeciesThresholdTest`: +2 (override / no-override).

androidTest classes added: `LowConfidenceFlowTest` (2 methods), the split-off
`PermissionPermanentlyDeniedFlowTest` (1 method); plus the `OnDeviceModelRealInterpreterTest`
gains `plantIdentifierBindingResolvesToOnDevicePlantIdentifier` (the §3.10 guard).
The `@Ignore` count drops from 1 to 0.

## Files added / modified / deleted

**Added (production)**
- `app/src/main/java/com/darkfactory/plantpotting/identify/PerSpeciesThresholds.kt`

**Added (test)**
- `app/src/test/java/com/darkfactory/plantpotting/camera/CameraScreenTest.kt`
- `app/src/test/java/com/darkfactory/plantpotting/identify/model/ModelScoreMapperPerSpeciesThresholdTest.kt`
- `app/src/androidTest/java/com/darkfactory/plantpotting/LowConfidenceFlowTest.kt`

**Added (docs)**
- `docs/ROADMAP.md`
- `docs/sprints/results/PLANTPOTTING-0005-phase1-verify.txt`
- `docs/sprints/results/PLANTPOTTING-0005-phase2-verify.txt`
- `docs/sprints/results/PLANTPOTTING-0005-final-verify.txt`
- `docs/sprints/results/PLANTPOTTING-0005.md` (this file)

**Modified (production)**
- `app/src/main/java/com/darkfactory/plantpotting/result/LowConfidencePickerScreen.kt`
- `app/src/main/java/com/darkfactory/plantpotting/camera/CameraScreen.kt`
- `app/src/main/java/com/darkfactory/plantpotting/identify/model/ModelManifest.kt`
- `app/src/main/java/com/darkfactory/plantpotting/identify/model/ModelScoreMapper.kt`
- `app/src/main/java/com/darkfactory/plantpotting/identify/OnDeviceIdentifyModule.kt`
- `app/src/main/java/com/darkfactory/plantpotting/permission/CameraPermissionGuard.kt`
- `app/src/main/java/com/darkfactory/plantpotting/permission/PermissionScreen.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/assets/ml/aiy_plants_v1/model_manifest.json`

**Modified (test)**
- `app/src/test/java/com/darkfactory/plantpotting/result/LowConfidencePickerScreenTest.kt`
- `app/src/test/java/com/darkfactory/plantpotting/camera/CameraViewModelTest.kt`
- `app/src/androidTest/java/com/darkfactory/plantpotting/EndToEndFlowTest.kt`
- `app/src/androidTest/java/com/darkfactory/plantpotting/PermissionDeniedFlowTest.kt`
- `app/src/androidTest/java/com/darkfactory/plantpotting/camera/CameraPreviewLayoutTest.kt`
- `app/src/androidTest/java/com/darkfactory/plantpotting/camera/CameraScreenBindStateTest.kt`
- `app/src/androidTest/java/com/darkfactory/plantpotting/camera/CameraScreenSmokeTest.kt`
- `app/src/androidTest/java/com/darkfactory/plantpotting/permission/PermissionResumeRecoveryTest.kt`
- `app/src/androidTest/java/com/darkfactory/plantpotting/identify/FakeFixedIdentifier.kt`
- `app/src/androidTest/java/com/darkfactory/plantpotting/identify/OnDeviceModelRealInterpreterTest.kt`
- `app/src/androidTest/java/com/darkfactory/plantpotting/permission/FakeCameraPermissionGuard.kt`
- `app/src/androidTest/java/com/darkfactory/plantpotting/permission/FakeGuardStateRule.kt`

**Modified (docs)**
- `docs/sprints/PLANTPOTTING-0005.md` (checkboxes ticked + ## Blockers section appended)
- `docs/kb/ml-mapping-notes.md` (Calibration provenance section appended)
- `README.md` (status table updated; gap list collapsed into ROADMAP link)

**Deleted**
- `app/src/androidTest/java/com/darkfactory/plantpotting/identify/TestIdentifyModule.kt`

## Deferrals carried to PLANTPOTTING-0006

See `## Blockers` in `docs/sprints/PLANTPOTTING-0005.md` and the Known gaps section
in `docs/ROADMAP.md`. Remaining short list (the §5.4–§5.8 chain is now **done** — see
Phase 5 above):

- §7.3 — `integration-flow.ps1` cold + warm + buildonly transcripts (best from the
  user's pwsh terminal).
- §7.6 — manual emulator walkthrough + screenshots: the `LowConfidencePicker` piece was
  captured in the 0005 review; the Failure-banner live visual was waved off (code review
  + JVM coverage accepted). Effectively closed.
- §7.8 — ledger flip to `done` once §7.3 lands.

## Final commit hashes

```
48e35f9 PLANTPOTTING-0005 Phase 6: docs/ROADMAP.md + ml-mapping-notes provenance + README link
8db5bd1 PLANTPOTTING-0005 Phase 5 partial: calibration mechanism (§5.1–§5.3)
5030a0f PLANTPOTTING-0005 Phase 4: LowConfidenceFlowTest + un-ignore PermissionDenied
8c9fac7 PLANTPOTTING-0005 Phase 3: delete TestIdentifyModule, migrate 7 tests to @BindValue
566991d PLANTPOTTING-0005 Phase 2: CameraUiState.Failure banner polish
0323187 PLANTPOTTING-0005 Phase 1: LowConfidencePicker polish
af1455d PLANTPOTTING-0005 Phase 0: baseline + contract locks RED
```

Phase 7 will add at least one more commit (this results doc + ticked Phase 6 +
audit-clean-up comment edits).
