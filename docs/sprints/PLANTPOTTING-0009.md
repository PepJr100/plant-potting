# PLANTPOTTING-0009 — Text-only KB-expansion over the model-covered delta

**Type:** Content / config expansion (NO ML, NO fine-tune, NO imagery, NO probing)
**Status:** PLANNED
**Depends on:** PLANTPOTTING-0007 (House Plant Species MobileNetV2 is the production default),
PLANTPOTTING-0008 (data-spike PARTIAL-GO; fine-tune deferred).

> **Merge note.** This plan was synthesised from three independent drafts (codex, gemini, claude) and
> three cross-critiques. It anchors on the claude draft's verbatim-label table and AIY coverage-invariant
> catch, grafts codex's preflight / evidence-artifact / existing-row regression guard / schema-restraint
> non-goal, and adopts gemini's care-lane reading order — with the archetype errors gemini's own critique
> disowned (no redundant `fern-*`/`palm-*` archetypes, no re-declared `succulent-gritty`) removed. The
> one gap all three drafts shared — naming **every** count-assertion site, including `KbLoaderTest` — is
> fixed here.

---

## 1. Intent

The production model `house_plant_species_mobilenetv2` emits **47 classes** but only **10** currently
resolve to a knowledge-base care card (8 exact + 2 coarse). This sprint **additively** maps **16 more
delta species** so coverage moves `10 → 26` of 47 model classes, as pure text/config work. Every newly
covered class gains a real, vetted potting-mix care card instead of dropping to the low-confidence
picker. No model training, no fine-tune, no self-shot/first-party imagery, and no real-photo probing —
the unprobed calibration of these classes is recorded as a known gap, not closed here.

The deliverable is four asset edits + tests + docs + an evidence note:

| Asset | Current | After sprint |
|---|---|---|
| `app/src/main/assets/kb/species.json` | 16 entries | **32** entries |
| `app/src/main/assets/kb/archetypes.json` | 8 archetypes | **9** (+`carnivorous-peat-sand`) |
| `app/src/main/assets/ml/house_plant_species_mobilenetv2/plant_class_map.json` | 10 mapped rows | **26** mapped rows |
| `app/src/main/assets/ml/aiy_plants_v1/plant_class_map.json` | 18 rows covering 16 KB species | +16 dormant rows covering 32 KB species |

---

## 2. Goals / Non-goals

### Goals
- Add 16 new species entries to `kb/species.json` (append-only; the existing 16 untouched).
- Add 16 byte-exact model-label rows to the house-plant `plant_class_map.json` (10 → 26 mapped).
- Add **one** new archetype (`carnivorous-peat-sand`, Venus Flytrap); all 15 other species reuse the
  existing vetted archetypes.
- Maintain the cross-model coverage invariant (dormant AIY rows) and bump all hard count assertions.
- Add the **first** referential-integrity / coverage test over the house-plant class map (none exists
  today).
- Vet toxicity + special-case care content before merge.
- Keep `verifyNoNetworking` and `scripts/check-stub-isolation.sh` GREEN.

### Non-goals / scope boundaries
- **NO ML work**: no training, fine-tuning, model swap, threshold/calibration edits, or
  `model_manifest.json` numeric changes.
- **NO imagery / probing**: no self-shot or first-party photos; no real-photo probing of the new
  classes. Per-class calibration stays **UNPROBED** — recorded as a known gap (§8), not closed.
- **NO Pilea**: `Chinese Money Plant (Pilea peperomioides)` is **deliberately not added**. It ships in a
  later sprint bundled with the pothos↔Pilea boundary fix, so a wrong-but-confident card cannot surface.
  Its model label MUST remain unmapped this sprint, enforced by a CI assertion (not just review).
- **NO modification of the existing 16 KB species entries** or their archetype mappings (append-only).
- **NO modification of the frozen `PlantIdentifier` / `IdentificationResult` seam** or any runtime
  identification code — assets + tests + docs only; new entries flow through the existing generic seam
  with zero code change.
