# PLANTPOTTING-0009 CODEX critique of GEMINI and CLAUDE drafts

Grounding checked against the repo:

- `app/src/main/assets/kb/archetypes.json` currently has exactly 8 archetypes: `standard-houseplant`, `aroid-chunky`, `succulent-gritty`, `cactus-pure-mineral`, `epiphytic-orchid-bark`, `moisture-retentive`, `semi-hydro-inert`, and `acidic-ericaceous`.
- `species.json` already demonstrates `mapping.kind = "blend"` via `hoya-carnosa`, so the schema is not limited to `single`.
- `labels.csv` contains parenthesized exact labels for most of the 16 target classes, e.g. `Chinese evergreen (Aglaonema)`, `Dumb Cane (Dieffenbachia spp.)`, and `Poinsettia (Euphorbia pulcherrima)`.
- Existing tests include hard count assertions in `KbContentSpeciesTest.bundlesExactlySixteenSpecies`, `KbContentArchetypesTest.bundlesExactlyEightArchetypes`, and `KbLoaderTest` (`species` size 16, `archetypes` size 8), plus AIY coverage behavior in `ModelLabelMappingValidationTest.mappingCoversEveryBundledKbSpecies`.

## GEMINI draft

### Stronger than my draft

- The draft is short and execution-oriented. The grouping into `Phase 2: Aroid Additions`, `Phase 3: Specialised Care Additions`, and `Phase 4: Standard Care Additions` is easier to scan than my one-checkbox-per-file-task layout.
- It makes the Pilea exclusion visible in `NO Imagery/Probing` / `DELIBERATELY DEFERRED`, and repeats the no-ML/no-seam boundaries clearly enough that an implementer is unlikely to wander into model work.
- It calls out toxicity in the individual species tasks for `Dumb Cane`, `English Ivy`, and `Poinsettia`, which is useful. My draft includes the same species, but Gemini's inline emphasis makes those three stand out.
- `Phase 5: CI & Validation Gates` explicitly names JSON linting. My draft has JSON parse validation, but Gemini's wording is more direct for avoiding trailing-comma failures.

### Weaker than my draft

- `Phase 1: New Care Archetypes` is materially wrong. It asks to add `succulent-gritty`, but that archetype already exists. It also invents `fern-moisture-retentive` and `palm-well-draining`, even though the existing `moisture-retentive` and `standard-houseplant` archetypes already cover Boston Fern and Areca Palm well enough for this sprint. Only Venus Flytrap plausibly needs a new archetype.
- The acceptance criterion "Appropriate new archetypes are added ... for succulents, ferns, palms, and carnivorous plants" would produce an incorrect 12-archetype KB instead of the intended 9-archetype KB.
- Several mapping tasks use non-exact model-label names. `Add Chinese evergreen ... map Chinese evergreen`, `Add Elephant Ear ... map Elephant Ear`, `Add Dumb Cane ... map Dumb Cane`, `Add Prayer Plant ... map Prayer Plant`, `Add Boston Fern ... map Boston Fern`, `Add Money Tree ... map Money Tree`, `Add Areca Palm ... map Areca Palm`, `Add English Ivy ... map English Ivy`, and `Add Poinsettia ... map Poinsettia` omit the parenthesized labels that exist in `labels.csv`. Runtime lookup is exact-keyed, so these would silently fail unless corrected.
- It does not distinguish common-name labels, exact species labels, and coarse/genus labels. My draft's class-map rows mark coarse rows with `alias:true` and notes; Gemini's species tasks do not preserve that mapping honesty.
- It does not mention that the species schema supports `mapping.kind = "blend"`. Even if these 16 mostly fit `single`, a plan should not imply that every new species must be single-archetype if vetting finds a Hoya-style tension.

### Missing tasks

