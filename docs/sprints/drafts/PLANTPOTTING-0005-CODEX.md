# PLANTPOTTING-0005 - Codex Draft

## Sprint Shape

PLANTPOTTING-0005 is a polish and un-deferral sprint. PLANTPOTTING-0003 shipped the on-device ML path and PLANTPOTTING-0004 fixed the two production blockers, so this sprint should make the post-shutter experience feel intentional while paying down the test architecture debt that let the 0003 bug escape.

The main product reality to design around: AIY Plants V1/3 covers only 2 of the 16 KB species directly, so `LowConfidencePicker` is not an edge-case fallback. It is the primary post-shutter screen for most real captures until the model strategy changes.

## Goals

- [ ] Polish `LowConfidencePickerScreen` as a first-class post-shutter destination, not a fallback.
- [ ] Polish the `CameraUiState.Failure` presentation so failed capture or identification is legible, retryable, and consistent with the rest of the app.
- [ ] Remove the global `TestIdentifyModule` fake identifier replacement and migrate the instrumentation tests that need determinism to local `@BindValue` fields.
- [ ] Add instrumentation coverage for the low-confidence route from camera capture through manual species selection to `ResultScreen`.
- [ ] Re-enable `PermissionDeniedFlowTest.openSettingsIntentFiresOnPermanentDenial` by making the permanent-denial state testable without driving the system permission dialog.
- [ ] Begin confidence calibration by defining a per-class threshold tuning contract, replacing the synthetic Monstera fixture with a real CC-licensed photo, and adding one accuracy-bearing assertion.
- [ ] Add `docs/ROADMAP.md` so sprint review has a single current-state and next-work snapshot to update.

## Scope Boundaries

In scope:

- [ ] Touch Compose UI for `LowConfidencePickerScreen` and `CameraScreen` only where needed for the post-shutter and failure polish.
- [ ] Touch navigation only where needed to keep low-confidence and failure flows testable.
- [ ] Touch Hilt androidTest wiring only to remove the global fake and add local fake bindings.
- [ ] Touch model metadata and tests only for threshold policy documentation, threshold parsing if needed, and the first real-photo accuracy assertion.
- [ ] Touch docs for `docs/ROADMAP.md`, fixture license/source notes, and sprint result evidence expectations.

Out of scope:

- No model swap.
- No INT8 conversion work beyond existing UINT8 handling.
- No GPU, NNAPI, delegate, or performance sprint.
- No ML training, fine-tuning, or dataset buildout beyond one real-photo fixture.
- No net-new app screens. `LowConfidencePicker` and the existing camera failure surface may be redesigned, but this sprint should not add a new route unless the executor proves the current route cannot carry the UX.
- No changes to the `PlantIdentifier` public interface unless a test proves the current contract blocks acceptance.
- No network calls at runtime; `./gradlew verifyNoNetworking` remains a hard gate.

## Working Decisions

- [ ] Treat `LowConfidencePicker` as the default post-shutter experience for unmapped, weak, or uncertain predictions.
- [ ] Preserve honest confidence language: do not imply the model identified a species when the user made the final selection.
- [ ] Keep `ResultScreen` source badge behavior unchanged: low-confidence user picks still surface `on-device match (low confidence)`.
- [ ] Keep the real-model test path production-shaped after the `TestIdentifyModule` removal; at least one androidTest must inject or use production `OnDevicePlantIdentifier` without a fake `PlantIdentifier`.
- [ ] Use a real, license-compatible Monstera photo only if its source, author when available, license, and retrieval date are documented beside the fixture.
- [ ] Keep calibration modest in this sprint: define the per-class threshold mechanism and prove it with one real fixture, but do not claim broad model accuracy.

## Phase 0 - Baseline And Inventory

