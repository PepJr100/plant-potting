# PLANTPOTTING-0001 Feedback

**Reviewer:** Rob (with a Claude Code review session driving the emulator and structured interview)
**Review date:** 2026-05-14
**Test environment:** `emulator-5554` (Pixel 6 API 34 AVD, KVM-accelerated, Windows 11 host). Rob ran the full §2.1 flow manually after the Claude Code session captured screenshots.
**Build:** `app/build/outputs/apk/debug/app-debug.apk` from commit `1f399cc` (verification-pass fixes)
**CI chain run during review:** `assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` ✅ green; `check-stub-isolation.sh` ✅ green; `integration-flow.ps1` ✅ green (with the device-branch caveat — see Bug 3)
**Sprint status after review:** kept `in-progress`. Bugs below are scoped into a dedicated fix sprint (PLANTPOTTING-0002) before the on-device ML sprint.

Manual-test screenshots from the Claude Code session are at `artifacts/review-PLANTPOTTING-0001/` and mirrored in `./screenshots/`.

---

## Bugs

### Bug 1 — Shutter button displays the letter "C" instead of a camera affordance
**Severity:** high (visible on every capture; user-perceptible quality issue)
**Reproduction:**
1. Launch app, grant camera permission
2. Observe the 72 dp circular button at bottom centre of the camera screen
**Expected:** A camera icon (or an empty FAB with content description) per sprint plan §6.3 ("72 dp circular shutter at bottom centre"). No literal text.
**Actual:** The button shows the letter `C`, which is the first character of the string resource `camera_shutter_label = "Capture plant photo"`.
**Root cause:** `app/src/main/java/com/darkfactory/plantpotting/camera/CameraScreen.kt:131`
```kotlin
Text(stringResource(id = R.string.camera_shutter_label).first().toString())
```
`.first().toString()` extracts only the first character. Almost certainly an unfinished placeholder where the author intended either a camera `Icon` or no content at all (with the string used only as content description for accessibility).
**Fix direction:** Replace `Button` with `FloatingActionButton`, use `Icon(Icons.Default.CameraAlt, contentDescription = stringResource(R.string.camera_shutter_label))`. The `camera_shutter_label` string then serves only as the a11y label and can stay full-text.
**Impact:** High — visible to every user on every capture. Cosmetic but signals unfinished work. The Compose-test suite (`CameraScreenSmokeTest`) was deferred per §6 de-scope, which is why no test caught it.
**Evidence:** `artifacts/review-PLANTPOTTING-0001/04-camera-screen.png` and `07-after-retake.png`.

