# Critique of PLANTPOTTING-0009 Drafts

Before reviewing the other drafts, I must honestly assess a major flaw in my own draft (GEMINI): I incorrectly proposed creating `fern-moisture-retentive` and `palm-well-draining` as new archetypes, which are redundant with the existing `moisture-retentive` and `standard-houseplant` archetypes. I also listed `succulent-gritty` as a new archetype, failing to recognize it is already one of the 8 existing archetypes.

Here is the critique of the Codex and Claude drafts.

## 1. CODEX Draft

**What is stronger than mine:**
*   **Archetype accuracy:** Codex correctly identifies `succulent-gritty`, `aroid-chunky`, `moisture-retentive`, and `standard-houseplant` as existing archetypes to reuse, avoiding my mistake of inventing redundant ones.
*   **Mapping nuance:** It introduces `alias:true` and `_note` fields for coarse genus mappings in the class map ("Phase 4 - Model Class Map"), showing a better understanding of the mapping schema's handling of generic labels.

**What is weaker than mine:**
*   **Schema precision:** When appending species entries, Codex just vaguely lists "`mapping.kind`" as a required field in "Phase 3 - Species Entries" without specifying the value (e.g., `single` vs `blend`).

**What tasks are missing:**
*   **AIY Map Maintenance:** It completely misses the task to update the dormant rows in `aiy_plants_v1/plant_class_map.json`. Adding new KB species will break the AIY coverage tests.
*   **Utilizing `blend`:** It misses the task to assign `mapping.kind='blend'` for coarse/genus entries.

**What risks are underweighted:**
*   **Cross-model test breakage:** By missing the AIY map maintenance, Codex severely underweights the risk of breaking existing test invariants (`ModelLabelMappingValidationTest`) outside of the immediate MobileNetV2 scope.

**What sequencing is wrong:**
*   Codex's "Phase 5 - Validation And Tests" adds validation assertions *after* the mapping tasks (Phase 4). However, it fails to sequence an early investigation of cross-model dependencies (like the AIY map), meaning the build will be broken between phases. The preflight (Phase 1) fails to check existing test assertions for KB species counts.

## 2. CLAUDE Draft

**What is stronger than mine:**
*   **Systemic test awareness:** Claude brilliantly identifies the cross-model test breakage in "Phase 3 — AIY map coverage-invariant maintenance" (`ModelLabelMappingValidationTest.mappingCoversEveryBundledKbSpecies`).
*   **Precise test updates:** It explicitly calls out exactly which tests need updating and how, such as renaming `KbContentSpeciesTest.bundlesExactlySixteenSpecies()` to assert size 32.
*   **Strict archetype reuse:** It accurately rejects creating fern/palm archetypes, confirming `moisture-retentive` and `standard-houseplant` are the correct botanical fits.

**What is weaker than mine:**
*   **Schema rigidity:** Claude rigidly forces `mapping{kind:"single"}` for all 16 new species in "Phase 1 — species.json entries".

**What tasks are missing:**
*   **Applying `mapping.kind='blend'`:** Because it forces `kind:"single"`, Claude completely misses the task to use `mapping.kind='blend'` for coarse genus entries (e.g., *Dracaena*, *Tradescantia*, *Schefflera*).

**What risks are underweighted:**
*   **Schema misrepresentation:** By forcing `kind:"single"` on coarse mappings, it underweights the risk of misrepresenting taxonomic precision within the KB schema.

**What sequencing is wrong:**
*   Placing "Phase 5 — Toxicity & special-case vet" *after* "Phase 1 — species.json entries" and "Phase 4 — Tests" is logically backward. Vetting horticultural facts and toxicity warnings should be a prerequisite *before* writing the JSON entries, not a post-implementation gate.

## 3. Merging Recommendation

If I were merging these into a final plan, I'd keep:
*   **From CLAUDE:** Phase 3 (AIY map dormant rows) and Phase 4 (precise test count updates like `bundlesExactlyThirtyTwoSpecies`).
*   **From CODEX:** Phase 4's `alias:true` class map row approach.
*   **Combined Adjustment:** I would update the species entry tasks to explicitly use `mapping.kind='blend'` for coarse model labels, and sequence the horticultural vet *before* authoring the JSON entries.
