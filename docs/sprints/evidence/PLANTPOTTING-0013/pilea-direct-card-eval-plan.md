# PLANTPOTTING-0013 — Direct Pilea card: pre-registered evaluation protocol (Phase A1)

**Pre-registered before any production-logic change and before inspecting any LOO fold.** This is the
protocol; the verdict lands in `pilea-direct-card-decision.md`.

## A0 — the crux (stated honestly, before designing)

A **real Pilea** (top-1 ≈ 0.98–1.00, no second-place mass) and a **pothos misread as Pilea**
(top-1 ≈ 0.97, no second-place mass) are near-identical in *score shape*. The top1−top2 **margin
cannot separate them** — both margins are huge. The **only** separating lever the score vector offers
is the **absolute Pilea top-1 value**. So a direct Pilea card is only safe behind a threshold `T_pilea`
set **strictly above the worst pothos→Pilea score**, with a safety margin.

**Rejected mechanism (recorded so nobody tunes a dead lever):** *margin-over-second as the separator.*
Both real-Pilea and pothos-misread have wide margins; a Pilea-specific margin is at most a
belt-and-suspenders sanity guard, never the separator.

**n≈1 reality:** on **clean** photos only the base `epipremnum-aureum.jpg` mispredicts as Pilea
(@0.9661 squash); the other pothos fixtures predict pothos/alocasia/calathea/monstera. Under
**synthetic perturbation** a second pothos event surfaces (`epipremnum-aureum__06` rotate_-90). `T_pilea`
is therefore fit above a *very thin* set of dangerous points — the fail-safe (picker) is load-bearing,
the bar is conservative, and any newly-surfaced pothos→Pilea event **raises the ceiling**.

## The candidate rule

Reuse the existing per-species plain-threshold mechanism: `per_species_thresholds["pilea-peperomioides"]
= T_pilea`. Refine the `boundary_pairs` early-return in `ModelScoreMapper` so a top-1 = Pilea result
routes to the `LowConfidencePicker` **unless** `bestProb ≥ T_pilea`, in which case control falls through
to the existing high-confidence path (the direct card). Composes the elevated bar **with** the boundary
rule; adds **no new manifest key**.

## Which pipeline the gate actually runs on (decisive)

Production preprocessing is **`squash` + 6-crop TTA** (`model_manifest.json`: `preprocess_mode=squash`,
`tta=6`). `OnDevicePlantIdentifier.identify()` always feeds the **TTA-6-averaged** score vector to
`ModelScoreMapper.map()`. **There is no production path where the gate sees a single-crop score.**

`AccuracyEvalTest` emits three `mode` rows per fixture: `squash` (single-crop, `tta=1`), `center_crop`
(single-crop — **DROPPED** in 0011), and `tta6` (= **the shipped pipeline**). The `squash` and
`center_crop` rows are 0011-era preprocessing A/B **diagnostics**, not shipped surfaces. The binding
`T_pilea` is therefore evaluated against the **`tta6`** distribution (the shipped pipeline); squash/cc
are reported for completeness/cross-check and as a documented residual-risk note, **not** as the ship
gate. (If a future sprint ever ships single-crop, this assumption must be revisited.)

## Pre-committed threshold grid (registered now, before any fold is seen)

```
T_pilea ∈ { 0.9700, 0.9750, 0.9800, 0.9850, 0.9900, 0.9950, 0.9990, 0.9998 }
```

All candidates satisfy the hard CI floor **`T_pilea > 0.9661`** (the documented clean-squash pothos
ceiling). LOO selection may pick **only** from this grid.

## Method

1. **Distributions.** From `baseline-strict-picker-eval.csv` extract, per fixture × mode, the raw
   top-1 Pilea score for (a) every pothos fixture whose raw top-1 resolves to Pilea — the
   **pothos→Pilea ceiling** — and (b) every Pilea fixture — the **real-Pilea floor**.
2. **Separation-gap early-abort.** `gap = min(real-Pilea clean top-1) − max(pothos→Pilea top-1)` on the
   **shipped (tta6)** pipeline. If `gap ≤ 0`, no safe `T_pilea` exists → **fall back to strict-picker**
   now (A4 blocker path). If `gap > 0`, proceed. (The gap is also reported per-mode for transparency.)
3. **Offline simulator** (`simulate_pilea_threshold.py`) — simulates each grid `T_pilea` from the CSV
   with **no app-code change**, decoupling threshold iteration from the slow wipe-on-reinstall GMD cycle.
4. **Leave-one-out + author separation.** The 6 Pilea fixtures have **6 distinct authors**, so every LOO
   fold is automatically author-separated (no tuning photo shares the held-out fixture's author).
   Selection rule (applied to the 5 tuning fixtures + the pothos ceiling, pre-committed): pick the
   **smallest grid `T_pilea`** such that `T_pilea > 0.9661` **and** every one of the 5 tuning Pilea
   fixtures clears it on the shipped pipeline. A fold **passes** iff, at that `T_pilea`, **no**
   pothos→Pilea event crosses (i.e. **+0 confident-wrong**). Restoring the held-out real-Pilea direct
   card is the *benefit*, not the safety bar.
5. **Sweep CSV** `pilea-direct-card-threshold-sweep.csv`: fold id, tuning authors, held-out
   photo/author, candidate `T_pilea`, held-out route, held-out correctness, confident-wrong count,
   pothos-sentinel outcome.

## Pass / fail bar (pre-committed)

- **SHIP** the direct card iff: `gap > 0` on the shipped pipeline **and** every LOO fold keeps **+0
  confident-wrong** (no pothos→Pilea event crosses the selected `T_pilea`) on the shipped pipeline,
  **and** the chosen shipped `T_pilea` is **> 0.9661** with a real margin above the pothos ceiling.
- Otherwise **FALL BACK** to strict-picker; this document + the decision doc become the blocker note.
- **Never** relax the global gates, the abstain margin, or set `T_pilea ≤ 0.9661` to make it pass
  (0006 discipline).

## Chosen-`T_pilea` rule for the shipped manifest

If SHIP: pick a single `T_pilea` from the grid that (a) clears the CI floor 0.9661 with margin, (b) sits
with margin above the shipped-pipeline pothos ceiling, and (c) admits as many of the 6 real-Pilea
fixtures as possible on the shipped pipeline. Record the squash/cc diagnostic behaviour as residual risk.
