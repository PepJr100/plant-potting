# PLANTPOTTING-0013 — Direct Pilea card: A1 measurement + decision

Protocol: `pilea-direct-card-eval-plan.md` (pre-registered). Data: `baseline-strict-picker-eval.csv`
(production-identical to 0012-shipped; raw scores are model-intrinsic). Simulator:
`simulate_pilea_threshold.py` → `pilea-direct-card-threshold-sweep.csv`.

## The two distributions (raw top-1 Pilea score)

| mode | pothos→Pilea events | **pothos ceiling (max)** | **real-Pilea clean floor (min)** | separation gap |
|---|---|---|---|---|
| squash (single-crop **diagnostic**, `tta=1`) | 6 | **0.9997** (`__06` rotate_-90) | 0.9821 (`__01`) | **−0.0176** |
| center_crop (**DROPPED** 0011 diagnostic) | 6 | **0.9997** (`__06` rotate_-90) | 0.9821 (`__01`) | **−0.0176** |
| **tta6 = SHIPPED PIPELINE** | 8 | **0.9063** (`__06` rotate_-90) | **0.9940** (`__01`) | **+0.0877** |

Key facts:
- **On the shipped pipeline (tta6) the gap is clean and positive (+0.0877).** 6-crop TTA averaging
  crushes every pothos→Pilea misread to ≤ **0.9063** (the worst, a synthetic 90°-rotated pothos), while
  every real Pilea stays ≥ **0.9940**. A safe `T_pilea` exists with margin on both sides.
- **On the non-shipped single-crop diagnostics (squash / center_crop) the gap is negative**: a
  synthetic rotate_-90 / blur pothos reaches **0.9997 / 0.9974**, above the weakest real Pilea (0.9821).
  These modes are 0011-era preprocessing A/B controls (center_crop was **DROPPED**); the production
  identify path **never** feeds single-crop scores to the gate (`OnDevicePlantIdentifier` always
  averages the `tta=6` crops). So these events are harness artifacts, not shipped behaviour.

## LOO + author separation (shipped pipeline, all 6 folds)

6 Pilea fixtures, **6 distinct authors** → every fold is author-separated by construction. Selection
rule picked `T_pilea = 0.9700` (smallest grid value > 0.9661 that all 5 tuning fixtures clear) on every
fold. **Every fold: held-out real Pilea earns its direct card back, and 0 pothos→Pilea events cross →
+0 confident-wrong.** Full table: `pilea-direct-card-threshold-sweep.csv`.

```
LOO VERDICT: ALL FOLDS PASS (+0 confident-wrong on shipped pipeline)
```

## Chosen `T_pilea`

**`T_pilea = 0.98`** on the shipped pipeline:
- CI floor: `0.98 > 0.9661` ✓ (config CI-bind enforces this).
- Margin above the shipped pothos ceiling: `0.98 − 0.9063 = +0.074`.
- Admits **all 6** real-Pilea fixtures (shipped floor 0.9940 ≥ 0.98) → every confident, correct Pilea
  gets a direct card.

## Decision

**SHIP the direct Pilea card with `T_pilea = 0.98`**, evaluated on the **shipped (tta6) pipeline**, where
the separation is clean (+0.0877 gap, +0 confident-wrong under LOO + author separation) and the at-home
review's real-Pilea-@98% photo would now get a direct card.

### Residual risk (documented, not hidden)
- The negative gap on the **non-shipped** single-crop diagnostic modes (squash/cc) means that *if the app
  ever shipped single-crop preprocessing*, a synthetic-perturbed pothos could cross `T_pilea`. It does
  not today (shipped = tta6). The on-device `post-direct-card-eval` confirms 0 pothos fixtures surface a
  direct Pilea card on the shipped pipeline.
- `T_pilea` is fit above a thin pothos→Pilea set (n=1 clean + a synthetic rotate event). The fail-safe
  (picker for the 0.9661<score<T_pilea band) remains load-bearing; any future pothos→Pilea fixture
  raising the tta6 ceiling toward 0.98 must re-open this decision.

> **VERDICT (principal-confirmed 2026-06-08): SHIP.** The shipped-pipeline (tta6) reading governs the
> +0-confident-wrong bar. `per_species_thresholds["pilea-peperomioides"] = 0.98` is activated (Phase A4),
> CI-bound `> 0.9661`. The squash/center_crop negative gap is from non-shipped single-crop diagnostic
> modes and is recorded above as documented residual risk. On-device `post-direct-card-eval` confirms the
> shipped-pipeline property (0 pothos→direct-Pilea cards).
