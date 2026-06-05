# Data-availability report — per-species sourcing (PLANTPOTTING-0008)

**Assess-only spike. No images downloaded or committed.** Counts retrieved **2026-06-05** (floor-as-of-date, R9).
Machine-readable mirror: `source-counts.csv`. Exact queries: `query-log.md`. Vocabulary: `species-targets.md`.

## How to read the counts

- **Usable (license-clean)** = CC0 / CC-BY / CC-BY-SA / PD. **CC-BY-NC and CC-BY-ND are excluded.**
- **iNaturalist is the primary, photo-license-verified source.** The iNat usable figure is
  **captive-inclusive** (`photos=true&photo_license=cc0,cc-by,cc-by-sa`, no `quality_grade`): a
  houseplant classifier wants potted/indoor (captive) shots, and iNat marks those "casual" — a
  research-grade filter would drop them (e.g. *Ficus lyrata* research-grade = 8 vs captive-inclusive = 165).
- **GBIF** is a **cross-check only** — its `license` is occurrence-level (over-counts vs photo-clean)
  and it re-publishes iNat, so its additive contribution is treated as **~0** (not summed).
- **Commons** = direct category file count (subcats not counted ⇒ lower bound); ~all license-clean.
- **Flickr** = CC/PD search-UI estimate; **low confidence, needs label verification**; label-discounted.
- **Total usable (deduped est.)** = iNat(captive-incl) + Commons + ~½·Flickr (label discount). GBIF not added.

## R1 — the license gap (why raw ≫ usable)

Only **1.0–6.8%** of iNat houseplant imagery is license-clean; CC-BY-NC dominates. The raw pool is
huge for every target (existence is not the problem — *license* is), so the threshold is applied to
the **usable** slice, never the raw total.

| Species | iNat raw (all-grade) | iNat usable | % usable |
|---|---:|---:|---:|
| Monstera adansonii | 4392 | 295 | 6.7% |
| Philodendron hederaceum | 6081 | 413 | 6.8% |
| Ficus lyrata | 7490 | 165 | 2.2% |
| Chlorophytum comosum | 18844 | 664 | 3.5% |
| Hoya carnosa | 6050 | 246 | 4.1% |
| Epipremnum aureum (pothos) | 36841 | 1178 | 3.2% |
| Pilea peperomioides | 3301 | 33 | 1.0% |

---

## OOV species

Columns: **source · usable (license-clean) · raw · license type(s) · attribution feasible · pitfall · cultivar-specific · count-confidence · disjoint-split achievable**

### 1. `monstera-adansonii` — GO (~369 usable)

| source | usable | raw | license(s) | attrib. | pitfall | cultivar | conf. | split |
|---|---:|---:|---|:--:|---|:--:|:--:|:--:|
| iNaturalist | 295 | 4392 | CC0/BY/BY-SA | Y | vs M. deliciosa & rare M. obliqua mislabels (~10–15%) | n-a | med | Y |
| Wikimedia Commons | 71 | — | CC0/BY/BY-SA/PD | Y | +4 subcats | n-a | high | Y |
| Flickr | ~5 | — | CC/PD | Y | label-verify | n-a | low | Y |
| GBIF (cross-check) | (2596) | — | CC0/BY occ-lic | partial | mirrors iNat | n-a | low | Y |

**Disjoint-split:** ~369 usable → e.g. 260/55/54; no fixture overlap (*M. deliciosa* fixture is a
different species). Discount ~10–15% for deliciosa/obliqua mislabels → still well above GO floor.

### 2. `philodendron-hederaceum` — GO (~467 usable)

| source | usable | raw | license(s) | attrib. | pitfall | cultivar | conf. | split |
|---|---:|---:|---|:--:|---|:--:|:--:|:--:|
| iNaturalist | 413 | 6081 | CC0/BY/BY-SA | Y | cross-tagged with *Epipremnum aureum* (pothos) | n-a | med | Y |
| Wikimedia Commons | 53 | — | CC0/BY/BY-SA/PD | Y | +2 subcats | n-a | high | Y |
| Flickr | ~2 | — | CC/PD | Y | label-verify | n-a | low | Y |
| GBIF (cross-check) | (1428) | — | CC0/BY occ-lic | partial | mirrors iNat | n-a | low | Y |

