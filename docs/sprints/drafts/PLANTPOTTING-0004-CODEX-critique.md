# PLANTPOTTING-0004 Codex Critique Of Other Drafts

This critique compares the Gemini and Claude drafts against `docs/sprints/drafts/PLANTPOTTING-0004-CODEX.md`, using the refined intent and PLANTPOTTING-0003 feedback as the source of truth. The merge should stay narrow: fix the UINT8 input contract, expose Compose tags to uiautomator, add one real-model gate, and capture the two device-aware transcripts.

---

## Gemini Draft

### 1. What is stronger than the Codex draft

- **Shorter implementation surface.** Gemini's Phase 1 / Phase 2 / Phase 3 / Phase 4 split is easier for an executor to scan than the Codex draft's larger task list. For a fix sprint, that concision is useful.
- **Phase 2 `Baseline Check (RED)` is operationally direct.** It says to run `adb shell uiautomator dump` before the fix and verify `resource-id=""`. Codex has a stronger automated `MainActivityResourceIdBridgeTest`, but Gemini's direct pre/post dump is closer to the actual failure mode in `diag-step2-camera-dump.xml`.
- **Acceptance requires multiple bridged screens.** Gemini's acceptance criterion "`resource-id` is visible in `uiautomator dump` for at least three distinct screens (Camera, Result, Recommendation)" is a good broad check. Codex names the important tags in goals, but Gemini's acceptance wording makes it harder to accidentally fix only `camera.shutter`.
- **It correctly picks the root `Box` shape.** Section 3.2 and Phase 2 choose `Box(modifier = Modifier.semantics { testTagsAsResourceId = true })` around `PlantPottingNavHost`, matching the intent and Codex's Decision 3.2.

### 2. What is weaker than the Codex draft

- **Bug 1 representation is under-specified and leaves a dangerous escape hatch.** Phase 1 says "Keep `PreprocessedImage` returning `FloatArray` for now IF the interpreter facade handles the cast, OR update `PreprocessedImage` to be a `TensorBuffer` wrapper." That ambiguity is exactly where the shipped bug lives. A UINT8 model should not be fed a fake "FloatArray of bytes." Codex is clearer that the internal representation must carry dtype and either raw UINT8 bytes or FLOAT32 values.
- **Manifest-driven dtype is named but not locked.** Section 3.1 says manifest-driven `input_dtype`, but Phase 1 only uses `inputDtype: String`. Codex requires an enum, validation of bad values, manifest-vs-interpreter assertion, and separate UINT8/FLOAT32 preprocessor tests.
- **The test fake is not updated to model dtype.** Gemini never says `FakeInterpreterFacade` or `PreprocessedImage` must record dtype / buffer shape. Without that, the unit path can remain blind to the contract that caused Bug 1.
- **The real-model Hilt plan ignores the existing `TestIdentifyModule` blocker.** Phase 3 says "Do NOT use `@UninstallModules(OnDeviceIdentifyModule::class)`" and inject `PlantIdentifier`, but the current `TestIdentifyModule` is a global `@TestInstallIn(replaces = [OnDeviceIdentifyModule::class])` provider for `FakeFixedIdentifier`. Injecting `PlantIdentifier` in `androidTest` would still resolve to the fake. Codex handles this by removing the global fake and moving fake identifiers to local `@BindValue` fields.
- **Bug 2 a11y risk is hand-waved.** Risk table says "Monitor TalkBack behavior," but the plan does not require checking the dump for `content-desc` / `text` leakage. Codex has Phase 1.5: verify `camera.shutter` appears only as `resource-id` while the shutter content description remains `Capture plant photo`.
- **Transcript mechanics are a command, not a credible execution plan.** Phase 4 says run the script twice and save transcripts, but does not address the sandbox-limited/live-emulator reality, cold vs warm setup details, manifest lines to verify, or build-only regression. Codex is more explicit about cold boot, force-stop warm run, expected manifest fields, and `-BuildOnly`.

### 3. What tasks are missing entirely

- A real-model test that proves the injected binding is **not** `FakeFixedIdentifier`.
- Removal of `TestIdentifyModule.kt` or an equivalent mechanism that prevents `@TestInstallIn` from auto-replacing the production interface binding.
- Local fake bindings for existing instrumentation tests (`EndToEndFlowTest`, camera tests, permission tests) after the global fake is removed.
- Manifest-vs-runtime input dtype assertion using `Interpreter.getInputTensor(0).dataType()`.
- Bad `input_dtype` validation.
- Explicit FLOAT32 branch retention tests for a future model swap.
- `FakeInterpreterFacade` / test fake updates that preserve and expose dtype-aware `PreprocessedImage`.
- Removal or explicit deactivation of the dead UINT8 normalization stanza. Gemini says normalization is "ignored" but does not force the manifest to stop implying FLOAT32 preprocessing.
- Accessibility dump verification that tags do not become spoken text or content descriptions.
- `pwsh ./scripts/integration-flow.ps1 -BuildOnly` transcript capture.

### 4. What risks are underweighted or ignored

