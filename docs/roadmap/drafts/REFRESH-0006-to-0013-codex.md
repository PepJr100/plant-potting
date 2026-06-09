## Known gaps

**Open after PLANTPOTTING-0013 — accuracy is improved and Pilea is safe/direct, but the next limits are
fixture depth, release readiness, and policy/process work.** Reconciled from PLANTPOTTING-0006 through
0013 plus the accepted idea-inbox fold-ins.

0. **★ Real-world accuracy: measured & reduced under clean conditions; still not real-world solved.**
   0010 surfaced the headline trust failure: messy on-device captures could be confidently wrong (snake
   plant roughly 1-in-3 correct, otherwise confidently another plant). 0011 closed the unmeasured part:
   `AccuracyEvalTest`, real-photo fixtures, synthetic perturbations, TTA-6, and
   `high_confidence_abstain_margin = 0.30` drove confident-wrong **0.382 → 0.179** (clean **0.268 → 0.098**),
   with held-out validation and accepted pick-manually cost **0.089 → 0.323**
   (`docs/sprints/PLANTPOTTING-0011.md`, `docs/sprints/feedback/PLANTPOTTING-0011/feedback.md`). 0013
   refreshed the fixture set to **66 photos / 39 species** and per-base clean top-1 **0.417 → 0.530**, but
   the review accepted that headline as honest measurement, not a solved per-species or messy-capture claim:
   **31 of 39 species still rest on only 1-2 photos** (`docs/sprints/feedback/PLANTPOTTING-0013/feedback.md`).
   **Close direction:** deepen the real fixture set to 3+ CC0/PD photos/species where supply allows, then
   use the harness to re-measure any lever; higher TTA should be swept only after the real fixture set is
   larger and still under the ~2 s worst-case budget noted in 0011 feedback.

