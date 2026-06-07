# PLANTPOTTING-0012 - Pothos/Pilea Boundary Sprint

PLANTPOTTING-0012 ships the long-deferred `Pilea peperomioides` KB entry only if the pothos->Pilea failure is made honest first. The production `house_plant_species_mobilenetv2` model, the AIY regression anchor, and the frozen `PlantIdentifier` / `IdentificationResult` / `IdSource` seam stay unchanged. The sprint expands the license-clean real-photo fixtures, measures the pothos/Pilea boundary on `AccuracyEvalTest`, adds targeted boundary gating inside the existing mapping/routing layer, then maps Pilea and lifts the CI absence guard in the same change set.

## Goals

- [ ] Expand the CC0/PD/CC-BY identify fixture set with real pothos and real `Pilea peperomioides` photos, plus any of the 8 currently untested mapped species that have license-clean sources.
- [ ] Produce an honest pre-change boundary scorecard for pothos, Pilea, the existing 41-photo set, perturbations, top-k scores, routes, confident-wrong rate, and latency on the local `pixel6Api34` `AccuracyEvalTest` path.
- [ ] Add a targeted pothos/Pilea disambiguation mechanism that prevents a confidently wrong Pilea care card from surfacing for pothos after Pilea becomes mapped.
- [ ] Add the Pilea KB entry and `Chinese Money Plant (Pilea peperomioides)` model mapping only in the same implementation phase as the boundary gate.
- [ ] Replace the `HousePlantClassMapValidationTest.pileaIsNotMapped` guard with positive Pilea mapping, count, and boundary-safety guards without weakening the remaining map validation rules.
- [ ] Sweep TTA x8, x10, and x20 against the expanded harness up to a roughly 2 second worst-case latency budget, adopting a new production TTA only if the confident-wrong/latency curve justifies it.
- [ ] Ship v0.6.0 / versionCode 6 with a debug APK copied to `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\`.

## Non-goals And Scope Boundaries

- [ ] Do not swap, retrain, fine-tune, quantize, or otherwise alter the bundled production model.
- [ ] Do not change `PlantIdentifier`, `IdentificationResult`, or `IdSource`.
- [ ] Do not use self-shot, first-party, principal-supplied, generated, CC-BY-SA, CC-BY-NC, NC, ND, or unknown-license imagery for fixtures or evidence.
- [ ] Do not add KB content beyond the Pilea species entry, its care/archetype linkage, its reference image if needed, and its model mapping.
- [ ] Do not bump AGP, Kotlin, Compose, Hilt, TFLite, or other platform dependencies.
- [ ] Do not work on My Plants backup/export/uninstall persistence.
- [ ] Do not loosen runtime networking bans, stub isolation, fixture license guards, fixture integrity guards, CSV schema guards, or the AIY V1/3 regression anchor.

## Phase 1 - Fixture Sourcing And License Discipline

- [ ] Create `docs/sprints/evidence/PLANTPOTTING-0012/fixture-sourcing-log.md` with search terms, source URLs, license decisions, accepted files, rejected near-misses, and the final per-species counts.
- [ ] Inventory the current `app/src/androidTest/assets/identify-fixtures/` set and identify baseline counts for `epipremnum-aureum`, Pilea, and the 8 mapped species with no clean fixture coverage.
- [ ] Query Wikimedia Commons, GBIF/iNaturalist license filters, Smithsonian Gardens, and other license-verifiable repositories for `Epipremnum aureum`, pothos, devil's ivy, golden pothos, `Pilea peperomioides`, Chinese money plant, missionary plant, pancake plant, UFO plant, and each untested mapped species.
- [ ] Accept only real photos with CC0, public domain, public-domain mark, or CC-BY licenses that permit commercial app test usage and can be attributed in `docs/licenses/`.
- [ ] Reject and log CC-BY-SA, CC-BY-NC, CC-BY-ND, all-rights-reserved, ambiguous, cultivar-mismatched, drawing/plate, duplicate, watermarked, and non-houseplant-context sources.
- [ ] Prefer Pilea photos that show the round peltate leaf habit clearly, include multiple angles/backgrounds, and are independently authored, because the known license-clean Pilea supply is thin at about 76 candidates.
- [ ] Prefer pothos photos that cover juvenile and mature leaf shape, variegated and green forms, trailing/vining habit, top-down and oblique views, and common indoor pot contexts.
- [ ] Add accepted fixture JPEGs under the existing identify-fixture convention with stable species ids such as `epipremnum-aureum__NN.jpg` and `pilea-peperomioides__NN.jpg`.
- [ ] Add or update fixture manifest rows with filename, expected KB species id, model label if applicable, source URL, author, license, license URL, acquisition date, and notes.
- [ ] Add CC-BY attribution details for every CC-BY fixture under `docs/licenses/` and keep CC0/PD rows explicit even when attribution is not legally required.
- [ ] Run `FixtureLicenseManifestTest` after adding the first pothos/Pilea fixture batch and fix manifest schema or license vocabulary failures before adding more images.
- [ ] Run `FixtureIntegrityTest` after the fixture batch to catch corrupt images, unresolved species ids, and accidental non-photo files.
- [ ] If at least 3 independent license-clean Pilea fixtures cannot be sourced, record the Pilea fixture shortfall and switch the implementation plan to the fallback path in Phase 4 instead of forcing a weak Pilea high-confidence route.
- [ ] If no Pilea fixture can be sourced at all, keep Pilea unmapped for this sprint, keep the absence guard, ship only fixture expansion/TTA evidence, and document the block in `docs/sprints/evidence/PLANTPOTTING-0012/pilea-blocker.md`.

## Phase 2 - Baseline Boundary Measurement

- [ ] Run the unchanged production app state through `AccuracyEvalTest` on the local `pixel6Api34` emulator before mapping Pilea or changing gates.
- [ ] Save the untouched baseline CSV and summary as `docs/sprints/evidence/PLANTPOTTING-0012/baseline-pre-pilea-eval.csv` and `baseline-pre-pilea-summary.md`.
- [ ] Add boundary-specific summary rows for each pothos and Pilea fixture: expected species, raw top-1 label/score, Pilea rank/score, pothos rank/score, top1-top2 margin, mapped result, route, correctness, confident-wrong flag, and latency.
- [ ] Add aggregate metrics for clean photos and perturbations separately so derived variants cannot hide the base-photo behavior.
- [ ] Compute the current pothos->Pilea failure rate while Pilea is still unmapped and report how often it routes to `LowConfidencePicker` only because the top-1 label maps to nothing.
- [ ] Compute the simulated naive-Pilea-mapped outcome from the baseline CSV so the plan has a concrete "what would break" number before any gating work.
- [ ] Confirm the pre-change `HousePlantClassMapValidationTest.pileaIsNotMapped` guard still passes before Phase 3 begins.

## Phase 3 - Disambiguation Design

- [ ] Evaluate candidate A, a pairwise top-k delta rule: when top-1 is Pilea and the pothos label appears in top-k within a measured delta, route to `LowConfidencePicker` with both candidates.
- [ ] Evaluate candidate B, a class-pair allow-list rule: when either Pilea or pothos dominates above threshold, force the boundary pair into `LowConfidencePicker` so the user chooses manually.
- [ ] Evaluate candidate C, a Pilea-specific elevated `per_species_thresholds` rule: require Pilea to clear a much higher score than the global 0.55 before returning a card.
- [ ] Evaluate candidate D, TTA/preprocessing-only mitigation: compare whether x6, x8, x10, or x20 changes the pothos/Pilea ranking enough to avoid a targeted gate.
- [ ] Reject a margin-only solution if the pothos fixture remains a 0.9661-style single-class confident error with no useful second-place mass.
- [ ] Choose the default design as a boundary-pair allow-list with optional measured Pilea elevated threshold if true-Pilea fixtures remain too weak to route safely.
- [ ] Document the selected design and rejected alternatives in `docs/sprints/evidence/PLANTPOTTING-0012/boundary-gating-decision.md`, including user impact, implementation surface, false-abstain cost, and evidence thresholds.
- [ ] Set the design bar that adding Pilea must not increase confident-wrong rate on clean fixtures, all variants, or the pothos/Pilea subset compared with the pre-Pilea baseline.

## Phase 4 - Implement Boundary Gate Before Mapping Pilea

- [ ] Add mapper-level unit tests that model the current pothos failure: raw top-1 `Chinese Money Plant (Pilea peperomioides)` at high confidence must route low-confidence rather than returning `pilea-peperomioides` after the mapping exists.
- [ ] Add mapper-level unit tests for true Pilea fixtures or synthetic score rows showing the selected rule's intended route: Pilea card only when the rule's evidence bar is met, otherwise `LowConfidencePicker`.
- [ ] Add mapper-level unit tests that ordinary non-boundary high-confidence species still return direct cards under the existing global and margin thresholds.
- [ ] Add candidate-bundle tests that the boundary low-confidence route includes both `pilea-peperomioides` and `epipremnum-aureum` when both are known candidates or when the configured pair forces them in.
- [ ] Extend the internal model manifest or mapper configuration with a pothos/Pilea boundary-pair rule without changing `PlantIdentifier`, `IdentificationResult`, or `IdSource`.
- [ ] Implement the selected boundary-pair gate in `ModelScoreMapper` or the narrowest existing score-routing layer that already has labels, mapped KB ids, top-k scores, and thresholds.
- [ ] Preserve the existing `high_confidence_abstain_margin = 0.30` behavior and ensure the new pair gate composes with, rather than replaces, the 0011 abstention policy.
- [ ] If the Pilea fixture shortfall fallback is active, implement Pilea as "mapped but strict-picker-only" for this pair so Pilea can appear in the picker but cannot surface as a direct high-confidence card.
- [ ] Run JVM unit tests covering `ModelScoreMapper`, candidate bundle behavior, manifest parsing, and low-confidence picker candidate display before editing the KB map.

## Phase 5 - Add Pilea KB Entry And Lift CI Absence Guard In Lockstep

- [ ] Add `pilea-peperomioides` to `app/src/main/assets/kb/species.json` with Chinese Money Plant naming, concise care fields, and an existing compatible substrate archetype unless a new archetype is strictly required.
- [ ] Add or select a license-clean Pilea reference image only if the KB image resolver requires one for the new entry, with attribution documented under `docs/licenses/`.
- [ ] Add `"Chinese Money Plant (Pilea peperomioides)"` to `app/src/main/assets/ml/house_plant_species_mobilenetv2/plant_class_map.json` mapped to `pilea-peperomioides`.
- [ ] Remove the deferral `_comment` language from `plant_class_map.json` and replace it with a note that Pilea is mapped only with PLANTPOTTING-0012 boundary gating.
- [ ] Replace `HousePlantClassMapValidationTest.pileaIsNotMapped` with a positive `pileaMapsToPileaPeperomioides` assertion.
- [ ] Update `HousePlantClassMapValidationTest.mapsExactlyThirtyEightClasses` to the new exact count after Pilea mapping and keep all verbatim-label, KB resolution, existing-ten, 0009, and 0010 mapping guards intact.
- [ ] Add a test that fails if `Chinese Money Plant (Pilea peperomioides)` is mapped but the pothos/Pilea boundary gate is absent from the production manifest/configuration.
- [ ] Update KB count tests, image manifest tests, and any species-list expectations for the new Pilea species.
- [ ] Update `docs/kb/ml-mapping-notes.md` to replace the Pilea deferral section with the measured PLANTPOTTING-0012 boundary decision.

## Phase 6 - Post-Mapping Measurement And No-Regression Gate

- [ ] Run `AccuracyEvalTest` on `pixel6Api34` after Pilea mapping and boundary gating.
- [ ] Save `post-pilea-gated-eval.csv` and `post-pilea-gated-summary.md` under `docs/sprints/evidence/PLANTPOTTING-0012/`.
- [ ] Compare baseline, naive-simulated-Pilea, and gated-Pilea outcomes for pothos fixtures, Pilea fixtures, all clean photos, all perturbations, top-1 accuracy, direct-card accuracy, low-confidence route rate, and confident-wrong rate.
- [ ] Confirm every pothos fixture that previously raw-predicted Pilea now routes to `LowConfidencePicker` or a correct pothos result, never a direct Pilea care card.
- [ ] Confirm true Pilea fixtures either route to a correct Pilea card under the selected bar or route to `LowConfidencePicker` with Pilea visible as a candidate.
- [ ] Confirm the expanded non-boundary fixture set does not show a new confident-wrong regression introduced by the pair gate.
- [ ] If confident-wrong increases after adding Pilea, tighten the Pilea direct-card rule or fall back to pair-forced `LowConfidencePicker` before accepting the sprint.

## Phase 7 - Supporting TTA Sweep

- [ ] Add configurable experiment support for TTA x8, x10, and x20 while keeping the production manifest unchanged until a decision is made.
- [ ] Run x6, x8, x10, and x20 on the expanded `AccuracyEvalTest` fixture set and collect clean-photo metrics, perturbation metrics, pothos/Pilea metrics, direct-card accuracy, confident-wrong rate, low-confidence rate, median latency, p95 latency if available, and worst observed latency.
- [ ] Stop or reject any TTA candidate whose worst observed latency exceeds roughly 2 seconds on `pixel6Api34`.
- [ ] Compare each TTA candidate against the gated x6 production baseline, not against the pre-0011 single-crop path.
- [ ] Adopt a new production `tta` value in `model_manifest.json` only if it gives a meaningful confident-wrong or boundary improvement without exceeding the latency budget.
- [ ] Leave `tta` at 6 if x8/x10/x20 gains are marginal, noisy, boundary-neutral, or too slow.
- [ ] Save `tta-sweep.csv` and `tta-sweep-decision.md` under `docs/sprints/evidence/PLANTPOTTING-0012/`.
- [ ] Update `model_manifest.json` preprocessing comments only after the final TTA decision is made.

## Phase 8 - Versioning, APK, And Final Verification

- [ ] Bump the app to v0.6.0 and versionCode 6 in the repo's version source.
- [ ] Run `.\gradlew.bat verifyNoNetworking`.
- [ ] Run `pwsh scripts/check-stub-isolation.sh`.
- [ ] Run `.\gradlew.bat ktlintCheck` before pushing because it is CI-only but required by the sprint.
- [ ] Run `.\gradlew.bat lintDebug` and fix abort-on-error findings.
- [ ] Run `.\gradlew.bat testDebugUnitTest`.
- [ ] Run the established local `pixel6Api34` GMD/androidTest command for `AccuracyEvalTest` and the on-device regression tests.
- [ ] Confirm `OnDeviceModelRealInterpreterTest` still pins the bundled AIY V1/3 regression anchor.
- [ ] Build a debug APK.
- [ ] Copy the debug APK to `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\`.
- [ ] Update `docs/ROADMAP.md` with the PLANTPOTTING-0012 result, fixture counts, gating decision, TTA decision, and remaining limitations.

## Sequencing And Dependencies

- [ ] Complete Phase 1 before trusting any new Pilea metric, because the Pilea supply is thin and license-clean scarcity is the sprint's main data risk.
- [ ] Complete Phase 2 before design finalization, because the gate must respond to measured top-k behavior rather than the hoped-for behavior of TTA or thresholds.
- [ ] Complete Phase 3 before changing production routing, because the pairwise gate has user-facing tradeoffs and must be chosen explicitly.
- [ ] Complete Phase 4 before Phase 5, because Pilea mapping without the gate converts today's unmapped honest miss into a confident wrong care card.
- [ ] Land the Pilea KB entry, Pilea mapping, positive map-validation guard, and removal of `pileaIsNotMapped` in one commit or one tightly reviewed change set.
- [ ] Run Phase 6 immediately after Phase 5, because post-mapping confident-wrong behavior is the sprint's central acceptance gate.
- [ ] Run the TTA sweep after the expanded fixtures and boundary gate exist, because TTA must be judged against the real production candidate path.
- [ ] Leave version bump and APK packaging until all model-routing, KB, fixture, and documentation gates are green.

## Risks And Mitigations

- [ ] Risk: Pilea license-clean supply is too thin to support honest direct-card calibration. Mitigation: require a minimum independent fixture count, document the shortfall, and use strict pair-forced picker fallback rather than pretending a high-confidence Pilea card is safe.
- [ ] Risk: The model's 0.9661 pothos->Pilea error has no useful second-place margin. Mitigation: do not rely on margin abstention alone; use explicit boundary-pair routing or a Pilea elevated threshold measured on the boundary set.
- [ ] Risk: Pair-forced picker hides correct Pilea cards and makes the app feel less capable. Mitigation: surface both Pilea and pothos as top picker candidates and adopt direct Pilea only where clean fixtures prove it is safe.
- [ ] Risk: Per-species thresholding overfits a tiny Pilea set. Mitigation: prefer a simple boundary-pair rule, keep clean-photo and perturbation metrics separate, and document rejected threshold candidates.
- [ ] Risk: Adding Pilea breaks map-validation tests beyond the intended absence guard. Mitigation: replace only `pileaIsNotMapped`, update the exact count, and keep verbatim-label and KB-id resolution tests intact.
- [ ] Risk: CC-BY attribution is incomplete. Mitigation: keep manifest tests strict, add license rows before images land, and reject sources whose author/license URL cannot be verified.
- [ ] Risk: Higher TTA improves the small fixture set but exceeds real-device latency. Mitigation: enforce the roughly 2 second worst-case cap and compare against x6 after boundary gating.
- [ ] Risk: Fixture perturbations overweight a single scarce Pilea photo. Mitigation: report base-photo metrics separately and avoid accepting direct-card Pilea behavior from perturbations alone.
- [ ] Risk: The boundary gate leaks into the public seam. Mitigation: keep all new logic inside manifest parsing, score mapping, internal candidate construction, or tests.

## Acceptance Criteria

- [ ] No production code changes alter `PlantIdentifier`, `IdentificationResult`, or `IdSource`.
- [ ] Every added fixture is a real photo with CC0, PD, public-domain mark, or CC-BY licensing documented in the fixture manifest and `docs/licenses/`.
- [ ] The final evidence directory contains sourcing log, baseline pre-Pilea CSV/summary, boundary gating decision, post-Pilea gated CSV/summary, TTA sweep CSV/decision, and any Pilea scarcity fallback note.
- [ ] Pilea is mapped only if the production pothos/Pilea gate is present and tested.
- [ ] `HousePlantClassMapValidationTest` no longer asserts Pilea absence and instead asserts Pilea maps to `pilea-peperomioides` while all other map validation guards remain active.
- [ ] Adding Pilea does not increase confident-wrong rate versus the pre-Pilea baseline on clean fixtures, all variants, or the pothos/Pilea subset.
- [ ] No pothos fixture can surface a direct Pilea care card in the final gated evaluation.
- [ ] True Pilea fixtures either surface a correct Pilea result under the selected evidence bar or route to `LowConfidencePicker` with Pilea available as a candidate.
- [ ] Any adopted TTA value has documented improvement over gated TTA-6 and worst observed latency under the roughly 2 second budget.
- [ ] If Pilea fixtures cannot be sourced, Pilea remains unmapped, the absence guard remains, and the sprint records the blocker rather than shipping an unsafe KB mapping.
- [ ] `verifyNoNetworking`, `scripts/check-stub-isolation.sh`, `ktlintCheck`, `lintDebug`, JVM unit tests, fixture/license/integrity/CSV schema tests, and local `pixel6Api34` on-device evaluation are green or have environment-only failures documented.
- [ ] The shipped build is v0.6.0 / versionCode 6 and the debug APK is present in `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\`.
