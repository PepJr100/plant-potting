# PLANTPOTTING-0007 Feedback

**Review date:** 2026-06-05
**Outcome:** Clean review — no bugs, no UX issues. All 11 acceptance criteria verified. One
forward-looking note for next-sprint planning (see below).

## Bugs

None.

## UX Issues

None. (This sprint had no user-facing chrome by design — the swap is a single
`ACTIVE_MODEL_ROOT` switch, not UI.)

## Verification performed during review

Static / net-free gates re-run live and GREEN:

- All required artifacts present: `model-candidate-matrix.md`, `model-swap-eval.csv`,
  `model-swap-eval-summary.md`, `results/PLANTPOTTING-0007.md`, winner bundle
  (`manifest`/`labels.csv`/`plant_class_map.json`).
- `docs/kb/ml-mapping-notes.md` has a PLANTPOTTING-0007 section.
- `per_species_thresholds: {}` (empty by design, with the anti-overfit comment).
- `PlantIdentifier` / `IdentificationResult` seam **unchanged** (git diff plan→exec is empty).
- 6 new fixtures live under `app/src/androidTest/` only (no production-APK leak), each with a
  provenance block in `LICENSE.txt`.
- `scripts/check-stub-isolation.sh` GREEN.
- `:app:verifyNoNetworking` GREEN.

Not re-run during review (rely on recorded results-doc evidence): full `testDebugUnitTest`,
`ktlintCheck`, and the `pixel6Api34` GMD instrumented suite — these need the emulator and a long
build. The user additionally confirmed the prototype on a **real device**.

## Human-judgment walkthrough — all passed

- **Survey & rejections** — sound. Named candidates correctly rejected (PlantNet-300K mobile TFLite
  exports don't exist; PlantCLEF/dima806 are ViT-scale, not bundle-fit). Off-list winner
  (`house_plant_species_mobilenetv2`, Apache-2.0) accepted.
- **Eval results** — accepted. 6/8 high-conf correct is a decisive win over AIY; the two weak spots
  (peace lily correct-but-sub-threshold 0.4468; pothos confidently confused with Pilea) are honestly
  documented and deferred.
- **Coverage claim (10/16 = 8 exact + 2 coarse)** — accepted; coarse-genus maps
  (phalaenopsis→Orchid, goeppertia→Calathea) are disclosed inline.
- **Post-close default flip** — confirmed intentional and verified. `ACTIVE_MODEL_ROOT` default was
  flipped to the new model post-sprint (2026-06-05) with gates re-run GREEN; this was a deliberate,
  documented ship decision.
- **Live G4 prototype** — accepted. GMD test drives the production `OnDevicePlantIdentifier`; user
  also tested on a real device.
- **Size impact** — accepted. +5.87 MiB raw; 42.05 MiB debug APK shipping both AIY + candidate
  bundles is within budget for now.

## Missing Features

None missing for this sprint's scope. The 6 still-out-of-vocab KB species
(monstera-adansonii, philodendron-hederaceum, philodendron-pink-princess, ficus-lyrata,
chlorophytum-comosum, hoya-carnosa) and the pothos→Pilea confusion are known gaps, intentionally
deferred (this sprint was swap-only; no training, by non-goal).

## Notes for Next Sprint

- **Gate the fine-tuning recommendation behind a data-availability spike.** The results doc
  recommends a fine-tuning sprint to close the 6 OOV species + the pothos→Pilea weakness. That is
  **moot if there's no free training data.** Before committing a fine-tuning sprint, run a cheap
  spike to answer: *is there enough CC/CC0/public-domain imagery (iNaturalist research-grade, GBIF,
  Wikimedia, Flickr-CC) for the 6 OOV species + extra pothos/Pilea hard examples?*
  - Ballpark need (transfer-learning on the existing MobileNetV2): ~50–100 images/class minimum,
    ~150–300/class comfortable; the pothos/Pilea boundary fix wants ~150–300 of *each* including
    lookalike "hard" shots. Rough total: a few hundred for a quick pass, ~1.2k–2.5k to do it well —
    plus disjoint train/val/test splits and test images **disjoint from the 8 androidTest fixtures**.
  - **If the data exists** → scope the fine-tuning sprint with sourcing included.
  - **If it doesn't** → the model swap stands as the V1 ceiling; fine-tuning is shelved unless a
    paid / self-shot data plan is approved.
  - The existing fixtures, swap-eval harness, and `ACTIVE_MODEL_ROOT` mechanism are all reusable for
    whichever path follows.
