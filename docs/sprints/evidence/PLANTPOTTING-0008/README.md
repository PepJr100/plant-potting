# PLANTPOTTING-0008 — Training-data availability spike (evidence)

This directory is the evidence package for the **desk-research feasibility spike** that decides
whether a later **fine-tuning sprint** for the production houseplant model
(`house_plant_species_mobilenetv2`, MobileNetV2, 47 classes, the `ACTIVE_MODEL_ROOT` default since
PLANTPOTTING-0007) is viable. It mirrors the structure PLANTPOTTING-0007 used for its model survey.

## What this spike IS

A per-species, per-source assessment of how much **free, license-clean, correctly-identified,
attribution-feasible** imagery exists for the KB species the production model **cannot name**, so a
future fine-tune can build disjoint train/val/test splits. The primary artifact is a written
**GO / CONDITIONAL / NO-GO** report backed by real image **counts**.

## What this spike is NOT — the no-download rule

**No bulk download. No images committed. No model training.** Counts are gathered through the
sources' own APIs / UI only. The single permitted exception is a *tiny* (≤~20 images/species)
manual eyeball sample to sanity-check label quality and the cultivar question — and that sample
lives in an **off-repo scratch dir outside the project tree** and is **never** staged. The local
`.gitignore` here is belt-and-braces against an accidental media commit. The committed deliverables
are **text reports only**.

## Sources assessed (license-clean = CC0 / CC-BY / CC-BY-SA / PD)

`CC-BY-NC` and `CC-BY-ND` are **recorded but excluded** from the usable count (NC is unusable for a
commercial-app model; ND forbids the derivative a fine-tune effectively creates).

- **iNaturalist** — research-grade observations; per-photo CC license (distinct from observation
  license). The largest volume, but heavily CC-BY-NC.
- **GBIF** — aggregates iNat + herbaria; used as a media-bearing cross-check and for taxon-key /
  synonym resolution. De-duped against iNat (GBIF mirrors most iNat media).
- **Wikimedia Commons** — CC0 / CC-BY-SA / PD; the source the 8 existing test fixtures already use,
  so the provenance-block pattern is proven. Lower volume, higher label quality.
- **Flickr (CC-licensed)** — license-filtered search; noisier common-name labels → flagged
  "needs manual label verification".

## Verdict vocabulary

Applied per species against license-clean **usable** counts (the deciding number is always shown):

| Verdict | Threshold | Meaning |
|---|---|---|
| **GO** (comfortable) | ~150–300+ usable images | Enough for a healthy transfer-learning split. |
| **CONDITIONAL** (floor) | ~50–100 usable | Viable but thin — augmentation / few-shot risk noted. |
| **NO-GO** (below floor) | < ~50 usable | Route to paid-dataset / self-shot fallback. |
| **Boundary** | ~150–300 of EACH of pothos & Pilea, incl. lookalike hard shots | Pair assessment, not a new class. |

An overall sprint verdict supports **partial-GO**: GO species get a fine-tune outline; NO-GO species
get a fallback memo — both in `finetune-sprint-outline.md`.

## Files

| File | Role |
|---|---|
| `species-targets.md` | Controlled vocabulary: KB id, accepted name, synonyms, lookalikes, and the 8-fixture disjoint-split exclusion set. **Gates the research** — every query uses these names. |
| `methodology.md` | Query template per source, "usable" definition, dedup method, count-confidence rubric, threshold definitions. |
| `source-counts.csv` | Machine-readable mirror of every counted row (species × source × license-bucket). |
| `query-log.md` | Exact executed query URLs + retrieval dates + any rate-limit notes. |
| `data-availability-report.md` | Per-species tables + pothos/Pilea boundary sub-table (Phase 2). |
| `go-no-go-matrix.md` | Per-species verdict roll-up + overall sprint verdict (Phase 2). |
| `finetune-sprint-outline.md` | Per-species hand-off: fine-tune outline (GO) / fallback memo (NO-GO) (Phase 2). |

Retrieval date for all counts: **2026-06-05** (counts are a floor-as-of-date; iNat/Commons grow daily — see R9).
