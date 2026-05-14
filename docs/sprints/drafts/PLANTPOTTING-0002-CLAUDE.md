# PLANTPOTTING-0002 — Fix sprint for PLANTPOTTING-0001 review bugs

**Status:** draft
**Sprint window:** ~3–5 days for a single AI implementer (opus, gpt-5.4, or gemini)
**Source review:** `docs/sprints/feedback/PLANTPOTTING-0001/feedback.md`
**Primary goal:** close the high-severity review issues so `PLANTPOTTING-0001` can move from `in-progress` to `done` in `docs/sprints/ledger.yaml`, and the §2.1 flow is clean on the `pixel6Api34` Gradle Managed Device.

---

## 1. Intent

PLANTPOTTING-0002 is a narrow stabilisation sprint. It does **not** introduce the real on-device ML model (that is PLANTPOTTING-0003). It closes the user-visible bugs and tooling gap that came out of the PLANTPOTTING-0001 review:

- **Bug 4** — permission state is stale after the Settings round-trip on Android 14+, so the documented denial-recovery path is a dead-end until the user kills and relaunches the app.
- **Bug 1** — the camera shutter renders a literal "C" glyph because `CameraScreen.kt:131` slices the first character off `camera_shutter_label` instead of using an icon.
- **Bug 3** — `scripts/integration-flow.ps1` silently skips device-driven coverage when `adb` is not on `PATH`, and the expected manifest at `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` never asks for the device-aware lines that §8.5 originally specified.

Bug 2 (transient preview letterboxing on Retake) was not reproducible in the human replay — we add a regression guard test rather than a speculative refactor. UX 1 (no feedback during CameraX bind window) is a small, scoped follow-up to Bug 1 that lands in the same area of `CameraScreen.kt`.

The sprint is **done** when:

1. The §2.1 user flow from `docs/sprints/PLANTPOTTING-0001.md` works end-to-end on the `pixel6Api34` GMD with no visible "C" glyph and no placeholder text on the camera or recommendation screens.
2. A Settings round-trip (deny → Open settings → toggle permission on → back to app) navigates the user to the camera screen on resume, without a process restart.
3. All new bug-fix tests are green in CI alongside the existing `assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking pixel6Api34DebugAndroidTest` chain.
4. `docs/sprints/ledger.yaml` moves PLANTPOTTING-0001 from `in-progress` → `done` and PLANTPOTTING-0002 to `done`.

---

## 2. Scope boundaries

### 2.1 In scope (must land)

- Bug 4 — permission state on resume, with paired instrumentation test.
- Bug 1 — replace the literal "C" with a FAB + `Icons.Default.CameraAlt`, and land the deferred `CameraScreenSmokeTest` (§6.7 in PLANTPOTTING-0001.md).
- Bug 3 — robust adb resolution in `scripts/integration-flow.ps1`, device-aware expected manifest, and a clear decision on hard-fail vs distinct build-only/device-aware manifest targets.

### 2.2 In scope (should land if time allows)

- Bug 2 — investigate `bindCameraUseCases` placement and add a Compose-UI test asserting the preview view's measured size is > 80% of its parent on re-entry post-Retake. Fix only if the test reproduces the issue.
- UX 1 — gate the shutter `enabled` state on `imageCapture != null` and show a `CircularProgressIndicator` overlay during the bind window.

### 2.3 Optional / backlog

- UX 2 — refactor the stub badge in `ResultScreen` to read from `IdentificationResult.source` so it self-clears once the real model lands.

### 2.4 Out of scope

- The on-device ML model itself (PLANTPOTTING-0003).
- Re-enabling `PermissionDeniedFlowTest` by driving the system permission dialog with `UiAutomator`.
- Any new production dependencies. (Test-only dependencies already in use — Compose UI Test, Espresso, Intents, Hilt-testing — may be expanded but no new artifacts.)
- Real-device evidence (§8.7) — still optional and uncoupled from the acceptance gate.
- Any KB content edits, engine logic changes, or navigation-graph restructuring.
- Detekt, dynamic colour, dark-mode tuning, accessibility audit beyond Compose defaults.

---

## 3. Task list

TDD is mandatory: paired test tasks land in failing state before the implementation tasks they cover. Test task IDs are suffixed `(test)`.

