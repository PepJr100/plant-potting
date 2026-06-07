# PLANTPOTTING-0012 — Pothos↔Pilea Boundary Fix + Pilea KB Entry (v0.6.0)

## Intent

Make the pothos→Pilea failure **honest before letting Pilea ship.** The production
`house_plant_species_mobilenetv2` model confidently confuses pothos with Pilea — pothos top-1 =
`Chinese Money Plant (Pilea peperomioides)` @ **0.9661**. That is a single-class confident error with
**no second-place mass**, so the 0011 `high_confidence_abstain_margin = 0.30` cannot catch it. Pilea is
safe *today* only because it is unmapped: pothos's top-1 resolves to nothing and falls through to the
`LowConfidencePicker`. Naively mapping Pilea would convert that silent miss into a *confidently-wrong*
Pilea care card. So this sprint expands the license-clean real-photo fixture set (pothos **and** Pilea),
measures the boundary on `AccuracyEvalTest`, adds a targeted disambiguation gate inside the **existing**
mapping/routing layer, then ships the `pilea-peperomioides` KB entry + model mapping **and** lifts the CI
Pilea-absence guard *in lockstep* — proving on the harness that adding Pilea does **not** increase
confident-wrong. A supporting TTA sweep (×8/×10/×20, ≤ ~2 s worst-case) rides along but only adopts a new
level if the curve earns it. All behind the frozen `PlantIdentifier` seam; no model swap, no training, no
self-shot imagery.

## Goals

