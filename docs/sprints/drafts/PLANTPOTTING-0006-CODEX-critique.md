# PLANTPOTTING-0006 — Codex Draft Critique

This critique compares the Codex draft against the Gemini and Claude drafts for
PLANTPOTTING-0006. It focuses on concrete sprint planning differences: stronger
coverage, weaker coverage, missing tasks, underweighted risks, and sequencing.

## Gemini draft

### Stronger than Codex

- Gemini's `Goals` section states the sprint outcome more cleanly: "Complete the V0.1 multi-species sweep" and "Provide real-photo evidence across both AIY V1/3 ↔ KB overlaps." Codex implies the same in `Goal`, but Gemini makes the release-level reason easier to see.
- Gemini's `Scope Boundaries` has a useful umbrella rule, `No new infrastructure`, covering fixture pattern, `OnDeviceModelRealInterpreterTest`, and `perSpeciesThresholds` in one place. Codex lists those separately in `Scope Boundaries`, which is more precise but less memorable.
- Gemini's `Tasks & Sequencing / Phase 1: Crassula Ovata Probing & Assertion` keeps the crassula work in one compact path from photo source to assertion. Codex splits this into `Phase 1 — Fixture and Provenance`, `Phase 2 — Probe and Evidence`, and `Phase 3 — Accuracy Assertion and Threshold Decision`, which is safer but more verbose.
- Gemini explicitly names `CI` in `Add the accuracy-bearing assertion for crassula-ovata (...) to the CI`. Codex says to add the assertion and run tests, but the CI intent is less prominent.

### Weaker than Codex

- Gemini recommends `Display <1%` in `Fix the degenerate-capture rendering for "(0%)" chips`. Codex's `Recommended UX Direction` is stronger because it explains why hiding only the degenerate suffix is narrower: the screen appears to have an integer percentage, so `<1%` can imply precision the UI may not still have.
- Gemini's accuracy task says `speciesId == "crassula-ovata" && !lowConfidence` and then says "If it clears the 0.55 global cleanly, use the preferred form." That wording is internally loose: it names the strong assertion before the probe result is known. Codex handles the branch more cleanly in `Phase 3 — Accuracy Assertion and Threshold Decision`.
- Gemini has no explicit task equivalent to Codex's `Verify the fixture is readable from androidTest assets using the same code path as monstera-deliciosa.jpg`.
- Gemini has no explicit subtitle contract task equivalent to Codex's `Update LowConfidencePickerSubtitleContractTest to pin the new subtitle copy`.
- Gemini's verification is too compressed. Codex's `Phase 5 — Verification` separately names `scripts/check-stub-isolation.sh`, `verifyNoNetworking`, focused UI tests, focused mapper/manifest tests if thresholds change, the GMD test, and `scripts/integration-flow.ps1`.

### Missing tasks

- Missing `docs/sprints/results/PLANTPOTTING-0006.md` detail: Gemini says "results doc" but its acceptance criteria only require `ml-mapping-notes.md`. Codex's `Record top-1 species, top-3 candidates, score, route, low-confidence state, fixture source, and test device in docs/sprints/results/PLANTPOTTING-0006.md` is stronger.
- Missing low-confidence subtitle pinning: Gemini's `Rewrite R.string.low_conf_subtitle` should be paired with `LowConfidencePickerSubtitleContractTest`, as Codex does.
- Missing normal-score chip regression: Gemini covers chosen degenerate behavior but does not explicitly preserve normal percentages. Codex has `Preserve normal percentage suffixes for candidates that display as 1% or higher` and `Confirm existing normal-score chip rendering remains covered`.
- Missing manifest contract testing if seeding occurs. Gemini says seed `model_manifest.json` if justified, but Codex adds `Update ModelScoreMapperPerSpeciesThresholdTest and PerSpeciesThresholdsContractTest for the new manifest contract`.
- Missing test device in evidence acceptance. Gemini records top-1/top-3/score/route/source, while Codex also records `device`.

