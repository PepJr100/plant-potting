# Sprint Plan: PLANTPOTTING-0001 — Android Bootstrap & E2E Stub Flow

**Sprint ID:** PLANTPOTTING-0001  
**Status:** DRAFT  
**Focus:** Infrastructure, Camera, Stubbed Identification, and Real Substrate Recommendation Engine.

## 1. Goals & Non-Goals

### Goals
- **Runnable Android Stub:** A debug APK that runs on a physical device and executes the full happy-path user journey.
- **Camera Integration:** Reliable CameraX implementation with permission handling and image capture.
- **Real Knowledge Base (KB):** 6–8 substrate archetypes and 10–20 common houseplant species mapped to them with defensible horticultural data.
- **Recommendation Engine:** Logic to fetch and display the correct recipe and rationale based on a plant ID.
- **TDD Foundation:** 100% test coverage for domain logic and high-value UI integration tests for the capture-to-recommendation flow.

### Non-Goals
- **Real ML Model:** On-device inference is a stub returning a random or fixed species from the KB.
- **Persistent Storage:** User history or "My Collection" features.
- **Network/Backend:** No API calls or cloud syncing.
- **Polish:** Final iconography, animations, or complex branding.
- **Cloud/CI Infrastructure:** GitHub Actions or similar (local build/test verification only for this sprint).

## 2. Tech Stack Proposal & Justification
- **Language:** Kotlin (Modern, expressive, first-class Android support).
- **UI Framework:** Jetpack Compose (Declarative UI, faster development, modern standard).
- **Asynchrony:** Kotlin Coroutines & Flow (Structured concurrency).
- **Camera:** CameraX (Lifecycle-aware, simplifies device compatibility).
- **DI:** Hilt (Standard for Android, simplifies boilerplate).
- **Navigation:** Compose Navigation (Type-safe navigation).
- **Testing:** 
    - Unit: JUnit 5 + MockK + Turbine (for Flow).
    - UI/Integration: Compose Test Library + Espresso.
- **Data:** Kotlin Serialization (JSON for the static KB file in assets).

## 3. Task List

### Phase 1: Project Scaffolding
- [ ] Initialize Android Studio project (Empty Compose Activity, API 31+).
- [ ] Configure Hilt dependency injection.
- [ ] Set up project-level and app-level `build.gradle.kts` with required dependencies (CameraX, Hilt, Navigation, Serialization).
- [ ] Implement a basic `MainActivity` with a Compose Navigation Host.
- [ ] **Test:** Create a smoke test verifying the app launches and the Hilt graph initializes.

### Phase 2: Knowledge Base & Recommendation Domain
- [ ] Define `SubstrateArchetype` data class (name, recipe: Map<String, String>, rationale: String).
- [ ] Define `PlantProfile` data class (id, commonName, scientificName, archetypeId).
- [ ] Create `SubstrateRepository` interface and its local implementation.
- [ ] Curate and enter data for 8 substrate archetypes (Standard, Aroid Chunky, Succulent Gritty, Cactus Mineral, Epiphytic Orchid Bark, Semi-Hydro Inert, Moisture-Retentive, Acidic).
- [ ] Curate and map 20 species (e.g., Monstera deliciosa, Ficus lyrata, Epipremnum aureum, Phalaenopsis, etc.).
- [ ] Implement `GetRecommendationUseCase` to retrieve substrate details by plant ID.
- [ ] **Test:** Unit test `SubstrateRepository` to ensure all 20 species return valid archetypes.
- [ ] **Test:** Unit test `GetRecommendationUseCase` for correct recipe assembly.

### Phase 3: Camera & Image Capture
- [ ] Implement `PermissionViewModel` to handle `Manifest.permission.CAMERA` requests.
- [ ] Create `CameraCaptureScreen` using CameraX `PreviewView` in a `AndroidView` composable.
- [ ] Implement "Capture" button that triggers `ImageCapture.takePicture`.
- [ ] Save captured image temporarily to cache and return the URI to the ViewModel.
- [ ] **Test:** Unit test `PermissionViewModel` state transitions (Granted/Denied/Rationale).
- [ ] **Test:** UI test verifying `CameraCaptureScreen` displays when permission is granted.

### Phase 4: Identification Stub & UI Flow
- [ ] Create `IdentificationViewModel` with a `identify(imageUri: Uri)` method.
- [ ] Implement `PlantIdentifier` interface and `StubPlantIdentifier` (returns a random plant from the 20-species KB).
- [ ] Create `LoadingScreen` with a placeholder animation during "identification".
- [ ] Create `RecommendationScreen` to display the plant name, archetype name, recipe table, and rationale.
- [ ] Link Navigation: Home -> Camera -> Loading -> Recommendation.
- [ ] **Test:** Unit test `IdentificationViewModel` to ensure it transitions from Loading to Success state.
- [ ] **Test:** Integration test: End-to-end flow from "Take Photo" to "Recommendation Displayed" with the stub identifier.

### Phase 5: Refinement & Validation
- [ ] Add a "Retake" button to the Recommendation screen to return to the Camera.
- [ ] Implement basic error handling for camera failures or missing permissions.
- [ ] Verify the app runs on a physical Android device.
- [ ] **Test:** Verify all tests pass on a physical device/emulator.

## 4. Substrate Knowledge Base (Initial Set)

### Archetypes (Examples)
1. **Aroid Chunky:** 40% Pine Bark, 25% Coco Coir, 20% Perlite, 10% Sphagnum, 5% Charcoal.
2. **Succulent Gritty:** 40% Pumice, 30% Coarse Sand, 30% Coir.
3. **Epiphytic Orchid:** 80% Orchiata Bark, 10% Charcoal, 10% Perlite.

### Species (Partial List)
- *Monstera deliciosa* -> Aroid Chunky
- *Ficus lyrata* -> Standard Houseplant
- *Phalaenopsis* -> Epiphytic Orchid Bark
- *Sansevieria trifasciata* -> Succulent Gritty
- *Zamioculcas zamiifolia* -> Succulent Gritty

## 5. Sequencing & Dependencies
1. **Scaffold (Phase 1)** unblocks everything.
2. **KB/Domain (Phase 2)** can be done in parallel with **Camera (Phase 3)**.
3. **UI/Logic (Phase 4)** depends on both Phase 2 and Phase 3.
4. **Validation (Phase 5)** is the final integration check.

## 6. Risks & Mitigations
- **Risk:** CameraX compatibility issues on specific hardware.
  - **Mitigation:** Test on at least two different physical devices (e.g., Pixel and Samsung) early.
- **Risk:** Permission denial loops.
  - **Mitigation:** Implement a clear "Permissions Required" screen with a "Go to Settings" link.
- **Risk:** Implementation of "Stub" identification is too simplistic.
  - **Mitigation:** Ensure the `PlantIdentifier` interface is robust so the future ML implementation is a drop-in replacement.

## 7. Acceptance Criteria (Done-ness)
1. [ ] App launches into a "Start" or "Camera" screen.
2. [ ] User can grant camera permission within the app.
3. [ ] Real-time camera preview is visible.
4. [ ] Tapping "Capture" simulates an identification process (loading state).
5. [ ] App displays a recommendation for one of the 20 KB species.
6. [ ] Recommendation UI includes: Plant Name, Archetype, Ingredients List, and Rationale.
7. [ ] All 30+ unit/integration tests pass in the local environment.
8. [ ] Code follows modern Android architecture (MVI or MVVM with Hilt).