### Phase 0 — Sprint setup

- [ ] **0.1** Read this plan, then re-read `docs/sprints/feedback/PLANTPOTTING-0001/feedback.md`, `app/src/main/java/com/darkfactory/plantpotting/permission/PermissionScreen.kt`, `app/src/main/java/com/darkfactory/plantpotting/permission/CameraPermissionGuard.kt`, `app/src/main/java/com/darkfactory/plantpotting/camera/CameraScreen.kt`, `scripts/integration-flow.ps1`, and `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`.
- [ ] **0.2** Confirm the baseline CI chain still runs green on the current `main` before starting: `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` and (locally) `pixel6Api34DebugAndroidTest`. Record the commit hash in sprint notes.
- [ ] **0.3** Update `docs/sprints/ledger.yaml`: set PLANTPOTTING-0002 `status: in-progress` and stamp `executor`. Leave PLANTPOTTING-0001 `in-progress` until §6 acceptance closes it.

### Phase 1 — Bug 4: permission state stale after Settings round-trip (must land)

Hotspot: `app/src/main/java/com/darkfactory/plantpotting/permission/PermissionScreen.kt:148-154`. `state` is derived inline from `guard.isGranted()` + saved local flags; nothing invalidates composition when the app returns from `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`, so a stale `permanentlyDenied = true` wins over the now-granted platform state.

- [ ] **1.1 (test, RED first)** Add an instrumentation test `PermissionResumeRecoveryTest` under `app/src/androidTest/java/com/darkfactory/plantpotting/permission/`. It must:
    - Use `@HiltAndroidTest` and bind a fake `CameraPermissionGuard` (or a controllable `@Provides` variant) that starts ungranted and flips to granted after a test-controlled signal.
    - Render `PermissionScreenHost` (or launch `MainActivity` and drive it via the existing nav graph) with `permanentlyDenied = true` as the initial state (simulating an in-app deny).
    - Flip the fake guard to `isGranted() = true` while the host is composed but *before* dispatching `ON_RESUME`.
    - Dispatch `Lifecycle.Event.ON_RESUME` via `TestLifecycleOwner` (or by driving `MainActivity` through `ActivityScenario.moveToState(Lifecycle.State.RESUMED)` after a `STARTED` round-trip).
    - Assert (a) `onGranted` callback fired exactly once after resume, (b) the navhost progresses to `camera` if launched through `MainActivity`, (c) the `OPEN_SETTINGS_BUTTON` test tag is no longer present after resume.
