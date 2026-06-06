# accuracy-eval summary (raw — polish into accuracy-eval-summary.md)

Production model: `house_plant_species_mobilenetv2`. Perturbation rows are *synthetic-robustness*.

## mode: squash

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 240 | 0.654 | 0.796 | 0.254 | 0.092 | 32 | 170 |
| clean | 20 | 0.750 | 0.850 | 0.100 | 0.150 | 40 | 99 |
| blur | 20 | 0.250 | 0.650 | 0.550 | 0.200 | 37 | 79 |
| brightness | 40 | 0.775 | 0.825 | 0.125 | 0.100 | 32 | 101 |
| contrast | 40 | 0.725 | 0.850 | 0.200 | 0.075 | 32 | 130 |
| crop | 40 | 0.675 | 0.850 | 0.300 | 0.025 | 34 | 104 |
| rotate | 80 | 0.625 | 0.750 | 0.288 | 0.088 | 32 | 170 |
| IN-VOCAB (all) | 240 | 0.654 | 0.796 | 0.254 | 0.092 | 32 | 170 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.750

## mode: center_crop

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 240 | 0.654 | 0.796 | 0.254 | 0.092 | 34 | 109 |
| clean | 20 | 0.750 | 0.850 | 0.100 | 0.150 | 38 | 64 |
| blur | 20 | 0.250 | 0.650 | 0.550 | 0.200 | 38 | 104 |
| brightness | 40 | 0.775 | 0.825 | 0.125 | 0.100 | 35 | 106 |
| contrast | 40 | 0.725 | 0.850 | 0.200 | 0.075 | 31 | 84 |
| crop | 40 | 0.675 | 0.850 | 0.300 | 0.025 | 34 | 109 |
| rotate | 80 | 0.625 | 0.750 | 0.288 | 0.088 | 34 | 107 |
| IN-VOCAB (all) | 240 | 0.654 | 0.796 | 0.254 | 0.092 | 34 | 109 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.750

## mode: tta5

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 240 | 0.671 | 0.808 | 0.121 | 0.208 | 168 | 1275 |
| clean | 20 | 0.750 | 0.950 | 0.100 | 0.150 | 175 | 331 |
| blur | 20 | 0.300 | 0.600 | 0.300 | 0.400 | 168 | 517 |
| brightness | 40 | 0.750 | 0.850 | 0.050 | 0.200 | 147 | 312 |
| contrast | 40 | 0.700 | 0.825 | 0.075 | 0.225 | 161 | 792 |
| crop | 40 | 0.650 | 0.800 | 0.125 | 0.225 | 175 | 367 |
| rotate | 80 | 0.700 | 0.800 | 0.138 | 0.163 | 170 | 1275 |
| IN-VOCAB (all) | 240 | 0.671 | 0.808 | 0.121 | 0.208 | 168 | 1275 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.750

