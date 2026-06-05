# PLANTPOTTING-0009 — CLAUDE's critique of the CODEX and GEMINI drafts

**Critic CLI:** claude
**Critiquing:** `PLANTPOTTING-0009-CODEX.md`, `PLANTPOTTING-0009-GEMINI.md`
**Baseline (mine):** `PLANTPOTTING-0009-CLAUDE.md`

Grounding I verified against the repo before writing (so the critique cites facts, not vibes):

- `app/src/main/assets/ml/house_plant_species_mobilenetv2/labels.csv` — the 47 labels are stored in their **full parenthetical form** (`Chinese evergreen (Aglaonema)`, `Elephant Ear (Alocasia spp.)`, `Anthurium (Anthurium andraeanum)`, `Dumb Cane (Dieffenbachia spp.)`, `Prayer Plant (Maranta leuconeura)`, `Boston Fern (Nephrolepis exaltata)`, `Money Tree (Pachira aquatica)`, `Areca Palm (Dypsis lutescens)`, `English Ivy (Hedera helix)`, `Poinsettia (Euphorbia pulcherrima)`). Bare labels only for `Aloe Vera`, `Dracaena`, `Tradescantia`, `Schefflera`, `Kalanchoe`, `Venus Flytrap`. The Pilea label is `Chinese Money Plant (Pilea peperomioides)`.
- `ModelLabelMappingValidationTest.mappingCoversEveryBundledKbSpecies` is **real**, is **AIY-scoped** (`private val dir = "ml/aiy_plants_v1"`), and asserts `kbIds − mappingIds == ∅`.
- Hard count assertions live in **three** test files: `KbContentSpeciesTest.bundlesExactlySixteenSpecies` (size 16), `KbContentArchetypesTest.bundlesExactlyEightArchetypes` (size 8), **and** `KbLoaderTest` (species size 16 + archetypes size 8).
- `archetypes.json` contains exactly 8 archetypes and `succulent-gritty` is **already one of them**.
- `species.json` schema supports `mapping.kind = "blend"` (used by `hoya-carnosa`), not only `"single"`.

---

## A. Critique of `PLANTPOTTING-0009-CODEX.md`

