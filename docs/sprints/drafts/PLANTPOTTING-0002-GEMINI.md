# PLANTPOTTING-0002 — Fix sprint for PLANTPOTTING-0001 review bugs

**Status:** planned
**Sprint window:** ~3-5 days
**Intent:** Close high-severity bugs and UX gaps identified in the PLANTPOTTING-0001 review to ensure the baseline flow is robust before the on-device ML implementation (PLANTPOTTING-0003).

---

## 1. Goals

1.  **Permission Recovery:** Ensure the app correctly picks up camera permission when the user returns from System Settings (Android 14+).
2.  **UI Polishing:** Fix the shutter button glyph ('C' -> Icon) and add basic loading feedback during CameraX initialization.
3.  **Tooling Robustness:** Fix the integration script to find `adb` via `ANDROID_HOME` and enforce device-driven manifest checks.
4.  **Verification:** Land deferred tests and add new regression tests for all reported bugs.

---

## 2. Tasks

### Phase 1: Must-Land (High Priority)

#### Bug 4 — Permission state stale after Settings round-trip
- [ ] **1.1 (test)** Add an instrumentation test `PermissionSettingsRecoveryTest` that:
    - Sets up `PermissionScreenHost` with a mock/fake `CameraPermissionGuard`.
    - Simulates transition to Settings (using `Intents.intending`).
    - Grants permission via `pm grant` or a test hook.
    - Resumes the activity and asserts navigation to `camera`.
- [ ] **1.2** Implement `LifecycleEventObserver` in `PermissionScreenHost` (in `permission/PermissionScreen.kt`):
    - Observe `ON_RESUME`.
    - On resume, re-read `guard.isGranted()`.
    - If `isGranted()` is true, set `permanentlyDenied = false` and trigger `onGranted()`.

#### Bug 1 — Shutter renders literal 'C'
- [ ] **2.1 (test)** Land the deferred `CameraScreenSmokeTest` (§6.7 in PLANTPOTTING-0001.md):
    - Assert shutter is visible and enabled.
    - Assert no `Text` with value "C" is present in the shutter button.
- [ ] **2.2** Refactor `CameraScreen.kt`:
    - Replace `Button` with `FloatingActionButton`.
    - Replace the internal `Text` with `Icon(Icons.Default.CameraAlt, contentDescription = stringResource(R.string.camera_shutter_label))`.
    - Remove the `.first().toString()` hack.

#### Bug 3 — Integration script adb pathing & manifest gap
- [ ] **3.1** Update `scripts/integration-flow.ps1`:
    - If `Get-Command adb` fails, search `$env:ANDROID_HOME\platform-tools\adb.exe` and `$env:ANDROID_SDK_ROOT\platform-tools\adb.exe`.
    - If `adb` is still not found, `throw` an error instead of silently skipping (hard-fail).
    - If a device is attached, ensure `ui-hierarchy.xml` is parsed to add `archetype-name` and `recipe-row-count` to the manifest.
- [ ] **3.2** Update `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`:
    - Add `archetype-name=Aroid Chunky`.
    - Add `recipe-row-count=5`.
- [ ] **3.3** Implement distinct manifest targets:
    - Manifest should include `target=device` or `target=build-only`.
    - `expected-artifacts/PLANTPOTTING-0001.txt` should contain the common lines, with a separate section or a second file for device-required lines. *Decision: Single file, script fails if device lines are missing when a device is connected.*

### Phase 2: Should-Land (Medium Priority)

#### Bug 2 — Transient camera preview letterboxing
- [ ] **4.1** Review `bindCameraUseCases` placement in `CameraScreen.kt`:
    - Verify `AndroidView` factory logic and `previewView.surfaceProvider` attachment.
- [ ] **4.2 (test)** Add a Compose-UI test `CameraPreviewStabilityTest`:
    - Navigate `camera` -> `result` -> `Retake` -> `camera`.
    - Assert `PreviewView` measured size is > 80% of parent `Box` size.

#### UX 1 — Shutter loading state
- [ ] **5.1** Gate `enabled` state of the shutter button on `imageCapture != null` in `CameraScreen.kt`.
- [ ] **5.2** Show a `CircularProgressIndicator` overlay or replace the shutter icon with a spinner while `imageCapture == null` (initializing).

### Phase 3: Optional / Backlog

#### UX 2 — Refactor stub badge
- [ ] **6.1** Refactor `ResultScreen.kt` to read from `IdentificationResult.source`.
- [ ] **6.2** Display "Stub identifier" only if source is `STUB_DETERMINISTIC` or `STUB_RANDOM`.

---

## 3. Out of Scope

- The on-device ML model implementation (reserved for PLANTPOTTING-0003).
- Full `UiAutomator` drive for `PermissionDeniedFlowTest` (deferred).
- New dependencies.

---

## 4. Acceptance Criteria

- [ ] `PLANTPOTTING-0001` ledger status moves from `in-progress` to `done`.
- [ ] `scripts/integration-flow.ps1` runs to completion on a machine with `adb` (via `ANDROID_HOME`) and a connected device, passing the updated manifest diff.
- [ ] All new and existing tests pass (`./gradlew test connectedDebugAndroidTest`).
- [ ] Manual verification:
    - Launch app -> Deny -> Settings -> Grant -> Back -> App navigates to camera immediately.
    - Camera screen shows a camera icon instead of 'C'.
    - Shutter button is disabled until the preview is ready.
- [ ] `docs/sprints/results/PLANTPOTTING-0002.md` created with verification evidence.

---

## 5. Risks

- **Emulator vs Path:** `ANDROID_HOME` might not be set on all environments; script should provide helpful error if both `adb` in PATH and `ANDROID_HOME` are missing.
- **Race conditions in Lifecycle:** Ensure `ON_RESUME` doesn't trigger multiple `onGranted()` calls if already in `Granted` state.
