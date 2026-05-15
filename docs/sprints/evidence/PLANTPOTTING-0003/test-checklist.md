# PLANTPOTTING-0003 — Review test checklist

Tick `[x]` as each step passes; `[F]` on failures (drop a short note below the step). Findings here graduate to `docs/sprints/feedback/PLANTPOTTING-0003/feedback.md` at the end of review.

Acceptance bars cross-referenced to the sprint plan §2.1 user flow.

---

## Step 1 — Boot Pixel_6_API_34 emulator

- [x] Emulator boots; `adb devices` lists `emulator-5554  device`
- [x] `sys.boot_completed = 1` returned (cold-boot reported 45,885 ms)

Commands:
```pwsh
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -avd Pixel_6_API_34 -no-snapshot-load
# (in a second terminal)
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" wait-for-device
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell getprop sys.boot_completed
```

**Visual — emulator lock screen** (expected once booted):

```
+-----------------------------+
|   :: emulator-5554 ::       |
|   Pixel 6 API 34            |
|                             |
|         12 : 34             |
|         May 15              |
|                             |
|       [swipe up to unlock]  |
|                             |
+-----------------------------+
```

Notes:

---

## Step 2 — Cold-launch transcript A (§7.5)

- [F] **FAILED** — but for a different (and more interesting) reason than predicted: script timed out at `Wait-ForNode -resourceId "camera.shutter"` after 12 dumps. The shutter button **is** on screen (UI dump shows a Button at `[446,2022][635,2211]` with `content-desc="Capture plant photo"`) but every Compose node has `resource-id=""`. Root cause: `testTagsAsResourceId = true` is missing from the root composition in `MainActivity.kt`. Without that opt-in, `testTag("camera.shutter")` never bridges to `android:id` in uiautomator dumps. PLANTPOTTING-0001's device-aware integration never actually passed — it was masked by the now-fixed null-root race. Carry-forward to PLANTPOTTING-0004.
- [x] `docs/sprints/evidence/PLANTPOTTING-0003/transcript-A-cold.txt` exists (timeout trace recorded)
- [x] Camera UI dump preserved at `docs/sprints/feedback/PLANTPOTTING-0003/diag-step2-camera-dump.xml`

**Evidence:** the dump's first interactable node tree:
```
<node class="android.view.View" resource-id="" bounds="[446,2022][635,2211]">
  <node content-desc="Capture plant photo" resource-id="" bounds="[509,2085][572,2148]" />
  <node class="android.widget.Button"      resource-id="" bounds="[446,2022][635,2211]" />
</node>
```

