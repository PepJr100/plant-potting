# Sprint PLANTPOTTING-0008: Training-Data Availability Spike

## Goals
- **Determine Feasibility**: Assess if there is enough free, license-clean image data available to teach the existing production model (`house_plant_species_mobilenetv2`) the 6 currently out-of-vocabulary (OOV) species.
- **Address Boundary Problem**: Assess the availability of hard examples for the pothos (`epipremnum-aureum`) vs. Pilea boundary problem to correct confident misclassifications.
- **Provide a Go/No-Go Recommendation**: Deliver a written report recommending whether a future fine-tuning sprint is viable based on data availability.
- **Fix Camera UI Bug**: Resolve the UX issue where the camera shutter button remains disabled after navigating back from the results screen.

## Non-Goals / Scope Boundaries
- **NO ML Training**: This is a feasibility spike only. No fine-tuning or transfer learning will be performed.
- **NO Model Swap**: No new model bundles, no changes to the `ACTIVE_MODEL_ROOT` default.
- **NO Knowledge Base (KB) Edits**: `species.json` and `archetypes.json` remain locked.
- **NO Bulk Image Downloads**: Do not commit sample images to the repo or ship them in the APK. Any manual checks stay off-repo.
- **NO Version Bumps**: No updates to AGP, Kotlin, Compose, Hilt, or TFLite.
- **NO Networking on Device**: The app's `verifyNoNetworking` gate and `scripts/check-stub-isolation.sh` must stay GREEN. Any research scripts must run locally and never on-device.
- **NO Changes to Core ML Seams**: The `PlantIdentifier` / `IdentificationResult` interfaces remain frozen.

## Tasks

### Phase 1: Data Availability Assessment (Spike)
- [ ] Create data availability matrix report draft at `docs/sprints/evidence/PLANTPOTTING-0008/data-availability-report.md`.
- [ ] Query iNaturalist (research-grade, CC-licensed) for image counts of `monstera-adansonii` and record count/license types.
- [ ] Query iNaturalist for image counts of `philodendron-hederaceum` and record count/license types.
- [ ] Query iNaturalist for image counts of `philodendron-pink-princess` (noting cultivar availability vs generic) and record count/license types.
- [ ] Query iNaturalist for image counts of `ficus-lyrata` and record count/license types.
- [ ] Query iNaturalist for image counts of `chlorophytum-comosum` and record count/license types.
- [ ] Query iNaturalist for image counts of `hoya-carnosa` and record count/license types.
- [ ] Query iNaturalist for hard examples of `epipremnum-aureum` (pothos) and `Pilea` to address the boundary misclassification.
- [ ] Repeat the above queries (all 6 OOV species + pothos/Pilea) across GBIF, Wikimedia Commons, and Flickr-CC, recording counts and license types.
- [ ] Analyze taxonomic/synonym pitfalls for each queried species (e.g., Pothos vs Scindapsus/Philodendron lookalikes) and add findings to the report.
- [ ] Verify attribution feasibility for each source: can provenance blocks (like `app/src/androidTest/assets/identify-fixtures/LICENSE.txt`) be generated for the imagery?
- [ ] Verify that a disjoint train/val/test split is achievable without using the 8 existing fixture images in `androidTest/assets/identify-fixtures/`.

### Phase 2: Spike Deliverables & Recommendation
- [ ] Evaluate total counts against target thresholds: ~150-300 viable, ~50-100 floor per species.
- [ ] Write the overall GO/NO-GO recommendation in `docs/sprints/results/PLANTPOTTING-0008.md`.
- [ ] **If GO:** Draft a scoped outline for the future fine-tuning sprint in the results doc. Include:
  - [ ] Sourcing/download plan.
  - [ ] Disjoint-split strategy.
  - [ ] Transfer-learning approach using the existing MobileNetV2.
  - [ ] float16/INT8 export strategy.
  - [ ] Plan to reuse `app/src/androidTest/.../identify/ModelSwapEvaluationTest.kt`, `ACTIVE_MODEL_ROOT` switch, and the fixture/provenance pattern.
- [ ] **If NO-GO:** Document the paid-dataset or self-shot-photo fallback options and estimated costs in the results doc.

### Phase 3: Camera UI Bug Fix
- [ ] Locate the shutter button state in the camera/capture Compose screen and its associated ViewModel.
- [ ] Identify why the state remains disabled/greyed out after navigating back from the results screen.
- [ ] Implement the fix to re-enable the shutter button upon return to the camera screen.
- [ ] Write/update a UI test to verify the shutter button is enabled when returning from the results screen.

## Sequencing & Dependencies
1. **Phase 1 (Data Assessment)** must be completed first as it relies on external APIs and UI searches to gather the necessary data.
2. **Phase 2 (Recommendation)** depends directly on the output of Phase 1.
3. **Phase 3 (UI Bug Fix)** is independent and can be picked up at any time, but is a strict requirement for completing the sprint.

## Risks & Mitigations
- **Risk:** Cultivar-specific imagery (e.g., `philodendron-pink-princess`) may be sparse or mislabelled under generic names.
  - **Mitigation:** Fall back to visual manual sampling (off-repo) to verify the quality of the subset if generic queries return low confidence results.
- **Risk:** Lookalikes in the pothos/Pilea boundary might share the same CC search tags, polluting the dataset.
  - **Mitigation:** Explicitly search for known negative pairings or specific leaf structures; document the necessary filtering steps if a GO is recommended.
- **Risk:** Automated API scripts might violate rate limits.
  - **Mitigation:** Use manual UI searches or low-volume, throttled API requests (kept strictly off-device and off-build pipeline).

## Acceptance Criteria
- [ ] `docs/sprints/results/PLANTPOTTING-0008.md` is complete with a definitive GO/NO-GO recommendation.
- [ ] The report contains concrete counts, license types, and attribution feasibility for the 6 OOV species + pothos/Pilea across multiple sources.
- [ ] Taxonomic pitfalls are documented.
- [ ] Depending on the decision, either a clear fine-tuning plan (GO) or a fallback cost analysis (NO-GO) is present.
- [ ] The UI bug is fixed: navigating back to the camera screen re-enables the shutter button.
- [ ] A test exists to prevent regression of the shutter button state.
- [ ] App networking gates (`verifyNoNetworking`) and stub isolation checks are GREEN.
- [ ] No image files or ML models were added to the repository.
