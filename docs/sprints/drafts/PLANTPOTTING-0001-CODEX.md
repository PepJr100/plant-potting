# PLANTPOTTING-0001 - Runnable Android Stub Flow

## Sprint Intent

Bootstrap the Houseplant Soil & Potting Guidance Android app from no code to a debug Android app that runs end to end on a real device:

launch app -> grant camera permission -> open camera -> take photo -> receive stub plant identification -> view a real substrate recommendation.

Identification is deliberately stubbed behind a replaceable interface. The recommendation knowledge base is not a stub: it must contain hand-curated, defensible substrate archetypes, recipes, rationales, and species mappings derived from `docs/Research_brief.md` and `docs/research/`.

## Goals

Success is falsifiable. By sprint end, a fresh checkout must build a debug APK, install on a real Android device, request camera permission, show a CameraX capture screen, capture one photo, pass that image to a `PlantIdentifier` interface, return a deterministic stub species from the local KB, and render a recommendation containing archetype name, ingredient proportions, and a one-sentence rationale.

This sprint directly exercises Research Brief hypotheses H1 and H2: a small common-species set can cover the first product slice, and common houseplant substrate needs can be represented as a small set of archetypes. It only creates the architecture seam for H3 and does not attempt real ML accuracy.

## Non-Goals

No real ML model, training, fine-tuning, LiteRT integration, backend, cloud API, analytics, accounts, Play Store release readiness, production signing, store metadata, Data Safety form, content rating, watering schedules, disease diagnosis, shopping links, or long-term photo storage.

## Tech Stack Proposal

Use Kotlin, Gradle Kotlin DSL, Android Gradle Plugin, version catalogs, Jetpack Compose Material 3, Navigation Compose, CameraX Preview and ImageCapture, Hilt, coroutines, `StateFlow`, JUnit, kotlinx-coroutines-test, AndroidX test, Compose UI tests, and GitHub Actions-compatible CI.

Rationale: this is the smallest modern Android stack that supports a real device camera flow, testable state-driven UI, dependency injection for the future ML seam, and a debug build path a single implementer can maintain over roughly two weeks. Store the KB as typed Kotlin seed data for sprint speed, with serialization-ready domain models so it can move to JSON or a reviewed content pipeline later.

## Task Plan

### Phase 0 - Research Extraction and Guardrails

- [ ] Read `docs/Idea.md` and add a one-paragraph implementation note describing the first-run product promise this sprint must satisfy.
- [ ] Read `docs/Research_brief.md` section 1.3 and record how this sprint tests H1 and H2, defers H3, and avoids overclaiming H4-H6.
- [ ] Read `docs/Research_brief.md` section 4.4 and extract the selected substrate archetypes and proportions into implementation notes.
- [ ] Read the three files under `docs/research/` and capture market evidence for the initial species list, explicitly including Gemini's 2025-2026 signals for Monstera, Ficus, Epipremnum, Dracaena/Sansevieria, ZZ, Phalaenopsis, Spathiphyllum, and Chlorophytum.
- [ ] Create `docs/kb/plant-substrate-kb-notes.md` with one source-backed note per archetype and one source-backed note per species mapping.

### Phase 1 - Android Scaffold, Build, and CI

- [ ] Scaffold a single Android app module named `app` with package `com.darkfactory.plantpotting`, Gradle wrapper, `settings.gradle.kts`, root `build.gradle.kts`, app `build.gradle.kts`, and `gradle/libs.versions.toml`.
- [ ] Configure compile SDK, min SDK, Kotlin, Compose compiler, Material 3, CameraX, Navigation Compose, Hilt, coroutines, JUnit, AndroidX test, and Compose test dependencies in the version catalog.
- [ ] Add `AndroidManifest.xml` with `MainActivity`, app theme, `android.permission.CAMERA`, and a non-required camera feature so installs are not blocked on camera-less test devices.
- [ ] Implement `MainActivity` hosting a Compose app shell with a Material 3 theme and three route placeholders: home, camera, recommendation.
- [ ] Add `.gitignore`, root `README.md` debug build/test commands, and `.github/workflows/android.yml` running unit tests and `assembleDebug`.
- [ ] Add a `scripts/check-android.ps1` wrapper that runs the same local build/test commands expected in CI.
- [ ] Test task: add a smoke unit test proving the app test runner is configured and `testDebugUnitTest` discovers tests.
- [ ] Test task: run `.\gradlew testDebugUnitTest assembleDebug` locally and record the command and result in sprint notes.
- [ ] Test task: run `scripts/check-android.ps1` locally and verify it fails fast on build or unit-test failure.