**Disjoint-split:** ~467 → 330/70/67. **Cross-tag risk is the headline pitfall** — this is the
canonical pothos lookalike, and it is one half of why the model confuses pothos↔Pilea. The fine-tune
must run a label-QA pass to strip pothos-mislabelled records (estimate via a small eyeball sample).

### 3. `philodendron-pink-princess` — **NO-GO** (cultivar-proven ~5–15)

| source | usable (cultivar) | raw | license(s) | attrib. | pitfall | cultivar | conf. | split |
|---|---:|---:|---|:--:|---|:--:|:--:|:--:|
| iNaturalist | **0** | (generic erubescens 151) | — | n-a | no cultivar rank on iNat — cannot isolate the pink cultivar | N | low | N |
| Wikimedia Commons | **4** | — | CC0/BY/BY-SA/PD | Y | category 'Pink Princess' = 4 files | **Y** | med | N |
| Commons full-text (loose) | 11 | — | — | Y | includes non-cultivar mentions | partial | low | N |
| Flickr | ~0–1 | — | CC/PD | Y | no usable cultivar-proven result | partial | low | N |

**The cultivar question, answered explicitly:** license-clean imagery of the *pink-variegated
cultivar specifically* is **~5–15 images total** across all sources. Generic *Philodendron erubescens*
(151 usable on iNat) is **excluded** — a green wild-type photo does not teach pink variegation, and
'Pink Congo' (fake variegation) actively poisons a free-text count. **Below the ~50 floor → NO-GO.**
Routed to the self-shot fallback (`finetune-sprint-outline.md`).

### 4. `ficus-lyrata` — GO (~257 usable)

| source | usable | raw | license(s) | attrib. | pitfall | cultivar | conf. | split |
|---|---:|---:|---|:--:|---|:--:|:--:|:--:|
| iNaturalist | 165 | 7490 | CC0/BY/BY-SA | Y | **research-grade only = 8** — GO depends on captive inclusion; filter tree-form vs potted bush | n-a | med | Y |
| Wikimedia Commons | 50 | — | CC0/BY/BY-SA/PD | Y | +2 subcats | n-a | high | Y |
| Flickr | ~43 | — | CC/PD | Y | label-verify | n-a | low | Y |
| GBIF (cross-check) | (290) | — | CC0/BY occ-lic | partial | mirrors iNat | n-a | low | Y |

**Disjoint-split:** ~257 → 180/40/37. **Caveat flagged:** the GO rests entirely on captive-inclusive
counts (research-grade is near-empty because the fiddle-leaf fig is almost exclusively cultivated).
Confidence medium; the fine-tune should prefer potted/indoor shots over mature outdoor tree-form.

### 5. `chlorophytum-comosum` — GO (~790 usable)

| source | usable | raw | license(s) | attrib. | pitfall | cultivar | conf. | split |
|---|---:|---:|---|:--:|---|:--:|:--:|:--:|
| iNaturalist | 664 | 18844 | CC0/BY/BY-SA | Y | variegated vs all-green cultivar split | n-a | med-high | Y |
| Wikimedia Commons | 53 | — | CC0/BY/BY-SA/PD | Y | +6 subcats; full-text totalhits 77304 discarded as noise | n-a | high | Y |
| Flickr | ~78 | — | CC/PD | Y | label-verify | n-a | low | Y |
| GBIF (cross-check) | (882) | — | CC0/BY occ-lic | partial | mirrors iNat | n-a | low | Y |

**Disjoint-split:** ~790 → 550/120/120. Strongest OOV target. Include both variegated ('Vittatum'/
'Variegatum') and all-green forms so the class generalises.

### 6. `hoya-carnosa` — GO (~430 usable)

| source | usable | raw | license(s) | attrib. | pitfall | cultivar | conf. | split |
|---|---:|---:|---|:--:|---|:--:|:--:|:--:|
| iNaturalist | 246 | 6050 | CC0/BY/BY-SA | Y | cultivar forms (compacta/variegata/Krimson) may not represent base class | n-a | med | Y |
| Wikimedia Commons | 105 | — | CC0/BY/BY-SA/PD | Y | +3 subcats | n-a | high | Y |
| Flickr | ~79 | — | CC/PD | Y | label-verify | n-a | low | Y |
| GBIF (cross-check) | (377) | — | CC0/BY occ-lic | partial | mirrors iNat | n-a | low | Y |

