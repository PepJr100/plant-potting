# PLANTPOTTING-0009 — Text-only KB-expansion over the model-covered delta (CLAUDE draft)

**Type:** Content / config expansion (NO ML, NO fine-tune, NO imagery)
**Status:** DRAFT
**Author CLI:** claude
**Depends on:** PLANTPOTTING-0007 (House Plant Species MobileNetV2 is production default), PLANTPOTTING-0008 (data-spike PARTIAL-GO; fine-tune deferred)

---

## 1. Goal

The production model `house_plant_species_mobilenetv2` emits **47 classes** but only **10** currently
resolve to a knowledge-base care card (8 exact + 2 coarse). This sprint **additively** maps **16 more
delta species** — `10 → 26` of 47 model classes covered — as pure text/config work. Every covered class
gains a real, vetted potting-mix care card instead of dropping to the low-confidence picker.

Deliverable is three asset edits + supporting tests/docs:
1. `app/src/main/assets/kb/species.json` — 16 new entries (16 → 32).
2. `app/src/main/assets/ml/house_plant_species_mobilenetv2/plant_class_map.json` — 16 new rows (10 → 26).
3. `app/src/main/assets/kb/archetypes.json` — **one** new archetype (`carnivorous-peat-sand`) for Venus Flytrap; all 15 other species reuse existing vetted archetypes.

Plus: AIY-map coverage-invariant maintenance (see §6 risk R1), a house-plant-map coverage test, count-assertion updates, a toxicity/special-case vet, and doc updates.

---

## 2. Non-goals / scope boundaries

- **NO ML work**: no training, no fine-tuning, no model swap, no threshold re-calibration, no `model_manifest.json` numeric edits.
- **NO imagery**: no self-shot / first-party photos, no real-photo probing of the newly mapped classes. Per-class calibration of the 16 new mappings stays **UNPROBED** — recorded as a known gap (§8), not closed here.
- **NO Pilea**: `Chinese Money Plant (Pilea peperomioides)` is **deliberately not added**. It ships in a later sprint bundled with the pothos↔Pilea boundary fix so a wrong-but-confident card cannot surface. The model label `Chinese Money Plant (Pilea peperomioides)` MUST remain unmapped this sprint.
- **NO modification of the existing 16 KB species entries** or their archetype mappings.
- **NO modification of the frozen `PlantIdentifier` / `IdentificationResult` seam** or any runtime identification code. This sprint touches assets + tests + docs only.
- **NO new runtime behaviour**: the recommendation/routing engine already consumes `species.json` + the class map generically; new entries flow through the existing seam with zero code change.

---

## 3. Current state (grounded)

| Asset | Current | After sprint |
|---|---|---|
| `kb/species.json` | 16 entries | 32 entries |
| `kb/archetypes.json` | 8 archetypes | 9 archetypes (+`carnivorous-peat-sand`) |
| `house_plant_species_mobilenetv2/plant_class_map.json` | 10 mapped rows | 26 mapped rows |
| `aiy_plants_v1/plant_class_map.json` | 18 rows covering 16 KB species | +16 dormant rows covering 32 KB species |

Existing archetypes available for reuse: `standard-houseplant`, `aroid-chunky`, `succulent-gritty`,
`cactus-pure-mineral`, `epiphytic-orchid-bark`, `moisture-retentive`, `semi-hydro-inert`, `acidic-ericaceous`.

---

## 4. Reference: the 16 species → KB id → archetype → exact model label

Model labels below are copied **verbatim** from `house_plant_species_mobilenetv2/labels.csv` — runtime lookup is exact-match on the label string, so any typo silently fails to map.