### Phase 2 - Domain Model and Real Knowledge Base

- [ ] Define domain models `PlantSpecies`, `SubstrateArchetype`, `IngredientProportion`, `SubstrateRecipe`, `Recommendation`, and stable string IDs suitable for future serialized data.
- [ ] Implement KB validation rules: recipe proportions total 100, archetype IDs are unique, species IDs are unique, aliases are unique after normalization, every species maps to one recommendation path, and every rationale is exactly one sentence.
- [ ] Enter archetype `standard_houseplant`: coir 60 percent, perlite 30 percent, bark fines 10 percent; target general foliage plants needing balanced moisture and aeration.
- [ ] Enter archetype `aroid_chunky`: pine/orchid bark 40 percent, coco coir 25 percent, perlite or pumice 20 percent, sphagnum 10 percent, horticultural charcoal 5 percent; target climbing/hemi-epiphytic aroids.
- [ ] Enter archetype `succulent_gritty`: pumice or akadama 40 percent, coarse sand or perlite 30 percent, coir or pine fines 30 percent; target drought-tolerant succulent-rooted houseplants.
- [ ] Enter archetype `cactus_mineral`: pumice 50 percent, akadama or lava rock 30 percent, coarse sand 20 percent; target cacti and highly rot-sensitive succulents.
- [ ] Enter archetype `epiphytic_orchid_bark`: medium bark 40 percent, sphagnum moss 30 percent, perlite 20 percent, charcoal 10 percent; target Phalaenopsis-type epiphytic orchids.
- [ ] Enter archetype `moisture_retentive`: coir 50 percent, fine bark or composted bark 20 percent, perlite 20 percent, long-fiber sphagnum 10 percent; target plants that want even moisture without stagnant roots.
- [ ] Enter archetype `acidic_ericaceous`: coir or peat-free acidic base 45 percent, pine bark fines 35 percent, perlite 15 percent, horticultural sulfur or acidic amendment allowance 5 percent; target acid-leaning houseplants in this first KB.
- [ ] Enter species mappings: `Monstera deliciosa`, `Epipremnum aureum`, and `Philodendron hederaceum` to `aroid_chunky`, each with common names and aliases.
- [ ] Enter species mappings: `Ficus lyrata`, `Ficus elastica`, and `Chlorophytum comosum` to `standard_houseplant`, each with common names and aliases.
- [ ] Enter species mappings: `Dracaena trifasciata` with alias `Sansevieria trifasciata`, `Zamioculcas zamiifolia`, `Aloe vera`, and `Peperomia obtusifolia` to `succulent_gritty`, with rationale for drought tolerance or semi-succulent roots.
- [ ] Enter species mappings: `Mammillaria elongata` or another common retail cactus representative to `cactus_mineral`, and `Phalaenopsis amabilis` or genus-level `Phalaenopsis` to `epiphytic_orchid_bark`.
- [ ] Enter species mappings: `Spathiphyllum wallisii`, `Goeppertia orbifolia` alias `Calathea orbifolia`, and `Begonia rex-cultorum` to `moisture_retentive`.
- [ ] Enter species mapping: `Saintpaulia ionantha` alias African violet to `acidic_ericaceous`, with rationale documenting the acidic/light organic mix choice.
- [ ] Implement `RecommendationRepository` and `GetRecommendationForSpeciesUseCase` with typed success and missing-mapping results.
- [ ] Test task: write KB validation tests before data entry and make them fail on intentionally invalid fixture data.
- [ ] Test task: assert every production recipe totals exactly 100 percent and has at least three rendered ingredient lines.
- [ ] Test task: assert every production species resolves to one recommendation with common name, scientific name, archetype name, recipe, and rationale.
- [ ] Test task: assert alias lookup resolves `Sansevieria trifasciata` and `Dracaena trifasciata` to the same canonical species.
- [ ] Test task: assert every rationale is one sentence and no recommendation renders placeholder text such as `TODO`, `stub`, or `lorem`.

