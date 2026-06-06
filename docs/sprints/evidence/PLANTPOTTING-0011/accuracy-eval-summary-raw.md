# accuracy-eval summary (raw — polish into accuracy-eval-summary.md)

Production model: `house_plant_species_mobilenetv2`. Perturbation rows are *synthetic-robustness*.

## mode: squash

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 492 | 0.528 | 0.713 | 0.382 | 0.089 | 20 | 83 |
| clean | 41 | 0.610 | 0.756 | 0.268 | 0.122 | 21 | 58 |
| blur | 41 | 0.244 | 0.561 | 0.610 | 0.146 | 19 | 43 |
| brightness | 82 | 0.573 | 0.732 | 0.341 | 0.085 | 21 | 78 |
| contrast | 82 | 0.549 | 0.756 | 0.354 | 0.098 | 21 | 68 |
| crop | 82 | 0.549 | 0.720 | 0.402 | 0.049 | 21 | 83 |
| rotate | 164 | 0.537 | 0.707 | 0.378 | 0.085 | 20 | 83 |
| IN-VOCAB (all) | 492 | 0.528 | 0.713 | 0.382 | 0.089 | 20 | 83 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.610

## mode: center_crop

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 492 | 0.528 | 0.713 | 0.382 | 0.089 | 20 | 189 |
| clean | 41 | 0.610 | 0.756 | 0.268 | 0.122 | 23 | 51 |
| blur | 41 | 0.244 | 0.561 | 0.610 | 0.146 | 19 | 77 |
| brightness | 82 | 0.573 | 0.732 | 0.341 | 0.085 | 21 | 121 |
| contrast | 82 | 0.549 | 0.756 | 0.354 | 0.098 | 21 | 56 |
| crop | 82 | 0.549 | 0.720 | 0.402 | 0.049 | 20 | 189 |
| rotate | 164 | 0.537 | 0.707 | 0.378 | 0.085 | 20 | 134 |
| IN-VOCAB (all) | 492 | 0.528 | 0.713 | 0.382 | 0.089 | 20 | 189 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.610

## mode: tta6

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 492 | 0.530 | 0.758 | 0.226 | 0.244 | 125 | 539 |
| clean | 41 | 0.561 | 0.829 | 0.171 | 0.268 | 125 | 273 |
| blur | 41 | 0.244 | 0.512 | 0.488 | 0.268 | 125 | 509 |
| brightness | 82 | 0.549 | 0.805 | 0.171 | 0.280 | 126 | 314 |
| contrast | 82 | 0.585 | 0.780 | 0.159 | 0.256 | 126 | 336 |
| crop | 82 | 0.524 | 0.744 | 0.232 | 0.244 | 126 | 356 |
| rotate | 164 | 0.561 | 0.774 | 0.232 | 0.207 | 123 | 539 |
| IN-VOCAB (all) | 492 | 0.530 | 0.758 | 0.226 | 0.244 | 125 | 539 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.561