**Disjoint-split:** ~430 → 300/65/65. Cap the share of twisted/variegated cultivar forms so the base
*H. carnosa* class isn't dominated by Hindu-rope ('Compacta') morphology.

---

## Boundary sub-table — pothos ↔ Pilea (hard examples, EACH side ~150–300)

This is **not a new class** — both classes already exist in the production model. The fine-tune adds
varied **hard-example** imagery to each existing class to break the pothos→Pilea misclassification.

| side | source | usable | raw | license(s) | attrib. | pitfall | conf. | split |
|---|---|---:|---:|---|:--:|---|:--:|:--:|
| **Pothos** (*Epipremnum aureum*) | iNaturalist | 1178 | 36841 | CC0/BY/BY-SA | Y | exclude fixture `epipremnum-aureum.jpg` + true *Pothos*-genus | med | Y |
| Pothos | Wikimedia Commons | 134 | — | CC0/BY/BY-SA/PD | Y | +8 subcats; exclude `Golden_Pothos_(Money_Plant).jpg` | high | Y |
| Pothos | Flickr | ~160 | — | CC/PD | Y | label-verify | low | Y |
| **Pothos total** | | **~1470** | | | | | | **GO** |
| **Pilea** (*Pilea peperomioides*) | iNaturalist | 33 | 3301 | CC0/BY/BY-SA | Y | 1.0% usable — license is the killer, not existence | med | Y |
| Pilea | Wikimedia Commons | 34 | — | CC0/BY/BY-SA/PD | Y | exclude generic genus *Pilea* (P. cadierei etc.) | high | Y |
| Pilea | Flickr | ~17 | — | CC/PD | Y | label-verify | low | Y |
| **Pilea total** | | **~76** | | | | | | **below 150–300 target** |

**Boundary verdict: CONDITIONAL.** Pothos hard examples are abundant (~1470). *Pilea peperomioides*
license-clean imagery is **~76** — above the absolute ~50 floor but **below the ~150–300 boundary
target**. The Pilea half is the binding constraint: it would need augmentation and/or ~75–150
first-party (self-shot) hard examples to reach a balanced pair. Note the asymmetry is benign for the
*failure mode* (pothos→Pilea): the model already over-predicts Pilea, so the higher-value examples are
**pothos** hard cases (abundant) plus enough Pilea to anchor the boundary.

**Pilea data note:** the production model's class is *Pilea peperomioides* (Chinese Money Plant)
specifically; generic genus *Pilea* is excluded. The taxon has 3337 lifetime iNat observations but
only 33 are CC-clean (it barely grows outside cultivation; almost all observations are captive and
NC/all-rights-reserved).

---

## Disjoint-split feasibility — summary

Every GO/CONDITIONAL species can form train/val/test splits **disjoint from the 8 existing
`identify-fixtures/` images** by source observation/file id. **Only `epipremnum-aureum.jpg` overlaps a
target** (the boundary pothos); its Commons source URL
`File:Golden_Pothos_(Money_Plant).jpg` is excluded from the pothos count and must be held out of
pothos training. The other 7 fixtures depict non-target species and impose no constraint on these counts.

## Attribution feasibility — summary

- **iNaturalist & Wikimedia Commons:** author / source page / permalink / date / license / attribution
  string are **API-exposed** (`prop=imageinfo&iiprop=extmetadata` on Commons; observation+photo JSON on
  iNat) ⇒ a fixture-style provenance block (`identify-fixtures/LICENSE.txt`) can be **auto-generated at
  download time**. Attribution feasible = **Y** for every iNat/Commons row.
- **GBIF:** provenance present but occurrence-level licensing is unreliable for photo reuse ⇒ resolve
  back to the iNat/Commons original before trusting the license.
- **Flickr:** per-photo owner + license fields exist (attribution feasible), but **labels** need manual
  verification (common-name tagging, no taxon binding).