### Phase 3 - Identification Seam and Stub

- [ ] Define `PlantIdentifier` as an injectable interface accepting captured image URI or bytes plus lightweight metadata and returning `IdentifiedPlant`.
- [ ] Define `IdentifiedPlant` with canonical species ID, display confidence label, source type, and debug note.
- [ ] Implement `StubPlantIdentifier` in deterministic mode returning one KB species, defaulting to `Monstera deliciosa` unless overridden for tests.
- [ ] Add debug-only random mode constrained to species present in the KB, while keeping deterministic mode the default for integration tests.
- [ ] Bind `PlantIdentifier`, `RecommendationRepository`, and use cases through Hilt modules.
- [ ] Test task: assert deterministic stub mode returns the configured species and does not inspect image content.
- [ ] Test task: assert random stub mode only returns species IDs present in the KB.
- [ ] Test task: assert ViewModels depend on the `PlantIdentifier` interface and can run with a fake identifier.

### Phase 4 - App State, Permission, Navigation, and Recommendation UI

- [ ] Implement routes `HomeRoute`, `CameraRoute`, and `RecommendationRoute` with Navigation Compose and typed navigation arguments or a shared ViewModel state holder.
- [ ] Implement camera permission state handling with first-run request, granted state, denied state, and retry/open-settings copy appropriate for a debug prototype.
- [ ] Implement `HomeViewModel` and/or app state holder tracking permission state, latest capture reference, identification loading, identified species, and recommendation result.
- [ ] Implement home screen with app title, camera permission CTA, denied-permission state, and open-camera CTA when permission is granted.
- [ ] Implement recommendation loading, success, and missing-mapping error states.
- [ ] Implement recommendation success UI showing captured-photo thumbnail or placeholder, identified species, confidence label, stub source note, archetype name, recipe ingredient proportions, and rationale.
- [ ] Add stable Compose test tags for permission CTA, open camera CTA, capture button, recommendation title, archetype name, recipe list, and rationale.
- [ ] Test task: write ViewModel tests for permission denied -> retry -> granted state transitions.
- [ ] Test task: write ViewModel tests for captured image -> identify -> recommendation success.
- [ ] Test task: write ViewModel tests for missing KB mapping error and retry path.
- [ ] Test task: write Compose UI tests for home screen CTA visibility in granted and denied states.
- [ ] Test task: write Compose UI tests for recommendation rendering of archetype, recipe proportions, rationale, and stub source note.

### Phase 5 - CameraX Capture and End-to-End Validation

- [ ] Implement CameraX `PreviewView` interop inside a Compose `CameraCaptureScreen`, binding preview lifecycle to the current lifecycle owner.
- [ ] Implement single-tap capture with CameraX `ImageCapture.takePicture`, writing a temporary JPEG in app cache and returning the URI to the ViewModel.
- [ ] Disable the capture button and show loading while capture, identification, and recommendation lookup are in progress.
- [ ] On successful capture, call `PlantIdentifier`, resolve `Recommendation`, and navigate to `RecommendationRoute` without requiring a backend or model file.
- [ ] Add a fake camera/capture path for tests so integration can drive the flow without relying on emulator camera hardware.
- [ ] Create `scripts/integration-flow.ps1` that assembles debug, installs to a connected device or emulator, grants camera permission with `adb`, launches the app, drives the fake-camera happy path, captures screenshots or UI dump, and writes artifacts under an ignored `artifacts/` directory.
- [ ] Make `scripts/integration-flow.ps1` compare the generated artifact manifest against a checked-in expected manifest and print a diff; acceptance/integration tasks may not be marked done without this diff.
- [ ] Add `docs/sprints/results/PLANTPOTTING-0001.md` template with fields for build output, unit-test output, integration artifact diff, real-device model, Android version, and known gaps.
- [ ] Test task: write orchestration unit tests using fake capture and fake identifier to prove capture triggers identification and recommendation lookup once.
- [ ] Test task: write a Compose navigation test proving fake capture lands on the recommendation screen.
- [ ] Test task: run connected integration script on emulator or connected device and attach/record the artifact diff in sprint results.
- [ ] Test task: run one manual real-device smoke test using the physical camera path and record launch, permission grant, preview visible, capture success, and recommendation visible.

