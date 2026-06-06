# PLANTPOTTING-0011 Phase 3 — abstention before/after

The sprint's headline deliverable: drive down **confident-wrong** (clears the high-confidence gate but
names the wrong species) by routing low-margin verdicts to the "pick manually" picker. Measured on the
local Pixel_6_API_34 emulator, **41 fixtures / 30 species**, 492 rows per mode (clean + 11 perturbations).

## Before → after

| pipeline | top-1 | top-3 | **confident-wrong** | abstain (pick-manually) | latency med/worst |
|---|---|---|---|---|---|
| **BEFORE** — squash, no abstention (sprint start) | 0.528 | 0.713 | **0.382** | 0.089 | 20 / 83 ms |
| TTA-6 only (no abstention) | 0.530 | 0.758 | 0.226 | 0.244 | 125 / 539 ms |
| **AFTER** — TTA-6 + `abstain_margin 0.30` (shipping) | 0.498 | 0.758 | **0.179** | 0.323 | 125 / 539 ms |

**Confident-wrong 0.382 → 0.179 — a 53% reduction.** On **clean** photos it goes **0.268 → 0.098**.
Accepted cost: pick-manually rises 0.089 → 0.323 (top-3 candidates preserved for the manual pick), and
latency ~125 ms median on a one-shot identify (TTA). Two levers: TTA-6 does most of the work
(0.382 → 0.226); the 0.30 abstention margin trims the rest (0.226 → 0.179).

## Held-out validation (D2 — not overfit to the fixture set)

- **Train/test split (tune on clean → evaluate on perturbations):** the 0.30 margin was set on the clean
  photos; on the **perturbation rows it was not tuned against** it holds at confident-wrong **0.186**
  (≈ the aggregate 0.179). Generalises, not memorises.
- **Leave-one-species-out (30 species):** aggregate confident-wrong with each species removed stays in
  **0.167–0.188 (mean 0.179)** — no single species drives the result.
- **per_species_thresholds** left `{}` — no species met the 0006 bar.

## Method note

`accuracy-eval.csv` records **raw** scores at `abstain_margin = 0` (squash/center_crop/tta6). The AFTER
numbers are the deterministic application of the 0.30 gate to those raw top1−top2 margins — identical to
what `ModelScoreMapper` produces in-app (gate logic unit-pinned by `ModelScoreMapperAbstainMarginTest`).
Production `model_manifest.json`: `preprocess_mode: squash`, `tta: 6`, `high_confidence_abstain_margin: 0.30`.

## Honesty framing

**Measured-under-clean-conditions + synthetic-robustness** numbers on a CC0/PD/CC-BY set spanning 30 of
the 38 mapped species — **not** "real-world accuracy solved." Still under-represents messy phone captures;
the on-device APK spot-check (Phase 5) is the real-world signal the harness can't be. What this sprint
delivers: **confident-wrong down ~53% (to ~10% on clean) under measurable conditions, top-1 ~flat, cost
quantified.**
