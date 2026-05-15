# PLANTPOTTING-0003 Feedback

**Reviewed:** 2026-05-15 by user (whichrobevans@gmail.com), Pixel 6 API 34 emulator (AOSP virtual scene), debug build at commit `ac120c9` (HEAD of `main` at review time).

**Bottom line:** two latent production bugs surface only when the integration-flow script actually drives a real device. Both were invisible to the unit-test + ktlint + verifyNoNetworking + check-stub-isolation gate chain because (a) Bug 1 is a contract mismatch with the real native TFLite interpreter, which the unit tests fake out; (b) Bug 2 is a Compose-to-uiautomator bridge that Hilt-swapped instrumentation tests don't exercise. The sprint's unit test green is genuine but the headline acceptance bar (§2.1 user flow with a real on-device model) does not pass against a real emulator.

---

## Bugs

### Bug 1 — On-device model fails every capture: INT8 model fed a FLOAT32 tensor

**Severity:** P0. The headline feature of the sprint (real on-device ML identifier replacing the stub) is non-functional in production. Every shutter capture results in `CameraUiState.Failure` and the user is stranded on the camera screen — no `ResultScreen`, no `LowConfidencePicker`, no `RecommendationScreen` is reachable.

**Reproduction:**
1. Boot `Pixel_6_API_34` emulator.
2. Build + install debug APK: `./gradlew assembleDebug` then `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
3. Grant camera permission, launch `MainActivity`.
4. Tap the shutter on the camera screen.
5. "Identifying…" overlay flashes briefly.
6. Camera screen returns; failure-state Text appears at TopCenter with the message below.

**Expected:** the identifier returns either an `IdentificationResult` (high-conf path → `ResultScreen`) or a low-conf result with candidates (→ `LowConfidencePicker`). Per the sprint plan §4.3 confidence policy.

**Actual:** `CameraUiState.Failure("TFLite inference failed: Cannot convert between a TensorFlowLite tensor with type UINT8 and a Java object of type [[[[F (which is compatible with the TensorFlowLite type FLOAT32).")`

**Evidence:** the failure text is rendered directly into the `CameraScreen` `Failure` state at TopCenter. Captured verbatim from a uiautomator dump after a real shutter tap; see `diag-step3-after-shutter.xml`.

**Root cause:**

- The real model swapped in by commit `ac120c9` (`app/src/main/assets/ml/aiy_plants_v1/model.tflite`, sha256 `9ff2cc02…`, 5 MB) is the **INT8 / UINT8-quantized** variant of AIY Plants V1 (`V1/3`). The model's input tensor has `DataType.UINT8`.
- `ImagePreprocessor.kt:48` constructs `TensorImage(org.tensorflow.lite.DataType.FLOAT32)` and runs a `NormalizeOp(mean=127.5, std=127.5)`, producing a FLOAT32 tensor centered at zero — appropriate for an FP16 model, **not** for the quantized model the manifest actually ships.
- `model_manifest.json:9-12` still declares an FP-normalization stanza (`mean/std=127.5`). The real INT8 input contract is "raw UINT8 bytes 0–255 fed directly into the tensor"; normalization is encoded in the tensor's quantization parameters (scale/zero_point).
- When B1 was resolved (commit `ac120c9`), only `.tflite` bytes + `labels.csv` + a few field re-stamps (`label_count`, `output_tensor_shape`, `sha256`) were updated. The input-type contract and the preprocessor pipeline weren't reviewed against the real model's input tensor type. The unit tests don't catch this because `OnDevicePlantIdentifierFixturesTest` uses a faked `InterpreterFacade` that doesn't enforce TFLite's tensor-type contract — `OnDevicePlantIdentifierFixturesTest.kt` constructs results by hand from fake `Classifications` outputs, never calling `Interpreter.run`.

**Why the test suite missed it:**

- `InterpreterFacade` is an interface; the test impl is a Kotlin fake that returns canned softmax outputs.
- `OnDeviceIdentifyModuleBindingTest` was de-scoped (Hilt `@UninstallModules` + `@TestInstallIn` constraint, see B2 in results doc).
- Instrumentation tests use `FakeFixedIdentifier` via `TestIdentifyModule` (the Hilt swap), so even a real-device run of `EndToEndFlowTest` never instantiates `OnDevicePlantIdentifier` against the real `.tflite`.
- Net: nothing in the gated test chain ever feeds a real `ByteArray` through the real `Interpreter.run` against this model. The §0.2 baseline + §8.5 final-verify both pass on a Hilt-faked path.

**Fix direction (for PLANTPOTTING-0004):**

1. **Detect quantization at facade load time.** In `InterpreterFacade.realImpl.load()`, read `interpreter.getInputTensor(0).dataType()` and store it. Surface as a field on `ModelManifest`-equivalent loaded shape.
2. **Branch the preprocessor.** Either:
   - **Option A — keep one path:** drop the `NormalizeOp` for UINT8 models; load `TensorImage(DataType.UINT8)`, feed raw 0–255 bytes. This matches the upstream AIY V1 INT8 model card's documented input contract.
   - **Option B — manifest-driven:** add an `input_dtype` field to `model_manifest.json` (`"uint8"` or `"float32"`); `ImagePreprocessor` reads it at construction. Branches `TensorImage(...)` + drops/keeps `NormalizeOp` accordingly. More general but more surface area.
   - Recommended: **Option B** so a future FP16 model swap doesn't trigger a code change.
3. **Update the manifest.** Add `"input_dtype": "uint8"`, remove the `normalization` stanza (or document that it's unused for UINT8). Re-stamp `placeholder: false` is already correct.
4. **Add an instrumentation test that actually runs the real model.** A small `@HiltAndroidTest` that does NOT replace `OnDeviceIdentifyModule`, calls `identifier.identify(realJpegBytes)` against a checked-in fixture JPEG, and asserts that the call returns successfully (no exception, `source = ON_DEVICE_MODEL`). This closes the test gap that allowed Bug 1 to ship.
5. (Stretch) Add a unit test that loads the real `.tflite` from `assets/`, reads `interpreter.getInputTensor(0).dataType()`, and asserts it matches `model_manifest.json#input_dtype`. Robolectric + the real native lib should be enough; if Robolectric can't load native libs cleanly, push to an instrumentation test.

