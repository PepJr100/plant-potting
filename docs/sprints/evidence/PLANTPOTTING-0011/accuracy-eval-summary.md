# PLANTPOTTING-0011 — accuracy scorecard (BEFORE)

Production model `house_plant_species_mobilenetv2`, **current thresholds + current `squash` preprocessing**
= the shipping pipeline at sprint start. Produced by `AccuracyEvalTest` on a local **Pixel_6_API_34**
emulator (2026-06-06), **41 fixtures / 30 species**, each run clean + 11 deterministic perturbations ×
3 preprocessing modes (`accuracy-eval.csv`, 1476 rows). Perturbation rows are **synthetic-robustness**,
never real-world samples.

## Headline BEFORE (squash, shipping pipeline)

| cut | n | top-1 | top-3 | **confident-wrong** | abstain | med-lat | worst-lat |
|---|---|---|---|---|---|---|---|
| **ALL** | 492 | 0.528 | 0.713 | **0.382** | 0.089 | 20 ms | 83 ms |
| clean | 41 | 0.610 | 0.756 | 0.268 | 0.122 | 21 | 58 |
| blur | 41 | 0.244 | 0.561 | 0.610 | 0.146 | 19 | 43 |
| brightness | 82 | 0.573 | 0.732 | 0.341 | 0.085 | 21 | 78 |
| contrast | 82 | 0.549 | 0.756 | 0.354 | 0.098 | 21 | 68 |
| crop | 82 | 0.549 | 0.720 | 0.402 | 0.049 | 21 | 83 |
| rotate | 164 | 0.537 | 0.707 | 0.378 | 0.085 | 20 | 83 |

All 41 fixtures in-vocab (in-vocab cut = ALL). Per-base-image-averaged clean top-1 = 0.610.

**Confident-wrong is 0.382 overall — 0.268 even on clean photos.** This broad set (30 of the model's 38
mapped species, mostly iNaturalist field photos) is the most honest cut yet: a 38% confidently-wrong rate
is close to the principal's real-world perception (snake plant mis-ID'd ~2/3 of the time). Blur is worst
(0.610).

## Preprocessing comparison (Phase 2 — DECIDED; see preprocessing-decision.md)

| mode | top-1 | confident-wrong | med-lat | worst-lat | verdict |
|---|---|---|---|---|---|
| **squash** (control) | 0.528 | 0.382 | 20 ms | 83 ms | kept (mode stays squash) |
| center_crop | 0.528 | 0.382 | 20 ms | 189 ms | **DROPPED** — identical, no benefit |
| **tta6** (centre + 4 corners + full-frame) | 0.530 | **0.226** | 125 ms | 539 ms | **ADOPTED** — confident-wrong ~40% lower |

center-crop is a no-op. **TTA-6 adopted** (`tta: 6`): confident-wrong 0.382 → 0.226 at ~6× latency on a
one-shot identify. This **locks the shipping pipeline**; Phase 3 tunes abstention against the tta6 numbers
(which already lift abstain to 0.244).

## Honesty caveat (load-bearing)

CC0/PD/CC-BY-clean set, 30 of 38 mapped species (1–3 photos each). It shows real confident-wrong
behaviour but still **under-represents messy real-world captures**. Numbers are
*measured-under-clean-conditions + synthetic-robustness*, NOT "real-world accuracy solved." The on-device
APK spot-check (Phase 5) is the real-world signal the harness can't be.
