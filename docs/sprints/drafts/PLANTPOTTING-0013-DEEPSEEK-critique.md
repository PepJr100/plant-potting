# PLANTPOTTING-0013 — Critique of CODEX and CLAUDE drafts (from the DEEPSEEK author)

Scope: I (DEEPSEEK draft) compare both sibling drafts against mine on five axes — stronger,
weaker, missing tasks, underweighted risks, wrong sequencing — then say what I''d merge. All
three agree on shape (Thread A conditional direct Pilea card behind elevated
`per_species_thresholds` composed with boundary gate; Thread B deepen 8 single-photo species;
doc cleanup; v0.7.0 + tag). Disagreements are in method rigour, which invariant is
load-bearing, and how deeply each draft confronts the n=1 pothos reality.

---

## Draft CODEX (`PLANTPOTTING-0013-CODEX.md`)

### Stronger than mine

- **The offline threshold-simulation script (`Phase 3` — `pilea-direct-card-threshold-sweep.csv`).**
  This is the best single idea in any of the three drafts. CODEX formalises a reusable,
  device-independent sweep script that decouples LOO threshold tuning from the slow,
  fragile GMD device-CSV-pull cycle. My A2 tunes off the pulled CSV in prose; a script
  that emits the full fold table (fold id, tuning photos, held-out, threshold, margin,
  route, correctness, confident-wrong, pothos sentinel) is a concrete, auditable
  artifact. Both mine and CLAUDE''s are weaker here. I would import this wholesale.

