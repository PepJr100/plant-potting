# PLANTPOTTING-0003 Draft Critique

## GEMINI draft

### What is stronger than mine

- **Framing:** Gemini's `## 1. Intent` is cleaner at explaining why the sprint matters to the user: moving from a "demo-ready scaffold" to a "functional tool" and calling out natural lighting, framing, and botanical variability. My draft is more operational, but less useful as an executive summary.
- **Performance awareness:** `3.2 Should-land (if time allows)` includes **Performance telemetry**, **Orientation-aware crop**, and **Warm-up**. My plan covers preprocessing and mentions optional NNAPI, but Gemini better calls out user-perceived first-shot latency and explicitly asks for inference/init timing evidence in `5.4 Capture a Logcat trace of a successful identification and extract the inference_time_ms`.
- **Bug A return shape:** `0.3 If stderr contains the "null root node" string, return a custom object { Success = $false; Retryable = $true }` is more expressive than my boolean-only `1.3 Make Invoke-AdbDump return $false for retryable dump failures instead of throwing`. A typed result would make future retry diagnostics clearer.
- **Preprocessing recipe:** `4.3 Image Processing Pipeline (TFLite Support Library)` gives a concrete `ResizeOp`, `CenterCropOp`, `NormalizeOp`, `TensorImage`, `TensorLabel` pipeline. My `4.1 OnDevicePlantIdentifierPreprocessTest` and `4.2 ImagePreprocessor` cover this through tests and implementation, but Gemini's prose is easier for an implementer to visualize.

### What is weaker than mine

- **Model choice is too vague and likely overconfident.** Gemini chooses **MobileNetV3-Small (iNaturalist)** in `4.1 The Model` without naming a concrete artifact, label map, version, license, or acquisition path. My `1.3 Model decision` names `google/aiy/vision/classifier/plants_V1/1`, the asset layout, manifest fields, hash requirement, and a measured-fixture posture.
- **Accuracy claims are unsafe.** Gemini claims expected `>85%` top-1 on the 16 targets in `4.1 The Model` and acceptance requires `IdentificationAccuracyTest` with `>80% accuracy on the bundled 3-species test set`. My draft is stronger because it avoids field-accuracy promises, states a lower ballpark, and makes fixture metrics evidence rather than a brittle acceptance blocker in `9.5 Record fixture metrics... do not block acceptance on a minimum accuracy threshold unless the model cannot identify any mapped species`.
- **Low-confidence UX is weaker.** Gemini's `4.2 Mapping & Fallback Architecture` allows **Tier 2: Genus Hit** to show a "Generic Match" result with an archetype recipe, and `4.4 Define UnsureScreen` includes "I'm not sure, give me a safe bet" routing to `standard-houseplant`. My `1.5 Low-confidence decision` and `8. Low-confidence manual picker` are safer: no silent recipe for weak or unmapped matches, picker over the existing 16 KB species, optional archetype prefilter only as a hint.
- **Interface/data-model churn is undercontrolled.** Gemini's `4.2 Update IdentificationResult to include val confidence: Float` adds confidence as a required concept for all results. My draft explicitly keeps `PlantIdentifier.identify(jpeg: ByteArray)` unchanged, rejects adding confidence to `IdentificationResult` as out of scope in `2.3 Out-of-scope`, and propagates only `IdSource` plus low-confidence UI state.
- **Task/test pairing is thinner.** Gemini has useful tests, but it does not pair each implementation slice as consistently as my plan: compare Gemini `3.1 Implement OnDevicePlantIdentifier` with my `4.1` through `4.10`, which split preprocessing, classifier facade, mapper, low-confidence behavior, and failure behavior into independently testable units.
- **Acceptance criteria are incorrectly pre-checked.** Gemini's `## 8. Acceptance Criteria` uses `[x]` for planned work, while my `## 6. Acceptance criteria` keeps acceptance unchecked and observable.

### What tasks are missing

