# PLANTPOTTING-0005 - Codex Draft Critique

This critique compares the Codex draft against the Gemini and Claude drafts. The Codex draft is strongest on conservative scope control, realistic test wiring, and keeping `LowConfidencePicker` framed as the primary post-shutter route. It is weaker than Claude on executor-ready specificity, and weaker than Gemini on brevity and sprint readability.

## Gemini Draft

### Stronger Than Codex

Gemini's draft is much easier to scan. Its `Section 3 Task List` gets the sprint shape across quickly: `Infrastructure & Documentation`, `UI/UX Polish`, `Confidence Calibration`, and `Testing & Validation`. The Codex draft is more complete, but it is also heavier; Gemini would be easier for a reviewer to approve at a planning meeting.

Gemini also makes the five headline tracks very legible in `Section 1 Goals`: post-shutter polish, confidence calibration, infrastructure cleanup, test completion, and documentation. Codex says the same things, but spreads them across `Goals`, `Scope Boundaries`, `Working Decisions`, and eight phases.

The `Polish LowConfidencePickerScreen` task names a concrete user-facing affordance, `"None of these look like my plant"`, more directly than Codex's more conditional "Add an explicit 'not listed' or 'I do not know' affordance only if it routes to the existing archetype picker." Gemini's version is better product language, even if Codex is safer about route scope.

### Weaker Than Codex

Gemini under-specifies the `TestIdentifyModule` migration. It says "Migrate other instrumentation tests (total 6)" but only names `EndToEndFlowTest.kt`; Codex names the likely affected set: `EndToEndFlowTest`, `CameraPreviewLayoutTest`, `CameraScreenSmokeTest`, `CameraScreenBindStateTest`, `CameraScreenBoundStateTest`, `PermissionResumeRecoveryTest`, and `PermissionDeniedFlowTest`, with an explicit inventory step if the count differs. Gemini's task is too easy to execute partially.

Gemini's `LowConfidenceFlowTest.kt` task is too thin. Codex specifies the fake result shape, `mostRecentCandidates`, `ViewModelProbe.findCameraViewModel()?.onCaptureReady(...)`, assertions on `LowConfidencePickerTags.HEADLINE`, `TOP_ROW`, `SEARCH`, the `monstera-deliciosa` candidate, `ResultScreenTags.SOURCE_BADGE`, and the `RecommendationScreen` hop. Gemini only says "drives a flow that hits the `LowConfidencePicker` path."

Gemini's calibration assertion is risky: `OnDeviceModelRealInterpreterTest.kt` with "score for Monstera deliciosa > 0.6" commits to a threshold before measuring the real fixture. Codex is safer with "choose the stricter assertion only after measuring the actual model output" and a candidate-aware fallback.

Gemini's failure UI task allows "Snackbar or Material Banner" but does not require retry state assertions, shutter availability, failure tags, or a ViewModel test proving a new capture clears `CameraUiState.Failure`. Codex gives the executor more guardrails for regressions around retry behavior.

### Missing Tasks

Gemini is missing a baseline and inventory phase. There is no equivalent to Codex `Phase 0 - Baseline And Inventory`, especially the current-main gate run, `check-stub-isolation`, starting `pixel6Api34DebugAndroidTest` inventory, and the exact androidTest class list launched through `MainActivity`.

Gemini is missing preservation or update of existing test tags and scripts. Codex explicitly protects `LowConfidencePickerTags.candidateTag(...)`, `speciesTag(...)`, `SEARCH`, `SPECIES_LIST`, `PICK_BY_ARCHETYPE`, and `integration-flow.ps1`; Gemini does not mention tag compatibility.

Gemini is missing the `CameraScreenTags` task surface for failure UI. It says implement a Snackbar/Banner, but does not require tags like failure container, message, and retry action.

Gemini is missing the real-photo measurement evidence. It asks for a real CC-licensed photograph and an accuracy assertion, but does not require recording observed top-1 label, probability, mapped candidates, and route in the 0005 results evidence.

Gemini is missing stub-isolation verification and detailed evidence capture. It has a final gate chain, but no `bash scripts/check-stub-isolation.sh`, no phase outputs, no evidence directory, and no manual walk-through of camera -> low-confidence -> result -> recommendation.

### Underweighted Risks

Gemini underweights the risk that the six-test `@BindValue` count is wrong. Codex treats that as an explicit risk and requires inventory before migration.

Gemini underweights overfitting one fixture. Its mitigation for threshold sprawl says to "only tune the 2/18 species currently in the AIY vocabulary," but the project reality described in Codex is 2 of 16 KB species, and seeding tuned values in the first calibration sprint is more aggressive than Codex's "do not claim broad model accuracy" posture.