1. **Mapped coverage is broad but still thinly calibrated.** 0009 and 0010 converted model-vocabulary
   coverage into care-card coverage with text/config work: `species.json` **16 → 32 → 44**, active
   class-map **10 → 26 → 38 of 47**, then 0012/0013 moved to **45 species / 39 of 47 mapped** by adding
   Pilea safely (`docs/sprints/PLANTPOTTING-0009.md`, `docs/sprints/PLANTPOTTING-0010.md`,
   `docs/sprints/PLANTPOTTING-0012.md`). That closed the cheap model/KB delta. The residue is calibration:
   many 0009/0010 delta rows are exact/coarse editorial mappings, not species-level real-photo behaviour.
   0012 covered the 8 previously untested mapped species with one fixture each; 0013 deepened 6 of those 8
   but left **Begonia + Schlumbergera** supply-ceilinged (CC0 Begonia mostly wild field species; CC0
   Schlumbergera mostly *S. truncata*, not the KB's `schlumbergera-bridgesii`) per 0013 feedback. **Close
   direction:** broaden sources beyond iNaturalist (Wikimedia/GBIF/Pexels/Unsplash where license-clean) and
   consider whether the Schlumbergera KB mapping should follow the available *truncata* taxonomy.

2. **Pilea boundary/direct-card gap is closed; residual tuning is open.** 0012 lifted the Pilea deferral only
   in lockstep with a pothos↔Pilea boundary gate: top-1=Pilea routed to the picker, adding Pilea with
   **+0 confident-wrong** vs. **+2/+9/+6** for naive mapping, and correct-pothos picker delta **+0**
   (`docs/sprints/feedback/PLANTPOTTING-0012/feedback.md`). 0013 then shipped a direct Pilea card under
   `per_species_thresholds["pilea-peperomioides"] = 0.98`, CI-bound **> 0.9661**, with leave-one-out +
   author-separated evaluation, **0 pothos→direct-Pilea**, and **6/6 real-Pilea fixtures → correct direct
   card** (`docs/sprints/PLANTPOTTING-0013.md`, `docs/sprints/feedback/PLANTPOTTING-0013/feedback.md`).
   **Residuals:** re-derive a lower-but-safe `T_pilea` after more real Pilea photos (0.9 is unsafe; ~0.97
   may be plausible but must clear the documented ceiling), and harmonise the Pilea-specific bespoke rules
   into a general per-species/per-pair policy instead of one-off code-path thinking.

3. **★ Pilea card has no hero image.** 0013 review found `pilea-peperomioides` is the only mapped/card-reachable
   species falling back to `ic_plant_placeholder`; `PlantImageResolver` lacks a Pilea mapping and
   `ReferenceImageManifestTest` only guards image→manifest, not mapped-species→non-placeholder
   (`docs/sprints/feedback/PLANTPOTTING-0013/feedback.md`). **Close direction:** source a CC0/PD Pilea photo,
   add resolver + manifest/license rows, and add a reverse test that every direct-card-eligible mapped species
   has a non-placeholder drawable.

4. **Six KB species remain out-of-vocab; fine-tune is deferred.** 0008 found a **PARTIAL-GO** data path:
   five OOV species have enough license-clean imagery (`chlorophytum-comosum`, `philodendron-hederaceum`,
   `hoya-carnosa`, `monstera-adansonii`, `ficus-lyrata`), while `philodendron-pink-princess` is **NO-GO**
   at cultivar-proven ~5-15 and the pothos/Pilea retrain side was only conditional. Review then made the
   no-self-shot constraint hard, removing the pink-princess and Pilea-balance fallback paths
   (`docs/sprints/PLANTPOTTING-0008.md`, `docs/sprints/feedback/PLANTPOTTING-0008/feedback.md`). Fine-tune
   remains viable only for the 5 GO classes on CC data alone, but it is lower ROI than fixture depth,
   calibration, and release readiness.

5. **AIY second-opinion cascade is unproven and risky.** 0013 review proposed routing primary low-confidence /
   abstain cases through the still-bundled AIY V1/3 model with TTA6. It is mechanically feasible because AIY
   still ships as the regression anchor, but the prior is weak: AIY overlaps only about **5 of 39** mapped
   species and 0011's main lesson was to avoid reintroducing confident-wrong. **Close direction:** measurement
   spike only: offline count recoverable primary-model abstentions on the 66-fixture set; require the same
   **+0 confident-wrong** held-out bar. Prefer "agreement-as-confidence" over AIY override if the data supports it
   (`docs/sprints/feedback/PLANTPOTTING-0013/feedback.md`).

6. **★ Not release-ready for the Play Store (blocks publication).** From the accepted Play Store fold-in:
   (1) `targetSdk`/`compileSdk` = 34 is below Play's current minimum API 35 (API 36 from 31 Aug 2026);
   (2) no release signing / Play App Signing config; (3) only debug APK delivery, not a signed `.aab`;
   (4) placeholder launcher icon; (5) R8 is off and TFLite needs keep-rule verification on a minified release
   build. **Likely sprint:** `PLANTPOTTING-0014 — release readiness`
   (`docs/future-ideas/play-store-publishing.md`).

7. **No project LICENSE and no privacy-policy URL.** The repo is effectively all-rights-reserved by default,
   while the accepted decision is a public repo under **PolyForm Noncommercial 1.0.0**. Play also requires a
   privacy-policy URL because the app holds `CAMERA`. Bundled licensing is already commercial-clean: both ML
   models are Apache-2.0, reference photos are Unsplash/Pexels/CC0/allowed attributions, no copyleft, and
   `verifyNoNetworking` holds (`docs/future-ideas/play-store-publishing.md`,
   `docs/future-ideas/feature-ideas.md`). **Close direction:** add root `LICENSE`, README licensing append, and
   `docs/PRIVACY.md` hosted publicly.

8. **Monetisation options are untriaged, deliberately deferred behind free Launch.** Raw idea #1 asks for a
   monetisation-options spike: think of 40 ideas, refine to top-5 easiest-to-implement and top-5 biggest-profit,
   then score those and the named levers on a 2x2 ease-vs-revenue grid. Candidate levers named in the inbox:
   in-app ads, paid/pro Play tier, subscription, substrate-supplier affiliate revenue, vertical integration
   (sell substrate), and horizontal referral partnerships (`docs/future-ideas/feature-ideas.md`). **Do not
   scope as a sprint now:** gated behind the free Play launch / 0014.

Standing non-goals, with Launch exception:

- **Delegate / quantization variants.** No INT8 / GPU / NNAPI delegate yet.
- **No net-new screens** for accuracy work. Launch may add Play/store assets and docs, not new app workflows.
- **No ML training or retraining.**
- **`PlantIdentifier` / `IdentificationResult` interface changes** stay frozen unless a future cascade spike
  explicitly proves a seam change is worth it.
- **KB edits locked** except sprint-scoped fixture/image/licensing work; 0014 does not change KB/model content.
- **AGP / Kotlin / Compose / Hilt / TFLite version bumps locked.** 0014 explicitly lifts only
  `targetSdk`/`compileSdk` **34 → 35** (evaluate 36) for Play target-API compliance.
- **README rewrite off the table.** 0014 may append a licensing section.
- **`expected-artifacts` re-baselining** remains off the table unless a scoped release-build artifact requires it.

Closed this window — remove from the carried gap list:

- ~~**The model recognises wild flora, not houseplants.**~~ Closed by 0007: production default swapped from AIY
  V1/3 to `house_plant_species_mobilenetv2` (Apache-2.0), coverage **2/16 → 10/16**, top-1 high-conf **1 → 6**,
  latency **43 ms → 33 ms**, seam unchanged (`docs/sprints/feedback/PLANTPOTTING-0007/feedback.md`).
- ~~**Model swap requires a selection spike / harness.**~~ Closed by 0007: decision matrix, fixture harness,
  `ModelSwapEvaluationTest`, `ACTIVE_MODEL_ROOT`, and running prototype shipped.
- ~~**0005/0006 UX calibration cleanup.**~~ Closed by 0006: Crassula probe landed honestly at **0.1055**
  low-confidence; subtitle de-jargoned; `(0%)` chips now drop the suffix and stay selectable
  (`docs/sprints/feedback/PLANTPOTTING-0006/feedback.md`).
- ~~**Pilea unmapped / strict-picker only.**~~ Closed by 0012/0013. Carry only residual threshold/image/policy
  work above.

Accepted as-is, not an open gap:

- **Failure-banner live visual (§7.6).** User waived it in the 0005/0006 era; JVM coverage accepted. Do not
  relist unless a future sprint makes it load-bearing.

## Species/Model Coverage

How much of the bundled KB the on-device model identifies verbatim, and the calibration state after
PLANTPOTTING-0013.

| Axis | State | Notes |
| --- | --- | --- |
| KB species | **45** | `species.json`: 16 original → 32 after 0009 → 44 after 0010 → **45** after 0012 Pilea. 0013 added fixtures/routing only, no KB species. |
| Active model mapped classes | **39 of 47** | `house_plant_species_mobilenetv2` class map: 10 after 0007, 26 after 0009, 38 after 0010, **39** after 0012. Pilea is mapped and direct-card eligible only under the 0012/0013 gate stack. |
| Unmapped active-model classes | **8 of 47** | Pilea is no longer unmapped. Remaining unmapped classes route through `LowConfidencePicker` / 0010 "Add this plant" rather than a care card (e.g. still-deliberate seasonal / non-houseplant / ambiguous model labels documented in mapping notes). |
| KB species OOV in active model | **6** | `monstera-adansonii`, `philodendron-hederaceum`, `philodendron-pink-princess`, `ficus-lyrata`, `chlorophytum-comosum`, `hoya-carnosa`. 0008: five GO on CC data; pink-princess NO-GO; no-self-shot makes fine-tune deferred. |
| Real-photo fixtures | **66 photos / 39 species** | 0011 built the scorecard set; 0012 grew it to 60 / 39 with Pilea+pothos+untested species; 0013 added +6 CC0 photos across 6 of the 8 single-photo mapped species. Still thin: **31/39 species have 1-2 photos**. |
| Real-photo accuracy | per-base clean top-1 **0.417 → 0.530**; confident-wrong **0.179** | 0013 per-base clean top-1 rose mainly from Pilea direct cards and some new second photos. Confident-wrong remains the 0011 trust headline: **0.382 → 0.179** overall after TTA-6 + abstain margin; 0013 clean cw stayed flat-ish (**0.117 → 0.121**) and perturbation cw improved slightly (**0.221 → 0.210**). Honest caveat: measured-under-clean + synthetic-robustness, not real-world solved. |
| Pilea calibration | direct-card eligible at **`per_species_thresholds["pilea-peperomioides"] = 0.98`** | Boundary gate from 0012 still surfaces Pilea+pothos in the picker below the bar; 0013 allows direct Pilea above it. CI binds the threshold **> 0.9661** and the mapping requires the boundary gate. Shipped TTA-6 pothos→Pilea ceiling measured **≤ 0.9063**, real Pilea **≥ 0.9940**; non-shipped single-crop diagnostics are documented residual risk. |
| Calibration mechanism | global + abstain + one species threshold | Global plain threshold 0.55, margin branch, `high_confidence_abstain_margin = 0.30`, `tta = 6`, and one per-species threshold for Pilea. 0006 anti-overfit rule still holds: no threshold is seeded to bless a weak prediction. |
| Latency | **~125 ms median** on TTA-6 | 0011 adopted center + 4 corners + full-frame softmax averaging; worst observed then 539 ms. 0012 grid tiling did not earn adoption and 3x3 exceeded ~2 s (2686 ms); `tta` stays 6. |
| AIY baseline | bundled regression anchor | AIY V1/3 remains shipped for tests/regression, now resolving about 5 mapped species. Any AIY cascade is a future measurement spike only, not current routing. |
| Licensing / commercial cleanliness | clean for bundled app assets | Active model + AIY are Apache-2.0; reference photos/fixtures are tracked via manifests and allowed licenses; `verifyNoNetworking` remains green. Project-level LICENSE/privacy policy are separate Launch gaps. |

## Proposed Sprint Path

### Active horizon (detailed)

#### Shipped one-liners — PLANTPOTTING-0006 → 0013

- **0006 — V0.1 calibration close:** Crassula probe added (0.1055, honest low-conf), subtitle and `(0%)`
  chip UX fixed; review elevated model swap.
- **0007 — V1 entry / model swap:** `house_plant_species_mobilenetv2` became production default; AIY kept as
  regression anchor; seam unchanged.
- **0008 — data availability spike:** fine-tune feasibility PARTIAL-GO, no-self-shot constraint re-pointed work
  toward text-only KB expansion; shutter-on-return fixed.
- **0009 — KB expansion 1:** species **16 → 32**, active mapping **10 → 26**, Pilea deliberately deferred.
- **0010 — app experience:** DataStore, Home, My Plants, confidence bar, Add-this-plant, reference photos,
  search containment, KB **32 → 44 / 38-of-47**; review surfaced confident-wrong as headline.
- **0011 — accuracy & trust:** scorecard + TTA-6 + abstain margin cut confident-wrong **0.382 → 0.179**;
  all botanical plates became real photos; still not real-world solved.
- **0012 — Pilea mapped safely:** class-map **38 → 39**, Pilea strict-picker behind pothos↔Pilea gate,
  **+0 confident-wrong** vs naive +2/+9/+6; TTA stayed 6.
- **0013 — direct Pilea card:** direct Pilea at `T_pilea=0.98`, **0 pothos→direct-Pilea**, 6/6 real-Pilea
  direct cards, fixtures **60 → 66**, v0.7.0 release published.

#### Next: PLANTPOTTING-0014 — release readiness for Google Play Store (parallel Launch track)

- **Intent:** make PlantPotting publishable as a free Google Play release without changing the frozen identifier
  seam, KB, model, or training state. This runs **parallel** to V1 accuracy/fixture work; it does not block V1.
- **Entry conditions:** 0013 released (`v0.7.0` tag + GitHub Release confirmed) and roadmap refresh merged.
- **Code/config track:**
  - Add root `LICENSE` = **PolyForm Noncommercial 1.0.0** and append a short README licensing section; keep
    per-asset license files/manifests.
  - Add `docs/PRIVACY.md`: on-device classification, camera only during identify, images not retained or
    transmitted, saved plants/request counts local, no analytics/networking; host at a public URL.
  - Lift SDK target lock only for Play compliance: `targetSdk`/`compileSdk` **34 → 35**; evaluate API 36 and fix
    edge-to-edge / predictive-back regressions surfaced by the bump.
  - Add release signing config and Play App Signing path with keystore/secrets out of repo.
  - Produce signed release **AAB** via `bundleRelease`; keep debug APK/GitHub Release flow for sideloaders.
  - Enable R8/minify with TFLite keep rules; verify inference and `AccuracyEvalTest` on the minified release
    variant. If fragile, ship v1 minify-off with the tradeoff recorded.
  - Replace placeholder launcher icon with real adaptive icon plus 512x512 Play icon.
  - Version toward **v1.0.0** / next `versionCode`.
- **Play-process track (plan-tracked, not all repo code):**
  - Create Personal Play Developer account ($25) and complete ID verification.
  - Recruit at least 12 testers early; Personal accounts created after Nov 2023 need 12 opted-in testers for
    14 continuous days before production access.
  - Complete Data Safety as no data collected/shared, content rating (likely Everyone), app access, ads
    declaration (none), category, short/full descriptions, feature graphic 1024x500, and at least 2 screenshots.
  - Run closed test, request production access, and stage rollout.
- **Exit criteria:** signed release AAB of a min-API-35 build; public repo licensed PolyForm Noncommercial;
  privacy policy live; icon/store assets ready; Data Safety/content rating complete; closed-test gate passed;
  production rollout ready.
- **Standing-lock note:** 0014 lifts only the SDK target lock and README append lock. Frozen seam, KB/model,
  no training, no net-new accuracy screens, and no networking remain intact.

#### Candidate after 0014 / parallel if release waits on tester calendar: fixture-depth + Pilea residuals

- **Goal:** deepen the eval set toward 3+ CC0/PD photos per mapped species, starting with the 31 species on
  1-2 photos; widen sources for Begonia and Schlumbergera; add Pilea photos specifically to re-derive the
  lowest safe `T_pilea` above the documented pothos ceiling.
- **Acceptance bar:** no relaxed licensing, every fixture manifest/integrity/schema gate green, clean vs
  perturbation metrics reported separately, and any lower `T_pilea` keeps **+0 confident-wrong** and
  **0 pothos→direct-Pilea**.

#### Candidate spike: AIY second-opinion / agreement-as-confidence

- **Goal:** offline measurement only. On the existing 66-fixture set, count primary-model abstentions that AIY
  can recover correctly at high confidence under TTA6, and separately count agreement cases.
- **Exit:** table of recoverable-abstention rate, AIY confident-wrong risk, and recommendation: reject cascade,
  use agreement-as-confidence, or scope a guarded routing sprint. Must preserve the 0011/0013 +0-cw discipline.

#### Candidate quick fix: Pilea hero image

- **Goal:** source one CC0/PD Pilea hero photo, wire `PlantImageResolver`, update manifests/licenses, and add
  a reverse card-reachable-species→non-placeholder test. Small enough to ride with 0014 or fixture-depth work
  if ownership is clear.

### Milestone ladder (skeleton)

#### MVP — On-device ID + KB-driven potting-mix recommendation — *(shipped ~PLANTPOTTING-0003)*
Camera → on-device identification → routing decision → KB-driven recipe, network-free, for the bundled species set.

#### V0.1 — Trustworthy confidence calibration — *(met ~PLANTPOTTING-0006)*
Exit criteria met: multi-species real-photo probe across the original AIY/KB overlap, CI accuracy assertions,
and evidence-driven `perSpeciesThresholds` discipline (map empty where evidence did not justify an override).

#### V1 — Broad species coverage and trust — *(active since PLANTPOTTING-0007; substantially advanced)*
Exit criteria: common houseplants identify directly often enough that low-confidence/manual pick is the exception,
and confident-wrong remains bounded by measured gates. Progress: model swap (0007), data spike (0008), KB expansion
(0009/0010), scorecard/abstention (0011), Pilea safe mapping/direct card (0012/0013). Remaining V1 work is mostly
fixture depth, per-species calibration honesty, and selected residuals (lower safe `T_pilea`, AIY spike if measured).

#### Launch — Google Play Store public release — *(parallel track, next via PLANTPOTTING-0014)*
Exit criteria: signed release AAB of a min-API-35 build, PolyForm-Noncommercial-licensed public repo, privacy policy
live, real launcher/store assets, Data Safety/content rating complete, and Personal-account 12-tester / 14-day closed
test passed. Runs parallel to V1 accuracy work; release engineering and Play process only.

#### V2 — Richer care guidance
Beyond one-shot recipe: care profiles, repotting schedule, reminders, and plant-collection persistence/backup choices.
My-Plants-survives-uninstall remains a candidate here unless Launch forces a backup/privacy decision earlier.

#### V3 — Sustainable product / monetisation
Only after the free Launch: monetisation-options spike (40 ideas → top ease/profit shortlists → 2x2 scoring) and any
chosen commercial levers. Keep separate from the 0014 free release.

#### Beyond V3 — Plant-health diagnostics
Identify stress / pest / over-watering signs from the same photo pipeline; sketch only.
