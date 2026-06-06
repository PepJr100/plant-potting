# PLANTPOTTING-0011 Phase 3 — abstention before/after

The sprint's headline deliverable: drive down **confident-wrong** (clears the high-confidence gate but
names the wrong species) by routing low-margin verdicts to the "pick manually" picker. Measured on the
local Pixel_6_API_34 emulator, **25 fixtures / 14 species**, 300 rows per mode (clean + 11 perturbations).

## Before → after

| pipeline | top-1 | top-3 | **confident-wrong** | abstain (pick-manually) | latency med/worst |
|---|---|---|---|---|---|
| **BEFORE** — squash, no abstention (sprint start) | 0.560 | 0.733 | **0.357** | 0.083 | 23 / 64 ms |
| TTA-6 only (no abstention) | 0.577 | 0.740 | 0.197 | 0.227 | 144 / 300 ms |
| **AFTER** — TTA-6 + `abstain_margin 0.30` (shipping) | 0.547 | 0.740 | **0.160** | 0.293 | 144 / 300 ms |

**Confident-wrong 0.357 → 0.160 — a 55% reduction.** On **clean** photos it goes **0.240 → 0.080**.
Accepted cost: pick-manually rises 0.083 → 0.293 (top-3 candidates preserved for the manual pick), and
latency ~144 ms median on a one-shot identify (TTA). Two levers: TTA-6 does most of the work
(0.357 → 0.197); the 0.30 abstention margin trims the rest (0.197 → 0.160).

## Held-out validation (D2 — not overfit to the small set)

- **Train/test split (tune on clean → evaluate on perturbations):** the 0.30 margin was set looking at
  the clean photos; on the **perturbation rows it was not tuned against** it holds at confident-wrong
  **0.167** (≈ the aggregate 0.160). Generalises, not memorises.
- **Leave-one-species-out:** aggregate confident-wrong with each species removed stays in
  **0.138–0.174 (mean 0.160)** — no single species drives the result.
- **per_species_thresholds** left `{}` — no species met the 0006 bar (a chronic offender with multiple
  independent clean photos that a per-class value fixes without harming others).

## Method note

`accuracy-eval.csv` records **raw** scores at `abstain_margin = 0` (squash/center_crop/tta6). The AFTER
numbers are the deterministic application of the 0.30 gate to those raw top1−top2 margins — identical to
what `ModelScoreMapper` produces in-app (gate logic unit-pinned by `ModelScoreMapperAbstainMarginTest`).
Production `model_manifest.json`: `preprocess_mode: squash`, `tta: 6`, `high_confidence_abstain_margin: 0.30`.

## Honesty framing

These are **measured-under-clean-conditions + synthetic-robustness** numbers on a small CC0/PD/CC-BY set
— **not** "real-world accuracy solved." The set still under-represents messy phone captures; the on-device
APK spot-check (Phase 5) is the real-world signal the harness can't be. What this sprint delivers:
**confident-wrong driven down ~55% (to 8% on clean) under measurable conditions, top-1 held ~flat, cost
quantified.**
