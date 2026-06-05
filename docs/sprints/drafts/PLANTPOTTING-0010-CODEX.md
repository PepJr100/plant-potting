# PLANTPOTTING-0010 — App-experience sprint plan

## Intent

PLANTPOTTING-0010 is a deliberately mixed app-experience sprint: make the shipped Android app feel more pickable and useful, add a local-only request signal for model-covered plants that do not yet have care entries, and continue cheap text-only KB expansion where it does not reopen the Pilea/pothos risk.

The sprint has three pillars. The UI/UX refresh pillar covers theme candidates, confidence display, My Plants, search containment, and reference images. The Add-this-plant wireframe pillar covers strong unmapped model hits and local request totals. The KB expansion pillar covers a bounded popular slice of remaining unmapped model classes as text-only KB rows.

The main architectural move is shared local persistence. "My Plants" and "Add this plant" both need local state; this sprint should introduce one small repository layer and two narrow domain APIs rather than two unrelated storage mechanisms.

## Goals

- [ ] Deliver 2-3 selectable Compose theme candidates, visible in app and captured as screenshots, so the principal can choose from real renders rather than a hardcoded redesign.
- [ ] Show numeric confidence percentage plus a progress bar on the post-identification result path without changing `PlantIdentifier` or `IdentificationResult`.
- [ ] Add a "My Plants" collection backed by the app's first local persistence layer.
- [ ] Add a local-only "Add this plant" request wireframe for strong-confidence model classes that have no KB entry.
- [ ] Reuse one persistence layer for both saved plants and add-request totals.
- [ ] Contain the species list visually and behaviorally inside the search-species control in `LowConfidencePickerScreen.kt`.
- [ ] Show bundled, license-clean reference imagery on plant detail / potting-mix recipe views within an explicit APK-size budget.
- [ ] Add a bounded text-only KB expansion over remaining popular unmapped model classes while keeping Pilea deliberately unmapped.
- [ ] Bump the Android app version and produce a debug APK plus screenshots for review/dropbox delivery.
- [ ] Keep `verifyNoNetworking` and `scripts/check-stub-isolation.sh` green.

## Non-goals

- [ ] Do not change `app/src/main/java/com/darkfactory/plantpotting/identify/PlantIdentifier.kt`, `IdentificationResult`, or the existing high-level identify contract.
- [ ] Do not add networking, remote config, image fetching, cloud identification, analytics, or upload paths.
- [ ] Do not train, fine-tune, replace, or recalibrate the bundled model.
- [ ] Do not add first-party/self-shot plant imagery.
- [ ] Do not add or map Pilea / Chinese Money Plant in this sprint.
- [ ] Do not attempt the pothos↔Pilea boundary fix; it stays deferred.
- [ ] Do not add broad app settings or account/user sync.
- [ ] Do not use reference images without license provenance recorded in-repo.

## Scope Boundaries

- [ ] Keep production identification behind `PlantIdentifier.identify(jpeg: ByteArray): IdentificationResult`.
- [ ] Keep any score/confidence UI data outside the frozen identify seam, likely through the existing `CandidateProvider`/`MappedScore` side-channel or route/view-model state.
- [ ] Keep local persistence behind injected repositories/modules, not direct `SharedPreferences`/file calls from composables.
- [ ] Prefer DataStore for this sprint unless Room becomes clearly necessary during implementation; the data shape is small, append/update oriented, and does not need relational queries yet.
- [ ] Add one storage module under `app/src/main/java/com/darkfactory/plantpotting/di/` and one local data package such as `app/src/main/java/com/darkfactory/plantpotting/local/`.
- [ ] Keep theme selection local/debug-review oriented; it should not require network or account state.
- [ ] Keep reference image assets under a dedicated bundled asset/resource path such as `app/src/main/assets/reference/` or `app/src/main/res/drawable*`, with a manifest file that maps `speciesId` to asset, license, source URL, author, and byte size.
- [ ] Bound reference images to the 32 existing KB species plus newly added 0010 species only where license-clean assets are found and size budget permits.
- [ ] Use a placeholder or omit-image state for species with no clean bundled image rather than using questionable assets.
- [ ] Keep KB edits in `app/src/main/assets/kb/species.json`, `app/src/main/assets/kb/archetypes.json` only if needed, and `app/src/main/assets/ml/house_plant_species_mobilenetv2/plant_class_map.json`.
- [ ] Keep AIY baseline files intact unless a KB species added in 0010 is already resolvable by AIY and the existing tests require explicit handling.

