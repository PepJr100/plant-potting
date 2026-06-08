# PLANTPOTTING-0013 Codex Draft Critique

This critique compares my `PLANTPOTTING-0013-CODEX.md` draft against the other two sprint drafts:
`PLANTPOTTING-0013-CLAUDE.md` and `PLANTPOTTING-0013-DEEPSEEK.md`.

## Claude Draft

### Stronger than mine

- **It states the key score-shape problem more bluntly.** Claude's `Phase A0 - The central problem, stated honestly` is stronger than my Phase 2/3 framing because it explicitly says the real Pilea and pothos-to-Pilea failure are "near-identical in score shape" and that top1-top2 margin cannot be the load-bearing separator. My draft still treats a measured top1-top2 margin as part of the direct-card rule in `Define the direct-card candidate rule` and `Pre-commit the candidate threshold grid`. That is more cautious in process, but technically weaker because the known 0.9661 pothos failure has no second-place mass, so margin is probably not discriminative.
- **It binds the threshold to the known pothos ceiling more concretely.** Claude's `Phase A1 - Measure the separation under the strictest eval the fixtures allow` requires computing `separation gap = min real-Pilea top-1 - max pothos->Pilea top-1` and falling back if the gap is non-positive. My `Phase 3 - Offline Threshold And Gate Experiment` says to run a threshold/margin sweep and treat the 0.9661 case as a must-fail sentinel, but it does not make the separation-gap calculation the central decision gate.
- **It has a better dormant-safe implementation path.** Claude's `Phase A4 - Conditional ship decision` keeps the gate-refinement code even if the manifest threshold is absent, so empty `per_species_thresholds` preserves strict-picker behavior. My `Phase 4 - Implement Conditional Direct Pilea Logic` says skip production mapper relaxation if strict-picker is retained. That is safer for immediate behavior, but weaker for an incremental implementation because it loses the chance to land tested dormant code that defaults to strict-picker.
- **It gives clearer repo anchors.** Claude's `Repo anchors (confirm each before editing)` names `ModelScoreMapper.kt`, `ModelManifest.kt`, `OnDeviceIdentifyModule.kt`, `HousePlantClassMapValidationTest`, the fixture manifest, and exact current manifest semantics. My Phase 1 lists several confirmations, but Claude's anchor table is more executable for the implementer.
- **It calls out config-level CI for the pothos ceiling.** Claude's `Phase A3 - Implement the gate refinement (tests first)` asks for a `ModelManifestTest` or `PerSpeciesThresholdsContractTest` enforcing `per_species_thresholds["pilea-peperomioides"] > 0.9661`. My `Add a manifest parsing or contract test...` is weaker because it only verifies the production Pilea threshold/exception config is present with the boundary pair, not that the value stays above the known dangerous score.
- **It preserves the release/Roadmap nuance better.** Claude's ship phase says update `docs/ROADMAP.md` with the 0013 result, while my scope boundary says not to edit ROADMAP unless execution uncovers a correction. If the direct-card decision or fixture-count delta changes the current-state summary, Claude's task is more complete.

### Weaker than mine

