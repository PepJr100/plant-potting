---
last_updated: 2026-06-05
through_sid: PLANTPOTTING-0007
---

# ROADMAP

One-page snapshot of where PlantPotting stands and what's next. Sprint history lives
in [`docs/sprints/`](sprints/); this file is the current view.

> Maintained by the `/roadmap` skill (INIT / REFRESH / BUMP). The frontmatter above is
> parsed by routing — `last_updated` and `through_sid` drive when a REFRESH is due.

## Current state (post-PLANTPOTTING-0007)

**The houseplant model swap shipped — V1 is open.** The AIY Plants V1/3 model (wild-flora-tuned,
2 of 16 KB species in-vocab) has been replaced as the production default by
**`house_plant_species_mobilenetv2`** (MobileNetV2, 47 house-plant classes, Apache-2.0, converted
out-of-band to float16 TFLite, 10.42 MiB). On the expanded 8-fixture probe it **decisively beats
AIY**: top-1 high-confidence-correct **1 → 6**, KB-vocabulary coverage **2/16 → 10/16** (8 exact +
2 coarse-genus), and median inference latency actually *drops* **43 ms → 33 ms**. The swap rides a
single `ACTIVE_MODEL_ROOT` switch — the `PlantIdentifier` / `IdentificationResult` seam (0003 §4.4)
is **unchanged**, the candidate's `[1,47]` FLOAT32 output fits the existing `ModelScoreMapper` and
its FLOAT32 input fits the existing `ImagePreprocessor` branch. AIY stays bundled as the regression
anchor (`OnDeviceModelRealInterpreterTest` pins it). A live `pixel6Api34` GMD run *and* a real-device
check identify houseplants directly through the production `OnDevicePlantIdentifier` — **not a paper
spike.**

`per_species_thresholds` ships **empty by design** (the 0006 anti-overfit discipline holds): the 6
high-confidence hits clear the global 0.55 outright, peace lily (correct top-1 @ 0.4468) is **not**
seeded to bless a sub-50% prediction, and pothos is confidently *wrong* (→ Pilea), not a threshold
case. `verifyNoNetworking` + `check-stub-isolation.sh` stayed GREEN throughout (conversion lives
out-of-band under `evidence/`, never in Gradle). PLANTPOTTING-0007 is closed.

**The swap's result is the new headline:** the model now recognises common houseplants, but **6 of
16 KB species remain out-of-vocab** (monstera-adansonii, both Philodendrons, ficus-lyrata,
chlorophytum-comosum, hoya-carnosa) and **pothos is confidently confused with Pilea**. Closing those
needs *training*, which 0007 deliberately did not do (swap-only). The next step is a **cheap
data-availability spike** — is there free CC/CC0 training imagery for those species? — that **gates**
whether a fine-tuning sprint is even viable.

> **Ship note (2026-06-05, post-close):** `ACTIVE_MODEL_ROOT` default was flipped from
> `ml/aiy_plants_v1` to `ml/house_plant_species_mobilenetv2` on the conclusive probe evidence, with
> all JVM + `pixel6Api34` gates re-run GREEN. Confirmed intentional in review. To revert: point the
> `buildConfigField` default back to AIY.

_Derived stats: ~9,100 Kotlin LOC (app/src, incl. tests); 40 unit + 16 instrumentation test
files; acceptance checkboxes 455/505 ticked (~90%) across `docs/sprints/*.md` (the open boxes are
the 0005 manual-GMD-walkthrough items the user accepted code + JVM coverage in lieu of)._

## Layer status

