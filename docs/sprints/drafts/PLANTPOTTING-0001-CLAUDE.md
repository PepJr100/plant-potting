# PLANTPOTTING-0001 — Android scaffold + end-to-end stub flow with KB

**Author:** CLAUDE draft
**Sprint window:** ~2 weeks of a single AI implementer
**Status:** draft for cross-critique

---

## 1. Goals & non-goals

### 1.1 The single observable success bar

On a real Android device (debug build, side-loaded), a user can complete this flow without crashes, without a stale screen, and without seeing placeholder copy in the recommendation:

1. Launch the app from the launcher.
2. Be asked for camera permission with a one-screen rationale; tap **Allow**.
3. Land on a CameraX live preview, frame a plant, tap a shutter button once.
4. Within ≤3 seconds, see a result screen showing a stubbed identification (one species name from the bundled KB, plus the placeholder text "Stub identifier — replace in PLANTPOTTING-000X") and a **See potting mix** button.
5. Tap the button, land on a recommendation screen showing:
   - The substrate **archetype name** (e.g., *Aroid Chunky*),
   - A **recipe table** of ingredients with proportions that sum to 100%,
   - A **one-sentence rationale** that names the species and explains why this archetype suits it.
6. Use the system Back gesture to return to the camera and repeat.

The denied-permission path must also work: the camera screen shows a polite explanation and a button that deep-links to system settings. No crash.

### 1.2 Falsifiability — how we know we hit it

- `./gradlew assembleDebug` produces an installable APK on a clean checkout with only the Android SDK pre-installed.
- A Macrobenchmark-free smoke run on a Pixel-class emulator (API 34) and on **one real mid-range device** (Pixel 6a, Galaxy A5x, Redmi Note 1x, or equivalent — minSdk 26) completes the flow described in §1.1.
- The implementer captures the device run as a short screen recording or a sequence of screenshots committed to `docs/sprints/evidence/PLANTPOTTING-0001/`.
- `./gradlew test connectedDebugAndroidTest lint ktlintCheck` is green in CI.
- The end-to-end instrumentation test described in §2 Phase 7 passes on a managed-device API-34 emulator in CI.

### 1.3 Non-goals (carry to later sprints)