- [ ] Run `./gradlew.bat --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` on current `main` and capture the output path planned for `docs/sprints/results/PLANTPOTTING-0005-baseline.txt`.
- [ ] Run `bash scripts/check-stub-isolation.sh` and record whether the stub isolation baseline is green.
- [ ] Run `./gradlew.bat --no-daemon pixel6Api34DebugAndroidTest` once before edits to confirm the starting androidTest inventory: currently 10 passing and 1 skipped per 0004.
- [ ] List every androidTest class launched through `MainActivity`: `EndToEndFlowTest`, `PermissionDeniedFlowTest`, `PermissionResumeRecoveryTest`, `CameraScreenSmokeTest`, `CameraScreenBindStateTest`, `CameraScreenBoundStateTest`, and `CameraPreviewLayoutTest`.
- [ ] Identify the exact six deterministic flow/screen tests that need local fake `PlantIdentifier` bindings after `TestIdentifyModule` is removed; if the count differs from six, document the reason in the results.
- [ ] Inspect `LowConfidencePickerScreenTest`, `LowConfidencePickerViewModelTest`, and the current emulator transcript evidence to decide which low-confidence UI states need test tags before UI polish begins.

## Phase 1 - Test Wiring Debt: Remove Global Fake

Current blocker: `app/src/androidTest/java/com/darkfactory/plantpotting/identify/TestIdentifyModule.kt` globally replaces `OnDeviceIdentifyModule` with `FakeFixedIdentifier`. PLANTPOTTING-0004 bypassed that for the real interpreter smoke test by injecting the concrete `OnDevicePlantIdentifier`, but the source set still has a footgun: interface-injection androidTests silently get the fake.

- [ ] Add the local fake binding pattern to `EndToEndFlowTest`: `@BindValue @JvmField val plantIdentifier: PlantIdentifier = FakeFixedIdentifier()`.
- [ ] Add the local fake binding pattern to `CameraPreviewLayoutTest`, because it drives `CameraViewModel.onCaptureReady(...)` through the full graph.
- [ ] Add the local fake binding pattern to each camera instrumentation class that launches `MainActivity` and can reach capture or identification state: `CameraScreenSmokeTest`, `CameraScreenBindStateTest`, and `CameraScreenBoundStateTest`, unless a focused run proves the class never resolves `PlantIdentifier`.
- [ ] Add the local fake binding pattern to permission flow tests that launch `MainActivity` and should stay deterministic after permission navigation reaches the camera: `PermissionResumeRecoveryTest` and the re-enabled `PermissionDeniedFlowTest`, unless a focused run proves no identifier binding is created.
- [ ] Delete `app/src/androidTest/java/com/darkfactory/plantpotting/identify/TestIdentifyModule.kt`.
- [ ] Update comments in androidTests that currently rely on "the global fake" so they name the local `@BindValue` binding instead.
- [ ] Update `OnDeviceModelRealInterpreterTest` comments to remove the obsolete explanation that it must bypass `TestIdentifyModule`.
- [ ] Add or update an androidTest assertion that the real-model smoke test uses `OnDevicePlantIdentifier` and does not resolve `PlantIdentifier` to `FakeFixedIdentifier`.
- [ ] Run `./gradlew.bat --no-daemon :app:compileDebugAndroidTestKotlin` after the migration.
- [ ] Run a focused GMD class filter for the migrated deterministic tests before moving on.

## Phase 2 - LowConfidencePicker Polish

The current screen is functional but skeletal: headline, candidate chips, search, full list, and archetype button. This phase should make it read as "the app is asking for confirmation" rather than "the AI failed."

- [ ] Rewrite the headline and supporting copy so the first line explains uncertainty and the next action without apology or technical phrasing.
- [ ] Promote top candidates into a scan-friendly confirmation area with stable row heights, clear common/scientific names, and visible confidence percentages where present.
- [ ] Keep the full KB species search visible without pushing it below the fold on common Pixel 6 and small-phone portrait viewports.
- [ ] Add an explicit "not listed" or "I do not know" affordance only if it routes to the existing archetype picker; do not add a new screen.
- [ ] Preserve `LowConfidencePickerTags.candidateTag(...)`, `speciesTag(...)`, `SEARCH`, `SPECIES_LIST`, and `PICK_BY_ARCHETYPE` or add replacements with compatibility updates to tests and `integration-flow.ps1`.
- [ ] Add unit/Compose tests that verify polished low-confidence copy, top-candidate visibility, search visibility, species-row tap routing, and archetype CTA routing.
- [ ] Add a small-screen Compose assertion that the search field and at least one selectable species row are reachable without layout overlap.
- [ ] Verify the picked low-confidence result still reaches `ResultScreen` with the low-confidence badge copy.

