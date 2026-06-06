# PLANTPOTTING-0011 — accuracy scorecard (BEFORE)

Production model `house_plant_species_mobilenetv2`, **current thresholds + current `squash` preprocessing**
= the shipping pipeline. Produced by `AccuracyEvalTest` on a local **Pixel_6_API_34** emulator
(2026-06-06), 20 fixtures / 14 species, each run clean + 11 deterministic perturbations × 3 modes
(`accuracy-eval.csv`, 720 rows). Perturbation rows are **synthetic-robustness**, never real-world samples.

## Headline BEFORE (squash, shipping pipeline)

| cut | n | top-1 | top-3 | **confident-wrong** | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| **ALL** | 240 | 0.654 | 0.796 | **0.254** | 0.092 | 32 ms | 170 ms |
| clean | 20 | 0.750 | 0.850 | 0.100 | 0.150 | 40 | 99 |
| blur | 20 | 0.250 | 0.650 | 0.550 | 0.200 | 37 | 79 |
| brightness | 40 | 0.775 | 0.825 | 0.125 | 0.100 | 32 | 101 |
| contrast | 40 | 0.725 | 0.850 | 0.200 | 0.075 | 32 | 130 |
| crop | 40 | 0.675 | 0.850 | 0.300 | 0.025 | 34 | 104 |
| rotate | 80 | 0.625 | 0.750 | 0.288 | 0.088 | 32 | 170 |

All 20 fixtures are in-vocab, so the in-vocab cut equals ALL. Per-base-image-averaged clean top-1 = 0.750.

**The number to drive down is confident-wrong: 0.254 overall, 0.100 even on clean photos** (the model
clears the high-confidence gate but names the wrong species). Blur is the worst stressor (0.55).

## Preprocessing comparison (Phase 2 input — threshold-independent top-1)

| mode | top-1 | confident-wrong | med-lat | worst-lat | verdict |
|---|---|---|---|---|---|
| **squash** (control) | 0.654 | 0.254 | 32 ms | 170 ms | shipping |
| center_crop | 0.654 | 0.254 | 34 ms | 109 ms | **DROP** — identical top-1, no benefit |
| tta5 (center+4 corners) | 0.671 | 0.121 | 168 ms | 1275 ms | candidate — +1.7pt top-1, **confident-wrong halved**, but ~5× latency + abstain 0.092→0.208 |

center-crop is a no-op on this set (the model already tolerates the squash). TTA is the only lever that
moves the needle, and it trades latency + a higher abstain rate for it — a decision to weigh in Phase 2/3.

## Honesty caveat (load-bearing)

This set is CC0/PD-clean and small (most species have 1–2 photos). Even so it shows real confident-wrong
behaviour, but it still **under-represents the principal's messy real-world captures** (where the snake
plant is mis-ID'd ~2/3 of the time). Numbers here are *measured-under-clean-CC-conditions +
synthetic-robustness*, NOT a claim that real-world accuracy is solved. The on-device APK spot-check
(Phase 5) remains the only real-world signal.