## Proposed Shape

### Shared Local Persistence

Use one local persistence stack with two logical repositories. `PlantCollectionRepository` owns saved identified plants for "My Plants". `PlantRequestRepository` owns local totals for "Add this plant" requests.

Suggested minimum saved-plant data is local id, `speciesId`, display name snapshot, source, confidence percent if available, and created timestamp. Suggested minimum add-request data is model label or unmapped label key, display label, probability percent, request count, first requested timestamp, and last requested timestamp.

The storage implementation can be a single DataStore file encoded as JSON via kotlinx.serialization, because the app already depends on kotlinx serialization and the sprint does not need SQL queries. If DataStore is added, add only the `androidx.datastore:datastore` dependency and keep `verifyNoNetworking` green.

### Confidence Display Without Seam Change

`IdentificationResult` currently carries no probability. Low-confidence candidates already travel through `CandidateProvider.mostRecentCandidates` and `Routes.lowConfidencePicker(...)` as integer percentages. For high-confidence results, add a small route/UI carrier rather than modifying `IdentificationResult`:

- [ ] Extend `NavCommand.Success` with optional `confidencePct`.
- [ ] Populate confidence in `CameraViewModel` from `(identifier as? CandidateProvider)?.mostRecentCandidates?.firstOrNull()` when it matches the success `speciesId`.
- [ ] Add an optional `confidence` query arg to `Routes.RESULT`.
- [ ] Read confidence in `ResultViewModel` and render percentage + `LinearProgressIndicator` in `ResultScreen`.

If the stub has no confidence value, render the existing badge-only result or a neutral "confidence unavailable" state only if design needs it; do not force fake model scores into the contract.

### Strong Unmapped "Add This Plant"

`ModelScoreMapper` already knows the best raw label, best probability, and whether mapping resolves. The current low-confidence path collapses unmapped results into `IdentificationResult(lowConfidence = true, speciesId = "")` plus mapped candidate chips. This sprint needs a new UI path for "strong model match, no KB entry":

- [ ] Preserve `IdentificationResult` as-is.
- [ ] Extend the side-channel data, not the seam, so `CameraViewModel` can distinguish weak/uncertain from strong-unmapped.
- [ ] Add a low-confidence/request screen state that can show the model label, confidence percent, and "Add this plant" button even when no KB species exists.
- [ ] Log request taps through `PlantRequestRepository`.
- [ ] Decide exact routing with tests: strong-unmapped should not silently show a wrong KB card, and low-confidence mapped candidates should keep the existing picker behavior.

## Phases and Sequencing

### Phase 0 — Baseline and Guards

- [ ] Run the existing JVM suite before edits to catch environment drift: `./gradlew testDebugUnitTest`.
- [ ] Run `./gradlew verifyNoNetworking`.
- [ ] Run `bash scripts/check-stub-isolation.sh` or equivalent shell available on the machine.
- [ ] Record current APK size from `app/build/outputs/apk/debug/` after a baseline `assembleDebug` so reference-image growth has a real comparison point.

### Phase 1 — Persistence Foundation First

- [ ] Add a version-catalog entry for DataStore if selected, then add the implementation dependency in `app/build.gradle.kts`.
- [ ] Add serializable local models for saved plants and add-request totals under a new local data package.
- [ ] Add a single injected local store implementation and Hilt module.
- [ ] Add `PlantCollectionRepository` with save/list/delete or save/list minimum operations.
- [ ] Add `PlantRequestRepository` with increment/listTotals minimum operations.
- [ ] Add unit tests for repository serialization, default empty state, increment idempotence, and saved-plant ordering.
- [ ] Re-run `verifyNoNetworking` immediately after adding the dependency.

### Phase 2 — Confidence and Add-Request Routing

