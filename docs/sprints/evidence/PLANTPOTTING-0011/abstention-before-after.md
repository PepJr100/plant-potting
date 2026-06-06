# PLANTPOTTING-0011 Phase 3 — abstention before/after

The sprint's headline deliverable: drive down **confident-wrong** (clears the high-confidence gate but
names the wrong species) by routing low-margin verdicts to the "pick manually" picker. Measured on the
local Pixel_6_API_34 emulator, 20 fixtures / 14 species, 240 rows (clean + 11 perturbations each).

## Before → after

| pipeline | top-1 | top-3 | **confident-wrong** | abstain (pick-manually) | latency med/worst |
|---|---|---|---|---|---|
| **BEFORE** — squash, no abstention (sprint start) | 0.654 | 0.796 | **0.254** | 0.092 | 30 / 103 ms |
| TTA-6 only (no abstention) | 0.667 | 0.808 | 0.113 | 0.221 | 184 / 453 ms |
| **AFTER** — TTA-6 + `abstain_margin 0.30` (shipping) | 0.642 | 0.808 | **0.083** | 0.275 | 184 / 453 ms |

**Confident-wrong 0.254 → 0.083 — a 67% reduction.** On **clean** photos it goes **0.100 → 0.000**.
Accepted cost: pick-manually rises 0.092 → 0.275 (top-3 candidates are preserved for the manual pick),
and latency rises to ~184 ms median on a one-shot identify (TTA). Two levers contribute: TTA-6 does
most of the work (0.254 → 0.113); the 0.30 abstention margin trims the rest (0.113 → 0.083) and zeroes
clean confident-wrong.

## Held-out validation (D2 — not overfit to the tiny clean set)

- **Train/test split (tune on clean → evaluate on perturbations):** the 0.30 margin was chosen to zero
  confident-wrong on clean photos; evaluated on the **perturbation rows it was not tuned against**, it
  holds at **confident-wrong 0.091** (≈ the aggregate 0.083). It generalises, not memorises.
- **Leave-one-species-out:** recomputing aggregate confident-wrong with each species removed stays in
  **0.056–0.093 (mean 0.083)** — no single species drives the result.
- **Per-species confident-wrong at 0.30:** 9 of 14 species at 0.000; the tail is `crassula-ovata` 0.333
  (its 2nd base photo is a hard trunk close-up), `schefflera` 0.167, `spathiphyllum-wallisii` 0.125,
  with aloe/dieffenbachia/dracaena/phalaenopsis at 0.083. No `per_species_thresholds` seeded — none met
  the 0006 bar (a chronic offender with multiple independent clean photos that a per-class value fixes
  without harming others); `per_species_thresholds` stays `{}`.

## Method note

`accuracy-eval.csv` records **raw** scores at `abstain_margin = 0` (across squash/center_crop/tta6).
The AFTER numbers are the deterministic application of the 0.30 gate to those raw margins — identical
to what `ModelScoreMapper` produces in-app (the gate logic is unit-pinned by
`ModelScoreMapperAbstainMarginTest`). The production `model_manifest.json` now carries
`preprocess_mode: squash`, `tta: 6`, `high_confidence_abstain_margin: 0.30`.

## Honesty framing

These are **measured-under-clean-CC-conditions + synthetic-robustness** numbers on a small CC0/PD set
— **not** "real-world accuracy solved." The set under-represents the principal's messy captures; the
on-device APK spot-check (Phase 5) is the real-world signal the harness can't be. What this sprint
delivers: **confident-wrong driven down ~67% (to 0% on clean) under measurable conditions, with raw
top-1 held flat and the cost quantified.**