- No task for a **model manifest with artifact hash, label count, input contract, license/source, thresholds, and acquisition date**. My `2.6 Commit model_manifest.json` and `2.7 ModelManifestTest` cover this.
- No task for **asset presence in the APK** beyond `TFLiteToolchainTest` opening an asset. My `2.1 ModelAssetsTest` catches model, labels, mapping, and manifest packaging.
- No task for **mapping invalid fixtures** such as duplicate labels, unknown species ids, unknown archetypes, empty labels, or invalid boost values. My `3.1 ModelLabelMappingValidationTest` covers this.
- No task for **all 16 KB species being reachable via direct mapping or manual picker**. My `3.9 Add a golden test proving all 16 KB species are reachable...` covers this.
- No task for **source route argument plumbing across `Routes`, `PlantPottingNavHost`, `ResultViewModel`, and `ResultUiState`** at the same specificity as my Phase 7.
- No task for **`StubPlantIdentifierTest` still directly referencing `StubPlantIdentifier`**. Gemini only verifies grep isolation in `3.6`.
- No task for a **model card**. My `9.3 Add docs/ml/model-card-aiy-plants-v1.md` covers source, hashes, known weaknesses, low-confidence policy, and APK size impact.
- No task for **manual picker search by common name and scientific name**. My `8.1 ManualIdentificationViewModelTest` covers this.

### What risks are underweighted

- **False-positive potting advice** is underweighted. Gemini mentions poor-light accuracy in `7.1`, but the mitigation is just a `0.6` threshold and `UnsureScreen`; it still allows genus/archetype auto-results and "safe bet" routing. My `5.2 False-positive plant match produces bad potting advice` treats this as the central UX risk.
- **Model-label coverage** is underweighted. Gemini's `1.2 Author model_mapping.json` says include mappings for all 16 target species, but does not treat missing labels or cultivar synonyms as likely. My `5.1 Model labels do not cover all 16 KB species` assumes gaps and routes through manual selection.
- **Native dependency and packaging risk** is too narrow. Gemini has `7.2 APK Size Growth` and `7.3 TFLite Initialization Latency`, but does not call out ABI/native library failures or `.tflite` compression beyond setup. My `5.3 TensorFlow Lite native dependency breaks CI or APK packaging` and `2.3 packaging configuration` cover this.
- **Stub isolation risk** is thin. Gemini verifies `check-stub-isolation.sh`, but it does not discuss UI tests or source-badge strings accidentally pulling stub language into production. My `5.9 Stub isolation grep catches source badge tests incorrectly` is more targeted.
- **Bug A may still fail despite stderr matching.** Gemini uses a 1-second settle sleep in `0.5`; my `1.8 Add a 2-second foreground-settle sleep` and `1.6 Wait-ForNode retries after a retryable dump miss` are more defensive.

### What sequencing is wrong

- **Bug A is placed first and serializes the whole sprint.** Gemini's `## 6. Sequencing` puts Phase 0 Bug A before assets, dependencies, implementation, and UX. My plan treats Phase 1 Bug A as parallel with model work; it should not block model sourcing or mapping tests.
- **Assets before dependencies is workable, but not ideal without an asset contract.** Gemini Phase 1 downloads assets before Phase 2 adds packaging/no-compress and `TFLiteToolchainTest`. My Phase 2 starts with asset presence tests, dependency wiring, packaging, manifest, and `verifyNoNetworking`, giving a clearer contract before implementation.
- **Low-confidence UX and badge are conflated.** Gemini's second `Phase 4` combines `ResultScreenBadgeTest`, `IdentificationResult` confidence, `UnsureScreen`, and `CameraViewModel` thresholding. My plan separates source plumbing (`Phase 6`), badge (`Phase 7`), and manual picker (`Phase 8`), which avoids blocking UX 2 on low-confidence route design.
- **Performance/hardware acceleration is in the middle of must-land flow.** Gemini's `Phase 4.5 Performance & Hardware Acceleration` adds `GpuDelegate` and power/thermal testing before acceptance. That should be optional and likely descoped; my plan keeps acceleration as nice-to-have only.

## CLAUDE draft

### What is stronger than mine

