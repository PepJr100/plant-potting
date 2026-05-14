# PLANTPOTTING-0002 - Review fix sprint for PLANTPOTTING-0001

**Status:** draft  
**Sprint type:** fix sprint  
**Source review:** `docs/sprints/feedback/PLANTPOTTING-0001/feedback.md`  
**Primary goal:** move `PLANTPOTTING-0001` from `in-progress` to `done` by closing the review bugs that block the documented section 2.1 flow.

## Intent

PLANTPOTTING-0002 is a narrow stabilization sprint. It does not add the real plant model. It closes the high-signal issues found in the PLANTPOTTING-0001 review: stale permission state after returning from Android Settings, the literal `C` shutter glyph, and an integration script that can pass while silently skipping device coverage.

The sprint is done only when the original PLANTPOTTING-0001 flow is clean on the Pixel 6 API 34 Gradle Managed Device, a Settings round-trip picks up a newly granted camera permission on resume, and the camera shutter no longer renders text.

## Scope Boundaries

- [ ] Keep PLANTPOTTING-0002 scoped to review fixes from `docs/sprints/feedback/PLANTPOTTING-0001/feedback.md`.
- [ ] Do not add new third-party dependencies.
- [ ] Do not implement the on-device ML model; that belongs to PLANTPOTTING-0003.
- [ ] Do not re-enable `PermissionDeniedFlowTest` by driving the platform permission dialog with UiAutomator in this sprint.
- [ ] Keep the existing `PlantIdentifier` seam intact and avoid production references to `StubPlantIdentifier` outside `identify/`.
- [ ] Leave optional physical-device evidence as optional unless a physical Android device is available during execution.

## Must Land

### Bug 4 - Permission state stale after Settings round-trip

Current hotspot: `app/src/main/java/com/darkfactory/plantpotting/permission/PermissionScreen.kt:148-154` derives `PermissionUiState` from a plain `guard.isGranted()` call plus saved local flags. Returning from Settings does not invalidate composition, so `permanentlyDenied` can stay true after the platform permission has been granted.

- [ ] Add an instrumentation test that starts from a permanently denied permission UI state, stubs `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`, grants `android.permission.CAMERA` while the app is backgrounded or before resume is dispatched, resumes `MainActivity`, and asserts navigation reaches the camera screen.
- [ ] Add a focused Compose or instrumentation assertion that the local permanently denied state is cleared when `guard.isGranted()` reports true on `Lifecycle.Event.ON_RESUME`.
- [ ] In `PermissionScreenHost`, store the current platform grant result in Compose state, initialized from `guard.isGranted()`.
- [ ] In `PermissionScreenHost`, register a `LifecycleEventObserver` against `LocalLifecycleOwner.current` and re-read `guard.isGranted()` on `Lifecycle.Event.ON_RESUME`.
- [ ] In the `ON_RESUME` observer, set the platform-granted state and clear `permanentlyDenied = false` when the permission is now granted.
- [ ] Keep `onGranted()` emission idempotent so resume-driven state refresh does not emit duplicate navigation events that destabilize the nav stack.
- [ ] Remove or update the stale KDoc in `PermissionScreenHost` that currently says recomposition is enough to re-read permission state.
- [ ] Run the new Settings round-trip test on `pixel6Api34DebugAndroidTest` and record the command/result in the PLANTPOTTING-0002 results note if a results file is created.

### Bug 1 - Shutter renders literal `C`

Current hotspot: `app/src/main/java/com/darkfactory/plantpotting/camera/CameraScreen.kt:118-131` renders a `Button` whose content is `Text(stringResource(R.string.camera_shutter_label).first().toString())`.

- [ ] Add or restore `CameraScreenSmokeTest` from PLANTPOTTING-0001 section 6.7 before changing the UI.
- [ ] In `CameraScreenSmokeTest`, assert the shutter node exists, is enabled once a fake bound `ImageCapture` is available, and uses `camera_shutter_label` as its content description.
- [ ] In `CameraScreenSmokeTest`, assert the shutter text does not contain the literal `C` or any one-character placeholder label.
- [ ] Replace the shutter `Button` in `CameraScreen.kt:118-131` with a `FloatingActionButton`.
- [ ] Render `Icon(Icons.Default.CameraAlt, contentDescription = stringResource(R.string.camera_shutter_label))` inside the FAB.
- [ ] Preserve the existing `CameraScreenTags.SHUTTER` test tag on the clickable shutter affordance.
- [ ] Keep the shutter at the bottom center with the existing 72 dp visual target unless the Material FAB default forces a minor size adjustment that tests verify.
- [ ] Run `CameraScreenSmokeTest` and the existing camera/navigation tests after the replacement.

