# PLANTPOTTING-0012 — post-mapping gated eval vs. pre-Pilea baseline (Phase 6)

> **VERDICT: PASS.** Adding Pilea behind the gate adds **0** confident-wrong on every surface and mode (Δ vs baseline = +0), vs. +2/+9/+6 for a naive gateless mapping. Correct-pothos picker-rate delta = **+0**. **0** pothos→direct-Pilea-card violations. All 6 true-Pilea fixtures route to the picker with Pilea visible & first. Pilea ships strict-picker (no direct card this sprint).

Gated = Pilea mapped + pothos↔Pilea boundary gate ON. The binding bar: gated confident-wrong must NOT exceed the **baseline** on any surface (i.e. Δ vs baseline ≤ 0), and the naive-gateless column shows what the gate prevented.
## 1. Confident-wrong: baseline vs naive-gateless vs gated
### mode: squash
| surface | n | baseline cw | naive cw (gateless) | gated cw | Δ gated−baseline |
|---|---|---|---|---|---|
| all clean | 60 | 18 (0.300) | 20 (0.333) | 18 (0.300) | +0 |
| all perturbations | 660 | 248 (0.376) | 259 (0.392) | 248 (0.376) | +0 |
| pothos/Pilea subset | 156 | 50 (0.321) | 56 (0.359) | 50 (0.321) | +0 |

### mode: center_crop
| surface | n | baseline cw | naive cw (gateless) | gated cw | Δ gated−baseline |
|---|---|---|---|---|---|
| all clean | 60 | 18 (0.300) | 20 (0.333) | 18 (0.300) | +0 |
| all perturbations | 660 | 248 (0.376) | 259 (0.392) | 248 (0.376) | +0 |
| pothos/Pilea subset | 156 | 50 (0.321) | 56 (0.359) | 50 (0.321) | +0 |

### mode: tta6
| surface | n | baseline cw | naive cw (gateless) | gated cw | Δ gated−baseline |
|---|---|---|---|---|---|
| all clean | 60 | 7 (0.117) | 9 (0.150) | 7 (0.117) | +0 |
| all perturbations | 660 | 146 (0.221) | 155 (0.235) | 146 (0.221) | +0 |
| pothos/Pilea subset | 156 | 29 (0.186) | 35 (0.224) | 29 (0.186) | +0 |

## 2. Correct-pothos low-confidence route-rate delta (must be ~0)
Correct-pothos direct card = pothos fixture routed high-conf to epipremnum-aureum. The gate fires only on top-1=Pilea, so a correct pothos-dominant result must be untouched.

| mode | pothos rows | base correct-direct | gated correct-direct | base picker | gated picker | Δ picker |
|---|---|---|---|---|---|---|
| squash | 84 | 13 | 13 | 23 | 23 | +0 |
| center_crop | 84 | 13 | 13 | 23 | 23 | +0 |
| tta6 | 84 | 6 | 6 | 49 | 49 | +0 |

## 3. No pothos fixture surfaces a direct Pilea card (gated, tta6 clean)
| fixture | raw top-1 | gated route | gated mapped_top1 | direct Pilea card? |
|---|---|---|---|---|
| epipremnum-aureum | Pilea | low-conf | pilea-peperomioides | no |
| epipremnum-aureum__01 | Pothos (Ivy arum) | low-conf | epipremnum-aureum | no |
| epipremnum-aureum__02 | Pothos (Ivy arum) | low-conf | epipremnum-aureum | no |
| epipremnum-aureum__03 | Elephant Ear (Alocas | low-conf | alocasia | no |
| epipremnum-aureum__04 | Calathea | low-conf | goeppertia-orbifolia | no |
| epipremnum-aureum__05 | Monstera Deliciosa ( | low-conf | monstera-deliciosa | no |
| epipremnum-aureum__06 | Elephant Ear (Alocas | low-conf | alocasia | no |

**Pothos→direct-Pilea-card violations: 0** (must be 0).

## 4. True-Pilea fixtures: picker with Pilea visible (gated, tta6 clean)
| fixture | gated route | mapped_top3 (candidates) | Pilea visible? |
|---|---|---|---|
| pilea-peperomioides__01 | low-conf | pilea-peperomioides|crassula-ovata|ficus-elastica | yes |
| pilea-peperomioides__02 | low-conf | pilea-peperomioides|crassula-ovata|epipremnum-aureum | yes |
| pilea-peperomioides__03 | low-conf | pilea-peperomioides|monstera-deliciosa|epipremnum-aureum | yes |
| pilea-peperomioides__04 | low-conf | pilea-peperomioides|monstera-deliciosa|schefflera | yes |
| pilea-peperomioides__05 | low-conf | pilea-peperomioides|monstera-deliciosa|dionaea-muscipula | yes |
| pilea-peperomioides__06 | low-conf | pilea-peperomioides|zamioculcas-zamiifolia|alocasia | yes |

**True-Pilea-without-Pilea-candidate: 0** (must be 0).
