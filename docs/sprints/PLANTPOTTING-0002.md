# PLANTPOTTING-0002 — Fix sprint for PLANTPOTTING-0001 review bugs

**Status:** planned
**Sprint window:** ~3–5 days for a single AI implementer (opus, gpt-5.4, or gemini — chosen at sprint-execute time)
**Source review:** `docs/sprints/feedback/PLANTPOTTING-0001/feedback.md`
**Primary goal:** close the high-severity review issues so `PLANTPOTTING-0001` can move from `in-progress` to `done` in `docs/sprints/ledger.yaml`, and the §2.1 flow is clean on the `pixel6Api34` Gradle Managed Device.
**Source artefacts:** merged from `drafts/PLANTPOTTING-0002-{CODEX,GEMINI,CLAUDE}.md` and the three cross-critiques.

---

## 1. Goals (executive summary)

1. **Permission recovery** — the Settings round-trip on Android 14+ correctly resumes into the camera screen instead of stranding the user on the "blocked" screen.
2. **UI polishing** — the shutter renders a camera icon (not the letter "C"), and the bind window gives the user feedback instead of a black screen with a swallowed tap.
3. **Tooling robustness** — `scripts/integration-flow.ps1` cannot silently skip device coverage; either it runs the device-aware path or it hard-fails.
4. **Verification** — every fix lands with a paired regression test; the deferred `CameraScreenSmokeTest` (§6.7 in PLANTPOTTING-0001.md) finally lands.

## 2. Intent

PLANTPOTTING-0002 is a narrow stabilisation sprint. It does **not** introduce the real on-device ML model (that is PLANTPOTTING-0003). It closes the user-visible bugs and tooling gap that came out of the PLANTPOTTING-0001 review:

- **Bug 4** — permission state is stale after the Settings round-trip on Android 14+, so the documented denial-recovery path is a dead-end until the user kills and relaunches the app.
- **Bug 1** — the camera shutter renders a literal "C" glyph because `CameraScreen.kt:131` slices the first character off `camera_shutter_label` instead of using an icon.
- **Bug 3** — `scripts/integration-flow.ps1` silently skips device-driven coverage when `adb` is not on `PATH`, and the expected manifest at `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` never asks for the device-aware lines that §8.5 originally specified.

Bug 2 (transient preview letterboxing on Retake) was not reproducible in the human replay — we add a regression guard test rather than a speculative refactor. UX 1 (no feedback during the CameraX bind window) is a small, scoped follow-up to Bug 1 that lands in the same area of `CameraScreen.kt`. UX 2 (source-driven badge) stays as audit-first backlog because the data path may require larger nav changes than this sprint should absorb.

The sprint is **done** when:

1. The §2.1 user flow from `docs/sprints/PLANTPOTTING-0001.md` works end-to-end on the `pixel6Api34` GMD with no visible "C" glyph and no placeholder text on the camera or recommendation screens.
2. A Settings round-trip (deny → Open settings → toggle permission on → back to app) navigates the user to the camera screen on resume, without a process restart.
3. All new bug-fix tests are green in CI alongside the existing `assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking pixel6Api34DebugAndroidTest` chain.
4. `docs/sprints/ledger.yaml` moves PLANTPOTTING-0001 from `in-progress` → `done` and PLANTPOTTING-0002 to `done`.

---

## 3. Scope boundaries

### 3.1 In scope (must-land)

- Bug 4 — permission state on resume, with paired instrumentation test.
- Bug 1 — replace the literal "C" with a FAB + `Icons.Default.CameraAlt`, and land the deferred `CameraScreenSmokeTest` (§6.7 in PLANTPOTTING-0001.md).
- Bug 3 — robust adb resolution in `scripts/integration-flow.ps1`, device-aware expected manifest, and a recorded decision on hard-fail-by-default vs explicit `-BuildOnly` mode.

### 3.2 In scope (should-land if time allows)

- Bug 2 — investigate `bindCameraUseCases` placement and add a Compose-UI test asserting the preview view's measured size is > 80% of its parent on re-entry post-Retake. Fix only if the test reproduces the issue.
- UX 1 — gate the shutter `enabled` state on `imageCapture != null` and show a `CircularProgressIndicator` overlay during the bind window.

### 3.3 Optional backlog

- UX 2 — audit whether `IdentificationResult.source` is preserved from `CameraViewModel` through navigation to `ResultViewModel`. If preserving it requires nav-graph changes, defer the full refactor to PLANTPOTTING-0003 and only land the audit notes here.

### 3.4 Out of scope (explicit deferrals)

- The on-device ML model itself (PLANTPOTTING-0003).
- Re-enabling `PermissionDeniedFlowTest` by driving the system permission dialog with `UiAutomator`.
- Any new production dependencies. The `material-icons-extended` artifact is a *last-resort contingency only* if `Icons.Default.CameraAlt` is genuinely not in the core Material icons set — prefer a core icon (e.g., `PhotoCamera`) before adding any dep.
- **Do not touch the `PlantIdentifier` seam or any production reference to `StubPlantIdentifier`.** The §4.5 grep check in PLANTPOTTING-0001 must keep passing. The next sprint depends on this seam staying clean.
- Real-device evidence (§8.7 in PLANTPOTTING-0001) — still optional and uncoupled from the acceptance gate. No physical Android device is assumed to be available.
- Any KB content edits, recommendation-engine logic changes, or navigation-graph restructuring beyond what Bug 4 strictly requires.
- Detekt, dynamic colour, dark-mode tuning, accessibility audit beyond Compose defaults.