- [ ] Extend `NavCommand.Success` with optional confidence percent while keeping existing call sites source-compatible where possible.
- [ ] Extend `Routes.RESULT` and `ResultViewModel` to carry/read an optional confidence percent.
- [ ] Render numeric percent and a Material3 `LinearProgressIndicator` in `ResultScreen.kt` for available confidence.
- [ ] Add result-screen tests covering confidence text/progress and stub/no-confidence behavior.
- [ ] Extend model side-channel data so strong unmapped model hits can be represented without changing `IdentificationResult`.
- [ ] Update `ModelScoreMapperTest` to pin the strong-unmapped case separately from weak low-confidence and mapped high-confidence cases.
- [ ] Update `CameraViewModelTest` to pin success confidence propagation and strong-unmapped navigation.
- [ ] Add an "Add this plant" wireframe surface in the low-confidence/request flow with model label, confidence percent, button, and local total after tapping.
- [ ] Log "Add this plant" taps through `PlantRequestRepository` only; do not add network, intents, share sheets, or filesystem export in this sprint.
- [ ] Decide and test interaction with the existing low-confidence path: weak/uncertain still shows candidate chips/search; strong-unmapped shows request CTA and may still offer search/manual selection below it.

### Phase 3 — My Plants

- [ ] Add a route in `Routes.kt` for My Plants and wire navigation in `PlantPottingNavHost.kt`.
- [ ] Add a way to reach My Plants from the app shell, likely a compact top action on `CameraScreen.kt` or result/recommendation screens rather than a new landing page.
- [ ] Add save-to-collection action from `ResultScreen.kt` and/or `RecommendationScreen.kt` once a species is known.
- [ ] Save species id, display name snapshot, source, confidence percent if present, and timestamp through `PlantCollectionRepository`.
- [ ] Add a My Plants screen showing saved plants newest-first with common/scientific names and a tap-through to `Routes.recommendation(speciesId)`.
- [ ] Add empty state for My Plants that does not require explanatory onboarding copy.
- [ ] Add delete/remove support only if it is cheap and testable; otherwise explicitly leave it out of this sprint's implementation checklist.
- [ ] Add unit tests for the My Plants view model.
- [ ] Add Compose screen tests for empty, populated, and tap-through states.

### Phase 4 — Search Containment

- [ ] Refactor `LowConfidencePickerScreen.kt` so the full species list is visually contained with the `OutlinedTextField` search control rather than appearing as a permanently separate page list.
- [ ] Prefer an exposed-dropdown or anchored panel pattern with fixed max height so the species list cannot consume the whole screen.
- [ ] Preserve existing candidate chips, no-candidates card, search-empty state, and archetype CTA behavior.
- [ ] Update `LowConfidencePickerScreenTest` and `LowConfidenceFlowTest` assertions for the contained list.
- [ ] Keep test tags stable where possible; add replacement tags only when the UI shape genuinely changes.

### Phase 5 — Theme Candidates and Screenshot Deliverable

- [ ] Refactor `PlantPottingTheme` in `ui/theme/Theme.kt` to support 2-3 named candidate palettes without disrupting the current system-dark handling more than necessary.
- [ ] Add candidate palettes in `ui/theme/Color.kt` with distinct clean/elegant directions, avoiding a one-note green/brown-only palette.
- [ ] Add a local theme picker surface in app, likely a compact segmented control or debug-review selector reachable from the app shell.
- [ ] Persist the selected candidate through the shared local store if cheap; otherwise use `rememberSaveable` only for screenshot review and document that final theme persistence is not guaranteed until selection.
- [ ] Build screenshots for each candidate on at least the camera, result, recommendation, low-confidence, and My Plants surfaces.
- [ ] Keep the theme selector available in the debug APK so the principal can switch candidates live.
- [ ] Do not silently choose the final theme in this sprint unless the principal has already selected one during review.

### Phase 6 — Reference Images and Licensing

- [ ] Define a reference-image manifest format keyed by `speciesId`.
- [ ] Add validator tests that every referenced image has license, author/source, and local asset path.
- [ ] Add an APK-size budget before importing images; suggested initial cap is no more than 3-5 MiB growth over the baseline debug APK.
- [ ] Source only CC0, public-domain, or clearly compatible CC images with attribution requirements recorded.
- [ ] Resize/compress images to app-display dimensions before bundling; do not commit original large source photos.
- [ ] Add images first for the most visible 0010/recently mapped species if full 32+ coverage would exceed the APK-size budget.
- [ ] Add a placeholder/no-image state for species with no license-clean image.
- [ ] Render the reference image on `RecommendationScreen.kt` for species-backed recommendations.
- [ ] Do not show plant reference images for archetype-only recommendations unless a species is known.
- [ ] Add tests that missing image metadata does not crash recommendation rendering.

### Phase 7 — KB Expansion