- **The Hilt auto-replace issue is the biggest miss.** PLANTPOTTING-0003 already de-scoped the real binding test because of `@UninstallModules` / `@TestInstallIn` constraints. Gemini does not solve the actual graph problem.
- **The `FloatArray` fallback can recreate Bug 1 under a new name.** "Facade handles the cast" is not a safe plan unless the facade consumes a real UINT8-compatible object.
- **Output tensor dtype may also be quantized.** Gemini only says "Verify input buffer alignment and size." It does not mention output tensor dtype or dequantization. Codex at least flags this as a risk; Claude handles it more strongly.
- **Using a "real-world JPEG of a plant" can become an accuracy test accidentally.** Gemini asserts `scientificName` is non-null. The real gate should be "does not throw and source is `ON_DEVICE_MODEL`"; species correctness belongs to calibration work.
- **Device evidence can be deferred by accident.** Gemini says run scripts but gives no blocker policy if the emulator is unavailable. The intent says transcripts are acceptance, not a user-punted follow-up.

### 5. What sequencing is wrong

- **Phase 3 real-model test is placed after Bug 1 implementation and is not RED-first.** It should be added before or during the dtype fix so it demonstrates the current failure against the real interpreter. Gemini's `Instrumentation Test (RED/GREEN)` label is not enough because its Hilt setup would still inject the fake.
- **Phase 1 allows implementation before contract shape is decided.** The "FloatArray OR TensorBuffer" branch means implementers can start coding without a locked preprocessor/facade contract.
- **Bug 2 can run in parallel, but Gemini's Phase 2 script run says it may fail after capture until Phase 1 is complete.** That is fine operationally, but it should not be treated as evidence until Bug 1 is green. Codex's dependency rules are clearer.
- **Hard gates are too loose.** Gemini lacks explicit "RED before edit" gates for manifest dtype, UINT8 preprocessor branch, `MainActivity` bridge, production binding, and real inference.

---

## Claude Draft

### 1. What is stronger than the Codex draft

- **Output tensor dtype is treated as must-land.** Claude Scope 3.1 and Task 1.9 require dequantising a UINT8 output tensor via `quantizationParams()` before returning `FloatArray` scores. Codex only lists output dtype as a risk to check "if easy." Claude is stronger here; if AIY V1/3 is fully quantized, the input-only fix is incomplete.
- **`ByteBuffer` as the preprocessor/facade seam is cleaner.** Claude Decision 4.4 and Tasks 1.5 / 1.7 / 1.9 pick `PreprocessedImage(width, height, buffer: ByteBuffer)` and pass `TensorImage.buffer` directly to `Interpreter.run`. That is less ambiguous than Codex's "either UINT8 bytes or FLOAT32 values" representation and avoids inventing a pseudo-float UINT8 path.
- **Manifest plus runtime assertion is sharper.** Claude Decision 4.1 says manifest-driven policy plus load-time `getInputTensor(0).dataType()` assertion. Codex has the same direction, but Claude makes the facade assertion a must-land implementation item.
- **It directly addresses the PLANTPOTTING-0003 Hilt de-scope reason.** Decision 4.3 proposes injecting the concrete `OnDevicePlantIdentifier` instead of the `PlantIdentifier` interface, with a fallback in Risk 7.8 if that assumption fails. This is a useful alternative to Codex's heavier "delete global fake and convert tests to local `@BindValue`" plan.
- **Risk 7.8 is the right kind of risk.** It explicitly names the assumption behind concrete-class injection and gives a rescue path. Codex is more conservative, but Claude gives the merge step a real option.
- **Phase 2 is correctly parallelized.** Sequencing Section 6 shows Bug 2 can proceed independently from the model pipeline. Codex also says this, but Claude's diagram makes it very clear.

### 2. What is weaker than the Codex draft

- **Concrete `OnDevicePlantIdentifier` injection may not prove the production binding.** It proves the concrete class can be constructed and can run the real model. It does not prove the app's `PlantIdentifier` binding resolves to `OnDevicePlantIdentifier` in a non-test graph. Codex's `productionPlantIdentifierBindingIsOnDevicePlantIdentifier` assertion catches that explicitly after removing the global fake. If the merge keeps Claude's concrete injection shortcut, it should still add a binding assertion somewhere.
- **It keeps `TestIdentifyModule` in place.** That may be acceptable for the concrete-class smoke test, but it leaves the instrumentation source set globally fake-bound for all interface-injection tests. Codex's local `@BindValue` migration is more invasive but removes the class of mistake that hid Bug 1.
- **Task 1.3 defaults missing `input_dtype` to `FLOAT32`.** For the shipped manifest this is less safe than failing loudly. The whole sprint exists because metadata drift was missed; a missing field should be RED, not silently treated as FLOAT32 except in narrowly scoped in-memory test fixtures.
- **Task 1.2 preserves the normalization block as "editorial metadata."** The refined intent says drop the dead normalization stanza or document that it is unused; Codex prefers removal. Preserving it under the shipped UINT8 manifest is a future footgun unless the schema makes it impossible for UINT8 preprocessing to read it.
- **A11y leakage is asserted from theory.** Section 4.2 and Risk 7.2 say TalkBack reads content descriptions/text, not resource ids, and that a separate manual a11y smoke test is not worth the time. Codex is stronger because it requires the uiautomator dump to show `resource-id` without tag leakage into `text` or `content-desc`.
- **The new real-model test is not RED-first.** Phase 3.2 is labelled "(test)" but not RED-first, and Phase 3.3 only compiles it before GMD. Codex adds RED-first tests for production binding, real inference, and manifest-vs-interpreter dtype.
- **Fixture choice is unnecessarily heavyweight.** Task 3.1 requires a recognisable Creative-Commons Monstera and license file. Codex's tiny generated or neutral JPEG fixture is better for a smoke test whose assertion is "native inference accepts bytes," not "model recognizes this species."