- **Observable success bar:** Claude's `2.1 The single observable success bar` is sharper than my goals list. It states what must happen on `pixel6Api34` and a connected emulator, including source-driven badge, low-confidence route, Bug A, and a time budget.
- **Falsifiability:** `2.2 Falsifiability - how we know we hit it` is stronger than my evidence framing because it names clean-clone commands, stub isolation, integration transcript capture, fixture tests, and badge tests as proof points.
- **Concrete model selection:** `4.1 Model choice` is more specific than my draft about the AIY model variant: `google/aiy/vision/classifier/plants_V1/3`, FP16 vs INT8, approximate sizes, license, class count, and alternatives rejected. My draft says AIY Plants V1 and prefers float if available, but Claude's variant-level decision is easier to execute.
- **Editorial mapping notes:** `2.5 Author docs/kb/ml-mapping-notes.md` is stronger than my model-card-only documentation because it asks for one justification per mapping line. My `9.3 model-card-aiy-plants-v1.md` covers model-level documentation, but not per-entry editorial rationale.
- **Hilt/test wiring specificity:** `4.4 Seam invariants`, `3.5 Wire OnDeviceIdentifyModule`, and `3.6 OnDeviceIdentifyModuleBindingTest` give a concrete production/test module strategy. My Phase 5 has the same intent but less detail on module replacement options.
- **Bug A evidence:** `6.5 Capture transcripts` is stronger than my `1.10` and `10.6` because it asks for cold, warm, and build-only transcripts under a named evidence directory.
- **Risk inventory:** Claude's risks `7.5 TFLite native library doesn't ship for the AOSP pixel6Api34 arch`, `7.8 Hilt @TestInstallIn plus instrumented Compose plus CameraX plus TFLite`, and `7.9 no placeholder copy` are more environment-specific than my risk list.

### What is weaker than mine

- **Low-confidence UX is more dangerous.** Claude's `4.3 Confidence policy and the fallback flow` returns an `IdentificationResult` with `lowConfidence = true`, may carry a best-mapped species, and can skip to an archetype picker. My `1.5 Low-confidence decision` is safer because the app does not silently recommend an archetype or weak best species; it asks the user to choose from the 16 KB species.
- **Mapping strategy overfits direct species IDs.** Claude's `4.2 Mapping 2,101 model classes -> 16 KB species` maps label strings directly to KB species ids and treats unmapped labels as null. My `3. Mapping table from model labels to KB species and archetypes` is more robust because it validates both direct species and archetype mappings, supports aliases, top-3 archetype aggregation, and "no mapping for labels that would create unsafe advice."
- **Result-data changes are too large.** Claude's `3.1 Extend IdentificationResult` adds `lowConfidence` and `topCandidates`. My plan keeps `IdentificationResult` closer to the existing seam and routes low-confidence through a named domain exception plus `CameraUiState.LowConfidence`, reducing pressure on every existing caller.
- **Dependency choice is heavier.** Claude's `1.1` adds `tensorflow-lite-task-vision` in addition to interpreter/support. My draft intentionally uses `tensorflow-lite` and `tensorflow-lite-support` only, with an `InterpreterFacade`, which is easier to audit against `verifyNoNetworking` and easier to unit-test without native TFLite.
- **Failure handling hides operational faults.** Claude's `3.3 On model-load failure or inference exception... fall back to IdentificationResult(... lowConfidence = true). Do not throw.` risks making a broken model look like user uncertainty. My `4.10 On malformed image or model failure, throw a user-safe identification exception` keeps technical failure distinct from low confidence.
- **Acceptance mutates older sprint documents.** Claude's `7.3 Add a closure note section to docs/sprints/PLANTPOTTING-0002.md` is unnecessary scope expansion. My plan records carry-forward closure in `docs/sprints/results/PLANTPOTTING-0003.md` without editing prior sprint plans.

### What tasks are missing

- No task for a **model manifest** with source, hash, input size, normalization, output shape, label count, and thresholds. My `2.6` and `2.7` cover this.
- No task for **preprocessing tests independent of TFLite Task Vision**. My `4.1 OnDevicePlantIdentifierPreprocessTest` tests JPEG decode, crop/resize, RGB ordering, and normalization directly.
- No task for a **fake interpreter/classifier facade** that avoids native TFLite in unit tests. My `4.3 TflitePlantClassifierTest` and `4.4 InterpreterFacade` cover this.
- No task for **invalid mapping fixtures** beyond right-hand-side existence. My `3.1 ModelLabelMappingValidationTest` covers duplicate labels, unknown species, unknown archetypes, empty labels, and invalid confidence boost values.
- No task for **top-3 archetype aggregation**. Claude has a top-3 picker and archetype picker, but not a scoring test equivalent to my `3.6 ModelScoreMapperTest` for `top-3 archetype aggregation`.
- No task for **all 16 KB species reachable by manual picker**. Claude's `7.4` risk proposes `KbToModelCoverageTest` only as a follow-up; my `3.9` makes reachability part of this sprint.
- No task for **badge coverage of `STUB_RANDOM` separately from `STUB_DETERMINISTIC`**. Claude's `4.1 ResultScreenBadgeTest` groups `STUB_DETERMINISTIC / STUB_RANDOM`; my `7.1` covers both values explicitly.

### What risks are underweighted

- **False-positive recommendations** are underweighted. Claude's low-confidence flow still lets a best-mapped species or archetype route downstream. My draft treats bad potting advice as a first-order safety/UX risk and avoids auto-fallback recipes.
- **Task Vision transitive dependency risk** is partially acknowledged in `7.3`, but still underweighted because `1.1` adds `tensorflow-lite-task-vision` as must-land. My plan keeps the dependency surface smaller and verifies after dependencies are added.
- **Mapping to archetype recipes without species context** is underweighted. Claude's `5.4 recommendation-from-archetype` and `5.6 recommendByArchetype` create a new recommendation path that can bypass species validation. My draft keeps manual selection on the existing species-backed result path.
- **Data class expansion risk** is underweighted. Claude says `IdentificationResult` defaults preserve callers, but optional `topCandidates` and sentinel empty `speciesId` introduce invariants that must be maintained across nav, result, and recommendation. My plan avoids sentinel result objects.
- **Fixture reliability risk** is underweighted. Claude's `3.2 OnDevicePlantIdentifierFixturesTest` requires specific real-photo outcomes; my `9.5` records fixture metrics and avoids making an arbitrary tiny corpus a hard accuracy bar.

### What sequencing is wrong

- **Dependencies before model assets can work, but Phase 1's RED test is suspect.** Claude's `1.3 VerifyNoNetworkingRegressionTest` says fail RED before adding TFLite deps, but a "no forbidden dependencies" test should already pass before TFLite. My plan runs baseline `verifyNoNetworking`, then runs dependency audit after adding TFLite, which matches the behavior being protected.
- **Pickers depend on enriched `IdentificationResult`, not just threshold policy.** Claude's sequencing makes Phase 4 and Phase 5 depend on `3.1` result expansion. If the merged plan uses my exception/UI-state approach, picker sequencing should depend on `CameraUiState.LowConfidence` and route hints, not on adding `topCandidates` to the core result.
- **Bug A is correctly parallel, but too late for acceptance evidence planning.** Claude Phase 6 can run in parallel, which is good, but its transcript requirements in `6.5` should be reflected in setup/results from Phase 0. My `0.5 Add sprint note... as evidence sink` establishes the evidence destination earlier.
- **Documentation/ledger work is too broad.** Claude Phase 7 includes updating `PLANTPOTTING-0002.md` and `ledger.yaml`; unless sprint execution policy requires it, this creates cross-sprint churn. My docs work is narrower: results doc, optional README if commands change, model card.

## If I Were Merging, I'd Keep X From Draft A And Y From Draft B

- Keep Claude's `2.1 The single observable success bar` and `2.2 Falsifiability - how we know we hit it` as the merged plan's opening acceptance frame.
- Keep Claude's concrete `4.1 Model choice` variant detail, but combine it with my `2.6 model_manifest.json` and `2.7 ModelManifestTest`.
- Keep Gemini's concise preprocessing recipe from `4.3 Image Processing Pipeline`, but implement it through my `4.1 OnDevicePlantIdentifierPreprocessTest`, `4.3 TflitePlantClassifierTest`, and `4.4 InterpreterFacade`.
- Keep Claude's `2.5 docs/kb/ml-mapping-notes.md` editorial mapping notes, and pair them with my `3.1 ModelLabelMappingValidationTest`, `3.6 ModelScoreMapperTest`, and `3.9 all 16 KB species are reachable` test.
- Keep Claude's `6.5 Capture transcripts` for Bug A evidence, but use my Phase 1 retry semantics and 2-second foreground settle.
- Keep my low-confidence UX policy: no archetype auto-recipe or "safe bet"; route weak or unmapped model output to a manual picker over the 16 KB species, optionally prefiltered by archetype hints.