- [ ] Compare `house_plant_species_mobilenetv2/labels.csv` against `plant_class_map.json` and list the remaining 21 unmapped model classes.
- [ ] Select a small popular slice that excludes Pilea and avoids classes where care guidance would be too ambiguous without probing.
- [ ] Add new species rows to `app/src/main/assets/kb/species.json` with citations and recipe mappings.
- [ ] Add archetype rows to `archetypes.json` only if no existing archetype fits.
- [ ] Add class-map entries to `app/src/main/assets/ml/house_plant_species_mobilenetv2/plant_class_map.json`.
- [ ] Keep `HousePlantClassMapValidationTest`'s Pilea-absence guard intact.
- [ ] Bump count assertions in KB/class-map tests to the exact new totals.
- [ ] Add content tests for toxic/common-name/citation expectations matching existing `KbContentSpeciesTest` style.
- [ ] Do not claim new mappings are calibrated; document them as editorial/model-vocabulary coverage.

### Phase 8 — Version, Build, and Delivery

- [ ] Bump `versionCode` and `versionName` in `app/build.gradle.kts` from `3` / `0.3.0` to the next sprint build version.
- [ ] Run `./gradlew testDebugUnitTest`.
- [ ] Run `./gradlew verifyNoNetworking`.
- [ ] Run `bash scripts/check-stub-isolation.sh`.
- [ ] Run `./gradlew lintDebug` if available in the local environment.
- [ ] Run `./gradlew assembleDebug`.
- [ ] Run targeted instrumentation tests for navigation/result/low-confidence flows on `pixel6Api34` if the managed device is available.
- [ ] Capture theme screenshots from the debug APK for each candidate theme.
- [ ] Copy or stage the debug APK and screenshots for Dropbox delivery using the repo's established delivery practice if one exists.
- [ ] Record final APK size and image-asset contribution in the sprint result notes.

## File-Level Touchpoints

- [ ] `app/src/main/java/com/darkfactory/plantpotting/identify/PlantIdentifier.kt`: read-only guard; no contract edits.
- [ ] `app/src/main/java/com/darkfactory/plantpotting/identify/model/ModelScoreMapper.kt`: add side-channel support for confidence/unmapped state if needed.
- [ ] `app/src/main/java/com/darkfactory/plantpotting/identify/model/MappedScore.kt`: extend internal mapped-score data only if this is the cleanest side-channel.
- [ ] `app/src/main/java/com/darkfactory/plantpotting/camera/CameraViewModel.kt`: route confidence and strong-unmapped states.
- [ ] `app/src/main/java/com/darkfactory/plantpotting/camera/NavCommand.kt`: add optional confidence/request navigation fields.
- [ ] `app/src/main/java/com/darkfactory/plantpotting/ui/navigation/Routes.kt`: add My Plants route and optional result confidence/request args.
- [ ] `app/src/main/java/com/darkfactory/plantpotting/ui/navigation/PlantPottingNavHost.kt`: wire My Plants, result confidence, and add-request callbacks.
- [ ] `app/src/main/java/com/darkfactory/plantpotting/result/ResultViewModel.kt`: expose confidence and saved-plant action state.
- [ ] `app/src/main/java/com/darkfactory/plantpotting/result/ResultUiState.kt`: add confidence and save status fields.
- [ ] `app/src/main/java/com/darkfactory/plantpotting/result/ResultScreen.kt`: render confidence and save action.
- [ ] `app/src/main/java/com/darkfactory/plantpotting/result/RecommendationViewModel.kt`: expose species-backed reference image metadata.
- [ ] `app/src/main/java/com/darkfactory/plantpotting/result/RecommendationUiState.kt`: add optional species/reference-image fields.
- [ ] `app/src/main/java/com/darkfactory/plantpotting/result/RecommendationScreen.kt`: render reference image and My Plants affordance.
- [ ] `app/src/main/java/com/darkfactory/plantpotting/result/LowConfidencePickerViewModel.kt`: support contained search and strong-unmapped request state.
- [ ] `app/src/main/java/com/darkfactory/plantpotting/result/LowConfidencePickerScreen.kt`: contain search results and render request CTA.
- [ ] `app/src/main/java/com/darkfactory/plantpotting/ui/theme/Color.kt`: add candidate palettes.
- [ ] `app/src/main/java/com/darkfactory/plantpotting/ui/theme/Theme.kt`: select candidate palettes.
- [ ] `app/src/main/java/com/darkfactory/plantpotting/MainActivity.kt`: pass selected theme state if needed.
- [ ] `app/src/main/java/com/darkfactory/plantpotting/di/`: add local persistence module.
- [ ] `app/src/main/assets/kb/species.json`: add selected KB species.
- [ ] `app/src/main/assets/kb/archetypes.json`: add only necessary archetype content.
- [ ] `app/src/main/assets/ml/house_plant_species_mobilenetv2/plant_class_map.json`: map selected new non-Pilea classes.
- [ ] `app/src/main/assets/reference/` or `app/src/main/res/drawable*`: add compressed licensed reference images.
- [ ] `app/build.gradle.kts`: add persistence dependency if needed and bump app version.
- [ ] `gradle/libs.versions.toml`: add DataStore coordinates if selected.

