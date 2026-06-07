# PLANTPOTTING-0012 — fixture sourcing log (Phase 1)

Auditable record of every search, source, license, and accept/reject decision for the
pothos↔Pilea boundary fixture expansion. Methodology: queried the **iNaturalist v1 API**
(`/observations?taxon_name=…&photo_license=…`) for license-clean photos, downloaded the
**original**-size JPEG, **visually vetted each candidate** (species correctness + houseplant
context), then centre-cropped to square / 480×480 / JPEG q80 to match the existing fixtures.
Helper scripts: `stage_inat.py` (download), `promote.py` (promote + manifest rows).

## License policy (test-enforced — stricter than the plan prose)

`FixtureLicenseManifestTest.onlyTheKnownFourFixturesMayBeNonCc0` enforces **CC0 / public-domain
ONLY** for every non-grandfathered fixture; a CC-BY fixture would turn the suite red. So although
the plan text mentioned CC-BY, **every new fixture here is CC0**, and CC-BY candidates were rejected
on that basis. (CC-BY remains allowed only for *reference/hero* images, a different policy.)

## Inventory (baseline, before this sprint)

- Mapped model classes → KB species: **38**.
- `identify-fixtures/` covered **30** species (41 JPEGs); `epipremnum-aureum` had **2**;
  `pilea-peperomioides` confirmed **absent** (0).
- **8 mapped species with zero fixture coverage** (the "untested" set): `alocasia`,
  `beaucarnea-recurvata`, `begonia`, `chamaedorea-elegans`, `ctenanthe`, `dracaena`,
  `saintpaulia-ionantha`, `schlumbergera-bridgesii`.

## Why Pilea is "casual" grade on iNaturalist

`Pilea peperomioides` is almost always an indoor cultivated houseplant → iNaturalist flags such
observations **captive/cultivated = "casual"** grade, so a `quality_grade=research` filter returns
**0**. Dropping that filter (keeping `photo_license=cc0`) surfaces the real supply. Pothos
naturalises outdoors in the tropics, so it has abundant research-grade CC0 photos.

## Pilea peperomioides — 12 candidates staged, 6 promoted

Search: `taxon_name=Pilea peperomioides`, `photo_license=cc0` (10 results) + `cc-by` (19),
one photo per distinct author for independence.

| cand | author | license | verdict | reason |
|---|---|---|---|---|
| cand01 | dinomariobob | CC0 | **PROMOTED → __01** | textbook peltate leaves, windowsill |
| cand02 | Tiago Lubiana | CC0 | **PROMOTED → __02** | top-down indoor, healthy |
| cand03 | Mehmet Baran | CC0 | not promoted | species-correct (leggy/etiolated); REDUNDANT vs cand08 |
| cand04 | inatnutcrm | CC0 | **REJECT** | atypical reddish-stressed leaves on mouldy perlite; unrepresentative |
| cand05 | Olsza Borys | CC0 | **PROMOTED → __03** | textbook specimen, green pot, plain ground |
| cand06 | dbgi | CC0 | not promoted | species-correct (in-ground outdoor); REDUNDANT |
| cand07 | Daniel Atha | CC0 | **PROMOTED → __04** | classic, white pot on wood |
| cand08 | Ludvig Hafström | CC0 | not promoted | species-correct (leggy, terracotta); REDUNDANT |
| cand09 | dmagdee | CC0 | **PROMOTED → __05** | top-down, outdoor blue table (angle variety) |
| cand10 | Curran Dwyer | CC0 | **PROMOTED → __06** | blue pot, windowsill |
| cand11 | petitcrabe | CC-BY | **REJECT** | CC-BY (fixtures CC0-only) + cluttered multi-species, sickly |
| cand12 | Margaret Forrest | CC-BY | **REJECT** | CC-BY (fixtures CC0-only) |

**Promoted: 6 independent CC0 Pilea base photos** (distinct authors; windowsill / top-down /
studio / outdoor / leggy and compact habits represented).

### ★ Pilea verdict (drives Phases 4–5)