Notes: continuing manual flow at Steps 3–8 (manual tap doesn't need resource-ids). Step 9 (warm transcript) will fail the same way — skipping unless we want a duplicate failure trace.

---

## Step 3 — Manual: shutter completes without crash (§2.1.1)

- [x] Camera screen renders with shutter button
- [F] **FAILED** — shutter capture triggers `OnDevicePlantIdentifier`, which throws because the real INT8 AIY V1 model expects a UINT8 input tensor but `ImagePreprocessor` emits FLOAT32. The user-visible failure text (rendered at TopCenter of CameraScreen): `"TFLite inference failed: Cannot convert between a TensorFlowLite tensor with type UINT8 and a Java object of type [[[[F (which is compatible with the TensorFlowLite type FLOAT32)."`
- [x] No ANR / crash / "App keeps stopping" dialog — failure path runs cleanly through `CameraUiState.Failure`, but the user is stranded on the camera screen
- [x] Failure UI dump preserved at `docs/sprints/feedback/PLANTPOTTING-0003/diag-step3-after-shutter.xml`

Commands:
```pwsh
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell am force-stop com.darkfactory.plantpotting
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell am start -n com.darkfactory.plantpotting/.MainActivity
```

**Visual — Camera screen**:

```
+-----------------------------+
| Plant Potting               |
+-----------------------------+
|                             |
|   ::: live preview :::      |
|   (AOSP virtual scene)      |
|                             |
|                             |
|                             |
|                             |
|         (   O   )           |
|         shutter             |
+-----------------------------+
```

Notes:

---

## Step 4 — Manual: source-driven badge text (§2.1.2 / §5.1)

**BLOCKED on Step 3 bug** — the ResultScreen is unreachable from a real shutter capture. The badge logic is exercised by `ResultScreenBadgeTest` (Compose-UI test, green) but cannot be visually verified in production until the UINT8/FLOAT32 mismatch is fixed.

After Step 3 lands you somewhere, check what badge is shown.

- [ ] If on `ResultScreen`: badge reads **"On-device match"** or **"On-device match (low confidence)"**
- [ ] Badge does **NOT** read "Stub identifier — replace in a later sprint"
- [ ] If on `LowConfidencePicker`: noted — badge gets tested at Step 6 after picking

**Visual — Result screen (high confidence, expected for Monstera)**:

```
+-----------------------------+
|  Monstera deliciosa         |
|  (Swiss cheese plant)       |
|                             |
|  [ On-device match ]        |
|                             |
|                             |
|  [   See potting mix   ]    |
|                             |
+-----------------------------+
```

**Visual — Result screen (low confidence, after picker pick)**:

```
+-----------------------------+
|  Monstera deliciosa         |
|  (Swiss cheese plant)       |
|                             |
|  [ On-device match          |
|    (low confidence)      ]  |
|                             |
|  [   See potting mix   ]    |
|                             |
+-----------------------------+
```

**Visual — Result screen (FAIL — stub copy showing means UX2 not delivered)**:

```
+-----------------------------+
|  Monstera deliciosa         |
|  (Swiss cheese plant)       |
|                             |
|  [ Stub identifier —        |
|    replace in a later       |
|    sprint               ]   |  <-- FAIL: §2.1.2 regressed
|                             |
+-----------------------------+
```

Notes:

---

## Step 5 — Manual: LowConfidencePicker layout (§2.1.4 / §6.4)

**BLOCKED on Step 3 bug** — LowConfidencePicker unreachable from a real shutter capture. The picker is exercised by `LowConfidencePickerScreenTest` + `LowConfidencePickerViewModelTest` (both green); production layout cannot be visually verified until the UINT8/FLOAT32 mismatch is fixed.

If Step 3 routed to the picker, verify shape. (Skip this step if you landed on ResultScreen instead.)

- [ ] Headline: "We couldn't identify your plant confidently."
- [ ] "Did you mean…" row with up-to-3 chips (chip text: `Common name (NN%)`)
- [ ] Search field labelled "Search species"
- [ ] Below: list of KB species (italic scientific name + common name underneath)
- [ ] Bottom: button **"I don't know — pick by archetype"**

**Visual — LowConfidencePicker**:

```
+--------------------------------------+
| We couldn't identify your plant      |
| confidently.                         |
|                                      |
| Did you mean…                        |
|  [ Swiss cheese plant (72%) ]        |
|  [ Jade plant (18%)         ]        |
| ------------------------------------ |
| ┌──────────────────────────────┐     |
| │ Search species               │     |
| └──────────────────────────────┘     |
|                                      |
|  Monstera deliciosa                  |
|    Swiss cheese plant                |
|  Epipremnum aureum                   |
|    Pothos                            |
|  Philodendron hederaceum             |
|    Heartleaf philodendron            |
|  ...  (16 total)                     |
|                                      |
|  [  I don't know — pick by archetype  ]
+--------------------------------------+
```

If the top-3 row is missing/empty: that's expected for the AOSP virtual scene (no mappable label was in the top-3 with non-null mapping). Note it.

Notes:

---

## Step 6 — Manual: search → result with low-conf badge (§2.1.4)

**BLOCKED on Step 3 bug** — search path is downstream of the picker, which is unreachable from a real capture. Covered by `LowConfidencePickerViewModelTest` (filter logic) + `LowConfidencePickerScreenTest` (tap routing).

- [ ] Type "monstera" in the search field; list filters to Monstera entries
- [ ] Tap Monstera deliciosa row
- [ ] Lands on ResultScreen with title "Monstera deliciosa"
- [ ] Badge reads **"On-device match (low confidence)"**
- [ ] "See potting mix" button visible

**Visual — search filter active**:

```
+--------------------------------------+
| ...                                  |
| ┌──────────────────────────────┐     |
| │ Search species: monstera     │     |
| └──────────────────────────────┘     |
|                                      |
|  Monstera deliciosa     <-- tap me   |
|    Swiss cheese plant                |
|  Monstera adansonii                  |
|    Swiss cheese vine                 |
|                                      |
+--------------------------------------+
```

Notes:

---

## Step 7 — Manual: archetype CTA → recommendation (§2.1.4 / §6.7)

**BLOCKED on Step 3 bug** — ArchetypePicker is downstream of LowConfidencePicker. Covered by `ArchetypePickerScreenTest` + `RecommendationEngineArchetypeTest` (both green).

From the picker (re-launch app if you've left it):

- [ ] Tap "I don't know — pick by archetype"
- [ ] Lands on ArchetypePicker; 8 archetype rows visible (name + 1-line description)
- [ ] Tap "Aroid chunky"
- [ ] Lands on RecommendationScreen with a recipe summing to 100
- [ ] Rationale text does **NOT** name a species

**Visual — ArchetypePicker**:

```
+--------------------------------------+
| Pick a substrate archetype           |
|                                      |
|  Aroid chunky                        |
|    Bark-heavy, fast-draining mix...  |
|                                      |
|  Aroid balanced                      |
|    All-purpose for climbing aroids...|
|                                      |
|  Succulent gritty                    |
|    Mineral-heavy, near-zero peat...  |
|                                      |
|  ... (8 total)                       |
+--------------------------------------+
```

**Visual — RecommendationScreen (archetype variant)**:

```
+--------------------------------------+
| Aroid chunky                         |
|                                      |
| Bark chunks       40 %               |
| Coco coir         25 %               |
| Perlite           20 %               |
| Sphagnum moss     10 %               |
| Worm castings      5 %               |
|                   ----                |
|                  100 %               |
|                                      |
| Rationale: Aroids prefer chunky,     |
| airy substrate with high drainage... |
+--------------------------------------+
```

Notes:

---

## Step 8 — Cold-result timing (§2.1.3)

**BLOCKED on Step 3 bug** — capture-to-result timing cannot be measured because the result screen is unreachable. Observed shutter-to-failure time was sub-second (close to instant) — the type mismatch fires from `Interpreter.run` on the very first invocation, before any meaningful inference latency could be measured.

- [ ] ~~Stopwatch shutter → render~~
- [ ] ~~≤3 s~~

To force cold:
```pwsh
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell am force-stop com.darkfactory.plantpotting
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell am start -n com.darkfactory.plantpotting/.MainActivity
```

Observed ms: _____

Notes:

---

## Step 9 — Warm transcript B (§7.5)

**SKIPPED** — would fail identically at Step 2's `Wait-ForNode camera.shutter` (testTagsAsResourceId regression). No new information from a duplicate failure trace.

- [ ] ~~Script run completes~~
- [ ] ~~`transcript-B-warm.txt`~~

```pwsh
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell am force-stop com.darkfactory.plantpotting
pwsh ./scripts/integration-flow.ps1 *> docs/sprints/evidence/PLANTPOTTING-0003/transcript-B-warm.txt; $LASTEXITCODE
```

Notes:

---

## Step 10 (optional) — GMD `pixel6Api34DebugAndroidTest` (§8.5)

**SKIPPED** — instrumentation tests pass because they use `FakeFixedIdentifier` (Hilt-swapped via `TestIdentifyModule`), not the real `OnDevicePlantIdentifier`. A green GMD run would not have surfaced the UINT8/FLOAT32 mismatch — the bug only fires against the real interpreter on a real model. Recording this here so the next sprint planner doesn't treat GMD-green as evidence that production works.

- [ ] ~~`./gradlew --no-daemon pixel6Api34DebugAndroidTest`~~

---

## Summary

| Bar | Pass/Fail | Note |
|---|---|---|
| §2.1.1 flow completes, no crash | **FAIL** | Bug 1 — INT8/FLOAT32 type mismatch breaks every real capture |
| §2.1.2 badge no longer "Stub identifier" | **N/A** | Result screen unreachable from real capture; unit tests green |
| §2.1.3 cold ≤3 s | **N/A** | Capture fails sub-second on type mismatch |
| §2.1.4 picker shape | **N/A** | Picker unreachable from real capture; unit tests green |
| §2.1.4 manual search route | **N/A** | Downstream of picker |
| §2.1.4 archetype CTA route | **N/A** | Downstream of picker |
| §7.5 cold transcript captured | **FAIL** | Bug 2 — `testTagsAsResourceId` not enabled on root composition |
| §7.5 warm transcript captured | **SKIP** | Would fail identically to cold |

**Two bugs to carry forward to PLANTPOTTING-0004 (both in `feedback.md`):**
1. **INT8 model fed FLOAT32 tensor** — sprint headline (real on-device ML) is non-functional in production. Unit tests passed because they use a fake `InterpreterFacade`; the real native interpreter only runs in instrumentation, which is Hilt-swapped to a fake identifier.
2. **`testTagsAsResourceId` missing** — Compose `testTag()` doesn't bridge to `android:id` in uiautomator dumps, so the integration-flow script can never find any node by `resource-id`. PLANTPOTTING-0001's device-aware path never actually passed — it was masked by Bug A's null-root race, which we fixed in Phase 7, now this surfaces.
