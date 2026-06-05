# Methodology — how counts are gathered, de-duped, and scored

Retrieval date for all Phase-1 counts: **2026-06-05** (counts are a floor-as-of-date; see R9).
All queries are **read-only count probes** (`per_page=0` / `limit=0` where supported), throttled,
and run **off-device / off-build-pipeline** (R6, R11). The exact executed URLs + any rate-limit
notes are logged in `query-log.md`. These probes are portable on Windows via `curl` (R10).

## "License-clean / usable" definition

| License | Counts toward usable? | Rationale |
|---|---|---|
| CC0 / Public Domain | ✅ yes | No restriction. |
| CC-BY | ✅ yes | Attribution only — feasible via a provenance block (see fixture `LICENSE.txt` pattern). |
| CC-BY-SA | ✅ yes | Attribution + share-alike — the model weights aren't a derivative *work* of any single image in the copyright sense; share-alike obligations attach to the image, satisfied by attribution + license record. (Same basis the existing CC-BY-SA fixtures rely on.) |
| **CC-BY-NC** | ❌ recorded, excluded | Non-commercial — unusable for a commercial-app model. Reported separately so the gap is explicit (R1). |
| **CC-BY-ND** | ❌ recorded, excluded | No-derivatives — a fine-tune is a derivative use. |
| All rights reserved / no license | ❌ excluded | Not license-clean. |

**Observation-license vs photo-license (iNat):** iNaturalist licenses the *observation* and each
*photo* separately, and they often differ. Only the **photo license** governs image reuse, so the
usable count filters on **`photo_license`**, never the observation `license`.

## Query template per source

> Substitute `<NAME>` with the accepted scientific name (URL-encoded) from `species-targets.md`.
> `<USABLE>` = the usable-license set for that source's filter syntax.

### iNaturalist (API v1)
- **Usable count:**
  `https://api.inaturalist.org/v1/observations?taxon_name=<NAME>&quality_grade=research&photos=true&photo_license=cc0,cc-by,cc-by-sa&per_page=0`
  → read `total_results`.
- **Raw count (any/no license, media-backed):** same URL without `photo_license`.
- **NC count (for the R1 gap):** `&photo_license=cc-by-nc`.
- Filters: `quality_grade=research` (community-verified ID), `photos=true` (media-backed),
  `taxon_name` (binds to the iNat taxon, not a free-text tag → reduces mislabels).
- **Caveat:** `total_results` counts *observations*, and a research-grade observation usually has
  ≥1 usable photo; treat observation count as a **lower bound** on photo count (we count
  conservatively at the observation level).

### GBIF (API v1)
- **Usable count:**
  `https://api.gbif.org/v1/occurrence/search?scientificName=<NAME>&mediaType=StillImage&license=CC0_1_0&license=CC_BY_4_0&license=CC_BY_SA_4_0&limit=0`
  → read `count`.
- **Raw media count:** same without the `license` params.
- **Role:** GBIF aggregates iNat + herbaria + others. Used as a **cross-check and dedup anchor**,
  not an independent additive source — most GBIF *StillImage* houseplant occurrences **mirror iNat**
  (same `occurrenceID`/observation). See dedup below.
- **Caveat:** GBIF's `license` facet applies to the **occurrence record**, which for iNat-sourced
  records tracks the photo license; for herbarium records it may differ. Confidence downgraded
  where the per-image license can't be confirmed.

### Wikimedia Commons (MediaWiki API)
- **File-namespace search count:**
  `https://commons.wikimedia.org/w/api.php?action=query&list=search&srsearch=<NAME>&srnamespace=6&srlimit=0&format=json`
  → read `query.searchinfo.totalhits`.
- Commons files are uniformly CC0 / CC-BY / CC-BY-SA / PD (NC/ND are out of scope for Commons), so
  the file-namespace hit count is **almost entirely usable** — but it includes diagrams, herbarium
  scans, and duplicates, so it is **raw**; usable is discounted by a label-quality factor (rubric
  below). Per-file license + full provenance (author/date/source/permalink) are API-exposed
  (`prop=imageinfo&iiprop=extmetadata`) → **attribution feasible = Y** for every Commons row (the
  fixture pattern is already proven against Commons).

### Flickr (CC-licensed)
- **License-filtered search (API):**
  `https://api.flickr.com/services/rest/?method=flickr.photos.search&text=<NAME>&license=1,2,4,5,7,9,10&content_type=1&per_page=1&format=json&nojsoncallback=1&api_key=<KEY>`
  → read `photos.total`. License ids: 1=CC-BY-NC-SA, 2=CC-BY-NC, 4=CC-BY, 5=CC-BY-SA, 7=PD-mark,
  9=CC0, 10=PD-Mark. **Usable filter = `4,5,7,9,10`** (drop the NC ids 1,2,3,6).
- **No API key available in this spike** → Flickr is assessed via the **public license-filtered
  search UI** result count as an estimate, flagged **"needs manual label verification"** (common-name
  free-text tagging is noisy; no taxon binding). Counts recorded at **low** confidence.

## Cross-source de-duplication

- **iNat ⊃ GBIF overlap:** GBIF re-publishes iNat research-grade media. To avoid double-counting,
  **iNat is the primary count; GBIF is reported separately and its *additive* contribution is the
  non-iNat remainder** (herbaria, museum collections). When GBIF's media count ≈ iNat's, the
  additive GBIF contribution is treated as **~0** and noted as such.
- **Dedup key:** observation/occurrence id (`gbifID` ↔ iNat `id` via `occurrenceID`), and for
  Commons, the file `pageid`. The future fine-tune de-dupes by these ids at download time.
- **Total usable** per species = iNat-usable + Commons-usable + Flickr-usable (label-discounted) +
  GBIF-additive (usually ~0). The boundary pair sums pothos and Pilea **separately**.

## Count-confidence rubric

Per species/source row, classify **high / medium / low**:

- **high** — taxon-bound query (iNat `taxon_name`, GBIF `scientificName`), per-image license
  verifiable, low duplicate risk, low mislabel risk.
- **medium** — license verifiable but some mislabel/cultivar risk (cross-tagged lookalikes), or
  aggregate (not strictly per-image) license, or moderate dedup uncertainty.
- **low** — free-text/common-name query (Flickr), per-image license not individually confirmed, or
  high mislabel/cultivar contamination (e.g. pink-princess generic-vs-cultivar).

## Thresholds (the deciding number is always shown)

- **GO (comfortable):** ~150–300+ license-clean usable images/species.
- **CONDITIONAL (floor):** ~50–100/species (viable but thin — augmentation / few-shot risk).
- **NO-GO (below floor):** < ~50 usable/species.
- **Boundary:** ~150–300 of **EACH** of pothos and Pilea, including lookalike hard shots.

## Eyeball-sample rule (off-repo)

Optional ≤~20 images/species visual check to estimate mislabel rate and (for pink-princess) the
cultivar question. Sample is downloaded to a local scratch dir **outside the project tree**, never
staged; each sample is noted in `query-log.md`. No sample is required to produce a count — counts
come from the API/UI probes above.
