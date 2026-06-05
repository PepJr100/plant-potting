---
last_updated: 2026-06-05
through_sid: PLANTPOTTING-0009
---

# ROADMAP

One-page snapshot of where PlantPotting stands and what's next. Sprint history lives
in [`docs/sprints/`](sprints/); this file is the current view.

> Maintained by the `/roadmap` skill (INIT / REFRESH / BUMP). The frontmatter above is
> parsed by routing — `last_updated` and `through_sid` drive when a REFRESH is due.

## Current state (post-PLANTPOTTING-0009)

**The fine-tune data spike landed PARTIAL-GO — but review re-pointed V1 at a cheaper win.**
PLANTPOTTING-0008 was an assess-only desk spike (no training): it counted license-clean CC imagery for
the 6 OOV KB species + the pothos/Pilea boundary across iNaturalist (primary, photo-license-verified),
GBIF (cross-check), Wikimedia Commons, and Flickr-CC. Verdict **PARTIAL-GO** — 5 OOV species GO
(`chlorophytum-comosum` ~790, `philodendron-hederaceum` ~467, `hoya-carnosa` ~430, `monstera-adansonii`
~369, `ficus-lyrata` ~257), the pothos/Pilea boundary CONDITIONAL (pothos ~1470 abundant, *Pilea
peperomioides* ~76 thin), and `philodendron-pink-princess` **NO-GO** (cultivar-proven CC imagery only
~5–15). Headline finding: only **1.0–6.8%** of iNat houseplant imagery is license-clean (CC-BY-NC
dominates) — existence was never the problem, *license* is. The spike also fixed one UX bug
(shutter re-enables on return-to-camera via an `ON_RESUME` reset of terminal `Success`). All artifacts
are text-only; no images, no model, no KB change committed; `verifyNoNetworking` + stub-isolation stayed
GREEN.

**Review changed the V1 plan.** Two user constraints re-pointed the milestone: (1) **no self-shot
training data** — which kills the pink-princess self-shot fallback and the ~75–150 self-shot Pilea
boundary supplement, the two paths the 0008 outline leaned on; and (2) review surfaced that the
production model already has **47 classes but only 10 map to the KB**, leaving a **delta of 37
model-recognized species with no KB care entry**. Closing that delta is **text-only** (KB rows, no
training, no self-shot), so the **next sprint is a KB-expansion over the popular model-covered delta
(~17 species)** — a 10→~27 coverage jump at zero ML cost — and the **fine-tune is deferred** (still
viable later for the 5 GO classes on CC data alone; pink-princess + Pilea-balance are out under the
no-self-shot rule). See `feedback/PLANTPOTTING-0008/feedback.md`.

---

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

> **Ship note (2026-06-05) — PLANTPOTTING-0009 shipped the KB-expansion described above as "the next
> sprint".** Text-only, zero ML: `species.json` **16 → 32**, the house-plant `plant_class_map.json`
> **10 → 26 of 47** model classes, **+1 archetype** (`carnivorous-peat-sand`, for Venus Flytrap). Pilea
> stays deliberately unmapped (CI-enforced) for a later pothos↔Pilea boundary-fix sprint. A free side
> effect: 3 of the new species (`Aloe vera`, `Hedera helix`, `Euphorbia pulcherrima`) exist in AIY's
> vocabulary, so the AIY baseline now resolves **5 of 32** (was 2). **Known gap (not closed):** the 16
> new mappings are editorial/model-vocabulary coverage only — **unprobed**, not calibrated against real
> photos. Full narrative reconciliation (Layer Status, Species/Model Coverage, Known Gaps, Proposed
> Path) is deferred to the next `/roadmap refresh`.