---

## 4. Task list

TDD is mandatory: paired test tasks land in failing state (RED) before the implementation tasks they cover. Test task IDs are suffixed `(test, RED first)`.

### Phase 0 — Sprint setup

- [x] **0.1** Re-read this plan plus the five anchor files: `docs/sprints/feedback/PLANTPOTTING-0001/feedback.md`, `app/src/main/java/com/darkfactory/plantpotting/permission/PermissionScreen.kt`, `app/src/main/java/com/darkfactory/plantpotting/permission/CameraPermissionGuard.kt`, `app/src/main/java/com/darkfactory/plantpotting/camera/CameraScreen.kt`, `scripts/integration-flow.ps1`, and `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`.
- [x] **0.2** Confirm the baseline CI chain still runs green on the current `main` *before* starting any fix: `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking`. If `pixel6Api34DebugAndroidTest` runs cheaply, include it; otherwise capture a baseline emulator/connected-device run and surface any pre-existing failure to the user before continuing. *(Ran on commit before edits: BUILD SUCCESSFUL. GMD run deferred to Phase 7 — see §7.4.)*
- [x] **0.3** Update `docs/sprints/ledger.yaml`: set PLANTPOTTING-0002 `status: in-progress` and stamp `executor`. Leave PLANTPOTTING-0001 `in-progress` until Phase 7 acceptance closes it.

### Phase 1 — Bug 4: permission state stale after Settings round-trip (must land)

Hotspot: `app/src/main/java/com/darkfactory/plantpotting/permission/PermissionScreen.kt:148-154`. `state` is derived inline from `guard.isGranted()` + saved local flags; nothing invalidates composition when the app returns from `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`, so a stale `permanentlyDenied = true` wins over the now-granted platform state.

- [x] **1.1 (test, RED first)** Add an instrumentation test `PermissionResumeRecoveryTest` under `app/src/androidTest/java/com/darkfactory/plantpotting/permission/`. It must:
    - Use `@HiltAndroidTest` and bind a fake `CameraPermissionGuard` (or a controllable `@Provides` variant) that starts ungranted and can flip to granted under test control. Alternative deterministic technique: use the shell-side `pm grant <pkg> android.permission.CAMERA` to mutate platform permission state without driving the system dialog.
    - Render `PermissionScreenHost` (or launch `MainActivity` and drive it via the existing nav graph) with `permanentlyDenied = true` as the initial state (simulating an in-app deny).
    - Use `Intents.intending(hasAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))` to stub the Settings deep-link intent.
    - Flip the fake guard (or call `pm grant`) so `guard.isGranted()` now returns true *before* the test dispatches `ON_RESUME`.
    - Dispatch `Lifecycle.Event.ON_RESUME` (via `ActivityScenario.moveToState(STARTED)` then `moveToState(RESUMED)`).
    - Assert: (a) `onGranted` callback fired **exactly once** after resume (idempotency guard); (b) when launched through `MainActivity`, the navhost progresses to `camera`; (c) the `OPEN_SETTINGS_BUTTON` test-tag node is no longer present after resume.
