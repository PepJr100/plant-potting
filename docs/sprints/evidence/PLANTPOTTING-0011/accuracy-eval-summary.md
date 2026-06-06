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

## Preprocessing comparison (Phase 2 — DECIDED; see preprocessing-decision.md)

| mode | top-1 | confident-wrong | med-lat | worst-lat | verdict |
|---|---|---|---|---|---|
| **squash** (control) | 0.654 | 0.254 | 30 ms | 103 ms | kept (mode stays squash) |
| center_crop | 0.654 | 0.254 | 30 ms | 58 ms | **DROPPED** — identical, no benefit |
| **tta6** (centre + 4 corners + full-frame) | 0.667 | **0.113** | 184 ms | 453 ms | **ADOPTED** — confident-wrong more than halved |

center-crop is a no-op on this set. **TTA-6 is adopted** (manifest `tta: 6`) — it more than halves
confident-wrong at ~6× latency on a one-shot identify. This **locks the shipping pipeline**; Phase 3
tunes the abstention thresholds against the tta6 numbers above (note tta6 already lifts abstain to 0.221).

## Honesty caveat (load-bearing)

This set is CC0/PD-clean and small (most species have 1–2 photos). Even so it shows real confident-wrong
behaviour, but it still **under-represents the principal's messy real-world captures** (where the snake
plant is mis-ID'd ~2/3 of the time). Numbers here are *measured-under-clean-CC-conditions +
synthetic-robustness*, NOT a claim that real-world accuracy is solved. The on-device APK spot-check
(Phase 5) remains the only real-world signal.
