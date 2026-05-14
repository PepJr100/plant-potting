# PLANTPOTTING-0001 — Android scaffold + end-to-end stub flow with curated substrate KB

**Status:** planned
**Sprint window:** ~2 weeks of a single AI implementer (opus, gpt-5.4, or gemini — chosen at sprint-execute time)
**Source artefacts:** merged from `drafts/PLANTPOTTING-0001-{CODEX,GEMINI,CLAUDE}.md` and the three cross-critiques

---

## 1. Intent

Bootstrap the Houseplant Soil & Potting Guidance Android app from zero to a runnable, end-to-end debug flow on an Android emulator (and a real device where one is available): **launch → grant camera permission → CameraX capture → stubbed identification → recommendation screen showing a real, hand-curated substrate recipe with rationale**. The identifier is deliberately a deterministic stub behind an interface seam that a real on-device model will replace in a later sprint. The knowledge base — 8 substrate archetypes, 16 species, recipes, and rationales — is *not* a stub: every entry is sourced from `docs/Research_brief.md` (especially §4.4 and §3) and the deep-research reports under `docs/research/`, with citations in the data.

This sprint exercises Research Brief hypotheses **H1** (a small species set covers the head of the retail market) and **H2** (substrate needs cluster into a small set of archetypes) by encoding them as code. It only builds the architectural seam for **H3** (on-device ML) — model integration is the next sprint's job.

---

## 2. Goals and non-goals

### 2.1 The single observable success bar

On an Android emulator (Gradle Managed Device, API 34) and on a real Android device *if available to the implementer*, a fresh `./gradlew assembleDebug` install can complete this flow without crashes, ANRs, or visible placeholder copy:

1. Launch the app from the launcher.
2. See a one-screen camera-permission rationale with a primary **Grant camera access** button. On grant, navigate to the camera screen.
3. On a CameraX live preview, tap a single shutter button. Within ≤3 s land on a result screen showing:
   - The identified species' scientific name and most common name,
   - A literal **Stub identifier — replace in a later sprint** badge so the demo viewer is not misled,
   - A primary **See potting mix** button.
4. Tap the button, land on a recommendation screen showing:
   - The substrate **archetype name** (e.g., *Aroid Chunky*),
   - A **recipe table** of ingredients with integer percentages that sum to exactly 100,
   - A **rationale** containing the species' scientific name and explaining why this archetype suits it,
   - A **Retake** button that returns to the camera.
5. The denied-permission path also works: a polite explanation plus an **Open system settings** button that deep-links to `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`. No crash on second-denial / "don't ask again".

### 2.2 Falsifiability — how we know we hit it

- `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck pixel6Api34DebugAndroidTest` runs green on a clean clone in CI.
- The Gradle Managed Device job runs `EndToEndFlowTest` and `PermissionDeniedFlowTest` to completion on every PR.
- `scripts/integration-flow.ps1` produces an artifact manifest that diffs cleanly against the checked-in expected manifest; no acceptance/integration checkbox is marked done without that diff.
- If the implementer has a physical device available, a screen recording or four screenshots from the §2.1 flow are committed to `docs/sprints/evidence/PLANTPOTTING-0001/` (this is *encouraged but not required* — the GMD run is the binding evidence).
- `docs/sprints/results/PLANTPOTTING-0001.md` is filled in with build commands, test commands, integration artifact diff, real-device model (if used), and known gaps.

### 2.3 Non-goals (explicit deferrals)

- No real ML model, no LiteRT/TFLite integration, no training, no fine-tuning. The identifier is a deterministic stub behind an interface.
- No backend, no cloud calls of any kind. Fully offline this sprint.
- No release-signing, Play Store listing, Data Safety form, content rating, ASO. Debug build only.
- No Room database, no DataStore persistence — the KB is bundled JSON loaded once at startup.
- No analytics, crash reporting, Firebase, or any external SDK that phones home.
- No camera live-guidance overlay, multi-shot capture, manual-refine UX, or confidence visualisation. Single-shot, single-result.
- No accessibility audit beyond Compose-default semantics + content descriptions on interactive elements.
- No internationalisation beyond English strings extracted to `strings.xml`.
- No dark-mode tuning, no tablet/foldable adaptive layouts.
- No `detekt` this sprint (ktlint only); `detekt` lands in a later quality sprint.
- No `PULL_REQUEST_TEMPLATE.md` / `CODEOWNERS`; light governance is deferred.

### 2.4 What is *real* this sprint (not stubbed)

The **knowledge base**: 8 archetype recipes with citations, 16 species mappings with citations and species-specific rationales, alias resolution for `Sansevieria` ↔ `Dracaena` and `Calathea` ↔ `Goeppertia`, plus one blend species (`Hoya carnosa`) that exercises the merged-recipe codepath. Every JSON entry carries a `citations` array pointing to a specific section of `docs/Research_brief.md` or a file under `docs/research/`. Reviewer rejects entries with empty citations.

---

## 3. Tech stack (locked)

