# PLANTPOTTING-0012 — Pothos↔Pilea Boundary Fix + Pilea KB Entry (v0.6.0)

## One-line intent

Make the pothos→Pilea failure *honest* before letting Pilea ship: expand license-clean
real-photo fixtures (pothos **and** Pilea), measure the boundary on `AccuracyEvalTest`,
add a targeted disambiguation gate inside the **existing** mapping/routing layer, then add
the `pilea-peperomioides` KB entry + model mapping **and** lift the CI Pilea-absence guard
in lockstep — proving adding Pilea does **not** increase confident-wrong. A supporting TTA
sweep (×8/×10/×20 ≤ ~2 s worst-case) rides along but only adopts a new level if the curve
earns it.

The hard truth driving the whole design: pothos top-1 = `Chinese Money Plant (Pilea
peperomioides)` @ **0.9661**. That is a single-class confident error with **no second-place
mass** — the 0011 `high_confidence_abstain_margin = 0.30` cannot catch it (margin is huge).
Pilea is safe *today* only because it is unmapped, so pothos's top-1 resolves to nothing and
falls through to the `LowConfidencePicker`. Naively mapping Pilea converts that silent miss
into a confidently-wrong Pilea care card. So the gate and the mapping **must land together**.

## Goals

- [ ] Expand the CC0/PD/CC-BY `identify-fixtures` set with real **pothos** (`epipremnum-aureum`) and real **Pilea** (`pilea-peperomioides`) photos, plus as many of the 8 untested mapped species as have license-clean sources — bounded by the license-clean ceiling, not by an arbitrary count.
- [ ] Produce an honest pre-change pothos/Pilea boundary scorecard on the local `pixel6Api34` `AccuracyEvalTest` path (top-k labels/scores, route, confident-wrong, latency).
- [ ] Implement a targeted pothos↔Pilea disambiguation gate inside `ModelScoreMapper` / `model_manifest.json` config that prevents a confidently-wrong Pilea card from surfacing for a pothos input — without touching the frozen `PlantIdentifier` / `IdentificationResult` / `IdSource` seam.
- [ ] Add the `pilea-peperomioides` KB entry + `"Chinese Money Plant (Pilea peperomioides)"` model mapping **in the same change set as the gate**, and replace `HousePlantClassMapValidationTest.pileaIsNotMapped` with a positive mapping guard while keeping every other map-validation guard intact.
- [ ] Prove on the harness that adding Pilea does not raise confident-wrong vs. the pre-Pilea baseline (clean photos, all variants, and the pothos/Pilea subset).
- [ ] Sweep TTA ×8/×10/×20 on the expanded fixtures against the gated ×6 production baseline; adopt a new `tta` only if confident-wrong/boundary improves under the ~2 s worst-case latency budget.
- [ ] Ship v0.6.0 / versionCode 6 with all gates green and a debug APK in `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\`.

## Non-goals / scope boundaries

- [ ] Do **not** swap, retrain, fine-tune, re-quantize, or otherwise alter the bundled `house_plant_species_mobilenetv2` model or the AIY V1/3 regression anchor.
- [ ] Do **not** change `PlantIdentifier`, `IdentificationResult`, or `IdSource` (frozen 0003 §4.4 seam).
- [ ] Do **not** add KB content beyond the single Pilea species entry, its archetype linkage, its reference image (only if the resolver requires one), and its model mapping.
- [ ] Do **not** use self-shot / first-party / principal-supplied / generated imagery, and do **not** accept CC-BY-SA, CC-BY-NC, ND, all-rights-reserved, or unknown-license sources anywhere.
- [ ] Do **not** loosen `verifyNoNetworking`, `check-stub-isolation.sh`, fixture license/integrity/CSV-schema guards, or the abstention/margin policy from 0011 (the new gate **composes with** it, never replaces it).
- [ ] Do **not** bump AGP / Kotlin / Compose / Hilt / TFLite or any platform dependency.
- [ ] Do **not** touch the My-Plants-survives-uninstall work (deferred), and do not re-litigate any 0007–0011 mapping that already shipped.

## Repo anchors (verified, for the executor)

- [ ] Fixtures live flat in `app/src/androidTest/assets/identify-fixtures/` as `<kb-species-id>__NN.jpg`, with `LICENSE.txt` + `fixture-manifest.tsv` in the same dir; guarded by `FixtureLicenseManifestTest`, `FixtureIntegrityTest`, `AccuracyEvalCsvSchemaTest`, and exercised by `AccuracyEvalTest` + `FixturePerturbationsTest`.
- [ ] Routing lives in `app/src/main/java/com/darkfactory/plantpotting/identify/model/ModelScoreMapper.kt`; thresholds (incl. `high_confidence_abstain_margin` and the empty `per_species_thresholds`) live in `app/src/main/assets/ml/house_plant_species_mobilenetv2/model_manifest.json`.
- [ ] The class map is `app/src/main/assets/ml/house_plant_species_mobilenetv2/plant_class_map.json` (currently 38 mapped rows; `"Pothos (Ivy arum)" → epipremnum-aureum`; the deferral `_comment` names Pilea explicitly).
- [ ] The CI absence guard is `HousePlantClassMapValidationTest.pileaIsNotMapped`; the count guard is `mapsExactlyThirtyEightClasses`. KB species are in `app/src/main/assets/kb/species.json`.
- [ ] Version source is `app/build.gradle.kts` (`versionCode = 5`, `versionName = "0.5.0"` → bump to 6 / "0.6.0").
- [ ] Mapping rationale doc is `docs/kb/ml-mapping-notes.md`. Sprint evidence goes under `docs/sprints/evidence/PLANTPOTTING-0012/`.

---

## Phase 1 — Source & expand license-clean fixtures (pothos + Pilea + untested mapped species)

Sequencing rule: **this phase first** — the Pilea supply is thin (~76 license-clean candidates per the 0008 spike) and license-clean scarcity is the sprint's primary data risk.

- [ ] Create `docs/sprints/evidence/PLANTPOTTING-0012/fixture-sourcing-log.md` capturing every search term, source URL, author, license + license URL, accept/reject decision, and the running per-species count.
- [ ] Inventory the current `identify-fixtures/` set from `fixture-manifest.tsv`: record baseline counts for `epipremnum-aureum`, confirm `pilea-peperomioides` is absent, and enumerate the 8 mapped species that still have **zero** clean fixture coverage (cross the 38 mapped rows against the manifest's covered species).
- [ ] Source real **pothos** (`epipremnum-aureum`) photos from Wikimedia Commons / GBIF+iNaturalist license-filtered / Smithsonian Gardens, covering juvenile vs. mature leaf shape, green vs. variegated (golden/marble/neon) forms, trailing/vining habit, and top-down + oblique indoor pot contexts — pothos CC supply is abundant (~1470), so aim for **breadth** here (target ≥ 6 independent photos).
- [ ] Source real **Pilea peperomioides** photos (Chinese money plant / missionary / pancake / UFO plant) prioritising the distinctive round **peltate** leaf habit, multiple angles/backgrounds, and **independent authors** — treat ~76 candidates as the ceiling and accept everything license-clean and species-correct (target ≥ 3 independent base photos; more if available).
- [ ] Source clean photos for as many of the 8 untested mapped species as have license-clean coverage (secondary priority; log any that remain unsourced rather than padding).
- [ ] Accept only CC0 / public-domain / PD-mark / CC-BY real photographs that permit commercial app-test use and are attributable; reject + log CC-BY-SA, CC-BY-NC, ND, all-rights-reserved, ambiguous-license, cultivar-mismatched, botanical-plate/drawing, watermarked, duplicate, and non-houseplant-context candidates (mirror the "no botanical plates" lesson from the 0011 hero-photo rebuild).
- [ ] Add accepted JPEGs as `epipremnum-aureum__NN.jpg` and `pilea-peperomioides__NN.jpg` (and untested-species ids) following the existing flat `__NN` convention.
- [ ] Append a `fixture-manifest.tsv` row per new file with the schema the manifest already uses (filename, expected KB species id, model label if applicable, source URL, author, license, license URL, acquisition date, notes).
- [ ] Add CC-BY attribution entries under `docs/licenses/` for every CC-BY fixture; keep CC0/PD rows explicit even where attribution is not legally required.
- [ ] Run `FixtureLicenseManifestTest` and `FixtureIntegrityTest` after the **first** pothos/Pilea batch and fix any schema / license-vocabulary / corrupt-image / unresolved-id failure before adding more.
- [ ] **Fallback gate — record the Pilea verdict in the sourcing log:** if **≥ 3** independent license-clean Pilea fixtures land → proceed to the full mapping path; if **1–2** land → proceed but flag Pilea for the *strict-picker-only* fallback (Phase 4); if **0** land → trigger the Pilea-blocked path (keep Pilea unmapped, keep the absence guard, ship fixture+TTA evidence only, write `docs/sprints/evidence/PLANTPOTTING-0012/pilea-blocker.md`).

## Phase 2 — Measure the boundary honestly (baseline, before any routing change)

- [ ] Run the **unchanged** production app (Pilea still unmapped, gate absent) through `AccuracyEvalTest` on the local `pixel6Api34` emulator over the expanded fixture set.
- [ ] Save the raw output as `docs/sprints/evidence/PLANTPOTTING-0012/baseline-pre-pilea-eval.csv` + `baseline-pre-pilea-summary.md`.
- [ ] For every pothos and Pilea fixture, record: expected species, raw top-1 label/score, Pilea rank+score, pothos rank+score, top1−top2 margin, mapped result, route (`direct` vs `LowConfidencePicker`), correctness, confident-wrong flag, latency.
- [ ] Aggregate clean-photo metrics **separately** from perturbation metrics so derived variants cannot hide base-photo behavior, and so a single scarce Pilea base photo cannot dominate via its perturbations.
- [ ] Quantify the live pothos→Pilea failure rate and how often pothos routes to the picker *only* because its top-1 (Pilea) maps to nothing today.
- [ ] Compute, directly from the baseline CSV, the **simulated naive-Pilea-mapped** outcome — i.e. "if we mapped Pilea with no gate, how many pothos fixtures would surface a confident Pilea card?" — to fix a concrete "what would break" number before designing the gate.
- [ ] Confirm `HousePlantClassMapValidationTest.pileaIsNotMapped` still passes before Phase 3.

## Phase 3 — Design the disambiguation gate (decision + tradeoffs)

- [ ] Evaluate **Candidate A — pairwise top-k delta rule:** when top-1 is Pilea and the pothos label is in top-k within a measured delta, route to `LowConfidencePicker` with both candidates surfaced. *Tradeoff:* precise and minimal, but tuning the delta on a tiny Pilea set risks overfit; does nothing for the 0.9661 case where pothos may not even be in the top-k.
- [ ] Evaluate **Candidate B — boundary-pair allow-list:** when **either** Pilea or pothos dominates above threshold, force the pair into the picker so the user disambiguates. *Tradeoff:* robust to the no-second-place-mass case (catches the 0.9661 error head-on) and trivially testable; cost is it suppresses *correct* direct Pilea **and** direct pothos cards into the picker (UX regression on a previously-direct pothos path).
- [ ] Evaluate **Candidate C — Pilea-specific elevated `per_species_thresholds`:** require Pilea to clear a bar far above the global 0.55 before it can map to a card, reusing the existing per-species mechanism honestly. *Tradeoff:* zero new code paths and keeps pothos direct, but a 0.9661 pothos→Pilea hit blows past almost any plausible bar, so C **alone** does not fix the core error.
- [ ] Evaluate **Candidate D — TTA/preprocessing-only:** measure whether ×6/×8/×10/×20 reorders the pothos/Pilea ranking enough to avoid a targeted gate at all. *Tradeoff:* if it worked it would need no boundary logic; almost certainly insufficient for a 0.9661 single-class error, but must be **measured, not assumed** (feeds Phase 7).
- [ ] Explicitly **reject margin-only abstention** as the sole fix and record *why*: a 0.9661 top-1 has no useful second-place mass for `high_confidence_abstain_margin` to bite on.
- [ ] **Select the default design: Candidate B (boundary-pair allow-list) — pothos OR Pilea dominating routes the pair to the picker with both candidates shown — optionally combined with C (a measured elevated Pilea threshold)** as the only honest route to a *direct* Pilea card, used solely if the true-Pilea fixtures prove a high bar is safe. This is the one design that neutralizes the no-second-place-mass error while still letting correct Pilea surface as a visible candidate.
- [ ] Write `docs/sprints/evidence/PLANTPOTTING-0012/boundary-gating-decision.md`: chosen design, rejected alternatives with the above tradeoffs, user-facing impact, implementation surface, false-abstain cost, and the evidence threshold required before any *direct* Pilea card is permitted.
- [ ] Set the binding design bar: **adding Pilea must not increase confident-wrong** on clean fixtures, on all variants, **or** on the pothos/Pilea subset vs. the Phase 2 baseline.

## Phase 4 — Implement the gate BEFORE mapping Pilea (tests first)

- [ ] Add a `ModelScoreMapper` unit test reproducing the live failure: a synthetic score row with raw top-1 `"Chinese Money Plant (Pilea peperomioides)"` @ 0.9661 must route to **low-confidence** (never a direct `pilea-peperomioides` card) once the mapping exists and the gate is on.
- [ ] Add a `ModelScoreMapper` unit test for true-Pilea behavior: a Pilea-dominant row routes to a Pilea card **only** if the selected evidence bar is met, otherwise to `LowConfidencePicker` with Pilea + pothos as visible candidates.
- [ ] Add a `ModelScoreMapper` unit test that ordinary non-boundary high-confidence species (e.g. monstera, snake plant) still return direct cards under the unchanged global/margin/abstain thresholds — i.e. the gate is scoped to the pair and the 0011 abstention behavior is byte-for-byte unchanged elsewhere.
- [ ] Add a candidate-bundle test asserting the boundary low-confidence route surfaces **both** `pilea-peperomioides` and `epipremnum-aureum` when the pair fires.
- [ ] Add the boundary-pair rule to `model_manifest.json` as data (e.g. a `boundary_pairs` / pair-routing config block) so the policy is declared in config, parsed by the existing manifest parser — **no** seam change.
- [ ] Implement the boundary-pair gate in `ModelScoreMapper` at the existing score-routing point (it already holds labels, mapped KB ids, ranked top-k scores, and thresholds), composing it **with** — not replacing — the `high_confidence_abstain_margin` veto and the plain/margin branches.
- [ ] If the Phase 1 fallback flagged Pilea as *strict-picker-only* (1–2 fixtures), implement Pilea as "mapped but pair-forced to the picker": Pilea may appear as a candidate but can never surface as a direct high-confidence card this sprint.
- [ ] Run `testDebugUnitTest` for `ModelScoreMapper*`, manifest parsing, and low-confidence candidate display **green before** editing the KB map.

## Phase 5 — Add the Pilea KB entry + mapping AND lift the CI absence guard in lockstep

Land Phases 4-result + 5 as **one tightly-reviewed change set** — Pilea mapping without the gate is the exact regression we are guarding against.

- [ ] Add `pilea-peperomioides` to `app/src/main/assets/kb/species.json` with Chinese-money-plant naming and concise care fields, reusing an existing compatible substrate archetype (do **not** invent a new archetype unless strictly required).
- [ ] Add a license-clean Pilea reference (hero) image **only if** the KB image resolver requires one for the new entry — sourced via the same license discipline (Unsplash/Pexels/Wikimedia), attributed under `docs/licenses/`; otherwise skip.
- [ ] Add `"Chinese Money Plant (Pilea peperomioides)" → pilea-peperomioides` to `plant_class_map.json`.
- [ ] Rewrite the `plant_class_map.json` `_comment`: remove the "deliberately LEFT UNMAPPED" deferral language and replace it with "mapped under PLANTPOTTING-0012 **only** with the pothos↔Pilea boundary gate".
- [ ] Replace `HousePlantClassMapValidationTest.pileaIsNotMapped` with a positive `pileaMapsToPileaPeperomioides` assertion.
- [ ] Update `HousePlantClassMapValidationTest.mapsExactlyThirtyEightClasses` to the new exact count (39) and leave `everyKbSpeciesIdResolvesInBundledKb`, `everyMappingKeyIsVerbatimLabelLine`, `eachNewLabelResolvesToExpectedKbId`, and `existingTenRowsStillPointAtSameKbIds` intact.
- [ ] Add a guard test that **fails if** `"Chinese Money Plant (Pilea peperomioides)"` is mapped while the pothos↔Pilea boundary-pair rule is absent from the production manifest/config (binds the mapping to the gate at CI level).
- [ ] Update any KB-count / image-manifest / species-list tests for the new Pilea species.
- [ ] Replace the Pilea deferral section in `docs/kb/ml-mapping-notes.md` with the measured PLANTPOTTING-0012 boundary decision.

## Phase 6 — Post-mapping measurement & the no-regression acceptance gate

- [ ] Run `AccuracyEvalTest` on `pixel6Api34` **after** Pilea mapping + gate, and save `post-pilea-gated-eval.csv` + `post-pilea-gated-summary.md` under the evidence dir.
- [ ] Build the comparison table: baseline / naive-simulated-Pilea / gated-Pilea across pothos fixtures, Pilea fixtures, all clean photos, all perturbations, top-1 accuracy, direct-card accuracy, low-confidence route rate, and confident-wrong rate.
- [ ] Confirm **every** pothos fixture that previously raw-predicted Pilea now routes to the picker or a correct pothos result — **never** a direct Pilea care card.
- [ ] Confirm true-Pilea fixtures either surface a correct Pilea card under the selected bar **or** route to the picker with Pilea visible as a candidate.
- [ ] Confirm the expanded non-boundary fixtures show **no** new confident-wrong regression introduced by the pair gate.
- [ ] If confident-wrong rose anywhere vs. baseline, tighten the Pilea direct-card rule or fall back to pair-forced picker before accepting the sprint (do not relax the baseline to pass).

## Phase 7 — Supporting TTA sweep (×8/×10/×20, ≤ ~2 s worst-case)

- [ ] Add experiment-only support to run TTA ×8/×10/×20 in `AccuracyEvalTest` **without** changing the production `model_manifest.json` `tta` until a decision is made.
- [ ] Run ×6/×8/×10/×20 on the expanded fixtures and collect, per level: clean-photo metrics, perturbation metrics, pothos/Pilea metrics, direct-card accuracy, confident-wrong rate, low-confidence rate, median latency, p95 (if available), and worst observed latency on `pixel6Api34`.
- [ ] Reject any TTA level whose **worst** observed latency exceeds ~2 s.
- [ ] Compare each surviving level against the **gated ×6** production candidate (not the pre-0011 single-crop path).
- [ ] Adopt a new `tta` in `model_manifest.json` **only if** it yields a meaningful confident-wrong or boundary improvement within the latency budget; otherwise leave `tta = 6`.
- [ ] Save `tta-sweep.csv` + `tta-sweep-decision.md`, and update `model_manifest.json`'s `_comment_preprocessing` **only after** the final decision.

## Phase 8 — Version bump, APK, gates, docs

- [ ] Bump `app/build.gradle.kts` to `versionCode = 6`, `versionName = "0.6.0"`.
- [ ] Run `.\gradlew.bat verifyNoNetworking`.
- [ ] Run `pwsh scripts/check-stub-isolation.sh`.
- [ ] Run `.\gradlew.bat ktlintCheck` **before pushing** (CI-only gate; on Windows stage only real changes — autocrlf churns ~76 files; run `ktlintFormat` if needed).
- [ ] Run `.\gradlew.bat lintDebug` (abortOnError) and fix findings.
- [ ] Run `.\gradlew.bat testDebugUnitTest` (incl. `ModelScoreMapper*`, `HousePlantClassMapValidationTest`, `FixtureLicenseManifestTest`, `FixtureIntegrityTest`, `AccuracyEvalCsvSchemaTest`).
- [ ] Run the established local `pixel6Api34` GMD/androidTest command for `AccuracyEvalTest` + on-device regression tests.
- [ ] Confirm `OnDeviceModelRealInterpreterTest` still pins the bundled AIY V1/3 regression anchor.
- [ ] Build a debug APK and copy it to `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\`; verify via PowerShell that the file landed.
- [ ] Update `docs/ROADMAP.md` with the 0012 result: fixture counts, gating decision, TTA decision, Pilea status (mapped/strict-picker-only/blocked), and remaining limitations.
- [ ] (If shipping a public release) push the `v0.6.0` tag — a `versionName` bump alone never publishes a GitHub Release.

---

## Sequencing / phase dependencies

- [ ] **P1 → P2:** source fixtures before measuring; the boundary numbers are meaningless without real pothos+Pilea photos.
- [ ] **P2 → P3:** design the gate against *measured* top-k behavior, not hoped-for TTA/threshold behavior.
- [ ] **P3 → P4:** choose the user-facing tradeoff explicitly before changing any routing.
- [ ] **P4 → P5:** the gate (and its tests) must be green **before** Pilea is mapped — mapping first would briefly create the confidently-wrong card the sprint exists to prevent.
- [ ] **P5 atomic:** Pilea KB entry + mapping + positive map guard + removal of `pileaIsNotMapped` land together (one commit / one review).
- [ ] **P5 → P6:** post-mapping confident-wrong is the central acceptance gate — run it immediately.
- [ ] **P7 after P1+P4:** TTA is judged on the real expanded fixtures against the gated production candidate.
- [ ] **P8 last:** version/APK only after all routing/KB/fixture/doc gates are green.

## Risks & mitigations

- [ ] **Pilea supply too thin for honest direct-card calibration (~76 ceiling).** → Require ≥ 3 independent fixtures for any *direct* Pilea card; below that, ship Pilea as strict-picker-only and document the shortfall.
- [ ] **The 0.9661 error has no second-place mass.** → Do not rely on margin abstention; use the explicit boundary-pair allow-list (Candidate B), optionally plus a measured elevated Pilea threshold.
- [ ] **Pair-forced picker hides correct Pilea/pothos cards (UX regression).** → Surface both Pilea and pothos as the top picker candidates; promote to a *direct* Pilea card only where clean fixtures prove it safe.
- [ ] **Per-species threshold overfits a tiny Pilea set.** → Prefer the simple pair rule; keep clean vs. perturbation metrics separate; log rejected threshold candidates.
- [ ] **Mapping Pilea breaks map-validation beyond the intended guard.** → Replace only `pileaIsNotMapped`, update the count to 39, keep verbatim-label + KB-id-resolution + existing-row guards intact.
- [ ] **Incomplete CC-BY attribution.** → Manifest tests stay strict; add license rows *before* images land; reject any source whose author/license URL can't be verified.
- [ ] **Higher TTA helps the small fixture set but blows the latency budget.** → Enforce the ~2 s worst-case cap; compare against gated ×6.
- [ ] **Perturbations overweight one scarce Pilea base photo.** → Report base-photo metrics separately; never accept direct-card Pilea behavior from perturbations alone.
- [ ] **Gate logic leaks into the frozen seam.** → Keep all new logic in manifest parsing, `ModelScoreMapper`, internal candidate construction, and tests only.
- [ ] **Sandbox filesystem overlay on Windows** may silently swallow writes outside the project tree (Dropbox APK copy). → Verify the APK landed from the user's terminal / PowerShell before claiming delivery.

## Acceptance criteria

- [ ] No production change alters `PlantIdentifier`, `IdentificationResult`, or `IdSource`.
- [ ] Every added fixture is a real photo with CC0/PD/PD-mark/CC-BY licensing, documented in `fixture-manifest.tsv` and (for CC-BY) `docs/licenses/`.
- [ ] The evidence dir contains: `fixture-sourcing-log.md`, `baseline-pre-pilea-eval.csv` + summary, `boundary-gating-decision.md`, `post-pilea-gated-eval.csv` + summary, `tta-sweep.csv` + `tta-sweep-decision.md`, and any Pilea fallback/blocker note.
- [ ] Pilea is mapped **only** if the production pothos↔Pilea gate is present and tested (CI-enforced by the new bind-mapping-to-gate test).
- [ ] `HousePlantClassMapValidationTest` no longer asserts Pilea absence; it asserts Pilea maps to `pilea-peperomioides`, the count is 39, and all other map-validation guards remain active.
- [ ] Adding Pilea does **not** increase confident-wrong vs. the pre-Pilea baseline on clean fixtures, all variants, or the pothos/Pilea subset.
- [ ] No pothos fixture can surface a direct Pilea care card in the final gated evaluation.
- [ ] True-Pilea fixtures surface a correct Pilea result under the selected bar, **or** route to `LowConfidencePicker` with Pilea visible as a candidate.
- [ ] Any adopted TTA level has documented improvement over gated ×6 and worst observed latency under ~2 s; otherwise `tta` stays 6.
- [ ] If Pilea fixtures cannot be sourced at all, Pilea stays unmapped, the absence guard stays, and `pilea-blocker.md` records the block instead of shipping an unsafe mapping.
- [ ] `verifyNoNetworking`, `check-stub-isolation.sh`, `ktlintCheck`, `lintDebug`, JVM unit tests, fixture/license/integrity/CSV-schema tests, and the local `pixel6Api34` on-device eval are green (or have documented environment-only failures).
- [ ] The shipped build is v0.6.0 / versionCode 6 and the debug APK is verified present in `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\`.
