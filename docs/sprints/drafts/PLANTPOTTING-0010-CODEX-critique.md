# PLANTPOTTING-0010 Codex Draft Critique

## Compared with PLANTPOTTING-0010-CLAUDE.md

### Stronger than my draft

- **Phase 0 — Shared persistence foundation** is more concrete than my **Phase 1 — Persistence Foundation First**. Claude names `PlantLogStore`, `DataStorePlantLogStore`, DTOs, `PersistenceModule`, a bounded `identifiedPlants` cap, and an injectable `TimeProvider`/`Clock`. My draft says to add serializable local models and repositories, but underweights bounded growth and testable time.
- **3.2 Confidence display without touching the seam** is sharper than my **Confidence Display Without Seam Change**. Claude calls out that `Candidate.probability` is raw softmax `0..1`, that `CameraViewModel.onCaptureReady` already reads `CandidateProvider`, and that the margin-confidence branch should be asserted in a test. My draft gives the route/nav approach, but does not identify the margin-branch subtlety.
- **3.3 "Add this plant" routing seam** is more explicit than my **Strong Unmapped "Add This Plant"** section. Claude specifies `topLabel`, `topProbability`, `topIsMapped`, a possible `UnmappedTopProvider`, the exact predicate `raw-top probability >= high-confidence threshold && topIsMapped == false`, and the new `NavCommand.AddPlant(modelClassLabel, confidencePct)`. My draft correctly warns about strong-unmapped collapse, but leaves the routing predicate less pinned down.
- **3.4 Reference images: licensing x APK size** is stricter than my **Phase 6 — Reference Images and Licensing**. Claude prefers CC0/public-domain only, rejects CC-BY-NC, recommends `res/drawable-nodpi/` WebP plus `painterResource`, and explicitly avoids Coil/image-loading libraries. My draft allows broader "clearly compatible CC images" and an `assets/reference` option, which increases attribution and rendering ambiguity.
- **Phase 5 — Pillar A1: theme candidates + hand-off** is stronger than my **Phase 5 — Theme Candidates and Screenshot Deliverable** on release safety. Claude makes the theme switcher debug-only with `BuildConfig.DEBUG`, persists via DataStore, keeps production default unchanged, and reconciles `res/values/themes.xml` / `colors.xml`. My draft says debug-review oriented but does not explicitly require release exclusion or system chrome reconciliation.
- **Phase 6 — Version bump, gates, delivery** is more operational. Claude names `versionCode` 3 to 4 and `versionName` 0.3.0 to 0.4.0, the Dropbox destination, evidence folder, and the known `pixel6Api34` timeout risk. My draft says "next sprint build version" and "repo's established delivery practice", which is safer but less actionable.

### Weaker than my draft

- Claude's file is less complete as a full sprint checklist. My **Goals**, **Scope Boundaries**, **File-Level Touchpoints**, and **Acceptance Criteria** enumerate more surfaces and expected states, especially `RecommendationViewModel`, `RecommendationUiState`, `ResultUiState`, `Routes.kt`, `PlantPottingNavHost.kt`, `MainActivity.kt`, `LowConfidencePickerViewModel.kt`, and test classes.
- My **Phase 3 — My Plants** keeps "save-to-collection action from `ResultScreen.kt` and/or `RecommendationScreen.kt`" open and includes delete/remove as a scoped optional. Claude's **Phase 3 — Pillar A: "My Plants" folder** automatically appends on high-confidence identification. That is concrete, but it may conflict with the product wording "My Plants collection"; users may expect an explicit save action instead of every scan becoming a saved plant.
- My **Phase 8 — Version, Build, and Delivery** includes targeted instrumentation tests for navigation/result/low-confidence flows and final APK-size/image contribution notes. Claude's delivery phase is good, but its acceptance criteria does not call out final notes separating calibrated model behavior from editorial KB/class-map coverage.
- My **Scope Boundaries** explicitly says keep AIY baseline files intact unless an added KB species is already resolvable by AIY and tests require handling. Claude does not mention AIY baseline protection.
- My **Phase 4 — Search Containment** preserves candidate chips, no-candidates card, search-empty state, and archetype CTA behavior. Claude mentions preserving tags and empty-state behavior, but under-specifies the archetype fallback and candidate-chip preservation.

### Missing tasks