- **Language:** Kotlin 1.9+, JVM target 17.
- **UI:** Jetpack Compose + Material 3, single Activity, no XML layouts.
- **SDK:** `minSdk = 26`, `targetSdk = 34`, `compileSdk = 34`.
- **Architecture:** MVVM with unidirectional data flow; one ViewModel per screen; state hoisted into immutable `*UiState` records; navigation events via `SharedFlow`.
- **DI:** Hilt (with a documented fallback to manual constructor injection if Hilt instrumented tests cost more than half a day to set up).
- **Camera:** CameraX (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`).
- **Serialisation:** Kotlinx Serialization JSON.
- **Persistence:** None this sprint (KB is bundled JSON read once).
- **Navigation:** AndroidX Navigation-Compose.
- **Build:** Gradle Kotlin DSL + version catalog (`gradle/libs.versions.toml`).
- **Static analysis:** ktlint (default profile). No detekt this sprint.
- **Testing — JVM:** JUnit4 + Truth + MockK + Turbine + Robolectric (for AssetManager only).
- **Testing — on-device:** Compose UI Test + Espresso + UI Automator + Hilt-testing on a Gradle Managed Device (`pixel6Api34`, AOSP system image).
- **CI:** GitHub Actions, single workflow, JDK 17, Gradle cache, runs `assembleDebug testDebugUnitTest lint ktlintCheck pixel6Api34DebugAndroidTest`.
- **No-go this sprint:** Firebase, Crashlytics, Retrofit/OkHttp, Glide/Coil, Room, DataStore, WorkManager, MediaPipe, LiteRT.

---

## 4. The knowledge base (locked content)

### 4.1 The 8 archetypes

| `id` | Display name | Recipe (sums to 100) | Source |
|---|---|---|---|
| `standard-houseplant` | Standard Houseplant | coir 60, perlite 30, bark fines 10 | Brief §4.4 |
| `aroid-chunky` | Aroid Chunky | pine/orchid bark 40, coco coir 25, perlite/pumice 20, sphagnum 10, charcoal 5 | Brief §4.4 |
| `succulent-gritty` | Succulent Gritty | pumice/akadama 40, coarse sand or perlite 30, coir or pine fines 30 | Brief §4.4 |
| `cactus-pure-mineral` | Cactus Pure Mineral | pumice 50, akadama/lava 30, coarse sand 20 | Brief §4.4 |
| `epiphytic-orchid-bark` | Epiphytic Orchid Bark | medium fir/orchiata bark 70, charcoal 15, perlite 15 | Brief §4.4 |
| `moisture-retentive` | Moisture-Retentive | coir 50, fine/composted bark 20, perlite 20, long-fibre sphagnum 10 | Brief §4.4 |
| `semi-hydro-inert` | Semi-Hydro Inert | LECA 100 (with hydroponic fertiliser noted in rationale) | Brief §4.4 |
| `acidic-ericaceous` | Acidic / Ericaceous | acidic coir or peat-free base 45, pine bark fines 35, perlite 15, acidic amendment 5 | Brief §4.4 (added per GEMINI critique for *Saintpaulia*) |

Note: the cactus archetype is declared so the schema is exercised; no v1 species references it (cactus is rarely the focus in the top-200 foliage retail set per Gemini's deep-research report). Future sprints can add species.

### 4.2 The 16 species

1. `Monstera deliciosa` → `aroid-chunky`
2. `Monstera adansonii` → `aroid-chunky`
3. `Epipremnum aureum` (Pothos / Devil's Ivy) → `aroid-chunky`
4. `Philodendron hederaceum` (Heartleaf Philodendron) → `aroid-chunky`
5. `Philodendron erubescens 'Pink Princess'` → `aroid-chunky` *(variegated; rationale must mention slower transpiration; cite Brief H5 + Gemini report)*
6. `Spathiphyllum wallisii` (Peace Lily) → `moisture-retentive`
7. `Ficus lyrata` (Fiddle-Leaf Fig) → `standard-houseplant`
8. `Ficus elastica` (Rubber Plant) → `standard-houseplant`
9. `Dracaena trifasciata` (Snake Plant; **alias** `Sansevieria trifasciata`) → `succulent-gritty`
10. `Zamioculcas zamiifolia` (ZZ Plant) → `succulent-gritty`
11. `Chlorophytum comosum` (Spider Plant) → `standard-houseplant`
12. `Phalaenopsis` *(genus-level — retail Phalaenopsis is essentially never identified to species)* → `epiphytic-orchid-bark`
13. `Goeppertia orbifolia` (**alias** `Calathea orbifolia`) → `moisture-retentive`
14. `Crassula ovata` (Jade Plant) → `succulent-gritty`
15. `Saintpaulia ionantha` (African Violet) → `acidic-ericaceous`
16. `Hoya carnosa` → **blend** `{primary: aroid-chunky 60%, secondary: succulent-gritty 40%}` *(exercises the merged-recipe codepath)*

Every entry needs `commonNames`, `speciesRationale` (1–3 sentences naming the scientific binomial), and `citations: List<String>` pointing to specific brief/report sections.

---

## 5. Task list

Tasks paired with test tasks (TDD ordering: failing test first, then implementation). Test tasks for content-as-code (KB) MUST land *before* the corresponding data entry.

### Phase 0 — Repository, toolchain, CI

- [x] **0.1** Add `.gitignore` covering `*.iml`, `.gradle/`, `build/`, `local.properties`, `.idea/`, `*.keystore`, `captures/`, `.cxx/`, `app/release/`, `artifacts/`.
- [x] **0.2** Scaffold an Android single-module app: `app/` with `applicationId = "com.darkfactory.plantpotting"`, Compose enabled, `minSdk = 26`, `targetSdk = 34`, `compileSdk = 34`, Kotlin JVM target 17. Use Gradle Kotlin DSL.
- [x] **0.3** Add `gradle/libs.versions.toml` with pinned entries for: Android Gradle Plugin, Kotlin, Compose BOM, CameraX, Hilt, Kotlinx Serialization, Kotlinx Coroutines, AndroidX Activity-Compose, Navigation-Compose, Material3, Lifecycle-ViewModel-Compose, JUnit4, Truth, MockK, Turbine, Robolectric, Compose UI Test, Espresso, AndroidX Test Runner/Rules, UI Automator, Hilt-testing.
- [x] **0.4** Configure Gradle wrapper (Gradle 8.x compatible with AGP). Verify `./gradlew --version` from a clean shell.
- [x] **0.5** Add ktlint via `org.jlleitschuh.gradle.ktlint` with the default profile. Wire `check` to depend on `ktlintCheck`. (No detekt this sprint.)
- [x] **0.6** Commit `AndroidManifest.xml` with `<uses-permission android:name="android.permission.CAMERA"/>` and `<uses-feature android:name="android.hardware.camera.any" android:required="false"/>` so emulators without a camera can still install.
- [x] **0.7** Add `.github/workflows/ci.yml` on `ubuntu-latest` with JDK 17, Gradle cache, running `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck pixel6Api34DebugAndroidTest`. Workflow fails on any task failure.
- [x] **0.8** Add `scripts/check-android.ps1` (PowerShell) wrapping the same command chain for local Windows runs. Mirrors CI; fails fast.
- [x] **0.9** Add a top-level `README.md` (≤40 lines): prerequisites (JDK 17, Android SDK 34), how to run `assembleDebug`, how to run unit + instrumentation tests, how to install on a device. No marketing copy.
- [x] **0.10** Configure `org.gradle.parallel=true`, `org.gradle.caching=true` in `gradle.properties`. Document Gradle JDK version (17) in `README.md`.
- [x] **0.11 (test)** Add a trivial smoke unit test in `app/src/test/` proving the JVM test runner is wired and `testDebugUnitTest` discovers tests.
- [ ] **0.12 (verify)** Run `./gradlew testDebugUnitTest assembleDebug ktlintCheck` locally and confirm green. Record the commands and result in sprint notes.

### Phase 1 — Research extraction and KB sourcing notes

- [x] **1.1** Read `docs/Idea.md`, `docs/Research_brief.md` (especially §1.3, §3, §4.4), and the three reports under `docs/research/`. Write `docs/kb/plant-substrate-kb-notes.md` with: one source-backed paragraph per archetype (citing brief §/report section), one source-backed paragraph per species mapping, and an explicit note on alias decisions (`Sansevieria`/`Dracaena`, `Calathea`/`Goeppertia`, genus-level `Phalaenopsis`). This file is the editorial source of truth that JSON citations point back to.

### Phase 2 — Knowledge base: schema, validation tests, data

- [x] **2.1** Define Kotlin data classes in `app/src/main/java/com/darkfactory/plantpotting/kb/model/`:
    - `data class Archetype(val id: String, val displayName: String, val shortDescription: String, val recipe: List<RecipeIngredient>, val rationaleTemplate: String, val citations: List<String>)`
    - `data class RecipeIngredient(val ingredient: String, val proportionPct: Int, val notes: String? = null)`
    - `data class Species(val id: String, val scientificName: String, val commonNames: List<String>, val aliases: List<String>, val mapping: ArchetypeMapping, val speciesRationale: String, val citations: List<String>)`
    - `sealed class ArchetypeMapping { data class Single(val archetypeId: String) : ArchetypeMapping(); data class Blend(val primaryArchetypeId: String, val secondaryArchetypeId: String, val primaryPct: Int) : ArchetypeMapping() }`

    All `@Serializable` (Kotlinx Serialization).
- [x] **2.2 (test — RED before data)** Add `KbValidationTest` *first*: for each invalid scenario (unknown archetype id, recipe summing to 99, missing `{species}` placeholder in `rationaleTemplate`, duplicate id, blend with `primaryPct = 0` or `100`, duplicate normalised alias, empty `citations`, rationale containing `TODO` / `stub` / `lorem`), feed a hand-written invalid JSON string and assert `KbValidationException` is thrown naming the offending id(s). All cases must fail on a placeholder validator first.
- [x] **2.3** Implement `KbLoader` in `kb/KbLoader.kt`: suspendable, parses `archetypes.json` and `species.json` from `AssetManager`, validates with `validate(...)`, caches an immutable `KnowledgeBase` (record of `archetypes: Map<String, Archetype>` and `species: List<Species>` and `speciesIndex: Map<String, Species>` keyed by normalised alias). Validator implements every rule from §2.2.
- [x] **2.4 (test)** Add `KbLoaderTest` (Robolectric for `AssetManager`): the bundled JSON loads, validates, and exposes exactly 8 archetypes and 16 species.
- [x] **2.5** Author `app/src/main/assets/kb/archetypes.json` with the 8 archetypes from §4.1, each with recipe summing to 100, `rationaleTemplate` containing `{species}`, and at least one citation. *Do not enter species data until tests in §2.2 are green and §2.6 is green.*
- [x] **2.6 (test)** Add `KbContentTest` for archetypes: every recipe sums to 100, every `rationaleTemplate` contains `{species}`, no duplicate ids.
- [x] **2.7** Author `app/src/main/assets/kb/species.json` with the 16 species from §4.2, each with `commonNames`, `aliases`, `mapping`, `speciesRationale` (1–3 sentences naming the scientific binomial), and `citations`. `Sansevieria trifasciata` is an alias on `Dracaena trifasciata`; `Calathea orbifolia` is an alias on `Goeppertia orbifolia`; `Hoya carnosa` uses `ArchetypeMapping.Blend(aroid-chunky, succulent-gritty, 60)`.
- [x] **2.8 (test)** Add `KbContentTest` for species: every `mapping.archetypeId` (or blend ids) resolves to a known archetype; no duplicate species ids; no duplicate normalised aliases across the corpus; every `speciesRationale` is non-empty; every species has ≥1 citation; alias lookup for `Sansevieria trifasciata` resolves to canonical id of `Dracaena trifasciata`; alias lookup for `Calathea orbifolia` resolves to canonical id of `Goeppertia orbifolia`.

### Phase 3 — Recommendation engine

- [ ] **3.1** Define `recommend/RecommendationEngine.kt`: `interface RecommendationEngine { fun recommend(speciesId: String): Recommendation }` with `data class Recommendation(val archetypeName: String, val recipe: List<RecipeIngredient>, val rationale: String, val isBlend: Boolean)`.
- [ ] **3.2 (test — RED before impl)** Add `KbRecommendationEngineTest` with hand-built `KnowledgeBase` fixtures:
    - `Single`-mapped species returns the exact archetype recipe and a rationale containing the scientific name.
    - `Blend`-mapped species returns a recipe whose proportions sum to exactly 100 (integer rounding) and `isBlend = true`.
    - Unknown `speciesId` raises `IllegalArgumentException("unknown species: <id>")`.
- [ ] **3.3** Implement `KbRecommendationEngine(kb: KnowledgeBase) : RecommendationEngine`:
    - For `Single`: return archetype recipe verbatim. Rationale = `archetype.rationaleTemplate` with `{species}` → `species.scientificName`, prefixed by `species.speciesRationale`.
    - For `Blend`: scale each contributor's percentages by primary/secondary ratio, sum per ingredient (matching by ingredient name), then re-normalise to integer percentages summing to 100 (allocate remainder to the largest-proportion ingredient deterministically). `archetypeName` = `"${primary.displayName} / ${secondary.displayName} blend"`. Rationale includes both archetypes and the species' rationale.
- [ ] **3.4 (test)** Add `RecommendationGoldenTest`: for every species in the *real bundled KB*, call `engine.recommend(speciesId)` and assert `recipe` sums to exactly 100, rationale contains the species' scientific name, and rationale contains none of `TODO`, `stub`, `lorem`, or `placeholder`. This catches editorial drift on every push.
- [ ] **3.5** Wire engine into Hilt as `@Singleton` in `RecommendModule`.

### Phase 4 — Identification: the stub behind the seam

- [ ] **4.1** Define `identify/PlantIdentifier.kt`: `interface PlantIdentifier { suspend fun identify(jpeg: ByteArray): IdentificationResult }` with `data class IdentificationResult(val speciesId: String, val displayName: String, val source: IdSource)` and `enum class IdSource { STUB_DETERMINISTIC, STUB_RANDOM, ON_DEVICE_MODEL, CLOUD }`. **This is the single point of replacement for the future ML model — keep the surface this small.**
- [ ] **4.2 (test — RED first)** Add `StubPlantIdentifierTest`: deterministic mode with fixed seed produces stable species pick across runs; returned `speciesId` is always one of the KB species ids; `IdSource` is `STUB_DETERMINISTIC` by default; random mode (debug-only) only ever returns species present in the KB.
- [ ] **4.3** Implement `StubPlantIdentifier(kb: KnowledgeBase, random: Random = Random(0L), mode: Mode = Mode.DETERMINISTIC) : PlantIdentifier`. Does **not** read JPEG bytes. Deterministic mode returns `Monstera deliciosa` (or first KB species). Random mode samples uniformly from KB species. KDoc on the class: single line, "Placeholder for PLANTPOTTING-000X to replace with real model — do not depend on this class outside the Hilt binding."
- [ ] **4.4** Wire `PlantIdentifier` into Hilt as `@Singleton` bound to `StubPlantIdentifier` (deterministic mode for production wiring; instrumentation tests may swap via `@HiltAndroidTest`).
- [ ] **4.5 (test)** Add a CI grep check: `grep -R "StubPlantIdentifier" app/src/main/` must return only paths under `identify/` (the binding module). No consumer outside `identify/` may reference the stub class by name.

### Phase 5 — App shell, navigation, permissions

- [ ] **5.1** Implement `PlantPottingApplication : Application` annotated `@HiltAndroidApp` and `MainActivity : ComponentActivity` hosting `setContent { PlantPottingTheme { PlantPottingNavHost() } }`.
- [ ] **5.2** Define a Material 3 theme in `ui/theme/` with light + dark schemes (dynamic colour OFF this sprint to reduce variability). Typography stays at Material defaults.
- [ ] **5.3** Implement `PlantPottingNavHost` using `androidx.navigation:navigation-compose`. Routes: `permission`, `camera`, `result/{speciesId}`, `recommendation/{speciesId}`. Default `startDestination = "permission"` until permission is granted, then `camera`. `{speciesId}` is a `StringType` arg, URL-encoded.
- [ ] **5.4** Implement `PermissionScreen`: one-line rationale ("We use the camera to photograph your plant. We never upload your photos."), primary **Grant camera access** button triggering `ActivityResultContracts.RequestPermission`, expandable secondary **Why we ask** disclosure. On grant: navigate to `camera`. On denial-with-don't-ask-again: swap primary button to **Open system settings** with `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` intent.
- [ ] **5.5** Extract `permission/CameraPermissionGuard.kt` that re-reads current permission state on every screen launch (never trust stale Compose state across process death).
- [ ] **5.6 (test)** Add `PermissionScreenTest` (Compose UI test with a fake permission contract): rationale visible; **Grant camera access** present; stubbed "granted" result navigates to `camera`; second-denial state shows **Open system settings**.
- [ ] **5.7** All user-visible strings extracted to `res/values/strings.xml`. No hard-coded strings in composables (enforced by ktlint check on this sprint's PRs; CI grep optional).

### Phase 6 — Camera capture

- [ ] **6.1** Add CameraX dependencies. Add `CameraXModule` providing `ProcessCameraProvider` as a Hilt singleton.
- [ ] **6.2** Implement `CameraPreview` composable wrapping `PreviewView` via `AndroidView`, binding `Preview` + `ImageCapture` use cases to `LocalLifecycleOwner`. `AspectRatio.RATIO_4_3`, target rotation tracked from display rotation, back camera default.
- [ ] **6.3** Implement `CameraScreen(viewModel: CameraViewModel)`: full-screen preview, 72 dp circular shutter at bottom centre, loading overlay during in-flight capture+identify. Shutter tap → `ImageCapture.takePicture(executor, OnImageCapturedCallback)` → convert `ImageProxy` to JPEG `ByteArray` on a background dispatcher → forward to ViewModel. Captured JPEG is written to app cache as a temp file with a deterministic naming scheme.
- [ ] **6.4** Implement `CameraViewModel` (Hilt-injected `PlantIdentifier` + a `Navigator` event flow). State machine: `Idle → Capturing → Identifying → Success(speciesId) | Failure(reason)`. On `Success`, emit navigate-to-`result/{speciesId}` event. On `Failure`, surface a Snackbar via state.
- [ ] **6.5** Add a cache-cleanup hook on `MainActivity.onDestroy` (and a `WorkManager`-free best-effort sweep on app startup) that deletes capture temp files older than 10 minutes. No persistent photo storage this sprint.
- [ ] **6.6 (test)** Add `CameraViewModelTest` (JVM, coroutines-test, Turbine): with a fake `PlantIdentifier`, state transitions are `Idle → Capturing → Identifying → Success`; navigation event carries expected `speciesId`; failure transition fires on identifier throw.
- [ ] **6.7 (test)** Add `CameraScreenSmokeTest` (Compose UI test with a fake `ImageCapture` provider): shutter button visible and enabled; tapping disables it until ViewModel returns to `Idle`. Do not assert real preview rendering — covered by the GMD integration test in §8.
- [ ] **6.8** Handle config change / rotation: confirm CameraX use-case rebinding does not crash on rotation. If it does on first attempt, lock orientation to portrait for this sprint and file a follow-up.

### Phase 7 — Result + recommendation screens

- [ ] **7.1** Implement `ResultScreen(speciesId: String, viewModel: ResultViewModel)`: column showing species' `scientificName` (italic), most common name in parentheses, the literal **Stub identifier — replace in a later sprint** badge, primary **See potting mix** button navigating to `recommendation/{speciesId}`.
- [ ] **7.2** Implement `ResultViewModel`: loads `Species` by id from the KB; exposes `data class ResultUiState(val scientificName: String, val commonName: String, val notFound: Boolean)`.
- [ ] **7.3 (test)** Add `ResultViewModelTest`: with a fake KB, populated state for a known id; `notFound = true` for unknown id.
- [ ] **7.4** Implement `RecommendationScreen(speciesId: String, viewModel: RecommendationViewModel)`: archetype name as title, rationale paragraph below, recipe table with one row per ingredient (`ingredient` left, `proportion%` right-aligned), "Blend recipe" chip near title when `isBlend = true`, primary **Retake** button at bottom returning to `camera`.
- [ ] **7.5** Implement `RecommendationViewModel(engine: RecommendationEngine)`: on creation runs `engine.recommend(speciesId)` and exposes a `RecommendationUiState`.
- [ ] **7.6 (test)** Add `RecommendationViewModelTest`: with a fake engine, state is populated from the engine's `Recommendation`. With a fake engine that throws, state becomes `RecommendationUiState.NotFound`.
- [ ] **7.7 (test)** Add `RecommendationScreenTest` (Compose UI test with `@HiltAndroidTest`): for three representative species (one Single mapping, the Hoya Blend, one `moisture-retentive` mapping), render the screen and assert: archetype name visible, rationale contains the scientific name, recipe rows include correct ingredient names and integer proportions, Retake button visible and emits the camera navigation event when clicked.

### Phase 8 — End-to-end integration and acceptance evidence

- [ ] **8.1** Configure Gradle Managed Device `pixel6Api34` (AOSP system image) in `app/build.gradle.kts`. Wire `pixel6Api34DebugAndroidTest` into the CI workflow from §0.7.
- [ ] **8.2 (integration test)** Add `EndToEndFlowTest` in `androidTest`: launches `MainActivity` with a Hilt test rule that binds `PlantIdentifier` to a `FakeFixedIdentifier` returning a deterministic species id. The test:
    1. Asserts the permission screen is shown.
    2. Grants camera permission via `GrantPermissionRule` (or `UiAutomator` against the system dialog).
    3. Asserts the camera screen is shown (preview view present, shutter button enabled).
    4. Taps the shutter (or invokes a test hook bypassing real camera hardware).
    5. Asserts navigation to the result screen with the fake species' scientific name visible.
    6. Taps **See potting mix**.
    7. Asserts the recommendation screen shows expected archetype name, rationale containing scientific name, ≥3 recipe rows whose displayed proportions sum to 100.
    8. Taps **Retake** and asserts return to the camera screen.
- [ ] **8.3 (integration test)** Add `PermissionDeniedFlowTest`: simulates camera-permission denial (including the "don't ask again" second-denial state) and asserts the **Open system settings** button is shown and emits the expected intent (verified via `Intents.intended(...)`).
- [ ] **8.4** Add `scripts/integration-flow.ps1` (PowerShell): `assembleDebug`, install to a connected device/emulator, `adb pm grant <pkg> android.permission.CAMERA` for the happy path, launch app, drive the fake-camera test hook, capture screenshots and a UI-hierarchy dump under `artifacts/PLANTPOTTING-0001/`, then diff that artifact manifest against `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`. Script fails on diff.
- [ ] **8.5** Commit the expected manifest at `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`. Manifest enumerates the screenshot filenames and key UI-hierarchy substrings (e.g., `archetype-name=Aroid Chunky`, `recipe-row-count=5`).
- [ ] **8.6** Confirm `./gradlew :app:dependencies --configuration releaseRuntimeClasspath` does not surface networking transitive dependencies (no OkHttp, no Retrofit, no Firebase). Add a Gradle task `verifyNoNetworking` that greps the dependency report and fails on any networking library; wire into `check`.
- [ ] **8.7 (optional, encouraged)** On a physical device (if the implementer has one), install the debug APK, run the §2.1 flow once with permission granted and once with permission denied, commit a screen recording or four screenshots to `docs/sprints/evidence/PLANTPOTTING-0001/`. This is *not* a hard acceptance gate — the GMD run is binding evidence — but it is the highest-signal manual check.
- [ ] **8.8** Author `docs/sprints/results/PLANTPOTTING-0001.md` from a checked-in template. Required fields: build commands run + result, unit-test output summary, integration-script diff result, GMD test result, real-device model+Android version (or "no physical device available"), known limitations, link to the `PlantIdentifier` seam as the entry point for PLANTPOTTING-000X.

---

## 6. Sequencing and dependency rules

```
Phase 0 (toolchain + CI)
   │
   ▼
Phase 1 (research notes)       ← editorial; can overlap with code work
   │
   ▼
Phase 2 (KB schema, validation tests RED, then data)
   │
   ├──► Phase 3 (engine; needs KB)
   │
   └──► Phase 4 (stub identifier; needs KB only for species id pool)
                    │
                    ▼
Phase 5 (nav + permission UI)  ← can run in parallel with 2/3/4
                    │
                    ▼
Phase 6 (camera)               ← depends on Phase 4 binding
                    │
                    ▼
Phase 7 (result + recommendation)
                    │
                    ▼
Phase 8 (E2E + acceptance evidence)
```

Hard gates:

- **§2.2 (KbValidationTest) must be RED then GREEN before §2.3 (KbLoader impl).** TDD for content-as-code.
- **§3.2 (KbRecommendationEngineTest) must be RED before §3.3 (engine impl).**
- **§4.2 (StubPlantIdentifierTest) must be RED before §4.3 (impl).**
- **§3.4 (RecommendationGoldenTest) must be GREEN before §5/§7 work touches recommendation rendering.** Otherwise we render data that can't be defended.
- **§6 (camera) must depend on the `PlantIdentifier` *interface*, never `StubPlantIdentifier` directly.** Enforced by the §4.5 grep check.
- **Phase 8 cannot start until every prior phase's tests are green.**

Soft parallels:

- Phase 5 (nav + permission) can land in parallel with Phases 2–4 since it has no KB or identifier dependency.
- Phase 1 (research notes) can be drafted while Phase 0 toolchain work is in flight.

De-scope order if the implementer slips (preserving the core end-to-end flow):

1. Drop §8.6 `verifyNoNetworking` Gradle task (keep the manual `:app:dependencies` check).
2. Drop §7.7's three-species coverage in `RecommendationScreenTest` in favour of two (one Single + the Blend).
3. Drop the Hilt swap in §8.2 in favour of a debug build flavour that hard-codes the fake identifier.
4. Drop the `LoadingScreen` separation if any temptation to add it appears (loading stays as an overlay).

Camera, KB, engine, the Hoya blend codepath, the §2.1 end-to-end flow, the GMD test, and the integration script are **non-negotiable**.

---

## 7. Risks and mitigations

### 7.1 KB editorial correctness drifts into plausible-sounding garbage
Risk: an unsourced recipe edits an ingredient by 1% and ships wrong advice silently.
Mitigation: §1.1 sourcing notes file is the editorial source of truth; §2.2 validator rejects entries without citations or with placeholder text (`TODO`, `stub`, `lorem`, `placeholder`); §3.4 `RecommendationGoldenTest` runs on every push and the same placeholder grep; reviewer rejects PRs whose `citations` arrays are empty.

### 7.2 JSON schema drifts silently between commits
Risk: a recipe is edited to sum to 99 and tests still pass.
Mitigation: three independent fences — §2.3 strict validation at load time, §2.6/§2.8 `KbContentTest` in CI on every push, §3.4 `RecommendationGoldenTest` re-validates from the engine side.

### 7.3 Camera permission "don't ask again" traps the user
Risk: on Android 11+, a second denial silently enables "don't ask again". A naive **Grant** button does nothing.
Mitigation: §5.4 codepath swaps the button to **Open system settings** on permanent denial; §8.3 `PermissionDeniedFlowTest` covers it.

### 7.4 CameraX use-case rebinding crashes on rotation / config change
Risk: known OEM-specific pothole. Emulator hides most cases.
Mitigation: §6.8 manual rotation check; if it crashes on first attempt, lock orientation to portrait and file a follow-up rather than burning sprint time. §8.7's physical-device check catches the second-tier of OEM-specific bugs *when a device is available*.

### 7.5 No physical device available to the AI implementer
Risk: §2.1 requires real-device validation; an opus/gpt-5.4/gemini implementer running headless has none.
Mitigation: the **binding evidence is the GMD test in §8.1/§8.2/§8.3 + the §8.4 integration script diff**, not the physical device. §8.7 is encouraged but optional. The implementer must surface "no physical device" explicitly in the §8.8 results doc; that is not a sprint failure.

### 7.6 Over-abstraction of the `PlantIdentifier` seam
Risk: `IdentifierFactory<Strategy<Result>>`. The seam in §4.1 is intentionally tiny: one `suspend fun`, one result data class with three fields, one source enum.
Mitigation: any additional method/field on `PlantIdentifier` requires a written justification on the PR. §4.5 grep enforces interface-only consumption.

### 7.7 Hilt instrumentation-test fragility
Risk: Hilt + Compose + UI test rule + GMD is the configuration most likely to burn half a day on cryptic errors.
Mitigation: pure-Kotlin tests in Phases 2–4 do not touch Hilt. Hilt rules only appear in `@HiltAndroidTest`-marked tests (§7.7, §8.2, §8.3). If Hilt setup costs more than half a day, swap to manual constructor injection for this sprint and file a Hilt-introduction task. Document the swap in §8.8.

### 7.8 Sandbox filesystem overlay on Windows (per implementer CLAUDE.md memory)
Risk: shell-tool writes outside the project tree may not reach disk.
Mitigation: every artifact path in this sprint is under `D:/DarkFactoryProject/Plant potting/`. The implementer verifies file presence from the user's terminal (`dir` or `ls`) before declaring any acceptance task done.

### 7.9 Greenfield Android toolchain friction eats week one
Risk: Gradle versions, SDK licences, emulator images, ktlint config can each cost half a day.
Mitigation: Phase 0 is front-loaded so toolchain pain is visible early. If `pixel6Api34` GMD setup fights for more than half a day, the implementer falls back to a manual emulator run for §8.2/§8.3 and files a follow-up to add GMD properly. CI gate then becomes `assembleDebug testDebugUnitTest lint ktlintCheck` only, and §8.2/§8.3 become manual local runs documented in §8.8.

### 7.10 Sprint task count is at the upper end for one implementer
~50 tasks is real work for 2 weeks even with TDD discipline.
Mitigation: §6 de-scope order is pre-decided. Core flow (KB + engine + camera + result + recommendation + Retake + permission + GMD test + integration script) is non-negotiable. Everything else can go.

---

## 8. Acceptance criteria

The sprint is done when **every** statement below is observably true. Each is testable; ambiguity is a bug in the criterion.

- [ ] `./gradlew assembleDebug testDebugUnitTest lint ktlintCheck pixel6Api34DebugAndroidTest verifyNoNetworking` runs green on a clean clone in CI.
- [ ] The debug APK installs and launches on the `pixel6Api34` Gradle Managed Device (and, if available, on a physical device — see §7.5).
- [ ] The §2.1 user flow works end-to-end on the GMD, for both the permission-granted and permission-denied paths, with no crash, no ANR, and no placeholder text on the recommendation screen.
- [ ] `assets/kb/archetypes.json` declares exactly **8** archetypes; `assets/kb/species.json` declares exactly **16** species; every species's mapping (or blend ids) resolves to a known archetype; every archetype recipe sums to exactly 100; the `Hoya carnosa` blend is present and the engine returns a recipe summing to exactly 100 for it.
- [ ] Alias lookups for `Sansevieria trifasciata` and `Calathea orbifolia` resolve to the canonical species ids of `Dracaena trifasciata` and `Goeppertia orbifolia` respectively, asserted by tests.
- [ ] `KbValidationTest`, `KbContentTest` (archetypes + species), `KbLoaderTest`, `KbRecommendationEngineTest`, `RecommendationGoldenTest`, `StubPlantIdentifierTest`, `CameraViewModelTest`, `CameraScreenSmokeTest`, `RecommendationViewModelTest`, `ResultViewModelTest`, `PermissionScreenTest`, `RecommendationScreenTest`, `EndToEndFlowTest`, and `PermissionDeniedFlowTest` all exist and are green.
- [ ] `PlantIdentifier` is bound in Hilt; the CI grep check at §4.5 confirms no production source file outside `identify/` references `StubPlantIdentifier` by class name.
- [ ] No production runtime dependency on networking libraries (verified via `verifyNoNetworking` Gradle task in §8.6).
- [ ] `scripts/integration-flow.ps1` produces an artifact manifest that diffs cleanly against `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`. **No acceptance or integration checkbox in this list is marked done unless this diff is clean.**
- [ ] `docs/kb/plant-substrate-kb-notes.md` exists and contains one source-backed paragraph per archetype and per species, with citation pointers to specific sections of `docs/Research_brief.md` or files under `docs/research/`.
- [ ] `docs/sprints/results/PLANTPOTTING-0001.md` is filled in with: build commands + result, test commands + result, integration artifact diff result, GMD test result, real-device model+Android version (or explicit "no physical device available"), known limitations, and an explicit handoff note pointing to the `PlantIdentifier` seam as the entry point for PLANTPOTTING-000X.
- [ ] Every completed feature task has a paired completed test task.

---

## 9. Handoff note for the sprint-execute implementer

You are one of `opus`, `gpt-5.4`, or `gemini`. You will be picked by the user via the `sprint-execute` skill.

- **Read this whole file before starting.** Then read `docs/Idea.md`, `docs/Research_brief.md` §1.3/§3/§4.4, and the three reports under `docs/research/` (skim — full read is overkill; you mostly need them as citation targets).
- **Tick `- [ ]` boxes as `- [x]` as you complete each task.** Do not batch checkmarks; commit them as you go.
- **TDD is mandatory.** For every feature task, write the test first, watch it fail, then implement. KB tests in §2.2 must be RED before any data lands.
- **A feature task is not done until its paired integration check is in the integration script.** §8.4 must show a diff for §6/§7/§8 to count.
- **Sandbox warning (per the user's auto-memory):** shell-tool writes outside `D:/DarkFactoryProject/Plant potting/` may not reach disk on this machine. Verify file presence from the user's terminal before declaring acceptance work done.
- **If you fall behind, de-scope from the §6 list, never from the core flow.** Camera + KB + engine + result + recommendation + Retake + permission + GMD test + integration script are non-negotiable.
