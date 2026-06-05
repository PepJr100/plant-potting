# PLANTPOTTING-0009 - KB expansion for MobileNetV2 covered delta

PLANTPOTTING-0009 is a text-only, additive knowledge-base expansion sprint for the
production `house_plant_species_mobilenetv2` model. The model already has 47 output
classes, but only 10 currently resolve to KB species. This sprint adds KB coverage for
16 more model-covered houseplant classes so the map moves from 10 mapped classes to
about 26 mapped classes without changing ML behavior.

All work is local JSON/config/documentation. The app remains fully network-free.

## Goals

- [ ] Add 16 new species entries to `app/src/main/assets/kb/species.json`, preserving
      all existing 16 entries byte-for-byte unless formatting is unavoidably changed by
      the existing JSON tooling.
- [ ] Add 16 exact model-label rows to
      `app/src/main/assets/ml/house_plant_species_mobilenetv2/plant_class_map.json`.
- [ ] Reuse existing vetted archetypes where botanically valid, especially
      `aroid-chunky`, `succulent-gritty`, `moisture-retentive`, and
      `standard-houseplant`.
- [ ] Add a new Venus Flytrap archetype for nutrient-poor carnivorous-plant substrate
      and care constraints, because no current archetype represents that cultivation
      lane.
- [ ] Flag toxicity and planning-vet notes in species rationale/citations for
      Dieffenbachia, English Ivy, Poinsettia, and any other species where a common
      retail-care warning materially affects the care card.
- [ ] Record the known gap that the 16 newly mapped classes are not first-party probed
      or calibrated in this sprint.
- [ ] Keep existing `verifyNoNetworking` and `scripts/check-stub-isolation.sh` gates
      green.

## Non-Goals / Scope Boundaries

- [ ] Do not train, fine-tune, evaluate, quantize, or swap any ML model.
- [ ] Do not change `PlantIdentifier`, `IdentificationResult`, score mapping,
      thresholds, calibration, model manifests, or the frozen identifier seam.
- [ ] Do not add real-photo probes, self-shot imagery, fixture photos, source-image
      downloads, or first-party image collection.
- [ ] Do not close calibration for the 16 new mappings; record it as a known gap only.
- [ ] Do not add `Chinese Money Plant`, `Pilea peperomioides`, or any Pilea KB mapping
      in this sprint.
- [ ] Do not alter the pothos-to-Pilea boundary behavior; that ships later with the
      dedicated boundary fix so a wrong-but-confident Pilea card cannot surface.
- [ ] Do not add production networking, telemetry, remote KB fetches, remote model
      downloads, or source-specific SDKs.
- [ ] Do not modify existing KB species entries other than append-only placement around
      the new entries.
- [ ] Do not broaden the care-card schema unless validation proves the current schema
      cannot represent a required warning.

## Proposed IDs And Archetype Assignments

- [ ] Add `aloe-vera` for exact model label `Aloe Vera`, scientific name `Aloe vera`,
      mapped to `succulent-gritty`.
- [ ] Add `aglaonema` for exact model label `Chinese evergreen`, scientific name
      `Aglaonema`, mapped to `aroid-chunky` unless planning vet determines
      `standard-houseplant` is more honest for common terrestrial retail forms.
- [ ] Add `alocasia` for exact model label `Elephant Ear`, scientific name
      `Alocasia spp.`, mapped to `aroid-chunky`.
- [ ] Add `anthurium-andraeanum` for exact model label `Anthurium`, scientific name
      `Anthurium andraeanum`, mapped to `aroid-chunky`.
- [ ] Add `dieffenbachia` for exact model label `Dumb Cane`, scientific name
      `Dieffenbachia spp.`, mapped to `aroid-chunky` and flagged toxic/irritant.
- [ ] Add `maranta-leuconeura` for exact model label `Prayer Plant`, scientific name
      `Maranta leuconeura`, mapped to `moisture-retentive`.
- [ ] Add `nephrolepis-exaltata` for exact model label `Boston Fern`, scientific name
      `Nephrolepis exaltata`, mapped to `moisture-retentive` unless a fern-specific
      archetype is justified during vet.
- [ ] Add `pachira-aquatica` for exact model label `Money Tree`, scientific name
      `Pachira aquatica`, mapped to `standard-houseplant`.
- [ ] Add `dypsis-lutescens` for exact model label `Areca Palm`, scientific name
      `Dypsis lutescens`, mapped to `standard-houseplant` unless a palm-specific
      archetype is justified during vet.
- [ ] Add `dracaena` for exact model label `Dracaena`, scientific name
      `Dracaena marginata / Dracaena fragrans`, mapped to `standard-houseplant` with a
      coarse genus/species-group note.