**Impact on shipped acceptance:**
- §2.1.1 (flow completes) — **fails** for any non-`FakeFixedIdentifier` path.
- §2.1.2, §2.1.3, §2.1.4 — all unreachable from a real capture.
- §8.1 build/lint/test chain — still green; the gate didn't catch the bug.
- §8.4 unit tests — green; the unit tests pass against the fake interpreter.
- §8.5 GMD — would also be green for the same reason (Hilt-swapped to fake).

---

### Bug 2 — Compose `testTag` not bridged to `resource-id` in uiautomator dumps

**Severity:** P1. Blocks any device-aware integration script that locates UI nodes by `resource-id` — which is exactly what `scripts/integration-flow.ps1` does throughout. Latent since PLANTPOTTING-0001 but masked there by Bug A's null-root race; surfaces now that Phase 7 fixed the race.

**Reproduction:**
1. Boot emulator, install + launch app, grant camera permission.
2. `adb shell uiautomator dump /sdcard/dump.xml && adb pull /sdcard/dump.xml`.
3. Inspect the dump: every node in the Compose subtree has `resource-id=""`. The shutter button is present at `bounds="[446,2022][635,2211]"` with `content-desc="Capture plant photo"`, but **no `resource-id`** attribute.
4. `pwsh ./scripts/integration-flow.ps1` therefore times out at `Wait-ForNode -resourceId "camera.shutter"` after 12 dumps.

**Expected:** the dump should expose `resource-id="camera.shutter"` on the shutter Button, matching the `Modifier.testTag(CameraScreenTags.SHUTTER)` in `CameraScreen.kt:167`. Same for all 30+ other `testTag(...)` calls across the Compose tree (`result.scientificName`, `result.sourceBadge`, `result.seePottingMix`, `lowConf.search`, `archetypePicker.archetype.*`, `recommendation.archetypeName`, `recommendation.recipeRow`, etc.).

**Actual:** every Compose node has `resource-id=""`.

**Evidence:** UI dump preserved at `diag-step2-camera-dump.xml`; relevant excerpt shows the shutter button without any `resource-id` attribute.

**Root cause:** `MainActivity.kt:19-23` calls `setContent { PlantPottingTheme { PlantPottingNavHost() } }` without wrapping the composition in a `Modifier.semantics { testTagsAsResourceId = true }` opt-in. Compose 1.2+ requires this semantic property to bridge `testTag` strings to the `android:id` / `resource-id` attribute that uiautomator surfaces. Without it, the `testTag()` strings are still visible to Compose UI tests (via the semantics tree, which is how `ResultScreenBadgeTest` etc. find them), but invisible to uiautomator.

**Why the test suite missed it:**
- Compose UI tests use the semantics tree directly (`onNodeWithTag`), not uiautomator dumps.
- The Hilt-swapped instrumentation flow tests don't dump XML from `uiautomator`; they use Espresso/Compose-Test matchers.
- The only consumer of resource-id is `scripts/integration-flow.ps1` (and its expected-artifacts manifest). That script's `Wait-ForNode` was never reaching the result-screen wait in PLANTPOTTING-0001 because of Bug A's null-root race — so this regression has been latent since day one.
- PLANTPOTTING-0002 Bug A surfaced *one* symptom (the race); fixing it in PLANTPOTTING-0003 §7 was correct but unmasked the deeper issue.

