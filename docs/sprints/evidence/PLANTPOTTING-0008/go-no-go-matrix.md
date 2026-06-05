# GO / NO-GO matrix — PLANTPOTTING-0008

Per-species verdict against the thresholds, **deciding number shown**. Counts as of **2026-06-05**.
Backing detail: `data-availability-report.md`; machine-readable: `source-counts.csv`.

## Thresholds

- **GO (comfortable):** ~150–300+ license-clean usable images/species.
- **CONDITIONAL (floor):** ~50–100/species (viable but thin — augmentation / few-shot risk).
- **NO-GO (below floor):** < ~50 usable/species.
- **Boundary:** ~150–300 of **EACH** of pothos and Pilea (incl. lookalike hard shots).

## Per-species roll-up

| KB target | Total usable (deduped est.) | Deciding number | Threshold band | Verdict |
|---|---:|---|---|:--:|
| `chlorophytum-comosum` | **~790** | iNat 664 + Commons 53 + Flickr ½·78 | ≫ 300 | **GO** |
| `philodendron-hederaceum` | **~467** | iNat 413 + Commons 53 | ≫ 300 | **GO** |
| `hoya-carnosa` | **~430** | iNat 246 + Commons 105 + Flickr ½·79 | ≫ 300 | **GO** |
| `monstera-adansonii` | **~369** | iNat 295 + Commons 71 | > 300 | **GO** |
| `ficus-lyrata` | **~257** | iNat 165 + Commons 50 + Flickr ½·43 | > 150 (captive-incl) | **GO** ⚠ |
| `epipremnum-aureum` (pothos, boundary) | **~1470** | iNat 1178 + Commons 134 + Flickr ½·160 | ≫ 300 | **GO** |
| `pilea-peperomioides` (boundary pair) | **~76** | iNat 33 + Commons 34 + Flickr ½·17 | 50–100 floor, < 150 boundary target | **CONDITIONAL** |
| `philodendron-pink-princess` (cultivar) | **~5–15** | Commons 4 + Flickr ~0–1 | < 50 | **NO-GO** |

⚠ `ficus-lyrata` GO is contingent on **captive-inclusive** counts (research-grade only = 8). Solid for
a houseplant classifier, but confidence is medium and the fine-tune should favour potted/indoor shots.

## Boundary verdict (pothos ↔ Pilea, assessed as a pair)

| side | usable | target | meets target? |
|---|---:|---|:--:|
| Pothos (*Epipremnum aureum*) | ~1470 | ~150–300 | ✅ yes (abundant) |
| Pilea (*Pilea peperomioides*) | ~76 | ~150–300 | ❌ no (below target, above ~50 floor) |

**Boundary = CONDITIONAL.** Pothos hard examples are plentiful; the *Pilea peperomioides* half is the
binding constraint at ~76 usable. Closing it needs augmentation and/or ~75–150 first-party (self-shot)
Pilea hard examples. The failure mode (pothos confidently → Pilea) is best attacked with abundant
**pothos** hard cases plus enough Pilea to anchor the decision boundary.

## Overall sprint verdict — **PARTIAL-GO**

A fine-tuning sprint **is viable** and worth committing, scoped per species:

**IN (GO) — 5 OOV new/relabel classes, license-clean data sufficient:**
`chlorophytum-comosum`, `philodendron-hederaceum`, `hoya-carnosa`, `monstera-adansonii`, `ficus-lyrata`.

**IN (CONDITIONAL) — boundary hard-example pass:**
`epipremnum-aureum` ↔ `pilea-peperomioides` — proceed with abundant pothos hard examples; supplement
the thin Pilea side (augmentation + ~75–150 self-shot) to balance the pair.

**OUT (NO-GO) — route to fallback:**
`philodendron-pink-princess` — cultivar-proven license-clean imagery is ~5–15 (below floor). Generic
*P. erubescens* does not count. → **self-shot fallback** (a variegated cultivar is trivially
first-party-photographable; ~150–200 own shots at a few plants).

### Deciding-number summary (one line each)

- chlorophytum-comosum — **790** ≥ 300 → GO
- philodendron-hederaceum — **467** ≥ 300 → GO (label-QA for pothos cross-tags)
- hoya-carnosa — **430** ≥ 300 → GO (cap cultivar-form share)
- monstera-adansonii — **369** ≥ 300 → GO (discount deliciosa/obliqua mislabels)
- ficus-lyrata — **257** ≥ 150 → GO (captive-inclusive; favour potted)
- pothos — **1470** ≥ 300 → GO (exclude fixture + true Pothos genus)
- pilea-peperomioides — **76** (≥ 50 floor, < 150 boundary) → CONDITIONAL (supplement)
- philodendron-pink-princess — **~10** < 50 → NO-GO (self-shot fallback)

**Net:** 6 of 7 targets are sourceable now (5 GO + boundary CONDITIONAL); 1 (pink-princess) needs a
first-party fallback. This is the realistic mixed outcome the plan anticipated (R2, R12), not an
all-or-nothing result.