- [ ] Add `tradescantia` for exact model label `Tradescantia`, scientific name
      `Tradescantia`, mapped to `standard-houseplant`.
- [ ] Add `hedera-helix` for exact model label `English Ivy`, scientific name
      `Hedera helix`, mapped to `standard-houseplant` and flagged toxic.
- [ ] Add `schefflera` for exact model label `Schefflera`, scientific name
      `Schefflera / Heptapleurum`, mapped to `standard-houseplant` with taxonomy/trade
      naming noted.
- [ ] Add `kalanchoe` for exact model label `Kalanchoe`, scientific name `Kalanchoe`,
      mapped to `succulent-gritty`.
- [ ] Add `euphorbia-pulcherrima` for exact model label `Poinsettia`, scientific name
      `Euphorbia pulcherrima`, mapped to `standard-houseplant` and flagged for
      toxicity/latex-irritant planning vet.
- [ ] Add `dionaea-muscipula` for exact model label `Venus Flytrap`, scientific name
      `Dionaea muscipula`, mapped to a new carnivorous archetype.

## Phase 1 - Preflight And Guardrails

- [ ] Read `species.json`, `archetypes.json`, and `plant_class_map.json` before editing
      and capture current counts: 16 KB species and 10 mapped MobileNetV2 classes.
- [ ] Confirm the exact 47-class label vocabulary from
      `app/src/main/assets/ml/house_plant_species_mobilenetv2/labels.csv` and verify all
      16 sprint labels appear exactly as requested.
- [ ] Confirm that `Chinese Money Plant` / `Pilea peperomioides` is present or absent
      only as an unmapped model label and remains unmapped after the sprint.
- [ ] Identify existing JSON validation tests or asset-loading tests that already fail
      on broken species IDs, broken archetype IDs, and broken class-map targets.
- [ ] Decide whether coverage-count validation belongs in an existing test class or a
      focused new test, using the repo's current test layout.

## Phase 2 - Archetype Work

- [ ] Validate that `aroid-chunky` is acceptable for Aglaonema, Alocasia, Anthurium,
      and Dieffenbachia, documenting any coarse-fit caveat in `speciesRationale`.
- [ ] Validate that `succulent-gritty` is acceptable for Aloe Vera and Kalanchoe.
- [ ] Validate that `moisture-retentive` is acceptable for Prayer Plant and Boston Fern.
- [ ] Validate that `standard-houseplant` is acceptable for Money Tree, Areca Palm,
      generic Dracaena, Tradescantia, English Ivy, Schefflera, and Poinsettia.
- [ ] Add a new `carnivorous-bog-nutrient-poor` archetype to `archetypes.json` for
      Venus Flytrap with a nutrient-poor acidic peat/sand or equivalent bog-style
      substrate, no standard fertiliser, and distilled/rain/RO water guidance.
- [ ] Give the new carnivorous archetype a concise `displayName`, `shortDescription`,
      recipe proportions, rationale template, and citations consistent with existing
      archetype schema.
- [ ] Ensure the new archetype does not imply ordinary potting mix, compost-rich media,
      tap-water tolerance, or routine fertiliser for Venus Flytrap.

## Phase 3 - Species Entries

- [ ] Append `aloe-vera` with common names, aliases if needed, `mapping.kind`, archetype
      assignment, species rationale, and citations.
- [ ] Append `aglaonema` with common names, aliases if needed, `mapping.kind`,
      archetype assignment, species rationale, and citations.
- [ ] Append `alocasia` with common names including Elephant Ear, aliases if needed,
      `mapping.kind`, archetype assignment, species rationale, and citations.
- [ ] Append `anthurium-andraeanum` with common names, aliases if needed,
      `mapping.kind`, archetype assignment, species rationale, and citations.
- [ ] Append `dieffenbachia` with Dumb Cane common name, aliases if needed,
      `mapping.kind`, archetype assignment, species rationale, citations, and
      toxic/irritant planning-vet warning.
- [ ] Append `maranta-leuconeura` with Prayer Plant common name, aliases if needed,
      `mapping.kind`, archetype assignment, species rationale, and citations.
- [ ] Append `nephrolepis-exaltata` with Boston Fern common name, aliases if needed,
      `mapping.kind`, archetype assignment, species rationale, and citations.
- [ ] Append `pachira-aquatica` with Money Tree common name, aliases if needed,
      `mapping.kind`, archetype assignment, species rationale, and citations.
- [ ] Append `dypsis-lutescens` with Areca Palm common name, aliases if needed,
      `mapping.kind`, archetype assignment, species rationale, and citations.