## Risks and Mitigations

- [ ] Risk: persistence sprawls into two systems because My Plants and Add-this-plant feel unrelated; mitigation: land the shared repository/store first and block feature UI on that layer.
- [ ] Risk: numeric confidence display tempts a change to `IdentificationResult`; mitigation: carry confidence through `CandidateProvider`/`MappedScore`/navigation side-channel and keep the seam frozen.
- [ ] Risk: strong-unmapped model matches get routed as low-confidence and the request CTA never appears; mitigation: add mapper and camera-view-model tests for best-label unmapped above threshold.
- [ ] Risk: adding DataStore introduces a dependency that trips the network gate; mitigation: run `verifyNoNetworking` immediately after dependency addition before feature work stacks on top.
- [ ] Risk: reference images bloat the APK or bring messy attribution; mitigation: set a size cap, resize before commit, add a metadata validator, and allow placeholder coverage.
- [ ] Risk: public-domain/CC images are not available for all species; mitigation: prioritize visible/new species and ship partial image coverage with explicit no-image state.
- [ ] Risk: theme work consumes the sprint without producing a pickable artifact; mitigation: keep theme candidates as palette/shape/type tokens over existing screens, require screenshots for each, and defer final polish until selection.
- [ ] Risk: KB expansion accidentally maps Pilea and exposes the known pothos→Pilea wrong-card issue; mitigation: keep and update the existing Pilea-absence guard in `HousePlantClassMapValidationTest`.
- [ ] Risk: three pillars produce too much instrumentation churn; mitigation: cover repositories and view models with JVM tests first, then run only targeted Compose/instrumentation flows plus final smoke gates.
- [ ] Risk: search containment regresses low-confidence manual selection; mitigation: preserve existing `LowConfidencePickerTags` where possible and update `LowConfidenceFlowTest`.

## Acceptance Criteria

- [ ] `PlantIdentifier.kt` and `IdentificationResult` are unchanged.
- [ ] `verifyNoNetworking` passes after all dependency and asset changes.
- [ ] `scripts/check-stub-isolation.sh` passes.
- [ ] The app has one shared local persistence implementation used by both My Plants and Add-this-plant request totals.
- [ ] Saved plants persist across app process recreation/device restart in the debug build.
- [ ] Add-this-plant request totals persist across app process recreation/device restart in the debug build.
- [ ] Strong-confidence unmapped model output shows the model match and an "Add this plant" button instead of a blank low-confidence-only experience.
- [ ] Tapping "Add this plant" increments a local-only count visible somewhere testable in the wireframe.
- [ ] Low-confidence weak/uncertain mapped-candidate behavior still works, including manual species search and archetype fallback.
- [ ] Result UI shows a numeric confidence percent and progress bar when confidence is available.
- [ ] Stub/no-confidence flows do not crash and do not require fake confidence.
- [ ] My Plants is reachable from normal app navigation and can tap through to the potting-mix recommendation for a saved species.
- [ ] Search species results are contained inside the search control/panel and are not an always-visible separate list.
- [ ] 2-3 theme candidates are selectable in the debug APK.
- [ ] Screenshots exist for each candidate theme on representative app screens.
- [ ] Reference images shown in the app are bundled, license-clean, attributed in repo metadata, and compressed.
- [ ] Species without a licensed bundled image render a clean placeholder/no-image state.
- [ ] Final debug APK size growth from reference images is recorded and stays within the agreed budget or the overage is explicitly justified.
- [ ] KB expansion adds only non-Pilea model classes and keeps the Pilea absence guard green.
- [ ] KB/class-map count tests are updated to exact post-sprint totals.
- [ ] App version is bumped from `0.3.0`/`3`.
- [ ] A debug APK is built for delivery.
- [ ] The sprint result notes clearly separate calibrated model behavior from editorial KB/class-map coverage.
