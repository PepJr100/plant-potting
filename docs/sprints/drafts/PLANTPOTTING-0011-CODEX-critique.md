# PLANTPOTTING-0011 Codex Draft Critique

This critique compares the Codex draft against `PLANTPOTTING-0011-GEMINI.md` and `PLANTPOTTING-0011-CLAUDE.md`, focusing on methodological honesty, threshold overfit risk, no-self-shot measurement constraints, TTA latency, missing tasks, risk weighting, and sequencing.

## Gemini Draft

### Stronger Than Codex

Gemini is more concise and easier to scan. Its phase names map directly to the sprint intent: `Phase 1: Measure (The Baseline)`, `Phase 2: Abstain (Threshold Tuning)`, `Phase 3: Try to Improve (Raw Accuracy Levers)`, and `Phase 4: Fold-in Refinements`. Codex is more operationally complete, but Gemini's shorter structure makes the sprint's three moves obvious.

Gemini's `Modify ModelScoreMapper to support an additional margin-based abstention check above the plain high-confidence gate` puts the central new gating feature first in the abstention phase. Codex includes the same idea in `Evaluate candidate threshold policies... add an above-plain minimum top1-top2 margin abstention`, but it is buried inside a sweep task and then implementation follows later. Gemini's wording makes the required capability more visible.

Gemini also keeps the acceptance criteria compact. `The confident-wrong rate on the evaluation set is significantly reduced... with the "pick manually" flow handling the abstained cases` is a clear user-facing outcome. Codex is more measurable, but Gemini has a sharper acceptance headline.

### Weaker Than Codex

Gemini under-specifies the license discipline. `Research and download an expanded set of CC0/PD real-world houseplant photos` and `Verify licenses, build an attribution manifest...` are directionally right, but it does not name required manifest fields, fixture naming, decode/integrity checks, rejected-source logging, or the constraint that perturbations are derived only from clean sources. Codex's `Replace the current freeform fixture LICENSE.txt... machine-checkable attribution manifest`, `Add a fixture integrity test`, and `Document scarcity honestly... including rejected near-misses` are stronger.

Gemini is too casual about per-species thresholds. `Seed per_species_thresholds... for specific chronic offenders (like snake plant) if global tuning is insufficient` risks overfitting to the user's reported snake plant failure and a tiny clean fixture set. Codex's `Prefer global policy changes over per-species thresholds...` and requirement for `multiple independent clean fixtures or perturbation families` are safer.

Gemini's evidence plan is thin. `baseline_metrics.md` and `tuned_metrics.md` are not enough for auditability. Codex asks for per-row CSVs with top-1/top-2 scores, margin, mapped top-3, route, correctness, confident-wrong flag, low-confidence flag, latency, and failure. That detail matters because this sprint is about honest measurement, not just a summary number.

Gemini does not separate clean-photo metrics from perturbation metrics or per-base-image weighting. Its risk mitigation says synthetic perturbations "effectively multiply the evaluation set size", which is methodologically dangerous. Perturbations improve stress coverage, but they do not create independent real-world samples. Codex's risk `Perturbations can overweight one source photo` and mitigation to aggregate both all variants and per-base-image averaged metrics is better.

Gemini's TTA treatment is weaker. `Implement multi-crop / test-time-augmentation (TTA) averaging logits over several crops` and `Evaluate the accuracy vs. latency trade-offs` are correct but vague. Codex's `bounded multi-crop/TTA experiment... center crop plus four corner-biased crops`, `Measure latency... median and worst observed latency`, and `Adopt multi-crop/TTA only if... added on-device latency is acceptable` better confront whether TTA is justified.

### Missing Tasks

Gemini is missing a fixture decode/integrity task equivalent to Codex's `Add a fixture integrity test that decodes every fixture...`.

Gemini is missing machine-readable per-row evidence equivalent to Codex's `Emit a CSV...` task.

Gemini is missing a schema/check task like Codex's `Add a small parser/check test for the generated CSV schema`.

Gemini is missing baseline capture before any threshold or preprocessing changes as a strict artifact gate. It says `Record the initial baseline metrics`, but not that thresholds/preprocessing must remain untouched when that baseline is captured.

Gemini is missing targeted unit coverage for existing `ModelScoreMapper` behavior before changing policy. Codex's `Add unit coverage around ModelScoreMapper for the existing direct high-confidence path...` is important.

Gemini is missing explicit final verification for JVM tests, instrumentation compile-clean, GMD/pixel6 evidence pull, and confirmation of Pilea absence beyond acceptance criteria.

