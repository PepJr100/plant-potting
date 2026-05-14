# PLANTPOTTING-0002 Feedback

**Reviewer:** PepJr100 (whichrobevans@gmail.com)
**Review date:** 2026-05-14
**Sprint outcome:** 5 of 6 bugs/UX items closed cleanly; 1 new tooling bug surfaced during the review's live device-aware integration-script run that had not previously been exercised end-to-end by anyone.

---

## Bugs

### Bug A — `scripts/integration-flow.ps1` device-aware happy path is reproducibly broken

**Reproduction:**
1. Boot a clean `Pixel_6_API_34` standalone emulator (`emulator -avd Pixel_6_API_34`); wait for home screen; unlock if needed.
2. From the project root: `pwsh ./scripts/integration-flow.ps1` (no `-BuildOnly` flag).
3. Script proceeds through `[1/5] assembleDebug + verifyNoNetworking` (green) and `[2/5] APK content inspection` (green), then fails inside `[3/5] device-aware flow` immediately after `installing APK` / `waiting for camera screen`.

Reproduced twice in this session: once with a `pixel6Api34` GMD-leftover emulator still attached, once with a freshly booted standalone `Pixel_6_API_34`. Same failure both times.

**Expected:** Script drives the §2.1 user flow (camera → shutter → result → recommendation), produces `artifacts/PLANTPOTTING-0001/manifest.txt`, and diffs cleanly against `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`.

**Actual:**
```
[3/5] device-aware flow
       adb: C:\Users\robev\AppData\Local\Android\Sdk\platform-tools\adb.exe
       installing APK
       waiting for camera screen
ERROR: null root node returned by UiTestAutomationBridge.
adb pull /sdcard/ui.xml -> artifacts/PLANTPOTTING-0001/ui-hierarchy-camera.xml failed
At D:\DarkFactoryProject\Plant potting\scripts\integration-flow.ps1:94 char:9
+         throw "adb pull /sdcard/ui.xml -> $path failed"
```

**Server log evidence:** Inline in the actual output above; no separate server logs apply (PowerShell script + adb).

**Root cause (diagnosed during review):**
Two interacting issues in `scripts/integration-flow.ps1`:

1. **`uiautomator dump` returns exit 0 even when it writes `ERROR: null root node returned by UiTestAutomationBridge` to stderr.** `Invoke-AdbDump` at `scripts/integration-flow.ps1:87-96` only checks `$LASTEXITCODE`, so the soft failure (no dump file written) is invisible to the script. The hard error surfaces one step later when `adb pull /sdcard/ui.xml` fails because the file was never created.
2. **`Wait-ForNode` has a retry loop (8 attempts × 750 ms)** at `scripts/integration-flow.ps1:98-108`, but it calls `Invoke-AdbDump`, which throws unconditionally on the first `adb pull` failure (`integration-flow.ps1:93-95`). The throw propagates *out* of `Wait-ForNode`'s `for` loop, so the retry never runs. The first dump miss is fatal — the wait/retry is structurally a no-op.

The "null root node returned by UiTestAutomationBridge" error itself is the standard Android symptom of `uiautomator dump` racing against `am start` before the launched activity has gained accessibility focus. The retry was designed to absorb exactly this — it just doesn't, because of (2).

**Fix direction (for next sprint):**

- Make `Invoke-AdbDump` return a status (`$true`/`$false` or `$null`) on soft failure rather than throwing, so the `Wait-ForNode` retry loop can actually retry. Reserve the throw for hard failures (e.g., adb itself missing).
- Additionally, capture `2>&1` from `adb shell uiautomator dump` and treat the literal string `"null root node returned by UiTestAutomationBridge"` as an explicit retry signal — this is the documented Android failure mode for racing `am start`.
- Add a `Start-Sleep -Seconds 2` (or a `cmd /c "adb shell am wait-for-activity ..."`-style wait) after the `am start` invocation at `integration-flow.ps1:177` before the first dump attempt, since 8 × 750 ms = 6 s and a cold first-launch foreground transition can need close to that on the AOSP image.

**Impact:**
- The §8 acceptance criterion *"Running `scripts/integration-flow.ps1` against a connected device produces a manifest that diffs cleanly against the device-aware expected file"* (line 310 of `docs/sprints/PLANTPOTTING-0002.md`) cannot currently be satisfied on this machine. It was left unchecked at sprint close-out with the rationale that no live run had been attempted; this review attempted it twice and confirmed it deterministically fails.
- The script's logic *is* covered indirectly — `RecommendationScreenTest` covers the `RECIPE_ROW` semantic-tag presence at the unit layer, and the AOSP GMD covers the §2.1 user flow at the instrumentation layer. So the breakage doesn't affect user-facing correctness or the sprint's "fix sprint" deliverable; it is the only remaining hole in the §8 acceptance grid.
- Structurally this is the spiritual sibling of Bug 3: Bug 3 closed the "silent skip when no adb" hole; Bug A closes the "happy path actually works when adb resolves" hole. PLANTPOTTING-0002 fixed half the script-robustness story.