- **It is too confident that absolute score is the only separator.** Claude is probably right for the known 0.9661 case, but my draft's `Pre-commit the candidate threshold grid` and `Produce pilea-direct-card-threshold-sweep.csv` leave room to discover whether any other derived signal matters. Claude's `Rejected - margin-over-second as the separator` could prematurely narrow the experiment before extracting all rows from the baseline CSV.
- **Its acceptance criterion overstates "can never".** Claude's acceptance says a pothos-to-Pilea input can never reach a direct Pilea card because the 0.9661 case is CI-bound. My acceptance is narrower and more defensible: the known pothos-to-Pilea input can never reach a direct card, with final eval proving no current `epipremnum-aureum` fixture violates it. Claude does include a residual-risk bullet about future pothos photos, but the acceptance language is still too absolute.
- **Fixture sourcing policy is looser.** Claude's Thread B accepts `CC-BY` real photographs and mentions adding `docs/licenses/` rows. My draft keeps the new fixture target to CC0/public-domain and rejects CC-BY-SA/NC/ND/unknown. Given the user explicitly asked for honest CC0/PD fixture depth and the existing 0012 target set was CC0, my `Phase 7 - Fixture Broadening For Thread B` is the stricter and cleaner policy.
- **It risks implementation before full design pre-commit.** Claude's `A1 -> A2 -> A3 -> A4` is strong, but `Phase A3 - Implement the gate refinement (tests first)` comes before the final post-change decision and allows dormant code to land. My draft more explicitly requires `pilea-direct-card-eval-plan.md` before changing production logic and says the threshold grid must be pre-committed before validation.
- **It under-specifies CSV schema preservation.** My `Phase 8 - Accuracy Scorecard Refresh` explicitly says to update or add the schema guard so `AccuracyEvalCsvSchemaTest` covers the current committed scorecard without dropping required columns. Claude mentions `AccuracyEvalCsvSchemaTest`, but not the risk of weakening the committed scorecard schema while refreshing evidence.

### Missing tasks

- Claude should add a task equivalent to my `Produce pilea-direct-card-threshold-sweep.csv`, with fold id, tuning photos, held-out photo, selected threshold, route, correctness, confident-wrong, and pothos sentinel result. Its `pilea-direct-card-eval.md` fold table is good, but a CSV artifact is easier to audit and diff.
- Claude should add my `Confirm Pilea fixtures that do not clear the selected bar still route to LowConfidencePicker with Pilea visible in mapped_top3_kb_ids` from `Phase 6 - Post-Change Accuracy Gate For Thread A`.
- Claude should add my `Record a final reason-coded coverage table` task from `Phase 7 - Fixture Broadening For Thread B`. It has an outcome table in the sourcing log, but my task names the starting count, added count, ending count, and supply ceiling explicitly.
- Claude should add a task equivalent to my `Verify with PowerShell that the APK is actually present in the Dropbox folder and record filename, size, and timestamp`. Claude verifies presence, but not size/timestamp.

### Underweighted risks

- **License-policy drift.** Claude's acceptance of CC-BY under Thread B underweights the risk that new fixtures become license-policy exceptions rather than the requested CC0/PD broadening.
- **Schema evidence drift.** Claude does not emphasize that changing `AccuracyEvalCsvSchemaTest` can mask old evidence assumptions. My `Risk: Updating AccuracyEvalCsvSchemaTest...` is stronger.
- **Pilea non-deepening vs author separation conflict.** Claude asks for author/source separation and says to drop or note folds that cannot be author-separated, but does not say what happens if dropping folds leaves too little validation signal. My fallback framing is more conservative, though I should also spell this out.

### Wrong sequencing

- Claude's `Ship - version, gates, APK, release tag` includes `Update docs/ROADMAP.md` after building/copying the APK. If ROADMAP changes are required by the 0013 outcome, they should happen before final gates and APK build so the released tag contains the documented state.
- Claude's `Thread B` says run `AccuracyEvalCsvSchemaTest` before the expanded `AccuracyEvalTest`. That is fine as a manifest/schema smoke check, but the schema test must also run after the final CSV artifact is produced or updated. My Phase 8/10 sequencing is clearer on final scorecard then static gates.

## DeepSeek Draft

### Stronger than mine