**≥ 3 independent license-clean Pilea fixtures sourced (6).** → Pilea **earns the right to be
evaluated for a direct card** (still gate-governed + no-regression governed; a *direct* Pilea card
additionally requires author-separated / held-out evaluation per the plan). Pilea is **not**
blocked and is **not** strict-picker-only on supply grounds. `pilea-blocker.md` is therefore **not**
written.

## Epipremnum aureum (pothos) — 12 candidates staged, 5 promoted (now 7 total with the 2 existing)

Search: `taxon_name=Epipremnum aureum`, `photo_license=cc0`, `quality_grade=research` (191 results).

| cand | author | verdict | reason |
|---|---|---|---|
| cand01 | Bruno Senterre | **REJECT** | cluttered multi-species forest floor; pothos not dominant |
| cand02 | natalie | **PROMOTED → __02** | green form, climbing a trunk |
| cand03 | Carter Dorscht | **PROMOTED → __03** | golden-variegated, climbing (variegation coverage) |
| cand04 | Al Kordesch | **PROMOTED → __04** | mature **fenestrated** + variegated (monstera-confusion coverage) |
| cand06 | 葉子 | **REJECT** | pothos mass in jungle; cluttered, not dominant |
| cand08 | Ben Keen | **PROMOTED → __05** | green/gold-flecked foliage close-up |
| cand12 | Stuart | **PROMOTED → __06** | trailing heart-leaf single vine (juvenile leaf shape) |
| cand05,07,09,10,11 | (various) | NOT NEEDED | target met with 5 diverse accepts; not vetted/promoted |

**Promoted: 5 new CC0 pothos base photos** → **7 pothos fixtures total** (≥ 6 target met), spanning
juvenile heart-leaf, mature fenestrated, green and golden-variegated, foliage close-up, climbing and
trailing habits.

## 8 untested mapped species — reason-coded outcome table

One license-clean CC0 base photo sourced and visually vetted for **every** untested species
(secondary priority). **None remain uncovered.**

| species (KB id) | search taxon | source author | result |
|---|---|---|---|
| saintpaulia-ionantha | Saintpaulia ionantha (→ *Streptocarpus ionanthus*) | Agnes Trekker | **SOURCED (1)** — bloom + fuzzy leaves |
| chamaedorea-elegans | Chamaedorea elegans | Diego Blanco | **SOURCED (1)** — parlor-palm frond |
| beaucarnea-recurvata | Beaucarnea recurvata | Jenny Christianson | **SOURCED (1)** — swollen base + strappy leaves |
| alocasia | Alocasia (genus, coarse map) | Richard Fuller | **SOURCED (1)** — large arrow-shaped leaves |
| dracaena | Dracaena fragrans (coarse map) | 葉子 | **SOURCED (1)** — corn-plant rosette |
| begonia | Begonia (genus, coarse map) | Alfredo F. Fuentes Claros | **SOURCED (1)** — asymmetric leaf + flower |
| ctenanthe | Ctenanthe (genus, coarse map) | Alex Fernando | **SOURCED (1)** — herringbone leaf |
| schlumbergera-bridgesii | Schlumbergera (coarse map) | Agnes Trekker | **SOURCED (1)** — magenta bloom + cladodes |

Coarse genus mappings (`alocasia`, `dracaena`, `begonia`, `ctenanthe`, `schlumbergera-bridgesii`)
accept any in-genus species, consistent with the class-map's coarse-row policy.

## Totals

- **+19 fixtures** (6 Pilea NEW species + 5 pothos + 8 untested), all CC0, all 480×480 q80.
- `identify-fixtures/` now holds **60 JPEGs** across **39 species** (30 prior + Pilea + 8 untested).
- Pilea KB entry (`pilea-peperomioides`) added to `species.json` (count 44 → 45) so the new fixtures
  resolve in `FixtureIntegrityTest`. **Pilea is NOT yet mapped** — `plant_class_map.json` is
  unchanged and `HousePlantClassMapValidationTest.pileaIsNotMapped` still passes. The mapping + gate
  land atomically in Phases 4–5 (no tree maps Pilea without the gate).

## Tests run (green)

`FixtureLicenseManifestTest`, `FixtureIntegrityTest`, `KbContentSpeciesTest`, `KbLoaderTest`,
`HousePlantClassMapValidationTest` — all pass.
