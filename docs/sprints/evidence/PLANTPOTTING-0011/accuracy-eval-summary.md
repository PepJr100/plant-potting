# PLANTPOTTING-0011 — accuracy scorecard (BEFORE)

Production model `house_plant_species_mobilenetv2`, **current thresholds + current `squash` preprocessing**
= the shipping pipeline at sprint start. Produced by `AccuracyEvalTest` on a local **Pixel_6_API_34**
emulator (2026-06-06), **25 fixtures / 14 species**, each run clean + 11 deterministic perturbations ×
3 preprocessing modes (`accuracy-eval.csv`, 900 rows). Perturbation rows are **synthetic-robustness**,
never real-world samples.

## Headline BEFORE (squash, shipping pipeline)

| cut | n | top-1 | top-3 | **confident-wrong** | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| **ALL** | 300 | 0.560 | 0.733 | **0.357** | 0.083 | 23 ms | 64 ms |
| clean | 25 | 0.640 | 0.800 | 0.240 | 0.120 | 22 | 51 |
| blur | 25 | 0.200 | 0.560 | 0.640 | 0.160 | 24 | 32 |
| brightness | 50 | 0.640 | 0.780 | 0.260 | 0.100 | 26 | 52 |
| contrast | 50 | 0.620 | 0.800 | 0.300 | 0.080 | 24 | 64 |
| crop | 50 | 0.600 | 0.760 | 0.380 | 0.020 | 25 | 49 |
| rotate | 100 | 0.540 | 0.690 | 0.380 | 0.080 | 23 | 47 |

All 25 fixtures are in-vocab (in-vocab cut = ALL). Per-base-image-averaged clean top-1 = 0.640.

**The number to drive down is confident-wrong: 0.357 overall, 0.240 even on clean photos.** This larger,
messier fixture set (more iNaturalist field photos) is markedly less flattering than an earlier 16-photo
cut (0.254) — and that's the point: it's closer to the principal's real-world experience (snake plant
mis-ID'd ~2/3 of the time). Blur is the worst stressor (0.640).

## Preprocessing comparison (Phase 2 — DECIDED; see preprocessing-decision.md)

| mode | top-1 | confident-wrong | med-lat | worst-lat | verdict |
|---|---|---|---|---|---|
| **squash** (control) | 0.560 | 0.357 | 23 ms | 64 ms | kept (mode stays squash) |
| center_crop | 0.560 | 0.357 | 22 ms | 72 ms | **DROPPED** — identical, no benefit |
| **tta6** (centre + 4 corners + full-frame) | 0.577 | **0.197** | 144 ms | 300 ms | **ADOPTED** — confident-wrong nearly halved |

center-crop is a no-op on this set. **TTA-6 adopted** (manifest `tta: 6`): confident-wrong 0.357 → 0.197
at ~6× latency on a one-shot identify. This **locks the shipping pipeline**; Phase 3 tunes the abstention
threshold against the tta6 numbers (which already lift abstain to 0.227).

## Honesty caveat (load-bearing)

CC0/PD/CC-BY-clean set, small (most species 1–3 photos). It shows real confident-wrong behaviour but
still **under-represents messy real-world captures**. Numbers are *measured-under-clean-conditions +
synthetic-robustness*, NOT "real-world accuracy solved." The on-device APK spot-check (Phase 5) is the
real-world signal the harness can't be.