- No preflight task to read `archetypes.json`, `species.json`, `plant_class_map.json`, and `labels.csv` before editing. That omission is what lets the invented archetypes and non-exact labels slip in.
- No task equivalent to my `Confirm the exact 47-class label vocabulary` or `16 new class-map keys match the model labels exactly`.
- No task to update the house-plant map `_comment` or `docs/kb/ml-mapping-notes.md` with the new coarse mappings, Pilea deferral, and unprobed-calibration gap.
- No task to guard that the existing 10 house-plant mappings remain unchanged.
- No task to add a referential-integrity test that every `kbSpeciesId` in `house_plant_species_mobilenetv2/plant_class_map.json` exists in `species.json`, every mapping key exists in `labels.csv`, and the 16 target labels resolve to the expected KB IDs.
- No task to update hard count tests beyond a vague "coverage count" test. The repo has `KbContentSpeciesTest.bundlesExactlySixteenSpecies`, `KbContentArchetypesTest.bundlesExactlyEightArchetypes`, and `KbLoaderTest` size assertions that will fail.
- No task for `ModelLabelMappingValidationTest.mappingCoversEveryBundledKbSpecies`, which currently makes the AIY map a likely red test once 16 KB species are added.
- No evidence-note task under `docs/sprints/evidence/PLANTPOTTING-0009/`.

### Risks underweighted

- Exact-label typo risk is missing, and this is the most immediate runtime risk in Gemini because many task names use shortened labels.
- Archetype sprawl risk is actively introduced rather than mitigated. Adding fern/palm/succulent archetypes would dilute the vetted archetype set and increase review surface for no clear gain.
- AIY coverage-test breakage is not mentioned.
- Hard count assertion failures are not mentioned.
- Alias and common-name collision risk is not mentioned, especially around `Money Tree` vs existing `crassula-ovata` common name `Money plant`, and `Dracaena` vs existing `dracaena-trifasciata`.
- Venus Flytrap special-care risk is present but too shallow: `carnivorous-nutrient-poor` says peat/sand and distilled water, but does not explicitly guard against ordinary fertilizer, lime/dolomite, tap-water tolerance, or compost-rich media in acceptance.

### Sequencing problems

- `Phase 1: New Care Archetypes` comes before repo preflight and therefore asks for archetypes that already exist or are not needed.
- Species and class-map edits are bundled into each species task. That is simple, but worse for validation: archetype first, species entries second, class-map rows third, tests fourth is cleaner because each step depends on the previous asset being valid.
- Vetting is not a gate before merge. Toxicity and Venus Flytrap care are mentioned in tasks, but there is no blocking `Toxicity & special-case vet` phase.

## CLAUDE draft

### Stronger than my draft

- It is much more grounded in the repo than either my draft or Gemini's. `Current state (grounded)` correctly names 16 species, 8 archetypes, 10 mapped house-plant rows, and the exact 8 existing archetype IDs.
- `Reference: the 16 species -> KB id -> archetype -> exact model label` is the strongest part of the draft. It correctly uses exact labels such as `Chinese evergreen (Aglaonema)`, `Elephant Ear (Alocasia spp.)`, `Dumb Cane (Dieffenbachia spp.)`, and `Poinsettia (Euphorbia pulcherrima)`, whereas my draft used shortened labels in several row names.
- It correctly refuses new fern and palm archetypes. The archetype reuse rationale explicitly says Boston Fern remains `moisture-retentive` and Areca Palm remains `standard-houseplant`.
- `Phase 3 - AIY map coverage-invariant maintenance` is a real issue my draft missed. `ModelLabelMappingValidationTest.mappingCoversEveryBundledKbSpecies` means adding 16 KB species can fail the AIY mapping invariant unless the test or AIY dormant rows are handled deliberately.
- `Phase 4 - Tests` is more concrete than mine on exact test behavior: mapping keys must exist in `labels.csv`, `modelLabelsAsset` must point to the house-plant labels asset, Pilea must be absent, and the house-plant map should not import the AIY alias-counterpart rule.
- The risk table is stronger than my prose risks. `R1` and `R2` correctly identify likely red tests; `R3` captures exact-label failure; `R7` captures alias/id collision.
- `Phase 5 - Toxicity & special-case vet` is better than my draft because it expands beyond the three headline toxic plants and asks for consistency across Alocasia, Anthurium, Aglaonema, Schefflera, Kalanchoe, and Tradescantia.

### Weaker than my draft

