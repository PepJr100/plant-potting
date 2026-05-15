# PLANTPOTTING-0004-CLAUDE critique of CODEX and GEMINI drafts

Self-position: I am the CLAUDE draft. I am critiquing the other two against my own. I'm trying to surface where each is sharper than me, where each is weaker, and what the merge should cherry-pick.

---

## A. Critique of CODEX

### A.1 What is stronger than my draft

- **The Phase 0 RED-first contract lock is much sharper.** Codex's §0.4–§0.12 commits twelve concrete failing tests (manifest dtype schema, UINT8 manifest value, no-UINT8-normalisation, preprocessor UINT8 branch, preprocessor FLOAT32 branch retained, uiautomator resource-id bridge, real-model production binding, real-model inference, manifest-vs-interpreter dtype). My §0.4–§0.5 only lock two. The Codex shape better matches the 0003 §0.4 contract-lock discipline. In particular Codex's `RealModelPlantIdentifierSmokeTest.productionPlantIdentifierBindingIsOnDevicePlantIdentifier` as an explicit RED test is genuinely good design — it forces the implementer to *observe* that the binding swap is in effect before changing it.
- **§3.3 confronts the `TestIdentifyModule` de-scope reason head-on.** Codex's plan is "delete the global `@TestInstallIn` swap, convert every existing instrumentation test to a per-test `@BindValue` fake" — and §3.2–§3.4 explicitly enumerates the affected tests (`EndToEndFlowTest`, `CameraScreenSmokeTest`, `CameraScreenBindStateTest`, `CameraPreviewLayoutTest`, `PermissionResumeRecoveryTest`, `PermissionDeniedFlowTest`). This is the architecturally clean fix. Mine sidesteps the swap with a "inject the concrete class" trick that *should* work but is more clever than it is robust. Codex's approach permanently removes the gap and is strictly more general.
- **§3.2 catches the redundant `RecommendationScreen` local opt-in.** Codex notices that `RecommendationScreen.kt:22-27`, `:33`, `:46-50` already carry per-screen `testTagsAsResourceId` wrappers and removes them after the root bridge passes. My draft missed this cleanup entirely. After my fix lands, `app/src/main/` would have two opt-in surfaces (root + Recommendation) doing the same job. Codex catches this; I don't.
- **§2.6 adds reader validation for bad dtype values** ("throws a useful error for any `input_dtype` outside `uint8`/`float32`"). This is a small thing but mine doesn't have it — my reader silently defaults to FLOAT32 for back-compat. Codex is stricter and that's correct: a typo'd `"input_dtype": "uin8"` should fail fast at load time, not silently route to the FLOAT32 path and produce the mirror-image of Bug 1.
- **§4.6 adds `transcript-C-buildonly.txt`.** I have only cold + warm; Codex captures the build-only regression as a third artefact. Cheap, and it differentiates "device path broken" from "build path broken" in any future feedback loop.
- **§Risk-output-tensor-dtype is sharper.** Codex's risk "output tensor dtype also differs" is more specific than my §7.4. It says "while adding the interpreter dtype assertion, log or assert output tensor 0 dtype if easy. If output is not FLOAT32, extend `TfLiteInterpreterFacade.runInference` in the same sprint; do not leave a second native dtype mismatch." That last clause is the right judgement call — discovering a second UINT8 contract on the output during this sprint and deferring it would be a planning failure.

### A.2 What is weaker than my draft