Gemini underweights `integration-flow.ps1` breakage from UI polish. Codex calls this out as a risk and mitigation; Gemini only says to run the script.

Gemini underweights the permanent-denial seam. It correctly says to use `FakeCameraPermissionGuard`, but it does not call out the risk that production code may lack a distinct open-settings UI state, which Codex covers in `Phase 5`.

### Sequencing Problems

Gemini sequences `ROADMAP.md` before the implementation work in `Phase 1: Foundation`. That is not wrong for a draft, but it risks writing a roadmap with aspirational post-0005 state before the sprint has measured the calibration result or confirmed the test migration. Codex's sequencing lets roadmap work happen before review, after the main facts are known.

Gemini puts UI polish before final instrumentation tests, which is fine, but it does not explicitly require `TestIdentifyModule` removal before `LowConfidenceFlowTest`. Codex is clearer that new instrumentation should be written in the post-migration pattern from the start.

Gemini places confidence calibration before `LowConfidenceFlowTest` and `PermissionDeniedFlowTest` closure in its sequence. I would close the deferred test and permission gaps before spending time on threshold scaffolding, because the sprint's "un-deferral" value depends on those tests not slipping again.

## Claude Draft

### Stronger Than Codex

Claude is much more executor-ready. The `Phase 1 - LowConfidencePicker polish` tasks name exact tags and strings: `LowConfidencePickerTags.SUBTITLE`, `R.string.low_conf_subtitle`, `LowConfidencePickerTags.NO_CANDIDATES_EMPTY`, and `LowConfidencePickerTags.SEARCH_EMPTY`. Codex asks for polished copy, search visibility, and routing tests, but does not decompose the empty-state work as sharply.

Claude's `CameraUiState.Failure polish` is more concrete than Codex. It names `CameraScreenTags.FAILURE_BANNER`, `CameraScreenTags.FAILURE_RETRY`, keeping `CameraScreenTags.ERROR` as a back-compat alias, a bottom-center `Card`, `Icons.Outlined.ErrorOutline`, and `TextButton("Try again")` calling `viewModel.reset()`. Codex gives the right behavior, but less of the implementation shape.

Claude's calibration sequence is stronger. `ModelScoreMapperPerSpeciesThresholdTest`, `perSpeciesOverrideUsedWhenPresent`, `globalThresholdUsedWhenNoOverride`, `per_species_thresholds`, the no-seeded-values rule, the probe run, and the post-probe assertion are all crisp. Codex has the same philosophy, but Claude makes the test names and exact RED/GREEN steps more usable.

Claude also has better audit criteria: grepping for `TestIdentifyModule`, `@Ignore`, and `testTagsAsResourceId`; final evidence files; `docs/sprints/results/PLANTPOTTING-0005.md`; ledger updates; and a sprint window estimate. Codex has good verification gates, but Claude has stronger close-out hygiene.

Claude's decisions section is valuable. The rejected alternatives for `LowConfidencePicker` full rebuild, `CameraUiState.Failure` Snackbar/new screen, `@BindValue` vs `@TestInstallIn`, and the `Map<String, Float>` threshold shape would prevent executor drift.

### Weaker Than Codex

Claude sometimes over-specifies implementation before code inspection. The `CameraUiState.Failure` task assumes `viewModel.reset()` is the right retry action and that a bottom-center `Card` with `padding(bottom = 144.dp)` will not overlap the shutter. Codex is less prescriptive: it requires retryability, tags, and no overlap, leaving room to adapt to the actual layout.

Claude's `LowConfidencePicker` copy is arguably less aligned with the "honest confidence language" goal. The proposed subtitle, "This model recognises a limited plant vocabulary - please confirm or pick below," exposes model limitation directly in user copy. Codex's working decision says to preserve honest confidence language but not over-technical phrasing; this should be a product-copy decision after seeing the screen.

Claude's "No production values seeded" rule for `per_species_thresholds` conflicts with Codex's acceptance-level desire to "add a test or manifest assertion that `Monstera deliciosa` has an explicit per-class calibration entry once the real fixture behavior is known." I think Codex is too eager to seed one entry, but Claude's blanket no-seeding rule also weakens the "begin calibration" goal unless the sprint explicitly defines scaffold-only calibration.

Claude's `TestIdentifyModuleAbsenceContractTest` is questionable. It proposes a JVM reflection test to prove an androidTest class does not exist, while admitting the JVM source set may not see the androidTest classpath. Codex's simpler audit and compile/run checks are less clever and more reliable.

Claude includes ledger and README mutation tasks (`Phase 6.2`, `Phase 6.3`, `Phase 7.7`, `Phase 7.8`) that may be inappropriate for a sprint plan draft if execution and review tooling own those files. Codex confines docs primarily to `docs/ROADMAP.md` and sprint evidence, which is less likely to create status churn before the sprint is actually done.