- **It is more immediately executable for a developer.** DeepSeek's `Repo anchors` table is concrete and includes paths for `.github/workflows/release.yml`, `scripts/source-reference-images.ps1`, and `app/build.gradle.kts`. My draft has the same concepts but fewer path-level anchors.
- **It names exact Pilea fixture authors.** `A2. LEAVE-ONE-OUT threshold tuning on the 6 Pilea fixtures` cites `dinomariobob`, `Tiago Lubiana`, `Olsza Borys`, `Daniel Atha`, `dmagdee`, and `Curran Dwyer`. My draft only says author/source-photo separation; DeepSeek makes the manifest check more tangible.
- **It explicitly includes the release workflow trigger.** `S5. Push the v0.7.0 git tag` names `.github/workflows/release.yml` and says `versionName` alone never publishes. My Phase 10 has the same task, but DeepSeek's wording is clearer about the trigger.
- **It includes sprint review feedback.** DeepSeek's `S6. Run the sprint review gates` and acceptance criterion for `docs/sprints/feedback/PLANTPOTTING-0013/feedback.md` are missing from my draft. My draft stops at release workflow confirmation and does not explicitly create sprint feedback.
- **It calls out TTA interaction in risk terms.** My draft says not to re-litigate TTA and to keep TTA-6, but DeepSeek's `TTA interaction` risk correctly notes that the threshold is checked against TTA-averaged `bestProb` and can move pothos scores.

### Weaker than mine

- **Its threshold tuning direction is wrong.** DeepSeek's `A2. LEAVE-ONE-OUT threshold tuning` says "Set the candidate threshold conservatively above the worst held-out score" while also expecting all six folds to clear. A threshold above the worst held-out real-Pilea score would make that worst held-out Pilea fail direct-card eligibility. My draft avoids this contradiction by pre-committing a threshold grid and validating pass/fail outcomes rather than deriving a threshold with impossible wording.
- **It has a dangerous test example.** DeepSeek's `A7. Update ModelScoreMapperBoundaryGateTest` suggests `pothosInputStillRoutesToPickerEvenWithPerSpeciesThreshold` with threshold `0.90` or even lower. Since the known pothos-to-Pilea score is 0.9661, a threshold of 0.90 would allow the exact failure if the escape only checks `bestProb >= speciesThreshold`. My draft's `pothosToPileaSentinelNeverReturnsDirectPileaCard` with the 0.9661-style score row is safer, but I should have added Claude's config test enforcing `T_pilea > 0.9661`.
- **Its fallback instruction is internally inconsistent.** DeepSeek's `A6. Conditional SHIP / FALLBACK decision` says revert both the `per_species_thresholds` entry and the `ModelScoreMapper` escape, while later acceptance allows the escape to be "reverted or gated behind an absent threshold." Reverting the code loses the dormant-safe path; leaving it gated behind an absent threshold is better. My draft consistently skips implementation if eval fails, but Claude's dormant-safe version is best.
- **It weakens the "no Pilea direct card unless earned" pre-commit discipline.** DeepSeek's `A3` and `A4` implement and set the manifest threshold before `A5` full eval. The sprint can still fall back in `A6`, but this sequencing encourages choosing and activating the threshold before the binding evaluation has proven +0 confident-wrong. My draft's Phase 2/3 pre-commit and simulated sweep before production relaxation is more disciplined.
- **It omits the top1-top2 margin requirement from the candidate rule.** DeepSeek's Thread A mostly becomes an elevated per-species threshold escape. Given the known margin limitation, this may be technically acceptable, but it diverges from the stated sprint requirement to require a clear top1-top2 margin. My draft includes the margin as part of the direct-card candidate rule.
- **It allows CC-BY in one place despite the stated CC0/PD thread.** DeepSeek's non-goals say "CC0 fixture" and Thread B says CC0/PD, which matches my draft. But the intent paragraph says "CC0/PD fixtures" while the scope says "no self-shot"; it does not explicitly reject CC-BY as strongly as my Phase 7 does. This is less severe than Claude's explicit CC-BY acceptance, but still less crisp than my license boundary.

### Missing tasks

