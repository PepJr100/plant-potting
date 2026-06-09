---
last_updated: 2026-06-09
through_sid: PLANTPOTTING-0013
---

# ROADMAP

One-page snapshot of where PlantPotting stands and what's next. Sprint history lives
in [`docs/sprints/`](sprints/); this file is the current view.

> Maintained by the `/roadmap` skill (INIT / REFRESH / BUMP). The frontmatter above is
> parsed by routing — `last_updated` and `through_sid` drive when a REFRESH is due.

## Current state (post-PLANTPOTTING-0013)

**A confident, correct Pilea now gets a *direct* care card — without re-opening the pothos→Pilea
confident-wrong boundary.** The 0012 strict-picker was too conservative: the at-home review found a real
Pilea identified at 98% (no pothos competing) yet still demoted to the picker. PLANTPOTTING-0013 (behind
the frozen seam, **no model swap, no training**, `tta` stays 6) refines the `ModelScoreMapper` boundary
gate so a top-1 = Pilea result yields a **direct** Pilea card **only** when it clears an elevated,
Pilea-specific bar `per_species_thresholds["pilea-peperomioides"] = 0.98`, and still routes to the picker
below it. The bar is composed *with* — never replacing — the boundary rule, and is **CI-bound > 0.9661**
(the documented pothos→Pilea ceiling) by `HousePlantClassMapValidationTest`, so a pothos misread as Pilea
can never reach a direct card. The crux (stated in A0): real-Pilea and pothos-misread are
near-identical in score *shape* (both huge margins), so the **only** separator is the absolute Pilea
top-1 — and on the shipped **tta6** pipeline 6-crop averaging crushes every pothos→Pilea misread to
**≤ 0.9063** while real-Pilea stays **≥ 0.9940** (a clean +0.088 gap). A pre-registered **leave-one-out +
author-separated** sweep over the 6 Pilea fixtures (all distinct authors) passed **+0 confident-wrong on
every fold**; the principal confirmed SHIP. **On-device result (66 fixtures):** gate-isolated
**Δ+0 confident-wrong** on the shipped pipeline (clean / all perturbations / pothos-Pilea subset), **0**
pothos→direct-Pilea cards, and **all 6 real-Pilea fixtures now get a correct direct card** (were 0/6).
Per-base clean top-1 rose **0.417 → 0.530**. (Residual risk, documented: in the *non-shipped* single-crop
diagnostic modes a synthetic-perturbed pothos can reach 0.9997 > 0.98; production never feeds single-crop
scores to the gate.) **Thread B** grew the honest sample count: **+6 CC0 fixtures** across 6 of the 8
single-photo species (`identify-fixtures` **60 → 66 photos / 39 species**); **Begonia + Schlumbergera hit
documented license-clean supply ceilings** (wild-only Begonia; only *S. truncata* ≠ the KB's *bridgesii*).
Confident-wrong stayed flat — honest measurement, not deep per-species calibration (31 of 39 species still
on 1–2 photos). Version **v0.7.0**; frozen seam + AIY V1/3 anchor unchanged; all gates GREEN; debug APK
delivered.

> **The window in one arc (0006 → 0013):** AIY V1/3 was swapped out for the houseplant-tuned
> `house_plant_species_mobilenetv2` (0007); a data spike gated fine-tuning to PARTIAL-GO under a
> no-self-shot constraint (0008); two text-only KB expansions filled the model/KB delta to **39-of-47
> mapped / 45 KB species** (0009/0010); the app gained a real experience layer — persistence, Home, My
> Plants, confidence bar (0010); the **confident-wrong trust failure was measured and roughly halved**,
> 0.382 → 0.179, behind an abstention mechanism (0011); and the pothos↔Pilea boundary was **closed without
> re-opening it** — a gate (0012) then a held-out-validated direct card (0013). The open frontier is now
> *honest per-species depth* (31/39 species on 1–2 photos) and *messy-capture* accuracy — plus, net-new
> this refresh, a **Launch track** to publish on the Play Store. Per-sprint narratives live in Sprint
> History and the Changelog below.

_Derived stats: ~14,893 Kotlin LOC (`git ls-files '*.kt'`, incl. tests); 58 unit + 22 instrumentation
test files; acceptance checkboxes 982/1034 ticked (~95%) across `docs/sprints/*.md` (the open boxes are
the 0005 manual-GMD-walkthrough items the user accepted code + JVM coverage in lieu of). Current release
**v0.7.0** (`versionCode 7`)._

## Layer status

| Layer | Status | Notes |
| --- | --- | --- |
| KB (species.json, archetypes.json, plant_class_map.json) | ✓ expanded (0012) | **45 species, 9 archetypes**. The active model's `plant_class_map.json` resolves **39 of 47** model classes (was 38 after 0010, 26 after 0009, 10 before). **Pilea mapped (0012) only in lockstep with the pothos↔Pilea boundary gate** (CI-bound by `pileaMappingRequiresBoundaryGate`); **0013 added a *direct* Pilea card above an elevated `per_species_thresholds["pilea-peperomioides"]=0.98`** (CI-bound > 0.9661), composed with the gate. 0009+0010 delta mappings unprobed (editorial coverage). |
| Persistence (DataStore) | ✓ shipped (0010) | App's first local-persistence layer. One DataStore document, two collections (`identifiedPlants` N-capped + `addPlantRequests` tally) via `PlantLogStore`; injectable `TimeProvider`. Local-only (`verifyNoNetworking` GREEN). **Does not survive uninstall** (principal request logged — candidate for V2). |
| Home / My Plants / Add-this-plant UI | ✓ shipped (0010) | Home/landing (2×2 tiles + recent carousel) is now the start destination; My Plants (Save, de-dup, remove, thumbnails, restart-persistent); Add-this-plant wireframe routes confident-but-unmapped classes and logs a tally. Shared bottom Home button on every screen. |
| Confidence display | ✓ shipped (0010) | Numeric %+`LinearProgressIndicator` on `ResultScreen` via an optional `confidencePct` nav arg off `CandidateProvider` (seam untouched); degrades gracefully (no %/bar) on stub flows. |
| Reference images | ✓ all-photo (0011) | 44 images (one per species) under `res/drawable-nodpi/` via `PlantImageResolver` + placeholder fallback; manifest + cross-check test. **All 11 botanical plates swapped to real photos** (0011). **Gap:** Pilea — promoted to a direct card in 0013 — is the only mapped species still on the placeholder (Known Gap 3). |
| Accuracy eval harness | ✓ extended (0013) | `AccuracyEvalTest` (GMD/local `pixel6Api34`) runs every fixture + 11 deterministic synthetic perturbations × 3 preprocessing modes → rich per-row `accuracy-eval.csv` + summary; CC0/PD fixture set **66 photos / 39 species** (0013: +6 second-photos across 6 of the 8 single-photo species; Begonia + Schlumbergera supply-ceilinged) with license + integrity + CSV-schema guard tests. Gated `sweepTtaLevelsEmitCsv` (behind `-e ttaSweep true`) for the grid-tiling TTA sweep (0012). |
| Model bundle (active / baseline) | ✓ swapped (0007) | **Active default: `house_plant_species_mobilenetv2`** (MobileNetV2, 47 classes, Apache-2.0, float16, 10.42 MiB). AIY V1/3 retained as the bundled regression anchor. One `ACTIVE_MODEL_ROOT` switch selects between them. |
| Identifier (interface + on-device impl + stub) | ✓ shipped | `OnDevicePlantIdentifier` is production. Seam **unchanged** through the 0007 swap (candidate `[1,47]` FLOAT32 fits existing mapper + preprocessor). |
| Confidence calibration | ✓ abstention-backed (0011) + Pilea direct-card bar (0013) | Mechanism shipped (`perSpeciesThresholds` + `high_confidence_abstain_margin`, default `0f`). **0011 set the production margin to 0.30** on the locked TTA-6 pipeline: confident-wrong **0.382 → 0.179** (0.098 clean), top-1 ~flat, held-out validated; pick-manually cost 0.089 → 0.323. `per_species_thresholds` now holds **exactly one entry — `pilea-peperomioides = 0.98`** (0013), the elevated direct-card bar above the pothos→Pilea ceiling (CI-bound > 0.9661); still **never** seeded to bless a weak prediction (0006 discipline). Routing honesty preserved. |
| Camera UI | ✓ shipped | Shutter, bind-pending overlay, Failure banner with `Try again`; shutter re-enables on return-to-camera (`ON_RESUME` reset, 0008). |
| Result + Recommendation UI | ✓ shipped | Source-driven badge; archetype + recipe; sum-to-100 enforced. |
| LowConfidencePicker | ✓ shipped | Subtitle (de-jargoned in 0006) + chevron chips (drop `(0%)` suffix on floored-zero candidates, still selectable) + empty-state cards + outlined archetype CTA + contained search (0010) + small-screen viewport assertion. |
| Permission flow | ✓ shipped | Settings round-trip recovery; permanent-denial test reachable via fake guard hook. |
| Stub-isolation / network-free gates | ✓ shipped | `scripts/check-stub-isolation.sh` + `verifyNoNetworking` GREEN. |
| Integration manifest | ✓ shipped | `scripts/integration-flow.ps1` cold + warm + buildonly GREEN against `expected-artifacts/PLANTPOTTING-0001*.txt`. |
| **Licensing & legal** | ⚠ partial (Launch gap) | Bundled ML models Apache-2.0; 44 reference photos Unsplash/Pexels/CC0 (CC-BY-SA banned by policy); no GPL/copyleft; `verifyNoNetworking` holds — **commercial-clean**. **Missing:** project `LICENSE` (decision: **PolyForm Noncommercial 1.0.0**) + a public privacy policy (`docs/PRIVACY.md`). Closes in PLANTPOTTING-0014. |
| **Release engineering / signing / distribution** | ✗ not started (Launch gap) | `release.yml` emits a **debug APK** to GitHub on `v*` tags only. No release `signingConfig`, no Play App Signing, no `bundleRelease`/AAB, R8 off, placeholder launcher icon. `targetSdk`/`compileSdk` = **34 < Play minimum 35**. Closes in PLANTPOTTING-0014. |

## Species/Model Coverage

How much of the bundled KB the on-device model identifies, plus the measured accuracy and
calibration state — rebuilt post-0012/0013.

| Axis | State | Notes |
| --- | --- | --- |
| KB species | **45** | `species.json`; +16 by 0009, +12 by 0010, +1 (Pilea) by 0012 (append-only; original 16 untouched). |
| Mapped (active model) | **39 of 47** | 10 → 26 (0009) → 38 (0010) → **39 (0012, Pilea)**. **Pilea now ships WITH a direct card** above `per_species_thresholds["pilea-peperomioides"] = 0.98` (0013), composed with the 0012 boundary gate, CI-bound **> 0.9661**. The 28 delta rows (0009+0010) are **unprobed** — editorial coverage, not real-photo calibrated (gap 1). |
| Unmapped model classes | **8 of 47** | Pilea left this set in 0012. The remaining 8 (Rattlesnake Plant, Iron Cross begonia, seasonal flowering bulbs, etc.) stay deliberately unmapped → `LowConfidencePicker` / the 0010 "Add this plant" flow. |
| KB species OOV in active model | **6** | monstera-adansonii, both Philodendrons, ficus-lyrata, chlorophytum-comosum, hoya-carnosa → route to `LowConfidencePicker`. 0008 confirmed CC fine-tune data for 5 of 6 (pink-princess NO-GO); fine-tune **deferred** (gap 4). |
| Fixture set (honest eval) | **66 photos / 39 species** | 8 → 41 (0011) → 60 (0012, +19) → **66 (0013, +6)** CC0/PD/CC-BY, with license + integrity + CSV-schema guard tests. **31 of 39 species still rest on 1–2 photos** (the principal's ≥3-per-species ask, gap 0). Begonia + Schlumbergera at 1 each — documented license-clean supply ceilings (wild-only Begonia; CC0 Schlumbergera all *S. truncata* ≠ KB *bridgesii*). |
| Real-photo accuracy | per-base clean top-1 **0.417 → 0.530**; confident-wrong **0.179** | Confident-wrong 0.382 → **0.179** (0.098 clean) via TTA-6 + 0.30 abstain margin (0011), held-out validated (LOSO 0.167–0.188). Per-base clean top-1 rose **0.417 → 0.530** (0013) — mainly the 6 real-Pilea fixtures flipping picker → correct direct card (+ new Thread-B second-photos); confident-wrong stayed flat (clean 0.117 → 0.121, perturbation 0.221 → 0.210). |
| Probe results (8 in-vocab fixtures) | 6 high-conf correct | monstera 1.0000, snake 1.0000, calathea 1.0000, orchid 1.0000, ZZ 0.9350, jade 0.5825; weak peace lily 0.4468 (correct, sub-threshold). The original pothos→Pilea confusion is now gated. |
| Routing | threshold-gated + abstention + Pilea direct-card bar | Top-1 vs per-class threshold falling back to the 0.55 global, then the 0.30 abstain margin, then the top-1=Pilea boundary gate / 0.98 direct-card bar → `ResultScreen` or `LowConfidencePicker`. |
| Latency | **~125 ms median (TTA-6)** | Single-crop ~20 ms; production runs 6 TTA views (centre + 4 corners + full-frame), 125 ms median / 539 ms worst on a one-shot identify. TTA stayed 6 through 0012/0013 (grid tiling went OOD; 3×3 busted the ~2 s cap at 2686 ms). |
| Calibration | ✓ abstention-backed (0011) + one Pilea direct-card threshold (0013) | `high_confidence_abstain_margin = 0.30` downgrades low-margin verdicts to the picker; `per_species_thresholds` holds **exactly one entry — `pilea-peperomioides = 0.98`** (the elevated direct-card bar above the pothos→Pilea ceiling), **never** seeded to bless a weak prediction (0006 anti-overfit discipline holds). |
| AIY baseline | bundled regression anchor (~5/39) | AIY V1/3 stays shipped for tests/regression, now resolving ~5 mapped species (Aloe vera / Hedera helix / Euphorbia pulcherrima entered its vocab via 0009). Any AIY second-opinion cascade is a future *measurement* spike only, not current routing (gap 2c). |
| Licensing / commercial cleanliness | clean for bundled app assets | Active model + AIY Apache-2.0; reference photos/fixtures tracked via manifests under allowed licenses; `verifyNoNetworking` GREEN. Project-level LICENSE / privacy policy are separate Launch gaps (6). |

**Honesty caveat (load-bearing):** these are *measured-under-clean + synthetic-robustness* numbers on a
set that still under-represents messy phone captures and rests **31/39 species on 1–2 photos** — **NOT
"real-world accuracy solved."** The harness is in place to measure any future lever honestly.

## Sprint history

| SID | Title | Status | Headline outcome |
| --- | --- | --- | --- |
| PLANTPOTTING-0001 | Android scaffold + end-to-end stub flow with KB | done | Camera → result → recommendation flow on stub identifier; KB + Hilt + Compose scaffolding; integration-flow manifest baseline. |
| PLANTPOTTING-0002 | Fix sprint for PLANTPOTTING-0001 review bugs | done | Settings round-trip recovery (Bug 4); permission rationale wording; bind-pending overlay. |
| PLANTPOTTING-0003 | On-device ML identifier + Bug A fix + source-driven badge | done | `OnDevicePlantIdentifier` wired to AIY V1/3; source-driven `ResultScreen` badge; `LowConfidencePicker` v1; `_comment_coverage` (2 of 16 species in-vocab). |
| PLANTPOTTING-0004 | Fix sprint for PLANTPOTTING-0003 review bugs | done | UINT8 preprocessor branch (Bug 1); `testTagsAsResourceId` bridge (Bug 2); `OnDeviceModelRealInterpreterTest` lands. |
| PLANTPOTTING-0005 | Post-shutter polish + un-defer carry-forward from 0003/0004 | done | LowConfidencePicker polished; bottom-anchored Failure banner; `TestIdentifyModule` deleted (7 tests migrated to `@BindValue`); `LowConfidenceFlowTest` + un-`@Ignore`'d PermissionDenied; `perSpeciesThresholds` mechanism + real-photo probe (*Monstera deliciosa* @ 0.8984, high-conf; map empty by design); accuracy assertion in CI; integration-flow cold/warm/buildonly GREEN. |
| PLANTPOTTING-0006 | Second in-vocab calibration probe (`crassula-ovata`) + two UX fixes | done | V0.1 multi-species sweep complete — *Crassula ovata* (jade) probes @ 0.1055 (low-conf), honest-fallback assertion in CI; `perSpeciesThresholds` stays empty by design. Two UX fixes: subtitle de-jargoned; `(0%)` chips drop the suffix, stay selectable. **Review surfaced the model-swap milestone — AIY V1/3 recognises wild flora, not houseplants.** |
| PLANTPOTTING-0007 | Houseplant model swap (V1 entry) — survey, eval harness, running prototype | done | **Model swapped: `house_plant_species_mobilenetv2` (MobileNetV2, Apache-2.0) replaces AIY as the production default.** Coverage 2/16 → **10/16**; top-1 high-conf 1 → 6; latency 43 → 33 ms. Single `ACTIVE_MODEL_ROOT` switch; seam unchanged; live `pixel6Api34` + real-device prototype. AIY kept as regression anchor. Known residue: 6 OOV species + pothos→Pilea confusion → next is a data-availability spike. |
| PLANTPOTTING-0008 | Training-data availability spike (gates fine-tuning) + shutter-on-return UX fix | done | **Assess-only CC-imagery spike → PARTIAL-GO.** 5 OOV species GO (CC counts ~257–790), pothos/Pilea boundary CONDITIONAL (pothos ~1470 / Pilea ~76), pink-princess NO-GO (cultivar-proven ~5–15). Only 1.0–6.8% of iNat houseplant imagery is license-clean. Text-only deliverables; no images/model/KB committed. Plus a shutter-on-return UX fix. **Review re-pointed V1:** the no-self-shot constraint kills the pink-princess + Pilea-balance fallbacks; the 47-class / 10-mapped delta makes a **text-only KB-expansion the next sprint**, fine-tune deferred. |
| PLANTPOTTING-0009 | Text-only KB-expansion: +16 delta species (10→26 mapped), Pilea deferred | done (opus) | **Pure content/config, zero ML.** `species.json` **16 → 32**; house-plant `plant_class_map.json` **10 → 26 of 47** classes; **+1 archetype** (`carnivorous-peat-sand`, Venus Flytrap). New `HousePlantClassMapValidationTest`. Pilea left unmapped (CI-enforced). Free win: AIY now resolves 5 of 32. **Known gap:** the 16 new mappings are unprobed (editorial coverage). |
| PLANTPOTTING-0010 | App-experience sprint: UI/UX refresh + add-this-plant wireframe + KB expansion | done (opus) | **"Make it feel like an app" — behind the frozen seam.** First local-persistence layer (**DataStore**); **Home/landing**; **My Plants**; **confidence %+bar**; **contained search**; **"Add this plant"** wireframe; **44 CC0/PD reference photos**; shared bottom **Home button**; KB **32 → 44 species / 26 → 38-of-47 mapped**. Theme switcher built then removed (default LEAF). v0.4.0. **Review clean — no bugs.** Headline finding (not a regression): real-world ID accuracy is poor (snake plant ~1/3 correct, confidently wrong otherwise) → calibration is the next move. |
| PLANTPOTTING-0011 | Accuracy & trust: real-photo eval harness + confidence abstention + no-training accuracy levers | done (opus) | **Confronted the 0010 confident-wrong finding behind the frozen seam — no model swap, no training.** `AccuracyEvalTest` scorecard (local `pixel6Api34`); CC0/PD/CC-BY fixtures **8 → 41 photos / 30 species**. **MEASURE** BEFORE confident-wrong **0.382**. **IMPROVE**: **TTA-6** adopted (center-crop dropped). **ABSTAIN**: new `high_confidence_abstain_margin=0.30` → **AFTER confident-wrong 0.179** (0.098 clean), top-1 ~flat, held-out validated. Plus thicker confidence bar, **all 11 botanical plates → real photos** + in-app **Image-credits**. v0.5.0. |
| PLANTPOTTING-0012 | Pothos↔Pilea boundary fix: ship Pilea KB entry behind disambiguation gating + fixture/TTA support | done (opus) | **The 0009 Pilea deferral LIFTED — Pilea mapped, but only in lockstep with a pothos↔Pilea gate.** +19 CC0 fixtures → **60 photos / 39 species**; class-map **38 → 39**. Gate in `ModelScoreMapper`: top-1=Pilea → `LowConfidencePicker` with Pilea + pothos surfaced. CI-bound by `pileaMappingRequiresBoundaryGate`. **Adding Pilea = +0 confident-wrong on every surface** (vs +2/+9/+6 naive); 0 pothos→direct-Pilea. Pilea ships **strict-picker (no direct card)**. TTA stays ×6. v0.6.0. **Review surfaced the next move:** real Pilea ID'd correctly @ 98% but lands in the picker → next = a direct Pilea card under an elevated bar. |
| PLANTPOTTING-0013 | Direct Pilea care card behind elevated held-out gate + broad CC0 fixture growth + doc cleanup | done (opus) | **A confident, correct Pilea now gets a direct card** without re-opening the pothos→Pilea boundary. `ModelScoreMapper` gate refined: top-1=Pilea → direct card **only** when `bestProb ≥ per_species_thresholds["pilea-peperomioides"]=0.98`, else picker; CI-bound **> 0.9661**. Decision via a pre-registered **LOO + author-separated** sweep (shipped tta6: pothos ≤ 0.9063, real-Pilea ≥ 0.9940, **+0 confident-wrong every fold**). **On-device (66 fixtures): Δ+0 confident-wrong**, **0 pothos→direct-Pilea**, **6/6 real-Pilea → correct direct card**; per-base clean top-1 **0.417 → 0.530**. **Thread B:** +6 CC0 fixtures (60 → 66); Begonia + Schlumbergera supply-ceilinged. `tta` stays 6. v0.7.0. **Review clean** — one cosmetic bug (no Pilea hero image, gap 3). |

## Known gaps

**The window's headline arc: confident-wrong was measured and roughly halved (0011), the pothos↔Pilea
boundary was closed without re-opening it (0012 gate → 0013 direct card), and the KB/coverage delta was
filled to 39-of-47. What's still open is honest *per-species* depth, the messy-capture accuracy frontier,
a handful of Pilea residuals, the deferred fine-tune — and, net-new this pass, Play Store
release-readiness.** Re-prioritized after the 0013 ship/review and the two accepted inbox fold-ins:

0. **★ Real-world accuracy: measured & reduced under clean conditions (0011), but messy-capture accuracy
   and thin per-species depth stay open.** 0011 built the first honest `AccuracyEvalTest` scorecard behind
   the frozen seam (no model swap, no training) and drove **confident-wrong 0.382 → 0.179** (0.098 on
   clean) via TTA-6 + a 0.30 abstain margin — top-1 held flat, held-out validated (tune-on-clean/eval-on-
   perturbation 0.186; leave-one-species-out 0.167–0.188), at an accepted pick-manually cost of 0.089 →
   0.323. **What's still open:** the win is *measured-under-clean + synthetic-robustness, NOT real-world
   solved*, and the fixture set — though grown to **66 photos / 39 species** (0012 +19, 0013 +6) — still
   rests **31 of 39 species on 1–2 photos**, so the per-species headline isn't trustworthy. The principal's
   standing ask (0013 review): **≥3 CC0/PD photos per species**, then sweep higher **TTA ×8/×10/×20**
   mapping confident-wrong vs latency up to a **~2 s worst-case budget** (0011 capped TTA at 6 ≈ 125 ms).
   The license-clean supply ceiling is the blocker (0008's 1.0–6.8% figure; Begonia + Schlumbergera already
   hit documented ceilings — see gap 2). Fine-tuning stays **deferred** (gap 4).
   `feedback/PLANTPOTTING-0011/feedback.md`, `feedback/PLANTPOTTING-0013/feedback.md`.

1. **The 28 delta mappings (0009's 16 + 0010's 12) are unprobed — editorial coverage, not real-photo
   calibrated.** Mapped coverage moved **10 → 26 → 38 → 39 of 47**, but the new rows' per-class confidence
   behaviour is assumed-from-routing, **not measured against real photos** (unlike the 0006/0007 in-vocab
   probes for Monstera and Crassula). A coarse/genus row could fire a confident-but-marginal card. Closing
   it needs imagery/probing — folds into the gap-0 fixture-depth work. **8 model classes remain
   deliberately unmapped** (route through `LowConfidencePicker` / the 0010 "Add this plant" flow). Further
   text-only expansion over the remaining slice is still cheap and available.

2. **Pilea boundary + direct card SHIPPED (0012 gate → 0013 direct card) — CLOSED; residual tuning open.**
   The 0009 deferral is lifted: 0012 mapped Pilea (38→39) only in lockstep with a `ModelScoreMapper`
   boundary gate (top-1 = Pilea → picker with Pilea + pothos surfaced; CI-bound by
   `pileaMappingRequiresBoundaryGate`), and 0013 added a **direct** Pilea card above an elevated
   `per_species_thresholds["pilea-peperomioides"] = 0.98`, composed with — never replacing — the gate and
   **CI-bound > 0.9661** by `HousePlantClassMapValidationTest`. Validated by a pre-registered leave-one-out
   + author-separated sweep (6 distinct-author fixtures, +0 confident-wrong every fold) and **confirmed in
   the wild** (0013 review: real Pilea → correct direct card; 6/6 real-Pilea fixtures, 0 pothos→direct-Pilea).
   The headline gap is closed. **Residual / open pieces from the 0013 review**
   (`feedback/PLANTPOTTING-0013/feedback.md`):
   - **(a) Re-derive a lower-but-safe `T_pilea`** to admit more genuine Pilea. ~0.97 stays CI-green: the
     `> 0.9661` floor is the *single-crop diagnostic* pothos ceiling, while the **shipped tta6** ceiling is
     only **0.9063** — real headroom. (The principal's initial "drop to 0.9" was flagged unsafe — below both
     the tta6 ceiling and the CI bind.) Needs more real Pilea photos (gap 0).
   - **(b) Harmonise the per-species "bespoke rules."** Pilea is the **only** species with bespoke rules
     (`boundary_pairs` + `per_species_thresholds`); every other species runs the global 0.55 + 0.30 abstain
     margin. Design call: a **general per-species/per-pair mechanism** any species can opt into (keeps the
     asymmetric pothos→Pilea guard — recommended) vs. collapsing back to global rules.
   - **(c) AIY second-opinion cascade — SPIKE, measure first.** Route primary-model abstentions through the
     still-bundled AIY anchor (with TTA-6). **Weak prior** (AIY vocab overlaps only ~5 of 39 species) +
     confident-wrong risk → cheapest first step is an **offline recoverable-abstention count** on the
     66-fixture set; a better framing may be **agreement-as-confidence** than AIY override. Must clear the
     same +0-confident-wrong held-out bar Pilea was held to.

3. **★ Pilea direct card shows NO hero image (placeholder only) — found in 0013 review.** Among the 39
   mapped species, `pilea-peperomioides` is the **only one** with no CC0/PD `.webp` in `res/drawable-nodpi/`
   — `PlantImageResolver` falls back to `ic_plant_placeholder`. Pilea was strict-picker until 0013, so its
   image was never added (it has shown the placeholder in the picker since 0012). `ReferenceImageManifestTest`
   only guards image→manifest (license), not species→image, so nothing went red. **Close:** source a CC0/PD
   Pilea photo via the `/reference-photos` skill + manifest + `docs/licenses/`, and add a test binding
   card-reachable species → non-placeholder drawable so a newly-promoted species can't regress. Small,
   self-contained.

4. **6 KB species remain out-of-vocab + the fine-tune is DEFERRED (training-bound).** The 0008 spike
   answered the data question — **PARTIAL-GO**: 5 OOV species (chlorophytum-comosum, philodendron-hederaceum,
   hoya-carnosa, monstera-adansonii, ficus-lyrata) have sufficient CC imagery to fine-tune; the pothos/Pilea
   boundary was CONDITIONAL (Pilea side thin ~76); `philodendron-pink-princess` is **NO-GO** (cultivar-proven
   CC imagery only ~5–15). The user's **no-self-shot constraint** removes the two paths the outline leaned
   on, so pink-princess + the Pilea-balance work are out. The 5-GO-class fine-tune stays viable on CC data
   alone but is **deferred behind the cheaper KB/accuracy/release levers** as lower ROI per effort. Reusable
   when revisited: the `finetune-sprint-outline.md` approach (47→52, float16 export, `ModelSwapEvaluationTest`,
   `ACTIVE_MODEL_ROOT`). `feedback/PLANTPOTTING-0008/feedback.md`.

5. **★ Not release-ready for the Google Play Store (blocks publication) — net-new this pass (fold-in A).**
   Licensing is already commercial-clean (gap 6); the blockers are engineering/process. (1) `targetSdk`/
   `compileSdk` = **34 < Play minimum** (API 35 now, API 36 from 31 Aug 2026) — bump to 35 (eval 36) + fix
   edge-to-edge / predictive-back Compose regressions; (2) **no release signing** — add `signingConfig` +
   Play App Signing, secrets out of repo; (3) **debug APK only** — Play needs a signed **`.aab`** via
   `bundleRelease`; (4) **placeholder launcher icon** — real adaptive icon + 512×512 Play icon; (5) **R8
   disabled** — enable + TFLite keep rules (`org.tensorflow.**`), verify inference + `AccuracyEvalTest` on
   the minified release variant (ship minify-off for v1 if fragile). **Likely sprint:** `PLANTPOTTING-0014
   (release readiness)`. `docs/future-ideas/play-store-publishing.md`.

6. **No project LICENSE and no privacy policy — net-new this pass (fold-in A).** The repo is "all rights
   reserved" by default (weak deterrent); Play requires a privacy-policy URL (the app holds `CAMERA`). Add a
   root `LICENSE` (**PolyForm Noncommercial 1.0.0**) + a short README licensing section (keep the existing
   per-asset `LICENSE-*.txt` and `docs/licenses/reference-images.md`), and author `docs/PRIVACY.md`
   (on-device-only; camera used only during ID; images not retained/transmitted; no analytics) hosted at a
   public URL. **Licensing is already commercial-clean:** both bundled ML models are Apache-2.0, all 44
   reference photos are Unsplash/Pexels/CC0 (CC-BY-SA banned by policy), no GPL/copyleft, `verifyNoNetworking`
   holds — so PolyForm binds *licensees*, not the principal, and a free launch now keeps later monetisation /
   dual-licensing open.

7. **Monetisation options unexplored — DEFERRED, gated behind the free Launch (0014) — fold-in B.** Raw idea
   #1 in the inbox: a monetisation-options spike (brainstorm ~40 levers → top-5 easiest-to-implement + top-5
   biggest-profit → 2×2 ease-vs-revenue scoring). Named candidate levers: in-app ads, a paid/pro Play tier,
   subscription, substrate-supplier affiliate revenue, vertical integration (sell substrate directly),
   horizontal referral partnerships (pots / supplies). **Not scoped this pass** — recorded as a single
   forward-looking gap + a V3 milestone sketch; it is gated behind shipping the free Launch and is **not** a
   sprint yet. `docs/future-ideas/feature-ideas.md` (raw idea #1).

### Standing non-goals (carried from 0005 §2.4)

- **Delegate / quantization variants.** No INT8 / GPU / NNAPI delegate yet. *(0014 R8/minify is build
  shrinking, not a delegate change.)*
- **No net-new screens.** No `CaptureFailedScreen`, `Settings`, or `ModelInfoScreen` for accuracy work.
- **No ML training or retraining.**
- **`PlantIdentifier` / `IdentificationResult` interface changes.** Frozen per 0003 §4.4.
- **KB edits.** Locked (except sprint-scoped fixture/image/licensing work; 0014 changes no KB/model content).
- **AGP / Kotlin / Compose / Hilt / TFLite version bumps.** Locked — **except `targetSdk`/`compileSdk`
  34→35 (eval 36), which PLANTPOTTING-0014 LIFTS** to clear the Play target-API requirement (fold-in A).
- **README rewrite.** Append-only — 0014 **appends a licensing section** (allowed); link this roadmap otherwise.
- **`expected-artifacts` re-baselining.** Off the table unless a scoped release-build artifact requires it.

### Closed this window (removed, not carried)

- ~~**The model recognises wild flora, not houseplants.**~~ Closed in 0007: AIY V1/3 replaced as the default
  by `house_plant_species_mobilenetv2` (in-vocab 2/16 → 10/16, top-1 high-conf 1 → 6, latency 43 → 33 ms).
  Residue tracked as gap 4 (6 OOV).
- ~~**Model swap requires a selection spike / harness.**~~ Closed in 0007: decision matrix, fixture harness,
  `ModelSwapEvaluationTest`, `ACTIVE_MODEL_ROOT`, and a running prototype shipped (reusable for the next
  model iteration).
- ~~**0005/0006 UX calibration cleanup.**~~ Closed in 0006: Crassula probe landed honestly at 0.1055
  low-confidence; subtitle de-jargoned; `(0%)` chips drop the suffix and stay selectable.
- ~~**Pilea silently unmapped / pothos↔Pilea confident-wrong boundary.**~~ Closed across 0012 (gate) + 0013
  (direct card) — see gap 2; only residual tuning remains.

### Accepted as-is (recorded, not an open gap)

- **Failure-banner live visual (§7.6).** No production-accessible path drives the camera into
  `CameraUiState.Failure` for a live visual check; the user explicitly waived this — code review + JVM
  coverage accepted. Recorded for history only; do not relist unless a future sprint makes it load-bearing.

## Proposed Sprint Path

### Active horizon (detailed)

#### Shipped — window 0006 → 0013 (all reviewed clean; one-liners)
- **0006 — V0.1 calibration close:** Crassula probe added (0.1055, honest low-conf), subtitle + `(0%)` chip
  UX fixed; review elevated the model-swap milestone.
- **0007 — V1 entry / model swap:** `house_plant_species_mobilenetv2` became the production default; AIY kept
  as the regression anchor; seam unchanged.
- **0008 — data-availability spike:** fine-tune feasibility PARTIAL-GO; the no-self-shot constraint re-pointed
  work to text-only KB expansion; shutter-on-return UX fixed.
- **0009 — KB-expansion 1 (text-only):** species **16 → 32**, mapped **10 → 26 of 47**, +`carnivorous-peat-sand`
  archetype; Pilea deliberately deferred. v0.3.0.
- **0010 — app experience:** DataStore, Home, My Plants, confidence bar, "Add this plant", 44 reference
  photos, contained search; KB **32 → 44 / 38-of-47**. v0.4.0. **Review surfaced confident-wrong as the headline.**
- **0011 — accuracy & trust:** scorecard + TTA-6 + 0.30 abstain margin cut confident-wrong **0.382 → 0.179**;
  all botanical plates → real photos. v0.5.0.
- **0012 — Pilea mapped safely:** class-map **38 → 39**, Pilea strict-picker behind the pothos↔Pilea gate,
  **+0 confident-wrong** vs naive +2/+9/+6; +19 CC0 fixtures (→60/39); TTA stays 6. v0.6.0.
- **0013 — direct Pilea card:** direct Pilea at `T_pilea = 0.98` (LOO + author-separated, +0 cw, CI-bound
  > 0.9661); **6/6 real-Pilea → correct direct card**, 0 pothos→direct-Pilea; fixtures **60 → 66**; per-base
  clean top-1 0.417 → 0.530. v0.7.0 released. **Review: clean** — one cosmetic bug (no Pilea hero image, gap 3).

#### Next — PLANTPOTTING-0014: release readiness (Launch track) — *detailed; fold-in A*
- **Intent:** make PlantPotting publishable as a **free** Google Play release without changing the frozen
  identifier seam, KB, model, or training state. Runs **parallel** to V1 accuracy/fixture work — it does not
  block or depend on accuracy work.
- **Entry conditions:** 0013 `status: done` and its review PR merged to `origin/main` (✓); this refresh
  merged; principal decisions locked (public repo, PolyForm Noncommercial 1.0.0; **Personal** Play account;
  **free launch**, monetisation deferred).
- **Code / config track:**
  1. Root `LICENSE` = PolyForm Noncommercial 1.0.0 (filled licensor line) + short README licensing section;
     keep per-asset licenses.
  2. Bump `targetSdk`/`compileSdk` **34 → 35** (eval 36) + fix API-35 edge-to-edge / predictive-back Compose
     regressions.
  3. Release `signingConfig` + Play App Signing; keystore + secrets **out of the repo**.
  4. `bundleRelease` → signed **AAB** (Play no longer accepts APKs for new apps).
  5. Enable R8 (`isMinifyEnabled = true`) + TFLite keep rules (`org.tensorflow.**`); **verify inference +
     `AccuracyEvalTest` on the minified release variant** (TFLite + reflection can break under R8) — ship
     minify-off for v1 if fragile.
  6. Real launcher icon (replace placeholder `ic_launcher_foreground.xml`) + 512×512 Play icon.
  7. `docs/PRIVACY.md` (on-device classification; camera only during ID; images not retained/transmitted;
     local records; no analytics) at a public URL (GitHub Pages).
  8. Version → **v1.0.0** (`versionCode 8`); keep gates green (`ktlintFormat` first per the Windows autocrlf
     note). NB: `release.yml` publishes a GitHub Release **only on a pushed `v*` tag** — a `versionName` bump
     alone never publishes.
- **Play-process track (plan-tracked, not code):** $25 Personal account + ID verification; Data Safety =
  *none collected*; content rating (IARC, likely Everyone); store-listing assets (512×512 icon, 1024×500
  feature graphic, ≥2 phone screenshots, descriptions, category); the **12-tester / 14-continuous-day closed
  test** (Personal accounts post-Nov-2023 — recruit testers **first**, it's the schedule long pole). The
  existing tag-triggered `release.yml` (debug APK → GitHub) can stay for side-loaders, separate from Play.
- **Exit criteria:** signed release **AAB** of a min-API-35 build; PolyForm-Noncommercial-licensed public
  repo; privacy policy live; icon/store assets ready; Data Safety + content rating complete; closed-test gate
  passed; production rollout ready.
- **Milestone contribution:** the **Launch** milestone (below).

#### Also-next — V1 accuracy continuation (candidates from the 0013 review) — *runs alongside the Launch track; SID TBD*
- **Quick fix — Pilea hero image (gap 3).** Source a CC0/PD Pilea photo via `/reference-photos`, wire it into
  `PlantImageResolver` + manifest + `docs/licenses/`, and add a test binding card-reachable species →
  non-placeholder drawable. Small; could ride along with any sprint.
- **Deepen the fixture set to ≥3 photos/species + re-derive a lower-but-safe `T_pilea` (gaps 0, 2a).** Target
  ≥3 CC0/PD fixtures per species (31/39 still on 1–2); with more real Pilea in hand, re-derive the lowest
  `T_pilea` that still clears the shipped tta6 pothos ceiling (0.9063) with margin (~0.97 stays CI-green).
  Widen sources beyond iNaturalist (Wikimedia / GBIF / Pexels) to break the Begonia + Schlumbergera ceilings;
  consider whether `schlumbergera-bridgesii` should remap to *truncata*.
- **Harmonise the per-species bespoke rules (design, gap 2b).** A general per-species/per-pair mechanism any
  species can opt into (keeps the asymmetric pothos→Pilea guard — recommended) vs. collapsing to global rules.
- **AIY second-opinion cascade (SPIKE, measure first, gap 2c).** Offline recoverable-abstention count on the
  66-fixture set first; a better framing may be agreement-as-confidence. Must clear the +0-confident-wrong
  held-out bar.
- **Notes:** **no self-shot / first-party imagery anywhere** (hard user constraint) and **no model training**
  — behind the frozen seam. A residual 0010 refinement (My-Plants-survives-uninstall via Auto Backup or local
  export — mind the no-cloud-sync non-goal) can fold in opportunistically or sit under V2.

### Milestone ladder (skeleton — sprints become detailed as they approach)

#### MVP — On-device ID + KB-driven potting-mix recommendation — *(shipped ~PLANTPOTTING-0003)*
Camera → on-device identification → routing decision → KB-driven recipe, network-free, for the bundled
species set.

#### V0.1 — Trustworthy confidence calibration — *(met ~PLANTPOTTING-0006)*
- **Met:** real-photo probes for both in-vocab species (*Monstera deliciosa* @ 0.8984, high-conf;
  *Crassula ovata* @ 0.1055, low-conf) with accuracy assertions in CI; the per-species threshold mechanism
  wired and `perSpeciesThresholds` empty by design. Further depth required the V1 model swap.

#### V1 — Broad species coverage — *(active since PLANTPOTTING-0007, substantially advanced)*
- **Exit criteria:** most common houseplants identify directly so the low-confidence path is the exception,
  not the rule — at honest, measured accuracy.
- **Progress:** 0007 swap moved coverage 2/16 → 10/16; 0009/0010 took the cheap text-only lever to **39 of
  47** mapped; 0011 measured & roughly halved confident-wrong (0.382 → 0.179) behind an abstention mechanism;
  0012/0013 closed the pothos↔Pilea boundary and shipped a direct Pilea card. **The open piece is honest
  per-species depth** (31/39 on 1–2 photos), calibration of the unprobed delta rows, and the messy-capture
  frontier — see gaps 0–2.
- **Skeleton sprints:** ~~model-selection spike (0007 ✓)~~; ~~training-data spike (0008 ✓ PARTIAL-GO)~~;
  ~~KB-expansion (0009 ✓)~~; ~~accuracy & trust (0011 ✓)~~; ~~pothos↔Pilea gate + direct card (0012/0013 ✓)~~;
  **next → ≥3-photos/species fixture deepening + lower-but-safe `T_pilea` + rule harmonisation + AIY-cascade
  spike + Pilea hero image**; *deferred* → fine-tune for the 5 GO OOV classes (CC data only, no self-shot).

#### Launch — Google Play Store public release — *(new this pass; parallel to V1, does not block it)*
- **Exit criteria:** a signed release **AAB** of a min-API-35 build, a PolyForm-Noncommercial-licensed public
  repo, a live privacy policy, store listing + Data Safety + content rating complete, and the **12-tester /
  14-continuous-day closed test** passed → production rollout.
- **Free launch** (PolyForm Noncommercial 1.0.0; Personal Play account). Licensing already commercial-clean
  (both ML models Apache-2.0; all reference photos Unsplash/Pexels/CC0; no copyleft; `verifyNoNetworking`
  holds) — the work is release-engineering + Play process, behind the frozen seam.
- **Skeleton sprints:** `PLANTPOTTING-0014` release readiness; then a 0014-review + closed-test-management
  follow-up if the tester gate surfaces issues.

#### V2 — Richer care guidance
- **Exit criteria:** beyond the one-shot recipe — repotting schedule, care reminders, or personalization to
  the user's plant set.
- **Skeleton sprints:** care-profile model; reminder UX; plant-collection persistence/backup
  (My-Plants-survives-uninstall via Auto Backup or local export lands here unless Launch forces the
  backup/privacy decision earlier).

#### V3 — Sustainable product / monetisation — *(deferred; gated behind the free Launch — gap 7)*
- **Only after the free Launch ships.** A monetisation-options spike (40 ideas → top-5 ease + top-5 profit →
  2×2 ease-vs-revenue scoring) and any chosen commercial levers (ads / paid-pro tier / subscription /
  substrate affiliate / vertical integration / referral partnerships). Kept entirely separate from the 0014
  free release. **Sketch only** — not scoped.

#### Beyond V3 — Plant-health diagnostics
- Identify stress / pest / over-watering signs from the same photo pipeline (sketch only).

## Changelog

- 2026-06-09 — **refreshed through PLANTPOTTING-0013** (first true REFRESH since 0005 — 0006→0012 close-outs
  were BUMP-only, so the heavy sections had drifted). **Rebuilt Species/Model Coverage** (was stale at
  44/38; now **45 species / 39-of-47 mapped**, Pilea direct card at `=0.98`, fixtures **66/39**, per-base
  clean top-1 **0.417 → 0.530**, confident-wrong **0.179**). **Reconciled Known Gaps** across the 0011
  accuracy work, the 0012 Pilea gate, and the 0013 direct card + its residuals (lower-but-safe `T_pilea`,
  per-species rule harmonisation, missing Pilea hero image, AIY second-opinion spike); removed the closed
  wild-flora / model-swap-harness / 0006-UX / Pilea-unmapped gaps. **Consolidated** the five stacked
  "Current state (post-XXXX)" narrative blocks into one current view (per-sprint detail preserved in Sprint
  History + this Changelog). **Folded in two accepted idea-inbox items:** (A) a net-new **Launch track —
  Google Play Store release** (public repo under **PolyForm Noncommercial 1.0.0**; bundled assets already
  commercial-clean) → a detailed **PLANTPOTTING-0014 (release readiness)** Next block + a **Launch** milestone
  parallel to V1 + two Layer-Status rows (Licensing & legal; Release engineering) + the targetSdk 34→35 lock
  lift; and (B) the **monetisation spike** as a deferred Known Gap (7) + a **V3** milestone sketch, gated
  behind the free Launch. Recomputed stats (~14,893 Kotlin LOC; 982/1034 acceptance). Multi-model drafting:
  **codex + claude** (agy quota-down — silent no-write; the DeepSeek/OpenRouter fallback was blocked on a
  1Password non-interactive auth timeout → proceeded two-model). Drafts under `docs/roadmap/drafts/`.
- 2026-06-07 — bumped through PLANTPOTTING-0012 (review close-out): reconciled the pothos↔Pilea
  boundary-fix sprint (gates clean; v0.6.0 APK verified). Lifted the 0009 Pilea deferral safely — Pilea
  mapped (38→39) only behind a `ModelScoreMapper` gate, +19 CC0 fixtures (→ 60 photos / 39 species), **+0
  confident-wrong on every surface** vs +2/+9/+6 naive. Pilea shipped strict-picker. Review re-pointed the
  next horizon to a direct Pilea card.
- 2026-06-07 — bumped through PLANTPOTTING-0011 (review close-out): clean review; recorded the first honest
  `AccuracyEvalTest` scorecard, fixtures **8 → 41 photos / 30 species**, **TTA-6 adopted**, and
  `high_confidence_abstain_margin = 0.30` → **confident-wrong 0.382 → 0.179**, held-out validated. Folded in
  a thicker confidence bar + all 11 botanical plates → real photos. v0.5.0.
- 2026-06-06 — bumped through PLANTPOTTING-0010 (review close-out): clean review; recorded the first
  local-persistence layer (DataStore), Home/landing, My Plants, confidence %+bar, contained search, the
  "Add this plant" wireframe, 44 CC0/PD reference photos, and the KB expansion to **44 species / 38-of-47
  mapped**. v0.4.0. New headline gap: real-world ID accuracy is poor (confidently wrong) → re-pointed the
  horizon to an accuracy/calibration sprint.
- 2026-06-05 — bumped through PLANTPOTTING-0008 (review close-out): clean review; recorded the **PARTIAL-GO**
  data-availability verdict and the no-self-shot constraint that re-scoped V1 to a text-only KB-expansion;
  the 5-GO-class fine-tune deferred.
- 2026-06-05 — bumped through PLANTPOTTING-0007 (review close-out): clean review; recorded the houseplant
  model swap (`house_plant_species_mobilenetv2` replaces AIY as the `ACTIVE_MODEL_ROOT` default; coverage
  2/16 → 10/16; AIY kept as anchor). Closed the wild-flora gap; opened the 6-OOV + pothos→Pilea gap.
- 2026-06-04 — bumped through PLANTPOTTING-0006 (review close-out): completed the V0.1 multi-species
  calibration sweep (Crassula probe @ 0.1055); closed two 0005-review UX gaps; elevated the model swap to the
  next-up V1 milestone.
- 2026-06-04 — refreshed through PLANTPOTTING-0005 (close-out): closed the §5.4–§5.8 real-photo calibration
  chain and the integration-flow / GMD-run gaps; advanced calibration to probe-backed; re-scoped 0006.
- 2026-06-04 — added roadmap-skill frontmatter (`last_updated`, `through_sid`), a Species/Model Coverage
  section, and reshaped "Candidate next sprint" into a Proposed Sprint Path. First entry.