| Layer | Status | Notes |
| --- | --- | --- |
| KB (species.json, archetypes.json, plant_class_map.json) | ✓ shipped | 16 species. KB locked; the active model's `plant_class_map.json` now resolves **10 of 16** (8 exact + 2 coarse). |
| Model bundle (active / baseline) | ✓ swapped (0007) | **Active default: `house_plant_species_mobilenetv2`** (MobileNetV2, 47 classes, Apache-2.0, float16, 10.42 MiB). AIY V1/3 retained as the bundled regression anchor. One `ACTIVE_MODEL_ROOT` switch selects between them. |
| Identifier (interface + on-device impl + stub) | ✓ shipped | `OnDevicePlantIdentifier` is production. Seam **unchanged** through the 0007 swap (candidate `[1,47]` FLOAT32 fits existing mapper + preprocessor). |
| Confidence calibration | ✓ probe-backed (post-swap) | Mechanism shipped (`perSpeciesThresholds` in manifest, reader, mapper). Re-probed against the new model over 8 fixtures: 6 hits clear the global 0.55 outright. `per_species_thresholds` stays **empty by design** — peace lily @ 0.4468 not seeded (sub-50%), pothos confidently wrong (not a threshold case). Routing honesty preserved (0006 discipline). |
| Camera UI | ✓ shipped | Shutter, bind-pending overlay, Failure banner with `Try again`. |
| Result + Recommendation UI | ✓ shipped | Source-driven badge; archetype + recipe; sum-to-100 enforced. |
| LowConfidencePicker | ✓ shipped | Subtitle (de-jargoned in 0006) + chevron chips (drop `(0%)` suffix on floored-zero candidates, still selectable) + empty-state cards + outlined archetype CTA + small-screen viewport assertion. |
| Permission flow | ✓ shipped | Settings round-trip recovery; permanent-denial test reachable via fake guard hook. |
| Instrumentation tests | ✓ shipped | All 7 migrated to `@BindValue`; `@Ignore` count is 0; `LowConfidenceFlowTest` covers chip + search paths. |
| Stub-isolation gate | ✓ shipped | `scripts/check-stub-isolation.sh` GREEN. |
| Network-free gate | ✓ shipped | `verifyNoNetworking` GREEN. |
| Integration manifest | ✓ shipped | `scripts/integration-flow.ps1` cold + warm + buildonly all GREEN against `expected-artifacts/PLANTPOTTING-0001*.txt`. No re-baselining in 0005; transcripts captured to `evidence/PLANTPOTTING-0005/`. |
| Documentation | ✓ shipped | This file + `ml-mapping-notes.md` calibration-provenance entry + `PLANTPOTTING-0005.md` results doc (probe numbers, §5.6/§5.7 decisions) all landed. |

## Species/Model Coverage

How much of the bundled KB the on-device model identifies verbatim, and the calibration state.

| Axis | State | Notes |
| --- | --- | --- |
| KB species | 16 | `species.json`; locked. |
| In-vocab (active model) | **10 of 16** | Up from 2/16 (AIY). 8 exact (epipremnum-aureum, spathiphyllum-wallisii, ficus-elastica, dracaena-trifasciata, zamioculcas-zamiifolia, saintpaulia-ionantha, crassula-ovata, monstera-deliciosa) + 2 coarse-genus (phalaenopsis→Orchid, goeppertia→Calathea). |
| Out-of-vocab | 6 of 16 | monstera-adansonii, philodendron-hederaceum, philodendron-pink-princess, ficus-lyrata, chlorophytum-comosum, hoya-carnosa — route to `LowConfidencePicker` as before. Closing these needs training (→ data-availability spike, then fine-tuning sprint). |
| Probe results (8 fixtures) | 6 high-conf correct | monstera 1.0000, snake 1.0000, calathea 1.0000, orchid 1.0000, ZZ 0.9350, jade 0.5825. Weak: peace lily 0.4468 (correct, sub-threshold); pothos confidently confused with Pilea. |
| Routing | threshold-gated | Top-1 score vs per-class threshold (`perSpeciesThresholds`) falling back to the 0.55 global → `ResultScreen` (high-conf) or `LowConfidencePicker` (low-conf / out-of-vocab). |
| Latency | 33 ms median | Down from AIY's 43 ms despite the larger, more capable model. |
| Calibration | ✓ probe-backed (re-baselined to new model) | `per_species_thresholds` empty **by design** — 6 hits clear the global outright; peace lily not seeded (sub-50%); pothos a confidently-wrong case, not a threshold case. 0006 anti-overfit discipline holds. |

## Sprint history

