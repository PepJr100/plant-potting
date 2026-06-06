# accuracy-eval summary (raw — polish into accuracy-eval-summary.md)

Production model: `house_plant_species_mobilenetv2`. Perturbation rows are *synthetic-robustness*.

## mode: squash

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 240 | 0.654 | 0.796 | 0.254 | 0.092 | 30 | 103 |
| clean | 20 | 0.750 | 0.850 | 0.100 | 0.150 | 31 | 103 |
| blur | 20 | 0.250 | 0.650 | 0.550 | 0.200 | 35 | 70 |
| brightness | 40 | 0.775 | 0.825 | 0.125 | 0.100 | 27 | 58 |
| contrast | 40 | 0.725 | 0.850 | 0.200 | 0.075 | 32 | 70 |
| crop | 40 | 0.675 | 0.850 | 0.300 | 0.025 | 28 | 79 |
| rotate | 80 | 0.625 | 0.750 | 0.288 | 0.088 | 31 | 91 |
| IN-VOCAB (all) | 240 | 0.654 | 0.796 | 0.254 | 0.092 | 30 | 103 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.750

## mode: center_crop

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 240 | 0.654 | 0.796 | 0.254 | 0.092 | 30 | 58 |
| clean | 20 | 0.750 | 0.850 | 0.100 | 0.150 | 33 | 50 |
| blur | 20 | 0.250 | 0.650 | 0.550 | 0.200 | 32 | 53 |
| brightness | 40 | 0.775 | 0.825 | 0.125 | 0.100 | 30 | 52 |
| contrast | 40 | 0.725 | 0.850 | 0.200 | 0.075 | 31 | 47 |
| crop | 40 | 0.675 | 0.850 | 0.300 | 0.025 | 30 | 57 |
| rotate | 80 | 0.625 | 0.750 | 0.288 | 0.088 | 30 | 58 |
| IN-VOCAB (all) | 240 | 0.654 | 0.796 | 0.254 | 0.092 | 30 | 58 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.750

## mode: tta6

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 240 | 0.667 | 0.808 | 0.113 | 0.221 | 184 | 453 |
| clean | 20 | 0.750 | 0.900 | 0.100 | 0.150 | 196 | 453 |
| blur | 20 | 0.300 | 0.600 | 0.300 | 0.400 | 182 | 341 |
| brightness | 40 | 0.750 | 0.850 | 0.050 | 0.200 | 181 | 279 |
| contrast | 40 | 0.750 | 0.850 | 0.075 | 0.175 | 184 | 279 |
| crop | 40 | 0.650 | 0.800 | 0.075 | 0.275 | 184 | 275 |
| rotate | 80 | 0.663 | 0.800 | 0.138 | 0.200 | 187 | 350 |
| IN-VOCAB (all) | 240 | 0.667 | 0.808 | 0.113 | 0.221 | 184 | 453 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.750

