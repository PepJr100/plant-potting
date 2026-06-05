# Fine-tune sprint outline (conditional hand-off) — PLANTPOTTING-0008

Scoped per species from the **PARTIAL-GO** verdict (`go-no-go-matrix.md`). This is the hand-off a
future fine-tuning sprint would expand into a plan. **PLANTPOTTING-0008 trains nothing** — this
outline is the deliverable, not an instruction to act now.

## Shared approach (all GO/CONDITIONAL species)

**Base model.** Transfer-learn on the *existing* production model
`app/src/main/assets/ml/house_plant_species_mobilenetv2/model.tflite` (MobileNetV2, 47 classes,
Apache-2.0, the `ACTIVE_MODEL_ROOT` default since PLANTPOTTING-0007). Recover the upstream Keras `.h5`
(see `../PLANTPOTTING-0007/ACQUISITION.md`), add/relabel head classes, fine-tune, re-export TFLite.

**Head surgery.**
- **+5 new head classes** for the OOV GO species (monstera-adansonii, philodendron-hederaceum,
  hoya-carnosa, chlorophytum-comosum, ficus-lyrata) → 47 → **52 classes**.
- **Boundary** (pothos ↔ Pilea): **no new class** — add hard examples to the existing
  `Pothos (Ivy arum)` and `Chinese Money Plant (Pilea peperomioides)` classes to break the
  pothos→Pilea confusion.
- pink-princess: **not** a head class from web data → fallback (below).