### Bug 2 — Transient: camera preview was letterboxed in a single Claude Code screenshot; **not reproducible**
**Severity:** low (single-shot observation that didn't survive a manual replay)
**Reproduction (attempted, did not reproduce):**
1. Grant permission → camera screen → tap shutter → result → See potting mix → Recommendation
2. Tap **Retake** to return to the camera screen
**What the Claude Code session saw:** in `07-after-retake.png`, the preview occupied only the lower portion of the screen with white space above.
**What the human reviewer saw on the same emulator:** full-screen preview on both first entry **and** after Retake. No letterboxing, no white area.
**Most likely explanation:** The Claude Code screenshot caught a transient state during `bindCameraUseCases` rebinding — possibly the `AndroidView`'s `PreviewView` had not yet measured against the parent `Box.fillMaxSize()` when the screenshot was taken. Subsequent frames laid out correctly.
**Disposition:** Not a confirmed bug. Worth a quick `bindCameraUseCases` review in PLANTPOTTING-0002 to confirm rebinding happens cleanly on each `composable(Routes.CAMERA)` entry (the binding currently runs inside the `AndroidView` factory, which only fires once per Compose insertion). If a future user reports the same letterboxing, escalate.
**Evidence:** `artifacts/review-PLANTPOTTING-0001/04-camera-screen.png` and `07-after-retake.png`.

### Bug 4 — Permission state goes stale after the Settings round-trip; user-granted permission is not picked up on return
**Severity:** high (breaks the documented "recover from denial via Settings" path; user-reproducible)
**Reproduction (user-confirmed):**
1. Launch the app with no permission. Tap **Grant camera access** → tap **Don't allow** (Android 14 sets permanently-denied on first deny).
2. Tap **Open system settings**. App Info opens.
3. In App Info, tap **Permissions** → **Camera** → **Allow** (or toggle the camera permission on).
4. Return to PlantPotting via back button.
**Expected:** App resumes, re-checks `Manifest.permission.CAMERA`, sees it granted, and navigates to the camera screen.
**Actual:** App still shows the "Camera access is blocked" screen. The user has to kill and relaunch the app for the granted permission to be picked up.
**Root cause:** `PermissionScreenHost` (`permission/PermissionScreen.kt:148–154`) computes `state` from `guard.isGranted()` inline during composition:
```kotlin
val state: PermissionUiState =
    when {
        guard.isGranted() -> PermissionUiState.Granted
        permanentlyDenied -> PermissionUiState.PermanentlyDenied
        hasRequested -> PermissionUiState.Denied
        else -> PermissionUiState.NotYetAsked
    }
```
`guard.isGranted()` is a plain function call, not a `State`. Returning from the Settings activity fires `MainActivity.onResume` but does not by itself invalidate the composition, so the stale `permanentlyDenied = true` flag (set during the in-app deny) wins over the now-granted platform state. The `CameraPermissionGuard` KDoc explicitly warns "callers must re-invoke `isGranted` on each screen launch" — but `PermissionScreenHost` doesn't have a lifecycle hook to do so.
**Fix direction:**
1. Add a `LifecycleEventObserver` in `PermissionScreenHost` that, on `Lifecycle.Event.ON_RESUME`, re-reads `guard.isGranted()` into a `mutableStateOf` and resets `permanentlyDenied = false` if the platform now reports granted.
2. Or hoist permission state into a `StateFlow` exposed by a Hilt-singleton `PermissionStateRepository` that observes the activity lifecycle, and consume it via `collectAsStateWithLifecycle()`.
3. Add an instrumentation test in PLANTPOTTING-0002 that simulates the Settings round-trip and asserts the navhost progresses to `camera` on resume.
**Impact:** High — this is the most common failure-recovery path for a user who denied initially. Without the fix, the **Open system settings** button is functionally a dead-end on Android 14+: the user fixes the permission but the app refuses to acknowledge it.
**Evidence:** User-reported during structured interview (2026-05-14); root cause confirmed by reading `permission/PermissionScreen.kt`.

### Bug 3 — Integration script silently skips device-driven coverage when adb isn't on PowerShell PATH
**Severity:** medium (false sense of security — script says "passed" but skipped half its job)
**Reproduction:**
1. Open PowerShell on Windows where `adb.exe` exists at `$env:ANDROID_HOME/platform-tools/adb.exe` but is **not** on `$env:Path`
2. Run `./scripts/integration-flow.ps1`
**Expected:** Either install the APK on the connected `emulator-5554`, drive the flow, capture screenshots, and write a device-aware manifest; or fail loudly with "adb not found, refusing to skip device coverage".
**Actual:** Script prints `[3/5] adb device check ... no device -- build-only manifest`, writes a four-line manifest (`apk-exists`, `archetypes-asset-present`, `species-asset-present`, `verify-no-networking-passed`), and **passes** the diff against `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` — because the expected manifest also doesn't require any device-driven lines.
**Root cause:** `scripts/integration-flow.ps1:59` uses `Get-Command adb -ErrorAction SilentlyContinue` which returns `$null` if adb isn't on PATH, even when `$env:ANDROID_HOME` clearly points to a valid SDK with platform-tools installed.
**Fix direction:**
1. Fall back to `"$env:ANDROID_HOME\platform-tools\adb.exe"` (and `ANDROID_SDK_ROOT`) when `Get-Command adb` returns null.
2. Update `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` to require the device-driven manifest lines (e.g. `archetype-name=Aroid Chunky`, `recipe-row-count=5`) when run with a device attached — sprint plan §8.5 originally specified these but they never landed.
3. Have the script either *require* a device (and `throw` if absent in CI/local) or distinguish "build-only manifest" vs "device-aware manifest" as a separate diff target.
**Impact:** Medium — script passing on a contributor's machine without a device gives a false-positive signal. The implementer-claimed clean diff in `results/PLANTPOTTING-0001.md §4` is the device-skipped path, not the device-aware one.
**Evidence:** PowerShell session output during review (step 3/5 printed "no device"), and the four-line `artifacts/PLANTPOTTING-0001/manifest.txt`.

---

## UX Issues

### UX 1 — Camera screen's first paint is fully black for ~2–3 s after permission grant
**What happened:** On first entry to the camera screen after granting permission, the screen is completely black (background + no preview) for a couple of seconds before the AVD virtual-scene preview appears. The shutter button is rendered but tappable; if the user taps it too quickly, the early-return path in `takeJpegPicture` (`val capture = imageCapture ?: return@Button`) silently swallows the tap.
**Why it matters:** The user has no feedback that the camera is initialising. A user tapping the shutter during this window will believe the app is unresponsive.
**Suggested fix:** Show a loading state (greyed-out shutter + `CircularProgressIndicator`) while `bindCameraUseCases` hasn't called `onBound` yet. The existing `state is CameraUiState.Idle || state is CameraUiState.Failure` enablement check doesn't gate on `imageCapture != null` — adding that condition to the `enabled = …` expression would disable the shutter until the bind callback fires.
**Evidence:** `artifacts/review-PLANTPOTTING-0001/04-camera-screen.png`.

### UX 2 — "Stub identifier — replace in a later sprint" badge is good but should arguably be a debug-only chip
**What happened:** The badge is shown on every result screen in the production debug build.
**Why it matters:** Sprint plan §2.1 explicitly requires the literal text, and the deep-research-driven KB is real, so the badge serves its purpose (don't mislead a demo viewer). Once the real model lands in PLANTPOTTING-000X, this badge will need to be removed or repurposed.
**Suggested fix:** Replace with a confidence/source indicator driven by `IdentificationResult.source` (`STUB_DETERMINISTIC` → current text; `ON_DEVICE_MODEL` → "On-device match" / confidence; `CLOUD` → "Cloud match"). The seam already supports it.

### UX 3 (resolved during user interview) — Why we ask disclosure toggles cleanly
**What user observed:** Tapping **Why we ask** expands the disclosure; tapping again collapses it. No issue.
**Confirmed by:** Rob, manual emulator test, 2026-05-14.

### UX 4 — Visual evaluation of camera screen is limited by the emulator (no real camera passthrough)
**What user observed:** Beyond the literal "C" shutter glyph, it's hard to evaluate camera-screen visuals (preview framing, focus indication, edge-to-edge layout) on the Pixel 6 API 34 AVD because the AVD's virtual scene is the only "camera input" available. Real-device evidence under `docs/sprints/evidence/PLANTPOTTING-0001/` would help close this evaluation gap.
**Why it matters:** Decisions about shutter size, preview aspect ratio, framing reticle, and zoom/focus affordances are hard to make without seeing how the preview behaves with a real, moving subject (a leaf, with shadows, in normal household light).
**Suggested action:** If a physical Android device becomes available, install the debug APK and capture a short screen recording or 3–4 screenshots of the camera screen pointing at a real plant. Commit to `docs/sprints/evidence/PLANTPOTTING-0001/`. This is §8.7 (optional) from the sprint plan but high-signal.

---

## Missing Features

- **CameraScreenSmokeTest (§6.7)** — deferred per §6 de-scope order. Re-add in PLANTPOTTING-0002; it would have caught Bug 1.
- **`PermissionDeniedFlowTest` (§8.3)** — `@Ignore`'d because reaching the permanently-denied state requires driving the system permission dialog. Manually it works on Android 14 (first deny = permanently denied). A UiAutomator-based test in PLANTPOTTING-0002 could automate this.
- **Physical-device evidence (§8.7)** — none captured; no physical Android device available to the implementer. Optional but encouraged. If user has a device, run the §2.1 flow and drop screenshots in `docs/sprints/evidence/PLANTPOTTING-0001/`.

---

## Notes for Next Sprint (PLANTPOTTING-0002 fix sprint scope)

Scope (in priority order):

**High — must land:**
1. **Fix Bug 4** (permission stale after Settings round-trip) — wire a `LifecycleEventObserver` for `ON_RESUME` into `PermissionScreenHost` that re-checks `guard.isGranted()` and clears the local `permanentlyDenied` flag when the platform now reports granted. Add an instrumentation test that simulates the Settings round-trip (use `Intents.intending(hasAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))` to stub the intent, manually call `pm grant`, then assert the navhost progresses to `camera`).
2. **Fix Bug 1** (shutter "C") — replace the `Button` + `Text(...first().toString())` with a `FloatingActionButton` + `Icon(Icons.Default.CameraAlt, ...)` using `camera_shutter_label` as content description only. Add `CameraScreenSmokeTest` (§6.7) so the regression can't return.
3. **Fix Bug 3** (integration script device-branch gap) — make `scripts/integration-flow.ps1` fall back to `$env:ANDROID_HOME\platform-tools\adb.exe` and `$env:ANDROID_SDK_ROOT\platform-tools\adb.exe`. Update `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` to include the device-aware lines (e.g. `archetype-name=Aroid Chunky`, `recipe-row-count=5`) that the sprint plan §8.5 originally specified. Decide whether the script should hard-fail when no device is attached.

**Medium — nice to land:**
4. **Investigate Bug 2** (transient preview letterboxing) — do a quick code review of `bindCameraUseCases` placement and `PreviewView` measurement on re-entry. Doesn't need a fix unless reproducible; do add a Compose-UI test that re-enters camera screen post-Retake and asserts the preview view's measured size is > 80% of the parent. If the test ever flakes, we have ground truth.
5. **UX 1** (shutter loading state) — gate `enabled` on `imageCapture != null` so the shutter is unresponsive until CameraX binds. Show a small overlay during the bind window.

**Backlog / optional:**
6. **UX 2** (source-driven stub badge) — refactor the badge to read from `IdentificationResult.source` so it auto-clears once the real model lands. Could wait for the on-device ML sprint.
7. **UX 4** (real-device evidence) — capture screenshots/video of the camera screen against a real plant on a physical device. §8.7 from the sprint plan; not blocking.

**Out of scope for PLANTPOTTING-0002:**
- The on-device ML model itself (that's PLANTPOTTING-0003).
- Re-enabling `PermissionDeniedFlowTest` with UiAutomator dialog driving — file as a follow-up under PLANTPOTTING-0003 or later.

The on-device ML sprint can start in parallel with PLANTPOTTING-0002 since none of these bugs are inside the `PlantIdentifier` seam.

---

## Acceptance criteria — review results

From `docs/sprints/PLANTPOTTING-0001.md §8`:

- [x] CI chain green on clean clone — verified locally during review (modulo GMD test, which the user is invited to re-run).
- [ ] Debug APK installs and launches on GMD — passed in the results doc; the §2.1 user flow works on `emulator-5554` *with bugs documented above*.
- [ ] §2.1 user flow works end-to-end *with no placeholder text on the recommendation screen* — recommendation screen is clean, but the camera screen has the literal "C" placeholder text (Bug 1). Strict reading of this criterion: **fails**.
- [x] 8 archetypes, 16 species, every recipe sums to 100, Hoya blend sums to 100 — verified by golden test (already green).
- [x] Alias lookups for Sansevieria → Dracaena and Calathea → Goeppertia — verified by `KbContentTest`.
- [x] All listed unit & UI tests exist and are green (except deferred `CameraScreenSmokeTest`).
- [x] `PlantIdentifier` Hilt binding + stub isolation grep — verified during review.
- [x] No networking dependencies — `verifyNoNetworking` green.
- [x] Integration manifest diff clean — but see Bug 3.
- [x] `docs/kb/plant-substrate-kb-notes.md` exists.
- [x] `docs/sprints/results/PLANTPOTTING-0001.md` filled in.
- [x] Every feature task has paired test task — only `CameraScreenSmokeTest` deferred.

**Verdict:** sprint is functionally complete but Bug 1 (shutter "C") and Bug 4 (permission stale after Settings round-trip) are visible enough on the §2.1 flow that the sprint stays `in-progress` until PLANTPOTTING-0002 closes them. Bug 4 in particular breaks the documented "recover from denial via Settings" path on Android 14+. Bug 2 (preview letterboxing) was not reproducible during the user's manual run and is downgraded to a transient observation.
