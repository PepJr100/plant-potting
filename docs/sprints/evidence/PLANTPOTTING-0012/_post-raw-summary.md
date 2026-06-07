# accuracy-eval summary (raw — polish into accuracy-eval-summary.md)

Production model: `house_plant_species_mobilenetv2`. Perturbation rows are *synthetic-robustness*.

## mode: squash

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 720 | 0.415 | 0.699 | 0.369 | 0.215 | 24 | 184 |
| clean | 60 | 0.450 | 0.717 | 0.300 | 0.250 | 26 | 97 |
| blur | 60 | 0.217 | 0.567 | 0.567 | 0.217 | 22 | 70 |
| brightness | 120 | 0.450 | 0.700 | 0.317 | 0.233 | 27 | 73 |
| contrast | 120 | 0.433 | 0.717 | 0.333 | 0.233 | 24 | 91 |
| crop | 120 | 0.442 | 0.733 | 0.408 | 0.150 | 23 | 83 |
| rotate | 240 | 0.417 | 0.700 | 0.363 | 0.221 | 25 | 184 |
| IN-VOCAB (all) | 720 | 0.415 | 0.699 | 0.369 | 0.215 | 24 | 184 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.450

## mode: center_crop

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 720 | 0.415 | 0.699 | 0.369 | 0.215 | 24 | 106 |
| clean | 60 | 0.450 | 0.717 | 0.300 | 0.250 | 26 | 68 |
| blur | 60 | 0.217 | 0.567 | 0.567 | 0.217 | 23 | 67 |
| brightness | 120 | 0.450 | 0.700 | 0.317 | 0.233 | 25 | 79 |
| contrast | 120 | 0.433 | 0.717 | 0.333 | 0.233 | 23 | 74 |
| crop | 120 | 0.442 | 0.733 | 0.408 | 0.150 | 24 | 79 |
| rotate | 240 | 0.417 | 0.700 | 0.363 | 0.221 | 24 | 106 |
| IN-VOCAB (all) | 720 | 0.415 | 0.699 | 0.369 | 0.215 | 24 | 106 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.450

## mode: tta6

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 720 | 0.404 | 0.732 | 0.213 | 0.383 | 152 | 491 |
| clean | 60 | 0.417 | 0.783 | 0.117 | 0.467 | 143 | 354 |
| blur | 60 | 0.233 | 0.550 | 0.367 | 0.400 | 157 | 340 |
| brightness | 120 | 0.425 | 0.767 | 0.167 | 0.408 | 161 | 327 |
| contrast | 120 | 0.417 | 0.750 | 0.167 | 0.417 | 150 | 465 |
| crop | 120 | 0.408 | 0.708 | 0.250 | 0.342 | 152 | 417 |
| rotate | 240 | 0.425 | 0.750 | 0.225 | 0.350 | 153 | 491 |
| IN-VOCAB (all) | 720 | 0.404 | 0.732 | 0.213 | 0.383 | 152 | 491 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.417