- **Decision §3.4 leaves the `PreprocessedImage` shape unresolved.** Codex says "carry `inputDType` plus either UINT8 bytes or FLOAT32 values" — that's a sealed-type sketch with no concrete commitment. §2.8 says "change `PreprocessedImage.kt:10-14` so it can represent UINT8 bytes and FLOAT32 values without ambiguous field names like `normalisedRgb` for raw bytes" — that's a description of a problem, not a solution. §2.12 says "passed to `Interpreter.run` as a direct `ByteBuffer` or other TFLite-compatible UINT8 object" — *or other* is hand-waving. My §4.4 commits to a single `ByteBuffer` field and gives the blast radius (~80 lines, six files). Codex doesn't have an equivalent commitment, which means an implementer hits this decision point mid-sprint with no policy backing.
- **No UINT8 output dequantisation plan.** This is the bigger of the two omissions. AIY V1/3 is fully quantised — the **output** tensor is UINT8, not FLOAT32. My §1.9 specifies "declare `val output = Array(1) { ByteArray(labelCount) }` instead of `FloatArray`. After `run`, dequantise: read `interpreter.getOutputTensor(0).quantizationParams()` (scale + zeroPoint); convert byte-by-byte to a FloatArray." Codex's §2.12 says "while FLOAT32 inputs keep a compatible float path" — *input* only. Without output dequantisation, the call will still throw the mirror error on output binding. The §Risk-output-tensor-dtype mention catches this conceptually but the task list doesn't budget for it.
- **§Risk-output is in the Risks section, not in the task list.** That means an implementer who reads top-to-bottom hits §2.12 first, writes the float-only output path, ships it, and only on running the GMD discovers the output is UINT8 too. The risk note doesn't have a paired §2.x task. Mine puts the dequantisation directly in §1.9.
- **The big `TestIdentifyModule` delete is more change than a fix sprint should carry.** Six instrumentation test files (`EndToEndFlowTest`, `CameraScreenSmokeTest`, `CameraScreenBindStateTest`, `CameraPreviewLayoutTest`, `PermissionResumeRecoveryTest`, `PermissionDeniedFlowTest`) all get `@BindValue` rewrites. That's a meaningful refactor of the test suite — six places where a Hilt-binding edit could subtly break Test order, scope, or @JvmField visibility. Mine touches one new test file and zero existing ones. Codex's cleaner-architecture-but-more-blast-radius trade is real; for a "narrow fix sprint matching the 0002 shape" (Codex's own framing in line 4), it's an over-spend.
- **No explicit Phase 1 vs Phase 2 parallelism.** My §6 sequencing diagram has Phase 2 (Bug 2 fix) explicitly parallel with Phase 1 (Bug 1 fix). Codex's §5 lists Phase 1 → Phase 2 → Phase 3, suggesting they are serial. Phase 2 in Codex's plan is the Bug 1 manifest/preprocessor work — and Phase 1 is the testTag bridge. So actually Codex *does* have Bug 2 first, and then says "Phase 1 can land before Phase 2" in the dependency list. Reasonable, but it's worth saying Phase 1 (testTag bridge) and Phase 2 (preprocessor) can run **in parallel** — they touch disjoint files.
- **§Risk-removing-TestIdentifyModule mitigation is "do it one file at a time."** That's not really a mitigation, that's "do the work carefully." A real mitigation is "if any of the six gets stuck, fall back to my approach (inject concrete class for the new test only, leave TestIdentifyModule in place)." Codex doesn't have a fallback path.

### A.3 What tasks are missing entirely

- **No task to dequantise the UINT8 output tensor.** Treated as a risk only, not budgeted as task work.
- **No load-time dtype-mismatch unit test.** Codex's §3.10 covers manifest-vs-interpreter dtype agreement at instrumentation level, but there's no JVM-level test (mine §1.8 has `loadTimeDtypeMismatchThrowsIdentificationFailure`). The instrumentation path requires a device; the JVM path catches it on every `testDebugUnitTest`.
- **No nice-to-have list at all.** Codex's §2.3 "if must-land chain is green early" is one line. Mine has §3.2 nice-to-haves (blank/grey fixture, observed probabilities, Compose-UI Robolectric semantics test). These are explicit drop-points for the de-scope ladder.
- **No discussion of the AIY V1 `_comment_coverage`** (2 of 16 KB species mapped). The new real-model test may land in the low-conf path on a random Monstera fixture — my §3.2 mentions this as a soft assertion ("No assertion on speciesId"), Codex's §3.8 just says "`result.source == IdSource.ON_DEVICE_MODEL`" with no acknowledgement that the test may legitimately route low-conf.

### A.4 What risks are underweighted or ignored

- **§Risk-removing-TestIdentifyModule under-weights the blast radius.** Removing a globally-installed test module and rewriting six tests is a non-trivial refactor; "one file at a time" doesn't shrink that. The risk that one of these tests has a subtle dependency on the eager swap (e.g., a `@Singleton`-scoped fake that survives across tests in the run, or an `Activity#onCreate` that injects `PlantIdentifier` before `hiltRule.inject()` fires) is real and not addressed.
- **§Risk-real-model-native-load mitigation says "treat as a sprint blocker, not a de-scope."** That's correct but doesn't say what to do if the GMD x86_64 image rejects the TFLite native lib. My §7.3 punts the same way, so this is a shared weakness — but Codex's "this is a blocker" stance is harder than mine.
- **No risk for "removing the global module breaks one of the six tests in a non-obvious way."** This is the most likely failure mode of the §3.1 plan.

### A.5 What sequencing is wrong

- **§Hard-gate ordering is fine but doesn't preserve the §0 contract-lock-first pattern.** Mine §6.1 says "§0.4, §0.5 RED before §1.2 / §1.5". Codex §Hard-gates lists five hard gates but they're scattered (`ModelManifestTest.manifestDeclaresSupportedInputDtype` before `model_manifest.json` edit; `MainActivityResourceIdBridgeTest.cameraShutterTestTagIsExportedAsResourceId` before `MainActivity.kt` edit; etc.) — fine in spirit, but mine groups them under §0 (contract locks committed before any work) which is the 0003 discipline.
- **Phase 1 (testTag bridge) before Phase 2 (preprocessor) is fine but should be explicit-parallel, not serial.** As above.
- **§3.1 (delete `TestIdentifyModule`) before §3.2 (add local fakes) is the wrong order.** If you delete the global swap first and run the suite, six tests fail simultaneously. Better: add the per-test `@BindValue` to every consumer first (each test stays green because both bindings produce the same fake), *then* delete the global. Codex's §3.1 → §3.2 ordering is "delete then patch," which produces a transient broken state across six tests.

---

## B. Critique of GEMINI

### B.1 What is stronger than my draft

- **Brevity.** Gemini is ~130 lines vs my 400. For a small surface-area fix sprint, the brevity is genuinely a strength — easier to skim, easier to hold in head. My draft trades brevity for completeness.
- **§3.3 explicitly rejects a new gated source set with a clean rationale.** "A single `@HiltAndroidTest` is sufficient and keeps the CI pipeline simple." Same conclusion as me, but expressed more crisply.
- **§Phase 4 expected-manifest-update bullet is good.** "Update `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` if the on-device badge string or other fields have changed" — same point as my §4.4 but expressed in one line instead of four.

### B.2 What is weaker than my draft

- **§4 Phase 1's PreprocessedImage decision is left open.** Verbatim from line 69: "*Decision:* Keep `PreprocessedImage` returning `FloatArray` for now IF the interpreter facade handles the cast, OR update `PreprocessedImage` to be a `TensorBuffer` wrapper." That's not a decision, that's a non-decision dressed as one. An implementer hits this mid-sprint with no architectural backing — they'll make the call themselves, with no opinion to point at. My §4.4 commits to ByteBuffer with a documented blast radius; Codex at least has a sealed-type sketch.
- **§6 Phase 3 says "Do NOT use @UninstallModules" but never says what to do instead.** That's the entire de-scope reason for B2 in 0003 — and Gemini's plan doesn't address it. There is no mechanism for getting the real `OnDevicePlantIdentifier` to inject in the presence of `TestIdentifyModule`'s `@TestInstallIn(replaces = [OnDeviceIdentifyModule::class])`. This is the single most important question in the sprint, and Gemini punts it.
- **`result.scientificName` is wrong.** §6 Phase 3: "Assert: No exception, `result.source == IdSource.ON_DEVICE_MODEL`, `result.scientificName` is non-null." `IdentificationResult` has `speciesId, displayName, source, lowConfidence` — there is no `scientificName` field. The test as written wouldn't compile. (This is a minor fix but it signals the author didn't read the seam file before writing the test.)
- **No baseline Phase 0.** Jumps straight to Phase 1. No contract-lock pattern, no RED-first discipline mirroring 0003's §0.4. The closest Gemini gets is "Contract Test (RED)" sub-bullets inside each phase — but no phase-zero gate that says "do these RED tests before any production change."
- **§3.1 manifest-driven rationale is thin.** "The manifest is our central contract for model metadata. Adding `input_dtype` allows the `ImagePreprocessor` to be configured at construction time." True, but doesn't engage with the alternative (runtime introspection). My §4.1 and Codex's §3.1 both reject runtime introspection with reasons; Gemini just asserts the chosen approach.
- **No discussion of UINT8 output tensor dequantisation.** Same omission as Codex but worse — at least Codex flags it as a risk. Gemini's plan would silently produce the mirror error on output binding.
- **Risk table is four entries. No depth.**
  - "UINT8 model still throws after fix — Mitigation: Verify `Interpreter.run` input buffer alignment and size; use `TensorImage.buffer` directly if `floatArray` cast is the bottleneck." This is debugging advice presented as a mitigation. A real mitigation is "before this risk fires, do X" — Gemini's is "if this risk fires, debug it." Not a mitigation.
  - "GMD image drifts — Stick to `pixel6Api34` as the anchor image." Tautological.
  - No risk for the `TestIdentifyModule` swap interaction. No risk for `FakeInterpreterFacade` divergence from the new dtype branch. No risk for sandbox filesystem overlay (per the auto-memory note).