- **NO care-card schema widening** unless validation proves the current schema cannot represent a
  required warning (toxicity text lives in the existing rationale/notes fields).

---

## 3. Reference: the 16 species (care-lane grouped) → exact model label → KB id → archetype

Model labels below are copied **verbatim** from `house_plant_species_mobilenetv2/labels.csv`. Runtime
lookup is **exact-match on the label string**, so the parenthetical suffix is part of the key — any
truncation silently fails to map. (This is the single biggest correctness risk in the sprint; two of the
three source drafts truncated these.)

Bare labels (no parenthetical) for: `Aloe Vera`, `Dracaena`, `Tradescantia`, `Schefflera`, `Kalanchoe`,
`Venus Flytrap`. All others carry the parenthetical shown.

### Aroids → reuse `aroid-chunky`
| # | Exact model label | KB id | scientificName | Map kind | Flag |
|---|---|---|---|---|---|
| 1 | `Chinese evergreen (Aglaonema)` | `aglaonema` | Aglaonema | coarse/genus (`alias:true`) | mild toxic |
| 2 | `Elephant Ear (Alocasia spp.)` | `alocasia` | Alocasia (spp.) | coarse/genus (`alias:true`) | toxic |
| 3 | `Anthurium (Anthurium andraeanum)` | `anthurium-andraeanum` | Anthurium andraeanum | exact | mild toxic |
| 4 | `Dumb Cane (Dieffenbachia spp.)` | `dieffenbachia` | Dieffenbachia (spp.) | coarse/genus (`alias:true`) | **toxic** |

### Succulents → reuse `succulent-gritty`
| # | Exact model label | KB id | scientificName | Map kind | Flag |
|---|---|---|---|---|---|
| 5 | `Aloe Vera` | `aloe-vera` | Aloe vera | exact | — |
| 6 | `Kalanchoe` | `kalanchoe` | Kalanchoe | coarse/genus (`alias:true`) | toxic |

### Moisture-loving → reuse `moisture-retentive`
| # | Exact model label | KB id | scientificName | Map kind | Flag |
|---|---|---|---|---|---|
| 7 | `Prayer Plant (Maranta leuconeura)` | `maranta-leuconeura` | Maranta leuconeura | exact | non-toxic |
| 8 | `Boston Fern (Nephrolepis exaltata)` | `nephrolepis-exaltata` | Nephrolepis exaltata | exact | non-toxic |

> Maranta and Boston Fern are dry-back-intolerant terrestrials; `moisture-retentive` covers the
> substrate envelope. A dedicated `fern-*` archetype was considered and **rejected** as redundant.

### Standard terrestrials → reuse `standard-houseplant`
| # | Exact model label | KB id | scientificName | Map kind | Flag |
|---|---|---|---|---|---|
| 9 | `Money Tree (Pachira aquatica)` | `pachira-aquatica` | Pachira aquatica | exact | non-toxic |
| 10 | `Areca Palm (Dypsis lutescens)` | `dypsis-lutescens` | Dypsis lutescens | exact | non-toxic |
| 11 | `Dracaena` | `dracaena` | Dracaena (marginata / fragrans) | coarse/genus (`alias:true`) | mild toxic |
| 12 | `Tradescantia` | `tradescantia` | Tradescantia | coarse/genus (`alias:true`) | mild irritant |
| 13 | `English Ivy (Hedera helix)` | `hedera-helix` | Hedera helix | exact | **toxic** |
| 14 | `Schefflera` | `schefflera` | Schefflera | coarse/genus (`alias:true`) | toxic |
| 15 | `Poinsettia (Euphorbia pulcherrima)` | `euphorbia-pulcherrima` | Euphorbia pulcherrima | exact | **irritant/latex** |