- It overstates that each new species entry "MUST follow" `mapping{kind:"single", archetypeId}`. The repo schema supports `mapping.kind = "blend"` (`hoya-carnosa` uses `primaryArchetypeId`, `secondaryArchetypeId`, and `primaryPct`). The sprint can choose `single` for most or all 16, but the plan should not make `single` a schema requirement.
- The chosen `Dracaena` KB ID and species treatment are too narrow. `dracaena-marginata` / `Dracaena marginata` may be a workable representative, but the model label is just `Dracaena` and existing `dracaena-trifasciata` is a separate mapped species. My draft's `dracaena` genus/species-group entry with a coarse-label note is more honest unless planning explicitly wants marginata as the representative.
- It makes `aiy_plants_v1/plant_class_map.json` edits a required asset change. That may be correct given the current AIY coverage invariant, but it is a broader surface than my draft's house-plant-focused scope and should be framed as either "add dormant rows or revise the invariant" rather than assumed automatically.
- The acceptance criterion says the house-plant map covers `>= 26` classes. For this sprint, "exactly 26 mapped rows for the production model unless another sprint-approved mapping is added" is a tighter guard against accidental extras, especially Pilea.
- It includes unsourced context like "user owns one" for Poinsettia and Venus Flytrap. That does not belong in a sprint plan unless it is already part of product requirements.

### Missing tasks

- It misses `KbLoaderTest`, which currently asserts `kb.archetypes` size 8 and `kb.species` size 16. `Phase 4` names `KbContentSpeciesTest.bundlesExactlySixteenSpecies` and `KbContentArchetypesTest`, but not `KbLoaderTest`.
- It does not explicitly require an assertion that the existing 10 house-plant mappings still point to the same KB IDs. My draft has that guard.
- It has docs tasks, but no sprint evidence task equivalent to my `docs/sprints/evidence/PLANTPOTTING-0009/` note recording final counts, commands run, and the unprobed calibration gap.
- It does not explicitly preserve existing 16 species byte-for-byte beyond acceptance text and a diff check. A task to compare or at least review original IDs/mappings would help.
- It does not ask implementers to consider `mapping.kind = "blend"` during vetting. Even if no target ends up blended, the task list should not encode a false schema constraint.

### Risks underweighted

- Schema-shape risk is underweighted because of the `single` mandate. A future implementer could reject or avoid a valid blend mapping even though the KB engine and tests support it.
- The AIY dormant-row mitigation has product-semantics risk: adding rows keyed by labels the AIY model may never emit preserves a test invariant, but can make the AIY map look more capable than it is. The draft cites the existing dormant-row convention, but the risk should still be called out explicitly.
- Dracaena granularity is underweighted. `Dracaena marginata` and `Dracaena fragrans` sit in a standard-care envelope, but the genus also contains the existing succulent-like `dracaena-trifasciata`; the class-map note and species rationale need to be very clear.
- The draft marks several rows "exact" where the model label is parenthesized but still a common model class. That is fine for key exactness, but care-card exactness should remain editorially cautious.

### Sequencing problems

- `Phase 3 - AIY map coverage-invariant maintenance` may need to happen before or alongside tests, but it should first be a decision point: either preserve the AIY inverse-coverage invariant with dormant rows or change that invariant for the active model era. The draft assumes the former.
- `Phase 4 - Tests` comes before `Phase 5 - Toxicity & special-case vet`; some species text may churn after vetting. Tests for counts and integrity can be written early, but content acceptance should wait until the vet gate closes.
- `Phase 6 - Docs` comes after vetting, which is good, but the unprobed-calibration and Pilea-deferral notes should be drafted early enough that reviewers can compare implementation against the known-gap language.

## If I were merging

- Keep Claude's exact-label table, one-new-archetype stance, AIY coverage-invariant task, and house-plant map integrity test plan.
- Keep Gemini's compact species grouping by aroid / specialized / standard-care lanes, but replace its invented archetypes with the existing `succulent-gritty`, `moisture-retentive`, and `standard-houseplant` reuse decisions.
- From my draft, keep the explicit existing-10-mappings guard, sprint evidence note, Venus Flytrap archetype safety constraints, and "exactly 26 mapped unless deliberately expanded" acceptance criterion.