### Underweighted Risks

The central methodological risk is underweighted. Gemini names overfitting, but its mitigation leans on perturbation count expansion and avoids only "overly granular" per-species thresholds. It does not require leave-one-species-out, clean-vs-perturbation splits, per-base-image weighting, or multiple independent photos before a per-species override.

Honest accuracy under the no-self-shot ban is also underweighted. Gemini says all photos must be CC0/PD, but it does not force documentation of scarcity, rejected sources, or the limits of a small clean fixture set. That makes it easier for the sprint to overclaim.

TTA latency is underweighted. Gemini says discard if latency spikes beyond acceptable UX bounds, but it does not define the crop count, measure worst-case latency, compare against center-crop, or require a clear gain over the cheaper alternative. A vague "winning strategy is merged" could accidentally merge TTA for a small fixture-set gain.

### Wrong Sequencing

Gemini says `Phase 2 (Abstain)` and `Phase 3 (Try to Improve)` can proceed in parallel or sequentially. That is risky. Preprocessing changes alter the score distribution, so thresholds tuned before a kept preprocessing change may be stale. Codex's sequence is better than Gemini's because it separates baseline, abstention, and preprocessing, but Codex should also say more explicitly that if preprocessing is adopted, final threshold tuning must be rerun against the shipped pipeline.

Gemini also allows `Phase 4 (Fold-in Refinements)` at any time. The UI change can happen any time, but reference-image replacement should reuse the same license discipline established by the fixture work. Codex captures that nuance better in `Phase 5 can proceed in parallel after Phase 1 license sourcing patterns are established`.

## Claude Draft

### Stronger Than Codex

Claude is strongest on the central methodological risk. Its `Anti-overfit guard (central methodological risk)` task requires leave-one-species-out / clean-vs-perturbation split and says to report perturbation rows not hand-tuned against as the honest generalization number. Codex mentions overfit mitigation in risks and says to separate base-photo and perturbation metrics, but it does not turn that into a concrete tuning task.

Claude is also stronger on default-disabled implementation. `Extend ModelManifest.Thresholds with one new optional field... defaulting to 0f`, `preprocess_mode... default "squash"`, and `tta count default 1` keep AIY and current production behavior stable until evidence justifies flipping defaults. Codex proposes the policy and preprocessing experiments, but it does not explicitly require new manifest fields to default to current behavior.

Claude's `Phase 0 - Scaffolding & evidence dir` is better than Codex's Phase 6-only verification. Capturing pre-sprint green state and an evidence README before behavior changes makes before/after claims more auditable.

Claude's scorecard task is stronger in two ways: it includes top-3 accuracy and requires separate summaries for clean and each perturbation family. Codex includes mapped top-3 IDs in CSV and aggregate metrics, but top-3 accuracy is not a named goal or acceptance criterion.

Claude handles TTA latency more rigorously. `TTA multiplies interpreter runs`, `keep N small (<=5)`, `default off if marginal`, and `ADOPT / DROP` with latency before/after are stronger than Codex's more general `acceptable for camera identification`.

Claude is more precise about current preprocessing. `Today ImagePreprocessor does a non-aspect-preserving ResizeOp... a squash` and the task to add `center_crop` under a manifest `preprocess_mode` gives implementers a clearer starting point than Codex's `Add an internal preprocessing strategy abstraction`.