> A dedicated airy-`palm-*` archetype was considered and **rejected**: `standard-houseplant` covers
> Areca's free-draining peat envelope for this sprint.
> **Dracaena is genus-level and coarse:** the model label is the bare `Dracaena`. The new `dracaena`
> KB id represents the standard-care marginata/fragrans envelope and is **distinct from the existing
> `dracaena-trifasciata`** (snake plant, `succulent-gritty`), which stays separately mapped. The
> class-map `_note` must state this explicitly.

### Carnivorous → NEW archetype `carnivorous-peat-sand`
| # | Exact model label | KB id | scientificName | Map kind | Flag |
|---|---|---|---|---|---|
| 16 | `Venus Flytrap` | `dionaea-muscipula` | Dionaea muscipula | exact (monospecific) | special-case |

> The only species that needs a new archetype: carnivorous, nutrient-poor peat/sand, **zero fertiliser**,
> **zero lime/dolomite**, **distilled/rain water only**. No existing archetype is botanically valid
> (`succulent-gritty` is mineral/fertilised; `acidic-ericaceous` carries an acidic amendment + bark).

**Schema note — `blend` considered, not needed.** `species.json` supports `mapping.kind = "blend"`
(used by `hoya-carnosa` with `primaryArchetypeId`/`secondaryArchetypeId`/`primaryPct`). None of the 16
cleanly straddle two substrate lanes, so all 16 use `mapping.kind = "single"`. `blend` is **not
forbidden** — if the toxicity/care vet (Phase 5) surfaces a genuine two-substrate tension for any
species, an executor may use it following the `hoya-carnosa` pattern. Do not treat `single` as a schema
requirement.

**Coarse/genus rows** follow the established `Orchid`→phalaenopsis / `Calathea`→goeppertia precedent:
the row is tagged `"alias": true` with a `_note`, mapping a broad model class onto the KB's
representative species. Existing confidence gating in the seam handles weak hits.

---

## 4. Phases & task list

### Phase 0 — Preflight & guardrails (do first; cheapest defence against the label-truncation bug)
- [x] Read `kb/species.json`, `kb/archetypes.json`, both `plant_class_map.json` files, and
      `house_plant_species_mobilenetv2/labels.csv`; capture the **before-counts** (16 species, 8
      archetypes, 10 mapped house-plant rows).
- [x] **Verify all 16 target labels appear in `labels.csv` byte-exactly** (full parenthetical form per
      §3). Copy the matched strings directly from the file — do not retype them.
- [x] Confirm `Chinese Money Plant (Pilea peperomioides)` is present in `labels.csv` and currently
      **unmapped**, and will remain unmapped after this sprint.
- [x] Identify the hard count assertions that will break: `KbContentSpeciesTest.bundlesExactlySixteenSpecies`
      (→32), `KbContentArchetypesTest.bundlesExactlyEightArchetypes` (→9), and **`KbLoaderTest`**
      (species size 16→32, archetypes size 8→9).
- [x] Confirm `ModelLabelMappingValidationTest.mappingCoversEveryBundledKbSpecies` is AIY-scoped
      (`dir = "ml/aiy_plants_v1"`, asserts `kbIds − mappingIds == ∅`) — it will red without Phase 3.

### Phase 1 — New archetype (do before species; Phase 2 references it)
- [x] Add `carnivorous-peat-sand` to `kb/archetypes.json` with `id`, `displayName`
      ("Carnivorous Bog (Peat/Sand)"), `shortDescription`, `recipe` (≈50% sphagnum peat moss / 50%
      horticultural silica sand or perlite; **zero** fertiliser, **zero** lime/dolomite),
      `rationaleTemplate` noting nutrient-poor substrate + distilled/rain-water mandate, and `citations[]`
      (drafter-sourced general horticulture, vet-flagged).
- [x] Verify the new archetype's `recipe` proportion percentages sum to 100 (matches every existing
      archetype's convention).