## Phase 3 - Failure UI Polish

The current failure UI is a white text message aligned at top center over the camera preview. It is easy to miss and gives the user no structured recovery affordance.

- [ ] Replace or augment the top text with a visible failure banner or bottom sheet-style surface inside `CameraScreen`, reusing the existing camera route.
- [ ] Keep the shutter enabled after `CameraUiState.Failure` when `ImageCapture` is bound so the user can retry immediately.
- [ ] Add a concise primary message for capture failure and identification failure; avoid raw TFLite or exception wording in user-visible text.
- [ ] Add an accessible retry affordance if the design needs more than tapping the shutter again.
- [ ] Add or preserve test tags for failure container, failure message, and retry action under `CameraScreenTags`.
- [ ] Add Compose tests or instrumentation tests that drive `CameraViewModel.onCaptureFailed(...)` and assert the failure surface, retry availability, and no overlap with the shutter.
- [ ] Add a ViewModel test that verifies a new capture attempt clears or supersedes the prior `CameraUiState.Failure`.

## Phase 4 - LowConfidenceFlowTest

This sprint should add the missing instrumentation path that PLANTPOTTING-0003 deferred and PLANTPOTTING-0004 made reachable.

- [ ] Add `app/src/androidTest/java/com/darkfactory/plantpotting/LowConfidenceFlowTest.kt`.
- [ ] Use a local `@BindValue` `PlantIdentifier` fake that returns `IdentificationResult(lowConfidence = true, speciesId = "", source = ON_DEVICE_MODEL)` and seeds `mostRecentCandidates` with at least `monstera-deliciosa`.
- [ ] Start from `MainActivity` with camera permission granted through `FakeGuardStateRule(granted = true)` plus platform `GrantPermissionRule`.
- [ ] Drive the flow by invoking `ViewModelProbe.findCameraViewModel()?.onCaptureReady(...)` rather than depending on a real camera frame.
- [ ] Assert `LowConfidencePickerTags.HEADLINE`, `TOP_ROW`, `SEARCH`, and the `monstera-deliciosa` candidate are displayed.
- [ ] Tap `LowConfidencePickerTags.candidateTag("monstera-deliciosa")`.
- [ ] Assert `ResultScreenTags.SOURCE_BADGE` displays the low-confidence source copy and `ResultScreen` names `Monstera deliciosa`.
- [ ] Tap through to `RecommendationScreen` and assert the archetype name plus recipe list are displayed.
- [ ] Add a second test path that searches the full species list and taps `LowConfidencePickerTags.speciesTag(...)` if that remains reliable after UI polish.

## Phase 5 - Re-enable PermissionDeniedFlowTest

The ignored test currently tries to click the permission request button and infer permanent denial through the real system permission dialog. That is the wrong seam for automation. The app already has a fake `CameraPermissionGuard`; use it to place the UI in the permanent-denied state deterministically.

- [ ] Remove `@Ignore` from `PermissionDeniedFlowTest.openSettingsIntentFiresOnPermanentDenial`.
- [ ] Extend `FakeCameraPermissionGuard` or `FakeGuardStateRule` so a test can start with `granted = false` and `shouldShowRationale = false` in the app's permanent-denied branch.
- [ ] If production code lacks a distinct open-settings UI state, add the smallest state exposure needed in the existing permission flow.
- [ ] Update `PermissionDeniedFlowTest` to assert the permission screen starts in the permanent-denied/open-settings state without clicking through the system dialog.
- [ ] Use Espresso Intents to assert tapping the open-settings action emits `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`.
- [ ] Run only `PermissionDeniedFlowTest` on GMD before adding it back to the full suite.

