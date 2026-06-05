# PLANTPOTTING-0009 — evidence note (text-only KB-expansion)

**Sprint type:** content / config expansion. **NO** ML, **NO** fine-tune, **NO** imagery, **NO**
real-photo probing. Implemented by `opus (claude-opus-4-8)` in-session, 2026-06-05.

## What changed (final counts)

| Asset | Before | After |
|---|---|---|
| `app/src/main/assets/kb/species.json` | 16 entries | **32** entries (+16 delta species, append-only) |
| `app/src/main/assets/kb/archetypes.json` | 8 archetypes | **9** (+`carnivorous-peat-sand`) |
| `house_plant_species_mobilenetv2/plant_class_map.json` | 10 mapped rows | **26** mapped rows (of 47 model classes) |
| `aiy_plants_v1/plant_class_map.json` | 18 rows / 16 KB species | **34** rows / 32 KB species (+16 dormant rows) |

The 16 delta species (model label → KB id → archetype) are tabulated in
`docs/kb/ml-mapping-notes.md` under the PLANTPOTTING-0009 section, copied verbatim from
`house_plant_species_mobilenetv2/labels.csv`.

## New archetype

`carnivorous-peat-sand` ("Carnivorous Bog (Peat/Sand)") — 50% sphagnum peat moss / 50% lime-free
horticultural silica sand or perlite (sums to 100). **Zero fertiliser, zero lime/dolomite,
distilled/RO/rainwater only**, winter-dormancy note. Sole consumer: `dionaea-muscipula` (Venus
Flytrap). No `fern-*`/`palm-*` archetype added; `succulent-gritty` not re-declared.

## Tests added / changed

- `KbContentSpeciesTest`: `bundlesExactlySixteenSpecies` → `bundlesExactlyThirtyTwoSpecies` (32);
  added `dionaeaMuscipulaMapsToCarnivorousPeatSand`.
- `KbContentArchetypesTest`: `bundlesExactlyEightArchetypes` → `bundlesExactlyNineArchetypes` (9);
  added `carnivorousPeatSandArchetypeIsPresent`.
- `KbLoaderTest`: size assertions bumped to 9 archetypes / 32 species (method renamed).
- **New** `HousePlantClassMapValidationTest` (none existed before): asserts labels-asset path,
  every `kbSpeciesId` resolves, **every key is a verbatim `labels.csv` line**, exactly **26** mapped
  classes, each of the 16 new labels resolves to its expected KB id, the existing 10 rows are
  unchanged, and **Pilea is NOT mapped**. Deliberately omits the AIY alias-counterpart rule (the
  house-plant map carries coarse alias rows without non-alias counterparts by design).
- `RecommendationGoldenTest` iterates `kb.species` dynamically (no count to bump) and the archetype
  test uses a synthetic in-memory KB — no golden edits required.

## Commands run

```
# JSON parse + invariant validation (sandbox python — pre-test gate, Phase 8)
python3 -c "<parse species.json / archetypes.json; id+alias uniqueness; recipe sums==100>"
python3 -c "<house-plant map: 26 keys, all ∈ labels.csv, Pilea absent, kbIds resolve, existing 10 intact>"
python3 -c "<AIY map: 34 rows, kbIds−mapIds==∅, only 2 original alias rows, no dup normalised keys>"
python3 -c "<golden-style check for 16 new species: no forbidden tokens, scientificName present, sum==100>"
```

The full JVM unit suite / `verifyNoNetworking` / `scripts/check-stub-isolation.sh` results are
recorded in this sprint's execution PR (the Android/Gradle toolchain is not available inside the
agent sandbox; see the Gradle note below).

## Explicit record — coverage is editorial, NOT calibrated

The 16 new mappings are **model-vocabulary / editorial coverage only**. They have **not** been
validated against real first-party photos (unlike the PLANTPOTTING-0006/0007 in-vocab probes for
Monstera and Crassula). Per-class confidence behaviour is assumed-from-routing, not measured. This
**unprobed-calibration gap is a known, recorded gap — not closed this sprint** (see ROADMAP /
ml-mapping-notes §PLANTPOTTING-0009). Closing it requires imagery/probing work that is out of scope
here.

## Other recorded gaps (not closed)

- **Pilea deferred** — ships later bundled with the pothos↔Pilea boundary fix; CI-enforced unmapped.
- **Coarse/genus rows** (Aglaonema, Alocasia, Dieffenbachia, Dracaena, Tradescantia, Schefflera,
  Kalanchoe) — sub-species substrate divergence within those genera is not modelled.
- **AIY dormant rows** preserve the coverage invariant but overstate AIY's real capability.
