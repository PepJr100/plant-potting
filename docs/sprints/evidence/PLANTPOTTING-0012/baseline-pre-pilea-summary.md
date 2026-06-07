# PLANTPOTTING-0012 — baseline (pre-Pilea) boundary scorecard
Source: `baseline-pre-pilea-eval.csv` (production model, **Pilea in KB but UNMAPPED, gate absent**). Production pipeline = `tta6` (squash + 6-crop TTA). Perturbation rows are synthetic-robustness, reported separately from clean base photos.
## 1. Live pothos→Pilea failure (clean base photos)
| mode | pothos fixtures | top-1 = Pilea | top-1 = Pilea @≥0.55 | mean Pilea-when-top1 score |
|---|---|---|---|---|
| squash | 7 | 1 | 1 | 0.9661 |
| center_crop | 7 | 1 | 1 | 0.9661 |
| tta6 | 7 | 1 | 1 | 0.6566 |

## 2. Per-fixture boundary detail (clean, `tta6` = production)
Route reason: a pothos that raw-predicts Pilea routes to the picker TODAY only because its top-1 (Pilea) is unmapped — that silent miss is what naive mapping would turn confidently wrong.

| fixture | expected | raw top-1 | score | margin | mapped | route | conf-wrong (now) | naive-mapped → |
|---|---|---|---|---|---|---|---|---|
| epipremnum-aureum | epipremnum-aureum | Pilea | 0.657 | 0.489 | anthurium-andraeanum | low-conf | false | Pilea card (WRONG) |
| epipremnum-aureum__01 | epipremnum-aureum | Pothos (Ivy arum) | 0.411 | 0.155 | epipremnum-aureum | low-conf | false | picker |
| epipremnum-aureum__02 | epipremnum-aureum | Pothos (Ivy arum) | 0.307 | 0.047 | epipremnum-aureum | low-conf | false | picker |
| epipremnum-aureum__03 | epipremnum-aureum | Elephant Ear (Alocasia | 0.564 | 0.139 | alocasia | low-conf | false | picker |
| epipremnum-aureum__04 | epipremnum-aureum | Calathea | 0.582 | 0.173 | goeppertia-orbifolia | low-conf | false | picker |
| epipremnum-aureum__05 | epipremnum-aureum | Monstera Deliciosa (Mo | 0.610 | 0.281 | monstera-deliciosa | low-conf | false | picker |
| epipremnum-aureum__06 | epipremnum-aureum | Elephant Ear (Alocasia | 0.277 | 0.001 | alocasia | low-conf | false | picker |
| pilea-peperomioides__01 | pilea-peperomioides | Pilea | 0.994 | 0.988 | crassula-ovata | low-conf | false | Pilea card (correct) |
| pilea-peperomioides__02 | pilea-peperomioides | Pilea | 1.000 | 1.000 | crassula-ovata | low-conf | false | Pilea card (correct) |
| pilea-peperomioides__03 | pilea-peperomioides | Pilea | 1.000 | 1.000 | monstera-deliciosa | low-conf | false | Pilea card (correct) |
| pilea-peperomioides__04 | pilea-peperomioides | Pilea | 1.000 | 1.000 | monstera-deliciosa | low-conf | false | Pilea card (correct) |
| pilea-peperomioides__05 | pilea-peperomioides | Pilea | 0.998 | 0.996 | monstera-deliciosa | low-conf | false | Pilea card (correct) |
| pilea-peperomioides__06 | pilea-peperomioides | Pilea | 1.000 | 1.000 | zamioculcas-zamiifolia | low-conf | false | Pilea card (correct) |

## 3. Naive-Pilea-mapped simulation (gateless) — confident-wrong delta
"If we mapped Pilea with **no gate**, how many results turn confidently wrong?" Baseline = Pilea unmapped (today). Δ = new confident-wrong introduced purely by the naive mapping. This is the number the Phase 6 gated result must NOT exceed (it must be 0 added).

### mode: squash
| surface | n | baseline conf-wrong | naive conf-wrong | Δ (added by naive Pilea) |
|---|---|---|---|---|
| all clean | 60 | 18 (0.300) | 20 (0.333) | +2 |
| all perturbations | 660 | 248 (0.376) | 259 (0.392) | +11 |
| pothos/Pilea subset (all inputs) | 156 | 50 (0.321) | 56 (0.359) | +6 |

### mode: center_crop
| surface | n | baseline conf-wrong | naive conf-wrong | Δ (added by naive Pilea) |
|---|---|---|---|---|
| all clean | 60 | 18 (0.300) | 20 (0.333) | +2 |
| all perturbations | 660 | 248 (0.376) | 259 (0.392) | +11 |
| pothos/Pilea subset (all inputs) | 156 | 50 (0.321) | 56 (0.359) | +6 |

### mode: tta6
| surface | n | baseline conf-wrong | naive conf-wrong | Δ (added by naive Pilea) |
|---|---|---|---|---|
| all clean | 60 | 7 (0.117) | 9 (0.150) | +2 |
| all perturbations | 660 | 146 (0.221) | 155 (0.235) | +9 |
| pothos/Pilea subset (all inputs) | 156 | 29 (0.186) | 35 (0.224) | +6 |

## 4. Latency (`tta6`, all rows)
median 120 ms · worst 268 ms · n=720