## Phase 6 - Confidence Calibration Start

This is not a full calibration sprint. The target is to stop treating one global threshold block as the final policy and to add one real-photo assertion that makes future calibration work falsifiable.

- [ ] Define the per-class threshold tuning format in `model_manifest.json`, preferably with optional overrides keyed by KB species id or mapped model label while preserving the existing global defaults.
- [ ] Update `ModelManifest` parsing to load the per-class threshold policy without breaking existing global-threshold tests.
- [ ] Update `ModelScoreMapper` to apply a class-specific threshold when the best mapped class has one and fall back to global thresholds otherwise.
- [ ] Add unit tests for class-specific threshold override, fallback to global thresholds, and low-confidence behavior when the best mapped class misses its override.
- [ ] Replace `app/src/androidTest/assets/identify-fixtures/monstera-deliciosa.jpg` with a real CC-licensed Monstera photo that is appropriate for automated testing.
- [ ] Replace `app/src/androidTest/assets/identify-fixtures/LICENSE.txt` with source, author if available, license, URL, retrieval date, and any required attribution for the real photo.
- [ ] Update `OnDeviceModelRealInterpreterTest` so the real-photo fixture has one accuracy-bearing assertion: the result is either a high-confidence `speciesId == "monstera-deliciosa"` or a low-confidence result whose candidates include `monstera-deliciosa`; choose the stricter assertion only after measuring the actual model output.
- [ ] Record observed top-1 label, probability, mapped candidates, and route for the real Monstera fixture in the PLANTPOTTING-0005 results evidence.
- [ ] Add a test or manifest assertion that `Monstera deliciosa` has an explicit per-class calibration entry once the real fixture behavior is known.
- [ ] Do not tune thresholds to force a false green result; if the real photo does not support a direct Monstera assertion, keep the candidate-bearing assertion and document the measured limitation.

## Phase 7 - Roadmap Doc

- [ ] Add `docs/ROADMAP.md`.
- [ ] Include current product state: on-device AIY V1/3 model, low-confidence-first routing reality, KB recommendation engine, no runtime networking.
- [ ] Include layer status: camera, permission, identifier, model assets, KB, recommendation, instrumentation tests, integration script.
- [ ] Include known gaps after 0005: model coverage, calibration depth, visual capture quality, offline fixture set, future model options, and any remaining UX debt.
- [ ] Include next-sprint candidates with short rationale rather than a long backlog.
- [ ] Link to `docs/sprints/results/PLANTPOTTING-0004.md`, the eventual `PLANTPOTTING-0005` results doc, and `docs/kb/ml-mapping-notes.md`.

## Phase 8 - Evidence And Verification

- [ ] Run `./gradlew.bat --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking`.
- [ ] Run `bash scripts/check-stub-isolation.sh`.
- [ ] Run `./gradlew.bat --no-daemon pixel6Api34DebugAndroidTest`.
- [ ] Run `pwsh ./scripts/integration-flow.ps1 -BuildOnly`.
- [ ] Run `pwsh ./scripts/integration-flow.ps1` on a booted emulator and capture the transcript.
- [ ] Manually walk the app on the emulator through permission granted -> camera -> shutter -> `LowConfidencePicker` -> picked species -> `ResultScreen` -> `RecommendationScreen` -> retake.
- [ ] Manually trigger or simulate a camera failure and verify the polished `CameraUiState.Failure` surface is legible and retryable.
- [ ] Capture screenshots or XML dumps for the polished `LowConfidencePicker` and failure UI if the sprint-review workflow needs visual evidence.
- [ ] Update the PLANTPOTTING-0005 results doc with command outputs, fixture source, measured real-photo model output, and any accepted threshold changes.

## Sequencing