## Sequencing and Dependencies

- [ ] Phase 0 must finish before KB data is treated as final, because species choices and recipes need traceability.
- [ ] Phase 1 must finish before feature work, because build, package structure, and test commands are shared dependencies.
- [ ] Phase 2 must finish before random stub identification, because the stub may only emit species with valid KB recommendations.
- [ ] Phase 3 must finish before camera orchestration, because UI and ViewModels must depend on the future-replaceable identification seam.
- [ ] Phase 4 must finish before CameraX integration is considered complete, because capture needs a tested destination state.
- [ ] Phase 5 must finish before sprint acceptance, because the end-to-end flow and diff-producing integration script are the evidence for done-ness.

## Risks and Mitigations

- [ ] Risk: CameraX is hard to automate on CI or emulators. Mitigation: keep a fake capture path for tests and require one manual real-device smoke test for the physical camera path.
- [ ] Risk: Android runtime permission dialogs are flaky in UI automation. Mitigation: unit-test permission state logic and use `adb pm grant` in the integration script for the happy path.
- [ ] Risk: KB content drifts into plausible but unsourced advice. Mitigation: no KB entry is done until `docs/kb/plant-substrate-kb-notes.md` has a source-backed note for it.
- [ ] Risk: Taxonomy aliases break lookup, especially `Sansevieria` to `Dracaena` and `Calathea` to `Goeppertia`. Mitigation: canonical IDs, normalized aliases, and explicit alias unit tests.
- [ ] Risk: Recipe text becomes untestable prose. Mitigation: store ingredients as numeric proportions and render UI from structured data only.
- [ ] Risk: Demo viewers mistake stub identification for real ML. Mitigation: show a debug stub source note in the result while keeping substrate recommendations real.
- [ ] Risk: Greenfield Android setup consumes too much sprint time. Mitigation: use standard Compose/CameraX/Hilt patterns and defer persistence, release hardening, and real ML.

## Acceptance Criteria

- [ ] `.\gradlew testDebugUnitTest assembleDebug` passes from a clean checkout.
- [ ] A debug APK installs and launches on a real Android device.
- [ ] First launch requests camera permission through the Android runtime permission flow.
- [ ] Permission denial shows a recoverable denied state.
- [ ] Permission grant allows opening a CameraX camera screen with visible preview on a real device.
- [ ] Tapping one capture control captures a still image without crashing.
- [ ] Capture invokes `PlantIdentifier` and receives a deterministic stub species from the KB.
- [ ] The app navigates to a recommendation screen after identification.
- [ ] The recommendation screen displays species name, confidence/source note, archetype name, ingredient recipe with proportions, and one-sentence rationale.
- [ ] The KB contains exactly 7 active archetypes and 16 production species mappings listed in Phase 2.
- [ ] Unit tests validate KB integrity, recipe totals, alias resolution, stub identifier behavior, and ViewModel state transitions.
- [ ] Compose or instrumentation tests validate home/permission UI, recommendation rendering, and fake-camera navigation.
- [ ] The integration script demonstrates launch-to-recommendation flow and prints a diff against an expected artifact manifest.
- [ ] No acceptance or integration checkbox is marked done unless the integration script produced the required diff.
- [ ] `docs/sprints/results/PLANTPOTTING-0001.md` records build commands, test commands, integration artifacts, real-device smoke-test result, and known limitations.

## Definition of Done

- [ ] Every completed feature task has a paired completed unit, Compose, instrumentation, or integration test task.
- [ ] Every KB entry has source notes tying it back to the research brief or deep-research reports.
- [ ] The real-device happy path works end to end: launch, permission, camera, capture, stub identification, recommendation.
- [ ] The code contains a clear replacement point for a future on-device model.
- [ ] Known gaps are documented in sprint results rather than hidden as TODO-only code comments.
