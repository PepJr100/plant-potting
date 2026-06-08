# PLANTPOTTING-0013 — expanded scorecard (refreshed honest headline)

**Set:** 66 CC0/PD fixtures / 39 species (was 60 in 0012; Thread B added +6 second-photos across 6 of
the 8 single-photo species — Begonia + Schlumbergera supply-ceilinged). **Config:** shipped direct-card
manifest (T_pilea=0.98). **Pipeline:** the shipped row is **tta6**; squash/center_crop are non-shipped
0011 diagnostics. Per-row CSV: expanded-scorecard.csv. Per-species counts + supply ceilings:
fixture-sourcing-log.md.

## Refreshed honest headline (tta6 = shipped pipeline)

| metric | 0012 baseline (strict-picker, 60 fixtures) | 0013 (direct-card, 66 fixtures) |
|---|---|---|
| per-base-image clean top-1 (high-conf correct) | 0.417 (25/60) | **0.530 (35/66)** |
| clean confident-wrong | 0.117 (7/60) | **0.121 (8/66)** |
| all-perturbation confident-wrong | 0.221 (146/660) | **0.210 (158/726)** |
| clean top-3 | — | 0.803 |

**Per-base clean top-1 rose 0.417 → 0.530**, driven mainly by Thread A: the 6 real-Pilea fixtures flip
from picker (not counted correct) to a **correct high-conf direct card**, plus 4 of the 6 new Thread B
second-photos land high-conf-correct. Confident-wrong is **flat** (clean 0.117→0.121, perturbation
0.221→0.210) — the new second-photos are honest, often-harder real-world shots of already-covered
species, so they neither flatter nor wreck the headline. This is honest measurement of a slightly larger
clean set, **not** deep per-species calibration: 31 of 39 species still rest on 1–2 photos (per-species
counts in fixture-sourcing-log.md). The Pilea direct card is **confident-wrong-neutral** on the shipped
pipeline (gate-isolated Δ+0; see direct-card-gating-decision.md).

---

# accuracy-eval summary (raw — polish into accuracy-eval-summary.md)

Production model: `house_plant_species_mobilenetv2`. Perturbation rows are *synthetic-robustness*.

## mode: squash

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 792 | 0.508 | 0.705 | 0.383 | 0.110 | 34 | 158 |
| clean | 66 | 0.545 | 0.727 | 0.303 | 0.152 | 38 | 78 |
| blur | 66 | 0.303 | 0.545 | 0.591 | 0.106 | 32 | 134 |
| brightness | 132 | 0.553 | 0.712 | 0.318 | 0.129 | 35 | 101 |
| contrast | 132 | 0.523 | 0.735 | 0.356 | 0.121 | 34 | 158 |
| crop | 132 | 0.545 | 0.742 | 0.409 | 0.045 | 33 | 119 |
| rotate | 264 | 0.500 | 0.701 | 0.383 | 0.117 | 34 | 133 |
| IN-VOCAB (all) | 792 | 0.508 | 0.705 | 0.383 | 0.110 | 34 | 158 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.545

## mode: center_crop

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 792 | 0.508 | 0.705 | 0.383 | 0.110 | 34 | 150 |
| clean | 66 | 0.545 | 0.727 | 0.303 | 0.152 | 38 | 95 |
| blur | 66 | 0.303 | 0.545 | 0.591 | 0.106 | 33 | 95 |
| brightness | 132 | 0.553 | 0.712 | 0.318 | 0.129 | 34 | 77 |
| contrast | 132 | 0.523 | 0.735 | 0.356 | 0.121 | 34 | 82 |
| crop | 132 | 0.545 | 0.742 | 0.409 | 0.045 | 33 | 98 |
| rotate | 264 | 0.500 | 0.701 | 0.383 | 0.117 | 33 | 150 |
| IN-VOCAB (all) | 792 | 0.508 | 0.705 | 0.383 | 0.110 | 34 | 150 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.545

## mode: tta6

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 792 | 0.486 | 0.740 | 0.210 | 0.304 | 217 | 869 |
| clean | 66 | 0.530 | 0.803 | 0.121 | 0.348 | 218 | 482 |
| blur | 66 | 0.318 | 0.530 | 0.364 | 0.318 | 224 | 640 |
| brightness | 132 | 0.523 | 0.780 | 0.167 | 0.311 | 218 | 497 |
| contrast | 132 | 0.492 | 0.773 | 0.167 | 0.341 | 223 | 698 |
| crop | 132 | 0.485 | 0.727 | 0.258 | 0.258 | 211 | 869 |
| rotate | 264 | 0.496 | 0.746 | 0.212 | 0.292 | 212 | 861 |
| IN-VOCAB (all) | 792 | 0.486 | 0.740 | 0.210 | 0.304 | 217 | 869 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.530