- [ ] Start with Phase 0 so the executor knows whether failures are pre-existing.
- [ ] Land Phase 1 before adding new flow tests; otherwise new androidTests may accidentally depend on the global fake that this sprint is meant to remove.
- [ ] Land Phase 2 before Phase 4 so `LowConfidenceFlowTest` locks the polished UI tags and navigation.
- [ ] Land Phase 3 independently of Phase 2; it can be reviewed as soon as camera failure tests are green.
- [ ] Land Phase 5 after Phase 1 because permission tests should use the same local binding pattern as the rest of androidTest.
- [ ] Land Phase 6 after the Hilt migration so the real-photo model test is production-shaped.
- [ ] Land Phase 7 before review so `sprint-review` has `docs/ROADMAP.md` available.
- [ ] Run Phase 8 at the end and again after any late threshold or test-wiring edits.

## Risks And Mitigations

- [ ] Risk: `@BindValue` migration reveals more than six tests need local fake bindings. Mitigation: inventory every `@HiltAndroidTest` that launches `MainActivity`, bind locally where needed, and document the final count.
- [ ] Risk: `@BindValue` fields conflict with Hilt rule ordering. Mitigation: keep `HiltAndroidRule` as rule order 0 and compile androidTest after each migration batch.
- [ ] Risk: `LowConfidencePicker` polish breaks `integration-flow.ps1` resource-id lookup. Mitigation: preserve existing test tags or update the script and expected artifacts in the same change.
- [ ] Risk: a real CC Monstera photo still routes low-confidence or misclassifies. Mitigation: make the first accuracy-bearing assertion candidate-aware unless measured output justifies a stricter species-id assertion.
- [ ] Risk: threshold tuning overfits one fixture. Mitigation: record the measured output and keep override changes minimal; acceptance is one falsifiable calibration assertion, not broad accuracy.
- [ ] Risk: permission denial remains hard to automate if production code only reacts to Android's dialog callback. Mitigation: test the app's permanent-denied branch through `CameraPermissionGuard.shouldShowRationale()` rather than the platform dialog.
- [ ] Risk: failure UI polish accidentally hides camera preview or blocks retry. Mitigation: add layout and enabled-state assertions around the shutter and failure surface.
- [ ] Risk: docs drift from the ledger and results docs. Mitigation: keep `docs/ROADMAP.md` short and current-state oriented, with links instead of duplicating full sprint history.

## Acceptance Criteria

- [ ] `LowConfidencePicker` looks and reads like a finished confirmation flow, with candidate rows, search, and archetype escape path visible and tested.
- [ ] `CameraUiState.Failure` shows a polished, accessible, retryable failure state instead of only top-center plain text.
- [ ] `TestIdentifyModule.kt` is gone and deterministic androidTests use local `@BindValue` fake identifiers.
- [ ] `LowConfidenceFlowTest` covers camera -> low-confidence picker -> picked species -> result -> recommendation.
- [ ] `PermissionDeniedFlowTest.openSettingsIntentFiresOnPermanentDenial` is no longer ignored and passes on GMD.
- [ ] `model_manifest.json` or adjacent model policy code supports per-class threshold overrides with tests.
- [ ] The Monstera androidTest fixture is a real CC-licensed photo with attribution and retrieval metadata.
- [ ] `OnDeviceModelRealInterpreterTest` includes one accuracy-bearing assertion tied to the real Monstera fixture.
- [ ] `docs/ROADMAP.md` exists and gives sprint-review a current roadmap snapshot.
- [ ] `./gradlew.bat --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` passes.
- [ ] `bash scripts/check-stub-isolation.sh` passes.
- [ ] `./gradlew.bat --no-daemon pixel6Api34DebugAndroidTest` passes with no ignored `PermissionDeniedFlowTest`.
- [ ] `pwsh ./scripts/integration-flow.ps1 -BuildOnly` passes.
- [ ] `pwsh ./scripts/integration-flow.ps1` passes on a booted emulator.