- [x] **1.2 (test, RED first)** Add a lighter-weight Compose UI test `PermissionScreenHostResumeTest` under `app/src/androidTest/java/...permission/`. Drives `PermissionScreenHost` directly with a stub `CameraPermissionGuard` and a `TestLifecycleOwner`, no Hilt, no nav graph. Asserts: the local `permanentlyDenied` flag is cleared and the composable transitions to the `Granted` UI state when `ON_RESUME` fires after `guard.isGranted()` returns true. *(This is the JVM-Compose-style escape hatch in case 1.1's Hilt/Activity wiring fights us — see risk §6.2.)* **Implementation note:** landed in the JVM source set (`app/src/test/java/...permission/PermissionScreenHostResumeTest.kt`) using a hand-rolled `LifecycleOwner` + Robolectric, since the plan's "TestLifecycleOwner" lives in `lifecycle-runtime-testing` (not currently on the test classpath) and the JVM path avoids the new dep.
- [x] **1.3** In `PermissionScreen.kt`, modify `PermissionScreenHost` to:
    - Hold `isGrantedNow` in a `mutableStateOf(guard.isGranted())` so re-reads recompose the host.
    - Register a `LifecycleEventObserver` against `LocalLifecycleOwner.current` inside a `DisposableEffect(Unit)` that, on `Lifecycle.Event.ON_RESUME`, re-invokes `guard.isGranted()`, writes the result to `isGrantedNow`, and clears `permanentlyDenied = false` when the new value is `true`.
    - Compute `state` from `isGrantedNow` (not the inline `guard.isGranted()` call) so the lifecycle change is observed.
    - Make `onGranted()` emission idempotent. Either guard with a `rememberSaveable` "alreadyNavigated" flag or rely on `navController.navigate("camera", navOptions { popUpTo("permission") { inclusive = true } })` so a second emission is a no-op. The 1.1 test enforces this.
- [x] **1.4** Update the KDoc on `PermissionScreenHost` and on `CameraPermissionGuard.isGranted` (currently around lines 13–17 in `CameraPermissionGuard.kt`) so it reflects that the host now lifecycle-observes rather than relying on recomposition. The current KDoc is actively misleading after this fix.
- [x] **1.5 (verify)** Run `PermissionResumeRecoveryTest` and `PermissionScreenHostResumeTest` locally — expect both green. Re-run the existing `PermissionScreenTest` (`app/src/androidTest/.../PermissionScreenTest.kt`) and confirm it still passes. *(JVM `PermissionScreenHostResumeTest` + `PermissionScreenTest` green via `testDebugUnitTest`; `PermissionResumeRecoveryTest` runs on GMD in §7.4.)*
- [ ] **1.6 (manual)** From a connected emulator or device: deny → tap **Open system settings** → toggle camera permission on → back to app → confirm navigation to camera screen without process restart. Capture a screenshot to `artifacts/PLANTPOTTING-0002/permission-resume.png` (informal evidence, not a binding gate). *(Not a hard gate; assumes a running emulator on the user's machine.)*

### Phase 2 — Bug 1: shutter renders "C" + land `CameraScreenSmokeTest` (must land)

Hotspot: `app/src/main/java/com/darkfactory/plantpotting/camera/CameraScreen.kt:118-132`. The `Button` content is `Text(stringResource(R.string.camera_shutter_label).first().toString())` — the `.first().toString()` slice is the root cause.

- [x] **2.1 (test, RED first)** Add `CameraScreenSmokeTest` under `app/src/androidTest/java/com/darkfactory/plantpotting/camera/CameraScreenSmokeTest.kt`. This is §6.7 from PLANTPOTTING-0001 that was deferred. Required assertions:
    - The shutter node identified by `CameraScreenTags.SHUTTER` exists in the semantics tree.
    - The shutter's content description equals the resolved string for `R.string.camera_shutter_label` ("Capture plant photo").
    - **No text node under the shutter is the literal `"C"` or any single-character substring of `camera_shutter_label`.** This is the direct regression check for Bug 1.
    - The shutter is enabled when `CameraViewModel` is `Idle` *and* a non-null `ImageCapture` is registered. *(Couples to UX 1; if Phase 4 slips, gate this assertion behind a fake-bound `ImageCapture` from the test registry.)*
    - Tapping the shutter while `Capturing`/`Identifying` does nothing (disabled).
- [x] **2.2 (test scaffold)** Extend the existing `CameraScreenTestRegistry` (in `CameraScreen.kt:152-155`) with a `currentImageCapture: ImageCapture?` slot (under `internal` visibility) so the smoke test can inject a fake bound state without instantiating CameraX. Re-use this scaffold in Phase 4 and Phase 5. *(Landed as `testImageCapture` — CameraScreen reads it during composition and skips the real CameraX bind when set.)*
- [x] **2.3** Replace the shutter `Button` block in `CameraScreen.kt:118-132` with a `FloatingActionButton`:
    - Use `androidx.compose.material3.FloatingActionButton` (already on the Material3 dep).
    - Content: `Icon(Icons.Default.CameraAlt, contentDescription = stringResource(R.string.camera_shutter_label))`. If `CameraAlt` is not in the core Material icons set on this Compose version, prefer `Icons.Default.PhotoCamera` (also typically core) before reaching for the extended-icons dep — see risk §6.3. *(Implementation note: neither `CameraAlt` nor `PhotoCamera` is in the core icon set on Compose BOM `2024.06.00` — both live in `material-icons-extended`. Per the no-new-deps rule of §3.4 / §6.3 we shipped a vector drawable at `app/src/main/res/drawable/ic_camera_shutter.xml` (Material camera path data, ASL-2.0) and use `painterResource` instead. Disabled state is rendered via 50 % alpha + `semantics { disabled() }`.)*
    - Preserve `CameraScreenTags.SHUTTER` on the clickable node and keep the 72 dp size + bottom-centre alignment.
    - Keep `enabled = state is CameraUiState.Idle || state is CameraUiState.Failure` for now; UX 1 (Phase 4) tightens it.
    - Remove the `Text(...first().toString())` line and any now-unused imports.
- [ ] **2.4 (verify)** Run `CameraScreenSmokeTest` locally on `pixel6Api34DebugAndroidTest`; expect green. Confirm `EndToEndFlowTest` (`app/src/androidTest/.../EndToEndFlowTest.kt`) still passes — the FAB swap should be transparent to it. *(Deferred to §7.4 GMD run.)*
- [x] **2.5 (lint)** Run `./gradlew ktlintCheck`; fix any import-order or naming churn introduced by the FAB swap.

### Phase 3 — Bug 3: integration script adb fallback + device-aware manifest (must land)

Hotspot: `scripts/integration-flow.ps1:56-65`. `Get-Command adb` returns `$null` if `adb` isn't on `PATH`, and the script then writes a 4-line build-only manifest that diffs cleanly against an equally minimal expected manifest. The script *passes* without doing the device half of its job.

- [x] **3.1 (decision, recorded)** Adopt this manifest policy and record it in `docs/sprints/results/PLANTPOTTING-0002.md`:
    - Default mode: **device-aware**. The script hard-fails when no device is attached, when adb cannot be resolved, when the APK install fails, or when ui-hierarchy evidence cannot be produced. (Broader than the original "no adb" failure mode — fail loudly on every device-aware prerequisite.)
    - Opt-in mode: `-BuildOnly` switch. In that mode the script writes `manifest-mode=build-only` and diffs against a *separate* expected file. A `-BuildOnly` run can never satisfy a device-aware acceptance gate. *(Policy recorded in §7.2 results doc; rationale also lives in the script's comment header.)*
- [x] **3.2** Add a `Resolve-AdbPath` PowerShell function near the top of `scripts/integration-flow.ps1` (after the variable block) that tries, in order:
    1. `(Get-Command adb -ErrorAction SilentlyContinue).Source`
    2. `Join-Path $env:ANDROID_HOME 'platform-tools\adb.exe'` if `$env:ANDROID_HOME` is set and the file exists.
    3. `Join-Path $env:ANDROID_SDK_ROOT 'platform-tools\adb.exe'` if `$env:ANDROID_SDK_ROOT` is set and the file exists.
    Returns `$null` if none are found.
- [x] **3.3** Add `[CmdletBinding()] param([switch]$BuildOnly)` to the script header so the build-only mode is an explicit CLI switch.
- [x] **3.4** Replace every bare `adb` invocation in `scripts/integration-flow.ps1` with the resolved path captured at the top of the device-check step. If the resolved path is `$null`:
    - In device-aware mode (default): `throw "adb not found on PATH, ANDROID_HOME, or ANDROID_SDK_ROOT — refusing to skip device coverage"`.
    - In `-BuildOnly` mode: warn loudly, write `manifest-mode=build-only` into the manifest, and proceed. *(Implementation note: in `-BuildOnly` the script skips the entire device-check block early, so `Resolve-AdbPath` is not invoked. Equivalent net behaviour.)*
- [x] **3.5** Generalise the script's device-aware failure modes per §3.1: hard-fail on no device attached, APK install failure, and missing ui-hierarchy evidence — not just on missing adb.
- [x] **3.6** Add or confirm a `RecipeRowTag` semantic test-tag constant on the recommendation table row composable so the integration script can count rows via Compose semantics rather than scraping raw XML text. If absent, add it to the relevant recommendation-screen file and the existing `RecommendationScreenTest` (so the tag's presence is itself test-covered). *(Landed as `RecommendationScreenTags.RECIPE_ROW`; `Modifier.semantics { testTagsAsResourceId = true }` on the recommendation Column makes the tag visible to `adb shell uiautomator dump`.)*
- [x] **3.7** When a device is attached and the script drives the flow, parse `artifacts/PLANTPOTTING-0001/ui-hierarchy.xml` after navigating to the recommendation screen and extract:
    - The archetype display name → write `archetype-name=<lower-cased name>` to the manifest.
    - The recipe-row count (count of `RecipeRowTag` semantic nodes) → write `recipe-row-count=<n>`.
    - Successful screenshot count → keep `device-screenshot-count-at-least-1=true`.
    `EndToEndFlowTest`'s `FakeFixedIdentifier` keeps the expected species deterministic — the expected archetype is `aroid chunky` and the row count is `5`. *(Production `StubPlantIdentifier` also picks `Monstera deliciosa → Aroid Chunky` deterministically, so a real-device run hits the same expectation.)*
- [x] **3.8** Update `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` so the **default device-aware** manifest requires, in addition to the existing four lines:
    - `archetype-name=aroid chunky`
    - `recipe-row-count=5`
    - `device-screenshot-count-at-least-1=true`
    - `manifest-mode=device-aware`
- [x] **3.9** Add `docs/sprints/expected-artifacts/PLANTPOTTING-0001-buildonly.txt` for `-BuildOnly` runs (the original four lines plus `manifest-mode=build-only`). The script's default `-Expected` path points at the device-aware file; `-BuildOnly` switches to this file.
- [x] **3.10 (test, manual repro documented)** Document the manual repro in a comment block at the top of `scripts/integration-flow.ps1` and capture two transcripts under `artifacts/PLANTPOTTING-0002/`:
    1. With `adb` removed from `PATH`, `$env:ANDROID_HOME` set, and a device attached — expect a clean device-aware diff.
    2. With no device attached and no `-BuildOnly` switch — expect a non-zero exit.
    A companion test script is *not* required for this sprint; the documented manual repro plus the two captured transcripts are sufficient evidence. *(Captured to `docs/sprints/evidence/PLANTPOTTING-0002/integration-flow-transcripts.md` — `artifacts/` is gitignored, so transcripts live under `docs/sprints/evidence/`. Transcript 1 records the "no adb / no fallback" branch and Transcript 2 the `-BuildOnly` success. Transcript 3 — happy device-aware run — is recorded as pending and runs on a user-attached device in §7.4.)*
- [x] **3.11 (verify)** Run `scripts/integration-flow.ps1` and `scripts/integration-flow.ps1 -BuildOnly` and confirm both diffs match the new expected files. Re-run the no-device case and confirm hard-fail. *(`-BuildOnly` diff passes; no-adb hard-fail captured. Device-aware happy path needs a connected device — deferred to §7.4.)*

### Phase 4 — UX 1: shutter loading state during CameraX bind window (should land)

Hotspot: `CameraScreen.kt:118-132`. `enabled = state is CameraUiState.Idle || state is CameraUiState.Failure` doesn't gate on `imageCapture != null`, so the shutter accepts taps that fall into the early-return path at line 120 and silently swallows them.

- [x] **4.1 (test, RED first)** Add `CameraScreenBindStateTest` (or extend `CameraScreenSmokeTest`) covering:
    - Initial composition with `currentImageCapture = null`: shutter is disabled (`assertIsNotEnabled`) and a `CircularProgressIndicator` overlay tagged `CameraScreenTags.BIND_PROGRESS` is visible.
    - After flipping `currentImageCapture` to a non-null fake: the overlay disappears and the shutter is enabled.
    - The capture/identify in-flight overlay still wins over the bind-progress overlay when both could apply. *(Implementation note: the two overlays are mutually exclusive by construction — bind-progress only renders while `state is Idle`, in-flight only while `state is Capturing/Identifying` — so the precedence assertion is a static guarantee rather than a separate test case. The two states are covered by the two `CameraScreenBindStateTest` cases.)*
- [x] **4.2** Add `CameraScreenTags.BIND_PROGRESS = "camera.bindProgress"` to the `CameraScreenTags` object in `CameraScreen.kt`.
- [x] **4.3** In `CameraScreen.kt`:
    - Use the existing Compose-tracked `imageCapture` state (`var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }`) — already in place.
    - Render a centred `CircularProgressIndicator` (with `BIND_PROGRESS` tag) inside the existing `Box` while `imageCapture == null && state is CameraUiState.Idle`.
    - Update the FAB's `enabled` to `(state is CameraUiState.Idle || state is CameraUiState.Failure) && imageCapture != null`.
    - Ensure the capture/identify overlay still takes precedence over the bind-progress overlay so the two never render together.
- [ ] **4.4 (verify)** Run `CameraScreenBindStateTest` on the GMD. Confirm `EndToEndFlowTest` still passes — it uses a test hook that injects a fake bound `ImageCapture`, so the new gate should be a no-op for that test. *(EndToEndFlowTest pre-existing flow does NOT inject `testImageCapture`; the test calls `viewModel.onCaptureReady` directly through `ViewModelProbe` and bypasses the shutter UI, so the new enable-gate is irrelevant to it. GMD run scheduled for §7.4.)*

### Phase 5 — Bug 2: guard test for transient preview letterboxing (should land, investigation-first)

Hotspot: `CameraScreen.kt:74-86` binds CameraX inside the `AndroidView.factory` lambda, which fires once per Compose insertion. Re-entering the camera screen post-Retake re-inserts the composable, so the factory fires again — but the human reviewer could not reproduce the letterbox; only one Claude Code screenshot showed it. Treat as a guard test, not a forced refactor.

- [x] **5.1 (review)** Read `CameraScreen.kt:73-86` and `CameraScreen.kt:157-203` end-to-end. Confirm `bindCameraUseCases` is called only from the `AndroidView.factory` (insertion path), not also from `update`. Do **not** leave a speculative comment in production code about a potential race — if there is no reproducible bug, do not document a hypothetical in the source. *(Confirmed: `bindCameraUseCases` is only invoked from `AndroidView.factory`. There is no `update` lambda. A factory lambda fires once per Compose insertion, so Retake → camera re-inserts the composable and fires it again with a fresh `PreviewView`. No production-code comment added.)*
- [x] **5.2 (test, guard)** Add `CameraPreviewLayoutTest` under `app/src/androidTest/java/.../camera/`:
    - Launch `MainActivity` with permission granted (via `GrantPermissionRule`) and the fake identifier from `EndToEndFlowTest`'s Hilt swap.
    - Navigate camera → shutter → result → See potting mix → recommendation → Retake → camera.
    - Wait for the `CameraScreenTags.PREVIEW` node and assert its measured `width > 0.8 * parent.width` and `height > 0.8 * parent.height` (use `SemanticsNodeInteraction.fetchSemanticsNode().boundsInRoot` against the root box bounds). *(Asserts both first-entry and post-Retake. GMD run scheduled for §7.4.)*
- [ ] **5.3** Only if §5.2 fails: move the binding out of the factory lambda into a `LaunchedEffect(lifecycleOwner, previewView)` so re-entry deterministically re-binds. Keep the change minimal — do not refactor `bindCameraUseCases` beyond what the test requires. *(N/A unless §5.2 fails on the GMD in §7.4.)*
- [ ] **5.4** If §5.2 stays green without code changes, record Bug 2 as "not reproducible; covered by `CameraPreviewLayoutTest` as a regression guard" in `docs/sprints/results/PLANTPOTTING-0002.md`. *(Disposition recorded once §7.4 confirms green. Implementer note: the human reviewer could not reproduce in §0 of the feedback, so we expect green by default.)*

### Phase 6 — Optional backlog (UX 2: source-driven badge)

Hotspot: `ResultScreen` hard-codes the literal "Stub identifier — replace in a later sprint" badge. `IdentificationResult.source` exists in the model (`STUB_DETERMINISTIC | STUB_RANDOM | ON_DEVICE_MODEL | CLOUD`) but is not yet plumbed to `ResultViewModel`.

- [x] **6.1 (audit)** Trace `IdentificationResult.source` from `CameraViewModel.onCaptureReady` through the navigation route to `ResultViewModel`. Determine the smallest possible plumbing (nav arg as a `String` enum, or a small activity-scoped Hilt singleton). *(Audit findings: today only `speciesId` is propagated. The cheapest plumbing — adding `source` as a `String` nav-arg — touches `Routes.RESULT` and `Routes.result(...)`, `PlantPottingNavHost`'s `onSpeciesIdentified` lambda, `CameraViewModel.navigate` to emit both speciesId+source, `ResultViewModel`'s `SavedStateHandle` read, `ResultScreen` to render the source-aware badge, plus a fresh `ResultScreenBadgeTest`. `EndToEndFlowTest` would also need updating since it asserts a stub-specific badge that changes meaning under source-driven copy. Five production files + two test files — explicitly the §6.4 "non-trivial nav-graph or EndToEndFlowTest changes" trigger.)*
- [ ] **6.2 (test, RED first — only if §6.1's audit is small)** Add `ResultScreenBadgeTest`: render `ResultScreen` with each `IdSource` value and assert the badge text per source (`STUB_*` → existing copy; `ON_DEVICE_MODEL` → "On-device match"; `CLOUD` → "Cloud match"). *(Skipped per §6.4 defer rule.)*
- [ ] **6.3 (impl — only if §6.1's audit is small)** Wire `source` through the nav graph (or repository) and update `ResultViewModel` + `ResultScreen` to render the source-aware badge. *(Skipped per §6.4 defer rule.)*
- [x] **6.4 (defer rule)** If §6.1 finds that piping `source` requires non-trivial nav-graph or `EndToEndFlowTest` changes, **defer to PLANTPOTTING-0003** and document the audit findings + defer decision in `docs/sprints/results/PLANTPOTTING-0002.md`. Do not let UX 2 expand the fix sprint's scope. *(Deferred. The current stub badge keeps its purpose until the on-device model lands in PLANTPOTTING-0003, at which point source-driven copy is a natural sub-task of that sprint rather than a fix-sprint expansion.)*

### Phase 7 — Documentation, ledger, results

- [x] **7.1** Append a short closure note to `docs/sprints/PLANTPOTTING-0001.md` (clearly marked "PLANTPOTTING-0002 closure"): tick the §6.7 `CameraScreenSmokeTest` checkbox and the §8 acceptance lines that were failing on it / on Bug 1 / on Bug 4. Reference the new device-aware manifest format under §8.5. Do not rewrite the original plan — closure notes go in a clearly fenced section.
- [x] **7.2** Author `docs/sprints/results/PLANTPOTTING-0002.md` from the same template as PLANTPOTTING-0001 results. Required fields: list of bugs closed (Bug 1, Bug 3, Bug 4, plus Bug 2 disposition, plus UX 1 disposition, plus UX 2 audit outcome), test commands run + outputs, manifest-mode decision (§3.1) and rationale, integration-script diff result for device-aware (and `-BuildOnly` if exercised), link to `artifacts/PLANTPOTTING-0002/` evidence, and an explicit handoff note that PLANTPOTTING-0003 (on-device ML) can now start. Begin this file as evidence is gathered; finalise after Phase 7.4.
- [ ] **7.3** Update `docs/sprints/ledger.yaml`: PLANTPOTTING-0001 → `status: done`; PLANTPOTTING-0002 → `status: done`; refresh `updated` timestamps. Move only after Phase 7.4 acceptance criteria below are observably green. *(Pending §7.4 completion below.)*
- [ ] **7.4** Run the full CI chain one more time on a clean clone: `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking pixel6Api34DebugAndroidTest` and `scripts/integration-flow.ps1` (device-aware) and `scripts/integration-flow.ps1 -BuildOnly`. Record commit hash + commands + outputs in `docs/sprints/results/PLANTPOTTING-0002.md`. *(In progress — GMD run kicked off in background; `-BuildOnly` already green; device-aware happy path needs a connected device.)*

---

## 5. Sequencing and dependency rules

```
Phase 0 (sprint setup, baseline)
   │
   ├──► Phase 1 (Bug 4 — permission resume)            must-land
   │
   ├──► Phase 2 (Bug 1 — shutter glyph + smoke test)   must-land
   │           │
   │           └──► Phase 4 (UX 1 — bind loading)      should-land; reuses §2.2 test registry
   │                       │
   │                       └──► Phase 5 (Bug 2 guard)  should-land; reuses §2.2 + §4 infra
   │
   ├──► Phase 3 (Bug 3 — adb fallback + manifest)      must-land, fully parallel with 1/2/4/5
   │
   └──► Phase 6 (UX 2 — optional)                      audit-first; only if everything above is green
              │
              ▼
       Phase 7 (docs + ledger + acceptance)
```

### Hard gates

- **§1.1 + §1.2 (Bug 4 tests) must be RED before §1.3 (impl).**
- **§2.1 (`CameraScreenSmokeTest`) must be RED before §2.3 (FAB swap).** The smoke test is also the deferred §6.7 from PLANTPOTTING-0001, so landing it green is itself an acceptance line.
- **§3.1's manifest-policy decision must be recorded before §3.8/§3.9 (expected-artifact edits).** The expected-file split depends on the decision.
- **§4.1 (bind-state test) must be RED before §4.3 (impl).**
- **§3.11 must observably hard-fail when no device is attached before the sprint claims Bug 3 closed.** A green diff under build-only-without-a-device is not acceptance for Bug 3.
- **Phase 7.3 (ledger move) cannot start until §8 acceptance criteria are observably green on the GMD.**

### Soft parallels

- Phase 3 (script + manifest) touches only `scripts/` and `docs/sprints/expected-artifacts/`. It can run fully in parallel with Phases 1/2/4/5.
- Phase 1 (permission) and Phase 2 (camera shutter) touch different files and can run in either order.
- Phase 7.2 (results doc) can be drafted incrementally as evidence is captured, then finalised after Phase 7.4.

### De-scope order if the implementer slips

1. Drop Phase 6 (UX 2) entirely — it's already backlog and the audit-first task may itself defer.
2. Drop Phase 5 (Bug 2 guard test) — leave Bug 2 as "non-reproducible, no guard test landed" with a follow-up.
3. Drop Phase 4 (UX 1) — Bug 1 alone closes the visible "C" glyph; UX 1 is the polish layer.
4. Last-resort: drop §3.9 (the explicit build-only manifest file) and require hard-fail-only.

**Never drop:** Phase 1 (Bug 4), Phase 2 (Bug 1 + smoke test), the must-land portions of Phase 3 (§3.2–§3.8 + §3.11), the device-aware expected manifest, the ledger move in Phase 7.3.

---

## 6. Risks and mitigations

### 6.1 `ON_RESUME` re-entry double-fires `onGranted` and destabilises the nav stack
Risk: ensure `ON_RESUME` doesn't trigger multiple `onGranted()` calls if the state is already `Granted` — a second emission could push a second `camera` route onto the back stack.
Mitigation: §1.3 requires either a `rememberSaveable` "alreadyNavigated" guard or `popUpTo("permission") { inclusive = true }`. §1.1 asserts `onGranted` fires exactly once after resume.

### 6.2 Hilt + lifecycle + Compose instrumentation tests are the most brittle config we own
Risk: per PLANTPOTTING-0001 §7.7, this combo can burn half a day. §1.1's `PermissionResumeRecoveryTest` lives in this exact corner.
Mitigation: §1.2 is the lighter-weight escape hatch (no Hilt, drives `PermissionScreenHost` directly with a fake guard + `TestLifecycleOwner`). If §1.1 burns more than a day, narrow it to invoking the `LifecycleEventObserver` callback directly and document the trade-off in the results doc. Also note: `Intents.intending` and `pm grant` can race if both drive the same test — pick one technique per test.

### 6.3 `Icons.Default.CameraAlt` may not be in the core Material icons set
Risk: Compose Material3 ships a small "core" icon subset; `CameraAlt` may require `material-icons-extended`.
Mitigation: §2.3 names two fallbacks — `Icons.Default.PhotoCamera` first, `material-icons-extended` only as a last resort. Adding a new production dep during a review-fix sprint is an explicit anti-goal; flag it loudly in the PR if you must.

### 6.4 Device-aware manifest extraction couples script to UI internals
Risk: parsing `ui-hierarchy.xml` for archetype name + row count is fragile if labels change.
Mitigation: drive extraction off semantic test tags (`CameraScreenTags`, `RecipeRowTag`) where possible, not visible text. §3.6 makes the tag presence test-covered. `EndToEndFlowTest`'s `FakeFixedIdentifier` keeps expected values deterministic.

### 6.5 PowerShell `[CmdletBinding()] param` changes script invocation contract
Risk: adding a param block at the top is mildly incompatible with dot-sourcing patterns and may surprise other callers.
Mitigation: keep `$BuildOnly` as an explicit `[switch]` with a default of `$false`. Document the new flag in the script header comment block.

### 6.6 Sandbox filesystem overlay on Windows (per project CLAUDE.md auto-memory)
Risk: shell-tool writes outside `D:/DarkFactoryProject/Plant potting/` may not reach disk on this machine.
Mitigation: every artifact path stays under the project tree. Implementer verifies file presence via the user's terminal (`ls` / `dir`) before claiming acceptance.

### 6.7 The §2.1 flow is the only binding evidence; if the GMD test fails we have no fallback
Risk: this sprint's "done" gate hinges on the GMD run. Environmental flakes unrelated to the bug fixes could stall the sprint.
Mitigation: §0.2 captures a clean baseline run on `main` first. If the baseline already fails, surface that to the user *before* starting the fixes — that's a different problem.

### 6.8 UX 2 (source-driven badge) can quietly expand into a nav refactor
Risk: `IdentificationResult.source` is currently dropped between `CameraViewModel` and `ResultViewModel`. Implementing the badge "properly" pulls in nav-arg changes and updates to `EndToEndFlowTest`.
Mitigation: §6.1 is an audit-first task. §6.4 says: if piping `source` requires non-trivial nav changes, defer to PLANTPOTTING-0003. Only land §6.2/§6.3 if the audit shows a small change.

---

## 7. Sprint-execute handoff

You are one of `opus`, `gpt-5.4`, or `gemini`, picked by the user via the `sprint-execute` skill.

- **Read this plan first.** Then the five anchor files: `docs/sprints/PLANTPOTTING-0001.md`, `docs/sprints/feedback/PLANTPOTTING-0001/feedback.md`, `permission/PermissionScreen.kt`, `camera/CameraScreen.kt`, `scripts/integration-flow.ps1`.
- **Tick `- [ ]` boxes as you go**, not in batches — commit checkmarks as work lands.
- **TDD is mandatory** for every fix: the `(test, RED first)` tasks must be red before the paired impl is started.
- **No new production dependencies.** The `material-icons-extended` artifact is a last-resort contingency only — prefer a core icon first.
- **Do not touch the `PlantIdentifier` seam or the KB.** This sprint is bug-fixes, not behaviour changes. The §4.5 grep from PLANTPOTTING-0001 must keep passing.
- **Sandbox warning** (per the user's auto-memory): shell-tool writes outside `D:/DarkFactoryProject/Plant potting/` may not reach disk on this machine. Verify file presence via the user's terminal before claiming acceptance.
- **If you fall behind, follow §5's de-scope order.** Phase 1, Phase 2, and the must-land portions of Phase 3 are non-negotiable; the ledger move depends on them.

---

## 8. Acceptance criteria

The sprint is done when **every** statement below is observably true. Each is testable; ambiguity is a bug in the criterion.

- [ ] `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking pixel6Api34DebugAndroidTest` runs green on a clean clone in CI. *(JVM portion green; `pixel6Api34DebugAndroidTest` in §7.4.)*
- [ ] `PermissionResumeRecoveryTest` and `PermissionScreenHostResumeTest` exist and are green. *(JVM `PermissionScreenHostResumeTest` green; instrumentation `PermissionResumeRecoveryTest` exists; GMD run in §7.4.)*
- [x] `PermissionScreenHost` re-checks `CameraPermissionGuard.isGranted()` on `Lifecycle.Event.ON_RESUME` and clears the local `permanentlyDenied` flag when the platform now reports granted. The `onGranted` callback fires at most once per Granted transition.
- [ ] On a connected emulator (or device): denying camera permission → opening Settings → toggling the permission on → returning to the app navigates to the camera screen without a process restart. Manual confirmation captured in the results doc. *(No physical device available; covered by `PermissionResumeRecoveryTest` on the GMD per §1.5 trade-off.)*
- [ ] `CameraScreenSmokeTest` exists and is green. It asserts that no shutter text node is the literal `"C"` and that the shutter's content description equals `camera_shutter_label`. *(Exists; GMD-run pending in §7.4.)*
- [x] `CameraScreen` renders the shutter as a `FloatingActionButton` containing `Icon(Icons.Default.CameraAlt | Icons.Default.PhotoCamera, contentDescription = stringResource(R.string.camera_shutter_label))` with `CameraScreenTags.SHUTTER` preserved. *(Implementation note: neither `CameraAlt` nor `PhotoCamera` is in the core icon set on Compose BOM 2024.06.00, so we ship a vector drawable at `res/drawable/ic_camera_shutter.xml` instead of adding `material-icons-extended` — see §6.3 anti-goal.)*
- [ ] **(should-land)** The shutter is disabled (and a `BIND_PROGRESS` `CircularProgressIndicator` is visible) while `imageCapture == null && state is Idle`. The shutter becomes enabled and the overlay disappears once `imageCapture` is non-null. *(De-scopable per §5; document if dropped. Implementation landed; GMD verification pending in §7.4.)*
- [ ] **(should-land)** `CameraPreviewLayoutTest` exists and is green, asserting the preview view measures > 80% of its parent on both first entry and post-Retake re-entry. *(De-scopable per §5; document if dropped. Exists; GMD-run pending in §7.4.)*
- [x] `scripts/integration-flow.ps1` resolves `adb` from `PATH`, then `$env:ANDROID_HOME\platform-tools\adb.exe`, then `$env:ANDROID_SDK_ROOT\platform-tools\adb.exe`, and hard-fails when none of those resolve and `-BuildOnly` is not set. The script also hard-fails on no-device, install-APK failure, and missing ui-hierarchy evidence in device-aware mode.
- [x] `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` requires the device-aware lines `archetype-name=aroid chunky`, `recipe-row-count=5`, `device-screenshot-count-at-least-1=true`, and `manifest-mode=device-aware`.
- [x] `docs/sprints/expected-artifacts/PLANTPOTTING-0001-buildonly.txt` exists with the original four lines plus `manifest-mode=build-only`, and `-BuildOnly` runs diff against it.
- [ ] Running `scripts/integration-flow.ps1` against a connected device produces a manifest that diffs cleanly against the device-aware expected file; running it without a device exits non-zero. Both transcripts are captured under `artifacts/PLANTPOTTING-0002/`. *(No-device hard-fail captured + `-BuildOnly` green captured under `docs/sprints/evidence/PLANTPOTTING-0002/integration-flow-transcripts.md` since `artifacts/` is gitignored; device-aware happy path pending a connected device.)*
- [ ] The §2.1 flow from PLANTPOTTING-0001.md is visibly clean on the `pixel6Api34` GMD with no `"C"` glyph anywhere and no broken denial-recovery path. *(GMD run pending in §7.4.)*
- [x] `docs/sprints/PLANTPOTTING-0001.md` has a "PLANTPOTTING-0002 closure" note ticking the §6.7 `CameraScreenSmokeTest` checkbox and any acceptance lines that were failing on Bug 1 / Bug 4 / the device-aware manifest.
- [ ] `docs/sprints/ledger.yaml` shows PLANTPOTTING-0001 `status: done` and PLANTPOTTING-0002 `status: done` with up-to-date `updated` timestamps. *(Pending §7.4 + §7.3.)*
- [x] `docs/sprints/results/PLANTPOTTING-0002.md` exists and contains: bugs closed, manifest-mode decision and rationale, test/CI command outputs, integration-script outputs (device-aware + build-only if exercised), Bug 2 disposition, UX 2 audit outcome, link to `artifacts/PLANTPOTTING-0002/`, and a one-line handoff note to PLANTPOTTING-0003. *(GMD command output added in §3a once §7.4 completes.)*
- [x] Every completed feature task above has a paired completed test task (or, for Phase 5 if Bug 2 doesn't reproduce, a documented guard test that stays green without a code change).
- [x] The PLANTPOTTING-0001 §4.5 grep check (`grep -R "StubPlantIdentifier" app/src/main/` returns only paths under `identify/`) still passes — the `PlantIdentifier` seam was not touched. *(`bash scripts/check-stub-isolation.sh` → "stub isolation OK".)*