- [x] Do **not** re-declare `succulent-gritty`; do **not** add `fern-*` or `palm-*` archetypes.

### Phase 2 — `species.json` entries (append-only at the END of the array; one checkbox per species)
Each entry follows the existing schema: `id`, `scientificName`, `commonNames[]`, `aliases[]`,
`mapping{kind:"single", archetypeId}` (see §3 blend note), `speciesRationale` (drafter-sourced
horticulture, vet-flagged), `citations[]` (≥1, non-empty). Grouped by care lane for legibility:

- [x] `aglaonema` (Aglaonema) → `aroid-chunky`
- [x] `alocasia` (Alocasia) → `aroid-chunky`
- [x] `anthurium-andraeanum` (Anthurium andraeanum) → `aroid-chunky`
- [x] `dieffenbachia` (Dieffenbachia) → `aroid-chunky` — toxic flag in rationale
- [x] `aloe-vera` (Aloe vera) → `succulent-gritty`
- [x] `kalanchoe` (Kalanchoe) → `succulent-gritty`
- [x] `maranta-leuconeura` (Maranta leuconeura) → `moisture-retentive`
- [x] `nephrolepis-exaltata` (Nephrolepis exaltata) → `moisture-retentive`
- [x] `pachira-aquatica` (Pachira aquatica) → `standard-houseplant`
- [x] `dypsis-lutescens` (Dypsis lutescens) → `standard-houseplant`
- [x] `dracaena` (Dracaena marginata/fragrans) → `standard-houseplant` (distinct from `dracaena-trifasciata`)
- [x] `tradescantia` (Tradescantia) → `standard-houseplant`
- [x] `hedera-helix` (Hedera helix) → `standard-houseplant` — toxic flag in rationale
- [x] `schefflera` (Schefflera) → `standard-houseplant` — toxic flag in rationale
- [x] `euphorbia-pulcherrima` (Euphorbia pulcherrima) → `standard-houseplant` — irritant/latex flag
- [x] `dionaea-muscipula` (Dionaea muscipula) → `carnivorous-peat-sand` — special-case care
- [x] Confirm no new `id` collides with an existing id and no `scientificName`/`aliases` collides with an
      existing normalised alias (guards `speciesIdsAreUnique` / `normalisedAliasesAreUnique`). Keep
      aliases minimal — watch `Money Tree` vs existing `crassula-ovata`'s "Money plant" common name, and
      `Dracaena` vs existing `dracaena-trifasciata`.

### Phase 3 — House-plant class-map rows (additive to `mapping{}`; keys byte-exact from §3)
Exact rows: `{ "kbSpeciesId": "<id>" }`. Coarse/genus rows:
`{ "kbSpeciesId": "<id>", "alias": true, "_note": "COARSE/genus: ..." }`.

- [x] `Chinese evergreen (Aglaonema)` → `aglaonema` (coarse)
- [x] `Elephant Ear (Alocasia spp.)` → `alocasia` (coarse)
- [x] `Anthurium (Anthurium andraeanum)` → `anthurium-andraeanum` (exact)
- [x] `Dumb Cane (Dieffenbachia spp.)` → `dieffenbachia` (coarse)
- [x] `Aloe Vera` → `aloe-vera` (exact)
- [x] `Kalanchoe` → `kalanchoe` (coarse)
- [x] `Prayer Plant (Maranta leuconeura)` → `maranta-leuconeura` (exact)
- [x] `Boston Fern (Nephrolepis exaltata)` → `nephrolepis-exaltata` (exact)
- [x] `Money Tree (Pachira aquatica)` → `pachira-aquatica` (exact)
- [x] `Areca Palm (Dypsis lutescens)` → `dypsis-lutescens` (exact)
- [x] `Dracaena` → `dracaena` (coarse; `_note` must state snake plant stays mapped to `dracaena-trifasciata`)
- [x] `Tradescantia` → `tradescantia` (coarse)
- [x] `English Ivy (Hedera helix)` → `hedera-helix` (exact)
- [x] `Schefflera` → `schefflera` (coarse)
- [x] `Poinsettia (Euphorbia pulcherrima)` → `euphorbia-pulcherrima` (exact)
- [x] `Venus Flytrap` → `dionaea-muscipula` (exact)
- [x] Update the file's `_comment` to reflect new coverage (10 → 26; note Pilea still intentionally
      unmapped).
