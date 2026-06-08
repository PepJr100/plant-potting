# PLANTPOTTING-0013 — direct Pilea card: on-device eval verdict (Phase A4)

> **VERDICT: SHIP confirmed on device.** With `per_species_thresholds["pilea-peperomioides"] = 0.98`
> active, the shipped (tta6) pipeline adds **+0 confident-wrong** on every surface (clean / all
> perturbations / pothos-Pilea subset) vs. the strict-picker baseline, **0** pothos fixtures surface a
> direct Pilea card, and **all 6 real-Pilea fixtures now get a correct direct card** (were all routed to
> the picker under strict-picker). The crux from A0 held: the only separator is the absolute Pilea top-1,
> and `T_pilea = 0.98` sits cleanly between the shipped pothos ceiling (0.9063) and the real-Pilea floor
> (0.9940).

Run: on-device `AccuracyEvalTest` on the local `pixel6Api34` emulator over the expanded **66-fixture**
set with the direct-card manifest active. Pulled CSV: `expanded-scorecard.csv`. Analysis:
`analyze_scorecard.py`. This one run serves **both** the A4 post-direct-card eval (this doc) and the
Thread B expanded scorecard (`expanded-scorecard-summary.md`).

## 1. Confident-wrong: direct-card vs strict-picker baseline (gate-isolated)

The gate refinement only re-routes Pilea-top-1 rows; Thread B added 6 **new** non-Pilea fixtures. To
isolate the gate effect from fixture-set growth, the direct-card eval is restricted to the **same 60
baseline fixtures** before comparing. (The pothos/Pilea subset is identical in both sets, so its Δ is
gate-only regardless.)

| mode | clean Δ | all-perturbation Δ | pothos/Pilea subset Δ |
|---|---|---|---|
| **tta6 (SHIPPED)** | **+0** (7/60) | **+0** (153/720) | **+0** (29/156) |
| squash (non-shipped diagnostic) | +0 (18/60) | +5 (271/720) | +3 (53/156) |
| center_crop (non-shipped, DROPPED 0011) | +0 (18/60) | +5 | +3 |

**The binding bar is met on the shipped pipeline: Δ+0 on all three surfaces.** The squash/center_crop
+3/+5 is exactly the documented residual risk — a synthetic-perturbed pothos (rotate_-90 @ 0.9997, blur
@ 0.9974, contrast_down @ 0.9810) crosses 0.98 only in the **non-shipped single-crop** modes. Production
never feeds single-crop scores to the gate (`OnDevicePlantIdentifier` always averages the tta=6 crops),
so these rows correspond to no shipped behaviour. Principal-confirmed the shipped-pipeline reading governs.

## 2. No pothos fixture surfaces a direct Pilea card (tta6 clean)

| fixture | raw top-1 (Pilea) | route | mapped top-1 | direct Pilea card? |
|---|---|---|---|---|
| epipremnum-aureum | 0.6566 | low-conf | pilea-peperomioides | **no** |
| epipremnum-aureum__01 | 0.4106 | low-conf | epipremnum-aureum | no |
| epipremnum-aureum__02 | 0.3074 | low-conf | epipremnum-aureum | no |
| epipremnum-aureum__03 | 0.5639 | low-conf | alocasia | no |
| epipremnum-aureum__04 | 0.5823 | low-conf | goeppertia-orbifolia | no |
| epipremnum-aureum__05 | 0.6095 | low-conf | monstera-deliciosa | no |
| epipremnum-aureum__06 | 0.2769 | low-conf | alocasia | no |

**pothos→direct-Pilea-card violations: 0** (must be 0). The base pothos (top-1 = Pilea @ 0.6566 tta6)
stays in the picker — 0.6566 < 0.98.

## 3. Real-Pilea fixtures now earn a direct card (tta6 clean) — the SHIP benefit

| fixture | raw top-1 | route | direct Pilea card? |
|---|---|---|---|
| pilea-peperomioides__01 | 0.9940 | high-conf | **yes** |
| pilea-peperomioides__02 | 1.0000 | high-conf | **yes** |
| pilea-peperomioides__03 | 1.0000 | high-conf | **yes** |
| pilea-peperomioides__04 | 1.0000 | high-conf | **yes** |
| pilea-peperomioides__05 | 0.9979 | high-conf | **yes** |
| pilea-peperomioides__06 | 1.0000 | high-conf | **yes** |

All 6 cleared `T_pilea = 0.98` → **6/6 direct Pilea cards** (vs. 0/6 under strict-picker). This is the
at-home review's "real Pilea @ 98% in the picker" complaint resolved.