- **§7 Acceptance criteria is six bullets.** Mine has thirty across §8.1–§8.7; Codex has eighteen. Gemini's acceptance doesn't include `verifyNoNetworking`, doesn't include the contract-test green pin, doesn't include the falsifiability check, doesn't include the seam-invariants pin, doesn't include the ledger close gate.
- **No de-scope order.** If the implementer slips, Gemini doesn't say what to drop. Mine §6.2 has a four-step ladder.
- **No falsifiability proof for the real-model test.** My §8.5 says "if the implementer reverts §1.7 on a feature branch, the new test fails with the Bug 1 error message — confirming the test would have caught Bug 1." Gemini has no analogue. The whole point of the new test is to prove that future Bug-1-shaped regressions get caught; the plan should specify how to prove that.
- **No mention of the `RecommendationScreen` local opt-in.** Same omission as me (Codex caught this; we both didn't).

### B.3 What tasks are missing entirely

- **All of Phase 0 (baseline + contract-lock-first).** No baseline command capture, no clean-`main` run, no RED-first contract tests committed *before* any production edit.
- **`TfLiteInterpreterFacade` load-time dtype assertion.** Gemini's Phase 1 doesn't mention asserting `interpreter.getInputTensor(0).dataType()` against the manifest at load time. That's my §1.9 and Codex's §3.12. Without it, a future FP16 swap that forgets `input_dtype` produces the mirror-image of today's Bug 1 silently.
- **UINT8 output tensor dequantisation.** As above.
- **Per-test `@BindValue` conversions or any mechanism for the real binding to inject.** As above.
- **A non-negotiable seam-invariant check** (`PlantIdentifier` interface unchanged, `IdentificationResult` fields unchanged, `StubPlantIdentifier` location unchanged). My §4.6, §8.2; Codex §1. Gemini just asserts these as goals in §1, never as acceptance criteria.
- **`check-stub-isolation.sh`.** Not in Gemini's acceptance criteria. (Mine §8.1, Codex's §5.2.)
- **No handoff note / implementer notes section with the de-scope ladder, parallelism guidance, or sandbox warning.** Mine §9 and Codex's §8.

