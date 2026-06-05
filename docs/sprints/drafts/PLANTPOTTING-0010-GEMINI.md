# PLANTPOTTING-0010 Sprint Plan

## Goals
- Refresh the app's UI/UX with modern, pickable Compose themes and improved confidence indicators.
- Establish a shared, local-only persistence layer for tracking "My Plants" and user requests.
- Introduce an "Add this plant" wireframe for out-of-vocab but confident model matches.
- Continue expanding the KB for model-recognized plants (text-only).
- Deliver a debug APK with 2-3 candidate themes to choose from, along with screenshots.

## Non-Goals
- Any network calls (must maintain `verifyNoNetworking` GREEN).
- Changing the `PlantIdentifier` or `IdentificationResult` contract.
- Model training or fine-tuning.
- Adding self-shot imagery or non-CC/public-domain reference images.
- Mapping Pilea or fixing the pothos/Pilea boundary (remains deferred).
- Complex cloud sync or remote telemetry.

## Scope Boundaries & Architecture Decisions
- **Shared Persistence Layer:** We will introduce **Android Room** (injected via Hilt) to handle local-only data. This layer will manage two entities: a `MyPlants` table for the user's collection, and a `PlantRequests` table for tallying out-of-vocab "Add this plant" hits.
- **"Add this plant" Interaction:** Currently, strong out-of-vocab (OOV) matches fall back to `LowConfidencePickerScreen`. We will intercept strong OOV matches *before* the picker, showing a modified `ResultScreen` variant (or banner) with the raw model class name and the "Add this plant" button. A "Pick manually" secondary button will route them into the existing `LowConfidencePickerScreen` flow.
- **Reference Image Strategy:** To balance aesthetics and APK size, we will bundle aggressively compressed WebP versions of license-clean (CC/public-domain) images. We will implement a placeholder strategy for any species without suitable clean images to prevent blocked progress.
- **Theme Delivery:** 2-3 candidate Compose themes will be built into `app/src/main/java/com/darkfactory/plantpotting/ui/theme/`. They will be togglable at runtime via a temporary debug menu/switch so the principal can test and select one via the delivered APK.

## Risks & Mitigations
- **APK Size Bloat from Images:**
  - *Mitigation:* Aggressively compress reference photos using WebP. Set a strict MB budget. Fallback to a placeholder for plants without available CC images.
- **Network Leakage:**
  - *Mitigation:* Room requires no network, but we must ensure we don't accidentally import image-loading libraries that try to fetch URLs. All images will be loaded locally via `R.drawable`. `verifyNoNetworking` CI gate must stay active.
- **Pilea Regression:**
  - *Mitigation:* Ensure `HousePlantClassMapValidationTest` retains its explicit Pilea-absence guard while the rest of the 21 unmapped classes are processed.

## Sequencing / Phases
- **Phase 1: Foundation (Persistence & KB Expansion)**
  - Expand KB textually for a slice of the 21 remaining classes.
  - Scaffold the Room database and DAOs.
- **Phase 2: Core UX (Themes & Visual Polish)**
  - Implement the 2-3 candidate Compose themes and debug toggle.
  - Update `ResultScreen` and `RecommendationScreen` to show confidence percentage and progress bar.
  - Contain the species list inside the search control on `LowConfidencePickerScreen`.
- **Phase 3: New Features ("My Plants" & "Add this plant")**
  - Bundle CC-licensed reference images and integrate them into recipe/details.
  - Wire up the "My Plants" folder screen to read from Room.
  - Intercept strong OOV matches, display the "Add this plant" wireframe, and wire to the Room request tally.
- **Phase 4: Delivery**
  - Version bump, capture screenshots, and generate debug APK.

## Task List

### Phase 1: Foundation
- [ ] Expand `app/src/main/assets/kb/species.json` and `plant_class_map.json` for a slice of the 21 unmapped model classes (strictly text-only).
- [ ] Update `HousePlantClassMapValidationTest` to assert the new mapped counts, ensuring the Pilea-absence guard remains intact.
- [ ] Add Room dependencies to `app/build.gradle.kts`.
- [ ] Define the Room database, `MyPlantsDao`, and `PlantRequestsDao`.
- [ ] Wire the Room database into the Hilt DI graph.

### Phase 2: Core UX
- [ ] Build 2-3 Compose themes in `app/src/main/java/com/darkfactory/plantpotting/ui/theme/{Color.kt,Theme.kt}`.
- [ ] Add a temporary debug toggle (e.g., in a Dev settings sheet or long-press on camera) to swap themes at runtime.
- [ ] Update `ResultScreen` and `RecommendationScreen` to display a numeric percentage and progress bar for model confidence.
- [ ] Refactor `LowConfidencePickerScreen` so the species search list is visually contained within the search-species control.

### Phase 3: New Features
- [ ] Source, compress (WebP), and bundle CC-licensed reference images into `res/drawable-nodpi/` or `assets/`, including a generic placeholder.
- [ ] Add attribution for images (e.g., to an `About` or `Licenses` screen if needed, or in code comments as per repository norms).
- [ ] Update recipe/details screens to display the plant's reference photo.
- [ ] Implement the "My Plants" screen to list previously identified plants from the `MyPlants` Room table.
- [ ] Wire the identification success flow to insert a record into `MyPlants`.
- [ ] Modify routing logic for strong-confidence matches with NO KB entry: show a screen/banner with the raw model class and the "Add this plant" wireframe.
- [ ] Wire the "Add this plant" button to increment the count in the `PlantRequests` Room table (and provide a fallback button to proceed to `LowConfidencePickerScreen`).

### Phase 4: Delivery
- [ ] Bump the app version in `app/build.gradle.kts`.
- [ ] Run `scripts/check-stub-isolation.sh` and `./gradlew check` (including `verifyNoNetworking`) to confirm all gates are GREEN.
- [ ] Capture UI screenshots of the 2-3 candidate themes.
- [ ] Build the debug APK and export alongside screenshots.

## Acceptance Criteria
- [ ] 2-3 distinct Compose themes can be previewed/toggled on device.
- [ ] Debug APK and theme screenshots are ready for delivery.
- [ ] Confidence percentage and progress bar are visible on `ResultScreen`/`RecommendationScreen`.
- [ ] "My Plants" screen shows a local history of identified plants backed by Room.
- [ ] Search species list is visually contained within its control on `LowConfidencePickerScreen`.
- [ ] CC-licensed reference photos appear on plant details, with a placeholder fallback.
- [ ] Strong out-of-vocab matches display the "Add this plant" wireframe before/instead of immediate low-confidence fallback.
- [ ] Clicking "Add this plant" increments a local Room database counter (network-free).
- [ ] KB successfully expanded for a new slice of mapped classes (excluding Pilea).
- [ ] No changes to `PlantIdentifier` or `IdentificationResult` interfaces.
- [ ] `verifyNoNetworking` and stub-isolation gates remain GREEN.
