# Sprint PLANTPOTTING-0009: Knowledge Base Expansion Phase 2

## 1. Goals
*   **Additive Knowledge Base Expansion:** Add text/config entries for 16 specific plant species already supported by the `house_plant_species_mobilenetv2` model.
*   **Increase Model Utility:** Increase the mapped, actionable species from 10 to 26 without modifying the model itself.
*   **Archetype Expansion:** Introduce carefully vetted new care/substrate archetypes for specialized plants (carnivorous, succulents, ferns, palms) while reusing existing valid archetypes (e.g., `aroid-chunky`) where appropriate.
*   **Ensure CI Stability:** Maintain a strict network-free, fully isolated environment.

## 2. Non-Goals & Scope Boundaries
*   **NO ML modifications:** No fine-tuning or retraining of the MobileNetV2 model.
*   **NO Imagery/Probing:** No self-shot, first-party imagery, or real-photo probing. Unprobed calibration of newly-mapped classes is a **KNOWN GAP** recorded for a future sprint.
*   **NO modifying existing KB entries:** The 16 existing records in `species.json` are frozen.
*   **NO modifying the Java/Kotlin seam:** The `PlantIdentifier` and `IdentificationResult` classes remain frozen.
*   **DELIBERATELY DEFERRED:** 'Chinese Money Plant (Pilea peperomioides)' is explicitly excluded from this sprint due to a known boundary issue with pothos. This will ship later with a boundary fix. Do not map or add Pilea.

## 3. Phased Task List

### Phase 1: New Care Archetypes
Define new substrate/care archetypes in `app/src/main/assets/kb/archetypes.json` for plants that do not fit existing profiles.
- [ ] Add `carnivorous-nutrient-poor` archetype (peat/sand, distilled water, strictly no fertiliser) for Venus Flytrap.
- [ ] Add `succulent-gritty` archetype (high drainage, infrequent watering) for succulents.
- [ ] Add `fern-moisture-retentive` archetype (consistent moisture, high humidity) for ferns.
- [ ] Add `palm-well-draining` archetype (sandy loam, even moisture) for palms.

### Phase 2: Aroid Additions
Map species that can reuse the existing `aroid-chunky` archetype.
- [ ] Add Chinese evergreen (*Aglaonema*) to `species.json` (archetype: `aroid-chunky`) & map `Chinese evergreen` in `plant_class_map.json`.
- [ ] Add Elephant Ear (*Alocasia spp.*) to `species.json` (archetype: `aroid-chunky`) & map `Elephant Ear` in `plant_class_map.json`.
- [ ] Add Anthurium (*Anthurium andraeanum*) to `species.json` (archetype: `aroid-chunky`) & map `Anthurium` in `plant_class_map.json`.
- [ ] Add Dumb Cane (*Dieffenbachia spp.*) to `species.json` (archetype: `aroid-chunky`), **flag toxicity** in speciesRationale/warnings, & map `Dumb Cane` in `plant_class_map.json`.

### Phase 3: Specialised Care Additions
Map species that require the new archetypes from Phase 1.
- [ ] Add Aloe Vera (*Aloe vera*) to `species.json` (archetype: `succulent-gritty`) & map `Aloe Vera` in `plant_class_map.json`.
- [ ] Add Kalanchoe to `species.json` (archetype: `succulent-gritty`) & map `Kalanchoe` in `plant_class_map.json`.
- [ ] Add Boston Fern (*Nephrolepis exaltata*) to `species.json` (archetype: `fern-moisture-retentive`) & map `Boston Fern` in `plant_class_map.json`.
- [ ] Add Areca Palm (*Dypsis lutescens*) to `species.json` (archetype: `palm-well-draining`) & map `Areca Palm` in `plant_class_map.json`.
- [ ] Add Venus Flytrap (*Dionaea muscipula*) to `species.json` (archetype: `carnivorous-nutrient-poor`), **flag special case constraints**, & map `Venus Flytrap` in `plant_class_map.json`.

### Phase 4: Standard Care Additions
Map remaining species using standard existing archetypes (e.g., standard potting mix, general tropical).
- [ ] Add Prayer Plant (*Maranta leuconeura*) to `species.json` & map `Prayer Plant` in `plant_class_map.json`.
- [ ] Add Money Tree (*Pachira aquatica*) to `species.json` & map `Money Tree` in `plant_class_map.json`.
- [ ] Add Dracaena (generic *Dracaena marginata/fragrans*) to `species.json` & map `Dracaena` in `plant_class_map.json`.
- [ ] Add Tradescantia to `species.json` & map `Tradescantia` in `plant_class_map.json`.
- [ ] Add Schefflera to `species.json` & map `Schefflera` in `plant_class_map.json`.
- [ ] Add English Ivy (*Hedera helix*) to `species.json`, **flag toxicity**, & map `English Ivy` in `plant_class_map.json`.
- [ ] Add Poinsettia (*Euphorbia pulcherrima*) to `species.json`, **flag toxicity**, & map `Poinsettia` in `plant_class_map.json`.

### Phase 5: CI & Validation Gates
Ensure isolation and mapping constraints remain unbroken.
- [ ] Add/update KB loading unit test to assert a minimum mapped coverage count of exactly 26 species (10 old + 16 new).
- [ ] Run `verifyNoNetworking` CI gate locally to ensure 0 network calls are made.
- [ ] Run `scripts/check-stub-isolation.sh` to ensure GREEN status and no isolation leaks.
- [ ] Vet the finalized JSON arrays using a JSON linter to prevent parse errors on load.

## 4. Risks & Mitigations
*   **Risk:** Typo in JSON configuration breaking the KB parser.
    **Mitigation:** The unit test coverage count will fail to hit 26 if a mapped species fails to parse. Use a strict JSON linter.
*   **Risk:** Pilea peperomioides gets mapped accidentally, exposing users to a pothos boundary hallucination.
    **Mitigation:** Explicit instruction provided to exclude Pilea. A code reviewer will verify its absence in `plant_class_map.json`.
*   **Risk:** Toxic plants lacking warning labels could lead to unsafe care advice.
    **Mitigation:** Checkboxes explicitly require flagging toxicity for Dieffenbachia, English Ivy, and Poinsettia. Ensure these fields are reviewed in the PR.

## 5. Acceptance Criteria
*   16 exactly matching species listed in the goals are added to `app/src/main/assets/kb/species.json`.
*   16 class mappings are added to `app/src/main/assets/ml/house_plant_species_mobilenetv2/plant_class_map.json`.
*   Appropriate new archetypes are added to `app/src/main/assets/kb/archetypes.json` for succulents, ferns, palms, and carnivorous plants.
*   No modification made to the initial 16 KB entries.
*   No code changes to `PlantIdentifier` or `IdentificationResult`.
*   `verifyNoNetworking` and `scripts/check-stub-isolation.sh` both execute and stay GREEN.
*   Total mapped count is functionally validated in unit tests to be 26.