_Derived stats: ~9,478 Kotlin LOC (app/src, incl. tests); 41 unit + 11 instrumentation test
files; acceptance checkboxes 623/673 ticked (~93%) across `docs/sprints/*.md` (the open boxes are
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
| Out-of-vocab | 6 of 16 | monstera-adansonii, philodendron-hederaceum, philodendron-pink-princess, ficus-lyrata, chlorophytum-comosum, hoya-carnosa — route to `LowConfidencePicker` as before. 0008 confirmed CC fine-tune data exists for 5 of 6 (pink-princess NO-GO); fine-tune **deferred** behind the cheaper text-only KB-expansion (PLANTPOTTING-0009). |
| Model/KB delta | **37 of 47 model classes unmapped** | The active model recognizes 47 species but only 10 have a KB entry — 37 model-recognized houseplants return a prediction with no care card. Closing the popular slice is text-only (next sprint). |
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
| PLANTPOTTING-0008 | Training-data availability spike (gates fine-tuning) + shutter-on-return UX fix | done | **Assess-only CC-imagery spike → PARTIAL-GO.** 5 OOV species GO (CC counts ~257–790), pothos/Pilea boundary CONDITIONAL (pothos ~1470 / Pilea ~76), pink-princess NO-GO (cultivar-proven ~5–15). Only 1.0–6.8% of iNat houseplant imagery is license-clean. Text-only deliverables (report + GO/NO-GO matrix + fine-tune outline + CSV/query-log); no images/model/KB committed. Plus a shutter-on-return UX fix (`ON_RESUME` reset of terminal `Success`). **Review re-pointed V1:** the no-self-shot constraint kills the pink-princess + Pilea-balance fallbacks; the model's 47-class / 10-mapped delta makes a **text-only KB-expansion the next sprint**, fine-tune deferred. |
| PLANTPOTTING-0009 | Text-only KB-expansion: +16 delta species (10→26 mapped), Pilea deferred | done (opus) | **Pure content/config, zero ML.** `species.json` **16 → 32**; house-plant `plant_class_map.json` **10 → 26 of 47** classes; **+1 archetype** (`carnivorous-peat-sand`, Venus Flytrap). New `HousePlantClassMapValidationTest` (verbatim-key guard, 26-count, Pilea-absence, existing-10 regression); count asserts bumped (32/9). Pilea left unmapped (CI-enforced) for the boundary-fix sprint. Free win: AIY now resolves 5 of 32 (Aloe vera / Hedera helix / Euphorbia pulcherrima are in its vocab). **Known gap:** the 16 new mappings are unprobed (editorial coverage, not real-photo calibrated). Full suite + `verifyNoNetworking` + stub-isolation GREEN. |

## Known gaps

**Open this window — 37-class KB/model coverage delta (text-only to close) + the deferred OOV
fine-tune.** Two distinct gaps, re-prioritized by the 0008 review:

1. **The model recognizes 37 species the KB can't describe.** The active model has **47 classes but
   only 10 map to the KB** — so 37 model-recognized species (Aloe Vera, Aglaonema, Alocasia,
   Anthurium, Dieffenbachia, Boston Fern, Monstera-money-tree, palms, Pilea, etc.) return a prediction
   with **no care card to show**. **Why it matters:** these are common houseplants; the recognition
   already works, only the KB entry is missing. **Closing it is text-only** — additive `species.json` /
   `archetypes.json` / `plant_class_map.json` rows, **no training, no self-shot imagery.** This is the
   cheapest large coverage win on the board and is the **next sprint** (KB-expansion over the popular
   delta, ~17 species incl. user-owned Poinsettia + Venus Flytrap → 10→~27 mapped). **Sharp edge:**
   adding a **Pilea** KB entry turns today's silent pothos→Pilea misread into a *confidently wrong* care
   card — ship the boundary fix or strict pothos/Pilea confidence gating alongside the Pilea entry.
2. **6 KB species remain out-of-vocab + pothos→Pilea confusion (training-bound, now DEFERRED).** The
   0008 spike answered the data question: **PARTIAL-GO** — 5 OOV species (chlorophytum, hederaceum,
   hoya, adansonii, ficus-lyrata) have sufficient CC imagery to fine-tune; pothos/Pilea boundary is
   CONDITIONAL (Pilea side thin at ~76); pink-princess is **NO-GO** (cultivar-proven ~5–15). The
   user's **no-self-shot constraint** removes the two paths the outline leaned on (pink-princess
   self-shot fallback; ~75–150 self-shot Pilea hard examples), so pink-princess and the Pilea-balance
   work are **out**. The 5-GO-class fine-tune stays viable on CC data alone but is **deferred behind the
   KB-expansion sprint** as lower ROI per effort. Reusable when revisited: the `finetune-sprint-outline.md`
   shared approach (47→52, float16 export, `ModelSwapEvaluationTest`, `ACTIVE_MODEL_ROOT`). Captured in
   `feedback/PLANTPOTTING-0008/feedback.md` and the 0008 evidence dir.

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

#### Next: PLANTPOTTING-0009 — KB-expansion over the model-covered delta (text-only, no ML)
- **Intent:** the active model already recognizes **47 species but only 10 map to the KB** — 37
  model-recognized houseplants currently return a prediction with **no care card.** Closing the popular
  slice of that delta is **pure KB work** (text rows, no training, no self-shot imagery), the cheapest
  large coverage win available. This was the explicit re-direction from the 0008 review, chosen over the
  deferred fine-tune.