| # | Model label (exact) | KB id | scientificName | Archetype | Map row kind | Flag |
|---|---|---|---|---|---|---|
| 1 | `Aloe Vera` | `aloe-vera` | Aloe vera | `succulent-gritty` | exact | — |
| 2 | `Chinese evergreen (Aglaonema)` | `aglaonema` | Aglaonema | `aroid-chunky` | coarse/genus | mild toxic |
| 3 | `Elephant Ear (Alocasia spp.)` | `alocasia` | Alocasia (spp.) | `aroid-chunky` | coarse/genus | toxic |
| 4 | `Anthurium (Anthurium andraeanum)` | `anthurium-andraeanum` | Anthurium andraeanum | `aroid-chunky` | exact | mild toxic |
| 5 | `Dumb Cane (Dieffenbachia spp.)` | `dieffenbachia` | Dieffenbachia (spp.) | `aroid-chunky` | coarse/genus | **toxic** |
| 6 | `Prayer Plant (Maranta leuconeura)` | `maranta-leuconeura` | Maranta leuconeura | `moisture-retentive` | exact | non-toxic |
| 7 | `Boston Fern (Nephrolepis exaltata)` | `nephrolepis-exaltata` | Nephrolepis exaltata | `moisture-retentive` | exact | non-toxic |
| 8 | `Money Tree (Pachira aquatica)` | `pachira-aquatica` | Pachira aquatica | `standard-houseplant` | exact | non-toxic |
| 9 | `Areca Palm (Dypsis lutescens)` | `dypsis-lutescens` | Dypsis lutescens | `standard-houseplant` | exact | non-toxic |
| 10 | `Dracaena` | `dracaena-marginata` | Dracaena marginata | `standard-houseplant` | coarse/genus | mild toxic |
| 11 | `Tradescantia` | `tradescantia` | Tradescantia | `standard-houseplant` | coarse/genus | mild irritant |
| 12 | `English Ivy (Hedera helix)` | `hedera-helix` | Hedera helix | `standard-houseplant` | exact | **toxic** |
| 13 | `Schefflera` | `schefflera` | Schefflera | `standard-houseplant` | coarse/genus | toxic |
| 14 | `Kalanchoe` | `kalanchoe` | Kalanchoe | `succulent-gritty` | coarse/genus | toxic |
| 15 | `Poinsettia (Euphorbia pulcherrima)` | `euphorbia-pulcherrima` | Euphorbia pulcherrima | `standard-houseplant` | exact | **irritant/latex** (user owns one) |
| 16 | `Venus Flytrap` | `dionaea-muscipula` | Dionaea muscipula | `carnivorous-peat-sand` **(NEW)** | exact (monospecific) | special-case (user owns one) |