| SID | Title | Status | Headline outcome |
| --- | --- | --- | --- |
| PLANTPOTTING-0001 | Android scaffold + end-to-end stub flow with KB | done | Camera → result → recommendation flow on stub identifier; KB + Hilt + Compose scaffolding; integration-flow manifest baseline. |
| PLANTPOTTING-0002 | Fix sprint for PLANTPOTTING-0001 review bugs | done | Settings round-trip recovery (Bug 4); permission rationale wording; bind-pending overlay. |
| PLANTPOTTING-0003 | On-device ML identifier + Bug A fix + source-driven badge | done | `OnDevicePlantIdentifier` wired to AIY V1/3; source-driven `ResultScreen` badge; `LowConfidencePicker` v1; `_comment_coverage` (2 of 16 species in-vocab). |
| PLANTPOTTING-0004 | Fix sprint for PLANTPOTTING-0003 review bugs | done | UINT8 preprocessor branch (Bug 1); `testTagsAsResourceId` bridge (Bug 2); `OnDeviceModelRealInterpreterTest` lands. |
| PLANTPOTTING-0005 | Post-shutter polish + un-defer carry-forward from 0003/0004 | done | LowConfidencePicker polished; bottom-anchored Failure banner; `TestIdentifyModule` deleted (7 tests migrated to `@BindValue`); `LowConfidenceFlowTest` + un-`@Ignore`'d PermissionDenied; `perSpeciesThresholds` mechanism + real-photo probe (*Monstera deliciosa* @ 0.8984, high-conf; map empty by design); accuracy assertion in CI; integration-flow cold/warm/buildonly GREEN. |
| PLANTPOTTING-0006 | Second in-vocab calibration probe (`crassula-ovata`) + two UX fixes | done | V0.1 multi-species sweep complete — *Crassula ovata* (jade) probes @ 0.1055 (low-conf), honest-fallback assertion in CI; `perSpeciesThresholds` stays empty by design (override not warranted). Two UX fixes: subtitle de-jargoned; `(0%)` chips drop the suffix, stay selectable. **Review surfaced the model-swap milestone — AIY V1/3 recognises wild flora, not houseplants.** |
| PLANTPOTTING-0007 | Houseplant model swap (V1 entry) — survey, eval harness, running prototype | done | **Model swapped: `house_plant_species_mobilenetv2` (MobileNetV2, Apache-2.0) replaces AIY as the production default.** Coverage 2/16 → **10/16**; top-1 high-conf 1 → 6; latency 43 → 33 ms. Single `ACTIVE_MODEL_ROOT` switch; seam unchanged; live `pixel6Api34` + real-device prototype. AIY kept as regression anchor. Survey rejected PlantNet/PlantCLEF/iNat/ViT candidates (non-existent or not bundle-fit). Known residue: 6 OOV species + pothos→Pilea confusion → next is a data-availability spike. |

## Known gaps

**Open this window — 6 KB species out-of-vocab + pothos→Pilea confusion (training-bound).**
The 0007 swap closed the wild-flora gap (2/16 → 10/16, see Sprint history), but **6 of 16 KB
species still have no class in the active model** (monstera-adansonii, philodendron-hederaceum,
philodendron-pink-princess, ficus-lyrata, chlorophytum-comosum, hoya-carnosa) and **pothos
(`epipremnum-aureum`) is confidently misclassified as Pilea** — a confident-wrong, worse than a
humble low-confidence miss. **Why it matters:** these are common houseplants a user is likely to
pot; until covered, they route to the manual picker (OOV) or, worse, return a wrong species
(pothos). **Closing them needs *training*, which 0007 deliberately did not do** (swap-only).

**Gating decision before any fine-tuning sprint: a data-availability spike.** A fine-tuning sprint
is *moot if there's no free training data*. The next step is a cheap spike to answer: is there
enough CC/CC0/public-domain imagery (iNaturalist research-grade, GBIF, Wikimedia, Flickr-CC) for the
6 OOV species + extra pothos/Pilea hard examples? Rough need (transfer-learning on the existing
MobileNetV2): ~50–100 imgs/class minimum, ~150–300/class comfortable; pothos/Pilea boundary wants
~150–300 of *each* incl. lookalikes; test/val splits must be **disjoint from the 8 androidTest
fixtures**. **If the data exists →** scope a fine-tuning sprint. **If not →** the swap stands as the
V1 ceiling and fine-tuning is shelved pending a paid/self-shot data plan. Captured in
`feedback/PLANTPOTTING-0007/feedback.md`.

