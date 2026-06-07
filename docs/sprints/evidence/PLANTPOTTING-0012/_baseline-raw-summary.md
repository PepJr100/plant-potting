# accuracy-eval summary (raw — polish into accuracy-eval-summary.md)

Production model: `house_plant_species_mobilenetv2`. Perturbation rows are *synthetic-robustness*.

## mode: squash

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 720 | 0.415 | 0.600 | 0.369 | 0.215 | 20 | 77 |
| clean | 60 | 0.450 | 0.617 | 0.300 | 0.250 | 20 | 43 |
| blur | 60 | 0.217 | 0.467 | 0.567 | 0.217 | 19 | 77 |
| brightness | 120 | 0.450 | 0.608 | 0.317 | 0.233 | 20 | 53 |
| contrast | 120 | 0.433 | 0.617 | 0.333 | 0.233 | 20 | 65 |
| crop | 120 | 0.442 | 0.633 | 0.408 | 0.150 | 20 | 55 |
| rotate | 240 | 0.417 | 0.600 | 0.363 | 0.221 | 20 | 52 |
| IN-VOCAB (all) | 648 | 0.461 | 0.667 | 0.407 | 0.131 | 20 | 65 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.450

## mode: center_crop

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 720 | 0.415 | 0.600 | 0.369 | 0.215 | 19 | 71 |
| clean | 60 | 0.450 | 0.617 | 0.300 | 0.250 | 24 | 42 |
| blur | 60 | 0.217 | 0.467 | 0.567 | 0.217 | 20 | 57 |
| brightness | 120 | 0.450 | 0.608 | 0.317 | 0.233 | 19 | 71 |
| contrast | 120 | 0.433 | 0.617 | 0.333 | 0.233 | 19 | 53 |
| crop | 120 | 0.442 | 0.633 | 0.408 | 0.150 | 19 | 63 |
| rotate | 240 | 0.417 | 0.600 | 0.363 | 0.221 | 19 | 63 |
| IN-VOCAB (all) | 648 | 0.461 | 0.667 | 0.407 | 0.131 | 19 | 71 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.450

## mode: tta6

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 720 | 0.404 | 0.635 | 0.213 | 0.383 | 120 | 268 |
| clean | 60 | 0.417 | 0.700 | 0.117 | 0.467 | 119 | 245 |
| blur | 60 | 0.233 | 0.450 | 0.367 | 0.400 | 121 | 268 |
| brightness | 120 | 0.425 | 0.667 | 0.167 | 0.408 | 124 | 254 |
| contrast | 120 | 0.417 | 0.658 | 0.167 | 0.417 | 121 | 249 |
| crop | 120 | 0.408 | 0.608 | 0.250 | 0.342 | 119 | 243 |
| rotate | 240 | 0.425 | 0.650 | 0.225 | 0.350 | 118 | 253 |
| IN-VOCAB (all) | 648 | 0.449 | 0.705 | 0.236 | 0.315 | 119 | 268 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.417