Claude's sequencing is better where it says `if Phase 3 adopts a preprocessing change, re-run Phase 2's tuning on top of it`. Codex says Phase 4 should branch from post-abstention state, but not that adopted preprocessing invalidates or at least requires rechecking threshold calibration.

### Weaker Than Codex

Claude violates or softens the shared image-license constraint in one place. The context says all real photos added must be CC0/public-domain only. Claude's fixture sourcing task says `CC-BY-SA is allowed for test-only fixtures, as the existing fixtures already are, but prefer CC0/PD`. That is weaker than Codex and conflicts with the hard constraint. Codex correctly keeps identify fixtures to CC0/public-domain.

Claude prescribes fixture naming as `<kb-species-id>.jpg` while also targeting multiple new fixtures. That does not support multiple independent photos per species without collision. Codex's `<kb-species-id>__NN.jpg` naming is stronger and supports the methodological need for more than one independent base image per species.

Claude's `Source each via scripts/source-reference-images.ps1 / source-missing-images.ps1` may over-constrain implementation. Those scripts may be useful, but Codex's source-agnostic attribution manifest tasks are less brittle. The sprint should enforce license/provenance, not require a particular sourcing script unless it is known to work for identify fixtures.

Claude's non-goals section uses checkboxes. The shared draft structure required actionable items to be checkboxes; non-goals are not actionable and should not look like work items. Codex's non-goals are cleaner.

Claude includes `AIY baseline anchor` several times. That may be valuable repo context, but the shared intent says the production MobileNetV2 model is the subject. Codex stays more focused on production-model evidence while still requiring instrumentation/GMD verification.

### Missing Tasks

Claude is missing Codex's explicit `fixture integrity test that decodes every fixture, verifies nonzero dimensions, verifies the expected species id resolves in the KB, and marks whether the species is reachable by the active model mapping`. Claude has a fixture readability/license guard, but not the KB/mapping reachability check.

Claude is missing Codex's explicit CSV schema guard: `Add a small parser/check test for the generated CSV schema`. Given the sprint depends on evidence artifacts, this is worth keeping.

Claude is missing Codex's `Prefer global policy changes over per-species thresholds unless...` as a standalone task. Claude says global changes in the tuning task and anti-overfit risk, but Codex makes the decision rule more direct.

Claude is missing Codex's specific requirement to document `rejected threshold candidates` in `docs/kb/ml-mapping-notes.md`. Claude asks for calibration story and chosen setting, but less explicitly requires rejected candidate documentation.

### Underweighted Risks

The biggest underweighted risk in Claude is license compliance. Because it allows CC-BY-SA for test-only fixtures, the draft creates a direct conflict with the no-self-shot/CC0-PD-only measurement constraint. That is not just a paperwork issue; if non-CC0/PD fixtures enter the eval, the "honest accuracy under the no-self-shot ban" deliverable is compromised.

Claude slightly underweights fixture independence by naming new files `<kb-species-id>.jpg`. Its anti-overfit plan depends on leave-one-species-out and perturbation splits, but without multiple base images per species, species-level splits can become too coarse and perturbations can still overweight a single clean photo.

Claude otherwise weights TTA latency appropriately and is stronger than Codex there.

### Wrong Sequencing

Claude's sequencing is mostly better than Codex's. The key improvement is its explicit final retune if preprocessing is adopted. Codex should borrow that.

The one sequencing issue is in `Phase 1a`, where sourcing, center-cropping, scaling, and re-encoding fixture assets are all bundled before the license cross-check exists. Ideally, the manifest/cross-check shape should be established before or alongside asset ingestion so invalid licenses fail immediately. Codex also has this problem to a lesser degree, but its machine-checkable manifest task appears earlier and does not prescribe transforming images first.

Claude's `Phase 4` is independent for the confidence bar, but the reference-image replacement should depend on the same CC0/PD sourcing discipline and manifest-test expectations. Codex's sequencing states that image replacements can proceed after Phase 1 license patterns are established, which is safer.

## What Codex Should Change Before Merge

Keep Codex's strong operational details: machine-checkable attribution manifest, fixture integrity test, detailed per-row CSV, parser/schema guard, explicit rejected-source documentation, and clear global-before-per-species threshold preference.

Borrow from Claude the concrete `high_confidence_abstain_margin` default-disabled manifest field, JVM tests for abstain/keep/no-op behavior, top-3 accuracy in the scorecard, leave-one-species-out / clean-vs-perturbation validation as a task, Phase 0 evidence scaffolding, manifest-gated preprocessing defaults, and the rule that threshold tuning must be rerun if preprocessing changes are adopted.

Do not borrow Claude's CC-BY-SA allowance for test fixtures or `<kb-species-id>.jpg` fixture naming. Those weaken the hard CC0/PD-only constraint and the independence needed for honest measurement.

From Gemini, keep the concise phase framing and the prominent wording of `Modify ModelScoreMapper to support an additional margin-based abstention check above the plain high-confidence gate`. Do not keep Gemini's loose overfit mitigation, vague metrics artifacts, or parallel Phase 2/Phase 3 sequencing.

## If I Were Merging

I would keep Claude's anti-overfit machinery: `Anti-overfit guard`, default-disabled `high_confidence_abstain_margin`, manifest-gated preprocessing/TTA, top-3 scorecard, and final retune-after-preprocessing sequencing.

I would keep Gemini's simple measure-abstain-improve phase names and its direct emphasis on the above-plain margin abstention task.

I would keep Codex's stricter CC0/PD license discipline, multi-photo fixture naming, fixture integrity checks, detailed CSV/schema evidence, rejected-source documentation, and global-first threshold policy.