- No real ML model — the identifier is a deterministic stub behind an interface.
- No backend, no cloud calls of any kind. The app is fully offline this sprint.
- No release-signing, Play Store listing, Data Safety form, content rating, or store screenshots. Debug build only.
- No Room database, no DataStore persistence — the KB is bundled JSON read once at startup. We will introduce a database only when we have something to persist (user's plant log, captured photos, feedback).
- No analytics, crash reporting, Firebase, or any external SDK that phones home.
- No camera "live guidance" overlay, no multi-shot capture, no manual-refine UX, no confidence visualization. Single-shot, single-result this sprint.
- No accessibility audit beyond Compose-default semantics + content descriptions on interactive elements (we promise WCAG-grade work in a later sprint, not this one).
- No internationalization beyond English strings extracted to `strings.xml`.
- No dark-mode tuning, no tablet/foldable adaptive layouts.

### 1.4 What is *real* this sprint (not stubbed)

The **knowledge base** — archetype catalogue, species mappings, recipes with proportions, and rationale text — is hand-curated from Research Brief §4.4 and the Gemini deep-research report on top houseplants 2025–2026. The recommendation engine reads this KB, not stub strings. Anyone reviewing the diff must be able to defend each species→archetype mapping against the brief.

---

## 2. Task list

Every line is a checkbox the implementer ticks as they finish. Test tasks are intentionally listed *next to* the feature they cover; do not let any feature task land green while its paired test task is open.

### Phase 0 — Repository and toolchain scaffold

- [ ] **0.1** Add `.gitignore` covering `*.iml`, `.gradle/`, `build/`, `local.properties`, `.idea/`, `*.keystore`, `captures/`, `.cxx/`, and `app/release/`.
- [ ] **0.2** Initialise the project as an Android single-module app: `app/` with `applicationId = "com.darkfactory.plantpotting"`, `compileSdk = 34`, `targetSdk = 34`, `minSdk = 26`, Kotlin JVM target 17, Compose enabled, viewBinding disabled. Use Gradle Kotlin DSL (`build.gradle.kts`, `settings.gradle.kts`) and a `gradle/libs.versions.toml` version catalogue.
- [ ] **0.3** Pin the Android Gradle Plugin and Kotlin versions in `libs.versions.toml`. Add catalog entries for: Compose BOM, CameraX (`androidx.camera:*`), Hilt, Kotlinx Serialization, Kotlinx Coroutines, AndroidX Activity-Compose, AndroidX Navigation-Compose, Material3, Lifecycle-ViewModel-Compose, JUnit4, Truth, MockK, Turbine, Robolectric, Compose UI Test, Espresso, AndroidX Test Runner/Rules, UI Automator.
- [ ] **0.4** Configure the Gradle wrapper (Gradle 8.x compatible with the chosen AGP) and commit `gradle/wrapper/`. Verify `./gradlew --version` from a clean shell.
- [ ] **0.5** Add **detekt** (config in `config/detekt/detekt.yml`) and **ktlint** (via `org.jlleitschuh.gradle.ktlint` Gradle plugin) with default profiles plus a `// no-newlines-required` exception list of zero. Wire `check` to depend on `ktlintCheck detekt`.
- [ ] **0.6** Commit an `AndroidManifest.xml` declaring the launcher activity (Compose host) and `<uses-permission android:name="android.permission.CAMERA"/>` plus the `<uses-feature android:name="android.hardware.camera.any" android:required="false"/>` declaration so the app remains installable on Chromebooks/emulators without a camera.
- [ ] **0.7** Add a `.github/workflows/ci.yml` GitHub Actions job that runs on `ubuntu-latest`, sets up JDK 17, caches `~/.gradle/caches`, and runs `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck detekt`. The workflow must fail on any task failure.
- [ ] **0.8** Add a top-level `README.md` (≤40 lines) covering: prerequisites (JDK 17, Android SDK 34), how to run `assembleDebug`, how to run unit + instrumentation tests, how to install on a device. No marketing copy.

### Phase 1 — Knowledge base: schema, data, and repository

- [ ] **1.1** Define Kotlin data classes in `:app` under `kb/model/`:
  - `data class Archetype(val id: String, val displayName: String, val shortDescription: String, val recipe: List<RecipeIngredient>, val rationaleTemplate: String, val citations: List<String>)`
  - `data class RecipeIngredient(val ingredient: String, val proportionPct: Int, val notes: String? = null)`
  - `data class Species(val id: String, val scientificName: String, val commonNames: List<String>, val mapping: ArchetypeMapping, val speciesRationale: String, val citations: List<String>)`
  - `sealed class ArchetypeMapping { data class Single(val archetypeId: String) : ArchetypeMapping(); data class Blend(val primaryArchetypeId: String, val secondaryArchetypeId: String, val primaryPct: Int) : ArchetypeMapping() }`
  All use `@Serializable` (Kotlinx Serialization).
- [ ] **1.2** Author `app/src/main/assets/kb/archetypes.json` with **7 archetypes** drawn from Research Brief §4.4: `standard-houseplant`, `aroid-chunky`, `succulent-gritty`, `cactus-pure-mineral`, `epiphytic-orchid-bark`, `semi-hydro-inert`, `moisture-retentive`. Each must have a recipe whose `proportionPct` values sum to exactly 100, a one-sentence `shortDescription`, a `rationaleTemplate` containing the literal `{species}` placeholder, and at least one citation pointer back to `docs/Research_brief.md §4.4` or a deep-research report section.
- [ ] **1.3** Author `app/src/main/assets/kb/species.json` with **16 species** chosen on market evidence from `docs/research/deep-research-Gemini.md` and Brief §3:
  1. `Monstera deliciosa` → `aroid-chunky`
  2. `Monstera adansonii` → `aroid-chunky`
  3. `Epipremnum aureum` (Pothos / Devil's Ivy) → `aroid-chunky`
  4. `Philodendron hederaceum` (Heartleaf Philodendron) → `aroid-chunky`
  5. `Philodendron erubescens 'Pink Princess'` → `aroid-chunky` *(variegated; species rationale must explicitly mention slower transpiration; cites Brief H5 and Gemini report §"Slower Transpiration")*
  6. `Spathiphyllum wallisii` (Peace Lily) → `moisture-retentive`
  7. `Ficus lyrata` (Fiddle-Leaf Fig) → `standard-houseplant`
  8. `Ficus elastica` (Rubber Plant) → `standard-houseplant`
  9. `Dracaena trifasciata` (Snake Plant; formerly *Sansevieria trifasciata*, commonName must include both) → `succulent-gritty`
  10. `Zamioculcas zamiifolia` (ZZ Plant) → `succulent-gritty`
  11. `Chlorophytum comosum` (Spider Plant) → `standard-houseplant`
  12. `Phalaenopsis` *(genus-level — the retail purchase is almost never identified to species)* → `epiphytic-orchid-bark`
  13. `Goeppertia orbifolia` (commonNames include *Calathea orbifolia*) → `moisture-retentive`
  14. `Crassula ovata` (Jade Plant) → `succulent-gritty`
  15. `Echeveria` *(genus-level)* → `succulent-gritty`
  16. `Hoya carnosa` → **blend** `{primary: aroid-chunky, secondary: succulent-gritty, primaryPct: 60}` *(demonstrates the blend codepath; rationale explains its epiphytic + drought-tolerant duality)*
  Every entry has a `speciesRationale` of one to three sentences and at least one citation pointer.
- [ ] **1.4** Implement `KbLoader` in `kb/KbLoader.kt`: a class that on first call parses `archetypes.json` and `species.json` from `AssetManager`, validates the data, and exposes an in-memory immutable `KnowledgeBase` (a record of `archetypes: Map<String, Archetype>` and `species: List<Species>`). Loader is suspendable and cached after first success.
- [ ] **1.5** Implement validation rules inside `KbLoader.validate(...)`:
  - Every `Species.mapping` archetype id resolves to a known `Archetype.id`.
  - Every `Archetype.recipe` proportion sums to 100 (integer equality, no float fuzz).
  - Every `Archetype.rationaleTemplate` contains the literal substring `{species}`.
  - No duplicate `Species.id` or `Archetype.id`.
  - `Blend.primaryPct` is in `[10, 90]` and both archetype ids resolve.
  Any failure throws `KbValidationException` with the offending id(s) listed.
- [ ] **1.6 (test)** Add `KbLoaderTest` (JVM unit test, Robolectric for `AssetManager` shim or fakes for the asset stream): asserts that the bundled JSON loads, validates, and exposes 7 archetypes and 16 species.
- [ ] **1.7 (test)** Add `KbContentTest` (pure JVM): for the loaded KB, assert every archetype recipe sums to 100, every species rationale is non-empty, no species's `scientificName` is duplicated, and every blend's referenced archetype ids exist.
- [ ] **1.8 (test)** Add `KbValidationTest`: for each invalid scenario (unknown archetype id, recipe summing to 99, missing `{species}` placeholder, duplicate id, blend with `primaryPct = 0`), feed a hand-written invalid JSON string and assert `KbValidationException` is thrown with a message that names the failure.

### Phase 2 — Recommendation engine

- [ ] **2.1** Define the engine contract in `recommend/RecommendationEngine.kt`: `interface RecommendationEngine { fun recommend(speciesId: String): Recommendation }` with `data class Recommendation(val archetypeName: String, val recipe: List<RecipeIngredient>, val rationale: String, val isBlend: Boolean)`.
- [ ] **2.2** Implement `KbRecommendationEngine(kb: KnowledgeBase) : RecommendationEngine`:
  - For a `Single` mapping, return the named archetype's recipe verbatim and a rationale produced by substituting `{species}` in the archetype's `rationaleTemplate` and prefixing the `speciesRationale` sentence.
  - For a `Blend`, compute a merged recipe by scaling each contributor's percentages by `primaryPct`/`secondaryPct`, summing per ingredient, then re-normalising to integer percentages whose sum is exactly 100 (allocate any rounding remainder to the largest-proportion ingredient deterministically). The displayed `archetypeName` is `"${primary.displayName} / ${secondary.displayName} blend"`.
- [ ] **2.3 (test)** Add `KbRecommendationEngineTest` covering, with hand-built `KnowledgeBase` fixtures:
  - A `Single`-mapped species returns the exact archetype recipe and a rationale containing the species's scientific name.
  - A `Blend`-mapped species returns a recipe whose proportions sum to exactly 100.
  - An unknown `speciesId` raises `IllegalArgumentException("unknown species: …")`.
- [ ] **2.4 (test)** Add `RecommendationGoldenTest`: for every species in the *real* bundled KB, call `engine.recommend(speciesId)` and assert the returned `Recommendation.recipe` sums to 100 and the rationale text contains the species's scientific name. This catches future KB edits that silently break the engine.
- [ ] **2.5** Wire the engine into Hilt as `@Singleton` provided by `RecommendModule`.

### Phase 3 — Identification: the stub behind the seam

- [ ] **3.1** Define `identify/PlantIdentifier.kt`: `interface PlantIdentifier { suspend fun identify(jpeg: ByteArray): IdentificationResult }` with `data class IdentificationResult(val speciesId: String, val displayName: String, val source: IdSource)` and `enum class IdSource { STUB_RANDOM, ON_DEVICE_MODEL, CLOUD }`. The point is that the contract — bytes in, structured result out — is the seam a real LiteRT/ONNX implementation will slot into in a later sprint without changing call sites.
- [ ] **3.2** Implement `StubPlantIdentifier(kb: KnowledgeBase, random: Random = Random.Default) : PlantIdentifier`. It does *not* read the bytes; it picks one species deterministically from a seeded RNG and returns it with `IdSource.STUB_RANDOM`. Document in a KDoc on the class — single line — that this is a placeholder for PLANTPOTTING-000X to replace.
- [ ] **3.3 (test)** Add `StubPlantIdentifierTest`: with a fixed seed, the same input bytes produce a stable species pick; the returned `speciesId` is always one of the KB's species ids; `IdSource` is always `STUB_RANDOM`.
- [ ] **3.4** Wire `PlantIdentifier` into Hilt as `@Singleton`, bound to `StubPlantIdentifier`. Crucially, every consumer (the ViewModel) depends on the interface, never on the stub class — this is the seam.

### Phase 4 — App shell, navigation, and permissions

- [ ] **4.1** Implement `PlantPottingApplication : Application` annotated with `@HiltAndroidApp`, and `MainActivity : ComponentActivity` hosting `setContent { PlantPottingTheme { PlantPottingNavHost() } }`. No fragments, no XML layouts.
- [ ] **4.2** Define a Material 3 theme in `ui/theme/` with light and dark colour schemes (default dynamic-colour off this sprint to keep variability low). Typography stays at Material defaults.
- [ ] **4.3** Implement `PlantPottingNavHost` using `androidx.navigation:navigation-compose`. Routes: `permission`, `camera`, `result/{speciesId}`, `recommendation/{speciesId}`. The `speciesId` arg is a `StringType` Nav argument, URL-encoded on the way in. Default `startDestination = "permission"`.
- [ ] **4.4** Implement `PermissionScreen`: a Compose screen with a single-line rationale ("We use the camera to photograph your plant. We never upload your photos."), a primary **Grant camera access** button that triggers `ActivityResultContracts.RequestPermission`, and a secondary **Why we ask** disclosure expanding to two paragraphs. On grant, navigate to `camera`. On denial-with-don't-ask-again, swap the primary button to **Open system settings** with an intent to `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`.
- [ ] **4.5 (test)** Add `PermissionScreenTest` (Compose UI test with a fake permission contract): assert the rationale text is visible, the **Grant camera access** button is present, and tapping a stubbed "granted" result navigates to `camera`.
- [ ] **4.6** On every screen launch, call a `CameraPermissionGuard` extracted into `permission/CameraPermissionGuard.kt` that re-reads the current permission state — never trust stale Compose state across process death.

### Phase 5 — Camera capture

- [ ] **5.1** Add CameraX dependencies (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`) and a `CameraXModule` for Hilt that exposes the `ProcessCameraProvider` as a singleton.
- [ ] **5.2** Implement `CameraPreview` composable wrapping `PreviewView` via `AndroidView`, binding `Preview` + `ImageCapture` use cases to the `LocalLifecycleOwner`. Aspect ratio is `AspectRatio.RATIO_4_3`, target rotation tracked from the activity's display rotation. Use the back camera by default.
- [ ] **5.3** Implement `CameraScreen(viewModel: CameraViewModel)`: full-screen preview, a single 72 dp circular shutter button at the bottom centre, and a small loading overlay during inflight identification. Tapping the shutter calls `ImageCapture.takePicture(executor, ImageCapture.OnImageCapturedCallback)`, converts the resulting `ImageProxy` to a JPEG `ByteArray` on a background dispatcher, and forwards bytes to the ViewModel.
- [ ] **5.4** Implement `CameraViewModel` (Hilt-injected `PlantIdentifier` and a `Navigator` event flow). State machine: `Idle → Capturing → Identifying → Success(speciesId) | Failure(reason)`. On `Success`, emit a navigate-to-`result/{speciesId}` event; on `Failure`, surface a Snackbar via state.
- [ ] **5.5 (test)** Add `CameraViewModelTest` (JVM, coroutines-test, Turbine): with a fake `PlantIdentifier` returning a known species, assert the state transitions are `Idle → Capturing → Identifying → Success` and the navigation event carries the expected species id. Assert the failure transition fires when the fake identifier throws.
- [ ] **5.6 (test)** Add `CameraScreenSmokeTest` (Compose UI test with the camera bound to a fake `ImageCapture` provider): assert the shutter button is visible and enabled, that tapping it disables the button until the ViewModel returns to `Idle`. *Do not* attempt to assert real preview rendering — that is covered by the on-device run in §7.

### Phase 6 — Result and recommendation screens

- [ ] **6.1** Implement `ResultScreen(speciesId: String, viewModel: ResultViewModel)`: a column showing the species's `scientificName` (italic), the most common name in parentheses, the literal stub badge ("Stub identifier — replace in a later sprint"), and a primary **See potting mix** button that navigates to `recommendation/{speciesId}`.
- [ ] **6.2** Implement `ResultViewModel`: loads `Species` by id from the KB; exposes `data class ResultUiState(val scientificName: String, val commonName: String, val notFound: Boolean)`.
- [ ] **6.3 (test)** Add `ResultViewModelTest`: with a fake KB, asserts the UI state is populated for a known id and `notFound = true` for an unknown id.
- [ ] **6.4** Implement `RecommendationScreen(speciesId: String, viewModel: RecommendationViewModel)`: shows the archetype name as the screen title, a single-paragraph rationale, and a table of `(ingredient, proportion%)` rows. Each row uses Material 3 list-item layout with the proportion right-aligned as `45%`. When `isBlend = true`, surface a small "Blend recipe" chip near the title.
- [ ] **6.5** Implement `RecommendationViewModel(engine: RecommendationEngine)`: on creation, runs `engine.recommend(speciesId)` and exposes a `RecommendationUiState` derived from it.
- [ ] **6.6 (test)** Add `RecommendationViewModelTest`: with a fake engine, assert the UI state is populated from the engine's `Recommendation`. With a fake engine that throws for an unknown id, assert the UI state becomes `RecommendationUiState.NotFound`.
- [ ] **6.7 (test)** Add `RecommendationScreenTest` (Compose UI test, hilt-android-testing): for each of three representative species (one Single, the Hoya Blend, one `moisture-retentive` mapping), render the screen and assert the archetype name, the rationale containing the scientific name, and the visible recipe rows including the correct numeric proportions.
- [ ] **6.8** Add string resources for all visible copy in `res/values/strings.xml`. No hard-coded user-visible strings in composables.

### Phase 7 — End-to-end integration and acceptance

- [ ] **7.1** Add an instrumentation test module configuration: `androidTestImplementation` deps for Compose UI test, Espresso, UI Automator, Hilt testing, and the AndroidX Test Orchestrator.
- [ ] **7.2 (integration test)** Add `EndToEndFlowTest` (in `androidTest`) that launches `MainActivity` with a Hilt test rule that swaps the real `PlantIdentifier` for a `FakeFixedIdentifier` returning a deterministic species id. The test then:
  1. Asserts the permission screen is shown.
  2. Grants camera permission via `UiAutomator` interacting with the system dialog (or via a `GrantPermissionRule`).
  3. Asserts the camera screen is shown (preview view present, shutter button enabled).
  4. Taps the shutter.
  5. Asserts navigation to the result screen and the presence of the fake species's scientific name.
  6. Taps **See potting mix**.
  7. Asserts the recommendation screen shows the expected archetype name, a rationale containing the scientific name, and ≥3 recipe rows whose displayed proportions sum to 100.
  The test runs on the API-34 Gradle Managed Device declared in `app/build.gradle.kts`.
- [ ] **7.3 (integration test)** Add `PermissionDeniedFlowTest`: simulates camera-permission denial and asserts the **Open system settings** button is shown and emits the expected intent (verified via `Intents.intended(...)`).
- [ ] **7.4** Configure a Gradle Managed Device (`pixel6Api34` with `automotive = false`, `aosp` system image) in `app/build.gradle.kts` and wire `pixel6Api34DebugAndroidTest` into the CI job from §0.7. CI must run on every PR.
- [ ] **7.5** On a physical device (the implementer's daily phone or a borrowed Pixel 6a / Galaxy A5x), install the debug APK, manually run the §1.1 flow once with permission granted and once with permission denied, and commit a screen recording or four screenshots to `docs/sprints/evidence/PLANTPOTTING-0001/`. Without this artefact the sprint is not done.
- [ ] **7.6** Author `docs/sprints/done/PLANTPOTTING-0001-DONE.md` summarising: what shipped, which §1 acceptance lines went green, what was deferred, and an explicit handoff note pointing to the `PlantIdentifier` seam as the next sprint's entry point.

### Phase 8 — Lightweight discipline tasks (run in parallel)

- [ ] **8.1** Configure `org.gradle.parallel=true` and `org.gradle.caching=true` in `gradle.properties`. Document Gradle JDK version (17) in `README.md`.
- [ ] **8.2** Add a `PULL_REQUEST_TEMPLATE.md` requiring: a screenshot or recording of the affected screen for any UI change, and an explicit "test plan" section. (Light governance, not a process tax.)
- [ ] **8.3** Add a `CODEOWNERS` placeholder mapping `/app/kb/` to the project lead — even greenfield, signal that the KB is editorially load-bearing.
- [ ] **8.4** Verify `./gradlew :app:dependencies --configuration releaseRuntimeClasspath` lists no unexpected transitive networking libraries (no OkHttp, no Retrofit, no Firebase). This sprint must remain fully offline; catch accidental network deps early.

---

## 3. Sequencing — what unblocks what

```
Phase 0 (scaffold)  ──►  Phase 1 (KB schema + data + loader)
                                 │
                                 ├──►  Phase 2 (engine, needs KB)
                                 │
                                 └──►  Phase 3 (stub identifier, needs KB only for the species id pool)
                                                 │
                                                 ▼
                          Phase 4 (nav + permission UI) ─►  Phase 5 (camera)
                                                                     │
                                                                     ▼
                                                  Phase 6 (result + recommendation screens)
                                                                     │
                                                                     ▼
                                                          Phase 7 (E2E + acceptance)
Phase 8 is parallel discipline, no hard deps.
```

Concrete dependency rules the implementer must respect:

- **Phase 1 must complete before Phase 2 and Phase 3.** Both depend on `KnowledgeBase` being loadable.
- **Phase 1.6/1.7/1.8 (the KB tests) must be green before any task in Phase 2 or 3 begins.** Otherwise we're building the engine on quicksand.
- **Phase 4 can run in parallel with Phases 1–3** because the permission screen and the NavHost don't need real KB. But Phase 5 cannot start until Phase 3 is done (the camera ViewModel injects `PlantIdentifier`).
- **Phase 6 depends on Phase 2** (engine) and Phase 4 (nav).
- **Phase 7 is the last gate.** It cannot start until every prior phase's tests are green.

If the implementer is tempted to start Phase 5 before Phase 3 is done because "the camera is more fun" — they should be redirected. The end-to-end flow is the deliverable; the camera is one node in it.

---

## 4. Risks and mitigations

### Risk: CameraX surface lifecycle bugs on a real device
Lifecycle-bound `Preview` use cases regularly break in subtle ways on configuration change (rotation, multi-window, back-from-background) on specific OEM ROMs. The emulator hides most of these.
**Mitigation:** §7.5 (physical-device smoke test) is a non-skippable acceptance task. The implementer must also test landscape rotation manually and confirm no crash. If a specific OEM bug surfaces, fall back to locking orientation to portrait for this sprint and file a follow-up.

### Risk: KB JSON schema drifts silently between commits
A misformatted recipe (sums to 99 because someone changed 30→29) won't crash — it will ship a wrong recommendation.
**Mitigation:** §1.5 strict validation runs on every load; §1.7 `KbContentTest` runs in CI on every push; §2.4 `RecommendationGoldenTest` re-validates from the engine side. Three independent fences.

### Risk: Permission UX traps the user on "don't ask again"
On Android 11+, a second denial silently enables "don't ask again". A naive implementation leaves the user with a permission screen whose **Grant** button does nothing.
**Mitigation:** §4.4 explicitly handles this with an **Open system settings** fallback. §7.3 covers it as an instrumentation test.

### Risk: The implementer over-designs the `PlantIdentifier` seam
A common failure for a stub-with-a-seam is to ship an over-abstracted `IdentifierFactory<Strategy<Result>>`. The interface in §3.1 is the maximum allowed surface area this sprint. If the implementer wants more, it goes in PLANTPOTTING-000X.
**Mitigation:** Code review explicitly checks the seam stays at one `suspend fun identify(jpeg: ByteArray): IdentificationResult`. Any additional method is a justification trigger.

### Risk: Hilt + Compose + tests bring more configuration than the value of the sprint
Hilt instrumentation tests can be fragile to set up.
**Mitigation:** Use `@HiltAndroidTest` + `HiltAndroidRule` only in the integration tests that *need* it (§7.2, §7.3). The pure-Kotlin tests in Phases 1–3 do not touch Hilt at all. If Hilt setup costs more than half a day, swap to manual constructor injection for this sprint and file a Hilt-introduction task for the next.

### Risk: KB content is plausible-sounding but wrong
The point of the sprint is *real* curated content, not stubs that look real. If the implementer just paraphrases a generic potting-mix article, we have nothing.
**Mitigation:** Every species and archetype JSON entry MUST carry a `citations` field pointing to a specific section of `docs/Research_brief.md` or `docs/research/deep-research-Gemini.md`. Reviewer rejects entries with empty `citations` arrays.

### Risk: The sprint plan tries to ship 60 tasks in 2 weeks and slips
~50 tasks is at the upper end for two weeks; the test-paired structure makes many tasks small but still real.
**Mitigation:** The de-scoping order, if we slip, is: drop Phase 8 except 8.4; drop §6.7 RecommendationScreenTest's three-species coverage in favour of one; drop the Hilt swap-for-fake in §7.2 in favour of a hardcoded test build flavour. Camera, KB, engine, and the end-to-end test are all non-negotiable.

### Risk: Sandbox filesystem overlay (per CLAUDE auto-memory)
The implementer's environment may have a filesystem overlay that silently swallows writes outside the project tree. This is a known issue for this user.
**Mitigation:** All writes in this sprint are under `D:/DarkFactoryProject/Plant potting/`. The implementer should verify file presence from the user's own terminal (`dir` or `ls`) before declaring any task done.

---

## 5. Acceptance criteria (sprint done-ness)

The sprint is considered done when **every** statement below is observably true. Each is testable; ambiguity is a bug in the criterion.

- [ ] `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck detekt pixel6Api34DebugAndroidTest` runs to completion green on a clean clone in CI.
- [ ] The debug APK installs and launches on a physical Android device with API ≥ 26 and the implementer's screen-recording / screenshots are committed under `docs/sprints/evidence/PLANTPOTTING-0001/`.
- [ ] The §1.1 user flow works end-to-end on that physical device for both the permission-granted and permission-denied paths, with no crash, no ANR, and no placeholder text on the recommendation screen.
- [ ] `assets/kb/archetypes.json` declares exactly 7 archetypes; `assets/kb/species.json` declares exactly 16 species; every species's mapping resolves to a known archetype; every archetype recipe sums to exactly 100; the Hoya blend is present and the engine returns a recipe summing to exactly 100 for it.
- [ ] `KbValidationTest`, `KbContentTest`, `RecommendationGoldenTest`, `StubPlantIdentifierTest`, `CameraViewModelTest`, `RecommendationViewModelTest`, `ResultViewModelTest`, `PermissionScreenTest`, `RecommendationScreenTest`, `EndToEndFlowTest`, and `PermissionDeniedFlowTest` all exist and are green.
- [ ] `PlantIdentifier` is bound in Hilt; no production source file outside `identify/` references `StubPlantIdentifier` by class name — only by interface. (Grep check in CI: `grep -R "StubPlantIdentifier" app/src/main/` must return only `identify/`.)
- [ ] No production runtime dependency on networking libraries (verified via §8.4).
- [ ] `docs/sprints/done/PLANTPOTTING-0001-DONE.md` exists, lists which acceptance lines went green, and explicitly identifies the `PlantIdentifier` seam as the entry point for the next sprint.

---

## 6. Tech stack proposal

Opinionated picks, with a one-line reason each. Drafters should challenge any of these in cross-critique.

- **Language:** Kotlin 1.9+ on JVM target 17. Justification: the Android consensus; every research report names it; coroutines and Compose are first-class.
- **UI:** Jetpack Compose + Material 3, single Activity. Justification: Brief §6.1 explicitly identifies Compose as the consensus; XML layouts are dead weight this late.
- **Min/Target SDK:** `minSdk = 26`, `targetSdk = 34`. Justification: 26 (Android 8) cuts the API-25-and-below long tail (sub-1%-of-active-devices in 2026), unlocks CameraX without legacy hassle, and clears AndroidX baseline-profile requirements; 34 is the current Play target floor.
- **Architecture:** MVVM with unidirectional data flow; one ViewModel per screen; state hoisted into immutable `*UiState` records; navigation events emitted via `SharedFlow`. Justification: this is the Now-In-Android shape; familiar to every AI implementer.
- **DI:** Hilt. Justification: small overhead, first-class Compose support, makes the test-time `PlantIdentifier` swap trivial.
- **Camera:** CameraX (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`). Justification: Google's recommended API; ImageCapture's in-memory callback gives us JPEG bytes without disk I/O.
- **Serialization:** Kotlinx Serialization JSON. Justification: matches Kotlin data-class ergonomics, no annotation-processor cost (it's a compiler plugin).
- **Persistence:** None this sprint. The KB is bundled JSON, loaded once. Room is a Phase-2-product concern; introducing it now is premature.
- **Navigation:** AndroidX Navigation-Compose. Justification: the canonical Compose nav library; integrates with Hilt via the official compose extension.
- **Build:** Gradle Kotlin DSL + version catalog (`libs.versions.toml`). Justification: type-safe build scripts, single source of truth for versions.
- **Static analysis:** ktlint + detekt with default profiles. Justification: cheap to add, expensive to retrofit; default profiles avoid bikeshedding now.
- **Testing:** JUnit4 + Truth + MockK + Turbine on the JVM; Compose UI Test + Espresso + UI Automator + Hilt-testing on-device; Gradle Managed Devices for CI emulator. Justification: the standard modern Android stack; no exotic dependencies.
- **CI:** GitHub Actions, single workflow, JDK 17, Gradle cache, runs the full check chain. Justification: free for OSS or for this account class; zero infra to maintain.
- **No-go this sprint:** no Firebase, no Crashlytics, no analytics SDK, no Retrofit/OkHttp, no Glide/Coil (we don't load remote images), no Room, no DataStore, no WorkManager, no MediaPipe, no LiteRT (the model swap is a future sprint).

---

## 7. Notes to the cross-critique reviewers

Please attack, specifically:

1. Are 7 archetypes and 16 species the right shape? Should Hoya be a blend or a Single mapping to a third archetype? Should `Phalaenopsis` go to species rank for *Phalaenopsis amabilis* given the retail reality?
2. Is the `Recommendation` schema rich enough — should we ship "sourcing notes" (where to buy each ingredient) this sprint, or defer?
3. Is `minSdk = 26` defensible, or should we push to 24 for one more long-tail percentage point?
4. The stub identifier ignores the JPEG. Should it instead hash the JPEG and pick a species deterministically by the hash, to make the demo feel slightly less random? (Cheap to add; could mislead if the user thinks it's working.)
5. Is the §7.5 physical-device requirement realistic for an AI implementer with no physical device? If not, we may need to fall back to a verified Gradle Managed Device + a Firebase Test Lab single-shard run.