- [ ] Append `dracaena` with common names for common Dracaena marginata/fragrans retail
      plants, aliases if needed, `mapping.kind`, archetype assignment, species
      rationale, citations, and coarse-label note.
- [ ] Append `tradescantia` with common names, aliases if needed, `mapping.kind`,
      archetype assignment, species rationale, and citations.
- [ ] Append `hedera-helix` with English Ivy common name, aliases if needed,
      `mapping.kind`, archetype assignment, species rationale, citations, and toxicity
      planning-vet warning.
- [ ] Append `schefflera` with common names, aliases covering current/common trade
      naming where appropriate, `mapping.kind`, archetype assignment, species
      rationale, citations, and taxonomy note.
- [ ] Append `kalanchoe` with common names, aliases if needed, `mapping.kind`,
      archetype assignment, species rationale, and citations.
- [ ] Append `euphorbia-pulcherrima` with Poinsettia common name, aliases if needed,
      `mapping.kind`, archetype assignment, species rationale, citations, and
      latex/toxicity planning-vet warning.
- [ ] Append `dionaea-muscipula` with Venus Flytrap common name, aliases if needed,
      `mapping.kind`, new carnivorous archetype assignment, species rationale,
      citations, and special-case care warning.

## Phase 4 - Model Class Map

- [ ] Add class-map row `Aloe Vera` -> `aloe-vera`.
- [ ] Add class-map row `Chinese evergreen` -> `aglaonema` with `alias:true` and a
      note that the common-name model label maps to a genus-level Aglaonema KB entry.
- [ ] Add class-map row `Elephant Ear` -> `alocasia` with `alias:true` and a note that
      elephant ear is a coarse retail name for the Alocasia spp. KB entry.
- [ ] Add class-map row `Anthurium` -> `anthurium-andraeanum` with `alias:true` and a
      note that the genus/common-name label maps to the common florist anthurium
      representative.
- [ ] Add class-map row `Dumb Cane` -> `dieffenbachia` with `alias:true` and a note
      that the common-name model label maps to Dieffenbachia spp.
- [ ] Add class-map row `Prayer Plant` -> `maranta-leuconeura` with `alias:true` and a
      note that the common-name model label maps to Maranta leuconeura.
- [ ] Add class-map row `Boston Fern` -> `nephrolepis-exaltata` with `alias:true` and
      a note that the common-name model label maps to Nephrolepis exaltata.
- [ ] Add class-map row `Money Tree` -> `pachira-aquatica` with `alias:true` and a
      note distinguishing Pachira aquatica from jade plant money-plant naming.
- [ ] Add class-map row `Areca Palm` -> `dypsis-lutescens` with `alias:true` and a
      note that the common-name model label maps to Dypsis lutescens.
- [ ] Add class-map row `Dracaena` -> `dracaena` with `alias:true` and a note that
      this is a coarse genus label for the common marginata/fragrans care envelope,
      while snake plant remains mapped separately to `dracaena-trifasciata`.
- [ ] Add class-map row `Tradescantia` -> `tradescantia` with `alias:true` and a note
      that the genus-level model label maps to a genus-level KB care entry.
- [ ] Add class-map row `English Ivy` -> `hedera-helix` with `alias:true` and a note
      that the common-name model label maps to Hedera helix.
- [ ] Add class-map row `Schefflera` -> `schefflera` with `alias:true` and a note that
      the genus/trade model label maps to the Schefflera/Heptapleurum retail care entry.
- [ ] Add class-map row `Kalanchoe` -> `kalanchoe` with `alias:true` and a note that
      the genus-level model label maps to a genus-level succulent KB care entry.
- [ ] Add class-map row `Poinsettia` -> `euphorbia-pulcherrima` with `alias:true` and
      a note that the common-name model label maps to Euphorbia pulcherrima.
- [ ] Add class-map row `Venus Flytrap` -> `dionaea-muscipula` with `alias:true` and a
      note that special carnivorous-plant care applies.
- [ ] Leave any `"Chinese Money Plant"` or Pilea label without a `kbSpeciesId` row.
- [ ] Update `docs/kb/ml-mapping-notes.md` or the local mapping comment if the repo's
      existing mapping-note convention requires every new class-map row to have an
      editorial explanation.

## Phase 5 - Validation And Tests

- [ ] Add or update a validation test asserting that every `kbSpeciesId` in
      `plant_class_map.json` exists in `species.json`.
- [ ] Add or update a validation test asserting that every species mapping archetype ID
      exists in `archetypes.json`, including the new carnivorous archetype.
- [ ] Add or update a coverage-count assertion that the MobileNetV2 map has 26 mapped
      model classes after this sprint.
- [ ] Add or update a validation assertion that the 16 sprint labels are mapped to the
      expected 16 KB IDs.
