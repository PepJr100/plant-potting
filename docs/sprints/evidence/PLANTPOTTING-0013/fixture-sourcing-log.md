# PLANTPOTTING-0013 Thread B — fixture sourcing log (CC0/PD only)

Goal: grow the honest real-world sample count for the **8 single-photo mapped species** (each had exactly
one `__01.jpg` from 0012). **Breadth over depth** — a 2nd photo across as many of the 8 as license-clean
supply allows. **No Pilea fixtures added** (non-goal — we do not source Pilea to flatter its own gate).

Method: `stage_inat.py` downloads CC0 candidates from the iNaturalist API (research-grade first; widened
to casual-grade for houseplant species that skew casual), into `_staging/` (gitignored). Every candidate
is then **visually vetted** (opened and inspected) for species-correctness, houseplant context, and image
quality before promotion. Accepted images are center-cropped to 480×480 (the existing fixture convention)
and written as `<id>__NN.jpg`; a `fixture-manifest.tsv` row + `LICENSE.txt` entry are added per file.

## Accepted (6 — all CC0, iNaturalist)

| fixture | taxon | author | obs | why accepted |
|---|---|---|---|---|
| `chamaedorea-elegans__02` | Chamaedorea elegans | Chris Lee | 369423355 | potted parlor palm indoors by a window; ideal houseplant context |
| `ctenanthe__02` | Ctenanthe oppenheimiana | Prince Molokomme | 199462306 | whole plant, diagnostic silver-feathered leaves + purple undersides |
| `dracaena__02` | Dracaena fragrans | Matt Schultz | 20310996 | corn-plant (the common houseplant Dracaena), whole plant, diagnostic caned foliage |
| `saintpaulia-ionantha__02` | (Streptocarpus) ionanthus | adamcvean | 368363861 | potted African violet indoors, well-exposed, blue-purple bloom |
| `beaucarnea-recurvata__02` | Beaucarnea recurvata | Christina Hewitt | 100983498 | diagnostic swollen caudex + cascading foliage, whole plant |
| `alocasia__02` | Alocasia odora | funnieanimals | 324134389 | elephant-ear, whole plant, diagnostic large sagittate leaves |

## Rejected near-misses (reason-coded)

| candidate | taxon | reason code |
|---|---|---|
| chamaedorea-elegans cand02/07 (Matt Schultz, obs 36465654) | Chamaedorea elegans | `WATERMARK` (red date-stamp overlay) + `FIELD-CONTEXT` + `LOW-RES` (147 KB forest-floor shot) |
| saintpaulia cand03 (Tim Dick, obs 110593298) | ionanthus | `PHOTO-OF-SCREEN` (mouse cursor + UI chrome visible — a monitor photo, not a direct photograph) |
| saintpaulia cand01 (dinomariobob, obs 305259161) | ionanthus | `OVEREXPOSED` (heavily blown-out backlit window; cand04 chosen instead) |
| alocasia cand05 (Frank Thomas Sautter, obs 236790885) | A. cucullata | `FLOWER-DOMINANT` (spadix close-up, not whole-plant foliage; cand06 chosen instead) |
| dracaena cand03/04/06 | Cordyline fruticosa / C. australis | `WRONG-GENUS` (Cordyline, not Dracaena) |
| dracaena cand09 (Isaiah Findley, obs 223497256) | Sansevieria trifasciata | `DISTINCT-KB-SPECIES` (that is `dracaena-trifasciata`, a separate mapped species — would mislabel) |
| begonia cand01–10 (all batches, research + casual) | wild Begonia spp. (weddelliana, wollnyi, nana, micranthera, pululahuana, grisea, rieckei, picta, leathermaniae, stigmosa) | `FIELD-CONTEXT` / `FLOWER-DOMINANT` — wild botanical observations, not houseplant-context (rex/wax) begonia |
| schlumbergera cand01–05 (incl. `Schlumbergera bridgesii` query) | Schlumbergera **truncata** | `SPECIES-MISMATCH` — iNat resolves `bridgesii` → `truncata`; the KB row is `schlumbergera-bridgesii` (a distinct species). No CC0 bridgesii-specific photo found |

## Reason-coded coverage table (auditable "sourced as many as possible")

| species | start | added | end | supply ceiling / note |
|---|---|---|---|---|
| chamaedorea-elegans | 1 | +1 | **2** | — (1 strong casual-grade indoor candidate; the rest field/watermarked) |
| ctenanthe | 1 | +1 | **2** | — (2 CC0 candidates total; best whole-plant chosen) |
| dracaena | 1 | +1 | **2** | genus-coarse; most CC0 are Cordyline (wrong genus), Sansevieria (distinct KB species), or outdoor *draco* dragon-trees. 1 fragrans houseplant accepted |
| saintpaulia-ionantha | 1 | +1 | **2** | casual-grade only (no research-grade CC0); several rejected (screen-photo, overexposed) |
| beaucarnea-recurvata | 1 | +1 | **2** | casual-grade; ample CC0 supply, 1 clean whole-plant accepted |
| alocasia | 1 | +1 | **2** | genus-coarse; ample CC0 *Alocasia* spp., 1 whole-plant *odora* accepted |
| begonia | 1 | **+0** | **1** | **SUPPLY CEILING** — all CC0 Begonia (research + casual) are wild field species (flower-dominant, in-habitat), none houseplant-context (rex/wax). None met the houseplant-context bar |
| schlumbergera-bridgesii | 1 | **+0** | **1** | **SUPPLY CEILING** — the only CC0 Schlumbergera are *S. truncata* (a distinct species; iNat treats `bridgesii` as a synonym resolving to truncata). Rejected on species-mismatch discipline |

**Result: 6 of 8 species deepened (+6 fixtures); 2 hit documented license-clean supply ceilings.**
Fixture total **60 → 66**. Pilea unchanged at 6 (deliberate). All new fixtures are CC0 real photographs,
center-cropped to 480×480, attributed in `fixture-manifest.tsv` + `LICENSE.txt`.