### Bug 3 - Integration script skips device coverage when adb is not on PATH

Current hotspot: `scripts/integration-flow.ps1:56-65` only checks `Get-Command adb`; it misses `$env:ANDROID_HOME\platform-tools\adb.exe` and `$env:ANDROID_SDK_ROOT\platform-tools\adb.exe`.

- [ ] Add a script-level test or documented manual repro that runs `scripts/integration-flow.ps1` with `adb` removed from `PATH` while `ANDROID_HOME` or `ANDROID_SDK_ROOT` points to an SDK containing `platform-tools\adb.exe`.
- [ ] Add an adb resolver function to `scripts/integration-flow.ps1` that checks `Get-Command adb`, then `$env:ANDROID_HOME\platform-tools\adb.exe`, then `$env:ANDROID_SDK_ROOT\platform-tools\adb.exe`.
- [ ] Use the resolved adb executable path for every adb invocation instead of the bare `adb` command.
- [ ] Decide and document the manifest policy: hard-fail when no device is attached by default, with an explicit opt-in build-only mode if maintainers still need that path.
- [ ] If build-only mode remains, split expected manifests into distinct build-only and device-aware targets so a build-only run cannot satisfy a device-aware acceptance gate.
- [ ] Update `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` so the default expected manifest requires device-aware evidence lines, including `archetype-name=Aroid Chunky` and `recipe-row-count=5`.
- [ ] Extend `scripts/integration-flow.ps1` to capture or derive the device-aware manifest lines from the driven flow, not from hard-coded assumptions.
- [ ] Ensure the script fails loudly when a device-aware run cannot find adb, cannot find a connected `device`, cannot install the APK, or cannot produce the required UI hierarchy evidence.
- [ ] Run the script once with adb resolved from the SDK fallback path and confirm the expected manifest diff fails before the device evidence is present and passes after it is present.

## Should Land

### Bug 2 - Transient camera preview letterboxing after Retake

This was not reproducible in the human replay, so the target is investigation plus a guard test rather than a speculative redesign. Current relevant code binds CameraX inside the `AndroidView` factory at `CameraScreen.kt:74-85`, with `bindCameraUseCases` defined at `CameraScreen.kt:157`.

- [ ] Review `CameraScreen.kt` for whether CameraX binding should move from the `AndroidView` factory into an effect keyed by `lifecycleOwner` and `previewView`.
- [ ] Add a Compose UI test that navigates camera -> result -> recommendation -> Retake -> camera and waits for the preview node to re-enter the hierarchy.
- [ ] In that test, assert `CameraScreenTags.PREVIEW` measures to more than 80 percent of the parent width and more than 80 percent of the parent height after Retake.
- [ ] If the test exposes a real layout or rebinding bug, adjust `CameraScreen` binding placement and keep the fix scoped to the preview lifecycle.
- [ ] If the test stays green without production changes, record Bug 2 as investigated with the test as the regression guard.

### UX 1 - Camera bind-window loading state

Current behavior: first paint can be black for a few seconds and the shutter is visible while `imageCapture == null`; `CameraScreen.kt:119-123` silently returns on early tap.

- [ ] Add a Compose UI test for the pre-bind state where `imageCapture == null`: shutter is disabled and a progress indicator overlay is visible.
- [ ] Add a Compose UI test for the post-bind state: the progress overlay disappears and the shutter becomes enabled when `onBound` supplies `ImageCapture`.
- [ ] Change shutter enablement from only `state is Idle || state is Failure` to also require `imageCapture != null`.
- [ ] Show a `CircularProgressIndicator` overlay while the screen is otherwise idle but CameraX has not completed binding.
- [ ] Ensure capture and identify loading states still use the existing in-flight overlay and do not fight with the bind-window overlay.
- [ ] Verify a premature shutter tap cannot be swallowed silently because the shutter is disabled until binding completes.

## Optional Backlog

### UX 2 - Source-driven result badge

This is useful cleanup, but it can wait until the real model sprint if time is tight.

- [ ] Review how `IdentificationResult.source` is preserved or discarded between `CameraViewModel`, navigation to `ResultScreen`, and `ResultViewModel`.
- [ ] Add a ViewModel or UI test proving `IdSource.STUB_DETERMINISTIC` displays the current `Stub identifier - replace in a later sprint` badge.
- [ ] Add a ViewModel or UI test proving `IdSource.ON_DEVICE_MODEL` does not display the stub badge and instead displays a source-appropriate label.
- [ ] Refactor `ResultScreen` or its state source so the badge is driven by `IdentificationResult.source`, not by a hard-coded permanent stub label.
- [ ] Defer the implementation if preserving `IdentificationResult.source` requires navigation or persistence changes larger than this fix sprint.