- [x] Expand the CC0/PD/CC-BY `identify-fixtures` set with real **pothos** (`epipremnum-aureum`) and real **Pilea** (`pilea-peperomioides`) photos, plus as many of the 8 untested mapped species as have license-clean sources — bounded by the license-clean ceiling, not an arbitrary count. *(+5 pothos, +6 Pilea, +8 untested = 19 CC0 fixtures; all 8 untested covered.)*
- [x] Produce an honest pre-change pothos/Pilea boundary scorecard on the local `pixel6Api34` `AccuracyEvalTest` path (top-k labels/scores, route, confident-wrong, latency) **and** a from-CSV "naive-Pilea-mapped" simulation across all three acceptance surfaces.
- [x] Implement a targeted pothos↔Pilea disambiguation gate inside `ModelScoreMapper` / manifest config that prevents a confidently-wrong Pilea card from surfacing for a pothos input — **without** touching the frozen `PlantIdentifier` / `IdentificationResult` / `IdSource` seam and **without** suppressing correct, direct pothos results unless evidence proves it necessary.
- [x] Add the `pilea-peperomioides` KB entry + `"Chinese Money Plant (Pilea peperomioides)"` model mapping **in the same change set as the gate**, replace `HousePlantClassMapValidationTest.pileaIsNotMapped` with a positive mapping guard (count → 39), and add a CI test that fails if Pilea is mapped while the gate is absent — keeping every other map-validation guard intact.
- [x] Prove on the harness that adding Pilea does not raise confident-wrong vs. the pre-Pilea baseline (clean photos, all perturbations, and the pothos/Pilea subset) **and** does not raise the low-confidence route-rate on correct pothos beyond an agreed bound.
- [x] Sweep TTA ×8/×10/×20 on the expanded fixtures against the gated ×6 production baseline; adopt a new `tta` only if confident-wrong/boundary improves under the ~2 s worst-case latency budget.
- [x] Ship v0.6.0 / versionCode 6 with all gates green and a debug APK **verified** present in `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\`.

## Non-goals / scope boundaries

- [x] Do **not** swap, retrain, fine-tune, re-quantize, or otherwise alter the bundled `house_plant_species_mobilenetv2` model or the AIY V1/3 regression anchor.
- [x] Do **not** change `PlantIdentifier`, `IdentificationResult`, or `IdSource` (frozen 0003 §4.4 seam). All new logic stays in manifest parsing, `ModelScoreMapper`, internal candidate construction, and tests.
- [x] Do **not** add KB content beyond the single Pilea species entry, its archetype linkage, its reference image (only if the resolver requires one), and its model mapping.
- [x] Do **not** use self-shot / first-party / principal-supplied / generated imagery anywhere, and do **not** accept CC-BY-SA, CC-BY-NC, ND, all-rights-reserved, or unknown-license sources for fixtures **or** the optional Pilea reference image.
- [x] Do **not** loosen `verifyNoNetworking`, `check-stub-isolation.sh`, fixture license/integrity/CSV-schema guards, or the 0011 abstention/margin policy (the new gate **composes with** it, never replaces it).
- [x] Do **not** bump AGP / Kotlin / Compose / Hilt / TFLite or any platform dependency.
- [x] Do **not** touch the My-Plants-survives-uninstall work (deferred), and do not re-litigate any 0007–0011 mapping that already shipped.

## Repo anchors (for the executor — confirm each before editing)

- [x] Fixtures live flat in `app/src/androidTest/assets/identify-fixtures/` as `<kb-species-id>__NN.jpg`, with `LICENSE.txt` + `fixture-manifest.tsv` in the same dir; guarded by `FixtureLicenseManifestTest`, `FixtureIntegrityTest`, `AccuracyEvalCsvSchemaTest`, and exercised by `AccuracyEvalTest` (+ the perturbation generator).
- [x] Routing lives in `app/src/main/java/com/darkfactory/plantpotting/identify/model/ModelScoreMapper.kt`; thresholds (`high_confidence_abstain_margin`, the empty `per_species_thresholds`, `tta`) live in `app/src/main/assets/ml/house_plant_species_mobilenetv2/model_manifest.json`.
- [x] The class map is `app/src/main/assets/ml/house_plant_species_mobilenetv2/plant_class_map.json` (currently 38 mapped rows; `"Pothos (Ivy arum)" → epipremnum-aureum`; a deferral `_comment` names Pilea explicitly).
- [x] The CI absence guard is `HousePlantClassMapValidationTest.pileaIsNotMapped`; the count guard is `mapsExactlyThirtyEightClasses`; the lineage guards include `everyMappingKeyIsVerbatimLabelLine`, `everyKbSpeciesIdResolvesInBundledKb`, `eachNewLabelResolvesToExpectedKbId`, and `existingTenRowsStillPointAtSameKbIds` (plus 0009/0010 lineage). KB species are in `app/src/main/assets/kb/species.json`.
- [x] Version source is `app/build.gradle.kts` (`versionCode = 5`, `versionName = "0.5.0"` → bump to 6 / "0.6.0"). Mapping rationale doc is `docs/kb/ml-mapping-notes.md`. Sprint evidence goes under `docs/sprints/evidence/PLANTPOTTING-0012/`.

---

## Phase 1 — Source & expand license-clean fixtures (pothos + Pilea + untested mapped species)

Sequencing rule: **this phase first** — the Pilea supply is thin (~76 license-clean candidates per the 0008 spike) and license-clean scarcity is the sprint's primary data risk.

- [x] Create `docs/sprints/evidence/PLANTPOTTING-0012/fixture-sourcing-log.md` capturing every search term, source URL, author, license + license URL, accept/reject decision, **rejected near-misses with reason codes**, and the running per-species count.
- [x] Inventory the current `identify-fixtures/` set from `fixture-manifest.tsv`: record baseline counts for `epipremnum-aureum`, confirm `pilea-peperomioides` is absent, and enumerate the **8 mapped species with zero clean fixture coverage** (cross the 38 mapped rows against the manifest's covered species).
- [x] Source real **pothos** (`epipremnum-aureum`) photos from Wikimedia Commons / GBIF+iNaturalist license-filtered / Smithsonian Gardens, covering juvenile vs. mature leaf shape, green vs. variegated (golden/marble/neon) forms, trailing/vining habit, and top-down + oblique indoor pot contexts. Pothos CC supply is abundant (~1470) — aim for **breadth, target ≥ 6 independent photos**.
- [x] Source real **Pilea peperomioides** photos (Chinese money / missionary / pancake / UFO plant) prioritising the distinctive round **peltate** leaf habit, multiple angles/backgrounds, and **independent authors**. Treat ~76 candidates as the ceiling; accept everything license-clean and species-correct (**target ≥ 3 independent base photos**, more if available).
- [x] Source clean photos for as many of the 8 untested mapped species as have license-clean coverage (secondary priority; log any that remain unsourced rather than padding).
- [x] Accept only CC0 / public-domain / PD-mark / CC-BY **real photographs** that permit commercial app-test use and are attributable. Reject + log CC-BY-SA, CC-BY-NC, ND, all-rights-reserved, ambiguous-license, cultivar-mismatched, **botanical-plate/drawing** (mirror the 0011 "no plates" lesson), watermarked, duplicate, and non-houseplant-context candidates.
- [x] Add accepted JPEGs as `epipremnum-aureum__NN.jpg`, `pilea-peperomioides__NN.jpg`, and untested-species ids following the existing flat `__NN` convention.
- [x] Append a `fixture-manifest.tsv` row per new file using the schema already in the file (filename, expected KB species id, model label if applicable, source URL, author, license, license URL, acquisition date, notes).
- [x] Add CC-BY attribution entries under `docs/licenses/` for every CC-BY fixture; keep CC0/PD rows explicit even where attribution is not legally required.
- [x] Run `FixtureLicenseManifestTest` after the **first** pothos/Pilea batch and clear all schema / license-vocabulary failures before adding more.
- [x] Run `FixtureIntegrityTest` after the batch (separate step — corrupt-image and unresolved-id failures have different fixes) and clear them before proceeding.
- [x] **Record the Pilea verdict in the sourcing log (drives Phases 4–5):** **≥ 3** independent license-clean Pilea fixtures → earns the right to *evaluate* a direct Pilea card (still gated + no-regression governed); **1–2** → Pilea is *strict-picker-only* this sprint; **0** → Pilea-blocked path (keep Pilea unmapped, keep the absence guard, ship fixture+TTA evidence only, write `docs/sprints/evidence/PLANTPOTTING-0012/pilea-blocker.md`).
- [x] Close the phase with a **reason-coded outcome table** in the sourcing log: each of the 8 untested species marked sourced (with count) or uncovered (with reason). "Sourced as many as possible" must be auditable.

## Phase 2 — Measure the boundary honestly (baseline, before any routing change)

- [x] Run the **unchanged** production app (Pilea still unmapped, gate absent) through `AccuracyEvalTest` on the local `pixel6Api34` emulator over the expanded fixture set.
- [x] Save raw output as `docs/sprints/evidence/PLANTPOTTING-0012/baseline-pre-pilea-eval.csv` + `baseline-pre-pilea-summary.md`.
- [x] For every pothos and Pilea fixture record: expected species, raw top-1 label/score, Pilea rank+score, pothos rank+score, top1−top2 margin, mapped result, route (`direct` vs `LowConfidencePicker`), correctness, confident-wrong flag, latency.
- [x] Aggregate clean-photo metrics **separately** from perturbation metrics, so derived variants cannot hide base-photo behavior and a single scarce Pilea base photo cannot dominate via its perturbations.
- [x] Quantify the live pothos→Pilea failure rate and how often pothos routes to the picker *only* because its top-1 (Pilea) maps to nothing today.
- [x] Compute, directly from the baseline CSV, the **simulated naive-Pilea-mapped** outcome across **all three acceptance surfaces** — all clean photos, all perturbations, and the pothos/Pilea subset — i.e. "if we mapped Pilea with no gate, how many results on each surface would turn confidently wrong?" This fixes the concrete "what would break" numbers Phase 6 compares against.
- [x] Confirm `HousePlantClassMapValidationTest.pileaIsNotMapped` still passes before Phase 3.

## Phase 3 — Design the disambiguation gate (decision + tradeoffs)

The brief's central ask — reason about each candidate **in-plan**, not only in a decision doc.

- [x] **Candidate A — pairwise top-k delta rule:** when top-1 is Pilea and the pothos label is in top-k within a measured delta, route to the picker with both surfaced. *Tradeoff:* precise and minimal, but tuning the delta on a tiny Pilea set risks overfit, and it does nothing for the 0.9661 case where pothos may not even appear in top-k.
- [x] **Candidate B — boundary-pair allow-list (fail-safe default):** when a result's **top-1 resolves to Pilea**, route the pothos↔Pilea pair to the picker with **both** candidates surfaced. *Tradeoff:* robust to the no-second-place-mass case (catches the 0.9661 error head-on) and trivially testable; cost is that a *true* Pilea photo loses its direct card and appears as a candidate instead. **Crucially, this default does NOT fire on pothos-dominant (top-1 = pothos) results**, so correct, confident, direct pothos cards are preserved (pothos photos that mispredict already route to the picker today, so there is no pothos regression there).
- [x] **Candidate C — Pilea-specific elevated `per_species_thresholds`:** require Pilea to clear a bar far above the global 0.55 before it can map to a card, reusing the existing per-species mechanism honestly. *Tradeoff:* zero new code paths and keeps pothos direct, but a 0.9661 pothos→Pilea hit blows past almost any plausible bar — so **C alone cannot fix the core error**; it can only ever *narrow* when a direct Pilea card is permitted.
- [x] **Candidate D — TTA/preprocessing-only:** measure whether ×6/×8/×10/×20 reorders the pothos/Pilea ranking enough to avoid a targeted gate. *Tradeoff:* would need no boundary logic if it worked; almost certainly insufficient for a 0.9661 single-class error, but must be **measured, not assumed** (feeds Phase 7).
- [x] Explicitly **reject margin-only abstention** as the sole fix and record *why*: a 0.9661 top-1 has no useful second-place mass for `high_confidence_abstain_margin` to bite on.
- [x] **Select the default design: Candidate B (top-1-resolves-to-Pilea → pair to picker) as the fail-safe**, justified **independently of TTA** by the no-second-place-mass logic. A *narrower* rule (e.g. B restricted by a measured top-k condition, or B + a measured elevated Pilea threshold C to permit a *direct* Pilea card) may replace it **only if** evidence shows the narrower rule still blocks every pothos→Pilea confident card **and** preserves the no-confident-wrong bar. Direct Pilea cards require author-separated / held-out evaluation before being permitted.
- [x] Write `docs/sprints/evidence/PLANTPOTTING-0012/boundary-gating-decision.md`: chosen design (provisional pending Phase 6 evidence), rejected alternatives with the above tradeoffs, user-facing impact (incl. the correct-Pilea-loses-direct-card cost), implementation surface, false-abstain cost, and the evidence threshold required before any *direct* Pilea card is permitted.
- [x] Set the binding design bar: **adding Pilea must not increase confident-wrong** on clean fixtures, on all perturbations, **or** on the pothos/Pilea subset vs. the Phase 2 baseline; **and** must not raise the low-confidence route-rate on **correct pothos** beyond an agreed, reported bound.

## Phase 4 — Implement the gate (tests first), staged for an atomic lockstep with Phase 5

- [x] Add a `ModelScoreMapper` unit test reproducing the live failure: a synthetic score row with raw top-1 `"Chinese Money Plant (Pilea peperomioides)"` @ 0.9661 must route to **low-confidence** (never a direct `pilea-peperomioides` card) once the mapping exists and the gate is on.
- [x] Add a `ModelScoreMapper` unit test for true-Pilea behavior: a Pilea-dominant row routes to a Pilea card **only** if the selected evidence bar is met, otherwise to the picker with Pilea + pothos as visible candidates.
- [x] Add a `ModelScoreMapper` unit test that ordinary non-boundary high-confidence species (e.g. monstera, snake plant) **and a pothos-dominant (top-1 = pothos) result** still return direct cards under the unchanged global/margin/abstain thresholds — i.e. the gate is scoped to the Pilea-top-1 case and the 0011 abstention behavior is byte-for-byte unchanged elsewhere.
- [x] Add a candidate-bundle test asserting the boundary low-confidence route surfaces **both** `pilea-peperomioides` and `epipremnum-aureum`, that the true raw-top candidate is not buried, and that candidates are not duplicated when one pair member is absent from top-k.
- [x] Add the boundary-pair rule as **data** in the internal model manifest **or** the narrowest existing mapper configuration (decide by reading the manifest parser; do **not** prematurely commit to a `boundary_pairs` manifest block before inspecting it) — parsed by existing parsing, **no** seam change.
- [x] Implement the gate in `ModelScoreMapper` at the existing score-routing point (it already holds labels, mapped KB ids, ranked top-k scores, and thresholds), composing it **with** — not replacing — the `high_confidence_abstain_margin` veto and the plain/margin branches.
- [x] If the Phase 1 verdict is *strict-picker-only* (1–2 Pilea fixtures), implement Pilea as "mapped but pair-forced to the picker": Pilea may appear as a candidate but can never surface as a direct high-confidence card this sprint.
- [x] Run `testDebugUnitTest` for `ModelScoreMapper*`, manifest parsing, and low-confidence candidate display green (against synthetic score rows) before the atomic map edit in Phase 5.

## Phase 5 — Add the Pilea KB entry + mapping AND lift the CI absence guard in lockstep

**Atomicity is load-bearing:** land the gate (Phase 4), the Pilea KB entry, the mapping, the positive map guard, the removal of `pileaIsNotMapped`, **and** the bind-mapping-to-gate guard as **one tightly-reviewed change set**. No branch state — not even a transient commit — may map Pilea while the gate or its guard is absent; that transient state is exactly the confidently-wrong window the sprint exists to prevent.

- [x] Add `pilea-peperomioides` to `app/src/main/assets/kb/species.json` with Chinese-money-plant naming and concise care fields, reusing an existing compatible substrate archetype (do **not** invent a new archetype unless strictly required).
- [x] Add a license-clean Pilea reference image **only if** the KB image resolver requires one for the new entry — sourced under the **same CC0/PD/CC-BY discipline** as fixtures, attributed under `docs/licenses/`; otherwise skip.
- [x] Add `"Chinese Money Plant (Pilea peperomioides)" → pilea-peperomioides` to `plant_class_map.json`.
- [x] Rewrite the `plant_class_map.json` `_comment`: remove the "deliberately LEFT UNMAPPED" deferral language and replace it with "mapped under PLANTPOTTING-0012 **only** with the pothos↔Pilea boundary gate" — being careful not to alter any field the count/parser assertions depend on.
- [x] Replace `HousePlantClassMapValidationTest.pileaIsNotMapped` with a positive `pileaMapsToPileaPeperomioides` assertion.
- [x] Update `HousePlantClassMapValidationTest.mapsExactlyThirtyEightClasses` to the new exact count **(39)** and keep `everyMappingKeyIsVerbatimLabelLine`, `everyKbSpeciesIdResolvesInBundledKb`, `eachNewLabelResolvesToExpectedKbId`, `existingTenRowsStillPointAtSameKbIds`, and the 0009/0010 lineage guards intact.
- [x] Add a guard test that **fails if** `"Chinese Money Plant (Pilea peperomioides)"` is mapped while the pothos↔Pilea boundary rule is absent from the production manifest/config (CI-binds the mapping to the gate; this test lands **with** the map edit, never after).
- [x] Update any KB-count / image-manifest / species-list tests for the new Pilea species.
- [x] Add a **stub** Pilea decision note to `docs/kb/ml-mapping-notes.md` (the deferral section is replaced here; the *measured* numbers are filled in during Phase 8 after Phases 6–7 land).

## Phase 6 — Post-mapping measurement & the no-regression acceptance gate

- [x] Run `AccuracyEvalTest` on `pixel6Api34` **after** Pilea mapping + gate; save `post-pilea-gated-eval.csv` + `post-pilea-gated-summary.md` under the evidence dir.
- [x] Build the comparison table: baseline / naive-simulated-Pilea / gated-Pilea across pothos fixtures, Pilea fixtures, all clean photos, all perturbations, top-1 accuracy, direct-card accuracy, **low-confidence route rate**, and confident-wrong rate.
- [x] Report a **separate correct-pothos direct-card vs picker delta** — how much *more* abstention the gate introduces for correct pothos — and check it against the Phase 3 agreed bound; flag for discussion if the picker rate rises sharply.
- [x] Confirm **every** pothos fixture that previously raw-predicted Pilea now routes to the picker or a correct pothos result — **never** a direct Pilea care card.
- [x] Confirm true-Pilea fixtures either surface a correct Pilea card under the selected bar **or** route to the picker with Pilea visible as a candidate — verifying candidate **visibility and ordering from the actual candidate bundle**, not only the CSV route column.
- [x] Confirm the expanded non-boundary fixtures show **no** new confident-wrong regression introduced by the gate.
- [x] If confident-wrong rose anywhere vs. baseline, or the correct-pothos picker rate exceeded the bound, tighten the Pilea direct-card rule or fall back to pair-forced picker before accepting the sprint (do **not** relax the baseline to pass).

## Phase 7 — Supporting TTA sweep (×8/×10/×20, ≤ ~2 s worst-case)

- [x] Add experiment-only support to run TTA ×8/×10/×20 in `AccuracyEvalTest` **without** changing the production `model_manifest.json` `tta` until a decision is made.
- [x] Run ×6/×8/×10/×20 on the expanded fixtures and collect per level: clean-photo metrics, perturbation metrics, pothos/Pilea metrics, direct-card accuracy, confident-wrong rate, low-confidence rate, median latency, p95 (if available), and worst observed latency on `pixel6Api34`.
- [x] Reject any TTA level whose **worst** observed latency exceeds ~2 s.
- [x] Compare each surviving level against the **gated ×6** production candidate (not the pre-0011 single-crop path).
- [x] Adopt a new `tta` in `model_manifest.json` **only if** it yields a meaningful confident-wrong or boundary improvement within the latency budget; otherwise leave `tta = 6`.
- [x] Save `tta-sweep.csv` + `tta-sweep-decision.md`; update `model_manifest.json`'s preprocessing comment **only after** the final decision.

## Phase 8 — Version bump, APK, gates, docs

- [x] Do the **final pass** on `docs/kb/ml-mapping-notes.md` so the PLANTPOTTING-0012 boundary section reflects the *measured* Phase 6 numbers and the Phase 7 TTA decision, not just the design intent.
- [x] Bump `app/build.gradle.kts` to `versionCode = 6`, `versionName = "0.6.0"`.
- [x] Run `.\gradlew.bat verifyNoNetworking`.
- [x] Run `pwsh scripts/check-stub-isolation.sh`.
- [x] Run `.\gradlew.bat ktlintCheck` **before pushing** (CI-only gate; on Windows stage only real changes — autocrlf churns ~76 files; run `ktlintFormat` if needed).
- [x] Run `.\gradlew.bat lintDebug` (abortOnError) and fix findings.
- [x] Run `.\gradlew.bat testDebugUnitTest` (incl. `ModelScoreMapper*`, `HousePlantClassMapValidationTest`, `FixtureLicenseManifestTest`, `FixtureIntegrityTest`, `AccuracyEvalCsvSchemaTest`, and the new bind-mapping-to-gate guard).
- [x] Run the established local `pixel6Api34` GMD/androidTest command for `AccuracyEvalTest` + on-device regression tests.
- [x] Confirm `OnDeviceModelRealInterpreterTest` still pins the bundled AIY V1/3 regression anchor.
- [x] Build a debug APK, copy it to `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\`, and **verify via PowerShell that the file actually landed** (sandbox-overlay writes outside the project tree can silently not reach disk).
- [x] Update `docs/ROADMAP.md` with the 0012 result: fixture counts, gating decision, TTA decision, Pilea status (mapped / strict-picker-only / blocked), and remaining limitations.
- [ ] (If shipping a public release) push the `v0.6.0` tag — `release.yml` fires only on a pushed `v*` tag; a `versionName` bump alone never publishes a GitHub Release.

---

## Sequencing / phase dependencies

- [x] **P1 → P2:** source fixtures before measuring; the boundary numbers are meaningless without real pothos+Pilea photos.
- [x] **P2 → P3:** design the gate against *measured* top-k behavior. The gate selection is **provisional** until Phase 6 evidence; it is chosen independent of TTA (the 0.9661-no-second-mass logic supports this).
- [x] **P3 → P4:** choose the user-facing tradeoff explicitly before changing any routing.
- [x] **P4 + P5 atomic:** the gate, its config, the Pilea KB entry + mapping, the positive map guard, the removal of `pileaIsNotMapped`, and the bind-mapping-to-gate guard land **together** (one commit / one review). No tree may map Pilea without the gate, even transiently.
- [x] **P5 → P6:** post-mapping confident-wrong is the central acceptance gate — run it immediately.
- [x] **P7 after P6:** the TTA sweep compares against the *gated production candidate*, which only exists once Pilea is mapped, the gate is in, and post-mapping eval has run.
- [x] **P8 last:** the final `ml-mapping-notes.md` pass, version/APK, and ROADMAP only after all routing/KB/fixture/eval/TTA gates are green.

## Risks & mitigations

- [x] **The 0.9661 error has no second-place mass.** → Do not rely on margin abstention; use the explicit Pilea-top-1 boundary gate (Candidate B), justified independently of TTA.
- [x] **Over-abstention on correct pothos (the biggest UX/accuracy risk).** → Scope the default gate to *top-1-resolves-to-Pilea* so correct pothos-dominant cards stay direct; report and bound the correct-pothos picker-rate delta in Phase 6; revisit the rule if it rises sharply.
- [x] **Pilea supply too thin for honest direct-card calibration (~76 ceiling).** → ≥ 3 independent fixtures earns only the *right to evaluate* a direct card (author-separated / held-out eval required); 1–2 → strict-picker-only; 0 → Pilea-blocked, absence guard stays, `pilea-blocker.md` written.
- [x] **Per-species threshold (Candidate C) overfits a tiny Pilea set + admits adversarial pothos false positives.** → Prefer the simple Pilea-top-1 pair rule; keep clean vs. perturbation metrics separate; require held-out evaluation before any *direct* Pilea route; log rejected threshold candidates.
- [x] **Mapping Pilea breaks map-validation beyond the intended guard, or a non-atomic landing opens a confidently-wrong window.** → Replace only `pileaIsNotMapped`, pin the count to 39, keep all lineage guards intact, and make the bind-mapping-to-gate test the thing that turns a Pilea-without-gate tree red — landed in the same atomic set.
- [x] **Incomplete CC-BY attribution.** → Manifest tests stay strict; add license rows *before* images land; reject any source whose author/license URL can't be verified.
- [x] **Perturbations overweight one scarce Pilea base photo.** → Report base-photo metrics separately; never accept direct-card Pilea behavior from perturbations alone.
- [x] **Higher TTA helps the small fixture set but blows the latency budget.** → Enforce the ~2 s worst-case cap; compare against gated ×6; leave `tta = 6` if gains are marginal/noisy.
- [x] **Windows sandbox filesystem overlay** may silently swallow writes outside the project tree (the Dropbox APK copy). → Verify the APK landed via PowerShell from the user's terminal before claiming delivery.
- [x] **Gate logic leaks into the frozen seam.** → Keep all new logic in manifest parsing, `ModelScoreMapper`, internal candidate construction, and tests only.

## Acceptance criteria

- [x] No production change alters `PlantIdentifier`, `IdentificationResult`, or `IdSource`.
- [x] Every added fixture (and the optional Pilea reference image) is a real photo with CC0/PD/PD-mark/CC-BY licensing, documented in `fixture-manifest.tsv` / `docs/licenses/`.
- [x] The evidence dir contains: `fixture-sourcing-log.md` (incl. the 8-untested reason-coded table), `baseline-pre-pilea-eval.csv` + summary, `boundary-gating-decision.md`, `post-pilea-gated-eval.csv` + summary, `tta-sweep.csv` + `tta-sweep-decision.md`, and any Pilea fallback/blocker note.
- [x] Pilea is mapped **only** if the production pothos↔Pilea gate is present and tested (CI-enforced by the bind-mapping-to-gate test); the gate and mapping landed atomically.
- [x] `HousePlantClassMapValidationTest` no longer asserts Pilea absence; it asserts Pilea maps to `pilea-peperomioides`, the count is **39**, and all other map-validation / lineage guards remain active.
- [x] Adding Pilea does **not** increase confident-wrong vs. the pre-Pilea baseline on clean fixtures, all perturbations, **or** the pothos/Pilea subset.
- [x] The correct-pothos low-confidence route-rate delta introduced by the gate is reported and within the Phase 3 agreed bound.
- [x] No pothos fixture can surface a direct Pilea care card in the final gated evaluation; correct, confident, direct pothos cards are preserved.
- [x] True-Pilea fixtures surface a correct Pilea result under the selected bar, **or** route to the picker with Pilea visible (and correctly ordered) as a candidate.
- [x] Any adopted TTA level has documented improvement over gated ×6 and worst observed latency under ~2 s; otherwise `tta` stays 6.
- [x] If Pilea fixtures cannot be sourced at all, Pilea stays unmapped, the absence guard stays, and `pilea-blocker.md` records the block instead of shipping an unsafe mapping.
- [x] `verifyNoNetworking`, `check-stub-isolation.sh`, `ktlintCheck`, `lintDebug`, JVM unit tests, fixture/license/integrity/CSV-schema tests, and the local `pixel6Api34` on-device eval are green (or have documented environment-only failures).
- [x] The shipped build is v0.6.0 / versionCode 6 and the debug APK is **verified present** in `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\`.