- Add explicit `PlantCollectionRepository` and `PlantRequestRepository` APIs. Claude collapses both into `PlantLogStore`; that may be fine internally, but the sprint intent called for one persistence layer with two narrow domain APIs. My **Shared Local Persistence** section makes that separation clearer.
- Add `ResultUiState` save status fields and a save action if the final product chooses explicit saves. Claude's **Phase 3 — Pillar A: "My Plants" folder** only covers automatic append from success.
- Add missing-image crash/render tests. Claude has a manifest cross-check and placeholder strategy, but my **Phase 6 — Reference Images and Licensing** explicitly includes "tests that missing image metadata does not crash recommendation rendering."
- Add exact acceptance around saved plants and add-request totals surviving process recreation/device restart. Claude has persistence round-trip verified, but my **Acceptance Criteria** states both persistence outcomes separately.
- Add the sprint-result note requirement that new class-map rows are editorial/model-vocabulary coverage, not calibrated model behavior. This is in my **Phase 7 — KB Expansion** and **Acceptance Criteria**, and Claude omits it.

### Underweighted risks

- **Automatic My Plants writes** are underweighted. Claude's **On a high-confidence identification, append an `IdentifiedPlant`** can create noisy history, duplicates, and accidental saves. It needs duplicate semantics, a cap, and possibly an explicit save affordance from my **Phase 3 — My Plants**.
- **Attribution burden** is partially handled by CC0/PD preference, but the **A5 licensing** task still says attribution manifest. If any non-PD image slips in, an in-app/legal display decision may be required. My broader manifest fields make this more visible.
- **Feature sprawl** is acknowledged in Claude risk 11, but the plan still schedules KB expansion, confidence, search, images, My Plants, Add-this-plant, themes, evidence, and delivery. My acceptance criteria are longer, but my phase split makes cross-feature UI and test blast radius more explicit.

### Sequencing issues

- Claude sequences **Phase 1 — Pillar C: text-only KB expansion** before **Phase 2 — Pillar A polish** and **Phase 4 — Pillar B**. That is mergeable, but it can reduce the number of unmapped classes available to exercise **Add this plant** during the sprint. Keep at least one high-confidence unmapped fixture unmapped for `ModelScoreMapperTest` and `CameraViewModelTest`.
- Claude places **A5 Reference images** in **Phase 2**, before **My Plants** and **Add this plant**. Images are visible polish and licensing-heavy; they may be safer after the routing/persistence features are proven, as in my later **Phase 6 — Reference Images and Licensing**.
- Claude puts **Theme candidates** after **Add this plant** to reuse DataStore, which is logical. But if screenshots are a core principal deliverable, theme token work could start earlier without persistence, then add the debug switcher after **Phase 0**.

## Compared with PLANTPOTTING-0010-GEMINI.md

### Stronger than my draft

- Gemini is much more concise and easier to execute as a high-level sprint brief. Its **Task List** is short enough for a human reviewer to scan quickly, while my draft risks becoming a large implementation checklist.
- Gemini's **Scope Boundaries & Architecture Decisions** states a simple product shape for **"Add this plant" Interaction**: intercept strong OOV before the picker, show a modified `ResultScreen` variant or banner, and provide a "Pick manually" secondary button. My draft discusses a low-confidence/request screen state but does not name the manual fallback button as clearly.
- Gemini's **Reference Image Strategy** cleanly states compressed WebP plus placeholder fallback. My draft gives more validation detail, but Gemini's statement is easier to communicate to non-engineering stakeholders.
- Gemini includes **RecommendationScreen** in its confidence display task: **Update `ResultScreen` and `RecommendationScreen` to display a numeric percentage and progress bar for model confidence**. My goals emphasize the post-identification result path and only later mention reference imagery on recommendation views; confidence on recommendation is less prominent in my draft.

### Weaker than my draft

- **Shared Persistence Layer** chooses Room without justification. For two small local collections, my **Shared Local Persistence** and **Scope Boundaries** prefer DataStore unless Room becomes necessary. Gemini's **Define the Room database, `MyPlantsDao`, and `PlantRequestsDao`** adds migrations, schema ceremony, KSP risk, and dependency surface the sprint does not obviously need.
- Gemini under-specifies the frozen seam. It says no changes to `PlantIdentifier` or `IdentificationResult`, but does not explain `CandidateProvider`, `MappedScore`, optional nav args, or how confidence and raw top labels move off-seam. My **Confidence Display Without Seam Change** and **Strong Unmapped "Add This Plant"** are much safer.
- Gemini's **Risks & Mitigations** section is too thin. It covers APK size, network leakage, and Pilea regression, but misses persistence duplication, confidence seam pressure, strong-unmapped routing collapse, theme hardcoding, instrumentation churn, and search containment regressions.
- Gemini lacks file-level touchpoints and test-level specificity. My **File-Level Touchpoints** and phase tests call out `CameraViewModelTest`, `ModelScoreMapperTest`, `LowConfidencePickerScreenTest`, `LowConfidenceFlowTest`, repository tests, KB/class-map tests, and Compose screen tests.
- Gemini's **Acceptance Criteria** does not require process-restart persistence, local request totals being visible/testable, a debug-only theme switcher, reference-image license manifest validation, APK-size delta recording, exact version numbers, or sprint notes separating editorial KB coverage from calibrated model behavior.