- DeepSeek should add a task equivalent to my `Create docs/sprints/evidence/PLANTPOTTING-0013/pilea-direct-card-eval-plan.md before changing production logic`. Its evidence starts with baseline and tuning, but no separate pre-registered eval plan.
- DeepSeek should add my `Produce pilea-direct-card-threshold-sweep.csv` or an equivalent full fold table that includes threshold, margin bar, held-out route, correctness, confident-wrong, and pothos sentinel outcome. Its `pilea-threshold-tuning.csv` is narrower.
- DeepSeek should add my `Add a manifest parsing or contract test that verifies the production Pilea threshold/exception config is present only when the boundary pair is present`, plus Claude's stronger value check `> 0.9661`.
- DeepSeek should add my `Confirm Pilea fixtures that do not clear the selected bar still route to LowConfidencePicker with Pilea visible in mapped_top3_kb_ids` as a post-change on-device assertion.
- DeepSeek should add my explicit `AccuracyEvalCsvSchemaTest` scorecard-refresh task, not just list the test among gates.
- DeepSeek should add my `Confirm the GitHub Release workflow published or record the exact CI failure` after tag push. It says the workflow triggers, but does not require checking the result.

### Underweighted risks

- **Threshold-overlap and threshold direction.** DeepSeek names the pothos score risk, but `A2` underweights the mathematical constraint that a threshold must be above the pothos ceiling and at or below the real-Pilea floor to restore every real-Pilea direct card. The "above the worst held-out score" phrasing makes the risk worse.
- **CI enforceability.** DeepSeek's `A8. CI binding` admits the on-device eval is a documentation gate if unenforceable in CI. That is honest, but underweighted: the plan needs an enforceable JVM-level config guard for `T_pilea > 0.9661` and synthetic mapper rows.
- **Roadmap drift.** DeepSeek says ROADMAP is already accurate, then says to update only if Thread A ships or falls back substantively. My draft also leans no-change, but the sprint's fixture-count refresh and direct-card decision likely deserve a ROADMAP note; Claude handles this better.
- **Source pipeline legality.** DeepSeek names Unsplash/Pexels/Wikimedia/GBIF and a sourcing script, but does not explicitly require reject logging for every non-CC0/PD near-miss until `B5`. My `fixture-sourcing-log.md` task starts before sourcing and records accept/reject decisions throughout.

### Wrong sequencing

- `A2 -> A3 -> A4 -> A5` is too activation-oriented. The safer sequence is: baseline, pre-registered eval plan, offline threshold sweep/LOO, decision, then code/config activation only if the decision earns it. DeepSeek activates the manifest in `A4` before the post-direct-card evidence exists.
- `A6` says if fallback fires, "A7-A9 still execute," but `A7` is listed before `A8/A9` and after the fallback decision in the task list. Test work should happen before or during implementation, not after the post-change eval. My Phase 5 puts unit and contract tests before post-change accuracy gating.
- `Phase 4 - Ship` does not explicitly place ROADMAP/doc outcome updates before the final version/tag gate. If ROADMAP or feedback is part of the release artifact, it should precede the final full gate run and tag.

## If I Were Merging

- I would keep **Claude's `Phase A0` score-shape framing**, the explicit `separation gap` gate from **Claude `Phase A1`**, the dormant-safe empty-threshold behavior from **Claude `Phase A4`**, and the config guard enforcing `per_species_thresholds["pilea-peperomioides"] > 0.9661` from **Claude `Phase A3`**.
- I would keep **DeepSeek's repo-anchor table**, the named Pilea fixture-author check from **DeepSeek `A2`**, the explicit release workflow trigger from **DeepSeek `S5`**, and the sprint feedback artifact from **DeepSeek `S6`**.
- I would keep from my draft the stricter **pre-registered eval plan plus threshold-sweep artifacts** from `Phase 2` and `Phase 3`, the CC0/PD-only fixture broadening discipline from `Phase 7`, the schema-drift guard from `Phase 8`, and the final release verification task that records the Dropbox APK and GitHub Release outcome from `Phase 10`.