- [ ] **1.2 (test, RED first)** Add a JVM Compose UI test `PermissionScreenHostResumeTest` under `app/src/androidTest/java/...permission/` (lighter-weight than 1.1, no Hilt, drives `PermissionScreenHost` directly with a stub guard and `TestLifecycleOwner`). Asserts the local `permanentlyDenied` flag is cleared when `ON_RESUME` fires and `guard.isGranted()` now returns true.
- [ ] **1.3** In `PermissionScreen.kt`, modify `PermissionScreenHost` to:
    - Hold `isGrantedNow` in a `mutableStateOf(guard.isGranted())` so re-reads recompose the host.
    - Register a `LifecycleEventObserver` against `LocalLifecycleOwner.current` inside a `DisposableEffect(Unit)` that fires on `Lifecycle.Event.ON_RESUME`, re-invokes `guard.isGranted()`, writes the result to `isGrantedNow`, and clears `permanentlyDenied = false` when the new value is `true`.
    - Compute `state` from `isGrantedNow` (not the inline `guard.isGranted()` call) so the change is observed.
    - Keep the existing `LaunchedEffect(state) { if (state is PermissionUiState.Granted) onGranted() }` behaviour but ensure resume-driven transitions to `Granted` cannot emit `onGranted()` twice (e.g. by guarding on a `rememberSaveable` "already navigated" flag or by trusting the nav graph's `popUpTo`).
- [ ] **1.4** Update the KDoc on `PermissionScreenHost` and on `CameraPermissionGuard.isGranted` (lines 13–17 in `CameraPermissionGuard.kt`) to reflect that the host now lifecycle-observes rather than relying on recomposition.
- [ ] **1.5 (verify)** Run `PermissionResumeRecoveryTest` and `PermissionScreenHostResumeTest` locally — expect both green. Re-run `pixel6Api34DebugAndroidTest` and confirm the existing `PermissionScreenTest` (`app/src/androidTest/.../PermissionScreenTest.kt`) is still green.
- [ ] **1.6 (manual)** From a connected emulator or device: deny → tap **Open system settings** → toggle camera permission on → back to app → confirm navigation to camera screen without process restart. Capture a screenshot to `artifacts/PLANTPOTTING-0002/permission-resume.png` (informal evidence, not a binding gate).

### Phase 2 — Bug 1: shutter renders "C" + land `CameraScreenSmokeTest` (must land)

Hotspot: `app/src/main/java/com/darkfactory/plantpotting/camera/CameraScreen.kt:118-132`. The `Button` content is `Text(stringResource(R.string.camera_shutter_label).first().toString())` — the `.first().toString()` slice is the root cause.

- [ ] **2.1 (test, RED first)** Add `CameraScreenSmokeTest` under `app/src/androidTest/java/com/darkfactory/plantpotting/camera/CameraScreenSmokeTest.kt` covering (this is §6.7 from PLANTPOTTING-0001 that was deferred):
    - The shutter node identified by `CameraScreenTags.SHUTTER` exists and is in the semantics tree.
    - The shutter's content description equals the resolved string for `R.string.camera_shutter_label` ("Capture plant photo").
    - **No text node anywhere under the shutter is the literal `"C"` or any single-character substring of `camera_shutter_label`** — this is the direct regression check for Bug 1.
    - The shutter is enabled when the `CameraViewModel` is in `Idle` *and* a non-null `ImageCapture` is registered (this assertion couples to UX 1; for the smoke test alone, drive the registry with a fake `ImageCapture` or expose a test seam — see 2.2).
    - Tapping the shutter while `Capturing`/`Identifying` does nothing (disabled).
- [ ] **2.2 (test scaffold)** If needed, extend the existing `CameraScreenTestRegistry` (`CameraScreen.kt:152-155`) with a `currentImageCapture: ImageCapture?` slot so the smoke test can inject a fake bound state without instantiating CameraX. Keep the scaffold under `internal` visibility.
- [ ] **2.3** Replace the shutter `Button` block in `CameraScreen.kt:118-132` with a `FloatingActionButton`:
    - Use `androidx.compose.material3.FloatingActionButton` (already on the Material3 dep).
    - Content: `Icon(Icons.Default.CameraAlt, contentDescription = stringResource(R.string.camera_shutter_label))` (import `androidx.compose.material.icons.Icons` and `androidx.compose.material.icons.filled.CameraAlt` — both are in the Material Icons module that Compose Material3 already pulls; if missing, **add** `androidx.compose.material:material-icons-extended` only if the core set doesn't expose `CameraAlt`, otherwise prefer the core icon).
    - Preserve `CameraScreenTags.SHUTTER` on the clickable node and keep the 72 dp size + bottom-centre alignment.
    - Keep `enabled = state is CameraUiState.Idle || state is CameraUiState.Failure` for now; UX 1 (Phase 4) tightens it.
    - Remove the `Text(...first().toString())` line and any now-unused imports.
- [ ] **2.4 (verify)** Run `CameraScreenSmokeTest` locally on `pixel6Api34DebugAndroidTest`; expect green. Confirm `EndToEndFlowTest` (`app/src/androidTest/.../EndToEndFlowTest.kt`) still passes — the FAB swap should be transparent to it.
- [ ] **2.5 (lint)** Run `./gradlew ktlintCheck`; fix any import-order or naming churn introduced by the FAB swap.

### Phase 3 — Bug 3: integration script adb fallback + device-aware manifest (must land)

Hotspot: `scripts/integration-flow.ps1:56-65`. `Get-Command adb` returns `$null` if `adb` isn't on `PATH`, and the script then writes a 4-line build-only manifest that diffs cleanly against an equally minimal expected manifest. The script *passes* without doing the device half of its job.

- [ ] **3.1 (decision, recorded in plan)** Decide and document the manifest policy. **Recommended default:** the script runs in "device-aware" mode and **hard-fails** when no device is attached, unless `-BuildOnly` (explicit opt-in) is passed at the command line. In `-BuildOnly` mode the script writes a manifest tagged `mode=build-only` and diffs against a *separate* expected file (or refuses to satisfy the device-aware gate). Record the decision in a short `## Manifest policy` block in `docs/sprints/results/PLANTPOTTING-0002.md` when the sprint closes.
- [ ] **3.2 (test, manual repro documented)** Add `scripts/test-integration-flow-adb-fallback.ps1` (a small companion script, ≤30 lines) that:
    - Saves and clears `$env:Path` of any directory containing `adb.exe`.
    - Sets `$env:ANDROID_HOME` to a valid SDK path.
    - Invokes `scripts/integration-flow.ps1` and asserts a non-zero exit when no device is connected (or, conversely, a successful device-aware run when a device is attached).
    - Restores `$env:Path` on exit.
    Document the repro steps in the script header so a human can run it from a fresh PowerShell with `adb` removed from `PATH`.
- [ ] **3.3** Add a `Resolve-AdbPath` PowerShell function near the top of `scripts/integration-flow.ps1` (after the variable block) that tries, in order:
    1. `(Get-Command adb -ErrorAction SilentlyContinue).Source`
    2. `Join-Path $env:ANDROID_HOME 'platform-tools\adb.exe'` if `$env:ANDROID_HOME` is set and the file exists.
    3. `Join-Path $env:ANDROID_SDK_ROOT 'platform-tools\adb.exe'` if `$env:ANDROID_SDK_ROOT` is set and the file exists.
    Returns `$null` if none are found.
- [ ] **3.4** In `scripts/integration-flow.ps1`, replace every bare `adb` invocation with the resolved path captured at the top of step `[3/5]`. If the resolved path is `$null`:
    - In device-aware mode (default): `throw "adb not found on PATH, ANDROID_HOME, or ANDROID_SDK_ROOT — refusing to skip device coverage"`.
    - In `-BuildOnly` mode (explicit opt-in): warn loudly, write `mode=build-only` into the manifest, and proceed.
- [ ] **3.5** Add a `[CmdletBinding()] param([switch]$BuildOnly)` to the script header so the build-only mode is an explicit CLI switch.
- [ ] **3.6** When a device is attached and the script drives the flow, parse `artifacts/PLANTPOTTING-0001/ui-hierarchy.xml` after navigating to the recommendation screen and extract:
    - The archetype display name → write `archetype-name=<name>` to the manifest.
    - The recipe-row count (count of `RecipeRowTag` semantic nodes, or fall back to a regex over the hierarchy XML) → write `recipe-row-count=<n>`.
    - A successful screenshot count → keep `device-screenshot-count-at-least-1=true`.
    If `EndToEndFlowTest` already exposes a deterministic species (it does — `FakeFixedIdentifier`), the expected archetype name and row count are fixed: `Aroid Chunky` and `5`.
- [ ] **3.7** Update `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` so the **default device-aware** manifest requires, in addition to the existing four lines:
    - `archetype-name=aroid chunky` (lower-cased per the existing case-folding in `[5/5]`),
    - `recipe-row-count=5`,
    - `device-screenshot-count-at-least-1=true`,
    - `mode=device-aware` (matches the new mode tag the script writes).
- [ ] **3.8** If §3.1's decision keeps build-only mode supported, add `docs/sprints/expected-artifacts/PLANTPOTTING-0001-buildonly.txt` for `-BuildOnly` runs (the original four lines + `mode=build-only`). The default `-Expected` flag points at the device-aware file; `-BuildOnly` switches to the build-only file. Otherwise (hard-fail-only path), skip the file and document the reason in `docs/sprints/results/PLANTPOTTING-0002.md`.
- [ ] **3.9 (test, after 3.3–3.8)** Run `scripts/integration-flow.ps1` with `adb` removed from `PATH` but `ANDROID_HOME` pointing at a valid SDK + a connected emulator. Expect green diff against the new expected file. Then re-run with no device attached — expect a hard fail (non-zero exit). Capture both runs' transcripts into `artifacts/PLANTPOTTING-0002/`.
- [ ] **3.10** Add a `RecipeRowTag` test-tag constant (or confirm one already exists in `RecommendationScreen`) so §3.6's row-count extraction has a deterministic semantic node to count rather than scraping raw text. Wire it into the recommendation table row composable if not already there.

### Phase 4 — UX 1: shutter loading state during CameraX bind window (should land)

Hotspot: `CameraScreen.kt:118-132`. `enabled = state is CameraUiState.Idle || state is CameraUiState.Failure` doesn't gate on `imageCapture != null`, so the shutter accepts taps that fall into the early-return path at line 120.

- [ ] **4.1 (test, RED first)** Extend `CameraScreenSmokeTest` (or add `CameraScreenBindStateTest` alongside it):
    - Initial composition with `currentImageCapture = null`: shutter is disabled (`assertIsNotEnabled`) and a `CircularProgressIndicator` overlay is visible (tag it under a new `CameraScreenTags.BIND_PROGRESS`).
    - After flipping `currentImageCapture` to a non-null fake: the overlay disappears and the shutter is enabled.
- [ ] **4.2** Add `CameraScreenTags.BIND_PROGRESS = "camera.bindProgress"` to the `CameraScreenTags` object in `CameraScreen.kt`.
- [ ] **4.3** In `CameraScreen.kt`, add a Compose-tracked `imageCapture` state read (it already is — `var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }`) and:
    - Render a small centred `CircularProgressIndicator` (with `BIND_PROGRESS` tag) inside the existing `Box` while `imageCapture == null && state is CameraUiState.Idle`.
    - Update the FAB's `enabled` to `state is CameraUiState.Idle || state is CameraUiState.Failure` **and** `imageCapture != null`.
    - Make sure the capture/identify overlay (lines 88–104) still wins over the bind-progress overlay when in-flight, so we don't render both.
- [ ] **4.4 (verify)** Run the new bind-state assertions on the GMD; confirm the §2.1 flow still passes `EndToEndFlowTest` (it should — `EndToEndFlowTest` uses a test hook that bypasses the real CameraX bind).

### Phase 5 — Bug 2: investigate transient preview letterboxing (should land, investigation only)

Hotspot: `CameraScreen.kt:74-86` binds CameraX inside the `AndroidView.factory` lambda, which fires once per Compose insertion. Re-entering the camera screen post-Retake re-inserts the composable, so the factory fires again — but the human reviewer could not reproduce the letterbox; only one Claude Code screenshot showed it. Treat as a guard test, not a forced refactor.

- [ ] **5.1 (review)** Read `CameraScreen.kt:73-86` and `CameraScreen.kt:157-203` end-to-end. Confirm `bindCameraUseCases` is called only from the `AndroidView.factory` (i.e. on insertion) and not also from `update`. Note any potential measurement race between `PreviewView` insertion and `surfaceProvider` attachment in a short comment at the head of `bindCameraUseCases`.
- [ ] **5.2 (test, guard)** Add `CameraPreviewLayoutTest` under `app/src/androidTest/java/.../camera/`:
    - Launch `MainActivity` with permission granted (via `GrantPermissionRule`) and the fake identifier from `EndToEndFlowTest`'s Hilt swap.
    - Navigate camera → shutter → result → See potting mix → recommendation → Retake → camera.
    - Wait for the `CameraScreenTags.PREVIEW` node and assert its measured `width > 0.8 * parent.width` and `height > 0.8 * parent.height` (use Compose `SemanticsNodeInteraction.fetchSemanticsNode().boundsInRoot` against the root box bounds).
- [ ] **5.3** Only if §5.2 fails: move the binding out of the factory lambda into a `LaunchedEffect(lifecycleOwner, previewView)` so re-entry deterministically re-binds. Keep the change minimal — do not refactor `bindCameraUseCases` beyond what the test requires.
- [ ] **5.4** If §5.2 stays green without code changes, record Bug 2 as "not reproducible; covered by `CameraPreviewLayoutTest` as a regression guard" in `docs/sprints/results/PLANTPOTTING-0002.md`.

### Phase 6 — Optional / backlog (only if Phases 1–5 close before the sprint window ends)

#### UX 2 — source-driven stub badge

Hotspot: `ResultScreen` currently hard-codes the literal "Stub identifier — replace in a later sprint" badge. `IdentificationResult.source` already carries `STUB_DETERMINISTIC | STUB_RANDOM | ON_DEVICE_MODEL | CLOUD` — the seam exists, only the rendering needs to follow.

- [ ] **6.1** Audit the nav graph: `IdentificationResult.source` is currently dropped between `CameraViewModel.onCaptureReady` and `ResultViewModel` (which loads only by `speciesId`). Decide whether to pipe `source` through the nav route as a second arg or to expose it via a small `IdentificationResultRepository` Hilt singleton scoped to the activity. Recommended: nav arg, since the data is tiny and process-death-survivable as a string enum.
- [ ] **6.2 (test, RED first)** Add `ResultScreenBadgeTest`: render `ResultScreen` with each `IdSource` value and assert the badge text per source — `STUB_*` shows the existing stub copy, `ON_DEVICE_MODEL` shows "On-device match", `CLOUD` shows "Cloud match".
- [ ] **6.3** Wire `source` through the nav graph (or repository) and update `ResultViewModel` + `ResultScreen` to render the source-aware badge.
- [ ] **6.4 (defer rule)** If §6.1 finds that piping `source` requires nav-graph changes that touch `MainActivity`, the nav-host wiring, or the existing `EndToEndFlowTest`, defer to PLANTPOTTING-0003 and document the defer in `docs/sprints/results/PLANTPOTTING-0002.md`.

### Phase 7 — Documentation, ledger, results

- [ ] **7.1** Update `docs/sprints/PLANTPOTTING-0001.md`: tick the §6.7 `CameraScreenSmokeTest` checkbox and the §6 acceptance line that was failing on it. Add a short note under §8.5 referencing the new device-aware manifest format introduced here.
- [ ] **7.2** Author `docs/sprints/results/PLANTPOTTING-0002.md` from the same template as PLANTPOTTING-0001 results. Required fields: list of bugs closed (Bug 1, Bug 3, Bug 4, plus Bug 2 disposition), test commands run + outputs, manifest-mode decision from §3.1, integration-script diff result for device-aware and (if kept) build-only modes, link to `artifacts/PLANTPOTTING-0002/` evidence, and explicit handoff note that PLANTPOTTING-0003 (on-device ML) can now start.
- [ ] **7.3** Update `docs/sprints/ledger.yaml`: PLANTPOTTING-0001 → `status: done`; PLANTPOTTING-0002 → `status: done`; stamp `updated` timestamps. Move only after §6 acceptance criteria below are observably green.
- [ ] **7.4** Run the full CI chain one more time on a clean clone: `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking pixel6Api34DebugAndroidTest` and the device-aware `scripts/integration-flow.ps1`. Record commit hash + commands + outputs in `docs/sprints/results/PLANTPOTTING-0002.md`.

---

## 4. Sequencing and dependency rules

```
Phase 0 (sprint setup)
   │
   ├──► Phase 1 (Bug 4 — permission resume)         must-land, independent of camera work
   │
   ├──► Phase 2 (Bug 1 — shutter glyph + smoke test) must-land, independent of permission work
   │           │
   │           └──► Phase 4 (UX 1 — bind loading)    should-land; same file, builds on §2's smoke test scaffold
   │                       │
   │                       └──► Phase 5 (Bug 2 guard test) should-land; needs UX 1's test infra
   │
   ├──► Phase 3 (Bug 3 — adb fallback + manifest)    must-land, can run fully in parallel with Phases 1/2/4/5
   │
   └──► Phase 6 (UX 2 — optional)                    only if everything above is green
              │
              ▼
       Phase 7 (docs + ledger + acceptance)
```

Hard gates:

- **§1.1 + §1.2 (Bug 4 tests) must be RED before §1.3 (impl)** — TDD.
- **§2.1 (`CameraScreenSmokeTest`) must be RED before §2.3 (FAB swap)** — TDD; the smoke test is also the deferred §6.7 from PLANTPOTTING-0001, so landing it green is itself an acceptance line.
- **§4.1 (bind-state test) must be RED before §4.3 (impl)** — TDD.
- **§3.9 must observably hard-fail when no device is attached before the sprint claims Bug 3 closed.** A green diff under build-only-without-a-device is not acceptance for Bug 3.
- **Phase 7 (ledger move) cannot start until §6 acceptance criteria are observably green on the GMD.**

Soft parallels:

- Phase 3 (script + manifest) touches only `scripts/` and `docs/sprints/expected-artifacts/`. It can run fully in parallel with Phases 1/2/4/5.
- Phase 1 (permission) and Phase 2 (camera shutter) touch different files and can run in either order.

De-scope order if the implementer slips:

1. Drop Phase 6 (UX 2) entirely — it's already backlog.
2. Drop Phase 5 (Bug 2 guard test) — leave Bug 2 as "non-reproducible, no guard test landed" with a follow-up.
3. Drop Phase 4 (UX 1) — Bug 1 alone closes the visible "C" glyph; UX 1 is the polish layer.
4. Last-resort: drop §3.8 (the explicit build-only manifest file) and require hard-fail-only. **Never drop Phase 1, Phase 2, the manifest-aware portion of Phase 3, the smoke test, or the device-aware expected manifest.**

---

## 5. Risks and mitigations

### 5.1 `ON_RESUME` re-entry double-fires `onGranted` and destabilises the nav stack
Risk: §1.3's lifecycle observer triggers `onGranted` on every resume after the initial grant, which `LaunchedEffect(state)` then also triggers — leading to a duplicate `navigate("camera")` call.
Mitigation: §1.3 explicitly notes the idempotency requirement; pair it with a `rememberSaveable` "alreadyNavigated" guard or rely on `navController.navigate(..., navOptions { popUpTo("permission") { inclusive = true } })`. §1.1's instrumentation test asserts `onGranted` fires exactly once.

### 5.2 Hilt + lifecycle + Compose instrumentation tests are the most brittle config we own
Risk: per PLANTPOTTING-0001 §7.7, this combo can burn half a day. §1.1's `PermissionResumeRecoveryTest` lives in this exact corner.
Mitigation: keep §1.2 as a lighter-weight test that doesn't touch Hilt (drives `PermissionScreenHost` directly with a fake guard + `TestLifecycleOwner`); §1.1 then only needs to verify the end-to-end activity-level wiring. If §1.1 burns more than a day, narrow it to invoking the `LifecycleEventObserver` callback directly and document the trade-off in the results doc.

### 5.3 `Icons.Default.CameraAlt` not in the core Material icons set
Risk: Compose Material3 ships a small "core" icon set; `CameraAlt` is usually present but the extended set may be needed.
Mitigation: §2.3 names the contingency. If the core set lacks it, add `androidx.compose.material:material-icons-extended` to `libs.versions.toml` as a test-cost-only dep on `app` (it adds ~1 MB to the APK in debug, acceptable). Sprint plan §2.4 says no new prod deps "in general" but an icon catalog isn't a behavioural dep — flag the addition in the results doc.

### 5.4 Device-aware manifest extraction (§3.6) couples script to UI internals
Risk: parsing `ui-hierarchy.xml` for archetype name + row count is fragile if a label changes.
Mitigation: drive extraction off semantic test tags (`CameraScreenTags`, `RecipeRowTag`) where possible, not visible text. The fake identifier in `EndToEndFlowTest` keeps the expected species — and therefore archetype name and row count — deterministic.

### 5.5 PowerShell strict-mode + new param block changes script invocation contract
Risk: `[CmdletBinding()] param(...)` at the top of `integration-flow.ps1` is mildly incompatible with `#requires -version 5.1` if anyone invokes the script via dot-sourcing.
Mitigation: keep `$BuildOnly` as an explicit `[switch]` and document the new flag in the script header. The existing CI invocation (PowerShell only, called from a dev or local) is unaffected.

### 5.6 Sandbox filesystem overlay on Windows (per CLAUDE.md auto-memory)
Risk: shell-tool writes outside `D:/DarkFactoryProject/Plant potting/` may not reach disk on this machine.
Mitigation: every artifact path stays under the project tree. Implementer verifies file presence via the user's terminal (`ls` / `dir`) before claiming acceptance.

### 5.7 The §2.1 flow is the only binding evidence; if the GMD test fails we have no fallback
Risk: this sprint's "done" gate hinges on the GMD run. If `pixel6Api34DebugAndroidTest` starts failing for environmental reasons unrelated to the bug fixes, the sprint stalls.
Mitigation: Phase 0.2 captures a clean baseline run on `main` first. If the baseline already fails, surface that to the user *before* starting the fixes — that's a different problem.

---

## 6. Acceptance criteria

The sprint is done when **every** statement below is observably true. Each is testable; ambiguity is a bug in the criterion.

- [ ] `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking pixel6Api34DebugAndroidTest` runs green on a clean clone in CI.
- [ ] `PermissionResumeRecoveryTest` and `PermissionScreenHostResumeTest` exist and are green.
- [ ] On a connected emulator (or device): denying camera permission → opening Settings → toggling the permission on → returning to the app navigates to the camera screen without a process restart, as a manual confirmation captured in the results doc.
- [ ] `CameraScreenSmokeTest` exists and is green. It asserts that no shutter text node is the literal `"C"` and that the shutter's content description equals `camera_shutter_label`.
- [ ] `CameraScreen` renders the shutter as a `FloatingActionButton` containing `Icon(Icons.Default.CameraAlt, contentDescription = stringResource(R.string.camera_shutter_label))` with `CameraScreenTags.SHUTTER` preserved.
- [ ] The shutter is disabled (and a `BIND_PROGRESS` `CircularProgressIndicator` is visible) while `imageCapture == null && state is Idle`. The shutter becomes enabled and the overlay disappears once `imageCapture` is non-null. *(UX 1; should-land — descope per §4 if needed and document.)*
- [ ] `CameraPreviewLayoutTest` exists and is green, asserting the preview view measures > 80% of its parent on both first entry and post-Retake re-entry. *(Bug 2 guard; should-land — descope per §4 if needed.)*
- [ ] `scripts/integration-flow.ps1` resolves `adb` from `PATH`, then `$env:ANDROID_HOME\platform-tools\adb.exe`, then `$env:ANDROID_SDK_ROOT\platform-tools\adb.exe`, and hard-fails when none of those resolve and `-BuildOnly` is not set.
- [ ] `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` requires the device-aware lines `archetype-name=aroid chunky`, `recipe-row-count=5`, `device-screenshot-count-at-least-1=true`, and `mode=device-aware`.
- [ ] Running `scripts/integration-flow.ps1` against a connected device produces a manifest that diffs cleanly against the updated expected file; running it without a device exits non-zero.
- [ ] The §2.1 flow from PLANTPOTTING-0001.md is visibly clean on the `pixel6Api34` GMD with no `"C"` glyph anywhere and no broken denial-recovery path.
- [ ] `docs/sprints/PLANTPOTTING-0001.md` §6.7 and any acceptance lines that referenced the deferred smoke test are now ticked.
- [ ] `docs/sprints/ledger.yaml` shows PLANTPOTTING-0001 `status: done` and PLANTPOTTING-0002 `status: done` with up-to-date `updated` timestamps.
- [ ] `docs/sprints/results/PLANTPOTTING-0002.md` exists and contains: bugs closed, manifest-mode decision, test/CI command outputs, integration-script outputs (device-aware + build-only if kept), Bug 2 disposition, link to `artifacts/PLANTPOTTING-0002/`, and a one-line handoff note to PLANTPOTTING-0003.
- [ ] Every completed feature task above has a paired completed test task (or, for Phase 5 if Bug 2 doesn't reproduce, a documented guard test that stays green without a code change).

---

## 7. Handoff note for the sprint-execute implementer

You are one of `opus`, `gpt-5.4`, or `gemini`, picked by the user via the `sprint-execute` skill.

- **Read this plan first**, then the five anchor files: `docs/sprints/PLANTPOTTING-0001.md`, `docs/sprints/feedback/PLANTPOTTING-0001/feedback.md`, `permission/PermissionScreen.kt`, `camera/CameraScreen.kt`, `scripts/integration-flow.ps1`.
- **Tick `- [ ]` boxes as you go**, not in batches.
- **TDD is mandatory** for every fix: the test in `(test)` tasks must be RED before the paired impl task is started.
- **No new production dependencies** (the icon-extended note in §5.3 is the one possible exception; flag it loudly in the PR if you take it).
- **Don't touch the `PlantIdentifier` seam or the KB.** This sprint is bug-fixes, not behaviour changes.
- **Sandbox warning** (per the user's auto-memory): shell-tool writes outside `D:/DarkFactoryProject/Plant potting/` may not reach disk. Verify with the user's terminal before claiming acceptance.
- **If you fall behind, de-scope per §4.** Phases 1, 2, and the must-land portions of Phase 3 are non-negotiable.