### Missing tasks

- Add baseline gates before edits: my **Phase 0 — Baseline and Guards** runs `testDebugUnitTest`, `verifyNoNetworking`, `scripts/check-stub-isolation.sh`, and records APK size before image changes. Gemini starts directly with implementation.
- Add repository or store tests for serialization/DAO behavior, default empty state, request increment semantics, ordering, and persistence restart behavior. Gemini only says to define Room database/DAOs.
- Add a concrete side-channel task equivalent to my **Extend model side-channel data so strong unmapped model hits can be represented without changing `IdentificationResult`**.
- Add `NavCommand.AddPlant`, `Routes.ADD_THIS_PLANT`, and routing tests. Gemini says "modify routing logic", but does not name the navigation contract.
- Add image license manifest validation. Gemini says **Add attribution for images (e.g., to an `About` or `Licenses` screen if needed, or in code comments as per repository norms)**, which is too loose for bundled third-party assets.
- Add search containment tests and preservation requirements for candidate chips, no-candidates state, search-empty state, and archetype fallback.
- Add version bump specifics: my draft says bump from `0.3.0`/`3`; Claude is even more explicit with `3 -> 4` and `0.3.0 -> 0.4.0`. Gemini only says bump the app version.

### Underweighted risks

- **Room complexity** is underweighted. Gemini treats Room as the default, but it brings schema and migration questions before the app has enough local data shape to justify it.
- **Strong OOV invisibility** is underweighted. Gemini identifies the interaction but does not mention that `ModelScoreMapper.candidateForIndex` can drop unmapped labels or that raw top label/probability must be preserved before the low-confidence picker.
- **Confidence correctness** is underweighted. Gemini says show confidence on `ResultScreen` and `RecommendationScreen`, but does not specify whether this is top raw probability, top mapped probability, absent in stub flows, or a nav/view-model argument.
- **Licensing** is underweighted. "CC/public-domain" and "About or Licenses screen if needed" is not enough; the sprint needs in-repo provenance and a testable manifest.
- **Search containment regression** is underweighted. Gemini has the UI task but no behavioral guard for manual selection, chips, empty states, or archetype fallback.

### Sequencing issues

- Gemini combines **Persistence & KB Expansion** in **Phase 1**. Those are independent, but mixing database scaffolding with KB content means the first phase has both dependency risk and content-review risk. My draft separates baseline/persistence from KB expansion, and Claude explicitly calls KB independent/parallel.
- Gemini puts **Themes & Visual Polish** before **New Features ("My Plants" & "Add this plant")**. If the sprint's architectural deliverable is one shared local persistence layer used by both features, My Plants and Add-this-plant should start as soon as persistence exists rather than waiting behind themes.
- Gemini puts reference images in **Phase 3: New Features** alongside My Plants and Add-this-plant. That creates a high-risk phase mixing licensing/assets, persistence UI, and raw-model routing. My draft isolates reference images into a separate later phase with size and licensing gates.
- Gemini's delivery phase lacks a pre/post image-size comparison and does not place `verifyNoNetworking` immediately after adding Room. My draft runs the network gate right after dependency changes and again at the end.

## If I Were Merging

- Keep Claude's **3.2 Confidence display without touching the seam**, **3.3 "Add this plant" routing seam**, debug-only theme switcher, `TimeProvider`/bounded persistence details, and explicit version/delivery evidence tasks.
- Keep Gemini's concise top-level framing, the clear **"Pick manually"** fallback in **"Add this plant" Interaction**, and the simple WebP-plus-placeholder reference-image messaging.
- Keep my draft's DataStore-first persistence preference with two domain repositories, broader file/test touchpoint coverage, explicit process-restart acceptance criteria, search containment preservation details, missing-image tests, and final notes distinguishing editorial KB expansion from calibrated model behavior.