- [ ] Add or update a validation assertion that `Chinese Money Plant` and
      `Pilea peperomioides` are not mapped in this sprint.
- [ ] Add or update a validation assertion that the existing 10 mapped MobileNetV2 rows
      still point to the same KB IDs as before.
- [ ] Run JSON parsing/format validation for `species.json`, `archetypes.json`, and
      `plant_class_map.json`.
- [ ] Run the focused unit tests covering KB asset validation and model-label mapping.
- [ ] Run the app's standard test/build gate used by the repo for asset-only changes.
- [ ] Run `verifyNoNetworking`.
- [ ] Run `scripts/check-stub-isolation.sh`.

## Phase 6 - Review Notes And Evidence

- [ ] Add a short evidence note under
      `docs/sprints/evidence/PLANTPOTTING-0009/` recording final species count,
      final mapped-class count, new archetype count, and commands run.
- [ ] Record that the 16 new mappings are editorial/model-vocabulary coverage only and
      were not calibrated with real-photo probes.
- [ ] Record toxicity/special-care vet items for Dieffenbachia, English Ivy,
      Poinsettia, and Venus Flytrap so planning can decide whether the UI needs stronger
      care-card presentation in a later sprint.
- [ ] Record that Pilea remains deliberately deferred to the later pothos/Pilea boundary
      sprint.

## Risks And Mitigations

- [ ] Risk: coarse genus labels such as Dracaena, Tradescantia, Schefflera, Kalanchoe,
      Aglaonema, and Alocasia may hide species-level care variation; mitigate by using
      genus-level KB IDs, explicit rationale wording, and `alias:true` notes that call
      out coarse routing.
- [ ] Risk: Venus Flytrap care is unlike ordinary houseplants and an ordinary substrate
      card would be harmful; mitigate with a dedicated nutrient-poor carnivorous
      archetype and explicit no-standard-fertiliser/distilled-water guidance.
- [ ] Risk: Pilea is a known pothos confusion class; mitigate by keeping
      `Chinese Money Plant` / `Pilea peperomioides` unmapped until the boundary fix
      ships.
- [ ] Risk: newly mapped classes may produce wrong-but-confident cards because they are
      not probed; mitigate by recording the calibration gap and avoiding threshold or
      score-policy changes in this sprint.
- [ ] Risk: toxicity text can drift into medical/legal overclaiming; mitigate by using
      concise planning-vet warnings and deferring presentation policy to a later UX/content
      sprint if needed.
- [ ] Risk: additive JSON edits can accidentally break existing KB IDs or mappings;
      mitigate with before/after count checks and validation tests for IDs, archetypes,
      and unchanged existing mappings.
- [ ] Risk: schema pressure from toxicity/special-care notes could trigger unnecessary
      model or UI changes; mitigate by keeping warnings inside existing rationale/note
      fields unless validation proves the current schema cannot carry them.

## Acceptance Criteria

- [ ] `species.json` contains the original 16 species plus exactly the 16 new sprint
      species, with no Pilea/Chinese Money Plant species entry added.
- [ ] `archetypes.json` contains exactly one required new archetype for Venus Flytrap
      unless planning vet explicitly approves another new archetype with evidence.
- [ ] Every new species entry has `id`, `scientificName`, `commonNames`, `aliases`,
      `mapping`, `speciesRationale`, and `citations` matching the existing schema.
- [ ] Every new species mapping references an existing or newly added valid
      `archetypeId`.
- [ ] `plant_class_map.json` contains the existing 10 MobileNetV2 mappings plus the 16
      new sprint mappings, for a total of 26 mapped model classes.
- [ ] The 16 new class-map keys match the model labels exactly:
      `Aloe Vera`, `Chinese evergreen`, `Elephant Ear`, `Anthurium`, `Dumb Cane`,
      `Prayer Plant`, `Boston Fern`, `Money Tree`, `Areca Palm`, `Dracaena`,
      `Tradescantia`, `English Ivy`, `Schefflera`, `Kalanchoe`, `Poinsettia`, and
      `Venus Flytrap`.
- [ ] `Chinese Money Plant` / `Pilea peperomioides` remains unmapped and is documented as
      deliberately deferred.
- [ ] No production networking or identifier-seam changes are present in the diff.
- [ ] No ML model, manifest, threshold, calibration, fixture-photo, or training-data
      files are added or modified.
- [ ] KB/mapping validation tests pass.
- [ ] `verifyNoNetworking` passes.
- [ ] `scripts/check-stub-isolation.sh` passes.
- [ ] Sprint evidence records the unprobed calibration gap for all 16 newly mapped
      classes.