### Missing Tasks

Claude does not include Codex's `CameraScreenBoundStateTest` in the named six-test migration list. Claude names `EndToEndFlowTest`, `CameraPreviewLayoutTest`, `CameraScreenSmokeTest`, `CameraScreenBindStateTest`, `PermissionResumeRecoveryTest`, and `PermissionDeniedFlowTest`; Codex also calls out `CameraScreenBoundStateTest` during inventory. Claude does require a grep reconciliation in `0.4`, but the must-land list itself may mislead the executor.

Claude's `LowConfidenceFlowTest` fallback in `4.2` allows a minimal `lowConfidence = true` test with no candidates if `FakeFixedIdentifier` does not expose `mostRecentCandidates`. Codex's version insists on seeding at least `monstera-deliciosa` and asserting the candidate tap. Claude's fallback would leave the top-candidate manual-selection path less covered.

Claude does not require the small-screen Compose assertion from Codex `Phase 2`, where search and at least one species row must be reachable without layout overlap. Claude has empty-state tests but less explicit mobile layout protection.

Claude does not include Codex's manual verification task for permission granted -> camera -> shutter -> `LowConfidencePicker` -> picked species -> `ResultScreen` -> `RecommendationScreen` -> retake. Claude relies more heavily on automated gates.

Claude does not explicitly preserve the source badge behavior as a working decision. Codex states that low-confidence user picks still surface `on-device match (low confidence)`, which matters because UX polish could otherwise rename the source semantics.

### Underweighted Risks

Claude underweights the risk of over-testing implementation details. Tasks like `LowConfidencePickerSubtitleContractTest`, exact string resources, and exact tag names are useful, but they can turn a polish sprint into a brittle tag-and-copy contract before design has settled.

Claude underweights the risk that `LowConfidenceFlowTest` without candidates is not enough. Its fallback keeps the sprint moving, but it could let the most important low-confidence UX path remain untested while still claiming the flow test landed.

Claude underweights README and ledger churn. The tasks to update README current state, status table, ledger, and tick every plan checkbox can conflict with review workflow if 0005 execution is not actually complete or if the repository keeps sprint status elsewhere.

Claude underweights the risk that the `@Ignore` grep acceptance is too broad. `grep -R "@Ignore" app/src/androidTest/` returning zero hits is clean, but it may be too rigid if unrelated platform-quarantined tests exist later. Codex focuses specifically on re-enabling `PermissionDeniedFlowTest.openSettingsIntentFiresOnPermanentDenial`.

### Sequencing Problems

Claude's phase diagram allows `LowConfidencePicker polish`, `CameraUiState.Failure polish`, and `TestIdentifyModule removal` to interleave after contract locks. That is efficient, but it could create conflict for an executor if UI tests and androidTest migration both touch shared test helpers or app launch behavior. Codex's sequence is more conservative: land `Phase 1 - Test Wiring Debt` before adding new flow tests.

Claude puts `ROADMAP.md` after calibration, which is reasonable, but then includes `README.md` status table and ledger close in the same flow. Those should be final review/closeout tasks, not part of the core implementation sequence unless the sprint executor is explicitly responsible for closing the sprint.

Claude's contract-lock `Phase 0` can slow the sprint with tests for file absence and subtitle tags before the executor has validated the current code shape. Codex's `Phase 0` baseline and inventory is lower risk and less likely to create artificial RED tests that need redesign.

Claude's `Phase 3` says migrate tests before deleting `TestIdentifyModule`, which is right. But the earlier `3.1 Must-land` list starts with "Delete `TestIdentifyModule.kt`" before the migration bullet. The detailed phase corrects it, but the task list ordering is internally inconsistent.

## If I Were Merging

I would keep Codex's sprint shape, scope boundaries, and conservative sequencing as the backbone.

From Gemini, I would keep the concise top-level structure and the product-language affordance `"None of these look like my plant"` for the archetype escape path.

From Claude, I would keep the executor-ready task decomposition for `LowConfidencePickerTags.SUBTITLE`, `NO_CANDIDATES_EMPTY`, `SEARCH_EMPTY`, `CameraScreenTags.FAILURE_BANNER`, `FAILURE_RETRY`, `ModelScoreMapperPerSpeciesThresholdTest`, the probe-before-assertion calibration flow, and the final audit/evidence checklist.

I would not keep Gemini's pre-measured `> 0.6` Monstera assertion, and I would soften Claude's rigid contract-lock tests and README/ledger closeout tasks unless the sprint executor is explicitly responsible for repository status closure.