**Training regime.** Frozen-backbone transfer first (train only the new head + a thin classifier),
then optionally unfreeze the top MobileNetV2 block for a low-LR full fine-tune if the new classes
underperform. Class-balanced sampling + augmentation (the new classes are 150–800 each vs the
existing 47 classes' larger counts).

**Export.** Re-export **float16** TFLite first (FLOAT32 input — fits the app's existing FLOAT32
preprocessing branch, exactly as 0007 shipped). Evaluate an **INT8** export as a size/latency option
only if float16 misses the device budget; INT8 needs a representative-dataset calibration pass and a
UINT8 input branch (already present per the 0007 code path).

**Sourcing / download plan (per source, with attribution capture).**
- **iNaturalist** (primary): pull captive-inclusive, `photo_license=cc0,cc-by,cc-by-sa`, per **taxon
  id**; capture per-photo author / license / observation URL / date from the photo JSON →
  auto-generate a provenance block. Throttle (R11).
- **Wikimedia Commons** (secondary): pull category members + subcats; capture
  `imageinfo/extmetadata` (author, license, source page, date) → provenance block. Proven pattern
  (the 8 existing fixtures are Commons-sourced).
- **Flickr** (top-up only, GO species with headroom): license `4,5,7,9,10`; **manual label-QA
  required** before use (common-name tagging).
- **GBIF**: not a download source — resolve back to the iNat/Commons original for a trustworthy photo
  license.

**Disjoint splits.** De-dupe across sources by observation/file id; build train/val/test **disjoint
from the 8 `identify-fixtures/` images** (exclude by source id). **Hold out
`File:Golden_Pothos_(Money_Plant).jpg` (= `epipremnum-aureum.jpg`) from pothos training.** Reserve a
few license-clean held-out images per new class as **new eval fixtures** under
`app/src/androidTest/assets/identify-fixtures/` following the existing `LICENSE.txt` provenance
pattern (480×480, JPEG q80, full attribution block).

**Eval / wiring reuse (no seam change).** Reuse `ModelSwapEvaluationTest` to score the fine-tuned
bundle against the held-out fixtures; bump `ACTIVE_MODEL_ROOT` to the new bundle root behind the same
single BuildConfig switch; keep `plant_class_map.json` the resolution path. The
`PlantIdentifier.identify(jpeg): IdentificationResult` seam stays frozen — a fine-tune is a pure
asset+manifest+label-map swap, exactly the 0007 shape.

---

## Per-species hand-off

### GO — full outline applies

| Species | New class? | Usable | Sourcing emphasis | Specific risk to manage |
|---|:--:|---:|---|---|
| `chlorophytum-comosum` | +1 | ~790 | iNat-led; include variegated + all-green | cultivar morphology balance |
| `philodendron-hederaceum` | +1 | ~467 | iNat-led | **label-QA: strip pothos cross-tags** (eyeball-sample mislabel rate; discount) |
| `hoya-carnosa` | +1 | ~430 | iNat + Commons (105) | **cap twisted/variegated cultivar share** so base form dominates |
| `monstera-adansonii` | +1 | ~369 | iNat-led | discount *M. deliciosa* / rare *M. obliqua* mislabels (~10–15%) |
| `ficus-lyrata` | +1 | ~257 | iNat captive + Flickr (43) | **favour potted/indoor over mature tree-form**; GO rests on captive inclusion |

### CONDITIONAL — boundary hard-example pass (pothos ↔ Pilea)

- **Pothos (`epipremnum-aureum`)** — ~1470 usable. Pull a **varied hard-example** subset (trailing
  vines, juvenile leaves, low-variegation leaves that look Pilea-ish). Exclude the fixture URL and
  true *Pothos*-genus records. No new class.
- **Pilea (`pilea-peperomioides`)** — **binding constraint at ~76 usable.** Pull all ~76 license-clean
  images, then **supplement**: (a) augmentation (rotations/crops/lighting), and (b) **~75–150 first-party
  self-shot** Pilea hard examples (trivially attributable; first-party). Target a balanced ~150–300
  pair. No new class — add to the existing Pilea head.
- **Acceptance:** post-fine-tune, the pothos fixture (`epipremnum-aureum.jpg`, held out) must classify
  as pothos (not Pilea) at high confidence in `ModelSwapEvaluationTest`.

### NO-GO — fallback memo

#### `philodendron-pink-princess` — self-shot fallback (recommended)

- **Why web data fails:** cultivar-proven license-clean imagery is **~5–15 total** (Commons cultivar
  category = 4; Flickr ~0; iNat has no cultivar rank — generic *P. erubescens* = 151 but is the green
  wild type and is **excluded**). 'Pink Congo' (fake variegation) poisons any free-text count.
- **Self-shot plan (primary):** photograph **2–4 Pink Princess plants** (sourced from a garden
  centre / owned) → **~150–200 images** across leaves/whole-plant/angles/lighting, emphasising the
  diagnostic pink variegation and the reverted-green contrast. First-party ⇒ attribution trivial
  (own work, license of choice), label certainty 100%. This single afternoon yields more
  cultivar-proven data than the entire CC web.
- **Paid-dataset option (not recommended):** no known CC or paid dataset targets the *Pink Princess
  cultivar* specifically; generic stock houseplant datasets (e.g. commercial plant-ID training sets,
  ~$ unknown) would still need manual cultivar filtering and don't solve the scarcity. Self-shot is
  cheaper and higher quality.
- **Covers:** the pink-princess class only. The other 6 targets do **not** depend on this fallback.
- **Sequencing:** can run in the same fine-tune sprint (self-shot batch feeds the +1 head for
  pink-princess), or be deferred — it does not block the 5 GO classes or the boundary pass.

---

## Suggested fine-tune sprint shape (for the planner)

1. **Sourcing + label-QA** (per the plan above; ~5 GO classes + boundary hard examples + self-shot
   pink-princess batch).
2. **Disjoint splits + new held-out fixtures** (provenance blocks; exclude the 8 existing + the pothos
   fixture URL).
3. **Transfer-learn** 47 → 52 classes + boundary hard examples; float16 export (INT8 fallback).
4. **Eval** via `ModelSwapEvaluationTest`; **gate** on the pothos→Pilea fix + new-class accuracy.
5. **Swap** `ACTIVE_MODEL_ROOT`; KB/seam unchanged; `verifyNoNetworking` + stub-isolation stay GREEN.