### Underweighted risks

- Gemini underweights fixture quality and licensing. It has no explicit equivalent to Codex's risk `the selected crassula photo is not representative enough` or a license/provenance risk. The `Source a CC-licensed photo` task is not enough mitigation by itself.
- Gemini underweights the `perSpeciesThresholds` semantics risk. Its conditional task `Revisit perSpeciesThresholds top-1-only override semantics ONLY IF real seeding for crassula forces the decision` is good, but its `Risks & Mitigations` treats seeding as straightforward. Codex's risk `crassula exposes ambiguity in top-1-only override semantics` is more honest.
- Gemini underweights copy-contract breakage. Codex names the risk that subtitle copy changes break `LowConfidencePickerSubtitleContractTest`; Gemini only says to update UI.
- Gemini underweights the possibility that fake setup becomes harder to maintain. It has a conditional `FakeFixedIdentifier` split task, but no corresponding risk.

### Sequencing problems

- Gemini's `Phase 1: Crassula Ovata Probing & Assertion` includes both `Run the on-device GMD probe` and `Add the accuracy-bearing assertion... to the CI` in the same phase. It should explicitly separate "probe and record" from "commit final assertion form" so the assertion is not written before the measured score.
- Gemini places `Determine if perSpeciesThresholds needs seeding` after the assertion task. The threshold decision can affect whether `lowConfidence` is false, so Codex's order in `Phase 3 — Accuracy Assertion and Threshold Decision` is safer: probe first, decide threshold, then assert the final intended behavior.
- Gemini's UX phase is independent but not called out as parallelizable. Codex also does not emphasize this as strongly as Claude, but Gemini's sequencing makes all UX work appear blocked behind crassula probing.

## Claude draft

### Stronger than Codex

- Claude is much more specific in `Phase 1 — Fixture & provenance`: `480×480`, `JPEG quality 80`, exact path `app/src/androidTest/assets/identify-fixtures/crassula-ovata.jpg`, and the expected provenance block fields. Codex says "crop dimensions matching the Monstera fixture convention", which is correct but less actionable.
- Claude's photo-selection task is stronger: it says to avoid flowering close-ups or mixed-succulent arrangements and names visual traits like fleshy oval leaves and woody stem. Codex only says "clear, centered, healthy jade plant image".
- Claude's `Phase 2 — Probe, record, assert` includes `source == ON_DEVICE_MODEL` in the preferred assertion. Codex records source in docs but does not include it in the assertion branch.
- Claude's `Phase 3 — perSpeciesThresholds decision` gives the clearest rule for seeding: only if top-1 is correct but score falls below `0.55` by a margin an absolute per-class override would cleanly close, with reference to `_comment_per_species_thresholds`. Codex's branch is sound but less precise.
- Claude's `Phase 5 — UX fix B ((0%) chips)` is stronger than Codex because it states the chip remains selectable and asks the test to cover both "no suffix" and clickability. Codex says preserve the candidate list, but not the explicit clickability assertion.
- Claude's `UX fix B — recommendation & trade-off` is the strongest analysis of the `(0%)` issue. It correctly rejects `<1%` unless the pre-floor float is available and rejects suppressing chips because it removes picker choices. Codex reaches the same recommendation but with less implementation evidence.
- Claude's `Sequencing & dependencies` is stronger because it explicitly marks `Phase 4` and `Phase 5` as parallelizable while the GMD work proceeds.
- Claude's acceptance criteria include flipping the sprint ledger and declaring the V0.1 sweep complete in the results doc. Codex does not mention ledger closure.

### Weaker than Codex

