# Query log — exact executed queries, retrieval dates, rate-limit notes

Retrieval date for all entries: **2026-06-05**. Query templates and the "usable" definition: see
`methodology.md`. Controlled vocabulary: see `species-targets.md`. Each probe is a read-only count
probe (`per_page=0` / `limit=0`), run off-device. Counts are a floor-as-of-date (R9). Portable on
Windows via `curl "<url>"` (R10). No rate-limit / throttle (HTTP 429) was encountered (R11).

## Headline methodological finding (drove the query design)

iNaturalist's `quality_grade=research` **and** `verifiable=true` both **exclude captive/cultivated**
observations (captive ⇒ "casual" grade). Houseplants are overwhelmingly captive, so a research-grade
filter undercounts them by 1–2 orders of magnitude (e.g. *Ficus lyrata* research-grade usable = **8**
vs captive-inclusive usable = **165**; *Pilea peperomioides* research-grade verifiable = **0/5** vs a
taxon lifetime of **3337** observations). For a **houseplant** classifier, captive (potted, indoor)
imagery is the *relevant* training pool. The **PRIMARY usable count is therefore iNat
captive-inclusive, photo-license-filtered** (`photos=true&photo_license=cc0,cc-by,cc-by-sa`, no
`quality_grade`/`verifiable`). Research-grade is retained as a high-label-confidence sub-figure.

## iNaturalist (api.inaturalist.org/v1/observations)

Three figures per taxon: **USABLE** = captive-incl photo-license CC0/CC-BY/CC-BY-SA; **RAW** =
captive-incl any license (`photos=true`); **NC** = CC-BY-NC (the R1 gap). `total_results` read.

| Taxon | USABLE (captive-incl) | RAW (all-grade) | %usable | NC (CC-BY-NC) | research-grade usable |
|---|---|---|---|---|---|
| Monstera adansonii | 295 | 4392 | 6.7% | 1786 | 202 |
| Philodendron hederaceum | 413 | 6081 | 6.8% | 1741 | 281 |
| Philodendron erubescens (generic, excluded) | 151 | 3052 | 4.9% | 433 | 67 |
| Ficus lyrata | 165 | 7490 | 2.2% | 293 | 8 |
| Chlorophytum comosum | 664 | 18844 | 3.5% | 1138 | 189 |
| Hoya carnosa | 246 | 6050 | 4.1% | 1388 | 138 |
| Epipremnum aureum (pothos) | 1178 | 36841 | 3.2% | 6716 | 755 |
| Pilea peperomioides | 33 | 3301 | 1.0% | ~3268 (raw−usable) | 0 |

Exact URL forms (substitute the URL-encoded taxon name):
- USABLE: `https://api.inaturalist.org/v1/observations?taxon_name=<NAME>&photos=true&photo_license=cc0,cc-by,cc-by-sa&per_page=0`
- RAW:    `https://api.inaturalist.org/v1/observations?taxon_name=<NAME>&photos=true&per_page=0`
- NC:     `https://api.inaturalist.org/v1/observations?taxon_name=<NAME>&verifiable=true&photos=true&photo_license=cc-by-nc&per_page=0`
- research-grade: `…&quality_grade=research&photos=true&photo_license=cc0,cc-by,cc-by-sa&per_page=0`
- Pilea taxon check: `https://api.inaturalist.org/v1/taxa?q=Pilea%20peperomioides&rank=species` → `id=125439, observations_count=3337`

> **R1 confirmed:** only **1.0–6.8%** of iNat houseplant imagery is license-clean. CC-BY-NC dominates
> every target. The usable pool is the small CC0/CC-BY/CC-BY-SA slice, not the raw total.

## GBIF (api.gbif.org/v1/occurrence/search) — cross-check / dedup anchor

`mediaType=StillImage`, usable license facet `license=CC0_1_0&license=CC_BY_4_0`. `count` read.
**Caveat:** GBIF's `license` is the **occurrence-record** license, not the photo license — for
iNat-sourced records it tracks the observation (often CC-BY) even when the photo is NC, so GBIF
**over-counts** vs photo-clean. GBIF re-publishes iNat, so its *additive* contribution over iNat is
treated as **~0**; used to corroborate, not to sum.

| Taxon | GBIF usable (occurrence-license) |
|---|---|
| Monstera adansonii | 2596 |
| Philodendron hederaceum | 1428 |
| Philodendron erubescens | 138 |
| Ficus lyrata | 290 |
| Chlorophytum comosum | 882 |
| Hoya carnosa | 377 |
| Epipremnum aureum | 965 |
| Pilea peperomioides | 25 |

URL: `https://api.gbif.org/v1/occurrence/search?scientificName=<NAME>&mediaType=StillImage&license=CC0_1_0&license=CC_BY_4_0&limit=0`

## Wikimedia Commons (commons.wikimedia.org/w/api.php) — direct category file count

`prop=categoryinfo` `categoryinfo.files` (direct members; **subcats not counted ⇒ lower bound**).
Commons files are uniformly CC0/CC-BY/CC-BY-SA/PD ⇒ ~all usable, and per-file author/date/source/
permalink/license are API-exposed (`prop=imageinfo&iiprop=extmetadata`) ⇒ **attribution feasible = Y**
(the existing 8 fixtures prove the provenance-block pattern against Commons).

| Category | files | subcats |
|---|---|---|
| Monstera adansonii | 71 | 4 |
| Philodendron hederaceum | 53 | 2 |
| Ficus lyrata | 50 | 2 |
| Chlorophytum comosum | 53 | 6 |
| Hoya carnosa | 105 | 3 |
| Epipremnum aureum | 134 | 8 |
| Pilea peperomioides | 34 | 0 |
| Philodendron erubescens (generic) | 24 | 2 |
| **Philodendron erubescens 'Pink Princess'** (cultivar) | **4** | 0 |

URL: `https://commons.wikimedia.org/w/api.php?action=query&titles=Category:<NAME>&prop=categoryinfo&format=json`
Pink Princess loose full-text cross-check: `…&list=search&srsearch="Pink Princess" Philodendron&srnamespace=6&srlimit=0` → `totalhits=11`.
**Discarded as noise:** full-text `list=search` `totalhits` for *Chlorophytum comosum* returned
**77304** (loose multi-field match) — category file count (53) used instead.

## Flickr (flickr.com/search, CC/PD license filter `4,5,7,9,10`) — supplementary, LOW confidence

No API key in this spike ⇒ public license-filtered search-UI "View all N" estimate. Common-name
free-text (no taxon binding) ⇒ noisy, **needs manual label verification**; counted at low confidence
and label-discounted in the synthesis.

| Search text | CC/PD estimate |
|---|---|
| Monstera adansonii | ~5 |
| Philodendron hederaceum | ~2 |
| Ficus lyrata | ~43 |
| Chlorophytum comosum | ~78 |
| Hoya carnosa | ~79 |
| Epipremnum aureum | ~160 |
| Pilea peperomioides | ~17 |
| Philodendron Pink Princess | ~0–1 (only a stray thumbnail; no usable count) |

URL: `https://www.flickr.com/search/?text=<TEXT>&license=4,5,7,9,10`

## Eyeball sample

No off-repo eyeball sample was downloaded this pass — the per-source taxon-bound counts plus the
cultivar/lookalike pitfalls (recorded per row) were sufficient to reach the verdicts. The future
fine-tune sprint will run a manual label-QA pass at download time (see `finetune-sprint-outline.md`).
The cultivar question for `philodendron-pink-princess` was answered structurally (no cultivar taxon
on iNat; Commons cultivar category = 4 files; Flickr ~0) rather than by sampling.
