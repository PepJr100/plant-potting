# accuracy-eval summary (raw — polish into accuracy-eval-summary.md)

Production model: `house_plant_species_mobilenetv2`. Perturbation rows are *synthetic-robustness*.

## mode: squash

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 300 | 0.560 | 0.733 | 0.357 | 0.083 | 23 | 64 |
| clean | 25 | 0.640 | 0.800 | 0.240 | 0.120 | 22 | 51 |
| blur | 25 | 0.200 | 0.560 | 0.640 | 0.160 | 24 | 32 |
| brightness | 50 | 0.640 | 0.780 | 0.260 | 0.100 | 26 | 52 |
| contrast | 50 | 0.620 | 0.800 | 0.300 | 0.080 | 24 | 64 |
| crop | 50 | 0.600 | 0.760 | 0.380 | 0.020 | 25 | 49 |
| rotate | 100 | 0.540 | 0.690 | 0.380 | 0.080 | 23 | 47 |
| IN-VOCAB (all) | 300 | 0.560 | 0.733 | 0.357 | 0.083 | 23 | 64 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.640

## mode: center_crop

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 300 | 0.560 | 0.733 | 0.357 | 0.083 | 22 | 72 |
| clean | 25 | 0.640 | 0.800 | 0.240 | 0.120 | 24 | 47 |
| blur | 25 | 0.200 | 0.560 | 0.640 | 0.160 | 22 | 44 |
| brightness | 50 | 0.640 | 0.780 | 0.260 | 0.100 | 22 | 61 |
| contrast | 50 | 0.620 | 0.800 | 0.300 | 0.080 | 22 | 43 |
| crop | 50 | 0.600 | 0.760 | 0.380 | 0.020 | 22 | 72 |
| rotate | 100 | 0.540 | 0.690 | 0.380 | 0.080 | 23 | 54 |
| IN-VOCAB (all) | 300 | 0.560 | 0.733 | 0.357 | 0.083 | 22 | 72 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.640

## mode: tta6

| cut | n | top1 | top3 | confident-wrong | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| ALL | 300 | 0.577 | 0.740 | 0.197 | 0.227 | 144 | 300 |
| clean | 25 | 0.600 | 0.840 | 0.160 | 0.240 | 147 | 264 |
| blur | 25 | 0.240 | 0.480 | 0.440 | 0.320 | 134 | 271 |
| brightness | 50 | 0.640 | 0.800 | 0.140 | 0.220 | 151 | 244 |
| contrast | 50 | 0.660 | 0.780 | 0.120 | 0.220 | 141 | 263 |
| crop | 50 | 0.580 | 0.720 | 0.160 | 0.260 | 154 | 300 |
| rotate | 100 | 0.580 | 0.740 | 0.230 | 0.190 | 138 | 257 |
| IN-VOCAB (all) | 300 | 0.577 | 0.740 | 0.197 | 0.227 | 144 | 300 |

Per-base-image-averaged clean top-1 (high-conf correct): 0.600