- [x] Confirm the existing 10 rows are **unchanged** and `Chinese Money Plant (Pilea peperomioides)` is
      **absent** (Pilea deferral guard).

### Phase 4 — AIY coverage-invariant maintenance (Risk R1)
`mappingCoversEveryBundledKbSpecies` (AIY-scoped) asserts every bundled KB species is reachable from the
**AIY** map. Adding 16 KB species reds it unless dormant rows are added.
- [x] Add 16 **dormant, non-alias** rows to `ml/aiy_plants_v1/plant_class_map.json` keyed by
      scientificName (e.g. `"Aloe vera": { "kbSpeciesId": "aloe-vera" }`), one per new KB id, so
      `kbIds − mappingIds == ∅` holds. (Dormant = AIY's vocabulary may never emit these; this is the
      documented intentional pattern, not a regression.)
- [x] Keep them non-alias single rows (avoids tripping `aliasRowsHaveNonAliasCounterpartPointingAtSameKbId`).
- [x] Do NOT alter any existing AIY row.

### Phase 5 — Toxicity & special-case content vet (blocking gate before merge)
Vet care facts before the content is considered final. Author the rationale/warnings during Phase 2, then
this gate confirms them.
- [x] **Dieffenbachia** — insoluble calcium-oxalate toxicity → handling / keep-away-from-pets-&-children
      warning in care text.
- [x] **English Ivy (Hedera helix)** — toxic to pets & humans (saponins) → warning.
- [x] **Poinsettia (Euphorbia pulcherrima)** — latex/sap irritant, mildly toxic → warning; confirm
      `standard-houseplant` (well-draining peat-based) substrate is correct.
- [x] **Venus Flytrap (Dionaea muscipula)** — confirm `carnivorous-peat-sand` recipe & rationale:
      **distilled/rain water only**, **no fertiliser**, **no lime**, nutrient-poor peat/sand, dormancy
      note.
- [x] Spot-vet remaining toxic/irritant flags (Alocasia, Anthurium, Aglaonema, Schefflera, Kalanchoe,
      Tradescantia) for a warning convention consistent with existing entries.
- [x] Confirm the fern (`moisture-retentive`) and palm (`standard-houseplant`) reuse decisions; introduce
      a dedicated archetype only if the vet rejects reuse (would expand Phase 1).

### Phase 6 — Tests
- [ ] Bump `KbContentSpeciesTest.bundlesExactlySixteenSpecies` → assert size **32** (rename to
      `bundlesExactlyThirtyTwoSpecies`).
- [ ] Bump `KbContentArchetypesTest.bundlesExactlyEightArchetypes` → assert size **9** (rename
      accordingly).
- [ ] Bump `KbLoaderTest` size assertions: species **16→32**, archetypes **8→9**.
- [ ] Add a test asserting `carnivorous-peat-sand` exists and `dionaea-muscipula` maps to it.
- [ ] **New house-plant-map coverage/integrity test** (none exists today; mirror the AIY validation test
      pointed at `ml/house_plant_species_mobilenetv2`). It MUST assert:
  - [ ] JSON parses; `version`/`modelLabelsAsset`/`mapping` present;
        `modelLabelsAsset == "ml/house_plant_species_mobilenetv2/labels.csv"`.
  - [ ] Every `kbSpeciesId` resolves to a real KB species id.
  - [ ] Every mapping **key** exists verbatim as a line in `labels.csv` (catches label typos).
  - [ ] Mapped-class count is **exactly 26** (10 existing + 16 new) — guards against accidental extras
        *and* silent drops.
  - [ ] Each of the 16 new labels is present and resolves to its expected KB id (§3).
  - [ ] The existing 10 mapped rows still point to the same KB ids as before (positive regression guard).
  - [ ] `Chinese Money Plant (Pilea peperomioides)` is **NOT** a mapping key (Pilea deferral guard).
  - [ ] Do **not** import the AIY alias-counterpart rule (the house-plant map intentionally has coarse
        alias rows without non-alias counterparts, e.g. existing `Orchid`, `Calathea`).