- **Pre-committed threshold grid before validation (`Phase 2` — "Pre-commit the candidate
  threshold grid before validation").** CODEX commits the full candidate set before seeing
  any fold outcome, which is stricter anti-overfit discipline than my per-fold LOO tuning in A2.
  I tune from the pulled CSV but don''t pre-register the grid.

- **"Read the 0012 evidence first" (`Phase 1`).** CODEX explicitly makes reading
  `boundary-gating-decision.md`, `post-pilea-gated-summary.md`, and `feedback.md` a
  checkbox task. My draft references those files in the Intent but never turns them into
  an executable Phase 1 step. Cleaner cold-start.

- **`AccuracyEvalCsvSchemaTest` schema-drift guard (`Phase 8`).** CODEX flags that
  growing the fixture set could break the CSV schema check and tasks an explicit
  "update or add the schema guard without dropping required columns." Neither mine nor
  CLAUDE''s draft catches this. It closes a real gap.

- **Explicit GitHub Release result check (`Phase 10`).** CODEX requires "Confirm the GitHub
  Release workflow published or record the exact CI failure if the tag push did not produce
  a release." My S5 says the workflow triggers on pushed tags; CODEX closes the loop by
  verifying the result. Both CLAUDE and I only assert the trigger.

### Weaker than mine

- **Margin treated as a real co-equal separator bar — and the score-shape says it can''t.**
  CODEX''s `Phase 2` requires a "measured top1-top2 margin" as part of the direct-card
  candidate rule, and `Phase 3` sweeps both threshold and margin in the grid. This is
  CODEX''s biggest analytical gap. Both CLAUDE''s A0/A2 and my own Risk #2 + the 0.9661
  analysis show the known pothos→Pilea failure has no second-place mass, just like the
  real Pilea signal — so margin cannot discriminate. CODEX risks an executor wasting a
  phase tuning a dead lever, or worse, banking false safety on it.

- **No config-level CI bind that `per_species_thresholds["pilea-peperomioides"] > 0.9661`.**
  CODEX''s `Phase 5` adds a synthetic mapper sentinel test (`pothosToPileaSentinelNever
  ReturnsDirectPileaCard`), which is good — but tests the mapper against a *synthetic* row,
  not the manifest config file. Nothing CI-binds the actual shipped `model_manifest.json`
  value to stay above the pothos ceiling. CLAUDE''s `Phase A3` `ModelManifestTest` extension
  is the compensating control CODEX lacks.

- **No empirical per-pothos-fixture breakdown.** My `Risk #3` notes that only ONE of 7
  pothos fixtures (`epipremnum-aureum.jpg`) mispredicts as Pilea — the other 6 pothos
  fixtures raw-predict different top-1 labels (pothos, alocasia, calathea, monstera).
  CODEX treats pothos→Pilea as a population-level problem without quantifying it. This
  matters because `T_pilea` is being fitted above a *single* data point, which is far
  more overfit-prone. CODEX''s risks list never asks "how many pothos fixtures actually
  fail?"

- **No named-author LOO separation.** My `A2` names the six Pilea fixture authors so an
  executor can immediately partition by author for LOO. CODEX describes author/source-photo
  separation in Phase 2 but at the conceptual level — it never makes it operational with
  the actual author column.

- **No honesty about CI unenforceability of the device-eval invariant.** My `A8` explicitly
  states the on-device eval cannot run in CI; the 0-pothos-to-direct-Pilea invariant is
  a manual/review gate enforced at sprint-close. CODEX lists the eval as a gate but never
  addresses whether it''s CI-bound. CLAUDE''s manifest-level tripwire compensates for this;
  CODEX has neither the compensation nor the honesty.

### Missing tasks

- The **pothos-fixture population analysis** (my Risk #3). CODEX should add a task to pull
  raw predictions for ALL pothos fixtures, not just the one known failure, and record
  whether pothos→Pilea is a single-fixture or multi-fixture phenomenon before setting
  `T_pilea`.
- The **named-author partition for LOO** (my A2). CODEX describes author separation but
  should make it operational with the actual author-column data from `fixture-manifest.tsv`.
- The **release workflow trigger** as a named task with the actual tag-push mechanics
  (my S5). CODEX''s tag-push task exists but is less specific about the workflow file path.
- The **sprint feedback artifact** (my S6). CODEX lists an acceptance criterion but not
  a named, scheduled task for writing it.
- **A8-level CI-honesty language.** CODEX should acknowledge that the 0-pothos→direct-Pilea
  invariant cannot be CI-bound and must be a manual/review gate.

### Underweighted risks

- **The n=1 pothos overfit risk.** CODEX''s risks list says "A high Pilea score alone still
  admits the known pothos→Pilea failure" but never asks whether the failure is one fixture
  or many. Fitting a threshold above a single data point is far more fragile than fitting
  above a distribution. CODEX doesn''t confront this.
- **Threshold-direction math.** CODEX doesn''t explicitly state the constraint that
  `T_pilea` must be above the max-pothos→Pilea score AND at or below the min-real-Pilea
  score — and that these two may overlap (as CLAUDE''s gap-sign abort catches). Without
  naming the math, the sweep could look like "pick a value that works for most folds"
  rather than "prove a gap exists."
- **LTGC / model-manifest drift from `_comment` conventions.** My draft references the 0006
  `_comment` precedent for manifest annotation. CODEX doesn''t reference prior manifest
  conventions, so an executor might land threshold values without documentation context.

### Wrong sequencing

- **Margin tuning before the score-shape analysis.** CODEX''s `Phase 2` commits margin as
  a candidate bar before establishing whether margin can even separate the two failure
  modes. The score-shape analysis (real Pilea vs. pothos→Pilea score distribution) must
  precede any margin design. If it shows no separation, drop margin from the candidate
  rule — don''t sweep it.
- **Tests after post-change eval.** CODEX''s `Phase 5` (unit/contract tests) is placed
  between the Phase 4 implementation and the Phase 6 post-change accuracy eval. Tests
  should gate the eval, not follow it. If a config test catches `T_pilea ≤ 0.9661`, you
  want that caught before spending time on a device eval.

---

## Draft CLAUDE (`PLANTPOTTING-0013-CLAUDE.md`)

### Stronger than mine

- **Phase A0 — "The central problem, stated honestly."** CLAUDE names the crux explicitly:
  the real Pilea signal (~0.98, no second-place mass) and the pothos→Pilea failure (0.9661,
  no second-place mass) are near-identical in score shape — top1-top2 margin cannot separate
  them, so the only lever is absolute top-1 score. My draft says the confusion is asymmetric
  and names the two score values, but never crystallises this into a single "here is why
  margin is dead" framing. CLAUDE''s A0 makes the entire Thread A method legible.

- **Phase A2 — "Rejected — margin-over-second as the separator."** CLAUDE explicitly
  documents margin as a rejected mechanism with the mathematical reasoning. My draft
  doesn''t include margin in the direct-card rule (I use only the elevated threshold composed
  with the boundary gate), but I never explain *why* margin is absent. CLAUDE''s rejected-
  mechanism note prevents a future executor from re-proposing it.

- **Config-level CI bind: `per_species_thresholds["pilea-peperomioides"] > 0.9661`
  (`Phase A3`, `ModelManifestTest`).** This is the single most important tripwire either
  sibling has that I lack. My `A8` correctly says the on-device eval cannot be CI-bound
  and must be a manual/review gate — but I stop there. CLAUDE compensates with a JVM-level
  manifest-parsing test that fails CI if the shipped Pilea threshold is ≤ 0.9661. That is
  enforceable in CI and is the compensating control for my A8''s honesty. CODEX also lacks
  this.

- **Separation-gap-sign early-abort rule (`Phase A1`).** CLAUDE''s `Phase A1` says: if
  (min real-Pilea top-1) − (max pothos→Pilea top-1) ≤ 0, no safe `T_pilea` exists →
  fall back immediately, before any tuning. My A2 runs LOO tuning assuming a safe threshold
  exists; I never build in the crisp go/no-go that says "if the distributions overlap,
  abort." My own Risk #3 (the n=1 pothos reality) is exactly the input to this stopping
  rule, but I never close that loop.

- **Dormant-safe implementation path (`Phase A4`).** CLAUDE keeps the gate-refinement code
  in `ModelScoreMapper` even when `per_species_thresholds` is empty — empty threshold → no
  escape → strict-picker unchanged. My A3 and A6 say to implement then potentially revert.
  CLAUDE''s approach means the code that was tested and reviewed can ship dormant, which is
  safer than reverting untested code paths after a fallback decision.

- **ROADMAP.md update as an explicit task.** CLAUDE''s ship phase says update `docs/ROADMAP.md`
  with the 0013 result. My non-goals say ROADMAP is already accurate, "no change needed."
  Given the direct-card decision (ship or fall-back) and the fixture-count delta from
  Thread B, a ROADMAP note is likely warranted. CLAUDE handles this better.

### Weaker than mine

- **No named-author LOO partition.** CLAUDE''s A1 describes LOO + author/source separation
  conceptually but never operationalises it. My `A2` names the six Pilea fixture authors
  and tasks an explicit author-based exclusion list per fold, which makes the separation
  immediately executable and reviewer-checkable.

- **No empirical per-pothos-fixture breakdown.** CLAUDE''s A0/A1 treat the pothos→Pilea
  failure as a single score (0.9661) but never ask how many of the 7 pothos fixtures
  actually mispredict as Pilea. My `Risk #3` does this and reports the per-fixture top-1
  labels: only `epipremnum-aureum.jpg` fails; the other 6 pothos fixtures predict
  different top-1 species. This is material: if pothos→Pilea is a single-fixture phenomenon,
  the threshold is being tuned above one data point and the fail-safe (boundary gate) is
  load-bearing in a way CLAUDE''s draft doesn''t fully price in.

- **Fixture sourcing policy accepts CC-BY.** CLAUDE''s Thread B explicitly accepts `CC-BY`
  real photographs and tasks adding `docs/licenses/` rows. My Thread B keeps the same
  CC0/PD-only discipline the 0012 baseline set used. My non-goals say "CC0 fixture" and
  the sourcing approach names Unsplash/Pexels/Wikimedia/GBIF CC0. CLAUDE''s looser policy
  dilutes the license-clean guarantee that makes the headline accuracy number honest.

- **No line-precise code anchors.** CLAUDE''s "Repo anchors" table lists files but no line
  numbers. My repo-anchor table includes line-precise references (`ModelScoreMapper.kt`
  69–74, `perSpeciesThresholds` 0005 §5.3, the 0006 `_comment` precedent). An executor
  opening `ModelScoreMapper.kt` at line 69 knows exactly where the boundary-gate check
  lives.

- **No explicit sprint feedback task.** I have `S6` ("Review feedback written to
  `docs/sprints/feedback/PLANTPOTTING-0013/feedback.md`") as a named, scheduled task.
  CLAUDE''s acceptance criteria list feedback as an artifact but never make it a Phase
  task.

- **No explicit release workflow trigger task.** I have `S5` with the `.github/workflows/
  release.yml` path and "fires on pushed `v*` tags." CLAUDE''s acceptance criteria say
  "tag pushed so the public GitHub Release with the APK is published" but don''t name the
  workflow file or task the explicit push-and-confirm step.

### Missing tasks

- The **named-author LOO partition** with the actual fixture-manifest author column
  (my A2). CLAUDE should add this to make A1''s author separation operational.
- The **per-pothos-fixture raw-prediction pull** (my Risk #3). CLAUDE should add a task to
  run raw predictions on ALL pothos fixtures (not just the known failure) before setting
  `T_pilea`, and record whether the failure is n=1 or multi-fixture.
- The **`_comment` field manifest-annotation convention** (my A4, referencing 0006
  precedent). CLAUDE''s manifest edit doesn''t reference the existing `_comment` pattern
  for documenting why a non-obvious config value exists.
- The **sprint feedback artifact** as a named Phase task (my S6).
- The **release workflow trigger** with the actual file path (my S5).
- The **`AccuracyEvalCsvSchemaTest` schema-drift guard** that CODEX catches and both
  CLAUDE and I miss.
- The **offline threshold-sweep script** that CODEX has and both CLAUDE and I lack.

### Underweighted risks

- **The n=1 pothos reality in method design.** CLAUDE''s A0/A1 correctly identifies the
  score-shape problem, and his A1 gap-sign abort is the right structural response — but
  his draft never quantifies *how many* pothos fixtures produce the dangerous score. Risk
  budgeting should distinguish "threshold tuned above a single data point" from "threshold
  tuned above a distribution." CLAUDE''s A0 names the 0.9661 value but doesn''t ask whether
  it generalises across pothos fixtures.
- **Schema-test drift from Thread B fixture growth.** Neither CLAUDE nor I flag this;
  CODEX''s Phase 8 does.
- **Windows Dropbox copy verification.** CLAUDE says "Verify the APK landed via PowerShell
  from the user''s terminal before claiming delivery" in risks but doesn''t make it a
  checkbox in the ship phase. My S4 ("APK verified present at `C:\Users\robev\...`") is
  more explicit.
- **`docs/licenses/` proliferation.** CLAUDE tasks adding license rows for CC-BY sources,
  but the existing `docs/licenses/` directory may need structural changes or a README
  update to accommodate the wider license surface. Neither draft addresses this.

### Wrong sequencing

- **CLAUDE''s sequencing is largely correct** and close to mine (A1 → A2 → A3 → A4
  sequential with gap-sign early-abort, B and C parallel, ship gates on all). One soft
  spot: the `ModelManifestTest` config guard (A3) should conceptually precede the
  gate-refinement implementation, not follow it in Phase A2 — the config constraint
  should constrain the code, not the reverse. But A3 is "tests first" within its own
  phase, which mostly corrects this. No material sequencing error.

---

## If I were merging

**Keep from CODEX:**

1. **Offline threshold-simulation script + `pilea-direct-card-threshold-sweep.csv`**
   (Phase 3) — the highest-value import across ALL drafts. Replaces my prose-only A2
   fold table with a reusable, device-independent artifact.
2. **Pre-committed threshold grid before validation** (Phase 2) — stricter anti-overfit
   discipline than my per-fold derivation. Register candidates before seeing any fold.
3. **"Read 0012 evidence first" Phase 1 task** — cleaner cold-start than my implicit
   references.
4. **`AccuracyEvalCsvSchemaTest` schema-drift guard** (Phase 8) — closes a gap both
   CLAUDE and I miss.
5. **Explicit GitHub Release result confirmation** (Phase 10) — closes the tag-push loop
   that CLAUDE and I only assert as a trigger.

**Retain from mine (DEEPSEEK) — neither sibling has these and they''re load-bearing:**

1. **Named-author LOO partition** (A2) — the six Pilea fixture authors make author-
   separation immediately executable and reviewer-checkable. Neither CODEX nor CLAUDE
   operationalise this.
2. **Empirical per-pothos-fixture prediction breakdown** (Risk #3) — the finding that
   only ONE of 7 pothos fixtures mispredicts as Pilea is the single most important
   empirical input to the conservatism of `T_pilea`. Promote it from a risk into a
   Phase A1 method input: if pothos→Pilea is n=1, the fail-safe is load-bearing.
3. **Line-precise code anchors** — `ModelScoreMapper.kt` 69–74, `perSpeciesThresholds`
   0005 §5.3, the 0006 `_comment` precedent. An executor opening any file at the cited
   line knows exactly where the relevant logic lives. Keep as the repo-anchor table.
4. **A8 CI-honesty language** — the 0-pothos→direct-Pilea invariant cannot be CI-bound;
   it is a manual/review gate. This honesty creates the *demand* for CLAUDE''s manifest
   CI bind.
5. **Explicit release workflow trigger** (S5) with the `.github/workflows/release.yml`
   path and tag-push mechanics.
6. **Sprint feedback artifact** (S6) as a named, scheduled task.

**Keep from CLAUDE — neither CODEX nor I have these and they''re non-negotiable:**

1. **Phase A0 — "The central problem, stated honestly"** — the score-shape crux (margin
   cannot separate; absolute top-1 is the only lever). This kills CODEX''s futile margin
   bar and justifies why my draft omits margin from the direct-card rule without
   explanation.
2. **Phase A2 — "Rejected — margin-over-second as the separator"** — documents the
   rejected mechanism so no future executor re-proposes it.
3. **Config-level CI bind: `per_species_thresholds["pilea-peperomioides"] > 0.9661`**
   (`Phase A3`, `ModelManifestTest`) — the ONE tripwire that makes "pothos→Pilea can
   never reach a direct card" enforceable at CI, compensating for my A8''s honesty that
   the device eval cannot be CI-bound.
4. **Separation-gap-sign early-abort rule** (Phase A1) — if min(real-Pilea top-1) ≤
   max(pothos→Pilea top-1), no safe `T_pilea` exists → fall back before tuning. The
   crisp go/no-go that my A2 assumes away.
5. **Dormant-safe implementation path** (Phase A4) — keep the gate-refinement code
   tested and shipping even when `per_species_thresholds` is empty, so the empty case
   preserves strict-picker byte-for-byte. Safer than my implement-then-potentially-
   revert approach.

**Net merged plan:** CODEX''s tuning rigour (offline sweep + pre-committed grid + schema
guard) running on my empirical grounding (named authors, n=1 pothos reality, precise
code anchors), gated by CLAUDE''s three non-negotiable invariants (margin-is-not-the-
separator crux, manifest-threshold CI-bound > 0.9661, gap-sign early-abort). The
conditional-ship-or-fall-back spine, Thread B CC0/PD-only discipline, doc-cleanup scope,
and v0.7.0+tag ship signal carry over from all three drafts as-is.