**Fix direction (for PLANTPOTTING-0004):**

```kotlin
// MainActivity.kt
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId

setContent {
    PlantPottingTheme {
        @OptIn(ExperimentalComposeUiApi::class)
        Box(modifier = Modifier.semantics { testTagsAsResourceId = true }) {
            PlantPottingNavHost()
        }
    }
}
```

After the fix, re-run `pwsh ./scripts/integration-flow.ps1` (which now needs Bug 1 fixed too) to capture the §7.5 cold + warm transcripts that PLANTPOTTING-0003 §8.5 couldn't deliver.

**Impact on shipped acceptance:**
- §7.5 cold + warm transcripts — still uncaptured (was already deferred to PLANTPOTTING-0004, but now we know it's not just B1; the script needs Bug 2 fixed too).
- §8.5 device-aware integration script line — would not have passed even with Bug 1 fixed.
- §8.5 build-only line — still green (it doesn't touch the device).

---

## UX issues

None observed in this review pass — the runtime errors in Bugs 1+2 prevent reaching most UX surfaces.

One minor observation worth carrying forward: when `CameraUiState.Failure` fires, the error text is rendered at TopCenter against a dark camera preview with default white text — it's legible but easy to miss (the user described "nothing happens" after the shutter, when in fact a multi-line error message was on screen). PLANTPOTTING-0004 could surface failures more prominently — a Snackbar / Material error banner, or even a navigation to a dedicated `CaptureFailedScreen` with a retry CTA.

---

## Missing features

None — the sprint plan's §3.1 must-land list is structurally complete. The issue is that two pieces of plumbing (preprocessor↔model input contract, Compose↔uiautomator bridge) were assumed to work but never end-to-end-verified against a real emulator.

---

## Notes for next sprint

**Suggested PLANTPOTTING-0004 framing: "fix sprint for PLANTPOTTING-0003" (same shape as PLANTPOTTING-0002 was for 0001).** Fix Bug 1 + Bug 2 + capture the §7.5 transcripts + add the missing real-model instrumentation test (Bug 1 root cause). Then schedule confidence calibration + UI polish (the originally planned PLANTPOTTING-0004 topic) into PLANTPOTTING-0005.

**Test gap to close in PLANTPOTTING-0004 planning:**

The whole-pipeline test gap that allowed Bug 1 to ship is the most important meta-finding. The Hilt swap to `FakeFixedIdentifier` for instrumentation + the fake `InterpreterFacade` for unit tests gives us 100 % control-flow coverage of the identifier with zero coverage of the **real native interpreter against the real model**. PLANTPOTTING-0004 should add a single `@HiltAndroidTest` that does NOT swap the production binding — it loads the real `.tflite`, decodes a checked-in fixture JPEG, calls `identifier.identify(bytes)`, and asserts success. That one test would have caught Bug 1 immediately.

**Carry-forward from PLANTPOTTING-0003 results doc that survives:**
- LowConfidenceFlowTest instrumentation (B3 — deferred per §6.3)
- INT8 quant variant / KbToModelCoverageTest stretch (§3.2 — deferred)
- GPU/NNAPI delegate stretch (§3.2 — deferred)
- Re-enable PermissionDeniedFlowTest (from PLANTPOTTING-0001, still `@Ignore`)
- The `LowConfidencePicker.testTag` chip-row mockup in the sprint plan §6.4 assumed two top candidates would render; in practice (per the manifest's `_comment_coverage`), the AIY V1 vocabulary contains only 2 of our 16 KB species verbatim, so most captures will route to an empty-candidates picker. Worth confirming the empty-candidates branch renders well in PLANTPOTTING-0004's UI polish.

---

## Artefacts saved here

- `diag-step2-camera.png` — screenshot of camera screen pre-shutter
- `diag-step2-camera-dump.xml` — uiautomator dump showing empty `resource-id` attributes (Bug 2 evidence)
- `diag-step3-after-shutter.xml` — uiautomator dump showing the `TFLite inference failed: …` Text on screen (Bug 1 evidence)
- `../../evidence/PLANTPOTTING-0003/test-checklist.md` — the walked-through review checklist; Steps 4–10 are blocked on Bugs 1+2
- `../../evidence/PLANTPOTTING-0003/transcript-A-cold.txt` — cold-launch script transcript ending in `Wait-ForNode camera.shutter` timeout (Bug 2 evidence)