- [ ] Sanity-check `RecommendationGoldenTest` / `RecommendationEngineArchetypeTest` don't enumerate the
      full species set in a way the 16 additions break; adjust goldens only if additive and required.

### Phase 7 — Docs & evidence
- [ ] Update `docs/kb/ml-mapping-notes.md`: the 16 new mappings, the coarse/genus rows, the Pilea
      deferral + rationale, and the unprobed-calibration known gap.
- [ ] Write `docs/sprints/evidence/PLANTPOTTING-0009/` note (matches the 0007 convention): final species
      count, final mapped-class count, new-archetype count, commands run, and an explicit record that the
      16 new mappings are editorial/model-vocabulary coverage only — **not** calibrated with real-photo
      probes.

### Phase 8 — CI gates & full-suite verification
- [ ] Validate all four edited JSON assets parse (no trailing-comma / encoding errors) **before** the
      test run.
- [ ] Run the full unit suite (`./gradlew :app:testDebugUnitTest` or project equivalent) — GREEN,
      including the bumped counts and the new house-plant coverage/integrity test.
- [ ] `verifyNoNetworking` Gradle task GREEN.
- [ ] `scripts/check-stub-isolation.sh` GREEN.
- [ ] `git diff --stat` review: ONLY `kb/species.json`, `kb/archetypes.json`, both `plant_class_map.json`
      files, the touched test files, the two docs, and the evidence note are modified — the existing 16
      KB entries and the `PlantIdentifier`/`IdentificationResult` seam are untouched.

---

## 5. Sequencing

`Phase 0 (preflight)` → `Phase 1 (archetype)` → `Phase 2 (species)` → `Phase 3 (house-plant map)` →
`Phase 4 (AIY dormant rows)` → `Phase 6 (tests)` → `Phase 7 (docs/evidence)` → `Phase 8 (CI + full
suite)`.

`Phase 5 (content vet)` runs alongside Phase 2 authoring (facts must be sourced before content is final)
and is a **blocking gate** that must close before the sprint is marked DONE. Phases 0–4 are mechanical
and may be drafted together; Phase 6 must follow the asset edits because it asserts against them.

---

## 6. Risks & mitigations