**Archetype reuse rationale:**
- Aroids (#2,3,4,5) → `aroid-chunky` per intent (epiphytic/terrestrial aroids; bark-led, oxygen-hungry roots).
- Succulents Aloe & Kalanchoe (#1,14) → `succulent-gritty` (water-storing CAM foliage; mineral, fast-drying).
- Maranta & Boston Fern (#6,7) → `moisture-retentive` (dry-back-intolerant terrestrials; coir+sphagnum). Ferns were evaluated for a dedicated `fern-humid` archetype; `moisture-retentive` covers the substrate envelope, so **no new fern archetype** — flagged for vet (§7-T13).
- Money Tree, Areca Palm, Dracaena, Tradescantia, English Ivy, Schefflera, Poinsettia (#8–13,15) → `standard-houseplant` (fibrous terrestrial roots, even moisture + drainage). Palms were evaluated for a dedicated airy-palm archetype; `standard-houseplant` covers Areca's free-draining peat envelope — **no new palm archetype** — flagged for vet (§7-T13).
- Venus Flytrap (#16) → **only** species needing a new archetype: carnivorous, nutrient-poor, peat/sand, **no fertiliser**, **distilled/rain water only**. No existing archetype is botanically valid (`succulent-gritty` is mineral/fertilised; `acidic-ericaceous` carries an acidic amendment + bark — both wrong for a bog carnivore).

**Coarse/genus rows** follow the established `Orchid`→phalaenopsis / `Calathea`→goeppertia precedent: the row is tagged `"alias": true` with a `_note`, mapping a broad model class onto the KB's representative species. Routing honesty + confidence gating already in the seam handle weak hits.

---

## 5. Phases & task list

### Phase 0 — New archetype (do first; species in Phase 1 reference it)

- [ ] Add `carnivorous-peat-sand` archetype to `app/src/main/assets/kb/archetypes.json` with: `id`, `displayName` ("Carnivorous Bog (Peat/Sand)"), `shortDescription`, `recipe` (≈50% sphagnum peat moss / 50% horticultural silica sand or perlite; **zero** fertiliser, **zero** lime/dolomite), `rationaleTemplate` noting nutrient-poor substrate + distilled/rain water mandate, and `citations[]` (drafter-sourced general horticulture, marked for vet).
- [ ] Verify the new archetype's `recipe` `proportionPct` values sum to 100 (matches the convention used by every existing archetype).

### Phase 1 — `species.json` entries (one checkbox per species; all additive at the END of the array)

Each entry MUST follow the existing schema: `id`, `scientificName`, `commonNames[]`, `aliases[]`, `mapping{kind:"single", archetypeId}`, `speciesRationale` (drafter-sourced horticulture, vet-flagged), `citations[]` (≥1, non-empty). Reuse the archetype id from the §4 table.

- [ ] `aloe-vera` (Aloe vera) → `succulent-gritty`
- [ ] `aglaonema` (Aglaonema) → `aroid-chunky`
- [ ] `alocasia` (Alocasia) → `aroid-chunky`
- [ ] `anthurium-andraeanum` (Anthurium andraeanum) → `aroid-chunky`
- [ ] `dieffenbachia` (Dieffenbachia) → `aroid-chunky`
- [ ] `maranta-leuconeura` (Maranta leuconeura) → `moisture-retentive`
- [ ] `nephrolepis-exaltata` (Nephrolepis exaltata) → `moisture-retentive`
- [ ] `pachira-aquatica` (Pachira aquatica) → `standard-houseplant`
- [ ] `dypsis-lutescens` (Dypsis lutescens) → `standard-houseplant`
- [ ] `dracaena-marginata` (Dracaena marginata) → `standard-houseplant` (distinct from existing `dracaena-trifasciata`)
- [ ] `tradescantia` (Tradescantia) → `standard-houseplant`
- [ ] `hedera-helix` (Hedera helix) → `standard-houseplant`
- [ ] `schefflera` (Schefflera) → `standard-houseplant`
- [ ] `kalanchoe` (Kalanchoe) → `succulent-gritty`
- [ ] `euphorbia-pulcherrima` (Euphorbia pulcherrima) → `standard-houseplant`
- [ ] `dionaea-muscipula` (Dionaea muscipula) → `carnivorous-peat-sand`
- [ ] Confirm no `id` collides with an existing id and no `scientificName`/`aliases` collides with an existing normalised alias (guards `KbContentSpeciesTest.normalisedAliasesAreUnique` / `speciesIdsAreUnique`). In particular keep aliases minimal to avoid `Dracaena`/`Money plant`-style clashes.

### Phase 2 — House-plant model class-map rows (one checkbox per row; additive to `mapping{}`)

Add to `app/src/main/assets/ml/house_plant_species_mobilenetv2/plant_class_map.json`. Keys MUST be byte-exact from `labels.csv` (§4). Exact rows: `{ "kbSpeciesId": "<id>" }`. Coarse/genus rows: `{ "kbSpeciesId": "<id>", "alias": true, "_note": "COARSE/genus: ..." }`.

- [ ] `Aloe Vera` → `aloe-vera` (exact)
- [ ] `Chinese evergreen (Aglaonema)` → `aglaonema` (coarse/genus)
- [ ] `Elephant Ear (Alocasia spp.)` → `alocasia` (coarse/genus)
- [ ] `Anthurium (Anthurium andraeanum)` → `anthurium-andraeanum` (exact)
- [ ] `Dumb Cane (Dieffenbachia spp.)` → `dieffenbachia` (coarse/genus)
- [ ] `Prayer Plant (Maranta leuconeura)` → `maranta-leuconeura` (exact)
- [ ] `Boston Fern (Nephrolepis exaltata)` → `nephrolepis-exaltata` (exact)
- [ ] `Money Tree (Pachira aquatica)` → `pachira-aquatica` (exact)
- [ ] `Areca Palm (Dypsis lutescens)` → `dypsis-lutescens` (exact)
- [ ] `Dracaena` → `dracaena-marginata` (coarse/genus)
- [ ] `Tradescantia` → `tradescantia` (coarse/genus)
- [ ] `English Ivy (Hedera helix)` → `hedera-helix` (exact)
- [ ] `Schefflera` → `schefflera` (coarse/genus)
- [ ] `Kalanchoe` → `kalanchoe` (coarse/genus)
- [ ] `Poinsettia (Euphorbia pulcherrima)` → `euphorbia-pulcherrima` (exact)
- [ ] `Venus Flytrap` → `dionaea-muscipula` (exact)
- [ ] Update the file's `_comment` to reflect new coverage ("8 exact + 2 coarse = 10" → updated counts; note Pilea still intentionally unmapped).
- [ ] Confirm `Chinese Money Plant (Pilea peperomioides)` is **absent** from the mapping (Pilea deferral guard).

### Phase 3 — AIY map coverage-invariant maintenance (see Risk R1)

`ModelLabelMappingValidationTest.mappingCoversEveryBundledKbSpecies` asserts every bundled KB species is reachable from the **AIY** `plant_class_map.json`. Adding 16 KB species breaks it unless dormant rows are added.

- [ ] Add 16 **dormant, non-alias** rows to `app/src/main/assets/ml/aiy_plants_v1/plant_class_map.json` keyed by scientificName (e.g. `"Aloe vera": { "kbSpeciesId": "aloe-vera" }`), one per new KB id, so `kbIds − mappingIds == ∅` holds. (Dormant = AIY's label vocabulary may never emit these; this is the documented intentional pattern, not a regression.)
- [ ] Keep these as non-alias single rows (avoids tripping `aliasRowsHaveNonAliasCounterpartPointingAtSameKbId`).
- [ ] Do NOT alter any existing AIY row.

### Phase 4 — Tests (count updates + new coverage test)

- [ ] Update `KbContentSpeciesTest.bundlesExactlySixteenSpecies()` → assert size **32** (rename to `bundlesExactlyThirtyTwoSpecies`). This edits a test, not a KB entry — allowed by the additive constraint.
- [ ] Update `KbContentArchetypesTest` `hasSize(8)` → `hasSize(9)`.
- [ ] **New coverage-count assertion** — add a referential-integrity test for the **house-plant** map (none exists today; mirror `ModelLabelMappingValidationTest` but pointed at `ml/house_plant_species_mobilenetv2`). It MUST assert:
  - [ ] JSON parses; `version`/`modelLabelsAsset`/`mapping` present; `modelLabelsAsset == "ml/house_plant_species_mobilenetv2/labels.csv"`.
  - [ ] Every `kbSpeciesId` resolves to a real KB species id.
  - [ ] Every mapping **key** exists verbatim as a line in `labels.csv` (catches typos in the 16 new labels).
  - [ ] Mapped-class count is **≥ 26** (coverage assertion; `10 + 16`).
  - [ ] Each of the 16 new model labels is present and resolves to its expected KB id.
  - [ ] `Chinese Money Plant (Pilea peperomioides)` is **NOT** a mapping key (Pilea deferral guard).
  - [ ] Do **not** import the AIY test's alias-counterpart rule — the house-plant map intentionally has coarse alias rows without non-alias counterparts (e.g. existing `Orchid`, `Calathea`).
- [ ] Add a test asserting the new `carnivorous-peat-sand` archetype exists and that `dionaea-muscipula` maps to it (or extend `KbContentArchetypesTest`).
- [ ] Sanity-check `RecommendationGoldenTest` / `RecommendationEngineArchetypeTest` do not enumerate the full species set in a way the 16 additions break; adjust goldens only if additive and required.

### Phase 5 — Toxicity & special-case vet (planning gate — resolve before merge)

- [ ] Vet **Dieffenbachia** care card: insoluble calcium-oxalate toxicity → care text must carry a handling/keep-away-from-pets-&-children warning.
- [ ] Vet **English Ivy (Hedera helix)**: toxic to pets & humans (saponins) → warning.
- [ ] Vet **Poinsettia (Euphorbia pulcherrima)**: latex/sap irritant, mildly toxic → warning; confirm `standard-houseplant` substrate is correct (well-draining peat-based).
- [ ] Vet **Venus Flytrap (Dionaea muscipula)** special-case card: **distilled/rain water only**, **no fertiliser**, nutrient-poor peat/sand, dormancy note — confirm `carnivorous-peat-sand` recipe & rationale are horticulturally sound.
- [ ] Spot-vet the remaining toxic/irritant flags (Alocasia, Anthurium, Aglaonema, Schefflera, Kalanchoe, Tradescantia) for a consistent warning convention with existing entries.
- [ ] Confirm the **fern** (`moisture-retentive`) and **palm** (`standard-houseplant`) reuse decisions with the vet; introduce a dedicated archetype only if the vet rejects reuse (would expand Phase 0).

### Phase 6 — Docs

- [ ] Update `docs/kb/ml-mapping-notes.md`: document the 16 new mappings, the coarse/genus rows, the Pilea deferral + its rationale, and the unprobed-calibration known gap.
- [ ] Note in `docs/kb/plant-substrate-kb-notes.md` (or mapping-notes) that calibration of the 16 newly mapped classes is unprobed (links to §8 known gap).

### Phase 7 — CI gates & full-suite verification

- [ ] Run `./gradlew :app:testDebugUnitTest` (or project equivalent) — full unit suite GREEN, including the updated counts + new coverage test.
- [ ] Confirm `verifyNoNetworking` Gradle task stays GREEN (pure asset/text changes introduce no network surface, but verify explicitly).
- [ ] Run `scripts/check-stub-isolation.sh` — stays GREEN (no stub/identifier-seam changes).
- [ ] `git diff --stat` review: ONLY `kb/species.json`, `kb/archetypes.json`, both `plant_class_map.json` files, the touched test files, and the two docs are modified — the 16 existing species entries and the `PlantIdentifier`/`IdentificationResult` seam are untouched.
- [ ] Validate all four edited JSON assets parse (no trailing-comma / encoding errors) before running the suite.

---

## 6. Risks & mitigations

| ID | Risk | Likelihood | Mitigation |
|---|---|---|---|
| R1 | **AIY-map coverage test breaks.** `mappingCoversEveryBundledKbSpecies` (AIY-scoped) requires *every* KB species reachable from the AIY map; +16 KB species fails it. | **High (certain if missed)** | Phase 3: add 16 dormant non-alias AIY rows. Explicitly called out so the executor doesn't treat the red test as a content bug. |
| R2 | Hard species/archetype count assertions (`hasSize(16)`, `hasSize(8)`) fail. | High (certain) | Phase 4: bump to 32 / 9. |
| R3 | Model-label typo → silent non-mapping (runtime exact-match). | Medium | Labels copied verbatim from `labels.csv` (§4); new house-plant test asserts every key exists as a `labels.csv` line. |
| R4 | Accidentally mapping Pilea. | Low | Explicit absence guard in Phase 2 + Phase 4 test. |
| R5 | Unprobed calibration → a coarse/genus class fires a confident-but-marginal card. | Medium (accepted) | Coarse rows tagged `alias:true` route through existing confidence gating; recorded as known gap (§8), not closed. No first-party probing this sprint. |
| R6 | Horticulture content errors (esp. toxicity, Venus Flytrap water/fertiliser). | Medium | Phase 5 vet gate before merge; toxic species flagged in §4. |
| R7 | Alias/id collision with existing 16 entries. | Low | Phase 1 final check; uniqueness tests (`speciesIdsAreUnique`, `normalisedAliasesAreUnique`) catch it. |
| R8 | New archetype recipe percentages don't sum to 100 (convention). | Low | Phase 0 sum check. |
| R9 | JSON parse error (trailing comma) bricks asset load at test time. | Low | Phase 7 parse-validation step before suite run. |

---

## 7. Sequencing summary

`Phase 0 (archetype)` → `Phase 1 (species)` → `Phase 2 (house-plant map)` → `Phase 3 (AIY dormant rows)` → `Phase 4 (tests)` → `Phase 5 (vet gate)` → `Phase 6 (docs)` → `Phase 7 (CI + full suite)`.

Phases 0–3 are mechanical and can be drafted together; Phase 4 must follow the asset edits; Phase 5 is a **blocking gate** before merge; Phases 6–7 close out. The vet (Phase 5) can run in parallel with Phase 4 but must resolve before the sprint is marked DONE.

---

## 8. Acceptance criteria

- [ ] `kb/species.json` contains **32** entries; the original 16 are byte-for-byte unchanged.
- [ ] `kb/archetypes.json` contains **9** archetypes; the 16 new species each map to a valid archetype.
- [ ] `house_plant_species_mobilenetv2/plant_class_map.json` maps **≥ 26** of 47 model classes; the 16 target labels each resolve to the §4 KB id; `Chinese Money Plant (Pilea peperomioides)` is unmapped.
- [ ] `aiy_plants_v1/plant_class_map.json` covers all 32 KB species (dormant rows added); no existing AIY row changed.
- [ ] Full unit suite GREEN, including updated count assertions and the new house-plant map coverage/integrity test.
- [ ] `verifyNoNetworking` GREEN; `scripts/check-stub-isolation.sh` GREEN.
- [ ] `PlantIdentifier` / `IdentificationResult` seam and the original 16 KB entries are untouched (`git diff` confirms).
- [ ] Toxicity warnings present for Dieffenbachia, English Ivy, Poinsettia (+ consistent flags for other toxic species); Venus Flytrap card carries distilled-water + no-fertiliser guidance; all new content vet-approved.
- [ ] `docs/kb/ml-mapping-notes.md` updated with the new mappings, Pilea deferral, and unprobed-calibration gap.

## 9. Known gaps recorded (NOT closed this sprint)

- **Unprobed calibration of the 16 newly mapped classes** — no first-party imagery / real-photo probing; per-class confidence behaviour is assumed-from-routing, not measured. To be addressed when imagery work is greenlit.
- **Pilea (`Chinese Money Plant`) deferred** — ships with the pothos↔Pilea boundary fix in a later sprint.
- **Coarse/genus rows** (Aglaonema, Alocasia, Dieffenbachia, Dracaena, Tradescantia, Schefflera, Kalanchoe) map a broad model class to one representative KB species; sub-species substrate divergence within those genera is not modelled.
- **Fern/palm archetype reuse** — Boston Fern → `moisture-retentive`, Areca Palm → `standard-houseplant`; revisit if a dedicated archetype proves warranted.