### Stronger than mine
- **Phase 1 — Preflight And Guardrails** is a genuine improvement. It mandates reading `species.json`/`archetypes.json`/`plant_class_map.json` and *capturing the before-counts*, and "Confirm the exact 47-class label vocabulary from `labels.csv` and verify all 16 sprint labels appear exactly as requested." My draft asserts labels are "copied verbatim" (§4) but never makes the executor *diff against the file first*. Ironically, had CODEX actually executed its own Phase 1 task it would have caught its own label bug (see below) — the task is right even though the body contradicts it.
- **Phase 6 — Review Notes And Evidence** writes a structured artifact to `docs/sprints/evidence/PLANTPOTTING-0009/` (final counts, commands run, calibration gap). This matches the existing repo convention (`docs/sprints/evidence/PLANTPOTTING-0007/` exists). My draft only edits `docs/kb/*` notes and has no evidence directory — CODEX's is the better paper trail for a review handoff.
- **Regression guard on the existing 10 rows:** Phase 5 task "validation assertion that the existing 10 mapped MobileNetV2 rows still point to the same KB IDs as before." I have a `git diff` review (Phase 7) but no positive *assertion* that the pre-existing mappings are untouched. CODEX's is a sharper guard.
- **Schema-restraint discipline:** the non-goal "Do not broaden the care-card schema unless validation proves the current schema cannot represent a required warning" and the matching risk (#7) is a good guardrail against toxicity text leaking into a schema change. My draft assumes the rationale field absorbs warnings but never states it as a boundary.

### Weaker than mine
- **FATAL: truncated model labels.** Every "Proposed IDs" line and the Phase 4 class-map tasks use the *common-name stem* as the map key — `Chinese evergreen`, `Elephant Ear`, `Anthurium`, `Dumb Cane`, `Prayer Plant`, `Boston Fern`, `Money Tree`, `Areca Palm`, `English Ivy`, `Poinsettia` — and the Acceptance Criteria *enshrine the wrong strings* ("The 16 new class-map keys match the model labels exactly: … `Chinese evergreen`, `Elephant Ear`, `Anthurium`, `Dumb Cane`, …"). The actual labels carry the parenthetical (`Chinese evergreen (Aglaonema)` etc.). Runtime lookup is exact-match on the label line, so **10 of the 16 new mappings would silently fail to resolve** and the sprint would ship believing it hit 26-class coverage when it actually hit ~16. My §4 table copies all 16 labels in full parenthetical form — this is the single biggest correctness gap between the two drafts.
- **Misses the AIY coverage invariant (my R1).** CODEX has no task to add dormant rows to `ml/aiy_plants_v1/plant_class_map.json`. Its Phase 5 only asserts the *forward* direction ("every `kbSpeciesId` in `plant_class_map.json` exists in `species.json`"). But `mappingCoversEveryBundledKbSpecies` enforces the *reverse* over the AIY map: adding 16 KB species without 16 AIY rows turns that test red, and nothing in CODEX's plan tells the executor it's an expected invariant-maintenance step rather than a content bug. My Phase 3 + R1 handle this explicitly.
- **Under-specified count-assertion updates.** Phase 5's "Add or update a coverage-count assertion that the MobileNetV2 map has 26" is the only count task, and it's vague ("Add or update … or a focused new test"). It never names the three hard assertions that *will* break: `KbContentSpeciesTest.bundlesExactlySixteenSpecies` (16→32), `KbContentArchetypesTest.bundlesExactlyEightArchetypes` (8→9), and `KbLoaderTest` (16/8). My Phase 4 names two of these by symbol with the new values. (Caveat against myself: *neither* of us named `KbLoaderTest` — that's a shared gap I'm fixing in the merge below.)
- **No explicit Pilea test guard.** CODEX defers Pilea correctly in non-goals and asserts it stays unmapped in acceptance, but the enforcement is "a code reviewer will verify its absence" — human, not CI. My Phase 4 bakes a "`Chinese Money Plant (Pilea peperomioides)` is NOT a mapping key" assertion into the new house-plant coverage test.

### Missing tasks
- No task to bump `KbLoaderTest` / `KbContentArchetypesTest` size literals.
- No AIY dormant-row task (above).
- No JSON-parse pre-validation step before running the suite (I have one in Phase 7; CODEX folds "Run JSON parsing/format validation" into Phase 5 but after the edits, not as a gate before the expensive test run — minor).

### Underweighted risks
- The label-exactness risk is the one that actually bites, and CODEX's risk list doesn't contain it at all — its closest entry ("additive JSON edits can accidentally break existing KB IDs") is about *existing* rows, not new-key typos. My R3 ("Model-label typo → silent non-mapping") plus the "every key exists as a `labels.csv` line" test is exactly the missing mitigation.
- The AIY-coverage red test is unranked.

### Sequencing
- Mostly sound and close to mine: Preflight → Archetype → Species → Class-map → Validation → Evidence. One ordering nit: CODEX's Phase 2 ("Archetype Work") validates archetype *reuse* before any species exist, but defers the actual carnivorous-archetype *recipe sum / schema* check into the same phase as authoring — fine. The real sequencing defect is downstream: because the AIY-row step is entirely absent, the validation phase will go red with no preceding step to satisfy it.

---

## B. Critique of `PLANTPOTTING-0009-GEMINI.md`

### Stronger than mine
- **Readability / archetype-grouped phasing.** Grouping the species by care lane (Phase 2 Aroid Additions, Phase 3 Specialised Care, Phase 4 Standard Care) reads cleanly and makes the archetype decision visible per group. My phase split is by *file* (species, then map, then AIY), which is more mechanically faithful but less botanically legible. Genuinely a nicer narrative for a human planner.
- That's about the extent of it — this draft is the thinnest of the three.

### Weaker than mine
- **Invents redundant archetypes — flagged by the grounding note.** Phase 1 adds `fern-moisture-retentive` and `palm-well-draining`. Both are redundant: `moisture-retentive` already covers Boston Fern's substrate envelope and `standard-houseplant` already covers Areca's free-draining peat. The repo has exactly 8 archetypes and only Venus Flytrap plausibly warrants a 9th. My §4 explicitly evaluates and *rejects* a fern/palm archetype, reusing `moisture-retentive`/`standard-houseplant`.
- **Re-declares an existing archetype — hard error.** Phase 1 also adds `succulent-gritty` "for succulents" — but `succulent-gritty` is already one of the 8 archetypes. Authoring it again is a duplicate `id`, which would break archetype-uniqueness loading and `bundlesExactlyEightArchetypes`. This isn't a style nit; it's a build-red bug baked into the plan.
- **Botanical mis-assignment of Maranta.** Phase 4 ("Standard Care Additions … using standard existing archetypes") includes "Add Prayer Plant (*Maranta leuconeura*) … map `Prayer Plant`." Maranta is dry-back-intolerant and belongs in `moisture-retentive` (where both CODEX and I put it), not the standard bucket. Filing it under "Standard Care" is a substrate error that produces a wrong care card.
- **Invented archetype vocabulary.** Phase 4's "standard existing archetypes (e.g., standard potting mix, general tropical)" references "general tropical," which is not one of the 8 archetypes. The executor is left guessing the actual `archetypeId` for half the species — Phase 4 never assigns archetypes per species the way Phases 2–3 do.
- **Truncated model labels — same fatal class as CODEX.** Every Phase 2–4 task maps the stem (`Chinese evergreen`, `Elephant Ear`, `Anthurium`, `Dumb Cane`, `Prayer Plant`, `Boston Fern`, `Money Tree`, `Areca Palm`, `English Ivy`, `Poinsettia`). Exact-match runtime → silent non-resolution for 10 of 16. No `labels.csv` verification task anywhere (it lacks CODEX's saving-grace Phase 1).
- **Misses every count-assertion bump.** Phase 5's lone test task is "assert a minimum mapped coverage count of exactly 26 species" — and it conflates *mapped model classes* with *species* in the wording. It never touches `bundlesExactlySixteenSpecies`, `bundlesExactlyEightArchetypes`, or `KbLoaderTest`. At least four hard assertions go red unplanned.
- **Misses the AIY coverage invariant** entirely (no AIY phase, no dormant rows) — same as CODEX but with even less validation scaffolding around it.
- **No docs / mapping-notes / evidence update.** No `docs/kb/ml-mapping-notes.md` edit, no evidence artifact. My Phase 6 and CODEX's Phase 6 both cover this; GEMINI drops it.

### Missing tasks
- AIY dormant rows; the three count-assertion bumps; docs/mapping-notes update; evidence artifact; a CI-level Pilea-absence assertion (GEMINI relies on "a code reviewer will verify its absence," weakest of the three); JSON-parse pre-gate (it has a JSON-lint task, acceptable).

### Underweighted risks
- Only 3 risks. Omits: label-exactness/typo→silent-miss, the AIY red test, the count-assertion breakage, and — most self-inflicted — the duplicate `succulent-gritty` id and redundant fern/palm archetypes its own Phase 1 introduces. Its risk table essentially doesn't cover the failure modes its own plan creates.

### Sequencing
- The "archetypes first, then species that depend on them" spine is correct (and matches my Phase 0→1). But the dependency is corrupted by Phase 1 itself: three of its four "new" archetypes are wrong (one duplicate, two redundant), so "species depend on new archetypes" propagates bad ids downstream. And bundling `species.json` + `plant_class_map.json` edits into a single checkbox per species couples two files per task — workable at this size, but it means a half-finished checkbox leaves the two assets out of sync, whereas my per-file phases keep each asset internally consistent at each step.

---

## C. Cross-cutting observations (apply to both)

- **Neither leverages `mapping.kind = "blend"`.** Both (and I) default every new species to a single archetype. That's *defensible* here — none of the 16 cleanly straddle two lanes — so I'm not scoring it as a defect, but it's worth a one-line note in the merged plan that `blend` was considered and not needed (CODEX says a generic "mapping.kind", GEMINI doesn't mention `kind` at all; my draft hard-codes `"single"`, which is at least explicit).
- **Both truncate the model labels.** This is the dominant, ship-breaking defect in both drafts and the clearest reason to anchor the merged plan on my §4 label table.
- **Both miss the AIY coverage invariant.** Whichever plan is executed, `mappingCoversEveryBundledKbSpecies` will go red without the dormant-AIY-row step.
- **All three (me included) under-specify the count bumps:** I named `KbContentSpeciesTest`/`KbContentArchetypesTest` but missed `KbLoaderTest`'s size 16/8 assertions; CODEX and GEMINI named none precisely. The merged plan must list all three.

---

## D. If I were merging

**Keep from CODEX (draft A):**
- **Phase 1 "Preflight And Guardrails"** — read-and-capture-counts plus *verify all 16 labels against `labels.csv` exactly*. Make this the literal first task; it's the cheapest defense against the label-truncation bug that A itself fell into.
- **Phase 6 "Review Notes And Evidence"** → write `docs/sprints/evidence/PLANTPOTTING-0009/` (final counts, commands, calibration gap), matching the 0007 convention.
- The **"existing 10 rows still point to the same KB IDs" regression assertion** as an explicit positive test, layered on top of my `git diff` review.
- The **schema-restraint non-goal** (no care-card schema widening unless validation proves it necessary).

**Keep from GEMINI (draft B):**
- The **archetype-grouped reading order** (Aroid / Specialised / Standard) as the *presentation* of the species list — but with explicit, correct `archetypeId` on every line.

**Keep from mine (baseline), non-negotiable:**
- The **§4 full-parenthetical label table** as the source of truth for all 16 map keys (fixes A's and B's fatal truncation).
- **Phase 3 AIY dormant-row maintenance + R1** (the only treatment of `mappingCoversEveryBundledKbSpecies`).
- **Exactly one new archetype** (`carnivorous-peat-sand` for Venus Flytrap); reuse `moisture-retentive` for fern and `standard-houseplant` for palm; **do not** re-declare `succulent-gritty`; **do not** add `fern-moisture-retentive`/`palm-well-draining` — kills B's archetype errors.
- **Maranta → `moisture-retentive`** (overrides B's Standard-bucket mis-assignment).
- The **CI-level Pilea-absence assertion** and the **new house-plant-map coverage/integrity test** (every key ∈ `labels.csv`, ≥26 mapped, no Pilea), since the repo has *no* test over the house-plant map today.

**Fix in all three:** the count-assertion task must name **all three** sites — `KbContentSpeciesTest.bundlesExactlySixteenSpecies` (→32), `KbContentArchetypesTest.bundlesExactlyEightArchetypes` (→9), **and** `KbLoaderTest` (species→32, archetypes→9) — which my own draft under-listed.