| ID | Risk | Likelihood | Mitigation |
|---|---|---|---|
| R1 | **AIY-map coverage test breaks.** `mappingCoversEveryBundledKbSpecies` (AIY-scoped) requires every KB species reachable from the AIY map; +16 KB species fails it. | High (certain if missed) | Phase 4: 16 dormant non-alias AIY rows. Called out so the red test isn't mistaken for a content bug. |
| R2 | **Label truncation → silent non-mapping.** Runtime is exact-match; dropping the parenthetical (the failure two source drafts made) silently fails to resolve. | High (the dominant defect) | §3 verbatim table + Phase 0 `labels.csv` verification + Phase 6 "every key ∈ labels.csv" assertion. |
| R3 | **Hard count assertions fail** (`bundlesExactlySixteenSpecies`, `bundlesExactlyEightArchetypes`, `KbLoaderTest`). | High (certain) | Phase 6 names all three sites with new values (32 / 9 / 32+9). |
| R4 | **Accidentally mapping Pilea** → wrong-but-confident card. | Low | Phase 3 absence guard + Phase 6 CI assertion (not just human review). |
| R5 | **Unprobed calibration** → a coarse class fires a confident-but-marginal card. | Medium (accepted) | Coarse rows `alias:true` route through existing gating; recorded as known gap (§8), not closed. No probing this sprint. |
| R6 | **Horticulture content errors** (toxicity; Venus Flytrap water/fertiliser). | Medium | Phase 5 blocking vet gate; toxic species flagged in §3. |
| R7 | **Alias/id collision** with existing 16 entries (e.g. Money Tree↔Money plant, Dracaena↔dracaena-trifasciata). | Low | Phase 2 final check + uniqueness tests. |
| R8 | **New archetype recipe % ≠ 100** (convention). | Low | Phase 1 sum check. |
| R9 | **JSON parse error** (trailing comma) bricks asset load at test time. | Low | Phase 8 parse-validation step before suite. |
| R10 | **Care-card schema pressure** from toxicity notes triggers an unnecessary schema/UI change. | Low | Schema-restraint non-goal (§2): warnings live in existing rationale/notes fields. |
| R11 | **AIY dormant rows misrepresent capability** (rows for labels AIY may never emit). | Low (accepted) | Documented intentional pattern; recorded in the evidence note. |

---

## 7. Acceptance criteria

- [ ] `kb/species.json` contains **32** entries; the original 16 are byte-for-byte unchanged.
- [ ] `kb/archetypes.json` contains **9** archetypes (+`carnivorous-peat-sand`); each new species maps to
      a valid archetype; `succulent-gritty` is not re-declared; no `fern-*`/`palm-*` archetype added.
- [ ] `house_plant_species_mobilenetv2/plant_class_map.json` maps **exactly 26** of 47 model classes; the
      16 target labels each resolve to the §3 KB id; the existing 10 rows are unchanged;
      `Chinese Money Plant (Pilea peperomioides)` is unmapped.
- [ ] `aiy_plants_v1/plant_class_map.json` covers all 32 KB species (16 dormant rows added); no existing
      AIY row changed.
- [ ] Full unit suite GREEN, including the bumped count assertions (`bundlesExactlyThirtyTwoSpecies`,
      archetypes size 9, `KbLoaderTest` 32/9) and the new house-plant map coverage/integrity test.
- [ ] `verifyNoNetworking` GREEN; `scripts/check-stub-isolation.sh` GREEN.
- [ ] `PlantIdentifier` / `IdentificationResult` seam and the original 16 KB entries untouched
      (`git diff` confirms).
- [ ] Toxicity warnings present for Dieffenbachia, English Ivy, Poinsettia (+ consistent flags for the
      other toxic species); Venus Flytrap card carries distilled-water + no-fertiliser + no-lime
      guidance; all new content vet-approved (Phase 5 closed).
- [ ] `docs/kb/ml-mapping-notes.md` updated; `docs/sprints/evidence/PLANTPOTTING-0009/` note records final
      counts, commands, and the unprobed-calibration gap.

## 8. Known gaps recorded (NOT closed this sprint)

- **Unprobed calibration of the 16 newly mapped classes** — no first-party imagery / real-photo probing;
  per-class confidence behaviour is assumed-from-routing, not measured. Addressed when imagery work is
  greenlit.
- **Pilea (`Chinese Money Plant`) deferred** — ships with the pothos↔Pilea boundary fix in a later sprint.
- **Coarse/genus rows** (Aglaonema, Alocasia, Dieffenbachia, Dracaena, Tradescantia, Schefflera,
  Kalanchoe) map a broad model class to one representative KB species; sub-species substrate divergence
  within those genera is not modelled.
- **Fern/palm archetype reuse** — Boston Fern → `moisture-retentive`, Areca Palm → `standard-houseplant`;
  revisit if a dedicated archetype proves warranted.
- **AIY dormant rows** preserve a test invariant but make the AIY map look more capable than its
  vocabulary supports (recorded, accepted).