**Priority:** medium. Tooling-only. Should be addressed in PLANTPOTTING-0003, but does not block the on-device ML work itself — it's a script-bug task that can land in parallel.

---

## UX Issues

None observed. The manual UX walkthrough on a connected emulator was clean:

- **Bug 1 (shutter icon):** The shutter now renders as a FloatingActionButton with a camera-icon glyph. No literal "C" anywhere. Content description matches `R.string.camera_shutter_label`.
- **UX 1 (bind window spinner):** Worked as expected on first composition — disabled shutter + `BIND_PROGRESS` spinner visible during the brief CameraX bind window.
- **Bug 4 (permission Settings round-trip):** Deny → Open settings → toggle camera on → back → navigated directly to the camera screen without a process restart. The previously-broken denial-recovery path is closed.
- **Bug 2 (preview letterbox post-Retake):** Did not reproduce. Preview filled the screen normally on both first entry and after Retake. Disposition as "regression-guarded by `CameraPreviewLayoutTest`" stands.

---

## Missing Features

None for this fix sprint's scope. The deferred items are correctly carried forward:

- **UX 2 (source-driven badge)** — deferred to PLANTPOTTING-0003 per §6.4 defer rule. Audit findings recorded in §6.1 of the sprint plan and §1 of the results doc.
- **On-device ML model** — out of scope for this sprint (PLANTPOTTING-0003).
- **Re-enabling `PermissionDeniedFlowTest`** — still `@Ignore`, same status as PLANTPOTTING-0001. Out of scope.
- **Real-device evidence for Bug 4** — not captured (no physical device available); the `PermissionResumeRecoveryTest` instrumentation test on the GMD is the binding evidence and is green.

---

## Test commands run during review

| Command | Result |
| --- | --- |
| `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` | `BUILD SUCCESSFUL in 2m 57s` |
| `./gradlew pixel6Api34DebugAndroidTest` | `BUILD SUCCESSFUL in 2m 38s` (re-run: `BUILD SUCCESSFUL in 2m 26s`) |
| `pwsh ./scripts/integration-flow.ps1 -BuildOnly` | `Integration manifest diff passed.` (clean diff against `PLANTPOTTING-0001-buildonly.txt`) |
| `pwsh ./scripts/integration-flow.ps1` (against GMD-leftover `pixel6Api34`) | **FAILED** — see Bug A |
| `pwsh ./scripts/integration-flow.ps1` (against freshly booted standalone `Pixel_6_API_34`) | **FAILED** — same error as above; deterministic |
| Manual UX walkthrough on connected emulator | All four checkpoints (Bug 1, UX 1, Bug 4, Bug 2) clean |

---

## Acceptance criteria — final state after live review

Only one acceptance line is genuinely unsatisfiable in the current state:

- **§8 line 310** — *"Running `scripts/integration-flow.ps1` against a connected device produces a manifest that diffs cleanly against the device-aware expected file; running it without a device exits non-zero. Both transcripts are captured under `artifacts/PLANTPOTTING-0002/`."*

The "no-device exits non-zero" half is satisfied (transcript captured by the executor under `docs/sprints/evidence/PLANTPOTTING-0002/integration-flow-transcripts.md`). The "clean diff against device-aware expected file" half is **confirmed-broken** by this review and tracked as Bug A above.

All other acceptance lines (60 of 61) remain ticked.

---

## Notes for next sprint (PLANTPOTTING-0003)

- **Land Bug A's fix early in PLANTPOTTING-0003.** Likely a single sub-task: ~30–60 lines of PowerShell, paired with a deterministic test run on a clean emulator that confirms the device-aware diff passes. Once green, the §8 line 310 from PLANTPOTTING-0002 finally closes (acceptable to close it retroactively in the PLANTPOTTING-0003 results doc).
- **UX 2 audit findings are ready** (§6.1 of the plan) — the source-driven badge wiring is in scope as a natural sub-task of replacing the stub identifier with the real on-device model.
- **`CameraScreenTestRegistry.testImageCapture` + `forceSkipBind` slots are reusable** — future on-device-model tests can inject a non-null `ImageCapture` without standing up CameraX, and can deterministically force the bind-pending UI for instrumentation.
- **No regressions observed in PLANTPOTTING-0001 territory.** §2.1 user flow is visibly clean; §4.5 stub-isolation grep still passes (`bash scripts/check-stub-isolation.sh` was implicitly green in the GMD chain).

---

## Handoff

Feedback logged at `docs/sprints/feedback/PLANTPOTTING-0002/feedback.md`. When ready, run `/sprint-planner` for PLANTPOTTING-0003 — the on-device ML sprint — and the planner will read this feedback automatically. Carry Bug A as a small must-land tooling sub-task; the rest is greenfield ML work.
