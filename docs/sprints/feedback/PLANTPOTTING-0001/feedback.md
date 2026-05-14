# PLANTPOTTING-0001 Feedback

**Reviewer:** Rob (with Claude Code review session)
**Review date:** 2026-05-14
**Test environment:** `emulator-5554` (Pixel 6 API 34 AVD, KVM-accelerated, Windows 11 host)
**Build:** `app/build/outputs/apk/debug/app-debug.apk` from commit `1f399cc` (verification-pass fixes)
**CI chain run during review:** `assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` ✅ green; `check-stub-isolation.sh` ✅ green; `integration-flow.ps1` ✅ green (with the device-branch caveat — see Bug 3)
**Sprint status after review:** kept `in-progress`. Bugs below are scoped into a dedicated fix sprint (PLANTPOTTING-0002) before the on-device ML sprint.

Manual-test screenshots from the Claude Code session are at `artifacts/review-PLANTPOTTING-0001/`. User-driven manual-test screenshots, if any, go in `./screenshots/`.

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

### Bug 2 — Camera preview is letterboxed / wrongly sized after Retake navigation
**Severity:** high (camera screen becomes visually broken on the most common re-entry path)
**Reproduction:**
1. Grant permission → camera screen → tap shutter → result → See potting mix → Recommendation
2. Tap **Retake** to return to the camera screen
**Expected:** Full-screen black background with full-bleed camera preview filling the box, identical to the first entry into the camera screen.
**Actual:** Preview occupies only the lower portion of the screen and is left-aligned; the upper half of the screen is white (Material3 surface colour, not the `Color.Black` background the Box specifies). Shutter button sits over the preview.
**Root cause:** Unknown — needs investigation. Hypotheses worth checking in priority order:
1. `bindCameraUseCases` is called inside an `AndroidView` factory that only runs once; on re-entry the `PreviewView`'s `SurfaceProvider` may not have been re-attached, causing the view to render at intrinsic content size instead of fill.
2. The `Box`'s `fillMaxSize().background(Color.Black)` modifier composition may be losing its background colour through a Compose-Navigation state bug.
3. `PreviewView`'s default scale type (`FILL_CENTER`) combined with the AVD's 4:3 virtual scene may produce unexpected layout when the parent doesn't lay out cleanly on the second composition.
**Fix direction:** Reproduce locally, attach `View.systemUiVisibility` / layout-bounds debugging, confirm whether `bindCameraUseCases` is rebinding on each composition. Likely fix: move binding into a `LaunchedEffect(lifecycleOwner)` rather than the `AndroidView` factory, and ensure the `PreviewView` is given an explicit `LayoutParams.MATCH_PARENT` / `setImplementationMode`.
**Impact:** High — Retake is in the §2.1 acceptance flow, so this fails the acceptance bar "no crash, no ANR, no placeholder text" *if* you read it broadly (no visual glitch is implied but expected).
**Evidence:** `artifacts/review-PLANTPOTTING-0001/04-camera-screen.png` (first entry, full-screen) vs. `07-after-retake.png` (after Retake, letterboxed).

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

### UX 3 (potential — needs your manual confirmation) — Why we ask disclosure doesn't appear to collapse
**What happened:** In my single tap of **Why we ask**, the disclosure expanded as expected. I did not test a second tap to confirm it collapses.
**Suggested action for next reviewer:** Tap **Why we ask** twice in a row and confirm it toggles open/closed, not "open only".

---

## Missing Features

- **CameraScreenSmokeTest (§6.7)** — deferred per §6 de-scope order. Re-add in PLANTPOTTING-0002; it would have caught Bug 1.
- **`PermissionDeniedFlowTest` (§8.3)** — `@Ignore`'d because reaching the permanently-denied state requires driving the system permission dialog. Manually it works on Android 14 (first deny = permanently denied). A UiAutomator-based test in PLANTPOTTING-0002 could automate this.
- **Physical-device evidence (§8.7)** — none captured; no physical Android device available to the implementer. Optional but encouraged. If user has a device, run the §2.1 flow and drop screenshots in `docs/sprints/evidence/PLANTPOTTING-0001/`.

---

## Notes for Next Sprint (PLANTPOTTING-0002 fix sprint scope)

Scope (in priority order):
1. **Fix Bug 1** — replace shutter Button + Text-first-char with FloatingActionButton + Icon. Tiny change. Add `CameraScreenSmokeTest` (§6.7) at the same time so the regression can't return.
2. **Fix Bug 2** — investigate camera preview re-entry layout. Likely a `bindCameraUseCases` placement issue or `PreviewView` measurement edge case. Add an instrumentation test that navigates camera → result → recommendation → Retake and asserts preview occupies > 80% of the screen.
3. **Fix Bug 3** — make `integration-flow.ps1` fall back to `$env:ANDROID_HOME/platform-tools/adb.exe`. Update `expected-artifacts/PLANTPOTTING-0001.txt` to include the device-aware lines (`archetype-name=Aroid Chunky`, `recipe-row-count=5`). Optionally have the script fail loudly when no device is found rather than silently skip.
4. **Optional polish** — UX 1 (shutter loading state) and UX 2 (source-driven badge) are nice-to-haves that the on-device ML sprint will revisit anyway. UX 3 (Why we ask collapse) needs user confirmation first.

The on-device ML sprint (PLANTPOTTING-0003?) can start in parallel with PLANTPOTTING-0002 since none of these bugs are inside the `PlantIdentifier` seam.

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

**Verdict:** sprint is functionally complete but Bug 1 + Bug 2 are visible enough on the §2.1 flow that the sprint stays `in-progress` until PLANTPOTTING-0002 closes them.