## Sequencing

- [ ] Start with Bug 4 because it breaks the documented recovery path and touches navigation behavior.
- [ ] Land the Bug 4 failing instrumentation test before changing `PermissionScreenHost`.
- [ ] Land the Bug 4 lifecycle observer fix and verify the Settings round-trip on Android 14+.
- [ ] Land `CameraScreenSmokeTest` before replacing the shutter UI.
- [ ] Replace the shutter with the Material FAB and icon after the smoke test fails for the current `C` glyph.
- [ ] Harden the camera bind-window state after the shutter affordance test is in place.
- [ ] Fix the integration script after the app-level tests are green so the script can be used as the final evidence gate.
- [ ] Investigate Bug 2 before final acceptance, but do not let a non-reproducible transient force a broad camera refactor.
- [ ] Update docs and ledger status only after tests and integration evidence are green.

## Risks

### Settings round-trip tests can be brittle

- [ ] Prefer stubbing the Settings intent and manipulating permission state with test APIs over full system Settings UI driving.
- [ ] Keep UiAutomator dialog driving out of scope for this sprint unless the chosen test path cannot simulate resume accurately.
- [ ] If Android permission mutation is blocked in the test environment, document the blocker and add the closest deterministic lifecycle test around `PermissionScreenHost`.

### CameraX is hard to fake cleanly in Compose tests

- [ ] Keep tests focused on Compose state, semantics, and preview layout nodes rather than real camera frames.
- [ ] Use existing test hooks such as `CameraScreenTags` and `CameraScreenTestRegistry` where they are already present.
- [ ] Avoid adding dependencies just to fake CameraX; prefer small local seams if needed.

### Integration evidence can drift between build-only and device-aware runs

- [ ] Make the chosen script mode visible in the manifest, for example `manifest-mode=device-aware`.
- [ ] Require the PLANTPOTTING-0001 acceptance gate to use the device-aware manifest.
- [ ] Keep any build-only manifest explicitly labeled as non-acceptance evidence.

### Over-expanding the sprint

- [ ] Treat Bug 4, Bug 1, Bug 3, `CameraScreenSmokeTest`, and UX 1 as the required work.
- [ ] Treat Bug 2 as investigation plus guard test unless reproduced.
- [ ] Treat UX 2 as optional backlog unless the source state is already available with a small change.

## Acceptance Criteria

- [ ] `PermissionScreenHost` re-checks `CameraPermissionGuard.isGranted()` on `Lifecycle.Event.ON_RESUME`.
- [ ] Returning from Android Settings after granting camera permission navigates to the camera screen without killing and relaunching the app.
- [ ] The local `permanentlyDenied` flag is cleared when the platform reports camera permission granted.
- [ ] The camera shutter renders a `FloatingActionButton` with `Icons.Default.CameraAlt`.
- [ ] `camera_shutter_label` is used as the shutter content description, not rendered as visible one-character text.
- [ ] `CameraScreenSmokeTest` exists and is green.
- [ ] The camera bind window disables the shutter until `imageCapture != null`.
- [ ] The camera bind window shows a `CircularProgressIndicator` overlay.
- [ ] The Retake re-entry preview-size test exists and is green, or Bug 2 is documented as non-reproducible with the exact test coverage that was added.
- [ ] `scripts/integration-flow.ps1` resolves adb from PATH, `ANDROID_HOME`, or `ANDROID_SDK_ROOT`.
- [ ] The integration script no longer passes device-aware acceptance when adb or a connected device is missing.
- [ ] `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` requires device-aware evidence lines including `archetype-name=Aroid Chunky` and `recipe-row-count=5`.
- [ ] The section 2.1 flow from `docs/sprints/PLANTPOTTING-0001.md` is clean on the Pixel 6 API 34 Gradle Managed Device with no visible `C` glyph.
- [ ] All new bug-fix tests run green in CI.
- [ ] Existing `assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking pixel6Api34DebugAndroidTest` gates remain green.
- [ ] `docs/sprints/ledger.yaml` moves `PLANTPOTTING-0001` from `in-progress` to `done` only after the above criteria are met.
- [ ] `docs/sprints/ledger.yaml` records PLANTPOTTING-0002 as complete only after this fix sprint's evidence is captured.