- Claude says `No new test classes` under `Scope boundaries`. That is probably right for this sprint, but it is over-constraining compared with Codex. If local patterns required a new focused test class, the sprint should not forbid it absolutely.
- Claude says to run the `full JVM unit suite` in `Phase 6 — Close-out gates`. Codex's focused-test-first plan is more practical for diagnosis. Full JVM may still be a close-out gate, but the sprint plan should preserve Codex's staged verification path.
- Claude's `Run scripts/integration-flow.ps1 (cold / warm / buildonly)` may be too broad or ambiguous. Codex's `Run the relevant scripts/integration-flow.ps1 mode required by the sprint branch before merge` is less concrete but avoids requiring every mode unless the branch policy actually demands it.
- Claude's `Flip the sprint ledger entry` may be premature as an implementation task if the project treats ledger updates as release-management work after acceptance. Codex avoids adding that ownership assumption, though the omission may still be a gap.

### Missing tasks

- Claude does not explicitly name `LowConfidencePickerSubtitleContractTest`; it says `Update/confirm any test asserting the old subtitle copy`. Codex's named task is better because this was a known 0005 review finding with an existing contract test.
- Claude does not explicitly name `ModelScoreMapperPerSpeciesThresholdTest` or `PerSpeciesThresholdsContractTest` if a threshold is seeded. It says to confirm `ModelManifestReader` parses and `ModelScoreMapper` applies the override, but Codex is better at naming the likely test owners.
- Claude does not explicitly call out `verifyNoNetworking` as a separate gate. It folds this into "network-free gates" in `Phase 6`, while Codex makes it an explicit verification item.
- Claude does not include Codex's `Verify the fixture is readable from androidTest assets using the same code path as monstera-deliciosa.jpg`. It has a merged-assets pickup check, which is close, but not the same runtime asset-read verification.

### Underweighted risks

- Claude underweights possible overreach from `480×480 / JPEG q80` if the existing Monstera fixture does not actually match that exact encoding. The task says "match exactly"; the risk is that a hard-coded dimension/quality requirement could create churn if the local fixture convention differs.
- Claude underweights the cost of running every close-out gate it lists. `Full JVM suite`, `LowConfidencePickerScreenTest`, instrumented Monstera and crassula tests, `integration-flow.ps1 cold/warm/buildonly`, stub isolation, and network-free gates may be too much if this sprint is intended to stay narrow. Codex's focused verification is more proportional, though less complete.
- Claude underweights the risk that `No new test classes` blocks a clean local test pattern. The mitigation should be "prefer existing test classes" rather than a hard prohibition.

### Sequencing problems

- Claude's sequencing is mostly strongest of the three. The main issue is in `Phase 2 — Probe, record, assert`: it includes committing the assertion in the same phase as recording the probe, while `Phase 3` then decides the threshold. Since a seeded threshold can affect `lowConfidence`, the final assertion should land after the threshold decision, or the task should be split into "probe assertion scaffold" and "final assertion".
- Claude's `Phase 6 — Close-out gates` places `full JVM unit suite` before the named UI and instrumented tests. Codex's ordering of focused tests before broader gates is better for failure diagnosis.
- Claude's ledger completion task in `Phase 6` should happen after all evidence and gates are actually green. It is listed with close-out gates, but the dependency should be explicit: do not flip the ledger until acceptance criteria are met.

## If I Were Merging

I would keep Claude's `Phase 1 — Fixture & provenance` specificity, Claude's `(0%)` chip trade-off analysis, and Claude's explicit parallelization of the UX phases.

I would keep Codex's evidence-first threshold sequencing, named test owners (`LowConfidencePickerSubtitleContractTest`, `ModelScoreMapperPerSpeciesThresholdTest`, `PerSpeciesThresholdsContractTest`), explicit `verifyNoNetworking` / stub-isolation gates, and the recommendation to hide the degenerate percentage suffix rather than display `<1%`.

From Gemini, I would keep the compact sprint framing in `Goals` and the concise `No new infrastructure` scope rule, but I would replace Gemini's `<1%` chip recommendation and tighten its assertion/threshold ordering.