- **Entry conditions:** PLANTPOTTING-0008 merged to `origin/main` (`status: done`) and its review PR
  merged. KB unlocks for **additive** entries this sprint (existing 16 untouched).
- **Scope hints (to be firmed up in planning):**
  1. **Add KB entries for the ~17 target delta species** — Aloe Vera, Pilea peperomioides, Aglaonema,
     Alocasia, Anthurium, Dieffenbachia, Maranta, Boston Fern, Pachira (Money Tree), Areca Palm, generic
     Dracaena, Tradescantia, English Ivy, Schefflera, Kalanchoe + user-owned **Poinsettia** and **Venus
     Flytrap**. Each = `species.json` + `archetypes.json` + a `plant_class_map.json` row mapping the
     model class → new KB id. Target ~10 → ~27 mapped.
  2. **Boundary safety for Pilea.** Adding a Pilea KB entry turns the silent pothos→Pilea misread into a
     confidently-wrong care card — pair the Pilea entry with strict pothos/Pilea confidence gating (or a
     boundary fix) so a wrong-but-confident result can't surface.
  3. **Verify care content + routing.** Care data per new species vetted in planning; confidence
     calibration for these previously-unprobed classes considered; `verifyNoNetworking` +
     `check-stub-isolation.sh` stay GREEN; seam frozen.
- **Milestone contribution:** delivers the bulk of **V1 — broad species coverage** directly (recognition
  already works; this makes it *useful*), at zero ML cost.
- **Notes:**
  - **No self-shot / first-party imagery** anywhere (hard user constraint) and **no model training** —
    this is KB text only, behind the frozen `PlantIdentifier` seam.
  - The 0008 fine-tune outline (5 GO OOV classes on CC data) is **deferred behind this sprint**, not
    cancelled; pink-princess + Pilea-balance retraining are out under the no-self-shot rule.

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
  houseplant-tuned. The 0008 spike confirmed CC fine-tune data exists for 5 of 6 OOV species
  (PARTIAL-GO). **Review then found the bigger, cheaper lever:** the model recognizes 37 species the KB
  can't describe — a text-only KB-expansion closes most of the milestone with no ML.
- **Skeleton sprints:** ~~model-selection spike (PLANTPOTTING-0007 ✓)~~; ~~training-data availability
  spike (PLANTPOTTING-0008 ✓ — PARTIAL-GO)~~; **KB-expansion over the model-covered delta
  (PLANTPOTTING-0009, next — ~17 species, text-only)**; *deferred* → fine-tune sprint for the 5 GO OOV
  classes (CC data only, no self-shot); expanded real-photo validation set.

#### V2 — Richer care guidance
- **Exit criteria:** beyond the one-shot recipe — repotting schedule, care reminders, or
  personalization to the user's plant set.
- **Skeleton sprints:** care-profile model; reminder UX; plant-collection persistence.

#### Beyond V2 — Plant-health diagnostics
- Identify stress / pest / over-watering signs from the same photo pipeline (sketch only).

## Changelog

- 2026-06-05 — bumped through PLANTPOTTING-0008 (review close-out): reconciled 0008 (clean review —
  no bugs, no UX issues; cheap gates re-run GREEN; shutter-on-return fix accepted). Recorded the
  **PARTIAL-GO** data-availability verdict (5 OOV species GO on CC imagery ~257–790; pothos/Pilea
  boundary CONDITIONAL; pink-princess NO-GO at cultivar-proven ~5–15; only 1.0–6.8% of iNat houseplant
  imagery license-clean). **Review re-pointed V1:** the user's **no-self-shot constraint** removes the
  pink-princess self-shot fallback and the ~75–150 self-shot Pilea boundary supplement, and review
  surfaced the model's **47-class / 10-mapped coverage delta** (37 recognized species with no KB care
  card). Re-scoped the active horizon from a fine-tune sprint to **PLANTPOTTING-0009 — a text-only
  KB-expansion over the popular delta (~17 species incl. user-owned Poinsettia + Venus Flytrap, 10→~27
  mapped, no ML, no self-shot)**; the 5-GO-class fine-tune is **deferred**, not cancelled. Flagged the
  Pilea sharp edge (a Pilea KB entry turns the silent pothos→Pilea miss into a confidently-wrong card →
  pair with confidence gating). Captured in `feedback/PLANTPOTTING-0008/feedback.md`.
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