Standing non-goals (carried from 0005's §2.4 — unchanged this window):

- **Delegate / quantization variants.** No INT8 / GPU / NNAPI delegate yet.
- **No net-new screens.** No `CaptureFailedScreen`, `Settings`, or `ModelInfoScreen`.
- **No ML training or retraining.**
- **`PlantIdentifier` / `IdentificationResult` interface changes.** Frozen per 0003 §4.4.
- **KB edits.** Locked.
- **AGP / Kotlin / Compose / Hilt / TFLite version bumps.** Locked.
- **README rewrite.** Append-only; link this roadmap instead.
- **`expected-artifacts` re-baselining.** Off the table.

Closed this window (PLANTPOTTING-0007 — removed, not carried):

- ~~**The model recognises wild flora, not houseplants.**~~ Closed: AIY V1/3 replaced as default by `house_plant_species_mobilenetv2`; in-vocab coverage 2/16 → 10/16, top-1 high-conf 1 → 6, latency 43 → 33 ms. The headline 0006 gap is resolved (residue tracked above as the 6 OOV species + pothos).
- ~~**Model swap requires a selection spike / harness.**~~ Closed: `model-candidate-matrix.md` surveyed 6 candidates; `ModelSwapEvaluationTest` harness probes AIY + candidate over 8 fixtures into `model-swap-eval.csv`; winner selected on evidence and wired behind `ACTIVE_MODEL_ROOT`. Harness + fixtures + switch are reusable for the next model iteration.

Accepted as-is (recorded, **not** an open gap):

- **Failure-banner live visual (§7.6).** There is no production-accessible path (debug menu, long-press, BuildConfig-gated intent) to drive the camera into `CameraUiState.Failure` for a live visual check. The user explicitly waived this off — code review + JVM-test coverage (render + retry + state-clear) accepted as sufficient. Recorded for history only; it is **not** a carry-forward and should not be re-listed as a gap unless a future sprint makes Failure-state visual evidence load-bearing.

## Proposed Sprint Path

### Active horizon (detailed)

#### Next: PLANTPOTTING-0008 — Training-data availability spike (gates fine-tuning)
- **Intent:** 0007 swapped the model and opened V1 (10/16 in-vocab), but **6 KB species remain
  out-of-vocab and pothos is confidently confused with Pilea** — both training-bound. A fine-tuning
  sprint is the obvious follow-up *but is moot without training data.* This spike answers the
  go/no-go question **before** committing a sprint to training: does enough free, license-clean
  imagery exist?
- **Entry conditions:** PLANTPOTTING-0007 merged to `origin/main` (`status: done`). Reusable assets:
  the 8-fixture set + provenance pattern, the `ModelSwapEvaluationTest` harness, and the
  `ACTIVE_MODEL_ROOT` switch.
- **Scope hints (to be firmed up in planning):**
  1. **Survey free imagery sources** — iNaturalist research-grade, GBIF, Wikimedia Commons,
     Flickr-CC — for the 6 OOV species (monstera-adansonii, both Philodendrons, ficus-lyrata,
     chlorophytum-comosum, hoya-carnosa) + extra pothos/Pilea hard examples. Record per-species
     count, license, and attribution feasibility.
  2. **Decide viability** — target ~150–300 license-clean images/class (≥50–100 floor), with
     train/val/test splits **disjoint from the androidTest fixtures**. Output a go/no-go per species.
  3. **Recommendation** — if viable, scope the fine-tuning sprint (data sourcing included); if not,
     record the swap as the V1 ceiling and document the paid/self-shot data option.
- **Milestone contribution:** de-risks **V1 — broad species coverage**; this spike is the gate, not
  the coverage win itself.
- **Notes:**
  - Keep it cheap — this is a *spike*, not a data-collection sprint. The deliverable is a sourcing
    feasibility report + a go/no-go, not a downloaded dataset.
  - Any actual model work stays behind the frozen `PlantIdentifier` seam and the `ACTIVE_MODEL_ROOT`
    switch.

### Milestone ladder (skeleton — sprints become detailed as they approach)

#### MVP — On-device ID + KB-driven potting-mix recommendation — *(shipped ~PLANTPOTTING-0003)*
Camera → on-device identification → routing decision → KB-driven recipe, network-free,
for the bundled species set.

#### V0.1 — Trustworthy confidence calibration — *(met ~PLANTPOTTING-0006)*
- **Exit criteria:** per-species thresholds backed by a real-photo probe ✓; an accuracy-bearing
  assertion in CI ✓; a multi-species probe sweep across both in-vocab species ✓.
- **Met:** real-photo probes for both in-vocab species (*Monstera deliciosa* @ 0.8984, high-conf;
  *Crassula ovata* @ 0.1055, low-conf) with accuracy assertions in `OnDeviceModelRealInterpreterTest`;
  the per-species threshold mechanism is wired and `perSpeciesThresholds` is empty by design
  (neither probe warranted an override). Further calibration depth requires a model swap (V1).

#### V1 — Broad species coverage *(active — entered at PLANTPOTTING-0007)*
- **Exit criteria:** most common houseplants identify directly (model swap, supplemental
  classifier, or expanded vocab) so the low-confidence path is the exception, not the rule.
- **Progress:** the 0007 swap moved coverage 2/16 → **10/16** and made the production model
  houseplant-tuned — the bulk of the milestone. **Residue:** 6 OOV species + the pothos→Pilea
  confusion, both training-bound.
- **Skeleton sprints:** ~~model-selection spike (PLANTPOTTING-0007 ✓)~~; training-data availability
  spike (next — gates the rest); *if data exists* → fine-tuning sprint (close 6 OOV + pothos);
  expanded real-photo validation set.

#### V2 — Richer care guidance
- **Exit criteria:** beyond the one-shot recipe — repotting schedule, care reminders, or
  personalization to the user's plant set.
- **Skeleton sprints:** care-profile model; reminder UX; plant-collection persistence.

#### Beyond V2 — Plant-health diagnostics
- Identify stress / pest / over-watering signs from the same photo pipeline (sketch only).

## Changelog

- 2026-06-05 — bumped through PLANTPOTTING-0007 (review close-out): reconciled 0007 (clean review,
  no bugs). **Recorded the houseplant model swap** — `house_plant_species_mobilenetv2` (MobileNetV2,
  Apache-2.0) replaces AIY V1/3 as the production `ACTIVE_MODEL_ROOT` default (flipped post-close
  2026-06-05, gates re-run GREEN, confirmed intentional in review); AIY kept as the regression
  anchor. Coverage 2/16 → **10/16** (8 exact + 2 coarse), top-1 high-conf 1 → 6, latency 43 → 33 ms;
  seam unchanged; live `pixel6Api34` + real-device prototype. `per_species_thresholds` stays empty by
  design. **Closed the "wild-flora model" gap; opened the 6-OOV-species + pothos→Pilea gap
  (training-bound).** Re-scoped the active horizon to PLANTPOTTING-0008 — a **training-data
  availability spike that gates** any fine-tuning sprint. Stats: ~9,100 app/src LOC; 455/505
  acceptance boxes (~90%).
- 2026-06-04 — bumped through PLANTPOTTING-0006 (review close-out): reconciled 0006 from
  in-progress → done. Completed the V0.1 multi-species calibration sweep — added the
  *Crassula ovata* probe (@ 0.1055, low-conf, honest-fallback assertion); `perSpeciesThresholds`
  stays empty by design. Closed the two 0005-review UX gaps (subtitle jargon, "(0%)" chips) and
  the single-species-evidence gap. Promoted calibration to `probe-backed (2 of 2 in-vocab)` and
  marked V0.1 **met**. **Elevated the model swap from a standing non-goal to the next-up V1
  milestone** — 0006's sweep + the user's review feedback show AIY V1/3 recognises wild flora,
  not houseplants. Re-scoped the active horizon to PLANTPOTTING-0007 (houseplant model-selection
  spike). Stats: ~8,455 LOC; 367/410 acceptance boxes (~90%).
- 2026-06-04 — refreshed through PLANTPOTTING-0005 (close-out): reconciled 0005 from
  in-progress → done. Closed the §5.4–§5.8 real-photo calibration chain, the §7.3
  integration-flow transcripts, and the §3.12/§4.7/§7.2 GMD-run gaps (removed from Known
  gaps, not carried). Opened 2 UX gaps (subtitle jargon, "(0%)" chips) + tightened the
  single-species-evidence gap; recorded the §7.6 Failure-banner visual as accepted-as-is.
  Advanced calibration from `emerging` → `probe-backed (1 of 2 in-vocab)`. Re-scoped
  PLANTPOTTING-0006 to the `crassula-ovata` probe + the two UX fixes (no longer "finish
  0005"); marked V0.1 partially met (✓ probe, ✓ CI assertion, ☐ multi-species sweep).
- 2026-06-04 — added roadmap-skill frontmatter (`last_updated`, `through_sid`), a
  Species/Model Coverage section, and reshaped "Candidate next sprint" into a Proposed
  Sprint Path (active horizon + milestone ladder). Content otherwise unchanged; no sprint
  window reconciled. First entry — the `/roadmap` skill appends one per REFRESH hereafter.