### 3. What tasks are missing entirely

- An explicit `PlantIdentifier` interface binding assertion in the real-model instrumentation path if `TestIdentifyModule` remains.
- Conversion of existing fake-flow instrumentation tests to local `@BindValue` fakes. Claude intentionally avoids this, but then it should add a separate production-graph binding check outside the globally replaced test graph.
- A RED-first real inference task that is expected to fail with the current UINT8/FLOAT32 exception before the dtype implementation lands.
- Removal of redundant screen-local `testTagsAsResourceId` wrappers after the root bridge is added. Codex calls out `RecommendationScreen.kt`; Claude does not.
- A hard acceptance item that `model_manifest.json` no longer declares active FLOAT32 normalization for the shipped UINT8 model. Claude preserves it.
- Build-only transcript capture (`transcript-C-buildonly.txt`). Claude runs `integration-flow.ps1 -BuildOnly` in final verify but does not save it as evidence.
- Verification of specific expected manifest lines such as `manifest-mode=device-aware`, screenshot count, `source-badge=on-device match`, archetype, and recipe row count.

### 4. What risks are underweighted or ignored

- **Global fake binding remains a systemic risk.** Claude's concrete injection smoke test is clever, but future instrumentation tests that inject `PlantIdentifier` will still use `FakeFixedIdentifier` unless the module shape changes. That is exactly the blind spot from PLANTPOTTING-0003.
- **Defaulting missing dtype to FLOAT32 undercuts the manifest contract.** If a future model asset lands without the field, the tests may pass in paths that should fail. Codex's "supported dtype required" schema is safer.
- **`ByteBuffer` equality/test ergonomics are glossed over.** The `PreprocessedImageBufferTest` checks capacity, but the fake and tests also need a reliable way to inspect dtype and byte order/content. Codex's fake-compatibility task is more explicit about recording the full preprocessed input.
- **`MainActivityTestTagsAsResourceIdTest` may be a brittle proxy for uiautomator.** The real failure is platform XML output, not only Compose semantics. Claude does include a manual dump in 2.3, but its primary RED test is one level above the integration bug.
- **Transcript feasibility is acknowledged but not solved.** The handoff says "If one isn't available, surface to the user." That is honest, but the prompt asks for a credible plan on this sandbox-limited machine. Codex does not fully automate emulator provisioning either, but it gives a clearer set of capture commands and evidence files.

### 5. What sequencing is wrong

- **Phase 3 depends too strongly on Phase 1.** Claude says the real-model test depends on the buffer pipeline working. That misses the value of adding the test while still RED from the current Bug 1 exception. The test should be introduced before the implementation is considered done, even if it cannot pass until Phase 1 lands.
- **Ledger `in-progress` update in 0.3 is premature for a draft merge.** It is execution bookkeeping, not a sprint-plan task. Codex waits until acceptance to update the ledger done state.
- **The `PreprocessorFacadeSeamContractTest` in 0.5 locks a chosen implementation before behavior.** RED-first is good, but reflection-locking `ByteBuffer` can make the plan harder to adapt. The stronger behavioral gates are UINT8 capacity/content, no `NormalizeOp`, runtime dtype match, and real interpreter success.
- **Hard gates omit the production-binding concern.** Section 6.1 gates dtype and `MainActivity`, but there is no hard gate proving the real-model test is not accidentally using the fake interface binding.

---

## Merge Recommendation

If I were merging, I would **keep Claude's ByteBuffer seam, output dequantisation, and manifest-plus-runtime dtype assertion**. Those are stronger than the Codex draft and should be promoted to must-land.

I would **keep Gemini's concise phase framing and "resource-id visible on at least three screens" acceptance criterion**, but not its implementation detail.

I would **keep Codex's root `Box` bridge, a11y dump sanity check, RED-first discipline, build-only transcript evidence, and conservative handling of `TestIdentifyModule`**. If the merge chooses Claude's concrete-class injection shortcut, it still needs Codex's explicit production-binding assertion or another proof that the global fake cannot hide the real graph again.

I would **discard Gemini's `FloatArray` fallback, `PlantIdentifier` injection under the existing `TestIdentifyModule`, and vague transcript plan**. I would also **discard Claude's missing-dtype-defaults-to-FLOAT32 behavior and preserving the active normalization stanza for a UINT8 manifest**.
