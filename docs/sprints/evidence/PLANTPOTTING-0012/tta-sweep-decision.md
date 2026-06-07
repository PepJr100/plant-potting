# PLANTPOTTING-0012 — TTA sweep decision (Phase 7)

> **Strategy note (principal-chosen):** views 7+ use **uniform grid tiling** (2×2 then 3×3), NOT flips/tight-corners — genuinely new spatial coverage, no zone re-scored. Finding: the grid tiles are noisy for this **whole-image classifier** (a 3×3 tile ≈ a leaf, out-of-distribution), so they *dilute* averaged confidence → more abstention. 2×2 left clean confident-wrong flat (0.117) and top-1 slightly worse; 2×2+3×3 cut confident-wrong to 0.050 only by crashing top-1 (0.417→0.317) and abstaining 63% — and busted the ~2 s cap (2686 ms). Neither is a real win.

Gated production pipeline (pothos↔Pilea gate ON) at TTA ×6/×8/×10/×20 over the expanded fixture set. Latency cap ~2 s (worst observed). The boundary gate is deterministic and TTA-independent (top-1=Pilea → picker), so TTA is judged only on overall confident-wrong / accuracy within the latency budget.

| level | surface | n | top1-acc | confident-wrong | low-conf | med lat | worst lat |
|---|---|---|---|---|---|---|---|
| tta6 | clean | 60 | 0.417 | 0.117 | 0.467 | 136 | 1076 |
| tta6 | perturb | 660 | 0.403 | 0.221 | 0.376 | · | · |
| tta6 | pothos/Pilea | 156 | 0.038 | 0.186 | 0.776 | · | · |
| grid2x2 | clean | 60 | 0.400 | 0.117 | 0.483 | 227 | 1500 |
| grid2x2 | perturb | 660 | 0.371 | 0.155 | 0.474 | · | · |
| grid2x2 | pothos/Pilea | 156 | 0.019 | 0.128 | 0.853 | · | · |
| grid2x2_3x3 | clean | 60 | 0.317 | 0.050 | 0.633 | 439 | 2686 |
| grid2x2_3x3 | perturb | 660 | 0.303 | 0.073 | 0.624 | · | · |
| grid2x2_3x3 | pothos/Pilea | 156 | 0.006 | 0.058 | 0.936 | · | · |

## Latency vs cap
| level | median ms | worst ms | within ~2s? |
|---|---|---|---|
| tta6 | 136 | 1076 | yes |
| grid2x2 | 227 | 1500 | yes |
| grid2x2_3x3 | 439 | 2686 | NO — REJECT |

## Decision
Gated ×6 production candidate — clean: top1=0.417, cw=0.117.

- grid2x2: clean cw 0.117 (Δ+0.000), top1 0.400 (Δ-0.017), worst 1500 ms → no meaningful gain (noise).
- grid2x2_3x3: REJECTED (worst 2686 ms > 2000 ms cap).

**Adopted:** **`tta = 6` (unchanged)**. No higher level cleared a meaningful confident-wrong/accuracy improvement over gated ×6 within the ~2 s budget; the gate (not TTA) is what fixes the boundary, so the extra latency of ×8/×10/×20 buys nothing material. `model_manifest.json` `tta` stays 6.