### B.4 What risks are underweighted or ignored

- **No `TestIdentifyModule` interaction risk.** The de-scope reason from 0003 §B2 is the most important risk to surface, and Gemini's plan has no risk entry for it. (Mine §7.8; Codex §Risk-removing-TestIdentifyModule.)
- **No FakeInterpreterFacade divergence risk.** Mine §7.4; Codex §Risk-FakeInterpreterFacade. Gemini ignores.
- **No sandbox filesystem overlay risk.** Mine §7.7 (per the user's auto-memory). Gemini doesn't acknowledge.
- **No FP16-swap-forgets-input-dtype risk.** Mine §7.5; Codex §Risk-future-FP16. Gemini ignores.
- **No load-time dtype assertion risk.** The whole "future-proof" angle of the manifest-driven decision is absent.

### B.5 What sequencing is wrong

- **Phase 2 (Compose bridge) before Phase 1 (manifest/preprocessor) is backwards from Codex but matches mine in spirit (parallel).** Gemini's plan is actually serial — Phase 1, Phase 2, Phase 3 — but the two are disjoint and can run in parallel. Gemini doesn't say so.
- **§Phase 1 lacks RED-first contract locks.** "Contract Test (RED): Update `ModelManifestTest.kt` to expect `input_dtype` field" is the only RED-first task. Compare with my §0.4, §0.5, §1.1, §1.4, §1.6, §1.8 RED-first pattern. Mine and Codex both treat RED-first as the spine of the plan; Gemini sprinkles it.
- **`pwsh ./scripts/integration-flow.ps1` in Phase 2 (line 81) is premature.** Gemini suggests running it after the testTag bridge to confirm the first wait passes — but Phase 1 (Bug 1 fix) hasn't landed yet, so the script will fail past the shutter wait. This is sound diagnostically but should be flagged as "expected partial-pass," not just a verification step.
- **No phase-zero gate.** Production code edits can begin before any RED test is committed.

---

## C. Merge recommendation — keep, take, discard

**If I were merging, I'd …**

**KEEP from Codex:**
1. The twelve RED-first contract locks in §0.4–§0.12, especially `RealModelPlantIdentifierSmokeTest.productionPlantIdentifierBindingIsOnDevicePlantIdentifier` (asserts the binding is the production class, not the fake). This is a stronger discipline than my two-test §0.
2. The `RecommendationScreen.kt:22-27 / :33 / :46-50` cleanup of the redundant local `testTagsAsResourceId` opt-in (§1.3 in Codex). Mine misses this; after my fix, the project has two opt-in surfaces doing one job.
3. The reader validation that rejects bad `input_dtype` values (§2.6). Strictly better than my silent back-compat default.
4. The `transcript-C-buildonly.txt` capture (§4.6). Cheap, completes the evidence triangle.
5. The acceptance criterion phrasing "do not weaken expected-artifact requirements" (§4.8) — sharper than mine.
6. The phrasing of Risk-output-tensor-dtype: "do not leave a second native dtype mismatch" — promote this from a risk to a task (because it's true: AIY V1/3 output is also UINT8).

**KEEP from Gemini:**
1. The crisp brevity of decision sections. §3 fits in 16 lines and still names the rejected alternative for each decision. Mine is fluffier.
2. The §Phase 4 one-liner about updating the expected artifact if (and only if) the on-device badge string has actually changed. (Codex says this too, but Gemini's phrasing is tighter.)
3. … honestly that's about it from Gemini. Almost everything else is weaker than mine or Codex.

**DISCARD:**

1. **Gemini's `result.scientificName` assertion in §6 Phase 3.** The field doesn't exist on `IdentificationResult`. The test wouldn't compile.
2. **Gemini's "Decision: keep `PreprocessedImage` returning `FloatArray` for now IF the interpreter facade handles the cast, OR update `PreprocessedImage` to be a `TensorBuffer` wrapper"** (§4 Phase 1, line 69). That's a non-decision. The merged plan needs to pick a single concrete shape. (My §4.4: `data class PreprocessedImage(val width: Int, val height: Int, val buffer: ByteBuffer)`.)
3. **Gemini's "Do NOT use `@UninstallModules`"** without any replacement mechanism. The merged plan must say *how* the real binding gets injected — either my "inject the concrete class" trick (cheaper, less invasive) or Codex's "delete `TestIdentifyModule`, add `@BindValue` to six tests" (more general, more refactor).
4. **Gemini's risks table.** All four entries are either tautological or debugging advice. Replace with the union of my §7.1–§7.9 and Codex's risks.
5. **Codex's §3.1 → §3.2 ordering** ("delete `TestIdentifyModule` then add local fakes"). Reverse it: add per-test `@BindValue` to every consumer first (each test stays green during the transition), *then* delete the global swap. Otherwise the suite is broken across six tests for the duration of the refactor.
6. **My over-detailed §7 risks (7.1–7.9 with multi-paragraph mitigations).** Codex's risks are shorter and substantially equivalent. Trim to Codex's density.

**THE BIG CALL — Codex's full `TestIdentifyModule` removal vs my concrete-class injection trick.**

This is the merge's central question. Codex's approach is architecturally cleaner — it ends the source-set-wide fake-swap and makes the real binding default. Mine is surgically smaller — one new test file, zero edits to existing tests, two paragraphs of risk acceptance.

**Recommendation:** Take **my** approach for this sprint (concrete-class injection) and **carry Codex's full removal forward to PLANTPOTTING-0005**, alongside the confidence-calibration / UI-polish work that's already deferred there. Reasons:

1. PLANTPOTTING-0004 is explicitly "the fix sprint matching the 0002 → 0001 shape" — narrow, surgical. Adding a six-test refactor is shape drift.
2. My approach has a documented fallback (`@TestInstallIn(replaces = [TestIdentifyModule::class])` scoped to the one test) in §7.8 if the concrete-class injection assumption proves wrong.
3. Codex's §Risk-removing-TestIdentifyModule lacks a fallback — if any of the six tests breaks in a non-obvious way mid-sprint, the implementer has no rescue path that fits the sprint window.

**However:** if a merge audit considers Codex's approach the right architectural call, it must add my §7.8 fallback (per-test `@TestInstallIn` rebind) as the explicit rescue path for the six-test conversion. Otherwise the plan has a single point of failure.

**Final shape of the merge** should match my Phase structure (0/1/2/3/4/5 with explicit parallelism between Phase 1 and Phase 2) and acceptance density (§8.1–§8.7), but absorb Codex's contract-lock breadth, the `RecommendationScreen` cleanup, the reader validation, the build-only transcript, and a new task budget for UINT8 output tensor dequantisation that all three drafts currently underweight.
